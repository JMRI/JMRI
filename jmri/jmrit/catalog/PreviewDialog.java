// PreviewDialog.java

package jmri.jmrit.catalog;

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
//import java.awt.RenderingHints;
//import java.awt.geom.AffineTransform;
//import java.awt.image.BufferedImage;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;
import java.util.ResourceBundle;

import java.io.File;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
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
public class PreviewDialog extends JDialog implements MouseListener {

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

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.catalog.CatalogBundle");

    public PreviewDialog(Frame frame, String title, File dir, String[] filter, boolean modality ) {
        super(frame, rb.getString(title), modality);
        _currentDir = dir;
        _filter = filter;
        _mode = modality;
    }

    public void init(ActionListener addAction, ActionListener moreAction, 
                     ActionListener lookAction, ActionListener cancelAction, int startNum) {

        addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
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
            _moreButton = new JButton(rb.getString("ButtonDisplayMore"));
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
                                      java.text.MessageFormat.format(rb.getString("tooManyIcons"), 
                                      new Object[] {_currentDir.getName()}),
                                      rb.getString("warn"), JOptionPane.INFORMATION_MESSAGE);
                msg.setText(rb.getString("moreMsg"));
            } else {
                p.add(Box.createHorizontalStrut(5));
                _addButton = new JButton(rb.getString("ButtonAddToCatalog"));
                _addButton.addActionListener(addAction);
                p.add(_addButton);
                msg.setText(rb.getString("addDirMsg"));
            }
        } else if (lookAction != null) {
            if (_moreButton.isVisible()) {
                msg.setText(rb.getString("moreMsg"));
            }
        }
        else {
            msg.setText(rb.getString("dragMsg"));
        }

        if (lookAction != null) {
            p.add(Box.createHorizontalStrut(5));
            JButton lookButton = new JButton(rb.getString("ButtonKeepLooking"));
            lookButton.addActionListener(lookAction);
            p.add(lookButton);
        }
        p.add(Box.createHorizontalStrut(5));
        JButton cancelButton = new JButton(rb.getString("ButtonCancel"));
        cancelButton.addActionListener(cancelAction);
        p.add(cancelButton);
        p.add(Box.createHorizontalStrut(5));
        p.setPreferredSize(new Dimension(400, 2*_moreButton.getPreferredSize().height));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(p);
        p.add(Box.createVerticalStrut(5));
        panel.add(new JSeparator());
        panel.add(previewPanel);
        getContentPane().add(panel);
        setMinimumSize(new Dimension(400,500));
        setLocationRelativeTo(null);
        setVisible(true);
        pack();
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
        JScrollPane js = new JScrollPane(_preview);                       
        previewPanel.add(js);
        _preview.setMinimumSize(new Dimension(200, 150));
        JRadioButton whiteButton = new JRadioButton(rb.getString("white"),false);
        JRadioButton grayButton = new JRadioButton(rb.getString("lightGray"),true);
        JRadioButton darkButton = new JRadioButton(rb.getString("darkGray"),false);
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
        pp.add(new JLabel(rb.getString("setBackground")));
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

    /**
    *  Return the icon sekected in the preview panel
    */
    public NamedIcon getSelectedIcon() {
        if (_selectedImage != null) {
            JLabel l = (JLabel)_selectedImage.getComponent(0);
            // deselect
            setSelectionBackground(_currentBackground);
            _selectedImage = null;
            return (NamedIcon)l.getIcon();
        }
        return null;
    }

    void setBackGround(Color color) {
        _preview.setBackground(color);
        _currentBackground = color;
        Component[] comp = _preview.getComponents();
        for (int i=0; i<comp.length; i++){
            comp[i].setBackground(color);
        }
        setSelectionBackground(Color.cyan);
        _preview.repaint();
    }

    void setSelectionBackground(Color color) {
        if (_selectedImage != null) {
            _selectedImage.getComponent(0).setBackground(color);
            _selectedImage.getComponent(1).setBackground(color);
            _selectedImage.setBackground(color);
        }
    }

    void resetPanel() {
        _selectedImage = null;
        if (_preview == null) {
            return;
        }
        if (log.isDebugEnabled()) log.debug("resetPanel");
        Component[] comp = _preview.getComponents();
        for (int i=comp.length-1; i>=0; i--) {
            comp[i].removeMouseListener(this);
            _preview.remove(i);
            comp[i] = null;
        }
        _preview.removeAll();
        System.gc();        // please take the hint...
        _preview.setBackground(_currentBackground);
        _preview.invalidate();
        pack();
    }

    public int getNumFilesShown() {
        return _startNum + _cnt;
    }

    /**
    *  Displays (thumbnails if image is large) of the current directory.
    *  Number of images displayed may be restricted due to memory constraints.
    *  Returns true if memory limits displaying all the images
    */
    private boolean setIcons(int startNum) throws OutOfMemoryError {
        if (log.isDebugEnabled()) log.debug("setIcons: startNum= "+startNum);
        int numCol = 6;
        int numRow = 5;
        int cellHeight = 0;
        int cellWidth = 0;
        long memoryNeeded = 0;
        long memoryAvailable = availableMemory();
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
        boolean noMemory = false;
        File[] files = _currentDir.listFiles();
        for (int i=0; i<files.length; i++) {
            String ext = jmri.util.FileChooserFilter.getFileExtension(files[i]);
            for (int k=0; k<_filter.length; k++) {
                if (ext != null && ext.equals(_filter[k])) {
                    if (cnt < startNum) {
                        cnt++;
                        continue;
                    }
                    if (noMemory) {
                        cnt++;
                        continue;
                    }
                    String name = files[i].getName();
                    int index = name.indexOf('.');
                    if (index > 0) {
                        name = name.substring(0, index);
                    }
                    if (memoryAvailable < 11*memoryNeeded) {
                        //log.debug("\n00\navailableMemory= "+memoryAvailable+", 'memoryNeeded'= "+memoryNeeded);
                        memoryAvailable = availableMemory();
                    }
                    if (memoryAvailable < 4*memoryNeeded) {
                        JOptionPane.showMessageDialog(this, 
                                java.text.MessageFormat.format(rb.getString("OutOfMemory"), 
                                new Object[] {new Integer(_cnt)}),
                                rb.getString("error"), JOptionPane.INFORMATION_MESSAGE);
                        noMemory = true;
                        //log.debug("\n1\navailableMemory= "+availableMemory()+", 'memoryNeeded'= "+memoryNeeded);
                        continue;
                    }
                    String path = files[i].getAbsolutePath();
                    NamedIcon icon = new NamedIcon(path, name);
                    double scale = icon.scale(0.0);

                    memoryNeeded += 4*icon.getIconWidth()*icon.getIconHeight();
                    if (memoryAvailable < 10*memoryNeeded) {
                        JOptionPane.showMessageDialog(this, 
                                java.text.MessageFormat.format(rb.getString("OutOfMemory"), 
                                new Object[] {new Integer(_cnt)}),
                                rb.getString("error"), JOptionPane.INFORMATION_MESSAGE);
                        //log.debug("\n3\navailableMemory= "+availableMemory()+", 'memoryNeeded'= "+memoryNeeded);
                        noMemory = true;
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
                        image = new DragJLabel();   //modeless is for ImageEditor dragging
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
                    JLabel label = new JLabel(java.text.MessageFormat.format(rb.getString("scale"),
                                        new Object[] {CatalogPanel.printDbl(scale,2)}));
                    p.add(label);
                    p.add(nameLabel);
                    if (cellHeight < icon.getIconHeight()) {
                        cellHeight = icon.getIconHeight()
                                        +label.getPreferredSize().height
                                        +nameLabel.getPreferredSize().height;
                    }
                    if (cellWidth < icon.getIconWidth()) {
                        cellWidth = Math.max(nameLabel.getPreferredSize().width, 
                                        Math.max(label.getPreferredSize().width, icon.getIconWidth()))+10;
                    }
                    p.addMouseListener(this);
                    gridbag.setConstraints(p, c);
                    if (log.isDebugEnabled()) log.debug(name+" inserted at ("+c.gridx+", "+c.gridy+") "
                                                        +memoryNeeded);
                    _preview.add(p);
                    _cnt++;
                    cnt++;
                    if (_cnt > 300) { // somewhere above this number, VM can't build display of panel
                        noMemory = true;
                    }
                }
            }
        }
        c.gridy++;
        c.gridx++;
        JLabel bottom = new JLabel();
        gridbag.setConstraints(bottom, c);
        _preview.add(bottom);
        _preview.setPreferredSize(new java.awt.Dimension(numCol*cellWidth, numRow*cellHeight));
        _previewLabel.setText(java.text.MessageFormat.format(rb.getString("numImagesInDir"),
                              new Object[] {_currentDir.getName(), new Integer(cnt),
                                  new Integer(startNum)}));
        jmri.jmrit.display.IconAdder.getParentFrame(this).pack();
        //log.debug("\n6\navailableMemory= "+availableMemory()+", 'memoryNeeded'= "+memoryNeeded);
        return noMemory;
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
            if (log.isDebugEnabled()) log.debug("Max Memory available= "+total+" bytes");
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

    public void mouseClicked(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
        setSelectionBackground(_currentBackground);

        _selectedImage = (JPanel)e.getSource();
        setSelectionBackground(Color.cyan);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PreviewDialog.class.getName());
}


