// VariableTableModel.java

package jmri.jmrit.symbolicprog;

import com.sun.java.util.collections.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Table data model for display of variables in symbolic programmer.
 * Also responsible for loading from the XML file...
 *
 * @author    Bob Jacobsen   Copyright (C) 2001
 * @author    Howard G. Penny   Copyright (C) 2005
 * @version   $Revision: 1.25 $
 */
public class VariableTableModel extends AbstractTableModel implements ActionListener, PropertyChangeListener {

    private String headers[] = null;

    private Vector rowVector = new Vector();  // vector of Variable items
    private CvTableModel _cvModel = null;          // reference to external table model
    private IndexedCvTableModel _indxCvModel = null;
    private Vector _writeButtons = new Vector();
    private Vector _readButtons = new Vector();
    private JLabel _status = null;

    /** Defines the columns; values understood are:
     *  "Name", "Value", "Range", "Read", "Write", "Comment", "CV", "Mask", "State"
     */
    public VariableTableModel(JLabel status, String h[], CvTableModel cvModel, IndexedCvTableModel iCvModel) {
        super();
        _status = status;
        _cvModel = cvModel;
        _indxCvModel = iCvModel;
        headers = h;
    }

    // basic methods for AbstractTableModel implementation
    public int getRowCount() {
        return rowVector.size();
    }

    public int getColumnCount( ){ return headers.length;}

    public String getColumnName(int col) {
        if (log.isDebugEnabled()) log.debug("getColumnName "+col);
        return headers[col];
    }

    public Class getColumnClass(int col) {
        // if (log.isDebugEnabled()) log.debug("getColumnClass "+col);
        if (headers[col].equals("Value"))
            return JTextField.class;
        else if (headers[col].equals("Read"))
            return JButton.class;
        else if (headers[col].equals("Write"))
            return JButton.class;
        else
            return String.class;
    }

    public boolean isCellEditable(int row, int col) {
        if (log.isDebugEnabled()) log.debug("isCellEditable "+col);
        if (headers[col].equals("Value"))
            return true;
        else if (headers[col].equals("Read"))
            return true;
        else if (headers[col].equals("Write")
                 && !((VariableValue)(rowVector.elementAt(row))).getReadOnly())
            return true;
        else
            return false;
    }

    public VariableValue getVariable(int row) {
        return ((VariableValue)rowVector.elementAt(row));
    }

    public String getLabel(int row) {
        return ((VariableValue)rowVector.elementAt(row)).label();
    }

    public String getItem(int row) {
        return ((VariableValue)rowVector.elementAt(row)).item();
    }

    public String getCvName(int row) {
        return ((VariableValue)rowVector.elementAt(row)).cvName();
    }

    public String getValString(int row) {
        return ((VariableValue)rowVector.elementAt(row)).getValueString();
    }

    public void setIntValue(int row, int val) {
        ((VariableValue)rowVector.elementAt(row)).setIntValue(val);
    }

    public void setState(int row, int val) {
        if (log.isDebugEnabled()) log.debug("setState row: "+row+" val: "+val);
        ((VariableValue)rowVector.elementAt(row)).setState(val);
    }

    public int getState(int row) {
        return ((VariableValue)rowVector.elementAt(row)).getState();
    }

    /*
     * Request a "unique representation", e.g. something we can show
     * for the row-th variable.
     */
    public Object getRep(int row, String format) {
        VariableValue v = (VariableValue)rowVector.elementAt(row);
        return v.getRep(format);
    }

    public Object getValueAt(int row, int col) {
        // if (log.isDebugEnabled()) log.debug("getValueAt "+row+" "+col);
        VariableValue v = (VariableValue)rowVector.elementAt(row);
        if (headers[col].equals("Value"))
            return v.getValue();
        else if (headers[col].equals("Read"))
            return _readButtons.elementAt(row);
        else if (headers[col].equals("Write"))
            return _writeButtons.elementAt(row);
        else if (headers[col].equals("CV"))
            return ""+v.getCvNum();
        else if (headers[col].equals("Name"))
            return ""+v.label();
        else if (headers[col].equals("Comment"))
            return v.getComment();
        else if (headers[col].equals("Mask"))
            return v.getMask();
        else if (headers[col].equals("State")) {
            int state = v.getState();
            switch (state) {
            case CvValue.UNKNOWN:  return "Unknown";
            case CvValue.READ:     return "Read";
            case CvValue.EDITED:   return "Edited";
            case CvValue.STORED:   return "Stored";
            case CvValue.FROMFILE: return "From file";
            default: return "inconsistent";
            }
        }
        else if (headers[col].equals("Range"))
            return v.rangeVal();
        else
            return "Later, dude";
    }

