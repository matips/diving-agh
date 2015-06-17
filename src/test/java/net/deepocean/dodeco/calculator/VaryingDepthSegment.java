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

import net.deepocean.dodeco.tools.MyXML;
import net.deepocean.dodeco.tools.MyXML.MyXMLException;

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;

/**
 *  This class represents an ascent or descent stage in a dive.
 */

public class VaryingDepthSegment extends DepthSegment
{
    private Length depthChangeRate;
    private Pressure changeOfAmbientPressure;
    private AtmosphericPressure atmosphericPressure;
    private DepthPressure startDepthPressure;
    private DepthPressure endDepthPressure;

    private Pressure initialHe2Pressure;
    private Pressure initialN2Pressure;

    private double                  fAmbPressureOnsetOfImperm;          // in bar
    private double                  fGasTensionOnsetOfImperm;           // in bar

    /*------------------------------------------------------------------------------------------------*\
     * Construction and reinitialising
    \*------------------------------------------------------------------------------------------------*/    
    /** Constructor. Initialises the variables
     *  @param          diveHeight Height at which the dive takes place
     *  @param          startDepth Depth at the start of this VaryingDepthSegment
     *  @param          fExposurePeriod Duration in minutes of the ascent/descent
     *  @param          depthChangeRate Rate of change of depth
     *  @param          gasMixture  The GasMixture used during the ascent/descent
     */
    public VaryingDepthSegment( Length diveHeight,
                                Length startDepth,
                                double fExposurePeriod,
                                Length depthChangeRate,
                                GasMixture gasMixture)
    {
        double fEndDepth;

        this.diveDepthAtStart   =(Length)startDepth.clone();
        this.depthChangeRate    =(Length)depthChangeRate.clone();
        this.diveHeight         =(Length)diveHeight.clone();

        fEndDepth=startDepth.getValue(Length.UNITS_METER)+
                    fExposurePeriod*depthChangeRate.getValue(Length.UNITS_METER);
        this.diveDepthAtEnd.setValue(fEndDepth, Pressure.UNITS_BAR);

        initSegment();

        this.fExposurePeriod        =fExposurePeriod;
        this.gasMixture             =gasMixture;
    }

    /** Constructor. Initialises the variables
     *  @param diveHeight Height at which the dive takes place
     *  @param startDepth Start depth of this ExposureSegment
     *  @param endDepth Depth at which the ascent/descent ends
     *  @param depthChangeRate Rate of depth change in meter/min or feet/min
     *  @param gasMixture GasMixture used during this ascent/descent
     *  @exception IllegalActionException is thrown when an initialisation error occurs
     */
    public VaryingDepthSegment(  Length diveHeight,
                                    Length startDepth,
                                    Length endDepth,
                                    Length depthChangeRate,
                                    GasMixture gasMixture)
                                    throws IllegalActionException

    {
        if (fExposurePeriod<0)
        {
            throw new IllegalActionException("Wrong segment: sign of rate not ok");
        }

        this.diveDepthAtStart   =(Length)startDepth.clone();
        this.diveDepthAtEnd     =(Length)endDepth.clone();
        this.depthChangeRate    =(Length)depthChangeRate.clone();
        this.diveHeight         =(Length)diveHeight.clone();

        this.gasMixture         =gasMixture;

        initSegment();
    }

    /** Constructor. Initializes the variables that characterises
     *  this ExposureSegment based on an representation of the segment in XML
     *  @exception net.deepocean.dodeco.tools.MyXML.MyXMLException If an XML parsing error occured
     *  @exception IllegalActionException when an initialising error occurs
     */
    public VaryingDepthSegment(  Length diveHeight,
                                 MyXML xmlRepresentation)
                                    throws MyXMLException, IllegalActionException

    {
        this.diveHeight         =(Length)diveHeight.clone();
        this.createFromXmlRepresentation(xmlRepresentation);

        initSegment();
    }

    /**
     *  This method sets the parameters that characterises the
     *  Varying depth segment
     */
    public void setParameters(  Length startDepth,
                                Length endDepth,
                                Length depthChangeRate,
                                GasMixture gasMixture)
    {
        this.diveDepthAtStart.equalsLength(startDepth);
        this.diveDepthAtEnd.equalsLength(endDepth);
        this.depthChangeRate.equalsLength(depthChangeRate);

        this.gasMixture.equalsGasMixture(gasMixture);

        initSegment();
    }


