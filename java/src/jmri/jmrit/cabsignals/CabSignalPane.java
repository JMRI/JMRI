package jmri.jmrit.cabsignals;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DefaultFormatter;
import jmri.LocoAddress;
import jmri.CabSignalListListener;
import jmri.CabSignalManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.roster.swing.GlobalRosterEntryComboBox;
import jmri.jmrit.roster.swing.RosterEntryComboBox;
import jmri.util.swing.XTableColumnModel;
import jmri.util.swing.StayOpenCheckBoxItem;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.util.table.JTableWithColumnToolTips;

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

    private JScrollPane slotScroll;
    private CabSignalTableModel slotModel;
    private JTable _slotTable;
    private XTableColumnModel tcm;

    private JMenu cabSigColMenu;
    private List<JCheckBoxMenuItem> colMenuList;
    private JToggleButton masterPauseButton;
    private JLabel textLocoLabel;
    private DccLocoAddressSelector locoSelector;
    private RosterEntryComboBox locoRosterBox;
    private JButton addLocoButton;
    private JButton resetLocoButton;
    private int _rotationOffset;
    private int _defaultRowHeight;
    
    public CabSignalPane() {
        super();
        cabSignalManager = jmri.InstanceManager.getNullableDefault(CabSignalManager.class);
        if(cabSignalManager == null){
           log.info("creating new DefaultCabSignalManager");
           jmri.InstanceManager.store(new jmri.managers.DefaultCabSignalManager(),CabSignalManager.class);
           cabSignalManager = jmri.InstanceManager.getNullableDefault(CabSignalManager.class); 
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        super.initComponents();
        if (cabSignalManager != null) {
            cabSignalManager.addCabSignalListListener(this);
        }
        slotModel = new CabSignalTableModel(5,
            CabSignalTableModel.MAX_COLUMN); // row, column
        
        tcm = new XTableColumnModel();
        cabSigColMenu = new JMenu(Bundle.getMessage("SigDataCol"));
        colMenuList = new ArrayList<>();
        textLocoLabel = new JLabel();
        locoSelector = new DccLocoAddressSelector();
        addLocoButton = new JButton();
        resetLocoButton = new JButton();
        _defaultRowHeight = 26;
        init();
    }

    public void init() {
        _slotTable = new JTableWithColumnToolTips(slotModel,CabSignalTableModel.COLUMNTOOLTIPS);        
        
        // Use XTableColumnModel so we can control which columns are visible
        _slotTable.setColumnModel(tcm);
        _slotTable.createDefaultColumnsFromModel();
        
        for (int i = 0; i < _slotTable.getColumnCount(); i++) {
            int colnumber=i;
            String colName = _slotTable.getColumnName(colnumber);
            StayOpenCheckBoxItem showcol = new StayOpenCheckBoxItem(colName);
            showcol.setToolTipText(CabSignalTableModel.COLUMNTOOLTIPS[i]);
            colMenuList.add(showcol);
            cabSigColMenu.add(showcol); // cabsig columns
        }

        for (int i = 0; i < CabSignalTableModel.MAX_COLUMN; i++) {
            int colnumber=i;
                TableColumn column  = tcm.getColumnByModelIndex(colnumber);
                
            if (Arrays.stream(CabSignalTableModel.STARTUPCOLUMNS).anyMatch(j -> j == colnumber)) {
                colMenuList.get(colnumber).setSelected(true);
                tcm.setColumnVisible(column, true);
            } else {
                colMenuList.get(colnumber).setSelected(false);
                tcm.setColumnVisible(column, false);
            }
        
            colMenuList.get(colnumber).addActionListener((ActionEvent e) -> {
                TableColumn column1 = tcm.getColumnByModelIndex(colnumber);
                boolean visible1 = tcm.isColumnVisible(column1);
                tcm.setColumnVisible(column1, !visible1);
            });
        }
        
        _slotTable.setAutoCreateRowSorter(true);
        
        final TableRowSorter<CabSignalTableModel> sorter = new TableRowSorter<>(slotModel);
        _slotTable.setRowSorter(sorter);
        
        _slotTable.setRowHeight(_defaultRowHeight);
        
        // configure items for GUI
        slotModel.configureTable(_slotTable);
        
        tcm.getColumnByModelIndex(CabSignalTableModel.REVERSE_BLOCK_DIR_BUTTON_COLUMN).setCellRenderer( 
            new ButtonRenderer() );
        tcm.getColumnByModelIndex(CabSignalTableModel.REVERSE_BLOCK_DIR_BUTTON_COLUMN).setCellEditor(
            new ButtonEditor( new JButton() ) );
            
        tcm.getColumnByModelIndex(CabSignalTableModel.NEXT_ASPECT_ICON).setCellRenderer( 
            tableSignalAspectRenderer() ); 
        
        slotScroll = new JScrollPane(_slotTable);
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
        masterPauseButton.addActionListener ((ActionEvent e) -> {
            refreshMasterPauseButton();
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return Bundle.getMessage("CabSignalPaneTitle");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<>();
        
        menuList.add(cabSigColMenu);
        
        JMenu displayMenu = new JMenu(Bundle.getMessage("DisplayMenu"));
        
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
        
        displayMenu.add(iconMenu);
        
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
        
        ActionListener rowHeightMenuListener = ae -> {
            JSpinner delaySpinner = getNewRowHeightSpinner();
            int option = JOptionPane.showOptionDialog(this, 
                delaySpinner, 
                Bundle.getMessage("RowHeightOption"), 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (option == JOptionPane.OK_OPTION) {
                _defaultRowHeight = (Integer) delaySpinner.getValue();
            }
            else {
                _slotTable.setRowHeight(_defaultRowHeight);
            }
        };

        JMenuItem searchForNodesMenuItem = new JMenuItem(Bundle.getMessage("RowHeightOption"));
        searchForNodesMenuItem.addActionListener(rowHeightMenuListener);
        displayMenu.add(searchForNodesMenuItem);
        
        menuList.add(displayMenu);
        
        return menuList;
    }
    
    private JSpinner getNewRowHeightSpinner() {
        JSpinner rqnnSpinner = new JSpinner(new SpinnerNumberModel(_defaultRowHeight, 10, 150, 1));
        JComponent rqcomp = rqnnSpinner.getEditor();
        JFormattedTextField rqfield = (JFormattedTextField) rqcomp.getComponent(0);
        DefaultFormatter rqformatter = (DefaultFormatter) rqfield.getFormatter();
        rqformatter.setCommitsOnValidEdit(true);
        rqnnSpinner.addChangeListener((ChangeEvent e) -> {
            _slotTable.setRowHeight((Integer) rqnnSpinner.getValue());
        });
        return rqnnSpinner;
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
            /**
             * {@inheritDoc}
             */
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        cabSignalManager.removeCabSignalListListener(this);
        _slotTable = null;
        slotModel.dispose();
        cabSignalManager = null;
        super.dispose();
    }

    /**
     * {@inheritDoc}
     * Cab Signal List Listener interface
     */
    @Override
    public void notifyCabSignalListChanged(){
        slotModel.fireTableDataChanged();
    }

    private static final Logger log = LoggerFactory.getLogger(CabSignalPane.class);

}
