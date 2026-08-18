package mt.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;

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
        MTEventForwarder.register();
    }
}
