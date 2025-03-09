package me.qigan.abse.sync;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

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
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Method def = null;
        Method matched = null;
        int cordtr = 0;
        for (Method method : this.getClass().getMethods()) {
            if (method.isAnnotationPresent(CommandRoute.class)) {
                CommandRoute route = method.getAnnotation(CommandRoute.class);
                String path = route.route().substring(1);
                if (path.isEmpty()) {
                    def = method;
                    continue;
                }

                int s = 0;
                String[] check = path.split("/");
                if (check.length <= args.length) {
                    for (String p : check) {
                        if (p.equalsIgnoreCase(args[s])) s++;
                        else {
                            s = 0;
                            break;
                        }
                    }
                    if (s > cordtr) {
                        matched = method;
                        cordtr = s;
                    }
                }
            }
        }

        try {
            if (matched != null) {
                String[] nargs = new String[args.length-cordtr];
                for (int i = 0; i < nargs.length; i++) {
                    nargs[i] = args[cordtr+i];
                }
                matched.invoke(this, (Object) nargs);
            } else if (def != null) {
                def.invoke(this, (Object) args);
            }
        } catch (IllegalAccessException | InvocationTargetException ex) {

        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
