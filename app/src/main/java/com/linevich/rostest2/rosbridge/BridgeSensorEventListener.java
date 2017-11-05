package com.linevich.rostest2.rosbridge;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import edu.wpi.rail.jrosbridge.Topic;
import edu.wpi.rail.jrosbridge.messages.geometry.Point;
import edu.wpi.rail.jrosbridge.messages.geometry.Pose;
import edu.wpi.rail.jrosbridge.messages.geometry.PoseStamped;
import edu.wpi.rail.jrosbridge.messages.geometry.Quaternion;
import edu.wpi.rail.jrosbridge.messages.std.Header;
import edu.wpi.rail.jrosbridge.primitives.Time;
import rx.Subscription;
import rx.subjects.PublishSubject;

import static rx.schedulers.Schedulers.newThread;

public class BridgeSensorEventListener implements SensorEventListener {
    private final static Logger log = LoggerFactory.getLogger(BridgeSensorEventListener.class);
    private final PublishSubject<PoseStamped> poses;
    private Topic topic;
    private static final AtomicInteger SEQUENCE_NUMBER = new AtomicInteger(0);


    public BridgeSensorEventListener(Topic topic) {
        this.topic = topic;
        this.poses = PublishSubject.create();
        // TODO
        Subscription subscribe = poses.onBackpressureDrop()
                .observeOn(newThread())
                .subscribeOn(newThread())
                .subscribe(poseStamped -> {
                    topic.publish(poseStamped);
                });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            this.poses.onNext(quaternionFromEvent(event));

//            float[] quaternion = new float[4];
//            SensorManager.getQuaternionFromVector(quaternion, event.values);
//            this.imu.getOrientation().setW(quaternion[0]);
//            this.imu.getOrientation().setX(quaternion[1]);
//            this.imu.getOrientation().setY(quaternion[2]);
//            this.imu.getOrientation().setZ(quaternion[3]);
//            double[] tmpCov = {0.001, 0, 0, 0, 0.001, 0, 0, 0, 0.001};// TODO Make Parameter
//            this.imu.setOrientationCovariance(tmpCov);
//            this.quatTime = event.timestamp;
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            this.imu.getLinearAcceleration().setX(event.values[0]);
//            this.imu.getLinearAcceleration().setY(event.values[1]);
//            this.imu.getLinearAcceleration().setZ(event.values[2]);
//
//            double[] tmpCov = {0.01, 0, 0, 0, 0.01, 0, 0, 0, 0.01};// TODO Make Parameter
//            this.imu.setLinearAccelerationCovariance(tmpCov);
//            this.accelTime = event.timestamp;
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//            this.imu.getAngularVelocity().setX(event.values[0]);
//            this.imu.getAngularVelocity().setY(event.values[1]);
//            this.imu.getAngularVelocity().setZ(event.values[2]);
//            double[] tmpCov = {0.0025, 0, 0, 0, 0.0025, 0, 0, 0, 0.0025};// TODO Make Parameter
//            this.imu.setAngularVelocityCovariance(tmpCov);
//            this.gyroTime = event.timestamp;
        }
    }

    private PoseStamped quaternionFromEvent(SensorEvent event) {
        float[] quaternion = new float[4];
        SensorManager.getQuaternionFromVector(quaternion, event.values);

        float w = quaternion[0];
        Quaternion quat = new Quaternion(quaternion[1], quaternion[2], quaternion[3], w);
        Point point = new Point();
        Pose pose = new Pose(point, quat);
//        log.debug("quat= {}", quaternion);
        PoseStamped msg = new PoseStamped(new Header(SEQUENCE_NUMBER.getAndIncrement(), Time.fromDate(new Date()), "map"), pose);
        return msg;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        log.debug("accuracy changed {} {}", i, sensor.getType());
    }
}
