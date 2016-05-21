// PreviewDialog.java

package jmri.jmrit.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
//import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.io.File;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;

/**
 * Create a Dialog to display the images in a file system directory.
 * <P>
 * 
 *
 * <hr>
 * This file is part of JMRI.  Displays filtered files from a file system
 * directory.  May be modal or modeless.  Modeless has dragging enabled.
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
 * @author			Pete Cressman  Copyright 2009
 *
 */
public class PreviewDialog extends JDialog {

    JPanel          _selectedImage;
    static Color    _grayColor = new Color(235,235,235);
    Color           _currentBackground = _grayColor;

    JLabel          _previewLabel = new JLabel();
    JPanel          _preview;


    int _cnt;           // number of files displayed when setIcons() method runs
    int _startNum;      // total number of files displayed from a directory

    File _currentDir;   // current FS directory
    String[] _filter;   // file extensions of types to display
    JButton _addButton;
    JButton _moreButton;
    boolean _mode;

    public PreviewDialog(Frame frame, String title, File dir, String[] filter, boolean modality ) {
        super(frame, Bundle.getMessage(title), modality);
        _currentDir = dir;
        _filter = new String[filter.length];
        for (int i=0; i<filter.length; i++) {
            _filter[i] = filter[i];
        }
        _mode = modality;
    }

