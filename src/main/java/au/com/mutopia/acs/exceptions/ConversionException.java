package au.com.mutopia.acs.exceptions;

import java.io.IOException;

@SuppressWarnings("serial")
public class ConversionException extends IOException {

  public ConversionException(String message) {
    super(message);
  }

  public ConversionException(String message, Throwable cause) {
    super(message, cause);
  }

}
