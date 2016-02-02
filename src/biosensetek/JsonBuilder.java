package biosensetek;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonBuilder {
	public static final JsonBuilder arr() {
		JsonBuilder builder = new JsonBuilder();
		builder.arr = new JsonArray();
		return builder;
	}

	private JsonArray arr;

	public JsonBuilder arr(JsonElement element) {
		arr.add(element);
		return this;
	}

	public JsonBuilder arr(Boolean... value) {
		for (Boolean val : value) {
			arr.add(new JsonPrimitive(val));
		}
		return this;
	}

	public JsonBuilder arr(Character... value) {
		for (Character val : value) {
			arr.add(new JsonPrimitive(val));
		}
		return this;
	}

	public JsonBuilder arr(Number... value) {
		for (Number val : value) {
			arr.add(new JsonPrimitive(val));
		}
		return this;
	}

	public JsonBuilder arr(String... value) {
		for (String val : value) {
			arr.add(new JsonPrimitive(val));
		}
		return this;
	}

	public JsonArray array() {
		return arr;
	}

	public static final JsonBuilder obj() {
		JsonBuilder builder = new JsonBuilder();
		builder.obj = new JsonObject();
		return builder;
	}

	private JsonObject obj;

	public JsonBuilder obj(String property, Boolean value) {
		obj.addProperty(property, value);
		return this;
	}

	public JsonBuilder obj(String property, Character value) {
		obj.addProperty(property, value);
		return this;
	}

	public JsonBuilder obj(String property, Number value) {
		obj.addProperty(property, value);
		return this;
	}

	public JsonBuilder obj(String property, String value) {
		obj.addProperty(property, value);
		return this;
	}

	public JsonBuilder obj(String property, JsonElement value) {
		obj.add(property, value);
		return this;
	}

	public JsonObject object() {
		return obj;
	}
}