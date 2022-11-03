package com.turing.config;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;

import java.util.Collections;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2022年10月29日 16:58:15
 */
public class MybatisPlusGenerator {
    public static void main(String[] args) {
        String property = System.getProperty("user.dir");
        System.out.println(property);
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/sign_we?useUnicode=true&characterEncoding=utf-8&useSSL=false", "root", "123456")
                         .globalConfig(builder -> {
                             builder.author("Paddi-Yan") // 设置作者
                                    .enableSwagger() // 开启 swagger 模式
                                    .fileOverride() // 覆盖已生成文件
                                    .outputDir("E:\\IntelliJ-IDEA-WorkPlace\\SignWe\\src\\main\\java"); // 指定输出目录
                         })
                         .packageConfig(builder -> {
                             builder.parent("com") // 设置父包名
                                    .moduleName("turing") // 设置父包模块名
                                    .pathInfo(Collections.singletonMap(OutputFile.mapperXml, "E:\\IntelliJ-IDEA-WorkPlace\\SignWe\\src\\main\\resources")); // 设置mapperXml生成路径
                         })
                         .strategyConfig(builder -> {
                             builder.addInclude("sys_chairs", "sys_door", "sys_notice", "sys_ranking", "sys_record", "sys_user", "sys_yesterday_record") // 设置需要生成的表名
                                    .addTablePrefix("sys_"); // 设置过滤表前缀
                             builder.entityBuilder()
                                    .enableRemoveIsPrefix()
                                    .enableLombok();
                             builder.controllerBuilder()
                                    .enableRestStyle()
                                    .formatFileName("%sController");
                             builder.serviceBuilder()
                                    .superServiceClass(IService.class)
                                    .superServiceImplClass(ServiceImpl.class)
                                    .formatServiceFileName("%sService")
                                    .formatServiceImplFileName("%sServiceImpl");
                             builder.mapperBuilder()
                                    .superClass(BaseMapper.class)
                                    .enableMapperAnnotation()
                                    .enableBaseResultMap()
                                    .formatMapperFileName("%sMapper")
                                    .formatXmlFileName("%sMapper");

                         })
                         //.templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                         .execute();
    }
}
