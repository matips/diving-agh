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
 * This class represents a breathing gas mixture. The mixture
 * contains Oxygen, Helium and Nitrogen.

 *
 */

public class GasMixture
{
    /*------------------------------------------------------------------------------------------------*\
     * Variables
    \*------------------------------------------------------------------------------------------------*/    
    /** Index. Is not used by this class but can be used as identification */
    private int     iIndex;
    /** The Oxygen fraction. Cannot be set, but is calculated based on N2 and He2 */
    private double  fO2Fraction;
    /** The Helium (He2) fraction of the gas mixture */
    private double  fHe2Fraction;
    /** The Nitrogen (N2) fraction of the gas mixture */
    private double  fN2Fraction;
    /** The name of the mixture. Is not actively being used by the class */
    private String  sName;


    /*------------------------------------------------------------------------------------------------*\
     * Construction, initialisation and reinitialising
    \*------------------------------------------------------------------------------------------------*/    
    /** Constructor. Initializes the gas mixture
     *  @param          fHe2Fraction Helium fraction
     *  @param          fO2Fraction  Oxygen fraction
     *  @exception      IllegalActionException is thrown if inconsistency is
     *                  detected in the fraction values
     */
    public GasMixture(double fO2Fraction, double fHe2Fraction) throws IllegalActionException
    {
        this.iIndex=0;
        if ((fO2Fraction+fHe2Fraction>1.0) ||
            (fO2Fraction<0.0) || (fHe2Fraction<0.0))
        {
            throw new IllegalActionException("Illegal gas mixture");
        }
        else
        {
            this.fHe2Fraction   =fHe2Fraction;
            this.fO2Fraction    =fO2Fraction;
            this.fN2Fraction    =1.0-fHe2Fraction-fO2Fraction;
            this.createName();
        }
    }

    /** Constructor. Initializes the gas mixture
     *  @param          iHe2Percentage Helium percentage in gas mixture
     *  @param          iO2Percentage  Nitrogen percentage in gas mixture
     *  @exception      IllegalActionException is thrown if inconsistency is
     *                  detected in the fraction values
     */
    public GasMixture(int iO2Percentage, int iHe2Percentage) throws IllegalActionException
    {
        this.iIndex=0;

        if ((iO2Percentage+iHe2Percentage>100) ||
            (iO2Percentage<0) || (iHe2Percentage<0))
        {
            throw new IllegalActionException("Illegal gas mixture");
        }
        else
        {
            this.fHe2Fraction   =(double)iHe2Percentage/100.0;
            this.fO2Fraction    =(double)iO2Percentage/100.0;
            this.fN2Fraction    =1.0-((double)iHe2Percentage+(double)iO2Percentage)/100.0;
            this.createName();            
        }          
    }
    
    /** Constructor. Initializes the gas mixture
     *  @param          xmlRepresentation XML representation of the GasMixture
     *  @exception      IllegalActionException is thrown if inconsistency is
     *                  detected in the fraction values
     */
    public GasMixture(MyXML xmlRepresentation) throws IllegalActionException, MyXMLException
    {
        this.iIndex=0;

        createFromXmlRepresentation(xmlRepresentation);
    }    
    
    public void setGasFractions(double fO2Fraction, double fHe2Fraction) throws IllegalActionException
    {
        if ((fO2Fraction+fHe2Fraction>1.0) ||
            (fO2Fraction<0.0) || (fHe2Fraction<0.0))
        {
            throw new IllegalActionException("Illegal gas mixture");
        }
        else
        {
            this.fHe2Fraction   =fHe2Fraction;
            this.fO2Fraction    =fO2Fraction;
            this.fN2Fraction    =1.0-fHe2Fraction-fO2Fraction;
            this.createName();
        }    
    }

    /**
     *  This method sets the GasMixture fractions, based on the percentages given. The
     *  nitrogen fraction is calculated
     *  @param iO2Percentage Oxygen percentage
     *  @param iHe2Percentage Helium percentage
     *  @throws IllegalActionException If percentages are below 0 or add up to more than 100%
     */
    public void setGasPercentages(int iO2Percentage, int iHe2Percentage) throws IllegalActionException
    {
        if ((iO2Percentage+iHe2Percentage>100) ||
            (iO2Percentage<0) || (iHe2Percentage<0))
        {
            throw new IllegalActionException("Illegal gas mixture");
        }
        else
        {
            this.fHe2Fraction   =(double)iHe2Percentage/100.0;
            this.fO2Fraction    =(double)iO2Percentage/100.0;
            this.fN2Fraction    =1.0-((double)iHe2Percentage+(double)iO2Percentage)/100.0;
            this.createName();
        }            
    }
    
    /**
     *  This method redefines the index of the GasMixture
     *  The index is an number by which the GasMixture is identified
     *  in the GasMixture list
     */
    public void setIndex(int iIndex)
    {
        this.iIndex=iIndex;
    }
    
    /*------------------------------------------------------------------------------------------------*\
     * Get information
    \*------------------------------------------------------------------------------------------------*/    
    /** Returns the Nitrogen (N2) fraction
     *  @return         The Nitrogen (N2) fraction (0.0<=fraction<=1.0)
     */
    public double getN2Fraction()
    {
        return fN2Fraction;
    }

