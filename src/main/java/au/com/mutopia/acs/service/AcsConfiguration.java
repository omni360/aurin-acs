package au.com.mutopia.acs.service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import au.com.mutopia.acs.service.config.BimServerConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

/**
 * Configuration template for the ACS instance.
 */
@Getter @Setter
public class AcsConfiguration extends Configuration {

  @Valid
  @NotNull
  @JsonProperty
  private BimServerConfiguration bimserver = new BimServerConfiguration();

}
