package jmri.jmrit.display;

import java.awt.*;
import java.awt.event.*;

import javax.annotation.Nonnull;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.NamedBeanHandle;
import jmri.NamedBean.DisplayOptions;
import jmri.util.swing.*;

/**
 * An icon to display and input a Memory value in a TextField.
 * <p>
 * Handles the case of either a String or an Integer in the Memory, preserving
 * what it finds.
 *
 * @author Pete Cressman Copyright (c) 2009
 * @author Bob Jacobsen  Copyright (c) 2026
 * @since 2.7.2
 */
public class MemoryInputIcon extends PositionableJPanel implements java.beans.PropertyChangeListener {

    class PositionableJPanelJTextField extends JTextField {
        // Painting is already inside the scalled MemoryInputIcon, so 
        // doesn't need to be scaled again

        // rescale the pointer location of mouse events
        @Override
        public void processMouseEvent(MouseEvent e) {
            // translateMouseEvent is obtained from the enclosing class
            super.processMouseEvent(translateMouseEvent(e));
        }
        @Override
        public void processMouseMotionEvent(MouseEvent e) {
            // translateMouseEvent is obtained from the enclosing class
            super.processMouseMotionEvent(translateMouseEvent(e));
        }
        
        // rescale detection of the bounds of the field
        @Override
        public boolean contains(int x, int y) {
            double scale = getScale();
            int logicalX = (int) Math.round(x / scale);
            int logicalY = (int) Math.round(y / scale);
            
            boolean retval = logicalX >= 0 && logicalX < getWidth() && logicalY >= 0 && logicalY < getHeight();
            return retval;
        }
    }
    
    JTextField _textBox = new PositionableJPanelJTextField();

