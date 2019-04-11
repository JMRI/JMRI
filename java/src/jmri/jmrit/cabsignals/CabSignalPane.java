package jmri.jmrit.cabsignals;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import jmri.LocoAddress;
import jmri.CabSignalListListener;
import jmri.CabSignalManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.roster.swing.GlobalRosterEntryComboBox;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for sending Cab Signal data via block lookup
 * @author Steve Young Copyright (C) 2018
 * @author Paul Bender Copyright (C) 2019
 * @see CabSignalTableModel
 * @since 4.15.4
 */
public class CabSignalPane extends jmri.util.swing.JmriPanel implements CabSignalListListener {

    private CabSignalManager cabSignalManager;

    protected JScrollPane slotScroll;
    
    protected CabSignalTableModel slotModel=null;
    protected JTable slotTable=null;
    protected final XTableColumnModel tcm = new XTableColumnModel();

    private JMenu cabSigColMenu = new JMenu(Bundle.getMessage("SigDataCol"));
    
    protected List<JCheckBoxMenuItem> colMenuList = new ArrayList<JCheckBoxMenuItem>();
    protected List<JCheckBoxMenuItem> cabSigColMenuList = new ArrayList<JCheckBoxMenuItem>();    
    private JToggleButton masterSendCabDataButton;
    JLabel textLocoLabel = new JLabel();
    DccLocoAddressSelector locoSelector = new DccLocoAddressSelector();
    RosterEntryComboBox locoRosterBox;
    JButton addLocoButton = new JButton();
    JButton resetLocoButton = new JButton();
    
    @Override
    public void initComponents() {
        super.initComponents();
        slotModel = new CabSignalTableModel(5,
            CabSignalTableModel.MAX_COLUMN); // row, column
        init();
    }


