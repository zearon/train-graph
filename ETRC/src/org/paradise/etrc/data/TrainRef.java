package org.paradise.etrc.data;

import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.Tuple;

public class TrainRef extends TrainGraphPart<NullPart> {

	
	TrainRef() {
	}
	
	TrainRef(String name) {
		this();
		setName(name);;
	}

	
	
	
	
	
	
	
	
	
	

	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	@Override
	protected String getStartSectionString() { return START_SECTION_TRAIN_REF; }
	@Override
	protected String getEndSectionString() { return END_SECTION_TRAIN_REF; }
	@Override
	protected Supplier<? extends TrainGraphPart> getConstructionFunc() {
		return TrainRef::new;
	}
	@Override
	public void registerSubclasses() {}

	/* Properties */
	private static Tuple<String, Class<?>>[] propTuples = null;

	/* Element array */
	@Override
	protected Vector<NullPart> getTGPElements() {return null;}

	@Override
	protected void addTGPElement(NullPart element) {}

	@Override
	protected boolean isOfElementType(TrainGraphPart part) {
		return part != null && part instanceof NullPart;
	}
	
	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {};
}
