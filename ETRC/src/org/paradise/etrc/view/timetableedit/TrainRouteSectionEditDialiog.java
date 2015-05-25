package org.paradise.etrc.view.timetableedit;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import java.awt.Font;

public class TrainRouteSectionEditDialiog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private final JTextArea txtrRemarks = new JTextArea();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			TrainRouteSectionEditDialiog dialog = new TrainRouteSectionEditDialiog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public TrainRouteSectionEditDialiog() {
		setBounds(100, 100, 450, 275);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel lblTrainname = createJLabel("Train Name", new Font("Lucida Grande", Font.PLAIN, 12));
			lblTrainname.setBounds(6, 6, 72, 16);
			contentPanel.add(lblTrainname);
		}
		{
			JLabel lblTrainType = TrainRouteSectionEditDialiog.createJLabel("Train Type", new Font("Lucida Grande", Font.PLAIN, 12));
			lblTrainType.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblTrainType.setBounds(6, 34, 66, 16);
			contentPanel.add(lblTrainType);
		}
		{
			JLabel lblVehicleName = TrainRouteSectionEditDialiog.createJLabel("Vehicle Name", new Font("Lucida Grande", Font.PLAIN, 12));
			lblVehicleName.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblVehicleName.setBounds(6, 62, 85, 16);
			contentPanel.add(lblVehicleName);
		}
		{
			JLabel lblRemarks = TrainRouteSectionEditDialiog.createJLabel("Remarks", new Font("Lucida Grande", Font.PLAIN, 12));
			lblRemarks.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
			lblRemarks.setBounds(6, 90, 61, 16);
			contentPanel.add(lblRemarks);
		}
		
		JComboBox comboBox = new JComboBox();
		comboBox.setEditable(true);
		comboBox.setBounds(110, 2, 134, 27);
		contentPanel.add(comboBox);
		
		JComboBox comboBox_1 = new JComboBox();
		comboBox_1.setBounds(110, 30, 134, 27);
		contentPanel.add(comboBox_1);
		
		textField = new JTextField();
		textField.setBounds(110, 56, 298, 28);
		contentPanel.add(textField);
		textField.setColumns(10);
		txtrRemarks.setLineWrap(true);
		txtrRemarks.setBounds(114, 90, 294, 110);
		contentPanel.add(txtrRemarks);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	/**
	 * @wbp.factory
	 * @wbp.factory.parameter.source text "Train Name"
	 * @wbp.factory.parameter.source font new java.awt.Font("Lucida Grande", java.awt.Font.PLAIN, 12)
	 */
	public static JLabel createJLabel(String text, Font font) {
		JLabel label = new JLabel(__(text));
		label.setFont(font);
		return label;
	}
}
