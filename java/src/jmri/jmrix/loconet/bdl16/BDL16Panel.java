package jmri.jmrix.loconet.bdl16;

import java.util.Collections;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import jmri.jmrix.loconet.AbstractBoardProgPanel;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel displaying and programming a BDL16x configuration.
 * <p>
 * The read and write require a sequence of operations, which we handle with a
 * state variable.
 * <p>
 * Programming of the BDL16x is done via configuration messages, so the BDL16x
 * should not be put into programming mode via the built-in pushbutton while
 * this tool is in use.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2004, 2007, 2010
 */
public class BDL16Panel extends AbstractBoardProgPanel {

    /**
     * BDL16x Configuration Tool.
     * <p>
     * Use this constructor when the Unit Address is unknown.
     */
    public BDL16Panel() {
        this(1, false);
    }
    
    JComboBox<Integer> addressComboBox;
    int[] boardNumbers;
    int origAccessBoardNum = 0;
    java.util.ArrayList<Integer> boardNumsEntryValue = new java.util.ArrayList<Integer>();
    
    @SuppressWarnings("unchecked") // type erasure means can't ask for new JComboBox<String>[48]
    JComboBox<String> comboBox[] = new JComboBox[48];
    
    /**
     * BDL16x Programming tool.
     * <p>
     * Use this constructor when the Unit Address is known.
     * 
     * @param boardNum    integer for the initial Unit Address
     * @param readOnInit  True to trigger automatic read of the board
     */
    public BDL16Panel(int boardNum, boolean readOnInit) {
        super(boardNum, readOnInit,"BDL16");
        setTypeWord(0x71);  // configure BDL16x message type
        origAccessBoardNum = boardNum;
        boardNumsEntryValue.add(boardNum);
    }

