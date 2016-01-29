package apps.startup;

import apps.StartupModel;

/**
 * Abstract startup action model. 
 * 
 * @author Randall Wood (c) 2016
 */
abstract class AbstractStartupModel implements StartupModel {

    private String name;
    
    protected AbstractStartupModel() {
        this.name = null;
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
    
}
