package hubhds.bpo.service.AssinaturaMercadoPagoService;

import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.repository.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class MercadoPagoWebhookService {

    private final AssinaturaMercadoPagoService assinaturaMercadoPagoService;
    private final UsuarioRepository usuarioRepository;

    //tirar depois
    @Value("${mercadopago.mock:false}")
    private boolean mockAtivo;

    @Value("${mercadopago.mock-user-id:1}")
    private Long mockUserId;

    public MercadoPagoWebhookService(AssinaturaMercadoPagoService assinaturaMercadoPagoService,
                                     UsuarioRepository usuarioRepository) {
        this.assinaturaMercadoPagoService = assinaturaMercadoPagoService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void processar(Map<String, Object> payload) {
        // O webhook normalmente vem com data.id
        Object dataObj = payload.get("data");
        if (!(dataObj instanceof Map<?, ?> dataMap)) {
            return;
        }

        Object idObj = dataMap.get("id");
        if (idObj == null) {
            return;
        }

        String preapprovalId = String.valueOf(idObj);
        System.out.println("Webhook recebido para preapprovalId: " + preapprovalId);

        // MOCK TEMPORÁRIO PARA TESTAR SEM MERCADO PAGO
        if (mockAtivo) {
            Usuario usuario = usuarioRepository.findById(mockUserId)
                    .orElseThrow(() -> new RuntimeException("Usuário de teste não encontrado"));

            usuario.setMpPreapprovalId(preapprovalId);
            usuario.setMpExternalReference("telefone:" + usuario.getTelefone());
            usuario.setMpStatus("autorizado");
            usuario.setMpAssinaturaAtualizadaEm(LocalDateTime.now());
            usuario.setAssinaturaAtiva(true);
            usuario.setDataInatividade(null);

            usuarioRepository.save(usuario);
            return;
        }

        // consulta a assinatura direto no Mercado Pago
        Map<String, Object> assinatura = assinaturaMercadoPagoService.consultarAssinatura(preapprovalId);

        String status = valor(assinatura.get("status")).toLowerCase();
        String externalReference = valor(assinatura.get("external_reference"));

        System.out.println("STATUS MP RECEBIDO = " + status);
        System.out.println("EXTERNAL REFERENCE = " + externalReference);

        Usuario usuario = usuarioRepository.findByMpPreapprovalId(preapprovalId)
                .or(() -> usuarioRepository.findByMpExternalReference(externalReference))
                .orElseGet(() -> buscarPorExternalReference(externalReference));

        // atualiza dados de vínculo da assinatura
        usuario.setMpPreapprovalId(preapprovalId);
        usuario.setMpExternalReference(externalReference);
        usuario.setMpStatus(status);
        usuario.setMpAssinaturaAtualizadaEm(LocalDateTime.now());

        switch (status) {
            case "authorized":
            case "autorizado":
                usuario.setAssinaturaAtiva(true);
                usuario.setDataInatividade(null);
                break;

            case "pending":
            case "pendente":
            case "paused":
            case "pausado":
            case "cancelled":
            case "cancelado":
                usuario.setAssinaturaAtiva(false);

                if (usuario.getDataInatividade() == null) {
                    usuario.setDataInatividade(LocalDateTime.now());
                }
                break;

            default:
                // só registra o status recebido, sem quebrar o fluxo
                break;
        }

        usuarioRepository.save(usuario);
    }

    private Usuario buscarPorExternalReference(String externalReference) {
        if (externalReference == null || !externalReference.startsWith("telefone:")) {
            throw new RuntimeException("External reference inválido: " + externalReference);
        }

        // CORREÇÃO:
        // trim() vem antes do Long.valueOf(...)
        String telefone = externalReference.replace("telefone:", "").trim();

        return usuarioRepository.findByTelefone(telefone)
                .orElseThrow(() -> new RuntimeException(
                        "Usuário não encontrado para external_reference: " + externalReference
                ));
    }

    private String valor(Object object) {
        return object == null ? "" : String.valueOf(object);
    }
}
