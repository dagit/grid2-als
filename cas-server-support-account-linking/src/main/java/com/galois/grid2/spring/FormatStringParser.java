package com.galois.grid2.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatStringParser {
	private final static String FORMAT_FIELD_REGEX = "\\$\\{([^\\{\\}]+)\\}";

	public interface Handler {
		public void addLiteral(String lit);

		public void addVar(String varName);
	}

	public static void parse(String formatString, Handler handler) {
		// Matching state
		Pattern p = Pattern.compile(FORMAT_FIELD_REGEX);
		Matcher m = p.matcher(formatString);
		// The location of the last unprocessed text
		int last = 0;

		// Advance the matcher to the next match, terminating when no match
		// is found.
		while (m.find()) {
			String varName = m.group(1);
			// The text that we skipped over to find this match.
			String intervening = formatString.substring(last, m.start());
			handler.addLiteral(intervening);
			handler.addVar(varName);

			// Remember where this match ended.
			last = m.end();
		}

		// Add any text past the end of the last match to the literals list.
		String finalText = formatString.substring(last);
		handler.addLiteral(finalText);
	}

	public static final class MessageFormatBuilder implements
			FormatStringParser.Handler {
		private final List<String> vars;
		private final StringBuffer buf;

		public MessageFormatBuilder() {
			this.vars = new ArrayList<String>();
			this.buf = new StringBuffer();
		}

		public final void addLiteral(final String lit) {
			boolean inQuote = false;
			for (final char c : lit.toCharArray()) {
				if (c == '\'') {
					buf.append('\'');
				} else {
					final boolean special = c == '}' || c == '{';
					final boolean startingQuote = !inQuote && special;
					final boolean endingQuote = inQuote && !special;
					if (startingQuote || endingQuote) {
						buf.append('\'');
						inQuote = !inQuote;
					}
				}
				buf.append(c);
			}
			if (inQuote) {
				buf.append('\'');
			}
		}

		public void addVar(String varName) {
			buf.append("{");
			buf.append(vars.size());
			buf.append("}");
			vars.add(varName);
		}

		public String getFormatString() {
			return buf.toString();
		}

		public List<String> getVars() {
			return vars;
		}
	}
}