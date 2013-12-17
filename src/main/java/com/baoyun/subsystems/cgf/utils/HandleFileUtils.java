package com.baoyun.subsystems.cgf.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.baoyun.subsystems.cgf.handler.processor.cdr.CdrType;

import static com.baoyun.subsystems.cgf.utils.CdrSerializationUtils.*;
/**
 * <B>处理文件的工具类</B>
 * 
 * @author BaoXu
 */
public class HandleFileUtils {

	static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");// 设置日期格式

	/**
	 * 将二进制流存储，接受CDR内容
	 * @param in
	 */
	public static void saveFinalFile(byte[] in) {
		String filepath = "src/main/resources";
		String cdrTag = "UNKNOWN";
		if (judgeCdrType(in) == CdrType.PGW_CDR) {
			cdrTag = "-P-CDR";
		} else if (judgeCdrType(in) == CdrType.SGW_CDR) {
			cdrTag = "-S-CDR";
		}
		
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = null;
		try {
			File dir = new File(filepath);
			if (!dir.exists() && dir.isDirectory()) {// 判断文件目录是否存在
				dir.mkdirs();
			}
			file = new File(filepath + "\\" + df.format(new Date()) + cdrTag + ".dat");
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(in);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * <B><li>以字节为单位读取文件，用于读二进制文件</li></B>
	 * 
	 * @return byte[]
	 * @author BaoXu
	 */
	public static byte[] readFileByBytes(String fileName) {
		File file = new File(fileName);
		InputStream in = null;
		byte[] tempbytes = null;
		try {

			// 一次读多个字节
			in = new FileInputStream(file);
			tempbytes = new byte[showAvailableBytes(in)];
			int byteread = 0;

			// 读入多个字节到字节数组中，byteread为一次读入的字节数
			while ((byteread = in.read(tempbytes)) != -1) {
				//System.out.write(tempbytes, 0, byteread);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
				}
			}
		}
		return tempbytes;
	}

	/**
	 * 显示输入流中还剩的字节数
	 * 
	 */
	private static int showAvailableBytes(InputStream in) {
		int temp = 0;
		try {
			temp = in.available();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return temp;
	}

}
