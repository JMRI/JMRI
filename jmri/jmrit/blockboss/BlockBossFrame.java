// BlockBossFrame.java

package jmri.jmrit.blockboss;

import jmri.Turnout;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.*;

/**
 * Provide "Simple Signal Logic" GUI.
 * <P>
 * Provides four panels, corresponding to the four possible
 * modes described in {@link BlockBossLogic}, which
 * are then selected via radio buttons in the GUI.
 * <P>
 * The four modes are:
 * <UL>
 * <LI>Single block (s)
 * <LI>Facing point (f)
 * <LI>Trailing point main (tm)
 * <LI>Trailing point diverging (td)
 * </UL>
 * <P>
 * The multiple-panel approach to the GUI is used to make layout easier;
 * the code just flips from one to the other as the user selects a mode.
 * The individual items all share data models to simplify the logic.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.3 $
 */

public class BlockBossFrame extends JFrame {

    JPanel modeSingle               = new JPanel();
    JRadioButton buttonSingle;
    JTextField sSensorField1        = new JTextField(6);
    JTextField sSensorField2        = new JTextField(6);
    JTextField sSensorField3        = new JTextField(6);
    JTextField sSensorField4        = new JTextField(6);
    JTextField sNextSignalField1    = new JTextField(6);
    JCheckBox sFlashBox;

    JPanel modeTrailMain                = new JPanel();
    JRadioButton buttonTrailMain;
    JTextField tmSensorField1           = new JTextField(6);
    JTextField tmSensorField2           = new JTextField(6);
    JTextField tmSensorField3           = new JTextField(6);
    JTextField tmSensorField4           = new JTextField(6);
    JTextField tmProtectTurnoutField    = new JTextField(6);
    JTextField tmNextSignalField1       = new JTextField(6);
    JCheckBox tmFlashBox;

    JPanel modeTrailDiv                 = new JPanel();
    JRadioButton buttonTrailDiv;
    JTextField tdSensorField1           = new JTextField(6);
    JTextField tdSensorField2           = new JTextField(6);
    JTextField tdSensorField3           = new JTextField(6);
    JTextField tdSensorField4           = new JTextField(6);
    JTextField tdProtectTurnoutField    = new JTextField(6);
    JTextField tdNextSignalField1       = new JTextField(6);
    JCheckBox tdFlashBox;

    JPanel modeFacing               = new JPanel();
    JRadioButton buttonFacing;
    JTextField fSensorField1        = new JTextField(6);
    JTextField fSensorField2        = new JTextField(6);
    JTextField fSensorField3        = new JTextField(6);
    JTextField fSensorField4        = new JTextField(6);
    JTextField fProtectTurnoutField = new JTextField(6);
    JTextField fNextSignalField1    = new JTextField(6);
    JTextField fNextSignalField2    = new JTextField(6);
    JCheckBox fFlashBox;

    JTextField outSignalField;

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

        // create GUI items
        sFlashBox  = new JCheckBox("With Flashing Yellow");
        tmFlashBox = new JCheckBox("With Flashing Yellow");
        tmFlashBox.setModel(sFlashBox.getModel());
        tdFlashBox = new JCheckBox("With Flashing Yellow");
        tdFlashBox.setModel(sFlashBox.getModel());
        fFlashBox  = new JCheckBox("With Flashing Yellow");
        fFlashBox.setModel(sFlashBox.getModel());

