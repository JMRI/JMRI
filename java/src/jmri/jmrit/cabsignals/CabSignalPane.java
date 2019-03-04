package jmri.jmrit.cabsignals;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.UIManager;
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
 * Pane for monitoring and configuring a MERG CBUS Command Station
 * Created with Notepad++
 * @author Steve Young Copyright (C) 2018
 * @since 4.13.4
 */
public class CabSignalPane extends jmri.util.swing.JmriPanel implements CabSignalListListener {

    private CabSignalManager cabSignalManager;

    private JScrollPane scrolltablefeedback;
    private JSplitPane split;
    private double _splitratio = 0.95;
    protected JScrollPane slotScroll;
    
    protected CabSignalTableModel slotModel=null;
    protected JTable slotTable=null;
    protected final XTableColumnModel tcm = new XTableColumnModel();

    private JMenu cabsigMenu = new JMenu(Bundle.getMessage("SigDataOpt"));
    private JMenu colMenu = new JMenu((Bundle.getMessage("SessCol")));
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
        
        // configure items for GUI
        slotModel.configureTable(slotTable);
        
        slotTable.addMouseListener(new CabSignalButtonMouseListener(slotTable));
        
        TableCellRenderer chngBlockDirRenderer = new ChngBlockDirRenderer();
        TableColumn ChngBlockDirColumn = tcm.getColumnByModelIndex(CabSignalTableModel.REVERSE_BLOCK_DIR_BUTTON_COLUMN);                
        ChngBlockDirColumn.setMinWidth(80);
        //  ChngBlockDirColumn.setMaxWidth(80);        
        ChngBlockDirColumn.setCellRenderer(chngBlockDirRenderer);        
        
        slotScroll = new JScrollPane(slotTable);
        slotScroll.setPreferredSize(new Dimension(400, 200));
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add event displays
        JPanel p1 = new JPanel();
        // p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        p1.setLayout(new BorderLayout());
        // p1.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutEvents")));
        
        JPanel toppanelcontainer = new JPanel();
        toppanelcontainer.setLayout(new BoxLayout(toppanelcontainer, BoxLayout.X_AXIS));
        scrolltablefeedback = new JScrollPane (slotModel.tablefeedback());
        
        Dimension scrolltablefeedbackminimumSize = new Dimension(150, 20);
        scrolltablefeedback.setMinimumSize(scrolltablefeedbackminimumSize);
        
        masterSendCabDataButton= new JToggleButton(Bundle.getMessage("SigDataOn"));
        masterSendCabDataButton.setIcon(new NamedIcon("resources/icons/throttles/power_green.png", "resources/icons/throttles/power_green.png"));
        
        masterSendCabDataButton.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (masterSendCabDataButton.isSelected()) {
                    masterSendCabDataButton.setText(Bundle.getMessage("SigDataOff"));
                    masterSendCabDataButton.setIcon(new NamedIcon("resources/icons/throttles/power_red.png", "resources/icons/throttles/power_red.png"));                    
                    slotModel.masterSendCabData = false;
                    slotModel.masterSendCabDataButton(false);
                }
                else {
                    masterSendCabDataButton.setText(Bundle.getMessage("SigDataOn"));
                    masterSendCabDataButton.setIcon(new NamedIcon("resources/icons/throttles/power_green.png", "resources/icons/throttles/power_green.png"));
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
            //resetLocoButtonActionPerformed(e);
        });

        toppanelcontainer.add(resetLocoButton);


        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            slotScroll, scrolltablefeedback);
        split.setResizeWeight(_splitratio);
        split.setContinuousLayout(true);

        p1.add(toppanelcontainer, BorderLayout.PAGE_START);
        p1.add(split, BorderLayout.CENTER);        
        add(p1);
        
        Dimension p1size = new Dimension(450, 200);
        p1.setMinimumSize(p1size);
        
        p1.setVisible(true);
        log.debug("class name {} ",CabSignalPane.class.getName());
    }
    
	private static class ChngBlockDirRenderer implements TableCellRenderer {		
		@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			JButton button = (JButton)value;
			if (isSelected){
                    button.setForeground(table.getSelectionForeground());
                    button.setBackground(table.getSelectionBackground());
                }
                else {
                    button.setForeground(table.getForeground());
                    button.setBackground(UIManager.getColor("Button.background"));
                }
			return button;	
		}
	}    
    
    private static class CabSignalButtonMouseListener extends MouseAdapter {
		private final JTable table;
		public CabSignalButtonMouseListener(JTable table) {
			this.table = table;
		}
        @Override
		public void mouseClicked(MouseEvent e) {
			int column = table.getColumnModel().getColumnIndexAtX(e.getX());
			int row    = e.getY()/table.getRowHeight(); 

			if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
			    Object value = table.getValueAt(row, column);
			    if (value instanceof JButton) {
			    	((JButton)value).doClick();
			    }
			}
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
        cabSignalManager.addCabSignalListListener(this);
    }
    
    /**
     * Creates a Menu List
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<JMenu>();
        
        JCheckBoxMenuItem autorevblock = new JCheckBoxMenuItem(Bundle.getMessage("MAutoRev"));
        autorevblock.setSelected(true);
        autorevblock.setToolTipText(Bundle.getMessage("MAutoRevTip"));
        autorevblock.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                slotModel.autoreverseblockdir = autorevblock.isSelected();
            }
        });
        
        cabsigMenu.add(autorevblock);
        
        menuList.add(colMenu);
        menuList.add(cabSigColMenu);
        menuList.add(cabsigMenu);
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
