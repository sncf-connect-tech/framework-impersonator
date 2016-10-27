package com.vsct.impersonator.http.integration.test;

import com.vsct.impersonator.http.util.Sleeper.Bounds;

public class DefaultBounds implements Bounds {

	@Override
	public int getMin() {
		return 0;
	}

	@Override
	public int getMax() {
		return 0;
	}

}
