// RosterEntryPane.java

package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;


import com.sun.java.util.collections.List;

/**
 * Display and edit a RosterEntry.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001; Dennis Miller Copyright 2004
 * @version	$Revision: 1.6 $
 */
public class RosterEntryPane extends javax.swing.JPanel  {

    JTextField id 		= new JTextField(12);
    JTextField roadName 	= new JTextField(12);
    JTextField roadNumber 	= new JTextField(12);
    JTextField mfg 		= new JTextField(12);
    JTextField model		= new JTextField(12);
    JTextField owner		= new JTextField(12);
    JLabel dccAddress		= new JLabel();
    JTextArea comment		= new JTextArea(3,30);
    //JScrollPanes are defined with scroll bars on always to avoid undesireable resizing behavior
    //Without this the field will shrink to minimum size any time the scroll bars become needed and
    //the scroll bars are inside, not outside the field area, obscuring their contents.
    //This way the shrinking does not happen and the scroll bars are outside the field area,
    //leaving the contents visible
    JScrollPane commentScroller = new JScrollPane(comment,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    JLabel filename 		= new JLabel();
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
        dccAddress.setText(r.getDccAddress());
        roadName.setText(r.getRoadName());
        roadNumber.setText(r.getRoadNumber());
        mfg.setText(r.getMfg());
        owner.setText(r.getOwner());
        model.setText(r.getModel());
        comment.setText(r.getComment());
        decoderModel.setText(r.getDecoderModel());
        decoderFamily.setText(r.getDecoderFamily());
        decoderComment.setText(r.getDecoderComment());

        pane = this;
        re = r;

        // add options
        id.setToolTipText(rb.getString("ToolTipID"));

        dccAddress.setToolTipText(rb.getString("ToolTipDccAddress"));
        decoderModel.setToolTipText(rb.getString("ToolTipDecoderModel"));
        decoderFamily.setToolTipText(rb.getString("ToolTipDecoderFamily"));
        filename.setToolTipText(rb.getString("ToolTipFilename"));

        id.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (checkDuplicate())
                        JOptionPane.showMessageDialog(pane, rb.getString("ErrorDuplicateID"));

                }
            });

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
        JLabel row0Label = new JLabel(rb.getString("FieldID"));
        gbLayout.setConstraints(row0Label,cL);
        add(row0Label);

        cR.gridx = 1;
        cR.gridy = 0;
        cR.anchor = GridBagConstraints.WEST;
        id.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(id,cR);
        add(id);

        cL.gridy = 1;
        JLabel row1Label = new JLabel(rb.getString("FieldRoadName"));
        gbLayout.setConstraints(row1Label,cL);
        add(row1Label);

        cR.gridy = 1;
        roadName.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(roadName,cR);
        add(roadName);

        cL.gridy = 2;
        JLabel row2Label = new JLabel(rb.getString("FieldRoadNumber"));
        gbLayout.setConstraints(row2Label,cL);
        add(row2Label);

        cR.gridy = 2;
        roadNumber.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(roadNumber,cR);
        add(roadNumber);

        cL.gridy = 3;
        JLabel row3Label = new JLabel(rb.getString("FieldManufacturer"));
        gbLayout.setConstraints(row3Label,cL);
        add(row3Label);

        cR.gridy = 3;
        mfg.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(mfg,cR);
        add(mfg);

        cL.gridy = 4;
        JLabel row4Label = new JLabel(rb.getString("FieldOwner"));
        gbLayout.setConstraints(row4Label,cL);
        add(row4Label);

        cR.gridy = 4;
        owner.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(owner,cR);
        add(owner);

        cL.gridy = 5;
        JLabel row5Label = new JLabel(rb.getString("FieldModel"));
        gbLayout.setConstraints(row5Label,cL);
        add(row5Label);

        cR.gridy = 5;
        model.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(model,cR);
        add(model);

        cL.gridy = 6;
        JLabel row6Label = new JLabel(rb.getString("FieldDCCAddress"));
        gbLayout.setConstraints(row6Label,cL);
        add(row6Label);

        cR.gridy = 6;
        dccAddress.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(dccAddress,cR);
        add(dccAddress);

        cL.gridy = 7;
        JLabel row7Label = new JLabel(rb.getString("FieldComment"));
        gbLayout.setConstraints(row7Label,cL);
        add(row7Label);

        cR.gridy = 7;
        commentScroller.setMinimumSize(minScrollerDim);
        gbLayout.setConstraints(commentScroller,cR);
        add(commentScroller);

        cL.gridy = 8;
        JLabel row8Label = new JLabel(rb.getString("FieldDecoderFamily"));
        gbLayout.setConstraints(row8Label,cL);
        add(row8Label);

        cR.gridy = 8;
        decoderFamily.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(decoderFamily,cR);
        add(decoderFamily);

        cL.gridy = 9;
        JLabel row9Label = new JLabel(rb.getString("FieldDecoderModel"));
        gbLayout.setConstraints(row9Label,cL);
        add(row9Label);

        cR.gridy = 9;
        decoderModel.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(decoderModel,cR);
        add(decoderModel);

        cL.gridy = 10;
        JLabel row10Label = new JLabel(rb.getString("FieldDecoderComment"));
        gbLayout.setConstraints(row10Label,cL);
        add(row10Label);

        cR.gridy = 10;
        decoderCommentScroller.setMinimumSize(minScrollerDim);
        gbLayout.setConstraints(decoderCommentScroller,cR);
        add(decoderCommentScroller);

        cL.gridy = 11;
        JLabel row11Label = new JLabel(rb.getString("FieldFilename"));
        gbLayout.setConstraints(row11Label,cL);
        add(row11Label);

        cR.gridy = 11;
        filename.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(filename,cR);
        add(filename);


    }

    /**
     *
     * @return true if the value in the id JTextField
     * is a duplicate of some other RosterEntry in the roster
     */
    boolean checkDuplicate() {
        // check its not a duplicate
        List l = Roster.instance().matchingList(null, null, null, null, null, null, id.getText());
        boolean oops = false;
        for (int i=0; i<l.size(); i++) {
            if (re != (RosterEntry)l.get(i)) oops = true;
        }
        return oops;
    }

    /** Update GUI contents to be consistent with the contents of a RosterEntry object **/
    public void update(RosterEntry r) {
        r.setId(id.getText());
        r.setRoadName(roadName.getText());
        r.setRoadNumber(roadNumber.getText());
        r.setMfg(mfg.getText());
        r.setOwner(owner.getText());
        r.setModel(model.getText());
        r.setDccAddress(dccAddress.getText());
        r.setComment(comment.getText());
        r.setDecoderFamily(decoderFamily.getText());
        r.setDecoderModel(decoderModel.getText());
        r.setDecoderComment(decoderComment.getText());
    }

    public void setDccAddress(String a) { dccAddress.setText(a); }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RosterEntryPane.class.getName());

}