    public void setValueAt(Object value, int row, int col) {
        if (log.isDebugEnabled()) log.debug("setvalueAt "+row+" "+col+" "+value);
        setFileDirty(true);
    }

    // for loading config:
    // Read from an Element to configure the row
    public void setRow(int row, Element e) {
        // get the values for the VariableValue ctor
        String name = e.getAttribute("label").getValue(); 	// Note the name variable is actually the label attribute
        if (log.isDebugEnabled()) log.debug("Starting to setRow \""+name+"\"");
        String item = ( e.getAttribute("item")!=null ?
                        e.getAttribute("item").getValue() :
                        null);
        String comment = null;
        if (e.getAttribute("comment") != null)
            comment = e.getAttribute("comment").getValue();
        int CV = Integer.valueOf(e.getAttribute("CV").getValue()).intValue();
        String mask = null;
        if (e.getAttribute("mask") != null)
            mask = e.getAttribute("mask").getValue();
        else {
            log.warn("Element missing mask attribute: "+name);
            mask ="VVVVVVVV";
        }

        int minVal = 0;
        int maxVal = 255;

        boolean readOnly = false;
        if (e.getAttribute("readOnly") != null) {
            readOnly = e.getAttribute("readOnly").getValue().equals("yes") ? true : false;
            if (log.isDebugEnabled()) log.debug("found readOnly "+e.getAttribute("readOnly").getValue());
        } else {
            log.warn("Element missing readOnly attribute: "+name);
        }

        boolean infoOnly = false;
        if (e.getAttribute("infoOnly") != null) {
            infoOnly = e.getAttribute("infoOnly").getValue().equals("yes") ? true : false;
            if (log.isDebugEnabled()) log.debug("found readOnly "+e.getAttribute("infoOnly").getValue());
        } else {
            log.warn("Element missing readOnly attribute: "+name);
        }

        boolean writeOnly = false;
        if (e.getAttribute("writeOnly") != null) {
            writeOnly = e.getAttribute("writeOnly").getValue().equals("yes") ? true : false;
            if (log.isDebugEnabled()) log.debug("found writeOnly "+e.getAttribute("writeOnly").getValue());
        } else {
            log.warn("Element missing readOnly attribute: "+name);
        }

        boolean opsOnly = false;
        if (e.getAttribute("opsOnly") != null) {
            opsOnly = e.getAttribute("opsOnly").getValue().equals("yes") ? true : false;
            if (log.isDebugEnabled()) log.debug("found opsOnly "+e.getAttribute("opsOnly").getValue());
        } else {
            log.warn("Element missing readOnly attribute: "+name);
        }

        JButton bw = new JButton("Write");
        _writeButtons.addElement(bw);
        JButton br = new JButton("Read");
        _readButtons.addElement(br);

        if (readOnly || infoOnly) { // readOnly or infoOnly, config write, read buttons
            if (writeOnly){
                bw.setEnabled(true);
                bw.setActionCommand("W" + row);
                bw.addActionListener(this);
            } else {
                bw.setEnabled(false);
            }
            if (infoOnly) {
                br.setEnabled(false);
            } else {
                br.setActionCommand("R"+row);
                br.addActionListener(this);
            }
        } else { // not readOnly or infoOnly, config write, read buttons
            bw.setActionCommand("W" + row);
            bw.addActionListener(this);
            if (writeOnly) {
                br.setEnabled(false);
            } else {
                br.setActionCommand("R" + row);
                br.addActionListener(this);
            }
        }

        if (_cvModel == null) {
            log.error("CvModel reference is null; cannot add variables");
            return;
        }
        _cvModel.addCV(""+CV, readOnly, infoOnly, writeOnly);

        // have to handle various value types, see "snippet"
        Attribute a;
        Element child;
        VariableValue v = null;
        if ( (child = e.getChild("decVal")) != null) {
            if ( (a = child.getAttribute("min")) != null)
                minVal = Integer.valueOf(a.getValue()).intValue();
            if ( (a = child.getAttribute("max")) != null)
                maxVal = Integer.valueOf(a.getValue()).intValue();
            v = new DecVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                                     CV, mask, minVal, maxVal, _cvModel.allCvVector(), _status, item);

        } else if ( (child = e.getChild("hexVal")) != null) {
            if ( (a = child.getAttribute("min")) != null)
                minVal = Integer.valueOf(a.getValue(),16).intValue();
            if ( (a = child.getAttribute("max")) != null)
                maxVal = Integer.valueOf(a.getValue(),16).intValue();
            v = new HexVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                                     CV, mask, minVal, maxVal, _cvModel.allCvVector(), _status, item);

        } else if ( (child = e.getChild("enumVal")) != null) {
            List l = child.getChildren("enumChoice");
            EnumVariableValue v1 = new EnumVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                                                         CV, mask, 0, l.size()-1, _cvModel.allCvVector(), _status, item);
            v = v1;
            v1.nItems(l.size());
            for (int k=0; k< l.size(); k++) {
                // is a value specified?
                Element enumChElement = (Element)l.get(k);
                Attribute valAttr = enumChElement.getAttribute("value");
                if ( valAttr==null)
                    v1.addItem(enumChElement.getAttribute("choice").getValue());
                else {
                    v1.addItem(enumChElement.getAttribute("choice").getValue(),
                                Integer.parseInt(valAttr.getValue()));
                }
            }
            v1.lastItem();

        } else if ( (child = e.getChild("speedTableVal")) != null) {
            if ( (a = child.getAttribute("min")) != null)
                minVal = Integer.valueOf(a.getValue()).intValue();
            if ( (a = child.getAttribute("max")) != null)
                maxVal = Integer.valueOf(a.getValue()).intValue();
            Attribute entriesAttr = child.getAttribute("entries");
            int entries = 28;
            try {
                if (entriesAttr!=null) entries = entriesAttr.getIntValue();
            } catch (org.jdom.DataConversionException e1) {}

            // ensure all CVs exist
            for (int i=0; i<entries; i++) { _cvModel.addCV(""+(CV+i), readOnly, infoOnly, writeOnly); }

            v = new SpeedTableVarValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                                       CV, mask, minVal, maxVal, _cvModel.allCvVector(), _status, item, entries);

        } else if ( (child = e.getChild("longAddressVal")) != null) {
            _cvModel.addCV(""+(CV+1), readOnly, infoOnly, writeOnly);  // ensure 2nd CV exists
            v = new LongAddrVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                                          CV, mask, minVal, maxVal, _cvModel.allCvVector(), _status, item);
        } else if ( (child = e.getChild("shortAddressVal")) != null) {
            ShortAddrVariableValue v1 = new ShortAddrVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                                     CV, mask, _cvModel.allCvVector(), _status, item);
            v = v1;
            // get specifics if any
            List l = child.getChildren("shortAddressChanges");
            for (int k=0; k< l.size(); k++)
                try {
                    v1.setModifiedCV(((Element)l.get(k)).getAttribute("cv").getIntValue());
                }
                catch (org.jdom.DataConversionException e1) {
                    log.error("invalid cv attribute in short address element of decoder file");
                }

        } else if ( (child = e.getChild("splitVal")) != null) {
            if ( (a = child.getAttribute("min")) != null)
                minVal = Integer.valueOf(a.getValue()).intValue();
            if ( (a = child.getAttribute("max")) != null)
                maxVal = Integer.valueOf(a.getValue()).intValue();
            int highCV = CV+1;
            if ( (a = child.getAttribute("highCV")) != null)
                highCV = Integer.valueOf(a.getValue()).intValue();
            int factor = 1;
            if ( (a = child.getAttribute("factor")) != null)
                factor = Integer.valueOf(a.getValue()).intValue();
            int offset = 0;
            if ( (a = child.getAttribute("offset")) != null)
                offset = Integer.valueOf(a.getValue()).intValue();
            String uppermask = "VVVVVVVV";
            if ( (a = child.getAttribute("upperMask")) != null)
                uppermask = a.getValue();

            _cvModel.addCV(""+(highCV), readOnly, infoOnly, writeOnly);  // ensure 2nd CV exists
            v = new SplitVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                                       CV, mask, minVal, maxVal, _cvModel.allCvVector(),
                                       _status, item,
                                       highCV, factor, offset, uppermask);
        } else {
            reportBogus();
            return;
        }

        // back to general processing
        // add tooltip text if present
        if ( (a = e.getAttribute("tooltip")) != null)
            v.setTooltipText(a.getValue());

        // record new variable, update state, hook up listeners
        rowVector.addElement(v);
        v.setState(VariableValue.FROMFILE);
        v.addPropertyChangeListener(this);

        // set to default value if specified (CV load may later override this)
        if ( (a = e.getAttribute("default")) != null) {
            String val = a.getValue();
            if (log.isDebugEnabled()) log.debug("Found default value: "+val+" for "+name);
            v.setIntValue(Integer.valueOf(val).intValue());
            _cvModel.getCvByNumber(CV).setState(VariableValue.FROMFILE);  // correct for transition to "edited"
        }
    }

    private int _piCv = -1;
    public int piCv() {return _piCv;}
    private int _siCv = -1;
    public int siCv() {return _siCv;}

    boolean isIncluded(String include, String productID) {
        int i = 0;
        while (i < include.length()) {
            if (include.startsWith(productID, i)) {
                return true;
            }
            i = include.indexOf(',',i) + 1;
            if (i == 0) {
                i = include.length();
            }
        }
        return false;
    }

    public int setIndxRow(int row, Element e, String productID) {
        if (e.getAttributeValue("include") != null) {
            String include = e.getAttributeValue("include");
            if (isIncluded(include, productID) == false) {
                return row - 1;
            }
        }

        // get the values for the VariableValue ctor
        String name = e.getAttribute("label").getValue(); 	// Note the name variable is actually the label attribute
        if (log.isDebugEnabled()) log.debug("Starting to setIndexedRow \""+name+"\"");
        String cvName = e.getAttributeValue("CVname");
        String item = ( e.getAttribute("item")!=null ?
                        e.getAttribute("item").getValue() :
                        null);
        String comment = null;
        if (e.getAttribute("comment") != null)
            comment = e.getAttribute("comment").getValue();
        int piVal = Integer.valueOf(e.getAttribute("PI").getValue()).intValue();
        int siVal = ( e.getAttribute("SI") != null ?
                      Integer.valueOf(e.getAttribute("SI").getValue()).intValue() :
                      -1);
        int cv = Integer.valueOf(e.getAttribute("CV").getValue()).intValue();
        String mask = null;
        if (e.getAttribute("mask") != null)
            mask = e.getAttribute("mask").getValue();
        else {
            log.warn("Element missing mask attribute: "+name);
            mask ="VVVVVVVV";
        }

        int minVal = 0;
        int maxVal = 255;

        boolean readOnly = e.getAttribute("readOnly").getValue().equals("yes") ? true : false;
        boolean infoOnly = e.getAttribute("infoOnly").getValue().equals("yes") ? true : false;
        boolean writeOnly = e.getAttribute("writeOnly").getValue().equals("yes") ? true : false;
        boolean opsOnly = e.getAttribute("opsOnly").getValue().equals("yes") ? true : false;

        JButton br = new JButton("Read");
        _readButtons.addElement(br);
        JButton bw = new JButton("Write");
        _writeButtons.addElement(bw);

        if (readOnly || infoOnly) { // readOnly or infoOnly, config write, read buttons
            if (writeOnly){
                bw.setEnabled(true);
                bw.setActionCommand("W" + row);
                bw.addActionListener(this);
            } else {
                bw.setEnabled(false);
            }
            if (infoOnly) {
                br.setEnabled(false);
            } else {
                br.setActionCommand("R"+row);
                br.addActionListener(this);
            }
        } else { // not readOnly or infoOnly, config write, read buttons
            bw.setActionCommand("W" + row);
            bw.addActionListener(this);
            if (writeOnly) {
                br.setEnabled(false);
            } else {
                br.setActionCommand("R" + row);
                br.addActionListener(this);
            }
        }

        if (_indxCvModel == null) {
            log.error("IndexedCvModel reference is null; can not add variables");
            return -1;
        }

        int _newRow = _indxCvModel.addIndxCV(row, cvName, _piCv, piVal, _siCv, siVal, cv, readOnly, infoOnly, writeOnly);
        if( _newRow != row) {
            row = _newRow;
        }

        // have to handle various value types, see "snippet"
        Attribute a;
        Element child;
        VariableValue iv = null;
        if ( (child = e.getChild("indexedVal")) != null) {
            if ( (a = child.getAttribute("min")) != null)
                minVal = Integer.valueOf(a.getValue()).intValue();
            if ( (a = child.getAttribute("max")) != null)
                maxVal = Integer.valueOf(a.getValue()).intValue();
            iv = new IndexedVariableValue(row, name, comment, cvName,
                                          readOnly, infoOnly, writeOnly, opsOnly,
                                          cv, mask, minVal, maxVal, _indxCvModel.allIndxCvVector(),
                                          _status, item );

        } else if ( (child = e.getChild("ienumVal")) != null) {
            List l = child.getChildren("ienumChoice");
            IndexedEnumVariableValue v1 = new IndexedEnumVariableValue(row, name, comment, cvName,
                readOnly, infoOnly, writeOnly, opsOnly,
                cv, mask, _indxCvModel.allIndxCvVector(),
                _status, item);
            iv = v1;
            for (int x = 0; x < l.size(); x++) {
                Element ex = (Element)l.get(x);
                String include = ex.getAttributeValue("include");
                if (include != null && !isIncluded(include, productID)) {
                    l.remove(x);
                    x--;
                }
            }
            v1.nItems(l.size());
            for (int k=0; k< l.size(); k++) {
                Element enumChElement = (Element)l.get(k);
                // is a value specified?
                Attribute valAttr = enumChElement.getAttribute("value");
                if (valAttr == null)
                    v1.addItem(enumChElement.getAttribute("choice").
                               getValue());
                else {
                    v1.addItem(enumChElement.getAttribute("choice").
                               getValue(),
                               Integer.parseInt(valAttr.getValue()));
                }
            }
            v1.lastItem();

        } else if (( child = e.getChild("indexedPairVal")) != null) {
            if ((a = child.getAttribute("min")) != null)
                minVal = Integer.valueOf(a.getValue()).intValue();
            if ((a = child.getAttribute("max")) != null)
                maxVal = Integer.valueOf(a.getValue()).intValue();
            int factor = 1;
            if ((a = child.getAttribute("factor")) != null)
                factor = Integer.valueOf(a.getValue()).intValue();
            int offset = 0;
            if ((a = child.getAttribute("offset")) != null)
                offset = Integer.valueOf(a.getValue()).intValue();
            String uppermask = "VVVVVVVV";
            if ((a = child.getAttribute("upperMask")) != null)
                uppermask = a.getValue();
            String highCVname = "";
            int highCVnumber = -1;
            int highCVpiVal = -1;
            int highCVsiVal = -1;
            if ((a = child.getAttribute("highCVname")) != null) {
                highCVname = a.getValue();
                int x = highCVname.indexOf('.');
                highCVnumber = Integer.valueOf(highCVname.substring(0, x)).intValue();
                int y = highCVname.indexOf('.',x+1);
                if (y > 0) {
                    highCVpiVal = Integer.valueOf(highCVname.substring(x+1, y)).intValue();
                    x = highCVname.lastIndexOf('.');
                    highCVsiVal = Integer.valueOf(highCVname.substring(x+1)).intValue();
                } else {
                    x = highCVname.lastIndexOf('.');
                    highCVpiVal = Integer.valueOf(highCVname.substring(x+1)).intValue();
                }
            }
            // ensure highCVnumber exists
            int highCVrow = _indxCvModel.addIndxCV(row, highCVname,
                                                   _piCv, highCVpiVal, _siCv, highCVsiVal, highCVnumber,
                                                   readOnly, infoOnly, writeOnly);
            iv = new IndexedPairVariableValue(row, name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly,
                                       cv, mask, minVal, maxVal, _indxCvModel.allIndxCvVector(),
                                       _status, item, highCVrow, highCVname, factor, offset, uppermask);

        } else {
            reportBogus();
            return -1;
        }

        // back to general processing
        // add tooltip text if present
        if ( (a = e.getAttribute("tooltip")) != null) {
            iv.setTooltipText(a.getValue());
        }
        // record new variable, update state, hook up listeners
        rowVector.addElement(iv);
        iv.setState(VariableValue.FROMFILE);
        iv.addPropertyChangeListener(this);

        // set to default value if specified (CV load may later override this)
        if ((a = e.getAttribute("default")) != null) {
            String val = a.getValue();
            if (log.isDebugEnabled()) log.debug("Found default value: "+val+" for "+name);
            iv.setIntValue(Integer.valueOf(val).intValue());
            if (_indxCvModel.getCvByRow(row).getInfoOnly()) {
                _indxCvModel.getCvByRow(row).setState(VariableValue.READ);
            } else {
                _indxCvModel.getCvByRow(row).setState(VariableValue.FROMFILE); // correct for transition to "edited"
            }
        } else {
            _indxCvModel.getCvByRow(row).setState(VariableValue.UNKNOWN);
        }
        return row;
    }

    void reportBogus() {
        log.error("Did not find a valid variable type");
    }

    /**
     * Configure from a constant.  This is like setRow (which processes
     * a variable Element).
     */
    public void setConstant(Element e) {
        // get the values for the VariableValue ctor
        String name = e.getAttribute("label").getValue();
        if (log.isDebugEnabled()) log.debug("Starting to setConstant \""+name+"\"");
        String stdname = ( e.getAttribute("item")!=null ?
                           e.getAttribute("item").getValue() :
                           null);
        String comment = null;
        if (e.getAttribute("comment") != null)
            comment = e.getAttribute("comment").getValue();
        String mask = null;

        // intrinsically readOnly, so use just that branch
        JButton bw = new JButton();
        _writeButtons.addElement(bw);

        // config read button as a dummy - there's really nothing to read
        JButton br = new JButton("Read");
        _readButtons.addElement(br);

        // no CV references are added here

        // have to handle various value types, see "snippet"
        Attribute a;

        // set to default value if specified (CV load will later override this)
        int defaultVal = 0;
        if ( (a = e.getAttribute("default")) != null) {
            String val = a.getValue();
            if (log.isDebugEnabled()) log.debug("Found default value: "+val+" for "+name);
            defaultVal = Integer.valueOf(val).intValue();
            if ( name.compareTo("PICV") == 0 ) {
                _piCv = Integer.valueOf(val).intValue();
            } else if ( name.compareTo("SICV") == 0 ) {
                _siCv = Integer.valueOf(val).intValue();
            }
        }

        // create the specific object

        ConstantValue v = new ConstantValue(name, comment, "", true, true, false, false,
                                            0, mask, defaultVal, defaultVal,
                                            _cvModel.allCvVector(), _status, stdname);

        // record new variable, update state, hook up listeners
        rowVector.addElement(v);
        v.setState(VariableValue.FROMFILE);
        v.addPropertyChangeListener(this);

        // set to default value if specified (CV load will later override this)
        if ( (a = e.getAttribute("default")) != null) {
            String val = a.getValue();
            if (log.isDebugEnabled()) log.debug("Found default value: "+val+" for "+name);
            v.setIntValue(defaultVal);
        }
    }

    public void newDecVariableValue(String name, int CV, String mask,
                                    boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly) {
        setFileDirty(true);
        String comment = "";
        int minVal = 0;
        int maxVal = 255;
        _cvModel.addCV(""+CV, readOnly, infoOnly, writeOnly);

        int row = getRowCount();

        // config write button
        JButton bw = new JButton("Write");
        bw.setActionCommand("W"+row);
        bw.addActionListener(this);
        _writeButtons.addElement(bw);

        // config read button
        JButton br = new JButton("Read");
        br.setActionCommand("R"+row);
        br.addActionListener(this);
        _readButtons.addElement(br);

        VariableValue v = new DecVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                                               CV, mask, minVal, maxVal, _cvModel.allCvVector(), _status, null);
        rowVector.addElement(v);
        v.addPropertyChangeListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) log.debug("action performed,  command: "+e.getActionCommand());
        setFileDirty(true);
        char b = e.getActionCommand().charAt(0);
        int row = Integer.valueOf(e.getActionCommand().substring(1)).intValue();
        if (log.isDebugEnabled()) log.debug("event on "+b+" row "+row);
        if (b=='R') {
            // read command
            read(row);
        } else {
            // write command
            write(row);
        }
    }

    /**
     * Command reading of a particular variable
     * @param i row number
     */
    public void read(int i) {
        VariableValue v = (VariableValue)rowVector.elementAt(i);
        v.readAll();
    }

    /**
     * Command writing of a particular variable
     * @param i row number
     */
    public void write(int i) {
        VariableValue v = (VariableValue)rowVector.elementAt(i);
        v.writeAll();
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("prop changed "+e.getPropertyName()
                      +" new value: "+e.getNewValue());
        }
        if (e.getNewValue() == null) {
            log.error("new value of "+e.getPropertyName()+" should not be null!");
            (new Exception()).printStackTrace();
        }
        setFileDirty(true);
        fireTableDataChanged();
    }

    public void configDone() {
        fireTableDataChanged();
    }

    /**
     * Represents any change to values, etc, hence rewriting the
     * file is desirable.
     */
    public boolean fileDirty() {
        return _fileDirty;
    }
    public void setFileDirty(boolean b) {
        _fileDirty = b;
    }
    private boolean _fileDirty;

    /**
     * Check for change to values, etc, hence rewriting the
     * decoder is desirable.
     */
    public boolean decoderDirty() {
        int len = rowVector.size();
        for (int i=0; i< len; i++) {
            if (((VariableValue)(rowVector.elementAt(i))).getState() == CvValue.EDITED ) return true;
        }
        return false;
    }

    public VariableValue findVar(String name) {
        for (int i=0; i<getRowCount(); i++) {
            if (name.equals(getItem(i)))return getVariable(i);
            if (name.equals(getLabel(i))) return  getVariable(i);
        }
        return null;
    }

    public int findVarIndex(String name) {
        for (int i=0; i<getRowCount(); i++) {
            if (name.equals(getItem(i))) return i;
            if (name.equals(getLabel(i))) return i;
            if (name.equals("CV"+getCvName(i))) return i;
//            try {
//                if (name.equals("CV"+((IndexedEnumVariableValue)rowVector.elementAt(i)).cvName())) return i;
//            } catch (Exception e){}
        }
        return -1;
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");

        // remove buttons
        for (int i = 0; i<_writeButtons.size(); i++) {
            ((JButton)_writeButtons.elementAt(i)).removeActionListener(this);
        }
        for (int i = 0; i<_readButtons.size(); i++) {
            ((JButton)_readButtons.elementAt(i)).removeActionListener(this);
        }

        // remove variables listeners
        for (int i = 0; i<rowVector.size(); i++) {
            VariableValue v = (VariableValue)rowVector.elementAt(i);
            v.removePropertyChangeListener(this);
            v.dispose();
        }

        headers = null;

        rowVector.removeAllElements();
        rowVector = null;

        _cvModel = null;
        _indxCvModel = null;

        _writeButtons.removeAllElements();
        _writeButtons = null;

        _readButtons.removeAllElements();
        _readButtons = null;

        _status = null;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VariableTableModel.class.getName());

}
