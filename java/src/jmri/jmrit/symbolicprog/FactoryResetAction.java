package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to create a dialog so that the user can select a factory reset to
 * execute. The user can cancel this dialog skipping any resets
 *
 * @author Howard G. Penny Copyright (C) 2005
 */
public class FactoryResetAction extends AbstractAction {

    ResetTableModel rModel;
    JFrame mParent;

    public FactoryResetAction(String actionName, ResetTableModel rpModel, JFrame pParent) {
        super(actionName);
        rModel = rpModel;
        mParent = pParent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        log.debug("start to display Factory Reset");
        Object[] options;
        options = new String[rModel.getRowCount()];
        for (int i = 0; i < rModel.getRowCount(); i++) {
            options[i] = (rModel.getValueAt(i, 0));
        }
        String s = (String) JOptionPane.showInputDialog(
                mParent,
                "Factory Reset" + (options.length > 1 ? "s" : ""),
                ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetTitle"),
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                null);

        //If a string was returned, a reset has been requested.
        if ((s != null) && (s.length() > 0)) {
            int i = 0;
            while (!options[i].equals(s)) {
                i++;
            }
            rModel.performReset(i);
            return;
        }

    }
    private final static Logger log = LoggerFactory.getLogger(FactoryResetAction.class);
}
