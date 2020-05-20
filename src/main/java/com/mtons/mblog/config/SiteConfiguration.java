package com.mtons.mblog.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.mtons.mblog.modules.template.TemplateDirective;
import com.mtons.mblog.modules.template.method.TimeAgoMethod;
import com.mtons.mblog.shiro.tags.ShiroTags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *包含freemarker配置
 */
@Configuration
@EnableAsync
public class SiteConfiguration {
    @Autowired
    private freemarker.template.Configuration configuration;
    @Autowired
    private ApplicationContext applicationContext;

    //被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行
    // 并且只会被服务器执行一次
    // PostConstruct在构造函数之后执行，init（）方法之前执行
    @PostConstruct
    public void setSharedVariable() {

        /**
         *  根据类型，getBeansOfType获取bean实例，
         *  加载每一个继承TemplateDirective父类的子类，如UserCommentsDirective，UserFavoritesDirective
         *  map集合 key值 为 bean 的name，配置若无别名，直接默认使用类名
         *  value 为bean实例对象 ，即 实体类
         *  map<"UserFavoritesDirective",UserFavoritesDirective>
         */
        //返回TemplateDirective接口的全部实现类
        Map<String, TemplateDirective> map = applicationContext.getBeansOfType(TemplateDirective.class);

        /**
         * 将标签@user_comments、或@user_favors注册到配置文件
         * v.getName()方法获取数据 user_comments、或user_favors，根据标签@user_comments注册到配置文件
         *  相当于setSharedVariable("user_comments",UserCommentsDirective)
         *  如果标签很多，那么每个标签都要执行 ，通过configuration.setSharedVariable实现注册
         *
         *  类似于  for(Map.Entry<String, TemplateDirective> entry : map.entrySet())
         *             {configuration.setSharedVariable(entry.getValue().getName , entry.getValue()) }
         */
        map.forEach((k, v) -> configuration.setSharedVariable(v.getName(), v));
        configuration.setSharedVariable("timeAgo", new TimeAgoMethod());
        configuration.setSharedVariable("shiro", new ShiroTags());
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("mtons.mblog.logThread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }

    @Bean
    @ConditionalOnClass({JSON.class})
    public FastJsonHttpMessageConverter fastJsonHttpMessageConverter() {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();

        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.DisableCircularReferenceDetect
        );
        fastConverter.setFastJsonConfig(fastJsonConfig);
        return fastConverter;
    }

//    @Bean
//    public HttpMessageConverters httpMessageConverters(){
//        FastJsonHttpMessageConverter jsonHttpMessageConverter = fastJsonHttpMessageConverter();
//        return new HttpMessageConverters(jsonHttpMessageConverter);
//    }
}
