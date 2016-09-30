package com.yetanotherdevblog;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.cluster.leader.LeaderElectionProperties;
import org.springframework.cloud.cluster.leader.event.LeaderEventPublisher;
import org.springframework.cloud.cluster.zk.ZookeeperClusterProperties;
import org.springframework.cloud.cluster.zk.leader.LeaderInitiator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@ConditionalOnClass(LeaderInitiator.class)
@ConditionalOnProperty(value = {
        "spring.cloud.cluster.zookeeper.leader.enabled",
        "spring.cloud.cluster.leader.enabled" }, matchIfMissing = true)
@ConditionalOnMissingBean(name = "zookeeperLeaderInitiator")
@EnableConfigurationProperties({ LeaderElectionProperties.class,
        ZookeeperClusterProperties.class })
@AutoConfigureAfter(LeaderAutoConfiguration.class)
public class ZookeeperLeaderConfig {

    @Autowired
    private LeaderElectionProperties lep;

    @Autowired
    private ZookeeperClusterProperties zkp;

    @Autowired
    private LeaderEventPublisher publisher;

    @Bean
    public SimpleCandidate candidate(Scheduler scheduler) {
        return new SimpleCandidate(lep.getId(), lep.getRole(), scheduler);
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework zookeeperLeaderCuratorClient() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .defaultData(new byte[0])
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .connectString(zkp.getConnect()).build();
        return client;
    }

    @Bean
    public LeaderInitiator zookeeperLeaderInitiator(SimpleCandidate actor) throws Exception {
        LeaderInitiator initiator = new LeaderInitiator(zookeeperLeaderCuratorClient(),
                actor, zkp.getNamespace());
        initiator.setLeaderEventPublisher(publisher);
        return initiator;
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
