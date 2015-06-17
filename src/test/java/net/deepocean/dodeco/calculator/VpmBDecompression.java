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

import java.util.Enumeration;
import java.util.Vector;

/**
 * This class represents the VPM-B decompression algorithm.
 * It is the VPMDECO algorithm with Boyle compensation added. 
 * This alogorith originally was developed in Fortran 
 * by Erik C. Baker. The comments between === lines  are copied comments
 * from the original authors.
 */
public class VpmBDecompression extends Decompression
{
    private static final int    BACKUP_STARTASCENT=0;
    private static final int    BACKUP_STARTDECOZONE=1;

    
    Length          ascentCeilingDepth;
    Length          decoCeilingDepth;
    Length          decoStopDepth;
    DepthPressure   decoStopPressure;
    Length          firstDecoStopDepth;
    DepthPressure   firstDecoStopPressure;
    Length          diveHeight;
    Length          startOfAscentDepth;
    double          fRunTimeStartOfDecoZone;
    double          fDecoPhaseVolumeTime;


    /*------------------------------------------------------------------------------------------------*\
     * Construction and reinitialising
    \*------------------------------------------------------------------------------------------------*/    
    /**
     * Constructor. Initialises the decompression
     */
    public VpmBDecompression()
    {
        startOfDecoZoneDepth=new Length(0.0, Length.UNITS_METER);
    }

    /*------------------------------------------------------------------------------------------------*\
     * Get information
    \*------------------------------------------------------------------------------------------------*/    
    
    /**
     *  This method returns the name or a short description of the algorithm.
     *  @return String identifying the algorithm.
     */
    public String getAlgorithmDescription()
    {
        return "VPM-B";
    }
    
    
    /*------------------------------------------------------------------------------------------------*\
     * Calculation
    \*------------------------------------------------------------------------------------------------*/    
    
