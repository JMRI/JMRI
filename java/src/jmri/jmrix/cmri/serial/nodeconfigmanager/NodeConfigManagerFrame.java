package jmri.jmrix.cmri.serial.nodeconfigmanager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.jmrix.cmri.serial.nodeiolist.NodeIOListFrame;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Frames for a table view to manage CMRInet node configuration management. c2
 * Created a table view for node configuration operations. Add, Edit, Delete and
 * Update are executed from the NodeTableManager. This class was derived from
 * the NodeConfig class.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 * @author	Dave Duchamp Copyright (C) 2004
 * @author	Chuck Catania Copyright (C) 2013, 2014, 2015, 2016, 2017, 2018
 */
public class NodeConfigManagerFrame extends jmri.util.JmriJFrame {

    ArrayList<SerialNode> cmriNode = new ArrayList<>();
    public int numConfigNodes = 0;

    public int numBits = 48;  // number of bits in assignment table
    public int numInputBits = 24;  // number of input bits for selected node
    public int numOutputBits = 48; // number of output bits for selected node

    protected int selectedNodeAddr = -1;
    protected int selectedTableRow = -1;
    protected boolean doingPrint = false;

    // node select pane items
    JLabel nodeLabel = new JLabel(Bundle.getMessage("NodeBoxLabel") + " ");

    // node table pane items
    protected JPanel nodeTablePanel = null;
    protected Border inputBorder = BorderFactory.createEtchedBorder();

    protected NodeTableModel nodeTableModel = null;
    protected JTable nodeTable = null;

    // button pane items
    JButton addButton = new JButton(Bundle.getMessage("ButtonAdd"));
    JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
    JButton printButton = new JButton(Bundle.getMessage("PrintButtonText"));

    NodeConfigManagerFrame curFrame;

    protected JTextField nodeAddrField = new JTextField(3);
    protected JLabel nodeAddrStatic = new JLabel("000");
    protected JComboBox<String> nodeTypeBox;
    protected JTextField receiveDelayField = new JTextField(3);
    protected JTextField nodeDescription = new JTextField(32);
    protected JTextField pulseWidthField = new JTextField(4);
    protected JComboBox<String> cardSizeBox;
    protected JComboBox<String> cardSize8Box;
    protected JLabel cardSizeText = new JLabel("   " + Bundle.getMessage("LabelCardSize"));
    protected JLabel onBoardBytesText = new JLabel(Bundle.getMessage("LabelOnBoardBytes") + " 3 Input Bytes, 6 Output Bytes");

    protected JButton addNodeButton = new JButton(Bundle.getMessage("ButtonAdd"));
    protected JButton editNodeButton = new JButton(Bundle.getMessage("ButtonEdit"));
    protected JButton deleteNodeButton = new JButton(Bundle.getMessage("ButtonDelete"));
    protected JButton doneNodeButton = new JButton(Bundle.getMessage("ButtonDone"));
    protected JButton updateNodeButton = new JButton(Bundle.getMessage("ButtonUpdate"));
    protected JButton cancelNodeButton = new JButton(Bundle.getMessage("ButtonCancel"));

    protected JLabel statusText1 = new JLabel();
    protected JLabel statusText2 = new JLabel();
    protected JLabel statusText3 = new JLabel();

    protected JPanel panel2 = new JPanel();
    protected JPanel panel2a = new JPanel();
    protected JPanel panel2b = new JPanel();
    protected JPanel panel2c = new JPanel();  //c2 IOX config
    protected JPanel panelnodeDescBox = new JPanel();   //c2 node desctipion box
    protected JPanel panelnodeDesc = new JPanel();  //c2 node description
    protected JPanel panelnetOpt = new JPanel();  //c2 CMRInet options
    protected JPanel panelnetOptBox = new JPanel();  //c2 CMRInet options frame
    protected JPanel panelnodeOpt = new JPanel();   //c2 node options

    protected boolean changedNode = false;  // true if a node was changed, deleted, or added
    protected boolean editMode = false;     // true if in edit mode

    protected SerialNode curNode = null;    // Serial Node being edited
    protected int nodeAddress = 0;          // Node address
    protected int nodeType = SerialNode.SMINI; // Node type - default SMINI
    protected int bitsPerCard = 24;         // number of bits per card
    protected int receiveDelay = 0;         // transmission delay
    protected int pulseWidth = 500;         // pulse width for turnout control (milliseconds)
    protected int num2LSearchLights = 0;    // number of 2-lead oscillating searchlights

    protected int numCards = 0;             //set by consistency check routine
    protected int cpNodeOnboard = 4;        //Number of fixed bytes(cards) for a cpNode
    protected int osNodeOnboard = 8;        //Number of fixed bytes(cards) for a osNode

    protected boolean errorInStatus1 = false;
    protected boolean errorInStatus2 = false;

    protected String editStatus1 = Bundle.getMessage("NotesEdit1");
    protected String editStatus2 = Bundle.getMessage("NotesEdit2");
    protected String editStatus3 = Bundle.getMessage("NotesEdit3");
    protected String addStatus1 = Bundle.getMessage("NotesAdd1");
    protected String addStatus2 = Bundle.getMessage("NotesAdd2");
    protected String addStatus3 = Bundle.getMessage("NotesAdd3");
    protected String delStatus1 = Bundle.getMessage("NotesDel1");
    protected String delStatus2 = Bundle.getMessage("NotesDel2");
    protected String delStatus3 = Bundle.getMessage("NotesDel3");

    protected String nodeDescText = "";
    protected int deleteNodeAddress = 0;

    HandlerClass nodeOptHandler = new HandlerClass();

    // --------------------------
    // CMRInet Options CheckBoxes
    // --------------------------
    protected JCheckBox cbx_cmrinetopt_AUTOPOLL = new JCheckBox(Bundle.getMessage("cmrinetOpt0"), true);
    protected JCheckBox cbx_cmrinetopt_USECMRIX = new JCheckBox(Bundle.getMessage("cmrinetOpt1"));
    protected JCheckBox cbx_cmrinetopt_USEBCC = new JCheckBox(Bundle.getMessage("cmrinetOpt2"));
    protected JCheckBox cbx_cmrinetopt_BIT8 = new JCheckBox(Bundle.getMessage("cmrinetOpt8"));
    protected JCheckBox cbx_cmrinetopt_BIT15 = new JCheckBox(Bundle.getMessage("cmrinetOpt15"));

    // -------------------------
    // cpNode Options CheckBoxes
    // -------------------------
    protected JCheckBox cbx_cpnodeopt_SENDEOT = new JCheckBox(Bundle.getMessage("cpnodeOpt0"));
    protected JCheckBox cbx_cpnodeopt_BIT1 = new JCheckBox(Bundle.getMessage("cpnodeOpt1"));
    protected JCheckBox cbx_cpnodeopt_BIT2 = new JCheckBox(Bundle.getMessage("cpnodeOpt2"));
    protected JCheckBox cbx_cpnodeopt_BIT8 = new JCheckBox(Bundle.getMessage("cpnodeOpt8"));
    protected JCheckBox cbx_cpnodeopt_BIT15 = new JCheckBox(Bundle.getMessage("cpnodeOpt15"));

    private CMRISystemConnectionMemo _memo = null;

