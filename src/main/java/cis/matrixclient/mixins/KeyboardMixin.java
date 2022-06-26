package cis.matrixclient.mixins;

import cis.matrixclient.MatrixClient;
import cis.matrixclient.event.events.client.KeyEvent;
import cis.matrixclient.feature.command.commands.BindSettingCommand;
import cis.matrixclient.feature.manager.SettingManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.key.Input;
import cis.matrixclient.util.key.KeyBinds;
import cis.matrixclient.util.key.Keybind;
import cis.matrixclient.util.player.ChatUtils;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cis.matrixclient.MatrixClient.mc;
import static cis.matrixclient.feature.command.commands.BindCommand.waitModule;
import static cis.matrixclient.feature.command.commands.BindCommand.waiting;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    private boolean f3down = false;

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKeyPress(long window, int key, int scancode, int i, int j, CallbackInfo info) {
        if (key != GLFW.GLFW_KEY_UNKNOWN) {
            KeyEvent event = new KeyEvent(key, KeyBinds.Action.get(i));
            MatrixClient.EVENT_BUS.post(event);

            if (key == 292 && (event.action == KeyBinds.Action.PRESS || event.action == KeyBinds.Action.RELEASE)) f3down = !f3down;

            if (event.isCancelled()) info.cancel();
            if (!f3down) {
                if (!waiting) for (Module module : ModuleManager.getModules()) {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (module.bind.get() == key && mc.currentScreen == null && event.action == KeyBinds.Action.PRESS)
                        module.toggle();
                }
                else if (event.action == KeyBinds.Action.PRESS) {
                    info.cancel();
                    waiting = false;
                    if (key == 256) {
                        waitModule.bind.set(true, -1);
                        ChatUtils.info("Bind of " + waitModule.getName() + " set to None");
                        return;
                    }
                    waitModule.bind.set(true, key);
                    ChatUtils.info("Bind of " + waitModule.getName() + " set to " + KeyBinds.getKeyName(key));
                }
                if (BindSettingCommand.waiting){
                    info.cancel();
                    BindSettingCommand.waiting = false;
                    if (key == 256) {
                        BindSettingCommand.waitSetting.setValue(new Keybind(true, -1));
                        ChatUtils.info("Bind of " + BindSettingCommand.waitSetting.getName() + " set to None");
                        return;
                    }
                    BindSettingCommand.waitSetting.setValue(new Keybind(true, key));
                    ChatUtils.info("Bind of " + BindSettingCommand.waitSetting.getName() + " set to " + KeyBinds.getKeyName(key));
                }
                else {
                    for (Setting<?> setting : SettingManager.getSettings(Setting.Type.Keybind)) {
                        if (((Setting<Keybind>) setting).get().get() == key && mc.currentScreen == null && event.action == KeyBinds.Action.PRESS){
                            ((Setting<Keybind>) setting).get().setPressed(true);
                        }
                        else if (!Input.isKeyPressed(((Setting<Keybind>) setting).get().get())){
                            ((Setting<Keybind>) setting).get().setPressed(false);
                        }
                    }
                }
            }
        }
    }
}