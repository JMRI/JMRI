// CombinedLocoSelPane.java

package jmri.jmrit.symbolicprog;

import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.decoderdefn.IdentifyDecoder;
import jmri.jmrit.roster.IdentifyLoco;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import com.sun.java.util.collections.List;

/**
 * Provide GUI controls to select a known loco and/or new decoder.
 * <P>
 * When the "open programmer" button is pushed, i.e. the user is ready to
 * continue, the startProgrammer method is invoked.  This should be
 * overridden (e.g. in a local anonymous class) to create the programmer frame
 * you're interested in.
 *
 * <P>To overide this class to use a different decoder-selection GUI,
 * replace members:
 * <UL>
 * <LI>layoutDecoderSelection
 * <LI>updateForDecoderTypeID
 * <LI>updateForDecoderMfgID
 * <LI>updateForDecoderNotID
 * <LI>resetDecoder
 * <LI>isDecoderSelected
 * <LI>selectedDecoderName
 * </UL>
 * 
 * <P>
 * On MacOS Classic, this class was causing a problem with multiple 
 * initialization of the programmer file default.  See
 * {@link ProgDefaults} and 
 * {@link jmri.jmrit.symbolicprog.configurexml.ProgrammerConfigPaneXml}
 * for further information.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.20 $
 */
public class CombinedLocoSelPane extends LocoSelPane implements PropertyChangeListener {
    
    public CombinedLocoSelPane(JLabel s) {
        _statusLabel = s;
        init();
    }
    
    public CombinedLocoSelPane() {
        init();
    }
    
