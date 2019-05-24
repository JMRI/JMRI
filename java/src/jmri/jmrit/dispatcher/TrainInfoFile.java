package jmri.jmrit.dispatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jmri.util.FileUtil;
import jmri.util.XmlFilenameFilter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles reading and writing of TrainInfo files to disk as an XML file to/from
 * the dispatcher/traininfo/ directory in the user's preferences area
 * <p>
 * This class manipulates the files conforming to the dispatcher-traininfo DTD
 * <p>
 * The file is written when the user requests that train information be saved. A
 * TrainInfo file is read when the user request it in the Activate New Train
 * window
 *
 * <p>
 * This file is part of JMRI.
 * <p>
 * JMRI is open source software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Dave Duchamp Copyright (C) 2009
 */
public class TrainInfoFile extends jmri.jmrit.XmlFile {

    public TrainInfoFile() {
        super();
    }
    // operational variables
    private String fileLocation = FileUtil.getUserFilesPath()
            + "dispatcher" + File.separator + "traininfo" + File.separator;

    public void setFileLocation(String testLocation) {
        fileLocation = testLocation;
    }
    private Document doc = null;
    private Element root = null;

    /*
     *  Reads Dispatcher TrainInfo from a file in the user's preferences directory
     *  If the file containing Dispatcher TrainInfo does not exist this routine returns quietly.
     *  "name" is assumed to have the .xml or .XML extension already included
     */
    public TrainInfo readTrainInfo(String name) throws org.jdom2.JDOMException, java.io.IOException {
        log.debug("entered readTrainInfo for {}", name);
        TrainInfo tInfo = null;
        int version  = 1;
        // check if file exists
        if (checkFile(fileLocation + name)) {
            // file is present.
            tInfo = new TrainInfo();
            root = rootFromName(fileLocation + name);
            if (root != null) {
                // there is a file
                Element traininfo = root.getChild("traininfo");
                if (traininfo != null) {
                    // get version so we dont look for missing fields
                    if (traininfo.getAttribute("version") != null ) {
                        try {
                            version = traininfo.getAttribute("version").getIntValue();
                        }
                        catch(Exception ex) {
                            log.error("Traininfo file version number not an integer: assuming version 1");
                            version = 1;
                        }
                    } else {
                        version = 1;
                    }
                    // there are train info options defined, read them
                    if (traininfo.getAttribute("transitname") != null) {
                        // there is a transit name selected
                        tInfo.setTransitName(traininfo.getAttribute("transitname").getValue());
                    } else {
                        log.error("Transit name missing when reading TrainInfoFile " + name);
                    }
                    if (traininfo.getAttribute("trainname") != null) {
                        // there is a transit name selected
                        tInfo.setTrainName(traininfo.getAttribute("trainname").getValue());
                    } else {
                        log.error("Train name missing when reading TrainInfoFile " + name);
                    }
                    if (traininfo.getAttribute("dccaddress") != null) {
                        tInfo.setDccAddress(traininfo.getAttribute("dccaddress").getValue());
                    } else {
                        log.error("DCC Address missing when reading TrainInfoFile " + name);
                    }
                    if (traininfo.getAttribute("trainintransit") != null) {
                        tInfo.setTrainInTransit(true);
                        if (traininfo.getAttribute("trainintransit").getValue().equals("no")) {
                            tInfo.setTrainInTransit(false);
                        }
                    } else {
                        log.error("Train in Transit check box missing  when reading TrainInfoFile " + name);
                    }
                    if (traininfo.getAttribute("startblockname") != null) {
                        // there is a transit name selected
                        tInfo.setStartBlockName(traininfo.getAttribute("startblockname").getValue());
                    } else {
                        log.error("Start block name missing when reading TrainInfoFile " + name);
                    }
                    if (traininfo.getAttribute("endblockname") != null) {
                        // there is a transit name selected
                        tInfo.setDestinationBlockName(traininfo.getAttribute("endblockname").getValue());
                    } else {
                        log.error("Destination block name missing when reading TrainInfoFile " + name);
                    }

                    if (traininfo.getAttribute("trainfromroster") != null) {
                        tInfo.setTrainFromRoster(true);
                        if (traininfo.getAttribute("trainfromroster").getValue().equals("no")) {
                            tInfo.setTrainFromRoster(false);
                        }
                    }
                    if (traininfo.getAttribute("trainfromtrains") != null) {
                        tInfo.setTrainFromTrains(true);
                        if (traininfo.getAttribute("trainfromtrains").getValue().equals("no")) {
                            tInfo.setTrainFromTrains(false);
                        }
                    }
                    if (traininfo.getAttribute("trainfromuser") != null) {
                        tInfo.setTrainFromUser(true);
                        if (traininfo.getAttribute("trainfromuser").getValue().equals("no")) {
                            tInfo.setTrainFromUser(false);
                        }
                    }
                    if (traininfo.getAttribute("priority") != null) {
                        tInfo.setPriority(Integer.parseInt(traininfo.getAttribute("priority").getValue()));
                    } else {
                        log.error("Priority missing when reading TrainInfoFile " + name);
                    }
                    if (traininfo.getAttribute("allocatealltheway") != null) {
                        if (traininfo.getAttribute("allocatealltheway").getValue().equals("yes")) {
                            tInfo.setAllocateAllTheWay(true);
                        }
                    }
                    if (traininfo.getAttribute("allocationmethod") != null) {
                        tInfo.setAllocationMethod(traininfo.getAttribute("allocationmethod").getIntValue());
                    }

                    if (traininfo.getAttribute("resetwhendone") != null) {
                        if (traininfo.getAttribute("resetwhendone").getValue().equals("yes")) {
                            tInfo.setResetWhenDone(true);
                        }
                        if (traininfo.getAttribute("delayedrestart") != null) {
                            switch (traininfo.getAttribute("delayedrestart").getValue()) {
                                case "no":
                                    tInfo.setDelayedRestart(ActiveTrain.NODELAY);
                                    break;
                                case "sensor":
                                    tInfo.setDelayedRestart(ActiveTrain.SENSORDELAY);
                                    if (traininfo.getAttribute("delayedrestartsensor") != null) {
                                        tInfo.setRestartSensorName(traininfo.getAttribute("delayedrestartsensor").getValue());
                                    }   
                                    if (traininfo.getAttribute("resetrestartsensor") != null) {
                                        tInfo.setResetRestartSensor(traininfo.getAttribute("resetrestartsensor").getValue().equals("yes"));
                                    }
                                    break;
                                case "timed":
                                    tInfo.setDelayedRestart(ActiveTrain.TIMEDDELAY);
                                    if (traininfo.getAttribute("delayedrestarttime") != null) {
                                        tInfo.setRestartDelayMin((int) traininfo.getAttribute("delayedrestarttime").getLongValue());
                                    }   break;
                                default:
                                    break;
                            }
                        }
                    }
                    if (traininfo.getAttribute("reverseatend") != null) {
                        tInfo.setReverseAtEnd(true);
                        if (traininfo.getAttribute("reverseatend").getValue().equals("no")) {
                            tInfo.setReverseAtEnd(false);
                        }
                    }
                    if (traininfo.getAttribute("delayedstart") != null) {
                        switch (traininfo.getAttribute("delayedstart").getValue()) {
                            case "no":
                                tInfo.setDelayedStart(ActiveTrain.NODELAY);
                                break;
                            case "sensor":
                                tInfo.setDelayedStart(ActiveTrain.SENSORDELAY);
                                break;
                            default:
                                //This covers the old versions of the file with "yes"
                                tInfo.setDelayedStart(ActiveTrain.TIMEDDELAY);
                                break;
                        }
                    }
                    if (traininfo.getAttribute("departuretimehr") != null) {
                        tInfo.setDepartureTimeHr(Integer.parseInt(traininfo.getAttribute("departuretimehr").getValue()));
                    }
                    if (traininfo.getAttribute("departuretimemin") != null) {
                        tInfo.setDepartureTimeMin(Integer.parseInt(traininfo.getAttribute("departuretimemin").getValue()));
                    }
                    if (traininfo.getAttribute("delayedSensor") != null) {
                        tInfo.setDelaySensorName(traininfo.getAttribute("delayedSensor").getValue());
                    }
                    if (traininfo.getAttribute("resetstartsensor") != null) {
                        tInfo.setResetStartSensor(traininfo.getAttribute("resetstartsensor").getValue().equals("yes"));
                    }
                    if (traininfo.getAttribute("traintype") != null) {
                        tInfo.setTrainType(traininfo.getAttribute("traintype").getValue());
                    }
                    if (traininfo.getAttribute("autorun") != null) {
                        tInfo.setAutoRun(true);
                        if (traininfo.getAttribute("autorun").getValue().equals("no")) {
                            tInfo.setAutoRun(false);
                        }
                    }
                    if (traininfo.getAttribute("loadatstartup") != null) {
                        tInfo.setLoadAtStartup(true);
                        if (traininfo.getAttribute("loadatstartup").getValue().equals("no")) {
                            tInfo.setLoadAtStartup(false);
                        }
                    }
                    // here retrieve items related only to automatically run trains if present
                    if (traininfo.getAttribute("speedfactor") != null) {
                        tInfo.setSpeedFactor(Float.parseFloat(traininfo.getAttribute("speedfactor").getValue()));
                    }
                    if (traininfo.getAttribute("maxspeed") != null) {
                        tInfo.setMaxSpeed(Float.parseFloat(traininfo.getAttribute("maxspeed").getValue()));
                    }
                    if (traininfo.getAttribute("ramprate") != null) {
                        tInfo.setRampRate(traininfo.getAttribute("ramprate").getValue());
                    }
                    if (traininfo.getAttribute("resistancewheels") != null) {
                        tInfo.setResistanceWheels(true);
                        if (traininfo.getAttribute("resistancewheels").getValue().equals("no")) {
                            tInfo.setResistanceWheels(false);
                        }
                    }
                    if (traininfo.getAttribute("runinreverse") != null) {
                        tInfo.setRunInReverse(true);
                        if (traininfo.getAttribute("runinreverse").getValue().equals("no")) {
                            tInfo.setRunInReverse(false);
                        }
                    }
                    if (traininfo.getAttribute("sounddecoder") != null) {
                        tInfo.setSoundDecoder(true);
                        if (traininfo.getAttribute("sounddecoder").getValue().equals("no")) {
                            tInfo.setSoundDecoder(false);
                        }
                    }
                    if (traininfo.getAttribute("maxtrainlength") != null) {
                        tInfo.setMaxTrainLength(Float.parseFloat(traininfo.getAttribute("maxtrainlength").getValue()));
                    }
                    if (traininfo.getAttribute("terminatewhendone") != null) {
                        tInfo.setTerminateWhenDone(false);
                        if (traininfo.getAttribute("terminatewhendone").getValue().equals("yes")) {
                            tInfo.setTerminateWhenDone(true);
                        }
                    }
                    if (traininfo.getAttribute("usespeedprofile") != null) {
                        tInfo.setUseSpeedProfile(false);
                        if (traininfo.getAttribute("usespeedprofile").getValue().equals("yes")) {
                            tInfo.setUseSpeedProfile(true);
                        }
                    }
                    if (traininfo.getAttribute("stopbyspeedprofile") != null) {
                        tInfo.setStopBySpeedProfile(false);
                        if (traininfo.getAttribute("stopbyspeedprofile").getValue().equals("yes")) {
                            tInfo.setStopBySpeedProfile(true);
                        }
                    }
                    if (traininfo.getAttribute("stopbyspeedprofileadjust") != null) {
                        tInfo.setStopBySpeedProfileAdjust(traininfo.getAttribute("stopbyspeedprofileadjust").getFloatValue());
                    }

                    if (version == 1) {
                        String parseArray[];
                        // If you only have a systemname then its everything before the dash
                        tInfo.setStartBlockId(tInfo.getStartBlockName().split("-")[0]);
                        // If you have a systemname and username you want everything before the open bracket
                        tInfo.setStartBlockId(tInfo.getStartBlockId().split("\\(")[0]);
                        // to guard against a dash in the names, we need the last part, not just [1]
                        parseArray = tInfo.getStartBlockName().split("-");
                        tInfo.setStartBlockSeq(-1); // default value
                        if (parseArray.length > 0) {
                            try {
                                tInfo.setStartBlockSeq(Integer.parseInt(parseArray[parseArray.length -1]));
                            }
                            catch (Exception Ex) {
                                log.error("Invalid StartBlockSequence{}",parseArray[parseArray.length -1]);
                            }
                        }
                        // repeat for destination
                        tInfo.setDestinationBlockId(tInfo.getDestinationBlockName().split("-")[0]);
                        tInfo.setDestinationBlockId(tInfo.getDestinationBlockId().split("\\(")[0]);
                        parseArray = tInfo.getDestinationBlockName().split("-");
                        tInfo.setDestinationBlockSeq(-1);
                        if (parseArray.length > 0) {
                            try {
                                tInfo.setDestinationBlockSeq(Integer.parseInt(parseArray[parseArray.length -1]));
                            }
                            catch (Exception Ex) {
                                log.error("Invalid StartBlockSequence{}",parseArray[parseArray.length -1]);
                            }
                        }
                        // Transit we need the whole thing or the bit before the first open bracket
                        tInfo.setTransitId(tInfo.getTransitName().split("\\(")[0]);
                    }
                    if ( version == 2 ) {
                        if (traininfo.getAttribute("transitid") != null) {
                            // there is a transit name selected
                            tInfo.setTransitId(traininfo.getAttribute("transitid").getValue());
                        } else {
                            log.error("Transit id missing when reading TrainInfoFile " + name);
                        }
                        if (traininfo.getAttribute("startblockid") != null) {
                            // there is a transit name selected
                            tInfo.setStartBlockId(traininfo.getAttribute("startblockid").getValue());
                        } else {
                            log.error("Start block Id missing when reading TrainInfoFile " + name);
                        }
                        if (traininfo.getAttribute("endblockid") != null) {
                            // there is a transit name selected
                            tInfo.setDestinationBlockId(traininfo.getAttribute("endblockid").getValue());
                        } else {
                            log.error("Destination block Id missing when reading TrainInfoFile " + name);
                        }
                        if (traininfo.getAttribute("startblockseq") != null) {
                            // there is a transit name selected
                            try {
                                tInfo.setStartBlockSeq(traininfo.getAttribute("startblockseq").getIntValue());
                            }
                            catch (Exception ex) {
                                log.error("Start block sequence invalid when reading TrainInfoFile");
                            }
                        } else {
                            log.error("Start block sequence missing when reading TrainInfoFile " + name);
                        }
                        if (traininfo.getAttribute("endblockseq") != null) {
                            // there is a transit name selected
                            try {
                                tInfo.setDestinationBlockSeq(traininfo.getAttribute("endblockseq").getIntValue());
                            }
                            catch (Exception ex) {
                                log.error("Destination block sequence invalid when reading TrainInfoFile " + name);
                            }
                        } else {
                            log.error("Destination block sequence missing when reading TrainInfoFile " + name);
                        }
                    }
                }
            }
        }
        return tInfo;
    }

