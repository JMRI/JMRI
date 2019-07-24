package jmri.jmrix.cmri.serial.serialmon;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for a message filter for CMRInet network packets.
 *
 * @author	 Chuck Catania   Copyright (C) 2016
 */
public class SerialFilterFrame extends jmri.util.JmriJFrame {

    ArrayList<SerialNode> monitorNode = new  ArrayList<SerialNode>();

    // node table pane items
    protected JPanel nodeEnablePanel = null;
    protected Border nodeEableBorder = BorderFactory.createEtchedBorder();
    protected Border nodeEableBorderTitled = BorderFactory.createTitledBorder(nodeEableBorder,"Monitor Nodes",TitledBorder.LEFT,TitledBorder.ABOVE_TOP);

    protected Border packetTypesBorder = BorderFactory.createEtchedBorder();
    protected Border packetTypesBorderTitled = BorderFactory.createTitledBorder(packetTypesBorder,"Packet Types",TitledBorder.LEFT,TitledBorder.ABOVE_TOP);

    protected Border enabledBorder = BorderFactory.createEtchedBorder();
    protected Border enabledBorderTitled = BorderFactory.createTitledBorder(enabledBorder,"Select Nodes",TitledBorder.LEFT,TitledBorder.ABOVE_TOP);

    protected Border packetSelectBorder = BorderFactory.createEtchedBorder();
    protected Border packetSelectBorderTitled = BorderFactory.createTitledBorder(packetSelectBorder,"Select Packets",TitledBorder.LEFT,TitledBorder.ABOVE_TOP);

    protected Border mainButtonsBorder = BorderFactory.createEtchedBorder();
    protected Border mainButtonsBorderTitled = BorderFactory.createTitledBorder(mainButtonsBorder,"");

    protected JTable nodeTable = null;
    protected TableModel nodeTableModel = null;

    // button pane items
    JButton doneButton = new JButton(Bundle.getMessage("DoneButtonText") );
    JButton haltPollButton = new JButton(Bundle.getMessage("HaltPollButtonText") );

    JButton nodeMonitorAll = new JButton(Bundle.getMessage("AllButtonText") );
    JButton nodeMonitorNone = new JButton(Bundle.getMessage("NoneButtonText") );

    JButton packetMonitorAll = new JButton(Bundle.getMessage("AllButtonText") );
    JButton packetMonitorNone = new JButton(Bundle.getMessage("NoneButtonText") );

    // CMRInet packet monitor variables only persistent when running
    //--------------------------------------------------------------
    public static final int monPktInit     =  0; // (I) 0x49 Initialize
    public static final int monPktPoll     =  1; // (P) 0x50 Poll
    public static final int monPktRead     =  2; // (R) 0x52 Read
    public static final int monPktTransmit =  3; // (T) 0x54 Transmit
    public static final int monPktEOT      =  4; // (E) 0x45 EOT
    public static final int monPktQuery    =  5; // (Q) 0x51 Query
    public static final int monPktDGread   =  6; // (D) 0x44 Datagram Read
    public static final int monPktDGwrite  =  7; // (W) 0x57 Datagram Write
    public static final int monPktDGack    =  8; // (A) 0x41 Datagram Ack
    public static final int monPktCodeline =  9; // (C) 0x43 Code Line
    public static final int monPktNMRAmast = 10; // (M) 0x4D NMRA Mast
    public static final int monPktRFE      = 11; // (?) 0x3F RFE
/*
    public static final int monPktRFE      = 12; // RFE
    public static final int monPktRFE      = 13; // RFE
    public static final int monPktRFE      = 14; // RFE
    public static final int monPktRFE      = 15; // RFE
*/
    public static final int numMonPkts     = monPktRFE;
    public static final int lastStdPkt     = monPktTransmit+1;
    public static final int[] monPktTypeID = {
                                                0x49, 0x50, 0x52, 0x54, 0x45,
                                                0x51, 0x44, 0x57, 0x41, 0x43,
                                                0x4D, 0x3F
                                              };

