/** 
 * SymbolicProgFrame.java
 *
 * Description:		Frame providing a command station programmer from decoder definition files
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.symbolicprog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.File;
import com.sun.java.util.collections.List;

import jmri.Programmer;
import jmri.ProgListener;
import jmri.ProgModePane;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.DocType;
import org.jdom.output.XMLOutputter;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

public class SymbolicProgFrame extends javax.swing.JFrame implements jmri.ProgListener {

	// GUI member declarations
	JButton selectFileButton = new javax.swing.JButton();

	VariableTableModel		variableModel	= new VariableTableModel(
					new String[]  {"Name", "Value", "Range", "Read", "Write", "CV", "Mask", "Comment" });
	JTable					variableTable	= new JTable(variableModel);
	JScrollPane 			variableScroll	= new JScrollPane(variableTable);

	ProgModePane   modePane = new ProgModePane();
			
	JLabel vendor  = new JLabel("         ");
	JLabel model   = new JLabel("         ");
	
	// member to find and remember the configuration file
	final JFileChooser fc = new JFileChooser("xml");

	// ctor
	public SymbolicProgFrame() {

		// configure GUI elements
		selectFileButton.setText("Select File");
		selectFileButton.setVisible(true);
		selectFileButton.setToolTipText("Press to select configuration file");
		
		variableTable.setDefaultRenderer(JComboBox.class, new ValueRenderer());
		variableTable.setDefaultEditor(JComboBox.class, new ValueEditor());
		variableScroll.setColumnHeaderView(variableTable.getTableHeader());

		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
		// instead of forcing the columns to fill the frame (and only fill)
		variableTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		// add actions to buttons
		selectFileButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				selectFileButtonActionPerformed(e);
			}
		});
		
		// general GUI config
		setTitle("Symbolic Programmer");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// install items in GUI
		JPanel tPane3 = new JPanel();
			tPane3.add(selectFileButton);  
			tPane3.setLayout(new BoxLayout(tPane3, BoxLayout.X_AXIS));
			tPane3.add(new JLabel(" Vendor: "));
			tPane3.add(vendor);
			tPane3.add(new JLabel(" Model: "));
			tPane3.add(model);
		getContentPane().add(tPane3);

		getContentPane().add(modePane);
			
		getContentPane().add(variableScroll);
		
		// for debugging
		
		DecVariableValue v = new DecVariableValue("name", "comment", false, 81, "VVVVVVVV", null);
		getContentPane().add(v.getValue()); 

		pack();
	}
  	
  	void selectFileButtonActionPerformed(java.awt.event.ActionEvent e) {
		// show dialog
		int retVal = fc.showOpenDialog(this);

		// handle selection or cancel
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (log.isInfoEnabled()) log.info("selectFileButtonActionPerformed: located file "+file+" for XML processing");
			// handle the file (later should be outside this thread?)
			readAndParseDecoderConfig(file);
			if (log.isInfoEnabled()) log.info("selectFileButtonActionPerformed: parsing complete");

		}
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
		setVisible(false);
		modePane.done();
		dispose();
	// and disconnect from the SlotManager
	
	}

	// handle programming later
	public void programmingOpReply(int value, int status) {}
	
	// data members for XML configuration
	Document doc = null;
	Namespace ns = null;
	Element root = null;

	void readAndParseDecoderConfig(File file) {
		try {
			// This is taken in large part from "Java and XML" page 354
			
			// Open and parse file
			ns = Namespace.getNamespace("decoder",
										"http://jmri.sourceforge.net/xml/decoder");
			SAXBuilder builder = new SAXBuilder(true);  // arugment controls validation, on for now
			doc = builder.build(file);
			
			// find root
			root = doc.getRootElement();
		
			// find decoder id, assuming first decoder is fine for now (e.g. one per file)
			Element decoderID = root.getChild("decoder",ns).getChild("id",ns);
			
			// store name, type
			vendor.setText(root.getChild("decoder", ns).getChild("id",ns).getAttribute("mfg").getValue());
			model.setText(root.getChild("decoder", ns).getChild("id",ns).getAttribute("model").getValue());
			
			// start loading variables to table
			List varList = root.getChild("decoder", ns).getChild("variables",ns).getChildren("variable",ns);
			variableModel.setNumRows(varList.size());
			for (int i=0; i<varList.size(); i++) {
				// load each row
				variableModel.setRow(i, (Element)(varList.get(i)), ns);
				}
			variableModel.configDone();
			
		} catch (Exception e) {
			if (log.isInfoEnabled()) log.info("readAndParseDecoderConfig: readAndParseDecoderConfig exception: "+e);
		}

	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SymbolicProgFrame.class.getName());

}
