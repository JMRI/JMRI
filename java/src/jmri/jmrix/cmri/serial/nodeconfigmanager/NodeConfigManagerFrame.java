/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.cmri.serial.nodeconfigmanager;

import apps.AppConfigBase;
import apps.gui3.TabbedPreferences;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.jmrix.cmri.serial.nodeiolist.NodeIOListFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.IOException;

import java.util.ResourceBundle;
import java.util.ArrayList;

import javax.swing.border.Border;
import javax.swing.*;
import javax.swing.table.*;
import java.util.EventListener;
import javax.swing.border.TitledBorder;

import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.jmrix.cmri.serial.SerialSensorManager;

/**
 * Frames for a table view to manage CMRInet node configuration management.  c2
 * Created a table view for node configuration operations.  Add, Edit, Delete
 * and Update are executed from the NodeTableManager.
 * This class was built from the NodeConfig class.
 * 
 * @author	 Bob Jacobsen   Copyright (C) 2004
 * @author	 Dave Duchamp   Copyright (C) 2004
 * @author	 Chuck Catania  Copyright (C) 2013, 2014, 2015, 2016
 * @version	 $Revision: 17977 $
 */

public class NodeConfigManagerFrame extends jmri.util.JmriJFrame {

    ResourceBundle rb  = ResourceBundle.getBundle("jmri.jmrix.cmri.serial.nodeconfigmanager.NodeManagerBundle");
    ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrix.cmri.serial.nodeconfigmanager.NodeConfigManagerBundle");

    ArrayList<SerialNode> cmriNode = new  ArrayList<SerialNode>();  //c2
    public int numConfigNodes = 0;
    
    public int numBits = 48;  // number of bits in assignment table
    public int numInputBits = 24;  // number of input bits for selected node
    public int numOutputBits = 48; // number of output bits for selected node

    protected int selectedNodeAddr = -1;  //c2
    protected int selectedTableRow = -1;  //c2
    protected boolean doingPrint = false;

    // node select pane items
    JLabel nodeLabel = new JLabel(rb.getString("NodeBoxLabel")+" ");

    // node table pane items
    protected JPanel nodeTablePanel = null;
    protected Border inputBorder = BorderFactory.createEtchedBorder();
    protected Border inputBorderTitled = BorderFactory.createTitledBorder(inputBorder,
                                                                    rb.getString("ConfiguredNodes"),
                                                                    TitledBorder.LEFT,TitledBorder.ABOVE_TOP);
    protected NodeTableModel nodeTableModel = null;
    protected JTable nodeTable = null;

    // button pane items
    JButton addButton = new JButton(rb.getString("AddButtonText") );
//    static JButton addButton = new JButton();
    JButton doneButton = new JButton(rb.getString("DoneButtonText") );
    JButton printButton = new JButton(rb.getString("PrintButtonText") );
  
    NodeConfigManagerFrame curFrame;
       
    protected javax.swing.JTextField nodeAddrField = new javax.swing.JTextField(3);
    protected javax.swing.JLabel nodeAddrStatic = new javax.swing.JLabel("000");
    protected javax.swing.JComboBox nodeTypeBox; 
    protected javax.swing.JTextField receiveDelayField = new javax.swing.JTextField(3);
    protected javax.swing.JTextField nodeDescription = new javax.swing.JTextField(32);  //c2
    protected javax.swing.JTextField pulseWidthField = new javax.swing.JTextField(4);
    protected javax.swing.JComboBox cardSizeBox; 
    protected javax.swing.JComboBox cardSize8Box; 
    protected javax.swing.JLabel cardSizeText = new javax.swing.JLabel("   "+rbx.getString("LabelCardSize"));
    protected javax.swing.JLabel onBoardBytesText = new javax.swing.JLabel(rbx.getString("LabelOnBoardBytes"));
	
    protected javax.swing.JButton addNodeButton = new javax.swing.JButton(rbx.getString("ButtonAdd"));
    protected javax.swing.JButton editNodeButton = new javax.swing.JButton(rbx.getString("ButtonEdit"));
    protected javax.swing.JButton deleteNodeButton = new javax.swing.JButton(rbx.getString("ButtonDelete"));
    protected javax.swing.JButton doneNodeButton = new javax.swing.JButton(rbx.getString("ButtonDone"));
    protected javax.swing.JButton updateNodeButton = new javax.swing.JButton(rbx.getString("ButtonUpdate"));
    protected javax.swing.JButton cancelNodeButton = new javax.swing.JButton(rbx.getString("ButtonCancel"));
	
    protected javax.swing.JLabel statusText1 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusText2 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusText3 = new javax.swing.JLabel();		
	
    protected javax.swing.JPanel panel2  = new JPanel();
    protected javax.swing.JPanel panel2a = new JPanel();
    protected javax.swing.JPanel panel2b = new JPanel();  //c2
    protected javax.swing.JPanel panel2c = new JPanel();  //c2
    protected javax.swing.JPanel panelnodeDescBox = new JPanel();   //c2 node desctipion box
    protected javax.swing.JPanel panelnodeDesc= new JPanel();  //c2 node description
    protected javax.swing.JPanel panelnetOpt = new JPanel();  //c2 CMRInet options
    protected javax.swing.JPanel panelnetOptBox = new JPanel();  //c2 CMRInet options frame
    protected javax.swing.JPanel panelnodeOpt = new JPanel();   //c2 node options
	
    private static boolean changedNode = false;  // true if a node was changed, deleted, or added
    protected boolean editMode = false;     // true if in edit mode
	
    protected SerialNode curNode = null;    // Serial Node being editted
    protected int nodeAddress = 0;          // Node address
    protected int nodeType = SerialNode.SMINI; // Node type - default SMINI
    protected int bitsPerCard = 24;         // number of bits per card
    protected int receiveDelay = 0;         // transmission delay
    protected int pulseWidth = 500;         // pulse width for turnout control (milliseconds)
    protected int num2LSearchLights = 0;    // number of 2-lead oscillating searchlights
	
    protected int numCards = 0;             //set by consistency check routine
    protected int cpNodeOnboard = 4;        //Number of fixed bytes(cards) for a cpNode
    protected int PiNodeOnboard = 6;        //Number of fixed bytes(cards) for a PiNode
	
    protected boolean errorInStatus1 = false;
    protected boolean errorInStatus2 = false;
    
    protected String editStatus1 = rbx.getString("NotesEdit1");
    protected String editStatus2 = rbx.getString("NotesEdit2");
    protected String editStatus3 = rbx.getString("NotesEdit3");
    protected String addStatus1 = rbx.getString("NotesAdd1");
    protected String addStatus2 = rbx.getString("NotesAdd2");
    protected String addStatus3 = rbx.getString("NotesAdd3");
    protected String delStatus1 = rbx.getString("NotesDel1");
    protected String delStatus2 = rbx.getString("NotesDel2");
    protected String delStatus3 = rbx.getString("NotesDel3");
    
    protected String nodeDescText = "";
    protected int deleteNodeAddress = 0;
    
    HandlerClass nodeOptHandler = new HandlerClass();
    
    // --------------------------
    // CMRInet Options CheckBoxes
    // --------------------------
    protected JCheckBox cbx_cmrinetopt_AUTOPOLL = new JCheckBox(rbx.getString("cmrinetOpt0"));
    protected JCheckBox cbx_cmrinetopt_USECMRIX = new JCheckBox(rbx.getString("cmrinetOpt1"));
    protected JCheckBox cbx_cmrinetopt_USEBCC = new JCheckBox(rbx.getString("cmrinetOpt2"));
    protected JCheckBox cbx_cmrinetopt_BIT8 = new JCheckBox(rbx.getString("cmrinetOpt8"));
    protected JCheckBox cbx_cmrinetopt_BIT15 = new JCheckBox(rbx.getString("cmrinetOpt15"));
	
    // -------------------------
    // cpNode Options CheckBoxes
    // -------------------------
    protected JCheckBox cbx_cpnodeopt_SENDEOT = new JCheckBox(rbx.getString("cpnodeOpt0"));
    protected JCheckBox cbx_cpnodeopt_BIT1 = new JCheckBox(rbx.getString("cpnodeOpt1")); 
    protected JCheckBox cbx_cpnodeopt_BIT2 = new JCheckBox(rbx.getString("cpnodeOpt2"));
    protected JCheckBox cbx_cpnodeopt_BIT8 = new JCheckBox(rbx.getString("cpnodeOpt8"));
    protected JCheckBox cbx_cpnodeopt_BIT15 = new JCheckBox(rbx.getString("cpnodeOpt15"));

    
    public NodeConfigManagerFrame() {
        super();
        curFrame = this;
//        prepareListeners();
    }
/*
   public void prepareListeners(){
      addWindowListener(new WindowAdapter() {
         public void windowClosed(WindowEvent windowEvent){
            addButton.setEnabled(true);
         }        
      });    
      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent windowEvent){
            addButton.setEnabled(true);
         }        
      });     
   }
*/ 
    public void initComponents() throws Exception
    {
        // set the frame's initial state
        setTitle(rb.getString("WindowTitle"));
        setSize(700,150);// 500,150

        Container contentPane = getContentPane();        
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
                
        // load the SerialNode data
	initializeNodes();
        
        // Set up the assignment panel
        nodeTablePanel = new JPanel();
        nodeTablePanel.setLayout(new BoxLayout(nodeTablePanel, BoxLayout.Y_AXIS));
        
        nodeTableModel = new NodeTableModel(); 
        nodeTable = new JTable(nodeTableModel);
        
        nodeTable.setShowGrid(true);
        nodeTable.setGridColor(Color.black);
        nodeTable.setRowSelectionAllowed(false);
        nodeTable.setFont(new Font("Arial", Font.PLAIN, 14));
        nodeTable.setRowHeight(30);
        nodeTable.getTableHeader().setReorderingAllowed(false);
        nodeTable.setPreferredScrollableViewportSize(new java.awt.Dimension(400,350));  //300,350                      
        TableColumnModel assignmentColumnModel = nodeTable.getColumnModel();

        DefaultTableCellRenderer dtcen = new DefaultTableCellRenderer();  
        dtcen.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer dtrt = new DefaultTableCellRenderer();  
        dtrt.setHorizontalAlignment(SwingConstants.RIGHT);

        TableCellRenderer rendererFromHeader = nodeTable.getTableHeader().getDefaultRenderer();
        JLabel headerLabel = (JLabel) rendererFromHeader;
        headerLabel.setHorizontalAlignment(JLabel.CENTER);
        headerLabel.setBackground(Color.LIGHT_GRAY);

        TableColumn nodenumColumn = assignmentColumnModel.getColumn(NodeTableModel.NODENUM_COLUMN);
        nodenumColumn.setMinWidth(40);
        nodenumColumn.setMaxWidth(80);
        nodenumColumn.setCellRenderer(dtcen);  
        nodenumColumn.setResizable(false);

        TableColumn nodetypeColumn = assignmentColumnModel.getColumn(NodeTableModel.NODETYPE_COLUMN);
        nodetypeColumn.setMinWidth(40);
        nodetypeColumn.setMaxWidth(80);
        nodetypeColumn.setCellRenderer(dtcen);  
        nodetypeColumn.setResizable(false);

        TableColumn numbitsColumn = assignmentColumnModel.getColumn(NodeTableModel.NUMBITS_COLUMN);
        numbitsColumn.setMinWidth(40);
        numbitsColumn.setMaxWidth(90);
        numbitsColumn.setCellRenderer(dtcen);
        numbitsColumn.setResizable(false);

        TableColumn numinputsColumn = assignmentColumnModel.getColumn(NodeTableModel.NUMINCARDS_COLUMN);
        numinputsColumn.setMinWidth(40);
        numinputsColumn.setMaxWidth(80);			
        numinputsColumn.setCellRenderer(dtcen);  
        numinputsColumn.setResizable(false);

        TableColumn numoutputsColumn = assignmentColumnModel.getColumn(NodeTableModel.NUMOUTCARDS_COLUMN);
        numoutputsColumn.setMinWidth(10);
        numoutputsColumn.setMaxWidth(80);
        numoutputsColumn.setCellRenderer(dtcen);  
        numoutputsColumn.setResizable(false);

        TableColumn numinbytesColumn = assignmentColumnModel.getColumn(NodeTableModel.NUMINBYTES_COLUMN);
        numinbytesColumn.setMinWidth(10);
        numinbytesColumn.setMaxWidth(80);
        numinbytesColumn.setCellRenderer(dtcen);  
        numinbytesColumn.setResizable(false);

        TableColumn numoutbytesColumn = assignmentColumnModel.getColumn(NodeTableModel.NUMOUTBYTES_COLUMN);
        numoutbytesColumn.setMinWidth(10);
        numoutbytesColumn.setMaxWidth(80);
        numoutbytesColumn.setCellRenderer(dtcen);  
        numoutbytesColumn.setResizable(false);

        TableColumn selectColumn = assignmentColumnModel.getColumn(NodeTableModel.SELECT_COLUMN);
        JComboBox comboBox = new JComboBox();
        comboBox.addItem("Select");
        comboBox.addItem("Edit");
        comboBox.addItem("Bit Assignments");
        comboBox.addItem("Delete");
        selectColumn.setCellEditor(new DefaultCellEditor(comboBox));

        selectColumn.setMinWidth(40);
        selectColumn.setMaxWidth(90);
        selectColumn.setCellRenderer(dtcen);  
        selectColumn.setResizable(false);

        TableColumn nodedescColumn = assignmentColumnModel.getColumn(NodeTableModel.NODEDESC_COLUMN);
        nodedescColumn.setMinWidth(40);
        nodedescColumn.setMaxWidth(400); //350
        nodedescColumn.setResizable(true);
        JScrollPane nodeTableScrollPane = new JScrollPane(nodeTable);

        nodeTablePanel.add(nodeTableScrollPane,BorderLayout.CENTER);
        nodeTablePanel.setBorder(inputBorderTitled);
        setPreferredSize(new Dimension(950, 550));

        nodeTable.setAutoCreateRowSorter(true);
        nodeTable.getRowSorter().toggleSortOrder(NodeTableModel.NODENUM_COLUMN);
        
        contentPane.add(nodeTablePanel);

        /**
         * -------------------------
         * Setup main window buttons
         * -------------------------
         */
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3,FlowLayout.RIGHT));
        panel3.setPreferredSize(new Dimension(950, 50));

        /**
         * -----------------
         * Set up Add button
         * -----------------
         */
