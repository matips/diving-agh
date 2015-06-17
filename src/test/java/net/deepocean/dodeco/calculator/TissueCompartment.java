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
 *  This class represents the divers Tissue Compartment. It is characterised
 *  by halftimes for Helium and Nitrogen. It acts as placeholder for a number
 *  of TissueCompartment related variables, like tissue tension, gradients, etc.
 *  It also contains critical Nuclei for Helium and Nitrogen.
 *
 */
public class TissueCompartment
{
    /*------------------------------------------------------------------------------------------------*\
     * Variables
    \*------------------------------------------------------------------------------------------------*/    
    
    public static final int MAX_BACKUP=2;

    private double fHalfTimeHe2;                // halftime in minutes
    private double fHalfTimeN2;
    private double fConstantKHe2;               // k in 1/minutes
    private double fConstantKN2;

    private Pressure partialPressureHe2;        // gas loading in bar
    private Pressure partialPressureN2;
    private Pressure[] partialPressureBackupHe2;// gas loading in bar backup array
    private Pressure[] partialPressureBackupN2;



    private Pressure maxCrushingPressureHe2;    // maximum crushing pressure gradient (used by VPM)
    private Pressure maxCrushingPressureN2;
    private Pressure adjMaxCrushingPressureHe2; // maximum crushing pressure gradient (used by VPM)
    private Pressure adjMaxCrushingPressureN2;
    private Pressure initialAllowableGradientHe2;
    private Pressure initialAllowableGradientN2;
    private Pressure allowableGradientHe2;
    private Pressure allowableGradientN2;
    private Pressure decoGradientHe2;
    private Pressure decoGradientN2;

    private Pressure maxActualGradient;

    private Nucleus criticalNucleusHe2;        // the critical nucleus
    private Nucleus criticalNucleusN2;

    private double   fSurfacePhaseVolumeTime;
    private double   fPhaseVolumeTime;
    private double   fLastPhaseVolumeTime;

    /*------------------------------------------------------------------------------------------------*\
     * Construction, initialisation and reinitialising
    \*------------------------------------------------------------------------------------------------*/    
    /** Constructor, initializes the tissue compartment
     *  @param          fHalfTimeN2 The tissue compartments halftime for Nitrogen
     *  @param          fHalfTimeHe2 The tissue compartments halftime for Helium
     */
    public TissueCompartment(double fHalfTimeN2, double fHalfTimeHe2)
    {
        this.fHalfTimeN2            =fHalfTimeN2;
        this.fConstantKN2           =Math.log(2.0)/fHalfTimeN2;
        this.fHalfTimeHe2           =fHalfTimeHe2;
        this.fConstantKHe2          =Math.log(2.0)/fHalfTimeHe2;


        this.partialPressureN2          =new Pressure(0.0, Pressure.UNITS_BAR);
        this.partialPressureHe2         =new Pressure(0.0, Pressure.UNITS_BAR);

        this.maxCrushingPressureHe2     =new Pressure(0.0, Pressure.UNITS_BAR);
        this.maxCrushingPressureN2      =new Pressure(0.0, Pressure.UNITS_BAR);

        this.adjMaxCrushingPressureHe2  =new Pressure(0.0, Pressure.UNITS_BAR);
        this.adjMaxCrushingPressureN2   =new Pressure(0.0, Pressure.UNITS_BAR);

        this.criticalNucleusHe2         =new Nucleus(new Length(0.0, Length.UNITS_METER));
        this.criticalNucleusN2          =new Nucleus(new Length(0.0, Length.UNITS_METER));

        this.initialAllowableGradientHe2=new Pressure(0.0, Pressure.UNITS_BAR);
        this.initialAllowableGradientN2 =new Pressure(0.0, Pressure.UNITS_BAR);

        this.allowableGradientHe2       =new Pressure(0.0, Pressure.UNITS_BAR);
        this.allowableGradientN2        =new Pressure(0.0, Pressure.UNITS_BAR);

        this.decoGradientHe2            =new Pressure(0.0, Pressure.UNITS_BAR);
        this.decoGradientN2             =new Pressure(0.0, Pressure.UNITS_BAR);

        this.maxActualGradient          =new Pressure(0.0, Pressure.UNITS_BAR);

        this.partialPressureBackupN2    =new Pressure[MAX_BACKUP];
        this.partialPressureBackupHe2   =new Pressure[MAX_BACKUP];


        fPhaseVolumeTime                =0.0;
        fSurfacePhaseVolumeTime         =0.0;
        fLastPhaseVolumeTime            =0.0;
    }


