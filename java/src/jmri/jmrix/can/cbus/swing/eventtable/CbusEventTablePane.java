package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.eventtable.*;
import jmri.jmrix.can.cbus.swing.*;
import jmri.swing.JmriJTablePersistenceManager;
import jmri.util.swing.XTableColumnModel;
import jmri.util.swing.StayOpenCheckBoxItem;
import jmri.util.table.JTableToCsvAction;
import jmri.util.table.JTableWithColumnToolTips;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Pane providing a CBUS event table.
 *
 * @author Andrew Crosland (C) 2009
 * @author Kevin Dickerson (C) 2012
 * @author Steve Young (C) 2018
 * @see CbusEventTableDataModel
 *
 * @since 2.99.2
 */
public class CbusEventTablePane extends jmri.jmrix.can.swing.CanPanel {

    protected CbusEventTableDataModel eventModel;
    protected JTable eventTable;
    protected CbusPreferences preferences;
    private jmri.UserPreferencesManager p;
    
    private CbusCreateBeanPane newBeanPanel;
    protected CbusNewEventPane neweventcontainer;
    private CbusSendEventPane sendPane;
    protected JPanel filterpanel;
    
    public CbusEventTable cbEvTable;

    protected final JTextField filterText = new JTextField("",8);
    protected JButton clearfilterButton;

    protected final JMenu evColMenu = new JMenu(Bundle.getMessage("evColMenuName")); // NOI18N
    protected final JMenu evStatMenu = new JMenu(Bundle.getMessage("evStatMenuName")); // NOI18N
    protected final JMenu evJmMenu = new JMenu(Bundle.getMessage("latestEvCols")); // NOI18N
    protected final JMenu buttonMenu = new JMenu(Bundle.getMessage("buttonCols")); // NOI18N
    
    private StayOpenCheckBoxItem showfilterpanel;
    private StayOpenCheckBoxItem shownewevent;
    private StayOpenCheckBoxItem showNewBeanPanel;
    private StayOpenCheckBoxItem showSendEventPanel;

    private final XTableColumnModel tcm;
    
    public CbusEventTablePane() {
        super();
        tcm = new XTableColumnModel();
    }
    
    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        
        preferences = InstanceManager.getDefault(CbusPreferences.class);
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        
        CbusEventTableDataModel.checkCreateNewEventModel(memo);
        eventModel = InstanceManager.getNullableDefault(CbusEventTableDataModel.class);
        
        eventTable = new JTableWithColumnToolTips(eventModel, CbusEventTableDataModel.CBUS_EV_TABLE_COL_TOOLTIPS);

        // Use XTableColumnModel so we can control which columns are visible
        eventTable.setColumnModel(tcm);
        
