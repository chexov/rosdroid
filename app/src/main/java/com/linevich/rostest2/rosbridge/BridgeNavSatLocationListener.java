package com.linevich.rostest2.rosbridge;

import android.annotation.SuppressLint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;

import com.linevich.rostest2.rosbridge.msgs.NavSatFix;
import com.linevich.rostest2.rosbridge.msgs.NavSatStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import edu.wpi.rail.jrosbridge.Topic;
import rx.Subscription;
import rx.subjects.PublishSubject;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static rx.schedulers.Schedulers.newThread;

public class BridgeNavSatLocationListener implements LocationListener {
    private final static Logger log = LoggerFactory.getLogger(BridgeNavSatLocationListener.class);
    private final PublishSubject<NavSatFix> msgs;
    private final NavSatThread navSatThread;
    private volatile byte currentStatus;
    private Topic topic;
    private static final AtomicInteger SEQUENCE_NUMBER = new AtomicInteger(0);

    public static class NavSatThread extends Thread {
        LocationManager locationManager;
        BridgeNavSatLocationListener navSatListener;
        private Looper threadLooper;

        public NavSatThread(LocationManager locationManager, BridgeNavSatLocationListener navSatListener) {
            this.locationManager = locationManager;
            this.navSatListener = navSatListener;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            Looper.prepare();
            threadLooper = Looper.myLooper();


            log.debug("nav sat thread started");
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_FINE);
            crit.setPowerRequirement(Criteria.POWER_LOW);
            // Gets the best matched provider, and only if it's on
            String p = locationManager.getBestProvider(crit, true);

            navSatListener.sendLastKnown(locationManager.getLastKnownLocation(GPS_PROVIDER));
            locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, navSatListener);

            Looper.loop();
        }

        public void shutdown() {
            log.debug("nav sat thread stopped");
            this.locationManager.removeUpdates(this.navSatListener);
            if (threadLooper != null) {
                threadLooper.quit();
            }
        }
    }


    public BridgeNavSatLocationListener(LocationManager locationManager, Topic topic) {
        this.navSatThread = new NavSatThread(locationManager, this);
        this.navSatThread.start();

        this.topic = topic;
        this.currentStatus = NavSatStatus.STATUS_FIX; // Default to fix until we are told otherwise.

        this.msgs = PublishSubject.create();
        Subscription subscribe = msgs.onBackpressureDrop()
                .observeOn(newThread())
                .subscribeOn(newThread())
                .subscribe(msg -> {
                    log.debug("navfix " + msg);
                    topic.publish(msg);
                });

        log.debug("navsat listener");
    }


    public void sendLastKnown(Location location) {
        log.debug("location changed");
        this.msgs.onNext(NavSatFix.fromLocation(SEQUENCE_NUMBER.getAndIncrement(), currentStatus, location));
    }

    @Override
    public void onLocationChanged(Location location) {
        log.debug("location changed");
        this.msgs.onNext(NavSatFix.fromLocation(SEQUENCE_NUMBER.getAndIncrement(), currentStatus, location));
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                currentStatus = NavSatStatus.STATUS_NO_FIX;
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                currentStatus = NavSatStatus.STATUS_NO_FIX;
                break;
            case LocationProvider.AVAILABLE:
                currentStatus = NavSatStatus.STATUS_FIX;
                break;
        }
    }

}
