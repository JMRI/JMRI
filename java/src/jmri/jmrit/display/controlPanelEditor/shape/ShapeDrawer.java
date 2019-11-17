package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pete Cressman Copyright (c) 2012
 */
public class ShapeDrawer {

    private final Editor _editor;
    private DrawFrame _drawFrame;
    private PositionableShape _currentSelection;

    public ShapeDrawer(Editor ed) {
        _editor = ed;
    }

    public JMenu makeMenu() {
        JMenu drawMenu = new JMenu(Bundle.getMessage("drawShapes"));

        JMenuItem shapeItem = new JMenuItem(Bundle.getMessage("drawSth", Bundle.getMessage("Rectangle")));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener((ActionEvent event) -> {
            newRectangle();
        });
        shapeItem = new JMenuItem(Bundle.getMessage("drawSth", Bundle.getMessage("roundRect")));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener((ActionEvent event) -> {
            newRoundRectangle();
        });

        shapeItem = new JMenuItem(Bundle.getMessage("drawSth", Bundle.getMessage("Polygon")));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener((ActionEvent event) -> {
            newPolygon();
        });

        shapeItem = new JMenuItem(Bundle.getMessage("drawSth", Bundle.getMessage("Circle")));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener((ActionEvent event) -> {
            newCircle();
        });
        shapeItem = new JMenuItem(Bundle.getMessage("drawSth", Bundle.getMessage("Ellipse")));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener((ActionEvent event) -> {
            newEllipse();
        });

        return drawMenu;
    }

    private void newRectangle() {
        if (makeNewShape()) {
            _drawFrame = new DrawRectangle("newShape", "Rectangle", null, _editor, true);
        }
    }

    private void newRoundRectangle() {
        if (makeNewShape()) {
            _drawFrame = new DrawRoundRect("newShape", "roundRect", null, _editor, true);
        }
    }

    private void newPolygon() {
        if (makeNewShape()) {
            _drawFrame = new DrawPolygon("newShape", "Polygon", null, _editor, true);
        }
    }

    private void newCircle() {
        if (makeNewShape()) {
            _drawFrame = new DrawCircle("newShape", "Circle", null, _editor, true);
        }
    }

    private void newEllipse() {
        if (makeNewShape()) {
            _drawFrame = new DrawEllipse("newShape", "Ellipse", null, _editor, true);
        }
    }

    protected boolean makeNewShape() {
        if (_drawFrame != null) {
            int ans = JOptionPane.showConfirmDialog(_drawFrame,
                   Bundle.getMessage("cancelFrame", _drawFrame.getTitle()),
                   Bundle.getMessage("QuestionTitle"),
                   JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ans == JOptionPane.YES_OPTION) {
                _drawFrame.closingEvent(true);
                return true;
            } else {
                _drawFrame.toFront();
                _drawFrame.setVisible(true);
                return false;
            }
        } else {
            return true;
        }
    }

    protected boolean setDrawFrame(DrawFrame f) {
        if (f == null) {
            _drawFrame = null;
            if (_currentSelection != null) {
                _currentSelection.removeHandles();
                _currentSelection = null;
            }
            return true;
        }
        if (makeNewShape()) {
            _drawFrame = f;
            _currentSelection = _drawFrame._shape;
            _currentSelection.drawHandles();
            _editor.deselectSelectionGroup();
            return true;
        }
       return false;
    }

    public void paint(Graphics g) {
        if (_drawFrame instanceof DrawPolygon) {
            ((DrawPolygon) _drawFrame).drawShape(g);
        }
    }

