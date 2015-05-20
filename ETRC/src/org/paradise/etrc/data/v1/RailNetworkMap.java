package org.paradise.etrc.data.v1;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.paradise.etrc.data.Base64EncodingBinaryPart;
import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.util.data.Tuple2;

import sun.misc.BASE64Decoder;

@TGElementType(name="RailNetwork Map")
public class RailNetworkMap extends Base64EncodingBinaryPart {
	BufferedImage image;
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
		System.gc();
		
		image = ImageIO.read(imageFile);
		height = image.getHeight();
		width = image.getWidth();

	}
	
	
	
	public BufferedImage getImage() {
		return image;
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
