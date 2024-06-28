package jmri.jmrit.display.layoutEditor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import jmri.BasicRosterEntry;
import jmri.Block;
import jmri.BlockManager;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.Path;
import jmri.jmrit.XmlFile;
import jmri.jmrit.beantable.BlockTableAction.RestoreRule;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.FileUtil;
import jmri.PowerManager;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;

/**
 * Handle saving/restoring block value information to XML files. This class
 * manipulates files conforming to the block_value DTD.
 *
 * @author Dave Duchamp Copyright (C) 2008
 * @author George Warner Copyright (c) 2017-2018
 */
public class BlockValueFile extends XmlFile {

    public BlockValueFile() {
        super();
        blockManager = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
    }

    // operational variables
    private BlockManager blockManager = null;
    private final static String defaultFileName = FileUtil.getUserFilesPath() + "blockvalues.xml";
    private Element root = null;

    /**
     * Reads Block values from a file in the user's preferences directory. If
     * the file containing block values does not exist this routine returns
     * quietly. If a Block named in the file does not exist currently, that
     * entry is quietly ignored.
     *
     * @throws JDOMException on rootFromName if all methods fail
     * @throws IOException   if an I/O error occurs while reading a file
     */
    public void readBlockValues() throws JDOMException, IOException {
        log.debug("entered readBlockValues");
        List<String> blocks = new ArrayList<>(blockManager.getNamedBeanSet().size());
        blockManager.getNamedBeanSet().forEach(bean -> {
            blocks.add(bean.getSystemName());
        });
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
                    
                    RestoreRule rr = jmri.jmrit.beantable.BlockTableAction.getRestoreRule();
                    
                    // check if all powermanagers are turned on, occupancy is meaningless without track power
                    boolean allPoweredUp = true;
                    for (PowerManager pm : jmri.InstanceManager.getList(PowerManager.class)) {
                        if (pm.getPower() != jmri.PowerManager.ON) {
                            allPoweredUp = false;
                        }
                    }

                    //if power is ON and "All Occupied" is checked, bail if any not found
                    if (rr==RestoreRule.RESTOREONLYIFALLOCCUPIED && allPoweredUp) {
                        for (Element bl : blockList) {
                            if (bl.getAttribute("systemname") != null) {
                                String sysName = bl.getAttribute("systemname").getValue();
                                Block b = blockManager.getBySystemName(sysName);
                                if (b != null) {
                                    if (b.getState() != Block.OCCUPIED) {
                                        log.error("block {} unoccupied but has saved value, not setting any block values, rule={}",
                                                b.getDisplayName(), rr);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    
                    //now set the values
                    int blks = 0;
                    int sets = 0; 
                    for (Element bl : blockList) {
                        if (bl.getAttribute("systemname") == null) {
                            log.warn("unexpected null in systemName {} {}", bl, bl.getAttributes());
                            break;
                        }
                        String sysName = bl.getAttribute("systemname").getValue();
                        // get Block - ignore entry if block not found
                        Block b = blockManager.getBySystemName(sysName);
                        if (b != null) {
                            blks++;
                            if (rr == RestoreRule.RESTOREALWAYS || !allPoweredUp || b.getState()==Block.OCCUPIED) {
                                Object v = bl.getAttribute("value").getValue();
                                if (bl.getAttribute("valueClass") != null) {
                                    if (bl.getAttribute("valueClass").getValue().equals("jmri.jmrit.roster.RosterEntry")) {
                                        RosterEntry re = Roster.getDefault().getEntryForId(((String) v));
                                        if (re != null) {
                                            v = re;
                                        }
                                    }
                                    if (bl.getAttribute("valueClass").getValue().equals("jmri.IdTag")) {
                                        var tag = jmri.InstanceManager.getDefault(IdTagManager.class).getIdTag((String) v);
                                        if (tag != null) {
                                            v = tag;
                                        }
                                    }
                                }
                                b.setValue(v);
                                sets++;
                                // set direction if there is one
                                int dd = Path.NONE;
                                Attribute a = bl.getAttribute("dir");
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
                    log.info("{} of {} block values restored. Rule={}, Power={}", sets, blks, rr, (allPoweredUp?"ON":"OFF"));
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
        if (blockManager.getNamedBeanSet().size() > 0) {
            // there are blocks defined, create root element
            root = new Element("block_values");
            Document doc = newDocument(root, dtdLocation + "block-values.dtd");
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
            
            for (Block b : blockManager.getNamedBeanSet()) {
                if (b != null) {
                    Object o = b.getValue();
                    if (o != null) {
                        // block has value, save it
                        Element val = new Element("block");
                        val.setAttribute("systemname", b.getSystemName());
                        if (o instanceof RosterEntry) {
                            val.setAttribute("value", ((BasicRosterEntry) o).getId());
                            val.setAttribute("valueClass", "jmri.jmrit.roster.RosterEntry");
                        } else if (o instanceof IdTag) {
                            val.setAttribute("value", ((IdTag) o).getSystemName());
                            val.setAttribute("valueClass", "jmri.IdTag");
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
                    log.error("Block null in blockManager.getNamedBeanSet()");
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
                    log.error("While writing block value file ", ioe);
                    throw (ioe);
                }
            }
        }
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BlockValueFile.class);

}
