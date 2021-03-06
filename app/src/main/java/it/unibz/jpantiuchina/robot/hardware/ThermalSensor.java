package it.unibz.jpantiuchina.robot.hardware;

import java.util.Arrays;


final class ThermalSensor
{
    private byte ambientTemperature;
    private final byte[] pixels = new byte[8];


    void readFromScanResult(byte[] buffer)
    {

        ambientTemperature = buffer[17];
        System.arraycopy(buffer, 18, pixels, 0, pixels.length);
    }

    private byte getAmbientTemperatureInDegreesC()
    {
        return ambientTemperature;
    }

    private byte[] getPixelsInDegreesC()
    {
        return pixels;
    }

    @Override
    public String toString()
    {
        return "Temperature Sensor: " + getAmbientTemperatureInDegreesC() + " C " +
                Arrays.toString(getPixelsInDegreesC());
    }
}
