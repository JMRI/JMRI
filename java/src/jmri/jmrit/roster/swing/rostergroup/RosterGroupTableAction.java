package jmri.jmrit.roster.swing.rostergroup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.swing.RosterGroupComboBox;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to create and register a Roster Group Table.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Kevin Dickerson Copyright (C) 2009
 */
public class RosterGroupTableAction extends jmri.util.swing.JmriAbstractAction {

    public RosterGroupTableAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public RosterGroupTableAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     * @param s action title though may be changed?
     *
     */
    public RosterGroupTableAction(String s) {
        super(s);

    }

    public RosterGroupTableAction() {
        this(Bundle.getMessage("RosterGroupTable"));
    }

    RosterGroupTableModel m;
    RosterGroupTableFrame f;

    void createModel() {

        m = new RosterGroupTableModel();
    }

    public void actionPerformed() {
        // create the JTable model, with changes for specific NamedBean
        createModel();

        // create the frame
        f = new RosterGroupTableFrame(m, helpTarget()) {
            /**
             * Include an "add" button
             */
            @Override
            void extras() {
                final JComboBox<String> selectCombo = new RosterGroupComboBox();
                selectCombo.insertItemAt("", 0);
                selectCombo.setSelectedIndex(-1);
                JPanel p25 = new JPanel();
                p25.add(new JLabel(Bundle.getMessage("SelectRosterGroup")));
                p25.add(selectCombo);
                addToTopBox(p25);
                selectCombo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            comboSelected(e, selectCombo.getSelectedItem().toString());
                        } catch (Exception ex) {
                            log.debug("Null pointer exception");
                        }
                    }
                });
                selectCombo.setVisible(true);

            }
        };
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actionPerformed();
    }

    public void addToFrame(RosterGroupTableFrame f) {
    }

    void setTitle() {
        f.setTitle(Bundle.getMessage("RosterGroupTable"));
    }

    String helpTarget() {
        return "package.jmri.jmrit.roster.swing.RosterGroupTable"; // NOI18N
    }

    void comboSelected(ActionEvent e, String group) {
        m.setGroup(Roster.ROSTER_GROUP_PREFIX + group);
        m.fireTableDataChanged();

    }

    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    private final static Logger log = LoggerFactory.getLogger(RosterGroupTableAction.class);
}
