package me.ayosynk.stuff.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public final class SchedulerUtils {

    private SchedulerUtils() {}

    /**
     * Runs a task asynchronously.
     */
    public static void runAsync(Plugin plugin, Runnable task) {
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
    }

    /**
     * Runs a task asynchronously after a delay.
     */
    public static void runAsyncLater(Plugin plugin, Runnable task, long delay, TimeUnit unit) {
        Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay, unit);
    }

    /**
     * Runs a task asynchronously repeating.
     */
    public static void runAsyncRepeating(Plugin plugin, Runnable task, long initialDelay, long period, TimeUnit unit) {
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), initialDelay, period, unit);
    }

    /**
     * Runs a task on the global region.
     */
    public static void runGlobal(Plugin plugin, Runnable task) {
        Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
    }

    /**
     * Runs a task on the region of a specific entity.
     */
    public static void runEntity(Plugin plugin, Entity entity, Runnable task) {
        entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
    }

    /**
     * Runs a task on the region of a specific entity after a delay in ticks.
     */
    public static void runEntityLater(Plugin plugin, Entity entity, Runnable task, Runnable retiredTask, long delayTicks) {
        entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), retiredTask, delayTicks);
    }

    /**
     * Runs a task on the region of a specific location.
     */
    public static void runLocation(Plugin plugin, Location location, Runnable task) {
        Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> task.run());
    }
}
