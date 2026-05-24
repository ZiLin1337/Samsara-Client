package cc.samsara.commands.commands.client;

import cc.samsara.Samsara;
import cc.samsara.commands.Command;
import cc.samsara.deeplearning.MLManager;
import cc.samsara.util.render.ChatUtil;

public class MLCommand extends Command {
    public MLCommand() {
        super(new String[]{"ml"}, "Controls for the ML rotation system. Usage: .ml <record/train/toggle>");
    }

    @Override
    public void execute(String[] args, String message) {
        if (args.length < 2) {
            ChatUtil.print("Usage: .ml <record/train/toggle>");
            return;
        }

        MLManager mlManager = Samsara.getInstance().getMlManager();
        String action = args[1].toLowerCase();

        switch (action) {
            case "record" -> {
                boolean recording = !mlManager.getRecorder().isRecording();
                mlManager.getRecorder().setRecording(recording);
                ChatUtil.print("ML Recording: " + (recording ? "Enabled" : "Disabled"));
            }
            case "train" -> {
                ChatUtil.print("ML Training started...");
                new Thread(() -> {
                    mlManager.train();
                    ChatUtil.print("ML Training finished and model saved.");
                }).start();
            }
            case "toggle" -> {
                boolean enabled = !mlManager.isEnabled();
                mlManager.setEnabled(enabled);
                ChatUtil.print("ML Rotation System: " + (enabled ? "Enabled" : "Disabled"));
            }
            default -> ChatUtil.print("Unknown action. Use record, train, or toggle.");
        }
    }
}
