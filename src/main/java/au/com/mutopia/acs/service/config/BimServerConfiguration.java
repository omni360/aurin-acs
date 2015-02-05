package au.com.mutopia.acs.service.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration details for the companion BIMserver instance.
 */
@Getter
@Setter
public class BimServerConfiguration {

  @Valid
  @NotNull
  @JsonProperty
  private String host;

  @Valid
  @NotNull
  @JsonProperty
  private String username;

  @Valid
  @NotNull
  @JsonProperty
  private String password;

}
