package jmri.jmrit.display.layoutEditor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.List;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.JmriException;
import jmri.Turnout;
import jmri.util.*;

/**
 * MVC View component abstract base for the LayoutTrack hierarchy.
 * <p>
 * This contains the display information, including screen geometry, for a
 * LayoutEditor panel. The geometry/connectivity information is held in
 * {@link LayoutTrack} subclasses.
 * <ul>
 * <li>Position(s) of the screen icons and its parts, typically the center;
 * scaling and translation; size and bounds
 * <li>Line colors
 * <li>Flipped status; drawing details like bezier curve points
 * <li>Various decorations: arrows, tunnels, bridges
 * <li>Hidden status
 * </ul>
 *
 * @author Bob Jacobsen Copyright (c) 2020
 *
 */
abstract public class LayoutTrackView {

    /**
     * Constructor method.
     *
     * @param track        the layout track to view
     * @param layoutEditor the panel in which to place the view
     */
    public LayoutTrackView(@Nonnull LayoutTrack track, @Nonnull LayoutEditor layoutEditor) {
        this.layoutTrack = track;
        this.layoutEditor = layoutEditor;
    }

    /**
     * constructor method
     *
     * @param track        the track to view
     * @param c            display location
     * @param layoutEditor for reference to tools
     */
    public LayoutTrackView(@Nonnull LayoutTrack track, @Nonnull Point2D c, @Nonnull LayoutEditor layoutEditor) {
        this.layoutTrack = track;
        this.layoutEditor = layoutEditor;
        this.center = c;
    }

    final private LayoutTrack layoutTrack;

    final protected LayoutEditor layoutEditor;

    // Accessor Methods

    @Nonnull
    final public String getId() {  // temporary Id vs name; is one for the View?
        return layoutTrack.getId();
    }

    @Nonnull
    final public String getName() {
        return layoutTrack.getName();
    }

    final protected void setIdent(@Nonnull String ident) {
        layoutTrack.setIdent(ident);
    }

    // temporary accessor?  Or is this a long term thing?
    // @Nonnull temporary until we gigure out if can be null or not
    public LayoutTrack getLayoutTrack() {
        return layoutTrack;
    }

    /**
     * Set center coordinates
     *
     * @return The center coordinates
     */
    public Point2D getCoordsCenter() { // should be final for efficiency, temporary not to allow redirction overrides.
        return center;
    }

    /**
     * Set center coordinates.
     * <p>
     * Some subtypes may reimplement this is "center" is a more complicated
     * idea, i.e. for Bezier curves
     *
     * @param p the coordinates to set
     */
    public void setCoordsCenter(@Nonnull Point2D p) {  // temporary = want to make protected after migration
        center = p;
    }

    private Point2D center = new Point2D.Double(50.0, 50.0);

    /**
     * @return true if this track segment has decorations
     */
    public boolean hasDecorations() {
        return false;
    }

    /**
     * Get current decorations
     *
     * @return the decorations
     */
    public Map<String, String> getDecorations() {
        return decorations;
    }

    /**
     * Set new decorations
     *
     * This is a complete replacement of the decorations, not an appending.
     *
     * @param decorations A map from strings ("arrow", "bridge", "bumper",..) to
     *                    specific value strings ("single", "entry;right", ),
     *                    perhaps including multiple values separated by
     *                    semicolons.
     */
    public void setDecorations(Map<String, String> decorations) {
        this.decorations = decorations;
    }
    protected Map<String, String> decorations = null;

    /**
     * convenience method for accessing...
     *
     * @return the layout editor's toolbar panel
     */
    @Nonnull
    final public LayoutEditorToolBarPanel getLayoutEditorToolBarPanel() {
        return layoutEditor.getLayoutEditorToolBarPanel();
    }

    // these are convenience methods to return circles & rectangle used to draw onscreen
    //
    // compute the control point rect at inPoint; use the turnout circle size
    final public Ellipse2D trackEditControlCircleAt(@Nonnull Point2D inPoint) {
        return trackControlCircleAt(inPoint);
    }

    // compute the turnout circle at inPoint (used for drawing)
    final public Ellipse2D trackControlCircleAt(@Nonnull Point2D inPoint) {
        return new Ellipse2D.Double(inPoint.getX() - layoutEditor.circleRadius,
                inPoint.getY() - layoutEditor.circleRadius,
                layoutEditor.circleDiameter, layoutEditor.circleDiameter);
    }

