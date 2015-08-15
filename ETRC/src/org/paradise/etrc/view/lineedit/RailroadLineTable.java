package org.paradise.etrc.view.lineedit;

import java.awt.Dimension;

import javax.swing.ListSelectionModel;

import com.zearon.util.ui.widget.table.JEditTable;

import static org.paradise.etrc.ETRC.__;

public class RailroadLineTable extends JEditTable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3790451911367790284L;

		public RailroadLineTable() {
			super(__("railroad line table"));
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//			setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		}

		public Dimension getPreferredScrollableViewportSize() {
			int r = this.getRowCount();
			int h = this.getRowHeight() * Math.min(r, 15);
//			int w = super.getPreferredScrollableViewportSize().width;
			int w = 250;
			return new Dimension(w, h);
		}
	}