package jmri.jmrit.roster;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import jmri.util.datatransfer.RosterEntrySelection;
import jmri.util.swing.JmriAbstractAction;

/**
 * A JPanel that lists Roster Groups
 * <p>
 * This panel contains a fairly self-contained display of Roster Groups that
 * allows roster groups to be fully manipulated through context menus.
 *
 * @author  Randall Wood    Copyright (C) 2011
 * @see     jmri.jmrit.roster.Roster
 */
public class RosterGroupsPanel extends JPanel {

    /**
     * Property change listeners can listen for property changes with this name
     * from this object to take action when a user selects a roster group.
     */
    public final static String ROSTER_GROUP_SELECTED_EVENT = "RosterGroupSelected";
    private static int GROUPS_MENU = 1;
    private static int ALL_ENTRIES_MENU = 2;
    private JScrollPane scrollPane;
    private JTree _tree;
    private DefaultTreeModel _model;
    private DefaultMutableTreeNode _root;
    private DefaultMutableTreeNode _groups;
    //private DefaultMutableTreeNode _consists;
    private TreeSelectionListener _TSL;
    private boolean _usesActiveRosterGroup;
    private String selectedRosterGroup = "";
    private JPopupMenu groupsMenu;
    private JPopupMenu allEntriesMenu;
    private JMenuItem newWindowMenuItem;
    private JmriAbstractAction newWindowMenuItemAction = null;

    /**
     * Create a RosterGroupsPanel that tracks the active Roster Group
     */
    public RosterGroupsPanel() {
        this(null, true);
    }

    /**
     * Create a RosterGroupsPanel that optionally tracks the active Roster Group
     *
     * If you create a RosterGroupsPanel that does not track the active Roster Group,
     * you will need to add a PropertyChangeListener to the JTree returned by
     * RosterGroupsPanel.getTree() to respond to changes to the roster group.
     *
     * @param useActiveRosterGroup
     */
    public RosterGroupsPanel(boolean useActiveRosterGroup) {
        this(null, useActiveRosterGroup);
    }

    /**
     * Create a RosterGroupsPanel that does not track the active Roster Group
     * with the defaultRosterGroup selected.
     *
     * If the defaultRosterGroup is null, the selected Roster Group will be "All Entries"
     *
     * @param defaultRosterGroup
     */
    public RosterGroupsPanel(String defaultRosterGroup) {
        this(defaultRosterGroup, false);
    }

    /**
     * Create a RosterGroupTreePane with the defaultRosterGroup selected that optionally
     * tracks the active Roster Group.
     *
     * Note that if useActiveRosterGroup is true, the defaultRosterGroup is ignored.
     *
     * @param defaultRosterGroup
     * @param useActiveRosterGroup
     */
    public RosterGroupsPanel(String defaultRosterGroup, boolean useActiveRosterGroup) {
        this.scrollPane = new JScrollPane(getTree());
        this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setGroupsMenu(defaultMenu(GROUPS_MENU));
        setAllEntriesMenu(defaultMenu(ALL_ENTRIES_MENU));
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(getButtons(), BorderLayout.SOUTH);
        setUsesActiveRosterGroup(useActiveRosterGroup);
        setSelectedRosterGroup(defaultRosterGroup);
    }

    /**
     * Returns <code>true</code> if the RosterGroupsPanel selection changes
     * as the Active roster group changes, and if the the Active roster group
     * changes when the selection changes.
     *
     * @return flag indicating that the selection is the active roster group
     */
    public boolean getUsesActiveRosterGroup() {
        return _usesActiveRosterGroup;
    }

    /**
     * The selection in the RosterGroupsPanel may or may not reflect the active
     * roster group, used for other purposes throughout JMRI.
     *
     * @param useActiveRosterGroup flag the selection should be the active roster group
     */
    public final void setUsesActiveRosterGroup(boolean useActiveRosterGroup) {
        _usesActiveRosterGroup = useActiveRosterGroup;
        if (useActiveRosterGroup) {
            setSelectedRosterGroup(null);
        }
    }

