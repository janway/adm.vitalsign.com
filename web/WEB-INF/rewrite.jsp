<%@page import="com.biosensetek.WebUtils"%>
<%@page import="java.net.HttpURLConnection,java.net.URL"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.util.Enumeration,java.util.List"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.concurrent.TimeUnit"%>
<%@page import="java.util.regex.Matcher,java.util.regex.Pattern"%>
<%@page import="org.apache.commons.io.IOUtils"%>
<%@page import="org.apache.commons.lang3.RandomStringUtils,org.apache.commons.lang3.StringUtils"%>
<%@page pageEncoding="UTF-8"%><%!
private static final String AGENT = new StringBuilder("rewrite").append(RandomStringUtils.randomAlphanumeric(16)).toString();
private static final boolean rewrite(HttpServletRequest request, HttpServletResponse response, String regex, String replacement) {
	StringBuffer requestURL = request.getRequestURL();
	Matcher matcher = Pattern.compile(regex).matcher(requestURL);
	if (matcher.matches()) {
		StringBuilder rewrite = new StringBuilder(matcher.replaceFirst(replacement).replaceFirst("(?i)^https?://.+?(?::\\d+)?(?=/)", "http://127.0.0.1:8080"));// here force to access 127.0.0.1 only
		//System.out.println(requestURL);
		//System.out.println(rewrite);
		String qry = StringUtils.trimToNull(request.getQueryString());
		if (qry != null) rewrite.append(rewrite.lastIndexOf("?") > -1 ? '&' : '?').append(qry);
		try {
			int len = -1; byte[] buf = new byte[4096]; boolean doInput = (len = request.getInputStream().read(buf)) != -1;
			HttpURLConnection conn = (HttpURLConnection) new URL(rewrite.toString()).openConnection();
			conn.setReadTimeout((int) TimeUnit.SECONDS.toMillis(10));
			conn.setInstanceFollowRedirects(false);
			conn.setUseCaches(false);
			conn.setDoOutput(true);
			if (doInput) conn.setDoInput(true);
			conn.setRequestMethod(request.getMethod());
			Enumeration<String> names = request.getHeaderNames();
			if (names.hasMoreElements()) {
				String name;
				do {
					name = names.nextElement();
					if (StringUtils.equalsIgnoreCase("connection", name)) continue;
					if (StringUtils.equalsIgnoreCase("accept-encoding", name)) continue;
					if (StringUtils.equalsIgnoreCase("user-agent", name)) {
						conn.setRequestProperty("X-User-Agent", request.getHeader(name));
						continue;
					}
					conn.setRequestProperty(name, request.getHeader(name));
				} while (names.hasMoreElements());
			}
			conn.setRequestProperty("X-Forwarded-For", WebUtils.remote(request));
			//conn.setRequestProperty("X-Request-Url", requestURL.toString());
			conn.setRequestProperty("User-Agent", AGENT);
			conn.setRequestProperty("Connection", "close");
			//
			conn.connect();
			if (doInput) {
				conn.getOutputStream().write(buf, 0, len);
				IOUtils.copy(request.getInputStream(), conn.getOutputStream());
			}
			if (conn.getResponseCode() == 302) {
				String location = conn.getHeaderField("location");
				if (location.matches("(?i)^http://[^/]+(.*)$")) {
					location = location.replaceFirst("(?i)^http://[^/]+(.*)$", "$1");
				}
				request.setAttribute("redirect", location);
				return true;
			}
			HttpServletResponse $response = (HttpServletResponse) request.getAttribute("rewrite.res");
			String contentType = conn.getContentType();
			for (Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
				String key = entry.getKey();
				if (key == null) continue;
				if (StringUtils.equalsIgnoreCase("connection", key)) continue;
				if (StringUtils.equalsIgnoreCase("content-type", key)) continue;
				if (StringUtils.equalsIgnoreCase("transfer-encoding", key)) continue;
				for (String val : entry.getValue()) {
					$response.setHeader(key, val);
				}
			}
			$response.setContentType(StringUtils.defaultIfBlank(contentType, "text/html;charset=UTF-8"));
			$response = null;
			//
			IOUtils.copy(conn.getInputStream(), response.getOutputStream());
			conn.disconnect();
			request.setAttribute("rewrite", rewrite);
			return true;
		//} catch (Throwable e) { System.err.println(e.getMessage()); }
		} catch (Throwable e) {}
	}
	return false;
}

private static final boolean redirect(HttpServletRequest request, HttpServletResponse response, String regex, String replacement) {
	String sp = request.getRequestURL().toString();
	if (sp.matches(regex)) {
		String redirect = sp.replaceFirst(regex, replacement);
		if (request.getContextPath().length() > 1) redirect = request.getContextPath() + redirect;
		request.setAttribute("redirect", redirect);
		return true;
	}
	return false;
}
%><%
if (AGENT.equals(request.getHeader("user-agent"))) { request.setAttribute("rewrite.agent", AGENT); return; }// apply once
//
request.setCharacterEncoding("UTF-8");
response.setCharacterEncoding("UTF-8");
// resource
//if (rewrite(request, response, "^(.+?)/cdn/(?i)([0-9a-z-_]{22})(?:/.*)?$", "$1/res.jsp?d=$2")) return;
// item page
//if (rewrite(request, response, "^(.+?)/item/(.{12})(/.*)?/inquiry$", "$1/item-inquiry.html?n=$2&q=$3")) return;
//if (rewrite(request, response, "^(.+?)/item/(.{12})(/.*)?$", "$1/item.html?n=$2&q=$3")) return;
// shop page
//if (rewrite(request, response, "^(.+?)(?<!adm)/shop/(?i)([0-9a-z_-]{2,50})(/.*)?/(info|inquiry|note)$", "$1/shop-$4.html?n=$2&q=$3")) return;
//if (rewrite(request, response, "^(.+?)(?<!adm)/shop/(?i)([0-9a-z_-]{2,50})(?:/?)(.*)?$", "$1/shop.html?n=$2&q=$3")) return;
// search page
//if (rewrite(request, response, "^(.+?)(?<!adm)/item/?(.*)$", "$1/search.html?q=$2")) return;
//if (rewrite(request, response, "^(.+?)(?<!adm)/shop/?(.*)$", "$1/search.html?z=s&q=$2")) return;
%>