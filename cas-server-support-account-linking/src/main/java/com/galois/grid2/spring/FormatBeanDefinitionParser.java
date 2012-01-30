package com.galois.grid2.spring;

import java.util.Collections;

import org.jasig.services.persondir.support.MessageFormatPersonAttributeDao;
import org.jasig.services.persondir.support.MessageFormatPersonAttributeDao.FormatAttribute;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

import com.galois.grid2.spring.FormatStringParser.MessageFormatBuilder;

public class FormatBeanDefinitionParser extends
		AbstractSingleBeanDefinitionParser {

	@SuppressWarnings("rawtypes")
	protected Class getBeanClass(Element element) {
		return MessageFormatPersonAttributeDao.class;
	}

	protected void doParse(Element element, BeanDefinitionBuilder bean) {
		String outputName = element.getAttribute("nameOut");
		String formatString = element.getAttribute("format");
		
		if (outputName == null) {
			throw new RuntimeException("Failed to find \"nameOut\" value!");
		}
		
		if (formatString == null) {
			throw new RuntimeException("Failed to find \"format\" value!");
		}

		final MessageFormatBuilder builder = new MessageFormatBuilder();
		FormatStringParser.parse(formatString, builder);
		FormatAttribute attr = new FormatAttribute(
				Collections.singleton(outputName), builder.getFormatString(),
				builder.getVars());
		bean.addPropertyValue("formatAttributes", Collections.singleton(attr));
	}

}
