package org.paradise.etrc.view.alltrains;

import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.DEBUG;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.paradise.etrc.ETRC;
import org.paradise.etrc.MainFrame;
import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.controller.action.UIAction;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.dialog.MessageBox;
import org.paradise.etrc.filter.CSVFilter;
import org.paradise.etrc.filter.TRFFilter;
import org.paradise.etrc.util.Config;
import org.paradise.etrc.util.ui.table.DefaultJEditTableModel;
import org.paradise.etrc.util.ui.table.JEditTable;

/**
 * @author lguo@sina.com
 * @version 1.0
 */

public class TrainView extends JPanel {
	private static final long serialVersionUID = 4016578609920190434L;

	private TrainTable table;
	private JTextField tfNameU;
	private JTextField tfNameD;
	private JTextField tfName;

	private MainFrame mainFrame;	

	Consumer<Train> editCallback = null;
	Train originalTrain;

//	public boolean isCanceled = false;
	
	private static Train empty_train;
	
	static {
		empty_train = TrainGraphFactory.createInstance(Train.class);
		empty_train.name = __("");
	}

	public TrainView() {
//		super(_mainFrame, _train.getTrainName(), true);

		mainFrame = MainFrame.getInstance();

		table = new TrainTable();
		table.setModel(new TrainTableModel(empty_train));

		try {
			jbInit();
//			pack();
			
			setModel(empty_train);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setModel(Train _train) {
		if (_train == null) {
			originalTrain = empty_train;
//			setEnabled(false);
		} else {
			originalTrain = _train;
//			setEnabled(true);
		}
		
		loadModel();
	}
	
	private void loadModel() {		
		table.setModel(new TrainTableModel(originalTrain));
//		((TrainTableModel) table.getModel()).setTrain(originalTrain);
		
		tfNameU.setText(((TrainTableModel) table.getModel()).myTrain.trainNameUp);
		tfNameD.setText(((TrainTableModel) table.getModel()).myTrain.trainNameDown);
		tfName.setText(((TrainTableModel) table.getModel()).myTrain
				.getTrainName());
		
//		((TrainTableModel) table.getModel()).fireTableDataChanged();

		revalidate();
	}

	public Train getTrain() {
		Train train = ((TrainTableModel) table.getModel()).myTrain;
		// train.startStation = train.stops[0].stationName;
		// train.terminalStation = train.stops[train.stopNum - 1].stationName;
		train.trainNameDown = tfNameD.getText().trim();
		train.trainNameUp = tfNameU.getText().trim();
		train.name = tfName.getText().trim();
		return train;
	}

	private void jbInit() throws Exception {
		table.setFont(new Font("Dialog", 0, 12));
		table.getTableHeader().setFont(new Font("Dialog", 0, 12));

//		JButton btColor = new JButton(__("Color"));
//		btColor.setFont(new Font("dialog", 0, 12));
//		btColor.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				if (table.getCellEditor() != null)
//					table.getCellEditor().stopCellEditing();
//
//				doSetColor(getTrain());
//			}
//		});

		JButton btLoad = new JButton(__("Load"));
		btLoad.setFont(new Font("dialog", 0, 12));
		btLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getCellEditor() != null)
					table.getCellEditor().stopCellEditing();

				Train loadingTrain = doLoadTrain();
				if (loadingTrain != null) {
//					if (loadingTrain.color == null) {
//						Color c = ((TrainTableModel) table.getModel()).myTrain.color;
//						loadingTrain.color = c;
//					}

					((TrainTableModel) table.getModel()).myTrain = loadingTrain;
					tfName.setText(loadingTrain.getTrainName());
					tfNameD.setText(loadingTrain.trainNameDown);
					tfNameU.setText(loadingTrain.trainNameUp);
				}
			}
		});

		JButton btSave = new JButton(__("Save"));
		btSave.setFont(new Font("dialog", 0, 12));
		btSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getCellEditor() != null)
					table.getCellEditor().stopCellEditing();

				Train savingTrain = ((TrainTableModel) table.getModel()).myTrain;
				savingTrain.trainNameDown = tfNameD.getText().trim();
				savingTrain.trainNameUp = tfNameU.getText().trim();
				savingTrain.name = tfName.getText().trim();

				doSaveTrain(savingTrain);
			}
		});

		JButton btOK = new JButton(__("OK"));
		btOK.setFont(new Font("dialog", 0, 12));
		btOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getCellEditor() != null)
					table.getCellEditor().stopCellEditing();

				Train train = ((TrainTableModel) table.getModel()).myTrain;
				train.trainNameDown = tfNameD.getText().trim();
				train.trainNameUp = tfNameU.getText().trim();
				train.name = tfName.getText().trim();

