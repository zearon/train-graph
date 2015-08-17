package com.zearon.util.ui.widget;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.Border;

public class StatusBar extends JLabel {

	private static final long	serialVersionUID	= -625030288614635292L;

	private final int defaultTempStatusTimeInMillis = 3000;
	private String normalStatusString = "Status: Normal";
	private Timer timer = new Timer(defaultTempStatusTimeInMillis, e -> setText(normalStatusString));
	
	
	public StatusBar() {
		super();
		init();
	}

	public StatusBar(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
		init();
	}

	public StatusBar(Icon image) {
		super(image);
		init();
	}

	public StatusBar(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		init();
		setNormalStatusString(text);
	}

	public StatusBar(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		init();
		setNormalStatusString(text);
	}

	public StatusBar(String text) {
		super(text);
		init();
		setNormalStatusString(text);
	}
	
	private void init() {
		Border border = BorderFactory.createLoweredBevelBorder();

		setBorder(border);
		setFont(new java.awt.Font("Dialog", 0, 12));
	}

	public String getNormalStatusString() {
		return normalStatusString;
	}

	public void setNormalStatusString(String normalStatusString) {
		this.normalStatusString = normalStatusString;
	}

	public int getDefaultTempStatusTimeInMillis() {
		return defaultTempStatusTimeInMillis;
	}

	public String getStatus() {
		return getText();
	}

	public void setStatus(String status) {
		setStatus(status, false, defaultTempStatusTimeInMillis);
	}
	
	public void setTempStatus(String status) {
		setStatus(status, true, defaultTempStatusTimeInMillis);
	}
	
	public void setTempStatus(String status, int statusRemainingTimeInMillis) {
		setStatus(status, true, statusRemainingTimeInMillis);
	}
	
	private void setStatus(String status, boolean isTempStatus, int delay) {
		
		if (timer.isRunning())
			timer.stop();
		
		setText(status);
		
		if (isTempStatus) {
			timer.setDelay(delay);
			timer.start();
		}
	}
}
