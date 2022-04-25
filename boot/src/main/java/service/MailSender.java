package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import java.util.List;

@Component
public class MailSender {
    @Autowired
    private JavaMailSender javaMailSender;
    private Wiser wiser;
    private String userTo = "user2@localhost";
    private String userFrom = "user1@localhost";
    private String subject = "Test subject";
    private String textMail = "Text subject mail";

    public void sendMail() {
        final int TEST_PORT = 25;
        wiser = new Wiser(TEST_PORT);
        wiser.start();
        SimpleMailMessage message = composeEmailMessage();
        javaMailSender.send(message);
        List<WiserMessage> messages = wiser.getMessages();
    }

    private SimpleMailMessage composeEmailMessage() {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(userTo);
        mailMessage.setReplyTo(userFrom);
        mailMessage.setFrom(userFrom);
        mailMessage.setSubject(subject);
        mailMessage.setText(textMail);
        return mailMessage;
    }
}
