package security;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import data.LogUtil;
import data.StringUtil;

public class SecurityUtil {
	public enum CheckSumAlgoritm {
		MD5("MD5"),  SHA1("SHA1"), SHA256("SHA-256"), SHA384(
				"SHA-384"), SHA512("SHA-512");
		private String value;

		private CheckSumAlgoritm(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	private static ThreadLocal<SecurityUtil> instance = new ThreadLocal<SecurityUtil>();

	public static SecurityUtil getInstance() {
		SecurityUtil result = instance.get();
		if (null == result) {
			result = new SecurityUtil();
			instance.set(result);
		}

		return result;
	}

	public byte[] getChecksum(String filename, CheckSumAlgoritm algorithm) {
		byte[] result = new byte[0];

		try (InputStream is = new FileInputStream(filename)) {
			MessageDigest md = MessageDigest.getInstance(algorithm.getValue());
			DigestInputStream dis = new DigestInputStream(is, md);
			while (0 < dis.read(new byte[100000], 0, 100000))
				;
			result = md.digest();
		} catch (IOException | NoSuchAlgorithmException e) {
			LogUtil.getInstance().error(
					"unable to calculate digest for [" + filename + "]", e);
		}

		return result;
	}

	public static void main(String[] args) {
		String filename = StringUtil.getInstance()
				.getArgument(args, "file", "");
		String checksum = StringUtil.getInstance()
				.getArgument(args, "checksum");

		if (!filename.isEmpty()) {
			String line = "checksum : ";
			System.out.print(StringUtil.getInstance().pad(line, ' ', 30));
			System.out.println(checksum);
			for (CheckSumAlgoritm algo : CheckSumAlgoritm.values()) {
				byte[] bytes = SecurityUtil.getInstance().getChecksum(filename,
						algo);
				line = "checksum " + algo.getValue() + " : ";
				System.out.print(StringUtil.getInstance().pad(line, ' ', 30));
				System.out.print(StringUtil.getInstance().byteArray2String(
						bytes, true));
				System.out
						.println(" -> result ["
								+ (StringUtil.getInstance()
										.byteArray2String(bytes, true)
										.equals(checksum) ? "passed"
										: "failed ") + "]");
			}

		}
	}
}
