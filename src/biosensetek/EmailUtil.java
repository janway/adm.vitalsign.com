package biosensetek;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @deprecated use {@link MailBuilder}
 */
@Deprecated
public class EmailUtil {

	private static final Properties settings = new Properties();

	public static final Properties load(String path) {
		synchronized (settings) {
			try (InputStream in = new FileInputStream(path)) {
				settings.load(in);
			} catch (Throwable e) {}
		}
		return settings;
	}

	public static final Document load(String path, CharSequence baseUri) throws IOException {
		return Jsoup.parse(new File(path), "UTF-8", String.valueOf(baseUri));
	}

	public static String bind(Document template, CharSequence content) {
		Document email = template.clone();
		email.select("#content").html(String.valueOf(content));
		for (Element e : email.select("[href]")) {
			e.attr("href", e.attr("abs:href"));
		}
		for (Element e : email.select("[src]")) {
			e.attr("src", e.attr("abs:src"));
		}
		return email.html();
	}

	public static boolean send(String to, String cc, CharSequence subject, CharSequence content) {
		MimeMessage message = new MimeMessage(Session.getInstance(settings, null));
		String from = settings.getProperty("mail.from");
		String bcc = settings.getProperty("mail.bcc");
		try {
			InternetAddress addr = new InternetAddress(from);
			message.setFrom(addr);
			message.setSender(addr);
			message.addRecipients(Message.RecipientType.TO, to);
			if (cc != null && cc.length() > 0) message.addRecipients(Message.RecipientType.CC, cc);
			if (bcc != null && bcc.length() > 0) message.addRecipients(Message.RecipientType.BCC, bcc);
			message.setSubject(subject instanceof String ? (String) subject : String.valueOf(subject));
			MimeMultipart multipart = new MimeMultipart();
			BodyPart body = new MimeBodyPart();
			body.setContent(content instanceof String ? (String) content : String.valueOf(content), "text/html;charset=\"UTF-8\"");
			multipart.addBodyPart(body);
			message.setContent(multipart);
			Transport.send(message);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void main(String[] args) throws IOException {
		EmailUtil.load("web/WEB-INF/email.properties");
		String to = "gavin@global-opto.com";
		String subject = "[哇酷]註冊申請";
		StringBuffer buffer = new StringBuffer();
		buffer.append("<p>親愛的 <a href=\"\">WoWKool</a> 會員 sfdsfds 您好:</p>");
		buffer.append("<p>這封認證信是由 <a href=\"\">WoWKool</a> 發出，用以確認您的會員註冊。如果確定是您本人時，請點選下方連結以正式啟用您的會員帳戶。</p>");
		buffer.append("<p><a href=\"signup.html?t=ea61eae4776f47d36c028a137ddd4900\">啟用</a></p>");
		buffer.append("<p>如果您未曾於 <a href=\"\">WoWKool</a> 註冊，請忽略本通知或至我們的 <a href=\"inquiry.html\">線上客服</a> 回報資料冒用。</p>");
		buffer.append("<p>如您未於 3 日內完成認證手續，您的會員註冊申請資料將會自動註銷。</p>");
		Document template = EmailUtil.load("web/WEB-INF/email-template.html", "http://127.0.0.1:8081/");
		String content = EmailUtil.bind(template, buffer);
		boolean result = EmailUtil.send(to, null, subject, content);
	}
}