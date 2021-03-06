package server;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * The Class SendMail controls the mail sending from the system to the users.
 */
public class SendMail implements Runnable{

	/** The station ID. */
	int stationID;
	
	/** The message content. */
	String fuelType,mail,messageHeader,messageContent;
	
	/**
	 * Instantiates a new send mail.
	 *
	 * @param stationID the station ID
	 * @param fuelType the fuel type
	 * @param mail the mail
	 * @param messageHeader the message header
	 * @param messageContent the message content
	 */
	public SendMail(int stationID, String fuelType, String mail, String messageHeader, String messageContent) {
		this.stationID = stationID;
		this.fuelType = fuelType;
		this.mail = mail;
		this.messageHeader = messageHeader;
		this.messageContent = messageContent;
	}

	/**
	 * Run.
	 */
	public void run() {
		sendMailToStationManager(stationID, fuelType, mail, messageHeader, messageContent);
	}
	
    /**
     * Send mail to station manager.
     *
     * @param stationID the station ID
     * @param fuelType the fuel type
     * @param mail the mail
     * @param messageHeader the message header
     * @param messageContent the message content
     */
    public static void sendMailToStationManager(int stationID,String fuelType,String mail,String messageHeader,String messageContent) {
		
		final String username = "myfuelltm2020@gmail.com";
	      final String password = "hy3!Nf+4P_3b";

	      Properties props = new Properties();
	      props.put("mail.smtp.auth", true);
	      props.put("mail.smtp.starttls.enable", true);
	      props.put("mail.smtp.host", "smtp.gmail.com");
	      props.put("mail.smtp.port", "587");

	      Session session = Session.getInstance(props,
	              new javax.mail.Authenticator() {
	                  protected PasswordAuthentication getPasswordAuthentication() {
	                      return new PasswordAuthentication(username, password);
	                  }
	              });

	      try {

	          Message message = new MimeMessage(session);
	          message.setFrom(new InternetAddress("myfuelltm2020@gmail.com"));
//	          message.setRecipients(Message.RecipientType.TO,
//	                  InternetAddress.parse(mail));
	          message.setRecipients(Message.RecipientType.TO,
	                  InternetAddress.parse(mail));
	          message.setSubject(messageHeader);
	          message.setText(messageContent);
	          
	          //for attaching files if needed
	          
//	          MimeBodyPart messageBodyPart = new MimeBodyPart();
//
//	          Multipart multipart = new MimeMultipart();
//
//	          messageBodyPart = new MimeBodyPart();
//	          String file = "path of file to be attached";
//	          String fileName = "attachmentName";
//	          DataSource source = new FileDataSource(file);
//	          messageBodyPart.setDataHandler(new DataHandler(source));
//	          messageBodyPart.setFileName(fileName);
//	          multipart.addBodyPart(messageBodyPart);

//	          message.setContent(multipart);

	          System.out.println("Sending");

	          Transport.send(message);

	          System.out.println("Done");

	      } catch (MessagingException e) {
	          e.printStackTrace();
	      }
		
	}
	
}
