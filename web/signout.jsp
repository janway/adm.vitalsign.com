<%@page import="com.biosensetek.DBUtils,com.biosensetek.Utils"%>
<%@page import="java.sql.Connection,java.sql.PreparedStatement"%>
<%@page import="java.util.Collections"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%
String licence = (String) session.getAttribute("licence");
//
HttpSession $session = session;
Collections.list(session.getAttributeNames()).forEach(attr -> {
	if (StringUtils.startsWithIgnoreCase(attr, "licence")) {
		$session.removeAttribute(attr);
	}
});
//
if (licence != null) {
	Cookie UID = Utils.cookie(licence, 0);
	if (UID != null) {
		UID.setDomain(request.getRemoteHost());
		response.addCookie(UID);
	}
	/*
	try (Connection conn = DBUtils.conn("main")) {
		try(PreparedStatement stat = conn.prepareStatement("INSERT INTO usrr(type,usr,crUsr,crIP)VALUES(1,?,?,?)")) {
			stat.setString(1, licence);
			stat.setString(2, licence);
			stat.setString(3, (String) request.getAttribute("remote"));
			stat.executeUpdate();
		}
	} catch (Exception e) { e.printStackTrace(); }*/
}
//
response.sendRedirect("signin.html");
%>