    ArrayList<JCheckBox> packetChkBoxes = new ArrayList<JCheckBox>();
    String packetChkBoxLabels[] = {
                                   "(I) Initialize    ",
                                   "(P) Poll          ",
                                   "(R) Read          ",
                                   "(T) Transmit      ",
                                   "(E) EOT           ",
                                   "(Q) Query         ",
                                   "(D) Datagram Read ",
                                   "(W) Datagram Write",
                                   "(A) Datagram Ack  ",
                                   "(C) Code Line     ",
                                   "(M) NMRA Mast     ",
                                   "RFE               "
                                  };

    protected JPanel packetTypes  = new JPanel();

    HandlerClass packetTypeCkBoxHandler = new HandlerClass();
    private CMRISystemConnectionMemo _memo = null;

    public SerialFilterFrame(CMRISystemConnectionMemo memo) {
        super("CMRInet Packet Filter");
        _memo = memo;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
	    initializeNodes();

        // For the class
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setPreferredSize(new Dimension(620,375));
        // setBackground(Color.LIGHT_GRAY);

        // Set up the node filter enable table
        //------------------------------------
        nodeEnablePanel = new JPanel();
        nodeEnablePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        nodeEnablePanel.setLayout(new BoxLayout(nodeEnablePanel, BoxLayout.LINE_AXIS));
        nodeEnablePanel.setBorder(nodeEableBorderTitled);
        nodeEnablePanel.setBackground(Color.WHITE);

        nodeTableModel = new NodeTableModel();
        nodeTable = new JTable(nodeTableModel);

        nodeTable.setPreferredScrollableViewportSize(new Dimension(200,90));
        nodeTable.setFillsViewportHeight(true);
        nodeTable.setShowGrid(true);
        nodeTable.setGridColor(Color.WHITE);
        nodeTable.setBackground(Color.WHITE);
        nodeTable.setRowSelectionAllowed(false);
        nodeTable.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
        nodeTable.setRowHeight(30);
        nodeTable.getTableHeader().setReorderingAllowed(false);

        // Monitor node enable selection check boxes
        //------------------------------------------
        JScrollPane nodeTableScrollPane = new JScrollPane(nodeTable);
        nodeEnablePanel.add(nodeTableScrollPane,BorderLayout.LINE_START);

        nodeEnablePanel.setBorder(nodeEableBorderTitled);

        TableColumnModel nodeEableColumnModel = nodeTable.getColumnModel();

        DefaultTableCellRenderer dtcen = new DefaultTableCellRenderer();
        dtcen.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer dtlft = new DefaultTableCellRenderer();
        dtlft.setHorizontalAlignment(SwingConstants.LEFT);
        TableCellRenderer rendererFromHeader = nodeTable.getTableHeader().getDefaultRenderer();
        JLabel headerLabel = (JLabel) rendererFromHeader;
        headerLabel.setHorizontalAlignment(JLabel.CENTER);

        TableColumn nodeaddrColumn = nodeEableColumnModel.getColumn(NodeTableModel.NODEADDR_COLUMN);
        nodeaddrColumn.setCellRenderer(dtcen);
        nodeaddrColumn.setResizable(false);

        TableColumn enabledColumn = nodeEableColumnModel.getColumn(NodeTableModel.ENABLED_COLUMN);
        enabledColumn.setResizable(false);

        nodeEnablePanel.setBorder(nodeEableBorderTitled);
        nodeEnablePanel.setBackground(Color.WHITE);
        nodeEnablePanel.setPreferredSize(new Dimension(200,200));

        nodeTable.setAutoCreateRowSorter(true);
        nodeTable.getRowSorter().toggleSortOrder(NodeTableModel.NODEADDR_COLUMN);
        nodeEnablePanel.setVisible(true);

        add(nodeEnablePanel);

        // packet type selection check boxes
        //----------------------------------
        JPanel packetSelectTypes = new JPanel();
        packetSelectTypes.setLayout(new GridLayout(0,2));
        packetSelectTypes.setPreferredSize(new Dimension(400, 200));
        packetSelectTypes.setBorder(packetTypesBorderTitled);
        packetSelectTypes.setBackground(Color.WHITE);

        // Losd the packet types filter check boxes
        //-----------------------------------------
        for (int i=0; i<numMonPkts; i++)
        {
            JCheckBox ckbox = new JCheckBox(packetChkBoxLabels[i]);
            packetChkBoxes.add(ckbox);
            packetChkBoxes.get(i).addItemListener(packetTypeCkBoxHandler);

            packetChkBoxes.get(i).setVisible(false);
            packetChkBoxes.get(i).setEnabled(false);
            packetChkBoxes.get(i).setSelected(false);
            if (i<lastStdPkt)
            {
                packetChkBoxes.get(i).setVisible(true);
                packetChkBoxes.get(i).setEnabled(true);
                packetChkBoxes.get(i).setSelected(true);
            }
            packetSelectTypes.add(ckbox);
        }

        packetSelectTypes.setVisible(true);
        add(packetSelectTypes);

        // node monitor enable button panel
        //---------------------------------
        JPanel enableSelectButtons = new JPanel();
        enableSelectButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        enableSelectButtons.setPreferredSize(new Dimension(200, 60));
        enableSelectButtons.setBackground(Color.WHITE);
        enableSelectButtons.setBorder(enabledBorderTitled);

        enableSelectButtons.add(nodeMonitorAll);
        nodeMonitorAll.setVisible(true);
        nodeMonitorAll.setToolTipText(Bundle.getMessage("AllButtonTip") );
	    nodeMonitorAll.addActionListener(new java.awt.event.ActionListener() {
	        @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                nodeMonitorAllButtonActionPerformed();
            }
        });

