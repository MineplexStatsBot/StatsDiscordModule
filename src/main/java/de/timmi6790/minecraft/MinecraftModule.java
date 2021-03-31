package de.timmi6790.minecraft;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.command.CommandModule;
import de.timmi6790.minecraft.commands.NamesCommand;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class MinecraftModule extends AbstractModule {
    public MinecraftModule() {
        super("Minecraft");

        this.addDependenciesAndLoadAfter(
                CommandModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        this.getModuleOrThrow(CommandModule.class).registerCommands(
                this,
                new NamesCommand()
        );

        return true;
    }
}
