package org.paradise.etrc.view.timetableedit;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import javax.swing.JTable;

import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.controller.action.UIAction;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.v1.RailroadLineChart;
import org.paradise.etrc.data.v1.Stop;
import org.paradise.etrc.data.v1.TrainGraph;
import org.paradise.etrc.data.v1.TrainRouteSection;
import org.paradise.etrc.util.ui.widget.table.DefaultJEditTableModel;

import static org.paradise.etrc.ETRC.__;

public class TimetableEditSheetModel extends DefaultJEditTableModel {
	private static final long serialVersionUID = 6767541225039467460L;
	protected JTable table;
	protected String tableName;
	protected TrainGraph trainGraph;
	protected RailroadLineChart chart;
	protected boolean downGoing;
	IntSupplier sectionCounter;
	IntFunction<TrainRouteSection> sectionGetter;
	Consumer<TrainRouteSection> sectionAdder;
	BiConsumer<Integer, TrainRouteSection> sectionInserter;
	Consumer<Integer> sectionRemover;
	
	TrainRouteSection newSection;
	private static int newTrainCounter = 0;

	protected TrainRouteSection[] trainsInClipboard = null;
	protected Stop[] stopsInClipboard;
	PasteParameters pasteParams = new PasteParameters();
	
	
	public static class PasteParameters implements Cloneable {
		public int times = -1;
		public int offset = 0;
		public PasteParameters copy() {
			try {
				return (PasteParameters) clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}
	
	
	public TimetableEditSheetModel(JTable table, TrainGraph trainGraph) {
		this.table = table;
		setModel(trainGraph);
	}
	
	public void setModel(TrainGraph trainGraph) {
		this.trainGraph = trainGraph;
		
		switchChart(true);
	}
	
	public void switchChart(boolean downGoing) {
		chart = trainGraph.currentLineChart;
		this.downGoing = downGoing;
		
		if (downGoing) {
			sectionCounter 	= chart::getDownwardTrainSectionCount;
			sectionGetter	= chart::getDownwardTrainSection;
			sectionAdder 	= chart::addDownwardTrainSection;
			sectionInserter	= chart::insertDownwardTrainSectionAt;
			sectionRemover	= chart::removeDownwardTrainSectionAt;
		} else {
			sectionCounter 	= chart::getUpwardTrainSectionCount;
			sectionGetter	= chart::getUpwardTrainSection;
			sectionAdder 	= chart::addUpwardTrainSection;
			sectionInserter	= chart::insertUpwardTrainSectionAt;
			sectionRemover	= chart::removeUpwardTrainSectionAt;
		}
		
		if (newSection != null) {
			newSection.allStops().clear();
		}
		
		tableName = String.format(__("%s timetable on %s"), downGoing ? __("down-going") : __("up-going"),
				chart.railroadLine.getName());
		
		newSection = _createNewTrainRouteSection();
		
		fireTableStructureChanged();
	}
	
	public TrainGraph getTrainGraph() {
		return trainGraph;
	}
	
	public int getColumnCount() {
		if (chart == null)
			return 0;
		
		return sectionCounter.getAsInt() + 1;
	}

	public int getRowCount() {
		return chart.railroadLine.getStationNum() * 2 + 1;
	}

	public Object getValueAt(int rowIndex, int colIndex) {
//		Stop stop = getStop(rowIndex, colIndex);
//		if (stop == null)
//			return null;
//		
//		return stop;
		TrainRouteSection trainSection = getTrainRouteSection(colIndex);
		return trainSection;
	}
	
	public TrainRouteSection getTrainRouteSection(int columnIndex) {
		if (columnIndex == sectionCounter.getAsInt())
			return newSection;
		else if (columnIndex < 0 || columnIndex > sectionCounter.getAsInt())
			return null;
		
		TrainRouteSection trainSection = sectionGetter.apply(columnIndex);
		return trainSection;
	}
		
	public boolean isNewTrainColumn(int column) {
		return column == sectionCounter.getAsInt();
	}
	
	public boolean isRemarksRow(int row) {
		return row == getRowCount() - 1;
	}

	public Stop getStop(int row, int column) {
		TrainRouteSection trainSection = getTrainRouteSection(column);
		if (trainSection == null)
			return null;
		
		if (row < 0 || row >= 2 * trainSection.allStops().size())
			return null;
		
		Stop stop = trainSection.allStops().get(row / 2);
		return stop;
	}

	public String getColumnName(int conIndex) {
		TrainRouteSection trainSection = getTrainRouteSection(conIndex);
		if (trainSection == null)
			return null;
		
		return trainSection.getName();
	}
	
	public Class<?> getColumnClass(int columnIndex) {
		return TrainRouteSection.class;
	}
	
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	protected UIAction getActionAndDoIt(Object aValue, int rowIndex, int columnIndex) {
		return ActionFactory.createTableCellEditAction(__("timetable"), 
				table, this, rowIndex, columnIndex, aValue).addToManagerAndDoIt();
	}
	
	//修改到发点时间不需要特殊处理，在CellEditor里面就处理好了
	//此处需要处理添加、删除停站的操作
	public void _setValueAt(Object aValue, int rowIndex, int colIndex)  {
		/*
		if (colIndex == chart.getTrainNum())
			return;
		
		Train theTrain = chart.getTrain(colIndex);
		String staName = chart.railroadLine.getStation(rowIndex / 2).name;
		Stop stop = (Stop) aValue;
		
		if(stop != null) {
//			System.out.println(theTrain.trainNameFull + " VV " + stop.stationName + "|" + stop.arrive + "|" + stop.leave);
			//stop的站名非空，表示原来就有这条数据
			if(stop.getName() != null) {
				//到发点只要有一个为"DEL"就认为要删除这个停站
				if(stop.getArrive().equals("DEL") || stop.getLeave().equals("DEL"))
					theTrain.delStop(stop.getName());
			}
			//stop站名为空，表示原来没有这条数据，是CellEditor接收到输入后new出来的新的停站数据
			else {
				stop.setName(staName);
				
				chart.insertNewStopToTrain(theTrain, stop);
			}
		}
		else {
		}
		*/
		
		chart.chartChanged = true;
	}

	@Override
	public boolean columnIsTimeString(int column) {
		return false;
	}

	@Override
	public boolean nextCellIsBelow(int row, int column, int increment) {
//		if (column == chart.getTrainRouteSectionCount())
//			return true;

		return true;
	}
	
	// {{ Train route section operations
	
	public TrainRouteSection createNewTrainRouteSection(int index) {
		TrainRouteSection createdSection = newSection;
		TrainRouteSection oldValue = newSection;
		TrainRouteSection newValue = _createNewTrainRouteSection();
		
		ActionFactory.createCreateNewTrainRouteSectionAction(
				String.format(__("create a new train in %s"), tableName),
				table, index, createdSection, sectionInserter,
				sectionRemover, oldValue, newValue, 
				value -> this.newSection = value, 
				this::fireTableStructureChanged)
				.addToManagerAndDoIt();
		
		return createdSection;
	}
	
	private TrainRouteSection _createNewTrainRouteSection() {
		TrainRouteSection newSection = TrainRouteSection
				.createEmptyTrainRouteSection(chart, downGoing,
						String.format(__("NewTrain%d"), ++newTrainCounter));
		
		return newSection;
	}
	
	public void copyStopTime(int[] rows, int column) {
		
	}
	
	public void pasteStopTimeWithOffset(int row, int column, int offset) {
		//
		
		if (offset != 0) {
			
		}
	}
	
	public void removeTrainRouteSection(int[] columns) {
		if (columns.length < 1)
			return;
		
		int count = sectionCounter.getAsInt();
		columns = Arrays.stream(columns)
				.filter(columnIndex -> (columnIndex >=0 && columnIndex < count))
				.toArray();
		
		ActionFactory.createRemoveTableElementsAction(tableName, table, false, 
				columns, sectionGetter, sectionInserter, sectionRemover, 
				this::fireTableStructureChanged).addToManagerAndDoIt();
	}
	
	public void cutTrainRouteSection(int[] columns) {
		copyTrainRouteSection(columns);
		removeTrainRouteSection(columns);
	}
	
	public void copyTrainRouteSection(int[] columns) {
		if (columns.length < 1) {
			trainsInClipboard = new TrainRouteSection[0];
			return;
		}
		
		int count = sectionCounter.getAsInt();
		trainsInClipboard = Arrays.stream(columns)
				.filter(columnIndex -> (columnIndex >=0 && columnIndex < count))
				.mapToObj(this::getTrainRouteSection)
				.toArray(TrainRouteSection[]::new);
		
		pasteParams.offset = 0;
		pasteParams.times = 0;
	}
	
	public void pasteTrainRouteSectionWithOffset(int column, int offset) {
		if (trainsInClipboard == null || trainsInClipboard.length < 1)
			return;
		
		PasteParameters origPasteParams = pasteParams.copy();
		pasteParams.times += 1;
		pasteParams.offset += offset;
		int count = trainsInClipboard.length;
		
		ActionFactory.createPasteTrainRouteSectionAction(
				String.format(__("Copy %d %s with %d minutes time offset."), count, count > 1 ? __("trains") : __("train"), pasteParams.offset),
				table, column, trainsInClipboard, sectionInserter, sectionRemover, 
				origPasteParams, pasteParams, value -> {
					pasteParams.times = value.times;
					pasteParams.offset = value.offset;
				}, 
				this::fireTableStructureChanged).addToManagerAndDoIt();
	}
	
	// }}

}
