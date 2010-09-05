// CarManagerXml.java

package jmri.jmrit.operations.rollingstock.cars;

import java.io.File;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;

/**
 * Loads and stores cars using xml files.  Also loads and stores
 * car road names, car types, car colors, car lengths, car owners,
 * and car kernels.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.20 $
 */
public class CarManagerXml extends XmlFile {
	
	public CarManagerXml(){
		
	}
	
	/** record the single instance **/
	private static CarManagerXml _instance = null;

	public static synchronized CarManagerXml instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("CarManagerXml creating instance");
			// create and load
			_instance = new CarManagerXml();
			try {
				_instance.readFile(defaultOperationsFilename());
			} catch (Exception e) {
				log.error("Exception during operations car file reading: "+e);
			}
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("CarManagerXml returns instance "+_instance);
		return _instance;
	}
	

	public void writeFile(String name) throws java.io.FileNotFoundException, java.io.IOException {
	        if (log.isDebugEnabled()) log.debug("writeFile "+name);
	        // This is taken in large part from "Java and XML" page 368
	        File file = findFile(name);
	        if (file == null) {
	            file = new File(name);
	        }
	        // create root element
	        Element root = new Element("operations-config");
	        Document doc = newDocument(root, dtdLocation+"operations-cars.dtd");

	        // add XSLT processing instruction
	        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
	        m.put("type", "text/xsl");
	        m.put("href", xsltLocation+"operations-cars.xsl");
	        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
	        doc.addContent(0,p);
	        
	        //Check the Comment fields for line breaks and
	        //convert them to a processor directive for storage in XML
	        CarManager manager = CarManager.instance();
	        List<String> carList = manager.getByRoadNameList();
	        
	        for (int i=0; i<carList.size(); i++){

	            //Extract the RosterEntry at this index and inspect the Comment and
	            //Decoder Comment fields to change any \n characters to <?p?> processor
	            //directives so they can be stored in the xml file and converted
	            //back when the file is read
	        	Car c = manager.getById(carList.get(i));
	            String tempComment = c.getComment();
	            StringBuffer buf = new StringBuffer();

	            //transfer tempComment to xmlComment one character at a time, except
	            //when \n is found.  In that case, insert <?p?>
	            for (int k = 0; k < tempComment.length(); k++) {
	                if (tempComment.startsWith("\n", k)) {
	                	buf.append("<?p?>");
	                }
	                else {
	                	buf.append(tempComment.substring(k, k + 1));
	                }
	            }
	            c.setComment(buf.toString());
	        }
	        //All Comments line feeds have been changed to processor directives

	        // add top-level elements
	        root.addContent(manager.store());
	        Element values;
	        root.addContent(values = new Element("roadNames"));
	        String[]roads = CarRoads.instance().getNames();
	        for (int i=0; i<roads.length; i++){
	        	String roadNames = roads[i]+"%%";
	        	values.addContent(roadNames);
	        }
	        root.addContent(values = new Element("carTypes"));
	        String[]types = CarTypes.instance().getNames();
	        for (int i=0; i<types.length; i++){
	        	String typeNames = types[i]+"%%";
	        	values.addContent(typeNames);
	        }
	        root.addContent(values = new Element("carColors"));
	        String[]colors = CarColors.instance().getNames();
	        for (int i=0; i<colors.length; i++){
	        	String colorNames = colors[i]+"%%";
	        	values.addContent(colorNames);
	        }
	        root.addContent(values = new Element("carLengths"));
	        String[]lengths = CarLengths.instance().getNames();
	        for (int i=0; i<lengths.length; i++){
	        	String lengthNames = lengths[i]+"%%";
	        	values.addContent(lengthNames);
	        }
	        root.addContent(values = new Element("carOwners"));
	        String[]owners = CarOwners.instance().getNames();
	        for (int i=0; i<owners.length; i++){
	        	String ownerNames = owners[i]+"%%";
	        	values.addContent(ownerNames);
	        }
	        root.addContent(values = new Element("kernels"));
	        List<String> kernels = manager.getKernelNameList();
	        for (int i=0; i<kernels.size(); i++){
	        	String kernelNames = kernels.get(i)+"%%";
	        	values.addContent(kernelNames);
	        }
	        // store car loads based on car types
	        root.addContent(CarLoads.instance().store());
	        
	        root.addContent(values = new Element("cars"));
	        // add entries
	        for (int i=0; i<carList.size(); i++) {
	        	Car c = manager.getById(carList.get(i));
	            values.addContent(c.store());
	        }
	        writeXML(file, doc);

	        //Now that the roster has been rewritten in file form we need to
	        //restore the RosterEntry object to its normal \n state.

	        for (int i=0; i<carList.size(); i++){
	        	Car c = manager.getById(carList.get(i));
	            String xmlComment = c.getComment();
	            StringBuffer buf = new StringBuffer();

	            for (int k = 0; k < xmlComment.length(); k++) {
	                if (xmlComment.startsWith("<?p?>", k)) {
	                	buf.append("\n");
	                    k = k + 4;
	                }
	                else {
	                	buf.append(xmlComment.substring(k, k + 1));
	                }
	            }
	            c.setComment(buf.toString());
	        }

	        // done - car file now stored, so can't be dirty
	        setDirty(false);
	    }

	
	/**
     * Store the all of the operation car objects in the default place, including making a backup if needed
     */
    public void writeOperationsCarFile() {
    	makeBackupFile(defaultOperationsFilename());
        try {
          	 if(!checkFile(defaultOperationsFilename())){
                 //The file does not exist, create it before writing
                 java.io.File file=new java.io.File(defaultOperationsFilename());
                 java.io.File parentDir=file.getParentFile();
                 if (!parentDir.exists()){
                     if (!parentDir.mkdir())
                     	log.error("Directory wasn't created");
                  }
                  if (file.createNewFile())
                 	 log.debug("File created");
             }
        	writeFile(defaultOperationsFilename());
        } catch (Exception e) {
            log.error("Exception while writing the new operations file, may not be complete: "+e);
        }
    }
    
    /**
     * Read the contents of a roster XML file into this object. Note that this does not
     * clear any existing entries.
     */
    void readFile(String name) throws org.jdom.JDOMException, java.io.IOException {
    	// suppress rootFromName(name) warning message by checking to see if file exists
    	if (findFile(name) == null) {
    		log.debug(name + " file could not be found");
    		return;
    	}
    	// find root
        Element root = rootFromName(name);
        if (root==null) {
            log.debug(name + " file could not be read");
            return;
        }
        if (log.isDebugEnabled()) XmlFile.dumpElement(root);
        
        CarManager manager = CarManager.instance();
       	if (root.getChild("options") != null) {
    		Element e = root.getChild("options");
    		manager.options(e);
    	}
       	
        if (root.getChild("roadNames")!= null){
        	String names = root.getChildText("roadNames");
        	String[] roads = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("road names: "+names);
        	CarRoads.instance().setNames(roads);
        }
        
        if (root.getChild("carTypes")!= null){
        	String names = root.getChildText("carTypes");
        	String[] types = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("car types: "+names);
        	CarTypes.instance().setNames(types);
        }
        
        if (root.getChild("carColors")!= null){
        	String names = root.getChildText("carColors");
        	String[] colors = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("car colors: "+names);
        	CarColors.instance().setNames(colors);
        }
        
        if (root.getChild("carLengths")!= null){
        	String names = root.getChildText("carLengths");
        	String[] lengths = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("car lengths: "+names);
        	CarLengths.instance().setNames(lengths);
        }
        
        if (root.getChild("carOwners")!= null){
        	String names = root.getChildText("carOwners");
        	String[] owners = names.split("%%");
        	if (log.isDebugEnabled()) log.debug("car owners: "+names);
        	CarOwners.instance().setNames(owners);
        }
        
        if (root.getChild("kernels")!= null){
        	String names = root.getChildText("kernels");
        	if(!names.equals("")){
        		String[] kernelNames = names.split("%%");
        		if (log.isDebugEnabled()) log.debug("kernels: "+names);
        		for (int i=0; i<kernelNames.length; i++){
        			manager.newKernel(kernelNames[i]);
        		}
        	}
        }
        
        if (root.getChild("loads")!= null){
        	CarLoads.instance().load(root);
        }
         
        if (root.getChild("cars") != null) {
        	@SuppressWarnings("unchecked")
            List<Element> l = root.getChild("cars").getChildren("car");
            if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" cars");
            for (int i=0; i<l.size(); i++) {
                manager.register(new Car(l.get(i)));
            }

            List<String> carList = manager.getByIdList();
            //Scan the object to check the Comment and Decoder Comment fields for
            //any <?p?> processor directives and change them to back \n characters
            for (int i = 0; i < carList.size(); i++) {
                //Get a RosterEntry object for this index
	        	Car c = manager.getById(carList.get(i));

                //Extract the Comment field and create a new string for output
                String tempComment = c.getComment();
                String xmlComment = new String();

                //transfer tempComment to xmlComment one character at a time, except
                //when <?p?> is found.  In that case, insert a \n and skip over those
                //characters in tempComment.
                for (int k = 0; k < tempComment.length(); k++) {
                    if (tempComment.startsWith("<?p?>", k)) {
                        xmlComment = xmlComment + "\n";
                        k = k + 4;
                    }
                    else {
                        xmlComment = xmlComment + tempComment.substring(k, k + 1);
                    }
                }
                c.setComment(xmlComment);
            }
        }
        else {
            log.error("Unrecognized operations car file contents in file: "+name);
        }
    }

    private boolean dirty = false;
    public void setDirty(boolean b) {dirty = b;}
    boolean isDirty() {return dirty;}
    
    public void writeFileIfDirty(){
    	if(isDirty())
    		writeOperationsCarFile();
    }

    
    // Operation files always use the same directory
    public static String defaultOperationsFilename() { 
    	return OperationsXml.getFileLocation()+OperationsXml.getOperationsDirectoryName()+File.separator+getOperationsFileName();
    }

    public static void setOperationsFileName(String name) { OperationsFileName = name; }
    public static String getOperationsFileName(){
    	return OperationsFileName;
    }
    private static String OperationsFileName = "OperationsCarRoster.xml";

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarManagerXml.class.getName());

}
