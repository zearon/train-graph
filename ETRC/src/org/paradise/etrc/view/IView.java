package org.paradise.etrc.view;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.paradise.etrc.ETRC;

import com.zearon.util.os.OSXUtil;
import com.zearon.util.ui.databinding.IDataBindingView;
import com.zearon.util.ui.databinding.UIBindingManager;
import com.zearon.util.ui.widget.StatusBar;

import static org.paradise.etrc.ETRC.__;

public interface IView extends IDataBindingView {
	// PLEASE CONFIRM that the parameter type is the same with the interface type.
	// Otherwise, this interface will NOT be initialized.
 	default void IMI_init(IView thisObject) {
 		// Invoke this method for EACH parent interface.
 		IMI_initSuper(IDataBindingView.class, thisObject);
 
		// Fields initializations. The First parameter is the CLASS of this interface.
		IMI_setProperty(IView.class, "editMenu", new JMenu());
		IMI_setProperty(IView.class, "statusBar", new StatusBar());
		JToolBar toolBar = new JToolBar(); toolBar.setFloatable(false); toolBar.addSeparator();
		IMI_setProperty(IView.class, "toolBar", toolBar);
	}
	
	public default JMenu getEditMenu() {
		return (JMenu) IMI_getProperty(IView.class, "editMenu");
	}
	
	public default JToolBar getToolBar() {
		return getDefaultToolBar();
	}
	
	public default JToolBar getDefaultToolBar() {
		return (JToolBar) IMI_getProperty(IView.class, "toolBar");
	}
	
	public default StatusBar getStatusBar() {
		return (StatusBar) IMI_getProperty(IView.class, "statusBar");
	}
	
	default String getViewStatus() {
		JLabel statusBar = getStatusBar();
		return statusBar != null ? statusBar.getText() : "";
	}
	
	default void setViewStatus(String status) {
		JLabel statusBar = getStatusBar();
		if (statusBar != null)
			statusBar.setText(status);
	}

	public default <T extends AbstractButton> T createToolBarButton(
			Class<T> buttonClass, String imgName, String toolTipText,
			Runnable actionListener) {
		T jbOnToolBar = createToolBarButton(buttonClass, imgName, toolTipText);
		jbOnToolBar.addActionListener(e -> actionListener.run());

		return jbOnToolBar;
	}

	public default <T extends AbstractButton> T createToolBarButton(
			Class<T> buttonClass, String imgName, String toolTipText,
			String uiBindingStr) {

		T jbOnToolBar = createToolBarButton(buttonClass, imgName, toolTipText);
		UIDB_addDataBinding(jbOnToolBar, uiBindingStr, null);

		return jbOnToolBar;
	}

	public default <T extends AbstractButton> T createToolBarButton(
			Class<T> buttonClass, String imgName, String toolTipText) {

		T jbOnToolBar = null;
		try {
			jbOnToolBar = buttonClass.newInstance();
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}

		try {
			ImageIcon imageOpenFile = new ImageIcon(IMI_getThisObject().getClass()
					.getResource("/pic/" + imgName));
			jbOnToolBar.setIcon(imageOpenFile);
		} catch (Exception e) {
			jbOnToolBar.setText(imgName);
		}
		jbOnToolBar.setToolTipText(toolTipText);

		getToolBar().add(jbOnToolBar);

		return jbOnToolBar;
	}
	
	public default JMenuItem createMenuItem(String name, boolean setName, boolean enabled,
			int shortcutKey, int extraModifier, Runnable listener) {
		
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.setFont(new java.awt.Font(__("FONT_NAME"), 0, 12));
		menuItem.setEnabled(enabled);
		
		if (setName)
			menuItem.setName(name);

		if (listener != null)
			menuItem.addActionListener(e -> listener.run());
		
		if (shortcutKey >= 0) {
			int ctrlOrCommand = InputEvent.CTRL_DOWN_MASK;
			if (OSXUtil.isOSX10_7OrAbove()) {
				ctrlOrCommand = InputEvent.META_DOWN_MASK; // command key down
				if (shortcutKey == KeyEvent.VK_DELETE)
					shortcutKey = KeyEvent.VK_BACK_SPACE;
			}
			
			int modifier = extraModifier >= 0 ? extraModifier | ctrlOrCommand : 0;
			
			menuItem.setAccelerator(
					KeyStroke.getKeyStroke(shortcutKey, modifier));

		}

		return menuItem;
	}
	
	default public void clipboard_cut() {}
	default public void clipboard_copy() {}
	default public void clipboard_paste() {}
}
