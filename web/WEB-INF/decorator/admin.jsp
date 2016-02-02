<%@page import="java.util.Map,java.util.ArrayList"%>
<%@page import="org.apache.commons.lang3.math.NumberUtils"%>
<%@page import="com.eapollo.DBUtils,com.eapollo.Utils,com.eapollo.WebUtils"%>
<%@page import="com.google.gson.JsonObject,com.google.gson.SerializedJsonObject"%>
<%@page import="java.sql.Connection,java.sql.PreparedStatement,java.sql.ResultSet"%>
<%@page import="java.util.List,java.util.LinkedList,java.util.Calendar,java.util.LinkedHashMap"%>
<%@page import="org.apache.commons.lang3.RandomStringUtils,org.apache.commons.lang3.StringUtils"%>
<%@page import="org.sitemesh.content.Content,org.sitemesh.content.ContentProperty"%>
<%@page import="org.apache.commons.lang3.time.DateFormatUtils"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%!
private static final String INQUIRIES_SQL = new StringBuilder()
.append("SELECT uid,category,qt,cr,(SELECT`name`FROM usr WHERE usr.uid=a.usr)`name`")
.append( " FROM(")
.append(       "SELECT uid,1 category,qt,cr,crUsr usr")
.append(        " FROM shopInquiry")
.append(       " WHERE`status`IN(0,1)AND shop=?")
.append(       " UNION ")
.append(       "SELECT uid,2 category,qt,cr,crUsr usr")
.append(        " FROM itemInquiry")
.append(       " WHERE`status`IN(0,1)AND shop=?")
.append(       " UNION ")
.append(       "SELECT uid,3 category,qt,cr,crUsr usr")
.append(        " FROM orderInquiry")
.append(       " WHERE`status`IN(0,1)AND shop=?")
.append(      ")a")
.append(" ORDER BY cr DESC")
.append(" LIMIT 20")
.toString();
private static final String ORDER_STATUS_SQL = new StringBuilder()
.append("SELECT SUM(CASE WHEN`status`=0 AND acceptTime IS NULL THEN 1 ELSE 0 END)a")//acpt
.append(      ",SUM(CASE WHEN`status`=1 OR(`status`=0 AND acceptTime IS NOT NULL)THEN 1 ELSE 0 END)b")//shipping
.append(      ",SUM(CASE WHEN`status`=2 THEN 1 ELSE 0 END)c")//trace
.append(      ",SUM(CASE WHEN`status`=4 AND shippingBackTime IS NULL THEN 1 ELSE 0 END)d")//return
.append(      ",SUM(CASE WHEN`status`=3 AND((unarrivalReqTime IS NOT NULL AND unarrivalCloseTime IS NULL)OR unarrivalReqTime>unarrivalCloseTime)THEN 1 ELSE 0 END)e")//conflict
.append( " FROM`order`")
.append( "WHERE shop=?")
.toString();
%><%
String shopId = null;
int apct = 0, shipping = 0, trace = 0, Return = 0, conflict = 0;
if (0 == (int) session.getAttribute("licence.adm.current.auth")) {
	shopId = (String) session.getAttribute("licence.adm.current.id");
	try (Connection conn = DBUtils.conn("main")) {
		try (PreparedStatement stat = conn.prepareStatement(ORDER_STATUS_SQL)) {
			stat.setString(1, shopId);
			try (ResultSet rs = stat.executeQuery()) {
				if(rs.next()) {
					apct     = rs.getInt(1);
					shipping = rs.getInt(2);
					trace    = rs.getInt(3);
					Return   = rs.getInt(4);
					conflict = rs.getInt(5);
				}
			}
		}
	} catch (Throwable e) {}
}
%><!DOCTYPE html><html><head><meta http-equiv=X-UA-Compatible content="IE=edge,chrome=1"><meta charset=utf-8>
<meta name=viewport content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
<title><sitemesh:write property='title' /> 哇酷商城管理系統</title>
<link rel=stylesheet href=${cdn}css/bootstrap.css>
<link rel=stylesheet href=${cdn}font/fontawesome.css>
<link rel=stylesheet href="//fonts.googleapis.com/css?family=Open+Sans:400,300">
<link rel=stylesheet href=${cdn}css/ace.css class=ace-main-stylesheet id=main-ace-style>
<!--[if lte IE 9]><link rel="stylesheet" href="${cdn}css/ace-ie9.css" class="ace-main-stylesheet"/><![endif]-->
<link rel=stylesheet href=${cdn}css/skins.css>
<link rel=stylesheet href=${cdn}css/adm.css>
<sitemesh:write property='head'/><%if (request.getAttribute("style") != null) { %>${style}<% } %>
</head><body class=skin-2>
<div id=navbar class="navbar navbar-default">
	<div class=navbar-container id=navbar-container>
		<button class="navbar-toggle menu-toggler pull-left" id=menu-toggler data-target=#sidebar><span class=sr-only> 切換側選單</span><span class=icon-bar></span><span class=icon-bar></span><span class=icon-bar></span></button>
		<div class="navbar-header pull-left"><a href=${prefix} class=navbar-brand><small>哇酷商城</small></a></div>
		<div class="navbar-buttons navbar-header pull-right" role=navigation><ul class="nav ace-nav">
		<%
		int item_s = 0, shop_s = 0, order_s = 0;
		if (0 == (int)session.getAttribute("licence.adm.current.auth")) { %>
			<li class=purple>
				<a data-toggle=dropdown class=dropdown-toggle href=#><i class="ace-icon fa fa-bell icon-animated-bell"></i><span class="badge badge-important"><%=apct + shipping + trace + Return + conflict%></span></a>
				<ul class="dropdown-menu-right dropdown-navbar navbar-pink dropdown-menu dropdown-caret dropdown-close">
					<li class=dropdown-header><i class="ace-icon fa fa-exclamation-triangle"></i><%=apct+shipping+trace+Return+conflict%> 筆 訂單須處理的列表狀態。</li>
					<li class=dropdown-content><ul class="dropdown-menu dropdown-navbar navbar-pink">						
						<li><a href=order-acpt.html>
						<div class=clearfix><span class=pull-left>收單與核可列表</span><span class="pull-right badge badge-pink">＋<%=apct%></span></div>
						</a></li>
						<li><a href=order-shipping.html>
						<div class=clearfix><span class=pull-left>出貨處理列表</span><span class="pull-right badge badge-pink">＋<%=shipping%></span></div>
						</a></li>
						<li><a href=order-trace.html>
						<div class=clearfix><span class=pull-left>物流追蹤列表</span><span class="pull-right badge badge-pink">＋<%=trace%></span></div>
						</a></li>
						<li><a href=order-conflict.html>
						<div class=clearfix><span class=pull-left>到貨客訴列表</span><span class="pull-right badge badge-pink">＋<%=Return%></span></div>
						</a></li>
						<li><a href=order-return.html>
						<div class=clearfix><span class=pull-left>退貨處理列表</span><span class="pull-right badge badge-pink">＋<%=conflict%></span></div>
						</a></li>
					</ul></li>
				</ul>
			</li><%
			LinkedList<JsonObject> list = new LinkedList<>();
			if (session.getAttribute("licence.adm.members") != null) {
				int current = (int) session.getAttribute("licence.adm.current");
				List<SerializedJsonObject> members = (List<SerializedJsonObject>) session.getAttribute("licence.adm.members");
				if (current < members.size()) {
					SerializedJsonObject member = members.get(current);
					if (member != null) {
						String uid = member.get("id").getAsString();
						try (Connection conn = DBUtils.conn("main")) {
							try (PreparedStatement stat = conn.prepareStatement(INQUIRIES_SQL)) {
								stat.setString(1, uid);
								stat.setString(2, uid);
								stat.setString(3, uid);
								try (ResultSet rs = stat.executeQuery()) {
									while(rs.next()) {
										JsonObject item = new JsonObject();
										item.addProperty("uid", Utils.obfuscate(rs.getString("uid")));
										item.addProperty("category", rs.getInt("category"));
										item.addProperty("qt", StringUtils.trimToEmpty(rs.getString("qt")));
										item.addProperty("cr", DateFormatUtils.format(rs.getTimestamp("cr"), "yyyy-MM-dd HH:mm"));
										item.addProperty("name", StringUtils.trimToEmpty(rs.getString("name")));
										list.add(item);
									}
								}
							}
						} catch (Throwable e) { e.printStackTrace(); }
					}
				}
			}
			%><li class=green>
				<a data-toggle=dropdown class=dropdown-toggle href=#><i class="ace-icon fa fa-envelope icon-animated-vertical"></i><span class="badge badge-success"><%=list.size()%></span></a>
				<ul class="dropdown-menu-right dropdown-navbar dropdown-menu dropdown-caret dropdown-close">
					<li class=dropdown-header><i class="ace-icon fa fa-envelope-o"></i> <%=list.size()%> 筆 未讀與未回覆的訊息</li><%
					if (list.size() > 0) { %>
					<li class=dropdown-content>
						<ul class="dropdown-menu dropdown-navbar"><%
							JsonObject item; String uid;
							for (int i = 0, ca; i < list.size(); i++) {
								item = list.get(i);
								uid = item.get("uid").getAsString();
								ca = item.get("category").getAsInt();
								int s= ca == 1 ? shop_s++ : ca == 2 ? item_s++ : order_s++;
						  %><li><a href="<%=ca == 1 ? "shop-inquiry-mgr" : ca == 2 ? "item-inquiry-mgr" : "order-inquiry-mgr"%>.html" class=clearfix>
								<span class="msg-photo blue"><%=ca == 1 ? "商店" : ca == 2 ? "商品" : "訂單"%><br>問題</span>
								<i class="msg-photo badge-<%=ca == 1 ? "pink" : ca == 2 ? "success" : "info" %>" style=width:100%;height:100%></i>
								<span class=msg-body>
									<span class=msg-title><%=item.get("qt").getAsString()%></span>
									<span class=msg-time><i class="ace-icon fa fa-clock-o"></i> <span><%=item.get("cr").getAsString()%></span></span>
								</span>
							</a></li><%}%>
						</ul>
					</li><%
					}
					list = null;
				%></ul>
			</li>
		<%} %>
			<li class=light-blue>
				<a data-toggle=dropdown href=# class=dropdown-toggle aria-expanded=true>
					<span class=user-info><small>歡迎,</small> <%=session.getAttribute("licence.name")%></span><i class="ace-icon fa fa-caret-down"></i>
				</a>
				<ul class="user-menu dropdown-menu-right dropdown-menu dropdown-yellow dropdown-caret dropdown-close"><%
				   if (((Map<Integer, ArrayList<SerializedJsonObject>>) session.getAttribute("licence.adm")).size() > 1) {
				    %><li><a href="${prefix}adm/switch.html"><i class="ace-icon fa fa-exchange"></i>切換權限</a></li><%
			       }%><li><a href="${prefix}signout.jsp"><i class="ace-icon fa fa-power-off"></i>登出</a></li>
				</ul>
			</li>
		</ul></div>
	</div>
