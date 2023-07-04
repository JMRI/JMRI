package jmri.jmrit.signalsystemeditor;

import java.util.ArrayList;
import java.util.List;

/**
 * An aspect
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class Aspect {

    private final StringWithComment _name;
    private String _title;
    private String _rule;
    private String _indication;
    private final List<String> _descriptions = new ArrayList<>();
    private final List<String> _references = new ArrayList<>();
    private final List<String> _comments = new ArrayList<>();
    private final List<String> _speedList = new ArrayList<>();
    private final List<String> _speed2List = new ArrayList<>();
    private String _route;
    private String _dccAspect;

    public Aspect(StringWithComment name, String title, String rule, String indication, String route, String dccAspect) {
        this._name = name;
        this._title = title;
        this._rule = rule;
        this._indication = indication;
        this._route = route;
        this._dccAspect = dccAspect;
    }

    public StringWithComment getName() {
        return this._name;
    }

    public void setTitle(String title) {
        this._title = title;
    }

    public String getTitle() {
        return this._title;
    }

    public void setRule(String rule) {
        this._rule = rule;
    }

    public String getRule() {
        return this._rule;
    }

    public void setIndication(String indication) {
        this._indication = indication;
    }

    public String getIndication() {
        return this._indication;
    }

    public List<String> getDescriptions() {
        return this._descriptions;
    }

    public List<String> getReferences() {
        return this._references;
    }

    public List<String> getComments() {
        return this._comments;
    }

    public List<String> getSpeedList() {
        return this._speedList;
    }

    public List<String> getSpeed2List() {
        return this._speed2List;
    }

    public void setRoute(String route) {
        this._route = route;
    }

    public String getRoute() {
        return this._route;
    }

    public void setDccAspect(String dccAspect) {
        this._dccAspect = dccAspect;
    }

    public String getDccAspect() {
        return this._dccAspect;
    }

}
