// PaneOpsProgAction.java

package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import jmri.jmrit.decoderdefn.*;
import jmri.jmrit.roster.*;
import jmri.jmrit.symbolicprog.*;
import jmri.*;

/**
 * Swing action to create and register a
 * frame for selecting the information needed to
 * open a PaneProgFrame in service mode.
 * <P>
 * The name is a historical accident, and probably should have
 * included "ServiceMode" or something.
 * <P>
 * The resulting JFrame
 * is constructed on the fly here, and has no specific type.
 *
 * @see  jmri.jmrit.symbolicprog.PaneOpsProgAction
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
public class PaneOpsProgAction 	extends AbstractAction {

    Object o1, o2, o3, o4;
    JLabel statusLabel;

    public PaneOpsProgAction(String s) {
        super(s);

        statusLabel = new JLabel("idle");

        // disable ourself if ops programming is not possible
        if (jmri.InstanceManager.programmerManagerInstance()==null ||
            !jmri.InstanceManager.programmerManagerInstance().isOpsModePossible()) {
            setEnabled(false);
        }

        // start a low priority request for the Roster & DecoderInstance
        Thread xmlThread = new Thread( new Runnable() {
                public void run() {
                    Roster.instance();
                    DecoderIndexFile.instance();
                    if (log.isDebugEnabled()) log.debug("xml loading thread finishes prereading Roster, DecoderIndexFile");
                }
            }, "pre-read XML files");
        xmlThread.setPriority(Thread.NORM_PRIORITY-2);
        xmlThread.start();

    }

    public void actionPerformed(ActionEvent e) {

        if (log.isInfoEnabled()) log.info("Pane programmer requested");

        // create the initial frame that steers
        final JFrame f = new JFrame("Ops-mode Programmer Setup");
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        // add the Roster menu
        JMenuBar menuBar = new JMenuBar();
        // menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
        menuBar.add(new jmri.jmrit.roster.RosterMenu("Roster", jmri.jmrit.roster.RosterMenu.MAINMENU, f));
        f.setJMenuBar(menuBar);

        // known loco on main track
        JLabel last;
        JPanel pane1 = new KnownLocoSelPane(false){  // no ident in ops mode yet

            protected void startProgrammer(DecoderFile decoderFile, RosterEntry re,
                                                String filename) {
                String title = "Program "+re.getId();
                // find the ops-mode programmer
                int address = Integer.parseInt(re.getDccAddress());
                boolean longAddr = true;
                if (address<100) longAddr = false;
                Programmer programmer = InstanceManager.programmerManagerInstance()
                                            .getOpsModeProgrammer(longAddr, address);
                // and created the frame
                JFrame p = new PaneOpsProgFrame(decoderFile, re,
                                                 title, "programmers"+File.separator+filename+".xml",
                                                 programmer);
                p.pack();
                p.show();
                f.setVisible(false);
                f.dispose();
            }
        };

        // load primary frame
        pane1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        f.getContentPane().add(pane1);

        f.pack();
        if (log.isInfoEnabled()) log.info("Tab-Programmer setup created");
        f.show();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneOpsProgAction.class.getName());

}

/* @(#)PaneOpsProgAction.java */
