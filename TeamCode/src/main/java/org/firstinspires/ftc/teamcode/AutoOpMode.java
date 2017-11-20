package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

/**
 * Created by raymo on 9/24/2017.
 */

public abstract class AutoOpMode extends LinearOpMode {
    double velocity = 0;
    double rotation = 0;
    double strafe = 0;
    VuforiaLocalizer vuforia;
    DcMotor FL;
    DcMotor FR;
    DcMotor BL;
    DcMotor BR;
    DcMotor leftLiftSlide;
    DcMotor rightLiftSlide;
    DcMotor liftMani;
    Servo leftMani;
    Servo rightMani;
    Servo jewelHitter;
    Servo leftArm;
    Servo rightArm;
    Servo leftRelic;
    Servo rightRelic;
    BNO055IMU imu;
    ColorSensor colorFront;
    ColorSensor colorBack;
    int recCount = 0;
    String cryptoboxKey;
    VuforiaLocalizer.Parameters parameters;
    char alliance;
    long closeTime;
    Servo RelicGrabber;
    private double currentTime;
    private double pastTime;
    private double integral;
    private double angleIntegral;
    private double angleDerivative;
    private double error;
    private double previousError;
    private double deltaT;
    private double deltaError;
    private double deltaAngError;
    private int startPos;
    private double startAngle;
    private double angDisplacement;
    private int displacement;
    private double kP;
    private double kI;
    private double kD;
    private double PIDPower;
    private double angError;
    private double previousAngError;
    private double strafeError;
    private double strafeDisplacement;
    private double previousStrafeError;
    private DcMotor intakeMotor;
    private double time;
    private int colorRec;


    public void initialize() throws InterruptedException {
        //RelicGrabber = hardwareMap.servo.get("RG");

        //FL is 0, BL is 1, FR is 2, BR is 3
        //Jewel is 0
        FL = hardwareMap.dcMotor.get("FL");
        FR = hardwareMap.dcMotor.get("FR");
        BR = hardwareMap.dcMotor.get("BR");
        BL = hardwareMap.dcMotor.get("BL");
        intakeMotor = hardwareMap.dcMotor.get("intake");
        FL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        BL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        BR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        FR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        FL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        BL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        BR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        FR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        /*leftArm = hardwareMap.servo.get("LRelicArm");
        rightArm = hardwareMap.servo.get("RRelicArm");
        leftRelic = hardwareMap.servo.get("LRelic");
        rightRelic = hardwareMap.servo.get("RRelic");
        leftMani = hardwareMap.servo.get("LMani");
        rightMani = hardwareMap.servo.get("RMani");
        rightMani.setDirection(Servo.Direction.FORWARD);
        leftMani.setDirection(Servo.Direction.REVERSE);
        leftLiftSlide = hardwareMap.dcMotor.get("LSlide");
        rightLiftSlide = hardwareMap.dcMotor.get("RSlide");
        liftMani = hardwareMap.dcMotor.get("liftMani");

        jewelHitter = hardwareMap.servo.get("jewelhitter");
        jewelHitter.setDirection(Servo.Direction.REVERSE);
        */
        //gyro init
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json";
        parameters.loggingEnabled = true;
        parameters.loggingTag = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
        //color sensor init
        colorFront = hardwareMap.colorSensor.get("color");
        //colorBack = hardwareMap.colorSensor.get("color2");
        colorFront.enableLed(true);
        //colorBack.enableLed(true);
        reportInitialized();
    }


    public void setPower(double velocity, double rotation, double strafe) throws InterruptedException {
        FL.setPower(velocity - rotation + strafe);
        FR.setPower(-velocity - rotation - strafe);
        BL.setPower(velocity - rotation - strafe);
        BR.setPower(-velocity - rotation + strafe);
    }

    public void setZero() {
        FL.setPower(0);
        FR.setPower(0);
        BL.setPower(0);
        BR.setPower(0);
    }

    public void reportInitialized(){
        int relativeLayoutId = hardwareMap.appContext.getResources().getIdentifier("RelativeLayout", "id", hardwareMap.appContext.getPackageName());
        final View relativeLayout = ((Activity) hardwareMap.appContext).findViewById(relativeLayoutId);
        if (alliance == 98){
            relativeLayout.post(new Runnable() {
                public void run() {
                    relativeLayout.setBackgroundColor(Color.BLUE);
                }
            });
            telemetry.addData("Blue Auto", "Initialized");
        }
        else {
            relativeLayout.post(new Runnable() {
                public void run() {
                    relativeLayout.setBackgroundColor(Color.RED);
                }
            });
            telemetry.addData("Red Auto", "Initialized");
        }
        telemetry.update();
    }

