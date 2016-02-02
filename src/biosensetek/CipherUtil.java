package biosensetek;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;

public class CipherUtil {
	private static final int LENGTH = 1024;
	private static final int INPUT 	= 117;
	private static final int OUTPUT = 128;
	private static KeyPair pair 	= null;

	public static final KeyPair init(File store) {
		if (pair == null) {
			if (store.exists()) {
				try (FileInputStream input = new FileInputStream(store)) {
					pair = (KeyPair) SerializationUtils.deserialize(input);
				} catch (Throwable e) {}
			} else {
				try (FileOutputStream output = new FileOutputStream(store)) {
					pair = init(LENGTH);
					SerializationUtils.serialize(pair, output);
					output.flush();
				} catch (Throwable e) {}
			}
		}
		return pair;
	}

	private static final KeyPair init(int length) throws NoSuchAlgorithmException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(length);
		return generator.genKeyPair();
	}

	private static final Cipher cipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
		return Cipher.getInstance("RSA/ECB/PKCS1Padding");
	}

	public static <T extends OutputStream> T encrypt(InputStream input, T output) {
		try {
			Cipher cipher = cipher();
			cipher.init(Cipher.ENCRYPT_MODE, pair.getPublic());
			byte[] buffer = new byte[INPUT];
			int length;
			while ((length = input.read(buffer)) != -1) {
				output.write(cipher.doFinal(buffer, 0, length));
			}
			output.flush();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return output;
	}

	public static <T extends OutputStream> T decrypt(InputStream input, T output) {
		try {
			Cipher cipher = cipher();
			cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
			byte[] buffer = new byte[OUTPUT];
			int length;
			while ((length = input.read(buffer)) == buffer.length) {
				output.write(cipher.doFinal(buffer, 0, length));
			}
			output.flush();
		} catch (Throwable e) {}
		return output;
	}

	public static final byte[] encrypt(byte[] data) {
		try {
			ByteArrayInputStream input = new ByteArrayInputStream(data);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			return encrypt(input, output).toByteArray();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static final String encrypt(String data) {
		try {
			ByteArrayInputStream input = new ByteArrayInputStream(data.getBytes());
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			return Base64.encodeBase64URLSafeString(encrypt(input, output).toByteArray());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static final byte[] decrypt(byte[] data) {
		try {
			ByteArrayInputStream input = new ByteArrayInputStream(data);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			return decrypt(input, output).toByteArray();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static final String decrypt(String data) {
		try {
			ByteArrayInputStream input = new ByteArrayInputStream(Base64.decodeBase64(data));
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			return new String(decrypt(input, output).toByteArray());
		} catch (Throwable e) {}
		return null;
	}

	public static void main(String[] args) throws Throwable {
		KeyPair tmpPair = init(4096);
		KeyPair pair = init(512);
		byte[] tmpPubse = SerializationUtils.serialize(tmpPair.getPublic());
		byte[] pubse = SerializationUtils.serialize(pair.getPublic());
		//
		Cipher cipher = cipher();
		cipher.init(Cipher.ENCRYPT_MODE, tmpPair.getPublic());
		byte[] encPubByTmpPub = cipher.doFinal(pubse);		
	}
}