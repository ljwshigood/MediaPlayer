package com.mediaplayer.mediaplayerlistener.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

public class FileUtils {
	
	private static FileUtils mInstance ;
	
	private FileUtils(){
		
	}
	
	public File findFileByName(Context context,List<File> fileList,String fileName){
		File retFile = null ;
		for(int i = 0;i < fileList.size() ;i++){
			File file = fileList.get(i) ;
			if(file.getName().contains(fileName)){
				retFile = file ;
				break ;
			}
		}
		return retFile ;
	}
	
	
	public static String readTxtFile(Context context) throws IOException{
		
		String readString = null ;
		StringBuffer sb = new StringBuffer();
		File file = new File(context.getCacheDir()+File.separator+"wanjia.txt");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = br.readLine())!=null){
				sb.append(line);
			}
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			readString = sb.toString() ;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return readString ;
	}
	
	public static void WriteTxtFile(Context context ,String strcontent){
		
      String strContent=strcontent+"\n";
      try {
           File file = new File(context.getCacheDir()+File.separator+"wanjia.txt");
           if (!file.exists()) {
        	   file.createNewFile();
           }
           RandomAccessFile raf = new RandomAccessFile(file, "rw");
           raf.seek(file.length());
           raf.write(strContent.getBytes());
           raf.close();
      } catch (Exception e) {
      		e.printStackTrace() ;
      }
    }
	
	public static FileUtils getInstance(){
		if(mInstance == null){
			mInstance = new FileUtils();
		}
		return mInstance ;
	}
	
	public int currentPosition(Context context,List<File> fileList,String fileName){
		int position = 0 ;
		for(int i = 0;i < fileList.size() ;i++){
			File file = fileList.get(i) ;
			if(file.getName().equals(fileName)){
				i = position ;
				break ;
			}
		}
		return position ;
	}
	

	public static File saveBitmap2Native(Context context, Bitmap bitmap,int type) {
		File file = null;
		try {
			if (type == 0) {
				file = createImageFile(context);
			} else {
				file = createPhotoImageFile(context);
			}
			saveBitmap2file(bitmap, file);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
		}
		return file;
	}

	public static void saveBitmap2file(Bitmap bmp, File file) {

		OutputStream stream = null;
		try {
			stream = new FileOutputStream(file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);

		try {
			stream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static File createPhotoImageFile(Context context)
			throws IOException {

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String timeStamp = format.format(new Date());
		String imageFileName = timeStamp + ".jpg";

		File image = new File(FileUtils.getPhotoDir(context), imageFileName);

		return image;
	}

	private class FileComparator implements Comparator<File> {

		@Override
		public int compare(File lhs, File rhs) {
			if (lhs.lastModified() < rhs.lastModified()) {
				return 1;
			} else {
				return -1;
			}
		}

	}

	private static File createImageFile(Context context) throws IOException {

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String timeStamp = format.format(new Date());
		String imageFileName = timeStamp + ".jpg";

		File image = new File(FileUtils.getWanjiaDir(context), imageFileName);

		return image;
	}

	public ArrayList<File> getAllFiles(File root) {
		ArrayList<File> list = new ArrayList<File>();
		File files[] = root.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					getAllFiles(f);
				} else {
					String name = f.getName();
					if (!name.startsWith(".nomedia")) {
						list.add(f);
					}
				}
			}
		}
		
		Collections.sort(list, new FileComparator());
		
		return list;
	}
	
	/**
	 * 获取保存图片的目录
	 * 
	 * @return
	 */
	public static File getWanjiaDir(Context context) {
		File dir = null;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			dir = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "wanjia");
		} else {
			dir = new File(context.getCacheDir() + File.separator + "wanjia");
		}
		File nomediaFile = new File(dir.getAbsolutePath() + File.separator
				+ ".nomedia");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		if (!nomediaFile.exists()) {
			try {
				nomediaFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dir;
	}

	public static File getPhotoDir(Context context) {
		File dir = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			dir = new File(Environment.getExternalStorageDirectory() + File.separator + "wanjia" + File.separator + "liujw");
		} else {
			dir = new File(context.getCacheDir() + File.separator + "wanjia" + File.separator + "liujw");
		}
		File nomediaFile = new File(dir.getAbsolutePath() + File.separator
				+ ".nomedia");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		if (!nomediaFile.exists()) {
			try {
				nomediaFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dir;
	}

	public static File getMateralDir(Context context) {
		
		File dir = null;
		dir = new File(context.getCacheDir() + File.separator + "wanjia" + File.separator + "material");
		File nomediaFile = new File(dir.getAbsolutePath() + File.separator + ".nomedia");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		if (!nomediaFile.exists()) {
			try {
				nomediaFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dir;
	}
	
	public static File getFontDir(Context context) {
		
		File dir = null;
		dir = new File(context.getCacheDir() + File.separator + "wanjia" + File.separator + "font");
		File nomediaFile = new File(dir.getAbsolutePath() + File.separator + ".nomedia");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		if (!nomediaFile.exists()) {
			try {
				nomediaFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dir;
	}

}
