package io.github.andrewsumsion.pythonplugins.py4jinjection;

import io.github.andrewsumsion.pythonplugins.PythonPlugins;
import py4j.commands.Command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class SynchronousManager {
    private static final AtomicBoolean sync = new AtomicBoolean(false);
    private static final BlockingQueue<CommandCall> waitingCommand = new LinkedBlockingQueue<>(1);
    private static final Semaphore lock = new Semaphore(1);
    static {
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void startSync() {
        sync.set(true);
    }

    public static void stopSync() {
        sync.set(false);
    }

    public static boolean isSync() {
        return sync.get();
    }

    public static void submitCommand(Command command, String commandLine, BufferedReader reader, BufferedWriter writer) {
        try {
            waitingCommand.put(new CommandCall(command, commandLine, reader, writer));
            lock.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void executeCommand() {
        try {
            CommandCall command = waitingCommand.take();
            command.execute();
            lock.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static class CommandCall {
        private Command command;
        private String commandLine;
        private BufferedReader reader;
        private BufferedWriter writer;

        public CommandCall(Command command, String commandLine, BufferedReader reader, BufferedWriter writer) {
            this.command = command;
            this.commandLine = commandLine;
            this.reader = reader;
            this.writer = writer;
        }

        public Command getCommand() {
            return command;
        }

        public String getCommandLine() {
            return commandLine;
        }

        public BufferedReader getReader() {
            return reader;
        }

        public BufferedWriter getWriter() {
            return writer;
        }

        public void execute() {
            try {
                command.execute(commandLine, reader, writer);
            } catch (Exception e) {
                PythonPlugins.getInstance().getLogger().severe("An error was encountered while running a synchronous Python task");
                e.printStackTrace();
                stopSync();
            }
        }
    }
}
