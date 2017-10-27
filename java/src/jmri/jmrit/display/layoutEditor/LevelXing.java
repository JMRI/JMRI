package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.SignalMastLogic;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.jmrit.signalling.SignallingGuiTools;
import jmri.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LevelXing is two track segment on a layout that cross at an angle.
 * <P>
 * A LevelXing has four connection points, designated A, B, C, and D. At the
 * crossing, A-C and B-D are straight segments. A train proceeds through the
 * crossing on either of these segments.
 * <P>
 * {@literal
 *    A   D
 *    \\ //
 *      X
 *    // \\
 *    B   C
 * literal}
 * <P>
 * Each straight segment carries Block information. A-C and B-D may be in the
 * same or different Layout Blocks.
 * <P>
 * For drawing purposes, each LevelXing carries a center point and displacements
 * for A and B. The displacements for C = - the displacement for A, and the
 * displacement for D = - the displacement for B. The center point and these
 * displacements may be adjusted by the user when in edit mode.
 * <P>
 * When LevelXings are first created, there are no connections. Block
 * information and connections are added when available.
 * <P>
 * Signal Head names are saved here to keep track of where signals are.
 * LevelXing only serves as a storage place for signal head names. The names are
 * placed here by Set Signals at Level Crossing in Tools menu.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 */
public class LevelXing extends LayoutTrack {

    // defined constants
    // operational instance variables (not saved between sessions)
    private LayoutBlock blockAC = null;
    private LayoutBlock blockBD = null;

    // persistent instances variables (saved between sessions)
    private String blockNameAC = "";
    private String blockNameBD = "";

    protected NamedBeanHandle<SignalHead> signalAHeadNamed = null; // signal at A track junction
    protected NamedBeanHandle<SignalHead> signalBHeadNamed = null; // signal at B track junction
    protected NamedBeanHandle<SignalHead> signalCHeadNamed = null; // signal at C track junction
    protected NamedBeanHandle<SignalHead> signalDHeadNamed = null; // signal at D track junction

    protected NamedBeanHandle<SignalMast> signalAMastNamed = null; // signal at A track junction
    protected NamedBeanHandle<SignalMast> signalBMastNamed = null; // signal at B track junction
    protected NamedBeanHandle<SignalMast> signalCMastNamed = null; // signal at C track junction
    protected NamedBeanHandle<SignalMast> signalDMastNamed = null; // signal at D track junction

    private NamedBeanHandle<Sensor> sensorANamed = null; // sensor at A track junction
    private NamedBeanHandle<Sensor> sensorBNamed = null; // sensor at B track junction
    private NamedBeanHandle<Sensor> sensorCNamed = null; // sensor at C track junction
    private NamedBeanHandle<Sensor> sensorDNamed = null; // sensor at D track junction

    private LayoutTrack connectA = null;
    private LayoutTrack connectB = null;
    private LayoutTrack connectC = null;
    private LayoutTrack connectD = null;

    private Point2D dispA = new Point2D.Double(-20.0, 0.0);
    private Point2D dispB = new Point2D.Double(-14.0, 14.0);

    public static final int POINTA = 0x01;
    public static final int POINTB = 0x10;
    public static final int POINTC = 0x20;
    public static final int POINTD = 0x30;

    /**
     * constructor method
     */
    public LevelXing(String id, Point2D c, LayoutEditor layoutEditor) {
        super(id, c, layoutEditor);
    }

    // this should only be used for debugging...
    public String toString() {
        return "LevelXing " + getName();
    }

    /**
     * Accessor methods
     */
    public String getBlockNameAC() {
        return blockNameAC;
    }

    public String getBlockNameBD() {
        return blockNameBD;
    }

    public SignalHead getSignalHead(int loc) {
        NamedBeanHandle<SignalHead> namedBean = null;
        switch (loc) {
            case POINTA:
                namedBean = signalAHeadNamed;
                break;
            case POINTB:
                namedBean = signalBHeadNamed;
                break;
            case POINTC:
                namedBean = signalCHeadNamed;
                break;
            case POINTD:
                namedBean = signalDHeadNamed;
                break;
            default:
                log.warn("Unhandled loc: {}", loc);
                break;
        }
        if (namedBean != null) {
            return namedBean.getBean();
        }
        return null;
    }

    public SignalMast getSignalMast(int loc) {
        NamedBeanHandle<SignalMast> namedBean = null;
        switch (loc) {
            case POINTA:
                namedBean = signalAMastNamed;
                break;
            case POINTB:
                namedBean = signalBMastNamed;
                break;
            case POINTC:
                namedBean = signalCMastNamed;
                break;
            case POINTD:
                namedBean = signalDMastNamed;
                break;
            default:
                log.warn("Unhandled loc: {}", loc);
                break;
        }
        if (namedBean != null) {
            return namedBean.getBean();
        }
        return null;
    }

