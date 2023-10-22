package jmri.jmrit.display.configurexml;

import jmri.jmrit.audio.AudioBuffer;
import jmri.jmrit.audio.AudioSource;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.*;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Handle configuration for display.AudioIcon objects.
 *
 * @author Daniel Bergqvist (C) 2023
 */
public class AudioIconXml extends PositionableLabelXml {

    /**
     * Default implementation for storing the contents of a AudioIcon
     *
     * @param o Object to store, of type AudioIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        AudioIcon p = (AudioIcon) o;

        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("audioicon");
        storeCommonAttributes(p, element);

        element.addContent(new Element("Identity").addContent(Integer.toString(p.getIdentity())));

        AudioSource source = (AudioSource)p.getAudio();
        if (source != null) {
            element.setAttribute("audio", source.getSystemName());

            AudioBuffer buffer = (AudioBuffer) jmri.InstanceManager.getDefault(jmri.AudioManager.class)
                    .getAudio(source.getAssignedBufferName());
            if (buffer != null) {
                // We need to use the attribute "sound" instead of the element "sound"
                // since the method jmri.web.server.WebServer.portablePathToURI()
                // converts paths in attributes to portable paths, but not paths
                // in elements.
                element.setAttribute("sound", buffer.getURL());
            }
        }

        if (p.isText()) {
            if (p.getUnRotatedText() != null) {
                element.setAttribute("text", p.getUnRotatedText());
            }
            storeTextInfo(p, element);
        }

        if (p.isIcon() && p.getIcon() != null) {
            element.setAttribute("icon", "yes");
            element.addContent(storeIcon("icon", (NamedIcon) p.getIcon()));
        }

        element.setAttribute("onClickOperation", p.getOnClickOperation().name());
        element.setAttribute("playSoundWhenJmriPlays", p.getPlaySoundWhenJmriPlays() ? "yes" : "no");
        element.setAttribute("stopSoundWhenJmriStops", p.getStopSoundWhenJmriStops() ? "yes" : "no");

        storeLogixNG_Data(p, element);

        element.setAttribute("class", "jmri.jmrit.display.configurexml.AudioIconXml");
        return element;
    }

    /**
     * Create a AudioIcon, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     * @throws JmriConfigureXmlException when a error prevents creating the objects as as
     *                   required by the input XML
     */
    @Override
    public void load(Element element, Object o) throws JmriConfigureXmlException {
        // create the objects
        AudioIcon l = null;

        int identity = Integer.parseInt(element.getChildText("Identity"));

        // get object class and determine editor being used
        Editor editor = (Editor) o;

        String audioName;
        Attribute attr = element.getAttribute("audio");
        if (attr == null) {
            log.error("incorrect information for audio; must use audio name");
            editor.loadFailed();
            return;
        } else {
            audioName = attr.getValue();
        }

        if (element.getAttribute("icon") != null) {
            NamedIcon icon;
            String name = element.getAttribute("icon").getValue();
            if (name.equals("yes")) {
                icon = getNamedIcon("icon", element, "AudioIcon ", editor);
            } else {
                icon = NamedIcon.getIconByName(name);
                if (icon == null) {
                    icon = editor.loadFailed("AudioIcon", name);
                    if (icon == null) {
                        log.info("AudioIcon icon removed for url= {}", name);
                        return;
                    }
                }
            }
            // abort if name != yes and have null icon
            if (icon == null && !name.equals("yes")) {
                log.info("AudioIcon icon removed for url= {}", name);
                return;
            }
            l = new AudioIcon(identity, icon, editor);
            try {
                Attribute a = element.getAttribute("rotate");
                if (a != null && icon != null) {
                    int rotation = element.getAttribute("rotate").getIntValue();
                    icon.setRotation(rotation, l);
                }
            } catch (org.jdom2.DataConversionException e) {
            }

            if (name.equals("yes")) {
                NamedIcon nIcon = loadIcon(l, "icon", element, "AudioIcon ", editor);
                if (nIcon != null) {
                    l.updateIcon(nIcon);
                } else {
                    log.info("AudioIcon icon removed for url= {}", name);
                    return;
                }
            } else {
                l.updateIcon(icon);
            }
        }

        if (element.getAttribute("text") != null) {
            if (l == null) {
                l = new AudioIcon(identity, element.getAttribute("text").getValue(), editor);
            }
            loadTextInfo(l, element);

        } else if (l == null) {
            log.error("AudioIcon is null!");
            if (log.isDebugEnabled()) {
                java.util.List<Attribute> attrs = element.getAttributes();
                log.debug("\tElement Has {} Attributes:", attrs.size());
                for (Attribute a : attrs) {
                    log.debug("  attribute:  {} = {}", a.getName(), a.getValue());
                }
                java.util.List<Element> kids = element.getChildren();
                log.debug("\tElementHas {} children:", kids.size());
                for (Element e : kids) {
                    log.debug("  child:  {} = \"{}\"", e.getName(), e.getValue());
                }
            }
            editor.loadFailed();
            return;
        }

        l.setAudio(audioName);

        String onClickOperation = element.getAttribute("onClickOperation").getValue();
        l.setOnClickOperation(AudioIcon.OnClickOperation.valueOf(onClickOperation));

        String yesno = element.getAttribute("playSoundWhenJmriPlays").getValue();
        if ((yesno != null) && (!yesno.equals(""))) {
            l.setPlaySoundWhenJmriPlays(yesno.equals("yes"));
        }

        yesno = element.getAttribute("stopSoundWhenJmriStops").getValue();
        if ((yesno != null) && (!yesno.equals(""))) {
            l.setStopSoundWhenJmriStops(yesno.equals("yes"));
        }

        try {
            editor.putItem(l);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }

        loadLogixNG_Data(l, element);

        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.LABELS, element);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AudioIconXml.class);
}
