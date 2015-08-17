package org.paradise.etrc.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.skb.ETRCSKB;
import org.paradise.etrc.data.v1.RailNetworkChart;

import static com.zearon.util.debug.DebugUtil.DEBUG;
import static com.zearon.util.debug.DebugUtil.IS_DEBUG;

import static org.paradise.etrc.ETRC.__;

/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class FindTrainsDialog extends DialogBase {
	private static final long serialVersionUID = -609136239072858202L;

	private ProgressPanel progressPanel = new ProgressPanel();

	private JLabel msgLabel;
	
	private MainFrame mainFrame;

	public FindTrainsDialog(MainFrame parent) {
		super(parent);
		mainFrame = parent;

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setTitle(__("Finding Train Information"));

		ImageIcon image = new ImageIcon(org.paradise.etrc.MainFrame.class.getResource("/pic/msg.png"));
		JLabel imageLabel = new JLabel();
		imageLabel.setIcon(image);
		
		msgLabel = new JLabel(__("Removing existing train data, please wait..."));
		msgLabel.setFont(new java.awt.Font("Dialog", 0, 12));

		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BorderLayout());
		messagePanel.setBorder(new EmptyBorder(4,4,4,4));
		messagePanel.add(imageLabel, BorderLayout.WEST);
		messagePanel.add(msgLabel, BorderLayout.CENTER);

		JPanel rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout());
		rootPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		rootPanel.add(progressPanel, BorderLayout.SOUTH);
		rootPanel.add(messagePanel, BorderLayout.CENTER);

		this.getContentPane().add(rootPanel, BorderLayout.CENTER);

		int w = imageLabel.getPreferredSize().width
				+ msgLabel.getPreferredSize().width + 40;
		int h = messagePanel.getPreferredSize().height
				+ progressPanel.getPreferredSize().height + 20;
		this.setSize(w, h);

		setResizable(false);
	}

	public void findTrains() {
		Dimension dlgSize = this.getPreferredSize();
		Dimension frmSize = mainFrame.getSize();
		Point loc = mainFrame.getLocation();

		this.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				         (frmSize.height - dlgSize.height) / 2 + loc.y);
		this.setModal(true);
		this.pack();
		
		new Thread(new LoadingThread()).start();
		setVisible(true);
	}

	class LoadingThread implements Runnable {
		public void run() {
			hold(500);
			RailNetworkChart networkChart = mainFrame.trainGraph.currentNetworkChart;
			networkChart.clearTrains();
			mainFrame.getChartView().repaint();
			
			msgLabel.setText(__("Loading built-in timetable..."));
			
			ETRCSKB skb = mainFrame.getSKB();
//			
//			List<Train> trains = skb.findTrains(mainFrame.trainGraph.railNetwork.getAllRailroadLines());		//skb.findTrains(mainFrame.chart.trunkCircuit);
//
			Instant instant1 = null, instant2 = null;
			if (IS_DEBUG())
				instant1= Instant.now();
			
			progressPanel.autoMakeProgress(false);
			
			msgLabel.setText(__("Please wait while imporing train information..."));

			skb.findTrains(mainFrame.trainGraph.currentNetworkChart, 
					status 	-> msgLabel.setText(status),
					max 	-> progressPanel.setRange(max), 
					value 	-> progressPanel.setValue(value));
			mainFrame.trainGraph.setTrainTypeByNameForAllTrains();
			
			msgLabel.setText(__("Creating train route sections..."));
			mainFrame.trainGraph.currentNetworkChart.createTrainRouteSectionsForTrainsInAllLines();
			
			mainFrame.allTrainsView.setModel(mainFrame.trainGraph);
			
			msgLabel.setText(__("Done."));
			progressPanel.terminate();
			
			if (IS_DEBUG())
				instant2= Instant.now();
			
			DEBUG("Benchmark: [import circuit]: %d", instant2.toEpochMilli() - instant1.toEpochMilli());
			
			mainFrame.getChartView().repaint();
			mainFrame.getSheetView().updateData();
	        mainFrame.getRunView().refresh();

			hold(200);

			setVisible(false);
			dispose();
		}
		
		private void hold(long time) {
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
			}
		}
	}
	
    class ProgressPanel extends JPanel
    {
		private static final long serialVersionUID = -2195298227589227704L;
		private JProgressBar pb;
		private Timer timer;

        public ProgressPanel() {
            pb = new JProgressBar();
            pb.setPreferredSize(new Dimension(200,20));
            
            // 设置定时器，用来控制进度条的处理
            timer = new Timer(1,new ActionListener() { 
                int counter = 0;
                public void actionPerformed(ActionEvent e) {
                    counter++;
                    pb.setValue(counter);
                    Timer t = (Timer)e.getSource();
                    
                    // 如果进度条达到最大值重新开发计数
                    if (counter == pb.getMaximum())
                    {
                        t.stop();
                        counter =0;
                        t.start();
                    }                    
                }
            });
            timer.start();
            
            //pb.setStringPainted(true);
            pb.setMinimum(0);
            pb.setMaximum(300);
            pb.setBackground(Color.white);
            pb.setForeground(Color.red);
                        
            this.add(pb);                
        }
        
        public void autoMakeProgress(boolean enabled) {
            pb.setMinimum(0);
            pb.setMaximum(300);
            pb.setValue(0);
            
            if (enabled)
            	timer.start();
            else
            	timer.stop();
        }
        
        public void setRange(int maximum) {
        	timer.stop();
        	
        	pb.setMinimum(0);
        	pb.setMaximum(maximum);
        }
        
        public void setValue(int value) {
        	pb.setValue(value);
        }
        
        /**
         * 设置进度条的数据模型
         */
        public void setProcessBar(BoundedRangeModel rangeModel) {
            pb.setModel(rangeModel);
        }
        
        public void gotoEnd() {
			pb.setValue(pb.getMaximum());
        }
        
        public void terminate() {
        	timer.stop();
        	gotoEnd();
        	repaint();
        }
    }
	
}
