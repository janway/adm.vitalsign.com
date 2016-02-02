package com.biosensetek;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ResultUtil {
	public static final List<String> arrayList(ResultSet rs) throws SQLException {
		List<String> $list = null;
		// ResultSetMetaData m = rs.getMetaData();
		if (rs.next()) {
			$list = new ArrayList<>();
			// int i = 1;
			// JsonObject j = new JsonObject();
			$list.add(rs.getString(1));
			while (rs.next()) {
				$list.add(rs.getString(1));
			}
		}
		return $list;
	}

	/**
	 * Save ResultSet content as a JsonArray, return null if no records found
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
			ResultSetMetaData meta = rs.getMetaData();
			for (int i = 1, j = meta.getColumnCount(); i <= j; i++) {
				json.addProperty(meta.getColumnLabel(i), StringUtils.trimToEmpty(rs.getString(i)));
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
				request.setAttribute(m.getColumnLabel(i), StringUtils.trimToEmpty(rs.getString(i++)));
			}
		}
	}

	//
	public static final List<String> groupCount(ResultSet rs, HttpServletRequest request, String field) throws SQLException {
		if (rs.next()) {
			List<String> a = new ArrayList<>();
			do {
				if (!a.contains(rs.getString(field))) {
					a.add(rs.getString(field));
				}
			} while (rs.next());
			return a;
		}
		return null;
	}

	//
	/**
	 * Build bi-layered tree structure JsonArray from flattened data. [{item:itemVal,other:otherV,childName:[{item:itemVal,other:otherV}]}]
	 */
	public static final JsonArray tree(ResultSet rs, String item, String parent, String childName) throws SQLException {
		JsonArray $tree = null;
		JsonObject o = null;
		if (rs.next()) {
			Map<String, JsonObject> r = new HashMap<>();
			ResultSetMetaData m = rs.getMetaData();
			$tree = new JsonArray();
			do {
				if (rs.getString(parent) == null) {
					$tree.add(o = new JsonObject());
					r.put(rs.getString(item), o);
				} else {
					JsonObject p = r.get(rs.getString(parent));
					if (p != null) {
						if (!p.has(childName)) p.add(childName, new JsonArray());
						r.get(rs.getString(parent)).get(childName).getAsJsonArray().add(o = new JsonObject());
					}
				}
				int c = 1;
				while (c <= m.getColumnCount()) {
					o.addProperty(m.getColumnLabel(c), StringUtils.trimToEmpty(rs.getString(c++)));
				}
			} while (rs.next());
		}
		return $tree;
	}
}