package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.WindowInterface;

/**
 * Offer an easy mechanism to save a roster group's contentsfrom one instance
 * of DecoderPro to another. The result is a zip format file, containing all of the roster
 * entries plus the overall roster.xml index file.
 *
 * @author Bob Jacobsen
 *
 */
public class GroupBackupExportAction
        extends FullBackupExportAction {

    // parent component for GUI
    public GroupBackupExportAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public GroupBackupExportAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * @param s      Name of this action, e.g. in menus
     * @param parent Component that action is associated with, used to ensure
     *               proper position in of dialog boxes
     */
    public GroupBackupExportAction(String s, Component parent) {
        super(s, parent);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // Always ask for group.  (See jmri.jmrit.roster.swing.DeleteRosterGroupAction
        // for an example of using a pre-selected group instead)
        String group = (String) JmriJOptionPane.showInputDialog(_parent,
                Bundle.getMessage("ExportRosterGroupDialog"),
                Bundle.getMessage("ExportRosterGroupTitle", ""),
                JmriJOptionPane.INFORMATION_MESSAGE,
                null,
                Roster.getDefault().getRosterGroupList().toArray(),
                null);
 
        String roster_filename_extension = "roster";

        JFileChooser chooser = new jmri.util.swing.JmriJFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "JMRI full roster files", roster_filename_extension);
        chooser.setFileFilter(filter);

        int returnVal = chooser.showSaveDialog(_parent);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        filename = chooser.getSelectedFile().getAbsolutePath();

        if (!filename.endsWith("."+roster_filename_extension)) {
            filename = filename.concat("."+roster_filename_extension);
        }

        var list = Roster.getDefault().getEntriesInGroup(group);
        new Thread(() -> {run(list);}).start();

    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GroupBackupExportAction.class);
}
