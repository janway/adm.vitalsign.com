package com.biosensetek;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

public class run {
	public static void main(String[] args) {
		for (String arg : args) {
			arg = StringUtils.trimToNull(arg);
			if (arg == null) continue;
			try {
				HttpURLConnection conn = (HttpURLConnection) new URL(arg).openConnection();
				conn.setReadTimeout((int) TimeUnit.SECONDS.toMillis(50));
				conn.setInstanceFollowRedirects(false);
				conn.setUseCaches(false);
				conn.setRequestProperty("User-Agent", "com.biosensetek.run");
				conn.setRequestProperty("Connection", "close");
				conn.connect();
				try (InputStream in = conn.getInputStream()) {
					byte[] buf = new byte[4096];
					while (in.read(buf) != -1) {}
				} catch (Throwable e) {}
				conn.disconnect();
			} catch (Throwable e) {}
		}
	}
}