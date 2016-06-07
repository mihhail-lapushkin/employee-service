package application.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfiguration {

  @Bean
  public FilterRegistrationBean corsFilter() {
      UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
      org.springframework.web.cors.CorsConfiguration springCorsConfiguration = new org.springframework.web.cors.CorsConfiguration();
      springCorsConfiguration.setAllowCredentials(true);
      springCorsConfiguration.addAllowedOrigin("*");
      springCorsConfiguration.addAllowedHeader("*");
      springCorsConfiguration.addAllowedMethod("OPTIONS");
      springCorsConfiguration.addAllowedMethod("HEAD");
      springCorsConfiguration.addAllowedMethod("GET");
      springCorsConfiguration.addAllowedMethod("PUT");
      springCorsConfiguration.addAllowedMethod("POST");
      springCorsConfiguration.addAllowedMethod("DELETE");
      springCorsConfiguration.addAllowedMethod("PATCH");
      corsConfigurationSource.registerCorsConfiguration("/**", springCorsConfiguration);
      FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new CorsFilter(corsConfigurationSource));
      filterRegistrationBean.setOrder(0);
      return filterRegistrationBean;
  }
}