    /** Decompresses the specified diver: i.e. calculate a decompression
     *  profile for the given diver so that he returns safely to the surface.
     *  The diver is updated during the decompression
     *  @param          diver The diver. Defines the tissue compartments and
     *                  critical nuclei. The diver is updated up to the moment
     *                  of ascent and decompression
     *  @param          diveSegments The diving segments prior to decompression
     *  @param          fRunTime Run time in minutes at start of decompression
     */
    public void decompressDiver(Diver diver,
                                        Vector<DepthSegment> diveSegments,
                                        double fRunTime)
                                        throws CalculationException
    {
        this.diver                  =diver;
        fCurrentRunTime             =fRunTime;
        iCurrentSegmentNumber       =iSegmentNumberAtStartAscent;

        verifyDecoStages();                                                     // check if the decostages make sense

//        lastDiveSegment             =(DepthSegment)diveSegments.getLastChainItem();
        lastDiveSegment             =(DepthSegment)diveSegments.lastElement();
        
        diveHeight                  =lastDiveSegment.getDiveHeight();
        startOfAscentDepth          =lastDiveSegment.getDepthAtEnd();

        iSegmentNumberAtStartAscent =lastDiveSegment.getSegmentNumber()+1;
        fRunTimeAtStartAscent       =fRunTime;
        this.diver.backupTissueTensions(BACKUP_STARTASCENT);

        currentDepth                =startOfAscentDepth;

/* ===============================================================================  */
/*     CALCULATE INITIAL ALLOWABLE GRADIENTS FOR ASCENT                             */
/*     This is based on the maximum effective crushing pressure on critical radii   */
/*     in each compartment achieved during the dive profile.                        */
/* ===============================================================================  */

        calculateInitialAllowableGradient(this.diver);        // calculate initial Pss

/* ===============================================================================  */
/*     INPUT PARAMETERS TO BE USED FOR STAGED DECOMPRESSION AND SAVE IN ARRAYS.     */
/*     ASSIGN INITAL PARAMETERS TO BE USED AT START OF ASCENT                       */
/*     The user has the ability to change mix, ascent rate, and step size in any    */
/*     combination at any depth during the ascent.                                  */
/* ===============================================================================  */

        currentDecoStage=getCurrentDecoStage(currentDepth);        // get current decostage

/* ===============================================================================  */
/*     CALCULATE THE DEPTH WHERE THE DECOMPRESSION ZONE BEGINS FOR THIS PROFILE     */
/*     BASED ON THE INITIAL ASCENT PARAMETERS AND WRITE THE DEEPEST POSSIBLE        */
/*     DECOMPRESSION STOP DEPTH TO THE OUTPUT FILE                                  */
/*     Knowing where the decompression zone starts is very important.  Below        */
/*     that depth there is no possibility for bubble formation because there        */
/*     will be no supersaturation gradients.  Deco stops should never start         */
/*     below the deco zone.  The deepest possible stop deco stop depth is           */
/*     defined as the next "standard" stop depth above the point where the          */
/*     leading compartment enters the deco zone.  Thus, the program will not        */
/*     base this calculation on step sizes larger than 10 fsw or 3 msw.  The        */
/*     deepest possible stop depth is not used in the program, per se, rather       */
/*     it is information to tell the diver where to start putting on the brakes     */
/*     during ascent.  This should be prominently displayed by any deco program.    */
/* ===============================================================================  */

        calcStartOfDecoZone(this.diver);
        calculateDeepestPossibleDecoStop();

/* ===============================================================================  */
/*     TEMPORARILY ASCEND PROFILE TO THE START OF THE DECOMPRESSION ZONE, SAVE      */
/*     VARIABLES AT THIS POINT, AND INITIALIZE VARIABLES FOR CRITICAL VOLUME LOOP   */
/*     The iterative process of the VPM Critical Volume Algorithm will operate      */
/*     only in the decompression zone since it deals with excess gas volume         */
/*     released as a result of supersaturation gradients (not possible below the    */
/*     decompression zone).                                                         */
/* ===============================================================================  */



        ascendDiverToStartOfDecoZone(this.diver);

        criticalVolumeLoop(this.diver);
    }



/* ===============================================================================  */
/*     SUBROUTINE CALC_INITIAL_ALLOWABLE_GRADIENT                                   */
/*     Purpose: This subprogram calculates the initial allowable gradients for      */
/*     helium and nitrogren in each compartment.  These are the gradients that      */
/*     will be used to set the deco ceiling on the first pass through the deco      */
/*     loop.  If the Critical Volume Algorithm is set to "off", then these          */
/*     gradients will determine the final deco schedule.  Otherwise, if the         */
/*     Critical Volume Algorithm is set to "on", these gradients will be further    */
/*     "relaxed" by the Critical Volume Algorithm subroutine.  The initial          */
/*     allowable gradients are referred to as "PssMin" in the papers by Yount       */
/*     and colleauges, i.e., the minimum supersaturation pressure gradients         */
/*     that will probe bubble formation in the VPM nuclei that started with the     */
/*     designated minimum initial radius (critical radius).                         */
/*     The initial allowable gradients are computed directly from the               */
/*     "regenerated" radii after the Nuclear Regeneration subroutine.  These        */
/*     gradients are tracked separately for helium and nitrogen.                    */
/* ===============================================================================  */
    /** Calculates the initial allowable supersaturation gradient Pss for
     *  each tissue compartment of the diver.
     *  @param          diver The diver. Defines the tissue compartments and
     *                  critical nuclei
     */
    public void calculateInitialAllowableGradient(Diver diver)
    {
        TissueCompartment   compartment;
        Vector              compartments;
        Enumeration         elements;
        double              initial_allowable_grad_n2_pa,
	                        initial_allowable_grad_he_pa,
                            regenerated_radius_n2,
                            regenerated_radius_he;

        Nucleus nucleus;

/* ===============================================================================  */
/*     CALCULATIONS                                                                 */
/*     The initial allowable gradients are computed in Pascals and then converted   */
/*     to the diving pressure units.  Two different sets of arrays are used to      */
/*     save the calculations - Initial Allowable Gradients and Allowable            */
/*     Gradients.  The Allowable Gradients are assigned the values from Initial     */
/*     Allowable Gradients however the Allowable Gradients can be changed later     */
/*     by the Critical Volume subroutine.  The values for the Initial Allowable     */
/*     Gradients are saved in a global array for later use by both the Critical     */
/*     Volume subroutine and the VPM Repetitive Algorithm subroutine.               */
/* ===============================================================================  */

        compartments=diver.getCompartments();
        elements=compartments.elements();

        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            
            regenerated_radius_n2=compartment.getN2RegeneratedRadius().getValue(Length.UNITS_METER);
            regenerated_radius_he=compartment.getHe2RegeneratedRadius().getValue(Length.UNITS_METER);
     	    initial_allowable_grad_n2_pa =
                Parameters.fGamma * 2.0 *
                (Parameters.fGammaC - Parameters.fGamma) /
                (regenerated_radius_n2 * Parameters.fGammaC);

            initial_allowable_grad_he_pa =
                Parameters.fGamma * 2.0 *
                (Parameters.fGammaC - Parameters.fGamma) /
                (regenerated_radius_he * Parameters.fGammaC);

            compartment.getN2InitialAllowableGradient().setValue(initial_allowable_grad_n2_pa,
                                                                 Pressure.UNITS_PASCAL);
            compartment.getHe2InitialAllowableGradient().setValue(initial_allowable_grad_he_pa,
                                                                 Pressure.UNITS_PASCAL);
            compartment.getN2AllowableGradient().setValue(initial_allowable_grad_n2_pa,
                                                                 Pressure.UNITS_PASCAL);
            compartment.getHe2AllowableGradient().setValue(initial_allowable_grad_he_pa,
                                                                 Pressure.UNITS_PASCAL);


        }
    }


    /** This method takes the diver to the start depth of the deco zone.
     *  Above this depth off gassing takes place.
     *  @param          diver The diver to be updated
     */
    public void ascendDiverToStartOfDecoZone(Diver diver)
            throws CalculationException
    {
        VaryingDepthSegment toStartOfDecoZone;

        try
        {
            toStartOfDecoZone=new VaryingDepthSegment(diveHeight,
                                                      startOfAscentDepth,
                                                      startOfDecoZoneDepth,
                                                      currentDecoStage.getAscentRate(),
                                                      currentDecoStage.getGasMixture());
            toStartOfDecoZone.setRunTime(fCurrentRunTime);
            toStartOfDecoZone.setSegmentNumber(iCurrentSegmentNumber);
            toStartOfDecoZone.exposeDiver(diver);
            fCurrentRunTime+=toStartOfDecoZone.getExposurePeriod();
            iCurrentSegmentNumber++;
        }
        catch (IllegalActionException e)
        {
            System.out.println(e.getMessage());
        }


    }

    /** This method takes the diver from the end of the dive directly to the
     *  surface. This is the case if no decompression is needed.
     *  @param          diver The diver to be updated
     */
    public void ascendDiverToSurface(Diver diver)
            throws CalculationException
    {
        VaryingDepthSegment toSurface;

        try
        {
            toSurface=new VaryingDepthSegment(diveHeight,
                                              startOfAscentDepth,
                                              new Length(0.0, Length.UNITS_METER),
                                              currentDecoStage.getAscentRate(),
                                              currentDecoStage.getGasMixture());
            addDecompressionSegment(toSurface);
            toSurface.exposeDiver(diver);

        }
        catch (IllegalActionException e)
        {
            System.out.println(e.getMessage());
        }

    }


    /** Critical volume iteration. Using the VPM relaxation algorithm,
     *  allowed super saturation gradients are relaxed until they converge.
     *  On entrance of the routine, the diver is supposed to be at the start
     *  of the decompression zone.
     *  @param          diver The diver. Defines the tissue compartments.
     */
    public  void criticalVolumeLoop(Diver diver) throws CalculationException
    {
        boolean bExitLoop;
        boolean bScheduleConverged;

        // Initialize some variables
        diver.resetLastPhaseVolumeTime();
        bScheduleConverged=false;

        // Backup some variables
        fRunTimeStartOfDecoZone=fCurrentRunTime;
        diver.backupTissueTensions(this.BACKUP_STARTDECOZONE);

/* ===============================================================================  */
/*     START OF CRITICAL VOLUME LOOP                                                */
/*     This loop operates between Lines 50 and 100.  If the Critical Volume         */
/*     Algorithm is toggled "off" in the program settings, there will only be       */
/*     one pass through this loop.  Otherwise, there will be two or more passes     */
/*     through this loop until the deco schedule is "converged" - that is when a    */
/*     comparison between the phase volume time of the present iteration and the    */
/*     last iteration is less than or equal to one minute.  This implies that       */
/*     the volume of released gas in the most recent iteration differs from the     */
/*     "critical" volume limit by an acceptably small amount.  The critical         */
/*     volume limit is set by the Critical Volume Parameter Lambda in the program   */
/*     settings (default setting is 7500 fsw-min with adjustability range from      */
/*     from 6500 to 8300 fsw-min according to Bruce Wienke).                        */
/* ===============================================================================  */
        bExitLoop=false;
        while (!bExitLoop)
        {
/* ===============================================================================  */
/*     CALCULATE CURRENT DECO CEILING BASED ON ALLOWABLE SUPERSATURATION            */
/*     GRADIENTS AND SET FIRST DECO STOP.  CHECK TO MAKE SURE THAT SELECTED STEP    */
/*     SIZE WILL NOT ROUND UP FIRST STOP TO A DEPTH THAT IS BELOW THE DECO ZONE.    */
/* ===============================================================================  */
            fDecoPhaseVolumeTime=0.0;
            calcAscentCeiling(diver);
            calcFirstDecoStop();

/* ===============================================================================  */
/*     PERFORM A SEPARATE "PROJECTED ASCENT" OUTSIDE OF THE MAIN PROGRAM TO MAKE    */
/*     SURE THAT AN INCREASE IN GAS LOADINGS DURING ASCENT TO THE FIRST STOP WILL   */
/*     NOT CAUSE A VIOLATION OF THE DECO CEILING.  IF SO, ADJUST THE FIRST STOP     */
/*     DEEPER BASED ON STEP SIZE UNTIL A SAFE ASCENT CAN BE MADE.                   */
/*     Note: this situation is a possibility when ascending from extremely deep     */
/*     dives or due to an unusual gas mix selection.                                */
/*     CHECK AGAIN TO MAKE SURE THAT ADJUSTED FIRST STOP WILL NOT BE BELOW THE      */
/*     DECO ZONE.                                                                   */
/* ===============================================================================  */

            projectedAscent(diver);
            if (decoStopDepth.largerThan(startOfDecoZoneDepth))
            {
                throw new CalculationException("1st deco stop below start of deco zone");
            }

            firstDecoStopDepth      = (Length)decoStopDepth.clone();
            firstDecoStopPressure   =new DepthPressure(firstDecoStopDepth, diveHeight);

/* ===============================================================================  */
/*     HANDLE THE SPECIAL CASE WHEN NO DECO STOPS ARE REQUIRED - ASCENT CAN BE      */
/*     MADE DIRECTLY TO THE SURFACE                                                 */
/*     Write ascent data to output file and exit the Critical Volume Loop.          */
/* ===============================================================================  */

            if (decoStopDepth.getValue(Length.UNITS_METER) <= 0.0)
            {
                currentDepth            =startOfAscentDepth;                // depth at start ascent
                fCurrentRunTime         =fRunTimeAtStartAscent;             // run time at start ascent
                iCurrentSegmentNumber   =iSegmentNumberAtStartAscent;       // segment number at start
                diver.restoreTissueTensions(BACKUP_STARTASCENT);            // tissue tensions ast start
                ascendDiverToSurface(diver);
                bExitLoop=true;
            }
            else
            {
                currentDepth          = (Length)startOfDecoZoneDepth.clone();
                calcDecoProfile(diver, false);

/* ===============================================================================  */
/*     COMPUTE TOTAL PHASE VOLUME TIME AND MAKE CRITICAL VOLUME COMPARISON          */
/*     The deco phase volume time is computed from the run time.  The surface       */
/*     phase volume time is computed in a subroutine based on the surfacing gas     */
/*     loadings from previous deco loop block.  Next the total phase volume time    */
/*     (in-water + surface) for each compartment is compared against the previous   */
/*     total phase volume time.  The schedule is converged when the difference is   */
/*     less than or equal to 1 minute in any one of the 16 compartments.            */
/*     Note:  the "phase volume time" is somewhat of a mathematical concept.        */
/*     It is the time divided out of a total integration of supersaturation         */
/*     gradient x time (in-water and surface).  This integration is multiplied      */
/*     by the excess bubble number to represent the amount of free-gas released     */
/*     as a result of allowing a certain number of excess bubbles to form.          */
/* ===============================================================================  */
/* end of deco stop loop */

                fDecoPhaseVolumeTime = fCurrentRunTime - fRunTimeStartOfDecoZone;

                bScheduleConverged=calcSurfacePhaseVolumeTime(diver);



/* =============================================================================== */
/*     CRITICAL VOLUME DECISION TREE BETWEEN LINES 70 AND 99 */
/*     There are two options here.  If the Critical Volume Agorithm setting is */
/*     "on" and the schedule is converged, or the Critical Volume Algorithm */
/*     setting was "off" in the first place, the program will re-assign variables */
/*     to their values at the start of ascent (end of bottom time) and process */
/*     a complete decompression schedule once again using all the same ascent */
/*     parameters and first stop depth.  This decompression schedule will match */
/*     the last iteration of the Critical Volume Loop and the program will write */
/*     the final deco schedule to the output file. */

/*     Note: if the Critical Volume Agorithm setting was "off", the final deco */
/*     schedule will be based on "Initial Allowable Supersaturation Gradients." */
/*     If it was "on", the final schedule will be based on "Adjusted Allowable */
/*     Supersaturation Gradients" (gradients that are "relaxed" as a result of */
/*     the Critical Volume Algorithm). */

/*     If the Critical Volume Agorithm setting is "on" and the schedule is not */
/*     converged, the program will re-assign variables to their values at the */
/*     start of the deco zone and process another trial decompression schedule. */
/* =============================================================================== */
/* L70: */

                if (bScheduleConverged || !Parameters.bCriticalVolumeAlgorithm)
                {
                    fCurrentRunTime         = fRunTimeAtStartAscent;
                    currentDepth.setValue(startOfAscentDepth);
                    currentDecoStage        = getCurrentDecoStage(currentDepth);
                    diver.restoreTissueTensions(BACKUP_STARTASCENT);
                    iCurrentSegmentNumber   = iSegmentNumberAtStartAscent;

                    decoStopDepth.setValue(firstDecoStopDepth);
                    decoStopPressure.setDepth(decoStopDepth);
            //	    last_run_time = 0.0;

/* ===============================================================================  */
/*     DECO STOP LOOP BLOCK FOR FINAL DECOMPRESSION SCHEDULE                        */
/* ===============================================================================  */
                    calcDecoProfile(diver, true);
                    bExitLoop=true;
                }
                else
/* ===============================================================================  */
/*     IF SCHEDULE NOT CONVERGED, COMPUTE RELAXED ALLOWABLE SUPERSATURATION         */
/*     GRADIENTS WITH VPM CRITICAL VOLUME ALGORITHM AND PROCESS ANOTHER             */
/*     ITERATION OF THE CRITICAL VOLUME LOOP                                        */
/* ===============================================================================  */
                {
                    criticalVolume(diver);
                    fCurrentRunTime = fRunTimeStartOfDecoZone;
                    currentDepth.setValue(startOfDecoZoneDepth);
                    currentDecoStage=getCurrentDecoStage(currentDepth);
                    diver.restoreTissueTensions(BACKUP_STARTDECOZONE);
                } /* end of critical volume decision */

            }
        } /* end of critical vol loop */


    }

