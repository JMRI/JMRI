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
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.2 $
 */

public class BlockBossFrame extends JFrame {

    JPanel modeSingle               = new JPanel();
    JRadioButton buttonSingle;
    JTextField sSensorField         = new JTextField(6);
    JTextField sNextSignalField1    = new JTextField(6);
    JCheckBox sFlashBox;

    JPanel modeTrailMain                = new JPanel();
    JRadioButton buttonTrailMain;
    JTextField tmSensorField            = new JTextField(6);
    JTextField tmProtectTurnoutField    = new JTextField(6);
    JTextField tmNextSignalField1       = new JTextField(6);
    JCheckBox tmFlashBox;

    JPanel modeTrailDiv                 = new JPanel();
    JRadioButton buttonTrailDiv;
    JTextField tdSensorField            = new JTextField(6);
    JTextField tdProtectTurnoutField    = new JTextField(6);
    JTextField tdNextSignalField1       = new JTextField(6);
    JCheckBox tdFlashBox;

    JPanel modeFacing               = new JPanel();
    JRadioButton buttonFacing;
    JTextField fSensorField         = new JTextField(6);
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

        tmSensorField.setDocument(sSensorField.getDocument());
        tdSensorField.setDocument(sSensorField.getDocument());
        fSensorField.setDocument(sSensorField.getDocument());

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
        line.add(new JLabel("Protects Sensor "));
        line.add(sSensorField);
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
        line.add(tmSensorField);
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
        line.add(tdSensorField);
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
        line.add(fSensorField);
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
        b.setSensor(sSensorField.getText());
        b.setMode(BlockBossLogic.SINGLEBLOCK);

        b.setWatchedSignal1(sNextSignalField1.getText(), sFlashBox.isSelected());
        b.retain();
        b.start();
    }

    void loadTrailMain(BlockBossLogic b) {
        b.setSensor(tmSensorField.getText());
        b.setMode(BlockBossLogic.TRAILINGMAIN);

        b.setTurnout(tmProtectTurnoutField.getText());

        b.setWatchedSignal1(tmNextSignalField1.getText(), tmFlashBox.isSelected());
        b.retain();
        b.start();
    }
    void loadTrailDiv(BlockBossLogic b) {
        b.setSensor(tdSensorField.getText());
        b.setMode(BlockBossLogic.TRAILINGDIVERGING);

        b.setTurnout(tdProtectTurnoutField.getText());

        b.setWatchedSignal1(tdNextSignalField1.getText(), tdFlashBox.isSelected());
        b.retain();
        b.start();
    }
    void loadFacing(BlockBossLogic b) {
        b.setSensor(fSensorField.getText());
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

        sSensorField.setText(b.getSensor());

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
