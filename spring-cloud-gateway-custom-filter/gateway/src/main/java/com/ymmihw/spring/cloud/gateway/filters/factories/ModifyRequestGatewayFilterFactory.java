package com.ymmihw.spring.cloud.gateway.filters.factories;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ModifyRequestGatewayFilterFactory
    extends AbstractGatewayFilterFactory<ModifyRequestGatewayFilterFactory.Config> {

  public ModifyRequestGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return Arrays.asList("defaultLocale");
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      if (exchange.getRequest().getHeaders().getAcceptLanguage().isEmpty()) {

        String queryParamLocale = exchange.getRequest().getQueryParams().getFirst("locale");

        Locale requestLocale = Optional.ofNullable(queryParamLocale)
            .map(l -> Locale.forLanguageTag(l)).orElse(config.getDefaultLocale());

        exchange.getRequest().mutate()
            .headers(h -> h.setAcceptLanguageAsLocales(Collections.singletonList(requestLocale)));
      }

      String allOutgoingRequestLanguages = exchange.getRequest().getHeaders().getAcceptLanguage()
          .stream().map(range -> range.getRange()).collect(Collectors.joining(","));

      log.info("Modify request output - Request contains Accept-Language header: {}",
          allOutgoingRequestLanguages);

      ServerWebExchange modifiedExchange =
          exchange.mutate()
              .request(originalRequest -> originalRequest.uri(UriComponentsBuilder
                  .fromUri(exchange.getRequest().getURI())
                  .replaceQueryParams(new LinkedMultiValueMap<String, String>()).build().toUri()))
              .build();

      log.info("Removed all query params: {}", modifiedExchange.getRequest().getURI());

      return chain.filter(modifiedExchange);
    };
  }

  @Getter
  public static class Config {
    private Locale defaultLocale;

    public void setDefaultLocale(String defaultLocale) {
      this.defaultLocale = Locale.forLanguageTag(defaultLocale);
    };
  }
}