        init();
    }
    
    public final void init() {
        
        setLayout(new BorderLayout());
        
        JPanel _toppanelcontainer = new JPanel();
        _toppanelcontainer.setLayout(new BoxLayout(_toppanelcontainer, BoxLayout.X_AXIS));
        
        JPanel _topToppanelcontainer = new JPanel();
        _topToppanelcontainer.setLayout(new BoxLayout(_topToppanelcontainer, BoxLayout.Y_AXIS));
        
        neweventcontainer = new CbusNewEventPane(this);
        cbEvTable = new CbusEventTable(this);
        newBeanPanel = new CbusCreateBeanPane(this);        
        sendPane = new CbusSendEventPane(this);
        
        _toppanelcontainer.add(newBeanPanel);
        _toppanelcontainer.add(getFilterPanel());
        _toppanelcontainer.add(neweventcontainer);
        _topToppanelcontainer.add(_toppanelcontainer);
        _topToppanelcontainer.add(newBeanPanel);
        _topToppanelcontainer.add(sendPane);
        add(_topToppanelcontainer, BorderLayout.PAGE_START);
        
        add(cbEvTable, BorderLayout.CENTER);
        setVisible(true);
        
        cbEvTable.tableChanged(null);
        
    }
    
    private void setPanesVisibleFromSettings() {
        
        if ( p.getSimplePreferenceState(this.getClass().getName() + ".Notfirstrun") ) { // NOI18N
            showfilterpanel.setSelected(p.getSimplePreferenceState(getClass().getName() + ".Showfilterpanel")); // NOI18N
            shownewevent.setSelected(p.getSimplePreferenceState(getClass().getName() + ".Shownewevent")); // NOI18N
            showNewBeanPanel.setSelected(p.getSimplePreferenceState(getClass().getName() + ".ShowNewBeanPanel")); // NOI18N
            showSendEventPanel.setSelected(p.getSimplePreferenceState(getClass().getName() + ".ShowSendEventPanel")); // NOI18N
            
            filterpanel.setVisible(p.getSimplePreferenceState(getClass().getName() + ".Showfilterpanel")); // NOI18N
            neweventcontainer.setVisible(p.getSimplePreferenceState(getClass().getName() + ".Shownewevent")); // NOI18N
            newBeanPanel.setVisible(p.getSimplePreferenceState(getClass().getName() + ".ShowNewBeanPanel")); // NOI18N
            sendPane.setVisible(p.getSimplePreferenceState(getClass().getName() + ".ShowSendEventPanel")); // NOI18N
            
        } else {
            // set virgin load view
            showfilterpanel.setSelected(true);
            shownewevent.setSelected(true);
            showNewBeanPanel.setSelected(false);
            showSendEventPanel.setSelected(false);
            newBeanPanel.setVisible(false);
            sendPane.setVisible(false);
        }
    }
    
    private JPanel getFilterPanel(){
        
        filterpanel = new JPanel();
        filterpanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("FilterSurround"))); // NOI18N
        
        clearfilterButton = new JButton(Bundle.getMessage("ClearFilter")); // NOI18N
        clearfilterButton.setEnabled(false);
        clearfilterButton.addActionListener((ActionEvent e) -> filterText.setText("") );
        
        filterText.setMaximumSize( filterText.getPreferredSize() );
        
        filterpanel.add(filterText);
        filterpanel.add(clearfilterButton);
        
        cbEvTable.addFilterListener(filterText);
        
        return filterpanel;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.swing.eventtable.EventTablePane"; // NOI18N
    }
    
    /**
     * Creates a Menu List
     * <p>
     * File - Print, Print Preview, Save, SaveAs csv
     * Display - show / hide Create new event pane, show/hide bottom feedback pane
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<>();
        
        menuList.add(getFileMenu());
        menuList.add(getDisplayMenu());
        menuList.add(evColMenu);
        menuList.add(evJmMenu);
        menuList.add(buttonMenu);
        menuList.add(evStatMenu);
        return menuList;
    }
    
    private JMenu getFileMenu(){
    
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile")); // NOI18N
    
        StayOpenCheckBoxItem saveRestoreEventTableItem = new StayOpenCheckBoxItem(Bundle.getMessage("SaveEvSession")); // NOI18N
        saveRestoreEventTableItem.setSelected( preferences.getSaveRestoreEventTable() );
        saveRestoreEventTableItem.addActionListener((ActionEvent e) ->
            preferences.setSaveRestoreEventTable(saveRestoreEventTableItem.isSelected()) );
    
        fileMenu.add(saveRestoreEventTableItem);
        fileMenu.add( new JSeparator() );
        fileMenu.add(new JTableToCsvAction(Bundle.getMessage("ExportCsvAll"),
            null, eventModel, "myevents.csv", CbusEventTableDataModel.BUTTON_COLUMNS)); // NOI18N
        fileMenu.add(new JTableToCsvAction(Bundle.getMessage("ExportCsvView"),
            eventTable, eventModel, "myevents.csv", CbusEventTableDataModel.BUTTON_COLUMNS)); // NOI18N
        
        // add print menu items
        fileMenu.add( new JSeparator() );
        fileMenu.add(new CbusEventTablePrintAction(Bundle.getMessage("PrintTable"),eventModel,getTitle(),false)); // NOI18N
        fileMenu.add(new CbusEventTablePrintAction(Bundle.getMessage("PreviewTable"),eventModel,getTitle(),true)); // NOI18N
        return fileMenu;
    
    }
    
    private JMenu getDisplayMenu(){
    
        JMenu displayMenu = new JMenu(Bundle.getMessage("Display")); // NOI18N
        
        showNewBeanPanel = new StayOpenCheckBoxItem(Bundle.getMessage("NewTsl")); // NOI18N
        showNewBeanPanel.addActionListener((ActionEvent e) ->
            newBeanPanel.setVisible(showNewBeanPanel.isSelected()) );
        
        shownewevent = new StayOpenCheckBoxItem((Bundle.getMessage("NewEvent"))); // NOI18N
        shownewevent.addActionListener((ActionEvent e) ->
            neweventcontainer.setVisible(shownewevent.isSelected()) );
        
        showfilterpanel = new StayOpenCheckBoxItem(Bundle.getMessage("FilterSurround")); // NOI18N
        showfilterpanel.addActionListener((ActionEvent e) -> 
            filterpanel.setVisible(showfilterpanel.isSelected()) );

        showSendEventPanel = new StayOpenCheckBoxItem(Bundle.getMessage("ButtonSendEvent")); // NOI18N
        showSendEventPanel.addActionListener((ActionEvent e) ->
            sendPane.setVisible(showSendEventPanel.isSelected()) );
        
        setPanesVisibleFromSettings();
        
        displayMenu.add(showfilterpanel);        
        displayMenu.add(shownewevent);
        displayMenu.add(showNewBeanPanel);
        displayMenu.add(showSendEventPanel);
        
        return displayMenu;
    
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return prependConnToString(Bundle.getMessage("EventTableTitle")); // NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        
        if(cbEvTable != null) {
            cbEvTable.dispose();
        }
        InstanceManager.getOptionalDefault(JmriJTablePersistenceManager.class).ifPresent((tpm) -> {
            tpm.stopPersisting(eventTable);
        });
        
        p.setSimplePreferenceState(getClass().getName() + ".Notfirstrun", true); // NOI18N
        p.setSimplePreferenceState(getClass().getName() + ".Showfilterpanel", showfilterpanel.isSelected()); // NOI18N
        p.setSimplePreferenceState(getClass().getName() + ".Shownewevent", shownewevent.isSelected()); // NOI18N
        p.setSimplePreferenceState(getClass().getName() + ".ShowNewBeanPanel", showNewBeanPanel.isSelected()); // NOI18N
        p.setSimplePreferenceState(getClass().getName() + ".ShowSendEventPanel", showSendEventPanel.isSelected()); // NOI18N
        super.dispose();
    }

    /**
     * Nested class to create one of these using old-style defaults.
     * Used as a startup action
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemEventTable"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    CbusEventTablePane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTablePane.class);

}