    public double calculateStrafe(double velocity, double angle) throws InterruptedException {
        return Math.tan(angle) / velocity;
    }

    public double getGyroPitch() throws InterruptedException {
        Orientation angles = imu.getAngularOrientation();
        return (angles.secondAngle * -1);
    }

    //
    public double getGyroRoll() throws InterruptedException {
        Orientation angles = imu.getAngularOrientation();
        return (angles.thirdAngle * -1);
    }


    //firstAngle
    public double getGyroYaw() throws InterruptedException {
        Orientation angles = imu.getAngularOrientation();
        return (angles.firstAngle * -1);
    }

    public void testGyro() throws InterruptedException {
        telemetry.addData("Yaw", getGyroYaw());
        telemetry.addData("Roll", getGyroRoll());
        telemetry.addData("Pitch", getGyroPitch());
        telemetry.update();
    }

    public double getGyroYaw(double turn) throws InterruptedException {
        double turnAbs = Math.abs(turn);
        Orientation angles = imu.getAngularOrientation();
        return angles.secondAngle;
//        if (turnAbs > 270 && Math.abs(angles.firstAngle) < 90)
//            return (Math.abs(angles.firstAngle) - (turnAbs - 360));
//        else if (turnAbs < 90 && Math.abs(angles.firstAngle) > 270)
//            return ((Math.abs(angles.firstAngle) - 360) - turnAbs);
//        return (Math.abs(angles.firstAngle) - turnAbs);
    }

    public int getRed(ColorSensor color) throws InterruptedException {
        return color.red();
    }

    public int getBlue(ColorSensor color) throws InterruptedException {
        return color.blue();
    }

    public void lowerJewel() throws InterruptedException {
        jewelHitter.setPosition(0.54);
    }

    public void raiseJewel() throws InterruptedException {
        jewelHitter.setPosition(1);
    }

    public void setAlliance(char c) throws InterruptedException {
        alliance = c;
        /*
        //I think this part sets the driver station color to alliance color
        int relativeLayoutId = hardwareMap.appContext.getResources().getIdentifier("RelativeLayout", "id", hardwareMap.appContext.getPackageName());
        final View relativeLayout = ((Activity) hardwareMap.appContext).findViewById(relativeLayoutId);
        final float[] purple = {276, 63, 78};
        if (alliance == 114) {
            relativeLayout.post(new Runnable() {
                public void run() {
                    relativeLayout.setBackgroundColor(Color.HSVToColor(purple));
                }
            });
        } else if (alliance == 98) {
            relativeLayout.post(new Runnable() {
                public void run() {
                    relativeLayout.setBackgroundColor(Color.HSVToColor(purple));
                }
            });
        }
        */
    }


    public int getAvgEncoder() throws InterruptedException {
        return (Math.abs(FL.getCurrentPosition()) + Math.abs(FR.getCurrentPosition())) / 2;
    }

    public void moveForward(double velocity) throws InterruptedException {
        setPower(velocity, 0, 0);
    }

    public void moveBackward(double velocity) throws InterruptedException {
        setPower(-velocity, 0, 0);
    }

    public void moveAtAngle(double velocity, double angle) throws InterruptedException {
        setPower(velocity, 0, calculateStrafe(velocity, angle));
    }

    public void turn(double rotation) throws InterruptedException {
        setPower(0, rotation, 0);
    }

    public void moveForward(double velocity, int distance) throws InterruptedException {
        int startPos = getAvgEncoder();
        while ((Math.abs(getAvgEncoder() - startPos) < distance) && (opModeIsActive())) {
            moveForward(velocity);
            telemetry.addData("distance", getAvgEncoder() - startPos);
            telemetry.update();
            idle();
        }
        setZero();
        if (Math.abs(getAvgEncoder() - startPos) > distance + 50) {
            telemetry.addData("overshoot", "fix");
            telemetry.update();
        }
    }

