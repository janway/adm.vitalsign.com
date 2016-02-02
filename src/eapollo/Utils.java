package com.eapollo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.net.ssl.SSLContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.solr.common.SolrInputDocument;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Utils {
	public static final String PATTERN_DATE = "yyyy-MM-dd";
	public static final String PATTERN_TIME = "HH:mm:ss";
	public static final String PATTERN_DATETIME = new StringBuilder(PATTERN_DATE).append(StringUtils.SPACE).append(PATTERN_TIME).toString();

	// public static final SimpleDateFormat FMT_DATE = new SimpleDateFormat(PATTERN_DATE);
	// public static final SimpleDateFormat FMT_TIME = new SimpleDateFormat(PATTERN_TIME);
	// public static final SimpleDateFormat FMT_DATETIME = new SimpleDateFormat(PATTERN_DATETIME);

	public static String date(Date date) {
		if (date == null) return StringUtils.EMPTY;
		return DateFormatUtils.format(date, PATTERN_DATE);
	}

	public static String time(Date date) {
		if (date == null) return StringUtils.EMPTY;
		return DateFormatUtils.format(date, PATTERN_TIME);
	}

	public static String datetime(Date date) {
		if (date == null) return StringUtils.EMPTY;
		return DateFormatUtils.format(date, PATTERN_DATETIME);
	}

	public static Date date(long millis) {
		return new Date(millis);
	}

	public static Date today() {
		return DateUtils.truncate(new Date(), Calendar.DATE);
	}

	public static Date date(String input) {
		input = StringUtils.trimToNull(input);
		if (input == null) return null;
		input = StringUtils.trimToNull(input.replaceAll("^\\D+|\\D+$", StringUtils.EMPTY));
		if (input == null) return null;
		input = input.replaceAll("\\D+", "-").replaceAll("^-|-$", "");
		if (input.matches("^0*(?:(?:(?:[13578]|1[02])\\D*0*(?:[1-9]|[12][0-9]|3[01]))|(?:(?:[469]|11)\\D*0*(?:[1-9]|[12][0-9]|30))|(?:2\\D*0*(?:[1-9]|[12][0-9])))\\D*\\d+$")) {
			input = input.replaceAll("(\\d+)-(\\d+)-(\\d+)", "$3-$1-$2");
			try {
				return DateUtils.parseDate(input, PATTERN_DATE);
			} catch (ParseException e) {}
		}
		if (input.matches("^\\d+\\D*0*(?:(?:(?:[13578]|1[02])\\D*0*(?:[1-9]|[12][0-9]|3[01]))|(?:(?:[469]|11)\\D*0*(?:[1-9]|[12][0-9]|30))|(?:2\\D*0*(?:[1-9]|[12][0-9])))$")) {
			try {
				return DateUtils.parseDate(input, PATTERN_DATE);
			} catch (ParseException e) {}
		}
		return null;
	}

	public static final String protect(String string) {
		byte[] bytes = null;
		try {
			bytes = string.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			bytes = string.getBytes();
		}
		bytes = DigestUtils.sha512(bytes);
		bytes = DigestUtils.sha512(bytes);
		return Base64.encodeBase64URLSafeString(bytes);
	}

	private static Key $key = null;
	private static Cipher $cipher = null;

	public static final String token(long sequence) {
		if ($key == null) {
			try {
				KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
				gen.initialize(512);
				$key = gen.genKeyPair().getPublic();
				$cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			} catch (Throwable e) {}
		}
		//
		try {
			byte[] data = BigInteger.valueOf(sequence).toByteArray();
			$cipher.init(Cipher.ENCRYPT_MODE, $key);
			data = $cipher.doFinal(data);
			data = DigestUtils.sha512(data);
			// encrypt version 00
			data[0x8] &= 0xbf;
			data[0xc] &= 0xbf;
			return Base64.encodeBase64URLSafeString(data);
		} catch (Throwable e) {}
		//
		return token();
	}

	// (?i)^[0-9a-z_-]{86}$
	public static final String token() {
		byte[] data = new byte[64];
		ThreadLocalRandom.current().nextBytes(data);
		// random version 01
		data[0x8] |= 0x40;
		data[0xc] &= 0xbf;
		return Base64.encodeBase64URLSafeString(data);
	}

	// captcha question
	public static final char[] rand = "23456789qweryuipasdfghjkzxcvbnmQWERTYUPASDFGHJKLZXCVBNM".toCharArray();

	public static final char[] rand(int len) {
		char[] result = new char[len];
		for (int i = 0; i < len; i++) {
			result[i] = rand[ThreadLocalRandom.current().nextInt(rand.length)];
		}
		return result;
	}

	public static void copy(InputStream in, OutputStream out) {
		copy(in, out, null);
	}

	public static byte[] copy(InputStream in, OutputStream out, String algorithm) {
		MessageDigest d = null;
		if (algorithm != null) {
			try {
				d = MessageDigest.getInstance(algorithm);
			} catch (NoSuchAlgorithmException e) {}
		}
		//
		int l;
		byte[] b = new byte[8192];
		if (d == null) {
			try (InputStream i = in) {
				try (OutputStream o = out) {
					while ((l = i.read(b)) != -1) {
						o.write(b, 0, l);
					}
					o.flush();
				} catch (Exception e) {}
			} catch (Exception e) {}
		} else {
			try (InputStream i = in) {
				try (OutputStream o = out) {
					while ((l = i.read(b)) != -1) {
						o.write(b, 0, l);
						d.update(b, 0, l);
					}
					o.flush();
					return d.digest();
				} catch (Exception e) {}
			} catch (Exception e) {}
		}
		//
		return null;
	}

	public static final <T> T invoke(T object, boolean stop, String... methods) {
		if (object != null) {
			if (methods != null && methods.length > 0) {
				Class<? extends Object> clazz = object.getClass();
				for (String method : methods) {
					try {
						clazz.getMethod(method).invoke(object);
					} catch (Throwable e) {
						if (stop) {
							break;
						}
					}
				}
			}
		}
		return object;
	}

	public static final void invoke(String method, Object... objects) {
		if (method != null && method.length() > 0) {
			if (objects != null && objects.length > 0) {
				for (Object object : objects) {
					try {
						object.getClass().getMethod(method).invoke(objects);
					} catch (Throwable e) {}
				}
			}
		}
	}

	public static final void invoke(String[] methods, Object... objects) {
		if (methods != null && methods.length > 0) {
			for (String method : methods) {
				invoke(method, objects);
			}
		}
	}

	public static final void close(Object... objects) {
		invoke("close", objects);
	}

	private static final Appendable json(int type, Object object, Appendable out) throws SQLException, IOException {
		if (Types.BOOLEAN == type || Types.BIT == type || Types.INTEGER == type || Types.BIGINT == type) {
			out.append(String.valueOf(object));
		} else {
			String value = String.valueOf(object);
			value = value.replace("\n", "");
			value = value.replace("\r", "");
			value = value.replace("\"", "\\\"");
			out.append('"').append(value).append('"');
		}
		return out;
	}

	public static final Appendable json(ResultSet in, Appendable out) throws SQLException, IOException {
		out.append('[');
		if (in.next()) {
			ResultSetMetaData meta = in.getMetaData();
			int dimension = meta.getColumnCount();
			StringBuilder[] labels = new StringBuilder[dimension];
			//
			out.append('{');
			for (int i = 0, j = 1; i < dimension; i++, j++) {
				labels[i] = new StringBuilder().append('"').append(meta.getColumnLabel(j)).append('"').append(':');
				if (in.getObject(j) == null) continue;
				if (i > 0) out.append(',');
				json(meta.getColumnType(j), in.getObject(j), out.append(labels[i]));
			}
			out.append('}');
			while (in.next()) {
				out.append(',').append('{');
				for (int i = 0, j = 1; i < dimension; i++, j++) {
					if (in.getObject(j) == null) continue;
					if (i > 0) out.append(',');
					json(meta.getColumnType(j), in.getObject(j), out.append(labels[i]));
				}
				out.append('}');
			}
		}
		return out.append(']');
	}

	public static final String default$(Object... objects) {
		for (Object object : objects) {
			if (object != null) {
				String string = StringUtils.trimToNull(String.valueOf(object));
				if (string != null) return string;
			}
		}
		return null;
	}

	public static final int val(HttpServletRequest request, String name, int def) {
		HttpSession session = request.getSession();
		String key = request.getRequestURI() + "#" + name;
		int val = NumberUtils.toInt(default$(request.getParameter(name), request.getAttribute(name), session.getAttribute(key)), def);
		session.setAttribute(key, val);
		return val;
	}

	public static final int force(HttpServletRequest request, String name, int val) {
		HttpSession session = request.getSession();
		String key = request.getRequestURI() + "#" + name;
		request.setAttribute(name, val);
		session.setAttribute(key, val);
		return val;
	}

	public static final String val(HttpServletRequest request, String name, String def) {
		HttpSession session = request.getSession();
		String key = request.getRequestURI() + "#" + name;
		String val = default$(request.getParameter(name), request.getAttribute(name), session.getAttribute(key), def);
		session.setAttribute(key, val);
		return val;
	}

	public static final Appendable redirect(String url, Appendable out) {
		try {
			out.append("<!DOCTYPE html><html>");
			out.append("<head>");
			out.append("<meta http-equiv=\"refresh\" content=\"0; url=").append(url).append("\">");
			out.append("<script>window.location='").append(url).append("'</script>");
			out.append("</head>");
			out.append("<body>");
			out.append("<a href=\"").append(url).append("\">Continue...</a>");
			out.append("</body>");
			out.append("</html>");
		} catch (IOException e) {}
		return out;
	}

	public static final String obfuscate(HttpSession session, Map<String, Object> data) {
		String token = "obfuscate:" + token();
		session.setAttribute(token, data);
		return token;
	}

	@SuppressWarnings("unchecked")
	public static final Map<String, Object> $obfuscate(HttpSession session, String token) {
		if (token == null) return null;
		Map<String, Object> data = (Map<String, Object>) session.getAttribute(token);
		session.removeAttribute(token);
		return data;
	}

	public static final Map<String, Object> map(Object... objects) {
		Map<String, Object> map = new LinkedHashMap<>(objects.length >>> 1);
		for (int i = 0; i < objects.length; i++) {
			map.put((String) objects[i], objects[++i]);
		}
		return map;
	}

	public static final Object[] arr(Map<String, Object> map) {
		Object[] objects = new Object[map.size() << 1];
		int cursor = 0;
		for (Entry<String, Object> entry : map.entrySet()) {
			objects[cursor++] = entry.getKey();
			objects[cursor++] = entry.getValue();
		}
		return objects;
	}

	public static char[] rot13(char[] s) {
		for (int i = 0; i < s.length; i++) {
			char c = s[i];
			if (c >= 'a' && c <= 'm') s[i] += 13;
			else if (c >= 'A' && c <= 'M') s[i] += 13;
			else if (c >= '0' && c <= '4') s[i] += 5;
			else if (c >= 'n' && c <= 'z') s[i] -= 13;
			else if (c >= 'N' && c <= 'Z') s[i] -= 13;
			else if (c >= '5' && c <= '9') s[i] -= 5;
		}
		return s;
	}

	public static CharSequence rot13(CharSequence s) {
		StringBuilder b = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 'a' && c <= 'm') c += 13;
			else if (c >= 'A' && c <= 'M') c += 13;
			else if (c >= '0' && c <= '4') c += 5;
			else if (c >= 'n' && c <= 'z') c -= 13;
			else if (c >= 'N' && c <= 'Z') c -= 13;
			else if (c >= '5' && c <= '9') c -= 5;
			b.append(c);
		}
		return b;
	}

	// input : format in hex
	public static final String obfuscate(String input) {
		try {
			return rot13(Base64.encodeBase64URLSafeString(Hex.decodeHex(input.toCharArray()))).toString();
		} catch (DecoderException e) {}
		return input;
	}

	// input : obfuscated string
	public static final String $obfuscate(String input) {
		return Hex.encodeHexString(Base64.decodeBase64(rot13(input).toString()));
	}

	public static final Cookie cookie(String licence, int age) {
		String value = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			out.write(Hex.decodeHex(licence.toCharArray()));
			out.write(BigInteger.valueOf(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(age)).toByteArray());
			value = Base64.encodeBase64URLSafeString(CipherUtil.encrypt(out.toByteArray()));
		} catch (Exception e) {}
		if (value == null) return null;
		//
		Cookie cookie = new Cookie("UID", value);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		// cookie.setSecure(true);
		// cookie.setDomain(".wowkool.com");
		cookie.setMaxAge(age);
		return cookie;
	}

	public static final String cookie(String string) {
		try {
			byte[] bytes = CipherUtil.decrypt(Base64.decodeBase64(string));
			if (bytes.length > 16) {
				byte[] data = new byte[bytes.length - 16];
				System.arraycopy(bytes, 16, data, 0, data.length);
				if (new BigInteger(data).longValue() > System.currentTimeMillis()) {
					data = new byte[16];
					System.arraycopy(bytes, 0, data, 0, 16);
					return Hex.encodeHexString(data);
				}
			}
		} catch (Exception e) {}
		return null;
	}

	private static final byte[] uuidBytes() {
		byte[] bytes = new byte[16];
		ThreadLocalRandom.current().nextBytes(bytes);
		bytes[6] &= 0x0f; /* clear version */
		bytes[6] |= 0x40; /* set to version 4 */
		bytes[8] &= 0x3f; /* clear variant */
		bytes[8] |= 0x80; /* set to IETF variant */
		return bytes;
	}

	public static final String uuid() {
		return Hex.encodeHexString(uuidBytes());
	}

	public static final String base64uuid() {
		return Base64.encodeBase64URLSafeString(uuidBytes());
	}

	// (?i)^[0-9a-z-_]{22}$
	public static final String base64uuid(String uuid) {
		try {
			return Base64.encodeBase64URLSafeString(Hex.decodeHex(uuid.toCharArray()));
		} catch (DecoderException e) {}
		return null; // GIGO(garbage in garbage out)
	}

	public static final String $base64uuid(String base64uuid) {
		return Hex.encodeHexString(Base64.decodeBase64(base64uuid));
	}

	private static final String $encodeUrl(String url, String enc) {
		if (url == null) return null;
		try {
			return URLEncoder.encode(url, enc == null ? "UTF-8" : enc);
		} catch (Throwable e) {}
		return url;
	}

	private static final String $decodeUrl(String url, String enc) {
		if (url == null) return null;
		try {
			// UTF16
			if (Pattern.compile("(?i)\\Q%u\\E[0-9A-F]{4}").matcher(url).find()) {
				url = url.replaceAll("(?i)\\Q%u\\E([0-9A-F]{2})([0-9A-F]{2})", "%$1%$2");
				return URLDecoder.decode(url, "UTF-16");
			}
			//
			return URLDecoder.decode(url, enc == null ? "UTF-8" : enc);
		} catch (Throwable e) {}
		return url;
	}

	public static final String encodeUrl(String url, String enc) {
		if (url == null) return null;
		return $encodeUrl(decodeUrl(url, enc), enc);
	}

	public static final String decodeUrl(String url, String enc) {
		if (url == null) return null;
		String dec = $decodeUrl(url, enc);
		while (!url.equals(dec)) {
			dec = $decodeUrl(url = dec, enc);
		}
		return url;
	}

	public static final StringBuilder bind(StringBuilder options, String value) {
		if (options != null && value != null) {
			String $ = Pattern.compile("(?<!^)<option value=\"" + Pattern.quote(value) + "\"").matcher(options).replaceAll("$0 selected=\"selected\"");
			if ($.length() > options.length()) options.replace(0, options.length(), $);
		}
		return options;
	}

	public static final char c(CharSequence string, int offset, int length) {
		int s = length;
		for (int i = offset; i < length; i++)
			s += string.charAt(i) ^ 0xff;
		if ((s %= 13) > 9) s = s - 10;
		return (char) (s + 48);
	}

	public static final char c(CharSequence string) {
		return c(string, 0, string.length());
	}

	public static final boolean check(CharSequence string) {
		if (StringUtils.isBlank(string)) return false;
		int last = string.length() - 1;
		return string.charAt(last) == c(string, 0, last);
	}

	@Deprecated
	public static final String title(HttpServletRequest request, String title) {
		request.setAttribute("page_title", title);
		try {
			if (request.getAttribute("solr.logs") != null) {
				SolrInputDocument logs = (SolrInputDocument) request.getAttribute("solr.logs");
				logs.setField("page_title", title);
			}
		} catch (Throwable e) {}
		return title;
	}

	public static final CharSequence displayId(char[] prefix, int sequence, int length) {
		StringBuilder s = new StringBuilder(prefix.length + length + 2);
		s.append(prefix).append(reverse(String.format("%0" + length + "d", sequence).toCharArray()));
		return s.append(ThreadLocalRandom.current().nextInt(10)).append(c(s));
	}

	public static final CharSequence displayId(CharSequence prefix, int sequence, int length) {
		StringBuilder s = new StringBuilder(prefix.length() + length + 2).append(prefix);
		s.append(reverse(String.format("%0" + length + "d", sequence).toCharArray()));
		return s.append(ThreadLocalRandom.current().nextInt(10)).append(c(s));
	}

	public static final CharSequence displayId(char prefix, int sequence, int length) {
		StringBuilder s = new StringBuilder(length + 3).append(prefix);
		s.append(reverse(String.format("%0" + length + "d", sequence).toCharArray()));
		return s.append(ThreadLocalRandom.current().nextInt(10)).append(c(s));
	}

	public static char[] reverse(char[] cs) {
		for (int n = cs.length >>> 1, i = 0, j = cs.length - 1; i < n; i++, j--) {
			if (cs[i] == cs[j]) continue;
			char c = cs[i];
			cs[i] = cs[j];
			cs[j] = c;
		}
		return cs;
	}

	public static final CharSequence supitemId(Connection conn) {
		char c = (char) ('A' + ThreadLocalRandom.current().nextInt(26));
		return displayId(c, DBUtils.seq(conn, "sup." + c), 9);
	}

	public static final CharSequence itemId(Connection conn) {
		char c = (char) ('A' + ThreadLocalRandom.current().nextInt(26));
		return displayId(c, DBUtils.seq(conn, "item." + c), 9);
	}

	public static final CharSequence cateId(Connection conn) {
		char c = (char) ('A' + ThreadLocalRandom.current().nextInt(26));
		return displayId(c, DBUtils.seq(conn, "cate." + c), 7);
	}

	public static final CharSequence inqId(Connection conn) {
		char c = (char) ('A' + ThreadLocalRandom.current().nextInt(26));
		return displayId(c, DBUtils.seq(conn, "inq." + c), 7);
	}

	public static final CharSequence orderId(Connection conn) {
		String prefix = String.format("%1$ty%1$tm", System.currentTimeMillis());
		return displayId(prefix, DBUtils.seq(conn, prefix), 6);
	}

	public static final boolean bool(String value) {
		return (value = StringUtils.trimToNull(value)) == null ? false : value.matches("(?i)^t(?:rue)?|y(?:es)?|o[kn]?$");
	}

	public static final CharSequence replace(CharSequence input, String regex, String replacement) {
		Matcher matcher = Pattern.compile(regex).matcher(input);
		if (matcher.find()) {
			StringBuffer sb = new StringBuffer();
			matcher.appendReplacement(sb, replacement);
			while (matcher.find()) {
				matcher.appendReplacement(sb, replacement);
			}
			return matcher.appendTail(sb);
		}
		return input;
	}

	public static final String phoneMask(String phone) {
		if (StringUtils.isBlank(phone)) return phone;
		return phone.replaceFirst("\\d{3}(\\d{" + ((phone.charAt(phone.length() - 1) % 3) + 1) + "})$", "***$1");
	}

	public static final int sum(int[] arr) {
		int s = 0;
		for (int e : arr)
			s += e;
		return s;
	}

	public static Date add(Date date, int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, days);
		switch (calendar.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SATURDAY:
			calendar.add(Calendar.DATE, 2);
			break;
		case Calendar.SUNDAY:
			calendar.add(Calendar.DATE, 1);
			break;
		}
		return calendar.getTime();
	}

	public static final void forName(String className) {
		if (className == null) return;
		try {
			Class.forName(className);
		} catch (ClassNotFoundException e) {}
	}

	public static Properties load(String path) {
		return load(new File(path));
	}

	public static final Properties load(File file) {
		Properties prop = new Properties();
		try (InputStream in = new FileInputStream(file)) {
			prop.load(in);
			return prop;
		} catch (Exception e) {}
		return null;
	}

	// @formatter:off
	private static SSLContext sslcontext = null;
	public static final CloseableHttpClient httpClient() {
		if (sslcontext == null) {
			try {
				sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
					@Override
					public boolean isTrusted(X509Certificate[] certificate, String param) throws CertificateException {
						return true;
					}
				}).build();
			} catch (Exception e) {}
		}
		return HttpClients.custom().setSslcontext(sslcontext).build();
	}
	// @formatter:on

	// @formatter:off
	private static Random[] $rand = {
		new SecureRandom(),
		new SecureRandom(),
		new SecureRandom(),
		new SecureRandom()
	};
	private static char[] $digis = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
		'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
		'u', 'v', 'w', 'x', 'y', 'z',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
		'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
		'U', 'V', 'W', 'X', 'Y', 'Z',
		'.', '-', '_'
	};
	// @formatter:on

	public static String rand() {
		Random r = $rand[ThreadLocalRandom.current().nextInt($rand.length)];
		char[] c = new char[32];
		for (int i = 0; i < c.length; i++) {
			c[i] = $digis[r.nextInt($digis.length)];
		}
		return new String(c);
	}

	public static char[] fast(int len) {
		char[] s = new char[len];
		for (int i = 0, j; i < len; i++) {
			s[i] += (j = ThreadLocalRandom.current().nextInt(62)) + (j < 10 ? 48 : j < 36 ? 55 : 61);
		}
		return s;
	}

	public static boolean rand(String rand) {
		if (rand == null || rand.length() != 32) return false;
		for (int i = 0, c; i < 32; i++) {
			c = rand.charAt(i);
			if ('a' <= c && c <= 'z') continue;
			if ('A' <= c && c <= 'Z') continue;
			if ('0' <= c && c <= '9') continue;
			if (c == '.' || c == '-' || c == '_') continue;
			return false;
		}
		return true;
	}

	public static byte[] shuffle(byte[] data) {
		for (int i = 0, j, k = data.length >>> 1; i < k; i++) {
			j = ThreadLocalRandom.current().nextInt(data.length);
			if (i == j) continue;
			data[i] ^= data[j];
			data[j] ^= data[i];
			data[i] ^= data[j];
		}
		return data;
	}

	public static short[] shuffle(short[] data) {
		for (int i = 0, j, k = data.length >>> 1; i < k; i++) {
			j = ThreadLocalRandom.current().nextInt(data.length);
			if (i == j) continue;
			data[i] ^= data[j];
			data[j] ^= data[i];
			data[i] ^= data[j];
		}
		return data;
	}

	public static char[] shuffle(char[] data) {
		for (int i = 0, j, k = data.length >>> 1; i < k; i++) {
			j = ThreadLocalRandom.current().nextInt(data.length);
			if (i == j) continue;
			data[i] ^= data[j];
			data[j] ^= data[i];
			data[i] ^= data[j];
		}
		return data;
	}

	public static int[] shuffle(int[] data) {
		for (int i = 0, j, k = data.length >>> 1; i < k; i++) {
			j = ThreadLocalRandom.current().nextInt(data.length);
			if (i == j) continue;
			data[i] ^= data[j];
			data[j] ^= data[i];
			data[i] ^= data[j];
		}
		return data;
	}

	public static long[] shuffle(long[] data) {
		for (int i = 0, j, k = data.length >>> 1; i < k; i++) {
			j = ThreadLocalRandom.current().nextInt(data.length);
			if (i == j) continue;
			data[i] ^= data[j];
			data[j] ^= data[i];
			data[i] ^= data[j];
		}
		return data;
	}

	public static boolean any(int[] data, int... require) {
		for (int r : require) {
			for (int d : data) {
				if (r == d) return true;
			}
		}
		return false;
	}

	public static boolean all(int[] data, int... require) {
		boolean has;
		for (int r : require) {
			has = false;
			for (int d : data) {
				if (r == d) {
					has = true;
					break;
				}
			}
			if (!has) return false;
		}
		return true;
	}

	private static ArrayList<JsonObject> EMPTY_JSON_ARRAY = new ArrayList<>(0);

	public static ArrayList<JsonObject> convert(JsonArray array) {
		if (array == null) return EMPTY_JSON_ARRAY;
		ArrayList<JsonObject> result = new ArrayList<>(array.size());
		array.forEach(element -> result.add(element.getAsJsonObject()));
		return result;
	}

	public static final String str(Object obj) {
		return str(obj, StringUtils.EMPTY);
	}

	public static final String str(Object obj, String def) {
		return obj == null ? def : String.valueOf(obj);
	}

	public static final String maskPhone(String input) {
		return input.replaceFirst("^(.+?)?.{4}(.{3})$", "$1****$2");
	}

	public static final String maskName(String input) {
		return input.replaceFirst("^(.).+$", "$1**");
	}

	public static final String maskAddr(String input) {
		input = input.replaceFirst("^(.+[縣市].+[鄉鎮市區](?:.+?[路街里])?).*$", "$1*****");
		if (input.indexOf('*') == -1) {
			char[] cs = input.toCharArray();
			for (int l = cs.length, i = (l >>> 2), j = 1 + i + (l >>> 1); i < j && i < l; i++) {
				cs[i] = '*';
			}
			return new String(cs);
		}
		return input;
	}

	public static final void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {}
	}
}