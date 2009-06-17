// PreviewDialog.java

package jmri.jmrit.catalog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
/**
 * Create a Dialog to display the images in a file system directory.
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
    int _totalCnt;      // total number of files displayed from a directory

    File _currentDir;   // current FS directory
    String[] _filter;   // file extensions of types to display
    JButton _addButton;
    JButton _moreButton;

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.catalog.CatalogBundle");

    public PreviewDialog(Frame frame, String title, File dir, String[] filter, boolean modality ) {
        super(frame, rb.getString(title), modality);
        _currentDir = dir;
        _filter = filter;
    }

    public void init(ActionListener addAction, ActionListener lookAction, ActionListener cancelAction) {

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel(_currentDir.getPath()));
        p.add(new JLabel(rb.getString("addDirMsg")));
        getContentPane().add(p, BorderLayout.NORTH);

        _moreButton = new JButton(rb.getString("ButtonDisplayMore"));
        _moreButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    displayMore();
                }
        });

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalStrut(5));
        p.add(_moreButton);

        JPanel previewPanel = setupPanel();     // provide panel for images, add to bottom of window
        _totalCnt = 0;
        displayMore();          // set _moreButton Visible?

        if (addAction != null) {
            if (_moreButton.isVisible()) {
                JOptionPane.showMessageDialog(this, 
                                      java.text.MessageFormat.format(rb.getString("tooManyIcons"), 
                                      new Object[] {_currentDir.getName()}),
                                      rb.getString("warn"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                p.add(Box.createHorizontalStrut(5));
                _addButton = new JButton(rb.getString("ButtonAddToCatalog"));
                _addButton.addActionListener(addAction);
                p.add(_addButton);
            }
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
        pack();
    }

    public void enableAddButton(boolean enable) {
        _addButton.setEnabled(enable);
    }

    void displayMore() {
        resetPanel();
        _totalCnt += _cnt;
        try {
            _moreButton.setVisible(setIcons(_totalCnt));
        } catch (OutOfMemoryError oome) {
            oome.printStackTrace();
            resetPanel();
            if (log.isDebugEnabled()) log.debug("setIcons threw OutOfMemoryError "+oome);
        }
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
        for (int i=0; i<comp.length; i++) {
            comp[i].removeMouseListener(this);
        }
        _preview.removeAll();
        _preview.setBackground(_currentBackground);
        _preview.invalidate();
        pack();
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
            //if (log.isDebugEnabled()) log.debug("setIcons: files["+i+"]= "+files[i].getName());
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
                    String path = files[i].getAbsolutePath();
                    //if (log.isDebugEnabled()) log.debug("setIcons: path= "+path);
                    NamedIcon icon = new NamedIcon(path, name);
                    int w = icon.getIconWidth();
                    int h = icon.getIconHeight();
                    if (memoryAvailable < memoryNeeded+(4*w*h)) {
                        JOptionPane.showMessageDialog(this, 
                                java.text.MessageFormat.format(rb.getString("OutOfMemory"), 
                                new Object[] {new Integer(_cnt)}),
                                rb.getString("error"), JOptionPane.INFORMATION_MESSAGE);
                        noMemory = true;
                        log.debug("\n1\navailableMemory= "+availableMemory()+", 'memoryNeeded'= "+memoryNeeded);
                        continue;
                    }
                    if (memoryAvailable < 3*memoryNeeded) {
                        log.debug("\2\navailableMemory= "+availableMemory()+", 'memoryNeeded'= "+memoryNeeded);
                    }
                    double scale = 1;
                    if (w > 100) {
                        scale = 100.0/(double)w;
                    }
                    if (h > 100) {
                        scale = Math.min(scale, 100.0/(double)h);
                    }
                    if (scale < 1) { // make a thumbnail
                        //scale = Math.max(scale, 0.25);  // but not too small
                        AffineTransform t = AffineTransform.getScaleInstance(scale, scale);
                        BufferedImage bufIm = new BufferedImage((int)Math.ceil(scale*w), (int)Math.ceil(scale*h), 
                                                                BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2d = bufIm.createGraphics();
                        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY); 
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g2d.drawImage(icon.getImage(), t, null);
                        icon.setImage(bufIm);
                        g2d.dispose();
                    }
                    memoryNeeded += icon.getIconWidth()*icon.getIconHeight() +
                                    icon.getIconWidth()+icon.getIconHeight();
                    if (memoryAvailable < memoryNeeded+2*CHUNK) {
                        JOptionPane.showMessageDialog(this, 
                                java.text.MessageFormat.format(rb.getString("OutOfMemory"), 
                                new Object[] {new Integer(_cnt)}),
                                rb.getString("error"), JOptionPane.INFORMATION_MESSAGE);
                        log.debug("\n3\navailableMemory= "+availableMemory()+", 'memoryNeeded'= "+memoryNeeded);
                        noMemory = true;
                        continue;
                    }
                    if (memoryAvailable < 3*memoryNeeded && !enoughMemory(memoryNeeded)) {
                        noMemory = true;
                        log.debug("\n4\navailableMemory= "+availableMemory()+", 'memoryNeeded'= "+memoryNeeded);
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
                    JLabel image = new DragJLabel();
                    image.setOpaque(true);
                    image.setName(name);
                    image.setBackground(_currentBackground);
                    image.setIcon(icon);
                    JPanel p = new JPanel();
                    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                    p.add(image);
                    p.add(new JLabel(java.text.MessageFormat.format(rb.getString("scale"),
                                        new Object[] {CatalogPanel.printDbl(scale,2)})));

                    p.addMouseListener(this);
                    gridbag.setConstraints(p, c);
                    if (log.isDebugEnabled()) log.debug(name+" inserted at ("+c.gridx+", "+c.gridy+")");
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
        setPreferredSize(new java.awt.Dimension(numCol*50, numRow*50));
        _previewLabel.setText(java.text.MessageFormat.format(rb.getString("numImagesInDir"),
                              new Object[] {_currentDir.getName(), new Integer(cnt),
                                  new Integer(_totalCnt)}));
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
            log.debug("OutOfMemoryError for "+total+" bytes");
        }
        return total;
    }

    private boolean enoughMemory(long bytes) {
        if (bytes > 500000)  {
            long total = bytes;
            int chunk = 0;
            ArrayList <byte[]> memoryTest = new ArrayList <byte[]>();
            try {
                while (total > 0) {
                    if (total > Integer.MAX_VALUE) {
                        chunk = Integer.MAX_VALUE;
                    } else {
                        chunk = (int)total;
                    }
                    memoryTest.add(new byte[chunk]);
                    total -= chunk;
                }
            } catch (OutOfMemoryError me) {
                for (int i=0; i<memoryTest.size(); i++){
                    memoryTest.remove(i);
                }
                memoryTest = null;
                log.debug("OutOfMemoryError for "+bytes+" bytes");
                JOptionPane.showMessageDialog(this, 
                        java.text.MessageFormat.format(rb.getString("OutOfMemory"), 
                        new Object[] {new Integer(_cnt)}),
                        rb.getString("error"), JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
            for (int i=0; i<memoryTest.size(); i++){
                memoryTest.remove(i);
            }
            memoryTest = null;
            System.gc();        // please take the hint...
        }
        return true;
    }

    public void dispose() {
        if (_preview != null) _preview.removeAll();
        this.removeAll();
        _preview = null;
        super.dispose();
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


