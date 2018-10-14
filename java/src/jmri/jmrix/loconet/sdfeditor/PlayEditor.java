package jmri.jmrix.loconet.sdfeditor;

import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.jmrix.loconet.sdf.Play;
import jmri.jmrix.loconet.sdf.SdfConstants;
import jmri.jmrix.loconet.sdf.SdfMacro;

/**
 * Editor panel for the PLAY macro from the Digitrax sound definition language
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
class PlayEditor extends SdfMacroEditor {

    public PlayEditor(SdfMacro inst) {
        super(inst);
        // remove warning message from SdfMacroEditor
        this.removeAll();

        // find & set selected values
        update();

        // set up GUI
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // handle numbers
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

        p.add(new JLabel("Play handle number: "));
        p.add(handle);
        add(p);

        // loop control
        p = new JPanel();
        p.add(new JLabel("Loop control: "));
        p.add(loop);
        add(p);

        // loop control
        p = new JPanel();
        p.add(wavbrk1);
        p.add(wavbrk2);
        add(p);

        // change the instruction when the value is changed
        ActionListener l = new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                guiChanged();
            }
        };
        loop.addActionListener(l);
        wavbrk1.addActionListener(l);
        wavbrk2.addActionListener(l);
        ChangeListener c = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                guiChanged();
            }
        };
        handle.addChangeListener(c);
    }

    SpinnerNumberModel handleModel = new SpinnerNumberModel(0, 0, 63, 1);
    JSpinner handle = new JSpinner(handleModel);
    JComboBox<String> loop = new JComboBox<String>(SdfConstants.loopNames);
    JCheckBox wavbrk1 = new JCheckBox("Invert Loop Reason");
    JCheckBox wavbrk2 = new JCheckBox("Global Loop Reason");

    /**
     * update instruction if GUI changes
     */
    void guiChanged() {
        Play instruction = (Play) PlayEditor.this.inst;

        instruction.setHandle(handleModel.getNumber().intValue());

        instruction.setBrk((String) loop.getSelectedItem());

        int flag = 0;
        if (wavbrk1.isSelected()) {
            flag |= 0x01;
        }
        if (wavbrk2.isSelected()) {
            flag |= 0x02;
        }
        instruction.setWaveBrkFlags(flag);

        // tell the world
        updated();
    }

    @Override
    public void update() {
        // find & set index of selected trigger
        Play instruction = (Play) inst;
        int handleVal = Integer.parseInt(instruction.handleVal());
        handleModel.setValue(Integer.valueOf(handleVal));

        // loop flag
        loop.setSelectedItem(instruction.brkVal());

        // wavbreak flags
        int flags = instruction.getWaveBrkFlags();
        wavbrk1.setSelected((flags & 0x01) != 0);
        wavbrk2.setSelected((flags & 0x02) != 0);
    }
}
