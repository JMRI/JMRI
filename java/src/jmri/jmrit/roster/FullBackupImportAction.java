package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import jmri.util.FileUtil;
import jmri.util.swing.WindowInterface;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reload the JMRI Roster ({@link jmri.jmrit.roster.Roster}) from a file
 * previously stored by {@link jmri.jmrit.roster.FullBackupExportAction}.
 *
 * Does not currently handle importing the group(s) that the entry belongs to.
 *
 * @author Bob Jacobsen Copyright 2014
 */
public class FullBackupImportAction extends ImportRosterItemAction {

    /**
     * Load from a file exported by {@link FullBackupImportAction}
     *
     * @author Bob Jacobsen Copyright 2014
     */
    private static final long serialVersionUID = 1L;
    private final static Logger log = LoggerFactory.getLogger(FullBackupImportAction.class);

    //private Component _who;
    public FullBackupImportAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public FullBackupImportAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * @param title  Name of this action, e.g. in menus
     * @param parent Component that action is associated with, used to ensure
     *               proper position in of dialog boxes
     */
    public FullBackupImportAction(String title, Component parent) {
        super(title, parent);
    }

    public void actionPerformed(ActionEvent e) {

        // ensure preferences will be found for read
        FileUtil.createDirectory(LocoFile.getFileLocation());

        // make sure instance loaded
        Roster.instance();

        // set up to read import file
        ZipInputStream zipper = null;
        FileInputStream inputfile = null;

        try {

            JFileChooser chooser = new JFileChooser();

            String roster_filename_extension = "roster";
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "JMRI full roster files", roster_filename_extension);
            chooser.addChoosableFileFilter(filter);

            int returnVal = chooser.showOpenDialog(mParent);
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return;
            }

            String filename = chooser.getSelectedFile().getAbsolutePath();

            inputfile = new FileInputStream(filename);
            zipper = new ZipInputStream(inputfile) {
                public void close() {
                } // SaxReader calls close when reading XML stream, ignore
                // and close directly later
            };

            // now iterate through each item in the stream. The get next
            // entry call will return a ZipEntry for each file in the
            // stream
            ZipEntry entry;
            while ((entry = zipper.getNextEntry()) != null) {
                log.debug(String.format("Entry: %s len %d added %TD",
                        entry.getName(), entry.getSize(),
                        new Date(entry.getTime())));

                // Once we get the entry from the stream, the stream is
                // positioned read to read the raw data, and we keep
                // reading until read returns 0 or less.
                try {
                    LocoFile xfile = new LocoFile();   // need a dummy object to do this operation in next line
                    Element lroot = xfile.rootFromInputStream(zipper).clone();
                    if (lroot.getChild("locomotive") == null) {
                        continue;  // that's the roster file
                    }
                    mToID = lroot.getChild("locomotive").getAttributeValue("id");

                    // see if user wants to do it
                    int retval = JOptionPane.showOptionDialog(mParent,
                            Bundle.getMessage("ConfirmImportID", mToID),
                            Bundle.getMessage("ConfirmImport"),
                            0,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new Object[]{Bundle.getMessage("CancelImports"),
                                Bundle.getMessage("Skip"),
                                Bundle.getMessage("OK")},
                            null);
                    if (retval == 0) {
                        break;
                    }
                    if (retval == 1) {
                        continue;
                    }

                    // see if duplicate
                    RosterEntry currentEntry = Roster.instance().getEntryForId(mToID);

                    if (currentEntry != null) {
                        retval = JOptionPane.showOptionDialog(mParent,
                                Bundle.getMessage("ConfirmImportDup", mToID),
                                Bundle.getMessage("ConfirmImport"),
                                0,
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                new Object[]{Bundle.getMessage("CancelImports"),
                                    Bundle.getMessage("Skip"),
                                    Bundle.getMessage("OK")},
                                null);
                        if (retval == 0) {
                            break;
                        }
                        if (retval == 1) {
                            continue;
                        }

                        // turn file into backup
                        LocoFile df = new LocoFile();   // need a dummy object to do this operation in next line
                        df.makeBackupFile(LocoFile.getFileLocation() + currentEntry.getFileName());

                        // delete entry
                        Roster.instance().removeEntry(currentEntry);

                    }

                    loadEntryFromElement(lroot);
                    addToEntryToRoster();

                    // use the new roster
                    Roster.instance().reloadRosterFile();
                } catch (org.jdom2.JDOMException ex) {
                    ex.printStackTrace();
                }
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (inputfile != null) {
                try {
                    inputfile.close(); // zipper.close() is meaningless, see above, but this will do
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }
}
