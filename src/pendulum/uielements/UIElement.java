package pendulum.uielements;

public interface UIElement {
    public int getPreferredWidth();
    public int getPreferredHeight();
    public void setBounds(int x, int y, int width, int height);
    public int getOffsetX();
    public int getOffsetY();
    public void setOffset(int xOffsetX, int getOffsetY);
}