package com.galois.grid2.spring;

import java.util.List;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.MergingPersonAttributeDaoImpl;
import org.jasig.services.persondir.support.MessageFormatPersonAttributeDao;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.galois.grid2.persondir.AttributeTranslatingDao;

public class MergeDaoBeanParserTest extends BeanDefinitionParserTestCase {
	private ApplicationContext context;

	@Override
	public void setUp() {
		this.context = new ClassPathXmlApplicationContext(
				"mergeBeanDefinitions.xml", MergeDaoBeanParserTest.class);
	}

	public void testValidMergeDao() {
		MergingPersonAttributeDaoImpl dao = (MergingPersonAttributeDaoImpl) this.context
				.getBean("validMergeDao");

		List<IPersonAttributeDao> actualDaos = dao.getPersonAttributeDaos();
		assertEquals(2, actualDaos.size());

		assertTrue(actualDaos.get(0) instanceof MessageFormatPersonAttributeDao);
		assertTrue(actualDaos.get(1) instanceof AttributeTranslatingDao);
	}

	public void testSingletonMergeDao() {
		MergingPersonAttributeDaoImpl dao = (MergingPersonAttributeDaoImpl) this.context
				.getBean("singletonMerge");

		List<IPersonAttributeDao> actualDaos = dao.getPersonAttributeDaos();
		assertEquals(1, actualDaos.size());

		assertTrue(actualDaos.get(0) instanceof MessageFormatPersonAttributeDao);
	}

	public void testInvalidMerge() {
		assertFailedParse("invalidMerge.xml");
	}
}
