package com.ironsmagiccircle.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Smooth numeric slider for the Magic Circle Configuration Screen.
 */
public class DoubleSlider extends AbstractSliderButton {
    private final double min;
    private final double max;
    private final Consumer<Double> onApply;
    private final Function<Double, Component> messageProvider;

    public DoubleSlider(
            int x,
            int y,
            int width,
            int height,
            double min,
            double max,
            double initialValue,
            Function<Double, Component> messageProvider,
            Consumer<Double> onApply
    ) {
        super(x, y, width, height, Component.empty(), Mth.clamp((initialValue - min) / (max - min), 0.0, 1.0));
        this.min = min;
        this.max = max;
        this.messageProvider = messageProvider;
        this.onApply = onApply;
        updateMessage();
    }

    public double getActualValue() {
        return min + this.value * (max - min);
    }

    public void setActualValue(double actualValue) {
        this.value = Mth.clamp((actualValue - min) / (max - min), 0.0, 1.0);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(messageProvider.apply(getActualValue()));
    }

    @Override
    protected void applyValue() {
        onApply.accept(getActualValue());
    }
}

