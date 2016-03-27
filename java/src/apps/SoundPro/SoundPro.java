// SoundPro.java
package apps.SoundPro;

import apps.Apps;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JMRI application for controlling audio.
 * <P>
 * If an argument is provided at startup, it will be used as the name of the
 * configuration file. Note that this is just the name, not the path; the file
 * is searched for in the usual way, first in the preferences tree and then in
 * xml/
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
 *
 * @author	Bob Jacobsen Copyright 2003, 2004, 2007
 * @author Matthew Harris copyright (c) 2009
 * @version $Revision$
 */
public class SoundPro extends Apps {

    SoundPro(JFrame p) {
        super(p);
    }

    @Override
    protected String logo() {
        return "resources/SoundPro.gif";
    }

    @Override
    protected String mainWindowHelpID() {
        return "package.apps.SoundPro.SoundPro";
    }

    @Override
    protected String line1() {
        return MessageFormat.format(Bundle.getMessage("SoundProVersionCredit"),
                new Object[]{jmri.Version.name()});
    }

    @Override
    protected String line2() {
        return "http://jmri.org/SoundPro ";
    }

    @Override
    protected JPanel statusPanel() {
        JPanel j = new JPanel();
        j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));
        j.add(super.statusPanel());

        // Buttons
        Action audioTable = new jmri.jmrit.beantable.AudioTableAction(Bundle.getMessage("SpButtonAudioTable"));
        Action quit = new AbstractAction(Bundle.getMessage("MenuItemQuit")) {
            public void actionPerformed(ActionEvent e) {
                Apps.handleQuit();
            }
        };

        JButton b1 = new JButton(Bundle.getMessage("SpButtonAudioTable"));
        b1.addActionListener(audioTable);
        b1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        j.add(b1);

        JPanel p3 = new JPanel();
        p3.setLayout(new java.awt.FlowLayout());
        JButton h1 = new JButton(Bundle.getMessage("ButtonHelp"));
        jmri.util.HelpUtil.addHelpToComponent(h1, "html.apps.SoundPro.SoundPro");
        h1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        p3.add(h1);
        JButton q1 = new JButton(Bundle.getMessage("ButtonQuit"));
        q1.addActionListener(quit);
        q1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        p3.add(q1);
        j.add(p3);

        return j;
    }

    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        Apps.setStartupInfo("SoundPro");

        setConfigFilename("SoundProConfig2.xml", args);
        JmriJFrame f = new JmriJFrame("SoundPro");
        createFrame(new SoundPro(f), f);

        log.debug("main initialization done");
        splash(false);
    }

    private final static Logger log = LoggerFactory.getLogger(SoundPro.class.getName());
}
