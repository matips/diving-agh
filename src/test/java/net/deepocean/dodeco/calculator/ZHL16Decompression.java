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
 *
 * @author Jorgen
 */
public class ZHL16Decompression extends Decompression
{
    public static final int ZH_L16A=1;
    public static final int ZH_L16B=2;
    public static final int ZH_L16C=3;
    


    /** The Nitrogen Buhlmann a factors in Bar that are used */
    public double[] N2A;
    
    public double[] N2B;
    
    public double[] He2A;
    
    public double[] He2B;
    
   
    protected int                       iCurrentModel;    
    
    /** The diver to apply the decompression to */
    protected Diver                     diver;
    
    /**  The segments of the dive */
    protected Vector<DepthSegment>      diveSegments;
    
    
    /** The hight of the dive */
    protected Length                      diveHeight;
    
    /** Depth at the end of dive/start of deco */
    protected Length                      depthAtEndOfDive;
    
   

    
    /** The atmospheric pressure at the dive heigth */
    protected AtmosphericPressure         atmosphericPressure;
    
    /** Tissue compartments of the Diver */
    protected Vector<TissueCompartment>   tissueCompartments;

    /** The first decompression stop, as calculated */
    protected Length                      firstDecoStopDepth;
    
    /** Variable indicating the current decompression stop during calculation */
    protected Length                      currentDecoStopDepth;
    
    /** Creates a new instance of ZH16LDecompression. By default
     *  the model is set to ZH-L16B. 
     */
    public ZHL16Decompression()
    { 
        firstDecoStopDepth=new Length(0.0, Length.UNITS_METER);
    
        // By default: use the ZH16LB model
        iCurrentModel=ZH_L16B;
        
        N2A=Parameters.N2A_BSeries;
        N2B=Parameters.N2B;
        
        He2A=Parameters.He2A;
        He2B=Parameters.He2B;
        
    }
    
    /**
     *  This method sets the model
     *  @param model Model. Set to ZH16
     */
    public void setModel(int model)
    {
        switch (model)
        {
            case ZH_L16A:
                N2A=Parameters.N2A_ASeries;
                break;
            case ZH_L16B:
                N2A=Parameters.N2A_BSeries;
                break;
            case ZH_L16C:
                N2A=Parameters.N2A_CSeries;
                break;
            default:
                N2A=Parameters.N2A_BSeries;;
                break;
        }
    }
    
    /**
     *  This method initialises some variables based on the dive segments.
     */
    protected void getInformationFromDive()
    {
        lastDiveSegment         =diveSegments.lastElement();
        
        iCurrentSegmentNumber   =lastDiveSegment.getSegmentNumber();
        
        depthAtEndOfDive        =lastDiveSegment.getDepthAtEnd();
        
        diveHeight=lastDiveSegment.getDiveHeight();
        
        atmosphericPressure     =new AtmosphericPressure(diveHeight);
        
        tissueCompartments      =diver.getCompartments();
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
        this.diver          =diver;
        this.diveSegments   =diveSegments;
        this.fCurrentRunTime=fRunTime;
        
        getInformationFromDive();
        
        // Calculate the start of the decompression zone
        // This information is printed in the decotable. It is not used for 
        // the rest of the calculation
        currentDecoStage=getCurrentDecoStage(depthAtEndOfDive);
        calcStartOfDecoZone(diver);
        
        // Find the 1st decompression stop
        findFirstDecoStop();
        
        if (!checkDiverSafety(currentDecoStopDepth))
        {
            throw new CalculationException("Error in algorithm: diver get the bends");
        }
        
        // Remember this one, again just for printing in the table
        deepestDecoStopDepth=currentDecoStopDepth;
        
        // Now proceed to the surface
        while (this.currentDecoStopDepth.largerThan(new Length(0.001, Length.UNITS_METER)))
        {
            // calculate the decostop length, stay at stop and ascend to next stop
            stayAtStopAndProceedToNextStop();
            
            if (!checkDiverSafety(currentDecoStopDepth))
            {
                throw new CalculationException("Error in algorithm: diver get the bends");
            }
        
            
        }
        
    }
    
