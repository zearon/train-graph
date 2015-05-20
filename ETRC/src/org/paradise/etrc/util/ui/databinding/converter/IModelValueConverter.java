package org.paradise.etrc.util.ui.databinding.converter;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

public interface IModelValueConverter<M, U> {
	public String getID();
	public U modelValueToUI(M modelValue);
	public M UIvalueToModel(U UIvalue);
}