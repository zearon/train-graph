package com.zearon.util.ui.map;

import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.function.Supplier;

import com.jogamp.newt.event.awt.AWTAdapter;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import static org.paradise.etrc.ETRCUtil.DEBUG;

public class GLWindowManager {
	private static HashMap<Container, GLWindowBinding> managedWindows = new HashMap<>();
	private static GLProfile	glp;
	private static GLCapabilities	glcaps;
	
//	private static Supplier<Boolean> 

	public static void initOpenGL() {
		glp = GLProfile.getDefault();
		glcaps = new GLCapabilities(glp);
		glcaps.setDoubleBuffered(true);
	}
	

	public static GLWindow createGLWindow() {
		GLWindow window = GLWindow.create(glcaps);
		window.setVisible(true);
		
		return window;
	}
	
	public static GLWindow createGLWindowForContainer(java.awt.Window topLevelAwtSwingWindow, Container container, int margin) {

		GLWindow window = GLWindow.create(glcaps);
		GLWindowBinding windowBinding = new GLWindowBinding(topLevelAwtSwingWindow, container, window);
		
		managedWindows.put(container, windowBinding);
		
		return window;
	}
	
	public static void switchTopContainer(Container container) {
		
	}
}

class GLWindowBinding {
	private java.awt.Window topLevelWindow;
	private Container container;
	private GLWindow window;
	private boolean shown;
	private boolean onTop;
	
	public GLWindowBinding(java.awt.Window topLevelWindow, Container container, GLWindow window) {
		this.topLevelWindow = topLevelWindow;
		this.container = container;
		this.window = window;
		
		setupWindow();
	}

	public GLWindow getWindow() {
		return window;
	}

	public void setWindow(GLWindow window) {
		this.window = window;
	}

	public boolean isShown() {
		return shown;
	}

	public void setShown(boolean shown) {
		this.shown = shown;
	}

	public boolean isOnTop() {
		return onTop;
	}

	public void setOnTop(boolean onTop) {
		this.onTop = onTop;
	}
	
	private void setupWindow() {

		window.setUndecorated(true);
		window.setAlwaysOnTop(true);
		window.setVisible(true);
		
		topLevelWindow.addWindowListener(new java.awt.event.WindowAdapter() {
			
			@Override
			public void windowIconified(java.awt.event.WindowEvent e) {
				if (window != null) {
					window.setAlwaysOnTop(false);
					window.setVisible(false);
				}
			}
			
			@Override
			public void windowDeiconified(java.awt.event.WindowEvent e) {
				if (window != null) {
					window.setAlwaysOnTop(true);
					window.setVisible(true);
				}
			}
			
			@Override
			public void windowDeactivated(java.awt.event.WindowEvent e) {
				if (window != null) {
					window.setAlwaysOnTop(false);
				}
			}
			
			@Override
			public void windowActivated(java.awt.event.WindowEvent e) {
				if (window != null) {
					window.setAlwaysOnTop(true);
				}
			}
		});
		
		topLevelWindow.addComponentListener(new ComponentListener() {
			@Override public void componentShown(ComponentEvent e) {}
			@Override public void componentResized(ComponentEvent e) {}
			@Override public void componentHidden(ComponentEvent e) {}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				syncWindowWithContainer(window, topLevelWindow, container);
			}
		});
		
		container.addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent event) {
				DEBUG("map pane resized");
				syncWindowWithContainer(window, topLevelWindow, container);
				container.revalidate();
			}

			@Override public void componentMoved(ComponentEvent e) {
				syncWindowWithContainer(window, topLevelWindow, container);
			}
			
			@Override public void componentShown(ComponentEvent e) {}
			@Override public void componentHidden(ComponentEvent e) {}
		});
	}
	
	public static void syncWindowWithContainer(GLWindow window, java.awt.Window topLevelWindow, Container container) {
		try {
			Rectangle bounds = container.getBounds();
			Point topLevelWindowLocation = topLevelWindow.getLocationOnScreen();
			Point containtLocation = container.getLocationOnScreen();
			window.setTopLevelPosition(containtLocation.x + 2, containtLocation.y + 2);
			window.setTopLevelSize(bounds.width - 4, bounds.height - 4);
		} catch (Exception ex) {
			System.err.println(ex);
		}
	}
}
