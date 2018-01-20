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

    public VirtualSignalMastAddPane() {
//         String mastType = mastNames.get(mastBox.getSelectedIndex()).getName();
//         mastType = mastType.substring(11, mastType.indexOf(".xml"));
//         DefaultSignalAppearanceMap sigMap = DefaultSignalAppearanceMap.getMap(sigsysname, mastType);
//         Enumeration<String> aspects = sigMap.getAspects();
//         disabledAspects = new LinkedHashMap<>(10);

        JPanel disabledAspectsPanel = new JPanel();
//         while (aspects.hasMoreElements()) {
//             String aspect = aspects.nextElement();
//             JCheckBox disabled = new JCheckBox(aspect);
//             disabledAspects.put(aspect, disabled);
//         }
        disabledAspectsPanel.setLayout(new jmri.util.javaworld.GridLayout2(disabledAspects.size() + 1, 1));
        for (String aspect : disabledAspects.keySet()) {
            disabledAspectsPanel.add(disabledAspects.get(aspect));
        }

        TitledBorder disableborder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        disableborder.setTitle(Bundle.getMessage("DisableAspectsLabel"));
        JScrollPane disabledAspectsScroll = new JScrollPane(disabledAspectsPanel);
        disabledAspectsScroll.setBorder(disableborder);
        add(disabledAspectsScroll);

    }
}
