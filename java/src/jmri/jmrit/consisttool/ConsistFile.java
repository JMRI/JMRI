package jmri.jmrit.consisttool;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.*;

import jmri.Consist;
import jmri.ConsistManager;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.util.FileUtil;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.jdom2.filter.ElementFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle saving/restoring consist information to XML files. This class
 * manipulates files conforming to the consist-roster-config DTD.
 *
 * @author Paul Bender Copyright (C) 2008
 */
public class ConsistFile extends XmlFile implements PropertyChangeListener {

    private static final String CONSISTNUMBER = "consistNumber"; //NOI18N
    private static final String LONGADDRESS = "longAddress"; //NOI18N
    private static final String CONSIST = "consist"; //NOI18N
    private static final String LOCODIR = "locoDir"; // NOI18N
    private static final String LOCONAME = "locoName"; //NOI18N
    private static final String LOCOROSTERID = "locoRosterId"; // NOI18N
    private static final String NORMAL = "normal"; //NOI18N
    private static final String REVERSE = "reverse"; // NOI18N

    protected ConsistManager consistMan = null;

    public ConsistFile() {
        super();
        consistMan = InstanceManager.getDefault(jmri.ConsistManager.class);
        Roster.getDefault().addPropertyChangeListener(this);
    }

    /**
     * Load a Consist from the consist elements in the file.
     *
     * @param consist a JDOM element containing a consist
     */
    private void consistFromXml(Element consist) {
        Attribute cnumber;
        Attribute isCLong;
        Consist newConsist;

        // Read the consist address from the file and create the
        // consisit in memory if it doesn't exist already.
        cnumber = consist.getAttribute(CONSISTNUMBER);
        isCLong = consist.getAttribute(LONGADDRESS);
        DccLocoAddress consistAddress;
        if (isCLong != null) {
            log.debug("adding consist {} with longAddress set to {}.", cnumber, isCLong.getValue());
            try {
                int number = Integer.parseInt(cnumber.getValue());
                consistAddress = new DccLocoAddress(number, isCLong.getValue().equals("yes"));
            } catch (NumberFormatException e) {
                log.debug("Consist number not an integer");
                return;
            }

        } else {
            log.debug("adding consist {} with default long address setting.", cnumber);
            consistAddress = new DccLocoAddress(Integer.parseInt(cnumber.getValue()), false);
        }
        newConsist = consistMan.getConsist(consistAddress);
        if (!(newConsist.getConsistList().isEmpty())) {
            log.debug("Consist {} is not empty.  Using version in memory.", consistAddress);
            return;
        }

        readConsistType(consist, newConsist);
        readConsistId(consist, newConsist);
        readConsistLocoList(consist,newConsist);

    }

    public void readConsistLocoList(Element consist,Consist newConsist) {
        // read each child of locomotive in the consist from the file
        // and restore it's information to memory.
        Iterator<Element> childIterator = consist.getDescendants(new ElementFilter("loco"));
        try {
            Element e;
            do {
                e = childIterator.next();
                Attribute number = e.getAttribute(CONSISTNUMBER);
                log.debug("adding Loco {}", number);
                DccLocoAddress address = readLocoAddress(e);

                Attribute direction = e.getAttribute(LOCODIR);
                boolean directionNormal = false;
                if (direction != null) {
                    // use the values from the file
                    log.debug("using direction from file {}", direction.getValue());
                    directionNormal = direction.getValue().equals(NORMAL);
                } else {
                    // use default, normal direction
                    directionNormal = true;
                }
                // Use restore so we DO NOT cause send any commands
                // to the command station as we recreate the consist.
                newConsist.restore(address,directionNormal);
                readLocoPosition(e,address,newConsist);
                Attribute rosterId = e.getAttribute(LOCOROSTERID);
                if (rosterId != null) {
                    newConsist.setRosterId(address, rosterId.getValue());
                }
            } while (true);
        } catch (NoSuchElementException nse) {
            log.debug("end of loco list");
        }
    }

    private void readConsistType(Element consist,Consist newConsist){
        // read and set the consist type
        Attribute type = consist.getAttribute("type");
        if (type != null) {
            // use the value read from the file
            newConsist.setConsistType((type.getValue().equals("CSAC")) ? Consist.CS_CONSIST : Consist.ADVANCED_CONSIST);
        } else {
            // use the default (DAC)
            newConsist.setConsistType(Consist.ADVANCED_CONSIST);
        }
    }

    private void readConsistId(Element consist,Consist newConsist){
        // Read the consist ID from the file
        Attribute cID = consist.getAttribute("id");
        if (cID != null) {
            // use the value read from the file
            newConsist.setConsistID(cID.getValue());
        }
    }

    private void readLocoPosition(Element loco,DccLocoAddress address, Consist newConsist){
        Attribute position = loco.getAttribute(LOCONAME);
        if (position != null && !position.getValue().equals("mid")) {
            if (position.getValue().equals("lead")) {
                newConsist.setPosition(address, Consist.POSITION_LEAD);
            } else if (position.getValue().equals("rear")) {
                newConsist.setPosition(address, Consist.POSITION_TRAIL);
            }
        } else {
            Attribute midNumber = loco.getAttribute("locoMidNumber");
            if (midNumber != null) {
                int pos = Integer.parseInt(midNumber.getValue());
                newConsist.setPosition(address, pos);
            }
        }
    }

    private DccLocoAddress readLocoAddress(Element loco){
        DccLocoAddress address;
        Attribute number = loco.getAttribute(CONSISTNUMBER);
        Attribute isLong = loco.getAttribute(LONGADDRESS);
        if (isLong != null ) {
            // use the values from the file
            address = new DccLocoAddress(
                    Integer.parseInt(number.getValue()),
                    isLong.getValue().equals("yes"));
        } else {
            // set as long address
            address = new DccLocoAddress(
                    Integer.parseInt(number.getValue()),
                    true);
        }

        return address;
    }

