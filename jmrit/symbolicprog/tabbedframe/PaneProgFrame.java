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

import java.io.File;
import java.io.FileInputStream;
import com.sun.java.util.collections.List;

import jmri.Programmer;
import jmri.ProgListener;
import jmri.ProgModePane;
import jmri.jmrit.symbolicprog.*;

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

public class PaneProgFrame extends javax.swing.JFrame  {

	// members to contain working variable, CV values
	JLabel 				progStatus     	= new JLabel("idle");
	CvTableModel		cvModel			= new CvTableModel(progStatus);
	VariableTableModel  variableModel	= new VariableTableModel(progStatus,
														new String[]  {"Name", "Value"},
														cvModel);

	// GUI member declarations
	JTabbedPane tabPane = new JTabbedPane();

	// ctor
	public PaneProgFrame() {

		// configure GUI elements

		// general GUI config
		setTitle("Pane Programmer");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// install items in GUI
		// most of this is done from XML in readConfig() function
		getContentPane().add(tabPane);
		
		// pack  - this should be done again later after config
		pack();
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
	
	void readConfig(Element root, Namespace ns) {
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
	}
	
	public void newPane(String name, Element pane, Namespace ns) {
	
		// create a panel to add as a new tab
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		
		// add the tab to the frame
		tabPane.addTab(name, p);
		
		// handle the xml definition
		// for all "column" elements ...
		List colList = pane.getChildren("column",ns);
		for (int i=0; i<colList.size(); i++) {
			// add separators except at beginning
			if (i != 0) p.add(new JSeparator(javax.swing.SwingConstants.VERTICAL));
			// load each column
			newColumn( ((Element)(colList.get(i))), ns, p);
		}
		// add glue to the right to allow resize - but this isn't working as expected? Alignment?
		p.add(Box.createHorizontalGlue());
	}
	
	public void newColumn(Element column, Namespace ns, JComponent pane) {

		// create a panel to add as a new column
		JPanel c = new JPanel();
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

		// add to the pane
		pane.add(c);
		
		// handle the xml definition
		// for all elements in the column
		List varList = column.getChildren();
		for (int i=0; i<varList.size(); i++) {
			Element e = (Element)(varList.get(i));
			String name = e.getName();
			// decode the type
			if (name.equals("variable")) { // its a variable
				// load the variable
				newVariable( e, ns, c );
			}
			else if (name.equals("separator")) { // its a separator
				c.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
			}
			else if (name.equals("label")) { // its  a label
				c.add(new JLabel(e.getAttribute("label").getValue()));
			} else { // its a mistake
				log.error("No code to handle element of type "+e.getName());
			}
		}
		// add glue to the bottom to allow resize
		c.add(Box.createVerticalGlue());
	}
	
	public void newVariable( Element var, Namespace ns, JComponent col) {
		// create a panel to add as a new variable
		JPanel v = new JPanel();
		
		// check label orientation
		int orient = BoxLayout.X_AXIS;
		Attribute attr;
		String layout ="left";
		if ( (attr = var.getAttribute("layout")) != null 
				&& (layout = attr.getValue()) != null
				&& (layout.equals("above") || layout.equals("below"))) 
			orient = BoxLayout.Y_AXIS;		
		v.setLayout(new BoxLayout(v, orient));
		
		// get the name, load it up
		String name = var.getAttribute("name").getValue();
		
		// if label right or below, include the represenation first
		if (layout.equals("right") || layout.equals("below")) {
			addRepresentation(name, v, var, ns);
		} else {
			v.add(Box.createHorizontalGlue());  // meant to justify, but isn't working?
		}

		// load label if specified, else use name
		String label = name;
		String temp ="";
		if ( (attr = var.getAttribute("label")) != null 
				&& (temp = attr.getValue()) != null
				&& !(temp.equals("")))   // "" is the default
			label = temp;
		v.add(new JLabel(label));

		// otherwise, include the representation here
		if (! (layout.equals("right") || layout.equals("below")) ) {
			addRepresentation(name, v, var, ns);
		} else {
			v.add(Box.createHorizontalGlue());  // meant to justify, but isn't working?
		}
		
		// add to column
		col.add(v);
		
	}

	public void addRepresentation(String name, JComponent v, Element var, Namespace ns) {
		int i = findVarIndex(name);

		String format = "default";
		Attribute attr;
		if ( (attr = var.getAttribute("format")) != null && attr.getValue() != null) format = attr.getValue();

		if (i>= 0) {
			JComponent rep = getRep(i, format);
			rep.setMaximumSize(rep.getPreferredSize());
			v.add(rep);
			// set tooltip if specified
			if ( (attr = var.getAttribute("tooltip")) != null && attr.getValue() != null) rep.setToolTipText(attr.getValue());
		}
	}

	public void loadVariables(Element decoder, Namespace ns) {
		DecoderFile.loadVariableModel(decoder, ns, variableModel);
	}
	
	JComponent getRep(int i, String format) {
		return (JComponent)(variableModel.getRep(i, format));
	}
	
	int findVarIndex(String name) {
		for (int i=0; i<variableModel.getRowCount(); i++) 
			if (name.equals(variableModel.getName(i))) return i;
		return -1;
	}
			
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgFrame.class.getName());

}
