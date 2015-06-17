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
import java.text.MessageFormat;

/**
 * This ExposureSegment represents a stay at (constant) depth.
 */

public class ConstantDepthSegment extends DepthSegment
{
    /*------------------------------------------------------------------------------------------------*\
     * Variables
    \*------------------------------------------------------------------------------------------------*/    

    DepthPressure       depthPressure;

    /*------------------------------------------------------------------------------------------------*\
     * Construction, initialisation and reinitialising
    \*------------------------------------------------------------------------------------------------*/    
    /** Constructor. Initializes the variables
     *  @param          diveHeight      Height at which the dive takes place
     *  @param          depth           Depth of the ConstantDepthSegment
     *  @param          fExposurePeriod Period of exposure
     *  @param          gasMixture      GasMixture used by the diver during this segment.
     */
    public ConstantDepthSegment(Length diveHeight,
                                Length depth,
                                double fExposurePeriod,
                                GasMixture gasMixture)
    {
        this.fExposurePeriod    =fExposurePeriod;
        this.diveDepthAtStart   =(Length)depth.clone();
        this.diveDepthAtEnd     =(Length)depth.clone();
        this.gasMixture         =(GasMixture)gasMixture.clone();

        this.diveHeight         =(Length)diveHeight.clone();
         
        
        initSegment();

    }

    public ConstantDepthSegment(Length diveHeight, 
                                MyXML xmlRepresentation) 
                                throws MyXMLException, IllegalActionException
    {
        this.diveHeight=(Length)diveHeight.clone();
        this.createFromXmlRepresentation(xmlRepresentation);
    }
    
    public void setParameters(  Length depth,
                                double fExposurePeriod,
                                GasMixture gasMixture)
    {
        this.fExposurePeriod    =fExposurePeriod;
        this.diveDepthAtStart.equalsLength(depth);
        this.diveDepthAtEnd.equalsLength(depth);
        this.gasMixture.equalsGasMixture(gasMixture);   
        initSegment();
    }

    /** This method initializes the segment parameters that are not directly 
     *  defined when creating or changing this class.
     */
    protected void initSegment()
    {
        depthPressure                   =new DepthPressure(diveDepthAtStart, diveHeight);
        this.ambientPressureAtStart     =(Pressure)depthPressure.clone();
        this.ambientPressureAtEnd       =(Pressure)ambientPressureAtStart.clone();
        this.ambientPressureChangeRate  =new Pressure(0.0, Pressure.UNITS_BAR);

    }
    
    /*------------------------------------------------------------------------------------------------*\
     * Get information
    \*------------------------------------------------------------------------------------------------*/    
    
    /** This method returns the value of the ambient pressure at the end of the exposure
     *  @return         The ambient pressure at the end of the segment
     */
    public Pressure getAmbientPressureAtEnd()
    {
        return ambientPressureAtStart;
    }    

