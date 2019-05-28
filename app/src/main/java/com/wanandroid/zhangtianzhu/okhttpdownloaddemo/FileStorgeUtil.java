package com.wanandroid.zhangtianzhu.okhttpdownloaddemo;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * @author yif
 */
public class FileStorgeUtil {
	
	private static final int ERROR = -1;
	/**如果路径不存在，返回参数*/
	public static final String NULL_PATH  = "NO_PATH";
	//没有可用路径，提示语为：“sd卡不可用”
    public static final String TOO_SMART  = "TOO_SMART";
    //存储空间不足，提示语为：“存储空间不足”
	public static final String NULL_PATH_STR  = "sd卡不可用";
	//没有可用路径，提示语为：“sd卡不可用”
	public static final String TOO_SMART_STR  = "存储空间不足";
	//存储空间不足，提示语为：“存储空间不足”

    public static final String PACKAGE_PATH = "com.yif.ztz";

	/**
	 * 优先sd卡，后内存存储
	 * @param size	文件大小，单位为B
	 * @return app根目录
	 */
	public static String selectFirstExrernal(long size) {	//单位为B
		if(externalMemoryAvailable()) {	//sd卡存在
			if(size > getAvailableExternalMemorySize()) {	//size 大于sd卡剩余存储空间
				return judgeInternal(size);
			} else {	
				return Environment.getExternalStorageDirectory()
							+ "/" + PACKAGE_PATH + "/";
			}
		} else {
			return judgeInternal(size);
		}
	}
	
	/**
	 * 优先内存后sd卡存储
	 * @param size	文件大小,单位为B
	 * @return app根目录
	 */
	public static String selectFirstInternal(long size) {	//单位为B
		if(size > getAvailableInternalMemorySize()) { // size 大于 手机内部存储空间
			return judgeExrernal(size);
		} else {
			return "/data/data/"+PACKAGE_PATH+"/";
		}
	}
	
	/**
	 * 判断内存大小够不够
	 * @param size
	 * @return
	 */
	public static String judgeInternal(long size) {
		if(size > getAvailableInternalMemorySize()) {
			return TOO_SMART;
		}else{
			return "/data/data/"+PACKAGE_PATH+"/";
		}
	}
	
	
	/**
	 * 判断sd卡能不能用及大小够不够
	 * @param size
	 * @return
	 */
	public static String judgeExrernal(long size) {
		if(externalMemoryAvailable()) {	//sd卡存在
			if(size > getAvailableExternalMemorySize()) {	//size 大于sd卡剩余存储空间
				return TOO_SMART;
			} else {	
				return Environment.getExternalStorageDirectory()
							+ "/" + PACKAGE_PATH + "/";
			}
		} else {
			return NULL_PATH;
			}
		}
	
	/**
	 * 判断SD卡是否存在
	 * @return
	 */
	public static boolean externalMemoryAvailable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}
	
	/**
     * 获取手机内部剩余存储空间
     * 
     * @return
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize =0;
        long availableBlocks=0;
        if (getAndroidApi()>17) {
			blockSize = stat.getBlockSizeLong();
			availableBlocks = stat.getAvailableBlocksLong();
		}else{
			 blockSize = stat.getBlockSize();
			 availableBlocks = stat.getAvailableBlocks();
		}
        return availableBlocks * blockSize;
    }

    /**
     * 获取手机内部总的存储空间
     * 
     * @return
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize =0;
        long totalBlocks =0;
        if (getAndroidApi()>17) {
			blockSize = stat.getBlockSizeLong();
			totalBlocks = stat.getBlockCountLong();
		}else{
			 blockSize = stat.getBlockSize();
			 totalBlocks = stat.getBlockCount();
		}

        return totalBlocks * blockSize;
    }


    /**
     * 获取SDCARD剩余存储空间
     * 
     * @return
     */
    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize =0;
            long availableBlocks =0;
            if (getAndroidApi()>17) {
            	blockSize = stat.getBlockSizeLong();
				availableBlocks = stat.getAvailableBlocksLong();
			}else{
				blockSize = stat.getBlockSize();
            	availableBlocks = stat.getAvailableBlocks();
			}
            return availableBlocks * blockSize;
        } else {
            return ERROR;
        }
    }

    /**
     * 获取SDCARD总的存储空间
     * 
     * @return
     */
    public static long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize =0;
            long totalBlocks =0;
            
            if (getAndroidApi()>17) {
				blockSize = stat.getBlockSizeLong();
				totalBlocks = stat.getBlockCountLong();
			}else{
				blockSize = stat.getBlockSize();
				totalBlocks = stat.getBlockCount();
			}
            return totalBlocks * blockSize;
        } else {
            return ERROR;
        }
    }

    /**
     * 获取系统总运行内存
     * 
     * @param context 可传入应用程序上下文。
     * @return 总内存大单位为B。
     */
    public static long getTotalMemorySize(Context context) {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            return Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024l;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取当前可用运行内存，返回数据以字节为单位。
     * 
     * @param context 可传入应用程序上下文。
     * @return 当前可用内存单位为B。
     */
    public static long getAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    private static DecimalFormat fileIntegerFormat = new DecimalFormat("#0");
    private static DecimalFormat fileDecimalFormat = new DecimalFormat("#0.#");

    /**
     * 单位换算
     * 
     * @param size 单位为B
     * @param isInteger 是否返回取整的单位
     * @return 转换后的单位
     */
    public static String formatFileSize(long size, boolean isInteger) {
        DecimalFormat df = isInteger ? fileIntegerFormat : fileDecimalFormat;
        String fileSizeString = "0M";
        if (size < 1024 && size > 0) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1024 * 1024) {
            fileSizeString = df.format((double) size / 1024) + "K";
        } else if (size < 1024 * 1024 * 1024) {
            fileSizeString = df.format((double) size / (1024 * 1024)) + "M";
        } else {
            fileSizeString = df.format((double) size / (1024 * 1024 * 1024)) + "G";
        }
        return fileSizeString;
    }
    
    /**
     * 获取api版本号
     * @return
     */
    public static int getAndroidApi(){
    	return Build.VERSION.SDK_INT;
    }

}
