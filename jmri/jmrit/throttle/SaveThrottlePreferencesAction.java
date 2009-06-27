package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import jmri.jmrit.XmlFile;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;


/**
 * Save the throttle preferences.
 *
 * @author			Glen Oberhauser
 * @author Daniel Boudreau (C) Copyright 2008
 * @version     $Revision: 1.4 $
 */
public class SaveThrottlePreferencesAction extends AbstractAction {

	ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.throttle.ThrottleBundle");

	/**
	 * Constructor
	 * @param s Name for the action.
	 */
	public SaveThrottlePreferencesAction(String s) {
		super(s);

		// disable the ourselves if there is no throttle Manager
		if (jmri.InstanceManager.throttleManagerInstance() == null) {
			setEnabled(false);
		}

	}

	/**
	 * The action is performed. Save ThrottleFrame.
	 * @param e The event causing the action.
	 */
	public void actionPerformed(ActionEvent e) {
		
		int throttles = ThrottleFrameManager.instance().getNumberThrottles();
		
		if (throttles == 0){
			javax.swing.JOptionPane.showMessageDialog(null, rb
					.getString("OpenOneThrottle"));
			return;
		}
		
		if (throttles > 1) {
			javax.swing.JOptionPane.showMessageDialog(null,
					java.text.MessageFormat.format(rb
							.getString("TooManyThrottles"), new Object[] { ""
							+ throttles }));
			return;
		}
		XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
		xf.makeBackupFile(getDefaultThrottleFilename());
		File file=new File(getDefaultThrottleFilename());
		try {
			//The file does not exist, create it before writing
			File parentDir=file.getParentFile();
			if(!parentDir.exists())
				parentDir.mkdir();
			file.createNewFile();
		} catch (Exception exp) {
			log.error("Exception while writing the new operations file, may not be complete: "+exp);
		}
   
		try {
			Element root = new Element("throttle-config");
			Document doc = XmlFile.newDocument(root, XmlFile.dtdLocation+"throttle-config.dtd");
			// add XSLT processing instruction
			// <?xml-stylesheet type="text/xsl" href="XSLT/throttle.xsl"?>
			java.util.Map<String,String> m = new java.util.HashMap<String,String>();
			m.put("type", "text/xsl");
			m.put("href", jmri.jmrit.XmlFile.xsltLocation+"throttle.xsl");
			ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
			doc.addContent(0,p);

			Iterator<ThrottleFrame> i = ThrottleFrameManager.instance().getThrottleFrames();
			ThrottleFrame f = i.next();
			Element throttleElement = f.getXml();
			// don't save the loco address or consist address
			throttleElement.getChild("AddressPanel").removeChild("locoaddress");
			throttleElement.getChild("AddressPanel").removeChild("locoaddress");
			root.setContent(throttleElement);
			xf.writeXML(file, doc);
		}       
		catch (FileNotFoundException ex){
			log.warn("Exception in storing throttle xml: "+ex);
		}
		catch (java.io.IOException ex){
			log.warn("Exception in storing throttle xml: "+ex);
		}
	}
	
    public static String getDefaultThrottleFilename() { return XmlFile.prefsDir()+"throttles"+File.separator+ThrottleFileName;}

    public static void setThrottlePreferencesFileName(String name) {ThrottleFileName = name; }
    private static String ThrottleFileName = "JMRI_ThrottlePreference.xml";


	// initialize logging
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(SaveThrottlePreferencesAction.class.getName());

}
