package com.vsct.impersonator.http.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

public final class MD5 {
	private static final Logger LOGGER = Logger.getLogger(MD5.class);

	private static MessageDigest md = null;
	private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	public static final int HASH_LENGTH = 32;

	static {
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static String hashData(final String dataToHash) {
		String md5 = hashData(dataToHash.getBytes());
		LOGGER.debug(dataToHash + " => " + md5);
		return md5;
	}

	public static String hashData(final byte[] dataToHash) {
		return stringFromBytes(calculateHash(dataToHash));
	}

	private static synchronized byte[] calculateHash(final byte[] dataToHash) {
		md.update(dataToHash, 0, dataToHash.length);
		return md.digest();
	}

	private static String stringFromBytes(final byte[] b) {
		final StringBuilder hex = new StringBuilder();
		// MSB maps to idx 0
		for (final byte bb : b) {
			int j = bb & 0x000000FF;
			int msb = j / 16;
			int lsb = j % 16;
			hex.append(HEX_CHARS[msb]);
			hex.append(HEX_CHARS[lsb]);
		}
		return hex.toString();
	}

}
