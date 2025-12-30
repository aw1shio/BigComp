package acs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步线程池配置。
 *
 * 目的：
 * - 让 @Async 有一个可控的线程池（而不是默认 SimpleAsyncTaskExecutor）
 * - 便于你在报告里解释“并发访问”的实现方式
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 访问控制系统专用线程池。
     *
     * 说明：
     * - 核心线程数/最大线程数/队列大小可以根据并发压测再调
     * - 线程名加前缀便于日志排查
     */
    @Bean(name = "acsExecutor")
    public Executor acsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(2000);
        executor.setThreadNamePrefix("acs-");
        executor.initialize();
        return executor;
    }
}
