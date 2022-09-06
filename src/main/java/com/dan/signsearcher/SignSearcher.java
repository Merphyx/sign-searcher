package com.dan.signsearcher;

import com.dan.signsearcher.event.SignUpdateCallback;
import com.dan.signsearcher.ext.BlockEntityExt;
import com.dan.signsearcher.ext.SignBlockEntityExt;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SignSearcher implements ClientModInitializer {
    private static final HashSet<SignBlockEntity> signs = new HashSet<>();
    private static ArrayList<String> searchTexts = new ArrayList();

    public static void updateSign(SignBlockEntity sign) {
        ((BlockEntityExt) sign).setGlowing(matchesSign(sign));
    }

    public static boolean matchesSign(SignBlockEntity sign) {
        if (searchTexts.isEmpty()) {
            return false;
        }
        StringBuilder signText = new StringBuilder();
        for (Text line : ((SignBlockEntityExt) sign).getTexts()) {
            line.visit((part) -> {
                signText.append(part);
                return Optional.empty();
            });
            signText.append(" ");
        }
        return isSearchedText(signText.toString());
    }

    public static boolean isSearchedText(String text) {
        for (String texts : searchTexts) {
            System.out.println(text + " " + texts);
            if (text.contains(" " + texts + " ") || text.trim().equals(texts)) {
                return true;
            }
        }
        return false;
    }

    public static void addSign(SignBlockEntity sign) {
        signs.add(sign);
        updateSign(sign);
    }

    public static void removeSign(SignBlockEntity sign) {
        signs.remove(sign);
    }

    public static void addSearchText(String searchText) {
        if (!searchTexts.contains(searchText)) searchTexts.add(searchText);
        for (SignBlockEntity sign : signs) {
            updateSign(sign);
        }
    }

    public static void removeSearchText(String searchText) {
        searchTexts.remove(searchText);
        for (SignBlockEntity sign : signs) {
            updateSign(sign);
        }
    }

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(
                    literal("signsearch")
                            .then(literal("desearch")
                                    .executes(context -> {
                                        searchTexts.clear();
                                        for (SignBlockEntity sign : signs) {
                                            updateSign(sign);
                                        }
                                        return 1;
                                    })
                                    .then(argument("text", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                String searchText = StringArgumentType.getString(context, "text");
                                                removeSearchText(searchText);
                                                return 1;
                                            })))
                            .then(literal("search")
                                    .then(argument("query", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                String searchText = StringArgumentType.getString(context, "query");
                                                addSearchText(searchText);
                                                context.getSource().sendFeedback(Text.of(String.format("Searching for \"%s\"..", searchText)));
                                                return 1;
                                            })
                                    )
                            ));
        }));

        SignUpdateCallback.EVENT.register(SignSearcher::addSign);
        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> {
            if (blockEntity instanceof SignBlockEntity) {
                removeSign((SignBlockEntity) blockEntity);
            }
        });
    }
}
