package org.paradise.etrc.view.network;

import java.awt.Font;
import java.util.Timer;
import java.util.TimerTask;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.nulldevice.NullSoundDevice;
import de.lessvoid.nifty.render.batch.BatchRenderDevice;
import de.lessvoid.nifty.renderer.jogl.input.JoglInputSystem;
import de.lessvoid.nifty.renderer.jogl.render.JoglBatchRenderBackendCoreProfileFactory;
import de.lessvoid.nifty.renderer.jogl.render.JoglRenderDevice;
import de.lessvoid.nifty.spi.render.RenderDevice;
import de.lessvoid.nifty.spi.time.TimeProvider;

/**
 * For our purposes only two of the GLEventListeners matter. Those would be
 * init() and display(). 为了达到我们的目的，GLEventListener中只有两个方法有用。
 * 它们是init()和display()。
 */
public class MapEditScene implements GLEventListener {
	private AnimatorBase animator;
	private GLWindow window;
	private Nifty nifty;
	
	private double theta = 0;
	private double s = 0;
	private double c = 0;
	
	private TextRenderer tr = new TextRenderer(new Font("SansSerif", Font.BOLD, 14), true, true);
	
	private int fps = 0, framesDrawn = 0;
	private Timer fpsTimer = new Timer("Fps timer thread", true);
	
	private static MapEditScene instance;
	public static MapEditScene getInstance() {
//		if (instance == null) {
//			instance = new MapScene();
//		}
		
		return instance;
	}

	public MapEditScene(GLWindow window, AnimatorBase animator) {
		this.window = window;
		this.animator = animator;
		
		instance = this;
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		// Initialize nifty OpneGL GUI control lib.
		@SuppressWarnings("deprecation")
		RenderDevice renderDevice = new JoglRenderDevice(window);
		JoglInputSystem inputSystem = new JoglInputSystem(window);
		window.addMouseListener(inputSystem);
		window.addKeyListener(inputSystem);
		nifty = new Nifty(renderDevice, new NullSoundDevice(), inputSystem, new TimeProvider() {
			
			@Override
			public long getMsTime() {
				return System.currentTimeMillis();
			}
		});
		nifty.fromXml("org/paradise/etrc/view/network/MapEditor.xml", "start");
		
		// OpenGL settings stuff
		GL gl = drawable.getGL();
		// setup v-sync
		//		gl.setSwapInterval(1);		
		System.out.println(gl.glGetString(GL.GL_VENDOR));
		
		// Initialize FPS painting
		fpsTimer.scheduleAtFixedRate(new TimerTask() {
			int counter = 0;
			@Override
			public void run() {
				fps = framesDrawn;
				framesDrawn = 0;
			}
		}, 100, 1000);
		
		// Start animation
//		animator.start();
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
//		gl.glPushMatrix();
		
		nifty.update();
		nifty.render(true);
		
		udpate();
		render(drawable);
		
    
    ++ framesDrawn;
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		System.out.println(String.format("x=%d, y=%d, width=%d, height=%d", x, y, width, height));
	}

	private void udpate() {
    theta += 0.01;
    s = Math.sin(theta);
    c = Math.cos(theta);
	}

	private void render(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
    
//		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
    // draw a triangle filling the window
    gl.glBegin(GL.GL_TRIANGLES);
    gl.glColor3f(1, 0, 0);
    gl.glVertex2d(-c, -c);
    gl.glColor3f(0, 1, 0);
    gl.glVertex2d(0, c);
    gl.glColor3f(0, 0, 1);
    gl.glVertex2d(s, -s);
    gl.glEnd();
    
    GLAnimatorControl animator = drawable.getAnimator();
    if (animator != null && animator.isAnimating()) {
	    tr.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
	    // optionally set the color
	    tr.setColor(1.0f, 0.2f, 0.2f, 0.8f);
	    tr.draw(String.format("FPS: %.1f", animator.getLastFPS()/*fps*/), 10, 10);
	    // ... more draw commands, color changes, etc.
	    tr.endRendering();
    }
    
    gl.glFlush();
	}
}