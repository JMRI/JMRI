package jmri.jmrit.symbolicprog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.*;
import java.util.MissingResourceException;
import java.util.stream.IntStream;
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
import jmri.util.CvUtil;
import jmri.util.FileUtil;
import jmri.util.jdom.LocaleSelector;

import org.jdom2.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a graphical representation of the ESU mapping table. Each row
 * represents a possible mapping between input conditions (function keys, etc.)
 * and logical, physical or sound outputs.
 * <p>
 * Uses data from the "model" and "family" elements from the decoder definition
 * file to configure the number of rows and set up any custom item names:
 * <dl>
 * <dt>extFnsESU</dt>
 * <dd>Uses the ESU-style function map rather than the NMRA style.</dd>
 * <dd>&nbsp;&nbsp;&nbsp;extFnsESU="V4" for generation 4 decoders.</dd>
 * <dd>&nbsp;&nbsp;&nbsp;extFnsESU="V5" for generation 5 decoders.</dd>
 * <dd>&nbsp;</dd>
 * <dt>numOuts</dt>
 * <dd>Number of physical outputs (information only, not used by the code).</dd>
 * <dd>&nbsp;</dd>
 * <dt>numFns</dt>
 * <dd>Number of mapping rows to display.</dd>
 * <dd><em>Only use this parameter if the specific decoder definition implements
 * less rows than the default for that decoder generation (V4/V5), for example
 * the LokPilot V4.</em></dd>
 * <dd>&nbsp;</dd>
 * <dt>output (in "family" or "model")</dt>
 * <dd>name="blockNo,itemNo" label="theName"</dd>
 * <dd>&nbsp;-&nbsp;Set name of block "blockNo", item "itemNo" to
 * "theName".</dd>
 * <dd>&nbsp;</dd>
 * <dd>name="blockNo,itemNo" label="theName|OnChoice|OffChoice"</dd>
 * <dd>&nbsp;-&nbsp;Set name of block "blockNo", item "itemNo" to "theName" and
 * replace the default "On and "Off" choices for enumChoice items.</dd>
 * <dd>&nbsp;</dd>
 * <dd>name="blockNo,itemNo" label="|"</dd>
 * <dd>&nbsp;-&nbsp;Cause item block "blockNo", item "itemNo" to be suppressed
 * from the table.</dd>
 * <dd>&nbsp;</dd>
 * <dd>name="itemNo" label="..."</dd>
 * <dd>&nbsp;-&nbsp;As above, but using an absolute "itemNo" (not
 * recommended).</dd>
 * <dd>&nbsp;</dd>
 * <dd>name="theName" label="OnChoice|OffChoice"</dd>
 * <dd>&nbsp;-&nbsp;Set name of the nth item to "theName" and replace the
 * default "On and "Off" choices for enumChoice items, where this line is the
 * nth "output" element of the "model" element in the decoder definition file
 * (not recommended).</dd>
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
 * fly" by this class. Many thousands of variables are needed to populate the
 * function map. It is more efficient to create these in code than to use XML in
 * the decoder file. <strong>DO NOT</strong> specify them in the decoder
 * file.</dd>
 * <dd><br>
 * The "tooltip" &amp; "label" attributes on a fnmapping variable are ignored.
 * Expanded internationalized tooltips are generated in the code.
 * </dd>
 * </dl>
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Dave Heap Copyright (C) 2016, 2019
 */
public final class FnMapPanelESU extends JPanel {

    // columns
    int firstCol = 0;
    int firstOut = 2;

    int currentCol = firstCol;

    // rows
    static final int HINTS_ROW = 0;
    static final int MOVE_ARROWS_TOP_ROW = 1;
    static final int BLOCK_NAME_ROW = 1;
    static final int FIRST_ROW = BLOCK_NAME_ROW + 2;
    static final int ROW_LABEL_ROW = FIRST_ROW - 1;

    int currentRow = FIRST_ROW;

    static final int PI_CV = 16;
    static final int SI_START_CV = 2;
    static final int SI_CV_MODULUS = 16;
    static final int START_CV = 257;
    static final int CV_PAGE_MODULUS = 256;
    static final int BIT_MODULUS = 8;

