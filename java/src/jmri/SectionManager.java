package jmri;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.AbstractManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Implementation of a SectionManager.
 * <p>
 * This doesn't have a "new" interface, since Sections are independently
 * implemented, instead of being system-specific.
 * <p>
 * Note that Section system names must begin with system prefix and type character,
 * usually IY, and be followed by a string, usually, but not always, a number. This
 * is enforced when a Section is created.
 * <br>
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
 * @author Dave Duchamp Copyright (C) 2008
 */
public class SectionManager extends AbstractManager<Section> implements InstanceManagerAutoDefault {

    public SectionManager() {
        super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        InstanceManager.getDefault(SensorManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(BlockManager.class).addVetoableChangeListener(this);
    }

    @Override
    public int getXMLOrder() {
        return Manager.SECTIONS;
    }

    @Override
    public char typeLetter() {
        return 'Y';
    }

    @Override
    public Class<Section> getNamedBeanClass() {
        return Section.class;
    }

    /**
     * Method to create a new Section if the Section does not exist.
     *
     * @param systemName the desired system name
     * @param userName   the desired user name
     * @return a new Section or null if a Section with the same systemName or
     *         userName already exists, or if there is trouble creating a new
     *         Section.
     */
    public Section createNewSection(String systemName, String userName) {
        // check system name
        if ((systemName == null) || (systemName.length() < 1)) {
            // no valid system name entered, return without creating
            return null;
        }
        String sysName = systemName;
        if (!sysName.startsWith(getSystemNamePrefix())) {
            sysName = makeSystemName(sysName);
        }
        // Check that Section does not already exist
        Section y;
        if (userName != null && !userName.equals("")) {
            y = getByUserName(userName);
            if (y != null) {
                return null;
            }
        }
        y = getBySystemName(sysName);
        if (y != null) {
            return null;
        }
        // Section does not exist, create a new Section
        y = new Section(sysName, userName);
        // save in the maps
        register(y);

        // Keep track of the last created auto system name
        updateAutoNumber(systemName);

        return y;
    }

    public Section createNewSection(String userName) {
        return createNewSection(getAutoSystemName(), userName);
    }

    /**
     * Remove an existing Section.
     *
     * @param y the section to remove
     */
    public void deleteSection(Section y) {
        // delete the Section
        deregister(y);
        y.dispose();
    }

    /**
     * Get an existing Section. First looks up assuming that name is a User
     * Name. If this fails looks up assuming that name is a System Name.
     *
     * @param name the name to find; user names are searched for a match first,
     *             followed by system names
     * @return the found section of null if no matching section found
     */
    public Section getSection(String name) {
        Section y = getByUserName(name);
        if (y != null) {
            return y;
        }
        return getBySystemName(name);
    }

    public Section getBySystemName(String key) {
        return _tsys.get(key);
    }

    public Section getByUserName(String key) {
        return _tuser.get(key);
    }

    /**
     * Validates all Sections.
     *
     * @param frame   ignored
     * @param lePanel the panel containing sections to validate
     * @return number or validation errors; -2 is returned if there are no
     *         sections
     */
    public int validateAllSections(jmri.util.JmriJFrame frame, LayoutEditor lePanel) {
        Set<Section> set = getNamedBeanSet();
        int numSections = 0;
        int numErrors = 0;
        if (set.size() <= 0) {
            return -2;
        }
        for (Section section : set) {
            String s = section.validate(lePanel);
            if (!s.equals("")) {
                log.error(s);
                numErrors++;
            }
            numSections++;
        }
        log.debug("Validated {} Sections - {} errors or warnings.", numSections, numErrors);
        return numErrors;
    }

    /**
     * Checks direction sensors in SSL for signals.
     *
     * @param lePanel the panel containing direction sensors
     * @return the number or errors; 0 if no errors; -1 if the panel is null; -2
     *         if there are no sections
     */
    public int setupDirectionSensors(LayoutEditor lePanel) {
        if (lePanel == null) {
            return -1;
        }
        Set<Section> set = getNamedBeanSet();
        int numSections = 0;
        int numErrors = 0;
        if (set.size() <= 0) {
            return -2;
        }
        for (Section section : set) {
            int errors = section.placeDirectionSensors(lePanel);
            numErrors = numErrors + errors;
            numSections++;
        }
        log.debug("Checked direction sensors for {} Sections - {} errors or warnings.", numSections, numErrors);
        return numErrors;
    }

    /**
     * Removes direction sensors from SSL for all signals.
     *
     * @param lePanel the panel containing direction sensors
     * @return the number or errors; 0 if no errors; -1 if the panel is null; -2
     *         if there are no sections
     */
    public int removeDirectionSensorsFromSSL(LayoutEditor lePanel) {
        if (lePanel == null) {
            return -1;
        }
        jmri.jmrit.display.layoutEditor.ConnectivityUtil cUtil = lePanel.getConnectivityUtil();
        Set<Section> set = getNamedBeanSet();
        if (set.size() <= 0) {
            return -2;
        }
        int numErrors = 0;
        List<String> sensorList = new ArrayList<>();
        for (Section s : set) {
            String name = s.getReverseBlockingSensorName();
            if ((name != null) && (!name.equals(""))) {
                sensorList.add(name);
            }
            name = s.getForwardBlockingSensorName();
            if ((name != null) && (!name.equals(""))) {
                sensorList.add(name);
            }
        }
        jmri.SignalHeadManager shManager = InstanceManager.getDefault(jmri.SignalHeadManager.class);
        for (SignalHead sh : shManager.getNamedBeanSet()) {
            if (!cUtil.removeSensorsFromSignalHeadLogic(sensorList, sh)) {
                numErrors++;
            }
        }
        return numErrors;
    }

    /**
     * Initialize all blocking sensors that exist - sets them to 'ACTIVE'
     */
    public void initializeBlockingSensors() {
        for (Section s : getNamedBeanSet()) {
            try {
                if (s.getForwardBlockingSensor() != null) {
                    s.getForwardBlockingSensor().setState(Sensor.ACTIVE);
                }
                if (s.getReverseBlockingSensor() != null) {
                    s.getReverseBlockingSensor().setState(Sensor.ACTIVE);
                }
            } catch (jmri.JmriException reason) {
                log.error("Exception when initializing blocking Sensors for Section " + s.getSystemName());
            }
        }
    }

    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameSections" : "BeanNameSection");
    }

    private final static Logger log = LoggerFactory.getLogger(SectionManager.class);
}
