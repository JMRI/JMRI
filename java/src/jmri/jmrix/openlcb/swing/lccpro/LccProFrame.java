package jmri.jmrix.openlcb.swing.lccpro;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.Transferable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.UserPreferencesManager;

import jmri.swing.ConnectionLabel;
import jmri.swing.JTablePersistenceManager;
import jmri.swing.RowSorterUtil;

import jmri.jmrix.ActiveSystemsMenu;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.openlcb.OlcbNodeGroupStore;
import jmri.jmrix.openlcb.swing.TrafficStatusLabel;

import jmri.util.*;
import jmri.util.datatransfer.RosterEntrySelection;
import jmri.util.swing.*;
import jmri.util.swing.multipane.TwoPaneTBWindow;

import org.openlcb.*;

/**
 * A window for LCC Network management.
 *
 * @author Bob Jacobsen Copyright (C) 2024
 */
public class LccProFrame extends TwoPaneTBWindow  {

    static final ArrayList<LccProFrame> frameInstances = new ArrayList<>();
    protected boolean allowQuit = true;
    protected JmriAbstractAction newWindowAction;

    CanSystemConnectionMemo memo;
    MimicNodeStore nodestore;
    OlcbNodeGroupStore groupStore;

    public LccProFrame(String name) {
        this(name,
            jmri.InstanceManager.getNullableDefault(jmri.jmrix.can.CanSystemConnectionMemo.class));
    }

    public LccProFrame(String name, CanSystemConnectionMemo memo) {
        this(name,
                "xml/config/parts/apps/gui3/lccpro/LccProFrameMenu.xml",
                "xml/config/parts/apps/gui3/lccpro/LccProFrameToolBar.xml",
                memo);
    }

    public LccProFrame(String name, String menubarFile, String toolbarFile) {
        this(name, menubarFile, toolbarFile, jmri.InstanceManager.getNullableDefault(jmri.jmrix.can.CanSystemConnectionMemo.class));
    }

    public LccProFrame(String name, String menubarFile, String toolbarFile, CanSystemConnectionMemo memo) {
        super(name, menubarFile, toolbarFile);
        this.memo = memo;
        if (memo == null) {
            // a functional LccFrame can't be created without an LCC ConnectionConfig
            javax.swing.JOptionPane.showMessageDialog(this, "LccPro requires a configured LCC or OpenLCB connection, will quit now",
               "LccPro", JOptionPane.ERROR_MESSAGE);
            // and close the program
            // This is justified because this should never happen in a properly
            // built application:  The existence of an LCC/OpenLCB connection
            // should have been checked long before reaching this point.
            InstanceManager.getDefault(jmri.ShutDownManager.class).shutdown();
            return;
        }
        this.nodestore = memo.get(MimicNodeStore.class);
        this.groupStore = InstanceManager.getDefault(OlcbNodeGroupStore.class);
        this.allowInFrameServlet = false;
        prefsMgr = InstanceManager.getDefault(UserPreferencesManager.class);
        this.setTitle(name);
        this.buildWindow();
    }

    final NodeInfoPane nodeInfoPane = new NodeInfoPane();
    final NodePipPane nodePipPane = new NodePipPane();
    JLabel firstHelpLabel;
    int groupSplitPaneLocation = 0;
    boolean hideGroups = false;
    final JTextPane id = new JTextPane();
    UserPreferencesManager prefsMgr;
    final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("apps.AppsBundle");
    // the three parts of the bottom half
    final JPanel bottomPanel = new JPanel();
    JSplitPane bottomLCPanel;  // left and center parts
    JSplitPane bottomRPanel;  // right part
    // main center window (TODO: rename this; TODO: Does this still need to be split?)
    JSplitPane rosterGroupSplitPane;
    
    LccProTable nodetable;   // node table in center of screen   

    JComboBox<String> matchGroupName;   // required group name to display; index <= 0 is all

    final JLabel statusField = new JLabel();
    final static Dimension summaryPaneDim = new Dimension(0, 170);

    protected void additionsToToolBar() {
        getToolBar().add(Box.createHorizontalGlue());
    }

