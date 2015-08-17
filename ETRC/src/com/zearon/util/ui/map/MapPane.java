package com.zearon.util.ui.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.paradise.etrc.ETRC;
import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.view.network.MapEditScene;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import com.zearon.util.ui.widget.TransparentComponent;
import com.zearon.util.ui.widget.TransparentLabel;
import com.zearon.util.ui.widget.ZoomSlider;

import de.lessvoid.nifty.Nifty;
import static com.zearon.util.debug.DebugUtil.DEBUG;
import static com.zearon.util.debug.DebugUtil.DEBUG_MSG;

import static org.paradise.etrc.ETRC.__;
import javax.swing.BoxLayout;
import javax.swing.JButton;

public class MapPane extends JDesktopPane implements ComponentListener, Runnable {

	private static final long	serialVersionUID	= -969315949208967536L;
	
	private TrainGraph trainGraph;
	private boolean	ui_inited;
	private MapPanel mapPanel;
	private GLWindow mapCanvas;
	private Component mapArea;
	
	private int fps = 0;
	private boolean animationOn = false;
	private boolean repaintThreadStarted = false;
	private Thread repaintThread;
	
	private JPanel panelViewOptions;
	private JSlider sliderBGMapTransparency;
	private JToggleButton tglbtnNightMode;
	private JToggleButton tglbtnGrayMode;
	private JToggleButton tglbtnAnnimation;
	private JToggleButton tglbtnShowFps;
	private JToggleButton tglbtnViewOptions;
	private TransparentLabel lblFps;
	private ZoomSlider sliderZoom;
	
	private Vector<TransparentComponent> transparentComponents = new Vector<> ();

	/**
	 * Create the panel.
	 */
	public MapPane(TrainGraph trainGraph) {
		setBackground(Color.WHITE);
		
		if (trainGraph != null)
			setModel(trainGraph);
		
		initUI();
		initAnimation();
		
		ui_inited=true;
	}

	public int getAlphaValue() {
		return mapPanel != null ? mapPanel.getAlphaValue() : 0;
	}

	public void setAlphaValue(int alpha_value) {
		if (mapPanel != null)
			mapPanel.setAlphaValue(alpha_value);
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		
		if (ui_inited) {
			if (mapPanel != null)
				mapPanel.setModel(trainGraph);
		}
	}

