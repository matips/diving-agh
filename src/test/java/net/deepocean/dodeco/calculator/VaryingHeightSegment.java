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
 *  This class represents an surface segment. During the stay
 *  at this segment the height varies at constant rate
 */
public class VaryingHeightSegment extends HeightSegment
{
    /*------------------------------------------------------------------------------------------------*\
     * Variables
    \*------------------------------------------------------------------------------------------------*/    

    Length  heightChangeRate;

    private Pressure initialHe2Pressure;
    private Pressure initialN2Pressure;

    /*------------------------------------------------------------------------------------------------*\
     * Construction, initialisation and reinitialising
    \*------------------------------------------------------------------------------------------------*/    
    /** Constructor. Initializes the variables
     *  @param          startHeight Height at start of segment
     *  @param          endHeight   Height at end of segment
     *  @param          gasMixture  Gas mixture during segment (usually air)
     *  @param          fExposurePeriod Period of exposure
     */
    public VaryingHeightSegment(  Length startHeight,
                                  Length endHeight,
                                  double fExposurePeriod,
                                  GasMixture gasMixture)
                                  throws IllegalActionException

    {
        this.heightAtStart              =startHeight;
        this.heightAtEnd                =endHeight;
        this.fExposurePeriod            =fExposurePeriod;
        this.gasMixture                 =gasMixture;
        
        heightChangeRate                =(Length)endHeight.clone();
        heightChangeRate.substractLength(startHeight);
        heightChangeRate.multiplyLength(1.0/fExposurePeriod);
        

        this.ambientPressureAtStart     =new AtmosphericPressure(heightAtStart);
        this.ambientPressureAtEnd       =new AtmosphericPressure(heightAtEnd  );
        this.ambientPressureChangeRate  =new Pressure(0.0, Pressure.UNITS_BAR);
        initSegment();

    }


    /** Constructor. Initializes the variables based on an XML representation
     *  of the VaryingHeightSegment
     *  @param  xmlRepresentation Representation in XML of this segment.
     */
    public VaryingHeightSegment(  MyXML xmlRepresentation)
                                  throws MyXMLException, IllegalActionException

    {
        this.createFromXmlRepresentation(xmlRepresentation);
        
        heightChangeRate                =(Length)heightAtEnd.clone();
        heightChangeRate.substractLength(heightAtStart);
        heightChangeRate.multiplyLength(1.0/fExposurePeriod);
        

        this.ambientPressureAtStart     =new AtmosphericPressure(heightAtStart);
        this.ambientPressureAtEnd       =new AtmosphericPressure(heightAtEnd  );
        this.ambientPressureChangeRate  =new Pressure(0.0, Pressure.UNITS_BAR);
        initSegment();

    }
    
    /** 
     *  This method sets the parameters that characterises the VaryingHeightSegment.
     *  It reinitialises the segment.
     *  @param          startHeight Height at start of segment
     *  @param          endHeight   Height at end of segment
     *  @param          gasMixture  Gas mixture during segment (usually air)
     *  @param          fExposurePeriod Period of exposure
     */  
    public void setParameters(    Length startHeight,
                                  Length endHeight,
                                  double fExposurePeriod,
                                  GasMixture gasMixture)
    {
        this.heightAtStart.equalsLength(startHeight);
        this.heightAtEnd.equalsLength(endHeight);
        this.fExposurePeriod    =fExposurePeriod;
        this.gasMixture.equalsGasMixture(gasMixture);
        
        initSegment();
    }
    
    
    /** Initializes the (parent) segment parameters
     */
    protected void initSegment()
    {
        heightChangeRate.equalsLength(heightAtEnd);
        heightChangeRate.substractLength(heightAtStart);
        heightChangeRate.multiplyLength(1.0/fExposurePeriod);
       
        ((AtmosphericPressure)ambientPressureAtStart).setHeight(heightAtStart);
        ((AtmosphericPressure)ambientPressureAtEnd).setHeight(heightAtEnd);
        
        this.ambientPressureChangeRate.equalsPressure(ambientPressureAtEnd);
        this.ambientPressureChangeRate.substractPressure(ambientPressureAtStart);
        this.ambientPressureChangeRate.multiplyPressure(1.0/fExposurePeriod);
    }
    
    /*------------------------------------------------------------------------------------------------*\
     * Get information
    \*------------------------------------------------------------------------------------------------*/    
    

    /*------------------------------------------------------------------------------------------------*\
     * Calculation
    \*------------------------------------------------------------------------------------------------*/    
    
    /** Applies the exposure of this segment to a tissue compartment. The partial
     *  N2 and He2 pressures in the given tissue compartment are updated
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
        double      fRate;

        Pressure tissuePressure;

        fAmbientPressure=ambientPressureAtStart.getValue(Pressure.UNITS_BAR);

        if (fExposurePeriod<0.0)
        {
            throw new CalculationException("Negative time value");
        }

        // NITROGEN
        initialN2Pressure=(Pressure)compartment.getN2TissueTension().clone();
        fConstantK=compartment.getN2K();
        fFraction=gasMixture.getN2Fraction();
        fRate=ambientPressureChangeRate.getValue(Pressure.UNITS_BAR)*fFraction;
        tissuePressure=compartment.getN2TissueTension();
        fTissuePressure=tissuePressure.getValue(Pressure.UNITS_BAR);
        fAlvPressure= Tools.alveolarPressure(ambientPressureAtStart, fFraction,
                Pressure.UNITS_BAR);


        // the schreiner equation
        fTissuePressure= Tools.schreinerEquation(fAlvPressure, fRate, fExposurePeriod,
                fConstantK, fTissuePressure);
        tissuePressure.setValue(fTissuePressure, Pressure.UNITS_BAR);

        // HELIUM
        initialHe2Pressure=(Pressure)compartment.getHe2TissueTension().clone();
        fConstantK=compartment.getHe2K();
        fFraction=gasMixture.getHe2Fraction();
        fRate=ambientPressureChangeRate.getValue(Pressure.UNITS_BAR)*fFraction;
        tissuePressure=compartment.getHe2TissueTension();
        fTissuePressure=tissuePressure.getValue(Pressure.UNITS_BAR);
        fAlvPressure= Tools.alveolarPressure(ambientPressureAtStart, fFraction,
                Pressure.UNITS_BAR);


        // the schreiner equation
        fTissuePressure= Tools.schreinerEquation(fAlvPressure, fRate, fExposurePeriod,
                fConstantK, fTissuePressure);
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
        
        xmlSegment=new MyXML("HeightChange");

        xmlHeight=xmlSegment.addElement("StartHeight");
        xmlHeight.addElement(heightAtStart.getXmlRepresentation());

        xmlHeight=xmlSegment.addElement("EndHeight");
        xmlHeight.addElement(heightAtEnd.getXmlRepresentation());
 
        xmlSegment.addElement("ChangePeriodInMinutes", Double.toString(this.fExposurePeriod));
        
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

        
        xmlHeight=xmlRepresentation.findElement("StartHeight");
        xmlLength=xmlHeight.findElement("Length");
        this.heightAtStart=new Length(xmlLength);

        
        xmlHeight=xmlRepresentation.findElement("EndHeight");
        xmlLength=xmlHeight.findElement("Length");
        this.heightAtEnd=new Length(xmlLength);
        
        xmlPeriod=xmlRepresentation.findElement("ChangePeriodInMinutes");
        this.fExposurePeriod=xmlPeriod.getValueAsDouble();
                
        xmlGasMix=xmlRepresentation.findElement("GasMixture");
        gasMixture=new GasMixture(xmlGasMix);
        
    }       
        

}

