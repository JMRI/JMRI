/**
 * Layout Editor currently represents both structure and graphical display through a single set of objects.
 * Work is proceeding to separate those out and provide clean interfaces for other packages to access the structural information.
 * <h2>Example</h2>
 * A small example layout:<br>
 * <a href="doc-files/SidingImage.png"><img src="doc-files/SidingImage.png" alt="Simple layout example" height="25%" width="25%"></a>
 * <h3>Connectivity</h3>
 * This is coded and stored as the following: (Some graphical attributes removed, reordered)<br>
<pre>
    &lt;layoutturnout ident="TO1" type="RH_TURNOUT" continuing="2" ver="1" connectaname="T3" connectbname="T2" connectcname="T1" class="jmri.jmrit.display.layoutEditor.configurexml.LayoutTurnoutXml" /&gt;
    &lt;layoutturnout ident="TO2" type="LH_TURNOUT" continuing="2" ver="1" connectaname="T4" connectbname="T2" connectcname="T1" class="jmri.jmrit.display.layoutEditor.configurexml.LayoutTurnoutXml" /&gt;
    &lt;tracksegment ident="T1" connect1name="TO1" type1="TURNOUT_C" connect2name="TO2" type2="TURNOUT_C" class="jmri.jmrit.display.layoutEditor.configurexml.TrackSegmentXml" /&gt;
    &lt;tracksegment ident="T2" connect1name="TO1" type1="TURNOUT_B" connect2name="TO2" type2="TURNOUT_B" class="jmri.jmrit.display.layoutEditor.configurexml.TrackSegmentXml" /&gt;
    &lt;tracksegment ident="T3" connect1name="EB1" type1="POS_POINT" connect2name="TO1" type2="TURNOUT_A" class="jmri.jmrit.display.layoutEditor.configurexml.TrackSegmentXml" /&gt;
    &lt;tracksegment ident="T4" connect1name="TO2" type1="TURNOUT_A" connect2name="EC1" type2="POS_POINT" class="jmri.jmrit.display.layoutEditor.configurexml.TrackSegmentXml" /&gt;
    &lt;positionablepoint ident="EB1" type="END_BUMPER" connect1name="T3" class="jmri.jmrit.display.layoutEditor.configurexml.PositionablePointXml" /&gt;
    &lt;positionablepoint ident="EC1" type="EDGE_CONNECTOR" connect1name="T4" linkedpanel="" linkpointid="" class="jmri.jmrit.display.layoutEditor.configurexml.PositionablePointXml" /&gt;
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
 * <h2>Internals</h2>
 * <h3>Class Hierarchy</h3>
 * <ul>
 * <li>The track is represented by a hierarchy of classes that's rooted in {@link jmri.jmrit.display.layoutEditor.LayoutTrack} and completely disconnected from 
 * the PanelEditor classes. New classes are being added to represent the specific types previously implemented via type variables and conditional code.
 * <br><a href="doc-files/NewTurnoutClassDiagram.png"><img src="doc-files/NewTurnoutClassDiagram.png" alt="UML class diagram for track objects" height="67%" width="67%"></a><br>
 * <li>{@link jmri.jmrit.display.layoutEditor.LayoutBlock} and {@link jmri.jmrit.display.layoutEditor.LayoutBlockManager} are a {@link jmri.NamedBean NamedBean} pair. 
 * The {@link jmri.jmrit.display.layoutEditor.LayoutBlock} is a group
 * of track segments and turnouts that corresponds to a 'block'. It may contain a {@link jmri.Memory} and/or {@link jmri.Sensor}. If present, the sensor 
 * defines occupancy for the LayoutBlock. Each LayoutBlock is paired 1-to-1
 * with a {@link jmri.Block} which it created; the Block has a specific form for its system name and the LayoutBlock's name as its user-name.
 * <li>{@link jmri.jmrit.display.layoutEditor.BlockContentsIcon}, {@link jmri.jmrit.display.layoutEditor.MemoryIcon} inherit from the equivalents in the {@link jmri.jmrit.display} package.
 * <li>{@link jmri.jmrit.display.layoutEditor.LayoutEditor} extends {@link jmri.jmrit.display.panelEditor.PanelEditor PanelEditor}
 * <li>Multiple classes, including {@link jmri.jmrit.display.layoutEditor.LayoutEditorAction}, {@link jmri.jmrit.display.layoutEditor.LayoutEditorComponent}, {@link jmri.jmrit.display.layoutEditor.LayoutEditorToolBarPanel}, 
 *      {@link jmri.jmrit.display.layoutEditor.MultiIconEditor}, and {@link jmri.jmrit.display.layoutEditor.MultiSensorIconFrame}, extend Swing or AWT components. 
 *      (Although {@link jmri.jmrit.display.layoutEditor.LayoutEditorComponent}
 *       sounds like a base class for others, it's not; it's part of the {@link jmri.jmrit.display.layoutEditor.LayoutEditor} implementation.)
 * <li>Three subpackages provide specific services:
 *   <ul>
 *   <li>{@link jmri.jmrit.display.layoutEditor.blockRoutingTable}
 *   <li>{@link jmri.jmrit.display.layoutEditor.LayoutEditorDialogs}
 *   <li>{@link jmri.jmrit.display.layoutEditor.configurexml}
 *   </ul>
 * </ul>
 *
 * <h3>Class Relationships</h3>
 * The LayoutBlock class is at the heart of the navigation web for layout-representing objects.
 * <br><a href="doc-files/LayoutBlockInterconnections.png"><img src="doc-files/LayoutBlockInterconnections.png" alt="UML class diagram for track objects" height="67%" width="67%"></a><br>
 * 
 * 
 * <h3>GUI</h3>
 * The Layout Editor window consists of a menu bar and an upper tool-bar that are all made with 
 * (basically) standard Swing components.  Below that is a JPane containing the 
 * layout drawing itself.
 * <p>
 * The LayoutTrackView tree defines <code>draw1</code> and <code>draw2</code> methods that 
 * draw two different representations of the track elements.  These are only 
 * invoked from {}@link jmri.jmrit.display.layoutEditor.LayoutEditorComponent}. LayoutEditorComponent is 
 * a JComponent with a {@link jmri.jmrit.display.layoutEditor.LayoutEditorComponent#paint} public method
 * that invokes a series of internal private methods to display the layers of the layout drawing. 
 * That in turn is invoked via the usual repaint() mechanism, although it's often kicked
 * off by a call to {@link jmri.jmrit.display.layoutEditor.LayoutEditor#redrawPanel()}.
 * Each of those layer private methods sets up graphics and method options, then
 * calls {@link jmri.jmrit.display.layoutEditor.LayoutEditorComponent#draw1}
 * or {@link jmri.jmrit.display.layoutEditor.LayoutEditorComponent#draw2}. Those in turn
 * loop through a list from {@link jmri.jmrit.display.layoutEditor.LayoutEditor#getLayoutTracks()}
 * calling their individual {@link jmri.jmrit.display.layoutEditor.LayoutTrackView#draw1} and {@link jmri.jmrit.display.layoutEditor.LayoutTrackView#draw1}
 * methods.
 * <p>
 * <br><a href="doc-files/NMVCmodel.png"><img src="doc-files/MVCmodel.png" alt="MVC object diagram" height="67%" width="67%"></a><br>
 * The goal is a MVC-like structure, with modifications to account for the mix of 
 * swing components and AWT-style paint operations.
 * 
 * 
 * <h3>Persistance</h3>
 * The classes that have ConfigureXML partner classes are:
 * <ul>
 * <li>{@link jmri.jmrit.display.layoutEditor.BlockContentsIcon}
 * <li>{@link jmri.jmrit.display.layoutEditor.LayoutBlockManager} (registered with ConfigurationManager)
 * <li>{@link jmri.jmrit.display.layoutEditor.LayoutEditor} (registered with ConfigurationManager)
 * <li>{@link jmri.jmrit.display.layoutEditor.LayoutShape}
 * <li>{@link jmri.jmrit.display.layoutEditor.LayoutSlip}
 * <li>{@link jmri.jmrit.display.layoutEditor.LayoutTrackDrawingOptions}
 * <li>{@link jmri.jmrit.display.layoutEditor.LayoutTurnout}
 * <li>{@link jmri.jmrit.display.layoutEditor.LayoutTurntable}
 * <li>{@link jmri.jmrit.display.layoutEditor.LevelXing}
 * <li>{@link jmri.jmrit.display.layoutEditor.MemoryIcon}
 * <li>{@link jmri.jmrit.display.layoutEditor.PositionablePoint}
 * <li>{@link jmri.jmrit.display.layoutEditor.TrackSegment}
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
 * A {@link jmri.jmrit.display.layoutEditor.BlockValueFile} stores and loads the value (internal temporary contents) of Block 
 * objects from the BlockManager. It doesn't reference {@link jmri.jmrit.display.layoutEditor.LayoutBlock} objects.
 * <h2>More Info</h2>
 * User-level documentation is available 
 * <a href="https://www.jmri.org/help/en/package/jmri/jmrit/display/LayoutEditor.shtml">here</a>.
 * <p>
 * (If these Javadoc were created with UML, the 
 * <a href="package.svg">
 *   full-scale package diagram is also available.</a>)
 * <p style="text-align: center; font-size: xx-small;">
 *      <a href="package.svg">
 *          <img src="i/resources/icons/misc/UML_small_logo.png" alt="Link to UML diagram">
 *      </a>
 * </p>
 * <!-- Put @see and @since tags down here. -->
 * @see jmri.jmrit.entryexit
 * @see jmri.Section
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
