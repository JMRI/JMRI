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

import com.sun.java.util.collections.List;

/**
 * Display and edit a RosterEntry.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version	$Revision: 1.5 $
 */
public class RosterEntryPane extends javax.swing.JPanel  {

    JTextField id 		= new JTextField(12);
    JTextField roadName 	= new JTextField(12);
    JTextField roadNumber 	= new JTextField(12);
    JTextField mfg 		= new JTextField(12);
    JTextField model		= new JTextField(12);
    JTextField owner		= new JTextField(12);
    JLabel dccAddress		= new JLabel();
    JTextField comment		= new JTextField(12);
    JLabel filename 		= new JLabel();
    JLabel decoderModel 	= new JLabel();
    JLabel decoderFamily 	= new JLabel();
    JTextField decoderComment 	= new JTextField(12);

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

        // assemble the GUI
        setLayout(new GridLayout(12,2));

        add(new JLabel(rb.getString("FieldID")));
        add(id);

        add(new JLabel(rb.getString("FieldRoadName")));
        add(roadName);

        add(new JLabel(rb.getString("FieldRoadNumber")));
        add(roadNumber);

        add(new JLabel(rb.getString("FieldManufacturer")));
        add(mfg);

        add(new JLabel(rb.getString("FieldOwner")));
        add(owner);

        add(new JLabel(rb.getString("FieldModel")));
        add(model);

        add(new JLabel(rb.getString("FieldDCCAddress")));
        add(dccAddress);

        add(new JLabel(rb.getString("FieldComment")));
        add(comment);

        add(new JLabel(rb.getString("FieldDecoderFamily")));
        add(decoderFamily);

        add(new JLabel(rb.getString("FieldDecoderModel")));
        add(decoderModel);

        add(new JLabel(rb.getString("FieldDecoderComment")));
        add(decoderComment);

        add(new JLabel(rb.getString("FieldFilename")));
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