    GridBagLayout gl = null;
    GridBagConstraints cs = null;
    VariableTableModel varModel;
    // Titles for blocks of items.
    String[] outBlockName;
    // Number of items per block.
    int[] outBlockLength;
    int[] outBlockSiStartCv;
    int[] outBlockSiCvModulus;
    int[] outBlockStartCv;
    int[] outBlockCvModulus;
    // Number of bits per block item.
    int[] outBlockItemBits;
    // Starting column column of block.
    int[] outBlockStartCol;
    // Number of used items per block.
    int[] outBlockUsed;
    JTextField[][] summaryLine;
    int maxItems;
    // Default item labels.
    String[] itemDescESU;
    /**
     * Default item labels.
     * <dl>
     * <dt>Two rows are available for item labels</dt>
     * <dd>Use the '|' character to designate a row break</dd>
     * </dl>
     * <p>
     * Item labels can be overridden by the "output" element of the "model" or
     * "family" element from the decoder definition file.
     */
    String[] itemLabel;
    String[][] itemName;
    boolean[] itemIsUsed;
    int[][] iVarIndex;

    // default values
    String extFnsESU = "no";
    int numItems = -1;
    int numFns = 1;
    int numRows = 5;
    int numOuts = 2;
    int numStates = 2;
    int numWheelSensors = 1;
    int numReserved = 1;
    int numSensors = 4;
    int numConfig2 = 4;
    int numLogic = 1;
    int numSounds = 1;

    // for row moves
    int selectedRow = -1;
    JRadioButton[] rowButton;

