package jmri.jmrit.dispatcher;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import java.beans.PropertyChangeEvent;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicToolBarUI;

import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AutoTrainsFrame provides a user interface to trains that are running
 * automatically under Dispatcher.
 * <p>
 * There is only one AutoTrains window. AutoTrains are added and deleted from
 * this window as they are added or terminated.
 * <p>
 * This file is part of JMRI.
 * <p>
 * JMRI is open source software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Dave Duchamp Copyright (C) 2010
 */
public class AutoTrainsFrame extends jmri.util.JmriJFrame {

    public AutoTrainsFrame(DispatcherFrame disp) {
        super(false, true);
        initializeAutoTrainsWindow();
    }

    // instance variables
    private final ArrayList<AutoActiveTrain> _autoTrainsList = new ArrayList<>();
    //Keep track of throttle and listeners to update frame with their current state.

    // accessor functions
    public ArrayList<AutoActiveTrain> getAutoTrainsList() {
        return _autoTrainsList;
    }

    /**
     * Creates and initializes a new control of type AutoTrainControl
     * @param autoActiveTrain the new train.
     */
    public void addAutoActiveTrain(AutoActiveTrain autoActiveTrain) {
        if (autoActiveTrain != null) {
            log.debug("Adding ActiveTrain[{}]",autoActiveTrain.getActiveTrain().getActiveTrainName());
            AbstractAutoTrainControl atn;
            if (useClassicControl.isSelected()) {
                atn = new AutoTrainControlDefault(autoActiveTrain);
            } else {
                atn = new AutoEngineerMicro(autoActiveTrain);
            }
            atn.setOnTopOnSpeedChange(frameOnTopOnSpeedChange.isSelected());
            // AutoTrainControl atn = new AutoTrainControl(autoActiveTrain);
            if (!trainsCanBeFloated.isSelected()) {
                atn.componentJPanel.setFloatable(false);
            }
            trainsPanel.add(atn);
            atn.addPropertyChangeListener("terminated", (PropertyChangeEvent e) -> {
                AbstractAutoTrainControl atnn = (AbstractAutoTrainControl) e.getSource();
                // must be attached to make it really go away
                ((BasicToolBarUI) atnn.componentJPanel.getUI()).setFloating(false,null);
                trainsPanel.remove((AbstractAutoTrainControl) e.getSource());
                pack();
            });
            // bit of overkill for when a floater floats and comes back.
            atn.componentJPanel.addAncestorListener ( new AncestorListener ()
            {
                @Override
                public void ancestorAdded ( AncestorEvent event )
                {
                    log.trace("ancestorAdded");
                    pack();
                }
                @Override
                public void ancestorRemoved ( AncestorEvent event )
                {
                    log.trace("ancestorRemoved");
                    pack();
                }
                @Override
                public void ancestorMoved ( AncestorEvent event )
                {
                    // blank.
                }
              } );
            // bit of overkill for when a floater floats and comes back.
            atn.componentJPanel.addAncestorListener ( new AncestorListener ()
            {
                @Override
                public void ancestorAdded ( AncestorEvent event )
                {
                    log.trace("ancestorAdded");
                    pack();
                }
                @Override
                public void ancestorRemoved ( AncestorEvent event )
                {
                    log.trace("ancestorRemoved");
                    pack();
                }
                @Override
                public void ancestorMoved ( AncestorEvent event )
                {
                    // blank.
                }
              } );

            pack();
        }
    }

    @Override
    public void componentResized(ComponentEvent ce) {
        pack();
    }

    // variables for AutoTrains window
    protected JmriJFrame autoTrainsFrame = null;
    private JPanel trainsPanel;
    private JScrollPane trainScrollPanel;
    private JCheckBoxMenuItem frameHasScrollBars = new JCheckBoxMenuItem(Bundle.getMessage("AutoTrainsFrameUseScrollBars"));
    private JCheckBoxMenuItem trainsCanBeFloated = new JCheckBoxMenuItem(Bundle.getMessage("AutoTrainsFrameAllowFloat"));
    private JCheckBoxMenuItem frameAlwaysOnTop = new JCheckBoxMenuItem(Bundle.getMessage("AutoTrainsFrameAlwaysOnTop"));
    private JCheckBoxMenuItem frameOnTopOnSpeedChange = new JCheckBoxMenuItem(Bundle.getMessage("AutoTrainsFrameOnTopOnSpeedChange"));

    private JCheckBoxMenuItem useClassicControl = new JCheckBoxMenuItem(Bundle.getMessage("AutoTrainsUseClassicControl"));

    jmri.UserPreferencesManager prefMan;

