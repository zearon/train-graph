package org.paradise.etrc.dialog;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import com.zearon.util.ui.map.GLWindowManager;

public class DialogBase extends JDialog {
	private static final long	serialVersionUID	= 1684672069911144368L;

	// {{ All constructors corresponding to super class

	protected DialogBase() {
		super();
		decorateDialog();
	}

	protected DialogBase(Dialog owner, boolean modal) {
		super(owner, modal);
		decorateDialog();
	}

	protected DialogBase(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		decorateDialog();
	}

	protected DialogBase(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
		decorateDialog();
	}

	protected DialogBase(Dialog owner, String title) {
		super(owner, title);
		decorateDialog();
	}

	protected DialogBase(Dialog owner) {
		super(owner);
		decorateDialog();
	}
	
	protected DialogBase(Frame window) {
		super(window);
		decorateDialog();
	}

	protected DialogBase(Frame owner, boolean modal) {
		super(owner, modal);
		decorateDialog();
	}

	protected DialogBase(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		decorateDialog();
	}

	protected DialogBase(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		decorateDialog();
	}

	protected DialogBase(Frame owner, String title) {
		super(owner, title);
		decorateDialog();
	}

	protected DialogBase(Window owner, ModalityType modalityType) {
		super(owner, modalityType);
		decorateDialog();
	}

	protected DialogBase(Window owner, String title, ModalityType modalityType,
			GraphicsConfiguration gc) {
		super(owner, title, modalityType, gc);
		decorateDialog();
	}

	protected DialogBase(Window owner, String title, ModalityType modalityType) {
		super(owner, title, modalityType);
		decorateDialog();
	}

	protected DialogBase(Window owner, String title) {
		super(owner, title);
		decorateDialog();
	}

	protected DialogBase(Window owner) {
		super(owner);
		decorateDialog();
	}
	
	// }}

	private void decorateDialog() {
		GLWindowManager.decorateDialog(this);
	}
}