    /*
     *  Writes out Dispatcher options to a file in the user's preferences directory
     */
    public void writeTrainInfo(TrainInfo tf, String name) throws java.io.IOException {
        log.debug("entered writeTrainInfo");
        root = new Element("traininfofile");
        doc = newDocument(root, dtdLocation + "dispatcher-traininfo.dtd");
        // add XSLT processing instruction
        // <?xml-stylesheet type="text/xsl" href="XSLT/block-values.xsl"?>
        java.util.Map<String, String> m = new java.util.HashMap<>();
        m.put("type", "text/xsl");
        m.put("href", xsltLocation + "dispatcher-traininfo.xsl");
        org.jdom2.ProcessingInstruction p = new org.jdom2.ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0, p);

        // save Dispatcher TrainInfo in xml format
        Element traininfo = new Element("traininfo");
        // write version number
        traininfo.setAttribute("version","2");
        traininfo.setAttribute("transitname", tf.getTransitName());
        traininfo.setAttribute("transitid", tf.getTransitId());
        traininfo.setAttribute("trainname", tf.getTrainName());
        traininfo.setAttribute("dccaddress", tf.getDccAddress());
        traininfo.setAttribute("trainintransit", "" + (tf.getTrainInTransit() ? "yes" : "no"));
        traininfo.setAttribute("startblockname", tf.getStartBlockName());
        traininfo.setAttribute("startblockid", tf.getStartBlockId());
        traininfo.setAttribute("startblockseq", Integer.toString(tf.getStartBlockSeq()));
        traininfo.setAttribute("endblockname", tf.getDestinationBlockName());
        traininfo.setAttribute("endblockid", tf.getDestinationBlockId());
        traininfo.setAttribute("endblockseq", Integer.toString(tf.getDestinationBlockSeq()));
        traininfo.setAttribute("trainfromroster", "" + (tf.getTrainFromRoster() ? "yes" : "no"));
        traininfo.setAttribute("trainfromtrains", "" + (tf.getTrainFromTrains() ? "yes" : "no"));
        traininfo.setAttribute("trainfromuser", "" + (tf.getTrainFromUser() ? "yes" : "no"));
        traininfo.setAttribute("priority", Integer.toString(tf.getPriority()));
        traininfo.setAttribute("resetwhendone", "" + (tf.getResetWhenDone() ? "yes" : "no"));
        switch (tf.getDelayedRestart()) {
            case ActiveTrain.SENSORDELAY:
                traininfo.setAttribute("delayedrestart", "sensor");
                traininfo.setAttribute("delayedrestartsensor", tf.getRestartSensorName());
                traininfo.setAttribute("resetrestartsensor", "" + (tf.getResetRestartSensor() ? "yes" : "no"));
                break;
            case ActiveTrain.TIMEDDELAY:
                traininfo.setAttribute("delayedrestart", "timed");
                traininfo.setAttribute("delayedrestarttime", Integer.toString(tf.getRestartDelayMin()));
                break;
            default:
                traininfo.setAttribute("delayedrestart", "no");
                break;
        }

