<%@page import="java.util.Calendar"%>
<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%><!DOCTYPE html><html><head><%
%><meta charset="utf-8"><meta http-equiv="X-UA-Compatible" content="IE=edge"><%
%><title><sitemesh:write property='title'/> 哇酷商城</title><%
%><link rel=stylesheet href=${cdn}css/bootstrap.css><%
%><link rel=stylesheet href=${cdn}css/common.css><%
%><link rel=stylesheet href=${cdn}css/sign.css><%
%><sitemesh:write property='head'/><%
%><%if (request.getAttribute("style") != null) out.print(request.getAttribute("style"));%></head><body><%
%><div class=container><%
%><div class="mar-top20 bbgd5 pb50"><sitemesh:write property='body'/></div><%
%><footer class="text-center mar-top20 mb50 mar-bottom100"><%
	%><img src=${cdn}img/logogroup.png title=漢唐集團商標><%
	%><p class=mar-top20>© 2010 - <%=Calendar.getInstance().get(Calendar.YEAR)%> 漢唐光電科技股份有限公司</p><%
%></footer><%
%></div><%
%><script src=${cdn}js/bootstrap.js></script><script src=${cdn}js/default.js></script><%
%><%if (request.getAttribute("script") != null) out.print(request.getAttribute("script"));%><%
%><!--[if lt IE 9]><script src="${cdn}js/ie-fix.js"></script><![endif]--><%
%></body></html>