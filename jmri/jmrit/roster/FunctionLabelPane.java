// FunctionEntryPane.java

package jmri.jmrit.roster;

import java.awt.*;
import javax.swing.*;
import java.util.ResourceBundle;


/**
 * Display and edit the function labels in a RosterEntry
 *
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version	$Revision: 1.5 $
 */
public class FunctionLabelPane extends javax.swing.JPanel {
    RosterEntry re;
    
    final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");

    JTextField[] labels;
    JCheckBox[] lockable;
    
    public FunctionLabelPane(RosterEntry r) {

        re = r;

        // we're doing a manual allocation of position for
        // now, based on 28 labels
        int maxfunction = 28;
        
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints cL = new GridBagConstraints();
        setLayout(gbLayout);

        labels = new JTextField[maxfunction+1];
        lockable = new JCheckBox[maxfunction+1];
        
        cL.gridx = 0;
        cL.gridy = 0;
        cL.ipadx = 3;
        cL.anchor = GridBagConstraints.NORTHWEST;
        cL.insets = new Insets (0,0,0,15);
        cL.fill = GridBagConstraints.HORIZONTAL;
        cL.weighty = 1.0;
        int nextx = 0;
        
        add(new JLabel("fn"), cL);
        cL.gridx++;
        add(new JLabel("label"), cL);
        cL.gridx++;
        add(new JLabel("lock"), cL);
        cL.gridx++;
        add(new JLabel("fn"), cL);
        cL.gridx++;
        add(new JLabel("label"), cL);
        cL.gridx++;
        add(new JLabel("lock"), cL);
        cL.gridx++;
        
        cL.gridx = 0;
        cL.gridy = 1;
        for (int i = 0; i<=maxfunction; i++) {
            // label the row
            add(new JLabel(""+i), cL);
            cL.gridx++;
            
            // add the label
            labels[i] = new JTextField(20);
            if (r.getFunctionLabel(i)!=null) labels[i].setText(r.getFunctionLabel(i));
            add(labels[i], cL);
            cL.gridx++;
            
            // add the checkbox
            lockable[i] = new JCheckBox();
            lockable[i].setSelected(r.getFunctionLockable(i));
            add(lockable[i], cL);
            cL.gridx++;

            // advance position
            cL.gridy++;
            if (cL.gridy-1 == ((maxfunction+1)/2)+1) {
                cL.gridy = 1;  // skip titles
                nextx = nextx+3;
            }
            cL.gridx = nextx;
        }
    }
    
    /**
     * Does the GUI contents agree with a RosterEntry?
     */
    public boolean guiChanged(RosterEntry r) {
        if (labels!=null) {
            for (int i = 0; i<labels.length; i++) 
               if (labels[i]!=null) {
                    if (r.getFunctionLabel(i)==null && !labels[i].getText().equals(""))
                        return true;
                    if (r.getFunctionLabel(i)!=null && !r.getFunctionLabel(i).equals(labels[i].getText()))
                        return true;
                }
        }
        if (lockable!=null) {
            for (int i = 0; i<lockable.length; i++) 
                if (lockable[i]!=null) {
                    if (r.getFunctionLockable(i) && !lockable[i].isSelected())
                        return true;
                    if (!r.getFunctionLockable(i) && lockable[i].isSelected())
                        return true;
                }
        }
        return false;        
    }
        
    /** 
     * Fill a RosterEntry object from GUI contents
     **/
    public void update(RosterEntry r) {
        if (labels!=null) {
            for (int i = 0; i<labels.length; i++) 
                if (labels[i]!=null && !labels[i].getText().equals("")) {
                    r.setFunctionLabel(i, labels[i].getText());
                    r.setFunctionLockable(i, lockable[i].isSelected());
                } else if (labels[i]!=null && labels[i].getText().equals("")) {
                    if (r.getFunctionLabel(i) != null) {
                        r.setFunctionLabel(i, null);
                    }
                }
        }
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FunctionLabelPane.class.getName());

}
