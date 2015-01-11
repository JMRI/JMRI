// FnMapPanelESU.java

package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import javax.swing.UIManager;
import java.awt.*;

import java.util.List;
import org.jdom2.Element;
import org.jdom2.Attribute;
import jmri.util.jdom.LocaleSelector;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgPane;
import jmri.util.SystemType;
import jmri.Application;
import jmri.jmrit.roster.RosterEntry;
import org.jdom2.*;

/**
 * <p>Provide a graphical representation of the ESU mapping table.
 * Each row represents a possible mapping between input conditions (function keys, etc.)
 * and logical, physical or sound outputs.</p>
 * <p>Uses data from the "model" element from the decoder definition file
 * to configure the number of rows and columns and set up any
 * custom column names:</p>
 *  <dl>
 *   <dt>extFnsESU="yes"</dt>
 *     <dd>Uses the ESU-style function map rather than the NMRA style.</dd>
 *     <dd>&nbsp;</dd>
 *   <dt>numOuts</dt>
 *     <dd>Highest column number to display.</dd>
 *     <dd>&nbsp;</dd>
 *   <dt>numFns</dt>
 *     <dd>Number of mapping rows to display.</dd>
 *     <dd>&nbsp;</dd>
 *   <dt>output</dt>
 *     <dd>name="n" label="yyy"</dd>
 *       <dd>&nbsp;-&nbsp;Set lower line of heading for column number "n" to "yyy".</dd>
 *       <dd>&nbsp;</dd>
 *     <dd>name="n" label="xxx|yyy"</dd>
 *       <dd>&nbsp;-&nbsp;Set upper line of heading for column number "n" to "xxx" and lower line to "yyy".</dd>
 *       <dd>&nbsp;</dd>
 *     <dd>name="n" label="|"</dd>
 *       <dd>&nbsp;-&nbsp;Sets both lines of heading for column number "n" to blank, causing the column to be suppressed from the table.</dd>
 *       <dd>&nbsp;</dd>
 *     <dd>name="text1" label="text2"</dd>
 *       <dd>&nbsp;-&nbsp;Set upper line of heading of the nth column to "xxx" and lower line to "yyy",
 *     where this line is the nth "output" element of the "model" element in the decoder definition file.</dd>
 * </dl>
 *  <dl>
 *  <dt>Default column headings:</dt>
 *   <dd>Coded in String array outDescESU[] of this class.</dd>
 *   <dd>Column headings can be overridden by the "output" elements documented above.</dd>
 *   <dd>&nbsp;</dd>
 *  <dt>Two rows are available for column headings:</dt>
 *   <dd>Use the "|" character to designate a row break.</dd>
 * </dl>
 *  <dl>
 *  <dt>Columns will be suppressed if any of the following are true:</dt>
 *   <dd>No variables are found for that column.</dd>
 *   <dd>The column output name is of the form name="n" label="|".</dd>
 *   <dd>Column number is &gt; numOuts.</dd>
 * </dl>
 *  <dl>
 *  <dt>Variable definitions:</dt>
 *   <dd>Are of the form "ESU Function Row xx Column yy" and are created "on the fly" by this class.
 *     Up to 5,120 variables are needed to populate the function map. It is more efficient to create
 *     these in code than to use XML in the decoder file. <strong>DO NOT</strong> specify them in the decoder file.</dd>
 * </dl>
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @author			Dave Heap   Copyright (C) 2014
 * @version			$Revision: 24716 $
 */