    ///////////////////////////// Mouse /////////////////////////////
    /**
     * @param event the event to process
     * @param pos   the item to check
     * @return true if creating or editing; false otherwise
     */
    public boolean doMousePressed(MouseEvent event, Positionable pos) {
        log.debug("Mouse Pressed _drawFrame= {}, _currentSelection= {}",
               (_drawFrame==null ? "null" : _drawFrame.getTitle()),
               (_currentSelection == null ? "null" :_currentSelection.getClass().getName()));
        if (_drawFrame != null) {
            if (_drawFrame instanceof DrawPolygon && _drawFrame._shape != null) {
                ((DrawPolygon) _drawFrame).anchorPoint(event.getX(), event.getY());
            }
            return true;
        }
        if (pos instanceof PositionableShape && _editor.isEditable()) {
            if (_currentSelection != null) {
                _currentSelection.removeHandles();
            }
            _currentSelection = (PositionableShape) pos;
            _currentSelection.drawHandles();
            return true;
        }
        if (_currentSelection != null) {
            _currentSelection.removeHandles();
            _currentSelection = null;
        }
        return false;
    }

    /*
     * For all PositionableShapes except PositionablePolygon, this terminates the first
     * phase of creating the PositionableShape. The initial DrawFrame is discarded and
     * the shape is created, entered into the Editor'c content and the editing version
     * of the DrawFrame is made.
     * For a PositionablePolygon, the releases add a vertex to the shape. The actual
     * creation of the PositionablePolygon, and the above actions are done at doMouseClicked
     *
     */
    public boolean doMouseReleased(Positionable selection, MouseEvent event, Editor ed) {
        log.debug("Mouse Released _drawFrame= {}", (_drawFrame==null ? "null" : _drawFrame.getTitle()));
        if (_drawFrame != null && _drawFrame._shape == null && _drawFrame._create == true) {
            if (_drawFrame instanceof DrawPolygon) {
                ((DrawPolygon)_drawFrame).makeVertex(event, ed);
            } else {
                Rectangle r = ed.getSelectRect();
                PositionableShape shape;
                if (r != null) {
                    shape = _drawFrame.makeFigure(r, ed);
                } else {
                    return false;
                }
                shape.setWidth(r.width);
                shape.setHeight(r.height);
                shape.setLocation(r.x, r.y);
                shape.updateSize();
                _drawFrame.closingEvent(false);       // close opening create prompt frame
                _drawFrame = shape.makeEditFrame(true); // make finishing create frame;
                ed.putItem(shape);
                return true;
            }
        }
        return false;
    }

    public boolean doMouseClicked(MouseEvent event, Editor ed) {
        log.debug("Mouse Clicked _drawFrame= {}", (_drawFrame==null ? "null" : _drawFrame.getTitle()));
        if (_drawFrame != null && _drawFrame._create) {
            PositionableShape shape;
            if (_drawFrame instanceof DrawPolygon && event.getClickCount() > 1) {
                shape = _drawFrame.makeFigure(null, ed);
                if (shape != null) {
                    Point pt = ((DrawPolygon)_drawFrame).getStartPoint();
                    shape.setLocation(pt.x, pt.y);
                    shape.updateSize();
                    _drawFrame.closingEvent(false);       // close opening create prompt frame
                    _drawFrame = shape.makeEditFrame(true); // make finishing create frame;
                    ed.putItem(shape);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean doMouseDragged(MouseEvent event) {
        log.debug("Mouse Dragged _drawFrame= {}, _currentSelection= {}",
                (_drawFrame==null ? "null" : _drawFrame.getTitle()),
                (_currentSelection == null ? "null" :_currentSelection.getClass().getName()));
        if (_currentSelection == null && _drawFrame instanceof DrawPolygon) {
            ((DrawPolygon) _drawFrame).moveTo(event.getX(), event.getY());
            return true;  // no select rect
        } else if (_currentSelection != null) {
            return _currentSelection.doHandleMove(event);
        }
        return false;
    }

    /*
     * Make rubber band line
     */
    public boolean doMouseMoved(MouseEvent event) {
        if (_drawFrame instanceof DrawPolygon) {
            ((DrawPolygon) _drawFrame).moveTo(event.getX(), event.getY());
            return true;     // no dragging when editing
        }
        return false;
    }

    public void add(boolean up) {
        if (_drawFrame instanceof DrawPolygon) {
            ((DrawPolygon) _drawFrame).addVertex(up);
        }
    }

    public void delete() {
        if (_drawFrame instanceof DrawPolygon) {
            ((DrawPolygon) _drawFrame).deleteVertex();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ShapeDrawer.class);
}