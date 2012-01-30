package com.galois.grid2.store;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.galois.grid2.GenericRemoteName;

public class HibernateAccountLinkingStorageTest extends
		MutableAccountLinkingStorageTest<HibernateAccountLinkingStorage> {

	public HibernateAccountLinkingStorageTest(String name) {
		super(name);
	}

	@Override
	public HibernateAccountLinkingStorage buildStore() {
		// Configure hibernate to use an in-memory database (HSQLDB) for
		// testing.
		Properties props = new Properties();
		props.setProperty("hibernate.connection.driver_class",
				"org.hsqldb.jdbcDriver");
		props.setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:testing");
		props.setProperty("hibernate.connection.username", "sa");
		props.setProperty("hibernate.connection.password", "");
		props.setProperty("hibernate.connection.pool_size", "1");
		props.setProperty("hibernate.dialect",
				"org.hibernate.dialect.HSQLDialect");
		props.setProperty("hibernate.current_session_context_class", "thread");
		props.setProperty("hibernate.cache.provider_class",
				"org.hibernate.cache.NoCacheProvider");
		props.setProperty("hibernate.show_sql", "true");
		props.setProperty("hibernate.hbm2ddl.auto", "update");

		EntityManagerFactory emf = Persistence.createEntityManagerFactory(
				"Grid2Persistence", props);
		return new HibernateAccountLinkingStorage(emf, new GenericRemoteName.ConverterSet());
	}

	@Override
	public void shutdownStore(HibernateAccountLinkingStorage store) {
		EntityManager em = store.entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("SHUTDOWN").executeUpdate();
	}
}
