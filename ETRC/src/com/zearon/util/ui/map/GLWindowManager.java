package com.zearon.util.ui.map;

import java.awt.Component;
import java.awt.Container;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowStateListener;
import java.util.HashMap;
import java.util.function.Supplier;

import com.jogamp.newt.event.awt.AWTAdapter;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import static com.zearon.util.debug.DebugUtil.DEBUG;
import static com.zearon.util.debug.DebugUtil.DEBUG_MSG;

public class GLWindowManager {
	private static HashMap<Container, GLWindowBinding> managedWindows = new HashMap<>();
	private static GLWindowBinding windowCurrentOnTop;
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
	
	/**
	 * AWT or Swing windows are not good at OpenGL performance aspects. 
	 * As a result, a work around is figured out: creating a NEWT window 
	 * and made it floating above an anchor AWT/Swing container. <br/>
	 * There is only one GLWindow at most visible at any specific time.
	 * Once a floating GLWidnow is visible, others are made invisible.
	 * @param topLevelAwtSwingWindow
	 * @param container
	 * @param margin
	 * @return
	 */
	public static GLWindow createFloatingGLWindow(java.awt.Window topLevelAwtSwingWindow, Container container, int margin) {

		GLWindow window = GLWindow.create(glcaps);
		window.setPosition(200, 200);
		GLWindowBinding windowBinding = new GLWindowBinding(topLevelAwtSwingWindow, container, window);
		
		managedWindows.put(container, windowBinding);
		
		return window;
	}
	
	public static void switchTopContainer(Container topContainer) {
		managedWindows.forEach((container, windowBinding) -> {
			if (isChild(container, topContainer)) {
				windowBinding.syncWindowWithContainer();
				windowBinding.setOnTop(true);
			} else {
				windowBinding.setOnTop(false);
				windowCurrentOnTop = windowBinding;
			}
		});
	}

	public static <T extends Window> T decorateDialog(T window) {
		window.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				disableOnTopGLWindow();
			}
			@Override
			public void componentHidden(ComponentEvent e) {
				enableOnTopGLWindow();
			}
		});
		
		return window;
	}
	
	public static <T> T showDialogOnFloatingGLWindow(Supplier<T> showDialogAction) {
		disableOnTopGLWindow();
		T result = showDialogAction.get();
		enableOnTopGLWindow();
		return result;
	}
	
	private static void disableOnTopGLWindow() {
		DEBUG_MSG("Dialog Decorator: Window %s. Temporarily disable the on top GL window.", "shwon");
		GLWindowManager.setCurrentOnTopWindowEnabled(false);
	}
	
	private static void enableOnTopGLWindow() {
		DEBUG_MSG("Dialog Decorator: Window %s. Re-enable the on top GL window.", "hidden");
		GLWindowManager.setCurrentOnTopWindowEnabled(false);
	}
	
	public static void setCurrentOnTopWindowEnabled(boolean enabled) {
		if (windowCurrentOnTop != null) {
			windowCurrentOnTop.setTempOnTop(enabled);
		}
	}
	
	private static boolean isChild(Component com, Container container) {
		while (com != null && com != container) {
			if (com.getParent() == container)
				return true;
			else
				com = com.getParent();
		}
		
		return false;
	}
}

class GLWindowBinding {
	private java.awt.Window topLevelWindow;
	private Container container;
	private GLWindow window;
	private boolean onTop, onTopEnabled;
	private boolean activated, iconified;
	
	public GLWindowBinding(java.awt.Window topLevelWindow, Container container, GLWindow window) {
		this.topLevelWindow = topLevelWindow;
		this.container = container;
		this.window = window;
		
		this.onTopEnabled = true;
		
		setupWindow();
	}

	public GLWindow getWindow() {
		return window;
	}

	public void setWindow(GLWindow window) {
		this.window = window;
	}

//	public boolean isShown() {
//		return shown;
//	}
//
//	public void setShown(boolean shown) {
//		this.shown = shown;
//		window.setVisible(shown);
//	}

	public boolean isOnTop() {
		return onTop;
	}

	public void setOnTop(boolean onTop) {
		this.onTop = onTop;
		updateOnTopStatus();
	}
	
	public void setTempOnTop(boolean onTopEnabled) {
		this.onTopEnabled = onTopEnabled;
		updateOnTopStatus();
	}
	
	private void setupWindow() {

		window.setUndecorated(true);
		window.setAlwaysOnTop(true);
		window.setVisible(true);
		
		topLevelWindow.addWindowFocusListener(new WindowFocusListener() {
			
			@Override
			public void windowLostFocus(WindowEvent e) {
//				System.out.println("top level window LOST FOCUS");
				if (e.getNewState() == WindowEvent.WINDOW_ICONIFIED) {
					// It seems that it is not working on OS X
//					System.out.println("top level window ICONIFIED");

					iconified();
				}
				deactivated();
			}
			
			@Override
			public void windowGainedFocus(WindowEvent e) {
//				System.out.println("top level window GAINED FOCUS");
				if (e.getNewState() == WindowEvent.WINDOW_DEICONIFIED) {
					// It seems that it is not working on OS X
//					System.out.println("top level window DEICONIFIED");
					
					deiconified();
				}
				activated();
			}
		});
		
		topLevelWindow.addComponentListener(new ComponentListener() {
			@Override public void componentShown(ComponentEvent e) {}
			@Override public void componentHidden(ComponentEvent e) {}
			@Override public void componentResized(ComponentEvent e) {}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				syncWindowWithContainer();
			}
		});
		
		container.addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent event) {
				DEBUG("map pane resized");
				syncWindowWithContainer();
				container.revalidate();
			}

			@Override public void componentMoved(ComponentEvent e) {
				syncWindowWithContainer();
			}
			
			@Override public void componentShown(ComponentEvent e) {}
			@Override public void componentHidden(ComponentEvent e) {}
		});
	}
	
	private void updateOnTopStatus() {
		window.setAlwaysOnTop(activated && onTop && onTopEnabled);
	}
	
	private void iconified() {
		iconified = true;
		window.setVisible(false);
	}
	
	private void deiconified() {
		iconified = false;
		window.setVisible(true);
	}
	
	private void deactivated() {
		activated = false;
		window.setAlwaysOnTop(false);
	}
	
	private void activated() {
		activated = true;
		updateOnTopStatus();
	}
	
	public void syncWindowWithContainer() {
		syncWindowWithContainer(window, topLevelWindow, container);
	}
	
	public static void syncWindowWithContainer(GLWindow window, java.awt.Window topLevelWindow, Container container) {
		try {
			Rectangle bounds = container.getBounds();
			Point topLevelWindowLocation = topLevelWindow.getLocationOnScreen();
			Point containtLocation = container.getLocationOnScreen();
			window.setTopLevelPosition(containtLocation.x + 2, containtLocation.y + 2);
			window.setTopLevelSize(bounds.width - 4, bounds.height - 4);
		} catch (IllegalComponentStateException ex) {
//			System.err.println(ex);
		}
	}
}
