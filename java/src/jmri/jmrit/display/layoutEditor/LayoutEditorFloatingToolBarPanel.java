package jmri.jmrit.display.layoutEditor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.FlowLayout;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.*;

/**
 * This is the floating toolbar panel
 *
 * @author George Warner Copyright: (c) 2017-2019
 */
public class LayoutEditorFloatingToolBarPanel extends LayoutEditorToolBarPanel {

    private JPanel floatEditTabsPanel = new JPanel();
    private JTabbedPane floatEditTabsPane = new JTabbedPane();

    /**
     * constructor for LayoutEditorFloatingToolBarPanel
     *
     * @param layoutEditor the layout editor that this is for
     */
    public LayoutEditorFloatingToolBarPanel(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);    
        localLayoutComponents();    
    }

    // super.setupComponents() used as-is. super.layoutComponents() is 
    // suppressed, as it doesn't need to run. localLayoutComponents()
    // is then invoked by this class's constructor after the initializers
    // are complete. (Putting this into an override of layoutComponents would
    // not be correct, as that is invoked from the superclass constructor before
    // this classes initializers are run; local reference members will be 
    // overwritten when that happens.)
    
    /**
     * Deliberately not running the superclass
     * {@link LayoutEditorToolBarPanel#layoutComponents()}
     * method.
     */
    @Override
    final protected void layoutComponents() {
    }
    
    /**
     * Local configuration of Swing components run as 
     * last phase of construction.
     */
    private void localLayoutComponents() {    
        /*
         *  JPanel - floatEditTabsPanel
         *      JTabbedPane - floatEditTabsPane
         *          ...
         *      JPanel - floatEditLocationPanel
         *          ...
         *      JPanel - floatEditActionPanel  (currently disabled)
         *          ...
         *      JPanel - floatEditHelpPanel
         *          ...
         */

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        FlowLayout floatContentLayout = new FlowLayout(FlowLayout.CENTER, 5, 2); //5 pixel gap between items, 2 vertical gap

        // Contains the block and sensor combo boxes.
        // It is moved to the appropriate detail pane when the tab changes.
        blockPropertiesPanel = new JPanel();
        blockPropertiesPanel.setLayout(new BoxLayout(blockPropertiesPanel, BoxLayout.Y_AXIS));

        JPanel blockPanel = new JPanel(floatContentLayout);
        blockPanel.add(blockLabel);
        blockPanel.add(blockIDComboBox);
        blockPanel.add(highlightBlockCheckBox);
        blockPropertiesPanel.add(blockPanel);

        JPanel blockSensorPanel = new JPanel(floatContentLayout);
        blockSensorPanel.add(blockSensorLabel);
        blockSensorPanel.add(blockSensorComboBox);
        blockSensorPanel.setBorder(new EmptyBorder(0, 20, 0, 0));
        blockPropertiesPanel.add(blockSensorPanel);

        // Begin the tabs structure
        //
        // Tab 0 - Turnouts
        JPanel floatEditTurnout = new JPanel();
        floatEditTurnout.setLayout(new BoxLayout(floatEditTurnout, BoxLayout.Y_AXIS));

        JPanel turnoutGroup1 = new JPanel(floatContentLayout);
        turnoutGroup1.add(turnoutRHButton);
        turnoutGroup1.add(turnoutLHButton);
        turnoutGroup1.add(turnoutWYEButton);
        turnoutGroup1.add(layoutSingleSlipButton);
        turnoutGroup1.add(layoutDoubleSlipButton);
        floatEditTurnout.add(turnoutGroup1);

        JPanel turnoutGroup2 = new JPanel(floatContentLayout);
        turnoutGroup2.add(doubleXoverButton);
        turnoutGroup2.add(rhXoverButton);
        turnoutGroup2.add(lhXoverButton);
        floatEditTurnout.add(turnoutGroup2);

        JPanel turnoutGroup3 = new JPanel(floatContentLayout);
        turnoutGroup3.add(turnoutNamePanel);
        floatEditTurnout.add(turnoutGroup3);

        JPanel turnoutGroup3a = new JPanel(floatContentLayout);
        turnoutGroup3a.add(extraTurnoutPanel);
        floatEditTurnout.add(turnoutGroup3a);

        JPanel turnoutGroup4 = new JPanel(floatContentLayout);
        turnoutGroup4.add(rotationPanel);
        floatEditTurnout.add(turnoutGroup4);

        floatEditTurnout.add(blockPropertiesPanel);

        floatEditTabsPane.addTab(Bundle.getMessage("Turnouts"), null, floatEditTurnout, null);

        // Tab 1 - Track
        JPanel floatEditTrack = new JPanel();
        floatEditTrack.setLayout(new BoxLayout(floatEditTrack, BoxLayout.Y_AXIS));

        JPanel trackGroup1 = new JPanel(floatContentLayout);
        trackGroup1.add(endBumperButton);
        trackGroup1.add(anchorButton);
        trackGroup1.add(edgeButton);
        floatEditTrack.add(trackGroup1);

        JPanel trackGroup2 = new JPanel(floatContentLayout);
        trackGroup2.add(trackButton);
        trackGroup2.add(levelXingButton);
        floatEditTrack.add(trackGroup2);

        JPanel trackGroup3 = new JPanel(floatContentLayout);
        trackGroup3.add(trackSegmentPropertiesPanel);
        floatEditTrack.add(trackGroup3);

        floatEditTabsPane.addTab(Bundle.getMessage("TabTrack"), null, floatEditTrack, null);

        // Tab 2 - Labels
        JPanel floatEditLabel = new JPanel();
        floatEditLabel.setLayout(new BoxLayout(floatEditLabel, BoxLayout.Y_AXIS));

        JPanel labelGroup1 = new JPanel(floatContentLayout);
        labelGroup1.add(textLabelButton);
        labelGroup1.add(textLabelTextField);
        floatEditLabel.add(labelGroup1);

        JPanel labelGroup2 = new JPanel(floatContentLayout);
        labelGroup2.add(memoryButton);
        labelGroup2.add(textMemoryComboBox);
        floatEditLabel.add(labelGroup2);

        JPanel labelGroup3 = new JPanel(floatContentLayout);
        labelGroup3.add(blockContentsButton);
        labelGroup3.add(blockContentsComboBox);
        floatEditLabel.add(labelGroup3);

        floatEditTabsPane.addTab(Bundle.getMessage("TabLabel"), null, floatEditLabel, null);

        // Tab 3 - Icons
        JPanel floatEditIcon = new JPanel();
        floatEditIcon.setLayout(new BoxLayout(floatEditIcon, BoxLayout.Y_AXIS));

        JPanel iconGroup1 = new JPanel(floatContentLayout);
        iconGroup1.add(multiSensorButton);
        iconGroup1.add(changeIconsButton);
        floatEditIcon.add(iconGroup1);

        JPanel iconGroup2 = new JPanel(floatContentLayout);
        iconGroup2.add(sensorButton);
        iconGroup2.add(sensorComboBox);
        floatEditIcon.add(iconGroup2);

        JPanel iconGroup3 = new JPanel(floatContentLayout);
        iconGroup3.add(signalMastButton);
        iconGroup3.add(signalMastComboBox);
        floatEditIcon.add(iconGroup3);

        JPanel iconGroup4 = new JPanel(floatContentLayout);
        iconGroup4.add(signalButton);
        iconGroup4.add(signalHeadComboBox);
        floatEditIcon.add(iconGroup4);

        JPanel iconGroup5 = new JPanel(floatContentLayout);
        iconGroup5.add(iconLabelButton);
        iconGroup5.add(shapeButton);
        floatEditIcon.add(iconGroup5);

        floatEditTabsPane.addTab(Bundle.getMessage("TabIcon"), null, floatEditIcon, null);
        floatEditTabsPanel.add(floatEditTabsPane);
        add(floatEditTabsPanel);

        // End the tabs structure
        // The next 3 groups reside under the tab secton
        JPanel floatEditLocationPanel = new JPanel();
        floatEditLocationPanel.add(zoomPanel);
        floatEditLocationPanel.add(locationPanel);
        add(floatEditLocationPanel);

        floatEditTabsPane.addChangeListener((e) -> {
            //Move the block group between the turnouts and track tabs
            int selIndex = floatEditTabsPane.getSelectedIndex();

            if (selIndex == 0) {
                floatEditTurnout.add(blockPropertiesPanel);
            } else if (selIndex == 1) {
                floatEditTrack.add(blockPropertiesPanel);
            }
        });
        floatEditTabsPane.setSelectedIndex(0);
        floatEditTurnout.add(blockPropertiesPanel);
    }

    public JTabbedPane getfloatEditTabsPane() {
        return floatEditTabsPane;
    }

    // initialize logging
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorFloatingToolBarPanel.class);
}
