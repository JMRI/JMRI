package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import jmri.util.ThreadingUtil;
import jmri.util.swing.CountingBusyDialog;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Offer an easy mechanism to save the entire roster contents from one instance
 * of DecoderPro. The result is a zip format file, containing all of the roster
 * entries plus the overall roster.xml index file.
 *
 * @author david d zuhn
 *
 */
public class FullBackupExportAction
        extends JmriAbstractAction {

    // parent component for GUI
    public FullBackupExportAction(String s, WindowInterface wi) {
        super(s, wi);
        _parent = wi.getFrame();
    }

    public FullBackupExportAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
        _parent = wi.getFrame();
    }
    private Component _parent;
    private String filename;
    private CountingBusyDialog dialog;

    /**
     * @param s      Name of this action, e.g. in menus
     * @param parent Component that action is associated with, used to ensure
     *               proper position in of dialog boxes
     */
    public FullBackupExportAction(String s, Component parent) {
        super(s);
        _parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

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

        new Thread(() -> {run();}).start();
    }

    /**
     * Actually do the copying
     */
    public void run() {
        try {

            Roster roster = Roster.getDefault();

            dialog = new CountingBusyDialog(null, "Exporting Roster", false, roster.getAllEntries().size());
            ThreadingUtil.runOnGUIEventually(() -> {dialog.start();});

            try (ZipOutputStream zipper = new ZipOutputStream(new FileOutputStream(filename))) {

                // create a zip file roster entry for each entry in the main roster
                int count = 0;
                for (RosterEntry entry : roster.getAllEntries()) {
                    count++;
                    final int thisCount = count;
                    ThreadingUtil.runOnGUIEventually(() -> {dialog.count(thisCount);});
                    try {
                        copyFileToStream(entry.getPathName(), "roster", zipper, "roster: "+entry.getId());

                        // process image files if present
                        if (entry.getImagePath() != null && ! entry.getImagePath().isEmpty())
                            copyFileToStream(entry.getImagePath(), "roster", zipper, "image: "+entry.getId());
                        if (entry.getIconPath() != null && ! entry.getIconPath().isEmpty())
                            copyFileToStream(entry.getIconPath(), "roster", zipper, "icon: "+entry.getId());

                    } catch (FileNotFoundException ex) {
                        log.error("Unable to find file in entry {}", entry.getId(), ex);
                    } catch (IOException ex) {
                        log.error("Unable to write during entry {}", entry.getId(), ex);
                    } catch (Exception ex) {
                        log.error("Unexpected exception during entry {}", entry.getId(), ex);
                    }
                }

                // Now the full roster entry
                copyFileToStream(Roster.getDefault().getRosterIndexPath(), null, zipper, null);

                zipper.setComment("Roster file saved from DecoderPro " + jmri.Version.name());

                zipper.close();

            } catch (FileNotFoundException ex) {
                log.error("Unable to find file {}", filename, ex);
            } catch (IOException ex) {
                log.error("Unable to write to {}", filename, ex);
            }
        } finally {
            ThreadingUtil.runOnGUIEventually(() -> {dialog.finish();});
            log.info("Writing backup done");
        }
    }

    /**
     * Copy a file to an entry in a zip file.
     * <p>
     * The basename of the source file will be used in the zip file, placed in
     * the directory of the zip file specified by dirname. If dirname is null,
     * the file will be placed in the root level of the zip file.
     *
     * @param filename the file to copy
     * @param dirname  the zip file "directory" to place this file in
     * @param zipper   the ZipOutputStream
     */
    private void copyFileToStream(String filename, String dirname, ZipOutputStream zipper, String comment)
            throws IOException {

        log.debug("write: {}", filename);

        File file = new File(filename);
        String entryName;

        if (dirname != null) {
            entryName = dirname + "/" + file.getName();
        } else {
            entryName = file.getName();
        }

        ZipEntry zipEntry = new ZipEntry(entryName);

        zipEntry.setTime(file.lastModified());
        zipEntry.setSize(file.length());
        if (comment != null) {
            zipEntry.setComment(comment);
        }

        zipper.putNextEntry(zipEntry);

        FileInputStream fis = new FileInputStream(file);
        try {
            int c;
            while ((c = fis.read()) != -1) {
                zipper.write(c);
            }
        } finally {
            fis.close();
        }

        zipper.closeEntry();
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FullBackupExportAction.class);
}
