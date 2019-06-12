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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundEvent implements PropertyChangeListener {

    public enum ButtonType {

        MOMENTARY, TOGGLE, ENGINE, NONE
    }

    String name;
    String button_label;
    String event_name;
    ButtonType buttontype;

    AbstractButton button;
    EnginePane engine_pane;

    Trigger t; // used in setXml as a temporary holder for creating the
    // event listener class.
    ButtonTrigger bt; // used in setupButtonAction() as a temporary holder
    // for creating the button listeners.
    VSDecoder parent;

    protected HashMap<String, ButtonTrigger> button_trigger_list;

    protected HashMap<String, Trigger> trigger_list;
    VSDSound my_sound;

    public SoundEvent() {
        this(null, null);
    }

    public SoundEvent(String n) {
        this(n, n);
    }

    public SoundEvent(String n, String bl) {
        name = n;
        button_label = bl;
        trigger_list = new HashMap<String, Trigger>();
        button_trigger_list = new HashMap<String, ButtonTrigger>();
        button = null;
    }

    public void setName(String n) {
        name = n;
    }

    public String getName() {
        return (name);
    }

    public void setEventName(String n) {
        event_name = n;
    }

    public String getEventName() {
        return (event_name);
    }

    public ButtonType getButtonType() {
        return (buttontype);
    }

    public boolean hasButton() {
        if ((buttontype == ButtonType.NONE) || (buttontype == ButtonType.ENGINE) || (button == null)) {
            return (false);
        } else {
            return (true);
        }
    }

    public boolean hasEnginePane() {
        if ((buttontype == ButtonType.ENGINE) && (engine_pane != null)) {
            return (true);
        } else {
            return (false);
        }
    }

    public void setButton(AbstractButton b) {
        button = b;
    }

    public JComponent getButton() {
        if ((buttontype == ButtonType.NONE) || (buttontype == ButtonType.ENGINE)) {
            return (null);
        } else {
            return (button);
        }
    }

    public EnginePane getEnginePane() {
        if (buttontype == ButtonType.ENGINE) {
            return (engine_pane);
        } else {
            return (null);
        }
    }

    public void setEnginePane(EnginePane e) {
        engine_pane = e;
    }

    public void setButtonLabel(String bl) {
        button.setText(bl);
    }

    public String getButtonLabel() {
        return (button.getText());
    }

    public void addTrigger(String s, Trigger t) {
        trigger_list.put(s, t);
    }

    public Trigger getTrigger(String s) {
        return trigger_list.get(s);
    }

    public void setSound(VSDSound v) {
        my_sound = v;
    }

    public VSDSound getSound() {
        return (my_sound);
    }

    public void setParent(VSDecoder v) {
        parent = v;
    }

    public VSDecoder getParent() {
        return parent;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        for (Trigger t : trigger_list.values()) {
            t.propertyChange(event);
        }
    }

    // What's wrong here:
    // the anonymous MouseListeners are storing a reference to BT, which keeps getting replaced
    // each time the function is called.
    // what we need to do is (maybe) make the ButtonTrigger itself a MouseListener (and ActionListener)
    // 
    protected ButtonTrigger setupButtonAction(Element te) {
        /*
         MouseListener ml;
         bt = new ButtonTrigger(te.getAttributeValue("name"));
         button_trigger_list.put(bt.getName(), bt);
         log.debug("new ButtonTrigger " + bt.getName() + " type " + btype.toString());
         switch(btype) {
         case TOGGLE:
         this.getButton().addActionListener(bt);
         break;
         case MOMENTARY:
         default:
         this.getButton().addMouseListener(bt);
         // Just send the trigger a click.
         }
         return(bt);  // cast OK since we just instantiated it up above.
         */
        return (null);  // cast OK since we just instantiated it up above.
    }

    public Element getXml() {
        Element me = new Element("SoundEvent");
        me.setAttribute("name", name);
        me.setAttribute("label", me.getText());
        for (Trigger t : trigger_list.values()) {
            me.addContent(t.getXml());
        }

        return (me);
    }

    public void setXml(Element el) {
        this.setXml(el, null);
    }

    protected void addXmlTrigger(Element te, VSDFile vf) {
        String tts;
        Trigger.TriggerType tt;
        if ((tts = te.getAttributeValue("type")) != null) {
            tt = Trigger.TriggerType.valueOf(tts.toUpperCase());
        } else {
            tt = Trigger.TriggerType.NONE;
        }

        switch (tt) {
            case BUTTON:
                if (this.buttontype != SoundEvent.ButtonType.NONE) {
                    t = setupButtonAction(te);
                }
                break;
            case BOOLEAN:
                t = new BoolTrigger(te.getAttributeValue("name"));
                break;
            case FLOAT:
                t = new FloatTrigger(te.getAttributeValue("name"), 0.0f, Trigger.CompareType.EQ);
                break;
            case NOTCH:
                t = new NotchTrigger(te.getAttributeValue("name"));
                break;
            case INT:
                t = new IntTrigger(te.getAttributeValue("name"));
                break;
            case STRING:
                //t = new StringTrigger(el.getAttributeValue("name"));
                log.warn("Don't have StringTriggers yet...");
                t = null;
                return;
            case THROTTLE:
                t = new ThrottleTrigger(te.getAttributeValue("name"));
                break;
            case NONE:
            default:
                break;
        }

        log.debug("Building trigger " + t.getName());
        t.setXml(te);
        trigger_list.put(te.getAttributeValue("name"), t);
        //log.debug("target name " + t.getTargetName() + " sound " + parent.getSound(t.getTargetName()));
        t.setTarget(parent.getSound(t.getTargetName()));
        //log.debug("target " + t.getTarget());

        if (t.getTarget() == null) {
            // If the target is missing, set up a do-nothing operation.
            // Protects against errors in the XML file.
            // Should probably post a warning, though.
            t.setTargetAction(Trigger.TargetAction.NOTHING);
        }
        switch (t.getTargetAction()) {
            case PLAY:
            case FADEIN:
                //log.debug("PLAY");
                t.setCallback(new TriggerListener() {
                    @Override
                    public void takeAction() {
                        t.getTarget().play();
                    }

                    @Override
                    public void takeAction(int i) {
                    }

                    @Override
                    public void takeAction(float f) {
                    } // do nothing
                });
                break;
            case LOOP:
                //log.debug("LOOP");
                t.setCallback(new TriggerListener() {
                    @Override
                    public void takeAction() {
                        t.getTarget().loop();
                    }

                    @Override
                    public void takeAction(int i) {
                    }

                    @Override
                    public void takeAction(float f) {
                    } // do nothing
                });
                break;
            case STOP:
            case FADEOUT:
                //log.debug("STOP");
                t.setCallback(new TriggerListener() {
                    @Override
                    public void takeAction() {
                        t.getTarget().stop();
                    }

                    @Override
                    public void takeAction(int i) {
                    }

                    @Override
                    public void takeAction(float f) {
                    } // do nothing
                });
                break;
            case NOTCH:
                //log.debug("NOTCH");
                log.debug("making callback t " + t + " target " + t.getTarget());
                t.setCallback(new TriggerListener() {
                    @Override
                    public void takeAction(int i) {
                        //log.debug("Notch Trigger Listener. t = " + t + " Target = " + t.getTarget() + " notch = " + i);
                        t.getTarget().changeNotch(i);
                    }

                    @Override
                    public void takeAction() {
                    }

                    @Override
                    public void takeAction(float f) {
                    } // do nothing
                });
                break;
            case CHANGE:
                //log.debug("CHANGE");
                log.debug("making callback t " + t + " target " + t.getTarget());
                t.setCallback(new TriggerListener() {
                    @Override
                    public void takeAction() {
                    } // do nothing

                    @Override
                    public void takeAction(int i) {
                    } // do nothing

                    @Override
                    public void takeAction(float f) {
                        //log.debug("Throttle Trigger Listener. t = " + t + " Target = " + t.getTarget() + " value = " + f);
                        t.getTarget().changeThrottle(f);
                    }
                });
                break;
            case NOTHING:
                // Used for when the target sound is missing.
                //log.debug("NOTHING");
                t.setCallback(new TriggerListener() {
                    @Override
                    public void takeAction() {
                    } // do nothing

                    @Override
                    public void takeAction(int i) {
                    } // do nothing

                    @Override
                    public void takeAction(float f) {
                    } // do nothing
                });
                break;
            default:
                // do nothing.
                break;
        } // end switch
    } // end function

    public void setXml(Element el, VSDFile vf) {
        Element te;
        String btv;

        // Get the SoundEvent's name.
        name = el.getAttributeValue("name");
        if ((btv = el.getAttributeValue("buttontype")) != null) {
            buttontype = SoundEvent.ButtonType.valueOf(btv.toUpperCase());
        } else {
            buttontype = SoundEvent.ButtonType.NONE;
        }

        // Get the SoundEvent's Triggers and set them up.
        Iterator<Element> itr = (el.getChildren("trigger")).iterator();
        while (itr.hasNext()) {
            te = itr.next();
            this.addXmlTrigger(te, vf);
        } // end while

    }  // end setXml()

    private static final Logger log = LoggerFactory.getLogger(SoundEvent.class);

}
