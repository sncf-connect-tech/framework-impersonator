package com.vsct.impersonator.http.integration.test;

import com.vsct.impersonator.http.message.RawRequest;
import com.vsct.impersonator.http.message.RequestIdentifier;

public class FixedUidRequestIdentifier implements RequestIdentifier {

	private final String uid;

	public FixedUidRequestIdentifier(final String uid) {
		this.uid = uid;
	}

	@Override
	public String calculateUid(final RawRequest request) {
		return uid;
	}

}