/* =============================================================================== */
/*      SUBROUTINE CALC_ASCENT_CEILING                                             */
/*      Purpose: This subprogram calculates the ascent ceiling (the safe ascent    */
/*      depth) in each compartment, based on the allowable gradients, and then     */
/*      finds the deepest ascent ceiling across all compartments.                  */
/* =============================================================================== */

    /** Calculates the ascent ceiling (depth to safely ascend to), assuming
     *  instantanious ascent.
     *  @param          diver The diver. Defines the tissue compartments and
     *                  critical nuclei
     *  @return         -
     *  @exception      -
     */
    private void calcAscentCeiling(Diver diver)
    {

        TissueCompartment   compartment;
        Vector              compartments;
        Enumeration         elements;

        /* Local variables */
        double      weighted_allowable_gradient,
                    gas_loading,
                    tolerated_ambient_pressure,
                    helium_pressure,
                    nitrogen_pressure,
                    allowable_gradient_n2,
                    allowable_gradient_he,
                    ascent_ceiling_depth;

        Length      compartment_ascent_ceiling;

        boolean     bDecoCeilingDepthInitialized;
        
        double      fPressureOtherGasses;
/* loop */
/* ===============================================================================  */
/*     CALCULATIONS                                                                 */
/*     Since there are two sets of allowable gradients being tracked, one for       */
/*     helium and one for nitrogen, a "weighted allowable gradient" must be         */
/*     computed each time based on the proportions of helium and nitrogen in        */
/*     each compartment.  This proportioning follows the methodology of             */
/*     Buhlmann/Keller.  If there is no helium and nitrogen in the compartment,     */
/*     such as after extended periods of oxygen breathing, then the minimum value   */
/*     across both gases will be used.  It is important to note that if a           */
/*     compartment is empty of helium and nitrogen, then the weighted allowable     */
/*     gradient formula cannot be used since it will result in division by zero.    */
/* ===============================================================================  */

        fPressureOtherGasses        =Parameters.pressureOtherGasses.getValue(Pressure.UNITS_BAR);
        
        ascent_ceiling_depth        =0.0;                       // some initial value
        compartments                =diver.getCompartments();   // the divers tissue compartments
        elements                    =compartments.elements();
        bDecoCeilingDepthInitialized=false;                     // deco ceiling depht not initialized

        while (elements.hasMoreElements())                      // process all compartments
        {
            compartment=(TissueCompartment)elements.nextElement();
            
            helium_pressure         =compartment.getHe2TissueTension().getValue(Pressure.UNITS_BAR);
            nitrogen_pressure       =compartment.getN2TissueTension().getValue(Pressure.UNITS_BAR);
            allowable_gradient_he   =compartment.getHe2AllowableGradient().getValue(Pressure.UNITS_BAR);
            allowable_gradient_n2   =compartment.getN2AllowableGradient().getValue(Pressure.UNITS_BAR);

            gas_loading             =
                helium_pressure + nitrogen_pressure;
            if (gas_loading > 0.0)
            {
                weighted_allowable_gradient =
                    (allowable_gradient_he * helium_pressure +
                     allowable_gradient_n2 * nitrogen_pressure) /
                    (helium_pressure + nitrogen_pressure);
                tolerated_ambient_pressure =
                    gas_loading +
                    fPressureOtherGasses -
                    weighted_allowable_gradient;
            }
            else
            {
                /* Computing MIN */
                weighted_allowable_gradient = Math.min(allowable_gradient_n2,
                                                       allowable_gradient_he);
                tolerated_ambient_pressure =
                    fPressureOtherGasses - weighted_allowable_gradient;
            }

/* ===============================================================================  */
/*     The tolerated ambient pressure cannot be less than zero absolute, i.e.,      */
/*     the vacuum of outer space!                                                   */
/* ===============================================================================  */

            if (tolerated_ambient_pressure < 0.0)
            {
                tolerated_ambient_pressure = 0.0;
            }

            compartment_ascent_ceiling =
                DepthPressure.convertPressureToDepth
                    (new Pressure(tolerated_ambient_pressure, Pressure.UNITS_BAR),
                    diveHeight);

            if (!bDecoCeilingDepthInitialized)
            {
                ascentCeilingDepth=compartment_ascent_ceiling;
                bDecoCeilingDepthInitialized=true;
            }
            else
            {
                if (compartment_ascent_ceiling.largerThan(ascentCeilingDepth))
                {
                    ascentCeilingDepth=compartment_ascent_ceiling;
                }
            }

        }

/* ===============================================================================  */
/*     The Ascent Ceiling Depth is computed in a loop after all of the individual   */
/*     compartment ascent ceilings have been calculated.  It is important that the  */
/*     Ascent Ceiling Depth (max ascent ceiling across all compartments) only be    */
/*     extracted from the compartment values and not be compared against some       */
/*     initialization value.  For example, if MAX(Ascent_Ceiling_Depth . .) was     */
/*     compared against zero, this could cause a program lockup because sometimes   */
/*     the Ascent Ceiling Depth needs to be negative (but not less than zero        */
/*     absolute ambient pressure) in order to decompress to the last stop at zero   */
/*     depth.                                                                       */
/* ===============================================================================  */
    }
    
    
