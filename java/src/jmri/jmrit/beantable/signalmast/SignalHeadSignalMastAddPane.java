package jmri.jmrit.beantable.signalmast;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import jmri.*;
import jmri.implementation.*;
import jmri.util.*;
import jmri.util.swing.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring SignalHeadSignalMast objects
 * <P>
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
public class SignalHeadSignalMastAddPane extends SignalMastAddPane {

    public SignalHeadSignalMastAddPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // lit/unlit controls
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("AllowUnLitLabel") + ": "));
        p.add(allowUnLit);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(p);

        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border.setTitle(Bundle.getMessage("MenuItemSignalTable")); // Signal Heads
        signalHeadPanel.setBorder(border);
        signalHeadPanel.setVisible(false);
        add(signalHeadPanel);
        
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("HeadCtlMast");
    }
    
    
    JPanel signalHeadPanel = new JPanel();
    ArrayList<JmriBeanComboBox> headList = new ArrayList<>(5);

    JCheckBox allowUnLit = new JCheckBox();

    SignalHeadSignalMast currentMast = null;

    /** {@inheritDoc} */
    @Override
    public void setAspectNames(@Nonnull SignalAppearanceMap map) {
        Enumeration<String> aspects = map.getAspects();
        log.debug("setAspectNames(...)");
        
        System.err.println(" map "+map);
        System.err.println(" system "+map.getSignalSystem());   // <-- this is null!
        Enumeration<String> keys = map.getSignalSystem().getKeys();
        while (keys.hasMoreElements()) {
            System.err.println("  key: "+keys.nextElement());
        }

        // int count = mapNameToShowSize.get(mastBox.getSelectedItem()).intValue(); //mapNameToShowSuze is from jmri/jmrit/beantable/signalmast/AddSignalMastPanel
        // headList = new ArrayList<>(count);

        //signalHeadPanel.removeAll();
        //signalHeadPanel.setLayout(new jmri.util.javaworld.GridLayout2(count + 1, 1));
        //for (int i = 0; i < count; i++) {
            //JmriBeanComboBox head = new JmriBeanComboBox(InstanceManager.getDefault(jmri.SignalHeadManager.class));
            //head.excludeItems(alreadyUsed);
            //headList.add(head);
            //signalHeadPanel.add(head);
        //}
        //signalHeadPanel.add(includeUsed);
          
    }


    /** {@inheritDoc} */
    @Override
    public boolean canHandleMast(@Nonnull SignalMast mast) {
        return mast instanceof SignalHeadSignalMast;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setMast(SignalMast mast) { 
        log.debug("setMast({})", mast);
        if (mast == null) { 
            currentMast = null; 
            return; 
        }
        
        if (! (mast instanceof TurnoutSignalMast) ) {
            log.error("mast was wrong type: {} {}", mast.getSystemName(), mast.getClass().getName());
            return;
        }

        currentMast = (SignalHeadSignalMast) mast;
        SignalAppearanceMap appMap = mast.getAppearanceMap();




        // don't have anything here yet



        //if (currentMast.allowUnLit()) {
            //turnoutUnLitBox.setDefaultNamedBean(currentMast.getUnLitTurnout());
            //if (currentMast.getUnLitTurnoutState() == Turnout.CLOSED) {
                //turnoutUnLitState.setSelectedItem(stateClosed);
            //} else {
                //turnoutUnLitState.setSelectedItem(stateThrown);
            //}

        //}

    }

    /** {@inheritDoc} */
    @Override
    public boolean createMast(@Nonnull String sigsysname, @Nonnull String mastname, @Nonnull String username) {
        log.debug("createMast({},{})", sigsysname, mastname);
        String name;
        if (currentMast == null) {
                StringBuilder build = new StringBuilder();
                build.append("IF$shsm:").append(sigsysname).append(":").append(mastname.substring(11, mastname.length() - 4));
                for (JmriBeanComboBox head : headList) {
                    if (head != null && head.getSelectedDisplayName() != null) {
                        build.append("(").append(StringUtil.parenQuote(head.getSelectedDisplayName())).append(")");
                    }
                }
                name = build.toString();
                log.debug("add signal: {}", name);
                
                // see if it exists (remember, we're not handling a current mast)
                SignalMast m = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(name);
                if (m != null) {
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(Bundle.getMessage("DuplicateMast"),
                            new Object[]{m.getDisplayName()}), Bundle.getMessage("DuplicateMastTitle"), JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
                try {
                    // now create it
                    currentMast = (SignalHeadSignalMast)InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(name);
                } catch (IllegalArgumentException ex) {
                    // user input no good
 // --->                   //handleCreateException(name);
                    return false; // without creating
                }
        }
        name = currentMast.getSystemName();
        
        // need update here


        if (!username.equals("")) {
            currentMast.setUserName(username);
        }
        //currentMast.setAllowUnLit(allowUnLit.isSelected());
        //if (allowUnLit.isSelected()) {
            //currentMast.setUnLitTurnout(turnoutUnLitBox.getDisplayName(), turnoutStateValues[turnoutUnLitState.getSelectedIndex()]);
        //}
        return true;
    }

    @ServiceProvider(service = SignalMastAddPane.SignalMastAddPaneProvider.class)
    static public class SignalMastAddPaneProvider extends SignalMastAddPane.SignalMastAddPaneProvider {
        /** {@inheritDoc} */
        @Override
        @Nonnull public String getPaneName() {
            return Bundle.getMessage("HeadCtlMast");
        }
        /** {@inheritDoc} */
        @Override
        @Nonnull public SignalMastAddPane getNewPane() {
            return new SignalHeadSignalMastAddPane();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalHeadSignalMastAddPane.class);
}
