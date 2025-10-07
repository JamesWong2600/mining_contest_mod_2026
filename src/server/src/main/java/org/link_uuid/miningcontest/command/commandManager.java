package org.link_uuid.miningcontest.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class commandManager {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("addadmin")
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            source.sendMessage(Text.literal("Admin added!"));
                            return 1;
                        })
        );
    }
}