    public FnMapPanelESU(VariableTableModel v, List<Integer> varsUsed,
            Element model, RosterEntry rosterEntry, CvTableModel cvModel) {

        // retrieve function map version
        Attribute a = model.getAttribute("extFnsESU");
        try {
            if (a != null) {
                extFnsESU = (a.getValue());
            }
        } catch (Exception ex) {
            log.error("error handling decoder's extFnsESU value");
        }

        switch (extFnsESU) {
            case "V4":
            case "yes":
                numFns = 29;
                numReserved = 0;
                numOuts = 12;
                numLogic = 16;
                numSounds = 24;
                numRows = 40;
                outBlockSiStartCv = new int[]{2, 2, 2, 2};
                outBlockSiCvModulus = new int[]{16, 16, 16, 16};
                outBlockStartCv = new int[]{257, 266, 268, 270};
                outBlockCvModulus = new int[]{16, 16, 16, 16};

                break;
            case "V5":
                numFns = 32;
                numReserved = 1;
                numOuts = 20;
                numLogic = 24;
                numSounds = 32;
                numRows = 72;
                outBlockSiStartCv = new int[]{3, 8, 8, 8};
                outBlockSiCvModulus = new int[]{16, 16, 16, 16};
                outBlockStartCv = new int[]{257, 257, 260, 263};
                outBlockCvModulus = new int[]{16, 16, 16, 16};
                break;
            default:
                throw new IllegalArgumentException("Invalid extFnsESU value '" + extFnsESU + "'");
        }

        // get relevant model attributes
        loadModelAttributes(model);

        // build outBlock*** arrays
        outBlockLength = new int[]{numStates + numFns + numWheelSensors + numReserved + numSensors,
            numOuts + numConfig2, numLogic, numSounds
        };
        outBlockItemBits = new int[]{2, 1, 1, 1};

        log.debug(
                "Constructor outBlockLength[0]={}, outBlockLength[1]={}, outBlockLength[2]={}, outBlockLength[3]={}",
                outBlockLength[0], outBlockLength[1], outBlockLength[2], outBlockLength[3]);

        maxItems = IntStream.of(outBlockLength).sum();
        if (numItems <= 0) {
            numItems = maxItems;
        }
        if (numItems > maxItems) {
            log.error("numItems=" + numItems + " exceeds the maximum number of items (" + maxItems + ") defined in the code");
            numItems = Math.min(numItems, maxItems);
        }

        log.debug(
                "Constructor numFns={}, numRows={}, numOuts={}, numItems={}, maxItems={}",
                numFns, numRows, numOuts, numItems, maxItems);

        itemName = new String[maxItems][3];
        iVarIndex = new int[maxItems][numRows];
        itemIsUsed = new boolean[maxItems];
        itemLabel = new String[maxItems];
        itemDescESU = new String[maxItems];
        summaryLine = new JTextField[numRows][outBlockLength.length];
        outBlockUsed = new int[outBlockLength.length];
        outBlockStartCol = new int[outBlockLength.length];
        outBlockName = new String[outBlockLength.length];

        log.debug(
                "ESU Function map starts");
        varModel = v;

        setupDefaultNamesLabels();

        // configure numRows(from numFns), numItems(from numOuts) & any custom labels from decoder file
        configOutputs(model);

        // initialize the layout
        gl = new GridBagLayout();
        cs = new GridBagConstraints();

        setLayout(gl);

        cs.anchor = GridBagConstraints.LINE_START;
        cs.gridwidth = GridBagConstraints.REMAINDER;

        saveAt(HINTS_ROW,
                0, new JLabel("<html><em>(For hints and instructions for using this pane, see the </em><strong>&quot;Function Map&quot;</strong><em> section of the </em><strong>&quot;Read Me - IMPORTANT&quot;</strong><em> pane.)</em><br />&nbsp;</html>"));
        cs.gridwidth = 1;

        // for row moves
        ButtonGroup group = new ButtonGroup();
        rowButton = new JRadioButton[numRows];

        // add row move buttons
        addRowMoveButtons();

        cs.anchor = GridBagConstraints.LINE_END;

        saveAt(ROW_LABEL_ROW, firstOut
                - 1, new JLabel("Row"));

        cs.anchor = GridBagConstraints.LINE_START;

        int siCV = 0;

        // loop through rows
        for (int iRow = 0;
                iRow < numRows;
                iRow++) {
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
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        selectedRow = Integer.parseInt(e.getActionCommand());
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
                GridBagLayout blockPanelLay;
                GridBagConstraints blockPanelCs = new GridBagConstraints();

                JPanel blockItemsSelectorPanel = new JPanel();
                GridBagLayout bIsPlay;
                GridBagConstraints bIsPcs = new GridBagConstraints();

                // check for block separators
                if (item == nextOutBlockStart) {
                    outBlockNum++;
                    outBlockStartCol[outBlockNum] = item;
                    nextOutBlockStart = item + outBlockLength[outBlockNum];
                    blockItemsSelectorPanel = new JPanel();
                    siCV = outBlockSiStartCv[outBlockNum] + (iRow / outBlockSiCvModulus[outBlockNum]);
                    nextFreeBit = 0;

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
                        int iCV = outBlockStartCv[outBlockNum] + (((outBlockCvModulus[outBlockNum] * iRow) + (nextFreeBit / BIT_MODULUS)) % CV_PAGE_MODULUS);
                        String thisCV = PI_CV + "." + siCV + "." + iCV;
                        int bitValue = (int) (Math.pow(2, outBlockItemBits[outBlockNum]) - 1) << (nextFreeBit % BIT_MODULUS);
                        String bitMask = "00000000" + Integer.toBinaryString(bitValue);
                        bitMask = (bitMask.substring(bitMask.length() - 8));
                        String bitPattern = bitMask.replace("0", "X").replace("1", "V");
                        log.debug("Block {} CV{} mask'{}' {}", outBlockNum, thisCV, bitPattern, name);

                        // Get Cv value from file. We need to do this to get the GUI synchronised with cvModel initially
                        int savedValue = 0;
                        CvValue cvObject = cvModel.allCvMap().get(thisCV);
                        if (cvObject != null) {
                            savedValue = cvObject.getValue();
                        }
                        String defaultValue = Integer.toString((savedValue & bitValue) >>> (nextFreeBit % BIT_MODULUS));

                        {
                            // create a JDOM tree with some elements to add to varModel
                            Element root = new Element("decoder-config");
                            Document doc = new Document(root);
                            doc.setDocType(new DocType("decoder-config"));

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

                            varModel.setRow(0, thisVar);

                            // cleanup
                            root = null;
                            doc = null;
                            thisVar = null;
                        }

                        int iVar = varModel.findVarIndex(name);  // now pick up the varModel entry we just created

                        // hopefully we found it!
                        if (iVar >= 0) {
                            // try to find item  labels for itemLabel[item]
                            if (itemName[item][0].startsWith(Bundle.getMessage("FnMapSndSlot"))) {
                                try {
                                    itemLabel[item] = rosterEntry.getSoundLabel(Integer.parseInt(itemName[item][0].substring((Bundle.getMessage("FnMapSndSlot") + " ").length())));
                                } catch (NumberFormatException e) {
                                    log.warn("Error for sound slot label \"{}\" in \"{}\"", itemName[item][0], item);
                                }
                            } else if (itemName[item][0].matches("F\\d+")) {
                                try {
                                    int fn = Integer.parseInt(itemName[item][0].substring(1));
                                    if (fn <= rosterEntry.getMAXFNNUM()) {
                                        itemLabel[item] = rosterEntry.getFunctionLabel(fn);
                                    }
                                } catch (NumberFormatException e) {
                                    log.warn("Error for function label \"{}\" in \"{}\"", itemName[item][0], item);
                                }
                            }
                            if (itemLabel[item] == null) {
                                itemLabel[item] = "";
                            }

                            // generate a fullItemName
                            String fullItemName = itemName[item][0];
                            if (!itemLabel[item].equals("")) {
                                fullItemName = fullItemName + (": " + itemLabel[item]);
                            }

                            log.debug("Process var: {} as index {}", name, iVar);
                            varsUsed.add(iVar);
                            JComponent varComp;
                            if (outBlockItemBits[outBlockNum] == 1) {
                                varComp = (JComponent) (varModel.getRep(iVar, "checkbox"));
                            } else {
                                varComp = (JComponent) (varModel.getRep(iVar, ""));
                            }
                            VariableValue var = varModel.getVariable(iVar);
                            varComp.setToolTipText(CvUtil.addCvDescription((Bundle.getMessage("FnMapESURow") + " " + Integer.toString(iRow + 1) + ", " + fullItemName), var.getCvDescription(), var.getMask()));
                            if (cvObject == null) {
                                cvObject = cvModel.allCvMap().get(thisCV); // case of new loco
                            }
                            if (cvObject != null) {
                                cvObject.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                                    private int row;
                                    private int block;

                                    @Override
                                    public void propertyChange(java.beans.PropertyChangeEvent e) {
                                        log.debug("Updating Summary Line for row {} block {}", row, block);
                                        updateSummaryLine(row, block);
                                    }

                                    private java.beans.PropertyChangeListener init(int i, int j) {
                                        row = i;
                                        block = j;
                                        return this;
                                    }
                                }.init(iRow, outBlockNum));
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
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        String params[] = e.getActionCommand().split(",");
                        JOptionPane.showMessageDialog(
                                blockPanel, blockItemsScrollPane, "Row " + (Integer.parseInt(params[0]) + 1) + ", "
                                + outBlockName[Integer.parseInt(params[1])], JOptionPane.PLAIN_MESSAGE);
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

        saveAt(ROW_LABEL_ROW, currentCol,
                new JLabel(Bundle.getMessage("FnMapESURow")));
        // tally used columns
        int currentBlock = -1;
        int blockStart = 0;
        for (int item = 0;
                item < maxItems;
                item++) {
            if (item == blockStart) {
                currentBlock++;
                blockStart = blockStart + outBlockLength[currentBlock];
                outBlockUsed[currentBlock] = 0;
            }
            if (itemIsUsed[item]) {
                outBlockUsed[currentBlock]++;
            }
        }

        // Create formatted block labels
        for (int iBlock = 0;
                iBlock < outBlockLength.length;
                iBlock++) {
            if (outBlockUsed[iBlock] > 0) {
                StringBuilder label = new StringBuilder("<html><strong>" + outBlockName[iBlock]);
                try {
                    String s = Bundle.getMessage("FnMapESUBlockDesc_" + (iBlock + 1));
                    label.append("</strong><br>");
                    label.append(s);
                    label.append("</html>");
                } catch (MissingResourceException e) {
                    label.append("</strong></html>");
                }
                JLabel lx = new JLabel(label.toString());
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

        log.debug(
                "Function map complete");
    }

    /**
     * Set up the default names and labels.
     */
    void setupDefaultNamesLabels() {
        // get block names
        for (int i = 0;
                i < outBlockName.length;
                i++) {
            outBlockName[i] = Bundle.getMessage("FnMapESUBlockName_" + (i + 1));
        }

        // make item names
        int item = 0;
        itemDescESU[item++] = Bundle.getMessage("FnMap_STATE") + "|" + Bundle.getMessage("FnMap_DRIVE") + "|" + Bundle.getMessage("FnMap_STOP");
        itemDescESU[item++] = Bundle.getMessage("FnMap_DIR") + "|" + Bundle.getMessage("FnMap_FWD") + "|" + Bundle.getMessage("FnMap_REV");
        for (int i = 0; i < numFns; i++) {
            itemDescESU[item++] = "F" + i;
        }
        itemDescESU[item++] = Bundle.getMessage("FnMap_WS");
        if (extFnsESU.equalsIgnoreCase("V5")) {
            itemDescESU[item++] = Bundle.getMessage("FnMap_RS");
        }
        for (int i = 1; i <= numSensors; i++) {
            itemDescESU[item++] = Bundle.getMessage("FnMap_S") + " " + i;
        }
        itemDescESU[item++] = Bundle.getMessage("FnMap_HL") + "[1]";
        itemDescESU[item++] = Bundle.getMessage("FnMap_RL") + "[1]";
        for (int i = 1; i <= numOuts - 2; i++) {
            itemDescESU[item++] = Bundle.getMessage("FnMap_A") + " " + i + (i <= 2 ? "[1]" : "");
        }
        itemDescESU[item++] = Bundle.getMessage("FnMap_HL") + "[2]";
        itemDescESU[item++] = Bundle.getMessage("FnMap_RL") + "[2]";
        for (int i = 1; i <= 2; i++) {
            itemDescESU[item++] = Bundle.getMessage("FnMap_A") + " " + i + "[2]";
        }
        for (int i = 1; i <= outBlockLength[2]; i++) {
            itemDescESU[item] = "(output 3," + Integer.toString(i) + ")"; // dummy, use output labels
            item++;
        }
        for (int i = 1; i <= outBlockLength[3]; i++) {
            itemDescESU[item++] = Bundle.getMessage("FnMapSndSlot") + " " + i;
        }

        // set up default names and labels
        for (int itemNum = 0;
                itemNum < maxItems;
                itemNum++) {
            itemLabel[itemNum] = "";
            itemName[itemNum][0] = "";
            itemName[itemNum][1] = "";
            itemName[itemNum][2] = "";
            itemIsUsed[itemNum] = false;
            for (int iRow = 0; iRow < numRows; iRow++) {
                iVarIndex[itemNum][iRow] = 0;
                for (int outBlockNum = 0; outBlockNum < outBlockLength.length; outBlockNum++) {
                    summaryLine[iRow][outBlockNum] = new JTextField(20);
                    summaryLine[iRow][outBlockNum].setHorizontalAlignment(JTextField.LEFT);
                    summaryLine[iRow][outBlockNum].setEditable(false);
                }
            }
        }
    }

    /**
     * Updates all summary lines, including setting appropriate states.
     */
    void updateAllSummaryLines() {
        for (int row = 0; row < numRows; row++) {
            for (int block = 0; block < outBlockLength.length; block++) {
                updateSummaryLine(row, block);
            }
        }
    }

    /**
     * Updates a summary line at the specified location, including setting
     * appropriate state.
     *
     * @param row   the row to update
     * @param block the block to update
     */
    void updateSummaryLine(int row, int block) {
        StringBuilder retString = new StringBuilder("");
        int retState = AbstractValue.SAME;

        for (int item = outBlockStartCol[block]; item < (outBlockStartCol[block] + outBlockLength[block]); item++) {
            if (itemIsUsed[item]) {
                int value = Integer.parseInt(varModel.getValString(iVarIndex[item][row]));
                int state = varModel.getState(iVarIndex[item][row]);
                if ((item == outBlockStartCol[block]) || (priorityValue(state) > priorityValue(retState))) {
                    retState = state;
                }
                if (value > 0) {
                    if (outBlockItemBits[block] == 1) {
                        if (itemLabel[item].equals("")) {
                            retString.append(",").append(itemName[item][0]);
                        } else {
                            retString.append(",").append(itemLabel[item]);
                        }
                    } else if (outBlockItemBits[block] == 2) {
                        if (value > 2) {
                            retString.append(",").append("reserved value ").append(value);
                        } else if (itemName[item][value].equals("")) {
                            if (value == 1) {
                                retString.append(",").append(itemName[item][0]);
                            } else {
                                retString.append(",not ").append(itemName[item][0]);
                            }
                        } else {
                            retString.append(",").append(itemName[item][value]);
                        }
                    }
                }
            }
        }

        if (retString.length() == 0) {
            retString.append("-");
        } else if (retString.charAt(0) == ',') {
            retString.deleteCharAt(0);
        }

        summaryLine[row][block].setBackground(AbstractValue.stateColorFromValue(retState));
        summaryLine[row][block].setText(retString.toString());
        summaryLine[row][block].setToolTipText(retString.toString());
    }

    /**
     * Assigns a priority value to a specified state.
     *
     * @param state the state
     * @return the assigned priority value
     */
    @SuppressFBWarnings({"SF_SWITCH_NO_DEFAULT", "SF_SWITCH_FALLTHROUGH"})
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

    /**
     * Saves an item at the specified row and column.
     *
     * @param row    the row
     * @param column the column
     * @param j      the item
     */
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
     * Moves rows up or down.
     * <p>
     * Row moves are for convenience purposes only. Decoder functioning is
     * unaffected by row position in mapping table.
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
        // now to swap the data
        for (int item = 0; item < maxItems; item++) {
            if (itemIsUsed[item]) {
                int selectedRowValue = Integer.parseInt(varModel.getValString(iVarIndex[item][selectedRow]));
                int newRowValue = Integer.parseInt(varModel.getValString(iVarIndex[item][newRow]));
                varModel.setIntValue(iVarIndex[item][selectedRow], newRowValue);
                varModel.setIntValue(iVarIndex[item][newRow], selectedRowValue);
            }
        }

        selectedRow = newRow;
        rowButton[selectedRow].setSelected(true);

    }

    /**
     * Adds the Row Move buttons at top and bottom.
     */
    void addRowMoveButtons() {
        {
            JButton button = new JButton(new ImageIcon(FileUtil.findURL("resources/icons/misc/ArrowUp-16.png")));
            button.setActionCommand(String.valueOf(-1));
            button.setToolTipText(Bundle.getMessage("FnMapESUMoveUp"));
//             button.setBorderPainted(false);
            button.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    moveRow(Integer.parseInt(e.getActionCommand()));
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
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    moveRow(Integer.parseInt(e.getActionCommand()));
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
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    moveRow(Integer.parseInt(e.getActionCommand()));
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
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    moveRow(Integer.parseInt(e.getActionCommand()));
                }
            });
            cs.anchor = GridBagConstraints.CENTER;
            saveAt(FIRST_ROW + numRows, 1, button);
        }
    }

