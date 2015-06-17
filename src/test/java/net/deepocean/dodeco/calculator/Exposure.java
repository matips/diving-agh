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

import java.io.IOException;
import java.io.Writer;


/**
 * This class represents an exposure to which the Diver is exposed. It is a
 * superclass for a number of specific exposures.
 *
 */
public abstract class Exposure
{

    protected double fRunTime;

    /** Constructor. Initializes the exposure
     */
    public Exposure()
    {
    }

    /**
     *  This method resets the state of the Exposure, so that 
     *  it can be (re)used for exposing a Diver to it
     */
    public void resetExposure()
    {
    }
    
    /** Updates the diver for this Exposure. The Exposure may be change state or be
     *  modified by this method. Use resetExposure() to reset the Exposure. 
     *  @param          diver The diver
     *  @exception CalculationException
     */
    public abstract void exposeDiver(Diver diver, double fRunTime) throws CalculationException;

    public void printExposure(Writer writer) throws IOException
    {
    }

    public double getRunTime()
    {
        return fRunTime;
    }



    public String getExposureName()
    {
        return new String("-");
    }
    
    /**
     *  This method creates and adds an XML representation of the Exposure
     *  @return The MyXML instance representing the GasMixture
     */
    public MyXML getXmlRepresentation() throws MyXMLException
    {
        MyXML element;
        
        element=new MyXML("ExposureSegment");
        element.addElement("Empty", "Empty");
        
        return element;
    }
}