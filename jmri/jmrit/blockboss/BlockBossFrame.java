// BlockBossFrame.java

package jmri.jmrit.blockboss;

import jmri.Turnout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.*;

/**
 * Provide "Simple Signal Logic" GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */

public class BlockBossFrame extends JFrame {

    JTextField outSignalField;
    JTextField sensorField;
    JTextField protectTurnoutField;
    JRadioButton protectTurnoutThrownButton;
    JRadioButton protectTurnoutClosedButton;
    JTextField nextSignalField1;
    JCheckBox flashBox;

    public BlockBossFrame() { this("Simple Signal Logic");}
    public BlockBossFrame(String frameName) {

        // create the frame
        super(frameName);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add save menu item
        JMenuBar menuBar = new JMenuBar();
        ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.configurexml.StoreXmlConfigAction());
        setJMenuBar(menuBar);

        // add lines of GUI
        JPanel line = new JPanel();
        line.add(new JLabel("Signal name: "));
        line.add(outSignalField= new JTextField(5));
        outSignalField.setToolTipText("Enter signal head and hit return for existing info");
        getContentPane().add(line);
        outSignalField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activate();
            }
        });

        getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        line = new JPanel();
        line.add(new JLabel("Protects sensor: "));
        line.add(sensorField = new JTextField(5));
        getContentPane().add(line);

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
        getContentPane().add(line);

        line = new JPanel();
        line.add(new JLabel("Protects signal: "));
        line.add(nextSignalField1 = new JTextField(5));
        line.add(flashBox = new JCheckBox("with flashing yellow"));
        getContentPane().add(line);

        JButton b = new JButton("OK");
        getContentPane().add(b);
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });

        pack();
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

    private boolean mShown = false;

    public void addNotify() {
        super.addNotify();

        if (mShown)
            return;

        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
        mShown = true;
    }
    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }

    public void dispose() {
        outSignalField = null;
        sensorField = null;
        protectTurnoutField = null;
        protectTurnoutThrownButton = null;
        protectTurnoutClosedButton = null;
        nextSignalField1 = null;
        flashBox = null;
        super.dispose();
    }
}

/* @(#)BlockBossFrame.java */
