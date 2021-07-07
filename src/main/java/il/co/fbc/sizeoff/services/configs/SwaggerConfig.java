package il.co.fbc.sizeoff.services.configs;

//import io.swagger.v3.oas.models.Components;
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class SwaggerConfig {
//    @Bean
//    public OpenAPI customOpenApi() {
//        return new OpenAPI()
//                .components(new Components()
//                        .addSecuritySchemes("bearer-key",
//                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("Custom")));
//    }
    private ApiKey jwtApiKey() {
        return new ApiKey("JWT", "Authorization", "header");
    }

    private SecurityContext jwtSecurityContext() {
        return SecurityContext.builder()
                .securityReferences(Collections.singletonList(
                        new SecurityReference("JWT", new AuthorizationScope[0])))
                .build();
    }

    private SecurityContext basicSecurityContext() {
        return SecurityContext.builder()
                .securityReferences(Collections.singletonList(
                        new SecurityReference("Basic", new AuthorizationScope[0])))
                .build();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .securityContexts(Arrays.asList(basicSecurityContext(), jwtSecurityContext()))
                .securitySchemes(Arrays.asList(new BasicAuth("Basic"), jwtApiKey()))
                .select()
                .apis(RequestHandlerSelectors.basePackage("il.co.fbc.sizeoff.controllers"))
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false)
                ;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("SizeOf")
                .version("2.0.0")
                .description("Back-end of SizeOf Application. Calculate of size Priority installation on FBC client cloud servers/databases")
                .license("Licensed to FBC")
                .licenseUrl("https://www.fbc.co.il/")
                .contact(new Contact("Andrew Miterev", "https://www.linkedin.com/in/andrew-miterev-9490b2b0/", "andrew.miterev@gmail.com"))
                .build();
    }
}
