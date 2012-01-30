package com.galois.grid2.store.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;

import org.hibernate.annotations.Index;

@Entity
public class LocalName {
	@Id
	@GeneratedValue
	private Long id;
	
	@Column(unique = true, nullable = false)
	@Index(name = "name_index")
	private String name;

	@SuppressWarnings("unused")
	@Column(nullable = false)
	private Date created;

	@SuppressWarnings("unused")
	@PrePersist
	private void onCreate() {
		created = new Date();
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
