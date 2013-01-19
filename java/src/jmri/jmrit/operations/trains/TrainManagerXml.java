// TrainManagerXml.java

package jmri.jmrit.operations.trains;

import java.io.File;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.OperationsXml;
import jmri.util.FileUtil;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

/**
 * Loads and stores trains using xml files. Also stores various train parameters managed by the TrainManager.
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version $Revision$
 */
public class TrainManagerXml extends OperationsXml {

	private boolean fileLoaded = false;

	public TrainManagerXml() {
	}

	/** record the single instance **/
	private static TrainManagerXml _instance = null;

	public static synchronized TrainManagerXml instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("TrainManagerXml creating instance");
			// create and load
			_instance = new TrainManagerXml();
			_instance.load();
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("TrainManagerXml returns instance " + _instance);
		return _instance;
	}

	public void writeFile(String name) throws java.io.FileNotFoundException, java.io.IOException {
		if (log.isDebugEnabled())
			log.debug("writeFile " + name);
		// This is taken in large part from "Java and XML" page 368
		File file = findFile(name);
		if (file == null) {
			file = new File(name);
		}
		// create root element
		Element root = new Element("operations-config"); // NOI18N
		Document doc = newDocument(root, dtdLocation + "operations-trains.dtd"); // NOI18N

		// add XSLT processing instruction
		java.util.Map<String, String> m = new java.util.HashMap<String, String>();
		m.put("type", "text/xsl"); // NOI18N
		m.put("href", xsltLocation + "operations-trains.xsl"); // NOI18N
		ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
		doc.addContent(0, p);

		TrainManager.instance().store(root);
		TrainScheduleManager.instance().store(root);

		writeXML(file, doc);

		// done - train file now stored, so can't be dirty
		setDirty(false);
	}

	/**
	 * Read the contents of a roster XML file into this object. Note that this does not clear any existing entries.
	 */
	public void readFile(String name) throws org.jdom.JDOMException, java.io.IOException {

		// suppress rootFromName(name) warning message by checking to see if file exists
		if (findFile(name) == null) {
			log.debug(name + " file could not be found");
			fileLoaded = true; // set flag, could be the first time
			return;
		}
		// find root
		Element root = rootFromName(name);
		if (root == null) {
			log.debug(name + " file could not be read");
			return;
		}

		TrainManager.instance().load(root);
		TrainScheduleManager.instance().load(root);

		fileLoaded = true; // set flag trains are loaded
		
		// now load train icons on panels
		TrainManager.instance().loadTrainIcons();

		// loading complete run startup scripts
		TrainManager.instance().runStartUpScripts();

		log.debug("Trains have been loaded!");
		TrainLogger.instance().enableTrainLogging(Setup.isTrainLoggerEnabled());
		setDirty(false); // clear dirty flag
	}

	public boolean isTrainFileLoaded() {
		return fileLoaded;
	}

	/**
	 * Store the train's build report
	 */
	public File createTrainBuildReportFile(String name) {
		return createFile(defaultBuildReportFilename(name), false); // don't backup
	}

	public File getTrainBuildReportFile(String name) {
		File file = new File(defaultBuildReportFilename(name));
		return file;
	}

	public String defaultBuildReportFilename(String name) {
		return FileUtil.getUserFilesPath() + OperationsXml.getOperationsDirectoryName() + File.separator
				+ "buildstatus" + File.separator + BuildReportFileName + name + fileType;	// NOI18N
	}

	public void setBuildReportName(String name) {
		BuildReportFileName = name;
	}

	private String BuildReportFileName = Bundle.getMessage("train")+" (";
	private String ManifestFileName = Bundle.getMessage("train")+" (";
	private String fileType = ").txt";	// NOI18N
	private String fileTypeCsv = ").csv";	// NOI18N

	/**
	 * Store the train's manifest
	 */
	public File createTrainManifestFile(String name) {
		return createFile(defaultManifestFilename(name), false); // don't backup
	}

	public File getTrainManifestFile(String name) {
		File file = new File(defaultManifestFilename(name));
		return file;
	}

	public String defaultManifestFilename(String name) {
		return FileUtil.getUserFilesPath() + OperationsXml.getOperationsDirectoryName() + File.separator
				+ "manifests" + File.separator + ManifestFileName + name + fileType;// NOI18N
	}

	public File getTrainCsvManifestFile(String name) {
		File file = new File(defaultCsvManifestFilename(name));
		return file;
	}

	public File createTrainCsvManifestFile(String name) {
		return createFile(defaultCsvManifestFilename(name), false); // don't backup
	}

	public String defaultCsvManifestFilename(String name) {
		return FileUtil.getUserFilesPath() + OperationsXml.getOperationsDirectoryName() + File.separator
				+ "csvManifests" + File.separator + ManifestFileName + name + fileTypeCsv; // NOI18N
	}

	/**
	 * Store the switch list for a location
	 */
	public File createSwitchListFile(String name) {
		return createFile(defaultSwitchListName(name), false); // don't backup
	}

	public File getSwitchListFile(String name) {
		File file = new File(defaultSwitchListName(name));
		return file;
	}

	public String defaultSwitchListName(String name) {
		return FileUtil.getUserFilesPath() + OperationsXml.getOperationsDirectoryName() + File.separator
				+ "switchLists" + File.separator + SwitchListFileName + name + fileType; // NOI18N
	}

	/**
	 * Store the csv switch list for a location
	 */
	public File createCsvSwitchListFile(String name) {
		return createFile(defaultCsvSwitchListName(name), false); // don't backup
	}

	public File getCsvSwitchListFile(String name) {
		File file = new File(defaultCsvSwitchListName(name));
		return file;
	}

	public String defaultCsvSwitchListName(String name) {
		return FileUtil.getUserFilesPath() + OperationsXml.getOperationsDirectoryName() + File.separator
				+ "csvSwitchLists" + File.separator + SwitchListFileName + name + fileTypeCsv;// NOI18N
	}

	public void setTrainSwitchListName(String name) {
		SwitchListFileName = name;
	}

	private String SwitchListFileName = Bundle.getMessage("location")+" (";

	public void setOperationsFileName(String name) {
		operationsFileName = name;
	}

	public String getOperationsFileName() {
		return operationsFileName;
	}

	private String operationsFileName = "OperationsTrainRoster.xml";// NOI18N

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainManagerXml.class
			.getName());

}