/* =============================================================================== */
/*      SUBROUTINE CALC_DECO_CEILING                                               */
/*      Purpose: This subprogram calculates the deco ceiling (the safe ascent      */
/*      depth) in each compartment, based on the allowable "deco gradients"        */
/*      computed in the Boyle's Law Compensation subroutine, and then finds the    */
/*      deepest deco ceiling across all compartments.  This deepest value          */
/*      (Deco Ceiling Depth) is then used by the Decompression Stop subroutine     */
/*      to determine the actual deco schedule.                                     */
/* =============================================================================== */ 

    /** Calculates the deco ceiling (depth to safely ascend to), assuming
     *  instantanious ascent.
     *  @param          diver The diver. Defines the tissue compartments and
     *                  critical nuclei
     */
    private void calcDecoCeiling(Diver diver)
    {

        TissueCompartment   compartment;
        Vector              compartments;
        Enumeration         elements;

        /* Local variables */
        double      weighted_allowable_gradient,
                    gas_loading,
                    tolerated_ambient_pressure,
                    helium_pressure,
                    nitrogen_pressure,
                    deco_gradient_n2,
                    deco_gradient_he,
                    deco_ceiling_depth;



        Length      compartment_deco_ceiling;


        boolean     bDecoCeilingDepthInitialized;
/* loop */
/* ===============================================================================  */
/*     CALCULATIONS                                                                 */
/*     Since there are two sets of deco gradients being tracked, one for            */
/*     helium and one for nitrogen, a "weighted allowable gradient" must be         */
/*     computed each time based on the proportions of helium and nitrogen in        */
/*     each compartment.  This proportioning follows the methodology of             */
/*     Buhlmann/Keller.  If there is no helium and nitrogen in the compartment,     */
/*     such as after extended periods of oxygen breathing, then the minimum value   */
/*     across both gases will be used.  It is important to note that if a           */
/*     compartment is empty of helium and nitrogen, then the weighted allowable     */
/*     gradient formula cannot be used since it will result in division by zero.    */
/* ===============================================================================  */

        deco_ceiling_depth          =0.0;                       // some initial value
        compartments                =diver.getCompartments();   // the divers tissue compartments
        elements                    =compartments.elements();
        bDecoCeilingDepthInitialized=false;                     // deco ceiling depht not initialized

        while (elements.hasMoreElements())                      // process all compartments
        {
            compartment=(TissueCompartment)elements.nextElement();
            
            helium_pressure     =compartment.getHe2TissueTension().getValue(Pressure.UNITS_BAR);
            nitrogen_pressure   =compartment.getN2TissueTension().getValue(Pressure.UNITS_BAR);
            deco_gradient_he    =compartment.getHe2DecoGradient().getValue(Pressure.UNITS_BAR);
            deco_gradient_n2    =compartment.getN2DecoGradient().getValue(Pressure.UNITS_BAR);

            gas_loading =
                helium_pressure + nitrogen_pressure;
            if (gas_loading > 0.0)
            {
                weighted_allowable_gradient =
                    (deco_gradient_he * helium_pressure +
                     deco_gradient_n2 * nitrogen_pressure) /
                    (helium_pressure + nitrogen_pressure);
                tolerated_ambient_pressure =
                    gas_loading +
                    Parameters.pressureOtherGasses.getValue(Pressure.UNITS_BAR) -
                    weighted_allowable_gradient;
            }
            else
            {
                /* Computing MIN */
                weighted_allowable_gradient = Math.min(deco_gradient_n2,
                                                       deco_gradient_he);
                tolerated_ambient_pressure =
                    Parameters.pressureOtherGasses.getValue(Pressure.UNITS_BAR) - 
                    weighted_allowable_gradient;
            }

/* ===============================================================================  */
/*     The tolerated ambient pressure cannot be less than zero absolute, i.e.,      */
/*     the vacuum of outer space!                                                   */
/* ===============================================================================  */

            if (tolerated_ambient_pressure < 0.0)
            {
                tolerated_ambient_pressure = 0.0;
            }

            // Here the ambient pressure is converted to depth
            compartment_deco_ceiling =
                DepthPressure.convertPressureToDepth
                    (new Pressure(tolerated_ambient_pressure, Pressure.UNITS_BAR),
                    diveHeight);

            // Find the compartment subscribing the deepest depth
            if (!bDecoCeilingDepthInitialized)
            {
                decoCeilingDepth=compartment_deco_ceiling;
                bDecoCeilingDepthInitialized=true;
            }
            else
            {
                if (compartment_deco_ceiling.largerThan(decoCeilingDepth))
                {
                    decoCeilingDepth=compartment_deco_ceiling;
                }
            }

        }

/* ===============================================================================  */
/*     The Deco Ceiling Depth is computed in a loop after all of the individual     */
/*     compartment deco ceilings have been calculated.  It is important that the    */
/*     Deco Ceiling Depth (max deco ceiling across all compartments) only be        */
/*     extracted from the compartment values and not be compared against some       */
/*     initialization value.  For example, if MAX(Deco_Ceiling_Depth . .) was       */
/*     compared against zero, this could cause a program lockup because sometimes   */
/*     the Deco Ceiling Depth needs to be negative (but not less than absolute      */
/*     zero) in order to decompress to the last stop at zero depth                  */
/* ===============================================================================  */
    }

    /** Calculates the 1st deco stop by rounding up the deco ceiling
     *  to the 1st multiple of the step size.
     */
    public void calcFirstDecoStop()
    {
        decoStopDepth=(Length)ascentCeilingDepth.clone();
        decoStopDepth.roundUp(currentDecoStage.getDecoStepSize());

        decoStopPressure=new DepthPressure(decoStopDepth, diveHeight);

    }

