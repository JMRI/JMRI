package jmri.jmrit.display;

import javax.swing.*;
import java.awt.event.*;
import jmri.jmrit.catalog.NamedIcon;
import jmri.*;
import jmri.jmrix.loconet.AspectGenerator;

/**
 * SignalHeadIcon provides a small icon to display a status of a SignalHead.
 * <P>In this implementation, it takes its information straight from an
 * AspectGenerator, so is tied very closely to that class.  This needs to
 * be fixed in the longer term.
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @version $Revision: 1.5 $
 */

public class SignalHeadIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public SignalHeadIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/searchlights/left-red-marker.gif",
                            "resources/icons/smallschematics/searchlights/left-red-marker.gif"));
        displayState(SignalHead.RED);
    }

    // what to display - at least one should be true!
    boolean showText = false;
    boolean showIcon = true;

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
    public void setRedIcon(NamedIcon i) { red = i; displayState(headState()); }

    public NamedIcon getYellowIcon() { return yellow; }
    public void setYellowIcon(NamedIcon i) { yellow = i; displayState(headState()); }

    public NamedIcon getFlashYellowIcon() { return flashYellow; }
    public void setFlashYellowIcon(NamedIcon i) { flashYellow = i; displayState(headState()); }

    public NamedIcon getGreenIcon() { return green; }
    public void setGreenIcon(NamedIcon i) { green = i; displayState(headState()); }

    public int getHeight() {
        return Math.max(
                        Math.max(red.getIconHeight(), yellow.getIconHeight()),
                        Math.max(flashYellow.getIconHeight(), green.getIconHeight())
                        );
    }

    public int getWidth() {
        return Math.max(
                        Math.max(red.getIconWidth(), yellow.getIconWidth()),
                        Math.max(flashYellow.getIconWidth(), green.getIconWidth())
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

    JPopupMenu popup = null;
    SignalHeadIcon ours = this;
    /**
     * Pop-up just displays the name
     */
    protected void showPopUp(MouseEvent e) {
        if (popup==null) {
            String name;
            name = "AG"+mGenerator.getSEName()+" head "+this.mHead;
            popup = new JPopupMenu();
            popup.add(new JMenuItem(name));
            if (showIcon) popup.add(new AbstractAction("Rotate") {
                    public void actionPerformed(ActionEvent e) {
                        green.setRotation(green.getRotation()+1, ours);
                        red.setRotation(red.getRotation()+1, ours);
                        yellow.setRotation(yellow.getRotation()+1, ours);
                        flashYellow.setRotation(flashYellow.getRotation()+1, ours);
                        displayState(headState());
                        ours.setSize(ours.getPreferredSize().width, ours.getPreferredSize().height);
                    }
                });
        }
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Drive the current state of the display from the state of the
     * turnout.
     */
    public void displayState(int state) {
        switch (state) {
        case SignalHead.RED:
            if (showText) super.setText("<red>");
            if (showIcon) super.setIcon(red);
            return;
        case SignalHead.YELLOW:
            if (showText) super.setText("<yellow>");
            if (showIcon) super.setIcon(yellow);
            return;
        case SignalHead.FLASHYELLOW:
            if (showText) super.setText("<flash yellow>");
            if (showIcon) super.setIcon(flashYellow);
            return;
        case SignalHead.GREEN:
            if (showText) super.setText("<green>");
            if (showIcon) super.setIcon(green);
            return;
        default:
            log.error("unexpected state during display: "+state);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SignalHeadIcon.class.getName());
}
