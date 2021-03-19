package jmri.jmrit.display.layoutEditor;

import java.util.ArrayList;
import java.util.List;

import jmri.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The transit creation tool, is designed to be used by higher level tools to
 * create transits between Beans. The higher level tools would already have a
 * valid knowledge of the track layout and Sections, therefore this tool does
 * little validation of sections being added to the transit.
 * <hr>
 * The tool currently only deals with SignalMasts, that have had logic created
 * and also have a section associated between them.
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
 * @author Kevin Dickerson Copyright (C) 2011
 * @author George Warner Copyright (c) 2017-2018
 */
final public class TransitCreationTool {

    public TransitCreationTool() {
    }

    ArrayList<NamedBean> list = new ArrayList<>();

    public void addNamedBean(NamedBean nb) throws JmriException {
        if (!list.isEmpty()) {
            if (list.get(list.size() - 1) == nb) {
                log.debug("Bean is the same as the last one so will not add or error");
                return;
            }
            //Run through a series of checks that this bean is reachable from the previous
            if ((nb instanceof SignalMast) && (list.get(list.size() - 1) instanceof SignalMast)) {
                jmri.SignalMastLogicManager smlm = InstanceManager.getDefault(jmri.SignalMastLogicManager.class);
                jmri.SignalMastLogic sml = smlm.getSignalMastLogic(((SignalMast) list.get(list.size() - 1)));
                if (sml == null || !sml.isDestinationValid((SignalMast) nb)) {
                    String error = Bundle.getMessage("TCTErrorMastPairsNotValid", nb.getDisplayName(), list.get(list.size() - 1).getDisplayName());
                    log.error("will throw {}", error);
                    throw new JmriException(error);
                }
                if (sml.getAssociatedSection((SignalMast) nb) == null) {
                    String error = Bundle.getMessage("TCTErrorMastPairsNoSection", list.get(list.size() - 1).getDisplayName(), nb.getDisplayName());
                    log.error("will throw {}", error);
                    throw new JmriException(error);
                }
            } else {
                //Need to add the method to get layout block connectivity.  Also work checking that the Layout Block routing has been initialised.
            }
        }
        list.add(nb);
    }

    public Transit createTransit() throws JmriException {
        TransitManager tm = InstanceManager.getDefault(jmri.TransitManager.class);
        String transitName =
                "From " + list.get(0).getDisplayName() + " to " + list.get(list.size() - 1).getDisplayName();
        Transit t = tm.createNewTransit(transitName);
        if (t == null) {
            log.error("Unable to create transit {}", transitName);
            throw new JmriException(Bundle.getMessage("TCTErrorUnableToCreate", transitName));
        }
        int seqNo = 1;
        if (list.get(0) instanceof SignalMast) {
            jmri.SignalMastLogicManager smlm = InstanceManager.getDefault(jmri.SignalMastLogicManager.class);
            for (int i = 1; i <= list.size() - 1; i++) {
                if (i == 1) {
                    // unless first sm = last sm in which case first section is
                    // last section
                    // (cirle)
                    if (list.get(i - 1) == list.get(list.size() - 1)) {
                        t.addTransitSection(new jmri.TransitSection(
                                smlm.getSignalMastLogic((SignalMast) list.get(list.size() - 2))
                                        .getAssociatedSection((SignalMast) list.get(list.size() - 1)),
                                seqNo, Section.FORWARD));
                        seqNo++;
                    } else {
                        Block blockToFind =
                                smlm.getSignalMastLogic((SignalMast) list.get(i - 1)).getFacingBlock().getBlock();
                        if (smlm.getSignalMastLogic((SignalMast) list.get(i - 1)).getFacingBlock() != null) {
                            // find shortest section with facing block at bottom
                            // of list
                            blockToFind =
                                    smlm.getSignalMastLogic((SignalMast) list.get(i - 1)).getFacingBlock().getBlock();
                            int count = 999;
                            Section secFound = null;
                            for (Section sec : InstanceManager.getDefault(jmri.SectionManager.class)
                                    .getNamedBeanSet()) {
                                if (sec.containsBlock(blockToFind)) {
                                    // for ( EntryPoint ep :
                                    // sec.getReverseEntryPointList()) {
                                    // if (ep.isForwardType() && ep.getBlock()
                                    // == blockToFind) {
                                    
                                    //check if we can find a section that contains the signal mast that 
                                    if (sec.getBlockList().size() < count) {
                                        count = sec.getBlockList().size();
                                        secFound = sec;
                                    }

                                }
                            }
                            //get the sections of signalmastlogics which have the first signal mast as their destination
                            //each section will be the same so weonly need the first one

                            Section secFoundForward = null;
                            SignalMast sm = (SignalMast) list.get(i - 1);
                            List<SignalMastLogic> smlList =  smlm.getLogicsByDestination(sm);
                            for( SignalMastLogic sml : smlList){
                                secFoundForward = sml.getAssociatedSection(sm);
                                break;
                            }

                            if (secFound != null) {
                                if ( secFoundForward == null ) {
                                    t.addTransitSection(new jmri.TransitSection(secFound, seqNo, Section.REVERSE));
                                }else{
                                    t.addTransitSection(new jmri.TransitSection(secFoundForward, seqNo, Section.FORWARD));
                                }
                                seqNo++;
                            } else {

                            }
                        }
                    }
                }
                jmri.SignalMastLogic sml = smlm.getSignalMastLogic((SignalMast) list.get(i - 1));
                Section sec = sml.getAssociatedSection((SignalMast) list.get(i));
                // In theory sec being null would already have been tested when
                // the signal was added.
                if (sec == null) {
                    String error = Bundle.getMessage("TCTErrorMastPairsNoSection", list.get(i - 1).getDisplayName(),
                            list.get(i).getDisplayName());
                    log.error("will throw {}", error);
                    tm.deregister(t);
                    t.dispose();
                    cancelTransitCreate();
                    throw new JmriException(error);
                }
                t.addTransitSection(new jmri.TransitSection(sec, seqNo, Section.FORWARD));
                seqNo++;
            }
        }
        // Once created clear the list for a fresh start.
        list = new ArrayList<>();
        return t;
    }

    public void cancelTransitCreate() {
        list = new ArrayList<>();
    }

    public List<NamedBean> getBeans() {
        return list;
    }

    public boolean isToolInUse() {
        return !list.isEmpty();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TransitCreationTool.class);
}
