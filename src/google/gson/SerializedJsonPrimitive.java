package com.google.gson;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.google.gson.internal.LazilyParsedNumber;

public class SerializedJsonPrimitive extends JsonElement implements Serializable {
	private static final long serialVersionUID = 4933912291201966538L;

	private static final Class<?>[] PRIMITIVE_TYPES = { Integer.TYPE, Long.TYPE, Short.TYPE, Float.TYPE, Double.TYPE, Byte.TYPE, Boolean.TYPE, Character.TYPE, Integer.class, Long.class, Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class };

	private Object value;

	public SerializedJsonPrimitive(Boolean bool) {
		setValue(bool);
	}

	public SerializedJsonPrimitive(Number number) {
		setValue(number);
	}

	public SerializedJsonPrimitive(String string) {
		setValue(string);
	}

	public SerializedJsonPrimitive(Character c) {
		setValue(c);
	}

	SerializedJsonPrimitive(Object primitive) {
		setValue(primitive);
	}

	JsonElement deepCopy() {
		return this;
	}

	void setValue(Object primitive) {
		if (primitive instanceof Character) {
			value = String.valueOf(((Character) primitive).charValue());
		} else if (primitive instanceof Number || isPrimitiveOrString(primitive)) {
			value = primitive;
		}
	}

	public boolean isBoolean() {
		return value instanceof Boolean;
	}

	Boolean getAsBooleanWrapper() {
		return (Boolean) value;
	}

	public boolean getAsBoolean() {
		if (isBoolean()) return getAsBooleanWrapper().booleanValue();
		return Boolean.parseBoolean(getAsString());
	}

	public boolean isNumber() {
		return value instanceof Number;
	}

	public Number getAsNumber() {
		return value instanceof String ? new LazilyParsedNumber((String) value) : (Number) value;
	}

	public boolean isString() {
		return value instanceof String;
	}

	public String getAsString() {
		if (isNumber()) return getAsNumber().toString();
		if (isBoolean()) return getAsBooleanWrapper().toString();
		return (String) value;
	}

	public double getAsDouble() {
		return isNumber() ? getAsNumber().doubleValue() : Double.parseDouble(getAsString());
	}

	public BigDecimal getAsBigDecimal() {
		return value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString());
	}

	public BigInteger getAsBigInteger() {
		return value instanceof BigInteger ? (BigInteger) value : new BigInteger(value.toString());
	}

	public float getAsFloat() {
		return isNumber() ? getAsNumber().floatValue() : Float.parseFloat(getAsString());
	}

	public long getAsLong() {
		return isNumber() ? getAsNumber().longValue() : Long.parseLong(getAsString());
	}

	public short getAsShort() {
		return isNumber() ? getAsNumber().shortValue() : Short.parseShort(getAsString());
	}

	public int getAsInt() {
		return isNumber() ? getAsNumber().intValue() : Integer.parseInt(getAsString());
	}

	public byte getAsByte() {
		return isNumber() ? getAsNumber().byteValue() : Byte.parseByte(getAsString());
	}

	public char getAsCharacter() {
		return getAsString().charAt(0);
	}

	private static boolean isPrimitiveOrString(Object target) {
		if (target instanceof String) return true;
		Class<?> classOfPrimitive = target.getClass();
		for (Class<?> standardPrimitive : PRIMITIVE_TYPES) {
			if (standardPrimitive.isAssignableFrom(classOfPrimitive)) return true;
		}
		return false;
	}

	public int hashCode() {
		if (this.value == null) return 31;
		if (isIntegral(this)) {
			long value = getAsNumber().longValue();
			return (int) (value ^ value >>> 32);
		}
		if (this.value instanceof Number) {
			long value = Double.doubleToLongBits(getAsNumber().doubleValue());
			return (int) (value ^ value >>> 32);
		}
		return this.value.hashCode();
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		SerializedJsonPrimitive other = (SerializedJsonPrimitive) obj;
		if (value == null) return other.value == null;
		if (other.value == null) return false;
		if (isIntegral(this) && isIntegral(other)) return getAsNumber().longValue() == other.getAsNumber().longValue();
		if (value instanceof Number && other.value instanceof Number) {
			double a = getAsNumber().doubleValue(), b = other.getAsNumber().doubleValue();
			return a == b || (Double.isNaN(a) && Double.isNaN(b));
		}
		return value.equals(other.value);
	}

	private static boolean isIntegral(SerializedJsonPrimitive primitive) {
		if (!(primitive.value instanceof Number)) return false;
		Number number = (Number) primitive.value;
		return number instanceof BigInteger || number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte;
	}
}