/* ===============================================================================  */
/*     SUBROUTINE PROJECTED_ASCENT                                                  */
/*     Purpose: This subprogram performs a simulated ascent outside of the main     */
/*     program to ensure that a deco ceiling will not be violated due to unusual    */
/*     gas loading during ascent (on-gassing).  If the deco ceiling is violated,    */
/*     the stop depth will be adjusted deeper by the step size until a safe         */
/*     ascent can be made.                                                          */
/* ===============================================================================  */
    /** Simulates the ascent to the deco stop depth.
     */

    public void projectedAscent(Diver diver)
    {
        /* Local variables */
        double      weighted_allowable_gradient,
                    ending_ambient_pressure,
                    initial_helium_pressure,
                    temp_gas_loading,
                    segment_time,
                    initial_inspired_n2_pressure,
                    new_ambient_pressure,
                    temp_helium_pressure,
                    initial_inspired_he_pressure,
                    allowable_gas_loading,
                    nitrogen_rate,
                    starting_ambient_pressure,
                    initial_nitrogen_pressure,
                    r1, r2,
                    helium_rate,
                    temp_nitrogen_pressure,
                    fraction_helium,
                    fraction_nitrogen,
                    rate,
                    helium_time_constant,
                    nitrogen_time_constant,
                    allowable_gradient_he,
                    allowable_gradient_n2;

        TissueCompartment   compartment;
        Vector              compartments;
        Enumeration         elements;

        boolean     bDecoStopDepthOk;
        boolean     bDecoDepthAdapted;
        
/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/* =============================================================================== */

        fraction_helium=currentDecoStage.getGasMixture().getHe2Fraction();      // fractions
        fraction_nitrogen=currentDecoStage.getGasMixture().getN2Fraction();
        rate=currentDecoStage.getAmbientPressureChangeRate().                   // rate
                              getValue(Pressure.UNITS_BAR);

        starting_ambient_pressure = startOfDecoZonePressure.getValue(Pressure.UNITS_BAR);
        initial_inspired_he_pressure =
            Tools.alveolarPressure(startOfDecoZonePressure,
                    fraction_helium,
                    Pressure.UNITS_BAR);
        initial_inspired_n2_pressure =
            Tools.alveolarPressure(startOfDecoZonePressure,
                    fraction_nitrogen,
                    Pressure.UNITS_BAR);
        helium_rate   = rate * fraction_helium;
        nitrogen_rate = rate * fraction_nitrogen;

        bDecoStopDepthOk=false;
        while (!bDecoStopDepthOk)
        {
            ending_ambient_pressure =  decoStopPressure.getValue(Pressure.UNITS_BAR);;
            segment_time = (ending_ambient_pressure - starting_ambient_pressure) / rate;

            compartments=diver.getCompartments();
            elements=compartments.elements();

            bDecoDepthAdapted=false;
            while (((elements.hasMoreElements()) && !bDecoDepthAdapted))
            {
                compartment=(TissueCompartment)elements.nextElement();
                
                initial_helium_pressure     =compartment.getHe2TissueTension().
                                                    getValue(Pressure.UNITS_BAR);
                initial_nitrogen_pressure   =compartment.getN2TissueTension().
                                                    getValue(Pressure.UNITS_BAR);
                allowable_gradient_he       =compartment.getHe2AllowableGradient().
                                                    getValue(Pressure.UNITS_BAR);
                allowable_gradient_n2       =compartment.getN2AllowableGradient().
                                                    getValue(Pressure.UNITS_BAR);
                helium_time_constant        =compartment.getHe2K();
                nitrogen_time_constant      =compartment.getN2K();
                temp_helium_pressure =
                    Tools.schreinerEquation(initial_inspired_he_pressure,
                            helium_rate,
                            segment_time,
                            helium_time_constant,
                            initial_helium_pressure);
                temp_nitrogen_pressure =
                    Tools.schreinerEquation(initial_inspired_n2_pressure,
                            nitrogen_rate,
                            segment_time,
                            nitrogen_time_constant,
                            initial_nitrogen_pressure);
                temp_gas_loading =
                    temp_helium_pressure +
                    temp_nitrogen_pressure;
                if (temp_gas_loading > 0.0)
                {
                    weighted_allowable_gradient =
                        (   allowable_gradient_he * temp_helium_pressure +
                            allowable_gradient_n2 * temp_nitrogen_pressure) /
                        temp_gas_loading;
                }
                else
                {
                    /* Computing MIN */
                    weighted_allowable_gradient =
                        Math.min(allowable_gradient_he, allowable_gradient_n2);
                }
                allowable_gas_loading =
                    ending_ambient_pressure +
                    weighted_allowable_gradient -
                    Parameters.pressureOtherGasses.getValue(Pressure.UNITS_BAR);

                if (temp_gas_loading > allowable_gas_loading)
                {
                    decoStopDepth.addLength(currentDecoStage.getDecoStepSize());
                    decoStopPressure.setDepth(decoStopDepth);
                    bDecoDepthAdapted=true;
                }
                
            }
            if (!bDecoDepthAdapted)
                bDecoStopDepthOk=true;

        }
    }


    /** Calculate the decompression profile
     *  each tissue compartment of the diver.
     *  @param          diver The diver. Defines the tissue compartments.
     *  @param          bFinalProfile 
     */
    public void calcDecoProfile(Diver diver, boolean bFinalProfile)
                                            throws CalculationException
    {
        boolean             bDiverSurfaced;

        VaryingDepthSegment ascentStage;

/* ===============================================================================  */
/*     ASSIGN VARIABLES FOR ASCENT FROM START OF DECO ZONE TO FIRST STOP.  SAVE     */
/*     FIRST STOP DEPTH FOR LATER USE WHEN COMPUTING THE FINAL ASCENT PROFILE       */
/* ===============================================================================  */


/* ===============================================================================  */
/*     DECO STOP LOOP BLOCK WITHIN CRITICAL VOLUME LOOP                             */
/*     This loop computes a decompression schedule to the surface during each       */
/*     iteration of the critical volume loop.  No output is written from this       */
/*     loop, rather it computes a schedule from which the in-water portion of the   */
/*     total phase volume time (Deco_Phase_Volume_Time) can be extracted.  Also,    */
/*     the gas loadings computed at the end of this loop are used the subroutine    */
/*     which computes the out-of-water portion of the total phase volume time       */
/*     (Surface_Phase_Volume_Time) for that schedule.                               */
/*     Note that exit is made from the loop after last ascent is made to a deco     */
/*     stop depth that is less than or equal to zero.  A final deco stop less       */
/*     than zero can happen when the user makes an odd step size change during      */
/*     ascent - such as specifying a 5 msw step size change at the 3 msw stop!      */
/* ===============================================================================  */

        bDiverSurfaced=false;
        while(!bDiverSurfaced)  // loop will run continuous until diver surfaces
        {
            try
            {

                ascentStage=new VaryingDepthSegment(diveHeight,
                                                    currentDepth,
                                                    decoStopDepth,
                                                    currentDecoStage.getAscentRate(),
                                                    currentDecoStage.getGasMixture());
                if (bFinalProfile)
                {
                    addDecompressionSegment(ascentStage);
                }
                else
                {
                    iCurrentSegmentNumber++;
                    fCurrentRunTime+=ascentStage.getExposurePeriod();
                }
                ascentStage.exposeDiver(diver);
           }
            catch(IllegalActionException e)
            {
                System.err.println(e.getMessage());
            }
/* ===============================================================================  */
/*     DURING FINAL DECOMPRESSION SCHEDULE PROCESS, COMPUTE MAXIMUM ACTUAL          */
/*     SUPERSATURATION GRADIENT RESULTING IN EACH COMPARTMENT                       */
/*     If there is a repetitive dive, this will be used later in the VPM            */
/*     Repetitive Algorithm to adjust the values for critical radii.                */
/* ===============================================================================  */

            if (bFinalProfile)
            {
    		    calcMaxActualGradient(diver);
            }


            // compare to 1 micrometer in order to compensate for rounding errors
            if (decoStopDepth.getValue(Length.UNITS_METER) <= 0.000001)
            {
                bDiverSurfaced=true;
            }
            else
            {

                currentDecoStage=getCurrentDecoStage(decoStopDepth);     // get current decostage

                boylesLawCompensation();
                
                decompressionStop(diver, bFinalProfile);
                currentDepth.setValue(decoStopDepth);                               // update current depth
                decoStopDepth.substractLength(currentDecoStage.getDecoStepSize());  // next decostop
                decoStopPressure.setDepth(decoStopDepth);
//                last_run_time = run_time;
            }
            /* L60: */
        }

    }

    /** Calculate the decompression stop period
     *  each tissue compartment of the diver.
     *  @param          diver The diver. Defines the tissue compartments.
     */
    public void decompressionStop(Diver diver, boolean bFinalProfile)
                                                throws CalculationException
    {
        /* Format strings */
//        static char fmt_905[] = "ERROR! OFF-GASSING GRADIENT IS TOO SMALL TO DECOMPRESS AT THE %6.1lf STOP\n";
        //was:"(\0020ERROR! OFF-GASSING GRADIENT IS TOO SMALL TO DECOMPRESS\0021x,\002AT THE\002,f6.1,1x,\002STOP\002)";
//        static char fmt_906[] = "REDUCE STEP SIZE OR INCREASE OXYGEN FRACTION\n";
        //was: "(\0020REDUCE STEP SIZE OR INCREASE OXYGEN FRACTION\002)";
//        static char fmt_907[] = "\n";
        //was: "(\002 \002)";



        int             last_segment_number,
                        i;

        /* Local variables */
        double      inspired_nitrogen_pressure,
                    weighted_allowable_gradient,
                    initial_helium_pressure,
                    initial_nitrogen_pressure,
//                    time_counter,
                    segment_time,
//                    ambient_pressure,
                    inspired_helium_pressure,
//                    next_stop,
//                    last_run_time,
                    temp_segment_time,
//                    deco_ceiling_depth,
                    round_up_operation,
                    helium_fraction,
                    nitrogen_fraction,
                    deco_gradient_he,
                    deco_gradient_n2;

        TissueCompartment       compartment;
        Vector                  compartments;
        Enumeration             elements;

        boolean                 bDiverDecompressedEnough;
        Length                  nextStopDepth;
        DepthPressure           nextStopPressure;
        ConstantDepthSegment decoStopSegment;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/* =============================================================================== */


        helium_fraction     =currentDecoStage.getGasMixture().getHe2Fraction();
        nitrogen_fraction   =currentDecoStage.getGasMixture().getN2Fraction();

//        last_run_time       = fCurrentRunTime;
        round_up_operation  =
            Math.round(fCurrentRunTime / Parameters.fMinimumDecoStopTime + 0.5) *
            Parameters.fMinimumDecoStopTime;
        segment_time        = round_up_operation - fCurrentRunTime;
//        fCurrentRunTime     = round_up_operation;
        temp_segment_time   = segment_time;
//        last_segment_number = fCurrentSegmentNumber;

//        fCurrentSegmentNumber= last_segment_number + 1;

//        ambient_pressure =decoStopPressure.getValue(Pressure.UNITS_BAR);

//        ending_ambient_pressure = ambient_pressure;
        nextStopDepth=(Length)decoStopDepth.clone();
        nextStopDepth.substractLength(currentDecoStage.getDecoStepSize());
        nextStopPressure=new DepthPressure(nextStopDepth, diveHeight);
//        next_stop = nextStopDepth.getValue(Length.UNITS_METER);
        inspired_helium_pressure =
            Tools.alveolarPressure(decoStopPressure,
                    helium_fraction,
                    Pressure.UNITS_BAR);

        inspired_nitrogen_pressure =
            Tools.alveolarPressure(decoStopPressure,
                    nitrogen_fraction,
                    Pressure.UNITS_BAR);


/* =============================================================================== */
/*     Check to make sure that program won't lock up if unable to decompress */
/*     to the next stop.  If so, write error message and terminate program. */
/* =============================================================================== */

        compartments=diver.getCompartments();
        elements=compartments.elements();
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            
            deco_gradient_he =compartment.getHe2DecoGradient().
                                                getValue(Pressure.UNITS_BAR);
            deco_gradient_n2 =compartment.getN2DecoGradient().
                                                getValue(Pressure.UNITS_BAR);
            if (inspired_helium_pressure + inspired_nitrogen_pressure > 0.0)
            {
                weighted_allowable_gradient =
                    (deco_gradient_he * inspired_helium_pressure +
                     deco_gradient_n2 * inspired_nitrogen_pressure) /
                    (inspired_helium_pressure + inspired_nitrogen_pressure);
                if (inspired_helium_pressure + inspired_nitrogen_pressure +
                        Parameters.pressureOtherGasses.getValue(Pressure.UNITS_BAR) - 
                        weighted_allowable_gradient >
                        nextStopPressure.getValue(Pressure.UNITS_BAR))
                {
                    throw new CalculationException("ERROR! OFF-GASSING GRADIENT IS TOO SMALL TO DECOMPRESS\n"+
                                                   "REDUCE STEP SIZE OR INCREASE OXYGEN FRACTION");
                }
            }

        }

        bDiverDecompressedEnough=false;
        while (!bDiverDecompressedEnough)
        {
            decoStopSegment=new ConstantDepthSegment(diveHeight,
                                                        decoStopDepth,
                                                        segment_time,
                                                        currentDecoStage.getGasMixture());
            decoStopSegment.exposeDiver(diver);

            calcDecoCeiling(diver);
            if (decoCeilingDepth.largerThan(nextStopDepth))
            {
                segment_time = Parameters.fMinimumDecoStopTime;
//                time_counter = temp_segment_time;
                temp_segment_time += segment_time;
//                last_run_time = run_time;
//                fCurrentRunTime = last_run_time + Parameters.fMinimumDecoStopTime;
            }
            else
            {
                bDiverDecompressedEnough=true;
            }
        }
        segment_time = temp_segment_time;     // total segment time spent at deco stop
        decoStopSegment=new ConstantDepthSegment(diveHeight,
                                                 decoStopDepth,
                                                 segment_time,
                                                 currentDecoStage.getGasMixture());
        if (bFinalProfile)
        {
            this.addDecompressionSegment(decoStopSegment);  // register segment
        }
        else
        {
            iCurrentSegmentNumber++;
            fCurrentRunTime+=decoStopSegment.getExposurePeriod();
        }
    }


