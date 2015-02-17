package au.com.mutopia.acs.conversion.impl;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import au.com.mutopia.acs.models.c3ml.C3mlData;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    writer.convert(data);
  }

}
