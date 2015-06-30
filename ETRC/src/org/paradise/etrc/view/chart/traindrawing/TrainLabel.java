package org.paradise.etrc.view.chart.traindrawing;

import java.awt.Color;
import java.awt.Font;

/**
 * TrainLabel表示运行图中绘制于运行图线旁的说明文字,包括车次等信息.
 * @author Jeff Gong
 *
 */
public class TrainLabel {
	protected String trainName;
	protected String vehicleName;
	
	protected Font textFont;
	protected Color textColor;
	protected ChartPoint anchorPoint;
	
	public static int MARGIN = 10;

	public TrainLabel() {
	}

}
