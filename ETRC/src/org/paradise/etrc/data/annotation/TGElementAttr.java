package org.paradise.etrc.data.annotation;
import org.paradise.etrc.data.TrainGraphPart;

public class TGElementAttr {
	private TGElement tge;
	private String _name;
	private Integer _index;
	
	private TGElementAttr() {}
	
	public static TGElementAttr fromAnnotation(TGElement tge) {
		TGElementAttr obj = new TGElementAttr();
		obj.tge = tge;
		return obj;
	}
	
	public void setName(String name) {
		this._name = name;
	}
	
	public void setIndex(int index) {
		this._index = index;
	}
	
	public String name() {
		if (_name == null)
			return tge.name();
		else 
			return _name;
	}
	public boolean isList() {
		return tge.isList();
	}
	
	public int index() {
		if (_index == null)
			return tge.index();
		else
			return _index;
	}
	
	public Class<? extends TrainGraphPart> type() {
		return tge.type();
	}
}
