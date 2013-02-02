// AppConfigPanel.java

package apps;

import org.apache.log4j.Logger;
import jmri.jmrix.JmrixConfigPane;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * Basic configuration GUI infrastructure.
 * Replaced by the new Tabbed Preferences {@link apps.gui3.TabbedPreferences}
 *
 * @author	Bob Jacobsen   Copyright (C) 2003, 2008, 2010
 * @author      Matthew Harris copyright (c) 2009
 * @version	$Revision$
 * @deprecated  2.10.3
 */
@Deprecated
public class AppConfigPanel extends AppConfigBase {

    /**
     * Construct a configuration panel for inclusion in a preferences
     * or configuration dialog.
     * @param nConnections number of connections configured, e.g. the number of connection
     *      sub-panels included
     */
    @Deprecated
    public AppConfigPanel(int nConnections) {
        super();  // by default number of connection slots
        log.debug("start app");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p;
        // Communications
        log.debug("start comm");
        p = JmrixConfigPane.instance(0);
        p.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutConnection")));
        addAndRemember(p);

        // Swing GUI LAF
        log.debug("start laf");
        final GuiLafConfigPane guiPane;
        super.add(guiPane = new GuiLafConfigPane());
        // place at beginning of preferences list to avoid UI anomalies
        items.add(0, guiPane);
        guiPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutGUI")));

        // default programmer configuration
        log.debug("start prog");
        jmri.jmrit.symbolicprog.ProgrammerConfigPane progPane;
        addAndRemember(progPane = new jmri.jmrit.symbolicprog.ProgrammerConfigPane());
        progPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutProgrammer")));

        // add button to show advanced section
        log.debug("start adv but");
        add(new JSeparator(JSeparator.HORIZONTAL));
        showAdvanced = new JCheckBox(rb.getString("ButtonShowAdv"));
        showAdvanced.setAlignmentX(1.f);
        JPanel p5 = new JPanel();
        p5.setLayout(new FlowLayout());
        p5.setAlignmentX(1.f);
        p5.add(showAdvanced);
        add(p5);

        // add advanced section itself
        log.debug("start adv");
        advScroll = new JPanel();
        advScroll.setLayout(new BoxLayout(advScroll, BoxLayout.Y_AXIS));
        advancedPane = new JPanel();
        JScrollPane js = new JScrollPane(advancedPane);
        advancedPane.setLayout(new BoxLayout(advancedPane, BoxLayout.Y_AXIS));
        advScroll.setVisible(false);  // have to click first
        advScroll.add(js);
        add(advScroll);
        showAdvanced.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (showAdvanced.isSelected()) {
                    if (!localeAdded) {
                        localeSpace.add(guiPane.doLocale());
                        localeAdded = true;
                    }
                    advScroll.setVisible(true);
                    advScroll.validate();
                    if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
                    advScroll.repaint();
                }
                else {
                    advScroll.setVisible(false);
                    advScroll.validate();
                    if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
                    advScroll.repaint();
                }
            }
        });

        // fill advanced section
        log.debug("start comm 2");
        if (nConnections > 1) {
            p = JmrixConfigPane.instance(1);
            p.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutAuxConnection2")));
            advancedPane.add(p);
            items.add(p);
            p = JmrixConfigPane.instance(2);
            p.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutAuxConnection3")));
            advancedPane.add(p);
            items.add(p);
            p = JmrixConfigPane.instance(3);
            p.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutAuxConnection4")));
            advancedPane.add(p);
            items.add(p);
        }

        // add advanced programmer options
        JPanel advProgSpace = new JPanel();
        advProgSpace.add(progPane.getAdvancedPanel());
        advProgSpace.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutProgrammer")));
        advancedPane.add(advProgSpace);

        // reserve space for Locale later
        log.debug("start res locale");
        localeSpace  = new JPanel();
        localeSpace.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutLocale")));
        localeAdded = false;
        advancedPane.add(localeSpace);

        log.debug("start act");
        PerformActionPanel action = new PerformActionPanel();
        action.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutStartupActions")));
        advancedPane.add(action);
        items.add(action);

        log.debug("start button");
        if (Apps.buttonSpace()!=null) {
            CreateButtonPanel buttons = new CreateButtonPanel();
            buttons.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutCreateButton")));
            advancedPane.add(buttons);
            items.add(buttons);
        }

        log.debug("start file");
        PerformFilePanel files = new PerformFilePanel();
        files.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutStartupFiles")));
        advancedPane.add(files);
        items.add(files);

        log.debug("start scripts");
        PerformScriptPanel scripts = new PerformScriptPanel();
        scripts.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutStartupScripts")));
        advancedPane.add(scripts);
        items.add(scripts);

        // default roster location configuration
        log.debug("start roster");
        JPanel roster = new jmri.jmrit.roster.RosterConfigPane();
        roster.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutRoster")));
        advancedPane.add(roster);
        items.add(roster);

        // put the "Save" button at the bottom
        JButton save = new JButton(rb.getString("ButtonSave"));
        add(save);  // don't want to persist the button!
        save.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    savePressed();
                }
            });

    }

    JCheckBox showAdvanced;
    JPanel advScroll;
    JPanel advancedPane;

    JPanel localeSpace = null;
    boolean localeAdded = false;

    @Deprecated
    public Component addAndRemember(Component c) {
        items.add(c);
        super.add(c);
        return c;
    }


    // initialize logging
    static Logger log = Logger.getLogger(AppConfigPanel.class.getName());

}
