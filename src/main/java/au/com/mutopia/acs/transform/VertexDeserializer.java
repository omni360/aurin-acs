package au.com.mutopia.acs.transform;

import java.io.IOException;
import java.util.List;

import au.com.mutopia.acs.models.c3ml.Vertex3D;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;

/**
 * Deserializes a 3-tuple of doubles into a {@link Vertex3D}.
 */
public class VertexDeserializer extends JsonDeserializer<Vertex3D> {

  @Override
  public Vertex3D deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
      JsonProcessingException {
    JsonNode node = jp.getCodec().readTree(jp);
    List<Double> coords =
        ImmutableList.of(node.get(0).asDouble(), node.get(1).asDouble(), node.get(2).asDouble());
    return new Vertex3D(coords, true);
  }

}