    /** Initialises the derived segment parameters
     */
    protected void initSegment()
    {
        double fDepthAtStart;
        double fDepthAtEnd;
        double fDepthChangeRate;

        this.startDepthPressure =new DepthPressure(diveDepthAtStart, diveHeight);
        this.endDepthPressure   =new DepthPressure(diveDepthAtEnd, diveHeight);

        this.ambientPressureAtStart =(Pressure)startDepthPressure.clone();
        this.ambientPressureAtEnd =(Pressure)endDepthPressure.clone();

        this.changeOfAmbientPressure=
                DepthPressure.convertDepthChangeToPressureChange(depthChangeRate);

        // Calculate the exposure period
        fDepthAtStart   =diveDepthAtStart.getValue(Length.UNITS_METER);
        fDepthAtEnd     =diveDepthAtEnd.getValue(Length.UNITS_METER);
        fDepthChangeRate=depthChangeRate.getValue(Length.UNITS_METER);
        this.fExposurePeriod=(fDepthAtEnd-fDepthAtStart)/fDepthChangeRate;
    }

    /*------------------------------------------------------------------------------------------------*\
     * Get information
    \*------------------------------------------------------------------------------------------------*/
    /** Gets the value of the ambient pressure at the end of the exposure
     *  segment
     *  @return         The ambient pressure at the end of the segment
     */
    public Pressure getAmbientPressureAtEnd()
    {
        double fPressure;
        double fPressureChange;

        fPressure=ambientPressureAtStart.getValue(Pressure.UNITS_BAR);
        fPressureChange=changeOfAmbientPressure.getValue(Pressure.UNITS_BAR);

        fPressure+=fPressureChange*fExposurePeriod;

        if (fPressure<0.0)
        {
            fPressure=0.0f;
        }


        return new Pressure(fPressure, Pressure.UNITS_BAR);
    }

    /**
     *  This method returns the change rate of the depth (ascent or descent rate)
     *  in length unit per minute
     *  @return The depth change rate
     */
    public Length getDepthChangeRate()
    {
        return this.depthChangeRate;
    }


    /*------------------------------------------------------------------------------------------------*\
     * Calculation
    \*------------------------------------------------------------------------------------------------*/

