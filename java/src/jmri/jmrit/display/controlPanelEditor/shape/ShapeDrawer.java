package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;

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
            _drawFrame = new DrawRectangle("newShape", "Rectangle", null);
        }
    }

    private void newRoundRectangle() {
        if (makeNewShape()) {
            _drawFrame = new DrawRoundRect("newShape", "roundRect", null);
        }
    }

    private void newPolygon() {
        if (makeNewShape()) {
            _drawFrame = new DrawPolygon("newShape", "Polygon", null);
        }
    }

    private void newCircle() {
        if (makeNewShape()) {
            _drawFrame = new DrawCircle("newShape", "Circle", null);
        }
    }

    private void newEllipse() {
        if (makeNewShape()) {
            _drawFrame = new DrawEllipse("newShape", "Ellipse", null);
        }
    }
    
    private boolean makeNewShape() {
        if (_drawFrame != null) {
            if (_drawFrame._shape == null) {
                _drawFrame.dispose();
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
    
    protected void closeDrawFrame() {
        _drawFrame = null;
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
        if (_drawFrame instanceof DrawPolygon) {
            ((DrawPolygon) _drawFrame).anchorPoint(event.getX(), event.getY());
        }
        if (pos instanceof PositionableShape && _editor.isEditable()) {
            if (!pos.equals(_currentSelection)) {
                if (_currentSelection != null) {
                    _currentSelection.removeHandles();                        
                }
                if (_drawFrame != null && _drawFrame._shape == null) {
                    // creation of a shape is in progress.  Don't change _drawFrame
                    return false;
                }
                _currentSelection = (PositionableShape) pos;
                _drawFrame = _currentSelection._editFrame;
                _currentSelection.drawHandles();
            }
            return true;
        }
        if (_currentSelection != null) {
            _currentSelection.removeHandles();                        
            _currentSelection = null;
        }
        return false;
    }

    public boolean doMouseReleased(Positionable selection, MouseEvent event, Editor ed) {
        if (_drawFrame != null && _drawFrame._shape == null) {
            _drawFrame.makeFigure(event, ed);
        }
        return false;
    }

    public boolean doMouseClicked(MouseEvent event, Editor ed) {
        if (_drawFrame != null) {
            if (_drawFrame instanceof DrawPolygon && event.getClickCount() > 1) {
                ((DrawPolygon) _drawFrame).makeShape(event, ed);
            }
            return true;
        }
        return false;
    }

    public boolean doMouseDragged(MouseEvent event) {
        if (_drawFrame instanceof DrawPolygon && _currentSelection == null) {
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

}
