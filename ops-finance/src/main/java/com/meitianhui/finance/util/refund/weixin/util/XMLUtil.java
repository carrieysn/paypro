package com.meitianhui.finance.util.refund.weixin.util;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * xml工具类
 * @author miklchen
 *
 */
public class XMLUtil {
	protected static Logger logger = Logger.getLogger(XMLUtil.class);
	/**
	 * 解析xml,返回第一级元素键值对。如果第一级元素有子节点，则此节点的值是子节点的xml数据。
	 * @param strxml
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static Map doXMLParse(String strxml) throws JDOMException, IOException {
		strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");

		if(null == strxml || "".equals(strxml)) {
			return null;
		}
		
		Map m = new HashMap();
		
		InputStream in = new ByteArrayInputStream(strxml.getBytes("UTF-8"));
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(in);
		Element root = doc.getRootElement();
		List list = root.getChildren();
		Iterator it = list.iterator();
		while(it.hasNext()) {
			Element e = (Element) it.next();
			String k = e.getName();
			String v = "";
			List children = e.getChildren();
			if(children.isEmpty()) {
				v = e.getTextNormalize();
			} else {
				v = XMLUtil.getChildrenText(children);
			}
			
			m.put(k, v);
		}
		
		//关闭流
		in.close();
		
		return m;
	}
	
	/**
	 * 获取子结点的xml
	 * @param children
	 * @return String
	 */
	public static String getChildrenText(List children) {
		StringBuffer sb = new StringBuffer();
		if(!children.isEmpty()) {
			Iterator it = children.iterator();
			while(it.hasNext()) {
				Element e = (Element) it.next();
				String name = e.getName();
				String value = e.getTextNormalize();
				List list = e.getChildren();
				sb.append("<" + name + ">");
				if(!list.isEmpty()) {
					sb.append(XMLUtil.getChildrenText(list));
				}
				sb.append(value);
				sb.append("</" + name + ">");
			}
		}
		
		return sb.toString();
	}
	
	public static Map<String, String> parseXml(HttpServletRequest request) {
		// 解析结果存储在HashMap
		Map<String, String> map = null;
		InputStream inputStream = null;
		try {
			inputStream = request.getInputStream();
			// 读取输入流
			SAXReader reader = new SAXReader();
			org.dom4j.Document document = reader.read(inputStream);
			String xml = document.asXML();
			logger.info("微信回调xml:"+xml);
			// 得到xml根元素
			org.dom4j.Element root = document.getRootElement();
			// 得到根元素的所有子节点
			List<DefaultElement> elementList = root.elements();
			// 遍历所有子节点
			map = new HashMap<String, String>();
			logger.info("准备转换xml为map....................");
			for (DefaultElement e : elementList) {
				map.put(e.getName(), e.getText());
			}
			map.put("myxml", xml);
		} catch (IOException e1) {
			logger.error("读取HttpServletRequest的xml流出现异常", e1);
		} catch (DocumentException e1) {
			logger.error("解析HttpServletRequest的xml流出现异常", e1);
		} finally {
			// 释放资源
			try {
				if (inputStream != null) {
					inputStream.close();
					inputStream = null;
				}
			} catch (IOException e) {
				logger.error("关闭inputStream流出现异常", e);
			}

		}

		return map;
	}
}
