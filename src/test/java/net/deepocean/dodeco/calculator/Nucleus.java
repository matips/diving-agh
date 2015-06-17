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
 * This class represents the critical nucleus known from VPM.
 */
public class Nucleus
{
    Length initialCriticalRadius;
    Length criticalRadius;
    Length regeneratedCriticalRadius;
    Length adjustedCriticalRadius;

    /** Constructor. Initializes the nucleus radius
     *  @param          initialCriticalRadius The initial critical radius of
     *                  the nucleus.
     */
    public Nucleus(Length initialCriticalRadius)
    {
        initialize(initialCriticalRadius);
    }

    /** Constructor. Does not initalize anything. Used for clone()
     */
    private Nucleus()
    {

    }

    private void initialize(Length initialCriticalRadius)
    {
        this.initialCriticalRadius       =(Length)initialCriticalRadius.clone();
        this.adjustedCriticalRadius      =(Length)initialCriticalRadius.clone();
        this.criticalRadius              =(Length)initialCriticalRadius.clone();
        this.regeneratedCriticalRadius   =(Length)initialCriticalRadius.clone();
    }


    public Length getAdjustedCriticalRadius()
    {
        return adjustedCriticalRadius;
    }

    public Length getRegeneratedCriticalRadius()
    {
        return regeneratedCriticalRadius;
    }

    public Length getCriticalRadius()
    {
        return criticalRadius;
    }

    public Length getInitialCriticalRadius()
    {
        return initialCriticalRadius;
    }

    /** Sets the initial critical nucleus size
     *  @param          initialCriticalRadius
     */
    public void setInitialCriticalRadius(Length initialCriticalRadius)
    {
        initialize(initialCriticalRadius);
    }


    /** Sets the adusted critical nucleus size
     *  @param          fNewRadius
     */
    private void setAdjustedCriticalRadius(Length newRadius)
    {
        adjustedCriticalRadius=newRadius;
    }

    /** Sets the regenerated critical nucleus size
     *  @param          fNewRadius
     */
    private void setRegeneratedCriticalRadius(Length newRadius)
    {
        regeneratedCriticalRadius=newRadius;
    }

    /** Sets the critical nucleus size
     *  @param          fNewRadius
     */
    private void setCriticalRadius(Length newRadius)
    {
        criticalRadius=newRadius;
    }


