// PaneNewProgAction.java

package jmri.jmrit.symbolicprog.tabbedframe;

import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.LocoSelTreePane;
import jmri.util.JmriJFrame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Swing action to create and register a
 * frame for selecting the information needed to
 * open a PaneProgFrame for creating a new Roster entry.
 * <P>
 * The resulting JFrame
 * is constructed on the fly here, and has no specific type.
 *
 * @see  jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @version			$Revision: 1.1 $
 */
public class PaneNewProgAction extends AbstractAction {

    Object o1, o2, o3, o4;

    static final java.util.ResourceBundle rbt 
        = java.util.ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle");

    public PaneNewProgAction() {
        this("DecoderPro service programmer");
    }

    public PaneNewProgAction(String s) {
        super(s);

        // disable ourself if programming is not possible
        if (jmri.InstanceManager.programmerManagerInstance()==null ||
            !jmri.InstanceManager.programmerManagerInstance().isServiceModePossible()) {
            setEnabled(false);
            // This needs to return, so we don't start the xmlThread
	    return;
        }

    }

    public void actionPerformed(ActionEvent e) {

        if (log.isDebugEnabled()) log.debug("New roster entry programmer requested");

        // create the initial frame that steers
        final JmriJFrame f = new JmriJFrame(rbt.getString("FrameNewEntrySetup"));
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));
        
        // add the Roster menu
        JMenuBar menuBar = new JMenuBar();
        // menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
        JMenu j = new JMenu("File");
        j.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(rbt.getString("MenuPrintDecoderDefinitions"), f, false));
        j.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(rbt.getString("MenuPrintPreviewDecoderDefinitions"), f, true));
        menuBar.add(j);
        menuBar.add(new jmri.jmrit.roster.RosterMenu(rbt.getString("MenuRoster"), jmri.jmrit.roster.RosterMenu.MAINMENU, f));
        f.setJMenuBar(menuBar);

        // new Loco on programming track
        JLabel last;
        JPanel pane1 = new LocoSelTreePane(null){
                protected void startProgrammer(DecoderFile decoderFile, RosterEntry re,
                                                String filename) {
                    String title = rbt.getString("FrameNewEntryTitle");
                    JFrame p = new PaneProgFrame(decoderFile, re,
                                                 title, "programmers"+File.separator+filename+".xml",
                                                 null, false){
                        JPanel getModePane() { return null; }
                    };
                    p.pack();
                    p.setVisible(true);

                }
            };

        // load primary frame

        pane1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        f.getContentPane().add(pane1);

        f.pack();
        if (log.isDebugEnabled()) log.debug("Tab-Programmer setup created");
        f.setVisible(true);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgAction.class.getName());

}

/* @(#)PaneProgAction.java */
