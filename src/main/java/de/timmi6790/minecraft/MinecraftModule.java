package de.timmi6790.minecraft;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.minecraft.commands.NameHistoryCommand;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class MinecraftModule extends AbstractModule {
    public MinecraftModule() {
        super("Minecraft");

        this.addDependenciesAndLoadAfter(
                SlashCommandModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        final SlashCommandModule commandModule = this.getModuleOrThrow(SlashCommandModule.class);
        commandModule.registerCommands(
                this,
                new NameHistoryCommand(commandModule)
        );

        return true;
    }
}