    // compute the turnout circle control rect at inPoint
    final public Rectangle2D trackControlCircleRectAt(@Nonnull Point2D inPoint) {
        return new Rectangle2D.Double(inPoint.getX() - layoutEditor.circleRadius,
                inPoint.getY() - layoutEditor.circleRadius,
                layoutEditor.circleDiameter, layoutEditor.circleDiameter);
    }

    final protected Color getColorForTrackBlock(
            @CheckForNull LayoutBlock layoutBlock, boolean forceBlockTrackColor) {
        Color result = ColorUtil.CLEAR;  // transparent
        if (layoutBlock != null) {
            if (forceBlockTrackColor) {
                result = layoutBlock.getBlockTrackColor();
            } else {
                result = layoutBlock.getBlockColor();
            }
        }
        return result;
    }

    // optional parameter forceTrack = false
    final protected Color getColorForTrackBlock(@CheckForNull LayoutBlock lb) {
        return getColorForTrackBlock(lb, false);
    }

    final protected Color setColorForTrackBlock(Graphics2D g2,
            @CheckForNull LayoutBlock layoutBlock, boolean forceBlockTrackColor) {
        Color result = getColorForTrackBlock(layoutBlock, forceBlockTrackColor);
        g2.setColor(result);
        return result;
    }

    // optional parameter forceTrack = false
    final protected Color setColorForTrackBlock(Graphics2D g2, @CheckForNull LayoutBlock lb) {
        return setColorForTrackBlock(g2, lb, false);
    }

