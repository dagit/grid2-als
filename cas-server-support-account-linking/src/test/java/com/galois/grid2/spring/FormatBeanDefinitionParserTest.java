package com.galois.grid2.spring;

import java.util.Collections;
import java.util.Set;

import org.jasig.services.persondir.support.MessageFormatPersonAttributeDao;
import org.jasig.services.persondir.support.MessageFormatPersonAttributeDao.FormatAttribute;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.galois.grid2.spring.FormatStringParser.MessageFormatBuilder;

public class FormatBeanDefinitionParserTest extends
		BeanDefinitionParserTestCase {
	private ApplicationContext context;

	@Override
	public void setUp() {
		this.context = new ClassPathXmlApplicationContext(
				"formatBeanDefinitions.xml",
				FormatBeanDefinitionParserTest.class);
	}

	/**
	 * Test that XML definitions of invalid beans fail as appropriate.
	 */
	public void testInvalidBeans() {
		assertFailedParse("invalidFormat1.xml");
		assertFailedParse("invalidFormat2.xml");
	}

	/**
	 * Test that a format tag constructs a DAO with the expected attribute when
	 * the format string has no interpolation requirements.
	 */
	public void testNoFormatVariables() {
		MessageFormatPersonAttributeDao dao = (MessageFormatPersonAttributeDao) this.context
				.getBean("noVariables");
		Set<FormatAttribute> actual = dao.getFormatAttributes();

		FormatAttribute attr = new FormatAttribute(
				Collections.singleton("outNoVars"), "plain",
				Collections.<String> emptyList());
		Set<FormatAttribute> expected = Collections.singleton(attr);

		assertEquals(expected, actual);
	}

	/**
	 * A somewhat weak test, we ensure that the bean parser's functionality is
	 * preserved (mostly involves repeating the parser's implementation).
	 */
	public void testWithFormatVariables() {
		MessageFormatPersonAttributeDao dao = (MessageFormatPersonAttributeDao) this.context
				.getBean("withVariables");
		Set<FormatAttribute> actual = dao.getFormatAttributes();

		String fmt = "{in1}foo{in2}";

		MessageFormatBuilder b = new MessageFormatBuilder();
		FormatStringParser.parse(fmt, b);

		FormatAttribute attr = new FormatAttribute(
				Collections.singleton("outWithVars"), b.getFormatString(),
				b.getVars());
		Set<FormatAttribute> expected = Collections.singleton(attr);

		assertEquals(expected, actual);
	}
}
