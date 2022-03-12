package jmri.jmrit.ussctc;

/**
 * A Section is the base type for the pieces that make up and are referenced by a {@link jmri.jmrit.ussctc.Station}.
 * It combines a {@link jmri.jmrit.ussctc.CentralSection} and a {@link jmri.jmrit.ussctc.FieldSection}
 * into one for convenience and consistency.
 * <p>
 * The type argument defines the communications from central to field and from field to central
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 * @param <To> communications from field to central.
 * @param <From> communications from central to field.
 */
public interface Section<To extends Enum<To>, From extends Enum<From>>
                 extends CentralSection<To, From>, FieldSection<To, From> {


    /**
     * Name of this Section.
     * Does not include name of associated Station.
     * @return section name without station.
    */
    public String getName();

    public Station<To, From> getStation();

}
