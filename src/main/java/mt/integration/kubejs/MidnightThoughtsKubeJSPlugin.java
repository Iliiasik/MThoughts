package mt.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.minecraftforge.common.MinecraftForge;

@SuppressWarnings("unused")
public class MidnightThoughtsKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void registerEvents() {
        MTKubeEvents.GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("MidnightThoughts", MTBindings.class);
    }

    @Override
    public void afterInit() {
        MinecraftForge.EVENT_BUS.register(new MTEventForwarder());
    }
}