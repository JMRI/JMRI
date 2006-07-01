// BlockBossAction.java

package jmri.jmrit.blockboss;

import jmri.Turnout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * Swing action to create and register a
 * "Simple Signal Logic" GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.6 $
 */

public class BlockBossAction extends AbstractAction {

    public BlockBossAction(String s) { super(s);}
    public BlockBossAction() { super();}

    JTextField outSignalField;
    JTextField sensorField;
    JTextField protectTurnoutField;
    JRadioButton protectTurnoutThrownButton;
    JRadioButton protectTurnoutClosedButton;
    JTextField nextSignalField1;
    JCheckBox flashBox;

    public void actionPerformed(ActionEvent e) {

        // create the frame
        JFrame f = new JFrame("Simple Signal Logic");

        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        // add lines of GUI
        JPanel line = new JPanel();
        line.add(new JLabel("Signal name: "));
        line.add(outSignalField= new JTextField(5));
        outSignalField.setToolTipText("Enter signal head and hit return for existing info");
        f.getContentPane().add(line);
        outSignalField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activate();
            }
        });

        f.getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        line = new JPanel();
        line.add(new JLabel("Protects sensor: "));
        line.add(sensorField = new JTextField(5));
        f.getContentPane().add(line);

        line = new JPanel();
        line.add(new JLabel("Protects turnout: "));
        line.add(protectTurnoutField = new JTextField(5));
        line.add(new JLabel("when "));
        JPanel above = new JPanel();
        above.setLayout(new BoxLayout(above, BoxLayout.Y_AXIS));
        above.add(protectTurnoutClosedButton = new JRadioButton("Closed"));
        above.add(protectTurnoutThrownButton = new JRadioButton("Thrown"));
        line.add(above);
        ButtonGroup g = new ButtonGroup();
        g.add(protectTurnoutClosedButton);
        g.add(protectTurnoutThrownButton);
        f.getContentPane().add(line);

        line = new JPanel();
        line.add(new JLabel("Protects signal: "));
        line.add(nextSignalField1 = new JTextField(5));
        line.add(flashBox = new JCheckBox("with flashing yellow"));
        f.getContentPane().add(line);

        JButton b = new JButton("OK");
        f.getContentPane().add(b);
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });

        f.pack();
        f.show();
    }

    void okPressed() {
        BlockBossLogic b = BlockBossLogic.getStoppedObject(outSignalField.getText());
        b.setSensor(sensorField.getText());

        b.setTurnout(protectTurnoutField.getText(),
                    protectTurnoutThrownButton.isSelected() ? Turnout.THROWN : Turnout.CLOSED);

        b.setWatchedSignal(nextSignalField1.getText(), flashBox.isSelected());
        b.retain();
        b.start();
    }

    void activate() {
        BlockBossLogic b = BlockBossLogic.getExisting(outSignalField.getText());
        if (b==null) return;
        sensorField.setText(b.getSensor());
        protectTurnoutField.setText(b.getTurnout());
        nextSignalField1.setText(b.getWatchedSignal());
        flashBox.setSelected(b.getUseFlash());
        protectTurnoutThrownButton.setSelected( b.getTurnoutState()==Turnout.THROWN);
    }
}

/* @(#)BlockBossAction.java */
