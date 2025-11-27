package pendulum;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Pendulum {

    // ----------------------------
    // Fields
    // ----------------------------
    private double length;
    private double mass;
    private double pivotX;
    private double pivotY;
    private double angle;
    private double initialAngle;
    private double angularVelocity;
    private double initialAngularVelocity;
    private double angularAcceleration;

    private int bobX;
    private int bobY;

    // ----------------------------
    // Constructor
    // ----------------------------
    public Pendulum(double length, double mass, double pivotX, double pivotY, double initialAngle, double initialAngularVelocity) {
        this.length = length;
        this.mass = mass;
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.angle = initialAngle;
        this.initialAngle = initialAngle;
        this.angularVelocity = initialAngularVelocity;
        this.initialAngularVelocity = initialAngularVelocity;
        this.angularAcceleration = 0;
        setBobPosition();
    }

    // ----------------------------
    // Simulation Update
    // ----------------------------
    public void update(double deltaTime) {
        double gravity = 9.81; // gravitational acceleration
        double damping = 0.0; // friction coefficient, tweak as needed

        angularAcceleration = -(gravity / length) * Math.sin(angle%(2*Math.PI));
        angularVelocity += angularAcceleration * deltaTime;
        angularVelocity *= (1 - damping);
        angle += angularVelocity * deltaTime;

        setBobPosition();
    }

    // ----------------------------
    // Drawing
    // ----------------------------
    public void draw(Graphics g, int offsetX, int offsetY, double zoom) {
    Graphics2D g2 = (Graphics2D) g;

    // Scale endpoints
    int scaledPivotX = (int) (pivotX * zoom) + offsetX;
    int scaledPivotY = (int) (pivotY * zoom) + offsetY;
    int scaledBobX   = (int) (bobX * zoom)   + offsetX;
    int scaledBobY   = (int) (bobY * zoom)   + offsetY;

    // Draw rod
    g2.setColor(Color.BLACK);
    g2.setStroke(new BasicStroke((int)(2*zoom)));
    g2.drawLine(scaledPivotX, scaledPivotY, scaledBobX, scaledBobY);

    // Scale bob size
    int scaledDiameter = (int) (20 * zoom);
    int scaledRadiusOffset = scaledDiameter / 2;

    // Draw bob (circle)
    Utils.drawCircle(
        g2,
        scaledBobX - scaledRadiusOffset,
        scaledBobY - scaledRadiusOffset,
        scaledDiameter,
        scaledDiameter,
        Color.RED,
        Color.BLACK,
        (float) (2f * zoom)  // border width also scales (optional)
    );
}

    // ----------------------------
    // Reset
    // ----------------------------
    public void reset() {
        angle = initialAngle;
        angularVelocity = initialAngularVelocity;
        setBobPosition();
    }

    // ----------------------------
    // Bob Position
    // ----------------------------
    private void setBobPosition() {
        bobX = (int) (pivotX + length * Math.sin(angle%(2*Math.PI)));
        bobY = (int) (pivotY + length * Math.cos(angle%(2*Math.PI)));
    }

    // ----------------------------
    // Data Retrieval
    // ----------------------------
    public List<DataElement> getPendulumData() {
        List<DataElement> elements = new ArrayList<>();
        elements.add(new DataElement(false, PendulumPanel.getTime(), "Time"));
        elements.add(new DataElement(false, getAngularAcceleration(), "Angular Acceleration"));
        elements.add(new DataElement(false, getBobX(), "Bob X"));
        elements.add(new DataElement(false, getBobY(), "Bob Y"));
        elements.add(new DataElement(true, getLength(), "Length"));
        elements.add(new DataElement(true, getMass(), "Mass"));
        elements.add(new DataElement(true, getPivotX(), "Pivot X"));
        elements.add(new DataElement(true, getPivotY(), "Pivot Y"));
        elements.add(new DataElement(true, getAngle(), "Angle"));
        elements.add(new DataElement(true, getInitialAngle(), "Initial Angle"));
        elements.add(new DataElement(true, getAngularVelocity(), "Angular Velocity"));
        elements.add(new DataElement(true, getInitialAngularVelocity(), "Initial Angular Velocity"));
        
        return elements;
    }

    // ----------------------------
    // Getters
    // ----------------------------
    public double getLength() { return length; }
    public double getMass() { return mass; }
    public double getPivotX() { return pivotX; }
    public double getPivotY() { return pivotY; }
    public double getAngle() { return angle; }
    public double getInitialAngle() { return initialAngle; }
    public double getAngularVelocity() { return angularVelocity; }
    public double getInitialAngularVelocity() { return initialAngularVelocity; }
    public double getAngularAcceleration() { return angularAcceleration; }
    public int getBobX() { return bobX; }
    public int getBobY() { return bobY; }

    public double getVelocityX() {return angularVelocity*length*Math.cos(angle%(2*Math.PI));}
    public double getVelocityY() {return angularVelocity*length*Math.sin(angle%(2*Math.PI));}

    // ----------------------------
    // Setters
    // ----------------------------
    public void setLength(double length) { this.length = length; }
    public void setMass(double mass) { this.mass = mass; }
    public void setPivotX(double pivotX) { this.pivotX = pivotX; }
    public void setPivotY(double pivotY) { this.pivotY = pivotY; }
    public void setAngle(double angle) { this.angle = angle; }
    public void setInitialAngle(double initialAngle) { this.initialAngle = initialAngle; }
    public void setAngularVelocity(double angularVelocity) { this.angularVelocity = angularVelocity; }
    public void setInitialAngularVelocity(double initialAngularVelocity) { this.initialAngularVelocity = initialAngularVelocity; }
}