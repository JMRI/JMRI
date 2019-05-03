package apps.DecoderPro;

import apps.Apps;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The JMRI main pane for configuring DCC decoders.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright 2003, 2014
 */
public class DecoderProPane extends apps.AppsLaunchPane {

    DecoderProPane() {
        super();
    }

    @Override
    protected String windowHelpID() {
        return "package.apps.DecoderPro.DecoderPro";
    }

    @Override
    protected String logo() {
        return "resources/decoderpro.gif";
    }

    @Override
    protected String line1() {
        return MessageFormat.format(Bundle.getMessage("DecoderProVersionCredit"),
                new Object[]{jmri.Version.name()});
    }

    @Override
    protected String line2() {
        return "http://jmri.org/DecoderPro ";
    }

    @Override
    protected JPanel statusPanel() {
        JPanel j = new JPanel();
        j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));
        j.add(super.statusPanel());

        // Buttons
        Action serviceprog = new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction(Bundle.getMessage("DpButtonUseProgrammingTrack"));
        Action opsprog = new jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction(Bundle.getMessage("DpButtonProgramOnMainTrack"));
        Action quit = new AbstractAction(Bundle.getMessage("MenuItemQuit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Apps.handleQuit();
            }
        };

        JButton b1 = new JButton(Bundle.getMessage("DpButtonUseProgrammingTrack"));
        b1.addActionListener(serviceprog);
        b1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        j.add(b1);
        if (jmri.InstanceManager.getNullableDefault(jmri.GlobalProgrammerManager.class) == null
                || !jmri.InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).isGlobalProgrammerAvailable()) {
            b1.setEnabled(false);
            b1.setToolTipText(Bundle.getMessage("MsgServiceButtonDisabled"));
        }
        JButton m1 = new JButton(Bundle.getMessage("DpButtonProgramOnMainTrack"));
        m1.addActionListener(opsprog);
        m1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        j.add(m1);
        if (jmri.InstanceManager.getNullableDefault(jmri.AddressedProgrammerManager.class) == null
                || !jmri.InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).isAddressedModePossible()) {
            m1.setEnabled(false);
            m1.setToolTipText(Bundle.getMessage("MsgOpsButtonDisabled"));
        }

        JPanel p3 = new JPanel();
        p3.setLayout(new java.awt.FlowLayout());
        JButton h1 = new JButton(Bundle.getMessage("ButtonHelp"));
        jmri.util.HelpUtil.addHelpToComponent(h1, "html.apps.DecoderPro.index");
        h1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        p3.add(h1);
        JButton q1 = new JButton(Bundle.getMessage("ButtonQuit"));
        q1.addActionListener(quit);
        q1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        p3.add(q1);
        j.add(p3);

        return j;
    }

}
