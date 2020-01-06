package jmri.managers.configurexml;

import java.util.List;
import java.util.SortedSet;
import javax.vecmath.Vector3f;
import jmri.Audio;
import jmri.AudioException;
import jmri.AudioManager;
import jmri.InstanceManager;
import jmri.jmrit.audio.AudioBuffer;
import jmri.jmrit.audio.AudioFactory;
import jmri.jmrit.audio.AudioListener;
import jmri.jmrit.audio.AudioSource;
import jmri.util.FileUtil;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the abstract base and store functionality for configuring
 * AudioManagers, working with AbstractAudioManagers.
 * <p>
 * Typically, a subclass will just implement the load(Element audio) class,
 * relying on implementation here to load the individual Audio objects. Note
 * that these are stored explicitly, so the resolution mechanism doesn't need to
 * see *Xml classes for each specific Audio or AbstractAudio subclass at store
 * time.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 * @author Matthew Harris copyright (c) 2009, 2011
 */
public abstract class AbstractAudioManagerConfigXML extends AbstractNamedBeanManagerConfigXML {

    /**
     * Default constructor
     */
    public AbstractAudioManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of an AudioManager.
     *
     * @param o Object to store, of type AudioManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element audios = new Element("audio");
        setStoreElementClass(audios);
        AudioManager am = (AudioManager) o;
        if (am != null) {
            SortedSet<Audio> audioList = am.getNamedBeanSet();
            // don't return an element if there are no audios to include
            if (audioList.isEmpty()) {
                return null;
            }
            // also, don't store if we don't have any Sources or Buffers
            // (no need to store the automatically created Listener object by itself)
            if (am.getNamedBeanSet(Audio.SOURCE).isEmpty()
                    && am.getNamedBeanSet(Audio.BUFFER).isEmpty()) {
                return null;
            }
            // finally, don't store if the only Sources and Buffers are for the
            // virtual sound decoder (VSD)
            int vsdObjectCount = 0;

            // count all VSD objects
            for (Audio a : audioList) {
                String aName = a.getSystemName();
                log.debug("Check if {} is a VSD object", aName);
                if (aName.length() >= 8 && aName.substring(3, 8).equalsIgnoreCase("$VSD:")) {
                    log.debug("...yes");
                    vsdObjectCount++;
                }
            }

            log.debug("Found {} VSD objects of {} objects", vsdObjectCount,
                    am.getNamedBeanSet(Audio.SOURCE).size() + am.getNamedBeanSet(Audio.BUFFER).size()
            );

            // check if the total number of Sources and Buffers is equal to
            // the number of VSD objects - if so, exit.
            if (am.getNamedBeanSet(Audio.SOURCE).size()
                    + am.getNamedBeanSet(Audio.BUFFER).size() == vsdObjectCount) {
                log.debug("Only VSD objects - nothing to store");
                return null;
            }

            // store global information
            AudioFactory audioFact = am.getActiveAudioFactory();
            if (audioFact != null) {
                audios.setAttribute("distanceattenuated", audioFact.isDistanceAttenuated() ? "yes" : "no");
            }
            // store the audios
            for (Audio a : audioList) {
                String aName = a.getSystemName();
                log.debug("system name is {}", aName);

                if (aName.length() >= 8 && aName.substring(3, 8).equalsIgnoreCase("$VSD:")) {
                    log.debug("Skipping storage of VSD object {}", aName);
                    continue;
                }

                // Transient objects for current element and any children
                Element e = null;
                Element ce = null;

                int type = a.getSubType();
                if (type == Audio.BUFFER) {
                    AudioBuffer ab = (AudioBuffer) a;
                    e = new Element("audiobuffer").setAttribute("systemName", aName);
                    e.addContent(new Element("systemName").addContent(aName));

                    // store common part
                    storeCommon(ab, e);

                    // store sub-type specific data
                    String url = ab.getURL();
                    ce = new Element("url")
                            .addContent("" + (url.isEmpty() ? "" : FileUtil.getPortableFilename(url)));
                    e.addContent(ce);

                    ce = new Element("looppoint");
                    ce.setAttribute("start", "" + ab.getStartLoopPoint());
                    ce.setAttribute("end", "" + ab.getEndLoopPoint());
                    e.addContent(ce);

                    ce = new Element("streamed");
                    ce.addContent("" + (ab.isStreamed() ? "yes" : "no"));
                    e.addContent(ce);
                } else if (type == Audio.LISTENER) {
                    AudioListener al = (AudioListener) a;
                    e = new Element("audiolistener").setAttribute("systemName", aName);
                    e.addContent(new Element("systemName").addContent(aName));

                    // store common part
                    storeCommon(al, e);

                    // store sub-type specific data
                    ce = new Element("position");
                    ce.setAttribute("x", "" + al.getPosition().x);
                    ce.setAttribute("y", "" + al.getPosition().y);
                    ce.setAttribute("z", "" + al.getPosition().z);
                    e.addContent(ce);

                    ce = new Element("velocity");
                    ce.setAttribute("x", "" + al.getVelocity().x);
                    ce.setAttribute("y", "" + al.getVelocity().y);
                    ce.setAttribute("z", "" + al.getVelocity().z);
                    e.addContent(ce);

                    ce = new Element("orientation");
                    ce.setAttribute("atX", "" + al.getOrientation(Audio.AT).x);
                    ce.setAttribute("atY", "" + al.getOrientation(Audio.AT).y);
                    ce.setAttribute("atZ", "" + al.getOrientation(Audio.AT).z);
                    ce.setAttribute("upX", "" + al.getOrientation(Audio.UP).x);
                    ce.setAttribute("upY", "" + al.getOrientation(Audio.UP).y);
                    ce.setAttribute("upZ", "" + al.getOrientation(Audio.UP).z);
                    e.addContent(ce);

                    ce = new Element("gain");
                    ce.addContent("" + al.getGain());
                    e.addContent(ce);

                    ce = new Element("metersperunit");
                    ce.addContent("" + al.getMetersPerUnit());
                    e.addContent(ce);
                } else if (type == Audio.SOURCE) {
                    AudioSource as = (AudioSource) a;
                    e = new Element("audiosource")
                            .setAttribute("systemName", aName);
                    e.addContent(new Element("systemName").addContent(aName));

                    // store common part
                    storeCommon(as, e);

                    // store sub-type specific data
                    ce = new Element("position");
                    ce.setAttribute("x", "" + as.getPosition().x);
                    ce.setAttribute("y", "" + as.getPosition().y);
                    ce.setAttribute("z", "" + as.getPosition().z);
                    e.addContent(ce);

                    ce = new Element("velocity");
                    ce.setAttribute("x", "" + as.getVelocity().x);
                    ce.setAttribute("y", "" + as.getVelocity().y);
                    ce.setAttribute("z", "" + as.getVelocity().z);
                    e.addContent(ce);

                    ce = new Element("assignedbuffer");
                    if (as.getAssignedBuffer() != null) {
                        ce.addContent("" + as.getAssignedBufferName());
                    }
                    e.addContent(ce);

                    ce = new Element("gain");
                    ce.addContent("" + as.getGain());
                    e.addContent(ce);

                    ce = new Element("pitch");
                    ce.addContent("" + as.getPitch());
                    e.addContent(ce);

                    ce = new Element("distances");
                    ce.setAttribute("ref", "" + as.getReferenceDistance());
                    float f = as.getMaximumDistance();
                    ce.setAttribute("max", "" + f);
                    e.addContent(ce);

                    ce = new Element("loops");
                    ce.setAttribute("min", "" + as.getMinLoops());
                    ce.setAttribute("max", "" + as.getMaxLoops());
//                    ce.setAttribute("mindelay", ""+as.getMinLoopDelay());
//                    ce.setAttribute("maxdelay", ""+as.getMaxLoopDelay());
                    e.addContent(ce);

                    ce = new Element("fadetimes");
                    ce.setAttribute("in", "" + as.getFadeIn());
                    ce.setAttribute("out", "" + as.getFadeOut());
                    e.addContent(ce);

                    ce = new Element("positionrelative");
                    ce.addContent("" + (as.isPositionRelative() ? "yes" : "no"));
                    e.addContent(ce);
                }

                log.debug("store Audio {}", aName);
                audios.addContent(e);
            }
        }
        return audios;
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param audio The top-level element being created
     */
    abstract public void setStoreElementClass(Element audio);

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Utility method to load the individual Audio objects. If there's no
     * additional info needed for a specific Audio type, invoke this with the
     * parent of the set of Audio elements.
     *
     * @param audio Element containing the Audio elements to load.
     */
    public void loadAudio(Element audio) {

        AudioManager am = InstanceManager.getDefault(jmri.AudioManager.class);

        // Count number of loaded Audio objects
        int loadedObjects = 0;

        // Load buffers first
        List<Element> audioList = audio.getChildren("audiobuffer");
        log.debug("Found {} Audio Buffer objects", audioList.size());

        for (Element au : audioList) {

            String sysName = getSystemName(au);
            if (sysName == null) {
                log.warn("unexpected null in systemName {} {}", (au), (au).getAttributes());
                break;
            }

            String userName = getUserName(au);
            log.debug("create Audio: ({})({})", sysName, (userName == null ? "<null>" : userName));

            try {
                AudioBuffer ab = (AudioBuffer) am.newAudio(sysName, userName);

                // load common parts
                loadCommon(ab, au);

                // load sub-type specific parts
                // Transient objects for reading child elements
                Element ce;
                String value;

                if ((ce = au.getChild("url")) != null) {
                    ab.setURL(ce.getValue());
                }

                if ((ce = au.getChild("looppoint")) != null) {
                    if ((value = ce.getAttributeValue("start")) != null) {
                        ab.setStartLoopPoint(Integer.parseInt(value));
                    }
                    if ((value = ce.getAttributeValue("end")) != null) {
                        ab.setEndLoopPoint(Integer.parseInt(value));
                    }
                }

                if ((ce = au.getChild("streamed")) != null) {
                    ab.setStreamed(ce.getValue().equals("yes"));
                }

            } catch (AudioException ex) {
                log.error("Error loading AudioBuffer ({}): ", sysName, ex);
            }
        }
        loadedObjects += audioList.size();

        // Now load sources
        audioList = audio.getChildren("audiosource");
        log.debug("Found {} Audio Source objects", audioList.size());

        for (Element au : audioList) {

            String sysName = getSystemName(au);
            if (sysName == null) {
                log.warn("unexpected null in systemName {} {}", (au), (au).getAttributes());
                break;
            }

            String userName = getUserName(au);
            log.debug("create Audio: ({})({})", sysName, (userName == null ? "<null>" : userName));

            try {
                AudioSource as = (AudioSource) am.newAudio(sysName, userName);

                // load common parts
                loadCommon(as, au);

                // load sub-type specific parts
                // Transient objects for reading child elements
                Element ce;
                String value;

                if ((ce = au.getChild("position")) != null) {
                    as.setPosition(
                            new Vector3f(
                                    Float.parseFloat(ce.getAttributeValue("x")),
                                    Float.parseFloat(ce.getAttributeValue("y")),
                                    Float.parseFloat(ce.getAttributeValue("z"))));
                }

                if ((ce = au.getChild("velocity")) != null) {
                    as.setVelocity(
                            new Vector3f(
                                    Float.parseFloat(ce.getAttributeValue("x")),
                                    Float.parseFloat(ce.getAttributeValue("y")),
                                    Float.parseFloat(ce.getAttributeValue("z"))));
                }

                if ((ce = au.getChild("assignedbuffer")) != null) {
                    if (ce.getValue().length() != 0 && !ce.getValue().equals("null")) {
                        as.setAssignedBuffer(ce.getValue());
                    }
                }

                if ((ce = au.getChild("gain")) != null && ce.getValue().length() != 0) {
                    as.setGain(Float.parseFloat(ce.getValue()));
                }

                if ((ce = au.getChild("pitch")) != null && ce.getValue().length() != 0) {
                    as.setPitch(Float.parseFloat(ce.getValue()));
                }

                if ((ce = au.getChild("distances")) != null) {
                    if ((value = ce.getAttributeValue("ref")) != null) {
                        as.setReferenceDistance(Float.parseFloat(value));
                    }
                    if ((value = ce.getAttributeValue("max")) != null) {
                        as.setMaximumDistance(Float.parseFloat(value));
                    }
                }

                if ((ce = au.getChild("loops")) != null) {
                    if ((value = ce.getAttributeValue("min")) != null) {
                        as.setMinLoops(Integer.parseInt(value));
                    }
                    if ((value = ce.getAttributeValue("max")) != null) {
                        as.setMaxLoops(Integer.parseInt(value));
                    }
//                    if ((value = ce.getAttributeValue("mindelay"))!=null)
//                        as.setMinLoopDelay(Integer.parseInt(value));
//                    if ((value = ce.getAttributeValue("maxdelay"))!=null)
//                        as.setMaxLoopDelay(Integer.parseInt(value));
                }

                if ((ce = au.getChild("fadetimes")) != null) {
                    if ((value = ce.getAttributeValue("in")) != null) {
                        as.setFadeIn(Integer.parseInt(value));
                    }
                    if ((value = ce.getAttributeValue("out")) != null) {
                        as.setFadeOut(Integer.parseInt(value));
                    }
                }

                if ((ce = au.getChild("positionrelative")) != null) {
                    as.setPositionRelative(ce.getValue().equals("yes"));
                }

            } catch (AudioException ex) {
                log.error("Error loading AudioSource ({}): ", sysName, ex);
            }
        }
        loadedObjects += audioList.size();

        // Finally, load Listeners if needed
        if (loadedObjects > 0) {
            audioList = audio.getChildren("audiolistener");
            log.debug("Found {} Audio Listener objects", audioList.size());

            for (Element au : audioList) {

                String sysName = getSystemName(au);
                if (sysName == null) {
                    log.warn("unexpected null in systemName {} {}", (au), (au).getAttributes());
                    break;
                }

                String userName = getUserName(au);
                log.debug("create Audio: ({})({})", sysName, (userName == null ? "<null>" : userName));

                try {
                    AudioListener al = (AudioListener) am.newAudio(sysName, userName);

                    // load common parts
                    loadCommon(al, au);

                    // load sub-type specific parts
                    // Transient object for reading child elements
                    Element ce;

                    if ((ce = au.getChild("position")) != null) {
                        al.setPosition(
                                new Vector3f(
                                        Float.parseFloat(ce.getAttributeValue("x")),
                                        Float.parseFloat(ce.getAttributeValue("y")),
                                        Float.parseFloat(ce.getAttributeValue("z"))));
                    }

                    if ((ce = au.getChild("velocity")) != null) {
                        al.setVelocity(
                                new Vector3f(
                                        Float.parseFloat(ce.getAttributeValue("x")),
                                        Float.parseFloat(ce.getAttributeValue("y")),
                                        Float.parseFloat(ce.getAttributeValue("z"))));
                    }

                    if ((ce = au.getChild("orientation")) != null) {
                        al.setOrientation(
                                new Vector3f(
                                        Float.parseFloat(ce.getAttributeValue("atX")),
                                        Float.parseFloat(ce.getAttributeValue("atY")),
                                        Float.parseFloat(ce.getAttributeValue("atZ"))),
                                new Vector3f(
                                        Float.parseFloat(ce.getAttributeValue("upX")),
                                        Float.parseFloat(ce.getAttributeValue("upY")),
                                        Float.parseFloat(ce.getAttributeValue("upZ"))));
                    }

                    if ((ce = au.getChild("gain")) != null) {
                        al.setGain(Float.parseFloat(ce.getValue()));
                    }

                    if ((ce = au.getChild("metersperunit")) != null) {
                        al.setMetersPerUnit(Float.parseFloat((ce.getValue())));
                    }

                } catch (AudioException ex) {
                    log.error("Error loading AudioListener ({}): ", sysName, ex);
                }
            }
            Attribute da = audio.getAttribute("distanceattenuated");
            if (da != null) {
                AudioFactory audioFact = am.getActiveAudioFactory();
                if (audioFact != null) {
                    audioFact.setDistanceAttenuated(da.getValue().equals("yes"));
                }
            }
        }
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.AudioManager.class).getXMLOrder();
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractAudioManagerConfigXML.class);

}
