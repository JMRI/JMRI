package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusAddress;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
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
public class NodeConfigToolPane extends jmri.jmrix.can.swing.CanPanel  {

    private CanSystemConnectionMemo _memo;
    public CbusNodeTableDataModel nodeModel=null;
    public JTable nodeTable=null;
    private CbusPreferences preferences;
    
    protected CbusNodeTablePane nodeTablePane = null;
    public CbusNodeEventVarPane nodeEventPane=null;
    private CbusNodeInfoPane nodeinfoPane = null;
 //   private CbusNodeUserCommentsPane commentsPane;
    private CbusNodeVarPane nodevarPane = null;
    private CbusNodeSetupPane setupPane;
    private CbusNodeRestoreFcuFrame fcuFrame;
    private CbusNodeEditEventFrame _editEventFrame;
    
    public JScrollPane eventScroll;
    public JScrollPane tabbedScroll;
    public JSplitPane split;
    protected JPanel pane1;
    // protected JPanel toppanelcontainer;
    // protected JPanel buttoncontainer = new JPanel();
    private JTabbedPane tabbedPane;
    
    private int _selectedNode;
    private Boolean editNvWindowActive;
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

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        try {
            nodeModel = jmri.InstanceManager.getDefault(CbusNodeTableDataModel.class);
        } catch (NullPointerException e) {
            log.error("Unable to get Node Table from Instance Manager");
        }
        
        _memo = memo;
        _selectedNode = -1;
        