//				isCanceled = false;
				// TrainDialog.this.setVisible(false);
//				TrainDialog.this.dispose();
				
				if (editCallback != null) {
					editCallback.accept(train);
				}
			}
		});

		JButton btCancel = new JButton(__("Cancel"));
		btCancel.setFont(new Font("dialog", 0, 12));
		btCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				isCanceled = true;
				// TrainDialog.this.setVisible(false);
//				TrainDialog.this.dispose();

				if (editCallback != null) {
					loadModel();
				}
			}
		});

		JButton btWeb = new JButton(__("Get From Web"));
		btWeb.setFont(new Font("dialog", 0, 12));
		btWeb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tfName.getText().trim().equals("")) {
					new MessageBox(
							__("Must input train number before trying to get data from web."))
							.showMessage();
					return;
				}
				if (table.getCellEditor() != null)
					table.getCellEditor().stopCellEditing();

				String proxyAddress = Config.getInstance().getHttpProxyServer();
				int proxyPort = Config.getInstance().getHttpProxyPort();
				
				Color color = table.getBackground();
				
				int proxyPort0 = proxyPort;
				new Thread( () -> {
					Train loadingTrain = doLoadTrainFromWeb(
							tfName.getText().trim(), proxyAddress, proxyPort0);

					if (TrainView.this.isVisible()) {
						if (loadingTrain != null ) {
//							if (loadingTrain.color == null) {
//								Color c = ((TrainTableModel) table.getModel()).myTrain.color;
//								loadingTrain.color = c;
//							}

							((TrainTableModel) table.getModel()).myTrain = loadingTrain;
							tfName.setText(loadingTrain.getTrainName());
							tfNameD.setText(loadingTrain.trainNameDown);
							tfNameU.setText(loadingTrain.trainNameUp);

							table.revalidate();
						} else {
							new MessageBox(
									__("Unable to get train information from web."))
									.showMessage();
						}
						
						btWeb.setText(__("Get From Web"));
						btWeb.setForeground(Color.BLUE);
						btWeb.updateUI();
					
						table.setBackground(color);
						table.updateUI();
					}
				} ).start();

				table.setBackground(Color.decode("0xB4FFDD"));
				table.updateUI();
				
				btWeb.setText(__("Searching..."));
				btWeb.setForeground(Color.RED);
				btWeb.updateUI();

			}
		});

		JPanel buttonPanel = new JPanel();
//		buttonPanel.add(btColor);
		buttonPanel.add(btLoad);
		buttonPanel.add(btSave);
		buttonPanel.add(btOK);
		buttonPanel.add(btCancel);
		buttonPanel.add(btWeb);

