package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.Dimension;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import jmri.InstanceManager;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusAddress;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.CbusSend;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.CbusConfigPaneProvider;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Master Pane for CBUS node configuration incl. CBUS node table
 * 
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Steve Young (C) 2019
 * @see CbusNodeTableDataModel
 *
 * @since 2.99.2
 */
public class NodeConfigToolPane extends jmri.jmrix.can.swing.CanPanel implements PropertyChangeListener{

    public CbusNodeTableDataModel nodeModel;
    public JTable nodeTable;
    private CbusPreferences preferences;
    
    protected CbusNodeTablePane nodeTablePane;
    private CbusNodeRestoreFcuFrame fcuFrame;
    private CbusNodeEditEventFrame _editEventFrame;
    // private CbusNodeBackupsPane _backupPane;
    
    private JScrollPane eventScroll;
    private JSplitPane split;
    protected JTabbedPane tabbedPane;
    
    private ArrayList<CbusNodeConfigTab> tabbedPanes;
    
    private int _selectedNode;
    private jmri.util.swing.BusyDialog busy_dialog;
    
    public int NODE_SEARCH_TIMEOUT = 5000;
    
    private JMenuItem teachNodeFromFcuFile;
    private JMenuItem searchForNodesMenuItem;
    private JCheckBoxMenuItem nodeNumRequestMenuItem;
    private JRadioButtonMenuItem backgroundDisabled;
    private JRadioButtonMenuItem backgroundSlow;
    private JRadioButtonMenuItem backgroundFast;
    private JCheckBoxMenuItem addCommandStationMenuItem;
    private JCheckBoxMenuItem addNodesMenuItem;
    private JCheckBoxMenuItem startupCommandStationMenuItem;
    private JCheckBoxMenuItem startupNodesMenuItem;
    private JCheckBoxMenuItem startupNodesXmlMenuItem;
    private JRadioButtonMenuItem zeroBackups;
    private JRadioButtonMenuItem fiveBackups;
    private JRadioButtonMenuItem tenBackups;
    private JRadioButtonMenuItem twentyBackups;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        nodeModel=InstanceManager.getNullableDefault(CbusNodeTableDataModel.class);
        if (nodeModel == null) {
            ThreadingUtil.runOnLayout(() -> {
            nodeModel = new CbusNodeTableDataModel(memo, 5, CbusNodeTableDataModel.MAX_COLUMN);
            InstanceManager.store(nodeModel, CbusNodeTableDataModel.class);
            nodeModel.startup();
            });
        }
        
        CbusConfigPaneProvider.loadInstances();

        _selectedNode = -1;