    /**
     * Constructor method
     */
    public NodeConfigManagerFrame(CMRISystemConnectionMemo memo) {
        super();
        _memo = memo;
        curFrame = this;

        // Clear information arrays
        for (int i = 0; i < 64; i++) {
            cardType[i] = Bundle.getMessage("CardTypeNone"); // NOI18N
        }
        for (int i = 0; i < 48; i++) {
            searchlightBits[i] = false;
            firstSearchlight[i] = false;
        }
        // addHelpMenu("package.jmri.jmrix.cmri.serial.nodeconfigmanager.NodeConfigManagerFrame", true); // NOI18N duplicate, see initComponents
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        // set the frame's initial state
        setTitle(Bundle.getMessage("WindowTitle") + Bundle.getMessage("WindowConnectionMemo")+_memo.getUserName());  // NOI18N
        setSize(500, 150);

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
        nodeTable.setPreferredScrollableViewportSize(new java.awt.Dimension(300, 350));
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
        numbitsColumn.setMaxWidth(120);
        numbitsColumn.setCellRenderer(dtcen);
        numbitsColumn.setResizable(false);

        TableColumn numinputsColumn = assignmentColumnModel.getColumn(NodeTableModel.NUMINCARDS_COLUMN);
        numinputsColumn.setMinWidth(40);
        numinputsColumn.setMaxWidth(80);
        numinputsColumn.setCellRenderer(dtcen);
        numinputsColumn.setResizable(false);

        TableColumn numoutputsColumn = assignmentColumnModel.getColumn(NodeTableModel.NUMOUTCARDS_COLUMN);
        numoutputsColumn.setMinWidth(10);
        numoutputsColumn.setMaxWidth(100);
        numoutputsColumn.setCellRenderer(dtcen);
        numoutputsColumn.setResizable(false);

        TableColumn numinbytesColumn = assignmentColumnModel.getColumn(NodeTableModel.NUMINBYTES_COLUMN);
        numinbytesColumn.setMinWidth(10);
        numinbytesColumn.setMaxWidth(80);
        numinbytesColumn.setCellRenderer(dtcen);
        numinbytesColumn.setResizable(false);

        TableColumn numoutbytesColumn = assignmentColumnModel.getColumn(NodeTableModel.NUMOUTBYTES_COLUMN);
        numoutbytesColumn.setMinWidth(10);
        numoutbytesColumn.setMaxWidth(100);
        numoutbytesColumn.setCellRenderer(dtcen);
        numoutbytesColumn.setResizable(false);

        TableColumn selectColumn = assignmentColumnModel.getColumn(NodeTableModel.SELECT_COLUMN);
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.addItem(Bundle.getMessage("SelectSelect"));
        comboBox.addItem(Bundle.getMessage("SelectEdit"));
        comboBox.addItem(Bundle.getMessage("SelectInfo"));
        comboBox.addItem(Bundle.getMessage("SelectDelete"));
        selectColumn.setCellEditor(new DefaultCellEditor(comboBox));

        selectColumn.setMinWidth(40);
        selectColumn.setMaxWidth(90);
        selectColumn.setCellRenderer(dtcen);
        selectColumn.setResizable(false);

        TableColumn nodedescColumn = assignmentColumnModel.getColumn(NodeTableModel.NODEDESC_COLUMN);
        nodedescColumn.setMinWidth(40);
        nodedescColumn.setMaxWidth(350);
        nodedescColumn.setResizable(true);
        JScrollPane nodeTableScrollPane = new JScrollPane(nodeTable);

        Border inputBorderTitled = BorderFactory.createTitledBorder(inputBorder,
                " ",
                TitledBorder.LEFT, TitledBorder.ABOVE_TOP);
        nodeTablePanel.add(nodeTableScrollPane, BorderLayout.CENTER);
        nodeTablePanel.setBorder(inputBorderTitled);
        setPreferredSize(new Dimension(950, 550));

        nodeTable.setAutoCreateRowSorter(true);
        nodeTable.getRowSorter().toggleSortOrder(NodeTableModel.NODENUM_COLUMN);

        contentPane.add(nodeTablePanel);

        // Setup main window buttons
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, FlowLayout.RIGHT));
        panel3.setPreferredSize(new Dimension(950, 50));

        // Set up Add button
        addButton.setVisible(true);
        addButton.setToolTipText(Bundle.getMessage("AddButtonTip"));
        addButton.addActionListener((java.awt.event.ActionEvent e) -> {
            addButtonActionPerformed(e);
        });
        panel3.add(addButton);

        // Set up Print button
        printButton.setVisible(true);
        printButton.setToolTipText(Bundle.getMessage("PrintButtonTip"));
        if (numConfigNodes > 0) {
            printButton.addActionListener((java.awt.event.ActionEvent e) -> {
                printButtonActionPerformed(e);
            });
        }
        panel3.add(printButton);

        // Set up Done button
        doneButton.setVisible(true);
        doneButton.setToolTipText(Bundle.getMessage("DoneButtonTip"));
        doneButton.addActionListener((java.awt.event.ActionEvent e) -> {
            doneButtonActionPerformed();
        });
        panel3.add(doneButton);

        contentPane.add(panel3);
        addHelpMenu("package.jmri.jmrix.cmri.serial.nodeconfigmanager.NodeConfigManagerFrame", true);
        // pack for display
        pack();
        nodeTablePanel.setVisible(true);
    }

    /**
     * Get the selected node address from the node table.
     */
    public int getSelectedNodeAddr() {
        return (Integer) nodeTable.getValueAt(nodeTable.getSelectedRow(), 0);
    }

    /**
     * Handle the done button click.
     */
    public void doneButtonActionPerformed() {
        changedNode = false;
        setVisible(false);
        dispose();
    }

    public void addButtonActionPerformed(ActionEvent e) {
        NodeConfigManagerFrame f = new NodeConfigManagerFrame(_memo);
        try {
            f.initNodeConfigWindow();
        } catch (Exception ex) {
            log.info("addButtonActionPerformed Exception-C2: " + ex.toString());
        }
        f.nodeTableModel = nodeTableModel;
        f.initNodeVariables();
        f.buttonSet_ADD();
        f.setLocation(100, 100);
        f.setVisible(true);
    }

    /**
     * Print.
     *
     * @param e the triggering event
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
        (nodeTableModel).printTable(writer, colWidth);
    }

    /**
     * Edit node table selection.
     */
    public void editActionSelected() {
        selectedNodeAddr = getSelectedNodeAddr();

        NodeConfigManagerFrame f = new NodeConfigManagerFrame(_memo);
        f.nodeTableModel = nodeTableModel;
        f.selectedTableRow = nodeTable.convertRowIndexToModel(nodeTable.getSelectedRow());

        try {
            f.initNodeConfigWindow();
            f.editNodeButtonActionPerformed(selectedNodeAddr);
        } catch (Exception ex) {
            log.info("editActionSelected", ex);

        }
        f.setLocation(200, 200);
        f.buttonSet_EDIT();
        f.setVisible(true);
    }

    /**
     * Handle the delete button click
     */
    public void deleteActionSelected() {
        selectedNodeAddr = getSelectedNodeAddr();

        NodeConfigManagerFrame f = new NodeConfigManagerFrame(_memo);
        f.nodeTableModel = nodeTableModel;
        f.selectedTableRow = nodeTable.convertRowIndexToModel(nodeTable.getSelectedRow());
        try {
            f.initNodeConfigWindow();
            f.deleteNodeButtonActionPerformed(selectedNodeAddr);
        } catch (Exception ex) {
            log.info("deleteActionSelected", ex);

        }
        f.setLocation(200, 200);
        f.buttonSet_DELETE();
        f.setVisible(true);
    }

    /**
     * Handle info action.
     */
    public void infoActionSelected() {
        selectedNodeAddr = getSelectedNodeAddr();

        NodeIOListFrame f = new NodeIOListFrame(_memo);
        try {
            f.initComponents();
            f.displayNodeIOBits(selectedNodeAddr);
        } catch (Exception ex) {
            log.info("infoActionSelected Exception-C2: " + ex.toString());

        }
        f.setLocation(100, 100);
        f.setVisible(true);
    }

    /**
     * Method to initialize configured nodes and sets up the node select combo
     * box
     */
    public void initializeNodes() {
        // get all configured nodes
        if (!cmriNode.isEmpty()) {
            cmriNode.clear();
        }

        SerialNode node = (SerialNode) _memo.getTrafficController().getNode(0);
        int index = 1;
        while (node != null) {
            cmriNode.add(node);
            node = (SerialNode) _memo.getTrafficController().getNode(index);
            index++;
        }

        numConfigNodes = cmriNode.size();
    }

    /**
     * Set up table for displaying bit assignments
     */
    public class NodeTableModel extends AbstractTableModel {

        @Override
        public String getColumnName(int c) {
            return nodeTableColumnsNames[c];
        }

        @Override
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
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if (c == SELECT_COLUMN) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int getColumnCount() {
            return NUM_COLUMNS;
        }

        @Override
        public int getRowCount() {
            return cmriNode.size();
        }

        public void removeRow(int row) {
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

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == SELECT_COLUMN) {
                if (Bundle.getMessage("SelectEdit").equals(value)) {
                    editActionSelected();
                } else if (Bundle.getMessage("SelectInfo").equals(value)) {
                    infoActionSelected();
                } else if (Bundle.getMessage("SelectDelete").equals(value)) {
                    deleteActionSelected();
                }
            } else {
                log.info("setValueAt Row" + row + " value " + value);
            }
            fireTableDataChanged();
        }

        @Override
        public Object getValueAt(int r, int c) {
            switch (c) {
                case NODENUM_COLUMN:
                    if (!doingPrint) {
                        return cmriNode.get(r).getNodeAddress();
                    } else {
                        return Integer.toString(cmriNode.get(r).getNodeAddress());
                    }

                case NODETYPE_COLUMN:
                    return "  " + nodeTableTypes[cmriNode.get(r).getNodeType()];

                case NUMBITS_COLUMN:
                    return Integer.toString(cmriNode.get(r).getNumBitsPerCard());

                case NUMINCARDS_COLUMN:
                    if (cmriNode.get(r).getNodeType() == SerialNode.SMINI) {
                        return Integer.toString(cmriNode.get(r).numInputCards() * 3);
                    } else {
                        return Integer.toString(cmriNode.get(r).numInputCards());
                    }

                case NUMOUTCARDS_COLUMN:
                    if (cmriNode.get(r).getNodeType() == SerialNode.SMINI) {
                        return Integer.toString(cmriNode.get(r).numOutputCards() * 3);
                    } else {
                        return Integer.toString(cmriNode.get(r).numOutputCards());
                    }

                case NUMINBYTES_COLUMN:
                    return Integer.toString((cmriNode.get(r).getNumBitsPerCard()) * cmriNode.get(r).numInputCards());

                case NUMOUTBYTES_COLUMN:
                    return Integer.toString((cmriNode.get(r).getNumBitsPerCard()) * cmriNode.get(r).numOutputCards());

                case SELECT_COLUMN:

                    return "Select";
                case NODEDESC_COLUMN:

                    return " " + cmriNode.get(r).getcmriNodeDesc();
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
        public static final int NUM_COLUMNS = NODEDESC_COLUMN + 1;

//        private String[] pollStatus = {"ERROR","IDLE","POLLING","TIMEOUT","SLOW POLL"};
        /**
         * Method to print or print preview the assignment table. Printed in
         * proportionately sized columns across the page with headings and
         * vertical lines between each column. Data is word wrapped within a
         * column. Can only handle 4 columns of data as strings. Adapted from
         * routines in BeanTableDataModel.java by Bob Jacobsen and Dennis Miller
         */
        public void printTable(HardcopyWriter w, int colWidth[]) {
            // determine the column sizes - proportionately sized, with space between for lines
            int[] columnSize = new int[NUM_COLUMNS];
            int charPerLine = w.getCharactersPerLine();
            int tableLineWidth = 0;  // table line width in characters
            int totalColWidth = 0;

            doingPrint = true;
            for (int j = 0; j < NUM_COLUMNS; j++) {
                if (j != SELECT_COLUMN) {
                    totalColWidth += colWidth[j];
                }
            }
            float ratio = ((float) charPerLine) / ((float) totalColWidth);
            for (int j = 0; j < NUM_COLUMNS; j++) {
                if (j != SELECT_COLUMN) {
                    columnSize[j] = ((int) (colWidth[j] * ratio)) - 1;
                    tableLineWidth += (columnSize[j] + 1);
                }
            }

            // Draw horizontal dividing line
            w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(), tableLineWidth);

            // print the column header labels
            String[] columnStrings = new String[NUM_COLUMNS];
            // Put each column header in the array
            for (int i = 0; i < NUM_COLUMNS; i++) {
                if (i != SELECT_COLUMN) {
                    columnStrings[i] = this.getColumnName(i);
                }
            }
            //          w.setFontStyle(Font.BOLD);
            printColumns(w, columnStrings, columnSize);
            w.setFontStyle(0);

            // draw horizontal line
            w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(), tableLineWidth);
            // now print each row of data
            String[] spaces = new String[NUM_COLUMNS];
            // create base strings the width of each of the columns
            for (int k = 0; k < NUM_COLUMNS; k++) {
                if (k != SELECT_COLUMN) {
                    spaces[k] = "";
                    for (int i = 0; i < columnSize[k]; i++) {
                        spaces[k] = spaces[k] + " ";
                    }
                }
            }
            for (int i = 0; i < this.getRowCount(); i++) {
                for (int j = 0; j < NUM_COLUMNS; j++) {
                    if (j != SELECT_COLUMN) {
                        //check for special, null contents
                        if (this.getValueAt(i, j) == null) {
                            columnStrings[j] = spaces[j];
                        } else {
                            columnStrings[j] = (String) this.getValueAt(i, j);
                        }
                    }
                }
                printColumns(w, columnStrings, columnSize);
                // draw horizontal line
                w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(), tableLineWidth);
            }
            doingPrint = false;
            w.close();
        }

        protected void printColumns(HardcopyWriter w, String columnStrings[], int columnSize[]) {
            String columnString = "";
            StringBuilder lineString = new StringBuilder("");
            String[] spaces = new String[NUM_COLUMNS];
            // create base strings the width of each of the columns
            for (int k = 0; k < NUM_COLUMNS; k++) {
                if (k != SELECT_COLUMN) {
                    spaces[k] = "";
                    for (int i = 0; i < columnSize[k]; i++) {
                        spaces[k] = spaces[k] + " ";
                    }
                }
            }
            // loop through each column
            boolean complete = false;
            while (!complete) {
                complete = true;
                for (int i = 0; i < NUM_COLUMNS; i++) {
                    if (i != SELECT_COLUMN) {
                        // if the column string is too wide cut it at word boundary (valid delimiters are space, - and _)
                        // use the initial part of the text,pad it with spaces and place the remainder back in the array
                        // for further processing on next line
                        // if column string isn't too wide, pad it to column width with spaces if needed
                        if (columnStrings[i].length() > columnSize[i]) {
                            // this column string will not fit on one line
                            boolean noWord = true;
                            for (int k = columnSize[i]; k >= 1; k--) {
                                if (columnStrings[i].substring(k - 1, k).equals(" ")
                                        || columnStrings[i].substring(k - 1, k).equals("-")
                                        || columnStrings[i].substring(k - 1, k).equals("_")) {
                                    columnString = columnStrings[i].substring(0, k)
                                            + spaces[i].substring(columnStrings[i].substring(0, k).length());
                                    columnStrings[i] = columnStrings[i].substring(k);
                                    noWord = false;
                                    complete = false;
                                    break;
                                }
                            }
                            if (noWord) {
                                columnString = columnStrings[i].substring(0, columnSize[i]);
                                columnStrings[i] = columnStrings[i].substring(columnSize[i]);
                                complete = false;
                            }
                        } else {
                            // this column string will fit on one line
                            columnString = columnStrings[i] + spaces[i].substring(columnStrings[i].length());
                            columnStrings[i] = "";
                        }
                        lineString.append(columnString).append(" ");
                    }
                }
                try {
                    w.write(lineString.toString());
                    //write vertical dividing lines
                    int iLine = w.getCurrentLineNumber();
                    for (int i = 0, k = 0; i < w.getCharactersPerLine(); k++) {
                        if (k != SELECT_COLUMN) {
                            w.write(iLine, i, iLine + 1, i);
                            if (k < NUM_COLUMNS) {
                                i = i + columnSize[k] + 1;
                            } else {
                                i = w.getCharactersPerLine();
                            }
                        }
                    }
                    w.write("\n");
                    lineString = new StringBuilder("");
                } catch (IOException e) {
                    log.warn("error during printing: ", e);
                }
            }
        }
    }

    private String[] nodeTableColumnsNames
            = {"Address", "   Type", "Bits per Card", "IN Cards", "OUT Cards", "IN Bits", "OUT Bits", " ", "  Description"};

    private String[] nodeTableTypes = {"--", "SMINI", "SUSIC", "CPNODE", "CPMEGA"};

    /*
     * ----------------------------------------------------------
     *    ------ Node Configuration Management Routines ------
     * ----------------------------------------------------------
     */
    public void initNodeVariables() {
        // Clear information arrays
        for (int i = 0; i < 64; i++) {
            cardType[i] = Bundle.getMessage("CardTypeNone");
        }

        //cpMega onboard bytes held in a separate array and will be copied
        //to CardArray.
        for (int i = 0; i < 16; i++) {
            onBoardType[i] = Bundle.getMessage("CardTypeNone");
        }

        for (int i = 0; i < 48; i++) {
            searchlightBits[i] = false;
            firstSearchlight[i] = false;
        }
        for (int i = 0; i < SerialNode.NUMCMRINETOPTS; i++) {
            cmrinetOpts[i] = 0;
        }
        for (int i = 0; i < SerialNode.NUMCPNODEOPTS; i++) {
            cpnodeOpts[i] = 0;
        }
        nodeDescText = "";
    }

    /**
     * Initialize the node configuration window This window is a template for
     * ADD,EDIT,DELETE node operations
     */
    public void initNodeConfigWindow() {
        setTitle(Bundle.getMessage("WindowTitle"));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Set up node address and node type
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        JPanel panel11 = new JPanel();
        panel11.setLayout(new FlowLayout());
        panel11.add(new JLabel(Bundle.getMessage("LabelNodeAddress") + " "));
        panel11.add(nodeAddrField);
        nodeAddrField.setToolTipText(Bundle.getMessage("TipNodeAddress"));
        nodeAddrField.setText("-1");
        panel11.add(nodeAddrStatic);
        nodeAddrStatic.setVisible(false);
        panel11.add(new JLabel("   " + Bundle.getMessage("LabelNodeType") + " "));
        nodeTypeBox = new JComboBox<>();
        panel11.add(nodeTypeBox);
        nodeTypeBox.addItem("SMINI");
        nodeTypeBox.addItem("USIC_SUSIC");
        nodeTypeBox.addItem("CPNODE");

        //  Hide the menu item until MRCS are ready to release the cpMega
        //  nodeTypeBox.addItem("CPMEGA");

        /*
         * Here add code for other types of nodes
         */
        nodeTypeBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                String s = (String) nodeTypeBox.getSelectedItem();

                if (s.equals("SMINI")) {
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
                    nodeType = SerialNode.SMINI;
                    onBoardBytesText.setText(Bundle.getMessage("LabelOnBoardBytes") + " 3 Input, 6 Output");
                } else if (s.equals("USIC_SUSIC")) {
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
                    nodeType = SerialNode.USIC_SUSIC;
                    onBoardBytesText.setText("  ");
                } else if (s.equals("CPNODE")) {
                    panel2.setVisible(false);
                    panel2a.setVisible(false);
                    panel2b.setVisible(true);
                    panel2c.setVisible(false);   // IOX bytes
                    cardSizeText.setVisible(true);
                    cardSizeBox.setVisible(false);
                    cardSize8Box.setVisible(true);
                    panelnodeDescBox.setVisible(true);
                    panelnodeDesc.setVisible(true);
                    panelnetOpt.setVisible(true);
                    panelnetOptBox.setVisible(true);
                    panelnodeOpt.setVisible(true);
                    nodeType = SerialNode.CPNODE;
                    onBoardBytesText.setText(Bundle.getMessage("LabelOnBoardBytes") + " 2 Bytes");
                } else if (s.equals("CPMEGA")) {
                    panel2.setVisible(false);
                    panel2a.setVisible(false);
                    panel2b.setVisible(true);
                    panel2c.setVisible(true);   // IOX bytes
                    cardSizeText.setVisible(true);
                    cardSizeBox.setVisible(false);
                    cardSize8Box.setVisible(true);
                    panelnodeDescBox.setVisible(true);
                    panelnodeDesc.setVisible(true);
                    panelnetOpt.setVisible(true);
                    panelnetOptBox.setVisible(true);
                    panelnodeOpt.setVisible(true);
                    nodeType = SerialNode.CPMEGA;
                    onBoardBytesText.setText(Bundle.getMessage("LabelOnBoardBytes") + " 8 Bytes");
                }
                /*
                 * Here add code for other types of nodes
                 */

                // reset notes as appropriate
                resetNotes();
            }
        });
        nodeTypeBox.setToolTipText(Bundle.getMessage("TipNodeType"));

        JPanel panel12 = new JPanel();
        panel12.setLayout(new FlowLayout());
        panel12.add(new JLabel(Bundle.getMessage("LabelDelay") + " "));
        panel12.add(receiveDelayField);
        receiveDelayField.setToolTipText(Bundle.getMessage("TipDelay"));
        receiveDelayField.setText("0");
        panel12.add(cardSizeText);
        cardSizeBox = new JComboBox<>();
        cardSize8Box = new JComboBox<>();
        panel12.add(cardSizeBox);
        panel12.add(cardSize8Box);
        cardSizeBox.addItem(Bundle.getMessage("CardSize24"));
        cardSizeBox.addItem(Bundle.getMessage("CardSize32"));
        cardSize8Box.addItem(Bundle.getMessage("CardSize8"));
        /*
         * Here add code for other types of nodes
         */

        cardSizeBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                String s = (String) cardSizeBox.getSelectedItem();
                if (s.equals(Bundle.getMessage("CardSize24"))) {
                    bitsPerCard = 24;
                } else if (s.equals(Bundle.getMessage("CardSize32"))) {
                    bitsPerCard = 32;
                } else if (s.equals(Bundle.getMessage("CardSize8"))) {
                    bitsPerCard = 8;
                }
                // here add code for other node types, if required
            }
        });
        cardSizeBox.setToolTipText(Bundle.getMessage("TipCardSize"));
        cardSizeText.setVisible(false);
        cardSizeBox.setVisible(false);
        cardSize8Box.setVisible(false);

        JPanel panel13 = new JPanel();
        panel13.setLayout(new FlowLayout());
        panel13.add(new JLabel(Bundle.getMessage("LabelPulseWidth") + " "));
        panel13.add(pulseWidthField);
        pulseWidthField.setToolTipText(Bundle.getMessage("TipPulseWidth"));
        pulseWidthField.setText("500");
        panel13.add(new JLabel(Bundle.getMessage("LabelMilliseconds")));

        JPanel panel14 = new JPanel();
        panel14.add(onBoardBytesText);
        panel14.setVisible(true);

        // Load the top half of the common window
        panel1.add(panel11);
        panel1.add(panel12);
        panel1.add(panel13);
        panel1.add(panel14);
        contentPane.add(panel1);

        // Set up USIC/SUSIC card type configuration table
        JPanel panel21 = new JPanel();
        panel21.setLayout(new BoxLayout(panel21, BoxLayout.Y_AXIS));
        panel21.add(new JLabel(Bundle.getMessage("HintCardTypePartA")));
        panel21.add(new JLabel(" " + Bundle.getMessage("HintCardTypePartB")));
        panel21.add(new JLabel(" " + Bundle.getMessage("HintCardTypePartC")));
        panel21.add(new JLabel("   "));
        panel21.add(new JLabel(Bundle.getMessage("HintCardTypePartD")));
        panel21.add(new JLabel(" " + Bundle.getMessage("HintCardTypePartE")));
        panel21.add(new JLabel(" " + Bundle.getMessage("HintCardTypePartF")));
        panel2.add(panel21);
        TableModel cardConfigModel = new CardConfigModel();
        JTable cardConfigTable = new JTable(cardConfigModel);
        cardConfigTable.setRowSelectionAllowed(false);
        cardConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180, 95));

        JComboBox<String> cardTypeCombo = new JComboBox<>();
        cardTypeCombo.addItem(Bundle.getMessage("CardTypeOutput"));
        cardTypeCombo.addItem(Bundle.getMessage("CardTypeInput"));
        cardTypeCombo.addItem(Bundle.getMessage("CardTypeNone"));

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
        panel2.add(cardScrollPane, BorderLayout.CENTER);
        contentPane.add(panel2);
        panel2.setVisible(false);

        // Set up SMINI oscillating 2-lead searchlight configuration table
        JPanel panel2a1 = new JPanel();
        panel2a1.setLayout(new BoxLayout(panel2a1, BoxLayout.Y_AXIS));
        panel2a1.add(new JLabel(Bundle.getMessage("HintSearchlightPartA")));
        panel2a1.add(new JLabel(" " + Bundle.getMessage("HintSearchlightPartB")));
        panel2a1.add(new JLabel(" " + Bundle.getMessage("HintSearchlightPartC")));
        panel2a1.add(new JLabel("   "));
        panel2a1.add(new JLabel(Bundle.getMessage("HintSearchlightPartD")));
        panel2a1.add(new JLabel(" " + Bundle.getMessage("HintSearchlightPartE")));
        panel2a1.add(new JLabel(" " + Bundle.getMessage("HintSearchlightPartF")));
        panel2a.add(panel2a1);

        TableModel searchlightConfigModel = new SearchlightConfigModel();
        JTable searchlightConfigTable = new JTable(searchlightConfigModel);
        searchlightConfigTable.setRowSelectionAllowed(false);
        searchlightConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(208, 100));
        TableColumnModel searchlightColumnModel = searchlightConfigTable.getColumnModel();
        TableColumn portColumn = searchlightColumnModel.getColumn(SearchlightConfigModel.PORT_COLUMN);
        portColumn.setMinWidth(90);
        portColumn.setMaxWidth(100);
        JScrollPane searchlightScrollPane = new JScrollPane(searchlightConfigTable);
        panel2a.add(searchlightScrollPane, BorderLayout.CENTER);
        contentPane.add(panel2a);
        panel2.setVisible(false);

        // Set up CPMEGA on board byte I/O assignments
        JPanel panel2b3 = new JPanel();
        panel2b3.setLayout(new BoxLayout(panel2b3, BoxLayout.Y_AXIS));
        panel2b3.add(new JLabel("Assign Onboard Bytes"));
        panel2c.add(panel2b3);

        TableModel osnodeConfigModel = new OSnodeConfigModel();
        JTable osnodeConfigTable = new JTable(osnodeConfigModel);
        osnodeConfigTable.setRowSelectionAllowed(false);
        osnodeConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(170, 95)); //160

        JComboBox<String> osnodeTypeCombo = new JComboBox<>();
        osnodeTypeCombo.addItem(Bundle.getMessage("CardTypeOutput"));
        osnodeTypeCombo.addItem(Bundle.getMessage("CardTypeInput"));
        osnodeTypeCombo.addItem(Bundle.getMessage("CardTypeNone"));

        TableColumnModel osnodePortModel = osnodeConfigTable.getColumnModel();
        TableColumn x11Column = osnodePortModel.getColumn(OSnodeConfigModel.CARDNUM_COLUMN);
        x11Column.setMinWidth(50);
        x11Column.setMaxWidth(90);
        TableColumn x21Column = osnodePortModel.getColumn(OSnodeConfigModel.CARDTYPE_COLUMN);
        x21Column.setCellEditor(new DefaultCellEditor(osnodeTypeCombo));
        x21Column.setResizable(false);
        x21Column.setMinWidth(80);
        x21Column.setMaxWidth(100);

        JScrollPane osnodeScrollPane = new JScrollPane(osnodeConfigTable);
        panel2c.add(osnodeScrollPane, BorderLayout.CENTER);
        contentPane.add(panel2c);
        panel2.setVisible(false);

        // Set up I/O Expander (IOX) port assignments
        JPanel panel2b1 = new JPanel();
        panel2b1.setLayout(new BoxLayout(panel2b1, BoxLayout.Y_AXIS));
        panel2b1.add(new JLabel("Assign IOX Ports"));

        panel2b.add(panel2b1);

        TableModel cpnodeConfigModel = new CPnodeConfigModel();
        JTable cpnodeConfigTable = new JTable(cpnodeConfigModel);
        cpnodeConfigTable.setRowSelectionAllowed(false);
        cpnodeConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(240, 130)); //160

        JComboBox<String> cpnodeTypeCombo = new JComboBox<>();
        cpnodeTypeCombo.addItem(Bundle.getMessage("CardTypeOutput"));
        cpnodeTypeCombo.addItem(Bundle.getMessage("CardTypeInput"));
        cpnodeTypeCombo.addItem(Bundle.getMessage("CardTypeNone"));

        TableColumnModel cpnodePortModel = cpnodeConfigTable.getColumnModel();
        TableColumn x0Column = cpnodePortModel.getColumn(CPnodeConfigModel.CARD_COLUMN);
        x0Column.setMinWidth(30);
        x0Column.setMaxWidth(50);
        TableColumn x1Column = cpnodePortModel.getColumn(CPnodeConfigModel.CARDNUM_COLUMN);
        x1Column.setMinWidth(70);
        x1Column.setMaxWidth(120);
        TableColumn x2Column = cpnodePortModel.getColumn(CPnodeConfigModel.CARDTYPE_COLUMN);
        x2Column.setCellEditor(new DefaultCellEditor(cpnodeTypeCombo));
        x2Column.setResizable(false);
        x2Column.setMinWidth(80);
        x2Column.setMaxWidth(100);

        JScrollPane cpnodeScrollPane = new JScrollPane(cpnodeConfigTable);
        panel2b.add(cpnodeScrollPane, BorderLayout.CENTER);
        contentPane.add(panel2b);
        panel2b.setVisible(false);

        // node Description field - all node types have this field
        panelnodeDescBox.setLayout(new BoxLayout(panelnodeDescBox, BoxLayout.Y_AXIS));
        panelnodeDesc.setLayout(new FlowLayout());
        panelnodeDesc.add(new JLabel("Description:"));
        nodeDescription.setVisible(true);
        panelnodeDesc.add(nodeDescription);
        panelnodeDesc.setVisible(true);
        contentPane.add(panelnodeDesc);

        // Set up CMRInet Options
        panelnetOpt.setLayout(new GridLayout(0, 2));

        panelnetOpt.add(cbx_cmrinetopt_AUTOPOLL);
        cbx_cmrinetopt_AUTOPOLL.addItemListener(nodeOptHandler);

        panelnetOpt.add(cbx_cmrinetopt_USECMRIX);
        cbx_cmrinetopt_USECMRIX.addItemListener(nodeOptHandler);
        cbx_cmrinetopt_USECMRIX.setVisible(false);

        panelnetOpt.add(cbx_cmrinetopt_USEBCC);
        cbx_cmrinetopt_USEBCC.addItemListener(nodeOptHandler);
        cbx_cmrinetopt_USEBCC.setVisible(false);

        panelnetOpt.add(cbx_cmrinetopt_BIT8);
        cbx_cmrinetopt_BIT8.addItemListener(nodeOptHandler);
        cbx_cmrinetopt_BIT8.setVisible(false);

        panelnetOpt.add(cbx_cmrinetopt_BIT15);
        cbx_cmrinetopt_BIT15.addItemListener(nodeOptHandler);
        cbx_cmrinetopt_BIT15.setVisible(false);

        Border panelnetOptBorder = BorderFactory.createEtchedBorder();
        Border panelnetOptTitled = BorderFactory.createTitledBorder(panelnetOptBorder, "CMRInet Options");
        panelnetOpt.setBorder(panelnetOptTitled);
        panelnetOpt.setVisible(true);
        contentPane.add(panelnetOpt);

        panelnetOptBox.add(Box.createHorizontalStrut(50));
        contentPane.add(panelnetOptBox);

        // Set up cpNode Options
        panelnodeOpt.setLayout(new GridLayout(0, 2));
        panelnodeOpt.add(cbx_cpnodeopt_SENDEOT);
        cbx_cpnodeopt_SENDEOT.addItemListener(nodeOptHandler);
        cbx_cpnodeopt_SENDEOT.setVisible(true);

        panelnodeOpt.add(cbx_cpnodeopt_BIT1);
        cbx_cpnodeopt_BIT1.addItemListener(nodeOptHandler);
        cbx_cpnodeopt_BIT1.setVisible(false);

        panelnodeOpt.add(cbx_cpnodeopt_BIT2);
        cbx_cpnodeopt_BIT2.addItemListener(nodeOptHandler);
        cbx_cpnodeopt_BIT2.setVisible(false);

        panelnodeOpt.add(cbx_cpnodeopt_BIT8);
        cbx_cpnodeopt_BIT8.addItemListener(nodeOptHandler);
        cbx_cpnodeopt_BIT8.setVisible(false);

        panelnodeOpt.add(cbx_cpnodeopt_BIT15);
        cbx_cpnodeopt_BIT15.addItemListener(nodeOptHandler);
        cbx_cpnodeopt_BIT15.setVisible(false);

        Border panelnodeOptBorder = BorderFactory.createEtchedBorder();
        Border panelnodeOptTitled = BorderFactory.createTitledBorder(panelnodeOptBorder, "cpNode Options");
        panelnodeOpt.setBorder(panelnodeOptTitled);
        panelnodeOpt.setVisible(false);
        contentPane.add(panelnodeOpt);

        // Set up the notes area panel for various message displays
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
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border, Bundle.getMessage("BoxLabelNotes"));
        panel3.setBorder(panel3Titled);
        contentPane.add(panel3);

        // Set up the functions buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());

        addNodeButton.setText(Bundle.getMessage("ButtonAdd"));
        addNodeButton.setVisible(false);
        addNodeButton.setToolTipText(Bundle.getMessage("TipAddButton"));
        addNodeButton.addActionListener((java.awt.event.ActionEvent e) -> {
            addNodeButtonActionPerformed();
        });
        panel4.add(addNodeButton);

        deleteNodeButton.setText(Bundle.getMessage("ButtonDelete"));
        deleteNodeButton.setVisible(false);
        deleteNodeButton.setToolTipText(Bundle.getMessage("TipDeleteButton"));
        deleteNodeButton.addActionListener((java.awt.event.ActionEvent e) -> {
            deleteNodeButtonActionConfirm();
        });
        panel4.add(deleteNodeButton);

        updateNodeButton.setText(Bundle.getMessage("ButtonUpdate"));
        updateNodeButton.setVisible(false);
        updateNodeButton.setToolTipText(Bundle.getMessage("TipUpdateButton"));
        updateNodeButton.addActionListener((java.awt.event.ActionEvent e) -> {
            updateNodeButtonActionPerformed();
        });
        panel4.add(updateNodeButton);

        cancelNodeButton.setText(Bundle.getMessage("ButtonCancel"));
        cancelNodeButton.setVisible(false);
        cancelNodeButton.setToolTipText(Bundle.getMessage("TipCancelButton"));
        cancelNodeButton.addActionListener((java.awt.event.ActionEvent e) -> {
            cancelNodeButtonActionPerformed();
        });
        panel4.add(cancelNodeButton);

        doneNodeButton.setText(Bundle.getMessage("ButtonDone"));
        doneNodeButton.setVisible(false);
        doneNodeButton.setToolTipText(Bundle.getMessage("TipDoneButton"));
        doneNodeButton.addActionListener((java.awt.event.ActionEvent e) -> {
            doneNodeButtonActionPerformed();
        });
        panel4.add(doneNodeButton);

        contentPane.add(panel4);
        pack();
    }

    /*
     * -------------------------------------------------------
     * Methods to set the correct states of the window buttons
     * -------------------------------------------------------
     */
    public void buttonSet_ADD() {
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

    public void buttonSet_EDIT() {
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

    public void buttonSet_DELETE() {
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
        // Check that a node with this address does not exist
        curNode = null;
        int nodeAddress = readNodeAddress();
        if (nodeAddress < 0) {
            return;
        }

        // get a SerialNode corresponding to this node address if one exists
        curNode = (SerialNode) _memo.getTrafficController().getNodeFromAddress(nodeAddress);
        // curNode can never be null at this point. Was this intended to catch
        // an exception?
        if (curNode != null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error1", Integer.toString(nodeAddress)),
                    "", JOptionPane.ERROR_MESSAGE);

            statusText1.setText(Bundle.getMessage("Error1", Integer.toString(nodeAddress)));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }

        // get node information from window and check for data consistency
        if (!readReceiveDelay()) {
            return;
        }
        if (!readPulseWidth()) {
            return;
        }
        if (!checkConsistency()) {
            return;
        }

        // all ready, create the new node
        curNode = new SerialNode(nodeAddress, nodeType, _memo.getTrafficController());
        nodeTableModel.addRow(curNode);

        // configure the new node
        setNodeParameters();

        // register any orphan sensors that this node may have
        if (_memo.getSensorManager() != null) {
            (_memo.getSensorManager()).registerSensorsForNode(curNode);
        }

        // reset text displays after succefully adding node
        resetNotes();
        changedNode = true;
        log.info("changedNode = "+changedNode);
        // provide user feedback
        statusText1.setText(Bundle.getMessage("FeedBackAdd") + " " + Integer.toString(nodeAddress));
        statusText2.setVisible(false);
        statusText3.setVisible(true);
        doneNodeButton.setVisible(true);
        cancelNodeButton.setVisible(false);
        errorInStatus1 = true;
    }

    /**
     * Load all of the configured node information from the serial node class.
     *
     * @param nodeaddr the node address
     */
    public void setupNodeInformationWindow(int nodeaddr) {
        // Find Serial Node address
        nodeAddress = nodeaddr;
        if (nodeAddress < 0) {
            return;
        }

        // get the SerialNode corresponding to this node address
        curNode = (SerialNode) _memo.getTrafficController().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(Bundle.getMessage("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }

        // Set up static node address from the table, cannot be changed
        nodeAddrStatic.setText(Integer.toString(nodeAddress));
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(true);

        // get information for this node and set up combo box
        nodeType = curNode.getNodeType();
        switch (nodeType) {

            // SMINI
            case SerialNode.SMINI:
                nodeTypeBox.setSelectedItem("SMINI");
                bitsPerCard = 24;
                cardSizeBox.setSelectedItem(Bundle.getMessage("CardSize24"));
                onBoardBytesText.setText(Bundle.getMessage("LabelOnBoardBytes") + " 3 Input Bytes, 6 Output Bytes");
                // set up the searchlight arrays
                num2LSearchLights = 0;
                for (int i = 0; i < 48; i++) {
                    if (curNode.isSearchLightBit(i)) {
                        searchlightBits[i] = true;
                        searchlightBits[i + 1] = true;
                        firstSearchlight[i] = true;
                        firstSearchlight[i + 1] = false;
                        num2LSearchLights++;
                        i++;
                    } else {
                        searchlightBits[i] = false;
                        firstSearchlight[i] = false;
                    }
                }
                break;

            // USIC/SUSIC
            case SerialNode.USIC_SUSIC:
                nodeTypeBox.setSelectedItem("USIC_SUSIC");
                bitsPerCard = curNode.getNumBitsPerCard();
                if (bitsPerCard == 24) {
                    cardSizeBox.setSelectedItem(Bundle.getMessage("CardSize24"));
                }
                if (bitsPerCard == 32) {
                    cardSizeBox.setSelectedItem(Bundle.getMessage("CardSize32"));
                }
                onBoardBytesText.setText("  ");

                break;

            // CPNODE
            case SerialNode.CPNODE:
                nodeTypeBox.setSelectedItem("CPNODE");
                bitsPerCard = 8;
                cardSize8Box.setSelectedItem(Bundle.getMessage("CardSize8"));
                onBoardBytesText.setText(Bundle.getMessage("LabelOnBoardBytes") + " 2 Bytes");

                // cpNode Options
                cbx_cpnodeopt_SENDEOT.setSelected(curNode.iscpnodeBit(SerialNode.optbitNode_SENDEOT));
                cbx_cpnodeopt_BIT1.setSelected(false);
                cbx_cpnodeopt_BIT2.setSelected(false);
                cbx_cpnodeopt_BIT8.setSelected(curNode.iscpnodeBit(SerialNode.optbitNode_BIT8));
                cbx_cpnodeopt_BIT15.setSelected(curNode.iscpnodeBit(SerialNode.optbitNode_BIT15));
                break;

            // CPMEGA
            case SerialNode.CPMEGA:
                nodeTypeBox.setSelectedItem("CPMEGA");
                bitsPerCard = 8;
                cardSize8Box.setSelectedItem(Bundle.getMessage("CardSize8"));
                onBoardBytesText.setText(Bundle.getMessage("LabelOnBoardBytes") + " 8 Bytes");

                // cpMega Options
                cbx_cpnodeopt_SENDEOT.setSelected(curNode.iscpnodeBit(SerialNode.optbitNode_SENDEOT));
                cbx_cpnodeopt_BIT1.setSelected(false);
                cbx_cpnodeopt_BIT2.setSelected(false);
                cbx_cpnodeopt_BIT8.setSelected(curNode.iscpnodeBit(SerialNode.optbitNode_BIT8));
                cbx_cpnodeopt_BIT15.setSelected(curNode.iscpnodeBit(SerialNode.optbitNode_BIT15));
                break;

            default:
                log.error("Unknown Node Type {}", nodeType);
                break;
        }

        // CMRInet Options for all node types
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
        for (int i = 0; i < 64; i++) {
            if (curNode.isOutputCard(i)) {
                cardType[i] = Bundle.getMessage("CardTypeOutput");
            } else if (curNode.isInputCard(i)) {
                cardType[i] = Bundle.getMessage("CardTypeInput");
            } else {
                cardType[i] = Bundle.getMessage("CardTypeNone");
            }
        }

        if (nodeType == SerialNode.CPMEGA) {
            for (int i = 0; i < 8; i++) // Remap the onboard bytes
            {
                if (curNode.isOutputCard(i)) {
                    onBoardType[i] = Bundle.getMessage("CardTypeOutput");
                } else if (curNode.isInputCard(i)) {
                    onBoardType[i] = Bundle.getMessage("CardTypeInput");
                } else {
                    onBoardType[i] = Bundle.getMessage("CardTypeNone");
                }
            }
        }

        // ensure that table displays correctly
        panel2.setVisible(false);
        panel2a.setVisible(false);
        if (nodeType == SerialNode.USIC_SUSIC) {
            panel2.setVisible(true);
        } else if (nodeType == SerialNode.SMINI) {
            panel2a.setVisible(true);
        } else if (nodeType == SerialNode.CPNODE) {
            panel2c.setVisible(false);
            panel2b.setVisible(true);
        } else if (nodeType == SerialNode.CPMEGA) {
            panel2c.setVisible(true);
            panel2b.setVisible(false);
        }

    }

    /**
     * Create and load edit function window.
     *
     * @param nodeaddr the node address
     */
    public void editNodeButtonActionPerformed(int nodeaddr) {
        // Find Serial Node address

        nodeAddress = nodeaddr;
        if (nodeAddress < 0) {
            return;
        }

        // get the SerialNode corresponding to this node address
        curNode = (SerialNode) _memo.getTrafficController().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(Bundle.getMessage("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }

        // Load the node data into the window
        setupNodeInformationWindow(nodeAddress);

        // Switch to edit notes
        editMode = true;
        statusText1.setText(editStatus1);
        statusText1.setVisible(true);
        statusText2.setText(editStatus2);
        statusText2.setVisible(true);
        statusText3.setText(editStatus3);
        statusText3.setVisible(true);
    }

    /**
     * Handle update button clicked.
     */
    public void updateNodeButtonActionPerformed() {
        // get node information from window
        if (!readReceiveDelay()) {
            return;
        }
        if (!readPulseWidth()) {
            return;
        }

        // check consistency of node information
        if (!checkConsistency()) {
            return;
        }

        // update node information
        if (curNode.getNodeType() != nodeType) {
            // node type has changed
            curNode.setNodeType(nodeType);
        }

        // cmri node description  c2
        curNode.setcmriNodeDesc(nodeDescription.getText());
        setNodeParameters();
        nodeTableModel.changeRow(selectedTableRow, curNode);

        // Switch buttons
        changedNode = true;
        doneNodeButton.setVisible(true);
        updateNodeButton.setVisible(true);
        cancelNodeButton.setVisible(true);
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(true);

        // provide user feedback
        statusText1.setText(Bundle.getMessage("FeedBackUpdate") + " " + Integer.toString(nodeAddress));
        statusText2.setVisible(false);
        cancelNodeButton.setVisible(false);
        errorInStatus1 = true;
    }

    /**
     * Handle delete button pressed.
     */
    public void deleteNodeButtonActionConfirm() {
        // confirm deletion with the user

        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
                this, Bundle.getMessage("ConfirmDelete1") + "\n"
                + Bundle.getMessage("ConfirmDelete2"), Bundle.getMessage("ConfirmDeleteTitle"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE)) {

            // delete this node
            _memo.getTrafficController().deleteNode(deleteNodeAddress);
            nodeTableModel.removeRow(selectedTableRow);

            initializeNodes();

            // provide user feedback
            resetNotes();
            statusText1.setText(Bundle.getMessage("FeedBackDelete") + " " + Integer.toString(deleteNodeAddress));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            changedNode = true;
            deleteNodeButton.setVisible(false);
            doneNodeButton.setVisible(true);
            cancelNodeButton.setVisible(false);
            statusText2.setVisible(false);
        } else {
            // reset as needed
            resetNotes();
        }
    }

    /**
     * Set up delete node window.
     *
     * @param nodeAddr the node address
     */
    public void deleteNodeButtonActionPerformed(int nodeAddr) {
        // Set up static node address
        nodeAddress = nodeAddr;
        nodeAddrStatic.setText(Integer.toString(nodeAddress));
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(true);

        // Find Serial Node address
        if (nodeAddress < 0) {
            log.info("nodeAddress < 0");
            return;
        }

        // get the SerialNode corresponding to this node address
        curNode = (SerialNode) _memo.getTrafficController().getNodeFromAddress(nodeAddress);
        deleteNodeAddress = nodeAddr;
        // Load the node data into the window

        setupNodeInformationWindow(nodeAddress);

        // get the node corresponding to this node address
        if (curNode == null) {
            statusText1.setText(Bundle.getMessage("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
        } else {
            statusText1.setText(Bundle.getMessage("NotesDel3"));
            statusText1.setVisible(true);
        }
    }

    /**
     * Handle done button clicked.
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
        if (changedNode) {
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("Reminder1") + "\n" + Bundle.getMessage("Reminder2"),
                    Bundle.getMessage("ReminderTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
        changedNode = false;

        setVisible(false);
        dispose();        
    }

    /**
     * Handle cancel button clicked.
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

    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        doneNodeButtonActionPerformed();
        super.windowClosing(e);
    }

    /**
     * Set the node parameters by type. Some parameters are specific to a
     * particular node type.
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
            case SerialNode.SMINI:

                // Note: most parameters are set by default on creation
                int numSet = 0;
                // Configure 2-lead oscillating searchlights - first clear unneeded searchlights
                for (int j = 0; j < 47; j++) {
                    if (curNode.isSearchLightBit(j)) {
                        if (!firstSearchlight[j]) {
                            // this is the first bit of a deleted searchlight - clear it
                            curNode.clear2LeadSearchLight(j);
                            // skip over the second bit of the cleared searchlight
                            j++;
                        } else {
                            // this is the first bit of a kept searchlight - skip second bit
                            j++;
                        }
                    }
                }
                // Add needed searchlights that are not already configured
                for (int i = 0; i < 47; i++) {
                    if (firstSearchlight[i]) {
                        if (!curNode.isSearchLightBit(i)) {
                            // this is the first bit of an added searchlight
                            curNode.set2LeadSearchLight(i);
                        }
                        numSet++;
                    }
                }
                // consistency check
                if (numSet != num2LSearchLights) {
                    log.error("Inconsistent numbers of 2-lead searchlights. numSet = "
                            + Integer.toString(numSet) + ", num2LSearchLights = "
                            + Integer.toString(num2LSearchLights));
                }

                // Force created node to be polled as the default
                curNode.setOptNet_AUTOPOLL(1);
                break;

            // USIC/SUSIC
            case SerialNode.USIC_SUSIC:
                // set number of bits per card
                curNode.setNumBitsPerCard(bitsPerCard);
                // configure the input/output cards
                numInput = 0;
                numOutput = 0;
                for (int i = 0; i < 64; i++) {
                    if ("No Card".equals(cardType[i])) {
                        curNode.setCardTypeByAddress(i, SerialNode.NO_CARD);
                    } else if ("Input Card".equals(cardType[i])) {
                        curNode.setCardTypeByAddress(i, SerialNode.INPUT_CARD);
                        numInput++;
                    } else if ("Output Card".equals(cardType[i])) {
                        curNode.setCardTypeByAddress(i, SerialNode.OUTPUT_CARD);
                        numOutput++;
                    } else {
                        log.error("Unexpected card type - " + cardType[i]);
                    }
                }
                // consistency check
                if (numCards != (numOutput + numInput)) {
                    log.error("Inconsistent numbers of cards - setNodeParameters.");
                }

                // Force created node to be polled as the default
                curNode.setOptNet_AUTOPOLL(1);
                break;

            // CPNODE
            case SerialNode.CPNODE:
                // set number of bits per card
                bitsPerCard = 8;
                curNode.setNumBitsPerCard(bitsPerCard);
                numInput = 2;
                numOutput = 2;
                // configure the input/output cards
                for (int i = 4; i < 64; i++) // Skip the onboard bytes
                {
                    if ("No Card".equals(cardType[i])) {
                        curNode.setCardTypeByAddress(i, SerialNode.NO_CARD);
                    } else if ("Input Card".equals(cardType[i])) {
                        curNode.setCardTypeByAddress(i, SerialNode.INPUT_CARD);
                        numInput++;
                    } else if ("Output Card".equals(cardType[i])) {
                        curNode.setCardTypeByAddress(i, SerialNode.OUTPUT_CARD);
                        numOutput++;
                    } else {
                        log.error("Unexpected card type - " + cardType[i]);
                    }
                }

                // Set the node option bits.  Some are moved from the CMRInet options
                curNode.setOptNet_AUTOPOLL(cbx_cmrinetopt_AUTOPOLL.isSelected() ? 1 : 0);

                curNode.setOptNode_SENDEOT(cbx_cpnodeopt_SENDEOT.isSelected() ? 1 : 0);
                curNode.setOptNode_USECMRIX(cbx_cmrinetopt_USECMRIX.isSelected() ? 1 : 0); // Copy from CMRInet
                curNode.setOptNode_USEBCC(cbx_cmrinetopt_USEBCC.isSelected() ? 1 : 0);     // Copy from CMRInet
                curNode.setOptNode_BIT8(cbx_cpnodeopt_BIT8.isSelected() ? 1 : 0);
                curNode.setOptNode_BIT15(cbx_cpnodeopt_BIT15.isSelected() ? 1 : 0);

                break;

            // CPMEGA
            case SerialNode.CPMEGA:
                // set number of bits per card
                bitsPerCard = 8;
                curNode.setNumBitsPerCard(bitsPerCard);
                numInput = 0;
                numOutput = 0;

                for (int i = 0; i < 8; i++) // Pick up the onboard bytes
                {
                    if ("No Card".equals(onBoardType[i])) {
                        curNode.setCardTypeByAddress(i, SerialNode.NO_CARD);
                    } else if ("Input Card".equals(onBoardType[i])) {
                        curNode.setCardTypeByAddress(i, SerialNode.INPUT_CARD);
                        numInput++;
                    } else if ("Output Card".equals(onBoardType[i])) {
                        curNode.setCardTypeByAddress(i, SerialNode.OUTPUT_CARD);
                        numOutput++;
                    } else {
                        log.error("Unexpected card type - " + onBoardType[i]);
                    }
                }

                // configure the IOX cards
                for (int i = 8; i < 64; i++) // Skip the onboard bytes
                {
                    if ("No Card".equals(cardType[i])) {
                        curNode.setCardTypeByAddress(i, SerialNode.NO_CARD);
                    } else if ("Input Card".equals(cardType[i])) {
                        curNode.setCardTypeByAddress(i, SerialNode.INPUT_CARD);
                        numInput++;
                    } else if ("Output Card".equals(cardType[i])) {
                        curNode.setCardTypeByAddress(i, SerialNode.OUTPUT_CARD);
                        numOutput++;
                    } else {
                        log.error("Unexpected card type - " + cardType[i]);
                    }
                }

                // Set the node option bits.  Some are moved from the CMRInet options
                curNode.setOptNet_AUTOPOLL(1);  // Default node to be polled

                curNode.setOptNode_SENDEOT(cbx_cpnodeopt_SENDEOT.isSelected() ? 1 : 0);
                curNode.setOptNode_USECMRIX(cbx_cmrinetopt_USECMRIX.isSelected() ? 1 : 0); // Copy from CMRInet
                curNode.setOptNode_USEBCC(cbx_cmrinetopt_USEBCC.isSelected() ? 1 : 0);     // Copy from CMRInet
                curNode.setOptNode_BIT8(cbx_cpnodeopt_BIT8.isSelected() ? 1 : 0);
                curNode.setOptNode_BIT15(cbx_cpnodeopt_BIT15.isSelected() ? 1 : 0);

                break;

            default:
                log.error("Unexpected node type in setNodeParameters- " + Integer.toString(nodeType));
                break;
        }

        // Set the node description for all types
        curNode.setcmriNodeDesc(nodeDescription.getText());

        // Cause reinitialization of this Node to reflect these parameters
        _memo.getTrafficController().initializeSerialNode(curNode);
    }

    /**
     * Reset the notes error after error display.
     */
    private void resetNotes() {
        if (errorInStatus1) {
            if (editMode) {
                statusText1.setText(editStatus1);
            } else {
                //  statusText1.setText(stdStatus1);
            }
            errorInStatus1 = false;
        }
        resetNotes2();
    }

    /**
     * Reset the second line of Notes area.
     */
    private void resetNotes2() {
        if (errorInStatus2) {
            if (editMode) {
                statusText1.setText(editStatus2);
            } else {
                //                statusText2.setText(stdStatus2);
            }
            errorInStatus2 = false;
        }
    }

    /**
     * Read node address and check for legal range. Sets the error message in
     * {@link #statusText1} if not legal.
     *
     * @return A node address in the range 0-127 if legal; -1 if not legal
     */
    private int readNodeAddress() {
        int addr = -1;
        try {
            addr = Integer.parseInt(nodeAddrField.getText());
        } catch (NumberFormatException e) {
            statusText1.setText(Bundle.getMessage("Error5"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return -1;
        }

        if ((addr < 0) || (addr > 127)) {
//            statusText1.setText(Bundle.getMessage("Error6"));
//            statusText1.setVisible(true);
            errorInStatus1 = true;
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error6"), "", JOptionPane.ERROR_MESSAGE);
            resetNotes2();
            return -1;
        }
        return (addr);
    }

    /**
     * Read receive delay from window. If an error is detected, a suitable error
     * message is placed in the Notes area.
     *
     * @return true if successful; false otherwise
     */
    protected boolean readReceiveDelay() {
        // get the transmission delay
        try {
            receiveDelay = Integer.parseInt(receiveDelayField.getText());
        } catch (NumberFormatException e) {
            statusText1.setText(Bundle.getMessage("Error7"));
            statusText1.setVisible(true);
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error7"), "", JOptionPane.ERROR_MESSAGE);
            receiveDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (receiveDelay < 0) {
            statusText1.setText(Bundle.getMessage("Error8"));
            statusText1.setVisible(true);
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error8"), "", JOptionPane.ERROR_MESSAGE);
            receiveDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (receiveDelay > 65535) {
            statusText1.setText(Bundle.getMessage("Error9"));
            statusText1.setVisible(true);
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error9"), "", JOptionPane.ERROR_MESSAGE);
            receiveDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        // successful
        return true;
    }

    /**
     * Read pulse width from window. If an error is detected, a suitable error
     * message is placed in the Notes area.
     *
     * @return true if successful; false otherwise
     */
    protected boolean readPulseWidth() {
        // get the pulse width
        try {
            pulseWidth = Integer.parseInt(pulseWidthField.getText());
        } catch (NumberFormatException e) {
            statusText1.setText(Bundle.getMessage("Error18"));
            statusText1.setVisible(true);
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error18"), "", JOptionPane.ERROR_MESSAGE);
            pulseWidth = 500;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (pulseWidth < 100) {
            statusText1.setText(Bundle.getMessage("Error16"));
            statusText1.setVisible(true);
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error16"), "", JOptionPane.ERROR_MESSAGE);
            pulseWidth = 100;
            pulseWidthField.setText(Integer.toString(pulseWidth));
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (pulseWidth > 10000) {
            statusText1.setText(Bundle.getMessage("Error17"));
            statusText1.setVisible(true);
            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error17"), "", JOptionPane.ERROR_MESSAGE);
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
     * Check for consistency errors by node type. If an error is detected, a
     * suitable error message is placed in the Notes area.
     *
     * @return true if successful; false otherwise
     */
    protected boolean checkConsistency() {
        switch (nodeType) {
            case SerialNode.SMINI:
                // ensure that number of searchlight bits is consistent
                int numBits = 0;
                for (int i = 0; i < 48; i++) {
                    if (searchlightBits[i]) {
                        numBits++;
                    }
                }
                if ((2 * num2LSearchLights) != numBits) {
                    statusText1.setText(Bundle.getMessage("Error10"));
                    statusText1.setVisible(true);
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("Error10"), "", JOptionPane.ERROR_MESSAGE);
                    errorInStatus1 = true;
                    resetNotes2();
                    return (false);
                }
                break;
            case SerialNode.USIC_SUSIC:
                // ensure that at least one card is defined
                numCards = 0;
                boolean atNoCard = false;
                for (int i = 0; i < 64; i++) {
                    if ((cardType[i].equals(Bundle.getMessage("CardTypeOutput")))
                            || (cardType[i].equals(Bundle.getMessage("CardTypeInput")))) {
                        if (atNoCard) {
                            // gap error
                            statusText1.setText(Bundle.getMessage("Error11"));
                            statusText1.setVisible(true);
                            statusText2.setText(Bundle.getMessage("Error12"));
                            JOptionPane.showMessageDialog(this, Bundle.getMessage("Error11") + Bundle.getMessage("Error12"), "", JOptionPane.ERROR_MESSAGE);
                            errorInStatus1 = true;
                            errorInStatus2 = true;
                            return (false);
                        } else {
                            numCards++;
                        }
                    } else if (cardType[i].equals(Bundle.getMessage("CardTypeNone"))) {
                        atNoCard = true;
                    }
                }
                // ensure that at least one card has been defined
                if (numCards <= 0) {
                    // no card error
                    statusText1.setText(Bundle.getMessage("Error13"));
                    statusText2.setText(Bundle.getMessage("Error14"));
                    statusText1.setVisible(true);
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("Error13") + Bundle.getMessage("Error14"), "", JOptionPane.ERROR_MESSAGE);
                    errorInStatus1 = true;
                    errorInStatus2 = true;
                    return (false);
                }
                // check that card size is 24 or 32 bit
                if ((bitsPerCard != 24) && (bitsPerCard != 32)) {
                    // card size error
                    statusText1.setText(Bundle.getMessage("Error15"));
                    statusText1.setVisible(true);
                    errorInStatus1 = true;
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("Error15"), "", JOptionPane.ERROR_MESSAGE);
                    resetNotes2();
                    return (false);
                }
                // further checking if in Edit mode
                if (editMode) {
                    // get pre-edit numbers of cards
                    int numOutput = curNode.numOutputCards();
                    int numInput = curNode.numInputCards();
                    // will the number of cards be reduced by this edit?
                    if (numCards < (numOutput + numInput)) {
                        if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this,
                                Bundle.getMessage("ConfirmUpdate1") + "\n"
                                + Bundle.getMessage("ConfirmUpdate2") + "\n"
                                + Bundle.getMessage("ConfirmUpdate3"),
                                Bundle.getMessage("ConfirmUpdateTitle"),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE)) {
                            // user said don't update - cancel the update
                            return (false);
                        }
                    }
                }
                break;
            case SerialNode.CPNODE:
                for (int j = 0; j < 64; j++) {
                    if ((cardType[j].equals(Bundle.getMessage("CardTypeOutput")))
                            || (cardType[j].equals(Bundle.getMessage("CardTypeInput")))) {
                        numCards++;
                    }
                }
                break;
            case SerialNode.CPMEGA:
                for (int j = 0; j < 64; j++) {
                    if ((cardType[j].equals(Bundle.getMessage("CardTypeOutput")))
                            || (cardType[j].equals(Bundle.getMessage("CardTypeInput")))) {
                        numCards++;
                    }
                }
                break;

            // here add code for other types of nodes
            default:
                log.warn("Unexpected node type - {}", nodeType);
                break;
        }
        return true;
    }

    /**
     * Set up table for selecting card type by address for USIC_SUSIC nodes
     */
    public class CardConfigModel extends AbstractTableModel {

        @Override
        public String getColumnName(int c) {
            return cardConfigColumnNames[c];
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return 64;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (c == 0) {
                return "       " + Integer.toString(r);
            } else if (c == 1) {
                return "  " + cardType[r];
            }
            return "";
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            if (c == 1) {
                cardType[r] = (String) type;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return (c == 1);
        }

        public static final int ADDRESS_COLUMN = 0;
        public static final int TYPE_COLUMN = 1;
    }
    private final String[] cardConfigColumnNames = {
        Bundle.getMessage("HeadingCardAddress"),
        Bundle.getMessage("HeadingCardType")
    };
    private final String[] cardType = new String[64];
    private final String[] onBoardType = new String[16];

    /**
     * Set up model for SMINI table for designating oscillating 2-lead
     * searchlights
     */
    public class SearchlightConfigModel extends AbstractTableModel {

        @Override
        public String getColumnName(int c) {
            return searchlightConfigColumnNames[c];
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c > 0) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        @Override
        public int getColumnCount() {
            return 9;
        }

        @Override
        public int getRowCount() {
            return 6;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (c == 0) {
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
            } else {
                int index = (r * 8) + (c - 1);
                return searchlightBits[index];
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            if (c > 0) {
                int index = (r * 8) + (c - 1);
                if (!((Boolean) type)) {
                    searchlightBits[index] = false;
                    if (firstSearchlight[index]) {
                        searchlightBits[index + 1] = false;
                        firstSearchlight[index] = false;
                    } else {
                        searchlightBits[index - 1] = false;
                        firstSearchlight[index - 1] = false;
                    }
                    num2LSearchLights--;
                } else {
                    if (index < 47) {
                        if (!searchlightBits[index] && !searchlightBits[index + 1]) {
                            searchlightBits[index] = true;
                            searchlightBits[index + 1] = true;
                            firstSearchlight[index] = true;
                            firstSearchlight[index + 1] = false;
                            if (index > 0) {
                                firstSearchlight[index - 1] = false;
                            }
                            num2LSearchLights++;
                        }
                    }
                }
                panel2a.setVisible(false);
                panel2a.setVisible(true);
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return (c != 0);
        }

        public static final int PORT_COLUMN = 0;
    }
    private final String[] searchlightConfigColumnNames = {Bundle.getMessage("HeadingPort"), "0", "1", "2", "3", "4", "5", "6", "7"};
    private final boolean[] searchlightBits = new boolean[48];   // true if this bit is a searchlight bit
    private final boolean[] firstSearchlight = new boolean[48];  // true if first of a pair of searchlight bits

    /**
     * Handles checkboxes for cpNode options
     */
    private class HandlerClass implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            JCheckBox checkbox = (JCheckBox) e.getSource();

            if (checkbox == cbx_cmrinetopt_AUTOPOLL) {
                curNode.setCMRInetOpts(SerialNode.optbitNet_AUTOPOLL, (cbx_cmrinetopt_AUTOPOLL.isSelected() ? 1 : 0));
            } else if (checkbox == cbx_cmrinetopt_USECMRIX) {
                curNode.setCMRInetOpts(SerialNode.optbitNet_USECMRIX, (cbx_cmrinetopt_USECMRIX.isSelected() ? 1 : 0));
                curNode.setcpnodeOpts(SerialNode.optbitNet_USECMRIX, curNode.getCMRInetOpts(SerialNode.optbitNet_USECMRIX));
            } else if (checkbox == cbx_cmrinetopt_USEBCC) {
                curNode.setCMRInetOpts(SerialNode.optbitNet_USEBCC, (cbx_cmrinetopt_USEBCC.isSelected() ? 1 : 0));
                curNode.setcpnodeOpts(SerialNode.optbitNet_USEBCC, curNode.getCMRInetOpts(SerialNode.optbitNet_USEBCC));
            } else if (checkbox == cbx_cmrinetopt_BIT8) {
                curNode.setCMRInetOpts(SerialNode.optbitNet_BIT8, (cbx_cmrinetopt_BIT8.isSelected() ? 1 : 0));
            } else if (checkbox == cbx_cmrinetopt_BIT15) {
                curNode.setCMRInetOpts(SerialNode.optbitNet_BIT15, (cbx_cmrinetopt_BIT15.isSelected() ? 1 : 0));
            } else if (checkbox == cbx_cpnodeopt_SENDEOT) {
                curNode.setcpnodeOpts(SerialNode.optbitNode_SENDEOT, (cbx_cpnodeopt_SENDEOT.isSelected() ? 1 : 0));
            } else if (checkbox == cbx_cpnodeopt_BIT1) {
                cbx_cpnodeopt_BIT1.setSelected(false);
            } else if (checkbox == cbx_cpnodeopt_BIT2) {
                cbx_cpnodeopt_BIT2.setSelected(false);
            } else if (checkbox == cbx_cpnodeopt_BIT8) {
                curNode.setcpnodeOpts(SerialNode.optbitNode_BIT8, (cbx_cpnodeopt_BIT8.isSelected() ? 1 : 0));
            } else if (checkbox == cbx_cpnodeopt_BIT15) {
                curNode.setcpnodeOpts(SerialNode.optbitNode_BIT15, (cbx_cpnodeopt_BIT15.isSelected() ? 1 : 0));
            }
            changedNode = true;
        }
    }

    /**
     * Set up table for selecting card type by address for CPNODE/CPMEGA nodes
     */
    public class CPnodeConfigModel extends AbstractTableModel {

        @Override
        public String getColumnName(int c) {
            return cpnodeConfigColumnNames[c];
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return CARDTYPE_COLUMN+1;
        }

        @Override
        public int getRowCount() {
            return 16;
        }

        @Override
        public Object getValueAt(int r, int c) {
            String[] cdPort = {"  A", "  B"};
            String val = "     ";
            switch (c) {
                case CARD_COLUMN:
                    val = Integer.toString(r+2);
                    return "   " + val;
                case CARDNUM_COLUMN:
                    int i = r / 2;
                    if (r % 2 == 0) {
                        val = Integer.toHexString(0x20 + i) + " ";
                    }
                    return "    " + val + "     " + cdPort[(r % 2)];
                case CARDTYPE_COLUMN:
                    return " " + cardType[r + cpNodeOnboard];
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            if (c == CARDTYPE_COLUMN) {
                cardType[r + cpNodeOnboard] = (String) type;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return (c == CARDTYPE_COLUMN);
        }

        public static final int CARD_COLUMN = 0;
        public static final int CARDNUM_COLUMN = 1;
        public static final int CARDTYPE_COLUMN = 2;
    }
    private final String[] cpnodeConfigColumnNames = {"Card","IOX Addr Port", "Port Type"};

    /**
     * Set up table for selecting card type by address for CPNODE/CPMEGA nodes
     */
    public class OSnodeConfigModel extends AbstractTableModel {

        @Override
        public String getColumnName(int c) {
            return osnodeConfigColumnNames[c];
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return 8;
        }

        @Override
        public Object getValueAt(int r, int c) {
            switch (c) {
                case CARDNUM_COLUMN:
                    int i = r;
                    return "    " + i + " ";
                case CARDTYPE_COLUMN:
                    return " " + onBoardType[r];
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            if (c == CARDTYPE_COLUMN) {
                onBoardType[r] = (String) type;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return (c == 1);
        }

        public static final int CARDNUM_COLUMN = 0;
        public static final int CARDTYPE_COLUMN = 1;
    }
    private final String[] osnodeConfigColumnNames = {"  Byte ", "Port Type"};

    private final int[] cpnodeOpts = new int[16];  // Local storage for node options
    private final int[] cmrinetOpts = new int[16];  // Local storage for node options

    private final static Logger log = LoggerFactory.getLogger(NodeConfigManagerFrame.class);

}
