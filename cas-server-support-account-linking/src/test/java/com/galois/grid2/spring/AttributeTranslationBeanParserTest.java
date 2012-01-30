package com.galois.grid2.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.galois.grid2.persondir.AttributeTranslatingDao;

/**
 * The class <code>AttributeTranslationBeanParserTest</code> contains tests for
 * the class {@link <code>AttributeTranslationBeanParser</code>}
 * 
 * @pattern JUnit Test Case
 * 
 * @generatedBy CodePro at 1/6/12 2:57 PM
 * 
 * @author cygnus
 * 
 * @version $Revision$
 */
public class AttributeTranslationBeanParserTest extends
		BeanDefinitionParserTestCase {
	private ApplicationContext context;

	@Override
	public void setUp() {
		this.context = new ClassPathXmlApplicationContext(
				"attributeTranslationBeanDefinitions.xml",
				AttributeTranslationBeanParserTest.class);
	}

	public void testWithNameOut() {
		AttributeTranslatingDao dao = (AttributeTranslatingDao) this.context
				.getBean("withNameOut");

		assertEquals("username", dao.getNameIn());
		assertEquals("uid", dao.getNameOut());
		assertTrue(dao.getValueTransform().isEmpty());
	}

	public void testWithoutNameOut() {
		AttributeTranslatingDao dao = (AttributeTranslatingDao) this.context
				.getBean("withoutNameOut");
		
		assertEquals("username", dao.getNameIn());
		assertEquals("username", dao.getNameOut());
		assertTrue(dao.getValueTransform().isEmpty());
	}

	public void testValueTranslation() {
		AttributeTranslatingDao dao = (AttributeTranslatingDao) this.context
				.getBean("withConversion");
		
		Map<String, String> expectedTranslations = new HashMap<String, String>();
		expectedTranslations.put("foo", "bar");
		expectedTranslations.put("baz", "stuff");
		
		assertEquals("username", dao.getNameIn());
		assertEquals("uid", dao.getNameOut());
		assertEquals(expectedTranslations, dao.getValueTransform());
	}

	public void testInvalidConfiguration() {
		assertFailedParse("invalidAttributeTranslation1.xml");
		assertFailedParse("invalidAttributeTranslation2.xml");
		assertFailedParse("invalidAttributeTranslation3.xml");
	}
}
