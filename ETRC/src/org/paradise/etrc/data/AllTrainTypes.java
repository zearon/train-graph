package org.paradise.etrc.data;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.Tuple;

public class AllTrainTypes extends TrainGraphPart<AllTrainTypes, TrainType>
implements Collection<TrainType>
{

	AllTrainTypes() {
	}

	public Vector<TrainType> trainTypes = new Vector<TrainType>();
	protected HashMap<String, TrainType> trainTypeDict = new HashMap<String, TrainType> ();
	
	@Override
	public void setToDefault(){
		add(TrainGraphFactory.createInstance(TrainType.class));
	}
	
	
	protected void updateTrainTypeDict() {
		trainTypeDict.clear();
		for (TrainType train : trainTypes) {
			trainTypeDict.put(train.name, train);
		}
	}
	protected void addItemToTrainTypeDict(TrainType train, Collection<? extends TrainType> c) {
		if (train != null)
			trainTypeDict.put(train.name, train);

		if (c != null)
			for (TrainType e : c) {
				trainTypeDict.put(e.name, e);
			}
	}
	protected void removeItemFromTrainTypeDict(Object train, Collection<?> c) {
		if (train != null && train instanceof TrainType)
			trainTypeDict.remove(((TrainType) train).name);

		if (c != null)
			for (Object e : c) {
				if (e instanceof TrainType)
					trainTypeDict.remove(((TrainType) e).name);
			}
	}

	public void updateTrainType(TrainType newTrainType) {
		for (int i = 0; i < trainTypes.size(); i++) {
			if (newTrainType.equals(trainTypes.get(i))) {
				if(newTrainType.color == null)
					newTrainType.color = trainTypes.get(i).color;
				trainTypes.set(i, newTrainType);
			}
		}
	}
	

	
	public TrainType findTrainType(String trainTypeName) {
		return trainTypeDict.get(trainTypeName);
	}
	
	public TrainType get(int index) {
		return trainTypes.get(index);
	}
	
	public int count() {
		return size();
	}

	@Override
	public boolean add(TrainType e) {
		boolean r = trainTypes.add(e);
		if (r)
			addItemToTrainTypeDict(e, null);
		return r;
	}

	public void add(int index, TrainType element) {
		trainTypes.add(index, element);
		addItemToTrainTypeDict(element, null);
	}

	@Override
	public boolean addAll(Collection<? extends TrainType> c) {
		boolean r = trainTypes.addAll(c);
		if (r)
			addItemToTrainTypeDict(null, c);
		return r;
	}
	
	public boolean addAll(int index, Collection<? extends TrainType> c) {
		boolean r =  trainTypes.addAll(index, c);
		if (r)
			addItemToTrainTypeDict(null, c);
		return r;
	}
	
	public TrainType remove(int index) {
		TrainType r = trainTypes.remove(index);
		removeItemFromTrainTypeDict(r, null);
		return r;
	}
	
	@Override
	public boolean remove(Object o) {
		boolean r = remove(o);
		if (r)
			removeItemFromTrainTypeDict(o, null);
		return r;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean r = trainTypes.removeAll(c);
		if (r)
			removeItemFromTrainTypeDict(null, c);
		return r;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean r = trainTypes.retainAll(c);
		updateTrainTypeDict();
		return r;
	}
	
	@Override
	public void clear() {
		trainTypes.clear();
		updateTrainTypeDict();
	};

	@Override
	public int size() {
		return trainTypes.size();
	}

	@Override
	public boolean isEmpty() {
		return trainTypes.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return trainTypes.contains(o);
	}
	
	@Override
	public Iterator<TrainType> iterator() {
		return trainTypes.iterator();
	}
	
	@Override
	public Object[] toArray() {
		return trainTypes.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		return trainTypes.toArray(a);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return trainTypes.containsAll(c);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	
	
	
	
	


	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	@Override
	protected String getStartSectionString() { return START_SECTION_ALL_TRAIN_TYPES; }
	@Override
	protected String getEndSectionString() { return END_SECTION_ALL_TRAIN_TYPES; }
	@Override
	protected Supplier<? extends TrainGraphPart> getConstructionFunc() {
		return AllTrainTypes::new;
	}
	@Override
	public void _prepareForFirstLoading() {
		new TrainType().prepareForFirstLoading();
	}
	@Override
	public String getName() {
		return String.format(__("%d train types in total"), trainTypes.size());
	}

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
	protected void setTGPProperty(String propName, String valueInStr) {
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
	protected Vector<TrainType> getTGPElements() {
		return trainTypes;
	}

	@Override
	protected void addTGPElement(TrainType element) {
		trainTypes.add(element);
	}

	@Override
	protected boolean isOfElementType(TrainGraphPart part) {
		return part != null && part instanceof TrainType;
	}
	
	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {
		updateTrainTypeDict();
	}
	
	/*********************End of TrainGraphPart method implementations*********/


}
