package jmri.jmrix.cmri.serial.cmrinetmanager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.serial.serialmon.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for a table view to manage a CMRInet network.
 * @author	 Chuck Catania   Copyright (C) 2014, 2015, 2016, 2017
 */
public class CMRInetManagerFrame extends jmri.util.JmriJFrame {

    ArrayList<SerialNode> cmriNode = new  ArrayList<SerialNode>();  //c2

    // poll list node information
    protected boolean inputSelected = false;  // true if displaying input assignments, false for output
    protected SerialNode selNode = null;
    protected String selNodeID = "x"; // text address of selected Node
    public int selNodeNum = 0;  // Address (ua) of selected Node

    protected int selectedNodeAddr = -1;  //c2

    // node select pane items
    JLabel nodeLabel = new JLabel(Bundle.getMessage("NodeBoxLabel")+" ");
    JComboBox nodeSelBox = new JComboBox();

    JLabel nodeNameText = new JLabel(Bundle.getMessage("NodeBoxLabel"));  //c2 rb.getString("NodeBoxLabel");
    JComboBox editBox = new JComboBox();

    // network controls panel
    protected JPanel networkControlsPanel = null;

    // node table pane items
    protected JPanel pollListPanel = null;
    protected Border pollListBorder = BorderFactory.createEtchedBorder();

    protected JTable nodeTable = null;
    protected TableModel nodeTableModel = null;

    protected JPanel CMRInetParams = new JPanel();

    // button pane items
    JButton monitorButton = new JButton(Bundle.getMessage("MonitorButtonText") );
    JButton doneButton = new JButton(Bundle.getMessage("DoneButtonText") );
    JButton haltPollButton = new JButton(Bundle.getMessage("HaltPollButtonText") );
    JButton netStatsButton = new JButton(Bundle.getMessage("NetStatsButtonText") );

    private CMRISystemConnectionMemo _memo = null;

    public CMRInetManagerFrame(CMRISystemConnectionMemo memo) {
        super();
	    _memo = memo;
        //addHelpMenu("package.jmri.jmrix.cmri.serial.cmrinetmanager.CMRInetManagerFrame", true); // duplicate, see initComponents()
    }

    protected javax.swing.JTextField pollIntervalField = new javax.swing.JTextField();

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
	    initializeNodes();

        // set the frame's initial state
        setTitle(Bundle.getMessage("WindowTitle") + " - Connection "+_memo.getUserName());
        setSize(1200,300);

        Container contentPane = getContentPane();
        contentPane.setLayout(new FlowLayout());

        // Set up the poll list panel
        pollListPanel = new JPanel();
        pollListPanel.setLayout(new BoxLayout(pollListPanel, BoxLayout.Y_AXIS));

        nodeTableModel = new NodeTableModel();
        nodeTable = new JTable(nodeTableModel);

        nodeTable.setShowGrid(true);
        nodeTable.setGridColor(Color.black);
        nodeTable.setRowSelectionAllowed(false);
        nodeTable.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
        nodeTable.setPreferredScrollableViewportSize(new java.awt.Dimension(300,350));
        nodeTable.setRowHeight(30);
        nodeTable.getTableHeader().setReorderingAllowed(false);
        TableColumnModel pollListColumnModel = nodeTable.getColumnModel();

        DefaultTableCellRenderer dtcen = new DefaultTableCellRenderer();
        dtcen.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer dtlft = new DefaultTableCellRenderer();
        dtlft.setHorizontalAlignment(SwingConstants.LEFT);

        TableCellRenderer rendererFromHeader = nodeTable.getTableHeader().getDefaultRenderer();
        JLabel headerLabel = (JLabel) rendererFromHeader;
        headerLabel.setHorizontalAlignment(JLabel.CENTER);

        TableColumn pollseqColumn = pollListColumnModel.getColumn(NodeTableModel.POLLSEQ_COLUMN);
        pollseqColumn.setMinWidth(40);
        pollseqColumn.setMaxWidth(80);
        pollseqColumn.setCellRenderer(dtcen);
        pollseqColumn.setResizable(false);

        TableColumn enabledColumn = pollListColumnModel.getColumn(NodeTableModel.ENABLED_COLUMN);
        enabledColumn.setMinWidth(40);
        enabledColumn.setMaxWidth(80);
        enabledColumn.setResizable(false);

        TableColumn nodenumColumn = pollListColumnModel.getColumn(NodeTableModel.NODENUM_COLUMN);
        nodenumColumn.setMinWidth(40);
        nodenumColumn.setMaxWidth(70);
        nodenumColumn.setCellRenderer(dtcen);
        nodenumColumn.setResizable(false);

