<%@page import="com.google.gson.SerializedJsonObject"%>
<%@page import="java.util.Map"%>
<%@page import="com.biosensetek.SolrUtil,com.biosensetek.WebUtils"%>
<%@page import="com.google.gson.Gson,com.google.gson.GsonBuilder"%>
<%@page import="java.io.File"%>
<%@page import="java.util.ArrayList,java.util.Calendar,java.util.Enumeration"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="org.apache.commons.lang3.math.NumberUtils"%>
<%@page import="org.apache.commons.lang3.time.DateFormatUtils"%>
<%@page import="org.apache.solr.client.solrj.impl.HttpSolrServer"%>
<%@page import="org.apache.solr.common.SolrInputDocument"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%!
private static HttpSolrServer shop = null, item = null;
private static Gson gson = null;
private static File res = null;
private static String cdn = null;
private static String prefix = null;

public void jspDestroy() {
	if (shop != null) shop.shutdown();
	if (item != null) item.shutdown();
}
%><%
Calendar calendar = Calendar.getInstance();
//
request.setCharacterEncoding("UTF-8");
response.setCharacterEncoding("UTF-8");
//
// if (shop == null) application.setAttribute("solr.shop", CateHolder.shopSolr(shop = new HttpSolrServer("http://192.168.11.13:8983/solr/shop")));
// if (item == null) application.setAttribute("solr.item", CateHolder.itemSolr(item = new HttpSolrServer("http://192.168.11.13:8983/solr/item")));
if (gson == null) application.setAttribute("gson", gson = new GsonBuilder().disableHtmlEscaping().create());
if (res == null) application.setAttribute("res", res = new File("/home/eapollo/master"));
if (cdn == null) application.setAttribute("cdn", cdn = "cdn/");
if (prefix == null) { prefix = application.getContextPath() + "/"; if (prefix.length() == 0) prefix = "/"; }
//
request.setAttribute("remote", WebUtils.remote(request));
if (StringUtils.endsWithAny(request.getServletPath(), "html", "upload.jsp", "a.jsp", "ctbc-result.jsp")) {
	request.setAttribute("prefix", prefix);
	//request.setAttribute("return", WebUtils.returnOf(request));
}
//
String requestURI = request.getRequestURI();
//
/*
if (requestURI.matches("^/adm/?(?:.+?html)?$")) {
	String licence = (String) session.getAttribute("licence");
	if (licence == null) {
		session.setAttribute("signin.html/return", request.getRequestURI());
		request.setAttribute("redirect", prefix + "signin.html");
		return;
	}
	if (session.getAttribute("licence.adm") == null) {
		request.setAttribute("redirect", prefix + "index.html");
		return;
	}
	ArrayList<SerializedJsonObject> members = null;
	int auth = NumberUtils.toInt(request.getParameter("auth"), -1);
	int current = NumberUtils.toInt(request.getParameter("current"), -1);
	if (-1 < auth && (members = ((Map<Integer,ArrayList<SerializedJsonObject>>) session.getAttribute("licence.adm")).get(auth)) != null) {
		session.setAttribute("licence.adm.members", members);
		session.setAttribute("licence.adm.current.auth", auth);
		current = 0;
	}
	members = (ArrayList<SerializedJsonObject>) session.getAttribute("licence.adm.members");
	if (-1 < current && current < members.size()) {
		session.setAttribute("licence.adm.current", current);
		session.setAttribute("licence.adm.current.id", members.get(current).get("id").getAsString());
		session.setAttribute("licence.adm.current.name", members.get(current).get("name").getAsString());
		session.setAttribute("licence.adm.current.display", members.get(current).get("display").getAsString());
	}
}*/
//
/*
if (requestURI.matches("^/profile-?.*html$") && !requestURI.matches("^/profile-inquiry.*html$")) {
	String licence = (String) session.getAttribute("licence");
	if (licence == null) {
		session.setAttribute("signin.html/return", request.getRequestURI());
		if (StringUtils.startsWith(request.getHeader("accept"), "application/json")) {
			response.reset();
			response.setContentType("application/json;charset=UTF-8");
			out.append("{\"location\":\"" + prefix + "signin.html\"}");
			request.setAttribute("prevent", StringUtils.EMPTY);
		} else {
			//response.sendRedirect(prefix + "signin.html");
			request.setAttribute("redirect", prefix + "signin.html");
		}
		return;
	}
}*/
// skip log
// if (requestURI.matches("^/fun/.+\\.jsp$")) return;
//
// Cookie[] cookies = request.getCookies();
//
/*
SolrInputDocument doc = new SolrInputDocument();
doc.setField("id", SolrUtil.logId(calendar.getTimeInMillis()));
doc.setField("time", calendar.getTime());
doc.setField("time_s", DateFormatUtils.format(calendar, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
doc.setField("ts_l", calendar.getTimeInMillis());
doc.setField("year_i", calendar.get(Calendar.YEAR));
doc.setField("month_i", calendar.get(Calendar.MONTH) + 1);
doc.setField("date_i", calendar.get(Calendar.DATE));
doc.setField("hour_i", calendar.get(Calendar.HOUR));
doc.setField("minute_i", calendar.get(Calendar.MINUTE));
doc.setField("second_i", calendar.get(Calendar.SECOND));
doc.setField("millis_i", calendar.get(Calendar.MILLISECOND));
doc.setField("remote", request.getAttribute("remote"));
doc.setField("r_method", request.getMethod());
doc.setField("r_url", request.getRequestURL());
doc.setField("r_uri", requestURI);
doc.setField("r_qry", request.getQueryString());
doc.setField("r_addr", request.getRemoteAddr());
doc.setField("r_port", String.valueOf(request.getRemotePort()));
doc.setField("r_servlet_path", request.getServletPath());
if (session.getAttribute("licence") != null) doc.setField("licence_s", session.getAttribute("licence"));
Enumeration<String> names;
if ((names = request.getHeaderNames()).hasMoreElements()) {
	String name; Enumeration<String> val;
	do {
		if ((val = request.getHeaders(name = names.nextElement())).hasMoreElements()) {
			name = "h_" + name;
			do {
				doc.addField(name, val.nextElement());
			} while (val.hasMoreElements());
		}
	} while (names.hasMoreElements());
}
names = null;
if ((names = request.getParameterNames()).hasMoreElements()) {
	String name; String[] val;
	do {
		val = request.getParameterValues(name = names.nextElement());
		name = "p_" + name;
		for (String s : val) {
			doc.addField(name, s);
		}
	} while (names.hasMoreElements());
}
names = null;
if (cookies != null) {
	for (Cookie cookie : cookies) {
		doc.addField("c_" + cookie.getName(), cookie.getValue());
	}
}
request.setAttribute(SolrUtil.LABEL, doc);*/



%>