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

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This abstract class represents the decompression. It contains
 * basic functionality for calculating and printing decompression profiles.
 * This class shall be taken as base class to inherrit concrete
 * decompression algorithms from.
 */

public abstract class Decompression {
    /*------------------------------------------------------------------------------------------------*\
     * Variables
    \*------------------------------------------------------------------------------------------------*/

    /**
     * The segments defining the decompression profile
     */
    protected Vector<DepthSegment> decoSegments = null;          // the decompression segments

    /**
     * The last dive segment, prior to decompression
     */
    protected DepthSegment lastDiveSegment;            // dive segment prior to deco

    /**
     * Decompression stages defined by depth, deco step size, ascent rate
     */
    protected Vector<DecoStage> decoStages = null;            // the decompression stages


    /**
     * The segment number at the start of ascent
     */
    protected int iSegmentNumberAtStartAscent;
    /**
     * The run time at start of ascent
     */
    protected double fRunTimeAtStartAscent;

    /**
     * Exact depth above which off-gassing takes place
     */
    protected Length startOfDecoZoneDepth;       // depth at which off-gasing starts

    /**
     * Ambient pressure at start of deco zone
     */
    protected DepthPressure startOfDecoZonePressure;    // amb pressure at start of deco zone

    /**
     * Deepest possible deco stop depth (1st stop above start of deco zone)
     */
    protected Length deepestDecoStopDepth;       // deepest possible deco stop

    /**
     * Current run time
     */
    protected double fCurrentRunTime;

    /**
     * Current segment number
     */
    protected int iCurrentSegmentNumber;

    /**
     * Current depth
     */
    protected Length currentDepth;

    /**
     * current diver
     */
    protected Diver diver;

    /**
     * Current deco stage
     */
    protected DecoStage currentDecoStage;           // current decompression stage


    /**
     * Last deco segment to which diver has been updated
     */
    protected DepthSegment lastDecoSegment;


    /*------------------------------------------------------------------------------------------------*\
     * Construction, initialisation and reinitialising
    \*------------------------------------------------------------------------------------------------*/

    /**
     * Constructor. Initializes some stuff
     */
    public Decompression() {
        decoSegments = new Vector<DepthSegment>();
        decoStages = new Vector<DecoStage>();
        startOfDecoZoneDepth = null;
        currentDecoStage = null;
        lastDecoSegment = null;
        lastDiveSegment = null;
    }

    /**
     * This method resets the decompression. It scratches previous
     * calculated values.
     */
    public void resetDecompression() {
        decoSegments.clear();
        startOfDecoZoneDepth = null;
        currentDecoStage = null;
        lastDecoSegment = null;
        lastDiveSegment = null;
    }

    /**
     * Adds a decompression stage to the list. A decompression stage is
     * defined by a start depth, an ascent rate, an gas mixture and a deco
     * step size.
     *
     * @param iIndex    Index in the array. Set to -1 if the item should be appended to the array.
     * @param decoStage The decompression stage to be added
     */
    public void addDecompressionStage(int iIndex, DecoStage decoStage) {
        if ((iIndex >= 0) && (iIndex < decoStages.size())) {
            decoStages.add(iIndex, decoStage);
        } else {
            decoStages.add(decoStage);
        }
    }

    /**
     * Adds a decompression stage to the list. A decompression stage is
     * defined by a start depth, an ascent rate, an gas mixture and a deco
     * step size.
     *
     * @param decoStage The decompression stage to be added
     */
    public void addDecompressionStage(DecoStage decoStage) {
        decoStages.add(decoStage);
    }


    /**
     * Adds a decompression segment to the list.
     *
     * @param decoSegment Depth segment to be added
     */
    public void addDecompressionSegment(DepthSegment decoSegment) {
        decoSegment.setRunTime(fCurrentRunTime);
        decoSegment.setSegmentNumber(iCurrentSegmentNumber);

        decoSegments.add(decoSegment);

        iCurrentSegmentNumber++;
        fCurrentRunTime += decoSegment.getExposurePeriod();
    }
    
