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

import java.util.Enumeration;
import java.util.Vector;

/**
 * Class        : SurfaceInterval
 * Package      : net.deepocean.vpm.calculator
 * Description  : This class represents a surface interval exposure of the
 *                diver. The diver stays at constant height during the
 *                surface interval.
 * Exceptions   :
 *
 * @author        B.J. van der Velde
 * @version       1.0
 *
 *
 */

public class SurfaceInterval extends Exposure
{
    /** The exposure segment corresponding to the surface interval */
    private ConstantHeightSegment   surfaceSegment;
    
   

    /** Constuctor. Initializes the surface interval at the indicated altitude
     *  @param          heightAboveSeaLevel The height at which the SurfaceInterval takes place
     *  @param          gasMixture          The saturation gas mixture
     *  @param          fIntervalPeriod     The duration of the surface interval
     *                  in minutes.
     */
    public SurfaceInterval( Length heightAboveSeaLevel, 
                            GasMixture gasMixture,
                            double fIntervalPeriod)
    {
        surfaceSegment=new ConstantHeightSegment(heightAboveSeaLevel,
                                                 fIntervalPeriod,
                                                 gasMixture);
    }
    
    /** Constuctor. Initialises the SurfaceInterval using the representation in XML
     *  @param          xmlRepresentation XML Representation of the SurfaceInterval
     */
    public SurfaceInterval(MyXML xmlRepresentation)
                           throws MyXMLException, IllegalActionException
    {
        this.createFromXmlRepresentation(xmlRepresentation);
    }
    
    /**
     *  This method sets the parameters that characterise the SurfaceInterval
     *  @param          heightAboveSeaLevel The saturation height
     *  @param          gasMixture          The saturation gas mixture
     *  @param          fIntervalPeriod     The duration of the surface interval
     *                  in minutes.
     */
    public void setParameters(  Length heightAboveSeaLevel, 
                                GasMixture gasMixture,
                                double fIntervalPeriod)
    {
        surfaceSegment.setParameters(heightAboveSeaLevel, fIntervalPeriod, gasMixture);
    }
    

