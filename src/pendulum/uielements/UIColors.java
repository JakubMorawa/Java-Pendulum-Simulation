package pendulum.uielements;

import java.awt.Color;

public enum UIColors {
    //BUTTONS
    STOP_RED(new Color(255,0,0)),
    GO_GREEN(new Color(0,255,0)),
    RESET(new Color(255, 127, 80)),
    FOLLOW(new Color(0, 123, 255)),
    TRACE(new Color(138, 43, 226)),
    ARROW_BUTTON(new Color(255, 140, 0)),
    PENDULUM_BUTTON(new Color(0, 140, 0)),
    //DATA SET
    TAB(new Color(138, 43, 226)),
    TAB_SELECTED(new Color(250,200,0)),
    UNCHANGEABLE(new Color(247, 243, 104)),
    CHANGEABLE(new Color(255, 253, 173)),
    //VALUE FEILD
    SET(new Color(0,0,200)),
    ACTIVE(new Color(200, 240, 255)),
    ERROR(new Color(200, 0, 0)),
    //GRAPH
    GRAPH_BACKGROUND(new Color(255,255,255)),
    GRID(new Color(200,200,255)), 
    AXES(new Color(0,0,255)),
    ARROW(new Color(200,0,255)),

    BACKGROUND(new Color(62,201,247)),
    VIGNETTE(new Color(0,0,0,125)),

    BOB_COLOR(new Color(255,50,50)),
    BOB_GREEN_COLOR(new Color(50,255,50)),

    LABEL_BACKGROUND(Color.WHITE)
    ;

    private final Color color;

    UIColors(Color color) {this.color = color;}
    
    public Color toColor() {return color;}
}