    /*------------------------------------------------------------------------------------------------*\
     * Get information
    \*------------------------------------------------------------------------------------------------*/

    /**
     * This method returns the runTime at any moment (depending on the state of the calculation)
     *
     * @return The runtime in minutes
     */
    public double getRunTime() {
        return fCurrentRunTime;
    }

    /**
     * This method returns the current segment number.
     *
     * @return Segment number.
     */
    public int getSegmentNumber() {
        return iCurrentSegmentNumber;
    }

    /**
     * This method returns the decostage in which the indicated depth
     * belongs.
     *
     * @param currentDepth Depth for which the decostage is looked up.
     * @return The decostage or null if the decostage is not found
     */
    public DecoStage getCurrentDecoStage(Length currentDepth) {
        DecoStage stage;
        Enumeration elements;
        DecoStage nextStage;
        boolean bExit;
        double fCompareDepth;

        bExit = false;
        stage = null;


        // deco stage depths often correspond to deco stop depths
        // the 'larger than' equality might be evaluated wrong due to
        // rounding errors.
        // compare depth: substract 1 micrometer to compensate for rounding errors
        fCompareDepth = currentDepth.getValue(Length.UNITS_METER) - 0.000001;


        elements = decoStages.elements();

        while (elements.hasMoreElements() && !bExit) {
            nextStage = (DecoStage) elements.nextElement();

            if (fCompareDepth > nextStage.getStartDepth().getValue(Length.UNITS_METER)) {
                bExit = true;
            } else {
                stage = nextStage;
            }


        }

        return stage;
    }

    /**
     * This method returns the Vector containing the Decompression Stages.
     *
     * @return Vector containing the DecoStage instances.
     */
    public Vector<DecoStage> getDecoStages() {
        return this.decoStages;
    }

    /**
     * This method sets a new Vector of decostages
     *
     * @param decoStages The new set of decostages.
     */
    public void setDecoStages(Vector<DecoStage> decoStages) {
        if (decoStages != null) {
            this.decoStages = decoStages;
        }
    }

    /**
     * This method returns the name or a short description of the algorithm.
     *
     * @return String identifying the algorithm.
     */
    public abstract String getAlgorithmDescription();
    
    
    /*------------------------------------------------------------------------------------------------*\
     * Calculation
    \*------------------------------------------------------------------------------------------------*/

    /**
     * Decompresses the specified diver: i.e. calculate a decompression
     * profile for the given diver so that he returns safely to the surface.
     * The diver is updated during the decompression
     *
     * @param diver        The diver. Defines the tissue compartments and
     *                     critical nuclei. The diver is updated up to the moment
     *                     of ascent and decompression
     * @param diveSegments The diving segments prior to decompression
     * @param fRunTime     Run time in minutes at start of decompression
     * @throws CalculationException
     */
    public abstract void decompressDiver(Diver diver,
                                         Vector<DepthSegment> diveSegments,
                                         double fRunTime)
            throws CalculationException;


    /**
     */
    public abstract void calculateInitialAllowableGradient(Diver diver);


    /**
     * Verify the list of decompression stages.
     *
     * @throws IllegalStateException When there is an inconsistency
     */
    public void verifyDecoStages() throws IllegalStateException {
        DecoStage stage;
        Enumeration elements;
        DecoStage nextStage;

        if (decoStages == null) {
            throw new IllegalStateException("No deco stages defined");
        }

        stage = null;
        elements = decoStages.elements();
        while (elements.hasMoreElements()) {
            nextStage = (DecoStage) elements.nextElement();

            if (stage != null) {
                if (nextStage.getStartDepth().largerThan(stage.getStartDepth())) {
                    throw new IllegalStateException("Deco stage depths not correct");
                }
            }

            stage = nextStage;

            if (stage.getAscentRate().getValue(Length.UNITS_METER) >= 0.0) {
                throw new IllegalStateException("Ascent rates should be negative");
            }
        }

    }

/* ===============================================================================  */
/*     SUBROUTINE CALC_START_OF_DECO_ZONE                                           */
/*     Purpose: This subroutine uses the Bisection Method to find the depth at      */
/*     which the leading compartment just enters the decompression zone.            */
/*     Source:  "Numerical Recipes in Fortran 77", Cambridge University Press,      */
/*     1992.                                                                        */
/* ===============================================================================  */

