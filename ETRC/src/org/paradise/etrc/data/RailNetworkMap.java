package org.paradise.etrc.data;
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

import org.paradise.etrc.data.util.Tuple;

import sun.misc.BASE64Decoder;

public class RailNetworkMap extends Base64EncodingBinaryPart<NullPart> {
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
	
	
	

	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	@Override
	protected String getStartSectionString() { return START_SECTION_RAILNETWORK_MAP; }
	@Override
	protected String getEndSectionString() { return END_SECTION_RAILNETWORK_MAP; }
	@Override
	protected Supplier<? extends TrainGraphPart> getConstructionFunc() {
		return RailNetworkMap::new;
	}
	@Override
	public void registerSubclasses() {}

	/* Element array */
	@Override
	protected Vector<NullPart> getTGPElements() {return null;}

	@Override
	protected void addTGPElement(NullPart element) {}

	@Override
	protected boolean isOfElementType(TrainGraphPart part) {return false;}
	
	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {};
}
