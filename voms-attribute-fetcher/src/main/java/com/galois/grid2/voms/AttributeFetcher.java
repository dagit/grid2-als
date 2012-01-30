package com.galois.grid2.voms;

public interface AttributeFetcher {
	public abstract UserAttributes fetchAttributes(UserInfo userInfo)
			throws AttributeFetchException;

}