//		JPanel rootPanel = new JPanel();
		setLayout(new BorderLayout());
		add(buildTrainPanel(), BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

//		getContentPane().add(rootPanel);
	}

	private JPanel buildTrainPanel() {
		JButton btDel = new JButton(__("Delete"));
		btDel.setFont(new Font("dialog", 0, 12));
		btDel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getCellEditor() != null)
					table.getCellEditor().stopCellEditing();

				((TrainTableModel) table.getModel()).myTrain.delStop(table
						.getSelectedRow());

				table.revalidate();
			}
		});

		JButton btAdd = new JButton(__("Add(Before)"));
		btAdd.setFont(new Font("dialog", 0, 12));
		btAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// table.getCellEditor().stopCellEditing();
				String name = __("Station");
				String arrive = "00:00";
				String leave = "00:00";
				((TrainTableModel) table.getModel()).myTrain.insertStop(
						TrainGraphFactory.createInstance(Stop.class, name)
							.setProperties(arrive, leave, false),
						table.getSelectedRow());

				table.revalidate();
			}
		});

		JButton btApp = new JButton(__("Add(After)"));
		btApp.setFont(new Font("dialog", 0, 12));
		btApp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// table.getCellEditor().stopCellEditing();
				int curIndex = table.getSelectedRow();
				if (curIndex < 0)
					return;

				String name = __("Station");
				String arrive = "00:00";
				String leave = "00:00";
				((TrainTableModel) table.getModel()).myTrain.insertStop(
						TrainGraphFactory.createInstance(Stop.class, name)
							.setProperties(arrive, leave, false), 
						curIndex + 1);

				table.revalidate();
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(btAdd);
		buttonPanel.add(btApp);
		buttonPanel.add(btDel);

		JLabel lbNameU = new JLabel(__("Up-going:"));
		lbNameU.setFont(new Font("dialog", 0, 12));
		JLabel lbNameD = new JLabel(__("Down-going"));
		lbNameD.setFont(new Font("dialog", 0, 12));
		JLabel lbName = new JLabel(__("Train number:"));
		lbName.setFont(new Font("dialog", 0, 12));

		tfNameU = new JTextField(4);
		tfNameU.setFont(new Font("dialog", 0, 12));
//		tfNameU.setText(((TrainTableModel) table.getModel()).myTrain.trainNameUp);
		tfNameD = new JTextField(4);
		tfNameD.setFont(new Font("dialog", 0, 12));
//		tfNameD.setText(((TrainTableModel) table.getModel()).myTrain.trainNameDown);
		tfName = new JTextField(12);
		tfName.setFont(new Font("dialog", 0, 12));
//		tfName.setText(((TrainTableModel) table.getModel()).myTrain
//				.getTrainName());

		JPanel namePanel = new JPanel();
		namePanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		namePanel.add(lbName);
		namePanel.add(tfName);
		namePanel.add(lbNameD);
		namePanel.add(tfNameD);
		namePanel.add(lbNameU);
		namePanel.add(tfNameU);