    /*------------------------------------------------------------------------------------------------*\
     * Get information
    \*------------------------------------------------------------------------------------------------*/    
    /** Gets the compartments halftime value for Nitrogen
     *  @return         The halftime in minutes
     */

    public double getN2HalfTime()
    {
        return fHalfTimeN2;
    }
    /** Gets the compartments halftime value for Helium
     *  @return         The halftime in minutes
     */
    public double getHe2HalfTime()
    {
        return fHalfTimeHe2;
    }

    /** Gets the tissue constant k value for Nitrogen
     *  @return         The k value in 1/minute
     */
    public double getN2K()
    {
        return fConstantKN2;
    }

    /** Gets the tissue constant k value for Helium
     *  @return         The k value in 1/minute
     */
    public double getHe2K()
    {
        return fConstantKHe2;
    }

    /** Gets the crushing pressure (maximum gradient between ambient pressure
     *  and tissue tension during the dive) for Nitrogen
     *  @return         The crushing pressure
     */
    public Pressure getN2MaxCrushingPressure()
    {
        return this.maxCrushingPressureN2;
    }

    /** Gets the crushing pressure (maximum gradient between ambient pressure
     *  and tissue tension during the dive) for Helium
     *  @return         The crushing pressure
     */
     public Pressure getHe2MaxCrushingPressure()
    {
        return this.maxCrushingPressureHe2;
    }

    /** Gets the adjusted crushing pressure (maximum gradient between ambient pressure
     *  and tissue tension during the dive) for Nitrogen
     *  @return         The adjusted crushing pressure
     */
    public Pressure getN2AdjMaxCrushingPressure()
    {
        return this.adjMaxCrushingPressureN2;
    }

    /** Gets the adjusted crushing pressure (maximum gradient between ambient pressure
     *  and tissue tension during the dive) for Helium
     *  @return         The adjusted crushing pressure
     */
     public Pressure getHe2AdjMaxCrushingPressure()
    {
        return this.adjMaxCrushingPressureHe2;
    }

    /** Gets the initial critical radius for Helium
     *  @return         The initial critical radius
     */
    public Length getHe2InitialCriticalRadius()
    {
        return criticalNucleusHe2.getInitialCriticalRadius();
    }

    /** Gets the initial critical radius for Nitrogen
     *  @return         The initial critical radius
     */
    public Length getN2InitialCriticalRadius()
    {
        return criticalNucleusN2.getInitialCriticalRadius();
    }


    /** Gets the adjusted critical radius for Nitrogen
     *  @return         The adjusted critical radius
     */
    public Length getN2AdjustedCriticalRadius()
    {
        return criticalNucleusN2.getAdjustedCriticalRadius();
    }

    /** Gets the adjusted critical radius for Helium
     *  @return         The adjusted critical radius
     */
    public Length getHe2AdjustedCriticalRadius()
    {
        return criticalNucleusHe2.getAdjustedCriticalRadius();
    }

    /** Gets the tissue tension for Helium
     *  @return         The tissue tension
     */
    public Pressure getHe2TissueTension()
    {
        return partialPressureHe2;
    }

    /** Gets intial allowable super saturation gradient for Helium
     *  @return         The gradient
     */
    public Pressure getHe2InitialAllowableGradient()
    {
        return initialAllowableGradientHe2;
    }

    /** Gets intial allowable super saturation gradient for Nitrogen
     *  @return         The gradient
     */
    public Pressure getN2InitialAllowableGradient()
    {
        return initialAllowableGradientN2;
    }

    /** Gets allowable super saturation gradient for Helium
     *  @return         The gradient
     */
    public Pressure getHe2AllowableGradient()
    {
        return allowableGradientHe2;
    }

    /** Gets allowable super saturation gradient for Nitrogen
     *  @return         The gradient
     */
    public Pressure getN2AllowableGradient()
    {
        return allowableGradientN2;
    }

