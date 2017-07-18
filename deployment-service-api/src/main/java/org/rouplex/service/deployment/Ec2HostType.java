package org.rouplex.service.deployment;

import java.util.HashMap;
import java.util.Map;

public enum Ec2HostType {
    //    T1Micro("t1.micro"), // not available anymore
    T2Nano,
    T2Micro,
    T2Small,
    T2Medium,
    T2Large,
    T2Xlarge,
    T22xlarge,
    M1Small,
    M1Medium,
    M1Large,
    M1Xlarge,
    M3Medium,
    M3Large,
    M3Xlarge,
    M32xlarge,
    M4Large,
    M4Xlarge,
    M42xlarge,
    M44xlarge,
    M410xlarge,
    M416xlarge,
    M2Xlarge,
    M22xlarge,
    M24xlarge,
    Cr18xlarge,
    R3Large,
    R3Xlarge,
    R32xlarge,
    R34xlarge,
    R38xlarge,
    R4Large,
    R4Xlarge,
    R42xlarge,
    R44xlarge,
    R48xlarge,
    R416xlarge,
    X116xlarge,
    X132xlarge,
    I2Xlarge,
    I22xlarge,
    I24xlarge,
    I28xlarge,
    I3Large,
    I3Xlarge,
    I32xlarge,
    I34xlarge,
    I38xlarge,
    I316xlarge,
    Hi14xlarge,
    Hs18xlarge,
    C1Medium,
    C1Xlarge,
    C3Large,
    C3Xlarge,
    C32xlarge,
    C34xlarge,
    C38xlarge,
    C4Large,
    C4Xlarge,
    C42xlarge,
    C44xlarge,
    C48xlarge,
    Cc14xlarge,
    Cc28xlarge,
    G22xlarge,
    G28xlarge,
    Cg14xlarge,
    P2Xlarge,
    P28xlarge,
    P216xlarge,
    D2Xlarge,
    D22xlarge,
    D24xlarge,
    D28xlarge,
    F12xlarge,
    F116xlarge;

    static final Map<Ec2HostType, String> enumToString = new HashMap<Ec2HostType, String>() {{
        put(T2Nano, "t2.nano");
        put(T2Micro, "t2.micro");
        put(T2Small, "t2.small");
        put(T2Medium, "t2.medium");
        put(T2Large, "t2.large");
        put(T2Xlarge, "t2.xlarge");
        put(T22xlarge, "t2.2xlarge");
        put(M1Small, "m1.small");
        put(M1Medium, "m1.medium");
        put(M1Large, "m1.large");
        put(M1Xlarge, "m1.xlarge");
        put(M3Medium, "m3.medium");
        put(M3Large, "m3.large");
        put(M3Xlarge, "m3.xlarge");
        put(M32xlarge, "m3.2xlarge");
        put(M4Large, "m4.large");
        put(M4Xlarge, "m4.xlarge");
        put(M42xlarge, "m4.2xlarge");
        put(M44xlarge, "m4.4xlarge");
        put(M410xlarge, "m4.10xlarge");
        put(M416xlarge, "m4.16xlarge");
        put(M2Xlarge, "m2.xlarge");
        put(M22xlarge, "m2.2xlarge");
        put(M24xlarge, "m2.4xlarge");
        put(Cr18xlarge, "cr1.8xlarge");
        put(R3Large, "r3.large");
        put(R3Xlarge, "r3.xlarge");
        put(R32xlarge, "r3.2xlarge");
        put(R34xlarge, "r3.4xlarge");
        put(R38xlarge, "r3.8xlarge");
        put(R4Large, "r4.large");
        put(R4Xlarge, "r4.xlarge");
        put(R42xlarge, "r4.2xlarge");
        put(R44xlarge, "r4.4xlarge");
        put(R48xlarge, "r4.8xlarge");
        put(R416xlarge, "r4.16xlarge");
        put(X116xlarge, "x1.16xlarge");
        put(X132xlarge, "x1.32xlarge");
        put(I2Xlarge, "i2.xlarge");
        put(I22xlarge, "i2.2xlarge");
        put(I24xlarge, "i2.4xlarge");
        put(I28xlarge, "i2.8xlarge");
        put(I3Large, "i3.large");
        put(I3Xlarge, "i3.xlarge");
        put(I32xlarge, "i3.2xlarge");
        put(I34xlarge, "i3.4xlarge");
        put(I38xlarge, "i3.8xlarge");
        put(I316xlarge, "i3.16xlarge");
        put(Hi14xlarge, "hi1.4xlarge");
        put(Hs18xlarge, "hs1.8xlarge");
        put(C1Medium, "c1.medium");
        put(C1Xlarge, "c1.xlarge");
        put(C3Large, "c3.large");
        put(C3Xlarge, "c3.xlarge");
        put(C32xlarge, "c3.2xlarge");
        put(C34xlarge, "c3.4xlarge");
        put(C38xlarge, "c3.8xlarge");
        put(C4Large, "c4.large");
        put(C4Xlarge, "c4.xlarge");
        put(C42xlarge, "c4.2xlarge");
        put(C44xlarge, "c4.4xlarge");
        put(C48xlarge, "c4.8xlarge");
        put(Cc14xlarge, "cc1.4xlarge");
        put(Cc28xlarge, "cc2.8xlarge");
        put(G22xlarge, "g2.2xlarge");
        put(G28xlarge, "g2.8xlarge");
        put(Cg14xlarge, "cg1.4xlarge");
        put(P2Xlarge, "p2.xlarge");
        put(P28xlarge, "p2.8xlarge");
        put(P216xlarge, "p2.16xlarge");
        put(D2Xlarge, "d2.xlarge");
        put(D22xlarge, "d2.2xlarge");
        put(D24xlarge, "d2.4xlarge");
        put(D28xlarge, "d2.8xlarge");
        put(F12xlarge, "f1.2xlarge");
        put(F116xlarge, "f1.16xlarge");
    }};

    static final Map<String, Ec2HostType> stringToEnum = new HashMap<String, Ec2HostType>() {{
        for (Entry<Ec2HostType, String> entry : enumToString.entrySet()) {
            put(entry.getValue(), entry.getKey());
        }
    }};

    public static Ec2HostType fromString(String string) {
        Ec2HostType hostType = stringToEnum.get(string);
        if (hostType != null) {
            return hostType;
        }

        throw new IllegalArgumentException("Cannot create enum from " + string + " value!");
    }

    @Override
    public String toString() {
        return enumToString.get(this);
    }
}