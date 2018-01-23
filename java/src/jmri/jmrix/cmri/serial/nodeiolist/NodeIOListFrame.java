package jmri.jmrix.cmri.serial.nodeiolist;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for running CMRI assignment list.
 *
 * @author	Dave Duchamp Copyright (C) 2006
 * @author	Chuck Catania Copyright (C) 2014, 2016, 2017
 */
public class NodeIOListFrame extends jmri.util.JmriJFrame {

    ArrayList<SerialNode> cmriNode = new ArrayList<SerialNode>();

    protected boolean inputSelected = true;  // true if displaying input assignments, false for output
    protected SerialNode selNode = null;
    public int selNodeNum = 0;  // Address (ua) of selected Node
    public int numBits = 48;  // number of bits in assignment table
    public int numInputBits = 24;  // number of input bits for selected node
    public int numOutputBits = 48; // number of output bits for selected node

    // node select pane items
    JLabel nodeLabel = new JLabel(Bundle.getMessage("NodeBoxLabel") + " ");
    JLabel nodeAddrBox = new JLabel("??");
    ButtonGroup bitTypeGroup = new ButtonGroup();
    JRadioButton inputBits = new JRadioButton(Bundle.getMessage("ShowInputButton") + "   ", true);
    JRadioButton outputBits = new JRadioButton(Bundle.getMessage("ShowOutputButton"), false);
    JLabel nodeInfoText = new JLabel("Node Information Text");

    JLabel nodeDesc = new JLabel("Description:");

    // assignment pane items
    protected JPanel assignmentPanel = null;
    protected Border inputBorder = BorderFactory.createEtchedBorder();
    protected Border inputBorderTitled = BorderFactory.createTitledBorder(inputBorder,
            Bundle.getMessage("AssignmentPanelInputName"));
    protected Border outputBorder = BorderFactory.createEtchedBorder();
    protected Border outputBorderTitled = BorderFactory.createTitledBorder(outputBorder,
            Bundle.getMessage("AssignmentPanelOutputName"));
    protected JTable assignmentTable = null;
    protected TableModel assignmentListModel = null;

    // button pane items
    JButton printButton = new JButton(Bundle.getMessage("PrintButtonText"));
    JButton doneButton = new JButton(Bundle.getMessage("DoneButtonText"));

    NodeIOListFrame curFrame;
    private CMRISystemConnectionMemo _memo = null;

    public NodeIOListFrame(CMRISystemConnectionMemo memo) {
        super();
        _memo = memo;
        curFrame = this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {

        // set the frame's initial state
        setTitle(Bundle.getMessage("WindowTitle"));
        setSize(500, 300);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Set up the node selection panel
        initializeNodes();
        if (cmriNode.size() > 0) {
            inputBits.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (inputSelected == false) {
                        inputSelected = true;
                        displayNodeIOBits(selNodeNum);
                    }
                }
            });
            outputBits.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (inputSelected == true) {
                        inputSelected = false;
                        displayNodeIOBits(selNodeNum);
                    }
                }
            });
        } else {
            nodeInfoText.setText(Bundle.getMessage("NoNodesError"));
        }
        nodeLabel.setToolTipText(Bundle.getMessage("NodeBoxTip"));
        inputBits.setToolTipText(Bundle.getMessage("ShowInputTip"));
        outputBits.setToolTipText(Bundle.getMessage("ShowOutputTip"));

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        JPanel panel11 = new JPanel();
        panel11.add(nodeLabel);
        bitTypeGroup.add(outputBits);
        bitTypeGroup.add(inputBits);
        panel11.add(inputBits);
        panel11.add(outputBits);
        JPanel panel12 = new JPanel();
        panel12.add(nodeInfoText);

        JPanel panel13 = new JPanel();
        panel13.add(nodeDesc);
        panel13.setVisible(true);
        nodeDesc.setVisible(false);
        panel1.add(panel13);

        panel1.add(panel11);
        panel1.add(panel12);

        Border panel1Border = BorderFactory.createEtchedBorder();
        Border panel1Titled = BorderFactory.createTitledBorder(panel1Border, Bundle.getMessage("NodePanelName") + " " + _memo.getUserName());
        panel1.setBorder(panel1Titled);
        contentPane.add(panel1);

