package jmri;

import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.managers.AbstractManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Basic Implementation of a SectionManager.
 * <P>
 * This doesn't have a "new" interface, since Sections are independently
 * implemented, instead of being system-specific.
 * <P>
 * Note that Section system names must begin with IY, and be followed by a
 * string, usually, but not always, a number. All alphabetic characters in a
 * Section system name must be upper case. This is enforced when a Section is
 * created.
 * <BR>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * </P><P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * </P>
 *
 * @author Dave Duchamp Copyright (C) 2008
 */
public class SectionManager extends AbstractManager<Section> implements PropertyChangeListener, InstanceManagerAutoDefault {

    public SectionManager() {
        super();
        InstanceManager.getDefault(SensorManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(BlockManager.class).addVetoableChangeListener(this);
    }

    @Override
    public int getXMLOrder() {
        return Manager.SECTIONS;
    }

    @Override
    public String getSystemPrefix() {
        return "I";
    }

    @Override
    public char typeLetter() {
        return 'Y';
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
        if ((sysName.length() < 2) || (!sysName.substring(0, 2).equals("IY"))) {
            sysName = "IY" + sysName;
        }
        // Check that Section does not already exist
        Section y;
        if (userName != null && !userName.equals("")) {
            y = getByUserName(userName);
            if (y != null) {
                return null;
            }
        }
        String sName = sysName.toUpperCase().trim();
        y = getBySystemName(sysName);
        if (y == null) {
            y = getBySystemName(sName);
        }
        if (y != null) {
            return null;
        }
        // Section does not exist, create a new Section
        y = new Section(sName, userName);
        // save in the maps
        register(y);
        /*The following keeps trace of the last created auto system name.
         currently we do not reuse numbers, although there is nothing to stop the
         user from manually recreating them*/
        if (systemName.startsWith("IY:AUTO:")) {
            try {
                int autoNumber = Integer.parseInt(systemName.substring(8));
                if (autoNumber > lastAutoSectionRef) {
                    lastAutoSectionRef = autoNumber;
                }
            } catch (NumberFormatException e) {
                log.warn("Auto generated SystemName " + systemName + " is not in the correct format");
            }
        }
        return y;
    }

    public Section createNewSection(String userName) {
        int nextAutoSectionRef = lastAutoSectionRef + 1;
        StringBuilder b = new StringBuilder("IY:AUTO:");
        String nextNumber = paddedNumber.format(nextAutoSectionRef);
        b.append(nextNumber);
        return createNewSection(b.toString(), userName);
    }

    DecimalFormat paddedNumber = new DecimalFormat("0000");

    int lastAutoSectionRef = 0;

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

    public Section getBySystemName(String name) {
        String key = name.toUpperCase();
        return _tsys.get(key);
    }

    public Section getByUserName(String key) {
        return _tuser.get(key);
    }

    /**
     * {@inheritDoc}
     * 
     * Forces upper case and trims leading and trailing whitespace.
     * Does not check for valid prefix, hence doesn't throw NamedBean.BadSystemNameException.
     */
    @CheckReturnValue
    @Override
    public @Nonnull
    String normalizeSystemName(@Nonnull String inputName) {
        // does not check for valid prefix, hence doesn't throw NamedBean.BadSystemNameException
        return inputName.toUpperCase().trim();
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
        List<String> list = getSystemNameList();
        int numSections = 0;
        int numErrors = 0;
        if (list.size() <= 0) {
            return -2;
        }
        for (int i = 0; i < list.size(); i++) {
            String s = getBySystemName(list.get(i)).validate(lePanel);
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
        List<String> list = getSystemNameList();
        int numSections = 0;
        int numErrors = 0;
        if (list.size() <= 0) {
            return -2;
        }
        for (int i = 0; i < list.size(); i++) {
            int errors = getBySystemName(list.get(i)).placeDirectionSensors(lePanel);
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
        List<String> list = getSystemNameList();
        if (list.size() <= 0) {
            return -2;
        }
        int numErrors = 0;
        ArrayList<String> sensorList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Section s = getBySystemName(list.get(i));
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
        List<String> signalList = shManager.getSystemNameList();
        for (int j = 0; j < signalList.size(); j++) {
            SignalHead sh = shManager.getBySystemName(signalList.get(j));
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
        List<String> list = getSystemNameList();
        for (int i = 0; i < list.size(); i++) {
            Section s = getBySystemName(list.get(i));
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
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameSection");
    }

    private final static Logger log = LoggerFactory.getLogger(SectionManager.class);
}
