package au.com.mutopia.acs.conversion.impl;

import java.util.List;

import lombok.extern.log4j.Log4j;
import au.com.mutopia.acs.conversion.Converter;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;

/**
 * Converts IFC files into a collection of {@link C3mlEntity} objects.
 */
@Log4j
public class IfcConverter extends AbstractConverter {

  public List<C3mlEntity> convert(Asset asset, boolean merge) {
    log.debug("Converting IFC asset " + asset.getName() + "...");
    // TODO Auto-generated method stub
    return null;
  }

}
