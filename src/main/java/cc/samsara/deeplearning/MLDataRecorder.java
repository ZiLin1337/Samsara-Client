package cc.samsara.deeplearning;

import cc.samsara.interfaces.IAccess;
import net.minecraft.world.entity.Entity;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MLDataRecorder implements IAccess {
    private static final Path DATA_PATH = Paths.get(mc.gameDirectory.getPath(), "samsara", "ml_data.csv");
    private boolean recording = false;

    public void setRecording(boolean recording) {
        this.recording = recording;
        if (recording) {
            try {
                Files.createDirectories(DATA_PATH.getParent());
                if (!Files.exists(DATA_PATH)) {
                    try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_PATH.toFile()))) {
                        writer.println("relX,relY,relZ,currYaw,currPitch,targetYaw,targetPitch");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRecording() {
        return recording;
    }

    public void record(Entity target, float targetYaw, float targetPitch) {
        if (!recording || target == null) return;

        double relX = target.getX() - mc.player.getX();
        double relY = target.getEyeY() - mc.player.getEyeY();
        double relZ = target.getZ() - mc.player.getZ();

        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_PATH.toFile(), true))) {
            writer.printf("%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f%n",
                    relX, relY, relZ,
                    mc.player.getYRot(), mc.player.getXRot(),
                    targetYaw, targetPitch);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
