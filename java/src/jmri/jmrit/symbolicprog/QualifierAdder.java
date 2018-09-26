package jmri.jmrit.symbolicprog;

import java.util.ArrayList;
import java.util.List;
import org.jdom2.Element;
import org.jdom2.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for adding qualifiers to objects
 *
 * @see jmri.jmrit.symbolicprog.Qualifier
 * @see jmri.jmrit.symbolicprog.ArithmeticQualifier
 * @see jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame
 *
 * @author Bob Jacobsen Copyright (C) 2014
 *
 */
public abstract class QualifierAdder {

    /**
     * Invoked to create the qualifier object and connect as needed. If extra
     * state is needed, provide it via the subclass constructor.
     *
     * @param var      The variable that qualifies this, e.g. the one that's
     *                 watched
     * @param relation The relation term from the qualifier definition, e.g.
     *                 greater than
     * @param value    The value for the comparison
     */
    // e.g. return new PaneQualifier(pane, var, Integer.parseInt(value), relation, tabPane, index);
    abstract protected Qualifier createQualifier(VariableValue var, String relation, String value);

    // e.g. arrange for this to be sent a property change event on change of the qualified object
    abstract protected void addListener(java.beans.PropertyChangeListener qc);

    public void processModifierElements(Element e, VariableTableModel model) {

        ArrayList<Qualifier> lq = new ArrayList<Qualifier>();

        List<Element> le = e.getChildren("qualifier"); // we assign to this to allow us to suppress unchecked error
        processList(le, lq, model);

        // search for enclosing element so we can find all relevant qualifiers
        Parent p = e;
        while ((p = p.getParent()) != null && p instanceof Element) {
            Element el = (Element) p;
            if (el.getName().equals("pane")) {
                break;  // stop when we get to an enclosing pane element
            }
            List<Element> le2 = el.getChildren("qualifier");  // we assign to this to allow us to suppress unchecked error
            processList(le2, lq, model);
        }

        // Add the AND logic - listen for change and ensure result correct
        if (lq.size() > 1) {
            QualifierCombiner qc = new QualifierCombiner(lq);
            addListener(qc);
        }
    }

    void processList(List<Element> le, ArrayList<Qualifier> lq, VariableTableModel model) {
        for (Element q : le) {
            processElement(q, lq, model);
        }
    }

    void processElement(Element q, ArrayList<Qualifier> lq, VariableTableModel model) {
        String variableRef = q.getChild("variableref").getText();
        String relation = q.getChild("relation").getText();
        String value = q.getChild("value").getText();

        // find the variable
        VariableValue var = model.findVar(variableRef);

        if (var != null || relation.equals("exists")) {
            // found, attach the qualifier object through creating it
            log.debug("Attached {} variable for {} {} qualifier", variableRef, relation, value);
        } else {
            log.debug("Didn't find {} variable for {} {} qualifier", variableRef, relation, value);
        }

        // create qualifier
        Qualifier qual = createQualifier(var, relation, value);
        qual.update();
        lq.add(qual);
    }

    private final static Logger log = LoggerFactory.getLogger(QualifierAdder.class);

}
