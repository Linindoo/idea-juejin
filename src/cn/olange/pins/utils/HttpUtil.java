package cn.olange.pins.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

public class HttpUtil {
	public static String postJson(String url, String data) throws IOException {
		return postJson(url, data, "");
	}
	public static String postJson(String url, String data, String cookie) throws IOException {
		CloseableHttpClient httpClient = createSSLClientDefault();
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		HttpPost httpPost = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(8000).setConnectTimeout(8000).build();
		httpPost.setConfig(requestConfig);
		StringEntity requestEntity = new StringEntity(data,"utf-8");
		if (StringUtils.isNotEmpty(cookie)) {
			httpPost.addHeader("cookie",cookie);
		}
		httpPost.addHeader("Content-Type", "application/json");
		httpPost.addHeader("X-Agent", "Juejin/Web");
		httpPost.addHeader(":authority", "apinew.juejin.im");
		httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");
		httpPost.setEntity(requestEntity);
		return httpClient.execute(httpPost, responseHandler);
	}

	public static String getJson(String url) throws IOException {
		return getJson(url, null);
	}

	public static CloseableHttpResponse getResponse(String url) throws IOException {
		CloseableHttpClient httpClient = createSSLClientDefault();
		HttpGet httpGet = new HttpGet(url);
		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(20 * 1000).setSocketTimeout(20 * 1000).setConnectTimeout(20 * 1000).build();
		httpGet.setConfig(requestConfig);
		httpGet.addHeader("Content-Type", "application/json;charset=utf-8");
		httpGet.addHeader("X-Agent", "Juejin/Web");
		httpGet.addHeader("X-Juejin-Src", "web");
		httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");
		return httpClient.execute(httpGet);
	}

	public static String getLonTimeJson(String url) throws IOException {
		CloseableHttpClient httpClient = createSSLClientDefault();
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		HttpGet httpGet = new HttpGet(url);
		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(20*1000).setSocketTimeout(20*1000).setConnectTimeout(20*1000).build();
		httpGet.setConfig(requestConfig);
		httpGet.addHeader("Content-Type", "application/json;charset=utf-8");
		httpGet.addHeader("X-Agent", "Juejin/Web");
		httpGet.addHeader("X-Juejin-Src","web");
		httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");
		httpClient.execute(httpGet);
		return httpClient.execute(httpGet, responseHandler);
	}
	public static String getJson(String url, String cookie) throws IOException {
		CloseableHttpClient httpClient = createSSLClientDefault();
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		HttpGet httpGet = new HttpGet(url);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(8000).setConnectTimeout(8000).build();
		httpGet.setConfig(requestConfig);
		httpGet.addHeader("Content-Type", "application/json;charset=utf-8");
		httpGet.addHeader("X-Agent", "Juejin/Web");
		httpGet.addHeader("X-Juejin-Src","web");
		if (StringUtils.isNotEmpty(cookie)) {
			httpGet.addHeader("cookie",cookie);
		}
		httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");
		return httpClient.execute(httpGet, responseHandler);
	}
	public static CloseableHttpClient createLongTimeSSLClientDefault() {
		try {
			//使用 loadTrustMaterial() 方法实现一个信任策略，信任所有证书
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				// 信任所有
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();
			//NoopHostnameVerifier类:  作为主机名验证工具，实质上关闭了主机名验证，它接受任何
			//有效的SSL会话并匹配到目标主机。
			HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
			return HttpClients.custom().setConnectionTimeToLive(16, TimeUnit.SECONDS).setSSLSocketFactory(sslsf).build();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		return HttpClients.createDefault();
	}
	public static CloseableHttpClient createSSLClientDefault() {
		try {
			//使用 loadTrustMaterial() 方法实现一个信任策略，信任所有证书
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				// 信任所有
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();
			//NoopHostnameVerifier类:  作为主机名验证工具，实质上关闭了主机名验证，它接受任何
			//有效的SSL会话并匹配到目标主机。
			HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		return HttpClients.createDefault();
	}
}
