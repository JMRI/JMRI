package jmri.jmrit.display;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.logixng.GlobalVariable;
import jmri.jmrit.logixng.GlobalVariableManager;
import jmri.util.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of a GlobalVariable in a JSpinner.
 * <p>
 * Handles the case of either a String or an Integer in the GlobalVariable, preserving
 * what it finds.
 *
 * @author Bob Jacobsen     Copyright (c) 2009
 * @author Daniel Bergqvist Copyright (C) 2022
 * @since 2.7.2
 */
public class GlobalVariableSpinnerIcon extends PositionableJPanel implements ChangeListener, PropertyChangeListener {

    int _min = 0;
    int _max = 100;
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, _min, _max, 1));
    // the associated GlobalVariable object
    //GlobalVariable globalVariable = null;
    private NamedBeanHandle<GlobalVariable> namedGlobalVariable;

    private final java.awt.event.MouseListener _mouseListener = JmriMouseListener.adapt(this);
    private final java.awt.event.MouseMotionListener _mouseMotionListener = JmriMouseMotionListener.adapt(this);

    public GlobalVariableSpinnerIcon(Editor editor) {
        super(editor);
        setDisplayLevel(Editor.LABELS);

        setLayout(new java.awt.GridBagLayout());
        add(spinner, new java.awt.GridBagConstraints());
        spinner.addChangeListener(this);
        javax.swing.JTextField textBox = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        textBox.addMouseMotionListener(_mouseMotionListener);
        textBox.addMouseListener(_mouseListener);
        setPopupUtility(new PositionablePopupUtil(this, textBox));
    }

    @Override
    public Positionable deepClone() {
        GlobalVariableSpinnerIcon pos = new GlobalVariableSpinnerIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(GlobalVariableSpinnerIcon pos) {
        pos.setGlobalVariable(namedGlobalVariable.getName());
        return super.finishClone(pos);
    }

    @Override
    public javax.swing.JComponent getTextComponent() {
        return ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
    }

    @Override
    public Dimension getSize() {
        if (log.isDebugEnabled()) {
            Dimension d = spinner.getSize();
            log.debug("spinner width= {}, height= {}", d.width, d.height);
            java.awt.Rectangle rect = getBounds(null);
            log.debug("Bounds rect= ({},{}) width= {}, height= {}",
                    rect.x, rect.y, rect.width, rect.height);
            d = super.getSize();
            log.debug("Panel width= {}, height= {}", d.width, d.height);
        }
        return super.getSize();
    }

    /**
     * Attached a named GlobalVariable to this display item
     *
     * @param pName Used as a system/user name to lookup the GlobalVariable object
     */
    public void setGlobalVariable(String pName) {
        log.debug("setGlobalVariable for memory= {}", pName);
        if (InstanceManager.getNullableDefault(GlobalVariableManager.class) != null) {
            try {
                GlobalVariable globalVariable = InstanceManager.getDefault(GlobalVariableManager.class).getGlobalVariable(pName);
                setGlobalVariable(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, globalVariable));
            } catch (IllegalArgumentException e) {
                log.error("GlobalVariable '{}' not available, icon won't see changes", pName);
            }
        } else {
            log.error("No GlobalVariableManager for this protocol, icon won't see changes");
        }
        updateSize();
    }

    /**
     * Attached a named GlobalVariable to this display item
     *
     * @param m The GlobalVariable object
     */
    public void setGlobalVariable(NamedBeanHandle<GlobalVariable> m) {
        if (namedGlobalVariable != null) {
            getGlobalVariable().removePropertyChangeListener(this);
        }
        namedGlobalVariable = m;
        if (namedGlobalVariable != null) {
            getGlobalVariable().addPropertyChangeListener(this, namedGlobalVariable.getName(), "GlobalVariable Spinner Icon");
            displayState();
            setName(namedGlobalVariable.getName());
        }
    }

    public NamedBeanHandle<GlobalVariable> getNamedGlobalVariable() {
        return namedGlobalVariable;
    }

    // update icon as state of GlobalVariable changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("value")) {
            displayState();
        }
    }

    public GlobalVariable getGlobalVariable() {
        if (namedGlobalVariable == null) {
            return null;
        }
        return namedGlobalVariable.getBean();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        spinnerUpdated();
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_GlobalVariableSpinnerIcon");
    }

    @Override
    public String getNameString() {
        String name;
        if (namedGlobalVariable == null) {
            name = Bundle.getMessage("NotConnected");
        } else {
            name = getGlobalVariable().getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        }
        return name;
    }

    /*
     public void setSelectable(boolean b) {selectable = b;}
     public boolean isSelectable() { return selectable;}
     boolean selectable = false;
     */
    @Override
    public boolean setEditIconMenu(javax.swing.JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameGlobalVariable"));
        popup.add(new AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });
        return true;
    }

    @Override
    protected void edit() {
        makeIconEditorFrame(this, "GlobalVariable", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.globalVariablePickModelInstance());
        ActionListener addIconAction = a -> editGlobalVariable();
        _iconEditor.complete(addIconAction, false, false, true);
        _iconEditor.setSelection(getGlobalVariable());
    }

    void editGlobalVariable() {
        setGlobalVariable(_iconEditor.getTableSelection().getDisplayName());
        setSize(getPreferredSize().width, getPreferredSize().height);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    /**
     * Drive the current state of the display from the state of the GlobalVariable.
     */
    public void displayState() {
        log.debug("displayState");
        if (namedGlobalVariable == null) {  // leave alone if not connected yet
            return;
        }
        if (getGlobalVariable().getValue() == null) {
            return;
        }
        Integer num = null;
        if (getGlobalVariable().getValue().getClass() == String.class) {
            try {
                num = Integer.valueOf((String) getGlobalVariable().getValue());
            } catch (NumberFormatException e) {
                return;
            }
        } else if (getGlobalVariable().getValue().getClass() == Integer.class) {
            num = ((Number) getGlobalVariable().getValue()).intValue();
        } else if (getGlobalVariable().getValue().getClass() == Float.class) {
            num = Math.round((Float) getGlobalVariable().getValue());
            log.debug("num= {}", num);
        } else {
            //spinner.setValue(getGlobalVariable().getValue());
            return;
        }
        int n = num;
        if (n > _max) {
            num = _max;
        } else if (n < _min) {
            num = _min;
        }
        spinner.setValue(num);
    }

    @Override
    public void mouseExited(JmriMouseEvent e) {
        spinnerUpdated();
        super.mouseExited(e);
    }

    protected void spinnerUpdated() {
        if (namedGlobalVariable == null) {
            return;
        }
        if (getGlobalVariable().getValue() == null) {
            getGlobalVariable().setValue(spinner.getValue());
            return;
        }
        // Spinner is always an Integer, but memory can contain Integer or String
        if (getGlobalVariable().getValue().getClass() == String.class) {
            String newValue = "" + spinner.getValue();
            if (!getGlobalVariable().getValue().equals(newValue)) {
                getGlobalVariable().setValue(newValue);
            }
        } else {
            getGlobalVariable().setValue(spinner.getValue());
        }
    }

    public String getValue() {
        return "" + spinner.getValue();
    }

    @Override
    void cleanup() {
        if (namedGlobalVariable != null) {
            getGlobalVariable().removePropertyChangeListener(this);
        }
        if (spinner != null) {
            spinner.removeChangeListener(this);
            ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().removeMouseMotionListener(_mouseMotionListener);
            ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().removeMouseListener(_mouseListener);
        }
        namedGlobalVariable = null;
    }

    private final static Logger log = LoggerFactory.getLogger(GlobalVariableSpinnerIcon.class);
}
