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
 * This class represents a Pressure value. The class contains functionality
 * to convert between pressure units and simple arithmetic.
 */
public class Pressure
{
    public static final int     UNITS_BAR=1;              // units
    public static final int     UNITS_MSW=2;
    public static final int     UNITS_FSW=3;
    public static final int     UNITS_ATM=4;
    public static final int     UNITS_PASCAL=5;
    public static final int     UNITS_MMHG=6;

    private static final double CONVFACTOR_BAR_TO_MSW=10.0;    // conversion factors
    private static final double CONVFACTOR_BAR_TO_FSW=32.56846780162842;
    private static final double CONVFACTOR_MSW_TO_BAR=0.10;
    private static final double CONVFACTOR_FSW_TO_BAR=1.0/32.56846780162842;
    private static final double CONVFACTOR_BAR_TO_ATM=1.0/1.01325;
    private static final double CONVFACTOR_ATM_TO_BAR=1.01325;
    private static final double CONVFACTOR_BAR_TO_PASCAL=1e5;
    private static final double CONVFACTOR_PASCAL_TO_BAR=1e-5;
    private static final double CONVFACTOR_BAR_TO_MMHG=1.0/0.001333;
    private static final double CONVFACTOR_MMHG_TO_BAR=0.001333;


    private double fPressureInBar;                                      // the pressure in bars

    /** Constructor. Defines the pressure value
     *  @param          fPressure Pressure value
     *  @param          iUnits    Defines the units in which the pressure is
     *                            specified
     */
    public Pressure(double fPressure, int iUnits)
    {
        setValue(fPressure, iUnits);
    }

    /** Sets the pressure value
     *  @param          fPressure Pressure value
     *  @param          iUnits    Defines the units in which the pressure is
     *                            specified
     */
    public void setValue(double fPressure, int iUnits)
    {
        if (iUnits==UNITS_BAR)
        {
            fPressureInBar=fPressure;
        }
        else if (iUnits==UNITS_MSW)
        {
            fPressureInBar=CONVFACTOR_MSW_TO_BAR*fPressure;
        }
        else if (iUnits==UNITS_FSW)
        {
            fPressureInBar=CONVFACTOR_FSW_TO_BAR*fPressure;
        }
        else if (iUnits==UNITS_ATM)
        {
            fPressureInBar=CONVFACTOR_ATM_TO_BAR*fPressure;
        }
        else if (iUnits==UNITS_PASCAL)
        {
            fPressureInBar=CONVFACTOR_PASCAL_TO_BAR*fPressure;
        }
        else if (iUnits==UNITS_MMHG)
        {
            fPressureInBar=CONVFACTOR_MMHG_TO_BAR*fPressure;
        }
        else
        {
            fPressureInBar=fPressure;
        }
    }

    public void setValue(Pressure pressure)
    {
        fPressureInBar=pressure.getValue(UNITS_BAR);
    }

    /** Returns the pressure value in indicated units
     *  @param          iUnits Units in which pressure value should be returned
     *  @return         The pressure value in units indicated by iUnits
     *  @exception      -
     */
    public double getValue(int iUnits)
    {
        double fPressure;

        if (iUnits==UNITS_BAR)
        {
            fPressure=fPressureInBar;
        }
        else if (iUnits==UNITS_MSW)
        {
            fPressure=fPressureInBar*CONVFACTOR_BAR_TO_MSW;
        }
        else if (iUnits==UNITS_FSW)
        {
            fPressure=fPressureInBar*CONVFACTOR_BAR_TO_FSW;
        }
        else if (iUnits==UNITS_ATM)
        {
            fPressure=fPressureInBar*CONVFACTOR_BAR_TO_ATM;
        }
        else if (iUnits==UNITS_PASCAL)
        {
            fPressure=fPressureInBar*CONVFACTOR_BAR_TO_PASCAL;
        }
        else if (iUnits==UNITS_MMHG)
        {
            fPressure=fPressureInBar*CONVFACTOR_BAR_TO_MMHG;
        }
        else
        {
            fPressure=fPressureInBar;
        }
        return fPressure;
    }

    /** Adds the specified pressure to this pressure
     *  @param          pressure Pressure to be added to this pressure
     */
    public void addPressure(Pressure pressure)
    {
        fPressureInBar+=pressure.getValue(this.UNITS_BAR);
    }

    /** Substracts the specified pressure from this pressure
     *  @param          pressure Pressure to be substracted from this pressure
     */
    public void substractPressure(Pressure pressure)
    {
        fPressureInBar-=pressure.getValue(this.UNITS_BAR);
    }

    /** Multiplies this pressure by the specified factor
     *  @param          fFactor Factor to multiply the pressure with
     */
    public void multiplyPressure(double fFactor)
    {
        fPressureInBar*=fFactor;
    }

    /** Sets this pressure equal to the indicated pressure
     *  @param          pressure Pressure to equal
     */
    public void equalsPressure(Pressure pressure)
    {
        fPressureInBar=pressure.getValue(this.UNITS_BAR);
    }    

