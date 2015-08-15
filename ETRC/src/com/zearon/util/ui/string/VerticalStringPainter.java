package com.zearon.util.ui.string;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Vector;

import com.zearon.util.data.Tuple2;

public class VerticalStringPainter {
	private boolean textFromLeftToRight = true;
	private int hCharMargin = 1;
	private int vCharMargin = 1;
	private int alignment = 1;
	
	private Rectangle bounds;
	private int top = 0;
	private int left = 0;
	private int standardCharWidth;
	private Vector<Tuple2<String, Boolean>> words = new Vector<>();
	
	public void setProperties(int horizontalCharMargin,
			int verticalCharMargin, boolean textFromLeftToRight) {
		
		this.hCharMargin = horizontalCharMargin;
		this.vCharMargin = verticalCharMargin;
		this.textFromLeftToRight = textFromLeftToRight;
	}
	
	/**
	 * Set text alignment. Default is center-alignment.
	 * @param alignment 0 for left-alignment, 1 for center-alignment, 
	 * and 2 for right-alignment.
	 */
	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}
	
	public void setBounds(Graphics2D g) {
		bounds = (Rectangle) g.getClipBounds().clone();
	}
	
	public void setBounds(Rectangle rect) {
		this.bounds = rect;
	}
	
	public void paintString(Graphics2D g, String str) {
		prepareString(str);
		startPainting(g);
		
		paintStringContinue(g, str);
	}
	
	public void paintStringContinue(Graphics2D g, String str) {
		if (words.size() == 0)
			prepareString(str);
		
		for (int index = 0; index < words.size(); ++ index) {
			Tuple2<String, Boolean> wordTuple = words.get(index);
			String word = wordTuple.A;
			boolean rotationFlag = wordTuple.B;
			if ("\n".equals(word)) {
				newLine(false);
				continue;
			} else {
				paintWord(g, word, rotationFlag);
			}
		}
	}
	
	private void prepareString(String str) {
		words.clear();
		if (str == null)
			return;
		
		str = str.replace("\r\n", "\n").replace("\n\r", "\n").replace("\r", "\n");
		int strLen = str.length();
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < strLen; ++ i) {
			char ch = str.charAt(i);
			boolean rotateChar = shouldRotateCharacter(ch);
			
			if (rotateChar) {
				if (' ' == ch || '\n' == ch || '\t' == ch) {
					if(sb.length() > 0) {
						words.add(Tuple2.of(sb.toString(), true));
						sb = new StringBuilder();
					}
					words.add(Tuple2.of("" + ch, true));
				} else {
					sb.append(ch);
				}
			} else {
				if(sb.length() > 0) {
					words.add(Tuple2.of(sb.toString(), true));
					sb = new StringBuilder();
				}
				words.add(Tuple2.of("" + ch, false));
			}
		}

		if(sb.length() > 0) {
			words.add(Tuple2.of(sb.toString(), true));
		}
	}
	 
	private void startPainting(Graphics2D g) {
//		FontMetrics fm = g.getFontMetrics();
		top = vCharMargin;
		standardCharWidth = g.getFontMetrics().charWidth('你');
		
		// 模拟画一遍,得出行数,计算left,使得文字居中.
		left = 0; top = 0;
		for (int index = 0; index < words.size(); ++ index) {
			Tuple2<String, Boolean> wordTuple = words.get(index);
			String word = wordTuple.A;
			boolean rotationFlag = wordTuple.B;
			if ("\n".equals(word)) {
				newLine(true);
				continue;
			} else {
				paintWordFake(g, word, rotationFlag);
			}
		}

		int doubleMargin = bounds.width - left - standardCharWidth - 2 * hCharMargin;
		int margin = doubleMargin / 2;
		
		top = vCharMargin;
		if (textFromLeftToRight) {
			if (margin > 0 && alignment == 1) {
				// center-alignment
				left = hCharMargin + margin;
			}
			else if (margin > 0 && alignment == 2) {
				// right-alignment
				left = bounds.width - left - hCharMargin - standardCharWidth;
			}
			else {
				// left-alignment
				left = hCharMargin;
			}
		} else {
			if (margin > 0 && alignment == 1) {
				// center-alignment
				left = bounds.width - margin - hCharMargin - standardCharWidth;
			}
			else if (margin > 0 && alignment == 0) {
				// left-alignment
				left = bounds.width - doubleMargin - hCharMargin - standardCharWidth;
			}
			else {
				// right-alignment
				left = bounds.width - hCharMargin - standardCharWidth;
			}
		}
	}
	
	private void paintWord(Graphics2D g, String word, boolean shouldRotate) {
		paintWord(g, word, shouldRotate, false, false);
	}
	
	private void paintWordFake(Graphics2D g, String word, boolean shouldRotate) {
		paintWord(g, word, shouldRotate, true, false);
	}
	
	private void paintWord(Graphics2D g, String word, boolean shouldRotate, boolean fake, boolean quit) {
		FontMetrics fm = g.getFontMetrics();
		int deltaY = shouldRotate ? fm.stringWidth(word) : fm.getHeight() - fm.getLeading() - fm.getDescent() + vCharMargin;
		if (deltaY + top > bounds.height - vCharMargin && !quit) {
			newLine(fake);
			paintWord(g, word, shouldRotate, fake, true);
			return;
		}
		
		if (fake) {
			top += deltaY;
			return;
		}
		
		// Move the origin of coordination system to (left, top), 
		// which is the new 
		AffineTransform origTransform = g.getTransform();
		
		int baselineX, baselineY;
		if (shouldRotate) {
			baselineX = left + fm.getDescent() - 2;
			baselineY = top + fm.getHeight() - fm.getAscent();
			g.translate(baselineX, baselineY);
			g.rotate(Math.PI * 0.5);
		} else {
			baselineX = left;
			baselineY = top + fm.getAscent();
			g.translate(baselineX, baselineY);
		}

//		DEBUG("%d (%b)\t%s", baselineY, shouldRotate, word);
		g.drawString(word, 0, 0);
		top += deltaY;

		// Restore coordination system
		g.setTransform(origTransform);
	}
	
	private void newLine(boolean fake) {
		top = vCharMargin;
		if (textFromLeftToRight || fake) {
			left += hCharMargin + standardCharWidth;
		} else {
			left -= hCharMargin + standardCharWidth;
		}
	}
	
	private boolean shouldRotateCharacter(char character) {
		return (int) character <= 255;
	}
	
	
	
	
	
	
	
	
	/*************************************************
	 * Unit Test Cases
	 ************************************************/
//	@Test
//	public void testPrepareString() {
//		prepareString("Hello, 你好! 我\n是123号字符串.");
//		System.out.println(String.join("|", words));
//		System.out.println(wordRotationFlags.stream().map(String::valueOf).collect(Collectors.joining("|")));
//	}
}
