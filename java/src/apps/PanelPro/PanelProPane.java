// PanelProPane.java
package apps.PanelPro;

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
 * The JMRI main pane for creating control panels.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author	Bob Jacobsen Copyright 2003, 2014
 * @version $Revision$
 */
public class PanelProPane extends apps.AppsLaunchPane {

    /**
     *
     */
    private static final long serialVersionUID = -5742354704602024439L;

    PanelProPane() {
        super();
    }

    /**
     * Returns the ID for the window's help, which is application specific
     */
    protected String windowHelpID() {
        return "package.apps.PanelPro.PanelPro";
    }

    protected String logo() {
        return "resources/PanelPro.gif";
    }

    protected String line1() {
        return MessageFormat.format(Bundle.getMessage("PanelProVersionCredit"),
                new Object[]{jmri.Version.name()});
    }

    protected String line2() {
        return "http://jmri.org/PanelPro ";
    }

    protected JPanel statusPanel() {
        JPanel j = new JPanel();
        j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));
        j.add(super.statusPanel());

        // Buttons
        Action quit = new AbstractAction(Bundle.getMessage("MenuItemQuit")) {
            /**
             *
             */
            private static final long serialVersionUID = -9134833676932931297L;

            public void actionPerformed(ActionEvent e) {
                Apps.handleQuit();
            }
        };

        JPanel p3 = new JPanel();
        p3.setLayout(new java.awt.FlowLayout());
        JButton h1 = new JButton(Bundle.getMessage("ButtonHelp"));
        jmri.util.HelpUtil.addHelpToComponent(h1, "html.apps.PanelPro.PanelPro");
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
