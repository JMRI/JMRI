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

import org.jdom2.Attribute;
import org.jdom2.Element;

import javax.annotation.Nonnull;

/**
 * Stores MeterFrames to the panel file.
 * @author Daniel Bergqvist Copyright (c) 2020
 */
public class MeterFrameManagerXml extends jmri.configurexml.AbstractXmlAdapter {

    private final EnumIO<MeterFrame.Unit> unitEnumMap = new EnumIoNames<>(MeterFrame.Unit.class);

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
        if (frame.getMeter() == null) {
            return e; // apparently user did not assign a real world meter; do not save
        }
        e2.addContent(frame.getMeter().getSystemName()); // this should be a NamedBeanHandle
        e.addContent(e2);
        
        e.setAttribute("unit", unitEnumMap.outputFromEnum(frame.getUnit()));
        e.setAttribute("integer-digits", Integer.toString(frame.getNumIntegerDigits()));
        e.setAttribute("decimal-digits", Integer.toString(frame.getNumDecimalDigits()));
        e.setAttribute("x", Integer.toString(frame.getX()));
        e.setAttribute("y", Integer.toString(frame.getY()));
        e.setAttribute("width", Integer.toString(frame.getWidth()));
        e.setAttribute("height", Integer.toString(frame.getHeight()));
        
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
    public boolean load(@Nonnull Element shared, Element perNode) throws JmriConfigureXmlException {
        // Don't create frames if headless
        if (GraphicsEnvironment.isHeadless()) return true;
        
        List<Element> frames = shared.getChildren("meterFrame");
        
        for (Element elem : frames) {
            String uuidStr = elem.getChild("uuid").getText();
            String meterSystemName = elem.getChild("meter").getText();      // This should be a NamedBeanHandle, but may not exist yet
            Meter meter = InstanceManager.getDefault(MeterManager.class).getBySystemName(meterSystemName);
            UUID uuid = UUID.fromString(uuidStr);
            MeterFrame frame = MeterFrameManager.getInstance().getByUUID(uuid);
            if (frame == null) {
                log.debug("creating frame for uuid: {}, selected meter: {}", uuidStr, meterSystemName);
                frame = new MeterFrame(uuid);
                frame.initComponents();
            }
            if (meter != null) {
                frame.setMeter(meter);
            } else {
                log.debug("selected meter '{}' not (yet) defined, remembering for later.", meterSystemName);                
                frame.setInitialMeterName(meterSystemName);
            }
            
            Attribute a = elem.getAttribute("unit");
            if (a != null) frame.setUnit(unitEnumMap.inputFromAttribute(a));
            
            frame.setNumIntegerDigits(getAttributeIntegerValue(elem, "integer-digits", 3));
            frame.setNumDecimalDigits(getAttributeIntegerValue(elem, "decimal-digits", 0));
            
            frame.setLocation(
                    Integer.parseInt(elem.getAttributeValue("x")),
                    Integer.parseInt(elem.getAttributeValue("y")));
            frame.setSize(
                    Integer.parseInt(elem.getAttributeValue("width")),
                    Integer.parseInt(elem.getAttributeValue("height")));
            frame.setVisible(true);
        }
        
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MeterFrameManagerXml.class);

}
