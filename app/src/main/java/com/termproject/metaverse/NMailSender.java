package com.termproject.metaverse;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class NMailSender extends javax.mail.Authenticator {
    String mailhost = "smtp.naver.com";
    String user = "vjsdy1@naver.com";
    String password = "na.dlatl1234";
    String emailCode, diaMessage = "";

    public String getEmailCode() {  // 생성된 이메일 인증코드

        return emailCode;
    }

    public String getDiaMassage() {
        String dia = "인증코드가 발송되었습니다.\n메일함을 확인해주세요.";

        return dia;
    }

    private String createEmailCode() {  // 이메일 인증코드 생성
        String[] str = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
                "t", "u", "v", "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String newCode = "";

        for (int x = 0; x < 6; x++) {
            int random = (int) (Math.random() * str.length);
            newCode += str[random];
        }

        return newCode;
    }

    public synchronized void sendMail(String recipient) {
        emailCode = createEmailCode();

        Properties props = new Properties();
        props.put("mail.smtp.host", mailhost);
        props.put("mail.smtp.port", 587);
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props,new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

            // 메일 제목
            message.setSubject("[메타버스] 이메일을 인증해주세요.");

            // 메일 내용
            message.setContent("안녕하세요.<br>메타버스 계정 보호를 위해 이메일 인증이 필요합니다.<br>"
                    + "다음 인증 코드를 입력해주세요.<br><br>인증 코드: [" + getEmailCode() + "]<br><br>감사합니다.", "text/html");

            // send the message
            Transport.send(message);

            diaMessage = "인증코드가 발송되었습니다.\n메일함을 확인해주세요.";

        } catch (MessagingException e) {
            e.printStackTrace();
        }

    } // NMailSender()
}