        traininfo.setAttribute("reverseatend", "" + (tf.getReverseAtEnd() ? "yes" : "no"));
        if (tf.getDelayedStart() == ActiveTrain.TIMEDDELAY) {
            traininfo.setAttribute("delayedstart", "timed");
        } else if (tf.getDelayedStart() == ActiveTrain.SENSORDELAY) {
            traininfo.setAttribute("delayedstart", "sensor");
            if (tf.getDelaySensorName() != null) {
                traininfo.setAttribute("delayedSensor", tf.getDelaySensorName());
                traininfo.setAttribute("resetstartsensor", "" + (tf.getResetStartSensor() ? "yes" : "no"));
            }
        }

        traininfo.setAttribute("terminatewhendone", (tf.getTerminateWhenDone() ? "yes" : "no"));
        traininfo.setAttribute("departuretimehr", Integer.toString(tf.getDepartureTimeHr()));
        traininfo.setAttribute("departuretimemin", Integer.toString(tf.getDepartureTimeMin()));
        traininfo.setAttribute("traintype", tf.getTrainType());
        traininfo.setAttribute("autorun", "" + (tf.getAutoRun() ? "yes" : "no"));
        traininfo.setAttribute("loadatstartup", "" + (tf.getLoadAtStartup() ? "yes" : "no"));
        traininfo.setAttribute("allocatealltheway", "" + (tf.getAllocateAllTheWay() ? "yes" : "no"));
        traininfo.setAttribute("allocationmethod", Integer.toString(tf.getAllocationMethod()));
        // here save items related to automatically running active trains
        traininfo.setAttribute("speedfactor", Float.toString(tf.getSpeedFactor()));
        traininfo.setAttribute("maxspeed", Float.toString(tf.getMaxSpeed()));
        traininfo.setAttribute("ramprate", tf.getRampRate());
        traininfo.setAttribute("resistancewheels", "" + (tf.getResistanceWheels() ? "yes" : "no"));
        traininfo.setAttribute("runinreverse", "" + (tf.getRunInReverse() ? "yes" : "no"));
        traininfo.setAttribute("sounddecoder", "" + (tf.getSoundDecoder() ? "yes" : "no"));
        traininfo.setAttribute("maxtrainlength", Float.toString(tf.getMaxTrainLength()));
        traininfo.setAttribute("usespeedprofile", "" + (tf.getUseSpeedProfile() ? "yes" : "no"));
        traininfo.setAttribute("stopbyspeedprofile", "" + (tf.getStopBySpeedProfile() ? "yes" : "no"));
        traininfo.setAttribute("stopbyspeedprofileadjust", Float.toString(tf.getStopBySpeedProfileAdjust()));