    public void moveBackward(double velocity, int distance) throws InterruptedException {
        int startPos = getAvgEncoder();
        while ((Math.abs(getAvgEncoder() - startPos) < distance) && (opModeIsActive())) {
            moveBackward(velocity);
            telemetry.addData("distance", getAvgEncoder() - startPos);
            telemetry.update();
            idle();
        }
        setZero();
        if (Math.abs(getAvgEncoder() - startPos) > distance + 50) {
            telemetry.addData("overshoot", "fix");
            telemetry.update();
        }
    }

    public void turn(double rotation, double angle) throws InterruptedException {
        double first = getGyroYaw();
        while ((Math.abs(getGyroYaw() - first) < angle) && (opModeIsActive())) {
            turn(rotation);
            idle();
        }
        setZero();
    }

    /*
    public void turnPID(double angle) throws InterruptedException {
        double p = 0.005;
        double i = 0.00001;
        double startTime = System.currentTimeMillis();
        double power = 0;
        double integral = 0;
        double proportion = 0;
        double start = getGyroYaw();
        double current = getGyroYaw();
        while (Math.abs(current - start) < angle && Math.abs(System.currentTimeMillis() - start) < 500){
            integral += Math.abs(current - start) * Math.abs(System.currentTimeMillis() - start) * i;
            proportion = p * Math.abs(current - start);
            power = proportion + integral;
            turn(power);
            idle();
        }
        setZero();
    }
    */

    /*public void scanImage() throws InterruptedException
    {

        RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(relicTemplate);
        if (vuMark != RelicRecoveryVuMark.UNKNOWN) {
            if (vuMark == RelicRecoveryVuMark.LEFT){
                telemetry.addData("Key","left");
                telemetry.update();
                cryptoboxKey = "left";
            }
            if (vuMark == RelicRecoveryVuMark.CENTER){
                telemetry.addData("Key","center");
                telemetry.update();
                cryptoboxKey = "center";
            }
            if (vuMark == RelicRecoveryVuMark.RIGHT){
                telemetry.addData("Key","right");
                telemetry.update();
                cryptoboxKey = "right";
            }
        }
        else {
            telemetry.addData("Key", "unknown");
            telemetry.update();
        }

    }
    */

