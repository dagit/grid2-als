package com.galois.grid2.spring;

import java.util.ArrayList;
import java.util.List;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.galois.grid2.persondir.PipelinePersonAttributeDao;

public class PipelineDaoBeanParser extends AbstractSingleBeanDefinitionParser {

	private static final class DaoFactory extends
			AbstractFactoryBean<PipelinePersonAttributeDao> {
		private List<?> sourceList;

		@Override
		public Class<PipelinePersonAttributeDao> getObjectType() {
			return PipelinePersonAttributeDao.class;
		}

		@Override
		protected PipelinePersonAttributeDao createInstance()
				throws Exception {
			List<IPersonAttributeDao> daos = new ArrayList<IPersonAttributeDao>();
			TypeConverter converter = getBeanTypeConverter();
			for (Object elem : this.sourceList) {
				daos.add(converter.convertIfNecessary(elem, IPersonAttributeDao.class));
			}
			return new PipelinePersonAttributeDao(daos);
		}

		@SuppressWarnings("unused")
		public void setSourceList(List<?> sourceList) {
			this.sourceList = sourceList;
		}
	}

	@Override
	protected Class<DaoFactory> getBeanClass(Element element) {
		return DaoFactory.class;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		List<?> parsedList = parserContext.getDelegate().parseListElement(element, builder.getRawBeanDefinition());
		builder.addPropertyValue("sourceList", parsedList);
	}
}
