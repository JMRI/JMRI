package jmri.jmrit.vsdecoder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;
import jmri.jmrit.XmlFile;
import jmri.Scale;
import jmri.Reporter;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.jmrit.display.layoutEditor.*;
import jmri.jmrit.display.EditorManager;
import jmri.util.FileUtil;
import jmri.util.PhysicalLocation;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load parameter from XML for the Advanced Location Following.
 *
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
 * @author Klaus Killinger Copyright (C) 2018-2022
 */
public class VSDGeoFile extends XmlFile {

    static final String VSDGeoDataFileName = "VSDGeoData.xml"; // NOI18N
    protected Element root;
    private float blockParameter[][][];
    private List<List<PhysicalLocation>> blockPositionlists; // Two-dimensional ArrayList
    private List<PhysicalLocation>[] blockPositionlist;
    private List<List<Integer>> reporterlists; // Two-dimensional ArrayList
    private List<Integer>[] reporterlist;
    private List<Boolean> circlelist;
    private int setup_index;
    private int num_issues;
    boolean geofile_ok;
    private int num_setups;
    private Scale _layout_scale;
    float layout_scale;
    int check_time; // Time interval in ms for track following updates
    private ArrayList<LayoutEditor> panels;
    private ArrayList<LayoutEditor> panelsFinal;
    HashMap<Block, LayoutEditor> possibleStartBlocks;
    ArrayList<Block> blockList;
    private LayoutEditor models;
    PhysicalLocation models_origin;
    int lf_version;  // location following
    int alf_version; // advanced location following

