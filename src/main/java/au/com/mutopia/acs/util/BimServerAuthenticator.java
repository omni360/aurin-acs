package au.com.mutopia.acs.util;

import lombok.extern.log4j.Log4j;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.BimServerClientFactory;
import org.bimserver.client.ChannelConnectionException;
import org.bimserver.client.json.JsonBimServerClientFactory;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.ServiceException;

/**
 * Handles opening connections to BIMserver.
 */
@Log4j
public class BimServerAuthenticator {

  private final String username;
  private final String password;
  private final String host;

  public BimServerAuthenticator(String host, String username, String password) {
    this.host = host;
    this.username = username;
    this.password = password;
  }

  /**
   * Opens a connection to the BIMserver instance using the given details.
   * 
   * @param username The username to login with.
   * @param password The password to login with.
   * @param host The host address of the BIMserver.
   * @return {@link BimServerClient} that is connected to BimServer at given host address, username
   *         and password.
   */
  public BimServerClient connectToBimServer() {
    log.debug(String.format("Connecting to BIMserver at %s@%s...", username, host));
    BimServerClientFactory factory = new JsonBimServerClientFactory(host);
    UsernamePasswordAuthenticationInfo authInfo =
        new UsernamePasswordAuthenticationInfo(username, password);

    try {
      return factory.create(authInfo);
    } catch (ServiceException | ChannelConnectionException e) {
      log.error("Failed to create BIMserver client", e);
      return null;
    }
  }

}
