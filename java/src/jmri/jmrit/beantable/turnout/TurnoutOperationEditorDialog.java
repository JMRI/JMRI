package jmri.jmrit.beantable.turnout;


import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.Turnout;
import jmri.TurnoutOperation;
import static jmri.jmrit.beantable.turnout.TurnoutTableDataModel.editingOps;
import jmri.jmrit.turnoutoperations.TurnoutOperationConfig;
import jmri.util.swing.JmriJOptionPane;

/**
 * Display a TurnoutOperationConfig Dialog for the turnout.
 * 
 * Code originally within TurnoutTableAction.
 * 
 * @author Bob Jacobsen Copyright (C) 2003, 2004, 2007
 * @author Egbert Broerse Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class TurnoutOperationEditorDialog extends JDialog {

    private TurnoutOperation myOp;
    final Turnout myTurnout;
    final TurnoutOperationEditorDialog self;

    /**
     * Pop up a TurnoutOperationConfig Dialog for the turnout.
     *
     * @param op TunoutOperation to edit, not null.
     * @param t  The turnout.
     * @param window  Parent Window for the Dialog, can be null.
     */
    TurnoutOperationEditorDialog( @Nonnull TurnoutOperation op, @Nonnull Turnout t,
            @javax.annotation.CheckForNull java.awt.Window window) {
        super(window);
        self = this;
        myOp = op;
        myTurnout = t;
        init();
    }

    private void init() {

        myOp.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("Deleted")) {
                setVisible(false);
            }
        });

        TurnoutOperationConfig config = TurnoutOperationConfig.getConfigPanel(myOp);
        setTitle();
        log.debug("TurnoutOpsEditDialog title set");
        if (config != null) {
            log.debug("OpsEditDialog opening");
            Box outerBox = Box.createVerticalBox();
            outerBox.add(config);
            Box buttonBox = Box.createHorizontalBox();
            JButton nameButton = new JButton(Bundle.getMessage("NameSetting"));
            nameButton.addActionListener(e -> {
                String newName = JmriJOptionPane.showInputDialog(self, Bundle.getMessage("NameParameterSetting"),"");
                if (newName != null && !newName.isEmpty()) {
                    if (!myOp.rename(newName)) {
                        JmriJOptionPane.showMessageDialog(self, Bundle.getMessage("TurnoutErrorDuplicate"),
                                Bundle.getMessage("WarningTitle"), JmriJOptionPane.ERROR_MESSAGE);
                    }
                    setTitle();
                    myTurnout.setTurnoutOperation(null);
                    myTurnout.setTurnoutOperation(myOp); // no-op but updates display - have to <i>change</i> value
                }
            });
            JButton okButton = new JButton(Bundle.getMessage("ButtonOK"));
            okButton.addActionListener(e -> {
                config.endConfigure();
                if (myOp.isNonce() && myOp.equivalentTo(myOp.getDefinitive())) {
                    myTurnout.setTurnoutOperation(null);
                    myOp.dispose();
                    myOp = null;
                }
                self.setVisible(false);
                editingOps.set(false);
            });
            JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
            cancelButton.addActionListener(e -> {
                self.setVisible(false);
                editingOps.set(false);
            });
            buttonBox.add(Box.createHorizontalGlue());
            if (!myOp.isDefinitive()) {
                buttonBox.add(nameButton);
            }
            buttonBox.add(okButton);
            buttonBox.add(cancelButton);
            outerBox.add(buttonBox);
            getContentPane().add(outerBox);
            this.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    editingOps.set(false);
                }
            });
            
        } else {
            log.error("Error opening Turnout automation edit pane");
        }
        pack();
    }

    private void setTitle() {
        String title = Bundle.getMessage("TurnoutOperationTitle") + " \"" + myOp.getName() + "\"";
        if (myOp.isNonce()) {
            title = Bundle.getMessage("TurnoutOperationForTurnout") + " " + myTurnout.getSystemName();
        }
        setTitle(title);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TurnoutOperationEditorDialog.class);

}