    /** Regenerates the nucleus size
     *  @param          fRegenerationPeriod The period for regeneration
     *  @param          crushingPressure The crushing pressure
     *  @param          adjustedCrushingPressure The adjusted crushing pressure
     */
    public void regenerate( double fRegenerationPeriod,
                            Pressure crushingPressure,
                            Pressure adjustedCrushingPressure)
    {
    /* Local variables */
    double  crush_pressure_adjust_ratio,
            ending_radius,
            crushing_pressure_pascals,
            adj_crush_pressure_pascals,
            regenerated_radius,
            adjusted_critical_radius;

    int     i;
/* loop */
/* ===============================================================================  */
/*     CALCULATIONS                                                                 */
/*     First convert the maximum crushing pressure obtained for each compartment    */
/*     to Pascals.  Next, compute the ending radius for the critical nuclei.        */
/* ===============================================================================  */

        crushing_pressure_pascals   =crushingPressure.getValue(Pressure.UNITS_PASCAL);
        adjusted_critical_radius    =adjustedCriticalRadius.getValue(Length.UNITS_METER);

	    ending_radius =
            1.0 / (crushing_pressure_pascals /
                  ((Parameters.fGammaC - Parameters.fGamma) * 2.0) +
                    1.0 / adjusted_critical_radius);


/* ===============================================================================  */
/*     A "regenerated" radius for each nucleus is now calculated based on the       */
/*     regeneration time constant.  This means that after application of            */
/*     crushing pressure and reduction in radius, a nucleus will slowly grow        */
/*     back to its original initial radius over a period of time.  This             */
/*     phenomenon is probabilistic in nature and depends on absolute temperature.   */
/*     It is independent of crushing pressure.                                      */
/* ===============================================================================  */

	    regenerated_radius =
		    adjusted_critical_radius +
		    (ending_radius - adjusted_critical_radius) *
		    Math.exp(-fRegenerationPeriod / Parameters.fRegenTimeConstant);

/* ===============================================================================  */
/*     In order to preserve reference back to the initial critical radii after      */
/*     regeneration, an "adjusted crushing pressure" for the nuclei in each         */
/*     compartment must be computed.  In other words, this is the value of          */
/*     crushing pressure that would have reduced the original nucleus to the        */
/*     to the present radius had regeneration not taken place.  The ratio           */
/*     for adjusting crushing pressure is obtained from algebraic manipulation      */
/*     of the standard VPM equations.  The adjusted crushing pressure, in lieu      */
/*     of the original crushing pressure, is then applied in the VPM Critical       */
/*     Volume Algorithm and the VPM Repetitive Algorithm.                           */
/* ===============================================================================  */

	    crush_pressure_adjust_ratio =
            ending_radius * (adjusted_critical_radius - regenerated_radius) /
                            (regenerated_radius *
                             (adjusted_critical_radius - ending_radius));

	    adj_crush_pressure_pascals =
            crushing_pressure_pascals * crush_pressure_adjust_ratio;


        regeneratedCriticalRadius.setValue(regenerated_radius, Length.UNITS_METER);
        adjustedCrushingPressure.setValue(adj_crush_pressure_pascals, Pressure.UNITS_PASCAL);

    }
    
    
    /**
     *  When the diver acclimatises at hight, this method updates the critical
     *  radii.
     *  @param fExposurePeriod The stay period at height
     *  @param fCompartmentGradientInPascal The compartment gradient
     */
    public void updateNucleusAtHeight(double fCompartmentGradientInPascal, double fExposurePeriod)
    {
        double              gradient_bubble_formation;
        double              regenerated_critical_radius;
        double              ending_radius;
        double              new_critical_radius;
        
        double              fTensionN2;

        gradient_bubble_formation =
                    Parameters.fGamma * 2. *
                    (Parameters.fGammaC -
                    Parameters.fGamma) / 
                    (initialCriticalRadius.getValue(Length.UNITS_METER) *
                    Parameters.fGammaC);

        if (fCompartmentGradientInPascal > gradient_bubble_formation)
        {
            new_critical_radius =
                    Parameters.fGamma * 2.0 *
                    (Parameters.fGammaC -
                    Parameters.fGamma) / 
                    (fCompartmentGradientInPascal *
                    Parameters.fGammaC);
            adjustedCriticalRadius.setValue(
                    this.initialCriticalRadius.getValue(Length.UNITS_METER) +
                    (this.initialCriticalRadius.getValue(Length.UNITS_METER) -
                    new_critical_radius) *
                    Math.exp(-fExposurePeriod /
                    Parameters.fRegenTimeConstant),
                    Length.UNITS_METER);
            initialCriticalRadius.setValue(adjustedCriticalRadius);

        }
        else
        {
            ending_radius = 1.0 / 
                    (fCompartmentGradientInPascal /
                    ((Parameters.fGamma -
                    Parameters.fGammaC) * 2.0) +
                    1. / initialCriticalRadius.getValue(Length.UNITS_METER));
            this.regeneratedCriticalRadius.setValue(
                    initialCriticalRadius.getValue(Length.UNITS_METER) +
                    (ending_radius -
                    initialCriticalRadius.getValue(Length.UNITS_METER)) *
                    Math.exp(-fExposurePeriod /
                    Parameters.fRegenTimeConstant),
                    Length.UNITS_METER);
            this.initialCriticalRadius.setValue(this.regeneratedCriticalRadius);
            this.adjustedCriticalRadius.setValue(this.initialCriticalRadius);
        }
/*       
        gradient_n2_bubble_formation =
                    surface_tension_gamma * 2. *
                    (skin_compression_gammac -
                    surface_tension_gamma) / (
                    initial_critical_radius_n2[i - 1] *
                    skin_compression_gammac);
        if (compartment_gradient_pascals > gradient_n2_bubble_formation)
        {
            new_critical_radius_n2 =
                    surface_tension_gamma * 2. *
                    (skin_compression_gammac -
                    surface_tension_gamma) /
                    (compartment_gradient_pascals *
                    skin_compression_gammac);
            adjusted_critical_radius_n2[i - 1] =
                    initial_critical_radius_n2[i - 1] +
                    (initial_critical_radius_n2[i - 1] -
                    new_critical_radius_n2) *
                    exp(-time_at_altitude_before_dive /
                    regeneration_time_constant);
            initial_critical_radius_n2[i - 1] =
                    adjusted_critical_radius_n2[i - 1];
        }
        else
        {
            ending_radius_n2 = 1. / (
                    compartment_gradient_pascals /
                    ((surface_tension_gamma -
                    skin_compression_gammac) * 2.) +
                    1. / initial_critical_radius_n2[i - 1]);
            regenerated_radius_n2 =
                    initial_critical_radius_n2[i - 1] +
                    (ending_radius_n2 -
                    initial_critical_radius_n2[i - 1]) *
                    exp(-time_at_altitude_before_dive /
                    regeneration_time_constant);
            initial_critical_radius_n2[i - 1] =
                    regenerated_radius_n2;
            adjusted_critical_radius_n2[i - 1] =
                    initial_critical_radius_n2[i - 1];
        }
*/
        
    }
    

    public Object clone()
    {
        Nucleus newNucleus;

        newNucleus=new Nucleus();

        newNucleus.setInitialCriticalRadius     ((Length)initialCriticalRadius.clone());
        newNucleus.setAdjustedCriticalRadius    ((Length)adjustedCriticalRadius.clone());
        newNucleus.setCriticalRadius            ((Length)criticalRadius.clone());
        newNucleus.setRegeneratedCriticalRadius ((Length)regeneratedCriticalRadius.clone());

        return newNucleus;
    }

}