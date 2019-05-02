package jmri.jmrit.ussctc;


import java.util.*;

/**
 * A Station represents a specific codeline field station.
 * It defines the bits in the code message and holds references to the 
 * hardware at both ends that is controlled by those bits. For example:
 * <ul>
 * <li>Two bits for Turnouts, see {@link CodeGroupTwoBits}
 * <li>Three bits for Signals, see {@link CodeGroupThreeBits}
 * <li>One bit for maintainer call, track circuits, etc, see {@link CodeGroupOneBit}
 * </ul>
 * The basic structure is to mate two objects that interact via a 
 * shared enum. Alternately, this can be a single object: e.g. a 
 * {@link TurnoutSection} that functions in both the central CTC machine and field hardware roles.
 * <ul>
 * <li>The field object listens to the status of the layout and sends indications on changes.
 * <p>The central (CTC machine) object responds to those indications.
 * <li>The central (CTC machine) object sends when Code is pressed.
 * <p>The field object responds to those when received.
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class Station {

    public Station(String name, CodeLine codeline, CodeButton button) {
        this.name = name;
        this.codeline = codeline;
        this.button = button;
        
        button.addStation(this);  // register with Codebutton
    }
    
    String name;
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
     * Provide access to CodeLine to which this Station is attached.
     */
    CodeLine getCodeLine() { return codeline; }
    
    /**
     * Provide access this Station's name
     */
    String getName() { return name; }
    
    @Override
    public String toString() { return "Station "+name; }
    
    /**
     * Tell the Sections to start a code-send operation (from machine to field).
     * Usually comes from a {@link CodeButton}
     */
    public void codeSendRequest() {
        log.debug("Station - start codeSendRequest");
        sentValues = new ArrayList<>();
        sections.forEach((section) -> {
            // accumulate send values, which also sets indicators
            sentValues.add(section.codeSendStart());
        } );
        
        // TODO: check for locks on each section
        
        codeline.requestSendCode(this);
        log.debug("Station - end codeSendRequest");
        
    }

    public void codeSendComplete() {
        log.debug("Station - start codeSendComplete");
        
        // notify
        sections.forEach((section) -> {
            // accumulate send values, which also sets indicators
            sentValues.add(section.codeSendStart());
        } );
        
        log.debug("Station - end codeSendComplete");
    }
    
    /**
     * Tell the sections that code information has arrived in the field
     */
    @SuppressWarnings("unchecked") // we store multiple enum types for codeValueDelivered
    public void codeValueDelivered() {
        log.debug("Station - start codeValueDelivered");
        // clear the code light
        button.codeValueDelivered();
        
        // tell each section
        for (int i = 0; i < sections.size(); i++) {
            sections.get(i).codeValueDelivered(sentValues.get(i));
        }
        log.debug("Station - end codeValueDelivered");
    }   


    public void requestIndicationStart() {
        log.debug("Station - start requestIndicationStart");
        codeline.requestIndicationStart(this);
        log.debug("Station - end requestIndicationStart");
    }

    /**
     * Gather layout status and turn on code lamp.
     *  Rest of action is on indicationComplete
     */
    public void indicationStart() {
        log.debug("Station - start indicationStart");
        
        button.indicationStart();

        indicationValues = new ArrayList<>();
        sections.forEach((section) -> {
            // accumulate send values, which also sets indicators
            indicationValues.add(section.indicationStart());
        } );
        log.debug("Station - end indicationStart");
    }

    /**
     * Gather layout status and turn on code lamp.
     *  Rest of action is on indicationComplete
     */
    @SuppressWarnings("unchecked") // we store multiple enum types for codeValueDelivered
    public void indicationComplete() {
        log.debug("Station - start indicationComplete");
        
        // tell each section
        for (int i = 0; i < sections.size(); i++) {
            sections.get(i).indicationComplete(indicationValues.get(i));
        }
        // clear the code light
        button.indicationComplete();

        log.debug("Station - end indicationComplete");
    }

    ArrayList<Section> sections = new ArrayList<>();
    ArrayList<Enum> sentValues;         // type is constrained in generic arguments to Section
    ArrayList<Enum> indicationValues;   // type is constrained in generic arguments to Section
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Station.class);
}
