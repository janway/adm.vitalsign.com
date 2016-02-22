package com.biosensetek;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
public class ResultUtil {
	/**
	 * Save ResultSet content as a List<JsonObject>, return null if no records found
	 */
	public static final List<JsonObject> jsonlist(ResultSet rs) throws SQLException {
		if (rs.next()) {
			// JsonArray a = new JsonArray();
			List<JsonObject> a = new ArrayList<>();
			JsonObject o;
			ResultSetMetaData m = rs.getMetaData();
			int c = m.getColumnCount();
			do {
				a.add(o = new JsonObject());
				for (int i = 1; i <= c; i++) {
					if (Types.INTEGER == m.getColumnType(i) || Types.BIGINT == m.getColumnType(i))
						o.addProperty(m.getColumnLabel(i), rs.getInt(i));
					else if (Types.FLOAT == m.getColumnType(i))
						o.addProperty(m.getColumnLabel(i), rs.getFloat(i));
					else if (Types.DOUBLE == m.getColumnType(i))
						o.addProperty(m.getColumnLabel(i), rs.getDouble(i));
					else
						o.addProperty(m.getColumnLabel(i), StringUtils.trimToEmpty(rs.getString(i)));
				}
			} while (rs.next());
			return a;
		}
		return null;
	}
	/**
	 * Save ResultSet content as a JsonArray, return null if no records found
	 */
	public static final JsonArray array(ResultSet rs) throws SQLException {
		if (rs.next()) {
			JsonArray a = new JsonArray();
			JsonObject o;
			ResultSetMetaData m = rs.getMetaData();
			int c = m.getColumnCount();
			do {
				a.add(o = new JsonObject());
				for (int i = 1; i <= c; i++) {
					if (Types.INTEGER == m.getColumnType(i) || Types.BIGINT == m.getColumnType(i))
						o.addProperty(m.getColumnLabel(i), rs.getInt(i));
					else if (Types.FLOAT == m.getColumnType(i))
						o.addProperty(m.getColumnLabel(i), rs.getFloat(i));
					else if (Types.DOUBLE == m.getColumnType(i))
						o.addProperty(m.getColumnLabel(i), rs.getDouble(i));
					else
						o.addProperty(m.getColumnLabel(i), StringUtils.trimToEmpty(rs.getString(i)));
				}
			} while (rs.next());
			return a;
		}
		return null;
	}
	//
	public static final void array(String name, ResultSet rs, HttpServletRequest request) throws SQLException {
		request.setAttribute(name, array(rs));
	}
	//
	public static final List<Map<String, Object>> list(ResultSet rs) throws SQLException {
		List<Map<String, Object>> $list = null;
		ResultSetMetaData m = rs.getMetaData();
		if (rs.next()) {
			$list = new ArrayList<>();
			int i = 1;
			ItemBuilder ib = ItemBuilder.create();
			while (i <= m.getColumnCount()) {
				ib.add(m.getColumnLabel(i), StringUtils.trimToEmpty(rs.getString(i++)));
			}
			$list.add(ib.get());
		}
		if ($list != null) {
			while (rs.next()) {
				int i = 1;
				ItemBuilder ib = ItemBuilder.create();
				while (i <= m.getColumnCount()) {
					ib.add(m.getColumnLabel(i), StringUtils.trimToEmpty(rs.getString(i++)));
				}
				$list.add(ib.get());
			}
		}
		return $list;
	}
	//
	public static final void list(String name, ResultSet rs, HttpServletRequest request) throws SQLException {
		request.setAttribute(name, list(rs));
	}
	//
	public static final JsonObject json(ResultSet rs) throws SQLException {
		JsonObject json = null;
		if (rs.next()) {
			json = new JsonObject();
			ResultSetMetaData m = rs.getMetaData();
			for (int i = 1, j = m.getColumnCount(); i <= j; i++) {
				if (Types.INTEGER == m.getColumnType(i) || Types.BIGINT == m.getColumnType(i))
					json.addProperty(m.getColumnLabel(i), rs.getInt(i));
				else if (Types.FLOAT == m.getColumnType(i))
					json.addProperty(m.getColumnLabel(i), rs.getFloat(i));
				else if (Types.DOUBLE == m.getColumnType(i))
					json.addProperty(m.getColumnLabel(i), rs.getDouble(i));
				else
					json.addProperty(m.getColumnLabel(i), StringUtils.trimToEmpty(rs.getString(i)));
			}
		}
		return json;
	}
	//
	public static final void attr(ResultSet rs, HttpServletRequest request) throws SQLException {
		ResultSetMetaData m = rs.getMetaData();
		if (rs.next()) {
			int i = 1;
			while (i <= m.getColumnCount()) {
				if (Types.INTEGER == m.getColumnType(i) || Types.BIGINT == m.getColumnType(i))
					request.setAttribute(m.getColumnLabel(i), rs.getInt(i++));
				else if (Types.FLOAT == m.getColumnType(i))
					request.setAttribute(m.getColumnLabel(i), rs.getFloat(i++));
				else if (Types.DOUBLE == m.getColumnType(i))
					request.setAttribute(m.getColumnLabel(i), rs.getDouble(i++));
				else
					request.setAttribute(m.getColumnLabel(i), StringUtils.trimToEmpty(rs.getString(i++)));
			}
		}
	}
	//
}