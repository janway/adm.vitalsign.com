package biosensetek;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.input.CharSequenceReader;
import org.apache.commons.io.output.StringBuilderWriter;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class Minifier {

	public static <T extends Appendable> T js(CharSequence input, T output) {
		StringBuilder out = new StringBuilder();
		js(new CharSequenceReader(input), new StringBuilderWriter(out));
		try {
			output.append(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output;
	}

	public static <T extends Writer> T js(Reader in, T out) {
		try {
			JavaScriptCompressor compressor = new JavaScriptCompressor(in, new $());
			compressor.compress(out, -1, true, false, false, false);
		} catch (EvaluatorException | IOException e) {
			e.printStackTrace();
		}
		return out;
	}

	public static <T extends Appendable> T css(CharSequence input, T output) {
		StringBuilder out = new StringBuilder();
		css(new CharSequenceReader(input), new StringBuilderWriter(out));
		try {
			output.append(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output;
	}

	public static <T extends Writer> T css(Reader in, T out) {
		try {
			CssCompressor compressor = new CssCompressor(in);
			compressor.compress(out, -1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out;
	}

	private static class $ implements ErrorReporter {
		public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
		}

		public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
		}

		public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
			return new EvaluatorException(message);
		}
	}
}