package com.zearon.util.image;

import java.awt.image.ColorModel;

public class GrayFilterColorModel extends FilterColorModel {

	public GrayFilterColorModel(ColorModel colorModel) {
		super(colorModel);
	}

	@Override
	public int getRed(int pixel) {
		return getGray(pixel);
	}

	@Override
	public int getGreen(int pixel) {
		return getGray(pixel);
	}

	@Override
	public int getBlue(int pixel) {
		return getGray(pixel);
	}

	@Override
	public int getAlpha(int pixel) {
		return colorModel.getAlpha(pixel);
	}
	
	private int getGray(int pixel) {
		int r = colorModel.getRed(pixel);
		int g = colorModel.getGreen(pixel);
		int b = colorModel.getBlue(pixel);
		
		return (int) (0.30 * r + 0.59 * g + 0.10 * b);
	}

}
