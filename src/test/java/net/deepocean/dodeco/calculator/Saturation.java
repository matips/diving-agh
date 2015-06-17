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
 * This class represents a saturation exposure of the diver.
 * The divers tissue is fully saturated and nuclear radii
 * are fully regenerated.
 */

public class Saturation extends Exposure
{
    private AtmosphericPressure ambientSaturationPressure;
    private Length              saturationHeight;
    private GasMixture gasMixture;

//    private SaturationEditPanel editPanel;

    /** Constuctor. Initializes the saturation at the indicated altitude
     *  @param          heightAboveSeaLevel The saturation height
     *  @param          gasMixture The saturation gas mixture
     */
    public Saturation(Length heightAboveSeaLevel, GasMixture gasMixture)
                    throws IllegalActionException
    {
        saturationHeight            =(Length)heightAboveSeaLevel.clone();
        this.gasMixture             =(GasMixture)gasMixture.clone();
 
        ambientSaturationPressure   =new AtmosphericPressure(saturationHeight);
    }
    
    /** Constuctor. Initialises this Exposure from an XML representation
     *  @param          xmlRepresentation XML representation of this Saturation
     */    
    public Saturation(MyXML xmlRepresentation) throws MyXMLException, IllegalActionException
    {
        createFromXmlRepresentation(xmlRepresentation);
        ambientSaturationPressure   =new AtmosphericPressure(saturationHeight);
    }
    
    /** Constuctor. Initializes the saturation at the indicated altitude
     *  @param          heightAboveSeaLevel The saturation height
     *  @param          gasMixture The saturation gas mixture
     */    
    public void setParameters(Length heightAboveSeaLevel, GasMixture gasMixture)
    {
        saturationHeight.equalsLength(heightAboveSeaLevel);
        gasMixture.equalsGasMixture(gasMixture);    
        
        ambientSaturationPressure.setHeight(saturationHeight);
    }
    
    

    /** Updates the diver for this exposure
     *  @param          diver The diver
     *  @param          fRunTime The runtime at the start of the exposure
     *  @exception      CalculationException
     */
    public void exposeDiver(Diver diver, double fRunTime) throws CalculationException
    {
        double fAmbientPressure;
        double fAlveolarPressureN2;
        double fAlveolarPressureHe2;
        Nucleus nucleus;

        TissueCompartment   compartment;
        Vector              compartments;
        Enumeration         elements;
        
        this.fRunTime=fRunTime;
/*
        fAmbientPressure=ambientSaturationPressure.getValue(Pressure.UNITS_BAR);
        fAlveolarPressureN2 =(fAmbientPressure-Parameters.fPressureH2O+
                              (1.0-Parameters.fRq)/Parameters.fRq*
                              Parameters.fPressureCO2)*gasMixture.getN2Fraction();
        fAlveolarPressureHe2=(fAmbientPressure-Parameters.fPressureH2O+
                              (1.0-Parameters.fRq)/Parameters.fRq*
                              Parameters.fPressureCO2)*gasMixture.getHe2Fraction();
*/
        fAlveolarPressureN2 = Tools.alveolarPressure(ambientSaturationPressure,
                gasMixture.getN2Fraction(),
                Pressure.UNITS_BAR);

        fAlveolarPressureHe2 = Tools.alveolarPressure(ambientSaturationPressure,
                gasMixture.getHe2Fraction(),
                Pressure.UNITS_BAR);


        compartments=diver.getCompartments();
        elements=compartments.elements();
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            
            compartment.setN2TissueTension (new Pressure(fAlveolarPressureN2 , Pressure.UNITS_BAR));
            compartment.setHe2TissueTension(new Pressure(fAlveolarPressureHe2, Pressure.UNITS_BAR));
            compartment.setN2InitialCriticalRadius(Parameters.initialCriticalRadiusN2);
            compartment.setHe2InitialCriticalRadius(Parameters.initialCriticalRadiusHe2);
        }

    }

    /**
     *  This method returns the GasMixture for which the diver is saturated. Usually it is air.
     *  @return The GasMixture
     */
    public GasMixture getGasMixture()
    {
        return gasMixture;
    }
    
    /**
     *  This method returns the height at which the diver is saturated
     *  @return The saturation height
     */
    public Length getSaturationHeight()
    {
        return saturationHeight;
    }
    
    /**
     *  This method returns the name of the Exposure
     *  @return The name of the exposure
     */
    public String getExposureName()
    {
        return new String("Saturation");
    }

    
    /**
     *  This method creates and adds an XML representation of the Saturation
     *  @return The MyXML instance representing the Saturation
     */
    public MyXML getXmlRepresentation() throws MyXMLException
    {
        MyXML element;
        MyXML height;
        
        element=new MyXML("Saturation");

        height=element.addElement("SaturationHeight");
        height.addElement(saturationHeight.getXmlRepresentation());
        
        element.addElement(gasMixture.getXmlRepresentation());
        return element;
    }    
    
    /**
     *  This method initialises the parameters characterising the Saturation.
     *  The values are retrieved from the XML representation.
     *  @param xmlRepresentation Representation of the Saturation
     */
    public void createFromXmlRepresentation(MyXML xmlRepresentation) throws MyXMLException, IllegalActionException
    {
        MyXML xmlHeight;
        MyXML xmlLength;
        MyXML xmlGasMix;
        
        xmlHeight=xmlRepresentation.findElement("SaturationHeight");
        xmlLength=xmlHeight.findElement("Length");
        saturationHeight=new Length(xmlLength);
        xmlGasMix=xmlRepresentation.findElement("GasMixture");
        gasMixture=new GasMixture(xmlGasMix);
        
    }
}
