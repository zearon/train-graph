package org.paradise.etrc.view.widget;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.swing.table.AbstractTableModel;

import org.paradise.etrc.controller.ActionManager;
import org.paradise.etrc.controller.action.ActionFactory;
import org.paradise.etrc.controller.action.UIAction;
import org.paradise.etrc.data.Stop;

/**
 * @author Jeff Gong
 *
 */
public abstract class DefaultJEditTableModel extends AbstractTableModel implements JEditTableModel  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int mod;


	public DefaultJEditTableModel() {
	}
	
	protected abstract UIAction getActionAndDoIt(Object aValue, int rowIndex, int columnIndex);
	
	public abstract void _setValueAt(Object aValue, int rowIndex, int columnIndex);
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		UIAction action = getActionAndDoIt(aValue, rowIndex, columnIndex);
	}	
	
	public abstract boolean columnIsTimeString(int column);
	
	public boolean increaseCell(int row, int column, int increment, boolean isFirstInSeries) {
		if (getColumnClass(column).equals(Integer.class))
			return increaseIntegerCell(row, column, increment, isFirstInSeries);
		else if (getColumnClass(column).equals(String.class) && columnIsTimeString(column))
			return increaseTimeCell(row, column, increment, isFirstInSeries);
		else if (getColumnClass(column).equals(Stop.class))
			return increaseStopCell(row, column, increment, isFirstInSeries);
		else
			return false;
	}

	@Override
	public <T> T getCopyOfMutableCellValue(T obj) {
		if (obj instanceof Integer)
			// Integer is immutable, so there is no need to get a real copy
			return obj;
		else if (obj instanceof String)
			// String is immutable, so there is no need to get a real copy
			return obj;
		else if (obj instanceof Stop)
			return (T) ((Stop) obj).copy();
		
		return obj;
	}

	@Override
	public <T> int getIncrement(T baseObj, T newObj) {
		Optional<Integer> increment;
		if (baseObj instanceof Integer)
			return (Integer)newObj - (Integer) baseObj;
		else if (baseObj instanceof String) {
			increment = getIntegerStringIncrement((String) baseObj, (String) newObj);
			if (increment.isPresent())
				return increment.get();
			
			increment = getTimeStringIncrement((String) baseObj, (String) newObj);
			if (increment.isPresent())
				return increment.get();
			
			return 0;
		} else if (baseObj instanceof Stop)
			return getStopTimeIncrement((Stop) baseObj, (Stop) newObj);
		return 0;
	}

	public boolean increaseIntegerCell(int row, int column, int increment, boolean isFirstInSeries) {
		if (increment == 0)
			return false;
		
		Integer val = (Integer) getValueAt(row, column);
		val = val + increment;
		_setValueAt(val, row, column);		
		fireTableCellUpdated(row, column);
		
		return true;
	}
	
	public boolean increaseTimeCell(int row, int column, int increment, boolean isFirstInSeries) {
		if (increment == 0)
			return false;
		
		String timeStr = (String) getValueAt(row, column);
		timeStr = increaseTime(timeStr, increment);
		
		_setValueAt(timeStr, row, column);		
		fireTableCellUpdated(row, column);
		
		return true;
	}
	
	private boolean increaseStopCell(int row, int column, int increment, boolean isFirstInSeries) {
		if (increment == 0)
			return false;
		
		/* Two rows in a table represent a stop, for arrival and departure time
		 *  respectively. So only process once for a stop and skip the others
		 */
		if (isFirstInSeries) {
			mod = row % 2;
		} else {
			if (mod != row % 2)
				return false;
		}
		
		boolean isArriveLine = row % 2 == 0;
		Stop stop = (Stop) getValueAt(row, column);
		
		if (stop == null)
			return false;
		
		if (isFirstInSeries) {
			if (isArriveLine) {
				// Change both arrival and departure time
				stop.arrive = increaseTime(stop.arrive, increment);
				stop.leave = increaseTime(stop.leave, increment);
			} else {
				// Only change departure time and remain arrival time unchanged
				stop.leave = increaseTime(stop.leave, increment);
			}
		} else {
			stop.arrive = increaseTime(stop.arrive, increment);
			stop.leave = increaseTime(stop.leave, increment);
		}
		
		/* Do not need to set the value, since the object reference keep 
		 * unchanged. Only its fields get changed.
		 */
		//		_setValueAt(stop, row, column);
		fireTableCellUpdated(row, column);
		
		return true;
	}
	
	public static String increaseTime(String timeStr, int increment) {
		LocalTime time = LocalTime.parse(timeStr);
		time = time.plusMinutes(increment);
		return time.toString();
	}
	
	public static Optional<Integer> getIntegerStringIncrement(String baseInt, String newInt) {
		try {
			int baseVal = Integer.parseInt(baseInt);
			int newVal = Integer.parseInt(newInt);
			return Optional.of(newVal - baseVal);
		} catch (Exception e) {
			return Optional.empty();
		}
	}
	
	public static Optional<Integer> getTimeStringIncrement(String baseTime, String newTime) {
		try {
			LocalTime baseVal = LocalTime.parse(baseTime);
			LocalTime newVal = LocalTime.parse(newTime);
			int minutes = Math.round(Duration.between(baseVal, newVal).get(ChronoUnit.SECONDS) / 60.0f);
			return Optional.of(minutes);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static int getStopTimeIncrement(Stop baseStop, Stop newStop) {
		Optional<Integer> increment;
		if (baseStop.arrive != null && newStop.arrive != null && !baseStop.arrive.equals(newStop.arrive)) {
			increment = getTimeStringIncrement(baseStop.arrive, newStop.arrive);
			if (increment.isPresent())
				return increment.get();
		} else if (baseStop.leave != null && newStop.leave != null) {
			increment = getTimeStringIncrement(baseStop.leave, newStop.leave);
			if (increment.isPresent())
				return increment.get();
		}
			
		return 0;
	}
}
