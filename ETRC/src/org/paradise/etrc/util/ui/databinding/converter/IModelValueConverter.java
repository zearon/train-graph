package org.paradise.etrc.util.ui.databinding.converter;

public interface IModelValueConverter<M, U> {
	public String getID();
	public U modelValueToUI(M modelValue);
	public M UIvalueToModel(U uiValue);
}