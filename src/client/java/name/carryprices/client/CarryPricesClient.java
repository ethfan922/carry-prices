package name.carryprices.client;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;


import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class CarryPricesClient implements ClientModInitializer {
	public static final String MOD_ID = "carry-prices";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Integer[] carryPriceMap(String argFloor){
		Map<String, Integer[]> priceMap = new HashMap<>();
		priceMap.put("f4", new Integer[]{600000, 850000, 0, 600000, 700000, 0});
		priceMap.put("f5", new Integer[]{600000, 750000, 1200000, 600000, 650000, 1000000});
		priceMap.put("f6", new Integer[]{700000, 1000000, 1400000, 700000, 850000, 1200000});
		priceMap.put("f7", new Integer[]{5000000, 9500000, 12000000, 4000000, 8250000, 10500000});
		return priceMap.get(argFloor);
	}

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommands.literal("carry")
				.then(ClientCommands.argument("Floor", StringArgumentType.string())
					.then(ClientCommands.argument("Amount", IntegerArgumentType.integer())
						.then(ClientCommands.argument("Score", StringArgumentType.string())
							.executes(context -> executeCarry(
								StringArgumentType.getString(context, "Floor"),
									IntegerArgumentType.getInteger(context, "Amount"),
									StringArgumentType.getString(context, "Score"),
									"",context))
										.then(ClientCommands.argument("Warp?", StringArgumentType.greedyString())
											.executes(context -> executeCarry(
												StringArgumentType.getString(context, "Floor"),
												IntegerArgumentType.getInteger(context, "Amount"),
												StringArgumentType.getString(context, "Score"),
												StringArgumentType.getString(context, "Warp?"),
												context)))))
							)
					);
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommands.literal("carryb")
					.then(ClientCommands.argument("Floor", StringArgumentType.string())
							.then(ClientCommands.argument("Amount", IntegerArgumentType.integer())
									.then(ClientCommands.argument("Score", StringArgumentType.string())
											.executes(context -> executeCarryb(
													StringArgumentType.getString(context, "Floor"),
													IntegerArgumentType.getInteger(context, "Amount"),
													StringArgumentType.getString(context, "Score"),
													"",context))
											.then(ClientCommands.argument("Warp?", StringArgumentType.greedyString())
													.executes(context -> executeCarryb(
															StringArgumentType.getString(context, "Floor"),
															IntegerArgumentType.getInteger(context, "Amount"),
															StringArgumentType.getString(context, "Score"),
															StringArgumentType.getString(context, "Warp?"),
															context)))))
					)
			);
		});

		LOGGER.info("Hello Fabric world!");
	}

	private static int executeCarry(String argFloor, int amount, String argScore, String warp, CommandContext<FabricClientCommandSource> context) {
		int price;
		String floor = argFloor.toLowerCase();
		String score = argScore.toLowerCase();
		String scoreFormatted;
		Integer[] floorPrices = carryPriceMap(floor);

		if(amount < 5){
            price = switch (score) {
                case "c" -> {
                    scoreFormatted = "comp";
                    yield floorPrices[0] * amount;
                }
                case "s" -> {
                    scoreFormatted = "S";
                    yield floorPrices[1] * amount;
                }
                case "s+" -> {
                    scoreFormatted = "S+";
                    yield floorPrices[2] * amount;
                }
                default -> throw new IllegalArgumentException("Invalid score.");
            };
		} else {
            price = switch (score) {
                case "c" -> {
                    scoreFormatted = "comp";
                    yield floorPrices[3] * amount;
                }
                case "s" -> {
                    scoreFormatted = "S";
                    yield floorPrices[4] * amount;
                }
                case "s+" -> {
                    scoreFormatted = "S+";
                    yield floorPrices[5] * amount;
                }
                default -> throw new IllegalArgumentException("Invalid score.");
            };
		}

		String truncatedPrice;
		DecimalFormat format = new DecimalFormat("0.#");

		if(price < 1000000){
			truncatedPrice = format.format((price/1000.0))+"k";
		} else {
			truncatedPrice = format.format((price/1000000.0))+"m";
		}

		String finalMessage;

		if(amount <= 1){
			finalMessage = "pc "+amount + " " + scoreFormatted + " run costs " + truncatedPrice + ".";
		}else{
			finalMessage = "pc "+amount + " " + scoreFormatted + " runs cost " + truncatedPrice + ".";
		}

		if (Objects.equals(warp, "w") || Objects.equals(warp, "?")){
			finalMessage += " May I warp?";
		}

		Minecraft client = Minecraft.getInstance();
		if (client.player != null && client.getConnection() != null) {
			ClientPacketListener connection = client.getConnection();
			connection.sendCommand(finalMessage);
		}

		MutableComponent rememberRecordingMessage = Component.literal("REMEMBER TO START RECORDING!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
		context.getSource().sendFeedback(rememberRecordingMessage);
		return 1;
	}

	private static int executeCarryb(String argFloor, int amount, String argScore, String warp, CommandContext<FabricClientCommandSource> context) {
		int price;
		String floor = argFloor.toLowerCase();
		String score = argScore.toLowerCase();
		String scoreFormatted;
		Integer[] floorPrices = carryPriceMap(floor);

		price = switch (score) {
			case "c" -> {
				scoreFormatted = "comp";
				yield floorPrices[3] * amount;
			}
			case "s" -> {
				scoreFormatted = "S";
				yield floorPrices[4] * amount;
			}
			case "s+" -> {
				scoreFormatted = "S+";
				yield floorPrices[5] * amount;
			}
			default -> throw new IllegalArgumentException("Invalid score.");
		};

		String truncatedPrice;
		DecimalFormat format = new DecimalFormat("0.#");

		if(price < 1000000){
			truncatedPrice = format.format((price/1000.0))+"k";
		} else {
			truncatedPrice = format.format((price/1000000.0))+"m";
		}

		String finalMessage;

		if(amount <= 1){
			finalMessage = "pc "+amount + " " + scoreFormatted + " run costs " + truncatedPrice + ".";
		}else{
			finalMessage = "pc "+amount + " " + scoreFormatted + " runs cost " + truncatedPrice + ".";
		}

		if (Objects.equals(warp, "w") || Objects.equals(warp, "?")){
			finalMessage += " May I warp?";
		}

		Minecraft client = Minecraft.getInstance();
		if (client.player != null && client.getConnection() != null) {
			ClientPacketListener connection = client.getConnection();
			connection.sendCommand(finalMessage);
		}

		MutableComponent rememberRecordingMessage = Component.literal("REMEMBER TO START RECORDING!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
		context.getSource().sendFeedback(rememberRecordingMessage);
		return 1;
	}


	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
