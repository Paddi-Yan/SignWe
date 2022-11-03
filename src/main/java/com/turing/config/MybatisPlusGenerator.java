package com.turing.config;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;

import java.util.Collections;

/**
 * @Author: 又蠢又笨的懒羊羊程序猿
 * @CreateTime: 2022年05月18日 18:59:04
 */
public class MybatisPlusGenerator {
    public static void main(String[] args) {
        String property = System.getProperty("user.dir");
        System.out.println(property);
        FastAutoGenerator.create("jdbc:mysql://119.23.54.229:3306/wx_mini_db?useUnicode=true&characterEncoding=utf-8&useSSL=false", "root", "123456")
                         .globalConfig(builder -> {
                             builder.author("qds") // 设置作者
                                    .enableSwagger() // 开启 swagger 模式
                                    .fileOverride() // 覆盖已生成文件
                                    .outputDir("I:\\ProjectAll\\兴农之桥\\xnzq\\src\\main\\java\\com\\turing\\qds"); // 指定输出目录
                         })
                         .packageConfig(builder -> {
                             builder.parent("com") // 设置父包名
                                    .moduleName("turing") // 设置父包模块名
                                    .pathInfo(Collections.singletonMap(OutputFile.mapperXml,"I:\\ProjectAll\\兴农之桥\\xnzq\\src\\main\\java\\com\\turing\\qds")); // 设置mapperXml生成路径
                         })
                         .strategyConfig(builder -> {
                             builder.addInclude("sys_element","sys_machine","sys_picture") // 设置需要生成的表名
                                    .addTablePrefix("sys_"); // 设置过滤表前缀
                             builder.entityBuilder()
                                    .enableLombok()
                                    .idType(IdType.ASSIGN_ID);
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
