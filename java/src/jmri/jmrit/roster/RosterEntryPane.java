// RosterEntryPane.java

package jmri.jmrit.roster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.InstanceManager;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import java.util.List;
import java.util.ArrayList;

/**
 * Display and edit a RosterEntry.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @author  Dennis Miller Copyright 2004, 2005
 * @version	$Revision$
 */
public class RosterEntryPane extends javax.swing.JPanel  {

// Field sizes expanded to 30 from 12 to match comment
// fields and allow for more text to be displayed
    JTextField id 		= new JTextField(30);
    JTextField roadName 	= new JTextField(30);
    JTextField maxSpeed		= new JTextField(3);
    JSpinner maxSpeedSpinner		= new JSpinner(new SpinnerNumberModel(100, 1, 100, 1));
 
    JTextField roadNumber 	= new JTextField(30);
    JTextField mfg 		= new JTextField(30);
    JTextField model		= new JTextField(30);
    JTextField owner		= new JTextField(30);
    DccLocoAddressSelector addrSel = new DccLocoAddressSelector();
    
    JTextArea comment		= new JTextArea(3,30);
    //JScrollPanes are defined with scroll bars on always to avoid undesirable resizing behavior
    //Without this the field will shrink to minimum size any time the scroll bars become needed and
    //the scroll bars are inside, not outside the field area, obscuring their contents.
    //This way the shrinking does not happen and the scroll bars are outside the field area,
    //leaving the contents visible
    JScrollPane commentScroller = new JScrollPane(comment,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    JLabel filename 		= new JLabel();
    JLabel dateUpdated   	= new JLabel();
    JLabel decoderModel 	= new JLabel();
    JLabel decoderFamily 	= new JLabel();
    JTextArea decoderComment	= new JTextArea(3,30);
    JScrollPane decoderCommentScroller = new JScrollPane(decoderComment,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    
    Component pane = null;
    RosterEntry re = null;

    final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");

    public RosterEntryPane(RosterEntry r) {

        id.setText(r.getId());
        filename.setText(r.getFileName());

        if (r.getDccAddress().equals("")) {
            // null address, so clear selector
            addrSel.reset();
        } else {
            // non-null address, so load
            DccLocoAddress tempAddr = new DccLocoAddress(
                Integer.parseInt(r.getDccAddress()), r.getProtocol());
            addrSel.setAddress(tempAddr);
        }
        
        // fill contents
        updateGUI(r);
        
        pane = this;
        re = r;

        // add options
        id.setToolTipText(rb.getString("ToolTipID"));

        addrSel.setEnabled(false);
        addrSel.setLocked(false);
        
        if ((InstanceManager.throttleManagerInstance() !=null) 
            && !InstanceManager.throttleManagerInstance().addressTypeUnique()){
            // This goes through to find common protocols between the command station and the decoder
            // and will set the selection box list to match those that are common.
            jmri.ThrottleManager tm = InstanceManager.throttleManagerInstance();
            List<LocoAddress.Protocol> protocoltypes = new ArrayList<LocoAddress.Protocol>();
            for (LocoAddress.Protocol prot : tm.getAddressProtocolTypes()) {
                protocoltypes.add(prot);
            }

            if(!protocoltypes.contains(LocoAddress.Protocol.DCC_LONG) && !protocoltypes.contains(LocoAddress.Protocol.DCC_SHORT)){
                //Multi protocol systems so far are not worried about dcc long vs dcc short
                List<DecoderFile> l = DecoderIndexFile.instance().matchingDecoderList(null, r.getDecoderFamily(), null, null, null,r.getDecoderModel());
                if (log.isDebugEnabled()) log.debug("found "+l.size()+" matches");
                if (l.size() == 0) {
                    log.debug("Loco uses "+decoderFamily+" "+decoderModel+" decoder, but no such decoder defined");
                    // fall back to use just the decoder name, not family
                    l = DecoderIndexFile.instance().matchingDecoderList(null, null, null, null, null, r.getDecoderModel());
                    if (log.isDebugEnabled()) log.debug("found "+l.size()+" matches without family key");
                }
                DecoderFile d=null;
                if (l.size() > 0) {
                    d = l.get(0);
                    if(d!=null && d.getSupportedProtocols().length>0){
                        ArrayList<String> protocols = new ArrayList<String>(d.getSupportedProtocols().length);
                        
                        for(LocoAddress.Protocol i:d.getSupportedProtocols()){
                            if(protocoltypes.contains(i))
                                protocols.add(tm.getAddressTypeString(i));
                        }
                        addrSel = new DccLocoAddressSelector(protocols.toArray(new String[protocols.size()]));
                        DccLocoAddress tempAddr = new DccLocoAddress(
                            Integer.parseInt(r.getDccAddress()), r.getProtocol());
                        addrSel.setAddress(tempAddr);
                        addrSel.setEnabled(false);
                        addrSel.setLocked(false);
                        addrSel.setEnabledProtocol(true);
                    }
                }
            }
        }
        
        JPanel selPanel = addrSel.getCombinedJPanel();
        selPanel.setToolTipText(rb.getString("ToolTipDccAddress"));
        decoderModel.setToolTipText(rb.getString("ToolTipDecoderModel"));
        decoderFamily.setToolTipText(rb.getString("ToolTipDecoderFamily"));
        filename.setToolTipText(rb.getString("ToolTipFilename"));
        dateUpdated.setToolTipText(rb.getString("ToolTipDateUpdated"));
        id.addFocusListener(
            new FocusListener() {
                public void focusGained(FocusEvent e){}
                public void focusLost(FocusEvent e) {
                    if (checkDuplicate())
                        JOptionPane.showMessageDialog(pane, rb.getString("ErrorDuplicateID"));
                }
            }
        );

        // New GUI to allow multiline Comment and Decoder Comment fields
        //Set up constraints objects for convenience in GridBagLayout alignment
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints cL = new GridBagConstraints();
        GridBagConstraints cR = new GridBagConstraints();
        Dimension minFieldDim = new Dimension(150,20);
        Dimension minScrollerDim = new Dimension(165,42);
        setLayout(gbLayout);

        cL.gridx = 0;
        cL.gridy = 0;
        cL.ipadx = 3;
        cL.anchor = GridBagConstraints.NORTHWEST;
        cL.insets = new Insets (0,0,0,15);
        JLabel row0Label = new JLabel(rb.getString("FieldID")+":");
        gbLayout.setConstraints(row0Label,cL);
        add(row0Label);

        cR.gridx = 1;
        cR.gridy = 0;
        cR.anchor = GridBagConstraints.WEST;
        id.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(id,cR);
        add(id);

        cL.gridy = 1;
        JLabel row1Label = new JLabel(rb.getString("FieldRoadName")+":");
        gbLayout.setConstraints(row1Label,cL);
        add(row1Label);

        cR.gridy = 1;
        roadName.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(roadName,cR);
        add(roadName);

        cL.gridy = 2;
        JLabel row2Label = new JLabel(rb.getString("FieldRoadNumber")+":");
        gbLayout.setConstraints(row2Label,cL);
        add(row2Label);

        cR.gridy = 2;
        roadNumber.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(roadNumber,cR);
        add(roadNumber);

        cL.gridy = 3;
        JLabel row3Label = new JLabel(rb.getString("FieldManufacturer")+":");
        gbLayout.setConstraints(row3Label,cL);
        add(row3Label);

        cR.gridy = 3;
        mfg.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(mfg,cR);
        add(mfg);

        cL.gridy = 4;
        JLabel row4Label = new JLabel(rb.getString("FieldOwner")+":");
        gbLayout.setConstraints(row4Label,cL);
        add(row4Label);

        cR.gridy = 4;
        owner.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(owner,cR);
        add(owner);

        cL.gridy = 5;
        JLabel row5Label = new JLabel(rb.getString("FieldModel")+":");
        gbLayout.setConstraints(row5Label,cL);
        add(row5Label);

        cR.gridy = 5;
        model.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(model,cR);
        add(model);

        cL.gridy = 6;
        JLabel row6Label = new JLabel(rb.getString("FieldDCCAddress")+":");
        gbLayout.setConstraints(row6Label,cL);
        add(row6Label);

        cR.gridy = 6;
        gbLayout.setConstraints(selPanel,cR);
        add(selPanel);

        cL.gridy = 7;
        JLabel row7Label = new JLabel(rb.getString("FieldSpeedLimit")+":");
        gbLayout.setConstraints(row7Label,cL);
        add(row7Label);

        cR.gridy = 7;
        maxSpeedSpinner.setEditor(new JSpinner.NumberEditor(maxSpeedSpinner, "#"));
        /*
         * Code below can disables editing of field (commented out for now)
         JSpinner.DefaultEditor editor = ((JSpinner.DefaultEditor)maxSpeedSpinner.getEditor());
         editor.getTextField().setEditable(false);
        */
        gbLayout.setConstraints(maxSpeedSpinner,cR);
        add(maxSpeedSpinner);
        
        cL.gridy = 8;
        JLabel row8Label = new JLabel(rb.getString("FieldComment")+":");
        gbLayout.setConstraints(row8Label,cL);
        add(row8Label);

        cR.gridy = 8;
        commentScroller.setMinimumSize(minScrollerDim);
        gbLayout.setConstraints(commentScroller,cR);
        add(commentScroller);

        cL.gridy = 9;
        JLabel row9Label = new JLabel(rb.getString("FieldDecoderFamily")+":");
        gbLayout.setConstraints(row9Label,cL);
        add(row9Label);

        cR.gridy = 9;
        decoderFamily.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(decoderFamily,cR);
        add(decoderFamily);

        cL.gridy = 10;
        JLabel row10Label = new JLabel(rb.getString("FieldDecoderModel")+":");
        gbLayout.setConstraints(row10Label,cL);
        add(row10Label);

        cR.gridy = 10;
        decoderModel.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(decoderModel,cR);
        add(decoderModel);

        cL.gridy = 11;
        JLabel row11Label = new JLabel(rb.getString("FieldDecoderComment")+":");
        gbLayout.setConstraints(row11Label,cL);
        add(row11Label);

        cR.gridy = 11;
        decoderCommentScroller.setMinimumSize(minScrollerDim);
        gbLayout.setConstraints(decoderCommentScroller,cR);
        add(decoderCommentScroller);

        cL.gridy = 12;
        JLabel row12Label = new JLabel(rb.getString("FieldFilename")+":");
        gbLayout.setConstraints(row12Label,cL);
        add(row12Label);

        cR.gridy = 12;
        filename.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(filename,cR);
        add(filename);

        cL.gridy = 13;
        JLabel row13Label = new JLabel(rb.getString("FieldDateUpdated")+":");
        gbLayout.setConstraints(row13Label,cL);
        add(row13Label);

        cR.gridy = 13;
        filename.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(dateUpdated,cR);
        add(dateUpdated);
    }

    /**
     * Does the GUI contents agree with a RosterEntry?
     */
    public boolean guiChanged(RosterEntry r) {
        if (!r.getRoadName().equals(roadName.getText()) ) return true;
        if (!r.getRoadNumber().equals(roadNumber.getText()) ) return true;
        if (!r.getMfg().equals(mfg.getText()) ) return true;
        if (!r.getOwner().equals(owner.getText()) ) return true;
        if (!r.getModel().equals(model.getText()) ) return true;
        if (!r.getComment().equals(comment.getText()) ) return true;
        if (!r.getDecoderFamily().equals(decoderFamily.getText()) ) return true;
        if (!r.getDecoderModel().equals(decoderModel.getText()) ) return true;
        if (!r.getDecoderComment().equals(decoderComment.getText()) ) return true;
        if (!r.getId().equals(id.getText()) ) return true;
        if (r.getMaxSpeedPCT() != ((Integer) maxSpeedSpinner.getValue()).intValue()) return true;

        DccLocoAddress a = addrSel.getAddress();
        if (a==null) {
            if (!r.getDccAddress().equals("")) return true;
        } else {
            
            if(r.getProtocol()!=a.getProtocol()) return true;
            if (! r.getDccAddress().equals(""+a.getNumber()) ) return true;
        }
        return false;
    }

    /**
     *
     * @return true if the value in the id JTextField
     * is a duplicate of some other RosterEntry in the roster
     */
    public boolean checkDuplicate() {
        // check its not a duplicate
        List<RosterEntry> l = Roster.instance().matchingList(null, null, null, null, null, null, id.getText());
        boolean oops = false;
        for (int i=0; i<l.size(); i++) {
            if (re!=l.get(i)) oops =true;
        }
        return oops;
    }

    /** 
     * Fill a RosterEntry object from GUI contents
     **/
    public void update(RosterEntry r) {
        r.setId(id.getText());
        r.setRoadName(roadName.getText());
        r.setRoadNumber(roadNumber.getText());
        r.setMfg(mfg.getText());
        r.setOwner(owner.getText());
        r.setModel(model.getText());
        DccLocoAddress a = addrSel.getAddress();
        if (a != null) {
            r.setDccAddress(""+a.getNumber());
            r.setProtocol(a.getProtocol());
        }
        r.setComment(comment.getText());
        
        JComponent editor = maxSpeedSpinner.getEditor();
        r.setMaxSpeedPCT(Integer.parseInt(((JSpinner.DefaultEditor)editor).getTextField().getText()));
        r.setDecoderFamily(decoderFamily.getText());
        r.setDecoderModel(decoderModel.getText());
        r.setDecoderComment(decoderComment.getText());
    }

    /**
     * File GUI from roster contents
     */
    public void updateGUI(RosterEntry r) {
        roadName.setText(r.getRoadName());
        roadNumber.setText(r.getRoadNumber());
        mfg.setText(r.getMfg());
        owner.setText(r.getOwner());
        model.setText(r.getModel());
        comment.setText(r.getComment());
        decoderModel.setText(r.getDecoderModel());
        decoderFamily.setText(r.getDecoderFamily());
        decoderComment.setText(r.getDecoderComment());
        dateUpdated.setText(r.getDateUpdated());
        maxSpeedSpinner.setValue(Integer.valueOf(r.getMaxSpeedPCT()));
    }
    
    public void setDccAddress(String a) {
        DccLocoAddress addr = addrSel.getAddress();
        LocoAddress.Protocol protocol = addr.getProtocol();
        addrSel.setAddress(new DccLocoAddress(Integer.parseInt(a), protocol));
    }
    public void setDccAddressLong(boolean m) {
        DccLocoAddress addr = addrSel.getAddress();
        int n = 0;
        if(addr!=null){
            //If the protocol is already set to something other than DCC, then do not try to configure it as DCC long or short.
            if(addr.getProtocol() != LocoAddress.Protocol.DCC_LONG 
                        && addr.getProtocol() != LocoAddress.Protocol.DCC_SHORT 
                        && addr.getProtocol() != LocoAddress.Protocol.DCC )
                return;
            n = addr.getNumber();
        }
        addrSel.setAddress(new DccLocoAddress(n, m));
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
    }

    static Logger log = LoggerFactory.getLogger(RosterEntryPane.class.getName());

}
