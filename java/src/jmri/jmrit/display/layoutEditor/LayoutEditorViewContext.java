package jmri.jmrit.display.layoutEditor;

// This class may not important any others.

/**
 * LayoutEditorViewContext is a memo object containing
 * the graphical View context information for a {@link LayoutEditor} MVC instance.
 * <p>
 * As a memo class, this may contain methods, but the class cannot include
 * references to other classes, and ideally the methods won't use references
 * to other classes. Just data, and operations on that data.
 * <p>
 * This should map to a subset of the variables stored and loaded by
 * {@link jmri.jmrit.display.layoutEditor.configurexml.LayoutEditorXml}
 * and the XML Schema for the LayoutEditor element.
 * <p>
 * This holds <u>graphical View</u> context information.
 * It should <u>not</u> include Model (e.g. layout hardware, even global values)
 * or Control (e.g. options affecting the operation of the editor) information.
 * <p>
 * It's OK for this to hold startup default values for the quantities.
 * <p>
 * This may be a temporary class, only existing to help
 * build a better structure into this package.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
final public class LayoutEditorViewContext {

    LayoutEditorViewContext() {}  // intentionally package-protected to limit exposure

    // -----------------------------------
    // Sides and positions
    // -----------------------------------

    final public void setLayoutWidth(int width) {
        panelWidth = width;
    }
    final public int getLayoutWidth() {
        return panelWidth;
    }
    private int panelWidth = 0;

    final public void setLayoutHeight(int height) {
        panelHeight = height;
    }
    final public int getLayoutHeight() {
        return panelHeight;
    }
    private int panelHeight = 0;

    final public void setWindowWidth(int width) {
        windowWidth = width;
    }
    final public int getWindowWidth() {
        return windowWidth;
    }
    private int windowWidth = 0;

    final public void setWindowHeight(int height) {
        windowHeight = height;
    }
    final public int getWindowHeight() {
        return windowHeight;
    }
    private int windowHeight = 0;

    // Window upper left x, not panel upper left x
    final public int getUpperLeftX() {
        return upperLeftX;
    }
    final public void setUpperLeftX(int x) {
        upperLeftX = x;
    }
    private int upperLeftX = 0;

    // Window upper left y, not panel upper left y
    final public int getUpperLeftY() {
        return upperLeftY;
    }
    final public void setUpperLeftY(int y) {
        upperLeftY = y;
    }
    private int upperLeftY = 0; // (not panel)

    final public int setGridSize(int newSize) {
        gridSize1st = newSize;
        return gridSize1st;
    }

    /**
     * Get the width drawing the grid; 10 is the
     * default/initial value.
     * @return current value
     */
    final public int getGridSize() {
        return gridSize1st;
    }
    private int gridSize1st = 10;

    final public int setGridSize2nd(int newSize) {
        gridSize2nd = newSize;
        return gridSize2nd;
    }

    /**
     * Get the width for 2nd drawing of the grid; 10 is the
     * default/initial value.
     * @return current value
     */
    final public int getGridSize2nd() {
        return gridSize2nd;
    }
    private int gridSize2nd = 10;

    // also found in LayoutTrackDrawingOptions?
    // why is this a float?  Needed for Graphics2D arguments?
    final public void setMainlineTrackWidth(float width) {
        mainlineTrackWidth = (int)width;
    }

    /**
     * Get the width for drawing mainline track; 4 is the
     * default/initial value.
     * @return current value
     */
    final public int getMainlineTrackWidth() {
        return (int) mainlineTrackWidth;
    }
    private float mainlineTrackWidth = 4.0F;

    /**
     * Set the width for sideline track; note
     * that the stored and retrievable value is an integer.
     * @param width Value to store; will be cast to (int)
     */
    // also found in LayoutTrackDrawingOptions? (temporary?)
    // why is this a float? (temporary?)
    final public void setSidelineTrackWidth(float width) {
        sidelineTrackWidth = (int)width;
    }

    /**
     * Get the width for drawing sideline track; 2 is the
     * default/initial value.
     * @return current value
     */
    final public int getSidelineTrackWidth() {
        return (int) sidelineTrackWidth;
    }
    private float sidelineTrackWidth = 2.0F;

    // also found in LayoutTrackDrawingOptions?
    // why is this a float?  Needed for Graphics2D arguments?
    final public void setMainlineBlockWidth(float width) {
        mainlineBlockWidth = (int)width;
    }

    /**
     * Get the width for drawing mainline Block; 4 is the
     * default/initial value.
     * @return current value
     */
    final public int getMainlineBlockWidth() {
        return (int) mainlineBlockWidth;
    }
    private float mainlineBlockWidth = 4.0F;

    /**
     * Set the width for sideline Block; note
     * that the stored and retrievable value is an integer.
     * @param width Value to store; will be cast to (int)
     */
    // also found in LayoutBlockDrawingOptions? (temporary?)
    // why is this a float? (temporary?)
    final public void setSidelineBlockWidth(float width) {
        sidelineBlockWidth = (int)width;
    }

    /**
     * Get the width for drawing sideline Block; 2 is the
     * default/initial value.
     * @return current value
     */
    final public int getSidelineBlockWidth() {
        return (int) sidelineBlockWidth;
    }
    private float sidelineBlockWidth = 2.0F;

    /**
     * Get the X-axis scaling value; 1.0 is the
     * default/initial value.
     * @return current value
     */
    final public double getXScale() {
        return xScale;
    }
    final public void setXScale(double scale) {
        xScale = scale;
    }
    private double xScale = 1.0;

    /**
     * Get the Y-axis scaling value; 1.0 is the
     * default/initial value.
     * @return current value
     */
    final public double getYScale() {
        return yScale;
    }
    final public void setYScale(double scale) {
        yScale = scale;
    }
    private double yScale = 1.0;


    // initialize logging
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditor.class);

}

