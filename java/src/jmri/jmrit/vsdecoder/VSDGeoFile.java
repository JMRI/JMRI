package jmri.jmrit.vsdecoder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import jmri.jmrit.XmlFile;
import jmri.Scale;
import jmri.PhysicalLocationReporter;
import jmri.Reporter;
import jmri.util.FileUtil;
import jmri.util.PhysicalLocation;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author Klaus Killinger Copyright (C) 2018
 */
public class VSDGeoFile extends XmlFile {

    static final String VSDGeoDataFileName = "VSDGeoData.xml"; //NOI18N
    protected Element root;
    private float blockParameter[][][];
    private List<List<PhysicalLocation>> blockPositionlists; // Two-dimensional ArrayList
    private List<List<Integer>> reporterlists; // Two-dimensional ArrayList
    private List<Boolean> circlelist;
    private int setup_index;
    private int num_issues;
    boolean geofile_ok;
    int num_setups;
    Scale _layout_scale;
    float layout_scale;
    int check_time; // Time interval in ms for track following updates

    /**
     * Looking for additional parameter for train tracking
     */
    @SuppressWarnings("unchecked") // ArrayList[n] is not detected as the coded generics
    public VSDGeoFile() {

        // Setup lists for Reporters and Positions
        reporterlists = new ArrayList<>();
        List<Integer>[] reporterlist = new ArrayList[VSDecoderManager.max_decoder]; // Limit number of supported VSDecoders
        blockPositionlists = new ArrayList<>();
        List<PhysicalLocation>[] blockPositionlist = new ArrayList[VSDecoderManager.max_decoder];
        for (int i = 0; i < VSDecoderManager.max_decoder; i++) {
            reporterlist[i] = new ArrayList<>();
            blockPositionlist[i] = new ArrayList<>();
        }

        // Another list to provide a flag for circling or non-circling routes
        circlelist = new ArrayList<>();

        File file = new File(FileUtil.getUserFilesPath() + VSDGeoDataFileName);

        Element c, c0, c1;
        String n, np;
        num_issues = 0;

        // Try to load data from the file
        try {
            root = rootFromFile(file);
        } catch (java.io.FileNotFoundException e1) {
            log.debug("{} for train tracking is not available", VSDGeoDataFileName);
            root = null;
        } catch (Exception e2) {
            log.error("Exception while loading file {}:", VSDGeoDataFileName, e2);
            root = null;
        }
        if (root == null) {
            geofile_ok = false;
            return;
        }

        // Get some layout parameters and route geometric data
        n = root.getChildText("layout-scale");
        if (n != null) {
            _layout_scale = jmri.ScaleManager.getScale(n);
            if (_layout_scale == null) {
                _layout_scale = jmri.ScaleManager.getScale("N"); // default
                log.info("{}: Element layout-scale '{}' unknown, defaulting to N", VSDGeoDataFileName, n); // NOI18N
            }
        } else {
            _layout_scale = jmri.ScaleManager.getScale("N"); // default
            log.info("{}: Element layout-scale missing, defaulting to N", VSDGeoDataFileName); // NOI18N
        }
        layout_scale = (float) _layout_scale.getScaleRatio(); // Take this for further calculations
        log.debug("layout-scale: {}, used for further calculations: {}", _layout_scale.toString(), layout_scale);

        n = root.getChildText("check-time");
        if (n != null) {
            check_time = Integer.parseInt(n);
            // Process some limitations; values in milliseconds
            if (check_time < 500 || check_time > 5000) {
                check_time = 2000; // default
                log.info("{}: Element check-time not in range, defaulting to {} ms", VSDGeoDataFileName, check_time); // NOI18N
            }
        } else {
            check_time = 2000; // default
            log.info("{}: Element check-time missing, defaulting to {} ms", VSDGeoDataFileName, check_time); // NOI18N
        }
        log.debug("check-time: {} ms", check_time);

        // Detect number of "setup" tags and maximal number of "geodataset" tags
        num_setups = 0; // # setup
        int num_geodatasets = 0; // # geodataset
        int max_geodatasets = 0; // helper
        Iterator<Element> ix = root.getChildren("setup").iterator(); // NOI18N
        while (ix.hasNext()) {
            c = ix.next();
            num_geodatasets = c.getChildren("geodataset").size();
            log.debug("setup {} has {} geodataset(s)", num_setups + 1, num_geodatasets);
            if (num_geodatasets > max_geodatasets) {
                max_geodatasets = num_geodatasets; // # geodatasets can vary; take highest value
            }
            num_setups++;
        }
        log.debug("counting setups: {}, maximum geodatasets: {}", num_setups, max_geodatasets);
        // Limitation check is done by the schema validation, but a XML schema is not yet in place
        if (num_setups == 0 || num_geodatasets == 0 || num_setups > VSDecoderManager.max_decoder) {
            log.warn("{}: Invalid number of setups or geodatasets", VSDGeoDataFileName);
            geofile_ok = false;
            return;
        }

        // Setup array to save the block parameters
        blockParameter = new float[num_setups][max_geodatasets][5];

        // Go through all setups and their geodatasets 
        //  - get the PhysicalLocation (position) from the parameter file
        //  - make checks which are not covered by the schema validation
        //  - make some basic checks for not validated VSDGeoData.xml files (avoid NPEs)
        setup_index = 0;
        Iterator<Element> i0 = root.getChildren("setup").iterator(); // NOI18N
        while (i0.hasNext()) {
            c0 = i0.next();
            log.debug("--- SETUP: {}", setup_index + 1);

            boolean is_end_position_set = false; // Need one end-position per setup
            int j = 0;
            Iterator<Element> i1 = c0.getChildren("geodataset").iterator(); // NOI18N
            while (i1.hasNext()) {
                c1 = i1.next();
                Reporter rep = null;
                int rep_int = 0;
                np = c1.getChildText("reporter-systemname");
                // An element "reporter-systemname" is required and a XML schema and a XML schema is not yet in place
                if (np != null) {
                    rep = jmri.InstanceManager.getDefault(jmri.ReporterManager.class).getBySystemName(np);
                    if (rep != null) {
                        rep_int = Integer.parseInt(np.substring(2));
                        reporterlist[setup_index].add(rep_int);
                        n = c1.getChildText("position");
                        // An element "position" is required and a XML schema and a XML schema is not yet in place
                        if (n != null) {
                            PhysicalLocation pl = PhysicalLocation.parse(n);
                            blockPositionlist[setup_index].add(pl);
                            // Establish relationship Reporter-PhysicalLocation (see window Manage VSD Locations)
                            PhysicalLocation.setBeanPhysicalLocation(pl, rep);
                            log.debug("Reporter: {}, position set to: {}", rep, pl);
                        } else {
                            log.warn("{}: Element position not found", VSDGeoDataFileName); // NOI18N
                            num_issues++;
                        }
                        n = c1.getChildText("radius");
                        if (n != null) {
                            blockParameter[setup_index][j][0] = Float.parseFloat(n);
                            log.debug(" radius: {}", n); 
                        } else {
                            log.warn("{}: Element radius not found", VSDGeoDataFileName); // NOI18N
                            num_issues++;
                        }
                        n = c1.getChildText("slope");
                        if (n != null) {
                            blockParameter[setup_index][j][1] = Float.parseFloat(n);
                            log.debug(" slope: {}", n); 
                        } else {
                            // If a radius is not defined (radius = 0), slope must exist!
                            if (blockParameter[setup_index][j][0] == 0.0f) {
                                log.warn("{}: Element slope not found", VSDGeoDataFileName);
                                num_issues++;
                            }
                        }
                        n = c1.getChildText("rotate-xpos");
                        if (n != null) {
                            blockParameter[setup_index][j][2] = Float.parseFloat(n);
                            log.debug(" rotate-xpos: {}", n); 
                        } else {
                            // If a radius is defined (radius > 0), rotate-xpos must exist!
                            if (blockParameter[setup_index][j][0] > 0.0f) {
                                log.warn("{}: Element rotate-xpos not found", VSDGeoDataFileName);
                                num_issues++;
                            }
                        }
                        n = c1.getChildText("rotate-ypos");
                        if (n != null) {
                            blockParameter[setup_index][j][3] = Float.parseFloat(n);
                            log.debug(" rotate-ypos: {}", n); 
                        } else {
                            // If a radius is defined (radius > 0), rotate-ypos must exist!
                            if (blockParameter[setup_index][j][0] > 0.0f) {
                                log.warn("{}: Element rotate-ypos not found", VSDGeoDataFileName);
                                num_issues++;
                            }
                        }
                        n = c1.getChildText("length");
                        if (n != null) {
                            blockParameter[setup_index][j][4] = Float.parseFloat(n);
                            log.debug(" length: {}", n); 
                        } else {
                            log.warn("{}: Element length not found", VSDGeoDataFileName); // NOI18N
                            num_issues++;
                        }
                        n = c1.getChildText("end-position");
                        if (n != null) {
                            if (!is_end_position_set) {
                                blockPositionlist[setup_index].add(PhysicalLocation.parse(n));
                                is_end_position_set = true;
                                log.debug("end-position for location {} set to {}", j,
                                        blockPositionlist[setup_index].get(blockPositionlist[setup_index].size() - 1));
                            } else {
                                log.warn("{}: Only the last geodataset should have an end-position", VSDGeoDataFileName);
                                num_issues++;
                            }
                        }
                    } else {
                        log.warn("{}: No Reporter available for system name = {}", VSDGeoDataFileName, np);
                        num_issues++;
                    }
                } else {
                    log.warn("{}: Reporter system name missing", VSDGeoDataFileName); // NOI18N
                    num_issues++;
                }
                j++;
            }

            if (!is_end_position_set) {
                log.warn("{}: End-position missing for setup {}", VSDGeoDataFileName, setup_index + 1);
                num_issues++;
            }

            if (num_issues == 0) {
                // Add lists to their array
                reporterlists.add(reporterlist[setup_index]);
                blockPositionlists.add(blockPositionlist[setup_index]);

                // Prove, if the setup has a circling route and add the result to a list
                //  compare first and last blockPosition without the tunnel attribute
                //  needed for the Reporter validation check in VSDecoderManager
                int last_index = blockPositionlist[setup_index].size() - 1;
                log.debug("first setup position: {}, last setup position: {}", blockPositionlist[setup_index].get(0),
                        blockPositionlist[setup_index].get(last_index));
                if (blockPositionlist[setup_index].get(0).x == blockPositionlist[setup_index].get(last_index).x
                        && blockPositionlist[setup_index].get(0).y == blockPositionlist[setup_index].get(last_index).y
                        && blockPositionlist[setup_index].get(0).z == blockPositionlist[setup_index].get(last_index).z) {
                    circlelist.add(true);
                } else {
                    circlelist.add(false);
                }
                log.debug("circling: {}", circlelist.get(setup_index));
            }

            setup_index++;
        }

        // Some Debug infos
        if (log.isDebugEnabled()) {
            log.debug("--- LISTS");
            log.debug("number of Reporter lists: {}", reporterlists.size());
            log.debug("Reporter lists with their Reporters (digit only): {}", reporterlists);
            //log.debug("TEST reporter get 0 list size: {}", reporterlists.get(0).size());
            //log.debug("TEST reporter [0] list size: {}", reporterlist[0].size());
            log.debug("number of Position lists: {}", blockPositionlists.size());
            log.debug("Position lists: {}", blockPositionlists);
            log.debug("--- COUNTERS");
            log.debug("number of setups: {}", num_setups);            
            log.debug("number of issues: {}", num_issues);
        }

        if (num_issues > 0) {
            geofile_ok = false;
        } else {
            geofile_ok = true;
        }
    }

    // Number of setups
    public int getNumberOfSetups() {
        return num_setups;
    }

    // Reporter lists
    public List<List<Integer>> getReporterList() {
        return reporterlists;
    }

    // Reporter Parameter
    public float[][][] getBlockParameter() {
        return blockParameter;
    }

    // Reporter (Block) Position lists
    public List<List<PhysicalLocation>> getBlockPosition() {
        return blockPositionlists;
    }

    // Circling list
    public List<Boolean> getCirclingList() {
        return circlelist;
    }

    private static final Logger log = LoggerFactory.getLogger(VSDGeoFile.class);

}
