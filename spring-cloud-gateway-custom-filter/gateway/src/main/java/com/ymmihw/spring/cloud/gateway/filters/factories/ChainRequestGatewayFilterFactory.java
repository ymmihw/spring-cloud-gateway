package com.ymmihw.spring.cloud.gateway.filters.factories;

import java.util.Arrays;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.stream.Collectors;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ChainRequestGatewayFilterFactory
    extends AbstractGatewayFilterFactory<ChainRequestGatewayFilterFactory.Config> {

  private final WebClient client;

  public ChainRequestGatewayFilterFactory(WebClient client) {
    super(Config.class);
    this.client = client;
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return Arrays.asList("languageServiceEndpoint", "defaultLanguage");
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      return client.get().uri(config.getLanguageServiceEndpoint()).exchange().flatMap(response -> {
        return (response.statusCode().is2xxSuccessful()) ? response.bodyToMono(String.class)
            : Mono.just(config.getDefaultLanguage());
      }).map(LanguageRange::parse).map(range -> {
        exchange.getRequest().mutate().headers(h -> h.setAcceptLanguage(range));

        String allOutgoingRequestLanguages = exchange.getRequest().getHeaders().getAcceptLanguage()
            .stream().map(r -> r.getRange()).collect(Collectors.joining(","));

        log.info("Chain Request output - Request contains Accept-Language header: "
            + allOutgoingRequestLanguages);

        return exchange;
      }).flatMap(chain::filter);

    };
  }

  @Getter
  @Setter
  public static class Config {
    private String languageServiceEndpoint;
    private String defaultLanguage;
  }
}
