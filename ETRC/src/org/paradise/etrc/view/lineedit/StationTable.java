package org.paradise.etrc.view.lineedit;

import java.awt.Dimension;

import javax.swing.ListSelectionModel;

import com.zearon.util.ui.widget.table.JEditTable;

import static org.paradise.etrc.ETRC.__;

public class StationTable extends JEditTable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8627865729136595002L;

		public StationTable() {
			super(__("station table"));
			setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		}

		public Dimension getPreferredScrollableViewportSize() {
			int r = this.getRowCount();
			int h = this.getRowHeight() * Math.min(r, 15);
			int w = super.getPreferredScrollableViewportSize().width;
//			int w = 400;
			return new Dimension(w, h);
		}
	}