    /**
     * Get the selected roster group.
     *
     * @return The selected roster group
     */
    public String getSelectedRosterGroup() {
        return selectedRosterGroup;
    }

    /**
     * Set the selected roster group. If the RosterGroupPanel uses the Active
     * Roster Group, this property will change the active roster group.
     * <p>
     * If the group is <code>null</code>, the selected roster group is set to "All Entries".
     * @param group The name of the group to set the selection to.
     */
    public final void setSelectedRosterGroup(String group) {
        if (group == null ? selectedRosterGroup != null : !group.equals(selectedRosterGroup)) {
            String oldGroup = selectedRosterGroup;
            selectedRosterGroup = group;
            setSelectionToGroup(group);
            firePropertyChange(ROSTER_GROUP_SELECTED_EVENT, oldGroup, group);
        }
    }

    /**
     * Is the selected roster group user or system defined.
     *
     * @return flag indicating current selection is a user defined roster group.
     */
    public boolean isSelectionUserDefinedRosterGroup() {
        return (selectedRosterGroup != null && !selectedRosterGroup.equals(Roster.ALLENTRIES));
    }

    /**
     * Set the context menu for Roster Groups
     *
     * @param menu The new menu for Roster Groups.
     */
    public final void setGroupsMenu(JPopupMenu menu) {
        this.groupsMenu = menu;
    }

    /**
     * Get the context menu for Roster Groups
     *
     * @return The current groups menu.
     */
    public JPopupMenu getGroupsMenu() {
        return this.groupsMenu;
    }

    /**
     * Set the context menu for the "All Entries" roster group
     * @param menu The new menu for All Entries.
     */
    public final void setAllEntriesMenu(JPopupMenu menu) {
        this.allEntriesMenu = menu;
    }

    /**
     * Get the context menu for "All Entries"
     * @return The menu for All Entries.
     */
    public JPopupMenu getAllEntriesMenu() {
        return this.allEntriesMenu;
    }

    /**
     * Set an action that the menu item "Open in New Window" will trigger.
     * <p>
     * Set a {@link JmriAbstractAction} that the "Open in New Window" menu item
     * will trigger. <code>null</code> will remove the "Open in New Window"
     * menu item from context menus. The "Open in New Window" menu item will be
     * added or removed from the menu as appropriate.
     * <p>
     * If the action you pass has access to the RosterGroupPanel, it may call
     * RosterGroupPanel.getSelectedRosterGroup to determine which group to open
     * in the new window, otherwise it must accept a String defining the group
     * in JmriAbstractAction.setParameter(String, String).
     * 
     * @param action - An action that can work on the current selection
     */
    public void setNewWindowMenuAction(JmriAbstractAction action) {
        if (action != null) {
            newWindowMenuItemAction = action;
            newWindowMenuItem = new JMenuItem("Open in New Window");
            newWindowMenuItem.addActionListener(action);
            newWindowMenuItem.setActionCommand("newWindow");
            groupsMenu.add(newWindowMenuItem, 0);
            groupsMenu.addSeparator();
            allEntriesMenu.add(newWindowMenuItem, 0);
            allEntriesMenu.addSeparator();
        } else if (newWindowMenuItem != null) {
            groupsMenu.remove(0);
            groupsMenu.remove(0);
            allEntriesMenu.remove(0);
            allEntriesMenu.remove(0);
            newWindowMenuItem = null;
            newWindowMenuItemAction = null;
        }
    }

    /**
     * The action triggered by the "Open in New Window" menu item.
     * @return A JmriAbstractAction or null
     */
    public JmriAbstractAction getNewWindowMenuAction() {
        return newWindowMenuItemAction;
    }

