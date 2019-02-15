package com.example.jorda.magnetart.utilities;

/**
 * Created by jordan on 14/08/2017.
 */

//An object which consists of magnetic field readings in the x,y and z axes
public class MagnetPosition {

        public double xPosition;
        public double yPosition;
        public double zPosition;

        //Constructor to produce an object using 3 variables type double
        public MagnetPosition(double x, double y, double z) {

            xPosition = x;
            yPosition = y;
            zPosition = z;
        }

        //Constructor to producr an object using an array of 3 doubles
        public MagnetPosition(double[] readings)
        {
            if(readings.length == 3) {
                xPosition = readings[0];
                yPosition = readings[1];
                zPosition = readings[2];
            }
        }

        //Method to create an array using 3 double variables
        public double[] toDoubleArray()
        {
            return new double[]{xPosition, yPosition, zPosition};
        }

    //calculate magnitude of position, this is used when choosing colours
    public double magnitude(){
        return Math.sqrt(Math.pow(xPosition,2) + Math.pow(yPosition,2) + Math.pow(zPosition,2));
    }
}
