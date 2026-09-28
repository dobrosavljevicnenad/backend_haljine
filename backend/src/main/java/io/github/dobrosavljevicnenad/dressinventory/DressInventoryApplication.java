package io.github.dobrosavljevicnenad.dressinventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DressInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DressInventoryApplication.class, args);
    }
}
