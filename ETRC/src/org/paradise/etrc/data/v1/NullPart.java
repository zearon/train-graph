package org.paradise.etrc.data.v1;

import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElementType;

/**
 * Meaningless. Only used as element type of types that have no elements.
 * @author Jeff Gong
 *
 */
@TGElementType(name="NULL", printInOneLine=true)
public class NullPart extends TrainGraphPart {

	NullPart() {}
	
	/* Override name property in base class and make it not a TG property*/
	public void setName(String name) {}
	public String getName() {return ""; }
}