    /**
     * convert a Consist to XML.
     *
     * @param consist a Consist object to write to the file
     * @return an Element representing the consist.
     */
    private Element consistToXml(Consist consist) {
        Element e = new Element(CONSIST);
        e.setAttribute("id", consist.getConsistID());
        e.setAttribute(CONSISTNUMBER, "" + consist.getConsistAddress()
                .getNumber());
        e.setAttribute(LONGADDRESS, consist.getConsistAddress()
                .isLongAddress() ? "yes" : "no");
        e.setAttribute("type", consist.getConsistType() == Consist.ADVANCED_CONSIST ? "DAC" : "CSAC");
        ArrayList<DccLocoAddress> addressList = consist.getConsistList();

        for (int i = 0; i < addressList.size(); i++) {
            DccLocoAddress locoaddress = addressList.get(i);
            Element eng = new Element("loco");
            eng.setAttribute("dccLocoAddress", "" + locoaddress.getNumber());
            eng.setAttribute(LONGADDRESS, locoaddress.isLongAddress() ? "yes" : "no");
            eng.setAttribute(LOCODIR, consist.getLocoDirection(locoaddress) ? NORMAL : REVERSE);
            int position = consist.getPosition(locoaddress);
            switch (position) {
                case Consist.POSITION_LEAD:
                    eng.setAttribute(LOCONAME, "lead");
                    break;
                case Consist.POSITION_TRAIL:
                    eng.setAttribute(LOCONAME, "rear");
                    break;
                default:
                    eng.setAttribute(LOCONAME, "mid");
                    eng.setAttribute("locoMidNumber", "" + position);
                    break;
            }
            String rosterId = consist.getRosterId(locoaddress);
            if (rosterId != null) {
                eng.setAttribute(LOCOROSTERID, rosterId);
            }
            e.addContent(eng);
        }
        return (e);
    }

    /**
     * Read all consists from the default file name.
     *
     * @throws org.jdom2.JDOMException if unable to parse consists
     * @throws java.io.IOException     if unable to read file
     */
    public void readFile() throws JDOMException, IOException {
        readFile(defaultConsistFilename());
    }

    /**
     * Read all consists from a file.
     *
     * @param fileName path to file
     * @throws org.jdom2.JDOMException if unable to parse consists
     * @throws java.io.IOException     if unable to read file
     */
    public void readFile(String fileName) throws JDOMException, IOException {
        if (checkFile(fileName)) {
            Element root = rootFromName(fileName);
            Element roster;
            if (root == null) {
                log.warn("consist file could not be read");
                return;
            }
            roster = root.getChild("roster");
            if (roster == null) {
                log.debug("consist file does not contain a roster entry");
                return;
            }
            Iterator<Element> consistIterator = root.getDescendants(new ElementFilter(CONSIST));
            try {
                Element consist;
                do {
                    consist = consistIterator.next();
                    consistFromXml(consist);
                } while (consistIterator.hasNext());
            } catch (NoSuchElementException nde) {
                log.debug("end of consist list");
            }
        } else {
            log.info("Consist file does not exist.  One will be created if necessary.");
        }

    }

    /**
     * Write all consists to the default file name.
     *
     * @param consistList list of consist addresses
     * @throws java.io.IOException if unable to write file
     */
    public void writeFile(List<LocoAddress> consistList) throws IOException {
        writeFile(consistList, defaultConsistFilename());
    }

    /**
     * Write all consists to a file.
     *
     * @param consistList list of consist addresses
     * @param fileName    path to file
     * @throws java.io.IOException if unable to write file
     */
    public void writeFile(List<LocoAddress> consistList, String fileName) throws IOException {
        // create root element
        Element root = new Element("consist-roster-config");
        Document doc = newDocument(root, dtdLocation + "consist-roster-config.dtd");

        // add XSLT processing instruction
        Map<String, String> m = new HashMap<>();
        m.put("type", "text/xsl");
        m.put("href", xsltLocation + "consistRoster.xsl");
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0, p);

        Element roster = new Element("roster");

        for (int i = 0; i < consistList.size(); i++) {
            Consist newConsist = consistMan.getConsist(consistList.get(i));
            roster.addContent(consistToXml(newConsist));
        }
        root.addContent(roster);
        if (!checkFile(fileName)) {
            //The file does not exist, create it before writing
            File file = new File(fileName);
            // verify the directory exists.
            File parentDir = file.getParentFile();
            FileUtil.createDirectory(parentDir);
            if (!file.createNewFile()) {
                throw (new IOException());
            }
        }
        writeXML(findFile(fileName), doc);
    }

    /**
     * Returns the preferences subdirectory in which Consist Files are kept 
     * this is relative to the roster files location. 
     */
    public static String getFileLocation() {
        return Roster.getDefault().getRosterFilesLocation() + CONSIST + File.separator;
    }

    /**
     * @deprecated since 4.17.3 file location is determined by roster location.
     */
    @Deprecated
    public static void setFileLocation(String loc) {
        // this method has been deprecated
    }

    /**
     * Get the filename for the default Consist file, including location.
     *
     * @return the filename
     */
    public static String defaultConsistFilename() {
        return getFileLocation() + "consist.xml";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof Roster &&
            evt.getPropertyName().equals(RosterConfigManager.DIRECTORY)) {
            try {
                this.writeFile(consistMan.getConsistList());
            } catch (IOException ioe) {
                log.error("Unable to write consist information to new consist folder");
            }
        }
    }

    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(ConsistFile.class);
}
