// FnMapPanelESU.java

package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import javax.swing.UIManager;
import java.awt.*;

import java.util.List;
import org.jdom.Element;
import org.jdom.Attribute;
import jmri.util.jdom.LocaleSelector;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgPane;
import jmri.util.SystemType;
import jmri.Application;
import java.util.Arrays;
import jmri.jmrit.roster.RosterEntry;
import org.jdom.*;

/**
 * <p>Provide a graphical representation of the ESU mapping table.
 * Each row represents a possible mapping between input conditions (cab functions, etc.)
 * and logical, physical or sound outputs.</p>
 * <p>Uses the "model" element from the decoder definition file
 * to configure the number of rows and columns and set up any
 * custom column names.</p>
 *  <dl>
 *   <dt>numOuts</dt>
 *     <dd>Number of columns</dd>
 *   <dt>numFns</dt>
 *     <dd>Number of mapping rows</dd>
 *   <dt>output name="ddd" label="xxx|yyy"</dt>
 *     <dd>Replace default name of column "ddd" with "xxx yyy"</dd>
 *   <dt>output name="text1" label="text2"</dt>
 *     <dd>Replace default name of the nth column with "text1 text2"
 *     (assuming this line is the nth "output" element of the "model" element from the decoder definition file).</dd>
 * </dl>
 *  <dl>
 *  <dt>Default column labels are coded in String array outDescESU[] of this class.</dt>
 *   <dd>Column labels can be overridden by the "output" element of the "model" element from the decoder definition file.</dd>
 *  <dt>Two rows are available for column labels.</dt>
 *   <dd>Use the '/' character to designate a row break.</dd>
 * </dl>
 *  <dl>
 *  <dt>Variable definitions for the decoder file are of the form:</dt>
 *   <dd>"ESU Function Row m Column n"</dd>
 * </dl>
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @author			Dave Heap   Copyright (C) 2014
 * @version			$Revision: 24716 $
 */
public class FnMapPanelESU extends JPanel {
    // columns
    int firstCol = 0;
    int firstOut = 1;
    int currentCol = firstCol;
    
    // rows
    int guiWarningRow = 0;
    int blockNameRow = 1;
    int outputNumRow = blockNameRow+1;
    int outputLabelRow = outputNumRow+1;
    int firstRow = outputLabelRow+1;
    int currentRow = firstRow;
    
    // these will eventually be passed in from the ctor
    int numRows = 14;
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
    final String[] outBlockName = new String[] {"Input Conditions (AND operation) Block ","Physical Outputs","Logical Outputs","Sound Slots"};
    
    /**
     * Number of items per block
     */
    final int[] outBlockLength = new int[] {72,16,16,24};

    final int[] outBlockStartCol = new int[outBlockLength.length]; // Starting column column of block
    final int[] outBlockUsed = new int[outBlockLength.length]; // Number of used items per block
    
    /**
     * <p>Default column labels.
     * <dl>
     *  <dt>Two rows are available for column labels</dt>
     *   <dd>Use the '/' character to designate a row break</dd>
     * </dl></p>
     * <p>Column labels can be overridden by the "output" element of the "model" element from the decoder definition file.</p>
     */
    final String[] outDescESU = new String[] {"Loco|drive","Loco|stop","Dir|fwd","Dir|rev"
        ,"F0|on","F0|off","F1|on","F1|off","F2|on","F2|off","F3|on","F3|off","F4|on","F4|off","F5|on","F5|off","F6|on","F6|off","F7|on","F7|off","F8|on","F8|off","F9|on","F9|off"
        ,"F10|on","F10|off","F11|on","F11|off","F12|on","F12|off","F13|on","F13|off","F14|on","F14|off","F15|on","F15|off","F16|on","F16|off","F17|on","F17|off","F18|on","F18|off","F19|on","F19|off"
        ,"F20|on","F20|off","F21|on","F21|off","F22|on","F22|off","F23|on","F23|off","F24|on","F24|off","F25|on","F25|off","F26|on","F26|off","F27|on","F27|off","F28|on","F28|off"
        ,"sensW|on","sensW|off","Sens1|on","Sens1|off","Sens2|on","Sens2|off","Sens3|on","Sens3|off","Sens4|on","Sens4|off"
        ,"Head|light[1]","Rear|light[1]","Aux|1[1]","Aux|2[1]","Aux|3","Aux|4","Aux|5","Aux|6","Aux|7","Aux|8","Aux|9","Aux|10","Head|light[2]","Rear|light[2]","Aux|1[2]","Aux|2[2]"
        ,"Momentum|off","Shunt|mode","Dynamic|brake","Uncouple|Cycle","","Fire|box","Dim|lights","Grade|cross","Smoke|gen","Notch|up","Notch|down","Sound|fade","Brk sql|off","Doppler|effect","Volume|& mute","Shift|mode"
        ,"SS|1","SS|2","SS|3","SS|4","SS|5","SS|6","SS|7","SS|8","SS|9","SS|10","SS|11","SS|12","SS|13","SS|14","SS|15","SS|16","SS|17","SS|18","SS|19","SS|20","SS|21","SS|22","SS|23","SS|24"
    };