	private void initUI() {
		setDoubleBuffered(true);
		setLayout(null);
		addComponentListener(this);
		
//		mapPanel = new MapPanel(trainGraph, bgScrollPane);
//		bgScrollPane.setViewportView(mapPanel);
		
		tglbtnViewOptions = new JToggleButton(__("View Options"));
		tglbtnViewOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean pushedDown = tglbtnViewOptions.isSelected();
				panelViewOptions.setVisible(pushedDown);
			}
		});
		setLayer(tglbtnViewOptions, -1);
		tglbtnViewOptions.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		tglbtnViewOptions.setBounds(0, 6, 93, 29);
		add(tglbtnViewOptions);
		
		panelViewOptions = new JPanel();
		panelViewOptions.setBorder(new LineBorder(Color.LIGHT_GRAY));
		panelViewOptions.setVisible(false);
		setLayer(panelViewOptions, -1);
		panelViewOptions.setBounds(93, 35, 213, 123);
		add(panelViewOptions);
		panelViewOptions.setLayout(null);
		
		JLabel lblTransparencyOfBackgound = new JLabel(__("Transparency of backgound map"));
		lblTransparencyOfBackgound.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		lblTransparencyOfBackgound.setBounds(6, 6, 201, 16);
		panelViewOptions.add(lblTransparencyOfBackgound);
		
		sliderBGMapTransparency = new JSlider();
		sliderBGMapTransparency.setValue(200);
		sliderBGMapTransparency.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				do_changeBGMapTransparency();
			}
		});
		sliderBGMapTransparency.setPreferredSize(new Dimension(120, 29));
		sliderBGMapTransparency.setMinimumSize(new Dimension(120, 29));
		sliderBGMapTransparency.setMaximumSize(new Dimension(150, 29));
		sliderBGMapTransparency.setMaximum(255);
		sliderBGMapTransparency.setMajorTickSpacing(25);
		sliderBGMapTransparency.setBounds(0, 19, 213, 29);
		panelViewOptions.add(sliderBGMapTransparency);
		
		tglbtnNightMode = new JToggleButton(__("Night Mode"));
		tglbtnNightMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_changeViewMode();
			}
		});
		tglbtnNightMode.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		tglbtnNightMode.setBounds(6, 47, 93, 29);
		panelViewOptions.add(tglbtnNightMode);
		
		tglbtnGrayMode = new JToggleButton(__("Gray Mode"));
		tglbtnGrayMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_changeViewMode();
			}
		});
		tglbtnGrayMode.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		tglbtnGrayMode.setBounds(111, 47, 93, 29);
		panelViewOptions.add(tglbtnGrayMode);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(6, 75, 201, 12);
		panelViewOptions.add(separator);
		
		tglbtnAnnimation = new JToggleButton(__("Annimation"));
		tglbtnAnnimation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_changeAnimation();
			}
		});
		tglbtnAnnimation.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		tglbtnAnnimation.setBounds(6, 88, 93, 29);
		panelViewOptions.add(tglbtnAnnimation);
		
		tglbtnShowFps = new JToggleButton("Show FPS");
		tglbtnShowFps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_changeShowFPS();
			}
		});
		tglbtnShowFps.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		tglbtnShowFps.setBounds(111, 88, 93, 29);
		panelViewOptions.add(tglbtnShowFps);
		
		lblFps = new TransparentLabel("FPS: 0");
		setLayer(lblFps, -2);
		lblFps.setVisible(false);
		lblFps.setBounds(93, 10, 151, 16);
		add(lblFps);
		transparentComponents.add(lblFps);
		
		sliderZoom = new ZoomSlider(new double[]{0.1, 0.2, 0.5, 0.8, 1, 1.2, 1.5, 2, 3, 5});
		sliderZoom.setBounds(10, 35, 60, 188);
		add(sliderZoom);
		sliderZoom.setBackground(Color.WHITE);
		sliderZoom.setBGOpaque(sliderBGMapTransparency.getValue());
		sliderZoom.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				do_changeZoomValue();
			}
		});
		sliderZoom.setValue(4);
		sliderZoom.setLabelStyle(true, true);
		sliderZoom.setOrientation(SwingConstants.VERTICAL);
		setLayer(sliderZoom, -1);
		transparentComponents.add(sliderZoom);
		
		JPanel panel = new JPanel();
		panel.setBounds(140, 191, 396, 206);
		add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JButton btnNewButton = new JButton("New button");
		panel.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("New button");
		panel.add(btnNewButton_1);
		
		if (trainGraph != null) {
			createMapCanvas(false);
		}
	}
	
	private void createMapCanvas(boolean initialVisibility) {
		mapCanvas = GLWindowManager.createFloatingGLWindow(MainFrame.getInstance(), this, 2);
		mapCanvas.addGLEventListener(new MapEditScene(mapCanvas, animator));
		animator.add(mapCanvas);
		
		mapCanvas.setVisible(initialVisibility);
	}

	private FPSAnimator animator = new FPSAnimator(60);
	private void initAnimation() {
		repaintThread = new Thread(this);
		repaintThread.setDaemon(true);
		
		animator.setFPS(60);
		animator.setUpdateFPSFrames(60, null);
		animator.start();
	}
	
	public void run() {
		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				lblFps.setText("FPS: " + DecimalFormat.getNumberInstance().format(fps));
				fps = 0;
			}
		}, 10, 1000);

		if (ui_inited) {
			while (true) {
				if (mapPanel != null) {
					mapPanel.repaint();
//					mapCanvas.display();
				}
				++fps;

				if (!animationOn) {
					synchronized (repaintThread) {
						try {
							DEBUG("Animation paused");
							repaintThread.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	@Override
	public void componentResized(ComponentEvent event) {
		DEBUG("map pane resized");
//		bgScrollPane.setBounds(0, 0, bounds.width, bounds.height);
//		mapArea.setBounds(0, 0, bounds.width, bounds.height);
		
		syncMapCanvasPisitionWithWindow();
		
		Point viewOptionsBtnPosition = tglbtnViewOptions.getLocation();
		panelViewOptions.setLocation(viewOptionsBtnPosition.x + 93, viewOptionsBtnPosition.y + 29); // y + 4 平齐
		
		revalidate();
	}

	@Override public void componentMoved(ComponentEvent e) {
		syncMapCanvasPisitionWithWindow();
	}
	
	@Override public void componentShown(ComponentEvent e) {}
	@Override public void componentHidden(ComponentEvent e) {}

	private void syncMapCanvasPisitionWithWindow() {
	}
	
	public void setBGMapTransparency(int alpha) {
		alpha = alpha & 0xFF;
		
		sliderBGMapTransparency.setValue(alpha);
		do_changeBGMapTransparency();
	}
	
	private void do_changeBGMapTransparency() {
		if (!ui_inited)
			return;
		
		int alpha = sliderBGMapTransparency.getValue();
		
		if (mapPanel != null)
			mapPanel.setAlphaValue(alpha);

		transparentComponents.forEach(tc -> {
			int tcAlpha = 150 - alpha;
			if (tcAlpha < 0) tcAlpha = 0;
			tc.setBGOpaque(tcAlpha);
		});
		
		repaint();
	}
	
	private void do_changeViewMode() {
		if (mapPanel != null)
			mapPanel.setViewMode(tglbtnNightMode.isSelected(), tglbtnGrayMode.isSelected());
	}
	
	private void do_changeAnimation() {
		animationOn = tglbtnAnnimation.isSelected();
		
		if (animationOn) {
			if (repaintThreadStarted) {
				fps = 0;
				synchronized (repaintThread) {
					repaintThread.notify();
				}
				DEBUG("Animation resumed");
			} else {
				repaintThread.start();
				repaintThreadStarted = true;
				DEBUG("Animation started");
			}
		}
	}
	
	private void do_changeShowFPS() {
		boolean on = tglbtnShowFps.isSelected();
		lblFps.setVisible(on);
	}
	
	private void do_changeZoomValue() {
		double zoomValue = sliderZoom.getZoomValue();
		if (mapPanel != null)
			mapPanel.setZoomValue(zoomValue);
	}

	
	public void createGLWindow() {
		GLWindow window = GLWindowManager.createGLWindow();
		window.setSize(300, 300);
		window.setVisible(true);
		window.setTitle("Map Edit");
		window.addGLEventListener(new MapEditScene(window, animator));

		window.addWindowListener(new WindowAdapter() {
			public void windowDestroyNotify(WindowEvent arg0) {
				animator.remove(window);
				DEBUG_MSG("window removed from animator");
			};
		});
		
		animator.add(window);
		window.display();		
	}
}
