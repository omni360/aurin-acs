package au.com.mutopia.acs.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Serves HTML for the index page.
 */
@Path("/")
public class MainResource {

  /** The URL to the source code repository. */
  private static final String REPO_URL = "https://bitbucket.org/mutopia/aurin-acs";

  /**
   * Serves the HTML for browsers on the index page.
   * 
   * @return A simple string explaining that the service doesn't have a GUI.
   */
  @GET
  public String main() {
    return "ACS does not have a user interface. For instructions, see the <a href=\"" + REPO_URL
        + "\">source code repository</a>.";
  }

}
