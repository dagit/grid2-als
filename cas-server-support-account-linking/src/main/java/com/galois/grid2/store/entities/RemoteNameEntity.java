package com.galois.grid2.store.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;

import org.hibernate.annotations.Index;

@Entity
@org.hibernate.annotations.Table(appliesTo = "RemoteNameEntity", indexes = {
		@Index(name = "localName_id_index", columnNames = { "localName_id" }),
		@Index(name = "attr_index", columnNames = { "attrHash", "namespace_id" }),
		@Index(name = "timestamp_index", columnNames = { "created" }) })
public class RemoteNameEntity {
	public static final int MAX_ATTR_STR_LEN = 8192;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(nullable = false)
	private Namespace namespace;

	@Column(length = MAX_ATTR_STR_LEN, nullable = false)
	private String attrString;

	@SuppressWarnings("unused")
	@Column(nullable = false)
	private Date created;

	@SuppressWarnings("unused")
	@PrePersist
	private void onCreate() {
		created = new Date();
	}

	/**
	 * SHA-256 hash of attrString used for lookup optimization.
	 */
	@Column(length = 64, nullable = false)
	private String attrHash;

	@ManyToOne
	@JoinColumn(nullable = false)
	private LocalName localName;

	public String getAttrString() {
		return attrString;
	}

	public void setAttrString(String attrString) {
		this.attrString = attrString;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	@SuppressWarnings("unused")
	private void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the namespace
	 */
	public Namespace getNamespace() {
		return namespace;
	}

	/**
	 * @param namespace
	 *            the namespace to set
	 */
	public void setNamespace(Namespace namespace) {
		this.namespace = namespace;
	}

	/**
	 * @return the localName
	 */
	public LocalName getLocalName() {
		return localName;
	}

	/**
	 * @param localName
	 *            the localName to set
	 */
	public void setLocalName(LocalName localName) {
		this.localName = localName;
	}

	/**
	 * @return the attrHash
	 */
	public String getAttrHash() {
		return attrHash;
	}

	/**
	 * @param attrHash
	 *            the attrHash to set
	 */
	public void setAttrHash(String attrHash) {
		this.attrHash = attrHash;
	}
}
