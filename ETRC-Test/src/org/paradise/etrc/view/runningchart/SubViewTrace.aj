package org.paradise.etrc.view.runningchart;
import static com.zearon.util.debug.DebugUtil.DEBUG;

import javax.swing.JComponent;

import org.paradise.etrc.view.runningchart.chart.LinesPanel;

public aspect SubViewTrace {
	pointcut startup() : 
		call (void ETRCRunner.run(..));
	
	before() : startup() {
		DEBUG("Tracing sub-view component of RunnningChartView [%s]", new Object());
	}
	
	pointcut getPrefSize() : 
		call (* org.paradise.etrc.view.runningchart.chart.LinesPanel.getPreferredSize());
}