/* ===============================================================================  */
/*     SUBROUTINE CALC_SURFACE_PHASE_VOLUME_TIME                                    */
/*     Purpose: This subprogram computes the surface portion of the total phase     */
/*     volume time.  This is the time factored out of the integration of            */
/*     supersaturation gradient x time over the surface interval.  The VPM          */
/*     considers the gradients that allow bubbles to form or to drive bubble        */
/*     growth both in the water and on the surface after the dive.                  */
/*     This subroutine is a new development to the VPM algorithm in that it         */
/*     computes the time course of supersaturation gradients on the surface         */
/*     when both helium and nitrogen are present.  Refer to separate write-up       */
/*     for a more detailed explanation of this algorithm.                           */
/* ===============================================================================  */

    private boolean calcSurfacePhaseVolumeTime(Diver diver)
    {
        /* Local variables */
        double              decay_time_to_zero_gradient,
                            integral_gradient_x_time,
                            surface_inspired_n2_pressure,
                            nitrogen_time_constant,
                            helium_time_constant,
                            nitrogen_pressure,
                            helium_pressure,
                            surface_phase_volume_time,
                            phase_volume_time;
        int                 i;

        AtmosphericPressure surfacePressure;
        TissueCompartment   compartment;
        Vector              compartments;
        Enumeration         elements;

        boolean             bScheduleConverged;
/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/* =============================================================================== */

        surfacePressure=new AtmosphericPressure(diveHeight);

        surface_inspired_n2_pressure =
            Tools.alveolarPressure(surfacePressure, 0.79, Pressure.UNITS_BAR);

        bScheduleConverged=false;

        compartments=diver.getCompartments();
        elements=compartments.elements();
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            nitrogen_time_constant=compartment.getN2K();
            helium_time_constant=compartment.getHe2K();
            nitrogen_pressure=compartment.getN2TissueTension().
                                            getValue(Pressure.UNITS_BAR);
            helium_pressure=compartment.getHe2TissueTension().
                                            getValue(Pressure.UNITS_BAR);

            if (nitrogen_pressure > surface_inspired_n2_pressure)
            {
                surface_phase_volume_time =
                    (helium_pressure / helium_time_constant +
                        (nitrogen_pressure - surface_inspired_n2_pressure) /
                            nitrogen_time_constant) /
                    (helium_pressure + nitrogen_pressure -
                        surface_inspired_n2_pressure);
            }
            else
            if (nitrogen_pressure <= surface_inspired_n2_pressure &&
            helium_pressure + nitrogen_pressure >= surface_inspired_n2_pressure)
            {
                decay_time_to_zero_gradient =
                    1.0 / (nitrogen_time_constant - helium_time_constant) *
                    Math.log((surface_inspired_n2_pressure - nitrogen_pressure) /
                    helium_pressure);
                integral_gradient_x_time =
                    helium_pressure /
                        helium_time_constant *
                            (1.0 - Math.exp(-helium_time_constant *
                                decay_time_to_zero_gradient)) +
                       (nitrogen_pressure - surface_inspired_n2_pressure) /
                        nitrogen_time_constant *
                            (1.0 - Math.exp(-nitrogen_time_constant *
                                decay_time_to_zero_gradient));
                surface_phase_volume_time =
                    integral_gradient_x_time /
                    (helium_pressure +
                        nitrogen_pressure -
                        surface_inspired_n2_pressure);
            }
            else
            {
                surface_phase_volume_time = 0.0;
            }
            compartment.setSurfacePhaseVolumeTime(surface_phase_volume_time);

            phase_volume_time=
                fDecoPhaseVolumeTime + surface_phase_volume_time;

            compartment.setPhaseVolumeTime(phase_volume_time);

            // critical volume comparison
            if (Math.abs(phase_volume_time - compartment.getLastPhaseVolumeTime()) <= 1.)
            {
                bScheduleConverged = true;
            }
            else
            {
                compartment.setLastPhaseVolumeTime(phase_volume_time);
            }



        }

        return bScheduleConverged;
    }

