package com.linevich.rostest2.rosserver;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.linevich.rostest2.OrientationEventListener;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import geometry_msgs.PoseStamped;

public class OrientationPublisherNode extends AbstractNodeMain {
    public static final Logger log = LoggerFactory.getLogger(OrientationPublisherNode.class);

    private SensorManager sensorManager;

    public OrientationPublisherNode(SensorManager sensorManager) {
        log.debug("orientation");
        this.sensorManager = sensorManager;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android/orientiation_sensor");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        log.debug("hello {}", connectedNode);
        super.onStart(connectedNode);

        Publisher<PoseStamped> posePublisher = connectedNode.newPublisher("android/orientation", "geometry_msgs/PoseStamped");
        OrientationEventListener orientationEventListener = new OrientationEventListener(posePublisher);

        Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        // 10Hz
//        sensorManager.registerListener(orientationEventListener, rotationSensor, 50000);
        sensorManager.registerListener(orientationEventListener, rotationSensor, 5000000);
    }

}
