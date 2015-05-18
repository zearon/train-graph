package org.paradise.etrc.data;

import java.util.List;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.paradise.etrc.util.data.Tuple2;

import static org.paradise.etrc.ETRC.__;

/**
 * Used when assigne object/list properties to element.
 * @author Jeff Gong
 *
 */
public class ListElementAssignment extends TrainGraphPart {
	
	private int lineNum;
	private String line;
	private TrainGraphPart parentObj;
	private Class<?> elementClass;
	
	private List<Object> listElements;
	public Supplier<? extends Object> creator;
	public Function<TrainGraphPart, Object> getter;
	public BiConsumer<TrainGraphPart, Object> setter;
	
	public ListElementAssignment(int lineNum, String line, 
			TrainGraphPart parentObj, Class<?> parentClass) {
		this.lineNum = lineNum;
		this.line = line;
		this.parentObj = parentObj;
		this.elementClass = parentClass;
	}
	
	@SuppressWarnings("unchecked")
	public void createList() {
		boolean needInstantiated = true;
		
//		if (getter != null) {
//			// Check if list is already instantiated.
//			try {
//				Object listProperty = getter.apply(parentObj);
//				if (listProperty != null && listProperty instanceof java.util.List) {
//					needInstantiated = false;
//					
//					// Clear the list if it is already instantiated.
//					((List<?>) listProperty).clear();
//				}
//			} catch (Exception e) {}
//		}
		
		if (needInstantiated)
			try {
				listElements = (List<Object>) creator.get();
			} catch (Exception e) {
				Throwable cause = e.getCause();
				String msg = String.format(__("Instantiating class failed due to %s ()."), 
						cause.getClass().getName(), cause.getMessage());
				throw ParsingException.create(lineNum, line, msg);
			}
	}
	
	public Class<?> getParentClass() {
		return parentObj.getClass();
	}

	public void addElement(int lineNum, String line, Object element) {
		if (element instanceof UnknownPart)
			return;
		
		if (element != null && elementClass.isInstance(element))
			throw ParsingException.create(lineNum, line, 
					__("Add to list failed due to value type %s is not compatible with the element type of property %s."),
					element.getClass().getName(), elementClass.getName());

		if (listElements != null)
			listElements.add(element);
	}
	
	public void assign() {
		try {
			setter.accept(parentObj, listElements);
		} catch (Exception e) {
			String msg;
			if (listElements != null && elementClass.isInstance(listElements))
				msg = String.format(__("Assignment failed due to value type %s is not compatible with the property type %s."),
						listElements.getClass().getName(), elementClass.getName());
			else
				msg = String.format(__("Assignment failed due to %s (%s)."), 
						e.getClass().getName(), e.getMessage());
			
			throw ParsingException.create(lineNum, line, msg);
		}
	}
	
	@Override
	public void loadComplete() {
		assign();
	}
	
	public String toString() {
		return "List element assignment node @ line " + lineNum;
	}
}
