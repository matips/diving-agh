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

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;


/**
 * This class represents the repository of exposures to which the Diver
 * is exposed. Exposures can be added or removed. Exposures can be 
 * saved or loaded.
 * The exposing of the diver is initiated by calling process().
 */

public class Processor 
{
    private static final long serialVersionUID = 1L;

    public  static final int ALGORITHM_BUHLMANNA                = 0;
    public  static final int ALGORITHM_BUHLMANNB                = 1;
    public  static final int ALGORITHM_BUHLMANNC                = 2;
    public  static final int ALGORITHM_BUHLMANNBWITHGRADIENT    = 3;
    public  static final int ALGORITHM_VPM                      = 4;
    public  static final int ALGORITHM_VPMB                     = 5;
    public  static final int ALGORITHM_RGBM                     = 6;
    
    
    public static final int                     MAX_EXPOSURES=20;
    private             Vector<Exposure>        theExposures;
    private             GasMixture[]            gasMixtures;
    private Diver diver;

    private             int                     iAlgorithm;

    /**
     * Constuctor. Initializes the arrays
     */
    public Processor()
    {
        // Initialise main variables
        diver           =new Diver();
        theExposures    =new Vector<Exposure>();
        gasMixtures     =null;
        
        iAlgorithm      =ALGORITHM_VPM;
        
        // Start a new sequence of exposures.
        newSequence();
       
    }

    /**
     *  Empty the exposure list
     */
    private void clear()
    {
        // Reset the Exposure array
        theExposures.clear();
    }

    public void setDecoAlgorithm(int iAlgorithm)
    {
        this.iAlgorithm=iAlgorithm;

        Enumeration         exposures;
        Exposure            exposure;


        exposures=theExposures.elements();
        while (exposures.hasMoreElements())
        {
            exposure=(Exposure)exposures.nextElement();
            
            if (exposure.getClass()==Dive.class)
            {
                addDecoAlgorithm((Dive)exposure);
            }
        }
        
    }
    
    /** 
     *  This method adds a new Decompression algorithm
     *  to the dive, based on the setting iAlgorithm in this
     *  class. If the value of iAlgorithm is not set, no algorithm is
     *  added. In that case it uses the default algorithm defined
     *  in the Dive constructor (which is VPM).
     *  @param dive The dive to modify
     */
    private void addDecoAlgorithm(Dive dive)
    {
        Decompression decoAlgorithm=null;
        
        if (iAlgorithm==this.ALGORITHM_VPM)
        {
            decoAlgorithm=new VpmDecoDecompression();
        }
        else if (iAlgorithm==this.ALGORITHM_VPMB)
        {
            decoAlgorithm=new VpmBDecompression();
        }
        else if (iAlgorithm==this.ALGORITHM_BUHLMANNA)
        {
            decoAlgorithm=new ZHL16Decompression();
            ((ZHL16Decompression)decoAlgorithm).setModel(ZHL16Decompression.ZH_L16A);
        }
        else if (iAlgorithm==this.ALGORITHM_BUHLMANNB)
        {
            decoAlgorithm=new ZHL16Decompression();
            ((ZHL16Decompression)decoAlgorithm).setModel(ZHL16Decompression.ZH_L16B);
        }
        else if (iAlgorithm==this.ALGORITHM_BUHLMANNC)
        {
            decoAlgorithm=new ZHL16Decompression();
            ((ZHL16Decompression)decoAlgorithm).setModel(ZHL16Decompression.ZH_L16C);
        }
        else if (iAlgorithm==this.ALGORITHM_BUHLMANNBWITHGRADIENT)
        {
            decoAlgorithm=new ZHL16WithGradientDecompression();
            ((ZHL16WithGradientDecompression)decoAlgorithm).setModel(ZHL16Decompression.ZH_L16B);
        }
        else if (iAlgorithm==this.ALGORITHM_RGBM)
        {
            // To be defined
        }
        if (decoAlgorithm!=null)
        {
            dive.setDecompression(decoAlgorithm);
        }
    }
    
    /**
     *  Empty the exposure list and start a new one.
     *  An initial Saturation is added as start of the list
     */
    public void newSequence()
    {
        GasMixture air;
        Length      seaLevel;
        Saturation saturation;
        
        clear();
        // 1st exposure: Initial saturation of the diver at sea level, normal air
        try
        {
            air         =new GasMixture(0.21, 0.00);
            seaLevel    =new Length(0.0, Length.UNITS_METER);
            saturation  =new Saturation(seaLevel, air);

            addExposure(saturation);
        }
        catch (IllegalActionException e)
        {
            System.err.println(e.getMessage());
        }           
    }
    
