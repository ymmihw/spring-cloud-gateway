package com.ymmihw.spring.cloud.gateway.filters.factories;

import java.util.Arrays;
import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingGatewayFilterFactory
    extends AbstractGatewayFilterFactory<LoggingGatewayFilterFactory.Config> {

  public static final String BASE_MSG = "baseMessage";
  public static final String PRE_LOGGER = "preLogger";
  public static final String POST_LOGGER = "postLogger";

  public LoggingGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return Arrays.asList(BASE_MSG, PRE_LOGGER, POST_LOGGER);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return new OrderedGatewayFilter((exchange, chain) -> {
      if (config.isPreLogger())
        log.info("Pre GatewayFilter logging: " + config.getBaseMessage());
      return chain.filter(exchange).then(Mono.fromRunnable(() -> {
        if (config.isPostLogger())
          log.info("Post GatewayFilter logging: " + config.getBaseMessage());
      }));
    }, 1);
  }

  @Getter
  public static class Config {
    private String baseMessage;
    private boolean preLogger;
    private boolean postLogger;

    public Config(String baseMessage, boolean preLogger, boolean postLogger) {
      super();
      this.baseMessage = baseMessage;
      this.preLogger = preLogger;
      this.postLogger = postLogger;
    }
  }
}
