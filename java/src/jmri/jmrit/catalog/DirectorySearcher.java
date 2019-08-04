package jmri.jmrit.catalog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file system directory searcher to locate Image files to include in an Image
 * Catalog.
 *
 * @author Pete Cressman Copyright 2010
 */
public class DirectorySearcher implements InstanceManagerAutoDefault {

    // For choosing image directories
    private JFileChooser _directoryChooser = null;

    PreviewDialog _previewDialog = null;
    Seacher _searcher;
    JFrame _waitDialog;
    JLabel _waitText;

    public DirectorySearcher() {
    }

    public static DirectorySearcher instance() {
        return InstanceManager.getDefault(DirectorySearcher.class);
    }

    /**
     * Open file anywhere in the file system and let the user decide whether to
     * add it to the Catalog.
     *
     * @param msg     Bundle property key (string) for i18n title string
     * @param recurse if directory choice has no images, set chooser to sub
     *                directory so user can continue looking
     * @return chosen directory or null to cancel operation
     */
    @SuppressFBWarnings(value = "UW_UNCOND_WAIT", justification="false postive, guarded by logic")
    private File getDirectory(String msg, boolean recurse) {
        if (_directoryChooser == null) {
            _directoryChooser = new JFileChooser(FileSystemView.getFileSystemView());
            jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Graphics Files");
            for (int i = 0; i < CatalogTreeManager.IMAGE_FILTER.length; i++) {
                filt.addExtension(CatalogTreeManager.IMAGE_FILTER[i]);
            }
            _directoryChooser.setFileFilter(filt);
        }
        _directoryChooser.setDialogTitle(Bundle.getMessage(msg));
        _directoryChooser.rescanCurrentDirectory();
        _directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        while (true) {
            int retVal = _directoryChooser.showOpenDialog(null);
            if (retVal != JFileChooser.APPROVE_OPTION) {
                return null;  // give up if no file selected
            }
            File dir = _directoryChooser.getSelectedFile();
            if (dir != null) {
                if (!recurse) {
                    return dir;
                }
                int cnt = numImageFiles(dir);
                if (cnt > 0) {
                    return dir;
                } else {
                    int choice = JOptionPane.showOptionDialog(null,
                            Bundle.getMessage("NoImagesInDir", dir), Bundle.getMessage("QuestionTitle"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                            new String[]{Bundle.getMessage("ButtonStop"), Bundle.getMessage("ButtonKeepLooking")}, 1);
                    switch (choice) {
                        case 0:
                            return null;
                        case 1:
                            _directoryChooser.setCurrentDirectory(dir);
                            break;
                        default:
                            return dir;
                    }
                }
            }
        }
    }

    protected static int numImageFiles(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < files.length; i++) {
            String ext = jmri.util.FileChooserFilter.getFileExtension(files[i]);
            for (int k = 0; k < CatalogTreeManager.IMAGE_FILTER.length; k++) {
                if (ext != null && ext.equalsIgnoreCase(CatalogTreeManager.IMAGE_FILTER[k])) {
                    count++; // OK directory has image files
                }
            }
        }
        return count;
    }

    private void showWaitFrame(String msgkey, File dir) {
        if (_waitDialog == null) {
            _waitDialog = new JFrame();
            _waitDialog.setUndecorated(true);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));
            panel.add(new JLabel(Bundle.getMessage("waitWarning")));

            _waitText = new JLabel();
            panel.add(_waitText);
            panel.setBackground(_waitText.getBackground());

