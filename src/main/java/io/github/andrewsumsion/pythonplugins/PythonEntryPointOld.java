package io.github.andrewsumsion.pythonplugins;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class PythonEntryPointOld {
    private LinkedBlockingQueue<Event> events = new LinkedBlockingQueue<Event>();
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
    private List<Class<? extends Event>> subscribedEvents = new ArrayList<>();

    public void subscribe(Class<? extends Event> clazz) {
        System.out.println("subscribe called");
        if(subscribedEvents.contains(clazz)) {
            return;
        }
        registerEvent(clazz, EventPriority.NORMAL, new DynamicListener<Event>() {
            @Override
            public void handle(Event event) {
                try {
                    events.put(event);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        subscribedEvents.add(clazz);
    }

    public void subscribe(String eventName) {
        subscribe(getEventClass(eventName));
    }

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

    public Event takeEvent() {
        System.out.println("takeEvent called");
        try {
            return events.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean eventAvailable() {
        return events.size() >= 1;
    }

    public Server getBukkitServer() {
        return Bukkit.getServer();
    }

    public Plugin getPlugin() {
        return PythonPlugins.getInstance();
    }

    public void handleSynchronously(final Event event, final PythonHandler handler) {
        System.out.println("handleSynchronously called");
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
            @Override
            public void run() {
                System.out.println("Scheduled task called");
                handler.handle(event);
            }
        });
    }

    public void createExplosion(World world, Location location, float size) {
        world.createExplosion(location, size);
        System.out.println("Exploded bro");
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
