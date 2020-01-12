package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.KnownLocoSelPane;
import jmri.jmrit.symbolicprog.SymbolicProgBundle;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a frame for selecting the information
 * needed to open a PaneProgFrame in service mode.
 * <p>
 * The name is a historical accident, and probably should have included
 * "ServiceMode" or something.
 * <p>
 * The resulting JFrame is constructed on the fly here, and has no specific
 * type.
 *
 * @see jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class PaneOpsProgAction extends AbstractAction {

    Object o1, o2, o3, o4;
    JLabel statusLabel;

    public PaneOpsProgAction() {
        this("DecoderPro ops-mode programmer");
    }

    public PaneOpsProgAction(String s) {
        super(s);

        statusLabel = new JLabel(SymbolicProgBundle.getMessage("StateIdle"));

        // disable ourself if ops programming is not possible
        if (jmri.InstanceManager.getNullableDefault(jmri.AddressedProgrammerManager.class) == null
                || !jmri.InstanceManager.getDefault(jmri.AddressedProgrammerManager.class).isAddressedModePossible()) {
            setEnabled(false);
            // This needs to return so the xmlThread is not started;
            return;
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (log.isDebugEnabled()) {
            log.debug("Pane programmer requested");
        }

        // create the initial frame that steers
        final JmriJFrame f = new JmriJFrame(SymbolicProgBundle.getMessage("FrameOpsProgrammerSetup"));
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        // add the Roster menu
        JMenuBar menuBar = new JMenuBar();
        // menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
        menuBar.add(new jmri.jmrit.roster.swing.RosterMenu(SymbolicProgBundle.getMessage("MenuRoster"),
                jmri.jmrit.roster.swing.RosterMenu.MAINMENU, f));
        f.setJMenuBar(menuBar);

        // known loco on main track
        JPanel pane1 = new KnownLocoSelPane(false) {  // no ident in ops mode yet

            @Override
            protected void startProgrammer(DecoderFile decoderFile, RosterEntry re,
                    String filename) {
                String title = java.text.MessageFormat.format(SymbolicProgBundle.getMessage("FrameOpsProgrammerTitle"),
                        new Object[]{re.getId()});
                // find the ops-mode programmer
                int address = Integer.parseInt(re.getDccAddress());
                boolean longAddr = re.isLongAddress();
                Programmer programmer = InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)
                        .getAddressedProgrammer(longAddr, address);
                // and created the frame
                JFrame p = new PaneOpsProgFrame(decoderFile, re,
                        title, "programmers" + File.separator + filename + ".xml",
                        programmer);
                p.pack();
                p.setVisible(true);

                // f.setVisible(false);
                // f.dispose();
            }
        };

        // load primary frame
        pane1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        f.getContentPane().add(pane1);

        f.pack();
        if (log.isDebugEnabled()) {
            log.debug("Tab-Programmer setup created");
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(PaneOpsProgAction.class);

}
