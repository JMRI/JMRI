// InitiateSoundEditor.java

package jmri.jmrix.loconet.sdfeditor;

import jmri.jmrix.loconet.sdf.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Editor panel for the INITIATE_SOUND macro.
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision$
 */

class InitiateSoundEditor extends SdfMacroEditor {

    public InitiateSoundEditor(SdfMacro inst) {
        super(inst);
        
        // remove warning message from SdfMacroEditor
        this.removeAll();
        
        // and set up our own
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        
        p.add(new JLabel("Start sequence when: "));
        box = new JComboBox(SdfConstants.editorTriggerNames);

        // find & set index of selected value
        update();
        
        p.add(box);
        add(p);

        // check boxes
        add(zap);
        add(run);
        add(noprempt);
        add(nottrig);
        
        // change the instruction when the value is changed
        ActionListener l = new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // have to convert back from string to 
                // trigger value
                String trigger = (String)box.getSelectedItem();
                int value = jmri.util.StringUtil.getStateFromName(trigger, SdfConstants.triggerCodes, SdfConstants.editorTriggerNames);
                ((InitiateSound)InitiateSoundEditor.this.inst).setTrigger(value);
                // buttons
                int prempt = 0;
                if (zap.isSelected())  prempt = prempt | SdfConstants.ZAP;
                if (run.isSelected())  prempt = prempt | SdfConstants.RUN_WHILE_TRIG;
                if (noprempt.isSelected()) prempt = prempt | SdfConstants.NO_PREEMPT_TRIG;
                if (nottrig.isSelected())  prempt = prempt | SdfConstants.NOT_TRIG;
                ((InitiateSound)InitiateSoundEditor.this.inst).setPrempt(prempt);
                // tell the world
                updated();
            }
        };
        
        box.addActionListener(l);
        zap.addActionListener(l);
        run.addActionListener(l);
        noprempt.addActionListener(l);
        nottrig.addActionListener(l);
    }
    
    JComboBox box;
    
    public void update() {
        // find & set index of selected trigger
        InitiateSound instruction = (InitiateSound)inst;
        int trig = instruction.getTrigger();
        for (int i=0; i<SdfConstants.triggerCodes.length; i++)
            if (SdfConstants.triggerCodes[i]==trig) {
                box.setSelectedIndex(i);
                break;
            }
        // buttons
        int prempt = instruction.getPrempt();
        zap.setSelected((prempt&SdfConstants.ZAP)!=0);
        run.setSelected((prempt&SdfConstants.RUN_WHILE_TRIG)!=0);
        noprempt.setSelected((prempt&SdfConstants.NO_PREEMPT_TRIG)!=0);
        nottrig.setSelected((prempt&SdfConstants.NOT_TRIG)!=0);
        
    }        
    
    JCheckBox zap = new JCheckBox("Zap");
    JCheckBox run = new JCheckBox("Run while triggered");
    JCheckBox noprempt = new JCheckBox("No preemptive trigger");
    JCheckBox nottrig = new JCheckBox("Not triggered");
    
}

/* @(#)InitiateSoundEditor.java */
