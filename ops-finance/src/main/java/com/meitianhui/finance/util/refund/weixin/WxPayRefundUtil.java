package com.meitianhui.finance.util.refund.weixin;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.meitianhui.common.util.PropertiesConfigUtil;
import com.meitianhui.finance.util.refund.weixin.common.PayConfigUtil;
import com.meitianhui.finance.util.refund.weixin.common.PayResData;
import com.meitianhui.finance.util.refund.weixin.common.XmlUtil;
import com.meitianhui.finance.util.refund.weixin.util.WxSign;





/**
 * 微信退款
 * @author Administrator
 *
 */
public class WxPayRefundUtil
{

    private static final Logger logger = Logger
            .getLogger(WxPayRefundUtil.class);
    

    public static void main(String[] args)
    {
        Map<String, String> param = new HashMap<String, String>();

        // 退款单号
        param.put("out_refund_no",  "Rfs45lbfdd8787653dsdafyt55");
        // 订单号
        param.put("out_trade_no", "9824410397499760641673291001");
        
        //退款金额以分为单位
        param.put("refund_fee", "2");
        
        //订单总额金额以分为单位
        param.put("total_fee", "2");
        
       // param.put("transaction_id", "9810758302297006081001");
        
        System.out.println(payRefund(param));
    }
	
    public synchronized static Map<String, String> payRefund(
            Map<String, String> param)
    {
        String url = "https://api.mch.weixin.qq.com/secapi/pay/refund";
        param.put("appid", PropertiesConfigUtil.getProperty("wechat.consumer_app_id"));
        param.put("mch_id", PropertiesConfigUtil.getProperty("wechat.consumer_mch_id"));
        param.put("nonce_str", PayConfigUtil.get32UUID());
        param.put("op_user_id", "10017164");
        String sign = WxSign.getSign(param);
        param.put("sign", sign);
        String xml = configXml(param);
        String result = refundSend(url, xml, "utf-8");
        logger.info("请求退款返回信息：" + result);
        PayResData payReseData = (PayResData) XmlUtil.getObjectFromXML(result, PayResData.class);

        Map<String, String> payParam = new HashMap<String, String>();

        payParam.put("return_code", payReseData.getReturn_code());
        payParam.put("return_msg", payReseData.getReturn_msg());
        payParam.put("result_code", payReseData.getResult_code());
        payParam.put("err_code", payReseData.getErr_code());
        payParam.put("err_code_des", payReseData.getErr_code_des());
        
        return payParam;
    }
    
    private static String configXml(Map<String, String> param)
    {
        StringBuffer bf = new StringBuffer();
        bf.append("<xml>");
        for (Entry<String, String> entry : param.entrySet())
        {
            bf.append("<" + entry.getKey() + ">");
            bf.append(entry.getValue());
            bf.append("</" + entry.getKey() + ">");
        }
        bf.append("</xml>");
        return bf.toString();
    }
    



	
	
    private static String refundSend(String url, String data, String charset)
    {

        KeyStore keyStore;
        StringEntity stEntity;
        try
        {
            HttpPost httppost = new HttpPost(url);

            keyStore = KeyStore.getInstance("PKCS12");

            FileInputStream instream = new FileInputStream(new File(
            		System.getProperty("mth.ops.finance.root") +PropertiesConfigUtil.getProperty("wechat.consumer_cert_local_path")));
            
            //商户id作为商户作为秘钥
            keyStore.load(instream, PropertiesConfigUtil.getProperty("wechat.consumer_mch_id").toCharArray());
            SSLContext sslcontext;
            sslcontext = SSLContexts.custom()
                    .loadKeyMaterial(keyStore, PropertiesConfigUtil.getProperty("wechat.consumer_mch_id").toCharArray())
                    .build();

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[] { "TLSv1" },
                    null,
                    SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

            // Httpclient SSLSocketFactory
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(sslsf).build();

            httppost.addHeader("Content-Type", "text/xml");
            stEntity = new StringEntity(data, charset);
            httppost.setEntity(stEntity);

            CloseableHttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                String resultString = "";
                resultString = EntityUtils.toString(entity, charset);
                System.out.println("--------------------------------------");
                System.out.println("Response content: " + resultString);
                System.out.println("--------------------------------------");
                return resultString;
            }

        } catch (Exception e)
        {
            logger.error("退款请求错误：" + e.toString(), e);
        }
        return null;
    }

}
