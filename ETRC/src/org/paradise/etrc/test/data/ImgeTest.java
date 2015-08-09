package org.paradise.etrc.test.data;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ImgeTest {

	public ImgeTest() {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoad() throws FileNotFoundException, IOException {
		File picture = new File("/Volumes/MacData/Users/zhiyuangong/Hobby/Railroad/列车运行图/map.jpg");  
        BufferedImage sourceImg =ImageIO.read(new FileInputStream(picture));   
        System.out.println(String.format("%.1f",picture.length()/1024.0));  
        System.out.println(sourceImg.getWidth());  
        System.out.println(sourceImg.getHeight());  
	}

}