    /** Applies the exposure of this segment to a tissue compartment
     *  @param          compartment The tissue compartment to be updated
     *  @exception CalculationException
     */
    public void exposeTissueCompartment(TissueCompartment compartment)
                throws CalculationException
    {
        double      fAmbientPressure;
        double      fAlvPressure;
        double      fTissuePressure;
        double      fConstantK;
        double      fFraction;
        double      fRate;

        Pressure tissuePressure;

        fAmbientPressure=ambientPressureAtStart.getValue(Pressure.UNITS_BAR);

        if (fExposurePeriod<0.0)
        {
            throw new CalculationException("Negative time value");
        }

        // NITROGEN
        initialN2Pressure=(Pressure)compartment.getN2TissueTension().clone();
        fConstantK=compartment.getN2K();
        fFraction=gasMixture.getN2Fraction();
        fRate=changeOfAmbientPressure.getValue(Pressure.UNITS_BAR)*fFraction;
        tissuePressure=compartment.getN2TissueTension();
        fTissuePressure=tissuePressure.getValue(Pressure.UNITS_BAR);
        fAlvPressure= Tools.alveolarPressure(ambientPressureAtStart, fFraction,
                Pressure.UNITS_BAR);

/*
        fAlvPressure=(fAmbientPressure-
                        Parameters.fPressureH2O+
                        (1.0-Parameters.fRq)/Parameters.fRq*Parameters.fPressureCO2)*
                        fFraction;
*/

        // the schreiner equation
        fTissuePressure= Tools.schreinerEquation(fAlvPressure, fRate, fExposurePeriod,
                fConstantK, fTissuePressure);
        tissuePressure.setValue(fTissuePressure, Pressure.UNITS_BAR);

        // HELIUM
        initialHe2Pressure=(Pressure)compartment.getHe2TissueTension().clone();
        fConstantK=compartment.getHe2K();
        fFraction=gasMixture.getHe2Fraction();
        fRate=changeOfAmbientPressure.getValue(Pressure.UNITS_BAR)*fFraction;
        tissuePressure=compartment.getHe2TissueTension();
        fTissuePressure=tissuePressure.getValue(Pressure.UNITS_BAR);
        fAlvPressure= Tools.alveolarPressure(ambientPressureAtStart, fFraction,
                Pressure.UNITS_BAR);
/*
        fAlvPressure=(fAmbientPressure-
                        Parameters.fPressureH2O+
                        (1.0-Parameters.fRq)/Parameters.fRq*Parameters.fPressureCO2)*
                        fFraction;
*/

        // the schreiner equation
        fTissuePressure= Tools.schreinerEquation(fAlvPressure, fRate, fExposurePeriod,
                fConstantK, fTissuePressure);
        tissuePressure.setValue(fTissuePressure, Pressure.UNITS_BAR);

        if (ambientPressureAtEnd.largerThan(ambientPressureAtStart))
        {
            calculateCrushingPressure(compartment);
        }
    }




/* ===============================================================================  */
/*     SUBROUTINE CALC_CRUSHING_PRESSURE                                            */
/*     Purpose: Compute the effective "crushing pressure" in each compartment as    */
/*     a result of descent segment(s).  The crushing pressure is the gradient       */
/*     (difference in pressure) between the outside ambient pressure and the        */
/*     gas tension inside a VPM nucleus (bubble seed).  This gradient acts to       */
/*     reduce (shrink) the radius smaller than its initial value at the surface.    */
/*     This phenomenon has important ramifications because the smaller the radius   */
/*     of a VPM nucleus, the greater the allowable supersaturation gradient upon    */
/*     ascent.  Gas loading (uptake) during descent, especially in the fast         */
/*     compartments, will reduce the magnitude of the crushing pressure.  The       */
/*     crushing pressure is not cumulative over a multi-level descent.  It will     */
/*     be the maximum value obtained in any one discrete segment of the overall     */
/*     descent.  Thus, the program must compute and store the maximum crushing      */
/*     pressure for each compartment that was obtained across all segments of       */
/*     the descent profile.                                                         */
/*     The calculation of crushing pressure will be different depending on          */
/*     whether or not the gradient is in the VPM permeable range (gas can diffuse   */
/*     across skin of VPM nucleus) or the VPM impermeable range (molecules in       */
/*     skin of nucleus are squeezed together so tight that gas can no longer        */
/*     diffuse in or out of nucleus; the gas becomes trapped and further resists    */
/*     the crushing pressure).  The solution for crushing pressure in the VPM       */
/*     permeable range is a simple linear equation.  In the VPM impermeable         */
/*     range, a cubic equation must be solved using a numerical method.             */
/*     Separate crushing pressures are tracked for helium and nitrogen because      */
/*     they can have different critical radii.  The crushing pressures will be      */
/*     the same for helium and nitrogen in the permeable range of the model, but    */
/*     they will start to diverge in the impermeable range.  This is due to         */
/*     the differences between starting radius, radius at the onset of              */
/*     impermeability, and radial compression in the impermeable range.             */
/* ===============================================================================  */
    /** Calculates the crushing pressure and updates the tissue compartment
     *  @param          compartment The tissue compartment to be updated
     *  @exception CalculationException
     */
    public void calculateCrushingPressure(TissueCompartment compartment)
                throws CalculationException
    {


        /* System generated locals */
        double r1, r2;

        double      low_bound_n2,
                    ending_radius_n2,
                    ending_ambient_pressure,
                    gradient_onset_of_imperm_pa,
                    low_bound_he,
                    ending_radius_he,
                    high_bound_n2,
                    crushing_pressure_n2=0.0,
                    crushing_pressure_pascals_n2,
                    gradient_onset_of_imperm,
                    starting_gas_tension,
                    high_bound_he,
                    crushing_pressure_he=0.0,
                    amb_press_onset_of_imperm_pa,
                    crushing_pressure_pascals_he,
                    radius_onset_of_imperm_n2,
                    starting_gradient,
                    radius_onset_of_imperm_he,
                    starting_ambient_pressure,
                    ending_gas_tension,
                    ending_ambient_pressure_pa,
                    a_n2,
                    b_n2,
                    c_n2,
                    ending_gradient,
                    gas_tension_onset_of_imperm_pa,
                    a_he,
                    b_he, c_he,

                    starting_depth,
                    ending_depth,
                    rate,
                    fTensionHe2,
                    fTensionN2,
                    adjusted_critical_radius_he,
                    adjusted_critical_radius_n2;


        Pressure currentMaxCrushingPressure;
        double      fCurrentMaxCrushingPressure;
        int i;
/* loop */
/* ===============================================================================  */
/*     CALCULATIONS                                                                 */
/*     First, convert the Gradient for Onset of Impermeability from units of        */
/*     atmospheres to diving pressure units (either fsw or msw) and to Pascals      */
/*     (SI units).  The reason that the Gradient for Onset of Impermeability is     */
/*     given in the program settings in units of atmospheres is because that is     */
/*     how it was reported in the original research papers by Yount and             */
/*     colleauges.                                                                  */
/* ===============================================================================  */

        starting_depth              = diveDepthAtStart.getValue(Length.UNITS_METER);
        gradient_onset_of_imperm    = Parameters.gradientOnsetOfImpermeability.
                                                getValue(Pressure.UNITS_BAR);   // bar
        gradient_onset_of_imperm_pa = Parameters.gradientOnsetOfImpermeability.
                                                 getValue(Pressure.UNITS_PASCAL);    // pascal

/* ===============================================================================  */
/*     Assign values of starting and ending ambient pressures for descent segment   */
/* ===============================================================================  */

        starting_ambient_pressure   = ambientPressureAtStart.getValue(Pressure.UNITS_BAR);
        ending_ambient_pressure     = ambientPressureAtEnd.getValue(Pressure.UNITS_BAR);
        rate                        = changeOfAmbientPressure.getValue(Pressure.UNITS_BAR);
        fTensionN2                  =compartment.getN2TissueTension().getValue(Pressure.UNITS_BAR);
        fTensionHe2                 =compartment.getHe2TissueTension().getValue(Pressure.UNITS_BAR);

        adjusted_critical_radius_he =compartment.getHe2AdjustedCriticalRadius().getValue(Length.UNITS_METER);
        adjusted_critical_radius_n2 =compartment.getN2AdjustedCriticalRadius().getValue(Length.UNITS_METER);


/* ===============================================================================  */
/*     MAIN LOOP WITH NESTED DECISION TREE                                          */
/*     For each compartment, the program computes the starting and ending           */
/*     gas tensions and gradients.  The VPM is different than some dissolved gas    */
/*     algorithms, Buhlmann for example, in that it considers the pressure due to   */
/*     oxygen, carbon dioxide, and water vapor in each compartment in addition to   */
/*     the inert gases helium and nitrogen.  These "other gases" are included in    */
/*     the calculation of gas tensions and gradients.                               */
/* ===============================================================================  */


        starting_gas_tension =  initialHe2Pressure.getValue(Pressure.UNITS_BAR)+
                                initialN2Pressure.getValue(Pressure.UNITS_BAR) +
                                Parameters.pressureOtherGasses.getValue(Pressure.UNITS_BAR);
        starting_gradient    =  ambientPressureAtStart.getValue(Pressure.UNITS_BAR) -
                                starting_gas_tension;
        ending_gas_tension   =  fTensionHe2 + fTensionN2 + Parameters.pressureOtherGasses.getValue(Pressure.UNITS_BAR);
        ending_gradient      =  ending_ambient_pressure - ending_gas_tension;

/* ===============================================================================  */
/*     Compute radius at onset of impermeability for helium and nitrogen            */
/*     critical radii                                                               */
/* ===============================================================================  */

        radius_onset_of_imperm_he = 1.0 / ( gradient_onset_of_imperm_pa /
                                    ((Parameters.fGammaC -
                                    Parameters.fGamma) * 2.0) +
                                    1.0 / adjusted_critical_radius_he);
        radius_onset_of_imperm_n2 = 1.0 / ( gradient_onset_of_imperm_pa /
                                    ((Parameters.fGammaC -
                                    Parameters.fGamma) * 2.0) +
                                    1.0 / adjusted_critical_radius_n2);

/* ===============================================================================  */
/*     FIRST BRANCH OF DECISION TREE - PERMEABLE RANGE                              */
/*     Crushing pressures will be the same for helium and nitrogen                  */
/* ===============================================================================  */

        if (ending_gradient <= gradient_onset_of_imperm)
        {
            crushing_pressure_he = ending_ambient_pressure - ending_gas_tension;
            crushing_pressure_n2 = ending_ambient_pressure - ending_gas_tension;
        }

/* ===============================================================================  */
/*     SECOND BRANCH OF DECISION TREE - IMPERMEABLE RANGE                           */
/*     Both the ambient pressure and the gas tension at the onset of                */
/*     impermeability must be computed in order to properly solve for the ending    */
/*     radius and resultant crushing pressure.  The first decision block            */
/*     addresses the special case when the starting gradient just happens to be     */
/*     equal to the gradient for onset of impermeability (not very likely!).        */
/* ===============================================================================  */

        if (ending_gradient > gradient_onset_of_imperm)
        {
            if (starting_gradient == gradient_onset_of_imperm)
            {
                fAmbPressureOnsetOfImperm =
                    starting_ambient_pressure;
                fGasTensionOnsetOfImperm =
                    starting_gas_tension;
            }

/* ===============================================================================  */
/*     In most cases, a subroutine will be called to find these values using a      */
/*     numerical method.                                                            */
/* ===============================================================================  */

            if (starting_gradient < gradient_onset_of_imperm)
            {
                calculateOnsetOfImpermeability(compartment);
            }

/* ===============================================================================  */
/*     Next, using the values for ambient pressure and gas tension at the onset     */
/*     of impermeability, the equations are set up to process the calculations      */
/*     through the radius root finder subroutine.  This subprogram will find the    */
/*     root (solution) to the cubic equation using a numerical method.  In order    */
/*     to do this efficiently, the equations are placed in the form                 */
/*     Ar^3 - Br^2 - C = 0, where r is the ending radius after impermeable          */
/*     compression.  The coefficients A, B, and C for helium and nitrogen are       */
/*     computed and passed to the subroutine as arguments.  The high and low        */
/*     bounds to be used by the numerical method of the subroutine are also         */
/*     computed (see separate page posted on Deco List ftp site entitled            */
/*     "VPM: Solving for radius in the impermeable regime").  The subprogram        */
/*     will return the value of the ending radius and then the crushing             */
/*     pressures for helium and nitrogen can be calculated.                         */
/* ===============================================================================  */

            ending_ambient_pressure_pa     = ending_ambient_pressure *1e5;
            amb_press_onset_of_imperm_pa   = fAmbPressureOnsetOfImperm*1e5;
            gas_tension_onset_of_imperm_pa = fGasTensionOnsetOfImperm * 1e5;
            b_he = (Parameters.fGammaC - Parameters.fGamma) * 2.0;
            a_he = ending_ambient_pressure_pa -
                    amb_press_onset_of_imperm_pa +
                    gas_tension_onset_of_imperm_pa +
                    (Parameters.fGammaC -
                     Parameters.fGamma) *
                    2.0 / radius_onset_of_imperm_he;
            /* Computing 3rd power */
            r1   = radius_onset_of_imperm_he;
            c_he = gas_tension_onset_of_imperm_pa * (r1 * (r1 * r1));
            high_bound_he = radius_onset_of_imperm_he;
            low_bound_he = b_he / a_he;
            ending_radius_he=radiusRootFinder(  a_he,
                                                b_he,
                                                c_he,
                                                low_bound_he,
                                                high_bound_he);
            /* Computing 3rd power */
            r1 = radius_onset_of_imperm_he;
            /* Computing 3rd power */
            r2 = ending_radius_he;
            crushing_pressure_pascals_he =
                    gradient_onset_of_imperm_pa +
                    ending_ambient_pressure_pa -
                    amb_press_onset_of_imperm_pa +
                    gas_tension_onset_of_imperm_pa *
                    (1.0 - r1 * (r1 * r1) / (r2 * (r2 * r2)));
            crushing_pressure_he = crushing_pressure_pascals_he/1e5;
            b_n2 = (Parameters.fGammaC - Parameters.fGamma) * 2.0;
            a_n2 = ending_ambient_pressure_pa -
                    amb_press_onset_of_imperm_pa +
                    gas_tension_onset_of_imperm_pa +
                    (Parameters.fGammaC - Parameters.fGamma) *
                        2.0 / radius_onset_of_imperm_n2;
            /* Computing 3rd power */
            r1              = radius_onset_of_imperm_n2;
            c_n2            = gas_tension_onset_of_imperm_pa * (r1 * (r1 * r1));
            high_bound_n2   = radius_onset_of_imperm_n2;
            low_bound_n2    = b_n2 / a_n2;
            ending_radius_n2=radiusRootFinder(a_n2,
                                              b_n2,
                                              c_n2,
                                              low_bound_n2,
                                              high_bound_n2);

            /* Computing 3rd power */
            r1 = radius_onset_of_imperm_n2;
            /* Computing 3rd power */
            r2 = ending_radius_n2;
            crushing_pressure_pascals_n2 =
                gradient_onset_of_imperm_pa +
                ending_ambient_pressure_pa -
                amb_press_onset_of_imperm_pa +
                gas_tension_onset_of_imperm_pa * (1.0 - r1 *
                (r1 * r1) / (r2 * (r2 * r2)));
            crushing_pressure_n2 = crushing_pressure_pascals_n2 / 1e5;
        }

/* ===============================================================================  */
/*     UPDATE VALUES OF MAX CRUSHING PRESSURE IN GLOBAL ARRAYS                      */
/* ===============================================================================  */

        /* Computing MAX */
        currentMaxCrushingPressure=compartment.getHe2MaxCrushingPressure();
        fCurrentMaxCrushingPressure=currentMaxCrushingPressure.getValue(Pressure.UNITS_BAR);

        if (crushing_pressure_he>fCurrentMaxCrushingPressure)
        {
            currentMaxCrushingPressure.setValue(crushing_pressure_he, Pressure.UNITS_BAR);
        }
        /* Computing MAX */
        currentMaxCrushingPressure=compartment.getN2MaxCrushingPressure();
        fCurrentMaxCrushingPressure=currentMaxCrushingPressure.getValue(Pressure.UNITS_BAR);

        if (crushing_pressure_n2>fCurrentMaxCrushingPressure)
        {
            currentMaxCrushingPressure.setValue(crushing_pressure_n2, Pressure.UNITS_BAR);
        }

    } /* calc_crushing_pressure */






/* ===============================================================================  */
/*     SUBROUTINE ONSET_OF_IMPERMEABILITY                                           */
/*     Purpose:  This subroutine uses the Bisection Method to find the ambient      */
/*     pressure and gas tension at the onset of impermeability for a given          */
/*     compartment.  Source:  "Numerical Recipes in Fortran 77",                    */
/*     Cambridge University Press, 1992.                                            */
/* ===============================================================================  */
    /** This routine calculates the ambient pressure and gas tension at the
     *  onset of impermeability for a TissueCompartment
     *  @param          compartment TissueCompartment
     *  @exception CalculationException if the root is not found
     */
    public void calculateOnsetOfImpermeability(TissueCompartment compartment)
                throws CalculationException
    {
        /* Local variables */
        double  time,
                last_diff_change,
                mid_range_nitrogen_pressure,
                gas_tension_at_mid_range=0.0,
                initial_inspired_n2_pressure,
                gradient_onset_of_imperm,
                starting_gas_tension,
                low_bound,
                initial_inspired_he_pressure,
                high_bound_nitrogen_pressure,
                nitrogen_rate,
                function_at_mid_range,
                function_at_low_bound,
                high_bound,
                mid_range_helium_pressure,
                mid_range_time,
                ending_gas_tension,
                function_at_high_bound,
                mid_range_ambient_pressure=0.0,
                high_bound_helium_pressure,
                helium_rate,
                differential_change,
                helium_time_constant,
                nitrogen_time_constant,
                initial_helium_pressure,
                initial_nitrogen_pressure,
                starting_ambient_pressure,
                ending_ambient_pressure,
                rate;

        double  fPressureOtherGasses;

        int     i;

        boolean bExit;


        fPressureOtherGasses= Parameters.pressureOtherGasses.getValue(Pressure.UNITS_BAR);

    /* loop */
/* ===============================================================================  */
/*     CALCULATIONS                                                                 */
/*     First convert the Gradient for Onset of Impermeability to the diving         */
/*     pressure units that are being used                                           */
/* ===============================================================================  */

        rate=changeOfAmbientPressure.getValue(Pressure.UNITS_BAR);
        starting_ambient_pressure=ambientPressureAtStart.getValue(Pressure.UNITS_BAR);
        ending_ambient_pressure=starting_ambient_pressure+rate*fExposurePeriod;

        gradient_onset_of_imperm = Parameters.gradientOnsetOfImpermeability.
                                   getValue(Pressure.UNITS_BAR);
        helium_time_constant=compartment.getHe2K();
        nitrogen_time_constant=compartment.getN2K();
        initial_helium_pressure=initialHe2Pressure.getValue(Pressure.UNITS_BAR);
        initial_nitrogen_pressure=initialN2Pressure.getValue(Pressure.UNITS_BAR);

/* ===============================================================================  */
/*     ESTABLISH THE BOUNDS FOR THE ROOT SEARCH USING THE BISECTION METHOD          */
/*     In this case, we are solving for time - the time when the ambient pressure   */
/*     minus the gas tension will be equal to the Gradient for Onset of             */
/*     Impermeabliity.  The low bound for time is set at zero and the high          */
/*     bound is set at the elapsed time (segment time) it took to go from the       */
/*     starting ambient pressure to the ending ambient pressure.  The desired       */
/*     ambient pressure and gas tension at the onset of impermeability will         */
/*     be found somewhere between these endpoints.  The algorithm checks to         */
/*     make sure that the solution lies in between these bounds by first            */
/*     computing the low bound and high bound function values.                      */
/* ===============================================================================  */

        initial_inspired_he_pressure =
            Tools.alveolarPressure(ambientPressureAtStart,
                    gasMixture.getHe2Fraction(),
                    Pressure.UNITS_BAR);
        initial_inspired_n2_pressure =
            Tools.alveolarPressure(ambientPressureAtStart,
                    gasMixture.getN2Fraction(),
                    Pressure.UNITS_BAR);
        helium_rate   = rate*gasMixture.getHe2Fraction();
        nitrogen_rate = rate*gasMixture.getN2Fraction();
        low_bound = 0.0;
        high_bound = (ending_ambient_pressure-starting_ambient_pressure)/rate;

        starting_gas_tension =
            initial_helium_pressure +
            initial_nitrogen_pressure +
            fPressureOtherGasses;
        function_at_low_bound =
            starting_ambient_pressure -
            starting_gas_tension -
            gradient_onset_of_imperm;
        high_bound_helium_pressure =
            Tools.schreinerEquation(initial_inspired_he_pressure,
                    helium_rate,
                    high_bound,
                    helium_time_constant,
                    initial_helium_pressure);
        high_bound_nitrogen_pressure =
            Tools.schreinerEquation(initial_inspired_n2_pressure,
                    nitrogen_rate,
                    high_bound,
                    nitrogen_time_constant,
                    initial_nitrogen_pressure);
        ending_gas_tension =
            high_bound_helium_pressure +
            high_bound_nitrogen_pressure +
            fPressureOtherGasses;
        function_at_high_bound =
            ending_ambient_pressure -
            ending_gas_tension -
            gradient_onset_of_imperm;
        if (function_at_high_bound * function_at_low_bound >= 0.0)
        {
            throw new CalculationException("ERROR! ROOT IS NOT WITHIN BRACKETS");
        }

/* ===============================================================================  */
/*     APPLY THE BISECTION METHOD IN SEVERAL ITERATIONS UNTIL A SOLUTION WITH       */
/*     THE DESIRED ACCURACY IS FOUND                                                */
/*     Note: the program allows for up to 100 iterations.  Normally an exit will    */
/*     be made from the loop well before that number.  If, for some reason, the     */
/*     program exceeds 100 iterations, there will be a pause to alert the user.     */
/* ===============================================================================  */

        if (function_at_low_bound < 0.0)
        {
            time = low_bound;
            differential_change = high_bound - low_bound;
        }
        else
        {
            time = high_bound;
            differential_change = low_bound - high_bound;
        }

        i=0;
        bExit=false;
        while ((i<100) && !bExit)
        {
            last_diff_change            = differential_change;
            differential_change         = last_diff_change * .5;
            mid_range_time              = time + differential_change;
            mid_range_ambient_pressure  = starting_ambient_pressure + rate * mid_range_time;
            mid_range_helium_pressure   =
                Tools.schreinerEquation(initial_inspired_he_pressure,
                        helium_rate,
                        mid_range_time,
                        helium_time_constant,
                        initial_helium_pressure);
            mid_range_nitrogen_pressure =
                Tools.schreinerEquation(initial_inspired_n2_pressure,
                        nitrogen_rate,
                        mid_range_time,
                        nitrogen_time_constant,
                        initial_nitrogen_pressure);
            gas_tension_at_mid_range =
                mid_range_helium_pressure +
                mid_range_nitrogen_pressure +
                fPressureOtherGasses;
            function_at_mid_range =
                mid_range_ambient_pressure -
                gas_tension_at_mid_range -
                gradient_onset_of_imperm;
            if (function_at_mid_range <= 0.0)
            {
                time = mid_range_time;
            }
            if ((Math.abs(differential_change) < .001) ||
                (function_at_mid_range == 0.0))
            {
                bExit=true;
            }
            i++;
        }
        if (!bExit)
        {
            throw new CalculationException("ERROR! ROOT SEARCH EXCEEDED MAXIMUM ITERATIONS");
        }


/* ===============================================================================  */
/*     When a solution with the desired accuracy is found, the program jumps out    */
/*     of the loop to Line 100 and assigns the solution values for ambient          */
/*     pressure and gas tension at the onset of impermeability.                     */
/* ===============================================================================  */


        fAmbPressureOnsetOfImperm = mid_range_ambient_pressure;
        fGasTensionOnsetOfImperm  = gas_tension_at_mid_range;

    } /* onset_of_impermeability */





/* =============================================================================== */
/*     SUBROUTINE RADIUS_ROOT_FINDER                                               */
/*     Purpose: This subroutine is a "fail-safe" routine that combines the         */
/*     Bisection Method and the Newton-Raphson Method to find the desired root.    */
/*     This hybrid algorithm takes a bisection step whenever Newton-Raphson would  */
/*     take the solution out of bounds, or whenever Newton-Raphson is not          */
/*     converging fast enough.  Source:  "Numerical Recipes in Fortran 77",        */
/*     Cambridge University Press, 1992.                                           */
/* =============================================================================== */
    /** Root finder
     *  @param          -
     *  @return         The root
     *  @exception CalculationException if the root is not found
     */
    private double radiusRootFinder(double a,
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




    /*------------------------------------------------------------------------------------------------*\
     * Printing the dive table
    \*------------------------------------------------------------------------------------------------*/
    /** Print the segment parameters as an entry in the dive table
     *  @param          writer Output stream writer used to write
     *  @exception      java.io.IOException
     */
    public void printDiveTableEntry(Writer writer) throws IOException
    {
        int             iUnits= Parameters.iPresentationPressureUnits;

        if (Parameters.iPresentationPressureUnits== Pressure.UNITS_FSW)
        {
            iUnits= Length.UNITS_FEET;
        }
        else
        {
            iUnits= Length.UNITS_METER;
        }
        String          sForm01;
        String          sAscent;


        if (depthChangeRate.isNegative())
        {
            sAscent=Text.sReport11;
        }
        else
        {
            sAscent=Text.sReport12;
        }


        Object[]        args=   {
                                    new Integer(iSegmentNumber),
                                    new Double(fExposurePeriod),
                                    new Double(fRunTimeAtStart+fExposurePeriod),
                                    new Integer(gasMixture.getIndex()),
                                    sAscent,
                                    new Double(diveDepthAtStart.getValue(iUnits)),
                                    new Double(diveDepthAtEnd.getValue(iUnits)),
                                    new Double(depthChangeRate.getValue(iUnits))
                                };
        writer.write(MessageFormat.format(Text.sReport10, args));

    }

    /** Print the segment parameters as an entry in the deco table
     *  @param          writer Output stream writer used to write
     *  @exception      java.io.IOException
     */
    public void printDecoTableEntry(Writer writer) throws IOException
    {
        int             iUnits= Parameters.iPresentationPressureUnits;

        if (Parameters.iPresentationPressureUnits== Pressure.UNITS_FSW)
        {
            iUnits= Length.UNITS_FEET;
        }
        else
        {
            iUnits= Length.UNITS_METER;
        }
        String          sForm01;
        String          sAscent;


        if (depthChangeRate.isNegative())
        {
            sAscent=Text.sReport11;
        }
        else
        {
            sAscent=Text.sReport12;
        }


        Object[]        args=   {
                                    new Integer(iSegmentNumber),
                                    new Double(fExposurePeriod),
                                    new Double(fRunTimeAtStart+fExposurePeriod),
                                    new Integer(gasMixture.getIndex()),
                                    new Double(diveDepthAtEnd.getValue(iUnits)),
                                    new Double(depthChangeRate.getValue(iUnits))
                                };
        writer.write(MessageFormat.format(Text.sReport21, args));
    }

    /*------------------------------------------------------------------------------------------------*\
     * XML parsing and writing
    \*------------------------------------------------------------------------------------------------*/
    /**
     *  This method creates an XML representation of the Segment
     *  @return The MyXML instance representing the Segment
     */
    public MyXML getXmlRepresentation() throws MyXMLException
    {
        MyXML xmlSegment;
        MyXML xmlDepth;
        MyXML xmlLength;
        
        xmlSegment=new MyXML("DepthChange");

        xmlDepth=xmlSegment.addElement("StartDepth");
        xmlDepth.addElement(diveDepthAtStart.getXmlRepresentation());

        xmlDepth=xmlSegment.addElement("EndDepth");
        xmlDepth.addElement(diveDepthAtEnd.getXmlRepresentation());
 
        xmlLength=xmlSegment.addElement("DepthChangeRate");
        xmlLength.addElement(depthChangeRate.getXmlRepresentation());        
        
        xmlSegment.addElement(gasMixture.getXmlRepresentation());
        
        return xmlSegment;
    }    
    
    /**
     *  This method initialises the parameters characterising the Segment
     *  The values are retrieved from the XML representation.
     *  @xmlRepresentation Representation of the SSegment
     */
    private void createFromXmlRepresentation(MyXML xmlRepresentation) throws MyXMLException, IllegalActionException
    {
        MyXML xmlDepth;
        MyXML xmlRate;
        MyXML xmlGasMix;
        MyXML xmlLength;

        
        xmlDepth=xmlRepresentation.findElement("StartDepth");
        xmlLength=xmlDepth.findElement("Length");
        diveDepthAtStart=new Length(xmlLength);

        
        xmlDepth=xmlRepresentation.findElement("EndDepth");
        xmlLength=xmlDepth.findElement("Length");
        diveDepthAtEnd=new Length(xmlLength);
        
        xmlRate=xmlRepresentation.findElement("DepthChangeRate");
        xmlLength=xmlRate.findElement("Length");
        depthChangeRate=new Length(xmlLength);
                
        xmlGasMix=xmlRepresentation.findElement("GasMixture");
        gasMixture=new GasMixture(xmlGasMix);
        
    }       
    

}