/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

import com.ucod.lang.LocaleUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import static load.file.MainApp.ERROR;
import static load.file.MainApp.INFO;
import static load.file.MainApp.WARNING;

/**
 *
 * @author gcastillo
 */
public class EMail {

    private static String username = "";
    private static String password = "";
    private static String host = "";
    private static String port = "";
    private static String ip = "";
    private List<String> mailList;
    CustomLogger customLogger = CustomLogger.getInstance();

    public EMail(String user, String pass, String host, String port, String ip) {
        this.username = user;
        this.password = pass;
        this.host = host;
        this.port = port;
        this.ip = ip;
        mailList = new ArrayList<String>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void addAddress(String address) {
        if (!mailList.contains(address)) {
            mailList.add(address);
        }
    }

    public void removeAddress(String address) {
        mailList.remove(address);
    }

    public boolean sendMail(String text) {
        /*         final String username = "testingsystel@gmail.com";
         final String password = "Systel#4316";
         */
        boolean result = false;
        customLogger.writeLog(INFO, ip, LocaleUtil.getMessage("enviando email..."));

        if (mailList.isEmpty()) {
            customLogger.writeLog(WARNING, ip, LocaleUtil.getMessage("No hay email configurado"));

            return false;
        }
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        /*       props.put("mail.smtp.host", "smtp.gmail.com");
         props.put("mail.smtp.port", "587");
         */

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {

            // Define message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setSubject(LocaleUtil.getMessage("Proceso de importación balanzas Systel"));
            mailList.forEach((temp) -> {
                try {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(temp));
                } catch (AddressException ex) {
                    Logger.getLogger(EMail.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MessagingException ex) {
                    Logger.getLogger(EMail.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            // message.addRecipient(Message.RecipientType.TO, new InternetAddress("dbelatti@systel.com.ar"));
            //message.addRecipient(Message.RecipientType.TO, new InternetAddress("gcastillo@systel.com.ar"));
            message.setText(text);
            // Envia el mensaje
            Transport.send(message);

            customLogger.writeLog(INFO, ip, LocaleUtil.getMessage("email enviado..."));

            result = true;
        } catch (Exception e) {
            result = false;
            customLogger.writeLog(ERROR, ip, "Email Error..." + e.getMessage());
        }

        return result;
    }
}
