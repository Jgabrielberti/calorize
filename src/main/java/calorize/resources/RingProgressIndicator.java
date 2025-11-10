package calorize.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.css.converter.SizeConverter;
import javafx.css.converter.StringConverter;

/**
 * Progress indicator showing a filling arc.
 *
 * @author Andrea Vacondio
 *
 */
public class RingProgressIndicator extends ProgressCircleIndicator {

    // Removed: private DoubleProperty progress = new SimpleDoubleProperty(0.0);
    // The 'progress' property and its methods are assumed to be inherited from ProgressCircleIndicator.

    // --- New property for label (unique to RingProgressIndicator) ---
    private StringProperty label = new SimpleStringProperty(""); // Text label to display

    public RingProgressIndicator() {
        this.getStylesheets().add(RingProgressIndicator.class.getResource("ringprogress.css").toExternalForm());
        this.getStyleClass().add("ringindicator");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new RingProgressIndicatorSkin(this);
    }

    // --- Progress Property Methods ---
    // Do NOT redefine progressProperty(), setProgress(), getProgress() here if they are inherited.
    // They will be accessed directly from the inherited methods (e.g., this.setProgress(value)).

    // --- Label Property Methods ---
    public final StringProperty labelProperty() {
        return label;
    }

    public final void setLabel(String value) {
        labelProperty().set(value);
    }

    public final String getLabel() {
        return labelProperty().get();
    }


    public final void setRingWidth(int value) {
        ringWidthProperty().set(value);
    }

    public final DoubleProperty ringWidthProperty() {
        return ringWidth;
    }

    public final double getRingWidth() {
        return ringWidthProperty().get();
    }

    /**
     * thickness of the ring indicator.
     */
    private DoubleProperty ringWidth = new StyleableDoubleProperty(22) {
        @Override
        public Object getBean() {
            return RingProgressIndicator.this;
        }

        @Override
        public String getName() {
            return "ringWidth";
        }

        @Override
        public CssMetaData<RingProgressIndicator, Number> getCssMetaData() {
            return StyleableProperties.RING_WIDTH;
        }
    };

    private static class StyleableProperties {
        private static final CssMetaData<RingProgressIndicator, Number> RING_WIDTH = new CssMetaData<RingProgressIndicator, Number>(
                "-fx-ring-width", SizeConverter.getInstance(), 22.0) {

            @Override
            public boolean isSettable(RingProgressIndicator n) {
                return n.ringWidth == null || !n.ringWidth.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(RingProgressIndicator n) {
                return (StyleableProperty<Number>) n.ringWidth;
            }
        };

        private static final CssMetaData<RingProgressIndicator, String> LABEL = new CssMetaData<RingProgressIndicator, String>(
                "-fx-label", StringConverter.getInstance(), "") {

            @Override
            public boolean isSettable(RingProgressIndicator n) {
                return n.label == null || !n.label.isBound();
            }

            @Override
            public StyleableProperty<String> getStyleableProperty(RingProgressIndicator n) {
                return (StyleableProperty<String>) n.labelProperty();
            }
        };


        public static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.addAll(ProgressCircleIndicator.getClassCssMetaData()); // Add parent's CSS metadata
            styleables.add(RING_WIDTH);
            styleables.add(LABEL); // Add the label CSS metadata
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }
}