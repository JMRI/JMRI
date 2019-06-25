package jmri.jmrit.beantable.signalmast;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import jmri.*;
import jmri.implementation.TurnoutSignalMast;
import jmri.util.swing.BeanSelectCreatePanel;

import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring TurnoutSignalMast objects.
 *
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2017, 2018
 * @since 4.11.2
 */
public class TurnoutSignalMastAddPane extends SignalMastAddPane {

    public TurnoutSignalMastAddPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // lit/unlit controls
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("AllowUnLitLabel") + ": "));
        p.add(allowUnLit);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(p);
        
        turnoutMastScroll = new JScrollPane(turnoutMastPanel);
        turnoutMastScroll.setBorder(BorderFactory.createEmptyBorder());
        add(turnoutMastScroll);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("TurnCtlMast");
    }

    JPanel turnoutMastPanel = new JPanel();
    JScrollPane turnoutMastScroll;
    JCheckBox resetPreviousState = new JCheckBox(Bundle.getMessage("ResetPrevious"));

    JCheckBox allowUnLit = new JCheckBox();
    
    LinkedHashMap<String, TurnoutAspectPanel> turnoutAspect = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT); // only used once, see updateTurnoutAspectPanel()
    JPanel disabledAspectsPanel = new JPanel();
    
    TurnoutSignalMast currentMast = null;

    /** {@inheritDoc} */
    @Override
    public void setAspectNames(@Nonnull SignalAppearanceMap map, @Nonnull SignalSystem sigSystem) {
        Enumeration<String> aspects = map.getAspects();
        log.debug("setAspectNames(...)");

        turnoutAspect.clear();
        while (aspects.hasMoreElements()) {
            String aspect = aspects.nextElement();
            TurnoutAspectPanel aPanel = new TurnoutAspectPanel(aspect);
            turnoutAspect.put(aspect, aPanel);
        }

        turnoutMastPanel.removeAll();
        for (Map.Entry<String, TurnoutAspectPanel> entry : turnoutAspect.entrySet()) {
            log.trace("   aspect: {}", entry.getKey());
            turnoutMastPanel.add(entry.getValue().getPanel());
        }

        turnoutMastPanel.add(resetPreviousState);
        resetPreviousState.setToolTipText(Bundle.getMessage("ResetPreviousToolTip"));

        turnoutMastPanel.setLayout(new jmri.util.javaworld.GridLayout2(0, 2)); // 0 means enough
        
        turnoutMastPanel.revalidate();
        turnoutMastScroll.revalidate();
    }

    /** {@inheritDoc} */
    @Override
    public boolean canHandleMast(@Nonnull SignalMast mast) {
        return mast instanceof TurnoutSignalMast;
    }

    /** {@inheritDoc} */
    @Override
    public void setMast(SignalMast mast) { 
        log.trace("setMast({}) start", mast);
        if (mast == null) { 
            currentMast = null; 
            return; 
        }
        
        if (! (mast instanceof TurnoutSignalMast) ) {
            log.error("mast was wrong type: {} {}", mast.getSystemName(), mast.getClass().getName());
            return;
        }

        currentMast = (TurnoutSignalMast) mast;
        SignalAppearanceMap appMap = mast.getAppearanceMap();

        if (appMap != null) {
            Enumeration<String> aspects = appMap.getAspects();
            while (aspects.hasMoreElements()) {
                String key = aspects.nextElement();
                Objects.requireNonNull(key, "only non-null keys are expected");
                TurnoutAspectPanel turnPanel = turnoutAspect.get(key);
                Objects.requireNonNull(turnPanel, "a panel should exist for each aspect");
                turnPanel.setSelectedTurnout(currentMast.getTurnoutName(key));
                turnPanel.setTurnoutState(currentMast.getTurnoutState(key));
                turnPanel.setAspectDisabled(currentMast.isAspectDisabled(key));
            }
        }
        if (currentMast.resetPreviousStates()) {
            resetPreviousState.setSelected(true);
        }
        if (currentMast.allowUnLit()) {
            turnoutUnLitBox.setDefaultNamedBean(currentMast.getUnLitTurnout());
            if (currentMast.getUnLitTurnoutState() == Turnout.CLOSED) {
                turnoutUnLitState.setSelectedItem(stateClosed);
            } else {
                turnoutUnLitState.setSelectedItem(stateThrown);
            }

        }

        allowUnLit.setSelected(currentMast.allowUnLit());

        log.trace("setMast({}) end", mast);
    }

    /** {@inheritDoc} */
    @Override
    public boolean createMast(@Nonnull String sigsysname, @Nonnull String mastname, @Nonnull String username) {
        log.debug("createMast({},{})", sigsysname, mastname);
        String name;
        if (currentMast == null) {
            name = "IF$tsm:"
                    + sigsysname
                    + ":" + mastname.substring(11, mastname.length() - 4);
            name += "($" + (paddedNumber.format(TurnoutSignalMast.getLastRef() + 1)) + ")";
            currentMast = new TurnoutSignalMast(name);

            InstanceManager.getDefault(jmri.SignalMastManager.class).register(currentMast);
        }
        name = currentMast.getSystemName();
        
        // load a new or existing mast
        for (Map.Entry<String, TurnoutAspectPanel> entry : turnoutAspect.entrySet()) {
            entry.getValue().setReference(name + ":" + entry.getKey());
            turnoutMastPanel.add(entry.getValue().getPanel());
            if (entry.getValue().isAspectDisabled()) {
                currentMast.setAspectDisabled(entry.getKey());
            } else {
                currentMast.setAspectEnabled(entry.getKey());
                currentMast.setTurnout(entry.getKey(), entry.getValue().getTurnoutName(), entry.getValue().getTurnoutState());
            }
        }
        currentMast.resetPreviousStates(resetPreviousState.isSelected());
        if (!username.equals("")) {
            currentMast.setUserName(username);
        }
        currentMast.setAllowUnLit(allowUnLit.isSelected());
        if (allowUnLit.isSelected()) {
            currentMast.setUnLitTurnout(turnoutUnLitBox.getDisplayName(), turnoutStateValues[turnoutUnLitState.getSelectedIndex()]);
        }
        return true;
    }

    String stateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
    String stateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
    String[] turnoutStates = new String[]{stateClosed, stateThrown};
    int[] turnoutStateValues = new int[]{Turnout.CLOSED, Turnout.THROWN};

    BeanSelectCreatePanel<Turnout> turnoutUnLitBox = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    JComboBox<String> turnoutUnLitState = new JComboBox<>(turnoutStates);
    DecimalFormat paddedNumber = new DecimalFormat("0000");

    @ServiceProvider(service = SignalMastAddPane.SignalMastAddPaneProvider.class)
    static public class SignalMastAddPaneProvider extends SignalMastAddPane.SignalMastAddPaneProvider {
        /** {@inheritDoc} */
        @Override
        @Nonnull public String getPaneName() {
            return Bundle.getMessage("TurnCtlMast");
        }
        /** {@inheritDoc} */
        @Override
        @Nonnull public SignalMastAddPane getNewPane() {
            return new TurnoutSignalMastAddPane();
        }
    }

    /**
     * JPanel to define properties of an Aspect for a Turnout Signal Mast.
     * <p>
     * Invoked from the AddSignalMastPanel class when a Turnout Signal Mast is
     * selected.
     */
    class TurnoutAspectPanel {

        BeanSelectCreatePanel<Turnout> beanBox = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
        JCheckBox disabledCheck = new JCheckBox(Bundle.getMessage("DisableAspect"));
        JLabel turnoutStateLabel = new JLabel(Bundle.getMessage("SetState"));
        JComboBox<String> turnoutState = new JComboBox<>(turnoutStates);

        String aspect = "";

        TurnoutAspectPanel(String aspect) {
            this.aspect = aspect;
        }

        TurnoutAspectPanel(String turnoutName, int state) {
            if (turnoutName == null || turnoutName.equals("")) {
                return;
            }
            beanBox.setDefaultNamedBean(InstanceManager.turnoutManagerInstance().getTurnout(turnoutName));
        }

        /**
         * Store the mast name as comment in the turnout.
         *
         * @param reference Text to use as comment
         */
        void setReference(String reference) {
            beanBox.setReference(reference);
        }

        int getTurnoutState() {
            return turnoutStateValues[turnoutState.getSelectedIndex()];
        }

        void setSelectedTurnout(String name) {
            if (name == null || name.equals("")) {
                return;
            }
            beanBox.setDefaultNamedBean(InstanceManager.turnoutManagerInstance().getTurnout(name));
        }

        void setTurnoutState(int state) {
            if (state == Turnout.CLOSED) {
                turnoutState.setSelectedItem(stateClosed);
            } else {
                turnoutState.setSelectedItem(stateThrown);
            }
        }

        void setAspectDisabled(boolean boo) {
            disabledCheck.setSelected(boo);
            if (boo) {
                beanBox.setEnabled(false);
                turnoutStateLabel.setEnabled(false);
                turnoutState.setEnabled(false);
            } else {
                beanBox.setEnabled(true);
                turnoutStateLabel.setEnabled(true);
                turnoutState.setEnabled(true);
            }
        }

        boolean isAspectDisabled() {
            return disabledCheck.isSelected();
        }

        String getTurnoutName() {
            return beanBox.getDisplayName();
        }

        NamedBean getTurnout() {
            try {
                return beanBox.getNamedBean();
            } catch (jmri.JmriException ex) {
                log.warn("skipping creation of turnout");
                return null;
            }
        }

        JPanel panel;

        JPanel getPanel() {
            if (panel == null) {
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                JPanel turnDetails = new JPanel();
                turnDetails.add(beanBox);
                turnDetails.add(turnoutStateLabel);
                turnDetails.add(turnoutState);
                panel.add(turnDetails);
                panel.add(disabledCheck);
                TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                border.setTitle(aspect);
                panel.setBorder(border);

                disabledCheck.addActionListener((ActionEvent e) -> {
                    setAspectDisabled(disabledCheck.isSelected());
                });

            }
            return panel;
        }

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TurnoutSignalMastAddPane.class);

}
