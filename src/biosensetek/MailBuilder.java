package biosensetek;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MailBuilder implements Closeable {

	private static HashMap<String, Properties> settings = null;

	public static final void init(File dir) {
		if (settings == null) settings = new HashMap<>();
		Properties setting;
		for (File file : dir.listFiles()) {
			if ((setting = Utils.load(file)) == null) continue;
			if (setting.size() == 0) continue;
			settings.put(file.getName(), setting);
		}
	}

	public static final void destroy() {
		settings = null;
		authenticators = null;
	}

	private static HashMap<String, PasswordAuthenticator> authenticators = null;

	private static final Authenticator authenticator(Properties setting) {
		if (Utils.bool(setting.getProperty("mail.smtp.auth"))) {
			String usr, pwd;
			if ((usr = StringUtils.trimToNull(setting.getProperty("mail.usr"))) != null && (pwd = StringUtils.trimToNull(setting.getProperty("mail.pwd"))) != null) {
				if (authenticators == null) authenticators = new HashMap<>();
				String key = Base64.encodeBase64URLSafeString(DigestUtils.md5(usr + '+' + pwd));
				PasswordAuthenticator val = authenticators.get(key);
				if (val == null) authenticators.put(key, val = new PasswordAuthenticator(usr, pwd));
				return val;
			}
		}
		return null;
	}

	private static final class PasswordAuthenticator extends Authenticator {
		private PasswordAuthentication authentication = null;

		private PasswordAuthenticator(String usr, String pwd) {
			authentication = new PasswordAuthentication(usr, pwd);
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return authentication;
		}
	}

	//

	public static final MailBuilder create(String name) throws MessagingException {
		return new MailBuilder(settings.get(name));
	}

	private MimeMessage message;

	private String from, subject, content;

	public String from() {
		return from;
	}

	public String subject() {
		return subject;
	}

	public String content() {
		return content;
	}

	public MailBuilder(Properties setting) throws MessagingException {
		message = new MimeMessage(Session.getInstance(setting, authenticator(setting)));
		from(setting.getProperty("mail.from")).bcc(setting.getProperty("mail.bcc"));
	}

	public MailBuilder from(String from) throws MessagingException {
		if ((from = StringUtils.trimToNull(from)) != null) {
			InternetAddress addr = new InternetAddress(from);
			message.setFrom(addr);
			message.setSender(addr);
			this.from = from;
		}
		return this;
	}

	public MailBuilder to(String to) throws MessagingException {
		if ((to = StringUtils.trimToNull(to)) != null) {
			message.addRecipients(Message.RecipientType.TO, to);
		}
		return this;
	}

	public MailBuilder cc(String... cc) throws MessagingException {
		if (cc != null && cc.length > 0) {
			for (String s : cc) {
				if ((s = StringUtils.trimToNull(s)) != null) {
					message.addRecipients(Message.RecipientType.CC, s);
				}
			}
		}
		return this;
	}

	public MailBuilder bcc(String... bcc) throws MessagingException {
		if (bcc != null && bcc.length > 0) {
			for (String s : bcc) {
				if ((s = StringUtils.trimToNull(s)) != null) {
					message.addRecipients(Message.RecipientType.BCC, s);
				}
			}
		}
		return this;
	}

	public MailBuilder subject(CharSequence subject) throws MessagingException {
		message.setSubject(this.subject = String.valueOf(subject), "UTF-8");
		return this;
	}

	public MailBuilder content(CharSequence content) throws MessagingException {
		MimeMultipart multipart = new MimeMultipart();
		MimeBodyPart body = new MimeBodyPart();
		body.setContent(this.content = String.valueOf(content), "text/html;charset=\"UTF-8\"");
		multipart.addBodyPart(body);
		message.setContent(multipart);
		return this;
	}

	public MailBuilder content(CharSequence content, String template, CharSequence baseUri) throws MessagingException, IOException {
		Document doc = Jsoup.parse(new File(template), "UTF-8", String.valueOf(baseUri));
		doc.select("#content").html(String.valueOf(content));
		doc.select("[href]").forEach(e -> e.attr("href", e.attr("abs:href")));
		doc.select("[src]").forEach(e -> e.attr("src", e.attr("abs:src")));
		return content(doc.html());
	}

	public void send() throws MessagingException {
		message.setSentDate(new Date());
		Transport.send(message);
	}

	@Override
	public void close() {
		message = null;
	}
}