package com.example.jorda.magnetart.utilities;

/**
 * Created by jordan on 14/08/2017.
 */

/**
 * This class contains algorithms attempted when calibrating the device.
 * Agorithms detailed are - fourcorners (assume linear and non-linear field), trilateration, rotation matrices, projection on planes
 */
public class CalibrationCalculations {

    /**
     * Method to take sensor data and produce an estimated set of screen co-ordinates
     * @param algorithm
     * @param topleft
     * @param topright
     * @param botleft
     * @param botright
     * @param current
     * @param dwidth
     * @param dheight
     * @return
     */
    public static MagnetPosition Calibrate (int algorithm, MagnetPosition topleft, MagnetPosition topright, MagnetPosition botleft, MagnetPosition botright, MagnetPosition current, double dwidth, double dheight) {
        double xLoc = 0, yLoc = 0;

        //4 corners algorithm
        if (algorithm == 1) {

            //calculate fractional distance along surface in x direction at top
        double xloc1 = (topleft.xPosition-current.xPosition)/(topleft.xPosition - topright.xPosition);
            //calculate fractional distance along surface in x direction at bottom
        double xloc2 = (botleft.xPosition-current.xPosition)/(botleft.xPosition - botright.xPosition);
            //take an average
        double avgx = (xloc1+xloc2)/2;
            //calculate fractional distance along y axis at left
        double yloc1 = (topleft.yPosition-current.yPosition)/(topleft.yPosition - botleft.yPosition);
            //calculate fractional distance along y axis on right
        double yloc2 = (topright.yPosition-current.yPosition)/(topright.yPosition - botright.yPosition);
//            take an average y
            double avgy = (yloc1 + yloc2) / 2;

            xLoc = avgx*dwidth;//multiply by screen dimensions
            yLoc = avgy*dheight;


            //trilateration
        } else if (algorithm == 2) {

            //Treat top left, top right and bottom left as reference points
            //Ignore z dimension- assume zero
            double i1 = topleft.xPosition;
            double i2 = topright.xPosition;
            double i3 = botleft.xPosition;
            double j1 = topleft.yPosition;
            double j2 = topright.yPosition;
            double j3 = botleft.yPosition;
            double x = current.xPosition;
            double y = current.yPosition;

            // Distances from reference points to current point
            double distxi1 = x - i1;
            double distyj1 = y - j1;
            double distxi2 = x - i2;
            double distyj2 = y - j2;
            double distxi3 = x - i3;
            double distyj3 = y - j3;

            double d1 = Math.sqrt(Math.pow(distxi1, 2) + Math.pow(distyj1, 2));//distance from top left
            double d2 = Math.sqrt(Math.pow(distxi2, 2) + Math.pow(distyj2, 2));//distance from top right
            double d3 = Math.sqrt(Math.pow(distxi3, 2) + Math.pow(distyj3, 2));//distance from bottom left

            //Solve simultaneous equations for x and cast as xPos to estimate the location
            double xPos = ((Math.pow(d1, 2) - Math.pow(d2, 2) - Math.pow(i1, 2) + Math.pow(i2, 2) - Math.pow(j1, 2) + Math.pow(j2, 2)) * (-2 * j2 + 2 * j3)
                    - (Math.pow(d2, 2) - Math.pow(d3, 2) - Math.pow(i2, 2) + Math.pow(i3, 2) - Math.pow(j2, 2) + Math.pow(j3, 2)) * (-2 * j1 + 2 * j2)) / ((-2 * j2 + 2 * j3) * (-2 * i1 + 2 * i2)
                    - (-2 * j1 + 2 * j2) * (-2 * i2 + 2 * i3));


//        Solve equations for y
            double yPos = (((Math.pow(d1, 2) - Math.pow(d2, 2) - Math.pow(i1, 2) + Math.pow(i2, 2) - Math.pow(j1, 2) + Math.pow(j2, 2)) * (-2 * i2 + 2 * i3) - (-2 * i1 + 2 * i2) *
                    (Math.pow(d2, 2) - Math.pow(d3, 2) - Math.pow(i2, 2) + Math.pow(i3, 2) - Math.pow(j2, 2) + Math.pow(j3, 2))) / (((-2 * j1 + 2 * j2) * (-2 * i2 + 2 * i3)) - (-2 * i1 + 2 * i2) * (-2 * j2 + 2 * j3)));


            // Find estimated position as a fraction of x and distance and multiply by screen dimensions
            xLoc = Math.abs(((i1 - xPos) / (i1 - botright.xPosition))) * dwidth;
            yLoc = Math.abs(((j1 - yPos) / (j1 - botright.yPosition))) * dheight;

        }
        return new MagnetPosition(xLoc, yLoc, 0);
    }
}
