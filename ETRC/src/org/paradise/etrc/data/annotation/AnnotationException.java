package org.paradise.etrc.data.annotation;

public class AnnotationException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3221832612680422098L;
	
	public int errorCount = 1;

	public AnnotationException() {
		super();
	}
	
	public AnnotationException(String msg) {
		super(msg);
		this.errorCount = 1;
	}
	
	public AnnotationException(int errorCount, String msg) {
		super(msg);
		this.errorCount = errorCount;
	}
	
	public AnnotationException(String msg, Throwable cause) {
		super(msg, cause);
		this.errorCount = 1;
	}
	
	public AnnotationException(int errorCount, String msg, Throwable cause) {
		super(msg, cause);
		this.errorCount = errorCount;
	}
}