    /*------------------------------------------------------------------------------------------------*\
     * Calculate
    \*------------------------------------------------------------------------------------------------*/    
    /** Applies the exposure of this segment to a tissue compartment. The
     *  N2 and He2 partial pressures are updated for the given TissueCompartment
     *  @param          compartment The tissue compartment to be updated
     *  @exception      CalculationException
     */
    public void exposeTissueCompartment(TissueCompartment compartment)
                throws CalculationException
    {
        double      fAmbientPressure;
        double      fAlvPressure;
        double      fTissuePressure;
        double      fConstantK;
        double      fFraction;
        Pressure    tissuePressure;

        if (fExposurePeriod<0.0)
        {
            throw new CalculationException("Negative time value");
        }

        fAmbientPressure=ambientPressureAtStart.getValue(Pressure.UNITS_BAR);

        // NITROGEN
        fConstantK=compartment.getN2K();
        fFraction=gasMixture.getN2Fraction();
        tissuePressure=compartment.getN2TissueTension();
        fTissuePressure=tissuePressure.getValue(Pressure.UNITS_BAR);
        fAlvPressure= Tools.alveolarPressure(ambientPressureAtStart, fFraction,
                Pressure.UNITS_BAR);

        // the haldane equation
        fTissuePressure=fAlvPressure+
                        (fTissuePressure-fAlvPressure)*Math.exp(-fExposurePeriod*fConstantK);

        tissuePressure.setValue(fTissuePressure, Pressure.UNITS_BAR);

        // HELIUM
        fConstantK=compartment.getHe2K();
        fFraction=gasMixture.getHe2Fraction();
        tissuePressure=compartment.getHe2TissueTension();
        fTissuePressure=tissuePressure.getValue(Pressure.UNITS_BAR);
        fAlvPressure= Tools.alveolarPressure(ambientPressureAtStart, fFraction,
                Pressure.UNITS_BAR);
        // the haldane equation
        fTissuePressure=fAlvPressure+
                        (fTissuePressure-fAlvPressure)*Math.exp(-fExposurePeriod*fConstantK);

        tissuePressure.setValue(fTissuePressure, Pressure.UNITS_BAR);

    }



    
    /*------------------------------------------------------------------------------------------------*\
     * Printing the dive table
    \*------------------------------------------------------------------------------------------------*/    
    /** Print the segment parameters as an entry in the dive table
     *  @param          writer Output stream writer used to write
     *  @exception      java.io.IOException
     */
    public void printDiveTableEntry(Writer writer) throws IOException
    {
        int             iUnits=Parameters.iPresentationPressureUnits;

        if (Parameters.iPresentationPressureUnits==Pressure.UNITS_FSW)
        {
            iUnits=Length.UNITS_FEET;
        }
        else
        {
            iUnits=Length.UNITS_METER;
        }


        Object[]        args=   {
                                    new Integer(iSegmentNumber),
                                    new Double(fExposurePeriod),
                                    new Double(fRunTimeAtStart+fExposurePeriod),
                                    new Integer(gasMixture.getIndex()),
                                    new Double(diveDepthAtStart.getValue(iUnits))
                                };
        writer.write(MessageFormat.format(Text.sReport13, args));

    }

    /** Print the segment parameters as an entry in the deco table
     *  @param          writer Output stream writer used to write
     *  @exception      java.io.IOException
     */
    public void printDecoTableEntry(Writer writer) throws IOException
    {
        int             iUnits=Parameters.iPresentationPressureUnits;
        double          fStopTime;

        if (Parameters.iPresentationPressureUnits==Pressure.UNITS_FSW)
        {
            iUnits=Length.UNITS_FEET;
        }
        else
        {
            iUnits=Length.UNITS_METER;
        }

        fStopTime=(Math.round(fExposurePeriod/Parameters.fMinimumDecoStopTime+0.5))*
                   Parameters.fMinimumDecoStopTime;

        Object[]        args=   {
                                    new Integer(iSegmentNumber),
                                    new Double(fExposurePeriod),
                                    new Double(fRunTimeAtStart+fExposurePeriod),
                                    new Integer(gasMixture.getIndex()),
                                    new Double(diveDepthAtStart.getValue(iUnits)),
                                    new Double(fStopTime),
                                    new Double(fRunTimeAtStart+fExposurePeriod)
                                };
        writer.write(MessageFormat.format(Text.sReport22, args));

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
        MyXML xmlSegment;
        MyXML xmlDepth;
        
        xmlSegment=new MyXML("StayAtDepth");

        xmlDepth=xmlSegment.addElement("Depth");
        xmlDepth.addElement(diveDepthAtStart.getXmlRepresentation());
        
        xmlSegment.addElement("PeriodInMinutes", Double.toString(this.fExposurePeriod));

        xmlSegment.addElement(gasMixture.getXmlRepresentation());
        
        return xmlSegment;
    }    
    
    /**
     *  This method initialises the parameters characterising the Segment
     *  The values are retrieved from the XML representation.
     *  @xmlRepresentation Representation of the SSegment
     */
    private void createFromXmlRepresentation(MyXML xmlRepresentation) throws MyXMLException, IllegalActionException
    {
        MyXML xmlDepth;
        MyXML xmlGasMix;
        MyXML xmlLength;

        
        xmlDepth=xmlRepresentation.findElement("Depth");
        xmlLength=xmlDepth.findElement("Length");
        diveDepthAtStart=new Length(xmlLength);
        diveDepthAtEnd=new Length(xmlLength);
        
        fExposurePeriod=xmlRepresentation.findElement("PeriodInMinutes").getValueAsDouble();
        
        xmlGasMix=xmlRepresentation.findElement("GasMixture");
        gasMixture=new GasMixture(xmlGasMix);
        
        initSegment();
        
    }    
    
}