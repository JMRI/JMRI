// PaneProgAction.java

package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import jmri.jmrit.decoderdefn.*;
import jmri.jmrit.roster.*;
import jmri.jmrit.symbolicprog.*;

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
 * @version			$Revision: 1.15 $
 */
public class PaneProgAction 			extends AbstractAction {

    Object o1, o2, o3, o4;
    JLabel statusLabel;
    jmri.ProgModeSelector  modePane    = new jmri.ProgDeferredServiceModePane();

    public PaneProgAction(String s) {
        super(s);

        statusLabel = new JLabel("idle");

        // start a low priority request for the Roster & DecoderInstance
        Thread xmlThread = new Thread( new Runnable() {
                public void run() {
                    Roster.instance();
                    DecoderIndexFile.instance();
				//jmri.jmrit.NameFile.instance();
                    if (log.isDebugEnabled()) log.debug("xml loading thread finishes prereading Roster, DecoderIndexFile");
                }
            }, "pre-read XML files");
        xmlThread.setPriority(Thread.NORM_PRIORITY-2);
        xmlThread.start();

    }

    public void actionPerformed(ActionEvent e) {

        if (log.isDebugEnabled()) log.debug("Pane programmer requested");

        // create the initial frame that steers
        final JFrame f = new JFrame("Service-mode Programmer Setup");
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        // add the Roster menu
        JMenuBar menuBar = new JMenuBar();
        // menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
        menuBar.add(new jmri.jmrit.roster.RosterMenu("Roster", jmri.jmrit.roster.RosterMenu.MAINMENU, f));
        f.setJMenuBar(menuBar);

        // new Loco on programming track
        JLabel last;
        JPanel pane1 = new CombinedLocoSelTreePane(statusLabel){
                protected void startProgrammer(DecoderFile decoderFile, RosterEntry re,
                                                String filename) {
                    String title = "Program new decoder on service track";
                    if (re!=null) title = "Program "+re.getId()+" on service track";
                    JFrame p = new PaneServiceProgFrame(decoderFile, re,
                                                 title, "programmers"+File.separator+filename+".xml",
                                                 modePane.getProgrammer());
                    p.pack();
                    p.show();
                    f.setVisible(false);
                    f.dispose();
                }
            };

        // load primary frame
        modePane.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        f.getContentPane().add(modePane);
        f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));

        pane1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        f.getContentPane().add(pane1);
        f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));

        statusLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        f.getContentPane().add(statusLabel);

        f.pack();
        if (log.isDebugEnabled()) log.debug("Tab-Programmer setup created");
        f.show();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgAction.class.getName());

}

/* @(#)PaneProgAction.java */
