/*
 * Title:        DoDeco
 * Description:  DoDeco generates decompression profiles. Several algoritms
 *               have been implemented.
 * Copyright:    GNU Public Licence
 * Author:       Jorgen van der Velde
 *               Original VPMDeco and VPM-B from Fortran code by Erik C. Baker
 * Disclaimer:   Do not use for real diving. Software may contain errors.
 *               For experimental and educational use only
 * Version:      1.0
 */

package net.deepocean.dodeco.calculator;

/**
 *   This class defines a number of static methods that implement
 *   generic algorithms used throughout the software.
 */

public class Tools
{

    public Tools()
    {
    }
/*
    public static Pressure alveolarPressure(Pressure ambientPressure, double fFractionInertGas)
    {
        double fAlveolarPressure;

        fAlveolarPressure=(ambientPressure.getValue(Pressure.UNITS_BAR)-
                            Parameters.fPressureH2O+
                            (1.0-Parameters.fRq)/Parameters.fRq*Parameters.fPressureCO2)*
                            fFractionInertGas;

        fAlveolarPressure=(ambientPressure.getValue(Pressure.UNITS_BAR)-1.607/33.0)*
                            fFractionInertGas;
        return new Pressure(fAlveolarPressure, Pressure.UNITS_BAR);

    }
*/
    /** 
     *  This method calculates the pressure of an inert gas fraction in the alveoli
     *  based on the ambient pressure and the fraction of the inert gas in the 
     *  breathing gas mixture
     *  @param ambientPressure The ambient pressure
     *  @param fFractionInertGas Fraction (0.0 - 1.0) of the inert gas in the breathing mixture
     *  @param iAlvPressureUnit Indicates the units in which the alveolar pressure should be
     *                           returned.
     *  @return The alveolar pressure value.
     */
    public static double alveolarPressure(Pressure ambientPressure, double fFractionInertGas,
                                            int iAlvPressureUnit)
    {
        double fAlveolarPressure;

        fAlveolarPressure=(ambientPressure.getValue(Pressure.UNITS_BAR)-
                            Parameters.pressureH2O.getValue(Pressure.UNITS_BAR)+
                            (1.0-Parameters.fRq)/Parameters.fRq*
                            Parameters.pressureCO2.getValue(Pressure.UNITS_BAR))*
                            fFractionInertGas;


        fAlveolarPressure=(ambientPressure.getValue(Pressure.UNITS_BAR)-
                            Pressure.convertPressure(1.607, Pressure.UNITS_FSW, Pressure.UNITS_BAR))*
                            fFractionInertGas;
        return Pressure.convertPressure(fAlveolarPressure, Pressure.UNITS_BAR, iAlvPressureUnit);

    }

    public static double schreinerEquation( double fAlvPressure,
                                            double fAlvRate,
                                            double fExposurePeriod,
                                            double fConstantK,
                                            double fTissueTension)
    {
        return fAlvPressure+fAlvRate*(fExposurePeriod-1.0/fConstantK)-
        (fAlvPressure-fTissueTension-fAlvRate/fConstantK)*Math.exp(-fExposurePeriod*fConstantK);
    }

