package com.github.bryx.workflow.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class FileUtil {

	public static void createFile(String filePath, InputStream inputStream) throws IOException {
		try {
			File file = new File(filePath);
		    if (!file.getParentFile().exists()) {
		    	file.getParentFile().mkdirs();
	        }
			Files.copy(inputStream, new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
		}finally {
			IOUtils.closeQuietly(inputStream, null);
		}
	}
	
	public static void createFile(String path, byte[] bytes, boolean replace) throws IOException{
	    //判断文件是否存在
	    File file = new File(path);
	    if (!file.getParentFile().exists()) {
	    	file.getParentFile().mkdirs();
        }
	    if (file.exists()) {
	    	if (replace) {
				FileUtils.forceDelete(file);
			}else {
				throw new IOException("file already exists");
			}
	    } else {
	        if(!file.createNewFile()) {
	        	throw new IOException("file to create file");
	        }
	    }
	    
	    try(FileOutputStream fileOutputStream = new FileOutputStream(file);){
	    	FileCopyUtils.copy(bytes, fileOutputStream);
	    }
	}
	
	public static void createFile(String fullfilepath, String content, boolean replace) throws IOException {
		if(StringUtil.isEmpty(content)){
			throw new IOException("create file requires content");
		}
		createFile(fullfilepath, content.getBytes("UTF-8"), replace);
	}
	
	/*
	 * 创建空文件
	 */
	public static void createEmptyFile(String folderPath, String filename, boolean replace) throws IOException{
		File folder = new File(folderPath);
		if(!folder.exists()){
			folder.mkdirs();
		}
	    File file = new File(folderPath + File.separator + filename);
	    if (file.exists() && replace) {
			FileUtils.forceDelete(file);
		}
	    if (!file.exists() && file.createNewFile()) {//创建文件
			 throw new RuntimeException("创建文件失败");
	    }
	}

	public static String readFileToString(File file) throws Exception{
		FileInputStream fisTargetFile = new FileInputStream(file);
		return IOUtils.toString(fisTargetFile, "UTF-8");
	}
	
	public static String readFileFromClasspath(String fileName) throws Exception{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		log.debug("read file from classpath:"+classLoader.getResource(fileName).getPath());
		InputStream in = classLoader.getResourceAsStream(fileName);
		return IOUtils.toString(in, "UTF-8");
	}
	
	public static String readFileToString(InputStream inputStream) throws IOException{
		return IOUtils.toString(inputStream, "UTF-8");
	}

	public static InputStream getInputStreamFromBytes(byte[] bytes){
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		return in;
	}
	
	public static boolean downloadFile(String fileUrl, String folderPath, String filename) throws Exception {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new Exception("makdirs: '" + folder + "'fail");
            }
        }
        URL url = new URL(fileUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(3 * 1000);
        //防止屏蔽程序抓取而放回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0(compatible;MSIE 5.0;Windows NT;DigExt)");
        Long totalSize = Long.parseLong(conn.getHeaderField("Content-Length"));
        if (totalSize > 0) {
            FileUtils.copyURLToFile(url, new File(folderPath+File.separator+filename));
            return true;
        } else {
            throw new Exception("can not find serverUrl :{}" + fileUrl);
        }
    }

	public static void writeToFile(String filePath, String content) throws IOException {
		FileUtils.writeStringToFile(new File(filePath), content, "UTF-8");
	}

	public static String getReadableFileSize(long size)
    {
        double value = (double) size;
        if (value < 1024) {
            return String.valueOf(value) + "B";
        } else {
            value = BigDecimal.valueOf(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
        }
        // 如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位
        // 因为还没有到达要使用另一个单位的时候
        // 接下去以此类推
        if (value < 1024) {
            return String.valueOf(value) + "KB";
        } else {
            value = BigDecimal.valueOf(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
        }
        if (value < 1024) {
            return String.valueOf(value) + "MB";
        } else {
            // 否则如果要以GB为单位的，先除于1024再作同样的处理
            value = BigDecimal.valueOf(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
            return String.valueOf(value) + "GB";
        }
    }
	/**
	 * 
	 * @param filePath contain folder and filename
	 * @return filename in filepath
	 */
	public static String getFileName(String filePath){
		String fileName = filePath;
		fileName = fileName.replaceAll("\\\\", "/");
		if (filePath.contains("/")) {
			fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
		}
		return fileName;
	}
	/**
	 * 
	 * @param filePath contain folder and filename
	 * @return folder in filepath
	 */
	public static String getFolder(String filePath) {
		String result = filePath;
		result = result.replaceAll("\\\\", "/");
		if (filePath.contains("/")) {
			result = filePath.substring(0, filePath.lastIndexOf("/"));
		}
		return result;
	}
	
	public static Set<PosixFilePermission> getPermission(String file){
		try {
			return Files.readAttributes(Paths.get(file),PosixFileAttributes.class).permissions();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new HashSet<>();
	}
	
	public static void setPersmission(String path, String mode){
		Integer m = Integer.valueOf(mode);
		int permission = m/100;
		Set<PosixFilePermission> perms = new HashSet<>();
		if(hasReadPersmission(permission)){
			 perms.add(PosixFilePermission.OWNER_READ);
		}
		if(hasWritePersmission(permission)){
			 perms.add(PosixFilePermission.OWNER_WRITE);
		}
		if(hasExePersmission(permission)){
			 perms.add(PosixFilePermission.OWNER_EXECUTE);
		}
		
		permission = (m%100)/10;
		if(hasReadPersmission(permission)){
			 perms.add(PosixFilePermission.GROUP_READ);
		}
		if(hasWritePersmission(permission)){
			 perms.add(PosixFilePermission.GROUP_WRITE);
		}
		if(hasExePersmission(permission)){
			 perms.add(PosixFilePermission.GROUP_EXECUTE);
		}
		
		permission = m%10;
		if(hasReadPersmission(permission)){
			 perms.add(PosixFilePermission.OTHERS_READ);
		}
		if(hasWritePersmission(permission)){
			 perms.add(PosixFilePermission.OTHERS_WRITE);
		}
		if(hasExePersmission(permission)){
			 perms.add(PosixFilePermission.OTHERS_EXECUTE);
		}
		setPermission(path, perms);
	}
	
	private static boolean hasReadPersmission(int mode){
		return mode>0?(mode/4==1):false;
	}
	
	private static boolean hasWritePersmission(int mode){
		return mode>0?((mode%4)/2==1):false;
	}
	
	private static boolean hasExePersmission(int mode){
		return mode>0?(mode%2==1):false;
	}
	
	private static void setPermission(String path, Set<PosixFilePermission> perms){
		Path paths = Paths.get(path);
        try {
			Files.setPosixFilePermissions(paths, perms);
		} catch (IOException e) {
			log.error("set permission fail ",e);
		}
	}

	public static void deleteFile(String filePath) {
		FileUtils.deleteQuietly(new File(filePath));
	}
}
