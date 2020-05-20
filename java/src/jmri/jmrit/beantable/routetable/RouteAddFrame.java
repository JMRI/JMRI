package jmri.jmrit.beantable.routetable;

public class RouteAddFrame extends AbstractRouteAddEditFrame {


    public RouteAddFrame() {
        this(Bundle.getMessage("TitleAddRoute"));
    }

    public RouteAddFrame(String name) {
        this(name,false,true);
    }

    public RouteAddFrame(String name, boolean saveSize, boolean savePosition) {
        super(name, saveSize, savePosition);
        initComponents();
    }


}