    public static double haldaneEquation(   double fTissueTension,
                                            double fAlvPressure,
                                            double fConstantK,
                                            double fExposurePeriod)
    {
        return fAlvPressure+
               (fTissueTension-fAlvPressure)*Math.exp(-fExposurePeriod*fConstantK);
    }

/* =============================================================================== */
/*     SUBROUTINE RADIUS_ROOT_FINDER                                               */
/*     Purpose: This subroutine is a "fail-safe" routine that combines the         */
/*     Bisection Method and the Newton-Raphson Method to find the desired root.    */
/*     This hybrid algorithm takes a bisection step whenever Newton-Raphson would  */
/*     take the solution out of bounds, or whenever Newton-Raphson is not          */
/*     converging fast enough.  Source:  "Numerical Recipes in Fortran 77",        */
/*     Cambridge University Press, 1992.                                           */
/* =============================================================================== */
    /** Radius Root finder
     *  @param          a
     *  @param          b
     *  @param          c
     *  @param          low_bound
     *  @param          high_bound
     *  @return         The root
     *  @exception      CalculationException if the root is not found
     */
    public static double radiusRootFinder(double a,
                                          double b,
                                          double c,
                                          double low_bound,
                                          double high_bound) throws CalculationException
    {
        /* Local variables */

        int             i;
        double          function_at_low_bound   =0.0,
                        last_ending_radius      =0.0,
                        function_at_high_bound  =0.0,
                        derivative_of_function  =0.0,
                        differential_change     =0.0,
                        ending_radius           =0.0,
                        radius_at_low_bound     =0.0,
                        last_diff_change        =0.0,
                        function                =0.0,
                        radius_at_high_bound    =0.0;
        boolean         bExit;
/* loop */
/* ===============================================================================  */
/*     BEGIN CALCULATIONS BY MAKING SURE THAT THE ROOT LIES WITHIN BOUNDS           */
/*     In this case we are solving for radius in a cubic equation of the form,      */
/*     Ar^3 - Br^2 - C = 0.  The coefficients A, B, and C were passed to this       */
/*     subroutine as arguments.                                                     */
/* ===============================================================================  */

        bExit=false;
        function_at_low_bound  = low_bound  * (low_bound  * (a * low_bound - b)) - c;
        function_at_high_bound = high_bound * (high_bound * (a * high_bound - b)) - c;
        if (function_at_low_bound > 0.0 && function_at_high_bound > 0.0)
        {
            throw new CalculationException("ERROR! ROOT IS NOT WITHIN BRACKETS");
        }

/* ===============================================================================  */
/*     Next the algorithm checks for special conditions and then prepares for       */
/*     the first bisection.                                                         */
/* ===============================================================================  */

        if (function_at_low_bound < 0.0 && function_at_high_bound < 0.0)
        {
            throw new CalculationException("ERROR! ROOT IS NOT WITHIN BRACKETS");
        }

        if (function_at_low_bound == 0.0)
        {
            ending_radius = low_bound;
            bExit=true;
        }
        else if (function_at_high_bound == 0.0)
        {
            ending_radius = high_bound;
            bExit=true;
        }
        else if (function_at_low_bound < 0.0)
        {
            radius_at_low_bound  = low_bound;
            radius_at_high_bound = high_bound;
        }
        else
        {
            radius_at_high_bound = low_bound;
            radius_at_low_bound  = high_bound;
        }

        if (!bExit)
        {
            ending_radius       = (low_bound + high_bound) * 0.5;
            last_diff_change    = Math.abs(high_bound - low_bound);
            differential_change = last_diff_change;

/* ===============================================================================  */
/*     At this point, the Newton-Raphson Method is applied which uses a function    */
/*     and its first derivative to rapidly converge upon a solution.                */
/*     Note: the program allows for up to 100 iterations.  Normally an exit will    */
/*     be made from the loop well before that number.  If, for some reason, the     */
/*     program exceeds 100 iterations, there will be a pause to alert the user.     */
/*     When a solution with the desired accuracy is found, exit is made from the    */
/*     loop by returning to the calling program.  The last value of ending          */
/*     radius has been assigned as the solution.                                    */
/* ===============================================================================  */

            function = ending_radius * (ending_radius * (a * ending_radius - b)) - c;
            derivative_of_function = ending_radius * (ending_radius *  3.0 * a - b * 2.0);
        }


/*
        i=0;
        while (i<100 & !bExit)
        {
            if (((ending_radius - radius_at_high_bound) * derivative_of_function - function) *
                    ((ending_radius - radius_at_low_bound) * derivative_of_function - function) >= 0.
              || (Math.abs(2.0*function) > Math.abs(last_diff_change * derivative_of_function)))
            {
                last_diff_change = differential_change;
                differential_change =
                    (radius_at_high_bound - radius_at_low_bound) * .5;
                ending_radius = radius_at_low_bound + differential_change;
                if (radius_at_low_bound == ending_radius)
                {
                    bExit=true;
                }
            }
            else
            {
                last_diff_change    = differential_change;
                differential_change = function / derivative_of_function;
                last_ending_radius  = ending_radius;
                ending_radius       -= differential_change;
                if (last_ending_radius == ending_radius)
                {
                    bExit=true;
                }
            }
            if (Math.abs(differential_change) < 1e-12)
            {
                bExit=true;
            }

            function = ending_radius * (ending_radius * (a * ending_radius - b)) - c;
            derivative_of_function = ending_radius * (ending_radius * 30. * a - b * 2.0);
            if (function < 0.0)
            {
                radius_at_low_bound = ending_radius;
            }
            else
            {
                radius_at_high_bound = ending_radius;
            }
            i++;
        }
*/
    i=0;
    while (i<100 && !bExit)
    {
	    if (((ending_radius - radius_at_high_bound) * derivative_of_function - function) *
            ((ending_radius - radius_at_low_bound) * derivative_of_function - function) >= 0.0
          || Math.abs(function * 2.0) > Math.abs(last_diff_change * derivative_of_function))
        {
	        last_diff_change = differential_change;
	        differential_change =
                (radius_at_high_bound - radius_at_low_bound) * 0.5;
	        ending_radius = radius_at_low_bound + differential_change;
	        if (radius_at_low_bound == ending_radius)
            {
		        bExit=true;
	        }
	    }
        else
        {
	        last_diff_change = differential_change;
	        differential_change = function / derivative_of_function;
	        last_ending_radius = ending_radius;
	        ending_radius -= differential_change;
	        if (last_ending_radius == ending_radius)
            {
		        bExit=true;
	        }
	    }
	    if (Math.abs(differential_change) < 1e-12)
        {
	        bExit=true;
	    }
        if (!bExit)
        {
	        function =
            ending_radius * (ending_radius * (a * ending_radius - b)) - c;
            derivative_of_function =
                ending_radius * (ending_radius * 3.0 * a - b * 2.0);
            if (function < 0.0)
            {
                radius_at_low_bound = ending_radius;
            }
            else
            {
                radius_at_high_bound = ending_radius;
            }
        }
        i++;
    }


        if (!bExit)
        {
            throw new CalculationException("ERROR! ROOT SEARCH EXCEEDED MAXIMUM ITERATIONS");
        }

        return ending_radius;
    } /* radius_root_finder */

    
    
}