package jmri.jmrit.display.layoutEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import jmri.Section;
import jmri.Transit;
import jmri.TransitManager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.SignalMast;
import jmri.JmriException;

/**
 * The transit creation tool, is designed to be used by higher level tools
 * to create transits between Beans.  The higher level tools would already
 * have a valid knowledge of the track layout and Sections, therefore this 
 * tool does little validation of sections being added to the transit.
 * <hr>
 * The tool currently only deals with SignalMasts, that have had logic created
 * and also have a section associated between them.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <P>
 *
 * @author			Kevin Dickerson Copyright (C) 2011
 * @version			$Revision: 23263 $
 */
public class TransitCreationTool{
    
    public TransitCreationTool(){
    }
    
    ArrayList<NamedBean> list = new ArrayList<NamedBean>();
    
    public void addNamedBean(NamedBean nb) throws JmriException{
        if(!list.isEmpty()){
            if(list.get(list.size()-1)==nb){
                log.debug("Bean is the same as the last one so will not add or error");
                return;
            }
            //Run through a series of checks that this bean is reachable from the previous
            if((nb instanceof SignalMast) && (list.get(list.size()-1) instanceof SignalMast)){
                jmri.SignalMastLogicManager smlm = InstanceManager.signalMastLogicManagerInstance();
                jmri.SignalMastLogic sml = smlm.getSignalMastLogic(((SignalMast)list.get(list.size()-1)));
                if(sml==null || !sml.isDestinationValid((SignalMast)nb)){
                    String error = Bundle.getMessage("TCTErrorMastPairsNotValid", nb.getDisplayName(), list.get(list.size()-1).getDisplayName());
                    log.error(error);
                    throw new JmriException(error);
                }
                if(sml.getAssociatedSection((SignalMast)nb)==null){
                    String error = Bundle.getMessage("TCTErrorMastPairsNoSection", list.get(list.size()-1).getDisplayName(), nb.getDisplayName());
                    log.error(error);
                    throw new JmriException(error);
                }
            } else {
                //Need to add the method to get layout block connectivity.  Also work checking that the Layout Block routing has been initialised.
            }
        }
        list.add(nb);
    }
    
    public Transit createTransit() throws JmriException{
        TransitManager tm = InstanceManager.transitManagerInstance();
        String transitName = "From " + list.get(0).getDisplayName() + " to " + list.get(list.size()-1).getDisplayName();
        Transit t = tm.createNewTransit(transitName);
        if(t==null){
            log.error("Unable to create transit " + transitName);
            throw new JmriException(Bundle.getMessage("TCTErrorUnableToCreate", transitName));
        }
        
        if(list.get(0) instanceof SignalMast){
            jmri.SignalMastLogicManager smlm = InstanceManager.signalMastLogicManagerInstance();
            for(int i = 1; i<=list.size()-1; i++){
                jmri.SignalMastLogic sml = smlm.getSignalMastLogic((SignalMast)list.get(i-1));
                Section sec = sml.getAssociatedSection((SignalMast)list.get(i));
                //In theory sec being null would already have been tested when the signal was added.
                if(sec==null){
                    String error = Bundle.getMessage("TCTErrorMastPairsNoSection", list.get(i-1).getDisplayName(), list.get(i).getDisplayName());
                    log.error(error);
                    tm.deregister(t);
                    t.dispose();
                    cancelTransitCreate();
                    throw new JmriException(error);
                }
                t.addTransitSection(new jmri.TransitSection(sec,i,Section.FORWARD));
            }
        }
        //Once created clear the list for a fresh start.
        list = new ArrayList<NamedBean>();
        return t;
    }
    
    public void cancelTransitCreate(){
        list = new ArrayList<NamedBean>();
    }
    
    public List<NamedBean> getBeans(){
        return list;
    }
    
    public boolean isToolInUse(){
        return !list.isEmpty();
    }
    
    static final Logger log = LoggerFactory.getLogger(TransitCreationTool.class.getName());
}