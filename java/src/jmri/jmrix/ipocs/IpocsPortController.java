package jmri.jmrix.ipocs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.AbstractPortController;
import jmri.jmrix.ipocs.protocol.Message;
import jmri.util.zeroconf.ZeroConfService;

public class IpocsPortController extends AbstractPortController implements IpocsClientListener {
  private final static Logger log = LoggerFactory.getLogger(IpocsPortController.class);
  private static String INADDR_ANY = "0.0.0.0";
  private AsynchronousServerSocketChannel serverSocket = null;
  private IpocsSocketAcceptor socketAcceptor;
  private ZeroConfService zeroConfService = null;
  private final List<IpocsClientListener> clientListeners = new ArrayList<IpocsClientListener>();
  private Map<String, IpocsClientHandler> clients = new HashMap<>();
  private Map<String, Message> lastMessage = new HashMap<>();

  public IpocsPortController() {
    super(new IpocsSystemConnectionMemo());
    super.setManufacturer(IpocsConnectionTypeList.IPOCSMR);
    final Option o1 = new Option("Listing port", new String[]{"10000"}, false, Option.Type.TEXT);
    super.options.put(super.option1Name, o1);
  }

  @Override
  public IpocsSystemConnectionMemo getSystemConnectionMemo() {
    return (IpocsSystemConnectionMemo) super.getSystemConnectionMemo();
  }

  @Override
  public void configure() {
    if (getSystemConnectionMemo().getPortController() == null) {
      getSystemConnectionMemo().setPortController(this);
    }
    getSystemConnectionMemo().configureManagers();
  }

  @Override
  public void connect() throws IOException {
    log.info("Setting up service");
    serverSocket = AsynchronousServerSocketChannel.open();
    socketAcceptor = new IpocsSocketAcceptor(this, serverSocket);
    final int port = Integer.parseInt(super.getOptionState(super.option1Name));
    final InetSocketAddress address = new InetSocketAddress(INADDR_ANY, port);
    serverSocket.bind(address);
    serverSocket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
    serverSocket.accept(null, socketAcceptor);
    log.info("Starting ZeroConfService _ipocs._tcp.local");
    zeroConfService = ZeroConfService.create("_ipocs._tcp.local.", "ipocs", 10000, 0, 0, new HashMap<String, String>());
    zeroConfService.publish();
  }

  @Override
  public DataInputStream getInputStream() {
    // This will never return anything, since this is a server port controller
    return null;
  }

  @Override
  public DataOutputStream getOutputStream() {
    // This will never return anything, since this is a server port controller
    return null;
  }

  @Override
  public String getCurrentPortName() {
    return "IPOCSMR";
  }
  
  public void addListener(IpocsClientListener clientListener) {
    clientListeners.add(clientListener);
  }

  public void removeListener(IpocsClientListener clientListener) {
    clientListeners.remove(clientListener);
  }

  @Override
  public void clientConnected(IpocsClientHandler client) {
    log.info("New client connected");
  }

  @Override
  public void clientDisconnected(IpocsClientHandler client) {
    clients.forEach((userName, storedClient) -> {
      if (storedClient == client) {
        clients.remove(userName);
        lastMessage.remove(userName);
        for (IpocsClientListener handler : clientListeners) {
          if (userName.equals(handler.getUserName())) {
            handler.clientDisconnected(client);
          }
        }
      }
    });
  }

  @Override
  public void onMessage(IpocsClientHandler client, Message msg) {
    clients.put(msg.getObjectName(), client);
    lastMessage.put(msg.getObjectName(), msg);
    for (IpocsClientListener handler : clientListeners) {
      if (handler.getUserName().equals(msg.getObjectName())) {
        handler.onMessage(client, msg);
      }
    }
  }

  public void send(Message msg) {
    IpocsClientHandler client = clients.get(msg.getObjectName());
    if (client != null) {
      client.send(msg);
    }
  }

  public Message getLastStatus(String userName) {
    return lastMessage.get(userName);
  }
}
