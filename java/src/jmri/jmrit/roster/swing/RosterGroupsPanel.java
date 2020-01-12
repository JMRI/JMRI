package jmri.jmrit.roster.swing;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import jmri.jmrit.roster.FullBackupExportAction;
import jmri.jmrit.roster.FullBackupImportAction;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.util.FileUtil;
import jmri.util.IterableEnumeration;
import jmri.util.datatransfer.RosterEntrySelection;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JPanel that lists Roster Groups
 * <p>
 * This panel contains a fairly self-contained display of Roster Groups that
 * allows roster groups to be fully manipulated through context menus.
 *
 * @author Randall Wood Copyright (C) 2011
 * @see jmri.jmrit.roster.Roster
 */
public class RosterGroupsPanel extends JPanel implements RosterGroupSelector {

    private static int GROUPS_MENU = 1;
    private static int ALL_ENTRIES_MENU = 2;
    private JScrollPane scrollPane;
    private JTree _tree;
    private DefaultTreeModel _model;
    private DefaultMutableTreeNode _root;
    private DefaultMutableTreeNode _groups;
    //private DefaultMutableTreeNode _consists;
    private TreeSelectionListener _TSL;
    private String selectedRosterGroup = "";
    private JPopupMenu groupsMenu;
    private JPopupMenu allEntriesMenu;
    private JmriAbstractAction newWindowMenuItemAction = null;

    /**
     * Create a RosterGroupsPanel with default settings
     */
    public RosterGroupsPanel() {
        this(null);
    }

    /**
     * Create a RosterGroupTreePane with the defaultRosterGroup selected.
     *
     * @param defaultRosterGroup the name of the default selection
     */
    public RosterGroupsPanel(String defaultRosterGroup) {
        this.scrollPane = new JScrollPane(getTree());
        this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setGroupsMenu(defaultMenu(GROUPS_MENU));
        setAllEntriesMenu(defaultMenu(ALL_ENTRIES_MENU));
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(getButtons(), BorderLayout.SOUTH);
        setSelectedRosterGroup(defaultRosterGroup);
    }

    /**
     * Get the selected roster group.
     *
     * @return The selected roster group
     */
    @Override
    public String getSelectedRosterGroup() {
        return selectedRosterGroup;
    }

