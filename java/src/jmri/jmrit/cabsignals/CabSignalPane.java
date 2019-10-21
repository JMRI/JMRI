package jmri.jmrit.cabsignals;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
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
 * @since 4.13.4
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
    private JToggleButton masterPauseButton;
    JLabel textLocoLabel = new JLabel();
    DccLocoAddressSelector locoSelector = new DccLocoAddressSelector();
    RosterEntryComboBox locoRosterBox;
    JButton addLocoButton = new JButton();
    JButton resetLocoButton = new JButton();
    private int _rotationOffset;
    
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
            
        tcm.getColumnByModelIndex(CabSignalTableModel.NEXT_ASPECT_ICON).setCellRenderer( 
            tableSignalAspectRenderer() ); 
        
        slotScroll = new JScrollPane(slotTable);
        slotScroll.setPreferredSize(new Dimension(400, 200));
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add event displays
        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout());
        
        JPanel toppanelcontainer = new JPanel();
        // toppanelcontainer.setLayout(new BoxLayout(toppanelcontainer, BoxLayout.X_AXIS));
        
        masterPauseButton= new JToggleButton();
        masterPauseButton.setSelected(false); // cabdata on
        refreshMasterPauseButton();
        masterPauseButton.setVisible(true);
        masterPauseButton.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshMasterPauseButton();
            }
        });
        
        toppanelcontainer.add(masterPauseButton);
        
        JPanel locoSelectContainer = new JPanel();

        textLocoLabel.setText(Bundle.getMessage("LocoLabelText"));
        textLocoLabel.setVisible(true);

        locoSelector.setToolTipText(Bundle.getMessage("LocoSelectorToolTip"));
        locoSelector.setVisible(true);
        textLocoLabel.setLabelFor(locoSelector);

        locoSelectContainer.add(textLocoLabel);
        locoSelectContainer.add(locoSelector);

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
        locoSelectContainer.add(locoRosterBox);

        addLocoButton.setText(Bundle.getMessage("ButtonAddText"));
        addLocoButton.setVisible(true);
        addLocoButton.setToolTipText(Bundle.getMessage("AddButtonToolTip"));
        addLocoButton.addActionListener((ActionEvent e) -> {
            addLocoButtonActionPerformed(e);
        });
        locoSelectContainer.add(addLocoButton);

        resetLocoButton.setText(Bundle.getMessage("ButtonReset"));
        resetLocoButton.setVisible(true);
        resetLocoButton.setToolTipText(Bundle.getMessage("ResetButtonToolTip"));
        resetLocoButton.addActionListener((ActionEvent e) -> {
            locoSelector.reset();
            locoRosterBox.setSelectedIndex(0);
        });

        locoSelectContainer.add(resetLocoButton);
        locoSelectContainer.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        
        locoSelectContainer.setVisible(true);
        toppanelcontainer.add(locoSelectContainer);

        p1.add(toppanelcontainer, BorderLayout.PAGE_START);
        p1.add(slotScroll, BorderLayout.CENTER);        
        add(p1);
        
        Dimension p1size = new Dimension(450, 200);
        p1.setMinimumSize(p1size);
        
        p1.setVisible(true);
        log.debug("class name {} ",CabSignalPane.class.getName());
    }
    
    private void refreshMasterPauseButton(){
        if (masterPauseButton.isSelected()) { // is paused
            masterPauseButton.setText(Bundle.getMessage("SigDataResume"));
            masterPauseButton.setToolTipText(Bundle.getMessage("SigDataResumeTip"));
            slotModel.setPanelPauseButton( true );
        }
        else { // pause relased, go back to normal
            masterPauseButton.setText(Bundle.getMessage("SigDataPause"));
            masterPauseButton.setToolTipText(Bundle.getMessage("SigDataPauseTip"));
            slotModel.setPanelPauseButton( false );
        }
    }
    
    @Override
    public String getTitle() {
        return Bundle.getMessage("CabSignalPaneTitle");
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
        
        JMenu iconMenu = new JMenu(Bundle.getMessage("AspectIconMenu"));
        ButtonGroup offsetGroup = new ButtonGroup();
        
        JRadioButtonMenuItem offset0MenuItem = new JRadioButtonMenuItem(Bundle.getMessage("IconDegrees", 0));
        JRadioButtonMenuItem offset1MenuItem = new JRadioButtonMenuItem(Bundle.getMessage("IconDegrees", 90));
        JRadioButtonMenuItem offset2MenuItem = new JRadioButtonMenuItem(Bundle.getMessage("IconDegrees", 180));
        JRadioButtonMenuItem offset3MenuItem = new JRadioButtonMenuItem(Bundle.getMessage("IconDegrees", 270));
        
        offsetGroup.add(offset0MenuItem);
        offsetGroup.add(offset1MenuItem);
        offsetGroup.add(offset2MenuItem);
        offsetGroup.add(offset3MenuItem);
        
        iconMenu.add(offset0MenuItem);
        iconMenu.add(offset1MenuItem);
        iconMenu.add(offset2MenuItem);
        iconMenu.add(offset3MenuItem);
        
        menuList.add(iconMenu);
        
        _rotationOffset = 0; // startup
        offset0MenuItem.setSelected(true);
        ActionListener iconMenuListener = ae -> {
            if ( offset0MenuItem.isSelected() ) {
                _rotationOffset = 0;
            }
            else if ( offset1MenuItem.isSelected() ) {
                _rotationOffset = 1;
            }
            else if ( offset2MenuItem.isSelected() ) {
                _rotationOffset = 2;
            }
            else if ( offset3MenuItem.isSelected() ) {
                _rotationOffset = 3;
            }
            notifyCabSignalListChanged();
        };
        offset0MenuItem.addActionListener(iconMenuListener);
        offset1MenuItem.addActionListener(iconMenuListener);
        offset2MenuItem.addActionListener(iconMenuListener);
        offset3MenuItem.addActionListener(iconMenuListener);
        
        return menuList;
    }

    public void addLocoButtonActionPerformed(ActionEvent e) {
        if (locoSelector.getAddress() == null) {
            return;
        }
        LocoAddress locoaddress = locoSelector.getAddress();
        // create and inform CabSignal state of master pause / resume
        cabSignalManager.getCabSignal(locoaddress).setMasterCabSigPauseActive( masterPauseButton.isSelected() );
    }

    public void locoSelected() {
        if (locoRosterBox.getSelectedRosterEntries().length == 1) {
            locoSelector.setAddress(locoRosterBox.getSelectedRosterEntries()[0].getDccLocoAddress());
        }
    }
    
    
    private TableCellRenderer tableSignalAspectRenderer() {
    
        return new TableCellRenderer() {
            JLabel f = new JLabel();
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
                f.setIcon(null);
                if ( !value.toString().isEmpty() ) {
                    // value gets passed as a string so image can be rotated here
                    NamedIcon tmpIcon = new NamedIcon(value.toString(), value.toString() );
                    tmpIcon.setRotation( tmpIcon.getRotation() + _rotationOffset,slotScroll);
                    //  double d = mastIcon.reduceTo(28, 28, 0.01d);
                    f.setIcon(tmpIcon);
                }
                f.setText("");
                f.setHorizontalAlignment(JLabel.CENTER);
                if (isSelected) {
                    f.setBackground( table.getSelectionBackground() );
                } else {
                    f.setBackground(null);
                }
                return f;
            }
        };
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

    @Override
    public void notifyCabSignalListChanged(){
        slotModel.fireTableDataChanged();
    }

    private static final Logger log = LoggerFactory.getLogger(CabSignalPane.class);

}
