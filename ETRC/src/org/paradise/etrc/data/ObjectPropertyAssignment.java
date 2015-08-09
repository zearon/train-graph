package org.paradise.etrc.data;

import java.util.function.BiConsumer;

import org.paradise.etrc.data.v1.NullPart;

import static org.paradise.etrc.ETRC.__;

/**
 * Used when assigne object/list properties to element.
 * @author Jeff Gong
 *
 */
public class ObjectPropertyAssignment extends TrainGraphPart {

	private int lineNum;
	private String line;
	private TrainGraphPart parentObj;
	private Class<?> elementClass;
	
	public BiConsumer<TrainGraphPart, Object> setter;

	public ObjectPropertyAssignment(int lineNum, String line, 
			TrainGraphPart parentObj, Class<?> parentClass) {
		this.lineNum = lineNum;
		this.line = line;
		this.parentObj = parentObj;
		this.elementClass = parentClass;
	}
	
	public void assign(TrainGraphPart element) {
		if (element instanceof UnknownPart)
			return;
		
		if (element instanceof NullPart)
			element = null;
		
		try {
			setter.accept(parentObj, element);
			element.setParent(parentObj);
		} catch (Exception e) {
			String msg;
			if (element != null && elementClass.isInstance(element))
				msg = String.format(__("Assignment failed due to value type %s is not compatible with the property type %s."),
						element.getClass().getName(), elementClass.getName());
			else
				msg = e.getMessage();
			
			throw ParsingException.create(lineNum, line, msg);
		}
	}

	public Class<?> getParentClass() {
		return parentObj.getClass();
	}
	
	public String toString() {
		return "Object property assignment node @ line " + lineNum;
	}
}
