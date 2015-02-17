package au.com.mutopia.acs.conversion.impl;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;

import au.com.mutopia.acs.conversion.output.KmzWriter;
import au.com.mutopia.acs.models.c3ml.C3mlData;
import au.com.mutopia.acs.util.ZipUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests that C3ML can be converted into KMZ.
 */
public class KmzWriterTest {

  private KmzWriter writer;

  @Before
  public void setUp() {
    writer = new KmzWriter();
  }

  @Test
  public void test() throws Exception {
    InputStream fixture = Class.class.getResourceAsStream("/fixtures/c3ml/typology.c3ml");
    C3mlData data = new ObjectMapper().readValue(fixture, C3mlData.class);
    File outFile = writer.convert(data);

    assertThat(FilenameUtils.getExtension(outFile.getName())).isEqualTo("kmz");
    List<File> unzipped = ZipUtils.unzipToTempDirectory(outFile);
    // TODO(orlade): Set to two when COLLADA is saved.
    assertThat(unzipped).hasSize(1);
  }

}
