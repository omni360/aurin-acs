package au.com.mutopia.acs.conversion.impl;

import java.io.IOException;
import java.util.List;

import lombok.extern.log4j.Log4j;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import au.com.mutopia.acs.util.BimServerAuthenticator;
import au.com.mutopia.acs.util.IfcExtractor;

/**
 * Converts IFC files into a collection of {@link C3mlEntity} objects.
 */
@Log4j
public class IfcConverter extends AbstractConverter {

  private BimServerAuthenticator auth;

  public IfcConverter(BimServerAuthenticator auth) {
    this.auth = auth;
  }

  public List<C3mlEntity> convert(Asset asset, boolean merge) throws ConversionException {
    log.debug("Converting IFC asset " + asset + "...");
    // TODO Auto-generated method stub
    try {
      new IfcExtractor(auth).extractJson(asset.getTemporaryFile());
    } catch (IOException e) {
      throw new ConversionException("Failed to convert IFC asset " + asset + " as file", e);
    }
    return null;
  }

}
