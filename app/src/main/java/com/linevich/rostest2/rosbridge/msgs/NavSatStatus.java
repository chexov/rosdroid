package com.linevich.rostest2.rosbridge.msgs;

import javax.json.Json;

import edu.wpi.rail.jrosbridge.messages.Message;

public class NavSatStatus extends Message {
    /*
# Navigation Satellite fix status for any Global Navigation Satellite System
     */

    static final String TYPE = "sensor_msgs/NavSatStatus";

    /*
# Whether to output an augmented fix is determined by both the fix
# type and the last time differential corrections were received.  A
# fix is valid when status >= STATUS_FIX.

int8 STATUS_NO_FIX =  -1        # unable to fix position
int8 STATUS_FIX =      0        # unaugmented fix
int8 STATUS_SBAS_FIX = 1        # with satellite-based augmentation
int8 STATUS_GBAS_FIX = 2        # with ground-based augmentation
int8 status
     */

    public static byte STATUS_NO_FIX = -1;
    public static byte STATUS_FIX = 0;
    public static byte STATUS_SBAS_FIX = 1;
    public static byte STATUS_GBAS_FIX = 2;
    public byte status;

    /*
# Bits defining which Global Navigation Satellite System signals were
# used by the receiver.

uint16 SERVICE_GPS =     1
uint16 SERVICE_GLONASS = 2
uint16 SERVICE_COMPASS = 4      # includes BeiDou.
uint16 SERVICE_GALILEO = 8

uint16 service
     */
    public static short SERVICE_GPS = 1;
    public static short SERVICE_GLONASS = 2;
    public static short SERVICE_COMPASS = 4;
    public static short SERVICE_GALILEO = 8;
    public short service;

    public NavSatStatus(byte status, short service) {
        super(Json.createObjectBuilder()
                        .add("status", status)
                        .add("service", service).build(),
                NavSatStatus.TYPE);
        this.status = status;
        this.service = service;
    }
}
