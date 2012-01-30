package com.galois.grid2.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class GridNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("format", new FormatBeanDefinitionParser());
		registerBeanDefinitionParser("convertAttribute",
				new AttributeTranslationBeanParser());
		registerBeanDefinitionParser("pipeline",
				new PipelineDaoBeanParser());
		registerBeanDefinitionParser("merge",
				new MergeDaoBeanParser());
	}
}
