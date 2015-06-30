package org.paradise.etrc.view;
import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.tree.TreePath;

import org.paradise.etrc.view.nav.Navigator;

public interface IView {
	
	public JMenu getEditMenu();
	public JToolBar getToolBar();
	public Component getStatusBar();
	
//	default void switchToThisView(ViewContext vc) {
//		if (switchToThisView_getDataSettings() != null)
//			switchToThisView_getDataSettings().run();
//		
//		Navigator.instance.setSelectionPath(switchToThisView_getTreePath());
//	}
	
	default public void clipboard_cut() {}
	default public void clipboard_copy() {}
	default public void clipboard_paste() {}
}
