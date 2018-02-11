/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.util.core;

import java.io.UnsupportedEncodingException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;
import org.talend.utils.security.AES;

public class Crypt {

	private SecretKeySpec keySpec;

	private byte[] key;

	private String algorithm;

    public static final String ENCRYPT = ",Encrypt"; //$NON-NLS-1$

	public static Crypt getDESCryptInstance(String sharedSecret) throws ShortBufferException{
		byte[] key = new byte[8];
		byte[] bytes ;
		try {
			bytes = sharedSecret.getBytes("utf-8");
		} catch (UnsupportedEncodingException uee) {
			throw new ShortBufferException("The shared secret cannot be used as a key");
		}
		if (bytes.length<8) throw new ShortBufferException("The shared secret is too short");
		System.arraycopy(bytes, 0, key, 0, 8);
		return new Crypt(key,"DES");
	}

	/** Creates a new instance of Crypt */
	public Crypt(byte[] key, String algorithm) {
		this.key = key;
		this.algorithm = algorithm;
		this.keySpec = new SecretKeySpec(this.key, this.algorithm);
	}

	/**
	 * Encrypts the given String to a hex representation
	 */
	public String encryptHexString(String text) {
		return toHex(encryptString(text));
	}

	/**
	 * Decrypts the given hex representation
	 */
	public String decryptHexString(String text) {
		return decryptString(toByteArray(text));
	}
	
	/** Encrypts the give String to an array of bytes */
	private byte[] encryptString(String text) {
		try {
			Cipher cipher = Cipher.getInstance(this.algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, this.keySpec);
			return cipher.doFinal(text.getBytes());
		} catch (Exception e) {
			return null;
		}
	}

	/** Decrypts the given array of bytes to a String */
	private String decryptString(byte[] b) {
		try {
			Cipher cipher = Cipher.getInstance(this.algorithm);
			cipher.init(Cipher.DECRYPT_MODE, this.keySpec);
			return new String(cipher.doFinal(b));
		} catch (Exception e) {
			return null;
		}
	}



	/** Converts the given array of bytes to a hex String */
	public static String toHex(byte[] buf) {
		char[] cbf = new char[buf.length * 2];
		for (int jj = 0, kk = 0; jj < buf.length; jj++) {
			cbf[kk++] = "0123456789ABCDEF".charAt((buf[jj] >> 4) & 0x0F);
			cbf[kk++] = "0123456789ABCDEF".charAt(buf[jj] & 0x0F);
		}
		return new String(cbf);
	}

	/** Converts a valid hex String to an array of bytes */
	public static byte[] toByteArray(String hex) {
		byte[] result = new byte[hex.length() / 2];
		for (int jj = 0, kk = 0; jj < result.length; jj++) {
			result[jj] = (byte) (("0123456789ABCDEF".indexOf(hex.charAt(kk++)) << 4) + "0123456789ABCDEF".indexOf(hex.charAt(kk++)));
		}
		return result;
	}
	
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Parameter of 'original password' is required."); //$NON-NLS-1$
            return;
        }
        try {
            System.out.println(encrypt(args[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String data) throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        if (data == null || data.isEmpty()) {
            return StringUtils.EMPTY;
        }
        String encryptedData = AES.getInstance().encrypt(data) + ENCRYPT;
        return encryptedData;
    }

    public static String decrypt(String encryptedData) throws UnsupportedEncodingException, IllegalBlockSizeException,
            BadPaddingException {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return StringUtils.EMPTY;
        }
        if (!encryptedData.endsWith(ENCRYPT)) {
            return encryptedData;
        }
        encryptedData = encryptedData.substring(0, encryptedData.length() - ENCRYPT.length());
        String decryptedData = AES.getInstance().decrypt(encryptedData);
        return decryptedData;
    }

}
