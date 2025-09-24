package uz.fido.pfexchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class PfExchangeApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(PfExchangeApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PfExchangeApplication.class);
    }

}
