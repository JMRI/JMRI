package jmri.web.servlet.panel;

import java.io.StringReader;
import java.util.List;

import jmri.InstanceManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the jmri.web.servlet.panel.ControlPanelServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 * @author Egbert Broerse Copyright (C) 2020
 */
public class ControlPanelServletTest {

    @Test
    public void testCtor() {
        ControlPanelServlet a = new ControlPanelServlet();
        assertNotNull(a);
    }

    @Test
    public void testIndicatorTrackIconElement() {
        String systemName = "OB1";
        String userName = "Internal OBlock 1";
        ControlPanelServlet servlet = new NullControlPanelServlet();
        OBlock ob = InstanceManager.getDefault(OBlockManager.class).provide(systemName);
        ob.setUserName(userName);
        IndicatorTrackIcon iti = new IndicatorTrackIcon(null);
        iti.setOccBlock(ob.getUserName());
        Element e = ConfigXmlManager.elementFromObject(iti);
        assertEquals(userName, e.getChild("occupancyblock").getValue());
        e = servlet.positionableElement(iti);
        assertEquals(userName, e.getChild("occupancyblock").getValue());
        //System.out.println(e.getChild("oblocksysname").toString()); // servlet is not adding anything to e
        //assertEquals(systemName, e.getChild("oblocksysname").getValue()); / Child (element) not found, NPE
    }

    /**
     * The served Control Panel XML must list positionables in ascending display
     * level (z-order) so the web client, which draws elements in document order,
     * reproduces the desktop stacking order. See JMRI/JMRI#12794.
     */
    @Test
    @DisabledIfHeadless
    public void testContentsSortedByDisplayLevel() throws Exception {
        String panelName = "CP Level Order Test";
        ControlPanelEditor editor = new ControlPanelEditor(panelName);
        try {
            // add in reverse z-order (highest level first) on purpose
            addLabel(editor, "high", Editor.MARKERS); // level 10
            addLabel(editor, "low", Editor.TURNOUTS); // level 7

            List<Element> labels = positionableLabels(
                    new EditorBoundServlet(editor, panelName).getXmlPanel(panelName));
            int idxLow = indexOfLabel(labels, "low");
            int idxHigh = indexOfLabel(labels, "high");
            assertTrue(idxLow >= 0 && idxHigh >= 0, "both labels emitted");
            assertTrue(idxLow < idxHigh,
                    "lower display level must be emitted before higher display level");

            // the emitted level attributes themselves must be non-decreasing
            int prev = Integer.MIN_VALUE;
            for (Element label : labels) {
                int level = Integer.parseInt(label.getAttributeValue("level"));
                assertTrue(level >= prev, "emitted levels must be non-decreasing");
                prev = level;
            }
        } finally {
            JUnitUtil.dispose(editor);
        }
    }

    /**
     * Two positionables sharing the same display level must keep their original
     * insertion order. Guards against an accidental unstable sort.
     */
    @Test
    @DisabledIfHeadless
    public void testSameDisplayLevelStable() throws Exception {
        String panelName = "CP Level Stability Test";
        ControlPanelEditor editor = new ControlPanelEditor(panelName);
        try {
            addLabel(editor, "first", Editor.LABELS); // level 4
            addLabel(editor, "second", Editor.LABELS); // level 4

            List<Element> labels = positionableLabels(
                    new EditorBoundServlet(editor, panelName).getXmlPanel(panelName));
            int idxFirst = indexOfLabel(labels, "first");
            int idxSecond = indexOfLabel(labels, "second");
            assertTrue(idxFirst >= 0 && idxSecond >= 0, "both labels emitted");
            assertTrue(idxFirst < idxSecond,
                    "same-level elements must retain insertion order (stable sort)");
        } finally {
            JUnitUtil.dispose(editor);
        }
    }

    private void addLabel(Editor editor, String text, int level) throws Exception {
        PositionableLabel l = new PositionableLabel(text, editor);
        editor.putItem(l);
        l.setDisplayLevel(level);
    }

    private List<Element> positionableLabels(String xml) throws Exception {
        assertNotNull(xml, "panel xml produced");
        Element panel = new SAXBuilder().build(new StringReader(xml)).getRootElement();
        return panel.getChildren("positionablelabel");
    }

    private int indexOfLabel(List<Element> labels, String text) {
        for (int i = 0; i < labels.size(); i++) {
            if (text.equals(labels.get(i).getAttributeValue("text"))) {
                return i;
            }
        }
        return -1;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        // ControlPanelEditor instances created by the z-order tests register a
        // BlockManager shutdown task; deregister it so the harness tearDown check
        // does not flag a leftover shutdown callable.
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    private static class NullControlPanelServlet extends ControlPanelServlet {

        @Override
        protected String getPanelType() {
            return "ControlPanel";
        }

        @Override
        protected String getJsonPanel(String name) {
            return null;
        }

        @Override
        protected String getXmlPanel(String name) {
            return null;
        }

    }

    /**
     * Test servlet that resolves {@code getEditor(name)} to a known editor so
     * {@link ControlPanelServlet#getXmlPanel(String)} can be exercised directly
     * without depending on frame-title lookup timing.
     */
    private static class EditorBoundServlet extends ControlPanelServlet {

        private final Editor boundEditor;
        private final String boundName;

        EditorBoundServlet(Editor editor, String name) {
            this.boundEditor = editor;
            this.boundName = name;
        }

        @Override
        protected Editor getEditor(String n) {
            return boundName.equals(n) ? boundEditor : super.getEditor(n);
        }
    }

}
