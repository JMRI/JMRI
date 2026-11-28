package jmri.jmrit.simulation.link.swing;

import javax.annotation.Nonnull;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import jmri.jmrit.simulation.link.LinkPreset;
import jmri.jmrit.simulation.link.SimulatedLink;

/**
 * The Link Timing menu: one choice per preset of a simulated link, the one in
 * use ticked. A system's simulator window adds it with one line.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public class SimulatedLinkMenu extends JMenu {

    /**
     * @param link the link it sets
     */
    public SimulatedLinkMenu(@Nonnull SimulatedLink link) {
        super(Bundle.getMessage("MenuLinkTiming"));
        setToolTipText(Bundle.getMessage("MenuLinkTimingTip"));
        ButtonGroup group = new ButtonGroup();
        for (LinkPreset preset : link.getPresets()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(preset.getName());
            item.setSelected(preset == link.getPreset());
            item.addActionListener(e -> link.setPreset(preset));
            group.add(item);
            add(item);
        }
    }
}
