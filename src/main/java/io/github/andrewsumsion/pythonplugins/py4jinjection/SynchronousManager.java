package io.github.andrewsumsion.pythonplugins.py4jinjection;

import py4j.commands.Command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronousManager {
    private static boolean sync = false;
    private static CommandCall waitingCommand;
    private static ReentrantLock lock = new ReentrantLock();

    public static void startSync() {
        sync = true;
    }

    public static void stopSync() {
        sync = false;
    }

    public static boolean isSync() {
        return sync;
    }

    public static void setCommandCall(Command command, String commandLine, BufferedReader reader, BufferedWriter writer) {
        waitingCommand = new CommandCall(command, commandLine, reader, writer);
    }

    public static CommandCall getCommandCall() {
        return waitingCommand;
    }

    public static ReentrantLock getLock() {
        return lock;
    }

    public static void runUntilDone() {
        while(sync) {
            System.out.println("Entering loop because sync mode is enabled");
            try {
                System.out.println("runUntilDone: Waiting for lock");
                getLock().lock();
                System.out.println("runUntilDone: Lock acquired");
                System.out.println("runUntilDone: Executing command");
                waitingCommand.execute();
                System.out.println("runUntilDone: Command executed");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                getLock().unlock();
                System.out.println("runUntilDone: Given up lock");
            }
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

        public void execute() throws IOException {
            command.execute(commandLine, reader, writer);
        }
    }
}
