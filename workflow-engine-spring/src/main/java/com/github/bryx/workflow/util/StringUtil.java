package com.github.bryx.workflow.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StringUtil extends StringUtils {

	private static Random random = new Random();
	
	public static boolean isNumeric(String str){ 
	   Pattern pattern = Pattern.compile("[0-9]+");
	   Matcher isNum = pattern.matcher(str);
	   if( !isNum.matches() ){
	       return false; 
	   } 
	   return true; 
	}
	/**
	 * 
	 * @param length
	 * @return
	 * 
	 * length位的随机数
	 * 
	 */
	public static String randomNumeric(int length){
		StringBuilder builder = new StringBuilder();
		for (int i= 0;  i< length; i++) {
			builder.append(random.nextInt(10));
		}
		return builder.toString();
	}
	
	public static String base64Encode(String s){
		try {
			return new String(Base64.encodeBase64(s.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public static String base64Decode(String s){
		return new String(Base64.decodeBase64(s));
	}


	public static String removePrefix(String source, String prefix){
		if(source.indexOf(prefix)==0){
			return source.substring(prefix.length());
		}
		return source;
	}
	
	public static String removesubfix(String source, String subfix){
		int subfixIndex = source.lastIndexOf(subfix);
		if(subfixIndex!=-1){
			return source.substring(0, subfixIndex);
		}
		return source;
	}

	public static String getFileExtension(String fileName) {
		 return fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")+1) : "";		
	}
	
	public static String changeFileExtension(String fileName, String newExtension){
		String extension = getFileExtension(fileName);
		return StringUtil.removesubfix(fileName, extension) + newExtension;
	}

	/**
	* @param regex
	* 正则表达式字符串
	* @param str
	* 要匹配的字符串
	* @return 如果str 符合 regex的正则表达式格式,返回true, 否则返回 false;
	*/
	public static boolean match(String regex, String str) {
		if(str==null || regex==null) return false;
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(str);
			return matcher.matches();
		} catch (PatternSyntaxException e) {
			throw new RuntimeException("非法的正则表达式:"+regex);
		}
	}
	
	public static String escape(String source, List<String> escapeChars, String escapePrefix){
		for(String item:escapeChars ){
			source = source.replaceAll(item, escapePrefix+item);
		}
		return source;
	}
	
	public static String urlEncode(String source){
		try {
			return URLEncoder.encode(source, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return source;
		} 
	}
}