public class FnMapPanelESU extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5897048084177413562L;
	// columns
    int firstCol = 0;
    int firstOut = 1;
    int currentCol = firstCol;
    
    // rows
    int guiWarningRow = 0;
    int outputNumRow = guiWarningRow+1;
    int outputLabelRow = outputNumRow+1;
    int blockNameRow = guiWarningRow+1;
    int firstRow = blockNameRow+2;
    int currentRow = firstRow;
    
    // these will eventually be passed in from the ctor
    int numRows = 40;
    int numOut = 20;

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
     * How many columns between GUI warning intervals
     */
    final int GUIwarningInterval = 26;
    
    /**
     * Titles for blocks of items
     */
    final String[] outBlockName = new String[] {"Input Conditions","Physical Outputs","Logical Outputs","Sounds"};
    
    /**
     * Number of items per block
     */
    final int[] outBlockLength = new int[] {36,16,16,24};

    /**
     * Number of bits per block item
     */
    final int[] outBlockItemBits = new int[] {2,1,1,1};

    final int[] outBlockStartCol = new int[outBlockLength.length]; // Starting column column of block
    final int[] outBlockUsed = new int[outBlockLength.length]; // Number of used items per block
    final JTextField[][] summaryLine = new JTextField[numRows][outBlockLength.length];
    
    /**
     * <p>Default column labels.
     * <dl>
     *  <dt>Two rows are available for column labels</dt>
     *   <dd>Use the '|' character to designate a row break</dd>
     * </dl></p>
     * <p>Column labels can be overridden by the "output" element of the "model" element from the decoder definition file.</p>
     */
    final String[] outDescESU = new String[] {"Motion|Drive|Stop","Direction|Forward|Reverse"
        ,"F0","F1","F2","F3","F4","F5","F6","F7","F8","F9"
        ,"F10","F11","F12","F13","F14","F15","F16","F17","F18","F19"
        ,"F20","F21","F22","F23","F24","F25","F26","F27","F28"
        ,"Wheel Sensor","Sensor 1","Sensor 2","Sensor 3","Sensor 4"
        ,"Head light[1]","Rear light[1]","Aux 1[1]","Aux 2[1]","Aux 3","Aux 4","Aux 5","Aux 6","Aux 7","Aux 8","Aux 9","Aux 10","Head light[2]","Rear light[2]","Aux 1[2]","Aux 2[2]"
        ,"Momentum off","Shunt mode","Dynamic brake","Uncouple Cycle","|","Fire box","Dim lights","Grade cross","Smoke gen","Notch up","Notch down","Sound fade","Brk sql off","Doppler effect","Volume & mute","Shift mode"
        ,"Sound slot 1","Sound slot 2","Sound slot 3","Sound slot 4","Sound slot 5","Sound slot 6","Sound slot 7","Sound slot 8","Sound slot 9","Sound slot 10","Sound slot 11","Sound slot 12"
        ,"Sound slot 13","Sound slot 14","Sound slot 15","Sound slot 16","Sound slot 17","Sound slot 18","Sound slot 19","Sound slot 20","Sound slot 21","Sound slot 22","Sound slot 23","Sound slot 24"
    };

    final int maxOut = outDescESU.length;
    final String[] outLabel = new String[maxOut];
    final String[][] outName = new String[maxOut][3];
    final boolean[] outIsUsed = new boolean[maxOut];
    final int iVarIndex[][] =new int[maxOut][numRows];
                                           
    public FnMapPanelESU(VariableTableModel v, List<Integer> varsUsed, Element model, RosterEntry rosterEntry, CvTableModel cvModel) {
        if (log.isDebugEnabled()) log.debug("ESU Function map starts");
        _varModel = v;
        
        // set up default names and labels
        for (int iOut=0; iOut<maxOut; iOut++) {
            outLabel[iOut] = "";
            outName[iOut][0] = "";
            outName[iOut][1] = "";
            outName[iOut][2] = "";
            outIsUsed[iOut] = false;
            for (int iRow = 0; iRow < numRows; iRow++) {
                iVarIndex[iOut][iRow] = 0;
                for (int outBlockNum = 0; outBlockNum < outBlockLength.length; outBlockNum++) {
                    summaryLine[iRow][outBlockNum] = new JTextField(20);
                    summaryLine[iRow][outBlockNum].setHorizontalAlignment(JTextField.LEFT);
                    summaryLine[iRow][outBlockNum].setEditable(false);
                }
            }
        }
        // configure numRows(from numFns), numOuts & any custom labels from decoder file
        configOutputs(model);
        
        // initialize the layout
        gl = new GridBagLayout();
        cs = new GridBagConstraints();
        setLayout(gl);

        labelAt(outputLabelRow,firstCol, "Mapping Row");
        
        cs.anchor = GridBagConstraints.LINE_START;
                
        // loop through rows
        for (int iRow = 0; iRow < numRows; iRow++) {
            currentCol = firstCol;
//             currentRow = firstRow+iRow;
            int outBlockNum = -1;
            int nextOutBlockStart = 0;
            int nextFreeBit = 0;
            cs.anchor = GridBagConstraints.LINE_END;
            labelAt( currentRow, currentCol, Integer.toString(iRow+1));
//             labelAt( currentRow + 1, currentCol, Integer.toString(iRow+1));
            currentCol++;
            cs.anchor = GridBagConstraints.LINE_START;

            // loop through outputs (columns)
            int iOut = 0;
            do {
                    JPanel blockPanel = new JPanel();
                    GridBagLayout blockPanelLay = new GridBagLayout();
                    GridBagConstraints blockPanelCs = new GridBagConstraints();

                    JPanel blockItemsSelectorPanel = new JPanel();
                    GridBagLayout bIsPlay = new GridBagLayout();
                    GridBagConstraints bIsPcs = new GridBagConstraints();

                // check for block separators
                if (iOut == nextOutBlockStart) {
                    outBlockNum++;
                    outBlockStartCol[outBlockNum] = iOut;
                    nextOutBlockStart = iOut+outBlockLength[outBlockNum];
                    blockItemsSelectorPanel = new JPanel();
//                     panelList.add(blockItemsSelectorPane);
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
                    if ( !outName[iOut][0].equals("") ) {
                        // set up the variable using the output label
                        String name = "ESU Function Row "+Integer.toString(iRow+1)+" Column "+Integer.toString(iOut+1);
                        int siCV = SI_START_CV + (iRow / SI_CV_MODULUS);
                        int iCV = START_CV + (((SI_CV_MODULUS * iRow) + (nextFreeBit / BIT_MODULUS)) % CV_MODULUS);
                        String thisCV = PI_CV+"."+siCV+"."+iCV;
                        int bitValue = (int)(Math.pow(2,outBlockItemBits[outBlockNum]) - 1) << (nextFreeBit % BIT_MODULUS);
                        String bitMask = "00000000"+Integer.toBinaryString(bitValue);
                        bitMask = (bitMask.substring(bitMask.length() - 8));
                        String bitPattern = bitMask.replace("0","X").replace("1","V");
    //                     if ( iRow == 0 ) log.info("nextFreeBit="+nextFreeBit+",thisCV="+thisCV+",bitPattern="+bitPattern);
                
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
                            doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));

                            // set up choices
                            String defChoice1 = "On";
                            String defChoice2 = "Off";
                            if ( !outName[iOut][1].equals("")  ) defChoice1 = outName[iOut][1];
                            if ( !outName[iOut][2].equals("")  ) defChoice2 = outName[iOut][2];

                            // add some elements
                            Element thisVar;
                            if (outBlockItemBits[outBlockNum] == 2) {
                                root.addContent(new Element("decoder")  // the sites information here lists all relevant
                                    .addContent(new Element("variables")
                                        .addContent(thisVar = new Element("variable")
                                            .setAttribute("CV",thisCV)
                                            .setAttribute("default",defaultValue)
                                            .setAttribute("mask",bitPattern)
                                            .setAttribute("item", name)
                                            .setAttribute("readOnly","no")
                                                .addContent(new Element("enumVal")
                                                    .addContent(new Element("enumChoice")
                                                        .setAttribute("choice","-")
                                                    )
                                                    .addContent(new Element("enumChoice")
                                                        .setAttribute("choice",defChoice1)
                                                    )
                                                    .addContent(new Element("enumChoice")
                                                        .setAttribute("choice",defChoice2)
                                                    )
                                                )
                                            )
                                        ) // variables element
                                    ); // decoder element
                            } else {
                                root.addContent(new Element("decoder")  // the sites information here lists all relevant
                                    .addContent(new Element("variables")
                                        .addContent(thisVar = new Element("variable")
                                            .setAttribute("CV",thisCV)
                                            .setAttribute("default",defaultValue)
                                            .setAttribute("mask",bitPattern)
                                            .setAttribute("item", name)
                                            .setAttribute("readOnly","no")
                                                .addContent(new Element("enumVal")
                                                    .addContent(new Element("enumChoice")
                                                        .setAttribute("choice","Off")
                                                    )
                                                    .addContent(new Element("enumChoice")
                                                        .setAttribute("choice","On")
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
                        if (iVar>=0) {
                            // try to find item  labels for outLabel[iOut]
                            if ( outName[iOut][0].startsWith("Sound slot") ) {
                                try {
                                    outLabel[iOut] = rosterEntry.getSoundLabel(Integer.valueOf(outName[iOut][0].substring( ("Sound slot"+" ").length())));
                                } catch (Exception e) {}
                            } else if ( outName[iOut][0].startsWith("F") ) {
                                try {
                                    outLabel[iOut] = rosterEntry.getFunctionLabel(Integer.valueOf(outName[iOut][0].substring(1))) ;
                                } catch (Exception e) {}
                            }
                            if (outLabel[iOut] == null) outLabel[iOut]= "";
                            
                            // generate a fullOutName
                            String fullOutName = outName[iOut][0];
                            if ( outLabel[iOut] != "") fullOutName = fullOutName + (": " + outLabel[iOut]);
                            
                            if (log.isDebugEnabled()) log.debug("Process var: "+name+" as index "+iVar);
                            varsUsed.add(Integer.valueOf(iVar));
                            JComponent varComp;
                            if (outBlockItemBits[outBlockNum] == 1) {
                                varComp = (JComponent)(_varModel.getRep(iVar, "checkbox"));
                            } else {
                                varComp = (JComponent)(_varModel.getRep(iVar, ""));
                            }
                            VariableValue var = _varModel.getVariable(iVar);
                            varComp.setToolTipText(PaneProgPane.addCvDescription(("Row "+Integer.toString(iRow+1)+", "+fullOutName), var.getCvDescription(), var.getMask()));
                            cvObject.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                                    public void propertyChange(java.beans.PropertyChangeEvent e) {
                                        String propertyName = e.getPropertyName();
//                                         String newValue = e.toString();
//                                         log.info("propertyName="+propertyName+",String="+newValue);
//                                         log.info("propertyName="+propertyName);
                                        updateAllSummaryLines();                                        
                                    }
                                });

                            // add line to scroll pane
                            String label = outName[iOut][0];
                            if ( outBlockItemBits[outBlockNum] == 1 ) label = fullOutName;
                            bIsPcs.anchor = GridBagConstraints.LINE_START;
                            bIsPcs.gridx = outBlockItemBits[outBlockNum] % 2;
                            blockItemsSelectorPanel.add(new JLabel(label),bIsPcs);
                            bIsPcs.gridx = outBlockItemBits[outBlockNum] - 1;
                            blockItemsSelectorPanel.add(varComp,bIsPcs);
                            bIsPcs.gridy++;

                            outIsUsed[iOut] = true;
                            iVarIndex[iOut][iRow] = iVar;
//                             log.info("iVarIndex[iOut][iRow]="+iVarIndex[iOut][iRow]);
                        } else {
                            if (log.isDebugEnabled()) log.debug("Did not find var: "+name);
                        }
                    }
                    nextFreeBit = nextFreeBit + outBlockItemBits[outBlockNum];

                    iOut++;
                } while ( (iOut < nextOutBlockStart) && (iOut < numOut) ); // end block loop

                // display block
                JScrollPane blockItemsScrollPane = new JScrollPane(blockItemsSelectorPanel);
                blockItemsScrollPane.setPreferredSize(new Dimension(400, 400));

//                 log.info("iRow="+iRow+",outBlockNum="+outBlockNum);
//                 saveAt(currentRow, currentCol, summaryLine[iRow][outBlockNum]);
                blockPanelCs.anchor = GridBagConstraints.LINE_START;
                blockPanelCs.gridx = 0;
                blockPanelCs.gridy = 0;
                blockPanelCs.insets = new Insets(0,20,0,0);
                blockPanel.add(summaryLine[iRow][outBlockNum],blockPanelCs);
                updateSummaryLine(iRow, outBlockNum);
                

//                 saveAt(currentRow + 1, currentCol, blockItemsScrollPane);
//                 blockPanelCs.anchor = GridBagConstraints.FIRST_LINE_START;
//                 blockPanelCs.gridx = 0;
//                 blockPanelCs.gridy = 1;
//                 blockPanelCs.gridwidth = GridBagConstraints.REMAINDER;
//                 blockPanel.add(blockItemsScrollPane,blockPanelCs);

//                 blockItemsScrollPane.setVisible(false);

                JButton button = new JButton("Change");
                            button.setActionCommand(iRow+","+outBlockNum);
                            button.addActionListener(new java.awt.event.ActionListener() {
                                    public void actionPerformed(java.awt.event.ActionEvent e) {
                                        String propertyName = e.getActionCommand();
                                        String params[] = e.getActionCommand().split(",");
//                                         blockItemsScrollPane.setVisible(!blockItemsScrollPane.isVisible());
//                                         log.info("action="+propertyName);
//                                         JOptionPane.showMessageDialog(blockPanel,"OK, all variables in file are known");
//                                         JOptionPane.showMessageDialog(blockPanel,blockItemsScrollPane,"Information",JOptionPane.INFORMATION_MESSAGE);
                                        JOptionPane.showMessageDialog(
                                            blockPanel,blockItemsScrollPane,"Row "+params[0]+", "+
                                            outBlockName[Integer.valueOf(params[1])],JOptionPane.PLAIN_MESSAGE);
//                                         log.info("propertyName="+propertyName);
                                    }
                                });
//                 saveAt(currentRow, currentCol, button);
                blockPanelCs.anchor = GridBagConstraints.LINE_START;
                blockPanelCs.gridx = 1;
                blockPanelCs.gridy = 0;
                blockPanelCs.insets = new Insets(0,0,0,0);
                blockPanel.add(button,blockPanelCs);

                saveAt(currentRow, currentCol, blockPanel);
                currentCol++;



            } while  (iOut < Math.min(numOut,maxOut) ); // end outputs (columns) loop
            
            labelAt( currentRow++, currentCol, Integer.toString(iRow+1));
//             labelAt( currentRow, currentCol, Integer.toString(iRow+1));
            currentRow++;
        }  // end row loop

        // tally used columns
        int currentBlock = -1;
        int blockStart = 0;
        for (int iOut=0; iOut<maxOut; iOut++) {
            if (iOut == blockStart) {
                currentBlock++;
                blockStart = blockStart + outBlockLength[currentBlock];
                outBlockUsed[currentBlock] = 0;
            }
            if (outIsUsed[iOut]) outBlockUsed[currentBlock]++;
        }

        for (int iBlock=0; iBlock<outBlockLength.length; iBlock++) {
            if (outBlockUsed[iBlock] > 0) {
                JLabel lx = new JLabel("<html><strong>&nbsp;"+outBlockName[iBlock]+"&nbsp;</strong></html>");
                GridBagConstraints csx = new GridBagConstraints();
                csx.gridy = blockNameRow;
                csx.gridx = firstOut + iBlock;
                csx.insets = new Insets(0,40,0,0);
                csx.gridwidth = 1;
                csx.anchor = GridBagConstraints.LINE_START;
                gl.setConstraints(lx, csx);
                add(lx);
            }
        }

        // warn if using OS X GUI
//         if (SystemType.isMacOSX() && UIManager.getLookAndFeel().isNativeLookAndFeel()) {
//             String warnMsg = "<html><center>";
//             String osVersion = System.getProperty("os.version");
//             String javaVersion = System.getProperty("java.version");
//             if ( javaVersion.startsWith("1.6.") ) {
//                 warnMsg = warnMsg + "<span style=\"font-size:1.5em;color:red\"><strong>WARNING: The version of Java installed on your system (" + javaVersion +") has a bug<br/>" +
//                      "that may cause JMRI to generate an error when using with this definition.<br/>" +
//                      "</strong></span>";
//                  if ( osVersion.startsWith("10.5.") || osVersion.startsWith("10.6.") ) {
//                     warnMsg = warnMsg + "<span style=\"font-size:1.2em\"><strong>To avoid this problem, change GUI as per the suggestion below.<br/>" +
//                          "</strong></span><br/>";
//                      } else {
//                     warnMsg = warnMsg + "<span style=\"font-size:1.2em\"><strong>To avoid this problem, change GUI as per the suggestion below<br/>" +
//                          "or update to Oracle Java from http://www.java.com/<br/>" +
//                          "</strong></span><br/>";
//                      }
//             }
//             warnMsg = warnMsg + "<strong>You are using the Mac OS X native GUI<br/>" +
//                 "and may experience slow scrolling.</strong><br/>" +
//                 "If affected, change GUI in "+Application.getApplicationName() +
//                 "-&gt;Preferences-&gt;Display-&gt;GUI.<br/><br/>" +
//                 "</center></html>";
//             JLabel l = new JLabel(warnMsg);
//             cs.gridy = guiWarningRow;
//             cs.gridx = 0;
//             cs.gridwidth = GridBagConstraints.REMAINDER;
//             cs.anchor = GridBagConstraints.CENTER;
//             gl.setConstraints(l, cs);
//             add(l);
//         }
        if (log.isDebugEnabled()) log.debug("Function map complete");
    }
    
    void updateAllSummaryLines() {
        for (int row = 0; row < numRows; row++) {
            for (int block = 0; block < outBlockLength.length; block++) {
//                 log.info("row="+row+",block="+block);
                updateSummaryLine(row, block);
            }
        }
        return;
    }

    void updateSummaryLine(int row, int block) {
        String retString = "";
        int retState = 0;

        for (int iOut = outBlockStartCol[block]; iOut < (outBlockStartCol[block] + outBlockLength[block]); iOut++) {
            if (outIsUsed[iOut]) {
                int value =  Integer.valueOf(_varModel.getValString(iVarIndex[iOut][row]));
                int state = _varModel.getState(iVarIndex[iOut][row]);
                if ( iOut == outBlockStartCol[block]) {
                    retState = state;
                } else if ( retState != state ){
                    retState = AbstractValue.EDITED;
                }
//                 if ( state != AbstractValue.FROMFILE ) log.info("default="+_varModel.getVariable(iVarIndex[iOut][row]));
                if (value > 0) {
                    if (outBlockItemBits[block] == 1) {
                        if ( outLabel[iOut].equals("") ) {
                            retString = retString + "," + outName[iOut][0];
                        } else {
                            retString = retString + "," + outLabel[iOut];
                        } 
                    } else if (outBlockItemBits[block] == 2) {
//                         log.info("iOut="+iOut+",value="+value);
                        if ( value > 2 ) {
                            retString = retString + "," + "reserved value "+value;
                        } else if ( outName[iOut][value].equals("") ) {
                            if ( value == 1 ) {
                                retString = retString + "," + outName[iOut][0];
                            } else if ( value == 2 ) {
                                retString = retString + ",not " + outName[iOut][0];
                            }
                        } else {
                            retString = retString + "," + outName[iOut][value];
                        }
                    }
                }
            }
        }

        if (retString.startsWith(",")) retString = retString.substring(1);
        if (retString.equals("")) retString = "-";
//         retString = retString + " state="+retState;

        summaryLine[row][block].setBackground(AbstractValue.stateColorFromValue(retState));
        summaryLine[row][block].setText(retString);
        summaryLine[row][block].setToolTipText(retString);
        return;
    }
    
    void saveAt(int row, int column, JComponent j) {
        if (row<0 || column<0) return;
        cs.gridy = row;
        cs.gridx = column;
        gl.setConstraints(j, cs);
        add(j);
    }
    
    void labelAt(int row, int column, String name) {
        this.labelAt(row, column, name, null);
    }

    void labelAt(int row, int column, String name, String toolTip) {
        if (row<0 || column<0) return;
        JLabel t = new JLabel(" "+name+" ");
        if ( toolTip != null ) t.setToolTipText(toolTip);
        saveAt(row, column, t);
    }
    
    /**
     * Use the "model" element from the decoder definition file
     * to configure the number of rows and columns and set up any
     * custom column names.
     */
    protected void configOutputs(Element model) {
        if (model==null) {
            log.debug("configOutputs was given a null model");
            return;
        }
        // get numOuts, numFns or leave the defaults
        Attribute a = model.getAttribute("numOuts");
        try { if (a!=null) numOut = Integer.valueOf(a.getValue()).intValue();}
        catch (Exception e) {log.error("error handling decoder's numOuts value");}
        a = model.getAttribute("numFns");
        try { if (a!=null) numRows = Integer.valueOf(a.getValue()).intValue();}
        catch (Exception e) {log.error("error handling decoder's numFns value");}
        if (log.isDebugEnabled()) log.debug("numFns, numOuts "+numRows+","+numOut);
        
        // add ESU default split labels before reading custom ones
        for (int iOut=0; iOut<maxOut; iOut++) {
            loadSplitLabel(iOut, outDescESU[iOut]);
        } 

        // take all "output" children
        List<Element> elemList = model.getChildren("output");
        if (log.isDebugEnabled()) log.debug("output scan starting with "+elemList.size()+" elements");
        for (int i=0; i<elemList.size(); i++) {
            Element e = elemList.get(i);
            String name = e.getAttribute("name").getValue();
            // if this a number, or a character name?
            try {
                int outputNum = Integer.valueOf(name).intValue();
                // yes, since it was converted.  All we do with
                // these are store the label index (if it exists)
                String at = LocaleSelector.getAttribute(e, "label");
                if ( at!=null ) {
                    loadSplitLabel(outputNum-1, at);
                }
            } catch (java.lang.NumberFormatException ex) {
                // not a number, must be a name
                if (i<maxOut) {
                    outName[i][0] = name;
                    outName[i][1] = "";
                    outName[i][2] = "";
                    String at;
                    if ((at=LocaleSelector.getAttribute(e, "label"))!=null)
                        outName[i][0] = at;
                    else
                        outName[i][0] ="";
                }
            }
        }
    }
    
    // split and load labels
    void loadSplitLabel(int iOut, String theLabel) {
        if (iOut < maxOut) {
            String itemList[] = theLabel.split("\\|");
            if ( theLabel.equals("|") ) {
                outName[iOut][0] = "";
                outName[iOut][1] = "";
                outName[iOut][2] = "";
            } else if (itemList.length == 1) {
                outName[iOut][0] = itemList[0];
                outName[iOut][1] = "";
            } else if (itemList.length == 2) {
                outName[iOut][0] = itemList[0];
                outName[iOut][1] = itemList[1];
                outName[iOut][2] = "";
            } else if (itemList.length > 2) {
                outName[iOut][0] = itemList[0];
                outName[iOut][1] = itemList[1];
                outName[iOut][2] = itemList[2];
            }
        }
    }
    
    /** clean up at end */
    public void dispose() {
        removeAll();
    }
    
    // initialize logging
    static Logger log = LoggerFactory.getLogger(FnMapPanelESU.class.getName());
}