        enableSelectButtons.add(nodeMonitorNone);
        nodeMonitorNone.setVisible(true);
        nodeMonitorNone.setToolTipText(Bundle.getMessage("NoneButtonTip") );
	    nodeMonitorNone.addActionListener(new java.awt.event.ActionListener() {
	        @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
					nodeMonitorNoneButtonActionPerformed();
				}
	    });

        add(enableSelectButtons);


        // packet select enable button panel
        //----------------------------------
        JPanel packetSelectButtons = new JPanel();
        packetSelectButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
        packetSelectButtons.setPreferredSize(new Dimension(400, 60));
        packetSelectButtons.setBackground(Color.WHITE);
        packetSelectButtons.setBorder(packetSelectBorderTitled);

        packetSelectButtons.add(packetMonitorAll);
        packetMonitorAll.setVisible(true);
        packetMonitorAll.setToolTipText(Bundle.getMessage("AllButtonTip") );
	    packetMonitorAll.addActionListener(new java.awt.event.ActionListener() {
	        @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
					packetMonitorAllButtonActionPerformed();
				}
	    });

        packetSelectButtons.add(packetMonitorNone);
        packetMonitorNone.setVisible(true);
        packetMonitorNone.setToolTipText(Bundle.getMessage("NoneButtonTip") );
	    packetMonitorNone.addActionListener(new java.awt.event.ActionListener() {
	        @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
					packetMonitorNoneButtonActionPerformed();
				}
	    });

        add(packetSelectButtons);

        // Main button panel
        //------------------
        JPanel mainButtons = new JPanel();
        mainButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
        mainButtons.setPreferredSize(new Dimension(600, 60));

        haltPollButton.setVisible(true);
        haltPollButton.setToolTipText(Bundle.getMessage("HaltPollButtonTip") );
	    haltPollButton.addActionListener(new java.awt.event.ActionListener() {
	        @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
					haltpollButtonActionPerformed(e);
				}
			});
	    mainButtons.add(haltPollButton);

        mainButtons.add(Box.createRigidArea(new Dimension(30,0)));

        doneButton.setVisible(true);
        doneButton.setToolTipText(Bundle.getMessage("DoneButtonTip") );
	    doneButton.addActionListener(new java.awt.event.ActionListener() {
	        @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
					doneButtonActionPerformed();
				}
			});
	    mainButtons.add(doneButton);
        mainButtons.setVisible(true);
        add(mainButtons);

        addHelpMenu("package.jmri.jmrix.cmri.serial.serialmon.SerialFilterFrame", true);

        // pack for display
        //-----------------
        pack();

    }

    /**
     * Method to initialize configured nodes and set up the node select combo box
     */
    public void initializeNodes() {
    	// get all configured nodes
        SerialNode node = (SerialNode) _memo.getTrafficController().getNode(0);
        int index = 1;
        while (node != null) {
            monitorNode.add(node);
            node = (SerialNode) _memo.getTrafficController().getNode(index);
            index ++;
	    }
    }

    /**
     * Set MonitorNodePackets.
     *
     */
    public void nodeMonitorAllButtonActionPerformed() {
        for(int i=0; i<monitorNode.size(); i++) {
            monitorNode.get(i).setMonitorNodePackets(false);
            nodeTableModel.setValueAt(monitorNode.get(i).getMonitorNodePackets(), i, NodeTableModel.ENABLED_COLUMN);
        }
    }

    public void nodeMonitorNoneButtonActionPerformed() {
        for(int i=0; i<monitorNode.size(); i++) {
            monitorNode.get(i).setMonitorNodePackets(true);
            nodeTableModel.setValueAt(monitorNode.get(i).getMonitorNodePackets(), i, NodeTableModel.ENABLED_COLUMN);
        }
    }

    public void packetMonitorAllButtonActionPerformed() {
        for (int i=0; i<lastStdPkt; i++) {
            packetChkBoxes.get(i).setSelected(true);
        }
    }

    public void packetMonitorNoneButtonActionPerformed() {
        for (int i=0; i<lastStdPkt; i++) {
            packetChkBoxes.get(i).setSelected(false);
        }
    }

    public void doneButtonActionPerformed() {
        setVisible(false);
        dispose();
    }

    /* ---------------------------- */
    /* Halt Poll button handler */
    /* ---------------------------- */
    public void haltpollButtonActionPerformed(ActionEvent e) {
        SerialTrafficController stc = _memo.getTrafficController();
        stc.setPollNetwork(!stc.getPollNetwork());
        if (stc.getPollNetwork()) {
            haltPollButton.setText(Bundle.getMessage("HaltPollingText"));
        } else {
            haltPollButton.setText(Bundle.getMessage("StartPollingText"));
        }
    }

    // -------------------------------
    // cpNode options checkbox handler
    // -------------------------------
    private class HandlerClass implements ItemListener{
        @Override
        public void itemStateChanged(ItemEvent e){
            JCheckBox pktTypeChkBox = (JCheckBox) e.getSource();
            int pktTypeIndex = 0;
            SerialNode aNode = null;
            do {
                if (pktTypeChkBox == packetChkBoxes.get(pktTypeIndex)) {
                    for (int i=0; i < monitorNode.size(); i++) {
                        aNode = monitorNode.get(i);
                        if (aNode != null) {
                            aNode.setMonitorPacketBit(pktTypeIndex,(packetChkBoxes.get(pktTypeIndex).isSelected() ? true : false));
                        }
                    }
                    return;
                }
                pktTypeIndex++;
            } while ( pktTypeIndex < numMonPkts);
        }
    }
    /**
     * Set up table for displaying bit assignments
     */
    public class NodeTableModel extends AbstractTableModel {
        @Override
        public String getColumnName(int c) {
            return nodeEnableColumnsNames[c];
        }
        @Override
        public Class<?> getColumnClass(int c) {
            switch (c) {
                case NODEADDR_COLUMN:
                    return Integer.class;
                default:
                    return Boolean.class;
            }
        }
    
        @Override
	    public boolean isCellEditable(int r,int c) {
           if (c!=NODEADDR_COLUMN) return true;
           else return false;
        }
        @Override
        public int getColumnCount () {return NUMCOLUMNS;}
        @Override
        public int getRowCount () {return monitorNode.size();}
        @Override
        public Object getValueAt (int r,int c)
        {

            if (c==NODEADDR_COLUMN) {
                 return monitorNode.get(r).getNodeAddress();
            } else if (c==ENABLED_COLUMN) {
             if (monitorNode.get(r).getMonitorNodePackets())
              return true;
             else
              return false;
            } else {
             return true;
            }
        }

        @Override
	    public void setValueAt(Object value, int r, int c) {
            if (c==NODEADDR_COLUMN) {
                monitorNode.get(r).setPollListPosition((Integer)value);
            }
            else if (c == ENABLED_COLUMN) {
                monitorNode.get(r).setMonitorNodePackets(!monitorNode.get(r).getMonitorNodePackets());
            }
            fireTableDataChanged();
        }

        public static final int NODEADDR_COLUMN = 0;
        public static final int ENABLED_COLUMN  = 1;
        public static final int NUMCOLUMNS = ENABLED_COLUMN+1;
    }

    private String[] nodeEnableColumnsNames = {"Node","Monitor"};

    // private final static Logger log = LoggerFactory.getLogger(SerialFilterFrame.class);

}
