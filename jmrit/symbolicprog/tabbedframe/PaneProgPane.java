// PaneProgPane.java

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

import org.jdom.Element;
import org.jdom.Attribute;

/** 
 * Provides the individual panes for the TabbedPaneProgrammer.
 * Note that this is not only the panes carrying variables, but also the
 * special purpose panes for the CV table, etc.
 *<P>
 * This class implements PropertyChangeListener so that it can be notified
 * when a variable changes its busy status at the end of a programming read/write operation
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: PaneProgPane.java,v 1.18 2002-02-20 15:54:57 jacobsen Exp $
 */
public class PaneProgPane extends javax.swing.JPanel 
							implements java.beans.PropertyChangeListener  {
		
	CvTableModel _cvModel;
	VariableTableModel _varModel;
  	
  	ActionListener l1;
  	ActionListener l2;
  	ActionListener l3;
  	
  	/** 
  	 * Create a null object.  Normally only used for tests and to pre-load classes.
  	 */   	
  	public PaneProgPane() {}
  	
  	/**
  	 * Construct the Pane from the XML definition element.
  	 *
  	 * @parameter name  Name to appear on tab of pane
  	 * @parameter pane  The JDOM Element for the pane definition
   	 * @parameter cvModel Already existing TableModel containing the CV definitions
  	 * @parameter varModel Already existing TableModel containing the variable definitions
  	 * @parameter modelElem "model" element from the Decoder Index, used to check what decoder options are present.
  	 */
	public PaneProgPane(String name, Element pane, CvTableModel cvModel, VariableTableModel varModel, Element modelElem) {
	
		_cvModel = cvModel;
		_varModel = varModel;
		
		// This is a JPanel containing a JScrollPane, containing a 
		// laid-out JPanel
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		// find out whether to display "label" (false) or "item" (true)
		boolean showItem = false;
		Attribute nameFmt = pane.getAttribute("nameFmt");
		if (nameFmt!= null && nameFmt.getValue().equals("item")) {
			log.debug("Pane "+name+" will show items, not labels, from decoder file");
			showItem = true;
		}
		// put the columns left to right in a panel
		JPanel p = new JPanel();
		panelList.add(p);
		p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
				
		// handle the xml definition
		// for all "column" elements ...
		List colList = pane.getChildren("column");
		for (int i=0; i<colList.size(); i++) {
			// load each column
			p.add(newColumn( ((Element)(colList.get(i))), showItem, modelElem));
		}
		// for all "row" elements ...
		List rowList = pane.getChildren("row");
		for (int i=0; i<rowList.size(); i++) {
			// load each row
			p.add(newRow( ((Element)(rowList.get(i))), showItem, modelElem));
		}
		
		// add glue to the right to allow resize - but this isn't working as expected? Alignment?
		add(Box.createHorizontalGlue());

		add(new JScrollPane(p));
		
		// add buttons in a new panel
		JPanel bottom = new JPanel();
		panelList.add(p);
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		readButton.setToolTipText("Read current values from decoder. Warning: may take a long time!");
		readButton.addActionListener( l1 = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (readButton.isSelected()) readPane();
			}
		});
		confButton.setEnabled(false);
		confButton.setToolTipText("Not implemented yet");
		
		writeButton.setToolTipText("Write current values to decoder");
		writeButton.addActionListener( l3 = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (writeButton.isSelected()) writePane();
			}
		});
		bottom.add(readButton);
		bottom.add(confButton);
		bottom.add(writeButton);
		add(bottom);
	}
	
	List varList = new ArrayList();     // list of VariableValue objects to be programmed
	
	JToggleButton readButton = new JToggleButton("Read sheet");
	JToggleButton confButton = new JToggleButton("Confirm sheet");
	JToggleButton writeButton = new JToggleButton("Write sheet");
	
	/**
	 * invoked by "Read Pane" button, this sets in motion a 
	 * continuing sequence of "read" operations on the 
	 * variables in the Pane.  Only variables in states
	 * UNKNOWN, EDITTED, FROMFILE are read; states STORED and READ don't
	 * need to be.  Each invocation of this method reads on CV; completion
	 * of that request will cause it to happen again, reading the next CV, until
	 * there's nothing left to read.
	 * <P>
	 * Returns true is a read has been started, false if the pane is complete.
	 */
	public boolean readPane() {
		if (log.isDebugEnabled()) log.debug("readPane starts");
		readButton.setSelected(true);
		for (int i=0; i<varList.size(); i++) {
			int varNum = ((Integer)varList.get(i)).intValue();
			int vState = _varModel.getState( varNum );
			if (log.isDebugEnabled()) log.debug("readPane index "+varNum+" state "+vState);
			if (vState == VariableValue.UNKNOWN ||
			    vState == VariableValue.EDITTED ||
			    vState == VariableValue.FROMFILE )  {
										if (log.isDebugEnabled()) log.debug("start read of variable "+_varModel.getLabel(varNum));
										setBusy(true);
										if (_programmingVar != null) log.error("listener already set at read start");
			    						_programmingVar = _varModel.getVariable(varNum);
			    						_read = true;
			    						// get notified when that state changes so can repeat
			    						_programmingVar.addPropertyChangeListener(this);
			    						// and make the read request
			    						_programmingVar.read();
										if (log.isDebugEnabled()) log.debug("return from starting read");
										// the request may have instantateously been satisfied...
			    						return true;  // only make one request at a time!
			}
		}	
		// nothing to program, end politely
		if (log.isDebugEnabled()) log.debug("readPane found nothing to do");
		readButton.setSelected(false);
		setBusy(false);
		return false;	
	}

	/**
	 * invoked by "Write Pane" button, this sets in motion a 
	 * continuing sequence of "write" operations on the 
	 * variables in the Pane.  Only variables in states
	 * UNKNOWN, EDITTED, FROMFILE are read; states STORED and READ don't
	 * need to be.  Each invocation of this method writes one CV; completion
	 * of that request will cause it to happen again, writing the next CV, until
	 * there's nothing left to write.
	 * <P>
	 * Returns true is a write has been started, false if the pane is complete.
	 */
	public boolean writePane() {
		if (log.isDebugEnabled()) log.debug("writePane starts");
		writeButton.setSelected(true);
		for (int i=0; i<varList.size(); i++) {
			int varNum = ((Integer)varList.get(i)).intValue();
			int vState = _varModel.getState( varNum );
			if (log.isDebugEnabled()) log.debug("writePane index "+varNum+" state "+vState);
			if (vState == VariableValue.UNKNOWN ||
			    vState == VariableValue.EDITTED ||
			    vState == VariableValue.FROMFILE )  {
										if (log.isDebugEnabled()) log.debug("start write of variable "+_varModel.getLabel(varNum));
										setBusy(true);
										if (_programmingVar != null) log.error("listener already set at write start");
			    						_programmingVar = _varModel.getVariable(varNum);
			    						_read = false;
			    						// get notified when that state changes so can repeat
			    						_programmingVar.addPropertyChangeListener(this);
			    						// and make the read request
			    						_programmingVar.write();
										if (log.isDebugEnabled()) log.debug("return from starting write");
			    						return true;  // only make one request at a time!
			}
		}	
		// nothing to program, end politely
		if (log.isDebugEnabled()) log.debug("writePane found nothing to do");
		writeButton.setSelected(false);
		setBusy(false);
		return false;	
	}
	
	// reference to variable being programmed (or null if none)
	VariableValue _programmingVar = null;
	boolean _read = true;
		
	// busy during read, write operations
	private boolean _busy = false;
	public boolean isBusy() { return _busy; }
	protected void setBusy(boolean busy) {
		boolean oldBusy = _busy;
		_busy = busy;
		if (oldBusy != busy) prop.firePropertyChange("Busy", new Boolean(oldBusy), new Boolean(busy));
	}

	/** 
	 * Get notification of a variable property change, specifically "busy" going to 
	 * false at the end of a programming operation. If we're in a programming
	 * operation, we then continue it by reinvoking the readPane/writePane operation.
	 */
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		// check for the right event
		if (_programmingVar == null) {
			log.warn("unexpected propertChange: "+e);
			return;
		} else if (log.isDebugEnabled()) log.debug("property changed: "+e.getPropertyName()
													+" new value: "+e.getNewValue());
		if (e.getSource() != _programmingVar ||
			!e.getPropertyName().equals("Busy") ||
			!((Boolean)e.getNewValue()).equals(Boolean.FALSE) )  { 
				if (log.isDebugEnabled() && e.getPropertyName().equals("Busy")) 
					log.debug("ignoring change of Busy "+((Boolean)e.getNewValue())
								+" "+( ((Boolean)e.getNewValue()).equals(Boolean.FALSE)));
				return;
		}
			
		if (log.isDebugEnabled()) log.debug("correct event, restart operation");
		// remove existing listener
		_programmingVar.removePropertyChangeListener(this);
		_programmingVar = null;
		// restart the operation
		if (_read && readButton.isSelected()) readPane();
		else if (writeButton.isSelected()) writePane();
		else if (log.isDebugEnabled()) log.debug("No operation to restart");
	}
		
	/**
	 * Create a single column from the JDOM column Element
	 */
	public JPanel newColumn(Element element, boolean showStdName, Element modelElem) {

		// create a panel to add as a new column or row
		JPanel c = new JPanel();
		panelList.add(c);
		GridBagLayout g = new GridBagLayout();
		GridBagConstraints cs = new GridBagConstraints();
		c.setLayout(g);
		
		// handle the xml definition
		// for all elements in the column or row
		List elemList = element.getChildren();
		if (log.isDebugEnabled()) log.debug("newColumn starting with "+elemList.size()+" elements");
		for (int i=0; i<elemList.size(); i++) {
			
			// update the grid position
			cs.gridy++;
			cs.gridx = 0;
				
			Element e = (Element)(elemList.get(i));
			String name = e.getName();
			if (log.isDebugEnabled()) log.debug("newColumn processing "+name+" element");
			// decode the type
			if (name.equals("display")) { // its a variable
				// load the variable
				newVariable( e, c, g, cs, showStdName);
			}
			else if (name.equals("separator")) { // its a separator
				JSeparator j = new JSeparator(javax.swing.SwingConstants.HORIZONTAL);
				cs.fill = GridBagConstraints.BOTH;
				cs.gridwidth = GridBagConstraints.REMAINDER;
				g.setConstraints(j, cs);
				c.add(j);
				cs.fill = GridBagConstraints.NONE;
				cs.gridwidth = 1;
			}
			else if (name.equals("label")) { // its  a label
				JLabel l = new JLabel(e.getAttribute("label").getValue());
				l.setAlignmentX(1.0f);
				cs.gridwidth = GridBagConstraints.REMAINDER;
				g.setConstraints(l, cs);
				c.add(l);
				cs.gridwidth = 1;
			}
			else if (name.equals("cvtable")) {
				log.debug("starting to build CvTable pane");
				// this is copied from SymbolicProgFrame
				JTable			cvTable		= new JTable(_cvModel);
				JScrollPane 	cvScroll	= new JScrollPane(cvTable);
				cvTable.setDefaultRenderer(JTextField.class, new ValueRenderer());
				cvTable.setDefaultRenderer(JButton.class, new ValueRenderer());
				cvTable.setDefaultEditor(JTextField.class, new ValueEditor());
				cvTable.setDefaultEditor(JButton.class, new ValueEditor());
				cvTable.setRowHeight(new JButton("X").getPreferredSize().height);
				cvScroll.setColumnHeaderView(cvTable.getTableHeader());
				// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
				// instead of forcing the columns to fill the frame (and only fill)
				cvTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				cs.gridwidth = GridBagConstraints.REMAINDER;
				g.setConstraints(cvScroll, cs);
				c.add(cvScroll);
				cs.gridwidth = 1;
				log.debug("end of building CvTable pane");
				
			}
			else if (name.equals("fnmapping")) {
				FnMapPanel l = new FnMapPanel(_varModel, varList, modelElem);
				fnMapList.add(l); // remember for deletion
				cs.gridwidth = GridBagConstraints.REMAINDER;
				g.setConstraints(l, cs);
				c.add(l);
				cs.gridwidth = 1;
			} 
			else if (name.equals("dccaddress")) {
				JPanel l = new DccAddressPanel(_varModel);
				panelList.add(l);			
				cs.gridwidth = GridBagConstraints.REMAINDER;
				g.setConstraints(l, cs);
				c.add(l);
				cs.gridwidth = 1;
				// make sure this will get read/written, even if real vars not on pane
				int iVar;
				iVar = _varModel.findVarIndex("Address Format");
				if (iVar>=0) varList.add(new Integer(iVar));
				iVar = _varModel.findVarIndex("Primary Address");
				if (iVar>=0) varList.add(new Integer(iVar));
				iVar = _varModel.findVarIndex("Extended Address");
				if (iVar>=0) varList.add(new Integer(iVar));
			} 
			else if (name.equals("row")) {
				// nested "row" elements ...
				cs.gridwidth = GridBagConstraints.REMAINDER;
				JPanel l = newRow(e, showStdName, modelElem);
				panelList.add(l);			
				g.setConstraints(l, cs);
				c.add(l);
				cs.gridwidth = 1;
			}
			else { // its a mistake
				log.error("No code to handle element of type "+e.getName()+" in newColumn");
			}
		}
		// add glue to the bottom to allow resize
		c.add(Box.createVerticalGlue());
		
		return c;
	}
	
	/**
	 * Create a single row from the JDOM column Element
	 */
	public JPanel newRow(Element element, boolean showStdName, Element modelElem) {

		// create a panel to add as a new column or row
		JPanel c = new JPanel();
		panelList.add(c);			
		GridBagLayout g = new GridBagLayout();
		GridBagConstraints cs = new GridBagConstraints();
		c.setLayout(g);
		
		// handle the xml definition
		// for all elements in the column or row
		List elemList = element.getChildren();
		if (log.isDebugEnabled()) log.debug("newRow starting with "+elemList.size()+" elements");
		for (int i=0; i<elemList.size(); i++) {

			// update the grid position
			cs.gridy = 0;
			cs.gridx++;
				
			Element e = (Element)(elemList.get(i));
			String name = e.getName();
			if (log.isDebugEnabled()) log.debug("newRow processing "+name+" element");
			// decode the type
			if (name.equals("display")) { // its a variable
				// load the variable
				newVariable( e, c, g, cs, showStdName);
			}
			else if (name.equals("separator")) { // its a separator
				JSeparator j = new JSeparator(javax.swing.SwingConstants.VERTICAL);
				cs.fill = GridBagConstraints.BOTH;
				cs.gridheight = GridBagConstraints.REMAINDER;
				g.setConstraints(j, cs);
				c.add(j);
				cs.fill = GridBagConstraints.NONE;
				cs.gridheight = 1;
			}
			else if (name.equals("label")) { // its  a label
				JLabel l = new JLabel(e.getAttribute("label").getValue());
				l.setAlignmentX(1.0f);
				cs.gridheight = GridBagConstraints.REMAINDER;
				g.setConstraints(l, cs);
				c.add(l);
				cs.gridheight = 1;
			}
			else if (name.equals("cvtable")) {
				log.debug("starting to build CvTable pane");
				// this is copied from SymbolicProgFrame
				JTable			cvTable		= new JTable(_cvModel);
				JScrollPane 	cvScroll	= new JScrollPane(cvTable);
				cvTable.setDefaultRenderer(JTextField.class, new ValueRenderer());
				cvTable.setDefaultRenderer(JButton.class, new ValueRenderer());
				cvTable.setDefaultEditor(JTextField.class, new ValueEditor());
				cvTable.setDefaultEditor(JButton.class, new ValueEditor());
				cvTable.setRowHeight(new JButton("X").getPreferredSize().height);
				cvScroll.setColumnHeaderView(cvTable.getTableHeader());
				// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
				// instead of forcing the columns to fill the frame (and only fill)
				cvTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

				cs.gridheight = GridBagConstraints.REMAINDER;
				g.setConstraints(cvScroll, cs);
				c.add(cvScroll);
				cs.gridheight = 1;
				log.debug("end of building CvTable pane");
				
			}
			else if (name.equals("fnmapping")) {
				FnMapPanel l = new FnMapPanel(_varModel, varList, modelElem);
				fnMapList.add(l); // remember for deletion
				cs.gridheight = GridBagConstraints.REMAINDER;
				g.setConstraints(l, cs);
				c.add(l);
				cs.gridheight = 1;
			} 
			else if (name.equals("dccaddress")) {
				JPanel l = new DccAddressPanel(_varModel);			
				panelList.add(l);			
				cs.gridheight = GridBagConstraints.REMAINDER;
				g.setConstraints(l, cs);
				c.add(l);
				cs.gridheight = 1;
				// make sure this will get read/written, even if real vars not on pane
				int iVar;
				iVar = _varModel.findVarIndex("Address Format");
				if (iVar>=0) varList.add(new Integer(iVar));
				iVar = _varModel.findVarIndex("Primary Address");
				if (iVar>=0) varList.add(new Integer(iVar));
				iVar = _varModel.findVarIndex("Extended Address");
				if (iVar>=0) varList.add(new Integer(iVar));
			} 
			else if (name.equals("column")) {
				// nested "column" elements ...
				cs.gridheight = GridBagConstraints.REMAINDER;
				JPanel l = newColumn(e, showStdName, modelElem);
				panelList.add(l);			
				g.setConstraints(l, cs);
				c.add(l);
				cs.gridheight = 1;
			}
			else { // its a mistake
				log.error("No code to handle element of type "+e.getName()+" in newRow");
			}
		}
		// add glue to the bottom to allow resize
		c.add(Box.createVerticalGlue());
		
		return c;
	}
	
	/**
	 * Add the representation of a single variable.  The 
	 * variable is defined by a JDOM variable Element from the XML file.
	 */
	public void newVariable( Element var, JComponent col, 
							GridBagLayout g, GridBagConstraints cs, boolean showStdName) {

		// get the name
		String name = var.getAttribute("item").getValue();

		// if it doesn't exist, do nothing
		int i = _varModel.findVarIndex(name);
		if (i<0) {
			if (log.isDebugEnabled()) log.debug("Variable \""+name+"\" not found, omitted");
			return;
		}
		
		// check label orientation
		Attribute attr;
		String layout ="left";  // this default is also set in the DTD
		if ( (attr = var.getAttribute("layout")) != null && attr.getValue() != null)
				layout = attr.getValue(); 
		
		// load label if specified, else use name
		String label = name;
		if (!showStdName) {
			// get name attribute from variable, as that's the mfg name
			label = _varModel.getLabel(i);
		}
		String temp ="";
		if ( (attr = var.getAttribute("label")) != null 
				&& (temp = attr.getValue()) != null )
			label = temp;
		JLabel l = new JLabel(" "+label+" ");

		// get representation; store into the list to be programmed
		JComponent rep = getRepresentation(name, var);
		if (i>=0) varList.add(new Integer(i));
		
		// now handle the four orientations
		// assemble v from label, rep
		
		if (layout.equals("left")) {
			cs.anchor= GridBagConstraints.EAST;
			g.setConstraints(l, cs);
			col.add(l);

			cs.gridx = GridBagConstraints.RELATIVE;
			cs.anchor= GridBagConstraints.WEST;
			g.setConstraints(rep, cs);
			col.add(rep);
			
		} else if (layout.equals("right")) {
			cs.anchor= GridBagConstraints.EAST;
			g.setConstraints(rep, cs);
			col.add(rep);

			cs.gridx = GridBagConstraints.RELATIVE;
			cs.anchor= GridBagConstraints.WEST;
			g.setConstraints(l, cs);
			col.add(l);
			
		} else if (layout.equals("below")) {
			// variable in center of upper line
			cs.anchor=GridBagConstraints.CENTER;
			g.setConstraints(rep, cs);
			col.add(rep);
			
			// label aligned like others
			cs.gridy++;
			cs.anchor= GridBagConstraints.WEST;
			g.setConstraints(l, cs);
			col.add(l);
			
		} else if (layout.equals("above")) {
			// label aligned like others
			cs.anchor= GridBagConstraints.WEST;
			g.setConstraints(l, cs);
			col.add(l);
			
			// variable in center of lower line
			cs.gridy++;
			cs.anchor=GridBagConstraints.CENTER;
			g.setConstraints(rep, cs);
			col.add(rep);
			
		} else {
			log.error("layout internally inconsistent: "+layout);
			return;
		}
	}

	public JComponent getRepresentation(String name, Element var) {
		int i = _varModel.findVarIndex(name);
		JComponent rep = null;
		String format = "default";
		Attribute attr;
		if ( (attr = var.getAttribute("format")) != null && attr.getValue() != null) format = attr.getValue();

		if (i>= 0) {
			rep = getRep(i, format);
			rep.setMaximumSize(rep.getPreferredSize());
			// set tooltip if specified
			if ( (attr = var.getAttribute("tooltip")) != null && attr.getValue() != null) 
				rep.setToolTipText(attr.getValue());
		}
		return rep;
	}

	JComponent getRep(int i, String format) {
		return (JComponent)(_varModel.getRep(i, format));
	}
	
	/** list of fnMapping objects to dispose */
	ArrayList fnMapList = new ArrayList();
	/** list of JPanel objects to removeAll */
	ArrayList panelList = new ArrayList();
	
	public void dispose() {
		if (log.isDebugEnabled()) log.debug("dispose");

		// remove components
		removeAll();

		readButton.removeActionListener(l1);
		writeButton.removeActionListener(l3);
		
		if (_programmingVar != null) _programmingVar.removePropertyChangeListener(this);

		prop = null;
		_programmingVar = null;
		
		varList.clear();
		varList = null;

		// dispose of any fnMaps
		for (int i=0; i<panelList.size(); i++) {
			((JPanel)(panelList.get(i))).removeAll();
		}
		panelList.clear();
		panelList = null;

		// dispose of any fnMaps
		for (int i=0; i<fnMapList.size(); i++) {
			((FnMapPanel)(fnMapList.get(i))).dispose();
		}
		fnMapList.clear();
		fnMapList = null;
		
		readButton = null;
		confButton = null;
		writeButton = null;
		// these two are disposed elsewhere
		_cvModel = null;
		_varModel = null;
		
	}
	
	// handle outgoing parameter notification for the Busy parameter
	java.beans.PropertyChangeSupport prop = new java.beans.PropertyChangeSupport(this);	
	public void removePropertyChangeListener(java.beans.PropertyChangeListener p) { prop.removePropertyChangeListener(p); }
	public void addPropertyChangeListener(java.beans.PropertyChangeListener p) { prop.addPropertyChangeListener(p); }

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgPane.class.getName());

}
