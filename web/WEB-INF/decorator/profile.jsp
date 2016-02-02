<%@page import="java.util.Calendar,org.apache.commons.lang3.StringUtils"%>
<%@page import="com.eapollo.SolrUtil"%>
<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%><!DOCTYPE html><html><head><%
%><meta charset=utf-8><meta http-equiv=X-UA-Compatible content="IE=edge"><%
%><title><sitemesh:write property='title'/> 我的帳戶</title><%
%><link rel=stylesheet href=${cdn}css/bootstrap.css><link rel=stylesheet href=${cdn}font/fontawesome.css><%
%><link rel=stylesheet href=${cdn}css/common.css><link rel=stylesheet href=${cdn}css/default.css><link rel=stylesheet href=${cdn}css/profile.css><%
%><sitemesh:write property='head' /><% if (request.getAttribute("style") != null) { %>${style}<% } %></head><body><%

%><div class=top-head><div class=container><div class=col-sm-12><div class="lh35 floatRight"><ul class="sperator simple-nav"><%
if (session.getAttribute("licence") == null) {
	%><li><a href=${prefix}signin.html>登入</a></li><li><a href=${prefix}signup.html>註冊</a></li><%
} else {
	%><li><a href=${prefix}signout.jsp>登出</a></li><%
}
%><li><a href=${prefix}notice.html>會員須知</a></li><%
%><li><a href=${prefix}profile-inquiry.html>線上客服</a></li><%
%><li><a href=${prefix}openshop.html>立即開店</a></li><%
%></ul></div></div></div></div><%

%><div class=logo-seach><div class="container pad-t5"><%
%><div class="logo w250 floatLeft"><a class="display-tb margin0" href=${prefix}><img alt=WoWKool src=${cdn}img/header_logo.png></a></div><%
%><div class="w76pct floatLeft"><%

%><div class=search-block><form action=${prefix}search.html method=post><%
	%><input class="w400 form-control floatLeft mar-r10" type=search name=q placeholder=搜尋 aria-required=true required<%
	if (StringUtils.endsWith((String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI), "search.html")) {
		String query = (String) request.getAttribute("search.html/query");
		if (query != null) { %> value="<%=query%>"<% } 
	}%>><%
	%><button type=submit class="floatLeft btn btn-primary mar-r10"><i class="glyphicon glyphicon-tag mar-r5"></i>搜尋商品</button><%
	%><button type=submit class="btn btn-default mar-r10" name=z value=s>搜尋商店</button><%
%></form></div><%

%><div class=rightbar-block><%
	%><div class="floatLeft mar-r15"><a class=colorOrange href=${prefix}profile-order.html><i class="fa fa-file-text-o mar-r10"></i>訂單查詢</a></div><%
	%><div class="floatLeft mar-r15"> | </div><%
	%><div class=floatLeft><a class=comm-bule href=${prefix}profile-cart-list.html><i class="fa fa-shopping-cart mar-r10"></i>結帳</a></div><%
%></div><%

%></div><%
%></div></div><%

%><div class=main-nav><div class=container><jsp:include page="/WEB-INF/part/top-category.jsp"></jsp:include></div></div><%

%><div class="container pad-t15"><div class=mar-bottom50><%
	%><div class=col2-left><%
		%><div class=mar-bottom10><%
		String name = StringUtils.trimToNull((String) session.getAttribute("licence.name"));
		if (name != null) {
			%><span class=comm-bule><%=name%></span> <%
		}
		%>您好！</div><%
		%><div class="panel panel-gray"><%
			%><div class=panel-heading><h3 class=panel-title><i class="membericon glyphicon glyphicon-user"></i>會員資訊</h3></div><%
			%><div class=box-body><ul><%
				%><li><a href=${prefix}profile.html>個人基本資料</a></li><%
				%><li><a href=${prefix}profile-change-passowrd.html>重設密碼</a></li><%
				%><li><a href=${prefix}profile-common-address.html>常用地址管理</a></li><%
				%><li><a href=${prefix}profile-common-invoice.html>常用統編管理</a></li><%
			%></ul></div><%
		%></div><%
		%><div class="panel panel-gray"><%
			%><div class=panel-heading><h3 class=panel-title><i class="membericon glyphicon glyphicon-list-alt"></i>訂單資料</h3></div><%
			%><div class=box-body><ul><%
				%><li><a href=${prefix}profile-order.html>訂單查詢</a></li><%
				%><li><a href=${prefix}profile-cart-list.html>購物車列表</a></li><%
			%></ul></div><%
		%></div><%
		%><div class="panel panel-gray"><%
			%><div class=panel-heading><h3 class=panel-title><i class="membericon glyphicon glyphicon-star"></i>我的收藏</h3></div><%
			%><div class=box-body><ul><%
				%><li><a href=${prefix}profile-favorite-item.html>商品收藏管理</a></li><%
				%><li><a href=${prefix}profile-favorite-shop.html>商店收藏管理</a></li><%
			%></ul></div><%
		%></div><%
		%><div class="panel panel-gray"><%
			%><div class=panel-heading><h3 class=panel-title><i class="membericon glyphicon glyphicon-comment"></i>客服記錄</h3></div><%
			%><div class=box-body><ul><%
				%><li><a href=${prefix}profile-item-inquiry.html>商品客服記錄</a></li><%
				%><li><a href=${prefix}profile-shop-inquiry.html>商店客服記錄</a></li><%
				%><li><a href=${prefix}profile-inquiry.html>線上客服</a></li><%
			%></ul></div><%
		%></div><%
	%></div><%
	%><div class=col2-right><div id=breadcrumb><%
		%><a href=${prefix} title=首頁>首頁</a>&nbsp;&gt;&nbsp;<a href=${prefix}profile.html title=我的帳戶>我的帳戶</a><%
		CharSequence title = SolrUtil.title(request);
		if (StringUtils.isNotBlank(title)) {
			%>&nbsp;&gt;&nbsp;<span title="<%=title%>"><%=title%></span><%
		}
		%></div><div><%
		if(!StringUtils.equals("/profile-inquiry.html", (String) request.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH))) {
			%><div class="page-header data-title"></div><%
		}
		%><div class=mar-top20><sitemesh:write property='body'/></div><%
	%></div></div><%
	%><div class=clear></div><%
