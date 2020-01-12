package jmri.jmrit.turnoutoperations;

import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import jmri.CommonTurnoutOperation;
import jmri.TurnoutOperation;

/**
 * Extension of TurnoutOperationConfig to handle config for common aspects of
 * some subclasses.
 *
 * @author John Harper Copyright 2005
 */
public class CommonTurnoutOperationConfig extends TurnoutOperationConfig {
    JSpinner intervalSpinner;
    JSpinner maxTriesSpinner;
    CommonTurnoutOperation myOp;

    /**
     * Create the config JPanel, if there is one, to configure this operation
     * type.
     */
    public CommonTurnoutOperationConfig(TurnoutOperation op) {
        super(op);
        myOp = (CommonTurnoutOperation) op;

        maxTriesSpinner = new JSpinner();
        intervalSpinner = new JSpinner();
        Box vbox = Box.createVerticalBox();
        Box hbox1 = Box.createHorizontalBox();
        Box hbox2 = Box.createHorizontalBox();
        vbox.add(hbox2); //Show TimesToTry first, keeping to the order of the help text at right
        vbox.add(hbox1); //Show Interval next
        vbox.add(Box.createVerticalGlue());
        hbox1.add(new JLabel(Bundle.getMessage("Interval")));
        hbox1.add(Box.createHorizontalGlue());
        intervalSpinner.setMinimumSize(new Dimension(100, 20));

        intervalSpinner.setModel(
                new SpinnerNumberModel(myOp.getInterval(),
                        CommonTurnoutOperation.minInterval, CommonTurnoutOperation.maxInterval,
                        CommonTurnoutOperation.intervalStepSize)); // val, min, max, step

        hbox1.add(intervalSpinner);
        hbox2.add(new JLabel(Bundle.getMessage("TimesToTry")));
        hbox2.add(Box.createHorizontalGlue());
        maxTriesSpinner.setMinimumSize(new Dimension(100, 20));

        maxTriesSpinner.setModel(
                new SpinnerNumberModel(myOp.getMaxTries(),
                        CommonTurnoutOperation.minMaxTries, CommonTurnoutOperation.maxMaxTries, 1)); // val, min, max, step

        hbox2.add(maxTriesSpinner);

        Box hbox3 = Box.createHorizontalBox();
        hbox3.add(Box.createHorizontalStrut(150));
        vbox.add(hbox3);
        add(vbox);
    }

    /**
     * Called when OK button pressed in config panel, to retrieve and set new
     * values.
     */
    @Override
    public void endConfigure() {
        int newInterval = ((Integer) intervalSpinner.getValue()).intValue();
        myOp.setInterval(newInterval);
        int newMaxTries = ((Integer) maxTriesSpinner.getValue()).intValue();
        myOp.setMaxTries(newMaxTries);
    }

}
