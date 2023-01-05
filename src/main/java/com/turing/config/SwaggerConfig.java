package com.turing.config;

import com.google.common.base.Predicate;
import com.turing.common.HttpStatusCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Author: 又蠢又笨的懒羊羊程序猿
 * @CreateTime: 2022年01月19日 23:35:51
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Value("${spring.swagger2.enabled}")
    private Boolean enabled;

    //    @Bean
    //    public Docket swaggerSpringMvcPlugin() {
    //        return createRestApi("前台接口", PathSelectors.regex(".*/user/.*"));
    //    }

    //    @Bean
    //    public Docket ProDocket() {
    //        return createRestApi("管理员接口", PathSelectors.regex(".*/admin/.*"));
    //    }

    public Docket createRestApi(String group, Predicate<String> paths) {
        List<ResponseMessage> messageList = new ArrayList<>();
        Arrays.stream(HttpStatusCode.values()).forEach(statusCode -> {
            messageList.add(new ResponseMessageBuilder().code(statusCode.getCode())
                                                        .message(statusCode.getMessage())
                                                        .responseModel(new ModelRef(statusCode.getMessage()))
                                                        .build());
        });

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName(group)
                .apiInfo(apiInfo())
                .enable(enabled)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.turing.controller"))
                .paths(paths)
                .build();
    }

    public ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("SignWe接口文档" + "\t" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                                   .description("SignWe Programmer API Document")
                                   .version("1.0")
                                   .build();
    }
}
