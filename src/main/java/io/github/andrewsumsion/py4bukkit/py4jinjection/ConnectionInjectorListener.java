package io.github.andrewsumsion.py4bukkit.py4jinjection;

import io.github.andrewsumsion.py4bukkit.PythonPlugins;
import py4j.GatewayServerListener;
import py4j.Py4JServerConnection;
import py4j.commands.Command;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ConnectionInjectorListener implements GatewayServerListener {
    @Override
    public void connectionError(Exception e) {

    }

    @Override
    public void connectionStarted(Py4JServerConnection py4JServerConnection) {
        injectCommands(py4JServerConnection);
    }

    private void injectCommands(Py4JServerConnection connection) {
        try {
            Field commandMapField = connection.getClass().getDeclaredField("commands");
            if(!Map.class.isAssignableFrom(commandMapField.getType())) {
                throw new Exception("Connection class missing Map named commands");
            }
            commandMapField.setAccessible(true);
            HashMap<String, Command> commandMap = (HashMap<String, Command>) commandMapField.get(connection);
            for(String key : commandMap.keySet()) {
                commandMap.put(key, new CommandWrapper(commandMap.get(key)));
            }

        } catch (Exception e) {
            PythonPlugins.getInstance().getLogger().warning("Unable to inject Python connection! This connection will not be able to run synchronously.");
            e.printStackTrace();
        }

    }

    @Override
    public void connectionStopped(Py4JServerConnection py4JServerConnection) {

    }

    @Override
    public void serverError(Exception e) {

    }

    @Override
    public void serverPostShutdown() {

    }

    @Override
    public void serverPreShutdown() {

    }

    @Override
    public void serverStarted() {

    }

    @Override
    public void serverStopped() {

    }
}
