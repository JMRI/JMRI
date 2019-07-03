package jmri.jmrit.beantable.signalmast;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.implementation.*;
import jmri.util.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring VirtualSignalMast objects.
 *
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
public class VirtualSignalMastAddPane extends SignalMastAddPane {

    public VirtualSignalMastAddPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // lit/unlit controls
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("AllowUnLitLabel") + ": "));
        p.add(allowUnLit);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(p);
        
        // disabled aspects controls
        TitledBorder disableborder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        disableborder.setTitle(Bundle.getMessage("DisableAspectsLabel"));
        JScrollPane disabledAspectsScroll = new JScrollPane(disabledAspectsPanel);
        disabledAspectsScroll.setBorder(disableborder);
        add(disabledAspectsScroll);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("VirtualMast");
    }

    JCheckBox allowUnLit = new JCheckBox();

    LinkedHashMap<String, JCheckBox> disabledAspects = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);
    JPanel disabledAspectsPanel = new JPanel();
    
    VirtualSignalMast currentMast = null;

    /** {@inheritDoc} */
    @Override
    public void setAspectNames(@Nonnull SignalAppearanceMap map, @Nonnull SignalSystem sigSystem) {
        Enumeration<String> aspects = map.getAspects();
        // update immediately
        disabledAspects = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);
        disabledAspectsPanel.removeAll();
        while (aspects.hasMoreElements()) {
            String aspect = aspects.nextElement();
            JCheckBox disabled = new JCheckBox(aspect);
            disabledAspects.put(aspect, disabled);
        }

        for (Map.Entry<String, JCheckBox> entry : disabledAspects.entrySet()) {
            disabledAspectsPanel.add(entry.getValue());
        }

        disabledAspectsPanel.setLayout(new jmri.util.javaworld.GridLayout2(0, 1)); // 0 means enough

        disabledAspectsPanel.revalidate();
    }

    /** {@inheritDoc} */
    @Override
    public boolean canHandleMast(@Nonnull SignalMast mast) {
        return mast instanceof jmri.implementation.VirtualSignalMast;
    }

    /** {@inheritDoc} */
    @Override
    public void setMast(SignalMast mast) { 
        log.trace("setMast {} start", mast);
        if (mast == null) { 
            currentMast = null; 
            return; 
        }
        
        if (! (mast instanceof jmri.implementation.VirtualSignalMast) ) {
            log.error("mast was wrong type: {} {}", mast.getSystemName(), mast.getClass().getName());
            return;
        }

        currentMast = (VirtualSignalMast) mast;
        List<String> disabled = currentMast.getDisabledAspects();
        if (disabled != null) {
            for (String aspect : disabled) {
                if (disabledAspects.containsKey(aspect)) {
                    disabledAspects.get(aspect).setSelected(true);
                }
            }
        }

        allowUnLit.setSelected(currentMast.allowUnLit());

        log.trace("setMast {} end", mast);
    }

    DecimalFormat paddedNumber = new DecimalFormat("0000");

    /** {@inheritDoc} */
    @Override
    public boolean createMast(@Nonnull
            String sigsysname, @Nonnull
                    String mastname, @Nonnull
                            String username) {
        log.trace("createMast {} {} {} start with currentMast: {}", sigsysname, mastname, username, currentMast);
        if (currentMast == null) {
            // create a mast
            String name = "IF$vsm:"
                    + sigsysname
                    + ":" + mastname.substring(11, mastname.length() - 4);
            name += "($" + (paddedNumber.format(VirtualSignalMast.getLastRef() + 1)) + ")";
            currentMast = new VirtualSignalMast(name);
            if (!username.equals("")) {
                currentMast.setUserName(username);
            }
            currentMast.setMastType(mastname.substring(11, mastname.length() - 4));
            InstanceManager.getDefault(jmri.SignalMastManager.class).register(currentMast);
        }

        // load a new or existing mast
        for (Map.Entry<String, JCheckBox> entry : disabledAspects.entrySet()) {
            if (entry.getValue().isSelected()) {
                currentMast.setAspectDisabled(entry.getKey());
            } else {
                currentMast.setAspectEnabled(entry.getKey());
            }
        }
        currentMast.setAllowUnLit(allowUnLit.isSelected());
        log.trace("createMast {} {} {} end", sigsysname, mastname, username);
        return true;
    }

    @ServiceProvider(service = SignalMastAddPane.SignalMastAddPaneProvider.class)
    static public class SignalMastAddPaneProvider extends SignalMastAddPane.SignalMastAddPaneProvider {
        /** {@inheritDoc} */
        @Override
        @Nonnull public String getPaneName() {
            return Bundle.getMessage("VirtualMast");
        }
        /** {@inheritDoc} */
        @Override
        @Nonnull public SignalMastAddPane getNewPane() {
            return new VirtualSignalMastAddPane();
        }
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualSignalMastAddPane.class);

}