        preferences = jmri.InstanceManager.getDefault(jmri.jmrix.can.cbus.CbusPreferences.class);
        init();
        
    }

    /**
     * Create a new NodeConfigToolPane
     */
    public NodeConfigToolPane() {
        super();
    }

    protected final ArrayList<CbusNodeConfigTab> getTabs() {
        if (tabbedPanes==null) {
            tabbedPanes = new ArrayList<>(6);
            tabbedPanes.add( new CbusNodeInfoPane(this));
            tabbedPanes.add( new CbusNodeUserCommentsPane(this));
            tabbedPanes.add( new CbusNodeEditNVarPane(this));
            tabbedPanes.add( new CbusNodeEventVarPane(this));
            tabbedPanes.add( new CbusNodeSetupPane(this));
            tabbedPanes.add( new CbusNodeBackupsPane(this));
        }
        return new ArrayList<>(this.tabbedPanes);
    }
    
    /**
     * Initialise the NodeConfigToolPane
     */
    public void init() {
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       
        // main pane
        JPanel _pane1 = new JPanel();
        _pane1.setLayout(new BorderLayout());
        
        // basis for future menu-bar if one required
        
       // buttoncontainer.setLayout(new BorderLayout());
       // updatenodesButton = new JButton(("Search for Nodes"));
       // buttoncontainer.add(updatenodesButton);
       // JPanel toppanelcontainer = new JPanel();
       // toppanelcontainer.setLayout(new BoxLayout(toppanelcontainer, BoxLayout.X_AXIS));
       // toppanelcontainer.add(buttoncontainer);
       // pane1.add(toppanelcontainer, BorderLayout.PAGE_START);
        
        // scroller for main table
        nodeTablePane = new CbusNodeTablePane();
        nodeTablePane.initComponents(memo);
        
        nodeTable = nodeTablePane.nodeTable;
        
        eventScroll = new JScrollPane( nodeTablePane );
        
        JPanel mainNodePane = new JPanel();
        
        mainNodePane.setLayout(new BorderLayout());
        mainNodePane.add(eventScroll);
        
        tabbedPane = new JTabbedPane();
        
        tabbedPane.setEnabled(false);
        
        getTabs().forEach((pn) -> {
            tabbedPane.addTab(pn.getTitle(),pn);
        });
        
        Dimension minimumSize = new Dimension(40, 40);
        mainNodePane.setMinimumSize(minimumSize);
        tabbedPane.setMinimumSize(minimumSize);
        
        this.setPreferredSize(new Dimension(700, 450));
        
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainNodePane, tabbedPane);
        split.setDividerLocation(100); // px from top of node table pane
        split.setContinuousLayout(true);
        _pane1.add(split, BorderLayout.CENTER);
        
        add(_pane1);
        _pane1.setVisible(true);
        
        tabbedPane.addChangeListener((ChangeEvent e) -> {
            userViewChanged();
        });
        
        // also add listener to tab action
        nodeTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if ( !e.getValueIsAdjusting() ) {
                userViewChanged();
            }
        });
        
        userViewChanged();
        
        tabbedPane.setTransferHandler(new TransferHandler());
        nodeTable.setTransferHandler(new TransferHandler());
        
        revalidate();
       
    }
    
    private final JFrame topFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
    
    /**
     * Create a non-modal dialogue box with node search results
     * @param csfound number of Command Stations
     * @param ndfound number of nodes
     */
    public void notifyNodeSearchComplete(int csfound, int ndfound){
        busy_dialog.finish();
        busy_dialog=null;
        
        JOptionPane pane = new JOptionPane("<html><h3>Node Responses : " + ndfound + 
            "</h3><p>Of which Command Stations: " + csfound + "</p></html>");
        pane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog(this, "Node Search Complete");
        dialog.setModal(false);
        dialog.setVisible(true);
        searchForNodesMenuItem.setEnabled(true);
    }
    
    /**
     * Notify this pane that the selected node or viewed tab has changed
     */
    protected void userViewChanged(){
        
        int sel = nodeTable.getSelectedRow();
        int rowBefore = nodeTable.getSelectedRow()-1;
        int rowAfter = nodeTable.getSelectedRow()+1;
        if ( sel > -1 ) {
            tabbedPane.setEnabled(true);
            
            
            _selectedNode = (int) nodeTable.getModel().getValueAt(nodeTable.convertRowIndexToModel(sel), CbusNodeTableDataModel.NODE_NUMBER_COLUMN);
            
            int tabindex = tabbedPane.getSelectedIndex();
            
            int nodeBefore = -1;
            int nodeAfter = -1;
            
            if ( rowBefore > -1 ) {
                nodeBefore = (int) nodeTable.getModel().getValueAt(rowBefore, CbusNodeTableDataModel.NODE_NUMBER_COLUMN);
            }
            if ( rowAfter < nodeTable.getRowCount() ) {
                nodeAfter = (int) nodeTable.getModel().getValueAt(rowAfter, CbusNodeTableDataModel.NODE_NUMBER_COLUMN);
            }
            
            log.debug("node {} selected tab index {} , node before {} node after {}", _selectedNode , tabindex, nodeBefore,nodeAfter );                

            boolean veto = false;
            for (CbusNodeConfigTab tab : getTabs()) {
                if ( tab.getActiveDialog() || tab.getVetoBeingChanged()) {
                    veto = true;
                    break; // or return obj
                }
            }
            
            if (veto){
                return;
            }
            
            tabbedPane.setSelectedIndex(tabindex);
            nodeTable.setRowSelectionInterval(sel,sel);
            
            // this also starts urgent fetch loop if not currently looping
            nodeModel.setUrgentFetch(_selectedNode,nodeBefore,nodeAfter);
            
            getTabs().get(tabindex).setNode( nodeModel.getNodeByNodeNum(_selectedNode) );
            
        }
        else {
            tabbedPane.setEnabled(false);
            
        }
    }
    
    /**
     * Set Menu Options eg. which checkboxes etc. should be checked
     */
    private void setMenuOptions(){
        
        nodeNumRequestMenuItem.setSelected( preferences.getAllocateNNListener() );
        backgroundDisabled.setSelected(false);
        backgroundSlow.setSelected(false);
        backgroundFast.setSelected(false);
        
        switch ((int) preferences.getNodeBackgroundFetchDelay()) {
            case 0:
                backgroundDisabled.setSelected(true);
                break;
            case 50:
                backgroundFast.setSelected(true);
                break;
            case 100:
                backgroundSlow.setSelected(true);
                break;
            default:
                break;
        }
        
        addCommandStationMenuItem.setSelected( preferences.getAddCommandStations() );
        addNodesMenuItem.setSelected( preferences.getAddNodes() );
        
        startupCommandStationMenuItem.setSelected( preferences.getStartupSearchForCs() );
        startupNodesMenuItem.setSelected( preferences.getStartupSearchForNodes() );
        startupNodesXmlMenuItem.setSelected( preferences.getSearchForNodesBackupXmlOnStartup() );
        
        zeroBackups.setSelected(false);
        fiveBackups.setSelected(false);
        tenBackups.setSelected(false);
        twentyBackups.setSelected(false);
        
        switch (preferences.getMinimumNumBackupsToKeep()) {
            case 0:
                zeroBackups.setSelected(true);
                break;
            case 5:
                fiveBackups.setSelected(true);
                break;
            case 10:
                tenBackups.setSelected(true);
                break;
            case 20:
                twentyBackups.setSelected(true);
                break;
            default:
                break;
        }
        
    }
    
    /**
     * Creates a Menu List.
     * {@inheritDoc}
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<>();
        
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        
        teachNodeFromFcuFile = new JMenuItem(("Restore Node / Import Data from FCU XML")); //  FCU
               
        fileMenu.add(teachNodeFromFcuFile);
        
        JMenu optionsMenu = new JMenu("Options");
        
        searchForNodesMenuItem = new JMenuItem("Search for Nodes and Command Stations");
        
        JMenuItem sendSysResetMenuItem = new JMenuItem("Send System Reset");
        
        searchForNodesMenuItem.setToolTipText(("Timeout set to " + NODE_SEARCH_TIMEOUT + "ms."));
        
        nodeNumRequestMenuItem = new JCheckBoxMenuItem(("Listen for Node Number Requests"));
        nodeNumRequestMenuItem.setToolTipText("Also adds a check for any node already awaiting a number when performing node searches.");
        
        addCommandStationMenuItem = new JCheckBoxMenuItem(("Add Command Stations when found"));
        addNodesMenuItem = new JCheckBoxMenuItem(("Add Nodes when found"));
        
        JMenu backgroundFetchMenu = new JMenu("Node Info Fetch Speed");
        ButtonGroup backgroundFetchGroup = new ButtonGroup();

        backgroundDisabled = new JRadioButtonMenuItem(Bundle.getMessage("HighlightDisabled"));
        backgroundSlow = new JRadioButtonMenuItem(("Slow"));
        backgroundFast = new JRadioButtonMenuItem(("Fast"));
        
        startupCommandStationMenuItem = new JCheckBoxMenuItem(("Search Command Stations on Startup"));
        startupNodesMenuItem = new JCheckBoxMenuItem(("Search Nodes on Startup"));
        
        startupNodesXmlMenuItem = new JCheckBoxMenuItem(("Add previously seen Nodes on Startup"));
        
        JMenu numBackupsMenu = new JMenu("Min. Auto Backups to retain");
        ButtonGroup minNumBackupsGroup = new ButtonGroup();
        
        zeroBackups = new JRadioButtonMenuItem(("0"));
        fiveBackups = new JRadioButtonMenuItem(("5"));
        tenBackups = new JRadioButtonMenuItem(("10"));
        twentyBackups = new JRadioButtonMenuItem(("20"));
        
        minNumBackupsGroup.add(zeroBackups);
        minNumBackupsGroup.add(fiveBackups);
        minNumBackupsGroup.add(tenBackups);
        minNumBackupsGroup.add(twentyBackups);
        
        numBackupsMenu.add(zeroBackups);
        numBackupsMenu.add(fiveBackups);
        numBackupsMenu.add(tenBackups);
        numBackupsMenu.add(twentyBackups);
        
        backgroundFetchGroup.add(backgroundDisabled);
        backgroundFetchGroup.add(backgroundSlow);
        backgroundFetchGroup.add(backgroundFast);
        
        backgroundFetchMenu.add(backgroundDisabled);
        backgroundFetchMenu.add(backgroundSlow);
        backgroundFetchMenu.add(backgroundFast);
        
        optionsMenu.add( searchForNodesMenuItem );
        optionsMenu.add( new JSeparator() );
        optionsMenu.add( sendSysResetMenuItem );
        optionsMenu.add( new JSeparator() );
        optionsMenu.add( backgroundFetchMenu );
        optionsMenu.add( new JSeparator() );
        optionsMenu.add( nodeNumRequestMenuItem );
        optionsMenu.add( new JSeparator() );
        optionsMenu.add( addCommandStationMenuItem );
        optionsMenu.add( addNodesMenuItem );
        optionsMenu.add( new JSeparator() );
        optionsMenu.add( startupCommandStationMenuItem );
        optionsMenu.add( startupNodesMenuItem );
        optionsMenu.add( startupNodesXmlMenuItem );
        optionsMenu.add( new JSeparator() );
        optionsMenu.add( numBackupsMenu );
        
        menuList.add(fileMenu);
        menuList.add(optionsMenu);
        
        ActionListener teachNodeFcu = ae -> {
            fcuFrame = new CbusNodeRestoreFcuFrame(this);
            fcuFrame.initComponents(memo);
        };
        
        teachNodeFromFcuFile.addActionListener(teachNodeFcu);
        
        // saved preferences go through the cbus table model so they can be actioned immediately
        // they'll be also saved by the table, not here.
        
        ActionListener updatenodes = ae -> {
            searchForNodesMenuItem.setEnabled(false);
            busy_dialog = new jmri.util.swing.BusyDialog(topFrame, "Node Search", false);
            busy_dialog.start();
            nodeModel.startASearchForNodes( this , NODE_SEARCH_TIMEOUT );
        };
        searchForNodesMenuItem.addActionListener(updatenodes);
        
        ActionListener systemReset = ae -> {
            new CbusSend(memo).aRST();
            // flash something to user so they know that something has happened
            busy_dialog = new jmri.util.swing.BusyDialog(topFrame, "System Reset", false);
            busy_dialog.start();
            ThreadingUtil.runOnGUIDelayed( () -> {
                busy_dialog.finish();
                busy_dialog=null;
            },300 );
        };
        sendSysResetMenuItem.addActionListener(systemReset);
        
        ActionListener nodeRequestActive = ae -> {
            nodeModel.setBackgroundAllocateListener( nodeNumRequestMenuItem.isSelected() );
            preferences.setAllocateNNListener( nodeNumRequestMenuItem.isSelected() );
        };
        nodeNumRequestMenuItem.addActionListener(nodeRequestActive);
        
        // values need to match setMenuOptions()
        ActionListener fetchListener = ae -> {
            if ( backgroundDisabled.isSelected() ) {
                preferences.setNodeBackgroundFetchDelay(0L);
                nodeModel.startBackgroundFetch();
            }
            else if ( backgroundSlow.isSelected() ) {
                preferences.setNodeBackgroundFetchDelay(100L);
                nodeModel.startBackgroundFetch();
            }
            else if ( backgroundFast.isSelected() ) {
                preferences.setNodeBackgroundFetchDelay(50L);
                nodeModel.startBackgroundFetch();
            }
        };
        backgroundDisabled.addActionListener(fetchListener);
        backgroundSlow.addActionListener(fetchListener);
        backgroundFast.addActionListener(fetchListener);
        
        ActionListener addCsListener = ae -> {
            preferences.setAddCommandStations( addCommandStationMenuItem.isSelected() );
        };
        addCommandStationMenuItem.addActionListener(addCsListener);
        
        ActionListener addNodeListener = ae -> {
            preferences.setAddNodes( addNodesMenuItem.isSelected() );
        };
        addNodesMenuItem.addActionListener(addNodeListener);
        
        ActionListener addstartupCommandStationMenuItem = ae -> {
            preferences.setStartupSearchForCs( startupCommandStationMenuItem.isSelected() );
        };
        startupCommandStationMenuItem.addActionListener(addstartupCommandStationMenuItem);
        
        ActionListener addstartupNodesMenuItem = ae -> {
            preferences.setStartupSearchForNodes( startupNodesMenuItem.isSelected() );
        };
        startupNodesMenuItem.addActionListener(addstartupNodesMenuItem);
        
        ActionListener addstartupNodesXmlMenuItem = ae -> {
            preferences.setSearchForNodesBackupXmlOnStartup( startupNodesXmlMenuItem.isSelected() );
        };
        startupNodesXmlMenuItem.addActionListener(addstartupNodesXmlMenuItem);
        
         // values need to match setMenuOptions()
        ActionListener minBackupsListener = ae -> {
            if ( zeroBackups.isSelected() ) {
                preferences.setMinimumNumBackupsToKeep(0);
            }
            else if ( fiveBackups.isSelected() ) {
                preferences.setMinimumNumBackupsToKeep(5);
            }
            else if ( tenBackups.isSelected() ) {
                preferences.setMinimumNumBackupsToKeep(10);
            }
            else if ( twentyBackups.isSelected() ) {
                preferences.setMinimumNumBackupsToKeep(10);
            }
        };
        zeroBackups.addActionListener(minBackupsListener);
        fiveBackups.addActionListener(minBackupsListener);
        tenBackups.addActionListener(minBackupsListener);
        twentyBackups.addActionListener(minBackupsListener);
        
        setMenuOptions();
        
        return menuList;
    }
    
    /**
     * Set Restore from FCU Menu Item active as only 1 instance per NodeConfigToolPane allowed
     * @param isActive set true if Frame opened, else false to notify closed
     */
    protected void setRestoreFcuActive( boolean isActive ){
        teachNodeFromFcuFile.setEnabled(!isActive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return prependConnToString(Bundle.getMessage("MenuItemNodeConfig"));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolPane";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        
        if (_toNode!=null){
            _toNode.removePropertyChangeListener(this);
        }
        
      //  nodeTable = null;
      //  eventScroll = null;
        super.dispose();
    }

    /**
     * Handles drag actions containing CBUS events to edit / teach to a node
     */
    private class TransferHandler extends javax.swing.TransferHandler {
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canImport(JComponent c, DataFlavor[] transferFlavors) {

            // prevent draggable on startup when a node has not yet been selected
            // the draggable must still be able to select a table row
            if ( (c instanceof JTabbedPane ) && ( _selectedNode < 1 )){
                return false;
            }
            
            for (DataFlavor flavor : transferFlavors) {
                if (DataFlavor.stringFlavor.equals(flavor)) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean importData(JComponent c, Transferable t) {
            if (canImport(c, t.getTransferDataFlavors())) {
                
                String eventInJmriFormat;
                try {
                    eventInJmriFormat = (String) t.getTransferData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException | IOException e) {
                    log.error("unable to get dragged address {}", e);
                    return false;
                }
                return openNewOrEditEventFrame(eventInJmriFormat);
            }
            return false;
        }
    }
    
    /**
     * Opens a new or edit event frame depending on if existing
     * @param eventInJmriFormat standard CBUS Sensor / Turnout phrase, eg "+44", "-N123E456", "X0A0B"
     * @return false if issue opening or editing
     */
    private boolean openNewOrEditEventFrame( String eventInJmriFormat ){
        
        // do some validation on the input string
        // processed in the same way as a sensor, turnout or light so less chance of breaking in future
        // and can also accept the Hex "X1234;X654876" format
        String validatedAddr;
        try {    
            validatedAddr = CbusAddress.validateSysName( eventInJmriFormat );
        } catch (IllegalArgumentException e) {
            return false;
        }
        
        if (nodeModel==null){
            log.warn("No Node Model");
            return false;
        }
        CbusNode _node = nodeModel.getNodeByNodeNum( _selectedNode );
        if (_node==null){
            log.warn("No Node");
            return false;
        }
        
        CanMessage m = ( new CbusAddress(validatedAddr) ).makeMessage(0x12);
        
        CbusNodeEvent newev = new CbusNodeEvent( memo,
            CbusMessage.getNodeNumber(m), 
            CbusMessage.getEvent(m), 
            _selectedNode, 
            -1, 
            _node.getNodeParamManager().getParameter(5)
            );
        java.util.Arrays.fill(newev.getEvVarArray(),0);
        
        log.debug("dragged nodeevent {} ",newev);
        ThreadingUtil.runOnGUI( () -> {
            getEditEvFrame().initComponents(memo,newev);
        });
        return true;
    }
    
    /**
     * Get the edit event frame
     * this could be requested from
     * CbusNodeEventDataModel button click to edit event,
     * this class when it receives an event via drag n drop,
     * creating new event from CbusNodeEventVarPane
     * @return the Frame
     */
    public CbusNodeEditEventFrame getEditEvFrame(){
        if (_editEventFrame == null ){
            _editEventFrame = new CbusNodeEditEventFrame(this);
        }
        return _editEventFrame;
    }
    
    /**
     * Receive notification from the frame that it has disposed
     */
    protected void clearEditEventFrame() {
        _editEventFrame = null;
    }

    private boolean _clearEvents;
    private boolean _teachEvents;
    private CbusNode _fromNode;
    private CbusNode _toNode;
    private JFrame _frame;
    
    /**
     * Show a Confirm before Save Dialogue Box then start teach process for Node
     * <p>
     * Used in Node Backup restore, Restore from FCU, edit NV's
     * Edit Event variables currently use a custom dialogue, not this
     * @param fromNode Node to get data from
     * @param toNode Node to send changes to
     * @param teachNVs true to Teach NV's
     * @param clearEvents true to clear events before teaching new ones
     * @param teachEvents true to teach events
     * @param frame the frame to which dialogue boxes can be attached to
     */
    protected void showConfirmThenSave( CbusNode fromNode, CbusNode toNode, 
        boolean teachNVs, boolean clearEvents, boolean teachEvents, JFrame frame){
        
        _clearEvents = clearEvents;
        _teachEvents = teachEvents;
        _fromNode = fromNode;
        _toNode = toNode;
        
        if ( frame == null ){
            frame = topFrame;
        }
        _frame = frame;
        
        StringBuilder buf = new StringBuilder();
        buf.append("<html> ")
        .append( ("Please Confirm Write ") )
        .append( ("to <br>") )
        .append ( _toNode.toString() )
        .append("<hr>");
        
        if ( teachNVs ){
            
            // Bundle.getMessage("NVConfirmWrite",nodeName)
            buf.append("Teaching ")
            .append(_toNode.getNodeNvManager().getNvDifference(_fromNode))
            .append(" of ").append(_fromNode.getNodeNvManager().getTotalNVs()).append(" NV's<br>");
        }
        if ( _clearEvents ){
            buf.append("Clearing ").append(Math.max( 0,_toNode.getNodeEventManager().getTotalNodeEvents() )).append(" Events<br>");
        } 
        if ( _teachEvents ){
            buf.append("Teaching ").append(Math.max( 0,_fromNode.getNodeEventManager().getTotalNodeEvents() )).append(" Events<br>");
        }         
        buf.append("</html>");
        
        int response = JOptionPane.showConfirmDialog(frame,
                ( buf.toString() ),
                ( ("Please Confirm Write to Node")),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if ( response == JOptionPane.OK_OPTION ) {
            _toNode.addPropertyChangeListener(this);
            busy_dialog = new jmri.util.swing.BusyDialog(frame, "Write NVs "+_fromNode.toString(), false);
            busy_dialog.start();
            // update main node name from fcu name
            _toNode.setNameIfNoName( _fromNode.getUserName() );
            // request the local nv model pass the nv update request to the CbusNode
            if ( teachNVs ){
                _toNode.getNodeNvManager().sendNvsToNode( _fromNode.getNodeNvManager().getNvArray());
            }
            else {
                nVTeachComplete(0);
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent ev){
        if (ev.getPropertyName().equals("TEACHNVCOMPLETE")) {
            jmri.util.ThreadingUtil.runOnGUIEventually( ()->{
                nVTeachComplete((Integer) ev.getNewValue());
            });
        }
        else if (ev.getPropertyName().equals("ADDALLEVCOMPLETE")) {
            jmri.util.ThreadingUtil.runOnGUIEventually( ()->{
                teachEventsComplete((Integer) ev.getNewValue());
            });
        }
    }
    
    /**
     * Notification from CbusNode NV Teach is complete
     * Starts check to see if clear events
     * @param numErrors number of errors writing NVs
     */
    private void nVTeachComplete(int numErrors){
        if ( numErrors > 0 ) {
            JOptionPane.showMessageDialog(_frame, 
                Bundle.getMessage("NVSetFailTitle",numErrors), Bundle.getMessage("WarningTitle"),
                JOptionPane.ERROR_MESSAGE);
        }
        
        if ( _clearEvents ){
        
            busy_dialog.setTitle("Clear Events");
            
            // node enter learn mode
            _toNode.send.nodeEnterLearnEvMode( _toNode.getNodeNumber() ); 
            // no response expected but we add a mini delay for other traffic
            
            ThreadingUtil.runOnLayoutDelayed( () -> {
                _toNode.send.nNCLR(_toNode.getNodeNumber());// no response expected
            }, 150 );
            ThreadingUtil.runOnLayoutDelayed( () -> {
                // node exit learn mode
                _toNode.send.nodeExitLearnEvMode( _toNode.getNodeNumber() ); // no response expected
            }, jmri.jmrix.can.cbus.node.CbusNodeTimerManager.SINGLE_MESSAGE_TIMEOUT_TIME );
            ThreadingUtil.runOnGUIDelayed( () -> {
                
                clearEventsComplete();
            
            }, ( jmri.jmrix.can.cbus.node.CbusNodeTimerManager.SINGLE_MESSAGE_TIMEOUT_TIME + 150 ) );
        }
        else {
            clearEventsComplete();
        }
    }
    
    /**
     * When clear Events completed ( in nvTeachComplete )
     * starts process for teaching events to Node
     */
    private void clearEventsComplete() {
        ArrayList<CbusNodeEvent> arL = _fromNode.getNodeEventManager().getEventArray();
        if ( _teachEvents){
            if (arL==null){
                log.error("No Event Array on Node {}",_fromNode);
                teachEventsComplete(1);
                return;
            }
            busy_dialog.setTitle("Teach Events");
            _toNode.getNodeEventManager().sendNewEvSToNode( arL );
        }
        else {
            teachEventsComplete(0);
        }
    }
    
    /**
     * Notification from CbusNode Event Teach is complete
     * @param numErrors number of errors writing events
     */
    private void teachEventsComplete( int numErrors ) {
        _toNode.removePropertyChangeListener(this);
        busy_dialog.finish();
        busy_dialog = null;
        if (numErrors != 0 ) {
            JOptionPane.showMessageDialog(_frame, 
            Bundle.getMessage("NdEvVarWriteError"), Bundle.getMessage("WarningTitle"),
            JOptionPane.ERROR_MESSAGE);
        }
        _frame = null;
        _toNode = null;
    }
    
    /**
     * Get the Default Instance Node Model
     * @return Default Instance Node Model
     */
    protected CbusNodeTableDataModel getNodeModel(){
        return InstanceManager.getDefault(CbusNodeTableDataModel.class);
    }
    
    /**
     * Nested class to create one of these using old-style defaults.
     * Used as a startup action
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemNodeConfig"),
            new jmri.util.swing.sdi.JmriJFrameInterface(),
            NodeConfigToolPane.class.getName(),
            jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NodeConfigToolPane.class);

}
