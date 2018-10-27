package org.firstinspires.ftc.teamcode.systems;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.components.scale.ExponentialRamp;
import org.firstinspires.ftc.teamcode.components.scale.IScale;
import org.firstinspires.ftc.teamcode.components.scale.LinearScale;
import org.firstinspires.ftc.teamcode.components.scale.Point;
import org.firstinspires.ftc.teamcode.components.scale.Ramp;
import org.firstinspires.ftc.teamcode.systems.BaseSystems.DriveSystem4Wheel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MecanumDriveSystem extends DriveSystem4Wheel {

    private static final double TICKS_TO_INCHES = 100;
    private final IScale JOYSTICK_SCALE = new LinearScale(0.62, 0);
    private static double TURN_RAMP_POWER_CUTOFF = 0.1;
    private static double RAMP_POWER_CUTOFF = 0.1;

    public IMUSystem imuSystem;

    Telemetry.Item distanceItem;
    Telemetry.Item powerItem;

    private double initialHeading;

    public MecanumDriveSystem(OpMode opMode) {
        super(opMode, "MecanumDrive");

        telem("about to start imu");
        imuSystem = new IMUSystem(opMode);
        initialHeading = Math.toRadians(imuSystem.getHeading());
        telem("started imu");

        powerItem = telemetry.addData("power", 0);
        distanceItem = telemetry.addData("distance", 0);
    }

    private void telem(String message) {
        telemetry.addLine(message);
        telemetry.update();
    }

    public void mecanumDrive(float rightX, float rightY, float leftX, float leftY, boolean slowDrive) {
        rightX = Range.clip(rightX, -1, 1);
        leftX = Range.clip(leftX, -1, 1);
        leftY = Range.clip(leftY, -1, 1);

        rightX = scaleJoystickValue(rightX);
        leftX = scaleJoystickValue(leftX);
        leftY = scaleJoystickValue(leftY);

        // write the values to the motors
        double frontRightPower = leftY + rightX + leftX;
        double backRightPower = leftY + rightX - leftX;
        double frontLeftPower = leftY - rightX - leftX;
        double backLeftPower = leftY - rightX + leftX;
        this.motorFrontRight.setPower(Range.clip(frontRightPower, -1, 1));
        telemetry.addLine("FRpower: " +  Range.clip(frontRightPower, -1, 1));
        this.motorBackRight.setPower(Range.clip(backRightPower, -1, 1));
        telemetry.addLine("BRpower: " +  Range.clip(backRightPower, -1, 1));
        this.motorFrontLeft.setPower(Range.clip(frontLeftPower - leftX, -1, 1));
        telemetry.addLine("FLpower: " +  Range.clip(frontLeftPower - leftX, -1, 1));
        this.motorBackLeft.setPower(Range.clip(backLeftPower + leftX, -1, 1));
        telemetry.addLine("BLpower: " +  Range.clip(backLeftPower + leftX, -1, 1));
        telemetry.update();
    }

    private float scaleJoystickValue(float joystickValue) {
        return joystickValue > 0
                ? (float)JOYSTICK_SCALE.scaleX(joystickValue * joystickValue)
                : (float)-JOYSTICK_SCALE.scaleX(joystickValue * joystickValue);
    }

    public void driveGodMode(float rightX, float rightY, float leftX, float leftY) {
        driveGodMode(rightX, rightY, leftX, leftY, 1);
    }

    public void driveGodMode(float rightX, float rightY, float leftX, float leftY, float coeff) {
        double currentHeading = Math.toRadians(imuSystem.getHeading());
        double headingDiff = initialHeading - currentHeading;

        rightX = scaleJoystickValue(rightX);
        leftX = scaleJoystickValue(leftX);
        leftY = scaleJoystickValue(leftY);

        double speed = Math.sqrt(leftX * leftX + leftY * leftY);
        double angle = Math.atan2(leftX, leftY) + (Math.PI / 2) + headingDiff;
        double changeOfDirectionSpeed = rightX;
        double x = coeff * speed * Math.cos(angle);
        double y = coeff * speed * Math.sin(angle);

        double frontLeft = y - changeOfDirectionSpeed + x;
        double frontRight = y + changeOfDirectionSpeed - x;
        double backLeft = y - changeOfDirectionSpeed - x;
        double backRight = y + changeOfDirectionSpeed + x;

        List<Double> powers = Arrays.asList(frontLeft, frontRight, backLeft, backRight);
        clampPowers(powers);

        motorFrontLeft.setPower(powers.get(0));
        motorFrontRight.setPower(powers.get(1));
        motorBackLeft.setPower(powers.get(2));
        motorBackRight.setPower(powers.get(3));
    }

    private void clampPowers(List<Double> powers) {
        double minPower = Collections.min(powers);
        double maxPower = Collections.max(powers);
        double maxMag = Math.max(Math.abs(minPower), Math.abs(maxPower));

        if (maxMag > 1.0)
        {
            for (int i = 0; i < powers.size(); i++)
            {
                powers.set(i, powers.get(i) / maxMag);
            }
        }
    }

    public void mecanumDriveXY(double x, double y) {
        this.motorFrontRight.setPower(Range.clip(y + x, -1, 1));
        this.motorBackRight.setPower(Range.clip(y - x, -1, 1));
        this.motorFrontLeft.setPower(Range.clip(y - x, -1, 1));
        this.motorBackLeft.setPower(Range.clip(y + x, -1, 1));
    }

    public void mecanumDrivePolar(double radians, double power) {
        double x = Math.cos(radians) * power;
        double y = Math.sin(radians) * power;
        mecanumDriveXY(x, y);
    }

    public void driveToPositionInches(double ticks, double power) {
        telemetry.addLine("BR motor position pre stopAnd: " + motorBackRight.getCurrentPosition());
        telemetry.update();
        setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        telemetry.addLine("BR motor position post stopAnd: " + motorBackRight.getCurrentPosition());
        telemetry.update();
        telemetry.addLine("runmode post stopAndReset: " + motorBackRight.getMode());
        telemetry.update();
        setRunMode(DcMotor.RunMode.RUN_TO_POSITION);
        telemetry.addLine("runmode post toPosition: " + motorBackRight.getMode());
        telemetry.update();


        ////////////
        // Ramp the power from power to RAMP_POWER_CUTOFF from (ticks / 10) (changed from rampLength) to 0
        Ramp ramp = new ExponentialRamp(new Point(0, RAMP_POWER_CUTOFF), new Point(ticks, power));

        telemetry.addLine("BR position pre setPos: " + motorBackRight.getCurrentPosition());
        telemetry.update();
        telemetry.addLine("setting target positio: " + (int) ticks);
        telemetry.update();
        setTargetPosition((int) ticks);
        telemetry.addLine("post setPos BR target position set to: " + motorBackRight.getTargetPosition());
        telemetry.update();
        telemetry.addLine("BR pos post setPos: " + motorBackRight.getCurrentPosition());
        telemetry.update();


        setPower(power);
        powerItem.setValue(power);
        telemetry.addLine("set powery boy to: " + power);
        telemetry.update();

        boolean meep = false;
        meep = anyMotorsBusy();
        telemetry.addLine("anymotorsBusy(): " + meep);
        telemetry.update();

        while (anyMotorsBusy()) {
            int distance = getDistanceFromTarget();
            telemetry.addLine("targetPostition: " + getDistanceFromTarget());

            telemetry.addLine("targetPos motorFL: " + this.motorFrontLeft.getTargetPosition());
            telemetry.addLine("targetPos motorFR: " + this.motorFrontRight.getTargetPosition());
            telemetry.addLine("targetPos motorBL: " + this.motorBackLeft.getTargetPosition());
            telemetry.addLine("targetPos motorBR: " + this.motorBackRight.getTargetPosition());

            telemetry.addLine("currentPos motorFL: " + this.motorFrontLeft.getCurrentPosition());
            telemetry.addLine("currentPos motorFR: " + this.motorFrontRight.getCurrentPosition());
            telemetry.addLine("currentPos motorBL: " + this.motorBackLeft.getCurrentPosition());
            telemetry.addLine("currentPos motorBR: " + this.motorBackRight.getCurrentPosition());
            telemetry.update();
            // ramp assumes the distance away from the target is positive,
            // so we make it positive here and account for the direction when
            // the motor power is set.
            double direction = 1.0;
            if (distance < 0) {
                distance = -distance;
                direction = -1.0;
            }

            double scaledPower = ramp.scaleX(distance);

            setPower(direction * scaledPower);
            telemetry.update();
        }
        setPower(0);
        telemetry.update();
    }

    private void driveToPos(double ticks, double power) {
        setRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setRunMode(DcMotor.RunMode.RUN_TO_POSITION);
        setPower(0);

        setTargetPosition((int) ticks);
        setPower(power);

        Ramp ramp = new ExponentialRamp(new Point(0, RAMP_POWER_CUTOFF), new Point(ticks, power));

        while (anyMotorsBusy()) {
            int distance = getDistanceFromTarget();

            double direction = 1.0;
            if (distance < 0) {
                distance = -distance;
                direction = -1.0;
            }

            double scaledPower = ramp.scaleX(distance);

            setPower(direction * scaledPower);
            telemetry.update();
        }

        setPower(0);
    }

    // I changed which distance the motors standardize to from the min to the max
    private int getDistanceFromTarget() {
        synchDistances(); // getDistanceFromTarget should be redundant with this method
        int d = this.motorFrontLeft.getTargetPosition() - this.motorFrontLeft.getCurrentPosition();
        d = max(d, this.motorFrontRight.getTargetPosition() - this.motorFrontRight.getCurrentPosition());
        d = max(d, this.motorBackLeft.getTargetPosition() - this.motorBackLeft.getCurrentPosition());
        d = max(d, this.motorBackRight.getTargetPosition() - this.motorBackRight.getCurrentPosition());
        distanceItem.setValue(d);
        return d;
    }

    private void synchDistances() {
        int d = this.motorFrontLeft.getCurrentPosition();
        d = max(d, this.motorFrontRight.getCurrentPosition());
        d = max(d, this.motorBackLeft.getCurrentPosition());
        d = max(d, this.motorBackRight.getCurrentPosition());
        if (this.motorBackLeft.getCurrentPosition() != d) {
            this.motorBackLeft.setTargetPosition(d + (d - this.motorBackLeft.getCurrentPosition()));
        }
        if (this.motorBackRight.getCurrentPosition() != d) {
            this.motorBackRight.setTargetPosition(d + (d - this.motorBackRight.getCurrentPosition()));
        }
        if (this.motorFrontLeft.getCurrentPosition() != d) {
            this.motorFrontLeft.setTargetPosition(d + (d - this.motorFrontLeft.getCurrentPosition()));
        }
        if (this.motorFrontRight.getCurrentPosition() != d) {
            this.motorFrontRight.setTargetPosition(d + (d - this.motorFrontRight.getCurrentPosition()));
        }
    }

    private int max(int d1, int d2) {
        if (d1 > d2) {
            return d1;
        } else {
            return d2;
        }
    }

    public void turn(double degrees, double maxPower) {

        double heading = -imuSystem.getHeading();
        double targetHeading = 0;

        if ((degrees % 360) > 180) {
            targetHeading = heading + ((degrees % 360) - 360);
        } else {
            targetHeading = heading + (degrees % 360);
        }

        setRunMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Between 90 (changed from 130) and 2 degrees away from the target
        // we want to slow down from maxPower to 0.1
        ExponentialRamp ramp = new ExponentialRamp(new Point(2.0, TURN_RAMP_POWER_CUTOFF), new Point(90, maxPower));

        while (Math.abs(computeDegreesDiff(targetHeading, heading)) > 1) {
            double power = getTurnPower(ramp, targetHeading, heading);
            telemetry.addLine("heading: " + heading);
            telemetry.addLine("target heading: " + targetHeading);
            telemetry.addLine("power: " + power);
            telemetry.addLine("distance left: " + Math.abs(computeDegreesDiff(targetHeading, heading)));
            telemetry.update();

            tankDrive(power, -power);
            heading = -imuSystem.getHeading();
        }
        this.setPower(0);
    }

    public void tankDrive(double leftPower, double rightPower) {
        this.motorFrontLeft.setPower(leftPower);
        this.motorBackLeft.setPower(leftPower);
        this.motorFrontRight.setPower(rightPower);
        this.motorBackRight.setPower(rightPower);
    }

    private double computeDegreesDiff(double targetHeading, double heading) {
        return targetHeading - heading;
    }

    private double getTurnPower(Ramp ramp, double targetHeading, double heading) {
        double diff = computeDegreesDiff(targetHeading, heading);

        if (diff < 0) {
            return -ramp.scaleX(Math.abs(diff));
        } else {
            return ramp.scaleX(Math.abs(diff));
        }
    }
}
