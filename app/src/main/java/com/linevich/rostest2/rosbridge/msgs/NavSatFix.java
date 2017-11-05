package com.linevich.rostest2.rosbridge.msgs;

import android.location.Location;

import java.util.Arrays;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import edu.wpi.rail.jrosbridge.messages.Message;
import edu.wpi.rail.jrosbridge.messages.std.Header;
import edu.wpi.rail.jrosbridge.primitives.Time;

public class NavSatFix extends Message {
    public static final String TYPE = "sensor_msgs/NavSatFix";

    /*
# satellite fix status information
NavSatStatus status

# Latitude [degrees]. Positive is north of equator; negative is south.
float64 latitude

# Longitude [degrees]. Positive is east of prime meridian; negative is west.
float64 longitude

# Altitude [m]. Positive is above the WGS 84 ellipsoid
# (quiet NaN if no altitude is available).
float64 altitude

# Position covariance [m^2] defined relative to a tangential plane
# through the reported position. The components are East, North, and
# Up (ENU), in row-major order.
#
# Beware: this coordinate system exhibits singularities at the poles.

float64[9] position_covariance

# If the covariance of the fix is known, fill it in completely. If the
# GPS receiver provides the variance of each measurement, put them
# along the diagonal. If only Dilution of Precision is available,
# estimate an approximate covariance from that.

uint8 COVARIANCE_TYPE_UNKNOWN = 0
uint8 COVARIANCE_TYPE_APPROXIMATED = 1
uint8 COVARIANCE_TYPE_DIAGONAL_KNOWN = 2
uint8 COVARIANCE_TYPE_KNOWN = 3

uint8 position_covariance_type
     */

    public Header header;
    public NavSatStatus status;
    public double latitude;
    public double longitude;
    public double altitude;
    public double[] position_covariance;
    public byte position_covariance_type;

    public static byte COVARIANCE_TYPE_UNKNOWN = 0;
    public static byte COVARIANCE_TYPE_APPROXIMATED = 1;
    public static byte COVARIANCE_TYPE_DIAGONAL_KNOWN = 2;
    public static byte COVARIANCE_TYPE_KNOWN = 3;

    public NavSatFix(Header header, NavSatStatus status, double latitude, double longitude, double altitude, double[] position_covariance, byte position_covariance_type) {
        super(Json.createObjectBuilder()
                        .add("header", header.toJsonObject())
                        .add("status", status.toJsonObject())
                        .add("latitude", latitude)
                        .add("longitude", longitude)
                        .add("altitude", altitude)
                        .add("position_covariance", Utils.jsonArray(position_covariance))
                        .build(),
                NavSatFix.TYPE);
        this.header = header;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.position_covariance = position_covariance;
        this.position_covariance_type = position_covariance_type;
    }

    public static NavSatFix fromLocation(int seq, byte currentStatus, Location loc) {
        Header header = new Header(seq, Time.fromDate(new Date()), "gps");
        NavSatStatus s = new NavSatStatus(currentStatus, NavSatStatus.SERVICE_GPS);

        double deviation = loc.getAccuracy();
        double covariance = deviation * deviation;
        double[] tmpCov = {covariance, 0, 0, 0, covariance, 0, 0, 0, covariance};

        return new NavSatFix(header, s, loc.getLatitude(), loc.getLongitude(), loc.getAltitude(), tmpCov, (byte) loc.getAccuracy());
    }
}