        root.addContent(traininfo);

        // write out the file
        try {
            if (!checkFile(fileLocation + name)) {
                // file does not exist, create it
                File file = new File(fileLocation + name);
                if (!file.createNewFile()) // create file and check result
                {
                    log.error("createNewFile failed");
                }
            }
            // write content to file
            writeXML(findFile(fileLocation + name), doc);
        } catch (java.io.IOException ioe) {
            log.error("IO Exception " + ioe);
            throw (ioe);
        }
    }

    /**
     * Get the names of all current TrainInfo files. Returns names as an array
     * of Strings. Returns an empty array if no files are present. Note: Fill
     * names still end with .xml or .XML. (Modeled after a method in
     * RecreateRosterAction.java by Bob Jacobsen)
     *
     * @return names as an array or an empty array if none present
     */
    public String[] getTrainInfoFileNames() {
        // ensure preferences will be found for read
        FileUtil.createDirectory(fileLocation);
        // create an array of file names from roster dir in preferences, count entries
        List<String> names = new ArrayList<>();
        log.debug("directory of TrainInfoFiles is {}", fileLocation);
        File fp = new File(fileLocation);
        if (fp.exists()) {
            names.addAll(Arrays.asList(fp.list(new XmlFilenameFilter())));
        }
        // Sort the resulting array
        names.sort((s1, s2) -> {
            return s1.compareTo(s2);
        });
        return names.toArray(new String[names.size()]);
    }

    /**
     * Delete a specified TrainInfo file.
     *
     * @param name the file to delete
     */
    public void deleteTrainInfoFile(String name) {
        // locate the file and delete it if it exists
        File f = new File(fileLocation + name);
        if (!f.delete()) { // delete file and check success
            log.error("failed to delete TrainInfo file - " + name);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainInfoFile.class);
}
