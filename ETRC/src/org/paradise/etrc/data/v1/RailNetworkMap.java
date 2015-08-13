package org.paradise.etrc.data.v1;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.paradise.etrc.data.Base64EncodingBinaryPart;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.util.image.ImageUtil;

@TGElementType(name="RailNetwork Map")
public class RailNetworkMap extends Base64EncodingBinaryPart {
	BufferedImage image, rgbPixelImage;
	int height;
	int width;
	
	RailNetworkMap() {}
	
	protected byte[] encode() { 
		if (image == null) 
			return new byte[0]; 
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "JPEG", out);
		} catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
		
		byte[] result = out.toByteArray();
		return result;
	}
	
	@Override
	protected void decode(byte[] byteBuffer) {
		image = null;
		try {
			image = ImageIO.read(new ByteArrayInputStream(byteBuffer));
			if (image != null) {
				height = image.getHeight();
				width = image.getWidth();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.gc();
	};
	
	public void loadFromFile(File imageFile) throws IOException {
		image = null;
		rgbPixelImage = null;
		System.gc();
		
		image = ImageIO.read(imageFile);
		height = image.getHeight();
		width = image.getWidth();
	}
	
	public BufferedImage getImage() {
		if (rgbPixelImage == null) {
			rgbPixelImage = ImageUtil.toRrbPixelImage(image);
			
			image.flush();
			image = rgbPixelImage;
			System.gc();
		}
		
		return rgbPixelImage;
	}

	@Override
	protected void loadComplete() {
		// TODO Auto-generated method stub
		super.loadComplete();
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void initElements() {
//		loadFromFile(new File("/Volumes/MacData/Users/zhiyuangong/Hobby/Railroad/列车运行图/map.jpg"));
	}

}
