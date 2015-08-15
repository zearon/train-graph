package com.zearon.util.image;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.lang.reflect.Field;

public class ChangableColorModelBufferedImage extends BufferedImage {
	
	public ChangableColorModelBufferedImage(int width, int height, int imageType) {
		super(width, height, imageType);
	}

	public void setColorModel(ColorModel colorModel) {
		try {
			Field f = getClass().getSuperclass().getDeclaredField("colorModel");
			f.setAccessible(true);
			f.set(this, colorModel);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