    /** Adds an exposure and appends it at the end of the exposure list
     *  @param          exposure Exposure to be added at the end of the list
     */
    public void addExposure(Exposure exposure)
    {
        // First make sure the proper deco algorithm  is set when the
        // exposure is a Dive instance
        if (exposure.getClass()==Dive.class)
        {
            this.addDecoAlgorithm((Dive)exposure);
        }
        // then append the exposure to the list
        theExposures.add(exposure);
    }

   
    
    /** Adds an exposure to the list. The exposure is added at the location
     *  indicated by the iIndex parameter. If the index points to a location
     *  outside the list, the exposure is appended to the list.
     *  @param          exposure The exposure to be added
     *  @param          index The index of the location where the exposure is added
     */
    public void addExposure(Exposure exposure, int index)
    {
        // First make sure the proper deco algorithm  is set when the
        // exposure is a Dive instance
        if (exposure.getClass()==Dive.class)
        {
            this.addDecoAlgorithm((Dive)exposure);
        }

        // insert the exposure in the list
        if (index>theExposures.size())
        {
            theExposures.add(exposure);
        }
        else
        {
            theExposures.add(index, exposure);
        }
    }

    /** Removes an exposure from the list of exposure. If the index 
     *  of the exposure to remove is not within the list of exposures,
     *  nothing is done.
     *  @param          index Index indicating the position of the exposure in
     *                  the list.
     */
    public void removeExposure(int index)
    {
        if ((index>=0) && (index<theExposures.size()))
        {
            theExposures.remove(index);
        }
    }

    /** This method generates a vector containing the names of the exposures
     *  in the list
     *  @return         The vector with names of the exposures.
     */
    public Vector getExposureNames()
    {
        Enumeration         exposures;
        Exposure            exposure;
        Vector<String>      names;

        names=new Vector<String>();

        exposures=theExposures.elements();
        while (exposures.hasMoreElements())
        {
            exposure=(Exposure)exposures.nextElement();
            names.add(exposure.getExposureName());
        }
        return names;
    }

    public Diver getDiver() {
        return diver;
    }

    /** This routine returns the double linked list containing the exposures.
     *  @return         The Vector containing the exposures
     */
    public Vector<Exposure> getExposures()
    {
        return theExposures;
    }

    /** This routine returns the indicated Exposure.
     *  @param          iIndex Index of the exposure
     *  @return         The Exposure or null if the index is out of bounds
     */
    public Exposure getExposure(int iIndex)
    {
        Exposure exposure;
        
        if ((iIndex>=0) && (iIndex<theExposures.size()))
        {
            exposure=theExposures.elementAt(iIndex);
        }
        else
        {
            exposure=null;
        }
        return exposure;
    }
    
    /*------------------------------------------------------------------------------------------------*\
     * Calculation
    \*------------------------------------------------------------------------------------------------*/    
    /** 
     *  This method resets the Processor, so that a new calculation
     *  (process()) can take place
     */
    public void resetProcessing()
    {
        Enumeration         exposures;
        Exposure            exposure;

        // Reset the Exposures
        exposures=theExposures.elements();
        while (exposures.hasMoreElements())
        {
            exposure=(Exposure)exposures.nextElement();
            exposure.resetExposure();
        }
    }
   
    
    /** This routine processes the exposures. The diver is updated for the
     *  exposures.
     *  @exception IllegalActionException
     *  @exception      CalculationException
     */
    public void process() throws IllegalActionException, CalculationException
    {
        Enumeration exposures;
        Exposure    exposure;
        double      fRunTime;

      
        if (theExposures.size()==0)
        {
             throw new IllegalActionException("No exposures defined");
        }        

        resetProcessing();
        
        
        fRunTime=0.0;
        exposures=theExposures.elements();

        while(exposures.hasMoreElements())
        {
            exposure=(Exposure)exposures.nextElement();
            exposure.exposeDiver(diver, fRunTime);
            fRunTime=exposure.getRunTime();            
        }
   
    }

