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

        angularAcceleration = -(gravity / length) * Math.sin(angle);
        angularVelocity += angularAcceleration * deltaTime;
        angularVelocity *= (1 - damping);
        angle += angularVelocity * deltaTime;

        setBobPosition();
    }

    // ----------------------------
    // Drawing
    // ----------------------------
    public void draw(Graphics g, int offsetX, int offsetY) {
        g.setColor(Color.BLACK);
        g.drawLine((int) pivotX + offsetX, (int) pivotY + offsetY, bobX + offsetX, bobY + offsetY);
        g.fillOval(bobX + offsetX - 10, bobY + offsetY - 10, 20, 20);
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
        bobX = (int) (pivotX + length * Math.sin(angle));
        bobY = (int) (pivotY + length * Math.cos(angle));
    }

    // ----------------------------
    // Data Retrieval
    // ----------------------------
    public List<DataElement> getPendulumData() {
        List<DataElement> elements = new ArrayList<>();
        elements.add(new DataElement(true, getLength(), "Length"));
        elements.add(new DataElement(true, getMass(), "Mass"));
        elements.add(new DataElement(true, getPivotX(), "Pivot X"));
        elements.add(new DataElement(true, getPivotY(), "Pivot Y"));
        elements.add(new DataElement(true, getAngle(), "Angle"));
        elements.add(new DataElement(true, getInitialAngle(), "Initial Angle"));
        elements.add(new DataElement(true, getAngularVelocity(), "Angular Velocity"));
        elements.add(new DataElement(true, getInitialAngularVelocity(), "Initial Angular Velocity"));
        elements.add(new DataElement(false, getAngularAcceleration(), "Angular Acceleration"));
        elements.add(new DataElement(false, getBobX(), "Bob X"));
        elements.add(new DataElement(false, getBobY(), "Bob Y"));
        elements.add(new DataElement(false, PendulumPanel.getTime(), "Time"));
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