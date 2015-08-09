package org.paradise.etrc.data.v1;

import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElementType;

@TGElementType(name="TrainRef", printInOneLine=true)
public class TrainRef extends TrainGraphPart {
	
	TrainRef() {
	}
	
	TrainRef(String name) {
		this();
		setName(name);;
	}
}
