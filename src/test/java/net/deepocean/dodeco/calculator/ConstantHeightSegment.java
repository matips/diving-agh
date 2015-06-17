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

/**
 *   This class represents an surface segment. During the stay
 *   at this segment the height remains constant.
 */

public class ConstantHeightSegment extends HeightSegment
{
    /*------------------------------------------------------------------------------------------------*\
     * Variables
    \*------------------------------------------------------------------------------------------------*/    
    AtmosphericPressure atmosphericPressure;

    /*------------------------------------------------------------------------------------------------*\
     * Construction, initialisation and reinitialising
    \*------------------------------------------------------------------------------------------------*/    
    /** 
     *  The constructor. It initialises the ConstantHeightSegment. Paramters that
     *  characterises the segment are passed.
     *  @param height Height above sealevel at which the exposure takes place
     *  @param fExposurePeriod The exposure period in minutes
     *  @param gasMixture The GasMixture the diver uses during the exposure (most likely air)
     */
    public ConstantHeightSegment(Length height,
                                 double fExposurePeriod,
                                 GasMixture gasMixture)
    {
        this.fExposurePeriod    =fExposurePeriod;
        this.heightAtStart      =(Length)height.clone();
        this.heightAtEnd        =(Length)height.clone();
        this.gasMixture         =gasMixture;
        
        this.ambientPressureAtStart     =new AtmosphericPressure(heightAtStart);
        this.ambientPressureAtEnd       =new AtmosphericPressure(heightAtEnd);
        this.ambientPressureChangeRate  =new Pressure(0.0, Pressure.UNITS_BAR);

        initSegment();
    }
    
    /** 
     *  The constructor. It initialises the ConstantHeightSegment. Paramters that
     *  characterises the segment are passed in the form of an XML representation.
     *  @param xmlRepresentation Representation of the ConstantHeightSegment in XML
     */    
    public ConstantHeightSegment(MyXML xmlRepresentation) throws MyXMLException, IllegalActionException
    {
        this.createFromXmlRepresentation(xmlRepresentation);
        
        this.ambientPressureAtStart     =new AtmosphericPressure(heightAtStart);
        this.ambientPressureAtEnd       =new AtmosphericPressure(heightAtEnd);
        this.ambientPressureChangeRate  =new Pressure(0.0, Pressure.UNITS_BAR);

        initSegment();
    }

    /** Initialises the segment parameters that are not directly defined during
     *  creation or reinitialisation of this ExposureSegment
     */
    protected void initSegment()
    {
        ((AtmosphericPressure)ambientPressureAtStart).setHeight(heightAtStart);
        ((AtmosphericPressure)ambientPressureAtEnd).setHeight(heightAtEnd);
    }
    
    /**
     *  This method sets the parameters that characterise the ConstantHeightSegment.
     *  It reinitialises the segment.
     *  @param height Height above sealevel at which the exposure takes place
     *  @param fExposurePeriod The exposure period in minutes
     *  @param gasMixture The GasMixture the diver uses during the exposure (most likely air)
     */
    public void setParameters(Length height, double fExposurePeriod, GasMixture gasMixture)
    {
        this.heightAtStart.equalsLength(height);
        this.heightAtEnd.equalsLength(height);
        this.fExposurePeriod=fExposurePeriod;
        this.gasMixture.equalsGasMixture(gasMixture);
        initSegment();
    }
    

    /*------------------------------------------------------------------------------------------------*\
     * Calculate
    \*------------------------------------------------------------------------------------------------*/    
    /** Applies the exposure of this segment to a tissue compartment
     *  @param          compartment The tissue compartment to be updated
     *  @exception      CalculationException when a calculation error occurred
     */
    public void exposeTissueCompartment(TissueCompartment compartment)
                throws CalculationException
    {
        double      fAmbientPressure;
        double      fAlvPressure;
        double      fTissuePressure;
        double      fConstantK;
        double      fFraction;
        Pressure tissuePressure;

        if (fExposurePeriod<0.0)
        {
            throw new CalculationException("Negative time value");
        }

        fAmbientPressure=ambientPressureAtStart.getValue(Pressure.UNITS_BAR);

        // NITROGEN
        fConstantK      =compartment.getN2K();
        fFraction       =gasMixture.getN2Fraction();
        tissuePressure  =compartment.getN2TissueTension();
        fTissuePressure =tissuePressure.getValue(Pressure.UNITS_BAR);
        fAlvPressure    = Tools.alveolarPressure(ambientPressureAtStart, fFraction,
                Pressure.UNITS_BAR);

        // the haldane equation
        fTissuePressure = Tools.haldaneEquation(fTissuePressure, fAlvPressure,
                fConstantK, fExposurePeriod);



        tissuePressure.setValue(fTissuePressure, Pressure.UNITS_BAR);

        // HELIUM
        fConstantK      =compartment.getHe2K();
        fFraction       =gasMixture.getHe2Fraction();
        tissuePressure  =compartment.getHe2TissueTension();
        fTissuePressure =tissuePressure.getValue(Pressure.UNITS_BAR);
        fAlvPressure    = Tools.alveolarPressure(ambientPressureAtStart, fFraction,
                Pressure.UNITS_BAR);

        // the haldane equation
        fTissuePressure = Tools.haldaneEquation(fTissuePressure, fAlvPressure,
                fConstantK, fExposurePeriod);

        tissuePressure.setValue(fTissuePressure, Pressure.UNITS_BAR);

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
        MyXML xmlHeight;
        MyXML xmlLength;
        
        xmlSegment=new MyXML("StayAtHeight");

        xmlHeight=xmlSegment.addElement("Height");
        xmlHeight.addElement(heightAtStart.getXmlRepresentation());
 
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
        MyXML xmlHeight;
        MyXML xmlPeriod;
        MyXML xmlGasMix;
        MyXML xmlLength;

        
        xmlHeight=xmlRepresentation.findElement("Height");
        xmlLength=xmlHeight.findElement("Length");
        this.heightAtStart=new Length(xmlLength);
        this.heightAtEnd=new Length(xmlLength);

       
        xmlPeriod=xmlRepresentation.findElement("PeriodInMinutes");
        this.fExposurePeriod=xmlPeriod.getValueAsDouble();
                
        xmlGasMix=xmlRepresentation.findElement("GasMixture");
        gasMixture=new GasMixture(xmlGasMix);
        
    } 
 
}