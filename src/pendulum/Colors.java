package pendulum;

import java.awt.Color;

public enum Colors {
    STOP_RED(new Color(255,0,0)),
    GO_GREEN(new Color(0,255,0)),
    RESET(new Color(255, 127, 80)),
    FOLLOW(new Color(0, 123, 255)),
    TRACE(new Color(138, 43, 226)),
    TAB(new Color(138, 43, 226)),
    ;

    private final Color color;

    Colors(Color color) {
        this.color = color;
    }

    public Color toColor() {
        return color;
    }
}
