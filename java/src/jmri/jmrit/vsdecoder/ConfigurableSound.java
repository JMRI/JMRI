package jmri.jmrit.vsdecoder;

/*
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author   Mark Underwood Copyright (C) 2011
 * 
 */
// JMRI and Java stuff
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.util.PhysicalLocation;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Usage:
// HornSound() : constructor
// play() : plays short horn pop
// loop() : starts extended sustain horn
// stop() : ends extended sustain horn (plays end sound)
class ConfigurableSound extends VSDSound {

    protected String start_file;
    protected String mid_file;
    protected String end_file;
    protected String short_file;

    SoundBite start_sound;
    SoundBite mid_sound;
    SoundBite end_sound;
    SoundBite short_sound;

    boolean initialized = false;

    protected boolean use_start_sound = false;
    protected boolean use_mid_sound = false;
    protected boolean use_end_sound = false;
    protected boolean use_short_sound = false;

    int start_sound_duration = 136;

    public ConfigurableSound(String name) {
        super(name);
        is_playing = false;
    }

    public boolean init() {
        return (this.init(null));
    }

    public boolean init(VSDFile vf) {

        if (!initialized) {
            if (use_start_sound) {
                start_sound = new SoundBite(vf, start_file, name + "_Start", name + "_Start");
                start_sound.setLooped(false);
                start_sound.setGain(gain);
            }
            if (use_mid_sound) {
                mid_sound = new SoundBite(vf, mid_file, name + "_Mid", name + "_Mid");
                mid_sound.setLooped(false);
                mid_sound.setGain(gain);
            }
            if (use_end_sound) {
                end_sound = new SoundBite(vf, end_file, name + "_End", name + "_End");
                end_sound.setLooped(false);
                end_sound.setGain(gain);
            }
            if (use_short_sound) {
                short_sound = new SoundBite(vf, short_file, name + "_Short", name + "_Short");
                short_sound.setLooped(false);
                short_sound.setGain(gain);
            }

        }
        return (true);
    }

    @Override
    public boolean isPlaying() {
        return (is_playing);
    }

    @Override
    public void play() {
        if (use_short_sound) {
            short_sound.play();
            is_playing = false; // short sound, won't be playing long...
        } else {
            if (use_start_sound) {
                t = newTimer(start_sound.getLengthAsInt(), false,
                        new ActionListener() {
                    @Override
                            public void actionPerformed(ActionEvent e) {
                                handleTimerPop(e);
                            }
                        });
                start_sound.play();
                if (use_mid_sound) {
                    t.start();
                    is_playing = true;
                }
            } else if (use_mid_sound) {
                mid_sound.setLooped(true);
                mid_sound.play();
            }
        }
    }

    @Override
    public void loop() {
        if (use_start_sound) {
            start_sound.setLooped(false);
            start_sound.play();
            t = newTimer(start_sound.getLengthAsInt() - 100, false,
                    new ActionListener() {
                @Override
                        public void actionPerformed(ActionEvent e) {
                            handleTimerPop(e);
                        }
                    });
            t.setRepeats(false); // timer pop only once to trigger the sustain sound.
            t.start();
        } else if (use_mid_sound) {
            mid_sound.setLooped(true);
            mid_sound.play();
        }
        is_playing = true;
    }

    // Catch the timer pop after the start sound is played and trigger the (looped) sustain sound.
    protected void handleTimerPop(ActionEvent e) {
        log.debug("Received timer pop after start sound played.");
        //TODO: Need to validate that this is the timer pop
        if (use_mid_sound) {
            mid_sound.setLooped(true);
            mid_sound.play();
        }
        t.stop();
    }

    @Override
    public void stop() {
        log.debug("Stopping");
        // make sure the start sound is killed
        if (use_start_sound) {
            start_sound.stop();
        }

        // If the mid sound is used, turn off the looping.
        // this will allow it to naturally die.
        if (use_mid_sound) {
            mid_sound.setLooped(false);
            mid_sound.fadeOut();
        }

        // If the timer is running, stop it.
        if (t != null) {
            t.stop();
        }

        // If we're using the end sound, stop the mid sound
        // and play the end sound.
        if (use_end_sound) {
            if (use_mid_sound) {
                mid_sound.stop();
            }
            end_sound.setLooped(false);
            end_sound.play();
        }
        is_playing = false;
    }

    @Override
    public void fadeIn() {
        this.play();
    }

    @Override
    public void fadeOut() {
        this.stop();
    }

