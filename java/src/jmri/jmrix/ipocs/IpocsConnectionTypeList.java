package jmri.jmrix.ipocs;

@org.openide.util.lookup.ServiceProvider(service = jmri.jmrix.ConnectionTypeList.class)
public class IpocsConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

  public final static String IPOCSMR = "IPOCSMR";

  public IpocsConnectionTypeList() {
    super();
  }

  @Override
  public String[] getAvailableProtocolClasses() {
    return new String[] {
      IpocsConnectionConfig.class.getName(),
    };
  }

  @Override
  public String[] getManufacturers() {
    return new String[] { IPOCSMR };
  }
}
