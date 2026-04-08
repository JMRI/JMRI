package jmri.jmrit.display.layoutEditor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.display.layoutEditor.configurexml.LayoutEditorXml;

import org.jdom2.*;

/**
 *
 * @author Dave Sand Copyright (C) 2026
 */
public class CheckPointManager implements InstanceManagerAutoDefault {

    public CheckPointManager() {
    }

    private HashMap<LayoutEditor, Panel> _panelMap = new HashMap<>();

    /**
     * Create a checkpoint for the requested LE panel
     * @param lepanel The LE panel instance.
     */
    void createCheckPoint(LayoutEditor lepanel) {
        // Get the current time
        var ldt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        var key = ldt.toString().replace("T", " ");

        // Get the current xml content for the panel
        var lex = new LayoutEditorXml();
        Element xml = lex.store(lepanel);

        if (log.isDebugEnabled()) {
            log.debug("XML Attributes and Elements:");
            for (Attribute at : xml.getAttributes()) {
                log.debug("  {}", at);
            }
            for (Element el : xml.getChildren()) {
                log.debug("    {}", el);
            }
        }

        // Create the checkpoint
        Panel panel = _panelMap.get(lepanel);
        if (panel == null) {
            panel = new Panel(lepanel);
            _panelMap.put(lepanel, panel);
        }
        panel.addCheckPoint(key, xml);

        if (log.isDebugEnabled()) {
            log.debug("Current key list:");
            for (String datetime : panel.getCheckPointKeys()) {
                log.debug("  {}", datetime);
            }
        }
    }

    /**
     * Get the existing keys for the specified panel.  The list
     * will be used to create the check point menu items.
     * @param lepanel The LE panel instance.
     * @return a list of keys in descending sequence.
     */
    List<String> getCheckPointList(LayoutEditor lepanel) {
        List<String> keyList = new ArrayList<>();
        Panel panel = _panelMap.get(lepanel);
        if (panel != null) {
            keyList = panel.getCheckPointKeys();
        }
        return keyList;
    }

    /**
     * Delete the current panel and create a new one based on the checkpoint key.
     * @param lepanel The panel to be deleted.
     * @param key The date time key for the checkpoint to be the replacement.
     */
    void revertPanel(LayoutEditor lepanel, String key) {
        Panel panel = _panelMap.get(lepanel);
        if (panel == null) {
            log.error("Unexpected error for panel {}", lepanel.getTitle());
            return;
        }

        if (lepanel.deletePanel()) {
            lepanel.dispose();
            var lex = new LayoutEditorXml();
            lex.load(panel.getPanelData(key), null);
        }
    }

    /**
     * For each Layout Editor panel, create a tree map that
     * uses the current date and time for a key and the xml
     * data for the panel.
     */
    class Panel {
        LayoutEditor _lepanel;
        TreeMap<String, Element> _checkPoints;

        Panel(LayoutEditor lepanel) {
            _lepanel = lepanel;
            _checkPoints = new TreeMap<>();
        }

        /**
         * Add a new entry to the tree map.
         * @param datetime The date and time to be used for the key.
         * @param element The XML data for the panel.
         */
        void addCheckPoint(String datetime, Element element) {
            _checkPoints.put(datetime, element);
        }

        /**
         * Get the XML data for the specified date/time key.
         * @param datetime The Date/time key.
         * @return the previous xml data for the panel.
         */
        Element getPanelData(String datetime) {
            return _checkPoints.get(datetime);
        }

        /**
         * @return a list of the checkpoint times in descending sequence.
         */
        List<String> getCheckPointKeys() {
            var list = new ArrayList<String>();
            for (String datetime : _checkPoints.descendingKeySet()) {
                list.add(datetime);
            }
            return list;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CheckPointManager.class);
}
