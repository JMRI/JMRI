// CombinedLocoSelPane.java

package jmri.jmrit.symbolicprog;

import jmri.jmrit.roster.*;
import jmri.jmrit.decoderdefn.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import com.sun.java.util.collections.List;

/** 
 * Provide GUI controls to select a known loco and/or new decoder.
 * <P>
 * When the "open programmer" button is pushed, i.e. the user is ready to 
 * continue, the startProgrammer method is invoked.  This should be
 * overridden (e.g. in a local anonymous class) to create the programmer frame
 * you're interested in.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: CombinedLocoSelPane.java,v 1.3 2001-12-10 06:30:25 jacobsen Exp $
 */
public class CombinedLocoSelPane extends javax.swing.JPanel  {
		
	public CombinedLocoSelPane(JLabel s) {
		_statusLabel = s;
		init();
	}

	public CombinedLocoSelPane() {
		init();
	}
	
	protected void init() {
		JLabel last;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			JPanel pane2a = new JPanel();
			pane2a.setLayout(new BoxLayout(pane2a, BoxLayout.X_AXIS));
			pane2a.add(new JLabel("Use locomotive settings for: "));
			locoBox = Roster.instance().matchingComboBox(null, null, null, null, null, null, null);
			locoBox.insertItemAt("<none - new loco>",0);
			locoBox.setSelectedIndex(0);
			pane2a.add(locoBox);
			locoBox.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (locoBox.getSelectedIndex()!=0) {
						// reset and disable decoder selection
						decoderBox.setSelectedIndex(0);
						go2.setEnabled(true);
						go2.setToolTipText("Click to open the programmer");
					} else {
						go2.setEnabled(false);
						go2.setToolTipText("Select a locomotive or decoder to enable");
					}
				}
			});
			idloco = new JToggleButton("Ident");
			idloco.setToolTipText("Read the locomotive's address and attempt to select the right settings");
			idloco.addActionListener( new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (log.isInfoEnabled()) log.info("Identify locomotive pressed");
					startIdentifyLoco();
				}
			});
			pane2a.add(idloco);
			pane2a.setAlignmentX(JLabel.RIGHT_ALIGNMENT);				
		add(pane2a);
			
		
			JPanel pane1a = new JPanel();
			pane1a.setLayout(new BoxLayout(pane1a, BoxLayout.X_AXIS));
			pane1a.add(new JLabel("Decoder installed: "));
			decoderBox = DecoderIndexFile.instance().matchingComboBox(null, null, null, null, null);
			decoderBox.insertItemAt("<from locomotive settings>",0);
			decoderBox.setSelectedIndex(0);
			decoderBox.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (decoderBox.getSelectedIndex()!=0) {
						// reset and disable loco selection
						locoBox.setSelectedIndex(0);
						go2.setEnabled(true);
						go2.setToolTipText("Click to open the programmer");
					} else {
						go2.setEnabled(false);
						go2.setToolTipText("Select a locomotive or decoder to enable");
					}
				}
			});
			pane1a.add(decoderBox);
			iddecoder= new JToggleButton("Ident");
			idloco.setToolTipText("Read the decoders mfg and version, then attempt to select it's type");
			iddecoder.addActionListener( new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (log.isInfoEnabled()) log.info("identify decoder pressed");
					startIdentifyDecoder();
				}
			});
			pane1a.add(iddecoder);
			pane1a.setAlignmentX(JLabel.RIGHT_ALIGNMENT);				
		add(pane1a);
		
		go2 = new JButton("Open Programmer");
		go2.addActionListener( new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (log.isInfoEnabled()) log.info("Open programmer pressed");
				openButton();
			}
		});
		go2.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
		go2.setEnabled(false);
		go2.setToolTipText("Select a locomotive or decoder to enable");
		add(go2);
		setBorder(new EmptyBorder(6,6,6,6));
	}
	
	JLabel _statusLabel = null;

	private void startIdentifyLoco() {
		// start identifying a loco
		final CombinedLocoSelPane me = this;
		IdentifyLoco id = new IdentifyLoco() {
			private CombinedLocoSelPane who = me;
			protected void done(int dccAddress) {
				// if Done, updated the selected decoder
				who.selectLoco(dccAddress);
			}
			protected void message(String m) {
				if (_statusLabel != null) _statusLabel.setText(m);
			}
		};
		id.start();
	}

	private void startIdentifyDecoder() {
		// start identifying a decoder
		final CombinedLocoSelPane me = this;
		IdentifyDecoder id = new IdentifyDecoder() {
			private CombinedLocoSelPane who = me;
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

	private void selectLoco(int dccAddress) {
		// raise the button again
		idloco.setSelected(false);
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
			
	private void selectDecoder(int mfgID, int modelID) {
		// raise the button again
		iddecoder.setSelected(false);
		// locate a decoder like that.
		JComboBox temp = DecoderIndexFile.instance().matchingComboBox(null, null, Integer.toString(mfgID), Integer.toString(modelID), null);
		if (log.isDebugEnabled()) log.debug("selectDecoder found "+temp.getItemCount()+" matches");
		// install all those in the JComboBox in place of the longer, original list
		System.out.println((String)temp.getItemAt(0));
		System.out.println((String)temp.getItemAt(1));
		if (temp.getItemCount() > 0) {
			decoderBox.setModel(temp.getModel());
			decoderBox.setSelectedIndex(0);
		} else {
			log.warn("Decoder says "+mfgID+" "+modelID+" decoder, but no such decoder defined");
		}
	}

	private JComboBox locoBox = null;
	private JComboBox decoderBox = null;
	private JToggleButton iddecoder;
	private JToggleButton idloco;
	private JButton go2;
	
	/** handle pushing the open programmer button by finding names, then calling a template method */
	protected void openButton() {
		// figure out which we're dealing with
		if (decoderBox.getSelectedIndex()!=0) {
			// new loco
			openNewLoco();
		} else if (locoBox.getSelectedIndex()!=0) {
			// known loco
			openKnownLoco();
		} else {
			// should not happen, as the button should be disabled!
			log.error("openButton with neither combobox nonzero");
		}
	}
	
	protected void openKnownLoco() {

		RosterEntry re = Roster.instance().entryFromTitle((String)locoBox.getSelectedItem());
		if (re == null) log.error("RosterEntry is null during open; that shouldnt be possible");
		
		String locoFile = Roster.instance().fileFromTitle((String)locoBox.getSelectedItem());
		if (log.isDebugEnabled()) log.debug("loco file: "+locoFile);
		
		startProgrammer(null, locoFile, re);
	}
	
	protected void openNewLoco() {
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
		log.error("startProgrammer method in CombinedLocoSelPane should have been overridden");
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CombinedLocoSelPane.class.getName());

}
