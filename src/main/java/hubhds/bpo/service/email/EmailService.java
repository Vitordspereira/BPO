package hubhds.bpo.service.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final String remetente;

    public EmailService(
            JavaMailSender javaMailSender,
            @Value("${spring.mail.username}") String remetente
    ) {
        this.javaMailSender = javaMailSender;
        this.remetente = remetente;
    }

    public void enviarRedefinicaoSenha(String destinatario, String link) {
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();

            mensagem.setFrom(remetente);
            mensagem.setTo(destinatario);
            mensagem.setSubject("Redefinição de senha - BPO HDS");
            mensagem.setText(
                    "Olá!\n\n" +
                            "Recebemos uma solicitação para redefinir sua senha.\n\n" +
                            "Clique no link abaixo para criar uma nova senha:\n\n" +
                            link + "\n\n" +
                            "Esse link expira em 10 minutos.\n\n" +
                            "Se você não solicitou essa alteração, ignore este e-mail.\n\n" +
                            "Equipe HDS"
            );

            javaMailSender.send(mensagem);

        } catch (MailException e) {
            throw new RuntimeException("Erro ao enviar e-mail de redefinição de senha: " + e.getMessage(), e);
        }
    }
}