    /** Updates the diver for this exposure
     *  @param          diver The diver
     *  @param          fRunTime The runtime at the start of the exposure
     *  @exception      CalculationException
     */
    public void exposeDiver(Diver diver, double fRunTime) throws CalculationException
    {
        surfaceSegment.setRunTime(fRunTime);
        surfaceSegment.exposeDiver(diver);
        vpmRepetitiveAlgorithm(diver);
    }

/* ===============================================================================  */
/*     SUBROUTINE VPM_REPETITIVE_ALGORITHM                                          */
/*     Purpose: This subprogram implements the VPM Repetitive Algorithm that was    */
/*     envisioned by Professor David E. Yount only months before his passing.       */
/* ===============================================================================  */
    /**
     *  The VPM Repetitive Algorithm
     *  @param Diver The Diver that is being exposed
     */
    private void vpmRepetitiveAlgorithm(Diver diver)
    {
        /* Local variables */
        double              max_actual_gradient_pascals,
                            initial_allowable_grad_n2_pa,
                            initial_allowable_grad_he_pa,

                            adj_crush_pressure_n2_pascals,
                            new_critical_radius_n2,
                            adj_crush_pressure_he_pascals,
                            new_critical_radius_he;

        TissueCompartment   compartment;
        Vector              compartments;
        Enumeration         elements;
        
        Length              adjustedCriticalRadiusN2;
        Length              initialCriticalRadiusN2;
        Length              adjustedCriticalRadiusHe2;
        Length              initialCriticalRadiusHe2;
        
        Double              fSurfaceIntervalPeriod;
        
        fSurfaceIntervalPeriod=surfaceSegment.getExposurePeriod();

/* loop */
/* =============================================================================== */
/*     CALCULATIONS */
/* =============================================================================== */

        compartments=diver.getCompartments();
        elements=compartments.elements();
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            
            max_actual_gradient_pascals =
                compartment.getMaxActualGradient().getValue(Pressure.UNITS_PASCAL);

            // NITROGEN
            adj_crush_pressure_n2_pascals =
                compartment.getN2AdjMaxCrushingPressure().getValue(Pressure.UNITS_PASCAL);
            initial_allowable_grad_n2_pa =
                compartment.getN2InitialAllowableGradient().getValue(Pressure.UNITS_PASCAL);

            adjustedCriticalRadiusN2=compartment.getN2AdjustedCriticalRadius();
            initialCriticalRadiusN2 =compartment.getN2InitialCriticalRadius();
            if (max_actual_gradient_pascals > initial_allowable_grad_n2_pa)
            {
                new_critical_radius_n2 =
                    Parameters.fGamma * 2.0 *
                    (Parameters.fGammaC - Parameters.fGamma) /
                    (max_actual_gradient_pascals * Parameters.fGammaC -
                        Parameters.fGamma * adj_crush_pressure_n2_pascals);
                adjustedCriticalRadiusN2.setValue(
                    initialCriticalRadiusN2.getValue(Length.UNITS_METER) +
                    (initialCriticalRadiusN2.getValue(Length.UNITS_METER) -
                        new_critical_radius_n2) *  Math.exp(-fSurfaceIntervalPeriod /
                        Parameters.fRegenTimeConstant), Length.UNITS_METER);

            }
            else
            {
                adjustedCriticalRadiusN2.setValue(initialCriticalRadiusN2);
            }

            // HELIUM
            adj_crush_pressure_he_pascals =
                compartment.getHe2AdjMaxCrushingPressure().getValue(Pressure.UNITS_PASCAL);
            initial_allowable_grad_he_pa =
                compartment.getHe2InitialAllowableGradient().getValue(Pressure.UNITS_PASCAL);

            adjustedCriticalRadiusHe2=compartment.getHe2AdjustedCriticalRadius();
            initialCriticalRadiusHe2 =compartment.getHe2InitialCriticalRadius();
            if (max_actual_gradient_pascals > initial_allowable_grad_he_pa)
            {
                new_critical_radius_he =
                    Parameters.fGamma * 2.0 *
                    (Parameters.fGammaC - Parameters.fGamma) /
                    (max_actual_gradient_pascals * Parameters.fGammaC -
                        Parameters.fGamma * adj_crush_pressure_he_pascals);
                adjustedCriticalRadiusHe2.setValue(
                    initialCriticalRadiusHe2.getValue(Length.UNITS_METER) +
                    (initialCriticalRadiusHe2.getValue(Length.UNITS_METER) -
                        new_critical_radius_he) *  Math.exp(-fSurfaceIntervalPeriod /
                        Parameters.fRegenTimeConstant), Length.UNITS_METER);

            }
            else
            {
                adjustedCriticalRadiusHe2.setValue(initialCriticalRadiusHe2);
            }

        }
    }

    /**
     *  This method returns the Height at which the SurfaceInterval takes place
     *  @return The height of the SurfaceInterval
     */
    public Length getIntervalHeight()
    {
        return surfaceSegment.getHeightAtStart();
    }
    
    /**
     *  This method returns the SurfaceInterval period in minutes
     *  @return The period in minutes.
     */
    public double getIntervalPeriod()
    {
        return surfaceSegment.getExposurePeriod();
    }
    
    
    /**
     *  This method returns the GasMixture that is used during the SurfaceInterval
     *  @return The GasMixture
     */
    public GasMixture getGasMixture()
    {
        return surfaceSegment.getGasMixture();
    }
    
    /**
     *  This method returns the name of the exposure
     *  @return String indicating the name of the exposure
     */
    public String getExposureName()
    {
        return new String("Surface Interval");
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
        MyXML xmlSurfaceInterval;
        
        xmlSurfaceInterval=new MyXML("SurfaceInterval");

        xmlSurfaceInterval.addElement(surfaceSegment.getXmlRepresentation());    
        
       
        return xmlSurfaceInterval;
    }    
    
    /**
     *  This method initialises the parameters characterising the Segment
     *  The values are retrieved from the XML representation.
     *  @xmlRepresentation Representation of the SSegment
     */
    private void createFromXmlRepresentation(MyXML xmlRepresentation) throws MyXMLException, IllegalActionException
    {
        MyXML xmlSegment;

        
        xmlSegment=xmlRepresentation.findElement("StayAtHeight");
        this.surfaceSegment=new ConstantHeightSegment(xmlSegment);
  
    }       
        
   
}