/* ***************************************************

      <xs:attribute name="sliders" type="yesNoType" default="yes"/>
      <xs:attribute name="drawgrid" type="yesNoType" default="yes" />
      <xs:attribute name="antialiasing" type="yesNoType" default="yes" />
      <xs:attribute name="turnoutcircles" type="yesNoType" default="yes" />

      <xs:attribute name="defaulttrackcolor" type="screenColorType" use="required" /> Default about display?
      <xs:attribute name="defaultalternativetrackcolor" type="screenColorType"/>      Or more Controller?
      <xs:attribute name="defaultoccupiedtrackcolor" type="screenColorType"/>
      <xs:attribute name="defaulttextcolor" type="screenColorType"/>

      <xs:attribute name="turnoutcirclecolor" type="turnoutCircleColourType"/>
      <xs:attribute name="turnoutcirclethrowncolor" type="turnoutCircleColourType"/>

      <xs:attribute name="turnoutfillcontrolcircles" type="yesNoType" default="no"/>
      <xs:attribute name="turnoutcirclesize" type="xs:integer"/>
      <xs:attribute name="turnoutdrawunselectedleg" type="yesNoType" default="yes"/>

      <xs:attribute name="turnoutbx" type="xs:float"/>
      <xs:attribute name="turnoutcx" type="xs:float"/>
      <xs:attribute name="turnoutwid" type="xs:float"/>
      <xs:attribute name="xoverlong" type="xs:float"/>
      <xs:attribute name="xoverhwid" type="xs:float"/>
      <xs:attribute name="xovershort" type="xs:float"/>

      <xs:attribute name="redBackground" type="xs:integer"/>
      <xs:attribute name="greenBackground" type="xs:integer"/>
      <xs:attribute name="blueBackground" type="xs:integer"/>

      <xs:attribute name="zoom" type="xs:float" default="1.0"/>


Probably not View graphical:
      <xs:attribute name="name" type="xs:string"/> Is that graphical? Not sure

      <xs:attribute name="scrollable" type="scrollableType" default="both" />
      <xs:attribute name="editable" type="yesNoType" default="yes" />
      <xs:attribute name="positionable" type="yesNoType" default="yes" />
      <xs:attribute name="controlling" type="yesNoType" default="yes" />
      <xs:attribute name="animating" type="yesNoType" default="yes" />
      <xs:attribute name="showhelpbar" type="yesNoType" default="yes" />

      <xs:attribute name="snaponadd" type="yesNoType" default="yes" />
      <xs:attribute name="snaponmove" type="yesNoType" default="yes" />

      <xs:attribute name="tooltipsnotedit" type="yesNoType" default="yes" />
      <xs:attribute name="tooltipsinedit" type="yesNoType" default="yes" />

      <xs:attribute name="autoblkgenerate" type="yesNoType" default="yes"/>

      <xs:attribute name="openDispatcher" type="yesNoType" default="no"/>
      <xs:attribute name="useDirectTurnoutControl" type="yesNoType" default="no"/>

 */

