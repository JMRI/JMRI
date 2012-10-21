package jmri.jmris.json;

import java.io.File;
import java.util.ResourceBundle;

import jmri.beans.Bean;
import jmri.jmrit.XmlFile;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

public class JsonServerPreferences extends Bean {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmris.json.JsonServer");
	//  Flag that prefs have not been saved:
	private boolean isDirty = false;
	// initial defaults if prefs not found
	private int heartbeatInterval = 15000;
	private int port = 2056;
	private static Logger log = Logger.getLogger(JsonServerPreferences.class.getName());

	public JsonServerPreferences(String fileName) {
		openFile(fileName);
	}

	public JsonServerPreferences() {
	}


	public void load(Element child) {
		Attribute a;
		if ((a = child.getAttribute("heartbeatInterval")) != null) {
			try {
				this.setHeartbeatInterval(a.getIntValue());
			} catch (DataConversionException e) {
				this.setHeartbeatInterval(15000);
				log.error("Unable to read heartbeat interval. Setting to default value.", e);
			}
		}
		if ((a = child.getAttribute("port")) != null) {
			try {
				this.setPort(a.getIntValue());
			} catch (DataConversionException e) {
				this.setPort(2056);
				log.error("Unable to read port. Setting to default value.", e);
			}
		}
	}

	public boolean compareValuesDifferent(JsonServerPreferences prefs) {
		if (this.getHeartbeatInterval() != prefs.getHeartbeatInterval()) {
			return true;
		}
		if (this.getPort() != prefs.getPort()) {
			return true;
		}
		return false;
	}

	public void apply(JsonServerPreferences prefs) {
		this.setHeartbeatInterval(prefs.getHeartbeatInterval());
		this.setPort(prefs.getPort());
	}

	public Element store() {
		Element prefs = new Element("WebServerPreferences");
		prefs.setAttribute("heartbeatInterval", Integer.toString(this.getHeartbeatInterval()));
		prefs.setAttribute("port", Integer.toString(this.getPort()));
		this.setIsDirty(false);  //  Resets only when stored
		return prefs;
	}
	private String fileName;

	public final void openFile(String fileName) {
		this.fileName = fileName;
		JsonServerPreferencesXml prefsXml = new JsonServerPreferences.JsonServerPreferencesXml();
		File file = new File(this.fileName);
		Element root;
		try {
			root = prefsXml.rootFromFile(file);
		} catch (java.io.FileNotFoundException ea) {
			log.info("Could not find JSON Server preferences file.  Normal if preferences have not been saved before.");
			root = null;
		} catch (Exception eb) {
			log.error("Exception while loading JSON server preferences: " + eb);
			root = null;
		}
		if (root != null) {
			this.load(root);
		}
	}

	public void save() {
		if (this.fileName == null) {
			return;
		}

		XmlFile xmlFile = new XmlFile() {
		};
		xmlFile.makeBackupFile(this.fileName);
		File file = new File(this.fileName);
		try {
			File parentDir = file.getParentFile();
			if (!parentDir.exists()) {
				if (!parentDir.mkdir()) {
					log.warn("Could not create parent directory for prefs file :" + fileName);
					return;
				}
			}
			if (file.createNewFile()) {
				log.debug("Creating new JSON Server prefs file: " + fileName);
			}
		} catch (Exception ea) {
			log.error("Could not create JSON Server preferences file.");
		}

		try {
			xmlFile.writeXML(file, XmlFile.newDocument(store()));
		} catch (Exception eb) {
			log.warn("Exception in storing JSON Server xml: " + eb);
		}
	}

	public boolean getIsDirty() {
		return this.isDirty;
	}

	public void setIsDirty(boolean value) {
		this.isDirty = value;
	}

	public int getHeartbeatInterval() {
		return this.heartbeatInterval;
	}

	public void setHeartbeatInterval(int value) {
		this.heartbeatInterval = value;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int value) {
		this.port = value;
	}

	public static class JsonServerPreferencesXml extends XmlFile {
	}

}
