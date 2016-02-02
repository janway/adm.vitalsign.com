package com.biosensetek;

import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;

public class SolrUtil {
	public static final String LABEL = "log.doc";

	public static StringBuilder logId(long millis) {
		StringBuilder s = new StringBuilder(32).append(millis);
		for (int i = 0, j; i < 16; i++) {
			s.append((char) ((j = ThreadLocalRandom.current().nextInt(62)) + (j < 10 ? 48 : j < 36 ? 55 : 61)));
		}
		return s;
	}

	public static CharSequence title(HttpServletRequest request) {
		SolrInputDocument doc = (SolrInputDocument) request.getAttribute(LABEL);
		if (doc != null) {
			CharSequence title = (CharSequence) doc.getFieldValue("title_s");
			if (StringUtils.isNotBlank(title)) { return title; }
		}
		return StringUtils.EMPTY;
	}

	public static <T extends CharSequence> T title(HttpServletRequest request, T data) {
		SolrInputDocument doc = (SolrInputDocument) request.getAttribute(LABEL);
		if (doc != null) doc.setField("title_s", data);
		return data;
	}
}