// Set up the assignment panel
        assignmentPanel = new JPanel();
        assignmentPanel.setLayout(new BoxLayout(assignmentPanel, BoxLayout.Y_AXIS));
        assignmentListModel = new AssignmentTableModel();
        assignmentTable = new JTable(assignmentListModel);
        assignmentTable.setRowSelectionAllowed(false);
        assignmentTable.setPreferredScrollableViewportSize(new java.awt.Dimension(300, 350));

        TableColumnModel assignmentColumnModel = assignmentTable.getColumnModel();
        TableColumn bitColumn = assignmentColumnModel.getColumn(AssignmentTableModel.BIT_COLUMN);
        bitColumn.setMinWidth(20);
        bitColumn.setMaxWidth(40);
        bitColumn.setResizable(true);
        TableColumn addressColumn = assignmentColumnModel.getColumn(AssignmentTableModel.ADDRESS_COLUMN);
        addressColumn.setMinWidth(40);
        addressColumn.setMaxWidth(85);
        addressColumn.setResizable(true);
        TableColumn sysColumn = assignmentColumnModel.getColumn(AssignmentTableModel.SYSNAME_COLUMN);
        sysColumn.setMinWidth(90);
        sysColumn.setMaxWidth(150);
        sysColumn.setResizable(true);
        TableColumn userColumn = assignmentColumnModel.getColumn(AssignmentTableModel.USERNAME_COLUMN);
        userColumn.setMinWidth(90);
        userColumn.setMaxWidth(250);
        userColumn.setResizable(true);
        assignmentColumnModel.getColumn(AssignmentTableModel.COMMENT_COLUMN);
        userColumn.setMinWidth(90);
        userColumn.setMaxWidth(250);
        userColumn.setResizable(true);
        JScrollPane assignmentScrollPane = new JScrollPane(assignmentTable);
        assignmentPanel.add(assignmentScrollPane, BorderLayout.CENTER);
        if (inputSelected) {
            assignmentPanel.setBorder(inputBorderTitled);
        } else {
            assignmentPanel.setBorder(outputBorderTitled);
        }
        contentPane.add(assignmentPanel);

        // Set up Print button
        JPanel panel3 = new JPanel();
        panel3.setLayout(new FlowLayout());
        printButton.setVisible(true);
        printButton.setToolTipText(Bundle.getMessage("PrintButtonTip"));
        if (cmriNode.size() > 0) {
            printButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    printButtonActionPerformed(e);
                }
            });
        }
        panel3.add(printButton);
        contentPane.add(panel3);

        doneButton.setVisible(true);
        doneButton.setToolTipText(Bundle.getMessage("DoneButtonTip"));
        if (cmriNode.size() > 0) {
            doneButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doneButtonActionPerformed();
                }
            });
        }
        panel3.add(doneButton);

        addHelpMenu("package.jmri.jmrix.cmri.serial.assignment.ListFrame", true);

        // pack for display
        pack();
    }

    /**
     * Method to initialize configured nodes and set up the node select combo
     * box
     */
    public void initializeNodes() {
        // get all configured nodes

        SerialNode node = (SerialNode) _memo.getTrafficController().getNode(0);

        int index = 0;  //1
        while (node != null) {
            cmriNode.add(node);
            node = (SerialNode) _memo.getTrafficController().getNode(index);
            index++;
        }
//            node = (SerialNode) SerialTrafficController.instance().getNode(index);
//            index ++;
    }

    /**
     * Method to handle selection of a Node for info display
     */
    public void displayNodeIOBits(int nodeID) {
        nodeLabel.setText("Node: " + nodeID + "     ");
        selNodeNum = nodeID;
        nodeDesc.setText("");
        for (int i = 0; i < cmriNode.size(); i++) {
            if (cmriNode.get(i).getNodeAddress() == nodeID) {
                selNode = cmriNode.get(i);
            }
        }
        String nodeType = "";
        int type = selNode.getNodeType();
        int bitsPerCard = selNode.getNumBitsPerCard();
        int numInputCards = selNode.numInputCards();
        int numOutputCards = selNode.numOutputCards();

        switch (type) {
            case SerialNode.SMINI:
                numInputBits = 24;
                numOutputBits = 48;
                nodeType = "SMINI - ";
                break;
            case SerialNode.USIC_SUSIC:
                numInputBits = bitsPerCard * numInputCards;
                numOutputBits = bitsPerCard * numOutputCards;
                nodeType = "USIC_SUSIC - ";
                break;
            case SerialNode.CPNODE:
                numInputBits = bitsPerCard * numInputCards;
                numOutputBits = bitsPerCard * numOutputCards;
                nodeType = "CPNODE - ";
                break;
            case SerialNode.CPMEGA:
                numInputBits = bitsPerCard * numInputCards;
                numOutputBits = bitsPerCard * numOutputCards;
                nodeType = "CPMEGA - ";
                break;
            default:
                break;
        }
        nodeInfoText.setText(nodeType + bitsPerCard + " " + Bundle.getMessage("BitsPerCard") + ", "
                + numInputBits + " " + Bundle.getMessage("InputBitsAnd") + " "
                + numOutputBits + " " + Bundle.getMessage("OutputBits"));
        String name = selNode.getcmriNodeDesc();
        if (name != null && (!name.isEmpty())) {
            nodeDesc.setText(Bundle.getMessage("NodeDesc") + " " + name);
            nodeDesc.setVisible(true);
        }

        // initialize for input or output assignments
        if (inputSelected) {
            numBits = numInputBits;
            assignmentPanel.setBorder(inputBorderTitled);
        } else {
            numBits = numOutputBits;
            assignmentPanel.setBorder(outputBorderTitled);
        }
        ((AssignmentTableModel) assignmentListModel).fireTableDataChanged();
    }

    /*  Done button handler */
    public void doneButtonActionPerformed() {
        setVisible(false);
        dispose();
    }

    /**
     * Method to handle print button in List Frame
     */
    public void printButtonActionPerformed(java.awt.event.ActionEvent e) {
        int[] colWidth = new int[5];
        // initialize column widths
        TableColumnModel assignmentColumnModel = assignmentTable.getColumnModel();
        colWidth[0] = assignmentColumnModel.getColumn(AssignmentTableModel.BIT_COLUMN).getWidth();
        colWidth[1] = assignmentColumnModel.getColumn(AssignmentTableModel.ADDRESS_COLUMN).getWidth();
        colWidth[2] = assignmentColumnModel.getColumn(AssignmentTableModel.SYSNAME_COLUMN).getWidth();
        colWidth[3] = assignmentColumnModel.getColumn(AssignmentTableModel.USERNAME_COLUMN).getWidth();
        colWidth[4] = assignmentColumnModel.getColumn(AssignmentTableModel.COMMENT_COLUMN).getWidth();
        // set up a page title
        String head;
        if (inputSelected) {
            head = "CMRInet " + Bundle.getMessage("AssignmentPanelInputName") + " - "
                    + Bundle.getMessage("NodeBoxLabel") + " " + selNodeNum;
        } else {
            head = "CMRInet " + Bundle.getMessage("AssignmentPanelOutputName") + " - "
                    + Bundle.getMessage("NodeBoxLabel") + " " + selNodeNum;
        }
        // initialize a printer writer
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(curFrame, head, 10, .8, .5, .5, .5, false);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            //log.debug("Print cancelled");
            return;
        }
        writer.increaseLineSpacing(20);
        // print the assignments
        ((AssignmentTableModel) assignmentListModel).printTable(writer, colWidth);
    }

    /**
     * Set up table for displaying bit assignments
     */
    public class AssignmentTableModel extends AbstractTableModel {

        private String free = Bundle.getMessage("AssignmentFree");
        private int curRow = -1;
        private String curRowSysName = "";

        public String getColumnName(int c) {
            return assignmentTableColumnNames[c];
        }

        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        public boolean isCellEditable(int r, int c) {
            return false;
        }

        public int getColumnCount() {
            return MAX_COLS;
        }

        public int getRowCount() {
            return numBits;
        }

        public Object getValueAt(int r, int c) {
            if (c == BIT_COLUMN) {
                return Integer.toString(r + 1);
            } else if (c == ADDRESS_COLUMN) {
                return Integer.toString((selNodeNum * 1000) + r + 1);
            } else if (c == SYSNAME_COLUMN) {
                String sName = null;
                if (curRow != r) {

                    if (inputSelected) {
                        sName = _memo.isInputBitFree(selNodeNum, (r + 1));
                    } else {
                        sName = _memo.isOutputBitFree(selNodeNum, (r + 1));
                    }
                    curRow = r;
                    curRowSysName = sName;
                } else {
                    sName = curRowSysName;
                }
                if (sName == null) {
                    return (free);
                } else {
                    return sName;
                }
            } else if (c == USERNAME_COLUMN) {
                String sName = null;
                if (curRow != r) {

                    if (inputSelected) {
                        sName = _memo.isInputBitFree(selNodeNum, (r + 1));
                    } else {
                        sName = _memo.isOutputBitFree(selNodeNum, (r + 1));
                    }

                    curRow = r;
                    curRowSysName = sName;
                } else {
                    sName = curRowSysName;
                }
                if (sName == null) {
                    return ("");
                } else {
                    return (_memo.getUserNameFromSystemName(sName));
                }
            } else if (c == COMMENT_COLUMN) {
                String sName = null;
                if (curRow != r) {
                    if (inputSelected) {
                        sName = _memo.isInputBitFree(selNodeNum, (r + 1));
                    } else {
                        sName = _memo.isOutputBitFree(selNodeNum, (r + 1));
                    }
                    curRow = r;
                    curRowSysName = sName;
                } else {
                    sName = curRowSysName;
                }

                if (sName == null) {
                    return ("");
                }

                if (inputSelected) {
                    jmri.Sensor s = null;
                    s = jmri.InstanceManager.sensorManagerInstance().getBySystemName(sName);
                    if (s != null) {
                        return s.getComment();
                    }
                } else {
                    jmri.Turnout t = null;
                    t = jmri.InstanceManager.turnoutManagerInstance().getBySystemName(sName);
                    if (t != null) {
                        return t.getComment();
                    }
                }

            }

            return "";  // fall through
        }

        public void setValueAt(Object type, int r, int c) {
            // nothing is stored here
        }

        public static final int BIT_COLUMN = 0;
        public static final int ADDRESS_COLUMN = 1;
        public static final int SYSNAME_COLUMN = 2;
        public static final int USERNAME_COLUMN = 3;
        public static final int COMMENT_COLUMN = 4;
        public static final int MAX_COLS = COMMENT_COLUMN + 1;

        /**
         * Method to print or print preview the assignment table. Printed in
         * proportionately sized columns across the page with headings and
         * vertical lines between each column. Data is word wrapped within a
         * column. Can only handle 4 columns of data as strings. Adapted from
         * routines in BeanTableDataModel.java by Bob Jacobsen and Dennis Miller
         */
        public void printTable(HardcopyWriter w, int colWidth[]) {
            // determine the column sizes - proportionately sized, with space between for lines
            int[] columnSize = new int[MAX_COLS];
            int charPerLine = w.getCharactersPerLine();
            int tableLineWidth = 0;  // table line width in characters
            int totalColWidth = 0;
            for (int j = 0; j < MAX_COLS; j++) {
                totalColWidth += colWidth[j];
            }
            float ratio = ((float) charPerLine) / ((float) totalColWidth);
            for (int j = 0; j < MAX_COLS; j++) {
                columnSize[j] = ((int) (colWidth[j] * ratio)) - 1;
                tableLineWidth += (columnSize[j] + 1);
            }

            // Draw horizontal dividing line
            w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                    tableLineWidth);

            // print the column header labels
            String[] columnStrings = new String[MAX_COLS];
            // Put each column header in the array
            for (int i = 0; i < MAX_COLS; i++) {
                columnStrings[i] = this.getColumnName(i);
            }
            //w.setFontStyle(Font.BOLD);
            printColumns(w, columnStrings, columnSize);
            w.setFontStyle(0);
            // draw horizontal line
            w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                    tableLineWidth);

            // now print each row of data
            String[] spaces = new String[MAX_COLS];
            // create base strings the width of each of the columns
            for (int k = 0; k < MAX_COLS; k++) {
                spaces[k] = "";
                for (int i = 0; i < columnSize[k]; i++) {
                    spaces[k] = spaces[k] + " ";
                }
            }
            for (int i = 0; i < this.getRowCount(); i++) {
                for (int j = 0; j < MAX_COLS; j++) {
                    //check for special, null contents
                    if (this.getValueAt(i, j) == null) {
                        columnStrings[j] = spaces[j];
                    } else {
                        columnStrings[j] = (String) this.getValueAt(i, j);
                    }
                }
                printColumns(w, columnStrings, columnSize);
                // draw horizontal line
                w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                        tableLineWidth);
            }
            w.close();
        }

        protected void printColumns(HardcopyWriter w, String columnStrings[], int columnSize[]) {
            String columnString = "";
            StringBuilder lineString = new StringBuilder("");
            String[] spaces = new String[MAX_COLS];
            // create base strings the width of each of the columns
            for (int k = 0; k < MAX_COLS; k++) {
                spaces[k] = "";
                for (int i = 0; i < columnSize[k]; i++) {
                    spaces[k] = spaces[k] + " ";
                }
            }
            // loop through each column
            boolean complete = false;
            while (!complete) {
                complete = true;
                for (int i = 0; i < MAX_COLS; i++) {
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
                try {
                    w.write(lineString.toString());
                    //write vertical dividing lines
                    int iLine = w.getCurrentLineNumber();
                    for (int i = 0, k = 0; i < w.getCharactersPerLine(); k++) {
                        w.write(iLine, i, iLine + 1, i);
                        if (k < MAX_COLS) {
                            i = i + columnSize[k] + 1;
                        } else {
                            i = w.getCharactersPerLine();
                        }
                    }
                    w.write("\n");
                    lineString = new StringBuilder("");
                } catch (IOException e) {
                    log.warn("error during printing: " + e);
                }
            }
        }
    }
    private String[] assignmentTableColumnNames = {Bundle.getMessage("HeadingBit"),
        Bundle.getMessage("HeadingAddress"),
        Bundle.getMessage("HeadingSystemName"),
        Bundle.getMessage("HeadingUserName"),
        Bundle.getMessage("HeadingComment")};

    private final static Logger log = LoggerFactory.getLogger(NodeIOListFrame.class);

}

