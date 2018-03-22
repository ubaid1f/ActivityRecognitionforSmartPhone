package dataobject;

import java.io.Serializable;

/**
 * Created by euguzns on 2018-01-19.
 */

public class SensorObject implements Serializable {

    private static final long serialVersionUID = 1234567890;
    MessageType msgType;
    String actType;

    // Array to store ACC data
    private double[] accX;
    private double[] accY;
    private double[] accZ;

    private int sensorSize = 50;

    public SensorObject() {
        accX = new double[sensorSize];
        accY = new double[sensorSize];
        accZ = new double[sensorSize];
    }

    public SensorObject(MessageType msgtype) {
        this.msgType = msgtype;
    }

    public SensorObject(MessageType msgtype, String actType) {
        this.msgType = msgtype;
        this.actType = actType;
        accX = new double[sensorSize];
        accY = new double[sensorSize];
        accZ = new double[sensorSize];
//        }
    }

    public MessageType getMsgtype() {
        return msgType;
    }

    public String getActType() {
        return actType;
    }


    public double[] getAccX() {
        return accX;
    }

    public double[] getAccY() {
        return accY;
    }

    public double[] getAccZ() {
        return accZ;
    }

    public void setData(double[] x, double[] y, double[] z) {
        accX = x;
        accY = y;
        accZ = z;
    }
}