    /**
     * Looking for additional parameter for train tracking
     */
    @SuppressWarnings("unchecked") // ArrayList[n] is not detected as the coded generics
    public VSDGeoFile() {

        // Setup lists for Reporters and Positions
        reporterlists = new ArrayList<>();
        reporterlist = new ArrayList[VSDecoderManager.max_decoder]; // Limit number of supported VSDecoders
        blockPositionlists = new ArrayList<>();
        blockPositionlist = new ArrayList[VSDecoderManager.max_decoder];
        for (int i = 0; i < VSDecoderManager.max_decoder; i++) {
            reporterlist[i] = new ArrayList<>();
            blockPositionlist[i] = new ArrayList<>();
        }

        // Another list to provide a flag for circling or non-circling routes
        circlelist = new ArrayList<>();

        File file = new File(FileUtil.getUserFilesPath() + VSDGeoDataFileName);

        models = null;
        geofile_ok = false;

        // Try to load data from the file
        try {
            root = rootFromFile(file);
        } catch (java.io.FileNotFoundException e1) {
            log.debug("File {} for train tracking is not available", VSDGeoDataFileName);
            root = null;
        } catch (Exception e2) {
            log.error("Exception while loading file", VSDGeoDataFileName, e2);
            root = null;
        }
        if (root == null) {
            // no VSDGeoData file available
            lf_version = 1; // assume "location following"
            return;
        }

        // Get some layout parameters and route geometric data
        String n;
        n = root.getChildText("layout-scale");
        if (n != null) {
            _layout_scale = jmri.ScaleManager.getScale(n);
            if (_layout_scale == null) {
                _layout_scale = jmri.ScaleManager.getScale("N"); // default
                log.info("File {}: Element layout-scale '{}' unknown, defaulting to N", VSDGeoDataFileName, n);
            }
        } else {
            _layout_scale = jmri.ScaleManager.getScale("N"); // default
            log.info("File {}: Element layout-scale missing, defaulting to N", VSDGeoDataFileName);
        }
        layout_scale = (float) _layout_scale.getScaleRatio(); // Take this for further calculations
        log.debug("layout-scale: {}, used for further calculations: {}", _layout_scale.toString(), layout_scale);

        n = root.getChildText("check-time");
        if (n != null) {
            check_time = Integer.parseInt(n);
            // Process some limitations; values in milliseconds
            if (check_time < 500 || check_time > 5000) {
                check_time = 2000; // default
                log.info("File {}: Element check-time not in range, defaulting to {} ms", VSDGeoDataFileName, check_time);
            }
        } else {
            check_time = 2000; // default
            log.info("File {}: Element check-time missing, defaulting to {} ms", VSDGeoDataFileName, check_time);
        }
        log.debug("check-time: {} ms", check_time);

        // Now look if the file contains "setup" data or "panel" data
        n = root.getChildText("setup");
        if ((n != null) && (!n.isEmpty())) {
            log.debug("A setup found for ALF version 1");
            alf_version = 1;
            readGeoInfos();

        } else {

            // Looking for the "panel" data
            n = root.getChildText("models");
            if ((n == null) || (n.isEmpty())) {
                // cannot continue
                log.warn("No Panel specified in {}", VSDGeoDataFileName);
            } else {
                // An existing (loaded) panel is expected
                panels = new ArrayList<>(InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class));
                if (panels.isEmpty()) {
                    log.warn("No Panel loaded. Please restart PanelPro and load Panel \"{}\" first", n);
                    return;
                } else {
                    // There is at least one panel;
                    // does it must match with the specified panel?
                    for (LayoutEditor panel : panels) {
                        log.debug("checking panel \"{}\" ... looking for \"{}\"", panel.getTitle(), n);
                        if (n.equals(panel.getTitle())) {
                            models = panel;
                            break;
                        }
                    }
                }
                if (models == null) {
                    log.error("Loaded Panel \"{}\" does not match with specified Panel \"{}\". Please correct and restart PanelPro", panels, n);
                } else {
                    log.debug("selected panel: {}", models.getTitle());
                    n = root.getChildText("models-origin");
                    if ((n != null) && (!n.isEmpty())) {
                        models_origin = PhysicalLocation.parse(n);
                        log.debug("models-origin: {}", models_origin);
                    } else {
                        models_origin = new PhysicalLocation(346f, 260f, 0f); // default
                    }
                    alf_version = 2;
                    log.debug("ALF version: {}", alf_version);
                    readPanelInfos(); // good to go
                }
            }
        }
    }

    private void readGeoInfos() {
        // Detect number of "setup" tags and maximal number of "geodataset" tags

        Element c, c0, c1;
        String n, np;
        num_issues = 0;

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
            log.warn("File {}: Invalid number of setups or geodatasets", VSDGeoDataFileName);
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
                int rep_int = 0;
                if (c1.getChildText("reporter-systemname") != null) {
                    np = c1.getChildText("reporter-systemname");
                    Reporter rep = jmri.InstanceManager.getDefault(jmri.ReporterManager.class).getBySystemName(np);
                    if (rep != null) {
                        String repNumber = np.substring(2); // connection prefix 3 signs?
                        // An internal Reporter System Name can have non-numeric parts - do not allow here
                        if (org.apache.commons.lang3.StringUtils.isNumeric(repNumber)) {
                            rep_int = Integer.parseInt(repNumber);
                            reporterlist[setup_index].add(rep_int);
                        } else {
                            log.warn("File {}: Reporter System Name {} is not valid for VSD", VSDGeoDataFileName, np);
                            num_issues++;
                        }
                        n = c1.getChildText("position");
                        // An element "position" is required and a XML schema and a XML schema is not yet in place
                        if (n != null) {
                            PhysicalLocation pl = PhysicalLocation.parse(n);
                            blockPositionlist[setup_index].add(pl);
                            // Establish relationship Reporter-PhysicalLocation (see window Manage VSD Locations)
                            PhysicalLocation.setBeanPhysicalLocation(pl, rep);
                            log.debug("Reporter: {}, position set to: {}", rep, pl);
                        } else {
                            log.warn("File {}: Element position not found", VSDGeoDataFileName);
                            num_issues++;
                        }
                    } else {
                        log.warn("File {}: No Reporter available for system name = {}", VSDGeoDataFileName, np);
                        num_issues++;
                    }
                } else {
                    log.warn("File {}: Reporter system name missing", VSDGeoDataFileName);
                    num_issues++;
                }

                if (num_issues == 0) {
                    n = c1.getChildText("radius");
                    if (n != null) {
                        blockParameter[setup_index][j][0] = Float.parseFloat(n);
                        log.debug(" radius: {}", n);
                    } else {
                        log.warn("File {}: Element radius not found", VSDGeoDataFileName);
                        num_issues++;
                    }
                    n = c1.getChildText("slope");
                    if (n != null) {
                        blockParameter[setup_index][j][1] = Float.parseFloat(n);
                        log.debug(" slope: {}", n);
                    } else {
                        // If a radius is not defined (radius = 0), slope must exist!
                        if (blockParameter[setup_index][j][0] == 0.0f) {
                            log.warn("File {}: Element slope not found", VSDGeoDataFileName);
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
                            log.warn("File {}: Element rotate-xpos not found", VSDGeoDataFileName);
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
                        log.warn("{}: Element length not found", VSDGeoDataFileName);
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
                            log.warn("File {}: Only the last geodataset should have an end-position", VSDGeoDataFileName);
                            num_issues++;
                        }
                    }
                }
                j++;
            }

            if (!is_end_position_set) {
                log.warn("File {}: End-position missing for setup {}", VSDGeoDataFileName, setup_index + 1);
                num_issues++;
            }
            addLists();
            setup_index++;
        }
        finishRead();
    }

    // Gather infos about the LayoutEditor panel(s)
    private void readPanelInfos() {
        int max_geodatasets = 0;
        possibleStartBlocks = new HashMap<>();
        blockList = new ArrayList<>();

        log.debug("Found panel: {}", models);

        // Look for panels with an Edge Connector
        panels = new ArrayList<>(InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class));
        panelsFinal = new ArrayList<>();
        for (LayoutEditor p : panels) {
            for (LayoutTrack lt : p.getLayoutTracks()) {
                if (lt instanceof PositionablePoint) {
                    PositionablePoint pp = (PositionablePoint) lt;
                    if (pp.getType() == PositionablePoint.PointType.EDGE_CONNECTOR) {
                        if (!panelsFinal.contains(p)) {
                            panelsFinal.add(p);
                        }
                    }
                }
            }
        }
        log.debug("edge panels: {}", panelsFinal);

        if (panelsFinal.isEmpty()) {
            panelsFinal.add(models);
        }
        log.debug("final panels: {}", panelsFinal);

        // ALL LAYOUT TRACKS; count turnouts and track segments only
        int max_ts = 0;
        for (LayoutEditor p : panelsFinal) {
            for (LayoutTrack lt : p.getLayoutTracks()) {
                if (lt instanceof LayoutTurnout) {
                    max_geodatasets++;
                } else if (lt instanceof TrackSegment) {
                    max_geodatasets++;
                    max_ts++;
                } else if (lt instanceof LevelXing) {
                    max_geodatasets++;
                    max_geodatasets++; // LevelXing contains 2 blocks, AC and BD
                } else {
                    log.debug("no LayoutTurnout, no TrackSegment, no PositionablePoint, but: {}", lt);
                }
            }
        }
        log.debug("number of turnouts and track segments: {}", max_geodatasets);

        // minimal 1 layout track
        if (max_geodatasets == 0) {
            log.warn("Panel must have minimum one layout track");
            return;
        }

        // minimal 1 track segment
        if (max_ts == 0) {
            log.warn("Panel must have minimum one track segment");
            return;
        }

        // Find size and setup array to save the block parameters
        BlockManager bmgr = InstanceManager.getDefault(BlockManager.class);
        Set<Block> blockSet = bmgr.getNamedBeanSet();
        if (blockSet.isEmpty()) {
            log.warn("Panel must have minimum one block");
            return;
        }

        LayoutBlockManager lm = InstanceManager.getDefault(LayoutBlockManager.class);
        LayoutBlock lblk;

        log.debug("panels: {}", panelsFinal);

        // List all blocks and list possible start blocks
        for (LayoutEditor le : panelsFinal) {
            log.debug("### panel: {}", le);
            for (Block bl : blockSet) {
                if (bl != null) {
                    String userName2 = bl.getUserName();
                    if (userName2 != null) {
                        lblk = lm.getByUserName(userName2);
                        if (lblk != null) {
                            log.debug("{}, block system name: {}, user name: {}", le.getTitle(), bl.getSystemName(), userName2);
                            int tsInBlock = 0;
                            // List of all LayoutTracks in the block
                            ArrayList<LayoutTrack> layoutTracksInBlock = new ArrayList<>();
                            for (LayoutTrack lt : le.getLayoutTracks()) {
                                if (lt instanceof LayoutTurnout) {
                                    LayoutTurnout to = (LayoutTurnout) lt;
                                    if (to.getLayoutBlock() == lblk) {
                                        layoutTracksInBlock.add(lt);
                                        blockList.add(bl);
                                    }
                                } else if (lt instanceof TrackSegment) {
                                    TrackSegment ts = (TrackSegment) lt;
                                    if (ts.getLayoutBlock() == lblk) {
                                        layoutTracksInBlock.add(lt);
                                        blockList.add(bl);
                                        tsInBlock++;
                                    }
                                } else if (lt instanceof LevelXing) {
                                    LevelXing lx = (LevelXing) lt;
                                    if (lx.getLayoutBlockAC() == lblk || lx.getLayoutBlockBD() == lblk) {
                                        layoutTracksInBlock.add(lt); // LevelXing contains 2 blocks, AC and BD; add one more entry here
                                        blockList.add(bl);
                                    }
                                }
                            }
                            log.debug("layoutTracksInBlock: {}", layoutTracksInBlock);
                            // A possible start-block is a block with a single TrackSegment
                            if (tsInBlock == 1 && possibleStartBlocks.get(bl) == null) {
                                possibleStartBlocks.put(bl, le); // Save a Block together with its LE Panel
                            }
                        }
                    }
                }
            }
        }
        log.debug("Block list: {}, possible start-blocks: {}", blockList, possibleStartBlocks);
        geofile_ok = true;
    }

    private void addLists() {
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
            if (blockPositionlist[setup_index].get(0) != null
                    && blockPositionlist[setup_index].get(0).x == blockPositionlist[setup_index].get(last_index).x
                    && blockPositionlist[setup_index].get(0).y == blockPositionlist[setup_index].get(last_index).y
                    && blockPositionlist[setup_index].get(0).z == blockPositionlist[setup_index].get(last_index).z) {
                circlelist.add(true);
            } else {
                circlelist.add(false);
            }
            log.debug("circling: {}", circlelist.get(setup_index));
        }
    }

    private void finishRead() {
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
        setGeoFileStatus();
    }

    private void setGeoFileStatus() {
        if (num_issues > 0) {
            geofile_ok = false;
            log.warn("set geofile to not ok");
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
