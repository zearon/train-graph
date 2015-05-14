package org.paradise.etrc.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.function.Supplier;

import org.paradise.etrc.data.annotation.TGPProperty;
import org.paradise.etrc.data.util.Tuple;

public class Stop extends TrainGraphPart<NullPart> {

	@TGPProperty
	public String arrive;

	@TGPProperty
	public String leave;
	
	//20070224新增，是否图定
	@TGPProperty
	public boolean isPassenger;
	
	Stop() {}

	Stop(String _name) {
		this();
		setName(_name);
	}
	
	public Stop setProperties(String arrive, String leave, boolean schedular) {
		this.arrive = arrive;
		this.leave = leave;
		this.isPassenger = schedular;
		
		return this;
	}

//	public Stop(String _name, String _arrive, String _leave) {
//		this(_name, _arrive, _leave, true); //默认是图定的，向下兼容 -- 取消，所有使用的地方显示指定true
//	}

	public Stop copy() {
		Stop st = new Stop(this.name).setProperties(this.arrive, this.leave, this.isPassenger);
		
		return st;
	}

	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof Stop))
			return false;

		return ((Stop) obj).name.equalsIgnoreCase(this.name);
	}
	
	public static Stop makeStop(String theName, String strArrive, String strLeave, boolean isSchedular) {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		Date theArrive = null;
		Date theLeave = null;
		String myArrive = "";
		String myLeave = "";
		
		try {
			theArrive = df.parse(strArrive);
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		
		try {
			theLeave = df.parse(strLeave);
		} catch (ParseException e) {
			//e.printStackTrace();
		}

		//如果到点解析不成功就把到点设为发点
		if(theArrive == null)
			myArrive = df.format(theLeave);
		else
			myArrive = df.format(theArrive);
		
		//如果发点解析不成功就把发点设为到点
		if(theLeave == null)
			myLeave = df.format(theArrive);
		else
			myLeave = df.format(theLeave);
		
		return new Stop(theName).setProperties(myArrive, myLeave, isSchedular);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	/**
	 * Implements method inherited from abstract base class TrainGraphPart
	 */
	@Override
	protected String getStartSectionString() { return START_SECTION_STOP; }
	@Override
	protected String getEndSectionString() { return END_SECTION_STOP; }
	@Override
	protected Supplier<? extends TrainGraphPart> getConstructionFunc() {
		return Stop::new;
	}
	@Override
	public void registerSubclasses() {}

	/* Element array */
	@Override
	protected Vector<NullPart> getTGPElements() {return null;}

	@Override
	protected void addTGPElement(NullPart element) {}

	@Override
	protected boolean isOfElementType(TrainGraphPart part) {
		return part != null && part instanceof NullPart;
	}
	
	/* Do complete work after all data loaded from file */
	@Override
	protected void loadComplete() {};
}
