package io.github.andrewsumsion.pythonplugins;

import io.github.andrewsumsion.pythonplugins.py4jinjection.ConnectionInjectorListener;
import org.bukkit.plugin.java.JavaPlugin;
import py4j.GatewayServer;
import py4j.reflection.ReflectionUtil;
import py4j.reflection.RootClassLoadingStrategy;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class PythonPlugins extends JavaPlugin {
    private static PythonPlugins INSTANCE;
    private PythonEntryPoint entryPoint;
    private GatewayServer gatewayServer;

    public static PythonPlugins getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        RootClassLoadingStrategy rmmClassLoader = new RootClassLoadingStrategy();
        ReflectionUtil.setClassLoadingStrategy(rmmClassLoader);
        entryPoint = new PythonEntryPoint();
        gatewayServer = new GatewayServer(entryPoint);
        gatewayServer.addListener(new ConnectionInjectorListener());

        GatewayServer.turnLoggingOn();
        Logger logger = Logger.getLogger("py4j");
        logger.setLevel(Level.ALL);

        gatewayServer.start();


    }

    @Override
    public void onDisable() {
        gatewayServer.shutdown();
    }
}
