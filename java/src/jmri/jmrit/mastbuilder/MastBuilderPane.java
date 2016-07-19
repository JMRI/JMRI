package jmri.jmrit.mastbuilder;

import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import jmri.util.FileUtil;

/**
 * Pane for building mast definitions
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class MastBuilderPane extends javax.swing.JPanel {

    public MastBuilderPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new jmri.util.javaworld.GridLayout2(1, 4));
        add(p);

        p.add(new JLabel("Signal System: "));
        p.add(new JComboBox<String>(new String[]{"basic", "AAR 1946", "NYCS 1937"}));

        p.add(new JLabel("Number of outputs: "));
        p.add(new JTextField(5));

        add(new JSeparator());

        p = new JPanel();
        p.setLayout(new jmri.util.javaworld.GridLayout2(1 + 4, 5));
        add(p);

        // first how is titles
        p.add(new JLabel("   Aspect name   \\  Select type"));
        p.add(new JComboBox<String>(new String[]{"SignalHead", "Turnout"}));
        p.add(new JComboBox<String>(new String[]{"SignalHead", "Turnout"}));
        p.add(new JComboBox<String>(new String[]{"SignalHead", "Turnout"}));
        p.add(new JLabel(""));

        p.add(new JComboBox<String>(new String[]{"Clear", "Advance Approach Medium", "Approach Limited", "Limited-Clear"}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JComboBox<String>(new String[]{"Closed", "Thrown"}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JButton("Delete"));

        p.add(new JComboBox<String>(new String[]{"Clear", "Advance Approach Medium", "Approach Limited", "Limited-Clear"}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JComboBox<String>(new String[]{"Closed", "Thrown"}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JButton("Delete"));

        p.add(new JComboBox<String>(new String[]{"Clear", "Advance Approach Medium", "Approach Limited", "Limited-Clear"}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JComboBox<String>(new String[]{"Closed", "Thrown"}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JButton("Delete"));

        p.add(new JComboBox<String>(new String[]{"Clear", "Advance Approach Medium", "Approach Limited", "Limited-Clear"}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JComboBox<String>(new String[]{"Closed", "Thrown"}));
        p.add(new JComboBox<Object>(icons()));
        p.add(new JButton("Delete"));

        p = new JPanel();
        p.setLayout(new FlowLayout());
        add(p);
        p.add(new JButton("Add aspect"));

        add(new JSeparator());

        p = new JPanel();
        p.setLayout(new FlowLayout());
        add(p);
        p.add(new JLabel("New appearance table name: "));
        p.add(new JTextField(30));
        p.add(new JButton("Save"));

    }

    Object[] icons() {
        Object[] list = new Object[3];
        list[0] = new ImageIcon(FileUtil.findURL("resources/icons/smallschematics/aspects/AAR-1946/SL-1-low/rule-287.gif"));
        list[1] = new ImageIcon(FileUtil.findURL("resources/icons/smallschematics/aspects/AAR-1946/SL-1-low/rule-290.gif"));
        list[2] = new ImageIcon(FileUtil.findURL("resources/icons/smallschematics/aspects/AAR-1946/SL-1-low/rule-292.gif"));
        return list;
    }

}
