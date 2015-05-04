package org.paradise.etrc.data;

import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.Tuple;

public class TrainRef extends TrainGraphPart<TrainRef, NullPart> {

	public String trainNameFull = "";
	
	public TrainRef() {
	}
	
	public TrainRef(String trainNameFull) {
		this();
		this.trainNameFull = trainNameFull;
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
	public void _prepareForFirstLoading() {}

	/* Properties */
	private static Tuple<String, Class<?>>[] propTuples = null;
	@Override
	protected Tuple<String, Class<?>>[] getSimpleTGPProperties() {
		if (propTuples == null) {
			propTuples = new Tuple[1];
			
			propTuples[0] = Tuple.of("trainNameFull", String.class);
		}
		
		return propTuples;
	}

	@Override
	protected void setTGPProperty(String porpName, String valueInStr) {
		Tuple<String, Class<?>>[] propTuples = getSimpleTGPProperties();
		
		if (propTuples[0].A.equals(porpName)) {
			trainNameFull = valueInStr;
		}
	}

	@Override
	protected String getTGPPropertyReprStr(int index) {
		String value = "";
		
		if (index == 0) {
			value = trainNameFull;	
		} 
		
		return value;
	}

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
