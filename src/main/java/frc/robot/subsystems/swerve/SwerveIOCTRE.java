// Copyright (c) 2024 CurtinFRC
// Open Source Software, you can modify it according to the terms
// of the MIT License at the root of this project

package frc.robot.subsystems.swerve;

import com.ctre.phoenix6.Utils;
import com.ctre.phoenix6.swerve.SwerveDrivetrain;
import com.ctre.phoenix6.swerve.SwerveDrivetrainConstants;
import com.ctre.phoenix6.swerve.SwerveModuleConstants;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.RobotController;

/**
 * Class that extends the Phoenix SwerveDrivetrain class and implements subsystem so it can be used
 * in command-based projects easily.
 */
public class SwerveIOCTRE extends SwerveDrivetrain implements SwerveIO {
  private static final double kSimLoopPeriod = 0.005; // 5 ms
  private Notifier m_simNotifier;
  private double m_lastSimTime;

  public SwerveIOCTRE(
      SwerveDrivetrainConstants driveTrainConstants,
      double OdometryUpdateFrequency,
      SwerveModuleConstants... modules) {
    super(driveTrainConstants, OdometryUpdateFrequency, modules);

    if (Utils.isSimulation()) {
      startSimThread();
    }
  }

  public SwerveIOCTRE(
      SwerveDrivetrainConstants driveTrainConstants, SwerveModuleConstants... modules) {
    super(driveTrainConstants, modules);

    if (Utils.isSimulation()) {
      startSimThread();
    }
  }

  @Override
  public void updateInputs(SwerveIOInputs inputs) {
    SwerveDriveState state = getState();

    inputs.moduleStates = state.ModuleStates;
    inputs.moduleTargetStates = state.ModuleTargets;
    inputs.pose = state.Pose;
    inputs.speeds = state.Speeds;
    inputs.odometryPeriodSeconds = state.OdometryPeriod;
    inputs.successfulDaqs = state.SuccessfulDaqs;
    inputs.failedDaqs = state.FailedDaqs;

    inputs.rotation3d = super.getRotation3d();
    inputs.gyroRate = super.getPigeon2().getAngularVelocityZWorld().getValueAsDouble();
    inputs.odometryIsValid = super.isOdometryValid();
  }

  private void startSimThread() {
    m_lastSimTime = Utils.getCurrentTimeSeconds();

    /* Run simulation at a faster rate so PID gains behave more reasonably */
    m_simNotifier =
        new Notifier(
            () -> {
              final double currentTime = Utils.getCurrentTimeSeconds();
              double deltaTime = currentTime - m_lastSimTime;
              m_lastSimTime = currentTime;

              /* use the measured time delta, get battery voltage from WPILib */
              updateSimState(deltaTime, RobotController.getBatteryVoltage());
            });
    m_simNotifier.startPeriodic(kSimLoopPeriod);
  }
}