    final int maxOut = outDescESU.length;
    final String[] outName = new String[maxOut];
    final String[] outLabel = new String[maxOut];
    final boolean[] outIsUsed = new boolean[maxOut];
                                           
    public FnMapPanelESU(VariableTableModel v, List<Integer> varsUsed, Element model, RosterEntry rosterEntry, CvTableModel cvModel) {
        if (log.isDebugEnabled()) log.debug("ESU Function map starts");
        _varModel = v;
        
        // set up default names and labels
        for (int iOut=0; iOut<maxOut; iOut++) {
            outName[iOut] = Integer.toString(iOut+1);
            outLabel[iOut] = "";
            outIsUsed[iOut] = false;
        }
        // configure numRows(numFns), numOuts & any custom labels from decoder file
        configOutputs(model);
        
        // initialize the layout
        gl = new GridBagLayout();
        cs = new GridBagConstraints();
        setLayout(gl);
                
        labelAt(outputNumRow,firstCol, "Mapping");
        labelAt(outputLabelRow,firstCol, "Row");
        labelAt(firstRow+numRows,firstCol, "Column");
        
        // loop through rows
        for (int iRow = 0; iRow < numRows; iRow++) {
            currentCol = firstCol;
            currentRow = firstRow+iRow;
            int outBlockNum = -1;
            int outBlockStart = 0;
            labelAt( currentRow, currentCol++, Integer.toString(iRow+1));

            // loop through outputs (columns)
            for (int iOut = 0; iOut < numOut; iOut++) {
                // check for block separators
                if (iOut == outBlockStart) {
                    if (iOut != 0) {
                        if (iRow == 0) {
                            labelAt( outputNumRow,   currentCol, " | ");
                            labelAt( outputLabelRow, currentCol, " | ");
                        }
                        labelAt( currentRow, currentCol++, " | "); // Integer.toString(iRow+1)
                    }
                    outBlockNum++;
                    outBlockStartCol[outBlockNum] = currentCol;
                    outBlockStart = iOut+outBlockLength[outBlockNum];
                }

                // check if column is used
                if ( !outName[iOut].equals("") || !outLabel[iOut].equals("") ) {
                    // set up the variable using the output label
                    String name = "ESU Function Row "+Integer.toString(iRow+1)+" Column "+Integer.toString(iOut+1);
                    int siCV = SI_START_CV + (iRow / SI_CV_MODULUS);
                    int iCV = START_CV + (((SI_CV_MODULUS * iRow) + (iOut / BIT_MODULUS)) % CV_MODULUS);
                    String thisCV = PI_CV+"."+siCV+"."+iCV;
                    int bitValue = 1 << (iOut % BIT_MODULUS);
                    String bitMask = "00000000"+Integer.toBinaryString(bitValue);
                    bitMask = (bitMask.substring(bitMask.length() - 8));
                    String bitPattern = bitMask.replace("0","X").replace("1","V");
                
                    // Get Cv value from file. We need to do this to get the GUI synchronised with cvModel initially
                    int savedValue = 0;
                    CvValue cvObject = cvModel.allCvMap().get(thisCV);
                    if (cvObject != null) {
                        savedValue = cvObject.getValue();
                    }
                    String defaultValue = null;
                    if ( (savedValue & bitValue) != 0) {
                        defaultValue = "1";
                    } else {
                        defaultValue = "0";
                    }
                
                    {
                        // create a JDOM tree with some elements to add to _varModel
                        Element root = new Element("decoder-config");
                        Document doc = new Document(root);
                        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));

                        // add some elements
                        Element thisVar;
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
                            ) // decoder element
                            ; // end of adding contents

                        _varModel.setRow(0, thisVar);

                        // cleanup
                        root = null;
                        doc = null;
                        thisVar = null;
                    }

                    int iVar = _varModel.findVarIndex(name);  // now pick up the _varModel entry we just created
                    
                    String fullColumnName = null;
                    // hopefully we found it!
                    if (iVar>=0) {
                        if ( outName[iOut].equals("SS") ) {
                            try {
                                fullColumnName = rosterEntry.getSoundLabel(Integer.valueOf(outLabel[iOut]));
                            } catch (Exception e) {}
                        } else if ( outName[iOut].startsWith("F") && ( outLabel[iOut].equalsIgnoreCase("on") || outLabel[iOut].equalsIgnoreCase("off") ) ) {
                            try {
                                fullColumnName = rosterEntry.getFunctionLabel(Integer.valueOf(outName[iOut].substring(1))) ;
                            } catch (Exception e) {}
                        }
                        if ( fullColumnName != null ) {
                            fullColumnName = outName[iOut]+" "+outLabel[iOut] + " (" + fullColumnName + ")";
                        } else {
                            fullColumnName = outName[iOut]+" "+outLabel[iOut];
                        }
                        // column labels
                        if (iRow == 0) {
                            labelAt( outputNumRow,   currentCol, outName[iOut], fullColumnName);
                            labelAt( outputLabelRow, currentCol, outLabel[iOut], fullColumnName);
                            labelAt( firstRow+numRows, currentCol, String.valueOf(iOut+1), ("Column "+String.valueOf(iOut+1)+", "+fullColumnName));
                        }
                        if (log.isDebugEnabled()) log.debug("Process var: "+name+" as index "+iVar);
                        varsUsed.add(Integer.valueOf(iVar));
                        JComponent j = (JComponent)(_varModel.getRep(iVar, "checkbox"));
                        VariableValue var = _varModel.getVariable(iVar);
                        j.setToolTipText(PaneProgPane.addCvDescription(("Row "+Integer.toString(iRow+1)+", "+fullColumnName), var.getCvDescription(), var.getMask()));
                        saveAt(currentRow, currentCol++, j);
                        outIsUsed[iOut] = true;
                    } else {
                        if (log.isDebugEnabled()) log.debug("Did not find var: "+name);
                    }
                }
            }  // end outputs (columns) loop
            
            labelAt( currentRow, currentCol++, Integer.toString(iRow+1));
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
                JLabel lx = new JLabel("<html><center><strong>"+outBlockName[iBlock]+"</strong></center></html>");
                GridBagConstraints csx = new GridBagConstraints();
                csx.gridy = blockNameRow;
                csx.gridx = outBlockStartCol[iBlock];
                csx.gridwidth = outBlockUsed[iBlock];
                gl.setConstraints(lx, csx);
                add(lx);
            }
        }

        // warn if using OS X GUI
        if (SystemType.isMacOSX() && UIManager.getLookAndFeel().isNativeLookAndFeel()) {
            int numWarnings = numOut/GUIwarningInterval;
            for (int i=0; i<numWarnings; i++) {
                JLabel l = new JLabel("<html><center><strong>You are using the Mac OS X native GUI<br/>"+
                    "and may experience slow scrolling</strong><br/>"+
                    "If affected, change GUI in "+Application.getApplicationName()+
                    "-&gt;Preferences-&gt;Display-&gt;GUI<br/><br/></center></html>");
                cs.gridy = guiWarningRow;
                cs.gridx = i*numOut/numWarnings;
                cs.gridwidth = GUIwarningInterval;
                gl.setConstraints(l, cs);
                add(l);
            }
        }
        if (log.isDebugEnabled()) log.debug("Function map complete");
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
    @SuppressWarnings("unchecked")
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
        
        // add ESU default labels before reading custom ones
        for (int iOut=0; iOut<maxOut; iOut++) {
            loadDescESU(iOut, outDescESU[iOut]);
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
                if ( at!=null && outputNum<=numOut) {
                    loadDescESU(outputNum-1, at);
                    }
                }
            catch (java.lang.NumberFormatException ex) {
                // not a number, must be a name
                if (i<maxOut) {
                    outName[i] = name;
                    String at;
                    if ((at=LocaleSelector.getAttribute(e, "label"))!=null)
                        outLabel[i] = at;
                    else
                        outLabel[i] ="";
                }
            }
        }
    }
    
    // split and load ESU default labels
    void loadDescESU(int iOut, String theLabel) {
        if (iOut < outDescESU.length) {
            String itemESU[] = theLabel.split("\\|");
            if ( theLabel.equals("") || theLabel.equals("|") ) {
                outName[iOut] = "";
                outLabel[iOut] = "";
            } else if (itemESU.length == 1) {
                outLabel[iOut] = itemESU[0];
            } else if (itemESU.length > 1) {
                outName[iOut] = itemESU[0];
                outLabel[iOut] = itemESU[1];
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