    /** Returns the Helium (He2) fraction
     *  @return         The Helium (He2) fraction (0.0<=fraction<=1.0)
     */
    public double getHe2Fraction()
    {
        return fHe2Fraction;
    }

    /** Returns the Oxygen (O2) fraction
     *  @return         The Oxygen (O2) fraction (0.0<=fraction<=1.0)
     */
    public double getO2Fraction()
    {
        return fO2Fraction;
    }
    
    /** This method returns the Nitrogen (N2) percentage
     *  @return         The Nitrogen (N2) percentage
     */
    public int getN2Percentage()
    {
        return (int)(100.0*this.fN2Fraction+0.5);
    }
    
    /** This method returns the Oxygen (O2) percentage
     *  @return         The Oxygen (O2) percentage
     */
    public int getO2Percentage()
    {
        return (int)(100.0*this.fO2Fraction+0.5);
    }
    
    /** This method returns the Helium (He2) percentage
     *  @return         The Helium (He2) percentage
     */
    public int getHe2Percentage()
    {
        return (int)(100.0*this.fHe2Fraction+0.5);
    }
    
    
    /** Returns the index of the gas mixture
     *  @return         The index
     */
    public int getIndex()
    {
        return iIndex;
    }

    /** Returns the name of the gas mixture
     *  @return         The name of the gas mixture
     */
    public String getName()
    {
        return sName;
    }

    /** Generates an exact copy of this gas mixture
     *  @return         The copy of the gas mixture
     */
    public Object clone()
    {
        GasMixture newMixture;

        newMixture=null;
        try
        {
            newMixture=new GasMixture(fO2Fraction, fHe2Fraction);
            newMixture.iIndex=this.iIndex;
        }
        catch (IllegalActionException e)
        {
            System.err.println(e.getMessage());
        }
        return newMixture;
    }
    
    /**
     *  This method creates the name of the mixture. Depending on the 
     *  constituents, it will be 'EAN nn', 'TMX nn/mm', 'Oxygen' or 'Air'.
     */
    private void createName()
    {
        int iHelium;
        int iOxygen;
        
        iHelium=(int)(100.0*fHe2Fraction+0.5);
        iOxygen=(int)(100.0*fO2Fraction+0.5);
        if (iHelium==0)
        {
            if (iOxygen==21)
            {
                sName="Air  ";
            }
            else if (iOxygen==100)
            {
                sName="Oxygen";
            }
            else
            {
                sName="EAN "+iOxygen;
            }
        }
        else
        {
            sName="TMX "+iOxygen+"/"+iHelium;
        }
        while (sName.length()<9)
        {
            sName+=" ";
        }
    }
    
    /**
     *  This method sets this GasMixture equal to the gasMixture passed as parameter
     *  @param gasMixture The GasMixture to equal
     */
    public void equalsGasMixture(GasMixture gasMixture)
    {
        this.iIndex         =gasMixture.iIndex;
        this.fHe2Fraction   =gasMixture.fHe2Fraction;
        this.fN2Fraction    =gasMixture.fN2Fraction;
        this.sName          =new String(gasMixture.sName);
    }    
    

    /*------------------------------------------------------------------------------------------------*\
     * XML parsing and writing
    \*------------------------------------------------------------------------------------------------*/
    /**
     *  This method creates an XML representation of the GasMixture
     *  @return The MyXML instance representing the GasMixture
     */
    public MyXML getXmlRepresentation() throws MyXMLException
    {
        MyXML element;
        
        element=new MyXML("GasMixture");
        element.addElement("Name", sName);
        element.addElement("OxygenPercentage", Integer.toString(this.getO2Percentage()));
        element.addElement("HeliumPercentage", Integer.toString(this.getHe2Percentage()));
        element.addElement("NitrogenPercentage", Integer.toString(this.getN2Percentage()));
        
        return element;
    }     
    
    /**
     *  This method initialises the parameters characterising the GasMixture.
     *  The values are retrieved from the XML representation.
     *  @xmlRepresentation Representation of the GasMixture
     */
    private void createFromXmlRepresentation(MyXML xmlRepresentation) throws MyXMLException, IllegalActionException
    {
        MyXML xmlO2;
        MyXML xmlHe2;
        MyXML xmlN2;
        MyXML xmlName;
        
        int     iO2Percentage;
        int     iN2Percentage;
        int     iHe2Percentage;
        
        xmlName=xmlRepresentation.findElement("Name");
        sName=xmlName.getValue();
        xmlO2=xmlRepresentation.findElement("OxygenPercentage");
        iO2Percentage=xmlO2.getValueAsInt();
        xmlHe2=xmlRepresentation.findElement("HeliumPercentage");
        iHe2Percentage=xmlHe2.getValueAsInt();
        xmlN2=xmlRepresentation.findElement("NitrogenPercentage");
        iN2Percentage=xmlN2.getValueAsInt();
        
        if (iO2Percentage+iN2Percentage+iHe2Percentage!=100)
        {
            throw new IllegalActionException("Percentages of GasMixture do not add up to 100%");
        }
        
        fO2Fraction     =(double)iO2Percentage/100.0;
        fHe2Fraction    =(double)iHe2Percentage/100.0;
        fN2Fraction     =(double)iN2Percentage/100.0;
    }    
}