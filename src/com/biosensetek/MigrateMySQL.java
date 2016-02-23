package com.biosensetek;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MigrateMySQL {
	public static final void main(String[] args) {
		DB src = new DB("wkmid", "192.168.11.14", "3306", "wowkool", "eapollo@4321");
		DB dst = new DB("wkmid", "192.168.10.180", "3306", "root", "root");
		clone(src, dst);
	}

	public static final void clone(DB src, DB dst) {
		List<String> ignore = Arrays.asList();
		cloneSchema(src, dst, ignore);// schema
		Map<String, String> condition = new HashMap<>();
		cloneData(src, dst, condition);// data
	}

	public static final void cloneSchema(DB src, DB dst, Collection<String> ignore) {
		try (CloseableProcess mysql = CloseableProcess.exec(dst.cmd("mysql", null, "--force"))) {
			try (PrintStream out = new PrintStream(mysql.getOutputStream())) {
				out.append("DROP SCHEMA IF EXISTS ").append(dst.name).println(';');
				out.append("CREATE SCHEMA ").append(dst.name).println(';');
				out.append("USE ").append(dst.name).println(';');
				out.flush();
				List<String> command = src.cmd("mysqldump", null, "--routines", "--no-data");
				if (ignore != null) {
					for (String table : ignore) {
						command.add("--ignore-table=" + src.name + "." + table);
					}
					System.out.println("ignore tables >> " + String.join(",", ignore));
				}
				command.add(src.name);
				String schema = null;
				try (CloseableProcess dump = CloseableProcess.exec(command)) {
					try (InputStream in = dump.getInputStream()) {
						int len;
						byte[] buf = new byte[4096];
						try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
							while ((len = in.read(buf)) != -1) {
								bos.write(buf, 0, len);
							}
							schema = new String(bos.toByteArray(), "UTF-8");
						}
					}
				}
				schema = schema.replace(src.name, dst.name);
				schema = schema.replace("InnoDB", "MyISAM");
				schema = schema.replace("\\s*CHARACTER SET big5", "");
				schema = schema.replaceAll("/\\*!50017 DEFINER=`.+?`@`.+?`\\*/", "");
				schema = schema.replaceAll("DEFINER=`.+`@`.+?`", "");
				schema = schema.replaceAll("\\s*AUTO_INCREMENT=\\d+", "");
				out.println(schema);
				out.println("quit");
				out.flush();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static final void cloneData(DB from, DB to, Map<String, String> condition) {
		LinkedList<String> tables = new LinkedList<>();
		try (Connection con = to.con()) {
			String table;
			try (ResultSet rs = con.getMetaData().getTables(null, null, null, new String[] { "TABLE" })) {
				if (condition != null) {
					while (rs.next()) {
						if (condition.containsKey(table = rs.getString(3))) continue;
						tables.add(table);
					}
				} else {
					while (rs.next()) {
						tables.add(rs.getString(3));
					}
				}
			}
		} catch (Throwable e) {
			System.err.println("cloneData:" + e.getMessage());
		}
		List<String> def = from.cmd("mysqldump", null, "--no-create-db", "--no-create-info", "--skip-triggers", "--skip-lock-tables"), cmd = new ArrayList<>(11);
		try (CloseableProcess mysql = CloseableProcess.exec(to.cmd("mysql", null, "--force"))) {
			byte[] buf = new byte[81920];
			int len;
			try (PrintStream out = new PrintStream(mysql.getOutputStream())) {
				out.println("USE " + to.name + ";");
				System.out.println("clone all data >> " + String.join(",", tables));
				for (String table : tables) {
					try (CloseableProcess dump = CloseableProcess.exec(cmd(def, cmd, from.name, table))) {
						try (InputStream in = dump.getInputStream()) {
							while ((len = in.read(buf)) != -1) {
								out.write(buf, 0, len);
							}
							out.flush();
						}
					} catch (Throwable e) {
						System.err.println("table:" + table + ",execption:" + e.getMessage());
					}
				}
				if (condition != null) {
					String where;
					for (String table : condition.keySet()) {
						where = condition.get(table);
						if (where == null) {
							System.out.println("skip data >> " + table);
							continue;
						}
						System.out.println("table " + table + " >> condition " + where);
						try (CloseableProcess dump = CloseableProcess.exec(cmd(def, cmd, "-w", where, from.name, table))) {
							try (InputStream in = dump.getInputStream()) {
								while ((len = in.read(buf)) != -1) {
									out.write(buf, 0, len);
								}
								out.println();
								out.flush();
							}
						} catch (Throwable e) {
							System.err.println("table:" + table + ",condition:" + where + ",execption:" + e.getMessage());
						}
					}
				}
				out.println("quit");
			}
		} catch (Throwable e) {
			System.err.println("cloneData:" + e.getMessage());
		}
	}

	public static List<String> cmd(List<String> def, List<String> cmd, String... options) {
		cmd.clear();
		cmd.addAll(def);
		for (String option : options) {
			cmd.add(option);
		}
		return cmd;
	}

	private static final class DB {
		public String name, host, port, usr, pwd;

		public DB(String name, String host, String port, String usr, String pwd) {
			super();
			this.name = name;
			this.host = host;
			this.port = port;
			this.usr = usr;
			this.pwd = pwd;
		}

		String url = null;

		String url() {
			if (url == null) url = new StringBuilder("jdbc:mysql://").append(host).append(':').append(port).append('/').append(name).append("?useUnicode=true&characterEncoding=UTF8&zeroDateTimeBehavior=convertToNull").toString();
			return url;
		}

		public Connection con() throws SQLException {
			return DriverManager.getConnection(url(), usr, pwd);
		}

		public List<String> cmd(String cmd, List<String> list, String... opts) {
			if (list == null) list = new LinkedList<>();
			list.add(cmd);
			list.add("--user=" + usr);
			list.add("--password=" + pwd);
			list.add("--host=" + host);
			list.add("--port=" + port);
			list.add("--default-character-set=utf8");
			for (String opt : opts) {
				list.add(opt);
			}
			return list;
		}
	}

	private static final class CloseableProcess extends Process implements Closeable {

		public static final CloseableProcess exec(List<String> command) throws IOException {
			// System.out.println("exec >> " + String.join(" ", command));
			return new CloseableProcess(new ProcessBuilder(command).redirectError(Redirect.PIPE).redirectInput(Redirect.PIPE).start());
		}

		private Process proc;

		public CloseableProcess(Process proc) {
			super();
			this.proc = proc;
		}

		@Override
		public void close() {
			this.destroy();
		}

		@Override
		public OutputStream getOutputStream() {
			return proc.getOutputStream();
		}

		@Override
		public InputStream getInputStream() {
			return proc.getInputStream();
		}

		@Override
		public InputStream getErrorStream() {
			return proc.getErrorStream();
		}

		@Override
		public int waitFor() throws InterruptedException {
			return proc.waitFor();
		}

		@Override
		public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
			return proc.waitFor(timeout, unit);
		}

		@Override
		public int exitValue() {
			return proc.exitValue();
		}

		@Override
		public void destroy() {
			byte[] buf = new byte[4096];
			try (InputStream in = proc.getErrorStream()) {
				while (in.read(buf) != -1) {}
			} catch (IOException e) {}
			try (InputStream in = proc.getInputStream()) {
				while (in.read(buf) != -1) {}
			} catch (IOException e) {}
			proc.destroy();
		}

		@Override
		public Process destroyForcibly() {
			return proc.destroyForcibly();
		}

		@Override
		public boolean isAlive() {
			return proc.isAlive();
		}

		@Override
		public int hashCode() {
			return proc.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return proc.equals(obj);
		}

		@Override
		public String toString() {
			return proc.toString();
		}
	}
}