    /**
     *  This method finds the first decompression depth. The method used is
     *  rather unoptimised: for each tissue compartment all decompression
     *  depths are checked (from 0.0 downwards) till the depth is found
     *  for which tissue compartment tension is within limits.
     *  This method takes the inert gas loading during ascent to the 1st
     *  stop into account.
     *  This method adds a VaryingDepth segment to the decompression profile
     *  taking the diver to the 1st stop depth.
     *  Note: this is very poor programming from a performance perspective. From
     *  a mathematic perspective, it is easy. And computers are fast nowadays.... :-)
     *  @exception CalculationException if for a TissueCompartment no safe depth is found
     *             In fact this means the diver is at an unsafe depth
     */
    protected void findFirstDecoStop() throws CalculationException
    {
        Enumeration         compartments;
        TissueCompartment   compartment;
        boolean             bFound;
        double              fN2InitialTension;
        double              fHe2InitialTension;
        double              fN2FinalTension;
        double              fHe2FinalTension;
        double              fN2InitialAlveolarPressure;
        double              fHe2InitialAlveolarPressure;
        double              fN2FinalAlveolarPressure;
        double              fHe2FinalAlveolarPressure;
        double              fN2Fraction;
        double              fHe2Fraction;
        double              fN2AlveolarPressureChangeRate;
        double              fHe2AlveolarPressureChangeRate;
        double              fAscentPeriod;
        double              fN2K;
        double              fHe2K;
        GasMixture          gasMixture;
        Pressure            initialAmbientPressure;
        DepthPressure       finalAmbientPressure;
        Length              proposedStopDepth;
        Length              ascentRate;
        int                 iIndex;
        double              fLimit;

        VaryingDepthSegment ascentToFirstDecoStop;
        DecoStage           decoStage;
        
        
        
        // Initialise some helper variables
        fAscentPeriod               =0.0;
        proposedStopDepth           =new Length(0.0, Length.UNITS_METER);
        finalAmbientPressure        =new DepthPressure(proposedStopDepth, diveHeight);
        
        // Gas mixture that is used for the ascent. The Mixture is used 
        decoStage                   =getCurrentDecoStage(depthAtEndOfDive);
        gasMixture                  =decoStage.getGasMixture();
        fN2Fraction                 =gasMixture.getN2Fraction();
        fHe2Fraction                =gasMixture.getHe2Fraction();
        
        // The ascent rate is defined with the decostage
        ascentRate                  =decoStage.getAscentRate();
        
        // prepare information obtained from the last dive segment
        initialAmbientPressure      =lastDiveSegment.getAmbientPressureAtEnd();

        fN2InitialAlveolarPressure  = Tools.alveolarPressure(initialAmbientPressure,
                fN2Fraction,
                Pressure.UNITS_BAR);
        fHe2InitialAlveolarPressure = Tools.alveolarPressure(initialAmbientPressure,
                fHe2Fraction,
                Pressure.UNITS_BAR);

        // Parse all tissue compartments
        iIndex=0;
        compartments=diver.getCompartments().elements();
        while (compartments.hasMoreElements())
        {
            compartment=(TissueCompartment)compartments.nextElement();

            // prepare information obtained from the compartment
            fN2InitialTension           =compartment.getN2TissueTension().getValue(Pressure.UNITS_BAR); 
            fHe2InitialTension          =compartment.getHe2TissueTension().getValue(Pressure.UNITS_BAR);
            fN2K                        =compartment.getN2K();
            fHe2K                       =compartment.getHe2K();
            
           
            // Start at depth 0.0 and proceed downwards, until a depth is found
            // at which the TissueCompartment tension is within ZH16-L limits
            proposedStopDepth.setValue(0.0, Length.UNITS_METER);
            bFound=false;
            while (!bFound && proposedStopDepth.smallerThan(depthAtEndOfDive))
            {
                // the ambient pressure at the proposed stop
                finalAmbientPressure.setDepth(proposedStopDepth);

                // the time in minutes to get from the end of the dive to this proposed stop depth
                fAscentPeriod=(proposedStopDepth.getValue(Length.UNITS_METER) - 
                               lastDiveSegment.getDepthAtEnd().getValue(Length.UNITS_METER)) /
                               ascentRate.getValue(Length.UNITS_METER);
                
                // Get the N2 tissue tension at this proposed stop depth
                fN2FinalAlveolarPressure   = Tools.alveolarPressure(finalAmbientPressure,
                        fN2Fraction,
                        Pressure.UNITS_BAR);
                fN2AlveolarPressureChangeRate=
                                   (fN2FinalAlveolarPressure - fN2InitialAlveolarPressure) /
                                   fAscentPeriod;
                
                fN2FinalTension             = Tools.schreinerEquation(fN2InitialAlveolarPressure,
                        fN2AlveolarPressureChangeRate,
                        fAscentPeriod,
                        fN2K,
                        fN2InitialTension);

                // Get the He2 tissue tension at this proposed stop depth
                fHe2FinalAlveolarPressure   = Tools.alveolarPressure(finalAmbientPressure,
                        fHe2Fraction,
                        Pressure.UNITS_BAR);
                fHe2AlveolarPressureChangeRate=
                                   (fHe2FinalAlveolarPressure - fHe2InitialAlveolarPressure) /
                                   fAscentPeriod;
                
                fHe2FinalTension            = Tools.schreinerEquation(fHe2InitialAlveolarPressure,
                        fHe2AlveolarPressureChangeRate,
                        fAscentPeriod,
                        fHe2K,
                        fHe2InitialTension);

                // Calculate the ZH16-L limit
                fLimit                      =calculateTissueTensionLimit(   iIndex, 
                                                                            finalAmbientPressure, 
                                                                            fN2FinalTension, 
                                                                            fHe2FinalTension);
                
                // The check whether TissueCompartment tension is within the limit
                // for the proposed depth.
                if (fN2FinalTension+fHe2FinalTension<fLimit)
// More safe: use the initial tension                    
//                if (fN2InitialTension+fHe2InitialTension<fLimit)
                {
                    if (proposedStopDepth.largerThan(firstDecoStopDepth))
                    {
                        firstDecoStopDepth.setValue(proposedStopDepth);
                    }
                    bFound=true;
                }
                else
                {                
                    // proceed to next deeper stop depth
                    proposedStopDepth.addLength(Parameters.decoStepSize);
                }
            }
            
            if (!bFound)
            {
                throw new CalculationException("No safe depth found");
            }
            iIndex++;
        }
       
       try
       {
           // Create a new ascent segment 
            ascentToFirstDecoStop=new VaryingDepthSegment(diveHeight, 
                                                         depthAtEndOfDive,
                                                         firstDecoStopDepth, 
                                                         ascentRate,
                                                         gasMixture);
            // update segment number and and runtime
            iCurrentSegmentNumber++;
            ascentToFirstDecoStop.setSegmentNumber(iCurrentSegmentNumber);
            ascentToFirstDecoStop.setRunTime(fCurrentRunTime);
            fCurrentRunTime+=ascentToFirstDecoStop.getExposurePeriod();
            
            // update the diver for the exposure
            ascentToFirstDecoStop.exposeDiver(diver);
            
            // update currentDecoStopDepth
            currentDecoStopDepth=firstDecoStopDepth;
       }
       catch(IllegalActionException e)
       {
           throw new CalculationException(e.getMessage());
       }
       
       decoSegments.add(ascentToFirstDecoStop);
         
    }
    
