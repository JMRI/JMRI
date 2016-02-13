// FnMapPanelESU.java
package jmri.jmrit.symbolicprog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgPane;
import jmri.util.FileUtil;
import jmri.util.jdom.LocaleSelector;
import org.jdom2.Attribute;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Provide a graphical representation of the ESU mapping table. Each row
 * represents a possible mapping between input conditions (function keys, etc.)
 * and logical, physical or sound outputs.</p>
 * <p>
 * Uses data from the "model" element from the decoder definition file to
 * configure the number of rows and items and set up any custom item names:</p>
 * <dl>
 * <dt>extFnsESU="yes"</dt>
 * <dd>Uses the ESU-style function map rather than the NMRA style.</dd>
 * <dd>&nbsp;</dd>
 * <dt>numOuts</dt>
 * <dd>Highest item number to display.</dd>
 * <dd>&nbsp;</dd>
 * <dt>numFns</dt>
 * <dd>Number of mapping rows to display.</dd>
 * <dd>&nbsp;</dd>
 * <dt>output</dt>
 * <dd>name="n" label="theName"</dd>
 * <dd>&nbsp;-&nbsp;Set name of item number "n" to "theName".</dd>
 * <dd>&nbsp;</dd>
 * <dd>name="n" label="theName|OnChoice|OffChoice"</dd>
 * <dd>&nbsp;-&nbsp;Set name of item number "n" to "theName" and replace the
 * default "On and "Off" choices for enumChoice items.</dd>
 * <dd>&nbsp;</dd>
 * <dd>name="n" label="|"</dd>
 * <dd>&nbsp;-&nbsp;Cause item number "n" to be suppressed from the table.</dd>
 * <dd>&nbsp;</dd>
 * <dd>name="theName" label="OnChoice|OffChoice"</dd>
 * <dd>&nbsp;-&nbsp;Set name of the nth item to "theName" and replace the
 * default "On and "Off" choices for enumChoice items, where this line is the
 * nth "output" element of the "model" element in the decoder definition
 * file.</dd>
 * </dl>
 * <dl>
 * <dt>Default item headings:</dt>
 * <dd>Coded in String array itemDescESU[] of this class.</dd>
 * <dd>Item headings can be overridden by the "output" elements documented
 * above.</dd>
 * </dl>
 * <dl>
 * <dt>Items will be suppressed if any of the following are true:</dt>
 * <dd>No variables are found for that item.</dd>
 * <dd>The item output name is of the form name="n" label="|".</dd>
 * <dd>Item number is &gt; numOuts.</dd>
 * </dl>
 * <dl>
 * <dt>Variable definitions:</dt>
 * <dd>Are of the form "ESU Function Row xx Item yy" and are created "on the
 * fly" by this class. Up to 5,120 variables are needed to populate the function
 * map. It is more efficient to create these in code than to use XML in the
 * decoder file. <strong>DO NOT</strong> specify them in the decoder file.</dd>
 * </dl>
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author	Dave Heap Copyright (C) 2014
 * @version	$Revision: 24716 $
 */
