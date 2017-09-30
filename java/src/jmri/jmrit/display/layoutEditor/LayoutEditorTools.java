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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;
import jmri.util.swing.JmriBeanComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Layout Editor Tools provides tools making use of layout connectivity
 * available in Layout Editor panels.
 * <P>
 * The tools in this module are accessed via the Tools menu in Layout Editor.
 *
 * @author Dave Duchamp Copyright (c) 2007
 */
public class LayoutEditorTools {

    // Defined text resource, should be called by Bundle.getMessage() to allow reuse of shared keys via path
    //static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    // constants
    private int NONE = 0;  // Signal at Turnout Positions
    private int A1 = 1;
    private int A2 = 2;
    private int A3 = 3;
    private int B1 = 4;
    private int B2 = 5;
    private int C1 = 6;
    private int C2 = 7;
    private int D1 = 8;
    private int D2 = 9;

    // operational instance variables shared between tools
    private LayoutEditor layoutEditor = null;
    private MultiIconEditor signalIconEditor = null;
    private JFrame signalFrame = null;
    private boolean needRedraw = false;
    private BlockBossLogic logic = null;
    private SignalHead auxSignal = null;

    // constructor method
    public LayoutEditorTools(@Nonnull LayoutEditor thePanel) {
        layoutEditor = thePanel;

        LayoutEditor.setupComboBox(turnoutComboBox, true, true);

        LayoutEditor.setupComboBox(throatContinuingSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(throatDivergingSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(continuingSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(divergingSignalHeadComboBox, true, true);

        LayoutEditor.setupComboBox(block1IDComboBox, true, true);
        LayoutEditor.setupComboBox(block2IDComboBox, true, true);

        LayoutEditor.setupComboBox(eastBoundSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(westBoundSignalHeadComboBox, true, true);

        LayoutEditor.setupComboBox(a1ComboBox, true, true);
        LayoutEditor.setupComboBox(a2ComboBox, true, true);
        LayoutEditor.setupComboBox(b1ComboBox, true, true);
        LayoutEditor.setupComboBox(b2ComboBox, true, true);
        LayoutEditor.setupComboBox(c1ComboBox, true, true);
        LayoutEditor.setupComboBox(c2ComboBox, true, true);
        LayoutEditor.setupComboBox(d1ComboBox, true, true);
        LayoutEditor.setupComboBox(d2ComboBox, true, true);

        LayoutEditor.setupComboBox(blockAComboBox, true, true);
        LayoutEditor.setupComboBox(blockCComboBox, true, true);

        LayoutEditor.setupComboBox(aSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(bSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(cSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(dSignalHeadComboBox, true, true);

        LayoutEditor.setupComboBox(turnout1ComboBox, true, true);
        log.debug("turnout1ComboBox set up, size: {}", turnout1ComboBox.getItemCount());
        LayoutEditor.setupComboBox(turnout2ComboBox, true, true);
        log.debug("turnout2ComboBox set up, size: {}", turnout1ComboBox.getItemCount());
        LayoutEditor.setupComboBox(a1TToTSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(a2TToTSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(b1TToTSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(b2TToTSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(c1TToTSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(c2TToTSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(d1TToTSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(d2TToTSignalHeadComboBox, true, true);

        LayoutEditor.setupComboBox(turnoutAComboBox, true, true);
        LayoutEditor.setupComboBox(turnoutBComboBox, true, true);

        LayoutEditor.setupComboBox(a1_3WaySignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(a2_3WaySignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(a3_3WaySignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(b_3WaySignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(c_3WaySignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(d_3WaySignalHeadComboBox, true, true);

        LayoutEditor.setupComboBox(signalMastsTurnoutComboBox, true, true);

        LayoutEditor.setupComboBox(xingBlockAComboBox, true, true);
        LayoutEditor.setupComboBox(xingBlockCComboBox, true, true);

        LayoutEditor.setupComboBox(sensorsTurnoutComboBox, true, true);

        LayoutEditor.setupComboBox(xingSensorsBlockAComboBox, true, true);
        LayoutEditor.setupComboBox(xingSensorsBlockCComboBox, true, true);

        LayoutEditor.setupComboBox(a1SlipSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(a2SlipSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(b1SlipSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(b2SlipSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(c1SlipSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(c2SlipSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(d1SlipSignalHeadComboBox, true, true);
        LayoutEditor.setupComboBox(d2SlipSignalHeadComboBox, true, true);
    }

    /**
     * Tool to set signals at a turnout, including placing the signal icons and
     * optionally setup of Simple Signal Logic for each signal head
     * <P>
     * This tool assumes left facing signal head icons have been selected, and
     * will rotate the signal head icons accordingly.
     * <P>
     * This tool will place throat icons on the right side of the track, and
     * continuing and diverging icons on the outside edge of the turnout.
     */
    // operational variables for Set Signals at Turnout tool
    private JmriJFrame setSignalsFrame = null;
    private boolean setSignalsOpen = false;

    private JmriBeanComboBox turnoutComboBox = new JmriBeanComboBox(
            InstanceManager.turnoutManagerInstance(),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JmriBeanComboBox throatContinuingSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox throatDivergingSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox continuingSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox divergingSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JCheckBox setPlaceAllHeads = new JCheckBox(Bundle.getMessage("PlaceAllHeads"));
    private JCheckBox setupAllLogic = new JCheckBox(Bundle.getMessage("SetAllLogic"));

    private JCheckBox setThroatContinuing = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupLogicThroatContinuing = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setThroatDiverging = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupLogicThroatDiverging = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setContinuing = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupLogicContinuing = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setDiverging = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupLogicDiverging = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JButton getSavedSignalHeads = null;
    private JButton changeSignalIcon = null;
    private JButton setSignalsDone = null;
    private JButton setSignalsCancel = null;

    private LayoutTurnout layoutTurnout = null;
    private double placeSignalDirectionDEG = 0.0;

    private boolean turnoutFromMenu = false;
    private Turnout turnout = null;
    private SignalHead throatContinuingHead = null;
    private SignalHead throatDivergingHead = null;
    private SignalHead continuingHead = null;
    private SignalHead divergingHead = null;

    // display dialog for Set Signals at Turnout tool
    public void setSignalsAtTurnoutFromMenu(@Nonnull LayoutTurnout to,
            @Nonnull MultiIconEditor theEditor, @Nonnull JFrame theFrame) {
        turnoutFromMenu = true;
        layoutTurnout = to;
        turnout = to.getTurnout();
        turnoutComboBox.setText(to.getTurnoutName());
        setSignalsAtTurnout(theEditor, theFrame);
    }

    public void setSignalsAtTurnout(@Nonnull MultiIconEditor theEditor, @Nonnull JFrame theFrame) {
        signalIconEditor = theEditor;
        signalFrame = theFrame;
        if (setSignalsOpen) {
            setSignalsFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (setSignalsFrame == null) {
            setSignalsFrame = new JmriJFrame(Bundle.getMessage("SignalsAtTurnout"), false, true);
            setSignalsFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtTurnout", true);
            setSignalsFrame.setLocation(70, 30);
            Container theContentPane = setSignalsFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel1 = new JPanel(new FlowLayout());
            if (turnoutFromMenu) {
                JLabel turnoutNameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
                        + layoutTurnout.getTurnoutName());
                panel1.add(turnoutNameLabel);
            } else {
                JLabel turnoutNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout"));
                panel1.add(turnoutNameLabel);
                panel1.add(turnoutComboBox);
                turnoutComboBox.setToolTipText(Bundle.getMessage("SignalsTurnoutNameHint"));
            }
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
                // (de)select all checkboxes
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
                // (de)select all checkboxes
                setupLogicThroatContinuing.setSelected(isSelected);
                setupLogicThroatDiverging.setSelected(isSelected);
                setupLogicContinuing.setSelected(isSelected);
                setupLogicDiverging.setSelected(isSelected);
            });
            theContentPane.add(panel2a);

            JPanel panel21 = new JPanel(new FlowLayout());
            JLabel throatContinuingLabel = new JLabel(Bundle.getMessage("ThroatContinuing") + " : ");
            panel21.add(throatContinuingLabel);
            panel21.add(throatContinuingSignalHeadComboBox);
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
            JLabel throatDivergingLabel = new JLabel(Bundle.getMessage("ThroatDiverging") + " : ");
            panel31.add(throatDivergingLabel);
            panel31.add(throatDivergingSignalHeadComboBox);
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
            JLabel continuingLabel = new JLabel(Bundle.getMessage("Continuing") + " : ");
            panel41.add(continuingLabel);
            panel41.add(continuingSignalHeadComboBox);
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
            JLabel divergingLabel = new JLabel(Bundle.getMessage("Diverging") + " : ");
            panel51.add(divergingLabel);
            panel51.add(divergingSignalHeadComboBox);
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

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(setSignalsDone);
                rootPane.setDefaultButton(setSignalsDone);
            });

            panel6.add(setSignalsCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setSignalsCancel.addActionListener((ActionEvent e) -> {
                setSignalsCancelPressed(e);
            });
            setSignalsCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
            setSignalsFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSignalsCancelPressed(null);
                }
            });
            if (turnoutFromMenu) {
                turnoutSignalsGetSaved(null);
            }
        }
        setSignalsFrame.pack();
        setSignalsFrame.setVisible(true);
        setSignalsOpen = true;
    }   // setSignalsAtTurnout

    private void turnoutSignalsGetSaved(ActionEvent a) {
        if (getTurnoutInformation(false)) {
            throatContinuingSignalHeadComboBox.setText(layoutTurnout.getSignalA1Name());
            throatDivergingSignalHeadComboBox.setText(layoutTurnout.getSignalA2Name());
            continuingSignalHeadComboBox.setText(layoutTurnout.getSignalB1Name());
            divergingSignalHeadComboBox.setText(layoutTurnout.getSignalC1Name());
        }
    }

    private void setSignalsCancelPressed(ActionEvent a) {
        setSignalsOpen = false;
        turnoutFromMenu = false;
        setSignalsFrame.setVisible(false);
    }

    private void setSignalsDonePressed(ActionEvent a) {
        // process turnout name
        if (!getTurnoutInformation(false)) {
            return;
        }
        // process signal head names
        if (!getTurnoutSignalHeadInformation()) {
            return;
        }
        // place signals as requested
        String signalHeadName = throatContinuingSignalHeadComboBox.getDisplayName();
        if (setThroatContinuing.isSelected()) {
            if (isHeadOnPanel(throatContinuingHead)
                    && (throatContinuingHead != getHeadFromName(layoutTurnout.getSignalA1Name()))) {
                JOptionPane.showMessageDialog(setSignalsFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(throatContinuingHead)
                        && isHeadAssignedAnywhere(throatContinuingHead)) {
                    JOptionPane.showMessageDialog(setSignalsFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
                    removeAssignment(throatContinuingHead);
                    layoutTurnout.setSignalA1Name(signalHeadName);
                }
                //} else if (assigned != A1) {
                // need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }
        signalHeadName = throatDivergingSignalHeadComboBox.getDisplayName();
        if ((setThroatDiverging.isSelected()) && (throatDivergingHead != null)) {
            if (isHeadOnPanel(throatDivergingHead)
                    && (throatDivergingHead != getHeadFromName(layoutTurnout.getSignalA2Name()))) {
                JOptionPane.showMessageDialog(setSignalsFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(throatDivergingHead)
                        && isHeadAssignedAnywhere(throatDivergingHead)) {
                    JOptionPane.showMessageDialog(setSignalsFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
                    removeAssignment(throatDivergingHead);
                    layoutTurnout.setSignalA2Name(signalHeadName);
                }
                //} else if (assigned != A2) {
                // need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        } else {   // throatDivergingHead is always null here
            removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
            layoutTurnout.setSignalA2Name("");
        }

        signalHeadName = continuingSignalHeadComboBox.getDisplayName();
        if (setContinuing.isSelected()) {
            if (isHeadOnPanel(continuingHead)
                    && (continuingHead != getHeadFromName(layoutTurnout.getSignalB1Name()))) {
                JOptionPane.showMessageDialog(setSignalsFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(continuingHead)
                        && isHeadAssignedAnywhere(continuingHead)) {
                    JOptionPane.showMessageDialog(setSignalsFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
                    removeAssignment(continuingHead);
                    layoutTurnout.setSignalB1Name(signalHeadName);
                }
                //} else if (assigned != B1) {
                // need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }

        signalHeadName = divergingSignalHeadComboBox.getDisplayName();
        if (setDiverging.isSelected()) {
            if (isHeadOnPanel(divergingHead)
                    && (divergingHead != getHeadFromName(layoutTurnout.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(divergingHead)
                        && isHeadAssignedAnywhere(divergingHead)) {
                    JOptionPane.showMessageDialog(setSignalsFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
                    removeAssignment(divergingHead);
                    layoutTurnout.setSignalC1Name(signalHeadName);
                }
                //} else if (assigned != C1) {
                // need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }
        // setup Logic if requested and enough information is available
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
        // make sure this layout turnout is not linked to another
        layoutTurnout.setLinkType(LayoutTurnout.NO_LINK);
        layoutTurnout.setLinkedTurnoutName("");
        // finish up
        setSignalsOpen = false;
        turnoutFromMenu = false;
        setSignalsFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            needRedraw = false;
        }
    }   // setSignalsDonePressed

    private boolean getTurnoutInformation(boolean isCrossover) {
        String str = "";
        if ((!turnoutFromMenu && !isCrossover) || (!xoverFromMenu && isCrossover)) {
            turnout = null;
            layoutTurnout = null;
            if (isCrossover) {
                str = NamedBean.normalizeUserName(xoverTurnoutName);
            } else {
                str = turnoutComboBox.getDisplayName();
            }
            if ((str == null) || str.isEmpty()) {
                JOptionPane.showMessageDialog(setSignalsFrame, Bundle.getMessage("SignalsError1"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
            turnout = InstanceManager.turnoutManagerInstance().getTurnout(str);
            if (turnout == null) {
                JOptionPane.showMessageDialog(setSignalsFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError2"),
                                new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            } else {
                String uname = turnout.getUserName();
                if ((uname == null) || uname.isEmpty() || !uname.equals(str)) {
                    str = str.toUpperCase();
                    if (isCrossover) {
                        xoverTurnoutName = str;
                    } else {
                        turnoutComboBox.setText(str);
                    }
                }
            }
            for (LayoutTurnout t : layoutEditor.getLayoutTurnouts()) {
                if (t.getTurnout() == turnout) {
                    layoutTurnout = t;
                    if (((t.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)
                            || (t.getTurnoutType() == LayoutTurnout.RH_XOVER)
                            || (t.getTurnoutType() == LayoutTurnout.LH_XOVER))
                            && (!isCrossover)) {
                        JOptionPane.showMessageDialog(layoutEditor,
                                Bundle.getMessage("InfoMessage1"), "",
                                JOptionPane.INFORMATION_MESSAGE);
                        setSignalsCancelPressed(null);
                        return false;
                    }
                    if ((!((t.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)
                            || (t.getTurnoutType() == LayoutTurnout.RH_XOVER)
                            || (t.getTurnoutType() == LayoutTurnout.LH_XOVER)))
                            && isCrossover) {
                        JOptionPane.showMessageDialog(layoutEditor,
                                Bundle.getMessage("InfoMessage8"), "",
                                JOptionPane.INFORMATION_MESSAGE);
                        setXoverSignalsCancelPressed(null);
                        return false;
                    }
                }
            }
        }

        if (layoutTurnout != null) {
            if (isCrossover) {
                Point2D coordsA = layoutTurnout.getCoordsA();
                Point2D coordsB = layoutTurnout.getCoordsB();
                placeSignalDirectionDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsB, coordsA));
            } else {
                Point2D coordsA = layoutTurnout.getCoordsA();
                Point2D coordsCenter = layoutTurnout.getCoordsCenter();
                placeSignalDirectionDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsCenter, coordsA));
            }
            return true;
        }
        JOptionPane.showMessageDialog(setSignalsFrame,
                MessageFormat.format(Bundle.getMessage("SignalsError3"),
                        new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
        return false;
    }   // getTurnoutInformation

    private boolean getTurnoutSignalHeadInformation() {
        throatContinuingHead = getSignalHeadFromEntry(throatContinuingSignalHeadComboBox, true, setSignalsFrame);
        if (throatContinuingHead == null) {
            return false;
        }
        throatDivergingHead = getSignalHeadFromEntry(throatDivergingSignalHeadComboBox, false, setSignalsFrame);
        continuingHead = getSignalHeadFromEntry(continuingSignalHeadComboBox, true, setSignalsFrame);
        if (continuingHead == null) {
            return false;
        }
        divergingHead = getSignalHeadFromEntry(divergingSignalHeadComboBox, true, setSignalsFrame);
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
        String signalHeadName = throatContinuingSignalHeadComboBox.getDisplayName();
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
        String signalHeadName = throatDivergingSignalHeadComboBox.getDisplayName();
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
            JOptionPane.showMessageDialog(setSignalsFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String signalHeadName = throatContinuingSignalHeadComboBox.getDisplayName();
        SignalHead nextHead = getNextSignalFromObject(track,
                layoutTurnout, signalHeadName, setSignalsFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(setSignalsFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block2 = track2.getLayoutBlock();
        if (block2 == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy2 = block2.getOccupancySensor();
        if (occupancy2 == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block2.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        signalHeadName = throatContinuingSignalHeadComboBox.getDisplayName();
        SignalHead nextHead2 = getNextSignalFromObject(track2,
                layoutTurnout, signalHeadName, setSignalsFrame);
        if ((nextHead2 == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{block2.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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
    }   // setLogicThroatContinuing

    private void setLogicThroatDiverging() {
        TrackSegment track = null;
        if (layoutTurnout.getContinuingSense() == Turnout.CLOSED) {
            track = (TrackSegment) layoutTurnout.getConnectC();
        } else {
            track = (TrackSegment) layoutTurnout.getConnectB();
        }
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = throatDivergingSignalHeadComboBox.getDisplayName();
        SignalHead nextHead = getNextSignalFromObject(track,
                layoutTurnout, signalHeadName, setSignalsFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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
    }   // setLogicThroatDiverging

    private void setLogicContinuing() {
        TrackSegment track = (TrackSegment) layoutTurnout.getConnectA();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = continuingSignalHeadComboBox.getDisplayName();
        SignalHead nextHead = getNextSignalFromObject(track,
                layoutTurnout, signalHeadName, setSignalsFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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
    }   // setLogicContinuing

    private void setLogicDiverging() {
        TrackSegment track = (TrackSegment) layoutTurnout.getConnectA();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = divergingSignalHeadComboBox.getDisplayName();
        SignalHead nextHead = getNextSignalFromObject(track,
                layoutTurnout, signalHeadName, setSignalsFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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
    }   // setLogicDiverging

    /**
     * Utility routines used by multiple tools
     */
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
            @Nullable JFrame theFrame) {
        for (LayoutTurnout t : layoutEditor.getLayoutTurnouts()) {
            if (t.getTurnout() == turnout) {
                // have the layout turnout corresponding to the turnout
                if ((t.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)
                        && (!requireDoubleXover)) {
                    JOptionPane.showMessageDialog(theFrame,
                            Bundle.getMessage("InfoMessage1"), "",
                            JOptionPane.INFORMATION_MESSAGE);
                    return null;
                }
                if (requireDoubleXover && (t.getTurnoutType() != LayoutTurnout.DOUBLE_XOVER)) {
                    JOptionPane.showMessageDialog(theFrame,
                            Bundle.getMessage("InfoMessage8"), "",
                            JOptionPane.INFORMATION_MESSAGE);
                    return null;
                }
                return t;
            }
        }
        // layout turnout not found
        JOptionPane.showMessageDialog(theFrame,
                MessageFormat.format(Bundle.getMessage("SignalsError3"),
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
            @Nonnull JmriBeanComboBox signalNameComboBox,
            boolean requireEntry,
            @Nonnull JmriJFrame frame) {
        String signalName = signalNameComboBox.getDisplayName();
        SignalHead result = getSignalHeadFromEntry(signalName, requireEntry, frame);
        if (result != null) {
            String uname = result.getUserName();
            if ((uname == null) || uname.isEmpty() || !uname.equals(signalName)) {
                signalName = signalName.toUpperCase();
                signalNameComboBox.setText(signalName);
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
                signalName = signalName.toUpperCase();
                signalNameTextField.setText(signalName);
            }
        }
        return result;
    }

    @CheckReturnValue
    public SignalHead getSignalHeadFromEntry(@Nullable String signalName,
            boolean requireEntry, @Nonnull JmriJFrame frame) {
        if ((signalName == null) || signalName.isEmpty()) {
            if (requireEntry) {
                JOptionPane.showMessageDialog(frame, Bundle.getMessage("SignalsError5"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }
        SignalHead head = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
        if (head == null) {
            JOptionPane.showMessageDialog(frame,
                    MessageFormat.format(Bundle.getMessage("SignalsError4"),
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
    public SignalHead getHeadFromName(@Nullable String str) {
        SignalHead result = null;
        if ((str != null) && !str.isEmpty()) {
            result = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(str);
        }
        return result;
    }

    /**
     * Places a signal head icon on the panel after rotation at the designated
     * place, with all icons taken care of.
     */
    public void setSignalHeadOnPanel(int rotation,
            @Nullable String signalHeadName,
            int xLoc, int yLoc) {
        SignalHeadIcon l = new SignalHeadIcon(layoutEditor);
        l.setSignalHead(signalHeadName);
        l.setIcon(Bundle.getMessage("SignalHeadStateRed"), signalIconEditor.getIcon(0));
        l.setIcon(Bundle.getMessage("SignalHeadStateFlashingRed"), signalIconEditor.getIcon(1));
        l.setIcon(Bundle.getMessage("SignalHeadStateYellow"), signalIconEditor.getIcon(2));
        l.setIcon(Bundle.getMessage("SignalHeadStateFlashingYellow"), signalIconEditor.getIcon(3));
        l.setIcon(Bundle.getMessage("SignalHeadStateGreen"), signalIconEditor.getIcon(4));
        l.setIcon(Bundle.getMessage("SignalHeadStateFlashingGreen"), signalIconEditor.getIcon(5));
        l.setIcon(Bundle.getMessage("SignalHeadStateDark"), signalIconEditor.getIcon(6));
        l.setIcon(Bundle.getMessage("SignalHeadStateHeld"), signalIconEditor.getIcon(7));
        l.setIcon(Bundle.getMessage("SignalHeadStateLunar"), signalIconEditor.getIcon(8));
        l.setIcon(Bundle.getMessage("SignalHeadStateFlashingLunar"), signalIconEditor.getIcon(9));
        l.setLocation(xLoc, yLoc);
        if (rotation > 0) {
            Iterator<String> e = l.getIconStateNames();
            while (e.hasNext()) {
                l.getIcon(e.next()).setRotation(rotation, l);
            }
        }
        layoutEditor.putSignal(l);
    }

    /**
     * Places a signal head icon on the panel after rotation at the designated
     * place, with all icons taken care of.
     */
    public void setSignalHeadOnPanel(double directionDEG, @Nonnull String signalHeadName, @Nonnull Point2D where) {
        setSignalHeadOnPanel(directionDEG, signalHeadName, (int) where.getX(), (int) where.getY());
    }

    public void setSignalHeadOnPanel(double directionDEG, @Nonnull String signalHeadName, int xLoc, int yLoc) {
        SignalHeadIcon l = new SignalHeadIcon(layoutEditor);

        l.setSignalHead(signalHeadName);

        l.setIcon(Bundle.getMessage("SignalHeadStateRed"), signalIconEditor.getIcon(0));
        l.setIcon(Bundle.getMessage("SignalHeadStateFlashingRed"), signalIconEditor.getIcon(1));
        l.setIcon(Bundle.getMessage("SignalHeadStateYellow"), signalIconEditor.getIcon(2));
        l.setIcon(Bundle.getMessage("SignalHeadStateFlashingYellow"), signalIconEditor.getIcon(3));
        l.setIcon(Bundle.getMessage("SignalHeadStateGreen"), signalIconEditor.getIcon(4));
        l.setIcon(Bundle.getMessage("SignalHeadStateFlashingGreen"), signalIconEditor.getIcon(5));
        l.setIcon(Bundle.getMessage("SignalHeadStateDark"), signalIconEditor.getIcon(6));
        l.setIcon(Bundle.getMessage("SignalHeadStateHeld"), signalIconEditor.getIcon(7));
        l.setIcon(Bundle.getMessage("SignalHeadStateLunar"), signalIconEditor.getIcon(8));
        l.setIcon(Bundle.getMessage("SignalHeadStateFlashingLunar"), signalIconEditor.getIcon(9));

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
        String sysName = head.getSystemName();
        String uName = head.getUserName();
        String name = lTurnout.getSignalA1Name();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return A1;
        }
        name = lTurnout.getSignalA2Name();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return A2;
        }
        name = lTurnout.getSignalA3Name();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return A3;
        }
        name = lTurnout.getSignalB1Name();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return B1;
        }
        name = lTurnout.getSignalB2Name();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return B2;
        }
        name = lTurnout.getSignalC1Name();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return C1;
        }
        name = lTurnout.getSignalC2Name();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return C2;
        }
        name = lTurnout.getSignalD1Name();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return D1;
        }
        name = lTurnout.getSignalD2Name();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return D2;
        }
        return NONE;
    }   // isHeadAssignedHere

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
            if ((to.getSignalA1Name().equals(sName) || ((uName != null)
                    && to.getSignalA1Name().equals(uName)))) {
                return true;
            }
            if ((to.getSignalA2Name().equals(sName) || ((uName != null)
                    && to.getSignalA2Name().equals(uName)))) {
                return true;
            }
            if ((to.getSignalA3Name().equals(sName) || ((uName != null)
                    && to.getSignalA3Name().equals(uName)))) {
                return true;
            }
            if ((to.getSignalB1Name().equals(sName) || ((uName != null)
                    && to.getSignalB1Name().equals(uName)))) {
                return true;
            }
            if ((to.getSignalB2Name().equals(sName) || ((uName != null)
                    && to.getSignalB2Name().equals(uName)))) {
                return true;
            }
            if ((to.getSignalC1Name().equals(sName) || ((uName != null)
                    && to.getSignalC1Name().equals(uName)))) {
                return true;
            }
            if ((to.getSignalC2Name().equals(sName) || ((uName != null)
                    && to.getSignalC2Name().equals(uName)))) {
                return true;
            }
            if ((to.getSignalD1Name().equals(sName) || ((uName != null)
                    && to.getSignalD1Name().equals(uName)))) {
                return true;
            }
            if ((to.getSignalD2Name().equals(sName) || ((uName != null)
                    && to.getSignalD2Name().equals(uName)))) {
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
    }   // isHeadAssignedAnywhere

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
    }   // removeAssignment

    /**
     * Removes the SignalHead with the specified name from the panel and from
     * assignment to any turnout, positionable point, or level crossing
     */
    public void removeSignalHeadFromPanel(@Nullable String signalName) {
        if ((signalName == null) || signalName.isEmpty()) {
            return;
        }
        SignalHead head = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
        if (head != null) {
            removeAssignment(head);
            layoutEditor.removeSignalHead(head);
            //TODO: Dead-code strip this?
//            if (false) {
//                SignalHeadIcon h = null;
//                int index = -1;
//                for (int i = 0; (i < layoutEditor.signalList.size()) && (index == -1); i++) {
//                    h = layoutEditor.signalList.get(i);
//                    if (h.getSignalHead() == head) {
//                        index = i;
//                    }
//                }
//                if (index != (-1)) {
//                    layoutEditor.signalList.remove(index);
//                    h.remove();
//                    h.dispose();
//                    needRedraw = true;
//                }
//            }
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
        //TODO: Findbugs says this test isn't necessary - dead code strip
//        if (logic == null) {
//            log.error("Trouble creating BlockBossLogic for '" + signalHeadName + "'.");
//            return false;
//        }
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
                    // p is a block boundary - should be signalled
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
                // Reached turnout throat, should be signalled
                LayoutTurnout to = (LayoutTurnout) connect;
                String signalName = to.getSignalA2Name();
                if (!signalName.isEmpty()) {
                    auxSignal = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
                signalName = to.getSignalA1Name();
                if (signalName.isEmpty()) {
                    if (!layoutEditor.skipIncludedTurnout) {
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
                // Reached turnout continuing, should be signalled
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
                    if (!layoutEditor.skipIncludedTurnout) {
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
                // Reached turnout diverging, should be signalled
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
                if ((signalName == null) || signalName.isEmpty()) {
                    if (!layoutEditor.skipIncludedTurnout) {
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
                // Reached turnout xover 4, should be signalled
                LayoutTurnout to = (LayoutTurnout) connect;
                String signalName = to.getSignalD2Name();
                if (!signalName.isEmpty()) {
                    auxSignal = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(signalName);
                }
                signalName = to.getSignalD1Name();
                if (signalName.isEmpty()) {
                    if (!layoutEditor.skipIncludedTurnout) {
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
                // Reached level crossing that may or may not be a block boundary
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
                // Reached level crossing that may or may not be a block boundary
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
                // Reached level crossing that may or may not be a block boundary
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
                // Reached level crossing that may or may not be a block boundary
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
                    if (!layoutEditor.skipIncludedTurnout) {
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
                    if (!layoutEditor.skipIncludedTurnout) {
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
                    if (!layoutEditor.skipIncludedTurnout) {
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
                    if (!layoutEditor.skipIncludedTurnout) {
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
    }   // getNextSignalFromObject

    private boolean hitEndBumper = false;

    private void warnOfSkippedTurnout(
            @Nonnull JFrame frame,
            @Nonnull String turnoutName,
            @Nonnull String signalHeadName) {
        JOptionPane.showMessageDialog(frame,
                MessageFormat.format(Bundle.getMessage("SignalsWarn2"),
                        new Object[]{turnoutName, signalHeadName}),
                null, JOptionPane.WARNING_MESSAGE);
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
            // track is primarily horizontal
            if (delX > 0.0) {
                return false;
            } else {
                return true;
            }
        } else if (Math.abs(delY) > 2.0 * Math.abs(delX)) {
            // track is primarily vertical
            if (delY > 0.0) {
                return false;
            } else {
                return true;
            }
        }
        // track is not primarily horizontal or vertical; assume horizontal
        //	log.error ("Track is not vertical or horizontal at anchor");
        if (delX > 0.0) {
            return false;
        }
        return true;
    }   // isAtWestEndOfAnchor

    /**
     * Tool to set signals at a block boundary, including placing the signal
     * icons and setup of Simple Signal Logic for each signal head
     * <P>
     * Block boundary must be at an Anchor Point on the LayoutEditor panel.
     */
    // operational variables for Set Signals at Block Boundary tool
    private JmriJFrame setSignalsAtBoundaryFrame = null;
    private boolean setSignalsAtBoundaryOpen = false;
    private JmriBeanComboBox block1IDComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox block2IDComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JmriBeanComboBox eastBoundSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox westBoundSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JCheckBox setEastBound = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupLogicEastBound = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setWestBound = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupLogicWestBound = new JCheckBox(Bundle.getMessage("SetLogic"));

    private JButton getAnchorSavedSignalHeads = null;
    private JButton changeSignalAtBoundaryIcon = null;
    private JButton setSignalsAtBoundaryDone = null;
    private JButton setSignalsAtBoundaryCancel = null;

    private LayoutBlock block1 = null;
    private LayoutBlock block2 = null;

    private TrackSegment eastTrack = null;
    private TrackSegment westTrack = null;

    private boolean boundaryFromMenu = false;

    private PositionablePoint boundary = null;
    private SignalHead eastBoundHead = null;
    private SignalHead westBoundHead = null;

    private boolean showWest = true;
    private boolean showEast = true;

    // display dialog for Set Signals at Block Boundary tool
    public void setSignalsAtBlockBoundaryFromMenu(PositionablePoint p, MultiIconEditor theEditor,
            JFrame theFrame) {
        boundaryFromMenu = true;
        boundary = p;

        block1IDComboBox.setText(boundary.getConnect1().getLayoutBlock().getId());
        block2IDComboBox.setText(boundary.getConnect2().getLayoutBlock().getId());

        setSignalsAtBlockBoundary(theEditor, theFrame);
        return;
    }

    public void setSignalsAtBlockBoundary(MultiIconEditor theEditor, JFrame theFrame) {
        signalIconEditor = theEditor;
        signalFrame = theFrame;
        if (setSignalsAtBoundaryOpen) {
            setSignalsAtBoundaryFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (setSignalsAtBoundaryFrame == null) {
            setSignalsAtBoundaryFrame = new JmriJFrame(Bundle.getMessage("SignalsAtBoundary"), false, true);
            setSignalsAtBoundaryFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtBoundary", true);
            setSignalsAtBoundaryFrame.setLocation(70, 30);
            Container theContentPane = setSignalsAtBoundaryFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel11 = new JPanel(new FlowLayout());
            if (boundaryFromMenu) {
                JLabel block1NameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 1 "
                        + Bundle.getMessage("Name") + " : " + boundary.getConnect1().getLayoutBlock().getId());
                panel11.add(block1NameLabel);
            } else {
                JLabel block1NameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 1 "
                        + Bundle.getMessage("Name") + " : ");
                panel11.add(block1NameLabel);
                panel11.add(block1IDComboBox);
                block1IDComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            }
            theContentPane.add(panel11);

            JPanel panel12 = new JPanel(new FlowLayout());
            if (boundaryFromMenu) {
                if (boundary.getConnect2() != null) {
                    JLabel block2NameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 2 "
                            + Bundle.getMessage("Name") + " : " + boundary.getConnect2().getLayoutBlock().getId());
                    panel12.add(block2NameLabel);
                }
            } else {
                JLabel block2NameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 2 "
                        + Bundle.getMessage("Name") + " : ");
                panel12.add(block2NameLabel);
                panel12.add(block2IDComboBox);
                block2IDComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            }
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
            panel6.add(setSignalsAtBoundaryDone = new JButton(Bundle.getMessage("ButtonDone")));
            setSignalsAtBoundaryDone.addActionListener((ActionEvent e) -> {
                setSignalsAtBoundaryDonePressed(e);
            });
            setSignalsAtBoundaryDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(setSignalsAtBoundaryDone);
                rootPane.setDefaultButton(setSignalsAtBoundaryDone);
            });

            panel6.add(setSignalsAtBoundaryCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setSignalsAtBoundaryCancel.addActionListener((ActionEvent e) -> {
                setSignalsAtBoundaryCancelPressed(e);
            });
            setSignalsAtBoundaryCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6);
            setSignalsAtBoundaryFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSignalsAtBoundaryCancelPressed(null);
                }
            });
            if (boundaryFromMenu) {
                getSavedAnchorSignals(null);
            }
        }
        setSignalsAtBoundaryFrame.pack();
        setSignalsAtBoundaryFrame.setVisible(true);
        setSignalsAtBoundaryOpen = true;
    }   // setSignalsAtBlockBoundary

    private void getSavedAnchorSignals(ActionEvent a) {
        if (!getBlockInformation()) {
            return;
        }
        eastBoundSignalHeadComboBox.setText(boundary.getEastBoundSignal());
        westBoundSignalHeadComboBox.setText(boundary.getWestBoundSignal());
    }

    private void setSignalsAtBoundaryCancelPressed(ActionEvent a) {
        setSignalsAtBoundaryOpen = false;
        boundaryFromMenu = false;
        setSignalsAtBoundaryFrame.setVisible(false);
    }

    private void setSignalsAtBoundaryDonePressed(ActionEvent a) {
        if (!getBlockInformation()) {
            return;
        }
        eastBoundHead = getSignalHeadFromEntry(eastBoundSignalHeadComboBox, false, setSignalsAtBoundaryFrame);
        westBoundHead = getSignalHeadFromEntry(westBoundSignalHeadComboBox, false, setSignalsAtBoundaryFrame);
        if ((eastBoundHead == null) && (westBoundHead == null)) {
            JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
                    Bundle.getMessage("SignalsError12"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        // place or update signals as requested
        String newEastBoundSignalName = eastBoundSignalHeadComboBox.getDisplayName();
        if ((eastBoundHead != null) && setEastBound.isSelected()) {
            if (isHeadOnPanel(eastBoundHead)
                    && (eastBoundHead != getHeadFromName(boundary.getEastBoundSignal()))) {
                JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{newEastBoundSignalName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError13"),
                                new Object[]{newEastBoundSignalName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(boundary.getEastBoundSignal());
                removeAssignment(eastBoundHead);
                boundary.setEastBoundSignal(newEastBoundSignalName);
            }
            //} else if ((eastBoundHead != null)
            //                && (eastBoundHead == getHeadFromName(boundary.getWestBoundSignal()))) {
            // need to figure out what to do in this case.
        }
        String newWestBoundSignalName = westBoundSignalHeadComboBox.getDisplayName();
        if ((westBoundHead != null) && setWestBound.isSelected()) {
            if (isHeadOnPanel(westBoundHead)
                    && (westBoundHead != getHeadFromName(boundary.getWestBoundSignal()))) {
                JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{newWestBoundSignalName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError13"),
                                new Object[]{newWestBoundSignalName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalHeadFromPanel(boundary.getWestBoundSignal());
                removeAssignment(westBoundHead);
                boundary.setWestBoundSignal(newWestBoundSignalName);
            }
            //} else if ((westBoundHead != null)
            //        && (westBoundHead == getHeadFromName(boundary.getEastBoundSignal()))) {
            // need to figure out what to do in this case.
        }
        if ((eastBoundHead != null) && setupLogicEastBound.isSelected()) {
            setLogicEastBound();
        }
        if ((westBoundHead != null) && setupLogicWestBound.isSelected()) {
            setLogicWestBound();
        }
        setSignalsAtBoundaryOpen = false;
        boundaryFromMenu = false;
        setSignalsAtBoundaryFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }   // setSignalsAtBoundaryDonePressed

    /*
     * Do some thing here for end bumpers.
     */
    private boolean getBlockInformation() {
        //might have to do something to trick it with an end bumper
        if (!boundaryFromMenu) {
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
                if (p.getType() == PositionablePoint.ANCHOR) {
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
                JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
                        Bundle.getMessage("SignalsError7"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        // set track orientation at boundary
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
                eastTrack = track1;	 // south
                westTrack = track2;	 // north
            } else {
                eastTrack = track2;	 // south
                westTrack = track1;	 // north
            }
        }
        return true;
    }   // getBlockInformation

    @CheckReturnValue
    private LayoutBlock getBlockFromEntry(@Nonnull JmriBeanComboBox blockNameComboBox) {
        return getBlockFromEntry(blockNameComboBox.getDisplayName());
    }

    //TODO: Unused - dead code strip
//    @CheckReturnValue
//    private LayoutBlock getBlockFromEntry(@Nonnull JTextField blockNameTextField) {
//        String theBlockName = NamedBean.normalizeUserName(blockNameTextField.getText());
//        return getBlockFromEntry(theBlockName);
//    }
    @CheckReturnValue
    private LayoutBlock getBlockFromEntry(@Nullable String theBlockName) {
        if ((theBlockName == null) || theBlockName.isEmpty()) {
            JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame, Bundle.getMessage("SignalsError9"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return null;
        }
        LayoutBlock block = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName(theBlockName);
        if (block == null) {
            block = InstanceManager.getDefault(LayoutBlockManager.class).getBySystemName(theBlockName);
            if (block == null) {
                JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError10"),
                                new Object[]{theBlockName}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        if (!block.isOnPanel(layoutEditor)) {
            JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
                    MessageFormat.format(Bundle.getMessage("SignalsError11"),
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
        String signalHeadName = eastBoundSignalHeadComboBox.getDisplayName();
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
        String signalHeadName = westBoundSignalHeadComboBox.getDisplayName();
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
            JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{eastBlock.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PositionablePoint p = boundary;
        if (boundary.getType() == PositionablePoint.EDGE_CONNECTOR && eastTrack != boundary.getConnect1()) {
            p = boundary.getLinkedPoint();
        }
        String newEastBoundSignalName = eastBoundSignalHeadComboBox.getDisplayName();
        SignalHead nextHead = getNextSignalFromObject(eastTrack,
                p, newEastBoundSignalName, setSignalsAtBoundaryFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{eastBlock.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{westBlock.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PositionablePoint p = boundary;
        if (boundary.getType() == PositionablePoint.EDGE_CONNECTOR && westTrack != boundary.getConnect1()) {
            p = boundary.getLinkedPoint();
        }
        String newWestBoundSignalName = westBoundSignalHeadComboBox.getDisplayName();
        SignalHead nextHead = getNextSignalFromObject(westTrack,
                p, newWestBoundSignalName, setSignalsAtBoundaryFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{westBlock.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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

    public void setSignalAtEdgeConnector(@Nonnull PositionablePoint p,
            @Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        boundary = p;
        if (p.getLinkedPoint() == null || p.getLinkedPoint().getConnect1() == null) {
            if (p.getConnect1Dir() == Path.EAST || p.getConnect1Dir() == Path.SOUTH) {
                showWest = false;
            } else {
                showEast = false;
            }
            block1IDComboBox.setText(boundary.getConnect1().getLayoutBlock().getId());
        } else {
            block1IDComboBox.setText(boundary.getConnect1().getLayoutBlock().getId());
            block2IDComboBox.setText(boundary.getConnect2().getLayoutBlock().getId());
        }
        boundaryFromMenu = true;

        setSignalsAtBlockBoundary(theEditor, theFrame);
        return;
    }

    /**
     * Tool to set signals at a double crossover turnout, including placing the
     * signal icons and setup of Simple Signal Logic for each signal head
     * <P>
     * This tool assumes left facing signal head icons have been selected, and
     * will rotate the signal head icons accordingly.
     * <P>
     * This tool will place icons on the outside edge of the turnout.
     * <P>
     * At least one signal at each of the four connection points is required. A
     * second signal at each is optional.
     */
    // operational variables for Set Signals at Double Crossover Turnout tool
    private JmriJFrame setSignalsAtXoverFrame = null;
    private boolean setSignalsAtXoverOpen = false;

    private JmriBeanComboBox a1ComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox a2ComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox b1ComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox b2ComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox c1ComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox c2ComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox d1ComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox d2ComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JCheckBox setA1Head = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setA2Head = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setB1Head = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setB2Head = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setC1Head = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setC2Head = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setD1Head = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setD2Head = new JCheckBox(Bundle.getMessage("PlaceHead"));

    private JCheckBox setupA1Logic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupA2Logic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupB1Logic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupB2Logic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupC1Logic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupC2Logic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupD1Logic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupD2Logic = new JCheckBox(Bundle.getMessage("SetLogic"));

    private JButton getSavedXoverSignalHeads = null;
    private JButton changeXoverSignalIcon = null;
    private JButton setXoverSignalsDone = null;
    private JButton setXoverSignalsCancel = null;

    private boolean xoverFromMenu = false;

    private SignalHead a1Head = null;
    private SignalHead a2Head = null;
    private SignalHead b1Head = null;
    private SignalHead b2Head = null;
    private SignalHead c1Head = null;
    private SignalHead c2Head = null;
    private SignalHead d1Head = null;
    private SignalHead d2Head = null;

    private int xoverType = LayoutTurnout.DOUBLE_XOVER;	 // changes to RH_XOVER or LH_XOVER as required
    private String xoverTurnoutName = "";
    private JLabel xoverTurnoutNameLabel = new JLabel("");

    // display dialog for Set Signals at Crossover Turnout tool
    public void setSignalsAtXoverTurnoutFromMenu(@Nonnull LayoutTurnout to,
            @Nonnull MultiIconEditor theEditor, @Nonnull JFrame theFrame) {
        xoverFromMenu = true;
        layoutTurnout = to;
        turnout = to.getTurnout();
        xoverType = layoutTurnout.getTurnoutType();
        if ((xoverType != LayoutTurnout.DOUBLE_XOVER) && (xoverType != LayoutTurnout.RH_XOVER)
                && (xoverType != LayoutTurnout.LH_XOVER)) {
            log.error("entered Set Signals at XOver, with a non-crossover turnout");
            return;
        }
        xoverTurnoutName = layoutTurnout.getTurnoutName();
        setSignalsAtXoverTurnout(theEditor, theFrame);
    }

    public void setSignalsAtXoverTurnout(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        signalIconEditor = theEditor;
        signalFrame = theFrame;
        if (!xoverFromMenu) {
            //TODO: convert to use turnout ComboBox
            xoverTurnoutName = JOptionPane.showInputDialog(layoutEditor,
                    Bundle.getMessage("MakeLabel", Bundle.getMessage("EnterXOverTurnout")));
            if (xoverTurnoutName.length() < 3) {
                return;	 // cancelled
            }
        }
        if (!getTurnoutInformation(true)) {
            return;
        }

        xoverTurnoutNameLabel.setText(Bundle.getMessage("BeanNameTurnout") + " "
                + Bundle.getMessage("Name") + " : " + xoverTurnoutName);
        xoverType = layoutTurnout.getTurnoutType();

        if (setSignalsAtXoverOpen) {
            setSignalsAtXoverFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (setSignalsAtXoverFrame == null) {
            setSignalsAtXoverFrame = new JmriJFrame(Bundle.getMessage("SignalsAtXoverTurnout"), false, true);
            setSignalsAtXoverFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtXoverTurnout", true);
            setSignalsAtXoverFrame.setLocation(70, 30);
            Container theContentPane = setSignalsAtXoverFrame.getContentPane();
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
                // (de)select all checkboxes
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
                // (de)select all checkboxes
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
            JLabel a1Label = new JLabel(Bundle.getMessage("XContinuing", "A") + " : ");
            panel21.add(a1Label);
            panel21.add(a1ComboBox);
            theContentPane.add(panel21);
            a1ComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

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
                JLabel a2Label = new JLabel(Bundle.getMessage("XDiverging", "A") + " : ");
                panel23.add(a2Label);
                panel23.add(a2ComboBox);
                theContentPane.add(panel23);
                a2ComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));
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
            JLabel b1Label = new JLabel(Bundle.getMessage("XContinuing", "B") + " : ");
            panel31.add(b1Label);
            panel31.add(b1ComboBox);
            theContentPane.add(panel31);
            b1ComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

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
                JLabel b2Label = new JLabel(Bundle.getMessage("XDiverging", "B") + " : ");
                panel33.add(b2Label);
                panel33.add(b2ComboBox);
                theContentPane.add(panel33);
                b2ComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));
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
            JLabel c1Label = new JLabel(Bundle.getMessage("XContinuing", "C") + " : ");
            panel41.add(c1Label);
            panel41.add(c1ComboBox);
            theContentPane.add(panel41);
            c1ComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

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
                JLabel c2Label = new JLabel(Bundle.getMessage("XDiverging", "C") + " : ");
                panel43.add(c2Label);
                panel43.add(c2ComboBox);
                theContentPane.add(panel43);
                c2ComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));
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
            JLabel d1Label = new JLabel(Bundle.getMessage("XContinuing", "D") + " : ");
            panel51.add(d1Label);
            panel51.add(d1ComboBox);
            theContentPane.add(panel51);
            d1ComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));

            JPanel panel52 = new JPanel(new FlowLayout());
            panel52.add(new JLabel("   "));
            panel52.add(setD1Head);
            setD1Head.setToolTipText(Bundle.getMessage("PlaceHeadHint"));
            panel52.add(new JLabel("  "));
            panel52.add(setupD1Logic);
            setupD1Logic.setToolTipText(Bundle.getMessage("SetLogicHint"));
            theContentPane.add(panel52);
            if (!(xoverType == LayoutTurnout.RH_XOVER)) {
                JPanel panel53 = new JPanel(new FlowLayout());
                JLabel d2Label = new JLabel(Bundle.getMessage("XDiverging", "D") + " : ");
                panel53.add(d2Label);
                panel53.add(d2ComboBox);
                theContentPane.add(panel53);
                d2ComboBox.setToolTipText(Bundle.getMessage("SignalHeadNameHint"));
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

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
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
            setSignalsAtXoverFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setXoverSignalsCancelPressed(null);
                }
            });
            if (xoverFromMenu) {
                xoverTurnoutSignalsGetSaved(null);
            }
        }
        setSignalsAtXoverFrame.pack();
        setSignalsAtXoverFrame.setVisible(true);
        setSignalsAtXoverOpen = true;
    }   // setSignalsAtXoverTurnout

    private void xoverTurnoutSignalsGetSaved(ActionEvent a) {
        a1ComboBox.setText(layoutTurnout.getSignalA1Name());
        a2ComboBox.setText(layoutTurnout.getSignalA2Name());
        b1ComboBox.setText(layoutTurnout.getSignalB1Name());
        b2ComboBox.setText(layoutTurnout.getSignalB2Name());
        c1ComboBox.setText(layoutTurnout.getSignalC1Name());
        c2ComboBox.setText(layoutTurnout.getSignalC2Name());
        d1ComboBox.setText(layoutTurnout.getSignalD1Name());
        d2ComboBox.setText(layoutTurnout.getSignalD2Name());
    }

    private void setXoverSignalsCancelPressed(ActionEvent a) {
        setSignalsAtXoverOpen = false;
        xoverFromMenu = false;
        setSignalsAtXoverFrame.setVisible(false);
    }

    private void setXoverSignalsDonePressed(ActionEvent a) {
        if (!getXoverSignalHeadInformation()) {
            return;
        }
        // place signal icons if requested, and assign signal heads to this turnout
        String signalHeadName = a1ComboBox.getDisplayName();
        if (setA1Head.isSelected()) {
            if (isHeadOnPanel(a1Head)
                    && (a1Head != getHeadFromName(layoutTurnout.getSignalA1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(a1Head)
                        && isHeadAssignedAnywhere(a1Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalA1Name());
                    removeAssignment(a1Head);
                    layoutTurnout.setSignalA1Name(signalHeadName);
                }
                //} else if (assigned != A1) {
                // need to figure out what to do in this case.
            }
        }
        signalHeadName = a2ComboBox.getDisplayName();
        if ((a2Head != null) && setA2Head.isSelected()) {
            if (isHeadOnPanel(a2Head)
                    && (a2Head != getHeadFromName(layoutTurnout.getSignalA2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(a2Head)
                        && isHeadAssignedAnywhere(a2Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
                    removeAssignment(a2Head);
                    layoutTurnout.setSignalA2Name(signalHeadName);
                }
                //} else if (assigned != A2) {
                // need to figure out what to do in this case.
            }
        } else { // a2Head known to be null here
            removeSignalHeadFromPanel(layoutTurnout.getSignalA2Name());
            layoutTurnout.setSignalA2Name("");
        }
        signalHeadName = b1ComboBox.getDisplayName();
        if (setB1Head.isSelected()) {
            if (isHeadOnPanel(b1Head)
                    && (b1Head != getHeadFromName(layoutTurnout.getSignalB1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(b1Head)
                        && isHeadAssignedAnywhere(b1Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalB1Name());
                    removeAssignment(b1Head);
                    layoutTurnout.setSignalB1Name(signalHeadName);
                }
                //} else if (assigned != B1) {
                // need to figure out what to do in this case.
            }
        }
        signalHeadName = b2ComboBox.getDisplayName();
        if ((b2Head != null) && setB2Head.isSelected()) {
            if (isHeadOnPanel(b2Head)
                    && (b2Head != getHeadFromName(layoutTurnout.getSignalB2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(b2Head)
                        && isHeadAssignedAnywhere(b2Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalB2Name());
                    removeAssignment(b2Head);
                    layoutTurnout.setSignalB2Name(signalHeadName);
                }
                //} else if (assigned != B2) {
                // need to figure out what to do in this case.
            }
        } else { // b2Head known to be null here
            removeSignalHeadFromPanel(layoutTurnout.getSignalB2Name());
            layoutTurnout.setSignalB2Name("");
        }
        signalHeadName = c1ComboBox.getDisplayName();
        if (setC1Head.isSelected()) {
            if (isHeadOnPanel(c1Head)
                    && (c1Head != getHeadFromName(layoutTurnout.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(c1Head)
                        && isHeadAssignedAnywhere(c1Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalC1Name());
                    removeAssignment(c1Head);
                    layoutTurnout.setSignalC1Name(signalHeadName);
                }
                //} else if (assigned != C1) {
                // need to figure out what to do in this case.
            }
        }
        signalHeadName = c2ComboBox.getDisplayName();
        if ((c2Head != null) && setC2Head.isSelected()) {
            if (isHeadOnPanel(c2Head)
                    && (c2Head != getHeadFromName(layoutTurnout.getSignalC2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(c2Head)
                        && isHeadAssignedAnywhere(c2Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalC2Name());
                    removeAssignment(c2Head);
                    layoutTurnout.setSignalC2Name(signalHeadName);
                }
                //} else if (assigned != C2) {
                // need to figure out what to do in this case.
            }
        } else { // c2Head known to be null here
            removeSignalHeadFromPanel(layoutTurnout.getSignalC2Name());
            layoutTurnout.setSignalC2Name("");
        }
        signalHeadName = d1ComboBox.getDisplayName();
        if (setD1Head.isSelected()) {
            if (isHeadOnPanel(d1Head)
                    && (d1Head != getHeadFromName(layoutTurnout.getSignalD1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(d1Head)
                        && isHeadAssignedAnywhere(d1Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalD1Name());
                    removeAssignment(d1Head);
                    layoutTurnout.setSignalD1Name(signalHeadName);
                }
                //} else if (assigned != D1) {
                // need to figure out what to do in this case.
            }
        }
        signalHeadName = d2ComboBox.getDisplayName();
        if ((d2Head != null) && setD2Head.isSelected()) {
            if (isHeadOnPanel(d2Head)
                    && (d2Head != getHeadFromName(layoutTurnout.getSignalD2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(d2Head)
                        && isHeadAssignedAnywhere(d2Head)) {
                    JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout.getSignalD2Name());
                    removeAssignment(d2Head);
                    layoutTurnout.setSignalD2Name(signalHeadName);
                }
                //} else if (assigned != D2) {
                // need to figure out what to do in this case.
            }
        } else { // d2Head known to be null here
            removeSignalHeadFromPanel(layoutTurnout.getSignalD2Name());
            layoutTurnout.setSignalD2Name("");
        }
        // setup logic if requested
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
        // make sure this layout turnout is not linked to another
        layoutTurnout.setLinkType(LayoutTurnout.NO_LINK);
        layoutTurnout.setLinkedTurnoutName("");
        // finish up
        setSignalsAtXoverOpen = false;
        xoverFromMenu = false;
        setSignalsAtXoverFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }   // setXoverSignalsDonePressed

    private boolean getXoverSignalHeadInformation() {
        a1Head = getSignalHeadFromEntry(a1ComboBox, true, setSignalsAtXoverFrame);
        if (a1Head == null) {
            return false;
        }
        if (!(xoverType == LayoutTurnout.LH_XOVER)) {
            a2Head = getSignalHeadFromEntry(a2ComboBox, false, setSignalsAtXoverFrame);
        } else {
            a2Head = null;
        }
        b1Head = getSignalHeadFromEntry(b1ComboBox, true, setSignalsAtXoverFrame);
        if (b1Head == null) {
            return false;
        }
        if (!(xoverType == LayoutTurnout.RH_XOVER)) {
            b2Head = getSignalHeadFromEntry(b2ComboBox, false, setSignalsAtXoverFrame);
        } else {
            b2Head = null;
        }
        c1Head = getSignalHeadFromEntry(c1ComboBox, true, setSignalsAtXoverFrame);
        if (c1Head == null) {
            return false;
        }
        if (!(xoverType == LayoutTurnout.LH_XOVER)) {
            c2Head = getSignalHeadFromEntry(c2ComboBox, false, setSignalsAtXoverFrame);
        } else {
            c2Head = null;
        }
        d1Head = getSignalHeadFromEntry(d1ComboBox, true, setSignalsAtXoverFrame);
        if (d1Head == null) {
            return false;
        }
        if (!(xoverType == LayoutTurnout.RH_XOVER)) {
            d2Head = getSignalHeadFromEntry(d2ComboBox, false, setSignalsAtXoverFrame);
        } else {
            d2Head = null;
        }
        return true;
    }

    private void placeA1() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = a1ComboBox.getDisplayName();
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
        String signalHeadName = a2ComboBox.getDisplayName();
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
        String signalHeadName = b1ComboBox.getDisplayName();
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
        String signalHeadName = b2ComboBox.getDisplayName();
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
        String signalHeadName = c1ComboBox.getDisplayName();
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
        String signalHeadName = c2ComboBox.getDisplayName();
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
        String signalHeadName = d1ComboBox.getDisplayName();
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
        String signalHeadName = d2ComboBox.getDisplayName();
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
            JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = null;
        SignalHead nextHead = null;
        if ((track != null) && setup1) {
            LayoutBlock block = track.getLayoutBlock();
            if (block == null) {
                JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                        Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            occupancy = block.getOccupancySensor();
            if (occupancy == null) {
                JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                        MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                                new Object[]{block.getUserName()}),
                        null, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            nextHead = getNextSignalFromObject(track,
                    layoutTurnout, head.getSystemName(), setSignalsAtXoverFrame);
            if ((nextHead == null) && (!reachedEndBumper())) {
                JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                        MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                                new Object[]{block.getUserName()}),
                        null, JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block2 = track2.getLayoutBlock();
        if (block2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy2 = block2.getOccupancySensor();
        if (occupancy2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block2.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = head.getSystemName();
        if (secondHead != null) {
            signalHeadName = secondHead.getSystemName();
        }
        SignalHead nextHead2 = getNextSignalFromObject(track2,
                layoutTurnout, signalHeadName, setSignalsAtXoverFrame);
        if ((nextHead2 == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{block2.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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
    }   // setLogicXover

    private void setLogicXoverContinuing(SignalHead head, TrackSegment track) {
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        SignalHead nextHead = getNextSignalFromObject(track,
                layoutTurnout, head.getSystemName(), setSignalsAtXoverFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtXoverFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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
    }   // setLogicXoverContinuing

    /**
     * Tool to set signals at a level crossing, including placing the signal
     * icons and setup of Simple Signal Logic for each signal head
     * <P>
     * This tool assumes left facing signal head icons have been selected, and
     * will rotate the signal head icons accordingly.
     * <P>
     * This tool will place icons on the right side of each track.
     * <P>
     * Both tracks do not need to be signalled. If one signal for a track, A-C
     * or B-D, the other must also be present.
     * <P>
     * Some user adjustment of turnout positions may be needed.
     */
    // operational variables for Set Signals at Level Crossing tool
    private JmriJFrame setSignalsAtXingFrame = null;
    private boolean setSignalsAtXingOpen = false;

    private JmriBeanComboBox blockAComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox blockCComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JmriBeanComboBox aSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox bSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox cSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox dSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JCheckBox setAHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setBHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setCHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setDHead = new JCheckBox(Bundle.getMessage("PlaceHead"));

    private JCheckBox setupALogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupBLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupCLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupDLogic = new JCheckBox(Bundle.getMessage("SetLogic"));

    private JButton getSavedXingSignalHeads = null;
    private JButton changeXingSignalIcon = null;
    private JButton setXingSignalsDone = null;
    private JButton setXingSignalsCancel = null;

    private boolean xingFromMenu = false;

    private LevelXing levelXing = null;

    private SignalHead aHead = null;
    private SignalHead bHead = null;
    private SignalHead cHead = null;
    private SignalHead dHead = null;

    // display dialog for Set Signals at Level Crossing tool
    public void setSignalsAtLevelXingFromMenu(@Nonnull LevelXing xing,
            @Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        xingFromMenu = true;
        levelXing = xing;
        blockAComboBox.setText(levelXing.getBlockNameAC());
        blockCComboBox.setText(levelXing.getBlockNameBD());
        setSignalsAtLevelXing(theEditor, theFrame);
        return;
    }

    public void setSignalsAtLevelXing(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        signalIconEditor = theEditor;
        signalFrame = theFrame;
        if (setSignalsAtXingOpen) {
            setSignalsAtXingFrame.setVisible(true);
            return;
        }
        aSignalHeadComboBox.setText("");
        bSignalHeadComboBox.setText("");
        cSignalHeadComboBox.setText("");
        dSignalHeadComboBox.setText("");
        // Initialize if needed
        if (setSignalsAtXingFrame == null) {
            setSignalsAtXingFrame = new JmriJFrame(Bundle.getMessage("SignalsAtLevelXing"), false, true);
            setSignalsAtXingFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtLevelXing", true);
            setSignalsAtXingFrame.setLocation(70, 30);
            Container theContentPane = setSignalsAtXingFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel11 = new JPanel(new FlowLayout());
            if (xingFromMenu) {
                JLabel blockANameLabel = new JLabel(Bundle.getMessage("MakeLabel", (Bundle.getMessage("BeanNameBlock") + " 1"))
                        + " " + levelXing.getBlockNameAC());

                panel11.add(blockANameLabel);
            } else {
                JLabel blockANameLabel = new JLabel(Bundle.getMessage("MakeLabel", (Bundle.getMessage("BeanNameBlock") + " 1")));
                panel11.add(blockANameLabel);
                panel11.add(blockAComboBox);
                blockAComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            }
            theContentPane.add(panel11);

            JPanel panel12 = new JPanel(new FlowLayout());
            if (xingFromMenu) {
                JLabel blockCNameLabel = new JLabel(Bundle.getMessage("MakeLabel", (Bundle.getMessage("BeanNameBlock") + " 2"))
                        + " " + levelXing.getBlockNameBD());

                panel12.add(blockCNameLabel);
            } else {
                JLabel blockCNameLabel = new JLabel(Bundle.getMessage("MakeLabel", (Bundle.getMessage("BeanNameBlock") + " 2")));
                panel12.add(blockCNameLabel);
                panel12.add(blockCComboBox);
                blockCComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            }
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
                // (de)select all checkboxes
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
                // (de)select all checkboxes
                setupALogic.setSelected(isSelected);
                setupBLogic.setSelected(isSelected);
                setupCLogic.setSelected(isSelected);
                setupDLogic.setSelected(isSelected);
            });
            theContentPane.add(panel2a);

            JPanel panel21 = new JPanel(new FlowLayout());
            JLabel aLabel = new JLabel(Bundle.getMessage("TrackXConnect", "A") + " : ");
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
            JLabel bLabel = new JLabel(Bundle.getMessage("TrackXConnect", "B") + " : ");
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
            JLabel cLabel = new JLabel(Bundle.getMessage("TrackXConnect", "C") + " : ");
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
            JLabel dLabel = new JLabel(Bundle.getMessage("TrackXConnect", "D") + " : ");
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

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
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
            setSignalsAtXingFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setXingSignalsCancelPressed(null);
                }
            });
            if (xingFromMenu) {
                xingSignalsGetSaved(null);
            }
        }
        setSignalsAtXingFrame.pack();
        setSignalsAtXingFrame.setVisible(true);
        setSignalsAtXingOpen = true;
    }   // setSignalsAtLevelXing

    private void xingSignalsGetSaved(ActionEvent a) {
        if (!getLevelCrossingInformation()) {
            return;
        }
        aSignalHeadComboBox.setText(levelXing.getSignalAName());
        bSignalHeadComboBox.setText(levelXing.getSignalBName());
        cSignalHeadComboBox.setText(levelXing.getSignalCName());
        dSignalHeadComboBox.setText(levelXing.getSignalDName());
    }

    private void setXingSignalsCancelPressed(ActionEvent a) {
        setSignalsAtXingOpen = false;
        xingFromMenu = false;
        setSignalsAtXingFrame.setVisible(false);
    }

    private void setXingSignalsDonePressed(ActionEvent a) {
        if (!getLevelCrossingInformation()) {
            return;
        }
        if (!getXingSignalHeadInformation()) {
            return;
        }

        // place or update signals as requested
        String signalName = aSignalHeadComboBox.getDisplayName();
        if ((aHead != null) && setAHead.isSelected()) {
            if (isHeadOnPanel(aHead)
                    && (aHead != getHeadFromName(levelXing.getSignalAName()))) {
                JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError13"),
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (aHead == null) {
            removeSignalHeadFromPanel(levelXing.getSignalAName());
            levelXing.setSignalAName("");
        }
        signalName = bSignalHeadComboBox.getDisplayName();
        if ((bHead != null) && setBHead.isSelected()) {
            if (isHeadOnPanel(bHead)
                    && (bHead != getHeadFromName(levelXing.getSignalBName()))) {
                JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError13"),
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (bHead == null) {
            removeSignalHeadFromPanel(levelXing.getSignalBName());
            levelXing.setSignalBName("");
        }
        signalName = cSignalHeadComboBox.getDisplayName();
        if ((cHead != null) && setCHead.isSelected()) {
            if (isHeadOnPanel(cHead)
                    && (cHead != getHeadFromName(levelXing.getSignalCName()))) {
                JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError13"),
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (cHead == null) {
            removeSignalHeadFromPanel(levelXing.getSignalCName());
            levelXing.setSignalCName("");
        }
        signalName = dSignalHeadComboBox.getDisplayName();
        if ((dHead != null) && setDHead.isSelected()) {
            if (isHeadOnPanel(dHead)
                    && (dHead != getHeadFromName(levelXing.getSignalDName()))) {
                JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError13"),
                                new Object[]{signalName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (dHead == null) {
            removeSignalHeadFromPanel(levelXing.getSignalDName());
            levelXing.setSignalDName("");
        }
        // setup logic if requested
        if (setupALogic.isSelected() && (aHead != null)) {
            setLogicXing(aHead, (TrackSegment) levelXing.getConnectC(),
                    levelXing.getLayoutBlockBD(), (TrackSegment) levelXing.getConnectB(),
                    (TrackSegment) levelXing.getConnectD(), aSignalHeadComboBox.getDisplayName());
        }
        if (setupBLogic.isSelected() && (bHead != null)) {
            setLogicXing(bHead, (TrackSegment) levelXing.getConnectD(),
                    levelXing.getLayoutBlockAC(), (TrackSegment) levelXing.getConnectA(),
                    (TrackSegment) levelXing.getConnectC(), bSignalHeadComboBox.getDisplayName());
        }
        if (setupCLogic.isSelected() && (cHead != null)) {
            setLogicXing(cHead, (TrackSegment) levelXing.getConnectA(),
                    levelXing.getLayoutBlockBD(), (TrackSegment) levelXing.getConnectB(),
                    (TrackSegment) levelXing.getConnectD(), cSignalHeadComboBox.getDisplayName());
        }
        if (setupDLogic.isSelected() && (dHead != null)) {
            setLogicXing(dHead, (TrackSegment) levelXing.getConnectB(),
                    levelXing.getLayoutBlockAC(), (TrackSegment) levelXing.getConnectA(),
                    (TrackSegment) levelXing.getConnectC(), dSignalHeadComboBox.getDisplayName());
        }
        // finish up
        setSignalsAtXingOpen = false;
        xingFromMenu = false;
        setSignalsAtXingFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }   // setXingSignalsDonePressed

    private boolean getLevelCrossingInformation() {
        if (!xingFromMenu) {
            levelXing = null;
            List<LevelXing> levelXings = layoutEditor.getLevelXings();
            if (levelXings.size() <= 0) {
                JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                        Bundle.getMessage("SignalsError15"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            } else if (levelXings.size() == 1) {
                levelXing = levelXings.get(0);
            } else {
                LayoutBlock xingBlockA = null;
                xingBlockA = getBlockFromEntry(blockAComboBox);
                if (xingBlockA == null) {
                    return false;
                }

                LayoutBlock xingBlockC = getBlockFromEntry(blockCComboBox);
                if (xingBlockC == null) {
                    return false;
                }

                int foundCount = 0;
                // make two block tests first
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
                    // try one block test
                    for (LevelXing x : layoutEditor.getLevelXings()) {
                        if ((xingBlockA == x.getLayoutBlockAC()) || (xingBlockA == x.getLayoutBlockBD())) {
                            levelXing = x;
                            foundCount++;
                        }
                    }
                }
                if (foundCount > 1) {
                    JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError16"),
                                    new Object[]{" " + foundCount + " "}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (levelXing == null) {
                    JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                            Bundle.getMessage("SignalsError17"),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }

        Point2D coordsA = levelXing.getCoordsA();
        Point2D coordsC = levelXing.getCoordsC();
        placeSignalDirectionDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsC, coordsA));

        return true;
    }   // getLevelCrossingInformation

    private boolean getXingSignalHeadInformation() {
        // note that all heads are optional, but pairs must be present
        aHead = getSignalHeadFromEntry(aSignalHeadComboBox, false, setSignalsAtXingFrame);
        bHead = getSignalHeadFromEntry(bSignalHeadComboBox, false, setSignalsAtXingFrame);
        cHead = getSignalHeadFromEntry(cSignalHeadComboBox, false, setSignalsAtXingFrame);
        dHead = getSignalHeadFromEntry(dSignalHeadComboBox, false, setSignalsAtXingFrame);
        if (((aHead != null) && (cHead == null)) || ((aHead == null) && (cHead != null))
                || ((bHead != null) && (dHead == null)) || ((bHead == null) && (dHead != null))) {
            JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                    Bundle.getMessage("SignalsError14"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if ((aHead == null) && (bHead == null) && (cHead == null) && (dHead == null)) {
            JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                    Bundle.getMessage("SignalsError12"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void placeXingA() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = aSignalHeadComboBox.getDisplayName();
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
        String signalHeadName = bSignalHeadComboBox.getDisplayName();
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
        String signalHeadName = cSignalHeadComboBox.getDisplayName();
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
        String signalHeadName = dSignalHeadComboBox.getDisplayName();
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
            JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = null;
        Sensor crossOccupancy = null;
        Sensor track1Occupancy = null;
        Sensor track2Occupancy = null;
        SignalHead nextHead = null;
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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
                head.getSystemName(), setSignalsAtXoverFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if ((crossOccupancy == null) && (track1Occupancy == null) && (track2Occupancy == null)) {
            JOptionPane.showMessageDialog(setSignalsAtXingFrame,
                    MessageFormat.format(Bundle.getMessage("SignalsWarn1"),
                            new Object[]{signalHeadName}),
                    null, JOptionPane.WARNING_MESSAGE);
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

    /**
     * Tool to set signals at throat-to-throat turnouts, including placing the
     * signal icons and setup of signal logic for each signal head
     * <P>
     * This tool can only be accessed from the Tools menu. There is no access
     * from a turnout pop-up menu.
     * <P>
     * This tool requires a situation where two turnouts are connected throat-
     * to-throat by a single "short" track segment. The actual length of the
     * track segment is not tested. If this situation is not found, and error
     * message is sent to the user. To get started with this the user needs to
     * enter at least one of the two connected turnouts.
     * <P>
     * This tool assumes two turnouts connected throat-to-throat, as would be
     * used to represent a double slip turnout. The turnouts may be either
     * left-handed, right-handed, wye, or any pair of these. This tool also
     * assumes that there are no signals at the throat junction. The signal
     * heads will be rotated to face outward--away from the throats. Four sets
     * of one or two signal heads will be placed, one at each of the converging
     * and diverging for each turnout.
     * <P>
     * This tool assumes that each of the four tracks is contained in a
     * different block. Things work best if the two throat-to-throat turnouts
     * are in their own separate block, but this is not necessary.
     * <P>
     * This tool will place icons on the outside edges of each turnout.
     * <P>
     * At least one signal at each of the four connection points is required. A
     * second signal at each is optional.
     */
    // operational variables for Set Signals at Double Crossover Turnout tool
    private JmriJFrame setSignalsAtTToTFrame = null;
    private boolean setSignalsAtTToTOpen = false;

    private JmriBeanComboBox turnout1ComboBox = new JmriBeanComboBox(
            InstanceManager.turnoutManagerInstance(),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox turnout2ComboBox = new JmriBeanComboBox(
            InstanceManager.turnoutManagerInstance(),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JmriBeanComboBox a1TToTSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox a2TToTSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox b1TToTSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox b2TToTSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox c1TToTSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox c2TToTSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox d1TToTSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox d2TToTSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JCheckBox setA1TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setA2TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setB1TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setB2TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setC1TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setC2TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setD1TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setD2TToTHead = new JCheckBox(Bundle.getMessage("PlaceHead"));

    private JCheckBox setupA1TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupA2TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupB1TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupB2TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupC1TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupC2TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupD1TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setupD2TToTLogic = new JCheckBox(Bundle.getMessage("SetLogic"));

    private JButton getSavedTToTSignalHeads = null;
    private JButton changeTToTSignalIcon = null;
    private JButton setTToTSignalsDone = null;
    private JButton setTToTSignalsCancel = null;

    private LayoutTurnout layoutTurnout1 = null;
    private LayoutTurnout layoutTurnout2 = null;

    private Turnout turnout1 = null;
    private Turnout turnout2 = null;

    private TrackSegment connectorTrack = null;

    private boolean ttotFromMenu = false;

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
        ttotFromMenu = true;
        ttotTurnoutName1 = to.getTurnoutName();
        ttotTurnoutName2 = linkedTurnoutName;

        a1TToTSignalHeadComboBox.setText("");
        a2TToTSignalHeadComboBox.setText("");
        b1TToTSignalHeadComboBox.setText("");
        b2TToTSignalHeadComboBox.setText("");
        c1TToTSignalHeadComboBox.setText("");
        c2TToTSignalHeadComboBox.setText("");
        d1TToTSignalHeadComboBox.setText("");
        d2TToTSignalHeadComboBox.setText("");

        setSignalsAtThroatToThroatTurnouts(theEditor, theFrame);
    }

    public void setSignalsAtThroatToThroatTurnouts(
            @Nonnull MultiIconEditor theEditor, @Nonnull JFrame theFrame) {
        signalIconEditor = theEditor;
        signalFrame = theFrame;
        if (setSignalsAtTToTOpen) {
            setSignalsAtTToTFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (setSignalsAtTToTFrame == null) {
            setSignalsAtTToTFrame = new JmriJFrame(Bundle.getMessage("SignalsAtTToTTurnout"), false, true);
            setSignalsAtTToTFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtTToTTurnout", true);
            setSignalsAtTToTFrame.setLocation(70, 30);
            Container theContentPane = setSignalsAtTToTFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel1a = new JPanel(new FlowLayout());
            if (false && ttotFromMenu) {
                JLabel turnout1NameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " 1 "
                        + Bundle.getMessage("Name") + " : " + ttotTurnoutName1);
                panel1a.add(turnout1NameLabel);
            } else {
                JLabel turnout1NameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " 1 "
                        + Bundle.getMessage("Name"));
                panel1a.add(turnout1NameLabel);
                panel1a.add(turnout1ComboBox);
                turnout1ComboBox.setToolTipText(Bundle.getMessage("SignalsTurnoutNameHint"));
            }
            theContentPane.add(panel1a);

            JPanel panel1b = new JPanel(new FlowLayout());
            if (false && ttotFromMenu) {
                JLabel turnout2NameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " 2 "
                        + Bundle.getMessage("Name") + " : " + ttotTurnoutName2);
                panel1b.add(turnout2NameLabel);
            } else {
                JLabel turnout2NameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " 2 "
                        + Bundle.getMessage("Name"));
                panel1b.add(turnout2NameLabel);
                panel1b.add(turnout2ComboBox);
                turnout2ComboBox.setToolTipText(Bundle.getMessage("SignalsTurnoutNameHint"));
            }
            theContentPane.add(panel1b);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            // Provide for retrieval of names of previously saved signal heads

            JPanel panel20 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("SignalHeads"));
            panel20.add(shTitle);
            panel20.add(new JLabel("		"));
            panel20.add(getSavedTToTSignalHeads = new JButton(Bundle.getMessage("GetSaved")));
            getSavedTToTSignalHeads.addActionListener((ActionEvent e) -> {
                tToTTurnoutSignalsGetSaved(e);
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
                // (de)select all checkboxes
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
                // (de)select all checkboxes
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

            // Signal heads located at turnout 1
            JPanel panel20a = new JPanel(new FlowLayout());
            panel20a.add(new JLabel(Bundle.getMessage("SignalLocated") + " " + Bundle.getMessage("BeanNameTurnout") + " 1 - "
                    + Bundle.getMessage("ContinuingTrack")));
            theContentPane.add(panel20a);

            JPanel panel21 = new JPanel(new FlowLayout());
            panel21.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 2 - " + Bundle.getMessage("ContinuingTrack") + " : "));
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
            panel23.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 2 - " + Bundle.getMessage("DivergingTrack") + " : "));
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
            panel31x.add(new JLabel(Bundle.getMessage("SignalLocated") + " " + Bundle.getMessage("BeanNameTurnout") + " 1 - "
                    + Bundle.getMessage("DivergingTrack")));
            theContentPane.add(panel31x);

            JPanel panel31 = new JPanel(new FlowLayout());
            panel31.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 2 - " + Bundle.getMessage("ContinuingTrack") + " : "));
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
            panel33.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 2 - " + Bundle.getMessage("DivergingTrack") + " : "));
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
            // Signal heads located at turnout 2

            JPanel panel41x = new JPanel(new FlowLayout());
            panel41x.add(new JLabel(Bundle.getMessage("SignalLocated") + " " + Bundle.getMessage("BeanNameTurnout") + " 2 - "
                    + Bundle.getMessage("ContinuingTrack")));
            theContentPane.add(panel41x);

            JPanel panel41 = new JPanel(new FlowLayout());
            panel41.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 1 - " + Bundle.getMessage("ContinuingTrack") + " : "));
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
            panel43.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 1 - " + Bundle.getMessage("DivergingTrack") + " : "));
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
            panel51x.add(new JLabel(Bundle.getMessage("SignalLocated") + " " + Bundle.getMessage("BeanNameTurnout") + " 2 - "
                    + Bundle.getMessage("DivergingTrack")));
            theContentPane.add(panel51x);

            JPanel panel51 = new JPanel(new FlowLayout());
            panel51.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 1 - " + Bundle.getMessage("ContinuingTrack") + " : "));
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
            panel53.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 1 - " + Bundle.getMessage("DivergingTrack") + " : "));
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

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
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
            setSignalsAtTToTFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setTToTSignalsCancelPressed(null);
                }
            });
        }
        if (false && ttotFromMenu) {
            SwingUtilities.invokeLater(() -> {
                tToTTurnoutSignalsGetSaved(null);
            });
        }
        setSignalsAtTToTFrame.pack();
        setSignalsAtTToTFrame.setVisible(true);
        setSignalsAtTToTOpen = true;
    }   // setSignalsAtTToTTurnouts

    private void tToTTurnoutSignalsGetSaved(ActionEvent a) {
        if (!getTToTTurnoutInformation()) {
            return;
        }
        a1TToTSignalHeadComboBox.setText(layoutTurnout1.getSignalB1Name());
        a2TToTSignalHeadComboBox.setText(layoutTurnout1.getSignalB2Name());
        b1TToTSignalHeadComboBox.setText(layoutTurnout1.getSignalC1Name());
        b2TToTSignalHeadComboBox.setText(layoutTurnout1.getSignalC2Name());
        c1TToTSignalHeadComboBox.setText(layoutTurnout2.getSignalB1Name());
        c2TToTSignalHeadComboBox.setText(layoutTurnout2.getSignalB2Name());
        d1TToTSignalHeadComboBox.setText(layoutTurnout2.getSignalC1Name());
        d2TToTSignalHeadComboBox.setText(layoutTurnout2.getSignalC2Name());
    }

    private void setTToTSignalsCancelPressed(ActionEvent a) {
        setSignalsAtTToTOpen = false;
        ttotFromMenu = false;
        setSignalsAtTToTFrame.setVisible(false);
    }

    private boolean getTToTTurnoutInformation() {
        int type = 0;
        Object connect = null;

        turnout1 = null;
        turnout2 = null;

        layoutTurnout1 = null;
        layoutTurnout2 = null;

        if (!ttotFromMenu) {
            ttotTurnoutName1 = turnout1ComboBox.getDisplayName();
        }
        if (ttotTurnoutName1.isEmpty()) {
            // turnout 1 not entered, test turnout 2
            ttotTurnoutName2 = turnout2ComboBox.getDisplayName();
            if (ttotTurnoutName2.isEmpty()) {
                // no entries in turnout fields
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame, Bundle.getMessage("SignalsError1"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
            turnout2 = InstanceManager.turnoutManagerInstance().getTurnout(ttotTurnoutName2);
            if (turnout2 == null) {
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError2"),
                                new Object[]{ttotTurnoutName2}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            String uname = turnout2.getUserName();
            if ((uname == null) || uname.isEmpty()
                    || !uname.equals(ttotTurnoutName2)) {
                ttotTurnoutName2 = ttotTurnoutName2.toUpperCase();
                turnout2ComboBox.setText(ttotTurnoutName2);
            }
            layoutTurnout2 = getLayoutTurnoutFromTurnout(turnout2, false, ttotTurnoutName2, setSignalsAtTToTFrame);
            if (layoutTurnout2 == null) {
                return false;
            }
            // have turnout 2 and layout turnout 2 - look for turnout 1
            connectorTrack = (TrackSegment) layoutTurnout2.getConnectA();
            if (connectorTrack == null) {
                // Inform user of error, and terminate
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        Bundle.getMessage("SignalsError18"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
            type = connectorTrack.getType1();
            connect = connectorTrack.getConnect1();
            if (connect == layoutTurnout2) {
                type = connectorTrack.getType2();
                connect = connectorTrack.getConnect2();
            }
            if ((type != LayoutTrack.TURNOUT_A) || (connect == null)) {
                // Not two turnouts connected throat-to-throat by a single Track Segment
                // Inform user of error and terminate
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        Bundle.getMessage("SignalsError18"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
            layoutTurnout1 = (LayoutTurnout) connect;
            turnout1 = layoutTurnout1.getTurnout();
            if (turnout1 == null) {
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        Bundle.getMessage("SignalsError18"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
            turnout1ComboBox.setText(layoutTurnout1.getTurnoutName());
        } else {
            // something was entered in the turnout 1 field
            turnout1 = InstanceManager.turnoutManagerInstance().getTurnout(ttotTurnoutName1);
            if (turnout1 == null) {
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError2"),
                                new Object[]{ttotTurnoutName1}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            String uname = turnout1.getUserName();
            if ((uname == null) || uname.isEmpty() || !uname.equals(ttotTurnoutName1)) {
                ttotTurnoutName1 = ttotTurnoutName1.toUpperCase();
                turnout1ComboBox.setText(ttotTurnoutName1);
            }
            // have turnout 1 - get corresponding layoutTurnout
            layoutTurnout1 = getLayoutTurnoutFromTurnout(turnout1, false, ttotTurnoutName1, setSignalsAtTToTFrame);
            if (layoutTurnout1 == null) {
                return false;
            }
            turnout1ComboBox.setText(ttotTurnoutName1);
            // have turnout 1 and layout turnout 1 - was something entered for turnout 2
            ttotTurnoutName2 = turnout2ComboBox.getDisplayName();
            if (ttotTurnoutName2.isEmpty()) {
                // no entry for turnout 2
                connectorTrack = (TrackSegment) layoutTurnout1.getConnectA();
                if (connectorTrack == null) {
                    // Inform user of error, and terminate
                    JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                            Bundle.getMessage("SignalsError18"),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                type = connectorTrack.getType1();
                connect = connectorTrack.getConnect1();
                if (connect == layoutTurnout1) {
                    type = connectorTrack.getType2();
                    connect = connectorTrack.getConnect2();
                }
                if ((type != LayoutTrack.TURNOUT_A) || (connect == null)) {
                    // Not two turnouts connected throat-to-throat by a single Track Segment
                    // Inform user of error and terminate
                    JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                            Bundle.getMessage("SignalsError18"),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                layoutTurnout2 = (LayoutTurnout) connect;
                turnout2 = layoutTurnout2.getTurnout();
                if (turnout2 == null) {
                    JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                            Bundle.getMessage("SignalsError18"),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                turnout2ComboBox.setText(layoutTurnout2.getTurnoutName());
            } else {
                // turnout 2 entered also
                turnout2 = InstanceManager.turnoutManagerInstance().getTurnout(ttotTurnoutName2);
                if (turnout2 == null) {
                    JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError2"),
                                    new Object[]{ttotTurnoutName2}), Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                uname = turnout2.getUserName();
                if ((uname == null) || uname.isEmpty() || !uname.equals(ttotTurnoutName2)) {
                    ttotTurnoutName2 = ttotTurnoutName2.toUpperCase();
                    turnout2ComboBox.setText(ttotTurnoutName2);
                }
                layoutTurnout2 = getLayoutTurnoutFromTurnout(turnout2, false, ttotTurnoutName2, setSignalsAtTToTFrame);
                if (layoutTurnout2 == null) {
                    return false;
                }
                turnout2ComboBox.setText(ttotTurnoutName2);
                // check that layout turnout 1 and layout turnout 2 are connected throat-to-throat
                if (layoutTurnout1.getConnectA() != layoutTurnout2.getConnectA()) {
                    // Not two turnouts connected throat-to-throat by a single Track Segment
                    // Inform user of error and terminate
                    JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                            Bundle.getMessage("SignalsError18"),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                connectorTrack = (TrackSegment) layoutTurnout1.getConnectA();
            }
        }
        // have both turnouts, correctly connected - complete initialization
        Point2D coordsA = layoutTurnout1.getCoordsA();
        Point2D coordsCenter = layoutTurnout1.getCoordsCenter();
        placeSignalDirectionDEG = MathUtil.wrap360(90.0 - MathUtil.computeAngleDEG(coordsCenter, coordsA));
        return true;
    }   // getTToTTurnoutInformation

    private void setTToTSignalsDonePressed(ActionEvent a) {
        if (!getTToTTurnoutInformation()) {
            return;
        }
        if (!getTToTSignalHeadInformation()) {
            return;
        }

        // place signal icons if requested, and assign signal heads to this turnout
        String signalHeadName = a1TToTSignalHeadComboBox.getDisplayName();
        if (setA1TToTHead.isSelected()) {
            if (isHeadOnPanel(a1TToTHead)
                    && (a1TToTHead != getHeadFromName(layoutTurnout1.getSignalB1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(a1TToTHead)
                        && isHeadAssignedAnywhere(a1TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout1.getSignalB1Name());
                    removeAssignment(a1TToTHead);
                    layoutTurnout1.setSignalB1Name(signalHeadName);
                }
                //} else if (assigned != B1) {
                // need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }

        signalHeadName = a2TToTSignalHeadComboBox.getDisplayName();
        if ((a2TToTHead != null) && setA2TToTHead.isSelected()) {
            if (isHeadOnPanel(a2TToTHead)
                    && (a2TToTHead != getHeadFromName(layoutTurnout1.getSignalB2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(a2TToTHead)
                        && isHeadAssignedAnywhere(a2TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout1.getSignalB2Name());
                    removeAssignment(a2TToTHead);
                    layoutTurnout1.setSignalB2Name(signalHeadName);
                }
                //} else if (assigned != B2) {
                // need to figure out what to do in this case.
            }
        } else { // a2TToTHead known to be null here
            removeSignalHeadFromPanel(layoutTurnout1.getSignalB2Name());
            layoutTurnout1.setSignalB2Name("");
        }

        signalHeadName = b1TToTSignalHeadComboBox.getDisplayName();
        if (setB1TToTHead.isSelected()) {
            if (isHeadOnPanel(b1TToTHead)
                    && (b1TToTHead != getHeadFromName(layoutTurnout1.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(b1TToTHead)
                        && isHeadAssignedAnywhere(b1TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout1.getSignalC1Name());
                    removeAssignment(b1TToTHead);
                    layoutTurnout1.setSignalC1Name(signalHeadName);
                }
                //} else if (assigned != C1) {
                // need to figure out what to do in this case.
            }
        }

        signalHeadName = b2TToTSignalHeadComboBox.getDisplayName();
        if ((b2TToTHead != null) && setB2TToTHead.isSelected()) {
            if (isHeadOnPanel(b2TToTHead)
                    && (b2TToTHead != getHeadFromName(layoutTurnout1.getSignalC2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(b2TToTHead)
                        && isHeadAssignedAnywhere(b2TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout1.getSignalC2Name());
                    removeAssignment(b2TToTHead);
                    layoutTurnout1.setSignalC2Name(signalHeadName);
                }
                //} else if (assigned != C2) {
                // need to figure out what to do in this case.
            }
        } else { // b2TToTHead known to be null here
            removeSignalHeadFromPanel(layoutTurnout1.getSignalC2Name());
            layoutTurnout1.setSignalC2Name("");
        }

        // signal heads on turnout 2
        signalHeadName = c1TToTSignalHeadComboBox.getDisplayName();
        if (setC1TToTHead.isSelected()) {
            if (isHeadOnPanel(c1TToTHead)
                    && (c1TToTHead != getHeadFromName(layoutTurnout2.getSignalB1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(c1TToTHead)
                        && isHeadAssignedAnywhere(c1TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout2.getSignalB1Name());
                    removeAssignment(c1TToTHead);
                    layoutTurnout2.setSignalB1Name(signalHeadName);
                }
                //} else if (assigned != B1) {
                // need to figure out what to do in this case.
            }
        }

        signalHeadName = c2TToTSignalHeadComboBox.getDisplayName();
        if ((c2TToTHead != null) && setC2TToTHead.isSelected()) {
            if (isHeadOnPanel(c2TToTHead)
                    && (c2TToTHead != getHeadFromName(layoutTurnout2.getSignalB2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(c2TToTHead)
                        && isHeadAssignedAnywhere(c2TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout2.getSignalB2Name());
                    removeAssignment(c2TToTHead);
                    layoutTurnout2.setSignalB2Name(signalHeadName);
                }
                //} else if (assigned != B2) {
                // need to figure out what to do in this case.
            }
        } else { // c2TToTHead known to be null here
            removeSignalHeadFromPanel(layoutTurnout2.getSignalB2Name());
            layoutTurnout2.setSignalB2Name("");
        }

        signalHeadName = d1TToTSignalHeadComboBox.getDisplayName();
        if (setD1TToTHead.isSelected()) {
            if (isHeadOnPanel(d1TToTHead)
                    && (d1TToTHead != getHeadFromName(layoutTurnout2.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(d1TToTHead)
                        && isHeadAssignedAnywhere(d1TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout2.getSignalC1Name());
                    removeAssignment(d1TToTHead);
                    layoutTurnout2.setSignalC1Name(signalHeadName);
                }
                //} else if (assigned != C1) {
                // need to figure out what to do in this case.
            }
        }

        signalHeadName = d2TToTSignalHeadComboBox.getDisplayName();
        if ((d2TToTHead != null) && setD2TToTHead.isSelected()) {
            if (isHeadOnPanel(d2TToTHead)
                    && (d2TToTHead != getHeadFromName(layoutTurnout2.getSignalC2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(d2TToTHead)
                        && isHeadAssignedAnywhere(d2TToTHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnout2.getSignalC2Name());
                    removeAssignment(d2TToTHead);
                    layoutTurnout2.setSignalC2Name(signalHeadName);
                }
                //} else if (assigned != C2) {
                // need to figure out what to do in this case.
            }
        } else { // d2TToTHead known to be null here
            removeSignalHeadFromPanel(layoutTurnout2.getSignalC2Name());
            layoutTurnout2.setSignalC2Name("");
        }

        // setup logic if requested
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
        // link the two turnouts
        layoutTurnout1.setLinkedTurnoutName(turnout2ComboBox.getDisplayName());
        layoutTurnout1.setLinkType(LayoutTurnout.THROAT_TO_THROAT);
        layoutTurnout2.setLinkedTurnoutName(turnout1ComboBox.getDisplayName());
        layoutTurnout2.setLinkType(LayoutTurnout.THROAT_TO_THROAT);
        // finish up
        setSignalsAtTToTOpen = false;
        ttotFromMenu = false;
        setSignalsAtTToTFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }   // setTToTSignalsDonePressed

    private boolean getTToTSignalHeadInformation() {
        a1TToTHead = getSignalHeadFromEntry(a1TToTSignalHeadComboBox, true, setSignalsAtTToTFrame);
        if (a1TToTHead == null) {
            return false;
        }
        a2TToTHead = getSignalHeadFromEntry(a2TToTSignalHeadComboBox, false, setSignalsAtTToTFrame);
        b1TToTHead = getSignalHeadFromEntry(b1TToTSignalHeadComboBox, true, setSignalsAtTToTFrame);
        if (b1TToTHead == null) {
            return false;
        }
        b2TToTHead = getSignalHeadFromEntry(b2TToTSignalHeadComboBox, false, setSignalsAtTToTFrame);
        c1TToTHead = getSignalHeadFromEntry(c1TToTSignalHeadComboBox, true, setSignalsAtTToTFrame);
        if (c1TToTHead == null) {
            return false;
        }
        c2TToTHead = getSignalHeadFromEntry(c2TToTSignalHeadComboBox, false, setSignalsAtTToTFrame);
        d1TToTHead = getSignalHeadFromEntry(d1TToTSignalHeadComboBox, true, setSignalsAtTToTFrame);
        if (d1TToTHead == null) {
            return false;
        }
        d2TToTHead = getSignalHeadFromEntry(d2TToTSignalHeadComboBox, false, setSignalsAtTToTFrame);
        return true;
    }

    private void placeA1TToT(String signalHeadName) {
        // place head near the continuing track of turnout 1
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
        // initialize common components and ensure all is defined
        LayoutBlock connectorBlock = connectorTrack.getLayoutBlock();
        LayoutBlock nearTurnoutBlock = nearTurnout.getLayoutBlock();
        LayoutBlock farTurnoutBlock = farTurnout.getLayoutBlock();
        Sensor connectorOccupancy = null;
        if ((connectorBlock == null) || (nearTurnoutBlock == null) || (farTurnoutBlock == null)) {
            JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        connectorOccupancy = connectorBlock.getOccupancySensor();
        if (connectorOccupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{connectorBlock.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // setup signal head for continuing track of far turnout (or both tracks of far turnout)
        if ((track1 == null) && setup1) {
            JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = null;
        SignalHead nextHead = null;
        if ((track1 != null) && setup1) {
            LayoutBlock block = track1.getLayoutBlock();
            if (block == null) {
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            occupancy = block.getOccupancySensor();
            if (occupancy == null) {
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                                new Object[]{block.getUserName()}),
                        null, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            nextHead = getNextSignalFromObject(track1, farTurnout,
                    head.getSystemName(), setSignalsAtTToTFrame);
            if ((nextHead == null) && (!reachedEndBumper())) {
                JOptionPane.showMessageDialog(setSignalsFrame,
                        MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                                new Object[]{block.getUserName()}),
                        null, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (secondHead != null) {
                // this head signals only the continuing track of the far turnout
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
            JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block2 = track2.getLayoutBlock();
        if (block2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy2 = block2.getOccupancySensor();
        if (occupancy2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block2.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        SignalHead nextHead2 = null;
        if (secondHead != null) {
            nextHead2 = getNextSignalFromObject(track2,
                    farTurnout, secondHead.getSystemName(), setSignalsAtTToTFrame);
            if ((nextHead2 == null) && (!reachedEndBumper())) {
                JOptionPane.showMessageDialog(setSignalsAtTToTFrame,
                        MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                                new Object[]{block2.getUserName()}),
                        null, JOptionPane.INFORMATION_MESSAGE);
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
    }   // setLogicTToT

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
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        } catch (IllegalArgumentException ex) {
            log.error("Trouble creating sensor " + sensorName + " while setting up Logix.");
            return "";
        }
        if (InstanceManager.getDefault(LogixManager.class).getBySystemName(logixName) == null) {
            // Logix does not exist, create it
            Logix x = InstanceManager.getDefault(LogixManager.class).createNewLogix(logixName, "");
            if (x == null) {
                log.error("Trouble creating logix " + logixName + " while setting up signal logic.");
                return "";
            }
            String cName = x.getSystemName() + "C1";
            Conditional c = InstanceManager.getDefault(ConditionalManager.class).
                    createNewConditional(cName, "");
            if (c == null) {
                log.error("Trouble creating conditional " + cName + " while setting up Logix.");
                return "";
            }
            int type = Conditional.TYPE_TURNOUT_THROWN;
            if (!continuing) {
                type = Conditional.TYPE_TURNOUT_CLOSED;
            }
            ArrayList<ConditionalVariable> variableList = c.getCopyOfStateVariables();
            variableList.add(new ConditionalVariable(false, Conditional.OPERATOR_AND,
                    type, turnoutName, true));
            c.setStateVariables(variableList);
            ArrayList<ConditionalAction> actionList = c.getCopyOfActions();
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                    Conditional.ACTION_SET_SENSOR, sensorName,
                    Sensor.ACTIVE, ""));
            actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE,
                    Conditional.ACTION_SET_SENSOR, sensorName,
                    Sensor.INACTIVE, ""));
            c.setAction(actionList);		  // string data
            x.addConditional(cName, -1);
            x.activateLogix();
        }
        return sensorName;
    }   // setupNearLogix

    /*
	 * Adds the sensor specified to the open BlockBossLogic, provided it is not already there and
	 *	provided there is an open slot. If 'name' is null or empty, returns without doing anything.
     */
    private void addNearSensorToLogic(String name) {
        if ((name != null) && !name.isEmpty()) {
            // return if a sensor by this name is already present
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
            // add in the first available slot
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
     * Tool to set signals at a three-way turnout, including placing the signal
     * icons and setup of signal logic for each signal head
     * <P>
     * This tool can only be accessed from the Tools menu. There is no access
     * from a turnout pop-up menu.
     * <P>
     * This tool requires a situation where two turnouts are connected to model
     * a 3-way turnout, with the throat of the second turnout connected to the
     * continuing leg of the first turnout by a very short track segment. The
     * actual length of the track segment is not tested. If this situation is
     * not found, and error message is sent to the user.
     * <P>
     * This tool assumes two turnouts connected with the throat of the second
     * turnout connected to the continuing leg of the first turnou, as used to
     * represent a 3-way turnout. The turnouts may be either left-handed, or
     * right-handed, or any pair of these. This tool also assumes that there are
     * no signals between the two turnouts. Signal heads are allowed/required at
     * the continuing leg of the second turnout, at each of the diverging legs,
     * and at the throat. At the throat, either one or three heads are provided
     * for. So four or six heads will be placed.
     * <P>
     * This tool assumes that each of the four tracks, the continuing, the two
     * diverging, and the throat is contained in a different block. The two
     * turnouts used to model the 3-way turnout must be in the same block.
     * Things work best if the two turnouts are in the same block as the track
     * connecting at the throat, or if the two turnouts are in their own
     * separate block, either works fine.
     */
    // operational variables for Set Signals at 3-Way Turnout tool
    private JmriJFrame setSignalsAt3WayFrame = null;
    private boolean setSignalsAt3WayOpen = false;

    private JmriBeanComboBox turnoutAComboBox = new JmriBeanComboBox(
            InstanceManager.turnoutManagerInstance(),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox turnoutBComboBox = new JmriBeanComboBox(
            InstanceManager.turnoutManagerInstance(),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JmriBeanComboBox a1_3WaySignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox a2_3WaySignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox a3_3WaySignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox b_3WaySignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox c_3WaySignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox d_3WaySignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JCheckBox setA13WayHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupA13WayLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setA23WayHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupA23WayLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setA33WayHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupA33WayLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setB3WayHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupB3WayLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setC3WayHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupC3WayLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setD3WayHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupD3WayLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JButton getSaved3WaySignalHeads = null;
    private JButton change3WaySignalIcon = null;
    private JButton set3WaySignalsDone = null;
    private JButton set3WaySignalsCancel = null;
    private LayoutTurnout layoutTurnoutA = null;
    private LayoutTurnout layoutTurnoutB = null;
    private Turnout turnoutA = null;
    private Turnout turnoutB = null;
    //private TrackSegment conTrack = null;
    private SignalHead a13WayHead = null;	// saved in A1 of Turnout A - Throat - continuing
    private SignalHead a23WayHead = null;	// saved in A2 of Turnout A - Throat - diverging A (optional)
    private SignalHead a33WayHead = null;	// saved in A3 of Turnout A - Throat - diverging B (optional)
    private SignalHead b3WayHead = null;	// saved in C1 of Turnout A - at diverging A
    private SignalHead c3WayHead = null;	// saved in B1 of Turnout B - at continuing
    private SignalHead d3WayHead = null;	// saved in C1 of Turnout B - at diverging B

    public void setSignalsAt3WayTurnoutFromMenu(
            @Nonnull String aName, @Nonnull String bName,
            @Nonnull MultiIconEditor theEditor, @Nonnull JFrame theFrame) {
        turnoutAComboBox.setText(aName);
        turnoutBComboBox.setText(bName);
        a1_3WaySignalHeadComboBox.setText("");
        a2_3WaySignalHeadComboBox.setText("");
        a3_3WaySignalHeadComboBox.setText("");
        b_3WaySignalHeadComboBox.setText("");
        c_3WaySignalHeadComboBox.setText("");
        d_3WaySignalHeadComboBox.setText("");
        setSignalsAt3WayTurnout(theEditor, theFrame);
    }

    public void setSignalsAt3WayTurnout(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        signalIconEditor = theEditor;
        signalFrame = theFrame;
        if (setSignalsAt3WayOpen) {
            setSignalsAt3WayFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (setSignalsAt3WayFrame == null) {
            setSignalsAt3WayFrame = new JmriJFrame(Bundle.getMessage("SignalsAt3WayTurnout"), false, true);
            setSignalsAt3WayFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAt3WayTurnout", true);
            setSignalsAt3WayFrame.setLocation(70, 30);
            Container theContentPane = setSignalsAt3WayFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel1 = new JPanel(new FlowLayout());
            JLabel turnoutANameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("TurnoutAName")));
            panel1.add(turnoutANameLabel);
            panel1.add(turnoutAComboBox);
            turnoutAComboBox.setToolTipText(Bundle.getMessage("SignalsTurnoutNameHint"));
            theContentPane.add(panel1);

            JPanel panel11 = new JPanel(new FlowLayout());
            JLabel turnoutBNameLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("TurnoutBName")));
            panel11.add(turnoutBNameLabel);
            panel11.add(turnoutBComboBox);
            turnoutBComboBox.setToolTipText(Bundle.getMessage("SignalsTurnoutNameHint"));
            theContentPane.add(panel11);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            // Provide for retrieval of names of previously saved signal heads

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
                // (de)select all checkboxes
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
                // (de)select all checkboxes
                setupA13WayLogic.setSelected(isSelected);
                setupA23WayLogic.setSelected(isSelected);
                setupA33WayLogic.setSelected(isSelected);
                setupB3WayLogic.setSelected(isSelected);
                setupC3WayLogic.setSelected(isSelected);
                setupD3WayLogic.setSelected(isSelected);
            });
            theContentPane.add(panel2a);
            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            // Signal heads located at turnout A
            JPanel panel20 = new JPanel(new FlowLayout());
            panel20.add(new JLabel(Bundle.getMessage("SignalLocated") + " " + Bundle.getMessage("BeanNameTurnout") + " A "));
            theContentPane.add(panel20);

            JPanel panel21 = new JPanel(new FlowLayout());
            panel21.add(new JLabel("	" + Bundle.getMessage("Throat") + " - "
                    + Bundle.getMessage("Continuing") + " : "));
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
            panel23.add(new JLabel("	" + Bundle.getMessage("Throat") + " - "
                    + Bundle.getMessage("Diverging_", "A") + " : "));
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
            panel25.add(new JLabel("	" + Bundle.getMessage("Throat") + " - "
                    + Bundle.getMessage("Diverging_", "B") + " : "));
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
            panel31.add(new JLabel("		" + Bundle.getMessage("Diverging_", "A") + " : "));
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
            // Signal heads located at turnout B

            JPanel panel40 = new JPanel(new FlowLayout());
            panel40.add(new JLabel(Bundle.getMessage("SignalLocated") + " " + Bundle.getMessage("BeanNameTurnout") + " B "));
            theContentPane.add(panel40);

            JPanel panel41 = new JPanel(new FlowLayout());
            panel41.add(new JLabel("		" + Bundle.getMessage("Continuing") + " : "));
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
            panel43.add(new JLabel("		" + Bundle.getMessage("Diverging_", "B") + " : "));
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
            // buttons

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

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
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
            setSignalsAt3WayFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    set3WaySignalsCancelPressed(null);
                }
            });
        }
        setSignalsAt3WayFrame.pack();
        setSignalsAt3WayFrame.setVisible(true);
        setSignalsAt3WayOpen = true;
    }   // setSignalsAt3WayTurnout

    private void getSaved3WaySignals(ActionEvent a) {
        if (!get3WayTurnoutInformation()) {
            return;
        }
        a1_3WaySignalHeadComboBox.setText(layoutTurnoutA.getSignalA1Name());
        a2_3WaySignalHeadComboBox.setText(layoutTurnoutA.getSignalA2Name());
        a3_3WaySignalHeadComboBox.setText(layoutTurnoutA.getSignalA3Name());
        b_3WaySignalHeadComboBox.setText(layoutTurnoutA.getSignalC1Name());
        c_3WaySignalHeadComboBox.setText(layoutTurnoutB.getSignalB1Name());
        d_3WaySignalHeadComboBox.setText(layoutTurnoutB.getSignalC1Name());
    }

    private void set3WaySignalsCancelPressed(ActionEvent a) {
        setSignalsAt3WayOpen = false;
        setSignalsAt3WayFrame.setVisible(false);
    }

    private boolean get3WayTurnoutInformation() {
        int type = 0;
        Object connect = null;
        turnoutA = null;
        turnoutB = null;
        layoutTurnoutA = null;
        layoutTurnoutB = null;

        String str = turnoutAComboBox.getDisplayName();
        if ((str == null) || str.isEmpty()) {
            // turnout A not entered, test turnout B
            str = turnoutBComboBox.getDisplayName();
            if ((str == null) || str.isEmpty()) {
                // no entries in turnout fields
                JOptionPane.showMessageDialog(setSignalsAt3WayFrame, Bundle.getMessage("SignalsError1"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
            turnoutB = InstanceManager.turnoutManagerInstance().getTurnout(str);
            if (turnoutB == null) {
                JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError2"),
                                new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            String uname = turnoutB.getUserName();
            if ((uname == null) || uname.isEmpty()
                    || !uname.equals(str)) {
                str = str.toUpperCase();
                turnoutBComboBox.setText(str);
            }
            layoutTurnoutB = getLayoutTurnoutFromTurnout(turnoutB, false, str, setSignalsAt3WayFrame);
            if (layoutTurnoutB == null) {
                return false;
            }
            // have turnout B and layout turnout B - look for turnout A
            connectorTrack = (TrackSegment) layoutTurnoutB.getConnectA();
            if (connectorTrack == null) {
                // Inform user of error, and terminate
                JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                        Bundle.getMessage("SignalsError19"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
            type = connectorTrack.getType1();
            connect = connectorTrack.getConnect1();
            if (connect == layoutTurnoutB) {
                type = connectorTrack.getType2();
                connect = connectorTrack.getConnect2();
            }
            if ((type != LayoutTrack.TURNOUT_B) || (connect == null)) {
                // Not two turnouts connected as required by a single Track Segment
                // Inform user of error and terminate
                JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                        Bundle.getMessage("SignalsError19"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
            layoutTurnoutA = (LayoutTurnout) connect;
            turnoutA = layoutTurnoutA.getTurnout();
            if (turnoutA == null) {
                JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                        Bundle.getMessage("SignalsError19"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
            turnoutAComboBox.setText(layoutTurnoutA.getTurnoutName());
        } else {
            // something was entered in the turnout A field
            turnoutA = InstanceManager.turnoutManagerInstance().getTurnout(str);
            if (turnoutA == null) {
                JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError2"),
                                new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            String uname = turnoutA.getUserName();
            if ((uname == null) || uname.isEmpty()
                    || !uname.equals(str)) {
                str = str.toUpperCase();
                turnoutAComboBox.setText(str);
            }
            // have turnout A - get corresponding layoutTurnout
            layoutTurnoutA = getLayoutTurnoutFromTurnout(turnoutA, false, str, setSignalsAt3WayFrame);
            if (layoutTurnoutA == null) {
                return false;
            }
            turnoutAComboBox.setText(str);
            // have turnout A and layout turnout A - was something entered for turnout B
            str = turnoutBComboBox.getDisplayName();
            if ((str == null) || str.isEmpty()) {
                // no entry for turnout B
                connectorTrack = (TrackSegment) layoutTurnoutA.getConnectB();
                if (connectorTrack == null) {
                    // Inform user of error, and terminate
                    JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                            Bundle.getMessage("SignalsError19"),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                type = connectorTrack.getType1();
                connect = connectorTrack.getConnect1();
                if (connect == layoutTurnoutA) {
                    type = connectorTrack.getType2();
                    connect = connectorTrack.getConnect2();
                }
                if ((type != LayoutTrack.TURNOUT_A) || (connect == null)) {
                    // Not two turnouts connected with the throat of B connected to the continuing of A
                    //	  by a single Track Segment.  Inform user of error and terminat.e
                    JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                            Bundle.getMessage("SignalsError19"),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                layoutTurnoutB = (LayoutTurnout) connect;
                turnoutB = layoutTurnoutB.getTurnout();
                if (turnoutB == null) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                            Bundle.getMessage("SignalsError19"),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                turnoutBComboBox.setText(layoutTurnoutB.getTurnoutName());
            } else {
                // turnout B entered also
                turnoutB = InstanceManager.turnoutManagerInstance().getTurnout(str);
                if (turnoutB == null) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError2"),
                                    new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                uname = turnoutB.getUserName();
                if ((uname == null) || uname.isEmpty()
                        || !uname.equals(str)) {
                    str = str.toUpperCase();
                    turnoutBComboBox.setText(str);
                }
                layoutTurnoutB = getLayoutTurnoutFromTurnout(turnoutB, false, str, setSignalsAt3WayFrame);
                if (layoutTurnoutB == null) {
                    return false;
                }
                turnoutBComboBox.setText(str);
                // check that layout turnout A and layout turnout B are connected as required
                if (layoutTurnoutA.getConnectB() != layoutTurnoutB.getConnectA()) {
                    // Not two turnouts connected as required by a single Track Segment
                    // Inform user of error and terminate
                    JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                            Bundle.getMessage("SignalsError19"),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                connectorTrack = (TrackSegment) layoutTurnoutA.getConnectB();
            }
        }
        return true;
    }   // get3WayTurnoutInformation

    private void set3WaySignalsDonePressed(ActionEvent a) {
        // process turnout names
        if (!get3WayTurnoutInformation()) {
            return;
        }
        // process signal head names
        if (!get3WaySignalHeadInformation()) {
            return;
        }
        // place signals as requested at turnout A
        String signalHeadName = a1_3WaySignalHeadComboBox.getDisplayName();
        if (setA13WayHead.isSelected()) {
            if (isHeadOnPanel(a13WayHead)
                    && (a13WayHead != getHeadFromName(layoutTurnoutA.getSignalA1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(a13WayHead)
                        && isHeadAssignedAnywhere(a13WayHead)) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnoutA.getSignalA1Name());
                    removeAssignment(a13WayHead);
                    layoutTurnoutA.setSignalA1Name(signalHeadName);
                }
                //} else if (assigned != A1) {
                // need to figure out what to do in this case.
            }
        }

        signalHeadName = a2_3WaySignalHeadComboBox.getDisplayName();
        if ((setA23WayHead.isSelected()) && (a23WayHead != null)) {
            if (isHeadOnPanel(a23WayHead)
                    && (a23WayHead != getHeadFromName(layoutTurnoutA.getSignalA2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(a23WayHead)
                        && isHeadAssignedAnywhere(a23WayHead)) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnoutA.getSignalA2Name());
                    removeAssignment(a23WayHead);
                    layoutTurnoutA.setSignalA2Name(signalHeadName);
                }
                //} else if (assigned != A2) {
                // need to figure out what to do in this case.
            }
        } else {  // a23WayHead is always null here
            removeSignalHeadFromPanel(layoutTurnoutA.getSignalA2Name());
            layoutTurnoutA.setSignalA2Name("");
        }

        signalHeadName = a3_3WaySignalHeadComboBox.getDisplayName();
        if ((setA33WayHead.isSelected()) && (a33WayHead != null)) {
            if (isHeadOnPanel(a33WayHead)
                    && (a33WayHead != getHeadFromName(layoutTurnoutA.getSignalA3Name()))) {
                JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(a33WayHead)
                        && isHeadAssignedAnywhere(a33WayHead)) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnoutA.getSignalA3Name());
                    removeAssignment(a33WayHead);
                    layoutTurnoutA.setSignalA3Name(signalHeadName);
                }
                //} else if (assigned != A3) {
                // need to figure out what to do in this case.
            }
        } else {  // a23WayHead is always null here
            removeSignalHeadFromPanel(layoutTurnoutA.getSignalA3Name());
            layoutTurnoutA.setSignalA3Name("");
        }

        signalHeadName = b_3WaySignalHeadComboBox.getDisplayName();
        if (setB3WayHead.isSelected()) {
            if (isHeadOnPanel(b3WayHead)
                    && (b3WayHead != getHeadFromName(layoutTurnoutA.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(b3WayHead)
                        && isHeadAssignedAnywhere(b3WayHead)) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnoutA.getSignalC1Name());
                    removeAssignment(b3WayHead);
                    layoutTurnoutA.setSignalC1Name(signalHeadName);
                }
                //} else if (assigned != A1) {
                // need to figure out what to do in this case.
            }
        }

        // place signals as requested at Turnout C
        signalHeadName = c_3WaySignalHeadComboBox.getDisplayName();
        if (setC3WayHead.isSelected()) {
            if (isHeadOnPanel(c3WayHead)
                    && (c3WayHead != getHeadFromName(layoutTurnoutB.getSignalB1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(c3WayHead)
                        && isHeadAssignedAnywhere(c3WayHead)) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnoutB.getSignalB1Name());
                    removeAssignment(c3WayHead);
                    layoutTurnoutB.setSignalB1Name(signalHeadName);
                }
                //} else if (assigned != B1) {
                // need to figure out what to do in this case.
            }
        }

        signalHeadName = d_3WaySignalHeadComboBox.getDisplayName();
        if (setD3WayHead.isSelected()) {
            if (isHeadOnPanel(d3WayHead)
                    && (d3WayHead != getHeadFromName(layoutTurnoutB.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(d3WayHead)
                        && isHeadAssignedAnywhere(d3WayHead)) {
                    JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutTurnoutB.getSignalC1Name());
                    removeAssignment(d3WayHead);
                    layoutTurnoutB.setSignalC1Name(signalHeadName);
                }
                //} else if (assigned != C1) {
                // need to figure out what to do in this case.
            }
        }
        // setup Logic if requested and enough information is available
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
        // link the two turnouts
        signalHeadName = turnoutBComboBox.getDisplayName();
        layoutTurnoutA.setLinkedTurnoutName(signalHeadName);
        layoutTurnoutA.setLinkType(LayoutTurnout.FIRST_3_WAY);
        signalHeadName = turnoutAComboBox.getDisplayName();
        layoutTurnoutB.setLinkedTurnoutName(signalHeadName);
        layoutTurnoutB.setLinkType(LayoutTurnout.SECOND_3_WAY);
        // finish up
        setSignalsAt3WayOpen = false;
        setSignalsAt3WayFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }   // set3WaySignalsDonePressed

    private boolean get3WaySignalHeadInformation() {
        a13WayHead = getSignalHeadFromEntry(a1_3WaySignalHeadComboBox, true, setSignalsAt3WayFrame);
        if (a13WayHead == null) {
            return false;
        }
        a23WayHead = getSignalHeadFromEntry(a2_3WaySignalHeadComboBox, false, setSignalsAt3WayFrame);
        a33WayHead = getSignalHeadFromEntry(a3_3WaySignalHeadComboBox, false, setSignalsAt3WayFrame);
        if (((a23WayHead == null) && (a33WayHead != null)) || ((a33WayHead == null)
                && (a23WayHead != null))) {
            return false;
        }
        b3WayHead = getSignalHeadFromEntry(b_3WaySignalHeadComboBox, true, setSignalsAt3WayFrame);
        if (b3WayHead == null) {
            return false;
        }
        c3WayHead = getSignalHeadFromEntry(c_3WaySignalHeadComboBox, true, setSignalsAt3WayFrame);
        if (c3WayHead == null) {
            return false;
        }
        d3WayHead = getSignalHeadFromEntry(d_3WaySignalHeadComboBox, true, setSignalsAt3WayFrame);
        if (d3WayHead == null) {
            return false;
        }
        return true;
    }

    private void place3WayThroatContinuing() {
        if (testIcon == null) {
            testIcon = signalIconEditor.getIcon(0);
        }
        String signalHeadName = a1_3WaySignalHeadComboBox.getDisplayName();
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
        String signalHeadName = a2_3WaySignalHeadComboBox.getDisplayName();
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
        String signalHeadName = a3_3WaySignalHeadComboBox.getDisplayName();
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
        String signalHeadName = b_3WaySignalHeadComboBox.getDisplayName();
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
        String signalHeadName = c_3WaySignalHeadComboBox.getDisplayName();
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
        String signalHeadName = d_3WaySignalHeadComboBox.getDisplayName();
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
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = a1_3WaySignalHeadComboBox.getDisplayName();
        SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutB,
                signalHeadName, setSignalsAt3WayFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (a23WayHead != null) {
            // set up logic for continuing head with 3 heads at throat
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
        // only one head at the throat
        JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                Bundle.getMessage("InfoMessage9"), "", JOptionPane.INFORMATION_MESSAGE);
        return;
    }   // set3WayLogicThroatContinuing

    private void set3WayLogicThroatDivergingA() {
        TrackSegment track = (TrackSegment) layoutTurnoutA.getConnectC();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = a2_3WaySignalHeadComboBox.getDisplayName();
        SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutA,
                signalHeadName, setSignalsAt3WayFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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
    }   // set3WayLogicThroatDivergingA

    private void set3WayLogicThroatDivergingB() {
        TrackSegment track = (TrackSegment) layoutTurnoutB.getConnectC();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = a3_3WaySignalHeadComboBox.getDisplayName();
        SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutB,
                signalHeadName, setSignalsAt3WayFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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
    }   // set3WayLogicThroatDivergingB

    private void set3WayLogicDivergingA() {
        TrackSegment track = (TrackSegment) layoutTurnoutA.getConnectA();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = b_3WaySignalHeadComboBox.getDisplayName();
        SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutA,
                signalHeadName, setSignalsAt3WayFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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
    }   // set3WayLogicDivergingA

    private void set3WayLogicContinuing() {
        TrackSegment track = (TrackSegment) layoutTurnoutA.getConnectA();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = c_3WaySignalHeadComboBox.getDisplayName();
        SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutA,
                signalHeadName, setSignalsAt3WayFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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
    }   // set3WayLogicContinuing

    private void set3WayLogicDivergingB() {
        TrackSegment track = (TrackSegment) layoutTurnoutA.getConnectA();
        if (track == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block = track.getLayoutBlock();
        if (block == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = block.getOccupancySensor();
        if (occupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String signalHeadName = d_3WaySignalHeadComboBox.getDisplayName();
        SignalHead nextHead = getNextSignalFromObject(track, layoutTurnoutA,
                signalHeadName, setSignalsAt3WayFrame);
        if ((nextHead == null) && (!reachedEndBumper())) {
            JOptionPane.showMessageDialog(setSignalsAt3WayFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                            new Object[]{block.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
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
    }   // set3WayLogicDivergingB

    //
    //The following is for placement of sensors and signal masts at points around the layout
    //
    //This section deals with assigning a sensor to a specific boundary point
    BeanDetails westBoundSensor;
    BeanDetails eastBoundSensor;

    private JButton getAnchorSavedSensors = null;
    private JButton changeSensorAtBoundaryIcon = null;
    private JButton setSensorsAtBoundaryDone = null;
    private JButton setSensorsAtBoundaryCancel = null;
    private boolean setSensorsAtBoundaryOpen = false;
    private JmriJFrame setSensorsAtBoundaryFrame = null;

    private JFrame sensorFrame = null;
    private MultiIconEditor sensorIconEditor = null;

    public void setSensorsAtBlockBoundaryFromMenu(@Nonnull PositionablePoint p,
            @Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        boundaryFromMenu = true;
        boundary = p;
        block1IDComboBox.setText(boundary.getConnect1().getLayoutBlock().getId());
        if (boundary.getType() != PositionablePoint.ANCHOR) {
            block2IDComboBox.setText(boundary.getConnect1().getLayoutBlock().getId());
        } else {
            block2IDComboBox.setText(boundary.getConnect2().getLayoutBlock().getId());
        }
        setSensorsAtBlockBoundary(theEditor, theFrame);
        return;
    }

    public void setSensorsAtBlockBoundary(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        sensorIconEditor = theEditor;
        sensorFrame = theFrame;
        if (setSensorsAtBoundaryOpen) {
            setSensorsAtBoundaryFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (setSensorsAtBoundaryFrame == null) {
            westBoundSensor = new BeanDetails("Sensor", InstanceManager.sensorManagerInstance());
            eastBoundSensor = new BeanDetails("Sensor", InstanceManager.sensorManagerInstance());

            setSensorsAtBoundaryFrame = new JmriJFrame(Bundle.getMessage("SensorsAtBoundary"), false, true);
            setSensorsAtBoundaryFrame.addHelpMenu("package.jmri.jmrit.display.SetSensorsAtBoundary", true);
            setSensorsAtBoundaryFrame.setLocation(70, 30);
            Container theContentPane = setSensorsAtBoundaryFrame.getContentPane();
            theContentPane.setLayout(new BorderLayout());

            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

            JPanel panel11 = new JPanel(new FlowLayout());
            if (boundaryFromMenu) {
                JLabel block1NameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 1 "
                        + Bundle.getMessage("Name") + " : " + boundary.getConnect1().getLayoutBlock().getId());
                panel11.add(block1NameLabel);
            } else {
                JLabel block1NameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 1 "
                        + Bundle.getMessage("Name") + " : ");
                panel11.add(block1NameLabel);
                panel11.add(block1IDComboBox);
                block1IDComboBox.setToolTipText(Bundle.getMessage("SensorsBlockNameHint"));
            }
            header.add(panel11);

            JPanel panel12 = new JPanel(new FlowLayout());
            if ((boundaryFromMenu) && (boundary.getType() == PositionablePoint.ANCHOR)) {
                JLabel block2NameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 2 "
                        + Bundle.getMessage("Name") + " : " + boundary.getConnect2().getLayoutBlock().getId());
                panel12.add(block2NameLabel);
            } else if (boundary.getType() == PositionablePoint.ANCHOR) {
                JLabel block2NameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 2 "
                        + Bundle.getMessage("Name") + " : ");
                panel12.add(block2NameLabel);
                panel12.add(block2IDComboBox);
                block2IDComboBox.setToolTipText(Bundle.getMessage("SensorsBlockNameHint"));
            }
            header.add(panel12);
            header.add(new JSeparator(JSeparator.HORIZONTAL));
            theContentPane.add(header, BorderLayout.NORTH);

            JPanel panel2 = new JPanel(new FlowLayout());

            JPanel main = new JPanel();
            main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

            JLabel shTitle = new JLabel(Bundle.getMessage("Sensors"));
            panel2.add(shTitle);
            panel2.add(new JLabel("	  "));
            panel2.add(getAnchorSavedSensors = new JButton(Bundle.getMessage("GetSaved")));
            getAnchorSavedSensors.addActionListener((ActionEvent e) -> {
                getSavedAnchorSensors(e);
            });
            getAnchorSavedSensors.setToolTipText(Bundle.getMessage("GetSavedHint"));
            if (boundary.getType() != PositionablePoint.END_BUMPER) {
                main.add(panel2);
            }

            if (boundary.getType() != PositionablePoint.END_BUMPER) {
                eastBoundSensor.setBoundaryTitle(Bundle.getMessage("East/SouthBound"));
                if ((boundaryFromMenu) && (boundary.getType() == PositionablePoint.ANCHOR)) {
                    if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                        eastBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
                    } else {
                        eastBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    }
                }
                eastBoundSensor.getDetailsPanel().setBackground(new Color(255, 255, 200));
                main.add(eastBoundSensor.getDetailsPanel());

                westBoundSensor.setBoundaryTitle(Bundle.getMessage("West/NorthBound"));
                if (boundaryFromMenu) {
                    if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                        westBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    } else {
                        westBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
                    }
                }
                westBoundSensor.getDetailsPanel().setBackground(new Color(200, 255, 255));
                main.add(westBoundSensor.getDetailsPanel());
            } else {
                if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary) && (boundaryFromMenu)) {
                    eastBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    eastBoundSensor.getDetailsPanel().setBackground(new Color(200, 255, 255));
                    main.add(eastBoundSensor.getDetailsPanel());
                } else if (boundaryFromMenu) {
                    westBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    westBoundSensor.getDetailsPanel().setBackground(new Color(255, 255, 200));
                    main.add(westBoundSensor.getDetailsPanel());
                }
            }
            main.add(new JSeparator(JSeparator.HORIZONTAL));
            theContentPane.add(main, BorderLayout.CENTER);

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(changeSensorAtBoundaryIcon = new JButton(Bundle.getMessage("ChangeSensorIcon")));
            changeSensorAtBoundaryIcon.addActionListener((ActionEvent e) -> {
                sensorFrame.setVisible(true);
            });
            changeSensorAtBoundaryIcon.setToolTipText(Bundle.getMessage("ChangeSensorIconHint"));
            panel6.add(new JLabel("	 "));
            panel6.add(setSensorsAtBoundaryDone = new JButton(Bundle.getMessage("ButtonDone")));
            setSensorsAtBoundaryDone.addActionListener((ActionEvent e) -> {
                setSensorsAtBoundaryDonePressed(e);
            });
            setSensorsAtBoundaryDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(setSensorsAtBoundaryDone);
                rootPane.setDefaultButton(setSensorsAtBoundaryDone);
            });

            panel6.add(setSensorsAtBoundaryCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setSensorsAtBoundaryCancel.addActionListener((ActionEvent e) -> {
                setSensorsAtBoundaryCancelPressed(e);
            });
            setSensorsAtBoundaryCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6, BorderLayout.SOUTH);
            setSensorsAtBoundaryFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSensorsAtBoundaryCancelPressed(null);
                }
            });
            if (boundaryFromMenu) {
                getSavedAnchorSensors(null);
            }
        } else {
            if (boundaryFromMenu) {
                getSavedAnchorSensors(null);
            }
        }
        setSensorsAtBoundaryFrame.setPreferredSize(null);
        setSensorsAtBoundaryFrame.pack();
        setSensorsAtBoundaryFrame.setVisible(true);
        setSensorsAtBoundaryOpen = true;
    }   // setSensorsAtBlockBoundary

    /**
     * Returns the Sensor corresponding to an entry field in the specified
     * dialog. This also takes care of UpperCase and trimming of leading and
     * trailing blanks. If entry is required, and no entry is present, and error
     * message is sent. An error message also results if a sensor head with the
     * entered name is not found in the SensorTable.
     */
    @CheckReturnValue
    public Sensor getSensorFromEntry(@Nullable String sensorName,
            boolean requireEntry,
            @Nonnull JmriJFrame frame) {
        String str = sensorName;
        if ((str == null) || str.isEmpty()) {
            if (requireEntry) {
                JOptionPane.showMessageDialog(frame, Bundle.getMessage("SensorsError5"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }
        Sensor head = InstanceManager.sensorManagerInstance().getSensor(str);
        if (head == null) {
            JOptionPane.showMessageDialog(frame,
                    MessageFormat.format(Bundle.getMessage("SensorsError4"),
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
     * panel, regardless of whether an icon is displayed or not With sensors we
     * do allow the same sensor to be allocated in both directions.
     */
    public boolean isSensorAssignedAnywhere(@Nonnull Sensor sensor) {
        for (PositionablePoint po : layoutEditor.getPositionablePoints()) {
            //We allow the same sensor to be allocated in both directions.
            if (po != boundary) {
                if (po.getEastBoundSensor() == sensor) {
                    if (!sensorAssignedElseWhere(sensor.getDisplayName())) {
                        return true;
                    }

                }
                if (po.getWestBoundSensor() == sensor) {
                    if (!sensorAssignedElseWhere(sensor.getDisplayName())) {
                        return true;
                    }
                }
            }
        }
        for (LayoutTurnout to : layoutEditor.getLayoutTurnouts()) {
            if ((to.getSensorA() != null) && to.getSensorA() == sensor) {
                if (!sensorAssignedElseWhere(sensor.getDisplayName())) {
                    return true;
                }
            }
            if ((to.getSensorB() != null) && to.getSensorB() == sensor) {
                if (!sensorAssignedElseWhere(sensor.getDisplayName())) {
                    return true;
                }
            }
            if ((to.getSensorC() != null) && to.getSensorC() == sensor) {
                if (!sensorAssignedElseWhere(sensor.getDisplayName())) {
                    return true;
                }
            }
            if ((to.getSensorD() != null) && to.getSensorD() == sensor) {
                if (!sensorAssignedElseWhere(sensor.getDisplayName())) {
                    return true;
                }
            }
        }

        for (LayoutSlip to : layoutEditor.getLayoutSlips()) {
            if ((to.getSensorA() != null) && to.getSensorA() == sensor) {
                if (!sensorAssignedElseWhere(sensor.getDisplayName())) {
                    return true;
                }
            }
            if ((to.getSensorB() != null) && to.getSensorB() == sensor) {
                if (!sensorAssignedElseWhere(sensor.getDisplayName())) {
                    return true;
                }
            }
            if ((to.getSensorC() != null) && to.getSensorC() == sensor) {
                if (!sensorAssignedElseWhere(sensor.getDisplayName())) {
                    return true;
                }
            }
            if ((to.getSensorD() != null) && to.getSensorD() == sensor) {
                if (!sensorAssignedElseWhere(sensor.getDisplayName())) {
                    return true;
                }
            }
        }

        for (LevelXing x : layoutEditor.getLevelXings()) {
            if ((x.getSensorA() != null) && x.getSensorA() == sensor) {
                if (!sensorAssignedElseWhere(sensor.getDisplayName())) {
                    return true;
                }
            }
            if ((x.getSensorB() != null) && x.getSensorB() == sensor) {
                if (!sensorAssignedElseWhere(sensor.getDisplayName())) {
                    return true;
                }
            }
            if ((x.getSensorC() != null) && x.getSensorC() == sensor) {
                if (!sensorAssignedElseWhere(sensor.getDisplayName())) {
                    return true;
                }
            }
            if ((x.getSensorD() != null) && x.getSensorD() == sensor) {
                if (!sensorAssignedElseWhere(sensor.getDisplayName())) {
                    return true;
                }
            }
        }
        return false;
    }   // isSensorAssignedAnywhere

    boolean sensorAssignedElseWhere(@Nonnull String sensor) {
        int i = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle.getMessage("DuplicateSensorAssign"),
                new Object[]{sensor}),
                Bundle.getMessage("DuplicateSensorAssignTitle"),
                JOptionPane.YES_NO_OPTION);
        if (i == 0) {
            return true;
        }
        return false;
    }

    /**
     * Removes the assignment of the specified Sensor to either a turnout, a
     * positionable point, or a level crossing wherever it is assigned
     */
    public void removeSensorAssignment(@Nullable Sensor sensor) {
        if (sensor == null) {
            return;
        }

        for (PositionablePoint po : layoutEditor.getPositionablePoints()) {
            if (po.getEastBoundSensor() == sensor) {
                po.setEastBoundSensor(null);
            }
            if (po.getWestBoundSensor() == sensor) {
                po.setWestBoundSensor(null);
            }
        }
        for (LayoutTurnout to : layoutEditor.getLayoutTurnouts()) {
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

        for (LayoutSlip to : layoutEditor.getLayoutSlips()) {
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
    }   // removeSensorAssignment

    /**
     * Removes the Sensor object from the panel and from assignment to any
     * turnout, positionable point, or level crossing
     */
    public void removeSensorFromPanel(@Nonnull Sensor sensor) {
        removeSensorAssignment(sensor);
        SensorIcon h = null;
        int index = -1;
        for (int i = 0; (i < layoutEditor.sensorList.size()) && (index == -1); i++) {
            h = layoutEditor.sensorList.get(i);
            if (h.getSensor() == sensor) {
                index = i;
            }
        }
        if (index != (-1) && h != null) {
            layoutEditor.sensorList.remove(index);
            h.remove();
            h.dispose();
            needRedraw = true;
        }
    }

    private void getSavedAnchorSensors(ActionEvent a) {
        if (!getSimpleBlockInformation()) {
            return;
        }
        eastBoundSensor.setTextField(boundary.getEastBoundSensorName());
        westBoundSensor.setTextField(boundary.getWestBoundSensorName());

        if (boundary.getType() != PositionablePoint.END_BUMPER) {
            if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                eastBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
            } else {
                eastBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            }
            if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                westBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            } else {
                westBoundSensor.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
            }
        } else {
            if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                westBoundSensor.setBoundaryLabelText("End of Block " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            } else {
                eastBoundSensor.setBoundaryLabelText("End of Block " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            }
        }

        setSensorsAtBoundaryFrame.setPreferredSize(null);
        setSensorsAtBoundaryFrame.pack();
    }

    private void setSensorsAtBoundaryCancelPressed(ActionEvent a) {
        setSensorsAtBoundaryOpen = false;
        boundaryFromMenu = false;
        setSensorsAtBoundaryFrame.setVisible(false);
    }

    private void setSensorsAtBoundaryDonePressed(ActionEvent a) {
        if (!getSimpleBlockInformation()) {
            return;
        }
        Sensor eastSensor = getSensorFromEntry(eastBoundSensor.getText(), false, setSensorsAtBoundaryFrame);
        Sensor westSensor = getSensorFromEntry(westBoundSensor.getText(), false, setSensorsAtBoundaryFrame);
        if (eastSensor == null) {
            removeSensorAssignment(InstanceManager.sensorManagerInstance().getSensor(boundary.getEastBoundSensorName()));
            boundary.setEastBoundSensor(null);
        }
        if (westSensor == null) {
            removeSensorAssignment(InstanceManager.sensorManagerInstance().getSensor(boundary.getWestBoundSensorName()));
            boundary.setWestBoundSensor(null);
        }
        // place or update signals as requested
        if ((eastSensor != null) && eastBoundSensor.addToPanel()) {
            if (isSensorAssignedAnywhere(eastSensor)
                    && (eastSensor != boundary.getEastBoundSensor())) {
                JOptionPane.showMessageDialog(setSensorsAtBoundaryFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError6"),
                                new Object[]{eastBoundSensor.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                placeEastBoundIcon(getSensorIcon(eastBoundSensor.getText()), eastBoundSensor.isRightSelected(), 0.0);
                removeSensorAssignment(eastSensor);
                boundary.setEastBoundSensor(eastBoundSensor.getText());
                needRedraw = true;
            }
        } else if ((eastSensor != null)
                && (eastSensor != boundary.getEastBoundSensor())
                && (eastSensor != boundary.getWestBoundSensor())) {
            if (isSensorAssignedAnywhere(eastSensor)) {
                JOptionPane.showMessageDialog(setSensorsAtBoundaryFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError13"),
                                new Object[]{eastBoundSensor.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorAssignment(eastSensor);
                boundary.setEastBoundSensor(eastBoundSensor.getText());
            }
        } else if ((eastSensor != null)
                && (eastSensor == boundary.getWestBoundSensor())) {
            boundary.setEastBoundSensor(eastBoundSensor.getText());
        }

        if ((westSensor != null) && westBoundSensor.addToPanel()) {
            if (isSensorAssignedAnywhere(westSensor)
                    && (westSensor != boundary.getWestBoundSensor())) {
                JOptionPane.showMessageDialog(setSensorsAtBoundaryFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError6"),
                                new Object[]{westBoundSensor.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                placeWestBoundIcon(getSensorIcon(westBoundSensor.getText()), westBoundSensor.isRightSelected(), 0.0);
                removeSensorAssignment(westSensor);
                boundary.setWestBoundSensor(westBoundSensor.getText());
                needRedraw = true;
            }
        } else if ((westSensor != null)
                && (westSensor != boundary.getEastBoundSensor())
                && (westSensor != boundary.getWestBoundSensor())) {
            if (isSensorAssignedAnywhere(westSensor)) {
                //Need to do this better, so that the sensor can be on panel multiple times but only alocated to one anchor at a time
                JOptionPane.showMessageDialog(setSensorsAtBoundaryFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError13"),
                                new Object[]{westBoundSensor.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorAssignment(westSensor);
                boundary.setWestBoundSensor(westBoundSensor.getText());
            }
        } else if ((westSensor != null)
                && (westSensor == boundary.getEastBoundSensor())) {
            boundary.setWestBoundSensor(westBoundSensor.getText());
        }
        setSensorsAtBoundaryOpen = false;
        boundaryFromMenu = false;
        setSensorsAtBoundaryFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
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

    private JButton getAnchorSavedSignalMasts = null;
    private JButton setSignalMastsAtBoundaryDone = null;
    private JButton setSignalMastsAtBoundaryCancel = null;
    private boolean setSignalMastsAtBoundaryOpen = false;
    private JmriJFrame setSignalMastsAtBoundaryFrame = null;

    BeanDetails eastSignalMast;
    BeanDetails westSignalMast;

    public void setSignalMastsAtBlockBoundaryFromMenu(
            @Nonnull PositionablePoint p) {
        boundaryFromMenu = true;
        boundary = p;
        block1IDComboBox.setText(boundary.getConnect1().getLayoutBlock().getId());
        if (boundary.getType() == PositionablePoint.ANCHOR) {
            block2IDComboBox.setText(boundary.getConnect2().getLayoutBlock().getId());
        }
        setSignalMastsAtBlockBoundary();
        return;
    }

    public void setSignalMastsAtBlockBoundary() {
        if (setSignalMastsAtBoundaryOpen) {
            setSignalMastsAtBoundaryFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (setSignalMastsAtBoundaryFrame == null) {
            eastSignalMast = new BeanDetails("SignalMast", InstanceManager.getDefault(SignalMastManager.class)); // NOI18N
            westSignalMast = new BeanDetails("SignalMast", InstanceManager.getDefault(SignalMastManager.class)); // NOI18N
            setSignalMastsAtBoundaryFrame = new JmriJFrame(Bundle.getMessage("SignalMastsAtBoundary"), false, true);
            setSignalMastsAtBoundaryFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalMastsAtBoundary", true);
            setSignalMastsAtBoundaryFrame.setLocation(70, 30);
            Container theContentPane = setSignalMastsAtBoundaryFrame.getContentPane();
            theContentPane.setLayout(new BorderLayout());

            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

            JPanel panel11 = new JPanel(new FlowLayout());
            if (boundaryFromMenu) {
                JLabel block1NameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 1 "
                        + Bundle.getMessage("Name") + " : " + boundary.getConnect1().getLayoutBlock().getId());
                panel11.add(block1NameLabel);
            } else {
                JLabel block1NameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 1 "
                        + Bundle.getMessage("Name") + " : ");
                panel11.add(block1NameLabel);
                panel11.add(block1IDComboBox);
                block1IDComboBox.setToolTipText(Bundle.getMessage("SignalMastsBlockNameHint"));
            }
            header.add(panel11);

            JPanel panel12 = new JPanel(new FlowLayout());
            if ((boundaryFromMenu) && (boundary.getType() == PositionablePoint.ANCHOR)) {
                JLabel block2NameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 2 "
                        + Bundle.getMessage("Name") + " : " + boundary.getConnect2().getLayoutBlock().getId());
                panel12.add(block2NameLabel);
            } else if (boundary.getType() == PositionablePoint.ANCHOR) {
                JLabel block2NameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 2 "
                        + Bundle.getMessage("Name") + " : ");
                panel12.add(block2NameLabel);
                panel12.add(block2IDComboBox);
                block2IDComboBox.setToolTipText(Bundle.getMessage("SignalMastsBlockNameHint"));
            }

            header.add(panel12);
            header.add(new JSeparator(JSeparator.HORIZONTAL));
            theContentPane.add(header, BorderLayout.NORTH);

            JPanel main = new JPanel();
            main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

            JPanel panel2 = new JPanel(new FlowLayout());
            JLabel shTitle = new JLabel(Bundle.getMessage("SignalMasts"));
            panel2.add(shTitle);
            panel2.add(new JLabel("	  "));
            panel2.add(getAnchorSavedSignalMasts = new JButton(Bundle.getMessage("GetSaved")));
            getAnchorSavedSignalMasts.addActionListener((ActionEvent e) -> {
                getSavedAnchorSignalMasts(e);
            });
            getAnchorSavedSignalMasts.setToolTipText(Bundle.getMessage("GetSavedHint"));
            if (boundary.getType() != PositionablePoint.END_BUMPER) {
                main.add(panel2);
            }

            if (boundary.getType() != PositionablePoint.END_BUMPER) {
                eastSignalMast.setBoundaryTitle(Bundle.getMessage("East/SouthBound"));
                if (boundaryFromMenu) {
                    if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                        eastSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
                    } else {
                        eastSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    }
                }
                eastSignalMast.getDetailsPanel().setBackground(new Color(255, 255, 200));
                main.add(eastSignalMast.getDetailsPanel());

                westSignalMast.setBoundaryTitle(Bundle.getMessage("West/NorthBound"));
                if (boundaryFromMenu) {
                    if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                        westSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    } else {
                        westSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
                    }
                }
                westSignalMast.getDetailsPanel().setBackground(new Color(200, 255, 255));
                main.add(westSignalMast.getDetailsPanel());
            } else {
                if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary) && (boundaryFromMenu)) {
                    eastSignalMast.setBoundaryLabelText("End of Block " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    eastSignalMast.getDetailsPanel().setBackground(new Color(200, 255, 255));
                    main.add(eastSignalMast.getDetailsPanel());
                } else if (boundaryFromMenu) {
                    westSignalMast.setBoundaryLabelText("End of Block " + boundary.getConnect1().getLayoutBlock().getDisplayName());
                    westSignalMast.getDetailsPanel().setBackground(new Color(255, 255, 200));
                    main.add(westSignalMast.getDetailsPanel());
                }
            }
            main.add(new JSeparator(JSeparator.HORIZONTAL));
            theContentPane.add(main, BorderLayout.CENTER);

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(setSignalMastsAtBoundaryDone = new JButton(Bundle.getMessage("ButtonDone")));
            setSignalMastsAtBoundaryDone.addActionListener((ActionEvent e) -> {
                setSignalMastsAtBoundaryDonePressed(e);
            });
            setSignalMastsAtBoundaryDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(setSignalMastsAtBoundaryDone);
                rootPane.setDefaultButton(setSignalMastsAtBoundaryDone);
            });

            panel6.add(setSignalMastsAtBoundaryCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            setSignalMastsAtBoundaryCancel.addActionListener((ActionEvent e) -> {
                setSignalMastsAtBoundaryCancelPressed(e);
            });
            setSignalMastsAtBoundaryCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            theContentPane.add(panel6, BorderLayout.SOUTH);
            setSignalMastsAtBoundaryFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSignalMastsAtBoundaryCancelPressed(null);
                }
            });
            if (boundaryFromMenu) {
                getSavedAnchorSignalMasts(null);
            }
        } else if (boundaryFromMenu) {
            getSavedAnchorSignalMasts(null);
        }
        refreshSignalMastAtBoundaryComboBox();
        setSignalMastsAtBoundaryFrame.setPreferredSize(null);
        setSignalMastsAtBoundaryFrame.pack();
        setSignalMastsAtBoundaryFrame.setVisible(true);
        setSignalMastsAtBoundaryOpen = true;
    }

    /**
     * Returns the SignalMast corresponding to an entry field in the specified
     * dialog. This also takes care of UpperCase and trimming of leading and
     * trailing blanks. If entry is required, and no entry is present, and error
     * message is sent. An error message also results if a signalMast head with
     * the entered name is not found in the SignalMastTable.
     */
    @CheckReturnValue
    public SignalMast getSignalMastFromEntry(@Nullable String signalMastName,
            boolean requireEntry,
            @Nonnull JmriJFrame frame) {
        String str = signalMastName;
        if ((str == null) || str.isEmpty()) {
            if (requireEntry) {
                JOptionPane.showMessageDialog(frame, Bundle.getMessage("SignalMastsError5"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }
        SignalMast head = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(str);
        if (head == null) {
            JOptionPane.showMessageDialog(frame,
                    MessageFormat.format(Bundle.getMessage("SignalMastsError4"),
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
        for (PositionablePoint po : layoutEditor.getPositionablePoints()) {
            if ((po.getEastBoundSignalMast() != null) && po.getEastBoundSignalMast() == signalMast) {
                return true;
            }
            if ((po.getWestBoundSignalMast() != null) && po.getWestBoundSignalMast() == signalMast) {
                return true;
            }
        }

        for (LayoutTurnout to : layoutEditor.getLayoutTurnouts()) {
            if ((to.getSignalAMast() != null) && to.getSignalDMast() == signalMast) {
                return true;
            }
            if ((to.getSignalBMast() != null) && to.getSignalDMast() == signalMast) {
                return true;
            }
            if ((to.getSignalCMast() != null) && to.getSignalDMast() == signalMast) {
                return true;
            }
            if ((to.getSignalDMast() != null) && to.getSignalDMast() == signalMast) {
                return true;
            }
        }

        for (LayoutSlip to : layoutEditor.getLayoutSlips()) {
            if ((to.getSignalAMast() != null) && to.getSignalDMast() == signalMast) {
                return true;
            }
            if ((to.getSignalBMast() != null) && to.getSignalDMast() == signalMast) {
                return true;
            }
            if ((to.getSignalCMast() != null) && to.getSignalDMast() == signalMast) {
                return true;
            }
            if ((to.getSignalDMast() != null) && to.getSignalDMast() == signalMast) {
                return true;
            }
        }

        for (LevelXing x : layoutEditor.getLevelXings()) {
            if ((x.getSignalAMast() != null) && x.getSignalAMast() == signalMast) {
                return true;
            }
            if ((x.getSignalBMast() != null) && x.getSignalAMast() == signalMast) {
                return true;
            }
            if ((x.getSignalCMast() != null) && x.getSignalAMast() == signalMast) {
                return true;
            }
            if ((x.getSignalDMast() != null) && x.getSignalAMast() == signalMast) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes the assignment of the specified SignalMast to either a turnout, a
     * positionable point, or a level crossing wherever it is assigned
     */
    public void removeSignalMastAssignment(@Nullable SignalMast signalMast) {
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
        for (LayoutTurnout to : layoutEditor.getLayoutTurnouts()) {
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

        for (LayoutSlip to : layoutEditor.getLayoutSlips()) {
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
            if (h.getSignalMast() == signalMast) {
                index = i;
            }
        }
        if (index != (-1)) {
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
                eastSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
            } else {
                eastSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            }
            if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                westSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            } else {
                westSignalMast.setBoundaryLabelText("Protecting Block : " + boundary.getConnect2().getLayoutBlock().getDisplayName());
            }
        } else {
            if (isAtWestEndOfAnchor(boundary.getConnect1(), boundary)) {
                westSignalMast.setBoundaryLabelText("End of Block " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            } else {
                eastSignalMast.setBoundaryLabelText("End of Block " + boundary.getConnect1().getLayoutBlock().getDisplayName());
            }
        }
        setSignalMastsAtBoundaryFrame.setPreferredSize(null);
        setSignalMastsAtBoundaryFrame.pack();
    }

    private void setSignalMastsAtBoundaryCancelPressed(ActionEvent a) {
        setSignalMastsAtBoundaryOpen = false;
        boundaryFromMenu = false;
        setSignalMastsAtBoundaryFrame.setVisible(false);
    }

    void refreshSignalMastAtBoundaryComboBox() {
        createListUsedSignalMasts();
        usedMasts.remove(eastSignalMast.getBean());
        usedMasts.remove(westSignalMast.getBean());
        eastSignalMast.getCombo().excludeItems(usedMasts);
        westSignalMast.getCombo().excludeItems(usedMasts);
    }

    private void setSignalMastsAtBoundaryDonePressed(ActionEvent a) {
        if (!getSimpleBlockInformation()) {
            return;
        }

        SignalMast oldBlock1SignalMast = boundary.getEastBoundSignalMast();
        SignalMast block1BoundSignalMast = getSignalMastFromEntry(eastSignalMast.getText(), false, setSignalMastsAtBoundaryFrame);
        if (block1BoundSignalMast == null) {
            if (InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled() && InstanceManager.getDefault(SignalMastLogicManager.class).isSignalMastUsed(oldBlock1SignalMast)) {
                SignallingGuiTools.removeSignalMastLogic(setSignalMastsAtBoundaryFrame, oldBlock1SignalMast);
            }

            removeSignalMastFromPanel(boundary.getEastBoundSignalMast());
            removeSignalMastAssignment(boundary.getEastBoundSignalMast());
            boundary.setEastBoundSignalMast("");
        }

        SignalMast oldBlock2SignalMast = boundary.getWestBoundSignalMast();
        SignalMast block2BoundSignalMast = getSignalMastFromEntry(westSignalMast.getText(), false, setSignalMastsAtBoundaryFrame);
        if (block2BoundSignalMast == null) {
            if (InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled() && InstanceManager.getDefault(SignalMastLogicManager.class).isSignalMastUsed(oldBlock2SignalMast)) {
                SignallingGuiTools.removeSignalMastLogic(setSignalMastsAtBoundaryFrame, oldBlock2SignalMast);
            }

            removeSignalMastFromPanel(boundary.getWestBoundSignalMast());
            removeSignalMastAssignment(boundary.getWestBoundSignalMast());
            boundary.setWestBoundSignalMast("");
        }
        if (block2BoundSignalMast != null && block1BoundSignalMast != null) {
            if (block1BoundSignalMast == block2BoundSignalMast) {
                JOptionPane.showMessageDialog(setSignalMastsAtBoundaryFrame,
                        Bundle.getMessage("SignalMastsError14"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                if (InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
                    SignallingGuiTools.swapSignalMastLogic(setSignalMastsAtBoundaryFrame, block1BoundSignalMast, block2BoundSignalMast);
                }
                needRedraw = true;
            }
        }
        if (!needRedraw) {
            if (block1BoundSignalMast != null) {
                if (eastSignalMast.addToPanel()) {
                    if (isSignalMastAssignedAnywhere(block1BoundSignalMast)
                            && (block1BoundSignalMast != oldBlock1SignalMast)) {
                        JOptionPane.showMessageDialog(setSignalMastsAtBoundaryFrame,
                                MessageFormat.format(Bundle.getMessage("SignalMastsError6"),
                                        new Object[]{eastSignalMast.getText()}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                        JOptionPane.showMessageDialog(setSignalMastsAtBoundaryFrame,
                                MessageFormat.format(Bundle.getMessage("SignalMastsError13"),
                                        new Object[]{eastSignalMast.getText()}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                        JOptionPane.showMessageDialog(setSignalMastsAtBoundaryFrame,
                                MessageFormat.format(Bundle.getMessage("SignalMastsError6"),
                                        new Object[]{westSignalMast.getText()}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                        JOptionPane.showMessageDialog(setSignalMastsAtBoundaryFrame,
                                MessageFormat.format(Bundle.getMessage("SignalMastsError13"),
                                        new Object[]{westSignalMast.getText()}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        removeSignalMastFromPanel(boundary.getWestBoundSignalMast());
                        removeSignalMastAssignment(block2BoundSignalMast);
                        boundary.setWestBoundSignalMast(westSignalMast.getText());
                    }
                }
            }

            //If advanced routing is enabled and then this indicates that we are using this for discovering the signalmast logic paths.
            if (InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()
                    && (block1BoundSignalMast != null
                    || block2BoundSignalMast != null)) {
                if ((oldBlock1SignalMast != null) && (block2BoundSignalMast != null)) {
                    updateBoundaryBasedSignalMastLogic(
                            oldBlock1SignalMast, oldBlock2SignalMast,
                            block1BoundSignalMast, block2BoundSignalMast);

                }
            }
        }
        setSignalMastsAtBoundaryOpen = false;

        setSignalMastsAtBoundaryFrame.setVisible(false);
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
        SignalMastLogicManager smlm = InstanceManager.getDefault(SignalMastLogicManager.class);
        boolean old1Used = smlm.isSignalMastUsed(oldBlock1SignalMast);
        boolean old2Used = smlm.isSignalMastUsed(oldBlock2SignalMast);
        //Just check that the old ones are used in logics somewhere.
        if (old1Used || old2Used) {
            boolean new1Used = smlm.isSignalMastUsed(block1BoundSignalMast);
            boolean new2Used = smlm.isSignalMastUsed(block2BoundSignalMast);
            if (new1Used || new2Used) {
                if ((new1Used) && (block1BoundSignalMast != oldBlock1SignalMast)) {
                    SignallingGuiTools.removeAlreadyAssignedSignalMastLogic(setSignalMastsAtBoundaryFrame, block1BoundSignalMast);
                }
                if ((new2Used) && (block2BoundSignalMast != oldBlock2SignalMast)) {
                    SignallingGuiTools.removeAlreadyAssignedSignalMastLogic(setSignalMastsAtBoundaryFrame, block2BoundSignalMast);
                }
            }
            if (block1BoundSignalMast != null) {
                if (oldBlock2SignalMast != null && old2Used
                        && oldBlock2SignalMast == block1BoundSignalMast) {
                    SignallingGuiTools.updateSignalMastLogic(setSignalMastsAtBoundaryFrame, oldBlock2SignalMast, block1BoundSignalMast);
                }

                if (oldBlock1SignalMast != null && old1Used
                        && oldBlock1SignalMast != block1BoundSignalMast) {

                    SignallingGuiTools.updateSignalMastLogic(setSignalMastsAtBoundaryFrame, oldBlock1SignalMast, block1BoundSignalMast);
                }
            }
            if (block2BoundSignalMast != null) {
                if (old1Used && oldBlock1SignalMast == block2BoundSignalMast) {

                    SignallingGuiTools.updateSignalMastLogic(setSignalMastsAtBoundaryFrame, oldBlock1SignalMast, block2BoundSignalMast);
                }
                if (old2Used && oldBlock2SignalMast != block2BoundSignalMast) {
                    SignallingGuiTools.updateSignalMastLogic(setSignalMastsAtBoundaryFrame, oldBlock2SignalMast, block2BoundSignalMast);
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
            // Compute arc's chord
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
    final int offSetFromPoint = 5;

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

    boolean setSignalMastsOpen = false;
    boolean turnoutMastFromMenu = false;
    private JmriJFrame signalMastsJmriFrame = null;

    private JmriBeanComboBox signalMastsTurnoutComboBox = new JmriBeanComboBox(
            InstanceManager.turnoutManagerInstance(), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JButton setSignalMastsDone;
    private JButton getSavedSignalMasts;
    private JButton setSignalMastsCancel;

    BeanDetails turnoutSignalMastA;
    BeanDetails turnoutSignalMastB;
    BeanDetails turnoutSignalMastC;
    BeanDetails turnoutSignalMastD;

    JPanel signalMastTurnoutPanel = new JPanel(new FlowLayout());

    private String[] turnoutBlocks = new String[4];

    public void setSignalMastsAtTurnoutFromMenu(@Nonnull LayoutTurnout to,
            @Nonnull String[] blocks) {
        turnoutMastFromMenu = true;
        layoutTurnout = to;
        turnout = to.getTurnout();
        signalMastsTurnoutComboBox.setText(to.getTurnoutName());
        turnoutBlocks = new String[4];
        for (int i = 0; i < blocks.length; i++) {
            turnoutBlocks[i] = blocks[i];
        }
        setSignalMastsAtTurnouts();
    }

    List<NamedBean> usedMasts = new ArrayList<>();

    void createListUsedSignalMasts() {
        usedMasts = new ArrayList<>();
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

        for (LayoutTurnout to : layoutEditor.getLayoutTurnouts()) {
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
        for (LayoutSlip sl : layoutEditor.getLayoutSlips()) {
            if (sl.getSignalAMast() != null) {
                usedMasts.add(sl.getSignalAMast());
            }
            if (sl.getSignalBMast() != null) {
                usedMasts.add(sl.getSignalBMast());
            }
            if (sl.getSignalCMast() != null) {
                usedMasts.add(sl.getSignalCMast());
            }
            if (sl.getSignalDMast() != null) {
                usedMasts.add(sl.getSignalDMast());
            }
        }
    }

    void refreshSignalMastAtTurnoutComboBox() {
        turnoutSignalMastsGetSaved(null);
        createListUsedSignalMasts();

        usedMasts.remove(turnoutSignalMastA.getBean());
        usedMasts.remove(turnoutSignalMastB.getBean());
        usedMasts.remove(turnoutSignalMastC.getBean());
        usedMasts.remove(turnoutSignalMastD.getBean());

        turnoutSignalMastA.getCombo().excludeItems(usedMasts);
        turnoutSignalMastB.getCombo().excludeItems(usedMasts);
        turnoutSignalMastC.getCombo().excludeItems(usedMasts);
        turnoutSignalMastD.getCombo().excludeItems(usedMasts);
    }

    public void setSignalMastsAtTurnouts() {
        if (setSignalMastsOpen) {
            //We will do a refresh in case the block boundaries have changed.
            turnoutSignalMastsGetSaved(null);
            refreshSignalMastAtTurnoutComboBox();
            signalMastsJmriFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (signalMastsJmriFrame == null) {
            signalMastsJmriFrame = new JmriJFrame(Bundle.getMessage("SignalMastsAtTurnout"), false, true);
            signalMastsJmriFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalMastsAtTurnout", true);
            signalMastsJmriFrame.setLocation(70, 30);
            Container theContentPane = signalMastsJmriFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel1 = new JPanel(new FlowLayout());

            turnoutSignalMastA = new BeanDetails("SignalMast", InstanceManager.getDefault(SignalHeadManager.class));
            turnoutSignalMastB = new BeanDetails("SignalMast", InstanceManager.getDefault(SignalHeadManager.class));
            turnoutSignalMastC = new BeanDetails("SignalMast", InstanceManager.getDefault(SignalHeadManager.class));
            turnoutSignalMastD = new BeanDetails("SignalMast", InstanceManager.getDefault(SignalHeadManager.class));

            if (turnoutMastFromMenu) {
                JLabel turnoutMastNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " "
                        + Bundle.getMessage("Name") + " : " + layoutTurnout.getTurnoutName());
                panel1.add(turnoutMastNameLabel);

                turnoutSignalMastA.setTextField(layoutTurnout.getSignalAMastName());
                turnoutSignalMastB.setTextField(layoutTurnout.getSignalBMastName());
                turnoutSignalMastC.setTextField(layoutTurnout.getSignalCMastName());
                turnoutSignalMastD.setTextField(layoutTurnout.getSignalDMastName());
            } else {
                JLabel turnoutMastNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " "
                        + Bundle.getMessage("Name"));
                panel1.add(turnoutMastNameLabel);
                panel1.add(signalMastsTurnoutComboBox);
                signalMastsTurnoutComboBox.setToolTipText(Bundle.getMessage("SignalMastsTurnoutNameHint"));
            }
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

            turnoutSignalMastA.getDetailsPanel().setBackground(new Color(255, 255, 200));
            turnoutSignalMastB.getDetailsPanel().setBackground(new Color(200, 255, 255));
            turnoutSignalMastC.getDetailsPanel().setBackground(new Color(200, 200, 255));
            turnoutSignalMastD.getDetailsPanel().setBackground(new Color(255, 200, 200));

            signalMastTurnoutPanel.setLayout(new GridLayout(0, 2));

            turnoutSignalMastA.setBoundaryLabel(turnoutBlocks[0]);
            turnoutSignalMastB.setBoundaryLabel(turnoutBlocks[1]);
            turnoutSignalMastC.setBoundaryLabel(turnoutBlocks[2]);
            turnoutSignalMastD.setBoundaryLabel(turnoutBlocks[3]);

            if (turnoutBlocks[0] != null) {
                signalMastTurnoutPanel.add(turnoutSignalMastA.getDetailsPanel());
            }
            if (turnoutBlocks[1] != null) {
                signalMastTurnoutPanel.add(turnoutSignalMastB.getDetailsPanel());
            }
            if (turnoutBlocks[2] != null) {
                signalMastTurnoutPanel.add(turnoutSignalMastC.getDetailsPanel());
            }
            if (turnoutBlocks[3] != null) {
                signalMastTurnoutPanel.add(turnoutSignalMastD.getDetailsPanel());
            }
            theContentPane.add(signalMastTurnoutPanel);

            theContentPane.add(new JSeparator(JSeparator.HORIZONTAL));

            JPanel panel6 = new JPanel(new FlowLayout());
            panel6.add(new JLabel("	 "));
            panel6.add(setSignalMastsDone = new JButton(Bundle.getMessage("ButtonDone")));
            setSignalMastsDone.addActionListener((ActionEvent e) -> {
                setSignalMastsDonePressed(e);
            });
            setSignalMastsDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
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
            //signalMastsJmriFrame.addWindowListener(new WindowAdapter() {
            //    @Override
            //    public void windowClosing(WindowEvent e) {
            //        setSignalMastsCancelPressed(null);
            //    }
            //});
            if (turnoutFromMenu) {
                turnoutSignalMastsGetSaved(null);
            }
        }
        refreshSignalMastAtTurnoutComboBox();
        signalMastsJmriFrame.setPreferredSize(null);
        signalMastsJmriFrame.pack();
        signalMastsJmriFrame.setVisible(true);
        setSignalMastsOpen = true;
    }

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

        signalMastTurnoutPanel.remove(turnoutSignalMastA.getDetailsPanel());
        signalMastTurnoutPanel.remove(turnoutSignalMastB.getDetailsPanel());
        signalMastTurnoutPanel.remove(turnoutSignalMastC.getDetailsPanel());
        signalMastTurnoutPanel.remove(turnoutSignalMastD.getDetailsPanel());

        boolean blockBoundary = false;
        if (turnoutBlocks[0] != null) {
            signalMastTurnoutPanel.add(turnoutSignalMastA.getDetailsPanel());
            blockBoundary = true;
        }
        if (turnoutBlocks[1] != null) {
            signalMastTurnoutPanel.add(turnoutSignalMastB.getDetailsPanel());
            blockBoundary = true;
        }
        if (turnoutBlocks[2] != null) {
            signalMastTurnoutPanel.add(turnoutSignalMastC.getDetailsPanel());
            blockBoundary = true;
        }
        if (turnoutBlocks[3] != null) {
            signalMastTurnoutPanel.add(turnoutSignalMastD.getDetailsPanel());
            blockBoundary = true;
        }
        if (!blockBoundary) {
            JOptionPane.showMessageDialog(signalMastsAtXingFrame, "There are no block boundaries on this turnout\nIt is therefore not possible to add Signal Masts to it");
        }
        signalMastsJmriFrame.setPreferredSize(null);
        signalMastsJmriFrame.pack();
    }

    private int isMastAssignedHere(
            @Nullable SignalMast mast,
            @Nullable LayoutTurnout lTurnout) {
        if ((mast == null) || (lTurnout == null)) {
            return NONE;
        }
        String sysName = mast.getSystemName();
        String uName = mast.getUserName();
        String name = lTurnout.getSignalAMastName();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return A1;
        }
        name = lTurnout.getSignalBMastName();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return A2;
        }
        name = lTurnout.getSignalCMastName();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return A3;
        }
        name = lTurnout.getSignalDMastName();
        if (!name.isEmpty() && (name.equals(uName) || name.equals(sysName))) {
            return B1;
        }
        return NONE;
    }

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
    }

    private void setSignalMastsDonePressed(ActionEvent a) {
        // process turnout name
        if (!getTurnoutMastInformation()) {
            return;
        }

        // process signal head names
        SignalMast turnoutMast = getSignalMastFromEntry(turnoutSignalMastA.getText(), false, setSignalsFrame);
        SignalMast turnoutMastB = getSignalMastFromEntry(turnoutSignalMastB.getText(), false, setSignalsFrame);
        SignalMast turnoutMastC = getSignalMastFromEntry(turnoutSignalMastC.getText(), false, setSignalsFrame);
        SignalMast turnoutMastD = getSignalMastFromEntry(turnoutSignalMastD.getText(), false, setSignalsFrame);

        // place signals as requested
        if (turnoutSignalMastA.addToPanel() && (turnoutMast != null)) {
            if (isSignalMastOnPanel(turnoutMast)
                    && (turnoutMast != layoutTurnout.getSignalAMast())) {
                JOptionPane.showMessageDialog(setSignalsFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{turnoutSignalMastA.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isSignalMastOnPanel(turnoutMast)
                        && isSignalMastAssignedAnywhere(turnoutMast)) {
                    JOptionPane.showMessageDialog(setSignalsFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{turnoutSignalMastA.getText()}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalMastFromPanel(layoutTurnout.getSignalAMast());
                    removeAssignment(turnoutMast);
                    layoutTurnout.setSignalAMast(turnoutSignalMastA.getText());
                }
                //} else if (assigned != A1) {
                // need to figure out what to do in this case.
            }
        } else {
            removeSignalMastFromPanel(layoutTurnout.getSignalAMast());
            layoutTurnout.setSignalAMast("");
        }
        if ((turnoutSignalMastB.addToPanel()) && (turnoutMastB != null)) {
            if (isSignalMastOnPanel(turnoutMastB)
                    && (turnoutMastB != layoutTurnout.getSignalBMast())) {
                JOptionPane.showMessageDialog(setSignalsFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{turnoutSignalMastB.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isSignalMastOnPanel(turnoutMastB)
                        && isSignalMastAssignedAnywhere(turnoutMastB)) {
                    JOptionPane.showMessageDialog(setSignalsFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{turnoutSignalMastB.getText()}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalMastFromPanel(layoutTurnout.getSignalBMast());
                    removeAssignment(turnoutMastB);
                    layoutTurnout.setSignalBMast(turnoutSignalMastB.getText());
                }
                //} else if (assigned != A2) {
                // need to figure out what to do in this case.
            }
        } else {
            removeSignalMastFromPanel(layoutTurnout.getSignalBMast());
            layoutTurnout.setSignalBMast("");
        }
        if (turnoutMastC != null) {
            if (turnoutSignalMastC.addToPanel()) {
                if (isSignalMastOnPanel(turnoutMastC)
                        && (turnoutMastC != layoutTurnout.getSignalCMast())) {
                    JOptionPane.showMessageDialog(setSignalsFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                    new Object[]{turnoutSignalMastC.getText()}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                if (assigned == NONE) {
                    if (isSignalMastOnPanel(turnoutMastC)
                            && isSignalMastAssignedAnywhere(turnoutMastC)) {
                        JOptionPane.showMessageDialog(setSignalsFrame,
                                MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                        new Object[]{turnoutSignalMastC.getText()}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        removeSignalMastFromPanel(layoutTurnout.getSignalCMast());
                        removeAssignment(turnoutMastC);
                        layoutTurnout.setSignalCMast(turnoutSignalMastC.getText());
                    }
                    //} else if (assigned != A3) {
                    // need to figure out what to do in this case.
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
                    String signalHeadName = divergingSignalHeadComboBox.getDisplayName();
                    JOptionPane.showMessageDialog(setSignalsFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                if (assigned == NONE) {
                    if (isSignalMastOnPanel(turnoutMastD)
                            && isSignalMastAssignedAnywhere(turnoutMastD)) {
                        JOptionPane.showMessageDialog(setSignalsFrame,
                                MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                        new Object[]{turnoutSignalMastD.getText()}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        removeSignalMastFromPanel(layoutTurnout.getSignalDMast());
                        removeAssignment(turnoutMastD);
                        layoutTurnout.setSignalDMast(turnoutSignalMastD.getText());
                    }
                    //} else if (assigned != B1) {
                    // need to figure out what to do in this case.
                }
            }
        } else {
            removeSignalMastFromPanel(layoutTurnout.getSignalDMast());
            layoutTurnout.setSignalDMast("");
        }

        // make sure this layout turnout is not linked to another
        layoutTurnout.setLinkType(LayoutTurnout.NO_LINK);
        layoutTurnout.setLinkedTurnoutName("");
        // finish up
        setSignalMastsOpen = false;
        turnoutFromMenu = false;
        signalMastsJmriFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }

    private boolean getTurnoutMastInformation() {
        turnout = null;
        layoutTurnout = null;
        String str = signalMastsTurnoutComboBox.getDisplayName();
        if ((str == null) || str.isEmpty()) {
            JOptionPane.showMessageDialog(setSignalsFrame, Bundle.getMessage("SignalsError1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        turnout = InstanceManager.turnoutManagerInstance().getTurnout(str);
        if (turnout == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    MessageFormat.format(Bundle.getMessage("SignalsError2"),
                            new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            String uname = turnout.getUserName();
            if ((uname == null) || uname.isEmpty()
                    || !uname.equals(str)) {
                signalMastsTurnoutComboBox.setText(str);
            }
        }
        for (LayoutTurnout t : layoutEditor.getLayoutTurnouts()) {
            if (t.getTurnout() == turnout) {
                layoutTurnout = t;
            }
        }

        LayoutTurnout t = layoutTurnout;
        if (t == null) {
            JOptionPane.showMessageDialog(setSignalsFrame,
                    MessageFormat.format(Bundle.getMessage("SignalsError3"),
                            new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void placingBlock(PositionableIcon icon, boolean isRightSide, double fromPoint, Object obj, Point2D p) {
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
        return;
    }

    private void setSignalMastsCancelPressed(ActionEvent a) {
        setSignalMastsOpen = false;
        turnoutFromMenu = false;
        signalMastsJmriFrame.setVisible(false);
    }

    // operational variables for Set SignalMast at Slip tool
    private JmriJFrame signalMastsAtSlipFrame = null;
    private boolean setSignalMastsAtSlipOpen = false;

    private JButton getSavedSlipSignalMasts = null;
    private JButton setSlipSignalMastsDone = null;
    private JButton setSlipSignalMastsCancel = null;

    private boolean slipMastFromMenu = false;
    private String[] slipBlocks = new String[4];

    BeanDetails slipSignalMastA;
    BeanDetails slipSignalMastB;
    BeanDetails slipSignalMastC;
    BeanDetails slipSignalMastD;

    JPanel signalMastLayoutSlipPanel = new JPanel(new FlowLayout());

    public void setSignalMastsAtSlipFromMenu(@Nonnull LayoutSlip slip,
            @Nonnull String[] blocks, @Nonnull JFrame theFrame) {
        slipMastFromMenu = true;
        layoutSlip = slip;
        layoutTurnout = slip;
        xingBlockAComboBox.setText(layoutSlip.getBlockName());
        slipBlocks = new String[4];
        for (int i = 0; i < blocks.length; i++) {
            slipBlocks[i] = blocks[i];
        }
        setSignalMastsAtLayoutSlip(theFrame);
        return;
    }

    public void setSignalMastsAtLayoutSlip(@Nonnull JFrame theFrame) {
        signalFrame = theFrame;
        if (setSignalMastsAtSlipOpen) {
            slipSignalMastsGetSaved(null);
            signalMastsAtSlipFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (signalMastsAtSlipFrame == null) {
            slipSignalMastA = new BeanDetails("SignalMast", InstanceManager.getDefault(SignalHeadManager.class));
            slipSignalMastB = new BeanDetails("SignalMast", InstanceManager.getDefault(SignalHeadManager.class));
            slipSignalMastC = new BeanDetails("SignalMast", InstanceManager.getDefault(SignalHeadManager.class));
            slipSignalMastD = new BeanDetails("SignalMast", InstanceManager.getDefault(SignalHeadManager.class));

            signalMastsAtSlipFrame = new JmriJFrame(Bundle.getMessage("SignalMastsAtLayoutSlip"), false, true);
            signalMastsAtSlipFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtLayoutSlip", true);
            signalMastsAtSlipFrame.setLocation(70, 30);
            Container theContentPane = signalMastsAtSlipFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel11 = new JPanel(new FlowLayout());
            if (slipMastFromMenu) {
                JLabel blockANameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 1 "
                        + Bundle.getMessage("Name") + " : " + layoutSlip.getBlockName());

                panel11.add(blockANameLabel);

                slipSignalMastA.setTextField(layoutSlip.getSignalAMastName());
                slipSignalMastB.setTextField(layoutSlip.getSignalBMastName());
                slipSignalMastC.setTextField(layoutSlip.getSignalCMastName());
                slipSignalMastD.setTextField(layoutSlip.getSignalDMastName());
            } else {
                JLabel blockANameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 1 "
                        + Bundle.getMessage("Name") + " : ");
                panel11.add(blockANameLabel);
                panel11.add(xingBlockAComboBox);
                xingBlockAComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            }
            theContentPane.add(panel11);

            JPanel panel12 = new JPanel(new FlowLayout());
            if (slipMastFromMenu) {
                JLabel blockCNameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 2 "
                        + Bundle.getMessage("Name") + " : " + layoutSlip.getBlockName());

                panel12.add(blockCNameLabel);
            }

            theContentPane.add(panel12);
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

            slipSignalMastA.getDetailsPanel().setBackground(new Color(255, 255, 200));
            slipSignalMastB.getDetailsPanel().setBackground(new Color(200, 255, 255));
            slipSignalMastC.getDetailsPanel().setBackground(new Color(200, 200, 255));
            slipSignalMastD.getDetailsPanel().setBackground(new Color(255, 200, 200));

            signalMastLayoutSlipPanel.setLayout(new GridLayout(0, 2));

            slipSignalMastA.setBoundaryLabel(slipBlocks[0]);
            slipSignalMastB.setBoundaryLabel(slipBlocks[1]);
            slipSignalMastC.setBoundaryLabel(slipBlocks[2]);
            slipSignalMastD.setBoundaryLabel(slipBlocks[3]);

            if (slipBlocks[0] != null) {
                signalMastLayoutSlipPanel.add(slipSignalMastA.getDetailsPanel());
            }
            if (slipBlocks[1] != null) {
                signalMastLayoutSlipPanel.add(slipSignalMastB.getDetailsPanel());
            }
            if (slipBlocks[2] != null) {
                signalMastLayoutSlipPanel.add(slipSignalMastC.getDetailsPanel());
            }
            if (slipBlocks[3] != null) {
                signalMastLayoutSlipPanel.add(slipSignalMastD.getDetailsPanel());
            }

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
            signalMastsAtSlipFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSlipSignalMastsCancelPressed(null);
                }
            });
            if (slipMastFromMenu) {
                slipSignalMastsGetSaved(null);
            }
        }
        refreshSignalMastAtSlipComboBox();
        signalMastsAtSlipFrame.setPreferredSize(null);
        signalMastsAtSlipFrame.pack();
        signalMastsAtSlipFrame.setVisible(true);
        setSignalMastsAtSlipOpen = true;
    }

    void refreshSignalMastAtSlipComboBox() {
        slipSignalMastsGetSaved(null);
        createListUsedSignalMasts();
        usedMasts.remove(slipSignalMastA.getBean());
        usedMasts.remove(slipSignalMastB.getBean());
        usedMasts.remove(slipSignalMastC.getBean());
        usedMasts.remove(slipSignalMastD.getBean());
        slipSignalMastA.getCombo().excludeItems(usedMasts);
        slipSignalMastB.getCombo().excludeItems(usedMasts);
        slipSignalMastC.getCombo().excludeItems(usedMasts);
        slipSignalMastD.getCombo().excludeItems(usedMasts);
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

        boolean boundary = false;
        signalMastLayoutSlipPanel.remove(slipSignalMastA.getDetailsPanel());
        signalMastLayoutSlipPanel.remove(slipSignalMastB.getDetailsPanel());
        signalMastLayoutSlipPanel.remove(slipSignalMastC.getDetailsPanel());
        signalMastLayoutSlipPanel.remove(slipSignalMastD.getDetailsPanel());
        if (slipBlocks[0] != null) {
            signalMastLayoutSlipPanel.add(slipSignalMastA.getDetailsPanel());
            boundary = true;
        }
        if (slipBlocks[1] != null) {
            signalMastLayoutSlipPanel.add(slipSignalMastB.getDetailsPanel());
            boundary = true;
        }
        if (slipBlocks[2] != null) {
            signalMastLayoutSlipPanel.add(slipSignalMastC.getDetailsPanel());
            boundary = true;
        }
        if (slipBlocks[3] != null) {
            signalMastLayoutSlipPanel.add(slipSignalMastD.getDetailsPanel());
            boundary = true;
        }
        if (!boundary) {
            JOptionPane.showMessageDialog(signalMastsAtSlipFrame, "There are no block boundaries on this level crossing\nIt is therefore not possible to add Signal Masts to it");
        }
        signalMastsAtSlipFrame.setPreferredSize(null);
        signalMastsAtSlipFrame.pack();
    }

    private boolean getSlipMastInformation() {
        if (!slipMastFromMenu) {
            layoutSlip = null;
            List<LayoutSlip> layoutSlips = layoutEditor.getLayoutSlips();
            if (layoutSlips.size() <= 0) {
                JOptionPane.showMessageDialog(signalMastsAtSlipFrame,
                        Bundle.getMessage("SignalsError15"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            } else if (layoutSlips.size() == 1) {
                layoutSlip = layoutSlips.get(0);
            } else {
                LayoutBlock slipBlockA = null;
                //LayoutBlock slipBlockC = null;
                slipBlockA = getBlockFromEntry(xingBlockAComboBox);
                if (slipBlockA == null) {
                    return false;
                }

                int foundCount = 0;
                // make two block tests first
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
                    // try one block test
                    for (LayoutSlip x : layoutEditor.getLayoutSlips()) {
                        if (slipBlockA == x.getLayoutBlock()) {
                            layoutSlip = x;
                            foundCount++;
                        }
                    }
                }
                if (foundCount > 1) {
                    JOptionPane.showMessageDialog(signalMastsAtSlipFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError16"),
                                    new Object[]{" " + foundCount + " "}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (layoutSlip == null) {
                    JOptionPane.showMessageDialog(signalMastsAtSlipFrame,
                            Bundle.getMessage("SignalsError17"),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    private void setSlipSignalMastsCancelPressed(ActionEvent a) {
        setSignalMastsAtSlipOpen = false;
        signalMastsAtSlipFrame.setVisible(false);
        slipMastFromMenu = false;
    }

    private void setSlipSignalMastsDonePressed(ActionEvent a) {
        if (!getSlipMastInformation()) {
            return;
        }
        SignalMast aMast = getSignalMastFromEntry(slipSignalMastA.getText(), false, signalMastsAtSlipFrame);
        SignalMast bMast = getSignalMastFromEntry(slipSignalMastB.getText(), false, signalMastsAtSlipFrame);
        SignalMast cMast = getSignalMastFromEntry(slipSignalMastC.getText(), false, signalMastsAtSlipFrame);
        SignalMast dMast = getSignalMastFromEntry(slipSignalMastD.getText(), false, signalMastsAtSlipFrame);
        // place or update signals as requested
        if ((aMast != null) && slipSignalMastA.addToPanel()) {
            if (isSignalMastOnPanel(aMast)
                    && (aMast != layoutSlip.getSignalAMast())) {
                JOptionPane.showMessageDialog(signalMastsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError6"),
                                new Object[]{slipSignalMastA.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(layoutSlip.getSignalAMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(slipSignalMastA.getText());
                placingBlock(l, slipSignalMastA.isRightSelected(), 0.0, layoutSlip.getConnectA(), layoutTurnout.getCoordsD());
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
                JOptionPane.showMessageDialog(signalMastsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError13"),
                                new Object[]{slipSignalMastA.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (aMast == null) {
            removeSignalMastFromPanel(layoutSlip.getSignalAMast());
            layoutSlip.setSignalAMast("");
        }
        if ((bMast != null) && slipSignalMastB.addToPanel()) {
            if (isSignalMastOnPanel(bMast)
                    && (bMast != layoutSlip.getSignalBMast())) {
                JOptionPane.showMessageDialog(signalMastsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError6"),
                                new Object[]{slipSignalMastB.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(layoutSlip.getSignalBMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(slipSignalMastB.getText());
                placingBlock(l, slipSignalMastB.isRightSelected(), 0.0, layoutTurnout.getConnectB(), layoutTurnout.getCoordsB());
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
                JOptionPane.showMessageDialog(signalMastsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError13"),
                                new Object[]{slipSignalMastB.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (bMast == null) {
            removeSignalMastFromPanel(layoutSlip.getSignalBMast());
            layoutSlip.setSignalBMast("");
        }
        if ((cMast != null) && slipSignalMastC.addToPanel()) {
            if (isSignalMastOnPanel(cMast)
                    && (cMast != layoutSlip.getSignalCMast())) {
                JOptionPane.showMessageDialog(signalMastsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError6"),
                                new Object[]{slipSignalMastC.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(layoutSlip.getSignalCMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(slipSignalMastC.getText());
                placingBlock(l, slipSignalMastA.isRightSelected(), 0.0, layoutTurnout.getConnectC(), layoutTurnout.getCoordsC());
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
                JOptionPane.showMessageDialog(signalMastsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError13"),
                                new Object[]{slipSignalMastC.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (cMast == null) {
            removeSignalMastFromPanel(layoutSlip.getSignalCMast());
            layoutSlip.setSignalCMast("");
        }
        if ((dMast != null) && slipSignalMastD.addToPanel()) {
            if (isSignalMastOnPanel(dMast)
                    && (dMast != layoutSlip.getSignalDMast())) {
                JOptionPane.showMessageDialog(signalMastsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError6"),
                                new Object[]{slipSignalMastD.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSignalMastFromPanel(layoutSlip.getSignalDMast());
                SignalMastIcon l = new SignalMastIcon(layoutEditor);
                l.setSignalMast(slipSignalMastD.getText());
                placingBlock(l, slipSignalMastD.isRightSelected(), 0.0, layoutTurnout.getConnectD(), layoutTurnout.getCoordsD());
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
                JOptionPane.showMessageDialog(signalMastsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError13"),
                                new Object[]{slipSignalMastD.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (dMast == null) {
            removeSignalMastFromPanel(layoutSlip.getSignalDMast());
            layoutSlip.setSignalDMast("");
        }
        // setup logic if requested
        // finish up
        setSignalMastsAtSlipOpen = false;
        signalMastsAtSlipFrame.setVisible(false);
        slipMastFromMenu = false;
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }

    // operational variables for Set SignalMast at Level Crossing tool
    private JmriJFrame signalMastsAtXingFrame = null;
    private boolean setSignalMastsAtXingOpen = false;

    private JmriBeanComboBox xingBlockAComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox xingBlockCComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JButton getSavedXingSignalMasts = null;
    private JButton setXingSignalMastsDone = null;
    private JButton setXingSignalMastsCancel = null;

    private boolean xingMastFromMenu = false;
    private String[] xingBlocks = new String[4];

    BeanDetails xingSignalMastA;
    BeanDetails xingSignalMastB;
    BeanDetails xingSignalMastC;
    BeanDetails xingSignalMastD;

    JPanel signalMastLevelXingPanel = new JPanel(new FlowLayout());

    Border blackline = BorderFactory.createLineBorder(Color.black);

    // display dialog for Set Signals at Level Crossing tool
    public void setSignalMastsAtLevelXingFromMenu(@Nonnull LevelXing xing,
            @Nonnull String[] blocks,
            @Nonnull JFrame theFrame) {
        xingMastFromMenu = true;
        levelXing = xing;
        xingBlockAComboBox.setText(levelXing.getBlockNameAC());
        xingBlockCComboBox.setText(levelXing.getBlockNameBD());
        xingBlocks = new String[4];
        for (int i = 0; i < blocks.length; i++) {
            xingBlocks[i] = blocks[i];
        }
        setSignalMastsAtLevelXing(theFrame);
        return;
    }

    public void setSignalMastsAtLevelXing(@Nonnull JFrame theFrame) {
        signalFrame = theFrame;
        if (setSignalMastsAtXingOpen) {
            xingSignalMastsGetSaved(null);
            signalMastsAtXingFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (signalMastsAtXingFrame == null) {
            xingSignalMastA = new BeanDetails("SignalMast", InstanceManager.getDefault(SignalHeadManager.class));
            xingSignalMastB = new BeanDetails("SignalMast", InstanceManager.getDefault(SignalHeadManager.class));
            xingSignalMastC = new BeanDetails("SignalMast", InstanceManager.getDefault(SignalHeadManager.class));
            xingSignalMastD = new BeanDetails("SignalMast", InstanceManager.getDefault(SignalHeadManager.class));

            signalMastsAtXingFrame = new JmriJFrame(Bundle.getMessage("SignalMastsAtLevelXing"), false, true);
            signalMastsAtXingFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtLevelXing", true);
            signalMastsAtXingFrame.setLocation(70, 30);
            Container theContentPane = signalMastsAtXingFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel11 = new JPanel(new FlowLayout());
            if (xingMastFromMenu) {
                JLabel blockANameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 1 "
                        + Bundle.getMessage("Name") + " : " + levelXing.getBlockNameAC());

                panel11.add(blockANameLabel);

                xingSignalMastA.setTextField(levelXing.getSignalAMastName());
                xingSignalMastB.setTextField(levelXing.getSignalBMastName());
                xingSignalMastC.setTextField(levelXing.getSignalCMastName());
                xingSignalMastD.setTextField(levelXing.getSignalDMastName());
            } else {
                JLabel blockANameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 1 "
                        + Bundle.getMessage("Name") + " : ");
                panel11.add(blockANameLabel);
                panel11.add(xingBlockAComboBox);
                xingBlockAComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            }
            theContentPane.add(panel11);

            JPanel panel12 = new JPanel(new FlowLayout());
            if (xingMastFromMenu) {
                JLabel blockCNameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 2 "
                        + Bundle.getMessage("Name") + " : " + levelXing.getBlockNameBD());

                panel12.add(blockCNameLabel);
            } else {
                JLabel blockCNameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 2 "
                        + Bundle.getMessage("Name") + " : ");
                panel12.add(blockCNameLabel);
                panel12.add(xingBlockCComboBox);
                xingBlockCComboBox.setToolTipText(Bundle.getMessage("SignalsBlockNameHint"));
            }
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

            xingSignalMastA.getDetailsPanel().setBackground(new Color(255, 255, 200));
            xingSignalMastB.getDetailsPanel().setBackground(new Color(200, 255, 255));
            xingSignalMastC.getDetailsPanel().setBackground(new Color(200, 200, 255));
            xingSignalMastD.getDetailsPanel().setBackground(new Color(255, 200, 200));

            signalMastLevelXingPanel.setLayout(new GridLayout(0, 2));

            xingSignalMastA.setBoundaryLabel(xingBlocks[0]);
            xingSignalMastB.setBoundaryLabel(xingBlocks[1]);
            xingSignalMastC.setBoundaryLabel(xingBlocks[2]);
            xingSignalMastD.setBoundaryLabel(xingBlocks[3]);

            if (xingBlocks[0] != null) {
                signalMastLevelXingPanel.add(xingSignalMastA.getDetailsPanel());
            }
            if (xingBlocks[1] != null) {
                signalMastLevelXingPanel.add(xingSignalMastB.getDetailsPanel());
            }
            if (xingBlocks[2] != null) {
                signalMastLevelXingPanel.add(xingSignalMastC.getDetailsPanel());
            }
            if (xingBlocks[3] != null) {
                signalMastLevelXingPanel.add(xingSignalMastD.getDetailsPanel());
            }

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
            signalMastsAtXingFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setXingSignalMastsCancelPressed(null);
                }
            });
            if (xingMastFromMenu) {
                xingSignalMastsGetSaved(null);
            }
        }
        refreshSignalMastAtXingComboBox();
        signalMastsAtXingFrame.setPreferredSize(null);
        signalMastsAtXingFrame.pack();
        signalMastsAtXingFrame.setVisible(true);
        setSignalMastsAtXingOpen = true;
    }

    void refreshSignalMastAtXingComboBox() {
        xingSignalMastsGetSaved(null);
        createListUsedSignalMasts();

        usedMasts.remove(xingSignalMastA.getBean());
        usedMasts.remove(xingSignalMastB.getBean());
        usedMasts.remove(xingSignalMastC.getBean());
        usedMasts.remove(xingSignalMastD.getBean());

        xingSignalMastA.getCombo().excludeItems(usedMasts);
        xingSignalMastB.getCombo().excludeItems(usedMasts);
        xingSignalMastC.getCombo().excludeItems(usedMasts);
        xingSignalMastD.getCombo().excludeItems(usedMasts);
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

        boolean boundary = false;
        signalMastLevelXingPanel.remove(xingSignalMastA.getDetailsPanel());
        signalMastLevelXingPanel.remove(xingSignalMastB.getDetailsPanel());
        signalMastLevelXingPanel.remove(xingSignalMastC.getDetailsPanel());
        signalMastLevelXingPanel.remove(xingSignalMastD.getDetailsPanel());
        if (xingBlocks[0] != null) {
            signalMastLevelXingPanel.add(xingSignalMastA.getDetailsPanel());
            boundary = true;
        }
        if (xingBlocks[1] != null) {
            signalMastLevelXingPanel.add(xingSignalMastB.getDetailsPanel());
            boundary = true;
        }
        if (xingBlocks[2] != null) {
            signalMastLevelXingPanel.add(xingSignalMastC.getDetailsPanel());
            boundary = true;
        }
        if (xingBlocks[3] != null) {
            signalMastLevelXingPanel.add(xingSignalMastD.getDetailsPanel());
            boundary = true;
        }
        if (!boundary) {
            JOptionPane.showMessageDialog(signalMastsAtXingFrame, "There are no block boundaries on this level crossing\nIt is therefore not possible to add Signal Masts to it");
        }
        signalMastsAtXingFrame.setPreferredSize(null);
        signalMastsAtXingFrame.pack();
    }

    private boolean getLevelCrossingMastInformation() {
        if (!xingMastFromMenu) {
            levelXing = null;
            List<LevelXing> levelXings = layoutEditor.getLevelXings();
            if (levelXings.size() <= 0) {
                JOptionPane.showMessageDialog(signalMastsAtXingFrame,
                        Bundle.getMessage("SignalsError15"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            } else if (levelXings.size() == 1) {
                levelXing = levelXings.get(0);
            } else {
                LayoutBlock xingBlockA = null;
                LayoutBlock xingBlockC = null;
                xingBlockA = getBlockFromEntry(xingBlockAComboBox);
                if (xingBlockA == null) {
                    return false;
                }

                String theBlockName = xingBlockCComboBox.getDisplayName();
                if ((theBlockName != null) && !theBlockName.isEmpty()) {
                    xingBlockC = getBlockFromEntry(xingBlockCComboBox);
                    if (xingBlockC == null) {
                        return false;
                    }
                }

                int foundCount = 0;
                // make two block tests first
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
                    // try one block test
                    for (LevelXing x : layoutEditor.getLevelXings()) {
                        if ((xingBlockA == x.getLayoutBlockAC()) || (xingBlockA == x.getLayoutBlockBD())) {
                            levelXing = x;
                            foundCount++;
                        }
                    }
                }
                if (foundCount > 1) {
                    JOptionPane.showMessageDialog(signalMastsAtXingFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError16"),
                                    new Object[]{" " + foundCount + " "}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (levelXing == null) {
                    JOptionPane.showMessageDialog(signalMastsAtXingFrame,
                            Bundle.getMessage("SignalsError17"),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    private void setXingSignalMastsCancelPressed(ActionEvent a) {
        setSignalMastsAtXingOpen = false;
        signalMastsAtXingFrame.setVisible(false);
        xingMastFromMenu = false;
    }

    private void setXingSignalMastsDonePressed(ActionEvent a) {
        if (!getLevelCrossingMastInformation()) {
            return;
        }
        SignalMast aMast = getSignalMastFromEntry(xingSignalMastA.getText(), false, signalMastsAtXingFrame);
        SignalMast bMast = getSignalMastFromEntry(xingSignalMastB.getText(), false, signalMastsAtXingFrame);
        SignalMast cMast = getSignalMastFromEntry(xingSignalMastC.getText(), false, signalMastsAtXingFrame);
        SignalMast dMast = getSignalMastFromEntry(xingSignalMastD.getText(), false, signalMastsAtXingFrame);
        //if ( !getXingSignalMastInformation() ) return;
        // place or update signals as requested
        if ((aMast != null) && xingSignalMastA.addToPanel()) {
            if (isSignalMastOnPanel(aMast)
                    && (aMast != levelXing.getSignalAMast())) {
                JOptionPane.showMessageDialog(signalMastsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError6"),
                                new Object[]{xingSignalMastA.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(signalMastsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError13"),
                                new Object[]{xingSignalMastA.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (aMast == null) {
            removeSignalMastFromPanel(levelXing.getSignalAMast());
            levelXing.setSignalAMast("");
        }
        if ((bMast != null) && xingSignalMastB.addToPanel()) {
            if (isSignalMastOnPanel(bMast)
                    && (bMast != levelXing.getSignalBMast())) {
                JOptionPane.showMessageDialog(signalMastsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError6"),
                                new Object[]{xingSignalMastB.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(signalMastsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError13"),
                                new Object[]{xingSignalMastB.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (bMast == null) {
            removeSignalMastFromPanel(levelXing.getSignalBMast());
            levelXing.setSignalBMast("");
        }
        if ((cMast != null) && xingSignalMastC.addToPanel()) {
            if (isSignalMastOnPanel(cMast)
                    && (cMast != levelXing.getSignalCMast())) {
                JOptionPane.showMessageDialog(signalMastsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError6"),
                                new Object[]{xingSignalMastC.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(signalMastsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError13"),
                                new Object[]{xingSignalMastC.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (cMast == null) {
            removeSignalMastFromPanel(levelXing.getSignalCMast());
            levelXing.setSignalCName("");
        }
        if ((dMast != null) && xingSignalMastD.addToPanel()) {
            if (isSignalMastOnPanel(dMast)
                    && (dMast != levelXing.getSignalDMast())) {
                JOptionPane.showMessageDialog(signalMastsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError6"),
                                new Object[]{xingSignalMastD.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(signalMastsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SignalMastsError13"),
                                new Object[]{xingSignalMastD.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (dMast == null) {
            removeSignalMastFromPanel(levelXing.getSignalDMast());
            levelXing.setSignalDMast("");
        }
        // setup logic if requested
        // finish up
        setSignalMastsAtXingOpen = false;
        signalMastsAtXingFrame.setVisible(false);
        xingMastFromMenu = false;
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }

    boolean setSensorsOpen = false;
    boolean turnoutSensorFromMenu = false;
    private JmriJFrame setSensorsFrame = null;
    private JFrame turnoutSensorFrame = null;

    private JmriBeanComboBox sensorsTurnoutComboBox = new JmriBeanComboBox(
            InstanceManager.turnoutManagerInstance(),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JButton setSensorsDone;
    private JButton getSavedSensors;
    private JButton setSensorsCancel;
    private JButton changeSensorIcon = null;

    private String[] turnoutSenBlocks = new String[4];

    BeanDetails turnoutSensorA;
    BeanDetails turnoutSensorB;
    BeanDetails turnoutSensorC;
    BeanDetails turnoutSensorD;

    JPanel sensorTurnoutPanel = new JPanel(new FlowLayout());

    public void setSensorsAtTurnoutFromMenu(@Nonnull LayoutTurnout to,
            @Nonnull String[] blocks,
            @Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame frame) {
        turnoutSensorFromMenu = true;
        sensorIconEditor = theEditor;
        layoutTurnout = to;
        turnout = to.getTurnout();
        sensorsTurnoutComboBox.setText(to.getTurnoutName());
        turnoutSenBlocks = new String[4];
        for (int i = 0; i < blocks.length; i++) {
            turnoutSenBlocks[i] = blocks[i];
        }
        setSensorsAtTurnouts(frame);
    }

    public void setSensorsAtTurnouts(@Nonnull JFrame frame) {
        turnoutSensorFrame = frame;
        if (setSensorsOpen) {
            turnoutSensorsGetSaved(null);
            setSensorsFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (setSensorsFrame == null) {
            setSensorsFrame = new JmriJFrame(Bundle.getMessage("SensorsAtTurnout"), false, true);
            setSensorsFrame.addHelpMenu("package.jmri.jmrit.display.SetSensorsAtTurnout", true);
            setSensorsFrame.setLocation(70, 30);
            Container theContentPane = setSensorsFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel1 = new JPanel(new FlowLayout());
            turnoutSensorA = new BeanDetails("Sensor", InstanceManager.sensorManagerInstance());
            turnoutSensorB = new BeanDetails("Sensor", InstanceManager.sensorManagerInstance());
            turnoutSensorC = new BeanDetails("Sensor", InstanceManager.sensorManagerInstance());
            turnoutSensorD = new BeanDetails("Sensor", InstanceManager.sensorManagerInstance());

            if (turnoutSensorFromMenu) {
                JLabel turnoutSensorNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " "
                        + Bundle.getMessage("Name") + " : " + layoutTurnout.getTurnoutName());
                panel1.add(turnoutSensorNameLabel);

                turnoutSensorA.setTextField(layoutTurnout.getSensorAName());
                turnoutSensorB.setTextField(layoutTurnout.getSensorBName());
                turnoutSensorC.setTextField(layoutTurnout.getSensorCName());
                turnoutSensorD.setTextField(layoutTurnout.getSensorDName());
            } else {
                JLabel turnoutSensorNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " "
                        + Bundle.getMessage("Name"));
                panel1.add(turnoutSensorNameLabel);
                panel1.add(sensorsTurnoutComboBox);
                sensorsTurnoutComboBox.setToolTipText(Bundle.getMessage("SensorsTurnoutNameHint"));
            }
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

            turnoutSensorA.getDetailsPanel().setBackground(new Color(255, 255, 200));

            turnoutSensorB.getDetailsPanel().setBackground(new Color(200, 255, 255));

            turnoutSensorC.getDetailsPanel().setBackground(new Color(200, 200, 255));

            turnoutSensorD.getDetailsPanel().setBackground(new Color(255, 200, 200));

            sensorTurnoutPanel.setLayout(new GridLayout(0, 2));

            turnoutSensorA.setBoundaryLabel(turnoutSenBlocks[0]);
            turnoutSensorB.setBoundaryLabel(turnoutSenBlocks[1]);
            turnoutSensorC.setBoundaryLabel(turnoutSenBlocks[2]);
            turnoutSensorD.setBoundaryLabel(turnoutSenBlocks[3]);

            if (turnoutSenBlocks[0] != null) {
                sensorTurnoutPanel.add(turnoutSensorA.getDetailsPanel());
            }
            if (turnoutSenBlocks[1] != null) {
                sensorTurnoutPanel.add(turnoutSensorB.getDetailsPanel());
            }
            if (turnoutSenBlocks[2] != null) {
                sensorTurnoutPanel.add(turnoutSensorC.getDetailsPanel());
            }
            if (turnoutSenBlocks[3] != null) {
                sensorTurnoutPanel.add(turnoutSensorD.getDetailsPanel());
            }
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
            setSensorsFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSensorsCancelPressed(null);
                }
            });
        }
        if (turnoutFromMenu) {
            turnoutSensorsGetSaved(null);
        }
        setSensorsFrame.setPreferredSize(null);
        setSensorsFrame.pack();
        setSensorsFrame.setVisible(true);
        setSensorsOpen = true;
    }

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

        boolean blockBoundary = false;
        if (turnoutSenBlocks[0] != null) {
            sensorTurnoutPanel.add(turnoutSensorA.getDetailsPanel());
            blockBoundary = true;
        }
        if (turnoutSenBlocks[1] != null) {
            sensorTurnoutPanel.add(turnoutSensorB.getDetailsPanel());
            blockBoundary = true;
        }
        if (turnoutSenBlocks[2] != null) {
            sensorTurnoutPanel.add(turnoutSensorC.getDetailsPanel());
            blockBoundary = true;
        }
        if (turnoutSenBlocks[3] != null) {
            sensorTurnoutPanel.add(turnoutSensorD.getDetailsPanel());
            blockBoundary = true;
        }
        if (!blockBoundary) {
            JOptionPane.showMessageDialog(setSensorsFrame, "There are no block boundaries on this turnout\nIt is therefore not possible to add Sensors to it");
        }
        setSensorsFrame.setPreferredSize(null);
        setSensorsFrame.pack();
    }

    private int isSensorAssignedHere(Sensor sensor, LayoutTurnout lTurnout) {
        if ((sensor == null) || (lTurnout == null)) {
            return NONE;
        }
        String sysName = sensor.getSystemName();
        String uName = sensor.getUserName();
        String name = lTurnout.getSensorAName();
        if (!name.isEmpty() && name.equals(uName) || name.equals(sysName)) {
            return A1;
        }
        name = lTurnout.getSensorBName();
        if (!name.isEmpty() && name.equals(uName) || name.equals(sysName)) {
            return A2;
        }
        name = lTurnout.getSensorCName();
        if (!name.isEmpty() && name.equals(uName) || name.equals(sysName)) {
            return A3;
        }
        name = lTurnout.getSensorDName();
        if (!name.isEmpty() && name.equals(uName) || name.equals(sysName)) {
            return B1;
        }
        return NONE;
    }

    public void removeAssignment(@Nonnull Sensor sensor) {
        for (LayoutTurnout to : layoutEditor.getLayoutTurnouts()) {
            if ((to.getSensorA() != null) && to.getSensorA() == sensor) {
                to.setSensorA(null);
            }
            if ((to.getSensorB() != null) && to.getSensorB() == sensor) {
                to.setSensorB(null);
            }
            if ((to.getSensorC() != null) && to.getSensorC() == sensor) {
                to.setSensorC(null);
            }
            if ((to.getSensorD() != null) && to.getSensorD() == sensor) {
                to.setSensorD(null);
            }
        }
        for (LayoutSlip to : layoutEditor.getLayoutSlips()) {
            if ((to.getSensorA() != null) && to.getSensorA() == sensor) {
                to.setSensorA(null);
            }
            if ((to.getSensorB() != null) && to.getSensorB() == sensor) {
                to.setSensorB(null);
            }
            if ((to.getSensorC() != null) && to.getSensorC() == sensor) {
                to.setSensorC(null);
            }
            if ((to.getSensorD() != null) && to.getSensorD() == sensor) {
                to.setSensorD(null);
            }
        }

        for (PositionablePoint po : layoutEditor.getPositionablePoints()) {
            if ((po.getEastBoundSensor() != null) && po.getEastBoundSensor() == sensor) {
                po.setEastBoundSensor(null);
            }
            if ((po.getWestBoundSensor() != null) && po.getWestBoundSensor() == sensor) {
                po.setWestBoundSensor(null);
            }
        }

        for (LevelXing x : layoutEditor.getLevelXings()) {
            if ((x.getSensorA() != null) && x.getSensorA() == sensor) {
                x.setSensorAName(null);
            }
            if ((x.getSensorB() != null) && x.getSensorB() == sensor) {
                x.setSensorBName(null);
            }
            if ((x.getSensorC() != null) && x.getSensorC() == sensor) {
                x.setSensorCName(null);
            }
            if ((x.getSensorD() != null) && x.getSensorD() == sensor) {
                x.setSensorDName(null);
            }
        }
    }

    SensorIcon turnoutSensorBlockIcon;

    private void setSensorsDonePressed(ActionEvent a) {
        //Placing of turnouts needs to be better handled
        // process turnout name
        if (!getTurnoutSensorInformation()) {
            return;
        }
        // process signal head names
        //if ( !getSensorTurnoutInformation() ) return;
        Sensor sensorA = getSensorFromEntry(turnoutSensorA.getText(), false, setSensorsFrame);
        //if (turnoutSensor==null) return false;
        Sensor sensorB = getSensorFromEntry(turnoutSensorB.getText(), false, setSensorsFrame);
        //if (turnoutSensorB==null) return false;
        Sensor sensorC = getSensorFromEntry(turnoutSensorC.getText(), false, setSensorsFrame);
        //if (turnoutSensorC==null) return false;
        Sensor sensorD = getSensorFromEntry(turnoutSensorD.getText(), false, setSensorsFrame);
        // place signals as requested
        if (turnoutSensorA.addToPanel()) {
            if (isSensorOnPanel(sensorA)
                    && (sensorA != layoutTurnout.getSensorA())) {
                JOptionPane.showMessageDialog(setSensorsFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError6"),
                                new Object[]{turnoutSensorA.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(layoutTurnout.getSensorA());
                placingBlock(getSensorIcon(turnoutSensorA.getText()), turnoutSensorA.isRightSelected(), 0.0, layoutTurnout.getConnectA(), layoutTurnout.getCoordsA());
                removeAssignment(sensorA);
                layoutTurnout.setSensorA(turnoutSensorA.getText());
                needRedraw = true;
            }
        } else if (sensorA != null) {
            int assigned = isSensorAssignedHere(sensorA, layoutTurnout);
            if (assigned == NONE) {
                if (isSensorOnPanel(sensorA)
                        && isSensorAssignedAnywhere(sensorA)) {
                    JOptionPane.showMessageDialog(setSensorsFrame,
                            MessageFormat.format(Bundle.getMessage("SensorsError8"),
                                    new Object[]{turnoutSensorA.getText()}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSensorFromPanel(layoutTurnout.getSensorA());
                    removeAssignment(sensorA);
                    layoutTurnout.setSensorA(turnoutSensorA.getText());
                }
                //} else if (assigned != A1) {
                // need to figure out what to do in this case.
            }
        } else {
            removeSensorFromPanel(layoutTurnout.getSensorA());
            layoutTurnout.setSensorA("");
        }
        if ((turnoutSensorB != null) && (turnoutSensorB.addToPanel())) {
            if (isSensorOnPanel(sensorB)
                    && (sensorB != layoutTurnout.getSensorB())) {
                JOptionPane.showMessageDialog(setSensorsFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError6"),
                                new Object[]{turnoutSensorB.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(layoutTurnout.getSensorB());

                placingBlock(getSensorIcon(turnoutSensorB.getText()), turnoutSensorB.isRightSelected(), 0.0, layoutTurnout.getConnectB(), layoutTurnout.getCoordsB());
                removeAssignment(sensorB);
                layoutTurnout.setSensorB(turnoutSensorB.getText());
                needRedraw = true;
            }
        } else if (sensorB != null) {
            int assigned = isSensorAssignedHere(sensorB, layoutTurnout);
            if (assigned == NONE) {
                if (isSensorOnPanel(sensorB)
                        && isSensorAssignedAnywhere(sensorB)) {
                    JOptionPane.showMessageDialog(setSensorsFrame,
                            MessageFormat.format(Bundle.getMessage("SensorsError8"),
                                    new Object[]{turnoutSensorB.getText()}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSensorFromPanel(layoutTurnout.getSensorB());
                    removeAssignment(sensorB);
                    layoutTurnout.setSensorB(turnoutSensorB.getText());
                }
                //} else if (assigned != A2) {
                // need to figure out what to do in this case.
            }
        } else {
            removeSensorFromPanel(layoutTurnout.getSensorB());
            layoutTurnout.setSensorB("");
        }
        if (sensorC != null) {
            if (turnoutSensorC.addToPanel()) {
                if (isSensorOnPanel(sensorC)
                        && (sensorC != layoutTurnout.getSensorC())) {
                    JOptionPane.showMessageDialog(setSensorsFrame,
                            MessageFormat.format(Bundle.getMessage("SensorsError6"),
                                    new Object[]{turnoutSensorC.getText()}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSensorFromPanel(layoutTurnout.getSensorC());

                    placingBlock(getSensorIcon(turnoutSensorC.getText()), turnoutSensorC.isRightSelected(), 0.0, layoutTurnout.getConnectC(), layoutTurnout.getCoordsC());
                    removeAssignment(sensorC);
                    layoutTurnout.setSensorC(turnoutSensorC.getText());
                    needRedraw = true;
                }
            } else {
                int assigned = isSensorAssignedHere(sensorC, layoutTurnout);
                if (assigned == NONE) {
                    if (isSensorOnPanel(sensorC)
                            && isSensorAssignedAnywhere(sensorC)) {
                        JOptionPane.showMessageDialog(setSensorsFrame,
                                MessageFormat.format(Bundle.getMessage("SensorsError8"),
                                        new Object[]{turnoutSensorC.getText()}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        removeSensorFromPanel(layoutTurnout.getSensorC());
                        removeAssignment(sensorC);
                        layoutTurnout.setSensorC(turnoutSensorC.getText());
                    }
                    //} else if (assigned != A3) {
                    // need to figure out what to do in this case.
                }
            }
        } else {
            removeSensorFromPanel(layoutTurnout.getSensorC());
            layoutTurnout.setSensorC("");
        }
        if (sensorD != null) {
            if (turnoutSensorD.addToPanel()) {
                if (isSensorOnPanel(sensorD)
                        && (sensorD != layoutTurnout.getSensorD())) {
                    String signalHeadName = divergingSignalHeadComboBox.getDisplayName();
                    JOptionPane.showMessageDialog(setSensorsFrame,
                            MessageFormat.format(Bundle.getMessage("SensorsError6"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSensorFromPanel(layoutTurnout.getSensorD());
                    placingBlock(getSensorIcon(turnoutSensorD.getText()), turnoutSensorD.isRightSelected(), 0.0, layoutTurnout.getConnectD(), layoutTurnout.getCoordsD());
                    removeAssignment(sensorD);
                    layoutTurnout.setSensorD(turnoutSensorD.getText());
                    needRedraw = true;
                }
            } else {
                int assigned = isSensorAssignedHere(sensorD, layoutTurnout);
                if (assigned == NONE) {
                    if (isSensorOnPanel(sensorD)
                            && isSensorAssignedAnywhere(sensorD)) {
                        JOptionPane.showMessageDialog(setSensorsFrame,
                                MessageFormat.format(Bundle.getMessage("SensorsError8"),
                                        new Object[]{turnoutSensorD.getText()}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        removeSensorFromPanel(layoutTurnout.getSensorD());
                        removeAssignment(sensorD);
                        layoutTurnout.setSensorD(turnoutSensorD.getText());
                    }
                    //} else if (assigned != B1) {
                    // need to figure out what to do in this case.
                }
            }
        } else {
            removeSensorFromPanel(layoutTurnout.getSensorD());
            layoutTurnout.setSensorD("");
        }

        // make sure this layout turnout is not linked to another
        layoutTurnout.setLinkType(LayoutTurnout.NO_LINK);
        layoutTurnout.setLinkedTurnoutName("");
        // finish up
        setSensorsOpen = false;
        turnoutFromMenu = false;
        setSensorsFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }

    private boolean getTurnoutSensorInformation() {
        turnout = null;
        layoutTurnout = null;
        String str = sensorsTurnoutComboBox.getDisplayName();
        if ((str == null) || str.isEmpty()) {
            JOptionPane.showMessageDialog(setSensorsFrame, Bundle.getMessage("SensorsError1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        turnout = InstanceManager.turnoutManagerInstance().getTurnout(str);
        if (turnout == null) {
            JOptionPane.showMessageDialog(setSensorsFrame,
                    MessageFormat.format(Bundle.getMessage("SensorsError2"),
                            new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            String uname = turnout.getUserName();
            if ((uname == null) || uname.isEmpty()
                    || !uname.equals(str)) {
                sensorsTurnoutComboBox.setText(str);
            }
        }
        for (LayoutTurnout t : layoutEditor.getLayoutTurnouts()) {
            if (t.getTurnout() == turnout) {
                layoutTurnout = t;
            }
        }

        LayoutTurnout t = layoutTurnout;
        if (t == null) {
            JOptionPane.showMessageDialog(setSensorsFrame,
                    MessageFormat.format(Bundle.getMessage("SensorsError3"),
                            new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void setSensorsCancelPressed(ActionEvent a) {
        setSensorsOpen = false;
        turnoutSensorFromMenu = false;
        setSensorsFrame.setVisible(false);
    }

    // operational variables for Set Sensors at Level Crossing tool
    private JmriJFrame sensorsAtXingFrame = null;
    private boolean setSensorsAtXingOpen = false;

    private JmriBeanComboBox xingSensorsBlockAComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox xingSensorsBlockCComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JButton getSavedXingSensors = null;
    private JButton setXingSensorsDone = null;
    private JButton setXingSensorsCancel = null;
    private JButton changeSensorXingIcon = null;
    JFrame sensorXingFrame = null;

    private boolean xingSensorFromMenu = false;
    private String[] xingSensorBlocks = new String[4];

    BeanDetails xingSensorA;
    BeanDetails xingSensorB;
    BeanDetails xingSensorC;
    BeanDetails xingSensorD;

    JPanel sensorXingPanel = new JPanel(new FlowLayout());

    // display dialog for Set Signals at Level Crossing tool
    public void setSensorsAtLevelXingFromMenu(@Nonnull LevelXing xing,
            @Nonnull String[] blocks,
            @Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        xingSensorFromMenu = true;
        levelXing = xing;
        xingSensorsBlockAComboBox.setText(levelXing.getBlockNameAC());
        xingSensorsBlockCComboBox.setText(levelXing.getBlockNameBD());
        for (int i = 0; i < blocks.length; i++) {
            xingSensorBlocks[i] = blocks[i];
        }
        setSensorsAtLevelXing(theEditor, theFrame);
        return;
    }

    public void setSensorsAtLevelXing(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        sensorIconEditor = theEditor;
        sensorXingFrame = theFrame;
        if (setSensorsAtXingOpen) {
            xingSensorsGetSaved(null);
            sensorsAtXingFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (sensorsAtXingFrame == null) {
            sensorsAtXingFrame = new JmriJFrame(Bundle.getMessage("SensorsAtLevelXing"), false, true);
            sensorsAtXingFrame.addHelpMenu("package.jmri.jmrit.display.SetSensorsAtLevelXing", true);
            sensorsAtXingFrame.setLocation(70, 30);
            Container theContentPane = sensorsAtXingFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel11 = new JPanel(new FlowLayout());
            xingSensorA = new BeanDetails("Sensor", InstanceManager.sensorManagerInstance());
            xingSensorB = new BeanDetails("Sensor", InstanceManager.sensorManagerInstance());
            xingSensorC = new BeanDetails("Sensor", InstanceManager.sensorManagerInstance());
            xingSensorD = new BeanDetails("Sensor", InstanceManager.sensorManagerInstance());
            if (xingSensorFromMenu) {
                JLabel blockANameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 1 "
                        + Bundle.getMessage("Name") + " : " + levelXing.getBlockNameAC());

                panel11.add(blockANameLabel);

                xingSensorA.setTextField(levelXing.getSensorAName());
                xingSensorB.setTextField(levelXing.getSensorBName());
                xingSensorC.setTextField(levelXing.getSensorCName());
                xingSensorD.setTextField(levelXing.getSensorDName());
            } else {
                JLabel blockANameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 1 "
                        + Bundle.getMessage("Name") + " : ");
                panel11.add(blockANameLabel);
                panel11.add(xingSensorsBlockAComboBox);
                xingSensorsBlockAComboBox.setToolTipText(Bundle.getMessage("SensorsBlockNameHint"));
            }
            theContentPane.add(panel11);

            JPanel panel12 = new JPanel(new FlowLayout());
            if (xingSensorFromMenu) {
                JLabel blockCNameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 2 "
                        + Bundle.getMessage("Name") + " : " + levelXing.getBlockNameBD());

                panel12.add(blockCNameLabel);
            } else {
                JLabel blockCNameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 2 "
                        + Bundle.getMessage("Name") + " : ");
                panel12.add(blockCNameLabel);
                panel12.add(xingSensorsBlockCComboBox);
                xingSensorsBlockCComboBox.setToolTipText(Bundle.getMessage("SensorsBlockNameHint"));
            }
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

            xingSensorA.getDetailsPanel().setBackground(new Color(255, 255, 200));

            xingSensorB.getDetailsPanel().setBackground(new Color(200, 255, 255));

            xingSensorC.getDetailsPanel().setBackground(new Color(200, 200, 255));

            xingSensorD.getDetailsPanel().setBackground(new Color(255, 200, 200));

            sensorXingPanel.setLayout(new GridLayout(0, 2));

            xingSensorA.setBoundaryLabel(xingSensorBlocks[0]);
            xingSensorB.setBoundaryLabel(xingSensorBlocks[1]);
            xingSensorC.setBoundaryLabel(xingSensorBlocks[2]);
            xingSensorD.setBoundaryLabel(xingSensorBlocks[3]);

            if (xingSensorBlocks[0] != null) {
                sensorXingPanel.add(xingSensorA.getDetailsPanel());
            }
            if (xingSensorBlocks[1] != null) {
                sensorXingPanel.add(xingSensorB.getDetailsPanel());
            }
            if (xingSensorBlocks[2] != null) {
                sensorXingPanel.add(xingSensorC.getDetailsPanel());
            }
            if (xingSensorBlocks[3] != null) {
                sensorXingPanel.add(xingSensorD.getDetailsPanel());
            }
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
            sensorsAtXingFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setXingSensorsCancelPressed(null);
                }
            });
        }
        if (xingSensorFromMenu) {
            xingSensorsGetSaved(null);
        }
        sensorsAtXingFrame.setPreferredSize(null);
        sensorsAtXingFrame.pack();
        sensorsAtXingFrame.setVisible(true);
        setSensorsAtXingOpen = true;
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

        boolean boundary = false;
        if (xingSensorBlocks[0] != null) {
            sensorXingPanel.add(xingSensorA.getDetailsPanel());
            boundary = true;
        }
        if (xingSensorBlocks[1] != null) {
            sensorXingPanel.add(xingSensorB.getDetailsPanel());
            boundary = true;
        }
        if (xingSensorBlocks[2] != null) {
            sensorXingPanel.add(xingSensorC.getDetailsPanel());
            boundary = true;
        }
        if (xingSensorBlocks[3] != null) {
            sensorXingPanel.add(xingSensorD.getDetailsPanel());
            boundary = true;
        }
        if (!boundary) {
            JOptionPane.showMessageDialog(sensorsAtXingFrame, Bundle.getMessage("NoBoundaryXingSensor"));
        }
        sensorsAtXingFrame.setPreferredSize(null);
        sensorsAtXingFrame.pack();
    }

    private boolean getLevelCrossingSensorInformation() {
        if (!xingSensorFromMenu) {
            levelXing = null;
            List<LevelXing> levelXings = layoutEditor.getLevelXings();
            if (levelXings.size() <= 0) {
                JOptionPane.showMessageDialog(sensorsAtXingFrame,
                        Bundle.getMessage("SignalsError15"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            } else if (levelXings.size() == 1) {
                levelXing = levelXings.get(0);
            } else {
                LayoutBlock xingSensorBlockA = null;
                LayoutBlock xingSensorBlockC = null;
                xingSensorBlockA = getBlockFromEntry(xingSensorsBlockAComboBox);
                if (xingSensorBlockA == null) {
                    return false;
                }
                String theBlockName = xingSensorsBlockCComboBox.getDisplayName();
                if ((theBlockName != null) && !theBlockName.isEmpty()) {
                    xingSensorBlockC = getBlockFromEntry(xingSensorsBlockCComboBox);
                    if (xingSensorBlockC == null) {
                        return false;
                    }
                }

                int foundCount = 0;
                // make two block tests first
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
                    // try one block test
                    for (LevelXing x : layoutEditor.getLevelXings()) {
                        if ((xingSensorBlockA == x.getLayoutBlockAC()) || (xingSensorBlockA == x.getLayoutBlockBD())) {
                            levelXing = x;
                            foundCount++;
                        }
                    }
                }
                if (foundCount > 1) {
                    JOptionPane.showMessageDialog(sensorsAtXingFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError16"),
                                    new Object[]{" " + foundCount + " "}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (levelXing == null) {
                    JOptionPane.showMessageDialog(sensorsAtXingFrame,
                            Bundle.getMessage("SignalsError17"),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    private void setXingSensorsCancelPressed(ActionEvent a) {
        setSensorsAtXingOpen = false;
        sensorsAtXingFrame.setVisible(false);
        xingSensorFromMenu = false;
    }

    private void setXingSensorsDonePressed(ActionEvent a) {
        if (!getLevelCrossingSensorInformation()) {
            return;
        }
        Sensor aSensor = getSensorFromEntry(xingSensorA.getText(), false, sensorsAtXingFrame);
        Sensor bSensor = getSensorFromEntry(xingSensorB.getText(), false, sensorsAtXingFrame);
        Sensor cSensor = getSensorFromEntry(xingSensorC.getText(), false, sensorsAtXingFrame);
        Sensor dSensor = getSensorFromEntry(xingSensorD.getText(), false, sensorsAtXingFrame);
        // place or update signals as requested
        if ((aSensor != null) && xingSensorA.addToPanel()) {
            if (isSensorOnPanel(aSensor)
                    && (aSensor != levelXing.getSensorA())) {
                JOptionPane.showMessageDialog(sensorsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError6"),
                                new Object[]{xingSensorA.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(levelXing.getSensorA());
                placingBlock(getSensorIcon(xingSensorA.getText()), xingSensorA.isRightSelected(), 0.0, levelXing.getConnectA(), levelXing.getCoordsA());
                removeAssignment(aSensor);
                levelXing.setSensorAName(xingSensorB.getText());
                needRedraw = true;
            }
        } else if ((aSensor != null)
                && (aSensor != levelXing.getSensorA())
                && (aSensor != levelXing.getSensorB())
                && (aSensor != levelXing.getSensorC())
                && (aSensor != levelXing.getSensorD())) {
            if (isSensorOnPanel(aSensor)) {
                JOptionPane.showMessageDialog(sensorsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError13"),
                                new Object[]{xingSensorA.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(levelXing.getSensorA());
                removeAssignment(aSensor);
                levelXing.setSensorAName(xingSensorA.getText());
            }
        } else if ((aSensor != null)
                && ((aSensor == levelXing.getSensorB())
                || (aSensor == levelXing.getSensorC())
                || (aSensor == levelXing.getSensorD()))) {
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (aSensor == null) {
            removeSensorFromPanel(levelXing.getSensorA());
            levelXing.setSensorAName("");
        }
        if ((bSensor != null) && xingSensorB.addToPanel()) {
            if (isSensorOnPanel(bSensor)
                    && (bSensor != levelXing.getSensorB())) {
                JOptionPane.showMessageDialog(sensorsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError6"),
                                new Object[]{xingSensorB.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(levelXing.getSensorB());
                placingBlock(getSensorIcon(xingSensorB.getText()), xingSensorB.isRightSelected(), 0.0, levelXing.getConnectB(), levelXing.getCoordsB());
                removeAssignment(bSensor);
                levelXing.setSensorBName(xingSensorB.getText());
                needRedraw = true;
            }
        } else if ((bSensor != null)
                && (bSensor != levelXing.getSensorA())
                && (bSensor != levelXing.getSensorB())
                && (bSensor != levelXing.getSensorC())
                && (bSensor != levelXing.getSensorD())) {
            if (isSensorOnPanel(bSensor)) {
                JOptionPane.showMessageDialog(sensorsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError13"),
                                new Object[]{xingSensorB.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(levelXing.getSensorB());
                removeAssignment(bSensor);
                levelXing.setSensorBName(xingSensorB.getText());
            }
        } else if ((bSensor != null)
                && ((bSensor == levelXing.getSensorA())
                || (bSensor == levelXing.getSensorC())
                || (bSensor == levelXing.getSensorD()))) {
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (bSensor == null) {
            removeSensorFromPanel(levelXing.getSensorB());
            levelXing.setSensorBName("");
        }
        if ((cSensor != null) && xingSensorC.addToPanel()) {
            if (isSensorOnPanel(cSensor)
                    && (cSensor != levelXing.getSensorC())) {
                JOptionPane.showMessageDialog(sensorsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError6"),
                                new Object[]{xingSensorC.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(levelXing.getSensorC());
                placingBlock(getSensorIcon(xingSensorC.getText()), xingSensorC.isRightSelected(), 0.0, levelXing.getConnectC(), levelXing.getCoordsC());
                removeAssignment(cSensor);
                levelXing.setSensorCName(xingSensorC.getText());
                needRedraw = true;
            }
        } else if ((cSensor != null)
                && (cSensor != levelXing.getSensorA())
                && (cSensor != levelXing.getSensorB())
                && (cSensor != levelXing.getSensorC())
                && (cSensor != levelXing.getSensorD())) {
            if (isSensorOnPanel(cSensor)) {
                JOptionPane.showMessageDialog(sensorsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError13"),
                                new Object[]{xingSensorC.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(levelXing.getSensorC());
                removeAssignment(cSensor);
                levelXing.setSensorCName(xingSensorC.getText());
            }
        } else if ((cSensor != null)
                && ((cSensor == levelXing.getSensorB())
                || (cSensor == levelXing.getSensorA())
                || (cSensor == levelXing.getSensorD()))) {
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (cSensor == null) {
            removeSensorFromPanel(levelXing.getSensorC());
            levelXing.setSensorCName("");
        }
        if ((dSensor != null) && xingSensorD.addToPanel()) {
            if (isSensorOnPanel(dSensor)
                    && (dSensor != levelXing.getSensorD())) {
                JOptionPane.showMessageDialog(sensorsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError6"),
                                new Object[]{xingSensorD.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(levelXing.getSensorD());
                placingBlock(getSensorIcon(xingSensorD.getText()), xingSensorD.isRightSelected(), 0.0, levelXing.getConnectD(), levelXing.getCoordsD());
                removeAssignment(dSensor);
                levelXing.setSensorDName(xingSensorD.getText());
                needRedraw = true;
            }
        } else if ((dSensor != null)
                && (dSensor != levelXing.getSensorA())
                && (dSensor != levelXing.getSensorB())
                && (dSensor != levelXing.getSensorC())
                && (dSensor != levelXing.getSensorD())) {
            if (isSensorOnPanel(dSensor)) {
                JOptionPane.showMessageDialog(sensorsAtXingFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError13"),
                                new Object[]{xingSensorD.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(levelXing.getSensorD());
                removeAssignment(dSensor);
                levelXing.setSensorDName(xingSensorD.getText());
            }
        } else if ((dSensor != null)
                && ((dSensor == levelXing.getSensorB())
                || (dSensor == levelXing.getSensorC())
                || (dSensor == levelXing.getSensorA()))) {
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (dSensor == null) {
            removeSensorFromPanel(levelXing.getSensorD());
            levelXing.setSensorDName("");
        }
        // setup logic if requested
        // finish up
        setSensorsAtXingOpen = false;
        sensorsAtXingFrame.setVisible(false);
        xingSensorFromMenu = false;
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }

    private boolean getSimpleBlockInformation() {
        //might have to do something to trick it with an end bumper
        if (!boundaryFromMenu) {
            block1 = getBlockFromEntry(block1IDComboBox);
            if (block1 == null) {
                return false;
            }
            block2 = getBlockFromEntry(block2IDComboBox);
            if (block2 == null) {
                for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                    if (p.getType() == PositionablePoint.END_BUMPER) {
                        boundary = p;
                        break;
                    } else {
                        return false;
                    }
                }
            }
            boundary = null;
            for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                if (p.getType() == PositionablePoint.ANCHOR) {
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
                JOptionPane.showMessageDialog(setSignalsAtBoundaryFrame,
                        Bundle.getMessage("SignalsError7"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    // operational variables for Set Sensors at Level Crossing tool
    private JmriJFrame sensorsAtSlipFrame = null;
    private boolean setSensorsAtSlipOpen = false;

    private JButton getSavedSlipSensors = null;
    private JButton setSlipSensorsDone = null;
    private JButton setSlipSensorsCancel = null;
    private JButton changeSensorSlipIcon = null;
    JFrame sensorSlipFrame = null;

    private boolean slipSensorFromMenu = false;
    private String[] slipSensorBlocks = new String[4];

    BeanDetails slipSensorA;
    BeanDetails slipSensorB;
    BeanDetails slipSensorC;
    BeanDetails slipSensorD;

    JPanel sensorSlipPanel = new JPanel(new FlowLayout());

    // display dialog for Set Signals at Level Crossing tool
    public void setSensorsAtSlipFromMenu(@Nonnull LayoutSlip slip,
            @Nonnull String[] blocks,
            @Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        slipSensorFromMenu = true;
        layoutSlip = slip;
        layoutTurnout = slip;
        xingSensorsBlockAComboBox.setText(layoutSlip.getBlockName());
        for (int i = 0; i < blocks.length; i++) {
            slipSensorBlocks[i] = blocks[i];
        }
        setSensorsAtSlip(theEditor, theFrame);
        return;
    }

    public void setSensorsAtSlip(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        sensorIconEditor = theEditor;
        sensorSlipFrame = theFrame;
        if (setSensorsAtSlipOpen) {
            slipSensorsGetSaved(null);
            sensorsAtSlipFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (sensorsAtSlipFrame == null) {
            sensorsAtSlipFrame = new JmriJFrame(Bundle.getMessage("SensorsAtSlip"), false, true);
            sensorsAtSlipFrame.addHelpMenu("package.jmri.jmrit.display.SetSensorsAtLevelSlip", true);
            sensorsAtSlipFrame.setLocation(70, 30);
            Container theContentPane = sensorsAtSlipFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel11 = new JPanel(new FlowLayout());
            slipSensorA = new BeanDetails("Sensor", InstanceManager.sensorManagerInstance());
            slipSensorB = new BeanDetails("Sensor", InstanceManager.sensorManagerInstance());
            slipSensorC = new BeanDetails("Sensor", InstanceManager.sensorManagerInstance());
            slipSensorD = new BeanDetails("Sensor", InstanceManager.sensorManagerInstance());
            if (slipSensorFromMenu) {
                JLabel blockANameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 1 "
                        + Bundle.getMessage("Name") + " : " + layoutSlip.getBlockName());

                panel11.add(blockANameLabel);

                slipSensorA.setTextField(layoutSlip.getSensorAName());
                slipSensorB.setTextField(layoutSlip.getSensorBName());
                slipSensorC.setTextField(layoutSlip.getSensorCName());
                slipSensorD.setTextField(layoutSlip.getSensorDName());
            } else {
                JLabel blockANameLabel = new JLabel(Bundle.getMessage("BeanNameBlock") + " 1 "
                        + Bundle.getMessage("Name") + " : ");
                panel11.add(blockANameLabel);
                panel11.add(xingSensorsBlockAComboBox);
                xingSensorsBlockAComboBox.setToolTipText(Bundle.getMessage("SensorsBlockNameHint"));
            }
            theContentPane.add(panel11);
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

            slipSensorA.getDetailsPanel().setBackground(new Color(255, 255, 200));

            slipSensorB.getDetailsPanel().setBackground(new Color(200, 255, 255));

            slipSensorC.getDetailsPanel().setBackground(new Color(200, 200, 255));

            slipSensorD.getDetailsPanel().setBackground(new Color(255, 200, 200));

            sensorSlipPanel.setLayout(new GridLayout(2, 2));

            slipSensorA.setBoundaryLabel(slipSensorBlocks[0]);
            slipSensorB.setBoundaryLabel(slipSensorBlocks[1]);
            slipSensorC.setBoundaryLabel(slipSensorBlocks[2]);
            slipSensorD.setBoundaryLabel(slipSensorBlocks[3]);

            if (slipSensorBlocks[0] != null) {
                sensorSlipPanel.add(slipSensorA.getDetailsPanel());
            } else {
                sensorSlipPanel.add(new JPanel());
            }
            if (slipSensorBlocks[3] != null) {
                sensorSlipPanel.add(slipSensorD.getDetailsPanel());
            } else {
                sensorSlipPanel.add(new JPanel());
            }
            if (slipSensorBlocks[1] != null) {
                sensorSlipPanel.add(slipSensorB.getDetailsPanel());
            } else {
                sensorSlipPanel.add(new JPanel());
            }
            if (slipSensorBlocks[2] != null) {
                sensorSlipPanel.add(slipSensorC.getDetailsPanel());
            } else {
                sensorSlipPanel.add(new JPanel());
            }

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
            sensorsAtSlipFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    setSlipSensorsCancelPressed(null);
                }
            });
        }
        if (slipSensorFromMenu) {
            slipSensorsGetSaved(null);
        }
        sensorsAtSlipFrame.setPreferredSize(null);
        sensorsAtSlipFrame.pack();
        sensorsAtSlipFrame.setVisible(true);
        setSensorsAtSlipOpen = true;
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

        boolean boundary = false;
        if (slipSensorBlocks[0] != null) {
            sensorSlipPanel.add(slipSensorA.getDetailsPanel());
            boundary = true;
        }
        if (slipSensorBlocks[1] != null) {
            sensorSlipPanel.add(slipSensorB.getDetailsPanel());
            boundary = true;
        }
        if (slipSensorBlocks[2] != null) {
            sensorSlipPanel.add(slipSensorC.getDetailsPanel());
            boundary = true;
        }
        if (slipSensorBlocks[3] != null) {
            sensorSlipPanel.add(slipSensorD.getDetailsPanel());
            boundary = true;
        }
        if (!boundary) {
            JOptionPane.showMessageDialog(sensorsAtSlipFrame, Bundle.getMessage("NoBoundarySlipSensor"));
        }
        sensorsAtSlipFrame.setPreferredSize(null);
        sensorsAtSlipFrame.pack();
    }

    private boolean getSlipSensorInformation() {
        if (!slipSensorFromMenu) {
            layoutSlip = null;
            List<LayoutSlip> layoutSlips = layoutEditor.getLayoutSlips();
            if (layoutSlips.size() <= 0) {
                JOptionPane.showMessageDialog(sensorsAtSlipFrame,
                        Bundle.getMessage("SignalsError15"),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            } else if (layoutSlips.size() == 1) {
                layoutSlip = layoutSlips.get(0);
            } else {
                LayoutBlock slipSensorBlockA = null;
                slipSensorBlockA = getBlockFromEntry(xingSensorsBlockAComboBox);
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
                    // try one block test
                    for (LayoutSlip x : layoutEditor.getLayoutSlips()) {
                        if (slipSensorBlockA == x.getLayoutBlock()) {
                            layoutSlip = x;
                            foundCount++;
                        }
                    }
                }
                if (foundCount > 1) {
                    JOptionPane.showMessageDialog(sensorsAtSlipFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError16"),
                                    new Object[]{" " + foundCount + " "}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (layoutSlip == null) {
                    JOptionPane.showMessageDialog(sensorsAtSlipFrame,
                            Bundle.getMessage("SignalsError17"),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    private void setSlipSensorsCancelPressed(ActionEvent a) {
        setSensorsAtSlipOpen = false;
        sensorsAtSlipFrame.setVisible(false);
        slipSensorFromMenu = false;
    }

    private void setSlipSensorsDonePressed(ActionEvent a) {
        if (!getSlipSensorInformation()) {
            return;
        }
        Sensor aSensor = getSensorFromEntry(slipSensorA.getText(), false, sensorsAtSlipFrame);
        Sensor bSensor = getSensorFromEntry(slipSensorB.getText(), false, sensorsAtSlipFrame);
        Sensor cSensor = getSensorFromEntry(slipSensorC.getText(), false, sensorsAtSlipFrame);
        Sensor dSensor = getSensorFromEntry(slipSensorD.getText(), false, sensorsAtSlipFrame);
        // place or update signals as requested
        if ((aSensor != null) && slipSensorA.addToPanel()) {
            if (isSensorOnPanel(aSensor)
                    && (aSensor != layoutSlip.getSensorA())) {
                JOptionPane.showMessageDialog(sensorsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError6"),
                                new Object[]{slipSensorA.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(layoutSlip.getSensorA());
                placingBlock(getSensorIcon(slipSensorA.getText()), slipSensorA.isRightSelected(), 0.0, layoutSlip.getConnectA(), layoutSlip.getCoordsA());
                removeAssignment(aSensor);
                layoutSlip.setSensorA(slipSensorA.getText());
                needRedraw = true;
            }
        } else if ((aSensor != null)
                && (aSensor != layoutSlip.getSensorA())
                && (aSensor != layoutSlip.getSensorB())
                && (aSensor != layoutSlip.getSensorC())
                && (aSensor != layoutSlip.getSensorD())) {
            if (isSensorOnPanel(aSensor)) {
                JOptionPane.showMessageDialog(sensorsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError13"),
                                new Object[]{slipSensorA.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(layoutSlip.getSensorA());
                removeAssignment(aSensor);
                layoutSlip.setSensorA(slipSensorA.getText());
            }
        } else if ((aSensor != null)
                && ((aSensor == layoutSlip.getSensorB())
                || (aSensor == layoutSlip.getSensorC())
                || (aSensor == layoutSlip.getSensorD()))) {
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (aSensor == null) {
            removeSensorFromPanel(layoutSlip.getSensorA());
            layoutSlip.setSensorA("");
        }
        if ((bSensor != null) && slipSensorB.addToPanel()) {
            if (isSensorOnPanel(bSensor)
                    && (bSensor != layoutSlip.getSensorB())) {
                JOptionPane.showMessageDialog(sensorsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError6"),
                                new Object[]{slipSensorB.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(layoutSlip.getSensorB());
                placingBlock(getSensorIcon(slipSensorB.getText()), slipSensorB.isRightSelected(), 0.0, layoutSlip.getConnectB(), layoutSlip.getCoordsB());
                removeAssignment(bSensor);
                layoutSlip.setSensorB(slipSensorB.getText());
                needRedraw = true;
            }
        } else if ((bSensor != null)
                && (bSensor != layoutSlip.getSensorA())
                && (bSensor != layoutSlip.getSensorB())
                && (bSensor != layoutSlip.getSensorC())
                && (bSensor != layoutSlip.getSensorD())) {
            if (isSensorOnPanel(bSensor)) {
                JOptionPane.showMessageDialog(sensorsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError13"),
                                new Object[]{slipSensorB.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(layoutSlip.getSensorB());
                removeAssignment(bSensor);
                layoutSlip.setSensorB(slipSensorB.getText());
            }
        } else if ((bSensor != null)
                && ((bSensor == layoutSlip.getSensorA())
                || (bSensor == layoutSlip.getSensorC())
                || (bSensor == layoutSlip.getSensorD()))) {
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (bSensor == null) {
            removeSensorFromPanel(layoutSlip.getSensorB());
            layoutSlip.setSensorB("");
        }
        if ((cSensor != null) && slipSensorC.addToPanel()) {
            if (isSensorOnPanel(cSensor)
                    && (cSensor != layoutSlip.getSensorC())) {
                JOptionPane.showMessageDialog(sensorsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError6"),
                                new Object[]{slipSensorC.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(layoutSlip.getSensorC());
                placingBlock(getSensorIcon(slipSensorC.getText()), slipSensorC.isRightSelected(), 0.0, layoutSlip.getConnectC(), layoutSlip.getCoordsC());
                removeAssignment(cSensor);
                layoutSlip.setSensorC(slipSensorC.getText());
                needRedraw = true;
            }
        } else if ((cSensor != null)
                && (cSensor != layoutSlip.getSensorA())
                && (cSensor != layoutSlip.getSensorB())
                && (cSensor != layoutSlip.getSensorC())
                && (cSensor != layoutSlip.getSensorD())) {
            if (isSensorOnPanel(cSensor)) {
                JOptionPane.showMessageDialog(sensorsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError13"),
                                new Object[]{slipSensorC.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(layoutSlip.getSensorC());
                removeAssignment(cSensor);
                layoutSlip.setSensorC(slipSensorC.getText());
            }
        } else if ((cSensor != null)
                && ((cSensor == layoutSlip.getSensorB())
                || (cSensor == layoutSlip.getSensorA())
                || (cSensor == layoutSlip.getSensorD()))) {
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (cSensor == null) {
            removeSensorFromPanel(layoutSlip.getSensorC());
            layoutSlip.setSensorC("");
        }
        if ((dSensor != null) && slipSensorD.addToPanel()) {
            if (isSensorOnPanel(dSensor)
                    && (dSensor != layoutSlip.getSensorD())) {
                JOptionPane.showMessageDialog(sensorsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError6"),
                                new Object[]{slipSensorD.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(layoutSlip.getSensorD());
                placingBlock(getSensorIcon(slipSensorD.getText()), slipSensorD.isRightSelected(), 0.0, layoutSlip.getConnectD(), layoutSlip.getCoordsD());
                removeAssignment(dSensor);
                layoutSlip.setSensorD(slipSensorD.getText());
                needRedraw = true;
            }
        } else if ((dSensor != null)
                && (dSensor != layoutSlip.getSensorA())
                && (dSensor != layoutSlip.getSensorB())
                && (dSensor != layoutSlip.getSensorC())
                && (dSensor != layoutSlip.getSensorD())) {
            if (isSensorOnPanel(dSensor)) {
                JOptionPane.showMessageDialog(sensorsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SensorsError13"),
                                new Object[]{slipSensorD.getText()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                removeSensorFromPanel(layoutSlip.getSensorD());
                removeAssignment(dSensor);
                layoutSlip.setSensorD(slipSensorD.getText());
            }
        } else if ((dSensor != null)
                && ((dSensor == layoutSlip.getSensorB())
                || (dSensor == layoutSlip.getSensorC())
                || (dSensor == layoutSlip.getSensorA()))) {
            // need to figure out what to do in this case.
            log.trace("need to figure out what to do in this case.");
        } else if (dSensor == null) {
            removeSensorFromPanel(layoutSlip.getSensorD());
            layoutSlip.setSensorD("");
        }
        // setup logic if requested
        // finish up
        setSensorsAtSlipOpen = false;
        sensorsAtSlipFrame.setVisible(false);
        slipSensorFromMenu = false;
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }

    static class BeanDetails {

        String bundleName;
        String beanString;
        JLabel textLabel;

        String boundaryLabelText = Bundle.getMessage("BoundaryOf");
        JLabel boundary = new JLabel(boundaryLabelText);

        Manager manager;

        JPanel detailsPanel = new JPanel(new FlowLayout());
        JRadioButton addBeanCheck = new JRadioButton(Bundle.getMessage("DoNotPlace"));
        JRadioButton left = new JRadioButton(Bundle.getMessage("LeftHandSide"));
        JRadioButton right = new JRadioButton(Bundle.getMessage("RightHandSide"));
        ButtonGroup buttonGroup = new ButtonGroup();
        JmriBeanComboBox beanCombo;

        JLabel boundaryBlocks = new JLabel();

        Border blackline = BorderFactory.createLineBorder(Color.black);

        BeanDetails(String beanType, Manager manager) {
            beanCombo = new JmriBeanComboBox(manager);
            beanCombo.setFirstItemBlank(true);
            // I18N translate from type (Sensor) to BeanNameSensor
            // to use NamedBeanBundle property
            if ("Sensor".equals(beanType)) {
                bundleName = "BeanNameSensor";
            } else if ("SignalMast".equals(beanType)) {
                bundleName = "BeanNameSignalMast";
            } else {
                log.error("Unexpected value for BeanDetails");
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
            boundary.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel boundaryDetails = new JPanel(new FlowLayout());
            boundaryDetails.setOpaque(false);
            boundaryDetails.setLayout(new BoxLayout(boundaryDetails, BoxLayout.Y_AXIS));
            boundaryDetails.add(boundary);
            boundaryDetails.add(boundaryBlocks);

            detailsPanel.add(boundaryDetails, BorderLayout.PAGE_START);
            detailsPanel.add(addIconPanel(), BorderLayout.CENTER);
            detailsPanel.add(positionLeftRight(), BorderLayout.PAGE_END);
        }

        void setTextField(String value) {
            beanCombo.setSelectedBean(manager.getNamedBean(value));
        }

        String getText() {
            return beanCombo.getSelectedDisplayName();
        }

        NamedBean getBean() {
            return beanCombo.getSelectedBean();
        }

        JPanel getDetailsPanel() {
            return detailsPanel;
        }

        boolean addToPanel() {
            return !addBeanCheck.isSelected();
        }

        boolean isRightSelected() {
            return right.isSelected();
        }

        void setBoundaryTitle(String text) {
            detailsPanel.setBorder(BorderFactory.createTitledBorder(blackline, text));
        }

        void setBoundaryLabelText(String text) {
            boundary.setText(text);
        }

        void setBoundaryLabel(String label) {
            boundaryBlocks.setText(label);
        }

        JmriBeanComboBox getCombo() {
            return beanCombo;
        }

        JPanel positionLeftRight() {
            JPanel placementPanel = new JPanel();
            placementPanel.setBorder(BorderFactory.createTitledBorder(blackline, MessageFormat.format(Bundle.getMessage("PlaceItem"),
                    new Object[]{beanString})));
            placementPanel.setLayout(new BoxLayout(placementPanel, BoxLayout.Y_AXIS));
            placementPanel.setOpaque(false);
            placementPanel.add(addBeanCheck);
            placementPanel.add(left);
            placementPanel.add(right);
            addBeanCheck.setOpaque(false);
            left.setOpaque(false);
            right.setOpaque(false);

            addBeanCheck.setToolTipText(MessageFormat.format(Bundle.getMessage("PlaceItemToolTip"),
                    new Object[]{beanString}));

            right.setToolTipText(MessageFormat.format(Bundle.getMessage("PlaceRightToolTip"),
                    new Object[]{beanString}));

            left.setToolTipText(MessageFormat.format(Bundle.getMessage("PlaceLeftToolTip"),
                    new Object[]{beanString}));
            return placementPanel;
        }

        JPanel addIconPanel() {
            JPanel addBeanPanel = new JPanel(new FlowLayout());
            addBeanPanel.setOpaque(false);
            addBeanPanel.add(textLabel);
            textLabel.setOpaque(false);
            addBeanPanel.add(beanCombo);
            return addBeanPanel;
        }
    }

    // operational variables for Set Signals at Double Crossover Turnout tool
    private JmriJFrame setSignalsAtSlipFrame = null;
    private boolean setSignalsAtSlipOpen = false;
    private JComboBox<String> slipNameCombo = new JComboBox<String>();

    private JmriBeanComboBox a1SlipSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox a2SlipSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox b1SlipSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox b2SlipSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox c1SlipSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox c2SlipSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox d1SlipSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JmriBeanComboBox d2SlipSignalHeadComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class),
            null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    private JCheckBox setA1SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupA1SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setA2SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupA2SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setB1SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupB1SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setB2SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupB2SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setC1SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupC1SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setC2SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupC2SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setD1SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupD1SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));
    private JCheckBox setD2SlipHead = new JCheckBox(Bundle.getMessage("PlaceHead"));
    private JCheckBox setupD2SlipLogic = new JCheckBox(Bundle.getMessage("SetLogic"));

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
    private boolean slipSignalFromMenu = false;

    public void setSignalsAtSlipFromMenu(@Nonnull LayoutSlip ls,
            @Nonnull MultiIconEditor theEditor, @Nonnull JFrame theFrame) {
        layoutSlip = ls;
        a1SlipSignalHeadComboBox.setText("");
        a2SlipSignalHeadComboBox.setText("");
        b1SlipSignalHeadComboBox.setText("");
        b2SlipSignalHeadComboBox.setText("");
        c1SlipSignalHeadComboBox.setText("");
        c2SlipSignalHeadComboBox.setText("");
        d1SlipSignalHeadComboBox.setText("");
        d2SlipSignalHeadComboBox.setText("");
        slipSignalFromMenu = true;

        setSignalsAtSlip(theEditor, theFrame);
    }

    public void setSignalsAtSlip(@Nonnull MultiIconEditor theEditor,
            @Nonnull JFrame theFrame) {
        signalIconEditor = theEditor;
        signalFrame = theFrame;
        if (setSignalsAtSlipOpen) {
            setSignalsAtSlipFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (setSignalsAtSlipFrame == null) {
            setSignalsAtSlipFrame = new JmriJFrame(Bundle.getMessage("SignalsAtSlip"), false, true);
            setSignalsAtSlipFrame.addHelpMenu("package.jmri.jmrit.display.SetSignalsAtSlip", true);
            setSignalsAtSlipFrame.setLocation(70, 30);
            Container theContentPane = setSignalsAtSlipFrame.getContentPane();
            theContentPane.setLayout(new BoxLayout(theContentPane, BoxLayout.Y_AXIS));

            JPanel panel1 = new JPanel(new FlowLayout());
            JLabel turnout1NameLabel = new JLabel(Bundle.getMessage("Slip") + " "
                    + Bundle.getMessage("Name"));
            panel1.add(turnout1NameLabel);
            panel1.add(slipNameCombo);
            for (LayoutSlip slip : layoutEditor.getLayoutSlips()) {
                slipNameCombo.addItem(slip.getDisplayName());
            }

            slipNameCombo.insertItemAt("", 0);

            if (layoutSlip != null) {
                slipNameCombo.setSelectedItem(layoutSlip.getDisplayName());
                getSlipTurnoutSignalsGetSaved(null);
            } else {
                slipNameCombo.setSelectedIndex(0);
            }
            slipNameCombo.addActionListener((ActionEvent e) -> {
                for (LayoutSlip slip : layoutEditor.getLayoutSlips()) {
                    if (slip.getDisplayName().equals(slipNameCombo.getSelectedItem())) {
                        //slip1NameField.setText(slip.getDisplayName());
                        getSlipTurnoutSignalsGetSaved(e);
                        dblSlipC2SigPanel.setVisible(false);
                        dblSlipB2SigPanel.setVisible(false);
                        if (slip.getSlipType() == LayoutSlip.DOUBLE_SLIP) {
                            dblSlipB2SigPanel.setVisible(true);
                            dblSlipC2SigPanel.setVisible(true);
                        }
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
                // (de)select all checkboxes
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
                // (de)select all checkboxes
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

            // Signal heads located at turnout 1
            JPanel panel21x = new JPanel(new FlowLayout());
            panel21x.add(new JLabel(Bundle.getMessage("SignalLocated") + " " + Bundle.getMessage("BeanNameTurnout") + " 1 - "
                    + Bundle.getMessage("ContinuingTrack")));
            theContentPane.add(panel21x);

            JPanel panel21 = new JPanel(new FlowLayout());
            panel21.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 2 - " + Bundle.getMessage("ContinuingTrack") + " : "));
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
            panel23.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 2 - " + Bundle.getMessage("DivergingTrack") + " : "));
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
            panel31x.add(new JLabel(Bundle.getMessage("SignalLocated") + " " + Bundle.getMessage("BeanNameTurnout") + " 1 - "
                    + Bundle.getMessage("DivergingTrack")));
            theContentPane.add(panel31x);

            JPanel panel31 = new JPanel(new FlowLayout());
            panel31.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 2 - " + Bundle.getMessage("ContinuingTrack") + " : "));
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
            panel33.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 2 - " + Bundle.getMessage("DivergingTrack") + " : "));
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
            // Signal heads located at turnout 2

            JPanel panel41x = new JPanel(new FlowLayout());
            panel41x.add(new JLabel(Bundle.getMessage("SignalLocated") + " " + Bundle.getMessage("BeanNameTurnout") + " 2 - "
                    + Bundle.getMessage("ContinuingTrack")));
            theContentPane.add(panel41x);

            JPanel panel41 = new JPanel(new FlowLayout());
            panel41.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 1 - " + Bundle.getMessage("ContinuingTrack") + " : "));
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
            panel43.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 1 - " + Bundle.getMessage("DivergingTrack") + " : "));
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
            panel51x.add(new JLabel(Bundle.getMessage("SignalLocated") + " " + Bundle.getMessage("BeanNameTurnout") + " 2 - "
                    + Bundle.getMessage("DivergingTrack")));
            theContentPane.add(panel51x);

            JPanel panel51 = new JPanel(new FlowLayout());
            panel51.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 1 - " + Bundle.getMessage("ContinuingTrack") + " : "));
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
            panel53.add(new JLabel(Bundle.getMessage("ProtectsTurnout") + " 1 - " + Bundle.getMessage("DivergingTrack") + " : "));
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
        dblSlipC2SigPanel.setVisible(false);
        dblSlipB2SigPanel.setVisible(false);
        if (layoutSlip != null && layoutSlip.getSlipType() == LayoutSlip.DOUBLE_SLIP) {
            dblSlipB2SigPanel.setVisible(true);
            dblSlipC2SigPanel.setVisible(true);
        }
        if (slipSignalFromMenu) {
            getSlipTurnoutSignalsGetSaved(null);
        }
        setSignalsAtSlipFrame.pack();
        setSignalsAtSlipFrame.setVisible(true);
        setSignalsAtSlipOpen = true;
    }

    private void getSlipTurnoutSignalsGetSaved(ActionEvent a) {
        if (!getSlipTurnoutInformation()) {
            return;
        }
        a1SlipSignalHeadComboBox.setText(layoutSlip.getSignalA1Name());
        a2SlipSignalHeadComboBox.setText(layoutSlip.getSignalA2Name());
        b1SlipSignalHeadComboBox.setText(layoutSlip.getSignalB1Name());
        b2SlipSignalHeadComboBox.setText(layoutSlip.getSignalB2Name());
        c1SlipSignalHeadComboBox.setText(layoutSlip.getSignalC1Name());
        c2SlipSignalHeadComboBox.setText(layoutSlip.getSignalC2Name());
        d1SlipSignalHeadComboBox.setText(layoutSlip.getSignalD1Name());
        d2SlipSignalHeadComboBox.setText(layoutSlip.getSignalD2Name());
    }

    private void setSlipSignalsCancelPressed(ActionEvent a) {
        setSignalsAtSlipOpen = false;
        setSignalsAtSlipFrame.setVisible(false);
    }

    private boolean getSlipTurnoutInformation() {
        String str = "";
        turnout1 = null;
        turnout2 = null;
        layoutSlip = null;
        for (LayoutSlip ls : layoutEditor.getLayoutSlips()) {
            if (ls.getDisplayName().equals(slipNameCombo.getSelectedItem())) {
                turnout1 = ls.getTurnout();
                turnout2 = ls.getTurnoutB();
                layoutSlip = ls;
                layoutTurnout = layoutSlip;
                break;
            }
        }
        if (layoutSlip == null) {
            return false;
        }
        if (turnout1 == null) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    MessageFormat.format(Bundle.getMessage("SignalsError2"),
                            new Object[]{str}), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (turnout2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    MessageFormat.format(Bundle.getMessage("SignalsError2"),
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

        // place signal icons if requested, and assign signal heads to this turnout
        String signalHeadName = a1SlipSignalHeadComboBox.getDisplayName();
        if (setA1SlipHead.isSelected()) {
            if (isHeadOnPanel(a1SlipHead)
                    && (a1SlipHead != getHeadFromName(layoutSlip.getSignalB1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(a1SlipHead)
                        && isHeadAssignedAnywhere(a1SlipHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutSlip.getSignalB1Name());
                    removeAssignment(a1SlipHead);
                    layoutSlip.setSignalA1Name(signalHeadName);
                }
                //} else if (assigned != B1) {
                // need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }

        signalHeadName = a2SlipSignalHeadComboBox.getDisplayName();
        if ((a2SlipHead != null) && setA2SlipHead.isSelected()) {
            if (isHeadOnPanel(a2SlipHead)
                    && (a2SlipHead != getHeadFromName(layoutSlip.getSignalB2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(a2SlipHead)
                        && isHeadAssignedAnywhere(a2SlipHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutSlip.getSignalA2Name());
                    removeAssignment(a2SlipHead);
                    layoutSlip.setSignalA2Name(signalHeadName);
                }
                //} else if (assigned != B2) {
                // need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        } else { // a2SlipHead known to be null here
            removeSignalHeadFromPanel(layoutSlip.getSignalA2Name());
            layoutSlip.setSignalB2Name("");
        }

        signalHeadName = b1SlipSignalHeadComboBox.getDisplayName();
        if (setB1SlipHead.isSelected()) {
            if (isHeadOnPanel(b1SlipHead)
                    && (b1SlipHead != getHeadFromName(layoutSlip.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(b1SlipHead)
                        && isHeadAssignedAnywhere(b1SlipHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutSlip.getSignalB1Name());
                    removeAssignment(b1SlipHead);
                    layoutSlip.setSignalB1Name(signalHeadName);
                }
                //} else if (assigned != C1) {
                // need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }

        if (layoutSlip.getTurnoutType() == LayoutSlip.DOUBLE_SLIP) {
            signalHeadName = b2SlipSignalHeadComboBox.getDisplayName();
            if ((b2SlipHead != null) && setB2SlipHead.isSelected()) {
                if (isHeadOnPanel(b2SlipHead)
                        && (b2SlipHead != getHeadFromName(layoutSlip.getSignalC2Name()))) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                if (assigned == NONE) {
                    if (isHeadOnPanel(b2SlipHead)
                            && isHeadAssignedAnywhere(b2SlipHead)) {
                        JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                                MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                        new Object[]{signalHeadName}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        removeSignalHeadFromPanel(layoutSlip.getSignalB2Name());
                        removeAssignment(b2SlipHead);
                        layoutSlip.setSignalB2Name(signalHeadName);
                    }
                    //} else if (assigned != C2) {
                    // need to figure out what to do in this case - assigned to a different position on the same turnout.
                }
            } else { // b2SlipHead known to be null here
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

        // signal heads on turnout 2
        signalHeadName = c1SlipSignalHeadComboBox.getDisplayName();
        if (setC1SlipHead.isSelected()) {
            if (isHeadOnPanel(c1SlipHead)
                    && (c1SlipHead != getHeadFromName(layoutSlip.getSignalB1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(c1SlipHead)
                        && isHeadAssignedAnywhere(c1SlipHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutSlip.getSignalC1Name());
                    removeAssignment(c1SlipHead);
                    layoutSlip.setSignalC1Name(signalHeadName);
                }
                //} else if (assigned != B1) {
                // need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }

        if (layoutSlip.getTurnoutType() == LayoutSlip.DOUBLE_SLIP) {
            signalHeadName = c2SlipSignalHeadComboBox.getDisplayName();
            if ((c2SlipHead != null) && setC2SlipHead.isSelected()) {
                if (isHeadOnPanel(c2SlipHead)
                        && (c2SlipHead != getHeadFromName(layoutSlip.getSignalB2Name()))) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
                if (assigned == NONE) {
                    if (isHeadOnPanel(c2SlipHead)
                            && isHeadAssignedAnywhere(c2SlipHead)) {
                        JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                                MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                        new Object[]{signalHeadName}),
                                Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        removeSignalHeadFromPanel(layoutSlip.getSignalC2Name());
                        removeAssignment(c2SlipHead);
                        layoutSlip.setSignalC2Name(signalHeadName);
                    }
                    //} else if (assigned != B2) {
                    // need to figure out what to do in this case - assigned to a different position on the same turnout.
                }
            } else { // c2SlipHead known to be null here
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

        signalHeadName = d1SlipSignalHeadComboBox.getDisplayName();
        if (setD1SlipHead.isSelected()) {
            if (isHeadOnPanel(d1SlipHead)
                    && (d1SlipHead != getHeadFromName(layoutSlip.getSignalC1Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(d1SlipHead)
                        && isHeadAssignedAnywhere(d1SlipHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutSlip.getSignalD1Name());
                    removeAssignment(d1SlipHead);
                    layoutSlip.setSignalD1Name(signalHeadName);
                }
                //} else if (assigned != C1) {
                // need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        }

        signalHeadName = d2SlipSignalHeadComboBox.getDisplayName();
        if ((d2SlipHead != null) && setD2SlipHead.isSelected()) {
            if (isHeadOnPanel(d2SlipHead)
                    && (d2SlipHead != getHeadFromName(layoutSlip.getSignalC2Name()))) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("SignalsError6"),
                                new Object[]{signalHeadName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
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
            if (assigned == NONE) {
                if (isHeadOnPanel(d2SlipHead)
                        && isHeadAssignedAnywhere(d2SlipHead)) {
                    JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                            MessageFormat.format(Bundle.getMessage("SignalsError8"),
                                    new Object[]{signalHeadName}),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    removeSignalHeadFromPanel(layoutSlip.getSignalD2Name());
                    removeAssignment(d2SlipHead);
                    layoutSlip.setSignalD2Name(signalHeadName);
                }
                //} else if (assigned != C2) {
                // need to figure out what to do in this case - assigned to a different position on the same turnout.
            }
        } else { // d2SlipHead known to be null here
            removeSignalHeadFromPanel(layoutSlip.getSignalD2Name());
            layoutSlip.setSignalD2Name("");
        }
        // setup logic if requested
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
        // finish up
        setSignalsAtSlipOpen = false;
        setSignalsAtSlipFrame.setVisible(false);
        if (needRedraw) {
            layoutEditor.redrawPanel();
            needRedraw = false;
            layoutEditor.setDirty();
        }
    }

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
        // place head near the continuing track of turnout 1
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

        Point2D coordsB = layoutTurnout.getCoordsB();
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

        Point2D coordsB = layoutTurnout.getCoordsB();
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

        Point2D coordsD = layoutTurnout.getCoordsD();
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

        Point2D coordsD = layoutTurnout.getCoordsD();
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
        // initialize common components and ensure all is defined
        LayoutBlock connectorBlock = slip.getLayoutBlock();
        Sensor connectorOccupancy = null;
        if (connectorBlock == null) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        connectorOccupancy = connectorBlock.getOccupancySensor();
        if (connectorOccupancy == null) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{connectorBlock.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int nearState = layoutSlip.getTurnoutState(nearTurnout, continueState);
        int farState = layoutSlip.getTurnoutState(farTurnout, continueState);

        // setup signal head for continuing track of far turnout (or both tracks of far turnout)
        if ((track1 == null) && setup1) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy = null;
        SignalHead nextHead = null;
        if ((track1 != null) && setup1) {
            LayoutBlock block = track1.getLayoutBlock();
            if (block == null) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            occupancy = block.getOccupancySensor();
            if (occupancy == null) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                                new Object[]{block.getUserName()}),
                        null, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            //need to sort this out???
            nextHead = getNextSignalFromObject(track1, slip,
                    head.getSystemName(), setSignalsAtSlipFrame);
            if ((nextHead == null) && (!reachedEndBumper())) {
                JOptionPane.showMessageDialog(setSignalsFrame,
                        MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                                new Object[]{block.getUserName()}),
                        null, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (secondHead != null) {
                // this head signals only the continuing track of the far turnout
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
                    Bundle.getMessage("InfoMessage7"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LayoutBlock block2 = track2.getLayoutBlock();
        if (block2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    Bundle.getMessage("InfoMessage6"), "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Sensor occupancy2 = block2.getOccupancySensor();
        if (occupancy2 == null) {
            JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                    MessageFormat.format(Bundle.getMessage("InfoMessage4"),
                            new Object[]{block2.getUserName()}),
                    null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        SignalHead nextHead2 = null;
        if (secondHead != null) {
            nextHead2 = getNextSignalFromObject(track2,
                    slip, secondHead.getSystemName(), setSignalsAtSlipFrame);
            if ((nextHead2 == null) && (!reachedEndBumper())) {
                JOptionPane.showMessageDialog(setSignalsAtSlipFrame,
                        MessageFormat.format(Bundle.getMessage("InfoMessage5"),
                                new Object[]{block2.getUserName()}),
                        null, JOptionPane.INFORMATION_MESSAGE);
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
    }

    private String setupNearLogixSlip(Turnout turn, int nearState,
            SignalHead head, Turnout farTurn, int farState, LayoutSlip slip, int number) {
        String turnoutName = turn.getDisplayName();
        String farTurnoutName = farTurn.getDisplayName();

        String logixName = "SYS_LAYOUTSLIP:" + slip.ident;
        String sensorName = "IS:" + logixName + "C" + number;
        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        } catch (IllegalArgumentException ex) {
            log.error("Trouble creating sensor " + sensorName + " while setting up Logix.");
            return "";
        }
        boolean newConditional = false;
        Logix x = InstanceManager.getDefault(LogixManager.class).getBySystemName(logixName);
        if (x == null) {
            x = InstanceManager.getDefault(LogixManager.class).createNewLogix(logixName, "");
            newConditional = true;
            if (x == null) {
                log.error("Trouble creating logix " + logixName + " while setting up signal logic.");
                return "";
            }
            x.setComment("Layout Slip, Signalhead logic");
        }
        x.deActivateLogix();
        String cName = logixName + "C" + number;

        Conditional c = InstanceManager.getDefault(ConditionalManager.class).getBySystemName(cName);
        if (c == null) {
            c = InstanceManager.getDefault(ConditionalManager.class).
                    createNewConditional(cName, "");
            newConditional = true;
            if (c == null) {
                log.error("Trouble creating conditional " + cName + " while setting up Logix.");
                return "";
            }
        }
        int type = Conditional.TYPE_TURNOUT_THROWN;
        if (nearState == Turnout.CLOSED) {
            type = Conditional.TYPE_TURNOUT_CLOSED;
        }
        ArrayList<ConditionalVariable> variableList = new ArrayList<>();
        variableList.add(new ConditionalVariable(false, Conditional.OPERATOR_AND,
                type, turnoutName, true));

        type = Conditional.TYPE_TURNOUT_THROWN;
        if (farState == Turnout.CLOSED) {
            type = Conditional.TYPE_TURNOUT_CLOSED;
        }
        variableList.add(new ConditionalVariable(false, Conditional.OPERATOR_AND,
                type, farTurnoutName, true));
        c.setStateVariables(variableList);
        ArrayList<ConditionalAction> actionList = new ArrayList<>();
        actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                Conditional.ACTION_SET_SENSOR, sensorName,
                Sensor.INACTIVE, ""));
        actionList.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE,
                Conditional.ACTION_SET_SENSOR, sensorName,
                Sensor.ACTIVE, ""));
        c.setAction(actionList);		// string data
        if (newConditional) {
            x.addConditional(cName, -1);
        }
        x.activateLogix();
        return sensorName;
    }

    /*
	 * Adds the sensor specified to the open BlockBossLogic, provided it is not already there and
	 *	provided there is an open slot. If 'name' is null or empty, returns without doing anything.
     */
    private void addNearSensorToSlipLogic(String name) {
        if ((name != null) && !name.isEmpty()) {
            // return if a sensor by this name is already present
            if (logic.getSensor1().equals(name)) {
                return;
            }
            if (logic.getSensor2().equals(name)) {
                return;
            }
            if (logic.getSensor3().equals(name)) {
                return;
            }
            if (logic.getSensor4().equals(name)) {
                return;
            }
            if (logic.getSensor5().equals(name)) {
                return;
            }
            // add in the first available slot
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

    @CheckReturnValue
    public SignalHeadIcon getSignalHeadIcon(@Nonnull String signalName) {
        SignalHeadIcon l = new SignalHeadIcon(layoutEditor);
        l.setSignalHead(signalName);
        l.setIcon(Bundle.getMessage("SignalHeadStateRed"), signalIconEditor.getIcon(0));
        l.setIcon(Bundle.getMessage("SignalHeadStateFlashingRed"), signalIconEditor.getIcon(1));
        l.setIcon(Bundle.getMessage("SignalHeadStateYellow"), signalIconEditor.getIcon(2));
        l.setIcon(Bundle.getMessage("SignalHeadStateFlashingYellow"), signalIconEditor.getIcon(3));
        l.setIcon(Bundle.getMessage("SignalHeadStateGreen"), signalIconEditor.getIcon(4));
        l.setIcon(Bundle.getMessage("SignalHeadStateFlashingGreen"), signalIconEditor.getIcon(5));
        l.setIcon(Bundle.getMessage("SignalHeadStateDark"), signalIconEditor.getIcon(6));
        l.setIcon(Bundle.getMessage("SignalHeadStateHeld"), signalIconEditor.getIcon(7));
        l.setIcon(Bundle.getMessage("SignalHeadStateLunar"), signalIconEditor.getIcon(8));
        l.setIcon(Bundle.getMessage("SignalHeadStateFlashingLunar"), signalIconEditor.getIcon(9));
        l.rotate(90);
        return l;
    }

    protected Boolean addLayoutTurnoutSignalHeadInfoToMenu(
            @Nonnull String inTurnoutNameA, @Nonnull String inTurnoutNameB,
            @Nonnull JMenu inMenu) {
        Boolean result = false; // assume failure (pessimist!)
        // just so we won't have to test for null later
        inTurnoutNameA = (inTurnoutNameA == null) ? "" : inTurnoutNameA;
        inTurnoutNameB = (inTurnoutNameB == null) ? "" : inTurnoutNameB;
        // lookup turnouts
        turnout = turnout1 = turnoutA = InstanceManager.turnoutManagerInstance().getTurnout(inTurnoutNameA);
        turnout2 = turnoutB = InstanceManager.turnoutManagerInstance().getTurnout(inTurnoutNameB);
        // map those to layout turnouts (if possible)
        for (LayoutTurnout lt : layoutEditor.getLayoutTurnouts()) {
            Turnout to = lt.getTurnout();
            if (to != null) {
                String uname = to.getUserName();
                String sname = to.getSystemName();
                if (!inTurnoutNameA.isEmpty() && (sname.equals(inTurnoutNameA.toUpperCase()) || ((uname != null) && uname.equals(inTurnoutNameA)))) {
                    layoutTurnout = layoutTurnout1 = layoutTurnoutA = lt;
                }
                if (!inTurnoutNameB.isEmpty() && (sname.equals(inTurnoutNameB.toUpperCase()) || ((uname != null) && uname.equals(inTurnoutNameB)))) {
                    layoutTurnout2 = layoutTurnoutB = lt;
                }
            }
        }

        // convenience strings
        String east = Bundle.getMessage("East");
        String west = Bundle.getMessage("West");
        String continuing = Bundle.getMessage("Continuing");
        String diverging = Bundle.getMessage("Diverging");
        String throat = Bundle.getMessage("Throat");
        String throatContinuing = Bundle.getMessage("ThroatContinuing");
        String throatDiverging = Bundle.getMessage("ThroatDiverging");

        String diverging_ = Bundle.getMessage("Diverging_");
        String divergingA = MessageFormat.format(diverging_, "A");
        String divergingB = MessageFormat.format(diverging_, "B");

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
            addInfoToMenu("A " + continuing, layoutTurnout.getSignalA1Name(), inMenu);
            addInfoToMenu("A " + diverging, layoutTurnout.getSignalA2Name(), inMenu);
            addInfoToMenu("B " + continuing, layoutTurnout.getSignalB1Name(), inMenu);
            addInfoToMenu("B " + diverging, layoutTurnout.getSignalB2Name(), inMenu);
            addInfoToMenu("C " + continuing, layoutTurnout.getSignalC1Name(), inMenu);
            addInfoToMenu("C " + diverging, layoutTurnout.getSignalC2Name(), inMenu);
            addInfoToMenu("D " + continuing, layoutTurnout.getSignalD1Name(), inMenu);
            addInfoToMenu("D " + diverging, layoutTurnout.getSignalD2Name(), inMenu);
        } else if (linkType == LayoutTurnout.NO_LINK) {
            JMenuItem jmi = inMenu.add(Bundle.getMessage("BeanNameTurnout"));
            jmi.setEnabled(false);
            inMenu.add(new JSeparator());
            before_mcc += 2;
            addInfoToMenu(throatContinuing, layoutTurnout.getSignalA1Name(), inMenu);
            addInfoToMenu(throatDiverging, layoutTurnout.getSignalA2Name(), inMenu);
            addInfoToMenu(continuing, layoutTurnout.getSignalB1Name(), inMenu);
            addInfoToMenu(diverging, layoutTurnout.getSignalC1Name(), inMenu);
        } else if (linkType == LayoutTurnout.THROAT_TO_THROAT) {
            String text = Bundle.getMessage("ThroatToThroat") + " (";
            text += Bundle.getMessage("BeanNameTurnout") + ", " + Bundle.getMessage("BeanNameRoute");
            text += ", " + Bundle.getMessage("Signal") + ":)";
            JMenuItem jmi = inMenu.add(text);
            jmi.setEnabled(false);
            inMenu.add(new JSeparator());
            before_mcc += 2;
            addInfoToMenu(east + ", " + continuing + ", " + continuing, layoutTurnout1.getSignalB1Name(), inMenu);
            addInfoToMenu(east + ", " + continuing + ", " + diverging, layoutTurnout1.getSignalB2Name(), inMenu);
            addInfoToMenu(east + ", " + diverging + ", " + continuing, layoutTurnout1.getSignalC1Name(), inMenu);
            addInfoToMenu(east + ", " + diverging + ", " + diverging, layoutTurnout1.getSignalC2Name(), inMenu);
            addInfoToMenu(west + ", " + continuing + ", " + continuing, layoutTurnout2.getSignalB1Name(), inMenu);
            addInfoToMenu(west + ", " + continuing + ", " + diverging, layoutTurnout2.getSignalB2Name(), inMenu);
            addInfoToMenu(west + ", " + diverging + ", " + continuing, layoutTurnout2.getSignalC1Name(), inMenu);
            addInfoToMenu(west + ", " + diverging + ", " + diverging, layoutTurnout2.getSignalC2Name(), inMenu);
        } else if (linkType == LayoutTurnout.FIRST_3_WAY) {
            JMenuItem jmi = inMenu.add(Bundle.getMessage("ThreeWay"));
            jmi.setEnabled(false);
            inMenu.add(new JSeparator());
            before_mcc += 2;
            addInfoToMenu(throat + " " + continuing, layoutTurnoutA.getSignalA1Name(), inMenu);
            addInfoToMenu(throat + " " + divergingA, layoutTurnoutA.getSignalA2Name(), inMenu);
            addInfoToMenu(throat + " " + divergingB, layoutTurnoutA.getSignalA3Name(), inMenu);
            addInfoToMenu(continuing, layoutTurnoutA.getSignalC1Name(), inMenu);
            addInfoToMenu(divergingA, layoutTurnoutB.getSignalB1Name(), inMenu);
            addInfoToMenu(divergingB, layoutTurnoutB.getSignalC1Name(), inMenu);
        } else if (linkType == LayoutTurnout.SECOND_3_WAY) {
            JMenuItem jmi = inMenu.add(Bundle.getMessage("ThreeWay"));
            jmi.setEnabled(false);
            inMenu.add(new JSeparator());
            before_mcc += 2;
            addInfoToMenu(throat + " " + continuing, layoutTurnoutB.getSignalA1Name(), inMenu);
            addInfoToMenu(throat + " " + divergingA, layoutTurnoutB.getSignalA2Name(), inMenu);
            addInfoToMenu(throat + " " + divergingB, layoutTurnoutB.getSignalA3Name(), inMenu);
            addInfoToMenu(continuing, layoutTurnoutB.getSignalC1Name(), inMenu);
            addInfoToMenu(divergingA, layoutTurnoutA.getSignalB1Name(), inMenu);
            addInfoToMenu(divergingB, layoutTurnoutA.getSignalC1Name(), inMenu);
        }
        int after_mcc = inMenu.getMenuComponentCount();
        if (before_mcc != after_mcc) {
            inMenu.add(new JSeparator());
            result = true;   // it's GOOD!
        }
        return result;
    }   // addLayoutTurnoutSignalHeadInfoToMenu

    protected Boolean addBlockBoundarySignalHeadInfoToMenu(
            @Nonnull PositionablePoint inPositionablePoint,
            @Nonnull JMenu inMenu) {
        Boolean result = false; // assume failure (pessimist!)

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
            result = true;   // it's GOOD!
        }

        return result;
    }

    protected Boolean addLevelXingSignalHeadInfoToMenu(
            @Nonnull LevelXing inLevelXing,
            @Nonnull JMenu inMenu) {
        Boolean result = false; // assume failure (pessimist!)

        int before_mcc = inMenu.getMenuComponentCount();
        if (before_mcc != 0) {
            inMenu.add(new JSeparator());
        }

        JMenuItem jmi = inMenu.add(Bundle.getMessage("LevelCrossing"));
        jmi.setEnabled(false);
        inMenu.add(new JSeparator());
        before_mcc += 2;

        addInfoToMenu(Bundle.getMessage("TrackXConnect", "A") + " : ", inLevelXing.getSignalAName(), inMenu);
        addInfoToMenu(Bundle.getMessage("TrackXConnect", "B") + " : ", inLevelXing.getSignalBName(), inMenu);
        addInfoToMenu(Bundle.getMessage("TrackXConnect", "C") + " : ", inLevelXing.getSignalCName(), inMenu);
        addInfoToMenu(Bundle.getMessage("TrackXConnect", "D") + " : ", inLevelXing.getSignalDName(), inMenu);

        int after_mcc = inMenu.getMenuComponentCount();
        if (before_mcc != after_mcc) {
            inMenu.add(new JSeparator());
            result = true;   // it's GOOD!
        }

        return result;
    }

    protected Boolean addLayoutSlipSignalHeadInfoToMenu(
            @Nonnull LayoutTurnout inLayoutTurnout,
            @Nonnull JMenu inMenu) {
        Boolean result = false; // assume failure (pessimist!)

        int before_mcc = inMenu.getMenuComponentCount();
        if (before_mcc != 0) {
            inMenu.add(new JSeparator());
        }

        JMenuItem jmi = inMenu.add(Bundle.getMessage("Slip"));
        jmi.setEnabled(false);
        inMenu.add(new JSeparator());
        before_mcc += 2;

        String continuing = Bundle.getMessage("Continuing");
        String diverging = Bundle.getMessage("Diverging");

        addInfoToMenu("A " + continuing, inLayoutTurnout.getSignalA1Name(), inMenu);
        addInfoToMenu("A " + diverging, inLayoutTurnout.getSignalA2Name(), inMenu);
        addInfoToMenu("B " + continuing, inLayoutTurnout.getSignalB1Name(), inMenu);
        addInfoToMenu("B " + diverging, inLayoutTurnout.getSignalB2Name(), inMenu);
        addInfoToMenu("C " + continuing, inLayoutTurnout.getSignalC1Name(), inMenu);
        addInfoToMenu("C " + diverging, inLayoutTurnout.getSignalC2Name(), inMenu);
        addInfoToMenu("D " + continuing, inLayoutTurnout.getSignalD1Name(), inMenu);
        addInfoToMenu("D " + diverging, inLayoutTurnout.getSignalD2Name(), inMenu);

        int after_mcc = inMenu.getMenuComponentCount();
        if (before_mcc != after_mcc) {
            inMenu.add(new JSeparator());
            result = true;   // it's GOOD!
        }

        return result;
    }

    private void addInfoToMenu(@Nullable String title,
            @Nullable String info, @Nonnull JMenu menu) {
        if ((title != null) && !title.isEmpty() && (info != null) && !info.isEmpty()) {
            addInfoToMenu(title + ": " + info, menu);
        }
    }

    private void addInfoToMenu(@Nullable String info, @Nonnull JMenu menu) {
        if ((info != null) && !info.isEmpty()) {
            JMenuItem jmi = new JMenuItem(info);
            jmi.setEnabled(false);
            menu.add(jmi);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutEditorTools.class);
}