    private void initializeAutoTrainsWindow() {

        prefMan = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        frameHasScrollBars.setSelected(prefMan.getCheckboxPreferenceState(hasScrollBars,false));
        trainsCanBeFloated.setSelected(prefMan.getCheckboxPreferenceState(canFloat,false));
        frameAlwaysOnTop.setSelected(prefMan.getCheckboxPreferenceState(alWaysOnTop,false));
        frameOnTopOnSpeedChange.setSelected(prefMan.getCheckboxPreferenceState(onTopOnSpeedChange,false));
        useClassicControl.setSelected(prefMan.getCheckboxPreferenceState(classicControl,true));

        autoTrainsFrame = this;
        autoTrainsFrame.setTitle(Bundle.getMessage("TitleAutoTrains"));
        trainsPanel = new JPanel();
        trainsPanel.setLayout(new BoxLayout(trainsPanel, BoxLayout.Y_AXIS));
        JMenuBar menuBar = new JMenuBar();
        JMenu optMenu = new JMenu(Bundle.getMessage("MenuOptions")); // NOI18N
        optMenu.add(frameHasScrollBars);
        frameHasScrollBars.addActionListener(e -> {
            setScrollBars();
        });

        optMenu.add(trainsCanBeFloated);
        trainsCanBeFloated.addActionListener(e -> {
            for (Object ob : trainsPanel.getComponents()) {
                if (ob instanceof AbstractAutoTrainControl) {
                    AbstractAutoTrainControl atnn = (AbstractAutoTrainControl) ob;
                    if (trainsCanBeFloated.isSelected()) {
                        atnn.componentJPanel.setFloatable(true);
                    } else {
                        // rejoin floating throttles before banning
                        // floating.
                        ((BasicToolBarUI) atnn.componentJPanel.getUI()).setFloating(false, null);
                        atnn.componentJPanel.setFloatable(false);
                    }
                }
            }
        });

        optMenu.add(frameAlwaysOnTop);
        frameAlwaysOnTop.addActionListener(e -> {
            setAlwaysOnTop(frameAlwaysOnTop.isSelected());
        });

        optMenu.add(frameOnTopOnSpeedChange);
        frameOnTopOnSpeedChange.addActionListener(e -> {
            for (Object ob : trainsPanel.getComponents()) {
                if (ob instanceof AbstractAutoTrainControl) {
                    AutoTrainControlDefault atnn = (AutoTrainControlDefault) ob;
                    atnn.setOnTopOnSpeedChange(frameOnTopOnSpeedChange.isSelected());
                }
            }
        });

        optMenu.add(useClassicControl);

        menuBar.add(optMenu);

        setJMenuBar(menuBar);
        autoTrainsFrame.addHelpMenu("package.jmri.jmrit.dispatcher.AutoTrains", true);
        trainsPanel.setLayout(new BoxLayout(trainsPanel, BoxLayout.Y_AXIS));
        JPanel pB = new JPanel();
        pB.setLayout(new FlowLayout());
        JButton stopAllButton = new JButton(Bundle.getMessage("StopAll"));
        pB.add(stopAllButton);
        stopAllButton.addActionListener(this::stopAllPressed);
        stopAllButton.setToolTipText(Bundle.getMessage("StopAllButtonHint"));
        trainsPanel.add(pB);
        trainsPanel.add(new JSeparator());
        trainsPanel.addComponentListener(this);
        trainsPanel.setVisible(true);
        trainsPanel.revalidate();
        trainScrollPanel = new JScrollPane();
        trainScrollPanel.getViewport().add(trainsPanel);
        autoTrainsFrame.getContentPane().setLayout(new BoxLayout(autoTrainsFrame.getContentPane(), BoxLayout.Y_AXIS));
        autoTrainsFrame.getContentPane().add(trainScrollPanel);
        setScrollBars();
        autoTrainsFrame.getContentPane().revalidate();
        autoTrainsFrame.pack();
        autoTrainsFrame.setVisible(true);

    }

    private void setScrollBars() {
        if (frameHasScrollBars.isSelected()) {
            trainScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            trainScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            autoTrainsFrame.getContentPane().revalidate();
        } else {
            trainScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            trainScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            autoTrainsFrame.getContentPane().revalidate();
        }
    }

    private void stopAllPressed(ActionEvent e) {
        for (Object ob: trainsPanel.getComponents()) {
            if (ob instanceof AbstractAutoTrainControl) {
                ((AbstractAutoTrainControl) ob).stopAll();
            }
        }
    }

    @Override
    public void dispose() {
        if (prefMan!=null) {
            prefMan.setSimplePreferenceState(hasScrollBars, frameHasScrollBars.isSelected());
            prefMan.setSimplePreferenceState(canFloat, trainsCanBeFloated.isSelected());
            prefMan.setSimplePreferenceState(hasScrollBars, frameHasScrollBars.isSelected());
            prefMan.setSimplePreferenceState(canFloat, trainsCanBeFloated.isSelected());
            prefMan.setSimplePreferenceState(classicControl, useClassicControl.isSelected());
        }
        super.dispose();
    }
    String hasScrollBars = this.getClass().getName() + ".HasScrollBars"; // NOI18N
    String canFloat = this.getClass().getName() + ".CanFloat"; // NOI18N
    String alWaysOnTop = this.getClass().getName() + ".AlWaysOnTop"; // NOI18N
    String onTopOnSpeedChange = this.getClass().getName() + ".OnTopOnSpeedChange"; // NOI18N
    String classicControl = this.getClass().getName() + ".UseClassicControl"; // NOI18N

        private boolean useOnTopOnSpeedChange;


    private final static Logger log = LoggerFactory.getLogger(AutoTrainsFrame.class);

}


