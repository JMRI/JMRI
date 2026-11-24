package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import jmri.TrainRecord.DetectionMarker;
import jmri.TrainRecord.DetectionMarker.Placement;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * A train's own detection markers: kept in its train info file, and edited
 * in {@link DetectionMarkersPanel}.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DetectionMarkersTest {

    private static final List<DetectionMarker> MARKERS = List.of(DetectionMarker.front(220.0),
            DetectionMarker.atOffset(600.0, 250.0), DetectionMarker.atOffset(null, 180.0), DetectionMarker.rear(null));

    @Test
    public void testKeptInTheTrainInfoFile(@TempDir File dir) throws Exception {
        TrainInfoFile file = new TrainInfoFile();
        file.setFileLocation(dir.getPath() + File.separator);
        TrainInfo info = new TrainInfo();
        info.setTransitName("T1");
        info.setTrainName("1A23");
        info.setDccAddress("3");
        info.setStartBlockName("B1");
        info.setDestinationBlockName("B2");
        info.setDetectionMarkers(MARKERS);
        file.writeTrainInfo(info, "markers.xml");
        assertEquals(MARKERS, file.readTrainInfo("markers.xml").getDetectionMarkers(), "read back as written");

        info.setDetectionMarkers(List.of());
        file.writeTrainInfo(info, "none.xml");
        assertTrue(file.readTrainInfo("none.xml").getDetectionMarkers().isEmpty(), "none written, none read");
    }

    @Test
    public void testMarkersThatCantBeReadAreDropped() {
        Element markers = new Element("detectionmarkers");
        markers.addContent(new Element("marker").setAttribute("placement", "FRONT").setAttribute("length", "-5"));
        assertTrue(TrainInfoFile.readMarkers(markers).isEmpty(), "Train Detection is used instead");
        JUnitAppender.assertWarnMessageStartingWith("Detection markers in the train info file can't be read");
        assertTrue(TrainInfoFile.readMarkers(null).isEmpty(), "no element, no markers");
    }

    @Test
    public void testPanelEditsMarkers() {
        DetectionMarkersPanel panel = new DetectionMarkersPanel(MARKERS);
        DetectionMarkersPanel.MarkerTableModel model = panel.model;
        assertEquals(MARKERS, panel.getMarkers(), "shown as given");
        assertEquals(4, model.getRowCount());
        assertFalse(model.isCellEditable(0, DetectionMarkersPanel.MarkerTableModel.OFFSET),
                "only a marker at an offset has one");
        assertTrue(model.isCellEditable(1, DetectionMarkersPanel.MarkerTableModel.OFFSET));

        // the second placed at the rear instead: its offset goes
        model.setValueAt(new DetectionMarkersPanel.PlacementItem(Placement.REAR), 1,
                DetectionMarkersPanel.MarkerTableModel.PLACEMENT);
        assertEquals(DetectionMarker.rear(250.0), panel.getMarkers().get(1));

        // a new row, with a length that isn't one, is left out
        model.addRow();
        model.setValueAt(0.0, 4, DetectionMarkersPanel.MarkerTableModel.LENGTH);
        assertEquals(4, panel.getMarkers().size(), "a zero length makes no marker");
        model.setValueAt(300.0, 4, DetectionMarkersPanel.MarkerTableModel.LENGTH);
        assertEquals(DetectionMarker.atOffset(null, 300.0), panel.getMarkers().get(4));

        model.removeRow(0);
        assertEquals(4, panel.getMarkers().size());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
