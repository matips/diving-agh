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

import java.util.Enumeration;
import java.util.Vector;

/**
 *  This class represents a Diver. A Diver basically is a repository of 
 *  TissueCompartments.
 *
 */
public class Diver
{
    /*------------------------------------------------------------------------------------------------*\
     * Variables
    \*------------------------------------------------------------------------------------------------*/    
    private Vector<TissueCompartment> tissueCompartments;


    /*------------------------------------------------------------------------------------------------*\
     * Construction and reinitialising
    \*------------------------------------------------------------------------------------------------*/    
    /**
     * Constructor. Initializes the tissue compartments
     */
    public Diver()
    {
        initializeDiver();
    }

    /**
     * Constructor. A choice can be made whether or not to inialise
     * the tissue compartments of the diver. If not, the tissue
     * compartment array is left empty.
     * @param bInitialzeDiver Boolean indicating whether to initialise th
     *                        tissue compartments or not.
     */
    private Diver(boolean bInitializeDiver)
    {
        if (bInitializeDiver)
        {
            initializeDiver();
        }
        else
        {
            tissueCompartments=new Vector<TissueCompartment>();
        }
    }

    /**
     * This method adds a tissue compartment to the Diver.
     * @param newCompartment New compartment to add to the array of
     *                       compartments.
     */
    private void addCompartment(TissueCompartment newCompartment)
    {
        tissueCompartments.add(newCompartment);
    }

    /**
     * This method initialises the tissue compartments with halftime-values
     * as defined in Parameters class. Basically, Buhlmann values are used ??
     * Besides half-times, also initial critical radii are set. These are used 
     * for VPM calculations.
     */
    private void initializeDiver()
    {
        int     i;
        Nucleus nucleus;

        TissueCompartment compartment;
        
        tissueCompartments=new Vector<TissueCompartment>();

        i=0;
        while (i<Parameters.nCompartments)
        {
            compartment=new TissueCompartment(Parameters.fHalfTimeN2[i],
                                              Parameters.fHalfTimeHe2[i]);
            compartment.setN2InitialCriticalRadius(Parameters.initialCriticalRadiusN2);
            compartment.setHe2InitialCriticalRadius(Parameters.initialCriticalRadiusHe2);

/*
            if (i==0)
            {
                tissueCompartments=compartment;
            }
            else
            {
                tissueCompartments.appendItemToChain(compartment);
            }
 */
            tissueCompartments.add(compartment);


            i++;
        }

    }

    /*------------------------------------------------------------------------------------------------*\
     * Get information
    \*------------------------------------------------------------------------------------------------*/    
    /**
     * Returns the tissue compartments of the diver.
     * @return Linked list of TissueCompartments
     */
    public Vector<TissueCompartment> getCompartments()
    {
        return tissueCompartments;
    }

    /**
     *  This method temporarily back-ups the tissue tensions.
     *  @param iBackupArrayIndex Array to which backup takes place
     */
    public void backupTissueTensions(int iBackupArrayIndex)
    {
        TissueCompartment   compartment;
        Enumeration         elements;

/*
        compartment=tissueCompartments;
        while (compartment!=null)
        {
            compartment.backupTissueTension(iBackupArrayIndex);
            compartment=(TissueCompartment)compartment.getNextChainItem();
        }
*/
        elements=tissueCompartments.elements();
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            compartment.backupTissueTension(iBackupArrayIndex);
        }
    }

    /**
     * This method restores the tissue tension.
     * @param iBackupArrayIndex Array from which backup takes place.
     */
    public void restoreTissueTensions(int iBackupArrayIndex)
    {
        TissueCompartment   compartment;
        Enumeration         elements;

/*
        compartment=tissueCompartments;
        while (compartment!=null)
        {
            compartment.restoreTissueTension(iBackupArrayIndex);
            compartment=(TissueCompartment)compartment.getNextChainItem();
        }
*/
        elements=tissueCompartments.elements();
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            compartment.restoreTissueTension(iBackupArrayIndex);
        }
    }

    /*------------------------------------------------------------------------------------------------*\
     * Calculation
    \*------------------------------------------------------------------------------------------------*/    
    /**
     * This method resets the TissueCompartments.
     */
    public void resetBeforeDive()
    {
        TissueCompartment   compartment;
        Enumeration         elements;

/*        
        compartment=tissueCompartments;
        while (compartment!=null)
        {
            compartment.resetBeforeDive();
            compartment=(TissueCompartment)compartment.getNextChainItem();
        }
 */
        elements=tissueCompartments.elements();
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            compartment.resetBeforeDive();
        } 
    }

    /**
     * This method resets the phase volume time.
     */
    public void resetLastPhaseVolumeTime()
    {
        TissueCompartment   compartment;
        Enumeration         elements;

/*
        compartment=tissueCompartments;
        while (compartment!=null)
        {
            compartment.setLastPhaseVolumeTime(0.0);
            compartment=(TissueCompartment)compartment.getNextChainItem();
        }
*/
        elements=tissueCompartments.elements();
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            compartment.setLastPhaseVolumeTime(0.0);
        }         
    }

    /**
     * This method clones the diver.
     */
    public Object clone()
    {
        Diver newDiver;
        TissueCompartment   compartment;
        Enumeration         elements;
        
        newDiver=new Diver(false);              // create unitialized diver

/*        
        compartment=tissueCompartments;
        while (compartment!=null)
        {
            newDiver.addCompartment((TissueCompartment)compartment.clone());
            compartment=(TissueCompartment)compartment.getNextChainItem();
        }
 */
        elements=tissueCompartments.elements();
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            newDiver.addCompartment((TissueCompartment)compartment.clone());
        }        
        return newDiver;
    }

}