        TableColumn nodetypeColumn = pollListColumnModel.getColumn(NodeTableModel.NODETYPE_COLUMN);
        nodetypeColumn.setMinWidth(40);
        nodetypeColumn.setMaxWidth(80);
        nodetypeColumn.setCellRenderer(dtcen);
        nodetypeColumn.setResizable(false);

        TableColumn statusColumn = pollListColumnModel.getColumn(NodeTableModel.STATUS_COLUMN);
        statusColumn.setMinWidth(10);
        statusColumn.setMaxWidth(80);
        statusColumn.setCellRenderer(dtcen);
        statusColumn.setResizable(false);

        TableColumn nodedescColumn = pollListColumnModel.getColumn(NodeTableModel.NODEDESC_COLUMN);
        nodedescColumn.setMinWidth(10);
        nodedescColumn.setMaxWidth(350);
        nodedescColumn.setCellRenderer(dtlft);
        nodedescColumn.setResizable(false);

        JScrollPane nodeTableScrollPane = new JScrollPane(nodeTable);

        Border pollListBorderTitled = BorderFactory.createTitledBorder(pollListBorder," ",
                                                                    TitledBorder.LEFT,TitledBorder.ABOVE_TOP);
        pollListPanel.add(nodeTableScrollPane,BorderLayout.EAST);
        pollListPanel.setBorder(pollListBorderTitled);
        // Set the scroll window size
        setPreferredSize(new Dimension(750, 500));

        nodeTable.setAutoCreateRowSorter(true);
        nodeTable.getRowSorter().toggleSortOrder(NodeTableModel.POLLSEQ_COLUMN);

        // Poll list drag and drop handler
        nodeTable.setDragEnabled(true);

        contentPane.add(pollListPanel);

        CMRInetParams.add(haltPollButton);
        contentPane.add(CMRInetParams);