    /**
     * Calculates the depth at which actual decompression starts (i.e. the depth
     * off gassing starts). Above this depth there is at least one tissue compartment
     * which is supersaturated.
     * Remark: currentDecoStage should be set according start depth
     *
     * @param diver The diver. Defines the tissue compartments and
     *              critical nuclei
     * @throws CalculationException
     * @throws IllegalStateException
     */

    public void calcStartOfDecoZone(Diver diver)
            throws CalculationException, IllegalStateException {
    /* Local variables */
        int j;

        double last_diff_change,
                initial_helium_pressure,
                mid_range_nitrogen_pressure,

                initial_inspired_n2_pressure,
                low_bound,
                initial_inspired_he_pressure,
                high_bound_nitrogen_pressure,
                nitrogen_rate,
                function_at_mid_range,
                function_at_low_bound,
                high_bound,
                mid_range_helium_pressure,
                mid_range_time,
                starting_ambient_pressure,
                initial_nitrogen_pressure,
                function_at_high_bound,

                time_to_start_of_deco_zone,
                high_bound_helium_pressure,
                helium_rate,
                differential_change,
                depth_start_of_deco_zone,
                rate,
                helium_time_constant,
                nitrogen_time_constant;
        GasMixture currentGasMixture;
        TissueCompartment compartment;
        Vector compartments;
        Enumeration elements;
        boolean bExit;

        Length compartmentStartOfDecoZone = new Length(0.0, Length.UNITS_METER);
/* loop */
/* ===============================================================================  */
/*     CALCULATIONS                                                                 */
/*     First initialize some variables                                              */
/* ===============================================================================  */

        if (currentDecoStage == null) {
            throw new IllegalStateException("Current deco stage not initialized");
        }

        if (lastDiveSegment == null) {
            throw new IllegalStateException("Last dive segment not initialized");
        }


        currentGasMixture = currentDecoStage.getGasMixture();


        rate = currentDecoStage.getAmbientPressureChangeRate().
                getValue(Pressure.UNITS_BAR);

        starting_ambient_pressure =
                lastDiveSegment.getAmbientPressureAtEnd().getValue(Pressure.UNITS_BAR);

        initial_inspired_he_pressure =
                Tools.alveolarPressure(lastDiveSegment.getAmbientPressureAtEnd(),
                        currentGasMixture.getHe2Fraction(),
                        Pressure.UNITS_BAR);


        initial_inspired_n2_pressure =
                Tools.alveolarPressure(lastDiveSegment.getAmbientPressureAtEnd(),
                        currentGasMixture.getN2Fraction(),
                        Pressure.UNITS_BAR);

        helium_rate = rate * currentGasMixture.getHe2Fraction();
        nitrogen_rate = rate * currentGasMixture.getN2Fraction();

/* ===============================================================================  */
/*     ESTABLISH THE BOUNDS FOR THE ROOT SEARCH USING THE BISECTION METHOD          */
/*     AND CHECK TO MAKE SURE THAT THE ROOT WILL BE WITHIN BOUNDS.  PROCESS         */
/*     EACH COMPARTMENT INDIVIDUALLY AND FIND THE MAXIMUM DEPTH ACROSS ALL          */
/*     COMPARTMENTS (LEADING COMPARTMENT)                                           */
/*     In this case, we are solving for time - the time when the gas tension in     */
/*     the compartment will be equal to ambient pressure.  The low bound for time   */
/*     is set at zero and the high bound is set at the time it would take to        */
/*     ascend to zero ambient pressure (absolute).  Since the ascent rate is        */
/*     negative, a multiplier of -1.0 is used to make the time positive.  The       */
/*     desired point when gas tension equals ambient pressure is found at a time    */
/*     somewhere between these endpoints.  The algorithm checks to make sure that   */
/*     the solution lies in between these bounds by first computing the low bound   */
/*     and high bound function values.                                              */
/* ===============================================================================  */

        low_bound = 0.0;
        high_bound = starting_ambient_pressure / rate * -1.0;

        compartments = diver.getCompartments();
        elements = compartments.elements();
        while (elements.hasMoreElements()) {
            compartment = (TissueCompartment) elements.nextElement();

            nitrogen_time_constant = compartment.getN2K();
            helium_time_constant = compartment.getHe2K();

            initial_helium_pressure =
                    compartment.getHe2TissueTension().getValue(Pressure.UNITS_BAR);
            initial_nitrogen_pressure =
                    compartment.getN2TissueTension().getValue(Pressure.UNITS_BAR);
            function_at_low_bound =
                    initial_helium_pressure +
                            initial_nitrogen_pressure +
                            Parameters.pressureOtherGasses.getValue(Pressure.UNITS_BAR) -
                            starting_ambient_pressure;
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
            function_at_high_bound = high_bound_helium_pressure +
                    high_bound_nitrogen_pressure +
                    Parameters.pressureOtherGasses.getValue(Pressure.UNITS_BAR);
            if (function_at_high_bound * function_at_low_bound >= 0.0) {
                throw new CalculationException("ERROR! ROOT IS NOT WITHIN BRACKETS");
            }

/* ===============================================================================  */
/*     APPLY THE BISECTION METHOD IN SEVERAL ITERATIONS UNTIL A SOLUTION WITH       */
/*     THE DESIRED ACCURACY IS FOUND                                                */
/*     Note: the program allows for up to 100 iterations.  Normally an exit will    */
/*     be made from the loop well before that number.  If, for some reason, the     */
/*     program exceeds 100 iterations, there will be a pause to alert the user.     */
/* ===============================================================================  */

            if (function_at_low_bound < 0.0) {
                time_to_start_of_deco_zone = low_bound;
                differential_change = high_bound - low_bound;
            } else {
                time_to_start_of_deco_zone = high_bound;
                differential_change = low_bound - high_bound;
            }

            j = 0;
            bExit = false;
            while ((j < 100) && !bExit) {
                last_diff_change = differential_change;
                differential_change = last_diff_change * 0.5;
                mid_range_time = time_to_start_of_deco_zone +
                        differential_change;
                mid_range_helium_pressure =
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
                function_at_mid_range =
                        mid_range_helium_pressure +
                                mid_range_nitrogen_pressure +
                                Parameters.pressureOtherGasses.getValue(Pressure.UNITS_BAR) -
                                (starting_ambient_pressure + rate * mid_range_time);
                if (function_at_mid_range <= 0.0) {
                    time_to_start_of_deco_zone = mid_range_time;
                }
                if (Math.abs(differential_change) < 0.001 ||
                        function_at_mid_range == 0.0) {
                    bExit = true;
                }
                j++;
            }

            if (!bExit) {
                throw new CalculationException("ERROR! ROOT SEARCH EXCEEDED MAXIMUM ITERATIONS");
            }
/* ===============================================================================  */
/*     When a solution with the desired accuracy is found, the program passes the   */
/*     exception throwing and assigns the solution value for the individual         */
/*     compartment.                                                                 */
/* ===============================================================================  */
/*
            cpt_depth_start_of_deco_zone =
                starting_ambient_pressure +
                rate * time_to_start_of_deco_zone -
                barometric_pressure;
*/

            compartmentStartOfDecoZone = (Length) currentDecoStage.getAscentRate().clone();
            compartmentStartOfDecoZone.multiplyLength(time_to_start_of_deco_zone);
            compartmentStartOfDecoZone.addLength(lastDiveSegment.getDepthAtEnd());


/* ===============================================================================  */
/*     The overall solution will be the compartment with the maximum depth where    */
/*     gas tension equals ambient pressure (leading compartment).                   */
/* ===============================================================================  */

            if (startOfDecoZoneDepth == null) {
                startOfDecoZoneDepth = compartmentStartOfDecoZone;
            } else {
                if (compartmentStartOfDecoZone.largerThan(startOfDecoZoneDepth)) {
                    startOfDecoZoneDepth = compartmentStartOfDecoZone;
                }
            }

        }

        startOfDecoZonePressure = new DepthPressure(startOfDecoZoneDepth,
                lastDiveSegment.getDiveHeight());
    }