    /**
     * Set the selected roster group.
     * <p>
     * If the group is <code>null</code>, the selected roster group is set to
     * "All Entries".
     *
     * @param group The name of the group to set the selection to.
     */
    public final void setSelectedRosterGroup(String group) {
        if (group == null ? selectedRosterGroup != null : !group.equals(selectedRosterGroup)) {
            String oldGroup = selectedRosterGroup;
            selectedRosterGroup = group;
            setSelectionToGroup(group);
            firePropertyChange(SELECTED_ROSTER_GROUP, oldGroup, group);
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
     *
     * @param menu The new menu for All Entries.
     */
    public final void setAllEntriesMenu(JPopupMenu menu) {
        this.allEntriesMenu = menu;
    }

    /**
     * Get the context menu for "All Entries"
     *
     * @return The menu for All Entries.
     */
    public JPopupMenu getAllEntriesMenu() {
        return this.allEntriesMenu;
    }

    /**
     * Set an action that the menu item "Open in New Window" will trigger.
     * <p>
     * Set a {@link JmriAbstractAction} that the "Open in New Window" menu item
     * will trigger. <code>null</code> will remove the "Open in New Window" menu
     * item from context menus. The "Open in New Window" menu item will be added
     * or removed from the menu as appropriate.
     * <p>
     * If the action you pass has access to the RosterGroupPanel, it may call
     * RosterGroupPanel.getSelectedRosterGroup to determine which group to open
     * in the new window, otherwise it must accept a String defining the group
     * in JmriAbstractAction.setParameter(String, String).
     *
     * @param action  An action that can work on the current selection
     */
    public void setNewWindowMenuAction(JmriAbstractAction action) {
        if (action != null) {
            if (newWindowMenuItemAction == null) {
                MenuActionListener ml = new MenuActionListener();
                JMenuItem mi = new JMenuItem(ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("MenuOpenInNewWindow"));
                mi.addActionListener(ml);
                mi.setActionCommand("newWindow");
                groupsMenu.insert(mi, 0);
                groupsMenu.insert(new JSeparator(), 1);
                // create the menu item twice because a menu item can only
                // be attached to a single menu
                mi = new JMenuItem(ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("MenuOpenInNewWindow"));
                mi.addActionListener(ml);
                mi.setActionCommand("newWindow");
                allEntriesMenu.insert(mi, 0);
                allEntriesMenu.insert(new JSeparator(), 1);
            }
            newWindowMenuItemAction = action;
        } else if (newWindowMenuItemAction != null) {
            groupsMenu.remove(0);
            groupsMenu.remove(0);
            allEntriesMenu.remove(0);
            allEntriesMenu.remove(0);
            newWindowMenuItemAction = null;
        }
        groupsMenu.revalidate();
        allEntriesMenu.revalidate();
    }

    /**
     * The action triggered by the "Open in New Window" menu item.
     *
     * @return A JmriAbstractAction or null
     */
    public JmriAbstractAction getNewWindowMenuAction() {
        return newWindowMenuItemAction;
    }

    @SuppressWarnings("unchecked")
    private void setSelectionToGroup(String group) {
        _tree.removeTreeSelectionListener(_TSL);
        if (group == null || group.equals(Roster.ALLENTRIES) || group.equals("")) {
            _tree.setSelectionPath(new TreePath(_model.getPathToRoot(_groups.getFirstChild())));
        } else {
            for (TreeNode n : new IterableEnumeration<TreeNode>(_groups.children())) {
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
        final JToggleButton addGroupBtn = new JToggleButton(new ImageIcon(FileUtil.findURL("resources/icons/misc/gui3/Add.png")), false);
        final JToggleButton actGroupBtn = new JToggleButton(new ImageIcon(FileUtil.findURL("resources/icons/misc/gui3/Action.png")), false);
        addGroupBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new CreateRosterGroupAction("", scrollPane.getTopLevelAncestor()).actionPerformed(e);
                addGroupBtn.setSelected(false);
            }
        });
        actGroupBtn.addItemListener(new ItemListener() {

            @Override
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

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
                // do nothing
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
                actGroupBtn.setSelected(false);
            }

            @Override
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
     * Get a JScrollPane containing the JTree that does not display horizontal
     * scrollbars.
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
            _tree.setDropMode(DropMode.ON);
            _tree.setTransferHandler(new TransferHandler());
            _tree.addMouseListener(new MouseAdapter());
            setSelectionToGroup(selectedRosterGroup);
            Roster.getDefault().addPropertyChangeListener(new PropertyChangeListener());
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
            _groups = new DefaultMutableTreeNode(Bundle.getMessage("MenuRosterGroups")); // "Roster Groups"
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
        JMenuItem mi = new JMenuItem(Bundle.getMessage("Exportddd"));
        mi.addActionListener(ml);
        mi.setActionCommand("export");
        pm.add(mi);
        mi = new JMenuItem(Bundle.getMessage("Importddd"));
        mi.addActionListener(ml);
        mi.setActionCommand("import");
        pm.add(mi);
        if (menu == GROUPS_MENU) {
            pm.addSeparator();
            mi = new JMenuItem(Bundle.getMessage("Renameddd")); // key is in jmri.NamedBeanBundle
            mi.addActionListener(ml);
            mi.setActionCommand("rename");
            pm.add(mi);
            mi = new JMenuItem(Bundle.getMessage("Duplicateddd"));
            mi.addActionListener(ml);
            mi.setActionCommand("duplicate");
            pm.add(mi);
            mi = new JMenuItem(Bundle.getMessage("ButtonDelete"));
            mi.addActionListener(ml);
            mi.setActionCommand("delete");
            pm.add(mi);
        }
        return pm;
    }

    private void setRosterGroups(DefaultMutableTreeNode root) {
        root.removeAllChildren();
        root.add(new DefaultMutableTreeNode(Roster.ALLENTRIES));
        for (String g : Roster.getDefault().getRosterGroupList()) {
            root.add(new DefaultMutableTreeNode(g));
        }
    }

    // allow private classes to fire property change events as the RGP
    protected void firePropertyChangeAsRGP(String propertyName, Object oldValue, Object newValue) {
        if (propertyName.equals(SELECTED_ROSTER_GROUP)) {
            selectedRosterGroup = (String) newValue;
        }
        this.firePropertyChange(propertyName, oldValue, newValue);
    }

    class MenuActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath g = new TreePath(_model.getPathToRoot(_groups));
            WindowInterface wi = (WindowInterface) scrollPane.getTopLevelAncestor();
            if (g.isDescendant(_tree.getSelectionPath())) {
                if (e.getActionCommand().equals("export")) {
                    new FullBackupExportAction(ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("Exportddd"), wi).actionPerformed(e);
                } else if (e.getActionCommand().equals("import")) {
                    new FullBackupImportAction(ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("Importddd"), wi).actionPerformed(e);
                } else if (e.getActionCommand().equals("rename")) {
                    new RenameRosterGroupAction("Rename", wi).actionPerformed(e);
                } else if (e.getActionCommand().equals("duplicate")) {
                    new CopyRosterGroupAction("Duplicate", wi).actionPerformed(e);
                } else if (e.getActionCommand().equals("delete")) {
                    new DeleteRosterGroupAction("Delete", wi).actionPerformed(e);
                } else if (e.getActionCommand().equals("newWindow") && newWindowMenuItemAction != null) {
                    newWindowMenuItemAction.actionPerformed(e);
                } else {
                    JOptionPane.showMessageDialog((JComponent) e.getSource(),
                            ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("NotImplemented"),
                            ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("NotImplemented"),
                            JOptionPane.ERROR_MESSAGE);
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

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            //log.debug(e.getPropertyName()); // seems a bit too much to keep active!
            if ((e.getPropertyName().equals("RosterGroupRemoved"))
                    || (e.getPropertyName().equals("RosterGroupAdded"))
                    || (e.getPropertyName().equals("RosterGroupRenamed"))) {
                setRosterGroups(_groups);
                _model.reload(_groups);
                setSelectionToGroup(selectedRosterGroup);
                log.debug("Refreshed Roster Groups pane"); // test for panel redraw after duplication
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
                        Roster.getDefault().writeRoster();
                        setSelectedRosterGroup(p.getLastPathComponent().toString());
                    } catch (java.awt.datatransfer.UnsupportedFlavorException | java.io.IOException | RuntimeException e) {
                        log.warn("Exception dragging RosterEntries onto RosterGroups: " + e);
                    }
                }
            } else {
                try {
                    JmriAbstractAction a = new CreateRosterGroupAction("Create From Selection", scrollPane.getTopLevelAncestor());
                    a.setParameter("RosterEntries", RosterEntrySelection.getRosterEntries(t));
                    a.actionPerformed(null);
                } catch (java.awt.datatransfer.UnsupportedFlavorException | java.io.IOException | RuntimeException e) {
                    log.warn("Exception creating RosterGroups from selection: " + e);
                }
            }
            return false;
        }
    }