    private void setSelectionToGroup(String group) {
        _tree.removeTreeSelectionListener(_TSL);
        if (_usesActiveRosterGroup) {
            group = Roster.getRosterGroupName();
        }
        if (group == null || group.equals(Roster.ALLENTRIES) || group.equals("")) {
            _tree.setSelectionPath(new TreePath(_model.getPathToRoot(_groups.getFirstChild())));
        } else {
            for (Enumeration<DefaultMutableTreeNode> e = _groups.children(); e.hasMoreElements();) {
                DefaultMutableTreeNode n = e.nextElement();
                if (n.toString().equals(group)) {
                    _tree.setSelectionPath(new TreePath(_model.getPathToRoot(n)));
                }
            }
        }
        _tree.addTreeSelectionListener(_TSL);
    }

    private JToolBar getButtons() {
        JToolBar controls = new JToolBar();
        controls.setLayout(new GridLayout(1, 0, 0, 0));
        controls.setFloatable(false);
        final JToggleButton addGroupBtn = new JToggleButton(new ImageIcon("resources/icons/misc/gui3/Add.png"), false);
        final JToggleButton actGroupBtn = new JToggleButton(new ImageIcon("resources/icons/misc/gui3/Action.png"), false);
        addGroupBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {        
                new CreateRosterGroupAction("", scrollPane.getTopLevelAncestor()).actionPerformed(e);
                addGroupBtn.setSelected(false);
            }
        });
        actGroupBtn.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    TreePath g = new TreePath(_model.getPathToRoot(_groups));
                    if (_tree.getSelectionPath() != null) {
                        if (_tree.getSelectionPath().getLastPathComponent().toString().equals(Roster.ALLENTRIES)) {
                            allEntriesMenu.show((JComponent) ie.getSource(), actGroupBtn.getX() - actGroupBtn.getWidth(), actGroupBtn.getY() - allEntriesMenu.getPreferredSize().height);
                        } else if (g.isDescendant(_tree.getSelectionPath()) && !_tree.getSelectionPath().isDescendant(g)) {
                            groupsMenu.show((JComponent) ie.getSource(), actGroupBtn.getX() - actGroupBtn.getWidth(), actGroupBtn.getY() - groupsMenu.getPreferredSize().height);
                        }
                    }
                }
            }
        });
        PopupMenuListener PML = new PopupMenuListener() {

            public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
                // do nothing
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
                actGroupBtn.setSelected(false);
            }

            public void popupMenuCanceled(PopupMenuEvent pme) {
                actGroupBtn.setSelected(false);
            }
        };
        allEntriesMenu.addPopupMenuListener(PML);
        groupsMenu.addPopupMenuListener(PML);
        controls.add(addGroupBtn);
        controls.add(actGroupBtn);
        return controls;
    }

    /**
     * Get a JScrollPane containing the JTree that does not display horizontal scrollbars.
     *
     * @return The internal JScrollPane
     */
    public JScrollPane getScrollPane() {
        return this.scrollPane;
    }

    /**
     * Get the JTree containing the roster groups.
     *
     * @return The internal JTree
     */
    public final JTree getTree() {
        if (_tree == null) {
            TreeSelectionModel sm = new DefaultTreeSelectionModel();
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            sm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            renderer.setLeafIcon(null);
            renderer.setClosedIcon(null);
            renderer.setOpenIcon(null);
            _tree = new JTree(getModel());
            _tree.setUI(new TreeUI());
            _tree.putClientProperty("JTree.lineStyle", "None");
            _tree.setRootVisible(false);
            _tree.expandRow(0);
            _tree.setSelectionModel(sm);
            _tree.setCellRenderer(renderer);
            _tree.addTreeWillExpandListener(new TreeWillExpandListener());
            _TSL = new TreeSelectionListener();
            _tree.addTreeSelectionListener(_TSL);
            _tree.setDragEnabled(true);
            try {
                // Java 1.6
                _tree.setDropMode(DropMode.ON);
            } catch (java.lang.NoClassDefFoundError ex) {
                // Java 1.5
                log.info("Failed to set DropMode. Falling back on setting DropTarget.");
                _tree.setDropTarget(new DropTarget(_tree,
                        DnDConstants.ACTION_COPY,
                        new DropTargetListener(),
                        true));
            }
            _tree.setTransferHandler(new TransferHandler());
            _tree.addMouseListener(new MouseAdapter());
            setSelectionToGroup(selectedRosterGroup);
            Roster.instance().addPropertyChangeListener(new PropertyChangeListener());
        }
        return _tree;
    }

    private DefaultTreeModel getModel() {
        if (_model == null) {
            _model = new DefaultTreeModel(getRoot());
        }
        return _model;
    }

    private DefaultMutableTreeNode getRoot() {
        if (_root == null) {
            _root = new DefaultMutableTreeNode();
            _groups = new DefaultMutableTreeNode("Roster Groups");
            _root.add(_groups);
            setRosterGroups(_groups);
            // once consists can be displayed in the DP3 table, add them here
            //_consists = new DefaultMutableTreeNode("Consists");
            //setConsists(_consists);
            //_root.add(_consists);
        }
        return _root;
    }

    private JPopupMenu defaultMenu(int menu) {
        JPopupMenu pm = new JPopupMenu();
        MenuActionListener ml = new MenuActionListener();
        JMenuItem mi = new JMenuItem("Export...");
        mi.addActionListener(ml);
        mi.setActionCommand("export");
        pm.add(mi);
        if (menu == GROUPS_MENU) {
            pm.addSeparator();
            mi = new JMenuItem("Rename...");
            mi.addActionListener(ml);
            mi.setActionCommand("rename");
            pm.add(mi);
            mi = new JMenuItem("Duplicate");
            mi.addActionListener(ml);
            mi.setActionCommand("duplicate");
            pm.add(mi);
            mi = new JMenuItem("Delete");
            mi.addActionListener(ml);
            mi.setActionCommand("delete");
            pm.add(mi);
        }
        return pm;
    }

    private void setRosterGroups(DefaultMutableTreeNode root) {
        root.removeAllChildren();
        root.add(new DefaultMutableTreeNode(Roster.ALLENTRIES));
        for (String g : Roster.instance().getRosterGroupList()) {
            root.add(new DefaultMutableTreeNode(g));
        }
    }

    // This class is required only for Java 1.5 support.
    class DropTargetListener implements java.awt.dnd.DropTargetListener {

        public void dragEnter(DropTargetDragEvent dtde) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        public void dragOver(DropTargetDragEvent dtde) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        public void dragExit(DropTargetEvent dte) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        public void drop(DropTargetDropEvent dtde) {
            TransferHandler th = new TransferHandler();
            if (dtde.isLocalTransfer() && th.canImport(_tree, dtde.getCurrentDataFlavors())) {
                Point l = dtde.getLocation();
                int closestRow = _tree.getClosestRowForLocation(l.x, l.y);
                Rectangle closestRowBounds = _tree.getRowBounds(closestRow);
                if (l.getY() >= closestRowBounds.getY()
                        && l.getY() < closestRowBounds.getY() + closestRowBounds.getHeight()) {
                    th.importData(_tree, dtde.getTransferable(), _tree.getPathForRow(closestRow));
                } else {
                    th.importData(_tree, dtde.getTransferable(), null);
                }
            }
        }

    }

    class MenuActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            TreePath g = new TreePath(_model.getPathToRoot(_groups));
            JmriAbstractAction a;
            if (g.isDescendant(_tree.getSelectionPath())) {
                if (e.getActionCommand().equals("export")) {
                    if (getUsesActiveRosterGroup()) {
                        a = new FullBackupExportAction("Export...", scrollPane.getTopLevelAncestor());
                        a.actionPerformed(e);
                    }
                } else if (e.getActionCommand().equals("rename")) {
                    a = new RenameRosterGroupAction("Rename", scrollPane.getTopLevelAncestor());
                    a.setParameter("group", _tree.getSelectionPath().getLastPathComponent().toString());
                    a.actionPerformed(e);
                } else if (e.getActionCommand().equals("duplicate")) {
                    a = new CopyRosterGroupAction("Duplicate", scrollPane.getTopLevelAncestor());
                    a.setParameter("group", _tree.getSelectionPath().getLastPathComponent().toString());
                    a.actionPerformed(e);
                } else if (e.getActionCommand().equals("delete")) {
                    a = new DeleteRosterGroupAction("Delete", scrollPane.getTopLevelAncestor());
                    a.setParameter("group", _tree.getSelectionPath().getLastPathComponent().toString());
                    a.actionPerformed(e);
                } else if (e.getActionCommand().equals("newWindow") && newWindowMenuItemAction != null) {
                    newWindowMenuItemAction.setParameter("group", _tree.getSelectionPath().getLastPathComponent().toString());
                    newWindowMenuItemAction.actionPerformed(e);
                } else {
                    JOptionPane.showMessageDialog((JComponent) e.getSource(), "Not Implemented", "Not Implemented", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    class MouseAdapter extends java.awt.event.MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                JTree t = (JTree) e.getSource();
                int closestRow = t.getClosestRowForLocation(e.getX(), e.getY());
                Rectangle closestRowBounds = t.getRowBounds(closestRow);
                if (e.getY() >= closestRowBounds.getY()
                        && e.getY() < closestRowBounds.getY() + closestRowBounds.getHeight()) {
                    // test the click is after start of row renderer
                    //if (e.getX() > closestRowBounds.getX()
                    //        && closestRow < t.getRowCount()) {
                    t.setSelectionRow(closestRow);
                    // setting selection to -1 removes the selection
                    //} else {
                    //    t.setSelectionRow(-1);
                }
            } else if (e.isPopupTrigger()) {
                showMenu(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showMenu(e);
            }
        }

        public void showMenu(MouseEvent e) {
            JTree t = (JTree) e.getSource();
            int closestRow = t.getClosestRowForLocation(e.getX(), e.getY());
            Rectangle closestRowBounds = t.getRowBounds(closestRow);
            if (e.getY() >= closestRowBounds.getY()
                    && e.getY() < closestRowBounds.getY() + closestRowBounds.getHeight()) {
                t.setSelectionRow(closestRow);
                TreePath g = new TreePath(_model.getPathToRoot(_groups));
                if (t.getSelectionPath().getLastPathComponent().toString().equals(Roster.ALLENTRIES)) {
                    allEntriesMenu.show((JComponent) e.getSource(), e.getX(), e.getY());
                } else if (g.isDescendant(t.getSelectionPath()) && !t.getSelectionPath().isDescendant(g)) {
                    groupsMenu.show((JComponent) e.getSource(), e.getX(), e.getY());
                }
            }
        }
    }

    class PropertyChangeListener implements java.beans.PropertyChangeListener {

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if ((e.getPropertyName().equals("RosterGroupRemoved"))
                    || (e.getPropertyName().equals("RosterGroupAdded"))
                    || (e.getPropertyName().equals("RosterGroupRenamed"))) {
                setRosterGroups(_groups);
                _model.reload(_groups);
                setSelectionToGroup(selectedRosterGroup);
            } else if (e.getPropertyName().equals("ActiveRosterGroup") && _usesActiveRosterGroup) {
                setSelectionToGroup(Roster.getRosterGroupName());
            }
        }
    }

    class TransferHandler extends javax.swing.TransferHandler {

        @Override
        public boolean canImport(JComponent c, DataFlavor[] transferFlavors) {
            for (DataFlavor flavor : transferFlavors) {
                if (RosterEntrySelection.rosterEntryFlavor.equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean importData(JComponent c, Transferable t) {
            if (canImport(c, t.getTransferDataFlavors())) {
                // getDropLocation is null unless dropping on an existing path
                return importData(c, t, ((JTree) c).getDropLocation().getPath());
            }
            return false;
        }

        public boolean importData(JComponent c, Transferable t, TreePath p) {
            JTree l = (JTree) c;
            if (p != null) {
                TreePath g = new TreePath(_model.getPathToRoot(_groups));
                // drag onto existing user defined group, but not onto current selection
                if (g.isDescendant(p) && !p.isDescendant(g) && !p.isDescendant(l.getSelectionPath())) {
                    try {
                        ArrayList<RosterEntry> REs = RosterEntrySelection.getRosterEntries(t);
                        for (RosterEntry re : REs) {
                            re.putAttribute(Roster.getRosterGroupProperty(p.getLastPathComponent().toString()), "yes");
                            re.updateFile();
                        }
                        Roster.writeRosterFile();
                        Roster.instance().rosterGroupEntryChanged();
                    } catch (Exception e) {
                        log.warn("Exception dragging RosterEntries onto RosterGroups: " + e);
                    }
                }
            } else {
                try {
                    JmriAbstractAction a = new CreateRosterGroupAction("Create From Selection", scrollPane.getTopLevelAncestor());
                    a.setParameter("RosterEntries", RosterEntrySelection.getRosterEntries(t));
                    a.actionPerformed(null);
                } catch (Exception e) {
                    log.warn("Exception creating RosterGroups from selection: " + e);
                }
            }
            return false;
        }
    }

    static public class TreeCellRenderer extends DefaultTreeCellRenderer {
    }

    public class TreeSelectionListener implements javax.swing.event.TreeSelectionListener {

        public void valueChanged(TreeSelectionEvent e) {
            TreePath g = new TreePath(_model.getPathToRoot(_groups));
            String oldGroup = selectedRosterGroup;
            if (e.getNewLeadSelectionPath() == null) {
                // if there are no Roster Groups set selection to "All Entries"
                if (Roster.instance().getRosterGroupList().isEmpty()) {
                    _tree.setSelectionPath(new TreePath(_model.getPathToRoot(_groups.getFirstChild())));
                }
            } else if (e.getNewLeadSelectionPath().isDescendant(g)) {
                // reject user attempts to select the "Roster Groups" header
                _tree.setSelectionPath(e.getOldLeadSelectionPath());
            } else if (g.isDescendant(e.getNewLeadSelectionPath())) {
                // set Active Roster Group if selection is under "Roster Groups"
                if (_usesActiveRosterGroup) {
                    Roster.instance().setRosterGroup(_tree.getSelectionPath().getLastPathComponent().toString());
                }
                selectedRosterGroup = _tree.getSelectionPath().getLastPathComponent().toString();
            } else {
                selectedRosterGroup = null;
            }
            firePropertyChange(ROSTER_GROUP_SELECTED_EVENT, oldGroup, selectedRosterGroup);
        }
    }

    public class TreeWillExpandListener implements javax.swing.event.TreeWillExpandListener {

        public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
            log.debug(_tree.getSelectionRows());
        }

        public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
            if (e.getPath().getLastPathComponent().toString().equals("Roster Groups")) {
                throw new ExpandVetoException(e);
            }
        }
    }

    static public class TreeUI extends BasicTreeUI {

        @Override
        public void paint(Graphics g, JComponent c) {
            // TODO use c.getVisibleRect to trim painting to minimum rectangle.
            // paint the background for the tree.
            g.setColor(UIManager.getColor("Tree.textBackground"));
            g.fillRect(0, 0, c.getWidth(), c.getHeight());

            // TODO use c.getVisibleRect to trim painting to minimum rectangle.
            // paint the background for the selected entry, if there is one.
            int selectedRow = getSelectionModel().getLeadSelectionRow();
            if (selectedRow >= 0 && tree.isVisible(tree.getPathForRow(selectedRow))) {

                Rectangle bounds = tree.getRowBounds(selectedRow);

                Graphics2D selectionBackgroundGraphics = (Graphics2D) g.create();
                selectionBackgroundGraphics.translate(0, bounds.y);
                selectionBackgroundGraphics.setColor(UIManager.getColor("Tree.selectionBackground"));
                selectionBackgroundGraphics.fillRect(0, 0, c.getWidth(), bounds.height);
                selectionBackgroundGraphics.dispose();
            }

            super.paint(g, c);
        }

        @Override
        protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right) {
            // do nothing - don't paint horizontal lines.
        }

        @Override
        protected void paintVerticalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets,
                TreePath path) {
            // do nothing - don't paint vertical lines.
        }
    }
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RosterGroupsPanel.class.getName());
}
