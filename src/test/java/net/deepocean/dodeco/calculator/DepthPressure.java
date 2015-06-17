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
 * This class represents the ambient pressure at a given water depth.
 * It consists of a hydrosatic pressure component and an 
 * atmospheric pressure component.
 */

public class DepthPressure extends Pressure
{
    /*------------------------------------------------------------------------------------------------*\
     * Variables
    \*------------------------------------------------------------------------------------------------*/    
    private static final double BARS_PER_MSW=0.1;
    private Length diveHeight;
    private Length diveDepth;

    private AtmosphericPressure atmosphericPressure;

    /*------------------------------------------------------------------------------------------------*\
     * Construction, initialisation and reinitialising
    \*------------------------------------------------------------------------------------------------*/    
    /**
     *  Constructor. Initialises the 
     *  @param          depth Depth at which the pressure is calculated
     *  @param          diveHeight Height of 'SeaLevel'
     */
    public DepthPressure(Length depth, Length diveHeight)
    {
        super(0.0, UNITS_BAR);                              // create pressure

        this.diveHeight=diveHeight;
        this.diveDepth =depth;

        atmosphericPressure=new AtmosphericPressure(diveHeight);
        setDepth(depth);                                    // set value
    }

    /**
     *  This method sets the depth of the DepthPressure.
     *  @param          depth New depth.
     */
    public void setDepth(Length depth)
    {
        double fDepth;

        this.diveDepth=depth;
        fDepth=depth.getValue(Length.UNITS_METER);
        setValue(fDepth*BARS_PER_MSW, Pressure.UNITS_BAR);
        addPressure(atmosphericPressure);
    }

    /*------------------------------------------------------------------------------------------------*\
     * Get information
    \*------------------------------------------------------------------------------------------------*/    
    
    /**
     *  This method returns the height of the water surface. 
     *  @return         The height as Length instance.
     */
    public Length getDiveHeight()
    {
        return diveHeight;
    }

    /**
     *  This method returns the depth of the DepthPressure.
     *  @return         The depth as Length instance
     */
    public Length getDepth()
    {
        return diveDepth;
    }

    /*------------------------------------------------------------------------------------------------*\
     * Calculate
    \*------------------------------------------------------------------------------------------------*/    
    /**
     *  This static method converts a depth change to a pressure change.
     *  @param          depthChange The depthChange
     *  @return         Pressure change as instance of Pressure.
     *  @exception      -
     */
    public static Pressure convertDepthChangeToPressureChange(Length depthChange)
    {
        double fDepthChange;

        fDepthChange=depthChange.getValue(Length.UNITS_METER);
        return new Pressure(fDepthChange*BARS_PER_MSW, UNITS_BAR);
    }

    /**
     *  This static method converts an ambient pressure to a depth at which this 
     *  pressure equals the ambient pressure.
     *  @param          ambientPressure The ambient pressure
     *  @param          diveHeight The height of the water surface
     *  @return         The depth at which the given ambient pressure is excerted
     */
    public static Length convertPressureToDepth(Pressure ambientPressure, Length diveHeight)
    {
        AtmosphericPressure atmPressure;
        double fAmbPressure;
        double fAtmPressure;

        atmPressure=new AtmosphericPressure(diveHeight);
        fAtmPressure=atmPressure.getValue(Pressure.UNITS_BAR);
        fAmbPressure=ambientPressure.getValue(Pressure.UNITS_BAR);


        return new Length((fAmbPressure-fAtmPressure)/BARS_PER_MSW, Length.UNITS_METER);
    }
}