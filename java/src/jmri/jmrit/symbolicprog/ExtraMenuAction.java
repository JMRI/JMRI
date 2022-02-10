package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to create a dialog so that the user can select an extra menu item to
 * execute. The user can cancel this dialog skipping any execution
 *
 * @author Howard G. Penny Copyright (C) 2005 (as FactoryResetAction)
 * @author Bob Jacobsen    Copyright (C) 2022
 */
public class ExtraMenuAction extends AbstractAction {

    ExtraMenuTableModel rModel;
    JFrame mParent;
    String name;

    public ExtraMenuAction(String actionName, ExtraMenuTableModel rpModel, JFrame pParent) {
        super(Bundle.getMessage("ExtraMessageActionMenuItem", actionName));
        name = actionName;
        rModel = rpModel;
        mParent = pParent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        log.debug("start to display extra menu item");
        Object[] options;
        options = new String[rModel.getRowCount()];
        for (int i = 0; i < rModel.getRowCount(); i++) {
            options[i] = (rModel.getValueAt(i, 0));
        }
        String s = (String) JOptionPane.showInputDialog(
                mParent,
                Bundle.getMessage("ExtraMessageActionLabel", name), // label over JComboBox
                Bundle.getMessage("ExtraMessageActionTitle", name), // Dialog box title
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
    private final static Logger log = LoggerFactory.getLogger(ExtraMenuAction.class);
}
