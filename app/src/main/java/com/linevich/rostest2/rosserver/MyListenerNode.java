package com.linevich.rostest2.rosserver;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.linevich.rostest2.OrientationEventListener;

import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import geometry_msgs.PoseStamped;

public class MyListenerNode extends AbstractNodeMain {

    public static final Logger log = LoggerFactory.getLogger(MyListenerNode.class);
    private SensorManager sensorManager;

    public MyListenerNode(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android/orientiation_sensor");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        super.onStart(connectedNode);

        Publisher<PoseStamped> posePublisher = connectedNode.newPublisher("android/orientation", "geometry_msgs/PoseStamped");

        PublisherListener<PoseStamped> listener = new TopicListener();
        posePublisher.addListener(listener);

        OrientationEventListener orientationEventListener = new OrientationEventListener(posePublisher);

        Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        // 10Hz
        sensorManager.registerListener(orientationEventListener, rotationSensor, 50000);
    }

    private class TopicListener implements PublisherListener<PoseStamped> {
        @Override
        public void onNewSubscriber(Publisher<PoseStamped> publisher, SubscriberIdentifier subscriberIdentifier) {
            log.debug("newSubscriber {}", subscriberIdentifier);
        }

        @Override
        public void onShutdown(Publisher<PoseStamped> publisher) {
            log.debug("onShutdown {}", publisher);
        }

        @Override
        public void onMasterRegistrationSuccess(Publisher<PoseStamped> poseStampedPublisher) {

        }

        @Override
        public void onMasterRegistrationFailure(Publisher<PoseStamped> poseStampedPublisher) {

        }

        @Override
        public void onMasterUnregistrationSuccess(Publisher<PoseStamped> poseStampedPublisher) {

        }

        @Override
        public void onMasterUnregistrationFailure(Publisher<PoseStamped> poseStampedPublisher) {

        }
    }
}
