package me.qigan.abse.fr.exc;

import me.qigan.abse.config.AddressedData;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class TimeoutTasks {

    public static List<AddressedData<Runnable, long[]>> ops = new ArrayList<>();

    /**
     * This shit is being called in MixinMinecraft.java
     */
    public static void runGameRef() {
        List<AddressedData<Runnable, long[]>> tasks = new ArrayList<>(ops);
        int i = 0;
        for (AddressedData<Runnable, long[]> task : tasks) {
            if (System.currentTimeMillis()-task.getObject()[1]>task.getObject()[0]) {
                ops.remove(i);
                task.getNamespace().run();
            }
            i++;
        }
    }

    public static void addTimeout(Runnable foo, long delay) {
        ops.add(new AddressedData<>(foo, new long[]{delay, System.currentTimeMillis()}));
    }
}
