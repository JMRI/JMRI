package jmri.jmrit.throttle;

import jmri.configurexml.StoreXmlConfigAction;
import jmri.jmrit.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import java.util.*;

import org.jdom.*;
import org.jdom.output.*;

/**
 * Save throttles to XML
 *
 * @author			Glen Oberhauser
 * @author Daniel Boudreau (C) Copyright 2008
 * @version     $Revision: 1.19 $
 */
public class StoreXmlThrottleAction extends AbstractAction {

	ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.throttle.ThrottleBundle");

	/**
	 * Constructor
	 * @param s Name for the action.
	 */
	public StoreXmlThrottleAction(String s) {
		super(s);
		// disable this ourselves if there is no throttle Manager
		if (jmri.InstanceManager.throttleManagerInstance() == null) {
			setEnabled(false);
		}
	}

	/**
	 * The action is performed. Let the user choose the file to save to.
	 * Write XML for each ThrottleFrame.
	 * @param e The event causing the action.
	 */
	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = jmri.jmrit.XmlFile.userFileChooser(rb
				.getString("PromptXmlFileTypes"), "xml");
		fileChooser.setCurrentDirectory(new File(defaultThrottleDirectory()));
		java.io.File file = StoreXmlConfigAction.getFileName(fileChooser);
		if (file == null)
			return;

		try {
			Element root = new Element("throttle-config");
			Document doc = XmlFile.newDocument(root, XmlFile.dtdLocation
					+ "throttle-config.dtd");

			// add XSLT processing instruction
			// <?xml-stylesheet type="text/xsl" href="XSLT/throttle.xsl"?>
			java.util.Map<String,String> m = new java.util.HashMap<String,String>();
			m.put("type", "text/xsl");
			m.put("href", jmri.jmrit.XmlFile.xsltLocation + "throttle.xsl");
			ProcessingInstruction p = new ProcessingInstruction(
					"xml-stylesheet", m);
			doc.addContent(0, p);

			java.util.ArrayList<Element> children = new java.util.ArrayList<Element>(5);
			for (Iterator<ThrottleFrame> i = ThrottleFrameManager.instance()
					.getThrottleFrames(); i.hasNext();) {
				ThrottleFrame f = i.next();
				Element throttleElement = f.getXml();
				children.add(throttleElement);
			}
			root.setContent(children);

			FileOutputStream o = new java.io.FileOutputStream(file);
			XMLOutputter fmt = new XMLOutputter();
			fmt.setFormat(org.jdom.output.Format.getPrettyFormat());
			fmt.output(doc, o);
			o.close();

		} catch (FileNotFoundException ex) {
			log.warn("Exception in storing throttle xml: " + ex);
		} catch (IOException ex) {
			log.warn("Exception in storing throttle xml: " + ex);
		}
	}

	public static String defaultThrottleDirectory() {
		return XmlFile.prefsDir() + "throttles";
	}

	// initialize logging
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(StoreXmlThrottleAction.class.getName());

}
