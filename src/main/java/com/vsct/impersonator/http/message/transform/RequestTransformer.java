package com.vsct.impersonator.http.message.transform;

import com.vsct.impersonator.http.message.RawRequest;

/**
 * Transformer permettant de modifier les requêtes.
 */
public interface RequestTransformer {

	void transform(RawRequest rawRequest);

}