/* ===============================================================================  */
/*     SUBROUTINE CRITICAL_VOLUME                                                   */
/*     Purpose: This subprogram applies the VPM Critical Volume Algorithm.  This    */
/*     algorithm will compute "relaxed" gradients for helium and nitrogen based     */
/*     on the setting of the Critical Volume Parameter Lambda.                      */
/* ===============================================================================  */


    public void criticalVolume(Diver diver)
    {
        int             i;

        /* Local variables */
        double          initial_allowable_grad_n2_pa,
                        initial_allowable_grad_he_pa,
                        parameter_lambda_pascals,
                        b,
                        c,
                        new_allowable_grad_n2_pascals,
                        new_allowable_grad_he_pascals,
                        adj_crush_pressure_n2_pascals,
                        adj_crush_pressure_he_pascals;

        TissueCompartment   compartment;
        Vector              compartments;
        Enumeration         elements;
        

/* loop */
/* ===============================================================================  */
/*     CALCULATIONS                                                                 */
/*     Note:  Since the Critical Volume Parameter Lambda was defined in units of    */
/*     fsw-min in the original papers by Yount and colleauges, the same             */
/*     convention is retained here.  Although Lambda is adjustable only in units    */
/*     of fsw-min in the program settings (range from 6500 to 8300 with default     */
/*     7500), it will convert to the proper value in Pascals-min in this            */
/*     subroutine regardless of which diving pressure units are being used in       */
/*     the main program - feet of seawater (fsw) or meters of seawater (msw).       */
/*     The allowable gradient is computed using the quadratic formula (refer to     */
/*     separate write-up posted on the Deco List web site).                         */
/* ===============================================================================  */

        parameter_lambda_pascals =
            Parameters.lambda.getValue(Pressure.UNITS_PASCAL);
//        for (i = 1; i <= 16; ++i) {
//            phase_volume_time[i - 1] =
//                *deco_phase_volume_time + surface_phase_volume_time[i - 1];
//        }

        compartments=diver.getCompartments();
        elements=compartments.elements();
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            adj_crush_pressure_he_pascals =
                compartment.getHe2AdjMaxCrushingPressure().
                            getValue(Pressure.UNITS_PASCAL);
            initial_allowable_grad_he_pa =
                compartment.getHe2InitialAllowableGradient().
                            getValue(Pressure.UNITS_PASCAL);

            b = initial_allowable_grad_he_pa + parameter_lambda_pascals *
                Parameters.fGamma /
                (Parameters.fGammaC * compartment.getPhaseVolumeTime());
            c = Parameters.fGamma *
                (Parameters.fGamma * (
                parameter_lambda_pascals * adj_crush_pressure_he_pascals)) /
                (Parameters.fGammaC *
                 (Parameters.fGammaC * compartment.getPhaseVolumeTime()));
            /* Computing 2nd power */
            new_allowable_grad_he_pascals =
                (b + Math.sqrt(b * b - c * 4.0)) / 2.0;
            compartment.getHe2AllowableGradient().
                        setValue(new_allowable_grad_he_pascals, Pressure.UNITS_PASCAL);


            adj_crush_pressure_n2_pascals =
                compartment.getN2AdjMaxCrushingPressure().getValue(Pressure.UNITS_PASCAL);
            initial_allowable_grad_n2_pa =
                compartment.getN2InitialAllowableGradient().getValue(Pressure.UNITS_PASCAL);
            b = initial_allowable_grad_n2_pa + parameter_lambda_pascals *
                Parameters.fGamma /
                (Parameters.fGammaC * compartment.getPhaseVolumeTime());
            c = Parameters.fGamma *
                (Parameters.fGamma *
                (parameter_lambda_pascals * adj_crush_pressure_n2_pascals)) /
                (Parameters.fGammaC *
                (Parameters.fGammaC * compartment.getPhaseVolumeTime()));
            /* Computing 2nd power */
            new_allowable_grad_n2_pascals =
                (b + Math.sqrt(b * b - c * 4.0)) / 2.0;
            compartment.getN2AllowableGradient().
                        setValue(new_allowable_grad_n2_pascals, Pressure.UNITS_PASCAL);


        }
    }


