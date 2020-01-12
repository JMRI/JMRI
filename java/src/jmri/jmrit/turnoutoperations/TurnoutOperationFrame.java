package jmri.jmrit.turnoutoperations;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.*;

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
        super(parent, Bundle.getMessage("TurnoutOperationEditorTitle"));
        Container contentPane = getContentPane();
        setSize(400, 165);
        Box outerBox = Box.createVerticalBox();
        contentPane.add(outerBox);
        tabPane = new JTabbedPane();
        tabPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                self.changeTab();
            }
        });
        outerBox.add(tabPane);
        Box bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue());
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
                                           @Override
                                           public void actionPerformed(ActionEvent a) {
                                               setVisible(false);
                                           }
                                       }
        );
        bottomBox.add(cancelButton);
        JButton okButton = new JButton(Bundle.getMessage("ButtonOK"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                self.doOK();
            }
        }
        );
        bottomBox.add(okButton);
        JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                self.doDelete();
            }
        }
        );
        bottomBox.add(deleteButton);
        outerBox.add(bottomBox);
        populateTabs();
        InstanceManager.getDefault(TurnoutOperationManager.class).addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals("Content")) {
                    populateTabs();
                }
            }
        });
        if (tabPane.getTabCount() > 0) {
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
                query = Bundle.getMessage("DeleteOperationInUse", currentOperation.getName())
                        + Bundle.getMessage("DeleteRevert");
            }
            if (JOptionPane.showConfirmDialog(this, query + Bundle.getMessage("DeleteOperationDialog", currentOperation.getName()),
                    Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                currentOperation.dispose();
                populateTabs();
            }
        }
    }

    private void populateTabs() {
        TurnoutOperation[] operations = InstanceManager.getDefault(TurnoutOperationManager.class).getTurnoutOperations();
        Component firstPane = null;
        tabPane.removeAll();
        Vector<TurnoutOperation> definitiveOperations = new Vector<TurnoutOperation>(10);
        Vector<TurnoutOperation> namedOperations = new Vector<TurnoutOperation>(50);
        for (int i = 0; i < operations.length; ++i) {
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
        for (int j = 0; j < definitiveOperations.size(); ++j) {
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
        for (int k = 0; k < namedOperations.size(); ++k) {
            op = namedOperations.elementAt(k);
            pane = TurnoutOperationConfig.getConfigPanel(op);
            if (pane != null) {
                tabPane.add(op.getName(), pane);
                if (op.getName().equals(previousSelectionName)) {
                    tabPane.setSelectedComponent(pane);
                }
            }
        }
        if (tabPane.getSelectedComponent() == null && firstPane != null) {
            tabPane.setSelectedComponent(firstPane);
        }
        changeTab();
    }

    private void changeTab() {
        currentConfig = (TurnoutOperationConfig) tabPane.getSelectedComponent();
        if (currentConfig == null) {
            currentOperation = null;
            previousSelectionName = "";
        } else {
            currentOperation = currentConfig.getOperation();
            previousSelectionName = currentOperation.getName();
        }
    }

}