    /**
     *  This method calculates the time to stay at current decostop. This is done
     *  by simulating a stay of minimum decostop time and an ascent to next stop.
     *  After this a check is made whether tissue tension stays within limits.
     *  If not, a longer stop is simulated and an ascent. Again a check is made.
     *  This is repeated until the the tissue tension remains within limits. 
     *  A ConstantDepthSegment and a VaryingDepthSegment are added representing
     *  the stay at the deco stop and the ascent to the next stop.
     *  @exception CalculationException Is thrown when someting odd occurs during
     *             calculation, e.g. a stay longer than 24 hours.
     */
    protected void stayAtStopAndProceedToNextStop() throws CalculationException
    {
        Enumeration             compartments;
        TissueCompartment       compartment;
        GasMixture              gasMixture;
        DecoStage               decoStage;
        boolean                 bFound;

        double                  fN2InitialTension;
        double                  fHe2InitialTension;
        double                  fHe2TensionAfterStay;      
        double                  fN2TensionAfterStay;      
        double                  fN2FinalTension;
        double                  fHe2FinalTension;
        
        double                  fN2InitialAlveolarPressure;
        double                  fHe2InitialAlveolarPressure;
        double                  fN2FinalAlveolarPressure;
        double                  fHe2FinalAlveolarPressure;  
        
        double                  fN2AlveolarPressureChangeRate;
        double                  fHe2AlveolarPressureChangeRate;
        
        double                  fN2Fraction;
        double                  fHe2Fraction;    
       
        double                  fN2K;
        double                  fHe2K;

        double                  fMinutes;
        double                  fMaxMinutes;
        double                  fAscentPeriod;
        
        double                  fLimit;
        
        int                     iIndex;
        
        DepthPressure           initialAmbientPressure;
        DepthPressure           finalAmbientPressure;
        
        Length                  nextStopDepth;
        Length                  ascentRate;
        
        VaryingDepthSegment     ascentToNextStop;
        ConstantDepthSegment stayAtStop;
        
        
        // Calculate the next stop depth simply by substracting the deco stop size from currentDecoStopDepth
        nextStopDepth               =new Length(currentDecoStopDepth);
        nextStopDepth.substractLength(Parameters.decoStepSize);
        if (nextStopDepth.smallerThan(Length.ZERO))
        {
            nextStopDepth.setValue(Length.ZERO);
        }
        
        // Gas mixture that is used during the deco stop
        decoStage                       =getCurrentDecoStage(currentDecoStopDepth);
        gasMixture                      =decoStage.getGasMixture();     
        fN2Fraction                     =gasMixture.getN2Fraction();
        fHe2Fraction                    =gasMixture.getHe2Fraction();
        
        // The ascent rate
        ascentRate                      =decoStage.getAscentRate();

        // Alveolar pressures used during the deco stop
        initialAmbientPressure          =new DepthPressure(currentDecoStopDepth, diveHeight);
        finalAmbientPressure            =new DepthPressure(nextStopDepth       , diveHeight);
        
        fN2InitialAlveolarPressure      = Tools.alveolarPressure(initialAmbientPressure,
                fN2Fraction,
                Pressure.UNITS_BAR);
        fHe2InitialAlveolarPressure     = Tools.alveolarPressure(initialAmbientPressure,
                fHe2Fraction,
                Pressure.UNITS_BAR);
        
        // Alveolar pressures at next stop
        // Get the N2 tissue tension at this proposed stop depth
        fN2FinalAlveolarPressure        = Tools.alveolarPressure(finalAmbientPressure,
                fN2Fraction,
                Pressure.UNITS_BAR);
        fHe2FinalAlveolarPressure       = Tools.alveolarPressure(finalAmbientPressure,
                fHe2Fraction,
                Pressure.UNITS_BAR);

        // Calculate the alveoloar pressure change rate when going to the next stop
        fAscentPeriod=-Parameters.decoStepSize.getValue(Length.UNITS_METER)/
                       ascentRate.getValue(Length.UNITS_METER);
                
        fN2AlveolarPressureChangeRate   =
                           (fN2FinalAlveolarPressure - fN2InitialAlveolarPressure) /
                           fAscentPeriod;        
        fHe2AlveolarPressureChangeRate  =
                           (fHe2FinalAlveolarPressure - fHe2InitialAlveolarPressure) /
                           fAscentPeriod;        
        
       
        // Now parse all tissue compartments
        fMaxMinutes                     =0.0;
        fMinutes                        =0.0;
        iIndex                          =0;
        compartments=diver.getCompartments().elements();
        while (compartments.hasMoreElements())
        {
            compartment=(TissueCompartment)compartments.nextElement();
            
            // prepare information obtained from the compartment
            fN2InitialTension           =compartment.getN2TissueTension().getValue(Pressure.UNITS_BAR); 
            fHe2InitialTension          =compartment.getHe2TissueTension().getValue(Pressure.UNITS_BAR);
            fN2K                        =compartment.getN2K();
            fHe2K                       =compartment.getHe2K();
            
            
            bFound=false;
            fMinutes=Parameters.fMinimumDecoStopTime;
            while (!bFound && fMinutes<24.0*60.0)
            {
                // Calculate the tension after the stay
                fN2TensionAfterStay     = Tools.haldaneEquation(fN2InitialTension,
                        fN2InitialAlveolarPressure,
                        fN2K,
                        fMinutes);
                fHe2TensionAfterStay    = Tools.haldaneEquation(fHe2InitialTension,
                        fHe2InitialAlveolarPressure,
                        fHe2K,
                        fMinutes);
                
                fN2FinalTension         = Tools.schreinerEquation(fN2InitialAlveolarPressure,
                        fN2AlveolarPressureChangeRate,
                        fAscentPeriod,
                        fN2K,
                        fN2TensionAfterStay);
                fHe2FinalTension        = Tools.schreinerEquation(fHe2InitialAlveolarPressure,
                        fHe2AlveolarPressureChangeRate,
                        fAscentPeriod,
                        fHe2K,
                        fHe2TensionAfterStay);
                
                fLimit                  =calculateTissueTensionLimit(iIndex, 
                                                                     finalAmbientPressure, 
                                                                     fN2FinalTension, 
                                                                     fHe2FinalTension);
                
                                // The check whether TissueCompartment tension is within the limit
                // for the proposed depth.
                if (fN2FinalTension+fHe2FinalTension<fLimit)
// More safe: use 
//                if (fN2TensionAfterStay+fHe2TensionAfterStay<fLimit)
                {
                    if (fMinutes>fMaxMinutes)
                    {
                        fMaxMinutes=fMinutes;
                    }
                    bFound=true;
                }
                else
                {                
                    // try a deco stop 1 minute larger
                    fMinutes+=1.0;
                }
                
            }
            // if bFound==true, apparently the decostop was longer than one day
            // something has gone wrong apparently
            if (!bFound)
            {
                throw new CalculationException("Decostop to long");
            }
            iIndex++;
        }

        try
        {
            // Create a segment that represents the stay at the DecoStop
            stayAtStop          =new ConstantDepthSegment(  diveHeight,
                                                            currentDecoStopDepth, 
                                                            fMaxMinutes,
                                                            gasMixture);
             // update segment number and and runtime
            iCurrentSegmentNumber++;
            stayAtStop.setSegmentNumber(iCurrentSegmentNumber);
            stayAtStop.setRunTime(fCurrentRunTime);           
            fCurrentRunTime+=fMaxMinutes;
            
            // Create a segment that represents the ascent to next stop (or the surface)
            ascentToNextStop    =new VaryingDepthSegment(   diveHeight, 
                                                            currentDecoStopDepth,
                                                            nextStopDepth, 
                                                            ascentRate,
                                                            gasMixture);
            // update segment number and and runtime
            iCurrentSegmentNumber++;
            ascentToNextStop.setSegmentNumber(iCurrentSegmentNumber);
            ascentToNextStop.setRunTime(fCurrentRunTime);
            fCurrentRunTime+=fAscentPeriod;
            
            // Update the diver for the stops
            stayAtStop.exposeDiver(diver);
            ascentToNextStop.exposeDiver(diver);
            
            
            currentDecoStopDepth=nextStopDepth;
            
            decoSegments.add(stayAtStop);
            decoSegments.add(ascentToNextStop);
            
       }
       catch(IllegalActionException e)
       {
           throw new CalculationException(e.getMessage());
       }
       
        
        
    }
    
