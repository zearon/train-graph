package com.zearon.util.image;

import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

public abstract class FilterColorModel extends ColorModel {

	protected ColorModel colorModel;

	public FilterColorModel(ColorModel colorModel) {
		super(colorModel.getPixelSize());
		this.colorModel = colorModel;
	}
	
	public int getPixelSize() {
		return colorModel.getPixelSize();
	}

	public int getComponentSize(int componentIdx) {
		return colorModel.getComponentSize(componentIdx);
	}

	public int[] getComponentSize() {
		return colorModel.getComponentSize();
	}

	public int getTransparency() {
		return colorModel.getTransparency();
	}

	public int getNumComponents() {
		return colorModel.getNumComponents();
	}

	public int getNumColorComponents() {
		return colorModel.getNumColorComponents();
	}

	public int getRGB(int pixel) {
		return colorModel.getRGB(pixel);
	}

	public int getRed(Object inData) {
		return colorModel.getRed(inData);
	}

	public int getGreen(Object inData) {
		return colorModel.getGreen(inData);
	}

	public int getBlue(Object inData) {
		return colorModel.getBlue(inData);
	}

	public int getAlpha(Object inData) {
		return colorModel.getAlpha(inData);
	}

	public int getRGB(Object inData) {
		return colorModel.getRGB(inData);
	}

	public Object getDataElements(int rgb, Object pixel) {
		return colorModel.getDataElements(rgb, pixel);
	}

	public int[] getComponents(int pixel, int[] components, int offset) {
		return colorModel.getComponents(pixel, components, offset);
	}

	public int[] getComponents(Object pixel, int[] components, int offset) {
		return colorModel.getComponents(pixel, components, offset);
	}

	public int[] getUnnormalizedComponents(float[] normComponents,
			int normOffset, int[] components, int offset) {
		return colorModel.getUnnormalizedComponents(normComponents, normOffset,
				components, offset);
	}

	public float[] getNormalizedComponents(int[] components, int offset,
			float[] normComponents, int normOffset) {
		return colorModel.getNormalizedComponents(components, offset,
				normComponents, normOffset);
	}

	public int getDataElement(int[] components, int offset) {
		return colorModel.getDataElement(components, offset);
	}

	public Object getDataElements(int[] components, int offset, Object obj) {
		return colorModel.getDataElements(components, offset, obj);
	}

	public int getDataElement(float[] normComponents, int normOffset) {
		return colorModel.getDataElement(normComponents, normOffset);
	}

	public Object getDataElements(float[] normComponents, int normOffset,
			Object obj) {
		return colorModel.getDataElements(normComponents, normOffset, obj);
	}

	public float[] getNormalizedComponents(Object pixel, float[] normComponents,
			int normOffset) {
		return colorModel
				.getNormalizedComponents(pixel, normComponents, normOffset);
	}

	public boolean equals(Object obj) {
		return colorModel.equals(obj);
	}

	public int hashCode() {
		return colorModel.hashCode();
	}

	public ColorModel coerceData(WritableRaster raster,
			boolean isAlphaPremultiplied) {
		return colorModel.coerceData(raster, isAlphaPremultiplied);
	}

	public boolean isCompatibleRaster(Raster raster) {
		return colorModel.isCompatibleRaster(raster);
	}

	public WritableRaster createCompatibleWritableRaster(int w, int h) {
		return colorModel.createCompatibleWritableRaster(w, h);
	}

	public SampleModel createCompatibleSampleModel(int w, int h) {
		return colorModel.createCompatibleSampleModel(w, h);
	}

	public boolean isCompatibleSampleModel(SampleModel sm) {
		return colorModel.isCompatibleSampleModel(sm);
	}

	public void finalize() {
		colorModel.finalize();
	}

	public WritableRaster getAlphaRaster(WritableRaster raster) {
		return colorModel.getAlphaRaster(raster);
	}

	public String toString() {
		return colorModel.toString();
	}

}
