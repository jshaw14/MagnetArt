package com.example.jorda.magnetart.utilities;

/**
 * Created by jordan on 03/09/2017.
 */

public class MagnetCalculations {

    private int buffsize = 500;
    private double[] xarray = new double[buffsize];
    private double[] yarray = new double[buffsize];
    private double[] zarray = new double[buffsize];
    private int iter = 1;
    private boolean bufferfull = false;
    private double dx = 0, dy = 0, dz = 0, ex = 0, ey = 0, ez = 0, bx = 0, by = 0, bz = 0;
    private static final float ALPHA = 0.1f;
    private double l = 0.001;//magnet length- in this case 1cm
    private double dr, r, xa, ya;
    private MagnetPosition earthp = new MagnetPosition(0,0,0);//create earth magnetposition and pass to newdrawingfrag to display earth field

    public MagnetPosition getEarthp() {
        return earthp;
    }

    public boolean isBufferfull() {
        return bufferfull;
    }

    /**
     * Method to correct readings for the terrestrial magnetic field
     * @param eventvalues
     * @return
     */
    public MagnetPosition correctReadings(float[] eventvalues) {

        double sumex = 0, sumey = 0, sumez = 0;//buffers are initially empty

        if (!bufferfull) {
            //Add new sensor readings to the buffer to fill it
            if (xarray[buffsize - 1] == 0) {
                xarray[iter] = eventvalues[0];
                yarray[iter] = eventvalues[1];
                zarray[iter] = eventvalues[2];
                iter++;

            } else {
                //Add all buffer values in x, y, and z dimensions
                for (int i = 0; i < buffsize; i++) {
                    sumex += xarray[i];
                    sumey += yarray[i];
                    sumez += zarray[i];
                }
                //Take an average value for each dimension, this is taken to be the terrestrial magnetic field
                ex = sumex / buffsize;
                ey = sumey / buffsize;
                ez = sumez / buffsize;

                earthp = new MagnetPosition(ex,ey,ez);//the estimated terrestrial field in 3 dimensions

                bufferfull = true;//x, y and z buffers have been filled

            }

        }

        bx = eventvalues[2];
        by = eventvalues[1];
        bz = eventvalues[0];

        //correct for the terrestrial magnetic field
        dx = bx - ex;
        dy = by - ey;
        dz = bz - ez;

        return new MagnetPosition(dx, dy, dz);
    }

    /**
     * Reset buffers to recalculate terrestrial field
     */
    public void clearBuffers(){

        xarray = new double[buffsize];
        yarray = new double[buffsize];
        zarray = new double[buffsize];
        bufferfull = false;
        iter = 0;
    }


    /**
     * Low-Pass filter to attenuate high frequency signals and ensure continuous data
     * @param input
     * @param output
     * @return
     */
    public static double[] lowPass(double[] input, double[] output) {

        if (output == null) {
            return input;//first set of data- return input
        }
        for (int i = 0; i < input.length; i++) {
            output[i] += ALPHA * (input[i] - output[i]);//ALPHA controls how much of the input signal is attenuated
        }
        return output;
    }

    /**
     * Method to calculate the relative co-ordinates by solving Coulomb's equation
     * @param currentpos
     * @return
     */
    public MagnetPosition geometryCalc(MagnetPosition currentpos){

        double dx = currentpos.xPosition;
        double dy = currentpos.yPosition;

        dr = - Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2));//Incremental distance in direction of magnet
        r=bisectionMethod(currentpos, dr);
        xa = r*Math.cos(Math.atan(dy/dx));
        ya = r*Math.sin(Math.atan(dy/dx));

        return new MagnetPosition(xa,ya,currentpos.zPosition);

    }

    /**
     * Vector r in direction of magnet can be estimated using the bisection method
     * @param currentpos
     * @param dr
     * @return
     */
    private double bisectionMethod(MagnetPosition currentpos, Double dr){

        double a = 0.001;//lower boundary of interval
        double tolerance = 0.00001;
        double b=1;//upper boundary of interval
        double p,fa,fp;
        int n = 500;//max number of iterations
        int iter;//iteration number

        iter = 1;
        fa=equationFunc(currentpos, a, dr);

        while (iter<=n){
            p = a + (b-a)/2;
            fp = equationFunc(currentpos, p, dr);

            if ((fp == 0) || ((b-a) < tolerance))
                return p;
            iter++;

            if (fp*fa > 0){
                a = p;
                fa = fp;
            }
            else
                b=p;
        }
        return 0.01;
    }

    /**
     * Plug values from each iteration into the equation
     * @param cp
     * @param esr
     * @param dr
     * @return
     */
    private double equationFunc(MagnetPosition cp, double esr, double dr){
        double dz = cp.zPosition;
        //Coulomb equation
        return Math.pow(l/esr,5)+3*Math.pow(l/esr,3)+(3-Math.pow(dr/dz,2))*(l/esr)+2*dr/dz;
    }
}