%></div></div><%

%><div class=container><%
	%><footer class="text-center mar-top20 mb50 mar-bottom100"><img src=${cdn}img/logogroup.png title=漢唐集團商標><p class=mar-top20>© 2010 - <%=Calendar.getInstance().get(Calendar.YEAR)%> 漢唐光電科技股份有限公司</p></footer><%
	%><nav class="navbar navbar-fixed-bottom" id=foorter-bar><div><div class=container><%
	%><div class=navbar-header><img src=${cdn}img/footer-services.png id=go-services-img><div class="panel panel-default go-services" id=go-services><%
	%><div class=panel-heading>漢唐集團服務</div><div class=panel-body><%
	%><div class=row><%
		%><div class=col-md-6><a href=http://www.global-opto.com><img src=${cdn}img/footer-global-opto.png alt=漢唐光電>漢唐光電</a><p>WoWKool 哇酷與 eApollo 太陽神網站服務群之母公司</p></div><%
		%><div class=col-md-6><a href=http://www.eapollo.com><img src=${cdn}img/footer-eapollo.png alt="eApollo 搜尋引擎">eApollo 搜尋引擎</a><p>eApollo 太陽神搜尋引擎</p></div><%
	%></div><%
	%><div class=row><%
		%><div class=col-md-6><a href=http://www.8899b2b.com><img src=${cdn}img/footer-8899b2b.png alt="eApollo B2B">eApollo B2B</a><p>eApollo 太陽神 B2B 服務，企業會員 2300 萬</p></div><%
		%><div class=col-md-6><a href=http://blog.eapollo.com><img src=${cdn}img/footer-blog.png alt="eApollo 部落格">eApollo 部落格</a><p>eApollo 太陽神部落格服務，社群、微網誌、影音一把罩</p></div><%
	%></div><%
	%></div></div></div><%
	%><div class="navbar-collapse collapse"><ul class="nav navbar-nav navbar-right no-bg-hover"><%
	if (session.getAttribute("licence.adm") != null) {
		%><li><a href=${prefix}adm/ title=商店管理>商店管理</a></li><%
	}
	if (session.getAttribute("licence") != null) {
		%><li><a href=${prefix}profile.html>我的帳戶</a></li><li><a href=${prefix}signout.jsp title=登出>登出</a></li><%
	} else {
		%><li><a href=${prefix}signup.html title=註冊>註冊</a></li><li><a href=${prefix}signin.html title=登入>登入</a></li><%
	}
	%><li><a href=${prefix}profile-cart-list.html title=購物車>購物車</a></li><%
	%><li><a href=javascript:scroll(0,0) title=返回頂端 data-toggle=tooltip data-placement=top>返回頂端</a></li><%
	%></ul></div><%
	%></div></div></nav><%
%></div><%

%><script src=${cdn}js/bootstrap.js></script><script src=${cdn}js/scrollToTop.js></script><script src=${cdn}js/default.js></script><%
%><!--[if lt IE 9]><script src=${cdn}js/ie-fix.js></script><![endif]--><%
if (request.getAttribute("script") != null) { %>${script}<% }
%><script>(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','//www.google-analytics.com/analytics.js','ga');ga('create','UA-42854088-3','auto');ga('send','pageview');</script><%
%></body></html>