    static public class TreeCellRenderer extends DefaultTreeCellRenderer {

    }

    public class TreeSelectionListener implements javax.swing.event.TreeSelectionListener {

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            TreePath g = new TreePath(_model.getPathToRoot(_groups));
            String oldGroup = selectedRosterGroup;
            if (e.getNewLeadSelectionPath() == null) {
                // if there are no Roster Groups set selection to "All Entries"
                if (Roster.getDefault().getRosterGroupList().isEmpty()) {
                    _tree.setSelectionPath(new TreePath(_model.getPathToRoot(_groups.getFirstChild())));
                }
            } else if (e.getNewLeadSelectionPath().isDescendant(g)) {
                // reject user attempts to select the "Roster Groups" header
                _tree.setSelectionPath(e.getOldLeadSelectionPath());
            } else if (g.isDescendant(e.getNewLeadSelectionPath())) {
                selectedRosterGroup = _tree.getSelectionPath().getLastPathComponent().toString();
                if (Roster.ALLENTRIES.equals(selectedRosterGroup)) {
                    selectedRosterGroup = null;
                }
            } else {
                selectedRosterGroup = null;
            }
            firePropertyChangeAsRGP(SELECTED_ROSTER_GROUP, oldGroup, selectedRosterGroup);
        }
    }

    public class TreeWillExpandListener implements javax.swing.event.TreeWillExpandListener {

        @Override
        public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
            log.debug("Selected rows ", _tree.getSelectionRows());
        }

        @Override
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
    private final static Logger log = LoggerFactory.getLogger(RosterGroupsPanel.class);
}