    // ==========
    // These are in the memory input icon itself - TODO: MOVE TO SUPER CLASS!
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        // Scale the rendering context
        g2d.scale(getScale(), getScale());
        super.paintComponent(g2d);
        g2d.dispose();
    }

    @Override
    public void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(translateMouseEvent(e));
    }
    @Override
    public void processMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(translateMouseEvent(e));
    }
    @Override
    public boolean contains(int x, int y) {
        double scale = getScale();
        int logicalX = (int) Math.round(x / scale);
        int logicalY = (int) Math.round(y / scale);
        
        boolean retval = logicalX >= 0 && logicalX < getWidth() && logicalY >= 0 && logicalY < getHeight();
        return retval;
    }
    MouseEvent translateMouseEvent(MouseEvent e) {
        double scale = getScale();
        int scaledX = (int) Math.round(e.getX() / scale);
        int scaledY = (int) Math.round(e.getY() / scale);
        return new MouseEvent(
            (Component)e.getSource(), 
            e.getID(),
            e.getWhen(),
            e.getModifiersEx(),
            scaledX, scaledY,
            e.getClickCount(),
            e.isPopupTrigger(),
            e.getButton()
        );
    }

    int originalX, originalY;
    
    @Override 
    public int getX() { return originalX; }
    @Override 
    public int getY() { return originalY; }
    @Override
    public void setLocation(int x, int y) {
        originalX = x;
        originalY = y;
        double scale = getScale();
        super.setLocation((int)Math.round(scale*x), (int)Math.round(scale*y));
    }
    @Override
    public void setLocation(Point p) {
        this.setLocation(p.x, p.y);
    }
    @Override
    public Point getLocation() {
        return new Point(originalX, originalY);
    }
    
    // =========
    
    // the associated Memory object
    private NamedBeanHandle<Memory> namedMemory;

    private final java.awt.event.MouseListener _mouseListener = JmriMouseListener.adapt(this);
    private final java.awt.event.MouseMotionListener _mouseMotionListener = JmriMouseMotionListener.adapt(this);

    int _nCols;

    public MemoryInputIcon(int nCols, Editor editor) {
        super(editor);
        _nCols = nCols;
        setDisplayLevel(Editor.LABELS);

        setLayout(new java.awt.GridBagLayout());
        add(_textBox, new java.awt.GridBagConstraints());
        _textBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                log.trace("Key Listener fired");
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_TAB) {
                    updateMemory();
                }
                // specical case for LayoutEditor -> Redraw the content
                getEditor().getTargetPanel().repaint();
            }
        });
        _textBox.setColumns(_nCols);
        _textBox.setBorder(null);  // drop default border so user can configure entire border appearance
        _textBox.addMouseMotionListener(_mouseMotionListener);
        _textBox.addMouseListener(_mouseListener);
        setPopupUtility(new PositionablePopupUtil(this, _textBox));
    }

    @Override
    public Positionable deepClone() {
        MemoryInputIcon pos = new MemoryInputIcon(_nCols, _editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(MemoryInputIcon pos) {
        pos.setMemory(namedMemory.getName());
        return super.finishClone(pos);
    }

    @Override
    public JComponent getTextComponent() {
        return _textBox;
    }

    @Override
    public void mouseExited(JmriMouseEvent e) {
        updateMemory();
        // TODO: understand e.consume();
        super.mouseExited(e);
    }

    @Override
    public void mouseMoved(JmriMouseEvent e) {
        // TODO: understand e.consume();
        updateMemory();
        super.mouseMoved(e);
    }

    /**
     * Attached a named Memory to this display item
     *
     * @param pName Used as a system/user name to lookup the Memory object
     */
    public void setMemory(String pName) {
        log.debug("setMemory for memory= {}", pName);
        if (InstanceManager.getNullableDefault(jmri.MemoryManager.class) != null) {
            try {
                Memory memory = InstanceManager.memoryManagerInstance().
                        provideMemory(pName);
                setMemory(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, memory));
            } catch (IllegalArgumentException e) {
                log.error("Memory '{}' not available, icon won't see changes", pName);
            }
        } else {
            log.error("No MemoryManager for this protocol, icon won't see changes");
        }
        updateSize();
    }

    /**
     * Attached a named Memory to this display item
     *
     * @param m The Memory object
     */
    public void setMemory(NamedBeanHandle<Memory> m) {
        if (namedMemory != null) {
            getMemory().removePropertyChangeListener(this);
        }
        namedMemory = m;
        if (namedMemory != null) {
            getMemory().addPropertyChangeListener(this, namedMemory.getName(), "Memory Input Icon");
            displayState();
            setName(namedMemory.getName());
        }
    }

    public void setNumColumns(int nCols) {
        _textBox.setColumns(nCols);
        _nCols = nCols;
    }

    public NamedBeanHandle<Memory> getNamedMemory() {
        return namedMemory;
    }

    public Memory getMemory() {
        if (namedMemory == null) {
            return null;
        }
        return namedMemory.getBean();
    }

    public int getNumColumns() {
        return _nCols;
    }

    // update icon as state of Memory changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("value")) {
            displayState();
        }
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_MemoryInputIcon");
    }

    @Override
    public String getNameString() {
        String name;
        if (namedMemory == null) {
            name = Bundle.getMessage("NotConnected");
        } else {
            name = getMemory().getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        }
        return name;
    }

    private void updateMemory() {
        if (namedMemory == null) {
            return;
        }
        String str = _textBox.getText();
        getMemory().setValue(str);
    }

    @Override
    public boolean setEditIconMenu(javax.swing.JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameMemory"));
        popup.add(new javax.swing.AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });
        return true;
    }

    /**
     * Popup menu iconEditor's ActionListener
     */
    SpinnerNumberModel _spinModel = new SpinnerNumberModel(3, 1, 100, 1);

    @Override
    protected void edit() {
        _iconEditor = new IconAdder("Memory") {
            final JSpinner spinner = new JSpinner(_spinModel);

            @Override
            protected void addAdditionalButtons(JPanel p) {
                ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(2);
                spinner.setMaximumSize(spinner.getPreferredSize());
                spinner.setValue(_textBox.getColumns());
                JPanel p2 = new JPanel();
                //p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
                //p2.setLayout(new FlowLayout(FlowLayout.TRAILING));
                p2.add(new JLabel(Bundle.getMessage("NumColsLabel")));
                p2.add(spinner);
                p.add(p2);
                p.setVisible(true);
            }
        };

        makeIconEditorFrame(this, "Memory", true, _iconEditor);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.memoryPickModelInstance());
        ActionListener addIconAction = a -> editMemory();
        _iconEditor.makeIconPanel(false);
        _iconEditor.complete(addIconAction, false, true, true);
        _iconEditor.setSelection(getMemory());
    }

    void editMemory() {
        setMemory(_iconEditor.getTableSelection().getDisplayName());
        _nCols = _spinModel.getNumber().intValue();
        _textBox.setColumns(_nCols);
        setSize(getPreferredSize().width + 1, getPreferredSize().height);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        validate();
    }

    /**
     * Drive the current state of the display from the state of the Memory.
     */
    public void displayState() {
        log.debug("displayState");
        if (namedMemory == null) {  // leave alone if not connected yet
            return;
        }
        Object show = getMemory().getValue();
        if (show != null) {
            _textBox.setText(show.toString());
        } else {
            _textBox.setText("");
        }
        // and redraw
        getEditor().getTargetPanel().repaint();
    }

    @Override
    void cleanup() {
        if (namedMemory != null) {
            getMemory().removePropertyChangeListener(this);
        }
        if (_textBox != null) {
            _textBox.removeMouseMotionListener(_mouseMotionListener);
            _textBox.removeMouseListener(_mouseListener);
        }
        namedMemory = null;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MemoryInputIcon.class);
}