        Container contentPane13 = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane13, BoxLayout.Y_AXIS));

        // --------------------
        // Setup window buttons
        // --------------------
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3,BoxLayout.LINE_AXIS));
        panel3.setPreferredSize(new Dimension(600, 45));

        // --------------------------
        // Set up Halt Polling button
        // --------------------------
        haltPollButton.setVisible(true);
        haltPollButton.setToolTipText(Bundle.getMessage("HaltPollButtonTip") );
	haltPollButton.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
					haltpollButtonActionPerformed(e);
				}
			});
         SerialTrafficController stc = _memo.getTrafficController();
         if (stc.getPollNetwork())
             haltPollButton.setText(Bundle.getMessage("HaltPollButtonText"));
         else
             haltPollButton.setText(Bundle.getMessage("ResumePollButtonText"));
	panel3.add(haltPollButton);

        // --------------------------
        // Set up Open monitor button
        // --------------------------
        monitorButton.setVisible(true);
        monitorButton.setToolTipText(Bundle.getMessage("MonitorButtonTip") );
	monitorButton.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
					monitorButtonActionPerformed(e);
				}
			});
	panel3.add(monitorButton);

        // -----------------------------
        // Set up Network Metrics button
        // -----------------------------
        netStatsButton.setVisible(false);
        netStatsButton.setToolTipText(Bundle.getMessage("NetStatsButtonTip") );
	netStatsButton.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
					netStatsButtonActionPerformed(e);
				}
			});
	panel3.add(netStatsButton);
        panel3.add(Box.createRigidArea(new Dimension(30,0)));

        // ------------------
        // Set up Done button
        // ------------------
        doneButton.setVisible(true);
        doneButton.setToolTipText(Bundle.getMessage("DoneButtonTip") );
	doneButton.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
					doneButtonActionPerformed();
				}
			});
	panel3.add(doneButton);
        contentPane13.add(panel3);

        addHelpMenu("package.jmri.jmrix.cmri.serial.cmrinetmanager.CMRInetManagerFrame", true);

        // pack for display
        pack();

    }

    /**
     * Method to initialize configured nodes and set up the node select combo box
     */
    public void initializeNodes()  //c2
    {
	// get all configured nodes
        SerialNode node = (SerialNode) _memo.getTrafficController().getNode(0);
        int index = 1;
        while (node != null)
        {
            cmriNode.add(node);
            // Set the polling sequence to the ordinal value in the list
            if (cmriNode.get(index-1).getPollListPosition() == 0) {
                cmriNode.get(index-1).setPollListPosition(index);
            }
            node = (SerialNode) _memo.getTrafficController().getNode(index);

            index ++;

	}
    }

    // --------------------------------------------
    // Extract the node address from the node table
    // --------------------------------------------
    public int getSelectedNodeAddr() {
        return (Integer)nodeTable.getValueAt(nodeTable.getSelectedRow(),1);
   }

    // ----------------------------
    // Node browser button handlers
    // ----------------------------
    public void doneButtonActionPerformed() {
        setVisible(false);
        dispose();
    }

    // ------------------------------
    // CMRInet Monitor button handler
    // ------------------------------
    public void monitorButtonActionPerformed(ActionEvent e) {
        SerialMonAction f = new SerialMonAction(_memo);
        try {
                f.actionPerformed(e);
            }
        catch (Exception ex)
            {
                log.info("Exception-C2: "+ex.toString());
            }
    }

    // ---------------------------------
    // CMRInet Statistics button handler
    // ---------------------------------
    public void netStatsButtonActionPerformed(ActionEvent e) {
//        CMRInetMetricsAction f = new CMRInetMetricsAction(_memo);  **********
        try {
//                f.actionPerformed(e);
                netStatsButton.setEnabled(false);
            }
        catch (Exception ex)
            {
                log.info("Exception-C2: "+ex.toString());
            }
    }

    // ------------------------
    // Halt Poll button handler
    // ------------------------
    public void haltpollButtonActionPerformed(ActionEvent e) {
         SerialTrafficController stc = _memo.getTrafficController();
         stc.setPollNetwork(!stc.getPollNetwork());
         if (stc.getPollNetwork())
             haltPollButton.setText(Bundle.getMessage("HaltPollButtonText"));
         else
             haltPollButton.setText(Bundle.getMessage("ResumePollButtonText"));
    }

                /**
     * Set up table for displaying bit assignments
     */
    public class NodeTableModel extends AbstractTableModel
    {
        @Override
        public String getColumnName(int c) {return pollListColumnsNames[c];}
        @Override
        public Class<?> getColumnClass(int c) {
            switch (c) {
                case ENABLED_COLUMN:
                    return Boolean.class;
                case POLLSEQ_COLUMN:
                      return Integer.class;
                case NODENUM_COLUMN:
                    return Integer.class;
                default:
                    return String.class;
            }
        }
        @Override
	public boolean isCellEditable(int r,int c)
        {
            switch (c)
            {
                case ENABLED_COLUMN:
                    return (true);
                default:
            }

            return (false);

        }
        @Override
        public int getColumnCount () {return NUM_COLUMNS;}
        @Override
        public int getRowCount () {return cmriNode.size();}
        @Override
        public Object getValueAt (int r,int c)
        {
          switch(c)
	  {
            case POLLSEQ_COLUMN:
                 return cmriNode.get(r).getPollListPosition();

            case ENABLED_COLUMN:
                if (cmriNode.get(r).getPollingEnabled())
                  return true;
		else
    		  return false;

            case NODENUM_COLUMN:
                return cmriNode.get(r).getNodeAddress();

            case NODETYPE_COLUMN:
                return "  "+nodeTypes[cmriNode.get(r).getNodeType()];

            case STATUS_COLUMN:
                fireTableDataChanged();
                return pollStatus[cmriNode.get(r).getPollStatus()];

            case NODEDESC_COLUMN:
                return " "+cmriNode.get(r).getcmriNodeDesc();

	    default:
	  }
          return "";
        }

        @Override
	public void setValueAt(Object value, int r, int c)
        {
	  switch(c)
	  {
            case POLLSEQ_COLUMN:
                    cmriNode.get(r).setPollListPosition((Integer)value);
                    fireTableDataChanged();

            break;
            case ENABLED_COLUMN:
                cmriNode.get(r).setPollingEnabled(!cmriNode.get(r).getPollingEnabled());
            break;
            case STATUS_COLUMN:
                cmriNode.get(r).setPollStatus(cmriNode.get(r).getPollStatus());
            break;
	    default:
	  }
        }

        public static final int POLLSEQ_COLUMN  = 0;
        public static final int ENABLED_COLUMN  = 1;
        public static final int NODENUM_COLUMN  = 2;
        public static final int NODETYPE_COLUMN = 3;
        public static final int STATUS_COLUMN   = 4;
        public static final int NODEDESC_COLUMN = 5;
        public static final int NUM_COLUMNS = NODEDESC_COLUMN+1;

}

    private String[] pollListColumnsNames =
                    {"Poll Seq","Enabled","Node","Type","Status","Description"};
    private String[] nodeTypes = {"--","SMINI","SUSIC","CPNODE","PiNODE"};
    private String[] pollStatus = {"ERROR","IDLE","POLLING","TIMEOUT","INIT"};

    private final static Logger log = LoggerFactory.getLogger(CMRInetManagerFrame.class);

}
