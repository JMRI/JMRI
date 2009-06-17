// ImageIndexEditor.java

package jmri.jmrit.catalog;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
//import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

//import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JMenuItem;
//import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
//import javax.swing.JScrollPane;
//import javax.swing.JTree;
import javax.swing.SwingConstants;
/*
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
*/
import jmri.util.JmriJFrame;
import jmri.jmrit.display.IconAdder;
//import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.XmlFile;
import jmri.CatalogTreeManager;
//import jmri.CatalogTree;
import jmri.InstanceManager;

/**
 * A JFrame for creating and editing an Image Index
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
public class ImageIndexEditor extends JmriJFrame {

    CatalogPanel    _catalog;
    CatalogPanel    _index;
    PreviewDialog   _previewDialog;

    static ImageIndexEditor _instance;
    static public boolean  _indexChanged = false;
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.catalog.CatalogBundle");

    public static final String IconDataFlavorMime = DataFlavor.javaJVMLocalObjectMimeType +
               ";class=jmri.jmrit.catalog.NamedIcon";


    private ImageIndexEditor() {
        super();
    }

    private ImageIndexEditor(String name) {
        super(name);
    }

    public static ImageIndexEditor instance() {
        if (_instance == null) {
            _instance = new ImageIndexEditor(rb.getString("editIndexFrame"));
            _instance.init();
        }
        return _instance;
    }

    private void init() {
        JMenuBar menuBar = new JMenuBar();
        JMenu findIcon = new JMenu(IconAdder.rb.getString("findIconMenu"));
        menuBar.add(findIcon);
        JMenuItem storeItem = new JMenuItem(IconAdder.rb.getString("MIStoreImageIndex"));
        findIcon.add(storeItem);
        storeItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					storeImageIndex();
                }
            });
        findIcon.addSeparator();
        JMenuItem openItem = new JMenuItem(IconAdder.rb.getString("openDirMenu"));
        openItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    openDirectory();
                }
            });
        findIcon.add(openItem);

        JMenu editMenu = new JMenu(rb.getString("EditIndexMenu"));
        menuBar.add(editMenu);
        JMenuItem addItem = new JMenuItem(rb.getString("addNode"));
        addItem.addActionListener (new ActionListener () {
                public void actionPerformed(ActionEvent e) {
                    addNode();
                }
            });
        editMenu.add(addItem);
        JMenuItem renameItem = new JMenuItem(rb.getString("renameNode"));
        renameItem.addActionListener (new ActionListener () {
                public void actionPerformed(ActionEvent e) {
                    renameNode();
                }
            });
        editMenu.add(renameItem);
        JMenuItem deleteItem = new JMenuItem(rb.getString("deleteNode"));
        deleteItem.addActionListener (new ActionListener () {
                public void actionPerformed(ActionEvent e) {
                    deleteNode();
                }
            });
        editMenu.add(deleteItem);
        setJMenuBar(menuBar);

        addHelpMenu("package.jmri.jmrit.catalog.ImageIndex", true);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        mainPanel.add(makeCatalogPanel());
        mainPanel.add(new JSeparator(SwingConstants.VERTICAL));
        mainPanel.add(makeIndexPanel());
        getContentPane().add(mainPanel);

        // when this window closes, check for saving 
        addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    checkImageIndex();
                }
            });
        setLocation(10, 200);
        pack();
        setVisible(true);
    }

    /**
    *  Called from window close of Icon Editors
    */
    public static void checkImageIndex() {
        if (jmri.jmrit.catalog.ImageIndexEditor._indexChanged) {
            int result = JOptionPane.showConfirmDialog(null, rb.getString("SaveImageIndex"), 
                                          rb.getString("question"), JOptionPane.YES_NO_CANCEL_OPTION,
                                                       JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                storeImageIndex();
            } else if (result == JOptionPane.NO_OPTION) {
                _indexChanged = false;
            }
        }
    }

    public static void storeImageIndex() {

        if (log.isDebugEnabled()) log.debug("Start writing CatalogTree info");
        try {
            new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().writeCatalogTrees();
        } 
        catch (org.jdom.JDOMException jde) { log.error("Exception writing CatalogTrees: "+jde); }                           
        catch (java.io.IOException ioe) { log.error("Exception writing CatalogTrees: "+ioe); }   
    }

    private void openDirectory() {
        File dir = IconAdder.getDirectory();
        if (dir != null) {
            _previewDialog = new PreviewDialog(this, "previewDir",  
                                 dir, CatalogTreeManager.IMAGE_FILTER, false );
            //_previewDialog.setModalityType(java.awt.Dialog.ModalityType.MODELESS); SDK1.6
            _previewDialog.init(null, null, new ActionListener() {
                                                public void actionPerformed(ActionEvent a) {
                                                    cancel();
                                                }
                                            }
            );
            _previewDialog.setVisible(true);
        }
    }

    void cancel() {
        _previewDialog.dispose();
        _previewDialog = null;
    }

    private JPanel makeCatalogPanel() {
        _catalog = new CatalogPanel("catalog", "selectNode");
        _catalog.init(false);
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        List sysNames = manager.getSystemNameList();
        if (sysNames != null) {
            for (int i=0; i<sysNames.size(); i++) {
                String systemName = (String)sysNames.get(i);
                if (systemName.startsWith("IF")) {
                    _catalog.addTree( manager.getBySystemName(systemName));
                }
            }
        }
        _catalog.createNewBranch("IFJAR", "resourceJar", "resources");
        XmlFile.ensurePrefsPresent("resources");
        _catalog.createNewBranch("IFPREF", "preferenceDir", XmlFile.prefsDir()+"resources");
        return _catalog;
    }

    private JPanel makeIndexPanel() {
        _index = new CatalogPanel("ImageIndex", "selectIndexNode");
        _index.init(true);

        boolean found = false;
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        List sysNames = manager.getSystemNameList();
        if (sysNames != null) {
            for (int i=0; i<sysNames.size(); i++) {
                String systemName = (String)sysNames.get(i);
                if (systemName.startsWith("IX")) {
                    _index.addTree(manager.getBySystemName(systemName));
                    found = true;
                }
            }
        }
        if (!found) {
            _index.createNewBranch("IXII", rb.getString("ImageIndexRoot"), "ImageIndexRoot");
        }
       return _index;
    }

    void addNode() {
        CatalogTreeNode selectedNode = _index.getSelectedNode();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, rb.getString("selectAddNode"), 
                                          rb.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            String name = JOptionPane.showInputDialog(this, rb.getString("nameAddNode"), 
                                          rb.getString("question"), JOptionPane.QUESTION_MESSAGE);
            if (name != null) {
                if(!_index.insertNodeIntoModel(name, selectedNode)) {
                    JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                        rb.getString("duplicateNodeName"), new Object[] {name}), 
                                        rb.getString("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    void renameNode() {
        CatalogTreeNode selectedNode = _index.getSelectedNode();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, rb.getString("selectRenameNode"), 
                                          rb.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            String name = JOptionPane.showInputDialog(this, rb.getString("newNameNode"), 
                                          (String)selectedNode.getUserObject());
            if (name != null) {
                if (!_index.NodeChange(selectedNode, name)){
                    JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                                        rb.getString("duplicateNodeName"), new Object[] {name}), 
                                        rb.getString("error"), JOptionPane.ERROR_MESSAGE);
                }

            }
        }
    }

    void deleteNode() {
        CatalogTreeNode selectedNode = _index.getSelectedNode();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, rb.getString("selectDeleteNode"), 
                                          rb.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            int numNodes = countSubNodes(selectedNode);
            int numIcons = countIcons(selectedNode);
            int response = JOptionPane.showConfirmDialog(this, java.text.MessageFormat.format(
                                    rb.getString("confirmDeleteNode"), new Object[] 
                                        {(String)selectedNode.getUserObject(), new Integer(numNodes), new Integer(numIcons)}),
                                        rb.getString("question"), JOptionPane.YES_NO_OPTION,
                                                        JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                _index.removeNodeFromModel(selectedNode);
            }
        }
    }

    private int countSubNodes(CatalogTreeNode node) {
        int cnt = 0;
        Enumeration e =node.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = (CatalogTreeNode)e.nextElement();
            cnt += countSubNodes(n) + 1;
        }
        return cnt;
    }

    private int countIcons(CatalogTreeNode node) {
        int cnt = 0;
        Enumeration e =node.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = (CatalogTreeNode)e.nextElement();
            cnt += countIcons(n);
        }
        cnt += node.getNumLeaves();
        return cnt;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ImageIndexEditor.class.getName());
}


