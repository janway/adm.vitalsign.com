package com.eapollo;

import java.util.regex.Pattern;

public class ValidateUtil {

	public static final int[] V = { 1, 10, 19, 28, 37, 46, 55, 64, 39, 73, 82, 2, 11, 20, 48, 29, 38, 47, 56, 65, 74, 83, 21, 3, 30, 12 };

	// A123456789 -> O
	// A123456788 -> X
	public static final boolean identityNumber(CharSequence n) {
		return n != null && Pattern.matches("(?i)^[A-Z][01][0-9]{8}$", n) ? 10 - ((V[(n.charAt(0) & 0x5f) - 65] + ((n.charAt(1) - 48) << 3) + ((n.charAt(2) - 48) * 7) + ((n.charAt(3) - 48) * 6) + ((n.charAt(4) - 48) * 5) + ((n.charAt(5) - 48) << 2) + ((n.charAt(6) - 48) * 3) + ((n.charAt(7) - 48) << 1) + (n.charAt(8) - 48)) % 10) == n.charAt(9) - 48 : false;
	}

	// 70578645 -> O
	// 70578646 -> X
	public static final boolean invoiceNumber(CharSequence n) {
		int y;
		return n != null && Pattern.matches("^[0-9]{8}$", n) ? ((n.charAt(0) - 48) + ((y = (n.charAt(1) - 48) << 1) < 10 ? y : (y / 10) + (y % 10)) + (n.charAt(2) - 48) + ((y = (n.charAt(3) - 48) << 1) < 10 ? y : (y / 10) + (y % 10)) + (n.charAt(4) - 48) + ((y = (n.charAt(5) - 48) << 1) < 10 ? y : (y / 10) + (y % 10)) + (n.charAt(7) - 48) + (n.charAt(6) == '7' ? 1
				: (y = (n.charAt(6) - 48) << 2) < 10 ? y : (y / 10) + (y % 10))) % 10 == 0 : false;
	}

	// 2000-01-01 -> O
	// 2000/02/29 -> O
	// 2000-02/29 -> O
	// 2000/02-29 -> O
	// 2100-02-29 -> X
	// 2000-12-39 -> X
	public static final boolean date(CharSequence s) {
		if (s == null || !Pattern.matches("^[0-9]+[-/](?:(?:0?[13578]|1[02])[-/](?:0?[1-9]|[12][0-9]|3[01])|0?2[-/](?:0?[1-9]|[12][0-9])|(?:0?[469]|11)[-/](?:0?[1-9]|[12][0-9]|30))$", s)) return false;
		if (!Pattern.matches("^[0-9]+[-/]0?2[-/]29$", s)) return true;
		int y = Integer.parseInt(Utils.replace(s, "^([0-9]+)[-/].*$", "$1").toString());
		return y % 400 == 0 || (y % 4 == 0 && y % 100 != 0);
	}

	// 00:00:00 -> O
	// 0:0:0 -> O
	// 1:1:1 -> O
	// 23:59:59 -> O
	// 24:60:60 -> X
	public static final boolean time(CharSequence s) {
		return s != null && Pattern.matches("^(?:0?[0-9]|1[0-9]|2[0-3])(?::(?:0?[0-9]|[1-4][0-9]|5[1-9])){2}$", s);
	}

	public static final boolean datetime(CharSequence s) {
		return datetime(s, " ");
	}

	public static final boolean datetime(CharSequence s, String separator) {
		if (s != null) {
			String[] ss = s.toString().split(separator, 2);
			return date(ss[0]) && time(ss[1]);
		}
		return false;
	}

	public static final boolean email(CharSequence s) {
		return s != null && Pattern.matches("(?i)^[_A-Z\\d-\\+]+(\\.[_A-Z\\d-]+)*@[A-Z\\d-]+(\\.[A-Z\\d-]+)*(\\.[A-Z]{2,})$", s);
	}
}