//        addButton.setText(rb.getString("AddButtonText"));
        addButton.setVisible(true);
        addButton.setToolTipText(rb.getString("AddButtonTip") );
        addButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent e) {
					addButtonActionPerformed(e);
//              addButton.setEnabled(false);
            }


	});
	panel3.add(addButton);
        
        /**
         * -------------------
         * Set up Print button
         * -------------------
         */
        printButton.setVisible(true);
        printButton.setToolTipText(rb.getString("PrintButtonTip") );
	if (numConfigNodes > 0) {
                printButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
					printButtonActionPerformed(e);
				}
			});
	}
	panel3.add(printButton);

        /**
         * ------------------
         * Set up Done button
         * ------------------
         */
        doneButton.setVisible(true);
        doneButton.setToolTipText(rb.getString("DoneButtonTip") );
	doneButton.addActionListener(new java.awt.event.ActionListener()
        {
                public void actionPerformed(java.awt.event.ActionEvent e) {
					doneButtonActionPerformed();
				}

	});        
	panel3.add(doneButton);
        
        contentPane.add(panel3);
        addHelpMenu("package.jmri.jmrix.cmri.serial.nodeconfigmanager.NodeConfigManagerFrame", true);
        // pack for display
        pack();
        nodeTablePanel.setVisible(true);
    }

    /** 
     * --------------------------------------------
     * Extract the node address from the node table
     * --------------------------------------------
     */
    public int getSelectedNodeAddr() {
        return (Integer)nodeTable.getValueAt(nodeTable.getSelectedRow(),0);
    }

    /**
     * ------------------------------------------------------------------
     * Node browser button handlers
     * The reminder to save changes is only done at the main window close
     * ------------------------------------------------------------------ 
     */
    public void doneButtonActionPerformed() {
        if(changedNode)
        {
             JOptionPane.showMessageDialog(this,
             rbx.getString("Reminder1")+"\n"+rbx.getString("Reminder2"),
             rbx.getString("ReminderTitle"),
             JOptionPane.INFORMATION_MESSAGE);
        }
      changedNode = false;
      setVisible(false);
      dispose();
    }

    public void addButtonActionPerformed(ActionEvent e) {
        NodeConfigManagerFrame f = new NodeConfigManagerFrame();
        try {
                f.initNodeConfigWindow();
            }
        catch (Exception ex) {
            log.info("addButtonActionPerformed Exception-C2: "+ex.toString());
            }
        f.nodeTableModel = nodeTableModel;
        f.InitNodeVariables();
        f.buttonSet_ADD();
        f.setLocation(100,100);
        f.setVisible(true);
    }
    
   /**
    * ---------------
    *   Print Button 
    *  --------------
    */
   public void printButtonActionPerformed(java.awt.event.ActionEvent e) {
        int[] colWidth = new int[10];
	// initialize column widths
        TableColumnModel nodeTableColumnModel = nodeTable.getColumnModel();
        colWidth[0] = nodeTableColumnModel.getColumn(NodeTableModel.NODENUM_COLUMN).getWidth();
        colWidth[1] = nodeTableColumnModel.getColumn(NodeTableModel.NODETYPE_COLUMN).getWidth();
        colWidth[2] = nodeTableColumnModel.getColumn(NodeTableModel.NUMBITS_COLUMN).getWidth();
        colWidth[3] = nodeTableColumnModel.getColumn(NodeTableModel.NUMINCARDS_COLUMN).getWidth();
        colWidth[4] = nodeTableColumnModel.getColumn(NodeTableModel.NUMOUTCARDS_COLUMN).getWidth();
        colWidth[5] = nodeTableColumnModel.getColumn(NodeTableModel.SELECT_COLUMN).getWidth();
        colWidth[6] = nodeTableColumnModel.getColumn(NodeTableModel.NUMBITS_COLUMN).getWidth();
        colWidth[7] = nodeTableColumnModel.getColumn(NodeTableModel.NUMBITS_COLUMN).getWidth();
        colWidth[8] = nodeTableColumnModel.getColumn(NodeTableModel.NUMBITS_COLUMN).getWidth();
        colWidth[9] = nodeTableColumnModel.getColumn(NodeTableModel.NUMBITS_COLUMN).getWidth();
        
	// set up a page title
	String head = "CMRInet Node Table";
	// initialize a printer writer
        HardcopyWriter writer = null;
        try {
                writer = new HardcopyWriter(curFrame, head, 10, .8, .5, .5, .5, false);
            } catch (HardcopyWriter.PrintCanceledException ex) {
	return;
		}
	writer.increaseLineSpacing(20);
		// print the assignments
	((NodeTableModel)nodeTableModel).printTable(writer,colWidth);
   }


    /**
     * ---------------------------------
     * Node table SELECT button handlers
     * ---------------------------------
     */
     
    /** --------
     *   EDIT 
     *  --------
     */ 
    public void editActionSelected() {
        selectedNodeAddr = getSelectedNodeAddr();
        
        NodeConfigManagerFrame f = new NodeConfigManagerFrame();
        f.nodeTableModel = nodeTableModel; 
        f.selectedTableRow = nodeTable.convertRowIndexToModel(nodeTable.getSelectedRow());

        try {
                f.initNodeConfigWindow();
                f.editNodeButtonActionPerformed(selectedNodeAddr);
            }
        catch (Exception ex) {
            log.info("editActionSelected Exception-C2: "+ex.toString());
            
            }
        f.setLocation(200,200);
        f.buttonSet_EDIT();
        f.setVisible(true);
    }

    /* ----------
     *   DELETE   
     * ----------
     */
    public void deleteActionSelected() {
        selectedNodeAddr = getSelectedNodeAddr();
        
        NodeConfigManagerFrame f = new NodeConfigManagerFrame();
        f.nodeTableModel = nodeTableModel; 
        f.selectedTableRow = nodeTable.convertRowIndexToModel(nodeTable.getSelectedRow());
        try {
                f.initNodeConfigWindow();
                f.deleteNodeButtonActionPerformed(selectedNodeAddr);
            }
        catch (Exception ex) {
            log.info("deleteActionSelected Exception-C2: "+ex.toString());
            
            }
        f.setLocation(200,200);
        f.buttonSet_DELETE();
        f.setVisible(true);
    }

    /**
     * ------------
     * I/O BIT INFO
     * ------------
     */
    public void infoActionSelected() {
        selectedNodeAddr = getSelectedNodeAddr();
        
        NodeIOListFrame f = new NodeIOListFrame();
        try {
                f.initComponents();
                f.displayNodeIOBits(selectedNodeAddr);
            }
        catch (Exception ex) {
            log.info("infoActionSelected Exception-C2: "+ex.toString());
            
            }
        f.setLocation(100,100);
        f.setVisible(true);
    }
    
    /**
     * Method to initialize configured nodes and sets up the node select combo box
     */         
    public void initializeNodes()
    {
	String str = "";
        
    // get all configured nodes
        if (!cmriNode.isEmpty())
        {
            cmriNode.clear();
        }
            
	SerialNode node = (SerialNode) SerialTrafficController.instance().getNode(0);
        int index = 1;
        while (node != null)
        {
            cmriNode.add(node);
            node = (SerialNode) SerialTrafficController.instance().getNode(index);
            index ++;                        
	}
        
        numConfigNodes = cmriNode.size();
    }
  
    /**
     * Set up table for displaying bit assignments
     */
    public class NodeTableModel extends AbstractTableModel  //c2
    {
        public String getColumnName(int c) {
            return nodeTableColumnsNames[c];
        }
        public Class<?> getColumnClass(int c) {
            switch (c) {
                case NODENUM_COLUMN:    
                return Integer.class;
                
                case NODETYPE_COLUMN:    
                return String.class; 
                    
                case NUMBITS_COLUMN:
                case NUMINCARDS_COLUMN:
                case NUMOUTCARDS_COLUMN:
                case NUMINBYTES_COLUMN:
                case NUMOUTBYTES_COLUMN:
                return Integer.class;
                    
                case SELECT_COLUMN:
                return String.class;
                    
                case NODEDESC_COLUMN:
                default:
                return String.class;
            }
        };
	public boolean isCellEditable(int r,int c) 
        {
            if (c==SELECT_COLUMN)
             return true;
            else
             return false;
        }
        public int getColumnCount () {return NUM_COLUMNS;}
        public int getRowCount () {return cmriNode.size();}
        public void removeRow(int row){
            cmriNode.remove(row);
            numConfigNodes = cmriNode.size();
            fireTableRowsDeleted(row, row);
        }
        public void addRow(SerialNode newNode) {
            cmriNode.add(newNode);
            numConfigNodes = cmriNode.size();
            fireTableDataChanged();
        }
        public void changeRow(int row, SerialNode aNode) {
            cmriNode.set(row, aNode);
            fireTableDataChanged();
        }
	public void setValueAt(Object value, int row, int col) {
            if (col == SELECT_COLUMN) {
               if ( rb.getString("ButtonEdit").equals(value) )
               {
                 editActionSelected();
               } 
               else 
                if (rb.getString("ButtonInfo").equals(value) )
                {
                  infoActionSelected();
                } 
               else
                if (rb.getString("ButtonDelete").equals(value) )
                {
                  deleteActionSelected();
                } 
            } else log.info("setValueAt Row"+row+" value "+value);
            fireTableDataChanged();
        }
        public Object getValueAt (int r,int c) {
	    switch(c)
            {
              case NODENUM_COLUMN:
                if (!doingPrint) 
                 return cmriNode.get(r).getNodeAddress();
                else
                 return Integer.toString(cmriNode.get(r).getNodeAddress());
              
              case NODETYPE_COLUMN:
                return "  "+nodeTableTypes[cmriNode.get(r).getNodeType()];

              case NUMBITS_COLUMN:
                return Integer.toString(cmriNode.get(r).getNumBitsPerCard());
             
              case NUMINCARDS_COLUMN:
                return Integer.toString(cmriNode.get(r).numInputCards());

              case NUMOUTCARDS_COLUMN:
                return Integer.toString(cmriNode.get(r).numOutputCards());

              case NUMINBYTES_COLUMN:
                return Integer.toString((cmriNode.get(r).getNumBitsPerCard()/8)*cmriNode.get(r).numInputCards());

              case NUMOUTBYTES_COLUMN:
                return Integer.toString((cmriNode.get(r).getNumBitsPerCard()/8)*cmriNode.get(r).numOutputCards());

              case SELECT_COLUMN:

                return "Select";
              case NODEDESC_COLUMN:

                return " "+cmriNode.get(r).getcmriNodeDesc();
              default:
                return "";
            }
        }

        public static final int NODENUM_COLUMN = 0;
        public static final int NODETYPE_COLUMN = 1;
        public static final int NUMBITS_COLUMN = 2;
        public static final int NUMINCARDS_COLUMN = 3;
        public static final int NUMOUTCARDS_COLUMN = 4;
        public static final int NUMINBYTES_COLUMN = 5;
        public static final int NUMOUTBYTES_COLUMN = 6;
        public static final int SELECT_COLUMN = 7;
        public static final int NODEDESC_COLUMN = 8;
        public static final int NUM_COLUMNS = NODEDESC_COLUMN+1;
        
        private String[] pollStatus = {"ERROR","IDLE","POLLING","TIMEOUT","SLOW POLL"};

        /**
        * Method to print or print preview the assignment table.
        * Printed in proportionately sized columns across the page with headings and
        * vertical lines between each column. Data is word wrapped within a column.
        * Can only handle 4 columns of data as strings.
        * Adapted from routines in BeanTableDataModel.java by Bob Jacobsen and Dennis Miller
        */
        public void printTable(HardcopyWriter w,int colWidth[]) {
            // determine the column sizes - proportionately sized, with space between for lines
            int[] columnSize = new int[NUM_COLUMNS];
            int charPerLine = w.getCharactersPerLine();
            int tableLineWidth = 0;  // table line width in characters
            int totalColWidth = 0;
            
            doingPrint = true;
            for (int j = 0; j < NUM_COLUMNS; j++)
              if(j!=SELECT_COLUMN) {
                  totalColWidth += colWidth[j];
            }
            float ratio = ((float)charPerLine)/((float)totalColWidth);
            for (int j = 0; j < NUM_COLUMNS; j++) 
              if(j!=SELECT_COLUMN) { 
                  columnSize[j] = ((int)(colWidth[j]*ratio)) - 1;
                  tableLineWidth += (columnSize[j] + 1);
            }

            // Draw horizontal dividing line
            w.write(w.getCurrentLineNumber(),0,w.getCurrentLineNumber(),tableLineWidth);

            // print the column header labels
            String[] columnStrings = new String[NUM_COLUMNS];
            // Put each column header in the array
            for (int i = 0; i < NUM_COLUMNS; i++)
              if(i!=SELECT_COLUMN) {
                  if (i!=SELECT_COLUMN) columnStrings[i] = this.getColumnName(i);
            }
  //          w.setFontStyle(Font.BOLD);
            printColumns(w,columnStrings,columnSize);
            w.setFontStyle(0);
            
            // draw horizontal line
            w.write(w.getCurrentLineNumber(),0,w.getCurrentLineNumber(),tableLineWidth);
            // now print each row of data
            String[] spaces = new String[NUM_COLUMNS];
            // create base strings the width of each of the columns
            for (int k = 0; k < NUM_COLUMNS; k++)
              if( k!=SELECT_COLUMN) {
               spaces[k] = "";
               for (int i = 0; i < columnSize[k]; i++)
                 { 
                     spaces[k] = spaces[k] + " ";
                 }
            }
            for (int i = 0; i < this.getRowCount(); i++)
            {
             for (int j = 0; j < NUM_COLUMNS; j++) if(j!=SELECT_COLUMN) {
                //check for special, null contents
                if (this.getValueAt(i, j) == null) {
                        columnStrings[j] = spaces[j];
                }
//                else if (j==0) {columnStrings[j] = "  "+Integer.toString(configNodeAddresses[i]);
//                }
//                else if (j>0) {columnStrings[j] = "  "+(String)this.getValueAt(i, j);
                else {columnStrings[j] = (String)this.getValueAt(i, j);
                }
              }
              printColumns(w, columnStrings, columnSize);
              // draw horizontal line
              w.write(w.getCurrentLineNumber(),0,w.getCurrentLineNumber(),tableLineWidth);
            }  
            doingPrint=false;
            w.close();
        }
    
        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION") 
        // Only used occasionally, so inefficient String processing not really a problem
        // though it would be good to fix it if you're working in this area
        protected void printColumns(HardcopyWriter w, String columnStrings[], int columnSize[]) 
        {
            String columnString = "";
            String lineString = "";
            String[] spaces = new String[NUM_COLUMNS];
            // create base strings the width of each of the columns
            for (int k = 0; k < NUM_COLUMNS; k++) 
              if(k!=SELECT_COLUMN) {
                    spaces[k] = "";
                    for (int i = 0; i < columnSize[k]; i++) {spaces[k] = spaces[k] + " ";
                    }
            }
            // loop through each column
            boolean complete = false;
            while (!complete){
            complete = true;
            for (int i = 0; i < NUM_COLUMNS; i++) if (i!=SELECT_COLUMN) {
                // if the column string is too wide cut it at word boundary (valid delimiters are space, - and _)
                // use the initial part of the text,pad it with spaces and place the remainder back in the array
                // for further processing on next line
                // if column string isn't too wide, pad it to column width with spaces if needed
                if (columnStrings[i].length() > columnSize[i]) {
                    // this column string will not fit on one line
                    boolean noWord = true;
                    for (int k = columnSize[i]; k >= 1 ; k--) {
                        if (columnStrings[i].substring(k-1,k).equals(" ") 
                                        || columnStrings[i].substring(k-1,k).equals("-")
                                        || columnStrings[i].substring(k-1,k).equals("_")) {
                                columnString = columnStrings[i].substring(0,k) 
                                                + spaces[i].substring(columnStrings[i].substring(0,k).length());
                                columnStrings[i] = columnStrings[i].substring(k);
                                noWord = false;
                                complete = false;
                                break;
                            }
                    }
                    if (noWord) {
                            columnString = columnStrings[i].substring(0,columnSize[i]);
                            columnStrings[i] = columnStrings[i].substring(columnSize[i]);
                            complete = false;
                    }                    
                }	
                else {
                    // this column string will fit on one line
                    columnString = columnStrings[i] + spaces[i].substring(columnStrings[i].length());
                    columnStrings[i] = "";
                }
                lineString = lineString + columnString + " ";
            }
            try {
                    w.write(lineString);
                    //write vertical dividing lines
                    int iLine = w.getCurrentLineNumber();
                    for (int i = 0, k = 0; i < w.getCharactersPerLine(); k++) if (k!=SELECT_COLUMN){
                        w.write( iLine, i, iLine + 1, i);
                        if (k<NUM_COLUMNS) {
                                i = i+columnSize[k]+1;
                        }
                        else {
                                i = w.getCharactersPerLine();
                        }
                    }
                    lineString = "\n";
                    w.write(lineString);
                    lineString = "";
            } 
            catch (IOException e) { 
                    log.warn("error during printing: "+e);
            }
        }
    }
}

    private String[] nodeTableColumnsNames =
                    {"Address","   Type","Bits per Card","IN Cards","OUT Cards","IN Bytes","OUT Bytes"," ","  Description"};
    private String[] nodeTableTypes = {"--","SMINI","SUSIC","CPNODE","PiNODE"};

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NodeConfigManagerFrame.class.getName());