    /**
     * Create the panel used to select the decoder
     * @return a JPanel for handling the decoder-selection GUI
     */
    protected JPanel layoutDecoderSelection() {
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
                        go2.setRequestFocusEnabled(true);
                        go2.requestFocus();
                        go2.setToolTipText("Click to open the programmer");
                    } else {
                        go2.setEnabled(false);
                        go2.setToolTipText("Select a locomotive or decoder to enable");
                    }
        	}
            });
        pane1a.add(decoderBox);
        iddecoder= new JToggleButton("Ident");
        iddecoder.setToolTipText("Read the decoders mfg and version, then attempt to select its type");
        if (!jmri.InstanceManager.programmerManagerInstance().getServiceModeProgrammer().getCanRead()) {
            // can't read, disable the button
            iddecoder.setEnabled(false);
            iddecoder.setToolTipText("Button disabled because configured command station can't read CVs");
        }
        iddecoder.addActionListener( new ActionListener() {
        	public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (log.isDebugEnabled()) log.debug("identify decoder pressed");
                    startIdentifyDecoder();
        	}
            });
        pane1a.add(iddecoder);
        pane1a.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        return pane1a;
    }
    
    /**
     *  Set the decoder GUI back to having no selection
     */
    void setDecoderSelectionFromLoco(String loco) {
        decoderBox.setSelectedIndex(0);
    }
    
    /**
     * Has the user selected a decoder type, either manually or
     * via a successful event?
     * @return true if a decoder type is selected
     */
    boolean isDecoderSelected() {
        return decoderBox.getSelectedIndex()!=0;
    }
    
    /**
     * Convert the decoder selection UI result into a name.
     * @return The selected decoder type name, or null if none selected.
     */
    String selectedDecoderType() {
        if (!isDecoderSelected()) return null;
        else return (String)decoderBox.getSelectedItem();
    }
    
    /**
     * Initialize the GUI
     */
    protected void init() {
        JLabel last;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel pane2a = new JPanel();
        pane2a.setLayout(new BoxLayout(pane2a, BoxLayout.X_AXIS));
        pane2a.add(new JLabel("Use locomotive settings for: "));
        locoBox = Roster.instance().matchingComboBox(null, null, null, null, null, null, null);
        Roster.instance().addPropertyChangeListener(this);
        locoBox.insertItemAt("<none - new loco>",0);
        locoBox.setSelectedIndex(0);
        pane2a.add(locoBox);
        locoBox.addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (locoBox.getSelectedIndex()!=0) {
                        // reset and disable decoder selection
                        setDecoderSelectionFromLoco((String)locoBox.getSelectedItem());
                        go2.setEnabled(true);
                        go2.setRequestFocusEnabled(true);
                        go2.requestFocus();
                        go2.setToolTipText("Click to open the programmer");
                    } else {
                        go2.setEnabled(false);
                        go2.setToolTipText("Select a locomotive or decoder to enable");
                    }
                }
            });
        idloco = new JToggleButton("Ident");
        idloco.setToolTipText("Read the locomotive's address and attempt to select the right settings");
        if (jmri.InstanceManager.programmerManagerInstance() != null &&
            jmri.InstanceManager.programmerManagerInstance().getServiceModeProgrammer()!= null
            && !jmri.InstanceManager.programmerManagerInstance().getServiceModeProgrammer().getCanRead()) {
            // can't read, disable the button
            idloco.setEnabled(false);
            idloco.setToolTipText("Button disabled because configured command station can't read CVs");
        }
        idloco.addActionListener( new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (log.isDebugEnabled()) log.debug("Identify locomotive pressed");
                    startIdentifyLoco();
                }
            });
        pane2a.add(idloco);
        pane2a.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        add(pane2a);
        
        
        add(layoutDecoderSelection());
        
        JPanel pane3a = new JPanel();
        pane3a.setLayout(new BoxLayout(pane3a, BoxLayout.X_AXIS));
        pane3a.add(new JLabel("Programmer format: "));
        
        // create the programmer box
        programmerBox = new JComboBox(ProgDefault.findListOfProgFiles());
        programmerBox.setSelectedIndex(0);
        if (ProgDefault.getDefaultProgFile()!=null) programmerBox.setSelectedItem(ProgDefault.getDefaultProgFile());
        pane3a.add(programmerBox);
        pane3a.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        add(pane3a);
        
        go2 = new JButton("Open Programmer");
        go2.addActionListener( new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                    openButton();
                }
            });
        go2.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        go2.setEnabled(false);
        go2.setToolTipText("Select a locomotive or decoder to enable");
        add(go2);
        setBorder(new EmptyBorder(6,6,6,6));
    }
    
    /**
     * Reference to an external (not in this pane) JLabel that should
     * be updated with status information as identification happens.
     */
    JLabel _statusLabel = null;
    
    /**
     * Identify loco button pressed, start the identify operation
     * This defines what happens when the identify is done.
     */
    protected void startIdentifyLoco() {
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
                protected void error() {
                    // raise the button again
                    idloco.setSelected(false);
                }
            };
        id.start();
    }
    
    /**
     * Identify loco button pressed, start the identify operation.
     * This defines what happens when the identify is done.
     */
    protected void startIdentifyDecoder() {
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
                protected void error() {
                    // raise the button again
                    iddecoder.setSelected(false);
                }
            };
        id.start();
    }
    
    /**
     * Notification that the Roster has changed, so the locomotive
     * selection list has to be changed.
     * @param ev Ignored.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        Roster.instance().updateComboBox(locoBox);
        locoBox.insertItemAt("<none - new loco>",0);
        locoBox.setSelectedIndex(0);
    }
    
    /**
     * Identify locomotive complete, act on it by setting the GUI.
     * This will fire "GUI changed" events which will reset the
     * decoder GUI.
     * @param dccAddress
     */
    protected void selectLoco(int dccAddress) {
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
            _statusLabel.setText("Read address "+dccAddress+", but no such loco in roster");
        }
    }
    
    /**
     * Identify decoder complete, act on it by setting the GUI
     * This will fire "GUI changed" events which will reset the
     * locomotive GUI.
     * @param mfgID the decoder's manufacturer ID value from CV8
     * @param modelID the decoder's model ID value from CV7
     */
    protected void selectDecoder(int mfgID, int modelID) {
        // raise the button again
        iddecoder.setSelected(false);
        // locate a decoder like that.
        List temp = DecoderIndexFile.instance().matchingDecoderList(null, null, Integer.toString(mfgID), Integer.toString(modelID), null);
        if (log.isDebugEnabled()) log.debug("selectDecoder found "+temp.size()+" matches");
        // install all those in the JComboBox in place of the longer, original list
        if (temp.size() > 0) {
            updateForDecoderTypeID(temp);
        } else {
            String mfg = DecoderIndexFile.instance().mfgNameFromId(Integer.toString(mfgID));
            if (mfg==null) {
                updateForDecoderNotID(mfgID, modelID);
            }
            else {
                updateForDecoderMfgID(mfg, mfgID, modelID);
            }
        }
    }
    
    /**
     * Decoder identify has matched one or more specific types
     */
    void updateForDecoderTypeID(List pList) {
        decoderBox.setModel(DecoderIndexFile.jComboBoxModelFromList(pList));
        decoderBox.insertItemAt("<from locomotive settings>",0);
        decoderBox.setSelectedIndex(1);
    }
    /**
     * Decoder identify has not matched specific types, but did
     * find manufacturer match
     * @param pMfg Manufacturer name. This is passed to save time,
     *              as it has already been determined once.
     * @param pMfgID Manufacturer ID number (CV8)
     * @param pModelID Model ID number (CV7)
     */
    void updateForDecoderMfgID(String pMfg, int pMfgID, int pModelID) {
        String msg = "Found mfg "+pMfgID+" ("+pMfg+") version "+pModelID+"; no such decoder defined";
        log.warn(msg);
        _statusLabel.setText(msg);
        // try to select all decoders from that MFG
        JComboBox temp = DecoderIndexFile.instance().matchingComboBox(null, null, Integer.toString(pMfgID), null, null);
        if (log.isDebugEnabled()) log.debug("mfg-only selectDecoder found "+temp.getItemCount()+" matches");
        // install all those in the JComboBox in place of the longer, original list
        if (temp.getItemCount() > 0) {
            decoderBox.setModel(temp.getModel());
            decoderBox.insertItemAt("<from locomotive settings>",0);
            decoderBox.setSelectedIndex(1);
        } else {
            // if there are none from this mfg, go back to showing everything
            temp = DecoderIndexFile.instance().matchingComboBox(null, null, null, null, null);
            decoderBox.setModel(temp.getModel());
            decoderBox.insertItemAt("<from locomotive settings>",0);
            decoderBox.setSelectedIndex(1);
        }
    }
    /**
     * Decoder identify did not match anything, warn and show all
     */
    void updateForDecoderNotID(int pMfgID, int pModelID) {
        log.warn("Found mfg "+pMfgID+" version "+pModelID+"; no such manufacterer defined");
        JComboBox temp = DecoderIndexFile.instance().matchingComboBox(null, null, null, null, null);
        decoderBox.setModel(temp.getModel());
        decoderBox.insertItemAt("<from locomotive settings>",0);
        decoderBox.setSelectedIndex(1);
    }
    
    protected JComboBox locoBox = null;
    private JComboBox decoderBox = null;       // private because children will override this
    protected JComboBox programmerBox = null;
    protected JToggleButton iddecoder;
    protected JToggleButton idloco;
    protected JButton go2;
    
    /** handle pushing the open programmer button by finding names, then calling a template method */
    protected void openButton() {
        // figure out which we're dealing with
        if (locoBox.getSelectedIndex()!=0) {
            // known loco
            openKnownLoco();
        } else if (isDecoderSelected()) {
            // new loco
            openNewLoco();
        } else {
            // should not happen, as the button should be disabled!
            log.error("openButton with neither combobox nonzero");
        }
    }
    
    /**
     * Start with a locomotive selected, so we're opening an existing
     * RosterEntry.
     */
    protected void openKnownLoco() {
        
        RosterEntry re = Roster.instance().entryFromTitle((String)locoBox.getSelectedItem());
        if (re == null) log.error("RosterEntry is null during open; that shouldnt be possible");
        
        if (log.isDebugEnabled()) log.debug("loco file: "
                                            +Roster.instance().fileFromTitle((String)locoBox.getSelectedItem()));
        
        startProgrammer(null, re, (String)programmerBox.getSelectedItem());
    }
    
    /**
     * Start with a decoder selected, so we're going to create a new
     * RosterEntry.
     */
    protected void openNewLoco() {
        // find the decoderFile object
        DecoderFile decoderFile = DecoderIndexFile.instance().fileFromTitle(selectedDecoderType());
        if (log.isDebugEnabled()) log.debug("decoder file: "+decoderFile.getFilename());
        
        // create a dummy RosterEntry with the decoder info
        RosterEntry re = new RosterEntry();
        re.setDecoderFamily(decoderFile.getFamily());
        re.setDecoderModel(decoderFile.getModel());
        re.setId("<new loco>");
        // note that we're leaving the filename null
        // add the new roster entry to the in-memory roster
        Roster.instance().addEntry(re);
        
        startProgrammer(decoderFile, re, (String)programmerBox.getSelectedItem());
    }
    
    /** meant to be overridden to start the desired type of programmer */
    protected void startProgrammer(DecoderFile decoderFile, RosterEntry r, String progName) {
        log.error("startProgrammer method in CombinedLocoSelPane should have been overridden");
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CombinedLocoSelPane.class.getName());
    
}