    /**
     * Use the "model" and "family" elements from the decoder definition file to
     * configure the number of rows and columns and set up any custom column
     * names.
     *
     * @param model the "model" element from the decoder definition file
     */
    void configOutputs(Element model) {
        if (model == null) {
            log.debug("configOutputs was given a null model");
            return;
        }
        Element family;
        Parent parent = model.getParent();
        if (parent != null && parent instanceof Element) {
            family = (Element) parent;
        } else {
            log.debug("configOutputs found an invalid parent family");
            return;
        }

        // add ESU default split labels before reading custom ones
        for (int item = 0; item < maxItems; item++) {
            loadSplitLabel(item, itemDescESU[item]);
        }

        // take all "output" children
        List<Element> elemList = new ArrayList<>();
        addOutputElements(family.getChildren(), elemList);
        addOutputElements(model.getChildren(), elemList);

        log.debug("output scan starting with {} elements", elemList.size());

        for (int i = 0; i < elemList.size(); i++) {
            Element e = elemList.get(i);
            String name = e.getAttribute("name").getValue();
            log.debug("output element name: {} value: {}", e.getAttributeValue("name"), e.getAttributeValue("label"));
            // does this element have a label?
            String label = LocaleSelector.getAttribute(e, "label");
            if (label != null) {
                parseLoadLabel(i, name, label);
            }
        }
    }

