package com.vsct.impersonator.http.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MD5Test {

    @Test
    public void shouldCalculateMD5FromString() throws Exception {
        assertThat(MD5.hashData("foobar"), is("3858F62230AC3C915F300C664312C63F"));
    }
}
