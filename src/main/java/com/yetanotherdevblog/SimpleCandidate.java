package com.yetanotherdevblog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.cluster.leader.Context;
import org.springframework.cloud.cluster.leader.DefaultCandidate;

import java.util.HashSet;
import java.util.Set;

public class SimpleCandidate extends DefaultCandidate implements CommandLineRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleCandidate.class);

    private final ZookeeperLeaderConfig.Scheduler scheduler;

    public SimpleCandidate(String id, String role, ZookeeperLeaderConfig.Scheduler scheduler) {
        super(id, role);
        this.scheduler = scheduler;
    }

    private Set<String> hooks = new HashSet<>();

    @Override
    public void onGranted(Context ctx) {
        LOGGER.info("onGranted {}", ctx);
        scheduler.start();
    }

    @Override
    public void onRevoked(Context ctx) {
        LOGGER.info("onRevoked {}", ctx);
        scheduler.stop();
    }

    @Override
    public void run(String... strings) throws Exception {
        if (hooks.size() == 0) {
            String cron = "*/2 * * * * *";
            hooks.add(cron);
            scheduler.addTask(getTask(), cron);
        }
    }

    private Runnable getTask() {
        return () -> {
            LOGGER.info("Pinging");
        };
    }

}
