package pendulum;

import java.awt.Color;

public enum Colors {
    //TOP BUTTONS
    STOP_RED(new Color(255,0,0)),
    GO_GREEN(new Color(0,255,0)),
    RESET(new Color(255, 127, 80)),
    FOLLOW(new Color(0, 123, 255)),
    TRACE(new Color(138, 43, 226)),
    ARROW_BUTTON(new Color(255, 140, 0)),
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
    ;

    private final Color color;

    Colors(Color color) {this.color = color;}
    
    public Color toColor() {return color;}
}