    public void init(ActionListener addAction, ActionListener moreAction, 
                     ActionListener lookAction, ActionListener cancelAction, 
                     int startNum, JFrame waitDialog) {
        waitDialog.setVisible(true);
        waitDialog.invalidate();
        waitDialog.repaint();
        if (log.isDebugEnabled()) log.debug("Enter _previewDialog.init dir= "+_currentDir.getPath()); 
        addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
                    DirectorySearcher.instance().close();
                    dispose();
                }
            });
        JPanel pTop = new JPanel();
        pTop.setLayout(new BoxLayout(pTop, BoxLayout.Y_AXIS));
        pTop.add(new JLabel(_currentDir.getPath()));
        JTextField msg = new JTextField();
        msg.setFont(new Font("Dialog", Font.BOLD, 12));
        msg.setEditable(false);
        msg.setBackground(pTop.getBackground());
        pTop.add(msg);
        getContentPane().add(pTop, BorderLayout.NORTH);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalStrut(5));

        if (moreAction != null) {
            p.add(Box.createHorizontalStrut(5));
            _moreButton = new JButton(Bundle.getMessage("ButtonDisplayMore"));
            _moreButton.addActionListener(moreAction);
            p.add(_moreButton);
        }

        JPanel previewPanel = setupPanel();     // provide panel for images, add to bottom of window
        _startNum = startNum;
        try {
            _moreButton.setVisible(setIcons(startNum));
        } catch (OutOfMemoryError oome) {
            log.error("OutOfMemoryError AvailableMemory= "+availableMemory()+", "+_cnt+" files read.");
            resetPanel();
        }

        if (addAction != null) {
            if (_moreButton.isVisible()) {
                JOptionPane.showMessageDialog(this, 
                                      java.text.MessageFormat.format(Bundle.getMessage("tooManyIcons"), 
                                      new Object[] {_currentDir.getName()}),
                                      Bundle.getMessage("warn"), JOptionPane.INFORMATION_MESSAGE);
                msg.setText(Bundle.getMessage("moreMsg"));
            } else {
                p.add(Box.createHorizontalStrut(5));
                _addButton = new JButton(Bundle.getMessage("ButtonAddToCatalog"));
                _addButton.addActionListener(addAction);
                p.add(_addButton);
                msg.setText(Bundle.getMessage("addDirMsg"));
            }
        } else if (lookAction != null) {
            if (_moreButton.isVisible()) {
                msg.setText(Bundle.getMessage("moreMsg"));
            }
        }
        else {
            msg.setText(Bundle.getMessage("dragMsg"));
        }

        if (lookAction != null) {
            p.add(Box.createHorizontalStrut(5));
            JButton lookButton = new JButton(Bundle.getMessage("ButtonKeepLooking"));
            lookButton.addActionListener(lookAction);
            p.add(lookButton);
        }
        p.add(Box.createHorizontalStrut(5));
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(cancelAction);
        p.add(cancelButton);
        p.add(Box.createHorizontalStrut(5));
        p.setPreferredSize(new Dimension(400, _moreButton.getPreferredSize().height));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(p);
        panel.add(new JSeparator());
        panel.add(previewPanel);
        getContentPane().add(panel);
        setPreferredSize(new Dimension(450, 
                        previewPanel.getPreferredSize().height + 2*p.getPreferredSize().height));
        //setMinimumSize(new Dimension(450,300));
        setLocationRelativeTo(null);
        pack();
        waitDialog.setVisible(false);
        setVisible(true);
    }

    /**
    * Setup a display panel to display icons
    */
    private JPanel setupPanel() {
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(_previewLabel);
        previewPanel.add(p);
        _preview = new JPanel();
        _preview.setMinimumSize(new Dimension(2*CatalogPanel.ICON_WIDTH, 2*CatalogPanel.ICON_HEIGHT));
        JScrollPane js = new JScrollPane(_preview);                       
        previewPanel.add(js);
        _preview.setMinimumSize(new Dimension(200, 150));
        JRadioButton whiteButton = new JRadioButton(Bundle.getMessage("white"),false);
        JRadioButton grayButton = new JRadioButton(Bundle.getMessage("lightGray"),true);
        JRadioButton darkButton = new JRadioButton(Bundle.getMessage("darkGray"),false);
        whiteButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    setBackGround(Color.white);
                }
            });
        grayButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    setBackGround(_grayColor);
                }
            });
        darkButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    setBackGround(new Color(150,150,150));
                }
            });
        JPanel pp = new JPanel();
        pp.add(new JLabel(Bundle.getMessage("setBackground")));
        previewPanel.add(pp);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        ButtonGroup selGroup = new ButtonGroup();
        selGroup.add(whiteButton);
        selGroup.add(grayButton);
        selGroup.add(darkButton);
        panel.add(whiteButton);
        panel.add(grayButton);
        panel.add(darkButton);
        previewPanel.add(panel);
        return previewPanel;
    }
    
    private void setBackGround(Color color) {
        _preview.setBackground(color);
        _currentBackground = color;
        Component[] comp = _preview.getComponents();
        for (int i=0; i<comp.length; i++){
            JLabel l = null;
            if (comp[i].getClass().getName().equals("javax.swing.JPanel")) {
                JPanel p = (JPanel)comp[i];
                p.setBackground(color);
                l = (JLabel)p.getComponent(0);
            } else if (comp[i].getClass().getName().equals("javax.swing.JLabel")) {
                l = (JLabel)comp[i];
            } else {
                if (log.isDebugEnabled()) log.debug("setBackGround label #"+i+
                                                    ", class= "+comp[i].getClass().getName());
                return;
            }
            l.setBackground(color);
        }
        _preview.invalidate();
    }

    void resetPanel() {
        _selectedImage = null;
        if (_preview == null) {
            return;
        }
        if (log.isDebugEnabled()) log.debug("resetPanel");
        Component[] comp = _preview.getComponents();
        for (int i=comp.length-1; i>=0; i--) {
            _preview.remove(i);
            comp[i] = null;
        }
        _preview.removeAll();
        _preview.setBackground(_currentBackground);
        _preview.invalidate();
        pack();
    }

    public int getNumFilesShown() {
        return _startNum + _cnt;
    }

    class MemoryExceptionHandler implements Thread.UncaughtExceptionHandler {
        public void uncaughtException(Thread t, Throwable e) {
            _noMemory = true;
            log.error("Exception from setIcons: "+e, e);
            if (log.isDebugEnabled()) log.debug("memoryAvailable = "+availableMemory());
        }
    }

    boolean _noMemory = false;
    
    /**
    *  Displays (thumbnails if image is large) of the current directory.
    *  Number of images displayed may be restricted due to memory constraints.
    *  Returns true if memory limits displaying all the images
    */
    private boolean setIcons(int startNum) throws OutOfMemoryError {
        // VM launches another thread to run ImageFetcher.
        // This handler will catch memory exceptions from that thread
        _noMemory = false;
        Thread.setDefaultUncaughtExceptionHandler(new MemoryExceptionHandler());
        int numCol = 6;
        int numRow = 5;
        long memoryNeeded = 0;
        // allow room for ImageFetcher threads
        long memoryAvailable = availableMemory() - 10000000;
        if (log.isDebugEnabled()) log.debug("setIcons: startNum= "+startNum+" memoryAvailable = "+availableMemory());
        boolean newCol = false;
        GridBagLayout gridbag = new GridBagLayout();
        _preview.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridy = 0;
        c.gridx = -1;
        _cnt = 0;
        int cnt = 0;
        File[] files = _currentDir.listFiles();
        for (int i=0; i<files.length; i++) {
            String ext = jmri.util.FileChooserFilter.getFileExtension(files[i]);
            for (int k=0; k<_filter.length; k++) {
                if (ext != null && ext.equalsIgnoreCase(_filter[k])) {
                    if (cnt < startNum || _noMemory) {
                        cnt++;
                        continue;
                    }
                    String name = files[i].getName();
                    int index = name.indexOf('.');
                    if (index > 0) {
                        name = name.substring(0, index);
                    }
                     try {
                        String path = files[i].getAbsolutePath();
                        NamedIcon icon = new NamedIcon(path, name);
                        memoryNeeded += 3*icon.getIconWidth()*icon.getIconHeight();
                        if (memoryAvailable < memoryNeeded) {
                            _noMemory = true;
                            continue;
                        }
                        double scale = icon.reduceTo(CatalogPanel.ICON_WIDTH, 
                                                     CatalogPanel.ICON_HEIGHT, CatalogPanel.ICON_SCALE);
                        if (_noMemory) {
                            continue;
                        }
                           if (c.gridx < numCol) {
                            c.gridx++;
                        } else if (c.gridy < numRow) { //start next row
                            c.gridy++;
                            if (!newCol) {
                                c.gridx=0;
                            }
                        } else if (!newCol) { // start new column
                            c.gridx++;
                            numCol++;
                            c.gridy = 0;
                            newCol = true;
                        } else {  // start new row
                            c.gridy++;
                            numRow++;
                            c.gridx = 0;
                            newCol = false;
                        }                    
                        c.insets = new Insets(5, 5, 0, 0);
                        JLabel image;
                        if (_mode){
                            image = new JLabel();
                        } else {
                            //modeless is for ImageEditor dragging
                            try {
                                image = new DragJLabel(new DataFlavor(ImageIndexEditor.IconDataFlavorMime));
                            } catch (java.lang.ClassNotFoundException cnfe) {
                                cnfe.printStackTrace();
                                image = new JLabel();
                            }
                        }
                        image.setOpaque(true);
                        image.setName(name);
                        image.setBackground(_currentBackground);
                        image.setIcon(icon);
                        JPanel p = new JPanel();
                        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                        p.add(image);
                        if (name.length()>18) {
                            name = name.substring(0, 18);
                        }
                        JLabel nameLabel = new JLabel(name);
                        JLabel label = new JLabel(java.text.MessageFormat.format(Bundle.getMessage("scale"),
                                            new Object[] {CatalogPanel.printDbl(scale,2)}));
                        p.add(label);
                        p.add(nameLabel);
                        gridbag.setConstraints(p, c);
                        if (_noMemory) {
                            continue;
                        }
                        if (log.isDebugEnabled()) log.debug(name+" inserted at ("+c.gridx+", "+c.gridy+")");
                        _preview.add(p);
                        _cnt++;
                        cnt++;
                        if (_cnt > 300) { // somewhere above this number, VM can't build display of panel
                            _noMemory = true;
                        }
                    } catch (OutOfMemoryError oome) {
                        JOptionPane.showMessageDialog(this, 
                                java.text.MessageFormat.format(Bundle.getMessage("OutOfMemory"), 
                                new Object[] {Integer.valueOf(_cnt)}),
                                Bundle.getMessage("error"), JOptionPane.INFORMATION_MESSAGE);
                        _noMemory = true;
                    }
                }
            }
        }
        c.gridy++;
        c.gridx++;
        JLabel bottom = new JLabel();
        gridbag.setConstraints(bottom, c);
        _preview.add(bottom);
        String msg = java.text.MessageFormat.format(Bundle.getMessage("numImagesInDir"),
                              new Object[] {_currentDir.getName(), Integer.valueOf(cnt)});
        if (startNum>0) {
            msg = msg +" "+ java.text.MessageFormat.format(Bundle.getMessage("numImagesShown"), 
                              new Object[] {Integer.valueOf(startNum)});
        }
        _previewLabel.setText(msg);
        _preview.setMinimumSize(new Dimension(CatalogPanel.ICON_WIDTH, 2*CatalogPanel.ICON_HEIGHT));
        CatalogPanel.packParentFrame(this);

        if (_noMemory) {
            JOptionPane.showMessageDialog(this, 
                    java.text.MessageFormat.format(Bundle.getMessage("OutOfMemory"), 
                    new Object[] {Integer.valueOf(_cnt)}),
                    Bundle.getMessage("error"), JOptionPane.INFORMATION_MESSAGE);
        }
        Thread.setDefaultUncaughtExceptionHandler(new jmri.util.exceptionhandler.UncaughtExceptionHandler());
        return _noMemory;
    }

    static int CHUNK = 500000;

    private long availableMemory() {
        long total = 0;
        ArrayList <byte[]> memoryTest = new ArrayList <byte[]>();
        try {
            while (true) {
                memoryTest.add(new byte[CHUNK]);
                total += CHUNK;
            }
        } catch (OutOfMemoryError me) {
            for (int i=0; i<memoryTest.size(); i++){
                memoryTest.remove(i);
            }
            memoryTest = null;
            //if (log.isDebugEnabled()) log.debug("Max Memory available= "+total+" bytes");
        }
        return total;
    }

    public void dispose() {
        if (_preview != null) resetPanel();
        this.removeAll();
        _preview = null;
        super.dispose();
        log.debug("PreviewDialog disposed.");
    }
    
    static Logger log = LoggerFactory.getLogger(PreviewDialog.class.getName());
}