/**
 * ---------------------------------------------------------
 * ------   Node Configuration Management Routines   ------
 *----------------------------------------------------------
 */

    public void InitNodeVariables() {
//    	super();
        // Clear information arrays
        for (int i = 0; i<64 ; i++) {
            cardType[i] = rbx.getString("CardTypeNone");
        }
        for (int i = 0; i<48 ; i++) {
            searchlightBits[i] = false;
            firstSearchlight[i] = false;
        }
        for (int i = 0; i<SerialNode.NUMCMRINETOPTS ; i++) {
            cmrinetOpts[i] = 0;
         }
        for (int i = 0; i<SerialNode.NUMCPNODEOPTS ; i++) {
            cpnodeOpts[i] = 0;
         }
        nodeDescText = "";
    }
    /** 
     *  Initialize the node configuration window
     *  This window is a template for ADD,EDIT,DELETE node operations
     */
    public void initNodeConfigWindow() {
        setTitle(rbx.getString("WindowTitle"));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        
        // Set up node address and node type
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        JPanel panel11 = new JPanel();
        panel11.setLayout(new FlowLayout());
        panel11.add(new JLabel(rbx.getString("LabelNodeAddress")+" "));
        panel11.add(nodeAddrField);
        nodeAddrField.setToolTipText(rbx.getString("TipNodeAddress"));
        nodeAddrField.setText("-1");
        panel11.add(nodeAddrStatic);
        nodeAddrStatic.setVisible(false);
        panel11.add(new JLabel("   "+rbx.getString("LabelNodeType")+" "));
        nodeTypeBox = new JComboBox();
        panel11.add(nodeTypeBox);
        nodeTypeBox.addItem("SMINI");
        nodeTypeBox.addItem("USIC_SUSIC");
        nodeTypeBox.addItem("CPNODE");       //c2    
        nodeTypeBox.addItem("PiNODE");       //c2    
        
        /**
         *   Here add code for other types of nodes
         */
        
        nodeTypeBox.addActionListener(new java.awt.event.ActionListener() 
	{
            public void actionPerformed(java.awt.event.ActionEvent event)
            {
                    String s = (String)nodeTypeBox.getSelectedItem();
                    
                    if (s.equals("SMINI")) 
                    {
                            panel2.setVisible(false);
                            panel2a.setVisible(true); 
                            panel2b.setVisible(false); 
                            panel2c.setVisible(false); 
                            cardSizeText.setVisible(false); 
                            cardSizeBox.setVisible(false);   
                            cardSize8Box.setVisible(false);
                            panelnodeDescBox.setVisible(true);
                            panelnodeDesc.setVisible(true);
                            panelnetOpt.setVisible(true); 
                            panelnetOptBox.setVisible(true); 
                            panelnodeOpt.setVisible(false);
                            cbx_cmrinetopt_USECMRIX.setEnabled(false);        
                            nodeType = SerialNode.SMINI;
                            
                    }
                    else if (s.equals("USIC_SUSIC")) 
                    {
                            panel2.setVisible(true);
                            panel2a.setVisible(false);
                            panel2b.setVisible(false);
                            panel2c.setVisible(false); 
                            cardSizeText.setVisible(true);
                            cardSizeBox.setVisible(true);
                            cardSize8Box.setVisible(false);
                            panelnodeDescBox.setVisible(true);
                            panelnodeDesc.setVisible(true);
                            panelnetOpt.setVisible(true);
                            panelnetOptBox.setVisible(true);
                            panelnodeOpt.setVisible(false);
                            cbx_cmrinetopt_USECMRIX.setEnabled(false);        
                            nodeType = SerialNode.USIC_SUSIC;
                    }
                    else if (s.equals("CPNODE"))   //c2
                    {
                            panel2.setVisible(false);
                            panel2a.setVisible(false);
                            panel2b.setVisible(true);
                            panel2c.setVisible(true); 
                            cardSizeText.setVisible(true);
                            cardSizeBox.setVisible(false);
                            cardSize8Box.setVisible(true);
                            panelnodeDescBox.setVisible(true);
                            panelnodeDesc.setVisible(true);
                            panelnetOpt.setVisible(true);
                            panelnetOptBox.setVisible(true);
                            panelnodeOpt.setVisible(true);
                            cbx_cmrinetopt_USECMRIX.setEnabled(true);        
                            nodeType = SerialNode.CPNODE;
                            onBoardBytesText.setText(rbx.getString("LabelOnBoardBytes")+" 2 Bytes");			
                   }
                    else if (s.equals("PiNODE"))   //c2
                    {
                            panel2.setVisible(false);
                            panel2a.setVisible(false);
                            panel2b.setVisible(true);
                            panel2c.setVisible(true); 
                            cardSizeText.setVisible(true);
                            cardSizeBox.setVisible(false);
                            cardSize8Box.setVisible(true);
                            panelnodeDescBox.setVisible(true);
                            panelnodeDesc.setVisible(true);
                            panelnetOpt.setVisible(true);
                            panelnetOptBox.setVisible(true);
                            panelnodeOpt.setVisible(true);
                            cbx_cmrinetopt_USECMRIX.setEnabled(true);        
                            nodeType = SerialNode.PINODE;
                            onBoardBytesText.setText(rbx.getString("LabelOnBoardBytes")+" 3 Bytes");			
                    }
             /**
              *   Here add code for other types of nodes
              */
        
                    
             /**
              * reset notes as appropriate
              */
              resetNotes();
            }
	});
        nodeTypeBox.setToolTipText(rbx.getString("TipNodeType"));        
        JPanel panel12 = new JPanel();
        panel12.setLayout(new FlowLayout());
        panel12.add(new JLabel(rbx.getString("LabelDelay")+" "));
        panel12.add(receiveDelayField);
        receiveDelayField.setToolTipText(rbx.getString("TipDelay"));
        receiveDelayField.setText("0");
        panel12.add(cardSizeText);
        cardSizeBox = new JComboBox();
        cardSize8Box = new JComboBox();
        panel12.add(cardSizeBox);
        panel12.add(cardSize8Box);
        cardSizeBox.addItem(rbx.getString("CardSize24"));
        cardSizeBox.addItem(rbx.getString("CardSize32"));
        cardSize8Box.addItem(rbx.getString("CardSize8"));
       /**
        *   Here add code for other types of nodes
        */
        
        cardSizeBox.addActionListener(new java.awt.event.ActionListener() 
	{
            public void actionPerformed(java.awt.event.ActionEvent event)
            {
                    String s = (String)cardSizeBox.getSelectedItem();
                    if (s.equals(rbx.getString("CardSize24"))) {
                            bitsPerCard = 24;
                    }
                    else if (s.equals(rbx.getString("CardSize32"))) {
                            bitsPerCard = 32;
                    }              
                    else if (s.equals(rbx.getString("CardSize8"))) {
                            bitsPerCard = 8;
                    }              
                    // here add code for other node types, if required
            }
	});
        cardSizeBox.setToolTipText(rbx.getString("TipCardSize"));
        cardSizeText.setVisible(false);
        cardSizeBox.setVisible(false);
        cardSize8Box.setVisible(false);
        
        JPanel panel13 = new JPanel();
        panel13.setLayout(new FlowLayout());
        panel13.add(new JLabel(rbx.getString("LabelPulseWidth")+" "));
        panel13.add(pulseWidthField);
        pulseWidthField.setToolTipText(rbx.getString("TipPulseWidth"));
        pulseWidthField.setText("500");       
        panel13.add(new JLabel(rbx.getString("LabelMilliseconds")));
                
        panel1.add(panel11);
        panel1.add(panel12);
	panel1.add(panel13);
        contentPane.add(panel1);			
		
        /**
         * Set up USIC/SUSIC card type configuration table
         *-------------------------------------------------
        */
        JPanel panel21 = new JPanel();
        panel21.setLayout(new BoxLayout(panel21, BoxLayout.Y_AXIS));
        panel21.add(new JLabel(rbx.getString("HintCardTypePartA")));
        panel21.add(new JLabel(" "+rbx.getString("HintCardTypePartB")));
        panel21.add(new JLabel(" "+rbx.getString("HintCardTypePartC")));
        panel21.add(new JLabel("   "));
        panel21.add(new JLabel(rbx.getString("HintCardTypePartD")));
        panel21.add(new JLabel(" "+rbx.getString("HintCardTypePartE")));
        panel21.add(new JLabel(" "+rbx.getString("HintCardTypePartF")));
        panel2.add(panel21);
        TableModel cardConfigModel = new CardConfigModel();
        JTable cardConfigTable = new JTable(cardConfigModel);
        cardConfigTable.setRowSelectionAllowed(false);
        cardConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180,95));  //c2
		
        JComboBox cardTypeCombo = new JComboBox();
        cardTypeCombo.addItem(rbx.getString("CardTypeOutput"));
        cardTypeCombo.addItem(rbx.getString("CardTypeInput"));
        cardTypeCombo.addItem(rbx.getString("CardTypeNone"));
		
        TableColumnModel typeColumnModel = cardConfigTable.getColumnModel();
        TableColumn addressColumn = typeColumnModel.getColumn(CardConfigModel.ADDRESS_COLUMN);
        addressColumn.setMinWidth(70);
        addressColumn.setMaxWidth(80);
        TableColumn cardTypeColumn = typeColumnModel.getColumn(CardConfigModel.TYPE_COLUMN);
        cardTypeColumn.setCellEditor(new DefaultCellEditor(cardTypeCombo));
        cardTypeColumn.setResizable(false);
        cardTypeColumn.setMinWidth(90);
        cardTypeColumn.setMaxWidth(100);
		
        JScrollPane cardScrollPane = new JScrollPane(cardConfigTable);
        panel2.add(cardScrollPane,BorderLayout.CENTER);
        contentPane.add(panel2);
        panel2.setVisible(false);
		
        /**
         * Set up SMINI oscillating 2-lead searchlight configuration table
         * ----------------------------------------------------------------
         */
        JPanel panel2a1 = new JPanel();
        panel2a1.setLayout(new BoxLayout(panel2a1, BoxLayout.Y_AXIS));
        panel2a1.add(new JLabel(rbx.getString("HintSearchlightPartA")));
        panel2a1.add(new JLabel(" "+rbx.getString("HintSearchlightPartB")));
        panel2a1.add(new JLabel(" "+rbx.getString("HintSearchlightPartC")));
        panel2a1.add(new JLabel("   "));
        panel2a1.add(new JLabel(rbx.getString("HintSearchlightPartD")));
        panel2a1.add(new JLabel(" "+rbx.getString("HintSearchlightPartE")));
        panel2a1.add(new JLabel(" "+rbx.getString("HintSearchlightPartF")));
        panel2a.add(panel2a1);
        TableModel searchlightConfigModel = new SearchlightConfigModel();
        JTable searchlightConfigTable = new JTable(searchlightConfigModel);
        searchlightConfigTable.setRowSelectionAllowed(false);
        searchlightConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(208,100));
        TableColumnModel searchlightColumnModel = searchlightConfigTable.getColumnModel();
        TableColumn portColumn = searchlightColumnModel.getColumn(SearchlightConfigModel.PORT_COLUMN);
        portColumn.setMinWidth(90);
        portColumn.setMaxWidth(100);
        JScrollPane searchlightScrollPane = new JScrollPane(searchlightConfigTable);
        panel2a.add(searchlightScrollPane,BorderLayout.CENTER);
        contentPane.add(panel2a);			
        panel2.setVisible(false);
		
	/**
         * Set up CPNODE/PINODE IOX port assignment table
         * ---------------------------------------
         */
        JPanel panel14 = new JPanel();        
        panel14.add(onBoardBytesText);
        panel14.setVisible(true);
        panel2c.add(panel14, BorderLayout.LINE_START);
        contentPane.add(panel2c);			
        
        JPanel panel2b1 = new JPanel();        
        panel2b1.setLayout(new BoxLayout(panel2b1, BoxLayout.Y_AXIS));
        panel2b1.add(new JLabel("Assign IOX Ports"));

        panel2b.add(panel2b1);
        
        TableModel cpnodeConfigModel = new CPnodeConfigModel();
        JTable cpnodeConfigTable = new JTable(cpnodeConfigModel);
        cpnodeConfigTable.setRowSelectionAllowed(false);
        cpnodeConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(200,130)); //160
		
        JComboBox cpnodeTypeCombo = new JComboBox();
        cpnodeTypeCombo.addItem(rbx.getString("CardTypeOutput"));
        cpnodeTypeCombo.addItem(rbx.getString("CardTypeInput"));
        cpnodeTypeCombo.addItem(rbx.getString("CardTypeNone"));
        
        TableColumnModel cpnodePortModel = cpnodeConfigTable.getColumnModel();
        TableColumn x1Column = cpnodePortModel.getColumn(CPnodeConfigModel.CARDNUM_COLUMN);
        x1Column.setMinWidth(70);
        x1Column.setMaxWidth(100);
        TableColumn x2Column = cpnodePortModel.getColumn(CPnodeConfigModel.CARDTYPE_COLUMN);
        x2Column.setCellEditor(new DefaultCellEditor(cpnodeTypeCombo));
        x2Column.setResizable(false);
        x2Column.setMinWidth(80);
        x2Column.setMaxWidth(100);
		
        JScrollPane cpnodeScrollPane = new JScrollPane(cpnodeConfigTable);
        panel2b.add(cpnodeScrollPane,BorderLayout.CENTER);
        contentPane.add(panel2b);
        panel2b.setVisible(false); 

        
        /**
         * node Description field - all node types have this field
         * -------------------------------------------------------
         */
        panelnodeDescBox.setLayout(new BoxLayout(panelnodeDescBox, BoxLayout.Y_AXIS));
        panelnodeDesc.setLayout(new FlowLayout());
        panelnodeDesc.add(new JLabel("Description:"));
        nodeDescription.setVisible(true);
        panelnodeDesc.add(nodeDescription);
        panelnodeDesc.setVisible(true);
        contentPane.add(panelnodeDesc);
		
        /**
         * Set up CMRInet/cpNode Options
         * -----------------------------
         */
        panelnetOpt.setLayout(new GridLayout(0,2));
        
        panelnetOpt.add(cbx_cmrinetopt_AUTOPOLL);
        cbx_cmrinetopt_AUTOPOLL.addItemListener(nodeOptHandler);
        cbx_cmrinetopt_AUTOPOLL.setEnabled(true);
        
        panelnetOpt.add(cbx_cmrinetopt_USECMRIX);
        cbx_cmrinetopt_USECMRIX.addItemListener(nodeOptHandler);
        cbx_cmrinetopt_USECMRIX.setEnabled(true);

        panelnetOpt.add(cbx_cmrinetopt_USEBCC);
        cbx_cmrinetopt_USEBCC.addItemListener(nodeOptHandler);
        cbx_cmrinetopt_USEBCC.setEnabled(true);
        
        panelnetOpt.add(cbx_cmrinetopt_BIT8);
        cbx_cmrinetopt_BIT8.addItemListener(nodeOptHandler);
        cbx_cmrinetopt_BIT8.setEnabled(false);
        
        panelnetOpt.add(cbx_cmrinetopt_BIT15);
        cbx_cmrinetopt_BIT15.addItemListener(nodeOptHandler);
        cbx_cmrinetopt_BIT15.setEnabled(false);

        Border panelnetOptBorder = BorderFactory.createEtchedBorder();
        Border panelnetOptTitled = BorderFactory.createTitledBorder(panelnetOptBorder,"CMRInet Options");
        panelnetOpt.setBorder(panelnetOptTitled);
        panelnetOpt.setVisible(true); //---------
        contentPane.add(panelnetOpt);
        
        panelnetOptBox.add(Box.createHorizontalStrut(50));
        contentPane.add(panelnetOptBox);

        panelnodeOpt.setLayout(new GridLayout(0,2));
        panelnodeOpt.add(cbx_cpnodeopt_SENDEOT);
        cbx_cpnodeopt_SENDEOT.setEnabled(true);
        cbx_cpnodeopt_SENDEOT.addItemListener(nodeOptHandler);
		
        panelnodeOpt.add(cbx_cpnodeopt_BIT1);
        cbx_cpnodeopt_BIT1.setEnabled(false);
        cbx_cpnodeopt_BIT1.addItemListener(nodeOptHandler);
        
        panelnodeOpt.add(cbx_cpnodeopt_BIT2);
        cbx_cpnodeopt_BIT2.setEnabled(false);
        cbx_cpnodeopt_BIT2.addItemListener(nodeOptHandler);
        
        panelnodeOpt.add(cbx_cpnodeopt_BIT8);
        cbx_cpnodeopt_BIT8.setEnabled(false);
        cbx_cpnodeopt_BIT8.addItemListener(nodeOptHandler);
        
        panelnodeOpt.add(cbx_cpnodeopt_BIT15);
        cbx_cpnodeopt_BIT15.setEnabled(false);
        cbx_cpnodeopt_BIT15.addItemListener(nodeOptHandler);
		
        Border panelnodeOptBorder = BorderFactory.createEtchedBorder();
        Border panelnodeOptTitled = BorderFactory.createTitledBorder(panelnodeOptBorder,"cpNode Options");
        panelnodeOpt.setBorder(panelnodeOptTitled);
        panelnodeOpt.setVisible(false);
        contentPane.add(panelnodeOpt);
            
        /**
         * Set up the notes area panel for various message displays
         * --------------------------------------------------------
         */
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
        panel31.setLayout(new FlowLayout());
        statusText1.setVisible(false);
        panel31.add(statusText1);
        JPanel panel32 = new JPanel();
        panel32.setLayout(new FlowLayout());
        statusText2.setVisible(false);
        panel32.add(statusText2);
        JPanel panel33 = new JPanel();
        panel33.setLayout(new FlowLayout());
        statusText3.setVisible(false);
        panel33.add(statusText3);
        panel3.add(panel31);
        panel3.add(panel32);
        panel3.add(panel33);
        Border panel3Border = BorderFactory.createEtchedBorder();
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border,rbx.getString("BoxLabelNotes"));
        panel3.setBorder(panel3Titled);
        contentPane.add(panel3);
        
        /**
         *  Set up the functions buttons
         * -----------------------------
         */
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());
        
        addNodeButton.setText(rbx.getString("ButtonAdd"));
        addNodeButton.setVisible(false);
        addNodeButton.setToolTipText(rbx.getString("TipAddButton"));
        addNodeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				addNodeButtonActionPerformed();
			}
		});
        panel4.add(addNodeButton);
		
        deleteNodeButton.setText(rbx.getString("ButtonDelete"));
        deleteNodeButton.setVisible(false);
        deleteNodeButton.setToolTipText(rbx.getString("TipDeleteButton"));
        deleteNodeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				deleteNodeButtonActionConfirm();
			}
		});
        panel4.add(deleteNodeButton);
        
        updateNodeButton.setText(rbx.getString("ButtonUpdate"));
        updateNodeButton.setVisible(false);
        updateNodeButton.setToolTipText(rbx.getString("TipUpdateButton"));
        updateNodeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				updateNodeButtonActionPerformed();
			}
		});
        panel4.add(updateNodeButton);
        
        cancelNodeButton.setText(rbx.getString("ButtonCancel"));
        cancelNodeButton.setVisible(false);
        cancelNodeButton.setToolTipText(rbx.getString("TipCancelButton"));
        cancelNodeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				cancelNodeButtonActionPerformed();
			}
		});
        panel4.add(cancelNodeButton);

        doneNodeButton.setText(rbx.getString("ButtonDone"));
        doneNodeButton.setVisible(false);
        doneNodeButton.setToolTipText(rbx.getString("TipDoneButton"));
        doneNodeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				doneNodeButtonActionPerformed();
			}
		});
        panel4.add(doneNodeButton);
        
        
        contentPane.add(panel4);
        pack();
    }
	
    /**
     * -------------------------------------------------------
     * Methods to set the correct states of the window buttons
     * -------------------------------------------------------
     */
    public void buttonSet_ADD()  {
        setTitle("ADD NODE");
        statusText1.setText(addStatus1);
        statusText1.setVisible(true);
        statusText2.setText(addStatus2);
        statusText2.setVisible(true);
        statusText3.setText(addStatus3);
        statusText3.setVisible(false);
        
        panel2c.setVisible(false);
        
        addNodeButton.setVisible(true);
        deleteNodeButton.setVisible(false);
        updateNodeButton.setVisible(false);
        doneNodeButton.setVisible(false);
        cancelNodeButton.setVisible(true); 
    }
    public void buttonSet_EDIT()  {
        setTitle("EDIT NODE");
        statusText1.setText(editStatus1);
        statusText1.setVisible(true);
        statusText2.setText(editStatus2);
        statusText2.setVisible(true);
        statusText3.setText(editStatus3);
        statusText3.setVisible(false);
        
        addNodeButton.setVisible(false);
        deleteNodeButton.setVisible(false);
        updateNodeButton.setVisible(true);
        doneNodeButton.setVisible(false);
        cancelNodeButton.setVisible(true); 
    }
    public void buttonSet_DELETE()  {
        setTitle("DELETE NODE");
        statusText1.setText(delStatus1);
        statusText1.setVisible(true);
        statusText2.setText(delStatus2);
        statusText2.setVisible(true);
        statusText3.setText(delStatus3);
        statusText3.setVisible(false);
        
        addNodeButton.setVisible(false);
        deleteNodeButton.setVisible(true);
        updateNodeButton.setVisible(false);
        doneNodeButton.setVisible(false);
        cancelNodeButton.setVisible(true); 
    }
    	
    public void addNodeButtonActionPerformed() {
        /**
         *  Check that a node with this address does not exist
         */
        curNode = null;
        int nodeAddress = readNodeAddress();
        if (nodeAddress < 0) return;
        
        /**         
         * get a SerialNode corresponding to this node address if one exists
         */
        curNode = (SerialNode) SerialTrafficController.instance().getNodeFromAddress(nodeAddress);
        if (curNode != null) {
            JOptionPane.showMessageDialog(this,rbx.getString("Error1")+Integer.toString(nodeAddress)+
								rbx.getString("Error2"),"",JOptionPane.ERROR_MESSAGE); 

            statusText1.setText(rbx.getString("Error1")+Integer.toString(nodeAddress)+
								rbx.getString("Error2"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        
        /**
         * get node information from window and check for data consistency 
         */
        if ( !readReceiveDelay() ) return;
	if ( !readPulseWidth() ) return;
        if ( !checkConsistency() ) return;
        
       /**
        * ------------------------------
        * all ready, create the new node
        * ------------------------------
        */
        curNode = new SerialNode(nodeAddress,nodeType);
        if (curNode == null) {
            statusText1.setText(rbx.getString("Error3"));
            statusText1.setVisible(true);
            JOptionPane.showMessageDialog(this,rbx.getString("Error3")+Integer.toString(nodeAddress),"",JOptionPane.ERROR_MESSAGE); 
            log.error("Error creating Serial Node, constructor returned null");
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        nodeTableModel.addRow(curNode);
        
        // configure the new node
        //-----------------------
        setNodeParameters();
        
        // register any orphan sensors that this node may have
        //----------------------------------------------------
        SerialSensorManager.instance().registerSensorsForNode(curNode);
        
        // reset text displays after succefully adding node
        //-------------------------------------------------
        resetNotes();
        changedNode = true;
        
        // provide user feedback
        //----------------------
        statusText1.setText(rbx.getString("FeedBackAdd")+" "+Integer.toString(nodeAddress));
        statusText2.setVisible(false);
        statusText3.setVisible(true);
        doneNodeButton.setVisible(true);
        cancelNodeButton.setVisible(false); 
        errorInStatus1 = true;        
    }
    
    /**
     * Method to set up edit function window
     */        
    public void editNodeButtonActionPerformed(int nodeaddr) {
        // Find Serial Node address
        //-------------------------
        nodeAddress = nodeaddr;
        if (nodeAddress < 0) return;
        
        // get the SerialNode corresponding to this node address
        //------------------------------------------------------
        curNode = (SerialNode) SerialTrafficController.instance().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(rbx.getString("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        
        // Set up static node address from the table, cannot be changed
        //-------------------------------------------------------------
        nodeAddrStatic.setText(Integer.toString(nodeAddress));
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(true);  
        
        // get information for this node and set up combo box
        //---------------------------------------------------
        nodeType = curNode.getNodeType();
        switch (nodeType) //c2
        {   // SMINI
            //------
            case SerialNode.SMINI:
                nodeTypeBox.setSelectedItem("SMINI");
                bitsPerCard = 24;
                cardSizeBox.setSelectedItem(rbx.getString("CardSize24"));
                // set up the searchlight arrays
                num2LSearchLights = 0;
                for (int i=0;i<48;i++) {
                if ( curNode.isSearchLightBit(i) ) {
                    searchlightBits[i] = true;
                    searchlightBits[i+1] = true;
                    firstSearchlight[i] = true;
                    firstSearchlight[i+1] = false;
                    num2LSearchLights ++;
                    i++;
                }
                else {
                    searchlightBits[i] = false;
                    firstSearchlight[i] = false;
                }
            }
            break;
            // USIC/SUSIC
            //-----------
            case SerialNode.USIC_SUSIC:
		nodeTypeBox.setSelectedItem("USIC_SUSIC");
                bitsPerCard = curNode.getNumBitsPerCard();
                if (bitsPerCard==24) {
                cardSizeBox.setSelectedItem(rbx.getString("CardSize24"));
                }
                if (bitsPerCard==32) {
                cardSizeBox.setSelectedItem(rbx.getString("CardSize32"));
                }
            break;
            // CPNODE
            //-------
            case SerialNode.CPNODE:
		nodeTypeBox.setSelectedItem("CPNODE");
                bitsPerCard = 8;
                cardSize8Box.setSelectedItem(rbx.getString("CardSize8"));
		onBoardBytesText.setText(onBoardBytesText.getText()+" 2 Bytes");			
                
                // --------------
                // cpNode Options
		// -------------- 
                cbx_cpnodeopt_SENDEOT.setSelected(curNode.iscpnodeBit(SerialNode.optbitNode_SENDEOT));
                cbx_cpnodeopt_BIT1.setSelected(false);
                cbx_cpnodeopt_BIT2.setSelected(false);
                cbx_cpnodeopt_BIT8.setSelected(curNode.iscpnodeBit(SerialNode.optbitNode_BIT8));
                cbx_cpnodeopt_BIT15.setSelected(curNode.iscpnodeBit(SerialNode.optbitNode_BIT15));
            break;
            // PINODE
            //-------               
            case SerialNode.PINODE:
		nodeTypeBox.setSelectedItem("PiNODE");
                bitsPerCard = 8;
                cardSize8Box.setSelectedItem(rbx.getString("CardSize8"));
		onBoardBytesText.setText(onBoardBytesText.getText()+" 3 Bytes");
                
                // --------------
                // Pi Node Options
		// -------------- 
                cbx_cpnodeopt_SENDEOT.setSelected(curNode.iscpnodeBit(SerialNode.optbitNode_SENDEOT));
                cbx_cpnodeopt_BIT1.setSelected(false);
                cbx_cpnodeopt_BIT2.setSelected(false);
                cbx_cpnodeopt_BIT8.setSelected(curNode.iscpnodeBit(SerialNode.optbitNode_BIT8));
                cbx_cpnodeopt_BIT15.setSelected(curNode.iscpnodeBit(SerialNode.optbitNode_BIT15));
            break;
                
            default:
              log.error("Unknown Node Type "+nodeType);
            break;
        }
        
        /**
         * ----------------------------------
         * CMRInet Options for all node types
	 *-----------------------------------
         */
        cbx_cmrinetopt_AUTOPOLL.setSelected(curNode.isCMRInetBit(SerialNode.optbitNet_AUTOPOLL));
        cbx_cmrinetopt_USECMRIX.setSelected(curNode.isCMRInetBit(SerialNode.optbitNet_USECMRIX));
        cbx_cmrinetopt_USEBCC.setSelected(curNode.isCMRInetBit(SerialNode.optbitNet_USEBCC));
        cbx_cmrinetopt_BIT8.setSelected(curNode.isCMRInetBit(SerialNode.optbitNet_BIT8));
        cbx_cmrinetopt_BIT15.setSelected(curNode.isCMRInetBit(SerialNode.optbitNet_BIT15));
        
                
        // set up receive delay
        receiveDelay = curNode.getTransmissionDelay();
        receiveDelayField.setText(Integer.toString(receiveDelay));
        
	// set up pulse width
        pulseWidth = curNode.getPulseWidth();
        pulseWidthField.setText(Integer.toString(pulseWidth));
        
	// node description
        nodeDescText = curNode.getcmriNodeDesc();
        nodeDescription.setText(nodeDescText);
        
        // set up card types
        for (int i=0;i<64;i++) {
            if (curNode.isOutputCard(i)) {
                cardType[i] = rbx.getString("CardTypeOutput");
            }
            else if (curNode.isInputCard(i)) {
                cardType[i] = rbx.getString("CardTypeInput");
            }
            else {
                cardType[i] = rbx.getString("CardTypeNone");
            }
        }
		
        // ensure that table displays correctly
        panel2.setVisible(false);
        panel2a.setVisible(false);
        if (nodeType==SerialNode.USIC_SUSIC) {
            panel2.setVisible(true);
        }
        else if (nodeType==SerialNode.SMINI) {
            panel2a.setVisible(true);
        }
        else if (nodeType==SerialNode.CPNODE) {
            panel2b.setVisible(true);
        }
        else if (nodeType==SerialNode.PINODE) {
            panel2b.setVisible(true);
        }
        		
        // Switch to edit notes
        //---------------------
        editMode = true;
        statusText1.setText(editStatus1);
        statusText1.setVisible(true);
        statusText2.setText(editStatus2);
        statusText2.setVisible(true);
        statusText3.setText(editStatus3);
        statusText3.setVisible(true);
    }
    
    /**
     * Method to handle update button 
     */        
    public void updateNodeButtonActionPerformed() {
        // get node information from window
        if ( !readReceiveDelay() ) return;
	if ( !readPulseWidth() ) return;
        
        // check consistency of node information
        if ( !checkConsistency() ) return;
        
        // update node information
        if (curNode.getNodeType() != nodeType) {
            // node type has changed
            curNode.setNodeType(nodeType);
        }
        
        // cmri node description  c2
        curNode.setcmriNodeDesc(nodeDescription.getText());
        setNodeParameters();
        nodeTableModel.changeRow(selectedTableRow,curNode);      
        
        // Switch buttons
        changedNode = true;
        doneNodeButton.setVisible(true);
        updateNodeButton.setVisible(true);
        cancelNodeButton.setVisible(true);
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(true);
        
        // provide user feedback
        statusText1.setText(rbx.getString("FeedBackUpdate")+" "+Integer.toString(nodeAddress));
        statusText2.setVisible(false);
        cancelNodeButton.setVisible(false); 
        errorInStatus1 = true;
    }
	
    /**
     * Method to handle delete button pressed 
     */        
    public void deleteNodeButtonActionConfirm() {
        // confirm deletion with the user
        //-------------------------------
       if ( JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
            this,rbx.getString("ConfirmDelete1")+"\n"+
            rbx.getString("ConfirmDelete2"),rbx.getString("ConfirmDeleteTitle"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE) ) {

            // delete this node
            SerialTrafficController.instance().deleteNode(deleteNodeAddress);  
            nodeTableModel.removeRow(selectedTableRow);
            
            // provide user feedback
            resetNotes();
            statusText1.setText(rbx.getString("FeedBackDelete")+" "+Integer.toString(deleteNodeAddress));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            changedNode = true;
            deleteNodeButton.setVisible(false);
            doneNodeButton.setVisible(true);
            cancelNodeButton.setVisible(false); 
            statusText2.setVisible(false);
	}
        else {
            // reset as needed
            resetNotes();
        }
    }
    /**
     * Method to set up delete node window 
     */        
    public void deleteNodeButtonActionPerformed(int nodeAddress) {
	// Set up static node address
        deleteNodeAddress = nodeAddress;
        nodeAddrStatic.setText(Integer.toString(nodeAddress));
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(true); 
        
        // Find Serial Node address
        if (nodeAddress < 0) 
        {
            log.info("nodeAddress < 0");
            return;
        }
        
        // get the node corresponding to this node address
        curNode = (SerialNode) SerialTrafficController.instance().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(rbx.getString("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        else {
            statusText1.setText(rbx.getString("NotesDel3"));
            statusText1.setVisible(true); 
        }
    }
	
    /**
     * Method to handle done button 
     */        
    public void doneNodeButtonActionPerformed() {
        if (editMode) {
            // Reset 
            editMode = false;
            curNode = null;
            // Switch buttons
            addNodeButton.setVisible(false);
            editNodeButton.setVisible(false);
            deleteNodeButton.setVisible(false);
            doneNodeButton.setVisible(true);
            updateNodeButton.setVisible(false);
            cancelNodeButton.setVisible(false);
            nodeAddrField.setVisible(false);
            nodeAddrStatic.setVisible(false);     
        }
        
        /**
         * The reminder dialog is now only displayed when closing the
         * node config manager window
         */
        /*
        if (changedNode) {
         Remind user to Save new configuration
         JOptionPane.showMessageDialog(this,
         rbx.getString("Reminder1")+"\n"+rbx.getString("Reminder2"),
         rbx.getString("ReminderTitle"),
         JOptionPane.INFORMATION_MESSAGE);
        }
        */
        setVisible(false);
        dispose(); 
    }
	
    /**
     * Method to handle cancel button 
     */        
    public void cancelNodeButtonActionPerformed() {
        // Reset 
        editMode = false;
        changedNode = false;
        curNode = null;
        // Switch buttons
        addNodeButton.setVisible(false);
        editNodeButton.setVisible(false);
        deleteNodeButton.setVisible(false);
        doneNodeButton.setVisible(false);
        updateNodeButton.setVisible(false);
        cancelNodeButton.setVisible(false);
        // make node address editable again	
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(false);             
        // refresh notes panel
	// statusText1.setText(stdStatus1);
	// statusText2.setText(stdStatus2);
	// statusText3.setText(stdStatus3);
        setVisible(false);
        dispose();
    }
	
    /**
     * Method to close the window when the close box is clicked
     */
    public void windowClosing(java.awt.event.WindowEvent e) {
        doneNodeButtonActionPerformed();
        super.windowClosing(e);
    }    

    /**
     * Set the node parameters by type.  Some parameters are specific to
     * a particular node type
     */
    void setNodeParameters() {
        // receive delay is common for all node types
	int numInput = 0;
	int numOutput = 0;
	curNode.setTransmissionDelay(receiveDelay);
        
        // pulse width is common for all node types
        curNode.setPulseWidth(pulseWidth);
        
        // continue in a node specific way
        switch (nodeType) {
            // SMINI
            //------
            case SerialNode.SMINI:
                // Note: most parameters are set by default on creation
                int numSet = 0;
                // Configure 2-lead oscillating searchlights - first clear unneeded searchlights
                for (int j=0;j<47;j++) {
                    if ( curNode.isSearchLightBit(j) ) {
                        if(!firstSearchlight[j]) {
                            // this is the first bit of a deleted searchlight - clear it
                            curNode.clear2LeadSearchLight(j);
                            // skip over the second bit of the cleared searchlight
                            j ++;
                        }
                        else {
                            // this is the first bit of a kept searchlight - skip second bit
                            j ++;
                        }
                    }
                }
                // Add needed searchlights that are not already configured    
                for (int i=0;i<47;i++) {
                    if ( firstSearchlight[i] ) {
                        if ( !curNode.isSearchLightBit(i) ) {
                            // this is the first bit of an added searchlight
                            curNode.set2LeadSearchLight(i);
                        }
                        numSet ++;
                    }
                }
                // consistency check
                if (numSet != num2LSearchLights) {
                    log.error("Inconsistent numbers of 2-lead searchlights. numSet = "+
							  Integer.toString(numSet)+", num2LSearchLights = "+
							  Integer.toString(num2LSearchLights) );
                }
	    break;
                
            // USIC/SUSIC
            //-----------
            case SerialNode.USIC_SUSIC:
                // set number of bits per card
                curNode.setNumBitsPerCard(bitsPerCard);
                // configure the input/output cards
                numInput = 0;
                numOutput = 0;
                for (int i=0;i<64;i++) {
                    if ( "No Card".equals(cardType[i]) ) {
                        curNode.setCardTypeByAddress(i,SerialNode.NO_CARD);
                    }
                    else if ( "Input Card".equals(cardType[i]) ) {
                        curNode.setCardTypeByAddress(i,SerialNode.INPUT_CARD);
                        numInput ++;
                    }
                    else if ( "Output Card".equals(cardType[i]) ) {
                        curNode.setCardTypeByAddress(i,SerialNode.OUTPUT_CARD);
                        numOutput ++;
                    }
                    else {
                        log.error("Unexpected card type - "+cardType[i]);
                    }
                }
                // consistency check
                if ( numCards != (numOutput+numInput) ) {
                    log.error("Inconsistent numbers of cards - setNodeParameters.");
                }

	    break;
            
            // CPNODE
            //-------
            case SerialNode.CPNODE:     //c2
                // set number of bits per card
                bitsPerCard = 8;
                curNode.setNumBitsPerCard(bitsPerCard);
                numInput  = 2;
                numOutput = 2;
				// configure the input/output cards
                for (int i=4;i<64;i++) // Skip the onboard bytes 
                {
                    if ( "No Card".equals(cardType[i]) ) 
                    {
                        curNode.setCardTypeByAddress(i,SerialNode.NO_CARD);
                    }
                    else if ( "Input Card".equals(cardType[i]) )
                    {
                        curNode.setCardTypeByAddress(i,SerialNode.INPUT_CARD);
                        numInput++;
                    }
                    else if ( "Output Card".equals(cardType[i]) )
                    {
                        curNode.setCardTypeByAddress(i,SerialNode.OUTPUT_CARD);
                        numOutput++;
                    }
                    else 
                    {
                        log.error("Unexpected card type - "+cardType[i]);
                    }
                }
                
                // Set the node option bits.  Some are moved from the CMRInet options
                //-------------------------------------------------------------------
                curNode.setOptNode_SENDEOT(cbx_cpnodeopt_SENDEOT.isSelected() ? 1 : 0);
                curNode.setOptNode_USECMRIX(cbx_cmrinetopt_USECMRIX.isSelected() ? 1 : 0); // From CMRInet
                curNode.setOptNode_USEBCC(cbx_cmrinetopt_USEBCC.isSelected() ? 1 : 0); // From CMRInet
                curNode.setOptNode_BIT8(cbx_cpnodeopt_BIT8.isSelected() ? 1 : 0);
                curNode.setOptNode_BIT15(cbx_cpnodeopt_BIT15.isSelected() ? 1 : 0);

            break;

            // PINODE
            //-------
            case SerialNode.PINODE:     //c2
                // set number of bits per card
                bitsPerCard = 8;
                curNode.setNumBitsPerCard(bitsPerCard);
                numInput  = 3;
                numOutput = 3;
				// configure the input/output cards
                for (int i=6;i<64;i++) // Skip the onboard bytes 
                {
                    if ( "No Card".equals(cardType[i]) ) 
                    {
                        curNode.setCardTypeByAddress(i,SerialNode.NO_CARD);
                    }
                    else if ( "Input Card".equals(cardType[i]) )
                    {
                        curNode.setCardTypeByAddress(i,SerialNode.INPUT_CARD);
                        numInput++;
                    }
                    else if ( "Output Card".equals(cardType[i]) )
                    {
                        curNode.setCardTypeByAddress(i,SerialNode.OUTPUT_CARD);
                        numOutput++;
                    }
                    else 
                    {
                        log.error("Unexpected card type - "+cardType[i]);
                    }
                }
                
                // Set the node option bits.  Some are moved from the CMRInet options
                //-------------------------------------------------------------------
                curNode.setOptNode_SENDEOT(cbx_cpnodeopt_SENDEOT.isSelected() ? 1 : 0);
                curNode.setOptNode_USECMRIX(cbx_cmrinetopt_USECMRIX.isSelected() ? 1 : 0); // From CMRInet
                curNode.setOptNode_USEBCC(cbx_cmrinetopt_USEBCC.isSelected() ? 1 : 0); // From CMRInet
                curNode.setOptNode_BIT8(cbx_cpnodeopt_BIT8.isSelected() ? 1 : 0);
                curNode.setOptNode_BIT15(cbx_cpnodeopt_BIT15.isSelected() ? 1 : 0);

            break;

            default:
                log.error("Unexpected node type in setNodeParameters- "+Integer.toString(nodeType));
            break;
        }
        
        /**
         *   Set the node description for all types
         */
        curNode.setcmriNodeDesc(nodeDescription.getText()); 

        /**
         * Cause reinitialization of this Node to reflect these parameters
         * ---------------------------------------------------------------
         */
        SerialTrafficController.instance().initializeSerialNode(curNode);
    }
    
    /**
     * Method to reset the notes error after error display
     */
    private void resetNotes() {
        if (errorInStatus1) {
            if (editMode) {
		statusText1.setText(editStatus1);
            }
            else {
	    //  statusText1.setText(stdStatus1);
            }
            errorInStatus1 = false;
        }
        resetNotes2();
    }
    /**
     * Reset the second line of Notes area
     */
    private void resetNotes2() {
        if (errorInStatus2) {
            if (editMode) {
                statusText1.setText(editStatus2);
            }
            else {
 //                statusText2.setText(stdStatus2);
            }
            errorInStatus2 = false;
        }
    }
    
    /**
     * Read node address and check for legal range
     *     If successful, a node address in the range 0-127 is returned.
     *     If not successful, -1 is returned and an appropriate error
     *          message is placed in statusText1.
     */
    private int readNodeAddress() {
        int addr = -1;
        try 
        {
            addr = Integer.parseInt(nodeAddrField.getText());
        }
        catch (Exception e)
        {
            statusText1.setText(rbx.getString("Error5"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return -1;
        }
        if ( (addr < 0) || (addr > 127) ) {
//            statusText1.setText(rbx.getString("Error6"));
//            statusText1.setVisible(true);
            errorInStatus1 = true;
            JOptionPane.showMessageDialog(this,rbx.getString("Error6"),"",JOptionPane.ERROR_MESSAGE); 
            resetNotes2();
            return -1;
        }
        return (addr);
    }
    
    /**
     * Read receive delay from window
     *    Returns 'true' if successful, 'false' if an error was detected.
     *    If an error is detected, a suitable error message is placed in the
     *        Notes area
     */
    protected boolean readReceiveDelay() {
        // get the transmission delay
        try 
        {
            receiveDelay = Integer.parseInt(receiveDelayField.getText());
        }
        catch (Exception e)
        {
            statusText1.setText(rbx.getString("Error7"));
            statusText1.setVisible(true);
            JOptionPane.showMessageDialog(this,rbx.getString("Error7"),"",JOptionPane.ERROR_MESSAGE); 
            receiveDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (receiveDelay < 0) {
            statusText1.setText(rbx.getString("Error8"));
            statusText1.setVisible(true);
            JOptionPane.showMessageDialog(this,rbx.getString("Error8"),"",JOptionPane.ERROR_MESSAGE); 
            receiveDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (receiveDelay > 65535) {
            statusText1.setText(rbx.getString("Error9"));
            statusText1.setVisible(true);
            JOptionPane.showMessageDialog(this,rbx.getString("Error9"),"",JOptionPane.ERROR_MESSAGE); 
            receiveDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        // successful
        return true;
    }
    
    /**
     * Read pulse width from window
     *    Returns 'true' if successful, 'false' if an error was detected.
     *    If an error is detected, a suitable error message is placed in the
     *        Notes area
     */
    protected boolean readPulseWidth() {
        // get the pulse width
        try 
        {
            pulseWidth = Integer.parseInt(pulseWidthField.getText());
        }
        catch (Exception e)
        {
            statusText1.setText(rbx.getString("Error18"));
            statusText1.setVisible(true);
            JOptionPane.showMessageDialog(this,rbx.getString("Error18"),"",JOptionPane.ERROR_MESSAGE); 
            pulseWidth = 500;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (pulseWidth < 100) {
            statusText1.setText(rbx.getString("Error16"));
            statusText1.setVisible(true);
            JOptionPane.showMessageDialog(this,rbx.getString("Error16"),"",JOptionPane.ERROR_MESSAGE); 
            pulseWidth = 100;
	    pulseWidthField.setText(Integer.toString(pulseWidth));
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (pulseWidth > 10000) {
            statusText1.setText(rbx.getString("Error17"));
            statusText1.setVisible(true);
            JOptionPane.showMessageDialog(this,rbx.getString("Error17"),"",JOptionPane.ERROR_MESSAGE); 
            pulseWidth = 500;
	    pulseWidthField.setText(Integer.toString(pulseWidth));
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        // successful
        return true;
    }
	
    /**
     * Check for consistency errors by node type
     *    Returns 'true' if successful, 'false' if an error was detected.
     *    If an error is detected, a suitable error message is placed in the
     *        Notes area
     */
    protected boolean checkConsistency() {
        switch (nodeType) {
            case SerialNode.SMINI:
                // ensure that number of searchlight bits is consistent
                int numBits = 0;
                for (int i = 0;i<48;i++) {
                    if (searchlightBits[i]) numBits ++;
                }
                if ( (2*num2LSearchLights) != numBits ) {
                    statusText1.setText(rbx.getString("Error10"));
                    statusText1.setVisible(true);
                    JOptionPane.showMessageDialog(this,rbx.getString("Error10"),"",JOptionPane.ERROR_MESSAGE); 
                    errorInStatus1 = true;
                    resetNotes2();
                    return (false);
                }
	    break;
            case SerialNode.USIC_SUSIC:
                // ensure that at least one card is defined
                numCards = 0;
                boolean atNoCard = false;
                for (int i = 0; i<64 ; i++) {
                    if ( (cardType[i].equals(rbx.getString("CardTypeOutput"))) || 
						(cardType[i].equals(rbx.getString("CardTypeInput"))) ) {
                        if (atNoCard) {
                            // gap error
                            statusText1.setText(rbx.getString("Error11"));
                            statusText1.setVisible(true);
                            statusText2.setText(rbx.getString("Error12"));
                            JOptionPane.showMessageDialog(this,rbx.getString("Error11")+rbx.getString("Error12"),"",JOptionPane.ERROR_MESSAGE); 
                            errorInStatus1 = true;
                            errorInStatus2 = true;
                            return (false);
                        }
                        else {
                            numCards ++;
                        }
                    }
                    else if (cardType[i].equals(rbx.getString("CardTypeNone"))) {
                        atNoCard = true;
                    }
                }
                // ensure that at least one card has been defined
                if ( numCards <= 0 ) {
                    // no card error
                    statusText1.setText(rbx.getString("Error13"));
                    statusText2.setText(rbx.getString("Error14"));
                    statusText1.setVisible(true);
                    JOptionPane.showMessageDialog(this,rbx.getString("Error13")+rbx.getString("Error14"),"",JOptionPane.ERROR_MESSAGE); 
                    errorInStatus1 = true;
                    errorInStatus2 = true;
                    return (false);
                }
                // check that card size is 24 or 32 bit
                if ( (bitsPerCard!=24 ) && (bitsPerCard!=32) ) {
                    // card size error
                    statusText1.setText(rbx.getString("Error15"));
                    statusText1.setVisible(true);
                    errorInStatus1 = true;
                    JOptionPane.showMessageDialog(this,rbx.getString("Error15"),"",JOptionPane.ERROR_MESSAGE); 
                    resetNotes2();
                    return (false);
                }
                // further checking if in Edit mode
                if (editMode) {
                    // get pre-edit numbers of cards
                    int numOutput = curNode.numOutputCards();
                    int numInput = curNode.numInputCards();
                    // will the number of cards be reduced by this edit?
                    if ( numCards<(numOutput+numInput) ) {
                        if ( javax.swing.JOptionPane.NO_OPTION == javax.swing.JOptionPane.showConfirmDialog(this,
                              rbx.getString("ConfirmUpdate1")+"\n"+
                              rbx.getString("ConfirmUpdate2")+"\n"+
                              rbx.getString("ConfirmUpdate3"),
                              rbx.getString("ConfirmUpdateTitle"),
                              javax.swing.JOptionPane.YES_NO_OPTION,
                              javax.swing.JOptionPane.WARNING_MESSAGE) ) {
                            // user said don't update - cancel the update
                            return (false);
                        }
                    }
                }
	    break;
            case SerialNode.CPNODE:   //c2
                for (int j = 0; j<64; j++) {
                    if ( (cardType[j].equals(rbx.getString("CardTypeOutput"))) || 
						(cardType[j].equals(rbx.getString("CardTypeInput"))) )
						numCards ++;  
                }
	    break;
            case SerialNode.PINODE:   //c2
                for (int j = 0; j<64; j++) {
                    if ( (cardType[j].equals(rbx.getString("CardTypeOutput"))) || 
						(cardType[j].equals(rbx.getString("CardTypeInput"))) )
						numCards ++;  
                }
	    break;
				
				
                // here add code for other types of nodes
            default:
             log.warn("Unexpected node type - "+Integer.toString(nodeType));
            break;
        }
        return true;
    }
	
    /**
     * Set up table for selecting card type by address for USIC_SUSIC nodes
     */
    public class CardConfigModel extends AbstractTableModel
    {
        public String getColumnName(int c) {return cardConfigColumnNames[c];}
        public Class<?> getColumnClass(int c) {return String.class;}
        public int getColumnCount () {return 2;}
        public int getRowCount () {return 64;}
        public Object getValueAt (int r,int c) {
            if (c==0) {
                return Integer.toString(r);
            }
            else if (c==1) {
                return cardType[r];
            }
            return "";
        }
        public void setValueAt(Object type,int r,int c) {
            if (c==1) {
                cardType[r] = (String)type;
            }
        }
        public boolean isCellEditable(int r,int c) {return (c==1);}
		
        public static final int ADDRESS_COLUMN = 0;
        public static final int TYPE_COLUMN = 1;
    }
    private String[] cardConfigColumnNames = {rbx.getString("HeadingCardAddress"),
                                              rbx.getString("HeadingCardType")};
    private String[] cardType = new String[64];
	
    /**
     * Set up model for SMINI table for designating oscillating 2-lead searchlights
     */
    public class SearchlightConfigModel extends AbstractTableModel
    {
        public String getColumnName(int c) {return searchlightConfigColumnNames[c];}
        public Class<?> getColumnClass(int c) {
            if (c > 0) {
                return Boolean.class;
            }
            else {
                return String.class;
            }
        }
        public int getColumnCount () {return 9;}
        public int getRowCount () {return 6;}
        public Object getValueAt (int r,int c) {
            if (c==0) {
                switch (r) {
                    case 0:
                        return ("Card 0 Port A");
                    case 1:
                        return ("Card 0 Port B");
                    case 2:
                        return ("Card 0 Port C");
                    case 3:
                        return ("Card 1 Port A");
                    case 4:
                        return ("Card 1 Port B");
                    case 5:
                        return ("Card 1 Port C");
                    default:
                        return ("");
                }
            }
            else {
                int index = (r*8) + (c-1);
                if (searchlightBits[index]) {
                    return (Boolean.TRUE);
                }
                else {
                    return (Boolean.FALSE);
                }
            }
        }
        public void setValueAt(Object type,int r,int c) {
            if (c > 0) {
                int index = (r*8) + (c-1);
				if (!((Boolean)type).booleanValue()) {
                    searchlightBits[index] = false;
                    if (firstSearchlight[index]) {
                        searchlightBits[index+1] = false;
                        firstSearchlight[index] = false;
                    }
                    else {
                        searchlightBits[index-1] = false;
                        firstSearchlight[index-1] = false;
                    }
                    num2LSearchLights --;
                }
                else {
                    if (index<47) {
                        if (!searchlightBits[index] && !searchlightBits[index+1]) {
                            searchlightBits[index] = true;
                            searchlightBits[index+1] = true;
                            firstSearchlight[index] = true;
                            firstSearchlight[index+1] = false;
                            if (index > 0) {
                                firstSearchlight[index-1] = false;
                            }
                            num2LSearchLights ++;
                        }
                    }
                }
                panel2a.setVisible(false);
                panel2a.setVisible(true);
            }
        }
        public boolean isCellEditable(int r,int c) {return (c!=0);}
		
        public static final int PORT_COLUMN = 0;
    }
    private String[] searchlightConfigColumnNames = {rbx.getString("HeadingPort"),"0","1","2","3","4","5","6","7"};
    private boolean[] searchlightBits = new boolean[48];   // true if this bit is a searchlight bit
    private boolean[] firstSearchlight = new boolean[48];  // true if first of a pair of searchlight bits
	
    /* ------------------------------- */   
    /* cpNode options checkbox handler */
    /* ------------------------------- */   
    private class HandlerClass implements ItemListener{
        public void itemStateChanged(ItemEvent e){
            JCheckBox checkbox = (JCheckBox) e.getSource();
            
         // Network options
         //----------------
            if (checkbox == cbx_cmrinetopt_AUTOPOLL) { 
                curNode.setCMRInetOpts(SerialNode.optbitNet_AUTOPOLL,(cbx_cmrinetopt_AUTOPOLL.isSelected() ? 1 : 0));
            } else
                
            if (checkbox == cbx_cmrinetopt_USECMRIX) {
                curNode.setCMRInetOpts(SerialNode.optbitNet_USECMRIX,(cbx_cmrinetopt_USECMRIX.isSelected() ? 1 : 0));
                curNode.setcpnodeOpts(SerialNode.optbitNet_USECMRIX,curNode.getCMRInetOpts(SerialNode.optbitNet_USECMRIX));
            } else 
					
            if (checkbox == cbx_cmrinetopt_USEBCC) {
                curNode.setCMRInetOpts(SerialNode.optbitNet_USEBCC,(cbx_cmrinetopt_USEBCC.isSelected() ? 1 : 0));
                curNode.setcpnodeOpts(SerialNode.optbitNet_USEBCC,curNode.getCMRInetOpts(SerialNode.optbitNet_USEBCC));
            } else 
                
            if (checkbox == cbx_cmrinetopt_BIT8) {
                curNode.setCMRInetOpts(SerialNode.optbitNet_BIT8,(cbx_cmrinetopt_BIT8.isSelected() ? 1 : 0));
            } else
						
            if (checkbox == cbx_cmrinetopt_BIT15) {
                curNode.setCMRInetOpts(SerialNode.optbitNet_BIT15,(cbx_cmrinetopt_BIT15.isSelected() ? 1 : 0));
            } else
							
         // cpNode options
         //---------------
            if (checkbox == cbx_cpnodeopt_SENDEOT) {
                curNode.setcpnodeOpts(SerialNode.optbitNode_SENDEOT,(cbx_cpnodeopt_SENDEOT.isSelected() ? 1 : 0));
            } else
            if (checkbox == cbx_cpnodeopt_BIT1) {
                cbx_cpnodeopt_BIT1.setSelected(false);
            } else								
            if (checkbox == cbx_cpnodeopt_BIT2) {
                cbx_cpnodeopt_BIT2.setSelected(false);
            } else								
            if (checkbox == cbx_cpnodeopt_BIT8) {
                curNode.setcpnodeOpts(SerialNode.optbitNode_BIT8,(cbx_cpnodeopt_BIT8.isSelected() ? 1 : 0));
            }
            if (checkbox == cbx_cpnodeopt_BIT15) {
                curNode.setcpnodeOpts(SerialNode.optbitNode_BIT15,(cbx_cpnodeopt_BIT15.isSelected() ? 1 : 0));
            }
        changedNode = true;
        }
    }
	
    /**
     * Set up table for selecting card type by address for CPNODE/PINODE nodes
     */
    public class CPnodeConfigModel extends AbstractTableModel  //c2
    {
        public String getColumnName(int c) {return cpnodeConfigColumnNames[c];}
        public Class<?> getColumnClass(int c) {return String.class;}
        public int getColumnCount () {return 2;}
        public int getRowCount () {return 16;}
        public Object getValueAt (int r,int c)
        {
            String[] cdPort = {"  A","  B"};
            String   val = "     ";
            switch(c)
            {
		case CARDNUM_COLUMN:
                    int i=r/2;
		if (r%2 == 0) 
                    val = Integer.toHexString(0x20+i)+" ";
		return "    "+val+"     "+cdPort[(r%2)];
		case CARDTYPE_COLUMN:
		return " "+cardType[r+cpNodeOnboard];
		default:
		return "";
            }
        }
		
        public void setValueAt(Object type,int r,int c)
        {
            if (c==1) 
            {
                cardType[r+cpNodeOnboard] = (String)type;
            }
        }
        public boolean isCellEditable(int r,int c) {return (c==1);}
		
        public static final int CARDNUM_COLUMN = 0;
        public static final int CARDTYPE_COLUMN = 1;
    }
    private String[] cpnodeConfigColumnNames = {"IOX Addr  Port","Port Type"};
    private String[] cpnodecardType = new String[64];
    private int[]    cpnodeOpts = new int[16];  // Local storage for node options
    private int[]    cmrinetOpts = new int[16];  // Local storage for node options
	
//    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NodeConfigurationMgrFrame.class.getName());
	
}

/* @(#)NodeConfigurationMgr.java */
