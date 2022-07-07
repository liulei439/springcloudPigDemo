package com.learn.common.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步处理问题时候，自定义线程池，配置
 */
@AutoConfiguration
public class TaskExecutorConfiguration implements AsyncConfigurer {
    /**
     * 获取当前机器的核数, 不一定准确 请根据实际场景 CPU密集 || IO 密集
     */
    public static final int cpuNum = Runtime.getRuntime().availableProcessors();

    @Value("${thread.pool.corePoolSize:}")
    private Optional<Integer> corePoolSize;

    @Value("${thread.pool.maxPoolSize:}")
    private Optional<Integer> maxPoolSize;

    @Value("${thread.pool.queueCapacity:}")
    private Optional<Integer> queueCapacity;

    @Value("${thread.pool.awaitTerminationSeconds:}")
    private Optional<Integer> awaitTerminationSeconds;

    @Override
    @Bean
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程大小 默认区 CPU 数量
        executor.setCorePoolSize(corePoolSize.orElse(cpuNum));
        // 最大线程大小 默认区 CPU * 2 数量
        executor.setMaxPoolSize(maxPoolSize.orElse(cpuNum * 2));
        // 队列最大容量
        executor.setQueueCapacity(queueCapacity.orElse(500));
        // 拒绝策略 直接在execute方法的调用线程中运行被拒绝的任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        /*
            在一些场景下，若需要在关闭线程池时等待当前调度任务完成后才开始关闭，可以通过简单的配置，进行优雅的停机策略配置。
            关键就是通过setWaitForTasksToCompleteOnShutdown(true)和setAwaitTerminationSeconds方法。
            setWaitForTasksToCompleteOnShutdown:表明等待所有线程执行完，默认为false。
            setAwaitTerminationSeconds:等待的时间，因为不能无限的等待下去。
         */
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds.orElse(60));
        // 设置线程前缀名字
        executor.setThreadNamePrefix("PIG-Thread-");
        executor.initialize();
        return executor;
    }
}
