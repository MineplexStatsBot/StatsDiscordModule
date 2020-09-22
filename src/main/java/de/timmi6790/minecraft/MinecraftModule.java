package de.timmi6790.minecraft;

import de.timmi6790.discord_framework.modules.AbstractModule;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.minecraft.commands.NamesCommand;
import de.timmi6790.minecraft.mojang_api.MojangApi;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class MinecraftModule extends AbstractModule {
    private final MojangApi mojangApi = new MojangApi();

    public MinecraftModule() {
        super("Minecraft");

        addDependenciesAndLoadAfter(
                CommandModule.class
        );
    }

    @Override
    public void onInitialize() {
        getModuleOrThrow(CommandModule.class).registerCommands(
                this,
                new NamesCommand()
        );
    }
    
}
