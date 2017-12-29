package jmri.jmrit.display.layoutEditor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jmri.BasicRosterEntry;
import jmri.Block;
import jmri.BlockManager;
import jmri.Path;
import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.FileUtil;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle saving/restoring block value information to XML files. This class
 * manipulates files conforming to the block_value DTD.
 *
 * @author Dave Duchamp Copyright (C) 2008
 */
public class BlockValueFile extends XmlFile {

    /**
     * {@inheritDoc}
     */
    public BlockValueFile() {
        super();
        blockManager = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
    }

    // operational variables
    private BlockManager blockManager = null;
    private final static String defaultFileName = FileUtil.getUserFilesPath() + "blockvalues.xml";
    private Document doc = null;
    private Element root = null;

    /**
     *  Reads Block values from a file in the user's preferences directory.
     *  If the file containing block values does not exist this routine returns quietly.
     *  If a Block named in the file does not exist currently, that entry is quietly ignored.
     * 
     * @throws JDOMException on rootFromName if all methods fail
     * @throws IOException if an I/O error occurs while reading a file
     */
    @SuppressWarnings("unchecked")
    public void readBlockValues() throws JDOMException, IOException {
        log.debug("entered readBlockValues");
        List<String> blocks = blockManager.getSystemNameList();
        // check if file exists
        if (checkFile(defaultFileName)) {
            // file is present, 
            root = rootFromName(defaultFileName);
            if ((root != null) && (blocks.size() > 0)) {
                // there is a file and there are Blocks defined
                Element blockvalues = root.getChild("blockvalues");
                if (blockvalues != null) {
                    // there are values defined, read and set block values if Block exists.
                    List<Element> blockList = blockvalues.getChildren("block");
                    for (int i = 0; i < blockList.size(); i++) {
                        if ((blockList.get(i)).getAttribute("systemname") == null) {
                            log.warn("unexpected null in systemName "
                                    + blockList.get(i) + " "
                                    + blockList.get(i).getAttributes());
                            break;
                        }
                        String sysName = blockList.get(i).
                                getAttribute("systemname").getValue();
                        // get Block - ignore entry if block not found
                        Block b = blockManager.getBySystemName(sysName);
                        if (b != null) {
                            // Block was found, set its value
                            Object v = blockList.get(i).
                                    getAttribute("value").getValue();
                            if (blockList.get(i).getAttribute("valueClass") != null) {
                                if (blockList.get(i).getAttribute("valueClass").getValue().equals("jmri.jmrit.roster.RosterEntry")) {
                                    RosterEntry re = Roster.getDefault().getEntryForId(((String) v));
                                    if (re != null) {
                                        v = re;
                                    }
                                }
                            }
                            b.setValue(v);
                            // set direction if there is one
                            int dd = Path.NONE;
                            Attribute a = blockList.get(i).getAttribute("dir");
                            if (a != null) {
                                try {
                                    dd = a.getIntValue();
                                } catch (DataConversionException e) {
                                    log.error("failed to convert direction attribute");
                                }
                            }
                            b.setDirection(dd);
                        }
                    }
                }
            }
        }
    }

    /*
     *  Writes out block values to a file in the user's preferences directory
     *  If there are no defined Blocks, no file is written.
     *  If none of the defined Blocks have values, no file is written.
     * 
     * @throws IOException 
     */
    public void writeBlockValues() throws IOException {
        log.debug("entered writeBlockValues");
        List<String> blocks = blockManager.getSystemNameList();
        if (blocks.size() > 0) {
            // there are blocks defined, create root element
            root = new Element("block_values");
            doc = newDocument(root, dtdLocation + "block-values.dtd");
            boolean valuesFound = false;

            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/block-values.xsl"?>
            Map<String, String> m = new HashMap<>();
            m.put("type", "text/xsl");
            m.put("href", xsltLocation + "blockValues.xsl");
            ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
            doc.addContent(0, p);

            // save block values in xml format
            Element values = new Element("blockvalues");
            for (int i = 0; i < blocks.size(); i++) {
                String sname = blocks.get(i);
                Block b = blockManager.getBySystemName(sname);
                if (b != null) {
                    Object o = b.getValue();
                    if (o != null) {
                        // block has value, save it
                        Element val = new Element("block");
                        val.setAttribute("systemname", sname);
                        if (o instanceof RosterEntry) {
                            val.setAttribute("value", ((BasicRosterEntry) o).getId());
                            val.setAttribute("valueClass", "jmri.jmrit.roster.RosterEntry");
                        } else {
                            val.setAttribute("value", o.toString());
                        }
                        int v = b.getDirection();
                        if (v != Path.NONE) {
                            val.setAttribute("dir", "" + v);
                        }
                        values.addContent(val);
                        valuesFound = true;
                    }
                } else {
                    log.error("Block " + sname + " was not found.");
                }
            }
            root.addContent(values);

            // write out the file if values were found
            if (valuesFound) {
                try {
                    if (!checkFile(defaultFileName)) {
                        // file does not exist, create it
                        File file = new File(defaultFileName);
                        if (!file.createNewFile()) // create and check result
                        {
                            log.error("createNewFile failed");
                        }
                    }
                    // write content to file
                    writeXML(findFile(defaultFileName), doc);
                } catch (IOException ioe) {
                    log.error("IO Exception " + ioe);
                    throw (ioe);
                }
            }
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(BlockValueFile.class);

}
