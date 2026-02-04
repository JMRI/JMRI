package jmri.jmrix.loconet.configurexml;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import jmri.jmrit.XmlFile;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.Ln7gAccyRoutesManager;
import jmri.jmrix.loconet.alm.LnSimple7thGenDeviceRoutes;
import jmri.jmrix.loconet.alm.LnSimpleRouteEntry;
import jmri.jmrix.loconet.alm.RouteSwitchPositionEnum;
import jmri.util.FileUtil;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for sevgen.Accy7thGenRoutes objects.
 *
 * @author B. Milhaupt; Copyright(c) 2024
 */

public class Digitrax7thGenAccyRoutesXML {
    private final Ln7gAccyRoutesManager l7garm;

    public Digitrax7thGenAccyRoutesXML(Ln7gAccyRoutesManager l7garm) {
        this.l7garm = l7garm;
    }

    /**
     * Load the Routes info from file.
     * @return true if loaded
     */
    public boolean loadXML() {
        if (l7garm.getCountOfDevicesWithRoutes() > 0) {
            log.warn("loadXML: already loaded.  Quitting.");
            return true;
        }
        DigitraxRoutesXmlFile xmlFile = new DigitraxRoutesXmlFile();
        File file = xmlFile.getLoadFile();

        if (file == null) {
            doStore();
            file = xmlFile.getStoreFile();
        }

        if (!file.exists()) {
            log.warn("loadXML: Note: File does not exist.");
            return false;
        }

        // Find digitraxRoutesPreferencesElement
        Element root;
        try {
            root = xmlFile.rootFromFile(file);
            if (root == null) {
                log.warn("loadXML: File could not be read");  // NOI18N
                return false;
            }
            if (!root.getName().equals("DigitraxRoutesPreferences") ) {  // NOI18N
                log.warn("loadXML: Wrong root name: {}",root.getName());
                return false;
            }

            for (Element routes: root.getChildren()) {
                if (!routes.getAttributeValue("class").equals("Accy7thGenRoutes.configurexml.RoutesManagerXml")) {
                    log.debug("loadXML: routes ignored at element {}, class {}",
                            routes.getName(), routes.getAttribute("class"));
                } else {
                    for (Element device: routes.getChildren("device")) {
                        String type=device.getAttributeValue("type");
                        String serNum = device.getAttributeValue("serNum");
                        String baseAddr = device.getAttributeValue("baseAddr");
                        log.debug("loadXML: device {}, serNum {}, baseAddr {}",
                                type, serNum, baseAddr);

                        LnSimple7thGenDeviceRoutes dev =
                                new LnSimple7thGenDeviceRoutes(
                                        LnSimple7thGenDeviceRoutes.getDeviceType(type),
                                        Integer.parseInt(serNum));
                        dev.setBaseAddr(Integer.parseInt(baseAddr));
                        log.debug("loadXML: add {}", dev.getDeviceType());

                        int numRoutes = ((dev.getDeviceType() == LnConstants.RE_IPL_DIGITRAX_HOST_DS78V) ? 16:8);
                        for (int i = 0; i < numRoutes; ++i) {
                            for (int j =0; j<8; ++j) {
                                dev.setOneEntry(i, j, -1, RouteSwitchPositionEnum.UNUSED);
                            }
                        }

                        for (Element route: device.getChildren()) {
                            log.debug("\tdevice's route {}", route.getAttributeValue("number"));
                            int rn = Integer.parseInt(route.getAttributeValue("number")) - 1;

                            int entryNum = 0;

                            for (Element entry: route.getChildren()) {
                                if (entry.getName().equals("routeTop")) {
                                    log.debug("\t\trouteTop: {}, {}",
                                            entry.getAttributeValue("controlTurnout"),
                                            entry.getAttributeValue("controlTurnoutState"));
                                    int t = Integer.parseInt(entry.getAttributeValue("controlTurnout"));
                                    dev.setOneEntry(rn, entryNum, t,
                                            RouteSwitchPositionEnum.valueOf(entry.getAttributeValue("controlTurnoutState")));
                                } else {
                                    log.debug("\t\tsubsequent Entry: {}, {}",
                                            entry.getAttributeValue("turnout"),
                                            entry.getAttributeValue("state"));
                                    dev.setOneEntry(rn, entryNum, Integer.parseInt(entry.getAttributeValue("turnout")),
                                            RouteSwitchPositionEnum.valueOf(entry.getAttributeValue("state")));
                                }
                                entryNum++;
                            }
                        }
                        // Register the device/serNum/BaseAddr and routes
                        // to the Ln7gAccyRoutesManager
                        l7garm.addDevice(dev);
                    }
                }
            }
        } catch (JDOMException ex) {
            log.error("loadXML: File invalid", ex);  // NOI18N
            return false;
        } catch (IOException ex) {
            log.error("loadXML: Error reading file", ex);  // NOI18N
            return false;
        }

        log.debug("loadXML: Finished reading the 'DigitraxRoutes' file.");
        return false;
    }


