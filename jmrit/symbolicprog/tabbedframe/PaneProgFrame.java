/** 
 * PaneProgFrame.java
 *
 * Description:		Frame providing a command station programmer from decoder definition files
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Attribute;
import org.jdom.DocType;
import org.jdom.output.XMLOutputter;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

public class PaneProgFrame extends javax.swing.JFrame 
							implements java.beans.PropertyChangeListener  {

	// members to contain working variable, CV values
	JLabel 				progStatus     	= new JLabel("idle");
	CvTableModel		cvModel			= new CvTableModel(progStatus);
	VariableTableModel  variableModel	= new VariableTableModel(progStatus,
														new String[]  {"Name", "Value"},
														cvModel);
	List paneList = new ArrayList();
	
	// GUI member declarations
	JTabbedPane tabPane = new JTabbedPane();
	JButton readAll = new JButton("Read all");
	JButton writeAll = new JButton("Write all");
	JButton confirmAll = new JButton("Confirm all");
	
	protected void installComponents() {
		// configure GUI elements
		confirmAll.setEnabled(false);
		confirmAll.setToolTipText("disabled because not yet implemented");
		
		// general GUI config
		setTitle("Pane Programmer");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		readAll.addActionListener( new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				readAll();
			}
		});
		writeAll.addActionListener( new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				writeAll();
			}
		});

		// most of this is done from XML in readConfig() function
		getContentPane().add(tabPane);
		
		// add buttons
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		bottom.add(readAll);
		bottom.add(confirmAll);
		bottom.add(writeAll);
		getContentPane().add(bottom);
		
		// pack  - this should be done again later after config
		pack();
	}
	
	// ctors
	public PaneProgFrame() {
		super();
		installComponents();
	}
  		
	public PaneProgFrame(DecoderFile decoderFile, String locoFile, RosterEntry r) {
		super();
		installComponents();
		loadDecoderFile(decoderFile);
		loadLocoFile(locoFile);
		loadProgrammerFile(r);
	}
  	
  	protected void loadLocoFile(String locoFile) {
  		if (locoFile == null) {
  			log.info("loadLocoFile file invoked with null filename");
  			return;
  		}
		LocoFile lf = new LocoFile();  // used as a temporary
		Namespace lns = lf.getNamespace();
		Element lroot = null;
		try {
			lroot = lf.rootFromFile(lf.fileLocation+File.separator+locoFile, true);
		} catch (Exception e) { log.error("Exception while loading loco XML file: "+e); }
		// load CVs from the loco file tree
		LocoFile.loadCvModel(lroot.getChild("locomotive", lns), lns, cvModel);
  	}
  	
  	protected void loadDecoderFile(DecoderFile df) {
  		if (df == null) {
  			log.warn("loadDecoder file invoked with null object");
  			return;
  		}
		
		Namespace dns = df.getNamespace();
		Element droot = null;
		try {
			droot = df.rootFromFile(df.fileLocation+File.separator+df.getFilename(), true);
		} catch (Exception e) { log.error("Exception while loading decoder XML file: "+e); }
		// load variables from decoder tree
		df.loadVariableModel(droot.getChild("decoder", dns), dns, variableModel);
  	}
  	
  	protected void loadProgrammerFile(RosterEntry r) {
		// Open and parse programmer file
		File pfile = new File("xml"+File.separator+"programmers"+File.separator+"MultiPane.xml");
		Namespace pns = Namespace.getNamespace("programmer",
										"http://jmri.sourceforge.net/xml/programmer");
		SAXBuilder pbuilder = new SAXBuilder(true);  // argument controls validation, on for now
		Document pdoc = null;
		try {
			pdoc = pbuilder.build(new FileInputStream(pfile),"xml"+File.separator);
		}
		catch (Exception e) {
			log.error("Exception in programmer SAXBuilder "+e);
		}
		// find root
		Element proot = pdoc.getRootElement();
					
		// load programmer config from programmer tree
		readConfig(proot, pns, r);
  	}
  	
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

	// Close the window when the close box is clicked
	void thisWindowClosing(java.awt.event.WindowEvent e) {
		//OK, close
		setVisible(false);
		dispose();	
	}
	
	void readConfig(Element root, Namespace ns, RosterEntry r) {
		// check for "programmer" element at start
		Element base;	
		if ( (base = root.getChild("programmer", ns)) == null) {
			log.error("xml file top element is not programmer");
			return;
		}

		// for all "pane" elements ...
		List paneList = base.getChildren("pane",ns);
		for (int i=0; i<paneList.size(); i++) {
			// load each pane
			String name = ((Element)(paneList.get(i))).getAttribute("name").getValue();
			newPane( name, ((Element)(paneList.get(i))), ns);
		}
		
		// create the identification pane (not configured by file now; maybe later?
		JPanel body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		
		// add the tab to the frame
		tabPane.addTab("Info", body);

		// add roster info
		JPanel bottom = new RosterEntryPane(r);
		bottom.setMaximumSize(bottom.getPreferredSize());
		
		body.add(bottom);
	}
	
	public void newPane(String name, Element pane, Namespace ns) {
	
		// create a panel to hold columns
		JPanel p = new PaneProgPane(name, pane, ns, cvModel, variableModel);
		
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
	 * Returns true is a read has been started, false if the operation is complete.
	 */
	public boolean readAll() {
		if (log.isDebugEnabled()) log.debug("readAll starts");
		for (int i=0; i<paneList.size(); i++) {
			_programmingPane = (PaneProgPane)paneList.get(i);
			if (_programmingPane.readPane()) {
				// operation in progress, register to hear results, then stop loop
			    _programmingPane.addPropertyChangeListener(this);
				return true;
			}
		}
		// nothing to program, end politely
		_programmingPane = null;
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
	 * Returns true is a write has been started, false if the operation is complete.
	 */
	public boolean writeAll() {
		if (log.isDebugEnabled()) log.debug("writeAll starts");
		for (int i=0; i<paneList.size(); i++) {
			_programmingPane = (PaneProgPane)paneList.get(i);
			if (_programmingPane.writePane()) {
				// operation in progress, register to hear results, then stop loop
			    _programmingPane.addPropertyChangeListener(this);
				return true;
			}
		}
		// nothing to program, end politely
		_programmingPane = null;
		if (log.isDebugEnabled()) log.debug("writeAll found nothing to do");
		return false;	
	}
	
	boolean _read = true;
	PaneProgPane _programmingPane = null;
	
	/** 
	 * get notification of a variable property change in the pane, specifically "busy" going to 
	 * false at the end of a programming operation
	 */
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		// check for the right event
		if (_programmingPane == null) {
			log.warn("unexpected propertChange: "+e);
			return;
		} else if (log.isDebugEnabled()) log.debug("property changed: "+e.getPropertyName()
													+" new value: "+e.getNewValue());
		if (e.getSource() != _programmingPane ||
			!e.getPropertyName().equals("Busy") ||
			!((Boolean)e.getNewValue()).equals(Boolean.FALSE) )  { 
				if (log.isDebugEnabled() && e.getPropertyName().equals("Busy")) 
					log.debug("ignoring change of Busy "+((Boolean)e.getNewValue())
								+" "+( ((Boolean)e.getNewValue()).equals(Boolean.FALSE)));
				return;
		}
			
		if (log.isDebugEnabled()) log.debug("correct event, restart operation");
		// remove existing listener
		_programmingPane.removePropertyChangeListener(this);
		_programmingPane = null;
		// restart the operation
		if (_read) readAll();
		else writeAll();
	}
			
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgFrame.class.getName());

}