    /**
     * For use when the DP3 window is called from another JMRI instance, set
     * this to prevent the DP3 from shutting down JMRI when the window is
     * closed.
     *
     * @param quitAllowed true if closing window should quit application; false
     *                    otherwise
     */
    protected void allowQuit(boolean quitAllowed) {
        if (allowQuit != quitAllowed) {
            newWindowAction = null;
            allowQuit = quitAllowed;
        }

        firePropertyChange("quit", "setEnabled", allowQuit);
        //if we are not allowing quit, ie opened from JMRI classic
        //then we must at least allow the window to be closed
        if (!allowQuit) {
            firePropertyChange("closewindow", "setEnabled", true);
        }
    }
    
    // Create right side of the bottom panel

    JPanel bottomRight() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(SwingConstants.LEFT);

        var searchPanel = new JPanel();
        searchPanel.setLayout(new WrapLayout());
        searchPanel.add(new JLabel("Search Node Names:"));
        var searchField = new JTextField(12) {
            @Override
            public Dimension getMaximumSize() {
                Dimension size = super.getMaximumSize();
                size.height = getPreferredSize().height;
                return size;
            } 
        };
        searchField.getDocument().putProperty("filterNewlines", Boolean.TRUE);
        searchField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
           }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                // on release so the searchField has been updated
                log.debug("keyTyped {} content {}", keyEvent.getKeyCode(), searchField.getText());
                String search = searchField.getText().toLowerCase();
                // start search process
                int count = nodetable.getModel().getRowCount();
                for (int row = 0; row < count; row++) {
                    String value = ((String)nodetable.getTable().getValueAt(row, LccProTableModel.NAMECOL)).toLowerCase();
                    if (value.startsWith(search)) {
                        log.trace("  Hit value {} on {}", value, row);
                        nodetable.getTable().setRowSelectionInterval(row, row);
                        nodetable.getTable().scrollRectToVisible(nodetable.getTable().getCellRect(row,LccProTableModel.NAMECOL, true)); 
                        return;
                    }
                }
                // here we didn't find anything
                nodetable.getTable().clearSelection();
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }
        });
        searchPanel.add(searchField);
        panel.add(searchPanel);
        
        
        var groupPanel = new JPanel();
        groupPanel.setLayout(new WrapLayout());
        JLabel display = new JLabel("Display Node Groups:");
        display.setToolTipText("Use the popup menu on a node's row to define node groups");
        groupPanel.add(display);
        
        matchGroupName = new JComboBox<>();
        updateMatchGroupName();     // before adding listener
        matchGroupName.addActionListener((ActionEvent e) -> {
            filter();
        });
        groupStore.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            updateMatchGroupName();
        });
        groupPanel.add(matchGroupName);
        panel.add(groupPanel);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }

    // load updateMatchGroup combobox with current contents
    protected void updateMatchGroupName() {
        matchGroupName.removeAllItems();
        matchGroupName.addItem("(All Groups)");
        
        var list = groupStore.getGroupNames();
        for (String group : list) {
            matchGroupName.addItem(group);
        }        
    }

    protected final void buildWindow() {
        //Additions to the toolbar need to be added first otherwise when trying to hide bits up during the initialisation they remain on screen
        additionsToToolBar();
        frameInstances.add(this);
        getTop().add(createTop());
        getBottom().setMinimumSize(summaryPaneDim);
        getBottom().add(createBottom());
        statusBar();
        systemsMenu();
        helpMenu(getMenu(), this);

        if (prefsMgr.getSimplePreferenceState(this.getClass().getName() + ".hideSummary")) {
            //We have to set it to display first, then we can hide it.
            hideBottomPane(false);
            hideBottomPane(true);
        }
        PropertyChangeListener propertyChangeListener = (PropertyChangeEvent changeEvent) -> {
            JSplitPane sourceSplitPane = (JSplitPane) changeEvent.getSource();
            String propertyName = changeEvent.getPropertyName();
            if (propertyName.equals(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY)) {
                int current = sourceSplitPane.getDividerLocation() + sourceSplitPane.getDividerSize();
                int panesize = (int) (sourceSplitPane.getSize().getHeight());
                hideBottomPane = panesize - current <= 1;
                //p.setSimplePreferenceState(DecoderPro3Window.class.getName()+".hideSummary",hideSummary);
            }
        };

        getSplitPane().addPropertyChangeListener(propertyChangeListener);
        if (frameInstances.size() > 1) {
            firePropertyChange("closewindow", "setEnabled", true);
            allowQuit(frameInstances.get(0).isAllowQuit());
        } else {
            firePropertyChange("closewindow", "setEnabled", false);
        }
    }

    //@TODO The disabling of the closeWindow menu item doesn't quite work as this in only invoked on the closing window, and not the one that is left
    void closeWindow(WindowEvent e) {
        saveWindowDetails();
        if (allowQuit && frameInstances.size() == 1 && !InstanceManager.getDefault(ShutDownManager.class).isShuttingDown()) {
            handleQuit(e);
        } else {
            //As we are not the last window open or we are not allowed to quit the application then we will just close the current window
            frameInstances.remove(this);
            super.windowClosing(e);
            if ((frameInstances.size() == 1) && (allowQuit)) {
                frameInstances.get(0).firePropertyChange("closewindow", "setEnabled", false);
            }
            dispose();
        }
    }

    JComponent createBottom() {
        JPanel leftPanel = nodeInfoPane;
        JPanel centerPanel = nodePipPane;
        JPanel rightPanel = bottomRight();
                
        bottomLCPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, centerPanel);
        bottomRPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bottomLCPanel, rightPanel);

        leftPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        centerPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        bottomLCPanel.setBorder(null);
        
        bottomLCPanel.setResizeWeight(0.67);  // determined empirically
        bottomRPanel.setResizeWeight(0.75);
        
        bottomLCPanel.setOneTouchExpandable(true);
        bottomRPanel.setOneTouchExpandable(true);
        
        // load split locations from preferences
        Object w = prefsMgr.getProperty(getWindowFrameRef(), "bottomLCPanelDividerLocation");
        if (w != null) {
            var splitPaneLocation = (Integer) w;
            bottomLCPanel.setDividerLocation(splitPaneLocation);
        }
        w = prefsMgr.getProperty(getWindowFrameRef(), "bottomRPanelDividerLocation");
        if (w != null) {
            var splitPaneLocation = (Integer) w;
            bottomRPanel.setDividerLocation(splitPaneLocation);
        }

        // add listeners that will store location preferences
        bottomLCPanel.addPropertyChangeListener((PropertyChangeEvent changeEvent) -> {
            String propertyName = changeEvent.getPropertyName();
            if (propertyName.equals("dividerLocation")) {
                prefsMgr.setProperty(getWindowFrameRef(), "bottomLCPanelDividerLocation", bottomLCPanel.getDividerLocation());
            }
        });
        bottomRPanel.addPropertyChangeListener((PropertyChangeEvent changeEvent) -> {
            String propertyName = changeEvent.getPropertyName();
            if (propertyName.equals("dividerLocation")) {
                prefsMgr.setProperty(getWindowFrameRef(), "bottomRPanelDividerLocation", bottomRPanel.getDividerLocation());
            }
        });
        return bottomRPanel;
    }

    JComponent createTop() {
        final JPanel rosters = new JPanel();
        rosters.setLayout(new BorderLayout());
        // set up node table
        nodetable = new LccProTable(memo);
        rosters.add(nodetable, BorderLayout.CENTER);
         // add selection listener to display selected row
        nodetable.getTable().getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            JTable table = nodetable.getTable();
            if (!e.getValueIsAdjusting()) {       
                if (table.getSelectedRow() >= 0) {
                    int row = table.convertRowIndexToModel(table.getSelectedRow());
                    log.debug("Selected: {}", row);
                    MimicNodeStore.NodeMemo nodememo = nodestore.getNodeMemos().toArray(new MimicNodeStore.NodeMemo[0])[row];
                    log.trace("   node: {}", nodememo.getNodeID().toString());
                    nodeInfoPane.update(nodememo);
                    nodePipPane.update(nodememo);
                }      
            }
        });
 
        // Set all the sort and width details of the table first.
        String nodetableref = getWindowFrameRef() + ":nodes";
        nodetable.getTable().setName(nodetableref);

        // Allow only one column to be sorted at a time -
        // Java allows multiple column sorting, but to effectively persist that, we
        // need to be intelligent about which columns can be meaningfully sorted
        // with other columns; this bypasses the problem by only allowing the
        // last column sorted to affect sorting
        RowSorterUtil.addSingleSortableColumnListener(nodetable.getTable().getRowSorter());

        // Reset and then persist the table's ui state
        JTablePersistenceManager tpm = InstanceManager.getNullableDefault(JTablePersistenceManager.class);
        if (tpm != null) {
            tpm.resetState(nodetable.getTable());
            tpm.persist(nodetable.getTable());
        }
        nodetable.getTable().setDragEnabled(true);
        nodetable.getTable().setTransferHandler(new TransferHandler() {

            @Override
            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY;
            }

            @Override
            public Transferable createTransferable(JComponent c) {
                JTable table = nodetable.getTable();
                ArrayList<String> Ids = new ArrayList<>(table.getSelectedRowCount());
                for (int i = 0; i < table.getSelectedRowCount(); i++) {
                    // TODO replace this with something about the nodes to be dragged and dropped
                    // Ids.add(nodetable.getModel().getValueAt(table.getRowSorter().convertRowIndexToModel(table.getSelectedRows()[i]), RostenodetableModel.IDCOL).toString());
                }
                return new RosterEntrySelection(Ids);
            }

            @Override
            public void exportDone(JComponent c, Transferable t, int action) {
                // nothing to do
            }
        });
        nodetable.getTable().addMouseListener(JmriMouseListener.adapt(new NodePopupListener()));

        // assemble roster/groups splitpane
        // TODO - figure out what to do with the left side of this and expand the following
        JPanel leftSide = new JPanel();
        leftSide.setEnabled(false);
        leftSide.setVisible(false);
        
        rosterGroupSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSide, rosters);
        rosterGroupSplitPane.setOneTouchExpandable(false); // TODO set this true once the leftSide is in use
        rosterGroupSplitPane.setResizeWeight(0); // emphasize right side (nodes)
        
        Object w = prefsMgr.getProperty(getWindowFrameRef(), "rosterGroupPaneDividerLocation");
        if (w != null) {
            groupSplitPaneLocation = (Integer) w;
            rosterGroupSplitPane.setDividerLocation(groupSplitPaneLocation);
        }
        
        log.trace("createTop returns {}", rosterGroupSplitPane);
        return rosterGroupSplitPane;
    }

    /**
     * Set up filtering of displayed rows by group level
     */
    private void filter() {
        RowFilter<LccProTableModel, Integer> rf = new RowFilter<LccProTableModel, Integer>() {
            /**
             * @return true if row is to be displayed
             */
            @Override
            public boolean include(RowFilter.Entry<? extends LccProTableModel, ? extends Integer> entry) {

                // check for group match
                if ( matchGroupName.getSelectedIndex() > 0) {  // -1 is empty combobox
                    String group = matchGroupName.getSelectedItem().toString();
                    NodeID node = new NodeID((String)entry.getValue(LccProTableModel.IDCOL));
                    if ( ! groupStore.isNodeInGroup(node, group)) {
                            return false;
                    }
                }
                
                // passed all filters
                return true;
            }
        };
        nodetable.sorter.setRowFilter(rf);
    }

    /*=============== Getters and Setters for core properties ===============*/

    /**
     * @return Will closing the window quit JMRI?
     */
    public boolean isAllowQuit() {
        return allowQuit;
    }

    /**
     * @param allowQuit Set state to either close JMRI or just the roster window
     */
    public void setAllowQuit(boolean allowQuit) {
        allowQuit(allowQuit);
    }

    /**
     * @return the newWindowAction
     */
    protected JmriAbstractAction getNewWindowAction() {
        if (newWindowAction == null) {
            newWindowAction = new LccProFrameAction("newWindow", this, allowQuit);
        }
        return newWindowAction;
    }

    /**
     * @param newWindowAction the newWindowAction to set
     */
    protected void setNewWindowAction(JmriAbstractAction newWindowAction) {
        this.newWindowAction = newWindowAction;
    }

    @Override
    public Object getProperty(String key) {
        // TODO - does this have any equivalent?
        if (key.equalsIgnoreCase("hideSummary")) {
            return hideBottomPane;
        }
        // call parent getProperty method to return any properties defined
        // in the class hierarchy.
        return super.getProperty(key);
    }

    void handleQuit(WindowEvent e) {
        if (e != null && frameInstances.size() == 1) {
            final String rememberWindowClose = this.getClass().getName() + ".closeDP3prompt";
            if (!prefsMgr.getSimplePreferenceState(rememberWindowClose)) {
                JPanel message = new JPanel();
                JLabel question = new JLabel(rb.getString("MessageLongCloseWarning"));
                final JCheckBox remember = new JCheckBox(rb.getString("MessageRememberSetting"));
                remember.setFont(remember.getFont().deriveFont(10.0F));
                message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
                message.add(question);
                message.add(remember);
                int result = JmriJOptionPane.showConfirmDialog(null,
                        message,
                        rb.getString("MessageShortCloseWarning"),
                        JmriJOptionPane.YES_NO_OPTION);
                if (remember.isSelected()) {
                    prefsMgr.setSimplePreferenceState(rememberWindowClose, true);
                }
                if (result == JmriJOptionPane.YES_OPTION) {
                    handleQuit();
                }
            } else {
                handleQuit();
            }
        } else if (frameInstances.size() > 1) {
            final String rememberWindowClose = this.getClass().getName() + ".closeMultipleDP3prompt";
            if (!prefsMgr.getSimplePreferenceState(rememberWindowClose)) {
                JPanel message = new JPanel();
                JLabel question = new JLabel(rb.getString("MessageLongMultipleCloseWarning"));
                final JCheckBox remember = new JCheckBox(rb.getString("MessageRememberSetting"));
                remember.setFont(remember.getFont().deriveFont(10.0F));
                message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
                message.add(question);
                message.add(remember);
                int result = JmriJOptionPane.showConfirmDialog(null,
                        message,
                        rb.getString("MessageShortCloseWarning"),
                        JmriJOptionPane.YES_NO_OPTION);
                if (remember.isSelected()) {
                    prefsMgr.setSimplePreferenceState(rememberWindowClose, true);
                }
                if (result == JmriJOptionPane.YES_OPTION) {
                    handleQuit();
                }
            } else {
                handleQuit();
            }
            //closeWindow(null);
        }
    }

    private void handleQuit(){
        try {
            InstanceManager.getDefault(jmri.ShutDownManager.class).shutdown();
        } catch (Exception e) {
            log.error("Continuing after error in handleQuit", e);
        }
    }

    protected void helpMenu(JMenuBar menuBar, final JFrame frame) {
        // create menu and standard items
        JMenu helpMenu = HelpUtil.makeHelpMenu("package.apps.gui3.lccpro.LccPro", true);
        // use as main help menu
        menuBar.add(helpMenu);
    }

    protected void hideGroups() {
        boolean boo = !hideGroups;
        hideGroupsPane(boo);
    }

    public void hideGroupsPane(boolean hide) {
        if (hideGroups == hide) {
            return;
        }
        hideGroups = hide;
        if (hide) {
            groupSplitPaneLocation = rosterGroupSplitPane.getDividerLocation();
            rosterGroupSplitPane.setDividerLocation(1);
            rosterGroupSplitPane.getLeftComponent().setMinimumSize(new Dimension());
        } else {
            rosterGroupSplitPane.setDividerSize(UIManager.getInt("SplitPane.dividerSize"));
            rosterGroupSplitPane.setOneTouchExpandable(true);
            if (groupSplitPaneLocation >= 2) {
                rosterGroupSplitPane.setDividerLocation(groupSplitPaneLocation);
            } else {
                rosterGroupSplitPane.resetToPreferredSizes();
            }
        }
    }

    protected void hideSummary() {
        boolean boo = !hideBottomPane;
        hideBottomPane(boo);
    }

    protected void newWindow() {
        this.newWindow(this.getNewWindowAction());
    }

    protected void newWindow(JmriAbstractAction action) {
        action.setWindowInterface(this);
        action.actionPerformed(null);
        firePropertyChange("closewindow", "setEnabled", true);
    }

    /**
     * Match the first argument in the array against a locally-known method.
     *
     * @param args Array of arguments, we take with element 0
     */
    @Override
    public void remoteCalls(String[] args) {
        args[0] = args[0].toLowerCase();
        switch (args[0]) {
            case "summarypane":
                hideSummary();
                break;
            case "groupspane":
                hideGroups();
                break;
            case "quit":
                saveWindowDetails();
                handleQuit(new WindowEvent(this, frameInstances.size()));
                break;
            case "closewindow":
                closeWindow(null);
                break;
            case "newwindow":
                newWindow();
                break;
            case "resettablecolumns":
                nodetable.resetColumnWidths();
                break;
            default:
                log.error("method {} not found", args[0]);
                break;
        }
    }

    void saveWindowDetails() {
        if (prefsMgr != null) {  // aborted startup doesn't set prefs manager
            prefsMgr.setSimplePreferenceState(this.getClass().getName() + ".hideSummary", hideBottomPane);
            prefsMgr.setSimplePreferenceState(this.getClass().getName() + ".hideGroups", hideGroups);
            if (rosterGroupSplitPane.getDividerLocation() > 2) {
                prefsMgr.setProperty(getWindowFrameRef(), "rosterGroupPaneDividerLocation", rosterGroupSplitPane.getDividerLocation());
            } else if (groupSplitPaneLocation > 2) {
                prefsMgr.setProperty(getWindowFrameRef(), "rosterGroupPaneDividerLocation", groupSplitPaneLocation);
            }
        }
    }

    protected void showPopup(JmriMouseEvent e) {
        int row = nodetable.getTable().rowAtPoint(e.getPoint());
        if (!nodetable.getTable().isRowSelected(row)) {
            nodetable.getTable().changeSelection(row, 0, false, false);
        }
        JPopupMenu popupMenu = new JPopupMenu();
        
        NodeID node = new NodeID((String) nodetable.getTable().getValueAt(row, LccProTableModel.IDCOL));
        
        var addMenu = new JMenuItem("Add Node To Group");
        addMenu.addActionListener((ActionEvent evt) -> {
            addToGroupPrompt(node);
        });
        popupMenu.add(addMenu);

        var removeMenu = new JMenuItem("Remove Node From Group");
        removeMenu.addActionListener((ActionEvent evt) -> {
            removeFromGroupPrompt(node);
        });
        popupMenu.add(removeMenu);
        
        var restartMenu = new JMenuItem("Restart Node");
        restartMenu.addActionListener((ActionEvent evt) -> {
            restart(node);
        });
        popupMenu.add(restartMenu);
        
        var clearCdiMenu = new JMenuItem("Clear CDI Cache");
        clearCdiMenu.addActionListener((ActionEvent evt) -> {
            clearCDI(node);
        });
        popupMenu.add(clearCdiMenu);
        
       popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    void addToGroupPrompt(NodeID node) {
        var group = JmriJOptionPane.showInputDialog(
                    null, "Add to Group:", "Add to Group", 
                    JmriJOptionPane.QUESTION_MESSAGE
                );
        if (! group.isEmpty()) {
            groupStore.addNodeToGroup(node, group);
        }
        updateMatchGroupName();
    }
    
    void removeFromGroupPrompt(NodeID node) {
        var group = JmriJOptionPane.showInputDialog(
                    null, "Remove from Group:", "Remove from Group", 
                    JmriJOptionPane.QUESTION_MESSAGE
                );
        if (! group.isEmpty()) {
            groupStore.removeNodeFromGroup(node, group);
        }
        updateMatchGroupName();
    }
    
    void restart(NodeID node) {
        memo.get(OlcbInterface.class).getDatagramService()
            .sendData(node, new int[] {0x20, 0xA9});
    }
    
    void clearCDI(NodeID destNodeID) {
        jmri.jmrix.openlcb.swing.DropCdiCache.drop(destNodeID, memo.get(OlcbInterface.class));
    }
    
    /**
     * Create and display a status bar along the bottom edge of the Roster main
     * pane.
     */
    protected void statusBar() {
        for (ConnectionConfig conn : InstanceManager.getDefault(ConnectionConfigManager.class)) {
            if (!conn.getDisabled()) {
                addToStatusBox(new ConnectionLabel(conn));
            }
        }
        addToStatusBox(new TrafficStatusLabel(memo));        
    }

    protected void systemsMenu() {
        ActiveSystemsMenu.addItems(getMenu());
        getMenu().add(new WindowMenu(this));
    }

    void updateDetails() {
        // TODO - once we decide what details to show, fix this
    }

    @Override
    @SuppressFBWarnings(value = "OVERRIDING_METHODS_MUST_INVOKE_SUPER",
            justification = "This calls closeWindow which invokes the super method")
    public void windowClosing(WindowEvent e) {
        closeWindow(e);
    }

    /**
     * Displays a context (right-click) menu for a node row.
     */
    private class NodePopupListener extends JmriMouseAdapter {

        @Override
        public void mousePressed(JmriMouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        @Override
        public void mouseReleased(JmriMouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        @Override
        public void mouseClicked(JmriMouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
                return;
            }
        }
    }

    /**
     * Displays SNIP information about a specific node
     */
    private static class NodeInfoPane extends JPanel {
        JLabel name = new JLabel();
        JLabel desc = new JLabel();
        JLabel nodeID = new JLabel();
        JLabel mfg = new JLabel();
        JLabel model = new JLabel();
        JLabel hardver = new JLabel();
        JLabel softver = new JLabel();
        
        public NodeInfoPane() {
            var gbl = new jmri.util.javaworld.GridLayout2(7,2);
            setLayout(gbl);
            
            var a = new JLabel("Name: ");
            a.setHorizontalAlignment(SwingConstants.RIGHT);
            add(a);
            add(name);

            a = new JLabel("Description: ");
            a.setHorizontalAlignment(SwingConstants.RIGHT);
            add(a);
            add(desc);

            a = new JLabel("Node ID: ");
            a.setHorizontalAlignment(SwingConstants.RIGHT);
            add(a);
            add(nodeID);
            
            a = new JLabel("Manufacturer: ");
            a.setHorizontalAlignment(SwingConstants.RIGHT);
            add(a);
            add(mfg);

            a = new JLabel("Model: ");
            a.setHorizontalAlignment(SwingConstants.RIGHT);
            add(a);
            add(model);

            a = new JLabel("Hardware Version: ");
            a.setHorizontalAlignment(SwingConstants.RIGHT);
            add(a);
            add(hardver);

            a = new JLabel("Software Version: ");
            a.setHorizontalAlignment(SwingConstants.RIGHT);
            add(a);
            add(softver);
        }
        
        public void update(MimicNodeStore.NodeMemo nodememo) {
            var snip = nodememo.getSimpleNodeIdent();
            
            // update with current contents
            name.setText(snip.getUserName());
            desc.setText(snip.getUserDesc());
            nodeID.setText(nodememo.getNodeID().toString());
            mfg.setText(snip.getMfgName());
            model.setText(snip.getModelName());
            hardver.setText(snip.getHardwareVersion());
            softver.setText(snip.getSoftwareVersion());
        }

    }
    

    /**
     * Displays PIP information about a specific node
     */
    private static class NodePipPane extends JPanel {
        
        public NodePipPane () {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(new JLabel("Supported Protocols:"));
        }
        
        public void update(MimicNodeStore.NodeMemo nodememo) {
            // remove existing content
            removeAll();
            revalidate();
            repaint();
            // add heading
            add(new JLabel("Supported Protocols:"));
            // and display new content
            var pip = nodememo.getProtocolIdentification();
            var names = pip.getProtocolNames();
            
            for (String name : names) {
                // make this name a bit more human-friendly
                final String regex = "([a-z])([A-Z])";
                final String replacement = "$1 $2";
                var formattedName = "   "+name.replaceAll(regex, replacement);
                add(new JLabel(formattedName));
            }
        }
    }
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LccProFrame.class);

}