            _waitDialog.getContentPane().add(panel);
            _waitDialog.setLocationRelativeTo(null);
            _waitDialog.setVisible(false);
        }
        if (dir != null) {
            _waitText.setText(Bundle.getMessage(msgkey, dir.getName()));
            _waitDialog.setVisible(true);
            _waitDialog.pack();
            _waitDialog.toFront();
        }
    }

    private void closeWaitFrame() {
        if (_waitDialog != null) {
            _waitDialog.dispose();
            _waitDialog = null;
        }
    }

    private void clearSearch() {
        if (_previewDialog != null) {
            _previewDialog.dispose();
        }
        if (_searcher != null) {
            synchronized (_searcher) {
                _searcher.notify();
            }
        }

    }

    /**
     * Open one directory.
     *
     */
    public void openDirectory() {
        clearSearch();
        File dir = getDirectory("openDirMenu", true); // NOI18N
        if (dir != null) {
            doPreviewDialog(dir, new MActionListener(dir, true),
                    null, new CActionListener(), 0);
            closeWaitFrame();
        }
    }

    public void searchFS() {
        clearSearch();
        File dir = getDirectory("searchFSMenu", false); // NOI18N
        showWaitFrame("searchWait", dir);
        if (dir != null) {
            _searcher = new Seacher(dir);
            _searcher.start();
        }
    }

    void searcherDone(File dir, int count) {
        if (_previewDialog != null) {
            _previewDialog.dispose();
        }
        closeWaitFrame();
        JOptionPane.showMessageDialog(null, Bundle.getMessage("numFound", count, dir.getAbsolutePath()),
                Bundle.getMessage("MessageTitle"), JOptionPane.INFORMATION_MESSAGE);
    }

    class Seacher extends Thread {

        File dir;
        boolean quit = false;
        int count;

        Seacher(File d) {
            dir = d;
        }

        void quit() {
            quit = true;
        }

        @Override
        public void run() {
            getImageDirectory(dir, CatalogTreeManager.IMAGE_FILTER);
            if (log.isDebugEnabled()) {
                log.debug("Searcher done for directory {}  quit={}", dir.getAbsolutePath(), quit);
            }
            ThreadingUtil.runOnGUI(() -> {
                searcherDone(dir, count);
            });
        }

        /**
         * Find a Directory with image files.
         * <p>
         * This waits on completion of the PrivateDialong (which is itself not modal)
         * so must not be called on the Layout or GUI threads
         *
         * @param dir    directory
         * @param filter file filter for images
         */
        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"WA_NOT_IN_LOOP", "UW_UNCOND_WAIT"}, justification="Waiting for single possible event")
        private void getImageDirectory(File dir, String[] filter) {
            if (jmri.util.ThreadingUtil.isGUIThread() || jmri.util.ThreadingUtil.isLayoutThread()) log.error("getImageDirectory called on wrong thread");
            
            File[] files = dir.listFiles();
            if (files == null || quit) {
                // no sub directories
                return;
            }
            int cnt = numImageFiles(dir);
            if (log.isDebugEnabled()) {
                log.debug("getImageDirectory dir= {} has {} files", dir.getAbsolutePath(), cnt);
            }
            count += cnt;
            if (cnt > 0) {
                ThreadingUtil.runOnGUI(() -> {
                    doPreviewDialog(dir, new MActionListener(dir, false),
                            new LActionListener(dir), new CActionListener(), 0);
                });
                // Since PreviewDialog is not modal, wait until user clicks a button to continue
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                        log.error("InterruptedException at _waitForSync " + ie);
                    } catch (java.lang.IllegalArgumentException iae) {
                        log.error("IllegalArgumentException " + iae);
                    }
                }
            }
            for (int k = 0; k < files.length; k++) {
                if (files[k].isDirectory()) {
                    if (quit) {
                        return;
                    }
                    File f = files[k];
                    ThreadingUtil.runOnGUI(() -> {
                        showWaitFrame("searchWait", f);
                    });
//                    if (log.isDebugEnabled()) log.debug("getImageDirectory SubDir= {} of {} has {} files",
//                            files[k].getName(), dir.getName(), numImageFiles(files[k]));
                    getImageDirectory(files[k], filter);
                }
            }
        }
    }

    // More action.  Directory dir has too many icons - display in separate windows
    class MActionListener implements ActionListener {

        File dir;
        boolean oneDir;

        public MActionListener(File d, boolean o) {
            dir = d;
            oneDir = o;
        }

        @Override
        public void actionPerformed(ActionEvent a) {
            displayMore(dir, oneDir);
        }
    }

    // Continue looking for images
    class LActionListener implements ActionListener {

        File dir;

        public LActionListener(File d) {
            dir = d;
        }

        @Override
        public void actionPerformed(ActionEvent a) {
            keepLooking(dir);
        }
    }

    // Cancel - Quit
    class CActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent a) {
            cancelLooking();
        }
    }

    private void doPreviewDialog(File dir, ActionListener moreAction,
            ActionListener lookAction, ActionListener cancelAction, int startNum) {
        showWaitFrame("previewWait", dir);
        if (log.isDebugEnabled()) {
            log.debug("doPreviewDialog dir= {}", dir.getAbsolutePath());
        }

        _previewDialog = new PreviewDialog(null, "previewDir", dir, CatalogTreeManager.IMAGE_FILTER);
        _previewDialog.init(moreAction, lookAction, cancelAction, startNum);
        _waitDialog.setVisible(false);
    }

    private void displayMore(File dir, boolean oneDir) {
        if (log.isDebugEnabled()) {
            log.debug("displayMore: dir= {} has {} files", dir.getName(), numImageFiles(dir));
        }
        if (_previewDialog != null) {
            int numFilesShown = _previewDialog.getNumFilesShown();
            ActionListener lookAction = _previewDialog.getLookActionListener();
            _previewDialog.dispose();
            if (numFilesShown > 0) {
                doPreviewDialog(dir, new MActionListener(dir, oneDir),
                        lookAction, new CActionListener(), numFilesShown);
            }

        } else {
            synchronized (_searcher) {
                _searcher.notify();
            }
        }
    }

    private void keepLooking(File dir) {
        if (log.isDebugEnabled()) {
            log.debug("keepLooking: dir= {} has {} files", dir.getName(), numImageFiles(dir));
        }
        if (_previewDialog != null) {
            _previewDialog.dispose();
            _previewDialog = null;
        }
        if (_searcher != null) {
            synchronized (_searcher) {
                _searcher.notify();
            }
        }
    }

    private void cancelLooking() {
        closeWaitFrame();
        if (_previewDialog != null) {
            _previewDialog.dispose();
            _previewDialog = null;
        }
        if (_searcher != null) {
            synchronized (_searcher) {
                _searcher.quit();
                _searcher.notify();
            }
        }
    }

    public void close() {
        closeWaitFrame();
        cancelLooking();
    }

    private final static Logger log = LoggerFactory.getLogger(DirectorySearcher.class);
}
