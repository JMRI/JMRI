package jmri.jmrit.vsdecoder;

/*
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author   Mark Underwood Copyright (C) 2011
 */
import javax.swing.JButton;
import javax.swing.JComponent;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MomentarySoundEvent extends SoundEvent {

    JButton button;

    Trigger t; // used in setXml as a temporary holder for creating the
    // event listener class.
    ButtonTrigger bt; // used in setupButtonAction() as a temporary holder
    // for creating the button listeners.

    public MomentarySoundEvent() {
        this(null, null);
    }

    public MomentarySoundEvent(String n) {
        this(n, n);
    }

    public MomentarySoundEvent(String n, String bl) {
        super(n, bl);
        button = null;
    }

    @Override
    public boolean hasButton() {
        if ((buttontype == ButtonType.NONE) || (buttontype == ButtonType.ENGINE) || (button == null)) {
            return (false);
        } else {
            return (true);
        }
    }

    public void setButton(JButton b) {
        button = b;
    }

    @Override
    public JComponent getButton() {
        return (button);
    }

    @Override
    public void setButtonLabel(String bl) {
        button.setText(bl);
    }

    @Override
    public String getButtonLabel() {
        return (button.getText());
    }

    @Override
    protected ButtonTrigger setupButtonAction(Element te) {
        bt = new ButtonTrigger(te.getAttributeValue("name"));
        button_trigger_list.put(bt.getName(), bt);
        log.debug("new ButtonTrigger " + bt.getName() + " type " + buttontype.toString());
        button.addMouseListener(bt);
        return (bt);  // cast OK since we just instantiated it up above.
    }

    @Override
    public Element getXml() {
        Element me = new Element("SoundEvent");
        me.setAttribute("name", name);
        me.setAttribute("label", me.getText());
        for (Trigger t : trigger_list.values()) {
            me.addContent(t.getXml());
        }

        return (me);
    }

    @Override
    public void setXml(Element el) {
        this.setXml(el, null);
    }

    @Override
    public void setXml(Element el, VSDFile vf) {

        // Create the button first (put this in constructor?)
        button = new JButton();

        // Handle common stuff.
        super.setXml(el, vf);

        // We know it's momentary, or this class wouldn't have been constructed.
        button.setText(el.getAttributeValue("label"));

        /*
         for (ButtonTrigger bt : button_trigger_list.values()) {
         log.debug("Button Trigger: " + bt.getName());
         if (bt.getTarget() != null)
         log.debug("  Target: " + bt.getTarget().getName());
         else
         log.debug("  Target: null");
         if (bt.getTargetAction() != null)
         log.debug("  Action: " + bt.getTargetAction().toString());
         else
         log.debug("  Target Action: null");

         } 
         */

        /*
         MouseListener [] listeners = button.getListeners(MouseListener.class);
         for (MouseListener l : listeners) {
         log.debug("Listener: " + l.toString());
         }
         */
    }  // end setXml()

    private static final Logger log = LoggerFactory.getLogger(MomentarySoundEvent.class);

}
