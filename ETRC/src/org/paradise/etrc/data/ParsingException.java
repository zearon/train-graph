package org.paradise.etrc.data;
import static org.paradise.etrc.ETRC.__;

public class ParsingException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3221832612680422098L;
	
	public int errorCount = 1;
	public int lineNum;
	public int line;

	public ParsingException() {
		super();
	}
	
	public ParsingException(String msg) {
		super(msg);
	}
	
	public static ParsingException create(int lineNum, String line, 
			String msgFormat, Object... params) {
		
		return create(lineNum, line, null,msgFormat, params);
	}
	
	public static ParsingException create(int lineNum, String line, Throwable cause,
			String msgFormat, Object... params) {
		
		String msg = msgFormat;
		if (params != null && params.length >= 1)
			msg = String.format(msgFormat, params);
		
		msg = String.format(__("Line %d: %s\r\n\t%s"), lineNum, msg, line);
		
		ParsingException e;
		if (cause == null)
			e = new ParsingException(msg);
		else 
			e = new ParsingException(msg, cause);
		
		return e;
	}
	
	public ParsingException(int errorCount, String msg) {
		super(msg);
		this.errorCount = errorCount;
	}
	
	public ParsingException(String msg, Throwable cause) {
		super(msg, cause);
		this.errorCount = 1;
	}
	
	public ParsingException(int errorCount, String msg, Throwable cause) {
		super(msg, cause);
		this.errorCount = errorCount;
	}
}