    /**
     * draw one line (Ballast, ties, center or 3rd rail, block lines)
     *
     * @param g2      the graphics context
     * @param isMain  true if drawing mainlines
     * @param isBlock true if drawing block lines
     */
    abstract protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock);

    /**
     * draw two lines (rails)
     *
     * @param g2               the graphics context
     * @param isMain           true if drawing mainlines
     * @param railDisplacement the offset from center to draw the lines
     */
    abstract protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement);

    /**
     * draw hidden track
     *
     * @param g2 the graphics context
     */
    // abstract protected void drawHidden(Graphics2D g2);
    // note: placeholder until I get this implemented in all sub-classes
    // TODO: replace with abstract declaration (above)
    final protected void drawHidden(Graphics2D g2) {
        // nothing to do here... move along...
    }

    /**
     * draw the text for this layout track
     * @param g
     * note: currently can't override (final); change this if you need to
     */
    final protected void drawLayoutTrackText(Graphics2D g) {
        // get the center coordinates
        int x = (int) center.getX(), y = (int) center.getY();

        // get the name of this track
        String name = getName();

        // get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(g.getFont());

        // determine the X coordinate for the text
        x -= metrics.stringWidth(name) / 2;

        // determine the Y coordinate for the text
        y += metrics.getHeight() / 2;

        // (note we add the ascent, as in java 2d 0 is top of the screen)
        //y += (int) metrics.getAscent();

        g.drawString(name, x, y);
    }

    /**
     * Load a file for a specific arrow ending.
     *
     * @param n               The arrow type as a number
     * @param arrowsCountMenu menu containing the arrows to set visible
     *                        selection
     * @return An item for the arrow menu
     */
    public JCheckBoxMenuItem loadArrowImageToJCBItem(int n, JMenu arrowsCountMenu) {
        ImageIcon imageIcon = new ImageIcon(FileUtil.findURL("program:resources/icons/decorations/ArrowStyle" + n + ".png"));
        JCheckBoxMenuItem jcbmi = new JCheckBoxMenuItem(imageIcon);
        arrowsCountMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationStyleMenuToolTip"));
        // can't set selected here because the ActionListener has to be set first
        return jcbmi;
    }
    protected static final int NUM_ARROW_TYPES = 6;

    /**
     * highlight unconnected connections
     *
     * @param g2           the graphics context
     * @param specificType the specific connection to draw (or NONE for all)
     */
    abstract protected void highlightUnconnected(Graphics2D g2, HitPointType specificType);

    // optional parameter specificType = NONE
    final protected void highlightUnconnected(Graphics2D g2) {
        highlightUnconnected(g2, HitPointType.NONE);
    }

    /**
     * draw the edit controls
     *
     * @param g2 the graphics context
     */
    abstract protected void drawEditControls(Graphics2D g2);

    /**
     * Draw the turnout controls
     *
     * @param g2 the graphics context
     */
    abstract protected void drawTurnoutControls(Graphics2D g2);

    /**
     * Draw track decorations
     *
     * @param g2 the graphics context
     */
    abstract protected void drawDecorations(Graphics2D g2);

    /**
     * Get the hidden state of the track element.
     *
     * @return true if hidden; false otherwise
     */
    final public boolean isHidden() {
        return hidden;
    }

    final public void setHidden(boolean hide) {
        if (hidden != hide) {
            hidden = hide;
            if (layoutEditor != null) {
                layoutEditor.redrawPanel();
            }
        }
    }

    private boolean hidden = false;

    /*
    * non-accessor methods
     */
    /**
     * get turnout state string
     *
     * @param turnoutState of the turnout
     * @return the turnout state string
     */
    final public String getTurnoutStateString(int turnoutState) {
        String result = "";
        if (turnoutState == Turnout.CLOSED) {
            result = Bundle.getMessage("TurnoutStateClosed");
        } else if (turnoutState == Turnout.THROWN) {
            result = Bundle.getMessage("TurnoutStateThrown");
        } else {
            result = Bundle.getMessage("BeanStateUnknown");
        }
        return result;
    }

    /**
     * Check for active block boundaries.
     * <p>
     * If any connection point of a layout track object has attached objects,
     * such as signal masts, signal heads or NX sensors, the layout track object
     * cannot be deleted.
     *
     * @return true if the layout track object can be deleted.
     */
    abstract public boolean canRemove();

    /**
     * Display the attached items that prevent removing the layout track item.
     *
     * @param itemList A list of the attached heads, masts and/or sensors.
     * @param typeKey  The object type such as Turnout, Level Crossing, etc.
     */
    final public void displayRemoveWarningDialog(List<String> itemList, String typeKey) {
        itemList.sort(null);
        StringBuilder msg = new StringBuilder(Bundle.getMessage("MakeLabel", // NOI18N
                Bundle.getMessage("DeleteTrackItem", Bundle.getMessage(typeKey))));  // NOI18N
        for (String item : itemList) {
            msg.append("\n    " + item);  // NOI18N
        }
        javax.swing.JOptionPane.showMessageDialog(layoutEditor,
                msg.toString(),
                Bundle.getMessage("WarningTitle"), // NOI18N
                javax.swing.JOptionPane.WARNING_MESSAGE);
    }

    /**
     * scale this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
    abstract public void scaleCoords(double xFactor, double yFactor);

    /**
     * translate this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to translate X coordinates
     * @param yFactor the amount to translate Y coordinates
     */
    abstract public void translateCoords(double xFactor, double yFactor);

    /**
     * rotate this LayoutTrack's coordinates by angleDEG's
     *
     * @param angleDEG the amount to rotate in degrees
     */
    abstract public void rotateCoords(double angleDEG);

    final protected Point2D rotatePoint(@Nonnull Point2D p, double sineRot, double cosineRot) {
        double cX = center.getX();
        double cY = center.getY();

        double deltaX = p.getX() - cX;
        double deltaY = p.getY() - cY;

        double x = cX + cosineRot * deltaX - sineRot * deltaY;
        double y = cY + sineRot * deltaX + cosineRot * deltaY;

        return new Point2D.Double(x, y);
    }

    /**
     * find the hit (location) type for a point
     *
     * @param hitPoint           the point
     * @param useRectangles      whether to use (larger) rectangles or (smaller)
     *                           circles for hit testing
     * @param requireUnconnected whether to only return hit types for free
     *                           connections
     * @return the location type for the point (or NONE)
     * @since 7.4.3
     */
    abstract protected HitPointType findHitPointType(@Nonnull Point2D hitPoint,
                                                    boolean useRectangles,
                                                    boolean requireUnconnected);

    // optional useRectangles & requireUnconnected parameters default to false
    final protected HitPointType findHitPointType(@Nonnull Point2D p) {
        return findHitPointType(p, false, false);
    }

    // optional requireUnconnected parameter defaults to false
    final protected HitPointType findHitPointType(@Nonnull Point2D p, boolean useRectangles) {
        return findHitPointType(p, useRectangles, false);
    }

    /**
     * return the coordinates for a specified connection type (abstract: should
     * be overridden by ALL subclasses)
     *
     * @param connectionType the connection type
     * @return the coordinates for the specified connection type
     */
    abstract public Point2D getCoordsForConnectionType(HitPointType connectionType);

    /**
     * @return the bounds of this track
     */
    abstract public Rectangle2D getBounds();

    /**
     * show the popup menu for this layout track
     *
     * @param mouseEvent the mouse down event that triggered this popup
     * @return the popup menu for this layout track
     */
    @Nonnull
    abstract protected JPopupMenu showPopup(@Nonnull MouseEvent mouseEvent);

    /**
     * show the popup menu for this layout track
     *
     * @param where to show the popup
     * @return the popup menu for this layout track
     */
    @Nonnull
    final protected JPopupMenu showPopup(Point2D where) {
        return this.showPopup(new MouseEvent(
                layoutEditor.getTargetPanel(), // source
                MouseEvent.MOUSE_CLICKED, // id
                System.currentTimeMillis(), // when
                0, // modifiers
                (int) where.getX(), (int) where.getY(), // where
                0, // click count
                true));                         // popup trigger

    }

    /**
     * show the popup menu for this layout track
     *
     * @return the popup menu for this layout track
     */
    @Nonnull
    final protected JPopupMenu showPopup() {
        Point2D where = MathUtil.multiply(getCoordsCenter(),
                layoutEditor.getZoom());
        return this.showPopup(where);
    }

    /**
     * get the LayoutTrack connected at the specified connection type
     *
     * @param connectionType where on us to get the connection
     * @return the LayoutTrack connected at the specified connection type
     * @throws JmriException - if the connectionType is invalid
     */
    abstract public LayoutTrack getConnection(HitPointType connectionType) throws JmriException;

    /**
     * set the LayoutTrack connected at the specified connection type
     *
     * @param connectionType where on us to set the connection
     * @param o              the LayoutTrack that is to be connected
     * @param type           where on the LayoutTrack we are connected
     * @throws JmriException - if connectionType or type are invalid
     */
    abstract public void setConnection(HitPointType connectionType, LayoutTrack o, HitPointType type) throws JmriException;

    /**
     * abstract method... subclasses should implement _IF_ they need to recheck
     * their block boundaries
     */
    abstract protected void reCheckBlockBoundary();

    /**
     * get the layout connectivity for this track
     *
     * @return the list of Layout Connectivity objects
     */
    abstract protected List<LayoutConnectivity> getLayoutConnectivity();

    /**
     * return true if this connection type is disconnected
     *
     * @param connectionType the connection type to test
     * @return true if the connection for this connection type is free
     */
    public boolean isDisconnected(HitPointType connectionType) {
        throw new IllegalArgumentException("should have called in Object instead of View (temporary)");
    }

    /**
     * return a list of the available connections for this layout track
     *
     * @return the list of available connections
     */
    // note: used by LayoutEditorChecks.setupCheckUnConnectedTracksMenu()
    //
    // This could have just returned a boolean but I thought a list might be
    // more useful (eventually... not currently being used; we just check to see
    // if it's not empty.)
    @Nonnull
    abstract public List<HitPointType> checkForFreeConnections();

    /**
     * determine if all the appropriate blocks have been assigned to this track
     *
     * @return true if all appropriate blocks have been assigned
     */
    // note: used by LayoutEditorChecks.setupCheckUnBlockedTracksMenu()
    //
    abstract public boolean checkForUnAssignedBlocks();

    /**
     * check this track and its neighbors for non-contiguous blocks
     * <p>
     * For each (non-null) blocks of this track do: #1) If it's got an entry in
     * the blockNamesToTrackNameSetMap then #2) If this track is not in one of
     * the TrackNameSets for this block #3) add a new set (with this
     * block/track) to blockNamesToTrackNameSetMap and #4) check all the
     * connections in this block (by calling the 2nd method below)
     * <p>
     * Basically, we're maintaining contiguous track sets for each block found
     * (in blockNamesToTrackNameSetMap)
     *
     * @param blockNamesToTrackNameSetMaps hashmap of key:block names to lists
     *                                     of track name sets for those blocks
     */
    // note: used by LayoutEditorChecks.setupCheckNonContiguousBlocksMenu()
    //
    abstract public void checkForNonContiguousBlocks(
            @Nonnull HashMap<String, List<Set<String>>> blockNamesToTrackNameSetMaps);

    /**
     * recursive routine to check for all contiguous tracks in this blockName
     *
     * @param blockName    the block that we're checking for
     * @param TrackNameSet the set of track names in this block
     */
    abstract public void collectContiguousTracksNamesInBlockNamed(
            @Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet);

    /**
     * Assign all the layout blocks in this track
     *
     * @param layoutBlock to this layout block (used by the Tools menu's "Assign
     *                    block to selection" item)
     */
    abstract public void setAllLayoutBlocks(LayoutBlock layoutBlock);

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrackView.class);
}
