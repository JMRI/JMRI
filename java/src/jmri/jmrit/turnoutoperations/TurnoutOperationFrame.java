package jmri.jmrit.turnoutoperations;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;

import jmri.*;
import jmri.util.swing.JmriJOptionPane;

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
    private JButton renameOrCopyButton;

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

        renameOrCopyButton = new JButton("RenameOrCopy");
        renameOrCopyButton.addActionListener(this::doRenameOrCopy);
        bottomBox.add(renameOrCopyButton);

        deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
        deleteButton.addActionListener(this::doDelete);
        bottomBox.add(deleteButton);

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener((ActionEvent a) -> {
            this.dispose();
        });
        bottomBox.add(cancelButton);
        JButton okButton = new JButton(Bundle.getMessage("ButtonOK"));
        okButton.addActionListener(this::doOK);
        bottomBox.add(okButton);
        outerBox.add(bottomBox);

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
        log.debug("OK clicked {}", e);
        for(Component tab : tabPane.getComponents()) {
            ((TurnoutOperationConfig)tab).endConfigure();
        }
        dispose();
    }

    private void doDelete(ActionEvent e) {
        log.debug("Delete clicked {}", e);
        String query = "";
        if (currentOperation != null && !currentOperation.isDefinitive()) {
            if (currentOperation.isInUse()) {
                query = Bundle.getMessage("DeleteOperationInUse", currentOperation.getName())
                        + Bundle.getMessage("DeleteRevert");
            }
            if (JmriJOptionPane.showConfirmDialog(this, query + Bundle.getMessage("DeleteOperationDialog", currentOperation.getName()),
                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.YES_OPTION) {
                currentOperation.dispose();
                populateTabs();
            }
        }
    }

    private void doRenameOrCopy(ActionEvent e) {
        log.debug("CopyOr clicked {}", e);
        String newName = JmriJOptionPane.showInputDialog(this,
            Bundle.getMessage("EnterNewName"), Bundle.getMessage("EnterNewNameTitle"),
            JmriJOptionPane.QUESTION_MESSAGE);
        
        if (newName != null && !newName.isEmpty()) {
            if ( currentOperation.isDefinitive() ) {
                currentOperation.makeCopy(newName);
            } else {
                if (!currentOperation.rename(newName)) {
                    JmriJOptionPane.showMessageDialog(this, ("TurnoutErrorDuplicate"),
                            Bundle.getMessage("WarningTitle"), JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }
                for ( Turnout t : InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet()) {
                    if ( currentOperation.equivalentTo(t.getTurnoutOperation()) ) {
                        t.setTurnoutOperation(null);
                        t.setTurnoutOperation(currentOperation);
                    }
                }
            }
            populateTabs();
        }
    }

    private void populateTabs() {
        TurnoutOperation[] operations = InstanceManager.getDefault(TurnoutOperationManager.class).getTurnoutOperations();
        log.debug("found {} turnoutoperations from TurnoutOperationManager", operations.length);

        Component firstPane = null;
        tabPane.removeAll();
        ArrayList<TurnoutOperation> definitiveOperations = new ArrayList<>(10);
        ArrayList<TurnoutOperation> namedOperations = new ArrayList<>(50);

        for (TurnoutOperation operation : operations) {
            if (operation.isDefinitive()) {
                definitiveOperations.add(operation);
            } else if (!operation.isNonce()) {
                namedOperations.add(operation);
            }
        }

        definitiveOperations.sort(null);
        namedOperations.sort(null);

        TurnoutOperationConfig pane;
        TurnoutOperation op;
        for (int j = 0; j < definitiveOperations.size(); ++j) {
            op = definitiveOperations.get(j);
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
            op = namedOperations.get(k);
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
        changeTab(null);
        pack();
    }

    private void changeTab( ChangeEvent e) {
        log.debug("tab changed {}", e);
        currentConfig = (TurnoutOperationConfig) tabPane.getSelectedComponent();
        if (currentConfig == null) {
            currentOperation = null;
            previousSelectionName = "";
        } else {
            currentOperation = currentConfig.getOperation();
            previousSelectionName = currentOperation.getName();
        }
        deleteButton.setEnabled( currentConfig!=null && !currentConfig.getOperation().isDefinitive() );
        renameOrCopyButton.setText( currentConfig!=null && !currentConfig.getOperation().isDefinitive()
            ? Bundle.getMessage("Rename") : Bundle.getMessage("MenuItemCopy")
        );
    }

    @Override
    public void dispose() {
        setVisible(false);
        InstanceManager.getDefault(TurnoutOperationManager.class).removePropertyChangeListener(pcl);
        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TurnoutOperationFrame.class);

}
