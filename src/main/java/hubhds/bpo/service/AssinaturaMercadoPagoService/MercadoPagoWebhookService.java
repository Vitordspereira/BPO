package hubhds.bpo.service.AssinaturaMercadoPagoService;

import hubhds.bpo.model.preCadastro.PreCadastro;
import hubhds.bpo.model.usuario.Usuario;
import hubhds.bpo.repository.preCadastro.PreCadastroRepository;
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
    private final PreCadastroRepository preCadastroRepository;

    @Value("${mercadopago.mock:false}")
    private boolean mockAtivo;

    @Value("${mercadopago.mock-user-id:1}")
    private Long mockUserId;

    public MercadoPagoWebhookService(
            AssinaturaMercadoPagoService assinaturaMercadoPagoService,
            UsuarioRepository usuarioRepository,
            PreCadastroRepository preCadastroRepository
    ) {
        this.assinaturaMercadoPagoService = assinaturaMercadoPagoService;
        this.usuarioRepository = usuarioRepository;
        this.preCadastroRepository = preCadastroRepository;
    }

    @Transactional
    public void processar(Map<String, Object> payload) {
        Object dataObj = payload.get("data");

        if (!(dataObj instanceof Map<?, ?> dataMap)) {
            return;
        }

        Object idObj = dataMap.get("id");

        if (idObj == null) {
            return;
        }

        String preapprovalId = String.valueOf(idObj);
        System.out.println("Webhook do Mercado Pago  recebido para preapprovalId: " + preapprovalId);

        if (mockAtivo) {
            Usuario usuario = usuarioRepository.findById(mockUserId)
                    .orElseThrow(() -> new RuntimeException("Usuário de teste não encontrado"));

            usuario.setMpPreapprovalId(preapprovalId);
            usuario.setMpExternalReference("telefone:" + usuario.getTelefone());
            usuario.setMpStatus("authorized");
            usuario.setMpAssinaturaAtualizadaEm(LocalDateTime.now());
            usuario.setAssinaturaAtiva(true);
            usuario.setDataInatividade(null);

            usuarioRepository.save(usuario);
            return;
        }

        Map<String, Object> assinatura = assinaturaMercadoPagoService.consultarAssinatura(preapprovalId);

        String status = valor(assinatura.get("status")).toLowerCase();
        String externalReference = valor(assinatura.get("external_reference"));

        if (externalReference != null && externalReference.startsWith("pre_cadastro:")) {
            atualizarPreCadastro(preapprovalId, externalReference, status);
            return;
        }

        System.out.println("STATUS MP RECEBIDO = " + status);
        System.out.println("EXTERNAL REFERENCE = " + externalReference);

        Usuario usuario = usuarioRepository.findByMpPreapprovalId(preapprovalId)
                .or(() -> usuarioRepository.findByMpExternalReference(externalReference))
                .orElseGet(() -> buscarPorExternalReference(externalReference));

        if (
                ("authorized".equalsIgnoreCase(usuario.getMpStatus()) ||
                        "approved".equalsIgnoreCase(usuario.getMpStatus()))
                        && "pending".equalsIgnoreCase(status)
        ) {
            System.out.println("Webhook do Mercado Pago ignorado: tentativa de voltar usuário autorizado para pending.");
            return;
        }

        usuario.setMpPreapprovalId(preapprovalId);
        usuario.setMpExternalReference(externalReference);
        usuario.setMpStatus(status);
        usuario.setMpAssinaturaAtualizadaEm(LocalDateTime.now());

        switch (status) {
            case "authorized":
            case "approved":
                usuario.setAssinaturaAtiva(true);
                usuario.setDataInatividade(null);
                break;

            case "pending":
            case "paused":
            case "cancelled":
            case "canceled":
            case "rejected":
                usuario.setAssinaturaAtiva(false);

                if (usuario.getDataInatividade() == null) {
                    usuario.setDataInatividade(LocalDateTime.now());
                }
                break;

            default:
                break;
        }

        usuarioRepository.save(usuario);
    }

    private Usuario buscarPorExternalReference(String externalReference) {
        if (externalReference == null || !externalReference.startsWith("telefone:")) {
            throw new RuntimeException("Assinatura inválido: " + externalReference);
        }

        String telefone = externalReference.replace("telefone:", "").trim();

        return usuarioRepository.findByTelefone(telefone)
                .orElseThrow(() -> new RuntimeException(
                        "Usuário não encontrado para assinatura: " + externalReference
                ));
    }

    private String valor(Object object) {
        return object == null ? "" : String.valueOf(object);
    }

    private void atualizarPreCadastro(String preapprovalId, String externalReference, String status) {
        PreCadastro preCadastro = preCadastroRepository.findByMpPreapprovalId(preapprovalId)
                .or(() -> preCadastroRepository.findByMpExternalReference(externalReference))
                .orElseThrow(() -> new RuntimeException(
                        "Cadastro não encontrado para assinatura: " + externalReference
                ));

        if (
                ("authorized".equalsIgnoreCase(preCadastro.getMpStatus()) ||
                        "approved".equalsIgnoreCase(preCadastro.getMpStatus()))
                        && "pending".equalsIgnoreCase(status)
        ) {
            System.out.println("Webhook do Mercado Pago ignorado: tentativa de voltar cadastro autorizado para pending.");
            return;
        }

        preCadastro.setMpPreapprovalId(preapprovalId);
        preCadastro.setMpExternalReference(externalReference);
        preCadastro.setMpStatus(status);
        preCadastro.setAtualizadoEm(LocalDateTime.now());

        preCadastroRepository.save(preCadastro);

        System.out.println("Cadastro atualizado.");
        System.out.println("Preapproval ID: " + preapprovalId);
        System.out.println("Assinatura: " + externalReference);
        System.out.println("Status: " + status);
    }
}