package jmri.jmrit.display.layoutEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import jmri.Block;
import jmri.BlockManager;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalManager;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.NamedBean.DisplayOptions;
import jmri.Path;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastLogicManager;
import jmri.SignalMastManager;
import jmri.Turnout;
import jmri.implementation.DefaultConditionalAction;
import jmri.jmrit.blockboss.BlockBossLogic;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PositionableIcon;
import jmri.jmrit.display.SensorIcon;
import jmri.jmrit.display.SignalHeadIcon;
import jmri.jmrit.display.SignalMastIcon;
import jmri.jmrit.signalling.SignallingGuiTools;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;
import jmri.util.swing.JComboBoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Layout Editor Tools provides tools making use of layout connectivity
 * available in Layout Editor panels.
 * <p>
 * The tools in this module are accessed via the Tools menu in Layout Editor.
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2019
 */
public class LayoutEditorTools {

    //constants
    //private final int NONE = 0;  //Signal at Turnout Positions
    //operational instance variables shared between tools
    private LayoutEditor layoutEditor = null;
    private MultiIconEditor signalIconEditor = null;
    private JFrame signalFrame = null;
    private boolean needRedraw = false;
    private BlockBossLogic logic = null;
    private SignalHead auxSignal = null;

    //constructor method
    public LayoutEditorTools(@Nonnull LayoutEditor thePanel) {
        layoutEditor = thePanel;

        //Turnouts
        LayoutEditor.setupComboBox(sensorsTurnoutComboBox, true, true, false);
        LayoutEditor.setupComboBox(signalMastsTurnoutComboBox, true, true, false);
        LayoutEditor.setupComboBox(turnout1ComboBox, true, true, false);
        LayoutEditor.setupComboBox(turnout2ComboBox, true, true, false);
        LayoutEditor.setupComboBox(turnoutAComboBox, true, true, false);
        LayoutEditor.setupComboBox(turnoutBComboBox, true, true, false);
        LayoutEditor.setupComboBox(turnoutComboBox, true, true, false);

        //Blocks
        LayoutEditor.setupComboBox(block1IDComboBox, true, true, false);
        LayoutEditor.setupComboBox(block2IDComboBox, true, true, false);
        LayoutEditor.setupComboBox(blockACComboBox, true, true, false);
        LayoutEditor.setupComboBox(blockBDComboBox, true, true, false);
        LayoutEditor.setupComboBox(slipSensorsBlockAComboBox, true, true, false);
        LayoutEditor.setupComboBox(slipSensorsBlockBComboBox, true, true, false);
        LayoutEditor.setupComboBox(slipSensorsBlockCComboBox, true, true, false);
        LayoutEditor.setupComboBox(slipSensorsBlockDComboBox, true, true, false);
        LayoutEditor.setupComboBox(slipSignalBlockAComboBox, true, true, false);
        LayoutEditor.setupComboBox(slipSignalBlockBComboBox, true, true, false);
        LayoutEditor.setupComboBox(slipSignalBlockCComboBox, true, true, false);
        LayoutEditor.setupComboBox(slipSignalBlockDComboBox, true, true, false);
        LayoutEditor.setupComboBox(xingBlockACComboBox, true, true, false);
        LayoutEditor.setupComboBox(xingBlockBDComboBox, true, true, false);
        LayoutEditor.setupComboBox(xingSensorsBlockACComboBox, true, true, false);
        LayoutEditor.setupComboBox(xingSensorsBlockBDComboBox, true, true, false);

        //Signal Heads
        LayoutEditor.setupComboBox(a1_3WaySignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(a1SignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(a1SlipSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(a1TToTSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(a2_3WaySignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(a2SignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(a2SlipSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(a2TToTSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(a3_3WaySignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(aSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(b_3WaySignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(b1SignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(b1SlipSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(b1TToTSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(b2SignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(b2SlipSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(b2TToTSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(bSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(c_3WaySignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(c1SignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(c1SlipSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(c1TToTSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(c2SignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(c2SlipSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(c2TToTSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(continuingSignalHeadComboBox, false, true, false);
        LayoutEditor.setupComboBox(cSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(d_3WaySignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(d1SignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(d1SlipSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(d1TToTSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(d2SignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(d2SlipSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(d2TToTSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(divergingSignalHeadComboBox, false, true, false);
        LayoutEditor.setupComboBox(dSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(eastBoundSignalHeadComboBox, true, true, false);
        LayoutEditor.setupComboBox(throatContinuingSignalHeadComboBox, false, true, false);
        LayoutEditor.setupComboBox(throatDivergingSignalHeadComboBox, false, true, false);
        LayoutEditor.setupComboBox(westBoundSignalHeadComboBox, true, true, false);

        //TODO: Set combobox exclude lists for turnouts, blocks and signal heads
        //that are not part of the current layout
    }

    /*=====================*\
    |* setSignalsAtTurnout *|
    \*=====================*/
    /**
     * Tool to set signals at a turnout, including placing the signal icons and
     * optionally setup of Simple Signal Logic for each signal head
     * <p>
     * This tool assumes left facing signal head icons have been selected, and
     * will rotate the signal head icons accordingly.
     * <p>
     * This tool will place throat icons on the right side of the track, and
     * continuing and diverging icons on the outside edge of the turnout.
     */
    //operational variables for Set Signals at Turnout tool
    private JmriJFrame setSignalsAtTurnoutFrame = null;
    private boolean setSignalsAtTurnoutOpenFlag = false;
    private boolean setSignalsAtTurnoutFromMenuFlag = false;

    private JLabel turnoutNameLabel = null;

    private final NamedBeanComboBox<Turnout> turnoutComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(),
            null, DisplayOptions.DISPLAYNAME);

    private final NamedBeanComboBox<SignalHead> throatContinuingSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> throatDivergingSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> continuingSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> divergingSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);

    private final JCheckBox setPlaceAllHeads = new JCheckBox(Bundle.getMessage("PlaceAllHeads"));
    private final JCheckBox setupAllLogic = new JCheckBox(Bundle.getMessage("SetAllLogic"));

    private final JCheckBox setThroatContinuing = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupLogicThroatContinuing = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setThroatDiverging = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupLogicThroatDiverging = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setContinuing = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupLogicContinuing = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setDiverging = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupLogicDiverging = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JButton getSavedSignalHeads = null;
    private JButton changeSignalIcon = null;
    private JButton setSignalsDone = null;
    private JButton setSignalsCancel = null;

    private LayoutTurnout layoutTurnout = null;
    private double placeSignalDirectionDEG = 0.0;

    private Turnout turnout = null;
    private SignalHead throatContinuingHead = null;
    private SignalHead throatDivergingHead = null;
    private SignalHead continuingHead = null;
    private SignalHead divergingHead = null;

    //display dialog for Set Signals at Turnout tool
    public void setSignalsAtTurnoutFromMenu(@Nonnull LayoutTurnout to,
            @Nonnull MultiIconEditor theEditor, @Nonnull JFrame theFrame) {
        layoutTurnout = to;
        turnout = to.getTurnout();
        turnoutComboBox.setSelectedItem(to.getTurnout());
        setSignalsAtTurnoutFromMenuFlag = true;
        setSignalsAtTurnout(theEditor, theFrame);
        setSignalsAtTurnoutFromMenuFlag = false;
    }

    public void setSignalsAtTurnout(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        signalIconEditor = theEditor;
        signalFrame = theFrame;

        //Initialize if needed
        if (setSignalsAtTurnoutFrame == null) {
            setSignalsAtTurnoutOpenFlag = false;
            setSignalsAtTurnoutFrame = new JmriJFrame(Bundle.getMessage("SignalsAtTurnout"), false, true);
            oneFrameToRuleThemAll(setSignalsAtTurnoutFrame);
            setSignalsAtTurnoutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            setSignalsAtTurnoutFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtTurnout", true);
            setSignalsAtTurnoutFrame.setLocation(70, 30);
            Container theContentPane = setSignalsAtTurnoutFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel1 = new JPanel(new FlowLayout());
            turnoutNameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout")));
            panel1.add(turnoutNameLabel);
            panel1.add(turnoutComboBox);
            turnoutNameLabel.setLabelFor(turnoutComboBox);
            turnoutComboBox.setToolTipText(Bundle.getMessage("SignalsTurnoutNameHint"));
            theContentPane.add(panel1);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel2 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("SignalHeads"));
            panel2.add(shTitle);
            panel2.add(new JLabel("	  "));
            panel2.add(getSavedSignalHeads = new JButton(Bundle.getMessage("GetSaved")));
            getSavedSignalHeads.addActionListener((ActionEvent e) -> {
                turnoutSignalsGetSaved(e);
            });
            getSavedSignalHeads.setToolTipText(Bundle.getMessage("GetSavedHint"));
            theContentPane.add(panel2);

            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2a = new JPanel(new FlowLayout());
            panel2a.add(new JLabel("   "));
            panel2a.add(setPlaceAllHeads);
            setPlaceAllHeads.setToolTipText(Bundle.getMessage("PlaceAllHeadsHint"));
            setPlaceAllHeads.addActionListener((ActionEvent e) -> {
                boolean isSelected = setPlaceAllHeads.isSelected();
                //(de)select all checkboxes
                setThroatContinuing.setSelected(isSelected);
                setThroatDiverging.setSelected(isSelected);
                setContinuing.setSelected(isSelected);
                setDiverging.setSelected(isSelected);
            });
            panel2a.add(new JLabel("  "));
            panel2a.add(setupAllLogic);
            setupAllLogic.setToolTipText(Bundle.getMessage("SetAllLogicHint"));
            setupAllLogic.addActionListener((ActionEvent e) -> {
                boolean isSelected = setupAllLogic.isSelected();
                //(de)select all checkboxes
                setupLogicThroatContinuing.setSelected(isSelected);
                setupLogicThroatDiverging.setSelected(isSelected);
                setupLogicContinuing.setSelected(isSelected);
                setupLogicDiverging.setSelected(isSelected);
            });
            theContentPane.add(panel2a);

            JPanel panel21 = new JPanel(new FlowLayout());
            JLabel throatContinuingLabel = new JLabel(
                    Bundle.getMessage("MakeLabel", throatContinuingString));
            panel21.add(throatContinuingLabel);
            panel21.add(throatContinuingSignalHeadComboBox);
            throatContinuingLabel.setLabelFor(throatContinuingSignalHeadComboBox);
            theContentPane.add(panel21);
            throatContinuingSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel22 = new JPanel(new FlowLayout());
            panel22.add(new JLabel("   "));
            panel22.add(setThroatContinuing);
            setThroatContinuing.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel22.add(new JLabel("  "));
            panel22.add(setupLogicThroatContinuing);
            setupLogicThroatContinuing.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel22);

            JPanel panel31 = new JPanel(new FlowLayout());
            JLabel throatDivergingLabel = new JLabel(
                    Bundle.getMessage("MakeLabel", throatDivergingString));
            panel31.add(throatDivergingLabel);
            panel31.add(throatDivergingSignalHeadComboBox);
            throatDivergingLabel.setLabelFor(throatDivergingSignalHeadComboBox);
            theContentPane.add(panel31);
            throatDivergingSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel32 = new JPanel(new FlowLayout());
            panel32.add(new JLabel("   "));
            panel32.add(setThroatDiverging);
            setThroatDiverging.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel32.add(new JLabel("  "));
            panel32.add(setupLogicThroatDiverging);
            setupLogicThroatDiverging.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel32);

            JPanel panel41 = new JPanel(new FlowLayout());
            JLabel continuingLabel = new JLabel(
                    Bundle.getMessage("MakeLabel", continuingString));
            panel41.add(continuingLabel);
            panel41.add(continuingSignalHeadComboBox);
            continuingLabel.setLabelFor(continuingSignalHeadComboBox);
            theContentPane.add(panel41);
            continuingSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel42 = new JPanel(new FlowLayout());
            panel42.add(new JLabel("   "));
            panel42.add(setContinuing);
            setContinuing.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel42.add(new JLabel("  "));
            panel42.add(setupLogicContinuing);
            setupLogicContinuing.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel42);

            JPanel panel51 = new JPanel(new FlowLayout());
            JLabel divergingLabel = new JLabel(
                    Bundle.getMessage("MakeLabel", divergingString));
            panel51.add(divergingLabel);
            panel51.add(divergingSignalHeadComboBox);
            divergingLabel.setLabelFor(divergingSignalHeadComboBox);
            theContentPane.add(panel51);
            divergingSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel52 = new JPanel(new FlowLayout());
            panel52.add(new JLabel("   "));
            panel52.add(setDiverging);
            setDiverging.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel52.add(new JLabel("  "));
            panel52.add(setupLogicDiverging);
            setupLogicDiverging.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel52);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(changeSignalIcon = new JButton(Bundle.getMessage("ChangeSignalIcon")));
            changeSignalIcon.addActionListener((ActionEvent e) -> {
                signalFrame.setVisible(true);
            });
            changeSignalIcon.setToolTipText(Bundle.getMessage("ChangeSignalIconHint"));
            panel6.add(new JLabel("	 "));
            panel6.add(setSignalsDone = new JButton(Bundle.getMessage("ButtonDone")));
            setSignalsDone.addActionListener((ActionEvent e) -> {
                setSignalsDonePressed(e);
            });
            setSignalsDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            panel6.add(setSignalsCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setSignalsCancel.addActionListener((ActionEvent e) -> {
                setSignalsCancelPressed(e);
            });
            setSignalsCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
            setSignalsAtTurnoutFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSignalsCancelPressed(null);
                }
            });
        }
        setPlaceAllHeads.setSelected(false);
        setupAllLogic.setSelected(false);

        turnoutComboBox.setVisible(!setSignalsAtTurnoutFromMenuFlag);
        String turnoutLabelString = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"));
        if (setSignalsAtTurnoutFromMenuFlag) {
            turnoutNameLabel.setText(turnoutLabelString + layoutTurnout.getTurnoutName());
            turnoutSignalsGetSaved(null);
        } else {
            turnoutNameLabel.setText(turnoutLabelString);
        }

        if (!setSignalsAtTurnoutOpenFlag) {
            setSignalsAtTurnoutFrame.setPreferredSize(null);
            setSignalsAtTurnoutFrame.pack();
            setSignalsAtTurnoutOpenFlag = true;
        }
        setSignalsAtTurnoutFrame.setVisible(true);
    }   //setSignalsAtTurnout

    private void turnoutSignalsGetSaved(ActionEvent a) {
        if (getTurnoutInformation(false)) {
            throatContinuingSignalHeadComboBox.setSelectedItem(layoutTurnout.getSignalA1());
            throatDivergingSignalHeadComboBox.setSelectedItem(layoutTurnout.getSignalA2());
            continuingSignalHeadComboBox.setSelectedItem(layoutTurnout.getSignalB1());
            divergingSignalHeadComboBox.setSelectedItem(layoutTurnout.getSignalC1());
        }
    }

    private void setSignalsCancelPressed(ActionEvent a) {
        setSignalsAtTurnoutOpenFlag = false;
        setSignalsAtTurnoutFrame.setVisible(false);
    }

    private void setSignalsDonePressed(ActionEvent a) {
        //process turnout name
        if (!getTurnoutInformation(false)) {
            return;
        }
        //process signal head names
        if (!getTurnoutSignalHeadInformation()) {
            return;
        }
        //place signals as requested
        String signalHeadName = throatContinuingSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setThroatContinuing.isSelected()) {
            if (isHeadOnPanel(throatContinuingHead)
                    && (throatContinuingHead != getHeadFromName(layoutTurnout.getSignalA1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
                placeThroatContinuing();
                removeAssignment(throatContinuingHead);
                layoutTurnout.setSignalA1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(throatContinuingHead, layoutTurnout);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(throatContinuingHead)
                        && isHeadAssignedAnywhere(throatContinuingHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
                    removeAssignment(throatContinuingHead);
                    layoutTurnout.setSignalA1Name(signalHeadName);
                }
                //} else if (assigned != A1) {
                //TODO: need to figure out what to do in this case
                //assigned to a different position on the same turnout.
                //}
            }
        }
        signalHeadName = throatDivergingSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if ((setThroatDiverging.isSelected()) && (throatDivergingHead != null)) {
            if (isHeadOnPanel(throatDivergingHead)
                    && (throatDivergingHead != getHeadFromName(layoutTurnout.getSignalA2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
                placeThroatDiverging();
                removeAssignment(throatDivergingHead);
                layoutTurnout.setSignalA2Name(signalHeadName);
                needRedraw = true;
            }
        } else if (throatDivergingHead != null) {
            int assigned = isHeadAssignedHere(throatDivergingHead, layoutTurnout);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(throatDivergingHead)
                        && isHeadAssignedAnywhere(throatDivergingHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
                    removeAssignment(throatDivergingHead);
                    layoutTurnout.setSignalA2Name(signalHeadName);
                }
                //} else if (assigned != A2) {
                //need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        } else {   //throatDivergingHead is always null here
            removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
            layoutTurnout.setSignalA2Name("");
        }

        signalHeadName = continuingSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setContinuing.isSelected()) {
            if (isHeadOnPanel(continuingHead)
                    && (continuingHead != getHeadFromName(layoutTurnout.getSignalB1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
                if (layoutTurnout.getContinuingSense() == Turnout.CLOSED) {
                    placeContinuing(signalHeadName);
                } else {
                    placeDiverging(signalHeadName);
                }
                removeAssignment(continuingHead);
                layoutTurnout.setSignalB1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(continuingHead, layoutTurnout);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(continuingHead)
                        && isHeadAssignedAnywhere(continuingHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
                    removeAssignment(continuingHead);
                    layoutTurnout.setSignalB1Name(signalHeadName);
                }
                //} else if (assigned != B1) {
                //need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }

        signalHeadName = divergingSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setDiverging.isSelected()) {
            if (isHeadOnPanel(divergingHead)
                    && (divergingHead != getHeadFromName(layoutTurnout.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
                if (layoutTurnout.getContinuingSense() == Turnout.CLOSED) {
                    placeDiverging(signalHeadName);
                } else {
                    placeContinuing(signalHeadName);
                }
                removeAssignment(divergingHead);
                layoutTurnout.setSignalC1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(divergingHead, layoutTurnout);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(divergingHead)
                        && isHeadAssignedAnywhere(divergingHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
                    removeAssignment(divergingHead);
                    layoutTurnout.setSignalC1Name(signalHeadName);
                }
                //} else if (assigned != C1) {
                //need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }
        //setup Logic if requested and enough information is available
        if (setupLogicThroatContinuing.isSelected()) {
            setLogicThroatContinuing();
        }
        if ((throatDivergingHead != null) && setupLogicThroatDiverging.isSelected()) {
            setLogicThroatDiverging();
        }
        if (setupLogicContinuing.isSelected()) {
            setLogicContinuing();
        }
        if (setupLogicDiverging.isSelected()) {
            setLogicDiverging();
        }
        //make sure this layout turnout is not linked to another
        layoutTurnout.setLinkType(LayoutTurnout.NO_LINK);
        layoutTurnout.setLinkedTurnoutName("");
        //finish up
        setSignalsAtTurnoutOpenFlag = false;
        setSignalsAtTurnoutFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            needRedraw = false;
        }
    }   //setSignalsDonePressed

    private boolean getTurnoutInformation(boolean isCrossover) {
        String str = "";
        if (isCrossover ? !setSignalsAtXoverTurnoutFromMenuFlag : !setSignalsAtTurnoutFromMenuFlag) {
            turnout = null;
            layoutTurnout = null;
            str = isCrossover ? NamedBean.normalizeUserName(xoverTurnoutName)
                    : turnoutComboBox.getSelectedItemDisplayName();
            if ((str == null) || str.isEmpty()) {
                JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                        Bundle.getMessage("SignalsError1"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            turnout = InstanceManager.turnoutManagerInstance().getTurnout(str);
            if (turnout == null) {
                JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                        Bundle.getMessage("SignalsError2",
                                new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            } else {
                String uname = turnout.getUserName();
                if ((uname == null) || uname.isEmpty() || !uname.equals(str)) {
                    if (isCrossover) {
                        xoverTurnoutName = str;
                    } else {
                        turnoutComboBox.setSelectedItem(turnout);
                    }
                }
            }
            for (LayoutTurnout t : layoutEditor.getLayoutTurnouts()) {
                if (t.getTurnout() == turnout) {
                    layoutTurnout = t;
                    if (t.isTurnoutTypeXover() != isCrossover) {
                        if (isCrossover) {
                            JOptionPane.showMessageDialog(layoutEditor,
                                    Bundle.getMessage("InfoMessage8"),
                                    Bundle.getMessage("MessageTitle"),
                                    JOptionPane.INFORMATION_MESSAGE);
                            setXoverSignalsCancelPressed(null);
                        } else {
                            JOptionPane.showMessageDialog(layoutEditor,
                                    Bundle.getMessage("InfoMessage1"),
                                    Bundle.getMessage("MessageTitle"),
                                    JOptionPane.INFORMATION_MESSAGE);
                            setSignalsCancelPressed(null);
                        }
                        return false;
                    }
                    break;
                }
            }
        }

        if (layoutTurnout != null) {
            Point2D coordsA = layoutTurnout.getCoordsA(), coords2;
            if (isCrossover) {
                coords2 = layoutTurnout.getCoordsB();
            } else {
                coords2 = layoutTurnout.getCoordsCenter();
            }
            placeSignalDirectionDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coords2, coordsA));
            return true;
        }
        JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                Bundle.getMessage("SignalsError3",
                        new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
        return false;
    }   //getTurnoutInformation

    private boolean getTurnoutSignalHeadInformation() {
        throatContinuingHead = getSignalHeadFromEntry(throatContinuingSignalHeadComboBox, true, setSignalsAtTurnoutFrame);
        if (throatContinuingHead == null) {
            return false;
        }
        throatDivergingHead = getSignalHeadFromEntry(throatDivergingSignalHeadComboBox, false, setSignalsAtTurnoutFrame);
        continuingHead = getSignalHeadFromEntry(continuingSignalHeadComboBox, true, setSignalsAtTurnoutFrame);
        if (continuingHead == null) {
            return false;
        }
        divergingHead = getSignalHeadFromEntry(divergingSignalHeadComboBox, true, setSignalsAtTurnoutFrame);
        if (divergingHead == null) {
            return false;
        }
        return true;
    }
    private NamedIcon testIcon = null;

    private void placeThroatContinuing() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = throatContinuingSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsA = layoutTurnout.getCoordsA();
        Point2D delta = new Point2D.Double(+shift, +shift);

        delta = MathUtil.rotateDEG(delta, placeSignalDirectionDEG);
        Point2D where = MathUtil.add(coordsA, delta);
        setSignalHeadOnPanel(placeSignalDirectionDEG + 180.0, signalHeadName, where);
    }

    private void placeThroatDiverging() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = throatDivergingSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsA = layoutTurnout.getCoordsA();
        Point2D delta = new Point2D.Double(-shift, +shift);

        delta = MathUtil.rotateDEG(delta, placeSignalDirectionDEG);
        Point2D where = MathUtil.add(coordsA, delta);
        setSignalHeadOnPanel(placeSignalDirectionDEG + 180.0, signalHeadName, where);
    }

    private void placeContinuing(@Nonnull String signalHeadName) {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutTurnout.getCoordsB();
        Point2D coordsC = layoutTurnout.getCoordsC();
        Point2D coordsCenter = layoutTurnout.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        double cDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsC, coordsCenter));
        double diffDirDEG = MathUtil.diffAngleDEG(cDirDEG, bDirDEG);
        double shiftX = 0.0;
        if (diffDirDEG < 0.0) {
            shiftX += shift * Math.cos(Math.toRadians(diffDirDEG));
        }
        Point2D delta = new Point2D.Double(shiftX, -shift);

        delta = MathUtil.rotateDEG(delta, bDirDEG);
        Point2D where = MathUtil.add(coordsB, delta);
        setSignalHeadOnPanel(bDirDEG, signalHeadName, where);
    }

    private void placeDiverging(String signalHeadName) {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutTurnout.getCoordsB();
        Point2D coordsC = layoutTurnout.getCoordsC();
        Point2D coordsCenter = layoutTurnout.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        double cDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsC, coordsCenter));
        double diffDirDEG = MathUtil.diffAngleDEG(cDirDEG, bDirDEG);
        double shiftX = 0.0;
        if (diffDirDEG >= 0.0) {
            shiftX += shift * Math.cos(Math.toRadians(diffDirDEG));
        }
        Point2D delta = new Point2D.Double(shiftX, -shift);

        delta = MathUtil.rotateDEG(delta, cDirDEG);
        Point2D where = MathUtil.add(coordsC, delta);
        setSignalHeadOnPanel(cDirDEG, signalHeadName, where);
    }

    private void setLogicThroatContinuing() {
        TrackSegment track = null;
        if (layoutTurnout.getContinuingSense() == Turnout.CLOSED) {
            track = (TrackSegment) layoutTurnout.getConnectB();
        } else {
            track = (TrackSegment) layoutTurnout.getConnectC();
        }
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String signalHeadName = throatContinuingSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        SignalHead nextHead = getNextSignalFromObject(track,
                layoutTurnout, signalHeadName, setSignalsAtTurnoutFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (throatDivergingHead != null) {
            if (!initializeBlockBossLogic(signalHeadName)) {
                return;
            }
            logic.setMode(BlockBossLogic.TRAILINGMAIN);
            logic.setTurnout(turnout.getSystemName());
            logic.setSensor1(occupancy.getSystemName());
            if (nextHead != null) {
                logic.setWatchedSignal1(nextHead.getSystemName(), false);
            }
            if (auxSignal != null) {
                logic.setWatchedSignal1Alt(auxSignal.getSystemName());
            }
            finalizeBlockBossLogic();
            return;
        }
        SignalHead savedAuxSignal = auxSignal;
        TrackSegment track2 = null;
        if (layoutTurnout.getContinuingSense() == Turnout.CLOSED) {
            track2 = (TrackSegment) layoutTurnout.getConnectC();
        } else {
            track2 = (TrackSegment) layoutTurnout.getConnectB();
        }
        if (track2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block2 = track2.getLayoutBlock();
        if (block2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy2 = block2.getOccupancySensor();
        if (occupancy2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block2.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        signalHeadName = throatContinuingSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        SignalHead nextHead2 = getNextSignalFromObject(track2,
                layoutTurnout, signalHeadName, setSignalsAtTurnoutFrame);
        if ((nextHead2 == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{block2.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!initializeBlockBossLogic(signalHeadName)) {
            return;
        }
        logic.setMode(BlockBossLogic.FACING);
        logic.setTurnout(turnout.getSystemName());
        logic.setWatchedSensor1(occupancy.getSystemName());
        logic.setWatchedSensor2(occupancy2.getSystemName());
        if (nextHead != null) {
            logic.setWatchedSignal1(nextHead.getSystemName(), false);
        }
        if (savedAuxSignal != null) {
            logic.setWatchedSignal1Alt(savedAuxSignal.getSystemName());
        }
        if (nextHead2 != null) {
            logic.setWatchedSignal2(nextHead2.getSystemName());
        }
        if (auxSignal != null) {
            logic.setWatchedSignal2Alt(auxSignal.getSystemName());
        }
        if (!layoutTurnout.isMainlineC()) {
            logic.setLimitSpeed2(true);
        }
        finalizeBlockBossLogic();
    }   //setLogicThroatContinuing

    private void setLogicThroatDiverging() {
        TrackSegment track = null;
        if (layoutTurnout.getContinuingSense() == Turnout.CLOSED) {
            track = (TrackSegment) layoutTurnout.getConnectC();
        } else {
            track = (TrackSegment) layoutTurnout.getConnectB();
        }
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = throatDivergingSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        SignalHead nextHead = getNextSignalFromObject(track,
                layoutTurnout, signalHeadName, setSignalsAtTurnoutFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!initializeBlockBossLogic(signalHeadName)) {
            return;
        }

        logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
        logic.setTurnout(turnout.getSystemName());
        logic.setSensor1(occupancy.getSystemName());
        if (nextHead != null) {
            logic.setWatchedSignal1(nextHead.getSystemName(), false);
        }
        if (auxSignal != null) {
            logic.setWatchedSignal1Alt(auxSignal.getSystemName());
        }
        if (!layoutTurnout.isMainlineC()) {
            logic.setLimitSpeed2(true);
        }
        finalizeBlockBossLogic();
    }   //setLogicThroatDiverging

    private void setLogicContinuing() {
        TrackSegment track = (TrackSegment) layoutTurnout.getConnectA();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = continuingSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        SignalHead nextHead = getNextSignalFromObject(track,
                layoutTurnout, signalHeadName, setSignalsAtTurnoutFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!initializeBlockBossLogic(signalHeadName)) {
            return;
        }
        logic.setMode(BlockBossLogic.TRAILINGMAIN);
        logic.setTurnout(turnout.getSystemName());
        logic.setSensor1(occupancy.getSystemName());
        if (nextHead != null) {
            logic.setWatchedSignal1(nextHead.getSystemName(), false);
        }
        if (auxSignal != null) {
            logic.setWatchedSignal1Alt(auxSignal.getSystemName());
        }
        finalizeBlockBossLogic();
    }   //setLogicContinuing

    private void setLogicDiverging() {
        TrackSegment track = (TrackSegment) layoutTurnout.getConnectA();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = divergingSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        SignalHead nextHead = getNextSignalFromObject(track,
                layoutTurnout, signalHeadName, setSignalsAtTurnoutFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!initializeBlockBossLogic(signalHeadName)) {
            return;
        }
        logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
        logic.setTurnout(turnout.getSystemName());
        logic.setSensor1(occupancy.getSystemName());
        if (nextHead != null) {
            logic.setWatchedSignal1(nextHead.getSystemName(), false);
        }
        if (auxSignal != null) {
            logic.setWatchedSignal1Alt(auxSignal.getSystemName());
        }
        if (!layoutTurnout.isMainlineC()) {
            logic.setLimitSpeed2(true);
        }
        finalizeBlockBossLogic();
    }   //setLogicDiverging

    /*==========================================*\
    | * Utility routines used by multiple tools *|
    \*==========================================*/
    /**
     * Returns the layout turnout corresponding to a given turnout. If require
     * double crossover is requested, and error message is sent to the user if
     * the layout turnout is not a double crossover, and null is returned. If a
     * layout turnout corresponding to the turnout is not found, an error
     * message is sent to the user and null is returned.
     */
    @CheckReturnValue
    public LayoutTurnout getLayoutTurnoutFromTurnout(
            @Nonnull Turnout turnout,
            boolean requireDoubleXover,
            @Nonnull String str,
            @CheckForNull JFrame theFrame) {
        for (LayoutTurnout t : layoutEditor.getLayoutTurnouts()) {
            if (t.getTurnout() == turnout) {
                //have the layout turnout corresponding to the turnout
                if ((t.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)
                        && (!requireDoubleXover)) {
                    JOptionPane.showMessageDialog(theFrame,
                            Bundle.getMessage("InfoMessage1"),
                            Bundle.getMessage("MessageTitle"),
                            JOptionPane.INFORMATION_MESSAGE);
                    return null;
                }
                if (requireDoubleXover && (t.getTurnoutType() != LayoutTurnout.DOUBLE_XOVER)) {
                    JOptionPane.showMessageDialog(theFrame,
                            Bundle.getMessage("InfoMessage8"),
                            Bundle.getMessage("MessageTitle"),
                            JOptionPane.INFORMATION_MESSAGE);
                    return null;
                }
                return t;
            }
        }
        //layout turnout not found
        JOptionPane.showMessageDialog(theFrame,
                Bundle.getMessage("SignalsError3",
                        new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
        return null;
    }

    /**
     * Returns the SignalHead corresponding to an entry field in the specified
     * dialog. This also takes care of UpperCase and trimming of leading and
     * trailing blanks. If entry is required, and no entry is present, and error
     * message is sent. An error message also results if a signal head with the
     * entered name is not found in the SignalTable.
     */
    @CheckReturnValue
    public SignalHead getSignalHeadFromEntry(
            @Nonnull NamedBeanComboBox<SignalHead> signalNameComboBox,
            boolean requireEntry,
            @Nonnull JmriJFrame frame) {
        String signalName = signalNameComboBox.getSelectedItemDisplayName();
        SignalHead result = getSignalHeadFromEntry(signalName, requireEntry, frame);
        if (result != null) {
            String uname = result.getUserName();
            if ((uname == null) || uname.isEmpty() || !uname.equals(signalName)) {
                signalNameComboBox.setSelectedItem(result);
            }
        }
        return result;
    }

    @CheckReturnValue
    public SignalHead getSignalHeadFromEntry(
            @Nonnull JTextField signalNameTextField,
            boolean requireEntry, @Nonnull JmriJFrame frame) {
        String signalName = NamedBean.normalizeUserName(signalNameTextField.getText());
        SignalHead result = getSignalHeadFromEntry(signalName, requireEntry, frame);
        if (result != null) {
            String uname = result.getUserName();
            if ((uname == null) || uname.isEmpty() || !uname.equals(signalName)) {
                signalNameTextField.setText(signalName);
            }
        }
        return result;
    }

    @CheckReturnValue
    public SignalHead getSignalHeadFromEntry(@CheckForNull String signalName,
            boolean requireEntry, @Nonnull JmriJFrame frame) {
        if ((signalName == null) || signalName.isEmpty()) {
            if (requireEntry) {
                JOptionPane.showMessageDialog(frame, Bundle.getMessage("SignalsError5"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }
        SignalHead head = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
        if (head == null) {
            JOptionPane.showMessageDialog(frame,
                    Bundle.getMessage("SignalsError4",
                            new Object[]{signalName}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return (head);
    }

    /**
     * Returns a SignalHead given a name
     */
    @CheckReturnValue
    public SignalHead getHeadFromName(@CheckForNull String str) {
        SignalHead result = null;
        if ((str != null) && !str.isEmpty()) {
            result = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(str);
        }
        return result;
    }

    /**
     * Places a signal head icon on the panel after rotation at the designated
     * place, with all icons taken care of.
     *
     * @deprecated since 4.11.6, use
     * {@link #setSignalHeadOnPanel(double, String, int, int)} directly.
     */
    @Deprecated
    public void setSignalHeadOnPanel(int rotation,
            @Nonnull String signalHeadName,
            int xLoc, int yLoc) {
        setSignalHeadOnPanel((double) rotation, signalHeadName, xLoc, yLoc);
    }

    /**
     * Places a signal head icon on the panel after rotation at the designated
     * place, with all icons taken care of.
     *
     * @param directionDEG   rotation in degrees.
     * @param signalHeadName name of a signal head.
     * @param where          coordinates for placing signal head on panel.
     */
    public void setSignalHeadOnPanel(double directionDEG,
            @Nonnull String signalHeadName,
            @Nonnull Point2D where) {
        setSignalHeadOnPanel(directionDEG, signalHeadName, (int) where.getX(), (int) where.getY());
    }

    /**
     * Places a signal head icon on the panel after rotation at the designated
     * place, with all icons taken care of.
     *
     * @param directionDEG   rotation in degrees.
     * @param signalHeadName name of a signal head.
     * @param xLoc           x coordinate for placing signal head on panel.
     * @param yLoc           y coordinate for placing signal head on panel.
     */
    public void setSignalHeadOnPanel(double directionDEG, @Nonnull String signalHeadName, int xLoc, int yLoc) {
        SignalHeadIcon l = getSignalHeadIcon(signalHeadName);

        if (directionDEG > 0) {
            Iterator<String> e = l.getIconStateNames();
            while (e.hasNext()) {
                l.getIcon(e.next()).rotate((int) directionDEG, l);
            }
        }

        l.setLocation(xLoc - (int) (l.maxWidth() / 2.0), yLoc - (int) (l.maxHeight() / 2.0));

        layoutEditor.putSignal(l);
    }

    /**
     * Returns an index if the specified signal head is assigned to the
     * LayoutTurnout initialized. Otherwise returns the NONE index. The index
     * specifies the turnout position of the signal head according to the code
     * listed at the beginning of this module.
     */
    private int isHeadAssignedHere(@Nonnull SignalHead head, @Nonnull LayoutTurnout lTurnout) {
        int result = LayoutTurnout.NONE;

        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put(lTurnout.getSignalA1Name(), LayoutTurnout.POINTA1);
        map.put(lTurnout.getSignalA2Name(), LayoutTurnout.POINTA2);
        map.put(lTurnout.getSignalA3Name(), LayoutTurnout.POINTA3);
        map.put(lTurnout.getSignalB1Name(), LayoutTurnout.POINTB1);
        map.put(lTurnout.getSignalB2Name(), LayoutTurnout.POINTB2);
        map.put(lTurnout.getSignalC1Name(), LayoutTurnout.POINTC1);
        map.put(lTurnout.getSignalC2Name(), LayoutTurnout.POINTC2);
        map.put(lTurnout.getSignalD1Name(), LayoutTurnout.POINTD1);
        map.put(lTurnout.getSignalD2Name(), LayoutTurnout.POINTD2);

        String sName = head.getSystemName();
        String uName = head.getUserName();

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String signalName = entry.getKey();

            if (!signalName.isEmpty() && (signalName.equals(sName) || signalName.equals(uName))) {
                result = entry.getValue();
                break;
            }
        }

        return result;
    }   //isHeadAssignedHere

    /**
     * Returns true if an icon for the specified SignalHead is on the panel
     */
    public boolean isHeadOnPanel(@Nonnull SignalHead head) {
        for (SignalHeadIcon h : layoutEditor.signalList) {
            if (h.getSignalHead() == head) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the specified Signal Head is assigned to an object on the
     * panel, regardless of whether an icon is displayed or not
     */
    public boolean isHeadAssignedAnywhere(@Nonnull SignalHead head) {
        String sName = head.getSystemName();
        String uName = head.getUserName();

        for (LayoutTurnout to : layoutEditor.getLayoutTurnouts()) {
            if (isHeadAssignedHere(head, to) != LayoutTurnout.NONE) {
                return true;
            }
        }

        for (PositionablePoint po : layoutEditor.getPositionablePoints()) {
            if ((po.getEastBoundSignal().equals(sName) || ((uName != null)
                    && (po.getEastBoundSignal().equals(uName))))) {
                return true;
            }
            if ((po.getWestBoundSignal().equals(sName) || ((uName != null)
                    && (po.getWestBoundSignal().equals(uName))))) {
                return true;
            }
        }

        for (LevelXing x : layoutEditor.getLevelXings()) {
            if ((x.getSignalAName() != null)
                    && (x.getSignalAName().equals(sName) || ((uName != null)
                    && (x.getSignalAName().equals(uName))))) {
                return true;
            }
            if ((x.getSignalBName() != null)
                    && (x.getSignalBName().equals(sName) || ((uName != null)
                    && (x.getSignalBName().equals(uName))))) {
                return true;
            }
            if ((x.getSignalCName() != null)
                    && (x.getSignalCName().equals(sName) || ((uName != null)
                    && (x.getSignalCName().equals(uName))))) {
                return true;
            }
            if ((x.getSignalDName() != null)
                    && (x.getSignalDName().equals(sName) || ((uName != null)
                    && (x.getSignalDName().equals(uName))))) {
                return true;
            }
        }
        return false;
    }   //isHeadAssignedAnywhere

    /**
     * Removes the assignment of the specified SignalHead to either a turnout, a
     * positionable point, or a level crossing wherever it is assigned
     */
    public void removeAssignment(@Nonnull SignalHead head) {
        String sName = head.getSystemName();
        String uName = head.getUserName();
        for (LayoutTurnout to : layoutEditor.getLayoutTurnouts()) {
            if ((to.getSignalA1Name().equals(sName) || ((uName != null)
                    && to.getSignalA1Name().equals(uName)))) {
                to.setSignalA1Name("");
            }
            if ((to.getSignalA2Name().equals(sName) || ((uName != null)
                    && to.getSignalA2Name().equals(uName)))) {
                to.setSignalA2Name("");
            }
            if ((to.getSignalA3Name().equals(sName) || ((uName != null)
                    && to.getSignalA3Name().equals(uName)))) {
                to.setSignalA3Name("");
            }
            if ((to.getSignalB1Name().equals(sName) || ((uName != null)
                    && to.getSignalB1Name().equals(uName)))) {
                to.setSignalB1Name("");
            }
            if ((to.getSignalB2Name().equals(sName) || ((uName != null)
                    && to.getSignalB2Name().equals(uName)))) {
                to.setSignalB2Name("");
            }
            if ((to.getSignalC1Name().equals(sName) || ((uName != null)
                    && to.getSignalC1Name().equals(uName)))) {
                to.setSignalC1Name("");
            }
            if ((to.getSignalC2Name().equals(sName) || ((uName != null)
                    && to.getSignalC2Name().equals(uName)))) {
                to.setSignalC2Name("");
            }
            if ((to.getSignalD1Name().equals(sName) || ((uName != null)
                    && to.getSignalD1Name().equals(uName)))) {
                to.setSignalD1Name("");
            }
            if ((to.getSignalD2Name().equals(sName) || ((uName != null)
                    && to.getSignalD2Name().equals(uName)))) {
                to.setSignalD2Name("");
            }
        }
        for (PositionablePoint po : layoutEditor.getPositionablePoints()) {
            if (po.getEastBoundSignal().equals(sName) || po.getEastBoundSignal().equals(uName)) {
                po.setEastBoundSignal("");
            }
            if (po.getWestBoundSignal().equals(sName) || po.getWestBoundSignal().equals(uName)) {
                po.setWestBoundSignal("");
            }
        }
        for (LevelXing x : layoutEditor.getLevelXings()) {
            if ((x.getSignalAName() != null)
                    && (x.getSignalAName().equals(sName) || ((uName != null)
                    && (x.getSignalAName().equals(uName))))) {
                x.setSignalAName("");
            }
            if ((x.getSignalBName() != null)
                    && (x.getSignalBName().equals(sName) || ((uName != null)
                    && (x.getSignalBName().equals(uName))))) {
                x.setSignalBName("");
            }
            if ((x.getSignalCName() != null)
                    && (x.getSignalCName().equals(sName) || ((uName != null)
                    && (x.getSignalCName().equals(uName))))) {
                x.setSignalCName("");
            }
            if ((x.getSignalDName() != null)
                    && (x.getSignalDName().equals(sName) || ((uName != null)
                    && (x.getSignalDName().equals(uName))))) {
                x.setSignalDName("");
            }
        }
    }   //removeAssignment

    /**
     * Removes the SignalHead with the specified name from the panel and from
     * assignment to any turnout, positionable point, or level crossing
     */
    public void removeSignalHeadFromPanel(@CheckForNull String signalName) {
        if ((signalName == null) || signalName.isEmpty()) {
            return;
        }
        SignalHead head = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
        if (head != null) {
            removeAssignment(head);
            layoutEditor.removeSignalHead(head);
        }
    }

    /*
	 * Initializes a BlockBossLogic for creation of a signal logic for the signal
	 *	head named in "signalHeadName".
	 * Should not be called until enough informmation has been gathered to allow
	 *	configuration of the Simple Signal Logic.
     */
    public boolean initializeBlockBossLogic(@Nonnull String signalHeadName) {
        logic = BlockBossLogic.getStoppedObject(signalHeadName);
        return true;
    }

    /*
	 * Finalizes a successfully created signal logic
     */
    public void finalizeBlockBossLogic() {
        if (logic == null) {
            return;
        }
        logic.retain();
        logic.start();
        logic = null;
    }

    /*
	 * Returns the signal head at the end of the block "track" is assigned to.
	 *	"track" is the Track Segment leaving "object".
	 *	"object" must be either an anchor point or one of the connecting
	 *	 points of a turnout or level crossing.
	 * Note: returns 'null' is signal is not present where it is expected, or
	 *	if an End Bumper is reached. To test for end bumper, use the
	 *		associated routine "reachedEndBumper()". Reaching a turntable ray
	 *	track connection is considered reaching an end bumper.
	 * Note: Normally this routine requires a signal at any turnout it finds.
	 *	However, if 'skipIncludedTurnout' is true, this routine will skip
	 *	over an absent signal at an included turnout, that is a turnout
	 *	with its throat track segment and its continuing track segment in
	 *	the same block. When this happens, the user is warned.
     */
    @CheckReturnValue
    public SignalHead getNextSignalFromObject(@Nonnull TrackSegment track,
            @Nonnull Object object,
            @Nonnull String signalHeadName, @Nonnull JmriJFrame frame) {
        hitEndBumper = false;
        auxSignal = null;
        TrackSegment t = track;
        Object obj = object;
        boolean inBlock = true;
        int type = 0;
        Object connect = null;
        while (inBlock) {
            if (t.getConnect1() == obj) {
                type = t.getType2();
                connect = t.getConnect2();
            } else {
                type = t.getType1();
                connect = t.getConnect1();
            }
            if (type == LayoutTrack.POS_POINT) {
                PositionablePoint p = (PositionablePoint) connect;
                if (p.getType() == PositionablePoint.END_BUMPER) {
                    hitEndBumper = true;
                    return null;
                }
                if (p.getConnect1() == t) {
                    t = p.getConnect2();
                } else {
                    t = p.getConnect1();
                }
                if (t == null) {
                    return null;
                }
                if (track.getLayoutBlock() != t.getLayoutBlock()) {
                    //p is a block boundary - should be signalled
                    String signalName;
                    if (isAtWestEndOfAnchor(t, p)) {
                        signalName = p.getWestBoundSignal();
                    } else {
                        signalName = p.getEastBoundSignal();
                    }
                    if (signalName.isEmpty()) {
                        return null;
                    }
                    return InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
                obj = p;
            } else if (type == LayoutTrack.TURNOUT_A) {
                //Reached turnout throat, should be signalled
                LayoutTurnout to = (LayoutTurnout) connect;
                String signalName = to.getSignalA2Name();
                if (!signalName.isEmpty()) {
                    auxSignal = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
                signalName = to.getSignalA1Name();
                if (signalName.isEmpty()) {
                    if (!layoutEditor.isIncludedTurnoutSkipped()) {
                        return null;
                    }
                    t = getContinuingTrack(to, type);
                    if ((t == null) || (track.getLayoutBlock() != t.getLayoutBlock())) {
                        return null;
                    }
                    warnOfSkippedTurnout(frame, to.getTurnoutName(), signalHeadName);
                    obj = to;
                } else {
                    return InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
            } else if (type == LayoutTrack.TURNOUT_B) {
                //Reached turnout continuing, should be signalled
                LayoutTurnout to = (LayoutTurnout) connect;
                String signalName = to.getSignalB2Name();
                if (to.getContinuingSense() == Turnout.THROWN) {
                    signalName = to.getSignalC2Name();
                }
                if (!signalName.isEmpty()) {
                    auxSignal = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
                if (to.getContinuingSense() == Turnout.CLOSED) {
                    signalName = to.getSignalB1Name();
                } else {
                    signalName = to.getSignalC1Name();
                }
                if (signalName.isEmpty()) {
                    if (!layoutEditor.isIncludedTurnoutSkipped()) {
                        return null;
                    }
                    t = getContinuingTrack(to, type);
                    if ((t == null) || (track.getLayoutBlock() != t.getLayoutBlock())) {
                        return null;
                    }
                    warnOfSkippedTurnout(frame, to.getTurnoutName(), signalHeadName);
                    obj = to;
                } else {
                    return InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
            } else if (type == LayoutTrack.TURNOUT_C) {
                //Reached turnout diverging, should be signalled
                LayoutTurnout to = (LayoutTurnout) connect;
                String signalName = to.getSignalC2Name();
                if (to.getContinuingSense() == Turnout.THROWN) {
                    signalName = to.getSignalB2Name();
                }
                if (!signalName.isEmpty()) {
                    auxSignal = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
                if (to.getContinuingSense() == Turnout.CLOSED) {
                    signalName = to.getSignalC1Name();
                } else {
                    signalName = to.getSignalB1Name();
                }
                if (signalName.isEmpty()) {
                    if (!layoutEditor.isIncludedTurnoutSkipped()) {
                        return null;
                    }
                    t = getContinuingTrack(to, type);
                    if ((t == null) || (track.getLayoutBlock() != t.getLayoutBlock())) {
                        return null;
                    }
                    warnOfSkippedTurnout(frame, to.getTurnoutName(), signalHeadName);
                    obj = to;
                } else {
                    return InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
            } else if (type == LayoutTrack.TURNOUT_D) {
                //Reached turnout xover 4, should be signalled
                LayoutTurnout to = (LayoutTurnout) connect;
                String signalName = to.getSignalD2Name();
                if (!signalName.isEmpty()) {
                    auxSignal = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
                signalName = to.getSignalD1Name();
                if (signalName.isEmpty()) {
                    if (!layoutEditor.isIncludedTurnoutSkipped()) {
                        return null;
                    }
                    t = getContinuingTrack(to, type);
                    if ((t == null) || (track.getLayoutBlock() != t.getLayoutBlock())) {
                        return null;
                    }
                    warnOfSkippedTurnout(frame, to.getTurnoutName(), signalHeadName);
                    obj = to;
                } else {
                    return InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
            } else if (type == LayoutTrack.LEVEL_XING_A) {
                //Reached level crossing that may or may not be a block boundary
                LevelXing x = (LevelXing) connect;
                String signalName = x.getSignalAName();
                if ((signalName != null) && !signalName.isEmpty()) {
                    return InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
                t = (TrackSegment) x.getConnectC();
                if (t == null) {
                    return null;
                }
                if (track.getLayoutBlock() != t.getLayoutBlock()) {
                    return null;
                }
                obj = x;
            } else if (type == LayoutTrack.LEVEL_XING_B) {
                //Reached level crossing that may or may not be a block boundary
                LevelXing x = (LevelXing) connect;
                String signalName = x.getSignalBName();
                if ((signalName != null) && !signalName.isEmpty()) {
                    return InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
                t = (TrackSegment) x.getConnectD();
                if (t == null) {
                    return null;
                }
                if (track.getLayoutBlock() != t.getLayoutBlock()) {
                    return null;
                }
                obj = x;
            } else if (type == LayoutTrack.LEVEL_XING_C) {
                //Reached level crossing that may or may not be a block boundary
                LevelXing x = (LevelXing) connect;
                String signalName = x.getSignalCName();
                if ((signalName != null) && !signalName.isEmpty()) {
                    return InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
                t = (TrackSegment) x.getConnectA();
                if (t == null) {
                    return null;
                }
                if (track.getLayoutBlock() != t.getLayoutBlock()) {
                    return null;
                }
                obj = x;
            } else if (type == LayoutTrack.LEVEL_XING_D) {
                //Reached level crossing that may or may not be a block boundary
                LevelXing x = (LevelXing) connect;
                String signalName = x.getSignalDName();
                if ((signalName != null) && !signalName.isEmpty()) {
                    return InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
                t = (TrackSegment) x.getConnectB();
                if (t == null) {
                    return null;
                }
                if (track.getLayoutBlock() != t.getLayoutBlock()) {
                    return null;
                }
                obj = x;
            } else if (type == LayoutTrack.SLIP_A) {
                LayoutSlip sl = (LayoutSlip) connect;
                String signalName = sl.getSignalA2Name();
                if (!signalName.isEmpty()) {
                    auxSignal = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
                signalName = sl.getSignalA1Name();
                if (signalName.isEmpty()) {
                    if (!layoutEditor.isIncludedTurnoutSkipped()) {
                        return null;
                    }
                    t = getContinuingTrack(sl, type);
                    if ((t == null) || (track.getLayoutBlock() != t.getLayoutBlock())) {
                        return null;
                    }
                    warnOfSkippedTurnout(frame, sl.getTurnoutName(), signalHeadName);
                    obj = sl;
                } else {
                    return InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
            } else if (type == LayoutTrack.SLIP_B) {
                LayoutSlip sl = (LayoutSlip) connect;
                String signalName;
                if (sl.getTurnoutType() == LayoutSlip.DOUBLE_SLIP) {
                    signalName = sl.getSignalB2Name();
                    if (!signalName.isEmpty()) {
                        auxSignal = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                    }
                }
                signalName = sl.getSignalB1Name();
                if (signalName.isEmpty()) {
                    if (!layoutEditor.isIncludedTurnoutSkipped()) {
                        return null;
                    }
                    t = getContinuingTrack(sl, type);
                    if ((t == null) || (track.getLayoutBlock() != t.getLayoutBlock())) {
                        return null;
                    }
                    warnOfSkippedTurnout(frame, sl.getTurnoutName(), signalHeadName);
                    obj = sl;
                } else {
                    return InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
            } else if (type == LayoutTrack.SLIP_C) {
                LayoutSlip sl = (LayoutSlip) connect;
                String signalName;
                if (sl.getTurnoutType() == LayoutSlip.DOUBLE_SLIP) {
                    signalName = sl.getSignalC2Name();
                    if (!signalName.isEmpty()) {
                        auxSignal = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                    }
                }
                signalName = sl.getSignalC1Name();
                if (signalName.isEmpty()) {
                    if (!layoutEditor.isIncludedTurnoutSkipped()) {
                        return null;
                    }
                    t = getContinuingTrack(sl, type);
                    if ((t == null) || (track.getLayoutBlock() != t.getLayoutBlock())) {
                        return null;
                    }
                    warnOfSkippedTurnout(frame, sl.getTurnoutName(), signalHeadName);
                    obj = sl;
                } else {
                    return InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
            } else if (type == LayoutTrack.SLIP_D) {
                LayoutSlip sl = (LayoutSlip) connect;
                String signalName = sl.getSignalD2Name();
                if (!signalName.isEmpty()) {
                    auxSignal = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
                signalName = sl.getSignalD1Name();
                if (signalName.isEmpty()) {
                    if (!layoutEditor.isIncludedTurnoutSkipped()) {
                        return null;
                    }
                    t = getContinuingTrack(sl, type);
                    if ((t == null) || (track.getLayoutBlock() != t.getLayoutBlock())) {
                        return null;
                    }
                    warnOfSkippedTurnout(frame, sl.getTurnoutName(), signalHeadName);
                    obj = sl;
                } else {
                    return InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
            } else if (type >= LayoutTrack.TURNTABLE_RAY_OFFSET) {
                hitEndBumper = true;
                return null;
            }
        }
        return null;
    }   //getNextSignalFromObject

    private boolean hitEndBumper = false;

    private void warnOfSkippedTurnout(
            @Nonnull JFrame frame,
            @Nonnull String turnoutName,
            @Nonnull String signalHeadName) {
        JOptionPane.showMessageDialog(frame,
                Bundle.getMessage("SignalsWarn2",
                        new Object[]{turnoutName, signalHeadName}),
                Bundle.getMessage("WarningTitle"),
                JOptionPane.WARNING_MESSAGE);
    }

    @CheckReturnValue
    private TrackSegment getContinuingTrack(@Nonnull LayoutTurnout to, int type) {
        int ty = to.getTurnoutType();
        if ((ty == LayoutTurnout.RH_TURNOUT) || (ty == LayoutTurnout.LH_TURNOUT)) {
            if (type == LayoutTrack.TURNOUT_A) {
                if (to.getContinuingSense() == Turnout.CLOSED) {
                    return (TrackSegment) to.getConnectB();
                } else {
                    return (TrackSegment) to.getConnectC();
                }
            } else {
                return (TrackSegment) to.getConnectA();
            }
        } else if ((ty == LayoutTurnout.DOUBLE_XOVER) || (ty == LayoutTurnout.RH_XOVER)
                || (ty == LayoutTurnout.LH_XOVER)) {
            if (type == LayoutTrack.TURNOUT_A) {
                return (TrackSegment) to.getConnectB();
            } else if (type == LayoutTrack.TURNOUT_B) {
                return (TrackSegment) to.getConnectA();
            } else if (type == LayoutTrack.TURNOUT_C) {
                return (TrackSegment) to.getConnectD();
            } else if (type == LayoutTrack.TURNOUT_D) {
                return (TrackSegment) to.getConnectC();
            }
        }
        log.error("Bad connection type around turnout " + to.getTurnoutName());
        return null;
    }

    /*
	 * Returns 'true' if an end bumper was reached during the last call to
	 *	GetNextSignalFromObject. Also used in the odd case of reaching a
	 *	turntable ray track connection, which is treated as an end
	 *	bumper here.
     */
    public boolean reachedEndBumper() {
        return hitEndBumper;
    }

    /*
	 * Returns 'true' if "track" enters a block boundary at the west(north) end of
	 *	"point". Returns "false" otherwise. If track is neither horizontal or
	 *		vertical, assumes horizontal, as done when setting signals at block boundary.
	 * "track" is a TrackSegment connected to "point".
	 *	"point" is an anchor point serving as a block boundary.
     */
    public static boolean isAtWestEndOfAnchor(TrackSegment t, PositionablePoint p) {
        if (p.getType() == PositionablePoint.EDGE_CONNECTOR) {
            if (p.getConnect1() == t) {
                if (p.getConnect1Dir() == Path.NORTH || p.getConnect1Dir() == Path.WEST) {
                    return false;
                }
                return true;
            } else {
                if (p.getConnect1Dir() == Path.NORTH || p.getConnect1Dir() == Path.WEST) {
                    return true;
                }
                return false;
            }

        }
        TrackSegment tx = null;
        if (p.getConnect1() == t) {
            tx = p.getConnect2();
        } else if (p.getConnect2() == t) {
            tx = p.getConnect1();
        } else {
            log.error("track not connected to anchor point");
            return false;
        }

        Point2D coords1;
        if (t.getConnect1() == p) {
            coords1 = LayoutEditor.getCoords(t.getConnect2(), t.getType2());
        } else {
            coords1 = LayoutEditor.getCoords(t.getConnect1(), t.getType1());
        }

        Point2D coords2;
        if (tx != null) {
            if (tx.getConnect1() == p) {
                coords2 = LayoutEditor.getCoords(tx.getConnect2(), tx.getType2());
            } else {
                coords2 = LayoutEditor.getCoords(tx.getConnect1(), tx.getType1());
            }
        } else {
            if (t.getConnect1() == p) {
                coords2 = LayoutEditor.getCoords(t.getConnect1(), t.getType1());
            } else {
                coords2 = LayoutEditor.getCoords(t.getConnect2(), t.getType2());
            }
        }

        double delX = coords1.getX() - coords2.getX();
        double delY = coords1.getY() - coords2.getY();
        if (Math.abs(delX) > 2.0 * Math.abs(delY)) {
            //track is primarily horizontal
            if (delX > 0.0) {
                return false;
            } else {
                return true;
            }
        } else if (Math.abs(delY) > 2.0 * Math.abs(delX)) {
            //track is primarily vertical
            if (delY > 0.0) {
                return false;
            } else {
                return true;
            }
        }
        //track is not primarily horizontal or vertical; assume horizontal
        //	log.error ("Track is not vertical or horizontal at anchor");
        if (delX > 0.0) {
            return false;
        }
        return true;
    }   //isAtWestEndOfAnchor

    /*===========================*\
    |* setSignalsAtBlockBoundary *|
    \*===========================*/
    /**
     * Tool to set signals at a block boundary, including placing the signal
     * icons and setup of Simple Signal Logic for each signal head
     * <p>
     * Block boundary must be at an Anchor Point on the LayoutEditor panel.
     */
    //operational variables for Set Signals at Block Boundary tool
    private JmriJFrame setSignalsAtBlockBoundaryFrame = null;
    private boolean setSignalsAtBlockBoundaryOpenFlag = false;
    private boolean setSignalsAtBlockBoundaryFromMenuFlag = false;

    private JLabel block1NameLabel = null;
    private JLabel block2NameLabel = null;

    private final NamedBeanComboBox<Block> block1IDComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Block> block2IDComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class),
            null, DisplayOptions.DISPLAYNAME);

    private final NamedBeanComboBox<SignalHead> eastBoundSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> westBoundSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);

    private final JCheckBox setEastBound = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupLogicEastBound = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setWestBound = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupLogicWestBound = new JCheckBox(Bundle.getMessage("SetLogic"));

    private JButton getAnchorSavedSignalHeads = null;
    private JButton changeSignalAtBoundaryIcon = null;
    private JButton setSignalsAtBlockBoundaryDone = null;
    private JButton setSignalsAtBlockBoundaryCancel = null;

    private LayoutBlock block1 = null;
    private LayoutBlock block2 = null;

    private TrackSegment eastTrack = null;
    private TrackSegment westTrack = null;

    private PositionablePoint boundary = null;
    private SignalHead eastBoundHead = null;
    private SignalHead westBoundHead = null;

    private boolean showWest = true;
    private boolean showEast = true;

    //display dialog for Set Signals at Block Boundary tool
    public void setSignalsAtBlockBoundaryFromMenu(PositionablePoint p,
            MultiIconEditor theEditor,
            JFrame theFrame) {
        boundary = p;

        //if this is an edge connector...
        if ((p.getType() == PositionablePoint.EDGE_CONNECTOR) && ((p.getLinkedPoint() == null)
                || (p.getLinkedPoint().getConnect1() == null))) {
            if (p.getConnect1Dir() == Path.EAST || p.getConnect1Dir() == Path.SOUTH) {
                showWest = false;
            } else {
                showEast = false;
            }
            block1IDComboBox.setSelectedItem(boundary.getConnect1().getLayoutBlock().getBlock());
        } else {
            block1IDComboBox.setSelectedItem(boundary.getConnect1().getLayoutBlock().getBlock());
            block2IDComboBox.setSelectedItem(boundary.getConnect2().getLayoutBlock().getBlock());
        }
        setSignalsAtBlockBoundaryFromMenuFlag = true;
        setSignalsAtBlockBoundary(theEditor, theFrame);
        setSignalsAtBlockBoundaryFromMenuFlag = false;
    }

    public void setSignalsAtBlockBoundary(MultiIconEditor theEditor, JFrame theFrame) {
        signalIconEditor = theEditor;
        signalFrame = theFrame;

        //Initialize if needed
        if (setSignalsAtBlockBoundaryFrame == null) {
            setSignalsAtBlockBoundaryOpenFlag = false;
            setSignalsAtBlockBoundaryFrame = new JmriJFrame(Bundle.getMessage("SignalsAtBoundary"), false, true);
            oneFrameToRuleThemAll(setSignalsAtBlockBoundaryFrame);
            setSignalsAtBlockBoundaryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSignalsAtBlockBoundaryFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtBoundary", true);
            setSignalsAtBlockBoundaryFrame.setLocation(70, 30);
            Container theContentPane = setSignalsAtBlockBoundaryFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel11 = new JPanel(new FlowLayout());
            block1NameLabel = new JLabel(
                    Bundle.getMessage("MakeLabel",
                            Bundle.getMessage("BeanNameBlock") + " 1 "));
            panel11.add(block1NameLabel);
            panel11.add(block1IDComboBox);
            block1IDComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            theContentPane.add(panel11);

            JPanel panel12 = new JPanel(new FlowLayout());
            block2NameLabel = new JLabel(
                    Bundle.getMessage("MakeLabel",
                            Bundle.getMessage("BeanNameBlock")
                            + " 2 " + Bundle.getMessage("Name")));
            panel12.add(block2NameLabel);
            panel12.add(block2IDComboBox);
            block2IDComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            theContentPane.add(panel12);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel2 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("SignalHeads"));
            panel2.add(shTitle);
            panel2.add(new JLabel("	  "));
            panel2.add(getAnchorSavedSignalHeads = new JButton(Bundle.getMessage("GetSaved")));
            getAnchorSavedSignalHeads.addActionListener((ActionEvent e) -> {
                getSavedAnchorSignals(e);
            });
            getAnchorSavedSignalHeads.setToolTipText(Bundle.getMessage("GetSavedHint"));
            theContentPane.add(panel2);
            if (showEast) {
                JPanel panel21 = new JPanel(new FlowLayout());
                JLabel eastBoundLabel = new JLabel(Bundle.getMessage("East/SouthBound") + ": ");
                panel21.add(eastBoundLabel);
                panel21.add(eastBoundSignalHeadComboBox);
                theContentPane.add(panel21);
                eastBoundSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadEastNameHint"));

                JPanel panel22 = new JPanel(new FlowLayout());
                panel22.add(new JLabel("   "));
                panel22.add(setEastBound);
                setEastBound.setToolTipText(Bundle.getMessage("AnchorPlaceHeadHint"));
                panel22.add(new JLabel("  "));
                if (showWest) {
                    panel22.add(setupLogicEastBound);
                    setupLogicEastBound.setToolTipText(Bundle.getMessage("SetLogicHint"));
                }
                theContentPane.add(panel22);
            }
            if (showWest) {
                JPanel panel31 = new JPanel(new FlowLayout());
                JLabel westBoundLabel = new JLabel(Bundle.getMessage("West/NorthBound") + ": ");
                panel31.add(westBoundLabel);
                panel31.add(westBoundSignalHeadComboBox);
                theContentPane.add(panel31);
                westBoundSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadWestNameHint"));

                JPanel panel32 = new JPanel(new FlowLayout());
                panel32.add(new JLabel("   "));
                panel32.add(setWestBound);
                setWestBound.setToolTipText(Bundle.getMessage("AnchorPlaceHeadHint"));
                panel32.add(new JLabel("  "));
                if (showEast) {
                    panel32.add(setupLogicWestBound);
                    setupLogicWestBound.setToolTipText(Bundle.getMessage("SetLogicHint"));
                }
                theContentPane.add(panel32);
            }

            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(changeSignalAtBoundaryIcon = new JButton(Bundle.getMessage("ChangeSignalIcon")));
            changeSignalAtBoundaryIcon.addActionListener((ActionEvent e) -> {
                signalFrame.setVisible(true);
            });
            changeSignalAtBoundaryIcon.setToolTipText(Bundle.getMessage("ChangeSignalIconHint"));
            panel6.add(new JLabel("	 "));
            panel6.add(setSignalsAtBlockBoundaryDone = new JButton(Bundle.getMessage("ButtonDone")));
            setSignalsAtBlockBoundaryDone.addActionListener((ActionEvent e) -> {
                setSignalsAtBlockBoundaryDonePressed(e);
            });
            setSignalsAtBlockBoundaryDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(setSignalsAtBlockBoundaryDone);
                rootPane.setDefaultButton(setSignalsAtBlockBoundaryDone);
            });

            panel6.add(setSignalsAtBlockBoundaryCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setSignalsAtBlockBoundaryCancel.addActionListener((ActionEvent e) -> {
                setSignalsAtBlockBoundaryCancelPressed(e);
            });
            setSignalsAtBlockBoundaryCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);

            //make this button the default button (return or enter activates)
            JRootPane rootPane = SwingUtilities.getRootPane(setSignalsDone);
            rootPane.setDefaultButton(setSignalsDone);

            setSignalsAtBlockBoundaryFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSignalsAtBlockBoundaryCancelPressed(null);
                }
            });
        }

        block1IDComboBox.setVisible(!setSignalsAtBlockBoundaryFromMenuFlag);
        block2IDComboBox.setVisible(!setSignalsAtBlockBoundaryFromMenuFlag);

        if (setSignalsAtBlockBoundaryFromMenuFlag) {
            getSavedAnchorSignals(null);
            block1NameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock")
                    + " 1 " + Bundle.getMessage("Name"))
                    + boundary.getConnect1().getLayoutBlock().getId());
            if (boundary.getConnect2() != null) {
                block2NameLabel.setText(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("BeanNameBlock")
                        + " 2 " + Bundle.getMessage("Name"))
                        + boundary.getConnect2().getLayoutBlock().getId());
            }
        }

        if (!setSignalsAtBlockBoundaryOpenFlag) {
            setSignalsAtBlockBoundaryFrame.setPreferredSize(null);
            setSignalsAtBlockBoundaryFrame.pack();
            setSignalsAtBlockBoundaryOpenFlag = true;
        }
        setSignalsAtBlockBoundaryFrame.setVisible(true);
    }   //setSignalsAtBlockBoundary

    private void getSavedAnchorSignals(ActionEvent a) {
        if (!getBlockInformation()) {
            return;
        }
        eastBoundSignalHeadComboBox.setSelectedItem(boundary.getEastBoundSignalHead());
        westBoundSignalHeadComboBox.setSelectedItem(boundary.getWestBoundSignalHead());
    }

    private void setSignalsAtBlockBoundaryCancelPressed(ActionEvent a) {
        setSignalsAtBlockBoundaryOpenFlag = false;
        setSignalsAtBlockBoundaryFrame.setVisible(false);
    }

    private void setSignalsAtBlockBoundaryDonePressed(ActionEvent a) {
        if (!getBlockInformation()) {
            return;
        }
        eastBoundHead = getSignalHeadFromEntry(eastBoundSignalHeadComboBox, false, setSignalsAtBlockBoundaryFrame);
        westBoundHead = getSignalHeadFromEntry(westBoundSignalHeadComboBox, false, setSignalsAtBlockBoundaryFrame);
        if ((eastBoundHead == null) && (westBoundHead == null)) {
            JOptionPane.showMessageDialog(setSignalsAtBlockBoundaryFrame,
                    Bundle.getMessage("SignalsError12"),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        //place or update signals as requested
        String newEastBoundSignalName = eastBoundSignalHeadComboBox.getSelectedItemDisplayName();
        if (newEastBoundSignalName == null) {
            newEastBoundSignalName = "";
        }
        if ((eastBoundHead != null) && setEastBound.isSelected()) {
            if (isHeadOnPanel(eastBoundHead)
                    && (eastBoundHead != getHeadFromName(boundary.getEastBoundSignal()))) {
                JOptionPane.showMessageDialog(setSignalsAtBlockBoundaryFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{newEastBoundSignalName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(boundary.getEastBoundSignal());
                placeEastBound();
                removeAssignment(eastBoundHead);
                boundary.setEastBoundSignal(newEastBoundSignalName);
                needRedraw = true;
            }
        } else if ((eastBoundHead != null)
                && (eastBoundHead != getHeadFromName(boundary.getEastBoundSignal()))
                && (eastBoundHead != getHeadFromName(boundary.getWestBoundSignal()))) {
            if (isHeadOnPanel(eastBoundHead)) {
                JOptionPane.showMessageDialog(setSignalsAtBlockBoundaryFrame,
                        Bundle.getMessage("SignalsError13",
                                new Object[]{newEastBoundSignalName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(boundary.getEastBoundSignal());
                removeAssignment(eastBoundHead);
                boundary.setEastBoundSignal(newEastBoundSignalName);
            }
            //} else if ((eastBoundHead != null)
            //            && (eastBoundHead == getHeadFromName(boundary.getWestBoundSignal()))) {
            //need to figure out what to do in this case.
        }
        String newWestBoundSignalName = westBoundSignalHeadComboBox.getSelectedItemDisplayName();
        if (newWestBoundSignalName == null) {
            newWestBoundSignalName = "";
        }
        if ((westBoundHead != null) && setWestBound.isSelected()) {
            if (isHeadOnPanel(westBoundHead)
                    && (westBoundHead != getHeadFromName(boundary.getWestBoundSignal()))) {
                JOptionPane.showMessageDialog(setSignalsAtBlockBoundaryFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{newWestBoundSignalName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(boundary.getWestBoundSignal());
                placeWestBound();
                removeAssignment(westBoundHead);
                boundary.setWestBoundSignal(newWestBoundSignalName);
                needRedraw = true;
            }
        } else if ((westBoundHead != null)
                && (westBoundHead != getHeadFromName(boundary.getEastBoundSignal()))
                && (westBoundHead != getHeadFromName(boundary.getWestBoundSignal()))) {
            if (isHeadOnPanel(westBoundHead)) {
                JOptionPane.showMessageDialog(setSignalsAtBlockBoundaryFrame,
                        Bundle.getMessage("SignalsError13",
                                new Object[]{newWestBoundSignalName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(boundary.getWestBoundSignal());
                removeAssignment(westBoundHead);
                boundary.setWestBoundSignal(newWestBoundSignalName);
            }
            //} else if ((westBoundHead != null)
            //    && (westBoundHead == getHeadFromName(boundary.getEastBoundSignal()))) {
            //need to figure out what to do in this case.
        }
        if ((eastBoundHead != null) && setupLogicEastBound.isSelected()) {
            setLogicEastBound();
        }
        if ((westBoundHead != null) && setupLogicWestBound.isSelected()) {
            setLogicWestBound();
        }
        setSignalsAtBlockBoundaryOpenFlag = false;
        setSignalsAtBlockBoundaryFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }   //setSignalsAtBlockBoundaryDonePressed

    /*
     * Do some thing here for end bumpers.
     */
    private boolean getBlockInformation() {
        //might have to do something to trick it with an end bumper
        if (!setSignalsAtBlockBoundaryFromMenuFlag) {
            block1 = getBlockFromEntry(block1IDComboBox);
            if (block1 == null) {
                return false;
            }
            block2 = getBlockFromEntry(block2IDComboBox);
            if (block2 == null) {
                return false;
            }
            boundary = null;
            for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                if (p.getType() == PositionablePoint.ANCHOR || p.getType() == PositionablePoint.EDGE_CONNECTOR) {
                    LayoutBlock bA = null;
                    LayoutBlock bB = null;
                    if (p.getConnect1() != null) {
                        bA = p.getConnect1().getLayoutBlock();
                    }
                    if (p.getConnect2() != null) {
                        bB = p.getConnect2().getLayoutBlock();
                    }
                    if ((bA != null) && (bB != null) && (bA != bB)) {
                        if (((bA == block1) && (bB == block2))
                                || ((bA == block2) && (bB == block1))) {
                            boundary = p;
                            break;
                        }
                    }
                }
            }
            if (boundary == null) {
                JOptionPane.showMessageDialog(setSignalsAtBlockBoundaryFrame,
                        Bundle.getMessage("SignalsError7"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        //set track orientation at boundary
        eastTrack = null;
        westTrack = null;
        TrackSegment track1 = boundary.getConnect1();
        Point2D coords1;
        if (track1.getConnect1() == boundary) {
            coords1 = LayoutEditor.getCoords(track1.getConnect2(), track1.getType2());
        } else {
            coords1 = LayoutEditor.getCoords(track1.getConnect1(), track1.getType1());
        }
        TrackSegment track2 = boundary.getConnect2();

        if (boundary.getType() == PositionablePoint.END_BUMPER) {
            return true;
        }
        if (boundary.getType() == PositionablePoint.EDGE_CONNECTOR) {
            if (boundary.getConnect1Dir() == Path.EAST || boundary.getConnect1Dir() == Path.SOUTH) {
                eastTrack = track2;
                westTrack = track1;
            } else {
                westTrack = track2;
                eastTrack = track1;
            }
            return true;
        }
        Point2D coords2;
        if (track2.getConnect1() == boundary) {
            coords2 = LayoutEditor.getCoords(track2.getConnect2(), track2.getType2());
        } else {
            coords2 = LayoutEditor.getCoords(track2.getConnect1(), track2.getType1());
        }

        placeSignalDirectionDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coords2, coords1));

        double delX = coords1.getX() - coords2.getX();
        double delY = coords1.getY() - coords2.getY();

        if (Math.abs(delX) >= Math.abs(delY)) {
            if (delX > 0.0) {
                eastTrack = track1;
                westTrack = track2;
            } else {
                eastTrack = track2;
                westTrack = track1;
            }
        } else {
            if (delY > 0.0) {
                eastTrack = track1;	 //south
                westTrack = track2;	 //north
            } else {
                eastTrack = track2;	 //south
                westTrack = track1;	 //north
            }
        }
        return true;
    }   //getBlockInformation

    @CheckReturnValue
    private LayoutBlock getBlockFromEntry(@Nonnull NamedBeanComboBox<Block> blockNameComboBox) {
        return getBlockFromEntry(blockNameComboBox.getSelectedItemDisplayName());
    }

    @CheckReturnValue
    private LayoutBlock getBlockFromEntry(@CheckForNull String theBlockName) {
        if ((theBlockName == null) || theBlockName.isEmpty()) {
            JOptionPane.showMessageDialog(setSignalsAtBlockBoundaryFrame,
                    Bundle.getMessage("SignalsError9"),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        LayoutBlock block = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(theBlockName);
        if (block == null) {
            block = InstanceManager.getDefault(LayoutBlockManager.class).getBySystemName(theBlockName);
            if (block == null) {
                JOptionPane.showMessageDialog(setSignalsAtBlockBoundaryFrame,
                        Bundle.getMessage("SignalsError10",
                                new Object[]{theBlockName}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        if (!block.isOnPanel(layoutEditor)
                && ((boundary == null) || boundary.getType() != PositionablePoint.EDGE_CONNECTOR)) {
            JOptionPane.showMessageDialog(setSignalsAtBlockBoundaryFrame,
                    Bundle.getMessage("SignalsError11",
                            new Object[]{theBlockName}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return (block);
    }

    private void placeEastBound() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = eastBoundSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coords = boundary.getCoordsCenter();
        Point2D delta = new Point2D.Double(0.0, +shift);

        delta = MathUtil.rotateDEG(delta, placeSignalDirectionDEG);
        Point2D where = MathUtil.add(coords, delta);
        setSignalHeadOnPanel(placeSignalDirectionDEG + 180.0, signalHeadName, where);
    }

    private void placeWestBound() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = westBoundSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coords = boundary.getCoordsCenter();

        Point2D delta = new Point2D.Double(0.0, -shift);
        delta = MathUtil.rotateDEG(delta, placeSignalDirectionDEG);
        Point2D where = MathUtil.add(coords, delta);
        setSignalHeadOnPanel(placeSignalDirectionDEG, signalHeadName, where);
    }

    private void setLogicEastBound() {
        LayoutBlock eastBlock = eastTrack.getLayoutBlock();
        Sensor eastBlockOccupancy = eastBlock.getOccupancySensor();
        if (eastBlockOccupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAtBlockBoundaryFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{eastBlock.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PositionablePoint p = boundary;
        if (boundary.getType() == PositionablePoint.EDGE_CONNECTOR && eastTrack != boundary.getConnect1()) {
            p = boundary.getLinkedPoint();
        }
        String newEastBoundSignalName = eastBoundSignalHeadComboBox.getSelectedItemDisplayName();
        if (newEastBoundSignalName == null) {
            newEastBoundSignalName = "";
        }
        SignalHead nextHead = getNextSignalFromObject(eastTrack,
                p, newEastBoundSignalName, setSignalsAtBlockBoundaryFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtBlockBoundaryFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{eastBlock.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (!initializeBlockBossLogic(newEastBoundSignalName)) {
            return;
        }
        logic.setMode(BlockBossLogic.SINGLEBLOCK);
        logic.setSensor1(eastBlockOccupancy.getSystemName());
        if (nextHead != null) {
            logic.setWatchedSignal1(nextHead.getSystemName(), false);
        }
        if (auxSignal != null) {
            logic.setWatchedSignal1Alt(auxSignal.getSystemName());
        }
        finalizeBlockBossLogic();
    }

    private void setLogicWestBound() {
        LayoutBlock westBlock = westTrack.getLayoutBlock();
        Sensor westBlockOccupancy = westBlock.getOccupancySensor();
        if (westBlockOccupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAtBlockBoundaryFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{westBlock.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PositionablePoint p = boundary;
        if (boundary.getType() == PositionablePoint.EDGE_CONNECTOR && westTrack != boundary.getConnect1()) {
            p = boundary.getLinkedPoint();
        }
        String newWestBoundSignalName = westBoundSignalHeadComboBox.getSelectedItemDisplayName();
        if (newWestBoundSignalName == null) {
            newWestBoundSignalName = "";
        }
        SignalHead nextHead = getNextSignalFromObject(westTrack,
                p, newWestBoundSignalName, setSignalsAtBlockBoundaryFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtBlockBoundaryFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{westBlock.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!initializeBlockBossLogic(newWestBoundSignalName)) {
            return;
        }
        logic.setMode(BlockBossLogic.SINGLEBLOCK);
        logic.setSensor1(westBlockOccupancy.getSystemName());
        if (nextHead != null) {
            logic.setWatchedSignal1(nextHead.getSystemName(), false);
        }
        if (auxSignal != null) {
            logic.setWatchedSignal1Alt(auxSignal.getSystemName());
        }
        finalizeBlockBossLogic();
    }

    /*==========================*\
    |* setSignalsAtXoverTurnout *|
    \*==========================*/
    /**
     * Tool to set signals at a double crossover turnout, including placing the
     * signal icons and setup of Simple Signal Logic for each signal head
     * <p>
     * This tool assumes left facing signal head icons have been selected, and
     * will rotate the signal head icons accordingly.
     * <p>
     * This tool will place icons on the outside edge of the turnout.
     * <p>
     * At least one signal at each of the four connection points is required. A
     * second signal at each is optional.
     */
    //operational variables for Set Signals at Double Crossover Turnout tool
    private JmriJFrame setSignalsAtXoverTurnoutFrame = null;
    private boolean setSignalsAtXoverTurnoutOpenFlag = false;
    private boolean setSignalsAtXoverTurnoutFromMenuFlag = false;

    private final NamedBeanComboBox<SignalHead> a1SignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> a2SignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> b1SignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> b2SignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> c1SignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> c2SignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> d1SignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> d2SignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);

    private final JCheckBox setA1Head = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setA2Head = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setB1Head = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setB2Head = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setC1Head = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setC2Head = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setD1Head = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setD2Head = new JCheckBox(Bundle.getMessage("PlaceHead"));

    private final JCheckBox setupA1Logic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupA2Logic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupB1Logic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupB2Logic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupC1Logic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupC2Logic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupD1Logic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupD2Logic = new JCheckBox(Bundle.getMessage("SetLogic"));

    private JButton getSavedXoverSignalHeads = null;
    private JButton changeXoverSignalIcon = null;
    private JButton setXoverSignalsDone = null;
    private JButton setXoverSignalsCancel = null;

    private SignalHead a1Head = null;
    private SignalHead a2Head = null;
    private SignalHead b1Head = null;
    private SignalHead b2Head = null;
    private SignalHead c1Head = null;
    private SignalHead c2Head = null;
    private SignalHead d1Head = null;
    private SignalHead d2Head = null;

    private int xoverType = LayoutTurnout.DOUBLE_XOVER;	 //changes to RH_XOVER or LH_XOVER as required
    private int xoverCurr = LayoutTurnout.UNKNOWN;          //Controls creating the frame
    private String xoverTurnoutName = "";
    private final JLabel xoverTurnoutNameLabel = new JLabel("");

    //display dialog for Set Signals at Crossover Turnout tool
    public void setSignalsAtXoverTurnoutFromMenu(@Nonnull LayoutTurnout to,
            @Nonnull MultiIconEditor theEditor, @Nonnull JFrame theFrame) {
        layoutTurnout = to;
        turnout = to.getTurnout();
        xoverType = layoutTurnout.getTurnoutType();
        if ((xoverType != LayoutTurnout.DOUBLE_XOVER) && (xoverType != LayoutTurnout.RH_XOVER)
                && (xoverType != LayoutTurnout.LH_XOVER)) {
            log.error("entered Set Signals at XOver, with a non-crossover turnout");
            return;
        }
        xoverTurnoutName = layoutTurnout.getTurnoutName();
        setSignalsAtXoverTurnoutFromMenuFlag = true;
        setSignalsAtXoverTurnout(theEditor, theFrame);
        setSignalsAtXoverTurnoutFromMenuFlag = false;
    }

    public void setSignalsAtXoverTurnout(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        signalIconEditor = theEditor;
        signalFrame = theFrame;

        if (!setSignalsAtXoverTurnoutFromMenuFlag) {

            List<LayoutTurnout> xovers = new ArrayList<>();
            for (LayoutTurnout layoutTurnout : layoutEditor.getLayoutTurnouts()) {
                if (layoutTurnout.isTurnoutTypeXover()) {
                    xovers.add(layoutTurnout);
                }
            }
            JComboBox<LayoutTurnout> jcb = new JComboBox<>(
                    xovers.toArray(new LayoutTurnout[xovers.size()]));
            jcb.setEditable(true);
            JOptionPane.showMessageDialog(layoutEditor, jcb,
                    Bundle.getMessage("MakeLabel",
                            Bundle.getMessage("EnterXOverTurnout")),
                    JOptionPane.QUESTION_MESSAGE);
            LayoutTurnout layoutTurnout = (LayoutTurnout) jcb.getSelectedItem();
            xoverTurnoutName = layoutTurnout.getTurnoutName();

            if (xoverTurnoutName.length() < 3) {
                return;
            }
        }

        if (!getTurnoutInformation(true)) {
            return;
        }

        //Initialize if needed which can be the first time or the crossover type has changed.
        if (setSignalsAtXoverTurnoutFrame == null || xoverCurr != xoverType) {
            xoverCurr = xoverType;
            setSignalsAtXoverTurnoutOpenFlag = false;
            setSignalsAtXoverTurnoutFrame = new JmriJFrame(Bundle.getMessage("SignalsAtXoverTurnout"), false, true);
            oneFrameToRuleThemAll(setSignalsAtXoverTurnoutFrame);
            setSignalsAtXoverTurnoutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSignalsAtXoverTurnoutFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtXoverTurnout", true);
            setSignalsAtXoverTurnoutFrame.setLocation(70, 30);
            Container theContentPane = setSignalsAtXoverTurnoutFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel1 = new JPanel(new FlowLayout());
            panel1.add(xoverTurnoutNameLabel);
            theContentPane.add(panel1);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel2 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("SignalHeads"));
            panel2.add(shTitle);
            panel2.add(new JLabel("	  "));
            panel2.add(getSavedXoverSignalHeads = new JButton(Bundle.getMessage("GetSaved")));
            getSavedXoverSignalHeads.addActionListener((ActionEvent e) -> {
                xoverTurnoutSignalsGetSaved(e);
            });
            getSavedXoverSignalHeads.setToolTipText(Bundle.getMessage("GetSavedHint"));
            theContentPane.add(panel2);

            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2a = new JPanel(new FlowLayout());
            panel2a.add(new JLabel("   "));
            panel2a.add(setPlaceAllHeads);
            setPlaceAllHeads.setToolTipText(Bundle.getMessage("PlaceAllHeadsHint"));
            setPlaceAllHeads.addActionListener((ActionEvent e) -> {
                boolean isSelected = setPlaceAllHeads.isSelected();
                //(de)select all checkboxes
                setA1Head.setSelected(isSelected);
                setA2Head.setSelected(isSelected);
                setB1Head.setSelected(isSelected);
                setB2Head.setSelected(isSelected);
                setC1Head.setSelected(isSelected);
                setC2Head.setSelected(isSelected);
                setD1Head.setSelected(isSelected);
                setD2Head.setSelected(isSelected);
            });
            panel2a.add(new JLabel("  "));
            panel2a.add(setupAllLogic);
            setupAllLogic.setToolTipText(Bundle.getMessage("SetAllLogicHint"));
            setupAllLogic.addActionListener((ActionEvent e) -> {
                boolean isSelected = setupAllLogic.isSelected();
                //(de)select all checkboxes
                setupA1Logic.setSelected(isSelected);
                setupA2Logic.setSelected(isSelected);
                setupB1Logic.setSelected(isSelected);
                setupB2Logic.setSelected(isSelected);
                setupC1Logic.setSelected(isSelected);
                setupC2Logic.setSelected(isSelected);
                setupD1Logic.setSelected(isSelected);
                setupD2Logic.setSelected(isSelected);
            });
            theContentPane.add(panel2a);

            JPanel panel21 = new JPanel(new FlowLayout());
            JLabel a1Label = new JLabel(
                    Bundle.getMessage("MakeLabel",
                            Bundle.getMessage("XContinuing", "A")));
            panel21.add(a1Label);
            panel21.add(a1SignalHeadComboBox);
            theContentPane.add(panel21);
            a1SignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel22 = new JPanel(new FlowLayout());
            panel22.add(new JLabel("   "));
            panel22.add(setA1Head);
            setA1Head.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel22.add(new JLabel("  "));
            panel22.add(setupA1Logic);
            setupA1Logic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel22);
            if (!(xoverType == LayoutTurnout.LH_XOVER)) {
                JPanel panel23 = new JPanel(new FlowLayout());
                JLabel a2Label = new JLabel(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("XDiverging", "A")));
                panel23.add(a2Label);
                panel23.add(a2SignalHeadComboBox);
                theContentPane.add(panel23);
                a2SignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));
                JPanel panel24 = new JPanel(new FlowLayout());
                panel24.add(new JLabel("   "));
                panel24.add(setA2Head);
                setA2Head.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
                panel24.add(new JLabel("  "));
                panel24.add(setupA2Logic);
                setupA2Logic.setToolTipText(Bundle.getMessage("SetLogicHint"));
                theContentPane.add(panel24);
            }

            JPanel panel31 = new JPanel(new FlowLayout());
            JLabel b1Label = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("XContinuing", "B")));
            panel31.add(b1Label);
            panel31.add(b1SignalHeadComboBox);
            theContentPane.add(panel31);
            b1SignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel32 = new JPanel(new FlowLayout());
            panel32.add(new JLabel("   "));
            panel32.add(setB1Head);
            setB1Head.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel32.add(new JLabel("  "));
            panel32.add(setupB1Logic);
            setupB1Logic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel32);
            if (!(xoverType == LayoutTurnout.RH_XOVER)) {
                JPanel panel33 = new JPanel(new FlowLayout());
                JLabel b2Label = new JLabel(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("XDiverging", "B")));
                panel33.add(b2Label);
                panel33.add(b2SignalHeadComboBox);
                theContentPane.add(panel33);
                b2SignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));
                JPanel panel34 = new JPanel(new FlowLayout());
                panel34.add(new JLabel("   "));
                panel34.add(setB2Head);
                setB2Head.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
                panel34.add(new JLabel("  "));
                panel34.add(setupB2Logic);
                setupB2Logic.setToolTipText(Bundle.getMessage("SetLogicHint"));
                theContentPane.add(panel34);
            }

            JPanel panel41 = new JPanel(new FlowLayout());
            JLabel c1Label = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("XContinuing", "C")));
            panel41.add(c1Label);
            panel41.add(c1SignalHeadComboBox);
            theContentPane.add(panel41);
            c1SignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel42 = new JPanel(new FlowLayout());
            panel42.add(new JLabel("   "));
            panel42.add(setC1Head);
            setC1Head.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel42.add(new JLabel("  "));
            panel42.add(setupC1Logic);
            setupC1Logic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel42);
            if (!(xoverType == LayoutTurnout.LH_XOVER)) {
                JPanel panel43 = new JPanel(new FlowLayout());
                JLabel c2Label = new JLabel(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("XDiverging", "C")));
                panel43.add(c2Label);
                panel43.add(c2SignalHeadComboBox);
                theContentPane.add(panel43);
                c2SignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));
                JPanel panel44 = new JPanel(new FlowLayout());
                panel44.add(new JLabel("   "));
                panel44.add(setC2Head);
                setC2Head.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
                panel44.add(new JLabel("  "));
                panel44.add(setupC2Logic);
                setupC2Logic.setToolTipText(Bundle.getMessage("SetLogicHint"));
                theContentPane.add(panel44);
            }

            JPanel panel51 = new JPanel(new FlowLayout());
            JLabel d1Label = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("XContinuing", "D")));
            panel51.add(d1Label);
            panel51.add(d1SignalHeadComboBox);
            theContentPane.add(panel51);
            d1SignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel52 = new JPanel(new FlowLayout());
            panel52.add(new JLabel("   "));
            panel52.add(setD1Head);
            setD1Head.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel52.add(new JLabel("  "));
            panel52.add(setupD1Logic);
            setupD1Logic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel52);
            if (xoverType != LayoutTurnout.RH_XOVER) {
                JPanel panel53 = new JPanel(new FlowLayout());
                JLabel d2Label = new JLabel(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("XDiverging", "D")));
                panel53.add(d2Label);
                panel53.add(d2SignalHeadComboBox);
                theContentPane.add(panel53);
                d2SignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));
                JPanel panel54 = new JPanel(new FlowLayout());
                panel54.add(new JLabel("   "));
                panel54.add(setD2Head);
                setD2Head.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
                panel54.add(new JLabel("  "));
                panel54.add(setupD2Logic);
                setupD2Logic.setToolTipText(Bundle.getMessage("SetLogicHint"));
                theContentPane.add(panel54);
            }
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(changeXoverSignalIcon = new JButton(Bundle.getMessage("ChangeSignalIcon")));
            changeXoverSignalIcon.addActionListener((ActionEvent e) -> {
                signalFrame.setVisible(true);
            });
            changeXoverSignalIcon.setToolTipText(Bundle.getMessage("ChangeSignalIconHint"));
            panel6.add(new JLabel("	 "));
            panel6.add(setXoverSignalsDone = new JButton(Bundle.getMessage("ButtonDone")));
            setXoverSignalsDone.addActionListener((ActionEvent e) -> {
                setXoverSignalsDonePressed(e);
            });
            setXoverSignalsDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(setXoverSignalsDone);
                rootPane.setDefaultButton(setXoverSignalsDone);
            });

            panel6.add(setXoverSignalsCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setXoverSignalsCancel.addActionListener((ActionEvent e) -> {
                setXoverSignalsCancelPressed(e);
            });
            setXoverSignalsCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
            setSignalsAtXoverTurnoutFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setXoverSignalsCancelPressed(null);
                }
            });
        }
        setPlaceAllHeads.setSelected(false);
        setupAllLogic.setSelected(false);

        xoverTurnoutNameLabel.setText(Bundle.getMessage("MakeLabel",
                Bundle.getMessage("BeanNameTurnout")
                + " " + Bundle.getMessage("Name")) + xoverTurnoutName);
        xoverType = layoutTurnout.getTurnoutType();

        xoverTurnoutSignalsGetSaved(null);

        if (!setSignalsAtXoverTurnoutOpenFlag) {
            setSignalsAtXoverTurnoutFrame.setPreferredSize(null);
            setSignalsAtXoverTurnoutFrame.pack();
            setSignalsAtXoverTurnoutOpenFlag = true;
        }
        setSignalsAtXoverTurnoutFrame.setVisible(true);
    }   //setSignalsAtXoverTurnout

    private void xoverTurnoutSignalsGetSaved(ActionEvent a) {
        a1SignalHeadComboBox.setSelectedItem(layoutTurnout.getSignalA1());
        a2SignalHeadComboBox.setSelectedItem(layoutTurnout.getSignalA2());
        b1SignalHeadComboBox.setSelectedItem(layoutTurnout.getSignalB1());
        b2SignalHeadComboBox.setSelectedItem(layoutTurnout.getSignalB2());
        c1SignalHeadComboBox.setSelectedItem(layoutTurnout.getSignalC1());
        c2SignalHeadComboBox.setSelectedItem(layoutTurnout.getSignalC2());
        d1SignalHeadComboBox.setSelectedItem(layoutTurnout.getSignalD1());
        d2SignalHeadComboBox.setSelectedItem(layoutTurnout.getSignalD2());
    }

    private void setXoverSignalsCancelPressed(ActionEvent a) {
        setSignalsAtXoverTurnoutOpenFlag = false;
        setSignalsAtXoverTurnoutFrame.setVisible(false);
    }

    private void setXoverSignalsDonePressed(ActionEvent a) {
        if (!getXoverSignalHeadInformation()) {
            return;
        }
        //place signal icons if requested, and assign signal heads to this turnout
        String signalHeadName = a1SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setA1Head.isSelected()) {
            if (isHeadOnPanel(a1Head)
                    && (a1Head != getHeadFromName(layoutTurnout.getSignalA1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
                placeA1();
                removeAssignment(a1Head);
                layoutTurnout.setSignalA1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(a1Head, layoutTurnout);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(a1Head)
                        && isHeadAssignedAnywhere(a1Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
                    removeAssignment(a1Head);
                    layoutTurnout.setSignalA1Name(signalHeadName);
                }
                //} else if (assigned != A1) {
                //need to figure out what to do in this case.
            }
        }
        signalHeadName = a2SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if ((a2Head != null) && setA2Head.isSelected()) {
            if (isHeadOnPanel(a2Head)
                    && (a2Head != getHeadFromName(layoutTurnout.getSignalA2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
                placeA2();
                removeAssignment(a2Head);
                layoutTurnout.setSignalA2Name(signalHeadName);
                needRedraw = true;
            }
        } else if (a2Head != null) {
            int assigned = isHeadAssignedHere(a2Head, layoutTurnout);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(a2Head)
                        && isHeadAssignedAnywhere(a2Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
                    removeAssignment(a2Head);
                    layoutTurnout.setSignalA2Name(signalHeadName);
                }
                //} else if (assigned != A2) {
                //need to figure out what to do in this case.
            }
        } else { //a2Head known to be null here
            removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
            layoutTurnout.setSignalA2Name("");
        }
        signalHeadName = b1SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setB1Head.isSelected()) {
            if (isHeadOnPanel(b1Head)
                    && (b1Head != getHeadFromName(layoutTurnout.getSignalB1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
                placeB1();
                removeAssignment(b1Head);
                layoutTurnout.setSignalB1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(b1Head, layoutTurnout);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(b1Head)
                        && isHeadAssignedAnywhere(b1Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
                    removeAssignment(b1Head);
                    layoutTurnout.setSignalB1Name(signalHeadName);
                }
                //} else if (assigned != B1) {
                //need to figure out what to do in this case.
            }
        }
        signalHeadName = b2SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if ((b2Head != null) && setB2Head.isSelected()) {
            if (isHeadOnPanel(b2Head)
                    && (b2Head != getHeadFromName(layoutTurnout.getSignalB2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout.getSignalB2Name());
                placeB2();
                removeAssignment(b2Head);
                layoutTurnout.setSignalB2Name(signalHeadName);
                needRedraw = true;
            }
        } else if (b2Head != null) {
            int assigned = isHeadAssignedHere(b2Head, layoutTurnout);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(b2Head)
                        && isHeadAssignedAnywhere(b2Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalB2Name());
                    removeAssignment(b2Head);
                    layoutTurnout.setSignalB2Name(signalHeadName);
                }
                //} else if (assigned != B2) {
                //need to figure out what to do in this case.
            }
        } else { //b2Head known to be null here
            removeSignalHeadFromPanel(layoutTurnout.getSignalB2Name());
            layoutTurnout.setSignalB2Name("");
        }
        signalHeadName = c1SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setC1Head.isSelected()) {
            if (isHeadOnPanel(c1Head)
                    && (c1Head != getHeadFromName(layoutTurnout.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
                placeC1();
                removeAssignment(c1Head);
                layoutTurnout.setSignalC1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(c1Head, layoutTurnout);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(c1Head)
                        && isHeadAssignedAnywhere(c1Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
                    removeAssignment(c1Head);
                    layoutTurnout.setSignalC1Name(signalHeadName);
                }
                //} else if (assigned != C1) {
                //need to figure out what to do in this case.
            }
        }
        signalHeadName = c2SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if ((c2Head != null) && setC2Head.isSelected()) {
            if (isHeadOnPanel(c2Head)
                    && (c2Head != getHeadFromName(layoutTurnout.getSignalC2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout.getSignalC2Name());
                placeC2();
                removeAssignment(c2Head);
                layoutTurnout.setSignalC2Name(signalHeadName);
                needRedraw = true;
            }
        } else if (c2Head != null) {
            int assigned = isHeadAssignedHere(c2Head, layoutTurnout);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(c2Head)
                        && isHeadAssignedAnywhere(c2Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalC2Name());
                    removeAssignment(c2Head);
                    layoutTurnout.setSignalC2Name(signalHeadName);
                }
                //} else if (assigned != C2) {
                //need to figure out what to do in this case.
            }
        } else { //c2Head known to be null here
            removeSignalHeadFromPanel(layoutTurnout.getSignalC2Name());
            layoutTurnout.setSignalC2Name("");
        }
        signalHeadName = d1SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setD1Head.isSelected()) {
            if (isHeadOnPanel(d1Head)
                    && (d1Head != getHeadFromName(layoutTurnout.getSignalD1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout.getSignalD1Name());
                placeD1();
                removeAssignment(d1Head);
                layoutTurnout.setSignalD1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(d1Head, layoutTurnout);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(d1Head)
                        && isHeadAssignedAnywhere(d1Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalD1Name());
                    removeAssignment(d1Head);
                    layoutTurnout.setSignalD1Name(signalHeadName);
                }
                //} else if (assigned != D1) {
                //need to figure out what to do in this case.
            }
        }
        signalHeadName = d2SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if ((d2Head != null) && setD2Head.isSelected()) {
            if (isHeadOnPanel(d2Head)
                    && (d2Head != getHeadFromName(layoutTurnout.getSignalD2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout.getSignalD2Name());
                placeD2();
                removeAssignment(d2Head);
                layoutTurnout.setSignalD2Name(signalHeadName);
                needRedraw = true;
            }
        } else if (d2Head != null) {
            int assigned = isHeadAssignedHere(d2Head, layoutTurnout);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(d2Head)
                        && isHeadAssignedAnywhere(d2Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalD2Name());
                    removeAssignment(d2Head);
                    layoutTurnout.setSignalD2Name(signalHeadName);
                }
                //} else if (assigned != D2) {
                //need to figure out what to do in this case.
            }
        } else { //d2Head known to be null here
            removeSignalHeadFromPanel(layoutTurnout.getSignalD2Name());
            layoutTurnout.setSignalD2Name("");
        }
        //setup logic if requested
        if (setupA1Logic.isSelected() || setupA2Logic.isSelected()) {
            if (xoverType == LayoutTurnout.LH_XOVER) {
                setLogicXoverContinuing(a1Head, (TrackSegment) layoutTurnout.getConnectB());
            } else {
                setLogicXover(a1Head, (TrackSegment) layoutTurnout.getConnectB(), a2Head,
                        (TrackSegment) layoutTurnout.getConnectC(), setupA1Logic.isSelected(),
                        setupA2Logic.isSelected());
            }
        }
        if (setupB1Logic.isSelected() || setupB2Logic.isSelected()) {
            if (xoverType == LayoutTurnout.RH_XOVER) {
                setLogicXoverContinuing(b1Head, (TrackSegment) layoutTurnout.getConnectA());
            } else {
                setLogicXover(b1Head, (TrackSegment) layoutTurnout.getConnectA(), b2Head,
                        (TrackSegment) layoutTurnout.getConnectD(), setupB1Logic.isSelected(),
                        setupB2Logic.isSelected());
            }
        }
        if (setupC1Logic.isSelected() || setupC2Logic.isSelected()) {
            if (xoverType == LayoutTurnout.LH_XOVER) {
                setLogicXoverContinuing(c1Head, (TrackSegment) layoutTurnout.getConnectD());
            } else {
                setLogicXover(c1Head, (TrackSegment) layoutTurnout.getConnectD(), c2Head,
                        (TrackSegment) layoutTurnout.getConnectA(), setupC1Logic.isSelected(),
                        setupC2Logic.isSelected());
            }
        }
        if (setupD1Logic.isSelected() || setupD2Logic.isSelected()) {
            if (xoverType == LayoutTurnout.RH_XOVER) {
                setLogicXoverContinuing(d1Head, (TrackSegment) layoutTurnout.getConnectC());
            } else {
                setLogicXover(d1Head, (TrackSegment) layoutTurnout.getConnectC(), d2Head,
                        (TrackSegment) layoutTurnout.getConnectB(), setupD1Logic.isSelected(),
                        setupD2Logic.isSelected());
            }
        }
        //make sure this layout turnout is not linked to another
        layoutTurnout.setLinkType(LayoutTurnout.NO_LINK);
        layoutTurnout.setLinkedTurnoutName("");
        //finish up
        setSignalsAtXoverTurnoutOpenFlag = false;
        setSignalsAtXoverTurnoutFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }   //setXoverSignalsDonePressed

    private boolean getXoverSignalHeadInformation() {
        a1Head = getSignalHeadFromEntry(a1SignalHeadComboBox, true, setSignalsAtXoverTurnoutFrame);
        if (a1Head == null) {
            return false;
        }
        if (!(xoverType == LayoutTurnout.LH_XOVER)) {
            a2Head = getSignalHeadFromEntry(a2SignalHeadComboBox, false, setSignalsAtXoverTurnoutFrame);
        } else {
            a2Head = null;
        }
        b1Head = getSignalHeadFromEntry(b1SignalHeadComboBox, true, setSignalsAtXoverTurnoutFrame);
        if (b1Head == null) {
            return false;
        }
        if (!(xoverType == LayoutTurnout.RH_XOVER)) {
            b2Head = getSignalHeadFromEntry(b2SignalHeadComboBox, false, setSignalsAtXoverTurnoutFrame);
        } else {
            b2Head = null;
        }
        c1Head = getSignalHeadFromEntry(c1SignalHeadComboBox, true, setSignalsAtXoverTurnoutFrame);
        if (c1Head == null) {
            return false;
        }
        if (!(xoverType == LayoutTurnout.LH_XOVER)) {
            c2Head = getSignalHeadFromEntry(c2SignalHeadComboBox, false, setSignalsAtXoverTurnoutFrame);
        } else {
            c2Head = null;
        }
        d1Head = getSignalHeadFromEntry(d1SignalHeadComboBox, true, setSignalsAtXoverTurnoutFrame);
        if (d1Head == null) {
            return false;
        }
        if (!(xoverType == LayoutTurnout.RH_XOVER)) {
            d2Head = getSignalHeadFromEntry(d2SignalHeadComboBox, false, setSignalsAtXoverTurnoutFrame);
        } else {
            d2Head = null;
        }
        return true;
    }

    private void placeA1() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = a1SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsA = layoutTurnout.getCoordsA();
        Point2D delta = new Point2D.Double(0.0, +shift);

        delta = MathUtil.rotateDEG(delta, placeSignalDirectionDEG);
        Point2D where = MathUtil.add(coordsA, delta);
        setSignalHeadOnPanel(placeSignalDirectionDEG + 180.0, signalHeadName, where);
    }

    private void placeA2() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = a2SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsA = layoutTurnout.getCoordsA();
        Point2D delta = new Point2D.Double(-2.0 * shift, +shift);

        delta = MathUtil.rotateDEG(delta, placeSignalDirectionDEG);
        Point2D where = MathUtil.add(coordsA, delta);
        setSignalHeadOnPanel(placeSignalDirectionDEG + 180.0, signalHeadName, where);
    }

    private void placeB1() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = b1SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutTurnout.getCoordsB();
        Point2D delta = new Point2D.Double(-shift, -shift);

        delta = MathUtil.rotateDEG(delta, placeSignalDirectionDEG);
        Point2D where = MathUtil.add(coordsB, delta);
        setSignalHeadOnPanel(placeSignalDirectionDEG, signalHeadName, where);
    }

    private void placeB2() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = b2SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutTurnout.getCoordsB();
        Point2D delta = new Point2D.Double(+shift, -shift);

        delta = MathUtil.rotateDEG(delta, placeSignalDirectionDEG);
        Point2D where = MathUtil.add(coordsB, delta);
        setSignalHeadOnPanel(placeSignalDirectionDEG, signalHeadName, where);
    }

    private void placeC1() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = c1SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsC = layoutTurnout.getCoordsC();
        Point2D delta = new Point2D.Double(0.0, -shift);

        delta = MathUtil.rotateDEG(delta, placeSignalDirectionDEG);
        Point2D where = MathUtil.add(coordsC, delta);
        setSignalHeadOnPanel(placeSignalDirectionDEG, signalHeadName, where);
    }

    private void placeC2() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = c2SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsC = layoutTurnout.getCoordsC();
        Point2D delta = new Point2D.Double(+2.0 * shift, -shift);

        delta = MathUtil.rotateDEG(delta, placeSignalDirectionDEG);
        Point2D where = MathUtil.add(coordsC, delta);
        setSignalHeadOnPanel(placeSignalDirectionDEG, signalHeadName, where);
    }

    private void placeD1() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = d1SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsD = layoutTurnout.getCoordsD();
        Point2D delta = new Point2D.Double(+shift, +shift);

        delta = MathUtil.rotateDEG(delta, placeSignalDirectionDEG);
        Point2D where = MathUtil.add(coordsD, delta);
        setSignalHeadOnPanel(placeSignalDirectionDEG + 180.0, signalHeadName, where);
    }

    private void placeD2() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = d2SignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsD = layoutTurnout.getCoordsD();
        Point2D delta = new Point2D.Double(-shift, +shift);

        delta = MathUtil.rotateDEG(delta, placeSignalDirectionDEG);
        Point2D where = MathUtil.add(coordsD, delta);
        setSignalHeadOnPanel(placeSignalDirectionDEG + 180.0, signalHeadName, where);
    }

    @SuppressWarnings("null")
    private void setLogicXover(SignalHead head, TrackSegment track, SignalHead secondHead, TrackSegment track2,
            boolean setup1, boolean setup2) {
        if ((track == null) && setup1) {
            JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = null;
        SignalHead nextHead = null;
        if ((track != null) && setup1) {
            LayoutBlock block = track.getLayoutBlock();
            if (block == null) {
                JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                        Bundle.getMessage("InfoMessage6"),
                        Bundle.getMessage("MessageTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            occupancy = block.getOccupancySensor();
            if (occupancy == null) {
                JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                        Bundle.getMessage("InfoMessage4",
                                new Object[]{block.getUserName()}),
                        Bundle.getMessage("MessageTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            nextHead = getNextSignalFromObject(track,
                    layoutTurnout, head.getSystemName(), setSignalsAtXoverTurnoutFrame);
            if ((nextHead == null) && (!reachedEndBumper())) {
                JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                        Bundle.getMessage("InfoMessage5",
                                new Object[]{block.getUserName()}),
                        Bundle.getMessage("MessageTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (secondHead != null) {
                if (!initializeBlockBossLogic(head.getSystemName())) {
                    return;
                }
                logic.setMode(BlockBossLogic.TRAILINGMAIN);
                logic.setTurnout(turnout.getSystemName());
                logic.setSensor1(occupancy.getSystemName());
                if (nextHead != null) {
                    logic.setWatchedSignal1(nextHead.getSystemName(), false);
                }
                if (auxSignal != null) {
                    logic.setWatchedSignal1Alt(auxSignal.getSystemName());
                }
                finalizeBlockBossLogic();
            }
        }
        if ((secondHead != null) && !setup2) {
            return;
        }
        SignalHead savedAuxSignal = auxSignal;
        if (track2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block2 = track2.getLayoutBlock();
        if (block2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy2 = block2.getOccupancySensor();
        if (occupancy2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block2.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = head.getSystemName();
        if (secondHead != null) {
            signalHeadName = secondHead.getSystemName();
        }
        SignalHead nextHead2 = getNextSignalFromObject(track2,
                layoutTurnout, signalHeadName, setSignalsAtXoverTurnoutFrame);
        if ((nextHead2 == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{block2.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if ((secondHead == null) && (track != null) && setup1) {
            if (!initializeBlockBossLogic(head.getSystemName())) {
                return;
            }
            logic.setMode(BlockBossLogic.FACING);
            logic.setTurnout(turnout.getSystemName());
            logic.setWatchedSensor1(occupancy.getSystemName());
            logic.setWatchedSensor2(occupancy2.getSystemName());
            if (nextHead != null) {
                logic.setWatchedSignal1(nextHead.getSystemName(), false);
            }
            if (savedAuxSignal != null) {
                logic.setWatchedSignal1Alt(savedAuxSignal.getSystemName());
            }
            if (nextHead2 != null) {
                logic.setWatchedSignal2(nextHead2.getSystemName());
            }
            if (auxSignal != null) {
                logic.setWatchedSignal2Alt(auxSignal.getSystemName());
            }
            logic.setLimitSpeed2(true);
            finalizeBlockBossLogic();
        } else if ((secondHead != null) && setup2) {
            if (!initializeBlockBossLogic(secondHead.getSystemName())) {
                return;
            }
            logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
            logic.setTurnout(turnout.getSystemName());
            logic.setSensor1(occupancy2.getSystemName());
            if (nextHead2 != null) {
                logic.setWatchedSignal1(nextHead2.getSystemName(), false);
            }
            if (auxSignal != null) {
                logic.setWatchedSignal1Alt(auxSignal.getSystemName());
            }
            logic.setLimitSpeed2(true);
            finalizeBlockBossLogic();
        }
    }   //setLogicXover

    private void setLogicXoverContinuing(SignalHead head, TrackSegment track) {
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        SignalHead nextHead = getNextSignalFromObject(track,
                layoutTurnout, head.getSystemName(), setSignalsAtXoverTurnoutFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtXoverTurnoutFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!initializeBlockBossLogic(head.getSystemName())) {
            return;
        }
        logic.setMode(BlockBossLogic.TRAILINGMAIN);
        logic.setTurnout(turnout.getSystemName());
        logic.setSensor1(occupancy.getSystemName());
        if (nextHead != null) {
            logic.setWatchedSignal1(nextHead.getSystemName(), false);
        }
        if (auxSignal != null) {
            logic.setWatchedSignal1Alt(auxSignal.getSystemName());
        }
        finalizeBlockBossLogic();
    }   //setLogicXoverContinuing

    /*=======================*\
    |* setSignalsAtLevelXing *|
    \*=======================*/
    /**
     * Tool to set signals at a level crossing, including placing the signal
     * icons and setup of Simple Signal Logic for each signal head
     * <p>
     * This tool assumes left facing signal head icons have been selected, and
     * will rotate the signal head icons accordingly.
     * <p>
     * This tool will place icons on the right side of each track.
     * <p>
     * Both tracks do not need to be signalled. If one signal for a track, A-C
     * or B-D, the other must also be present.
     * <p>
     * Some user adjustment of turnout positions may be needed.
     */
    //operational variables for Set Signals at Level Crossing tool
    private JmriJFrame setSignalsAtLevelXingFrame = null;
    private boolean setSignalsAtLevelXingOpenFlag = false;
    private boolean setSignalsAtLevelXingFromMenuFlag = false;

    private JLabel blockACNameLabel = null;
    private JLabel blockBDNameLabel = null;

    private final NamedBeanComboBox<Block> blockACComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Block> blockBDComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class),
            null, DisplayOptions.DISPLAYNAME);

    private final NamedBeanComboBox<SignalHead> aSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> bSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> cSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> dSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, DisplayOptions.DISPLAYNAME);

    private final JCheckBox setAHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setBHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setCHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setDHead = new JCheckBox(Bundle.getMessage("PlaceHead"));

    private final JCheckBox setupALogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupBLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupCLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupDLogic = new JCheckBox(Bundle.getMessage("SetLogic"));

    private JButton getSavedXingSignalHeads = null;
    private JButton changeXingSignalIcon = null;
    private JButton setXingSignalsDone = null;
    private JButton setXingSignalsCancel = null;

    private LevelXing levelXing = null;

    private SignalHead aHead = null;
    private SignalHead bHead = null;
    private SignalHead cHead = null;
    private SignalHead dHead = null;

    //display dialog for Set Signals at Level Crossing tool
    public void setSignalsAtLevelXingFromMenu(@Nonnull LevelXing xing,
            @Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        levelXing = xing;
        blockACComboBox.setSelectedItem(levelXing.getLayoutBlockAC().getBlock());
        blockBDComboBox.setSelectedItem(levelXing.getLayoutBlockBD().getBlock());
        setSignalsAtLevelXingFromMenuFlag = true;
        setSignalsAtLevelXing(theEditor, theFrame);
        setSignalsAtLevelXingFromMenuFlag = false;
    }

    public void setSignalsAtLevelXing(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        signalIconEditor = theEditor;
        signalFrame = theFrame;

        //Initialize if needed
        if (setSignalsAtLevelXingFrame == null) {
            setSignalsAtLevelXingOpenFlag = false;
            setSignalsAtLevelXingFrame = new JmriJFrame(Bundle.getMessage("SignalsAtLevelXing"), false, true);
            oneFrameToRuleThemAll(setSignalsAtLevelXingFrame);
            setSignalsAtLevelXingFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSignalsAtLevelXingFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtLevelXing", true);
            setSignalsAtLevelXingFrame.setLocation(70, 30);
            Container theContentPane = setSignalsAtLevelXingFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel11 = new JPanel(new FlowLayout());
            blockACNameLabel = new JLabel(Bundle.getMessage("MakeLabel", (Bundle.getMessage("BeanNameBlock") + " AC")));
            panel11.add(blockACNameLabel);
            panel11.add(blockACComboBox);
            blockACComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            theContentPane.add(panel11);

            JPanel panel12 = new JPanel(new FlowLayout());
            blockBDNameLabel = new JLabel(Bundle.getMessage("MakeLabel", (Bundle.getMessage("BeanNameBlock") + " BD")));
            panel12.add(blockBDNameLabel);
            panel12.add(blockBDComboBox);
            blockBDComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));

            theContentPane.add(panel12);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel2 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("SignalHeads"));
            panel2.add(shTitle);
            panel2.add(new JLabel("	  "));
            panel2.add(getSavedXingSignalHeads = new JButton(Bundle.getMessage("GetSaved")));
            getSavedXingSignalHeads.addActionListener((ActionEvent e) -> {
                xingSignalsGetSaved(e);
            });
            getSavedXingSignalHeads.setToolTipText(Bundle.getMessage("GetSavedHint"));
            theContentPane.add(panel2);

            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2a = new JPanel(new FlowLayout());
            panel2a.add(new JLabel("   "));
            panel2a.add(setPlaceAllHeads);
            setPlaceAllHeads.setToolTipText(Bundle.getMessage("PlaceAllHeadsHint"));
            setPlaceAllHeads.addActionListener((ActionEvent e) -> {
                boolean isSelected = setPlaceAllHeads.isSelected();
                //(de)select all checkboxes
                setAHead.setSelected(isSelected);
                setBHead.setSelected(isSelected);
                setCHead.setSelected(isSelected);
                setDHead.setSelected(isSelected);
            });
            panel2a.add(new JLabel("  "));
            panel2a.add(setupAllLogic);
            setupAllLogic.setToolTipText(Bundle.getMessage("SetAllLogicHint"));
            setupAllLogic.addActionListener((ActionEvent e) -> {
                boolean isSelected = setupAllLogic.isSelected();
                //(de)select all checkboxes
                setupALogic.setSelected(isSelected);
                setupBLogic.setSelected(isSelected);
                setupCLogic.setSelected(isSelected);
                setupDLogic.setSelected(isSelected);
            });
            theContentPane.add(panel2a);

            JPanel panel21 = new JPanel(new FlowLayout());
            JLabel aLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("TrackXConnect", "A")));
            panel21.add(aLabel);
            panel21.add(aSignalHeadComboBox);
            theContentPane.add(panel21);
            aSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel22 = new JPanel(new FlowLayout());
            panel22.add(new JLabel("   "));
            panel22.add(setAHead);
            setAHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel22.add(new JLabel("  "));
            panel22.add(setupALogic);
            setupALogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel22);

            JPanel panel31 = new JPanel(new FlowLayout());
            JLabel bLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("TrackXConnect", "B")));
            panel31.add(bLabel);
            panel31.add(bSignalHeadComboBox);
            theContentPane.add(panel31);
            bSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel32 = new JPanel(new FlowLayout());
            panel32.add(new JLabel("   "));
            panel32.add(setBHead);
            setBHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel32.add(new JLabel("  "));
            panel32.add(setupBLogic);
            setupBLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel32);

            JPanel panel41 = new JPanel(new FlowLayout());
            JLabel cLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("TrackXConnect", "C")));
            panel41.add(cLabel);
            panel41.add(cSignalHeadComboBox);
            theContentPane.add(panel41);
            cSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel42 = new JPanel(new FlowLayout());
            panel42.add(new JLabel("   "));
            panel42.add(setCHead);
            setCHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel42.add(new JLabel("  "));
            panel42.add(setupCLogic);
            setupCLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel42);

            JPanel panel51 = new JPanel(new FlowLayout());
            JLabel dLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("TrackXConnect", "D")));
            panel51.add(dLabel);
            panel51.add(dSignalHeadComboBox);
            theContentPane.add(panel51);
            dSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel52 = new JPanel(new FlowLayout());
            panel52.add(new JLabel("   "));
            panel52.add(setDHead);
            setDHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel52.add(new JLabel("  "));
            panel52.add(setupDLogic);
            setupDLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel52);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(changeXingSignalIcon = new JButton(Bundle.getMessage("ChangeSignalIcon")));
            changeXingSignalIcon.addActionListener((ActionEvent e) -> {
                signalFrame.setVisible(true);
            });
            changeXingSignalIcon.setToolTipText(Bundle.getMessage("ChangeSignalIconHint"));
            panel6.add(new JLabel("	 "));
            panel6.add(setXingSignalsDone = new JButton(Bundle.getMessage("ButtonDone")));
            setXingSignalsDone.addActionListener((ActionEvent e) -> {
                setXingSignalsDonePressed(e);
            });
            setXingSignalsDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(setXingSignalsDone);
                rootPane.setDefaultButton(setXingSignalsDone);
            });

            panel6.add(setXingSignalsCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setXingSignalsCancel.addActionListener((ActionEvent e) -> {
                setXingSignalsCancelPressed(e);
            });
            setXingSignalsCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
            setSignalsAtLevelXingFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setXingSignalsCancelPressed(null);
                }
            });
        }

        aSignalHeadComboBox.setSelectedItem(null);
        bSignalHeadComboBox.setSelectedItem(null);
        cSignalHeadComboBox.setSelectedItem(null);
        dSignalHeadComboBox.setSelectedItem(null);

        setPlaceAllHeads.setSelected(false);
        setupAllLogic.setSelected(false);

        blockACComboBox.setVisible(!setSignalsAtLevelXingFromMenuFlag);
        blockBDComboBox.setVisible(!setSignalsAtLevelXingFromMenuFlag);

        if (setSignalsAtLevelXingFromMenuFlag) {
            blockACNameLabel.setText(Bundle.getMessage("MakeLabel",
                    (Bundle.getMessage("BeanNameBlock") + " AC"))
                    + levelXing.getBlockNameAC());
            blockBDNameLabel.setText(Bundle.getMessage("MakeLabel",
                    (Bundle.getMessage("BeanNameBlock") + " BD"))
                    + levelXing.getBlockNameBD());
            xingSignalsGetSaved(null);
        } else {
            blockACNameLabel.setText(Bundle.getMessage("MakeLabel", (Bundle.getMessage("BeanNameBlock") + " AC")));
            blockBDNameLabel.setText(Bundle.getMessage("MakeLabel", (Bundle.getMessage("BeanNameBlock") + " BD")));
        }

        if (!setSignalsAtLevelXingOpenFlag) {
            setSignalsAtLevelXingFrame.setPreferredSize(null);
            setSignalsAtLevelXingFrame.pack();
            setSignalsAtLevelXingOpenFlag = true;
        }

        setSignalsAtLevelXingFrame.setVisible(true);
    }   //setSignalsAtLevelXing

    private void xingSignalsGetSaved(ActionEvent a) {
        if (!getLevelCrossingInformation()) {
            return;
        }
        aSignalHeadComboBox.setSelectedItem(levelXing.getSignalHead(LevelXing.POINTA));
        bSignalHeadComboBox.setSelectedItem(levelXing.getSignalHead(LevelXing.POINTB));
        cSignalHeadComboBox.setSelectedItem(levelXing.getSignalHead(LevelXing.POINTC));
        dSignalHeadComboBox.setSelectedItem(levelXing.getSignalHead(LevelXing.POINTD));
    }

    private void setXingSignalsCancelPressed(ActionEvent a) {
        setSignalsAtLevelXingOpenFlag = false;
        setSignalsAtLevelXingFrame.setVisible(false);
    }

    private void setXingSignalsDonePressed(ActionEvent a) {
        if (!getLevelCrossingInformation()) {
            return;
        }
        if (!getXingSignalHeadInformation()) {
            return;
        }

        //place or update signals as requested
        String signalName = aSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalName == null) {
            signalName = "";
        }
        if ((aHead != null) && setAHead.isSelected()) {
            if (isHeadOnPanel(aHead)
                    && (aHead != getHeadFromName(levelXing.getSignalAName()))) {
                JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(levelXing.getSignalAName());
                placeXingA();
                removeAssignment(aHead);
                levelXing.setSignalAName(signalName);
                needRedraw = true;
            }
        } else if ((aHead != null)
                && (aHead != getHeadFromName(levelXing.getSignalAName()))
                && (aHead != getHeadFromName(levelXing.getSignalBName()))
                && (aHead != getHeadFromName(levelXing.getSignalCName()))
                && (aHead != getHeadFromName(levelXing.getSignalDName()))) {
            if (isHeadOnPanel(aHead)) {
                JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                        Bundle.getMessage("SignalsError13",
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(levelXing.getSignalAName());
                removeAssignment(aHead);
                levelXing.setSignalAName(signalName);
            }
        } else if ((aHead != null)
                && ((aHead == getHeadFromName(levelXing.getSignalBName()))
                || (aHead == getHeadFromName(levelXing.getSignalCName()))
                || (aHead == getHeadFromName(levelXing.getSignalDName())))) {
            //need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (aHead == null) {
            removeSignalHeadFromPanel(levelXing.getSignalAName());
            levelXing.setSignalAName("");
        }
        signalName = bSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalName == null) {
            signalName = "";
        }
        if ((bHead != null) && setBHead.isSelected()) {
            if (isHeadOnPanel(bHead)
                    && (bHead != getHeadFromName(levelXing.getSignalBName()))) {
                JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(levelXing.getSignalBName());
                placeXingB();
                removeAssignment(bHead);
                levelXing.setSignalBName(signalName);
                needRedraw = true;
            }
        } else if ((bHead != null)
                && (bHead != getHeadFromName(levelXing.getSignalAName()))
                && (bHead != getHeadFromName(levelXing.getSignalBName()))
                && (bHead != getHeadFromName(levelXing.getSignalCName()))
                && (bHead != getHeadFromName(levelXing.getSignalDName()))) {
            if (isHeadOnPanel(bHead)) {
                JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                        Bundle.getMessage("SignalsError13",
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(levelXing.getSignalBName());
                removeAssignment(bHead);
                levelXing.setSignalBName(signalName);
            }
        } else if ((bHead != null)
                && ((bHead == getHeadFromName(levelXing.getSignalAName()))
                || (bHead == getHeadFromName(levelXing.getSignalCName()))
                || (bHead == getHeadFromName(levelXing.getSignalDName())))) {
            //need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (bHead == null) {
            removeSignalHeadFromPanel(levelXing.getSignalBName());
            levelXing.setSignalBName("");
        }
        signalName = cSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalName == null) {
            signalName = "";
        }
        if ((cHead != null) && setCHead.isSelected()) {
            if (isHeadOnPanel(cHead)
                    && (cHead != getHeadFromName(levelXing.getSignalCName()))) {
                JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(levelXing.getSignalCName());
                placeXingC();
                removeAssignment(cHead);
                levelXing.setSignalCName(signalName);
                needRedraw = true;
            }
        } else if ((cHead != null)
                && (cHead != getHeadFromName(levelXing.getSignalAName()))
                && (cHead != getHeadFromName(levelXing.getSignalBName()))
                && (cHead != getHeadFromName(levelXing.getSignalCName()))
                && (cHead != getHeadFromName(levelXing.getSignalDName()))) {
            if (isHeadOnPanel(cHead)) {
                JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                        Bundle.getMessage("SignalsError13",
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(levelXing.getSignalCName());
                removeAssignment(cHead);
                levelXing.setSignalCName(signalName);
            }
        } else if ((cHead != null)
                && ((cHead == getHeadFromName(levelXing.getSignalBName()))
                || (cHead == getHeadFromName(levelXing.getSignalAName()))
                || (cHead == getHeadFromName(levelXing.getSignalDName())))) {
            //need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (cHead == null) {
            removeSignalHeadFromPanel(levelXing.getSignalCName());
            levelXing.setSignalCName("");
        }
        signalName = dSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalName == null) {
            signalName = "";
        }
        if ((dHead != null) && setDHead.isSelected()) {
            if (isHeadOnPanel(dHead)
                    && (dHead != getHeadFromName(levelXing.getSignalDName()))) {
                JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(levelXing.getSignalDName());
                placeXingD();
                removeAssignment(dHead);
                levelXing.setSignalDName(signalName);
                needRedraw = true;
            }
        } else if ((dHead != null)
                && (dHead != getHeadFromName(levelXing.getSignalAName()))
                && (dHead != getHeadFromName(levelXing.getSignalBName()))
                && (dHead != getHeadFromName(levelXing.getSignalCName()))
                && (dHead != getHeadFromName(levelXing.getSignalDName()))) {
            if (isHeadOnPanel(dHead)) {
                JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                        Bundle.getMessage("SignalsError13",
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(levelXing.getSignalDName());
                removeAssignment(dHead);
                levelXing.setSignalDName(signalName);
            }
        } else if ((dHead != null)
                && ((dHead == getHeadFromName(levelXing.getSignalBName()))
                || (dHead == getHeadFromName(levelXing.getSignalCName()))
                || (dHead == getHeadFromName(levelXing.getSignalAName())))) {
            //need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (dHead == null) {
            removeSignalHeadFromPanel(levelXing.getSignalDName());
            levelXing.setSignalDName("");
        }
        //setup logic if requested
        if (setupALogic.isSelected() && (aHead != null)) {
            setLogicXing(aHead, (TrackSegment) levelXing.getConnectC(),
                    levelXing.getLayoutBlockBD(), (TrackSegment) levelXing.getConnectB(),
                    (TrackSegment) levelXing.getConnectD(), aSignalHeadComboBox.getSelectedItemDisplayName());
        }
        if (setupBLogic.isSelected() && (bHead != null)) {
            setLogicXing(bHead, (TrackSegment) levelXing.getConnectD(),
                    levelXing.getLayoutBlockAC(), (TrackSegment) levelXing.getConnectA(),
                    (TrackSegment) levelXing.getConnectC(), bSignalHeadComboBox.getSelectedItemDisplayName());
        }
        if (setupCLogic.isSelected() && (cHead != null)) {
            setLogicXing(cHead, (TrackSegment) levelXing.getConnectA(),
                    levelXing.getLayoutBlockBD(), (TrackSegment) levelXing.getConnectB(),
                    (TrackSegment) levelXing.getConnectD(), cSignalHeadComboBox.getSelectedItemDisplayName());
        }
        if (setupDLogic.isSelected() && (dHead != null)) {
            setLogicXing(dHead, (TrackSegment) levelXing.getConnectB(),
                    levelXing.getLayoutBlockAC(), (TrackSegment) levelXing.getConnectA(),
                    (TrackSegment) levelXing.getConnectC(), dSignalHeadComboBox.getSelectedItemDisplayName());
        }
        //finish up
        setSignalsAtLevelXingOpenFlag = false;
        setSignalsAtLevelXingFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }   //setXingSignalsDonePressed

    private boolean getLevelCrossingInformation() {
        if (!setSignalsAtLevelXingFromMenuFlag) {
            levelXing = null;
            List<LevelXing> levelXings = layoutEditor.getLevelXings();
            if (levelXings.size() <= 0) {
                JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                        Bundle.getMessage("SignalsError15"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            } else if (levelXings.size() == 1) {
                levelXing = levelXings.get(0);
            } else {
                LayoutBlock xingBlockA = null;
                xingBlockA = getBlockFromEntry(blockACComboBox);
                if (xingBlockA == null) {
                    return false;
                }

                LayoutBlock xingBlockC = getBlockFromEntry(blockBDComboBox);
                if (xingBlockC == null) {
                    return false;
                }

                int foundCount = 0;
                //make two block tests first
                for (LevelXing x : layoutEditor.getLevelXings()) {
                    LayoutBlock xA = null;
                    LayoutBlock xB = null;
                    LayoutBlock xC = null;
                    LayoutBlock xD = null;
                    LayoutBlock xAC = x.getLayoutBlockAC();
                    LayoutBlock xBD = x.getLayoutBlockBD();
                    if (x.getConnectA() != null) {
                        xA = ((TrackSegment) x.getConnectA()).getLayoutBlock();
                    }
                    if (x.getConnectB() != null) {
                        xB = ((TrackSegment) x.getConnectB()).getLayoutBlock();
                    }
                    if (x.getConnectC() != null) {
                        xC = ((TrackSegment) x.getConnectC()).getLayoutBlock();
                    }
                    if (x.getConnectD() != null) {
                        xD = ((TrackSegment) x.getConnectD()).getLayoutBlock();
                    }
                    if (((xA != null) && (xC != null) && (((xA == xingBlockA) && (xC == xingBlockC))
                            || ((xA == xingBlockC) && (xC == xingBlockA))))
                            || ((xB != null) && (xD != null) && (((xB == xingBlockA) && (xD == xingBlockC))
                            || ((xB == xingBlockC) && (xD == xingBlockA))))) {
                        levelXing = x;
                        foundCount++;
                    } else if ((xAC != null) && (xBD != null) && (((xAC == xingBlockA) && (xBD == xingBlockC))
                            || ((xAC == xingBlockC) && (xBD == xingBlockA)))) {
                        levelXing = x;
                        foundCount++;
                    }
                }
                if (foundCount == 0) {
                    //try one block test
                    for (LevelXing x : layoutEditor.getLevelXings()) {
                        if ((xingBlockA == x.getLayoutBlockAC()) || (xingBlockA == x.getLayoutBlockBD())) {
                            levelXing = x;
                            foundCount++;
                        }
                    }
                }
                if (foundCount > 1) {
                    JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                            Bundle.getMessage("SignalsError16",
                                    new Object[]{" " + foundCount + " "}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (levelXing == null) {
                    JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                            Bundle.getMessage("SignalsError17"),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }

        Point2D coordsA = levelXing.getCoordsA();
        Point2D coordsC = levelXing.getCoordsC();
        placeSignalDirectionDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsC, coordsA));

        return true;
    }   //getLevelCrossingInformation

    private boolean getXingSignalHeadInformation() {
        //note that all heads are optional, but pairs must be present
        aHead = getSignalHeadFromEntry(aSignalHeadComboBox, false, setSignalsAtLevelXingFrame);
        bHead = getSignalHeadFromEntry(bSignalHeadComboBox, false, setSignalsAtLevelXingFrame);
        cHead = getSignalHeadFromEntry(cSignalHeadComboBox, false, setSignalsAtLevelXingFrame);
        dHead = getSignalHeadFromEntry(dSignalHeadComboBox, false, setSignalsAtLevelXingFrame);
        if (((aHead != null) && (cHead == null)) || ((aHead == null) && (cHead != null))
                || ((bHead != null) && (dHead == null)) || ((bHead == null) && (dHead != null))) {
            JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                    Bundle.getMessage("SignalsError14"),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if ((aHead == null) && (bHead == null) && (cHead == null) && (dHead == null)) {
            JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                    Bundle.getMessage("SignalsError12"),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void placeXingA() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = aSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsA = levelXing.getCoordsA();
        Point2D delta = new Point2D.Double(0.0, +shift);

        delta = MathUtil.rotateDEG(delta, placeSignalDirectionDEG);
        Point2D where = MathUtil.add(coordsA, delta);
        setSignalHeadOnPanel(placeSignalDirectionDEG + 180.0, signalHeadName, where);
    }

    private void placeXingB() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = bSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = levelXing.getCoordsB();
        Point2D coordsD = levelXing.getCoordsD();

        double directionDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsD));
        Point2D delta = new Point2D.Double(0.0, -shift);

        delta = MathUtil.rotateDEG(delta, directionDEG);
        Point2D where = MathUtil.add(coordsB, delta);
        setSignalHeadOnPanel(directionDEG, signalHeadName, where);
    }

    private void placeXingC() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = cSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsC = levelXing.getCoordsC();
        Point2D delta = new Point2D.Double(0.0, -shift);

        delta = MathUtil.rotateDEG(delta, placeSignalDirectionDEG);
        Point2D where = MathUtil.add(coordsC, delta);
        setSignalHeadOnPanel(placeSignalDirectionDEG, signalHeadName, where);
    }

    private void placeXingD() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = dSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = levelXing.getCoordsB();
        Point2D coordsD = levelXing.getCoordsD();

        double directionDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsD, coordsB));
        double diffDirDEG = MathUtil.diffAngleDEG(placeSignalDirectionDEG, directionDEG + 180.0);
        Point2D delta = new Point2D.Double(-shift * Math.cos(Math.toRadians(diffDirDEG)), -shift);

        delta = MathUtil.rotateDEG(delta, directionDEG);
        Point2D where = MathUtil.add(coordsD, delta);
        setSignalHeadOnPanel(directionDEG, signalHeadName, where);
    }

    @SuppressWarnings("null")
    private void setLogicXing(SignalHead head, TrackSegment track, LayoutBlock crossBlock,
            TrackSegment crossTrack1, TrackSegment crossTrack2, String signalHeadName) {
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = null;
        Sensor crossOccupancy = null;
        Sensor track1Occupancy = null;
        Sensor track2Occupancy = null;
        SignalHead nextHead = null;
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (crossBlock != null) {
            crossOccupancy = crossBlock.getOccupancySensor();
        }
        if (crossTrack1 != null) {
            block = crossTrack1.getLayoutBlock();
            if (block != null) {
                track1Occupancy = block.getOccupancySensor();
                if (track1Occupancy == crossOccupancy) {
                    track1Occupancy = null;
                }
            }
        }
        if (crossTrack2 != null) {
            block = crossTrack2.getLayoutBlock();
            if (block != null) {
                track2Occupancy = block.getOccupancySensor();
                if ((track2Occupancy == crossOccupancy)
                        || (track2Occupancy == track1Occupancy)) {
                    track2Occupancy = null;
                }
            }
        }
        nextHead = getNextSignalFromObject(track, levelXing,
                head.getSystemName(), setSignalsAtXoverTurnoutFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if ((crossOccupancy == null) && (track1Occupancy == null) && (track2Occupancy == null)) {
            JOptionPane.showMessageDialog(setSignalsAtLevelXingFrame,
                    Bundle.getMessage("SignalsWarn1",
                            new Object[]{signalHeadName}),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.WARNING_MESSAGE);
        }
        if (!initializeBlockBossLogic(head.getSystemName())) {
            return;
        }
        logic.setMode(BlockBossLogic.SINGLEBLOCK);
        logic.setSensor1(occupancy.getSystemName());
        if (nextHead != null) {
            logic.setWatchedSignal1(nextHead.getSystemName(), false);
        }
        if (auxSignal != null) {
            logic.setWatchedSignal1Alt(auxSignal.getSystemName());
        }
        if (crossOccupancy != null) {
            logic.setSensor2(crossOccupancy.getSystemName());
            if (track1Occupancy != null) {
                logic.setSensor3(track1Occupancy.getSystemName());
                if (track2Occupancy != null) {
                    logic.setSensor4(track2Occupancy.getSystemName());
                }
            } else if (track2Occupancy != null) {
                logic.setSensor3(track2Occupancy.getSystemName());
            }
        } else if (track1Occupancy != null) {
            logic.setSensor2(track1Occupancy.getSystemName());
            if (track2Occupancy != null) {
                logic.setSensor3(track2Occupancy.getSystemName());
            }
        } else if (track2Occupancy != null) {
            logic.setSensor2(track2Occupancy.getSystemName());
        }
        finalizeBlockBossLogic();
    }

    /*====================================*\
    |* setSignalsAtThroatToThroatTurnouts *|
    \*====================================*/
    /**
     * Tool to set signals at throat-to-throat turnouts, including placing the
     * signal icons and setup of signal logic for each signal head
     * <p>
     * This tool can only be accessed from the Tools menu. There is no access
     * from a turnout pop-up menu.
     * <p>
     * This tool requires a situation where two turnouts are connected throat-
     * to-throat by a single "short" track segment. The actual length of the
     * track segment is not tested. If this situation is not found, and error
     * message is sent to the user. To get started with this the user needs to
     * enter at least one of the two connected turnouts.
     * <p>
     * This tool assumes two turnouts connected throat-to-throat, as would be
     * used to represent a double slip turnout. The turnouts may be either
     * left-handed, right-handed, wye, or any pair of these. This tool also
     * assumes that there are no signals at the throat junction. The signal
     * heads will be rotated to face outward--away from the throats. Four sets
     * of one or two signal heads will be placed, one at each of the converging
     * and diverging for each turnout.
     * <p>
     * This tool assumes that each of the four tracks is contained in a
     * different block. Things work best if the two throat-to-throat turnouts
     * are in their own separate block, but this is not necessary.
     * <p>
     * This tool will place icons on the outside edges of each turnout.
     * <p>
     * At least one signal at each of the four connection points is required. A
     * second signal at each is optional.
     */
    //operational variables for Set Signals at Double Crossover Turnout tool
    private JmriJFrame setSignalsAtThroatToThroatTurnoutsFrame = null;
    private boolean setSignalsAtThroatToThroatTurnoutsOpenFlag = false;
    private boolean setSignalsAtThroatToThroatTurnoutsFromMenuFlag = false;

    private JLabel ttotTurnoutName1Label = null;
    private JLabel ttotTurnoutName2Label = null;

    private final NamedBeanComboBox<Turnout> turnout1ComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Turnout> turnout2ComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(),
            null, DisplayOptions.DISPLAYNAME);

    private final NamedBeanComboBox<SignalHead> a1TToTSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class
            ),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> a2TToTSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class
            ),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> b1TToTSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class
            ),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> b2TToTSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class
            ),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> c1TToTSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class
            ),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> c2TToTSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class
            ),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> d1TToTSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class
            ),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> d2TToTSignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class
            ),
            null, DisplayOptions.DISPLAYNAME);

    private final JCheckBox setA1TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setA2TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setB1TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setB2TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setC1TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setC2TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setD1TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setD2TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));

    private final JCheckBox setupA1TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupA2TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupB1TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupB2TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupC1TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupC2TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupD1TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setupD2TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));

    private JButton getSavedTToTSignalHeads = null;
    private JButton changeTToTSignalIcon = null;
    private JButton setTToTSignalsDone = null;
    private JButton setTToTSignalsCancel = null;

    private LayoutTurnout layoutTurnout1 = null;
    private LayoutTurnout layoutTurnout2 = null;

    private Turnout turnout1 = null;
    private Turnout turnout2 = null;

    private TrackSegment connectorTrack = null;

    private String ttotTurnoutName1 = null;
    private String ttotTurnoutName2 = null;

    private SignalHead a1TToTHead = null;
    private SignalHead a2TToTHead = null;
    private SignalHead b1TToTHead = null;
    private SignalHead b2TToTHead = null;
    private SignalHead c1TToTHead = null;
    private SignalHead c2TToTHead = null;
    private SignalHead d1TToTHead = null;
    private SignalHead d2TToTHead = null;

    public void setSignalsAtThroatToThroatTurnoutsFromMenu(
            @Nonnull LayoutTurnout to, @Nonnull String linkedTurnoutName,
            @Nonnull MultiIconEditor theEditor, @Nonnull JFrame theFrame) {
        ttotTurnoutName1 = to.getTurnoutName();
        ttotTurnoutName2 = linkedTurnoutName;

        turnout1ComboBox.setSelectedItem(to.getTurnout());
        turnout2ComboBox.setSelectedItem(to.getSecondTurnout());

        a1TToTSignalHeadComboBox.setSelectedItem(null);
        a2TToTSignalHeadComboBox.setSelectedItem(null);
        b1TToTSignalHeadComboBox.setSelectedItem(null);
        b2TToTSignalHeadComboBox.setSelectedItem(null);
        c1TToTSignalHeadComboBox.setSelectedItem(null);
        c2TToTSignalHeadComboBox.setSelectedItem(null);
        d1TToTSignalHeadComboBox.setSelectedItem(null);
        d2TToTSignalHeadComboBox.setSelectedItem(null);

        setSignalsAtThroatToThroatTurnoutsFromMenuFlag = true;
        setSignalsAtThroatToThroatTurnouts(theEditor, theFrame);
        setSignalsAtThroatToThroatTurnoutsFromMenuFlag = false;
    }

    public void setSignalsAtThroatToThroatTurnouts(
            @Nonnull MultiIconEditor theEditor, @Nonnull JFrame theFrame) {
        signalIconEditor = theEditor;
        signalFrame = theFrame;

        //Initialize if needed
        if (setSignalsAtThroatToThroatTurnoutsFrame == null) {
            setSignalsAtThroatToThroatTurnoutsOpenFlag = false;
            setSignalsAtThroatToThroatTurnoutsFrame = new JmriJFrame(Bundle.getMessage("SignalsAtTToTTurnout"), false, true);
            oneFrameToRuleThemAll(setSignalsAtThroatToThroatTurnoutsFrame);
            setSignalsAtThroatToThroatTurnoutsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSignalsAtThroatToThroatTurnoutsFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtTToTTurnout", true);
            setSignalsAtThroatToThroatTurnoutsFrame.setLocation(70, 30);
            Container theContentPane = setSignalsAtThroatToThroatTurnoutsFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel1a = new JPanel(new FlowLayout());
            ttotTurnoutName1Label = new JLabel(Bundle.getMessage("BeanNameTurnout") + " 1 "
                    + Bundle.getMessage("Name"));
            panel1a.add(ttotTurnoutName1Label);
            panel1a.add(turnout1ComboBox);
            turnout1ComboBox.setToolTipText(Bundle.getMessage("SignalsTurnoutNameHint"));
            theContentPane.add(panel1a);

            JPanel panel1b = new JPanel(new FlowLayout());
            ttotTurnoutName2Label = new JLabel(Bundle.getMessage("BeanNameTurnout") + " 2 "
                    + Bundle.getMessage("Name"));
            panel1b.add(ttotTurnoutName2Label);
            panel1b.add(turnout2ComboBox);
            turnout2ComboBox.setToolTipText(Bundle.getMessage("SignalsTurnoutNameHint"));
            theContentPane.add(panel1b);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            //Provide for retrieval of names of previously saved signal heads

            JPanel panel20 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("SignalHeads"));
            panel20.add(shTitle);
            panel20.add(new JLabel("		"));
            panel20.add(getSavedTToTSignalHeads = new JButton(Bundle.getMessage("GetSaved")));
            getSavedTToTSignalHeads.addActionListener((ActionEvent e) -> {
                setSignalsAtTToTTurnoutsGetSaved(e);
            });
            getSavedTToTSignalHeads.setToolTipText(Bundle.getMessage("GetSavedHint"));
            theContentPane.add(panel20);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel2a = new JPanel(new FlowLayout());
            panel2a.add(new JLabel("   "));
            panel2a.add(setPlaceAllHeads);
            setPlaceAllHeads.setToolTipText(Bundle.getMessage("PlaceAllHeadsHint"));
            setPlaceAllHeads.addActionListener((ActionEvent e) -> {
                boolean isSelected = setPlaceAllHeads.isSelected();
                //(de)select all checkboxes
                setA1TToTHead.setSelected(isSelected);
                setA2TToTHead.setSelected(isSelected);
                setB1TToTHead.setSelected(isSelected);
                setB2TToTHead.setSelected(isSelected);
                setC1TToTHead.setSelected(isSelected);
                setC2TToTHead.setSelected(isSelected);
                setD1TToTHead.setSelected(isSelected);
                setD2TToTHead.setSelected(isSelected);
            });
            panel2a.add(new JLabel("  "));
            panel2a.add(setupAllLogic);
            setupAllLogic.setToolTipText(Bundle.getMessage("SetAllLogicHint"));
            setupAllLogic.addActionListener((ActionEvent e) -> {
                boolean isSelected = setupAllLogic.isSelected();
                //(de)select all checkboxes
                setupA1TToTLogic.setSelected(isSelected);
                setupA2TToTLogic.setSelected(isSelected);
                setupB1TToTLogic.setSelected(isSelected);
                setupB2TToTLogic.setSelected(isSelected);
                setupC1TToTLogic.setSelected(isSelected);
                setupC2TToTLogic.setSelected(isSelected);
                setupD1TToTLogic.setSelected(isSelected);
                setupD2TToTLogic.setSelected(isSelected);
            });
            theContentPane.add(panel2a);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            //Signal heads located at turnout 1
            JPanel panel20a = new JPanel(new FlowLayout());
            panel20a.add(new JLabel(Bundle.getMessage("SignalLocated")
                    + " " + Bundle.getMessage("BeanNameTurnout") + " 1 - "
                    + Bundle.getMessage("ContinuingTrack")));
            theContentPane.add(panel20a);

            JPanel panel21 = new JPanel(new FlowLayout());
            panel21.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 2 - "
                    + Bundle.getMessage("ContinuingTrack"))));
            panel21.add(a1TToTSignalHeadComboBox);
            theContentPane.add(panel21);
            a1TToTSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel22 = new JPanel(new FlowLayout());
            panel22.add(new JLabel(Bundle.getMessage("OrBoth") + " 2 " + Bundle.getMessage("Tracks)") + "	  "));
            panel22.add(setA1TToTHead);
            setA1TToTHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel22.add(new JLabel("  "));
            panel22.add(setupA1TToTLogic);
            setupA1TToTLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel22);

            JPanel panel23 = new JPanel(new FlowLayout());
            panel23.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 2 - "
                    + Bundle.getMessage("DivergingTrack"))));
            panel23.add(a2TToTSignalHeadComboBox);
            theContentPane.add(panel23);
            a2TToTSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel24 = new JPanel(new FlowLayout());
            panel24.add(new JLabel("				"));
            panel24.add(setA2TToTHead);
            setA2TToTHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel24.add(new JLabel("  "));
            panel24.add(setupA2TToTLogic);
            setupA2TToTLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel24);

            JPanel panel31x = new JPanel(new FlowLayout());
            panel31x.add(new JLabel(Bundle.getMessage("SignalLocated")
                    + " " + Bundle.getMessage("BeanNameTurnout") + " 1 - "
                    + Bundle.getMessage("DivergingTrack")));
            theContentPane.add(panel31x);

            JPanel panel31 = new JPanel(new FlowLayout());
            panel31.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 2 - "
                    + Bundle.getMessage("ContinuingTrack"))));
            panel31.add(b1TToTSignalHeadComboBox);
            theContentPane.add(panel31);
            b1TToTSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel32 = new JPanel(new FlowLayout());
            panel32.add(new JLabel(Bundle.getMessage("OrBoth") + " 2 " + Bundle.getMessage("Tracks)") + "	  "));
            panel32.add(setB1TToTHead);
            setB1TToTHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel32.add(new JLabel("  "));
            panel32.add(setupB1TToTLogic);
            setupB1TToTLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel32);

            JPanel panel33 = new JPanel(new FlowLayout());
            panel33.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 2 - "
                    + Bundle.getMessage("DivergingTrack"))));
            panel33.add(b2TToTSignalHeadComboBox);
            theContentPane.add(panel33);
            b2TToTSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel34 = new JPanel(new FlowLayout());
            panel34.add(new JLabel("				"));
            panel34.add(setB2TToTHead);
            setB2TToTHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel34.add(new JLabel("  "));
            panel34.add(setupB2TToTLogic);
            setupB2TToTLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel34);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            //Signal heads located at turnout 2

            JPanel panel41x = new JPanel(new FlowLayout());
            panel41x.add(new JLabel(Bundle.getMessage("SignalLocated")
                    + " " + Bundle.getMessage("BeanNameTurnout") + " 2 - "
                    + Bundle.getMessage("ContinuingTrack")));
            theContentPane.add(panel41x);

            JPanel panel41 = new JPanel(new FlowLayout());
            panel33.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 1 - "
                    + Bundle.getMessage("ContinuingTrack"))));
            panel41.add(c1TToTSignalHeadComboBox);
            theContentPane.add(panel41);
            c1TToTSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel42 = new JPanel(new FlowLayout());
            panel42.add(new JLabel(Bundle.getMessage("OrBoth") + " 1 " + Bundle.getMessage("Tracks)") + "	  "));
            panel42.add(setC1TToTHead);
            setC1TToTHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel42.add(new JLabel("  "));
            panel42.add(setupC1TToTLogic);
            setupC1TToTLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel42);

            JPanel panel43 = new JPanel(new FlowLayout());
            panel43.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 1 - "
                    + Bundle.getMessage("DivergingTrack"))));
            panel43.add(c2TToTSignalHeadComboBox);
            theContentPane.add(panel43);
            c2TToTSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel44 = new JPanel(new FlowLayout());
            panel44.add(new JLabel("				"));
            panel44.add(setC2TToTHead);
            setC2TToTHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel44.add(new JLabel("  "));
            panel44.add(setupC2TToTLogic);
            setupC2TToTLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel44);

            JPanel panel51x = new JPanel(new FlowLayout());
            panel51x.add(new JLabel(Bundle.getMessage("SignalLocated")
                    + " " + Bundle.getMessage("BeanNameTurnout") + " 2 - "
                    + Bundle.getMessage("DivergingTrack")));
            theContentPane.add(panel51x);

            JPanel panel51 = new JPanel(new FlowLayout());
            panel51.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 1 - "
                    + Bundle.getMessage("ContinuingTrack"))));
            panel51.add(d1TToTSignalHeadComboBox);
            theContentPane.add(panel51);
            d1TToTSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel52 = new JPanel(new FlowLayout());
            panel52.add(new JLabel(Bundle.getMessage("OrBoth") + " 1 " + Bundle.getMessage("Tracks)") + "	  "));
            panel52.add(setD1TToTHead);
            setD1TToTHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel52.add(new JLabel("  "));
            panel52.add(setupD1TToTLogic);
            setupD1TToTLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel52);

            JPanel panel53 = new JPanel(new FlowLayout());
            panel53.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 1 - "
                    + Bundle.getMessage("DivergingTrack"))));
            panel53.add(d2TToTSignalHeadComboBox);
            theContentPane.add(panel53);
            d2TToTSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel54 = new JPanel(new FlowLayout());
            panel54.add(new JLabel("				"));
            panel54.add(setD2TToTHead);
            setD2TToTHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel54.add(new JLabel("  "));
            panel54.add(setupD2TToTLogic);
            setupD2TToTLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel54);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(changeTToTSignalIcon = new JButton(Bundle.getMessage("ChangeSignalIcon")));
            changeTToTSignalIcon.addActionListener((ActionEvent e) -> {
                signalFrame.setVisible(true);
            });
            changeTToTSignalIcon.setToolTipText(Bundle.getMessage("ChangeSignalIconHint"));
            panel6.add(new JLabel("	 "));
            panel6.add(setTToTSignalsDone = new JButton(Bundle.getMessage("ButtonDone")));
            setTToTSignalsDone.addActionListener((ActionEvent e) -> {
                setTToTSignalsDonePressed(e);
            });
            setTToTSignalsDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(setTToTSignalsDone);
                rootPane.setDefaultButton(setTToTSignalsDone);
            });

            panel6.add(setTToTSignalsCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setTToTSignalsCancel.addActionListener((ActionEvent e) -> {
                setTToTSignalsCancelPressed(e);
            });
            setTToTSignalsCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
            setSignalsAtThroatToThroatTurnoutsFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setTToTSignalsCancelPressed(null);
                }
            });
        }
        setPlaceAllHeads.setSelected(false);
        setupAllLogic.setSelected(false);

        turnout1ComboBox.setVisible(!setSignalsAtThroatToThroatTurnoutsFromMenuFlag);
        turnout2ComboBox.setVisible(!setSignalsAtThroatToThroatTurnoutsFromMenuFlag);

        if (setSignalsAtThroatToThroatTurnoutsFromMenuFlag) {
            ttotTurnoutName1Label.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameTurnout") + " 1 "
                    + Bundle.getMessage("Name")) + ttotTurnoutName1);
            ttotTurnoutName2Label.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameTurnout") + " 2 "
                    + Bundle.getMessage("Name")) + ttotTurnoutName2);

            SwingUtilities.invokeLater(() -> {
                setSignalsAtTToTTurnoutsGetSaved(null);
            });
        } else {
            ttotTurnoutName1Label.setText(
                    Bundle.getMessage("BeanNameTurnout") + " 1 "
                    + Bundle.getMessage("Name"));
            ttotTurnoutName2Label.setText(
                    Bundle.getMessage("BeanNameTurnout") + " 2 "
                    + Bundle.getMessage("Name"));
        }

        if (!setSignalsAtThroatToThroatTurnoutsOpenFlag) {
            setSignalsAtThroatToThroatTurnoutsFrame.setPreferredSize(null);
            setSignalsAtThroatToThroatTurnoutsFrame.pack();
            setSignalsAtThroatToThroatTurnoutsOpenFlag = true;
        }
        setSignalsAtThroatToThroatTurnoutsFrame.setVisible(true);
    }   //setSignalsAtTToTTurnouts

    private void setSignalsAtTToTTurnoutsGetSaved(ActionEvent a) {
        if (!getTToTTurnoutInformation()) {
            return;
        }
        a1TToTSignalHeadComboBox.setSelectedItem(layoutTurnout1.getSignalB1());
        a2TToTSignalHeadComboBox.setSelectedItem(layoutTurnout1.getSignalB2());
        b1TToTSignalHeadComboBox.setSelectedItem(layoutTurnout1.getSignalC1());
        b2TToTSignalHeadComboBox.setSelectedItem(layoutTurnout1.getSignalC2());
        c1TToTSignalHeadComboBox.setSelectedItem(layoutTurnout2.getSignalB1());
        c2TToTSignalHeadComboBox.setSelectedItem(layoutTurnout2.getSignalB2());
        d1TToTSignalHeadComboBox.setSelectedItem(layoutTurnout2.getSignalC1());
        d2TToTSignalHeadComboBox.setSelectedItem(layoutTurnout2.getSignalC2());
    }

    private void setTToTSignalsCancelPressed(ActionEvent a) {
        setSignalsAtThroatToThroatTurnoutsOpenFlag = false;
        setSignalsAtThroatToThroatTurnoutsFrame.setVisible(false);
    }

    private boolean getTToTTurnoutInformation() {
        int type = 0;
        Object connect = null;

        turnout1 = null;
        turnout2 = null;

        layoutTurnout1 = null;
        layoutTurnout2 = null;

        if (!setSignalsAtThroatToThroatTurnoutsFromMenuFlag) {
            ttotTurnoutName1 = turnout1ComboBox.getSelectedItemDisplayName();
            if (ttotTurnoutName1 == null) {
                ttotTurnoutName1 = "";
            }
        }
        if (ttotTurnoutName1.isEmpty()) {
            //turnout 1 not entered, test turnout 2
            ttotTurnoutName2 = turnout2ComboBox.getSelectedItemDisplayName();
            if (ttotTurnoutName2 == null) {
                ttotTurnoutName2 = "";
            }
            if (ttotTurnoutName2.isEmpty()) {
                //no entries in turnout fields
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("SignalsError1"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            turnout2 = InstanceManager.turnoutManagerInstance().getTurnout(ttotTurnoutName2);
            if (turnout2 == null) {
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("SignalsError2",
                                new Object[]{ttotTurnoutName2}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            String uname = turnout2.getUserName();
            if ((uname == null) || uname.isEmpty()
                    || !uname.equals(ttotTurnoutName2)) {
                turnout2ComboBox.setSelectedItem(turnout2);
            }
            layoutTurnout2 = getLayoutTurnoutFromTurnout(turnout2, false, ttotTurnoutName2, setSignalsAtThroatToThroatTurnoutsFrame);
            if (layoutTurnout2 == null) {
                return false;
            }
            //have turnout 2 and layout turnout 2 - look for turnout 1
            connectorTrack = (TrackSegment) layoutTurnout2.getConnectA();
            if (connectorTrack == null) {
                //Inform user of error, and terminate
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("SignalsError18"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            type = connectorTrack.getType1();
            connect = connectorTrack.getConnect1();
            if (connect == layoutTurnout2) {
                type = connectorTrack.getType2();
                connect = connectorTrack.getConnect2();
            }
            if ((type != LayoutTrack.TURNOUT_A) || (connect == null)) {
                //Not two turnouts connected throat-to-throat by a single Track Segment
                //Inform user of error and terminate
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("SignalsError18"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            layoutTurnout1 = (LayoutTurnout) connect;
            turnout1 = layoutTurnout1.getTurnout();
            if (turnout1 == null) {
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("SignalsError18"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            turnout1ComboBox.setSelectedItem(turnout1);
        } else {
            //something was entered in the turnout 1 field
            turnout1 = InstanceManager.turnoutManagerInstance().getTurnout(ttotTurnoutName1);
            if (turnout1 == null) {
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("SignalsError2",
                                new Object[]{ttotTurnoutName1}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            String uname = turnout1.getUserName();
            if ((uname == null) || uname.isEmpty() || !uname.equals(ttotTurnoutName1)) {
                turnout1ComboBox.setSelectedItem(turnout1);
            }
            //have turnout 1 - get corresponding layoutTurnout
            layoutTurnout1 = getLayoutTurnoutFromTurnout(turnout1, false, ttotTurnoutName1, setSignalsAtThroatToThroatTurnoutsFrame);
            if (layoutTurnout1 == null) {
                return false;
            }
            turnout1ComboBox.setSelectedItem(layoutTurnout1.getTurnout());
            //have turnout 1 and layout turnout 1 - was something entered for turnout 2
            ttotTurnoutName2 = turnout2ComboBox.getSelectedItemDisplayName();
            if (ttotTurnoutName2 == null) {
                ttotTurnoutName2 = "";
            }
            if (ttotTurnoutName2.isEmpty()) {
                //no entry for turnout 2
                connectorTrack = (TrackSegment) layoutTurnout1.getConnectA();
                if (connectorTrack == null) {
                    //Inform user of error, and terminate
                    JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                            Bundle.getMessage("SignalsError18"),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                type = connectorTrack.getType1();
                connect = connectorTrack.getConnect1();
                if (connect == layoutTurnout1) {
                    type = connectorTrack.getType2();
                    connect = connectorTrack.getConnect2();
                }
                if ((type != LayoutTrack.TURNOUT_A) || (connect == null)) {
                    //Not two turnouts connected throat-to-throat by a single Track Segment
                    //Inform user of error and terminate
                    JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                            Bundle.getMessage("SignalsError18"),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                layoutTurnout2 = (LayoutTurnout) connect;
                turnout2 = layoutTurnout2.getTurnout();
                if (turnout2 == null) {
                    JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                            Bundle.getMessage("SignalsError18"),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                turnout2ComboBox.setSelectedItem(turnout2);
            } else {
                //turnout 2 entered also
                turnout2 = InstanceManager.turnoutManagerInstance().getTurnout(ttotTurnoutName2);
                if (turnout2 == null) {
                    JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                            Bundle.getMessage("SignalsError2",
                                    new Object[]{ttotTurnoutName2}), Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                uname = turnout2.getUserName();
                if ((uname == null) || uname.isEmpty() || !uname.equals(ttotTurnoutName2)) {
                    turnout2ComboBox.setSelectedItem(turnout2);
                }
                layoutTurnout2 = getLayoutTurnoutFromTurnout(turnout2, false, ttotTurnoutName2, setSignalsAtThroatToThroatTurnoutsFrame);
                if (layoutTurnout2 == null) {
                    return false;
                }
                turnout2ComboBox.setSelectedItem(layoutTurnout2.getTurnout());
                //check that layout turnout 1 and layout turnout 2 are connected throat-to-throat
                if (layoutTurnout1.getConnectA() != layoutTurnout2.getConnectA()) {
                    //Not two turnouts connected throat-to-throat by a single Track Segment
                    //Inform user of error and terminate
                    JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                            Bundle.getMessage("SignalsError18"),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                connectorTrack = (TrackSegment) layoutTurnout1.getConnectA();
            }
        }
        //have both turnouts, correctly connected - complete initialization
        Point2D coordsA = layoutTurnout1.getCoordsA();
        Point2D coordsCenter = layoutTurnout1.getCoordsCenter();
        placeSignalDirectionDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsCenter, coordsA));
        return true;
    }   //getTToTTurnoutInformation

    private void setTToTSignalsDonePressed(ActionEvent a) {
        if (!getTToTTurnoutInformation()) {
            return;
        }
        if (!getTToTSignalHeadInformation()) {
            return;
        }

        //place signal icons if requested, and assign signal heads to this turnout
        String signalHeadName = a1TToTSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setA1TToTHead.isSelected()) {
            if (isHeadOnPanel(a1TToTHead)
                    && (a1TToTHead != getHeadFromName(layoutTurnout1.getSignalB1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout1.getSignalB1Name());
                if (layoutTurnout1.getContinuingSense() == Turnout.CLOSED) {
                    placeA1TToT(signalHeadName);
                } else {
                    placeB1TToT(signalHeadName);
                }
                removeAssignment(a1TToTHead);
                layoutTurnout1.setSignalB1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(a1TToTHead, layoutTurnout1);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(a1TToTHead)
                        && isHeadAssignedAnywhere(a1TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout1.getSignalB1Name());
                    removeAssignment(a1TToTHead);
                    layoutTurnout1.setSignalB1Name(signalHeadName);
                }
                //} else if (assigned != B1) {
                //need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }

        signalHeadName = a2TToTSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if ((a2TToTHead != null) && setA2TToTHead.isSelected()) {
            if (isHeadOnPanel(a2TToTHead)
                    && (a2TToTHead != getHeadFromName(layoutTurnout1.getSignalB2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout1.getSignalB2Name());
                if (layoutTurnout1.getContinuingSense() == Turnout.CLOSED) {
                    placeA2TToT(signalHeadName);
                } else {
                    placeB2TToT(signalHeadName);
                }
                removeAssignment(a2TToTHead);
                layoutTurnout1.setSignalB2Name(signalHeadName);
                needRedraw = true;
            }
        } else if (a2TToTHead != null) {
            int assigned = isHeadAssignedHere(a2TToTHead, layoutTurnout1);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(a2TToTHead)
                        && isHeadAssignedAnywhere(a2TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout1.getSignalB2Name());
                    removeAssignment(a2TToTHead);
                    layoutTurnout1.setSignalB2Name(signalHeadName);
                }
                //} else if (assigned != B2) {
                //need to figure out what to do in this case.
            }
        } else { //a2TToTHead known to be null here
            removeSignalHeadFromPanel(layoutTurnout1.getSignalB2Name());
            layoutTurnout1.setSignalB2Name("");
        }

        signalHeadName = b1TToTSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setB1TToTHead.isSelected()) {
            if (isHeadOnPanel(b1TToTHead)
                    && (b1TToTHead != getHeadFromName(layoutTurnout1.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout1.getSignalC1Name());
                if (layoutTurnout1.getContinuingSense() == Turnout.CLOSED) {
                    placeB1TToT(signalHeadName);
                } else {
                    placeA1TToT(signalHeadName);
                }
                removeAssignment(b1TToTHead);
                layoutTurnout1.setSignalC1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(b1TToTHead, layoutTurnout1);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(b1TToTHead)
                        && isHeadAssignedAnywhere(b1TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout1.getSignalC1Name());
                    removeAssignment(b1TToTHead);
                    layoutTurnout1.setSignalC1Name(signalHeadName);
                }
                //} else if (assigned != C1) {
                //need to figure out what to do in this case.
            }
        }

        signalHeadName = b2TToTSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if ((b2TToTHead != null) && setB2TToTHead.isSelected()) {
            if (isHeadOnPanel(b2TToTHead)
                    && (b2TToTHead != getHeadFromName(layoutTurnout1.getSignalC2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout1.getSignalC2Name());
                if (layoutTurnout1.getContinuingSense() == Turnout.CLOSED) {
                    placeB2TToT(signalHeadName);
                } else {
                    placeA2TToT(signalHeadName);
                }
                removeAssignment(b2TToTHead);
                layoutTurnout1.setSignalC2Name(signalHeadName);
                needRedraw = true;
            }
        } else if (b2TToTHead != null) {
            int assigned = isHeadAssignedHere(b2TToTHead, layoutTurnout1);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(b2TToTHead)
                        && isHeadAssignedAnywhere(b2TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout1.getSignalC2Name());
                    removeAssignment(b2TToTHead);
                    layoutTurnout1.setSignalC2Name(signalHeadName);
                }
                //} else if (assigned != C2) {
                //need to figure out what to do in this case.
            }
        } else { //b2TToTHead known to be null here
            removeSignalHeadFromPanel(layoutTurnout1.getSignalC2Name());
            layoutTurnout1.setSignalC2Name("");
        }

        //signal heads on turnout 2
        signalHeadName = c1TToTSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setC1TToTHead.isSelected()) {
            if (isHeadOnPanel(c1TToTHead)
                    && (c1TToTHead != getHeadFromName(layoutTurnout2.getSignalB1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout2.getSignalB1Name());
                if (layoutTurnout2.getContinuingSense() == Turnout.CLOSED) {
                    placeC1TToT(signalHeadName);
                } else {
                    placeD1TToT(signalHeadName);
                }
                removeAssignment(c1TToTHead);
                layoutTurnout2.setSignalB1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(c1TToTHead, layoutTurnout2);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(c1TToTHead)
                        && isHeadAssignedAnywhere(c1TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout2.getSignalB1Name());
                    removeAssignment(c1TToTHead);
                    layoutTurnout2.setSignalB1Name(signalHeadName);
                }
                //} else if (assigned != B1) {
                //need to figure out what to do in this case.
            }
        }

        signalHeadName = c2TToTSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if ((c2TToTHead != null) && setC2TToTHead.isSelected()) {
            if (isHeadOnPanel(c2TToTHead)
                    && (c2TToTHead != getHeadFromName(layoutTurnout2.getSignalB2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout2.getSignalB2Name());
                if (layoutTurnout2.getContinuingSense() == Turnout.CLOSED) {
                    placeC2TToT(signalHeadName);
                } else {
                    placeD2TToT(signalHeadName);
                }
                removeAssignment(c2TToTHead);
                layoutTurnout2.setSignalB2Name(signalHeadName);
                needRedraw = true;
            }
        } else if (c2TToTHead != null) {
            int assigned = isHeadAssignedHere(c2TToTHead, layoutTurnout2);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(c2TToTHead)
                        && isHeadAssignedAnywhere(c2TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout2.getSignalB2Name());
                    removeAssignment(c2TToTHead);
                    layoutTurnout2.setSignalB2Name(signalHeadName);
                }
                //} else if (assigned != B2) {
                //need to figure out what to do in this case.
            }
        } else { //c2TToTHead known to be null here
            removeSignalHeadFromPanel(layoutTurnout2.getSignalB2Name());
            layoutTurnout2.setSignalB2Name("");
        }

        signalHeadName = d1TToTSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setD1TToTHead.isSelected()) {
            if (isHeadOnPanel(d1TToTHead)
                    && (d1TToTHead != getHeadFromName(layoutTurnout2.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout2.getSignalC1Name());
                if (layoutTurnout2.getContinuingSense() == Turnout.CLOSED) {
                    placeD1TToT(signalHeadName);
                } else {
                    placeC1TToT(signalHeadName);
                }
                removeAssignment(d1TToTHead);
                layoutTurnout2.setSignalC1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(d1TToTHead, layoutTurnout2);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(d1TToTHead)
                        && isHeadAssignedAnywhere(d1TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout2.getSignalC1Name());
                    removeAssignment(d1TToTHead);
                    layoutTurnout2.setSignalC1Name(signalHeadName);
                }
                //} else if (assigned != C1) {
                //need to figure out what to do in this case.
            }
        }

        signalHeadName = d2TToTSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if ((d2TToTHead != null) && setD2TToTHead.isSelected()) {
            if (isHeadOnPanel(d2TToTHead)
                    && (d2TToTHead != getHeadFromName(layoutTurnout2.getSignalC2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnout2.getSignalC2Name());
                if (layoutTurnout2.getContinuingSense() == Turnout.CLOSED) {
                    placeD2TToT(signalHeadName);
                } else {
                    placeC2TToT(signalHeadName);
                }
                removeAssignment(d2TToTHead);
                layoutTurnout2.setSignalC2Name(signalHeadName);
                needRedraw = true;
            }
        } else if (d2TToTHead != null) {
            int assigned = isHeadAssignedHere(d2TToTHead, layoutTurnout2);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(d2TToTHead)
                        && isHeadAssignedAnywhere(d2TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout2.getSignalC2Name());
                    removeAssignment(d2TToTHead);
                    layoutTurnout2.setSignalC2Name(signalHeadName);
                }
                //} else if (assigned != C2) {
                //need to figure out what to do in this case.
            }
        } else { //d2TToTHead known to be null here
            removeSignalHeadFromPanel(layoutTurnout2.getSignalC2Name());
            layoutTurnout2.setSignalC2Name("");
        }

        //setup logic if requested
        if (setupA1TToTLogic.isSelected() || setupA2TToTLogic.isSelected()) {
            setLogicTToT(a1TToTHead, (TrackSegment) layoutTurnout2.getConnectB(), a2TToTHead,
                    (TrackSegment) layoutTurnout2.getConnectC(), setupA1TToTLogic.isSelected(),
                    setupA2TToTLogic.isSelected(), true, layoutTurnout2, layoutTurnout1);
        }
        if (setupB1TToTLogic.isSelected() || setupB2TToTLogic.isSelected()) {
            setLogicTToT(b1TToTHead, (TrackSegment) layoutTurnout2.getConnectB(), b2TToTHead,
                    (TrackSegment) layoutTurnout2.getConnectC(), setupB1TToTLogic.isSelected(),
                    setupB2TToTLogic.isSelected(), false, layoutTurnout2, layoutTurnout1);
        }
        if (setupC1TToTLogic.isSelected() || setupC2TToTLogic.isSelected()) {
            setLogicTToT(c1TToTHead, (TrackSegment) layoutTurnout1.getConnectB(), c2TToTHead,
                    (TrackSegment) layoutTurnout1.getConnectC(), setupC1TToTLogic.isSelected(),
                    setupC2TToTLogic.isSelected(), true, layoutTurnout1, layoutTurnout2);
        }
        if (setupD1TToTLogic.isSelected() || setupD2TToTLogic.isSelected()) {
            setLogicTToT(d1TToTHead, (TrackSegment) layoutTurnout1.getConnectB(), d2TToTHead,
                    (TrackSegment) layoutTurnout1.getConnectC(), setupD1TToTLogic.isSelected(),
                    setupD2TToTLogic.isSelected(), false, layoutTurnout1, layoutTurnout2);
        }
        //link the two turnouts
        layoutTurnout1.setLinkedTurnoutName(turnout2ComboBox.getSelectedItemDisplayName());
        layoutTurnout1.setLinkType(LayoutTurnout.THROAT_TO_THROAT);
        layoutTurnout2.setLinkedTurnoutName(turnout1ComboBox.getSelectedItemDisplayName());
        layoutTurnout2.setLinkType(LayoutTurnout.THROAT_TO_THROAT);
        //finish up
        setSignalsAtThroatToThroatTurnoutsOpenFlag = false;
        setSignalsAtThroatToThroatTurnoutsFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }   //setTToTSignalsDonePressed

    private boolean getTToTSignalHeadInformation() {
        a1TToTHead = getSignalHeadFromEntry(a1TToTSignalHeadComboBox, true, setSignalsAtThroatToThroatTurnoutsFrame);
        if (a1TToTHead == null) {
            return false;
        }
        a2TToTHead = getSignalHeadFromEntry(a2TToTSignalHeadComboBox, false, setSignalsAtThroatToThroatTurnoutsFrame);
        b1TToTHead = getSignalHeadFromEntry(b1TToTSignalHeadComboBox, true, setSignalsAtThroatToThroatTurnoutsFrame);
        if (b1TToTHead == null) {
            return false;
        }
        b2TToTHead = getSignalHeadFromEntry(b2TToTSignalHeadComboBox, false, setSignalsAtThroatToThroatTurnoutsFrame);
        c1TToTHead = getSignalHeadFromEntry(c1TToTSignalHeadComboBox, true, setSignalsAtThroatToThroatTurnoutsFrame);
        if (c1TToTHead == null) {
            return false;
        }
        c2TToTHead = getSignalHeadFromEntry(c2TToTSignalHeadComboBox, false, setSignalsAtThroatToThroatTurnoutsFrame);
        d1TToTHead = getSignalHeadFromEntry(d1TToTSignalHeadComboBox, true, setSignalsAtThroatToThroatTurnoutsFrame);
        if (d1TToTHead == null) {
            return false;
        }
        d2TToTHead = getSignalHeadFromEntry(d2TToTSignalHeadComboBox, false, setSignalsAtThroatToThroatTurnoutsFrame);
        return true;
    }

    private void placeA1TToT(String signalHeadName) {
        //place head near the continuing track of turnout 1
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutTurnout1.getCoordsB();
        Point2D coordsCenter = layoutTurnout1.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        Point2D delta = new Point2D.Double(0.0, -shift);

        delta = MathUtil.rotateDEG(delta, bDirDEG);
        Point2D where = MathUtil.add(coordsB, delta);
        setSignalHeadOnPanel(bDirDEG, signalHeadName, where);
    }

    private void placeA2TToT(String signalHeadName) {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutTurnout1.getCoordsB();
        Point2D coordsCenter = layoutTurnout1.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        Point2D delta = new Point2D.Double(2.0 * shift, -shift);

        delta = MathUtil.rotateDEG(delta, bDirDEG);
        Point2D where = MathUtil.add(coordsB, delta);
        setSignalHeadOnPanel(bDirDEG, signalHeadName, where);
    }

    private void placeB1TToT(String signalHeadName) {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutTurnout1.getCoordsB();
        Point2D coordsC = layoutTurnout1.getCoordsC();
        Point2D coordsCenter = layoutTurnout1.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        double cDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsC, coordsCenter));
        double diffDirDEG = MathUtil.diffAngleDEG(cDirDEG, bDirDEG);
        double shiftX = 0.0;
        if (diffDirDEG >= 0.0) {
            shiftX += shift * Math.cos(Math.toRadians(diffDirDEG));
        }
        Point2D delta = new Point2D.Double(shiftX, -shift);

        delta = MathUtil.rotateDEG(delta, cDirDEG);
        Point2D where = MathUtil.add(coordsC, delta);
        setSignalHeadOnPanel(cDirDEG, signalHeadName, where);
    }

    private void placeB2TToT(String signalHeadName) {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutTurnout1.getCoordsB();
        Point2D coordsC = layoutTurnout1.getCoordsC();
        Point2D coordsCenter = layoutTurnout1.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        double cDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsC, coordsCenter));
        double diffDirDEG = MathUtil.diffAngleDEG(cDirDEG, bDirDEG);
        double shiftX = 2.0 * shift;
        if (diffDirDEG >= 0.0) {
            shiftX += shift * Math.cos(Math.toRadians(diffDirDEG));
        }
        Point2D delta = new Point2D.Double(shiftX, -shift);

        delta = MathUtil.rotateDEG(delta, cDirDEG);
        Point2D where = MathUtil.add(coordsC, delta);
        setSignalHeadOnPanel(cDirDEG, signalHeadName, where);
    }

    private void placeC1TToT(String signalHeadName) {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutTurnout2.getCoordsB();
        Point2D coordsCenter = layoutTurnout2.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        Point2D delta = new Point2D.Double(0.0, -shift);

        delta = MathUtil.rotateDEG(delta, bDirDEG);
        Point2D where = MathUtil.add(coordsB, delta);
        setSignalHeadOnPanel(bDirDEG, signalHeadName, where);
    }

    private void placeC2TToT(String signalHeadName) {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutTurnout2.getCoordsB();
        Point2D coordsCenter = layoutTurnout2.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        Point2D delta = new Point2D.Double(2.0 * shift, -shift);

        delta = MathUtil.rotateDEG(delta, bDirDEG);
        Point2D where = MathUtil.add(coordsB, delta);
        setSignalHeadOnPanel(bDirDEG, signalHeadName, where);
    }

    private void placeD1TToT(String signalHeadName) {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutTurnout2.getCoordsB();
        Point2D coordsC = layoutTurnout2.getCoordsC();
        Point2D coordsCenter = layoutTurnout2.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        double cDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsC, coordsCenter));
        double diffDirDEG = MathUtil.diffAngleDEG(cDirDEG, bDirDEG);
        double shiftX = 0.0;
        if (diffDirDEG >= 0.0) {
            shiftX += shift * Math.cos(Math.toRadians(diffDirDEG));
        }
        Point2D delta = new Point2D.Double(shiftX, -shift);

        delta = MathUtil.rotateDEG(delta, cDirDEG);
        Point2D where = MathUtil.add(coordsC, delta);
        setSignalHeadOnPanel(cDirDEG, signalHeadName, where);
    }

    private void placeD2TToT(String signalHeadName) {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutTurnout2.getCoordsB();
        Point2D coordsC = layoutTurnout2.getCoordsC();
        Point2D coordsCenter = layoutTurnout2.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        double cDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsC, coordsCenter));
        double diffDirDEG = MathUtil.diffAngleDEG(cDirDEG, bDirDEG);
        double shiftX = 2.0 * shift;
        if (diffDirDEG >= 0.0) {
            shiftX += shift * Math.cos(Math.toRadians(diffDirDEG));
        }
        Point2D delta = new Point2D.Double(shiftX, -shift);

        delta = MathUtil.rotateDEG(delta, cDirDEG);
        Point2D where = MathUtil.add(coordsC, delta);
        setSignalHeadOnPanel(cDirDEG, signalHeadName, where);
    }

    @SuppressWarnings("null")
    private void setLogicTToT(SignalHead head, TrackSegment track1, SignalHead secondHead, TrackSegment track2,
            boolean setup1, boolean setup2, boolean continuing,
            LayoutTurnout farTurnout, LayoutTurnout nearTurnout) {
        //initialize common components and ensure all is defined
        LayoutBlock connectorBlock = connectorTrack.getLayoutBlock();
        LayoutBlock nearTurnoutBlock = nearTurnout.getLayoutBlock();
        LayoutBlock farTurnoutBlock = farTurnout.getLayoutBlock();
        Sensor connectorOccupancy = null;
        if ((connectorBlock == null) || (nearTurnoutBlock == null) || (farTurnoutBlock == null)) {
            JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        connectorOccupancy = connectorBlock.getOccupancySensor();
        if (connectorOccupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{connectorBlock.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        //setup signal head for continuing track of far turnout (or both tracks of far turnout)
        if ((track1 == null) && setup1) {
            JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = null;
        SignalHead nextHead = null;
        if ((track1 != null) && setup1) {
            LayoutBlock block = track1.getLayoutBlock();
            if (block == null) {
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("InfoMessage6"),
                        Bundle.getMessage("MessageTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            occupancy = block.getOccupancySensor();
            if (occupancy == null) {
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("InfoMessage4",
                                new Object[]{block.getUserName()}),
                        Bundle.getMessage("MessageTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            nextHead = getNextSignalFromObject(track1, farTurnout,
                    head.getSystemName(), setSignalsAtThroatToThroatTurnoutsFrame);
            if ((nextHead == null) && (!reachedEndBumper())) {
                JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                        Bundle.getMessage("InfoMessage5",
                                new Object[]{block.getUserName()}),
                        Bundle.getMessage("MessageTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (secondHead != null) {
                //this head signals only the continuing track of the far turnout
                if (!initializeBlockBossLogic(head.getSystemName())) {
                    return;
                }
                logic.setMode(BlockBossLogic.TRAILINGMAIN);
                logic.setTurnout(farTurnout.getTurnout().getSystemName());
                logic.setSensor1(occupancy.getSystemName());
                if (occupancy != connectorOccupancy) {
                    logic.setSensor2(connectorOccupancy.getSystemName());
                }
                if (nextHead != null) {
                    logic.setWatchedSignal1(nextHead.getSystemName(), false);
                }
                if (auxSignal != null) {
                    logic.setWatchedSignal1Alt(auxSignal.getSystemName());
                }
                String nearSensorName = setupNearLogix(nearTurnout, continuing, head);
                addNearSensorToLogic(nearSensorName);
                finalizeBlockBossLogic();
            }
        }
        if ((secondHead != null) && !setup2) {
            return;
        }
        SignalHead savedAuxSignal = auxSignal;
        if (track2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block2 = track2.getLayoutBlock();
        if (block2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy2 = block2.getOccupancySensor();
        if (occupancy2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block2.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        SignalHead nextHead2 = null;
        if (secondHead != null) {
            nextHead2 = getNextSignalFromObject(track2,
                    farTurnout, secondHead.getSystemName(), setSignalsAtThroatToThroatTurnoutsFrame);
            if ((nextHead2 == null) && (!reachedEndBumper())) {
                JOptionPane.showMessageDialog(setSignalsAtThroatToThroatTurnoutsFrame,
                        Bundle.getMessage("InfoMessage5",
                                new Object[]{block2.getUserName()}),
                        Bundle.getMessage("MessageTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        if ((secondHead == null) && (track1 != null) && setup1) {
            if (!initializeBlockBossLogic(head.getSystemName())) {
                return;
            }
            logic.setMode(BlockBossLogic.FACING);
            logic.setTurnout(farTurnout.getTurnout().getSystemName());
            logic.setWatchedSensor1(occupancy.getSystemName());
            logic.setWatchedSensor2(occupancy2.getSystemName());
            logic.setSensor2(connectorOccupancy.getSystemName());
            if (nextHead != null) {
                logic.setWatchedSignal1(nextHead.getSystemName(), false);
            }
            if (savedAuxSignal != null) {
                logic.setWatchedSignal1Alt(savedAuxSignal.getSystemName());
            }
            if (nextHead2 != null) {
                logic.setWatchedSignal2(nextHead2.getSystemName());
            }
            if (auxSignal != null) {
                logic.setWatchedSignal2Alt(auxSignal.getSystemName());
            }
            String nearSensorName = setupNearLogix(nearTurnout, continuing, head);
            addNearSensorToLogic(nearSensorName);
            logic.setLimitSpeed2(true);
            finalizeBlockBossLogic();
        } else if ((secondHead != null) && setup2) {
            if (!initializeBlockBossLogic(secondHead.getSystemName())) {
                return;
            }
            logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
            logic.setTurnout(farTurnout.getTurnout().getSystemName());
            logic.setSensor1(occupancy2.getSystemName());
            if (occupancy2 != connectorOccupancy) {
                logic.setSensor2(connectorOccupancy.getSystemName());
            }
            if (nextHead2 != null) {
                logic.setWatchedSignal1(nextHead2.getSystemName(), false);
            }
            if (auxSignal != null) {
                logic.setWatchedSignal1Alt(auxSignal.getSystemName());
            }
            String nearSensorName = setupNearLogix(nearTurnout, continuing, head);
            addNearSensorToLogic(nearSensorName);
            logic.setLimitSpeed2(true);
            finalizeBlockBossLogic();
        }
    }   //setLogicTToT

    /*
	 * Sets up a Logix to set a sensor active if a turnout is set against
	 *		a track.  This routine creates an internal sensor for the purpose.
	 * Note: The sensor and logix are named IS or IX followed by TTT_X_HHH where
	 *	TTT is the system name of the turnout, X is either C or T depending
	 *		on "continuing", and HHH is the system name of the signal head.
	 * Note: If there is any problem, a string of "" is returned, and a warning
	 *	message is issued.
     */
    private String setupNearLogix(LayoutTurnout nearTurnout, boolean continuing,
            SignalHead head) {
        String turnoutName = nearTurnout.getTurnout().getSystemName();
        String namer = turnoutName + "_T_" + head.getSystemName();
        if (!continuing) {
            namer = turnoutName + "_C_" + head.getSystemName();
        }
        String sensorName = "IS" + namer;
        String logixName = "IX" + namer;
        try {
            InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        } catch (IllegalArgumentException ex) {
            log.error("Trouble creating sensor " + sensorName + " while setting up Logix.");
            return "";

        }
        if (InstanceManager.getDefault(LogixManager.class
        ).getBySystemName(logixName) == null) {
            //Logix does not exist, create it
            Logix x = InstanceManager.getDefault(LogixManager.class
            ).createNewLogix(logixName, "");
            if (x == null) {
                log.error("Trouble creating logix " + logixName + " while setting up signal logic.");
                return "";
            }
            String cName = x.getSystemName() + "C1";
            Conditional c = InstanceManager.getDefault(ConditionalManager.class
            ).
                    createNewConditional(cName, "");
            if (c == null) {
                log.error("Trouble creating conditional " + cName + " while setting up Logix.");
                return "";
            }
            Conditional.Type type = Conditional.Type.TURNOUT_THROWN;
            if (!continuing) {
                type = Conditional.Type.TURNOUT_CLOSED;
            }
            List<ConditionalVariable> variableList = c.getCopyOfStateVariables();
            variableList.add(new ConditionalVariable(false, Conditional.Operator.AND,
                    type, turnoutName, true));
            c.setStateVariables(variableList);
            List<ConditionalAction> actionList = c.getCopyOfActions();
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                    Conditional.Action.SET_SENSOR, sensorName,
                    Sensor.ACTIVE, ""));
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE,
                    Conditional.Action.SET_SENSOR, sensorName,
                    Sensor.INACTIVE, ""));
            c.setAction(actionList);		  //string data
            x.addConditional(cName, -1);
            x.activateLogix();
        }
        return sensorName;
    }   //setupNearLogix

    /*
	 * Adds the sensor specified to the open BlockBossLogic, provided it is not already there and
	 *	provided there is an open slot. If 'name' is null or empty, returns without doing anything.
     */
    private void addNearSensorToLogic(String name) {
        if ((name != null) && !name.isEmpty()) {
            //return if a sensor by this name is already present
            if ((logic.getSensor1() != null) && (logic.getSensor1().equals(name))) {
                return;
            }
            if ((logic.getSensor2() != null) && (logic.getSensor2().equals(name))) {
                return;
            }
            if ((logic.getSensor3() != null) && (logic.getSensor3().equals(name))) {
                return;
            }
            if ((logic.getSensor4() != null) && (logic.getSensor4().equals(name))) {
                return;
            }
            if ((logic.getSensor5() != null) && (logic.getSensor5().equals(name))) {
                return;
            }
            //add in the first available slot
            if (logic.getSensor1() == null) {
                logic.setSensor1(name);
            } else if (logic.getSensor2() == null) {
                logic.setSensor2(name);
            } else if (logic.getSensor3() == null) {
                logic.setSensor3(name);
            } else if (logic.getSensor4() == null) {
                logic.setSensor4(name);
            } else if (logic.getSensor5() == null) {
                logic.setSensor5(name);
            } else {
                log.error("Error - could not add sensor to SSL for signal head " + logic.getDrivenSignal());
            }
        }
    }

    /*=========================*\
    |* setSignalsAt3WayTurnout *|
    \*=========================*/
    /**
     * Tool to set signals at a three-way turnout, including placing the signal
     * icons and setup of signal logic for each signal head
     * <p>
     * This tool can only be accessed from the Tools menu. There is no access
     * from a turnout pop-up menu.
     * <p>
     * This tool requires a situation where two turnouts are connected to model
     * a 3-way turnout, with the throat of the second turnout connected to the
     * continuing leg of the first turnout by a very short track segment. The
     * actual length of the track segment is not tested. If this situation is
     * not found, and error message is sent to the user.
     * <p>
     * This tool assumes two turnouts connected with the throat of the second
     * turnout connected to the continuing leg of the first turnou, as used to
     * represent a 3-way turnout. The turnouts may be either left-handed, or
     * right-handed, or any pair of these. This tool also assumes that there are
     * no signals between the two turnouts. Signal heads are allowed/required at
     * the continuing leg of the second turnout, at each of the diverging legs,
     * and at the throat. At the throat, either one or three heads are provided
     * for. So four or six heads will be placed.
     * <p>
     * This tool assumes that each of the four tracks, the continuing, the two
     * diverging, and the throat is contained in a different block. The two
     * turnouts used to model the 3-way turnout must be in the same block.
     * Things work best if the two turnouts are in the same block as the track
     * connecting at the throat, or if the two turnouts are in their own
     * separate block, either works fine.
     */
    //operational variables for Set Signals at 3-Way Turnout tool
    private JmriJFrame setSignalsAt3WayTurnoutFrame = null;
    private boolean setSignalsAt3WayTurnoutOpenFlag = false;
    private boolean setSignalsAt3WayTurnoutFromMenuFlag = false;

    private JLabel turnoutANameLabel = null;
    private JLabel turnoutBNameLabel = null;

    private final NamedBeanComboBox<Turnout> turnoutAComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Turnout> turnoutBComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(),
            null, DisplayOptions.DISPLAYNAME);

    private final NamedBeanComboBox<SignalHead> a1_3WaySignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class
            ),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> a2_3WaySignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class
            ),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> a3_3WaySignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class
            ),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> b_3WaySignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class
            ),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> c_3WaySignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class
            ),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> d_3WaySignalHeadComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(SignalHeadManager.class
            ),
            null, DisplayOptions.DISPLAYNAME);

    private final JCheckBox setA13WayHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupA13WayLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setA23WayHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupA23WayLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setA33WayHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupA33WayLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setB3WayHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupB3WayLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setC3WayHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupC3WayLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setD3WayHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupD3WayLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JButton getSaved3WaySignalHeads = null;
    private JButton change3WaySignalIcon = null;
    private JButton set3WaySignalsDone = null;
    private JButton set3WaySignalsCancel = null;
    private LayoutTurnout layoutTurnoutA = null;
    private LayoutTurnout layoutTurnoutB = null;
    private Turnout turnoutA = null;
    private Turnout turnoutB = null;
    //private TrackSegment conTrack = null;
    private SignalHead a13WayHead = null;	//saved in A1 of Turnout A - Throat - continuing
    private SignalHead a23WayHead = null;	//saved in A2 of Turnout A - Throat - diverging A (optional)
    private SignalHead a33WayHead = null;	//saved in A3 of Turnout A - Throat - diverging B (optional)
    private SignalHead b3WayHead = null;	//saved in C1 of Turnout A - at diverging A
    private SignalHead c3WayHead = null;	//saved in B1 of Turnout B - at continuing
    private SignalHead d3WayHead = null;	//saved in C1 of Turnout B - at diverging B

    public void setSignalsAt3WayTurnoutFromMenu(
            @Nonnull String aName, @Nonnull String bName,
            @Nonnull MultiIconEditor theEditor, @Nonnull JFrame theFrame) {
        Turnout ta = InstanceManager.getDefault(jmri.TurnoutManager.class).getTurnout(aName);
        Turnout tb = InstanceManager.getDefault(jmri.TurnoutManager.class).getTurnout(bName);
        turnoutAComboBox.setSelectedItem(ta);
        turnoutBComboBox.setSelectedItem(tb);
        a1_3WaySignalHeadComboBox.setSelectedItem(null);
        a2_3WaySignalHeadComboBox.setSelectedItem(null);
        a3_3WaySignalHeadComboBox.setSelectedItem(null);
        b_3WaySignalHeadComboBox.setSelectedItem(null);
        c_3WaySignalHeadComboBox.setSelectedItem(null);
        d_3WaySignalHeadComboBox.setSelectedItem(null);
        setSignalsAt3WayTurnoutFromMenuFlag = true;
        setSignalsAt3WayTurnout(theEditor, theFrame);
        setSignalsAt3WayTurnoutFromMenuFlag = false;
    }

    public void setSignalsAt3WayTurnout(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        signalIconEditor = theEditor;
        signalFrame = theFrame;

        //Initialize if needed
        if (setSignalsAt3WayTurnoutFrame == null) {
            setSignalsAt3WayTurnoutOpenFlag = false;
            setSignalsAt3WayTurnoutFrame = new JmriJFrame(Bundle.getMessage("SignalsAt3WayTurnout"), false, true);
            oneFrameToRuleThemAll(setSignalsAt3WayTurnoutFrame);
            setSignalsAt3WayTurnoutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSignalsAt3WayTurnoutFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAt3WayTurnout", true);
            setSignalsAt3WayTurnoutFrame.setLocation(70, 30);
            Container theContentPane = setSignalsAt3WayTurnoutFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel1A = new JPanel(new FlowLayout());
            turnoutANameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("TurnoutAName")));
            panel1A.add(turnoutANameLabel);
            panel1A.add(turnoutAComboBox);
            turnoutAComboBox.setToolTipText(Bundle.getMessage("SignalsTurnoutNameHint"));
            theContentPane.add(panel1A);

            JPanel panel1B = new JPanel(new FlowLayout());
            turnoutBNameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("TurnoutBName")));
            panel1B.add(turnoutBNameLabel);
            panel1B.add(turnoutBComboBox);
            turnoutBComboBox.setToolTipText(Bundle.getMessage("SignalsTurnoutNameHint"));
            theContentPane.add(panel1B);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            //Provide for retrieval of names of previously saved signal heads

            JPanel panel2 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("SignalHeads"));
            panel2.add(shTitle);
            panel2.add(new JLabel("		"));
            panel2.add(getSaved3WaySignalHeads = new JButton(Bundle.getMessage("GetSaved")));
            getSaved3WaySignalHeads.addActionListener((ActionEvent e) -> {
                getSaved3WaySignals(e);
            });
            getSaved3WaySignalHeads.setToolTipText(Bundle.getMessage("GetSavedHint"));
            theContentPane.add(panel2);

            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel2a = new JPanel(new FlowLayout());
            panel2a.add(new JLabel("   "));
            panel2a.add(setPlaceAllHeads);
            setPlaceAllHeads.setToolTipText(Bundle.getMessage("PlaceAllHeadsHint"));
            setPlaceAllHeads.addActionListener((ActionEvent e) -> {
                boolean isSelected = setPlaceAllHeads.isSelected();
                //(de)select all checkboxes
                setA13WayHead.setSelected(isSelected);
                setA23WayHead.setSelected(isSelected);
                setA33WayHead.setSelected(isSelected);
                setB3WayHead.setSelected(isSelected);
                setC3WayHead.setSelected(isSelected);
                setD3WayHead.setSelected(isSelected);
            });
            panel2a.add(new JLabel("  "));
            panel2a.add(setupAllLogic);
            setupAllLogic.setToolTipText(Bundle.getMessage("SetAllLogicHint"));
            setupAllLogic.addActionListener((ActionEvent e) -> {
                boolean isSelected = setupAllLogic.isSelected();
                //(de)select all checkboxes
                setupA13WayLogic.setSelected(isSelected);
                setupA23WayLogic.setSelected(isSelected);
                setupA33WayLogic.setSelected(isSelected);
                setupB3WayLogic.setSelected(isSelected);
                setupC3WayLogic.setSelected(isSelected);
                setupD3WayLogic.setSelected(isSelected);
            });
            theContentPane.add(panel2a);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            //Signal heads located at turnout A
            JPanel panel20 = new JPanel(new FlowLayout());
            panel20.add(new JLabel(Bundle.getMessage("SignalLocated")
                    + " " + Bundle.getMessage("BeanNameTurnout") + " A "));
            theContentPane.add(panel20);

            JPanel panel21 = new JPanel(new FlowLayout());
            panel21.add(new JLabel(Bundle.getMessage("MakeLabel",
                    throatString + " - "
                    + continuingString)));
            panel21.add(a1_3WaySignalHeadComboBox);
            a1_3WaySignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));
            theContentPane.add(panel21);

            JPanel panel22 = new JPanel(new FlowLayout());
            panel22.add(new JLabel("   "));
            panel22.add(setA13WayHead);
            setA13WayHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel22.add(new JLabel("  "));
            panel22.add(setupA13WayLogic);
            setupA13WayLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel22);

            JPanel panel23 = new JPanel(new FlowLayout());
            panel23.add(new JLabel(Bundle.getMessage("MakeLabel",
                    throatString + " - "
                    + divergingAString)));
            panel23.add(a2_3WaySignalHeadComboBox);
            a2_3WaySignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));
            theContentPane.add(panel23);

            JPanel panel24 = new JPanel(new FlowLayout());
            panel24.add(new JLabel("   "));
            panel24.add(setA23WayHead);
            setA23WayHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel24.add(new JLabel("  "));
            panel24.add(setupA23WayLogic);
            setupA23WayLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel24);

            JPanel panel25 = new JPanel(new FlowLayout());
            panel25.add(new JLabel(Bundle.getMessage("MakeLabel",
                    throatString + " - "
                    + divergingBString)));
            panel25.add(a3_3WaySignalHeadComboBox);
            a3_3WaySignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));
            theContentPane.add(panel25);

            JPanel panel26 = new JPanel(new FlowLayout());
            panel26.add(new JLabel("   "));
            panel26.add(setA33WayHead);
            setA33WayHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel26.add(new JLabel("  "));
            panel26.add(setupA33WayLogic);
            setupA33WayLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel26);

            JPanel panel31 = new JPanel(new FlowLayout());
            panel31.add(new JLabel(Bundle.getMessage("MakeLabel",
                    divergingBString)));
            panel31.add(b_3WaySignalHeadComboBox);
            b_3WaySignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));
            theContentPane.add(panel31);

            JPanel panel32 = new JPanel(new FlowLayout());
            panel32.add(new JLabel("   "));
            panel32.add(setB3WayHead);
            setB3WayHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel32.add(new JLabel("  "));
            panel32.add(setupB3WayLogic);
            setupB3WayLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel32);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            //Signal heads located at turnout B

            JPanel panel40 = new JPanel(new FlowLayout());
            panel40.add(new JLabel(Bundle.getMessage("SignalLocated")
                    + " " + Bundle.getMessage("BeanNameTurnout") + " B "));
            theContentPane.add(panel40);

            JPanel panel41 = new JPanel(new FlowLayout());
            panel41.add(new JLabel(Bundle.getMessage("MakeLabel",
                    continuingString)));
            panel41.add(c_3WaySignalHeadComboBox);
            c_3WaySignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));
            theContentPane.add(panel41);

            JPanel panel42 = new JPanel(new FlowLayout());
            panel42.add(new JLabel("   "));
            panel42.add(setC3WayHead);
            setC3WayHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel42.add(new JLabel("  "));
            panel42.add(setupC3WayLogic);
            setupC3WayLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel42);

            JPanel panel43 = new JPanel(new FlowLayout());
            panel43.add(new JLabel(Bundle.getMessage("MakeLabel",
                    divergingBString)));
            panel43.add(d_3WaySignalHeadComboBox);
            d_3WaySignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));
            theContentPane.add(panel43);

            JPanel panel44 = new JPanel(new FlowLayout());
            panel44.add(new JLabel("   "));
            panel44.add(setD3WayHead);
            setD3WayHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel44.add(new JLabel("  "));
            panel44.add(setupD3WayLogic);
            setupD3WayLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel44);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            //buttons

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(change3WaySignalIcon = new JButton(Bundle.getMessage("ChangeSignalIcon")));
            change3WaySignalIcon.addActionListener((ActionEvent e) -> {
                signalFrame.setVisible(true);
            });
            change3WaySignalIcon.setToolTipText(Bundle.getMessage("ChangeSignalIconHint"));
            panel6.add(new JLabel("	 "));
            panel6.add(set3WaySignalsDone = new JButton(Bundle.getMessage("ButtonDone")));
            set3WaySignalsDone.addActionListener((ActionEvent e) -> {
                set3WaySignalsDonePressed(e);
            });
            set3WaySignalsDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(set3WaySignalsDone);
                rootPane.setDefaultButton(set3WaySignalsDone);
            });

            panel6.add(set3WaySignalsCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            set3WaySignalsCancel.addActionListener((ActionEvent e) -> {
                set3WaySignalsCancelPressed(e);
            });
            set3WaySignalsCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
            setSignalsAt3WayTurnoutFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    set3WaySignalsCancelPressed(null);
                }
            });
        }
        setPlaceAllHeads.setSelected(false);
        setupAllLogic.setSelected(false);

        turnoutAComboBox.setVisible(!setSignalsAt3WayTurnoutFromMenuFlag);
        turnoutBComboBox.setVisible(!setSignalsAt3WayTurnoutFromMenuFlag);
        if (setSignalsAt3WayTurnoutFromMenuFlag) {
            turnoutANameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameTurnout") + " A")
                    + turnoutAComboBox.getSelectedItemDisplayName());
            turnoutBNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameTurnout") + " B")
                    + turnoutBComboBox.getSelectedItemDisplayName());
            getSaved3WaySignals(null);
        } else {
            turnoutANameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("TurnoutAName")));
            turnoutBNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("TurnoutBName")));
        }

        if (!setSignalsAt3WayTurnoutOpenFlag) {
            setSignalsAt3WayTurnoutFrame.setPreferredSize(null);
            setSignalsAt3WayTurnoutFrame.pack();
            setSignalsAt3WayTurnoutOpenFlag = true;
        }
        setSignalsAt3WayTurnoutFrame.setVisible(true);
    }   //setSignalsAt3WayTurnout

    private void getSaved3WaySignals(ActionEvent a) {
        if (!get3WayTurnoutInformation()) {
            return;
        }
        a1_3WaySignalHeadComboBox.setSelectedItem(layoutTurnoutA.getSignalA1());
        a2_3WaySignalHeadComboBox.setSelectedItem(layoutTurnoutA.getSignalA2());
        a3_3WaySignalHeadComboBox.setSelectedItem(layoutTurnoutA.getSignalA3());
        b_3WaySignalHeadComboBox.setSelectedItem(layoutTurnoutA.getSignalC1());
        c_3WaySignalHeadComboBox.setSelectedItem(layoutTurnoutB.getSignalB1());
        d_3WaySignalHeadComboBox.setSelectedItem(layoutTurnoutB.getSignalC1());
    }

    private void set3WaySignalsCancelPressed(ActionEvent a) {
        setSignalsAt3WayTurnoutOpenFlag = false;
        setSignalsAt3WayTurnoutFrame.setVisible(false);
    }

    private boolean get3WayTurnoutInformation() {
        int type = 0;
        Object connect = null;
        turnoutA = null;
        turnoutB = null;
        layoutTurnoutA = null;
        layoutTurnoutB = null;

        String str = turnoutAComboBox.getSelectedItemDisplayName();
        if ((str == null) || str.isEmpty()) {
            //turnout A not entered, test turnout B
            str = turnoutBComboBox.getSelectedItemDisplayName();
            if ((str == null) || str.isEmpty()) {
                //no entries in turnout fields
                JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                        Bundle.getMessage("SignalsError1"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            turnoutB = InstanceManager.turnoutManagerInstance().getTurnout(str);
            if (turnoutB == null) {
                JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                        Bundle.getMessage("SignalsError2",
                                new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            String uname = turnoutB.getUserName();
            if ((uname == null) || uname.isEmpty()
                    || !uname.equals(str)) {
                turnoutBComboBox.setSelectedItem(turnoutB);
            }
            layoutTurnoutB = getLayoutTurnoutFromTurnout(turnoutB, false, str, setSignalsAt3WayTurnoutFrame);
            if (layoutTurnoutB == null) {
                return false;
            }
            //have turnout B and layout turnout B - look for turnout A
            connectorTrack = (TrackSegment) layoutTurnoutB.getConnectA();
            if (connectorTrack == null) {
                //Inform user of error, and terminate
                JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                        Bundle.getMessage("SignalsError19"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            type = connectorTrack.getType1();
            connect = connectorTrack.getConnect1();
            if (connect == layoutTurnoutB) {
                type = connectorTrack.getType2();
                connect = connectorTrack.getConnect2();
            }
            if ((type != LayoutTrack.TURNOUT_B) || (connect == null)) {
                //Not two turnouts connected as required by a single Track Segment
                //Inform user of error and terminate
                JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                        Bundle.getMessage("SignalsError19"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            layoutTurnoutA = (LayoutTurnout) connect;
            turnoutA = layoutTurnoutA.getTurnout();
            if (turnoutA == null) {
                JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                        Bundle.getMessage("SignalsError19"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            turnoutAComboBox.setSelectedItem(turnoutA);
        } else {
            //something was entered in the turnout A field
            turnoutA = InstanceManager.turnoutManagerInstance().getTurnout(str);
            if (turnoutA == null) {
                JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                        Bundle.getMessage("SignalsError2",
                                new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            String uname = turnoutA.getUserName();
            if ((uname == null) || uname.isEmpty()
                    || !uname.equals(str)) {
                turnoutAComboBox.setSelectedItem(turnoutA);
            }
            //have turnout A - get corresponding layoutTurnout
            layoutTurnoutA = getLayoutTurnoutFromTurnout(turnoutA, false, str, setSignalsAt3WayTurnoutFrame);
            if (layoutTurnoutA == null) {
                return false;
            }
            turnoutAComboBox.setSelectedItem(layoutTurnoutA.getTurnout());
            //have turnout A and layout turnout A - was something entered for turnout B
            str = turnoutBComboBox.getSelectedItemDisplayName();
            if ((str == null) || str.isEmpty()) {
                //no entry for turnout B
                connectorTrack = (TrackSegment) layoutTurnoutA.getConnectB();
                if (connectorTrack == null) {
                    //Inform user of error, and terminate
                    JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                            Bundle.getMessage("SignalsError19"),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                type = connectorTrack.getType1();
                connect = connectorTrack.getConnect1();
                if (connect == layoutTurnoutA) {
                    type = connectorTrack.getType2();
                    connect = connectorTrack.getConnect2();
                }
                if ((type != LayoutTrack.TURNOUT_A) || (connect == null)) {
                    //Not two turnouts connected with the throat of B connected to the continuing of A
                    //	  by a single Track Segment.  Inform user of error and terminat.e
                    JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                            Bundle.getMessage("SignalsError19"),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                layoutTurnoutB = (LayoutTurnout) connect;
                turnoutB = layoutTurnoutB.getTurnout();
                if (turnoutB == null) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                            Bundle.getMessage("SignalsError19"),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                turnoutBComboBox.setSelectedItem(turnoutB);
            } else {
                //turnout B entered also
                turnoutB = InstanceManager.turnoutManagerInstance().getTurnout(str);
                if (turnoutB == null) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                            Bundle.getMessage("SignalsError2",
                                    new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                uname = turnoutB.getUserName();
                if ((uname == null) || uname.isEmpty()
                        || !uname.equals(str)) {
                    turnoutBComboBox.setSelectedItem(turnoutB);
                }
                layoutTurnoutB = getLayoutTurnoutFromTurnout(turnoutB, false, str, setSignalsAt3WayTurnoutFrame);
                if (layoutTurnoutB == null) {
                    return false;
                }
                turnoutBComboBox.setSelectedItem(layoutTurnoutB.getTurnout());
                //check that layout turnout A and layout turnout B are connected as required
                if (layoutTurnoutA.getConnectB() != layoutTurnoutB.getConnectA()) {
                    //Not two turnouts connected as required by a single Track Segment
                    //Inform user of error and terminate
                    JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                            Bundle.getMessage("SignalsError19"),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                connectorTrack = (TrackSegment) layoutTurnoutA.getConnectB();
            }
        }
        return true;
    }   //get3WayTurnoutInformation

    private void set3WaySignalsDonePressed(ActionEvent a) {
        //process turnout names
        if (!get3WayTurnoutInformation()) {
            return;
        }
        //process signal head names
        if (!get3WaySignalHeadInformation()) {
            return;
        }
        //place signals as requested at turnout A
        String signalHeadName = a1_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setA13WayHead.isSelected()) {
            if (isHeadOnPanel(a13WayHead)
                    && (a13WayHead != getHeadFromName(layoutTurnoutA.getSignalA1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnoutA.getSignalA1Name());
                place3WayThroatContinuing();
                removeAssignment(a13WayHead);
                layoutTurnoutA.setSignalA1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(a13WayHead, layoutTurnoutA);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(a13WayHead)
                        && isHeadAssignedAnywhere(a13WayHead)) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnoutA.getSignalA1Name());
                    removeAssignment(a13WayHead);
                    layoutTurnoutA.setSignalA1Name(signalHeadName);
                }
                //} else if (assigned != A1) {
                //need to figure out what to do in this case.
            }
        }

        signalHeadName = a2_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if ((setA23WayHead.isSelected()) && (a23WayHead != null)) {
            if (isHeadOnPanel(a23WayHead)
                    && (a23WayHead != getHeadFromName(layoutTurnoutA.getSignalA2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnoutA.getSignalA2Name());
                place3WayThroatDivergingA();
                removeAssignment(a23WayHead);
                layoutTurnoutA.setSignalA2Name(signalHeadName);
                needRedraw = true;
            }
        } else if (a23WayHead != null) {
            int assigned = isHeadAssignedHere(a23WayHead, layoutTurnoutA);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(a23WayHead)
                        && isHeadAssignedAnywhere(a23WayHead)) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnoutA.getSignalA2Name());
                    removeAssignment(a23WayHead);
                    layoutTurnoutA.setSignalA2Name(signalHeadName);
                }
                //} else if (assigned != A2) {
                //need to figure out what to do in this case.
            }
        } else {  //a23WayHead is always null here
            removeSignalHeadFromPanel(layoutTurnoutA.getSignalA2Name());
            layoutTurnoutA.setSignalA2Name("");
        }

        signalHeadName = a3_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if ((setA33WayHead.isSelected()) && (a33WayHead != null)) {
            if (isHeadOnPanel(a33WayHead)
                    && (a33WayHead != getHeadFromName(layoutTurnoutA.getSignalA3Name()))) {
                JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnoutA.getSignalA3Name());
                place3WayThroatDivergingB();
                removeAssignment(a33WayHead);
                layoutTurnoutA.setSignalA3Name(signalHeadName);
                needRedraw = true;
            }
        } else if (a33WayHead != null) {
            int assigned = isHeadAssignedHere(a33WayHead, layoutTurnoutA);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(a33WayHead)
                        && isHeadAssignedAnywhere(a33WayHead)) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnoutA.getSignalA3Name());
                    removeAssignment(a33WayHead);
                    layoutTurnoutA.setSignalA3Name(signalHeadName);
                }
                //} else if (assigned != A3) {
                //need to figure out what to do in this case.
            }
        } else {  //a23WayHead is always null here
            removeSignalHeadFromPanel(layoutTurnoutA.getSignalA3Name());
            layoutTurnoutA.setSignalA3Name("");
        }

        signalHeadName = b_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setB3WayHead.isSelected()) {
            if (isHeadOnPanel(b3WayHead)
                    && (b3WayHead != getHeadFromName(layoutTurnoutA.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnoutA.getSignalC1Name());
                place3WayDivergingA();
                removeAssignment(b3WayHead);
                layoutTurnoutA.setSignalC1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(b3WayHead, layoutTurnoutA);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(b3WayHead)
                        && isHeadAssignedAnywhere(b3WayHead)) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnoutA.getSignalC1Name());
                    removeAssignment(b3WayHead);
                    layoutTurnoutA.setSignalC1Name(signalHeadName);
                }
                //} else if (assigned != A1) {
                //need to figure out what to do in this case.
            }
        }

        //place signals as requested at Turnout C
        signalHeadName = c_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setC3WayHead.isSelected()) {
            if (isHeadOnPanel(c3WayHead)
                    && (c3WayHead != getHeadFromName(layoutTurnoutB.getSignalB1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnoutB.getSignalB1Name());
                place3WayContinuing();
                removeAssignment(c3WayHead);
                layoutTurnoutB.setSignalB1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(c3WayHead, layoutTurnoutB);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(c3WayHead)
                        && isHeadAssignedAnywhere(c3WayHead)) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnoutB.getSignalB1Name());
                    removeAssignment(c3WayHead);
                    layoutTurnoutB.setSignalB1Name(signalHeadName);
                }
                //} else if (assigned != B1) {
                //need to figure out what to do in this case.
            }
        }

        signalHeadName = d_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setD3WayHead.isSelected()) {
            if (isHeadOnPanel(d3WayHead)
                    && (d3WayHead != getHeadFromName(layoutTurnoutB.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutTurnoutB.getSignalC1Name());
                place3WayDivergingB();
                removeAssignment(d3WayHead);
                layoutTurnoutB.setSignalC1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(d3WayHead, layoutTurnoutB);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(d3WayHead)
                        && isHeadAssignedAnywhere(d3WayHead)) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnoutB.getSignalC1Name());
                    removeAssignment(d3WayHead);
                    layoutTurnoutB.setSignalC1Name(signalHeadName);
                }
                //} else if (assigned != C1) {
                //need to figure out what to do in this case.
            }
        }
        //setup Logic if requested and enough information is available
        if (setupA13WayLogic.isSelected()) {
            set3WayLogicThroatContinuing();
        }
        if ((a23WayHead != null) && setupA23WayLogic.isSelected()) {
            set3WayLogicThroatDivergingA();
        }
        if ((a33WayHead != null) && setupA33WayLogic.isSelected()) {
            set3WayLogicThroatDivergingB();
        }
        if (setupB3WayLogic.isSelected()) {
            set3WayLogicDivergingA();
        }
        if (setupC3WayLogic.isSelected()) {
            set3WayLogicContinuing();
        }
        if (setupD3WayLogic.isSelected()) {
            set3WayLogicDivergingB();
        }
        //link the two turnouts
        signalHeadName = turnoutBComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        layoutTurnoutA.setLinkedTurnoutName(signalHeadName);
        layoutTurnoutA.setLinkType(LayoutTurnout.FIRST_3_WAY);
        signalHeadName = turnoutAComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        layoutTurnoutB.setLinkedTurnoutName(signalHeadName);
        layoutTurnoutB.setLinkType(LayoutTurnout.SECOND_3_WAY);
        //finish up
        setSignalsAt3WayTurnoutOpenFlag = false;
        setSignalsAt3WayTurnoutFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }   //set3WaySignalsDonePressed

    private boolean get3WaySignalHeadInformation() {
        a13WayHead = getSignalHeadFromEntry(a1_3WaySignalHeadComboBox, true, setSignalsAt3WayTurnoutFrame);
        if (a13WayHead == null) {
            return false;
        }
        a23WayHead = getSignalHeadFromEntry(a2_3WaySignalHeadComboBox, false, setSignalsAt3WayTurnoutFrame);
        a33WayHead = getSignalHeadFromEntry(a3_3WaySignalHeadComboBox, false, setSignalsAt3WayTurnoutFrame);
        if (((a23WayHead == null) && (a33WayHead != null)) || ((a33WayHead == null)
                && (a23WayHead != null))) {
            return false;
        }
        b3WayHead = getSignalHeadFromEntry(b_3WaySignalHeadComboBox, true, setSignalsAt3WayTurnoutFrame);
        if (b3WayHead == null) {
            return false;
        }
        c3WayHead = getSignalHeadFromEntry(c_3WaySignalHeadComboBox, true, setSignalsAt3WayTurnoutFrame);
        if (c3WayHead == null) {
            return false;
        }
        d3WayHead = getSignalHeadFromEntry(d_3WaySignalHeadComboBox, true, setSignalsAt3WayTurnoutFrame);
        if (d3WayHead == null) {
            return false;
        }
        return true;
    }

    private void place3WayThroatContinuing() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = a1_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsA = layoutTurnoutA.getCoordsA();
        Point2D coordsCenter = layoutTurnoutA.getCoordsCenter();

        double aDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsA, coordsCenter));
        Point2D delta = new Point2D.Double(-shift, -shift);

        delta = MathUtil.rotateDEG(delta, aDirDEG);
        Point2D where = MathUtil.add(coordsA, delta);
        setSignalHeadOnPanel(aDirDEG, signalHeadName, where);
    }

    private void place3WayThroatDivergingA() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = a2_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsA = layoutTurnoutA.getCoordsA();
        Point2D coordsCenter = layoutTurnoutA.getCoordsCenter();

        double aDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsA, coordsCenter));
        Point2D delta = new Point2D.Double(+shift, -shift);

        delta = MathUtil.rotateDEG(delta, aDirDEG);
        Point2D where = MathUtil.add(coordsA, delta);
        setSignalHeadOnPanel(aDirDEG, signalHeadName, where);
    }

    private void place3WayThroatDivergingB() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = a3_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsA = layoutTurnoutA.getCoordsA();
        Point2D coordsCenter = layoutTurnoutA.getCoordsCenter();

        double aDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsA, coordsCenter));
        Point2D delta = new Point2D.Double(+3.0 * shift, -shift);

        delta = MathUtil.rotateDEG(delta, aDirDEG);
        Point2D where = MathUtil.add(coordsA, delta);
        setSignalHeadOnPanel(aDirDEG, signalHeadName, where);
    }

    private void place3WayDivergingA() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = b_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutTurnoutA.getCoordsB();
        Point2D coordsC = layoutTurnoutA.getCoordsC();
        Point2D coordsCenter = layoutTurnoutA.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        double cDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsC, coordsCenter));
        double diffDirDEG = MathUtil.diffAngleDEG(cDirDEG, bDirDEG);
        double shiftX = shift;
        if (diffDirDEG >= 0.0) {
            shiftX += shift * Math.cos(Math.toRadians(diffDirDEG));
        }
        Point2D delta = new Point2D.Double(shiftX, -shift);

        delta = MathUtil.rotateDEG(delta, cDirDEG);
        Point2D where = MathUtil.add(coordsC, delta);
        setSignalHeadOnPanel(cDirDEG, signalHeadName, where);
    }

    private void place3WayContinuing() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = c_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutTurnoutB.getCoordsB();
        Point2D coordsC = layoutTurnoutB.getCoordsC();
        Point2D coordsCenter = layoutTurnoutB.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        double cDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsC, coordsCenter));
        double diffDirDEG = MathUtil.diffAngleDEG(cDirDEG, bDirDEG);
        double shiftX = shift;
        if (diffDirDEG >= 0.0) {
            shiftX += shift * Math.cos(Math.toRadians(diffDirDEG));
        }
        Point2D delta = new Point2D.Double(shiftX, -shift);

        delta = MathUtil.rotateDEG(delta, bDirDEG);
        Point2D where = MathUtil.add(coordsB, delta);
        setSignalHeadOnPanel(bDirDEG, signalHeadName, where);
    }

    private void place3WayDivergingB() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = d_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsC = layoutTurnoutB.getCoordsC();
        Point2D coordsB = layoutTurnoutB.getCoordsB();
        Point2D coordsCenter = layoutTurnoutB.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        double cDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsC, coordsCenter));
        double diffDirDEG = MathUtil.diffAngleDEG(cDirDEG, bDirDEG);
        double shiftX = shift;
        if (diffDirDEG >= 0.0) {
            shiftX += shift * Math.cos(Math.toRadians(diffDirDEG));
        }
        Point2D delta = new Point2D.Double(shiftX, -shift);

        delta = MathUtil.rotateDEG(delta, cDirDEG);
        Point2D where = MathUtil.add(coordsC, delta);
        setSignalHeadOnPanel(cDirDEG, signalHeadName, where);
    }

    private void set3WayLogicThroatContinuing() {
        TrackSegment track = (TrackSegment) layoutTurnoutB.getConnectB();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = a1_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutB,
                signalHeadName, setSignalsAt3WayTurnoutFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (a23WayHead != null) {
            //set up logic for continuing head with 3 heads at throat
            if (!initializeBlockBossLogic(signalHeadName)) {
                return;
            }
            logic.setMode(BlockBossLogic.TRAILINGMAIN);
            logic.setTurnout(turnoutB.getSystemName());
            logic.setSensor1(occupancy.getSystemName());
            if (nextHead != null) {
                logic.setWatchedSignal1(nextHead.getSystemName(), false);
            }
            if (auxSignal != null) {
                logic.setWatchedSignal1Alt(auxSignal.getSystemName());
            }
            String nearSensorName = setupNearLogix(layoutTurnoutA, true, a13WayHead);
            addNearSensorToLogic(nearSensorName);
            finalizeBlockBossLogic();
            return;
        }
        //only one head at the throat
        JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                Bundle.getMessage("InfoMessage9"),
                Bundle.getMessage("MessageTitle"),
                JOptionPane.INFORMATION_MESSAGE);
        return;
    }   //set3WayLogicThroatContinuing

    private void set3WayLogicThroatDivergingA() {
        TrackSegment track = (TrackSegment) layoutTurnoutA.getConnectC();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = a2_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutA,
                signalHeadName, setSignalsAt3WayTurnoutFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!initializeBlockBossLogic(signalHeadName)) {
            return;
        }
        logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
        logic.setTurnout(turnoutA.getSystemName());
        logic.setSensor1(occupancy.getSystemName());
        if (nextHead != null) {
            logic.setWatchedSignal1(nextHead.getSystemName(), false);
        }
        if (auxSignal != null) {
            logic.setWatchedSignal1Alt(auxSignal.getSystemName());
        }
        if (!layoutTurnoutA.isMainlineC()) {
            logic.setLimitSpeed2(true);
        }
        finalizeBlockBossLogic();
    }   //set3WayLogicThroatDivergingA

    private void set3WayLogicThroatDivergingB() {
        TrackSegment track = (TrackSegment) layoutTurnoutB.getConnectC();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = a3_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutB,
                signalHeadName, setSignalsAt3WayTurnoutFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!initializeBlockBossLogic(signalHeadName)) {
            return;
        }
        logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
        logic.setTurnout(turnoutB.getSystemName());
        logic.setSensor1(occupancy.getSystemName());
        if (nextHead != null) {
            logic.setWatchedSignal1(nextHead.getSystemName(), false);
        }
        if (auxSignal != null) {
            logic.setWatchedSignal1Alt(auxSignal.getSystemName());
        }
        String nearSensorName = setupNearLogix(layoutTurnoutA, true, a33WayHead);
        addNearSensorToLogic(nearSensorName);
        if (!layoutTurnoutB.isMainlineC()) {
            logic.setLimitSpeed2(true);
        }
        finalizeBlockBossLogic();
    }   //set3WayLogicThroatDivergingB

    private void set3WayLogicDivergingA() {
        TrackSegment track = (TrackSegment) layoutTurnoutA.getConnectA();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = b_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutA,
                signalHeadName, setSignalsAt3WayTurnoutFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!initializeBlockBossLogic(signalHeadName)) {
            return;
        }
        logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
        logic.setTurnout(turnoutA.getSystemName());
        logic.setSensor1(occupancy.getSystemName());
        if (nextHead != null) {
            logic.setWatchedSignal1(nextHead.getSystemName(), false);
        }
        if (auxSignal != null) {
            logic.setWatchedSignal1Alt(auxSignal.getSystemName());
        }
        if (!layoutTurnoutA.isMainlineC()) {
            logic.setLimitSpeed2(true);
        }
        finalizeBlockBossLogic();
    }   //set3WayLogicDivergingA

    private void set3WayLogicContinuing() {
        TrackSegment track = (TrackSegment) layoutTurnoutA.getConnectA();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = c_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutA,
                signalHeadName, setSignalsAt3WayTurnoutFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!initializeBlockBossLogic(signalHeadName)) {
            return;
        }
        logic.setMode(BlockBossLogic.TRAILINGMAIN);
        logic.setTurnout(turnoutB.getSystemName());
        logic.setSensor1(occupancy.getSystemName());
        if (nextHead != null) {
            logic.setWatchedSignal1(nextHead.getSystemName(), false);
        }
        if (auxSignal != null) {
            logic.setWatchedSignal1Alt(auxSignal.getSystemName());
        }
        String nearSensorName = setupNearLogix(layoutTurnoutA, true, c3WayHead);
        addNearSensorToLogic(nearSensorName);
        if (!layoutTurnoutB.isMainlineB()) {
            logic.setLimitSpeed2(true);
        }
        finalizeBlockBossLogic();
    }   //set3WayLogicContinuing

    private void set3WayLogicDivergingB() {
        TrackSegment track = (TrackSegment) layoutTurnoutA.getConnectA();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = d_3WaySignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutA,
                signalHeadName, setSignalsAt3WayTurnoutFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAt3WayTurnoutFrame,
                    Bundle.getMessage("InfoMessage5",
                            new Object[]{block.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!initializeBlockBossLogic(signalHeadName)) {
            return;
        }
        logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
        logic.setTurnout(turnoutB.getSystemName());
        logic.setSensor1(occupancy.getSystemName());
        if (nextHead != null) {
            logic.setWatchedSignal1(nextHead.getSystemName(), false);
        }
        if (auxSignal != null) {
            logic.setWatchedSignal1Alt(auxSignal.getSystemName());
        }
        String nearSensorName = setupNearLogix(layoutTurnoutA, true, d3WayHead);
        addNearSensorToLogic(nearSensorName);
        if (!layoutTurnoutB.isMainlineC()) {
            logic.setLimitSpeed2(true);
        }
        finalizeBlockBossLogic();
    }   //set3WayLogicDivergingB

    /*===========================*\
    |* setSensorsAtBlockBoundary *|
    \*===========================*/
    //
    //The following is for placement of sensors and signal masts at points around the layout
    //
    //This section deals with assigning a sensor to a specific boundary point
    BeanDetails<Sensor> westBoundSensor;
    BeanDetails<Sensor> eastBoundSensor;

    private JmriJFrame setSensorsAtBlockBoundaryFrame = null;
    private boolean setSensorsAtBlockBoundaryOpenFlag = false;
    private boolean setSensorsAtBlockBoundaryFromMenuFlag = false;

    private JButton getAnchorSavedSensors = null;
    private JButton changeSensorAtBoundaryIcon = null;
    private JButton setSensorsAtBlockBoundaryDone = null;
    private JButton setSensorsAtBlockBoundaryCancel = null;

    private JFrame sensorFrame = null;
    private MultiIconEditor sensorIconEditor = null;

    JPanel sensorBlockPanel = new JPanel(new FlowLayout());

    public void setSensorsAtBlockBoundaryFromMenu(@Nonnull PositionablePoint p,
            @Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        boundary = p;
        block1IDComboBox.setSelectedItem(boundary.getConnect1().getLayoutBlock().getBlock());
        if (boundary.getConnect2() == null) {
            block2IDComboBox.setSelectedItem(boundary.getConnect1().getLayoutBlock().getBlock());
        } else {
            block2IDComboBox.setSelectedItem(boundary.getConnect2().getLayoutBlock().getBlock());
        }
        setSensorsAtBlockBoundaryFromMenuFlag = true;
        setSensorsAtBlockBoundary(theEditor, theFrame);
        setSensorsAtBlockBoundaryFromMenuFlag = false;
    }

    //TODO: Add to Tools menu?
    public void setSensorsAtBlockBoundary(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        sensorIconEditor = theEditor;
        sensorFrame = theFrame;

        //Initialize if needed
        if (setSensorsAtBlockBoundaryFrame == null) {
            setSensorsAtBlockBoundaryOpenFlag = false;

            westBoundSensor = new BeanDetails<>("Sensor", InstanceManager.sensorManagerInstance());
            eastBoundSensor = new BeanDetails<>("Sensor", InstanceManager.sensorManagerInstance());

            setSensorsAtBlockBoundaryFrame = new JmriJFrame(Bundle.getMessage("SensorsAtBoundary"), false, true);
            oneFrameToRuleThemAll(setSensorsAtBlockBoundaryFrame);
            setSensorsAtBlockBoundaryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//         setSensorsAtBlockBoundaryFrame.addHelpMenu("package.jmri.jmrit.display.SetSensorsAtBoundary", true);
            setSensorsAtBlockBoundaryFrame.setLocation(70, 30);
            Container theContentPane = setSensorsAtBlockBoundaryFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

            JPanel panel11 = new JPanel(new FlowLayout());
            block1NameLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " 1 - "
                    + Bundle.getMessage("Name")));
            panel11.add(block1NameLabel);

            panel11.add(block1IDComboBox);
            block1IDComboBox.setToolTipText(Bundle.getMessage("SensorsBlockNameHint"));
            header.add(panel11);

            JPanel panel12 = new JPanel(new FlowLayout());
            block2NameLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " 2 - "
                    + Bundle.getMessage("Name")));
            panel12.add(block2NameLabel);

            panel12.add(block2IDComboBox);
            block2IDComboBox.setToolTipText(Bundle.getMessage("SensorsBlockNameHint"));
            header.add(panel12);

            header.add(new JSeparator(JSeparator.HORIZONTAL));
            theContentPane.add(header);

            JPanel panel2 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("Sensors"));
            panel2.add(shTitle);
            panel2.add(new JLabel("	  "));
            panel2.add(getAnchorSavedSensors = new JButton(Bundle.getMessage("GetSaved")));
            getAnchorSavedSensors.addActionListener((ActionEvent e) -> {
                getSavedAnchorSensors(e);
            });
            getAnchorSavedSensors.setToolTipText(Bundle.getMessage("GetSavedHint"));
            theContentPane.add(panel2);

            sensorBlockPanel.setLayout(new GridLayout(0, 1));
            theContentPane.add(sensorBlockPanel);

            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(changeSensorAtBoundaryIcon = new JButton(Bundle.getMessage("ChangeSensorIcon")));
            changeSensorAtBoundaryIcon.addActionListener((ActionEvent e) -> {
                sensorFrame.setVisible(true);
            });
            changeSensorAtBoundaryIcon.setToolTipText(Bundle.getMessage("ChangeSensorIconHint"));
            panel6.add(new JLabel("	 "));
            panel6.add(setSensorsAtBlockBoundaryDone = new JButton(Bundle.getMessage("ButtonDone")));
            setSensorsAtBlockBoundaryDone.addActionListener((ActionEvent e) -> {
                setSensorsAtBlockBoundaryDonePressed(e);
            });
            setSensorsAtBlockBoundaryDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(setSensorsAtBlockBoundaryDone);
                rootPane.setDefaultButton(setSensorsAtBlockBoundaryDone);
            });

            panel6.add(setSensorsAtBlockBoundaryCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setSensorsAtBlockBoundaryCancel.addActionListener((ActionEvent e) -> {
                setSensorsAtBlockBoundaryCancelPressed(e);
            });
            setSensorsAtBlockBoundaryCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6, BorderLayout.SOUTH);
            setSensorsAtBlockBoundaryFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSensorsAtBlockBoundaryCancelPressed(null);
                }
            });
        }

        sensorBlockPanel.removeAll();

        if (boundary.getType() != PositionablePoint.END_BUMPER) {
            eastBoundSensor.setBoundaryTitle(Bundle.getMessage("East/SouthBound"));
            if ((setSensorsAtBlockBoundaryFromMenuFlag) && (boundary.getType() == PositionablePoint.ANCHOR)) {
                if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                    eastBoundSensor.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect2().getLayoutBlock().getDisplayName());
                } else {
                    eastBoundSensor.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
                }
            }
            eastBoundSensor.getDetailsPanel().setBackground(new Color(255, 255, 200));
            sensorBlockPanel.add(eastBoundSensor.getDetailsPanel());

            westBoundSensor.setBoundaryTitle(Bundle.getMessage("West/NorthBound"));
            if (setSensorsAtBlockBoundaryFromMenuFlag) {
                if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                    westBoundSensor.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
                } else {
                    westBoundSensor.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect2().getLayoutBlock().getDisplayName());
                }
            }
            westBoundSensor.getDetailsPanel().setBackground(new Color(200, 255, 255));
            sensorBlockPanel.add(westBoundSensor.getDetailsPanel());
        } else {
            if (setSensorsAtBlockBoundaryFromMenuFlag) {
                if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                    eastBoundSensor.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    eastBoundSensor.getDetailsPanel().setBackground(new Color(200, 255, 255));
                    sensorBlockPanel.add(eastBoundSensor.getDetailsPanel());
                } else {
                    westBoundSensor.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    westBoundSensor.getDetailsPanel().setBackground(new Color(255, 255, 200));
                    sensorBlockPanel.add(westBoundSensor.getDetailsPanel());
                }
            }
        }

        block1IDComboBox.setVisible(!setSensorsAtBlockBoundaryFromMenuFlag);
        block2IDComboBox.setVisible(!setSensorsAtBlockBoundaryFromMenuFlag);

        if (setSensorsAtBlockBoundaryFromMenuFlag) {
            block1NameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " 1 "
                    + Bundle.getMessage("Name"))
                    + " " + boundary.getConnect1().getLayoutBlock().getId());
            if (boundary.getConnect2() != null) {
                block2NameLabel.setText(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("BeanNameBlock") + " 2 "
                        + Bundle.getMessage("Name"))
                        + " " + boundary.getConnect2().getLayoutBlock().getId());
            }
            getSavedAnchorSensors(null);
        } else {
            block1NameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("Name") + " 1 "
                    + Bundle.getMessage("Name")));
            block2NameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("Name") + " 2  "
                    + Bundle.getMessage("Name")));
        }
        //boundary should never be null... however, just in case...
        boolean enable = ((boundary != null) && (boundary.getType() != PositionablePoint.END_BUMPER));
        block2NameLabel.setVisible(enable);

        if (!setSensorsAtBlockBoundaryOpenFlag) {
            setSensorsAtBlockBoundaryFrame.setPreferredSize(null);
            setSensorsAtBlockBoundaryFrame.pack();
            setSensorsAtBlockBoundaryOpenFlag = true;
        }
        setSensorsAtBlockBoundaryFrame.setVisible(true);
    }   //setSensorsAtBlockBoundary

    /**
     * Returns the Sensor corresponding to an entry field in the specified
     * dialog. This also takes care of UpperCase and trimming of leading and
     * trailing blanks. If entry is required, and no entry is present, and error
     * message is sent. An error message also results if a sensor head with the
     * entered name is not found in the SensorTable.
     */
    @CheckReturnValue
    public Sensor getSensorFromEntry(@CheckForNull String sensorName,
            boolean requireEntry,
            @Nonnull JmriJFrame frame) {
        String str = sensorName;
        if ((str == null) || str.isEmpty()) {
            if (requireEntry) {
                JOptionPane.showMessageDialog(frame, Bundle.getMessage("SensorsError5"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }
        Sensor head = InstanceManager.sensorManagerInstance().getSensor(str);
        if (head == null) {
            JOptionPane.showMessageDialog(frame,
                    Bundle.getMessage("SensorsError4",
                            new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return (head);
    }

    @CheckReturnValue
    public SensorIcon getSensorIcon(@Nonnull String sensorName) {
        SensorIcon l = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                "resources/icons/smallschematics/tracksegments/circuit-error.gif"), layoutEditor);
        l.setIcon("SensorStateActive", sensorIconEditor.getIcon(0));
        l.setIcon("SensorStateInactive", sensorIconEditor.getIcon(1));
        l.setIcon("BeanStateInconsistent", sensorIconEditor.getIcon(2));
        l.setIcon("BeanStateUnknown", sensorIconEditor.getIcon(3));
        l.setSensor(sensorName);
        return l;
    }

    /**
     * Returns true if the specified Sensor is assigned to an object on the
     * panel, regardless of whether an icon is displayed or not. With sensors we
     * NO LONGER (4.11.2) allow the same sensor to be allocated in both
     * directions.
     *
     * @param sensor The sensor to be checked.
     * @return true if the sensor is currently assigned someplace.
     */
    public boolean isSensorAssignedAnywhere(@Nonnull Sensor sensor) {
        boolean result = false;

        //check positionable points
        for (PositionablePoint po : layoutEditor.getPositionablePoints()) {
            if (po.getEastBoundSensor() == sensor) {
                result = true;
                break;
            }
            if (po.getWestBoundSensor() == sensor) {
                result = true;
                break;
            }
        }
        if (!result) {
            //check turnouts and slips
            for (LayoutTurnout to : layoutEditor.getLayoutTurnoutsAndSlips()) {
                if (whereIsSensorAssigned(sensor, to) != LayoutTurnout.NONE) {
                    result = true;
                    break;
                }
            }
        }
        if (!result) {
            //check level crossings
            for (LevelXing x : layoutEditor.getLevelXings()) {
                if ((x.getSensorA() != null) && x.getSensorA() == sensor) {
                    result = true;
                    break;
                }
                if ((x.getSensorB() != null) && x.getSensorB() == sensor) {
                    result = true;
                    break;
                }
                if ((x.getSensorC() != null) && x.getSensorC() == sensor) {
                    result = true;
                    break;
                }
                if ((x.getSensorD() != null) && x.getSensorD() == sensor) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }   //isSensorAssignedAnywhere

    private int whereIsSensorAssigned(Sensor sensor, LayoutTurnout lTurnout) {
        int result = LayoutTurnout.NONE;

        if (sensor != null && lTurnout != null) {
            String sName = sensor.getSystemName();
            String uName = sensor.getUserName();

            String name = lTurnout.getSensorAName();
            if (!name.isEmpty() && name.equals(uName) || name.equals(sName)) {
                return LayoutTurnout.POINTA1;
            }
            name = lTurnout.getSensorBName();
            if (!name.isEmpty() && name.equals(uName) || name.equals(sName)) {
                return LayoutTurnout.POINTA2;
            }
            name = lTurnout.getSensorCName();
            if (!name.isEmpty() && name.equals(uName) || name.equals(sName)) {
                return LayoutTurnout.POINTA3;
            }
            name = lTurnout.getSensorDName();
            if (!name.isEmpty() && name.equals(uName) || name.equals(sName)) {
                return LayoutTurnout.POINTB1;
            }
        }
        return result;
    }   //whereIsSensorAssigned

    /**
     * Display an error dialog.
     *
     * @param sensor The sensor that is already assigned.
     */
    void sensorAssignedElseWhere(@Nonnull Sensor sensor) {
        JOptionPane.showMessageDialog(setSensorsAtBlockBoundaryFrame,
                Bundle.getMessage("SensorsError6", //NOI18N
                        new Object[]{sensor.getDisplayName()}),
                Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);  //NOI18N
    }

    /**
     * Removes the assignment of the specified Sensor to either a turnout, a
     * positionable point, or a level crossing wherever it is assigned. Removes
     * any NX Pairs that use the sensor.
     * <p>
     * If the NX deletes fail due to Conditional references or user deny, the
     * assignment is not deleted. No additional notification is necessary since
     * they have already been notified or made a choice to not continue.
     * <p>
     * @param sensor The sensor to be removed.
     * @return true if the sensor has been removed.
     */
    public boolean removeSensorAssignment(@Nonnull Sensor sensor) {
        log.trace("Remove sensor assignment at block boundary for '{}'", sensor.getDisplayName());  //NOI18N
        if (!InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).deleteNxPair(sensor)) {
            log.trace("Removal of NX pairs for sensor '{}' failed", sensor.getDisplayName());  //NOI18N
            return false;
        }
        for (PositionablePoint po : layoutEditor.getPositionablePoints()) {
            if (po.getEastBoundSensor() == sensor) {
                po.setEastBoundSensor(null);
            }
            if (po.getWestBoundSensor() == sensor) {
                po.setWestBoundSensor(null);
            }
        }

        for (LayoutTurnout to : layoutEditor.getLayoutTurnoutsAndSlips()) {
            if (to.getSensorA() == sensor) {
                to.setSensorA(null);
            }
            if (to.getSensorB() == sensor) {
                to.setSensorB(null);
            }
            if (to.getSensorC() == sensor) {
                to.setSensorC(null);
            }
            if (to.getSensorD() == sensor) {
                to.setSensorD(null);
            }
        }

        for (LevelXing x : layoutEditor.getLevelXings()) {
            if (x.getSensorA() == sensor) {
                x.setSensorAName(null);
            }
            if (x.getSensorB() == sensor) {
                x.setSensorBName(null);
            }
            if (x.getSensorC() == sensor) {
                x.setSensorCName(null);
            }
            if (x.getSensorD() == sensor) {
                x.setSensorDName(null);
            }
        }

        return true;
    }   //removeSensorAssignment

    /**
     * Removes the Sensor icon from the panel and from assignment to any
     * turnout, positionable point, or level crossing.
     *
     * @param sensor The sensor whose icon and references are to be removed.
     * @return true if the removal was successful.
     */
    public boolean removeSensorFromPanel(@Nonnull Sensor sensor) {
        log.trace("Remove sensor icon and assignment for '{}'", sensor.getDisplayName());  //NOI18N
        if (!removeSensorAssignment(sensor)) {
            return false;
        }

        SensorIcon h = null;
        int index = -1;
        for (int i = 0; (i < layoutEditor.sensorList.size()) && (index == -1); i++) {
            h = layoutEditor.sensorList.get(i);
            if (h.getSensor() == sensor) {
                index = i;
            }
        }
        if ((h != null) && (index != -1)) {
            layoutEditor.sensorList.remove(index);
            h.remove();
            h.dispose();
            needRedraw = true;
        }
        return true;
    }

    private void getSavedAnchorSensors(ActionEvent a) {
        if (!getSimpleBlockInformation()) {
            return;
        }
        eastBoundSensor.setTextField(boundary.getEastBoundSensorName());
        westBoundSensor.setTextField(boundary.getWestBoundSensorName());

        if (boundary.getType() != PositionablePoint.END_BUMPER) {
            if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                eastBoundSensor.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect2().getLayoutBlock().getDisplayName());
            } else {
                eastBoundSensor.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
            }
            if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                westBoundSensor.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
            } else {
                westBoundSensor.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect2().getLayoutBlock().getDisplayName());
            }
        } else {
            if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                westBoundSensor.setBoundaryLabelText(Bundle.getMessage("EndOfBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
            } else {
                eastBoundSensor.setBoundaryLabelText(Bundle.getMessage("EndOfBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
            }
        }

        setSensorsAtBlockBoundaryFrame.setPreferredSize(null);
        setSensorsAtBlockBoundaryFrame.pack();
    }

    private void setSensorsAtBlockBoundaryCancelPressed(ActionEvent a) {
        setSensorsAtBlockBoundaryOpenFlag = false;
        setSensorsAtBlockBoundaryFrame.setVisible(false);
    }

    private void setSensorsAtBlockBoundaryDonePressed(ActionEvent a) {
        log.trace("setSensorsAtBlockBoundaryDonePressed");  //NOI18N
        if (!getSimpleBlockInformation()) {
            return;
        }

        Sensor eastSensor = getSensorFromEntry(eastBoundSensor.getText(), false, setSensorsAtBlockBoundaryFrame);
        Sensor westSensor = getSensorFromEntry(westBoundSensor.getText(), false, setSensorsAtBlockBoundaryFrame);
        Sensor currEastSensor = InstanceManager.sensorManagerInstance().getSensor(boundary.getEastBoundSensorName());
        Sensor currWestSensor = InstanceManager.sensorManagerInstance().getSensor(boundary.getWestBoundSensorName());

        if (log.isTraceEnabled()) {
            log.trace("current sensors: east = {}, west = {}", //NOI18N
                    (currEastSensor == null) ? "- none- " : currEastSensor.getDisplayName(), //NOI18N
                    (currWestSensor == null) ? "- none- " : currWestSensor.getDisplayName());  //NOI18N
            log.trace("new sensors: east = {}, west = {}", //NOI18N
                    (eastSensor == null) ? "- none- " : eastSensor.getDisplayName(), //NOI18N
                    (westSensor == null) ? "- none- " : westSensor.getDisplayName());  //NOI18N
        }

        if (eastSensor == null) {
            if (currEastSensor != null && removeSensorFromPanel(currEastSensor)) {
                boundary.setEastBoundSensor(null);
            }
        } else if (eastBoundSensor != null) {
            setBoundarySensor(eastSensor, currEastSensor, eastBoundSensor, "East");  //NOI18N
        }

        if (westSensor == null) {
            if (currWestSensor != null && removeSensorFromPanel(currWestSensor)) {
                boundary.setWestBoundSensor(null);
            }
        } else if (westBoundSensor != null) {
            setBoundarySensor(westSensor, currWestSensor, westBoundSensor, "West");  //NOI18N
        }

        setSensorsAtBlockBoundaryOpenFlag = false;
        setSensorsAtBlockBoundaryFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }

    /**
     * Attached a sensor to the block boundary positional point.
     *
     * @since 4.11.2
     * @param newSensor  The sensor that is being added.
     * @param currSensor The sensor that might already be there, otherwise null.
     * @param beanDetail The BeanDetails object that contains the supporting
     *                   data.
     * @param direction  The direction, East or West.
     */
    void setBoundarySensor(Sensor newSensor, Sensor currSensor,
            BeanDetails<Sensor> beanDetail, String direction) {
        if (currSensor == null) {
            if (!isSensorAssignedAnywhere(newSensor)) {
                log.trace("Add sensor '{}'", newSensor.getDisplayName());  //NOI18N
                if (direction.equals("West")) {  //NOI18N
                    boundary.setWestBoundSensor(beanDetail.getText());
                } else {
                    boundary.setEastBoundSensor(beanDetail.getText());
                }
                if (beanDetail.addToPanel()) {
                    log.trace("Add icon for sensor '{}'", newSensor.getDisplayName());  //NOI18N
                    if (direction.equals("West")) {  //NOI18N
                        placeWestBoundIcon(getSensorIcon(beanDetail.getText()),
                                beanDetail.isRightSelected(), 0.0);
                    } else {
                        placeEastBoundIcon(getSensorIcon(beanDetail.getText()),
                                beanDetail.isRightSelected(), 0.0);
                    }
                    needRedraw = true;
                }
            } else {
                sensorAssignedElseWhere(newSensor);
            }
        } else if (currSensor == newSensor) {
            if (beanDetail.addToPanel()) {
                if (!isSensorOnPanel(newSensor)) {
                    log.trace("Add icon for existing sensor '{}'", newSensor.getDisplayName());  //NOI18N
                    if (direction.equals("West")) {  //NOI18N
                        placeWestBoundIcon(getSensorIcon(beanDetail.getText()),
                                beanDetail.isRightSelected(), 0.0);
                    } else {
                        placeEastBoundIcon(getSensorIcon(beanDetail.getText()),
                                beanDetail.isRightSelected(), 0.0);
                    }
                    needRedraw = true;
                }
            }
        } else {
            if (!isSensorAssignedAnywhere(newSensor)) {
                if (removeSensorFromPanel(currSensor)) {
                    log.trace("Replace sensor '{}' with sensor '{}'", //NOI18N
                            currSensor.getDisplayName(), newSensor.getDisplayName());
                    if (direction.equals("West")) {  //NOI18N
                        boundary.setWestBoundSensor(beanDetail.getText());
                    } else {
                        boundary.setEastBoundSensor(beanDetail.getText());
                    }
                    if (beanDetail.addToPanel()) {
                        log.trace("Add icon for replacement sensor '{}'", //NOI18N
                                newSensor.getDisplayName());
                        if (direction.equals("West")) {  //NOI18N
                            placeWestBoundIcon(getSensorIcon(beanDetail.getText()),
                                    beanDetail.isRightSelected(), 0.0);
                        } else {
                            placeEastBoundIcon(getSensorIcon(beanDetail.getText()),
                                    beanDetail.isRightSelected(), 0.0);
                        }
                        needRedraw = true;
                    }
                }
            } else {
                sensorAssignedElseWhere(newSensor);
            }
        }
    }

    public boolean isSensorOnPanel(@Nonnull Sensor sensor) {
        for (SensorIcon s : layoutEditor.sensorList) {
            if (s.getSensor() == sensor) {
                return true;
            }
        }
        return false;
    }

    /*===============================*\
    |* setSignalMastsAtBlockBoundary *|
    \*===============================*/
    private JmriJFrame setSignalMastsAtBlockBoundaryFrame = null;
    private boolean setSignalMastsAtBlockBoundaryOpenFlag = false;
    private boolean setSignalMastsAtBlockBoundaryFromMenuFlag = false;

    private JButton getAnchorSavedSignalMasts = null;
    private JButton setSignalMastsAtBlockBoundaryDone = null;
    private JButton setSignalMastsAtBlockBoundaryCancel = null;

    BeanDetails<SignalMast> eastSignalMast;
    BeanDetails<SignalMast> westSignalMast;

    JPanel signalMastBlockPanel = new JPanel(new FlowLayout());

    public void setSignalMastsAtBlockBoundaryFromMenu(
            @Nonnull PositionablePoint p) {
        boundary = p;
        block1IDComboBox.setSelectedItem(boundary.getConnect1().getLayoutBlock().getBlock());
        if (boundary.getType() != PositionablePoint.END_BUMPER) {
            block2IDComboBox.setSelectedItem(boundary.getConnect2().getLayoutBlock().getBlock());
        } else {
            block2IDComboBox.setSelectedItem(boundary.getConnect1().getLayoutBlock().getBlock());
        }
        setSignalMastsAtBlockBoundaryFromMenuFlag = true;
        setSignalMastsAtBlockBoundary();
        setSignalMastsAtBlockBoundaryFromMenuFlag = false;
    }

    //TODO: Add to Tools menu?
    public void setSignalMastsAtBlockBoundary() {

        //Initialize if needed
        if (setSignalMastsAtBlockBoundaryFrame == null) {
            setSignalMastsAtBlockBoundaryOpenFlag = false;

            eastSignalMast = new BeanDetails<>("SignalMast", //NOI18N
                    InstanceManager.getDefault(SignalMastManager.class));
            westSignalMast = new BeanDetails<>("SignalMast", //NOI18N
                    InstanceManager.getDefault(SignalMastManager.class));

            setSignalMastsAtBlockBoundaryFrame = new JmriJFrame(Bundle.getMessage("SignalMastsAtBoundary"), false, true);
            oneFrameToRuleThemAll(setSignalMastsAtBlockBoundaryFrame);
            setSignalMastsAtBlockBoundaryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            //setSignalMastsAtBlockBoundaryFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalMastsAtBoundary", true);
            setSignalMastsAtBlockBoundaryFrame.setLocation(70, 30);
            Container theContentPane = setSignalMastsAtBlockBoundaryFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

            //Create the block 1 label and combo box
            JPanel panel11 = new JPanel(new FlowLayout());
            block1NameLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " 1 "
                    + Bundle.getMessage("Name")));
            panel11.add(block1NameLabel);
            panel11.add(block1IDComboBox);
            block1IDComboBox.setToolTipText(Bundle.getMessage("SignalMastsBlockNameHint"));
            header.add(panel11);

            //Create the block 2 label and combo box, visibility will be controlled later
            block2NameLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " 2 "
                    + Bundle.getMessage("Name")));
            block2IDComboBox.setToolTipText(Bundle.getMessage("SignalMastsBlockNameHint"));

            JPanel panel12 = new JPanel(new FlowLayout());
            panel12.add(block2NameLabel);
            panel12.add(block2IDComboBox);
            header.add(panel12);

            header.add(new JSeparator(JSeparator.HORIZONTAL));
            theContentPane.add(header);

            JPanel panel2 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("SignalMasts"));
            panel2.add(shTitle);
            panel2.add(new JLabel("	  "));
            panel2.add(getAnchorSavedSignalMasts = new JButton(Bundle.getMessage("GetSaved")));
            getAnchorSavedSignalMasts.addActionListener((ActionEvent e) -> {
                getSavedAnchorSignalMasts(e);
            });
            getAnchorSavedSignalMasts.setToolTipText(Bundle.getMessage("GetSavedHint"));
            theContentPane.add(panel2);

            signalMastBlockPanel.setLayout(new GridLayout(0, 1));
            theContentPane.add(signalMastBlockPanel);

            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(setSignalMastsAtBlockBoundaryDone = new JButton(Bundle.getMessage("ButtonDone")));
            setSignalMastsAtBlockBoundaryDone.addActionListener((ActionEvent e) -> {
                setSignalMastsAtBlockBoundaryDonePressed(e);
            });
            setSignalMastsAtBlockBoundaryDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(setSignalMastsAtBlockBoundaryDone);
                rootPane.setDefaultButton(setSignalMastsAtBlockBoundaryDone);
            });

            panel6.add(setSignalMastsAtBlockBoundaryCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setSignalMastsAtBlockBoundaryCancel.addActionListener((ActionEvent e) -> {
                setSignalMastsAtBlockBoundaryCancelPressed(e);
            });
            setSignalMastsAtBlockBoundaryCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
            setSignalMastsAtBlockBoundaryFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSignalMastsAtBlockBoundaryCancelPressed(null);
                }
            });
        }

        eastSignalMast.getCombo().setExcludedItems(new HashSet<>());
        westSignalMast.getCombo().setExcludedItems(new HashSet<>());
        signalMastBlockPanel.removeAll();

        if (boundary.getType() != PositionablePoint.END_BUMPER) {   //Anchor points and Edge Connectors
            eastSignalMast.setBoundaryTitle(Bundle.getMessage("East/SouthBound"));
            if (boundary.getType() == PositionablePoint.EDGE_CONNECTOR) {
                eastSignalMast.setBoundaryTitle(Bundle.getMessage("West/NorthBound"));
            }
            if (setSignalMastsAtBlockBoundaryFromMenuFlag) {
                if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                    eastSignalMast.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect2().getLayoutBlock().getDisplayName());
                } else {
                    eastSignalMast.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
                }
            }
            eastSignalMast.getDetailsPanel().setBackground(new Color(255, 255, 200));
            signalMastBlockPanel.add(eastSignalMast.getDetailsPanel());

            westSignalMast.setBoundaryTitle(Bundle.getMessage("West/NorthBound"));
            if (boundary.getType() == PositionablePoint.EDGE_CONNECTOR) {
                westSignalMast.setBoundaryTitle(Bundle.getMessage("East/SouthBound"));
            }
            if (setSignalMastsAtBlockBoundaryFromMenuFlag) {
                if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                    westSignalMast.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
                } else {
                    westSignalMast.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect2().getLayoutBlock().getDisplayName());
                }
            }
            westSignalMast.getDetailsPanel().setBackground(new Color(200, 255, 255));
            signalMastBlockPanel.add(westSignalMast.getDetailsPanel());
        } else {    //End Bumper
            if (setSignalMastsAtBlockBoundaryFromMenuFlag) {
                if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                    eastSignalMast.setBoundaryLabelText(Bundle.getMessage("EndOfBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    eastSignalMast.getDetailsPanel().setBackground(new Color(200, 255, 255));
                    signalMastBlockPanel.add(eastSignalMast.getDetailsPanel());
                } else {
                    westSignalMast.setBoundaryLabelText(Bundle.getMessage("EndOfBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    westSignalMast.getDetailsPanel().setBackground(new Color(255, 255, 200));
                    signalMastBlockPanel.add(westSignalMast.getDetailsPanel());
                }
            }
        }
        block1IDComboBox.setVisible(!setSignalMastsAtBlockBoundaryFromMenuFlag);
        block2IDComboBox.setVisible(!setSignalMastsAtBlockBoundaryFromMenuFlag);

        if (setSignalMastsAtBlockBoundaryFromMenuFlag) {
            block1NameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " 1 "
                    + Bundle.getMessage("Name"))
                    + " " + boundary.getConnect1().getLayoutBlock().getId());
            if (boundary.getConnect2() != null) {
                block2NameLabel.setText(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("BeanNameBlock") + " 2 "
                        + Bundle.getMessage("Name"))
                        + " " + boundary.getConnect2().getLayoutBlock().getId());
                block2NameLabel.setVisible(true);
            } else {
                block2NameLabel.setVisible(false);
            }
            getSavedAnchorSignalMasts(null);
        }

        if (!setSignalMastsAtBlockBoundaryOpenFlag) {
            setSignalMastsAtBlockBoundaryFrame.setPreferredSize(null);
            setSignalMastsAtBlockBoundaryFrame.pack();
            setSignalMastsAtBlockBoundaryOpenFlag = true;
        }
        refreshSignalMastAtBoundaryComboBox();
        setSignalMastsAtBlockBoundaryFrame.setVisible(true);
    }

    /**
     * Returns the SignalMast corresponding to an entry field in the specified
     * dialog. This also takes care of UpperCase and trimming of leading and
     * trailing blanks. If entry is required, and no entry is present, and error
     * message is sent. An error message also results if a signalMast head with
     * the entered name is not found in the SignalMastTable.
     */
    @CheckReturnValue
    public SignalMast getSignalMastFromEntry(@CheckForNull String signalMastName,
            boolean requireEntry,
            @Nonnull JmriJFrame frame) {
        String str = signalMastName;
        if ((str == null) || str.isEmpty()) {
            if (requireEntry) {
                JOptionPane.showMessageDialog(frame, Bundle.getMessage("SignalMastsError5"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
            return null;

        }
        SignalMast head = InstanceManager.getDefault(SignalMastManager.class
        ).getSignalMast(str);
        if (head == null) {
            JOptionPane.showMessageDialog(frame,
                    Bundle.getMessage("SignalMastsError4",
                            new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return (head);
    }

    /**
     * Returns true if the specified SignalMast is assigned to an object on the
     * panel, regardless of whether an icon is displayed or not
     */
    public boolean isSignalMastAssignedAnywhere(@Nonnull SignalMast signalMast) {
        boolean result = false;
        for (PositionablePoint po : layoutEditor.getPositionablePoints()) {
            if ((po.getEastBoundSignalMast() != null) && po.getEastBoundSignalMast() == signalMast) {
                result = true;
                break;
            }
            if ((po.getWestBoundSignalMast() != null) && po.getWestBoundSignalMast() == signalMast) {
                result = true;
                break;
            }
        }

        if (!result) {
            for (LayoutTurnout to : layoutEditor.getLayoutTurnoutsAndSlips()) {
                if ((to.getSignalAMast() != null) && to.getSignalAMast() == signalMast) {
                    result = true;
                    break;
                }
                if ((to.getSignalBMast() != null) && to.getSignalBMast() == signalMast) {
                    result = true;
                    break;
                }
                if ((to.getSignalCMast() != null) && to.getSignalCMast() == signalMast) {
                    result = true;
                    break;
                }
                if ((to.getSignalDMast() != null) && to.getSignalDMast() == signalMast) {
                    result = true;
                    break;
                }
            }
        }

        if (!result) {
            for (LevelXing x : layoutEditor.getLevelXings()) {
                if ((x.getSignalAMast() != null) && x.getSignalAMast() == signalMast) {
                    result = true;
                    break;
                }
                if ((x.getSignalBMast() != null) && x.getSignalBMast() == signalMast) {
                    result = true;
                    break;
                }
                if ((x.getSignalCMast() != null) && x.getSignalCMast() == signalMast) {
                    result = true;
                    break;
                }
                if ((x.getSignalDMast() != null) && x.getSignalDMast() == signalMast) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Removes the assignment of the specified SignalMast to either a turnout, a
     * positionable point, or a level crossing wherever it is assigned
     */
    public void removeSignalMastAssignment(@CheckForNull SignalMast signalMast) {
        if (signalMast == null) {
            return;
        }

        for (PositionablePoint po : layoutEditor.getPositionablePoints()) {
            if ((po.getEastBoundSignalMast() != null) && po.getEastBoundSignalMast() == signalMast) {
                po.setEastBoundSignalMast(null);
            }
            if ((po.getWestBoundSignalMast() != null) && po.getWestBoundSignalMast() == signalMast) {
                po.setWestBoundSignalMast(null);
            }
        }
        for (LayoutTurnout to : layoutEditor.getLayoutTurnoutsAndSlips()) {
            if ((to.getSignalAMast() != null) && to.getSignalAMast() == signalMast) {
                to.setSignalAMast(null);
            }
            if ((to.getSignalBMast() != null) && to.getSignalBMast() == signalMast) {
                to.setSignalBMast(null);
            }
            if ((to.getSignalCMast() != null) && to.getSignalCMast() == signalMast) {
                to.setSignalCMast(null);
            }
            if ((to.getSignalDMast() != null) && to.getSignalDMast() == signalMast) {
                to.setSignalDMast(null);
            }
        }

        for (LevelXing x : layoutEditor.getLevelXings()) {
            if ((x.getSignalAMast() != null) && x.getSignalAMast() == signalMast) {
                x.setSignalAMast(null);
            }

            if ((x.getSignalBMast() != null) && x.getSignalBMast() == signalMast) {
                x.setSignalBMast(null);
            }

            if ((x.getSignalCMast() != null) && x.getSignalCMast() == signalMast) {
                x.setSignalCMast(null);
            }

            if ((x.getSignalDMast() != null) && x.getSignalDMast() == signalMast) {
                x.setSignalDMast(null);
            }
        }
    }

    /**
     * Removes the SignalMast with the specified name from the panel and from
     * assignment to any turnout, positionable point, or level crossing
     */
    public void removeSignalMastFromPanel(@Nonnull SignalMast signalMast) {
        removeSignalMastAssignment(signalMast);
        SignalMastIcon h = null;
        int index = -1;
        for (int i = 0; (i < layoutEditor.signalMastList.size()) && (index == -1); i++) {
            h = layoutEditor.signalMastList.get(i);
            if ((h != null) && (h.getSignalMast() == signalMast)) {
                index = i;
            }
        }
        if ((h != null) && (index != -1)) {
            layoutEditor.signalMastList.remove(index);
            h.remove();
            h.dispose();
            needRedraw = true;
        }
    }

    private void getSavedAnchorSignalMasts(ActionEvent a) {
        if (!getSimpleBlockInformation()) {
            return;
        }

        eastSignalMast.setTextField(boundary.getEastBoundSignalMastName());
        westSignalMast.setTextField(boundary.getWestBoundSignalMastName());

        if (boundary.getType() != PositionablePoint.END_BUMPER) {
            if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                eastSignalMast.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect2().getLayoutBlock().getDisplayName());
            } else {
                eastSignalMast.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
            }
            if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                westSignalMast.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
            } else {
                westSignalMast.setBoundaryLabelText(Bundle.getMessage("ProtectingBlock") + boundary.getConnect2().getLayoutBlock().getDisplayName());
            }
        } else {
            if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                westSignalMast.setBoundaryLabelText(Bundle.getMessage("EndOfBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
            } else {
                eastSignalMast.setBoundaryLabelText(Bundle.getMessage("EndOfBlock") + boundary.getConnect1().getLayoutBlock().getDisplayName());
            }
        }
        setSignalMastsAtBlockBoundaryFrame.setPreferredSize(null);
        setSignalMastsAtBlockBoundaryFrame.pack();
    }

    private void setSignalMastsAtBlockBoundaryCancelPressed(ActionEvent a) {
        setSignalMastsAtBlockBoundaryOpenFlag = false;
        setSignalMastsAtBlockBoundaryFrame.setVisible(false);
    }

    void refreshSignalMastAtBoundaryComboBox() {
        createListUsedSignalMasts();
        usedMasts.remove(eastSignalMast.getBean());
        usedMasts.remove(westSignalMast.getBean());
        eastSignalMast.getCombo().setExcludedItems(usedMasts);
        westSignalMast.getCombo().setExcludedItems(usedMasts);
    }

    private void setSignalMastsAtBlockBoundaryDonePressed(ActionEvent a) {
        if (!getSimpleBlockInformation()) {
            return;
        }

        SignalMast oldBlock1SignalMast = boundary.getEastBoundSignalMast();
        SignalMast block1BoundSignalMast = getSignalMastFromEntry(eastSignalMast.getText(), false, setSignalMastsAtBlockBoundaryFrame);

        if (block1BoundSignalMast == null) {
            if (InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()
                    && InstanceManager.getDefault(SignalMastLogicManager.class).isSignalMastUsed(oldBlock1SignalMast)) {
                SignallingGuiTools.removeSignalMastLogic(setSignalMastsAtBlockBoundaryFrame, oldBlock1SignalMast);
            }

            removeSignalMastFromPanel(boundary.getEastBoundSignalMast());
            removeSignalMastAssignment(boundary.getEastBoundSignalMast());
            boundary.setEastBoundSignalMast("");
        }

        SignalMast oldBlock2SignalMast = boundary.getWestBoundSignalMast();
        SignalMast block2BoundSignalMast = getSignalMastFromEntry(westSignalMast.getText(), false, setSignalMastsAtBlockBoundaryFrame);

        if (block2BoundSignalMast == null) {
            if (InstanceManager.getDefault(LayoutBlockManager.class
            ).isAdvancedRoutingEnabled() && InstanceManager.getDefault(SignalMastLogicManager.class
            ).isSignalMastUsed(oldBlock2SignalMast)) {
                SignallingGuiTools.removeSignalMastLogic(setSignalMastsAtBlockBoundaryFrame, oldBlock2SignalMast);
            }

            removeSignalMastFromPanel(boundary.getWestBoundSignalMast());
            removeSignalMastAssignment(boundary.getWestBoundSignalMast());
            boundary.setWestBoundSignalMast("");
        }
        if (block2BoundSignalMast != null && block1BoundSignalMast != null) {
            if (block1BoundSignalMast == block2BoundSignalMast) {
                JOptionPane.showMessageDialog(setSignalMastsAtBlockBoundaryFrame,
                        Bundle.getMessage("SignalMastsError14"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (oldBlock1SignalMast == block2BoundSignalMast && oldBlock2SignalMast == block1BoundSignalMast) {
                //We are going for a swap!
                //Need to remove old items first
                removeSignalMastFromPanel(boundary.getWestBoundSignalMast());
                removeSignalMastFromPanel(boundary.getEastBoundSignalMast());
                removeSignalMastAssignment(block1BoundSignalMast);
                removeSignalMastAssignment(block2BoundSignalMast);
                //Then place new ones
                SignalMastIcon l;
                if (eastSignalMast.addToPanel()) {
                    l = new SignalMastIcon(layoutEditor);
                    l.setSignalMast(eastSignalMast.getText());
                    placeEastBoundIcon(l, eastSignalMast.isRightSelected(), 0);
                }
                if (westSignalMast.addToPanel()) {
                    l = new SignalMastIcon(layoutEditor);
                    l.setSignalMast(westSignalMast.getText());
                    placeWestBoundIcon(l, westSignalMast.isRightSelected(), 0);
                }
                boundary.setEastBoundSignalMast(eastSignalMast.getText());
                boundary.setWestBoundSignalMast(westSignalMast.getText());
                //Then sort out the logic

                if (InstanceManager.getDefault(LayoutBlockManager.class
                ).isAdvancedRoutingEnabled()) {
                    SignallingGuiTools.swapSignalMastLogic(setSignalMastsAtBlockBoundaryFrame, block1BoundSignalMast, block2BoundSignalMast);
                }
                needRedraw = true;
            }
        }
        if (!needRedraw) {
            if (block1BoundSignalMast != null) {
                if (eastSignalMast.addToPanel()) {
                    if (isSignalMastAssignedAnywhere(block1BoundSignalMast)
                            && (block1BoundSignalMast != oldBlock1SignalMast)) {
                        JOptionPane.showMessageDialog(setSignalMastsAtBlockBoundaryFrame,
                                Bundle.getMessage("SignalMastsError6",
                                        new Object[]{eastSignalMast.getText()}),
                                Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        removeSignalMastFromPanel(boundary.getEastBoundSignalMast());
                        SignalMastIcon l = new SignalMastIcon(layoutEditor);
                        l.setSignalMast(eastSignalMast.getText());
                        placeEastBoundIcon(l, eastSignalMast.isRightSelected(), 0);
                        removeSignalMastAssignment(block1BoundSignalMast);
                        boundary.setEastBoundSignalMast(eastSignalMast.getText());
                        needRedraw = true;
                    }
                } else if ((block1BoundSignalMast != boundary.getEastBoundSignalMast())
                        && (block1BoundSignalMast != boundary.getWestBoundSignalMast())) {
                    if (isSignalMastOnPanel(block1BoundSignalMast)) {
                        JOptionPane.showMessageDialog(setSignalMastsAtBlockBoundaryFrame,
                                Bundle.getMessage("SignalMastsError13",
                                        new Object[]{eastSignalMast.getText()}),
                                Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        removeSignalMastFromPanel(boundary.getEastBoundSignalMast());
                        removeSignalMastAssignment(block1BoundSignalMast);
                        boundary.setEastBoundSignalMast(eastSignalMast.getText());
                    }
                }
            }
            if (block2BoundSignalMast != null) {
                if (westSignalMast.addToPanel()) {
                    if (isSignalMastAssignedAnywhere(block2BoundSignalMast)
                            && (block2BoundSignalMast != oldBlock2SignalMast)) {
                        JOptionPane.showMessageDialog(setSignalMastsAtBlockBoundaryFrame,
                                Bundle.getMessage("SignalMastsError6",
                                        new Object[]{westSignalMast.getText()}),
                                Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    } else /*(oldBlock2SignalMast!=block2BoundSignalMast)*/ {
                        removeSignalMastFromPanel(boundary.getWestBoundSignalMast());
                        SignalMastIcon l = new SignalMastIcon(layoutEditor);
                        l.setSignalMast(westSignalMast.getText());
                        placeWestBoundIcon(l, westSignalMast.isRightSelected(), 0);
                        removeSignalMastAssignment(block2BoundSignalMast);
                        boundary.setWestBoundSignalMast(westSignalMast.getText());
                        needRedraw = true;
                    }
                } else if ((block2BoundSignalMast != boundary.getEastBoundSignalMast())
                        && (block2BoundSignalMast != oldBlock2SignalMast)) {
                    if (isSignalMastAssignedAnywhere(block2BoundSignalMast)) {
                        //Need to do this better, so that the signalMast can be on panel multiple times but only alocated to one anchor at a time
                        JOptionPane.showMessageDialog(setSignalMastsAtBlockBoundaryFrame,
                                Bundle.getMessage("SignalMastsError13",
                                        new Object[]{westSignalMast.getText()}),
                                Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        removeSignalMastFromPanel(boundary.getWestBoundSignalMast());
                        removeSignalMastAssignment(block2BoundSignalMast);
                        boundary.setWestBoundSignalMast(westSignalMast.getText());

                    }
                }
            }

            //If advanced routing is enabled and then this indicates that we are using this for discovering the signalmast logic paths.
            if (InstanceManager.getDefault(LayoutBlockManager.class
            ).isAdvancedRoutingEnabled()
                    && (block1BoundSignalMast != null
                    || block2BoundSignalMast != null)) {
                if ((oldBlock1SignalMast != null) && (block2BoundSignalMast != null)) {
                    updateBoundaryBasedSignalMastLogic(
                            oldBlock1SignalMast, oldBlock2SignalMast,
                            block1BoundSignalMast, block2BoundSignalMast);

                }
            }
        }
        setSignalMastsAtBlockBoundaryOpenFlag = false;
        setSignalMastsAtBlockBoundaryFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }

    public void updateBoundaryBasedSignalMastLogic(
            @Nonnull SignalMast oldBlock1SignalMast,
            @Nonnull SignalMast oldBlock2SignalMast,
            @Nonnull SignalMast block1BoundSignalMast,
            @Nonnull SignalMast block2BoundSignalMast) {
        SignalMastLogicManager smlm = InstanceManager.getDefault(SignalMastLogicManager.class
        );
        boolean old1Used = smlm.isSignalMastUsed(oldBlock1SignalMast);
        boolean old2Used = smlm.isSignalMastUsed(oldBlock2SignalMast);
        //Just check that the old ones are used in logics somewhere.
        if (old1Used || old2Used) {
            boolean new1Used = smlm.isSignalMastUsed(block1BoundSignalMast);
            boolean new2Used = smlm.isSignalMastUsed(block2BoundSignalMast);
            if (new1Used || new2Used) {
                if ((new1Used) && (block1BoundSignalMast != oldBlock1SignalMast)) {
                    SignallingGuiTools.removeAlreadyAssignedSignalMastLogic(setSignalMastsAtBlockBoundaryFrame, block1BoundSignalMast);
                }
                if ((new2Used) && (block2BoundSignalMast != oldBlock2SignalMast)) {
                    SignallingGuiTools.removeAlreadyAssignedSignalMastLogic(setSignalMastsAtBlockBoundaryFrame, block2BoundSignalMast);
                }
            }
            if (block1BoundSignalMast != null) {
                if (oldBlock2SignalMast != null && old2Used
                        && oldBlock2SignalMast == block1BoundSignalMast) {
                    SignallingGuiTools.updateSignalMastLogic(setSignalMastsAtBlockBoundaryFrame, oldBlock2SignalMast, block1BoundSignalMast);
                }

                if (oldBlock1SignalMast != null && old1Used
                        && oldBlock1SignalMast != block1BoundSignalMast) {

                    SignallingGuiTools.updateSignalMastLogic(setSignalMastsAtBlockBoundaryFrame, oldBlock1SignalMast, block1BoundSignalMast);
                }
            }
            if (block2BoundSignalMast != null) {
                if (old1Used && oldBlock1SignalMast == block2BoundSignalMast) {

                    SignallingGuiTools.updateSignalMastLogic(setSignalMastsAtBlockBoundaryFrame, oldBlock1SignalMast, block2BoundSignalMast);
                }
                if (old2Used && oldBlock2SignalMast != block2BoundSignalMast) {
                    SignallingGuiTools.updateSignalMastLogic(setSignalMastsAtBlockBoundaryFrame, oldBlock2SignalMast, block2BoundSignalMast);
                }
            }
        }
    }

    public void setIconOnPanel(@Nonnull PositionableIcon l,
            int rotation, @Nonnull Point p) {
        setIconOnPanel(l, rotation, (int) p.getX(), (int) p.getY());
    }

    public void setIconOnPanel(@Nonnull PositionableIcon l,
            int rotation, int xLoc, int yLoc) {
        l.setLocation(xLoc, yLoc);
        if (rotation > 0) {
            l.rotate(rotation);
        }
        if (l instanceof SignalMastIcon) {
            layoutEditor.putSignalMast((SignalMastIcon) l);
        } else if (l instanceof SensorIcon) {
            layoutEditor.putSensor((SensorIcon) l);
        } else if (l instanceof SignalHeadIcon) {
            layoutEditor.putSignal((SignalHeadIcon) l);
        }
    }

    private void placeEastBoundIcon(PositionableIcon icon, boolean isRightSide, double fromPoint) {

        Point2D p = boundary.getCoordsCenter();

        //Track segment is used to determine the alignment, therefore this is opposite to the block that we are protecting
        TrackSegment t = boundary.getConnect2();
        boolean dir = true;
        if (boundary.getType() == PositionablePoint.END_BUMPER) {
            t = boundary.getConnect1();
        } else {
            if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                t = boundary.getConnect1();
            }
        }

        Point2D pt2;
        if (t.getConnect1() == boundary) {
            pt2 = LayoutEditor.getCoords(t.getConnect2(), t.getType2());
        } else {
            pt2 = LayoutEditor.getCoords(t.getConnect1(), t.getType1());
        }
        setIconOnPanel(t, icon, dir, p, pt2, isRightSide, fromPoint);
    }

    private void placeWestBoundIcon(PositionableIcon icon, boolean isRightSide, double fromPoint) {

        Point2D p = boundary.getCoordsCenter();

        //Track segment is used to determine the alignment, therefore this is opposite to the block that we are protecting
        TrackSegment t = boundary.getConnect1();
        boolean dir = false;
        if (boundary.getType() != PositionablePoint.END_BUMPER) {
            if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                t = boundary.getConnect2();
            }
        }

        Point2D pt2;
        if (t.getConnect1() == boundary) {
            pt2 = LayoutEditor.getCoords(t.getConnect2(), t.getType2());
        } else {
            pt2 = LayoutEditor.getCoords(t.getConnect1(), t.getType1());
        }
        setIconOnPanel(t, icon, dir, p, pt2, isRightSide, fromPoint);

    }

    private void setIconOnPanel(@Nonnull TrackSegment t,
            @Nonnull PositionableIcon l,
            boolean isEastBound, @Nonnull Point2D pt1, @Nonnull Point2D pt2,
            boolean isRightSide, double fromPoint) {
        double pt1x = pt1.getX(), pt1y = pt1.getY();
        double pt2x = pt2.getX(), pt2y = pt2.getY();

        int triX = (int) Math.round(pt2x - pt1x);
        int triY = (int) Math.round(pt2y - pt1y);

        if (log.isDebugEnabled()) {
            log.debug("X " + triX + " Y " + triY);
        }
        Point loc;
        if (triX == 0 || triX == 360) {
            //In a vertical Striaght Line
            if (isEastBound) {
                log.debug("In a vertical striaghtline facing South");
                loc = northToSouth(pt1, l, isRightSide, fromPoint);
            } else {
                log.debug("In a vertical striaghtline facing North");
                loc = southToNorth(pt1, l, isRightSide, fromPoint);
            }
        } else if (triY == 0 || triY == 360) {
            //In a Horizontal Straight Line
            if (isEastBound) {
                log.debug("In a Horizontal striaghtline facing east");
                loc = westToEast(pt1, l, isRightSide, fromPoint);
            } else {
                log.debug("In a Horizontal striaghtline facing west");
                loc = eastToWest(pt1, l, isRightSide, fromPoint);
            }
        } else {
            //Compute arc's chord
            double a = pt2x - pt1x;
            double o = pt2y - pt1y;
            double radius = Math.hypot(a, o);  //chord equates to radius of circle

            double pt1xa = pt1x + radius;
            double pt1ya = pt1y;
            double a1 = pt2x - pt1xa;
            double o1 = pt2y - pt1ya;
            double chord = Math.hypot(a1, o1);
            double rsq = Math.pow(radius, 2);

            double radAngleFromDatum = Math.acos((rsq + rsq - Math.pow(chord, 2)) / (2 * rsq));
            if (log.isDebugEnabled()) {
                log.debug("radius " + radius + " Chord " + chord);
                log.debug("Angle from datum line " + Math.toDegrees(radAngleFromDatum));
            }

            int rotateDEG = ((int) Math.toDegrees(radAngleFromDatum));
            if (log.isDebugEnabled()) {
                double tanx = o / a;
                double angletanRAD = Math.atan2(o, a);
                log.debug(Math.toDegrees(angletanRAD) + " = atan2(" + o + ", " + a + ") (" + tanx + ")");
            }

            int oldHeight = l.maxHeight();
            int oldWidth = l.maxWidth();

            //pt1 is always our boundary point
            //East side
            if (pt2x > pt1x) {
                //East Sides
                if (pt2y > pt1y) {
                    //"South East Corner"
                    rotateDEG = rotateDEG + 270;  //Correct for SM111, sm101, sm121, SM80
                    l.rotate(rotateDEG);
                    loc = southEastToNorthWest(pt1, l, oldWidth, oldHeight, rotateDEG, isRightSide, fromPoint);
                } else {
                    //"North East corner" //correct for sm110, sm70, sm131
                    rotateDEG = 270 - rotateDEG;
                    l.rotate(rotateDEG);
                    loc = northEastToSouthWest(pt1, l, oldWidth, oldHeight, rotateDEG, isRightSide, fromPoint);
                }

            } else {
                //West Side
                if (pt2y > pt1y) {
                    //South West //WORKING FOR SM141, sm130, SM71
                    l.rotate(rotateDEG - 90);
                    //South West
                    loc = southWestToNorthEast(pt1, l, oldWidth, oldHeight, rotateDEG, isRightSide, fromPoint);
                } else {
                    //North West //Working FOR SM140, SM81, sm120
                    rotateDEG = (180 - rotateDEG) + 90;
                    l.rotate(rotateDEG);
                    loc = northWestToSouthEast(pt1, l, oldWidth, oldHeight, rotateDEG, isRightSide, fromPoint);
                }
            }
        }
        setIconOnPanel(l, 0, loc);
    }

    Point southToNorth(Point2D p, PositionableIcon l, boolean right, double fromPoint) {
        int offsetx = 0;
        int offsety = (int) (p.getY() + offSetFromPoint + fromPoint);
        if (right) {
            offsetx = (int) p.getX() + offSetFromPoint;
        } else {
            offsetx = (int) p.getX() - offSetFromPoint - l.maxWidth();
        }
        return new Point(offsetx, offsety);
    }

    Point northToSouth(Point2D p, PositionableIcon l, boolean right, double fromPoint) {
        l.rotate(180);
        int offsetx = 0;
        int offsety = (int) (p.getY() - (offSetFromPoint + fromPoint) - l.maxHeight());
        if (right) {
            offsetx = (int) p.getX() - offSetFromPoint - l.maxWidth();
        } else {
            offsetx = (int) p.getX() + offSetFromPoint;
        }
        return new Point(offsetx, offsety);
    }

    Point westToEast(Point2D p, PositionableIcon l, boolean right, double fromPoint) {
        l.rotate(90);
        int offsetx = (int) (p.getX() - (l.maxWidth() + (offSetFromPoint + fromPoint - 1)));
        int offsety = 0;
        if (right) {
            offsety = (int) p.getY() + (offSetFromPoint - 1);
        } else {
            offsety = (int) p.getY() - (offSetFromPoint) - l.maxHeight();
        }
        return new Point(offsetx, offsety);
    }

    Point eastToWest(Point2D p, PositionableIcon l, boolean right, double fromPoint) {
        l.rotate(-90);
        int offsetx = (int) (p.getX() + offSetFromPoint + fromPoint);
        int offsety = 0;
        if (right) {
            offsety = (int) p.getY() - (offSetFromPoint - 1) - l.maxHeight();
        } else {
            offsety = (int) p.getY() + (offSetFromPoint);
        }
        return new Point(offsetx, offsety);
    }

    /**
     * come back to this as its a bit tight to the rail on SM110 need re
     * checking
     */
    Point northEastToSouthWest(Point2D p, PositionableIcon l, int oldWidth, int oldHeight, double angleDEG, boolean right, double fromPoint) {
        angleDEG = angleDEG - 180;
        if (angleDEG < 45) {
            //Because of the angle things get shifted about.
            int tmpWidth = oldWidth;
            int tmpHeight = oldHeight;
            oldWidth = tmpHeight;
            oldHeight = tmpWidth;
        }
        double ang = angleDEG;
        double oppAng = 90 - ang;
        double angleRAD = Math.toRadians(angleDEG);
        double oppAngRAD = Math.toRadians(oppAng);
        double iconAdj = Math.sin(angleRAD) * oldHeight;
        double iconAdjOpp = Math.sin(oppAngRAD) * oldHeight;
        double bpa = Math.sin(angleRAD) * (offSetFromPoint + fromPoint);
        double bpo = Math.sin(oppAngRAD) * (offSetFromPoint + fromPoint);
        double ta = Math.sin(angleRAD) * offSetFromPoint;
        double to = Math.sin(oppAngRAD) * offSetFromPoint;

        if (log.isDebugEnabled()) {
            log.debug("north east to south west " + angleDEG);
            log.debug("oldWidth " + oldWidth + " oldHeight " + oldHeight);
            log.debug("newWidth " + l.maxWidth() + " newHeight " + l.maxHeight());
            log.debug("Icon adj: " + iconAdj + " opp adj: " + iconAdjOpp);
            log.debug("boundary point opp " + bpo);
            log.debug("boundary point adj " + bpa);
            log.debug("track opp " + to);
            log.debug("track adj " + ta);
        }
        int xpos = 0;
        int ypos = 0;
        if (right) {
            //double x_dist_to_Icon = (l.maxWidth()-iconAdj)-(bpa-bpo);
            //double y_dist_to_Icon = bpa+bpo+l.maxHeight();

            double x_dist_to_Icon = (iconAdjOpp) - (bpa - to);
            double y_dist_to_Icon = ta + bpo + l.maxHeight();

            log.debug("x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);

            xpos = (int) (p.getX() - x_dist_to_Icon);
            ypos = (int) (p.getY() - y_dist_to_Icon);

        } else {
            double y_dist_to_Icon = iconAdjOpp + (bpo - ta);
            double x_dist_to_Icon = to + bpa;
            //double y_dist_to_Icon = (l.maxHeight()-iconAdj)-(ta-bpo);
            //double x_dist_to_Icon = bpa+to;
            log.debug("x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);

            xpos = (int) (p.getX() + x_dist_to_Icon);
            ypos = (int) (p.getY() - y_dist_to_Icon);

        }
        if (log.isDebugEnabled()) {
            log.debug("xpos " + xpos);
            log.debug("yPos " + ypos);
        }
        return new Point(xpos, ypos);

    }

    Point southWestToNorthEast(Point2D p, PositionableIcon l, int oldWidth, int oldHeight, double angleDEG, boolean right, double fromPoint) {
        angleDEG = 180 - angleDEG;

        double oppAng = angleDEG;
        double angDEG = 90 - oppAng;

        //Because of the angle things get shifted about.
        if (angDEG < 45) { //was angle
            int tmpWidth = oldWidth;
            int tmpHeight = oldHeight;
            oldWidth = tmpHeight;
            oldHeight = tmpWidth;
        }

        double angRAD = Math.toRadians(angDEG);
        double oppAngRAD = Math.toRadians(oppAng);
        double iconAdj = Math.sin(angRAD) * oldHeight;
        double iconAdjOpp = Math.sin(oppAngRAD) * oldHeight;
        double bpa = Math.sin(angRAD) * (offSetFromPoint + fromPoint);
        double bpo = Math.sin(oppAngRAD) * (offSetFromPoint + fromPoint);
        double ta = Math.sin(angRAD) * offSetFromPoint;
        double to = Math.sin(oppAngRAD) * offSetFromPoint;

        if (log.isDebugEnabled()) {
            log.debug("south west to north east " + angleDEG);
            log.debug("oldWidth " + oldWidth + " oldHeight " + oldHeight);
            log.debug("newWidth " + l.maxWidth() + " newHeight " + l.maxHeight());
            log.debug("Icon adj: " + iconAdj + " opp adj: " + iconAdjOpp);
            log.debug("boundary point opp " + bpo);
            log.debug("boundary point adj " + bpa);
            log.debug("track opp " + to);
            log.debug("track adj " + ta);
        }

        int xpos;
        int ypos;

        if (right) {
            double x_dist_to_Icon = iconAdj + (bpa - to);
            double y_dist_to_Icon = ta + bpo;
            log.debug("x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);

            xpos = (int) (p.getX() - x_dist_to_Icon);
            log.debug("xpos " + xpos);
            ypos = (int) (p.getY() + y_dist_to_Icon);
            log.debug("yPos " + ypos);
        } else {
            double x_dist_to_Icon = (bpa + to) + l.maxWidth();
            //double y_dist_to_Icon = (iconAdj+(ta-bpo));
            double y_dist_to_Icon = (bpo - ta) - (l.maxHeight() - iconAdjOpp);
            //double y_dist_to_Icon = (iconAdj+(ta-bpo));
            log.debug("x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);
            xpos = (int) (p.getX() - x_dist_to_Icon);
            ypos = (int) (p.getY() + y_dist_to_Icon);
        }
        if (log.isDebugEnabled()) {
            log.debug("xpos " + xpos);
            log.debug("yPos " + ypos);
        }
        return new Point(xpos, ypos);

    }

    //Working FOR SM140, SM81, sm120
    Point northWestToSouthEast(Point2D p, PositionableIcon l, int oldWidth, int oldHeight, double angleDEG, boolean right, double fromPoint) {
        log.debug("angle before " + angleDEG);
        angleDEG = 180 - angleDEG;
        angleDEG = 90 - angleDEG;
        log.debug("north west to south east " + angleDEG);
        if (angleDEG < 45) {
            //Because of the angle things get shifted about.
            int tmpWidth = oldWidth;
            int tmpHeight = oldHeight;
            oldWidth = tmpHeight;
            oldHeight = tmpWidth;
        }
        log.debug("oldWidth " + oldWidth + " oldHeight " + oldHeight);
        log.debug("newWidth " + l.maxWidth() + " newHeight " + l.maxHeight());
        //double ang = angle;
        double oppAng = 90 - angleDEG;
        double angleRAD = Math.toRadians(angleDEG);
        double oppAngRAD = Math.toRadians(oppAng);
        double iconAdj = Math.sin(angleRAD) * oldHeight;
        double iconAdjOpp = Math.sin(oppAngRAD) * oldHeight;

        double bpa = Math.sin(angleRAD) * (offSetFromPoint + fromPoint);  //distance from point
        double bpo = Math.sin(oppAngRAD) * (offSetFromPoint + fromPoint);
        double ta = Math.sin(angleRAD) * offSetFromPoint; //distance from track
        double to = Math.sin(oppAngRAD) * offSetFromPoint;

        if (log.isDebugEnabled()) {
            log.debug("north west to south east " + angleDEG);
            log.debug("oldWidth " + oldWidth + " oldHeight " + oldHeight);
            log.debug("newWidth " + l.maxWidth() + " newHeight " + l.maxHeight());
            log.debug("Icon adj: " + iconAdj + " opp adj: " + iconAdjOpp);
            log.debug("boundary point opp " + bpo);
            log.debug("boundary point adj " + bpa);
            log.debug("track opp " + to);
            log.debug("track adj " + ta);
        }
        int xpos = 0;
        int ypos = 0;
        if (right) {
            //double x_dist_to_Icon = bpa+bpo+l.maxWidth();
            //double y_dist_to_Icon = bpa-(l.maxHeight()-iconAdj);
            double x_dist_to_Icon = (l.maxWidth() + ta + bpo);
            double y_dist_to_Icon = iconAdj + (bpa - to);

            log.debug("right x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);

            xpos = (int) (p.getX() - x_dist_to_Icon);
            ypos = (int) (p.getY() - y_dist_to_Icon); //was +
        } else {
            //This still needs to be worked out.
            //double y_dist_to_Icon = bpa+bpo+l.maxHeight();
            //double x_dist_to_Icon = iconAdj+(bpa-bpo);

            double y_dist_to_Icon = l.maxHeight() + bpa + to;//+(l.maxWidth()-iconAdj);
            //double y_dist_to_Icon = bpa-(l.maxHeight()-iconAdj);
            //double y_dist_to_Icon = ta+bpo+l.maxHeight();
            double x_dist_to_Icon = (iconAdjOpp) + (bpo - ta);
            //double x_dist_to_Icon = iconAdj+(bpa-to);
            log.debug("left x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);

            xpos = (int) (p.getX() - x_dist_to_Icon);
            ypos = (int) (p.getY() - y_dist_to_Icon);
        }
        if (log.isDebugEnabled()) {
            log.debug(p.getX() + " xpos " + xpos);
            log.debug(p.getY() + " yPos " + ypos);
        }
        return new Point(xpos, ypos);
    }

    double adjust = (5.0 / 90.0);
    int awayright = 5;
    private final int offSetFromPoint = 5;

    //Correct for SM111, sm101, sm121, SM80
    Point southEastToNorthWest(Point2D p, PositionableIcon l, int oldWidth, int oldHeight, double angleDEG, boolean right, double fromPoint) {
        angleDEG = 360 - angleDEG;

        if (angleDEG > 45) {
            //Because of the angle things get shifted about.
            int tmpWidth = oldWidth;
            int tmpHeight = oldHeight;
            oldWidth = tmpWidth;
            oldHeight = tmpHeight;
        }

//		  double ang = angle;
        double oppAng = 90 - angleDEG;
        double angleRAD = Math.toRadians(angleDEG);
        double oppAngRAD = Math.toRadians(oppAng);
        double iconAdj = Math.sin(angleRAD) * oldHeight;
        double iconAdjOpp = Math.sin(oppAngRAD) * oldHeight;
        double bpa = Math.sin(angleRAD) * (offSetFromPoint + fromPoint);
        double bpo = Math.sin(oppAngRAD) * (offSetFromPoint + fromPoint);
        double ta = Math.sin(angleRAD) * offSetFromPoint; //distance from track
        double to = Math.sin(oppAngRAD) * offSetFromPoint;
        if (log.isDebugEnabled()) {
            log.debug("south east to north west " + angleDEG);
            log.debug("oldWidth " + oldWidth + " oldHeight " + oldHeight);
            log.debug("newWidth " + l.maxWidth() + " newHeight " + l.maxHeight());
            log.debug("Icon adj: " + iconAdj + " opp adj: " + iconAdjOpp);
            log.debug("boundary point opp " + bpo);
            log.debug("boundary point adj " + bpa);
            log.debug("track opp " + to);
            log.debug("track adj " + ta);
        }
        int xpos = 0;
        int ypos = 0;
        if (right) {
            //double x_dist_to_Icon = bpa+bpo;
            //double y_dist_to_Icon = (iconAdj+bpa-bpo);
            double x_dist_to_Icon = bpa + to;
            double y_dist_to_Icon = (bpo - ta) - (l.maxHeight() - iconAdjOpp);

            log.debug(Double.toString((bpo - ta) - (l.maxHeight() - iconAdjOpp)));
            log.debug(Double.toString(bpo - (iconAdj + ta)));
            /*if(angleDeg<45){
			 y_dist_to_Icon = (bpo-ta)-(l.maxHeight()-iconAdjOpp);
			 } else {
			 y_dist_to_Icon = bpo-(iconAdj+ta);
			 }*/
            //double y_dist_to_Icon = (l.maxHeight()-iconAdj)+(bpo-ta);
            xpos = (int) (p.getX() + x_dist_to_Icon);
            ypos = (int) (p.getY() + y_dist_to_Icon);
            log.debug("right x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);
        } else {
            //double x_dist_to_Icon = l.maxWidth()-(iconAdj+(bpa-bpo));
            //double y_dist_to_Icon = bpa+bpo;

            double x_dist_to_Icon = (bpa - to) - (l.maxWidth() - iconAdj);
            double y_dist_to_Icon = bpo + ta;

            xpos = (int) (p.getX() + x_dist_to_Icon);
            ypos = (int) (p.getY() + y_dist_to_Icon);
            log.debug("left x dist " + x_dist_to_Icon + ", y dist " + y_dist_to_Icon);
        }
        if (log.isDebugEnabled()) {
            log.debug(p.getX() + " xpos " + xpos);
            log.debug(p.getY() + " yPos " + ypos);
        }

        return new Point(xpos, ypos);
    }

    public boolean isSignalMastOnPanel(@Nonnull SignalMast signalMast) {
        for (SignalMastIcon s : layoutEditor.signalMastList) {
            if (s.getSignalMast() == signalMast) {
                return true;
            }
        }
        return false;
    }

    /*=========================*\
    |* setSignalMastsAtTurnout *|
    \*=========================*/
    private JmriJFrame setSignalMastsAtTurnoutFrame = null;
    private boolean setSignalMastsAtTurnoutOpenFlag = false;
    private boolean setSignalMastsAtTurnoutFromMenuFlag = false;

    private final NamedBeanComboBox<Turnout> signalMastsTurnoutComboBox = new NamedBeanComboBox<>(
            InstanceManager.turnoutManagerInstance(), null,
            DisplayOptions.DISPLAYNAME);

    private JButton setSignalMastsDone;
    private JButton getSavedSignalMasts;
    private JButton setSignalMastsCancel;
    private JLabel turnoutMastNameLabel = null;

    BeanDetails<SignalMast> turnoutSignalMastA;
    BeanDetails<SignalMast> turnoutSignalMastB;
    BeanDetails<SignalMast> turnoutSignalMastC;
    BeanDetails<SignalMast> turnoutSignalMastD;

    JPanel signalMastTurnoutPanel = new JPanel(new FlowLayout());

    private String[] turnoutBlocks = new String[4];

    public void setSignalMastsAtTurnoutFromMenu(@Nonnull LayoutTurnout to,
            @Nonnull String[] blocks) {
        layoutTurnout = to;
        turnout = to.getTurnout();
        signalMastsTurnoutComboBox.setSelectedItem(turnout);
        turnoutBlocks = new String[4];
        for (int i = 0; i < blocks.length; i++) {
            turnoutBlocks[i] = blocks[i];
        }
        setSignalMastsAtTurnoutFromMenuFlag = true;
        setSignalMastsAtTurnout();
        setSignalMastsAtTurnoutFromMenuFlag = false;
    }

    //TODO: Add to Tools menu?
    public void setSignalMastsAtTurnout() {

        //Initialize if needed
        if (setSignalMastsAtTurnoutFrame == null) {
            setSignalMastsAtTurnoutOpenFlag = false;

            turnoutSignalMastA = new BeanDetails<>("SignalMast", //NOI18N
                    InstanceManager.getDefault(SignalMastManager.class));
            turnoutSignalMastB = new BeanDetails<>("SignalMast", //NOI18N
                    InstanceManager.getDefault(SignalMastManager.class));
            turnoutSignalMastC = new BeanDetails<>("SignalMast", //NOI18N
                    InstanceManager.getDefault(SignalMastManager.class));
            turnoutSignalMastD = new BeanDetails<>("SignalMast", //NOI18N
                    InstanceManager.getDefault(SignalMastManager.class));

            turnoutSignalMastA.getDetailsPanel().setBackground(new Color(255, 255, 200));
            turnoutSignalMastB.getDetailsPanel().setBackground(new Color(200, 255, 255));
            turnoutSignalMastC.getDetailsPanel().setBackground(new Color(200, 200, 255));
            turnoutSignalMastD.getDetailsPanel().setBackground(new Color(255, 200, 200));

            setSignalMastsAtTurnoutFrame = new JmriJFrame(Bundle.getMessage("SignalMastsAtTurnout"), false, true);
            oneFrameToRuleThemAll(setSignalMastsAtTurnoutFrame);
            setSignalMastsAtTurnoutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//         setSignalMastsAtTurnoutFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalMastsAtTurnout", true);
            setSignalMastsAtTurnoutFrame.setLocation(70, 30);
            Container theContentPane = setSignalMastsAtTurnoutFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel1 = new JPanel(new FlowLayout());

            turnoutMastNameLabel = new JLabel(
                    Bundle.getMessage("BeanNameTurnout")
                    + " " + Bundle.getMessage("Name"));
            panel1.add(turnoutMastNameLabel);
            panel1.add(signalMastsTurnoutComboBox);
            signalMastsTurnoutComboBox.setToolTipText(Bundle.getMessage("SignalMastsTurnoutNameHint"));

            theContentPane.add(panel1);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel2 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("SignalMasts"));
            panel2.add(shTitle);
            panel2.add(new JLabel("	  "));
            panel2.add(getSavedSignalMasts = new JButton(Bundle.getMessage("GetSaved")));
            getSavedSignalMasts.addActionListener((ActionEvent e) -> {
                turnoutSignalMastsGetSaved(e);
            });
            getSavedSignalMasts.setToolTipText(Bundle.getMessage("GetSavedHint"));
            theContentPane.add(panel2);

            signalMastTurnoutPanel.setLayout(new GridLayout(0, 2));
            theContentPane.add(signalMastTurnoutPanel);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(new JLabel("	 "));
            panel6.add(setSignalMastsDone = new JButton(Bundle.getMessage("ButtonDone")));
            setSignalMastsDone.addActionListener((ActionEvent e) -> {
                setSignalMastsDonePressed(e);
            });
            setSignalMastsDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            //make this button the default button (return or enter activates)
            //Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(setSignalMastsDone);
                rootPane.setDefaultButton(setSignalMastsDone);
            });

            panel6.add(setSignalMastsCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setSignalMastsCancel.addActionListener((ActionEvent e) -> {
                setSignalMastsCancelPressed(e);
            });
            setSignalMastsCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
        }

        turnoutSignalMastA.getCombo().setExcludedItems(new HashSet<>());
        turnoutSignalMastB.getCombo().setExcludedItems(new HashSet<>());
        turnoutSignalMastC.getCombo().setExcludedItems(new HashSet<>());
        turnoutSignalMastD.getCombo().setExcludedItems(new HashSet<>());
        signalMastTurnoutPanel.removeAll();

        signalMastsTurnoutComboBox.setVisible(!setSignalMastsAtTurnoutFromMenuFlag);

        if (setSignalMastsAtTurnoutFromMenuFlag) {
            turnoutMastNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameTurnout")
                    + " " + Bundle.getMessage("Name"))
                    + " " + layoutTurnout.getTurnoutName());
        }

        if (!setSignalMastsAtTurnoutOpenFlag) {
            setSignalMastsAtTurnoutFrame.setPreferredSize(null);
            setSignalMastsAtTurnoutFrame.pack();
            setSignalMastsAtTurnoutOpenFlag = true;
        }
        refreshSignalMastAtTurnoutComboBox();
        setSignalMastsAtTurnoutFrame.setVisible(true);
    }   //setSignalMastsAtTurnout

    private void turnoutSignalMastsGetSaved(ActionEvent a) {
        if (!getTurnoutMastInformation()) {
            return;
        }
        turnoutBlocks = layoutTurnout.getBlockBoundaries();

        turnoutSignalMastA.setTextField(layoutTurnout.getSignalAMastName());
        turnoutSignalMastB.setTextField(layoutTurnout.getSignalBMastName());
        turnoutSignalMastC.setTextField(layoutTurnout.getSignalCMastName());
        turnoutSignalMastD.setTextField(layoutTurnout.getSignalDMastName());

        turnoutSignalMastA.setBoundaryLabel(turnoutBlocks[0]);
        turnoutSignalMastB.setBoundaryLabel(turnoutBlocks[1]);
        turnoutSignalMastC.setBoundaryLabel(turnoutBlocks[2]);
        turnoutSignalMastD.setBoundaryLabel(turnoutBlocks[3]);

        signalMastTurnoutPanel.removeAll();
        boolean boundaryFlag = false;
        if (turnoutBlocks[0] != null) {
            signalMastTurnoutPanel.add(turnoutSignalMastA.getDetailsPanel());
            boundaryFlag = true;
        }
        if (turnoutBlocks[1] != null) {
            signalMastTurnoutPanel.add(turnoutSignalMastB.getDetailsPanel());
            boundaryFlag = true;
        }
        if (turnoutBlocks[2] != null) {
            signalMastTurnoutPanel.add(turnoutSignalMastC.getDetailsPanel());
            boundaryFlag = true;
        }
        if (turnoutBlocks[3] != null) {
            signalMastTurnoutPanel.add(turnoutSignalMastD.getDetailsPanel());
            boundaryFlag = true;
        }
        if (!boundaryFlag) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("SignalsError20"));
        }
        setSignalMastsAtTurnoutFrame.setPreferredSize(null);
        setSignalMastsAtTurnoutFrame.pack();
    }   //turnoutSignalMastsGetSaved

    private void setSignalMastsDonePressed(ActionEvent a) {
        //process turnout name
        if (!getTurnoutMastInformation()) {
            return;
        }

        //process signal head names
        SignalMast turnoutMast = getSignalMastFromEntry(turnoutSignalMastA.getText(), false, setSignalsAtTurnoutFrame);
        SignalMast turnoutMastB = getSignalMastFromEntry(turnoutSignalMastB.getText(), false, setSignalsAtTurnoutFrame);
        SignalMast turnoutMastC = getSignalMastFromEntry(turnoutSignalMastC.getText(), false, setSignalsAtTurnoutFrame);
        SignalMast turnoutMastD = getSignalMastFromEntry(turnoutSignalMastD.getText(), false, setSignalsAtTurnoutFrame);

        //place signals as requested
        if (turnoutSignalMastA.addToPanel() && (turnoutMast != null)) {
            if (isSignalMastOnPanel(turnoutMast)
                    && (turnoutMast != layoutTurnout.getSignalAMast())) {
                JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{turnoutSignalMastA.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(layoutTurnout.getSignalAMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(turnoutSignalMastA.getText());
                placingBlock(l, turnoutSignalMastA.isRightSelected(), 0.0, layoutTurnout.getConnectA(), layoutTurnout.getCoordsA());
                removeAssignment(turnoutMast);
                layoutTurnout.setSignalAMast(turnoutSignalMastA.getText());
                needRedraw = true;
            }
        } else if (turnoutMast != null) {
            int assigned = isMastAssignedHere(turnoutMast, layoutTurnout);
            if (assigned == LayoutTurnout.NONE) {
                if (isSignalMastOnPanel(turnoutMast)
                        && isSignalMastAssignedAnywhere(turnoutMast)) {
                    JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{turnoutSignalMastA.getText()}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalMastFromPanel(layoutTurnout.getSignalAMast());
                    removeAssignment(turnoutMast);
                    layoutTurnout.setSignalAMast(turnoutSignalMastA.getText());
                }
                //} else if (assigned != A1) {
                //need to figure out what to do in this case.
            }
        } else {
            removeSignalMastFromPanel(layoutTurnout.getSignalAMast());
            layoutTurnout.setSignalAMast("");
        }
        if ((turnoutSignalMastB.addToPanel()) && (turnoutMastB != null)) {
            if (isSignalMastOnPanel(turnoutMastB)
                    && (turnoutMastB != layoutTurnout.getSignalBMast())) {
                JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{turnoutSignalMastB.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(layoutTurnout.getSignalBMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(turnoutSignalMastB.getText());
                placingBlock(l, turnoutSignalMastB.isRightSelected(), 0.0, layoutTurnout.getConnectB(), layoutTurnout.getCoordsB());
                removeAssignment(turnoutMastB);
                layoutTurnout.setSignalBMast(turnoutSignalMastB.getText());
                needRedraw = true;
            }
        } else if (turnoutMastB != null) {
            int assigned = isMastAssignedHere(turnoutMastB, layoutTurnout);
            if (assigned == LayoutTurnout.NONE) {
                if (isSignalMastOnPanel(turnoutMastB)
                        && isSignalMastAssignedAnywhere(turnoutMastB)) {
                    JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{turnoutSignalMastB.getText()}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalMastFromPanel(layoutTurnout.getSignalBMast());
                    removeAssignment(turnoutMastB);
                    layoutTurnout.setSignalBMast(turnoutSignalMastB.getText());
                }
                //} else if (assigned != A2) {
                //need to figure out what to do in this case.
            }
        } else {
            removeSignalMastFromPanel(layoutTurnout.getSignalBMast());
            layoutTurnout.setSignalBMast("");
        }
        if (turnoutMastC != null) {
            if (turnoutSignalMastC.addToPanel()) {
                if (isSignalMastOnPanel(turnoutMastC)
                        && (turnoutMastC != layoutTurnout.getSignalCMast())) {
                    JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                            Bundle.getMessage("SignalsError6",
                                    new Object[]{turnoutSignalMastC.getText()}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalMastFromPanel(layoutTurnout.getSignalCMast());
                    SignalMastIcon l = new SignalMastIcon(layoutEditor);
                    l.setSignalMast(turnoutSignalMastC.getText());
                    placingBlock(l, turnoutSignalMastC.isRightSelected(), 0.0, layoutTurnout.getConnectC(), layoutTurnout.getCoordsC());
                    removeAssignment(turnoutMastC);
                    layoutTurnout.setSignalCMast(turnoutSignalMastC.getText());
                    needRedraw = true;
                }
            } else {
                int assigned = isMastAssignedHere(turnoutMastC, layoutTurnout);
                if (assigned == LayoutTurnout.NONE) {
                    if (isSignalMastOnPanel(turnoutMastC)
                            && isSignalMastAssignedAnywhere(turnoutMastC)) {
                        JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                                Bundle.getMessage("SignalsError8",
                                        new Object[]{turnoutSignalMastC.getText()}),
                                Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        removeSignalMastFromPanel(layoutTurnout.getSignalCMast());
                        removeAssignment(turnoutMastC);
                        layoutTurnout.setSignalCMast(turnoutSignalMastC.getText());
                    }
                    //} else if (assigned != A3) {
                    //need to figure out what to do in this case.
                }
            }
        } else {
            removeSignalMastFromPanel(layoutTurnout.getSignalCMast());
            layoutTurnout.setSignalCMast("");
        }
        if (turnoutMastD != null) {
            if (turnoutSignalMastD.addToPanel()) {
                if (isSignalMastOnPanel(turnoutMastD)
                        && (turnoutMastD != layoutTurnout.getSignalDMast())) {
                    String signalHeadName = divergingSignalHeadComboBox.getSelectedItemDisplayName();
                    if (signalHeadName == null) {
                        signalHeadName = "";
                    }
                    JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                            Bundle.getMessage("SignalsError6",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalMastFromPanel(layoutTurnout.getSignalDMast());
                    SignalMastIcon l = new SignalMastIcon(layoutEditor);
                    l.setSignalMast(turnoutSignalMastD.getText());
                    placingBlock(l, turnoutSignalMastD.isRightSelected(), 0.0, layoutTurnout.getConnectD(), layoutTurnout.getCoordsD());
                    removeAssignment(turnoutMastD);
                    layoutTurnout.setSignalDMast(turnoutSignalMastD.getText());
                    needRedraw = true;
                }
            } else {
                int assigned = isMastAssignedHere(turnoutMastD, layoutTurnout);
                if (assigned == LayoutTurnout.NONE) {
                    if (isSignalMastOnPanel(turnoutMastD)
                            && isSignalMastAssignedAnywhere(turnoutMastD)) {
                        JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                                Bundle.getMessage("SignalsError8",
                                        new Object[]{turnoutSignalMastD.getText()}),
                                Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        removeSignalMastFromPanel(layoutTurnout.getSignalDMast());
                        removeAssignment(turnoutMastD);
                        layoutTurnout.setSignalDMast(turnoutSignalMastD.getText());
                    }
                    //} else if (assigned != B1) {
                    //need to figure out what to do in this case.
                }
            }
        } else {
            removeSignalMastFromPanel(layoutTurnout.getSignalDMast());
            layoutTurnout.setSignalDMast("");
        }

        //make sure this layout turnout is not linked to another
        layoutTurnout.setLinkType(LayoutTurnout.NO_LINK);
        layoutTurnout.setLinkedTurnoutName("");
        //finish up
        setSignalMastsAtTurnoutOpenFlag = false;
        setSignalMastsAtTurnoutFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }   //setSignalMastsDonePressed

    Set<SignalMast> usedMasts = new HashSet<>();

    void createListUsedSignalMasts() {
        usedMasts = new HashSet<>();
        for (PositionablePoint po : layoutEditor.getPositionablePoints()) {
            //We allow the same sensor to be allocated in both directions.
            if (po != boundary) {
                if (po.getEastBoundSignalMast() != null) {
                    usedMasts.add(po.getEastBoundSignalMast());
                }
                if (po.getWestBoundSignalMast() != null) {
                    usedMasts.add(po.getWestBoundSignalMast());
                }
            }
        }

        for (LayoutTurnout to : layoutEditor.getLayoutTurnoutsAndSlips()) {
            if (to.getSignalAMast() != null) {
                usedMasts.add(to.getSignalAMast());
            }
            if (to.getSignalBMast() != null) {
                usedMasts.add(to.getSignalBMast());
            }
            if (to.getSignalCMast() != null) {
                usedMasts.add(to.getSignalCMast());
            }
            if (to.getSignalDMast() != null) {
                usedMasts.add(to.getSignalDMast());
            }
        }
        for (LevelXing x : layoutEditor.getLevelXings()) {
            if (x.getSignalAMast() != null) {
                usedMasts.add(x.getSignalAMast());
            }
            if (x.getSignalBMast() != null) {
                usedMasts.add(x.getSignalBMast());
            }
            if (x.getSignalCMast() != null) {
                usedMasts.add(x.getSignalCMast());
            }
            if (x.getSignalDMast() != null) {
                usedMasts.add(x.getSignalDMast());
            }
        }
    }   //createListUsedSignalMasts

    void refreshSignalMastAtTurnoutComboBox() {
        turnoutSignalMastsGetSaved(null);
        createListUsedSignalMasts();

        usedMasts.remove(turnoutSignalMastA.getBean());
        usedMasts.remove(turnoutSignalMastB.getBean());
        usedMasts.remove(turnoutSignalMastC.getBean());
        usedMasts.remove(turnoutSignalMastD.getBean());

        turnoutSignalMastA.getCombo().setExcludedItems(usedMasts);
        turnoutSignalMastB.getCombo().setExcludedItems(usedMasts);
        turnoutSignalMastC.getCombo().setExcludedItems(usedMasts);
        turnoutSignalMastD.getCombo().setExcludedItems(usedMasts);
    }

    private int isMastAssignedHere(
            @CheckForNull SignalMast mast,
            @CheckForNull LayoutTurnout lTurnout) {
        if ((mast == null) || (lTurnout == null)) {
            return LayoutTurnout.NONE;
        }
        String sysName = mast.getSystemName();
        String uName = mast.getUserName();

        String name = lTurnout.getSignalAMastName();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return LayoutTurnout.POINTA1;
        }
        name = lTurnout.getSignalBMastName();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return LayoutTurnout.POINTA2;
        }
        name = lTurnout.getSignalCMastName();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return LayoutTurnout.POINTA3;
        }
        name = lTurnout.getSignalDMastName();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return LayoutTurnout.POINTB1;
        }
        return LayoutTurnout.NONE;
    }   //isMastAssignedHere

    public void removeAssignment(@Nonnull SignalMast mast) {
        String sName = mast.getSystemName();
        String uName = mast.getUserName();
        for (LayoutTurnout to : layoutEditor.getLayoutTurnouts()) {
            if ((to.getSignalAMastName().equals(sName) || ((uName != null)
                    && (to.getSignalAMastName().equals(uName))))) {
                to.setSignalAMast("");
            }
            if ((to.getSignalBMastName().equals(sName) || ((uName != null)
                    && (to.getSignalBMastName().equals(uName))))) {
                to.setSignalBMast("");
            }
            if ((to.getSignalCMastName().equals(sName) || ((uName != null)
                    && (to.getSignalCMastName().equals(uName))))) {
                to.setSignalCMast("");
            }
            if ((to.getSignalDMastName().equals(sName) || ((uName != null)
                    && (to.getSignalDMastName().equals(uName))))) {
                to.setSignalDMast("");
            }
        }
        for (PositionablePoint po : layoutEditor.getPositionablePoints()) {
            if (po.getEastBoundSignalMastName().equals(sName) || po.getEastBoundSignalMastName().equals(uName)) {
                po.setEastBoundSignalMast("");
            }
            if (po.getWestBoundSignalMastName().equals(sName) || po.getWestBoundSignalMastName().equals(uName)) {
                po.setWestBoundSignalMast("");
            }
        }
        for (LevelXing x : layoutEditor.getLevelXings()) {
            if ((x.getSignalAMastName() != null)
                    && (x.getSignalAMastName().equals(sName) || ((uName != null)
                    && (x.getSignalAMastName().equals(uName))))) {
                x.setSignalAMast("");
            }
            if ((x.getSignalBMastName() != null)
                    && (x.getSignalBMastName().equals(sName) || ((uName != null)
                    && (x.getSignalBMastName().equals(uName))))) {
                x.setSignalBMast("");
            }
            if ((x.getSignalCMastName() != null)
                    && (x.getSignalCMastName().equals(sName) || ((uName != null)
                    && (x.getSignalCMastName().equals(uName))))) {
                x.setSignalCMast("");
            }
            if ((x.getSignalDMastName() != null)
                    && (x.getSignalDMastName().equals(sName) || ((uName != null)
                    && (x.getSignalDMastName().equals(uName))))) {
                x.setSignalDMast("");
            }
        }
    }   //removeAssignment

    private boolean getTurnoutMastInformation() {
        turnout = null;
        layoutTurnout = null;
        String str = signalMastsTurnoutComboBox.getSelectedItemDisplayName();
        if ((str == null) || str.isEmpty()) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame, Bundle.getMessage("SignalsError1") + "qqq",
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        turnout = InstanceManager.turnoutManagerInstance().getTurnout(str);
        if (turnout == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("SignalsError2",
                            new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            String uname = turnout.getUserName();
            if ((uname == null) || uname.isEmpty()
                    || !uname.equals(str)) {
                signalMastsTurnoutComboBox.setSelectedItem(turnout);
            }
        }
        layoutTurnout = layoutEditor.getFinder().findLayoutTurnoutByBean(turnout);

        if (layoutTurnout == null) {
            JOptionPane.showMessageDialog(setSignalsAtTurnoutFrame,
                    Bundle.getMessage("SignalsError3",
                            new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void placingBlock(PositionableIcon icon, boolean isRightSide,
            double fromPoint, Object obj, Point2D p) {
        if (obj instanceof TrackSegment) {
            TrackSegment ts = (TrackSegment) obj;
            Point2D endPoint;
            if (ts.getConnect1() == layoutTurnout) {
                endPoint = LayoutEditor.getCoords(ts.getConnect2(), ts.getType2());
            } else {
                endPoint = LayoutEditor.getCoords(ts.getConnect1(), ts.getType1());
            }
            boolean isEast = false;
            if (MathUtil.equals(endPoint.getX(), p.getX())) {
                log.debug("X in both is the same");
                if (endPoint.getY() < p.getY()) {
                    log.debug("Y end point is less than our point");
                    isEast = true;
                }
            } else if (endPoint.getX() < p.getX()) {
                log.debug("end X point is less than our point");
                isEast = true;
            }

            log.debug("East set is " + isEast);
            setIconOnPanel(ts, icon, isEast, p, endPoint, isRightSide, fromPoint);
        }
    }

    private void setSignalMastsCancelPressed(ActionEvent a) {
        setSignalMastsAtTurnoutOpenFlag = false;
        setSignalMastsAtTurnoutFrame.setVisible(false);
    }

    /*============================*\
    |* setSignalMastsAtLayoutSlip *|
    \*============================*/
    //operational variables for Set SignalMast at Slip tool
    private JmriJFrame setSignalMastsAtLayoutSlipFrame = null;
    private boolean setSignalMastsAtLayoutSlipOpenFlag = false;
    private boolean setSignalMastsAtLayoutSlipFromMenuFlag = false;

    private JButton getSavedSlipSignalMasts = null;
    private JButton setSlipSignalMastsDone = null;
    private JButton setSlipSignalMastsCancel = null;

    private String[] slipBlocks = new String[4];

    private final NamedBeanComboBox<Block> slipSignalBlockAComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(BlockManager.class),
                    null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Block> slipSignalBlockBComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(BlockManager.class),
                    null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Block> slipSignalBlockCComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(BlockManager.class),
                    null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Block> slipSignalBlockDComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(BlockManager.class),
                    null, DisplayOptions.DISPLAYNAME);

    private JLabel slipSignalBlockANameLabel = null;
    private JLabel slipSignalBlockBNameLabel = null;
    private JLabel slipSignalBlockCNameLabel = null;
    private JLabel slipSignalBlockDNameLabel = null;

    BeanDetails<SignalMast> slipSignalMastA;
    BeanDetails<SignalMast> slipSignalMastB;
    BeanDetails<SignalMast> slipSignalMastC;
    BeanDetails<SignalMast> slipSignalMastD;

    JPanel signalMastLayoutSlipPanel = new JPanel(new FlowLayout());

    public void setSignalMastsAtSlipFromMenu(@Nonnull LayoutSlip slip,
            @Nonnull String[] blocks, @Nonnull JFrame theFrame) {
        layoutSlip = slip;
        layoutTurnout = slip;

        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        slipSignalBlockAComboBox.setSelectedItem(bm.getBlock(layoutSlip.getBlockName()));
        slipSignalBlockBComboBox.setSelectedItem(bm.getBlock(layoutSlip.getBlockBName()));
        slipSignalBlockCComboBox.setSelectedItem(bm.getBlock(layoutSlip.getBlockCName()));
        slipSignalBlockDComboBox.setSelectedItem(bm.getBlock(layoutSlip.getBlockDName()));

        slipBlocks = new String[4];
        for (int i = 0; i < blocks.length; i++) {
            slipBlocks[i] = blocks[i];
        }
        setSignalMastsAtLayoutSlipFromMenuFlag = true;
        setSignalMastsAtLayoutSlip(theFrame);
    }

    //TODO: Add to Tools menu?
    public void setSignalMastsAtLayoutSlip(@Nonnull JFrame theFrame) {
        signalFrame = theFrame;

        //Initialize if needed
        if (setSignalMastsAtLayoutSlipFrame == null) {
            setSignalMastsAtLayoutSlipOpenFlag = false;

            slipSignalMastA = new BeanDetails<>("SignalMast",
                    InstanceManager.getDefault(SignalMastManager.class));
            slipSignalMastB = new BeanDetails<>("SignalMast",
                    InstanceManager.getDefault(SignalMastManager.class));
            slipSignalMastC = new BeanDetails<>("SignalMast",
                    InstanceManager.getDefault(SignalMastManager.class));
            slipSignalMastD = new BeanDetails<>("SignalMast",
                    InstanceManager.getDefault(SignalMastManager.class));

            slipSignalMastA.getDetailsPanel().setBackground(new Color(255, 255, 200));
            slipSignalMastB.getDetailsPanel().setBackground(new Color(200, 255, 255));
            slipSignalMastC.getDetailsPanel().setBackground(new Color(200, 200, 255));
            slipSignalMastD.getDetailsPanel().setBackground(new Color(255, 200, 200));

            setSignalMastsAtLayoutSlipFrame = new JmriJFrame(Bundle.getMessage("SignalMastsAtLayoutSlip"), false, true);
            oneFrameToRuleThemAll(setSignalMastsAtLayoutSlipFrame);
            setSignalMastsAtLayoutSlipFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//         setSignalMastsAtLayoutSlipFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtLayoutSlip", true);
            setSignalMastsAtLayoutSlipFrame.setLocation(70, 30);
            Container theContentPane = setSignalMastsAtLayoutSlipFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel11A = new JPanel(new FlowLayout());
            //note: this is just placeholder text; real text is set below
            slipSignalBlockANameLabel = new JLabel(" A ");
            panel11A.add(slipSignalBlockANameLabel);
            panel11A.add(slipSignalBlockAComboBox);
            slipSignalBlockAComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            theContentPane.add(panel11A);

            JPanel panel11B = new JPanel(new FlowLayout());
            //note: this is just placeholder text; real text is set below
            slipSignalBlockBNameLabel = new JLabel(" B ");
            panel11B.add(slipSignalBlockBNameLabel);
            panel11B.add(slipSignalBlockBComboBox);
            slipSignalBlockBComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            theContentPane.add(panel11B);

            JPanel panel11C = new JPanel(new FlowLayout());
            //note: this is just placeholder text; real text is set below
            slipSignalBlockCNameLabel = new JLabel(" C ");
            panel11C.add(slipSignalBlockCNameLabel);
            panel11C.add(slipSignalBlockCComboBox);
            slipSignalBlockCComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            theContentPane.add(panel11C);

            JPanel panel11D = new JPanel(new FlowLayout());
            //note: this is just placeholder text; real text is set below
            slipSignalBlockDNameLabel = new JLabel(" D ");
            panel11D.add(slipSignalBlockDNameLabel);
            panel11D.add(slipSignalBlockDComboBox);
            slipSignalBlockDComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            theContentPane.add(panel11D);

            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel2 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("BeanNameSignalMast"));
            panel2.add(shTitle);
            panel2.add(new JLabel("	  "));
            panel2.add(getSavedSlipSignalMasts = new JButton(Bundle.getMessage("GetSaved")));
            getSavedSlipSignalMasts.addActionListener((ActionEvent e) -> {
                slipSignalMastsGetSaved(e);
            });
            getSavedSlipSignalMasts.setToolTipText(Bundle.getMessage("GetSavedHint"));
            theContentPane.add(panel2);

            signalMastLayoutSlipPanel.setLayout(new GridLayout(0, 2));
            theContentPane.add(signalMastLayoutSlipPanel);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());

            panel6.add(new JLabel("	 "));
            panel6.add(setSlipSignalMastsDone = new JButton(Bundle.getMessage("ButtonDone")));
            setSlipSignalMastsDone.addActionListener((ActionEvent e) -> {
                setSlipSignalMastsDonePressed(e);
            });
            setSlipSignalMastsDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));
            panel6.add(setSlipSignalMastsCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setSlipSignalMastsCancel.addActionListener((ActionEvent e) -> {
                setSlipSignalMastsCancelPressed(e);
            });
            setSlipSignalMastsCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
            setSignalMastsAtLayoutSlipFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSlipSignalMastsCancelPressed(null);
                }
            });
        }

        //Unhide any excluded masts
        slipSignalMastA.getCombo().setExcludedItems(new HashSet<>());
        slipSignalMastB.getCombo().setExcludedItems(new HashSet<>());
        slipSignalMastC.getCombo().setExcludedItems(new HashSet<>());
        slipSignalMastD.getCombo().setExcludedItems(new HashSet<>());
        signalMastLayoutSlipPanel.removeAll();

        slipSignalBlockAComboBox.setVisible(!setSignalMastsAtLayoutSlipFromMenuFlag);
        slipSignalBlockBComboBox.setVisible(!setSignalMastsAtLayoutSlipFromMenuFlag);
        slipSignalBlockCComboBox.setVisible(!setSignalMastsAtLayoutSlipFromMenuFlag);
        slipSignalBlockDComboBox.setVisible(!setSignalMastsAtLayoutSlipFromMenuFlag);

        if (setSignalMastsAtLayoutSlipFromMenuFlag) {
            slipSignalBlockANameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " A "
                    + Bundle.getMessage("Name"))
                    + " " + layoutSlip.getBlockName());
            slipSignalBlockBNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " B "
                    + Bundle.getMessage("Name"))
                    + " " + layoutSlip.getBlockBName());
            slipSignalBlockCNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " C "
                    + Bundle.getMessage("Name"))
                    + " " + layoutSlip.getBlockCName());
            slipSignalBlockDNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " D "
                    + Bundle.getMessage("Name"))
                    + " " + layoutSlip.getBlockDName());
            refreshSignalMastAtSlipComboBox();
        } else {
            slipSignalBlockANameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " A "
                    + Bundle.getMessage("Name")));
            slipSignalBlockBNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " B "
                    + Bundle.getMessage("Name")));
            slipSignalBlockCNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " C "
                    + Bundle.getMessage("Name")));
            slipSignalBlockDNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " D "
                    + Bundle.getMessage("Name")));
        }

        if (!setSignalMastsAtLayoutSlipOpenFlag) {
            setSignalMastsAtLayoutSlipFrame.setPreferredSize(null);
            setSignalMastsAtLayoutSlipFrame.pack();
            setSignalMastsAtLayoutSlipOpenFlag = true;
        }
        setSignalMastsAtLayoutSlipFrame.setVisible(true);
    }

    void refreshSignalMastAtSlipComboBox() {
        slipSignalMastsGetSaved(null);
        createListUsedSignalMasts();

        usedMasts.remove(slipSignalMastA.getBean());
        usedMasts.remove(slipSignalMastB.getBean());
        usedMasts.remove(slipSignalMastC.getBean());
        usedMasts.remove(slipSignalMastD.getBean());

        slipSignalMastA.getCombo().setExcludedItems(usedMasts);
        slipSignalMastB.getCombo().setExcludedItems(usedMasts);
        slipSignalMastC.getCombo().setExcludedItems(usedMasts);
        slipSignalMastD.getCombo().setExcludedItems(usedMasts);
    }

    private void slipSignalMastsGetSaved(ActionEvent a) {
        if (!getSlipMastInformation()) {
            return;
        }
        slipBlocks = layoutSlip.getBlockBoundaries();

        slipSignalMastA.setTextField(layoutSlip.getSignalAMastName());
        slipSignalMastB.setTextField(layoutSlip.getSignalBMastName());
        slipSignalMastC.setTextField(layoutSlip.getSignalCMastName());
        slipSignalMastD.setTextField(layoutSlip.getSignalDMastName());

        slipSignalMastA.setBoundaryLabel(slipBlocks[0]);
        slipSignalMastB.setBoundaryLabel(slipBlocks[1]);
        slipSignalMastC.setBoundaryLabel(slipBlocks[2]);
        slipSignalMastD.setBoundaryLabel(slipBlocks[3]);

        boolean boundaryFlag = false;
        signalMastLayoutSlipPanel.remove(slipSignalMastA.getDetailsPanel());
        signalMastLayoutSlipPanel.remove(slipSignalMastB.getDetailsPanel());
        signalMastLayoutSlipPanel.remove(slipSignalMastC.getDetailsPanel());
        signalMastLayoutSlipPanel.remove(slipSignalMastD.getDetailsPanel());
        if (slipBlocks[0] != null) {
            signalMastLayoutSlipPanel.add(slipSignalMastA.getDetailsPanel());
            boundaryFlag = true;
        }
        if (slipBlocks[1] != null) {
            signalMastLayoutSlipPanel.add(slipSignalMastB.getDetailsPanel());
            boundaryFlag = true;
        }
        if (slipBlocks[2] != null) {
            signalMastLayoutSlipPanel.add(slipSignalMastC.getDetailsPanel());
            boundaryFlag = true;
        }
        if (slipBlocks[3] != null) {
            signalMastLayoutSlipPanel.add(slipSignalMastD.getDetailsPanel());
            boundaryFlag = true;
        }
        if (!boundaryFlag) {
            JOptionPane.showMessageDialog(setSignalMastsAtLayoutSlipFrame, "There are no block boundaries on this level crossing\nIt is therefore not possible to add Signal Masts to it");
        }
        setSignalMastsAtLayoutSlipFrame.setPreferredSize(null);
        setSignalMastsAtLayoutSlipFrame.pack();
    }

    private boolean getSlipMastInformation() {
        if (!setSignalMastsAtLayoutSlipFromMenuFlag) {
            layoutSlip = null;
            List<LayoutSlip> layoutSlips = layoutEditor.getLayoutSlips();
            if (layoutSlips.size() <= 0) {
                JOptionPane.showMessageDialog(setSignalMastsAtLayoutSlipFrame,
                        Bundle.getMessage("SignalsError15"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            } else if (layoutSlips.size() == 1) {
                layoutSlip = layoutSlips.get(0);
            } else {
                LayoutBlock slipBlockA = null;
                //LayoutBlock slipBlockC = null;
                slipBlockA = getBlockFromEntry(xingBlockACComboBox);
                if (slipBlockA == null) {
                    return false;
                }

                int foundCount = 0;
                //make two block tests first
                for (LayoutSlip x : layoutEditor.getLayoutSlips()) {
                    LayoutBlock xA = null;
                    LayoutBlock xB = null;
                    LayoutBlock xC = null;
                    LayoutBlock xD = null;

                    LayoutBlock xAC = x.getLayoutBlock();
                    if (x.getConnectA() != null) {
                        xA = ((TrackSegment) x.getConnectA()).getLayoutBlock();
                    }
                    if (x.getConnectB() != null) {
                        xB = ((TrackSegment) x.getConnectB()).getLayoutBlock();
                    }
                    if (x.getConnectC() != null) {
                        xC = ((TrackSegment) x.getConnectC()).getLayoutBlock();
                    }
                    if (x.getConnectD() != null) {
                        xD = ((TrackSegment) x.getConnectD()).getLayoutBlock();
                    }
                    if (((xA != null) && (xC != null) && ((xA == slipBlockA)
                            || (xC == slipBlockA)))
                            || ((xB != null) && (xD != null) && ((xB == slipBlockA)
                            || (xD == slipBlockA)))) {
                        layoutSlip = x;
                        foundCount++;
                    } else if ((xAC != null) && (xAC == slipBlockA)) {
                        layoutSlip = x;
                        foundCount++;
                    }
                }
                if (foundCount == 0) {
                    //try one block test
                    for (LayoutSlip x : layoutEditor.getLayoutSlips()) {
                        if (slipBlockA == x.getLayoutBlock()) {
                            layoutSlip = x;
                            foundCount++;
                        }
                    }
                }
                if (foundCount > 1) {
                    JOptionPane.showMessageDialog(setSignalMastsAtLayoutSlipFrame,
                            Bundle.getMessage("SignalsError16",
                                    new Object[]{" " + foundCount + " "}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (layoutSlip == null) {
                    JOptionPane.showMessageDialog(setSignalMastsAtLayoutSlipFrame,
                            Bundle.getMessage("SignalsError17"),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    private void setSlipSignalMastsCancelPressed(ActionEvent a) {
        setSignalMastsAtLayoutSlipOpenFlag = false;
        setSignalMastsAtLayoutSlipFrame.setVisible(false);
    }

    private void setSlipSignalMastsDonePressed(ActionEvent a) {
        if (!getSlipMastInformation()) {
            return;
        }
        SignalMast aMast = getSignalMastFromEntry(slipSignalMastA.getText(), false, setSignalMastsAtLayoutSlipFrame);
        SignalMast bMast = getSignalMastFromEntry(slipSignalMastB.getText(), false, setSignalMastsAtLayoutSlipFrame);
        SignalMast cMast = getSignalMastFromEntry(slipSignalMastC.getText(), false, setSignalMastsAtLayoutSlipFrame);
        SignalMast dMast = getSignalMastFromEntry(slipSignalMastD.getText(), false, setSignalMastsAtLayoutSlipFrame);
        //place or update signals as requested
        if ((aMast != null) && slipSignalMastA.addToPanel()) {
            if (isSignalMastOnPanel(aMast)
                    && (aMast != layoutSlip.getSignalAMast())) {
                JOptionPane.showMessageDialog(setSignalMastsAtLayoutSlipFrame,
                        Bundle.getMessage("SignalMastsError6",
                                new Object[]{slipSignalMastA.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(layoutSlip.getSignalAMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(slipSignalMastA.getText());
                placingBlock(l, slipSignalMastA.isRightSelected(), 0.0, layoutSlip.getConnectA(), layoutSlip.getCoordsA());
                removeAssignment(aMast);
                layoutSlip.setSignalAMast(slipSignalMastA.getText());
                needRedraw = true;
            }
        } else if ((aMast != null)
                && (aMast != layoutSlip.getSignalAMast())
                && (aMast != layoutSlip.getSignalBMast())
                && (aMast != layoutSlip.getSignalCMast())
                && (aMast != layoutSlip.getSignalDMast())) {
            if (isSignalMastOnPanel(aMast)) {
                JOptionPane.showMessageDialog(setSignalMastsAtLayoutSlipFrame,
                        Bundle.getMessage("SignalMastsError13",
                                new Object[]{slipSignalMastA.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(layoutSlip.getSignalAMast());
                removeAssignment(aMast);
                layoutSlip.setSignalAMast(slipSignalMastA.getText());
            }
        } else if ((aMast != null)
                && ((aMast == layoutSlip.getSignalBMast())
                || (aMast == layoutSlip.getSignalCMast())
                || (aMast == layoutSlip.getSignalDMast()))) {
            //need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (aMast == null) {
            removeSignalMastFromPanel(layoutSlip.getSignalAMast());
            layoutSlip.setSignalAMast("");
        }
        if ((bMast != null) && slipSignalMastB.addToPanel()) {
            if (isSignalMastOnPanel(bMast)
                    && (bMast != layoutSlip.getSignalBMast())) {
                JOptionPane.showMessageDialog(setSignalMastsAtLayoutSlipFrame,
                        Bundle.getMessage("SignalMastsError6",
                                new Object[]{slipSignalMastB.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(layoutSlip.getSignalBMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(slipSignalMastB.getText());
                placingBlock(l, slipSignalMastB.isRightSelected(), 0.0, layoutSlip.getConnectB(), layoutSlip.getCoordsB());
                removeAssignment(bMast);
                layoutSlip.setSignalBMast(slipSignalMastB.getText());
                needRedraw = true;
            }
        } else if ((bMast != null)
                && (bMast != layoutSlip.getSignalAMast())
                && (bMast != layoutSlip.getSignalBMast())
                && (bMast != layoutSlip.getSignalCMast())
                && (bMast != layoutSlip.getSignalDMast())) {
            if (isSignalMastOnPanel(bMast)) {
                JOptionPane.showMessageDialog(setSignalMastsAtLayoutSlipFrame,
                        Bundle.getMessage("SignalMastsError13",
                                new Object[]{slipSignalMastB.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(layoutSlip.getSignalBMast());
                removeAssignment(bMast);
                layoutSlip.setSignalBMast(slipSignalMastB.getText());
            }
        } else if ((bMast != null)
                && ((bMast == layoutSlip.getSignalAMast())
                || (bMast == layoutSlip.getSignalCMast())
                || (bMast == layoutSlip.getSignalDMast()))) {
            //need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (bMast == null) {
            removeSignalMastFromPanel(layoutSlip.getSignalBMast());
            layoutSlip.setSignalBMast("");
        }
        if ((cMast != null) && slipSignalMastC.addToPanel()) {
            if (isSignalMastOnPanel(cMast)
                    && (cMast != layoutSlip.getSignalCMast())) {
                JOptionPane.showMessageDialog(setSignalMastsAtLayoutSlipFrame,
                        Bundle.getMessage("SignalMastsError6",
                                new Object[]{slipSignalMastC.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(layoutSlip.getSignalCMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(slipSignalMastC.getText());
                placingBlock(l, slipSignalMastA.isRightSelected(), 0.0, layoutSlip.getConnectC(), layoutSlip.getCoordsC());
                removeAssignment(cMast);
                layoutSlip.setSignalCMast(slipSignalMastC.getText());
                needRedraw = true;
            }
        } else if ((cMast != null)
                && (cMast != layoutSlip.getSignalAMast())
                && (cMast != layoutSlip.getSignalBMast())
                && (cMast != layoutSlip.getSignalCMast())
                && (cMast != layoutSlip.getSignalDMast())) {
            if (isSignalMastOnPanel(cMast)) {
                JOptionPane.showMessageDialog(setSignalMastsAtLayoutSlipFrame,
                        Bundle.getMessage("SignalMastsError13",
                                new Object[]{slipSignalMastC.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(layoutSlip.getSignalCMast());
                removeAssignment(cMast);
                layoutSlip.setSignalCMast(slipSignalMastC.getText());
            }
        } else if ((cMast != null)
                && ((cMast == layoutSlip.getSignalBMast())
                || (cMast == layoutSlip.getSignalAMast())
                || (cMast == layoutSlip.getSignalDMast()))) {
            //need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (cMast == null) {
            removeSignalMastFromPanel(layoutSlip.getSignalCMast());
            layoutSlip.setSignalCMast("");
        }
        if ((dMast != null) && slipSignalMastD.addToPanel()) {
            if (isSignalMastOnPanel(dMast)
                    && (dMast != layoutSlip.getSignalDMast())) {
                JOptionPane.showMessageDialog(setSignalMastsAtLayoutSlipFrame,
                        Bundle.getMessage("SignalMastsError6",
                                new Object[]{slipSignalMastD.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(layoutSlip.getSignalDMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(slipSignalMastD.getText());
                placingBlock(l, slipSignalMastD.isRightSelected(), 0.0, layoutSlip.getConnectD(), layoutSlip.getCoordsD());
                removeAssignment(dMast);
                layoutSlip.setSignalDMast(slipSignalMastD.getText());
                needRedraw = true;
            }
        } else if ((dMast != null)
                && (dMast != layoutSlip.getSignalAMast())
                && (dMast != layoutSlip.getSignalBMast())
                && (dMast != layoutSlip.getSignalCMast())
                && (dMast != layoutSlip.getSignalDMast())) {
            if (isSignalMastOnPanel(dMast)) {
                JOptionPane.showMessageDialog(setSignalMastsAtLayoutSlipFrame,
                        Bundle.getMessage("SignalMastsError13",
                                new Object[]{slipSignalMastD.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(layoutSlip.getSignalDMast());
                removeAssignment(dMast);
                layoutSlip.setSignalDMast(slipSignalMastD.getText());
            }
        } else if ((dMast != null)
                && ((dMast == layoutSlip.getSignalBMast())
                || (dMast == layoutSlip.getSignalCMast())
                || (dMast == layoutSlip.getSignalAMast()))) {
            //need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (dMast == null) {
            removeSignalMastFromPanel(layoutSlip.getSignalDMast());
            layoutSlip.setSignalDMast("");
        }
        //setup logic if requested
        //finish up
        setSignalMastsAtLayoutSlipOpenFlag = false;
        setSignalMastsAtLayoutSlipFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }

    /*===========================*\
    |* setSignalMastsAtLevelXing *|
    \*===========================*/
    //operational variables for Set SignalMast at Level Crossing tool
    private JmriJFrame setSignalMastsAtLevelXingFrame = null;
    private boolean setSignalMastsAtLevelXingOpenFlag = false;
    private boolean setSignalMastsAtLevelXingFromMenuFlag = false;

    private JLabel xingSignalBlockACNameLabel = null;
    private JLabel xingSignalBlockBDNameLabel = null;

    private final NamedBeanComboBox<Block> xingBlockACComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class),
            null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Block> xingBlockBDComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class),
            null, DisplayOptions.DISPLAYNAME);

    private JButton getSavedXingSignalMasts = null;
    private JButton setXingSignalMastsDone = null;
    private JButton setXingSignalMastsCancel = null;

    private String[] xingBlocks = new String[4];

    BeanDetails<SignalMast> xingSignalMastA;
    BeanDetails<SignalMast> xingSignalMastB;
    BeanDetails<SignalMast> xingSignalMastC;
    BeanDetails<SignalMast> xingSignalMastD;

    JPanel signalMastLevelXingPanel = new JPanel(new FlowLayout());

    Border blackline = BorderFactory.createLineBorder(Color.black);

    //display dialog for Set Signals at Level Crossing tool
    public void setSignalMastsAtLevelXingFromMenu(@Nonnull LevelXing xing,
            @Nonnull String[] blocks,
            @Nonnull JFrame theFrame) {
        levelXing = xing;
        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        xingBlockACComboBox.setSelectedItem(bm.getBlock(levelXing.getBlockNameAC()));
        xingBlockBDComboBox.setSelectedItem(bm.getBlock(levelXing.getBlockNameBD()));
        xingBlocks = new String[4];
        for (int i = 0; i < blocks.length; i++) {
            xingBlocks[i] = blocks[i];
        }
        setSignalMastsAtLevelXingFromMenuFlag = true;
        setSignalMastsAtLevelXing(theFrame);
        setSignalMastsAtLevelXingFromMenuFlag = false;
    }

    //TODO: Add to Tools menu?
    public void setSignalMastsAtLevelXing(@Nonnull JFrame theFrame) {
        signalFrame = theFrame;

        //Initialize if needed
        if (setSignalMastsAtLevelXingFrame == null) {
            setSignalMastsAtLevelXingOpenFlag = false;

            xingSignalMastA = new BeanDetails<>("SignalMast",
                    InstanceManager.getDefault(SignalMastManager.class
                    ));
            xingSignalMastB = new BeanDetails<>("SignalMast",
                    InstanceManager.getDefault(SignalMastManager.class
                    ));
            xingSignalMastC = new BeanDetails<>("SignalMast",
                    InstanceManager.getDefault(SignalMastManager.class
                    ));
            xingSignalMastD = new BeanDetails<>("SignalMast",
                    InstanceManager.getDefault(SignalMastManager.class
                    ));

            xingSignalMastA.getDetailsPanel().setBackground(new Color(255, 255, 200));
            xingSignalMastB.getDetailsPanel().setBackground(new Color(200, 255, 255));
            xingSignalMastC.getDetailsPanel().setBackground(new Color(200, 200, 255));
            xingSignalMastD.getDetailsPanel().setBackground(new Color(255, 200, 200));

            setSignalMastsAtLevelXingFrame = new JmriJFrame(Bundle.getMessage("SignalMastsAtLevelXing"), false, true);
            oneFrameToRuleThemAll(setSignalMastsAtLevelXingFrame);
            setSignalMastsAtLevelXingFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSignalMastsAtLevelXingFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtLevelXing", true);
            setSignalMastsAtLevelXingFrame.setLocation(70, 30);
            Container theContentPane = setSignalMastsAtLevelXingFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel11 = new JPanel(new FlowLayout());
            xingSignalBlockACNameLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " AC "
                    + Bundle.getMessage("Name")));
            panel11.add(xingSignalBlockACNameLabel);
            panel11.add(xingBlockACComboBox);
            xingBlockACComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            theContentPane.add(panel11);

            JPanel panel12 = new JPanel(new FlowLayout());
            xingSignalBlockBDNameLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " BD "
                    + Bundle.getMessage("Name")));
            panel12.add(xingSignalBlockBDNameLabel);
            panel12.add(xingBlockBDComboBox);
            xingBlockBDComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            theContentPane.add(panel12);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel2 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("BeanNameSignalMast"));
            panel2.add(shTitle);
            panel2.add(new JLabel("	  "));
            panel2.add(getSavedXingSignalMasts = new JButton(Bundle.getMessage("GetSaved")));
            getSavedXingSignalMasts.addActionListener((ActionEvent e) -> {
                xingSignalMastsGetSaved(e);
            });
            getSavedXingSignalMasts.setToolTipText(Bundle.getMessage("GetSavedHint"));
            theContentPane.add(panel2);

            signalMastLevelXingPanel.setLayout(new GridLayout(0, 2));

            theContentPane.add(signalMastLevelXingPanel);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());

            panel6.add(new JLabel("	 "));
            panel6.add(setXingSignalMastsDone = new JButton(Bundle.getMessage("ButtonDone")));
            setXingSignalMastsDone.addActionListener((ActionEvent e) -> {
                setXingSignalMastsDonePressed(e);
            });
            setXingSignalMastsDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));
            panel6.add(setXingSignalMastsCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setXingSignalMastsCancel.addActionListener((ActionEvent e) -> {
                setXingSignalMastsCancelPressed(e);
            });
            setXingSignalMastsCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
            setSignalMastsAtLevelXingFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setXingSignalMastsCancelPressed(null);
                }
            });
        } //if (setSignalMastsAtLevelXingFrame == null)

        //Unhide any excluded masts
        xingSignalMastA.getCombo().setExcludedItems(new HashSet<>());
        xingSignalMastB.getCombo().setExcludedItems(new HashSet<>());
        xingSignalMastC.getCombo().setExcludedItems(new HashSet<>());
        xingSignalMastD.getCombo().setExcludedItems(new HashSet<>());
        signalMastLevelXingPanel.removeAll();

        if (setSignalMastsAtLevelXingFromMenuFlag) {
            xingBlockACComboBox.setVisible(false);
            xingBlockBDComboBox.setVisible(false);

            xingSignalBlockACNameLabel.setText(Bundle.getMessage("MakeLabel",
                    (Bundle.getMessage("BeanNameBlock") + " AC"))
                    + " " + levelXing.getBlockNameAC());
            xingSignalBlockBDNameLabel.setText(Bundle.getMessage("MakeLabel",
                    (Bundle.getMessage("BeanNameBlock") + " BD"))
                    + " " + levelXing.getBlockNameBD());

            xingSignalMastA.setTextField(levelXing.getSignalAMastName());
            xingSignalMastB.setTextField(levelXing.getSignalBMastName());
            xingSignalMastC.setTextField(levelXing.getSignalCMastName());
            xingSignalMastD.setTextField(levelXing.getSignalDMastName());

            xingSignalMastsGetSaved(null);
            refreshSignalMastAtXingComboBox();
        } else {
            xingSignalBlockACNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " AC "
                    + Bundle.getMessage("Name")));
            xingSignalBlockBDNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " BD "
                    + Bundle.getMessage("Name")));
        }

        if (!setSignalMastsAtLevelXingOpenFlag) {
            setSignalMastsAtLevelXingFrame.setPreferredSize(null);
            setSignalMastsAtLevelXingFrame.pack();
            setSignalMastsAtLevelXingOpenFlag = true;
        }
        setSignalMastsAtLevelXingFrame.setVisible(true);
    }   //setSignalMastsAtLevelXing

    void refreshSignalMastAtXingComboBox() {
        xingSignalMastsGetSaved(null);
        createListUsedSignalMasts();

        usedMasts.remove(xingSignalMastA.getBean());
        usedMasts.remove(xingSignalMastB.getBean());
        usedMasts.remove(xingSignalMastC.getBean());
        usedMasts.remove(xingSignalMastD.getBean());

        xingSignalMastA.getCombo().setExcludedItems(usedMasts);
        xingSignalMastB.getCombo().setExcludedItems(usedMasts);
        xingSignalMastC.getCombo().setExcludedItems(usedMasts);
        xingSignalMastD.getCombo().setExcludedItems(usedMasts);
    }

    private void xingSignalMastsGetSaved(ActionEvent a) {
        if (!getLevelCrossingMastInformation()) {
            return;
        }
        xingBlocks = levelXing.getBlockBoundaries();

        xingSignalMastA.setTextField(levelXing.getSignalAMastName());
        xingSignalMastB.setTextField(levelXing.getSignalBMastName());
        xingSignalMastC.setTextField(levelXing.getSignalCMastName());
        xingSignalMastD.setTextField(levelXing.getSignalDMastName());

        xingSignalMastA.setBoundaryLabel(xingBlocks[0]);
        xingSignalMastB.setBoundaryLabel(xingBlocks[1]);
        xingSignalMastC.setBoundaryLabel(xingBlocks[2]);
        xingSignalMastD.setBoundaryLabel(xingBlocks[3]);

        boolean boundaryFlag = false;
        signalMastLevelXingPanel.remove(xingSignalMastA.getDetailsPanel());
        signalMastLevelXingPanel.remove(xingSignalMastB.getDetailsPanel());
        signalMastLevelXingPanel.remove(xingSignalMastC.getDetailsPanel());
        signalMastLevelXingPanel.remove(xingSignalMastD.getDetailsPanel());
        if (xingBlocks[0] != null) {
            signalMastLevelXingPanel.add(xingSignalMastA.getDetailsPanel());
            boundaryFlag = true;
        }
        if (xingBlocks[1] != null) {
            signalMastLevelXingPanel.add(xingSignalMastB.getDetailsPanel());
            boundaryFlag = true;
        }
        if (xingBlocks[2] != null) {
            signalMastLevelXingPanel.add(xingSignalMastC.getDetailsPanel());
            boundaryFlag = true;
        }
        if (xingBlocks[3] != null) {
            signalMastLevelXingPanel.add(xingSignalMastD.getDetailsPanel());
            boundaryFlag = true;
        }
        if (!boundaryFlag) {
            JOptionPane.showMessageDialog(setSignalMastsAtLevelXingFrame, "There are no block boundaries on this level crossing\nIt is therefore not possible to add Signal Masts to it");
        }
        setSignalMastsAtLevelXingFrame.setPreferredSize(null);
        setSignalMastsAtLevelXingFrame.pack();
    }   //xingSignalMastsGetSaved

    private boolean getLevelCrossingMastInformation() {
        if (!setSignalMastsAtLevelXingFromMenuFlag) {
            levelXing = null;
            List<LevelXing> levelXings = layoutEditor.getLevelXings();
            if (levelXings.size() <= 0) {
                JOptionPane.showMessageDialog(setSignalMastsAtLevelXingFrame,
                        Bundle.getMessage("SignalsError15"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            } else if (levelXings.size() == 1) {
                levelXing = levelXings.get(0);
            } else {
                LayoutBlock xingBlockA = null;
                LayoutBlock xingBlockC = null;
                xingBlockA = getBlockFromEntry(xingBlockACComboBox);
                if (xingBlockA == null) {
                    return false;
                }

                String theBlockName = xingBlockBDComboBox.getSelectedItemDisplayName();
                if ((theBlockName != null) && !theBlockName.isEmpty()) {
                    xingBlockC = getBlockFromEntry(xingBlockBDComboBox);
                    if (xingBlockC == null) {
                        return false;
                    }
                }

                int foundCount = 0;
                //make two block tests first
                if (xingBlockC != null) {
                    for (LevelXing x : layoutEditor.getLevelXings()) {
                        LayoutBlock xA = null;
                        LayoutBlock xB = null;
                        LayoutBlock xC = null;
                        LayoutBlock xD = null;
                        LayoutBlock xAC = x.getLayoutBlockAC();
                        LayoutBlock xBD = x.getLayoutBlockBD();
                        if (x.getConnectA() != null) {
                            xA = ((TrackSegment) x.getConnectA()).getLayoutBlock();
                        }
                        if (x.getConnectB() != null) {
                            xB = ((TrackSegment) x.getConnectB()).getLayoutBlock();
                        }
                        if (x.getConnectC() != null) {
                            xC = ((TrackSegment) x.getConnectC()).getLayoutBlock();
                        }
                        if (x.getConnectD() != null) {
                            xD = ((TrackSegment) x.getConnectD()).getLayoutBlock();
                        }
                        if (((xA != null) && (xC != null) && (((xA == xingBlockA) && (xC == xingBlockC))
                                || ((xA == xingBlockC) && (xC == xingBlockA))))
                                || ((xB != null) && (xD != null) && (((xB == xingBlockA) && (xD == xingBlockC))
                                || ((xB == xingBlockC) && (xD == xingBlockA))))) {
                            levelXing = x;
                            foundCount++;
                        } else if ((xAC != null) && (xBD != null) && (((xAC == xingBlockA) && (xBD == xingBlockC))
                                || ((xAC == xingBlockC) && (xBD == xingBlockA)))) {
                            levelXing = x;
                            foundCount++;
                        }
                    }
                }
                if (foundCount == 0) {
                    //try one block test
                    for (LevelXing x : layoutEditor.getLevelXings()) {
                        if ((xingBlockA == x.getLayoutBlockAC()) || (xingBlockA == x.getLayoutBlockBD())) {
                            levelXing = x;
                            foundCount++;
                        }
                    }
                }
                if (foundCount > 1) {
                    JOptionPane.showMessageDialog(setSignalMastsAtLevelXingFrame,
                            Bundle.getMessage("SignalsError16",
                                    new Object[]{" " + foundCount + " "}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (levelXing == null) {
                    JOptionPane.showMessageDialog(setSignalMastsAtLevelXingFrame,
                            Bundle.getMessage("SignalsError17"),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }   //getLevelCrossingMastInformation

    private void setXingSignalMastsCancelPressed(ActionEvent a) {
        setSignalMastsAtLevelXingOpenFlag = false;
        setSignalMastsAtLevelXingFrame.setVisible(false);
    }

    private void setXingSignalMastsDonePressed(ActionEvent a) {
        if (!getLevelCrossingMastInformation()) {
            return;
        }
        SignalMast aMast = getSignalMastFromEntry(xingSignalMastA.getText(), false, setSignalMastsAtLevelXingFrame);
        SignalMast bMast = getSignalMastFromEntry(xingSignalMastB.getText(), false, setSignalMastsAtLevelXingFrame);
        SignalMast cMast = getSignalMastFromEntry(xingSignalMastC.getText(), false, setSignalMastsAtLevelXingFrame);
        SignalMast dMast = getSignalMastFromEntry(xingSignalMastD.getText(), false, setSignalMastsAtLevelXingFrame);
        //if ( !getXingSignalMastInformation() ) return;
        //place or update signals as requested
        if ((aMast != null) && xingSignalMastA.addToPanel()) {
            if (isSignalMastOnPanel(aMast)
                    && (aMast != levelXing.getSignalAMast())) {
                JOptionPane.showMessageDialog(setSignalMastsAtLevelXingFrame,
                        Bundle.getMessage("SignalMastsError6",
                                new Object[]{xingSignalMastA.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(levelXing.getSignalAMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(xingSignalMastA.getText());
                placingBlock(l, xingSignalMastA.isRightSelected(), 0.0, levelXing.getConnectA(), levelXing.getCoordsA());
                removeAssignment(aMast);
                levelXing.setSignalAMast(xingSignalMastA.getText());
                needRedraw = true;
            }
        } else if ((aMast != null)
                && (aMast != levelXing.getSignalAMast())
                && (aMast != levelXing.getSignalBMast())
                && (aMast != levelXing.getSignalCMast())
                && (aMast != levelXing.getSignalDMast())) {
            if (isSignalMastOnPanel(aMast)) {
                JOptionPane.showMessageDialog(setSignalMastsAtLevelXingFrame,
                        Bundle.getMessage("SignalMastsError13",
                                new Object[]{xingSignalMastA.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(levelXing.getSignalAMast());
                removeAssignment(aMast);
                levelXing.setSignalAMast(xingSignalMastA.getText());
            }
        } else if ((aMast != null)
                && ((aMast == levelXing.getSignalBMast())
                || (aMast == levelXing.getSignalCMast())
                || (aMast == levelXing.getSignalDMast()))) {
            //need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (aMast == null) {
            removeSignalMastFromPanel(levelXing.getSignalAMast());
            levelXing.setSignalAMast("");
        }
        if ((bMast != null) && xingSignalMastB.addToPanel()) {
            if (isSignalMastOnPanel(bMast)
                    && (bMast != levelXing.getSignalBMast())) {
                JOptionPane.showMessageDialog(setSignalMastsAtLevelXingFrame,
                        Bundle.getMessage("SignalMastsError6",
                                new Object[]{xingSignalMastB.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(levelXing.getSignalBMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(xingSignalMastB.getText());
                placingBlock(l, xingSignalMastB.isRightSelected(), 0.0, levelXing.getConnectB(), levelXing.getCoordsB());
                removeAssignment(bMast);
                levelXing.setSignalBMast(xingSignalMastB.getText());
                needRedraw = true;
            }
        } else if ((bMast != null)
                && (bMast != levelXing.getSignalAMast())
                && (bMast != levelXing.getSignalBMast())
                && (bMast != levelXing.getSignalCMast())
                && (bMast != levelXing.getSignalDMast())) {
            if (isSignalMastOnPanel(bMast)) {
                JOptionPane.showMessageDialog(setSignalMastsAtLevelXingFrame,
                        Bundle.getMessage("SignalMastsError13",
                                new Object[]{xingSignalMastB.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(levelXing.getSignalBMast());
                removeAssignment(bMast);
                levelXing.setSignalBMast(xingSignalMastB.getText());
            }
        } else if ((bMast != null)
                && ((bMast == levelXing.getSignalAMast())
                || (bMast == levelXing.getSignalCMast())
                || (bMast == levelXing.getSignalBMast())
                || (bMast == levelXing.getSignalDMast()))) {
            //need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (bMast == null) {
            removeSignalMastFromPanel(levelXing.getSignalBMast());
            levelXing.setSignalBMast("");
        }
        if ((cMast != null) && xingSignalMastC.addToPanel()) {
            if (isSignalMastOnPanel(cMast)
                    && (cMast != levelXing.getSignalCMast())) {
                JOptionPane.showMessageDialog(setSignalMastsAtLevelXingFrame,
                        Bundle.getMessage("SignalMastsError6",
                                new Object[]{xingSignalMastC.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(levelXing.getSignalCMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(xingSignalMastC.getText());
                placingBlock(l, xingSignalMastC.isRightSelected(), 0.0, levelXing.getConnectC(), levelXing.getCoordsC());
                removeAssignment(cMast);
                levelXing.setSignalCMast(xingSignalMastC.getText());
                needRedraw = true;
            }
        } else if ((cMast != null)
                && (cMast != levelXing.getSignalAMast())
                && (cMast != levelXing.getSignalBMast())
                && (cMast != levelXing.getSignalCMast())
                && (cMast != levelXing.getSignalDMast())) {
            if (isSignalMastOnPanel(cMast)) {
                JOptionPane.showMessageDialog(setSignalMastsAtLevelXingFrame,
                        Bundle.getMessage("SignalMastsError13",
                                new Object[]{xingSignalMastC.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(levelXing.getSignalCMast());
                removeAssignment(cMast);
                levelXing.setSignalCMast(xingSignalMastC.getText());
            }
        } else if ((cMast != null)
                && ((cMast == levelXing.getSignalBMast())
                || (cMast == levelXing.getSignalAMast())
                || (cMast == levelXing.getSignalDMast()))) {
            //need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (cMast == null) {
            removeSignalMastFromPanel(levelXing.getSignalCMast());
            levelXing.setSignalCName("");
        }
        if ((dMast != null) && xingSignalMastD.addToPanel()) {
            if (isSignalMastOnPanel(dMast)
                    && (dMast != levelXing.getSignalDMast())) {
                JOptionPane.showMessageDialog(setSignalMastsAtLevelXingFrame,
                        Bundle.getMessage("SignalMastsError6",
                                new Object[]{xingSignalMastD.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(levelXing.getSignalDMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(xingSignalMastD.getText());
                placingBlock(l, xingSignalMastD.isRightSelected(), 0.0, levelXing.getConnectD(), levelXing.getCoordsD());
                removeAssignment(dMast);
                levelXing.setSignalDMast(xingSignalMastD.getText());
                needRedraw = true;
            }
        } else if ((dMast != null)
                && (dMast != levelXing.getSignalAMast())
                && (dMast != levelXing.getSignalBMast())
                && (dMast != levelXing.getSignalCMast())
                && (dMast != levelXing.getSignalDMast())) {
            if (isSignalMastOnPanel(dMast)) {
                JOptionPane.showMessageDialog(setSignalMastsAtLevelXingFrame,
                        Bundle.getMessage("SignalMastsError13",
                                new Object[]{xingSignalMastD.getText()}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(levelXing.getSignalDMast());
                removeAssignment(dMast);
                levelXing.setSignalDMast(xingSignalMastD.getText());
            }
        } else if ((dMast != null)
                && ((dMast == levelXing.getSignalBMast())
                || (dMast == levelXing.getSignalCMast())
                || (dMast == levelXing.getSignalAMast()))) {
            //need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (dMast == null) {
            removeSignalMastFromPanel(levelXing.getSignalDMast());
            levelXing.setSignalDMast("");
        }
        //setup logic if requested
        //finish up
        setSignalMastsAtLevelXingOpenFlag = false;
        setSignalMastsAtLevelXingFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }   //setXingSignalMastsDonePressed

    /*=====================*\
    |* setSensorsAtTurnout *|
    \*=====================*/
    private JmriJFrame setSensorsAtTurnoutFrame = null;
    private boolean setSensorsAtTurnoutOpenFlag = false;
    private boolean setSensorsAtTurnoutFromMenuFlag = false;

    private JFrame turnoutSensorFrame = null;
    private JLabel turnoutSensorNameLabel = null;

    private final NamedBeanComboBox<Turnout> sensorsTurnoutComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.turnoutManagerInstance(),
                    null, DisplayOptions.DISPLAYNAME);

    private JButton setSensorsDone;
    private JButton getSavedSensors;
    private JButton setSensorsCancel;
    private JButton changeSensorIcon = null;

    private String[] turnoutSenBlocks = new String[4];

    BeanDetails<Sensor> turnoutSensorA;
    BeanDetails<Sensor> turnoutSensorB;
    BeanDetails<Sensor> turnoutSensorC;
    BeanDetails<Sensor> turnoutSensorD;

    JPanel sensorTurnoutPanel = new JPanel(new FlowLayout());

    public void setSensorsAtTurnoutFromMenu(@Nonnull LayoutTurnout to,
            @Nonnull String[] blocks,
            @Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame frame) {
        sensorIconEditor = theEditor;
        layoutTurnout = to;
        turnout = to.getTurnout();
        sensorsTurnoutComboBox.setSelectedItem(turnout);
        turnoutSenBlocks = new String[4];
        for (int i = 0; i < blocks.length; i++) {
            turnoutSenBlocks[i] = blocks[i];
        }
        setSensorsAtTurnoutFromMenuFlag = true;
        setSensorsAtTurnout(frame);
        setSensorsAtTurnoutFromMenuFlag = false;
    }

    //TODO: Add to Tools menu?
    public void setSensorsAtTurnout(@Nonnull JFrame frame) {
        turnoutSensorFrame = frame;

        //Initialize if needed
        if (setSensorsAtTurnoutFrame == null) {
            setSensorsAtTurnoutOpenFlag = false;

            turnoutSensorA = new BeanDetails<>("Sensor", InstanceManager.sensorManagerInstance());  //NOI18N
            turnoutSensorB = new BeanDetails<>("Sensor", InstanceManager.sensorManagerInstance());  //NOI18N
            turnoutSensorC = new BeanDetails<>("Sensor", InstanceManager.sensorManagerInstance());  //NOI18N
            turnoutSensorD = new BeanDetails<>("Sensor", InstanceManager.sensorManagerInstance());  //NOI18N

            turnoutSensorA.getDetailsPanel().setBackground(new Color(255, 255, 200));
            turnoutSensorB.getDetailsPanel().setBackground(new Color(200, 255, 255));
            turnoutSensorC.getDetailsPanel().setBackground(new Color(200, 200, 255));
            turnoutSensorD.getDetailsPanel().setBackground(new Color(255, 200, 200));

            setSensorsAtTurnoutFrame = new JmriJFrame(Bundle.getMessage("SensorsAtTurnout"), false, true);
            oneFrameToRuleThemAll(setSensorsAtTurnoutFrame);
            setSensorsAtTurnoutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//         setSensorsAtTurnoutFrame.addHelpMenu("package.jmri.jmrit.display.SetSensorsAtTurnout", true);
            setSensorsAtTurnoutFrame.setLocation(70, 30);
            Container theContentPane = setSensorsAtTurnoutFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel1 = new JPanel(new FlowLayout());

            turnoutSensorNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " "
                    + Bundle.getMessage("Name"));
            panel1.add(turnoutSensorNameLabel);
            panel1.add(sensorsTurnoutComboBox);
            sensorsTurnoutComboBox.setToolTipText(Bundle.getMessage("SensorsTurnoutNameHint"));

            theContentPane.add(panel1);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel2 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("Sensors"));
            panel2.add(shTitle);
            panel2.add(new JLabel("	  "));
            panel2.add(getSavedSensors = new JButton(Bundle.getMessage("GetSaved")));
            getSavedSensors.addActionListener((ActionEvent e) -> {
                turnoutSensorsGetSaved(e);
            });
            getSavedSensors.setToolTipText(Bundle.getMessage("GetSavedHint"));
            theContentPane.add(panel2);

            sensorTurnoutPanel.setLayout(new GridLayout(0, 2)); //Content added as needed
            theContentPane.add(sensorTurnoutPanel);

            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(changeSensorIcon = new JButton(Bundle.getMessage("ChangeSensorIcon")));
            changeSensorIcon.addActionListener((ActionEvent e) -> {
                turnoutSensorFrame.setVisible(true);
            });
            changeSensorIcon.setToolTipText(Bundle.getMessage("ChangeSensorIconHint"));
            panel6.add(new JLabel("	 "));
            panel6.add(setSensorsDone = new JButton(Bundle.getMessage("ButtonDone")));
            setSensorsDone.addActionListener((ActionEvent e) -> {
                setSensorsDonePressed(e);
            });
            setSensorsDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));
            panel6.add(setSensorsCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setSensorsCancel.addActionListener((ActionEvent e) -> {
                setSensorsCancelPressed(e);
            });
            setSensorsCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
            setSensorsAtTurnoutFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSensorsCancelPressed(null);
                }
            });
        }

        sensorTurnoutPanel.removeAll();

        sensorsTurnoutComboBox.setVisible(!setSensorsAtTurnoutFromMenuFlag);

        if (setSensorsAtTurnoutFromMenuFlag) {
            turnoutSensorNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameTurnout")
                    + " " + Bundle.getMessage("Name"))
                    + " " + layoutTurnout.getTurnoutName());
            turnoutSensorsGetSaved(null);
        } else {
            turnoutSensorNameLabel.setText(Bundle.getMessage("BeanNameTurnout") + " "
                    + Bundle.getMessage("Name"));
        }

        if (!setSensorsAtTurnoutOpenFlag) {
            setSensorsAtTurnoutFrame.setPreferredSize(null);
            setSensorsAtTurnoutFrame.pack();
            setSensorsAtTurnoutOpenFlag = true;
        }
        setSensorsAtTurnoutFrame.setVisible(true);
    }   //setSensorsAtTurnout

    private void turnoutSensorsGetSaved(ActionEvent a) {
        if (!getTurnoutSensorInformation()) {
            return;
        }
        turnoutSenBlocks = layoutTurnout.getBlockBoundaries();

        turnoutSensorA.setTextField(layoutTurnout.getSensorAName());
        turnoutSensorB.setTextField(layoutTurnout.getSensorBName());
        turnoutSensorC.setTextField(layoutTurnout.getSensorCName());
        turnoutSensorD.setTextField(layoutTurnout.getSensorDName());

        turnoutSensorA.setBoundaryLabel(turnoutSenBlocks[0]);
        turnoutSensorB.setBoundaryLabel(turnoutSenBlocks[1]);
        turnoutSensorC.setBoundaryLabel(turnoutSenBlocks[2]);
        turnoutSensorD.setBoundaryLabel(turnoutSenBlocks[3]);

        sensorTurnoutPanel.remove(turnoutSensorA.getDetailsPanel());
        sensorTurnoutPanel.remove(turnoutSensorB.getDetailsPanel());
        sensorTurnoutPanel.remove(turnoutSensorC.getDetailsPanel());
        sensorTurnoutPanel.remove(turnoutSensorD.getDetailsPanel());

        boolean boundaryFlag = false;
        if (turnoutSenBlocks[0] != null) {
            sensorTurnoutPanel.add(turnoutSensorA.getDetailsPanel());
            boundaryFlag = true;
        }
        if (turnoutSenBlocks[1] != null) {
            sensorTurnoutPanel.add(turnoutSensorB.getDetailsPanel());
            boundaryFlag = true;
        }
        if (turnoutSenBlocks[2] != null) {
            sensorTurnoutPanel.add(turnoutSensorC.getDetailsPanel());
            boundaryFlag = true;
        }
        if (turnoutSenBlocks[3] != null) {
            sensorTurnoutPanel.add(turnoutSensorD.getDetailsPanel());
            boundaryFlag = true;
        }
        if (!boundaryFlag) {
            JOptionPane.showMessageDialog(setSensorsAtTurnoutFrame, "There are no block boundaries on this turnout\nIt is therefore not possible to add Sensors to it");
        }
        setSensorsAtTurnoutFrame.setPreferredSize(null);
        setSensorsAtTurnoutFrame.pack();
    }   //turnoutSensorsGetSaved

    SensorIcon turnoutSensorBlockIcon;

    private void setSensorsDonePressed(ActionEvent a) {
        log.trace("setSensorsDonePressed (turnouts)");  //NOI18N
        if (!getTurnoutSensorInformation()) {
            return;
        }

        //process sensor names
        Sensor sensorA = getSensorFromEntry(turnoutSensorA.getText(), false, setSensorsAtTurnoutFrame);
        Sensor sensorB = getSensorFromEntry(turnoutSensorB.getText(), false, setSensorsAtTurnoutFrame);
        Sensor sensorC = getSensorFromEntry(turnoutSensorC.getText(), false, setSensorsAtTurnoutFrame);
        Sensor sensorD = getSensorFromEntry(turnoutSensorD.getText(), false, setSensorsAtTurnoutFrame);

        Sensor currSensorA = layoutTurnout.getSensorA();
        Sensor currSensorB = layoutTurnout.getSensorB();
        Sensor currSensorC = layoutTurnout.getSensorC();
        Sensor currSensorD = layoutTurnout.getSensorD();

        if (log.isTraceEnabled()) {
            log.trace("current sensors: A = {}, B = {}, C = {}, D = {}", //NOI18N
                    (currSensorA == null) ? "- none- " : currSensorA.getDisplayName(), //NOI18N
                    (currSensorB == null) ? "- none- " : currSensorB.getDisplayName(), //NOI18N
                    (currSensorC == null) ? "- none- " : currSensorC.getDisplayName(), //NOI18N
                    (currSensorD == null) ? "- none- " : currSensorD.getDisplayName());  //NOI18N
            log.trace("new sensors: A = {}, B = {}, C = {}, D = {}", //NOI18N
                    (sensorA == null) ? "- none- " : sensorA.getDisplayName(), //NOI18N
                    (sensorB == null) ? "- none- " : sensorB.getDisplayName(), //NOI18N
                    (sensorC == null) ? "- none- " : sensorC.getDisplayName(), //NOI18N
                    (sensorD == null) ? "- none- " : sensorD.getDisplayName());  //NOI18N
        }

        //place/remove sensors as requested
        if (sensorA == null) {
            if (currSensorA != null && removeSensorFromPanel(currSensorA)) {
                layoutTurnout.setSensorA(null);
            }
        } else if (turnoutSensorA != null && layoutTurnout.getConnectA() != null) {
            setTurnoutSensor(layoutTurnout, sensorA, currSensorA, turnoutSensorA, layoutTurnout.getConnectA(), layoutTurnout.getCoordsA(), "A");
        }

        if (sensorB == null) {
            if (currSensorB != null && removeSensorFromPanel(currSensorB)) {
                layoutTurnout.setSensorB(null);
            }
        } else if (turnoutSensorB != null && layoutTurnout.getConnectB() != null) {
            setTurnoutSensor(layoutTurnout, sensorB, currSensorB, turnoutSensorB, layoutTurnout.getConnectB(), layoutTurnout.getCoordsB(), "B");
        }

        if (sensorC == null) {
            if (currSensorC != null && removeSensorFromPanel(currSensorC)) {
                layoutTurnout.setSensorC(null);
            }
        } else if (turnoutSensorC != null && layoutTurnout.getConnectC() != null) {
            setTurnoutSensor(layoutTurnout, sensorC, currSensorC, turnoutSensorC, layoutTurnout.getConnectC(), layoutTurnout.getCoordsC(), "C");
        }

        if (sensorD == null) {
            if (currSensorD != null && removeSensorFromPanel(currSensorD)) {
                layoutTurnout.setSensorD(null);
            }
        } else if (turnoutSensorD != null && layoutTurnout.getConnectD() != null) {
            setTurnoutSensor(layoutTurnout, sensorD, currSensorD, turnoutSensorD, layoutTurnout.getConnectD(), layoutTurnout.getCoordsD(), "D");
        }

        //make sure this layout turnout is not linked to another
        layoutTurnout.setLinkType(LayoutTurnout.NO_LINK);
        layoutTurnout.setLinkedTurnoutName("");

        //finish up
        setSensorsAtTurnoutOpenFlag = false;
        setSensorsAtTurnoutFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }   //setSensorsDonePressed

    /**
     * Attached a sensor to a turnout block boundary. Supports both
     * LayoutTurnout and LayoutSlip classes.
     *
     * @since 4.11.2
     * @param trackItem  The turnout or slip that is being modified.
     * @param newSensor  The sensor that is being added.
     * @param currSensor The sensor that might already be there, otherwise null.
     * @param beanDetail The BeanDetails object that contains the supporting
     *                   data.
     * @param connect    The track segment that is attached to this point
     * @param coords     The track componennt coordinates
     * @param position   Which of the four points is being changed
     */
    <T extends LayoutTurnout> void setTurnoutSensor(T trackItem, Sensor newSensor, Sensor currSensor,
            BeanDetails beanDetail, LayoutTrack connect, Point2D coords, String position) {
        if (currSensor == null) {
            if (!isSensorAssignedAnywhere(newSensor)) {
                log.trace("Add sensor '{}'", newSensor.getDisplayName());  //NOI18N
                switch (position) {
                    case "A":  //NOI18N
                        trackItem.setSensorA(beanDetail.getText());
                        break;
                    case "B":  //NOI18N
                        trackItem.setSensorB(beanDetail.getText());
                        break;
                    case "C":  //NOI18N
                        trackItem.setSensorC(beanDetail.getText());
                        break;
                    case "D":  //NOI18N
                        trackItem.setSensorD(beanDetail.getText());
                        break;
                    default:
                        break;
                }
                if (beanDetail.addToPanel()) {
                    log.trace("Add icon for sensor '{}'", newSensor.getDisplayName());  //NOI18N
                    placingBlock(getSensorIcon(beanDetail.getText()),
                            beanDetail.isRightSelected(), 0.0,
                            connect, coords);
                    needRedraw = true;
                }
            } else {
                sensorAssignedElseWhere(newSensor);
            }
        } else if (currSensor == newSensor) {
            if (beanDetail.addToPanel()) {
                if (!isSensorOnPanel(newSensor)) {
                    log.trace("Add icon for existing sensor '{}'", newSensor.getDisplayName());  //NOI18N
                    placingBlock(getSensorIcon(beanDetail.getText()),
                            beanDetail.isRightSelected(), 0.0,
                            connect, coords);
                    needRedraw = true;
                }
            }
        } else {
            if (!isSensorAssignedAnywhere(newSensor)) {
                if (removeSensorFromPanel(currSensor)) {
                    log.trace("Replace sensor '{}' with sensor '{}'", //NOI18N
                            currSensor.getDisplayName(), newSensor.getDisplayName());
                    switch (position) {
                        case "A":  //NOI18N
                            trackItem.setSensorA(beanDetail.getText());
                            break;
                        case "B":  //NOI18N
                            trackItem.setSensorB(beanDetail.getText());
                            break;
                        case "C":  //NOI18N
                            trackItem.setSensorC(beanDetail.getText());
                            break;
                        case "D":  //NOI18N
                            trackItem.setSensorD(beanDetail.getText());
                            break;
                        default:
                            break;
                    }
                    if (beanDetail.addToPanel()) {
                        log.trace("Add icon for replacement sensor '{}'", //NOI18N
                                newSensor.getDisplayName());
                        placingBlock(getSensorIcon(beanDetail.getText()),
                                beanDetail.isRightSelected(), 0.0,
                                connect, coords);
                        needRedraw = true;
                    }
                }
            } else {
                sensorAssignedElseWhere(newSensor);
            }
        }
    }

    private boolean getTurnoutSensorInformation() {
        turnout = null;
        layoutTurnout = null;
        String str = sensorsTurnoutComboBox.getSelectedItemDisplayName();
        if ((str == null) || str.isEmpty()) {
            JOptionPane.showMessageDialog(setSensorsAtTurnoutFrame, Bundle.getMessage("SensorsError1"),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        turnout = InstanceManager.turnoutManagerInstance().getTurnout(str);
        if (turnout == null) {
            JOptionPane.showMessageDialog(setSensorsAtTurnoutFrame,
                    Bundle.getMessage("SensorsError2",
                            new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            String uname = turnout.getUserName();
            if ((uname == null) || uname.isEmpty()
                    || !uname.equals(str)) {
                sensorsTurnoutComboBox.setSelectedItem(turnout);
            }
        }
        layoutTurnout = layoutEditor.getFinder().findLayoutTurnoutByBean(turnout);
        if (layoutTurnout == null) {
            JOptionPane.showMessageDialog(setSensorsAtTurnoutFrame,
                    Bundle.getMessage("SensorsError3",
                            new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }   //getTurnoutSensorInformation

    private void setSensorsCancelPressed(ActionEvent a) {
        setSensorsAtTurnoutOpenFlag = false;
        setSensorsAtTurnoutFrame.setVisible(false);
    }

    /*=======================*\
    |* setSensorsAtLevelXing *|
    \*=======================*/
    //operational variables for Set Sensors at Level Crossing tool
    private JmriJFrame setSensorsAtLevelXingFrame = null;
    private boolean setSensorsAtLevelXingOpenFlag = false;
    private boolean setSensorsAtLevelXingFromMenuFlag = false;

    private JLabel xingSensorsBlockACNameLabel = null;
    private JLabel xingSensorsBlockBDNameLabel = null;

    private final NamedBeanComboBox<Block> xingSensorsBlockACComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(BlockManager.class),
                    null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Block> xingSensorsBlockBDComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(BlockManager.class),
                    null, DisplayOptions.DISPLAYNAME);

    private JButton getSavedXingSensors = null;
    private JButton setXingSensorsDone = null;
    private JButton setXingSensorsCancel = null;
    private JButton changeSensorXingIcon = null;
    JFrame sensorXingFrame = null;

    private String[] xingSensorBlocks = new String[4];

    BeanDetails<Sensor> xingSensorA;
    BeanDetails<Sensor> xingSensorB;
    BeanDetails<Sensor> xingSensorC;
    BeanDetails<Sensor> xingSensorD;

    JPanel sensorXingPanel = new JPanel(new FlowLayout());

    //display dialog for Set Signals at Level Crossing tool
    public void setSensorsAtLevelXingFromMenu(@Nonnull LevelXing xing,
            @Nonnull String[] blocks,
            @Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        levelXing = xing;
        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        xingSensorsBlockACComboBox.setSelectedItem(bm.getBlock(levelXing.getBlockNameAC()));
        xingSensorsBlockBDComboBox.setSelectedItem(bm.getBlock(levelXing.getBlockNameBD()));
        for (int i = 0; i < blocks.length; i++) {
            xingSensorBlocks[i] = blocks[i];
        }
        setSensorsAtLevelXingFromMenuFlag = true;
        setSensorsAtLevelXing(theEditor, theFrame);
        setSensorsAtLevelXingFromMenuFlag = false;
    }

    //TODO: Add to Tools menu?
    public void setSensorsAtLevelXing(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        sensorIconEditor = theEditor;
        sensorXingFrame = theFrame;

        //Initialize if needed
        if (setSensorsAtLevelXingFrame == null) {
            setSensorsAtLevelXingOpenFlag = false;

            xingSensorA = new BeanDetails<>("Sensor", InstanceManager.sensorManagerInstance());
            xingSensorB = new BeanDetails<>("Sensor", InstanceManager.sensorManagerInstance());
            xingSensorC = new BeanDetails<>("Sensor", InstanceManager.sensorManagerInstance());
            xingSensorD = new BeanDetails<>("Sensor", InstanceManager.sensorManagerInstance());

            xingSensorA.getDetailsPanel().setBackground(new Color(255, 255, 200));
            xingSensorB.getDetailsPanel().setBackground(new Color(200, 255, 255));
            xingSensorC.getDetailsPanel().setBackground(new Color(200, 200, 255));
            xingSensorD.getDetailsPanel().setBackground(new Color(255, 200, 200));

            setSensorsAtLevelXingFrame = new JmriJFrame(Bundle.getMessage("SensorsAtLevelXing"), false, true);
            oneFrameToRuleThemAll(setSensorsAtLevelXingFrame);
            setSensorsAtLevelXingFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//         setSensorsAtLevelXingFrame.addHelpMenu("package.jmri.jmrit.display.SetSensorsAtLevelXing", true);
            setSensorsAtLevelXingFrame.setLocation(70, 30);
            Container theContentPane = setSensorsAtLevelXingFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel11 = new JPanel(new FlowLayout());

            xingSensorsBlockACNameLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " AC "
                    + Bundle.getMessage("Name")));
            panel11.add(xingSensorsBlockACNameLabel);
            panel11.add(xingSensorsBlockACComboBox);
            xingSensorsBlockACComboBox.setToolTipText(Bundle.getMessage("SensorsBlockNameHint"));
            theContentPane.add(panel11);

            JPanel panel12 = new JPanel(new FlowLayout());
            xingSensorsBlockBDNameLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " BD "
                    + Bundle.getMessage("Name")));
            panel12.add(xingSensorsBlockBDNameLabel);
            panel12.add(xingSensorsBlockBDComboBox);
            xingSensorsBlockBDComboBox.setToolTipText(Bundle.getMessage("SensorsBlockNameHint"));
            theContentPane.add(panel12);

            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel2 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("BeanNameSensor"));
            panel2.add(shTitle);
            panel2.add(new JLabel("	  "));
            panel2.add(getSavedXingSensors = new JButton(Bundle.getMessage("GetSaved")));
            getSavedXingSensors.addActionListener((ActionEvent e) -> {
                xingSensorsGetSaved(e);
            });
            getSavedXingSensors.setToolTipText(Bundle.getMessage("GetSavedHint"));
            theContentPane.add(panel2);

            sensorXingPanel.setLayout(new GridLayout(0, 2));
            theContentPane.add(sensorXingPanel);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(changeSensorXingIcon = new JButton(Bundle.getMessage("ChangeSensorIcon")));
            changeSensorXingIcon.addActionListener((ActionEvent e) -> {
                sensorXingFrame.setVisible(true);
            });
            changeSensorXingIcon.setToolTipText(Bundle.getMessage("ChangeSensorIconHint"));

            panel6.add(new JLabel("	 "));
            panel6.add(setXingSensorsDone = new JButton(Bundle.getMessage("ButtonDone")));
            setXingSensorsDone.addActionListener((ActionEvent e) -> {
                setXingSensorsDonePressed(e);
            });
            setXingSensorsDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));
            panel6.add(setXingSensorsCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setXingSensorsCancel.addActionListener((ActionEvent e) -> {
                setXingSensorsCancelPressed(e);
            });
            setXingSensorsCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
            setSensorsAtLevelXingFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setXingSensorsCancelPressed(null);
                }
            });
        }

        sensorXingPanel.removeAll();

        xingSensorsBlockACComboBox.setVisible(!setSensorsAtLevelXingFromMenuFlag);
        xingSensorsBlockBDComboBox.setVisible(!setSensorsAtLevelXingFromMenuFlag);

        if (setSensorsAtLevelXingFromMenuFlag) {
            xingSensorsBlockACNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " AC "
                    + Bundle.getMessage("Name")) + " " + levelXing.getBlockNameAC());
            xingSensorsBlockBDNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " BD "
                    + Bundle.getMessage("Name")) + " " + levelXing.getBlockNameBD());

            xingSensorA.setTextField(levelXing.getSensorAName());
            xingSensorB.setTextField(levelXing.getSensorBName());
            xingSensorC.setTextField(levelXing.getSensorCName());
            xingSensorD.setTextField(levelXing.getSensorDName());
            xingSensorsGetSaved(null);
        } else {
            xingSensorsBlockACNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " AC "
                    + Bundle.getMessage("Name")));
            xingSensorsBlockBDNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " BD "
                    + Bundle.getMessage("Name")));
        }

        if (!setSensorsAtLevelXingOpenFlag) {
            setSensorsAtLevelXingFrame.setPreferredSize(null);
            setSensorsAtLevelXingFrame.pack();
            setSensorsAtLevelXingOpenFlag = true;
        }
        setSensorsAtLevelXingFrame.setVisible(true);
    }

    private void xingSensorsGetSaved(ActionEvent a) {
        if (!getLevelCrossingSensorInformation()) {
            return;
        }

        xingSensorBlocks = levelXing.getBlockBoundaries();

        xingSensorA.setTextField(levelXing.getSensorAName());
        xingSensorB.setTextField(levelXing.getSensorBName());
        xingSensorC.setTextField(levelXing.getSensorCName());
        xingSensorD.setTextField(levelXing.getSensorDName());

        sensorXingPanel.remove(xingSensorA.getDetailsPanel());
        sensorXingPanel.remove(xingSensorB.getDetailsPanel());
        sensorXingPanel.remove(xingSensorC.getDetailsPanel());
        sensorXingPanel.remove(xingSensorD.getDetailsPanel());

        xingSensorA.setBoundaryLabel(xingSensorBlocks[0]);
        xingSensorB.setBoundaryLabel(xingSensorBlocks[1]);
        xingSensorC.setBoundaryLabel(xingSensorBlocks[2]);
        xingSensorD.setBoundaryLabel(xingSensorBlocks[3]);

        boolean boundaryFlag = false;
        if (xingSensorBlocks[0] != null) {
            sensorXingPanel.add(xingSensorA.getDetailsPanel());
            boundaryFlag = true;
        }
        if (xingSensorBlocks[1] != null) {
            sensorXingPanel.add(xingSensorB.getDetailsPanel());
            boundaryFlag = true;
        }
        if (xingSensorBlocks[2] != null) {
            sensorXingPanel.add(xingSensorC.getDetailsPanel());
            boundaryFlag = true;
        }
        if (xingSensorBlocks[3] != null) {
            sensorXingPanel.add(xingSensorD.getDetailsPanel());
            boundaryFlag = true;
        }
        if (!boundaryFlag) {
            JOptionPane.showMessageDialog(setSensorsAtLevelXingFrame, Bundle.getMessage("NoBoundaryXingSensor"));
        }
        setSensorsAtLevelXingFrame.setPreferredSize(null);
        setSensorsAtLevelXingFrame.pack();
    }

    private boolean getLevelCrossingSensorInformation() {
        if (!setSensorsAtLevelXingFromMenuFlag) {
            levelXing = null;
            List<LevelXing> levelXings = layoutEditor.getLevelXings();
            if (levelXings.size() <= 0) {
                JOptionPane.showMessageDialog(setSensorsAtLevelXingFrame,
                        Bundle.getMessage("SignalsError15"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            } else if (levelXings.size() == 1) {
                levelXing = levelXings.get(0);
            } else {
                LayoutBlock xingSensorBlockA = null;
                LayoutBlock xingSensorBlockC = null;
                xingSensorBlockA = getBlockFromEntry(xingSensorsBlockACComboBox);
                if (xingSensorBlockA == null) {
                    return false;
                }
                String theBlockName = xingSensorsBlockBDComboBox.getSelectedItemDisplayName();
                if ((theBlockName != null) && !theBlockName.isEmpty()) {
                    xingSensorBlockC = getBlockFromEntry(xingSensorsBlockBDComboBox);
                    if (xingSensorBlockC == null) {
                        return false;
                    }
                }

                int foundCount = 0;
                //make two block tests first
                if (xingSensorBlockC != null) {
                    for (LevelXing x : layoutEditor.getLevelXings()) {
                        LayoutBlock xA = null;
                        LayoutBlock xB = null;
                        LayoutBlock xC = null;
                        LayoutBlock xD = null;
                        LayoutBlock xAC = x.getLayoutBlockAC();
                        LayoutBlock xBD = x.getLayoutBlockBD();
                        if (x.getConnectA() != null) {
                            xA = ((TrackSegment) x.getConnectA()).getLayoutBlock();
                        }
                        if (x.getConnectB() != null) {
                            xB = ((TrackSegment) x.getConnectB()).getLayoutBlock();
                        }
                        if (x.getConnectC() != null) {
                            xC = ((TrackSegment) x.getConnectC()).getLayoutBlock();
                        }
                        if (x.getConnectD() != null) {
                            xD = ((TrackSegment) x.getConnectD()).getLayoutBlock();
                        }
                        if (((xA != null) && (xC != null) && (((xA == xingSensorBlockA) && (xC == xingSensorBlockC))
                                || ((xA == xingSensorBlockC) && (xC == xingSensorBlockA))))
                                || ((xB != null) && (xD != null) && (((xB == xingSensorBlockA) && (xD == xingSensorBlockC))
                                || ((xB == xingSensorBlockC) && (xD == xingSensorBlockA))))) {
                            levelXing = x;
                            foundCount++;
                        } else if ((xAC != null) && (xBD != null) && (((xAC == xingSensorBlockA) && (xBD == xingSensorBlockC))
                                || ((xAC == xingSensorBlockC) && (xBD == xingSensorBlockA)))) {
                            levelXing = x;
                            foundCount++;
                        }
                    }
                }
                if (foundCount == 0) {
                    //try one block test
                    for (LevelXing x : layoutEditor.getLevelXings()) {
                        if ((xingSensorBlockA == x.getLayoutBlockAC()) || (xingSensorBlockA == x.getLayoutBlockBD())) {
                            levelXing = x;
                            foundCount++;
                        }
                    }
                }
                if (foundCount > 1) {
                    JOptionPane.showMessageDialog(setSensorsAtLevelXingFrame,
                            Bundle.getMessage("SignalsError16",
                                    new Object[]{" " + foundCount + " "}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (levelXing == null) {
                    JOptionPane.showMessageDialog(setSensorsAtLevelXingFrame,
                            Bundle.getMessage("SignalsError17"),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    private void setXingSensorsCancelPressed(ActionEvent a) {
        setSensorsAtLevelXingOpenFlag = false;
        setSensorsAtLevelXingFrame.setVisible(false);
    }

    private void setXingSensorsDonePressed(ActionEvent a) {
        log.trace("setXingSensorsDonePressed");  //NOI18N

        if (!getLevelCrossingSensorInformation()) {
            return;
        }

        Sensor aSensor = getSensorFromEntry(xingSensorA.getText(), false, setSensorsAtLevelXingFrame);
        Sensor bSensor = getSensorFromEntry(xingSensorB.getText(), false, setSensorsAtLevelXingFrame);
        Sensor cSensor = getSensorFromEntry(xingSensorC.getText(), false, setSensorsAtLevelXingFrame);
        Sensor dSensor = getSensorFromEntry(xingSensorD.getText(), false, setSensorsAtLevelXingFrame);

        Sensor currSensorA = levelXing.getSensorA();
        Sensor currSensorB = levelXing.getSensorB();
        Sensor currSensorC = levelXing.getSensorC();
        Sensor currSensorD = levelXing.getSensorD();

        if (log.isTraceEnabled()) {
            log.trace("current sensors: A = {}, B = {}, C = {}, D = {}", //NOI18N
                    (currSensorA == null) ? "- none- " : currSensorA.getDisplayName(), //NOI18N
                    (currSensorB == null) ? "- none- " : currSensorB.getDisplayName(), //NOI18N
                    (currSensorC == null) ? "- none- " : currSensorC.getDisplayName(), //NOI18N
                    (currSensorD == null) ? "- none- " : currSensorD.getDisplayName());  //NOI18N
            log.trace("new sensors: A = {}, B = {}, C = {}, D = {}", //NOI18N
                    (aSensor == null) ? "- none- " : aSensor.getDisplayName(), //NOI18N
                    (bSensor == null) ? "- none- " : bSensor.getDisplayName(), //NOI18N
                    (cSensor == null) ? "- none- " : cSensor.getDisplayName(), //NOI18N
                    (dSensor == null) ? "- none- " : dSensor.getDisplayName());  //NOI18N
        }

        //place/remove sensors as requested
        if (aSensor == null) {
            if (currSensorA != null && removeSensorFromPanel(currSensorA)) {
                levelXing.setSensorAName(null);
            }
        } else if (xingSensorA != null && levelXing.getConnectA() != null) {
            setLevelXingSensor(aSensor, currSensorA, xingSensorA, levelXing.getConnectA(), levelXing.getCoordsA(), "A");
        }

        if (bSensor == null) {
            if (currSensorB != null && removeSensorFromPanel(currSensorB)) {
                levelXing.setSensorBName(null);
            }
        } else if (xingSensorB != null && levelXing.getConnectB() != null) {
            setLevelXingSensor(bSensor, currSensorB, xingSensorB, levelXing.getConnectB(), levelXing.getCoordsB(), "B");
        }

        if (cSensor == null) {
            if (currSensorC != null && removeSensorFromPanel(currSensorC)) {
                levelXing.setSensorCName(null);
            }
        } else if (xingSensorC != null && levelXing.getConnectC() != null) {
            setLevelXingSensor(cSensor, currSensorC, xingSensorC, levelXing.getConnectC(), levelXing.getCoordsC(), "C");
        }

        if (dSensor == null) {
            if (currSensorD != null && removeSensorFromPanel(currSensorD)) {
                levelXing.setSensorDName(null);
            }
        } else if (xingSensorD != null && levelXing.getConnectD() != null) {
            setLevelXingSensor(dSensor, currSensorD, xingSensorD, levelXing.getConnectD(), levelXing.getCoordsD(), "D");
        }

        //setup logic if requested
        //finish up
        setSensorsAtLevelXingOpenFlag = false;
        setSensorsAtLevelXingFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }

    /**
     * Attached a sensor to a level crossing block boundary.
     *
     * @since 4.11.2
     * @param newSensor  The sensor that is being added.
     * @param currSensor The sensor that might already be there, otherwise null.
     * @param beanDetail The BeanDetails object that contains the supporting
     *                   data.
     * @param connect    The track segment that is attached to this point
     * @param coords     The track componennt coordinates
     * @param position   Which of the four points is being changed
     */
    void setLevelXingSensor(Sensor newSensor, Sensor currSensor, BeanDetails beanDetail,
            LayoutTrack connect, Point2D coords, String position) {
        if (currSensor == null) {
            if (!isSensorAssignedAnywhere(newSensor)) {
                log.trace("Add sensor '{}'", newSensor.getDisplayName());  //NOI18N
                switch (position) {
                    case "A":  //NOI18N
                        levelXing.setSensorAName(beanDetail.getText());
                        break;
                    case "B":  //NOI18N
                        levelXing.setSensorBName(beanDetail.getText());
                        break;
                    case "C":  //NOI18N
                        levelXing.setSensorCName(beanDetail.getText());
                        break;
                    case "D":  //NOI18N
                        levelXing.setSensorDName(beanDetail.getText());
                        break;
                    default:
                        break;
                }
                if (beanDetail.addToPanel()) {
                    log.trace("Add icon for sensor '{}'", newSensor.getDisplayName());  //NOI18N
                    placingBlock(getSensorIcon(beanDetail.getText()),
                            beanDetail.isRightSelected(), 0.0, connect, coords);
                    needRedraw = true;
                }
            } else {
                sensorAssignedElseWhere(newSensor);
            }
        } else if (currSensor == newSensor) {
            if (beanDetail.addToPanel()) {
                if (!isSensorOnPanel(newSensor)) {
                    log.trace("Add icon for existing sensor '{}'", newSensor.getDisplayName());  //NOI18N
                    placingBlock(getSensorIcon(beanDetail.getText()),
                            beanDetail.isRightSelected(), 0.0, connect, coords);
                    needRedraw = true;
                }
            }
        } else {
            if (!isSensorAssignedAnywhere(newSensor)) {
                if (removeSensorFromPanel(currSensor)) {
                    log.trace("Replace sensor '{}' with sensor '{}'", //NOI18N
                            currSensor.getDisplayName(), newSensor.getDisplayName());
                    switch (position) {
                        case "A":  //NOI18N
                            levelXing.setSensorAName(beanDetail.getText());
                            break;
                        case "B":  //NOI18N
                            levelXing.setSensorBName(beanDetail.getText());
                            break;
                        case "C":  //NOI18N
                            levelXing.setSensorCName(beanDetail.getText());
                            break;
                        case "D":  //NOI18N
                            levelXing.setSensorDName(beanDetail.getText());
                            break;
                        default:
                            break;
                    }
                    if (beanDetail.addToPanel()) {
                        log.trace("Add icon for replacement sensor '{}'", //NOI18N
                                newSensor.getDisplayName());
                        placingBlock(getSensorIcon(beanDetail.getText()),
                                beanDetail.isRightSelected(), 0.0, connect, coords);
                        needRedraw = true;
                    }
                }
            } else {
                sensorAssignedElseWhere(newSensor);
            }
        }
    }

    private boolean getSimpleBlockInformation() {
        //might have to do something to trick it with an end bumper
        if (!setSignalMastsAtBlockBoundaryFromMenuFlag) {
            block1 = getBlockFromEntry(block1IDComboBox);
            if (block1 == null) {
                return false;
            }
            block2 = getBlockFromEntry(block2IDComboBox);
            boundary = null;
            //if block2 is undefined or same as block 1
            if (block2 == null || (block1 == block2)) {
                //find the 1st positionablePoint that's connect1'ed to block1
                for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                    if (p.getType() == PositionablePoint.END_BUMPER) {
                        if (p.getConnect1() != null && p.getConnect1().getLayoutBlock() == block1) {
                            boundary = p;
                            break;
                        }
                    }
                }
            }

            //now we try to find an anchor that connected to blocks 1 and 2
            //(if this fails boundary will still be set to the pp set if
            //block2 was null or equal to block1 above.)
            for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                if (p.getType() != PositionablePoint.END_BUMPER) {
                    LayoutBlock bA = null;
                    LayoutBlock bB = null;
                    if (p.getConnect1() != null) {
                        bA = p.getConnect1().getLayoutBlock();
                    }
                    if (p.getConnect2() != null) {
                        bB = p.getConnect2().getLayoutBlock();
                    }
                    if ((bA != null) && (bB != null) && (bA != bB)) {
                        if (((bA == block1) && (bB == block2))
                                || ((bA == block2) && (bB == block1))) {
                            boundary = p;
                            break;
                        }
                    }
                }
            }
            //if all that failed...
            if (boundary == null) {
                JOptionPane.showMessageDialog(setSignalsAtBlockBoundaryFrame,
                        Bundle.getMessage("SignalsError7"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    /*==================*\
    |* setSensorsAtSlip *|
    \*==================*/
    //operational variables for Set Sensors at Slip tool
    private JmriJFrame setSensorsAtSlipFrame = null;
    private boolean setSensorsAtSlipOpenFlag = false;
    private boolean setSensorsAtSlipFromMenuFlag = false;

    private JButton getSavedSlipSensors = null;
    private JButton setSlipSensorsDone = null;
    private JButton setSlipSensorsCancel = null;
    private JButton changeSensorSlipIcon = null;
    JFrame sensorSlipFrame = null;

    private final NamedBeanComboBox<Block> slipSensorsBlockAComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(BlockManager.class),
                    null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Block> slipSensorsBlockBComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(BlockManager.class),
                    null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Block> slipSensorsBlockCComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(BlockManager.class),
                    null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Block> slipSensorsBlockDComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(BlockManager.class),
                    null, DisplayOptions.DISPLAYNAME);

    private String[] slipSensorBlocks = new String[4];

    BeanDetails<Sensor> slipSensorA;
    BeanDetails<Sensor> slipSensorB;
    BeanDetails<Sensor> slipSensorC;
    BeanDetails<Sensor> slipSensorD;

    JPanel sensorSlipPanel = new JPanel(new FlowLayout());

    //display dialog for Set Signals at Level Crossing tool
    public void setSensorsAtSlipFromMenu(@Nonnull LayoutSlip slip,
            @Nonnull String[] blocks,
            @Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        layoutSlip = slip;
        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        slipSensorsBlockAComboBox.setSelectedItem(bm.getBlock(layoutSlip.getBlockName()));
        slipSensorsBlockBComboBox.setSelectedItem(bm.getBlock(layoutSlip.getBlockBName()));
        slipSensorsBlockCComboBox.setSelectedItem(bm.getBlock(layoutSlip.getBlockCName()));
        slipSensorsBlockDComboBox.setSelectedItem(bm.getBlock(layoutSlip.getBlockDName()));
        for (int i = 0; i < blocks.length; i++) {
            slipSensorBlocks[i] = blocks[i];
        }
        setSensorsAtSlipFromMenuFlag = true;
        setSensorsAtSlip(theEditor, theFrame);
        setSensorsAtSlipFromMenuFlag = false;
    }

    //TODO: Add to Tools menu?
    public void setSensorsAtSlip(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        sensorIconEditor = theEditor;
        sensorSlipFrame = theFrame;

        //Initialize if needed
        if (setSensorsAtSlipFrame == null) {
            setSensorsAtSlipOpenFlag = false;

            slipSensorA = new BeanDetails<>("Sensor", InstanceManager.sensorManagerInstance());
            slipSensorB = new BeanDetails<>("Sensor", InstanceManager.sensorManagerInstance());
            slipSensorC = new BeanDetails<>("Sensor", InstanceManager.sensorManagerInstance());
            slipSensorD = new BeanDetails<>("Sensor", InstanceManager.sensorManagerInstance());

            slipSensorA.getDetailsPanel().setBackground(new Color(255, 255, 200));
            slipSensorB.getDetailsPanel().setBackground(new Color(200, 255, 255));
            slipSensorC.getDetailsPanel().setBackground(new Color(200, 200, 255));
            slipSensorD.getDetailsPanel().setBackground(new Color(255, 200, 200));

            setSensorsAtSlipFrame = new JmriJFrame(Bundle.getMessage("SensorsAtSlip"), false, true);
            oneFrameToRuleThemAll(setSensorsAtSlipFrame);
            setSensorsAtSlipFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//         setSensorsAtSlipFrame.addHelpMenu("package.jmri.jmrit.display.SetSensorsAtLevelSlip", true);
            setSensorsAtSlipFrame.setLocation(70, 30);
            Container theContentPane = setSensorsAtSlipFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel11A = new JPanel(new FlowLayout());
            slipSignalBlockANameLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " A "
                    + Bundle.getMessage("Name")));
            panel11A.add(slipSignalBlockANameLabel);
            panel11A.add(slipSensorsBlockAComboBox);
            slipSensorsBlockAComboBox.setToolTipText(Bundle.getMessage("SensorsBlockNameHint"));
            theContentPane.add(panel11A);

            JPanel panel11B = new JPanel(new FlowLayout());
            slipSignalBlockBNameLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " B "
                    + Bundle.getMessage("Name")));
            panel11B.add(slipSignalBlockBNameLabel);
            panel11B.add(slipSensorsBlockBComboBox);
            slipSensorsBlockBComboBox.setToolTipText(Bundle.getMessage("SensorsBlockNameHint"));
            theContentPane.add(panel11B);

            JPanel panel11C = new JPanel(new FlowLayout());
            slipSignalBlockCNameLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " C "
                    + Bundle.getMessage("Name")));
            panel11C.add(slipSignalBlockCNameLabel);
            panel11C.add(slipSensorsBlockCComboBox);
            slipSensorsBlockCComboBox.setToolTipText(Bundle.getMessage("SensorsBlockNameHint"));
            theContentPane.add(panel11C);

            JPanel panel11D = new JPanel(new FlowLayout());
            slipSignalBlockDNameLabel = new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " D "
                    + Bundle.getMessage("Name")));
            panel11D.add(slipSignalBlockDNameLabel);
            panel11D.add(slipSensorsBlockDComboBox);
            slipSensorsBlockDComboBox.setToolTipText(Bundle.getMessage("SensorsBlockNameHint"));
            theContentPane.add(panel11D);

            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel2 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("BeanNameSensor"));
            panel2.add(shTitle);
            panel2.add(new JLabel("	  "));
            panel2.add(getSavedSlipSensors = new JButton(Bundle.getMessage("GetSaved")));
            getSavedSlipSensors.addActionListener((ActionEvent e) -> {
                slipSensorsGetSaved(e);
            });
            getSavedSlipSensors.setToolTipText(Bundle.getMessage("GetSavedHint"));
            theContentPane.add(panel2);

            sensorSlipPanel.setLayout(new GridLayout(0, 2));
            theContentPane.add(sensorSlipPanel);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(changeSensorSlipIcon = new JButton(Bundle.getMessage("ChangeSensorIcon")));
            changeSensorSlipIcon.addActionListener((ActionEvent e) -> {
                sensorSlipFrame.setVisible(true);
            });
            changeSensorSlipIcon.setToolTipText(Bundle.getMessage("ChangeSensorIconHint"));

            panel6.add(new JLabel("	 "));
            panel6.add(setSlipSensorsDone = new JButton(Bundle.getMessage("ButtonDone")));
            setSlipSensorsDone.addActionListener((ActionEvent e) -> {
                setSlipSensorsDonePressed(e);
            });
            setSlipSensorsDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));
            panel6.add(setSlipSensorsCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setSlipSensorsCancel.addActionListener((ActionEvent e) -> {
                setSlipSensorsCancelPressed(e);
            });
            setSlipSensorsCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
            setSensorsAtSlipFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSlipSensorsCancelPressed(null);
                }
            });
        }   //if (setSensorsAtSlipFrame == null)

        sensorSlipPanel.removeAll();

        slipSensorsBlockAComboBox.setVisible(!setSensorsAtSlipFromMenuFlag);
        slipSensorsBlockBComboBox.setVisible(!setSensorsAtSlipFromMenuFlag);
        slipSensorsBlockCComboBox.setVisible(!setSensorsAtSlipFromMenuFlag);
        slipSensorsBlockDComboBox.setVisible(!setSensorsAtSlipFromMenuFlag);

        if (setSensorsAtSlipFromMenuFlag) {
            slipSignalBlockANameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " A "
                    + Bundle.getMessage("Name")) + " " + layoutSlip.getBlockName());
            slipSignalBlockBNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " B "
                    + Bundle.getMessage("Name")) + " " + layoutSlip.getBlockBName());
            slipSignalBlockCNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " C "
                    + Bundle.getMessage("Name")) + " " + layoutSlip.getBlockCName());
            slipSignalBlockDNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " D "
                    + Bundle.getMessage("Name")) + " " + layoutSlip.getBlockDName());
            slipSensorsGetSaved(null);
        } else {
            slipSignalBlockANameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " A "
                    + Bundle.getMessage("Name")));
            slipSignalBlockBNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " B "
                    + Bundle.getMessage("Name")));
            slipSignalBlockCNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " C "
                    + Bundle.getMessage("Name")));
            slipSignalBlockDNameLabel.setText(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("BeanNameBlock") + " D "
                    + Bundle.getMessage("Name")));
        }

        if (setSensorsAtSlipOpenFlag) {
            slipSensorsGetSaved(null);
        } else {
            setSensorsAtSlipFrame.setPreferredSize(null);
            setSensorsAtSlipFrame.setVisible(true);
            setSensorsAtSlipOpenFlag = true;
        }
        setSensorsAtSlipFrame.setPreferredSize(null);
        setSensorsAtSlipFrame.pack();
    }

    private void slipSensorsGetSaved(ActionEvent a) {
        if (!getSlipSensorInformation()) {
            return;
        }

        slipSensorBlocks = layoutSlip.getBlockBoundaries();

        slipSensorA.setTextField(layoutSlip.getSensorAName());
        slipSensorB.setTextField(layoutSlip.getSensorBName());
        slipSensorC.setTextField(layoutSlip.getSensorCName());
        slipSensorD.setTextField(layoutSlip.getSensorDName());

        sensorSlipPanel.remove(slipSensorA.getDetailsPanel());
        sensorSlipPanel.remove(slipSensorB.getDetailsPanel());
        sensorSlipPanel.remove(slipSensorC.getDetailsPanel());
        sensorSlipPanel.remove(slipSensorD.getDetailsPanel());

        slipSensorA.setBoundaryLabel(slipSensorBlocks[0]);
        slipSensorB.setBoundaryLabel(slipSensorBlocks[1]);
        slipSensorC.setBoundaryLabel(slipSensorBlocks[2]);
        slipSensorD.setBoundaryLabel(slipSensorBlocks[3]);

        boolean boundaryFlag = false;
        if (slipSensorBlocks[0] != null) {
            sensorSlipPanel.add(slipSensorA.getDetailsPanel());
            boundaryFlag = true;
        }
        if (slipSensorBlocks[1] != null) {
            sensorSlipPanel.add(slipSensorB.getDetailsPanel());
            boundaryFlag = true;
        }
        if (slipSensorBlocks[2] != null) {
            sensorSlipPanel.add(slipSensorC.getDetailsPanel());
            boundaryFlag = true;
        }
        if (slipSensorBlocks[3] != null) {
            sensorSlipPanel.add(slipSensorD.getDetailsPanel());
            boundaryFlag = true;
        }
        if (!boundaryFlag) {
            JOptionPane.showMessageDialog(setSensorsAtSlipFrame, Bundle.getMessage("NoBoundarySlipSensor"));
        }
        setSensorsAtSlipFrame.setPreferredSize(null);
        setSensorsAtSlipFrame.pack();
    }

    private boolean getSlipSensorInformation() {
        if (!setSensorsAtSlipFromMenuFlag) {
            layoutSlip = null;
            List<LayoutSlip> layoutSlips = layoutEditor.getLayoutSlips();
            if (layoutSlips.size() <= 0) {
                JOptionPane.showMessageDialog(setSensorsAtSlipFrame,
                        Bundle.getMessage("SignalsError15"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            } else if (layoutSlips.size() == 1) {
                layoutSlip = layoutSlips.get(0);
            } else {
                LayoutBlock slipSensorBlockA = null;
                slipSensorBlockA = getBlockFromEntry(slipSensorsBlockAComboBox);
                if (slipSensorBlockA == null) {
                    return false;
                }

                int foundCount = 0;
                for (LayoutSlip x : layoutEditor.getLayoutSlips()) {
                    LayoutBlock xA = null;
                    LayoutBlock xB = null;
                    LayoutBlock xC = null;
                    LayoutBlock xD = null;

                    LayoutBlock xAC = x.getLayoutBlock();
                    if (x.getConnectA() != null) {
                        xA = ((TrackSegment) x.getConnectA()).getLayoutBlock();
                    }
                    if (x.getConnectB() != null) {
                        xB = ((TrackSegment) x.getConnectB()).getLayoutBlock();
                    }
                    if (x.getConnectC() != null) {
                        xC = ((TrackSegment) x.getConnectC()).getLayoutBlock();
                    }
                    if (x.getConnectD() != null) {
                        xD = ((TrackSegment) x.getConnectD()).getLayoutBlock();
                    }
                    if (((xA != null) && (xC != null) && ((xA == slipSensorBlockA)
                            || (xC == slipSensorBlockA)))
                            || ((xB != null) && (xD != null) && (((xB == slipSensorBlockA))
                            || ((xD == slipSensorBlockA))))) {
                        layoutSlip = x;
                        foundCount++;
                    } else if ((xAC != null) && (xAC == slipSensorBlockA)) {
                        layoutSlip = x;
                        foundCount++;
                    }
                }
                if (foundCount == 0) {
                    //try one block test
                    for (LayoutSlip x : layoutEditor.getLayoutSlips()) {
                        if (slipSensorBlockA == x.getLayoutBlock()) {
                            layoutSlip = x;
                            foundCount++;
                        }
                    }
                }
                if (foundCount > 1) {
                    JOptionPane.showMessageDialog(setSensorsAtSlipFrame,
                            Bundle.getMessage("SignalsError16",
                                    new Object[]{" " + foundCount + " "}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (layoutSlip == null) {
                    JOptionPane.showMessageDialog(setSensorsAtSlipFrame,
                            Bundle.getMessage("SignalsError17"),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    private void setSlipSensorsCancelPressed(ActionEvent a) {
        setSensorsAtSlipOpenFlag = false;
        setSensorsAtSlipFrame.setVisible(false);
    }

    private void setSlipSensorsDonePressed(ActionEvent a) {
        log.trace("setSlipSensorsDonePressed");
        if (!getSlipSensorInformation()) {
            return;
        }

        Sensor sensorA = getSensorFromEntry(slipSensorA.getText(), false, setSensorsAtSlipFrame);
        Sensor sensorB = getSensorFromEntry(slipSensorB.getText(), false, setSensorsAtSlipFrame);
        Sensor sensorC = getSensorFromEntry(slipSensorC.getText(), false, setSensorsAtSlipFrame);
        Sensor sensorD = getSensorFromEntry(slipSensorD.getText(), false, setSensorsAtSlipFrame);

        Sensor currSensorA = layoutSlip.getSensorA();
        Sensor currSensorB = layoutSlip.getSensorB();
        Sensor currSensorC = layoutSlip.getSensorC();
        Sensor currSensorD = layoutSlip.getSensorD();

        if (log.isTraceEnabled()) {
            log.trace("current sensors: A = {}, B = {}, C = {}, D = {}", //NOI18N
                    (currSensorA == null) ? "- none- " : currSensorA.getDisplayName(), //NOI18N
                    (currSensorB == null) ? "- none- " : currSensorB.getDisplayName(), //NOI18N
                    (currSensorC == null) ? "- none- " : currSensorC.getDisplayName(), //NOI18N
                    (currSensorD == null) ? "- none- " : currSensorD.getDisplayName());  //NOI18N
            log.trace("new sensors: A = {}, B = {}, C = {}, D = {}", //NOI18N
                    (sensorA == null) ? "- none- " : sensorA.getDisplayName(), //NOI18N
                    (sensorB == null) ? "- none- " : sensorB.getDisplayName(), //NOI18N
                    (sensorC == null) ? "- none- " : sensorC.getDisplayName(), //NOI18N
                    (sensorD == null) ? "- none- " : sensorD.getDisplayName());  //NOI18N
        }

        //place/remove sensors as requested
        if (sensorA == null) {
            if (currSensorA != null && removeSensorFromPanel(currSensorA)) {
                layoutSlip.setSensorA(null);
            }
        } else if (slipSensorA != null && layoutSlip.getConnectA() != null) {
            setTurnoutSensor(layoutSlip, sensorA, currSensorA, slipSensorA, layoutSlip.getConnectA(), layoutSlip.getCoordsA(), "A");
        }

        if (sensorB == null) {
            if (currSensorB != null && removeSensorFromPanel(currSensorB)) {
                layoutSlip.setSensorB(null);
            }
        } else if (slipSensorB != null && layoutSlip.getConnectB() != null) {
            setTurnoutSensor(layoutSlip, sensorB, currSensorB, slipSensorB, layoutSlip.getConnectB(), layoutSlip.getCoordsB(), "B");
        }

        if (sensorC == null) {
            if (currSensorC != null && removeSensorFromPanel(currSensorC)) {
                layoutSlip.setSensorC(null);
            }
        } else if (slipSensorC != null && layoutSlip.getConnectC() != null) {
            setTurnoutSensor(layoutSlip, sensorC, currSensorC, slipSensorC, layoutSlip.getConnectC(), layoutSlip.getCoordsC(), "C");
        }

        if (sensorD == null) {
            if (currSensorD != null && removeSensorFromPanel(currSensorD)) {
                layoutSlip.setSensorD(null);
            }
        } else if (slipSensorD != null && layoutSlip.getConnectD() != null) {
            setTurnoutSensor(layoutSlip, sensorD, currSensorD, slipSensorD, layoutSlip.getConnectD(), layoutSlip.getCoordsD(), "D");
        }

        //setup logic if requested
        //finish up
        setSensorsAtSlipOpenFlag = false;
        setSensorsAtSlipFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();

        }
    }

    static class BeanDetails<B extends NamedBean> {

        private String bundleName;
        private String beanString;
        private JLabel textLabel;

        private final String boundaryLabelText = Bundle.getMessage("BoundaryOf");
        private final JLabel boundaryLabel = new JLabel(boundaryLabelText);

        private Manager<B> manager;

        private final JPanel detailsPanel = new JPanel(new FlowLayout());
        private final JRadioButton addBeanCheck = new JRadioButton(Bundle.getMessage("DoNotPlace"));
        private final JRadioButton left = new JRadioButton(Bundle.getMessage("LeftHandSide"));
        private final JRadioButton right = new JRadioButton(Bundle.getMessage("RightHandSide"));
        private final ButtonGroup buttonGroup = new ButtonGroup();
        private NamedBeanComboBox<B> beanCombo;

        private final JLabel boundaryBlocks = new JLabel();

        private final Border blackline = BorderFactory.createLineBorder(Color.black);

        BeanDetails(@Nonnull String beanType, @Nonnull Manager<B> manager) {
            beanCombo = new NamedBeanComboBox<>(manager);
            beanCombo.setAllowNull(true);
            JComboBoxUtil.setupComboBoxMaxRows(beanCombo);
            //I18N translate from type (Sensor) to BeanNameSensor
            //to use NamedBeanBundle property
            if ("Sensor".equals(beanType)) {
                bundleName = "BeanNameSensor";
            } else if ("SignalMast".equals(beanType)) {
                bundleName = "BeanNameSignalMast";
            } else {
                log.error("Unexpected value for BeanDetails: '{}'", beanType);
                bundleName = beanType;
            }
            beanString = Bundle.getMessage(bundleName);
            textLabel = new JLabel(beanString);
            this.manager = manager;
            //this.beanType = beanType;

            buttonGroup.add(addBeanCheck);
            buttonGroup.add(left);
            buttonGroup.add(right);
            addBeanCheck.setSelected(true);

            boundaryBlocks.setAlignmentX(Component.CENTER_ALIGNMENT);
            boundaryBlocks.setOpaque(false);
            detailsPanel.setLayout(new BorderLayout());
            detailsPanel.setBorder(BorderFactory.createTitledBorder(blackline, Bundle.getMessage("BlockBoundary")));
            boundaryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel boundaryDetails = new JPanel(new FlowLayout());
            boundaryDetails.setOpaque(false);
            boundaryDetails.setLayout(new BoxLayout(boundaryDetails, BoxLayout.Y_AXIS));
            boundaryDetails.add(boundaryLabel);
            boundaryDetails.add(boundaryBlocks);

            detailsPanel.add(boundaryDetails, BorderLayout.PAGE_START);
            detailsPanel.add(addIconPanel(), BorderLayout.CENTER);
            detailsPanel.add(positionLeftRight(), BorderLayout.PAGE_END);
        }

        protected void setTextField(String value) {
            beanCombo.setSelectedItem(manager.getNamedBean(value));
        }

        protected String getText() {
            return beanCombo.getSelectedItemDisplayName();
        }

        protected B getBean() {
            return beanCombo.getSelectedItem();
        }

        protected JPanel getDetailsPanel() {
            return detailsPanel;
        }

        protected boolean addToPanel() {
            return !addBeanCheck.isSelected();
        }

        protected boolean isRightSelected() {
            return right.isSelected();
        }

        protected void setBoundaryTitle(String text) {
            detailsPanel.setBorder(BorderFactory.createTitledBorder(blackline, text));
        }

        protected void setBoundaryLabelText(String text) {
            boundaryLabel.setText(text);
        }

        protected void setBoundaryLabel(String label) {
            boundaryBlocks.setText(label);
        }

        protected NamedBeanComboBox<B> getCombo() {
            return beanCombo;
        }

        protected JPanel positionLeftRight() {
            JPanel placementPanel = new JPanel();
            placementPanel.setBorder(BorderFactory.createTitledBorder(
                    blackline,
                    Bundle.getMessage("PlaceItem", new Object[]{beanString})));

            placementPanel.setLayout(new BoxLayout(placementPanel, BoxLayout.Y_AXIS));
            placementPanel.setOpaque(false);
            placementPanel.add(addBeanCheck);
            placementPanel.add(left);
            placementPanel.add(right);
            addBeanCheck.setOpaque(false);
            left.setOpaque(false);
            right.setOpaque(false);

            addBeanCheck.setToolTipText(Bundle.getMessage("PlaceItemToolTip",
                    new Object[]{beanString}));

            right.setToolTipText(Bundle.getMessage("PlaceRightToolTip",
                    new Object[]{beanString}));

            left.setToolTipText(Bundle.getMessage("PlaceLeftToolTip",
                    new Object[]{beanString}));
            return placementPanel;
        }

        protected JPanel addIconPanel() {
            JPanel addBeanPanel = new JPanel(new FlowLayout());
            addBeanPanel.setOpaque(false);
            addBeanPanel.add(textLabel);
            textLabel.setOpaque(false);
            addBeanPanel.add(beanCombo);
            return addBeanPanel;
        }
    }

    /*==================*\
    |* setSignalsAtSlip *|
    \*==================*/
//operational variables for Set Signals at slip tool
    private JmriJFrame setSignalsAtSlipFrame = null;
    private boolean setSignalsAtSlipOpenFlag = false;
    private boolean setSignalsAtSlipFromMenuFlag = false;

    private final JComboBox<String> slipNameComboBox = new JComboBox<String>();

    private final NamedBeanComboBox<SignalHead> a1SlipSignalHeadComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(SignalHeadManager.class),
                    null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> a2SlipSignalHeadComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(SignalHeadManager.class),
                    null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> b1SlipSignalHeadComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(SignalHeadManager.class),
                    null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> b2SlipSignalHeadComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(SignalHeadManager.class),
                    null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> c1SlipSignalHeadComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(SignalHeadManager.class),
                    null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> c2SlipSignalHeadComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(SignalHeadManager.class),
                    null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> d1SlipSignalHeadComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(SignalHeadManager.class),
                    null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> d2SlipSignalHeadComboBox
            = new NamedBeanComboBox<>(
                    InstanceManager.getDefault(SignalHeadManager.class),
                    null, DisplayOptions.DISPLAYNAME);

    private final JCheckBox setA1SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupA1SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setA2SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupA2SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setB1SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupB1SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setB2SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupB2SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setC1SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupC1SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setC2SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupC2SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setD1SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupD1SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private final JCheckBox setD2SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private final JCheckBox setupD2SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));

    private JButton setSlipSignalsDone = null;
    private JButton setSlipSignalsCancel = null;
    private LayoutSlip layoutSlip = null;

    private SignalHead a1SlipHead = null;
    private SignalHead a2SlipHead = null;
    private SignalHead b1SlipHead = null;
    private SignalHead b2SlipHead = null;
    private SignalHead c1SlipHead = null;
    private SignalHead c2SlipHead = null;
    private SignalHead d1SlipHead = null;
    private SignalHead d2SlipHead = null;

    private JPanel dblSlipC2SigPanel;
    private JPanel dblSlipB2SigPanel;

    public void setSignalsAtSlipFromMenu(@Nonnull LayoutSlip ls,
            @Nonnull MultiIconEditor theEditor, @Nonnull JFrame theFrame) {
        layoutSlip = ls;
        a1SlipSignalHeadComboBox.setSelectedItem(null);
        a2SlipSignalHeadComboBox.setSelectedItem(null);
        b1SlipSignalHeadComboBox.setSelectedItem(null);
        b2SlipSignalHeadComboBox.setSelectedItem(null);
        c1SlipSignalHeadComboBox.setSelectedItem(null);
        c2SlipSignalHeadComboBox.setSelectedItem(null);
        d1SlipSignalHeadComboBox.setSelectedItem(null);
        d2SlipSignalHeadComboBox.setSelectedItem(null);

        setSignalsAtSlipFromMenuFlag = true;
        setSignalsAtSlip(theEditor, theFrame);
        setSignalsAtSlipFromMenuFlag = false;
    }

    public void setSignalsAtSlip(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        signalIconEditor = theEditor;
        signalFrame = theFrame;

        //Initialize if needed
        if (setSignalsAtSlipFrame == null) {
            setSignalsAtSlipOpenFlag = false;
            setSignalsAtSlipFrame = new JmriJFrame(Bundle.getMessage("SignalsAtSlip"), false, true);
            oneFrameToRuleThemAll(setSignalsAtSlipFrame);
            setSignalsAtSlipFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSignalsAtSlipFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtSlip", true);
            setSignalsAtSlipFrame.setLocation(70, 30);
            Container theContentPane = setSignalsAtSlipFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel1 = new JPanel(new FlowLayout());
            JLabel turnout1NameLabel = new JLabel(Bundle.getMessage("Slip") + " "
                    + Bundle.getMessage("Name"));
            panel1.add(turnout1NameLabel);
            panel1.add(slipNameComboBox);
            for (LayoutSlip slip : layoutEditor.getLayoutSlips()) {
                slipNameComboBox.addItem(slip.getDisplayName());
            }

            slipNameComboBox.insertItemAt("", 0);

            if (layoutSlip != null) {
                slipNameComboBox.setSelectedItem(layoutSlip.getDisplayName());
                getSlipTurnoutSignalsGetSaved(null);
            } else {
                slipNameComboBox.setSelectedIndex(0);
            }
            slipNameComboBox.addActionListener((ActionEvent e) -> {
                for (LayoutSlip slip : layoutEditor.getLayoutSlips()) {
                    if (slip.getDisplayName().equals(slipNameComboBox.getSelectedItem())) {
                        //slip1NameField.setText(slip.getDisplayName());
                        getSlipTurnoutSignalsGetSaved(e);
                        boolean enable = (slip.getSlipType() == LayoutSlip.DOUBLE_SLIP);
                        dblSlipC2SigPanel.setVisible(enable);
                        dblSlipB2SigPanel.setVisible(enable);
                        setSignalsAtSlipFrame.pack();
                        return;
                    }
                }
            });
            theContentPane.add(panel1);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel2a = new JPanel(new FlowLayout());
            panel2a.add(new JLabel("   "));
            panel2a.add(setPlaceAllHeads);
            setPlaceAllHeads.setToolTipText(Bundle.getMessage("PlaceAllHeadsHint"));
            setPlaceAllHeads.addActionListener((ActionEvent e) -> {
                boolean isSelected = setPlaceAllHeads.isSelected();
                //(de)select all checkboxes
                setA1SlipHead.setSelected(isSelected);
                setA2SlipHead.setSelected(isSelected);
                setB1SlipHead.setSelected(isSelected);
                setB2SlipHead.setSelected(isSelected);
                setC1SlipHead.setSelected(isSelected);
                setC2SlipHead.setSelected(isSelected);
                setD1SlipHead.setSelected(isSelected);
                setD2SlipHead.setSelected(isSelected);
            });
            panel2a.add(new JLabel("  "));
            panel2a.add(setupAllLogic);
            setupAllLogic.setToolTipText(Bundle.getMessage("SetAllLogicHint"));
            setupAllLogic.addActionListener((ActionEvent e) -> {
                boolean isSelected = setupAllLogic.isSelected();
                //(de)select all checkboxes
                setupA1SlipLogic.setSelected(isSelected);
                setupA2SlipLogic.setSelected(isSelected);
                setupB1SlipLogic.setSelected(isSelected);
                setupB2SlipLogic.setSelected(isSelected);
                setupC1SlipLogic.setSelected(isSelected);
                setupC2SlipLogic.setSelected(isSelected);
                setupD1SlipLogic.setSelected(isSelected);
                setupD2SlipLogic.setSelected(isSelected);
            });
            theContentPane.add(panel2a);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            //Signal heads located at turnout 1
            JPanel panel21x = new JPanel(new FlowLayout());
            panel21x.add(new JLabel(Bundle.getMessage("SignalLocated")
                    + " " + Bundle.getMessage("BeanNameTurnout") + " 1 - "
                    + Bundle.getMessage("ContinuingTrack")));
            theContentPane.add(panel21x);

            JPanel panel21 = new JPanel(new FlowLayout());
            panel21.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 2 - "
                    + Bundle.getMessage("ContinuingTrack"))));
            panel21.add(a1SlipSignalHeadComboBox);
            theContentPane.add(panel21);
            a1SlipSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel22 = new JPanel(new FlowLayout());
            panel22.add(new JLabel(Bundle.getMessage("OrBoth") + " 2 " + Bundle.getMessage("Tracks)") + "	  "));
            panel22.add(setA1SlipHead);
            setA1SlipHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel22.add(new JLabel("  "));
            panel22.add(setupA1SlipLogic);
            setupA1SlipLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel22);

            JPanel panel23 = new JPanel(new FlowLayout());
            panel23.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 2 - "
                    + Bundle.getMessage("DivergingTrack"))));
            panel23.add(a2SlipSignalHeadComboBox);
            theContentPane.add(panel23);
            a2SlipSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel24 = new JPanel(new FlowLayout());
            panel24.add(new JLabel("				"));
            panel24.add(setA2SlipHead);
            setA2SlipHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel24.add(new JLabel("  "));
            panel24.add(setupA2SlipLogic);
            setupA2SlipLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel24);

            JPanel panel31x = new JPanel(new FlowLayout());
            panel31x.add(new JLabel(Bundle.getMessage("SignalLocated")
                    + " " + Bundle.getMessage("BeanNameTurnout") + " 1 - "
                    + Bundle.getMessage("DivergingTrack")));
            theContentPane.add(panel31x);

            JPanel panel31 = new JPanel(new FlowLayout());
            panel31.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 2 - "
                    + Bundle.getMessage("ContinuingTrack"))));
            panel31.add(b1SlipSignalHeadComboBox);
            theContentPane.add(panel31);
            b1SlipSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel32 = new JPanel(new FlowLayout());
            panel32.add(new JLabel(Bundle.getMessage("OrBoth") + " 2 " + Bundle.getMessage("Tracks)") + "	  "));
            panel32.add(setB1SlipHead);
            setB1SlipHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel32.add(new JLabel("  "));
            panel32.add(setupB1SlipLogic);
            setupB1SlipLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel32);

            dblSlipB2SigPanel = new JPanel(new FlowLayout());
            dblSlipB2SigPanel.setLayout(new BoxLayout(dblSlipB2SigPanel, BoxLayout.Y_AXIS));

            JPanel panel33 = new JPanel(new FlowLayout());
            panel33.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 2 - "
                    + Bundle.getMessage("DivergingTrack"))));
            panel33.add(b2SlipSignalHeadComboBox);
            dblSlipB2SigPanel.add(panel33);
            b2SlipSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel34 = new JPanel(new FlowLayout());
            panel34.add(new JLabel("				"));
            panel34.add(setB2SlipHead);
            setB2SlipHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel34.add(new JLabel("  "));
            panel34.add(setupB2SlipLogic);
            setupB2SlipLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            dblSlipB2SigPanel.add(panel34);

            theContentPane.add(dblSlipB2SigPanel);
            dblSlipB2SigPanel.setVisible(false);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            //Signal heads located at turnout 2

            JPanel panel41x = new JPanel(new FlowLayout());
            panel41x.add(new JLabel(Bundle.getMessage("SignalLocated")
                    + " " + Bundle.getMessage("BeanNameTurnout") + " 2 - "
                    + Bundle.getMessage("ContinuingTrack")));
            theContentPane.add(panel41x);

            JPanel panel41 = new JPanel(new FlowLayout());
            panel41.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 2 - "
                    + Bundle.getMessage("ContinuingTrack"))));
            panel41.add(c1SlipSignalHeadComboBox);
            theContentPane.add(panel41);
            c1SlipSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel42 = new JPanel(new FlowLayout());
            panel42.add(new JLabel(Bundle.getMessage("OrBoth") + " 1 " + Bundle.getMessage("Tracks)") + "	  "));
            panel42.add(setC1SlipHead);
            setC1SlipHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel42.add(new JLabel("  "));
            panel42.add(setupC1SlipLogic);
            setupC1SlipLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel42);
            dblSlipC2SigPanel = new JPanel(new FlowLayout());
            dblSlipC2SigPanel.setLayout(new BoxLayout(dblSlipC2SigPanel, BoxLayout.Y_AXIS));

            JPanel panel43 = new JPanel(new FlowLayout());
            panel43.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 1 - "
                    + Bundle.getMessage("DivergingTrack"))));
            panel43.add(c2SlipSignalHeadComboBox);
            dblSlipC2SigPanel.add(panel43);
            c2SlipSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel44 = new JPanel(new FlowLayout());
            panel44.add(new JLabel("				"));
            panel44.add(setC2SlipHead);
            setC2SlipHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel44.add(new JLabel("  "));
            panel44.add(setupC2SlipLogic);
            setupC2SlipLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            dblSlipC2SigPanel.add(panel44);
            theContentPane.add(dblSlipC2SigPanel);

            JPanel panel51x = new JPanel(new FlowLayout());
            panel51x.add(new JLabel(Bundle.getMessage("SignalLocated")
                    + " " + Bundle.getMessage("BeanNameTurnout") + " 2 - "
                    + Bundle.getMessage("DivergingTrack")));
            theContentPane.add(panel51x);

            JPanel panel51 = new JPanel(new FlowLayout());
            panel51.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 1 - "
                    + Bundle.getMessage("ContinuingTrack"))));
            panel51.add(d1SlipSignalHeadComboBox);
            theContentPane.add(panel51);
            d1SlipSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel52 = new JPanel(new FlowLayout());
            panel52.add(new JLabel(Bundle.getMessage("OrBoth") + " 1 " + Bundle.getMessage("Tracks)") + "	  "));
            panel52.add(setD1SlipHead);
            setD1SlipHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel52.add(new JLabel("  "));
            panel52.add(setupD1SlipLogic);
            setupD1SlipLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel52);

            JPanel panel53 = new JPanel(new FlowLayout());
            panel53.add(new JLabel(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ProtectsTurnout") + " 1 - "
                    + Bundle.getMessage("DivergingTrack"))));
            panel53.add(d2SlipSignalHeadComboBox);
            theContentPane.add(panel53);
            d2SlipSignalHeadComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel54 = new JPanel(new FlowLayout());
            panel54.add(new JLabel("				"));
            panel54.add(setD2SlipHead);
            setD2SlipHead.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel54.add(new JLabel("  "));
            panel54.add(setupD2SlipLogic);
            setupD2SlipLogic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel54);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(changeTToTSignalIcon = new JButton(Bundle.getMessage("ChangeSignalIcon")));
            changeTToTSignalIcon.addActionListener((ActionEvent e) -> {
                signalFrame.setVisible(true);
            });
            changeTToTSignalIcon.setToolTipText(Bundle.getMessage("ChangeSignalIconHint"));
            panel6.add(new JLabel("	 "));
            panel6.add(setSlipSignalsDone = new JButton(Bundle.getMessage("ButtonDone")));
            setSlipSignalsDone.addActionListener((ActionEvent e) -> {
                setSlipSignalsDonePressed(e);
            });
            setSlipSignalsDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));
            panel6.add(setSlipSignalsCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setSlipSignalsCancel.addActionListener((ActionEvent e) -> {
                setSlipSignalsCancelPressed(e);
            });
            setSlipSignalsCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
            setSignalsAtSlipFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSlipSignalsCancelPressed(null);
                }
            });
        }
        setPlaceAllHeads.setSelected(false);
        setupAllLogic.setSelected(false);

        boolean enable = (layoutSlip != null
                && layoutSlip.getSlipType() == LayoutSlip.DOUBLE_SLIP);
        dblSlipC2SigPanel.setVisible(enable);
        dblSlipB2SigPanel.setVisible(enable);

        if (setSignalsAtSlipFromMenuFlag) {
            getSlipTurnoutSignalsGetSaved(null);
        }

        if (!setSignalsAtSlipOpenFlag) {
            setSignalsAtSlipFrame.setPreferredSize(null);
            setSignalsAtSlipFrame.pack();
            setSignalsAtSlipOpenFlag = true;
        }
        setSignalsAtSlipFrame.setVisible(true);
    }

    private void getSlipTurnoutSignalsGetSaved(ActionEvent a) {
        if (!getSlipTurnoutInformation()) {
            return;
        }
        a1SlipSignalHeadComboBox.setSelectedItem(layoutSlip.getSignalA1());
        a2SlipSignalHeadComboBox.setSelectedItem(layoutSlip.getSignalA2());
        b1SlipSignalHeadComboBox.setSelectedItem(layoutSlip.getSignalB1());
        b2SlipSignalHeadComboBox.setSelectedItem(layoutSlip.getSignalB2());
        c1SlipSignalHeadComboBox.setSelectedItem(layoutSlip.getSignalC1());
        c2SlipSignalHeadComboBox.setSelectedItem(layoutSlip.getSignalC2());
        d1SlipSignalHeadComboBox.setSelectedItem(layoutSlip.getSignalD1());
        d2SlipSignalHeadComboBox.setSelectedItem(layoutSlip.getSignalD2());
    }

    private void setSlipSignalsCancelPressed(ActionEvent a) {
        setSignalsAtSlipOpenFlag = false;
        setSignalsAtSlipFrame.setVisible(false);
    }

    private boolean getSlipTurnoutInformation() {
        turnout1 = null;
        turnout2 = null;
        layoutSlip = null;
        for (LayoutSlip ls : layoutEditor.getLayoutSlips()) {
            if (ls.getDisplayName().equals(slipNameComboBox.getSelectedItem())) {
                layoutSlip = ls;
                break;
            }
        }
        if (layoutSlip == null) {
            return false;
        }
        String str = layoutSlip.getDisplayName();

        turnout1 = layoutSlip.getTurnout();
        if (turnout1 == null) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    Bundle.getMessage("SignalsError2",
                            new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        turnout2 = layoutSlip.getTurnoutB();
        if (turnout2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    Bundle.getMessage("SignalsError2",
                            new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void setSlipSignalsDonePressed(ActionEvent a) {
        if (!getSlipTurnoutInformation()) {
            return;
        }
        if (!getSlipSignalHeadInformation()) {
            return;
        }

        //place signal icons if requested, and assign signal heads to this turnout
        String signalHeadName = a1SlipSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setA1SlipHead.isSelected()) {
            if (isHeadOnPanel(a1SlipHead)
                    && (a1SlipHead != getHeadFromName(layoutSlip.getSignalB1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutSlip.getSignalA1Name());
                if (layoutSlip.getContinuingSense() == Turnout.CLOSED) {
                    placeA1Slip(signalHeadName);
                } else {
                    placeB1Slip(signalHeadName);
                }
                removeAssignment(a1SlipHead);
                layoutSlip.setSignalA1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(a1SlipHead, layoutSlip);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(a1SlipHead)
                        && isHeadAssignedAnywhere(a1SlipHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutSlip.getSignalB1Name());
                    removeAssignment(a1SlipHead);
                    layoutSlip.setSignalA1Name(signalHeadName);
                }
                //} else if (assigned != B1) {
                //need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }

        signalHeadName = a2SlipSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if ((a2SlipHead != null) && setA2SlipHead.isSelected()) {
            if (isHeadOnPanel(a2SlipHead)
                    && (a2SlipHead != getHeadFromName(layoutSlip.getSignalB2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutSlip.getSignalB2Name());
                if (layoutSlip.getContinuingSense() == Turnout.CLOSED) {
                    placeA2Slip(signalHeadName);
                } else {
                    placeB2Slip(signalHeadName);
                }
                removeAssignment(a2SlipHead);
                layoutSlip.setSignalA2Name(signalHeadName);
                needRedraw = true;
            }
        } else if (a2SlipHead != null) {
            int assigned = isHeadAssignedHere(a2SlipHead, layoutSlip);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(a2SlipHead)
                        && isHeadAssignedAnywhere(a2SlipHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutSlip.getSignalA2Name());
                    removeAssignment(a2SlipHead);
                    layoutSlip.setSignalA2Name(signalHeadName);
                }
                //} else if (assigned != B2) {
                //need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        } else { //a2SlipHead known to be null here
            removeSignalHeadFromPanel(layoutSlip.getSignalA2Name());
            layoutSlip.setSignalB2Name("");
        }

        signalHeadName = b1SlipSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setB1SlipHead.isSelected()) {
            if (isHeadOnPanel(b1SlipHead)
                    && (b1SlipHead != getHeadFromName(layoutSlip.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutSlip.getSignalB1Name());
                if (layoutSlip.getContinuingSense() == Turnout.CLOSED) {
                    placeB1Slip(signalHeadName);
                } else {
                    placeA1Slip(signalHeadName);
                }
                removeAssignment(b1SlipHead);
                layoutSlip.setSignalB1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(b1SlipHead, layoutSlip);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(b1SlipHead)
                        && isHeadAssignedAnywhere(b1SlipHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutSlip.getSignalB1Name());
                    removeAssignment(b1SlipHead);
                    layoutSlip.setSignalB1Name(signalHeadName);
                }
                //} else if (assigned != C1) {
                //need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }

        if (layoutSlip.getTurnoutType() == LayoutSlip.DOUBLE_SLIP) {
            signalHeadName = b2SlipSignalHeadComboBox.getSelectedItemDisplayName();
            if (signalHeadName == null) {
                signalHeadName = "";
            }
            if ((b2SlipHead != null) && setB2SlipHead.isSelected()) {
                if (isHeadOnPanel(b2SlipHead)
                        && (b2SlipHead != getHeadFromName(layoutSlip.getSignalC2Name()))) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            Bundle.getMessage("SignalsError6",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutSlip.getSignalB2Name());
                    if (layoutSlip.getContinuingSense() == Turnout.CLOSED) {
                        placeB2Slip(signalHeadName);
                    } else {
                        placeA2Slip(signalHeadName);
                    }
                    removeAssignment(b2SlipHead);
                    layoutSlip.setSignalB2Name(signalHeadName);
                    needRedraw = true;
                }
            } else if (b2SlipHead != null) {
                int assigned = isHeadAssignedHere(b2SlipHead, layoutSlip);
                if (assigned == LayoutTurnout.NONE) {
                    if (isHeadOnPanel(b2SlipHead)
                            && isHeadAssignedAnywhere(b2SlipHead)) {
                        JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                                Bundle.getMessage("SignalsError8",
                                        new Object[]{signalHeadName}),
                                Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        removeSignalHeadFromPanel(layoutSlip.getSignalB2Name());
                        removeAssignment(b2SlipHead);
                        layoutSlip.setSignalB2Name(signalHeadName);
                    }
                    //} else if (assigned != C2) {
                    //need to figure out what to do in this case - assigned to a different position on the same turnout.
                }
            } else { //b2SlipHead known to be null here
                removeSignalHeadFromPanel(layoutSlip.getSignalB2Name());
                layoutSlip.setSignalB2Name("");
            }
        } else {
            if (b2SlipHead != null) {
                BlockBossLogic.getStoppedObject(layoutSlip.getSignalB2Name());
                removeSignalHeadFromPanel(layoutSlip.getSignalB2Name());
                layoutSlip.setSignalB2Name("");
                b2SlipHead = null;
            }
        }

        //signal heads on turnout 2
        signalHeadName = c1SlipSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setC1SlipHead.isSelected()) {
            if (isHeadOnPanel(c1SlipHead)
                    && (c1SlipHead != getHeadFromName(layoutSlip.getSignalB1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutSlip.getSignalC1Name());
                if (layoutSlip.getContinuingSense() == Turnout.CLOSED) {
                    placeC1Slip(signalHeadName);
                } else {
                    placeD1Slip(signalHeadName);
                }
                removeAssignment(c1SlipHead);
                layoutSlip.setSignalC1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(c1SlipHead, layoutSlip);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(c1SlipHead)
                        && isHeadAssignedAnywhere(c1SlipHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutSlip.getSignalC1Name());
                    removeAssignment(c1SlipHead);
                    layoutSlip.setSignalC1Name(signalHeadName);
                }
                //} else if (assigned != B1) {
                //need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }

        if (layoutSlip.getTurnoutType() == LayoutSlip.DOUBLE_SLIP) {
            signalHeadName = c2SlipSignalHeadComboBox.getSelectedItemDisplayName();
            if (signalHeadName == null) {
                signalHeadName = "";
            }
            if ((c2SlipHead != null) && setC2SlipHead.isSelected()) {
                if (isHeadOnPanel(c2SlipHead)
                        && (c2SlipHead != getHeadFromName(layoutSlip.getSignalB2Name()))) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            Bundle.getMessage("SignalsError6",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutSlip.getSignalC2Name());
                    if (layoutSlip.getContinuingSense() == Turnout.CLOSED) {
                        placeC2Slip(signalHeadName);
                    } else {
                        placeD2Slip(signalHeadName);
                    }
                    removeAssignment(c2SlipHead);
                    layoutSlip.setSignalC2Name(signalHeadName);
                    needRedraw = true;
                }
            } else if (c2SlipHead != null) {
                int assigned = isHeadAssignedHere(c2SlipHead, layoutSlip);
                if (assigned == LayoutTurnout.NONE) {
                    if (isHeadOnPanel(c2SlipHead)
                            && isHeadAssignedAnywhere(c2SlipHead)) {
                        JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                                Bundle.getMessage("SignalsError8",
                                        new Object[]{signalHeadName}),
                                Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        removeSignalHeadFromPanel(layoutSlip.getSignalC2Name());
                        removeAssignment(c2SlipHead);
                        layoutSlip.setSignalC2Name(signalHeadName);
                    }
                    //} else if (assigned != B2) {
                    //need to figure out what to do in this case - assigned to a different position on the same turnout.
                }
            } else { //c2SlipHead known to be null here
                removeSignalHeadFromPanel(layoutSlip.getSignalC2Name());
                layoutSlip.setSignalC2Name("");
            }
        } else {
            if (c2SlipHead != null) {
                BlockBossLogic.getStoppedObject(layoutSlip.getSignalC2Name());
                removeSignalHeadFromPanel(layoutSlip.getSignalC2Name());
                layoutSlip.setSignalC2Name("");
                c2SlipHead = null;
            }
        }

        signalHeadName = d1SlipSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if (setD1SlipHead.isSelected()) {
            if (isHeadOnPanel(d1SlipHead)
                    && (d1SlipHead != getHeadFromName(layoutSlip.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutSlip.getSignalD1Name());
                if (layoutSlip.getContinuingSense() == Turnout.CLOSED) {
                    placeD1Slip(signalHeadName);
                } else {
                    placeC1Slip(signalHeadName);
                }
                removeAssignment(d1SlipHead);
                layoutSlip.setSignalD1Name(signalHeadName);
                needRedraw = true;
            }
        } else {
            int assigned = isHeadAssignedHere(d1SlipHead, layoutSlip);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(d1SlipHead)
                        && isHeadAssignedAnywhere(d1SlipHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutSlip.getSignalD1Name());
                    removeAssignment(d1SlipHead);
                    layoutSlip.setSignalD1Name(signalHeadName);
                }
                //} else if (assigned != C1) {
                //need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }

        signalHeadName = d2SlipSignalHeadComboBox.getSelectedItemDisplayName();
        if (signalHeadName == null) {
            signalHeadName = "";
        }
        if ((d2SlipHead != null) && setD2SlipHead.isSelected()) {
            if (isHeadOnPanel(d2SlipHead)
                    && (d2SlipHead != getHeadFromName(layoutSlip.getSignalC2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        Bundle.getMessage("SignalsError6",
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(layoutSlip.getSignalD2Name());
                if (layoutSlip.getContinuingSense() == Turnout.CLOSED) {
                    placeD2Slip(signalHeadName);
                } else {
                    placeC2Slip(signalHeadName);
                }
                removeAssignment(d2SlipHead);
                layoutSlip.setSignalD2Name(signalHeadName);
                needRedraw = true;
            }
        } else if (d2SlipHead != null) {
            int assigned = isHeadAssignedHere(d2SlipHead, layoutSlip);
            if (assigned == LayoutTurnout.NONE) {
                if (isHeadOnPanel(d2SlipHead)
                        && isHeadAssignedAnywhere(d2SlipHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            Bundle.getMessage("SignalsError8",
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutSlip.getSignalD2Name());
                    removeAssignment(d2SlipHead);
                    layoutSlip.setSignalD2Name(signalHeadName);
                }
                //} else if (assigned != C2) {
                //need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        } else { //d2SlipHead known to be null here
            removeSignalHeadFromPanel(layoutSlip.getSignalD2Name());
            layoutSlip.setSignalD2Name("");
        }
        //setup logic if requested
        if (setupA1SlipLogic.isSelected() || setupA2SlipLogic.isSelected()) {
            setLogicSlip(a1SlipHead, (TrackSegment) layoutSlip.getConnectC(), a2SlipHead,
                    (TrackSegment) layoutSlip.getConnectD(), setupA1SlipLogic.isSelected(),
                    setupA2SlipLogic.isSelected(), layoutSlip, layoutSlip.getTurnout(),
                    layoutSlip.getTurnoutB(), LayoutTurnout.STATE_AC, LayoutTurnout.STATE_AD, 0);
        }
        if (setupB1SlipLogic.isSelected() || setupB2SlipLogic.isSelected()) {
            setLogicSlip(b1SlipHead, (TrackSegment) layoutSlip.getConnectD(), b2SlipHead,
                    (TrackSegment) layoutSlip.getConnectC(), setupB1SlipLogic.isSelected(),
                    setupB2SlipLogic.isSelected(), layoutSlip, layoutSlip.getTurnout(),
                    layoutSlip.getTurnoutB(), LayoutTurnout.STATE_BD, LayoutTurnout.STATE_BC, 2);
        }
        if (setupC1SlipLogic.isSelected() || setupC2SlipLogic.isSelected()) {
            setLogicSlip(c1SlipHead, (TrackSegment) layoutSlip.getConnectA(), c2SlipHead,
                    (TrackSegment) layoutSlip.getConnectB(), setupC1SlipLogic.isSelected(),
                    setupC2SlipLogic.isSelected(), layoutSlip, layoutSlip.getTurnoutB(),
                    layoutSlip.getTurnout(), LayoutTurnout.STATE_AC, LayoutTurnout.STATE_BC, 4);
        }
        if (setupD1SlipLogic.isSelected() || setupD2SlipLogic.isSelected()) {
            setLogicSlip(d1SlipHead, (TrackSegment) layoutSlip.getConnectB(), d2SlipHead,
                    (TrackSegment) layoutSlip.getConnectA(), setupD1SlipLogic.isSelected(),
                    setupD2SlipLogic.isSelected(), layoutSlip, layoutSlip.getTurnoutB(),
                    layoutSlip.getTurnout(), LayoutTurnout.STATE_BD, LayoutTurnout.STATE_AD, 6);
        }
        //finish up
        setSignalsAtSlipOpenFlag = false;
        setSignalsAtSlipFrame.setVisible(false);

        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }   //setSlipSignalsDonePressed

    private boolean getSlipSignalHeadInformation() {
        a1SlipHead = getSignalHeadFromEntry(a1SlipSignalHeadComboBox, true, setSignalsAtSlipFrame);
        if (a1SlipHead == null) {
            return false;
        }
        a2SlipHead = getSignalHeadFromEntry(a2SlipSignalHeadComboBox, false, setSignalsAtSlipFrame);

        b1SlipHead = getSignalHeadFromEntry(b1SlipSignalHeadComboBox, true, setSignalsAtSlipFrame);
        if (b1SlipHead == null) {
            return false;
        }
        b2SlipHead = getSignalHeadFromEntry(b2SlipSignalHeadComboBox, false, setSignalsAtSlipFrame);

        c1SlipHead = getSignalHeadFromEntry(c1SlipSignalHeadComboBox, true, setSignalsAtSlipFrame);
        if (c1SlipHead == null) {
            return false;
        }
        c2SlipHead = getSignalHeadFromEntry(c2SlipSignalHeadComboBox, false, setSignalsAtSlipFrame);

        d1SlipHead = getSignalHeadFromEntry(d1SlipSignalHeadComboBox, true, setSignalsAtSlipFrame);
        if (d1SlipHead == null) {
            return false;
        }
        d2SlipHead = getSignalHeadFromEntry(d2SlipSignalHeadComboBox, false, setSignalsAtSlipFrame);

        return true;
    }

    private void placeA1Slip(String signalHeadName) {
        //place head near the continuing track of turnout 1
        //placingBlock(getSignalHeadIcon(signalHeadName), false, 0.0, layoutSlip.getConnectA(), layoutSlip.getCoordsA());
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsA = layoutSlip.getCoordsA();
        Point2D coordsD = layoutSlip.getCoordsD();
        Point2D coordsCenter = layoutSlip.getCoordsCenter();

        double aDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsA, coordsCenter));
        double dDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsD, coordsCenter));
        double diffDirDEG = MathUtil.diffAngleDEG(aDirDEG, dDirDEG);
        double shiftX = 0.0;
        if (diffDirDEG < 0.0) {
            shiftX -= shift * Math.cos(Math.toRadians(diffDirDEG));
        }
        Point2D delta = new Point2D.Double(shiftX, -shift);

        delta = MathUtil.rotateDEG(delta, aDirDEG);
        Point2D where = MathUtil.add(coordsA, delta);
        setSignalHeadOnPanel(aDirDEG, signalHeadName, where);
    }

    private void placeA2Slip(String signalHeadName) {
        //SignalHeadIcon l = getSignalHeadIcon(signalHeadName);
        //placingBlock(l, false, (4 + l.getHeight()), layoutSlip.getConnectA(), layoutSlip.getCoordsA());
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsA = layoutSlip.getCoordsA();
        Point2D coordsD = layoutSlip.getCoordsD();
        Point2D coordsCenter = layoutSlip.getCoordsCenter();

        double aDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsA, coordsCenter));
        double dDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsD, coordsCenter));
        double diffDirDEG = MathUtil.diffAngleDEG(aDirDEG, dDirDEG);
        double shiftX = 2.0 * shift;
        if (diffDirDEG < 0.0) {
            shiftX -= shift * Math.cos(Math.toRadians(diffDirDEG));
        }
        Point2D delta = new Point2D.Double(shiftX, -shift);

        delta = MathUtil.rotateDEG(delta, aDirDEG);
        Point2D where = MathUtil.add(coordsA, delta);
        setSignalHeadOnPanel(aDirDEG, signalHeadName, where);
    }

    private void placeB1Slip(String signalHeadName) {
        //placingBlock(getSignalHeadIcon(signalHeadName), true, 0.0, layoutSlip.getConnectB(), layoutSlip.getCoordsB());
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutSlip.getCoordsB();
        Point2D coordsCenter = layoutSlip.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));

        Point2D delta = new Point2D.Double(+shift, -shift);
        delta = MathUtil.rotateDEG(delta, bDirDEG);
        Point2D where = MathUtil.add(coordsB, delta);
        setSignalHeadOnPanel(bDirDEG, signalHeadName, where);
    }

    private void placeB2Slip(String signalHeadName) {
        //SignalHeadIcon l = getSignalHeadIcon(signalHeadName);
        //placingBlock(l, true, (4 + l.getHeight()), layoutSlip.getConnectB(), layoutSlip.getCoordsB());
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutSlip.getCoordsB();
        Point2D coordsCenter = layoutSlip.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));

        Point2D delta = new Point2D.Double(-shift, -shift);
        delta = MathUtil.rotateDEG(delta, bDirDEG);
        Point2D where = MathUtil.add(coordsB, delta);
        setSignalHeadOnPanel(bDirDEG, signalHeadName, where);
    }

    private void placeC1Slip(String signalHeadName) {
        //placingBlock(getSignalHeadIcon(signalHeadName), false, 0.0, layoutSlip.getConnectC(), layoutSlip.getCoordsC());
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutSlip.getCoordsB();
        Point2D coordsC = layoutSlip.getCoordsC();
        Point2D coordsCenter = layoutSlip.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        double cDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsC, coordsCenter));
        double diffDirDEG = MathUtil.diffAngleDEG(cDirDEG, bDirDEG);
        double shiftX = 0.0;
        if (diffDirDEG < 0.0) {
            shiftX -= shift * Math.cos(Math.toRadians(diffDirDEG));
        }
        Point2D delta = new Point2D.Double(shiftX, -shift);

        delta = MathUtil.rotateDEG(delta, cDirDEG);
        Point2D where = MathUtil.add(coordsC, delta);
        setSignalHeadOnPanel(cDirDEG, signalHeadName, where);
    }

    private void placeC2Slip(String signalHeadName) {
        //SignalHeadIcon l = getSignalHeadIcon(signalHeadName);
        //placingBlock(l, false, (4 + l.getHeight()), layoutSlip.getConnectC(), layoutSlip.getCoordsC());
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsB = layoutSlip.getCoordsB();
        Point2D coordsC = layoutSlip.getCoordsC();
        Point2D coordsCenter = layoutSlip.getCoordsCenter();

        double bDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsCenter));
        double cDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsC, coordsCenter));
        double diffDirDEG = MathUtil.diffAngleDEG(cDirDEG, bDirDEG);
        double shiftX = 2.0 * shift;
        if (diffDirDEG < 0.0) {
            shiftX -= shift * Math.cos(Math.toRadians(diffDirDEG));
        }
        Point2D delta = new Point2D.Double(shiftX, -shift);

        delta = MathUtil.rotateDEG(delta, cDirDEG);
        Point2D where = MathUtil.add(coordsC, delta);
        setSignalHeadOnPanel(cDirDEG, signalHeadName, where);
    }

    private void placeD1Slip(String signalHeadName) {
        //placingBlock(getSignalHeadIcon(signalHeadName), true, 0.0, layoutSlip.getConnectD(), layoutSlip.getCoordsD());
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsD = layoutSlip.getCoordsD();
        Point2D coordsCenter = layoutSlip.getCoordsCenter();

        double dDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsD, coordsCenter));

        Point2D delta = new Point2D.Double(+shift, -shift);
        delta = MathUtil.rotateDEG(delta, dDirDEG);
        Point2D where = MathUtil.add(coordsD, delta);
        setSignalHeadOnPanel(dDirDEG, signalHeadName, where);
    }

    private void placeD2Slip(String signalHeadName) {
        //SignalHeadIcon l = getSignalHeadIcon(signalHeadName);
        //placingBlock(l, true, (4 + l.getHeight()), layoutSlip.getConnectD(), layoutSlip.getCoordsD());
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        double shift = Math.hypot(testIcon.getIconHeight(), testIcon.getIconWidth()) / 2.0;

        Point2D coordsD = layoutSlip.getCoordsD();
        Point2D coordsCenter = layoutSlip.getCoordsCenter();

        double dDirDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsD, coordsCenter));

        Point2D delta = new Point2D.Double(-shift, -shift);
        delta = MathUtil.rotateDEG(delta, dDirDEG);
        Point2D where = MathUtil.add(coordsD, delta);
        setSignalHeadOnPanel(dDirDEG, signalHeadName, where);
    }

    private void setLogicSlip(SignalHead head, TrackSegment track1, SignalHead secondHead, TrackSegment track2,
            boolean setup1, boolean setup2,
            LayoutSlip slip, Turnout nearTurnout, Turnout farTurnout,
            int continueState, int divergeState, int number) {
        //initialize common components and ensure all is defined
        LayoutBlock connectorBlock = slip.getLayoutBlock();
        Sensor connectorOccupancy = null;
        if (connectorBlock == null) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        connectorOccupancy = connectorBlock.getOccupancySensor();
        if (connectorOccupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{connectorBlock.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int nearState = layoutSlip.getTurnoutState(nearTurnout, continueState);
        int farState = layoutSlip.getTurnoutState(farTurnout, continueState);

        //setup signal head for continuing track of far turnout (or both tracks of far turnout)
        if ((track1 == null) && setup1) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = null;
        SignalHead nextHead = null;
        if ((track1 != null) && setup1) {
            LayoutBlock block = track1.getLayoutBlock();
            if (block == null) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        Bundle.getMessage("InfoMessage6"),
                        Bundle.getMessage("MessageTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            occupancy = block.getOccupancySensor();
            if (occupancy == null) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        Bundle.getMessage("InfoMessage4",
                                new Object[]{block.getUserName()}),
                        Bundle.getMessage("MessageTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            //need to sort this out???
            nextHead = getNextSignalFromObject(track1, slip,
                    head.getSystemName(), setSignalsAtSlipFrame);
            if ((nextHead == null) && (!reachedEndBumper())) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        Bundle.getMessage("InfoMessage5",
                                new Object[]{block.getUserName()}),
                        Bundle.getMessage("MessageTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (secondHead != null) {
                //this head signals only the continuing track of the far turnout
                if (!initializeBlockBossLogic(head.getSystemName())) {
                    return;
                }
                logic.setMode(BlockBossLogic.TRAILINGMAIN);
                if (farState == Turnout.THROWN) {
                    logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
                }
                logic.setTurnout(farTurnout.getSystemName());
                logic.setSensor1(occupancy.getSystemName());
                if (occupancy != connectorOccupancy) {
                    logic.setSensor2(connectorOccupancy.getSystemName());
                }
                if (nextHead != null) {
                    logic.setWatchedSignal1(nextHead.getSystemName(), false);
                }
                if (auxSignal != null) {
                    logic.setWatchedSignal1Alt(auxSignal.getSystemName());
                }
                String nearSensorName = setupNearLogixSlip(nearTurnout, nearState, head, farTurnout, farState, slip, number);
                addNearSensorToSlipLogic(nearSensorName);
                finalizeBlockBossLogic();
            }
        }
        if ((secondHead != null) && !setup2) {
            return;
        }
        SignalHead savedAuxSignal = auxSignal;
        if (track2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    Bundle.getMessage("InfoMessage7"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block2 = track2.getLayoutBlock();
        if (block2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    Bundle.getMessage("InfoMessage6"),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy2 = block2.getOccupancySensor();
        if (occupancy2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    Bundle.getMessage("InfoMessage4",
                            new Object[]{block2.getUserName()}),
                    Bundle.getMessage("MessageTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        SignalHead nextHead2 = null;
        if (secondHead != null) {
            nextHead2 = getNextSignalFromObject(track2,
                    slip, secondHead.getSystemName(), setSignalsAtSlipFrame);
            if ((nextHead2 == null) && (!reachedEndBumper())) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        Bundle.getMessage("InfoMessage5",
                                new Object[]{block2.getUserName()}),
                        Bundle.getMessage("MessageTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        if ((secondHead == null) && (track1 != null) && setup1) {
            if (!initializeBlockBossLogic(head.getSystemName())) {
                return;
            }
            logic.setMode(BlockBossLogic.FACING);
            logic.setTurnout(farTurnout.getSystemName());
            if (occupancy != null) {
                logic.setWatchedSensor1(occupancy.getSystemName());
            }
            logic.setWatchedSensor2(occupancy2.getSystemName());
            logic.setSensor2(connectorOccupancy.getSystemName());
            if (nextHead != null) {
                logic.setWatchedSignal1(nextHead.getSystemName(), false);
            }
            if (savedAuxSignal != null) {
                logic.setWatchedSignal1Alt(savedAuxSignal.getSystemName());
            }
            if (nextHead2 != null) {
                logic.setWatchedSignal2(nextHead2.getSystemName());
            }
            if (auxSignal != null) {
                logic.setWatchedSignal2Alt(auxSignal.getSystemName());
            }
            String nearSensorName = setupNearLogixSlip(nearTurnout, nearState, head, farTurnout, farState, slip, number + 1);
            addNearSensorToSlipLogic(nearSensorName);
            logic.setLimitSpeed2(true);
            finalizeBlockBossLogic();
        } else if ((secondHead != null) && setup2) {
            if (!initializeBlockBossLogic(secondHead.getSystemName())) {
                return;
            }
            nearState = layoutSlip.getTurnoutState(nearTurnout, divergeState);
            farState = layoutSlip.getTurnoutState(farTurnout, divergeState);

            logic.setMode(BlockBossLogic.TRAILINGDIVERGING);
            if (farState == Turnout.CLOSED) {
                logic.setMode(BlockBossLogic.TRAILINGMAIN);
                logic.setLimitSpeed1(true);
            } else {
                logic.setLimitSpeed2(true);
            }
            logic.setTurnout(farTurnout.getSystemName());
            logic.setSensor1(occupancy2.getSystemName());
            if (occupancy2 != connectorOccupancy) {
                logic.setSensor2(connectorOccupancy.getSystemName());
            }
            if (nextHead2 != null) {
                logic.setWatchedSignal1(nextHead2.getSystemName(), false);
            }
            if (auxSignal != null) {
                logic.setWatchedSignal1Alt(auxSignal.getSystemName());
            }
            String nearSensorName = setupNearLogixSlip(nearTurnout, nearState, secondHead, farTurnout, farState, slip, number + 1);
            addNearSensorToSlipLogic(nearSensorName);
            finalizeBlockBossLogic();
        }
    }   //setLogicSlip

    private String setupNearLogixSlip(Turnout turn, int nearState,
            SignalHead head, Turnout farTurn, int farState, LayoutSlip slip, int number) {
        String turnoutName = turn.getDisplayName();
        String farTurnoutName = farTurn.getDisplayName();

        String logixName = "IX_LAYOUTSLIP:" + slip.ident;
        String sensorName = "IS:" + logixName + "C" + number;
        try {
            InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        } catch (IllegalArgumentException ex) {
            log.error("Trouble creating sensor " + sensorName + " while setting up Logix.");
            return "";
        }
        boolean newConditional = false;
        Logix x = InstanceManager.getDefault(LogixManager.class
        ).getBySystemName(logixName);

        if (x == null) {
            x = InstanceManager.getDefault(LogixManager.class
            ).createNewLogix(logixName, "");
            newConditional = true;
            if (x == null) {
                log.error("Trouble creating logix " + logixName + " while setting up signal logic.");
                return "";
            }
            x.setComment("Layout Slip, Signalhead logic");
        }
        x.deActivateLogix();
        String cName = logixName + "C" + number;

        Conditional c = InstanceManager.getDefault(ConditionalManager.class
        ).getBySystemName(cName);

        if (c == null) {
            c = InstanceManager.getDefault(ConditionalManager.class
            ).
                    createNewConditional(cName, "");
            newConditional = true;
            if (c == null) {
                log.error("Trouble creating conditional " + cName + " while setting up Logix.");
                return "";
            }
        }
        Conditional.Type type = Conditional.Type.TURNOUT_THROWN;
        if (nearState == Turnout.CLOSED) {
            type = Conditional.Type.TURNOUT_CLOSED;
        }
        ArrayList<ConditionalVariable> variableList = new ArrayList<>();
        variableList.add(new ConditionalVariable(false, Conditional.Operator.AND,
                type, turnoutName, true));

        type = Conditional.Type.TURNOUT_THROWN;
        if (farState == Turnout.CLOSED) {
            type = Conditional.Type.TURNOUT_CLOSED;
        }
        variableList.add(new ConditionalVariable(false, Conditional.Operator.AND,
                type, farTurnoutName, true));
        c.setStateVariables(variableList);
        ArrayList<ConditionalAction> actionList = new ArrayList<>();
        actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                Conditional.Action.SET_SENSOR, sensorName,
                Sensor.INACTIVE, ""));
        actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE,
                Conditional.Action.SET_SENSOR, sensorName,
                Sensor.ACTIVE, ""));
        c.setAction(actionList);		//string data
        if (newConditional) {
            x.addConditional(cName, -1);
        }
        x.activateLogix();
        return sensorName;
    }   //setupNearLogixSlip

    /*
	 * Adds the sensor specified to the open BlockBossLogic, provided it is not already there and
	 *	provided there is an open slot. If 'name' is null or empty, returns without doing anything.
     */
    private void addNearSensorToSlipLogic(String name) {
        if ((name != null) && !name.isEmpty()) {
            //return if a sensor by this name is already present
            if (logic.getSensor1() != null && logic.getSensor1().equals(name)) {
                return;
            }
            if (logic.getSensor2() != null && logic.getSensor2().equals(name)) {
                return;
            }
            if (logic.getSensor3() != null && logic.getSensor3().equals(name)) {
                return;
            }
            if (logic.getSensor4() != null && logic.getSensor4().equals(name)) {
                return;
            }
            if (logic.getSensor5() != null && logic.getSensor5().equals(name)) {
                return;
            }
            //add in the first available slot
            if (logic.getSensor1() == null) {
                logic.setSensor1(name);
            } else if (logic.getSensor2() == null) {
                logic.setSensor2(name);
            } else if (logic.getSensor3() == null) {
                logic.setSensor3(name);
            } else if (logic.getSensor4() == null) {
                logic.setSensor4(name);
            } else if (logic.getSensor5() == null) {
                logic.setSensor5(name);
            } else {
                log.error("Error - could not add sensor to SSL for signal head " + logic.getDrivenSignal());
            }
        }
    }

    /**
     * get a signal head icon for the given signal head
     *
     * @param signalName name of a signal head.
     * @return a SignalHeadIcon for the signal.
     */
    @CheckReturnValue
    public SignalHeadIcon getSignalHeadIcon(@Nonnull String signalName) {
        if (signalIconEditor == null) {
            signalIconEditor = layoutEditor.signalIconEditor;
        }
        SignalHeadIcon l = new SignalHeadIcon(layoutEditor);
        l.setSignalHead(signalName);
        l.setIcon("SignalHeadStateRed", signalIconEditor.getIcon(0));
        l.setIcon("SignalHeadStateFlashingRed", signalIconEditor.getIcon(1));
        l.setIcon("SignalHeadStateYellow", signalIconEditor.getIcon(2));
        l.setIcon("SignalHeadStateFlashingYellow", signalIconEditor.getIcon(3));
        l.setIcon("SignalHeadStateGreen", signalIconEditor.getIcon(4));
        l.setIcon("SignalHeadStateFlashingGreen", signalIconEditor.getIcon(5));
        l.setIcon("SignalHeadStateDark", signalIconEditor.getIcon(6));
        l.setIcon("SignalHeadStateHeld", signalIconEditor.getIcon(7));
        l.setIcon("SignalHeadStateLunar", signalIconEditor.getIcon(8));
        l.setIcon("SignalHeadStateFlashingLunar", signalIconEditor.getIcon(9));
        l.rotate(90);
        return l;
    }

    //convenience strings
    private final String eastString = Bundle.getMessage("East");
    private final String westString = Bundle.getMessage("West");
    private final String continuingString = Bundle.getMessage("Continuing");
    private final String divergingString = Bundle.getMessage("Diverging");
    private final String throatString = Bundle.getMessage("Throat");
    private final String throatContinuingString = Bundle.getMessage("ThroatContinuing");
    private final String throatDivergingString = Bundle.getMessage("ThroatDiverging");

    private final String divergingAString = Bundle.getMessage("Diverging_", "A");
    private final String divergingBString = Bundle.getMessage("Diverging_", "B");

    protected Boolean addLayoutTurnoutSignalHeadInfoToMenu(
            @Nonnull String inTurnoutNameA, @Nonnull String inTurnoutNameB,
            @Nonnull JMenu inMenu) {
        Boolean result = false; //assume failure (pessimist!)

        //lookup turnouts
        turnout = turnout1 = turnoutA = InstanceManager.turnoutManagerInstance().getTurnout(inTurnoutNameA);
        turnout2 = turnoutB = InstanceManager.turnoutManagerInstance().getTurnout(inTurnoutNameB);
        //map those to layout turnouts (if possible)
        for (LayoutTurnout lt : layoutEditor.getLayoutTurnouts()) {
            Turnout to = lt.getTurnout();
            if (to != null) {
                String uname = to.getUserName();
                String sname = to.getSystemName();
                if (!inTurnoutNameA.isEmpty() && (sname.equals(inTurnoutNameA) || ((uname != null) && uname.equals(inTurnoutNameA)))) {
                    layoutTurnout = layoutTurnout1 = layoutTurnoutA = lt;
                }
                if (!inTurnoutNameB.isEmpty() && (sname.equals(inTurnoutNameB) || ((uname != null) && uname.equals(inTurnoutNameB)))) {
                    layoutTurnout2 = layoutTurnoutB = lt;
                }
            }
        }

        int before_mcc = inMenu.getMenuComponentCount();
        if (before_mcc != 0) {
            inMenu.add(new JSeparator());
        }
        int linkType = layoutTurnout.getLinkType();
        if ((layoutTurnout.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)
                || (layoutTurnout.getTurnoutType() == LayoutTurnout.RH_XOVER)
                || (layoutTurnout.getTurnoutType() == LayoutTurnout.LH_XOVER)) {
            JMenuItem jmi = inMenu.add(Bundle.getMessage("Crossover"));
            jmi.setEnabled(false);
            inMenu.add(new JSeparator());
            before_mcc += 2;
            addInfoToMenu("A " + continuingString, layoutTurnout.getSignalA1Name(), inMenu);
            addInfoToMenu("A " + divergingString, layoutTurnout.getSignalA2Name(), inMenu);
            addInfoToMenu("B " + continuingString, layoutTurnout.getSignalB1Name(), inMenu);
            addInfoToMenu("B " + divergingString, layoutTurnout.getSignalB2Name(), inMenu);
            addInfoToMenu("C " + continuingString, layoutTurnout.getSignalC1Name(), inMenu);
            addInfoToMenu("C " + divergingString, layoutTurnout.getSignalC2Name(), inMenu);
            addInfoToMenu("D " + continuingString, layoutTurnout.getSignalD1Name(), inMenu);
            addInfoToMenu("D " + divergingString, layoutTurnout.getSignalD2Name(), inMenu);
        } else if (linkType == LayoutTurnout.NO_LINK) {
            JMenuItem jmi = inMenu.add(Bundle.getMessage("BeanNameTurnout"));
            jmi.setEnabled(false);
            inMenu.add(new JSeparator());
            before_mcc += 2;
            addInfoToMenu(throatContinuingString, layoutTurnout.getSignalA1Name(), inMenu);
            addInfoToMenu(throatDivergingString, layoutTurnout.getSignalA2Name(), inMenu);
            addInfoToMenu(continuingString, layoutTurnout.getSignalB1Name(), inMenu);
            addInfoToMenu(divergingString, layoutTurnout.getSignalC1Name(), inMenu);
        } else if (linkType == LayoutTurnout.THROAT_TO_THROAT) {
            String menuString = Bundle.getMessage("ThroatToThroat") + " (";
            menuString += Bundle.getMessage("BeanNameTurnout") + ", " + Bundle.getMessage("BeanNameRoute");
            menuString += ", " + Bundle.getMessage("BeanNameSignalHead") + ":)";
            JMenuItem jmi = inMenu.add(menuString);
            jmi.setEnabled(false);
            inMenu.add(new JSeparator());
            before_mcc += 2;
            addInfoToMenu(eastString + ", " + continuingString + ", " + continuingString, layoutTurnout1.getSignalB1Name(), inMenu);
            addInfoToMenu(eastString + ", " + continuingString + ", " + divergingString, layoutTurnout1.getSignalB2Name(), inMenu);
            addInfoToMenu(eastString + ", " + divergingString + ", " + continuingString, layoutTurnout1.getSignalC1Name(), inMenu);
            addInfoToMenu(eastString + ", " + divergingString + ", " + divergingString, layoutTurnout1.getSignalC2Name(), inMenu);
            addInfoToMenu(westString + ", " + continuingString + ", " + continuingString, layoutTurnout2.getSignalB1Name(), inMenu);
            addInfoToMenu(westString + ", " + continuingString + ", " + divergingString, layoutTurnout2.getSignalB2Name(), inMenu);
            addInfoToMenu(westString + ", " + divergingString + ", " + continuingString, layoutTurnout2.getSignalC1Name(), inMenu);
            addInfoToMenu(westString + ", " + divergingString + ", " + divergingString, layoutTurnout2.getSignalC2Name(), inMenu);
        } else if (linkType == LayoutTurnout.FIRST_3_WAY) {
            JMenuItem jmi = inMenu.add(Bundle.getMessage("ThreeWay"));
            jmi.setEnabled(false);
            inMenu.add(new JSeparator());
            before_mcc += 2;
            addInfoToMenu(throatString + " " + continuingString, layoutTurnoutA.getSignalA1Name(), inMenu);
            addInfoToMenu(throatString + " " + divergingAString, layoutTurnoutA.getSignalA2Name(), inMenu);
            addInfoToMenu(throatString + " " + divergingBString, layoutTurnoutA.getSignalA3Name(), inMenu);
            addInfoToMenu(continuingString, layoutTurnoutA.getSignalC1Name(), inMenu);
            addInfoToMenu(divergingAString, layoutTurnoutB.getSignalB1Name(), inMenu);
            addInfoToMenu(divergingBString, layoutTurnoutB.getSignalC1Name(), inMenu);
        } else if (linkType == LayoutTurnout.SECOND_3_WAY) {
            JMenuItem jmi = inMenu.add(Bundle.getMessage("ThreeWay"));
            jmi.setEnabled(false);
            inMenu.add(new JSeparator());
            before_mcc += 2;
            addInfoToMenu(throatString + " " + continuingString, layoutTurnoutB.getSignalA1Name(), inMenu);
            addInfoToMenu(throatString + " " + divergingAString, layoutTurnoutB.getSignalA2Name(), inMenu);
            addInfoToMenu(throatString + " " + divergingBString, layoutTurnoutB.getSignalA3Name(), inMenu);
            addInfoToMenu(continuingString, layoutTurnoutB.getSignalC1Name(), inMenu);
            addInfoToMenu(divergingAString, layoutTurnoutA.getSignalB1Name(), inMenu);
            addInfoToMenu(divergingBString, layoutTurnoutA.getSignalC1Name(), inMenu);
        }
        int after_mcc = inMenu.getMenuComponentCount();
        if (before_mcc != after_mcc) {
            inMenu.add(new JSeparator());
            result = true;   //it's GOOD!
        }
        return result;
    }   //addLayoutTurnoutSignalHeadInfoToMenu

    protected Boolean addBlockBoundarySignalHeadInfoToMenu(
            @Nonnull PositionablePoint inPositionablePoint,
            @Nonnull JMenu inMenu) {
        Boolean result = false; //assume failure (pessimist!)

        int before_mcc = inMenu.getMenuComponentCount();
        if (before_mcc != 0) {
            inMenu.add(new JSeparator());
        }

        JMenuItem jmi = inMenu.add(Bundle.getMessage("BlockBoundary"));
        jmi.setEnabled(false);
        inMenu.add(new JSeparator());
        before_mcc += 2;

        addInfoToMenu(Bundle.getMessage("East/SouthBound"), inPositionablePoint.getEastBoundSignal(), inMenu);
        addInfoToMenu(Bundle.getMessage("West/NorthBound"), inPositionablePoint.getWestBoundSignal(), inMenu);

        int after_mcc = inMenu.getMenuComponentCount();
        if (before_mcc != after_mcc) {
            inMenu.add(new JSeparator());
            result = true;   //it's GOOD!
        }

        return result;
    }

    protected Boolean addLevelXingSignalHeadInfoToMenu(
            @Nonnull LevelXing inLevelXing,
            @Nonnull JMenu inMenu) {
        Boolean result = false; //assume failure (pessimist!)

        int before_mcc = inMenu.getMenuComponentCount();
        if (before_mcc != 0) {
            inMenu.add(new JSeparator());
        }

        JMenuItem jmi = inMenu.add(Bundle.getMessage("LevelCrossing"));
        jmi.setEnabled(false);
        inMenu.add(new JSeparator());
        before_mcc += 2;

        addInfoToMenu(Bundle.getMessage("MakeLabel",
                Bundle.getMessage("TrackXConnect", "A")),
                inLevelXing.getSignalAName(), inMenu);
        addInfoToMenu(Bundle.getMessage("MakeLabel",
                Bundle.getMessage("TrackXConnect", "B")),
                inLevelXing.getSignalBName(), inMenu);
        addInfoToMenu(Bundle.getMessage("MakeLabel",
                Bundle.getMessage("TrackXConnect", "C")),
                inLevelXing.getSignalCName(), inMenu);
        addInfoToMenu(Bundle.getMessage("MakeLabel",
                Bundle.getMessage("TrackXConnect", "D")),
                inLevelXing.getSignalDName(), inMenu);

        int after_mcc = inMenu.getMenuComponentCount();
        if (before_mcc != after_mcc) {
            inMenu.add(new JSeparator());
            result = true;   //it's GOOD!
        }

        return result;
    }

    protected Boolean addLayoutSlipSignalHeadInfoToMenu(
            @Nonnull LayoutTurnout inLayoutTurnout,
            @Nonnull JMenu inMenu) {
        Boolean result = false; //assume failure (pessimist!)

        int before_mcc = inMenu.getMenuComponentCount();
        if (before_mcc != 0) {
            inMenu.add(new JSeparator());
        }

        JMenuItem jmi = inMenu.add(Bundle.getMessage("Slip"));
        jmi.setEnabled(false);
        inMenu.add(new JSeparator());
        before_mcc += 2;

        addInfoToMenu("A " + continuingString, inLayoutTurnout.getSignalA1Name(), inMenu);
        addInfoToMenu("A " + divergingString, inLayoutTurnout.getSignalA2Name(), inMenu);
        addInfoToMenu("B " + continuingString, inLayoutTurnout.getSignalB1Name(), inMenu);
        addInfoToMenu("B " + divergingString, inLayoutTurnout.getSignalB2Name(), inMenu);
        addInfoToMenu("C " + continuingString, inLayoutTurnout.getSignalC1Name(), inMenu);
        addInfoToMenu("C " + divergingString, inLayoutTurnout.getSignalC2Name(), inMenu);
        addInfoToMenu("D " + continuingString, inLayoutTurnout.getSignalD1Name(), inMenu);
        addInfoToMenu("D " + divergingString, inLayoutTurnout.getSignalD2Name(), inMenu);

        int after_mcc = inMenu.getMenuComponentCount();
        if (before_mcc != after_mcc) {
            inMenu.add(new JSeparator());
            result = true;   //it's GOOD!
        }

        return result;
    }

    private void addInfoToMenu(@CheckForNull String title,
            @CheckForNull String info, @Nonnull JMenu menu) {
        if ((title != null) && !title.isEmpty() && (info != null) && !info.isEmpty()) {
            addInfoToMenu(title + ": " + info, menu);
        }
    }

    private void addInfoToMenu(@CheckForNull String info, @Nonnull JMenu menu) {
        if ((info != null) && !info.isEmpty()) {
            JMenuItem jmi = new JMenuItem(info);
            jmi.setEnabled(false);
            menu.add(jmi);
        }
    }

    private void oneFrameToRuleThemAll(@Nonnull JmriJFrame goodFrame) {
        setSensorsAtBlockBoundaryFrame = closeIfNotFrame(goodFrame, setSensorsAtBlockBoundaryFrame);
        setSensorsAtLevelXingFrame = closeIfNotFrame(goodFrame, setSensorsAtLevelXingFrame);
        setSensorsAtSlipFrame = closeIfNotFrame(goodFrame, setSensorsAtSlipFrame);
        setSensorsAtTurnoutFrame = closeIfNotFrame(goodFrame, setSensorsAtTurnoutFrame);
        setSignalMastsAtBlockBoundaryFrame = closeIfNotFrame(goodFrame, setSignalMastsAtBlockBoundaryFrame);
        setSignalMastsAtLayoutSlipFrame = closeIfNotFrame(goodFrame, setSignalMastsAtLayoutSlipFrame);
        setSignalMastsAtLevelXingFrame = closeIfNotFrame(goodFrame, setSignalMastsAtLevelXingFrame);
        setSignalMastsAtTurnoutFrame = closeIfNotFrame(goodFrame, setSignalMastsAtTurnoutFrame);
        setSignalsAt3WayTurnoutFrame = closeIfNotFrame(goodFrame, setSignalsAt3WayTurnoutFrame);
        setSignalsAtBlockBoundaryFrame = closeIfNotFrame(goodFrame, setSignalsAtBlockBoundaryFrame);
        setSignalsAtLevelXingFrame = closeIfNotFrame(goodFrame, setSignalsAtLevelXingFrame);
        setSignalsAtSlipFrame = closeIfNotFrame(goodFrame, setSignalsAtSlipFrame);
        setSignalsAtThroatToThroatTurnoutsFrame = closeIfNotFrame(goodFrame, setSignalsAtThroatToThroatTurnoutsFrame);
        setSignalsAtTurnoutFrame = closeIfNotFrame(goodFrame, setSignalsAtTurnoutFrame);
        setSignalsAtXoverTurnoutFrame = closeIfNotFrame(goodFrame, setSignalsAtXoverTurnoutFrame);
    }

    private JmriJFrame closeIfNotFrame(@Nonnull JmriJFrame goodFrame, @CheckForNull JmriJFrame badFrame) {
        JmriJFrame result = badFrame;
        if ((badFrame != null) && (goodFrame != badFrame)) {
            badFrame.setVisible(false);
            badFrame.dispose();
            result = null;
        }
        return result;
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutEditorTools.class);
}
