package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.InstanceManager;
import jmri.Light;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display a status of a light.
 * <p>
 * A click on the icon will command a state change. Specifically, it will set
 * the state to the opposite (THROWN vs CLOSED) of the current state.
 * <p>
 * The default icons show a crossed lamp symbol.
 * @see Editor#addLightEditor()
 *
 * @author Bob Jacobsen Copyright (c) 2002
 */
public class LightIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public LightIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/lights/cross-on.png",
                "resources/icons/smallschematics/lights/cross-off.png"), editor);
        _control = true;
        displayState(lightState());
        setPopupUtility(null);
    }

    // the associated Light object
    Light light = null;

    @Override
    public Positionable deepClone() {
        LightIcon pos = new LightIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(LightIcon pos) {
        pos.setLight(getNameString());
        pos.setOffIcon(cloneIcon(getOffIcon(), pos));
        pos.setOnIcon(cloneIcon(getOnIcon(), pos));
        pos.setInconsistentIcon(cloneIcon(getInconsistentIcon(), pos));
        pos.setUnknownIcon(cloneIcon(getUnknownIcon(), pos));
        return super.finishClone(pos);
    }

    /**
     * Attached a named light to this display item
     *
     * @param pName Used as a system/user name to lookup the light object
     */
    public void setLight(String pName) {
        if (InstanceManager.getNullableDefault(jmri.LightManager.class) != null) {
            light = InstanceManager.lightManagerInstance().
                    provideLight(pName);
            setLight(light);
        } else {
            log.error("No LightManager for this protocol, icon won't see changes");
        }
    }

    public void setLight(Light to) {
        if (light != null) {
            light.removePropertyChangeListener(this);
        }
        light = to;
        if (light != null) {
            displayState(lightState());
            light.addPropertyChangeListener(this);
        }
    }

    public Light getLight() {
        return light;
    }

    // display icons
    String offLName = "resources/icons/smallschematics/lights/cross-on.png";
    NamedIcon off = new NamedIcon(offLName, offLName);
    String onLName = "resources/icons/smallschematics/lights/cross-off.png";
    NamedIcon on = new NamedIcon(onLName, onLName);
    String inconsistentLName = "resources/icons/smallschematics/lights/cross-inconsistent.png";
    NamedIcon inconsistent = new NamedIcon(inconsistentLName, inconsistentLName);
    String unknownLName = "resources/icons/smallschematics/lights/cross-unknown.png";
    NamedIcon unknown = new NamedIcon(unknownLName, unknownLName);

    public NamedIcon getOffIcon() {
        return off;
    }

    public void setOffIcon(NamedIcon i) {
        off = i;
        displayState(lightState());
    }

    public NamedIcon getOnIcon() {
        return on;
    }

    public void setOnIcon(NamedIcon i) {
        on = i;
        displayState(lightState());
    }

    public NamedIcon getInconsistentIcon() {
        return inconsistent;
    }

    public void setInconsistentIcon(NamedIcon i) {
        inconsistent = i;
        displayState(lightState());
    }

    public NamedIcon getUnknownIcon() {
        return unknown;
    }

    public void setUnknownIcon(NamedIcon i) {
        unknown = i;
        displayState(lightState());
    }

    @Override
    public int maxHeight() {
        return Math.max(
                Math.max((off != null) ? off.getIconHeight() : 0,
                        (on != null) ? on.getIconHeight() : 0),
                (inconsistent != null) ? inconsistent.getIconHeight() : 0
        );
    }

    @Override
    public int maxWidth() {
        return Math.max(
                Math.max((off != null) ? off.getIconWidth() : 0,
                        (on != null) ? on.getIconWidth() : 0),
                (inconsistent != null) ? inconsistent.getIconWidth() : 0
        );
    }

    /**
     * Get current state of attached light
     *
     * @return A state variable from a Light, e.g. Turnout.CLOSED
     */
    int lightState() {
        if (light != null) {
            return light.getState();
        } // This doesn't seem right. (Light.UNKNOWN = Light.ON = 0X01)  
        //else return Light.UNKNOWN;
        else {
            return Light.INCONSISTENT;
        }
    }

    // update icon as state of light changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("property change: " + getNameString() + " " + e.getPropertyName() + " is now "
                    + e.getNewValue());
        }

        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue());
            displayState(now);
        }
    }

    @Override
    public String getNameString() {
        String name;
        if (light == null) {
            name = Bundle.getMessage("NotConnected");
        } else if (light.getUserName() != null) {
            name = light.getUserName() + " (" + light.getSystemName() + ")";
        } else {
            name = light.getSystemName();
        }
        return name;
    }

    //
    // ****** popup AbstractAction.actionPerformed method overrides ********
    //
    @Override
    protected void rotateOrthogonal() {
        off.setRotation(on.getRotation() + 1, this);
        on.setRotation(off.getRotation() + 1, this);
        unknown.setRotation(unknown.getRotation() + 1, this);
        inconsistent.setRotation(inconsistent.getRotation() + 1, this);
        displayState(lightState());
        //bug fix, must repaint icons that have same width and height
        repaint();
    }

    @Override
    public void setScale(double s) {
        off.scale(s, this);
        on.scale(s, this);
        unknown.scale(s, this);
        inconsistent.scale(s, this);
        displayState(lightState());
    }

    @Override
    public void rotate(int deg) {
        off.rotate(deg, this);
        on.rotate(deg, this);
        unknown.rotate(deg, this);
        inconsistent.rotate(deg, this);
        displayState(lightState());
    }

    @Override
    protected void edit() {
        makeIconEditorFrame(this, "Light", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.lightPickModelInstance());
        _iconEditor.setIcon(3, "StateOff", off);
        _iconEditor.setIcon(2, "StateOn", on);
        _iconEditor.setIcon(0, "BeanStateInconsistent", inconsistent);
        _iconEditor.setIcon(1, "BeanStateUnknown", unknown);
        _iconEditor.makeIconPanel(false);

        ActionListener addIconAction = (ActionEvent a) -> {
            updateLight();
        };
        _iconEditor.complete(addIconAction, true, true, true);
        _iconEditor.setSelection(light);
    }

    void updateLight() {
        setOffIcon(_iconEditor.getIcon("StateOff"));
        setOnIcon(_iconEditor.getIcon("StateOn"));
        setUnknownIcon(_iconEditor.getIcon("BeanStateUnknown"));
        setInconsistentIcon(_iconEditor.getIcon("BeanStateInconsistent"));
        setLight((Light) _iconEditor.getTableSelection());
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    //
    // *********** end popup action methods ***************
    //
    /**
     * Drive the current state of the display from the state of the light.
     *
     * @param state the new state
     */
    void displayState(int state) {
        log.debug("{} displayState {}", getNameString(), state);
        updateSize();
        switch (state) {
            case Light.OFF:
                if (isText()) {
                    super.setText(InstanceManager.turnoutManagerInstance().getClosedText());
                }
                if (isIcon()) {
                    super.setIcon(off);
                }
                break;
            case Light.ON:
                if (isText()) {
                    super.setText(InstanceManager.turnoutManagerInstance().getThrownText());
                }
                if (isIcon()) {
                    super.setIcon(on);
                }
                break;
            default:
                if (isText()) {
                    super.setText(Bundle.getMessage("BeanStateInconsistent"));
                }
                if (isIcon()) {
                    super.setIcon(inconsistent);
                }
                break;
        }
    }

    /**
     * Change the light when the icon is clicked.
     *
     * @param e the mouse click
     */
    @Override
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) {
            return;
        }
        if (e.isMetaDown() || e.isAltDown()) {
            return;
        }
        if (light == null) {
            log.error("No light connection, can't process click");
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("doMouseClicked state= " + light.getState());
        }
        if (light.getState() == jmri.Light.OFF) {
            light.setState(jmri.Light.ON);
        } else {
            light.setState(jmri.Light.OFF);
        }
    }

    @Override
    public void dispose() {
        if (light != null) {
            light.removePropertyChangeListener(this);
        }
        light = null;

        off = null;
        on = null;
        inconsistent = null;
        unknown = null;

        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(LightIcon.class);
}
