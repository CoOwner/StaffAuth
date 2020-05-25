package me.conflicted.staffauth.utils;

import me.conflicted.staffauth.*;
import javax.mail.internet.*;
import javax.mail.*;
import com.mongodb.*;
import java.util.*;

public class Mail
{
    private static String username;
    private static String password;
    private static StaffAuth auth;
    private static DBCollection emails;
    
    public static void sendCode(final String to, final String playerName, final String code) {
        try {
            final Properties props = new Properties();
            ((Hashtable<String, String>)props).put("mail.smtp.starttls.enable", "true");
            ((Hashtable<String, String>)props).put("mail.smtp.auth", "true");
            ((Hashtable<String, String>)props).put("mail.smtp.host", "smtp.gmail.com");
            ((Hashtable<String, String>)props).put("mail.smtp.port", "587");
            final Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(Mail.username, Mail.password);
                }
            });
            final Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(StaffAuth.getInstance().getConfig().getString("Mail.mail")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Your authentication code");
            message.setText("Dear " + playerName + ",\n\n Here's your login code, " + code);
            Transport.send(message);
        }
        catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean hasEmail(final UUID u) {
        if (Mail.auth.getConfig().getString("Settings.storage-sys").equalsIgnoreCase("file")) {
            if (Mail.auth.getConfig().contains("Players-Mail." + u)) {
                return true;
            }
        }
        else if (Mail.auth.getConfig().getString("Settings.storage-sys").equalsIgnoreCase("database")) {
            Mail.emails = Mail.auth.getMongo().getDatabase().getCollection("emails");
            final BasicDBObject query = new BasicDBObject("UUID", u.toString());
            final DBCursor c = Mail.emails.find(query);
            return c.hasNext();
        }
        return false;
    }
    
    static {
        Mail.username = StaffAuth.getInstance().getConfig().getString("Mail.mail");
        Mail.password = StaffAuth.getInstance().getConfig().getString("Mail.pass");
        Mail.auth = StaffAuth.getInstance();
    }
}
