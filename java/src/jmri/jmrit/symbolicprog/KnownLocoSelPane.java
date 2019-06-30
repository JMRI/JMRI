package jmri.jmrit.symbolicprog;

import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import jmri.GlobalProgrammerManager;
import jmri.Programmer;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.progsupport.ProgModeSelector;
import jmri.jmrit.roster.IdentifyLoco;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide GUI controls to select a known loco via the Roster.
 * <p>
 * When the "open programmer" button is pushed, i.e. the user is ready to
 * continue, the startProgrammer method is invoked. This should be overridden
 * (e.g. in a local anonymous class) to create the programmer frame you're
 * interested in.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
public class KnownLocoSelPane extends LocoSelPane {

    public KnownLocoSelPane(JLabel s, boolean ident, ProgModeSelector selector) {
        mCanIdent = ident;
        mStatusLabel = s;
        this.selector = selector;
        init();
    }

    public KnownLocoSelPane(boolean ident) {
        this(null, ident, null);
    }

    boolean mCanIdent;

    JComboBox<String> programmerBox;
    ProgModeSelector selector;

    protected void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel pane2a = new JPanel();
        pane2a.setLayout(new BoxLayout(pane2a, BoxLayout.X_AXIS));
        pane2a.add(new JLabel(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("UseExisting")));

        if (mCanIdent) {
            JButton idloco = new JButton(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("ReadAndSelect"));
            idloco.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Identify locomotive pressed");
                    }
                    startIdentify();
                }
            });
            pane2a.add(idloco);
            pane2a.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        }
        add(pane2a);

        locoBox = new RosterEntrySelectorPanel();
        locoBox.setNonSelectedItem("Locomotive");
        add(locoBox);

        addProgrammerBox();

        JButton go2 = new JButton(Bundle.getMessage("OpenProgrammer"));
        go2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("Open programmer pressed");
                }
                openButton();
            }
        });
        add(go2);
        setBorder(new EmptyBorder(6, 6, 6, 6));
    }

    /**
     * Add the GUI for selecting a specific programmer
     */
    private void addProgrammerBox() {
        JPanel pane3a = new JPanel();
        pane3a.setLayout(new BoxLayout(pane3a, BoxLayout.X_AXIS));
        pane3a.add(new JLabel(Bundle.getMessage("ProgrammerFormat")));

        // create the programmer box
        programmerBox = new JComboBox<String>(ProgDefault.findListOfProgFiles());
        programmerBox.setSelectedIndex(0);
        if (ProgDefault.getDefaultProgFile() != null) {
            programmerBox.setSelectedItem(ProgDefault.getDefaultProgFile());
        }
        pane3a.add(programmerBox);
        // pane3a.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        add(pane3a);
    }

    JLabel mStatusLabel = null;

    private void startIdentify() {
        // start identifying a loco
        final KnownLocoSelPane me = this;
        Programmer p = null;
        if (selector != null && selector.isSelected()) p = selector.getProgrammer();
        if (p == null) {
            log.warn("Selector did not provide a programmer, use default");
            p = jmri.InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        }
        IdentifyLoco id = new IdentifyLoco(p) {
            private KnownLocoSelPane who = me;

            @Override
            protected void done(int dccAddress) {
                // if Done, updated the selected decoder
                who.selectLoco(dccAddress);
            }

            @Override
            protected void message(String m) {
                if (mStatusLabel != null) {
                    mStatusLabel.setText(m);
                }
            }

            @Override
            public void error() {
            }
        };
        id.start();
    }

    protected void selectLoco(int dccAddress) {
        // locate that loco
        List<RosterEntry> l = Roster.getDefault().matchingList(null, null, Integer.toString(dccAddress),
                null, null, null, null);
        if (log.isDebugEnabled()) {
            log.debug("selectLoco found " + l.size() + " matches");
        }
        if (l.size() > 0) {
            RosterEntry r = l.get(0);
            String id = r.getId();
            if (log.isDebugEnabled()) {
                log.debug("Loco id is " + id);
            }
            String group = locoBox.getSelectedRosterGroup();
            if (group != null && !group.equals(Roster.ALLENTRIES)) {
                List<RosterEntry> entries = Roster.getDefault().getEntriesWithAttributeKeyValue(Roster.getRosterGroupProperty(group), "yes");
                if (entries.contains(r)) {
                    locoBox.setSelectedRosterEntry(r);
                } else {
                    locoBox.setSelectedRosterEntryAndGroup(r, Roster.ALLENTRIES);
                }
            } else {
                locoBox.setSelectedRosterEntry(r);
            }
        } else {
            log.warn("Read address " + dccAddress + ", but no such loco in roster");
        }
    }

    private RosterEntrySelectorPanel locoBox = null;

    /**
     * handle pushing the open programmer button by finding names, then calling
     * a template method
     */
    protected void openButton() {

        if (locoBox.getSelectedRosterEntries().length != 0) {
            RosterEntry re = locoBox.getSelectedRosterEntries()[0];
            startProgrammer(null, re, (String) programmerBox.getSelectedItem());
        } else {
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("LocoMustSelected"),
                    Bundle.getMessage("NoSelection"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * meant to be overridden to start the desired type of programmer
     */
    protected void startProgrammer(DecoderFile decoderFile, RosterEntry r,
            String programmerName) {
        log.error("startProgrammer method in NewLocoSelPane should have been overridden");
    }

    private final static Logger log = LoggerFactory.getLogger(KnownLocoSelPane.class);

}
