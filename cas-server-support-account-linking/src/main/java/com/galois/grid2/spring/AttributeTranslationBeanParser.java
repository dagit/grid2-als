package com.galois.grid2.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.galois.grid2.persondir.AttributeTranslatingDao;

public class AttributeTranslationBeanParser extends
		AbstractSingleBeanDefinitionParser {
	@SuppressWarnings("rawtypes")
	protected Class getBeanClass(Element element) {
		return AttributeTranslatingDao.class;
	}

	protected void doParse(Element formatElement, BeanDefinitionBuilder bean) {
		String nameIn = formatElement.getAttribute("nameIn");
		String nameOut = formatElement.getAttribute("nameOut");
		
		if (nameIn.equals("")) {
			nameIn = null;
		}
		
		if (nameOut.equals("")) {
			nameOut = null;
		}
		
		bean.addConstructorArgValue(nameIn);
		bean.addConstructorArgValue(nameOut);
		
		Map<String, String> translation = new HashMap<String, String>();
		NodeList children = formatElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			short ty = child.getNodeType();
			if (ty == Node.ELEMENT_NODE) {
				Element el = (Element) child;

				final String fromVal = el.getAttribute("from");
				if (fromVal == null) {
					throw new RuntimeException("Failed to find \"from\" value!");
				}
				final String toVal = el.getAttribute("to");
				if (toVal == null) {
					throw new RuntimeException("Failed to find \"to\" value!");
				}
				translation.put(fromVal, toVal);
			}
		}
		bean.addConstructorArgValue(translation);
	}
}
