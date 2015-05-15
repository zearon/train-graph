package org.paradise.etrc.data;

import java.util.List;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.paradise.etrc.util.data.Tuple2;

/**
 * Used when assigne object/list properties to element.
 * @author Jeff Gong
 *
 */
public class ListElementAssignment extends TrainGraphPart {
	
	List<Object> listElement;
	Supplier<? extends Object> creator;
	BiConsumer<TrainGraphPart, Object> setter;
	
	ListElementAssignment() {
	}
	
	public void createList() {
		listElement = (List<Object>) creator.get();
	}
	
	public void addElement(Object element) {
		listElement.add(element);
	}
	
	public void assign(TrainGraphPart parentObj) {
		setter.accept(parentObj, listElement);
	}
}
