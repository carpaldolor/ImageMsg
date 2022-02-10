package io.github.imagemsg;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

	public static final int SHA256_BLOCK_SIZE = 32;
	public static final String algorithm = "AES/CBC/NoPadding";

	SecretKey secretkey;
	IvParameterSpec iv;

	public Encryption() {
	}

	public Encryption(String key) {
		init(key);
	}

	public static byte[] reHash(String str, int iter) {
		byte[] hash = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
			for (int i = 0; i < iter; i++) {
				digest.update(hash);
				hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return hash;
	}

	public static IvParameterSpec generateIv(String key) {
		byte[] iv = reHash(key, 10005);
		return new IvParameterSpec(iv, 0, 16);
	}

	public static SecretKey createKey(String key) {
		byte[] bar = reHash(key, 10000);
		SecretKey ret = new SecretKeySpec(bar, 0, 32, "AES");
		return ret;
	}

	public void init(String key) {
		secretkey = createKey(key);
		iv = generateIv(key);
	}

	public byte[] decrypt(byte[] cipherText, int start, int len) {

		byte[] plainText = null;
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, secretkey, iv);
			plainText = cipher.doFinal(cipherText, start, len);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return plainText;
	}

	public byte[] encrypt(byte[] cipherText) {
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, secretkey, iv);
			cipherText = cipher.doFinal(cipherText, 0, cipherText.length);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return cipherText;
	}

	public static byte[] hexTOBytes(String s) {
		byte[] bar = new byte[s.length() / 2];
		for (int i = 0; i < bar.length; i++) {
			bar[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 1), 16);
		}
		return bar;
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}


}
