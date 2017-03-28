package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.Optional;
import javax.swing.JPopupMenu;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.CoordinateEdit;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableJComponent;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PositionableShape is item drawn by java.awt.Graphics2D.
 * <P>
 * @author Pete Cressman Copyright (c) 2012
 */
public class PositionableShape extends PositionableJComponent
        implements java.beans.PropertyChangeListener {

    private Shape _shape;
    protected Color _lineColor = Color.black;
    protected Color _fillColor = new Color(255, 255, 255, 0);
    protected int _lineWidth = 1;
    private int _degrees;
    protected AffineTransform _transform;
    private NamedBeanHandle<Sensor> _controlSensor = null;
    private int _saveLevel = 5;   // default level set in popup
    private int _changeLevel = 5;
    private boolean _doHide;  // whether sensor controls show/hide or change level
    // GUI resizing params
    private Rectangle[] _handles;
    protected int _hitIndex = -1; // dual use! also is index of polygon's vertices
    protected int _lastX;
    protected int _lastY;
    // params for shape's bounding box
    protected int _width;
    protected int _height;
    static final int TOP = 0;
    static final int RIGHT = 1;
    static final int BOTTOM = 2;
    static final int LEFT = 3;
    static final int SIZE = 4;

    public PositionableShape(Editor editor) {
        super(editor);
        setName("Graphic");
        setShowTooltip(false);
        setDisplayLevel(ControlPanelEditor.LABELS);
    }

    public PositionableShape(Editor editor, Shape shape) {
        this(editor);
        _shape = shape;
    }

    public PathIterator getPathIterator(AffineTransform at) {
        return _shape.getPathIterator(at);
    }

    protected void setShape(Shape s) {
        _shape = s;
    }

    protected Shape getShape() {
        return _shape;
    }

    public AffineTransform getTransform() {
        return _transform;
    }

    public void setWidth(int w) {
        if (w > SIZE) {
            _width = w;
        } else {
            _width = SIZE;
        }
    }

    public void setHeight(int h) {
        if (h > SIZE) {
            _height = h;
        } else {
            _height = SIZE;
        }
    }

    @Override
    public int getHeight() {
        return _height;
    }

    @Override
    public int getWidth() {
        return _width;
    }

    /**
     * this class must be overridden by its subclasses and executed only after
     * its parameters have been set
     */
    public void makeShape() {
    }

    public void setLineColor(Color c) {
        if (c == null) {
            c = Color.black;
        }
        _lineColor = c;
    }

    public Color getLineColor() {
        return _lineColor;
    }

    public void setFillColor(Color c) {
        if (c != null) {
            _fillColor = c;
        }
    }

    public Color getFillColor() {
        return _fillColor;
    }

    public void setLineWidth(int w) {
        _lineWidth = w;
    }

    public int getLineWidth() {
        return _lineWidth;
    }

    @Override
    public void rotate(int deg) {
        _degrees = deg % 360;
        if (_degrees == 0) {
            _transform = null;
        } else {
            double rad = _degrees * Math.PI / 180.0;
            _transform = new AffineTransform();
            // use bit shift to avoid FindBugs paranoia
            _transform.setToRotation(rad, (_width >>> 1), (_height >>> 1));
        }
        updateSize();
    }

    @Override
    public void paint(Graphics g) {
        if (!getEditor().isEditable() && !isVisible()) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g;
        /*
         g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
         RenderingHints.VALUE_RENDER_QUALITY); 
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
         RenderingHints.VALUE_ANTIALIAS_ON);
         g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, 
         RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
         RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         */
        g2d.setClip(null);
        if (_transform != null) {
            g2d.transform(_transform);
        }
        if (_fillColor != null) {
            g2d.setColor(_fillColor);
            g2d.fill(_shape);
        }
        if (_lineColor != null) {
            BasicStroke stroke = new BasicStroke(_lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f);
            g2d.setColor(_lineColor);
            g2d.setStroke(stroke);
            g2d.draw(_shape);
        }
        paintHandles(g2d);
    }

    protected void paintHandles(Graphics2D g2d) {
        if (_editor.isEditable() && _handles != null) {
            g2d.setColor(Editor.HIGHLIGHT_COLOR);
            g2d.setStroke(new java.awt.BasicStroke(2.0f));
            Rectangle r = getBounds();
            r.x = -_lineWidth / 2;
            r.y = -_lineWidth / 2;
            r.width += _lineWidth;
            r.height += _lineWidth;
            g2d.draw(r);
//         g2d.fill(r);
            for (int i = 0; i < _handles.length; i++) {
                if (_handles[i] != null) {
                    g2d.setColor(Color.RED);
                    g2d.fill(_handles[i]);
                    g2d.setColor(Editor.HIGHLIGHT_COLOR);
                    g2d.draw(_handles[i]);
                }
            }
        }
    }

    @Override
    public Positionable deepClone() {
        PositionableShape pos = new PositionableShape(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(PositionableShape pos) {
        pos._lineWidth = _lineWidth;
        pos._fillColor = new Color(_fillColor.getRed(), _fillColor.getGreen(), _fillColor.getBlue(), _fillColor.getAlpha());
        pos._lineColor = new Color(_lineColor.getRed(), _lineColor.getGreen(), _lineColor.getBlue(), _lineColor.getAlpha());
        pos.setControlSensor(getSensorName(), _doHide, _changeLevel);
        pos.setWidth(_width);
        pos.setHeight(_height);
        pos.makeShape();
        pos.rotate(getDegrees());       // must be after makeShape due to updateSize call
        return super.finishClone(pos);
    }
    
    @Override
    public Dimension getSize(Dimension rv) {
        return new Dimension(maxWidth(), maxHeight());
    }

    @Override
    public void updateSize() {
        Rectangle r;
        if (_shape != null) {
            r = _shape.getBounds();
        } else {
            r = super.getBounds();
        }
        setWidth(r.width);
        setHeight(r.height);
        setSize(r.width, r.height);
    }

    @Override
    public int maxWidth() {
        return getSize().width;
    }

    @Override
    public int maxHeight() {
        return getSize().height;
    }

    @Override
    public boolean showPopUp(JPopupMenu popup) {
        return false;
    }

    /**
     * return true if popup is set
     */
    @Override
    public boolean setRotateMenu(JPopupMenu popup) {
        if (super.getDisplayLevel() > Editor.BKG) {
            popup.add(CoordinateEdit.getRotateEditAction(this));
            return true;
        }
        return false;
    }

    @Override
    public boolean setScaleMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public int getDegrees() {
        return _degrees;
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (log.isDebugEnabled()) {
            log.debug("property change: \"{}\"= {} for {}", 
                   evt.getPropertyName(), evt.getNewValue(), getClass().getName());
        }
        if (!_editor.isEditable()) {
            if (evt.getPropertyName().equals("KnownState")) {
                if (((Integer) evt.getNewValue()).intValue() == Sensor.ACTIVE) {
                    if (_doHide) {
                        setVisible(true);
                    } else {
                        super.setDisplayLevel(_changeLevel);
                        setVisible(true);
                    }
                } else if (((Integer) evt.getNewValue()).intValue() == Sensor.INACTIVE) {
                    if (_doHide) {
                        setVisible(false);
                    } else {
                        super.setDisplayLevel(_saveLevel);
                        setVisible(true);
                    }
                } else {
                    super.setDisplayLevel(_saveLevel);
                    setVisible(true);
                }
                ((ControlPanelEditor)_editor).mouseMoved(new MouseEvent(this, 
                        MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 
                        0, getX(), getY(), 0, false));
                repaint();
                _editor.getTargetPanel().revalidate();
            }
        } else {
            super.setDisplayLevel(_saveLevel);
            setVisible(true);
        }
        if (log.isDebugEnabled()) {
            log.debug("_changeLevel= {} _saveLevel= {} displayLevel= {} _doHide= {} visible= {}", 
                    _changeLevel, _saveLevel, getDisplayLevel(), _doHide, isVisible()); 
        }
    }

    @Override
    // changing the level from regular popup
    public void setDisplayLevel(int l) {
        super.setDisplayLevel(l);
        _saveLevel = l;
    }

    /**
     * Attach a named sensor to shape
     *
     * @param pName Used as a system/user name to lookup the sensor object
     * @param hide true if sensor should be hidden
     * @param level level at which sensor is placed
     * @return error message, if any
     */
    public String setControlSensor(String pName, boolean hide, int level) {
        String msg = null;
        if (pName == null || pName.trim().isEmpty()) {
            setControlSensorHandle(null);
            return null;
        }
        NamedBeanHandle<Sensor> senHandle = null;
        _saveLevel = super.getDisplayLevel();
        Optional<SensorManager> sensorManager = InstanceManager.getOptionalDefault(SensorManager.class);
        if (sensorManager.isPresent()) {
            Sensor sensor = sensorManager.get().getSensor(pName);
            Optional<NamedBeanHandleManager> nbhm = InstanceManager.getOptionalDefault(NamedBeanHandleManager.class);
            if (sensor != null) {
                if (nbhm.isPresent()) {
                    senHandle = nbhm.get().getNamedBeanHandle(pName, sensor);
                }
                _doHide = hide;
                _changeLevel = level;
                if (_changeLevel <= 0) {
                    _changeLevel = super.getDisplayLevel();
                }
            } else {
                msg = Bundle.getMessage("badSensorName", pName); // NOI18N
            }
        } else {
            msg = Bundle.getMessage("NoSensorManager"); // NOI18N
        }
        if (msg != null) {
            log.warn("{} for {} sensor", msg, Bundle.getMessage("VisibleSensor"));
        }
        setControlSensorHandle(senHandle);
        return msg;
    }

    public void setControlSensorHandle(NamedBeanHandle<Sensor> senHandle) {
        if (_controlSensor != null) {
            getControlSensor().removePropertyChangeListener(this);
            setDisplayLevel(_saveLevel);
            setVisible(true);
        }
        _controlSensor = senHandle;
        if (_controlSensor != null) {
            getControlSensor().addPropertyChangeListener(this, _controlSensor.getName(), "PositionalShape");
        }
    }

    public Sensor getControlSensor() {
        if (_controlSensor == null) {
            return null;
        }
        return _controlSensor.getBean();
    }
    protected String getSensorName() {
        Sensor s = getControlSensor();
        if (s != null) {
            return s.getDisplayName();
        }
        return null;
    }

    public NamedBeanHandle<Sensor> getControlSensorHandle() {
        return _controlSensor;
    }

    public boolean isHideOnSensor() {
        return _doHide;
    }

    public int getChangeLevel() {
        return _changeLevel;
    }
    
    public void setChangeLevel(int l) {
        _changeLevel = l;
    }

    public void dispose() {
        if (_controlSensor != null) {
            getControlSensor().removePropertyChangeListener(this);
        }
        _controlSensor = null;
    }

    DrawFrame _editFrame;
    
    protected void setEditFrame(DrawFrame f) {
        _editFrame = f;
    }

    protected void setEditParams() {
        _editFrame.setDisplayParams(this);
        _editFrame.makeCopy(this);
        drawHandles();
    }

    protected void closeEditFrame() {
        _editFrame = null;
        removeHandles();
    }

    public void removeHandles() {
        _handles = null;
        repaint();
    }

    public void drawHandles() {
        _handles = new Rectangle[4];
        int rectSize = 2 * SIZE;
        if (_width < rectSize || _height < rectSize) {
            rectSize = Math.min(_width, _height);
        }
        _handles[RIGHT] = new Rectangle(_width - rectSize, _height / 2 - rectSize / 2, rectSize, rectSize);
        _handles[LEFT] = new Rectangle(0, _height / 2 - rectSize / 2, rectSize, rectSize);
        _handles[TOP] = new Rectangle(_width / 2 - rectSize / 2, 0, rectSize, rectSize);
        _handles[BOTTOM] = new Rectangle(_width / 2 - rectSize / 2, _height - rectSize, rectSize, rectSize);
    }

    public Point getInversePoint(int x, int y) throws java.awt.geom.NoninvertibleTransformException {
        if (_transform != null) {
            java.awt.geom.AffineTransform t = _transform.createInverse();
            float[] pt = new float[2];
            pt[0] = x;
            pt[1] = y;
            t.transform(pt, 0, pt, 0, 1);
            return new Point(Math.round(pt[0]), Math.round(pt[1]));
        }
        return new Point(x, y);
    }

    @Override
    public void doMousePressed(MouseEvent event) {
        _hitIndex = -1;
        if (!_editor.isEditable()) {
            return;
        }
        if (_handles != null) {
            _lastX = event.getX();
            _lastY = event.getY();
            int x = _lastX - getX();
            int y = _lastY - getY();
            Point pt;
            try {
                pt = getInversePoint(x, y);
            } catch (java.awt.geom.NoninvertibleTransformException nte) {
                log.error("Can't locate Hit Rectangles " + nte.getMessage());
                return;
            }
            for (int i = 0; i < _handles.length; i++) {
                if (_handles[i] != null && _handles[i].contains(pt.x, pt.y)) {
                    _hitIndex = i;
                }
            }
        }
    }

    protected boolean doHandleMove(MouseEvent event) {
        if (_hitIndex >= 0 && _editor.isEditable()) {
            int deltaX = event.getX() - _lastX;
            int deltaY = event.getY() - _lastY;
            int height = _height;
            int width = _width;
            switch (_hitIndex) {
                case TOP:
                    if (_height - deltaY > SIZE) {
                        height = _height - deltaY;
                        _editor.moveItem(this, 0, deltaY);
                    } else {
                        height = SIZE;
                    }
                    setHeight(height);
                    break;
                case RIGHT:
                    width = Math.max(SIZE, _width + deltaX);
                    setWidth(width);
                    break;
                case BOTTOM:
                    height = Math.max(SIZE, _height + deltaY);
                    setHeight(height);
                    break;
                case LEFT:
                    if (_width - deltaX > SIZE) {
                        width = Math.max(SIZE, _width - deltaX);
                        _editor.moveItem(this, deltaX, 0);
                    } else {
                        width = SIZE;
                    }
                    setWidth(width);
                    break;
            }
            if (_editFrame!=null) {
                _editFrame.setDisplayWidth(_width);
                _editFrame.setDisplayHeight(_height);
            }
            makeShape();
            updateSize();
            drawHandles();
            repaint();
            _lastX = event.getX();
            _lastY = event.getY();
            return true;
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableShape.class.getName());
}
