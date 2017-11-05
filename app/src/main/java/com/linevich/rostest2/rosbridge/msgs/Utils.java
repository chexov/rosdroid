package com.linevich.rostest2.rosbridge.msgs;

import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;

public class Utils {
    public static JsonBuilderFactory jsonBuilderFactory = Json.createBuilderFactory(new HashMap());

    public static JsonArrayBuilder jsonArray(double[] position_covariance) {
        JsonArrayBuilder arrayBuilder = Utils.jsonBuilderFactory.createArrayBuilder();
        for (int i = 0; i < position_covariance.length; i++) {
            arrayBuilder.add(position_covariance[i]);
        }
        return arrayBuilder;
    }

}
