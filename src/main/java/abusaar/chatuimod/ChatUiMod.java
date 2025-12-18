package abusaar.chatuimod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.Style;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;


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
				} else if (text.contains("alliance")) {
					currentChatChannel = "alliance";
					System.out.println("[ChatUiMod] Channel updated to: alliance");
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
			if (screen instanceof ChatScreen chatScreen) {
				// Remember the current focused element (the chat input)
				Element chatInput = chatScreen.getFocused();

				// Prepare positions
				labelW = 70; labelH = 18; labelX = 10; labelY = 10;
				rotateW = 18; rotateH = 18; rotateX = labelX + labelW + 4; rotateY = labelY;

				Text labelText = Text.literal(currentChatChannel.toUpperCase())
					.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(getChannelColor(currentChatChannel) & 0xFFFFFF)));
				labelBtn = new NonFocusableButton(labelX, labelY, labelW, labelH, labelText, b -> {}, chatScreen, chatInput);

				rotateBtn = new NonFocusableButton(rotateX, rotateY, rotateW, rotateH, Text.literal("⟳"),
						b -> sendSwitchCommand(client, chatScreen), chatScreen, chatInput);

				Screens.getButtons(chatScreen).add(labelBtn);
				Screens.getButtons(chatScreen).add(rotateBtn);
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

	private void sendSwitchCommand(MinecraftClient client, ChatScreen chatScreen) {
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
				cmd = "/ac"; // nation -> alliance
				break;
			case "alliance":
				cmd = "/gc"; // alliance -> general
				break;
			default:
				cmd = "/gc";
		}

		System.out.println("[ChatUiMod] Sending command: " + cmd);
		if (client.player != null && client.player.networkHandler != null) {
			client.player.networkHandler.sendChatMessage(cmd);
			// Re-open chat screen to restore focus
			client.setScreen(new ChatScreen("", false));
		}
	}

	private int getChannelColor(String channel) {
		return switch (channel) {
			case "general" -> 0xFFFFFFFF;
			case "local" -> 0xFFFFA500;
			case "nation" -> 0xFFFFFF00;
			case "town" -> 0xFF87CEEB;
			case "alliance" -> 0xFF90EE90;
			case "staff" -> 0xFF00FF00;
			default -> 0xFFFFFFFF;
		};
	}

	class NonFocusableButton extends ButtonWidget {
        private final Screen parent;
        private final Element defaultFocus;

        public NonFocusableButton(int x, int y, int width, int height, Text message,
                                  ButtonWidget.PressAction onPress,
                                  Screen parent, Element defaultFocus) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.parent = parent;
            this.defaultFocus = defaultFocus;
        }

        @Override
        public void setFocused(boolean focused) {
            if (focused) restoreChatFocus();
        }

		public boolean changeFocus(boolean lookForwards) {
		restoreChatFocus();
		return false; // don't keep focus here
	}

        private void restoreChatFocus() {
            if (parent != null && defaultFocus != null) {
                parent.setFocused(defaultFocus);
				parent.setFocused(defaultFocus);
            }
        }

        @Override
        public Selectable.SelectionType getType() {
            return Selectable.SelectionType.NONE;
        }
    }
}
