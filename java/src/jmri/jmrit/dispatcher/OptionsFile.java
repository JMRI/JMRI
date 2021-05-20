package jmri.jmrit.dispatcher;

import java.io.File;
import java.util.Set;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.ScaleManager;
import jmri.configurexml.AbstractXmlAdapter.EnumIO;
import jmri.configurexml.AbstractXmlAdapter.EnumIoNamesNumbers;
import jmri.jmrit.dispatcher.DispatcherFrame.TrainsFrom;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.FileUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles reading and writing of Dispatcher options to disk as an XML file
 * called "dispatcher-options.xml" in the user's preferences area.
 * <p>
 * This class manipulates the files conforming to the dispatcher-options DTD
 * <p>
 * The file is written when the user requests that options be saved. If the
 * dispatcheroptions.xml file is present when Dispatcher is started, it is read
 * and options set accordingly
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
 * @author Dave Duchamp Copyright (C) 2008
 */
public class OptionsFile extends jmri.jmrit.XmlFile implements InstanceManagerAutoDefault {

    public OptionsFile() {
        super();
    }

    static final EnumIO<DispatcherFrame.TrainsFrom> trainsFromEnumMap = new EnumIoNamesNumbers<>(DispatcherFrame.TrainsFrom.class);

    // operational variables
    protected DispatcherFrame dispatcher = null;
    private static String defaultFileName = FileUtil.getUserFilesPath() + "dispatcheroptions.xml";

    public static void setDefaultFileName(String testLocation) {
        defaultFileName = testLocation;
    }
    private Document doc = null;
    private Element root = null;

