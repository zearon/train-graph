package org.paradise.etrc.wizard.addtrain;

import java.util.Vector;

import org.paradise.etrc.MainFrame;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.v1.Train;
import org.paradise.etrc.dialog.MessageBox;
import org.paradise.etrc.dialog.YesNoBox;
import org.paradise.etrc.util.config.Config;
import org.paradise.etrc.view.alltrains.TrainView;
import org.paradise.etrc.view.runningchart.chart.ChartView;
import org.paradise.etrc.wizard.Wizard;
import org.paradise.etrc.wizard.WizardDialog;

import static org.paradise.etrc.ETRC.__;

public class AddTrainWizard extends Wizard {
	private Train train;
	private ChartView chartView;
	private MainFrame mainFrame = MainFrame.getInstance();
	
	public AddTrainWizard(ChartView _view) {
		wizardSteps = 4;
		chartView = _view;
	}
	
	public int doWizard() {
		WZTrainNameInput step1 = new WZTrainNameInput(mainFrame, 1, __("Add New Train"), __("Input Train Number"));
		WZTimeEdit step2 = new WZTimeEdit(mainFrame, 2, __("Add New Train"), __("Edit Time Table"));
		WZInPointSet step3 = new WZInPointSet(mainFrame, 3, __("Add New Train"), __("Select Start Station"));
		WZOutPointSet step4 = new WZOutPointSet(mainFrame, 4, __("Add New Train"), __("Select Terminal Station"));
		
		String fullName;
		String downName = null;
		String upName = null;
		
		train = null;
		
		int curStep = 1;
		int rt = WizardDialog.NEXT;
		while(rt != WizardDialog.CANCEL && rt != WizardDialog.FINISH) {
			switch(curStep) {
			case 1:
				rt = step1.doModal();
				break;
			case 2:
				if(train == null) {
					fullName = step1.getFullName();
					downName = step1.getDownName();
					upName = step1.getUpName();
					
					if(new YesNoBox(mainFrame, __("Automatically get train informtaion from web?")).askForYes()) {
						String proxyAddress = Config.getInstance().getHttpProxyServer();
						int proxyPort = Config.getInstance().getHttpProxyPort();
						train = TrainView.doLoadTrainFromWeb(fullName, proxyAddress, proxyPort);
						if (train == null) 
						{
							new MessageBox(mainFrame, String.format(__("Unable to get train information for the train %s from web."), fullName)).showMessage();
						}
					}
					if ((train == null) && (new YesNoBox(mainFrame, __("Automatically import train informtaion from build-in time table?")).askForYes())) {
						String[] names = fullName.split("/");
						Vector<Train> trains = new Vector<Train>();
						for(int i=0; i<names.length; i++) {
							Vector<Train> fTras = mainFrame.getSKB().getTrains(names[i]);
							for(int j=0; j<fTras.size(); j++) {
								Train obj = fTras.get(j);
								if(!trains.contains(obj))
									trains.add(obj);
							}
						}
						
						//找到了几条数据，如果是多条的话应当选择。
						if(trains.size() == 0) {
							new MessageBox(mainFrame, String.format(__("Unable to find information for the train %s, please input manually."), fullName)).showMessage();
							train = TrainGraphFactory.createInstance(Train.class);
							train.setName(fullName);
							train.trainNameDown = downName;
							train.trainNameUp = upName;
					    }
						else if(trains.size() == 1)
							train = (Train) trains.get(0);
						else {
							train = (Train) trains.get(0);
							String temp = ((Train) trains.get(0)).getTrainName();
							for(int i=1; i<trains.size(); i++) {
								temp = temp + ", " + ((Train) trains.get(i)).getTrainName();
							}
							new MessageBox(mainFrame, String.format(__("Find %d related information for the train %s, use the first record."), trains.size(), fullName)).showMessage();
						}
					}
					else if (train == null) {
						train = TrainGraphFactory.createInstance(Train.class);
						train.setName(fullName);
						train.trainNameDown = downName;
						train.trainNameUp = upName;
					}

					//看看输入的上下行车次时候与查出来的一致，如果一致则设置为输入的，否则就用时刻表读出来的时候所设的默认的
					Vector<String> myNamesVec = new Vector<String>();
					String[] myNames = train.getTrainName().split("/");
					for(int i=0; i<myNames.length; i++)
						myNamesVec.add(myNames[i]);
					
					if(myNamesVec.contains(downName))
						train.trainNameDown = downName;
					if(myNamesVec.contains(upName))
						train.trainNameUp = upName;
				}

				System.out.println(train);
				step2.setTrain(train, downName, upName);
				System.out.println(train);

				rt = step2.doModal();
				System.out.println(train);
				break;
			case 3:
				step3.setData(chartView.activeLineChart, train);
				rt = step3.doModal();
				break;
			case 4:
				step4.setData(chartView.activeLineChart, train);
				rt = step4.doModal();
				break;
			}
			
			if(rt == WizardDialog.NEXT)
				curStep ++;
			else if(rt == WizardDialog.PREV)
				curStep --;
			
			if(curStep < 1)
				curStep = 1;
			else if(curStep > wizardSteps)
				curStep = wizardSteps;
		}
		
		if(rt == WizardDialog.FINISH) {
			return FINISHED;
		}
		else {
			return CANCELED;
		}
	}
	
	public Train getTrain() {
		return train;
	}
}