    public void scanImage() throws InterruptedException {
        //Camera Set Up
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        //Key
        parameters.vuforiaLicenseKey = "AQ1iIdT/////AAAAGZ0U6OKRfU8tpKf9LKl/7DM85y3Wp791rb6q3WwHfYaY53vqKSjAO8wU2FgulWnDt6gLqu9hB33z1reejMz/NyfL8u11QZlMIbimmnP/v4hvoXZWu0p62V9eMG3R2PQ3Z7rZ0qK8HwsQYE/0jmBhTy0D17M4fWpNW64QQnMJqFxq/N1BXm32PEInYDHBYs7WUrHL5oa9xeSSurxUq/TqDpeJwQM+1/GYppdAqzbcM1gi3yzU7JDLdNtOZ6+lbi5uXlU++GnFvQaEXL9uVcnTwMEgBhBng6oOEVoEDXiSUBuZHuMRGZmHfVXSNE3m1UXWyEdPTlMRI5vfEwfsBHmQTmvYr/jJjng3+tBpu85Q1ivo";
        //Use Front Camera
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.FRONT;
        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);
        VuforiaTrackables relicTrackables = this.vuforia.loadTrackablesFromAsset("RelicVuMark");
        VuforiaTrackable relicTemplate = relicTrackables.get(0);
        relicTemplate.setName("relicVuMarkTemplate");
        relicTrackables.activate();
        boolean detected = false;
        long scanTime = System.currentTimeMillis();
        int relativeLayoutId = hardwareMap.appContext.getResources().getIdentifier("RelativeLayout", "id", hardwareMap.appContext.getPackageName());
        final View relativeLayout = ((Activity) hardwareMap.appContext).findViewById(relativeLayoutId);
        relativeLayout.post(new Runnable() {
            public void run() {
                relativeLayout.setBackgroundColor(Color.RED);
            }
        });
        while ((System.currentTimeMillis() - scanTime < 10000) && (opModeIsActive()) && (!detected)) {
            RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(relicTemplate);
            if (vuMark != RelicRecoveryVuMark.UNKNOWN) {
                if (vuMark == RelicRecoveryVuMark.LEFT) {
                    telemetry.addData("VuMark", "Left");
                    telemetry.update();
                    cryptoboxKey = "left";
                    relativeLayout.post(new Runnable() {
                        public void run() {
                            relativeLayout.setBackgroundColor(Color.GREEN);
                        }
                    });
                    detected = true;
                } else if (vuMark == RelicRecoveryVuMark.CENTER) {
                    telemetry.addData("VuMark", "Center");
                    telemetry.update();
                    cryptoboxKey = "center";
                    relativeLayout.post(new Runnable() {
                        public void run() {
                            relativeLayout.setBackgroundColor(Color.GREEN);
                        }
                    });
                    detected = true;
                }
                if (vuMark == RelicRecoveryVuMark.RIGHT) {
                    telemetry.addData("VuMark", "Right");
                    telemetry.update();
                    cryptoboxKey = "right";
                    relativeLayout.post(new Runnable() {
                        public void run() {
                            relativeLayout.setBackgroundColor(Color.GREEN);
                        }
                    });
                    detected = true;
                }
            }
        }
        telemetry.addData("VuMark", "NotDetected");
        telemetry.update();
    }


    /*
    public void moveToDropBlock(String place) throws InterruptedException {
        scanImage();
        if (place.equals("left")) {
            moveForward(.2, 1000);
        }
        else if (place.equals("middle")) {
            moveForward(.2,2000);
        }
        else
            moveForward(.2,2000);
    }
    */

    public void useIntake(){
        double intakeTime =  System.currentTimeMillis();
        while ((System.currentTimeMillis() - intakeTime < 30000) && (opModeIsActive())){
            intakeMotor.setPower(-1);
            idle();
        }
        intakeMotor.setPower(0);
    }

    public void grabGlyph() throws InterruptedException {
        closeTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - closeTime < 2000) && (opModeIsActive())) {
            leftMani.setPosition(1);
            rightMani.setPosition(1);
            idle();
        }
        leftMani.setPosition(0.5);
        rightMani.setPosition(0.5);
    }

    public void releaseGlyph() throws InterruptedException {
        closeTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - closeTime < 2000) && (opModeIsActive())) {
            leftMani.setPosition(0.3);
            rightMani.setPosition(0.3);
            idle();
        }
        leftMani.setPosition(0.5);
        rightMani.setPosition(0.5);
    }

    public int getStrafeEncoders() {
        int backLeftEncoderValue = BL.getCurrentPosition();
        int backRightEncoderValue = BR.getCurrentPosition();
        int frontRightEncoderValue = FR.getCurrentPosition();
        int frontLeftEncoderValue = FL.getCurrentPosition();

        int avgDiagPosition1 = (Math.abs(backLeftEncoderValue) + Math.abs(frontRightEncoderValue)) / 2;
        //double avgDiagPosition2 = (Math.abs(backRightEncoderValue) + Math.abs(frontLeftEncoderValue)) / 2.0;


        //return (avgDiagPosition1 + avgDiagPosition2) / 2.0;
        return avgDiagPosition1;
    }

    public void moveStrafe(double strafe) throws InterruptedException {
        setPower(0, 0, strafe);
    }


    public void moveStrafe(double strafe, int distance) throws InterruptedException {
        double startStrafe = getStrafeEncoders();
        while ((Math.abs(getStrafeEncoders() - startStrafe) < distance) && (opModeIsActive())) {
            moveStrafe(strafe);
            telemetry.addData("distance", getStrafeEncoders() - startStrafe);
            telemetry.update();
            idle();
        }
        setZero();
    }

    /*
    //Moves the robot the the correct column (values not tested)
    public void moveKey() throws InterruptedException {
        if (cryptoboxKey.equals("left")) {
            moveStrafe(0.5, 600);
        } else if (cryptoboxKey.equals("center")) {
            moveStrafe(0.5, 400);
        } else {
            moveStrafe(0.5, 200);
        }
    }
    */
    //PID Stuff

    public void setStartPos() throws InterruptedException {
        startPos = getAvgEncoder();
        findDisplacement();
    }

    public void setStartAngle() throws InterruptedException {
        startAngle = getGyroYaw();

    }

    public void findDisplacement() throws InterruptedException {
        displacement = Math.abs(getAvgEncoder() - startPos);
        telemetry.addData("Displacement", displacement);
    }

    public void findAngDisplacement() throws InterruptedException {
        angDisplacement = Math.abs(getGyroYaw() - startAngle);
        telemetry.addData("Angular Displacement", angDisplacement);
    }

    public void setInitialError(int goalDistance) {
        error = goalDistance;
    }

    public void findError(int goalDistance) throws InterruptedException {
        previousError = error;
        error = Math.abs(goalDistance - displacement);
    }

    public void setInitialAngError(double goalAngle) {
        angError = goalAngle;
    }

    public void findAngError(double goalAngle) throws InterruptedException {
        previousAngError = angError;
        angError = Math.abs(goalAngle - angDisplacement);
    }

    //Finds error, but has angle can be negative
    public void findTrueAngError(double goalAngle) throws InterruptedException {
        previousAngError = angError;
        angError = goalAngle - angDisplacement;
    }

    public void setKValues(double p, double i, double d) {
        kP = p;
        kI = i;
        kD = d;
    }

    public void getPercentTraveled(double goalDistance) {
        double percent = displacement / goalDistance * 100;

        if (Math.abs(100 - percent) <= 2) {
            telemetry.addData("Success", percent + "% Accurate");
        } else if (displacement - 100 > 2) {
            telemetry.addData("Too Much", percent + "% Accurate");
        } else if (100 - displacement > 2) {
            telemetry.addData("Too Little", percent + "% Accurate");
        }
        telemetry.addData("Distance", displacement + "/" + goalDistance);

    }

    public void getPercentTurned(double goalAngle){
        double percent = angDisplacement / goalAngle * 100;

        if (Math.abs(100 - percent) <= 2) {
            telemetry.addData("Success", percent + "% Accurate");
        } else if (displacement - 100 > 2) {
            telemetry.addData("Too Much", percent + "% Accurate");
        } else if (100 - displacement > 2) {
            telemetry.addData("Too Little", percent + "% Accurate");
        }
        telemetry.addData("Angle", angDisplacement + "/" + goalAngle);

    }

    public void getStraightness() throws InterruptedException {
        if (getGyroYaw() - startAngle >= 0) {
            telemetry.addData("Angle", getGyroYaw() - startAngle + " right");
        }
        if (getGyroYaw() - startAngle <= 0) {
            telemetry.addData("Angle", startAngle - getGyroYaw() + " left");
        }

    }

    public void reportSuccess(int distance){
        int relativeLayoutId = hardwareMap.appContext.getResources().getIdentifier("RelativeLayout", "id", hardwareMap.appContext.getPackageName());
        final View relativeLayout = ((Activity) hardwareMap.appContext).findViewById(relativeLayoutId);
        if (Math.abs(displacement - distance) <= 5){
            relativeLayout.post(new Runnable() {
                public void run() {
                    relativeLayout.setBackgroundColor(Color.GREEN);
                }
            });
        }
        else if (Math.abs(displacement - distance) <= 20){
            relativeLayout.post(new Runnable() {
                public void run() {
                    relativeLayout.setBackgroundColor(Color.YELLOW);
                }
            });
        }
        else{
            relativeLayout.post(new Runnable() {
                public void run() {
                    relativeLayout.setBackgroundColor(Color.RED);
                }
            });
        }
    }



    //Proportion Stuff

    public double getProportion() {
        telemetry.addData("Proportion", kP * error);
        return kP * error;
    }

    public double getAngProportion() {
        telemetry.addData("Proportion", kP * angError);
        return kP * angError;
    }

    public double getStrafeProportion() {
        telemetry.addData("Proportion", kP * strafeError);
        return kP * strafeError;
    }

    //Integral Stuff
    public void findDeltaT() {
        deltaT = (System.currentTimeMillis() - pastTime);
        pastTime = System.currentTimeMillis();
        time += deltaT;
        telemetry.addData("Time", time);
    }

    public void resetIntegral() {
        integral = 0;
        angError = 0;
        pastTime = System.currentTimeMillis();
    }

    public void tallyIntegral() {
        integral += deltaT * error;
    }

    public void tallyAngIntegral() {
        angleIntegral += deltaT * angError;
    }

    public void tallyStrafeIntegral() {
        integral += deltaT * strafeError;
    }

    public double getIntegral() {
        telemetry.addData("Current Integral", integral*kI);
        return integral * kI;
    }

    public double getAngIntegral(){
        telemetry.addData("Current Integral", angleIntegral*kI);
        return angleIntegral * kI;
    }

    //Derivative Stuff

    public void findDeltaError() {
        telemetry.addData("Error", error);
        telemetry.addData("Previous", previousError);
        deltaError = error - previousError;
    }

    public void findDeltaAngError() {
        telemetry.addData("Error", angError);
        telemetry.addData("Previous", previousAngError);
        deltaAngError = angError - previousAngError;
        telemetry.addData("AngError", deltaAngError);
    }

    public void findStrafeDeltaError() {
        deltaError = strafeError - previousStrafeError;
    }

    public double getDerivative() {
        telemetry.addData("Current Derivative", kD * deltaError / deltaT);
        return kD * deltaAngError / deltaT;
    }

    public double getAngDerivative() {
        telemetry.addData("Current Derivative", kD * deltaAngError / deltaT);
        return kD * deltaAngError / deltaT;
    }


    public void moveForwardPID(int distance) throws InterruptedException {
        setStartPos();
        setStartAngle();
        resetIntegral();
        setInitialError(distance);
        setKValues(0.0002, 0.0000001, 0.5);
        while ((displacement < distance) && (opModeIsActive())) {
            findDisplacement();
            findError(distance);
            findDeltaT();
            findDeltaError();
            tallyIntegral();
            telemetry.update();
            moveForward(getProportion() + getIntegral() + getDerivative());
            idle();
        }
        setZero();
        getPercentTraveled(distance);
        getStraightness();
        telemetry.update();
        reportSuccess(distance);
    }

    public void moveBackwardPID(int distance) throws InterruptedException {
        setStartPos();
        setStartAngle();
        resetIntegral();
        setInitialError(distance);
        setKValues(0.0002, 0.0000001, 0.5);
        while ((displacement < distance) && (opModeIsActive())) {
            findDisplacement();
            findError(distance);
            findDeltaT();
            findDeltaError();
            tallyIntegral();
            telemetry.update();
            moveBackward(getProportion() + getIntegral() + getDerivative());
            idle();
        }
        setZero();
        getPercentTraveled(distance);
        getStraightness();
        telemetry.update();
    }


    public void turnRightPID(double angle) throws InterruptedException {
        setStartAngle();
        resetIntegral();
        setInitialAngError(angle);
        //setKValues(0.002777, 0.000002, 0.1);
        setKValues(0.002777, 0.000002, 0.11);
        while ((angDisplacement < angle)&&(opModeIsActive())) {
            findAngDisplacement();
            telemetry.addData("Yaw", getGyroYaw());
            findAngError(angle);
            findDeltaT();
            findDeltaAngError();
            tallyAngIntegral();
            telemetry.update();
            turn(getAngProportion() + getAngIntegral() + getAngDerivative());
            idle();
        }
        setZero();
        getPercentTurned(angle);
    }

    public void turnLeftPID(double angle) throws InterruptedException {
        setStartAngle();
        resetIntegral();
        setInitialAngError(angle);
        setKValues(0.002777, 0.000015, 0.1);
        //setKValues(0.004, 0.000015, 3.0);
        while ((angDisplacement < angle)&&(opModeIsActive())) {
            findAngDisplacement();
            telemetry.addData("Yaw", getGyroYaw());
            findAngError(angle);
            findDeltaT();
            findDeltaAngError();
            tallyAngIntegral();
            telemetry.update();
            turn(-1 * (getAngProportion() + getAngIntegral() + getAngDerivative()));
            idle();
        }
        setZero();
        getPercentTurned(angle);
    }

    public void moveForwardStraight(int distance) throws InterruptedException {
        setStartPos();
        setStartAngle();
        resetIntegral();
        setInitialError(distance);
        setKValues(0.00015, 0.00000015, 0.25);
        while ((displacement < distance) && (opModeIsActive())) {
            findDisplacement();
            findError(distance);
            findDeltaT();
            findDeltaError();
            tallyIntegral();
            telemetry.update();
            setPower(getProportion() + getIntegral() + getDerivative(), complexStraighten(), 0);
            idle();
        }
        setZero();
        getPercentTraveled(distance);
        getStraightness();
        telemetry.update();
    }

    public void moveBackwardStraight(int distance) throws InterruptedException {
        setStartPos();
        setStartAngle();
        resetIntegral();
        setInitialError(distance);
        setKValues(0.00015, 0.00000015, 2);
        while ((displacement < distance) && opModeIsActive()) {
            findDisplacement();
            findError(distance);
            deltaT = System.currentTimeMillis() - pastTime;
            findDeltaT();
            findDeltaError();
            tallyIntegral();
            telemetry.update();
            moveBackward(getProportion() + getIntegral() + getDerivative());
            idle();
        }
        setZero();
        getPercentTraveled(distance);
        getStraightness();
        telemetry.update();
    }

    //Untested
    public double simpleStraighten() throws InterruptedException {
        findAngDisplacement();
        findTrueAngError(0);
        if (angError >= 0.1 ){
            return -0.1;
        }
        else if (angError <= -0.1){
            return 0.1;
        }
        return 0;
    }

    public double complexStraighten() throws InterruptedException {
        findAngDisplacement();
        findTrueAngError(0);
        angleIntegral += deltaT * angError;
        angleDerivative = (angError - previousAngError)/deltaT;
        return -1 * (0.004 * angError + 0.000015 * angleDerivative + 2 * angleIntegral);

    }

    //PID Strafing

    public void setStartStrafePos(){
        startPos = getStrafeEncoders();
        findStrafeDisplacement();
    }

    public void findStrafeDisplacement(){
        displacement = Math.abs(getStrafeEncoders() - startPos);
    }

    public void moveStrafeRightPID(int distance) throws InterruptedException {
        setStartStrafePos();
        setStartAngle();
        resetIntegral();
        setInitialError(distance);
        setKValues(0.0008, 0.0000004, 0.5);
        while ((displacement < distance) && (opModeIsActive())) {
            findStrafeDisplacement();
            findError(distance);
            findDeltaT();
            findDeltaError();
            tallyIntegral();
            telemetry.update();
            setPower(0,0,getProportion() + getIntegral() + getDerivative());
            idle();
        }
        setZero();
        getPercentTraveled(distance);
        getStraightness();
        telemetry.update();
    }

    public void moveStrafeLeftPID(int distance) throws InterruptedException {
        setStartStrafePos();
        setStartAngle();
        resetIntegral();
        setInitialError(distance);
        setKValues(0.0008, 0.0000004, 0.5);
        while ((displacement < distance) && (opModeIsActive())) {
            findStrafeDisplacement();
            findError(distance);
            findDeltaT();
            findDeltaError();
            tallyIntegral();
            telemetry.update();
            setPower(0,0,-1*(getProportion() + getIntegral() + getDerivative()));
            idle();
        }
        setZero();
        getPercentTraveled(distance);
        getStraightness();
        telemetry.update();
    }

    public void moveToColumn() throws InterruptedException {
        if (cryptoboxKey.equals("left")){
            moveStrafeLeftPID(2000);
        }
        else if (cryptoboxKey.equals("center")){
            moveStrafeLeftPID(1000);
        }
        else{
            moveStrafeLeftPID(400);
            sleep(1000);
        }
    }

    //Color Sensor + Hitting the Jewel

    public void hitJewel() throws InterruptedException {
       String direction = pickDirection();
       if (direction.equals("forward")) {
            //park in safe zone
            moveForwardPID(200);
            setZero();
            moveBackwardPID(200);
        }
        else if (direction.equals("backward")) {
           moveBackwardPID(200);
           setZero();
           moveForwardPID(200);
        }
        raiseJewel();
    }

    public void hitJewelTurn() throws InterruptedException {
        String direction = pickDirection();
        if (direction.equals("forward")) {
            //park in safe zone
            turnLeftPID(20);
            setZero();
            turnRightPID(20);
        }
        else if (direction.equals("backward")) {
            turnRightPID(20);
            setZero();
            turnLeftPID(20);
        }
        raiseJewel();
    }

    public String pickDirection(){
        String jewelColor = simpleColorDetect();
        if (alliance == 114){
            if (jewelColor.equals("red")){
                return "forward";
            }
            else if (jewelColor.equals("blue")){
                return "backward";
            }
        }
        else if (alliance == 98){
            if (jewelColor.equals("blue")){
                return "forward";
            }
            else if (jewelColor.equals("red")){
                return "backward";
            }
        }
        return "not functioning";
    }

    public String simpleColorDetect(){
        double red = colorFront.red();
        if (red > 18)
            return "red";
        else if (red < 10)
            return "blue";
        else if (colorRec < 4){
            sleep(500);
            colorRec++;
            simpleColorDetect();
        }
        telemetry.addData("Color", "Not Found");
        telemetry.update();
        return "unknown";
    }

    public String simpleColorCompare(){
        double red = colorFront.red();
        sleep(500);
        double blue = colorFront.blue();

        if (red > blue + 6)
            return "red";
        else if (blue > red + 6)
            return "blue";
        else if (colorRec < 4){
            colorRec++;
            simpleColorCompare();
        }
        telemetry.addData("Color", "Not Found");
        telemetry.update();
        return "unknown";
    }

    public String avgColorDetect(){
        double red = 0;
        sleep(500);
        for (int i = 0; i < 10; i++){
            sleep(200);
            red += colorFront.red();
        }
        red /= 10;
        telemetry.addData("Red", red);
        telemetry.update();

        if (red > 18)
            return "red";
        else if (red < 10)
            return "blue";
        else if (colorRec < 1){
            colorRec++;
            avgColorDetect();
        }
        telemetry.addData("Color", "Not Found");
        telemetry.update();
        return "unknown";
    }

    public String avgColorCompare(){
        double red = 0;
        double blue = 0;
        sleep(500);
        for (int i = 0; i < 20; i++){
            sleep(100);
            if (i % 2 == 0)
                red += colorFront.red();
            if (i % 2 == 1)
                blue += colorFront.blue();
        }
        red /= 10;
        blue /= 10;

        telemetry.addData("Red", red);
        telemetry.addData("Blue", blue);
        telemetry.update();

        if (red > blue + 6)
            return "red";
        else if (blue > red + 6)
            return "blue";
        else if (colorRec < 1){
            colorRec++;
            avgColorCompare();
        }

        telemetry.addData("Color", "Not Found");
        telemetry.update();
        return "unknown";
    }
}

