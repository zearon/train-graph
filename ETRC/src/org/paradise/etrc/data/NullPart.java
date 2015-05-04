package org.paradise.etrc.data;

import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.Tuple;

/**
 * Meaningless. Only used as element type of types that have no elements.
 * @author Jeff Gong
 *
 */
public class NullPart extends TrainGraphPart<NullPart, NullPart> {

	public NullPart() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	@Override
	protected String getStartSectionString() { return "NULL_PART {"; }
	@Override
	protected String getEndSectionString() { return "}"; }

	/* Properties */
	private static Tuple<String, Class<?>>[] propTuples = null;
	@Override
	protected Tuple<String, Class<?>>[] getSimpleTGPProperties() {
		if (propTuples == null) {
			propTuples = new Tuple[0];
		}
		
		return propTuples;
	}

	@Override
	protected void setTGPProperty(String porpName, String valueInStr) {}

	@Override
	protected String getTGPPropertyReprStr(int index) {return "";}

	/* Element array */
	@Override
	protected Vector<NullPart> getTGPElements() {return null;}

	@Override
	protected void addTGPElement(NullPart element) {}

	@Override
	protected boolean isOfElementType(TrainGraphPart part) {return false;}
	
	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {};
}