    /** Gets allowable super saturation gradient for Helium
     *  @return         The gradient
     */
    public Pressure getHe2DecoGradient()
    {
        return decoGradientHe2;
    }

    /** Gets allowable super saturation gradient for Nitrogen
     *  @return         The gradient
     */
    public Pressure getN2DecoGradient()
    {
        return decoGradientN2;
    }



    /** Returns the regenerated radius of the critical nucleus for Nitrogen
     *  @return         The regenerated radisu
     */
    public Length getN2RegeneratedRadius()
    {
        return criticalNucleusN2.getRegeneratedCriticalRadius();
    }

    /** Returns the regenerated radius of the critical nucleus for Helium
     *  @return         The regenerated radisu
     */
    public Length getHe2RegeneratedRadius()
    {
        return criticalNucleusHe2.getRegeneratedCriticalRadius();
    }

    /**
     *  This method returns the Surface Phase Volume Time
     *  @return The Surface Phase Volume Time
     */
    public double getSurfacePhaseVolumeTime()
    {
        return fSurfacePhaseVolumeTime;
    }

    public void setSurfacePhaseVolumeTime(double fTime)
    {
        fSurfacePhaseVolumeTime=fTime;
    }

    public double getPhaseVolumeTime()
    {
        return fPhaseVolumeTime;
    }

    public void setPhaseVolumeTime(double fTime)
    {
        fPhaseVolumeTime=fTime;
    }

    public double getLastPhaseVolumeTime()
    {
        return fLastPhaseVolumeTime;
    }


    public Pressure getMaxActualGradient()
    {
        return maxActualGradient;
    }

    /** Gets the tissue tension for Nitrogen
     *  @return         The tissue tension
     */
    public Pressure getN2TissueTension()
    {
        return partialPressureN2;
    }
    
    
    /*------------------------------------------------------------------------------------------------*\
     * Set information
    \*------------------------------------------------------------------------------------------------*/    
    /** Sets the compartments initial critical radius for Nitrogen. The actual
     *  critical radius is reset to this value as well.
     *  @param          newRadius The critical radius
     */
    public void setN2InitialCriticalRadius(Length newRadius)
    {
        criticalNucleusN2.setInitialCriticalRadius(newRadius);
    }

    /** Sets the compartments initial critical radius for Helium. The actual
     *  critical radius is reset to this value as well.
     *  @param          newRadius The critical radius
     */
    public void setHe2InitialCriticalRadius(Length newRadius)
    {
        criticalNucleusHe2.setInitialCriticalRadius(newRadius);
    }
    
    
    /** Sets the partial pressure value for this tissue for Helium
     *  @param          newPressureValue The new value for the partial pressure
     */
    public void setHe2TissueTension(Pressure newPressureValue)
    {
        partialPressureHe2=newPressureValue;
    }



    /** Sets the partial pressure value for this tissue for Nitrogen
     *  @param          newPressureValue The new value for the partial pressure
     */
    public void setN2TissueTension(Pressure newPressureValue)
    {
        partialPressureN2=newPressureValue;
    }

    public void setN2CriticalNucleus(Nucleus newNucleus)
    {
        this.criticalNucleusN2=newNucleus;
    }

    public void setHe2CriticalNucleus(Nucleus newNucleus)
    {
        this.criticalNucleusHe2=newNucleus;
    }

    public void setLastPhaseVolumeTime(double fTime)
    {
        fLastPhaseVolumeTime=fTime;
    }

    

    /*------------------------------------------------------------------------------------------------*\
     * Calculate
    \*------------------------------------------------------------------------------------------------*/    
    
    public void resetBeforeDive()
    {
        maxCrushingPressureHe2.setValue(0.0, Pressure.UNITS_BAR);
        maxCrushingPressureN2.setValue(0.0, Pressure.UNITS_BAR);
        maxActualGradient.setValue(0.0, Pressure.UNITS_BAR);
    }

    
    
