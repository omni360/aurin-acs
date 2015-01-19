package au.com.mutopia.acs.exceptions;

/**
 * An exception to be thrown when attempting to convert an invalid COLLADA file.
 */
@SuppressWarnings("serial")
public class InvalidColladaException extends Throwable {

  /**
   * Creates the exception with a description of why the COLLADA was invalid.
   * 
   * @param message An explanation of the error.
   */
  public InvalidColladaException(String message) {
    super(message);
  }

  /**
   * Creates the exception with a description of why the COLLADA was invalid and the cause.
   * 
   * @param message An explanation of the error.
   * @param cause The error that caused this error.
   */
  public InvalidColladaException(String message, Throwable cause) {
    super(message, cause);
  }

}
