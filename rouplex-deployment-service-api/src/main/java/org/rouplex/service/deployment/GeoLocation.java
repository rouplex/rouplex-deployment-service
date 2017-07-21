package org.rouplex.service.deployment;

import java.util.HashMap;
import java.util.Map;

public enum GeoLocation {
    US_WEST_2,
    GovCloud,
    US_EAST_1,
    US_EAST_2,
    US_WEST_1,
    EU_WEST_1,
    EU_WEST_2,
    EU_CENTRAL_1,
    AP_SOUTH_1,
    AP_SOUTHEAST_1,
    AP_SOUTHEAST_2,
    AP_NORTHEAST_1,
    AP_NORTHEAST_2,
    SA_EAST_1,
    CN_NORTH_1,
    CA_CENTRAL_1;

    static final Map<GeoLocation, String> enumToString = new HashMap<GeoLocation, String>() {{
        put(US_WEST_2, "us-west-2");
        put(GovCloud, "us-gov-west-1");
        put(US_EAST_1, "us-east-1");
        put(US_EAST_2, "us-east-2");
        put(US_WEST_1, "us-west-1");
        put(EU_WEST_1, "eu-west-1");
        put(EU_WEST_2, "eu-west-2");
        put(EU_CENTRAL_1, "eu-central-1");
        put(AP_SOUTH_1, "ap-south-1");
        put(AP_SOUTHEAST_1, "ap-southeast-1");
        put(AP_SOUTHEAST_2, "ap-southeast-2");
        put(AP_NORTHEAST_1, "ap-northeast-1");
        put(AP_NORTHEAST_2, "ap-northeast-2");
        put(SA_EAST_1, "sa-east-1");
        put(CN_NORTH_1, "cn-north-1");
        put(CA_CENTRAL_1, "ca-central-1");
    }};

    static final Map<String, GeoLocation> stringToEnum = new HashMap<String, GeoLocation>() {{
        for (Entry<GeoLocation, String> entry : enumToString.entrySet()) {
            put(entry.getValue(), entry.getKey());
        }
    }};

    public static GeoLocation fromString(String string) {
        GeoLocation geoLocation = stringToEnum.get(string);
        if (geoLocation != null) {
            return geoLocation;
        }

        throw new IllegalArgumentException("Cannot create enum from " + string + " value!");
    }

    @Override
    public String toString() {
        return enumToString.get(this);
    }
}
