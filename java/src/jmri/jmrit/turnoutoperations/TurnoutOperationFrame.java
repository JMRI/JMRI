/**
 * 
 */
package jmri.jmrit.turnoutoperations;

import org.apache.log4j.Logger;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

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
            setVisible(false);
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
		TurnoutOperationManager.getInstance().addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getPropertyName().equals("Content")) {
					populateTabs();
				}
			}
		});
		if (tabPane.getTabCount()>0) {
      setVisible(true);
		}
	}
	
	private void doOK() {
		if (currentOperation != null) {
			currentConfig.endConfigure();
		}
    setVisible(false);
	}
	
	private void doDelete() {
		String query = "";
		if (currentOperation != null && !currentOperation.isDefinitive()) {
			if (currentOperation.isInUse()) {
				query = "Operation "+currentOperation.getName()+" is in use\n"+
						"Turnouts using it will revert to the global default\n";
			}
			if (JOptionPane.showConfirmDialog(this, query+"Delete operation "+currentOperation.getName()+"?",
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
		Vector<TurnoutOperation> definitiveOperations = new Vector<TurnoutOperation>(10);
		Vector<TurnoutOperation> namedOperations = new Vector<TurnoutOperation>(50);
		for (int i=0; i<operations.length; ++i) {
			if (operations[i].isDefinitive()) {
				definitiveOperations.addElement(operations[i]);
			} else if (!operations[i].isNonce()) {
				namedOperations.addElement(operations[i]);
			}
		}
    	java.util.Collections.sort(definitiveOperations);
    	java.util.Collections.sort(namedOperations);
    	TurnoutOperationConfig pane;
    	TurnoutOperation op;
		for (int j=0; j<definitiveOperations.size(); ++j) {
			op = definitiveOperations.elementAt(j);
			pane = TurnoutOperationConfig.getConfigPanel(op);
			if (pane != null) {
				if (firstPane == null) {
					firstPane = pane;
				}
				tabPane.add(op.getName(), pane);
				if (op.getName().equals(previousSelectionName)) {
					tabPane.setSelectedComponent(pane);
				}
			}
		}
		for (int k=0; k<namedOperations.size(); ++k) {
			op = namedOperations.elementAt(k);
			pane = TurnoutOperationConfig.getConfigPanel(op);
			if (pane != null) {
				tabPane.add(op.getName(), pane);
				if (op.getName()==previousSelectionName) {
					tabPane.setSelectedComponent(pane);
				}
			}
		}
		if (tabPane.getSelectedComponent()==null && firstPane!=null) {
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
	
	static Logger log = Logger.getLogger(TurnoutOperationFrame.class.getName());
}
