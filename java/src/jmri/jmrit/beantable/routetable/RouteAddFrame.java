package jmri.jmrit.beantable.routetable;

/**
 * Add frame for the Route Table.
 *
 * Split from {@link jmri.jmrit.beantable.RouteTableAction}
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse Copyright (C) 2016
 * @author Paul Bender Colyright (C) 2020
 */
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
