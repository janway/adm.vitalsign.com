<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%><!DOCTYPE html><html><head><meta charset="utf-8">
<meta name="viewport" content="width=1024"><meta http-equiv="X-UA-Compatible" content="IE=edge">
<title><sitemesh:write property='title'/> 哇酷商城</title>
<link rel="stylesheet" href="${cdn}css/bootstrap.css">
<link rel="stylesheet" href="${cdn}font/fontawesome.css">
<link rel="stylesheet" href="${cdn}css/default.css">
<link rel="stylesheet" href="${cdn}css/common.css">
<link rel="stylesheet" href="${cdn}css/cart.css">
<sitemesh:write property='head'/><%if (request.getAttribute("style") != null) {%>${style}<%}%></head><body>
<div class="container"><div class="bbgd5 pb50 mar-bottom100"><sitemesh:write property='body'/></div><nav class="navbar navbar-fixed-bottom" id="foorter-bar"><div><div class="container">
<div class="navbar-header"><img src="${cdn}img/footer-services.png" id="go-services-img"><div class="panel panel-default go-services" id="go-services">
<div class="panel-heading">漢唐集團服務</div><div class="panel-body">
<div class="row">
<div class="col-md-6"><a href="http://www.global-opto.com"><img src="${cdn}img/footer-global-opto.png" alt="漢唐光電">漢唐光電</a><p>WoWKool 哇酷與 eApollo 太陽神網站服務群之母公司</p></div>
<div class="col-md-6"><a href="http://www.eapollo.com"><img src="${cdn}img/footer-eapollo.png" alt="eApollo 搜尋引擎">eApollo 搜尋引擎</a><p>eApollo 太陽神搜尋引擎</p></div>
</div>
<div class="row">
<div class="col-md-6"><a href="http://www.8899b2b.com"><img src="${cdn}img/footer-8899b2b.png" alt="eApollo B2B">eApollo B2B</a><p>eApollo 太陽神 B2B 服務，企業會員 2300 萬</p></div>
<div class="col-md-6"><a href="http://blog.eapollo.com"><img src="${cdn}img/footer-blog.png" alt="eApollo 部落格">eApollo 部落格</a><p>eApollo 太陽神部落格服務，社群、微網誌、影音一把罩</p></div>
</div>
</div></div></div>
<div class="navbar-collapse collapse"><ul class="nav navbar-nav navbar-right no-bg-hover"><%
if (session.getAttribute("licence.shop") != null) { %><li><a href="${prefix}adm/" title="商店管理">商店管理</a></li><% }
if (session.getAttribute("licence") != null) {
	%><li><a href="${prefix}profile.html">我的帳戶</a></li><li><a href="${prefix}signout.jsp" title="登出">登出</a></li><%
} else {
	%><li><a href="${prefix}signup.html" title="註冊">註冊</a></li><li><a href="${prefix}signin.html" title="登入">登入</a></li><%
}
%><li><a href="${prefix}profile-cart-list.html" title="購物車列表">購物車列表</a></li><li><a href="javascript:scroll(0,0)" title="返回頂端">返回頂端</a></li>
</ul></div>
</div></div></nav></div>
<script src="${cdn}js/bootstrap.js"></script><%if (request.getAttribute("script") != null) {%>${script}<%}%><!--[if lt IE 9]><script src="${prefix}js/f.js"></script><![endif]-->
<script src="${cdn}js/default.js"></script></body></html>