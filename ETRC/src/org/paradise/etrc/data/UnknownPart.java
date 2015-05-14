package org.paradise.etrc.data;

import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.util.Tuple;

/**
 * Used as unknown element in train graph model when loading from file.
 * @author Jeff Gong
 *
 */
public class UnknownPart extends TrainGraphPart<UnknownPart, UnknownPart> {
	
	public int startLineIndex;
	public int endLineIndex;
	public String startLine;
	public String endLine;

	public UnknownPart() {
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	@Override
	protected String getStartSectionString() { return "UNKNOWN_PART {\r\n"; }
	@Override
	protected String getEndSectionString() { return "}"; }
	@Override
	protected Supplier<? extends TrainGraphPart> getConstructionFunc() {
		return UnknownPart::new;
	}
	@Override
	public void registerSubclasses() {}

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
	protected void setTGPProperty(TrainGraphPart obj, String porpName, String valueInStr) {}

	@Override
	protected String getTGPPropertyReprStr(int index) {return "";}

	/* Element array */
	@Override
	protected Vector<UnknownPart> getTGPElements() {return null;}

	@Override
	protected void addTGPElement(UnknownPart element) {}

	@Override
	protected boolean isOfElementType(TrainGraphPart part) {return false;}
	
	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {};
}
