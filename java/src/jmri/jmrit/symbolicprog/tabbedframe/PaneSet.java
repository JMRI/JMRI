package jmri.jmrit.symbolicprog.tabbedframe;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.ResetTableModel;
import jmri.jmrit.symbolicprog.SymbolicProgBundle;
import jmri.jmrit.symbolicprog.VariableTableModel;
import org.jdom2.*;
import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for the container of a set of PaneProgPanes. The panes use services
 * provided here to work with buttons and the busy cursor.
 * <p>
 * TODO: Several methods are copied from PaneProgFrame and should be refactored
 * No programmer support yet No glass pane support Need better support for
 * visible/non-visible panes Special panes (Roster entry, attributes, graphics)
 * not included
 *
 * @see apps.gui3.dp3.DecoderPro3Window
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class PaneSet {

    List<PaneProgPane> paneList = new ArrayList<>();
    PaneContainer container;
    Programmer mProgrammer;
    CvTableModel cvModel = null;
    VariableTableModel variableModel;
    ResetTableModel resetModel = null;
    JLabel progStatus = new JLabel(SymbolicProgBundle.getMessage("StateIdle"));

    /**
     * The 'model' element representing the decoder type
     */
    Element modelElem = null;

    public PaneSet(PaneContainer container, RosterEntry re, Programmer programmer) {
        this.container = container;
        this.mProgrammer = programmer;

        cvModel = new CvTableModel(progStatus, mProgrammer);

        variableModel = new VariableTableModel(progStatus, new String[]{"Name", "Value"},
                cvModel);

        resetModel = new ResetTableModel(progStatus, mProgrammer);

        if (re.getFileName() != null) {
            // set the loco file name in the roster entry
            re.readFile();  // read, but don't yet process
        }

        // load from decoder file
        loadDecoderFromLoco(re);

        // finally fill the Variable and CV values from the specific loco file
        if (re.getFileName() != null) {
            re.loadCvModel(variableModel, cvModel);
        }
    }

    // copied from PaneProgFrame
    protected void loadDecoderFromLoco(RosterEntry r) {
        // get a DecoderFile from the locomotive xml
        String decoderModel = r.getDecoderModel();
        String decoderFamily = r.getDecoderFamily();
        if (log.isDebugEnabled()) {
            log.debug("selected loco uses decoder " + decoderFamily + " " + decoderModel);
        }
        // locate a decoder like that.
        List<DecoderFile> l = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, decoderFamily, null, null, null, decoderModel);
        if (log.isDebugEnabled()) {
            log.debug("found " + l.size() + " matches");
        }
        if (l.size() == 0) {
            log.debug("Loco uses " + decoderFamily + " " + decoderModel + " decoder, but no such decoder defined");
            // fall back to use just the decoder name, not family
            l = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, null, null, null, null, decoderModel);
            if (log.isDebugEnabled()) {
                log.debug("found " + l.size() + " matches without family key");
            }
        }
        if (l.size() > 0) {
            DecoderFile d = l.get(0);
            loadDecoderFile(d, r);
        } else {
            if (decoderModel.equals("")) {
                log.debug("blank decoderModel requested, so nothing loaded");
            } else {
                log.warn("no matching \"" + decoderModel + "\" decoder found for loco, no decoder info loaded");
            }
        }
    }

    protected void loadDecoderFile(DecoderFile df, RosterEntry re) {
        if (df == null) {
            log.warn("loadDecoder file invoked with null object");
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("loadDecoderFile from " + DecoderFile.fileLocation
                    + " " + df.getFileName());
        }

        try {
            decoderRoot = df.rootFromName(DecoderFile.fileLocation + df.getFileName());
        } catch (JDOMException | IOException e) {
            log.error("Exception while loading decoder XML file: " + df.getFileName(), e);
        }
        // load variables from decoder tree
        df.getProductID();
        df.loadVariableModel(decoderRoot.getChild("decoder"), variableModel);

        // load reset from decoder tree
        df.loadResetModel(decoderRoot.getChild("decoder"), resetModel);

        // load function names
        re.loadFunctions(decoderRoot.getChild("decoder").getChild("family").getChild("functionlabels"));

//         // get the showEmptyPanes attribute, if yes/no update our state
//         if (decoderRoot.getAttribute("showEmptyPanes") != null) {
//             if (log.isDebugEnabled()) log.debug("Found in decoder "+decoderRoot.getAttribute("showEmptyPanes").getValue());
//             if (decoderRoot.getAttribute("showEmptyPanes").getValue().equals("yes"))
//                 setShowEmptyPanes(true);
//             else if (decoderRoot.getAttribute("showEmptyPanes").getValue().equals("no"))
//                 setShowEmptyPanes(false);
//             // leave alone for "default" value
//             if (log.isDebugEnabled()) log.debug("result "+getShowEmptyPanes());
//         }
        // save the pointer to the model element
        modelElem = df.getModelElement();
    }
    Element decoderRoot = null;

    /**
     * Create a set of panes from a programmer definition and roster entry
     *
     * @param root Root element of programmer XML definition
     * @param r    Locomotive to load from
     */
    public void makePanes(Element root, RosterEntry r) {
        // check for "programmer" element at start
        Element base;
        if ((base = root.getChild("programmer")) == null) {
            log.error("xml file top element is not programmer");
            return;
        }

        List<Element> paneList = base.getChildren("pane");

        if (log.isDebugEnabled()) {
            log.debug("will process " + paneList.size() + " pane definitions");
        }

        for (Element e : paneList) {
            // load each pane
            String name = e.getAttribute("name").getValue();
            newPane(name, e, modelElem, r);
        }
    }

    /**
     * Create a single pane from a "pane" element in programmer or decoder
     * definition
     */
    public void newPane(String name, Element pane, Element modelElem, RosterEntry r) {
        if (log.isDebugEnabled()) {
            log.debug("newPane " + name);
        }
        // create a panel to hold columns
        PaneProgPane p = new PaneProgPane(container, name, pane, cvModel, variableModel, modelElem, r);

        // and remember it for programming
        paneList.add(p);
    }

    public List<PaneProgPane> getList() {
        return paneList;
    }

    /**
     * Store current content to file
     */
    public void storeFile(RosterEntry re) {
        // set up file write
        re.ensureFilenameExists();

        // write the RosterEntry to its file
        re.writeFile(cvModel, variableModel);
    }

    private final static Logger log = LoggerFactory.getLogger(PaneSet.class);
}
