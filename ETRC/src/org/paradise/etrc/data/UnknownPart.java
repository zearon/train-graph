package org.paradise.etrc.data;

import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.util.data.Tuple2;

/**
 * Used as unknown element in train graph model when loading from file.
 * @author Jeff Gong
 *
 */
public class UnknownPart extends TrainGraphPart<UnknownPart> {
	
	public String message;
	public int startLineIndex;
	public int endLineIndex;
	public String startLine;
	public String endLine;
	public boolean topLevel = false;

	public UnknownPart() {
		// TODO Auto-generated constructor stub
	}
}
