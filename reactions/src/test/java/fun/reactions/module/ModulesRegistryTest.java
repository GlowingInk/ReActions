package fun.reactions.module;

import fun.reactions.ReActions;
import fun.reactions.cfg.RaConfiguration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class ModulesRegistryTest {
    private ComponentLogger logger;
    private ModulesRegistry registry;
    private List<String> registered;

    @BeforeMethod
    public void setUp() {
        logger = mock(ComponentLogger.class);
        PluginManager pluginManager = mock(PluginManager.class);
        when(pluginManager.isPluginEnabled(anyString())).thenReturn(true);
        Server server = mock(Server.class);
        when(server.getPluginManager()).thenReturn(pluginManager);
        ReActions.Platform platform = mock(ReActions.Platform.class);
        when(platform.logger()).thenReturn(logger);
        when(platform.getServer()).thenReturn(server);
        when(platform.getDataFolder()).thenReturn(new File("."));
        when(platform.getConfiguration()).thenReturn(mock(RaConfiguration.class, RETURNS_DEEP_STUBS));
        registry = new ModulesRegistry(platform);
        registered = new ArrayList<>();
    }

    @Test
    public void waitsForRequiredModulesTest() {
        registry.registerModule(new TestModule("A", List.of("B"), List.of()));
        registry.registerModule(new TestModule("B", List.of("C"), List.of("Plugin")));
        registry.registerModule(new TestModule("C", List.of(), List.of()));
        assertEquals(registered, List.of("C"));
        registry.registerPending();
        assertEquals(registered, List.of("C", "B", "A"));
    }

    @Test
    public void registersModulesInLoopTest() {
        registry.registerModule(new TestModule("A", List.of("B"), List.of()));
        registry.registerModule(new TestModule("B", List.of("A"), List.of()));
        registry.registerPending();
        assertEquals(registered, List.of("B", "A"));
        verify(logger).warn(contains("loop"), any(Object[].class));
    }

    @Test
    public void skipsModulesWithMissingRequiredModulesTest() {
        registry.registerModule(new TestModule("A", List.of("B"), List.of()));
        registry.registerModule(new TestModule("B", List.of("Missing"), List.of()));
        registry.registerModule(new TestModule("C", List.of(), List.of()));
        registry.registerPending();
        assertEquals(registered, List.of("C"));
    }

    private class TestModule implements Module {
        private final String name;
        private final List<String> requiredModules;
        private final List<String> requiredPlugins;

        private TestModule(String name, List<String> requiredModules, List<String> requiredPlugins) {
            this.name = name;
            this.requiredModules = requiredModules;
            this.requiredPlugins = requiredPlugins;
        }

        @Override
        public @NotNull Collection<@NotNull String> requiredModules() {
            return requiredModules;
        }

        @Override
        public @NotNull Collection<@NotNull String> requiredPlugins() {
            return requiredPlugins;
        }

        @Override
        public void onRegister(@NotNull ReActions.Platform platform) {
            registered.add(name);
        }

        @Override
        public @NotNull String getName() {
            return name;
        }
    }
}
