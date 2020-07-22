package io.github.andrewsumsion.pythonplugins;

import io.github.andrewsumsion.pythonplugins.py4jinjection.SynchronousManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

public class PythonEntryPoint {

    private static final String[] eventPackages = {
            "org.bukkit.event",
            "org.bukkit.event.block",
            "org.bukkit.event.enchantment",
            "org.bukkit.event.entity",
            "org.bukkit.event.hanging",
            "org.bukkit.event.inventory",
            "org.bukkit.event.player",
            "org.bukkit.event.raid",
            "org.bukkit.event.server",
            "org.bukkit.event.vehicle",
            "org.bukkit.event.weather",
            "org.bukkit.event.world"
    };

    private Class getEventClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {

        }
        for(String packageName : eventPackages) {
            try {
                return Class.forName(packageName + "." + name);
            } catch (ClassNotFoundException e) {

            }
        }
        throw new RuntimeException("No event class called " + name + " found.");
    }

    public Server getBukkitServer() {
        return Bukkit.getServer();
    }

    public Plugin getPlugin() {
        return PythonPlugins.getInstance();
    }

    public void startSync() {
        SynchronousManager.startSync();
    }

    public void stopSync() {
        SynchronousManager.stopSync();
    }

    public boolean isSync() {
        return SynchronousManager.isSync();
    }

    public void registerEvent(String eventType, final PythonEventHandler handler) {
        registerEvent(getEventClass(eventType), EventPriority.NORMAL, new DynamicListener<Event>() {
            @Override
            public void handle(final Event event) {
                runSynchronousTask(new Runnable() {
                    @Override
                    public void run() {
                        handler.handle(event);
                    }
                });
            }
        });
    }

    public void scheduleTask(final PythonTask task) {
        scheduleDelayedTask(task, 0);
    }

    public void scheduleDelayedTask(final PythonTask task, long delay) {
        Bukkit.getScheduler().runTaskLater(PythonPlugins.getInstance(), new Runnable() {
            @Override
            public void run() {
                runSynchronousTask(new Runnable() {
                    @Override
                    public void run() {
                        task.run();
                    }
                });
            }
        }, delay);
    }

    public void scheduleRepeatingTask(PythonTask task, long interval) {
        scheduleDelayedRepeatingTask(task, 0, interval);
    }

    public void scheduleDelayedRepeatingTask(final PythonTask task, long delay, long interval) {
        Bukkit.getScheduler().runTaskTimer(PythonPlugins.getInstance(), new Runnable() {
            @Override
            public void run() {
                runSynchronousTask(new Runnable() {
                    @Override
                    public void run() {
                        task.run();
                    }
                });
            }
        }, delay, interval);
    }

    public void scheduleAsyncTask(PythonTask task) {
        scheduleDelayedAsyncTask(task, 0);
    }

    public void scheduleDelayedAsyncTask(final PythonTask task, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(PythonPlugins.getInstance(), new Runnable() {
            @Override
            public void run() {
                task.run();
            }
        }, delay);
    }

    public void scheduleRepeatingAsyncTask(PythonTask task, long interval) {
        scheduleDelayedRepeatingAsyncTask(task, 0, interval);
    }

    public void scheduleDelayedRepeatingAsyncTask(final PythonTask task, long delay, long interval) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(PythonPlugins.getInstance(), new Runnable() {
            @Override
            public void run() {
                task.run();
            }
        }, delay, interval);
    }

    private void runSynchronousTask(Runnable runnable) {
        SynchronousManager.startSync();
        Thread thread = new Thread(runnable);
        thread.start();
        while (SynchronousManager.isSync()) {
            SynchronousManager.executeCommand();
        }
    }

    private static void registerEvent(Class<? extends Event> eventClass, EventPriority priority, DynamicListener<? extends Event> listener) {
        Bukkit.getPluginManager().registerEvent(eventClass, listener, priority, new DynamicExecutor(), PythonPlugins.getInstance());
    }

    private static class DynamicExecutor implements EventExecutor {
        public void execute(Listener listener, Event event) {
            if(!(listener instanceof DynamicListener)) {
                throw new IllegalArgumentException();
            }
            ((DynamicListener) listener).handle(event);
        }
    }

    public abstract static class DynamicListener<T extends Event> implements Listener {
        public abstract void handle(T event);
    }
}
