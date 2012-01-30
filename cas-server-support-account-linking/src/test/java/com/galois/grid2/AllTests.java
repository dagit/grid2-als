package com.galois.grid2;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.galois.grid2.actions.MapUnmappedRemoteNamesActionTest;
import com.galois.grid2.actions.TicketRegistryAuthenticationStoreTest;
import com.galois.grid2.persondir.AttributeTranslatingDaoTest;
import com.galois.grid2.persondir.PipelinePersonAttributeDaoTest;
import com.galois.grid2.persondir.UniquifyingMultivaluedAttributeMergerTest;
import com.galois.grid2.spring.AttributeTranslationBeanParserTest;
import com.galois.grid2.spring.FormatBeanDefinitionParserTest;
import com.galois.grid2.spring.FormatStringParserTest;
import com.galois.grid2.spring.MergeDaoBeanParserTest;
import com.galois.grid2.spring.PipelineDaoBeanParserTest;
import com.galois.grid2.store.AttributeEncodingTest;
import com.galois.grid2.store.AutomaticLinkingStorageTest;
import com.galois.grid2.store.HibernateAccountLinkingStorageTest;
import com.galois.grid2.store.MemoryAccountLinkingStorageTest;

public class AllTests {

	public static Test suite() {
		final TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(AccountLinkingCredentialsToPrincipalResolverTest.class);
		suite.addTestSuite(AccountLinkingDaoTest.class);
		suite.addTestSuite(AutomaticLinkingStorageTest.class);
		suite.addTestSuite(MemoryAccountLinkingStorageTest.class);
		suite.addTestSuite(HibernateAccountLinkingStorageTest.class);
		suite.addTestSuite(AttributeEncodingTest.class);
		suite.addTestSuite(UsernamePasswordCredentialsConverterTest.class);
		suite.addTestSuite(X509CertificateCredentialsConverterTest.class);
		suite.addTestSuite(MapUnmappedRemoteNamesActionTest.class);
		suite.addTestSuite(MapRemoteNameFactoryTest.class);
		suite.addTestSuite(UsernameRemoteNameTest.class);
		suite.addTestSuite(X509RemoteNameTest.class);
		suite.addTestSuite(FormatStringParserTest.class);
		suite.addTestSuite(AttributeTranslatingDaoTest.class);
		suite.addTestSuite(TicketRegistryAuthenticationStoreTest.class);
		suite.addTestSuite(UniquifyingMultivaluedAttributeMergerTest.class);
		suite.addTestSuite(PipelinePersonAttributeDaoTest.class);
		suite.addTestSuite(PipelineDaoBeanParserTest.class);
		suite.addTestSuite(MergeDaoBeanParserTest.class);
		suite.addTestSuite(FormatBeanDefinitionParserTest.class);
		suite.addTestSuite(AttributeTranslationBeanParserTest.class);
		//$JUnit-END$
		return suite;
	}

}
