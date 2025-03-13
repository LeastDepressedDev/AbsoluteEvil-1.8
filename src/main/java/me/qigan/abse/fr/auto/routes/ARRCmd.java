package me.qigan.abse.fr.auto.routes;

import me.qigan.abse.fr.auto.routes.elems.ARENull;
import me.qigan.abse.fr.auto.routes.elems.ARElement;
import me.qigan.abse.fr.auto.routes.elems.ARWait;
import me.qigan.abse.fr.auto.routes.elems.ARWalk;
import me.qigan.abse.sync.CommandRoute;
import me.qigan.abse.sync.GenCommandDispatcher;
import me.qigan.abse.sync.Sync;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.lang.reflect.Method;

public class ARRCmd extends GenCommandDispatcher {

    public static ARoute route;

    public ARRCmd() {
        super("arr");
    }

    @CommandRoute(route = "/")
    public void def(String[] args) {
        Sync.player().addChatMessage(new ChatComponentText("\u00A7aQigan auto routes. Type /arr help for help."));
    }

    @CommandRoute(route = "/clear")
    public void clearRoute(String[] args) {
        route.clear();
    }

    @CommandRoute(route = "/begin")
    public void beginRoute(String[] args) {
        try {
            ARoute.Referer ref = ARoute.Referer.valueOf(args[0]);
            route = new ARoute(ref, args.length > 1 ? Integer.parseInt(args[1]) : 0, Sync.player().getPositionVector());
        } catch (IllegalArgumentException ex) {
            Sync.player().addChatMessage(new ChatComponentText("\u00A7cIllegal referrer type. \u00A76Allowed type: [general, dungeon, floor]"));
        }
    }

    @CommandRoute(route = "/help")
    public void helpCmd(String[] args) {
        Sync.player().addChatMessage(new ChatComponentText("\u00A7aThere will be help."));
    }


    /*

        Route parts



     */

    @CommandRoute(route = "/add")
    public void addRoutePart(String[] args) {

    }

    @CommandRoute(route = "/undo")
    public void addRouteUndo(String[] args) {
        route.elems.remove(route.elems.size()-1);
    }

    @CommandRoute(route = "/add/walk")
    public void addRouteWalk(String[] args) {
        ARElement ele = route.elems.get(route.elems.size()-1);
        route.add(new ARWalk(ele.endPos, Sync.player().getPositionVector(), false, true));
    }

    @CommandRoute(route = "/add/wait")
    public void addRouteWait(String[] args) {
        if (args.length > 0) {
            route.add(new ARWait(Sync.player().getPositionVector(), Long.parseLong(args[0])));
        } else {
            Sync.player().addChatMessage(new ChatComponentText("\u00A7cDelay required."));
        }
    }

    @Override
    protected Method[] methods() {
        return this.getClass().getMethods();
    }
}
