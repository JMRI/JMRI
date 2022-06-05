package jmri.jmrit.turnoutoperations;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;

import jmri.*;

/**
 * @author John Harper
 *
 */
public class TurnoutOperationFrame extends JDialog {

    TurnoutOperationConfig currentConfig = null;
    TurnoutOperation currentOperation = null;
    String previousSelectionName = "";
    JTabbedPane tabPane;
    private JButton deleteButton;

    public TurnoutOperationFrame(Frame parent) {
        super(parent, Bundle.getMessage("TurnoutOperationEditorTitle"));
        init();
    }

    private void init() {
        Container contentPane = getContentPane();
        setMinimumSize(new java.awt.Dimension(400, 165));
        Box outerBox = Box.createVerticalBox();
        contentPane.add(outerBox);
        tabPane = new JTabbedPane();
        tabPane.addChangeListener(this::changeTab);

        outerBox.add(tabPane);
        Box bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue());
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener((ActionEvent a) -> {
            this.dispose();
        });
        bottomBox.add(cancelButton);
        JButton okButton = new JButton(Bundle.getMessage("ButtonOK"));
        okButton.addActionListener(this::doOK);
        bottomBox.add(okButton);
        outerBox.add(bottomBox);

        deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
        deleteButton.addActionListener(this::doDelete);

        populateTabs();
        InstanceManager.getDefault(TurnoutOperationManager.class).addPropertyChangeListener(pcl);
        setVisible(tabPane.getTabCount() > 0);
    }

    private final java.beans.PropertyChangeListener pcl = (PropertyChangeEvent e) -> {
        if (e.getPropertyName().equals("Content")) {
            populateTabs();
        }
    };

    private void doOK(ActionEvent e) {
        for(Component tab : tabPane.getComponents()) {
            ((TurnoutOperationConfig)tab).endConfigure();
        }
        dispose();
    }

    private void doDelete(ActionEvent e) {
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
        log.debug("found {} turnoutoperations from TurnoutOperationManager", operations.length);

        Component firstPane = null;
        tabPane.removeAll();
        Vector<TurnoutOperation> definitiveOperations = new Vector<>(10);
        Vector<TurnoutOperation> namedOperations = new Vector<>(50);
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
                pane.add(deleteButton);
                tabPane.add(op.getName(), pane);
                if (op.getName().equals(previousSelectionName)) {
                    tabPane.setSelectedComponent(pane);
                }
            }
        }
        if (tabPane.getSelectedComponent() == null && firstPane != null) {
            tabPane.setSelectedComponent(firstPane);
        }
        changeTab(null);
        pack();
    }

    private void changeTab( ChangeEvent e) {
        currentConfig = (TurnoutOperationConfig) tabPane.getSelectedComponent();
        if (currentConfig == null) {
            currentOperation = null;
            previousSelectionName = "";
        } else {
            currentOperation = currentConfig.getOperation();
            previousSelectionName = currentOperation.getName();
        }
    }

    @Override
    public void dispose() {
        setVisible(false);
        InstanceManager.getDefault(TurnoutOperationManager.class).removePropertyChangeListener(pcl);
        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TurnoutOperationFrame.class);

}
