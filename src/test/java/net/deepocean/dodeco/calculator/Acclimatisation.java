/*
 * Title:        DoDeco
 * Description:  DoDeco generates decompression profiles. Several algorithms
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
 *  This class represents an acclimatisation. An acclimatisation
 *  starts at a initial height (e.g. sealevel). From there the diver ascends to
 *  the acclimatiation height and stays there for a while (the acclimatisation 
 *  period). After the acclimatisation the diver (usually) starts the Dive.
 */
public class Acclimatisation extends Exposure
{
    private static final double HEIGHT_MTEVEREST=9144.0;

    private Length      startHeight;
    private Length      endHeight;
    private double      fAscentPeriod;
    private double      fAcclimatisePeriod;
    private GasMixture gasMixture;
    private double      fRunTime;

    private ConstantHeightSegment acclimatiseSegment;
    private VaryingHeightSegment ascentSegment;

    /**
     *  The constructor. Parameters are passed that characterise the Acclimatisation.
     *  The Acclimatisation is initialised. Heights should not be higher than the Mount Everest...
     *  @param startHeight          Height (above sealevel) at which the Acclimatisation starts (with ascending)
     *  @param endHeight            The height at which the Acclimatisation takes place, after initial ascent(/descent)
     *  @param fAscentPeriod        The time it takes in minutes to go from the startHeight to the endHeight. 
     *                              Should be larger than 0.
     *  @param fAcclimatisePeriod   The period at endHeight for acclimatisation.
     *  @param gasMixture           The GasMixture to use during Acclimatisation (ascent/descent and stay). Usually air.
     */
    public Acclimatisation( Length        startHeight,
                            Length        endHeight,
                            double        fAscentPeriod,
                            double        fAcclimatisePeriod,
                            GasMixture gasMixture)
                            throws IllegalActionException
    {
        if ((startHeight.getValue(Length.UNITS_METER) > HEIGHT_MTEVEREST) ||
            (startHeight.getValue(Length.UNITS_METER) > HEIGHT_MTEVEREST) ||
            (fAscentPeriod<=0.0))
        {
            throw new IllegalActionException();
        }

        this.startHeight        =(Length)startHeight.clone();
        this.endHeight          =(Length)endHeight.clone();
        this.gasMixture         =(GasMixture)gasMixture.clone();
        this.fAscentPeriod      =fAscentPeriod;
        this.fAcclimatisePeriod =fAcclimatisePeriod;
        
        acclimatiseSegment=new ConstantHeightSegment(endHeight, fAcclimatisePeriod,
                                                     gasMixture);
        ascentSegment     =new VaryingHeightSegment(startHeight, endHeight,
                                                    fAscentPeriod, gasMixture);        

    }
    
    /**
     *  The constructor. Parameters are passed that characterise the Acclimatisation in the
     *  form of an XML representation.
     *  The Acclimatisation is initialised. Heights should not be higher than the Mount Everest...
     *  @param xmlRepresentation Representation of the acclimatisation
     */    
    public Acclimatisation(MyXML xmlRepresentation) throws MyXMLException, IllegalActionException
    {
        this.createFromXmlRepresentation(xmlRepresentation);
    }

/* ===============================================================================  */
/*     SUBROUTINE VPM_ALTITUDE_DIVE_ALGORITHM                                       */
/*     Purpose:  This subprogram updates gas loadings and adjusts critical radii    */
/*     (as required) based on whether or not diver is acclimatised at altitude or   */
/*     makes an ascent to altitude before the dive.                                 */
/* ===============================================================================  */

    /** Updates the diver for this exposure.
     *  @param          diver The diver
     *  @param          fRunTime The runtime at the start of the dive
     *  @exception      CalculationException
     */
    public void exposeDiver(Diver diver, double fRunTime) throws CalculationException
    {
        
        // TO DO: it is nicer to create these in the constructor and update them 
        // when variables are updated

        
        this.fRunTime=fRunTime;
        ascentSegment.setRunTime(this.fRunTime);
        ascentSegment.exposeDiver(diver);
        this.fRunTime+=ascentSegment.getExposurePeriod();

        updateCriticalRadii(diver);

        acclimatiseSegment.setRunTime(this.fRunTime);
        ascentSegment.exposeDiver(diver);
        this.fRunTime+=ascentSegment.getExposurePeriod();

    }

