// KnownLocoSelPane.java

package jmri.jmrit.symbolicprog;

import jmri.jmrit.roster.*;
import jmri.jmrit.decoderdefn.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import com.sun.java.util.collections.List;

/** 
 * Provide GUI controls to select a known loco via the Roster.
 * <P>
 * When the "open programmer" button is pushed, i.e. the user is ready to 
 * continue, the startProgrammer method is invoked.  This should be
 * overridden (e.g. in a local anonymous class) to create the programmer frame
 * you're interested in.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: KnownLocoSelPane.java,v 1.6 2002-01-02 23:48:57 jacobsen Exp $
 */
public class KnownLocoSelPane extends javax.swing.JPanel  {
		
	public KnownLocoSelPane(JLabel s) {
		_statusLabel = s;
		init();
	}

	public KnownLocoSelPane() {
		init();
	}
	
	protected void init() {
		JLabel last;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JLabel l2 = new JLabel("Known locomotive on programming track");
		l2.setBorder(new EmptyBorder(6,0,6,0));
		add(l2);
			JPanel pane2a = new JPanel();
			pane2a.setLayout(new BoxLayout(pane2a, BoxLayout.X_AXIS));
			pane2a.add(new JLabel("Select from roster:"));
			JButton idloco = new JButton("Identify locomotive");
			idloco.addActionListener( new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (log.isInfoEnabled()) log.info("Identify locomotive pressed");
					startIdentify();
				}
			});
			pane2a.add(idloco);
			pane2a.setAlignmentX(JLabel.LEFT_ALIGNMENT);				
		add(pane2a);
			
		locoBox = Roster.instance().matchingComboBox(null, null, null, null, null, null, null);
		add(locoBox);
		
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
	
	JLabel _statusLabel = null;

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
				if (_statusLabel != null) _statusLabel.setText(m);
			}
			public void error() {}
		};
		id.start();
	}

	private void selectLoco(int dccAddress) {
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
		
		String locoFile = Roster.instance().fileFromTitle((String)locoBox.getSelectedItem());
		if (log.isDebugEnabled()) log.debug("loco file: "+locoFile);
		
		startProgrammer(null, locoFile, re);
	}
	
	/** meant to be overridden to start the desired type of programmer */
	protected void startProgrammer(DecoderFile decoderFile, String locoFile, RosterEntry r) {
		log.error("startProgrammer method in NewLocoSelPane should have been overridden");
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(KnownLocoSelPane.class.getName());

}
