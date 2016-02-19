<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.google.gson.JsonArray"%>
<%@page import="java.sql.Statement"%>
<%@page import="com.google.gson.JsonObject"%>
<%@page import="com.google.gson.SerializedJsonObject"%>
<%@page import="java.sql.Connection,java.sql.PreparedStatement,java.sql.ResultSet"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%><%
String token = (String) request.getAttribute("auth-utils.jsp");
request.removeAttribute("auth-utils.jsp");
if (token == null) return;
//
Connection conn = (Connection) request.getAttribute(token + "conn");
request.removeAttribute(token + "conn");
//
String licence = (String) request.getAttribute(token + "licence");
request.removeAttribute(token + "licence");
//
if (conn == null || licence == null) return;
try (Statement stat = conn.createStatement()) {
	StringBuilder sql = new StringBuilder()
	.append("SELECT auth,ref")
	.append(	  ",shop.name,shop.display")
	.append( " FROM usrauth")
	.append( " JOIN shop ON shop.uid=usrauth.ref")
	.append(          " AND(auth=0 OR auth=2)")
	.append(          " AND shop.status=1")
	.append(          " AND usr='").append(licence).append("'")
	.append(" UNION ")
	.append("SELECT auth,ref")
	.append(	  ",sup.name,sup.name")
	.append( " FROM usrauth")
	.append( " JOIN sup ON sup.uid=usrauth.ref")
	.append(         " AND(auth=1 OR auth=3)")
	.append(         " AND sup.status=1")
	.append(         " AND usr='").append(licence).append("'");
	try (ResultSet rs = stat.executeQuery(sql.toString())) {
		SerializedJsonObject a = null;
		if (rs.next()) {
			Map<Integer,ArrayList<SerializedJsonObject>> adm = new HashMap<>();
			session.setAttribute("licence.adm", adm);
			do {
				if (adm.get(rs.getInt("auth")) == null) {
					adm.put(rs.getInt("auth"), new ArrayList<SerializedJsonObject>());
				}
				adm.get(rs.getInt("auth")).add(a = new SerializedJsonObject());
				a.addProperty("id", rs.getString("ref"));
				if (0 == rs.getInt("auth") || 2 == rs.getInt("auth")) {
					a.addProperty("name", rs.getString("name"));
					a.addProperty("display", rs.getString("display"));
				} else if (1 == rs.getInt("auth") || 3 == rs.getInt("auth")) {
					a.addProperty("name", rs.getString("name"));
					a.addProperty("display", rs.getString("display"));
				}
				//
				if (session.getAttribute("licence.adm.current.id") == null) {
					session.setAttribute("licence.adm.current.auth", rs.getInt("auth"));
					session.setAttribute("licence.adm.current.id", a.get("id").getAsString());
					session.setAttribute("licence.adm.current.name", a.get("name").getAsString());
					session.setAttribute("licence.adm.current.display", a.get("display").getAsString());
				}
			} while (rs.next());
			session.setAttribute("licence.adm.current", 0);
			session.setAttribute("licence.adm.members", adm.get((int) session.getAttribute("licence.adm.current.auth")));
		}
	}
}
// record this sign in
String remote = (String) request.getAttribute("remote");
try (PreparedStatement stat = conn.prepareStatement("INSERT INTO usrr(type,usr,crUsr,crIP)VALUES(0,?,?,?)")) {
	stat.setString(1, licence);
	stat.setString(2, licence);
	stat.setString(3, remote);
	stat.executeUpdate();
} catch (Exception e) { e.printStackTrace(); }
%>