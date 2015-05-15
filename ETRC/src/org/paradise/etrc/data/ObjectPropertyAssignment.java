package org.paradise.etrc.data;

import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.paradise.etrc.util.data.Tuple2;

/**
 * Used when assigne object/list properties to element.
 * @author Jeff Gong
 *
 */
public class ObjectPropertyAssignment extends TrainGraphPart<ObjectPropertyAssignment> {
	
	BiConsumer<TrainGraphPart, Object> setter;

	ObjectPropertyAssignment() {
	}
	
	public void assign(TrainGraphPart parentObj, TrainGraphPart element) {
		setter.accept(parentObj, element);
	}
}