    @Override
    public void shutdown() {
        if (use_start_sound) {
            start_sound.stop();
        }
        if (use_mid_sound) {
            mid_sound.stop();
        }
        if (use_end_sound) {
            end_sound.stop();
        }
        if (use_short_sound) {
            short_sound.stop();
        }
    }

    @Override
    public void mute(boolean m) {
        if (use_start_sound) {
            start_sound.mute(m);
        }
        if (use_mid_sound) {
            mid_sound.mute(m);
        }
        if (use_end_sound) {
            end_sound.mute(m);
        }
        if (use_short_sound) {
            short_sound.mute(m);
        }
    }

    @Override
    public void setVolume(float v) {
        if (use_start_sound) {
            start_sound.setVolume(v);
        }
        if (use_mid_sound) {
            mid_sound.setVolume(v);
        }
        if (use_end_sound) {
            end_sound.setVolume(v);
        }
        if (use_short_sound) {
            short_sound.setVolume(v);
        }
    }

    @Override
    public void setPosition(PhysicalLocation p) {
        super.setPosition(p);
        if (use_start_sound) {
            start_sound.setPosition(p);
        }
        if (use_mid_sound) {
            mid_sound.setPosition(p);
        }
        if (use_end_sound) {
            end_sound.setPosition(p);
        }
        if (use_short_sound) {
            short_sound.setPosition(p);
        }
    }

    @Override
    public Element getXml() {
        Element me = new Element("sound");
        Integer i;

        log.debug("Configurable Sound:");
        log.debug("  name = " + this.getName());
        log.debug("  start_file = " + start_file);
        log.debug("  mid_file = " + mid_file);
        log.debug("  end_file = " + end_file);
        log.debug("  short_file = " + short_file);
        log.debug("  use_start_file = " + start_file);

        me.setAttribute("name", this.getName());
        me.setAttribute("type", "configurable");
        if (use_start_sound) {
            me.addContent(new Element("start-file").addContent(start_file));
        }
        if (use_mid_sound) {
            me.addContent(new Element("mid-file").addContent(mid_file));
        }
        if (use_end_sound) {
            me.addContent(new Element("end-file").addContent(end_file));
        }
        if (use_short_sound) {
            me.addContent(new Element("short-file").addContent(short_file));
        }
        i = start_sound_duration;
        log.debug("  duration = " + i.toString());
        me.addContent(new Element("start-sound-duration").addContent(i.toString()));

        return (me);
    }

    @Override
    public void setXml(Element e) {
        this.setXml(e, null);
    }

    public void setXml(Element e, VSDFile vf) {
        log.debug("ConfigurableSound: " + e.getAttributeValue("name"));
        //log.debug("  start file: " + e.getChildText("start-file"));
        if (((start_file = e.getChildText("start-file")) != null) && !(start_file.equals(""))) {
            use_start_sound = true;
        } else {
            use_start_sound = false;
        }
        //log.debug("  mid file: " + e.getChildText("mid-file"));
        if (((mid_file = e.getChildText("mid-file")) != null) && !(mid_file.equals(""))) {
            use_mid_sound = true;
        } else {
            use_mid_sound = false;
        }
        //log.debug("  end file: " + e.getChildText("end-file"));
        if (((end_file = e.getChildText("end-file")) != null) && !(end_file.equals(""))) {
            use_end_sound = true;
        } else {
            use_end_sound = false;
        }
        //log.debug("  short file: " + e.getChildText("short-file"));
        if (((short_file = e.getChildText("short-file")) != null) && !(short_file.equals(""))) {
            use_short_sound = true;
        } else {
            use_short_sound = false;
        }

        //log.debug("  start sound dur: " + e.getChildText("start-sound-duration"));
        String ssd = e.getChildText("start-sound-duration");
        if ((ssd != null) && !(ssd.equals(""))) {
            start_sound_duration = Integer.parseInt(ssd);
        } else {
            start_sound_duration = 0;
        }

        //log.debug("  gain: " + e.getChildText("gain"));
        String g = e.getChildText("gain");
        if ((g != null) && !(g.equals(""))) {
            gain = Float.parseFloat(g);
        } else {
            gain = default_gain;
        }

        /*
         log.debug("Use:  start = " + use_start_sound + 
         "mid = " + use_mid_sound +
         "end = " + use_end_sound +
         "short = " + use_short_sound);
         */
        // Reboot the sound
        initialized = false;
        this.init(vf);

    }

    private final static Logger log = LoggerFactory.getLogger(ConfigurableSound.class);

}