    /**
     *  This method calculates the allowed tissue tension limit based on the 
     *  compartment number, the N2 and He2 tensions and the ambient pressure.
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
        fLimit=ambientPressure.getValue(Pressure.UNITS_BAR)/fB+fA;        
    
        return fLimit;
    }
    
    
    /** 
     *  This method checks whether the Tissue Tension in all compartments
     *  of the Diver remains within Buhlmann limits at given depth
     *  @param depth Depth at which divers safety is checked
     *  @return True if the diver is safe, false if tissue tension of at least one
     *          TissueCompartment is not within limits
     */
    protected boolean checkDiverSafety(Length depth)
    {
        Enumeration             compartments;
        TissueCompartment       compartment;     
        double                  fLimit;
        double                  fHeliumTension;
        double                  fNitrogenTension;
        boolean                 bSafe;
        int                     iIndex;
        
        DepthPressure           ambientPressure;
        
        
        ambientPressure=new DepthPressure(depth, diveHeight);
        bSafe=true;
        
        iIndex=0;
        compartments=diver.getCompartments().elements();
        while (compartments.hasMoreElements() && bSafe)
        {
            compartment=(TissueCompartment)compartments.nextElement();
        
            fHeliumTension      =compartment.getHe2TissueTension().getValue(Pressure.UNITS_BAR);
            fNitrogenTension    =compartment.getN2TissueTension().getValue(Pressure.UNITS_BAR);
            
            fLimit=this.calculateTissueTensionLimit(iIndex, ambientPressure, fNitrogenTension, fHeliumTension);
            
            if (fNitrogenTension+fHeliumTension>fLimit)
            {
                bSafe=false;
            }
            
            iIndex++;
        }
        
        return bSafe;
    }
    
    public void calculateInitialAllowableGradient(Diver diver)
    {
        
    }
    
    /**
     *  This method returns the name or a short description of the algorithm.
     *  @return String identifying the algorithm.
     */
    public String getAlgorithmDescription()
    {
        String sName;
        
        sName="Buhlmann ZH-L16";
        switch (iCurrentModel)
        {
            case ZH_L16A:
                sName+="A";
                break;
            case ZH_L16B:
                sName+="B";
                break;
            case ZH_L16C:
                sName+="C";
                break;
        }
        return sName;
    }
}
