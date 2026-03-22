package com.replacetool.util;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Singleton queue that holds pending ReplaceTask jobs.
 * Registered on the Forge event bus; processes one task per tick,
 * one batch per tick inside that task.
 */
@Mod.EventBusSubscriber
public class ReplaceTaskQueue {

    public static final ReplaceTaskQueue INSTANCE = new ReplaceTaskQueue();

    private final Deque<ReplaceTask> queue = new ArrayDeque<>();

    private ReplaceTaskQueue() {}

    public void enqueue(ReplaceTask task) {
        queue.addLast(task);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ReplaceTask current = INSTANCE.queue.peekFirst();
        if (current == null) return;
        boolean done = current.tick();
        if (done) {
            INSTANCE.queue.pollFirst();
        }
    }
}
