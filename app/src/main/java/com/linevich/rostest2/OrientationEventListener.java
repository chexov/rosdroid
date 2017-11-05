package com.linevich.rostest2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.ros.node.topic.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import geometry_msgs.Pose;
import geometry_msgs.PoseStamped;

public class OrientationEventListener implements SensorEventListener {
    private final static Logger log = LoggerFactory.getLogger(OrientationEventListener.class);
    private Publisher<PoseStamped> pub;

    public OrientationEventListener(Publisher<PoseStamped> pub) {
        this.pub = pub;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] quaternion = new float[4];
            SensorManager.getQuaternionFromVector(quaternion, event.values);
            PoseStamped msg = pub.newMessage();
            msg.getHeader().setFrameId("map");
            Pose pose = msg.getPose();
            pose.getOrientation().setW(quaternion[0]);
            pose.getOrientation().setX(quaternion[1]);
            pose.getOrientation().setY(quaternion[2]);
            pose.getOrientation().setX(quaternion[3]);
            log.debug("pose= {}", quaternion);
            pub.publish(msg);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