        buttonSingle = new JRadioButton("On Single Block");
        buttonTrailMain = new JRadioButton("On Main Leg of Trailing-Point Turnout");
        buttonTrailDiv = new JRadioButton("On Diverging Leg of Trailing-Point Turnout");
        buttonFacing = new JRadioButton("On Facing-Point Turnout");
        ButtonGroup g = new ButtonGroup();
        g.add(buttonSingle);
        g.add(buttonTrailMain);
        g.add(buttonTrailDiv);
        g.add(buttonFacing);
        ActionListener a = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonClicked();
            }
        };

        buttonSingle.addActionListener(a);
        buttonTrailMain.addActionListener(a);
        buttonTrailDiv.addActionListener(a);
        buttonFacing.addActionListener(a);

        // share data models
        tmSensorField1.setDocument(sSensorField1.getDocument());
        tdSensorField1.setDocument(sSensorField1.getDocument());
        fSensorField1.setDocument(sSensorField1.getDocument());

        tmSensorField2.setDocument(sSensorField2.getDocument());
        tdSensorField2.setDocument(sSensorField2.getDocument());
        fSensorField2.setDocument(sSensorField2.getDocument());

        tmSensorField3.setDocument(sSensorField3.getDocument());
        tdSensorField3.setDocument(sSensorField3.getDocument());
        fSensorField3.setDocument(sSensorField3.getDocument());

        tmSensorField4.setDocument(sSensorField4.getDocument());
        tdSensorField4.setDocument(sSensorField4.getDocument());
        fSensorField4.setDocument(sSensorField4.getDocument());

        tdProtectTurnoutField.setDocument(tmProtectTurnoutField.getDocument());
        fProtectTurnoutField.setDocument(tmProtectTurnoutField.getDocument());

        tdNextSignalField1.setDocument(sNextSignalField1.getDocument());
        tmNextSignalField1.setDocument(sNextSignalField1.getDocument());
        fNextSignalField1.setDocument(sNextSignalField1.getDocument());

        // add top part of GUI, holds signal head name to drive
        JPanel line = new JPanel();
        line.add(new JLabel("Signal Named "));
        line.add(outSignalField= new JTextField(5));
        outSignalField.setToolTipText("Enter signal head and hit return for existing info");
        getContentPane().add(line);
        outSignalField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activate();
            }
        });

        line = new JPanel();
        line.setLayout(new BoxLayout(line, BoxLayout.Y_AXIS));
        line.add(buttonSingle);
        line.add(buttonTrailMain);
        line.add(buttonTrailDiv);
        line.add(buttonFacing);
        getContentPane().add(line);

        getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        // fill in the specific panels for the modes
        getContentPane().add(fillModeSingle());
        getContentPane().add(fillModeTrailMain());
        getContentPane().add(fillModeTrailDiv());
        getContentPane().add(fillModeFacing());

        getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        // add OK button at bottom
        JButton b = new JButton("OK");
        getContentPane().add(b);
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });

        pack();
        // set a definite mode selection, which also repacks.
        buttonSingle.setSelected(true);
        buttonClicked();

    }

    JPanel fillModeSingle() {
        modeSingle.setLayout(new BoxLayout(modeSingle, BoxLayout.Y_AXIS));

        JPanel line = new JPanel();
        line.add(new JLabel("Protects Sensors "));
        line.add(sSensorField1);
        line.add(sSensorField2);
        line.add(sSensorField3);
        line.add(sSensorField4);
        modeSingle.add(line);

        line = new JPanel();
        line.add(new JLabel("Protects Signal "));
        line.add(sNextSignalField1);
        line.add(sFlashBox);
        modeSingle.add(line);

        return modeSingle;
    }

    JPanel fillModeTrailMain() {
        modeTrailMain.setLayout(new BoxLayout(modeTrailMain, BoxLayout.Y_AXIS));

        JPanel line = new JPanel();
        line.add(new JLabel("Protects Sensor "));
        line.add(tmSensorField1);
        line.add(tmSensorField2);
        line.add(tmSensorField3);
        line.add(tmSensorField4);
        modeTrailMain.add(line);

        line = new JPanel();
        line.add(new JLabel("Protects Against Turnout "));
        line.add(tmProtectTurnoutField);
        line.add(new JLabel("Thrown"));
        modeTrailMain.add(line);

        line = new JPanel();
        line.add(new JLabel("Protects Signal "));
        line.add(tmNextSignalField1);
        line.add(tmFlashBox);
        modeTrailMain.add(line);

        return modeTrailMain;
    }

    JPanel fillModeTrailDiv() {
        modeTrailDiv.setLayout(new BoxLayout(modeTrailDiv, BoxLayout.Y_AXIS));

        JPanel line = new JPanel();
        line.add(new JLabel("Protects Sensor "));
        line.add(tdSensorField1);
        line.add(tdSensorField2);
        line.add(tdSensorField3);
        line.add(tdSensorField4);
        modeTrailDiv.add(line);

        line = new JPanel();
        line.add(new JLabel("Protects Against Turnout "));
        line.add(tdProtectTurnoutField);
        line.add(new JLabel("Closed"));
        modeTrailDiv.add(line);

        line = new JPanel();
        line.add(new JLabel("Protects Signal "));
        line.add(tdNextSignalField1);
        line.add(tdFlashBox);
        modeTrailDiv.add(line);

        return modeTrailDiv;
    }

    JPanel fillModeFacing() {
        modeFacing.setLayout(new BoxLayout(modeFacing, BoxLayout.Y_AXIS));

        JPanel line = new JPanel();
        line.add(new JLabel("Protects Sensor "));
        line.add(fSensorField1);
        line.add(fSensorField2);
        line.add(fSensorField3);
        line.add(fSensorField4);
        modeFacing.add(line);

        line = new JPanel();
        line.add(new JLabel("Watches Turnout "));
        line.add(fProtectTurnoutField);
        modeFacing.add(line);

        line = new JPanel();
        line.add(new JLabel("To Protect Signal "));
        line.add(fNextSignalField1);
        line.add(new JLabel("When Turnout is Closed"));
         modeFacing.add(line);

        line = new JPanel();
        line.add(new JLabel("And Protect Signal "));
        line.add(fNextSignalField2);
        line.add(new JLabel("When Turnout is Thrown"));
        modeFacing.add(line);

        line = new JPanel();
        line.add(fFlashBox);
        modeFacing.add(line);

        return modeFacing;
    }

    void okPressed() {
        BlockBossLogic b = BlockBossLogic.getStoppedObject(outSignalField.getText());
        if (buttonSingle.isSelected())
            loadSingle(b);
        else if (buttonTrailMain.isSelected())
            loadTrailMain(b);
        else if (buttonTrailDiv.isSelected())
            loadTrailDiv(b);
        else if (buttonFacing.isSelected())
            loadFacing(b);
        else {
            log.error("no button selected?");
            return;
        }
    }

    void loadSingle(BlockBossLogic b) {
        b.setSensor1(sSensorField1.getText());
        b.setSensor2(sSensorField2.getText());
        b.setSensor3(sSensorField3.getText());
        b.setSensor4(sSensorField4.getText());
        b.setMode(BlockBossLogic.SINGLEBLOCK);

        b.setWatchedSignal1(sNextSignalField1.getText(), sFlashBox.isSelected());
        b.retain();
        b.start();
    }

    void loadTrailMain(BlockBossLogic b) {
        b.setSensor1(tmSensorField1.getText());
        b.setSensor2(tmSensorField2.getText());
        b.setSensor3(tmSensorField3.getText());
        b.setSensor4(tmSensorField4.getText());
        b.setMode(BlockBossLogic.TRAILINGMAIN);

        b.setTurnout(tmProtectTurnoutField.getText());

        b.setWatchedSignal1(tmNextSignalField1.getText(), tmFlashBox.isSelected());
        b.retain();
        b.start();
    }
    void loadTrailDiv(BlockBossLogic b) {
        b.setSensor1(tdSensorField1.getText());
        b.setSensor2(tdSensorField2.getText());
        b.setSensor3(tdSensorField3.getText());
        b.setSensor4(tdSensorField4.getText());
        b.setMode(BlockBossLogic.TRAILINGDIVERGING);

        b.setTurnout(tdProtectTurnoutField.getText());

        b.setWatchedSignal1(tdNextSignalField1.getText(), tdFlashBox.isSelected());
        b.retain();
        b.start();
    }
    void loadFacing(BlockBossLogic b) {
        b.setSensor1(fSensorField1.getText());
        b.setSensor2(fSensorField2.getText());
        b.setSensor3(fSensorField3.getText());
        b.setSensor4(fSensorField4.getText());
        b.setMode(BlockBossLogic.FACING);

        b.setTurnout(fProtectTurnoutField.getText());

        b.setWatchedSignal1(fNextSignalField1.getText(), fFlashBox.isSelected());
        b.setWatchedSignal2(fNextSignalField2.getText());
        b.retain();
        b.start();
    }

    void activate() {
        BlockBossLogic b = BlockBossLogic.getExisting(outSignalField.getText());
        if (b==null) return;

        sSensorField1.setText(b.getSensor1());
        sSensorField2.setText(b.getSensor2());
        sSensorField3.setText(b.getSensor3());
        sSensorField4.setText(b.getSensor4());

        tmProtectTurnoutField.setText(b.getTurnout());

        sNextSignalField1.setText(b.getWatchedSignal1());

        fNextSignalField2.setText(b.getWatchedSignal2());

        sFlashBox.setSelected(b.getUseFlash());

        int mode = b.getMode();
        if (mode == BlockBossLogic.SINGLEBLOCK)
            buttonSingle.setSelected(true);
        else if (mode == BlockBossLogic.TRAILINGMAIN)
            buttonTrailMain.setSelected(true);
        else if (mode == BlockBossLogic.TRAILINGDIVERGING)
            buttonTrailDiv.setSelected(true);
        else if (mode == BlockBossLogic.FACING)
            buttonFacing.setSelected(true);

        // do setup of visible panels
        buttonClicked();
    }

    void buttonClicked() {
        modeSingle.setVisible(false);
        modeTrailMain.setVisible(false);
        modeTrailDiv.setVisible(false);
        modeFacing.setVisible(false);
        if (buttonSingle.isSelected())
            modeSingle.setVisible(true);
        else if (buttonTrailMain.isSelected())
            modeTrailMain.setVisible(true);
        else if (buttonTrailDiv.isSelected())
            modeTrailDiv.setVisible(true);
        else if (buttonFacing.isSelected())
            modeFacing.setVisible(true);
        else {
            log.debug("no button selected?");
        }
        modeSingle.validate();
        modeTrailMain.validate();
        modeTrailDiv.validate();
        modeFacing.validate();
        pack();
        modeSingle.repaint();
        modeTrailMain.repaint();
        modeTrailDiv.repaint();
        modeFacing.repaint();
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
        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BlockBossLogic.class.getName());
}

/* @(#)BlockBossFrame.java */
