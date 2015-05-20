package org.paradise.etrc.util.ui.databinding;

public interface IValueTypeConverter<A, B> {
	public Class<A> getAValueType();
	public Class<B> getBValueType();
	public B convertAValueToBValue(A value);
}