package com.eapollo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

public class JSPCache {
	private File cache;

	public JSPCache name(String name) {
		this.cache = new File(SystemUtils.JAVA_IO_TMPDIR, String.format("%s.cache", name));
		return this;
	}

	private long expire;

	public JSPCache expire(long expire) {
		this.expire = expire;
		return this;
	}

	public JSPCache expire(long expire, TimeUnit unit) {
		this.expire = unit.toMillis(expire);
		return this;
	}

	private File ref = null;

	public JSPCache ref(HttpServletRequest request) {
		String path = (String) request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
		if (path == null) path = (String) request.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH);
		if (path == null) path = request.getRequestURI();
		ref = new File(request.getServletContext().getRealPath(path));
		return this;
	}

	public boolean hit(JspWriter out) {
		long last = cache.lastModified();
		if (cache.exists() && (System.currentTimeMillis() - last) < expire && (ref == null || ref.lastModified() < last)) {
			try (InputStream in = new FileInputStream(cache)) {
				IOUtils.copy(in, out, "UTF-8");
				return true;
			} catch (Throwable e) {}
		}
		return false;
	}

	public JspWriter wrap(JspWriter out) throws IOException {
		return new TeeJspWriter(out, cache, "UTF-8");
	}

	public JspWriter done(JspWriter out) throws IOException {
		if (out instanceof TeeJspWriter) {
			out.flush();
			out.close();
			out = ((TeeJspWriter) out).original();
		}
		return out;
	}

	public JSPCache expire() { // force expire
		if (cache.exists()) cache.delete();
		return this;
	}
}