//		JScrollPane spTrain = new JScrollPane(table);

		JPanel trainPanel = new JPanel();
		trainPanel.setLayout(new BorderLayout());
		trainPanel.add(namePanel, BorderLayout.NORTH);
		trainPanel.add(table.getContainerPanel(), BorderLayout.CENTER);
		trainPanel.add(buttonPanel, BorderLayout.SOUTH);

		return trainPanel;
	}

	protected void doSaveTrain(Train savingTrain) {
		JFileChooser chooser = new JFileChooser();
		ETRC.setFont(chooser);

		chooser.setDialogTitle(__("Save Train"));
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new CSVFilter());
		chooser.addChoosableFileFilter(new TRFFilter());
		chooser.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		chooser.setApproveButtonText(__("Save "));
		try {
			File recentPath = new File(Config.getInstance().getLastTrainPath());
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {
		}

		String savingName = savingTrain.getTrainName().replace('/', '_');
		chooser.setSelectedFile(new File(savingName));

		int returnVal = chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String f = chooser.getSelectedFile().getAbsolutePath();
			if (!f.endsWith(".trf"))
				f += ".trf";

			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(f), "UTF-8"));
				savingTrain.writeTo(out);

				out.close();

				Config.getInstance().setLastTrainPath(chooser
						.getSelectedFile().getParentFile()
						.getAbsolutePath());
			} catch (IOException ex) {
				System.err.println("Error: " + ex.getMessage());
			}
		}
	}

	protected Train doLoadTrain() {
		JFileChooser chooser = new JFileChooser();
		ETRC.setFont(chooser);

		chooser.setDialogTitle(__("Load Train Information"));
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new CSVFilter());
		chooser.addChoosableFileFilter(new TRFFilter());
		chooser.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		try {
			File recentPath = new File(Config.getInstance().getLastTrainPath());
			if (recentPath.exists() && recentPath.isDirectory())
				chooser.setCurrentDirectory(recentPath);
		} catch (Exception e) {
		}

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();

			Train loadingTrain = TrainGraphFactory.createInstance(Train.class);
			try {
				loadingTrain.loadFromFile2(f.getAbsolutePath());
				Config.getInstance().setLastTrainPath(chooser
						.getSelectedFile().getParentFile()
						.getAbsolutePath());
			} catch (IOException ex) {
				System.err.println("Error: " + ex.getMessage());
			}

			return loadingTrain;
		}

		return null;
	}

	/**
	 * Load train data from 盛名时刻表
	 * http://wap.smskb.com/search.asp?action=cccx&checi=K121
	 * 
	 * @param code
	 * @param proxyAddress
	 * @param proxyPort
	 * @return
	 */
	public static Train doLoadTrainFromWeb(String code, String proxyAddress,
			int proxyPort) {
		Train train = TrainGraphFactory.createInstance(Train.class);
		train.name = "";
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String date = format.format(now);

		if (code.indexOf("/") != -1)
			code = code.split("/")[0];

		String getData = "action=cccx&checi="  + code;
		try {
			Proxy proxy = null;
			if (proxyAddress.equals("") || proxyPort == 0)
				proxy = Proxy.NO_PROXY;
			else
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
						proxyAddress, proxyPort));
			URL url = new URL("http://wap.smskb.com/search.asp?"
					+ getData);

			URLConnection conn = url.openConnection(proxy);
			conn.setConnectTimeout(5000);
			conn.setRequestProperty("User-Agent",
//					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.78.2 (KHTML, like Gecko) Version/7.0.6 Safari/537.78.2");

			conn.setRequestProperty("Referer",
					"http://wap.smskb.com/index2.asp");

			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(
					conn.getOutputStream());
			// wr.write(postData);
			wr.flush();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "GBK"));
			String inputLine;
			String trainName = null;

			while ((inputLine = in.readLine()) != null) {
				DEBUG(inputLine);
				if (trainName == null) {
					Pattern trainNamePattern = Pattern.compile(">([^>]*?)次列车");   // <td width="100%">1112/1113次列车<br>
					Matcher trainNameMather = trainNamePattern.matcher(inputLine);
					trainName = trainNameMather.find() ? trainNameMather.group(1) : null;
					train.name = trainName;
				}
				
				Pattern stopPattern = Pattern.compile("第\\d+站：(.*?)<br>(.*?)到；(.*?)开");   // <td width="100%">第1站：青岛<br>15:04到；15:05开<br>
				Matcher stopMather = stopPattern.matcher(inputLine);
				while (stopMather.find()) {
					Stop stop = TrainGraphFactory.createInstance(Stop.class, stopMather.group(1))
							.setProperties(stopMather.group(2), stopMather.group(3), true);
					train.appendStop(stop);
				}

			}
			in.close();

			String[] names = train.name.split("/");
			for (int i = 0; i < names.length; ++i) {
				if ((names[i].charAt(names[i].length() - 1) - '0') % 2 == 0) {
					train.trainNameUp = names[i];
				} else {
					train.trainNameDown = names[i];
				}
			}

			// Fix wrong start/end time
			train.getStop(0).arrive = train.getStop(0).leave;
			train.getStop(train.getStopNum() - 1).leave = train.getStop(train
					.getStopNum() - 1).arrive;

			return train;
		} catch (Exception e) {
			return null;
		}
	}

	public static Train doLoadTrainFromWeb_12306(String code,
			String proxyAddress, int proxyPort) {
		Train train = TrainGraphFactory.createInstance(Train.class);
		train.name = "";
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String date = format.format(now);

		if (code.indexOf("/") != -1)
			code = code.split("/")[0];

		String getData = "cxlx=cc&date=" + date + "&trainCode=" + code;
		try {
			Proxy proxy = null;
			if (proxyAddress.equals("") || proxyPort == 0)
				proxy = Proxy.NO_PROXY;
			else
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
						proxyAddress, proxyPort));
			URL url = new URL("http://dynamic.12306.cn/TrainQuery/skbcx.jsp?"
					+ getData);

			URLConnection conn = url.openConnection(proxy);
			conn.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
			conn.setRequestProperty("Referer",
					"http://dynamic.12306.cn/TrainQuery/trainInfoByStation.jsp");

			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(
					conn.getOutputStream());
			// wr.write(postData);
			wr.flush();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "UTF-8"));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				if ((inputLine.indexOf("//") == -1)
						&& (inputLine.indexOf("mygrid.addRow") != -1)) {
					String[] items = inputLine.split(",");
					// if (items[4].indexOf("----") != -1) items[4] = items[5];
					// if (items[5].indexOf("----") != -1) items[5] = items[4];
					Stop stop = TrainGraphFactory.createInstance(Stop.class, items[2].split("\\^")[0].trim())
							.setProperties(items[4].trim(), items[5].trim(), true);
					train.appendStop(stop);
					String c = items[3].trim();
					if ((c.charAt(c.length() - 1) - '0') % 2 == 0) {
						if (train.trainNameUp.equals(""))
							train.trainNameUp = c;
					} else {
						if (train.trainNameDown.equals(""))
							train.trainNameDown = c;
					}
					if (train.name.indexOf(c) == -1) {
						if (train.name.equals("")) {
							train.name = c;
						} else {
							train.name += ("/" + c);
						}
					}
				}
			}
			in.close();

			String[] names = train.name.split("/");
			for (int i = 0; i < names.length; ++i) {
				if ((names[i].charAt(names[i].length() - 1) - '0') % 2 == 0) {
					train.trainNameUp = names[i];
				} else {
					train.trainNameDown = names[i];
				}
			}

			// Fix wrong start/end time
			train.getStop(0).arrive = train.getStop(0).leave;
			train.getStop(train.getStopNum() - 1).leave = train.getStop(train
					.getStopNum() - 1).arrive;

			return train;
		} catch (Exception e) {
			return null;
		}
	}

