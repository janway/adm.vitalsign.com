package com.biosensetek.nil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class NullResponse implements HttpServletResponse {
	public static final NullResponse wrap(ServletResponse wrap) {
		return new NullResponse((HttpServletResponse) wrap);
	}

	private HttpServletResponse wrap;

	public NullResponse(HttpServletResponse wrap) {
		this.wrap = wrap;
	}

	@Override
	public void flushBuffer() throws IOException {}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public String getCharacterEncoding() {
		return wrap.getCharacterEncoding();
	}

	@Override
	public String getContentType() {
		return wrap.getContentType();
	}

	@Override
	public Locale getLocale() {
		return wrap.getLocale();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return NullOutput.DEFAULT;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return NullWriter.DEFAULT;
	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void reset() {}

	@Override
	public void resetBuffer() {}

	@Override
	public void setBufferSize(int arg0) {}

	@Override
	public void setCharacterEncoding(String arg0) {}

	@Override
	public void setContentLength(int arg0) {}

	@Override
	public void setContentLengthLong(long arg0) {}

	@Override
	public void setContentType(String arg0) {}

	@Override
	public void setLocale(Locale arg0) {}

	@Override
	public void addCookie(Cookie arg0) {}

	@Override
	public void addDateHeader(String arg0, long arg1) {}

	@Override
	public void addHeader(String arg0, String arg1) {}

	@Override
	public void addIntHeader(String arg0, int arg1) {}

	@Override
	public boolean containsHeader(String arg0) {
		return wrap.containsHeader(arg0);
	}

	@Override
	public String encodeRedirectURL(String arg0) {
		return wrap.encodeRedirectURL(arg0);
	}

	@Override
	public String encodeRedirectUrl(String arg0) {
		return wrap.encodeRedirectURL(arg0);
	}

	@Override
	public String encodeURL(String arg0) {
		return wrap.encodeURL(arg0);
	}

	@Override
	public String encodeUrl(String arg0) {
		return wrap.encodeURL(arg0);
	}

	@Override
	public String getHeader(String arg0) {
		return wrap.getHeader(arg0);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return wrap.getHeaderNames();
	}

	@Override
	public Collection<String> getHeaders(String arg0) {
		return wrap.getHeaders(arg0);
	}

	@Override
	public int getStatus() {
		return wrap.getStatus();
	}

	@Override
	public void sendError(int arg0) throws IOException {}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {}

	@Override
	public void sendRedirect(String arg0) throws IOException {}

	@Override
	public void setDateHeader(String arg0, long arg1) {}

	@Override
	public void setHeader(String arg0, String arg1) {}

	@Override
	public void setIntHeader(String arg0, int arg1) {}

	@Override
	public void setStatus(int arg0) {}

	@Override
	public void setStatus(int arg0, String arg1) {}
}