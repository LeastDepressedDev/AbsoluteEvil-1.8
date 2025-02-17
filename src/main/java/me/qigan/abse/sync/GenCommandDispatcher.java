package me.qigan.abse.sync;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.lang.reflect.Method;

public class GenCommandDispatcher extends CommandBase {

    private final String cmd_name;

    public GenCommandDispatcher(String cmdName) {
        this.cmd_name = cmdName;
    }

    @Override
    public String getCommandName() {
        return cmd_name;
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return cmd_name + " <args>";
    }

    @Override
    public void processCommand(ICommandSender iCommandSender, String[] strings) throws CommandException {
        String args = "/"+String.join("/", strings);
        for (Method method : this.getClass().getMethods()) {
            if (method.isAnnotationPresent(CommandRoute.class)) {
                CommandRoute route = method.getAnnotation(CommandRoute.class);

            }
        }
    }

    @CommandRoute(route = "")
    public void sus() {

    }
}
