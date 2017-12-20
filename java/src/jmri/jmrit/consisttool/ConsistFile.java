package jmri.jmrit.consisttool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import jmri.Consist;
import jmri.ConsistManager;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.Roster;
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
public class ConsistFile extends XmlFile {

    protected ConsistManager consistMan = null;

    public ConsistFile() {
        super();
        consistMan = InstanceManager.getDefault(jmri.ConsistManager.class);
        // set the location to a subdirectory of the defined roster
        // directory
        setFileLocation(Roster.getDefault().getRosterLocation() + "roster" + File.separator + "consist");
    }

    /**
     * Load a Consist from the consist elements in the file.
     *
     * @param consist a JDOM element containing a consist
     */
    @SuppressWarnings("unchecked")
    private void consistFromXml(Element consist) {
        Attribute type, cnumber, isCLong, cID;
        Consist newConsist;

        // Read the consist address from the file and create the
        // consisit in memory if it doesn't exist already.
        cnumber = consist.getAttribute("consistNumber");
        isCLong = consist.getAttribute("longAddress");
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
            consistAddress = new DccLocoAddress(
                    Integer.parseInt(cnumber.getValue()),
                    false);
        }
        newConsist = consistMan.getConsist(consistAddress);
        if (!(newConsist.getConsistList().isEmpty())) {
            log.debug("Consist {} is not empty.  Using version in memory.", consistAddress.toString());
            return;
        }

        // read and set the consist type
        type = consist.getAttribute("type");
        if (type != null) {
            // use the value read from the file
            newConsist.setConsistType((type.getValue().equals("CSAC")) ? Consist.CS_CONSIST : Consist.ADVANCED_CONSIST);
        } else {
            // use the default (DAC)
            newConsist.setConsistType(Consist.ADVANCED_CONSIST);
        }

        // Read the consist ID from the file;
        cID = consist.getAttribute("id");
        if (cID != null) {
            // use the value read from the file
            newConsist.setConsistID(cID.getValue());
        }

