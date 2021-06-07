package cn.rongcloud.moment.server.common.rce;

import io.rong.util.CodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


public class HttpUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class.getCanonicalName());
	
	private static final String UID = "uid";
	private static final String NONCE = "nonce";
	private static final String TIMESTAMP = "timestamp";
	private static final String SIGNATURE = "sign";

	private static SSLContext sslCtx = null;
	static {

		try {
			sslCtx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			sslCtx.init(null, new TrustManager[] { tm }, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				// TODO Auto-generated method stub
				return true;
			}

		});

		HttpsURLConnection.setDefaultSSLSocketFactory(sslCtx.getSocketFactory());

	}

	public static HttpURLConnection CreateHttpConnection(String host, String systemUid, String secret, String uri) throws IOException {
			String nonce = String.valueOf(Math.random() * 1000000);
			String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
			StringBuilder toSign = new StringBuilder(secret).append(nonce).append(timestamp);
			String sign = CodeUtil.hexSHA1(toSign.toString());
			uri = host + uri;
			URL url = new URL(uri);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(true);
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);

			conn.setRequestProperty(UID, systemUid);
			conn.setRequestProperty(NONCE, nonce);
			conn.setRequestProperty(TIMESTAMP, timestamp);
			conn.setRequestProperty(SIGNATURE, sign);
			conn.setRequestProperty("Content-Type", "application/json");

			return conn;
	}


	public static byte[] readInputStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();
		outStream.close();
		inStream.close();
		return data;
	}

	public static String returnResult(HttpURLConnection conn) throws Exception {
		InputStream input = null;
		if (conn.getResponseCode() == 200) {
			input = conn.getInputStream();
		} else {
			input = conn.getErrorStream();
		}
		String result = new String(readInputStream(input), "UTF-8");
		LOGGER.info("IM server api response:{}", result);
		return result;
	}

	public static void setBodyParameter(String str, HttpURLConnection conn) throws IOException {
		LOGGER.info("Call IM server api with url {}, data {}", conn.getURL().toString(), str);
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		out.write(str.getBytes("utf-8"));
		out.flush();
		out.close();
	}
}
