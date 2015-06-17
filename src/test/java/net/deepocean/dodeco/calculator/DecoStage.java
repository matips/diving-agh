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
 *  This class speficies a decompression stage. A decompression
 *  stage starts at a given depth (extends upwards) and is
 *  defined by a starting depth, an end depth, a gas mixture,
 *  a deco step size and an ascent rate
 */

public class DecoStage
{
    /*------------------------------------------------------------------------------------------------*\
     * Variables
    \*------------------------------------------------------------------------------------------------*/    
    private Length      startDepth;
    private Length      endDepth;
    private Length      ascentRate;
    private Length      decoStepSize;
    private GasMixture gasMixture;
    private Pressure ambientPressureChangeRate;


    /*------------------------------------------------------------------------------------------------*\
     * Construction, initialisation and reinitialising
    \*------------------------------------------------------------------------------------------------*/    
    public DecoStage(   Length startDepth, 
                        Length endDepth,
                        Length ascentRate, 
                        GasMixture gasMixture,
                        Length decoStepSize)
                     throws IllegalActionException
    {
        this.startDepth     =(Length)startDepth.clone();
        this.endDepth       =(Length)endDepth.clone();
        this.ascentRate     =(Length)ascentRate.clone();
        this.decoStepSize   =(Length)decoStepSize.clone();
        this.gasMixture     =(GasMixture)gasMixture.clone();

        initDecoStage();

    }
    
    public DecoStage(MyXML xmlRepresentation) throws MyXMLException, IllegalActionException
    {
        createFromXmlRepresentation(xmlRepresentation);
        initDecoStage();
    }

    /**
     *  This method define the parameters that characterise the 
     *  DecoStage.
     */
    public void setParameters(   Length startDepth, 
                                Length endDepth,
                                Length ascentRate,
                                GasMixture gasMixture)
    {
        this.startDepth.equalsLength(startDepth);
        this.endDepth.equalsLength(endDepth);
        this.ascentRate.equalsLength(ascentRate);
        this.gasMixture.equalsGasMixture(gasMixture);
        
        initDecoStage();
    }
    
    private void initDecoStage()
    {
        ambientPressureChangeRate=
            DepthPressure.convertDepthChangeToPressureChange(ascentRate);
    }
    
    /*------------------------------------------------------------------------------------------------*\
     * Get information
    \*------------------------------------------------------------------------------------------------*/    
    public Length getStartDepth()
    {
        return startDepth;
    }

    public Length getEndDepth()
    {
        return endDepth;
    }

    public Length getAscentRate()
    {
        return ascentRate;
    }

    public Pressure getAmbientPressureChangeRate()
    {
        return ambientPressureChangeRate;
    }

    public Length getDecoStepSize()
    {
        return decoStepSize;
    }

    public GasMixture getGasMixture()
    {
        return gasMixture;
    }

    /*------------------------------------------------------------------------------------------------*\
     * XML parsing and writing
    \*------------------------------------------------------------------------------------------------*/
    /**
     *  This method creates and adds an XML representation of the GasMixture
     *  @return The MyXML instance representing the GasMixture
     */
    public MyXML getXmlRepresentation() throws MyXMLException
    {
        MyXML xmlDecoStage;
        MyXML xmlDepth;
        MyXML xmlAscentRate;
        MyXML xmlStep;
        
        xmlDecoStage=new MyXML("DecompressionStage");

        xmlDepth=xmlDecoStage.addElement("StartDepth");
        xmlDepth.addElement(startDepth.getXmlRepresentation());
        
        xmlDepth=xmlDecoStage.addElement("EndDepth");
        xmlDepth.addElement(endDepth.getXmlRepresentation());
        
        xmlDepth=xmlDecoStage.addElement("AscentRate");
        xmlDepth.addElement(ascentRate.getXmlRepresentation());

        xmlStep=xmlDecoStage.addElement("DecompressionStepSize");
        xmlStep.addElement(decoStepSize.getXmlRepresentation());
        
        xmlDecoStage.addElement(gasMixture.getXmlRepresentation());
        return xmlDecoStage;
    }     
    
    /**
     *  This method initialises the parameters characterising the GasMixture.
     *  The values are retrieved from the XML representation.
     *  @xmlRepresentation Representation of the GasMixture
     */
    private void createFromXmlRepresentation(MyXML xmlRepresentation) throws MyXMLException, IllegalActionException
    {
        MyXML xmlGasMix;
        MyXML xmlDecoStage;
        MyXML xmlDepth;
        MyXML xmlLength;
        MyXML xmlAscentRate;
        MyXML xmlStep;
        
        xmlDepth=xmlRepresentation.findElement("StartDepth");
        xmlLength=xmlDepth.findElement("Length");
        startDepth=new Length(xmlLength);

        xmlDepth=xmlRepresentation.findElement("EndDepth");
        xmlLength=xmlDepth.findElement("Length");
        endDepth=new Length(xmlLength);

        xmlAscentRate=xmlRepresentation.findElement("AscentRate");
        xmlLength=xmlAscentRate.findElement("Length");
        ascentRate=new Length(xmlLength);

        xmlStep=xmlRepresentation.findElement("DecompressionStepSize");
        xmlLength=xmlStep.findElement("Length");
        decoStepSize=new Length(xmlLength);
        
        xmlGasMix=xmlRepresentation.findElement("GasMixture");
        gasMixture=new GasMixture(xmlGasMix);
        
    }

}