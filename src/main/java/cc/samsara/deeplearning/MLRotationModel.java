package cc.samsara.deeplearning;

import ai.djl.Model;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.nn.Activation;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.Trainer;
import ai.djl.training.loss.Loss;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

public class MLRotationModel {
    public static Model createModel() {
        Model model = Model.newInstance("rotation-model");
        SequentialBlock block = new SequentialBlock();
        block.add(Linear.builder().setUnits(64).build());
        block.add(Activation.reluBlock());
        block.add(Linear.builder().setUnits(32).build());
        block.add(Activation.reluBlock());
        block.add(Linear.builder().setUnits(2).build()); // Output: yaw, pitch
        model.setBlock(block);
        return model;
    }

    public static Translator<float[], float[]> getTranslator() {
        return new Translator<float[], float[]>() {
            @Override
            public NDList processInput(TranslatorContext ctx, float[] input) {
                return new NDList(ctx.getNDManager().create(input));
            }

            @Override
            public float[] processOutput(TranslatorContext ctx, NDList list) {
                return list.get(0).toFloatArray();
            }

            @Override
            public Batchifier getBatchifier() {
                return Batchifier.STACK;
            }
        };
    }
}
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
/home/engine/.bashrc: line 1: syntax error near unexpected token `('
/home/engine/.bashrc: line 1: `. /etc/profile.d/workload-containment.shn# ~/.bashrc: executed by bash(1) for non-login shells.'
