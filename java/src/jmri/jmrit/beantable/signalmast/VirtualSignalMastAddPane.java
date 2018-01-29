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
 * A pane for configuring VirtualSignalMast objects
 * <P>
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
@ServiceProvider(service = SignalMastAddPane.class)
public class VirtualSignalMastAddPane extends SignalMastAddPane {

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("VirtualMast");
    }

    JCheckBox allowUnLit = new JCheckBox();

    LinkedHashMap<String, JCheckBox> disabledAspects = new LinkedHashMap<>(10);
    JPanel disabledAspectsPanel = new JPanel();

    /** {@inheritDoc} */
    @Override
    public void setAspectNames(@Nonnull Enumeration<String> aspects) {
        // update immediately
        disabledAspects = new LinkedHashMap<>(10);
        disabledAspectsPanel.removeAll();
        while (aspects.hasMoreElements()) {
            String aspect = aspects.nextElement();
            JCheckBox disabled = new JCheckBox(aspect);
            disabledAspects.put(aspect, disabled);
        }
        disabledAspectsPanel.setLayout(new jmri.util.javaworld.GridLayout2(disabledAspects.size() + 1, 1));
        for (String aspect : disabledAspects.keySet()) {
            disabledAspectsPanel.add(disabledAspects.get(aspect));
        }

        disabledAspectsPanel.revalidate();
    }

    public VirtualSignalMastAddPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // lit/unlit controls
        JPanel p = p = new JPanel();
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
    public boolean setMast(@Nonnull SignalMast mast) { 
        if (! (mast instanceof jmri.implementation.VirtualSignalMast) ) return false;

        List<String> disabled = ((VirtualSignalMast) mast).getDisabledAspects();
        if (disabled != null) {
            for (String aspect : disabled) {
                if (disabledAspects.containsKey(aspect)) {
                    disabledAspects.get(aspect).setSelected(true);
                }
            }
        }
         
        return true;
    }

    DecimalFormat paddedNumber = new DecimalFormat("0000");

    /** {@inheritDoc} */
    @Override
    public void createMast(@Nonnull String sigsysname, @Nonnull String mastname, @Nonnull String username) {
        // create a mast
        String name = "IF$vsm:"
                + sigsysname
                + ":" + mastname.substring(11, mastname.length() - 4);
        name += "($" + (paddedNumber.format(VirtualSignalMast.getLastRef() + 1)) + ")";
        VirtualSignalMast virtMast = new VirtualSignalMast(name);
        if (!username.equals("")) {
            virtMast.setUserName(username);
        }
        InstanceManager.getDefault(jmri.SignalMastManager.class).register(virtMast);

        // load a new or existing mast
        for (String aspect : disabledAspects.keySet()) {
            if (disabledAspects.get(aspect).isSelected()) {
                virtMast.setAspectDisabled(aspect);
            } else {
                virtMast.setAspectEnabled(aspect);
            }
        }
        virtMast.setAllowUnLit(allowUnLit.isSelected());
    }
}
