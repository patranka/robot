package it.unibz.jpantiuchina.robot.util;


public final class RobotMath
{
    public static float constrain(float value, float min, float max)
    {
        return Math.max(min, Math.min(value, max));
    }

    private static float map(float value, float inMin, float inMax, float outMin, float outMax)
    {
        return (value - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    public static float mapAndConstrain(float value, float inMin, float inMax, float outMin, float outMax)
    {
        return map(constrain(value, inMin, inMax), inMin, inMax, outMin, outMax);
    }
}
