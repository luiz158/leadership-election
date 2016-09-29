package com.yetanotherdevblog;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.leader.Candidate;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class Config {

    @Bean
    public LockRegistry lockRegistry(RedisConnectionFactory redis) {
        return new RedisLockRegistry(redis, "canditate-key");
    }

    @Bean
    public SimpleActor candidate(Scheduler scheduler) {
        return new SimpleActor(scheduler);
    }

    @Bean
    public LockRegistryLeaderInitiator lockRegistryLeaderInitiator(
            LockRegistry client,
            Candidate canditate,
            ApplicationEventPublisher publisher) {
        LockRegistryLeaderInitiator initiator = new LockRegistryLeaderInitiator(client, canditate);
        initiator.setApplicationEventPublisher(publisher);
        return initiator;

    }

//    @Bean
    public MyEventListener myEventListener() {
        return new MyEventListener();
    }

    @Component
    static class Scheduler implements SchedulingConfigurer, Closeable {

        private volatile ScheduledTaskRegistrar taskRegistrar;
        private volatile ExecutorService pool;
        private volatile boolean running = false;

        @Override
        public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
            this.taskRegistrar = taskRegistrar;
        }

        public void addTask(Runnable task, String expression) {
            if (running) {
                taskRegistrar.scheduleCronTask(new CronTask(task, expression));
            }
            else {
                taskRegistrar.addCronTask(new CronTask(task, expression));
            }
        }

        public void start() {
            running = true;
            if (taskRegistrar != null) {
                pool = Executors.newScheduledThreadPool(10);
                taskRegistrar.setScheduler(pool);
                taskRegistrar.afterPropertiesSet();
            }
        }

        public void stop() {
            if (taskRegistrar != null) {
                taskRegistrar.destroy();
                if (pool != null) {
                    pool.shutdown();
                    pool = null;
                }
            }
            running = false;
        }

        @Override
        public void close() throws IOException {
            stop();
        }

    }

}


