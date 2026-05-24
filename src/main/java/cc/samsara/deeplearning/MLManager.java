package cc.samsara.deeplearning;

import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.Batch;
import ai.djl.training.loss.Loss;
import ai.djl.translate.TranslateException;
import cc.samsara.Samsara;
import cc.samsara.module.impl.combat.KillauraModule;
import cc.samsara.event.EventTarget;
import cc.samsara.event.events.impl.game.RotationEvent;
import cc.samsara.interfaces.IAccess;
import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MLManager implements IAccess {
    @Getter
    private final MLDataRecorder recorder = new MLDataRecorder();
    private Model model;
    private Predictor<float[], float[]> predictor;
    private boolean enabled = false;
    
    private final Path modelPath = Paths.get(mc.gameDirectory.getPath(), "samsara", "ml_model");

    public MLManager() {
        Samsara.getInstance().getEventManager().register(this);
        model = MLRotationModel.createModel();
        try {
            if (java.nio.file.Files.exists(modelPath)) {
                model.load(modelPath);
                predictor = model.newPredictor(MLRotationModel.getTranslator());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled && predictor == null) {
            predictor = model.newPredictor(MLRotationModel.getTranslator());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    @EventTarget
    public void onRotation(RotationEvent event) {
        LivingEntity target = KillauraModule.target;
        if (target == null) {
            target = getTarget();
        }

        if (target != null) {
            if (recorder.isRecording()) {
                recorder.record(target, event.getYaw(), event.getPitch());
            }

            if (enabled && predictor != null) {
                float[] input = new float[]{
                        (float) (target.getX() - mc.player.getX()),
                        (float) (target.getEyeY() - mc.player.getEyeY()),
                        (float) (target.getZ() - mc.player.getZ()),
                        mc.player.getYRot(),
                        mc.player.getXRot()
                };

                try {
                    float[] prediction = predictor.predict(input);
                    event.setYaw(prediction[0]);
                    event.setPitch(prediction[1]);
                } catch (TranslateException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private LivingEntity getTarget() {
        if (mc.hitResult instanceof EntityHitResult ehr) {
            if (ehr.getEntity() instanceof LivingEntity le) {
                return le;
            }
        }
        return null;
    }

    public void train() {
        Path dataPath = Paths.get(mc.gameDirectory.getPath(), "samsara", "ml_data.csv");
        if (!java.nio.file.Files.exists(dataPath)) return;

        try (NDManager manager = NDManager.newBaseManager()) {
            List<float[]> inputs = new ArrayList<>();
            List<float[]> labels = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(dataPath.toFile()))) {
                String line = reader.readLine(); // skip header
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    inputs.add(new float[]{
                        Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]),
                        Float.parseFloat(parts[3]), Float.parseFloat(parts[4])
                    });
                    labels.add(new float[]{Float.parseFloat(parts[5]), Float.parseFloat(parts[6])});
                }
            }

            if (inputs.isEmpty()) return;

            NDArray x = manager.create(inputs.toArray(new float[0][0]));
            NDArray y = manager.create(labels.toArray(new float[0][0]));

            DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.l2Loss());
            try (Trainer trainer = model.newTrainer(config)) {
                trainer.initialize(new ai.djl.ndarray.types.Shape(1, 5));
                for (int epoch = 0; epoch < 100; epoch++) {
                    trainer.trainBatch(new Batch(manager, new NDList(x), new NDList(y), x.getShape().get(0), null, null, 0, 1));
                    trainer.step();
                }
            }
            model.save(modelPath, "rotation-model");
            predictor = model.newPredictor(MLRotationModel.getTranslator());
        } catch (Exception e) {
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
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
