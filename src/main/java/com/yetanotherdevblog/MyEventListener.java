package com.yetanotherdevblog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.integration.leader.event.AbstractLeaderEvent;

/**
 * Created by ssouris on 29/9/2016.
 */
class MyEventListener implements ApplicationListener<AbstractLeaderEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyEventListener.class);


    @Override
    public void onApplicationEvent(AbstractLeaderEvent event) {
        // do something with OnGrantedEvent or OnRevokedEvent

//        event.getContext().yield();
        LOGGER.info("{}, {}, {}, {}, {}, {}, {}",
                event,
                event.getContext(),
                event.getRole(),
                event.getSource(),
                event.getTimestamp(),
                event.getClass(),
                event.getContext().isLeader());
    }
}
