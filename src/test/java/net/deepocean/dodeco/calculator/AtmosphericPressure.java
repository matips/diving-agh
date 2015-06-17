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
 * This class represents the atmospheric Pressure. The pressure
 * is specified by the height above sealevel
 */
public class AtmosphericPressure extends Pressure
{
    /*------------------------------------------------------------------------------------------------*\
     * Variables
    \*------------------------------------------------------------------------------------------------*/    

    private static final double HEIGHT_MIN=0.0;             // Height of sea level in meters
    private static final double HEIGHT_MAX=9144.0;          // Height of Mt Everest in meters

    private double              fHeight;                    // Height in meters above sealevel

    /*------------------------------------------------------------------------------------------------*\
     * Construction, initialisation and reinitialising
    \*------------------------------------------------------------------------------------------------*/    
    /**
     *  Constructor. The height above sealevel is passed as parameter.
     *  The height can be redefined using setHeight().
     *  The model was copied from the Erik C. Baker VPM Fortran program.
     *  @param heightAboveSeaLevel The height above sealevel
     */
    public AtmosphericPressure(Length heightAboveSeaLevel) throws IllegalStateException
    {
        super(0.0, UNITS_BAR);                              // create pressure

        setHeight(heightAboveSeaLevel);                      // set value

    }

    /*------------------------------------------------------------------------------------------------*\
     * Calculate
    \*------------------------------------------------------------------------------------------------*/    
   /**
     *  This method sets the height an calculates the ambient pressure
     *  at this height
     *  @param heightAboveSeaLevel The new height above sealevel
     */
    public void setHeight(Length heightAboveSeaLevel)
    {
        /* Local variables */
        double  fAltitude,
                molecular_weight_of_air,
	        acceleration_of_operation,
	        temp_gradient,
                temp_at_sea_level,
                pressure_at_sea_level,
	        geopotential_altitude,
                gmr_factor,
	        temp_at_geopotential_altitude,
                gas_constant_r,
	        radius_of_earth,
                fBarometricPressure;

        fHeight=heightAboveSeaLevel.getValue(Length.UNITS_METER);

        if ((fHeight<HEIGHT_MIN) ||
            (fHeight>HEIGHT_MAX))
        {
            throw new IllegalStateException();
        }
        else
        {

/* ===============================================================================  */
/*     CALCULATIONS                                                                 */
/*     If the units for presentation are FSW pressure at sea level is assumed to be */
/*     101325 Pa. If the units are MSW this pressure is assumed to be 100000 Pa.    */
/* ===============================================================================  */
            radius_of_earth             = 6369.0e3;  /* meters */
            acceleration_of_operation   = 9.80665;   /* meters/s2 */
            molecular_weight_of_air     = 28.9644;
            gas_constant_r              = 8.31432;   /* Joules/mol*de */
            temp_at_sea_level           = 288.15;    /* degree */

            if (Parameters.iPresentationPressureUnits==Pressure.UNITS_FSW)
            {
                pressure_at_sea_level       = Pressure.convertPressure(33.0,
                                                                       Pressure.UNITS_FSW,
                                                                       Pressure.UNITS_MSW);
            }                                      /* feet of seawater based on 101325 Pa   */
            else                                   /* at sea level (Standard Atmosphere)    */
            {
                pressure_at_sea_level       = 10.0; /* msw at sea level (European)           */
            }                                       /* meters of seawater based on 100000 pa */



            temp_gradient                   = -0.0065;  /* change in geopotential a */
                                                        /* valid for first layer of at */
                                                        /* up to 11 kilometers or 36, */
                                                        /* Change in Temp Kelvin/meter */
            gmr_factor =
                acceleration_of_operation*molecular_weight_of_air/gas_constant_r;

            fAltitude = heightAboveSeaLevel.getValue(Length.UNITS_METER);           // in meter

            geopotential_altitude =
                fAltitude * radius_of_earth / (fAltitude + radius_of_earth);        // in meter
            temp_at_geopotential_altitude =
                temp_at_sea_level + temp_gradient * geopotential_altitude;          // in Kelvin
            fBarometricPressure =
                pressure_at_sea_level *
                Math.exp(Math.log(temp_at_sea_level / temp_at_geopotential_altitude)
                * gmr_factor / temp_gradient/1000.0);                               // in msw


            setValue(fBarometricPressure, UNITS_MSW);
        }
    }

    /**
     *  This method clones the atmospheric pressure.
     *  @param pressure The pressure to clone
     *  @return The clone of the pressure
     */
    public AtmosphericPressure clone(AtmosphericPressure pressure)
    {
        return new AtmosphericPressure(new Length(fHeight, Length.UNITS_METER));
    }
    /*------------------------------------------------------------------------------------------------*\
     * Get information
    \*------------------------------------------------------------------------------------------------*/    
    

}