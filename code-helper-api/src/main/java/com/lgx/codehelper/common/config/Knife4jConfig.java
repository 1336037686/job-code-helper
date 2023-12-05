package com.lgx.codehelper.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import javax.annotation.Resource;

/**
 * Knife4j API文档配置类
 * @author LGX_TvT <br>
 * @version 1.0 <br>
 * Create by 2022-04-04 22:25 <br>
 * @description: JyKnife4jConfiguration <br>
 */
@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfig {

    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        // 更新api描述
                        .description("Spring Boot2 Restful Code Helper API")
                        // 更新api版本
                        .version("v0.0.1")
                        .build())
                .select()
                //这里指定Controller扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.lgx"))
                .paths(PathSelectors.any())
                .build();
    }
}
