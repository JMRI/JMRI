// CarManagerXml.java

package jmri.jmrit.operations.rollingstock.cars;

import java.io.File;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;

import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.RollingStockLogger;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.OperationsXml;

/**
 * Loads and stores cars using xml files.  Also loads and stores
 * car road names, car types, car colors, car lengths, car owners,
 * and car kernels.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision$
 */
public class CarManagerXml extends OperationsXml {
	
	public CarManagerXml(){
	}
	
	/** record the single instance **/
	private static CarManagerXml _instance = null;

	public static synchronized CarManagerXml instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("CarManagerXml creating instance");
			// create and load
			_instance = new CarManagerXml();
			_instance.load();
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
	        Element root = new Element("operations-config"); // NOI18N
	        Document doc = newDocument(root, dtdLocation+"operations-cars.dtd"); // NOI18N

	        // add XSLT processing instruction
	        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
	        m.put("type", "text/xsl"); // NOI18N
	        m.put("href", xsltLocation+"operations-cars.xsl");	// NOI18N
	        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
	        doc.addContent(0,p);
	        
	        //All Comments line feeds have been changed to processor directives
	        CarManager manager = CarManager.instance();
	        // add top-level elements
	        root.addContent(manager.store());
	        Element values;
	        root.addContent(values = new Element(Xml.ROAD_NAMES));
	        String[]roads = CarRoads.instance().getNames();
	        for (int i=0; i<roads.length; i++){
	        	String roadNames = roads[i]+"%%"; // NOI18N
	        	values.addContent(roadNames);
	        }
	        root.addContent(values = new Element(Xml.CAR_TYPES));
	        String[]types = CarTypes.instance().getNames();
	        for (int i=0; i<types.length; i++){
	        	String typeNames = types[i]+"%%"; // NOI18N
	        	values.addContent(typeNames);
	        }
	        root.addContent(values = new Element(Xml.CAR_COLORS));
	        String[]colors = CarColors.instance().getNames();
	        for (int i=0; i<colors.length; i++){
	        	String colorNames = colors[i]+"%%"; // NOI18N
	        	values.addContent(colorNames);
	        }
	        root.addContent(values = new Element(Xml.CAR_LENGTHS));
	        String[]lengths = CarLengths.instance().getNames();
	        for (int i=0; i<lengths.length; i++){
	        	String lengthNames = lengths[i]+"%%"; // NOI18N
	        	values.addContent(lengthNames);
	        }
	        root.addContent(values = new Element(Xml.CAR_OWNERS));
	        String[]owners = CarOwners.instance().getNames();
	        for (int i=0; i<owners.length; i++){
	        	String ownerNames = owners[i]+"%%"; // NOI18N
	        	values.addContent(ownerNames);
	        }
	        root.addContent(values = new Element(Xml.KERNELS));
	        List<String> kernels = manager.getKernelNameList();
	        for (int i=0; i<kernels.size(); i++){
	        	String kernelNames = kernels.get(i)+"%%"; // NOI18N
	        	values.addContent(kernelNames);
	        }
	        // store car loads based on car types
	        root.addContent(CarLoads.instance().store());
	        
	        root.addContent(values = new Element(Xml.CARS));
	        // add entries
	        List<String> carList = manager.getList();
	        for (int i=0; i<carList.size(); i++) {
	        	Car c = manager.getById(carList.get(i));
	        	c.setComment(convertToXmlComment(c.getComment()));
	            values.addContent(c.store());
	        }
	        writeXML(file, doc);

	        //Now that the roster has been rewritten in file form we need to
	        //restore the RosterEntry object to its normal \n state.

