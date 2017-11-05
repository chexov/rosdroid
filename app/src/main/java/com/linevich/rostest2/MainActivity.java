package com.linevich.rostest2;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.linevich.rostest2.rosbridge.BridgeNavSatLocationListener;
import com.linevich.rostest2.rosbridge.BridgeSensorEventListener;
import com.linevich.rostest2.rosserver.OrientationPublisherNode;

import org.ros.android.view.RosImageView;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import edu.wpi.rail.jrosbridge.JRosbridge;
import edu.wpi.rail.jrosbridge.Ros;
import edu.wpi.rail.jrosbridge.Topic;
import rx.Emitter;
import rx.Observable;
import sensor_msgs.CompressedImage;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.widget.Toast.LENGTH_SHORT;
import static rx.schedulers.Schedulers.newThread;

public class MainActivity extends AppCompatActivity {
    private final static Logger log = LoggerFactory.getLogger(MainActivity.class);

    private RosImageView<CompressedImage> cameraView;
    private NodeMainExecutor nodeMainExecutor;
    private NodeConfiguration nodeConfiguration;
    private OrientationPublisherNode orientationNodeMain;

    public MainActivity() {
        super();
        // The RosActivity constructor configures the notification title and
        // ticker
        // messages.
//        super("Make a map", "Make a map");

    }

    static void bridgetest() {

//        Topic echoBack = new Topic(ros, "/echo_back", "std_msgs/String");
//        echoBack.subscribe(new TopicCallback() {
//            @Override
//            public void handleMessage(Message message) {
//                System.out.println("From ROS: " + message.toString());
//            }
//        });
//
//        Service addTwoInts = new Service(ros, "/add_two_ints", "rospy_tutorials/AddTwoInts");
//
//        ServiceRequest request = new ServiceRequest("{\"a\": 10, \"b\": 20}");
//        ServiceResponse response = addTwoInts.callServiceAndWait(request);
//        System.out.println(response.toString());
//
//        ros.disconnect();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        TextView text = findViewById(R.id.textview);
//        text.append("hello\n");


        log.debug("hello");
        rosBridgeConnect("anton.videogorillas.com", 9090, JRosbridge.WebSocketType.ws)
                .subscribeOn(newThread())
                .subscribe(ros -> {
                    log.debug("ros connected {}", ros.isConnected());

                    log.debug("location");
                    locationTopic(ros);

                    log.debug("orientation");
                    orientationTopic(ros);
                });
//        text.append("ros connected\n");


        Toast.makeText(this, "ros ok", LENGTH_SHORT).show();
    }

    private void orientationTopic(Ros ros) {
        Topic topic = new Topic(ros, "android/orientation", "geometry_msgs/PoseStamped");
        log.debug("orientation topic subscribed{}", topic.isSubscribed());

        SensorManager sensorManager = sensorManager();
        Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        // 10Hz
        sensorManager.registerListener(new BridgeSensorEventListener(topic), rotationSensor, 50000);
    }

    private SensorManager sensorManager() {
        return getBaseContext().getSystemService(SensorManager.class);
    }

    private void locationTopic(Ros ros) {
        Topic topic = new Topic(ros, "android/fix", "sensor_msgs/NavSatFix");
        log.debug("location topic subscribed{}", topic.isSubscribed());

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 43);
            return;
        }

        LocationManager locationManager = locactionManager();
        BridgeNavSatLocationListener navSatListener = new BridgeNavSatLocationListener(locationManager, topic);
    }

    private LocationManager locactionManager() {
        return (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    private Observable<Ros> rosBridgeConnect(String hostname, int port, JRosbridge.WebSocketType ws) {
        Ros ros = new Ros(hostname, port, ws);
        return Observable.create(o -> {
            o.setCancellation(() -> {
                ros.disconnect();
            });

            if (ros.connect()) {
                o.onNext(ros);
            } else {
                o.onError(new Throwable("ros connection error"));
            }

        }, Emitter.BackpressureMode.ERROR);
    }

    protected void onCreateRosServer(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        SensorManager sensor = sensorManager();
        orientationNodeMain = new OrientationPublisherNode(sensor);
        nodeMainExecutor = DefaultNodeMainExecutor.newDefault();

        try {
            log.debug("Connecting to ROS...");
            URI masterURI = URI.create("http://anton.videogorillas.com:11411/");
            nodeConfiguration = NodeConfiguration.newPublic("localhost", masterURI);
            nodeMainExecutor.execute(orientationNodeMain, nodeConfiguration);
        } catch (Throwable e) {
            log.error("error", e);
        }

//        MyListenerNode mylist = new MyListenerNode(sensor);
//        nodeMainExecutor.execute(mylist, nodeConfiguration);
    }

    //    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        this.nodeMainExecutor = nodeMainExecutor;
//        this.nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(), getMasterUri());

        nodeMainExecutor.execute(orientationNodeMain, nodeConfiguration);


    }
}
