package com.galois.grid2.spring;

import java.util.ArrayList;
import java.util.List;

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.MergingPersonAttributeDaoImpl;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class MergeDaoBeanParser extends AbstractSingleBeanDefinitionParser {

	public static final class DaoFactory extends
			AbstractFactoryBean<MergingPersonAttributeDaoImpl> {
		private List<?> sourceList;

		@Override
		public Class<MergingPersonAttributeDaoImpl> getObjectType() {
			return MergingPersonAttributeDaoImpl.class;
		}

		@Override
		protected MergingPersonAttributeDaoImpl createInstance()
				throws Exception {
			List<IPersonAttributeDao> daos = new ArrayList<IPersonAttributeDao>();
			TypeConverter converter = getBeanTypeConverter();
			for (Object elem : this.sourceList) {
				daos.add(converter.convertIfNecessary(elem, IPersonAttributeDao.class));
			}
			MergingPersonAttributeDaoImpl dao = new MergingPersonAttributeDaoImpl();
			dao.setPersonAttributeDaos(daos);
			return dao;
		}

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
