package com.eapollo;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import it.sauronsoftware.cron4j.Scheduler;

public class Adapter implements Filter {
	private Scheduler scheduler;

	@Override
	public void init(FilterConfig cfg) throws ServletException {
		ServletContext context = cfg.getServletContext();
		File INF = new File(context.getRealPath("/WEB-INF"));
		//
		CipherUtil.init(new File(INF, ".keystore"));
		DBUtils.init(new File(INF, "ds"));
		MailBuilder.init(new File(INF, "es"));
		//
		File crontab = new File(INF, "crontab");
		if (crontab.isFile()) {
			scheduler = new Scheduler();
			scheduler.setDaemon(true);
			scheduler.scheduleFile(crontab);
			scheduler.start();
		}
	}

	@Override
	public void destroy() {
		DBUtils.destroy();
		MailBuilder.destroy();
		try {
			if (scheduler != null && scheduler.isStarted()) scheduler.stop();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		req.setAttribute("rewrite.res", res);
		include(req, res, "/WEB-INF/rewrite.jsp");
		req.removeAttribute("rewrite.res");
		if (req.getAttribute("rewrite") != null) return;
		if (req.getAttribute("redirect") != null) {
			((HttpServletResponse) res).sendRedirect((String) req.getAttribute("redirect"));
			return;
		}
		include(req, res, "/WEB-INF/before.jsp");
		Throwable t = null;
		if (req.getAttribute("redirect") == null) {
			if (req.getAttribute("prevent") == null) {
				try {
					// ((HttpServletResponse) res).setHeader("Access-Control-Allow-Origin", "*");
					chain.doFilter(req, res);
				} catch (Throwable e) {
					t = e;
				}
			}
		} else {
			((HttpServletResponse) res).sendRedirect((String) req.getAttribute("redirect"));
		}
		include(req, res, "/WEB-INF/after.jsp");
		if (t != null && !res.isCommitted()) {
			if (t instanceof IOException) throw (IOException) t;
			if (t instanceof ServletException) throw (ServletException) t;
			throw new RuntimeException(t);
		}
	}

	private void include(ServletRequest req, ServletResponse res, String path) {
		try {
			req.getRequestDispatcher(path).include(req, res);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}