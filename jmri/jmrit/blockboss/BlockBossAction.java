// BlockBossAction.java

package jmri.jmrit.blockboss;

import java.awt.event.ActionEvent;

import javax.swing.*;

/**
 * Swing action to create and register a
 * "Simple Signal Logic" GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.3 $
 */

public class BlockBossAction extends AbstractAction {

    public BlockBossAction(String s) { super(s);}
    public BlockBossAction() { super();}

    public void actionPerformed(ActionEvent e) {

        // create the frame
        JFrame f = new JFrame("Simple Signal Logic");

        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        // add lines of GUI
        JPanel line = new JPanel();
        line.add(new JLabel("Signal name: "));
        line.add(new JTextField(5));
        f.getContentPane().add(line);

        f.getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        line = new JPanel();
        line.add(new JLabel("Protects sensor: "));
        line.add(new JTextField(5));
        f.getContentPane().add(line);

        line = new JPanel();
        line.add(new JLabel("Protects turnout: "));
        line.add(new JTextField(5));
        line.add(new JLabel("when "));
        line.add(new JRadioButton("Closed"));
        line.add(new JRadioButton("Thrown"));
        f.getContentPane().add(line);

        line = new JPanel();
        line.add(new JLabel("Protects signal: "));
        line.add(new JTextField(5));
        line.add(new JCheckBox("with flashing yellow"));
        f.getContentPane().add(line);

        f.getContentPane().add(new JButton("OK"));

        f.pack();
        f.show();
    }

}


/* @(#)BlockBossAction.java */
