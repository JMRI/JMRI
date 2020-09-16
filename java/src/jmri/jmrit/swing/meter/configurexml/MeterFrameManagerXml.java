package jmri.jmrit.swing.meter.configurexml;

import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.UUID;

import jmri.InstanceManager;
import jmri.Meter;
import jmri.MeterManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.swing.meter.MeterFrame;
import jmri.jmrit.swing.meter.MeterFrameManager;

import org.jdom2.Element;

/**
 * Stores MeterFrames to the panel file.
 * @author Daniel Bergqvist Copyright (c) 2020
 */
public class MeterFrameManagerXml extends jmri.configurexml.AbstractXmlAdapter {

    public MeterFrameManagerXml() {
    }
    
    @Override
    public Element store(Object o) {
        Element meterFrames = new Element("meterFrames");
        setStoreElementClass(meterFrames);
        
        for (MeterFrame frame : MeterFrameManager.getInstance().getMeterFrames()) {
            meterFrames.addContent(storeMeterFrame(frame));
        }
        
        return meterFrames;
    }
    
    public Element storeMeterFrame(MeterFrame frame) {
        Element e = new Element("meterFrame");
        
        Element e2 = new Element("uuid");
        e2.addContent(frame.getUUID().toString());
        e.addContent(e2);
        
        e2 = new Element("meter");
        e2.addContent(frame.getMeter().getSystemName());    // This should be a NamedBeanHandle
        e.addContent(e2);
        
        return e;
    }
    
    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param meterFrames The top-level element being created
     */
    public void setStoreElementClass(Element meterFrames) {
        meterFrames.setAttribute("class", this.getClass().getName());  // NOI18N
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        // Don't create frames if headless
        if (GraphicsEnvironment.isHeadless()) return true;
        
        List<Element> frames = shared.getChildren("meterFrame");
        
        for (Element elem : frames) {
            String uuidStr = elem.getChild("uuid").getText();
            String meterSystemName = elem.getChild("meter").getText();      // This should be a NamedBeanHandle
            Meter meter = InstanceManager.getDefault(MeterManager.class).getBySystemName(meterSystemName);
            UUID uuid = UUID.fromString(uuidStr);
            MeterFrame frame = MeterFrameManager.getInstance().getByUUID(uuid);
            if (frame == null) {
                frame = new MeterFrame(uuid);
                frame.initComponents();
                if (meter != null) frame.setMeter(meter);
                log.error("uuid: {}, meter: {}, meter: {}, systemName: {}", frame.getUUID(), meter, frame.getMeter().getSystemName(), meterSystemName);
                frame.setVisible(true);
            } else {
                if (meter != null) frame.setMeter(meter);
            }
        }
        
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MeterFrameManagerXml.class);
}
