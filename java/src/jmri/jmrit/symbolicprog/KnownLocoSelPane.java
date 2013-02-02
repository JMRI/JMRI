// KnownLocoSelPane.java

package jmri.jmrit.symbolicprog;

import org.apache.log4j.Logger;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.IdentifyLoco;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;

/**
 * Provide GUI controls to select a known loco via the Roster.
 * <P>
 * When the "open programmer" button is pushed, i.e. the user is ready to
 * continue, the startProgrammer method is invoked.  This should be
 * overridden (e.g. in a local anonymous class) to create the programmer frame
 * you're interested in.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision$
 */
public class KnownLocoSelPane extends LocoSelPane  {

    public KnownLocoSelPane(JLabel s, boolean ident) {
        mCanIdent = ident;
        mStatusLabel = s;
        init();
    }

    public KnownLocoSelPane(boolean ident) {
        this(null, ident);
    }

    boolean mCanIdent;

    JComboBox programmerBox;

    protected void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel pane2a = new JPanel();
        pane2a.setLayout(new BoxLayout(pane2a, BoxLayout.X_AXIS));
        pane2a.add(new JLabel(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("UseExisting")));

        if (mCanIdent) {
            JButton idloco = new JButton(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("ReadAndSelect"));
            idloco.addActionListener( new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (log.isDebugEnabled()) log.debug("Identify locomotive pressed");
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
        go2.addActionListener( new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                    openButton();
                }
            });
        add(go2);
        setBorder(new EmptyBorder(6,6,6,6));
    }

    /**
     * Add the GUI for selecting a specific programmer
     */
    private void addProgrammerBox() {
        JPanel pane3a = new JPanel();
        pane3a.setLayout(new BoxLayout(pane3a, BoxLayout.X_AXIS));
        pane3a.add(new JLabel(Bundle.getMessage("ProgrammerFormat")));

        // create the programmer box
        programmerBox = new JComboBox(ProgDefault.findListOfProgFiles());
        programmerBox.setSelectedIndex(0);
        if (ProgDefault.getDefaultProgFile()!=null) programmerBox.setSelectedItem(ProgDefault.getDefaultProgFile());
        pane3a.add(programmerBox);
        // pane3a.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        add(pane3a);
    }

    JLabel mStatusLabel = null;

    private void startIdentify() {
        // start identifying a loco
        final KnownLocoSelPane me = this;
        IdentifyLoco id = new IdentifyLoco() {
                private KnownLocoSelPane who = me;
                protected void done(int dccAddress) {
				// if Done, updated the selected decoder
                    who.selectLoco(dccAddress);
                }
                protected void message(String m) {
                    if (mStatusLabel != null) mStatusLabel.setText(m);
                }
                public void error() {}
            };
        id.start();
    }

    protected void selectLoco(int dccAddress) {
        // locate that loco
        List<RosterEntry> l = Roster.instance().matchingList(null, null, Integer.toString(dccAddress),
                                                null, null, null, null);
        if (log.isDebugEnabled()) log.debug("selectLoco found "+l.size()+" matches");
        if (l.size() > 0) {
            RosterEntry r = l.get(0);
            String id = r.getId();
            if (log.isDebugEnabled()) log.debug("Loco id is "+id);
            String group = locoBox.getSelectedRosterGroup();
            if (group != null && !group.equals(Roster.ALLENTRIES)) {
                List<RosterEntry> entries = Roster.instance().getEntriesWithAttributeKeyValue(Roster.getRosterGroupProperty(group), "yes");
                if (entries.contains(r)) {
                    locoBox.setSelectedRosterEntry(r);
                } else {
                    locoBox.setSelectedRosterEntryAndGroup(r, Roster.ALLENTRIES);
                }
            } else {
                locoBox.setSelectedRosterEntry(r);
            }
        } else {
            log.warn("Read address "+dccAddress+", but no such loco in roster");
        }
    }

    private RosterEntrySelectorPanel locoBox = null;

    /** handle pushing the open programmer button by finding names, then calling a template method */
    protected void openButton() {

        if (locoBox.getSelectedRosterEntries().length != 0) {
            RosterEntry re = locoBox.getSelectedRosterEntries()[0];
            startProgrammer(null, re, (String)programmerBox.getSelectedItem());
        } else {
            JOptionPane.showMessageDialog(this, 
                    Bundle.getMessage("LocoMustSelected"), 
                    Bundle.getMessage("NoSelection"), 
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** meant to be overridden to start the desired type of programmer */
    protected void startProgrammer(DecoderFile decoderFile, RosterEntry r,
                                    String programmerName) {
        log.error("startProgrammer method in NewLocoSelPane should have been overridden");
    }

    static Logger log = Logger.getLogger(KnownLocoSelPane.class.getName());

}
