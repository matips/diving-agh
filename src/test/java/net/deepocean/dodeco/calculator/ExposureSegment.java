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
 * This class represents an exposure segment. An exposure
 * segment is a period of constant or linear varying ambient
 * pressure, whether due to height or depth. The exposure is
 * applied to the diver
 */
public class ExposureSegment
{
    /*------------------------------------------------------------------------------------------------*\
     * Variables
    \*------------------------------------------------------------------------------------------------*/    
    
    protected Pressure ambientPressureAtStart;
    protected Pressure ambientPressureAtEnd;
    protected Pressure ambientPressureChangeRate;

    protected double      fExposurePeriod;
    protected double      fRunTimeAtStart;
    protected GasMixture gasMixture;
    protected int         iSegmentNumber;

    /*------------------------------------------------------------------------------------------------*\
     * Construction, initialisation and reinitialising
    \*------------------------------------------------------------------------------------------------*/    
    
    /** Constructor. Initializes values
     */
    public ExposureSegment()
    {
//        diveHeight=new Length(0.0, Length.UNITS_METER);
        ambientPressureAtStart      =null;
        ambientPressureAtEnd        =null;
        ambientPressureChangeRate   =null;
        fExposurePeriod             =0.0;
        fRunTimeAtStart             =0.0;
        gasMixture                  =null;
        iSegmentNumber              =0;
    }

    /** Initializes the segment parameters
     */
    protected void initSegment()
    {
    }

    /**
     *  This method sets the runtime at start of the ExposureSegment
     *  @param fRunTime The runtime at start of the ExposureSegment in minutes.
     */
    public void setRunTime(double fRunTime)
    {
        fRunTimeAtStart=fRunTime;
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
        return ambientPressureAtEnd;
    }

    /** Gets the value of the ambient pressure at the start of the exposure
     *  segment
     *  @return         The ambient pressure at the start of the segment
     */
    public Pressure getAmbientPressureAtStart()
    {
        return ambientPressureAtStart;
    }

    /** Gets the value of the exposure period
     *  @return         The exposure period in minutes
     */
    public double getExposurePeriod()
    {
        return fExposurePeriod;
    }
    
    public double getRunTime()
    {
        return fRunTimeAtStart;
    }

    public void setSegmentNumber(int iNumber)
    {
        this.iSegmentNumber=iNumber;
    }

    public int getSegmentNumber()
    {
        return iSegmentNumber;
    }
    
    public GasMixture getGasMixture()
    {
        return gasMixture;
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
    }

    /** Applies the exposure of this segment to a tissue compartments of a diver
     *  @param          diver Diver to be updated
     *  @exception CalculationException
     */
    public void exposeDiver(Diver diver)
                throws CalculationException
    {
        Vector              compartments;
        Enumeration         elements;
        TissueCompartment compartment;


        compartments=diver.getCompartments();
        elements=compartments.elements();

        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            exposeTissueCompartment(compartment);
        }
    }



    /** Calculates the crushing pressure and updates the tissue compartment
     *  @param          compartment The tissue compartment to be updated
     *  @exception CalculationException
     */
    public void calculateCrushingPressure(TissueCompartment compartment)
                throws CalculationException
    {
    }


    /*------------------------------------------------------------------------------------------------*\
     * XML parsing and writing
    \*------------------------------------------------------------------------------------------------*/
    /**
     *  This method creates an XML representation of the ExposureSegment
     *  @return The MyXML instance representing representing the ExposureSegment
     */
    public MyXML getXmlRepresentation() throws MyXMLException
    {
        MyXML element;
        
        element=new MyXML("Exposure");
        element.addElement("Empty", "Empty");
        
        return element;
    }    
}