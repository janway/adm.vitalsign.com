package biosensetek;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class Min {

	private File to = null;

	public void setTo(File to) {
		this.to = to;
	}

	private ArrayList<Src> srcs = new ArrayList<>();

	public Src createSrc() {
		Src src = new Src();
		srcs.add(src);
		return src;
	}

	public void execute() throws IOException {
		if (to == null || srcs.size() == 0) return;
		// if (to.exists()) {
		// boolean go = false;
		// long lastModified = to.lastModified();
		// for (Src src : srcs) {
		// if (src.from == null) continue;
		// if (src.from.lastModified() > lastModified) {
		// go = true;
		// break;
		// }
		// }
		// if (!go) return;
		// }
		File tmp = File.createTempFile("min.", ".tmp");
		System.out.println("min");
		try (FileOutputStream output = new FileOutputStream(tmp)) {
			for (Src src : srcs) {
				if (src.from == null) continue;
				System.out.println("\t" + src.from.getAbsolutePath());
				if (StringUtils.endsWithAny(src.from.getName(), ".js", ".css")) {
					IOUtils.write(Utils.replace(FileUtils.readFileToString(src.from, "UTF-8"), "(?ms)/\\*.*?\\*/", StringUtils.EMPTY), output);
				} else {
					FileUtils.copyFile(src.from, output);
				}
				output.flush();
			}
		}
		System.out.println(" >> " + to.getAbsolutePath());
		if (!to.getParentFile().exists()) to.getParentFile().mkdirs();
		try (FileReader reader = new FileReader(tmp)) {
			try (FileWriter writer = new FileWriter(to)) {
				if (this.to.getName().endsWith(".js")) {
					Minifier.js(reader, writer);
				} else {
					Minifier.css(reader, writer);
				}
			}
		}
		tmp.delete();
	}

	public static final class Src {
		private File from = null;

		public void setFrom(File from) {
			this.from = from;
		}
	}
}