    /**
     * Read Dispatcher Options from a file in the user's preferences directory.
     * If the file containing Dispatcher Options does not exist, this routine returns quietly.
     * @param f   The dispatcher instance.
     * @throws org.jdom2.JDOMException  if dispatcher parameter logically incorrect
     * @throws java.io.IOException    if dispatcher parameter not found
     */
    public void readDispatcherOptions(DispatcherFrame f) throws org.jdom2.JDOMException, java.io.IOException {
        // check if file exists
        if (checkFile(defaultFileName)) {
            // file is present,
            log.debug("Reading Dispatcher options from file {}", defaultFileName);
            root = rootFromName(defaultFileName);
            dispatcher = f;
            if (root != null) {
                // there is a file
                Element options = root.getChild("options");
                if (options != null) {
                    // there are options defined, read and set Dispatcher options
                    if (options.getAttribute("lename") != null) {
                        // there is a layout editor name selected
                        String leName = options.getAttribute("lename").getValue();
                        // get list of Layout Editor panels
                        Set<LayoutEditor> layoutEditorList = InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class);
                        if (layoutEditorList.isEmpty()) {
                            log.warn("Dispatcher options specify a Layout Editor panel that is not present.");
                        } else {
                            boolean found = false;
                            for (LayoutEditor editor : layoutEditorList) {
                                if (leName.equals(editor.getTitle())) {
                                    found = true;
                                    dispatcher.setLayoutEditor(editor);
                                }
                            }
                            if (!found) {
                                log.warn("Layout Editor panel - {} - not found.", leName);
                            }
                        }
                    }
                    if (options.getAttribute("usesignaltype") != null) {
                        switch (options.getAttribute("usesignaltype").getValue()) {
                            case "signalmast":
                                dispatcher.setSignalType(DispatcherFrame.SIGNALMAST);
                                break;
                            case "sectionsallocated":
                                dispatcher.setSignalType(DispatcherFrame.SECTIONSALLOCATED);
                                break;
                            default:
                                dispatcher.setSignalType(DispatcherFrame.SIGNALHEAD);
                        }
                    }
                    if (options.getAttribute("useconnectivity") != null) {
                        dispatcher.setUseConnectivity(true);
                        if (options.getAttribute("useconnectivity").getValue().equals("no")) {
                            dispatcher.setUseConnectivity(false);
                        }
                    }
                    if (options.getAttribute("trainsfrom") != null) {
                        dispatcher.setTrainsFrom(trainsFromEnumMap.inputFromAttribute(options.getAttribute("trainsfrom")));
                    } else {
                        log.warn("Old Style dispatcheroptions file found - will be converted when saved");
                    if (options.getAttribute("trainsfromroster") != null &&
                            options.getAttribute("trainsfromroster").getValue().equals("yes")) {
                        dispatcher.setTrainsFrom(TrainsFrom.TRAINSFROMROSTER);
                    } else if (options.getAttribute("trainsfromtrains") != null &&
                            options.getAttribute("trainsfromtrains").getValue().equals("no")) {
                        dispatcher.setTrainsFrom(TrainsFrom.TRAINSFROMOPS);
                    } else if (options.getAttribute("trainsfromuser") != null &&
                            options.getAttribute("trainsfromuser").getValue().equals("no")) {
                        dispatcher.setTrainsFrom(TrainsFrom.TRAINSFROMUSER);
                    }
                    }
                    if (options.getAttribute("autoallocate") != null) {
                        dispatcher.setAutoAllocate(false);
                        if (options.getAttribute("autoallocate").getValue().equals("yes")) {
                            dispatcher.setAutoAllocate(true);
                        }
                    }
                    if (options.getAttribute("autorelease") != null) {
                        dispatcher.setAutoRelease(false);
                        if (options.getAttribute("autorelease").getValue().equals("yes")) {
                            dispatcher.setAutoRelease(true);
                        }
                    }
                    if (options.getAttribute("autoturnouts") != null) {
                        dispatcher.setAutoTurnouts(true);
                        if (options.getAttribute("autoturnouts").getValue().equals("no")) {
                            dispatcher.setAutoTurnouts(false);
                        }
                    }
                    if (options.getAttribute("trustknownturnouts") != null) {
                        dispatcher.setTrustKnownTurnouts(false);
                        if (options.getAttribute("trustknownturnouts").getValue().equals("yes")) {
                            dispatcher.setTrustKnownTurnouts(true);
                        }
                    }
                    if (options.getAttribute("minthrottleinterval") != null) {
                        String s = (options.getAttribute("minthrottleinterval")).getValue();
                        dispatcher.setMinThrottleInterval(Integer.parseInt(s));
                    }
                    if (options.getAttribute("fullramptime") != null) {
                        String s = (options.getAttribute("fullramptime")).getValue();
                        dispatcher.setFullRampTime(Integer.parseInt(s));
                    }
                    if (options.getAttribute("hasoccupancydetection") != null) {
                        dispatcher.setHasOccupancyDetection(true);
                        if (options.getAttribute("hasoccupancydetection").getValue().equals("no")) {
                            dispatcher.setHasOccupancyDetection(false);
                        }
                    }
                    if (options.getAttribute("sslcheckdirectionsensors") != null) {
                        dispatcher.setSetSSLDirectionalSensors(true);
                        if (options.getAttribute("sslcheckdirectionsensors").getValue().equals("no")) {
                            dispatcher.setSetSSLDirectionalSensors(false);
                        }
                    }
                    if (options.getAttribute("shortactivetrainnames") != null) {
                        dispatcher.setShortActiveTrainNames(true);
                        if (options.getAttribute("shortactivetrainnames").getValue().equals("no")) {
                            dispatcher.setShortActiveTrainNames(false);
                        }
                    }
                    if (options.getAttribute("shortnameinblock") != null) {
                        dispatcher.setShortNameInBlock(true);
                        if (options.getAttribute("shortnameinblock").getValue().equals("no")) {
                            dispatcher.setShortNameInBlock(false);
                        }
                    }
                    if (options.getAttribute("extracolorforallocated") != null) {
                        dispatcher.setExtraColorForAllocated(true);
                        if (options.getAttribute("extracolorforallocated").getValue().equals("no")) {
                            dispatcher.setExtraColorForAllocated(false);
                        }
                    }
                    if (options.getAttribute("nameinallocatedblock") != null) {
                        dispatcher.setNameInAllocatedBlock(true);
                        if (options.getAttribute("nameinallocatedblock").getValue().equals("no")) {
                            dispatcher.setNameInAllocatedBlock(false);
                        }
                    }
                    if (options.getAttribute("supportvsdecoder") != null) {
                        dispatcher.setSupportVSDecoder(true);
                        if (options.getAttribute("supportvsdecoder").getValue().equals("no")) {
                            dispatcher.setSupportVSDecoder(false);
                        }
                    }
                    if (options.getAttribute("layoutscale") != null) {
                        String s = (options.getAttribute("layoutscale")).getValue();
                        dispatcher.setScale(ScaleManager.getScale(s));
                    }
                    if (options.getAttribute("usescalemeters") != null) {
                        dispatcher.setUseScaleMeters(true);
                        if (options.getAttribute("usescalemeters").getValue().equals("no")) {
                            dispatcher.setUseScaleMeters(false);
                        }
                    }
                    if (options.getAttribute("userosterentryinblock") != null) {
                        dispatcher.setRosterEntryInBlock(false);
                        if (options.getAttribute("userosterentryinblock").getValue().equals("yes")) {
                            dispatcher.setRosterEntryInBlock(true);
                        }
                    }
                    if (options.getAttribute("stoppingspeedname") != null) {
                        dispatcher.setStoppingSpeedName((options.getAttribute("stoppingspeedname")).getValue());
                    }
                    
                    log.debug("  Options: {}, Detection={}, AutoAllocate={}, AutoTurnouts={}, SetSSLDirectionSensors={}", 
                            (dispatcher.getSignalTypeString()),
                            (dispatcher.getAutoAllocate()?"yes":"no"),
                            (dispatcher.getAutoTurnouts()?"yes":"no"),
                            (dispatcher.getSetSSLDirectionalSensors()?"yes":"no"));
                }
            }
        } else {
            log.debug("No Dispatcher options file found at {}, using defaults", defaultFileName);
        }
    }

    /**
     * Write out Dispatcher options to a file in the user's preferences directory.
     * @param f Dispatcher instance.
     * @throws java.io.IOException Thrown if dispatcher option file not found
     */
    public void writeDispatcherOptions(DispatcherFrame f) throws java.io.IOException {
        log.debug("Saving Dispatcher options to file {}", defaultFileName);
        dispatcher = f;
        root = new Element("dispatcheroptions");
        doc = newDocument(root, dtdLocation + "dispatcher-options.dtd");
        // add XSLT processing instruction
        // <?xml-stylesheet type="text/xsl" href="XSLT/block-values.xsl"?>
        java.util.Map<String, String> m = new java.util.HashMap<>();
        m.put("type", "text/xsl");
        m.put("href", xsltLocation + "dispatcheroptions.xsl");
        org.jdom2.ProcessingInstruction p = new org.jdom2.ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0, p);

        // save Dispatcher Options in xml format
        Element options = new Element("options");
        LayoutEditor le = dispatcher.getLayoutEditor();
        if (le != null) {
            options.setAttribute("lename", le.getTitle());
        }
        options.setAttribute("useconnectivity", "" + (dispatcher.getUseConnectivity() ? "yes" : "no"));
        options.setAttribute("trainsfrom", trainsFromEnumMap.outputFromEnum(dispatcher.getTrainsFrom()));
        options.setAttribute("autoallocate", "" + (dispatcher.getAutoAllocate() ? "yes" : "no"));
        options.setAttribute("autorelease", "" + (dispatcher.getAutoRelease() ? "yes" : "no"));
        options.setAttribute("autoturnouts", "" + (dispatcher.getAutoTurnouts() ? "yes" : "no"));
        options.setAttribute("trustknownturnouts", "" + (dispatcher.getTrustKnownTurnouts() ? "yes" : "no"));
        options.setAttribute("minthrottleinterval", "" + (dispatcher.getMinThrottleInterval()));
        options.setAttribute("fullramptime", "" + (dispatcher.getFullRampTime()));
        options.setAttribute("hasoccupancydetection", "" + (dispatcher.getHasOccupancyDetection() ? "yes" : "no"));
        options.setAttribute("sslcheckdirectionsensors", "" + (dispatcher.getSetSSLDirectionalSensors() ? "yes" : "no"));
        options.setAttribute("shortactivetrainnames", "" + (dispatcher.getShortActiveTrainNames() ? "yes" : "no"));
        options.setAttribute("shortnameinblock", "" + (dispatcher.getShortNameInBlock() ? "yes" : "no"));
        options.setAttribute("extracolorforallocated", "" + (dispatcher.getExtraColorForAllocated() ? "yes" : "no"));
        options.setAttribute("nameinallocatedblock", "" + (dispatcher.getNameInAllocatedBlock() ? "yes" : "no"));
        options.setAttribute("supportvsdecoder", "" + (dispatcher.getSupportVSDecoder() ? "yes" : "no"));
        options.setAttribute("layoutscale", dispatcher.getScale().getScaleName());
        options.setAttribute("usescalemeters", "" + (dispatcher.getUseScaleMeters() ? "yes" : "no"));
        options.setAttribute("userosterentryinblock", "" + (dispatcher.getRosterEntryInBlock() ? "yes" : "no"));
        options.setAttribute("stoppingspeedname", dispatcher.getStoppingSpeedName());
        switch (dispatcher.getSignalType()) {
            case DispatcherFrame.SIGNALMAST:
                options.setAttribute("usesignaltype", "signalmast");
                break;
            case DispatcherFrame.SECTIONSALLOCATED:
                options.setAttribute("usesignaltype", "sectionsallocated");
                break;
            default:
                options.setAttribute("usesignaltype", "signalhead");
        }
        root.addContent(options);

        // write out the file
        try {
            if (!checkFile(defaultFileName)) {
                // file does not exist, create it
                File file = new File(defaultFileName);
                if (!file.createNewFile()) // create new file and check result
                {
                    log.error("createNewFile failed");
                }
            }
            // write content to file
            writeXML(findFile(defaultFileName), doc);
        } catch (java.io.IOException ioe) {
            log.error("IO Exception {}", ioe.getMessage());
            throw (ioe);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(OptionsFile.class);
}
