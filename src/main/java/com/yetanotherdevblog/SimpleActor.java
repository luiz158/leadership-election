package com.yetanotherdevblog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.integration.leader.Context;
import org.springframework.integration.leader.DefaultCandidate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SimpleActor extends DefaultCandidate implements CommandLineRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleActor.class);

    private final Config.Scheduler scheduler;

    public SimpleActor(Config.Scheduler scheduler) {
        super(UUID.randomUUID().toString(), "");
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
            LOGGER.info("Pinging: ");

        };
    }
}