    /**
     * Store the Routes info.
     * @return true if stored
     */
    @SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public boolean doStore()  {
        // Create digitraxRoutesPreferencesElement element
        Element digitraxRoutesPreferencesElement = new Element("DigitraxRoutesPreferences");  // NOI18N
        digitraxRoutesPreferencesElement.setAttribute("noNamespaceSchemaLocation",  // NOI18N
                "http://jmri.org/xml/schema/DigitraxRoutes.xsd",  // NOI18N
                org.jdom2.Namespace.getNamespace("xsi",   // NOI18N
                        "http://www.w3.org/2001/XMLSchema-instance"));  // NOI18N

        Element routesEntity = new Element("routes");  // NOI18N
        digitraxRoutesPreferencesElement.addContent(routesEntity);
        routesEntity.setAttribute("class", "jmri.jmrix.loconet.configurexml.CmdStnRoutesManagerXml");  // NOI18N

        routesEntity = new Element("routes");  // NOI18N
        digitraxRoutesPreferencesElement.addContent(routesEntity);  // NOI18N
        routesEntity.setAttribute("class", "Accy7thGenRoutes.configurexml.RoutesManagerXml");  // NOI18N

        int countOfDevices =  l7garm.getCountOfDevicesWithRoutes();

        log.debug("Saving {} devices",  l7garm.getCountOfDevicesWithRoutes());
        if (countOfDevices < 1) {
            log.debug("No Digitrax 7th-gen Accessory devices with stored/storable routes.");
        } else {
            StringBuilder loggingString = new StringBuilder("Known Digitrax 7th-gen Accessory devices with "
                    + "stored/storable routes:\n");  // NOI18N
            for (int aDevice = 0; aDevice < l7garm.getCountOfDevicesWithRoutes(); aDevice++) {
                LnSimple7thGenDeviceRoutes device = l7garm.getDevice(aDevice);
                loggingString.append("\tDevice ");  // NOI18N
                loggingString.append(l7garm.getDevName(device.getDeviceType()));
                loggingString.append(", ser. num. ");  // NOI18N
                loggingString.append(device.getSerNum());
                loggingString.append(", base addr. ");  // NOI18N
                loggingString.append(device.getBaseAddr());
                loggingString.append(".\n");
                log.debug("doStore: saving {}\n",loggingString.toString());
                Element deviceEntity = new Element("device");  // NOI18N
                routesEntity.addContent(deviceEntity);

                deviceEntity.setAttribute("type",   // NOI18N
                        l7garm.getDevName(device.getDeviceType()));
                deviceEntity.setAttribute("serNum",   // NOI18N
                        Integer.toString(device.getSerNum()));
                deviceEntity.setAttribute("baseAddr",    // NOI18N
                        Integer.toString(device.getBaseAddr()));

                // number of routes for the device
                int numAvailRoutes =
                        (device.getDeviceType() == LnConstants.RE_IPL_DIGITRAX_HOST_DS78V)
                        ? 16 : 8;

                for (int routeNumber = 0; routeNumber < numAvailRoutes; ++routeNumber) {
                    log.debug("doStore: device {} route {} (of {} possible device routes)",
                            aDevice, routeNumber+1, numAvailRoutes);
                    // get route'loggingString "top" entry, to see if it is "used"
                    if (device.
                            getRoutes(routeNumber) != null) {
                        LnSimpleRouteEntry re = device.getRoutes(routeNumber).getRouteEntry(0);
                        if (re != null) {
                            int turn = device.getRoutes(routeNumber).getRouteEntry(0).getNumber();
                            log.debug("\tdoStore: route {} entry 0 turn = {}", routeNumber, turn);
                            if ((turn >= 0) && (turn <= 2043)) {
                                log.debug("\tdoStore: Continued at route {} with"
                                        + " 1<=address<=2044: {}.",
                                        routeNumber, turn);

                                Element routeElement = new Element("route");  // NOI18N
                                routeElement.setAttribute("number",   // NOI18N
                                        Integer.toString((routeNumber+1)));
                                loggingString.append("\t\tRoute ");  // NOI18N
                                loggingString.append(routeNumber+1);
                                loggingString.append(":\n");
                                boolean anUndefined = false;
                                for (int entryNumber = 0; (entryNumber < 8) &&
                                        (!anUndefined); ++entryNumber) {
                                    int addr = device.getRoutes(routeNumber).
                                                    getRouteEntry(entryNumber).getNumber();
                                    RouteSwitchPositionEnum state = device.getRoutes(routeNumber).
                                                    getRouteEntry(entryNumber).getPosition();
                                    if (state == null) {
                                        state = RouteSwitchPositionEnum.UNUSED;
                                        anUndefined = true;
                                        log.debug("\tDevice {} Route {} loop entry {}"
                                                + " has state {} ",
                                                aDevice, routeNumber+1, entryNumber,
                                                state.toString());
                                    } else {
                                        log.debug("\tDevice {} Route {} loop entry {} "
                                                + "address {} has state {} ",
                                                aDevice, routeNumber+1, entryNumber,
                                                addr, state.toString());
                                        Element routeEntryElement;
                                        if (entryNumber == 0) {
                                            routeEntryElement = new Element("routeTop");  // NOI18N
                                            routeEntryElement.setAttribute("controlTurnout",  // NOI18N
                                                    Integer.toString(addr+1));
                                            routeEntryElement.setAttribute("controlTurnoutState",   // NOI18N
                                                    state.toString() );
                                            routeElement.addContent(routeEntryElement);
                                            loggingString.append("\t\tTop Addr:");  // NOI18N
                                            loggingString.append(Integer.toString(addr+1));
                                        } else {
                                            routeEntryElement = new Element("routeOutputTurnout");  // NOI18N
                                            routeEntryElement.setAttribute("turnout",   // NOI18N
                                                    Integer.toString(addr+1));
                                            routeEntryElement.setAttribute("state",   // NOI18N
                                                    state.toString());
                                            routeElement.addContent(routeEntryElement);
                                            loggingString.append("\tAddr:");    // NOI18N
                                            loggingString.append(Integer.toString(addr+1));
                                        }
                                        loggingString.append(", State:");  // NOI18N
                                        loggingString.append(state);
                                    }
                                    if ((entryNumber == 0) && (state != RouteSwitchPositionEnum.UNUSED)) {
                                        deviceEntity.addContent(routeElement);

                                    }
                                }
                                loggingString.append("\n");
                            } else {
                                log.debug("\tdoStore: no entries because top address is -1.");
                            }
                        }
                    }
                }
            }
            log.debug("doStore: saving string as: {}", loggingString.toString());
        }

        DigitraxRoutesXmlFile dtxXmlFil = new DigitraxRoutesXmlFile();
        File fileFileFile = dtxXmlFil.getStoreFile();
        Document xmlDoc = new Document(digitraxRoutesPreferencesElement);

        log.debug("doStore: checking canwrite()");
        if (!fileFileFile.canWrite()) {
            log.debug("doStore: canwrite is false; Unable to create writable local Digitrax routes file");
            try {
                fileFileFile.createNewFile();
            } catch (IOException e) {
                log.error("IOError trying to create file!");
                return false;
            }
            if (!fileFileFile.canWrite()) {
                return false;
            }
        }

        try {
            dtxXmlFil.writeXML(fileFileFile, xmlDoc);
        } catch (FileNotFoundException ex) {
            log.error("File not found when writing", ex);  // NOI18N
            return false;
        } catch (IOException ex) {
            log.error("IO Exception when writing", ex);  // NOI18N
            return false;
        }
        return true;
    }

