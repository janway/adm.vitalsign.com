package com.eapollo;

import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class Validator {
	public static final Validator create(String val) {
		return new Validator().val(val);
	}

	private String val;

	public Validator val(String val) {
		this.val = StringUtils.trimToNull(val);
		return this;
	}

	public String val() {
		return val;
	}

	public String valid() {
		if (StringUtils.isBlank(val)) return null;
		boolean b = true;
		if (length) b = min <= val.length() && val.length() <= max;
		if (b && equalTo) b = val.equals($val);
		if (b && equalsIgnoreCase) b = val.equalsIgnoreCase($val);
		if (b && notEqualTo) b = !val.equals(not$val);
		if (b && notEqualsIgnoreCase) b = !val.equalsIgnoreCase(not$val);
		if (b && number) b = NumberUtils.isNumber(val);
		if (b && date) b = ValidateUtil.date(val);
		if (b && email) b = ValidateUtil.email(val);
		if (b && identityNumber) b = ValidateUtil.identityNumber(val);
		if (b && invoiceNumber) b = ValidateUtil.invoiceNumber(val);
		if (b && regex) b = val.matches(pattern);
		if (b && mobile && (b = val.matches("^(?:\\+886|0)9[0-9]{8}$"))) val = val.replaceFirst("^(?:\\+886|0)(9[0-9]{8})$", "0$1");
		if (b && tel && (b = val.matches("^(?:\\+?886|0)?(?:[29][0-9]{8}|4[0-9]{7,8}|[3-8][0-9]{7})(?:\\s*#\\s*[0-9]{1,10})?$"))) val = val.replaceFirst("^(?:\\+?886|0)?([29][0-9]{8}|4[0-9]{7,8}|[3-8][0-9]{7})(?:\\s*(#)\\s*([0-9]{1,10}))?$", "0$1$2$3");
		if (b && fax && (b = val.matches("^(?:\\+?886|0)?(?:[29][0-9]{8}|4[0-9]{7,8}|[3-8][0-9]{7})$"))) val = val.replaceFirst("^(?:\\+?886|0)?([29][0-9]{8}|4[0-9]{7,8}|[3-8][0-9]{7})$", "0$1");
		if (b && predicate != null) b = predicate.test(val);
		if (b && function != null) val = function.apply(val);
		return b ? val : null;
	}

	private boolean number = false;

	public Validator number() {
		number = true;
		return this;
	}

	private boolean date = false;

	public Validator date() {
		date = true;
		return this;
	}

	private boolean email = false;

	public Validator email() {
		email = true;
		return this;
	}

	private boolean mobile = false;

	public Validator mobile() {
		mobile = true;
		val = val.replaceAll("-", StringUtils.EMPTY);
		return this;
	}

	private boolean tel = false;

	public Validator tel() {
		tel = true;
		val = val.replaceAll("-", StringUtils.EMPTY);
		return this;
	}

	private boolean fax = false;

	public Validator fax() {
		fax = true;
		val = val.replaceAll("-", StringUtils.EMPTY);
		return this;
	}

	private boolean identityNumber = false;

	public Validator identityNumber() {
		identityNumber = true;
		return this;
	}

	private boolean invoiceNumber = false;

	public Validator invoiceNumber() {
		invoiceNumber = true;
		return this;
	}

	private boolean length = false;
	private int min = -1, max = -1;

	public Validator length(int min, int max) {
		length = true;
		this.min = min;
		this.max = max;
		return this;
	}

	private boolean regex = false;
	private String pattern = null;

	public Validator regex(String regex) {
		this.regex = true;
		this.pattern = regex;
		return this;
	}

	private String $val = null;
	private boolean equalTo = false;

	public Validator equalTo(String val) {
		this.equalTo = true;
		this.$val = val;
		return this;
	}

	private String not$val = null;
	private boolean notEqualTo = false;

	public Validator notEqualTo(String val) {
		this.notEqualTo = true;
		this.not$val = val;
		return this;
	}

	private boolean equalsIgnoreCase = false;

	public Validator equalsIgnoreCase(String val) {
		this.equalsIgnoreCase = true;
		this.$val = val;
		return this;
	}

	private boolean notEqualsIgnoreCase = false;

	public Validator notEqualsIgnoreCase(String val) {
		this.notEqualsIgnoreCase = true;
		this.not$val = val;
		return this;
	}

	private Predicate<String> predicate;

	public Validator predicate(Predicate<String> predicate) {
		this.predicate = predicate;
		return this;
	}

	private Function<String, String> function;

	public Validator function(Function<String, String> function) {
		this.function = function;
		return this;
	}
}