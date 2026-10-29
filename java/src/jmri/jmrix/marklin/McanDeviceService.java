package jmri.jmrix.marklin;

/**
 * Marker interface for type-specific services attached to an {@link McanDevice}.
 * <p>
 * A service encapsulates all state and logic that only makes sense for one particular
 * device class (e.g. WeichenChef Magnet generation, Reporter occupancy channels).
 * {@code McanDevice} itself stays generic and never imports device-class-specific types.
 * <p>
 * The classifier layer (e.g. {@link McanDeviceManager}) instantiates and attaches the
 * appropriate service after discovery by calling {@link McanDevice#setDeviceService(McanDeviceService)}.
 * <p>
 * Future device-class services that should be wired automatically include:
 * <ul>
 *   <li>{@link WeichenChefKombiParameterService}  -  class 0x27</li>
 *   <li>{@link ReporterParameterService}  -  classes 0x0D, 0x0B</li>
 * </ul>
 * This interface is intentionally kept free of methods so that heterogeneous service
 * types can be stored in the same typed slot without imposing a lowest-common-denominator
 * API. Callers cast to the concrete type after checking with {@code instanceof}.
 * <p>
 * If CS2-native device support is added in the future (e.g. {@code Cs2DeviceService}),
 * those types would also implement this interface and be stored in the same slot.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public interface McanDeviceService {
}