/* ===============================================================================  */
/*     SUBROUTINE CALC_MAX_ACTUAL_GRADIENT                                          */
/*     Purpose: This subprogram calculates the actual supersaturation gradient      */
/*     obtained in each compartment as a result of the ascent profile during        */
/*     decompression.  Similar to the concept with crushing pressure, the           */
/*     supersaturation gradients are not cumulative over a multi-level, staged      */
/*     ascent.  Rather, it will be the maximum value obtained in any one discrete   */
/*     step of the overall ascent.  Thus, the program must compute and store the    */
/*     maximum actual gradient for each compartment that was obtained across all    */
/*     steps of the ascent profile.  This subroutine is invoked on the last pass    */
/*     through the deco stop loop block when the final deco schedule is being       */
/*     generated.                                                                   */
/*     The max actual gradients are later used by the VPM Repetitive Algorithm to   */
/*     determine if adjustments to the critical radii are required.  If the max     */
/*     actual gradient did not exceed the initial alllowable gradient, then no      */
/*     adjustment will be made.  However, if the max actual gradient did exceed     */
/*     the intitial allowable gradient, such as permitted by the Critical Volume    */
/*     Algorithm, then the critical radius will be adjusted (made larger) on the    */
/*     repetitive dive to compensate for the bubbling that was allowed on the       */
/*     previous dive.  The use of the max actual gradients is intended to prevent   */
/*     the repetitive algorithm from being overly conservative.                     */
/* ===============================================================================  */


    public void calcMaxActualGradient(Diver diver)
    {
        double              compartment_gradient;
        TissueCompartment   compartment;
        Vector              compartments;
        Enumeration         elements;
        Pressure            maxActualGradient;

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/*     Note: negative supersaturation gradients are meaningless for this */
/*     application, so the values must be equal to or greater than zero. */
/* =============================================================================== */

        compartments=diver.getCompartments();
        elements=compartments.elements();
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            compartment_gradient =
                compartment.getHe2TissueTension().getValue(Pressure.UNITS_BAR) +
                compartment.getN2TissueTension().getValue(Pressure.UNITS_BAR) +
                Parameters.pressureOtherGasses.getValue(Pressure.UNITS_BAR) -
                decoStopPressure.getValue(Pressure.UNITS_BAR);

            if (compartment_gradient <= 0.0)
            {
                compartment_gradient = 0.0;
            }
            maxActualGradient=compartment.getMaxActualGradient();
            if (compartment_gradient>maxActualGradient.getValue(Pressure.UNITS_BAR))
            {
                maxActualGradient.setValue(compartment_gradient, Pressure.UNITS_BAR);
            }
        }
    }

/* =============================================================================== */
/*      SUBROUTINE BOYLES_LAW_COMPENSATION                                         */
/*      Purpose: This subprogram calculates the reduction in allowable gradients   */
/*      with decreasing ambient pressure during the decompression profile based    */
/*      on Boyle's Law considerations.                                             */
/* =============================================================================== */
    /**
     *  
     */
    private void boylesLawCompensation() throws CalculationException
    {
/* =============================================================================== */
/*     LOCAL VARIABLES                                                             */
/* =============================================================================== */
        int                     i;
        TissueCompartment       compartment;
        Vector                  compartments;
        Enumeration             elements;
        
        Length                  nextStopDepth;
        DepthPressure           nextStopPressure;
        DepthPressure           firstStopPressure;
        double                  Amb_Press_First_Stop_Pascals, 
                                Amb_Press_Next_Stop_Pascals,
                                A, B, C, Low_Bound, High_Bound, Ending_Radius,
                                Deco_Gradient_Pascals,
                                Allow_Grad_First_Stop_He_Pa, Radius_First_Stop_He,
                                Allow_Grad_First_Stop_N2_Pa, Radius_First_Stop_N2;
        

/* =============================================================================== */
/*     LOCAL ARRAYS                                                                */
/* =============================================================================== */
      double[] Radius1_He=new double[Parameters.nCompartments];      
      double[] Radius2_He=new double[Parameters.nCompartments];      
      double[] Radius1_N2=new double[Parameters.nCompartments];      
      double[] Radius2_N2=new double[Parameters.nCompartments];      

/* =============================================================================== */
/*      CALCULATIONS                                                               */
/* =============================================================================== */
  
        nextStopDepth               =(Length)decoStopDepth.clone();
        nextStopDepth.substractLength(currentDecoStage.getDecoStepSize());
        nextStopPressure            =new DepthPressure(nextStopDepth, diveHeight);

        Amb_Press_First_Stop_Pascals=this.firstDecoStopPressure.getValue(Pressure.UNITS_PASCAL);
        Amb_Press_Next_Stop_Pascals =nextStopPressure.getValue(Pressure.UNITS_PASCAL);
        
        i=0;
        compartments=diver.getCompartments();
        elements=compartments.elements();
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            
            // Update He2 Decompression gradient
            
            Allow_Grad_First_Stop_He_Pa =compartment.getHe2AllowableGradient().getValue(Pressure.UNITS_PASCAL);
            
            Radius_First_Stop_He        = (2.0 * Parameters.fGamma) / Allow_Grad_First_Stop_He_Pa;
            
            Radius1_He[i]               = Radius_First_Stop_He;
            
            A                           = Amb_Press_Next_Stop_Pascals;
            B                           = -2.0 * Parameters.fGamma;
            C                           = (Amb_Press_First_Stop_Pascals + (2.0*Parameters.fGamma)/
                                            Radius_First_Stop_He)* Radius_First_Stop_He*
                                            (Radius_First_Stop_He*(Radius_First_Stop_He));
            Low_Bound                   = Radius_First_Stop_He;
            High_Bound                  = Radius_First_Stop_He*
                                            Math.pow(Amb_Press_First_Stop_Pascals/Amb_Press_Next_Stop_Pascals, 1.0/3.0);

            // Throws CalculationException:
            Ending_Radius               = Tools.radiusRootFinder(A, B, C, Low_Bound, High_Bound);

            Radius2_He[i]               = Ending_Radius;
            
            Deco_Gradient_Pascals       = (2.0 * Parameters.fGamma) / Ending_Radius;

            // Store the value
            compartment.getHe2DecoGradient().setValue(Deco_Gradient_Pascals, Pressure.UNITS_PASCAL);

            // Update Nitrogen deco gradient
            Allow_Grad_First_Stop_N2_Pa = compartment.getN2AllowableGradient().getValue(Pressure.UNITS_PASCAL);

            Radius_First_Stop_N2        = (2.0 * Parameters.fGamma) /  Allow_Grad_First_Stop_N2_Pa;

            Radius1_N2[i]               = Radius_First_Stop_N2;
            
            A                           = Amb_Press_Next_Stop_Pascals;
            B                           = -2.0 * Parameters.fGamma;
            C                           = (Amb_Press_First_Stop_Pascals + (2*Parameters.fGamma)/
                                            Radius_First_Stop_N2)* Radius_First_Stop_N2*
                                            (Radius_First_Stop_N2*(Radius_First_Stop_N2));
            Low_Bound                   = Radius_First_Stop_N2;
            High_Bound                  = Radius_First_Stop_N2*
                                          Math.pow(Amb_Press_First_Stop_Pascals/ Amb_Press_Next_Stop_Pascals, 1.0/3.0);

            Ending_Radius               = Tools.radiusRootFinder(A, B, C, Low_Bound, High_Bound);

            Radius2_N2[i]               = Ending_Radius;
            Deco_Gradient_Pascals       = (2.0 * Parameters.fGamma) / Ending_Radius;

            // Store the value
            compartment.getN2DecoGradient().setValue(Deco_Gradient_Pascals, Pressure.UNITS_PASCAL);
            
            i++;
        }        
/* =============================================================================== */
/*      END OF SUBROUTINE                                                          */
/* =============================================================================== */
    }
    


}