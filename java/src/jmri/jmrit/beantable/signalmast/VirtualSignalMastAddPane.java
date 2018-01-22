package jmri.jmrit.beantable.signalmast;

import java.awt.Color;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import javax.annotation.Nonnull;
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
        TitledBorder disableborder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        disableborder.setTitle(Bundle.getMessage("DisableAspectsLabel"));
        JScrollPane disabledAspectsScroll = new JScrollPane(disabledAspectsPanel);
        disabledAspectsScroll.setBorder(disableborder);
        add(disabledAspectsScroll);

    }
}
