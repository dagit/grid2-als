package com.galois.grid2.spring;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.galois.grid2.spring.FormatStringParser.MessageFormatBuilder;

public class FormatStringParserTest extends TestCase {

	private static final class NoVarsHandler implements
			FormatStringParser.Handler {
		String lastLit = null;

		public void addVar(String varName) {
			fail("Got unexpected var: " + varName);
		}

		public void addLiteral(String lit) {
			assertTrue("Unexpectedly called addLiteral more than once!",
					lastLit == null);
			lastLit = lit;
		}

		public String getLastFinish() {
			return lastLit;
		}
	}

	protected void assertNoVars(String formatString) {
		NoVarsHandler handler = new NoVarsHandler();
		FormatStringParser.parse(formatString, handler);
		assertEquals(formatString, handler.getLastFinish());
	}

	public void testParse() {
		assertNoVars("foo");
		assertNoVars("{0}foo");
		assertNoVars("$foo");
		assertNoVars("${foo");
	}

	public void testBuildFormatAttribute1() {
		assertMessageFormatOK("{foo}", "'{'foo'}'",
				Collections.<String, String> emptyMap(), "{foo}");

		final Map<String, String> subst = new LinkedHashMap<String, String>();
		subst.put("var", "baz");
		assertMessageFormatOK("before ${var} after", "before {0} after", subst,
				"before baz after");
		assertMessageFormatOK("before ' ${var} after", "before '' {0} after",
				subst, "before ' baz after");
		assertMessageFormatOK("before '' ${var} after",
				"before '''' {0} after", subst, "before '' baz after");
		assertMessageFormatOK("before {} ${var} after",
				"before '{}' {0} after", subst, "before {} baz after");
		assertMessageFormatOK("before {{}} ${var} after",
				"before '{{}}' {0} after", subst, "before {{}} baz after");
		assertMessageFormatOK("before { ${var} after", "before '{' {0} after",
				subst, "before { baz after");
		assertMessageFormatOK("before {'} ${var} after",
				"before '{''}' {0} after", subst, "before {'} baz after");
		assertMessageFormatOK("before '{'}' ${var} after",
				"before '''{''}''' {0} after", subst, "before '{'}' baz after");
		assertMessageFormatOK("before '{''}' ${var} after",
				"before '''{''''}''' {0} after", subst,
				"before '{''}' baz after");

		subst.put("var2", "bar");
		assertMessageFormatOK("${var}${var2}", "{0}{1}", subst, "bazbar");

		subst.clear();
		subst.put("var2", "bar");
		subst.put("var", "baz");
		assertMessageFormatOK("${var2}${var}", "{0}{1}", subst, "barbaz");
		assertMessageFormatOK("-${var2}-${var}-", "-{0}-{1}-", subst,
				"-bar-baz-");
	}

	private void assertMessageFormatOK(final String formatString,
			final String msgFormatString, final Map<String, String> subst,
			final String expected) {
		MessageFormatBuilder builder = new FormatStringParser.MessageFormatBuilder();
		FormatStringParser.parse(formatString, builder);

		assertEquals(new ArrayList<String>(subst.keySet()), builder.getVars());
		assertEquals(msgFormatString, builder.getFormatString());
		assertEquals(expected, MessageFormat.format(builder.getFormatString(),
				subst.values().toArray()));
	}

}
