// PaneProgFrame.java

package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import java.io.*;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;

import jmri.Programmer;
import jmri.ProgListener;
import jmri.ProgModePane;
import jmri.jmrit.symbolicprog.*;
import jmri.jmrit.decoderdefn.*;
import jmri.jmrit.roster.*;
import jmri.jmrit.XmlFile;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Attribute;
import org.jdom.DocType;
import org.jdom.output.XMLOutputter;
import org.jdom.JDOMException;

/**
 * Frame providing a command station programmer from decoder definition files
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.5 $
 */
public class PaneProgFrame extends javax.swing.JFrame
							implements java.beans.PropertyChangeListener  {

	// members to contain working variable, CV values
	JLabel 				progStatus     	= new JLabel("idle");
	CvTableModel		cvModel			= new CvTableModel(progStatus);
	VariableTableModel  variableModel	= new VariableTableModel(progStatus,
														new String[]  {"Name", "Value"},
														cvModel);
	RosterEntry         _rosterEntry    = null;
	RosterEntryPane     _rPane          = null;

    jmri.ProgModePane   modePane        = new jmri.ProgModePane(BoxLayout.X_AXIS);

	List paneList = new ArrayList();

	String filename = null;

	// GUI member declarations
	JTabbedPane tabPane = new JTabbedPane();
	JToggleButton readAllButton = new JToggleButton("Read all");
	JToggleButton writeAllButton = new JToggleButton("Write all");
	JToggleButton confirmAllButton = new JToggleButton("Confirm all");

	ActionListener l1;
	ActionListener l2;

	protected void installComponents() {
		// to control size, we need to insert a single
		// JPanel, then have it laid out with BoxLayout
		JPanel pane = new JPanel();

		// general GUI config
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

		// configure GUI elements
		confirmAllButton.setEnabled(false);
		confirmAllButton.setToolTipText("disabled because not yet implemented");

		readAllButton.setToolTipText("Read current values from decoder. Warning: may take a long time!");
		readAllButton.addActionListener( l1 = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (readAllButton.isSelected()) readAll();
			}
		});
		writeAllButton.setToolTipText("Write current values to decoder");
		writeAllButton.addActionListener( l2 = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (writeAllButton.isSelected()) writeAll();
			}
		});

		// most of the GUI is done from XML in readConfig() function
		// which configures the tabPane
		pane.add(tabPane);

		// add buttons
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		bottom.add(readAllButton);
		bottom.add(confirmAllButton);
		bottom.add(writeAllButton);
		pane.add(bottom);

		pane.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
        pane.add(modePane);

		pane.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
		progStatus.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		pane.add(progStatus);

		// and put that pane into the JFrame
		getContentPane().add(pane);
	}

	public Dimension getPreferredSize() {
		Dimension screen = getMaximumSize();
		int width = Math.min(super.getPreferredSize().width, screen.width);
		int height = Math.min(super.getPreferredSize().height, screen.height);
		return new Dimension(width, height);
	}

	public Dimension getMaximumSize() {
        Dimension screen = getToolkit().getScreenSize();
        return new Dimension(screen.width, screen.height-25);
	}

	// ctors
	public PaneProgFrame() {
		super();
		installComponents();

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});

 		pack();

		if (log.isDebugEnabled()) log.debug("PaneProgFrame contructed with no args, unconstrained size is "+super.getPreferredSize()
                                    +", constrained to "+getPreferredSize());
	}

  	/**
  	 * Initialization sequence:
  	 * <UL>
  	 * <LI> If the locoFile is specified, open it
  	 * <LI> If the decoder file is specified, open and load it, otherwise
  	 *		get the decoder filename from the RosterEntry and load that.
  	 *		Note that we're assuming the roster entry has the right decoder,
  	 *		at least w.r.t. the loco file.
  	 * <LI> Fill CV values from the locoFile
  	 * <LI> Create the programmer panes
  	 * </UL>
     * @param decoderFile XML file defining the decoder contents
     * @param locoFile filename defining locomotive contents
     * @param r RosterEntry for information on this locomotive
     * @param name
     * @param file
  	 */
	public PaneProgFrame(DecoderFile decoderFile, String locoFile, RosterEntry r, String name, String file) {
		super(name);
		filename = file;
		installComponents();

		if (locoFile != null) readLocoFile(locoFile);  // read, but don't process

		if (decoderFile != null) loadDecoderFile(decoderFile);
		else					 loadDecoderFromLoco(r);

		// save default values
		saveDefaults();

		// finally fill the CV values from the specific loco file
		if (locoFile != null) loadLocoFile();

		// mark file state as consistent
		variableModel.setFileDirty(false);

		// and build the GUI
		loadProgrammerFile(r);

        // set the programming mode
        if (jmri.InstanceManager.programmerInstance() != null) {
            // go through in preference order, trying to find a mode
            // that exists in both the programmer and decoder.
            // First, get attributes. If not present, assume that
            // all modes are usable
            Element programming = null;
            boolean paged = true;
            boolean direct= true;
            boolean register= true;
            if (decoderRoot != null
                    && (programming = decoderRoot.getChild("decoder").getChild("programming"))!= null) {
                Attribute a;
                if ( (a = programming.getAttribute("paged")) != null )
                    if (a.getValue().equals("no")) paged = false;
                if ( (a = programming.getAttribute("direct")) != null )
                    if (a.getValue().equals("no")) direct = false;
                if ( (a = programming.getAttribute("register")) != null )
                    if (a.getValue().equals("no")) register = false;
            }

            jmri.Programmer p = jmri.InstanceManager.programmerInstance();
            if (p.hasMode(Programmer.PAGEMODE)&&paged)
                p.setMode(jmri.Programmer.PAGEMODE);
            else if (p.hasMode(Programmer.DIRECTBYTEMODE)&&direct)
                p.setMode(jmri.Programmer.DIRECTBYTEMODE);
            else if (p.hasMode(Programmer.REGISTERMODE)&&register)
                p.setMode(jmri.Programmer.REGISTERMODE);
            else log.warn("No acceptable mode found, leave as found");
        } else {
            log.error("Can't set programming mode, no programmer instance");
        }

		// optionally, add extra panes from the decoder file
		Attribute a;
		if ( (a = programmerRoot.getChild("programmer").getAttribute("decoderFilePanes")) != null
				&& a.getValue().equals("yes")) {
			if (decoderRoot != null) {
				List paneList = decoderRoot.getChildren("pane");
				if (log.isDebugEnabled()) log.debug("will process "+paneList.size()+" pane definitions from decoder file");
				for (int i=0; i<paneList.size(); i++) {
					// load each pane
					String pname = ((Element)(paneList.get(i))).getAttribute("name").getValue();
					newPane( pname, ((Element)(paneList.get(i))), modelElem);
				}
			}
		}

		// ensure cleanup at end
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});

		pack();

		if (log.isDebugEnabled()) log.debug("PaneProgFrame \""+name+"\" constructed for file "+locoFile
											+", unconstrained size is "+super.getPreferredSize()
                                            +", constrained to "+getPreferredSize());
	}

	Element lroot = null;

	/**
	 * Data element holding the 'model' element representing the decoder type
	 */
	Element modelElem = null;

  	protected void readLocoFile(String locoFile) {
  		if (locoFile == null) {
  			log.debug("loadLocoFile file invoked with null filename");
  			return;
  		}
		LocoFile lf = new LocoFile();  // used as a temporary
		lroot = null;
		try {
			lroot = lf.rootFromName(lf.fileLocation+File.separator+locoFile);
		} catch (Exception e) { log.error("Exception while loading loco XML file: "+locoFile+" exception: "+e); }
  	}

  	protected void loadLocoFile() {
		// load CVs from the loco file tree
		LocoFile.loadCvModel(lroot.getChild("locomotive"), cvModel);
  	}

	Element decoderRoot = null;

  	protected void loadDecoderFromLoco(RosterEntry r) {
  		// get a DecoderFile from the locomotive xml
		String decoderModel = r.getDecoderModel();
		String decoderFamily = r.getDecoderFamily();
		if (log.isDebugEnabled()) log.debug("selected loco uses decoder "+decoderFamily+" "+decoderModel);
		// locate a decoder like that.
		List l = DecoderIndexFile.instance().matchingDecoderList(null, decoderFamily, null, null, decoderModel);
		if (log.isDebugEnabled()) log.debug("found "+l.size()+" matches");
		if (l.size() == 0) {
			log.warn("Loco uses "+decoderFamily+" "+decoderModel+" decoder, but no such decoder defined");
            // fall back to use just the decoder name, not family
		    l = DecoderIndexFile.instance().matchingDecoderList(null, null, null, null, decoderModel);
		    if (log.isDebugEnabled()) log.debug("found "+l.size()+" matches without family key");
		}
		if (l.size() > 0) {
			DecoderFile d = (DecoderFile)l.get(0);
			loadDecoderFile(d);
        } else {
            log.warn("no matching \""+decoderModel+"\" decoder found for loco, no decoder info loaded");
        }
	}

  	protected void loadDecoderFile(DecoderFile df) {
  		if (df == null) {
  			log.warn("loadDecoder file invoked with null object");
  			return;
  		}

		try {
			decoderRoot = df.rootFromName(df.fileLocation+df.getFilename());
		} catch (Exception e) { log.error("Exception while loading decoder XML file: "+df.getFilename()+" exception: "+e); }
		// load variables from decoder tree
		df.loadVariableModel(decoderRoot.getChild("decoder"), variableModel);

		// save the pointer to the model element
		modelElem = df.getModelElement();
  	}

  	protected void loadProgrammerFile(RosterEntry r) {
		// Open and parse programmer file
		XmlFile pf = new XmlFile(){};  // XmlFile is abstract
		try {
			programmerRoot = pf.rootFromName(filename);

			// load programmer config from programmer tree
			readConfig(programmerRoot, r);
		}
		catch (Exception e) {log.error("exception reading programmer file: "+filename+" exception: "+e); }
  	}

  	Element programmerRoot = null;

	// handle resizing when first shown
  	private boolean mShown = false;
	public void addNotify() {
		super.addNotify();
		if (mShown)
			return;
		// resize frame to account for menubar
		JMenuBar jMenuBar = getJMenuBar();
		if (jMenuBar != null) {
			int jMenuBarHeight = jMenuBar.getPreferredSize().height;
			Dimension dimension = getSize();
			dimension.height += jMenuBarHeight;
			setSize(dimension);
		}
		mShown = true;
	}

	/**
	 * Close box has been clicked; handle check for dirty with respect to
	 * decoder or file, then close.
     * @param e Not used
	 */
	void thisWindowClosing(java.awt.event.WindowEvent e) {
		// check for various types of dirty - first table data not written back
		if (log.isDebugEnabled()) log.debug("Checking decoder dirty status. CV: "+cvModel.decoderDirty()+" variables:"+variableModel.decoderDirty());
		if (cvModel.decoderDirty() || variableModel.decoderDirty() ) {
			if (JOptionPane.showConfirmDialog(null,
		   		"Some changes have not been written to the decoder. They will be lost. Close window?",
		    	"choose one", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) return;
		    }
		if (variableModel.fileDirty() ) {
			if (JOptionPane.showConfirmDialog(null,
		    	"Some changes have not been written to a configuration file. Close window?",
		    	"choose one", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) return;
		    }
		// Check for a "<new loco>" roster entry; if found, remove it
		List l = Roster.instance().matchingList(null, null, null, null, null, null, "<new loco>");
		if (l.size() > 0 && log.isDebugEnabled()) log.debug("Removing "+l.size()+" <new loco> entries");
		while (l.size() > 0 ) {
			Roster.instance().removeEntry((RosterEntry)l.get(0));
			l = Roster.instance().matchingList(null, null, null, null, null, null, "<new loco>");
		}
		//OK, close
		setVisible(false);
		dispose();
	}

	void readConfig(Element root, RosterEntry r) {
		// check for "programmer" element at start
		Element base;
		if ( (base = root.getChild("programmer")) == null) {
			log.error("xml file top element is not programmer");
			return;
		}

		// add the Info tab
		tabPane.addTab("Roster Entry", makeInfoPane(r));

		// for all "pane" elements ...
		List paneList = base.getChildren("pane");
		if (log.isDebugEnabled()) log.debug("will process "+paneList.size()+" pane definitions");
		for (int i=0; i<paneList.size(); i++) {
			// load each pane
			String name = ((Element)(paneList.get(i))).getAttribute("name").getValue();
			newPane( name, ((Element)(paneList.get(i))), modelElem);
		}

	}

	/**
	 * reset all CV values to defaults stored earlier.  This will in turn update
	 * the variables
	 */
	protected void resetToDefaults() {
		int n = defaultCvValues.length;
		for (int i=0; i<n; i++) {
			CvValue cv = cvModel.getCvByNumber(defaultCvNumbers[i]);
			if (cv == null) log.warn("Trying to set default in CV "+defaultCvNumbers[i]
									+" but didn't find the CV object");
			else cv.setValue(defaultCvValues[i]);
		}
	}

	int defaultCvValues[] = null;
	int defaultCvNumbers[] = null;

	/**
	 * Save all CV values.  These stored values are used by
	 * resetToDefaults
	 */
	protected void saveDefaults() {
		int n = cvModel.getRowCount();
		defaultCvValues = new int[n];
		defaultCvNumbers = new int[n];

		for (int i=0; i<n; i++) {
			CvValue cv = cvModel.getCvByRow(i);
			defaultCvValues[i] = cv.getValue();
			defaultCvNumbers[i] = cv.number();
		}
	}

	protected JPanel makeInfoPane(RosterEntry r) {
		// create the identification pane (not configured by file now; maybe later?
		JPanel body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

		// add roster info
		_rPane = new RosterEntryPane(r);
		_rosterEntry = r;
		_rPane.setMaximumSize(_rPane.getPreferredSize());
		body.add(_rPane);

		// add the store button
		JButton store = new JButton("Save");
		store.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		store.addActionListener( new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				storeFile();
			}
		});

		// add the reset button
		JButton reset = new JButton(" Reset to defaults ");
		reset.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		store.setPreferredSize(reset.getPreferredSize());
		reset.addActionListener( new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				resetToDefaults();
			}
		});

		store.setPreferredSize(reset.getPreferredSize());
		body.add(store);
		body.add(reset);

		// arrange for the dcc address to be updated
		java.beans.PropertyChangeListener dccNews = new java.beans.PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent e) { updateDccAddress(); }
		};
		primaryAddr = variableModel.findVar("Short Address");
		if (primaryAddr==null) log.debug("DCC Address monitor didnt find a Short Address variable");
		else primaryAddr.addPropertyChangeListener(dccNews);
		extendAddr = variableModel.findVar("Long Address");
		if (extendAddr==null) log.debug("DCC Address monitor didnt find an Long Address variable");
		else extendAddr.addPropertyChangeListener(dccNews);
		addMode = variableModel.findVar("Address Format");
		if (addMode==null) log.debug("DCC Address monitor didnt find an Address Format variable");
		else addMode.addPropertyChangeListener(dccNews);

		return body;
	}

	// hold refs to variables to check dccAddress
	VariableValue primaryAddr = null;
	VariableValue extendAddr = null;
	VariableValue addMode = null;

	void updateDccAddress() {
		if (log.isDebugEnabled())
			log.debug("updateDccAddress: short "+(primaryAddr==null?"<null>":primaryAddr.getValueString())+
						" long "+(extendAddr==null?"<null>":extendAddr.getValueString())+
						" mode "+(addMode==null?"<null>":addMode.getValueString()));
		String newAddr = null;
		if (addMode == null || extendAddr == null || !addMode.getValueString().equals("1")) {
			// short address mode
			if (primaryAddr != null && !primaryAddr.getValueString().equals(""))
				newAddr = primaryAddr.getValueString();
		}
		else {
			// long address
			if (extendAddr != null && !extendAddr.getValueString().equals(""))
				newAddr = extendAddr.getValueString();
		}
		// update if needed
		if (newAddr!=null) _rPane.setDccAddress(newAddr);
	}

	public void newPane(String name, Element pane, Element modelElem) {

		// create a panel to hold columns
		JPanel p = new PaneProgPane(name, pane, cvModel, variableModel, modelElem);

		// add the tab to the frame
		tabPane.addTab(name, p);

		// and remember it for programming
		paneList.add(p);
	}

	/**
	 * invoked by "Read All" button, this sets in motion a
	 * continuing sequence of "read" operations on the
	 * panes. Each invocation of this method reads one [ane; completion
	 * of that request will cause it to happen again, reading the next pane, until
	 * there's nothing left to read.
	 * <P>
	 * @return true if a read has been started, false if the operation is complete.
	 */
	public boolean readAll() {
		if (log.isDebugEnabled()) log.debug("readAll starts");
		_read = true;
		for (int i=0; i<paneList.size(); i++) {
			if (log.isDebugEnabled()) log.debug("readAll calls readPane on "+i);
			_programmingPane = (PaneProgPane)paneList.get(i);
			// some programming operations are instant, so need to have listener registered at readPane
		    _programmingPane.addPropertyChangeListener(this);
			if (_programmingPane.readPane()) {
				// operation in progress, register to hear results, then stop loop
				if (log.isDebugEnabled()) log.debug("readAll expecting callback from readPane "+i);
				return true;
			}
			else
				_programmingPane.removePropertyChangeListener(this);
		}
		// nothing to program, end politely
		_programmingPane = null;
		readAllButton.setSelected(false);
		if (log.isDebugEnabled()) log.debug("readAll found nothing to do");
		return false;
	}

	/**
	 * invoked by "Write All" button, this sets in motion a
	 * continuing sequence of "write" operations on each pane.
	 * Each invocation of this method writes one pane; completion
	 * of that request will cause it to happen again, writing the next pane, until
	 * there's nothing left to write.
	 * <P>
	 * @return true if a write has been started, false if the operation is complete.
	 */
	public boolean writeAll() {
		if (log.isDebugEnabled()) log.debug("writeAll starts");
		_read = false;
		for (int i=0; i<paneList.size(); i++) {
			if (log.isDebugEnabled()) log.debug("writeAll calls writePane on "+i);
			_programmingPane = (PaneProgPane)paneList.get(i);
			// some programming operations are instant, so need to have listener registered at readPane
		    _programmingPane.addPropertyChangeListener(this);
			if (_programmingPane.writePane()) {
				// operation in progress, register to hear results, then stop loop
				if (log.isDebugEnabled()) log.debug("writeAll expecting callback from writePane "+i);
				return true;
			}
			else
				_programmingPane.removePropertyChangeListener(this);
		}
		// nothing to program, end politely
		_programmingPane = null;
		writeAllButton.setSelected(false);
		if (log.isDebugEnabled()) log.debug("writeAll found nothing to do");
		return false;
	}

	boolean _read = true;
	PaneProgPane _programmingPane = null;

	/**
	 * get notification of a variable property change in the pane, specifically "busy" going to
	 * false at the end of a programming operation
     * @param e Event, used to find source
	 */
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		// check for the right event
		if (_programmingPane == null) {
			log.warn("unexpected propertChange: "+e);
			return;
		} else if (log.isDebugEnabled()) log.debug("property changed: "+e.getPropertyName()
													+" new value: "+e.getNewValue());
		log.debug("check valid: "+(e.getSource() == _programmingPane)+" "+(!e.getPropertyName().equals("Busy"))+" "+(((Boolean)e.getNewValue()).equals(Boolean.FALSE)));
		if (e.getSource() == _programmingPane &&
				e.getPropertyName().equals("Busy") &&
				((Boolean)e.getNewValue()).equals(Boolean.FALSE) )  {

			if (log.isDebugEnabled()) log.debug("end of a programming pane operation, remove");

			// remove existing listener
			_programmingPane.removePropertyChangeListener(this);
			_programmingPane = null;
			// restart the operation
			if (_read && readAllButton.isSelected()) {
				if (log.isDebugEnabled()) log.debug("restart readAll");
				readAll();
			}
			else if (writeAllButton.isSelected()) {
				if (log.isDebugEnabled()) log.debug("restart writeAll");
				writeAll();
			}
			else if (log.isDebugEnabled()) log.debug("readAll/writeAll end because button is lifted");
		}
	}

	/**
	 * Write everything to a file.
	 */
	public void storeFile() {
		log.info("storeFile starts");

		// reload the RosterEntry
		updateDccAddress();
		_rPane.update(_rosterEntry);

		// id has to be set!
		if (_rosterEntry.getId().equals("") || _rosterEntry.getId().equals("<new loco>")) {
			log.info("storeFile without a filename; issued dialog");
			JOptionPane.showMessageDialog(this, "Please fill in the ID field first");
			return;
		}
		// if there isn't a filename, store using the id
		if (_rosterEntry.getFileName().equals("")) {
			String newFilename = _rosterEntry.getId().replace(' ','_')+".xml";
			_rosterEntry.setFileName(newFilename);
			log.debug("new filename: "+_rosterEntry.getFileName());
		}
		String filename = _rosterEntry.getFileName();

		// create a DecoderFile to represent this
		LocoFile df = new LocoFile();

		// do I/O
		XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+LocoFile.fileLocation);

		try {
			String fullFilename = XmlFile.prefsDir()+LocoFile.fileLocation+filename;
			File f = new File(fullFilename);
			// do backup
			df.makeBackupFile(LocoFile.fileLocation+filename);

			// and finally write the file
			df.writeFile(f, cvModel, variableModel, _rosterEntry);

		} catch (Exception e) {
			log.error("error during locomotive file output: "+e);
		}

		// mark this as a success
		variableModel.setFileDirty(false);

		//and store an updated roster file
		XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
		Roster.writeRosterFile();

	}

	/**
	 * local dispose, which also invokes parent. Note that
	 * we remove the components (removeAll) before taking those
	 * apart.
	 */
	public void dispose() {

		if (log.isDebugEnabled()) log.debug("dispose local");

		// remove listeners (not much of a point, though)
		readAllButton.removeActionListener(l1);
		writeAllButton.removeActionListener(l2);
		if (_programmingPane != null) _programmingPane.removePropertyChangeListener(this);

		// dispose the list of panes
		for (int i=0; i<paneList.size(); i++) {
			PaneProgPane p = (PaneProgPane) paneList.get(i);
			p.dispose();
		}
		paneList.clear();

		// dispose of things we owned, in order of dependence
		_rPane.dispose();
		variableModel.dispose();
		cvModel.dispose();

		// remove references to everything we remember
		progStatus = null;
		cvModel = null;
		variableModel = null;
		_rosterEntry = null;
		_rPane = null;

		paneList.clear();
		paneList = null;
		_programmingPane = null;

		lroot = null;
		tabPane = null;
		readAllButton = null;
		writeAllButton = null;
		confirmAllButton = null;

		if (log.isDebugEnabled()) log.debug("dispose superclass");
		removeAll();
		super.dispose();

	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgFrame.class.getName());

}
