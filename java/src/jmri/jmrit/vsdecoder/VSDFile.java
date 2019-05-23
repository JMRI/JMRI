package jmri.jmrit.vsdecoder;

/*
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author   Mark Underwood Copyright (C) 2011
 */
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import jmri.jmrit.XmlFile;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VSDFile extends ZipFile {

    private static final String VSDXmlFileName = "config.xml"; // NOI18N

    // Dummy class just used to instantiate
    private static class VSDXmlFile extends XmlFile {
    }

    protected Element root;
    protected boolean initialized = false;
    private String _statusMsg = Bundle.getMessage("ButtonOK"); // File Status = OK

    ZipInputStream zis;

    public VSDFile(File file) throws ZipException, IOException {
        super(file);
        initialized = init();
    }

    public VSDFile(File file, int mode) throws ZipException, IOException {
        super(file, mode);
        initialized = init();
    }

    public VSDFile(String name) throws ZipException, IOException {
        super(name);
        initialized = init();
    }

    public boolean isInitialized() {
        return (initialized);
    }

    public String getStatusMessage() {
        return (_statusMsg);
    }

    protected boolean init() {
        VSDXmlFile xmlfile = new VSDXmlFile();
        initialized = false;

        try {
            // Debug: List all the top-level contents in the file.
            Enumeration<?> entries = this.entries();
            while (entries.hasMoreElements()) {
                ZipEntry z = (ZipEntry) entries.nextElement();
                log.debug("Entry: " + z.getName());
            }

            ZipEntry config = this.getEntry(VSDXmlFileName);
            if (config == null) {
                _statusMsg = "File does not contain " + VSDXmlFileName;
                log.error(_statusMsg);
                return (false);
            }
            File f2 = new File(this.getURL(VSDXmlFileName));
            root = xmlfile.rootFromFile(f2);
            ValidateStatus rv = this.validate(root);
            if (!rv.getValid()) {
                // Need to make this one fancier right here.
                _statusMsg = rv.getMessage();
                log.error("VALIDATE FAILED: File " + VSDXmlFileName);
            }
            initialized = rv.getValid();
            return (initialized);

        } catch (java.io.IOException ioe) {
            _statusMsg = "IO Error auto-loading VSD File: " + VSDXmlFileName + " " + ioe.toString();
            log.warn(_statusMsg);
            return (false);
        } catch (NullPointerException npe) {
            _statusMsg = "NP Error auto-loading VSD File: path = " + VSDXmlFileName + " " + npe.toString();
            log.warn(_statusMsg);
            return (false);
        } catch (org.jdom2.JDOMException ex) {
            _statusMsg = "JDOM Exception loading VSDecoder from path " + VSDXmlFileName + " " + ex.toString();
            log.error(_statusMsg);
            return (false);
        }
    }

    public Element getRoot() {
        return (root);
    }

    public java.io.InputStream getInputStream(String name) {

        java.io.InputStream rv;
        try {
            ZipEntry e = this.getEntry(name);
            if (e == null) {
                e = this.getEntry(name.toLowerCase());
                if (e == null) {
                    e = this.getEntry(name.toUpperCase());
                    if (e == null) {
                        // I give up.  Return null.
                        return (null);
                    }
                }
            }
            rv = getInputStream(this.getEntry(name));
        } catch (IOException e) {
            log.error("IOException caught " + e);
            rv = null;
        } catch (NullPointerException ne) {
            log.error("Null Pointer Exception caught. name=" + name, ne);
            rv = null;
        }
        return (rv);
    }

    public java.io.File getFile(String name) {
        try {
            ZipEntry e = this.getEntry(name);

            File f = new File(e.getName());
            return (f);
        } catch (NullPointerException e) {
            return (null);
        }

    }

    public String getURL(String name) {
        try {
            // Grab the entry from the Zip file, and create a tempfile to dump it into
            ZipEntry e = this.getEntry(name);
            File t = File.createTempFile(name, ".wav.tmp");
            t.deleteOnExit();

            // Dump the file from the Zip into the tempfile.
            copyInputStream(this.getInputStream(e), new BufferedOutputStream(new FileOutputStream(t)));

            // return the name of the tempfile.
            return (t.getPath());

        } catch (NullPointerException e) {
            log.warn("Null pointer exception", e);
            return (null);
        } catch (IOException e) {
            log.warn("IO exception", e);
            return (null);
        }
    }

    private static final void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }

    static class ValidateStatus {

        String msg = "";
        Boolean valid = false;

        public ValidateStatus() {
            this(false, "");
        }

        public ValidateStatus(Boolean v, String m) {
            valid = v;
            msg = m;
        }

        public void setValid(Boolean v) {
            valid = v;
        }

        public void setMessage(String m) {
            msg = m;
        }

        public Boolean getValid() {
            return (valid);
        }

        public String getMessage() {
            return (msg);
        }
    }

    public ValidateStatus validate(Element xmlroot) {
        Element e, el;
        // Iterate through all the profiles in the file.
        // Would like to get rid of this suppression, but I think it's fairly safe to assume a list of children
        // returned from an Element is going to be a list of Elements.
        Iterator<Element> i = xmlroot.getChildren("profile").iterator();
        // If no Profiles, file is invalid.
        if (!i.hasNext()) {

            log.warn("Validate: No Profiles.");
            return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusNoProfiles")));
        }

        // Iterate through Profiles
        while (i.hasNext()) {
            e = i.next(); // e points to a profile.
            log.debug("Validate: Profile " + e.getAttributeValue("name"));
            // Get the "Sound" children ... these are the ones that should have files
            // Would like to get rid of this suppression, but I think it's fairly safe to assume a list of children
            // returned from an Element is going to be a list of Elements.
            Iterator<Element> i2 = (e.getChildren("sound")).iterator();
            if (!i2.hasNext()) {
                log.warn("Validate: Profile " + e.getAttributeValue("name") + " has no Sounds");
                return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusNoSounds") + " : " + e.getAttributeValue("name")));
            }

            // Iterate through Sounds
            while (i2.hasNext()) {
                el = i2.next();
                log.debug("Element: " + el.toString());
                if (el.getAttribute("name") == null) {
                    log.debug("Name missing.");
                    return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusNoName") + " : " + e.getAttributeValue("name") + "(" + el.getName() + ")"));
                }
                String type = el.getAttributeValue("type");
                log.debug("  Name: " + el.getAttributeValue("name"));
                log.debug("   type: " + type);
                if (type.equals("configurable")) {
                    // Validate a Configurable Sound
                    // All these elements are optional, so if the element is missing,
                    // that's OK.  But if there is an element, and the FILE is missing,
                    // that's bad.
                    if (!validateOptionalFile(el, "start-file")) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingElement") + " : <start-file>"));
                    }
                    if (!validateOptionalFile(el, "mid-file")) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingElement") + " : <mid-file>"));
                    }
                    if (!validateOptionalFile(el, "end-file")) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingElement") + " : <end-file>"));
                    }
                    if (!validateOptionalFile(el, "short-file")) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingElement") + " : <short-file>"));
                    }
                } else if (type.equals("diesel")) {
                    // Validate a Diesel sound
                    // All these elements are optional, so if the element is missing,
                    // that's OK.  But if there is an element, and the FILE is missing,
                    // that's bad.
                    String[] file_elements = {"file"};
                    if (!validateOptionalFile(el, "start-file")) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingSoundFile") + " : <start-file>"));
                    }
                    if (!validateOptionalFile(el, "shutdown-file")) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingSoundFile") + " : <shutdown-file>"));
                    }
                    if (!validateFiles(el, "notch-sound", file_elements)) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingSoundFile") + " : <notch-sound>"));
                    }
                    if (!validateFiles(el, "notch-transition", file_elements, false)) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingSoundFile") + " : <notch-transition>"));
                    }
                } else if (type.equals("diesel2")) {
                    // Validate a diesel2 type sound
                    // Validate a Diesel sound
                    // All these elements are optional, so if the element is missing,
                    // that's OK.  But if there is an element, and the FILE is missing,
                    // that's bad.
                    String[] file_elements = {"file", "accel-file", "decel-file"};
                    if (!validateOptionalFile(el, "start-file")) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingSoundFile") + " : <start-file>"));
                    }
                    if (!validateOptionalFile(el, "shutdown-file")) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingSoundFile") + " : <shutdown-file>"));
                    }
                    if (!validateFiles(el, "notch-sound", file_elements)) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingSoundFile") + " : <notch-sound>"));
                    }
                } else if (type.equals("steam")) {
                    // Validate a Steam sound
                    String[] file_elements = {"file"};
                    if (!validateRequiredElement(el, "top-speed")) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingElement") + " : <top-speed>"));
                    }
                    if (!validateRequiredElement(el, "driver-diameter")) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingElement") + " : <driver-diameter>"));
                    }
                    if (!validateRequiredElement(el, "cylinders")) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingElement") + " : <cylinders>"));
                    }
                    if (!validateRequiredElement(el, "rpm-steps")) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingElement") + " : <rpm-steps>"));
                    }
                    if (!validateFiles(el, "rpm-step", file_elements)) {
                        return (new ValidateStatus(false, Bundle.getMessage("VSDFileStatusMissingSoundFile") + " : <notch-sound>"));
                    }
                }
            }
        }
        log.debug("File Validation Successful.");
        return (new ValidateStatus(true, Bundle.getMessage("ButtonOK"))); //  File Status = OK
    }

    protected boolean validateRequiredElement(Element el, String name) {
        if (el.getChild(name) == null) {
            log.debug("Element " + name + " for Element " + el.getAttributeValue("name") + " missing.");
            return (false);
        }
        return (true);
    }

    protected boolean validateOptionalFile(Element el, String name) {
        return (validateOptionalFile(el, name, true));
    }

    protected boolean validateOptionalFile(Element el, String name, Boolean required) {
        String s = el.getChildText(name);
        if ((s != null) && (getFile(s) == null)) {
            log.debug("File " + s + " for element " + name + " in Element " + el.getAttributeValue("name") + " not found.");
            return (false);
        }
        return (true);
    }

    protected boolean validateFiles(Element el, String name, String[] fnames) {
        return (validateFiles(el, name, fnames, true));
    }

    protected boolean validateFiles(Element el, String name, String[] fnames, Boolean required) {
        List<Element> elist = el.getChildren(name);
        String s;

        // First, check to see if any elements of this <name> exist.
        if ((elist.size() == 0) && (required)) {
            // Only fail if this type of element is required.
            log.debug("No elements of name " + name);
            return (false);
        }

        // Now, if the elements exist, make sure the files they point to exist.
        // Would like to get rid of this suppression, but I think it's fairly safe to assume a list of children
        // returned from an Element is going to be a list of Elements.
        Iterator<Element> ns_i = elist.iterator();
        while (ns_i.hasNext()) {
            Element ns_e = ns_i.next();
            for (String fn : fnames) {
                s = ns_e.getChildText(fn);
                if ((s == null) || (getFile(s) == null)) {
                    log.debug("File " + s + " for element " + fn + " in Element " + ns_e.getAttributeValue("name") + " not found.");
                    return (false);
                }
            }
        }
        // Made it this far, all is well.
        return (true);
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(VSDFile.class);

}
