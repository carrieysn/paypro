package com.meitianhui.finance.util.refund.weixin.util;

import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class HttpClientTool {
	private static Logger logger = Logger.getLogger(HttpClientTool.class);
    /**
     * 
     * post请求
     * @param url
     * @param data
     * @param charset
     * @return
     * @throws IOException
     */
    public static String post(String url,String data, String charset) throws IOException{

        HttpPost httppost = new HttpPost(url);
        
        StringEntity stEntity;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try
        {
            httppost.addHeader("Content-Type", "text/xml");
            stEntity = new StringEntity(data,charset);
            httppost.setEntity(stEntity);
           
            CloseableHttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            
            if (entity != null)
            {
                String resultString="";
                resultString=EntityUtils.toString(entity,charset);
                logger.debug("--------------------------------------");
                logger.debug("Response content: " +resultString);
                logger.debug("--------------------------------------");
                return  resultString;  
            }
        } catch (UnsupportedEncodingException e)
        {
          throw e;
        } catch (ClientProtocolException e)
        {
           throw e;
        } catch (IOException e)
        {
           throw e;
        }finally{
            httpclient.close();
        }
        return null;
    }
    

    public synchronized static String postData(String url,
            Map<String, String> params, String codePage) throws Exception {

        final HttpClient httpClient = new HttpClient();
        httpClient.getHttpConnectionManager().getParams()
                .setConnectionTimeout(10 * 1000);
        httpClient.getHttpConnectionManager().getParams()
                .setSoTimeout(10 * 1000);

        final PostMethod method = new PostMethod(url);
        if (params != null) {
            method.getParams().setParameter(
                    HttpMethodParams.HTTP_CONTENT_CHARSET, codePage);
            method.setRequestBody(assembleRequestParams(params));
        }
        String result = "";
        try {
            httpClient.executeMethod(method);
            result = new String(method.getResponseBody(), codePage);
        } catch (final Exception e) {
            throw e;
        } finally {
            method.releaseConnection();
        }
        return result;
    }
    
    /**
     * 组装http请求参数
     * 
     * @return
     */
    private synchronized static NameValuePair[] assembleRequestParams(
            Map<String, String> data) {
        final List<NameValuePair> nameValueList = new ArrayList<NameValuePair>();

        Iterator<Map.Entry<String, String>> it = data.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) it
                    .next();
            nameValueList.add(new NameValuePair((String) entry.getKey(),
                    (String) entry.getValue()));
        }

        return nameValueList.toArray(new NameValuePair[nameValueList.size()]);
    }
    
    public static String get(String url){
        String result = null;
        try {
          
            // 根据地址获取请求
            HttpGet request = new HttpGet(url);
            // 获取当前客户端对象
            CloseableHttpClient httpclient = HttpClients.createDefault();
            
            // 通过请求对象获取响应对象
            HttpResponse response = httpclient.execute(request);
            
            // 判断网络连接状态码是否正常(0--200都数正常)
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result= EntityUtils.toString(response.getEntity());
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
	 * 发起https请求并获取结果
	 * 
	 * @param requestUrl 请求地址
	 * @param requestMethod 请求方式（GET、POST）
	 * @param outputStr 提交的数据
	 * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
	 */
	public static JSONObject httpsRequest(String requestUrl, String requestMethod, String outputStr) {
		JSONObject jsonObject = null;
		StringBuffer buffer = new StringBuffer();
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new MyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();

			URL url = new URL(requestUrl);
			HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
			httpUrlConn.setSSLSocketFactory(ssf);

			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);

			if ("GET".equalsIgnoreCase(requestMethod))
				httpUrlConn.connect();

			// 当有数据需要提交时
			if (null != outputStr) {
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}

			// 将返回的输入流转换成字符串
			InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();
			jsonObject = JSONObject.fromObject(buffer.toString());
		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	 /**
	 * 发起https请求并获取结果
	 * 
	 * @param requestUrl 请求地址
	 * @param requestMethod 请求方式（GET、POST）
	 * @param outputStr 提交的数据
	 */
	public static String httpsRequest(String requestUrl, String requestMethod) {
		StringBuffer buffer = new StringBuffer();
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new MyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();

			URL url = new URL(requestUrl);
			HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
			httpUrlConn.setSSLSocketFactory(ssf);

			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);

			if ("GET".equalsIgnoreCase(requestMethod))
				httpUrlConn.connect();

			// 当有数据需要提交时
			/*if (null != outputStr) {
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}*/

			// 将返回的输入流转换成字符串
			InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();
			return buffer.toString();
		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
