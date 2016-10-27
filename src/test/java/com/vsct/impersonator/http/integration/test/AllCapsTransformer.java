package com.vsct.impersonator.http.integration.test;

import com.vsct.impersonator.http.message.Message;
import com.vsct.impersonator.http.message.RawRequest;
import com.vsct.impersonator.http.message.RawResponse;
import com.vsct.impersonator.http.message.transform.RequestTransformer;
import com.vsct.impersonator.http.message.transform.ResponseTransformer;

public class AllCapsTransformer implements RequestTransformer, ResponseTransformer {

	@Override
	public void transform(final RawRequest request) {
		upperCaseBody(request);
	}

	@Override
	public void transform(final RawResponse response) {
		upperCaseBody(response);
	}

	private void upperCaseBody(final Message message) {
		message.setBody(message.getBody().toUpperCase());
	}
}
