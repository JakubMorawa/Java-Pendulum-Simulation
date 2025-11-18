package pendulum;
import java.awt.*;

public class Label {
    String text;
    int x;
    int y;
    int width;
    int height;
    int fontSize;
    Color color;

    public Label(int x, int y, int width, int height, int fontSize, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fontSize = fontSize;
        this.color = color;
    }

    public void draw(Graphics2D g2){
        Utils.drawBox(g2,x,y,width,height,color);
        Utils.drawTextInBox(g2,text,x,y,width,height,fontSize);
    }

    public void setText(String text){
        this.text = text;
    }
}
