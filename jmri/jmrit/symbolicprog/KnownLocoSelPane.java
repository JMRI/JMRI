// KnownLocoSelPane.java

package jmri.jmrit.symbolicprog;

import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.IdentifyLoco;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.sun.java.util.collections.List;

/**
 * Provide GUI controls to select a known loco via the Roster.
 * <P>
 * When the "open programmer" button is pushed, i.e. the user is ready to
 * continue, the startProgrammer method is invoked.  This should be
 * overridden (e.g. in a local anonymous class) to create the programmer frame
 * you're interested in.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.8 $
 */
public class KnownLocoSelPane extends LocoSelPane  {

    public KnownLocoSelPane(JLabel s, boolean ident) {
        mCanIdent = ident;
        mStatusLabel = s;
        init();
    }

    public KnownLocoSelPane(boolean ident) {
        mCanIdent = ident;
        mStatusLabel = null;
        init();
    }

    boolean mCanIdent;

    JComboBox programmerBox;

    protected void init() {
        JLabel last;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel pane2a = new JPanel();
        pane2a.setLayout(new BoxLayout(pane2a, BoxLayout.X_AXIS));
        pane2a.add(new JLabel("Select from roster:"));

        if (mCanIdent) {
            JButton idloco = new JButton("Identify locomotive");
            idloco.addActionListener( new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (log.isInfoEnabled()) log.info("Identify locomotive pressed");
                    startIdentify();
                }
            });
            pane2a.add(idloco);
            pane2a.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        }
        add(pane2a);

        locoBox = Roster.instance().fullRosterComboBox();
        add(locoBox);

        addProgrammerBox();

        JButton go2 = new JButton("Open programmer");
        go2.addActionListener( new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (log.isInfoEnabled()) log.info("Open programmer pressed");
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
        pane3a.add(new JLabel("Programmer format: "));

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
        List l = Roster.instance().matchingList(null, null, Integer.toString(dccAddress),
                                                null, null, null, null);
        if (log.isDebugEnabled()) log.debug("selectLoco found "+l.size()+" matches");
        if (l.size() > 0) {
            RosterEntry r = (RosterEntry)l.get(0);
            String id = r.getId();
            if (log.isDebugEnabled()) log.debug("Loco id is "+id);
            for (int i = 0; i<locoBox.getItemCount(); i++) {
                if (id.equals((String)locoBox.getItemAt(i))) locoBox.setSelectedIndex(i);
            }
        } else {
            log.warn("Read address "+dccAddress+", but no such loco in roster");
        }
    }

    private JComboBox locoBox = null;

    /** handle pushing the open programmer button by finding names, then calling a template method */
    protected void openButton() {

        RosterEntry re = Roster.instance().entryFromTitle((String)locoBox.getSelectedItem());
        if (re == null) log.error("RosterEntry is null during open; that shouldnt be possible");

        startProgrammer(null, re, (String)programmerBox.getSelectedItem());
    }

    /** meant to be overridden to start the desired type of programmer */
    protected void startProgrammer(DecoderFile decoderFile, RosterEntry r,
                                    String programmerName) {
        log.error("startProgrammer method in NewLocoSelPane should have been overridden");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(KnownLocoSelPane.class.getName());

}
