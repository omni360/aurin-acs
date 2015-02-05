package au.com.mutopia.acs.conversion.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j;
import au.com.mutopia.acs.exceptions.ConversionException;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Converts a C3ML file into a collection of {@link C3mlEntity} objects.
 */
@Log4j
public class C3mlConverter extends AbstractConverter {

  public List<C3mlEntity> convert(Asset asset, boolean merge) throws ConversionException {
    log.debug("Converting C3ML asset " + asset + "...");
    try {
      return new ObjectMapper().readValue(new String(asset.getData()), C3mlData.class).getC3mls();
    } catch (IOException e) {
      throw new ConversionException("Failed to convert C3ML asset " + asset, e);
    }
  }

}
