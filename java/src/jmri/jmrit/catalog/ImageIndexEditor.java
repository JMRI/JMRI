// ImageIndexEditor.java
package jmri.jmrit.catalog;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import jmri.CatalogTreeManager;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.implementation.swing.SwingShutDownTask;
import jmri.jmrit.display.Editor;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JFrame for creating and editing an Image Index. This is a singleton class.
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
 *
 * @author	Pete Cressman Copyright 2009
 *
 */
public final class ImageIndexEditor extends JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = 8308930846582782285L;
    CatalogPanel _catalog;
    CatalogPanel _index;

    static ImageIndexEditor _instance;
    static boolean _indexChanged = false;
    static ShutDownTask _shutDownTask;

    public static final String IconDataFlavorMime = DataFlavor.javaJVMLocalObjectMimeType
            + ";class=jmri.jmrit.catalog.NamedIcon";

    private ImageIndexEditor() {
        super();
    }

    private ImageIndexEditor(String name) {
        super(name);
    }

    public static ImageIndexEditor instance(Editor editor) {
        if (_instance == null) {
            _instance = new ImageIndexEditor(Bundle.getMessage("editIndexFrame"));
            _instance.init(editor);
        }
        return _instance;
    }

    private void init(Editor editor) {
        JMenuBar menuBar = new JMenuBar();
        JMenu findIcon = new JMenu(ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle").getString("MenuFile"));
        menuBar.add(findIcon);
        JMenuItem storeItem = new JMenuItem(ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle").getString("MIStoreImageIndex"));
        findIcon.add(storeItem);
        storeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                storeImageIndex();
            }
        });

        findIcon.addSeparator();
        JMenuItem openItem = new JMenuItem(Bundle.getMessage("openDirMenu"));
        openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DirectorySearcher.instance().openDirectory(false);
            }
        });
        findIcon.add(openItem);

        JMenu editMenu = new JMenu(Bundle.getMessage("EditIndexMenu"));
        menuBar.add(editMenu);
        JMenuItem addItem = new JMenuItem(Bundle.getMessage("addNode"));
        addItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addNode();
            }
        });
        editMenu.add(addItem);
        JMenuItem renameItem = new JMenuItem(Bundle.getMessage("renameNode"));
        renameItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                renameNode();
            }
        });
        editMenu.add(renameItem);
        JMenuItem deleteItem = new JMenuItem(Bundle.getMessage("deleteNode"));
        deleteItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteNode();
            }
        });
        editMenu.add(deleteItem);
        setJMenuBar(menuBar);

        addHelpMenu("package.jmri.jmrit.catalog.ImageIndex", true);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        JPanel labelPanel = new JPanel();
        String msg = java.text.MessageFormat.format(
                Bundle.getMessage("dragIcons"),
                new Object[]{Bundle.getMessage("defaultCatalog"), Bundle.getMessage("ImageIndex")});
        labelPanel.add(new JLabel(msg, SwingConstants.LEFT));
        mainPanel.add(labelPanel);
        JPanel catalogsPanel = new JPanel();
        catalogsPanel.setLayout(new BoxLayout(catalogsPanel, BoxLayout.X_AXIS));
        catalogsPanel.add(makeCatalogPanel());
        catalogsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        catalogsPanel.add(makeIndexPanel());
        mainPanel.add(catalogsPanel);
        getContentPane().add(mainPanel);

        // when this window closes, check for saving 
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                DirectorySearcher.instance().close();
                checkImageIndex();
            }
        });
        setLocation(10, 200);
        pack();
        setVisible(true);
    }

    public static final synchronized void indexChanged(boolean changed) {
        _indexChanged = changed;
        if (jmri.InstanceManager.shutDownManagerInstance() != null) {
            if (changed) {
                if (_shutDownTask == null) {
                    _shutDownTask = new SwingShutDownTask("PanelPro Save default icon check",
                            Bundle.getMessage("IndexChanged"),
                            Bundle.getMessage("SaveAndQuit"), null) {
                                public boolean checkPromptNeeded() {
                                    return !_indexChanged;
                                }

                                public boolean doPrompt() {
                                    storeImageIndex();
                                    return true;
                                }
                            };
                    jmri.InstanceManager.shutDownManagerInstance().register(_shutDownTask);
                }
            }
        }
    }

    public static boolean isIndexChanged() {
        return _indexChanged;
    }

    /**
     * Called from window close of Icon Editors
     */
    public static boolean checkImageIndex() {
        if (_indexChanged) {
            int result = JOptionPane.showConfirmDialog(null, Bundle.getMessage("SaveImageIndex"),
                    Bundle.getMessage("question"), JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                storeImageIndex();
                return true;
            } else if (result == JOptionPane.NO_OPTION) {
                indexChanged(false);
            }
        }
        return false;
    }

    public static void storeImageIndex() {
        jmri.jmrit.display.palette.ItemPalette.storeIcons();

        if (log.isDebugEnabled()) {
            log.debug("Start writing CatalogTree info");
        }
        try {
            new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().writeCatalogTrees();
            indexChanged(false);
        } //catch (org.jdom2.JDOMException jde) { log.error("Exception writing CatalogTrees: "+jde); }                           
        catch (java.io.IOException ioe) {
            log.error("Exception writing CatalogTrees: " + ioe);
        }
    }

    private JPanel makeCatalogPanel() {
        _catalog = new CatalogPanel("defaultCatalog", "selectNode");
        _catalog.init(false);
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        List<String> sysNames = manager.getSystemNameList();
        if (sysNames != null) {
            for (int i = 0; i < sysNames.size(); i++) {
                String systemName = sysNames.get(i);
                if (systemName.startsWith("IF")) {
                    _catalog.addTree(manager.getBySystemName(systemName));
                }
            }
        }
        _catalog.createNewBranch("IFJAR", "Program Directory", "resources");
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "resources");
        _catalog.createNewBranch("IFPREF", "Preferences Directory", FileUtil.getUserFilesPath() + "resources");
        return _catalog;
    }

    private JPanel makeIndexPanel() {
        _index = new CatalogPanel("ImageIndex", "selectIndexNode");
        _index.init(true);

        boolean found = false;
        CatalogTreeManager manager = InstanceManager.catalogTreeManagerInstance();
        List<String> sysNames = manager.getSystemNameList();
        if (sysNames != null) {
            for (int i = 0; i < sysNames.size(); i++) {
                String systemName = sysNames.get(i);
                if (systemName.startsWith("IX")) {
                    _index.addTree(manager.getBySystemName(systemName));
                    found = true;
                }
            }
        }
        if (!found) {
            _index.createNewBranch("IXII", Bundle.getMessage("ImageIndexRoot"), "ImageIndexRoot");
        }
        return _index;
    }

    void addNode() {
        CatalogTreeNode selectedNode = _index.getSelectedNode();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("selectAddNode"),
                    Bundle.getMessage("info"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            String name = JOptionPane.showInputDialog(this, Bundle.getMessage("nameAddNode"),
                    Bundle.getMessage("question"), JOptionPane.QUESTION_MESSAGE);
            if (name != null) {
                if (!_index.insertNodeIntoModel(name, selectedNode)) {
                    JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                            Bundle.getMessage("duplicateNodeName"), new Object[]{name}),
                            Bundle.getMessage("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        invalidate();
    }

    void renameNode() {
        CatalogTreeNode selectedNode = _index.getSelectedNode();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("selectRenameNode"),
                    Bundle.getMessage("info"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            String name = JOptionPane.showInputDialog(this, Bundle.getMessage("newNameNode"),
                    selectedNode.getUserObject());
            if (name != null) {
                if (!_index.nodeChange(selectedNode, name)) {
                    JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                            Bundle.getMessage("duplicateNodeName"), new Object[]{name}),
                            Bundle.getMessage("error"), JOptionPane.ERROR_MESSAGE);
                }

            }
        }
        invalidate();
    }

    void deleteNode() {
        CatalogTreeNode selectedNode = _index.getSelectedNode();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("selectDeleteNode"),
                    Bundle.getMessage("info"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            int numNodes = countSubNodes(selectedNode);
            int numIcons = countIcons(selectedNode);
            int response = JOptionPane.showConfirmDialog(this, java.text.MessageFormat.format(
                    Bundle.getMessage("confirmDeleteNode"), new Object[]{(String) selectedNode.getUserObject(), Integer.valueOf(numNodes), Integer.valueOf(numIcons)}),
                    Bundle.getMessage("question"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                _index.removeNodeFromModel(selectedNode);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private int countSubNodes(CatalogTreeNode node) {
        int cnt = 0;
        Enumeration<CatalogTreeNode> e = node.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = e.nextElement();
            cnt += countSubNodes(n) + 1;
        }
        return cnt;
    }

    @SuppressWarnings("unchecked")
    private int countIcons(CatalogTreeNode node) {
        int cnt = 0;
        Enumeration<CatalogTreeNode> e = node.children();
        while (e.hasMoreElements()) {
            CatalogTreeNode n = e.nextElement();
            cnt += countIcons(n);
        }
        cnt += node.getNumLeaves();
        return cnt;
    }

    private final static Logger log = LoggerFactory.getLogger(ImageIndexEditor.class.getName());
}
