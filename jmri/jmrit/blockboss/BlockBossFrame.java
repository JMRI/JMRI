// BlockBossFrame.java

package jmri.jmrit.blockboss;

import jmri.InstanceManager;

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
 * @author	Bob Jacobsen    Copyright (C) 2003, 2005
 * @version     $Revision: 1.15 $
 *              Revisions to add facing point sensors, limited speed,
 *              change layout, and tool tips.  Dick Bronson (RJB) 2006
 
*/

public class BlockBossFrame extends JFrame {

    JPanel modeSingle               = new JPanel();
    JRadioButton buttonSingle;
    JTextField sSensorField1        = new JTextField(6);
    JTextField sSensorField2        = new JTextField(6);
    JTextField sSensorField3        = new JTextField(6);
    JTextField sSensorField4        = new JTextField(6);
    JTextField sNextSignalField1    = new JTextField(6);
    JTextField sNextSignalField1Alt = new JTextField(6);
    JCheckBox sLimitBox;
    JCheckBox sFlashBox;
    JCheckBox sDistantBox;

    JPanel modeTrailMain                = new JPanel();
    JRadioButton buttonTrailMain;
    JTextField tmSensorField1           = new JTextField(6);
    JTextField tmSensorField2           = new JTextField(6);
    JTextField tmSensorField3           = new JTextField(6);
    JTextField tmSensorField4           = new JTextField(6);
    JTextField tmProtectTurnoutField    = new JTextField(6);
    JTextField tmNextSignalField1       = new JTextField(6);
    JTextField tmNextSignalField1Alt    = new JTextField(6);
    JCheckBox tmLimitBox;
    JCheckBox tmFlashBox;
    JCheckBox tmDistantBox;

    JPanel modeTrailDiv                 = new JPanel();
    JRadioButton buttonTrailDiv;
    JTextField tdSensorField1           = new JTextField(6);
    JTextField tdSensorField2           = new JTextField(6);
    JTextField tdSensorField3           = new JTextField(6);
    JTextField tdSensorField4           = new JTextField(6);
    JTextField tdProtectTurnoutField    = new JTextField(6);
    JTextField tdNextSignalField1       = new JTextField(6);
    JTextField tdNextSignalField1Alt    = new JTextField(6);
    JCheckBox tdLimitBox;
    JCheckBox tdFlashBox;
    JCheckBox tdDistantBox;

    JPanel modeFacing               = new JPanel();
    JRadioButton buttonFacing;
    JTextField fSensorField1        = new JTextField(6);
    JTextField fSensorField2        = new JTextField(6);
    JTextField fSensorField3        = new JTextField(6);
    JTextField fSensorField4        = new JTextField(6);
    JTextField fProtectTurnoutField = new JTextField(6);
    JTextField fNextSignalField1    = new JTextField(6);
    JTextField fNextSignalField1Alt = new JTextField(6);
    JTextField fNextSignalField2    = new JTextField(6);
    JTextField fNextSignalField2Alt = new JTextField(6);
    JTextField fNextSensorField1    = new JTextField(6);
    JTextField fNextSensorField1Alt = new JTextField(6);
    JTextField fNextSensorField2    = new JTextField(6);
    JTextField fNextSensorField2Alt = new JTextField(6);
    JCheckBox fmLimitBox;
    JCheckBox fdLimitBox;
    JCheckBox fFlashBox;
    JCheckBox fDistantBox;

    JTextField outSignalField;

    
    String buttonSingleTooltip = "In direction of traffic";
    String buttonTrailMainTooltip = "<HTML><BODY>Signal head for main track<BR>" 
        + "through turnout in either direction</BODY></HTML>";
    String buttonTrailDivTooltip = "<HTML><BODY>Signal head for branching track<BR>"
        + "through turnout in either direction</BODY></HTML>";
    String buttonFacingTooltip = "<HTML><BODY>Single signal head on single<BR>"
        + "track facing double track</BODY></HTML>";
    String outSignalFieldTooltip =  "<HTML><BODY>Enter a new signal head number, or<BR>"
        + "enter an existing signal head number<BR>" 
        + "then hit return to load its information.</BODY></HTML>";
    String sensorFieldTooltip =  "Sensor active sets this signal to Red.";            
    String turnoutFieldTooltip = "Enter protected turnout number here.";
    String flashBoxTooltip = "<HTML><BODY>One aspect faster than yellow displays<BR>" 
        + "flashing yellow, rather than green.</BODY></HTML>";
    String limitBoxTooltip = "<HTML><BODY>Limits the fastest aspect displayed<BR>"
        + "to yellow, rather than green.</BODY></HTML>";
    String nextSignalFieldTooltip = "<HTML><BODY>Enter the low speed signal head for this track.<BR>" 
        + "For dual head signals the fastest aspect is protected.</BODY></HTML>";
    String highSignalFieldTooltip = "<HTML><BODY>Enter the high speed signal head for this track.<BR>" 
        + "For dual head signals the fastest aspect is protected.</BODY></HTML>";
   String distantBoxTooltip = "<HTML><BODY>Mirrors the protected (following) signal's status<BR>" 
        + "unless over ridden by an intermediate stop sensor.</BODY></HTML>";
    
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
        fileMenu.add(new jmri.configurexml.SaveMenu());
        setJMenuBar(menuBar);

