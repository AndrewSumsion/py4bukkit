package io.github.andrewsumsion.pythonplugins.py4jinjection;

import py4j.Gateway;
import py4j.Py4JException;
import py4j.Py4JServerConnection;
import py4j.commands.Command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class CommandWrapper implements Command {
    private Command wrapped;

    public CommandWrapper(Command wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void execute(String commandLine, BufferedReader bufferedReader, BufferedWriter bufferedWriter) throws Py4JException, IOException {
        if(SynchronousManager.isSync()) {
            SynchronousManager.setCommandCall(wrapped, commandLine, bufferedReader, bufferedWriter);
            System.out.println("CommandWrapper: Set SynchronousManager command");
            System.out.println("CommandWrapper: Waiting for SynchronousManager to lock");
            while(!SynchronousManager.getLock().isLocked()) {}
            System.out.println("CommandWrapper: SynchronousManager locked, waiting for unlock");
            try {
                SynchronousManager.getLock().lock();
                System.out.println("CommandWrapper: SynchronousManager unlocked; lock acquired");
            } finally {
                SynchronousManager.getLock().unlock();
                System.out.println("CommandWrapper: Lock given up");
            }
        } else {
            wrapped.execute(commandLine, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public String getCommandName() {
        return wrapped.getCommandName();
    }

    @Override
    public void init(Gateway gateway, Py4JServerConnection py4JServerConnection) {
        wrapped.init(gateway, py4JServerConnection);
    }
}
