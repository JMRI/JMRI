package jmri.jmrit.display;

import javax.swing.*;
import java.awt.event.*;
import jmri.jmrit.catalog.NamedIcon;
import jmri.*;
import jmri.jmrix.loconet.AspectGenerator;

/**
 * SignalHeadIcon provides a small icon to display a status of a SignalHead.
 * <P>
 * SignalHeads are located via the SignalHeadManager, which in turn is located
 * via the InstanceManager
 * @see jmri.SignalHeadManager
 * @see jmri.InstanceManager
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @version $Revision: 1.11 $
 */

public class SignalHeadIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public SignalHeadIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/searchlights/left-red-marker.gif",
                            "resources/icons/smallschematics/searchlights/left-red-marker.gif"));
        icon = true;
        text = false;

        displayState(SignalHead.RED);
    }

    // the associated AspectGenerator object
    AspectGenerator mGenerator;
    int mHead;

    /**
     * Attached a numbered element to this display item
     * @param name Used as a number to lookup the AspectGenerator object
     * @param number Number of the head on the generator
     */
    public void setSignalHead(String pName, int pNumber) {
        mGenerator = jmri.jmrix.loconet.LnSecurityElementManager.instance()
            .getAspectGenerator(pName);
        mHead = pNumber;
        mGenerator.addPropertyChangeListener(this);
    }

    public AspectGenerator getAspectGenerator() { return mGenerator; }

    public int getHeadNumber() { return mHead; }

    // display icons
    String redName = "resources/icons/smallschematics/searchlights/left-red-marker.gif";
    NamedIcon red = new NamedIcon(redName, redName);

    String yellowName = "resources/icons/smallschematics/searchlights/left-yellow-marker.gif";
    NamedIcon yellow = new NamedIcon(yellowName, yellowName);

    String flashYellowName = "resources/icons/smallschematics/searchlights/left-flashyellow-marker.gif";
    NamedIcon flashYellow = new NamedIcon(flashYellowName, flashYellowName);

    String greenName = "resources/icons/smallschematics/searchlights/left-green-marker.gif";
    NamedIcon green = new NamedIcon(greenName, greenName);

    public NamedIcon getRedIcon() { return red; }
    public void setRedIcon(NamedIcon i) {
        red = i;
        updateSize();
        displayState(headState());
    }

    public NamedIcon getYellowIcon() { return yellow; }
    public void setYellowIcon(NamedIcon i) {
        yellow = i;
        updateSize();
        displayState(headState());
    }

    public NamedIcon getFlashYellowIcon() { return flashYellow; }
    public void setFlashYellowIcon(NamedIcon i) {
        flashYellow = i;
        updateSize();
        displayState(headState());
    }

    public NamedIcon getGreenIcon() { return green; }
    public void setGreenIcon(NamedIcon i) {
        green = i;
        updateSize();
        displayState(headState());
    }

    protected int maxHeight() {
        return Math.max(
                Math.max( (red!=null) ? red.getIconHeight() : 0,
                        (green!=null) ? green.getIconHeight() : 0),
                Math.max((yellow!=null) ? yellow.getIconHeight() : 0,
                        (flashYellow!=null) ? flashYellow.getIconHeight() : 0)
            );
    }
    protected int maxWidth() {
        return Math.max(
                Math.max((red!=null) ? red.getIconWidth() : 0,
                        (green!=null) ? green.getIconWidth() : 0),
                Math.max((yellow!=null) ? yellow.getIconWidth() : 0,
                        (flashYellow!=null) ? flashYellow.getIconWidth() : 0)
            );
    }

    /**
     * Get current appearance of the head
     * @return An appearance variable from a SignalHead, e.g. SignalHead.RED
     */
    public int headState() {
        if (mGenerator==null) return 0;
        else return mGenerator.getHeadState(mHead);
    }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "+e.getPropertyName()
                                            +" current state: "+headState());
        displayState(headState());
    }

    public void setProperToolTip() {
        setToolTipText(getNameString());
    }

    String getNameString() {
        String name;
        if (mGenerator == null) name = "<Not connected>";
        else
            name = "AG"+mGenerator.getSEName()+" head "+this.mHead;
        return name;
    }

    /**
     * Pop-up just displays the name
     */
    protected void showPopUp(MouseEvent e) {
        ours = this;
        if (popup==null) {
            popup = new JPopupMenu();
            popup.add(new JMenuItem(getNameString()));
            if (icon) popup.add(new AbstractAction("Rotate") {
                    public void actionPerformed(ActionEvent e) {
                        green.setRotation(green.getRotation()+1, ours);
                        red.setRotation(red.getRotation()+1, ours);
                        yellow.setRotation(yellow.getRotation()+1, ours);
                        flashYellow.setRotation(flashYellow.getRotation()+1, ours);
                        displayState(headState());
                    }
                });

            popup.add(new AbstractAction("Remove") {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });

        } // end creation of pop-up menu

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Drive the current state of the display from the state of the
     * turnout.
     */
    public void displayState(int state) {
        switch (state) {
        case SignalHead.RED:
            if (text) super.setText("<red>");
            if (icon) super.setIcon(red);
            break;
        case SignalHead.YELLOW:
            if (text) super.setText("<yellow>");
            if (icon) super.setIcon(yellow);
            break;
        case SignalHead.FLASHYELLOW:
            if (text) super.setText("<flash yellow>");
            if (icon) super.setIcon(flashYellow);
            break;
        case SignalHead.GREEN:
            if (text) super.setText("<green>");
            if (icon) super.setIcon(green);
            break;
        default:
            log.error("unexpected state during display: "+state);
        }

        return;
    }

    public void dispose() {
        mGenerator.removePropertyChangeListener(this);
        mGenerator = null;

        red = null;
        yellow = null;
        flashYellow = null;
        green = null;

        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SignalHeadIcon.class.getName());
}