    /**
     * Based on the startOfDecoZoneDepth, the next deeper deco stop depth
     * is calculated. The deco step is a multiple of the deco step size.
     * To calculate the startOfDecoZoneDedpth, calcStartOfDecoZone should
     * be called.
     *
     * @throws CalculationException when startOfDecoZoneDepth is not defined.
     */
    public void calculateDeepestPossibleDecoStop() throws CalculationException {
        double step_size,
                rounding_operation,
                deepest_possible_stop_depth,
                depth_start_of_deco_zone;

        if (startOfDecoZoneDepth == null) {
            throw new CalculationException("Start of deco zone not calculated");
        }

        if (Parameters.iPresentationPressureUnits == Pressure.UNITS_FSW) {
            step_size = this.currentDecoStage.getDecoStepSize().getValue(Length.UNITS_FEET);
            depth_start_of_deco_zone = startOfDecoZoneDepth.getValue(Length.UNITS_FEET);
            if (step_size > 10.0) {
                step_size = 10.0;
            }
            rounding_operation = depth_start_of_deco_zone / step_size - 0.5;
            deepest_possible_stop_depth = Math.round(rounding_operation) * step_size;
            deepestDecoStopDepth = new Length(deepest_possible_stop_depth,
                    Length.UNITS_FEET);
        }
        if (Parameters.iPresentationPressureUnits == Pressure.UNITS_MSW) {
            step_size = this.currentDecoStage.getDecoStepSize().getValue(Length.UNITS_METER);
            depth_start_of_deco_zone = startOfDecoZoneDepth.getValue(Length.UNITS_METER);
            if (step_size > 3.0) {
                step_size = 3.0;
            }
            rounding_operation = depth_start_of_deco_zone / step_size - 0.5;
            deepest_possible_stop_depth = Math.round(rounding_operation) * step_size;
            deepestDecoStopDepth = new Length(deepest_possible_stop_depth,
                    Length.UNITS_METER);
        }

    }

