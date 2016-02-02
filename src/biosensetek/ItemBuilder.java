package biosensetek;

import java.util.HashMap;
import java.util.Map;

public class ItemBuilder {
	public static ItemBuilder create() {
		return new ItemBuilder();
	}

	private Map<String, Object> map;

	public ItemBuilder() {
		super();
		map = new HashMap<>();
	}

	public ItemBuilder add(String property, String value) {
		map.put(property, value);
		return this;
	}

	public ItemBuilder add(String property, Number value) {
		map.put(property, value);
		return this;
	}

	public Map<String, Object> get() {
		return map;
	}
}