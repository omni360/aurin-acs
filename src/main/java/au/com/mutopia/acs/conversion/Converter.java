package au.com.mutopia.acs.conversion;

import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;

/**
 * Converts an {@link Asset} into a {@link C3mlEntity}.
 */
public interface Converter {

  public C3mlEntity convert(Asset asset);

}