public class FnMapPanelESU extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = -5897048084177413562L;

    // columns
    final int firstCol = 0;
    final int firstOut = 2;

    int currentCol = firstCol;

    // rows
    static final int HINTS_ROW = 0;
    static final int MOVE_ARROWS_TOP_ROW = 1;
    static final int BLOCK_NAME_ROW = 1;
    static final int FIRST_ROW = BLOCK_NAME_ROW + 2;
    static final int ROW_LABEL_ROW = FIRST_ROW - 1;

    static final int MAX_ROWS = 40;

    int currentRow = FIRST_ROW;

    static final int PI_CV = 16;
    static final int SI_START_CV = 2;
    static final int SI_CV_MODULUS = 16;
    static final int START_CV = 257;
    static final int CV_MODULUS = 256;
    static final int BIT_MODULUS = 8;

    GridBagLayout gl = null;
    GridBagConstraints cs = null;
    VariableTableModel _varModel;

    /**
     * Titles for blocks of items
     */
    final String[] outBlockName = new String[]{"Input Conditions", "Physical Outputs", "Logical Outputs", "Sounds"};

    /**
     * Number of items per block
     */
    final int[] outBlockLength = new int[]{36, 16, 16, 24};

    /**
     * Number of bits per block item
     */
    final int[] outBlockItemBits = new int[]{2, 1, 1, 1};

    final int[] outBlockStartCol = new int[outBlockLength.length]; // Starting column column of block
    final int[] outBlockUsed = new int[outBlockLength.length]; // Number of used items per block
    final JTextField[][] summaryLine = new JTextField[MAX_ROWS][outBlockLength.length];

    /**
     * <p>
     * Default column labels.
     * <dl>
     * <dt>Two rows are available for column labels</dt>
     * <dd>Use the '|' character to designate a row break</dd>
     * </dl></p>
     * <p>
     * Item labels can be overridden by the "output" element of the "model"
     * element from the decoder definition file.</p>
     */
    final String[] itemDescESU = new String[]{"Motion|Drive|Stop", "Direction|Forward|Reverse", "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12", "F13", "F14", "F15", "F16", "F17", "F18", "F19", "F20", "F21", "F22", "F23", "F24", "F25", "F26", "F27", "F28", "Wheel Sensor", "Sensor 1", "Sensor 2", "Sensor 3", "Sensor 4", "Head light[1]", "Rear light[1]", "Aux 1[1]", "Aux 2[1]", "Aux 3", "Aux 4", "Aux 5", "Aux 6", "Aux 7", "Aux 8", "Aux 9", "Aux 10", "Head light[2]", "Rear light[2]", "Aux 1[2]", "Aux 2[2]", "Momentum off", "Shunt mode", "Dynamic brake", "Uncouple Cycle", "|", "Fire box", "Dim lights", "Grade cross", "Smoke gen", "Notch up", "Notch down", "Sound fade", "Brk sql off", "Doppler effect", "Volume & mute", "Shift mode", "Sound slot 1", "Sound slot 2", "Sound slot 3", "Sound slot 4", "Sound slot 5", "Sound slot 6", "Sound slot 7", "Sound slot 8", "Sound slot 9", "Sound slot 10", "Sound slot 11", "Sound slot 12", "Sound slot 13", "Sound slot 14", "Sound slot 15", "Sound slot 16", "Sound slot 17", "Sound slot 18", "Sound slot 19", "Sound slot 20", "Sound slot 21", "Sound slot 22", "Sound slot 23", "Sound slot 24"
    };

    final int MAX_ITEMS = itemDescESU.length;
    final String[] itemLabel = new String[MAX_ITEMS];
    final String[][] itemName = new String[MAX_ITEMS][3];
    final boolean[] itemIsUsed = new boolean[MAX_ITEMS];
    final int iVarIndex[][] = new int[MAX_ITEMS][MAX_ROWS];

    // default values
    int numItems = MAX_ITEMS;
    int numRows = MAX_ROWS;

    // for row moves
    int selectedRow = -1;
    final JRadioButton rowButton[];

    public FnMapPanelESU(VariableTableModel v, List<Integer> varsUsed, Element model, RosterEntry rosterEntry, CvTableModel cvModel) {
        log.debug("ESU Function map starts");
        _varModel = v;

        // set up default names and labels
        for (int item = 0; item < MAX_ITEMS; item++) {
            itemLabel[item] = "";
            itemName[item][0] = "";
            itemName[item][1] = "";
            itemName[item][2] = "";
            itemIsUsed[item] = false;
            for (int iRow = 0; iRow < numRows; iRow++) {
                iVarIndex[item][iRow] = 0;
                for (int outBlockNum = 0; outBlockNum < outBlockLength.length; outBlockNum++) {
                    summaryLine[iRow][outBlockNum] = new JTextField(20);
                    summaryLine[iRow][outBlockNum].setHorizontalAlignment(JTextField.LEFT);
                    summaryLine[iRow][outBlockNum].setEditable(false);
                }
            }
        }
        // configure numRows(from numFns), numItems(from numOuts) & any custom labels from decoder file
        configOutputs(model);

        // initialize the layout
        gl = new GridBagLayout();
        cs = new GridBagConstraints();
        setLayout(gl);

        cs.anchor = GridBagConstraints.LINE_START;
        cs.gridwidth = GridBagConstraints.REMAINDER;
        saveAt(HINTS_ROW, 0, new JLabel("<html><em>(For hints and instructions for using this pane, see the </em><strong>&quot;Function Map&quot;</strong><em> section of the </em><strong>&quot;Read Me - IMPORTANT&quot;</strong><em> pane.)</em><br />&nbsp;</html>"));
        cs.gridwidth = 1;

        // for row moves
        ButtonGroup group = new ButtonGroup();
        rowButton = new JRadioButton[numRows];

        // add row move buttons
        AddRowMoveButtons();

        cs.anchor = GridBagConstraints.LINE_END;
        saveAt(ROW_LABEL_ROW, firstOut - 1, new JLabel("Row"));

        cs.anchor = GridBagConstraints.LINE_START;

        // loop through rows
        for (int iRow = 0; iRow < numRows; iRow++) {
            currentCol = firstCol;
            int outBlockNum = -1;
            int nextOutBlockStart = 0;
            int nextFreeBit = 0;
            // add row shift buttons
            {
                rowButton[iRow] = new JRadioButton();
                rowButton[iRow].setActionCommand(String.valueOf(iRow));
                rowButton[iRow].setToolTipText(Bundle.getMessage("FnMapESURowSelect"));
                rowButton[iRow].addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        selectedRow = Integer.valueOf(e.getActionCommand());
//                     log.info("selectedRow="+selectedRow);
                    }
                });
                group.add(rowButton[iRow]);
                cs.anchor = GridBagConstraints.CENTER;
                saveAt(currentRow, currentCol++, rowButton[iRow]);
            }
            cs.anchor = GridBagConstraints.LINE_END;
            saveAt(currentRow, currentCol++, new JLabel(Integer.toString(iRow + 1)));
            cs.anchor = GridBagConstraints.LINE_START;

            // loop through outputs (columns)
            int item = 0;
            do {
                JPanel blockPanel = new JPanel();
                GridBagLayout blockPanelLay = new GridBagLayout();
                GridBagConstraints blockPanelCs = new GridBagConstraints();

                JPanel blockItemsSelectorPanel = new JPanel();
                GridBagLayout bIsPlay = new GridBagLayout();
                GridBagConstraints bIsPcs = new GridBagConstraints();

                // check for block separators
                if (item == nextOutBlockStart) {
                    outBlockNum++;
                    outBlockStartCol[outBlockNum] = item;
                    nextOutBlockStart = item + outBlockLength[outBlockNum];
                    blockItemsSelectorPanel = new JPanel();
                    bIsPlay = new GridBagLayout();
                    bIsPcs = new GridBagConstraints();
                    blockItemsSelectorPanel.setLayout(bIsPlay);
                    bIsPcs.gridx = 0;
                    bIsPcs.gridy = 0;

                    blockPanelLay = new GridBagLayout();
                    blockPanelCs = new GridBagConstraints();
                    blockPanel.setLayout(blockPanelLay);
                    blockPanelCs.gridx = 0;
                    blockPanelCs.gridy = 0;
                }

                // block loop
                do {
                    // if column is not suppressed by blank headers
                    if (!itemName[item][0].equals("")) {
                        // set up the variable using the output label
                        String name = "ESU Function Row " + Integer.toString(iRow + 1) + " Item " + Integer.toString(item + 1);
                        int siCV = SI_START_CV + (iRow / SI_CV_MODULUS);
                        int iCV = START_CV + (((SI_CV_MODULUS * iRow) + (nextFreeBit / BIT_MODULUS)) % CV_MODULUS);
                        String thisCV = PI_CV + "." + siCV + "." + iCV;
                        int bitValue = (int) (Math.pow(2, outBlockItemBits[outBlockNum]) - 1) << (nextFreeBit % BIT_MODULUS);
                        String bitMask = "00000000" + Integer.toBinaryString(bitValue);
                        bitMask = (bitMask.substring(bitMask.length() - 8));
                        String bitPattern = bitMask.replace("0", "X").replace("1", "V");

                        // Get Cv value from file. We need to do this to get the GUI synchronised with cvModel initially
                        int savedValue = 0;
                        CvValue cvObject = cvModel.allCvMap().get(thisCV);
                        if (cvObject != null) {
                            savedValue = cvObject.getValue();
                        }
                        String defaultValue = Integer.toString((savedValue & bitValue) >>> (nextFreeBit % BIT_MODULUS));

                        {
                            // create a JDOM tree with some elements to add to _varModel
                            Element root = new Element("decoder-config");
                            Document doc = new Document(root);
                            doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

                            // set up choices
                            String defChoice1 = "On";
                            String defChoice2 = "Off";
                            if (!itemName[item][1].equals("")) {
                                defChoice1 = itemName[item][1];
                            }
                            if (!itemName[item][2].equals("")) {
                                defChoice2 = itemName[item][2];
                            }

                            // add some elements
                            Element thisVar;
                            if (outBlockItemBits[outBlockNum] == 2) {
                                root.addContent(new Element("decoder") // the sites information here lists all relevant
                                        .addContent(new Element("variables")
                                                .addContent(thisVar = new Element("variable")
                                                        .setAttribute("CV", thisCV)
                                                        .setAttribute("default", defaultValue)
                                                        .setAttribute("mask", bitPattern)
                                                        .setAttribute("item", name)
                                                        .setAttribute("readOnly", "no")
                                                        .addContent(new Element("enumVal")
                                                                .addContent(new Element("enumChoice")
                                                                        .setAttribute("choice", "-")
                                                                )
                                                                .addContent(new Element("enumChoice")
                                                                        .setAttribute("choice", defChoice1)
                                                                )
                                                                .addContent(new Element("enumChoice")
                                                                        .setAttribute("choice", defChoice2)
                                                                )
                                                        )
                                                )
                                        ) // variables element
                                ); // decoder element
                            } else {
                                root.addContent(new Element("decoder") // the sites information here lists all relevant
                                        .addContent(new Element("variables")
                                                .addContent(thisVar = new Element("variable")
                                                        .setAttribute("CV", thisCV)
                                                        .setAttribute("default", defaultValue)
                                                        .setAttribute("mask", bitPattern)
                                                        .setAttribute("item", name)
                                                        .setAttribute("readOnly", "no")
                                                        .addContent(new Element("enumVal")
                                                                .addContent(new Element("enumChoice")
                                                                        .setAttribute("choice", "Off")
                                                                )
                                                                .addContent(new Element("enumChoice")
                                                                        .setAttribute("choice", "On")
                                                                )
                                                        )
                                                )
                                        ) // variables element
                                ); // decoder element
                            }
                            // end of adding content

                            _varModel.setRow(0, thisVar);

                            // cleanup
                            root = null;
                            doc = null;
                            thisVar = null;
                        }

                        int iVar = _varModel.findVarIndex(name);  // now pick up the _varModel entry we just created

                        // hopefully we found it!
                        if (iVar >= 0) {
                            // try to find item  labels for itemLabel[item]
                            if (itemName[item][0].startsWith("Sound slot")) {
                                try {
                                    itemLabel[item] = rosterEntry.getSoundLabel(Integer.valueOf(itemName[item][0].substring(("Sound slot" + " ").length())));
                                } catch (Exception e) {
                                }
                            } else if (itemName[item][0].startsWith("F")) {
                                try {
                                    itemLabel[item] = rosterEntry.getFunctionLabel(Integer.valueOf(itemName[item][0].substring(1)));
                                } catch (Exception e) {
                                }
                            }
                            if (itemLabel[item] == null) {
                                itemLabel[item] = "";
                            }

                            // generate a fullItemName
                            String fullItemName = itemName[item][0];
                            if (itemLabel[item] != "") {
                                fullItemName = fullItemName + (": " + itemLabel[item]);
                            }

                            log.debug("Process var: {} as index {}", name, iVar);
                            varsUsed.add(Integer.valueOf(iVar));
                            JComponent varComp;
                            if (outBlockItemBits[outBlockNum] == 1) {
                                varComp = (JComponent) (_varModel.getRep(iVar, "checkbox"));
                            } else {
                                varComp = (JComponent) (_varModel.getRep(iVar, ""));
                            }
                            VariableValue var = _varModel.getVariable(iVar);
                            varComp.setToolTipText(PaneProgPane.addCvDescription(("Row " + Integer.toString(iRow + 1) + ", " + fullItemName), var.getCvDescription(), var.getMask()));
                            if (cvObject == null) {
                                cvObject = cvModel.allCvMap().get(thisCV); // case of new loco
                            }
                            if (cvObject != null) {
                                cvObject.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                                    public void propertyChange(java.beans.PropertyChangeEvent e) {
                                        updateAllSummaryLines();
                                    }
                                });
                            } else {
                                log.error("cvObject still null after attempt to allocate");
                            }

                            // add line to scroll pane
                            String label = itemName[item][0];
                            if (outBlockItemBits[outBlockNum] == 1) {
                                label = fullItemName;
                            }
                            bIsPcs.anchor = GridBagConstraints.LINE_START;
                            bIsPcs.gridx = outBlockItemBits[outBlockNum] % 2;
                            blockItemsSelectorPanel.add(new JLabel(label), bIsPcs);
                            bIsPcs.gridx = outBlockItemBits[outBlockNum] - 1;
                            blockItemsSelectorPanel.add(varComp, bIsPcs);
                            bIsPcs.gridy++;

                            itemIsUsed[item] = true;
                            iVarIndex[item][iRow] = iVar;
                        } else {
                            log.debug("Did not find var: {}", name);
                        }
                    }
                    nextFreeBit = nextFreeBit + outBlockItemBits[outBlockNum];

                    item++;
                } while ((item < nextOutBlockStart) && (item < numItems)); // end block loop

                // display block
                JScrollPane blockItemsScrollPane = new JScrollPane(blockItemsSelectorPanel);
                blockItemsScrollPane.setPreferredSize(new Dimension(400, 400));

                blockPanelCs.anchor = GridBagConstraints.LINE_START;
                blockPanelCs.gridx = 0;
                blockPanelCs.gridy = 0;
                blockPanelCs.insets = new Insets(0, 20, 0, 0);
                blockPanel.add(summaryLine[iRow][outBlockNum], blockPanelCs);
                updateSummaryLine(iRow, outBlockNum);

                JButton button = new JButton("Change");
                button.setActionCommand(iRow + "," + outBlockNum);
                button.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        String params[] = e.getActionCommand().split(",");
                        JOptionPane.showMessageDialog(
                                blockPanel, blockItemsScrollPane, "Row " + (Integer.valueOf(params[0]) + 1) + ", "
                                + outBlockName[Integer.valueOf(params[1])], JOptionPane.PLAIN_MESSAGE);
                    }
                });
                blockPanelCs.anchor = GridBagConstraints.LINE_START;
                blockPanelCs.gridx = 1;
                blockPanelCs.gridy = 0;
                blockPanelCs.insets = new Insets(0, 0, 0, 0);
                blockPanel.add(button, blockPanelCs);

                saveAt(currentRow, currentCol, blockPanel);
                currentCol++;

            } while (item < numItems); // end outputs (columns) loop

            saveAt(currentRow++, currentCol, new JLabel(Integer.toString(iRow + 1)));
        }  // end row loop

        saveAt(ROW_LABEL_ROW, currentCol, new JLabel("Row"));
        // tally used columns
        int currentBlock = -1;
        int blockStart = 0;
        for (int item = 0; item < MAX_ITEMS; item++) {
            if (item == blockStart) {
                currentBlock++;
                blockStart = blockStart + outBlockLength[currentBlock];
                outBlockUsed[currentBlock] = 0;
            }
            if (itemIsUsed[item]) {
                outBlockUsed[currentBlock]++;
            }
        }

        for (int iBlock = 0; iBlock < outBlockLength.length; iBlock++) {
            if (outBlockUsed[iBlock] > 0) {
                JLabel lx = new JLabel("<html><strong>&nbsp;" + outBlockName[iBlock] + "&nbsp;</strong></html>");
                GridBagConstraints csx = new GridBagConstraints();
                csx.gridy = BLOCK_NAME_ROW;
                csx.gridx = firstOut + iBlock;
                csx.insets = new Insets(0, 40, 0, 0);
                csx.gridwidth = 1;
                csx.anchor = GridBagConstraints.LINE_START;
                gl.setConstraints(lx, csx);
                add(lx);
            }
        }

        log.debug("Function map complete");
    }

    void updateAllSummaryLines() {
        for (int row = 0; row < numRows; row++) {
            for (int block = 0; block < outBlockLength.length; block++) {
                updateSummaryLine(row, block);
            }
        }
        return;
    }

    /**
     * Updates a summary line, including setting appropriate state.
     */
    void updateSummaryLine(int row, int block) {
        String retString = "";
        int retState = AbstractValue.SAME;

        for (int item = outBlockStartCol[block]; item < (outBlockStartCol[block] + outBlockLength[block]); item++) {
            if (itemIsUsed[item]) {
                int value = Integer.valueOf(_varModel.getValString(iVarIndex[item][row]));
                int state = _varModel.getState(iVarIndex[item][row]);
                if ((item == outBlockStartCol[block]) || (priorityValue(state) > priorityValue(retState))) {
                    retState = state;
                }
                if (value > 0) {
                    if (outBlockItemBits[block] == 1) {
                        if (itemLabel[item].equals("")) {
                            retString = retString + "," + itemName[item][0];
                        } else {
                            retString = retString + "," + itemLabel[item];
                        }
                    } else if (outBlockItemBits[block] == 2) {
                        if (value > 2) {
                            retString = retString + "," + "reserved value " + value;
                        } else if (itemName[item][value].equals("")) {
                            if (value == 1) {
                                retString = retString + "," + itemName[item][0];
                            } else if (value == 2) {
                                retString = retString + ",not " + itemName[item][0];
                            }
                        } else {
                            retString = retString + "," + itemName[item][value];
                        }
                    }
                }
            }
        }

        if (retString.startsWith(",")) {
            retString = retString.substring(1);
        }
        if (retString.equals("")) {
            retString = "-";
        }

        summaryLine[row][block].setBackground(AbstractValue.stateColorFromValue(retState));
        summaryLine[row][block].setText(retString);
        summaryLine[row][block].setToolTipText(retString);
        return;
    }

    /**
     * Assigns a priority value to a given state.
     */
    int priorityValue(int state) {
        int value = 0;
        switch (state) {
            case AbstractValue.UNKNOWN:
                value++;
                    //$FALL-THROUGH$
            case AbstractValue.DIFF:
                value++;
                    //$FALL-THROUGH$
            case AbstractValue.EDITED:
                value++;
                    //$FALL-THROUGH$
            case AbstractValue.FROMFILE:
                value++;
                    //$FALL-THROUGH$
            default:
                return value;
        }
    }

    void saveAt(int row, int column, JComponent j) {
        if (row < 0 || column < 0) {
            return;
        }
        cs.gridy = row;
        cs.gridx = column;
        gl.setConstraints(j, cs);
        add(j);
    }

    /**
     * Moves rows up or down
     * <p>
     * Row moves are for convenience purposes only. Decoder functioning is
     * unaffected by row position in mapping table.</p>
     *
     * @param increment number of rows to move by
     */
    void moveRow(int increment) {
        if (selectedRow == -1) {
            return;
        }
        if ((selectedRow + increment) < 0) {
            return;
        }
        if ((selectedRow + increment) >= numRows) {
            return;
        }
        int newRow = selectedRow + increment;
//         log.info("selectedRow="+selectedRow+",newRow="+newRow);
        // now to swap the data
        for (int item = 0; item < MAX_ITEMS; item++) {
            if (itemIsUsed[item]) {
                int selectedRowValue = Integer.valueOf(_varModel.getValString(iVarIndex[item][selectedRow]));
                int newRowValue = Integer.valueOf(_varModel.getValString(iVarIndex[item][newRow]));
                _varModel.setIntValue(iVarIndex[item][selectedRow], newRowValue);
                _varModel.setIntValue(iVarIndex[item][newRow], selectedRowValue);
            }
        }

        selectedRow = newRow;
        rowButton[selectedRow].setSelected(true);

    }

    /**
     * Adds the Row Move buttons at top and bottom
     */
    void AddRowMoveButtons() {
        {
            JButton button = new JButton(new ImageIcon(FileUtil.findURL("resources/icons/misc/ArrowUp-16.png")));
            button.setActionCommand(String.valueOf(-1));
            button.setToolTipText(Bundle.getMessage("FnMapESUMoveUp"));
//             button.setBorderPainted(false);
            button.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    moveRow(Integer.valueOf(e.getActionCommand()));
                }
            });
            cs.anchor = GridBagConstraints.CENTER;
            saveAt(MOVE_ARROWS_TOP_ROW, 0, button);
        }
        {
            JButton button = new JButton(new ImageIcon(FileUtil.findURL("resources/icons/misc/ArrowDown-16.png")));
            button.setActionCommand(String.valueOf(1));
            button.setToolTipText(Bundle.getMessage("FnMapESUMoveDown"));
//             button.setBorderPainted(false);
            button.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    moveRow(Integer.valueOf(e.getActionCommand()));
                }
            });
            cs.anchor = GridBagConstraints.CENTER;
            saveAt(MOVE_ARROWS_TOP_ROW, 1, button);
        }
        {
            JButton button = new JButton(new ImageIcon(FileUtil.findURL("resources/icons/misc/ArrowUp-16.png")));
            button.setActionCommand(String.valueOf(-1));
            button.setToolTipText(Bundle.getMessage("FnMapESUMoveUp"));
//             button.setBorderPainted(false);
            button.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    moveRow(Integer.valueOf(e.getActionCommand()));
                }
            });
            cs.anchor = GridBagConstraints.CENTER;
            saveAt(FIRST_ROW + numRows, 0, button);
        }
        {
            JButton button = new JButton(new ImageIcon(FileUtil.findURL("resources/icons/misc/ArrowDown-16.png")));
            button.setActionCommand(String.valueOf(1));
            button.setToolTipText(Bundle.getMessage("FnMapESUMoveDown"));
//             button.setBorderPainted(false);
            button.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    moveRow(Integer.valueOf(e.getActionCommand()));
                }
            });
            cs.anchor = GridBagConstraints.CENTER;
            saveAt(FIRST_ROW + numRows, 1, button);
        }
    }

    /**
     * Use the "model" element from the decoder definition file to configure the
     * number of rows and columns and set up any custom column names.
     */
    protected void configOutputs(Element model) {
        if (model == null) {
            log.debug("configOutputs was given a null model");
            return;
        }
        // get numOuts, numFns or leave the defaults
        Attribute a = model.getAttribute("numOuts");
        try {
            if (a != null) {
                numItems = Integer.valueOf(a.getValue()).intValue();
            }
        } catch (Exception e) {
            log.error("error handling decoder's numOuts value");
        }
        if (numItems > MAX_ITEMS) {
            log.error("numOuts=" + numItems + " exceeds the maximum number of items (" + MAX_ITEMS + ") defined in the code");
            numItems = Math.min(numItems, MAX_ITEMS);
        }
        a = model.getAttribute("numFns");
        try {
            if (a != null) {
                numRows = Integer.valueOf(a.getValue()).intValue();
            }
        } catch (Exception e) {
            log.error("error handling decoder's numFns value");
        }
        if (numRows > MAX_ROWS) {
            log.error("numFns=" + numRows + " exceeds the maximum number of rows (" + MAX_ROWS + ") defined in the code");
            numRows = Math.min(numRows, MAX_ROWS);
        }
        log.debug("numFns, numOuts {}, {}", numRows, numItems);

        // add ESU default split labels before reading custom ones
        for (int item = 0; item < MAX_ITEMS; item++) {
            loadSplitLabel(item, itemDescESU[item]);
        }

        // take all "output" children
        List<Element> elemList = model.getChildren("output");
        log.debug("output scan starting with {} elements", elemList.size());
        for (int i = 0; i < elemList.size(); i++) {
            Element e = elemList.get(i);
            String name = e.getAttribute("name").getValue();
            // if this a number, or a character name?
            try {
                int outputNum = Integer.valueOf(name).intValue();
                // yes, since it was converted.  All we do with
                // these are store the label index (if it exists)
                String at = LocaleSelector.getAttribute(e, "label");
                if (at != null) {
                    loadSplitLabel(outputNum - 1, at);
                }
            } catch (java.lang.NumberFormatException ex) {
                // not a number, must be a name
                if (i < MAX_ITEMS) {
                    itemName[i][0] = name;
                    itemName[i][1] = "";
                    itemName[i][2] = "";
                    String at;
                    if ((at = LocaleSelector.getAttribute(e, "label")) != null) {
                        loadSplitLabel(i, name + "|" + at);
                    }
                }
            }
        }
    }

    // split and load labels
    void loadSplitLabel(int item, String theLabel) {
        if (item < MAX_ITEMS) {
            String itemList[] = theLabel.split("\\|");
            if (theLabel.equals("|")) {
                itemName[item][0] = "";
                itemName[item][1] = "";
                itemName[item][2] = "";
            } else if (itemList.length == 1) {
                itemName[item][0] = itemList[0];
                itemName[item][1] = "";
            } else if (itemList.length == 2) {
                itemName[item][0] = itemList[0];
                itemName[item][1] = itemList[1];
                itemName[item][2] = "";
            } else if (itemList.length > 2) {
                itemName[item][0] = itemList[0];
                itemName[item][1] = itemList[1];
                itemName[item][2] = itemList[2];
            }
        }
    }

    /**
     * clean up at end
     */
    public void dispose() {
        removeAll();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(FnMapPanelESU.class.getName());
}
