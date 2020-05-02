package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.annotation.*;
import javax.swing.*;
import javax.swing.border.*;
import jmri.*;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.display.layoutEditor.*;
import jmri.jmrit.display.layoutEditor.LayoutTurntable.RayTrack;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;

/**
 * MVC Editor component for LayoutSlip objects.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LayoutSlipEditor extends LayoutTurnoutEditor {

    /**
     * constructor method
     */
    public LayoutSlipEditor(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
    }

    /*================*\
    | Edit Layout Slip |
    \*================*/
    // variables for Edit slip Crossing pane
    private LayoutSlip layoutSlip = null;

    private JmriJFrame editLayoutSlipFrame = null;
    private JButton editLayoutSlipBlockButton;
    private NamedBeanComboBox<Turnout> editLayoutSlipTurnoutAComboBox;
    private NamedBeanComboBox<Turnout> editLayoutSlipTurnoutBComboBox;
    private final JCheckBox editLayoutSlipHiddenBox = new JCheckBox(Bundle.getMessage("HideSlip"));
    private final NamedBeanComboBox<Block> editLayoutSlipBlockNameComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(BlockManager.class), null, DisplayOptions.DISPLAYNAME);

    private boolean editLayoutSlipOpen = false;
    private boolean editLayoutSlipNeedsRedraw = false;
    private boolean editLayoutSlipNeedsBlockUpdate = false;

    /**
     * Edit a Slip.
     */
    public void editLayoutSlip(LayoutSlip layoutSlip) {
        this.layoutSlip = layoutSlip;
        sensorList.clear();

        if (editLayoutSlipOpen) {
            editLayoutSlipFrame.setVisible(true);
        } else if (editLayoutSlipFrame == null) {   // Initialize if needed
            editLayoutSlipFrame = new JmriJFrame(Bundle.getMessage("EditSlip"), false, true);  // NOI18N
            editLayoutSlipFrame.addHelpMenu("package.jmri.jmrit.display.EditLayoutSlip", true);  // NOI18N
            editLayoutSlipFrame.setLocation(50, 30);

            Container contentPane = editLayoutSlipFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

            // Setup turnout A
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel turnoutNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " A");  // NOI18N
            panel1.add(turnoutNameLabel);
            editLayoutSlipTurnoutAComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(TurnoutManager.class));
            editLayoutSlipTurnoutAComboBox.setToolTipText(Bundle.getMessage("EditTurnoutToolTip"));
            LayoutEditor.setupComboBox(editLayoutSlipTurnoutAComboBox, false, true, false);
            turnoutNameLabel.setLabelFor(editLayoutSlipTurnoutAComboBox);
            panel1.add(editLayoutSlipTurnoutAComboBox);
            contentPane.add(panel1);

            // Setup turnout B
            JPanel panel1a = new JPanel();
            panel1a.setLayout(new FlowLayout());
            JLabel turnoutBNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " B");  // NOI18N
            panel1a.add(turnoutBNameLabel);
            editLayoutSlipTurnoutBComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(TurnoutManager.class));
            editLayoutSlipTurnoutBComboBox.setToolTipText(Bundle.getMessage("EditTurnoutToolTip"));
            LayoutEditor.setupComboBox(editLayoutSlipTurnoutBComboBox, false, true, false);
            turnoutBNameLabel.setLabelFor(editLayoutSlipTurnoutBComboBox);
            panel1a.add(editLayoutSlipTurnoutBComboBox);

            contentPane.add(panel1a);

            JPanel panel2 = new JPanel();
            panel2.setLayout(new GridLayout(0, 3, 2, 2));

            panel2.add(new Label("   "));
            panel2.add(new Label(Bundle.getMessage("BeanNameTurnout") + " A:"));  // NOI18N
            panel2.add(new Label(Bundle.getMessage("BeanNameTurnout") + " B:"));  // NOI18N
            for (Map.Entry<Integer, LayoutSlip.TurnoutState> ts : layoutSlip.getTurnoutStates().entrySet()) {
                SampleStates draw = new SampleStates(ts.getKey());
                draw.repaint();
                draw.setPreferredSize(new Dimension(40, 40));
                panel2.add(draw);

                panel2.add(ts.getValue().getComboA());
                panel2.add(ts.getValue().getComboB());
            }

            testPanel = new TestState();
            testPanel.setSize(40, 40);
            testPanel.setPreferredSize(new Dimension(40, 40));
            panel2.add(testPanel);
            JButton testButton = new JButton("Test");  // NOI18N
            testButton.addActionListener((ActionEvent e) -> toggleStateTest());
            panel2.add(testButton);
            contentPane.add(panel2);

            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            editLayoutSlipHiddenBox.setToolTipText(Bundle.getMessage("HiddenToolTip"));  // NOI18N
            panel33.add(editLayoutSlipHiddenBox);
            contentPane.add(panel33);

            // setup block name
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            JLabel block1NameLabel = new JLabel(Bundle.getMessage("BlockID"));  // NOI18N
            panel3.add(block1NameLabel);
            block1NameLabel.setLabelFor(editLayoutSlipBlockNameComboBox);
            panel3.add(editLayoutSlipBlockNameComboBox);
            LayoutEditor.setupComboBox(editLayoutSlipBlockNameComboBox, false, true, true);
            editLayoutSlipBlockNameComboBox.setToolTipText(Bundle.getMessage("EditBlockNameHint"));  // NOI18N

            contentPane.add(panel3);
            // set up Edit Block buttons
            JPanel panel4 = new JPanel();
            panel4.setLayout(new FlowLayout());
            // Edit Block
            panel4.add(editLayoutSlipBlockButton = new JButton(Bundle.getMessage("EditBlock", "")));  // NOI18N
            editLayoutSlipBlockButton.addActionListener(this::editLayoutSlipEditBlockPressed
            );
            editLayoutSlipBlockButton.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1  // NOI18N

            contentPane.add(panel4);

            // set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            addDoneCancelButtons(panel5, editLayoutSlipFrame.getRootPane(),
                    this::editLayoutSlipDonePressed, this::editLayoutSlipCancelPressed);
            contentPane.add(panel5);
        }

        editLayoutSlipHiddenBox.setSelected(layoutSlip.isHidden());

        // Set up for Edit
        List<Turnout> currentTurnouts = new ArrayList<>();
        currentTurnouts.add(layoutSlip.getTurnout());
        currentTurnouts.add(layoutSlip.getTurnoutB());

        editLayoutSlipTurnoutAComboBox.setSelectedItem(layoutSlip.getTurnout());
        editLayoutSlipTurnoutAComboBox.addPopupMenuListener(
                layoutEditor.newTurnoutComboBoxPopupMenuListener(editLayoutSlipTurnoutAComboBox, currentTurnouts));

        editLayoutSlipTurnoutBComboBox.setSelectedItem(layoutSlip.getTurnoutB());
        editLayoutSlipTurnoutBComboBox.addPopupMenuListener(
                layoutEditor.newTurnoutComboBoxPopupMenuListener(editLayoutSlipTurnoutBComboBox, currentTurnouts));

        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        editLayoutSlipBlockNameComboBox.getEditor().setItem(bm.getBlock(layoutSlip.getBlockName()));
        editLayoutSlipBlockNameComboBox.setEnabled(!hasNxSensorPairs(layoutSlip.getLayoutBlock()));

        editLayoutSlipFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                editLayoutSlipCancelPressed(null);
            }
        });
        editLayoutSlipFrame.pack();
        editLayoutSlipFrame.setVisible(true);
        editLayoutSlipOpen = true;
        editLayoutSlipNeedsBlockUpdate = false;

        showSensorMessage();
    }   // editLayoutSlip

    private void drawSlipState(Graphics2D g2, int state) {
        Point2D cenP = layoutSlip.getCoordsCenter();
        Point2D A = MathUtil.subtract(layoutSlip.getCoordsA(), cenP);
        Point2D B = MathUtil.subtract(layoutSlip.getCoordsB(), cenP);
        Point2D C = MathUtil.subtract(layoutSlip.getCoordsC(), cenP);
        Point2D D = MathUtil.subtract(layoutSlip.getCoordsD(), cenP);

        Point2D ctrP = new Point2D.Double(20.0, 20.0);
        A = MathUtil.add(MathUtil.normalize(A, 18.0), ctrP);
        B = MathUtil.add(MathUtil.normalize(B, 18.0), ctrP);
        C = MathUtil.add(MathUtil.normalize(C, 18.0), ctrP);
        D = MathUtil.add(MathUtil.normalize(D, 18.0), ctrP);

        g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        g2.draw(new Line2D.Double(A, MathUtil.oneThirdPoint(A, C)));
        g2.draw(new Line2D.Double(C, MathUtil.oneThirdPoint(C, A)));

        if (state == LayoutTurnout.STATE_AC || state == LayoutTurnout.STATE_BD || state == LayoutTurnout.UNKNOWN) {
            g2.draw(new Line2D.Double(A, MathUtil.oneThirdPoint(A, D)));
            g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, A)));

            if (layoutSlip.getSlipType() == LayoutTurnout.TurnoutType.DOUBLE_SLIP) {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, C)));
                g2.draw(new Line2D.Double(C, MathUtil.oneThirdPoint(C, B)));
            }
        } else {
            g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
            g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));
        }

        if (layoutSlip.getSlipType() == LayoutTurnout.TurnoutType.DOUBLE_SLIP) {
            if (state == LayoutTurnout.STATE_AC) {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));

                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, C));
            } else if (state == LayoutTurnout.STATE_BD) {
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B, D));
            } else if (state == LayoutTurnout.STATE_AD) {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, C)));

                g2.draw(new Line2D.Double(C, MathUtil.oneThirdPoint(C, B)));

                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, D));
            } else if (state == LayoutTurnout.STATE_BC) {
                g2.draw(new Line2D.Double(A, MathUtil.oneThirdPoint(A, D)));

                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, A)));
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B, C));
            } else {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));
            }
        } else {
            g2.draw(new Line2D.Double(A, MathUtil.oneThirdPoint(A, D)));
            g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, A)));

            if (state == LayoutTurnout.STATE_AD) {
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, D));
            } else if (state == LayoutTurnout.STATE_AC) {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));

                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, C));
            } else if (state == LayoutTurnout.STATE_BD) {
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B, D));
            } else {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));
            }
        }
    }   // drawSlipState

    class SampleStates extends JPanel {

        // Methods, constructors, fields.
        SampleStates(int state) {
            super();
            this.state = state;
        }
        int state;

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);    // paints background
            if (g instanceof Graphics2D) {
                drawSlipState((Graphics2D) g, state);
            }
        }
    }

    private int testState = LayoutTurnout.UNKNOWN;

    /**
     * Toggle slip states if clicked on, physical turnout exists, and not
     * disabled.
     */
    public void toggleStateTest() {
        int turnAState;
        int turnBState;
        switch (testState) {
            default:
            case LayoutTurnout.STATE_AC: {
                testState = LayoutTurnout.STATE_AD;
                break;
            }

            case LayoutTurnout.STATE_BD: {
                if (layoutSlip.getSlipType() == LayoutTurnout.TurnoutType.SINGLE_SLIP) {
                    testState = LayoutTurnout.STATE_AC;
                } else {
                    testState = LayoutTurnout.STATE_BC;
                }
                break;
            }

            case LayoutTurnout.STATE_AD: {
                testState = LayoutTurnout.STATE_BD;
                break;
            }

            case LayoutTurnout.STATE_BC: {
                testState = LayoutTurnout.STATE_AC;
                break;
            }
        }
        turnAState = layoutSlip.getTurnoutStates().get(testState).getTestTurnoutAState();
        turnBState = layoutSlip.getTurnoutStates().get(testState).getTestTurnoutBState();

        if (editLayoutSlipTurnoutAComboBox.getSelectedItem() != null) {
            editLayoutSlipTurnoutAComboBox.getSelectedItem().setCommandedState(turnAState);
        }
        if (editLayoutSlipTurnoutBComboBox.getSelectedItem() != null) {
            editLayoutSlipTurnoutBComboBox.getSelectedItem().setCommandedState(turnBState);
        }
        if (testPanel != null) {
            testPanel.repaint();
        }
    }

    class TestState extends JPanel {

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (g instanceof Graphics2D) {
                drawSlipState((Graphics2D) g, testState);
            }
        }
    }

    private TestState testPanel;

    private void editLayoutSlipEditBlockPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = editLayoutSlipBlockNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutSlip.getBlockName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutSlip.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            editLayoutSlipNeedsRedraw = true;
            editLayoutSlipNeedsBlockUpdate = true;
        }
        // check if a block exists to edit
        if (layoutSlip.getLayoutBlock() == null) {
            JOptionPane.showMessageDialog(editLayoutSlipFrame,
                    Bundle.getMessage("Error1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        layoutSlip.getLayoutBlock().editLayoutBlock(editLayoutSlipFrame);
        editLayoutSlipNeedsRedraw = true;
        layoutEditor.setDirty();
    }

    private void editLayoutSlipDonePressed(ActionEvent a) {
        String newName = editLayoutSlipTurnoutAComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutSlip.getTurnoutName().equals(newName)) {
            if (layoutEditor.validatePhysicalTurnout(newName, editLayoutSlipFrame)) {
                layoutSlip.setTurnout(newName);
            } else {
                layoutSlip.setTurnout("");
            }
            editLayoutSlipNeedsRedraw = true;
        }

        newName = editLayoutSlipTurnoutBComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutSlip.getTurnoutBName().equals(newName)) {
            if (layoutEditor.validatePhysicalTurnout(newName, editLayoutSlipFrame)) {
                layoutSlip.setTurnoutB(newName);
            } else {
                layoutSlip.setTurnoutB("");
            }
            editLayoutSlipNeedsRedraw = true;
        }

        newName = editLayoutSlipBlockNameComboBox.getSelectedItemDisplayName();
        if (newName == null) {
            newName = "";
        }
        if (!layoutSlip.getBlockName().equals(newName)) {
            // get new block, or null if block has been removed
            layoutSlip.setLayoutBlock(layoutEditor.provideLayoutBlock(newName));
            editLayoutSlipNeedsRedraw = true;
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            editLayoutSlipNeedsBlockUpdate = true;
        }
        for (LayoutSlip.TurnoutState ts : layoutSlip.getTurnoutStates().values()) {
            ts.updateStatesFromCombo();
        }

        // set hidden
        boolean oldHidden = layoutSlip.isHidden();
        layoutSlip.setHidden(editLayoutSlipHiddenBox.isSelected());
        if (oldHidden != layoutSlip.isHidden()) {
            editLayoutSlipNeedsRedraw = true;
        }

        editLayoutSlipOpen = false;
        editLayoutSlipFrame.setVisible(false);
        editLayoutSlipFrame.dispose();
        editLayoutSlipFrame = null;
        if (editLayoutSlipNeedsBlockUpdate) {
            layoutSlip.updateBlockInfo();
        }
        if (editLayoutSlipNeedsRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            editLayoutSlipNeedsRedraw = false;
        }
    }

    private void editLayoutSlipCancelPressed(ActionEvent a) {
        editLayoutSlipOpen = false;
        editLayoutSlipFrame.setVisible(false);
        editLayoutSlipFrame.dispose();
        editLayoutSlipFrame = null;
        if (editLayoutSlipNeedsBlockUpdate) {
            layoutSlip.updateBlockInfo();
        }
        if (editLayoutSlipNeedsRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
            editLayoutSlipNeedsRedraw = false;
        }
    }
    

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSlipEditor.class);
}
