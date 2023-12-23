package jmri.jmrit.logixng;

import jmri.*;
import jmri.implementation.VirtualSignalHead;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.jmrit.entryexit.PointDetails;
import jmri.jmrit.entryexit.Source;

/**
 * Classes for testing transits
 * @author Daniel Bergqvist (C) 2023
 */
public class TransitScaffold {

    public static void initTransits() {
        InstanceManager.setDefault(EntryExitPairs.class, new MyEntryExitPairs());
    }

    public static class MyDestinationPoints extends DestinationPoints {
        MyDestinationPoints(PointDetails point, String id, Source src) {
            super(point, id, src);
        }

        @Override
        public void setActiveEntryExit(boolean boo) {
            super.setActiveEntryExit(boo);
        }
    }

    private static class MyPointDetails extends PointDetails {
        MyPointDetails() {
            super(null, null);
        }

        @Override
        public String getDisplayName() {
            return "DisplayName";
        }

        @Override
        public NamedBean getRefObject() {
            return InstanceManager.getDefault(SignalHeadManager.class)
                    .getBySystemName("IHTransitScaffold");
        }
    }

    private static class MyEntryExitPairs extends EntryExitPairs {
        final PointDetails point;
        final Source src;
        final MyDestinationPoints dp1;
        final MyDestinationPoints dp2;
        final MyDestinationPoints myBeanEntryExit;

        public MyEntryExitPairs() {
            InstanceManager.getDefault(SignalHeadManager.class)
                    .register(new VirtualSignalHead("IHTransitScaffold"));
            point = new MyPointDetails();
            src = new Source(point);
            dp1 = new MyDestinationPoints(new MyPointDetails(), "DP1", src);
            dp1.setUserName("Destination point 1");
            dp2 = new MyDestinationPoints(new MyPointDetails(), "DP2", src);
            dp2.setUserName("Destination point 2");
            myBeanEntryExit = new MyDestinationPoints(new MyPointDetails(), "MyBeanEntryExit", src);
        }

        @Override
        public DestinationPoints getBySystemName(String systemName) {
            switch (systemName) {
                case "DP1":
                    return dp1;
                case "DP2":
                    return dp2;
                case "MyBeanEntryExit":
                    return myBeanEntryExit;
                default:
                    throw new IllegalArgumentException("Unknown system name: "+systemName);
            }
        }

        @Override
        public DestinationPoints getNamedBean(String name) {
            switch (name) {
                case "Destination point 1":
                    return dp1;
                case "Destination point 2":
                    return dp2;
                default:
                    return getBySystemName(name);
            }
        }
    }

}
