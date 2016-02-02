package biosensetek;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class FasterSync {
	private static final class Sync implements Runnable {
		private String url;
		private Gson gson;
		private boolean run;

		public Sync url(String url) {
			this.url = url;
			return this;
		}

		public Sync gson(Gson gson) {
			this.gson = gson;
			return this;
		}

		public Sync run(boolean run) {
			this.run = run;
			return this;
		}

		@Override
		public void run() {
			while (run) {
				try (JsonReader in = new JsonReader(new InputStreamReader(new URL(url).openStream()))) {
					JsonObject result = gson.fromJson(in, JsonObject.class);
					System.out.println(result);
					if (!result.has("checkout") || result.get("checkout").getAsInt() == 0) break;
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static final void main(String[] args) {
		// http://127.0.0.1:8080/fun/purge-solr.jsp
		/*
		@formatter:off
		INSERT IGNORE INTO`sync`(`type`,`uid`,`ck`)
		SELECT 0,uid,'dev' FROM shop
		UNION
		SELECT 1,uid,'dev' FROM item;
		@formatter:on
		*/
		Gson gson = new GsonBuilder().create();
		new Sync().url("http://127.0.0.1:8080/fun/shop2solr.jsp").gson(gson).run(true).run();
		new Sync().url("http://127.0.0.1:8080/fun/item2solr.jsp").gson(gson).run(true).run();
	}
}