    /*------------------------------------------------------------------------------------------------*\
     * Setting an example Exposure List
    \*------------------------------------------------------------------------------------------------*/    
    /** This method defines an example dive, showing the capabilities of this
     *  deco software. In fact it is the dive defined in the documentation of 
     *  Erik Baker, which comes with his VPMDECO fortran program
     */
    public void setExampleDive()
    {
        ExposureSegment segment;
        Exposure        exposure;
        Dive dive;
        Length          diveHeight;

        // Erase ALL segments;
        this.theExposures.clear();
        
        // dive height
        diveHeight=new Length(0.0, Length.UNITS_METER);

        // Gas mixtures
        gasMixtures=new GasMixture[4];
        try
        {
            gasMixtures[0]=new GasMixture(0.15, 0.45);   // trimix
            gasMixtures[1]=new GasMixture(0.36, 0.00);   // nitrox: 36% oxygen, 64% Nitrogen
            gasMixtures[2]=new GasMixture(1.00, 0.00);   // nitrox: pure oxygen
            gasMixtures[3]=new GasMixture(0.21, 0.00);   // plain air
        }
        catch (IllegalActionException e)
        {
            System.err.println(e.getMessage());
        }

        // 1st exposure: Initial saturation of the diver at sea level, normal air
        try
        {
            exposure=new Saturation(new Length(0.0, Length.UNITS_METER), gasMixtures[3]);

            addExposure(exposure);
        }
        catch (IllegalActionException e)
        {
            System.err.println(e.getMessage());
        }

        // 2nd exposure: dive
        try
        {
            dive=new Dive(  "TRIMIX DIVE TO 260 FSW",       // Description of the dive
                            diveHeight);                    // Height at which dive takes place

            dive.addVaryingDepthSegment(new Length(0.0  , Length.UNITS_FEET),   // going down
                                        new Length(260.0, Length.UNITS_FEET),
                                        new Length(75.0 , Length.UNITS_FEET),
                                        gasMixtures[0]);

            dive.addConstantDepthSegment(new Length(260.0, Length.UNITS_FEET),  // staying at depth
                                         30.0-260.0/75.0,
                                         gasMixtures[0]);


            dive.addDecoStage(new Length(260.0, Length.UNITS_FEET),             // define gas mixtures during deco
                              new Length(110.0, Length.UNITS_FEET),
                              new Length(-30.0, Length.UNITS_FEET),
                              gasMixtures[0],
                              new Length(10.0, Length.UNITS_FEET));
            dive.addDecoStage(new Length(110.0, Length.UNITS_FEET),
                              new Length(20.0, Length.UNITS_FEET),
                              new Length(-30.0, Length.UNITS_FEET),
                              gasMixtures[1],
                              new Length(10.0, Length.UNITS_FEET));
            dive.addDecoStage(new Length(20.0, Length.UNITS_FEET),
                              new Length(0.0, Length.UNITS_FEET),
                              new Length(-10.0, Length.UNITS_FEET),
                              gasMixtures[2],
                              new Length(20.0, Length.UNITS_FEET));

            addExposure(dive);
        }
        catch (IllegalActionException e)
        {
            System.err.println(e.getMessage());
        }

        // 3rd exposure: surface interval

        exposure=new SurfaceInterval(   diveHeight,
                                        gasMixtures[3], 
                                        60.0);
        addExposure(exposure);


        // 4th exposure: dive
        try
        {
            dive=new Dive("TRIMIX DIVE TO 260 FSW", diveHeight);

            dive.addVaryingDepthSegment(new Length(0.0  , Length.UNITS_FEET),
                                        new Length(260.0, Length.UNITS_FEET), 
                                        new Length(75.0 , Length.UNITS_FEET), 
                                        gasMixtures[0]);

            dive.addConstantDepthSegment(new Length(260.0, Length.UNITS_FEET),
                                         30.0-260.0/75.0,
                                         gasMixtures[0]);


            dive.addDecoStage(new Length(260.0, Length.UNITS_FEET),
                              new Length(110.0, Length.UNITS_FEET),
                              new Length(-30.0, Length.UNITS_FEET),
                              gasMixtures[0],
                              new Length(10.0, Length.UNITS_FEET));
            dive.addDecoStage(new Length(110.0, Length.UNITS_FEET),
                              new Length(20.0, Length.UNITS_FEET),
                              new Length(-30.0, Length.UNITS_FEET),
                              gasMixtures[1],
                              new Length(10.0, Length.UNITS_FEET));
            dive.addDecoStage(new Length(20.0, Length.UNITS_FEET),
                              new Length(0.0, Length.UNITS_FEET),
                              new Length(-10.0, Length.UNITS_FEET),
                              gasMixtures[2],
                              new Length(20.0, Length.UNITS_FEET));

            addExposure(dive);
        }
        catch (IllegalActionException e)
        {
            System.err.println(e.getMessage());
        }
        int i=1;
    }

