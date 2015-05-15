package org.paradise.etrc.data.v1;

import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.TrainGraphPart;
import org.paradise.etrc.data.annotation.TGElement;
import org.paradise.etrc.data.annotation.TGElementType;
import org.paradise.etrc.util.data.Tuple2;

@TGElementType(name="TrainRef", printInOneLine=true)
public class TrainRef extends TrainGraphPart {

	
	TrainRef() {
	}
	
	TrainRef(String name) {
		this();
		setName(name);;
	}
}
