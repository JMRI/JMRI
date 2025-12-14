package jmri.time.rate.configurexml;

import jmri.time.Rate;
import jmri.time.rate.ChangeableDoubleRate;

import org.jdom2.Element;

/**
 * Store and load an ChangeableDoubleRate.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class ChangeableDoubleRateXml {

    public static void store(Rate rate, Element element) {
        if (!(rate instanceof ChangeableDoubleRate)) {
            String clazz = rate != null ? rate.getClass().getName() : "null";
            throw new IllegalArgumentException("rate is not a ChangeableDoubleRate: " + clazz);
        }

        element.addContent(new Element("rate").addContent(Double.toString(rate.getRate())));
    }

    public static Rate load(Rate rate, Element element) {
        return null;
    }

}
