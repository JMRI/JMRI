package apps.DispatcherPro;

import apps.Apps;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JMRI program for creating control panels.
 * <p>
 * If an argument is provided at startup, it will be used as the name of the
 * configuration file. Note that this is just the name, not the path; the file
 * is searched for in the usual way, first in the preferences tree and then in
 * xml/
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
 * @author Bob Jacobsen Copyright 2003
 */
public class DispatcherPro extends Apps {

    DispatcherPro() {
        super();
    }

    @Override
    protected String logo() {
        return "resources/logo.gif";
    }

    @Override
    protected String mainWindowHelpID() {
        return "package.apps.DispatcherPro.DispatcherPro";
    }

    @Override
    protected String line1() {
        return MessageFormat.format(Bundle.getMessage("DispatcherProVersionCredit"),
                new Object[]{jmri.Version.name()});
    }

    @Override
    protected String line2() {
        return "http://jmri.org/DispatcherPro ";
    }

    /**
     * JPanel displayed as DispatcherPro main screen.
     */
    @Override
    protected JPanel statusPanel() {
        JPanel j = new JPanel();
        j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));
        j.add(super.statusPanel());

        // Buttons
        Action quit = new AbstractAction(Bundle.getMessage("MenuItemQuit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Apps.handleQuit();
            }
        };

        JPanel p3 = new JPanel();
        p3.setLayout(new java.awt.FlowLayout());
        h1 = new JButton(Bundle.getMessage("ButtonHelp"));
        // as globalHelpBroker is still null, wait to attach help target after help menu is created
        h1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        p3.add(h1);
        JButton q1 = new JButton(Bundle.getMessage("ButtonQuit"));
        q1.addActionListener(quit);
        q1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        p3.add(q1);
        j.add(p3);

        return j;
    }

    /**
     * Help button on Main Screen.
     */
    private JButton h1;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void attachHelp() {
        if (h1 != null) {
            jmri.util.HelpUtil.addHelpToComponent(h1, "html.apps.DispatcherPro.index");
        }
    }
    
    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        Apps.setStartupInfo("DispatcherPro");

        setConfigFilename("DispatcherProConfig2.xml", args);
        DispatcherPro dp = new DispatcherPro();
        JmriJFrame f = new JmriJFrame(jmri.Application.getApplicationName());
        createFrame(dp, f);

        log.debug("main initialization done");
        splash(false);
    }

    private final static Logger log = LoggerFactory.getLogger(DispatcherPro.class);

}