//	private void doSetColor(final Train train) {
//		final JColorChooser colorChooser = new JColorChooser();
//		ActionListener listener = new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				train.color = colorChooser.getColor();
//				mainFrame.chartView.panelLines.updateBuffer();
//			}
//		};
//
//		JDialog dialog = JColorChooser.createDialog(mainFrame,
//				__("Select the color for the line"), true, // modal
//				colorChooser, listener, // OK button handler
//				null); // no CANCEL button handler
//		ETRC.setFont(dialog);
//
//		colorChooser.setColor(train.color);
//
//		Dimension dlgSize = dialog.getPreferredSize();
//		Dimension frmSize = mainFrame.getSize();
//		Point loc = mainFrame.getLocation();
//		dialog.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
//				(frmSize.height - dlgSize.height) / 2 + loc.y);
//		dialog.setVisible(true);
//	}

	public void editTrain(Consumer<Train> editCallback) {
//		Dimension dlgSize = getPreferredSize();
//		Dimension frmSize = mainFrame.getSize();
//		Point loc = mainFrame.getLocation();
//		setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
//				(frmSize.height - dlgSize.height) / 2 + loc.y);
//		setVisible(true);
		
		this.editCallback = editCallback;
	}

	public class TrainTableModel extends DefaultJEditTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1014817974495127589L;

		Train myTrain;

		TrainTableModel() {
		}

		TrainTableModel(Train _train) {
			setTrain(_train);
		}
		
		public void setTrain(Train _train) {
			myTrain = _train.copy();
			fireTableDataChanged();
		}

		/**
		 * getColumnCount
		 *
		 * @return int
		 */
		public int getColumnCount() {
			return 4;
		}

		/**
		 * getRowCount
		 *
		 * @return int
		 */
		public int getRowCount() {
			return myTrain.getStopNum();
		}

		/**
		 * isCellEditable
		 *
		 * @param rowIndex
		 *            int
		 * @param columnIndex
		 *            int
		 * @return boolean
		 */
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return (columnIndex == 0) || (columnIndex == 1)
					|| (columnIndex == 2) || (columnIndex == 3);
		}

		/**
		 * getColumnClass
		 *
		 * @param columnIndex
		 *            int
		 * @return Class
		 */
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
			case 1:
			case 2:
				return String.class;
			case 3:
				return Boolean.class;
			default:
				return null;
			}
		}

		/**
		 * getValueAt
		 *
		 * @param rowIndex
		 *            int
		 * @param columnIndex
		 *            int
		 * @return Object
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return myTrain.getStop(rowIndex).name;
			case 1:
				return myTrain.getStop(rowIndex).arrive;
			case 2:
				return myTrain.getStop(rowIndex).leave;
			case 3:
				return Boolean.valueOf(myTrain.getStop(rowIndex).isPassenger);
			default:
				return null;
			}
		}

		protected UIAction getActionAndDoIt(Object aValue, int rowIndex, int columnIndex) {
			return ActionFactory.createTableCellEditActionAndDoIt(__("train table"), 
					table, this, rowIndex, columnIndex, aValue);
		}

		/**
		 * setValueAt
		 *
		 * @param aValue
		 *            Object
		 * @param rowIndex
		 *            int
		 * @param columnIndex
		 *            int
		 */
		public void _setValueAt(Object aValue, int rowIndex, int columnIndex) {
			// SimpleDateFormat df = new SimpleDateFormat("H:mm");
			// try {
			switch (columnIndex) {
			case 0:
				myTrain.getStop(rowIndex).name = (String) aValue;
				break;
			case 1:
				// train.stops[rowIndex].arrive = df.parse((String) aValue);
				myTrain.getStop(rowIndex).arrive = (String) aValue;
				break;
			case 2:
				// train.stops[rowIndex].leave = df.parse((String) aValue);
				myTrain.getStop(rowIndex).leave = (String) aValue;
				break;
			case 3:
				myTrain.getStop(rowIndex).isPassenger = ((Boolean) aValue)
						.booleanValue();
				break;
			default:
			}
			// } catch (ParseException ex) {
			// ex.printStackTrace();
			// }
			fireTableCellUpdated(rowIndex, columnIndex);
		}

		/**
		 * getColumnName
		 *
		 * @param columnIndex
		 *            int
		 * @return String
		 */
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return __("Station");
			case 1:
				return __("Arrival");
			case 2:
				return __("Leave");
			case 3:
				return __("Passenger");
			default:
				return null;
			}
		}

		@Override
		public boolean nextCellIsBelow(int row, int column, int increment) {
			return true;
		}

		@Override
		public boolean columnIsTimeString(int column) {
			return 1 <= column && column <= 2;
		}
	}

	public class TrainTable extends JEditTable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2242015871442153005L;

		public TrainTable() {
			super(__("train table"));
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}

		public Dimension getPreferredScrollableViewportSize() {
			int h = this.getRowHeight() * 12;
			int w = super.getPreferredScrollableViewportSize().width;
			return new Dimension(w, h);
		}

		@Override
		protected boolean increaseCell(int row, int column, int startRow,
				int startColumn, int increment) {
			boolean changed = super.increaseCell(row, column, startRow, startColumn, increment);
			
			if (column == 1) {
				// If the column in edit is the arrival time.
				changed = super.increaseCell(row, column + 1, startRow, startColumn, increment) || changed;
			} else if (column == 2) {
				// If the column in edit is the departure time.
				if (row != startRow) {
					changed = super.increaseCell(row, column - 1, startRow, startColumn, increment) || changed;
				}
			}
			
			return changed;
		}
		
	}
	
	public static void main(String[] args) {
		String inputLine = "<td width=\"100%\">第1站：青岛<br>15:04到；15:05开<br>";
		System.out.println(inputLine);
		Pattern stopPattern = Pattern.compile("第\\d+站：(.*?)<br>(.*?)到；(.*?)开");   // <td width="100%">第1站：青岛<br>15:04到；15:05开<br>
		Matcher stopMather = stopPattern.matcher(inputLine);
//		System.out.println(trainNameMather.find());
		if (stopMather.find()) {
			String group0 = stopMather.group(0);
			System.out.println(group0);
			String group1 = stopMather.group(1);
			System.out.println(group1);
			String group2= stopMather.group(2);
			System.out.println(group2);
			String group3= stopMather.group(3);
			System.out.println(group3);
			int i = 1;
		}
	}
}
