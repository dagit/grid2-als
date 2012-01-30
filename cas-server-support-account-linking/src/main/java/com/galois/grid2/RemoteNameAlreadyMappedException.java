package com.galois.grid2;

public class RemoteNameAlreadyMappedException extends RuntimeException {

	private static final long serialVersionUID = 1183525132761238967L;

	private final String existingMapping;

	public RemoteNameAlreadyMappedException(String existingMapping) {
		this.existingMapping = existingMapping;
	}

	/**
	 * @return the name that is already mapped to the remote name in the store
	 */
	public String getExistingMapping() {
		return existingMapping;
	}

	@Override
	public String toString() {
		return "RemoteNameAlreadyMappedException [existingMapping="
				+ existingMapping + "]";
	}
}
