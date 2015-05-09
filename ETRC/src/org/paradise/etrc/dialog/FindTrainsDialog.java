package org.paradise.etrc.dialog;

import static org.paradise.etrc.ETRC.__;
import static org.paradise.etrc.ETRCUtil.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

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
import org.paradise.etrc.data.Train;
import org.paradise.etrc.data.skb.ETRCSKB;

/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class FindTrainsDialog extends JDialog {
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
			mainFrame.currentLineChart.clearTrains();
			mainFrame.chartView.repaint();
			
			msgLabel.setText(__("Please wait while imporing train information..."));
			
			ETRCSKB skb = mainFrame.getSKB();
			List<Train> trains = skb.findTrains(mainFrame.currentLineChart.allCircuits.stream());		//skb.findTrains(mainFrame.chart.trunkCircuit);

			Instant instant1 = null, instant2 = null;
			if (IS_DEBUG())
				instant1= Instant.now();
			
			trains.stream().parallel()
				.filter(train-> (train.isDownTrain(mainFrame.currentLineChart.railroadLine) > 0))
				.forEach(train-> {
					mainFrame.currentLineChart.addTrain(train);
					msgLabel.setText(String.format(__("Importing train information %s"), train.getTrainName()));
				});
			
			if (IS_DEBUG())
				instant2= Instant.now();
			
			DEBUG("Benchmark: [import circuit]: %d", instant2.toEpochMilli() - instant1.toEpochMilli());
			
			
//			for(int i=0; i<trains.size(); i++) {
//				Train loadingTrain = (Train) (trains.get(i));
//				
//				if(loadingTrain.isDownTrain(mainFrame.chart.trunkCircuit, false) > 0) {
//					mainFrame.chart.addTrain(loadingTrain);
//					
//					msgLabel.setText(String.format(__("Importing train information %s"), loadingTrain.getTrainName()));
//					hold(50);
//				}
//			}
			
			mainFrame.chartView.repaint();
			mainFrame.sheetView.updateData();
	        mainFrame.runView.refresh();

			progressPanel.gotoEnd();

			hold(200);
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

        public ProgressPanel() {
            pb = new JProgressBar();
            pb.setPreferredSize(new Dimension(200,20));
            
            // 设置定时器，用来控制进度条的处理
            Timer time = new Timer(1,new ActionListener() { 
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
            time.start();
            
            //pb.setStringPainted(true);
            pb.setMinimum(0);
            pb.setMaximum(300);
            pb.setBackground(Color.white);
            pb.setForeground(Color.red);
                        
            this.add(pb);                
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
    }
	
}
