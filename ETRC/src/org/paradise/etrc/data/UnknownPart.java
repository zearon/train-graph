package org.paradise.etrc.data;


/**
 * Used as unknown element in train graph model when loading from file.
 * @author Jeff Gong
 *
 */
public class UnknownPart extends TrainGraphPart {
	
	public String message;
	public int startLineIndex;
	public int endLineIndex;
	public String startLine;
	public String endLine;
	public boolean topLevel = false;
	
	public boolean alerted = false;

	public UnknownPart() {
	}
}
