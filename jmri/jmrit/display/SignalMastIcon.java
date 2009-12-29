// SignalMastIcon.java

package jmri.jmrit.display;

import jmri.*;

import java.awt.event.*;

import javax.swing.*;

import jmri.util.NamedBeanHandle;

/**
 * An icon to display a status of a SignalMast.
 * <P>
 * For now, this is done via text.
 *
 * @see jmri.SignalMastManager
 * @see jmri.InstanceManager
 * @author Bob Jacobsen Copyright (C) 2009
 * @version $Revision: 1.2 $
 */

public class SignalMastIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public SignalMastIcon() {
        // super ctor call to make sure this is an icon label
        super("<empty>");
        icon = false;
        text = true;
        
        setDisplayLevel(PanelEditor.SIGNALS);
    }
    
    public SignalMastIcon(LayoutEditor panel){
        // super ctor call to make sure this is an icon label
        super("<empty>");
        icon = false;
        text = true;

        setDisplayLevel(LayoutEditor.SIGNALS);
    }

    private SignalMast mMast;
    private NamedBeanHandle<SignalMast> namedMast;

    /**
     * Attached a signalmast element to this display item
     * @param sh Specific SignalMast handle
     */
    public void setSignalMast(NamedBeanHandle<SignalMast> sh) {
        if (mMast != null) {
            mMast.removePropertyChangeListener(this);
        }
        mMast = sh.getBean();
        if (mMast != null) {
            displayState(mastState());
            mMast.addPropertyChangeListener(this);
            setProperToolTip();
            namedMast = sh;
        }
    }
    
     /**
     * Taken from the layout editor
     * Attached a numbered element to this display item
     * @param pName Used as a system/user name to lookup the SignalMast object
     */
    public void setSignalMast(String pName) {
        mMast = InstanceManager.signalMastManagerInstance().provideSignalMast(pName);
        if (mMast == null) log.warn("did not find a SignalMast named "+pName);
        else {
            namedMast = new NamedBeanHandle<SignalMast>(pName, mMast);
            displayState(mastState());
            mMast.addPropertyChangeListener(this);
            setProperToolTip();
        }
    }

    public NamedBeanHandle<SignalMast> getSignalMast() {
        return namedMast;
    }


    /**
     * Get current appearance of the mast
     * @return An aspect from the SignalMast
     */
    public String mastState() {
        if (mMast==null) return "<empty>";
        else return mMast.getAspect();
    }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "+e.getPropertyName()
                                            +" current state: "+mastState());
        displayState(mastState());
        if (getLayoutPanel()!=null){
            //super.layoutPanel.resetAwaitingIconChange();
            getLayoutPanel().resetAwaitingIconChange();
            getLayoutPanel().redrawPanel();
        }
    }

    public void setProperToolTip() {
        setToolTipText(getNameString());
    }

    public String getNameString() {
        String name;
        if (mMast == null) name = rb.getString("NotConnected");
        else if (mMast.getUserName() == null)
            name = mMast.getSystemName();
        else
            name = mMast.getUserName()+" ("+mMast.getSystemName()+")";
        return name;
    }

    ButtonGroup clickButtonGroup = null;
    ButtonGroup litButtonGroup = null;

    /**
     * Pop-up just displays the name
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
        popup = new JPopupMenu();
        popup.add(new JMenuItem(getNameString()));

        checkLocationEditable(popup, getNameString());

        addFixedItem(popup);
        addDisableMenuEntry(popup);
        if (layoutPanel!=null){
            popup.add(new AbstractAction("Set x & y") {
                public void actionPerformed(ActionEvent e) {
                    String name = getText();
                    displayCoordinateEdit(name);
                }
            });
        }

        popup.add(new AbstractAction(rb.getString("Remove")) {
            public void actionPerformed(ActionEvent e) {
                remove();
                dispose();
            }
        });

        popup.add(new AbstractAction(rb.getString("EditLogic")) {
            public void actionPerformed(ActionEvent e) {
                jmri.jmrit.blockboss.BlockBossFrame f = new jmri.jmrit.blockboss.BlockBossFrame();
                String name;
                if (mMast.getUserName()==null || mMast.getUserName().equals(""))
                    name = mMast.getSystemName();
                else
                    name = mMast.getUserName();
                f.setTitle(java.text.MessageFormat.format(rb.getString("SignalLogic"), name));
                f.setSignal(name);
                f.setVisible(true);
            }
        });

        popup.add(setHiddenMenu());
        
        popup.show(e.getComponent(), e.getX(), e.getY());
    }
    
    /**
     * Drive the current state of the display from the state of the
     * underlying SignalMast object.
     */
    public void displayState(String state) {
        updateSize();
        if (mMast == null) {
            log.debug("Display state "+state+", disconnected");
        } else {
            log.debug("Display state "+state+" for "+mMast.getSystemName());
        }
        
        super.setText(state);
        
        return;
    }


    //private static boolean warned = false;

    public void dispose() {
        mMast.removePropertyChangeListener(this);
        
        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalMastIcon.class.getName());
}
