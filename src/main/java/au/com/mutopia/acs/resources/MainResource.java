package au.com.mutopia.acs.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Serves HTML for the index page.
 */
@Path("/")
@Produces(MediaType.TEXT_HTML)
public class MainResource {

  /** The URL to the source code repository. */
  private static final String REPO_URL = "https://bitbucket.org/mutopia/aurin-acs";

  /**
   * Serves the HTML for browsers on the index page.
   */
  @GET
  public String main() {
    return "ACS does not have a user interface. For instructions, see the <a href=\"" + REPO_URL
        + "\">source code repository</a>.";
  }

}
