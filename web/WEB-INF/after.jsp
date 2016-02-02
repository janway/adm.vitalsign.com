<%@page import="com.biosensetek.SolrUtil,org.apache.solr.client.solrj.impl.HttpSolrServer,org.apache.solr.common.SolrInputDocument"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%><%!
private static HttpSolrServer log = null;
public void jspDestroy() {
	if (log != null) log.shutdown();
}
%><%
/*
SolrInputDocument doc = (SolrInputDocument) request.getAttribute(SolrUtil.LABEL);
if (doc != null) {
	long ts = System.currentTimeMillis();
	doc.setField("ets_l", ts);
	doc.setField("cost_i", ts -  ((long) doc.getFieldValue("ts_l")));
	if (log == null) log = new HttpSolrServer("http://192.168.11.13:8983/solr/log");
	log.add(doc);
}*/
%>