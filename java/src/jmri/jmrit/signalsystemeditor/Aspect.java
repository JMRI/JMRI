package jmri.jmrit.signalsystemeditor;

import java.util.ArrayList;
import java.util.List;

/**
 * An aspect
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class Aspect {

    private String _name;
    private String _title;
    private String _rule;
    private String _indication;
    private final List<String> _descriptionList = new ArrayList<>();
    private final List<String> _referenceList = new ArrayList<>();
    private final List<String> _commentList = new ArrayList<>();
    private final List<String> _speedList = new ArrayList<>();
    private final List<String> _speed2List = new ArrayList<>();
    private String _route;
    private String _dccAspect;

    public Aspect(String name, String title, String rule, String indication, String route, String dccAspect) {
        this._name = name;
        this._title = title;
        this._rule = rule;
        this._indication = indication;
        this._route = route;
        this._dccAspect = dccAspect;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getName() {
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

    public List<String> getDescriptionList() {
        return this._descriptionList;
    }

    public List<String> getReferenceList() {
        return this._referenceList;
    }

    public List<String> getCommentList() {
        return this._commentList;
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
