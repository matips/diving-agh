/*
 * Title:        DoDeco
 * Description:  DoDeco generates decompression profiles. Several algoritms
 *               have been implemented.
 * Copyright:    GNU Public Licence
 * Author:       Jorgen van der Velde
 *               Original VPMDeco and VPM-B from Fortran code by Erik C Baker
 * Disclaimer:   Do not use for real diving. Software may contain errors.
 *               For experimental and educational use only
 * Version:      1.0
 */

package net.deepocean.dodeco.calculator;

import java.util.Vector;

/**
 *
 * @author Jorgen
 */
public class ZHL16WithGradientDecompression extends ZHL16Decompression
{

    /** This is the gradient factor (conservatism factor) at 
     *  the deepest deco stop
     */
    private double                      fLowGradientFactor;
    
    /**
     *  This is the gradient factor at the surface
     */
    private double                      fHighGradientFactor;
    
    
    
    
    
    private double                      fCurrentGradientFactor;
    

    private double                      fGradientSlopePerMeter;

    
    
    /** Creates a new instance of ZH16LDecompression. By default
     *  the model is set to ZH-L16B. 
     */
    public ZHL16WithGradientDecompression()
    { 
    }
    
    
    /** Decompresses the specified diver: i.e. calculate a decompression
     *  profile for the given diver so that he returns safely to the surface.
     *  The diver is updated during the decompression.
     *  @param          diver The diver. Defines the tissue compartments and
     *                  critical nuclei. The diver is updated up to the moment
     *                  of ascent and decompression
     *  @param          diveSegments The diving segments prior to decompression
     *  @param          fRunTime Run time in minutes at start of decompression
     *  @exception      CalculationException 
     */
    public void decompressDiver(        Diver diver,
                                        Vector<DepthSegment> diveSegments,
                                        double fRunTime)
                                        throws CalculationException
    {
        double fCurrentStopDepth;
        
        // Initialise
        this.fLowGradientFactor= Parameters.fLowGradientFactor;
        this.fHighGradientFactor= Parameters.fHighGradientFactor;
        this.fCurrentGradientFactor=this.fLowGradientFactor;
        
        this.diver          =diver;
        this.diveSegments   =diveSegments;
        this.fCurrentRunTime=fRunTime;
        
        // Start with extracting some information fromt the dives
        getInformationFromDive();
        
        // Calculate the start of the decompression zone
        // This information is printed in the decotable. It is not used for 
        // the rest of the calculation
        currentDecoStage=getCurrentDecoStage(depthAtEndOfDive);
        calcStartOfDecoZone(diver);
        
        
        // Find the 1st decompression stop
        findFirstDecoStop();

        
        // Calculate the slope with which the GradientFactor changes per meter
        fCurrentStopDepth=currentDecoStopDepth.getValue(Length.UNITS_METER);
        if (fCurrentStopDepth>0.0)
        {
            fGradientSlopePerMeter=(fHighGradientFactor-fLowGradientFactor)/(0.0-fCurrentStopDepth);
        }
        
        // check, check, double check...
        if (!checkDiverSafety(currentDecoStopDepth))
        {
            throw new CalculationException("Error in algorithm: diver get the bends");
        }
        
        // Remember this one, again just for printing in the table
        deepestDecoStopDepth=currentDecoStopDepth;
        
        // Now proceed to the surface
        while (this.currentDecoStopDepth.largerThan(new Length(0.001, Length.UNITS_METER)))
        {
            // Calculate the gradient factor for current stop depth
            fCurrentStopDepth=currentDecoStopDepth.getValue(Length.UNITS_METER);
            fCurrentGradientFactor=fCurrentStopDepth*fGradientSlopePerMeter+fHighGradientFactor;            
            
            // calculate the decostop length, stay at stop and ascend to next stop
            stayAtStopAndProceedToNextStop();
            
            // check,check double check
            if (!checkDiverSafety(currentDecoStopDepth))
            {
                throw new CalculationException("Error in algorithm: diver get the bends");
            }
        
            
        }
        
    }
    
    /**
     *  This method calculates the allowed tissue tension limit based on the 
     *  compartment number, the N2 and He2 tensions and the ambient pressure.
     *  This method overrides the original Buhlmann limit with a version 
     *  designed by Erik Baker
     *  @param iTissueCompartment Index of the tissue compartment
     *  @param ambientPressure Ambient pressure to which the diver is exposed
     *  @param fN2Tension The Nitrogen tissue tension
     *  @param fHe2Tension The Helium tissue tension
     *  @return The limit to the tension. The sum of Helium and Nitrogen tension should stay below this value
     */
    protected double calculateTissueTensionLimit(int iTissueCompartment, Pressure ambientPressure, double fN2Tension, double fHe2Tension)
    {
        double fA;
        double fB;
        double fLimit;
        
        fA=(N2A[iTissueCompartment]*fN2Tension+He2A[iTissueCompartment]*fHe2Tension)/
            (fN2Tension+fHe2Tension);
        fB=(N2B[iTissueCompartment]*fN2Tension+He2B[iTissueCompartment]*fHe2Tension)/
            (fN2Tension+fHe2Tension);
        
// Old Buhlmann limit        
//      fLimit=ambientPressure.getValue(Pressure.UNITS_BAR)/fB+fA;        
    
// New limit, Baker style
        fLimit=ambientPressure.getValue(Pressure.UNITS_BAR)*
               (fCurrentGradientFactor/fB-fCurrentGradientFactor+1.0)+fA*fCurrentGradientFactor; 
        
        return fLimit;
    }
    
    
    
    /**
     *  This method returns the name or a short description of the algorithm.
     *  @return String identifying the algorithm.
     */
    public String getAlgorithmDescription()
    {
        String sName;
        
        sName=super.getAlgorithmDescription();
        
        sName+=" Gradient";
        return sName;
    }
}
