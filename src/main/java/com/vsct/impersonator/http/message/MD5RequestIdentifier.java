package com.vsct.impersonator.http.message;

import com.vsct.impersonator.http.util.MD5;

public class MD5RequestIdentifier implements RequestIdentifier {

	@Override
	public String calculateUid(final RawRequest rawRequest) {
		String fullUri = rawRequest.getFullUri();
		return MD5.hashData(fullUri);
	}

}
