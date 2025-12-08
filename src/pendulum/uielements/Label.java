package pendulum.uielements;
import java.awt.*;

public class Label implements UIElement{
    private String text;
    private int x;
    private int y;
    private int width;
    private int height;
    private final int fontSize;
    private final Color color;
    private final int labelType;
    private final int borderWidth;
    private final int arc = 20;
    public static final int TYPE_SQUARE = 0;
    public static final int TYPE_ROUND = 1;
    private int offsetX = 0;
    private int offsetY = 0;

    public Label(int x, int y, int width, int height, int fontSize, Color color, int labelType, int borderWidth) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fontSize = fontSize;
        this.color = color;
        this.labelType = labelType;
        this.borderWidth = borderWidth;
    }

    public Label(int x, int y, int width, int height, int fontSize, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fontSize = fontSize;
        this.color = color;
        this.labelType = 0;
        this.borderWidth = 2;
    }

    public void draw(Graphics2D g2){
        if(labelType == TYPE_ROUND){
            Utils.drawRoundSquare(g2,x,y,width,height,color,0.3f, 0.2, arc, borderWidth);
            g2.setColor(Color.BLACK);
            Utils.drawTextCentered(g2,text,x,y,width,height,fontSize); 
        }else{
            Utils.drawBox(g2,x,y,width,height,color);
            g2.setColor(Color.BLACK);
            Utils.drawTextInBox(g2,text,x,y,width,height,fontSize); 
        } 
    }

    public void setText(String text){
        this.text = text;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getPreferredWidth() {
        return width;
    }

    @Override
    public int getPreferredHeight() {
        return height;
    }

    @Override
    public int getOffsetX() {
        return offsetX;
    }

    @Override
    public int getOffsetY() {
        return offsetY;
    }

    @Override
    public void setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
}
