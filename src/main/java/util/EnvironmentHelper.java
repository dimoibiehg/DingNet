package util;

import IotDomain.Environment;
import IotDomain.networkentity.NetworkEntity;

import java.util.stream.Stream;

public class EnvironmentHelper {

    public static NetworkEntity getNetworkEntityById(Environment env, long id) {
        return Stream.concat(env.getMotes().stream(), env.getGateways().stream())
            .filter(ne -> ne.getEUI() == id)
            .findFirst()
            .orElseThrow();
    }
}