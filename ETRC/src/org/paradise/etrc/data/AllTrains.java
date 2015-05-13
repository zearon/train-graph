package org.paradise.etrc.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.Tuple;

import static org.paradise.etrc.ETRC.__;

/**
 * A list of all trains
 * @author Jeff Gong
 *
 */
public class AllTrains extends TrainGraphPart<AllTrains, Train>
implements Collection<Train>
{

	AllTrains() {
	}

	public Vector<Train> trains = new Vector<Train>();
	protected HashMap<String, Train> trainDict = new HashMap<String, Train> ();
	
	
	protected void updateTrainDict() {
		trainDict.clear();
		for (Train train : trains) {
			trainDict.put(train.name, train);
		}
	}
	protected void addItemToTrainDict(Train train, Collection<? extends Train> c) {
		if (train != null)
			trainDict.put(train.name, train);

		if (c != null)
			for (Train e : c) {
				trainDict.put(e.name, e);
			}
	}
	protected void removeItemFromTrainDict(Object train, Collection<?> c) {
		if (train != null && train instanceof Train)
			trainDict.remove(((Train) train).name);

		if (c != null)
			for (Object e : c) {
				if (e instanceof Train)
					trainDict.remove(((Train) e).name);
			}
	}

	public void updateTrain(Train newTrain) {
		for (int i = 0; i < trains.size(); i++) {
			if (newTrain.equals(trains.get(i))) {
				if(newTrain.color == null)
					newTrain.color = trains.get(i).color;
				trains.set(i, newTrain);
			}
		}
	}
	

	
	public Train findTrain(String trainNameFull) {
		return trainDict.get(trainNameFull);
	}
	
	public Train get(int index) {
		return trains.get(index);
	}
	
	public int count() {
		return size();
	}

	@Override
	public boolean add(Train e) {
		boolean r = trains.add(e);
		if (r)
			addItemToTrainDict(e, null);
		return r;
	}

	public void add(int index, Train element) {
		trains.add(index, element);
		addItemToTrainDict(element, null);
	}

	@Override
	public boolean addAll(Collection<? extends Train> c) {
		boolean r = trains.addAll(c);
		if (r)
			addItemToTrainDict(null, c);
		return r;
	}
	
	public boolean addAll(int index, Collection<? extends Train> c) {
		boolean r =  trains.addAll(index, c);
		if (r)
			addItemToTrainDict(null, c);
		return r;
	}
	
	public Train remove(int index) {
		Train r = trains.remove(index);
		removeItemFromTrainDict(r, null);
		return r;
	}
	
	@Override
	public boolean remove(Object o) {
		boolean r = remove(o);
		if (r)
			removeItemFromTrainDict(o, null);
		return r;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean r = trains.removeAll(c);
		if (r)
			removeItemFromTrainDict(null, c);
		return r;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean r = trains.retainAll(c);
		updateTrainDict();
		return r;
	}
	
	@Override
	public void clear() {
		trains.clear();
		updateTrainDict();
	};

	@Override
	public int size() {
		return trains.size();
	}

	@Override
	public boolean isEmpty() {
		return trains.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return trains.contains(o);
	}
	
	@Override
	public Iterator<Train> iterator() {
		return trains.iterator();
	}
	
	@Override
	public Object[] toArray() {
		return trains.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		return trains.toArray(a);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return trains.containsAll(c);
	}
	
	
	
	
	
	
	


	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	@Override
	protected String getStartSectionString() { return START_SECTION_ALL_TRAIN; }
	@Override
	protected String getEndSectionString() { return END_SECTION_ALL_TRAIN; }
	@Override
	protected Supplier<? extends TrainGraphPart> getConstructionFunc() {
		return AllTrains::new;
	}
	@Override
	public void registerSubclasses() {
		new Train().registerClasses();
	}
	@Override
	public String getName() {
		return String.format(__("%d trains in total"), trains.size());
	}
	@Override
	public void setName(String name) {}

	/* Properties */
	private static Tuple<String, Class<?>>[] propTuples = null;
	@Override
	protected Tuple<String, Class<?>>[] getSimpleTGPProperties() {
		if (propTuples == null) {
			propTuples = new Tuple[0];
			
//			propTuples[0] = Tuple.of("trainNameFull", String.class);
//			propTuples[1] = Tuple.of("trainNameDown", String.class);
//			propTuples[2] = Tuple.of("trainNameUp", String.class);
//			propTuples[3] = Tuple.of("startStation", String.class);
//			propTuples[4] = Tuple.of("terminalStation", String.class);
		}
		
		return propTuples;
	}

	@Override
	protected void setTGPProperty(TrainGraphPart obj, String propName, String valueInStr) {
//		Tuple<String, Class<?>>[] propTuples = getSimpleTGPProperties();
//		
//		if (propTuples[0].A.equals(propName)) {
//			trainNameFull = valueInStr;
//		} else if (propTuples[1].A.equals(propName)) {
//			trainNameDown = valueInStr;
//		} else if (propTuples[2].A.equals(propName)) {
//			trainNameUp = valueInStr;
//		} else if (propTuples[3].A.equals(propName)) {
//			setStartStation(valueInStr);
//		} else if (propTuples[4].A.equals(propName)) {
//			setTerminalStation(valueInStr);
//		}
	}

	@Override
	protected String getTGPPropertyReprStr(int index) {
		String value = "";
		
//		if (index == 0) {
//			value = trainNameFull + "";	
//		} else if (index == 1) {
//			value = trainNameDown + "";
//		} else if (index == 2) {
//			value = trainNameUp + "";
//		} else if (index == 3) {
//			value = getStartStation() + "";
//		} else if (index == 4) {
//			value = getTerminalStation() + "";
//		}
		
		return value;
	}

	/* Element array */
	@Override
	protected Vector<Train> getTGPElements() {
		return trains;
	}

	@Override
	protected void addTGPElement(Train element) {
		trains.add(element);
	}

	@Override
	protected boolean isOfElementType(TrainGraphPart part) {
		return part != null && part instanceof Train;
	}
	
	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {
		updateTrainDict();
	}
	
	/*********************End of TrainGraphPart method implementations*********/

}