    /**
     * Get the URL for the HTML help for this tool.
     * 
     * @return URL
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.bdl16.BDL16Frame"; // NOI18N
    }

    /**
     * Get the name of the tool for use in the title of the window.
     * 
     * @return String containing text for the title of the window
     */
    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemBDL16Programmer"));
    }

    /**
     * Copy from the GUI to the OpSw array.
     * <p>
     * Used before write operations start.
     */
    @Override
    protected void copyToOpsw() {
        // copy over the display
        opsw[1] = comboBox[1].getSelectedIndex()==1;
        opsw[3] = comboBox[3].getSelectedIndex()==1;
        opsw[5] = comboBox[5].getSelectedIndex()==1;
        opsw[6] = comboBox[6].getSelectedIndex()==1;
        opsw[7] = comboBox[7].getSelectedIndex()==1;
        opsw[9] = comboBox[9].getSelectedIndex()==1;
        opsw[10] = comboBox[10].getSelectedIndex()==1;
        opsw[11] = comboBox[11].getSelectedIndex()==1;
        opsw[12] = comboBox[12].getSelectedIndex()==1;
        opsw[13] = comboBox[13].getSelectedIndex()==1;
        opsw[19] = comboBox[19].getSelectedIndex()==1;
        opsw[25] = comboBox[25].getSelectedIndex()==1;
        opsw[26] = comboBox[26].getSelectedIndex()==1;
        opsw[36] = comboBox[36].getSelectedIndex()==1;
        opsw[39] = comboBox[39].getSelectedIndex()==1;
        opsw[42] = comboBox[42].getSelectedIndex()==1;

        int index = comboBox[37].getSelectedIndex();
        opsw[37] = ((index==1) || (index==3))?true:false;
        opsw[38] = (index >=2)?true:false;

        index = comboBox[43].getSelectedIndex();
        opsw[43] = ((index==1) || (index==3))?true:false;
        opsw[44] = (index >=2)?true:false;
        opsw[40] = comboBox[40].getSelectedIndex()==1;
    }

    /**
     * Update the GUI elements.
     */
    @Override
    protected void updateDisplay() {
        comboBox[1].setSelectedIndex(opsw[1]?1:0);
        comboBox[3].setSelectedIndex(opsw[3]?1:0);
        comboBox[5].setSelectedIndex(opsw[5]?1:0);
        comboBox[6].setSelectedIndex(opsw[6]?1:0);
        comboBox[7].setSelectedIndex(opsw[7]?1:0);
        comboBox[9].setSelectedIndex(opsw[9]?1:0);
        comboBox[10].setSelectedIndex(opsw[10]?1:0);
        comboBox[11].setSelectedIndex(opsw[11]?1:0);
        comboBox[12].setSelectedIndex(opsw[12]?1:0);
        comboBox[13].setSelectedIndex(opsw[13]?1:0);
        comboBox[19].setSelectedIndex(opsw[19]?1:0);
        comboBox[25].setSelectedIndex(opsw[25]?1:0);
        comboBox[26].setSelectedIndex(opsw[26]?1:0);
        comboBox[36].setSelectedIndex(opsw[36]?1:0);
        comboBox[39].setSelectedIndex(opsw[39]?1:0);
        comboBox[42].setSelectedIndex(opsw[42]?1:0);
        comboBox[40].setSelectedIndex(opsw[40]?1:0);

        int temp = opsw[37]?1:0;
        temp += opsw[38]?2:0;
        comboBox[37].setSelectedIndex(temp);
        
        temp = opsw[43]?1:0;
        temp += opsw[44]?2:0;
        comboBox[43].setSelectedIndex(temp);
    }

    /**
     * Determine the next OpSw to be accessed.
     * 
     * @param state  most-recently accessed OpSw
     * @return       next OpSw to be accessed
     */
    @Override
    protected int nextState(int state) {
        switch (state) {
            case 1:
                return 3;
            case 3:
                return 5;
            case 5:
                return 6;
            case 6:
                return 7;
            case 7:
                return 9;
            case 9:
                return 10;
            case 10:
                return 11;
            case 11:
                return 12;
            case 12:
                return 13;
            case 13:
                return 19;
            case 19:
                return 25;
            case 25:
                return 26;
            case 26:
                return 36;
            case 36:
                return 37;
            case 37:
                return 38;
            case 38:
                return 39;
            case 39:
                return 42;
            case 42:
                return 43;
            case 43:
                return 44;
            case 44:
                return 40;    // have to do 40 last
            case 40:
                return 0;    // done!
            default:
                log.error("unexpected state " + state); // NOI18N
                return 0;
        }
    }
    
    /**
     * Initialize LocoNet connection for use by the tool.
     * 
     * @param memo  the LocoNet Connection
     */
    @Override
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
        LocoNetMessage m = new LocoNetMessage(6);
        m.setElement(0, LnConstants.OPC_MULTI_SENSE);
        m.setElement(1, 0x62);
        m.setElement(2, 0);
        m.setElement(3, 0x70);
        m.setElement(4, 0);
        memo.getLnTrafficController().sendLocoNetMessage(m);
    }

    private GridBagConstraints gc = new GridBagConstraints();
    private GridBagLayout gl = new GridBagLayout();

    /**
     * Initialize the GUI elements for use by the tool.
     */
    @Override
    public void initComponents() {
        JPanel addressingPanel = provideAddressing(" "); // create read/write buttons, address

        
        int indexOfTargetBoardAddress = 0;

        addressComboBox = new JComboBox<>();
        for (Integer index = 0; index < boardNumsEntryValue.size(); ++index) {
            if (boardNumsEntryValue.get(index) == origAccessBoardNum) {
                origAccessBoardNum = -1;
                indexOfTargetBoardAddress = index;
            }
            addressComboBox.addItem(boardNumsEntryValue.get(index));
        }

        addressComboBox.setSelectedIndex(indexOfTargetBoardAddress);
        addressingPanel.add(addressComboBox, 2);
        addressingPanel.getComponent(1).setVisible(false);
        addressComboBox.setEditable(true);
        
        addressingPanel.add(new JLabel(Bundle.getMessage("LabelBoardID")),1);
        addressingPanel.getComponent(0).setVisible(false);
        
        readAllButton.setPreferredSize(null);
        readAllButton.setText(Bundle.getMessage("ButtonReadFullSheet"));
        readAllButton.setToolTipText(Bundle.getMessage("ToolTipButtonReadFullSheet"));

        writeAllButton.setPreferredSize(null);
        writeAllButton.setText(Bundle.getMessage("ButtonWriteFullSheet"));
        writeAllButton.setToolTipText(Bundle.getMessage("ToolTipButtonWriteFullSheet"));

        // make both buttons a little bit bigger, with identical (preferred) sizes
        // (width increased because some computers/displays trim the button text)
        java.awt.Dimension d = writeAllButton.getPreferredSize();
        int w = d.width;
        d = readAllButton.getPreferredSize();
        if (d.width > w) {
            w = d.width;
        }
        writeAllButton.setPreferredSize(new java.awt.Dimension((int) (w * 1.1), d.height));
        readAllButton.setPreferredSize(new java.awt.Dimension((int) (w * 1.1), d.height));
        
        appendLine(addressingPanel);  // add read/write buttons, address
                
        JPanel frame1 = new JPanel();
        frame1.setLayout(new BoxLayout(frame1, BoxLayout.PAGE_AXIS));

        TitledBorder allBoardsTitleBorder;
        Border blackline;
        blackline = BorderFactory.createLineBorder(java.awt.Color.black);
        allBoardsTitleBorder = BorderFactory.createTitledBorder(blackline,
                Bundle.getMessage("TitledBorderLabelAllBoards"));
        frame1.setBorder(allBoardsTitleBorder);

        JPanel allBoardsOptions = new JPanel();
        allBoardsOptions.setLayout(gl);
        // configure GridBagConstraints
        gc.ipadx = 15;
        gc.ipady = 20;
        gc.gridy = 0;

        allBoardsOptions.add(getLabel(1));
        allBoardsOptions.add(getComboBox(1));
        gc.gridy++;

        allBoardsOptions.add(getLabel(9));
        allBoardsOptions.add(getComboBox(9));
        gc.gridy++;

        allBoardsOptions.add(getLabel(10));
        allBoardsOptions.add(getComboBox(10));
        gc.gridy++;

        allBoardsOptions.add(getLabel(11));
        allBoardsOptions.add(getComboBox(11));
        gc.gridy++;

        allBoardsOptions.add(getLabel(12));
        allBoardsOptions.add(getComboBox(12));
        gc.gridy++;

        allBoardsOptions.add(getLabel(13));
        allBoardsOptions.add(getComboBox(13));
        gc.gridy++;

        allBoardsOptions.add(getLabel(19));
        allBoardsOptions.add(getComboBox(19));
        gc.gridy++;

        allBoardsOptions.add(getLabel(25));
        allBoardsOptions.add(getComboBox(25));
        gc.gridy++;

        allBoardsOptions.add(getLabel(26));
        allBoardsOptions.add(getComboBox(26));
        gc.gridy++;

        allBoardsOptions.add(getLabel(40));
        allBoardsOptions.add(getComboBox(40));

        frame1.add(allBoardsOptions);

        JPanel frame2 = new JPanel();
        frame2.setLayout(new BoxLayout(frame2, BoxLayout.PAGE_AXIS));
        TitledBorder bdl162Bdl168BoardsTitleBorder;
        bdl162Bdl168BoardsTitleBorder = BorderFactory.createTitledBorder(blackline,
                Bundle.getMessage("TitledBorderLabelBdl162Bdl168Boards"));
        frame2.setBorder(bdl162Bdl168BoardsTitleBorder);

        JPanel bdl162Bdl168BoardsOptions = new JPanel();
        bdl162Bdl168BoardsOptions.setLayout(gl);
        gc.gridy = 0;

        bdl162Bdl168BoardsOptions.add(getLabel(3));
        bdl162Bdl168BoardsOptions.add(getComboBox(3));
        gc.gridy++;

        bdl162Bdl168BoardsOptions.add(getLabel(5));
        bdl162Bdl168BoardsOptions.add(getComboBox(5));
        gc.gridy++;

        bdl162Bdl168BoardsOptions.add(getLabel(6));
        bdl162Bdl168BoardsOptions.add(getComboBox(6));
        gc.gridy++;

        bdl162Bdl168BoardsOptions.add(getLabel(7));
        bdl162Bdl168BoardsOptions.add(getComboBox(7));
        gc.gridy++;

        bdl162Bdl168BoardsOptions.add(getLabel(36));
        bdl162Bdl168BoardsOptions.add(getComboBox(36));

        frame2.add(bdl162Bdl168BoardsOptions);
        frame1.add(frame2);

        JPanel frame3 = new JPanel();
        frame3.setLayout(new BoxLayout(frame3, BoxLayout.PAGE_AXIS));

        TitledBorder bdl168SpecificTitleBorder;
        bdl168SpecificTitleBorder = BorderFactory.createTitledBorder(blackline,
                Bundle.getMessage("TitledBorderLabelBdl168Only"));
        frame3.setBorder(bdl168SpecificTitleBorder);

        JPanel bdl168SpecificOptions = new JPanel();
        bdl168SpecificOptions.setLayout(gl);
        gc.gridy = 0;

        bdl168SpecificOptions.add(getLabel(37, 38));
        bdl168SpecificOptions.add(getComboBox(37, 38));
        gc.gridy++;

        bdl168SpecificOptions.add(getLabel(42));
        bdl168SpecificOptions.add(getComboBox(42));
        gc.gridy++;

        bdl168SpecificOptions.add(getLabel(39));
        bdl168SpecificOptions.add(getComboBox(39));
        gc.gridy++;

        bdl168SpecificOptions.add(getLabel(43, 44));
        bdl168SpecificOptions.add(getComboBox(43, 44));

        frame3.add(bdl168SpecificOptions);
        frame2.add(frame3);

        appendLine(frame1);
        appendLine(provideStatusLine());
        setStatus(Bundle.getMessage("STATUS_TEXT_BOARD_MODE"));

        panelToScroll();

    }

    /**
     * Create a JLabel for an OpSw.
     *
     * @param n   OpSw number
     * @return the JPanel into which the Label is placed
     */
    private JLabel getLabel(int n) {
        String number = Integer.toString(n);
        if (number.length() == 1) {
            number = "0" + number;
        }
        JLabel label = new JLabel(Bundle.getMessage("LabelX", number)); // NOI18N
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setPreferredSize(new JLabel("XXXXXXXXXXXXXXXXXX").getPreferredSize()); // NOI18N
        // layout
        gc.gridx = 0;
        gc.weightx = 0.5;
        gc.anchor = GridBagConstraints.EAST;
        gl.setConstraints(label, gc);
        return label;
    }

    /**
     * Create a JLabel for an OpSw.
     *
     * @param n first OpSw number
     * @param n2 second OpSw number
     * @return the JPanel into which the Label is placed
     */
    private JLabel getLabel(int n, int n2) {
        String number = Integer.toString(n);
        if (number.length() == 1) {
            number = "0" + number;
        }
        String number2 = Integer.toString(n2);
        if (number2.length() == 1) {
            number2 = "0" + number2;
        }
        JLabel label = new JLabel(Bundle.getMessage("LabelXY", number, number2)); // NOI18N
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setPreferredSize(new JLabel("XXXXXXXXXXXXXXXXXX").getPreferredSize()); // NOI18N
        // layout
        gc.gridx = 0;
        gc.weightx = 0.5;
        gc.anchor = GridBagConstraints.EAST;
        gl.setConstraints(label, gc);
        return label;
    }

    /**
     * Create a JComboBox with two possible values.
     * <p>
     * For a given OpSw number, create a JComboBox containing the appropriate 
     * strings from the bundle.  Sets the initial value based on the OpSw's 
     * reported default value.
     * 
     * @param n   OpSw number
     * @return  the JPanel into which the JComboBox is placed
     */
    private JComboBox getComboBox(int n) {
        String number = Integer.toString(n);
        if (number.length() == 1) {
            number = "0" + number;
        }
        String[] s = new String[] {Bundle.getMessage("COMBOBOX_TEXT_OPSW" + number + "_THROWN"),
                        Bundle.getMessage("COMBOBOX_TEXT_OPSW" + number + "_CLOSED")};
        comboBox[n] = new JComboBox<>(s);
        comboBox[n].setSelectedIndex(getIndexForDefault(n));
        // size all combos to match the widest one (and a little bit more)
        comboBox[n].setPreferredSize(new JLabel("XXXXXXX" + Bundle.getMessage("COMBOBOX_TEXT_OPSW36_THROWN")).getPreferredSize()); // NOI18N
        // layout
        gc.gridx = 1;
        gc.weightx = 1.0;
        gc.anchor = GridBagConstraints.WEST;
        gl.setConstraints(comboBox[n], gc);
        return comboBox[n];
    }

    /**
     * Create a JComboBox with four possible values.
     * <p>
     * For two given OpSw numbers, create a JComboBox containing the appropriate 
     * strings from the bundle.  Sets the initial value based on the OpSws' 
     * reported default value.
     * 
     * @param n first OpSw number
     * @param n2 second OpSw number
     * @return the JPanel into which the JComboBox is placed
     */
    private JComboBox getComboBox(int n, int n2) {
        String number = Integer.toString(n);
        if (number.length() == 1) {
            number = "0" + number; // NOI18N
        }
        String number2 = Integer.toString(n2);
        if (number2.length() == 1) {
            number2 = "0" + number2; // NOI18N
        }

        comboBox[n] = new JComboBox<>(
                new String[] {Bundle.getMessage("COMBOBOX_TEXT_OPSW" + number + "_THROWN_OPSW" + number2 + "_THROWN"),
                        Bundle.getMessage("COMBOBOX_TEXT_OPSW" + number + "_CLOSED_OPSW" + number2 + "_THROWN"),
                        Bundle.getMessage("COMBOBOX_TEXT_OPSW" + number + "_THROWN_OPSW" + number2 + "_CLOSED"),
                        Bundle.getMessage("COMBOBOX_TEXT_OPSW" + number + "_CLOSED_OPSW" + number2 + "_CLOSED")
                });
        // default choice = 0 so already set
        // size all combos to match the widest one (and a little bit more)
        comboBox[n].setPreferredSize(new JLabel("XXXXXXX" + Bundle.getMessage("COMBOBOX_TEXT_OPSW36_THROWN")).getPreferredSize()); // NOI18N
        // layout
        gc.gridx = 1;
        gc.weightx = 1.0;
        gc.anchor = GridBagConstraints.WEST;
        gl.setConstraints(comboBox[n], gc);
        return comboBox[n];
    }

    /**
     * Determine the JComboBox index which corresponds to the default value 
     * for a given OpSw.
     * 
     * @param  n OpSw number
     * @return index of default choice
     */
    private int getIndexForDefault(int n) {
        switch (n) {
            case 5:
            case 11:
            case 12:
                return 1;
            default:
                return 0;
        }
    }
    
    /**
     * Already know of this board (unit address)?
     * 
     * @param id  Unit address to be checked against list
     * @return    True if the unit address is already in the list
     */
    private boolean alreadyKnowThisBoardId(Integer id) {
        return (boardNumsEntryValue.contains(id));
    }

    /**
     * Add a board to the list of unit addresses if not already there.
     * 
     * @param   id a unit address to be added
     * @return  index into the boardNumsEntryValue list of entry for unit address "id"
     */
    private Integer addBoardIdToList(Integer id) {
        boardNumsEntryValue.add(boardNumsEntryValue.size(), id);
        addressComboBox.removeAllItems();
        Collections.sort(boardNumsEntryValue);
        Integer indexOfTargetBoardAddress = 0;
        for (Integer index = 0; index < boardNumsEntryValue.size(); ++index) {
            if (boardNumsEntryValue.get(index).equals(id)) {
                indexOfTargetBoardAddress = index;
            }
            addressComboBox.addItem(boardNumsEntryValue.get(index));
        }
        return indexOfTargetBoardAddress;
    }

    /**
     * Select a device based on an index into the list of unit addresses.
     * 
     * @param index into the list of addresses
     */
    private void selectBoardIdByIndex(Integer index) {
        addressComboBox.setSelectedIndex(index);
    }

    /**
     * Read all OpSws, based on the selected unit address in the JComboBox.
     */
    @Override
    public void readAll() {
        addrField.setText(addressComboBox.getSelectedItem().toString());
        Integer curAddr = Integer.parseInt(addrField.getText());

        // If a new board address is specified, add it (and sort it) into the current list.
        if (!alreadyKnowThisBoardId(curAddr)) {
            Integer index = addBoardIdToList(curAddr);
            selectBoardIdByIndex(index);
        }
        super.readAll();
    }

    /**
     * Interpret incoming LocoNet messages.
     * 
     * @param m LocoNet message to be interpreted
     */
    @Override
    public void message(LocoNetMessage m) {
        super.message(m);
        if ((m.getOpCode() == LnConstants.OPC_MULTI_SENSE) && ((m.getElement(1) & 0x7E) == 0x62)) {
            // device identity report
            if (m.getElement(3) == 0x01) {
                Integer extractedBoardId = 1 + ((m.getElement(1) & 0x1) << 7)
                        + (m.getElement(2) & 0x7F);
                if (!alreadyKnowThisBoardId(extractedBoardId)) {
                    addBoardIdToList(extractedBoardId);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(BDL16Panel.class);

}
