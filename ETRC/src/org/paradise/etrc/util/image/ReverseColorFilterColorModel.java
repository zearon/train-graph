package org.paradise.etrc.util.image;

import java.awt.image.ColorModel;

public class ReverseColorFilterColorModel extends FilterColorModel {

	public ReverseColorFilterColorModel(ColorModel colorModel) {
		super(colorModel);
	}

	@Override
	public int getRed(int pixel) {
		return 255 - colorModel.getRed(pixel);
	}

	@Override
	public int getGreen(int pixel) {
		return 255 - colorModel.getGreen(pixel);
	}

	@Override
	public int getBlue(int pixel) {
		return 255 - colorModel.getBlue(pixel);
	}

	@Override
	public int getAlpha(int pixel) {
		return colorModel.getAlpha(pixel);
	}

}
