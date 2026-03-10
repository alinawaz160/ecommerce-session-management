package com.ecommerce.cart.config;

import de.neuland.pug4j.PugConfiguration;
import de.neuland.pug4j.spring.template.SpringTemplateLoader;
import de.neuland.pug4j.spring.view.PugViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Pug4jConfig {

    @Bean
    public SpringTemplateLoader templateLoader() {
        SpringTemplateLoader loader = new SpringTemplateLoader();
        loader.setBase("classpath:/templates/");
        loader.setSuffix(".pug");
        return loader;
    }

    @Bean
    public PugConfiguration pugConfiguration() {
        PugConfiguration config = new PugConfiguration();
        config.setTemplateLoader(templateLoader());
        config.setCaching(false);
        return config;
    }

    @Bean
    public PugViewResolver pugViewResolver() {
        PugViewResolver resolver = new PugViewResolver();
        resolver.setConfiguration(pugConfiguration());
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".pug");
        resolver.setRenderExceptions(true);
        resolver.setOrder(1);
        return resolver;
    }
}
