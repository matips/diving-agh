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
 * This class represents a Length. It is also used for Lenght changes.
 * A Length is defined by a length value and accompanying units.
 * The value is always converted to meters before storing.
 * @author        B.J. van der Velde
 * @version       1.0
 */

public class Length
{
    public static final int UNITS_METER=1;
    public static final int UNITS_FEET =2;
    
    public static final Length ZERO=new Length(0.0, UNITS_METER);

    private static final double CONVFACTOR_METER_TO_FEET=3.256846780162842;    // conversion factors
    private static final double CONVFACTOR_FEET_TO_METER=1.0/3.256846780162842;

    private double fLengthInMeter;

    /** Constructor. Sets the length value
     *  @param          fLength   Length value
     *  @param          iUnits    Defines the units in which the Length is
     *                            specified. Possible values: UNITS_METER \
     *                            and UNITS_FEET.
     */
    public Length(double fLength, int iUnits)
    {
        setValue(fLength, iUnits);
    }
    
    /** Constructor. Creates a new length with same value as the one specified
     *  @param          length   Length value
     */
    public Length(Length length)
    {
        this.fLengthInMeter=length.fLengthInMeter;
    }
    
    /**
     *  Constuctor. Creates the Length instance based on 
     *  an XML representation
     *  @param xmlRepresentation Representation in XML of the Length
     */
    public Length(MyXML xmlRepresentation) throws MyXMLException
    {
        this.createFromXmlRepresentation(xmlRepresentation);
    }

    /** Sets the length value
     *  @param          fLength   Length value
     *  @param          iUnits    Defines the units in which the Length is
     *                            specified. Possible values: UNITS_METER \
     *                            and UNITS_FEET.
     */
    public void setValue(double fLength, int iUnits)
    {
        if (iUnits==UNITS_METER)
        {
            fLengthInMeter=fLength;
        }
        else if (iUnits==UNITS_FEET)
        {
            fLengthInMeter=CONVFACTOR_FEET_TO_METER*fLength;
        }
        else
        {
            fLengthInMeter=fLength;
        }
    }

    /** Sets the length value
     *  @param          length    Length defining the length value
     */
    public void setValue(Length length)
    {
        this.fLengthInMeter=length.getValue(UNITS_METER);
    }


    /** Returns the length value in indicated units
     *  @param          iUnits Units in which length value should be returned.
     *                  Possible values: UNITS_METER and UNITS_FEET.
     *  @return         The length value in units indicated by iUnits
     */
    public double getValue(int iUnits)
    {
        double fLength;

        if (iUnits==UNITS_METER)
        {
            fLength=fLengthInMeter;
        }
        else if (iUnits==UNITS_FEET)
        {
            fLength=fLengthInMeter*CONVFACTOR_METER_TO_FEET;
        }
        else
        {
            fLength=fLengthInMeter;
        }
        return fLength;
    }

    /** Adds the specified length to this length
     *  @param          length Length to be added to this length
     */
    public void addLength(Length length)
    {
        fLengthInMeter+=length.getValue(UNITS_METER);
    }

    /** Substracts the specified length from this length
     *  @param          length Length to be substracted from this length
     */
    public void substractLength(Length length)
    {
        fLengthInMeter-=length.getValue(UNITS_METER);
    }


    /** Multiplies this length by the specified factor
     *  @param          fFactor Factor to multiply the length with
     */
    public void multiplyLength(double fFactor)
    {
        fLengthInMeter*=fFactor;
    }

    /** Sets the value of this length to the value of the Length passed as parameter.
     *  @param          length The lenght to copy
     */
    public void equalsLength(Length length)
    {
        fLengthInMeter=length.getValue(UNITS_METER);        
    }
    
    /** Compares this length to the specified length (ohter length) and
     *  returns true if this length is smaller than the specified length.
     *  @param          otherLength Length to compare this length with.
     *  @return         true if this length is smaller than the other,
     *                  otherwise false is returned
     */
    public boolean smallerThan(Length otherLength)
    {
        boolean bReturn;

        if (fLengthInMeter<otherLength.getValue(UNITS_METER))
            bReturn=true;
        else
            bReturn=false;


        return bReturn;
    }

    /** Compares this length to the specified length (ohter length) and
     *  returns true if this length is larger than the specified length.
     *  @param          otherLength Length to compare this length with.
     *  @return         true if this length is larger than the other,
     *                  otherwise false is returned
     */
    public boolean largerThan(Length otherLength)
    {
        boolean bReturn;

        if (fLengthInMeter>otherLength.getValue(UNITS_METER))
            bReturn=true;
        else
            bReturn=false;
        return bReturn;
    }

    /** Compares this length to zero and
     *  returns true if this length is smaller zero.
     *  @return         true if this length is negative,
     *                  otherwise false is returned
     */
    public boolean isNegative()
    {
        boolean bIsNegative;

        if (this.fLengthInMeter<0)
        {
            bIsNegative=true;
        }
        else
        {
            bIsNegative=false;
        }
        return bIsNegative;
    }


    /** Rounds current length value up to a multiple of stepSize, expressed in
     *  the defined units.
     */
    public void roundUp(Length stepSize)
    {
        double fStepSize;
        double fRound;

        fStepSize=stepSize.getValue(UNITS_METER);
        fLengthInMeter=fStepSize*Math.round(fLengthInMeter/fStepSize+0.5);
    }


    /** Clones this Length instance
     *  @return         The clone of the instance
     */
    public Object clone()
    {
        return new Length(fLengthInMeter, UNITS_METER);
    }

    /*------------------------------------------------------------------------------------------------*\
     * XML parsing and writing
    \*------------------------------------------------------------------------------------------------*/

    /**
     *  This method creates and adds an XML representation of the Length
     *  @param iLengthUnits The units that are used to write the length to XML
     *  @return The MyXML instance representing the GasMixture
     */
    public MyXML getXmlRepresentation(int iLengthUnits) throws MyXMLException
    {
        MyXML element;
        
        element=new MyXML("Length");
        if (iLengthUnits==UNITS_METER)
        {
            element.addElement("Units", "Meter");
        }
        else if (iLengthUnits==UNITS_FEET)
        {
            element.addElement("Units", "Feet");
        }
        else
        {
            element.addElement("Units", "Unit error");
        }
        element.addElement("LengthValue", Double.toString(this.getValue(iLengthUnits)));            
        return element;
    }     
    
    
    /**
     *  This method creates and adds an XML representation of the Length
     *  The length is written to XML using meter as units.
     *  @return The MyXML instance representing the GasMixture
     */
    public MyXML getXmlRepresentation() throws MyXMLException
    {
        return this.getXmlRepresentation(UNITS_METER);
    }     
    
    /**
     *  This method initialises the parameters characterising the GasMixture.
     *  The values are retrieved from the XML representation.
     *  @param xmlRepresentation Representation of the GasMixture
     */
    public void createFromXmlRepresentation(MyXML xmlRepresentation) throws MyXMLException
    {
        MyXML xmlUnits;
        MyXML xmlLength;
        double  fValue;

        String  units;
        
        xmlUnits =xmlRepresentation.findElement("Units");
        xmlLength=xmlRepresentation.findElement("LengthValue");
        
        fValue=xmlLength.getValueAsDouble();
        if (xmlUnits.getValue().equals("Meter"))
        {
            setValue(fValue, UNITS_METER);
        }
        else if (xmlUnits.getValue().equals("Feet"))
        {
            setValue(fValue, UNITS_FEET);
        }
        else
        {
            fValue=0.0;
        }

    } 
    
}