    /*------------------------------------------------------------------------------------------------*\
     * Printing the dive table
    \*------------------------------------------------------------------------------------------------*/    
    /**
     * This method prints the tissue compartment tensions fo the diver to
     * standard out.
     */
    public void printDiver()
    {
        TissueCompartment   compartment;
        Vector              compartments;
        Enumeration         elements;
        
        int i;

        compartments=diver.getCompartments();
        elements=compartments.elements();
        
        i=0;
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();
            System.out.println( "Compartment "+i+
                                " He: "+compartment.getHe2TissueTension().getValue(Pressure.UNITS_FSW)+
                                " N: "+ compartment.getN2TissueTension().getValue(Pressure.UNITS_FSW)+
                               " He: "+compartment.getHe2MaxCrushingPressure().getValue(Pressure.UNITS_FSW)+
                                " N: "+ compartment.getN2MaxCrushingPressure().getValue(Pressure.UNITS_FSW));
            i++;
        }
    }

    /**
     * This method prints an overview of the exposures. In fact, it is the dive
     * plan.
     */
    public void printExposures(Writer writer)
    {
        Enumeration exposures;
        Exposure exposure;

        try
        {
            exposures=theExposures.elements();
            while(exposures.hasMoreElements())
            {
                exposure=(Exposure)exposures.nextElement();
                exposure.printExposure(writer);
            }            
            writer.flush();
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }
    
    /*------------------------------------------------------------------------------------------------*\
     * XML parsing and writing
    \*------------------------------------------------------------------------------------------------*/
    /**
     *  This method loads the list of exposures from XML file
     *  @param file The file to read from
     */
    public void loadExposures(File file)
    {
        FileReader          reader;
        BufferedReader      bufferedReader;
        MyXML               root;
        MyXML               element;
        int                 i;
        String              tag;
        Exposure            exposure;
        
        // Empty
        this.clear();
        try
        {
            reader          =new FileReader(file);
            bufferedReader  =new BufferedReader(reader);
            root=new MyXML(bufferedReader);
            
            i=0;
            while (i<root.size())
            {
                element=root.getElement(i);
                tag=element.getTag();
                if (tag.equals("Saturation"))
                {
                    exposure=new Saturation(element);
                    this.addExposure(exposure);
                }
                else if (tag.equals("Dive"))
                {
                    exposure=new Dive(element);
                    this.addExposure(exposure);
                }
                else if (tag.equals("SurfaceInterval"))
                {
                    exposure=new SurfaceInterval(element);
                    this.addExposure(exposure);
                }
                else if (tag.equals("Acclimatisation"))
                {
                    exposure=new Acclimatisation(element);
                    this.addExposure(exposure);
                }
                i++;
            }
        }
        catch (Exception e)
        {
            System.err.println("Error "+e.getMessage());
        }        
        
    }
    
    /**
     *  This method saves the list of exposures to XML file
     *  @param file The file to write to
     */
    public void saveExposures(File file)
    {
        FileWriter          fileWriter;
        BufferedWriter      bufferedWriter;
        PrintWriter         printWriter;
        
        Enumeration         exposures;
        Exposure            exposure;
        
        try
        {
            fileWriter=new FileWriter(file);
            bufferedWriter=new BufferedWriter(fileWriter);
            printWriter=new PrintWriter(bufferedWriter);

            MyXML root = new MyXML("ExposureList");
            
            exposures=theExposures.elements();
            while (exposures.hasMoreElements())
            {
                exposure=(Exposure)exposures.nextElement();
                root.addElement(exposure.getXmlRepresentation());
            }
            
            root.serialize(printWriter);

            printWriter.flush();
            bufferedWriter.flush();
            printWriter.close();

        }
        catch(Exception e)
        {
            System.err.println("Error "+e.getMessage());
        }
        
    }

}