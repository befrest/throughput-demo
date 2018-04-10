package rest.bef.demo.util;


import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpUtil {

    private static final Logger LOGGER = LogManager.getLogger(HttpUtil.class);
    private static int defaultRequestTimeout = 5000;
    private static PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    private static SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();
    private static SSLContext sslContext = null;
    private static IdleConnectionMonitorThread monitorThread = new IdleConnectionMonitorThread();

    private static RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(defaultRequestTimeout)
            .setConnectionRequestTimeout(defaultRequestTimeout)
            .setSocketTimeout(defaultRequestTimeout).build();

    static {
        // Increase max total connection
        cm.setMaxTotal(500);
        // Increase default max connection per route
        cm.setDefaultMaxPerRoute(250);

        try {
            sslContextBuilder.loadTrustMaterial((x509Certificates, s) -> true);
            sslContext = sslContextBuilder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // this monitor is supposed to tell connection manager (cm) to check for stale connections and release their resources
        Thread t = new Thread(monitorThread);
        t.start();
    }

    private static SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(sslContext, (s, sslSession) -> true);

    private static CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(sslFactory)
            .setConnectionManager(cm)
            .setDefaultRequestConfig(config)
            .build();

    @SuppressWarnings("unchecked")
    public static <T> T fetchJson(HttpRequestBase request, Class<T> cls) {

        try {
            String response = fetchRawResponse(request, 5);
            ObjectMapper mapper = JsonFactory.create();
            return mapper.fromJson(response, cls);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * @param timeout In seconds
     */
    public static String fetchRawResponse(HttpRequestBase request, int timeout) throws Exception {
        timeout *= 1000;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout).build();

        request.setConfig(config);
        return fetchRawResponse(request);
    }

    public static byte[] fetchBinaryResponse(HttpRequestBase request, int timeout) throws Exception {
        timeout *= 1000;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout).build();

        request.setConfig(config);
        return fetchBinaryResponse(request);
    }


    public static HttpRequestBase buildHttpRequest(
            String method, String url, List<NameValuePair> params, Map<String, String> headers, String rawBody)
            throws MalformedURLException, UnsupportedEncodingException {

        new URL(url);

        if (method.toUpperCase().equals("POST")) {
            HttpPost post = new HttpPost(url);

            if (params != null && params.size() > 0)
                post.setEntity(new UrlEncodedFormEntity(params, Charset.forName("UTF8")));

            if (rawBody != null && !rawBody.trim().isEmpty())
                post.setEntity(new StringEntity(rawBody, "UTF8"));

            if (headers != null)
                for (Map.Entry<String, String> entry : headers.entrySet())
                    post.setHeader(entry.getKey(), entry.getValue());

            return post;
        }

        if (params != null && params.size() > 0) {
            url += "?";

            for (NameValuePair param : params)
                url += String.format("%s=%s&", param.getName(), URLEncoder.encode(param.getValue(), "UTF-8"));

            url = url.substring(0, url.length() - 1);
        }

        HttpGet get = new HttpGet(url);

        if (headers != null)
            for (Map.Entry<String, String> entry : headers.entrySet())
                get.setHeader(entry.getKey(), entry.getValue());

        return get;
    }

    private static String fetchRawResponse(HttpRequestBase request) throws Exception {
        byte[] bytes = fetchBinaryResponse(request);
        if (bytes != null)
            return new String(bytes);

        return null;
    }

    private static byte[] fetchBinaryResponse(HttpRequestBase request) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            InputStream stream = response.getEntity().getContent();

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                byte[] buffer = new byte[4096];
                int n;

                while ((n = stream.read(buffer)) != -1) {
                    output.write(buffer, 0, n);
                }
            } else {
                LOGGER.warn(String.format("resp not ok:[%d] [%s]", statusCode, request.getURI()));
            }

            return output.toByteArray();
        } finally {
            request.releaseConnection();
            output.close();
        }
    }

    private static class IdleConnectionMonitorThread extends Thread {
        private volatile boolean shutdown = false;

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(1000);
                        cm.closeExpiredConnections();
                        cm.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                LOGGER.error("ec-10027");
            }
        }
    }
}
