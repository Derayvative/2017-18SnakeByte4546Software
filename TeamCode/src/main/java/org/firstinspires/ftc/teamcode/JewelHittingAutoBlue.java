package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Created by sopa on 10/25/17.
 */
@Autonomous (name = "Blue is the warmest color")
public class JewelHittingAutoBlue extends AutoOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        initialize();
        telemetry.addData("Blue", "Ready");
        waitForStart();
        setAlliance('b');
        //scanImage();
        moveStrafe(0.5,300);
        /*
        String cryptoKey = scanImage();
        sleep (5000);
        lowerJewel();
        sleep(2500);
        hitJewel();
        sleep(1000);
        moveForward(0.2,2000);*/
        sleep(1000);
    }
}
