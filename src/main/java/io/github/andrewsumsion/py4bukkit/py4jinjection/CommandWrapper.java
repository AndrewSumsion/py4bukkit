package io.github.andrewsumsion.py4bukkit.py4jinjection;

import py4j.Gateway;
import py4j.Py4JException;
import py4j.Py4JServerConnection;
import py4j.commands.Command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;

public class CommandWrapper implements Command {

    private Command wrapped;

    public CommandWrapper(Command wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void execute(String commandLine, BufferedReader bufferedReader, BufferedWriter bufferedWriter) throws Py4JException, IOException {
        if(SynchronousManager.isSync()) {
            SynchronousManager.submitCommand(wrapped, commandLine, bufferedReader, bufferedWriter);
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

    private static class WrappedBufferedReader extends BufferedReader {

        public WrappedBufferedReader(Reader in) {
            super(in);
        }

        @Override
        public String readLine() throws IOException {
            String line = super.readLine();
            System.err.print("String read: \"" + line.replace("\n", "") + "\"");
            new Throwable().printStackTrace();
            return line;
        }
    }
}
