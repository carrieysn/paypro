package com.meitianhui.supplierCentre.util;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/***
 * 页面验证码生成类，提供页面验证方式
 * 
 * @author 丁硕
 * @date 2016年9月22日
 */
public class ValidateCodeImageHepler {

	public static final String CODE_KEY = "validataCode";

	// 设置字母的大小,大小
	private static Font mFont = new Font("Times New Roman", Font.BOLD, 23);

	// 控制验证码的宽度
	private Integer width = 80;

	// 控制验证码的高度
	private Integer height = 30;

	// 控制验证码的字符个数
	private Integer codeCount = 4;

	public ValidateCodeImageHepler() {
	}

	public ValidateCodeImageHepler(Integer width, Integer height, Integer codeCount) {
		this.width = width;
		this.height = height;
		this.codeCount = codeCount;
	}
	
	/**
	 * 获得default类型验证码
	 * 
	 * @param       @param
	 * @param       @return
	 * @return      String 生成的随机验证码
	 * @throws
	 * @author      刘涛
	 * @date        2015-4-28
	 */
	private  String getDefaultTypeValidateCode(Graphics g){
		Random random = new Random();
		g.setColor(Color.WHITE);  //设置图片的背景颜色
		g.fillRect(0, 0, width, height);  //不设置边框
		g.setColor(Color.WHITE);
		g.drawRect(1, 1, width, height);
		g.setColor(getRandColor(120, 200));  //设置干扰线的颜色
		
		// 文本的版本中不允许加入随机线的要求：
		// 画随机线
		for (int i = 0; i < 155; i++) {
			int x = random.nextInt(this.width - 1);
			int y = random.nextInt(this.height - 1);
			int xl = random.nextInt(6) + 1;
			int yl = random.nextInt(12) + 1;
			g.drawLine(x, y, x + xl, y + yl);
		}
//		// 生成随机数,并将随机数字转换为字母
		String sRand = "";
		char[] charArr = "ABCDEFGHIGKLMNOPQRSTUVWXYZabcdefghigklmnopqrstuvwxyz0123456789".toCharArray();
		for (int i = 0; i < this.codeCount; i++) {
			int itmp = random.nextInt(62);
			char ctmp = charArr[itmp];
			sRand += String.valueOf(ctmp);
			g.setColor(new Color(20 + random.nextInt(150),20 + random.nextInt(160),20 + random.nextInt(110)));
			g.setFont(mFont);
			g.drawString(String.valueOf(ctmp), 15 * i + 10, this.height - 4);
		}
		return sRand;
	}
	
	/***
	 * 获得simple类型验证码
	 * 
	 * @param       @param g
	 * @param       @param request
	 * @param       @return
	 * @return      String
	 * @throws
	 * @author      刘涛
	 * @date        2015-4-28
	 */
	private  String getNumberTypeValidateCode(Graphics g ,HttpServletRequest request){
		 Random random = new Random();   
	        int intTemp;   
	        int intFirst = random.nextInt(10);   
	        int intSec = random.nextInt(10);   
	        String checkCode = "";   
	        int result = 0;   
	        switch (random.nextInt(6)) {   
	            case 0:   
	                if (intFirst < intSec) {   
	                    intTemp = intFirst;   
	                    intFirst = intSec;   
	                    intSec = intTemp;   
	                }   
	                checkCode = intFirst + " - " + intSec + " = ?";   
	                result = intFirst-intSec;   
	                break;   
	            case 1:   
	                if (intFirst < intSec) {   
	                    intTemp = intFirst;   
	                    intFirst = intSec;   
	                    intSec = intTemp;   
	                }   
	                checkCode = intFirst + " - ? = "+(intFirst-intSec);   
	                result = intSec;   
	                break;   
	            case 2:   
	                if (intFirst < intSec) {   
	                    intTemp = intFirst;   
	                    intFirst = intSec;   
	                    intSec = intTemp;   
	                }   
	                checkCode = "? - "+intSec+" = "+(intFirst-intSec);   
	                result = intFirst;   
	                break;   
	            case 3:   
	                checkCode = intFirst + " + " + intSec + " = ?";   
	                result = intFirst + intSec;   
	                break;   
	            case 4:   
	                checkCode = intFirst + " + ? ="+(intFirst+intSec);   
	                result = intSec;   
	                break;   
	            case 5:   
	                checkCode = "? + " + intSec + " ="+(intFirst+intSec);   
	                result = intFirst;   
	                break;   
	        }   
	        g.setColor(getRandColor(200, 250));   
	        g.fillRect(0, 0, width, height);   
	  
	        String[] fontTypes = { "\u5b8b\u4f53", "\u65b0\u5b8b\u4f53", "\u9ed1\u4f53", "\u6977\u4f53", "\u96b6\u4e66" };   
	        int fontTypesLength = fontTypes.length;   
	  
	        g.setColor(getRandColor(160, 200));   
	        g.setFont(new Font("Times New Roman", Font.PLAIN, 12));   
	           
	        for (int i = 0; i < 255; i++) {   
	            int x = random.nextInt(width);   
	            int y = random.nextInt(height);   
	            int xl = random.nextInt(12);   
	            int yl = random.nextInt(12);   
	            g.drawLine(x, y, x + xl, y + yl);   
	        }   
	           
	        String [] baseChar = checkCode.split(" ");   
	        for (int i = 0; i < baseChar.length; i++) {   
	            g.setColor(getRandColor(30, 150));   
	            g.setFont(new Font(fontTypes[random.nextInt(fontTypesLength)], Font.BOLD, 22));   
	            g.drawString(baseChar[i], 15 * i + 10, this.height - 1);   
	        }   
	        return String.valueOf(result);   

	}
 
	/***
	 * 获取创建验证码后的结果
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author 丁硕
	 * @date   2016年9月22日
	 */
	public String getCreateValidImageResult(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0); 
		
		// 表明生成的响应是图片
		response.setContentType("image/jpeg");
		BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB); 
		Graphics g = image.getGraphics();
		
		String result = "";
		//验证码类型
		String codeType = request.getParameter("codeType");
		if(StringUtils.isEmpty(codeType) || "default".equals(codeType)){
			 result = getDefaultTypeValidateCode(g);
		}else if("number".equals(codeType)){
			result = getNumberTypeValidateCode(g,request);
		}
		
		g.dispose();
		ImageIO.write(image, "JPEG", response.getOutputStream());
		return result;
	} 
	

	/***
	 * 获取随机验证码颜色
	 * 
	 * @param       @param fc
	 * @param       @param bc
	 * @param       @return
	 * @return      Color
	 * @throws
	 * @author      刘涛
	 * @date        2015-4-28
	 */
	private Color getRandColor(int fc, int bc) {
		Random random = new Random();
		if (fc > 255)
			fc = 255;
		if (bc > 255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}
}