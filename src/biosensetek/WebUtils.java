package biosensetek;

import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WebUtils {
	public static final String encode(String string) {
		string = Utils.encodeUrl(string, "UTF-8");
		string = string.replace("%2F", "/");
		return string;
	}

	public static final String remote(HttpServletRequest request) {
		Enumeration<String> names = request.getHeaderNames();
		String name, value;
		while (names.hasMoreElements()) {
			name = names.nextElement();
			if ("X-Forwarded-For".equalsIgnoreCase(name) && (value = remote(request.getHeader(name))) != null) return value;
			if ("Proxy-Client-IP".equalsIgnoreCase(name) && (value = remote(request.getHeader(name))) != null) return value;
			if ("WL-Proxy-Client-IP".equalsIgnoreCase(name) && (value = remote(request.getHeader(name))) != null) return value;
			if ("HTTP_CLIENT_IP".equalsIgnoreCase(name) && (value = remote(request.getHeader(name))) != null) return value;
			if ("HTTP_X_FORWARDED_FOR".equalsIgnoreCase(name) && (value = remote(request.getHeader(name))) != null) return value;
		}
		return new StringBuilder(request.getRemoteAddr()).append(':').append(request.getRemotePort()).toString();
	}

	private static final String remote(String value) {
		if (StringUtils.isBlank(value)) return null;
		if ("unknown".equalsIgnoreCase(value)) return null;
		return value;
	}

	public static final String returnOf(HttpServletRequest request) {
		String referer = request.getHeader("referer");
		if (referer != null) {
			StringBuffer url = request.getRequestURL();
			int len = url.length() - request.getRequestURI().length() + 1;
			if (len <= referer.length()) {
				boolean startsWith = true;
				for (int i = 4; i < len; i++) {
					if (referer.charAt(i) != url.charAt(i)) {
						startsWith = false;
						break;
					}
				}
				if (startsWith) return referer.substring(len - 1);
			}
		}
		return Utils.str(request.getAttribute("prefix")) + "index.html";
	}

	public static final Document abs(Document doc) {
		for (Element e : doc.select("[href]")) {
			e.attr("href", e.attr("abs:href"));
		}
		return doc;
	}

	public static final CharSequence baseUri(HttpServletRequest request) {
		StringBuffer base = request.getRequestURL();
		base.setLength(base.length() - request.getRequestURI().length() + 1);
		return base;
	}

	public static final CharSequence uri(HttpServletRequest request) {
		StringBuilder uri = new StringBuilder(request.getRequestURL());
		uri.setLength(uri.length() - request.getRequestURI().length());
		if (StringUtils.isNoneBlank(request.getContextPath())) {
			uri.append(request.getContextPath());
		}
		return uri;
	}

	public static final CharSequence val(HttpServletRequest request, String name) {
		StringBuilder b = new StringBuilder();
		if (request.getAttribute(name) != null) b.append("value=\"").append(request.getAttribute(name)).append('"');
		return b;
	}

	public static final StringBuilder sel(HttpServletRequest request, String name, String val) {
		StringBuilder b = new StringBuilder(" value=\"").append(val).append('"');
		if (request.getAttribute(name) != null && StringUtils.equals(val, (String) request.getAttribute(name))) b.append(" selected=\"selected\"");
		return b;
	}

	public static final String SCRIPT = "script";
	public static final String STYLE = "style";

	public static final JspWriter script(HttpServletRequest request, JspWriter out) {
		return wrap(request, out, SCRIPT);
	}

	public static final JspWriter script(JspWriter out) {
		return script(out, true);
	}

	public static final JspWriter script(JspWriter out, boolean optimize) {
		if (!(out instanceof $JspWriter)) return out;
		$JspWriter x = ($JspWriter) out;
		if (optimize) {
			Matcher m = Pattern.compile("(?mis)<script[^>]*>(.*?)</script>").matcher(x.buffer);
			if (m.find()) {
				StringBuilder s = new StringBuilder(), b = new StringBuilder();
				String g;
				do {
					g = StringUtils.trimToNull(m.group(1));
					if (g == null) {
						if (b.length() > 0) {
							Minifier.js(b, s.append("<script>")).append("</script>");
							b.setLength(0);
						}
						s.append(m.group());
					} else {
						b.append(g);
					}
				} while (m.find());
				if (b.length() > 0) Minifier.js(b, s.append("<script>")).append("</script>");
				x.buffer.setLength(0);
				if (StringUtils.isNotBlank(s)) x.buffer.append(s);
			}
		}
		return x.original;
	}

	public static final JspWriter style(HttpServletRequest request, JspWriter out) {
		return wrap(request, out, STYLE);
	}

	public static final JspWriter style(JspWriter out) {
		return style(out, true);
	}

	public static final JspWriter style(JspWriter out, boolean optimize) {
		if (!(out instanceof $JspWriter)) return out;
		$JspWriter x = ($JspWriter) out;
		if (optimize) {
			Matcher m = Pattern.compile("(?mis)<style[^>]*>(.*?)</style>").matcher(x.buffer);
			if (m.find()) {
				StringBuilder s = new StringBuilder(), b = new StringBuilder();
				String g;
				do {
					g = StringUtils.trimToNull(m.group(1));
					if (g == null) {
						if (b.length() > 0) {
							Minifier.css(b, s.append("<style>")).append("</style>");
							b.setLength(0);
						}
						s.append(m.group());
					} else {
						b.append(g);
					}
				} while (m.find());
				if (b.length() > 0) Minifier.css(b, s.append("<style>")).append("</style>");
				x.buffer.setLength(0);
				if (StringUtils.isNotBlank(s)) x.buffer.append(s);
			}
		}
		return x.original;
	}

	public static final JspWriter wrap(HttpServletRequest request, JspWriter out, String name) {
		@SuppressWarnings("resource")
		$JspWriter agent = new $JspWriter(out.getBufferSize(), out.isAutoFlush()).original(out);
		if (request.getAttribute(name) == null) request.setAttribute(name, new StringBuilder());
		agent.buffer((StringBuilder) request.getAttribute(name));
		return agent;
	}

	public static final JspWriter wrap(JspWriter out) {
		if (!(out instanceof $JspWriter)) return out;
		$JspWriter x = ($JspWriter) out;
		return x.original;
	}

	private static class $JspWriter extends JspWriter {
		private JspWriter original;
		private StringBuilder buffer;

		public $JspWriter original(JspWriter original) {
			this.original = original;
			return this;
		}

		public $JspWriter buffer(StringBuilder buffer) {
			this.buffer = buffer;
			return this;
		}

		protected $JspWriter(int bufferSize, boolean autoFlush) {
			super(bufferSize, autoFlush);
		}

		@Override
		public void clear() throws IOException {}

		@Override
		public void clearBuffer() throws IOException {}

		@Override
		public int getRemaining() {
			return 0;
		}

		@Override
		public void newLine() throws IOException {
			buffer.append('\n');
		}

		@Override
		public void close() throws IOException {}

		@Override
		public void flush() throws IOException {}

		@Override
		public void print(boolean b) throws IOException {
			buffer.append(b);
		}

		@Override
		public void print(char c) throws IOException {
			// if (c < 32) return;
			buffer.append(c);
		}

		@Override
		public void print(int i) throws IOException {
			buffer.append(i);
		}

		@Override
		public void print(long l) throws IOException {
			buffer.append(l);
		}

		@Override
		public void print(float f) throws IOException {
			buffer.append(f);
		}

		@Override
		public void print(double d) throws IOException {
			buffer.append(d);
		}

		@Override
		public void print(char[] s) throws IOException {
			// char[] x = new char[s.length];
			// int len = 0;
			// for (int i = 0; i < x.length; i++) {
			// if (s[i] < 32) continue;
			// x[len++] = s[i];
			// }
			// buffer.append(x, 0, len);
			buffer.append(s);
		}

		@Override
		public void print(String s) throws IOException {
			print(s.toCharArray());
		}

		@Override
		public void print(Object o) throws IOException {
			buffer.append(o);
		}

		@Override
		public void println() throws IOException {
			buffer.append('\n');
		}

		@Override
		public void println(boolean b) throws IOException {
			buffer.append(b).append('\n');
		}

		@Override
		public void println(char c) throws IOException {
			buffer.append(c).append('\n');
		}

		@Override
		public void println(int i) throws IOException {
			buffer.append(i).append('\n');
		}

		@Override
		public void println(long l) throws IOException {
			buffer.append(l).append('\n');
		}

		@Override
		public void println(float f) throws IOException {
			buffer.append(f).append('\n');
		}

		@Override
		public void println(double d) throws IOException {
			buffer.append(d).append('\n');
		}

		@Override
		public void println(char[] s) throws IOException {
			buffer.append(s).append('\n');
		}

		@Override
		public void println(String s) throws IOException {
			buffer.append(s).append('\n');
		}

		@Override
		public void println(Object o) throws IOException {
			buffer.append(o).append('\n');
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			// char[] x = new char[len];
			// len += off;
			// int l = 0;
			// for (int i = off; i < len; i++) {
			// if (cbuf[i] < 32) continue;
			// x[l++] = cbuf[i];
			// }
			// buffer.append(x, 0, l);
			buffer.append(cbuf, off, len);
		}
	}

	public static final <T extends Appendable> T dumpRequest(HttpServletRequest request, T output) {
		try {
			output.append("addr").append('=').append(request.getRemoteAddr()).append(StringUtils.LF);
			output.append("method").append('=').append(request.getMethod()).append(StringUtils.LF);
			output.append("uri").append('=').append(request.getRequestURI()).append(StringUtils.LF);
			output.append("url").append('=').append(request.getRequestURL()).append(StringUtils.LF);
			Enumeration<String> name = null;
			output.append("header").append('=').append(StringUtils.LF);
			if ((name = request.getHeaderNames()).hasMoreElements()) {
				String k = name.nextElement();
				output.append(StringUtils.SPACE).append(k).append('=').append(request.getHeader(k)).append(StringUtils.LF);
				while (name.hasMoreElements()) {
					k = name.nextElement();
					output.append(StringUtils.SPACE).append(k).append('=').append(request.getHeader(k)).append(StringUtils.LF);
				}
			}
			output.append("params").append('=').append(StringUtils.LF);
			if ((name = request.getParameterNames()).hasMoreElements()) {
				String k = name.nextElement();
				output.append(StringUtils.SPACE).append(k).append('=').append(request.getParameter(k)).append(StringUtils.LF);
				while (name.hasMoreElements()) {
					k = name.nextElement();
					output.append(StringUtils.SPACE).append(k).append('=').append(request.getParameter(k)).append(StringUtils.LF);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output;
	}
}