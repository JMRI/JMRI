// NewLocoSelPane.java

package jmri.jmrit.symbolicprog;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.decoderdefn.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import com.sun.java.util.collections.List;

/** 
 * Provide GUI controls to select a decoder for a new loco and/or copy an existing config.
 *<P>
 * The user can select either a loco to copy, or a new decoder type or both.
 * <P>
 * When the "open programmer" button is pushed, i.e. the user is ready to 
 * continue, the startProgrammer method is invoked.  This should be
 * overridden (e.g. in a local anonymous class) to create the programmer frame
 * you're interested in.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: NewLocoSelPane.java,v 1.7 2001-12-02 05:46:42 jacobsen Exp $
 */
public class NewLocoSelPane extends javax.swing.JPanel  {
			
	public NewLocoSelPane(JLabel s) {
		_statusLabel = s;
		init();
	}
	
	public NewLocoSelPane() {
		init();
	}

	public void init() {
		JLabel last;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(last = new JLabel("New locomotive on programming track"));
		last.setBorder(new EmptyBorder(6,0,6,0));
		add(new JLabel("Copy settings from existing locomotive:"));

		locoBox = Roster.instance().matchingComboBox(null, null, null, null, null, null, null);
		locoBox.addActionListener( new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (log.isInfoEnabled()) log.info("Locomotive selected changed");
				matchDecoderToLoco();
			}
		});
		locoBox.insertItemAt("<none>",0);
		locoBox.setSelectedIndex(0);
		add(locoBox);
		
			JPanel pane1a = new JPanel();
			pane1a.setLayout(new BoxLayout(pane1a, BoxLayout.X_AXIS));
			pane1a.add(new JLabel("Decoder installed:"));
			JButton iddecoder= new JButton("Identify decoder");
			iddecoder.addActionListener( new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (log.isInfoEnabled()) log.info("identify decoder pressed");
					startIdentify();
				}
			});
			pane1a.add(iddecoder);
			pane1a.setAlignmentX(JLabel.LEFT_ALIGNMENT);				
		add(pane1a);
		
		DecoderIndexFile.instance();
		decoderBox = DecoderIndexFile.instance().matchingComboBox(null, null, null, null, null);
		add(decoderBox);
		
		// Open programmer button
		JButton go1 = new JButton("Open programmer");
		go1.addActionListener( new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (log.isInfoEnabled()) log.info("Open programmer pressed");
				openButton();
			}
		});
		add(go1);
		setBorder(new EmptyBorder(6,6,6,6));
	}

	JLabel _statusLabel = null;
	
	private void startIdentify() {
		// start identifying a decoder
		final NewLocoSelPane me = this;
		IdentifyDecoder id = new IdentifyDecoder() {
			private NewLocoSelPane who = me;
			protected void done(int mfg, int model) {
				// if Done, updated the selected decoder
				who.selectDecoder(mfg, model);
			}
			protected void message(String m) {
				if (_statusLabel != null) _statusLabel.setText(m);
			}
		};
		id.start();
	}
		
	private void selectDecoder(int mfgID, int modelID) {
		// locate a decoder like that.
		List l = DecoderIndexFile.instance().matchingDecoderList(null, null, Integer.toString(mfgID), Integer.toString(modelID), null);
		if (log.isDebugEnabled()) log.debug("selectDecoder found "+l.size()+" matches");
		if (l.size() > 0) {
			DecoderFile d = (DecoderFile)l.get(0);
			String title = d.titleString();
			if (log.isDebugEnabled()) log.debug("Decoder file title "+title);
			for (int i = 0; i<decoderBox.getItemCount(); i++) {
				if (title.equals((String)decoderBox.getItemAt(i))) decoderBox.setSelectedIndex(i);
			}
		} else {
			log.warn("Decoder says "+mfgID+" "+modelID+" decoder, but no such decoder defined");
		}
	}
		
	private void matchDecoderToLoco() {
		if (((String)locoBox.getSelectedItem()).equals("<none>")) return;
		RosterEntry r = Roster.instance().entryFromTitle((String) locoBox.getSelectedItem());
		String decoderModel = r.getDecoderModel();
		String decoderFamily = r.getDecoderFamily();
		if (log.isDebugEnabled()) log.debug("selected loco uses decoder "+decoderFamily+" "+decoderModel);
		// locate a decoder like that.
		List l = DecoderIndexFile.instance().matchingDecoderList(null, decoderFamily, null, null, decoderModel);
		if (log.isDebugEnabled()) log.debug("found "+l.size()+" matches");
		if (l.size() > 0) {
			DecoderFile d = (DecoderFile)l.get(0);
			String title = d.titleString();
			if (log.isDebugEnabled()) log.debug("Decoder file title "+title);
			for (int i = 0; i<decoderBox.getItemCount(); i++) {
				if (title.equals((String)decoderBox.getItemAt(i))) decoderBox.setSelectedIndex(i);
			}
		} else {
			log.warn("Loco uses "+decoderFamily+" "+decoderModel+" decoder, but no such decoder defined");
		}
	}
	
	private JComboBox locoBox = null;
	private JComboBox decoderBox = null;
	
	/** handle pushing the open programmer button by finding names, then calling a template method */
	protected void openButton() {
		String locoFile = null;
		
		// find the loco file
		if ( ! ((String)locoBox.getSelectedItem()).equals("<none>")) {
		
			locoFile = Roster.instance().fileFromTitle((String)locoBox.getSelectedItem());
			if (log.isDebugEnabled()) log.debug("loco file: "+locoFile);
		}

		// find the decoderFile object
		DecoderFile decoderFile = DecoderIndexFile.instance().fileFromTitle((String)decoderBox.getSelectedItem());
		if (log.isDebugEnabled()) log.debug("decoder file: "+decoderFile.getFilename());

		// create a dummy RosterEntry with the decoder info
		RosterEntry re = new RosterEntry();
		re.setDecoderFamily(decoderFile.getFamily());
		re.setDecoderModel(decoderFile.getModel());
		re.setId("<new loco>");
		// add the new roster entry to the in-memory roster
		Roster.instance().addEntry(re);
		
		startProgrammer(decoderFile, locoFile, re);
	}
	
	/** meant to be overridden to start the desired type of programmer */
	protected void startProgrammer(DecoderFile decoderFile, String locoFile, RosterEntry r) {
		log.error("startProgrammer method in NewLocoSelPane should have been overridden");
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NewLocoSelPane.class.getName());

}
