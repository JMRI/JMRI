// ImageIndexEditor.java
package jmri.jmrit.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.CatalogTreeManager;

/**
 * A file system directory searcher to locate Image files to include in an Image Catalog.
 *  This is a singleton class.
 * <P>
 * 
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Pete Cressman  Copyright 2010
 *
 */
public class DirectorySearcher {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.catalog.CatalogBundle");
    
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

    /*
    * Open file anywhere in the file system and let the user decide whether
    * to add it to the Catalog
    */
    private static File getDirectory(String msg, boolean recurseDirs) {    
        if (_directoryChooser == null) {
            _directoryChooser = new JFileChooser(System.getProperty("user.dir")+java.io.File.separator+"resources");
            jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Graphics Files");
            for (int i=0; i<CatalogTreeManager.IMAGE_FILTER.length; i++) {
                filt.addExtension(CatalogTreeManager.IMAGE_FILTER[i]);
            }
            _directoryChooser.setFileFilter(filt);
        }
        _directoryChooser.setDialogTitle(rb.getString(msg));
        _directoryChooser.rescanCurrentDirectory();
        
        _directoryChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(rb.getString("loadDir1"));
        panel.add(label);
        label = new JLabel(rb.getString("loadDir2"));
        panel.add(label);
        label = new JLabel(rb.getString("loadDir3"));
        panel.add(label);
        label = new JLabel(rb.getString("loadDir4"));
        panel.add(label);
        _directoryChooser.setAccessory(panel);
        int retVal = _directoryChooser.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) return null;  // give up if no file selected
 
        File dir = _directoryChooser.getSelectedFile();
        if (dir!=null) {
            if (!dir.isDirectory()) {
                dir = dir.getParentFile();
            }
            if (hasImageFiles(dir)) {
                return dir; // OK directory has image files
            }
            if (!recurseDirs) {
                return null;
            }
        }
        return dir;
    }

    private static boolean hasImageFiles(File dir) {
        File[] files = dir.listFiles();
        for (int i=0; i<files.length; i++) {
            String ext = jmri.util.FileChooserFilter.getFileExtension(files[i]);
            for (int k=0; k<CatalogTreeManager.IMAGE_FILTER.length; k++) {
                if (ext != null && ext.equalsIgnoreCase(CatalogTreeManager.IMAGE_FILTER[k])) {
                    return true; // OK directory has image files
                }
            }
        }
        return false;
    }

    private void showWaitFrame() {
        _waitDialog = new JFrame(rb.getString("waitTitle"));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(rb.getString("waitWarning")));

        _waitText.setText(rb.getString("prevMsg"));
        _waitText.setEditable(false);
        _waitText.setFont(new Font("Dialog", Font.BOLD, 12));
        _waitText.setBackground(panel.getBackground());
        _waitText.setOpaque(true);
        panel.add(_waitText);

        _waitDialog.getContentPane().add(panel);
        _waitDialog.setLocation(400,40);
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
    *  Open one directory.
    * @param addDir - <pre>if true, allows directory to be added as a tree to the Catalog.
    *                      if false, allows preview panel to drag icons.
    */
    public void openDirectory(boolean addDir) {
        showWaitFrame();
        File dir = getDirectory("openDirMenu", false);
        if (dir != null) {
            if (addDir) {
                doPreviewDialog(dir, new AActionListener(dir), new MActionListener(dir, true),
                                null, new CActionListener(), 0);
            } else {
                doPreviewDialog(dir, null, new MActionListener(dir, true),
                                null, new CActionListener(), 0);
            }
        } else {
            JOptionPane.showMessageDialog(null, rb.getString("NoImagesInDir"), rb.getString("searchFSMenu"),
                                                     JOptionPane.INFORMATION_MESSAGE);
        }
        closeWaitFrame();
    }

    public File searchFS() {
        showWaitFrame();
        _addDir = null;
        File dir = getDirectory("searchFSMenu", true);
        if (dir != null) {
            getImageDirectory(dir, CatalogTreeManager.IMAGE_FILTER);
            if (!_quitLooking) {
                JOptionPane.showMessageDialog(null, rb.getString("DirNotFound"), rb.getString("searchFSMenu"),
                                                     JOptionPane.INFORMATION_MESSAGE);
            }
        }
        closeWaitFrame();
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
        public void actionPerformed(ActionEvent a) {
            keepLooking();
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
        _previewDialog = new PreviewDialog(null, "previewDir", dir, CatalogTreeManager.IMAGE_FILTER,
                                            ((addAction != null)||(lookAction != null)) );
        _previewDialog.init(addAction, moreAction, lookAction, cancelAction,
                            startNum, _waitDialog);
        if (lookAction == null) {
            closeWaitFrame();
        }
    }

    /**
    * Directory dir has image files
    */
    private void getImageDirectory(File dir, String[] filter) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        if (hasImageFiles(dir)) {
            doPreviewDialog(dir, new AActionListener(dir), new MActionListener(dir, false),
                                new LActionListener(), new CActionListener(), 0);
            if (_quitLooking) {
                return;
            }
        }
        for (int k=0; k<files.length; k++) {
            if (files[k].isDirectory()) {
                getImageDirectory(files[k], filter);
                if (_quitLooking) {
                    return;
                }
            }
        }
    }

    PreviewDialog _previewDialog = null;
    JFrame _waitDialog;
    JTextField _waitText = new JTextField();
    boolean _quitLooking = false;
    
    void displayMore(File dir, boolean oneDir) {
        if (_previewDialog != null) {
            _quitLooking = false;
            int numFilesShown = _previewDialog.getNumFilesShown();
            _previewDialog.dispose();
            if (oneDir) {
                doPreviewDialog(dir, null, new MActionListener(dir, oneDir),
                                null, new CActionListener(), numFilesShown);
            } else {
                doPreviewDialog(dir, null, new MActionListener(dir,oneDir),
                                new LActionListener(), new CActionListener(), numFilesShown);
            }
        }
    }
    
    void keepLooking() {
        if (_previewDialog != null) {
            _quitLooking = false;
            _previewDialog.dispose();
            _previewDialog = null;
        }
    }

    public void cancelLooking() {
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

    static Logger log = LoggerFactory.getLogger(DirectorySearcher.class.getName());
}

