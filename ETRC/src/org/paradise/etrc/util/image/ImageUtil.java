package org.paradise.etrc.util.image;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;

import static org.paradise.etrc.ETRCUtil.DEBUG_MSG;

public class ImageUtil {
	
	public static BufferedImage toRrbPixelImage(BufferedImage img) {
		if (img == null)
			return null;
		
		long startTime = System.nanoTime(), duration = 0;
		
		// 设定 BufferedImage 的宽与高
		int width = img.getWidth(), height = img.getHeight();
		int size = width * height;
		// 创建数组，用以保存对应 BufferedImage 的像素集合
		int [] pixels = new int [size];
		// 以指定数组创建出指定大小的 DataBuffer
		DataBuffer dataBuffer = new DataBufferInt(pixels, size);
		// 创建一个 WritableRaster 对象，用以 管理光栅
		WritableRaster raster = Raster.createPackedRaster (dataBuffer, width, height,
				width, new int [] { 0xFF0000, 0xFF00, 0xFF, 0xFF000000 }, null );
//		// 创建一个 24 位的 RGB 色彩模型，并填充相应的 R 、 G 、 B 掩码
//		DirectColorModel directColorModel = new DirectColorModel(24, 0xFF0000, 0xFF00, 0xFF);
		// 以下为 32 位 RGB 色彩模型
		DirectColorModel directColorModel = new DirectColorModel(32,
				0xFF0000, 0xFF00, 0xFF, 0xFF000000);
		// 生成 BufferedImage, 预设 Alpha ，无配置
		BufferedImage image = new BufferedImage(directColorModel, raster, true,
				null);
		
		Graphics g = image.getGraphics();
		g.drawImage(img, 0, 0, null);
		g.finalize();
		
		duration = System.nanoTime() - startTime;
		DEBUG_MSG("toRrbPixelImage takes %,d nanoseconds", duration);
		
		System.gc();
		
		return image;
	}

	public static BufferedImage cloneRgbPixelImage(BufferedImage img) {
		if (img == null)
			return null;
		
		long startTime = System.nanoTime(), t1, t2, end;

		//
		int [] oldPixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
		
		// 设定 BufferedImage 的宽与高
		int width = img.getWidth(), height = img.getHeight();
		int size = width * height;
		// 创建数组，用以保存对应 BufferedImage 的像素集合
		int [] pixels = Arrays.copyOf(oldPixels, oldPixels.length);		//new int [size];
		
		// 以指定数组创建出指定大小的 DataBuffer
		DataBuffer dataBuffer = new DataBufferInt(pixels, size);
		// 创建一个 WritableRaster 对象，用以 管理光栅
		WritableRaster raster = Raster.createPackedRaster (dataBuffer, width, height,
				width, new int [] { 0xFF0000, 0xFF00, 0xFF, 0xFF000000 }, null );
//		// 创建一个 24 位的 RGB 色彩模型，并填充相应的 R 、 G 、 B 掩码
//		DirectColorModel directColorModel = new DirectColorModel(24, 0xFF0000, 0xFF00, 0xFF);
		// 以下为 32 位 RGB 色彩模型
		DirectColorModel directColorModel = new DirectColorModel(32,
				0xFF0000, 0xFF00, 0xFF, 0xFF000000);
		t1 = System.nanoTime();
		// 生成 BufferedImage, 预设 Alpha ，无配置
		BufferedImage image = new BufferedImage(directColorModel, raster, true,
				null);
		t2 = System.nanoTime();
		
		end = System.nanoTime();
		DEBUG_MSG("cloneRgbPixelImage takes %,d (copy time is %,d) nanoseconds", end - startTime, t2 - t1);
		
		System.gc();
		
		return image;
	}

	public static BufferedImage cloneImage(BufferedImage img) {
		if (img == null)
			return null;
		
		long startTime = System.nanoTime(), t1, t2, end;
		
		BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		img2.setData(img.getData());
		
		end = System.nanoTime();
		DEBUG_MSG("cloneRgbPixelImage takes %,d nanoseconds", end - startTime);
		
		System.gc();
		
		return img2;
	}
	
	public static void copyImageData(BufferedImage src, BufferedImage dest) {
		if (src == null || dest == null)
			return;
		
//		dest.setData(src.getData());
		int[] srcPixels = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
		int[] destPixels = ((DataBufferInt) dest.getRaster().getDataBuffer()).getData();
		int size = srcPixels.length;
		for (int i = 0; i < size; ++ i) {
			destPixels[i] = srcPixels[i];
		}
	}
	
	public static void reverseColor(BufferedImage img) {
		processImageByPixels(img, ImageUtil::reverseColorByPixel);
	}
	
	public static void toGrayImage(BufferedImage img) {
		processImageByPixels(img, ImageUtil::toGrayImageByPixel);
	}
	
	public static void reverseColorAndToGrayImage(BufferedImage img) {
		processImageByPixels(img, 
				(argb) -> toGrayImageByPixel(reverseColorByPixel(argb)));
	}
	
	private static void processImageByPixels(BufferedImage img, PixelConverter pixelConverter) {
		int[] dataBuffer = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
		int pixelRGB;
		int[] argb = new int[4];
		
		long time1, time2;
		int i = 0, pixelCount = dataBuffer.length;
		time1 = System.nanoTime();
		for (; i < pixelCount; ++ i) {
			pixelRGB = dataBuffer[i];
			argb[0] = (pixelRGB & 0xff000000) >> 24;
			argb[1] = (pixelRGB & 0x00ff0000) >> 16;
			argb[2] = (pixelRGB & 0x0000ff00) >> 8;
			argb[3] = pixelRGB & 0x000000ff;
			
			pixelConverter.convertPixelByARGB(argb);
			
			dataBuffer[i] = (argb[0] << 24) | (argb[1] << 16) | (argb[2] << 8) | argb[3];
		}
		time2 = System.nanoTime();
		DEBUG_MSG("Processing %,d pixels takes %,d nanoseconds.", pixelCount, (time2 - time1));
	}
	
	private static int[] reverseColorByPixel(int[] argb) {
		argb[0] = argb[0];				// a
		argb[1] = 255 - argb[1];	// r
		argb[2] = 255 - argb[2];	// g
		argb[3] = 255 - argb[3];	// b
		
		return argb;
	}
	
	private static int[] toGrayImageByPixel(int[] argb) {
		int gray = (int) (0.30 * argb[1] + 0.59 * argb[2] + 0.10 * argb[3]);
		if (gray > 255)
			gray = 255;

		argb[0] = argb[0];	// a
		argb[1] = gray;			// r
		argb[2] = gray;			// r
		argb[3] = gray;			// r
		
		return argb;
	}
}

@FunctionalInterface
interface PixelConverter {
	public int[] convertPixelByARGB(int[] argb);
}