package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * @author Pete Cressman Copyright: Copyright (c) 2012
 * @version $Revision: 1 $
 *
 */
public class ShapeDrawer {

    protected ControlPanelEditor _editor;
    private DrawFrame _drawFrame;
    private PositionableShape _currentSelection;

    public ShapeDrawer(ControlPanelEditor ed) {
        _editor = ed;
    }

    public JMenu makeMenu() {
        JMenu drawMenu = new JMenu(Bundle.getMessage("drawShapes"));

        JMenuItem shapeItem = new JMenuItem(Bundle.getMessage("drawRectangle"));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                newRectangle();
            }
        });
        shapeItem = new JMenuItem(Bundle.getMessage("drawRoundRectangle"));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                newRoundRectangle();
            }
        });

        shapeItem = new JMenuItem(Bundle.getMessage("drawPolygon"));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                newPolygon();
            }
        });

        shapeItem = new JMenuItem(Bundle.getMessage("drawCircle"));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                newCircle();
            }
        });
        shapeItem = new JMenuItem(Bundle.getMessage("drawEllipse"));
        drawMenu.add(shapeItem);
        shapeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                newEllipse();
            }
        });

        return drawMenu;
    }

    private void newRectangle() {
        if (_drawFrame == null) {
            _drawFrame = new DrawRectangle("newShape", "rectangle", this);
        } else {
            _drawFrame.toFront();
        }
    }

    private void newRoundRectangle() {
        if (_drawFrame == null) {
            _drawFrame = new DrawRoundRect("newShape", "roundRect", this);
        } else {
            _drawFrame.toFront();
        }
    }

    private void newPolygon() {
        if (_drawFrame == null) {
            _drawFrame = new DrawPolygon("newShape", "polygon", this);
        } else {
            _drawFrame.toFront();
        }
    }

    private void newCircle() {
        if (_drawFrame == null) {
            _drawFrame = new DrawCircle("newShape", "circle", this);
        } else {
            _drawFrame.toFront();
        }
    }

    private void newEllipse() {
        if (_drawFrame == null) {
            _drawFrame = new DrawEllipse("newShape", "ellipse", this);
        } else {
            _drawFrame.toFront();
        }
    }

    protected void setDrawFrame(DrawFrame f) {
        _drawFrame = f;
    }

    protected void closeDrawFrame(DrawFrame f) {
        _drawFrame = null;
    }

    protected ControlPanelEditor getEditor() {
        return _editor;
    }

    public void paint(Graphics g) {
        if (_drawFrame instanceof DrawPolygon) {
            ((DrawPolygon) _drawFrame).drawShape(g);
        }
    }

    /**
     * ************************** Mouse ************************
     */
    /**
     * *** return true if creating or editing **
     */
    public boolean doMousePressed(MouseEvent event, Positionable pos) {
        if (_drawFrame instanceof DrawPolygon) {
            DrawPolygon f = (DrawPolygon) _drawFrame;
            f.anchorPoint(event.getX(), event.getY());
        }
        if (pos instanceof PositionableShape && _editor.isEditable()) {
            if (!pos.equals(_currentSelection)) {
                if (_currentSelection != null) {
                    _currentSelection.removeHandles();
                }
                _currentSelection = (PositionableShape) pos;
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

    public boolean doMouseReleased(Positionable selection, MouseEvent event) {
        if (_drawFrame != null && !_drawFrame._editing) {
            if (_drawFrame.makeFigure(event)) {
                _drawFrame.closingEvent();
                _editor.resetEditor();
            }
            return true;
        }
        return false;
    }

    public boolean doMouseClicked(MouseEvent event) {
        if (_drawFrame != null) {
            return true;
        }
        return false;
    }

    public boolean doMouseDragged(MouseEvent event) {
        if (_drawFrame instanceof DrawPolygon && _currentSelection == null) {
            ((DrawPolygon) _drawFrame).moveTo(event.getX(), event.getY());
            return true;		// no select rect
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

    private final static Logger log = LoggerFactory.getLogger(ShapeDrawer.class.getName());
}
