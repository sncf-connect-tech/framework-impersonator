package com.vsct.impersonator.http.message.transform;

import com.vsct.impersonator.http.message.RawResponse;

/**
 * Transformer permettant de modifier les réponses
 */
public interface ResponseTransformer {

	void transform(RawResponse response);

}
