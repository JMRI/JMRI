/** 
 * SymbolicProgFrame.java
 *
 * Description:		Frame providing a command station programmer from decoder definition files
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package symbolicprog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import com.sun.java.util.collections.List;

import jmri.Programmer;
import jmri.ProgListener;

import ErrLoggerJ.ErrLog;

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
	javax.swing.JButton selectFileButton = new javax.swing.JButton();

	VariableTableModel		variableModel	= new VariableTableModel();
	JTable					variableTable	= new JTable(variableModel);
	JScrollPane 			variableScroll	= new JScrollPane(variableTable);
		
	// member to find and remember the configuration file
	final JFileChooser fc = new JFileChooser("xml");

	// ctor
	public SymbolicProgFrame() {

		// configure GUI elements
		selectFileButton.setText("Select File");
		selectFileButton.setVisible(true);
		selectFileButton.setToolTipText("Press to select configuration file");

		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
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
		getContentPane().add(selectFileButton);  
		getContentPane().add(variableScroll);

		pack();
	}
  	
  	void selectFileButtonActionPerformed(java.awt.event.ActionEvent e) {
		// show dialog
		int retVal = fc.showOpenDialog(this);

		// handle selection or cancel
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			System.out.println("File found: "+file);
			// handle the file (later should be outside this thread?)
			readAndParseDecoderConfig(file);
			System.out.println("Parsing done!");

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
			SAXBuilder builder = new SAXBuilder(false);
			doc = builder.build(file);
			
			// find root
			root = doc.getRootElement();
		
			// find decoder id, assuming first decoder is fine for now (e.g. one per file)
			Element decoderID = root.getChild("decoder",ns).getChild("id",ns);
			
			// print attributes of ID
			System.out.println("Model: "+decoderID.getAttribute("model").getValue());
			
			// start loading variables to table
			List varList = root.getChild("decoder", ns).getChild("variables",ns).getChildren("variable",ns);
			variableModel.setNumRows(varList.size());
			for (int i=0; i<varList.size(); i++) {
				// load each row
				variableModel.setRow(i, (Element)(varList.get(i)), ns);
				}
			variableModel.configDone();
			
		} catch (Exception e) {
			System.out.println("readAndParseDecoderConfig exception: "+e);
		}

	}
}
