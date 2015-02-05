package au.com.mutopia.acs.models.c3ml;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests the {@link C3mlEntity} class and its serialization to JSON.
 */
public class C3mlEntityTest {

  /**
   * Tests that Jackson serializes the childrenIds field instead of the actual children field.
   */
  @Test
  public void testJson_recursive() throws Exception {
    C3mlEntity entity = new C3mlEntity();
    entity.setId("foo");
    entity.addChild(entity);
    String json = new ObjectMapper().writeValueAsString(entity);
    assertThat(json).contains("\"children\":[\"foo\"]");
  }

}
