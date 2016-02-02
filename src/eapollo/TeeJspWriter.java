package com.eapollo;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

import javax.servlet.jsp.JspWriter;

public class TeeJspWriter extends JspWriter {

	private JspWriter out;
	private PrintStream tee;

	public JspWriter original() {
		return out;
	}

	public TeeJspWriter(JspWriter out, File tee, String encoding) throws IOException {
		super(0, false);
		this.out = out;
		this.tee = new PrintStream(tee, encoding);
	}

	@Override
	public void clear() throws IOException {
		out.clear();
	}

	@Override
	public void clearBuffer() throws IOException {
		out.clearBuffer();
	}

	@Override
	public void close() throws IOException {
		// out.close();
		tee.close();
	}

	@Override
	public void flush() throws IOException {
		out.flush();
		tee.flush();
	}

	@Override
	public int getRemaining() {
		return out.getRemaining();
	}

	@Override
	public void print(boolean b) throws IOException {
		out.print(b);
		tee.print(b);
	}

	@Override
	public void print(char c) throws IOException {
		out.print(c);
		tee.print(c);
	}

	@Override
	public void print(int i) throws IOException {
		out.print(i);
		tee.print(i);
	}

	@Override
	public void print(long l) throws IOException {
		out.print(l);
		tee.print(l);
	}

	@Override
	public void print(float f) throws IOException {
		out.print(f);
		tee.print(f);
	}

	@Override
	public void print(double d) throws IOException {
		out.print(d);
		tee.print(d);
	}

	@Override
	public void print(char[] s) throws IOException {
		write(s, 0, s.length);
	}

	@Override
	public void print(String s) throws IOException {
		print(s.toCharArray());
	}

	@Override
	public void print(Object o) throws IOException {
		out.print(o);
		tee.print(o);
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		out.write(cbuf, off, len);
		StringBuilder tmp = new StringBuilder(len);
		for (int i = off, j = Math.min(i + len, cbuf.length); i < j; i++) {
			tmp.append(cbuf[i]);
		}
		tee.append(tmp);
		tmp = null;
	}

	@Override
	public Writer append(char c) throws IOException {
		out.append(c);
		tee.append(c);
		return this;
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		out.append(csq);
		tee.append(csq);
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		out.append(csq, start, end);
		tee.append(csq, start, end);
		return this;
	}

	@Override
	public void newLine() throws IOException {}

	@Override
	public void println() throws IOException {}

	@Override
	public void println(boolean b) throws IOException {
		print(b);
	}

	@Override
	public void println(char c) throws IOException {
		print(c);
	}

	@Override
	public void println(int i) throws IOException {
		print(i);
	}

	@Override
	public void println(long l) throws IOException {
		print(l);
	}

	@Override
	public void println(float f) throws IOException {
		print(f);
	}

	@Override
	public void println(double d) throws IOException {
		print(d);
	}

	@Override
	public void println(char[] s) throws IOException {
		print(s);
	}

	@Override
	public void println(String s) throws IOException {
		print(s);
	}

	@Override
	public void println(Object o) throws IOException {
		print(o);
	}
}