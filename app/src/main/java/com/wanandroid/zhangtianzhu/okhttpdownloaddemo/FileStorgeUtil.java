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
	/**���·�������ڣ����ز���*/
	public static final String NULL_PATH  = "NO_PATH";
	//û�п���·������ʾ��Ϊ����sd�������á�
    public static final String TOO_SMART  = "TOO_SMART";
    //�洢�ռ䲻�㣬��ʾ��Ϊ�����洢�ռ䲻�㡱
	public static final String NULL_PATH_STR  = "sd��������";
	//û�п���·������ʾ��Ϊ����sd�������á�
	public static final String TOO_SMART_STR  = "�洢�ռ䲻��";
	//�洢�ռ䲻�㣬��ʾ��Ϊ�����洢�ռ䲻�㡱

    public static final String PACKAGE_PATH = "com.yif.ztz";

	/**
	 * ����sd�������ڴ�洢
	 * @param size	�ļ���С����λΪB
	 * @return app��Ŀ¼
	 */
	public static String selectFirstExrernal(long size) {	//��λΪB
		if(externalMemoryAvailable()) {	//sd������
			if(size > getAvailableExternalMemorySize()) {	//size ����sd��ʣ��洢�ռ�
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
	 * �����ڴ��sd���洢
	 * @param size	�ļ���С,��λΪB
	 * @return app��Ŀ¼
	 */
	public static String selectFirstInternal(long size) {	//��λΪB
		if(size > getAvailableInternalMemorySize()) { // size ���� �ֻ��ڲ��洢�ռ�
			return judgeExrernal(size);
		} else {
			return "/data/data/"+PACKAGE_PATH+"/";
		}
	}
	
	/**
	 * �ж��ڴ��С������
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
	 * �ж�sd���ܲ����ü���С������
	 * @param size
	 * @return
	 */
	public static String judgeExrernal(long size) {
		if(externalMemoryAvailable()) {	//sd������
			if(size > getAvailableExternalMemorySize()) {	//size ����sd��ʣ��洢�ռ�
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
	 * �ж�SD���Ƿ����
	 * @return
	 */
	public static boolean externalMemoryAvailable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}
	
	/**
     * ��ȡ�ֻ��ڲ�ʣ��洢�ռ�
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
     * ��ȡ�ֻ��ڲ��ܵĴ洢�ռ�
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
     * ��ȡSDCARDʣ��洢�ռ�
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
     * ��ȡSDCARD�ܵĴ洢�ռ�
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
     * ��ȡϵͳ�������ڴ�
     * 
     * @param context �ɴ���Ӧ�ó��������ġ�
     * @return ���ڴ��λΪB��
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
     * ��ȡ��ǰ���������ڴ棬�����������ֽ�Ϊ��λ��
     * 
     * @param context �ɴ���Ӧ�ó��������ġ�
     * @return ��ǰ�����ڴ浥λΪB��
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
     * ��λ����
     * 
     * @param size ��λΪB
     * @param isInteger �Ƿ񷵻�ȡ���ĵ�λ
     * @return ת����ĵ�λ
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
     * ��ȡapi�汾��
     * @return
     */
    public static int getAndroidApi(){
    	return Build.VERSION.SDK_INT;
    }

}
