package com.biosensetek;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.eapollo.DBUtils;

public class FixStaticWowKool {
	public static final String enc(String url) {
		StringBuilder buf = new StringBuilder(url.length());
		char c;
		for (int i = 0; i < url.length(); i++) {
			c = url.charAt(i);
			if (c == ' ') {
				buf.append("%20");
			} else if (c > 256) {
				try {
					buf.append(URLEncoder.encode(url.substring(i, i + 1), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					buf.append(c);
				}
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}

	public static final void main(String[] args) {
		File dir = new File("/home/eapollo/Public"), dst;
		File xxx = new File(dir, "map");
		File tmp;
		StringBuilder str = new StringBuilder("http:");
		HashMap<String, String> map = null;
		HashSet<String> set = new HashSet<>();
		if (xxx.isFile()) {
			try (InputStream in = new FileInputStream(xxx)) {
				map = SerializationUtils.deserialize(in);
			} catch (Throwable e) {}
		}
		if (map == null) map = new HashMap<>();
		String url, md5, b64, src;
		//
		DBUtils.init(new File("web/WEB-INF", "ds"));
		try (Connection conn = DBUtils.conn("main")) {
			System.out.println(conn);
			try (Statement stat = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
				try (ResultSet rs = stat.executeQuery("SELECT * FROM item WHERE intro LIKE '%static.wowkool.com%'")) {
					while (rs.next()) {
						Document intro = Jsoup.parseBodyFragment(rs.getString("intro"));
						Elements imgs = intro.body().select("img[src^=//static.wowkool.com]");
						int b = 0;
						for (Element img : imgs) {
							set.add(src = img.attr("src"));
							str.setLength(5);
							url = enc(str.append(src).toString());
							md5 = Base64.encodeBase64URLSafeString(DigestUtils.md5(url));
							if (!map.containsKey(md5)) {
								b64 = null;
								tmp = null;
								try (InputStream in = new URL(url).openStream()) {
									try (OutputStream out = new FileOutputStream(tmp = File.createTempFile("static.", ".wowkool"))) {
										MessageDigest md = MessageDigest.getInstance("md5");
										int len;
										byte[] buf = new byte[4096];
										while ((len = in.read(buf)) != -1) {
											out.write(buf, 0, len);
											md.update(buf, 0, len);
										}
										map.put(md5, b64 = Base64.encodeBase64URLSafeString(md.digest()));
									}
								} catch (Throwable e) {
									System.err.println(e.getMessage());
								}
								if (b64 != null) {
									dst = new File(dir, b64.replaceAll("^(.{2})(.{2})(.+)$", "$1/$2/$3"));
									if (!dst.getParentFile().exists()) dst.getParentFile().mkdirs();
									int len;
									byte[] buf = new byte[8192];
									try (InputStream in = new FileInputStream(tmp)) {
										try (OutputStream out = new FileOutputStream(dst)) {
											while ((len = in.read(buf)) != -1) {
												out.write(buf, 0, len);
											}
										}
									} catch (Throwable e) {}
									tmp.delete();
								}
								//
								try (OutputStream out = new FileOutputStream(xxx)) {
									SerializationUtils.serialize(map, out);
								} catch (Throwable e) {}
							}
							if (map.containsKey(md5)) {
								img.attr("ssrc", src);
								img.attr("src", map.get(md5));
								b++;
							}
						}
						if (b > 0) {
							rs.updateString("intro", intro.body().html());
							rs.updateRow();
						}
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		DBUtils.destroy();
	}
}