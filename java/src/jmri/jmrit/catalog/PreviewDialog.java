package jmri.jmrit.catalog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
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
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a Dialog to display the images in a file system directory.
 * <BR>
 * PreviewDialog is not modal to allow dragNdrop of icons from it to catalog panels and
 * functioning of the catalog panels without dismissing this dialog
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
 *
 * @author Pete Cressman Copyright 2009
 *
 */
public class PreviewDialog extends JDialog {

    JPanel _selectedImage;
    static Color _grayColor = new Color(235, 235, 235);
    Color _currentBackground = _grayColor;

    JLabel _previewLabel = new JLabel();
    JPanel _preview;

    int _cnt;           // number of files displayed when setIcons() method runs
    int _startNum;      // total number of files displayed from a directory
    boolean needsMore = true;

    File _currentDir;   // current FS directory
    String[] _filter;   // file extensions of types to display
    ActionListener _lookAction;

    protected PreviewDialog(Frame frame, String title, File dir, String[] filter) {
        super(frame, Bundle.getMessage(title), false);
        _currentDir = dir;
        _filter = new String[filter.length];
        for (int i = 0; i < filter.length; i++) {
            _filter[i] = filter[i];
        }
    }

    protected void init(ActionListener moreAction, ActionListener lookAction, ActionListener cancelAction, int startNum) {
        if (log.isDebugEnabled()) {
            log.debug("Enter _previewDialog.init dir= {}", _currentDir.getPath());
        }
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                InstanceManager.getDefault(DirectorySearcher.class).close();
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

        JPanel previewPanel = setupPanel();     // provide panel for images, add to bottom of window
        _startNum = startNum;
        needsMore = setIcons(startNum);
        if (_noMemory) {
            int choice = JOptionPane.showOptionDialog(null,
                    Bundle.getMessage("OutOfMemory", _cnt), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                    new String[]{Bundle.getMessage("Quit"), Bundle.getMessage("ShowContents")}, 1);
            if (choice==0) {
                return;
            }
        }

        if (needsMore) {
            if (moreAction != null) {
                p.add(Box.createHorizontalStrut(5));
                JButton moreButton = new JButton(Bundle.getMessage("ButtonDisplayMore"));
                moreButton.addActionListener(moreAction);
                moreButton.setVisible(needsMore);
                p.add(moreButton);
            } else {
                log.error("More ActionListener missing");
            }
            msg.setText(Bundle.getMessage("moreMsg"));
        }

        boolean hasButtons = needsMore;
        msg.setText(Bundle.getMessage("dragMsg"));

        _lookAction = lookAction;
        if (lookAction != null) {
            p.add(Box.createHorizontalStrut(5));
            JButton lookButton = new JButton(Bundle.getMessage("ButtonKeepLooking"));
            lookButton.addActionListener(lookAction);
            p.add(lookButton);
            hasButtons = true;
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        if (hasButtons) {
            p.add(Box.createHorizontalStrut(5));
            JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
            cancelButton.addActionListener(cancelAction);
            p.add(cancelButton);
            p.add(Box.createHorizontalStrut(5));
            p.setPreferredSize(new Dimension(400, cancelButton.getPreferredSize().height));
            panel.add(p);
            panel.add(new JSeparator());
        }

        panel.add(previewPanel);
        getContentPane().add(panel);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }

    ActionListener getLookActionListener() {
        return _lookAction;
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
        JRadioButton whiteButton = new JRadioButton(Bundle.getMessage("white"), false);
        JRadioButton grayButton = new JRadioButton(Bundle.getMessage("lightGray"), true);
        JRadioButton darkButton = new JRadioButton(Bundle.getMessage("darkGray"), false);
        whiteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setBackGround(Color.white);
            }
        });
        grayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setBackGround(_grayColor);
            }
        });
        darkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setBackGround(new Color(150, 150, 150));
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
        for (int i = 0; i < comp.length; i++) {
            JLabel l = null;
            if (comp[i].getClass().getName().equals("javax.swing.JPanel")) {
                JPanel p = (JPanel) comp[i];
                p.setBackground(color);
                l = (JLabel) p.getComponent(0);
            } else if (comp[i].getClass().getName().equals("javax.swing.JLabel")) {
                l = (JLabel) comp[i];
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("setBackGround label #{}, class= {}", i, comp[i].getClass().getName());
                }
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
        if (log.isDebugEnabled()) {
            log.debug("resetPanel");
        }
        Component[] comp = _preview.getComponents();
        for (int i = comp.length - 1; i >= 0; i--) {
            _preview.remove(i);
            comp[i] = null;
        }
        _preview.removeAll();
        _preview.setBackground(_currentBackground);
        _preview.invalidate();
        pack();
    }

    protected int getNumFilesShown() {
        return _startNum + _cnt;
    }

    class MemoryExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            _noMemory = true;
            log.error("MemoryExceptionHandler: {} {} files read from directory {}", e, _cnt, _currentDir);
            if (log.isDebugEnabled()) {
                log.debug("memoryAvailable = {}", availableMemory());
            }
        }
    }

    boolean _noMemory = false;

    /**
     * Displays (thumbnails if image is large) of the current directory. Number
     * of images displayed may be restricted due to memory constraints. Returns
     * true if memory limits displaying all the images
     */
    private boolean setIcons(int startNum) {
        Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        // VM launches another thread to run ImageFetcher.
        // This handler will catch memory exceptions from that thread
        _noMemory = false;
        Thread.setDefaultUncaughtExceptionHandler(new MemoryExceptionHandler());
        // allow room for ImageFetcher threads
        if (log.isDebugEnabled()) {
            log.debug("setIcons: startNum= {}", startNum);
        }
        GridBagLayout gridbag = new GridBagLayout();
        _preview.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridy = -1;
        c.gridx = 0;
        _cnt = 0;       // number of images displayed in this panel
        int cnt = 0;    // total number of images in directory
        File[] files = _currentDir.listFiles(); // all files, filtered below
        int nCols = 1;
        int nRows = 1;
        int nAvail = 1;

        long memoryAvailable = availableMemory();
        long memoryUsed = 0;        // estmate
        for (int i = 0; i < files.length; i++) {
            String ext = jmri.util.FileChooserFilter.getFileExtension(files[i]);
            for (int k = 0; k < _filter.length; k++) {
                if (ext != null && ext.equalsIgnoreCase(_filter[k])) {
                    // files[i] filtered to be an image file
                    if (cnt < startNum) {
                        cnt++;
                        continue;
                    }
                    String name = files[i].getName();
                    int index = name.indexOf('.');
                    if (index > 0) {
                        name = name.substring(0, index);
                    }
                    String path = files[i].getAbsolutePath();
                    NamedIcon icon = new NamedIcon(path, name);
                    long size = icon.getIconWidth()*icon.getIconHeight();
                    log.debug("Memory calculation icon size= {} memoryAvailable= {} memoryUsed= {}", size, memoryAvailable, memoryUsed);

                    if (memoryAvailable < 4*size) {
                        _noMemory = true;
                        log.debug("Memory calculation caught icon size= {} testSize= {} memoryAvailable= {}", size, 4*size, memoryAvailable);
                        break;
                    }
                    double scale = icon.reduceTo(CatalogPanel.ICON_WIDTH,
                            CatalogPanel.ICON_HEIGHT, CatalogPanel.ICON_SCALE);
                    if (_noMemory) {
                        log.debug("MemoryExceptionHandler caught icon size={} ", size);
                        break;
                    }
                    if (scale < 1.0) {
                        size *= 4;
                    } else {
                        size += 1000;
                    }
                    memoryUsed += size;
                    memoryAvailable -= size;
                    _cnt++;
                    cnt++;
                    if (_cnt > nAvail) {
                        nCols++;
                        nRows++;
                        nAvail = nCols*nRows;
                        c.gridx = nCols-1;
                        c.gridy = 0;
                    } else if (_cnt > nAvail - nRows) {
                        if (c.gridx < nCols-1) {
                            c.gridx++;
                        } else {
                            c.gridx = 0;
                            c.gridy++;
                        }
                    } else {
                        c.gridy++;
                    }

                    c.insets = new Insets(5, 5, 0, 0);
                    JLabel image;
                    try {
                        image = new DragJLabel(new DataFlavor(ImageIndexEditor.IconDataFlavorMime));
                    } catch (java.lang.ClassNotFoundException cnfe) {
                        cnfe.printStackTrace();
                        image = new JLabel(cnfe.getMessage());
                    }
                    image.setOpaque(true);
                    //image.setName(name);
                    image.setBackground(_currentBackground);
                    image.setIcon(icon);
                    JPanel p = new JPanel();
                    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                    p.add(image);
                    if (name.length() > 18) {
                        name = name.substring(0, 18);
                    }
                    JLabel nameLabel = new JLabel(name);
                    JLabel label = new JLabel(Bundle.getMessage("scale", CatalogPanel.printDbl(scale, 2)));
                    p.add(label);
                    p.add(nameLabel);
                    gridbag.setConstraints(p, c);
                    if (log.isDebugEnabled()) {
                        log.debug("{} inserted at ({}, {})", name, c.gridx, c.gridy);
                    }
                    _preview.add(p);
                }
                if (_noMemory) {
                    break;
                }
            }
        }
        c.gridy++;
        c.gridx++;
        JLabel bottom = new JLabel();
        gridbag.setConstraints(bottom, c);
        _preview.add(bottom);
        String msg = Bundle.getMessage("numImagesInDir", _currentDir.getName(), DirectorySearcher.numImageFiles(_currentDir));
        if (startNum > 0) {
            msg = Bundle.getMessage("numImagesShown", msg, _cnt, startNum);
        }
        _previewLabel.setText(msg);
        CatalogPanel.packParentFrame(this);

        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        return _noMemory;
    }

    static int CHUNK = 500000;

    private long availableMemory() {
        long total = 0;
        ArrayList<byte[]> memoryTest = new ArrayList<byte[]>();
        try {
            while (true) {
                memoryTest.add(new byte[CHUNK]);
                total += CHUNK;
            }
        } catch (OutOfMemoryError me) {
            for (int i = 0; i < memoryTest.size(); i++) {
                memoryTest.remove(i);
            }
            if (log.isDebugEnabled()) log.debug("availableMemory= {}", total);
        }
        return total;
    }

    @Override
    public void dispose() {
        if (_preview != null) {
            resetPanel();
        }
        this.removeAll();
        _preview = null;
        super.dispose();
        log.debug("PreviewDialog disposed.");
    }

    private final static Logger log = LoggerFactory.getLogger(PreviewDialog.class);
}
