package ru.itmo.zavar.highload.gateway;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactivefeign.spring.config.EnableReactiveFeignClients;

@OpenAPIDefinition(
        info = @Info(contact = @Contact(name = "gourav kumar",
                email = "gov.kumarbharatdwaj@gmail.com"), title = "Product Service",
                termsOfService = "www.product.com/terms-and-condition",
                description = "Product service provide the product informations to the other users.",
                license = @License(name = "product service licence", url = "www.product.com/licence"),
                version = "v1"))
@EnableReactiveFeignClients
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
