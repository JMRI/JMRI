package jmri.jmrit.catalog;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;
import jmri.CatalogTreeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file system directory searcher to locate Image files to include in an Image
 * Catalog. This is a singleton class.
 * <BR>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * </P><P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * </P>
 * @author	Pete Cressman Copyright 2010
 *
 */
public class DirectorySearcher {

    // For choosing image directories
    static JFileChooser _directoryChooser = null;
    static DirectorySearcher _instance;

    private DirectorySearcher() {
    }

    public static DirectorySearcher instance() {
        if (_instance == null) {
            _instance = new DirectorySearcher();
        }
        return _instance;
    }

    /**
     * Open file anywhere in the file system and let the user decide whether
     * to add it to the Catalog
     * @param msg title
     * @param recurse if directory choice has no images, set chooser to sub directory so user can continue looking
     * @return chosen directory or null to cancel operation
     */
    private static File getDirectory(String msg, boolean recurse) {
        if (_directoryChooser == null) {
//            _directoryChooser = new JFileChooser(System.getProperty("user.dir") + java.io.File.separator + "resources");
            _directoryChooser = new JFileChooser(FileSystemView.getFileSystemView());
            jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Graphics Files");
            for (int i = 0; i < CatalogTreeManager.IMAGE_FILTER.length; i++) {
                filt.addExtension(CatalogTreeManager.IMAGE_FILTER[i]);
            }
            _directoryChooser.setFileFilter(filt);
        }
        _directoryChooser.setDialogTitle(Bundle.getMessage(msg));
        _directoryChooser.rescanCurrentDirectory();
//        _directoryChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        _directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        File dir = _directoryChooser.getCurrentDirectory();
        while (true) {
            int retVal = _directoryChooser.showOpenDialog(null);
            if (retVal != JFileChooser.APPROVE_OPTION) {
                return null;  // give up if no file selected
            }
            dir = _directoryChooser.getSelectedFile();
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
                            new String[]{Bundle.getMessage("Quit"), Bundle.getMessage("ButtonKeepLooking")}, 1);
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

    private static int numImageFiles(File dir) {
        File[] files = dir.listFiles();
        if (files==null) {
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

    private void showWaitFrame() {
        _waitDialog = new JFrame(Bundle.getMessage("waitTitle"));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Bundle.getMessage("waitWarning")));

        _waitText.setText(Bundle.getMessage("prevMsg"));
        _waitText.setEditable(false);
        _waitText.setFont(new Font("Dialog", Font.BOLD, 12));
        _waitText.setBackground(panel.getBackground());
        _waitText.setOpaque(true);
        panel.add(_waitText);

        _waitDialog.getContentPane().add(panel);
        _waitDialog.setLocation(400, 40);
        _waitDialog.pack();
        _waitDialog.setVisible(false);
    }

    private void closeWaitFrame() {
        if (_waitDialog != null) {
            _waitDialog.dispose();
            _waitDialog = null;
        }
    }

    /**
     * Open one directory.
     *
     */
    public void openDirectory() {
        File dir = getDirectory("openDirMenu", true);
        if (dir != null) {
            showWaitFrame();
            doPreviewDialog(dir, null, new MActionListener(dir, true),
                    null, new CActionListener(), 0);
            closeWaitFrame();
        }
    }

    public File searchFS() {
        showWaitFrame();
        _addDir = null;
        JOptionPane.showMessageDialog(null, "Sorry - Temporarily disabled", Bundle.getMessage("searchFSMenu"),
                JOptionPane.INFORMATION_MESSAGE);
/*        File dir = getDirectory("searchFSMenu", false);
        if (dir != null) {
            if (!getImageDirectory(dir, CatalogTreeManager.IMAGE_FILTER)) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("DirNotFound", dir.getName()), Bundle.getMessage("searchFSMenu"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
        closeWaitFrame();*/
        return _addDir;
    }
    File _addDir;

    class AActionListener implements ActionListener {

        File dir;

        public AActionListener(File d) {
            dir = d;
        }

        public void actionPerformed(ActionEvent a) {
            _addDir = dir;
            cancelLooking();
        }
    }

    class MActionListener implements ActionListener {

        File dir;
        boolean oneDir;

        public MActionListener(File d, boolean o) {
            dir = d;
            oneDir = o;
        }

        public void actionPerformed(ActionEvent a) {
            displayMore(dir, oneDir);
        }
    }

    class LActionListener implements ActionListener {

        File dir;
 
        public LActionListener(File d) {
            dir = d;
        }
        public void actionPerformed(ActionEvent a) {
            keepLooking(dir);
        }
    }

    class CActionListener implements ActionListener {

        public void actionPerformed(ActionEvent a) {
            cancelLooking();
        }
    }

    private void doPreviewDialog(File dir, ActionListener addAction, ActionListener moreAction,
            ActionListener lookAction, ActionListener cancelAction,
            int startNum) {
        _quitLooking = false;
        // if both addAction & lookAction not null dialog will be modeless - i.e dragable
        if (log.isDebugEnabled()) log.debug("doPreviewDialog dir= {}", dir.getAbsolutePath());
        
        _previewDialog = new PreviewDialog(null, "previewDir", dir, CatalogTreeManager.IMAGE_FILTER, false);
//                ((addAction != null) || (lookAction != null)));
        _previewDialog.init(addAction, moreAction, lookAction, cancelAction, startNum);
    }

    /**
     * Find a Directory with image files
     * @param dir directory
     * @param filter file filter for images
     * @return true if directory has image files
     */
    private boolean getImageDirectory(File dir, String[] filter) {
        File[] files = dir.listFiles();
        if (files == null) {
            return false;
        }
        if (numImageFiles(dir) > 0) {
            if (log.isDebugEnabled()) log.debug("getImageDirectory dir= {} has {} files", dir.getAbsolutePath(), numImageFiles(dir));
            doPreviewDialog(dir, new AActionListener(dir), new MActionListener(dir, false),
                    new LActionListener(dir), new CActionListener(), 0);
            return true;
        }
        for (int k = 0; k < files.length; k++) {
            if (files[k].isDirectory()) {
                if (log.isDebugEnabled()) log.debug("getImageDirectory SubDir= {} of {} has {} files", 
                        files[k].getName(), dir.getName(), numImageFiles(files[k]));
                if (getImageDirectory(files[k], filter)) {
                    return true;
                }
/*                if (!getImageDirectory(files[k], filter)) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("DirNotFound", dir.getName()), Bundle.getMessage("searchFSMenu"),
                            JOptionPane.INFORMATION_MESSAGE);
                }*/
/*               if (_quitLooking) {
                    return;
                }*/
            }
        }
        return false;
    }

    PreviewDialog _previewDialog = null;
    JFrame _waitDialog;
    JTextField _waitText = new JTextField();
    boolean _quitLooking = false;

    private void displayMore(File dir, boolean oneDir) {
        if (log.isDebugEnabled()) log.debug("displayMore: dir= {} has {} files", dir.getName(), numImageFiles(dir));
        if (_previewDialog != null) {
            int numFilesShown = _previewDialog.getNumFilesShown();
            _previewDialog.dispose();
            if (numFilesShown>0) {
                doPreviewDialog(dir, null, new MActionListener(dir, oneDir),
                        new LActionListener(dir), new CActionListener(), numFilesShown);
            }
            
        } else {
            if (!getImageDirectory(dir, CatalogTreeManager.IMAGE_FILTER)) {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("DirNotFound", dir.getName()), Bundle.getMessage("searchFSMenu"),
                        JOptionPane.INFORMATION_MESSAGE);
            }            
        }
/*        if (_previewDialog != null) {
            _quitLooking = false;
            int numFilesShown = _previewDialog.getNumFilesShown();
            _previewDialog.dispose();
            if (oneDir) {
                doPreviewDialog(dir, null, new MActionListener(dir, oneDir),
                        null, new CActionListener(), numFilesShown);
            } else {
                doPreviewDialog(dir, null, new MActionListener(dir, oneDir),
                        new LActionListener(), new CActionListener(), numFilesShown);
            }
        }*/
    }

    private void keepLooking(File dir) {
        if (log.isDebugEnabled()) log.debug("keepLooking: dir= {} has {} files", dir.getName(), numImageFiles(dir));
        if (_previewDialog != null) {
            _previewDialog.dispose();
            _previewDialog = null;
        }
        _quitLooking = false;
        if (!getImageDirectory(dir, CatalogTreeManager.IMAGE_FILTER)) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("DirNotFound", dir.getName()), Bundle.getMessage("searchFSMenu"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void cancelLooking() {
        if (_previewDialog != null) {
            _quitLooking = true;
            _previewDialog.dispose();
            _previewDialog = null;
        }
    }

    public void close() {
        closeWaitFrame();
        cancelLooking();
    }
    
    private final static Logger log = LoggerFactory.getLogger(DirectorySearcher.class.getName());
}
