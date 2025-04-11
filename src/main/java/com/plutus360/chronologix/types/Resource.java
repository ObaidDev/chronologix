package com.plutus360.chronologix.types;

public enum Resource {
    MQTT("mqtt"),
    DEVICES("gw/devices"),
    GEOFENCES("gw/geofences"),
    CALCS("gw/calcs") ,
    POIS("gw/pois") ;

    private final String path;

    Resource(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    // Validate if a path is a valid resource
    public static boolean isValidPath(String path) {
        for (Resource resource : values()) {
            if (resource.getPath().equals(path)) {
                return true;
            }
        }
        return false;
    }
}


