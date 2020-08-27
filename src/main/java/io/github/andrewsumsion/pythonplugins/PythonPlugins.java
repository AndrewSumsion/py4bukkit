package io.github.andrewsumsion.pythonplugins;

import io.github.andrewsumsion.pythonplugins.py4jinjection.ConnectionInjectorListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import py4j.GatewayServer;
import py4j.reflection.ReflectionUtil;
import py4j.reflection.RootClassLoadingStrategy;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PythonPlugins extends JavaPlugin {
    private static PythonPlugins INSTANCE;
    private PythonEntryPoint entryPoint;
    private Process pythonProcess;
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

        gatewayServer.start();

        //GatewayServer.turnLoggingOn();
        //Logger logger = Logger.getLogger("py4j");
        //logger.setLevel(Level.ALL);

        saveDefaultConfig();

        if(!(new File(getDataFolder(), "py4j").exists())) {
            File zipFile = copyFileIfNotExistent("py4j.zip");
            unzip(zipFile);
            zipFile.delete();
        }

        copyFileIfNotExistent("minecraft.py");
        copyFileIfNotExistent("plugin_loader.py");
        String pluginFolderName = getConfig().getString("plugin-folder-name");
        if(pluginFolderName == null) {
            pluginFolderName = "python_plugins";
        }
        File pluginsFolder = new File(pluginFolderName);
        if(!pluginsFolder.exists()) {
            pluginsFolder.mkdir();
            copyFileIfNotExistent("example_plugin.py", new File(pluginsFolder, "example_plugin.py"));
        }

        try {
            pythonProcess = Runtime.getRuntime().exec(new String[]{
                    getConfig().getString("python-command"),
                    new File(getDataFolder(), "plugin_loader.py").getAbsolutePath(),
                    pluginsFolder.getAbsolutePath()
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread loggingThread = new Thread(new Runnable() {
            private StringBuilder stdoutBuilder = new StringBuilder();
            private StringBuilder stderrBuilder = new StringBuilder();

            @Override
            public void run() {
                BufferedReader stdout = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
                BufferedReader stderr = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()));
                while (true) {
                    try {
                        if(stdout.ready()) {
                            int input = stdout.read();
                            if(input == -1) {
                                break;
                            }
                            stdoutBuilder.append((char) input);
                            if(input == '\n') {
                                System.out.print(stdoutBuilder.toString());
                                stdoutBuilder = new StringBuilder();
                            }
                        }
                        if(stderr.ready()) {
                            int input = stderr.read();
                            if(input == -1) {
                                break;
                            }
                            stderrBuilder.append((char) input);
                            if(input == '\n') {
                                System.err.print(stderrBuilder.toString());
                                stderrBuilder = new StringBuilder();
                            }
                        }
                    } catch (IOException e) {
                        break;
                    }
                }
                getLogger().info("Python process disconnected");
            }
        });

        loggingThread.setDaemon(true);
        loggingThread.start();

        Thread closeChildThread = new Thread() {
            public void run() {
                pythonProcess.destroy();
            }
        };

        Runtime.getRuntime().addShutdownHook(closeChildThread);
    }

    private static void unzip(File zipFile) {
        String destDir = zipFile.getAbsolutePath().replace(".zip", "");
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFile);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private File copyFileIfNotExistent(String fileName) {
        return copyFileIfNotExistent(fileName, new File(getDataFolder(), fileName));
    }

    private File copyFileIfNotExistent(String fileName, File destinationFile) {
        InputStream in = getResource(fileName);

        if(!destinationFile.exists()) {
            try {
                Files.copy(in, Paths.get(destinationFile.toURI()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return destinationFile;
    }

    @Override
    public void onDisable() {
        pythonProcess.destroy();
        gatewayServer.shutdown();
    }
}
