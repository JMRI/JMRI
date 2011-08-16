package jmri.jmrix.vsdecoder;

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
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision$
 */

// JMRI and Java stuff
import jmri.jmrit.Sound;
import java.awt.event.*;
import jmri.jmrit.audio.*;
import java.io.File;

// XML stuff
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Content;

// Usage:
// HornSound() : constructor
// play() : plays short horn pop
// loop() : starts extended sustain horn
// stop() : ends extended sustain horn (plays end sound)

class ConfigurableSound extends VSDSound {

    protected String start_file = "CSX_K5LA_Horn4_Start.wav";
    protected String mid_file = "CSX_K5LA_Horn4_Sustain.wav";
    protected String end_file = "CSX_K5LA_Horn4_End.wav";
    protected String short_file = "CSX_K5LA_Horn4_Short.wav";

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
    

    javax.swing.Timer t;
    /*
    public ConfigurableSound() {
	is_playing = false;
	//initialized = init();
    }
    */
    public ConfigurableSound(String name) {
	super(name);
	is_playing = false;
    }

    public boolean init() {
	return(this.init(null));
    }

    public boolean init(VSDFile vf) {
	
	if (!initialized) {
	    if (use_start_sound) {
		start_sound = new SoundBite(vf, start_file, name+"_Start", name+"_Start");
		start_sound.setLooped(false);
	    }
	    if (use_mid_sound) {
		mid_sound = new SoundBite(vf, mid_file, name+"_Mid", name+"_Mid");
		mid_sound.setLooped(false);
	    }
	    if (use_end_sound) {
		end_sound = new SoundBite(vf, end_file, name+"_End", name+"_End");
		end_sound.setLooped(false);
	    }
	    if (use_short_sound) {
		short_sound = new SoundBite(vf, short_file, name+"_Short", name+"_Short");
		short_sound.setLooped(false);
	    }
	    t = new javax.swing.Timer(start_sound_duration, new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			handleTimerPop(e);
		    }
		});
	}
	return(true);
    }
    
    public boolean isPlaying() {
        return(is_playing);
    }

    public void play() {
	if (use_short_sound) {
	    short_sound.play();
	    is_playing = false; // short sound, won't be playing long...
	} else {
	    if (use_start_sound) {
		start_sound.play();
		if (use_mid_sound) {
		    t.setRepeats(false); // timer pop only once to trigger the sustain sound.
		    t.start();
		    is_playing = true;
		}
	    }
	    else if (use_mid_sound)
		mid_sound.setLooped(true);
		mid_sound.play();
	}
    }

    public void loop() {
	if (use_start_sound) {
	    start_sound.setLooped(false);
	    start_sound.play();
	    t.setInitialDelay(start_sound_duration);
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
	log.info("Received timer pop after start sound played.");
	//TODO: Need to validate that this is the timer pop
	if (use_mid_sound) {
	    mid_sound.setLooped(true);
	    mid_sound.play();
	}
	t.stop();
    }

    public void stop() {
	//start_sound.stop();
	//mid_sound.stop();
	log.warn("Stopping");
	if (use_start_sound) {
	    start_sound.stop();
	}
	if (use_mid_sound) {
	    mid_sound.stop();
	}
	t.stop();
	if (use_end_sound) {
	    end_sound.setLooped(false);
	    end_sound.play();
	}
	is_playing = false;
    }

    public void fadeIn() {
	this.play();
    }

    public void fadeOut() {
	this.stop();
    }
    
    public Element getXml() {
	Element me = new Element("sound");
	Boolean b;
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
	if (use_start_sound)
	    me.addContent(new Element("start-file").addContent(start_file));
	if (use_mid_sound)
	    me.addContent(new Element("mid-file").addContent(mid_file));
	if (use_end_sound)
	    me.addContent(new Element("end-file").addContent(end_file));
	if (use_short_sound)
	    me.addContent(new Element("short-file").addContent(short_file));
	/*
	b = use_start_sound;
	log.debug("  use_start_file = " + b.toString());
	me.addContent(new Element("use-start-sound").addContent(b.toString()));
	b = use_mid_sound;
	log.debug("  use_mid_file = " + b.toString());
	me.addContent(new Element("use-mid-sound").addContent(b.toString()));
	b = use_end_sound;
	log.debug("  use_end_file = " + b.toString());
	me.addContent(new Element("use-end-sound").addContent(b.toString()));
	b = use_short_sound;
	log.debug("  use_short_file = " + b.toString());
	me.addContent(new Element("use-short-sound").addContent(b.toString()));
	*/
	i = start_sound_duration;
	log.debug("  duration = " + i.toString());
	me.addContent(new Element("start-sound-duration").addContent(i.toString()));
	
	return(me);
    }

    public void setXml(Element e) {
	this.setXml(e, null);
    }

    public void setXml(Element e, VSDFile vf) {
	this.setName(e.getAttributeValue("name"));
	log.debug("ConfigurableSound: " + e.getAttributeValue("name"));
	log.debug("  start file: " + e.getChildText("start-file"));
	if (((start_file = e.getChildText("start-file")) != null) && !(start_file.equals("")))
	    use_start_sound = true;
	else
	    use_start_sound = false;
	log.debug("  mid file: " + e.getChildText("mid-file"));
	if (((mid_file = e.getChildText("mid-file")) != null) && !(mid_file.equals("")))
	    use_mid_sound = true;
	else
	    use_mid_sound = false;
	log.debug("  end file: " + e.getChildText("end-file"));
	if (((end_file = e.getChildText("end-file")) != null) && !(end_file.equals("")))
	    use_end_sound = true;
	else
	    use_end_sound = false;
	log.debug("  short file: " + e.getChildText("short-file"));
	if (((short_file = e.getChildText("short-file")) != null) && !(short_file.equals("")))
	    use_short_sound = true;
	else
	    use_short_sound = false;

	log.debug("  start sound dur: " + e.getChildText("start-sound-duration"));
	String ssd = e.getChildText("start-sound-duration");
	if ((ssd != null) && !(ssd.equals("")))
	    start_sound_duration = Integer.parseInt(ssd);
	else
	    start_sound_duration = 0;

	log.debug("Use:  start = " + use_start_sound + 
		  "mid = " + use_mid_sound +
		  "end = " + use_end_sound +
		  "short = " + use_short_sound);


	/*
	log.debug("  use start file: " + e.getChild("use-start-sound").getValue());
	log.debug("     value = " + Boolean.parseBoolean(e.getChild("use-start-sound").getValue()));
	use_start_sound = Boolean.parseBoolean(e.getChild("use-start-sound").getValue());
	log.debug("  use mid file: " + e.getChild("use-mid-sound").getValue());
	log.debug("     value = " + Boolean.parseBoolean(e.getChild("use-mid-sound").getValue()));
	use_mid_sound = Boolean.parseBoolean(e.getChild("use-mid-sound").getValue());
	log.debug("  use end file: " + e.getChild("use-end-sound").getValue());
	log.debug("     value = " + Boolean.parseBoolean(e.getChild("use-end-sound").getValue()));
	use_end_sound = Boolean.parseBoolean(e.getChild("use-end-sound").getValue());
	log.debug("  use short file: " + e.getChild("use-short-sound").getValue());
	log.debug("     value = " + Boolean.parseBoolean(e.getChild("use-short-sound").getValue()));
	use_short_sound = Boolean.parseBoolean(e.getChild("use-short-sound").getValue());
	*/

	// Reboot the sound
	initialized = false;
	this.init(vf);
	
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConfigurableSound.class.getName());

}