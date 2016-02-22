<%@page import="java.util.Collection"%>
<%@page import="org.apache.commons.lang3.math.NumberUtils"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" session="false"%><%
String l = StringUtils.defaultIfBlank((String) request.getAttribute("param"), "s");
int s = request.getAttribute("start") == null ? NumberUtils.toInt(request.getParameter(l), 0) : (int) request.getAttribute("start");
int n = request.getAttribute("found") == null ? 0                                             : (int) request.getAttribute("found");
int r = request.getAttribute("rows")  == null ? 50                                            : (int) request.getAttribute("rows");
int p = request.getAttribute("page")  == null ? 10                                            : (int) request.getAttribute("page");
//
if (s >= n) s = n - r;
if (s < 0) s = 0;
s = (s / r) * r;
request.setAttribute("start", s);
if (request.getAttribute("init") != null) {
	request.removeAttribute("init");
	return;
}
if (n == 0) return;
//
StringBuilder q = new StringBuilder();
if (request.getAttribute("keep.more") != null) {
	// keep.more should append & in front of string  
	q.append(request.getAttribute("keep.more"));
}
if (request.getAttribute("keep") != null) {
	Collection<String> k = (Collection<String>) request.getAttribute("keep");
	request.getParameterMap().keySet().stream().filter(key -> k.contains(key))
	.forEach(key -> {
		for (String val : request.getParameterValues(key)) {
			if (StringUtils.isBlank(val)) continue;
			q.append('&').append(key).append('=').append(val);
		}
	});
}
q.append('&').append(l).append('=');
q.setCharAt(0, '?');
//
if (request.getAttribute("out") != null) {
	out = (JspWriter) request.getAttribute("out");
}
//
int rp = r * p;
int sp = s / r, sw = sp / p;
int np = n / r, nw = np / p;
%><ul class=pagination><%
if (sw == 0) {
	%><li class=disabled><span>&laquo;</span></li><%
} else {
	%><li><a href="<%=q%><%=(sw - 1) * rp%>">&laquo;</a></li><%
}
for (int i = sw * p, j = Math.min(i + p, np + (n % r > 0 ? 1 : 0)); i < j; i++) {
	if (i == sp) {
		%><li class=active><span><%=i + 1%></span></li><%
	} else {
		%><li><a href="<%=q%><%=i * r%>"><%=i + 1%></a></li><%
	}
}
if (sw == nw || (sw == nw - 1 && n % rp == 0)) {
	%><li class=disabled><span>&raquo;</span></li><%
} else {
	%><li><a href="<%=q%><%=(sw + 1) * rp%>">&raquo;</a></li><%
}%></ul>