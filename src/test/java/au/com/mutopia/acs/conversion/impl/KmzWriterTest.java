package au.com.mutopia.acs.conversion.impl;

import static org.fest.assertions.api.Assertions.assertThat;

import au.com.mutopia.acs.conversion.AssetTest;
import au.com.mutopia.acs.conversion.BroadC3mlFixture;
import au.com.mutopia.acs.models.Asset;
import au.com.mutopia.acs.models.Format;
import au.com.mutopia.acs.models.c3ml.C3mlEntity;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import au.com.mutopia.acs.conversion.output.KmzWriter;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.util.ZipUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests that C3ML can be converted into KMZ.
 */
public class KmzWriterTest extends AssetTest {

  private KmzWriter writer;

  /** The expected {@link C3mlData} structure of the broad test fixture. */
  protected C3mlData BROAD_DATA = new BroadC3mlFixture();

  @Before
  public void setUp() {
    writer = new KmzWriter();
  }

  @Ignore("Error in C3ml file.")
  @Test
  public void testTypologyExportToKmz() throws Exception {
    InputStream fixture = Class.class.getResourceAsStream("/fixtures/c3ml/typology.c3ml");
    C3mlData data = new ObjectMapper().readValue(fixture, C3mlData.class);

    File outFile = writer.convert(data);

    assertThat(FilenameUtils.getExtension(outFile.getName())).isEqualTo("kmz");
    List<File> unzipped = ZipUtils.unzipToTempDirectory(outFile);
    assertThat(unzipped).hasSize(2);
  }

  @Test
  public void testKmzImportAndExport() throws Exception {
    Asset asset =
        createResourceAsset("/fixtures/" + getResourceFolder() + "/broad." + getExtension());
    KmlConverter kmlConverter = new KmlConverter();
    KmzConverter kmzConverter = new KmzConverter(kmlConverter);
    List<C3mlEntity> entities = kmzConverter.convert(asset);
    C3mlData data = new C3mlData(entities);
    File convertedKmzFile = writer.convert(data);
    Asset convertedKmzAsset = new Asset(convertedKmzFile);
    List<C3mlEntity> convertedEntities = kmzConverter.convert(convertedKmzAsset);
    C3mlData convertedData = new C3mlData(convertedEntities);
    assertThatC3mlBroadDataIsEqual(convertedData, BROAD_DATA);
  }

  @Override
  protected String getResourceFolder() {
    return Format.KMZ.toString();
  }

  @Override
  protected String getExtension() {
    return Format.KMZ.toString();
  }
}