    /*------------------------------------------------------------------------------------------------*\
     * Printing the dive table
    \*------------------------------------------------------------------------------------------------*/

    public void printDecoTable(Writer writer) throws IOException {
        DepthSegment segment;
        Enumeration elements;

        Object[] args = new Object[2];

        writer.write(Text.sReport14);

        if (Parameters.iPresentationPressureUnits == Pressure.UNITS_FSW) {
            args[0] = new Double(startOfDecoZoneDepth.getValue(Length.UNITS_FEET));
            args[1] = Text.sReport50a;
        } else {
            args[0] = new Double(startOfDecoZoneDepth.getValue(Length.UNITS_METER));
            args[1] = Text.sReport50b;
        }
        writer.write(MessageFormat.format(Text.sReport15, args));
        if (Parameters.iPresentationPressureUnits == Pressure.UNITS_FSW) {
            args[0] = new Double(deepestDecoStopDepth.getValue(Length.UNITS_FEET));
            args[1] = Text.sReport50a;
        } else {
            args[0] = new Double(deepestDecoStopDepth.getValue(Length.UNITS_METER));
            args[1] = Text.sReport50b;
        }
        writer.write(MessageFormat.format(Text.sReport16, args));

        writer.write(Text.sReport17);
        writer.write(Text.sReport18);

        if (Parameters.iPresentationPressureUnits == Pressure.UNITS_FSW) {
            args[0] = Text.sReport50a;
            args[1] = Text.sReport51a;
        } else {
            args[0] = Text.sReport50b;
            args[1] = Text.sReport51b;
        }
        writer.write(MessageFormat.format(Text.sReport19, args));
        writer.write(Text.sReport20);


        elements = decoSegments.elements();
        while (elements.hasMoreElements()) {
            segment = (DepthSegment) elements.nextElement();

            segment.printDecoTableEntry(writer);
        }
        writer.write("\n");

    }

}