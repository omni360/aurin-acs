package au.com.mutopia.acs.service;

import lombok.extern.log4j.Log4j;
import au.com.mutopia.acs.resources.AssetsResource;
import au.com.mutopia.acs.resources.MainResource;

import com.fiestacabin.dropwizard.guice.AutoConfigService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;

/**
 * The main gateway service that provides the REST API for the Catalyst platform.
 */
@Log4j
public class AcsRestService extends AutoConfigService<Configuration> {

  public AcsRestService() {
    super("Catalyst REST Service", "com.deltarch.catalyst.core.rest");
  }

  /**
   * Starts the REST server.
   */
  public static void main(String[] args) throws Exception {
    log.info("Starting core service...");
    new AcsRestService().run(args);
  }

  /**
   * Creates the Guice dependency injector for the service using the {@link CatalystCoreModule}
   * configuration modules.
   *
   * @param configuration The service configuration information read out of the Dropwizard YAML
   *        file.
   */
  @Override
  protected Injector createInjector(Configuration configuration) {
    log.info("Creating core injector...");
    AbstractModule coreModule = new AcsModule();
    Injector injector = Guice.createInjector(coreModule);
    log.info("Configuring core injector...");
    log.info("Injector created and configured.");

    return injector;
  }

  /**
   * Runs the service using Guice to create the context.
   *
   * @see #run(AcsConfig, Environment)
   */
  @Override
  protected void runWithInjector(Configuration configuration, Environment environment,
      final Injector injector) throws Exception {
    super.runWithInjector(configuration, environment, injector);
    log.info("Running core service...");

    environment.addResource(MainResource.class);
    environment.addResource(AssetsResource.class);
    
    log.info("Core service running.");
  }

  /**
   * Initializes the services by adding service bundles to the service bootstrap.
   */
  @Override
  public void initialize(Bootstrap<Configuration> bootstrap) {
    log.info("Initializing core service...");
    bootstrap.setName("acs");
    log.info("Core service initialized.");
  }
}
