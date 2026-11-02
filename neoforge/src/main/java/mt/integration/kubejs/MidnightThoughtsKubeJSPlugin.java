package mt.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import net.neoforged.neoforge.common.NeoForge;

@SuppressWarnings("unused")
public class MidnightThoughtsKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(MTKubeEvents.GROUP);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("MidnightThoughts", MTBindings.class);
    }

    @Override
    public void init() {
        NeoForge.EVENT_BUS.register(new MTEventForwarder());
    }
}