    public static class DigitraxRoutesXmlFile extends XmlFile {
        private static final String PRODUCTIONFILEPATH = FileUtil.getProgramPath()
                + "resources/digitraxRoutes/";  // NOI18N
        private static final String USERFILEPATH = FileUtil.getUserFilesPath()
                + "resources/digitraxRoutes/";  // NOI18N
        private static final String TARGETFILEPATH = "DigitraxRoutes.xml";  // NOI18N

        /**
         * Getter.
         * @return store file name including path
         */
        public static String getStoreFileName() {
            return USERFILEPATH + TARGETFILEPATH;
        }

        /**
         * Getter.
         * @return load file name including path
         */
        public static String getLoadFileName() {
            return getStoreFileName();
        }

        /**
         * Getter
         * @return store file
         */
        public File getStoreFile() {

            log.debug("getStoreFile: check directory exists - {}", USERFILEPATH);
            File chkdir = new File(USERFILEPATH);
            log.debug("getStoreFile: check directory exists result: chkdir {}", chkdir);
            if ((!chkdir.exists()) && (!chkdir.mkdirs())) {
                log.warn("getStoreFile: check directory does not exist & make directory failed!");
                return null;
            }
            File file = findFile(getStoreFileName());
            log.debug("getStoreFile part n/2: file is {}.", file);

            if (file == null) {
                log.debug("getStoreFile: Try to Create new DigitraxRoutes.xml file");  // NOI18N
                file = new File(getStoreFileName());
                log.debug("getStoreFile part n: file is {}.", file);
            } else {
                try {
                    FileUtil.rotate(file, 4, "bup");  // NOI18N
                } catch (IOException ex) {
                    log.debug("Rotate failed, reverting to xml backup");  // NOI18N
                    makeBackupFile(getStoreFileName());
                }
            }
            return file;
        }

        /**
         * Getter.
         * @return load file
         */
        public File getLoadFile() {
            log.debug("getLoadFile: USERFILEPATH = {}, TARGETFILEPATH = {}",
                    USERFILEPATH , TARGETFILEPATH);
            File file = findFile(USERFILEPATH + TARGETFILEPATH);

            if (file == null) {
                file = findFile(PRODUCTIONFILEPATH + TARGETFILEPATH);
                log.debug("getLoadFile: try2 - file = {}", file);
            }
            return file;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Digitrax7thGenAccyRoutesXML.class);
}
