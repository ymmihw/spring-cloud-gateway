package com.ymmihw.spring.cloud.gateway.filters.factories;

import java.util.Optional;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ModifyResponseGatewayFilterFactory
    extends AbstractGatewayFilterFactory<ModifyResponseGatewayFilterFactory.Config> {

  public ModifyResponseGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      return chain.filter(exchange).then(Mono.fromRunnable(() -> {
        ServerHttpResponse response = exchange.getResponse();

        Optional.ofNullable(exchange.getRequest().getQueryParams().getFirst("locale"))
            .ifPresent(qp -> {
              String responseContentLanguage =
                  response.getHeaders().getContentLanguage().getLanguage();

              response.getHeaders().add("Bael-Custom-Language-Header", responseContentLanguage);
              log.info("Added custom header to Response");
            });
      }));
    };
  }

  public static class Config {
  }
}
