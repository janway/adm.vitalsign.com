package biosensetek;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;

public class DBUtils {
	private static final String DRIVER = "driverClass";
	private static Map<String, DataSource> sources = null;

	public static final void init(File dir) {
		if (sources == null) sources = new HashMap<>();
		Properties prop;
		DataSource source;
		for (File file : dir.listFiles()) {
			if ((prop = Utils.load(file)) == null) continue;
			if (prop.containsKey(DRIVER)) Utils.forName(StringUtils.trimToNull((String) prop.remove(DRIVER)));
			if (prop.size() == 0) continue;
			try {
				source = new BoneCPDataSource(new BoneCPConfig(prop));
			} catch (Throwable e) {
				continue;
			}
			source = sources.put(file.getName(), source);
			if (source != null) Utils.close(source);
		}
	}

	public static final void destroy() {
		sources.forEach(Utils::close);
		sources.clear();
		sources = null;
	}

	public static Connection conn(String name) throws SQLException {
		DataSource source = sources.get(name);
		if (source == null) return null;
		return source.getConnection();
	}

	public static Connection conn(String... name) throws SQLException {
		return conn(name[name.length == 1 ? 0 : ThreadLocalRandom.current().nextInt(name.length)]);
	}

	public static final int seq(Connection conn, String key) {
		int result = -1;
		try (Statement stat = conn.createStatement()) {
			stat.executeUpdate("INSERT INTO s(k,v)VALUES('" + key + "',0) ON DUPLICATE KEY UPDATE v=v+1;");
			try (ResultSet res = stat.executeQuery("SELECT v FROM s WHERE k='" + key + "'")) {
				if (res.next()) {
					result = res.getInt(1);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return result;
	}

	public static final String uuid(Connection conn) {
		try (Statement stat = conn.createStatement()) {
			try (ResultSet res = stat.executeQuery("SELECT newid()")) {
				if (res.next()) return res.getString(1);
			}
		} catch (Throwable e) {}
		return Utils.uuid();
	}

	private static PreparedStatement bind(PreparedStatement stat, Object... parameters) throws SQLException {
		if (parameters != null && parameters.length > 0) {
			int cursor = 1;
			for (Object parameter : parameters) {
				stat.setObject(cursor++, parameter);
			}
		}
		return stat;
	}

	public static final <T extends Appendable> T opts(Connection conn, T out, String query, Object... parameters) {
		try (PreparedStatement stat = conn.prepareStatement(query)) {
			try (ResultSet res = bind(stat, parameters).executeQuery()) {
				int cnt = res.getMetaData().getColumnCount();
				while (res.next()) {
					try {
						out.append("<option");
						if (cnt > 1) out.append(" value=\"").append(res.getString(2)).append("\"");
						out.append(">").append(res.getString(1)).append("</option>");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return out;
	}

	public static final <T extends Appendable> T opts(String def, Connection conn, T out, String query, Object... parameters) {
		try (PreparedStatement stat = conn.prepareStatement(query)) {
			try (ResultSet res = bind(stat, parameters).executeQuery()) {
				int cnt = res.getMetaData().getColumnCount();
				while (res.next()) {
					try {
						out.append("<option");
						if (cnt > 1) {
							out.append(" value=\"").append(res.getString(2)).append("\"");
							if (StringUtils.equals(res.getString(2), def)) out.append(" selected");
						} else {
							if (StringUtils.equals(res.getString(1), def)) out.append(" selected");
						}
						out.append(">").append(res.getString(1)).append("</option>");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return out;
	}

	public static final Date date(java.util.Date date) {
		if (date == null) return null;
		if (date instanceof Date) return (Date) date;
		return date(date.getTime());
	}

	public static final Date date(long time) {
		return new Date(time);
	}

	public static final Time time(java.util.Date date) {
		if (date == null) return null;
		if (date instanceof Time) return (Time) date;
		return time(date.getTime());
	}

	public static final Time time(long time) {
		return new Time(time);
	}

	public static final Timestamp timestamp(java.util.Date date) {
		if (date == null) return null;
		if (date instanceof Timestamp) return (Timestamp) date;
		return timestamp(date.getTime());
	}

	public static final Timestamp timestamp(long time) {
		return new Timestamp(time);
	}
}