package jmri.jmrit.catalog;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.List;
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
 * @author Pete Cressman Copyright 2009
 *
 */
public final class ImageIndexEditor extends JmriJFrame {

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
        JMenu findIcon = new JMenu(Bundle.getMessage("MenuFile"));
        menuBar.add(findIcon);
        JMenuItem storeItem = new JMenuItem(Bundle.getMessage("MIStoreImageIndex"));
        findIcon.add(storeItem);
        storeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                storeImageIndex();
            }
        });

        findIcon.addSeparator();
        JMenuItem openItem = new JMenuItem(Bundle.getMessage("openDirMenu"));
        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DirectorySearcher.instance().openDirectory();
            }
        });
        findIcon.add(openItem);

        JMenuItem searchItem = new JMenuItem(Bundle.getMessage("searchFSMenu"));
        searchItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jmri.jmrit.catalog.DirectorySearcher.instance().searchFS();
            }
        });
        findIcon.add(searchItem);

        JMenu editMenu = new JMenu(Bundle.getMessage("EditIndexMenu"));
        menuBar.add(editMenu);
        JMenuItem addItem = new JMenuItem(Bundle.getMessage("addNode"));
        addItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNode();
            }
        });
        editMenu.add(addItem);
        JMenuItem renameItem = new JMenuItem(Bundle.getMessage("renameNode"));
        renameItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                renameNode();
            }
        });
        editMenu.add(renameItem);
        JMenuItem deleteItem = new JMenuItem(Bundle.getMessage("deleteNode"));
        deleteItem.addActionListener(new ActionListener() {
            @Override
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
        labelPanel.add(new JLabel(Bundle.getMessage("dragIconsInstr"), SwingConstants.LEFT));
        mainPanel.add(labelPanel);
        JPanel catalogsPanel = new JPanel();
        catalogsPanel.setLayout(new BoxLayout(catalogsPanel, BoxLayout.X_AXIS));
        catalogsPanel.add(makeCatalogPanel());
        catalogsPanel.add(new JSeparator(SwingConstants.VERTICAL));
        catalogsPanel.add(makeIndexPanel());
        mainPanel.add(catalogsPanel);
        getContentPane().add(mainPanel);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                DirectorySearcher.instance().close();
            }
        });
        setLocation(10, 200);
        pack();
        setVisible(true);
    }

    public static final synchronized void indexChanged(boolean changed) {
        _indexChanged = changed;
        if (jmri.InstanceManager.getNullableDefault(jmri.ShutDownManager.class) != null) {
            if (changed) {
                if (_shutDownTask == null) {
                    _shutDownTask = new SwingShutDownTask("PanelPro Save default icon check",
                            Bundle.getMessage("IndexChanged"),
                            Bundle.getMessage("SaveAndQuit"), null) {
                                @Override
                                public boolean checkPromptNeeded() {
                                    return !_indexChanged;
                                }

                                @Override
                                public boolean doPrompt() {
                                    storeImageIndex();
                                    return true;
                                }
                            };
                    jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).register(_shutDownTask);
                }
            } else {
                if (_shutDownTask!=null) {
                    jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(_shutDownTask);
                    _shutDownTask = null;                    
                }
            }
        }
    }

    public static boolean isIndexChanged() {
        return _indexChanged;
    }

    public static void storeImageIndex() {
        jmri.jmrit.display.palette.ItemPalette.storeIcons();

        if (log.isDebugEnabled()) {
            log.debug("Start writing CatalogTree info");
        }
        try {
            new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().writeCatalogTrees();
            indexChanged(false);
        }                           
        catch (java.io.IOException ioe) {
            log.error("Exception writing CatalogTrees: {}", ioe);
        }
    }

    private JPanel makeCatalogPanel() {
        _catalog = new CatalogPanel("defaultCatalog", "selectNode");
        _catalog.init(false);
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        List<String> sysNames = manager.getSystemNameList();

        for (int i = 0; i < sysNames.size(); i++) {
            String systemName = sysNames.get(i);
            if (systemName.startsWith("IF")) {
                _catalog.addTree(manager.getBySystemName(systemName));
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
        CatalogTreeManager manager = InstanceManager.getDefault(jmri.CatalogTreeManager.class);
        List<String> sysNames = manager.getSystemNameList();

        for (int i = 0; i < sysNames.size(); i++) {
            String systemName = sysNames.get(i);
            if (systemName.startsWith("IX")) {
                _index.addTree(manager.getBySystemName(systemName));
                found = true;
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
                    Bundle.getMessage("QuestionTitle"), JOptionPane.QUESTION_MESSAGE);
            if (name != null) {
                if (!_index.insertNodeIntoModel(name, selectedNode)) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("duplicateNodeName", name),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        invalidate();
    }

    private void renameNode() {
        CatalogTreeNode selectedNode = _index.getSelectedNode();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("selectRenameNode"),
                    Bundle.getMessage("info"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            String name = JOptionPane.showInputDialog(this, Bundle.getMessage("newNameNode"),
                    selectedNode.getUserObject());
            if (name != null) {
                if (!_index.nodeChange(selectedNode, name)) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("duplicateNodeName", name),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                }

            }
        }
        invalidate();
    }

    private void deleteNode() {
        CatalogTreeNode selectedNode = _index.getSelectedNode();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("selectDeleteNode"),
                    Bundle.getMessage("info"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (log.isDebugEnabled())  log.debug("delete node \"{}\" level= {}.", selectedNode, selectedNode.getLevel());
        if (selectedNode.getLevel() <= 1) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("deleteRootNode"),
                    Bundle.getMessage("info"), JOptionPane.INFORMATION_MESSAGE);
            return;
        } else {
            int numNodes = countSubNodes(selectedNode);
            int numIcons = countIcons(selectedNode);
            int response = JOptionPane.showConfirmDialog(this, 
                    Bundle.getMessage("confirmDeleteNode", selectedNode.getUserObject(), numNodes, numIcons),
                    Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                _index.removeNodeFromModel(selectedNode);
            }
        }
    }

    @SuppressWarnings("unchecked")
    int countSubNodes(CatalogTreeNode node) {
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
