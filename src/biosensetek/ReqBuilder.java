package biosensetek;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ReqBuilder {
	public static final int NUMBER = 0;
	public static final int EMAIL = 1;
	public static final int DATE = 2;

	public static ReqBuilder build(HttpServletRequest request) {
		ReqBuilder b = new ReqBuilder();
		b.map = new HashMap<>();
		b.req = request;
		return b;
	}

	private Map<String, String> map;
	private HttpServletRequest req;

	/**
	 * Add specified request parameters into map with default
	 *
	 */
	public ReqBuilder def(String key, String def) {
		map.put(key, StringUtils.defaultIfBlank(req.getParameter(key), def));
		req.setAttribute(key, StringUtils.defaultIfBlank(req.getParameter(key), def));
		return this;
	}

	//
	public ReqBuilder copy(String key1, String key2) {
		set(key1);
		map.put(key2, map.get(key1));
		req.setAttribute(key2, map.get(key1));
		return this;
	}

	//
	public ReqBuilder ren(String key0, String key1, String def) {
		req.setAttribute(key1, StringUtils.defaultIfBlank(req.getParameter(key0), def));
		req.removeAttribute(key0);
		map.put(key1, StringUtils.defaultIfBlank(req.getParameter(key0), def));
		map.remove(key0);
		return this;
	}

	public ReqBuilder ren(String key0, String key1) {
		req.setAttribute(key1, StringUtils.trimToNull(req.getParameter(key0)));
		req.removeAttribute(key0);
		map.put(key1, StringUtils.trimToNull(req.getParameter(key0)));
		map.remove(key0);
		return this;
	}

	//
	/**
	 * dump all request parameters and put into map and request attributes, all string would be trim to null if it is empty
	 */
	public ReqBuilder all() {
		Collections.list(req.getParameterNames()).forEach(k -> {
			req.setAttribute(k, StringUtils.trimToNull(req.getParameter(k)));
			map.put(k, StringUtils.trimToNull(req.getParameter(k)));
		});
		return this;
	}

	/**
	 * Add specified request parameters into map and attribute
	 */
	public ReqBuilder set(String... key) {
		for (String k : key) {
			req.setAttribute(k, StringUtils.trimToNull(req.getParameter(k)));
			map.put(k, StringUtils.trimToNull(req.getParameter(k)));
		}
		return this;
	}

	/**
	 * Add specified request parameters into request attribute but not map
	 */
	public ReqBuilder attr(String... key) {
		for (String k : key) {
			req.setAttribute(k, StringUtils.trimToNull(req.getParameter(k)));
		}
		return this;
	}

	/**
	 * put extra value into map and attribute besides of parameters
	 */
	public ReqBuilder kv(String k, String v) {
		map.put(k, StringUtils.trimToNull(v));
		req.setAttribute(k, StringUtils.trimToNull(v));
		return this;
	}

	/**
	 * put attribute value into map by key
	 */
	public ReqBuilder put2map(String k) {
		map.put(k, StringUtils.trimToNull((String) req.getAttribute(k)));
		return this;
	}

	/**
	 * dump all data into a json
	 */
	public JsonObject obj() {
		JsonObject obj = new JsonObject();
		this.map.forEach((k, v) -> {
			obj.addProperty(k, (String) map.get(k));
		});
		return obj;
	}

	/**
	 * get data as a String value from map,null if not exist
	 */
	public String val(String k) {
		return (String) map.get(k);
	}

	/**
	 * get data as an int value from map,0 if not exist
	 */
	public int integer(String k) {
		return NumberUtils.toInt((String) map.get(k), 0);
	}

	/**
	 * get data as an String value from map,empty if not exist
	 */
	public String empty(String k) {
		return StringUtils.trimToEmpty((String) map.get(k));
	}

	/**
	 * test if map contains an non-null key
	 */
	public boolean has(String... keys) {
		for (String k : keys) {
			if (map.get(k) != null) return true;
		}
		return true;
	}

	/**
	 * test if txt equals to the value of map(k)
	 */
	public boolean eq(String k, String txt) {
		if (map.get(k) != null && txt != null) { return txt.equals((String) map.get(k)); }
		return false;
	}

	/**
	 * test if attribute(k1) is greater then attribute(k2) the value of map(k)
	 */
	public boolean gt(String k1, String k2) {
		if (is(k1, NUMBER) && is(k2, NUMBER)) return integer(k1) > integer(k2);
		return false;
	}

	/**
	 * get length of value specified by key,zero if null or empty
	 */
	public int len(String k) {
		if (map.get(k) != null) return ((String) map.get(k)).length();
		return 0;
	}

	/**
	 * verify if key is a ..
	 */
	public boolean is(String k, int item) {
		if (map.get(k) == null) return false;
		if (item == NUMBER) return NumberUtils.isNumber(map.get(k));
		else if (item == EMAIL) return ValidateUtil.email(map.get(k));
		else if (item == DATE) return ValidateUtil.date(map.get(k));
		return false;
	}

	/**
	 * Get data as a map
	 */
	public Map<String, String> map() {
		return this.map;
	}
}