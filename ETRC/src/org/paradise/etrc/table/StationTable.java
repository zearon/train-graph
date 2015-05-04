package org.paradise.etrc.table;

import java.awt.Dimension;

import javax.swing.ListSelectionModel;

import org.paradise.etrc.view.widget.JEditTable;

public class StationTable extends JEditTable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8627865729136595002L;

		public StationTable() {
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