package au.com.mutopia.acs.exceptions;

import java.io.IOException;

/**
 * Indicates that a conversion operation has failed.
 */
@SuppressWarnings("serial")
public class ConversionException extends IOException {

  /**
   * Creates the exception with an explanation of the problem.
   * 
   * @param message The explanation of the problem.
   */
  public ConversionException(String message) {
    super(message);
  }

  /**
   * Creates the exception with an explanation of the problem and the error that caused it.
   * 
   * @param message The explanation of the problem.
   * @param cause The error that caused this error.
   */
  public ConversionException(String message, Throwable cause) {
    super(message, cause);
  }

}