    public Sensor getSensor(int loc) {
        NamedBeanHandle<Sensor> namedBean = null;
        switch (loc) {
            case POINTA:
                namedBean = sensorANamed;
                break;
            case POINTB:
                namedBean = sensorBNamed;
                break;
            case POINTC:
                namedBean = sensorCNamed;
                break;
            case POINTD:
                namedBean = sensorDNamed;
                break;
            default:
                log.warn("Unhandled loc: {}", loc);
                break;
        }
        if (namedBean != null) {
            return namedBean.getBean();
        }
        return null;
    }

    public String getSignalAName() {
        if (signalAHeadNamed != null) {
            return signalAHeadNamed.getName();
        }
        return "";
    }

    public void setSignalAName(String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalAHeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalAHeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalAHeadNamed = null;
        }
    }

    public String getSignalBName() {
        if (signalBHeadNamed != null) {
            return signalBHeadNamed.getName();
        }
        return "";
    }

    public void setSignalBName(String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalBHeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalBHeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalBHeadNamed = null;
        }
    }

    public String getSignalCName() {
        if (signalCHeadNamed != null) {
            return signalCHeadNamed.getName();
        }
        return "";
    }

    public void setSignalCName(String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalCHeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalCHeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalCHeadNamed = null;
        }
    }

    public String getSignalDName() {
        if (signalDHeadNamed != null) {
            return signalDHeadNamed.getName();
        }
        return "";
    }

    public void setSignalDName(String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalDHeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalDHeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalDHeadNamed = null;
        }
    }

    public void removeBeanReference(jmri.NamedBean nb) {
        if (nb == null) {
            return;
        }
        if (nb instanceof SignalMast) {
            if (nb.equals(getSignalAMast())) {
                setSignalAMast(null);
                return;
            }
            if (nb.equals(getSignalBMast())) {
                setSignalBMast(null);
                return;
            }
            if (nb.equals(getSignalCMast())) {
                setSignalCMast(null);
                return;
            }
            if (nb.equals(getSignalDMast())) {
                setSignalDMast(null);
                return;
            }
        }
        if (nb instanceof Sensor) {
            if (nb.equals(getSensorA())) {
                setSensorAName(null);
                return;
            }
            if (nb.equals(getSensorB())) {
                setSensorBName(null);
                return;
            }
            if (nb.equals(getSensorC())) {
                setSensorCName(null);
                return;
            }
            if (nb.equals(getSensorD())) {
                setSensorDName(null);
                return;
            }
        }
        if (nb instanceof SignalHead) {
            if (nb.equals(getSignalHead(POINTA))) {
                setSignalAName(null);
                return;
            }
            if (nb.equals(getSignalHead(POINTB))) {
                setSignalBName(null);
                return;
            }
            if (nb.equals(getSignalHead(POINTC))) {
                setSignalCName(null);
                return;
            }
            if (nb.equals(getSignalHead(POINTD))) {
                setSignalDName(null);
                return;
            }
        }
    }

    public String getSignalAMastName() {
        if (signalAMastNamed != null) {
            return signalAMastNamed.getName();
        }
        return "";
    }

    public SignalMast getSignalAMast() {
        if (signalAMastNamed != null) {
            return signalAMastNamed.getBean();
        }
        return null;
    }

    public void setSignalAMast(String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalAMastNamed = null;
            return;
        }

        try {
            SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(signalMast);
            signalAMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } catch (IllegalArgumentException ex) {
            signalAMastNamed = null;
        }
    }

    public String getSignalBMastName() {
        if (signalBMastNamed != null) {
            return signalBMastNamed.getName();
        }
        return "";
    }

    public SignalMast getSignalBMast() {
        if (signalBMastNamed != null) {
            return signalBMastNamed.getBean();
        }
        return null;
    }

    public void setSignalBMast(String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalBMastNamed = null;
            return;
        }

        try {
            SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(signalMast);
            signalBMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } catch (IllegalArgumentException ex) {
            signalBMastNamed = null;
        }
    }

    public String getSignalCMastName() {
        if (signalCMastNamed != null) {
            return signalCMastNamed.getName();
        }
        return "";
    }

    public SignalMast getSignalCMast() {
        if (signalCMastNamed != null) {
            return signalCMastNamed.getBean();
        }
        return null;
    }

    public void setSignalCMast(String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalCMastNamed = null;
            return;
        }

        try {
            SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(signalMast);
            signalCMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } catch (IllegalArgumentException ex) {
            signalCMastNamed = null;
        }
    }

    public String getSignalDMastName() {
        if (signalDMastNamed != null) {
            return signalDMastNamed.getName();
        }
        return "";
    }

    public SignalMast getSignalDMast() {
        if (signalDMastNamed != null) {
            return signalDMastNamed.getBean();
        }
        return null;
    }

    public void setSignalDMast(String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalDMastNamed = null;
            return;
        }

        try {
            SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(signalMast);
            signalDMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } catch (IllegalArgumentException ex) {
            signalDMastNamed = null;
        }
    }

    public String getSensorAName() {
        if (sensorANamed != null) {
            return sensorANamed.getName();
        }
        return "";
    }

    public Sensor getSensorA() {
        if (sensorANamed != null) {
            return sensorANamed.getBean();
        }
        return null;
    }

    public void setSensorAName(String sensorName) {
        if (sensorName == null || sensorName.isEmpty()) {
            sensorANamed = null;
            return;
        }

        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
            sensorANamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } catch (IllegalArgumentException ex) {
            sensorANamed = null;
        }
    }

    public String getSensorBName() {
        if (sensorBNamed != null) {
            return sensorBNamed.getName();
        }
        return "";
    }

    public Sensor getSensorB() {
        if (sensorBNamed != null) {
            return sensorBNamed.getBean();
        }
        return null;
    }

    public void setSensorBName(String sensorName) {
        if (sensorName == null || sensorName.isEmpty()) {
            sensorBNamed = null;
            return;
        }

        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
            sensorBNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } catch (IllegalArgumentException ex) {
            sensorBNamed = null;
        }
    }

    public String getSensorCName() {
        if (sensorCNamed != null) {
            return sensorCNamed.getName();
        }
        return "";
    }

    public Sensor getSensorC() {
        if (sensorCNamed != null) {
            return sensorCNamed.getBean();
        }
        return null;
    }

    public void setSensorCName(String sensorName) {
        if (sensorName == null || sensorName.isEmpty()) {
            sensorCNamed = null;
            return;
        }

        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
            sensorCNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } catch (IllegalArgumentException ex) {
            sensorCNamed = null;
        }
    }

    public String getSensorDName() {
        if (sensorDNamed != null) {
            return sensorDNamed.getName();
        }
        return "";
    }

    public Sensor getSensorD() {
        if (sensorDNamed != null) {
            return sensorDNamed.getBean();
        }
        return null;
    }

    public void setSensorDName(String sensorName) {
        if (sensorName == null || sensorName.isEmpty()) {
            sensorDNamed = null;
            return;
        }

        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
            sensorDNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } catch (IllegalArgumentException ex) {
            sensorDNamed = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutTrack getConnection(int connectionType) throws jmri.JmriException {
        switch (connectionType) {
            case LEVEL_XING_A:
                return connectA;
            case LEVEL_XING_B:
                return connectB;
            case LEVEL_XING_C:
                return connectC;
            case LEVEL_XING_D:
                return connectD;
            default:
                log.warn("Unhandled loc: {}", connectionType);
                break;
        }
        log.error("Invalid Point Type " + connectionType); //I18IN
        throw new jmri.JmriException("Invalid Point");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnection(int connectionType, LayoutTrack o, int type) throws jmri.JmriException {
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of connection to LevelXing - " + type);
            throw new jmri.JmriException("unexpected type of connection to LevelXing - " + type);
        }
        switch (connectionType) {
            case LEVEL_XING_A:
                connectA = o;
                break;
            case LEVEL_XING_B:
                connectB = o;
                break;
            case LEVEL_XING_C:
                connectC = o;
                break;
            case LEVEL_XING_D:
                connectD = o;
                break;
            default:
                log.error("Invalid Connection Type " + connectionType); //I18IN
                throw new jmri.JmriException("Invalid Connection Type " + connectionType);
        }
    }

    public LayoutTrack getConnectA() {
        return connectA;
    }

    public LayoutTrack getConnectB() {
        return connectB;
    }

    public LayoutTrack getConnectC() {
        return connectC;
    }

    public LayoutTrack getConnectD() {
        return connectD;
    }

    public void setConnectA(LayoutTrack o, int type) {
        connectA = o;
        if ((connectA != null) && (type != TRACK)) {
            log.error("unexpected type of A connection to levelXing - " + type);
        }
    }

    public void setConnectB(LayoutTrack o, int type) {
        connectB = o;
        if ((connectB != null) && (type != TRACK)) {
            log.error("unexpected type of B connection to levelXing - " + type);
        }
    }

    public void setConnectC(LayoutTrack o, int type) {
        connectC = o;
        if ((connectC != null) && (type != TRACK)) {
            log.error("unexpected type of C connection to levelXing - " + type);
        }
    }

    public void setConnectD(LayoutTrack o, int type) {
        connectD = o;
        if ((connectD != null) && (type != TRACK)) {
            log.error("unexpected type of D connection to levelXing - " + type);
        }
    }

    public LayoutBlock getLayoutBlockAC() {
        if ((blockAC == null) && !blockNameAC.isEmpty()) {
            blockAC = layoutEditor.provideLayoutBlock(blockNameAC);
            if ((blockAC != null) && (blockAC == blockBD)) {
                blockAC.decrementUse();
            }
        }
        return blockAC;
    }

    public LayoutBlock getLayoutBlockBD() {
        if ((blockBD == null) && !blockNameBD.isEmpty()) {
            blockBD = layoutEditor.provideLayoutBlock(blockNameBD);
            if ((blockBD != null) && (blockBD == blockAC)) {
                blockBD.decrementUse();
            }
        }
        return blockBD;
    }

    public Point2D getCoordsA() {
        return MathUtil.add(center, dispA);
    }

    public Point2D getCoordsB() {
        return MathUtil.add(center, dispB);
    }

    public Point2D getCoordsC() {
        return MathUtil.subtract(center, dispA);
    }

    public Point2D getCoordsD() {
        return MathUtil.subtract(center, dispB);
    }

    /**
     * return the coordinates for a specified connection type
     *
     * @param connectionType the connection type
     * @return the coordinates for the specified connection type
     */
    public Point2D getCoordsForConnectionType(int connectionType) {
        Point2D result = center;
        switch (connectionType) {
            case LEVEL_XING_CENTER:
                break;
            case LEVEL_XING_A:
                result = getCoordsA();
                break;
            case LEVEL_XING_B:
                result = getCoordsB();
                break;
            case LEVEL_XING_C:
                result = getCoordsC();
                break;
            case LEVEL_XING_D:
                result = getCoordsD();
                break;
            default:
                log.error("Invalid connection type " + connectionType); //I18IN
        }
        return result;
    }

    /**
     * @return the bounds of this crossing
     */
    public Rectangle2D getBounds() {
        Rectangle2D result;

        Point2D pointA = getCoordsA();
        result = new Rectangle2D.Double(pointA.getX(), pointA.getY(), 0, 0);
        result.add(getCoordsB());
        result.add(getCoordsC());
        result.add(getCoordsD());
        return result;
    }

    /**
     * Add Layout Blocks
     */
    public void setLayoutBlockAC(LayoutBlock b) {
        if (blockAC != b) {
            // block 1 has changed, if old block exists, decrement use
            if ((blockAC != null) && (blockAC != blockBD)) {
                blockAC.decrementUse();
            }
            blockAC = b;
            blockNameAC = (b == null) ? "" : b.getId();
            // decrement use if block was previously counted
            if ((blockAC != null) && (blockAC == blockBD)) {
                blockAC.decrementUse();
            }
        }
    }

    public void setLayoutBlockBD(LayoutBlock b) {
        if (blockBD != b) {
            // block 1 has changed, if old block exists, decrement use
            if ((blockBD != null) && (blockBD != blockAC)) {
                blockBD.decrementUse();
            }
            blockBD = b;
            blockNameBD = (b == null) ? "" : b.getId();
            // decrement use if block was previously counted
            if ((blockBD != null) && (blockBD == blockAC)) {
                blockBD.decrementUse();
            }
        }

    }

    protected void updateBlockInfo() {
        LayoutBlock b1 = null;
        LayoutBlock b2 = null;
        if (blockAC != null) {
            blockAC.updatePaths();
        }
        if (connectA != null) {
            b1 = ((TrackSegment) connectA).getLayoutBlock();
            if ((b1 != null) && (b1 != blockAC)) {
                b1.updatePaths();
            }
        }
        if (connectC != null) {
            b2 = ((TrackSegment) connectC).getLayoutBlock();
            if ((b2 != null) && (b2 != blockAC) && (b2 != b1)) {
                b2.updatePaths();
            }
        }
        if (blockBD != null) {
            blockBD.updatePaths();
        }
        if (connectB != null) {
            b1 = ((TrackSegment) connectB).getLayoutBlock();
            if ((b1 != null) && (b1 != blockBD)) {
                b1.updatePaths();
            }
        }
        if (connectD != null) {
            b2 = ((TrackSegment) connectD).getLayoutBlock();
            if ((b2 != null) && (b2 != blockBD) && (b2 != b1)) {
                b2.updatePaths();
            }
        }
        reCheckBlockBoundary();
    }

    void removeSML(SignalMast signalMast) {
        if (signalMast == null) {
            return;
        }
        if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled() && InstanceManager.getDefault(jmri.SignalMastLogicManager.class).isSignalMastUsed(signalMast)) {
            SignallingGuiTools.removeSignalMastLogic(null, signalMast);
        }
    }

    /**
     * Methods to test if mainline track or not Returns true if either
     * connecting track segment is mainline Defaults to not mainline if
     * connecting track segments are missing
     */
    public boolean isMainlineAC() {
        if (((connectA != null) && (((TrackSegment) connectA).isMainline()))
                || ((connectB != null) && (((TrackSegment) connectB).isMainline()))) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isMainlineBD() {
        if (((connectB != null) && (((TrackSegment) connectB).isMainline()))
                || ((connectD != null) && (((TrackSegment) connectD).isMainline()))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Modify coordinates methods
     */
    public void setCoordsA(Point2D p) {
        dispA = MathUtil.subtract(p, center);
    }

    public void setCoordsB(Point2D p) {
        dispB = MathUtil.subtract(p, center);
    }

    public void setCoordsC(Point2D p) {
        dispA = MathUtil.subtract(center, p);
    }

    public void setCoordsD(Point2D p) {
        dispB = MathUtil.subtract(center, p);
    }

    /**
     * scale this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
    public void scaleCoords(float xFactor, float yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        center = MathUtil.granulize(MathUtil.multiply(center, factor), 1.0);
        dispA = MathUtil.granulize(MathUtil.multiply(dispA, factor), 1.0);
        dispB = MathUtil.granulize(MathUtil.multiply(dispB, factor), 1.0);
    }

    /**
     * translate this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to translate X coordinates
     * @param yFactor the amount to translate Y coordinates
     */
    @Override
    public void translateCoords(float xFactor, float yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        center = MathUtil.add(center, factor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int findHitPointType(Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        int result = NONE;  // assume point not on connection

        Rectangle2D r = layoutEditor.trackControlCircleRectAt(hitPoint);

        if (!requireUnconnected) {
            //check the center point
            if (r.contains(getCoordsCenter())) {
                result = LEVEL_XING_CENTER;
            }
        }

        if (!requireUnconnected || (getConnectA() == null)) {
            //check the A connection point
            if (r.contains(getCoordsA())) {
                result = LEVEL_XING_A;
            }
        }

        if (!requireUnconnected || (getConnectB() == null)) {
            //check the B connection point
            if (r.contains(getCoordsB())) {
                //mouse was pressed on this connection point
                result = LEVEL_XING_B;
            }
        }

        if (!requireUnconnected || (getConnectC() == null)) {
            //check the C connection point
            if (r.contains(getCoordsC())) {
                result = LEVEL_XING_C;
            }
        }

        if (!requireUnconnected || (getConnectD() == null)) {
            //check the D connection point
            if (r.contains(getCoordsD())) {
                result = LEVEL_XING_D;
            }
        }
        return result;
    }

    // initialization instance variables (used when loading a LayoutEditor)
    public String connectAName = "";
    public String connectBName = "";
    public String connectCName = "";
    public String connectDName = "";
    public String tBlockNameAC = "";
    public String tBlockNameBD = "";

    /**
     * Initialization method The above variables are initialized by
     * PositionablePointXml, then the following method is called after the
     * entire LayoutEditor is loaded to set the specific TrackSegment objects.
     */
    public void setObjects(LayoutEditor p) {
        connectA = p.getFinder().findTrackSegmentByName(connectAName);
        connectB = p.getFinder().findTrackSegmentByName(connectBName);
        connectC = p.getFinder().findTrackSegmentByName(connectCName);
        connectD = p.getFinder().findTrackSegmentByName(connectDName);
        if (!tBlockNameAC.isEmpty()) {
            blockAC = p.getLayoutBlock(tBlockNameAC);
            if (blockAC != null) {
                blockNameAC = tBlockNameAC;
                if (blockAC != blockBD) {
                    blockAC.incrementUse();
                }
            } else {
                log.error("bad blocknameac '" + tBlockNameAC + "' in levelxing " + getName());
            }
        }
        if (!tBlockNameBD.isEmpty()) {
            blockBD = p.getLayoutBlock(tBlockNameBD);
            if (blockBD != null) {
                blockNameBD = tBlockNameBD;
                if (blockAC != blockBD) {
                    blockBD.incrementUse();
                }
            } else {
                log.error("bad blocknamebd '" + tBlockNameBD + "' in levelxing " + getName());
            }
        }
    }

    JPopupMenu popup = null;
    LayoutEditorTools tools = null;

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected JPopupMenu showPopup(@Nullable MouseEvent mouseEvent) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }
        tools = layoutEditor.getLETools();
        if (layoutEditor.isEditable()) {
            JMenuItem jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("LevelCrossing")) + getName());
            jmi.setEnabled(false);

            boolean blockACAssigned = false;
            boolean blockBDAssigned = false;
            if ((blockNameAC == null) || (blockNameAC.isEmpty())) {
                jmi = popup.add(Bundle.getMessage("NoBlockX", 1));
            } else {
                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", 1)) + getLayoutBlockAC().getDisplayName());
                blockACAssigned = true;
            }
            jmi.setEnabled(false);

            if ((blockNameBD == null) || (blockNameBD.isEmpty())) {
                jmi = popup.add(Bundle.getMessage("NoBlockX", 2));
            } else {
                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", 2)) + getLayoutBlockBD().getDisplayName());
                blockBDAssigned = true;
            }
            jmi.setEnabled(false);

            // if there are any track connections
            if ((connectA != null) || (connectB != null)
                    || (connectC != null) || (connectD != null)) {
                JMenu connectionsMenu = new JMenu(Bundle.getMessage("Connections")); // there is no pane opening (which is what ... implies)
                if (connectA != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "A") + connectA.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectA.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                if (connectB != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "B") + connectB.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectB.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                if (connectC != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "C") + connectC.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectC.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                if (connectD != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "D") + connectD.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectD.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                popup.add(connectionsMenu);
            }

            popup.add(new JSeparator(JSeparator.HORIZONTAL));

            JCheckBoxMenuItem hiddenCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Hidden"));
            hiddenCheckBoxMenuItem.setSelected(hidden);
            popup.add(hiddenCheckBoxMenuItem);
            hiddenCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e3) -> {
                setHidden(hiddenCheckBoxMenuItem.isSelected());
            });

            popup.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    layoutEditor.getLayoutTrackEditors().editLevelXing(LevelXing.this);
                }
            });
            popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (layoutEditor.removeLevelXing(LevelXing.this)) {
                        // Returned true if user did not cancel
                        remove();
                        dispose();
                    }
                }
            });
            if (blockACAssigned && blockBDAssigned) {
                AbstractAction ssaa = new AbstractAction(Bundle.getMessage("SetSignals")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tools = layoutEditor.getLETools();
                        // bring up signals at level crossing tool dialog
                        tools.setSignalsAtLevelXingFromMenu(LevelXing.this,
                                layoutEditor.signalIconEditor, layoutEditor.signalFrame);
                    }
                };
                JMenu jm = new JMenu(Bundle.getMessage("SignalHeads"));
                if (tools.addLevelXingSignalHeadInfoToMenu(LevelXing.this, jm)) {
                    jm.add(ssaa);
                    popup.add(jm);
                } else {
                    popup.add(ssaa);
                }
            }

            final String[] boundaryBetween = getBlockBoundaries();
            boolean blockBoundaries = false;
            if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
                if (blockACAssigned && !blockBDAssigned) {
                    popup.add(new AbstractAction(Bundle.getMessage("ViewBlockRouting")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction("ViewRouting", getLayoutBlockAC());
                            routeTableAction.actionPerformed(e);
                        }
                    });
                } else if (!blockACAssigned && blockBDAssigned) {
                    popup.add(new AbstractAction(Bundle.getMessage("ViewBlockRouting")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction("ViewRouting", getLayoutBlockBD());
                            routeTableAction.actionPerformed(e);
                        }
                    });
                } else if (blockACAssigned && blockBDAssigned) {
                    JMenu viewRouting = new JMenu(Bundle.getMessage("ViewBlockRouting"));
                    viewRouting.add(new AbstractAction(blockNameAC) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction(blockNameAC, getLayoutBlockAC());
                            routeTableAction.actionPerformed(e);
                        }
                    });

                    viewRouting.add(new AbstractAction(blockNameBD) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction(blockNameBD, getLayoutBlockBD());
                            routeTableAction.actionPerformed(e);
                        }
                    });

                    popup.add(viewRouting);
                }
            }

            for (int i = 0; i < 4; i++) {
                if (boundaryBetween[i] != null) {
                    blockBoundaries = true;
                }
            }
            if (blockBoundaries) {
                popup.add(new AbstractAction(Bundle.getMessage("SetSignalMasts")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tools = layoutEditor.getLETools();
                        tools.setSignalMastsAtLevelXingFromMenu(LevelXing.this, boundaryBetween, layoutEditor.signalFrame);
                    }
                });
                popup.add(new AbstractAction(Bundle.getMessage("SetSensors")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tools = layoutEditor.getLETools();
                        tools.setSensorsAtLevelXingFromMenu(LevelXing.this, boundaryBetween, layoutEditor.sensorIconEditor, layoutEditor.sensorFrame);
                    }
                });
            }

            layoutEditor.setShowAlignmentMenu(popup);
            popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        } else if (!viewAdditionalMenu.isEmpty()) {
            setAdditionalViewPopUpMenu(popup);
            popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        }
        return popup;
    }   // showPopup

    public String[] getBlockBoundaries() {
        final String[] boundaryBetween = new String[4];

        if ((blockNameAC != null) && (!blockNameAC.isEmpty()) && (blockAC != null)) {
            if ((connectA instanceof TrackSegment) && (((TrackSegment) connectA).getLayoutBlock() != blockAC)) {
                try {
                    boundaryBetween[0] = (((TrackSegment) connectA).getLayoutBlock().getDisplayName() + " - " + blockAC.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection A doesn't contain a layout block");
                }
            }
            if ((connectC instanceof TrackSegment) && (((TrackSegment) connectC).getLayoutBlock() != blockAC)) {
                try {
                    boundaryBetween[2] = (((TrackSegment) connectC).getLayoutBlock().getDisplayName() + " - " + blockAC.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection C doesn't contain a layout block");
                }
            }
        }
        if ((blockNameBD != null) && (!blockNameBD.isEmpty()) && (blockBD != null)) {
            if ((connectB instanceof TrackSegment) && (((TrackSegment) connectB).getLayoutBlock() != blockBD)) {
                try {
                    boundaryBetween[1] = (((TrackSegment) connectB).getLayoutBlock().getDisplayName() + " - " + blockBD.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection B doesn't contain a layout block");
                }
            }
            if ((connectD instanceof TrackSegment) && (((TrackSegment) connectD).getLayoutBlock() != blockBD)) {
                try {
                    boundaryBetween[3] = (((TrackSegment) connectD).getLayoutBlock().getDisplayName() + " - " + blockBD.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection D doesn't contain a layout block");
                }
            }
        }
        return boundaryBetween;
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

    ArrayList<SignalMast> sml = new ArrayList<>();

    public void addSignalMastLogic(SignalMast sm) {
        if (sml.contains(sm)) {
            return;
        }
        if (sml.isEmpty()) {
            sml.add(sm);
            return;
        }
        SignalMastLogic sl = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(sm);
        for (int i = 0; i < sml.size(); i++) {
            SignalMastLogic s = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(sml.get(i));
            if (s != null) {
                s.setConflictingLogic(sm, this);
            }
            sl.setConflictingLogic(sml.get(i), this);
        }
        sml.add(sm);
    }

    public void removeSignalMastLogic(SignalMast sm) {
        if (!sml.contains(sm)) {
            return;
        }
        sml.remove(sm);
        if (sml.isEmpty()) {
            return;
        }
        for (int i = 0; i < sml.size(); i++) {
            SignalMastLogic s = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(sm);
            if (s != null) {
                s.removeConflictingLogic(sm, this);
            }
        }
    }

    ArrayList<JMenuItem> editAdditionalMenu = new ArrayList<>(0);
    ArrayList<JMenuItem> viewAdditionalMenu = new ArrayList<>(0);

    public void addEditPopUpMenu(JMenuItem menu) {
        if (!editAdditionalMenu.contains(menu)) {
            editAdditionalMenu.add(menu);
        }
    }

    public void addViewPopUpMenu(JMenuItem menu) {
        if (!viewAdditionalMenu.contains(menu)) {
            viewAdditionalMenu.add(menu);
        }
    }

    public void setAdditionalEditPopUpMenu(JPopupMenu popup) {
        if (editAdditionalMenu.isEmpty()) {
            return;
        }
        popup.addSeparator();
        for (JMenuItem mi : editAdditionalMenu) {
            popup.add(mi);
        }
    }

    public void setAdditionalViewPopUpMenu(JPopupMenu popup) {
        if (viewAdditionalMenu.isEmpty()) {
            return;
        }
        popup.addSeparator();
        for (JMenuItem mi : viewAdditionalMenu) {
            popup.add(mi);
        }
    }

    /**
     * draw this level crossing
     *
     * @param g2 the graphics port to draw to
     */
    protected void draw(Graphics2D g2) {
        if (isMainlineBD() && (!isMainlineAC())) {
            drawXingAC(g2);
            drawXingBD(g2);
        } else {
            drawXingBD(g2);
            drawXingAC(g2);
        }
    }   // drawHidden(Graphics2D g2)

    private void drawXingAC(Graphics2D g2) {
        // set color for an AC block
        setColorForTrackBlock(g2, getLayoutBlockAC());
        // set track width for AC block
        layoutEditor.setTrackStrokeWidth(g2, isMainlineAC());
        // drawHidden AC segment
        g2.draw(new Line2D.Double(getCoordsA(), getCoordsC()));
    }

    private void drawXingBD(Graphics2D g2) {
        // set color - check for an BD block
        setColorForTrackBlock(g2, getLayoutBlockBD());
        // set track width for BD block
        layoutEditor.setTrackStrokeWidth(g2, isMainlineBD());
        // drawHidden BD segment
        g2.draw(new Line2D.Double(getCoordsB(), getCoordsD()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawUnconnected(Graphics2D g2) {
        if (getConnectA() == null) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsA()));
        }

        if (getConnectB() == null) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsB()));
        }

        if (getConnectC() == null) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsC()));
        }

        if (getConnectD() == null) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsD()));
        }
    }

    protected void drawEditControls(Graphics2D g2) {
        g2.setColor(defaultTrackColor);
        g2.draw(layoutEditor.trackControlCircleAt(getCoordsCenter()));

        if (getConnectA() == null) {
            g2.setColor(Color.magenta);
        } else {
            g2.setColor(Color.blue);
        }
        g2.draw(layoutEditor.trackControlPointRectAt(getCoordsA()));

        if (getConnectB() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.trackControlPointRectAt(getCoordsB()));

        if (getConnectC() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.trackControlPointRectAt(getCoordsC()));

        if (getConnectD() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.trackControlPointRectAt(getCoordsD()));
    }

    protected void drawTurnoutControls(Graphics2D g2) {
        // LevelXings don't have turnout controls...
        // nothing to see here... move along...
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void reCheckBlockBoundary() {
        // nothing to see here... move along...
    }

    /*
     * {@inheritDoc}
     */
    @Override
    protected ArrayList<LayoutConnectivity> getLayoutConnectivity() {
        // nothing to see here... move along...
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> checkForFreeConnections() {
        List<Integer> result = new ArrayList<>();

        //check the A connection point
        if (getConnectA() == null) {
            result.add(Integer.valueOf(LEVEL_XING_A));
        }

        //check the B connection point
        if (getConnectB() == null) {
            result.add(Integer.valueOf(LEVEL_XING_B));
        }

        //check the C connection point
        if (getConnectC() == null) {
            result.add(Integer.valueOf(LEVEL_XING_C));
        }

        //check the D connection point
        if (getConnectD() == null) {
            result.add(Integer.valueOf(LEVEL_XING_D));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkForUnAssignedBlocks() {
        return ((getLayoutBlockAC() != null) && (getLayoutBlockBD() != null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkForNonContiguousBlocks(
            @Nonnull HashMap<String, List<Set<String>>> blockNamesToTrackNameSetsMap) {
        /*
         * For each (non-null) blocks of this track do:
         * #1) If it's got an entry in the blockNamesToTrackNameSetMap then
         * #2) If this track is already in the TrackNameSet for this block
         *     then return (done!)
         * #3) else add a new set (with this block/track) to
         *     blockNamesToTrackNameSetMap and check all the connections in this
         *     block (by calling the 2nd method below)
         * <p>
         *     Basically, we're maintaining contiguous track sets for each block found
         *     (in blockNamesToTrackNameSetMap)
         */

        // We're only using a map here because it's convient to
        // use it to pair up blocks and connections
        Map<LayoutTrack, String> blocksAndTracksMap = new HashMap<>();
        if ((getLayoutBlockAC() != null) && (connectA != null)) {
            blocksAndTracksMap.put(connectA, getLayoutBlockAC().getDisplayName());
        }
        if ((getLayoutBlockAC() != null) && (connectC != null)) {
            blocksAndTracksMap.put(connectC, getLayoutBlockAC().getDisplayName());
        }
        if ((getLayoutBlockBD() != null) && (connectB != null)) {
            blocksAndTracksMap.put(connectB, getLayoutBlockBD().getDisplayName());
        }
        if ((getLayoutBlockBD() != null) && (connectD != null)) {
            blocksAndTracksMap.put(connectD, getLayoutBlockBD().getDisplayName());
        }

        List<Set<String>> TrackNameSets = null;
        Set<String> TrackNameSet = null;
        for (Map.Entry<LayoutTrack, String> entry : blocksAndTracksMap.entrySet()) {
            LayoutTrack theConnect = entry.getKey();
            String theBlockName = entry.getValue();

            TrackNameSet = null;    // assume not found (pessimist!)
            TrackNameSets = blockNamesToTrackNameSetsMap.get(theBlockName);
            if (TrackNameSets != null) { // (#1)
                for (Set<String> checkTrackNameSet : TrackNameSets) {
                    if (checkTrackNameSet.contains(getName())) { // (#2)
                        TrackNameSet = checkTrackNameSet;
                        break;
                    }
                }
            } else {    // (#3)
                log.info("•New block ('{}') trackNameSets", theBlockName);
                TrackNameSets = new ArrayList<>();
                blockNamesToTrackNameSetsMap.put(theBlockName, TrackNameSets);
            }
            if (TrackNameSet == null) {
                TrackNameSet = new LinkedHashSet<>();
                TrackNameSets.add(TrackNameSet);
            }
            if (TrackNameSet.add(getName())) {
                log.info("•    Add track '{}' to trackNameSet for block '{}'", getName(), theBlockName);
            }
            theConnect.collectContiguousTracksNamesInBlockNamed(theBlockName, TrackNameSet);
        }
    }   // collectContiguousTracksNamesInBlockNamed

    /**
     * {@inheritDoc}
     */
    public void collectContiguousTracksNamesInBlockNamed(@Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet) {
        if (!TrackNameSet.contains(getName())) {
            // check all the matching blocks in this track and...
            //  #1) add us to TrackNameSet and...
            //  #2) flood them
            //check the AC blockName
            if ((blockNameAC != null) && (blockNameAC.equals(blockName))) {
                // if we are added to the TrackNameSet
                if (TrackNameSet.add(getName())) {
                    log.info("•    Add track '{}'for block '{}'", getName(), blockName);
                }
                // it's time to play... flood your neighbours!
                if (connectA != null) {
                    connectA.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                }
                if (connectC != null) {
                    connectC.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                }
            }
            //check the BD blockName
            if ((blockNameBD != null) && (blockNameBD.equals(blockName))) {
                // if we are added to the TrackNameSet
                if (TrackNameSet.add(getName())) {
                    log.info("•    Add track '{}'for block '{}'", getName(), blockName);
                }
                // it's time to play... flood your neighbours!
                if (connectB != null) {
                    connectB.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                }
                if (connectD != null) {
                    connectD.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LevelXing.class);
}
