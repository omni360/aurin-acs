package au.com.mutopia.acs.exceptions;

/**
 * An exception to be thrown when attempting to convert an invalid COLLADA file.
 */
@SuppressWarnings("serial")
public class InvalidColladaException extends Throwable {

  public InvalidColladaException(String message) {
    super(message);
  }

  public InvalidColladaException(String message, Throwable cause) {
    super(message, cause);
  }

}
