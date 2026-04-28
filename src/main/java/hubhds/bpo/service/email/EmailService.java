package hubhds.bpo.service.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void enviarRedefinicaoSenha(String destinatario, String link) {
        SimpleMailMessage mensagem = new SimpleMailMessage();

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
    }
}