</div>
<div class="main-container" id="main-container">
	<div id="sidebar" class="sidebar responsive">
		<div class="sidebar-shortcuts"><a href="${prefix}shop/<%=session.getAttribute("licence.adm.current.name")%>"><h2 class="white"><%=session.getAttribute("licence.adm.current.display")%></h2></a></div><%
		LinkedHashMap<String, LinkedHashMap<String,String>> menu = new LinkedHashMap<>();
		LinkedHashMap<String,String> $submenu; // href,name
		//
		int auth = (int)session.getAttribute("licence.adm.current.auth");
		if (0 == auth) {
			menu.put("商店管理", $submenu = new LinkedHashMap<>());
			$submenu.put("shop-setup.html", "營運設定");
			$submenu.put("shop-shipping.html", "物流設定");
			$submenu.put("shop-note.html", "購物需知設定");
			$submenu.put("shop-theme.html", "版面樣式");
			//
			menu.put("商品管理", $submenu = new LinkedHashMap<>());
			$submenu.put("shop-cate.html", "分類設定");
			$submenu.put("item-mgr.html", "商品列表與查詢");
			//$submenu.put("promote-mgr.html", "促銷活動設定");
			$submenu.put("item-d.html", null);
			//
			menu.put("訂單管理", $submenu = new LinkedHashMap<>());
			$submenu.put("order-mgr.html", "訂單列表與查詢");
			$submenu.put("order-acpt.html","收單與核可列表");
			$submenu.put("order-shipping.html", "出貨處理列表");
			$submenu.put("order-trace.html", "物流追蹤列表");
			$submenu.put("order-conflict.html", "到貨客訴列表");
			$submenu.put("order-return.html", "退貨處理列表");
			//
			menu.put("客服管理", $submenu = new LinkedHashMap<>());
			$submenu.put("item-inquiry-mgr.html",  "商品客服列表");
			$submenu.put("shop-inquiry-mgr.html",  "賣場客服列表");
			$submenu.put("order-inquiry-mgr.html", "訂單客服列表");
			//
			//menu.put("廠請管理", submenu = new LinkedHashMap<>());
			//submenu.put("estat-mgr.html",  "對帳單列表與查詢");
			//submenu.put("estat-confirm.html",  "當月未結對帳單");
			//
			menu.put("哇酷須知", $submenu = new LinkedHashMap<>());
			$submenu.put("adm-policy.html",  "商品刊登規範");
			$submenu.put("add-term.html",  "商店信用卡增補合約條款");
		} else if (1 == auth) {
			menu.put("供應商管理", $submenu = new LinkedHashMap<>());
			//$submenu.put("sup-setup.html", "營運設定");
			$submenu.put("sup-shipping.html", "物流設定");
			//
			menu.put("商品管理", $submenu = new LinkedHashMap<>());
			$submenu.put("sup-cate.html", "分類設定");
			$submenu.put("supitem-mgr.html", "商品列表與查詢");
		}/* else if (2 == auth) {
			menu.put("商店系統管理", $submenu = new LinkedHashMap<>());
		} else if (3 == auth) {
			menu.put("供應商系統管理", $submenu = new LinkedHashMap<>());
		}*/
		$submenu = null;
		%><ul class="nav nav-list"><%
		if (menu.size() > 0) {
			String uri = ((String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI)).substring(5);
			String cate = StringUtils.trimToNull(request.getParameter("cate"));
			boolean faq = cate != null && "faq.html".equals(uri); 
			StringBuilder buf = new StringBuilder();
			menu.forEach((name, submenu) -> {
				boolean $faq = faq && StringUtils.equalsIgnoreCase(name, cate);
				buf.append("<li");
				if ($faq || submenu.containsKey(uri)) {
					buf.append(" class=active");
					request.setAttribute("menu.cate", name);
				}
				buf.append("><a href=# class=dropdown-toggle><i class=\"menu-icon fa fa-circle-o\"></i><span class=menu-text> ").append(name).append("</span><b class=\"arrow fa fa-angle-down\"></b></a>");	
				buf.append("<b class=arrow></b>");
				buf.append("<ul class=submenu>");
				submenu.forEach((href, subname) -> {
					if (subname == null) return;
					buf.append("<li"); if (uri.equals(href)) buf.append(" class=active");
					buf.append("><a href=").append(href).append("><i class=\"menu-icon fa fa-caret-right\"></i>").append(subname).append("</a></li>");
				});
				if (!"哇酷須知".equals(name)) {
					buf.append("<li"); if ($faq) buf.append(" class=active");
					buf.append("><a href=faq.html?cate=").append(name).append("><i class=\"menu-icon fa fa-question\"></i>操作說明</a></li>");
				}
				buf.append("</ul></li>");
			});
			out.append(buf);
			buf.setLength(0);
		} %></ul>
		<div class="sidebar-toggle sidebar-collapse" id=sidebar-collapse><i class="ace-icon fa fa-angle-double-left" data-icon1="ace-icon fa fa-angle-double-left" data-icon2="ace-icon fa fa-angle-double-right"></i></div>
	</div>
	<div class=main-content><div class=main-content-inner><div class=breadcrumbs id=breadcrumbs><ul class=breadcrumb><%
		%><li>管理系統</li><%
		List<SerializedJsonObject> members = (List<SerializedJsonObject>) session.getAttribute("licence.adm.members");
		if (members.size() > 0) {
			int current = (int) session.getAttribute("licence.adm.current");
			SerializedJsonObject shop = members.get(current);
			request.setAttribute("shopSelectorId", RandomStringUtils.randomAlphabetic(5));
			%><li><select id=${shopSelectorId}><%
			for (int i = 0; i < members.size(); i++) {
				%><option value="<%=i%>"<% if (i == current) { %> selected<% }%>><%=members.get(i).get("name").getAsString()%></option><%
			}
			%></select></li><%
			out = WebUtils.script(request, out);
			%><script>(function($){$(document).ready(function(){
			$('#${shopSelectorId}').change(function(e){
				location.replace('<%=request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI)%>?current=' + $(this).val());
			});
			});})(jQuery);</script><%
			out = WebUtils.script(out);
			request.removeAttribute("shopSelectorId");
		}
		if (request.getAttribute("menu.cate") != null) { %><li><%=request.getAttribute("menu.cate")%></li><% }
		ContentProperty sitemesh = ((Content) request.getAttribute("org.sitemesh.content.Content")).getExtractedProperties();
		if (sitemesh.hasChild("title") && StringUtils.isNotBlank(sitemesh.getChild("title").getValue())) {
		%><li class=active><sitemesh:write property='title' /></li><%
		}
		%>
	</ul></div><div class=page-content><div class=row><div class=col-xs-12><sitemesh:write property='body'/></div></div></div></div></div>
	<div class=footer><div class=footer-inner>
		<div class=footer-content><span class=bigger-120><span class="blue bolder">哇酷商城</span> &copy; 2010 - <%=Calendar.getInstance().get(Calendar.YEAR)%></span></div>
	</div></div>
	<a href=# id=btn-scroll-up class="btn-scroll-up btn btn-sm btn-inverse"><i class="ace-icon fa fa-angle-double-up icon-only bigger-110"></i></a>
