/**
 * 
 */
package jmri.jmrit.turnoutoperations;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import jmri.TurnoutOperation;
import jmri.TurnoutOperationManager;

/**
 * @author John Harper
 *
 */
public class TurnoutOperationFrame extends JDialog {
	
	TurnoutOperationFrame self = this;
	TurnoutOperationConfig currentConfig = null;
	TurnoutOperation currentOperation = null;
	String previousSelectionName = "";
	JTabbedPane tabPane;
	
	public TurnoutOperationFrame(Frame parent) {
		super(parent, "Turnout Operation Editor");
		Container contentPane = getContentPane();
		setSize(400,150);
		Box outerBox = Box.createVerticalBox();
		contentPane.add(outerBox);
		tabPane = new JTabbedPane();
		tabPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				self.changeTab();
			}
		});
		outerBox.add(tabPane);
		Box bottomBox = Box.createHorizontalBox();
		bottomBox.add(Box.createHorizontalGlue());
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent a) {
						self.doOK();
					}
				}
			);
		bottomBox.add(okButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent a) {
						hide();
					}
				}
			);
		bottomBox.add(cancelButton);
		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent a) {
						self.doDelete();
					}
				}
			);
		bottomBox.add(deleteButton);
		outerBox.add(bottomBox);
		populateTabs();
		show();
	}
	
	private void doOK() {
		if (currentOperation != null) {
			currentConfig.endConfigure();
		}
		hide();
	}
	
	private void doDelete() {
		if (currentOperation != null && !currentOperation.isDefinitive()) {
			if (JOptionPane.showConfirmDialog(this, "Delete operation "+currentOperation.getName()+"?",
					"Confirm delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				currentOperation.dispose();
				populateTabs();
			}
		}
	}
	
	private void populateTabs() {
		TurnoutOperation[] operations = TurnoutOperationManager.getInstance().getTurnoutOperations();
		Component firstPane = null;
		tabPane.removeAll();
		for (int i=0; i<operations.length; ++i) {
			TurnoutOperation op = operations[i];
			TurnoutOperationConfig pane = TurnoutOperationConfig.getConfigPanel(op);
			if (pane != null) {
				if (firstPane == null) {
					firstPane = pane;
				}
				tabPane.add(op.getName(), pane);
				if (op.getName()==previousSelectionName) {
					tabPane.setSelectedComponent(pane);
				}
			}
		}
		if (tabPane.getSelectedComponent()==null) {
			tabPane.setSelectedComponent(firstPane);
		}
		changeTab();
	}
	
	private void changeTab() {
		currentConfig = (TurnoutOperationConfig)tabPane.getSelectedComponent();
		if (currentConfig == null) {
			currentOperation = null;
			previousSelectionName = "";
		} else {
			currentOperation = currentConfig.getOperation();
			previousSelectionName = currentOperation.getName();
		}
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutOperationFrame.class.getName());
}
