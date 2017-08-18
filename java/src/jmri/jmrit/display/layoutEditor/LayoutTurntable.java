package jmri.jmrit.display.layoutEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;
import jmri.util.swing.BeanSelectCreatePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutTurntable is a representation used by LayoutEditor to display a
 * turntable.
 * <P>
 * A LayoutTurntable has a variable number of connection points, called
 * RayTracks, each radiating from the center of the turntable. Each of these
 * points should be connected to a TrackSegment.
 * <P>
 * Each radiating segment (RayTrack) gets its Block information from its
 * connected track segment.
 * <P>
 * Each radiating segment (RayTrack) has a unique connection index. The
 * connection index is set when the RayTrack is created, and cannot be changed.
 * This connection index is used to maintain the identity of the radiating
 * segment to its connected Track Segment as ray tracks are added and deleted by
 * the user.
 * <P>
 * The radius of the turntable circle is variable by the user.
 * <P>
 * Each radiating segment (RayTrack) connecting point is a fixed distance from
 * the center of the turntable. The user may vary the angle of the radiating
 * segment. Angles are measured from the vertical (12 o'clock) position in a
 * clockwise manner. For example, 30 degrees is 1 o'clock, 60 degrees is 2
 * o'clock, 90 degrees is 3 o'clock, etc.
 * <P>
 * Each radiating segment is drawn from its connection point to the turntable
 * circle in the direction of the turntable center.
 *
 * @author Dave Duchamp Copyright (c) 2007
 */
public class LayoutTurntable extends LayoutTrack {

    // Defined text resource
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    // defined constants
    // operational instance variables (not saved between sessions)
    private LayoutTurntable instance = null;

    private boolean dccControlledTurnTable = false;

    // persistent instance variables (saved between sessions)
    private double radius = 25.0;
    private Point2D center = new Point2D.Double(50.0, 50.0);
    private ArrayList<RayTrack> rayList = new ArrayList<RayTrack>(); // list of Ray Track objects.
    private int lastKnownIndex = -1;

    /**
     * constructor method
     */
    public LayoutTurntable(String id, Point2D c, LayoutEditor myPanel) {
        instance = this;
        layoutEditor = myPanel;
        ident = id;
        center = c;
        radius = 25.0;
    }

    // this should only be used for debugging...
    public String toString() {
        return "LayoutTurntable " + ident;
    }

    /**
     * Accessor methods
     */

    public double getRadius() {
        return radius;
    }

    public void setRadius(double r) {
        radius = r;
    }

    /**
     * @return the bounds of this turntable
     */
    public Rectangle2D getBounds() {
        Rectangle2D result;

        result = new Rectangle2D.Double(center.getX(), center.getY(), 0, 0);
        for (int k = 0; k < getNumberRays(); k++) {
            result.add(getRayCoordsOrdered(k));
        }
        return result;
    }

    protected RayTrack addRay(double angle) {
        RayTrack rt = new RayTrack(angle, getNewIndex());
        // (ray!=null) {
        rayList.add(rt);
        //}
        return rt;
    }

    private int getNewIndex() {
        int index = -1;
        if (rayList.size() == 0) {
            return 0;
        }

        boolean found = true;
        while (found) {
            index++;
            found = false; // assume failure (pessimist!)
            for (RayTrack rt : rayList) {
                if (index == rt.getConnectionIndex()) {
                    found = true;
                }
            }
        }
        return index;
    }

    // the following method is only for use in loading layout turntables
    public void addRayTrack(double angle, int index, String name) {
        RayTrack rt = new RayTrack(angle, index);
        //if (ray!=null) {
        rayList.add(rt);
        rt.connectName = name;
        //}
    }

    public TrackSegment getRayConnectIndexed(int index) {
        TrackSegment result = null;
        for (RayTrack rt : rayList) {
            if (rt.getConnectionIndex() == index) {
                result = rt.getConnect();
                break;
            }
        }
        return result;
    }

    public TrackSegment getRayConnectOrdered(int i) {
        TrackSegment result = null;

        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            if (rt != null) {
                result = rt.getConnect();
            }
        }
        return result;
    }

    public void setRayConnect(TrackSegment ts, int index) {
        for (RayTrack rt : rayList) {
            if (rt.getConnectionIndex() == index) {
                rt.setConnect(ts);
                break;
            }
        }
    }

    public int getNumberRays() {
        return rayList.size();
    }

    public int getRayIndex(int i) {
        int result = 0;
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            result = rt.getConnectionIndex();
        }
        return result;
    }

    public double getRayAngle(int i) {
        double result = 0.0;
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            result = rt.getAngle();
        }
        return result;
    }

    public void setRayTurnout(int index, String turnoutName, int state) {
        boolean found = false; // assume failure (pessimist!)
        for (RayTrack rt : rayList) {
            if (rt.getConnectionIndex() == index) {
                rt.setTurnout(turnoutName, state);
                found = true;
                break;
            }
        }
        if (!found) {
            log.error("Attempt to add Turnout control to a non-existant ray track");
        }
    }

    public String getRayTurnoutName(int i) {
        String result = null;
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            result = rt.getTurnoutName();
        }
        return result;
    }

    public Turnout getRayTurnout(int i) {
        Turnout result = null;
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            result = rt.getTurnout();
        }
        return result;
    }

    public int getRayTurnoutState(int i) {
        int result = 0;
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            result = rt.getTurnoutState();
        }
        return result;
    }

    public Point2D getRayCoordsIndexed(int index) {
        Point2D result = MathUtil.zeroPoint2D();
        for (RayTrack rt : rayList) {
            if (rt.getConnectionIndex() == index) {
                double angle = Math.toRadians(rt.getAngle());
                // calculate coordinates
                result = new Point2D.Double(
                        (center.getX() + ((1.25 * radius) * Math.sin(angle))),
                        (center.getY() - ((1.25 * radius) * Math.cos(angle))));
                break;
            }
        }
        return result;
    }

    public Point2D getRayCoordsOrdered(int i) {
        Point2D result = MathUtil.zeroPoint2D();
        RayTrack rt = rayList.get(i);
        if (rt != null) {
            double angle = Math.toRadians(rt.getAngle());
            // calculate coordinates
            result = new Point2D.Double(
                    (center.getX() + ((1.25 * radius) * Math.sin(angle))),
                    (center.getY() - ((1.25 * radius) * Math.cos(angle))));
        }
        return result;
    }

    public void setRayCoordsIndexed(double x, double y, int index) {
        boolean found = false; // assume failure (pessimist!)
        for (RayTrack rt : rayList) {
            if (rt.getConnectionIndex() == index) {
                // convert these coordinates to an angle
                double angle = Math.atan2(x - center.getX(), y - center.getY());
                angle = MathUtil.wrapPM360(180.0 - Math.toDegrees(angle));
                rt.setAngle(angle);
                found = true;
                break;
            }
        }
        if (!found) {
            log.error("Attempt to move a non-existant ray track");
        }
    }

    /**
     * Get the coordinates for a specified connection type.
     *
     * @param locationType the connection type
     * @return the coordinates for the specified connection type
     */
    public Point2D getCoordsForConnectionType(int locationType) {
        Point2D result = getCoordsCenter();
        if (TURNTABLE_CENTER == locationType) {
            // nothing to do here, move along...
            // (results are already correct)
        } else if (locationType >= TURNTABLE_RAY_OFFSET) {
            result = getRayCoordsIndexed(locationType - TURNTABLE_RAY_OFFSET);
        } else {
            log.error("Invalid connection type " + locationType); // NOI18N
        }
        return result;
    }

    /**
     * get the object connected to this track for the specified connection type
     * @param connectionType the specified connection type
     * @return the object connected to this slip for the specified connection type
     * @throws jmri.JmriException - if the connectionType is invalid
     */
    @Override
    public Object getConnection(int connectionType) throws jmri.JmriException {
        Object result = null;
        if (connectionType >= TURNTABLE_RAY_OFFSET) {
            result = getRayConnectIndexed(connectionType - TURNTABLE_RAY_OFFSET);
        } else {
            log.error("Invalid Turntable connection type " + connectionType); // NOI18N
            throw new jmri.JmriException("Invalid Point");
        }
        return result;
    }

    /**
     * Set the object connected to this turnout for the specified connection type.
     *
     * @param connectionType the connection type (where it is connected to the us)
     * @param o the object that is being connected
     * @param type the type of object that we're being connected to (Should always be "NONE" or "TRACK")
     * @throws jmri.JmriException - if connectionType or type are invalid
     */
    @Override
    public void setConnection(int connectionType, Object o, int type) throws jmri.JmriException {
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of connection to LevelXing - " + type);
            throw new jmri.JmriException("unexpected type of connection to LevelXing - " + type);
        }
        if (connectionType >= TURNTABLE_RAY_OFFSET) {
            setRayConnect((TrackSegment) o, connectionType - TURNTABLE_RAY_OFFSET);
        } else {
            log.error("Invalid Connection Type " + connectionType); // NOI18N
            throw new jmri.JmriException("Invalid Connection Type " + connectionType);
        }
    }

    /**
     * Test if ray is a mainline track or not.
     * <p>
     * Defaults to false (not mainline) if
     * connecting track segment is missing.
     * @return true if connecting track segment is mainline
     */
    public boolean isMainlineIndexed(int index) {
        boolean result = false; // assume failure (pessimist!)

        for (RayTrack rt : rayList) {
            if (rt.getConnectionIndex() == index) {
                TrackSegment ts = rt.getConnect();
                if (ts != null) {
                    result = ts.getMainline();
                    break;
                }
            }
        }
        return result;
    }

    public boolean isMainlineOrdered(int i) {
        boolean result = false; // assume failure (pessimist!)
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            if (rt != null) {
                TrackSegment ts = rt.getConnect();
                if (ts != null) {
                    result = ts.getMainline();
                }
            }
        }
        return result;
    }

    /**
     * Modify coordinates methods
     */

    /**
     * scale this LayoutTrack's coordinates by the x and y factors
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
    public void scaleCoords(float xFactor, float yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        center = MathUtil.granulize(MathUtil.multiply(center, factor), 1.0);
    }

    /**
     * translate this LayoutTrack's coordinates by the x and y factors
     * @param xFactor the amount to translate X coordinates
     * @param yFactor the amount to translate Y coordinates
     */
    @Override
    public void translateCoords(float xFactor, float yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        center = MathUtil.add(center, factor);
    }

    /**
     * find the hit (location) type for a point
     * @param p the point
     * @param useRectangles - whether to use (larger) rectangles or (smaller) circles for hit testing
     * @param requireUnconnected - whether to only return hit types for free connections
     * @return the location type for the point (or NONE)
     * @since 7.4.3
     */
    protected int findHitPointType(Point2D p, boolean useRectangles, boolean requireUnconnected) {
        int result = NONE;  // assume point not on connection

        Rectangle2D r = layoutEditor.trackControlCircleRectAt(p);

        if (!requireUnconnected) {
            //check the center point
            if (r.contains(getCoordsCenter())) {
                result = LayoutTrack.TURNTABLE_CENTER;
            }
        }

        for (int k = 0; k < getNumberRays(); k++) {
            if (!requireUnconnected || (getRayConnectOrdered(k) == null)) {
                if (r.contains(getRayCoordsOrdered(k))) {
                    result = LayoutTrack.TURNTABLE_RAY_OFFSET + getRayIndex(k);
                }
            }
        }
        return result;
    }

    /**
     * Initialization method The name of each track segment connected to a ray
     * track is initialized by by LayoutTurntableXml, then the following method
     * is called after the entire LayoutEditor is loaded to set the specific
     * TrackSegment objects.
     */
    public void setObjects(LayoutEditor p) {
        for (RayTrack rt : rayList) {
            rt.setConnect(p.getFinder().findTrackSegmentByName(rt.connectName));
        }
    }

    public boolean isTurnoutControlled() {
        return dccControlledTurnTable;
    }

    public void setTurnoutControlled(boolean boo) {
        dccControlledTurnTable = boo;
    }

    JPopupMenu popup = null;

    /**
     * Display popup menu for information and editing
     */
    protected void showPopUp(MouseEvent e) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }
        JMenuItem jmi = popup.add(rb.getString("Turntable"));
        jmi.setEnabled(false);
        popup.add(new JSeparator(JSeparator.HORIZONTAL));
        popup.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editTurntable(instance);
            }
        });
        popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (layoutEditor.removeTurntable(instance)) {
                    // Returned true if user did not cancel
                    remove();
                    dispose();
                }
            }
        });
        layoutEditor.setShowAlignmentMenu(popup);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    JPopupMenu rayPopup = null;

    protected void showRayPopUp(MouseEvent e, int index) {
        if (rayPopup != null) {
            rayPopup.removeAll();
        } else {
            rayPopup = new JPopupMenu();
        }

        for (RayTrack rt : rayList) {
            if (rt.getConnectionIndex() == index) {
                JMenuItem jmi = rayPopup.add("Turntable Ray " + index);
                jmi.setEnabled(false);
                if (rt.getTurnout() != null) {
                    jmi = rayPopup.add(rt.getTurnout().getDisplayName() + " (" + rt.getTurnoutState() + ")");
                    jmi.setEnabled(false);
                }
                rayPopup.show(e.getComponent(), e.getX(), e.getY());
                break;
            }
        }
    }

    public void setPosition(int index) {
        if (isTurnoutControlled()) {
            boolean found = false; // assume failure (pessimist!)
            for (RayTrack rt : rayList) {
                if (rt.getConnectionIndex() == index) {
                    lastKnownIndex = index;
                    rt.setPosition();
                    layoutEditor.redrawPanel();
                    layoutEditor.setDirty();
                    needsRedraw = false;
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.error("Attempt to set the position on a non-existant ray track");
            }
        }
    }

    public int getPosition() {
        return lastKnownIndex;
    }

    // variables for Edit Turntable pane
    JmriJFrame editTurntableFrame = null;
    JTextField radiusField = new JTextField(8);
    JTextField angleField = new JTextField(8);
    JButton turntableEditDone;
    JButton turntableEditCancel;
    JButton addRayTrack;
    JButton deleteRayTrack;
    JCheckBox dccControlled;
    String oldRadius = "";
    JPanel rayPanel;
    boolean editOpen = false;
    boolean needsRedraw = false;

    /**
     * Edit a Turntable
     */
    protected void editTurntable(LayoutTurntable x) {
        if (editOpen) {
            editTurntableFrame.setVisible(true);
            return;
        }
        needsRedraw = false;
        // Initialize if needed
        if (editTurntableFrame == null) {
            editTurntableFrame = new JmriJFrame(rb.getString("EditTurntable"), false, true);
            editTurntableFrame.addHelpMenu("package.jmri.jmrit.display.EditTurntable", true);
            editTurntableFrame.setLocation(50, 30);
            Container contentPane = editTurntableFrame.getContentPane();
            JPanel headerPane = new JPanel();
            JPanel footerPane = new JPanel();
            headerPane.setLayout(new BoxLayout(headerPane, BoxLayout.Y_AXIS));
            footerPane.setLayout(new BoxLayout(footerPane, BoxLayout.Y_AXIS));
            contentPane.setLayout(new BorderLayout());
            contentPane.add(headerPane, BorderLayout.NORTH);
            contentPane.add(footerPane, BorderLayout.SOUTH);
            // setup radius
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel radiusLabel = new JLabel(rb.getString("TurntableRadius"));
            panel1.add(radiusLabel);
            panel1.add(radiusField);
            radiusField.setToolTipText(rb.getString("TurntableRadiusHint"));
            headerPane.add(panel1);
            // setup add ray track
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel rayAngleLabel = new JLabel(rb.getString("RayAngle"));
            panel2.add(rayAngleLabel);
            panel2.add(angleField);
            angleField.setToolTipText(rb.getString("RayAngleHint"));
            headerPane.add(panel2);
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            panel3.add(addRayTrack = new JButton(rb.getString("AddRayTrack")));
            addRayTrack.setToolTipText(rb.getString("AddRayTrackHint"));
            addRayTrack.addActionListener((ActionEvent e) -> {
                addRayTrackPressed(e);
                updateRayPanel();
            });

            panel3.add(dccControlled = new JCheckBox(rb.getString("TurntableDCCControlled")));
            dccControlled.setSelected(isTurnoutControlled());
            dccControlled.addActionListener((ActionEvent e) -> {
                setTurnoutControlled(dccControlled.isSelected());
                for (RayTrack rt : rayList) {
                    rt.showTurnoutDetails();
                }
                editTurntableFrame.pack();
            });
            headerPane.add(panel3);
            // set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(turntableEditDone = new JButton(Bundle.getMessage("ButtonDone")));
            turntableEditDone.addActionListener((ActionEvent e) -> {
                turntableEditDonePressed(e);
            });

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(turntableEditDone);
                rootPane.setDefaultButton(turntableEditDone);
            });

            turntableEditDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));
            // Cancel
            panel5.add(turntableEditCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            turntableEditCancel.addActionListener((ActionEvent e) -> {
                turntableEditCancelPressed(e);
            });
            turntableEditCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            footerPane.add(panel5);

            rayPanel = new JPanel();
            rayPanel.setLayout(new BoxLayout(rayPanel, BoxLayout.Y_AXIS));
            for (RayTrack rt : rayList) {
                rayPanel.add(rt.getPanel());
            }
            JScrollPane rayScrollPane = new JScrollPane(rayPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            contentPane.add(rayScrollPane, BorderLayout.CENTER);
        } else {
            updateRayPanel();
        }
        // Set up for Edit
        radiusField.setText(" " + radius);
        oldRadius = radiusField.getText();
        angleField.setText("0");
        editTurntableFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                turntableEditCancelPressed(null);
            }
        });
        editTurntableFrame.pack();
        editTurntableFrame.setVisible(true);
        editOpen = true;
    }

    //Remove old rays and add them back in
    private void updateRayPanel() {
        for (Component comp : rayPanel.getComponents()) {
            rayPanel.remove(comp);
        }

        rayPanel.setLayout(new BoxLayout(rayPanel, BoxLayout.Y_AXIS));
        for (RayTrack rt : rayList) {
            rayPanel.add(rt.getPanel());
        }
        rayPanel.revalidate();
        rayPanel.repaint();
        editTurntableFrame.pack();
    }

    private void saveRayPanelDetail() {
        for (RayTrack rt : rayList) {
            rt.updateDetails();
        }
    }

    private void addRayTrackPressed(ActionEvent a) {
        double ang = 0.0;
        try {
            ang = Float.parseFloat(angleField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(editTurntableFrame, rb.getString("EntryError") + ": "
                    + e + rb.getString("TryAgain"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        addRay(ang);
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
        needsRedraw = false;
    }

    void deleteRayTrackPressed(ActionEvent a) {
        double ang = 0.0;
        try {
            ang = Float.parseFloat(angleField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(editTurntableFrame, rb.getString("EntryError") + ": "
                    + e + rb.getString("TryAgain"), Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // scan rays to find the one to delete
        RayTrack closest = null;
        double bestDel = 360.0;
        for (RayTrack rt : rayList) {
            double del = MathUtil.absDiffAngleDEG(rt.getAngle(), ang);
            if (del < bestDel) {
                bestDel = del;
                closest = rt;
            }
        }
        if (bestDel > 30.0) {
            JOptionPane.showMessageDialog(editTurntableFrame, rb.getString("Error13"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        deleteRay(closest);
    }

    void deleteRay(RayTrack closest) {
        TrackSegment t = null;
        if (closest == null) {
            log.error("closest is null!");
        } else {
            t = closest.getConnect();
            rayList.remove(closest.getConnectionIndex());
            closest.dispose();
        }
        if (t != null) {
            layoutEditor.removeTrackSegment(t);
        }

        // update the panel
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
        needsRedraw = false;
    }

    void turntableEditDonePressed(ActionEvent a) {
        // check if new radius was entered
        String str = radiusField.getText();
        if (!str.equals(oldRadius)) {
            double rad = 0.0;
            try {
                rad = Float.parseFloat(str);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(editTurntableFrame, rb.getString("EntryError") + ": "
                        + e + rb.getString("TryAgain"), Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            radius = rad;
            needsRedraw = true;
        }
        // clean up
        editOpen = false;
        editTurntableFrame.setVisible(false);
        editTurntableFrame.dispose();
        editTurntableFrame = null;
        saveRayPanelDetail();
        if (needsRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }

    void turntableEditCancelPressed(ActionEvent a) {
        editOpen = false;
        editTurntableFrame.setVisible(false);
        editTurntableFrame.dispose();
        editTurntableFrame = null;
        if (needsRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }

    /**
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see remove()
     */
    void dispose() {
        if (popup != null) {
            popup.removeAll();
        }
        popup = null;
        for (RayTrack rt : rayList) {
            rt.dispose();
        }
    }

    /**
     * Removes this object from display and persistance
     */
    void remove() {
        // remove from persistance by flagging inactive
        active = false;
    }

    boolean active = true;

    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }

    class RayTrack {

        public RayTrack(double angle, int index) {
            rayAngle = MathUtil.wrapPM360(angle);
            connect = null;
            connectionIndex = index;
        }

        // persistant instance variables
        double rayAngle = 0.0;
        TrackSegment connect = null;
        int connectionIndex = -1;

        // accessor routines
        public TrackSegment getConnect() {
            return connect;
        }

        public void setConnect(TrackSegment ts) {
            connect = ts;
        }

        public double getAngle() {
            return rayAngle;
        }

        public void setAngle(double an) {
            rayAngle = MathUtil.wrapPM360(an);
        }

        public int getConnectionIndex() {
            return connectionIndex;
        }

        // initialization instance variable (used when loading a LayoutEditor)
        public String connectName = "";

        NamedBeanHandle<Turnout> namedTurnout;
        //Turnout t;
        int turnoutState;
        private java.beans.PropertyChangeListener mTurnoutListener;

        public void setTurnout(String turnoutName, int state) {
            Turnout turnout = null;
            if (mTurnoutListener == null) {
                mTurnoutListener = (java.beans.PropertyChangeEvent e) -> {
                    if (getTurnout().getKnownState() == turnoutState) {
                        lastKnownIndex = connectionIndex;
                        layoutEditor.redrawPanel();
                        layoutEditor.setDirty();
                    }
                };
            }
            if (turnoutName != null) {
                turnout = jmri.InstanceManager.turnoutManagerInstance().
                        getTurnout(turnoutName);
            }
            if (namedTurnout != null && namedTurnout.getBean() != turnout) {
                namedTurnout.getBean().removePropertyChangeListener(mTurnoutListener);
            }
            if (turnout != null && (namedTurnout == null || namedTurnout.getBean() != turnout)) {
                namedTurnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutName, turnout);
                turnout.addPropertyChangeListener(mTurnoutListener, turnoutName, "Layout Editor Turntable");
                needsRedraw = true;
            }
            if (turnout == null) {
                namedTurnout = null;
            }

            if (this.turnoutState != state) {
                this.turnoutState = state;
                needsRedraw = true;
            }
        }

        public void setPosition() {
            if (namedTurnout != null) {
                getTurnout().setCommandedState(turnoutState);
            }
        }

        public Turnout getTurnout() {
            if (namedTurnout == null) {
                return null;
            }
            return namedTurnout.getBean();
        }

        public String getTurnoutName() {
            if (namedTurnout == null) {
                return null;
            }
            return namedTurnout.getName();
        }

        public int getTurnoutState() {
            return turnoutState;
        }

        JPanel panel;
        JPanel turnoutPanel;
        BeanSelectCreatePanel beanBox;
        TitledBorder border;
        JComboBox<String> turnoutStateCombo;
        JLabel turnoutStateLabel;
        JTextField angle;
        final int[] turnoutStateValues = new int[]{Turnout.CLOSED, Turnout.THROWN};
        final DecimalFormat twoDForm = new DecimalFormat("#.00");

        public JPanel getPanel() {
            if (panel == null) {
                JPanel top = new JPanel();
                /*JLabel lbl = new JLabel("Index :"+connectionIndex);
                 top.add(lbl);*/
                top.add(new JLabel(rb.getString("RayAngle") + " : "));
                top.add(angle = new JTextField(5));
                angle.addFocusListener(
                        new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        try {
                            Float.parseFloat(angle.getText());
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(editTurntableFrame, rb.getString("EntryError") + ": "
                                    + ex + rb.getString("TryAgain"), Bundle.getMessage("ErrorTitle"),
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
                );
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.add(top);

                beanBox = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), getTurnout());
                String turnoutStateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
                String turnoutStateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
                String[] turnoutStates = new String[]{turnoutStateClosed, turnoutStateThrown};

                turnoutStateCombo = new JComboBox<String>(turnoutStates);
                turnoutStateLabel = new JLabel(rb.getString("TurnoutState"));
                turnoutPanel = new JPanel();

                turnoutPanel.setBorder(new EtchedBorder());
                turnoutPanel.add(beanBox);
                turnoutPanel.add(turnoutStateLabel);
                turnoutPanel.add(turnoutStateCombo);
                if (turnoutState == Turnout.CLOSED) {
                    turnoutStateCombo.setSelectedItem(turnoutStateClosed);
                } else {
                    turnoutStateCombo.setSelectedItem(turnoutStateThrown);
                }
                panel.add(turnoutPanel);

                JButton deleteRayButton;
                top.add(deleteRayButton = new JButton(Bundle.getMessage("ButtonDelete")));
                deleteRayButton.setToolTipText(rb.getString("DeleteRayTrack"));
                deleteRayButton.addActionListener((ActionEvent e) -> {
                    delete();
                    updateRayPanel();
                });
                border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));

                panel.setBorder(border);
            }
            showTurnoutDetails();

            angle.setText(twoDForm.format(getAngle()));
            border.setTitle(rb.getString("Ray") + " : " + connectionIndex);
            if (connect == null) {
                border.setTitle(rb.getString("Unconnected") + " : " + connectionIndex);
            } else if (connect.getLayoutBlock() != null) {
                border.setTitle(rb.getString("Connected") + " : " + connect.getLayoutBlock().getDisplayName());
            }
            return panel;
        }

        void delete() {
            int n = JOptionPane.showConfirmDialog(null,
                    rb.getString("Question7"),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.NO_OPTION) {
                return;
            }
            deleteRay(this);
        }

        void updateDetails() {
            if (beanBox == null || turnoutStateCombo == null) {
                return;
            }
            setTurnout(beanBox.getDisplayName(), turnoutStateValues[turnoutStateCombo.getSelectedIndex()]);
            if (!angle.getText().equals(twoDForm.format(getAngle()))) {
                try {
                    double ang = Float.parseFloat(angle.getText());
                    setAngle(ang);
                    needsRedraw = true;
                } catch (Exception e) {
                    log.error("Angle is not in correct format so will skip " + angle.getText());
                }
            }
        }

        void showTurnoutDetails() {
            turnoutPanel.setVisible(isTurnoutControlled());
            beanBox.setVisible(isTurnoutControlled());
            turnoutStateCombo.setVisible(isTurnoutControlled());
            turnoutStateLabel.setVisible(isTurnoutControlled());
        }

        void dispose() {
            if (getTurnout() != null) {
                getTurnout().removePropertyChangeListener(mTurnoutListener);
            }
            if (lastKnownIndex == connectionIndex) {
                lastKnownIndex = -1;
            }
        }
    }

    /**
     * draw this turntable
     *
     * @param g2 the graphics port to draw to
     */
    public void draw(Graphics2D g2) {
        // draw turntable circle - default track color, side track width
        layoutEditor.setTrackStrokeWidth(g2, false);
        double r = getRadius(), d = r + r;
        g2.setColor(defaultTrackColor);
        g2.draw(new Ellipse2D.Double(center.getX() - r, center.getY() - r, d, d));

        // draw ray tracks
        for (int j = 0; j < getNumberRays(); j++) {
            TrackSegment ts = getRayConnectOrdered(j);
            if (ts != null) {
                layoutEditor.setTrackStrokeWidth(g2, ts.getMainline());
                setColorForTrackBlock(g2, ts.getLayoutBlock());
            } else {
                layoutEditor.setTrackStrokeWidth(g2, false);
                g2.setColor(defaultTrackColor);
            }
            Point2D pt = getRayCoordsOrdered(j);
            g2.draw(new Line2D.Double(new Point2D.Double(
                    pt.getX() - ((pt.getX() - center.getX()) * 0.2),
                    pt.getY() - ((pt.getY() - center.getY()) * 0.2)), pt));
        }
        if (isTurnoutControlled() && getPosition() != -1) {
            Point2D pt = getRayCoordsIndexed(getPosition());
            g2.draw(new Line2D.Double(new Point2D.Double(
                    pt.getX() - ((pt.getX() - center.getX()) * 1.8/* 2 */),
                    pt.getY() - ((pt.getY() - center.getY()) * 1.8/* * * 2 */)), pt));
        }
    }

    public void drawEditControls(Graphics2D g2) {
        Point2D pt = getCoordsCenter();
        g2.setColor(defaultTrackColor);
        g2.draw(layoutEditor.trackControlPointRectAt(pt));

        for (int j = 0; j < getNumberRays(); j++) {
            pt = getRayCoordsOrdered(j);

            if (getRayConnectOrdered(j) == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
            g2.draw(layoutEditor.trackControlPointRectAt(pt));
        }
    }

    public void reCheckBlockBoundary()
    {
        // nothing to do here... move along...
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutTurntable.class.getName());
}
