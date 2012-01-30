package com.galois.grid2.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galois.grid2.MutableAccountLinkingStorage;
import com.galois.grid2.Namespaced;
import com.galois.grid2.RemoteNameFactory;
import com.galois.grid2.RemoteName;
import com.galois.grid2.RemoteNameAlreadyMappedException;
import com.galois.grid2.store.entities.LocalName;
import com.galois.grid2.store.entities.Namespace;
import com.galois.grid2.store.entities.RemoteNameEntity;

public class HibernateAccountLinkingStorage implements
		MutableAccountLinkingStorage {

	// Package-visible so tests can shut down the database.
	final EntityManagerFactory entityManagerFactory;

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private final RemoteNameFactory converters;

	public HibernateAccountLinkingStorage(final EntityManagerFactory emf,
			RemoteNameFactory converters) {
		this.entityManagerFactory = emf;
		this.converters = converters;
	}

	private abstract class TransactionManager<T> {
		T run() {
			final EntityManager em = entityManagerFactory.createEntityManager();
			final EntityTransaction txn = em.getTransaction();
			txn.begin();
			T result = null;
			try {
				result = inTransaction(em);
			} catch (RuntimeException e) {
				txn.rollback();
				throw e;
			}
			txn.commit();
			return result;
		}

		protected abstract T inTransaction(EntityManager em);
	}

	public String getLocalName(final Namespaced<RemoteName> attributes) {
		RemoteNameEntity remoteName = new TransactionManager<RemoteNameEntity>() {

			@Override
			protected RemoteNameEntity inTransaction(EntityManager em) {
				// Get the namespace associated with the specified attributes
				final Namespace ns = listToT(em
						.createQuery(
								"select n from Namespace n where n.name = :name",
								Namespace.class)
						.setParameter("name", attributes.getNamespace())
						.getResultList());

				if (ns == null) {
					return null;
				} else {
					final AttributeEncoding encoded = new AttributeEncoding(
							attributes.getValue().getAttributeMap());
					return lookupRemoteName(em, ns, encoded);
				}
			}
		}.run();

		// If we didn't find any remote names whose attributes match the ones we
		// got, then we can't return a local name.
		if (remoteName == null) {
			return null;
		} else {
			return remoteName.getLocalName().getName();
		}
	}

	public Collection<Namespaced<RemoteName>> getRemoteNames(
			final String localName) {
		final List<RemoteNameEntity> remoteNames = new TransactionManager<List<RemoteNameEntity>>() {
			protected List<RemoteNameEntity> inTransaction(
					final EntityManager em) {
				final LocalName localNameRec = listToT(em
						.createQuery("from LocalName where name = :name",
								LocalName.class)
						.setParameter("name", localName).getResultList());

				if (localNameRec == null) {
					return Collections.emptyList();
				} else {
					return em
							.createQuery(
									"from RemoteNameEntity where localName.id = :lid",
									RemoteNameEntity.class)
							.setParameter("lid", localNameRec.getId())
							.getResultList();
				}
			}
		}.run();

		final Collection<Namespaced<RemoteName>> results = new ArrayList<Namespaced<RemoteName>>();

		for (final RemoteNameEntity rn : remoteNames) {
			// Get the attributes and namespace for the remote name.
			final String ns = rn.getNamespace().getName();
			final AttributeEncoding attrEncoding = new AttributeEncoding(
					rn.getAttrString());
			try {
				final Namespaced<RemoteName> remoteName = converters
						.fromSerialized(ns, attrEncoding.decode());
				results.add(remoteName);
			} catch (IllegalArgumentException e) {
				// We continue here so that the user has a chance of completing
				// her task even in the face of internal inconsistency.
				log.error("Failed to decode value loaded from database!\n"
						+ attrEncoding.getNormalized(), e);
			} catch (InvalidNameException e) {
				log.error("Failed to decode value loaded from database!\n"
						+ attrEncoding.getNormalized(), e);
			}
		}

		return results;
	}

	public boolean linkAccounts(final String localName,
			final Namespaced<RemoteName> attrs)
			throws RemoteNameAlreadyMappedException {
		return new TransactionManager<Boolean>() {
			@Override
			protected Boolean inTransaction(EntityManager em) {
				// Create local name record if necessary
				final LocalName localNameRec = findOrCreateLocalName(localName,
						em);

				// Create remote name attributes and map the local name to them
				return mapRemoteName(attrs, localNameRec, em);
			}
		}.run();
	}

	private LocalName findOrCreateLocalName(final String localName,
			final EntityManager em) {
		LocalName localNameRec = listToT(em
				.createQuery("from LocalName where name = :name",
						LocalName.class).setParameter("name", localName)
				.getResultList());

		if (localNameRec == null) {
			localNameRec = new LocalName();
			localNameRec.setName(localName);
			em.persist(localNameRec);
		}

		return localNameRec;
	}

	/**
	 * Given a list of results, return the first element if present, null if
	 * empty, or throw an exception if the result set contains more than one
	 * element.
	 * 
	 * @param list
	 * @return
	 * @throws InvalidResultSetException
	 */
	private static <T> T listToT(final List<T> list)
			throws InvalidResultSetException {
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() == 0) {
			return null;
		} else {
			throw new InvalidResultSetException(
					"result set should have contained at most one element; contained "
							+ list.size());
		}
	}

	private boolean mapRemoteName(final Namespaced<RemoteName> attrs,
			final LocalName localName, final EntityManager em)
			throws RemoteNameAlreadyMappedException, AttributeEncodingException {

		// Find or create the namespace record
		Namespace ns = listToT(em
				.createQuery("from Namespace where name = :name",
						Namespace.class)
				.setParameter("name", attrs.getNamespace()).getResultList());

		if (ns == null) {
			ns = new Namespace();
			ns.setName(attrs.getNamespace());
			em.persist(ns);
		}
		// Compute the remote name hash for the specified remote name
		final AttributeEncoding normalizedName = new AttributeEncoding(attrs
				.getValue().getAttributeMap());

		final RemoteNameEntity existing = lookupRemoteName(em, ns,
				normalizedName);

		if (existing == null) {
			final RemoteNameEntity newRemoteName = new RemoteNameEntity();
			newRemoteName.setLocalName(localName);
			newRemoteName.setNamespace(ns);
			newRemoteName.setAttrString(normalizedName.getNormalized());
			newRemoteName.setAttrHash(normalizedName.getHexHash());
			em.persist(newRemoteName);
			return true;
		} else if (!existing.getLocalName().getId().equals(localName.getId())) {
			throw new RemoteNameAlreadyMappedException(existing.getLocalName()
					.getName());
		} else {
			return false;
		}
	}

	private RemoteNameEntity lookupRemoteName(final EntityManager em,
			final Namespace ns, final AttributeEncoding encoded) {
		return listToT(em
				.createQuery(
						"from RemoteNameEntity where attrHash = :hash and attrString = :attrString and namespace.id = :nsid",
						RemoteNameEntity.class)
				.setParameter("attrString", encoded.getNormalized())
				.setParameter("hash", encoded.getHexHash())
				.setParameter("nsid", ns.getId()).getResultList());
	}
}
