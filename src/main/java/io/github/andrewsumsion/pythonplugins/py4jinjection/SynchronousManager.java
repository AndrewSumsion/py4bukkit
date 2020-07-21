package io.github.andrewsumsion.pythonplugins.py4jinjection;

import py4j.commands.Command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronousManager {
    private static final AtomicBoolean sync = new AtomicBoolean(false);
    private static final AtomicReference<CommandCall> waitingCommand = new AtomicReference<>();
    private static ReentrantLock lock = new ReentrantLock();
    private static final Object monitor = new Object();

    public static void startSync() {
        sync.set(true);
    }

    public static void stopSync() {
        sync.set(false);
    }

    public static boolean isSync() {
        return sync.get();
    }

    public static void setCommandCall(Command command, String commandLine, BufferedReader reader, BufferedWriter writer) {
        waitingCommand.set(new CommandCall(command, commandLine, reader, writer));
    }

    public static CommandCall getCommandCall() {
        return waitingCommand.get();
    }

    public static ReentrantLock getLock() {
        return lock;
    }

    public static Object getMonitor() {
        return monitor;
    }

    private static boolean printedNotAvailable = false;

    public static void executeCommand() {
        if(waitingCommand.get() == null) {
            if(!printedNotAvailable) {
                System.out.println("Command not available");
                printedNotAvailable = !printedNotAvailable;
            }
            return;
        }
        waitingCommand.get().execute();
        //System.out.println("Command executed");
        synchronized (monitor) {
            monitor.notify();
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
            if(command == null) {
                return;
            }
            try {
                command.execute(commandLine, reader, writer);
                command = null;
            } catch (IOException e) {
                if(!e.getMessage().contains("Stream closed")) {
                    e.printStackTrace();
                }
            }
        }
    }
}