        // read each child of locomotive in the consist from the file
        // and restore it's information to memory.
        Iterator<Element> childIterator = consist.getDescendants(new ElementFilter("loco"));
        try {
            Element e;
            do {
                e = childIterator.next();
                Attribute number, isLong, direction, position, rosterId;
                number = e.getAttribute("dccLocoAddress");
                isLong = e.getAttribute("longAddress");
                direction = e.getAttribute("locoDir");
                position = e.getAttribute("locoName");
                rosterId = e.getAttribute("locoRosterId");
                log.debug("adding Loco {}", number);
                // Use restore so we DO NOT cause send any commands
                // to the command station as we recreate the consist.
                DccLocoAddress address;
                if (isLong != null && direction != null) {
                    // use the values from the file
                    log.debug("using direction from file {}", direction.getValue());
                    address = new DccLocoAddress(
                            Integer.parseInt(number.getValue()),
                            isLong.getValue().equals("yes"));
                    newConsist.restore(address,
                            direction.getValue().equals("normal"));
                } else if (isLong == null && direction != null) {
                    // use the direction from the file
                    // but set as long address
                    log.debug("using direction from file {}", direction.getValue());
                    address = new DccLocoAddress(
                            Integer.parseInt(number.getValue()),
                            true);
                    newConsist.restore(address,
                            direction.getValue().equals("normal"));
                } else if (isLong != null && direction == null) {
                    // use the default direction
                    // but the long/short value from the file
                    address = new DccLocoAddress(
                            Integer.parseInt(number.getValue()),
                            isLong.getValue().equals("yes"));
                    newConsist.restore(address, true);
                } else {
                    // use the default values long address
                    // and normal direction
                    address = new DccLocoAddress(
                            Integer.parseInt(number.getValue()),
                            true);
                    newConsist.restore(address, true);
                }
                if (position != null && !position.getValue().equals("mid")) {
                    if (position.getValue().equals("lead")) {
                        newConsist.setPosition(address, Consist.POSITION_LEAD);
                    } else if (position.getValue().equals("rear")) {
                        newConsist.setPosition(address, Consist.POSITION_TRAIL);
                    }
                } else {
                    Attribute midNumber = e.getAttribute("locoMidNumber");
                    if (midNumber != null) {
                        int pos = Integer.parseInt(midNumber.getValue());
                        newConsist.setPosition(address, pos);
                    }
                }
                if (rosterId != null) {
                    newConsist.setRosterId(address, rosterId.getValue());
                }
            } while (true);
        } catch (NoSuchElementException nse) {
            log.debug("end of loco list");
        }
    }

    /**
     * convert a Consist to XML.
     *
     * @param consist a Consist object to write to the file
     * @return an Element representing the consist.
     */
    private Element consistToXml(Consist consist) {
        Element e = new Element("consist");
        e.setAttribute("id", consist.getConsistID());
        e.setAttribute("consistNumber", "" + consist.getConsistAddress()
                .getNumber());
        e.setAttribute("longAddress", consist.getConsistAddress()
                .isLongAddress() ? "yes" : "no");
        e.setAttribute("type", consist.getConsistType() == Consist.ADVANCED_CONSIST ? "DAC" : "CSAC");
        ArrayList<DccLocoAddress> addressList = consist.getConsistList();

        for (int i = 0; i < addressList.size(); i++) {
            DccLocoAddress locoaddress = addressList.get(i);
            Element eng = new Element("loco");
            eng.setAttribute("dccLocoAddress", "" + locoaddress.getNumber());
            eng.setAttribute("longAddress", locoaddress.isLongAddress() ? "yes" : "no");
            eng.setAttribute("locoDir", consist.getLocoDirection(locoaddress) ? "normal" : "reverse");
            int position = consist.getPosition(locoaddress);
            switch (position) {
                case Consist.POSITION_LEAD:
                    eng.setAttribute("locoName", "lead");
                    break;
                case Consist.POSITION_TRAIL:
                    eng.setAttribute("locoName", "rear");
                    break;
                default:
                    eng.setAttribute("locoName", "mid");
                    eng.setAttribute("locoMidNumber", "" + position);
                    break;
            }
            String rosterId = consist.getRosterId(locoaddress);
            if (rosterId != null) {
                eng.setAttribute("locoRosterId", rosterId);
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
    @SuppressWarnings("unchecked")
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
            Iterator<Element> consistIterator = root.getDescendants(new ElementFilter("consist"));
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
    public void writeFile(ArrayList<LocoAddress> consistList) throws IOException {
        writeFile(consistList, defaultConsistFilename());
    }

    /**
     * Write all consists to a file.
     *
     * @param consistList list of consist addresses
     * @param fileName    path to file
     * @throws java.io.IOException if unable to write file
     */
    public void writeFile(ArrayList<LocoAddress> consistList, String fileName) throws IOException {
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
        try {
            if (!checkFile(fileName)) {
                //The file does not exist, create it before writing
                File file = new File(fileName);
                File parentDir = file.getParentFile();
                if (!parentDir.exists()) {
                    if (!parentDir.mkdir()) {
                        throw (new IOException());
                    }
                }
                if (!file.createNewFile()) {
                    throw (new IOException());
                }
            }
            writeXML(findFile(fileName), doc);
        } catch (IOException ioe) {
            log.error("IO Exception " + ioe);
            throw (ioe);
        }
    }

    /**
     * Defines the preferences subdirectory in which LocoFiles are kept by
     * default.
     */
    // TODO: move this logic to getFileLocation, so it can match the roster,
    // which is not always in the UserFiles location, and can change
    static private String fileLocation = FileUtil.getUserFilesPath() + "roster" + File.separator + "consist";

    static public String getFileLocation() {
        return fileLocation;
    }

    static public void setFileLocation(String loc) {
        fileLocation = loc;
        if (!fileLocation.endsWith(File.separator)) {
            fileLocation = fileLocation + File.separator;
        }
    }

    /**
     * Get the filename for the default Consist file, including location.
     *
     * @return the filename
     */
    public static String defaultConsistFilename() {
        return getFileLocation() + "consist.xml";
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ConsistFile.class);
}