    public void init() {
        JTable slotTable = new JTable(slotModel) {
            // Override JTable Header to implement table header tool tips.
            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    @Override
                    public String getToolTipText(MouseEvent e) {
                        try {
                            java.awt.Point p = e.getPoint();
                            int index = columnModel.getColumnIndexAtX(p.x);
                            int realIndex = columnModel.getColumn(index).getModelIndex();
                            return CabSignalTableModel.columnToolTips[realIndex];    
                        } catch (RuntimeException e1) {
                            //catch null pointer exception if mouse is over an empty line
                        }
                        return null;
                    }
                };
            }
        };        
        
        // Use XTableColumnModel so we can control which columns are visible
        slotTable.setColumnModel(tcm);
        slotTable.createDefaultColumnsFromModel();
        
        for (int i = 0; i < slotTable.getColumnCount(); i++) {
            int colnumber=i;
            String colName = slotTable.getColumnName(colnumber);
            JCheckBoxMenuItem showcol = new JCheckBoxMenuItem(colName);
            colMenuList.add(showcol);
            cabSigColMenu.add(showcol); // cabsig columns
        }

        for (int i = 0; i < CabSignalTableModel.MAX_COLUMN; i++) {
            int colnumber=i;
                TableColumn column  = tcm.getColumnByModelIndex(colnumber);
                
            if (Arrays.stream(CabSignalTableModel.startupColumns).anyMatch(j -> j == colnumber)) {
                colMenuList.get(colnumber).setSelected(true);
                tcm.setColumnVisible(column, true);
            } else {
                colMenuList.get(colnumber).setSelected(false);
                tcm.setColumnVisible(column, false);
            }
        
            colMenuList.get(colnumber).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TableColumn column  = tcm.getColumnByModelIndex(colnumber);
                    boolean     visible = tcm.isColumnVisible(column);
                    tcm.setColumnVisible(column, !visible);
                }
            });
        }
        
        slotTable.setAutoCreateRowSorter(true);
        
        final TableRowSorter<CabSignalTableModel> sorter = new TableRowSorter<CabSignalTableModel>(slotModel);
        slotTable.setRowSorter(sorter);
        
        slotTable.setRowHeight(26);
        
        // configure items for GUI
        slotModel.configureTable(slotTable);
        
        tcm.getColumnByModelIndex(CabSignalTableModel.REVERSE_BLOCK_DIR_BUTTON_COLUMN).setCellRenderer( 
            new ButtonRenderer() );
        tcm.getColumnByModelIndex(CabSignalTableModel.REVERSE_BLOCK_DIR_BUTTON_COLUMN).setCellEditor(
            new ButtonEditor( new JButton() ) );   
        
        slotScroll = new JScrollPane(slotTable);
        slotScroll.setPreferredSize(new Dimension(400, 200));
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add event displays
        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout());
        
        JPanel toppanelcontainer = new JPanel();
        // toppanelcontainer.setLayout(new BoxLayout(toppanelcontainer, BoxLayout.X_AXIS));
        
        masterSendCabDataButton= new JToggleButton();
        masterSendCabDataButton.setSelected(false);
        setViewOnMasterCabSigButton( masterSendCabDataButton.isSelected() );
        
        masterSendCabDataButton.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                setViewOnMasterCabSigButton( masterSendCabDataButton.isSelected() );
                if (masterSendCabDataButton.isSelected()) {
                    slotModel.masterSendCabData = false;
                    slotModel.masterSendCabDataButton(false);
                }
                else {
                    slotModel.masterSendCabData = true;
                    slotModel.masterSendCabDataButton(true);
                }
            }
        }); 
        
        toppanelcontainer.add(masterSendCabDataButton);

        textLocoLabel.setText(Bundle.getMessage("LocoLabelText"));
        textLocoLabel.setVisible(true);

        locoSelector.setToolTipText(Bundle.getMessage("LocoSelectorToolTip"));
        locoSelector.setVisible(true);
        textLocoLabel.setLabelFor(locoSelector);

        toppanelcontainer.add(textLocoLabel);
        toppanelcontainer.add(locoSelector);

        locoSelector.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                // if we start typing, set the selected index of the locoRosterbox to nothing.
                locoRosterBox.setSelectedIndex(0);
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        locoRosterBox = new GlobalRosterEntryComboBox();
        locoRosterBox.setNonSelectedItem("");
        locoRosterBox.setSelectedIndex(0);

        locoRosterBox.addPropertyChangeListener("selectedRosterEntries", (PropertyChangeEvent pce) -> {
            locoSelected();
        });

        locoRosterBox.setVisible(true);
        toppanelcontainer.add(locoRosterBox);

        addLocoButton.setText(Bundle.getMessage("AddButtonText"));
        addLocoButton.setVisible(true);
        addLocoButton.setToolTipText(Bundle.getMessage("AddButtonToolTip"));
        addLocoButton.addActionListener((ActionEvent e) -> {
            addLocoButtonActionPerformed(e);
        });

        toppanelcontainer.add(addLocoButton);

        resetLocoButton.setText(Bundle.getMessage("ButtonReset"));
        resetLocoButton.setVisible(true);
        resetLocoButton.setToolTipText(Bundle.getMessage("ResetButtonToolTip"));
        resetLocoButton.addActionListener((ActionEvent e) -> {
            locoSelector.reset();
            locoRosterBox.setSelectedIndex(0);
        });

        toppanelcontainer.add(resetLocoButton);

        p1.add(toppanelcontainer, BorderLayout.PAGE_START);
        p1.add(slotScroll, BorderLayout.CENTER);        
        add(p1);
        
        Dimension p1size = new Dimension(450, 200);
        p1.setMinimumSize(p1size);
        
        p1.setVisible(true);
        log.debug("class name {} ",CabSignalPane.class.getName());
    }
    
    private void setViewOnMasterCabSigButton( boolean buttonSelected){
        
        if (masterSendCabDataButton.isSelected()) {
            masterSendCabDataButton.setText(Bundle.getMessage("SigDataOff"));
            masterSendCabDataButton.setIcon(
                new NamedIcon("resources/icons/panels/CSD/AZD/button/button-green-off.GIF", 
                "resources/icons/panels/CSD/AZD/button/button-green-off.GIF"));
        }
        else {
            masterSendCabDataButton.setText(Bundle.getMessage("SigDataOn"));
            masterSendCabDataButton.setIcon(
                new NamedIcon("resources/icons/panels/CSD/AZD/button/button-green.GIF",
                "resources/icons/panels/CSD/AZD/button/button-green.GIF"));
        }
    }
    
    @Override
    public String getTitle() {
        return Bundle.getMessage("MenuItemCabSignalPane");
    }

    
    public CabSignalPane() {
        super();
        cabSignalManager = jmri.InstanceManager.getNullableDefault(CabSignalManager.class);
        if(cabSignalManager == null){
           log.info("creating new DefaultCabSignalManager");
           jmri.InstanceManager.store(new jmri.managers.DefaultCabSignalManager(),CabSignalManager.class);
           cabSignalManager = jmri.InstanceManager.getNullableDefault(CabSignalManager.class); 
        }
        if (cabSignalManager != null) {
            cabSignalManager.addCabSignalListListener(this);
        }
    }
    
    /**
     * Creates a Menu List
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<JMenu>();
        
        menuList.add(cabSigColMenu);
        return menuList;
    }

    public void addLocoButtonActionPerformed(ActionEvent e) {
        if (locoSelector.getAddress() == null) {
            return;
        }
        LocoAddress locoaddress = locoSelector.getAddress();
        cabSignalManager.getCabSignal(locoaddress);
    }

    public void locoSelected() {
        if (locoRosterBox.getSelectedRosterEntries().length == 1) {
            locoSelector.setAddress(locoRosterBox.getSelectedRosterEntries()[0].getDccLocoAddress());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrit.cabsignals.CabSignalPane";
    }    
    
    @Override
    public void dispose() {
        cabSignalManager.removeCabSignalListListener(this);
        slotTable = null;
        slotModel.dispose();
        cabSignalManager = null;
        super.dispose();
    }


    // Cab Signal List Listener interface

    public void notifyCabSignalListChanged(){
        slotModel.fireTableDataChanged();
    }

    private static final Logger log = LoggerFactory.getLogger(CabSignalPane.class);

}
