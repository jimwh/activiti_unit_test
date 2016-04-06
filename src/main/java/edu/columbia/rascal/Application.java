package edu.columbia.rascal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@EnableAutoConfiguration
@ImportResource( { "spring-context.xml" } )
public class Application {

    static final Logger log = LoggerFactory.getLogger(Application.class);
    public static void main(String[] args) {
        log.info("start ...");

        ApplicationContext ctx= SpringApplication.run(Application.class, args);
        KickOff kickOff = ctx.getBean(KickOff.class);

        kickOff.startUp();

        SpringApplication.exit(ctx);
        log.info("end ...");
        System.exit(0);
    }
}
