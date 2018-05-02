package jmri.jmrit.operations.setup;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to show a dialog to allow the user to delete Automatic backups.
 *
 * For now, at least, this is needed as it is used by the menu system on the
 * OperationsSetup frame.
 *
 *
 * @author Gregory Madsen Copyright (C) 2012
 */
public class ManageBackupsAction extends AbstractAction {

//    private final static Logger log = LoggerFactory.getLogger(ManageBackupsAction.class);

    public ManageBackupsAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ManageBackupsDialog dlg = new ManageBackupsDialog();
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
    }
}