</div><script src=${cdn}js/ace-extra.js></script>
<!--[if lte IE 8]><script src="${cdn}js/ie-fix.js"></script><![endif]-->
<script>
try{ace.settings.check('navbar','fixed')}catch(e){}
try{ace.settings.check('main-container','fixed')}catch(e){}
try{ace.settings.check('sidebar','fixed')}catch(e){}
try{ace.settings.check('sidebar','collapsed')}catch(e){}
try{ace.settings.check('breadcrumbs','fixed')}catch(e){}
</script>
<!--[if !IE]> --><script src=${cdn}js/jquery2.js></script><!-- <![endif]-->
<!--[if IE]><script src="${cdn}js/jquery1.js"></script><![endif]-->
<!--[if !IE]> --><script>window.jQuery || document.write("<script src='${cdn}js/jquery1.js'>"+"<"+"/script>");</script><!-- <![endif]-->
<!--[if IE]><script>window.jQuery || document.write("<script src='${cdn}js/jquery1x.js'>"+"<"+"/script>");</script><![endif]-->
<script>if('ontouchstart' in document.documentElement) document.write("<script src='${cdn}js/jquery.mobile.js'>"+"<"+"/script>");</script>
<script src=${cdn}adm/js/bootstrap.js></script>
<!--[if lte IE 8]><script src="${cdn}js/excanvas.js"></script><![endif]-->
<script src=${cdn}js/jqueryui1.js></script>
<script src=${cdn}js/jqueryui.touch-punch.js></script>
<script src=${cdn}js/jquery.easypiechart.js></script>
<script src=${cdn}js/jquery.sparkline.js></script>
<script src=${cdn}js/jquery.flot.js></script>
<script src=${cdn}js/ace.js></script>
<script src=${cdn}js/default.js></script>
<script>jQuery(function($) {
$("li a[href='<%=request.getAttribute("current")%>']").parent().addClass("active").parent().parent().addClass("open");
$("li a[href='order-acpt.html']").append($("<span class='badge badge-pink'>").text('<%=apct%>'));
$("li a[href='order-shipping.html']").append($("<span class='badge badge-pink'>").text('<%=shipping%>'));
$("li a[href='order-trace.html']").append($("<span class='badge badge-pink'>").text('<%=trace%>'));
$("li a[href='order-return.html']").append($("<span class='badge badge-pink'>").text('<%=Return%>'));
$("li a[href='order-conflict.html']").append($("<span class='badge badge-pink'>").text(<%=conflict%>));
$("#sidebar ul.nav>li>a").eq(2).append($("<span class='badge badge-pink'>").text(<%=apct+shipping+trace+Return+conflict%>));

$("li a[href='item-inquiry-mgr.html']").append($("<span class='badge badge-success'>").text('<%=item_s%>'));
$("li a[href='shop-inquiry-mgr.html']").append($("<span class='badge badge-success'>").text('<%=shop_s%>'));
$("li a[href='order-inquiry-mgr.html']").append($("<span class='badge badge-success'>").text('<%=order_s%>'));
$("#sidebar ul.nav>li>a").eq(3).append($("<span class='badge badge-success'>").text(<%=order_s+shop_s+item_s%>));
});</script><%
if (request.getAttribute("script") != null) { %>${script}<% }
%></body></html>