package com.galois.grid2.spring;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionStoreException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class BeanDefinitionParserTestCase extends TestCase {

	public BeanDefinitionParserTestCase() {
		super();
	}

	public BeanDefinitionParserTestCase(String name) {
		super(name);
	}

	public void assertFailedParse(String filename) {
		try {
			@SuppressWarnings("unused")
			ClassPathXmlApplicationContext unused = new ClassPathXmlApplicationContext(
					filename, this.getClass());
			fail("Expected parsing failure for file " + filename);
		} catch (BeanCreationException e) {
			// Pass.
		} catch (XmlBeanDefinitionStoreException e) {
			// Pass.
		}
	}
}