/*
                    Code Graveyard (R.I.P.)
public String chooseColor(char c) throws InterruptedException {
        //hitting blue
        if (c == 114) {
            if (getBlue(colorFront) < getBlue(colorBack)) {
                telemetry.addData("hit", "forwards");
                telemetry.update();
                return "forwards";
            } else if (getBlue(colorFront) > getBlue(colorBack)) {
                telemetry.addData("hit", "backwards");
                telemetry.update();
                return "backwards";
            } else {
                sleep(1000);
                if (recCount < 2)
                    recCount++;
                chooseColor(c);
                telemetry.addData("ColorSensors", "broken");
                telemetry.update();
                return "broken";

            }
        }
        //hitting red
        if (c == 98) {
            if (getRed(colorFront) < getRed(colorBack)) {
                telemetry.addData("hit", "forwards");
                telemetry.update();
                return "forwards";
            } else if (getRed(colorFront) > getRed(colorBack)) {
                telemetry.addData("hit", "backwards");
                telemetry.update();
                return "backwards";
            } else {
                sleep(1000);
                if (recCount < 2)
                    recCount++;
                chooseColor(c);
                telemetry.addData("ColorSensors", "broken");
                telemetry.update();
                return "broken";
            }
        }
        return "broken";
    }

    public String choseOneColor(char c) throws InterruptedException {
        //hitting blue
        if (c == 114) {
            if (getRed(colorBack) < getBlue(colorBack)) {
                telemetry.addData("hit", "backwards");
                telemetry.update();
                return "forwards";
            } else if (getRed(colorBack) > getBlue(colorBack)) {
                telemetry.addData("hit", "forwards");
                telemetry.update();
                return "backwards";
            } else {
                sleep(1000);
                if (recCount < 2)
                    recCount++;
                chooseColor(c);
            }
        }
        //hitting red
        if (c == 98) {
            if (getRed(colorBack) < getBlue(colorBack)) {
                telemetry.addData("hit", "forwards");
                telemetry.update();
                return "forwards";
            } else if (getRed(colorBack) > getBlue(colorBack)) {
                telemetry.addData("hit", "backwards");
                telemetry.update();
                return "backwards";
            } else {
                sleep(1000);
                if (recCount < 2)
                    recCount++;
                chooseColor(c);
            }
        }
        return "broken";
    }

 */