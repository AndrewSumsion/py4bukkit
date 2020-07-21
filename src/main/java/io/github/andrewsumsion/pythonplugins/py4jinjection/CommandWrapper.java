package io.github.andrewsumsion.pythonplugins.py4jinjection;

import py4j.Gateway;
import py4j.Py4JException;
import py4j.Py4JServerConnection;
import py4j.commands.Command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CommandWrapper implements Command {
    private Command wrapped;

    public CommandWrapper(Command wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void execute(String commandLine, BufferedReader bufferedReader, BufferedWriter bufferedWriter) throws Py4JException, IOException {
        if(SynchronousManager.isSync()) {

            SynchronousManager.setCommandCall(wrapped, commandLine, bufferedReader, bufferedWriter);
            try {
                synchronized (SynchronousManager.getMonitor()) {
                    SynchronousManager.getMonitor().wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
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
