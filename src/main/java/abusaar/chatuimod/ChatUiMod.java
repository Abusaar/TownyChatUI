package abusaar.chatuimod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.Style;

@Environment(EnvType.CLIENT)
public class ChatUiMod implements ClientModInitializer {
	private static String currentChatChannel = "general";

	// Cached button positions for click handling
	private static int labelX, labelY, labelW, labelH;
	private static int rotateX, rotateY, rotateW, rotateH;

	private ButtonWidget labelBtn;
	private ButtonWidget rotateBtn;

	@Override
	public void onInitializeClient() {
		System.out.println("[ChatUiMod] Initializing...");
		
		// Register chat message listener for game chat
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (overlay) return;

			String text = message.getString().toLowerCase();
			System.out.println("[ChatUiMod] Message received: " + text);

			if (text.contains("[townychat]")) {
				System.out.println("[ChatUiMod] Found [townychat] in message");
				
				if (text.contains("staff")) {
					currentChatChannel = "staff";
					System.out.println("[ChatUiMod] Channel updated to: staff");
					updateLabelButtonText();
				} else if (text.contains("general")) {
					currentChatChannel = "general";
					System.out.println("[ChatUiMod] Channel updated to: general");
					updateLabelButtonText();
				} else if (text.contains("local")) {
					currentChatChannel = "local";
					System.out.println("[ChatUiMod] Channel updated to: local");
					updateLabelButtonText();
				} else if (text.contains("nation")) {
					currentChatChannel = "nation";
					System.out.println("[ChatUiMod] Channel updated to: nation");
					updateLabelButtonText();
				} else if (text.contains(" town")) {
					currentChatChannel = "town";
					System.out.println("[ChatUiMod] Channel updated to: town");
					updateLabelButtonText();
				} else {
					System.out.println("[ChatUiMod] No channel keyword found in message");
					return;
				}
			}
		});

		// Register screen event to render on chat screen
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof ChatScreen) {
				// Prepare positions
				labelW = 70; labelH = 18; labelX = 10; labelY = 10;
				rotateW = 18; rotateH = 18; rotateX = labelX + labelW + 4; rotateY = labelY;

				// Create vanilla-style label button with colored text
				Text labelText = Text.literal(currentChatChannel.toUpperCase())
					.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(getChannelColor(currentChatChannel) & 0xFFFFFF)));
				labelBtn = ButtonWidget.builder(labelText, b -> {})
					.dimensions(labelX, labelY, labelW, labelH)
					.build();

				// Create vanilla-style rotate button
				rotateBtn = ButtonWidget.builder(Text.literal("⟳"), b -> sendSwitchCommand(client))
					.dimensions(rotateX, rotateY, rotateW, rotateH)
					.build();

				Screens.getButtons((Screen) screen).add(labelBtn);
				Screens.getButtons((Screen) screen).add(rotateBtn);
			}
		});
	}

	private void updateLabelButtonText() {
		if (labelBtn != null && currentChatChannel != null) {
			Text labelText = Text.literal(currentChatChannel.toUpperCase())
				.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(getChannelColor(currentChatChannel) & 0xFFFFFF)));
			labelBtn.setMessage(labelText);
		}
	}

	private void sendSwitchCommand(MinecraftClient client) {
		String cmd;
		switch (currentChatChannel) {
			case "staff":
				cmd = "/gc"; // go to general
				break;
			case "general":
				cmd = "/lc"; // general -> local
				break;
			case "local":
				cmd = "/tc"; // local -> town
				break;
			case "town":
				cmd = "/nc"; // town -> nation
				break;
			case "nation":
				cmd = "/gc"; // nation -> general
				break;
			default:
				cmd = "/gc";
		}

		System.out.println("[ChatUiMod] Sending command: " + cmd);
		if (client.player != null && client.player.networkHandler != null) {
			client.player.networkHandler.sendChatMessage(cmd);
		}
	}

	private int getChannelColor(String channel) {
		return switch (channel) {
			case "general" -> 0xFFFFFFFF;
			case "local" -> 0xFFFFA500;
			case "nation" -> 0xFFFFFF00;
			case "town" -> 0xFF87CEEB;
			case "staff" -> 0xFF00FF00;
			default -> 0xFFFFFFFF;
		};
	}
}
