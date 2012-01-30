package com.galois.grid2.spring;

import java.util.List;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.MessageFormatPersonAttributeDao;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.galois.grid2.persondir.AttributeTranslatingDao;
import com.galois.grid2.persondir.PipelinePersonAttributeDao;

/**
 * The class <code>PipelineDaoBeanParserTest</code> contains tests for the class
 * {@link <code>PipelineDaoBeanParser</code>}
 * 
 * @pattern JUnit Test Case
 * 
 * @generatedBy CodePro at 1/6/12 2:44 PM
 * 
 * @author cygnus
 * 
 * @version $Revision$
 */
public class PipelineDaoBeanParserTest extends BeanDefinitionParserTestCase {
	private ApplicationContext context;

	@Override
	public void setUp() {
		this.context = new ClassPathXmlApplicationContext(
				"pipelineBeanDefinitions.xml", PipelineDaoBeanParserTest.class);
	}

	public void testValidPipelineDao() {
		PipelinePersonAttributeDao dao = (PipelinePersonAttributeDao) this.context
				.getBean("validPipeline");

		List<IPersonAttributeDao> actualDaos = dao.getDaos();
		assertEquals(2, actualDaos.size());

		assertTrue(actualDaos.get(0) instanceof MessageFormatPersonAttributeDao);
		assertTrue(actualDaos.get(1) instanceof AttributeTranslatingDao);
	}

	public void testSingletonPipelineDao() {
		PipelinePersonAttributeDao dao = (PipelinePersonAttributeDao) this.context
				.getBean("singletonPipeline");

		List<IPersonAttributeDao> actualDaos = dao.getDaos();
		assertEquals(1, actualDaos.size());

		assertTrue(actualDaos.get(0) instanceof MessageFormatPersonAttributeDao);
	}

	public void testInvalidPipeline() {
		assertFailedParse("invalidPipeline.xml");
	}
}