        // create GUI items
        sLimitBox  = new JCheckBox("Limited Speed");
        tmLimitBox = new JCheckBox("Limited Speed");
        tmLimitBox.setModel(sLimitBox.getModel());
        fmLimitBox  = new JCheckBox("Limited Speed");
        fmLimitBox.setModel(sLimitBox.getModel());

        tdLimitBox = new JCheckBox("Limited Speed");
        fdLimitBox  = new JCheckBox("Limited Speed");
        fdLimitBox.setModel(tdLimitBox.getModel());



        sFlashBox  = new JCheckBox("With Flashing Yellow");
        tmFlashBox = new JCheckBox("With Flashing Yellow");
        tmFlashBox.setModel(sFlashBox.getModel());
        tdFlashBox = new JCheckBox("With Flashing Yellow");
        tdFlashBox.setModel(sFlashBox.getModel());
        fFlashBox  = new JCheckBox("With Flashing Yellow");
        fFlashBox.setModel(sFlashBox.getModel());

        sDistantBox  = new JCheckBox("Is Distant Signal");
        tmDistantBox = new JCheckBox("Is Distant Signal");
        tmDistantBox.setModel(sDistantBox.getModel());
        tdDistantBox = new JCheckBox("Is Distant Signal");
        tdDistantBox.setModel(sDistantBox.getModel());
        fDistantBox  = new JCheckBox("Is Distant Signal");
        fDistantBox.setModel(sDistantBox.getModel());

        buttonSingle = new JRadioButton("On Single Block");
        buttonTrailMain = new JRadioButton("Main Leg of Turnout");
        buttonTrailDiv = new JRadioButton("Diverging Leg of Turnout");
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
        tdNextSignalField1Alt.setDocument(sNextSignalField1Alt.getDocument());
        tmNextSignalField1.setDocument(sNextSignalField1.getDocument());
        tmNextSignalField1Alt.setDocument(sNextSignalField1Alt.getDocument());
        fNextSignalField1.setDocument(sNextSignalField1.getDocument());
        fNextSignalField1Alt.setDocument(sNextSignalField1Alt.getDocument());

        // add top part of GUI, holds signal head name to drive
        JPanel line = new JPanel();
        line.add(new JLabel("Signal Named "));
        line.add(outSignalField= new JTextField(5));
        outSignalField.setToolTipText(outSignalFieldTooltip);
        line.setAlignmentX(1);        
        getContentPane().add(line);
        outSignalField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activate();
            }
        });

        line = new JPanel();
        line.setLayout(new BoxLayout(line, BoxLayout.Y_AXIS));
        buttonSingle.setToolTipText(buttonSingleTooltip);
        line.add(buttonSingle);
        buttonTrailMain.setToolTipText(buttonTrailMainTooltip);
        line.add(buttonTrailMain);
        buttonTrailDiv.setToolTipText(buttonTrailDivTooltip);
        line.add(buttonTrailDiv);
        buttonFacing.setToolTipText(buttonFacingTooltip);
        line.add(buttonFacing);
        line.setAlignmentX(0.5f);
        getContentPane().add(line);

        getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        // fill in the specific panels for the modes
        getContentPane().add(fillModeSingle());
        getContentPane().add(fillModeTrailMain());
        getContentPane().add(fillModeTrailDiv());
        getContentPane().add(fillModeFacing());

        getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        // add OK button at bottom
        JButton b = new JButton("Apply");
        b.setAlignmentX(0.5f);
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

