package jmri.jmrit.operations.trains;

import java.awt.Dimension;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.tools.ShowCarsInTrainAction;
import jmri.jmrit.operations.trains.tools.ToggleShowFullLocationName;

/**
 * Conductor Frame. Shows work for a train one location at a time.
 *
 * @author Dan Boudreau Copyright (C) 2011, 2013
 * 
 */
public class TrainConductorFrame extends OperationsFrame {

    public TrainConductorFrame(Train train) {
        super(new TrainConductorPanel(train));
        this.initComponents(train);
    }

    private void initComponents(Train train) {
        super.initComponents();

        if (train != null) {
            setTitle(Bundle.getMessage("TitleTrainConductor") + " (" + train.getName() + ")");
        }

        // build menu
        JMenuBar menuBar = new JMenuBar();
        if (train != null) {
            JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
            toolMenu.add(new ShowCarsInTrainAction(train));
            toolMenu.add(new ToggleShowFullLocationName());
            menuBar.add(toolMenu);
        }
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_TrainConductor", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight500));
    }
}