        try {
            preferences = jmri.InstanceManager.getDefault(jmri.jmrix.can.cbus.CbusPreferences.class);
        } catch (NullPointerException e) {
            log.warn("Unable to get CBUS Preferences from Instance Manager");
        }
        init();
        
    }

    public NodeConfigToolPane() {
        super();
    }

    public void init() {
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       
        // main pane
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BorderLayout());
        
        editNvWindowActive = false;
        
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
        
        nodeEventPane = new CbusNodeEventVarPane(this);
        nodeEventPane.initComponents(memo);
        
        nodeinfoPane = new CbusNodeInfoPane();
        nodeinfoPane.initComponents(null);
        
        setupPane = new CbusNodeSetupPane(this);
        setupPane.initComponents(0);
        
      //  commentsPane = new CbusNodeUserCommentsPane();
      //  commentsPane.initComponents(0);
        
        nodevarPane = new CbusNodeVarPane(this);
        nodevarPane.initComponents(memo);
        
        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab(("Node Info"), nodeinfoPane);
        // node comments
        tabbedPane.addTab(("Node Variables"), nodevarPane);
        tabbedPane.addTab(("Node Events"), nodeEventPane);
        // node setup
        tabbedPane.addTab(("Node Setup"),setupPane);
        
      //  tabbedPane.addTab(("Node Comments"),commentsPane);
        
        tabbedPane.setEnabledAt(1,false);
        tabbedPane.setEnabledAt(2,false);
        tabbedPane.setEnabledAt(3,false);
      //  tabbedPane.setEnabledAt(4,false);
        
        
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainNodePane, tabbedPane);
        split.setDividerLocation(0.5);
        split.setContinuousLayout(true);
        
        pane1.add(split, BorderLayout.CENTER);
        
        setPreferredSize(new Dimension(700, 400));
        
        add(pane1);
        pane1.setVisible(true);
        
        tabbedPane.addChangeListener((ChangeEvent e) -> {
            userViewChanged();
        });
        
        // also add listener to tab action
        nodeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if ( !e.getValueIsAdjusting() ) {
                    userViewChanged();
                }
            }
        });
        
        tabbedPane.setTransferHandler(new TransferHandler());
        nodeTable.setTransferHandler(new TransferHandler());
        
        revalidate();
        // major resize, repack
       // ((JFrame) getTopLevelAncestor()).pack();
       
    }
    
    private JFrame topFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
    
    // create a non-modal dialogue box with node search results
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
    
    // no need to initialise info pane, nv or event panes
    protected void userViewChanged(){
        
        int sel = nodeTable.getSelectedRow();
        int rowBefore = nodeTable.getSelectedRow()-1;
        int rowAfter = nodeTable.getSelectedRow()+1;
        if ( sel > -1 ) {
            
            sel = nodeTable.convertRowIndexToModel(sel);
            _selectedNode = (int) nodeTable.getModel().getValueAt(sel, CbusNodeTableDataModel.NODE_NUMBER_COLUMN);
            
            int tabindex = tabbedPane.getSelectedIndex();
            
            int nodeBefore = -1;
            int nodeAfter = -1;
            
            log.debug("before {} after {}",rowBefore,rowAfter);
            
            if ( rowBefore > -1 ) {
                nodeBefore = (int) nodeTable.getModel().getValueAt(rowBefore, CbusNodeTableDataModel.NODE_NUMBER_COLUMN);
            }
            
            if ( rowAfter < nodeTable.getRowCount() ) {
                nodeAfter = (int) nodeTable.getModel().getValueAt(rowAfter, CbusNodeTableDataModel.NODE_NUMBER_COLUMN);
            }
            
            log.debug("node {} selected tab index {} , node before {} node after {}", _selectedNode , tabindex, nodeBefore,nodeAfter );                

            // this also starts urgent fetch loop if not currently looping
            nodeModel.setUrgentFetch(tabindex,_selectedNode,nodeBefore,nodeAfter);
            
            tabbedPane.setEnabledAt(1,true);
            tabbedPane.setEnabledAt(2,true);
            tabbedPane.setEnabledAt(3,true);
          //  tabbedPane.setEnabledAt(4,true);
            
            
            if ( tabindex == 0 ){ // parameters
            
                nodeEventPane.setNode( null );
                nodevarPane.setNode( null );
                nodeinfoPane.initComponents( nodeModel.getNodeByNodeNum(_selectedNode) );
                
                if ( nodeModel.getNodeByNodeNum(_selectedNode).getOutstandingParams() > 0 ){
    
                    // fetch from node already happening in model
                    // so we refresh the screen after 1000ms
                    // the other tabs have their values updated by jtable mechanisms
                    ThreadingUtil.runOnGUIDelayed( () -> {
                    
                        int newsel = nodeTable.convertRowIndexToModel(nodeTable.getSelectedRow());
                        
                        if ( newsel > -1 ) {
                        
                            int newnodenum = (int) nodeTable.getModel().getValueAt(newsel, CbusNodeTableDataModel.NODE_NUMBER_COLUMN);
                            
                            // reset node in case has changed
                            nodeinfoPane.initComponents( nodeModel.getNodeByNodeNum(newnodenum) );
                        }
                    
                    },1000 );
                }
                
            }
            
            if ( tabindex == 1 ){ // NV's
                nodevarPane.setNode( nodeModel.getNodeByNodeNum(_selectedNode) );
                nodeEventPane.setNode( null );
                ThreadingUtil.runOnGUIDelayed( () -> {
                    // refetch node number in case has changed
                    nodevarPane.refreshEditButton();
                },20 );
            }
            
            if ( tabindex == 2 ){ // events
                nodeEventPane.setNode( nodeModel.getNodeByNodeNum(_selectedNode) );
                nodevarPane.setNode( null );
            }

            if ( tabindex == 3 ) { // Network setup
                setupPane.initComponents(_selectedNode);
                nodeEventPane.setNode( null );
                nodevarPane.setNode( null );
            }
            
         //   if ( tabindex == 4 ) {
         //       commentsPane.initComponents(_selectedNode);
         //   }
            
        }
        else {
            
            log.debug("selected node -1");
            tabbedPane.setEnabledAt(1,false);
            tabbedPane.setEnabledAt(2,false);
            tabbedPane.setEnabledAt(3,false);
            nodeinfoPane.initComponents(null);
            tabbedPane.setSelectedIndex(0);
            
        }
    }
    
    private void setMenuOptions(){
        
        nodeNumRequestMenuItem.setSelected( preferences.getAllocateNNListener() );
        backgroundDisabled.setSelected(false);
        backgroundSlow.setSelected(false);
        backgroundFast.setSelected(false);
        
        if ( preferences.getNodeBackgroundFetchDelay()==0L ) {
            backgroundDisabled.setSelected(true);
        }
        else if ( preferences.getNodeBackgroundFetchDelay()==50L ) {
            backgroundFast.setSelected(true);
        }
        else if ( preferences.getNodeBackgroundFetchDelay()==100L ) {
            backgroundSlow.setSelected(true);
        }
        
        addCommandStationMenuItem.setSelected( preferences.getAddCommandStations() );
        addNodesMenuItem.setSelected( preferences.getAddNodes() );
        
        startupCommandStationMenuItem.setSelected( preferences.getStartupSearchForCs() );
        startupNodesMenuItem.setSelected( preferences.getStartupSearchForNodes() );
        
    }
    
    /**
     * Creates a Menu List.
     * <p>
     * File - Print, Print Preview, Save, SaveAs csv
     * <p>
     * Display - show / hide Create new event pane, show/hide bottom feedback pane.
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<JMenu>();
        
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
        
        menuList.add(fileMenu);
        menuList.add(optionsMenu);
        
        ActionListener teachNodeFcu = ae -> {
            fcuFrame = new CbusNodeRestoreFcuFrame(this);
            fcuFrame.initComponents(_memo);
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
            nodeModel.sendSystemReset();
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
        
        setMenuOptions();
        
        return menuList;
    }
    
    protected void setRestoreFcuActive( boolean isActive ){
        teachNodeFromFcuFile.setEnabled(!isActive);
    }

    protected Boolean getEditNvActive() {
        return editNvWindowActive;
    }

    protected void setEditNvActive( Boolean state ) {
        editNvWindowActive = state;
        if ( nodevarPane.editButton != null ) {
            nodevarPane.editButton.setEnabled(!state);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " " + Bundle.getMessage("MenuItemNodeConfig"));
        }
        return Bundle.getMessage("MenuItemNodeConfig");
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
      //  nodeTable = null;
      //  eventScroll = null;
        super.dispose();
    }

    private class TransferHandler extends javax.swing.TransferHandler {

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

        @Override
        public boolean importData(JComponent c, Transferable t) {
            if (canImport(c, t.getTransferDataFlavors())) {
                
                String eventInJmriFormat;
                try {
                    eventInJmriFormat = (String) t.getTransferData(DataFlavor.stringFlavor);
                } catch (Exception e) {
                    log.error("unable to get dragged address");
                    e.printStackTrace();
                    return false;
                }
                
                return openNewOrEditEventFrame(eventInJmriFormat);
            }
            return false;
        }
    }
    
    // lower case boolean does not have a null
    boolean openNewOrEditEventFrame( String eventInJmriFormat ){
        
        // do some validation on the input string
        // processed in the same way as a sensor, turnout or light so less chance of breaking in future
        // and can also accept the Hex "X1234;X654876" format
        String validatedAddr;
        try {    
            validatedAddr = CbusAddress.validateSysName( eventInJmriFormat );
        } catch (IllegalArgumentException e) {
            return false;
        }
        
        CanMessage m = ( new CbusAddress(validatedAddr) ).makeMessage(0x12);
        
        CbusNodeEvent newev = new CbusNodeEvent(
            CbusMessage.getNodeNumber(m), 
            CbusMessage.getEvent(m), 
            _selectedNode, 
            -1, 
            nodeModel.getNodeByNodeNum( _selectedNode ).getParameter(5)
            );
        java.util.Arrays.fill(newev._evVarArr,0);
        
        log.debug("dragged nodeevent {} ",newev);
        ThreadingUtil.runOnGUI( () -> {
            getEditEvFrame().initComponents(_memo,newev);
        });
        
        
        return true;
        
    }
    
    // this could be requested from
    // CbusNodeEventDataModel button click to edit event
    // this class when it receives an event via drag n drop
    // creating new event from CbusNodeEventVarPane
    public CbusNodeEditEventFrame getEditEvFrame(){
        
        if (_editEventFrame == null ){
            _editEventFrame = new CbusNodeEditEventFrame(this);
        }
        
        return _editEventFrame;
    }
    
    // notification from the frame that it has disposed
    protected void clearEditEventFrame() {
        _editEventFrame = null;
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