    /**
     * Use the "model" element from the decoder definition file to fetch
     * attributes relevant to building this function map.
     *
     * @param model the "model" element from the decoder definition file
     */
    void loadModelAttributes(Element model) {

        Attribute a;

        if (model == null) {
            log.debug("configOutputs was given a null model");
            return;
        }

        // get numOuts, numFns or leave the defaults
        a = model.getAttribute("numFns");
        try {
            if (a != null) {
                numRows = Integer.parseInt(a.getValue());
            }
        } catch (NumberFormatException e) {
            log.error("error handling decoder's numFns value");
        }
        log.debug("loadModelAttributes numFns={}, numRows={}, numOuts={}, numItems={}, {}", numFns, numRows, numOuts, numItems);
    }

    /**
     * Adds a list of "output" or "outputs" elements to an existing list.
     *
     * @param input      the list to add from
     * @param accumulate the list to add to
     */
    void addOutputElements(List<Element> input, List<Element> accumulate) {
        for (Element elem : input) {
            if (elem.getName().equals("outputs")) {
                log.debug(" found outputs element of size {}", elem.getChildren().size());
                addOutputElements(elem.getChildren(), accumulate);
            } else if (elem.getName().equals("output")) {
                log.debug("adding output element name: {} value: {}", elem.getAttributeValue("name"), elem.getAttributeValue("label"));
                accumulate.add(elem);
            }
        }
    }