    /** Converts a source pressure value into the destination pressure value.
     *  The units of the source and destination pressure have to be specified.
     *  @param          fSourcePressure Pressure value to be converted
     *  @param          iSourceUnits Units in which fSourcePressure is
     *                  specified.
     *  @param          iDestinationUnits Units to which the pressure have to
     *                  be converted.
     *  @return         The converted destination pressure value
     */
    public static double convertPressure(double fSourcePressure,
                                         int iSourceUnits, int iDestinationUnits)

    {
        double fDestinationPressureBar;
        double fDestinationPressure;

        if (iSourceUnits==UNITS_BAR)
        {
            fDestinationPressureBar=fSourcePressure;
        }
        else if (iSourceUnits==UNITS_MSW)
        {
            fDestinationPressureBar=CONVFACTOR_MSW_TO_BAR*fSourcePressure;
        }
        else if (iSourceUnits==UNITS_FSW)
        {
            fDestinationPressureBar=CONVFACTOR_FSW_TO_BAR*fSourcePressure;
        }
        else if (iSourceUnits==UNITS_ATM)
        {
            fDestinationPressureBar=CONVFACTOR_ATM_TO_BAR*fSourcePressure;
        }
        else if (iSourceUnits==UNITS_PASCAL)
        {
            fDestinationPressureBar=CONVFACTOR_PASCAL_TO_BAR*fSourcePressure;
        }
        else if (iSourceUnits==UNITS_MMHG)
        {
            fDestinationPressureBar=CONVFACTOR_MMHG_TO_BAR*fSourcePressure;
        }
        else
        {
            fDestinationPressureBar=fSourcePressure;
        }

        if (iDestinationUnits==UNITS_BAR)
        {
            fDestinationPressure=fDestinationPressureBar;
        }
        else if (iDestinationUnits==UNITS_MSW)
        {
            fDestinationPressure=fDestinationPressureBar*CONVFACTOR_BAR_TO_MSW;
        }
        else if (iDestinationUnits==UNITS_FSW)
        {
            fDestinationPressure=fDestinationPressureBar*CONVFACTOR_BAR_TO_FSW;
        }
        else if (iDestinationUnits==UNITS_ATM)
        {
            fDestinationPressure=fDestinationPressureBar*CONVFACTOR_BAR_TO_ATM;
        }
        else if (iDestinationUnits==UNITS_PASCAL)
        {
            fDestinationPressure=fDestinationPressureBar*CONVFACTOR_BAR_TO_PASCAL;
        }
        else if (iDestinationUnits==UNITS_MMHG)
        {
            fDestinationPressure=fDestinationPressureBar*CONVFACTOR_BAR_TO_MMHG;
        }
        else
        {
            fDestinationPressure=fDestinationPressureBar;
        }

        return fDestinationPressure;

    }

    /** Compares this pressure to the specified pressure (ohter pressure) and
     *  returns true if this pressure is smaller than the specified pressure.
     *  @param          otherPressure Pressure to compare this pressure with.
     *  @return         true if this pressure is smaller than the other,
     *                  otherwise false is returned
     *  @exception      -
     */
    public boolean smallerThan(Pressure otherPressure)
    {
        boolean bReturn;

        if (fPressureInBar<otherPressure.getValue(UNITS_BAR))
            bReturn=true;
        else
            bReturn=false;


        return bReturn;
    }

    /** Compares this pressure to the specified pressure (ohter pressure) and
     *  returns true if this pressure is larger than the specified pressure.
     *  @param          otherPressure Pressure to compare this pressure with.
     *  @return         true if this pressure is larger than the other,
     *                  otherwise false is returned
     *  @exception      -
     */
    public boolean largerThan(Pressure otherPressure)
    {
        boolean bReturn;

        if (fPressureInBar>otherPressure.getValue(UNITS_BAR))
            bReturn=true;
        else
            bReturn=false;


        return bReturn;
    }

    /** Compares this pressure to zero and
     *  returns true if this pressure is smaller zero.
     *  @return         true if this pressure is negative,
     *                  otherwise false is returned
     */
    public boolean isNegative()
    {
        boolean bIsNegative;

        if (this.fPressureInBar<0)
        {
            bIsNegative=true;
        }
        else
        {
            bIsNegative=false;
        }
        return bIsNegative;
    }

    /** Clones this pressure instance
     *  @return         The clone of the instance
     */
    public Object clone()
    {
        return new Pressure(fPressureInBar, UNITS_BAR);
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
        MyXML element;
        
        element=new MyXML("Pressure");
        element.addElement("Units", "Bar");
        element.addElement("PressureValue", Double.toString(this.fPressureInBar));
       
        return element;
    }     
    
    /**
     *  This method initialises the parameters characterising the GasMixture.
     *  The values are retrieved from the XML representation.
     *  @param xmlRepresentation Representation of the GasMixture
     */
    public void createFromXmlRepresentation(MyXML xmlRepresentation) throws MyXMLException
    {
        MyXML   xmlUnits;
        MyXML   xmlLength;
        double  fValue;

        String  units;
        
        xmlUnits =xmlRepresentation.findElement("Units");
        xmlLength=xmlRepresentation.findElement("PressureValue");
        
        fValue=xmlLength.getValueAsDouble();
        if (xmlUnits.getValue().equals("Bar"))
        {
            setValue(fValue, UNITS_BAR);
        }
        // TO DO: ADD OTHER UNITS
        else
        {
            fValue=0.0;
        }

    } 
    
}