    /** Regenerate the critical radius of nuclei (Helium and Nitrogen) for the
     *  period defined
     *  @param          fRegenerationPeriod Period of regeneration
     */
    public void regenerateNuclei(double fRegenerationPeriod)
    {
        criticalNucleusHe2.regenerate(fRegenerationPeriod,
                                      maxCrushingPressureHe2,
                                      adjMaxCrushingPressureHe2);
        criticalNucleusN2.regenerate(fRegenerationPeriod,
                                      maxCrushingPressureN2,
                                      adjMaxCrushingPressureN2);
    }

    /**
     *  When the diver acclimatises at hight, this method updates the critical
     *  radii.
     *  @param fExposurePeriod The stay period at height
     *  @param ambientPressure The ambient atmospheric pressure
     */
    public void updateNucleiAtHeight(double fExposurePeriod, Pressure ambientPressure)
    {
        double fCompartmentGradientInPascal;
        double fCompartmentGradientInBar;
        
        fCompartmentGradientInBar=  partialPressureN2.getValue(Pressure.UNITS_BAR)+
                                    Parameters.pressureOtherGasses.getValue(Pressure.UNITS_BAR)-
                                    ambientPressure.getValue(Pressure.UNITS_BAR);
        
        fCompartmentGradientInPascal= Pressure.convertPressure(fCompartmentGradientInBar, Pressure.UNITS_BAR, Pressure.UNITS_PASCAL);
        
        criticalNucleusHe2.updateNucleusAtHeight(fCompartmentGradientInPascal, fExposurePeriod);
        criticalNucleusN2.updateNucleusAtHeight(fCompartmentGradientInPascal, fExposurePeriod);
    }

    /*------------------------------------------------------------------------------------------------*\
     * Backup and restore
    \*------------------------------------------------------------------------------------------------*/    
    public void backupTissueTension(int iBackupArrayIndex)
    {
        if ((iBackupArrayIndex>=0) && (iBackupArrayIndex<MAX_BACKUP))
        {
            partialPressureBackupHe2[iBackupArrayIndex]=
                (Pressure)partialPressureHe2.clone();
            partialPressureBackupN2 [iBackupArrayIndex]=
                (Pressure)partialPressureN2.clone();
        }
    }
    
    
    public void restoreTissueTension(int iBackupArrayIndex)
    {
        if ((iBackupArrayIndex>=0) && (iBackupArrayIndex<MAX_BACKUP))
        {
            partialPressureHe2=
                (Pressure)partialPressureBackupHe2[iBackupArrayIndex].clone();
            partialPressureN2=
                (Pressure)partialPressureBackupN2[iBackupArrayIndex].clone();
        }
    }


    /*------------------------------------------------------------------------------------------------*\
     * Cloning
    \*------------------------------------------------------------------------------------------------*/    
    public Object clone()
    {
        TissueCompartment newCompartment;

        newCompartment=new TissueCompartment(fHalfTimeN2, fHalfTimeHe2);

        newCompartment.setHe2TissueTension((Pressure)this.partialPressureHe2.clone());
        newCompartment.setN2TissueTension((Pressure)this.partialPressureN2.clone());

        newCompartment.getHe2MaxCrushingPressure().setValue(this.maxCrushingPressureHe2);
        newCompartment.getN2MaxCrushingPressure().setValue(this.maxCrushingPressureN2);

        newCompartment.getHe2AllowableGradient().setValue(this.allowableGradientHe2);
        newCompartment.getN2AllowableGradient().setValue(this.allowableGradientN2);

        newCompartment.getHe2DecoGradient().setValue(this.decoGradientHe2);
        newCompartment.getN2DecoGradient().setValue(this.decoGradientN2);

        newCompartment.getHe2InitialAllowableGradient().setValue(this.initialAllowableGradientHe2);
        newCompartment.getN2InitialAllowableGradient().setValue(this.initialAllowableGradientN2);

        newCompartment.getHe2AdjMaxCrushingPressure().setValue(this.adjMaxCrushingPressureHe2);
        newCompartment.getN2AdjMaxCrushingPressure().setValue(this.adjMaxCrushingPressureN2);

        newCompartment.setPhaseVolumeTime(fPhaseVolumeTime);
        newCompartment.setSurfacePhaseVolumeTime(fSurfacePhaseVolumeTime);
        newCompartment.setLastPhaseVolumeTime(fLastPhaseVolumeTime);

        return newCompartment;
    }

}