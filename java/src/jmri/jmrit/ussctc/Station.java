package jmri.jmrit.ussctc;

import jmri.*;

import java.util.*;

/**
 * A Station represents a specific codeline field station.
 * It defines the bits in the code message and holds references to the 
 * hardware at both ends that is controlled by those bits.
 * <ul>
 * <li>Two bits for Turnouts
 * <li>Three bits for Signals
 * <li>One bit for maintainer call, track circuits, etc
 * </ul>
 * The basic structure is to mate two objects that interact via a 
 * shared enum. Alternately, this can be a single object: e.g. a 
 * {@link TurnoutSection} that functions in both the CTC machine and field hardware roles.
 * <ul>
 * <li>The field object listens to the status of the layout and sends indications on changes.
 * <p>The CTC object responds to those indications.
 * <li>The CTC machine object sends when Code is pressed.
 * <p>The field object responds to those when received.
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class Station {

    public Station(CodeLine codeline, CodeButton button) {
        this.codeline = codeline;
        this.button = button;
    }
    
    CodeLine codeline;
    CodeButton button;
    
    /** 
     * @param section next Section subclass that makes up part of this Station
     * @return this Station to allow chaining
     */
    public Station add(Section section) {
        sections.add(section);
        return this;
    }

    /**
     * Tell the Sections to start a code-send operation (from machine to field).
     * Usually comes from a {@link CodeButton}
     */
    public void codeSendRequest() {
        sentValues = new ArrayList<>();
        sections.forEach((section) -> {
            // accumulate send values, which also sets indicators
            sentValues.add(section.codeSendStart());
        } );
        
        // TODO: check for locks on each section
        
        codeline.requestSendCode(this);
        
    }

    public void codeSendComplete() {
        log.debug("code sending complete, notify");
        
        // notify
        sections.forEach((section) -> {
            // accumulate send values, which also sets indicators
            sentValues.add(section.codeSendStart());
        } );
        
    }
    
    /**
     * Tell the sections that code information has arrived in the field
     */
    public void codeValueDelivered() {
        // clear the code light
        button.codeValueDelivered();
        
        // tell each section
        for (int i = 0; i < sections.size(); i++) {
            sections.get(i).codeValueDelivered(sentValues.get(i));
        }
    }   


    public void requestIndicationStart() {
        codeline.requestIndicationStart(this);
    }

    /**
     * Gather layout status and turn on code lamp.
     *  Rest of action is on indicationComplete
     */
    public void indicationStart() {
        log.debug("Station indicationStart");
        
        button.indicationStart();

        indicationValues = new ArrayList<>();
        sections.forEach((section) -> {
            // accumulate send values, which also sets indicators
            indicationValues.add(section.indicationStart());
        } );
    }

    /**
     * Gather layout status and turn on code lamp.
     *  Rest of action is on indicationComplete
     */
    public void indicationComplete() {
        log.debug("Station indicationComplete");
        // clear the code light
        button.codeValueDelivered();
        
        // tell each section
        for (int i = 0; i < sections.size(); i++) {
            sections.get(i).indicationComplete(indicationValues.get(i));
        }
    }

    ArrayList<Section> sections = new ArrayList<>();
    ArrayList<Value> sentValues;
    ArrayList<Value> indicationValues;
    
    enum Value {
        Single0, Single1,
        Double00, Double10, Double01,
        Triple000, Triple100, Triple010, Triple001
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Station.class.getName());
}
