// PaneProgAction.java

package jmri.jmrit.symbolicprog.tabbedframe;

import jmri.jmrit.symbolicprog.*;
import jmri.jmrit.decoderdefn.*;
import jmri.jmrit.roster.*;

import java.awt.event.*;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
//import org.jdom.*;
//import org.jdom.input.*;

/**
 * Swing action to create and register a
 * frame used to eventually open the PaneProgFrame. That JFrame
 * is constructed on the fly here, and has no specific type.
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.6 $
 */
public class PaneProgAction 			extends AbstractAction {

	Object o1, o2, o3, o4;
	JLabel statusLabel;

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

		// start a read low priority request to load some classes
		final ClassLoader loader = this.getClass().getClassLoader();
		Thread classLoadingThread = new Thread( new Runnable() {
				public void run() {
					// load classes by requesting objects
					new PaneProgFrame();
					new PaneProgPane();
					new EnumVariableValue();
					new SpeedTableVarValue();

					if (log.isInfoEnabled()) log.info("class loading thread finishes");
				}
			}, "loading classes");
		classLoadingThread.setPriority(Thread.MIN_PRIORITY);
		classLoadingThread.start();

	}

    public void actionPerformed(ActionEvent e) {

		if (log.isInfoEnabled()) log.info("Pane programmer requested");

		// create the initial frame that steers
		final JFrame f = new JFrame("Tab-Programmer Setup");
		f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

		// new Loco on programming track
		JLabel last;
		JPanel pane1 = new CombinedLocoSelPane(statusLabel){
			protected void startProgrammer(DecoderFile decoderFile, RosterEntry re, String filename) {
				String title = "Program new decoder";
				if (re!=null) title = "Program "+re.getId();
				JFrame p = new PaneProgFrame(decoderFile, re,
												title, "programmers"+File.separator+filename+".xml");
				p.pack();
				p.show();
				f.setVisible(false);
				f.dispose();
			}
		};

		// load primary frame
		pane1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		f.getContentPane().add(pane1);
		f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));

        jmri.ProgModePane   modePane    = new jmri.ProgModePane(BoxLayout.X_AXIS);
        f.getContentPane().add(modePane);
		f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));

		statusLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		f.getContentPane().add(statusLabel);

		f.pack();
		if (log.isInfoEnabled()) log.info("Tab-Programmer setup created");
		f.show();
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgAction.class.getName());

}


/* @(#)PanecProgAction.java */
