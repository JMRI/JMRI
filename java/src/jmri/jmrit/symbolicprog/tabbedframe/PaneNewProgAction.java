package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.LocoSelTreePane;
import jmri.jmrit.symbolicprog.SymbolicProgBundle;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a frame for selecting the information
 * needed to open a PaneProgFrame for creating a new Roster entry.
 * <p>
 * The resulting JFrame is constructed on the fly here, and has no specific
 * type.
 * <p>
 * Note that this just works with the roster, no programming track or
 * layout operations are present.
 *
 * @see jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2015
 */
public class PaneNewProgAction extends AbstractAction {

    Object o1, o2, o3, o4;

    public PaneNewProgAction() {
        this("DecoderPro service programmer");
    }

    public PaneNewProgAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (log.isDebugEnabled()) {
            log.debug("New roster entry programmer requested");
        }

        // create the initial frame that steers
        final JmriJFrame f = new JmriJFrame(SymbolicProgBundle.getMessage("FrameNewEntrySetup"));
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        // add the Roster menu
        JMenuBar menuBar = new JMenuBar();
        // menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
        JMenu j = new JMenu(SymbolicProgBundle.getMessage("MenuFile"));
        j.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(SymbolicProgBundle.getMessage("MenuPrintDecoderDefinitions"), f, false));
        j.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(SymbolicProgBundle.getMessage("MenuPrintPreviewDecoderDefinitions"), f, true));
        menuBar.add(j);
        menuBar.add(new jmri.jmrit.roster.swing.RosterMenu(SymbolicProgBundle.getMessage("MenuRoster"), jmri.jmrit.roster.swing.RosterMenu.MAINMENU, f));
        f.setJMenuBar(menuBar);

        // new Loco on programming track
        JPanel pane1 = new LocoSelTreePane(null, null) {

            @Override
            protected void startProgrammer(DecoderFile decoderFile, RosterEntry re,
                    String filename) {
                String title = SymbolicProgBundle.getMessage("FrameNewEntryTitle");
                JFrame p = new PaneProgFrame(decoderFile, re,
                        title, "programmers" + File.separator + filename + ".xml",
                        null, false) {

                    @Override
                            protected JPanel getModePane() {
                                return null;
                            }
                        };
                p.pack();
                p.setVisible(true);

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

    private final static Logger log = LoggerFactory.getLogger(PaneProgAction.class);

}
