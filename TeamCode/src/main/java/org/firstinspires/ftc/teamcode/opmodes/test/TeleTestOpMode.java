package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.hardware.controller.Controller;
import org.firstinspires.ftc.teamcode.hardware.controller.Handler;
import org.firstinspires.ftc.teamcode.opmodes.debuggers.TeleOpModeDebugger;
import org.firstinspires.ftc.teamcode.systems.arm.ArmState;
import org.firstinspires.ftc.teamcode.systems.arm.ArmSystem;
import org.firstinspires.ftc.teamcode.systems.drive.MecanumDriveSystem;
import org.firstinspires.ftc.teamcode.systems.flail.Flail;
import org.firstinspires.ftc.teamcode.systems.slide.SlideState;
import org.firstinspires.ftc.teamcode.systems.slide.SlideSystem;

/**
 * Created by idiot on 10/11/17.
 */
@TeleOp(name = "TeleOpTest", group="TeleOp")
public class TeleTestOpMode extends TeleOpModeDebugger {
    private Controller controller1;
    private MecanumDriveSystem driveSystem;
    private ArmSystem armSystem;
    private SlideSystem slideSystem;
    private Flail flail;

    private boolean slowDrive;

    public TeleTestOpMode() {
        msStuckDetectLoop = 1000000000;
    }

    @Override
    public void init()
    {
        this.controller1 = new Controller(gamepad1);
        armSystem = new ArmSystem(this);
        slideSystem = new SlideSystem(this);
        this.driveSystem = new MecanumDriveSystem(this);
        this.flail = new Flail(this);
        initButton();

        slowDrive = false;
    }


    @Override
    public void initialize()
    {

    }

    public void initButton() {
        telemetry.addData("buttons", "initialize");
        telemetry.update();
        addWinchButton();
        addRotateButton();
        addFlailButton();
    }

    private void addWinchButton() {
        controller1.rightTrigger.pressedHandler = new Handler()
        {
            @Override
            public void invoke() throws Exception
            {
                slideSystem.setState(SlideState.WINCHING_TO_TOP);
            }
        };
        controller1.rightTrigger.releasedHandler = new Handler()
        {
            @Override
            public void invoke() throws Exception
            {
                slideSystem.setState(SlideState.IDLE);
            }
        };
        controller1.leftTrigger.pressedHandler = new Handler()
        {
            @Override
            public void invoke() throws Exception
            {
                slideSystem.setState(SlideState.WINCHING_TO_BOTTOM);
            }
        };
        controller1.leftTrigger.releasedHandler = new Handler()
        {
            @Override
            public void invoke() throws Exception
            {
                slideSystem.setState(SlideState.IDLE);
            }
        };
    }

    private void addRotateButton() {
        controller1.dPadDown.pressedHandler = new Handler()
        {
            @Override
            public void invoke() throws Exception
            {
                armSystem.setState(ArmState.ROTATING_BOTTOM);
            }
        };
        controller1.dPadDown.releasedHandler = new Handler()
        {
            @Override
            public void invoke() throws Exception
            {
                armSystem.setState(ArmState.IDLE);
            }
        };
        controller1.dPadUp.pressedHandler = new Handler()
        {
            @Override
            public void invoke() throws Exception
            {
                armSystem.setState(ArmState.ROTATING_TOP);
            }
        };
        controller1.dPadUp.releasedHandler = new Handler()
        {
            @Override
            public void invoke() throws Exception
            {
                armSystem.setState(ArmState.IDLE);
            }
        };
        controller1.leftStickButton.pressedHandler = new Handler() {
            @Override
            public void invoke() throws Exception {
                armSystem.releaseArmPin();
            }
        };
        controller1.rightStickButton.pressedHandler = new Handler() {
            @Override
            public void invoke() throws Exception {
                armSystem.setArmPin();
            }
        };
    }

    private void addFlailButton() {
        controller1.rightBumper.pressedHandler = new Handler()
        {
            @Override
            public void invoke() throws Exception
            {
                flail.start();
            }
        };
        controller1.rightBumper.releasedHandler = new Handler()
        {
            @Override
            public void invoke() throws Exception
            {
                flail.stop();
            }
        };
    }

    private void addSlowDriveButton() {
        controller1.leftStickButton.pressedHandler = new Handler() {
            @Override
            public void invoke() throws Exception {
                slowDrive = true;
            }
        };
        controller1.leftStickButton.releasedHandler = new Handler() {
            @Override
            public void invoke() throws Exception {
                slowDrive = false;
            }
        };
    }

    @Override
    public void run(){
        controller1.handle();
        armSystem.run();
        slideSystem.run();

        float rx = controller1.gamepad.right_stick_x;
        float ry = controller1.gamepad.right_stick_y;
        float lx = controller1.gamepad.left_stick_x;
        float ly = controller1.gamepad.left_stick_y;

        driveSystem.mecanumDrive(rx, ry, lx, ly, slowDrive);
    }
}
