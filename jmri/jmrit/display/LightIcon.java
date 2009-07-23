package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
//import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * An icon to display a status of a light.<P>
 * <P>
 * A click on the icon will command a state change. Specifically, it
 * will set the state to the opposite (THROWN vs CLOSED) of
 * the current state.
 *<P>
 * The default icons are for a left-handed turnout, facing point
 * for east-bound traffic.
 * @author Bob Jacobsen  Copyright (c) 2002
 * @version $Revision: 1.3 $
 */

public class LightIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public LightIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif",
                            "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif"));
        setDisplayLevel(PanelEditor.LIGHTS);
        displayState(lightState());
        icon = true;
        text = false;
    }

    // the associated Light object
    Light light = null;

    /**
     * Attached a named light to this display item
     * @param pName Used as a system/user name to lookup the light object
     */
     public void setLight(String pName) {
         if (InstanceManager.lightManagerInstance()!=null) {
             light = InstanceManager.lightManagerInstance().
                 provideLight(pName);
             if (light != null) {
                 setLight(light);
             } else {
                 log.error("Light '"+pName+"' not available, icon won't see changes");
             }
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
            setProperToolTip();
        } 
    }

    public Light getLight() { return light; }

    // display icons
    String offLName = "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif";
    NamedIcon off = new NamedIcon(offLName, offLName);
    String onLName = "resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif";
    NamedIcon on = new NamedIcon(onLName, onLName);
    String inconsistentLName = "resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif";
    NamedIcon inconsistent = new NamedIcon(inconsistentLName, inconsistentLName);
    String unknownLName = "resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif";
    NamedIcon unknown = new NamedIcon(unknownLName, unknownLName);

    public NamedIcon getOffIcon() { return off; }
    public void setOffIcon(NamedIcon i) {
        off = i;
        displayState(lightState());
    }

    public NamedIcon getOnIcon() { return on; }
    public void setOnIcon(NamedIcon i) {
        on = i;
        displayState(lightState());
    }

    public NamedIcon getInconsistentIcon() { return inconsistent; }
    public void setInconsistentIcon(NamedIcon i) {
        inconsistent = i;
        displayState(lightState());
    }

    public NamedIcon getUnknownIcon() { return unknown; }
    public void setUnknownIcon(NamedIcon i) {
        unknown = i;
        displayState(lightState());
    }

    protected int maxHeight() {
        return Math.max(
                Math.max( (off!=null) ? off.getIconHeight() : 0,
                        (on!=null) ? on.getIconHeight() : 0),
                (inconsistent!=null) ? inconsistent.getIconHeight() : 0
            );
    }
    protected int maxWidth() {
        return Math.max(
                Math.max((off!=null) ? off.getIconWidth() : 0,
                        (on!=null) ? on.getIconWidth() : 0),
                (inconsistent!=null) ? inconsistent.getIconWidth() : 0
            );
    }

    /**
     * Get current state of attached light
     * @return A state variable from a Light, e.g. Turnout.CLOSED
     */
    int lightState() {
        if (light != null) return light.getState();
        // This doesn't seem right. (Light.UNKNOWN = Light.ON = 0X01)  
        //else return Light.UNKNOWN;
        else return Light.INCONSISTENT;
    }
    
    // update icon as state of light changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled())
			log.debug("property change: " + getNameString() + " " + e.getPropertyName() + " is now "
					+ e.getNewValue());

		if (e.getPropertyName().equals("State")) {
			int now = ((Integer) e.getNewValue()).intValue();
			displayState(now);
		}
	}

    public void setProperToolTip() {
        setToolTipText(getNameString());
    }

    public String getNameString() {
        String name;
        if (light == null) name = rb.getString("NotConnected");
        else if (light.getUserName()!=null)
            name = light.getUserName()+" ("+light.getSystemName()+")";
        else
            name = light.getSystemName();
        return name;
    }


    /**
     * Pop-up displays the light name, allows you to rotate the icons
     */
    protected void showPopUp(MouseEvent e) {
		if (!getEditable())
			return;
		ours = this;
// create popup each time called
		popup = new JPopupMenu();
		popup.add(new JMenuItem(getNameString()));
		if (icon)
			
            checkLocationEditable(popup, getNameString());
			
			popup.add(new AbstractAction(rb.getString("Rotate")) {
				public void actionPerformed(ActionEvent e) {
					off.setRotation(off.getRotation() + 1, ours);
					on.setRotation(on.getRotation() + 1, ours);
					inconsistent.setRotation(inconsistent.getRotation() + 1, ours);
					unknown.setRotation(unknown.getRotation() + 1, ours);
							
					displayState(lightState());
				}
			});

		addDisableMenuEntry(popup);

        popup.add(new AbstractAction(rb.getString("EditIcon")) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });
		popup.add(new AbstractAction(rb.getString("Remove")) {
			public void actionPerformed(ActionEvent e) {
				remove();
				dispose();
			}
		});

		// end creation of pop-up menu
		popup.show(e.getComponent(), e.getX(), e.getY());
	}

    /**
	 * Drive the current state of the display from the state of the light.
	 */
    void displayState(int state) {
        log.debug(getNameString() +" displayState "+state);
        updateSize();
        switch (state) {
        case Light.OFF:
            if (text) super.setText(InstanceManager.turnoutManagerInstance().getClosedText());
            if (icon) super.setIcon(off);
            break;
        case Light.ON:
            if (text) super.setText(InstanceManager.turnoutManagerInstance().getThrownText());
            if (icon) super.setIcon(on);
            break;
        default:
            if (text) super.setText(rb.getString("Inconsistent"));
            if (icon) super.setIcon(inconsistent);
            break;
        }

        return;
    }

    void edit() {
        if (_editorFrame != null) {
            _editorFrame.setLocationRelativeTo(null);
            _editorFrame.toFront();
            return;
        }
        _editor = new IconAdder();
        _editor.setIcon(3, "LightStateOff", off);
        _editor.setIcon(2, "LightStateOn", on);
        _editor.setIcon(0, "BeanStateInconsistent", inconsistent);
        _editor.setIcon(1, "BeanStateUnknown", unknown);
        makeAddIconFrame("EditLight", "addIconsToPanel", "SelectLight", _editor);
        _editor.makeIconPanel();
        _editor.setPickList(PickListModel.lightPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateLight();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _editor.addCatalog();
                    _editorFrame.pack();
                }
        };
        _editor.complete(addIconAction, changeIconAction, true);
        _editor.setSelection(light);
    }
    void updateLight() {
        setOffIcon(_editor.getIcon("LightStateOff"));
        setOnIcon(_editor.getIcon("LightStateOn"));
        setUnknownIcon(_editor.getIcon("BeanStateUnknown"));
        setInconsistentIcon(_editor.getIcon("BeanStateInconsistent"));
        setLight((Light)_editor.getTableSelection());
        _editorFrame.dispose();
        _editorFrame = null;
        _editor = null;
        invalidate();
    }

    /**
     * Change the light when the icon is clicked
     * @param e
     */
    // Was mouseClicked, changed to mouseRelease to workaround touch screen driver limitation
    public void mouseReleased(java.awt.event.MouseEvent e) {
        super.mouseReleased(e);
        if (!getControlling()) return;
        if (getForceControlOff()) return;
        if (e.isMetaDown() || e.isAltDown() ) return;
        if (light==null) {
            log.error("No light connection, can't process click");
            return;
        }
        if (light.getState()==jmri.Light.OFF)
            light.setState(jmri.Light.ON);
        else
            light.setState(jmri.Light.OFF);
    }

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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LightIcon.class.getName());
}
