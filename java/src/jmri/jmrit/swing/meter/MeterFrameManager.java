package jmri.jmrit.swing.meter;

import java.util.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import jmri.*;

/**
 * Default implementation of a MeterFrameManager.
 * This class is only used by jmri.jmrit.swing.meter.MeterFrame and
 * jmri.jmrit.swing.meter.configurexml.MeterFrameManagerXml so no need to store
 * it in the InstanceManager.
 *
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class MeterFrameManager {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MeterFrameManager.class);

    private static final MeterFrameManager _instance = new MeterFrameManager();

    private final Map<UUID, MeterFrame> _meterFrameList = new HashMap<>();

    /**
     * Get the instance of MeterFrameManager.
     * @return the MeterFrameManager instance
     */
    @CheckReturnValue
    public static MeterFrameManager getInstance() {
        return _instance;
    }

    /**
     * Create a new MeterFrameManager instance.
     */
    private MeterFrameManager() {
        log.debug("registerSelf for config of type {}", getClass());
        InstanceManager.getOptionalDefault(ConfigureManager.class).ifPresent(cm -> {
            cm.registerConfig(this, getXMLOrder());
            log.debug("registering for config of type {}", getClass());
        });
    }

    /**
     * Determine the order that types should be written when storing panel
     * files. Uses one of the constants defined in this class.
     * <p>
     * Yes, that's an overly-centralized methodology, but it works for now.
     *
     * @return write order for this Manager; larger is later.
     */
    @CheckReturnValue
    public int getXMLOrder() {
        return Manager.METERFRAMES;
    }

    public void register(@Nonnull MeterFrame frame) {
        _meterFrameList.put(frame.getUUID(), frame);
    }

    public void deregister(@Nonnull MeterFrame frame) {
        _meterFrameList.remove(frame.getUUID());
    }

    public MeterFrame getByUUID(@Nonnull UUID uuid) {
        return _meterFrameList.get(uuid);
    }

    public Collection<MeterFrame> getMeterFrames() {
        return Collections.unmodifiableCollection(_meterFrameList.values());
    }

}