    /**
     *  This method updates the critial radii for the acclimatisation
     *  @param diver The diver to expose
     */
    public void updateCriticalRadii(Diver diver)
    {
        Vector              compartments;
        TissueCompartment   compartment;
        Enumeration         elements;
        
       
        compartments=diver.getCompartments();
        elements=compartments.elements();
        
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            
            compartment.updateNucleiAtHeight(acclimatiseSegment.getExposurePeriod(),
                                             acclimatiseSegment.getAmbientPressureAtStart());

        }
    }
    
    
    

    /**
     *  This method return the initial height abouve sealevel at which the Acclimatisation
     *  starts.
     *  @return The initial height
     */
    public Length getStartHeight()
    {
        return ascentSegment.getHeightAtStart();
    }
    
    /**
     *  This method returns the final height at which the diver acclimatises
     *  @return The Acclimatisation height
     */
    public Length getEndHeight()
    {
        return acclimatiseSegment.getHeightAtEnd();
    }
    
    /**
     *  This method defines the parameters that characterise the Acclimatisation
     *  @param startHeight          Height (above sealevel) at which the Acclimatisation starts (with ascending)
     *  @param endHeight            The height at which the Acclimatisation takes place, after initial ascent(/descent)
     *  @param fAscentPeriod        The time it takes in minutes to go from the startHeight to the endHeight. 
     *                              Should be larger than 0.
     *  @param fAcclimatisePeriod   The period at endHeight for acclimatisation.
     *  @param gasMixture           The GasMixture to use during Acclimatisation (ascent/descent and stay). Usually air.
     */
    public void setParameters(Length startHeight,
                              Length endHeight,
                              double fAscentPeriod,
                              double fAcclimatisePeriod,
                              GasMixture gasMixture)
    {
        this.startHeight        =startHeight;
        this.endHeight          =endHeight;
        this.fAscentPeriod      =fAscentPeriod;
        this.fAcclimatisePeriod =fAcclimatisePeriod;
        this.gasMixture         =gasMixture;        
        
        this.ascentSegment.setParameters    (startHeight, endHeight, fAscentPeriod, gasMixture);
        this.acclimatiseSegment.setParameters(endHeight, fAcclimatisePeriod, gasMixture);
    }
    
   
    
    /**
     *  This method returns the GasMixture the diver breathes during ascent 
     *  and acclimatisation (usually air).
     *  @return The GasMixture instance representing the gas the diver uses.
     */
    public GasMixture getGasMixture()
    {
        return this.gasMixture;
    }
    
    /**
     *  This method returns the time it takes in minutes for the diver
     *  to go from the intial height to the acclimatisation heigt.
     *  @return The ascent (or descent) period
     */
    public double getAscentPeriod()
    {
        return this.fAscentPeriod;
    }
    
    /**
     *  This method returns the time in minutes the diver takes 
     *  to stay at the acclimatisation height
     *  @return The acclimatisation period in minutes
     */
    public double getAcclimatisationPeriod()
    {
        return this.fAcclimatisePeriod;
    }
    
    
    public void setAscentPeriod(double fAscentPeriod)
    {
        this.fAscentPeriod=fAscentPeriod;
    }
 
   
    /**
     *  This method returns the name of the exposure
     *  @return String indicating the name of the exposure
     */
    public String getExposureName()
    {
        return new String("Acclimatisation");
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
        MyXML xmlAcclimatisation;
        
        xmlAcclimatisation=new MyXML("Acclimatisation");

        xmlAcclimatisation.addElement(ascentSegment.getXmlRepresentation());    

        xmlAcclimatisation.addElement(acclimatiseSegment.getXmlRepresentation());    
        
       
        return xmlAcclimatisation;
    }    
    
    /**
     *  This method initialises the parameters characterising the Segment
     *  The values are retrieved from the XML representation.
     *  @xmlRepresentation Representation of the SSegment
     */
    private void createFromXmlRepresentation(MyXML xmlRepresentation) throws MyXMLException, IllegalActionException
    {
        MyXML xmlSegment;

        xmlSegment=xmlRepresentation.findElement("HeightChange");
        this.ascentSegment=new VaryingHeightSegment(xmlSegment);

        
        xmlSegment=xmlRepresentation.findElement("StayAtHeight");
        this.acclimatiseSegment=new ConstantHeightSegment(xmlSegment);
  
    } 
    
}