package org.paradise.etrc.view.runningchart.chart;
import static com.zearon.util.debug.DebugUtil.DEBUG;

import javax.swing.JComponent;
import org.paradise.etrc.view.runningchart.chart.LinesPanel;

public aspect SubViewTrace {
	pointcut getPrefSize(JComponent container) : 
		call (public java.awt.Dimension LinesPanel.getPreferredSize()) && target(container);
	
	after(JComponent container) returning() : getPrefSize(container) {
		DEBUG("Tracing sub-view component of RunnningChartView [%s]", container.getClass().getSimpleName());
	}
}