    /**
     * Loads labels as per documentation at {@link FnMapPanelESU}.
     *
     * @param item  the item number to load
     * @param name  the "name" attribute from the "output" element
     * @param label the "label" attribute from the "output" element
     */
    void parseLoadLabel(int item, String name, String label) {
        // is the name a number?
        try {
            int outputNum = Integer.parseInt(name);
            // yes, since it was converted
            // store the label at the appropriate index
            log.debug("Output name='{}', label='{}' has an item number.", name, label);
            loadSplitLabel(outputNum - 1, label);
            return;
        } catch (java.lang.NumberFormatException ex) {
            log.debug("Output name='{}', label='{}' is not a simple item number.", name, label);
        }

        // see if it is a "block,item" construct
        String[] itemList = name.split(",");
        if (itemList.length == 2) {
            try {
                int blockNum = Integer.parseInt(itemList[0]);
                int blockItemNum = Integer.parseInt(itemList[1]);
                int itemNum = Arrays.stream(outBlockLength, 0, blockNum - 1).sum() + blockItemNum - 1;
                log.debug("Output name='{}', label='{}', blockNum='{}', blockItemNum='{}', itemNum='{}'.",
                        name, label, blockNum, blockItemNum, itemNum);
                loadSplitLabel(itemNum, label);
                return;
            } catch (java.lang.NumberFormatException ex1) {
                log.debug("Output name='{}', label='{}' is not a \"block,item\" construct.",
                        name, label);
            }
        }

        //  must be a name
        if (item < maxItems) {
            itemName[item][0] = name;
            itemName[item][1] = "";
            itemName[item][2] = "";
            log.debug("Output name='{}', label='{}' has no item number.", name, label);
            loadSplitLabel(item, name + "|" + label);
        }
    }

    /**
     * Splits a label as per documentation at {@link FnMapPanelESU}.
     *
     * @param item     the item number to load
     * @param theLabel the label attribute from the "output" element
     */
    void loadSplitLabel(int item, String theLabel) {
        if (item < maxItems) {
            String[] itemList = theLabel.split("\\|");
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
     * Clean up at end.
     */
    public void dispose() {
        varModel = null; // kills GC cycles during test
        for (int i = 0; i < rowButton.length; i++) {
            rowButton[i] = null;
        }
        for (JTextField[] summaryLine1 : summaryLine) {
            for (int j = 0; j < summaryLine1.length; j++) {
                summaryLine1[j] = null;
            }
        }
        removeAll();  // JPanel call
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(FnMapPanelESU.class
    );
}
