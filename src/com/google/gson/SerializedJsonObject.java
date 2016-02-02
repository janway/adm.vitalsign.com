package com.google.gson;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

public class SerializedJsonObject extends JsonElement implements Serializable {
	private static final long serialVersionUID = 6252042833045929640L;
	private final LinkedHashMap<String, JsonElement> members = new LinkedHashMap<>();

	JsonElement deepCopy() {
		SerializedJsonObject result = new SerializedJsonObject();
		members.forEach((key, value) -> result.add(key, value.deepCopy()));
		return result;
	}

	public void add(String property, JsonElement value) {
		members.put(property, value == null ? JsonNull.INSTANCE : value);
	}

	public JsonElement remove(String property) {
		return (JsonElement) members.remove(property);
	}

	public void addProperty(String property, String value) {
		add(property, createJsonElement(value));
	}

	public void addProperty(String property, Number value) {
		add(property, createJsonElement(value));
	}

	public void addProperty(String property, Boolean value) {
		add(property, createJsonElement(value));
	}

	public void addProperty(String property, Character value) {
		add(property, createJsonElement(value));
	}

	private JsonElement createJsonElement(Object value) {
		return value == null ? JsonNull.INSTANCE : new SerializedJsonPrimitive(value);
	}

	public Set<Entry<String, JsonElement>> entrySet() {
		return members.entrySet();
	}

	public boolean has(String memberName) {
		return members.containsKey(memberName);
	}

	public JsonElement get(String memberName) {
		return members.get(memberName);
	}

	public JsonPrimitive getAsJsonPrimitive(String memberName) {
		return (JsonPrimitive) members.get(memberName);
	}

	public JsonArray getAsJsonArray(String memberName) {
		return (JsonArray) members.get(memberName);
	}

	public JsonObject getAsJsonObject(String memberName) {
		return (JsonObject) members.get(memberName);
	}

	public boolean equals(Object o) {
		return (o == this) || (((o instanceof JsonObject)) && (members.equals(members)));
	}

	public int hashCode() {
		return members.hashCode();
	}
}