	        for (int i=0; i<carList.size(); i++){
	        	Car c = manager.getById(carList.get(i));
	        	c.setComment(convertFromXmlComment(c.getComment()));
	        }
	        // done - car file now stored, so can't be dirty
	        setDirty(false);
	    }
    
    /**
     * Read the contents of a roster XML file into this object. Note that this does not
     * clear any existing entries.
     */
    public void readFile(String name) throws org.jdom.JDOMException, java.io.IOException {
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
        
        CarManager manager = CarManager.instance();
       	if (root.getChild(Xml.OPTIONS) != null) {
    		Element e = root.getChild(Xml.OPTIONS);
    		manager.options(e);
    	}
       	
        if (root.getChild(Xml.ROAD_NAMES)!= null){
        	String names = root.getChildText(Xml.ROAD_NAMES);
        	String[] roads = names.split("%%"); // NOI18N
        	if (log.isDebugEnabled()) log.debug("road names: "+names);
        	CarRoads.instance().setNames(roads);
        }
        
        if (root.getChild(Xml.CAR_TYPES)!= null){
        	String names = root.getChildText(Xml.CAR_TYPES);
        	String[] types = names.split("%%"); // NOI18N
        	if (log.isDebugEnabled()) log.debug("car types: "+names);
        	CarTypes.instance().setNames(types);
        }
        
        if (root.getChild(Xml.CAR_COLORS)!= null){
        	String names = root.getChildText(Xml.CAR_COLORS);
        	String[] colors = names.split("%%"); // NOI18N
        	if (log.isDebugEnabled()) log.debug("car colors: "+names);
        	CarColors.instance().setNames(colors);
        }
        
        if (root.getChild(Xml.CAR_LENGTHS)!= null){
        	String names = root.getChildText(Xml.CAR_LENGTHS);
        	String[] lengths = names.split("%%"); // NOI18N
        	if (log.isDebugEnabled()) log.debug("car lengths: "+names);
        	CarLengths.instance().setNames(lengths);
        }
        
        if (root.getChild(Xml.CAR_OWNERS)!= null){
        	String names = root.getChildText(Xml.CAR_OWNERS);
        	String[] owners = names.split("%%"); // NOI18N
        	if (log.isDebugEnabled()) log.debug("car owners: "+names);
        	CarOwners.instance().setNames(owners);
        }
        
        if (root.getChild(Xml.KERNELS)!= null){
        	String names = root.getChildText(Xml.KERNELS);
        	if(!names.equals("")){
        		String[] kernelNames = names.split("%%"); // NOI18N
        		if (log.isDebugEnabled()) log.debug("kernels: "+names);
        		for (int i=0; i<kernelNames.length; i++){
        			manager.newKernel(kernelNames[i]);
        		}
        	}
        }
        
        if (root.getChild(Xml.LOADS)!= null){
        	CarLoads.instance().load(root);
        }
         
        if (root.getChild(Xml.CARS) != null) {
        	@SuppressWarnings("unchecked")
            List<Element> l = root.getChild(Xml.CARS).getChildren(Xml.CAR);
            if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" cars");
            for (int i=0; i<l.size(); i++) {
                manager.register(new Car(l.get(i)));
            }

            //Scan the object to check the Comment and Decoder Comment fields for
            //any <?p?> processor directives and change them to back \n characters
            List<String> carList = manager.getList();
            for (int i = 0; i < carList.size(); i++) {
                //Get a RosterEntry object for this index
	        	Car c = manager.getById(carList.get(i));
	        	c.setComment(convertFromXmlComment(c.getComment()));
            }
        }
        else {
        	log.error("Unrecognized operations car file contents in file: "+name);
        }
		log.debug("Cars have been loaded!");
		RollingStockLogger.instance().enableCarLogging(Setup.isCarLoggerEnabled());
		// clear dirty bit
		setDirty(false);
		// clear location dirty flag, locations get modified during the loading of cars and locos
		LocationManagerXml.instance().setDirty(false);
    }

    public void setOperationsFileName(String name) { operationsFileName = name; }
	public String getOperationsFileName(){
		return operationsFileName;
	}
    private String operationsFileName = "OperationsCarRoster.xml"; // NOI18N

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarManagerXml.class.getName());

}
