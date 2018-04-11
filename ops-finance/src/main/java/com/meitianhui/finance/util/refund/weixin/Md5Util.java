package com.meitianhui.finance.util.refund.weixin;

import org.apache.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {

	/** log4j */
	private static final Logger logger = Logger.getLogger(Md5Util.class);

	/**
	 * 128位MD5(16字节)
	 * 
	 * @param data
	 * @return
	 */
	public static String md532(String data) {
		try {
			MessageDigest mdInst;
			mdInst = MessageDigest.getInstance("MD5");
			byte[] bytes = data.getBytes();
			mdInst.update(bytes);
			return bytes2HexString(mdInst.digest()).toLowerCase();
		} catch (NoSuchAlgorithmException e) {
			logger.error("处理MD5过程中出错:" + data, e);
		}
		return null;
	}

	/** 16进制所有的位的字节数组 */
	private final static byte[] hex = "0123456789ABCDEF".getBytes();

	private static String bytes2HexString(byte[] b) {
		byte[] buff = new byte[2 * b.length];
		for (int i = 0; i < b.length; i++) {
			buff[2 * i] = hex[(b[i] >> 4) & 0x0f];
			buff[2 * i + 1] = hex[b[i] & 0x0f];
		}
		return new String(buff);
	}

	private static String byteArrayToHexString(byte b[]) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++)
			resultSb.append(byteToHexString(b[i]));
		return resultSb.toString();
	}

	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0)
			n += 256;
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigits[d1] + hexDigits[d2];
	}

	public static String MD5Encode(String origin, String charsetname) {
		String resultString = null;
		try {
			resultString = new String(origin);
			MessageDigest md = MessageDigest.getInstance("MD5");
			if (charsetname == null || "".equals(charsetname))
				resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
			else
				resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetname)));
		} catch (Exception exception) {
		}
		return resultString;
	}
	public static String MD5(String laintext) {
		String re_md5 = new String();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(laintext.getBytes("UTF-8"));
			byte b[] = md.digest();

			int i;

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			re_md5 = buf.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return re_md5;
	}

	public static String md5Ctrip(String laintext) {
		String re_md5 = new String();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(laintext.getBytes("UTF-8"));
			byte b[] = md.digest();

			int i;

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			re_md5 = buf.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return re_md5;
	}

	private static final String hexDigits[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
			"e", "f" };
	public static void main(String args[])
	{
		System.out.println(MD5("12345"));
	}
}
