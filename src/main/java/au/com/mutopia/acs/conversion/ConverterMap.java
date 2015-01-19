package au.com.mutopia.acs.conversion;

import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.Format;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;

/**
 * A map of each supported file format to the {@link Converter} used to convert {@link Asset}s of
 * that format into a {@link C3mlEntity}.
 */
public interface ConverterMap {

  /**
   * Returns the {@link Converter} for the given format.
   * 
   * @param format The format to get the {@link Converter} for.
   * @return A {@link Converter} that can convert files of the given format.
   */
  public Converter get(Format format);

}
