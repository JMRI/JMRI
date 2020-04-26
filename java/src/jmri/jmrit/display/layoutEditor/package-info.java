/**
 * The Layout Editor represents both structure and graphical display through a single set of objects.
 * <p>
 * A small sample layout:<br>
 * <a href="doc-files/SidingImage.png"><img src="doc-files/SidingImage.png" alt="Simple layout example" height="25%" width="25%"></a>
 * <h3>Connectivity</h3>
 * This is coded and stored as the following: (Some graphical attributes removed, reordered)<br>
<pre>
    &lt;layoutturnout ident="TO1" type="1" continuing="2" ver="1" connectaname="T3" connectbname="T2" connectcname="T1" class="jmri.jmrit.display.layoutEditor.configurexml.LayoutTurnoutXml" /&gt;
    &lt;layoutturnout ident="TO2" type="2" continuing="2" ver="1" connectaname="T4" connectbname="T2" connectcname="T1" class="jmri.jmrit.display.layoutEditor.configurexml.LayoutTurnoutXml" /&gt;
    &lt;tracksegment ident="T1" connect1name="TO1" type1="TURNOUT_C" connect2name="TO2" type2="TURNOUT_C" class="jmri.jmrit.display.layoutEditor.configurexml.TrackSegmentXml" /&gt;
    &lt;tracksegment ident="T2" connect1name="TO1" type1="TURNOUT_B" connect2name="TO2" type2="TURNOUT_B" class="jmri.jmrit.display.layoutEditor.configurexml.TrackSegmentXml" /&gt;
    &lt;tracksegment ident="T3" connect1name="EB1" type1="POS_POINT" connect2name="TO1" type2="TURNOUT_A" class="jmri.jmrit.display.layoutEditor.configurexml.TrackSegmentXml" /&gt;
    &lt;tracksegment ident="T4" connect1name="TO2" type1="TURNOUT_A" connect2name="EC1" type2="POS_POINT" class="jmri.jmrit.display.layoutEditor.configurexml.TrackSegmentXml" /&gt;
    &lt;positionablepoint ident="EB1" type="2" connect1name="T3" class="jmri.jmrit.display.layoutEditor.configurexml.PositionablePointXml" /&gt;
    &lt;positionablepoint ident="EC1" type="3" connect1name="T4" linkedpanel="" linkpointid="" class="jmri.jmrit.display.layoutEditor.configurexml.PositionablePointXml" /&gt;
</pre>
<p>
 * <a href="doc-files/SidingConnections.png"><img src="doc-files/SidingConnections.png" alt="Example interconnections" height="33%" width="33%"></a>
 * <p>
 * For TrackSegment objects, a connection is represented by the name of the other end, and the type (in <code>type2</code> or <code>type2</code>) of the connection at the other end.
 * For example, a connection in object T1 to a turnout might give the turnouts TO1 name, and that the connection is to the TURNOUT_C leg of that turnout.
 * <p>
 * Connections to END BUMPERs and EDGE CONNECTORs don't quite fit that.  The far end sees the connection to the EB and EC as to a POS_POINT, not 
 * specifically a END BUMPER or EDGE CONNECTOR.  There's no <code>type1</code> value representing the far end of the connection <u>from</u> the EB and EC either.  
 * But the EB and EC PositionablePoint knows what itself is through
 * its <code>type</code> variable, and that can be queried by following the link as there's only one connection to a PositionablePoint.
 * <p>
 * Turnouts also have a <code>type</code> variable to represent their geometry, i.e. RH, LH, Wye, etc.
 * 
 * <h3>Class Structure</h3>
 * <ul>
 * <li>The track is represented by a hierarchy of classes that's rooted in {@link LayoutTrack} and completely disconnected from 
 * the PanelEditor classes.<br>
 * <a href="doc-files/TrackHierarchy.png"><img src="doc-files/TrackHierarchy.png" alt="UML class diagram for track objects" height="33%" width="33%"></a>
 * <li>{@link LayoutBlock} and {@link LayoutBlockManager} are a {@link jmri.NamedBean NamedBean} pair. The {@link LayoutBlock} is a group
 * of track segments and turnouts that corresponds to a 'block'. It may contain a {@link jmri.Memory} and/or {@link jmri.Sensor}. If present, the sensor 
 * defines occupancy for the LayoutBlock. Each LayoutBlock is paired 1-to-1
 * with a {@link jmri.Block} which it created; the Block has a specific form for its system name and the LayoutBlock's name as its user-name.
 * <li>{@link BlockContentsIcon}, {@link MemoryIcon} inherit from the equivalents in the {@link jmri.jmrit.display} package.
 * <li>{@link LayoutEditor} extends {@link jmri.jmrit.display.PanelEditor PanelEditor}
 * <li>Multiple classes, including {@link LayoutEditorAction}, {@link LayoutEditorComponent}, {@link LayoutEditorToolBarPanel}, 
 *      {@link MultiIconEditor}, and {@link} MultiSensorIconFrame.java, extend Swing or AWT components. (Although {@link LayoutEditorComponent}
 *       sounds like a base class for others, it's not; it's part of the {@link LayoutEditor} implementation.)
 * </ul>
 * 
 * <h3>Persistance</h3>
 * The classes that have ConfigureXML partner classes are:
 * <ul>
 * <li>{@link BlockContentsIcon}
 * <li>{@link LayoutBlockManager} (registered with ConfigurationManager)
 * <li>{@link LayoutEditor} (registered with ConfigurationManager)
 * <li>{@link LayoutShape}
 * <li>{@link LayoutSlip}
 * <li>{@link LayoutTrackDrawingOptions}
 * <li>{@link LayoutTurnout}
 * <li>{@link LayoutTurntable}
 * <li>{@link LevelXing}
 * <li>{@link MemoryIcon}
 * <li>{@link PositionablePoint}
 * <li>{@link TrackSegment}
 * </ul>
 * Only LayoutBlockManager and LayoutEditor are registered with the {@link jmri.ConfigureManager}.
 * <p>
 * LayoutBlockManager is stored and loaded in the usual manager way, including the LayoutBlock objects.
 * They are stored with the configuration information because they can be used
 * on multiple LayoutPanels.
 * <p>
 * LayoutEditorXml handles the storing and loading of all the track and icon objects.
 * It is stored at the user level ("Store Panels...")
 * <p>
 * A {@link BlockValueFile} stores and loads the value (internal transient contents) of Block 
 * objects from the BlockManager. It doesn't reference {@link LayoutBlock} objects.
 * <h3>More Info</h3>
 * User-level documentation is available 
 * <a href="https://www.jmri.org/help/en/package/jmri/jmrit/display/LayoutEditor.shtml">here</a>.
 * <!-- Put @see and @since tags down here. -->
 *
 */

// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.jmrit.display.layoutEditor;

// Connectivity of the objects in the example
/*
@startuml jmri/jmrit/display/layoutEditor/doc-files/SidingConnections.png

object EB1 {
    type="2" 
    connect1name="T3" 
}

object TO1 {
    type="1" 
    continuing="2" 
    ver="1" 
    connectaname="T3" 
    connectbname="T2" 
    connectcname="T1" 
}

object T3 {
    connect1name="EB1" 
    type1="POS_POINT" 
    connect2name="TO1" 
    type2="TURNOUT_A" 
}

object TO2 {
    type="2" 
    continuing="2" 
    ver="1" c
    onnectaname="T4" 
    connectbname="T2" 
    connectcname="T1" 
}

object T1 {
    connect1name="TO1" 
    type1="TURNOUT_C" 
    connect2name="TO2" 
    type2="TURNOUT_C"
}

object T2 {
    connect1name="TO1" 
    type1="TURNOUT_B" 
    connect2name="TO2" 
    type2="TURNOUT_B" 
}

object T4 {
    connect1name="TO2" 
    type1="TURNOUT_A"
    connect2name="EC1" 
    type2="POS_POINT"
}

object EC1 {
    type="3" 
    connect1name="T4"
}

T3 "1" o--o EB1
T3 "2" o--o "A" TO1

TO1 "C" o--o "C" T1
TO1 "B" o--o "B" T2

TO2 "C" o--o "C" T1
TO2 "B" o--o "B" T2

T4 "1" o--o "A" TO2
T4 "2" o--o EC1

@end
*/

// simplified inheritance diagram for track
/*
@startuml jmri/jmrit/display/layoutEditor/doc-files/TrackHierarchy.png

abstract class LayoutTrack

LayoutTrack <|-- LayoutTurnout
LayoutTrack <|-- LayoutTurntable
LayoutTrack <|-- LevelXing
LayoutTrack <|-- PositionablePoint
LayoutTrack <|-- TrackSegment

LayoutTurnout <|-- LayoutSlip

@end
*/
