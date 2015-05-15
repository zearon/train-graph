package org.paradise.etrc.data.v1;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.util.data.Tuple2;

import static org.paradise.etrc.ETRC.__;

/**
 * A list of all trains
 * @author Jeff Gong
 *
 */
@TGElementType(name="All Trains")
public class AllTrains extends TrainGraphPart
implements Collection<Train>
{

	AllTrains() {
	}

	@TGElement(name="All Trains", isList=true, type=Train.class)
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
	
}
