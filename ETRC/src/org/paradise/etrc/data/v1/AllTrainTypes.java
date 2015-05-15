package org.paradise.etrc.data.v1;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.util.data.Tuple2;

@TGElementType(name="All Train Types")
public class AllTrainTypes extends TrainGraphPart
implements Collection<TrainType>
{

	AllTrainTypes() {
	}

	@TGElement(name="All Train Types", isList=true, type=TrainType.class)
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
	
	
	
}
