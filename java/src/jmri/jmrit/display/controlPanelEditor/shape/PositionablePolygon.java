package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPopupMenu;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pete cresman Copyright (c) 2013
 */
public class PositionablePolygon extends PositionableShape {

    private ArrayList<Rectangle> _vertexHandles;
    private boolean _editing = false;   // during popUp or create, allows override of drawHandles etc.
//    protected boolean _isClosed;

    // there is no default PositionablePolygon
    private PositionablePolygon(Editor editor) {
        super(editor);
    }

    public PositionablePolygon(Editor editor, Shape shape) {
        super(editor, shape);
    }

    @Override
    public Positionable deepClone() {
        PositionablePolygon pos = new PositionablePolygon(_editor);
        return finishClone(pos);
    }

    @Override
    protected Positionable finishClone(PositionableShape pos) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        path.append(getPathIterator(null), false);
        /*
         PathIterator iter = _shape.getPathIterator(null);
         float[] coord = new float[6];
         while (!iter.isDone()) {
         int type = iter.currentSegment(coord);
         switch (type) {
         case PathIterator.SEG_MOVETO:
         path.moveTo(coord[0], coord[1]);
         break;
         case PathIterator.SEG_LINETO:
         path.lineTo(coord[0], coord[1]);
         break;
         case PathIterator.SEG_QUADTO:
         path.quadTo(coord[0], coord[1], coord[2], coord[3]);
         break;
         case PathIterator.SEG_CUBICTO:
         path.curveTo(coord[0], coord[1], coord[2], coord[3], coord[4], coord[53]);
         break;
         case PathIterator.SEG_CLOSE:
         path.closePath();
         break;
         }
         }
         */
        pos.setShape(path);
        return super.finishClone(pos);
    }

    protected void editing(boolean edit) {
        _editing = edit;
        log.debug("set _editing = {}", _editing);
    }

    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = Bundle.getMessage("editShape", Bundle.getMessage("Polygon"));
        popup.add(new javax.swing.AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                makeEditFrame(false);
            }
        });
        return true;
    }

    @Override
    protected DrawFrame makeEditFrame(boolean create) {
        _editFrame = new DrawPolygon("editShape", "Polygon", this, getEditor(), create);
        _editFrame.setDisplayParams(this);
        return _editFrame;
    }

    @Override
    public void removeHandles() {
        _vertexHandles = null;
        super.removeHandles();
    }

    @Override
    public void drawHandles() {
        if (_editing) {
            _vertexHandles = new ArrayList<>();
            PathIterator iter = getPathIterator(null);
            float[] coord = new float[6];
            while (!iter.isDone()) {
                iter.currentSegment(coord);
                int x = Math.round(coord[0]);
                int y = Math.round(coord[1]);
                _vertexHandles.add(new Rectangle(x - SIZE, y - SIZE, 2 * SIZE, 2 * SIZE));
                iter.next();
            }
        } else {
            super.drawHandles();
        }
    }

    @Override
    public void doMousePressed(MouseEvent event) {
        _hitIndex = -1;
        if (!_editor.isEditable()) {
            return;
        }
        if (_editing) {
            if (_vertexHandles != null) {
                _lastX = event.getX();
                _lastY = event.getY();
                int x = _lastX - getX();//-SIZE/2;
                int y = _lastY - getY();//-SIZE/2;
                Point pt;
                try {
                    pt = getInversePoint(x, y);
                } catch (java.awt.geom.NoninvertibleTransformException nte) {
                    log.error("Can't locate Hit Rectangles {}", nte.getMessage());
                    return;
                }
                for (int i = 0; i < _vertexHandles.size(); i++) {
                    if (_vertexHandles.get(i).contains(pt.x, pt.y)) {
                        _hitIndex = i;
                    }
                }
            }
            log.debug("doMousePressed _editing = {}, _hitIndex= {}", _editing, _hitIndex);
        } else {
            super.doMousePressed(event);
        }
    }

    @Override
    protected boolean doHandleMove(MouseEvent event) {
        if (_hitIndex >= 0 && _editor.isEditable()) {
            if (_editing) {
                Point pt = new Point(event.getX() - _lastX, event.getY() - _lastY);
                Rectangle rect = _vertexHandles.get(_hitIndex);
                rect.x += pt.x;
                rect.y += pt.y;
                DrawPolygon editFrame = (DrawPolygon) getEditFrame();
                if (editFrame != null) {
                    if (event.getX() - getX() < 0) {
                        _editor.moveItem(this, event.getX() - getX(), 0);
                    } else if (isLeftMost(rect.x)) {
                        _editor.moveItem(this, event.getX() - _lastX, 0);
                    }
                    if (event.getY() - getY() < 0) {
                        _editor.moveItem(this, 0, event.getY() - getY());
                    } else if (isTopMost(rect.y)) {
                        _editor.moveItem(this, 0, event.getY() - _lastY);
                    }

                    editFrame.doHandleMove(_hitIndex, pt);
                }
                _lastX = event.getX();
                _lastY = event.getY();
            } else {
                float deltaX = event.getX() - _lastX;
                float deltaY = event.getY() - _lastY;
                float width = _width;
                float height = _height;
                if (_height < SIZE || _width < SIZE) {
                    log.error("Bad size _width= {}, _height= {}", _width, _height);
                }
                GeneralPath path = null;
                switch (_hitIndex) {
                    case TOP:
                        if (height - deltaY > SIZE) {
                            path = scale(1, (height - deltaY) / height);
                            _editor.moveItem(this, 0, (int) deltaY);
                        } else {
                            path = scale(1, SIZE / height);
                            _editor.moveItem(this, 0, _height - SIZE);
                        }
                        break;
                    case RIGHT:
                        path = scale(Math.max(SIZE / width, (width + deltaX) / width), 1);
                        break;
                    case BOTTOM:
                        path = scale(1, Math.max(SIZE / height, (height + deltaY) / height));
                        break;
                    case LEFT:
                        if (_width - deltaX > SIZE) {
                            path = scale((width - deltaX) / width, 1);
                            _editor.moveItem(this, (int) deltaX, 0);
                        } else {
                            path = scale(SIZE / width, 1);
                            _editor.moveItem(this, _width - SIZE, 0);
                        }
                        break;
                    default:
                        log.warn("Unhandled direction code: {}", _hitIndex);
                }
                if (path != null) {
                    setShape(path);
                }
            }
            drawHandles();
            repaint();
            updateSize();
            _lastX = event.getX();
            _lastY = event.getY();
            log.debug("doHandleMove _editing = {}, _hitIndex= {}", _editing, _hitIndex);
            return true;
        }
        return false;
    }

    private boolean isLeftMost(int x) {
        Iterator<Rectangle> it = _vertexHandles.iterator();
        while (it.hasNext()) {
            if (it.next().x < x) {
                return false;
            }
        }
        return true;
    }

    private boolean isTopMost(int y) {
        Iterator<Rectangle> it = _vertexHandles.iterator();
        while (it.hasNext()) {
            if (it.next().y < y) {
                return false;
            }
        }
        return true;
    }

    private GeneralPath scale(float ratioX, float ratioY) {
//     log.info("scale("+ratioX+" , "+ratioY+")");
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        PathIterator iter = getPathIterator(null);
        float[] coord = new float[6];
        while (!iter.isDone()) {
            int type = iter.currentSegment(coord);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    path.moveTo(coord[0] * ratioX, coord[1] * ratioY);
                    break;
                case PathIterator.SEG_LINETO:
                    path.lineTo(coord[0] * ratioX, coord[1] * ratioY);
                    break;
                case PathIterator.SEG_QUADTO:
                    path.quadTo(coord[0], coord[1], coord[2], coord[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    path.curveTo(coord[0], coord[1], coord[2], coord[3], coord[4], coord[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    path.closePath();
                    break;
                default:
                    log.warn("Unhandled path iterator type: {}", type);
                    break;
            }
//      log.debug("type= "+type+"  x= "+coord[0]+", y= "+ coord[1]);
            iter.next();
        }
        return path;
    }

    @Override
    protected void paintHandles(Graphics2D g2d) {
        if (_editing) {
            if (_vertexHandles != null) {
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                Iterator<Rectangle> iter = _vertexHandles.iterator();
                while (iter.hasNext()) {
                    Rectangle rect = iter.next();
                    g2d.setColor(Color.BLUE);
                    g2d.fill(rect);
                    g2d.setColor(Editor.HIGHLIGHT_COLOR);
                    g2d.draw(rect);
                }
                if (_hitIndex >= 0) {
                    Rectangle rect = _vertexHandles.get(_hitIndex);
                    g2d.setColor(Color.RED);
                    g2d.fill(rect);
                    g2d.draw(rect);
                }
            }
        } else {
            super.paintHandles(g2d);
        }
    }

    @Override
    protected void invalidateShape() {
        // do nothing to prevent PositionableShape from invalidating this path
    }

    @Override
    protected Shape makeShape() {
        // return an empty shape so it can be appended to
        return new GeneralPath(GeneralPath.WIND_EVEN_ODD);
    }

    private final static Logger log = LoggerFactory.getLogger(PositionablePolygon.class);
}