// Panel arrangements all changed to use GridBagLayout format. RJB

    JPanel fillModeSingle() {
        modeSingle.setLayout(new GridBagLayout()); 

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(2, 3, 2, 3); // top, left, bottom, right
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 0;
        insets.top = 9;
        insets.bottom = 9;

        modeSingle.add(new JLabel("Protects Sensor/s"), constraints);        
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        sSensorField1.setToolTipText(sensorFieldTooltip);
        modeSingle.add(sSensorField1, constraints);
        constraints.gridx = 2;
        sSensorField2.setToolTipText(sensorFieldTooltip);
        modeSingle.add(sSensorField2, constraints);
        constraints.gridx = 3;
        sSensorField3.setToolTipText(sensorFieldTooltip);
        modeSingle.add(sSensorField3, constraints);
        constraints.gridx = 4;
        sSensorField4.setToolTipText(sensorFieldTooltip);
        modeSingle.add(sSensorField4, constraints);

        insets.top = 2;
        constraints.gridx = 0;
        constraints.gridy = 1;       
        constraints.fill = GridBagConstraints.NONE;

        modeSingle.add(new JLabel("Protects Signal"), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        sNextSignalField1.setToolTipText(highSignalFieldTooltip);
        modeSingle.add(sNextSignalField1, constraints);
        constraints.gridx = 2;
        sNextSignalField1Alt.setToolTipText(nextSignalFieldTooltip);
        modeSingle.add(sNextSignalField1Alt, constraints);
        constraints.gridx = 3;
        constraints.gridwidth = 2;
        sLimitBox.setToolTipText(limitBoxTooltip);
        modeSingle.add(sLimitBox, constraints);

        constraints.gridy = 6;
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        insets.bottom = 9;
        sFlashBox.setToolTipText(flashBoxTooltip);
        modeSingle.add(sFlashBox, constraints);

        constraints.gridx = 3;
        sDistantBox.setToolTipText(distantBoxTooltip);
        modeSingle.add(sDistantBox, constraints);
        return modeSingle;
    }

    JPanel fillModeTrailMain() {
        modeTrailMain.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(2, 3, 2, 3); // top, left, bottom, right
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 0;
        insets.top = 9;
        insets.bottom = 9;
        modeTrailMain.add(new JLabel("Protects Sensor/s"), constraints);        
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        tmSensorField1.setToolTipText(sensorFieldTooltip);
        modeTrailMain.add(tmSensorField1, constraints);
        constraints.gridx = 2;
        tmSensorField2.setToolTipText(sensorFieldTooltip);
        modeTrailMain.add(tmSensorField2, constraints);
        constraints.gridx = 3;
        tmSensorField3.setToolTipText(sensorFieldTooltip);
        modeTrailMain.add(tmSensorField3, constraints);
        constraints.gridx = 4;
        tmSensorField4.setToolTipText(sensorFieldTooltip);
        modeTrailMain.add(tmSensorField4, constraints);

        insets.top = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 1;
        insets.bottom = 9;
        modeTrailMain.add(new JLabel("Red When Turnout"), constraints);        
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        tmProtectTurnoutField.setToolTipText(turnoutFieldTooltip);
        modeTrailMain.add(tmProtectTurnoutField, constraints);
        constraints.gridx = 2;
        constraints.gridwidth = 2;
        modeTrailMain.add(new JLabel("Is "+InstanceManager.turnoutManagerInstance().getThrownText()), constraints);
        constraints.gridwidth = 1;
        
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 4;
        insets.bottom = 2;
        modeTrailMain.add(new JLabel("Protects Signal"), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        tmNextSignalField1.setToolTipText(highSignalFieldTooltip);
        modeTrailMain.add(tmNextSignalField1, constraints);
        constraints.gridx = 2;
        tmNextSignalField1Alt.setToolTipText(nextSignalFieldTooltip);
        modeTrailMain.add(tmNextSignalField1Alt, constraints);
        constraints.gridx = 3;
        constraints.gridwidth = 2;
        tmLimitBox.setToolTipText(limitBoxTooltip);
        modeTrailMain.add(tmLimitBox, constraints);

        constraints.gridy = 6;
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        insets.bottom = 9;
        tmFlashBox.setToolTipText(flashBoxTooltip);
        modeTrailMain.add(tmFlashBox, constraints);

        constraints.gridx = 3;
        tmDistantBox.setToolTipText(distantBoxTooltip);
        modeTrailMain.add(tmDistantBox, constraints);
        return modeTrailMain;
    }

    JPanel fillModeTrailDiv() {
        modeTrailDiv.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(2, 3, 2, 3); // top, left, bottom, right
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 0;
        insets.top = 9;
        insets.bottom = 9;
        modeTrailDiv.add(new JLabel("Protects Sensor/s"), constraints);        
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        tdSensorField1.setToolTipText(sensorFieldTooltip);
        modeTrailDiv.add(tdSensorField1, constraints);
        constraints.gridx = 2;
        tdSensorField2.setToolTipText(sensorFieldTooltip);
        modeTrailDiv.add(tdSensorField2, constraints);
        constraints.gridx = 3;
        tdSensorField3.setToolTipText(sensorFieldTooltip);
        modeTrailDiv.add(tdSensorField3, constraints);
        constraints.gridx = 4;
        tdSensorField4.setToolTipText(sensorFieldTooltip);
        modeTrailDiv.add(tdSensorField4, constraints);

        insets.top = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 1;
        insets.bottom = 9;
        modeTrailDiv.add(new JLabel("Red When Turnout"), constraints);        
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        tdProtectTurnoutField.setToolTipText(turnoutFieldTooltip);
        modeTrailDiv.add(tdProtectTurnoutField, constraints);
        constraints.gridx = 2;
        constraints.gridwidth = 2;
        modeTrailDiv.add(new JLabel("Is "+InstanceManager.turnoutManagerInstance().getClosedText()), constraints);
        constraints.gridwidth = 1;
        
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 4;
        insets.bottom = 2;
        modeTrailDiv.add(new JLabel("Protects Signal"), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        tdNextSignalField1.setToolTipText(highSignalFieldTooltip);
        modeTrailDiv.add(tdNextSignalField1, constraints);
        constraints.gridx = 2;
        tdNextSignalField1Alt.setToolTipText(nextSignalFieldTooltip);
        modeTrailDiv.add(tdNextSignalField1Alt, constraints);
        constraints.gridx = 3;
        constraints.gridwidth = 2;
        tdLimitBox.setToolTipText(limitBoxTooltip);
        modeTrailDiv.add(tdLimitBox, constraints);

        constraints.gridy = 6;
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        insets.bottom = 9;
        tdFlashBox.setToolTipText(flashBoxTooltip);
        modeTrailDiv.add(tdFlashBox, constraints);

        constraints.gridx = 3;
        tdDistantBox.setToolTipText(distantBoxTooltip);
        modeTrailDiv.add(tdDistantBox, constraints);

        return modeTrailDiv;
    }

    JPanel fillModeFacing() {
        modeFacing.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(2, 3, 2, 3); // top, left, bottom, right
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 0;
        insets.top = 9;
        insets.bottom = 9;
        modeFacing.add(new JLabel("Protects Sensor/s"), constraints);        
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        fSensorField1.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fSensorField1, constraints);
        constraints.gridx = 2;
        fSensorField2.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fSensorField2, constraints);
        constraints.gridx = 3;
        fSensorField3.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fSensorField3, constraints);
        constraints.gridx = 4;
        fSensorField4.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fSensorField4, constraints);

        insets.top = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 1;
        insets.bottom = 9;
        modeFacing.add(new JLabel("Watches Turnout"), constraints);        
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        fProtectTurnoutField.setToolTipText(turnoutFieldTooltip);
        modeFacing.add(fProtectTurnoutField, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 2;
        insets.bottom = 2;
        modeFacing.add(new JLabel("To Protect Signal"), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        fNextSignalField1.setToolTipText(highSignalFieldTooltip);
        modeFacing.add(fNextSignalField1, constraints);
        constraints.gridx = 2;
        fNextSignalField1Alt.setToolTipText(nextSignalFieldTooltip);
        modeFacing.add(fNextSignalField1Alt, constraints);
        constraints.gridx = 3;
        constraints.gridwidth = 2;
        fmLimitBox.setToolTipText(limitBoxTooltip);
        modeFacing.add(fmLimitBox, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 3;
        insets.bottom = 9;
        modeFacing.add(new JLabel("And Sensor/s"), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        fNextSensorField1.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fNextSensorField1, constraints);
        constraints.gridx = 2;
        fNextSensorField1Alt.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fNextSensorField1Alt, constraints);
        constraints.gridx = 3;
        constraints.gridwidth = 2;
        modeFacing.add(new JLabel("When Turnout is "+InstanceManager.turnoutManagerInstance().getClosedText()), constraints);
        constraints.gridwidth = 1;

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 4;
        insets.bottom = 2;
        modeFacing.add(new JLabel("And To Protect Signal"), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        fNextSignalField2.setToolTipText(highSignalFieldTooltip);
        modeFacing.add(fNextSignalField2, constraints);
        constraints.gridx = 2;
        fNextSignalField2Alt.setToolTipText(nextSignalFieldTooltip);
        modeFacing.add(fNextSignalField2Alt, constraints);
        constraints.gridx = 3;
        constraints.gridwidth = 2;
        fdLimitBox.setToolTipText(limitBoxTooltip);
        modeFacing.add(fdLimitBox, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 5;
        insets.bottom = 9;
        modeFacing.add(new JLabel("And Sensor/s"), constraints);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        fNextSensorField2.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fNextSensorField2, constraints);
        constraints.gridx = 2;
        fNextSensorField2Alt.setToolTipText(sensorFieldTooltip);
        modeFacing.add(fNextSensorField2Alt, constraints);
        constraints.gridx = 3;
        constraints.gridwidth = 2;
        modeFacing.add(new JLabel("When Turnout is "+InstanceManager.turnoutManagerInstance().getThrownText()), constraints);
        constraints.gridwidth = 1;

        constraints.gridy = 6;
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        insets.bottom = 9;
        fFlashBox.setToolTipText(flashBoxTooltip);
        modeFacing.add(fFlashBox, constraints);

        constraints.gridx = 3;
        fDistantBox.setToolTipText(distantBoxTooltip);
        modeFacing.add(fDistantBox, constraints);
        
        return modeFacing;
    }

    void okPressed() {
        // check signal head exists
        if (InstanceManager.signalHeadManagerInstance().getSignalHead(outSignalField.getText())==null) {
            setTitle("Simple Signal Logic");
            JOptionPane.showMessageDialog(this,"Signal head \""+outSignalField.getText()+"\" is not defined yet");
            return;
        }

        // it does
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
        b.setWatchedSignal1Alt(sNextSignalField1Alt.getText());
        b.setLimitSpeed1(sLimitBox.isSelected());
        b.setDistantSignal(sDistantBox.isSelected());
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
        b.setWatchedSignal1Alt(tmNextSignalField1Alt.getText());
        b.setLimitSpeed1(tmLimitBox.isSelected());
        b.setDistantSignal(tmDistantBox.isSelected());
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
        b.setWatchedSignal1Alt(tdNextSignalField1Alt.getText());
        b.setLimitSpeed2(tdLimitBox.isSelected());
        b.setDistantSignal(tdDistantBox.isSelected());
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
        b.setWatchedSignal1Alt(fNextSignalField1Alt.getText());
        b.setWatchedSignal2(fNextSignalField2.getText());
        b.setWatchedSignal2Alt(fNextSignalField2Alt.getText());
        b.setWatchedSensor1(fNextSensorField1.getText());
        b.setWatchedSensor1Alt(fNextSensorField1Alt.getText());
        b.setWatchedSensor2(fNextSensorField2.getText());
        b.setWatchedSensor2Alt(fNextSensorField2Alt.getText());
        b.setLimitSpeed1(fmLimitBox.isSelected());
        b.setLimitSpeed2(fdLimitBox.isSelected());
        
        b.setDistantSignal(fDistantBox.isSelected());
        b.retain();
        b.start();
    }

    void activate() {
        
        // check signal head exists
        if (InstanceManager.signalHeadManagerInstance().getSignalHead(outSignalField.getText())==null) {
            setTitle("Simple Signal Logic");
            return;
        }
        
        // find existing logic  
        BlockBossLogic b = BlockBossLogic.getExisting(outSignalField.getText());
        if (b==null) {
            setTitle("Simple Signal Logic");
            return;
        }
        
        setTitle("Signal logic for "+outSignalField.getText());

        sSensorField1.setText(b.getSensor1());
        sSensorField2.setText(b.getSensor2());
        sSensorField3.setText(b.getSensor3());
        sSensorField4.setText(b.getSensor4());

        tmProtectTurnoutField.setText(b.getTurnout());

        sNextSignalField1.setText(b.getWatchedSignal1());
        sNextSignalField1Alt.setText(b.getWatchedSignal1Alt());

        fNextSignalField2.setText(b.getWatchedSignal2());
        fNextSignalField2Alt.setText(b.getWatchedSignal2Alt());

        fNextSensorField1.setText(b.getWatchedSensor1());
        fNextSensorField1Alt.setText(b.getWatchedSensor1Alt());
        fNextSensorField2.setText(b.getWatchedSensor2());
        fNextSensorField2Alt.setText(b.getWatchedSensor2Alt());

        sLimitBox.setSelected(b.getLimitSpeed1());
        tdLimitBox.setSelected(b.getLimitSpeed2());
        sFlashBox.setSelected(b.getUseFlash());
        sDistantBox.setSelected(b.getDistantSignal());

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

    /**
     * Programmatically open the frame to edit a specific signal
     */
    public void setSignal(String name) {
        outSignalField.setText(name);
    }
    
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
