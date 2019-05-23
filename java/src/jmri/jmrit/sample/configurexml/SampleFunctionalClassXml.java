package jmri.jmrit.sample.configurexml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.jmrit.sample.SampleFunctionalClass;
import org.jdom2.Element;

/**
 * Handle XML persistance of SampleFunctionalClass objects.
 * <p>
 * Mote we have not (yet) updated xml/schema to provide an XML schema for this
 * sample.  Configure PanelPro to run the jython/dis.py script to disable
 * verification when loading files written by this.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2018
 */
public class SampleFunctionalClassXml extends jmri.configurexml.AbstractXmlAdapter {

    public SampleFunctionalClassXml() {
    }

    /**
     * Default implementation for storing the contents of a SampleFunctionalClassXml.
     *
     * @param o Object to store
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        
        SampleFunctionalClass s = (SampleFunctionalClass) o;
        
        Element elem = new Element("samplefunctionalclass");
        elem.setAttribute("class", this.getClass().getName());

        elem.addContent(
            new Element("remember")
                .addContent(s.toString())
        );
        return elem;
    }

    @Override
    public boolean load(Element shared, Element perNode) {

        boolean result = true;
        
        String content = shared.getChild("remember").getValue();
        
        // the following creates and registers
        
        new SampleFunctionalClass(content);
        
        return result;
    }
    
    // Conversion format for dates created by Java Date.toString().
    // The Locale needs to be always US, irrelevant from computer's and program's settings!
    final SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

    /**
     * Update static data from XML file
     *
     * @param element Top level Element to unpack.
     * @param o       ignored
     */
    @Override
    public void load(Element element, Object o) {
        log.error("load(Element, Object) called unexpectedly");
    }

    @Override
    public int loadOrder() {
        return jmri.Manager.TIMEBASE;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SampleFunctionalClassXml.class);

}
