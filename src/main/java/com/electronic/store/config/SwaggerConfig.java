package com.electronic.store.config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "scheme1",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@OpenAPIDefinition(
        info = @Info(
                title = "Electronic Store API",
                description = "This is backend of electronic store developed",
                version = "1.0v",
                contact = @Contact(
                        name = "Rupesh kumar",
                        email = "rupesh123@gmail.com",
                        url = "https://substirng.com"
                ),
                license = @License(
                        name = "OPEN License",
                        url = "https//substring.com"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "This is external docs",
                url = "https://substring.com"
        )
)
public class SwaggerConfig {


//    @Bean
//    public OpenAPI openAPI(){
//        String schemeName="bearerScheme";
//        return new OpenAPI()
//                .addSecurityItem(new SecurityRequirement()
//                        .addList(schemeName)
//                )
//                .components(new Components()
//                        .addSecuritySchemes(schemeName,new SecurityScheme()
//                                .name(schemeName)
//                                .type(SecurityScheme.Type.HTTP)
//                                .bearerFormat("Jwt")
//                                .scheme("bearer")
//                        )
//                )
//                .info(new Info()
//                        .title("Electronic Store API")
//                        .description("This is electronic store project api")
//                        .version("1.0")
//                        .contact(new Contact().name("Rupesh").email("rupesh123@gmail.com").url("www.substringprojects.com"))
//                        .license(new License().name("Apache"))
//
//                ).externalDocs(new ExternalDocumentation().url("substirng.com").description("This is external url"));
//    }
}
