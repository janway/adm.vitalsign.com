package biosensetek;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class JspOptimizedImpot {
	public static void main(String[] args) {
		File dir = new File("web");
//		visit(dir, file -> file.getName().matches("^shop(?:-(?:info|inquiry|note))?\\.html$") && "web".equals(file.getParentFile().getName()));
		visit(dir, file -> file.getName().matches("^.+\\.(html|jsp)$"));
	}

	private static Pattern PAGE_DIRECTIVE = Pattern.compile("(?ims)\\s*<%\\s*@\\s*page(.+?)%>\\s*");
	private static Pattern ATTRIBUTE = Pattern.compile("(?ms)(\\S+?)\\s*=\\s*\"\\s*(.+?)\\s*\"");

	public static void visit(File file, FileFilter filter) {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					visit(child, filter);
				}
			}
		} else if (file.isFile() && filter.accept(file)) {
			handle(file);
		}
	}

	public static void handle(File file) {
		try {
			TreeSet<String> imports = new TreeSet<>();
			String data = FileUtils.readFileToString(file);
			String k, v;
			Matcher m0, m1;
			boolean x;
			StringBuffer sb = new StringBuffer();
			//
			m0 = PAGE_DIRECTIVE.matcher(data);
			while (m0.find()) {
				x = false;
				m1 = ATTRIBUTE.matcher(m0.group(1));
				while (m1.find()) {
					k = m1.group(1);
					v = m1.group(2).trim();
					if ("import".equals(k)) {
						if (v.indexOf(',') > 0) {
							for (String s : v.split("\\s*,\\s*")) {
								imports.add(s);
							}
						} else {
							imports.add(v);
						}
						x = true;
					}
				}
				if (x) m0.appendReplacement(sb, StringUtils.EMPTY);
			}
			m0.appendTail(sb);
			//
			LinkedHashSet<String> removes = new LinkedHashSet<>();
			imports.parallelStream().forEach(remove -> {
				String regex = "\\b" + Pattern.quote(remove.replaceFirst("^.+\\.(.+)$", "$1")) + "\\b";
				if (Pattern.compile(regex).matcher(sb).find()) return;
				removes.add(remove);
			});
			//
			sb.setLength(0);
			sb.append(StringUtils.LF).append(file.getAbsolutePath()).append(StringUtils.LF);
			if (removes.size() > 0) {
				imports.removeAll(removes);
				sb.append(StringUtils.join(removes, ",")).append(StringUtils.LF);
			}
			if (imports.size() > 0) {
				// auto optimized import
				if (imports.size() > 3) {
					Map<String, Set<String>> map = new LinkedHashMap<>();
					for (String string : imports) {
						String $package = string.replaceFirst("^(.+)\\..+$", "$1");
						if (!map.containsKey($package)) map.put($package, new LinkedHashSet<String>());
						map.get($package).add(string);
					}
					for (Set<String> value : map.values()) {
						sb.append("<%@page import=\"").append(StringUtils.join(value, ',')).append("\"%>").append(StringUtils.LF);
					}
				} else {
					sb.append("<%@page import=\"").append(StringUtils.join(imports, ',')).append("\"%>").append(StringUtils.LF);
				}
			}
			if (removes.size() > 0) System.out.println(sb);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}