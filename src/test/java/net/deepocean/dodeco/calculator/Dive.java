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

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 *  This class represents a Dive. A dive consists of a number
 *  of dive segments (DepthSegment child classes) and a
 *  Decompression. The Decompression defines how the decompression
 *  takes place and what algorithm is used to calculate the decompression
 *  profile.
 *
 */

public class Dive extends Exposure
{
    /*------------------------------------------------------------------------------------------------*\
     * Variables
    \*------------------------------------------------------------------------------------------------*/    
    private String                      sDescription;
    private Vector<DepthSegment>        diveSegments;
    private Length                      diveHeight;
    private Decompression               decompression;
    
    private double                      fDiveTime;
    private int                         iSegmentNumber;
    private Length                      decoDepth;
    private Vector<GasMixture>          gasMixtures;

    /*------------------------------------------------------------------------------------------------*\
     * Construction and reinitialising
    \*------------------------------------------------------------------------------------------------*/    
    /**
     * The constructor. Resets variables.
     * @param sDescription A description of the dive.
     * @param diveHeight   Height at which the dive takes place
     */
    public Dive(String sDescription, Length diveHeight)
    {
        diveSegments        =new Vector<DepthSegment>();
        gasMixtures         =new Vector<GasMixture>();
        decompression       =new VpmDecoDecompression();    // create new decompression
        this.sDescription   =sDescription;
        this.diveHeight     =(Length)diveHeight.clone();
    }

    /**
     * The constructor. Resets variables and initialises the dive
     * from an XML representation
     * @param xmlRepresentation A representation in XML of the dive.
     */
    public Dive(MyXML xmlRepresentation) throws MyXMLException, IllegalActionException
    {
        diveSegments        =new Vector<DepthSegment>();
        gasMixtures         =new Vector<GasMixture>();
        decompression       =new VpmDecoDecompression();    // create new decompression

        createFromXmlRepresentation(xmlRepresentation);
    }    
    
    
    
    /**
     * This methods resets the segments. It basically means all segements are
     * removed from the dive.
     */
    public void resetSegments()
    {
        diveSegments.clear();
    }

    /**
     *  This method resets the state of the Exposure, so that 
     *  it can be (re)used for exposing a Diver to it
     */    
    public void resetExposure()
    {
        decompression.resetDecompression();
    }
    
    /** 
     *  This method defines a new decompression for this dive
     *  @param decompression The new decompression
     */
    public void setDecompression(Decompression decompression)
    {
        Vector<DecoStage>   decoStages;
        
        // Get the current set of decompression stages
        decoStages=this.decompression.getDecoStages();
        
        // Set the new decompression
        this.decompression=decompression;

        // Copy the decoStages from previous decompression to the new one
        this.decompression.setDecoStages(decoStages);
    }
    
    /**
     * This method sets the dive height (i.e. height of the surface)
     * @param diveHeight The dive heigth.
     */
    public void setDiveHeight(Length diveHeight)
    {
        DepthSegment    segment;
        Enumeration     elements;

        this.diveHeight=(Length)diveHeight.clone();
        
        elements=diveSegments.elements();
        while (elements.hasMoreElements())
        {
            segment=(DepthSegment)elements.nextElement();
            
            segment.setDiveHeight(diveHeight);
        }
    }
    
    /**
     *  This method sets the description of the dive
     *  @param newDescription The new title/description
     */
    public void setDescription(String newDescription)
    {
        sDescription=newDescription;
    }

    /**
     * This method adds a segment of constant depth. The segment is defined
     * by a period, a depth and a gas mixture.
     * @param iIndex Index in the array. Set to -1 if the item should be appended to the array.
     * @param depth Depth of the segment
     * @param fPeriod Duration of the stay
     * @param gasMixture GasMixture to use for the segment
     * @return The newly added segment
     * @exception IllegalActionException is thrown
     */
    public ConstantDepthSegment addConstantDepthSegment(int iIndex, Length depth, double fPeriod, GasMixture gasMixture)
                                           throws IllegalActionException
    {
        ConstantDepthSegment segment;

        segment=new ConstantDepthSegment(diveHeight, depth, fPeriod, gasMixture);

        addSegment(iIndex, segment);
        
        return segment;
    }

    /**
     * This method adds a segment of constant depth. The segment is defined
     * by a period, a depth and a gas mixture.
     * @param depth Depth of the segment
     * @param fPeriod Duration of the stay
     * @param gasMixture GasMixture to use for the segment
     * @return The newly added segment
     * @exception IllegalActionException is thrown
     */
    public ConstantDepthSegment addConstantDepthSegment(Length depth,
                                                        double fPeriod, 
                                                        GasMixture gasMixture)
                                           throws IllegalActionException
    {
        ConstantDepthSegment segment;
        
        segment=addConstantDepthSegment(-1, depth, fPeriod, gasMixture);
        
        return segment;
    }
    
    

    /**
     * This method adds a varying depth segment. The segment is defined
     * by a start depth, an end depth, a constant rate of change and
     * the GasMixture which is used
     * @param iIndex Index in the array. Set to -1 if the item should be appended to the array.
     * @param startDepth Start depth
     * @param endDepth End depth
     * @param depthChangeRate Rate of change in lengthunit per minute???
     * @param gasMixture GasMixture to use for the segment
     * @exception IllegalActionException is thrown
     */
    public VaryingDepthSegment addVaryingDepthSegment(  int iIndex,
                                                        Length startDepth, Length endDepth,
                                                        Length depthChangeRate, GasMixture gasMixture)
                               throws IllegalActionException
    {
        VaryingDepthSegment segment;

        segment=new VaryingDepthSegment(diveHeight, startDepth, endDepth,
                                        depthChangeRate,
                                        gasMixture);
        addSegment(iIndex, segment);
        
        return segment;
    }

   /**
     * This method adds a varying depth segment. The segment is defined
     * by a start depth, an end depth, a constant rate of change and
     * the GasMixture which is used
     * @param startDepth Start depth
     * @param endDepth End depth
     * @param depthChangeRate Rate of change in lengthunit per minute???
     * @param gasMixture GasMixture to use for the segment
     * @exception IllegalActionException is thrown
     */
    public VaryingDepthSegment addVaryingDepthSegment( Length startDepth, Length endDepth,
                                                       Length depthChangeRate, GasMixture gasMixture)
                                        throws IllegalActionException
    {
        VaryingDepthSegment segment;
        
        segment=addVaryingDepthSegment(-1, startDepth, endDepth, depthChangeRate, gasMixture);
        
        return segment;
    }    
    
    /**
     * This method adds a DepthSegment or sub class of it.
     * @param iIndex Index in the array. Set to -1 if the item should be appended to the array.
     * @param segment The segment to add
     */
    private void addSegment(int iIndex, DepthSegment segment)
    {
        if ((iIndex>=0) && (iIndex<diveSegments.size()))
        {
            diveSegments.add(iIndex, segment);
        }
        else
        {
            diveSegments.add(segment);
        }
    }

   /**
     * This method adds a DepthSegment or sub class of it.
     * @param segment The segment to add
     */
    private void addSegment(DepthSegment segment)
    {
        diveSegments.add(segment);
    }
    

    /**
     * This method indicates a decostage to be added to the dive.
     * It is characterised by the start depth of the stage, the gas mixture,
     * the ascent rate and the deco step size. The deco step size is the
     * depth difference between 2 deco depths.
     * @param iIndex Index in the array. Set to -1 if the item should be appended to the array.
     * @param startDepth 
     * @param gasMixture GasMixture to use for this decostage
     * @param ascentRate Ascent rate to use between decosteps
     * @param decoStepSize Distance between two subsequent deco stops
     * @return The newly added DecoStage instance
     */
    public DecoStage addDecoStage(  int iIndex,
                                    Length startDepth, 
                                    Length endDepth,
                                    Length ascentRate, 
                                    GasMixture gasMixture,
                                    Length decoStepSize)
                             throws IllegalActionException
    {
        DecoStage decoStage;

        decoStage=new DecoStage(startDepth, endDepth, 
                                ascentRate, gasMixture, decoStepSize);
        
        decompression.addDecompressionStage(iIndex, decoStage);
        
        return decoStage;
    }

    /**
     * This method indicates a decostage to be added to the dive.
     * It is characterised by the start depth of the stage, the gas mixture,
     * the ascent rate and the deco step size. The deco step size is the
     * depth difference between 2 deco depths.
     * @param startDepth 
     * @param gasMixture GasMixture to use for this decostage
     * @param ascentRate Ascent rate to use between decosteps
     * @param decoStepSize Distance between two subsequent deco stops
     * @return The newly added DecoStage instance
     */
    public DecoStage addDecoStage(  Length startDepth, 
                                    Length endDepth,
                                    Length ascentRate, 
                                    GasMixture gasMixture,
                                    Length decoStepSize)
                             throws IllegalActionException
    {
        DecoStage decoStage;

        decoStage=new DecoStage(startDepth, endDepth, 
                                ascentRate, gasMixture, decoStepSize);
        
        decompression.addDecompressionStage(decoStage);
        
        return decoStage;
    }
    
    /*------------------------------------------------------------------------------------------------*\
     * Get information
    \*------------------------------------------------------------------------------------------------*/    
    /**
     *  This method returns the description of the dive.
     *  The description is a short line that is shown 
     *  above the dive in the deco table
     *  @return The description
     */
    public String getDescription()
    {
        return sDescription;
    }

    public Vector<DecoStage> getDecoStages()
    {
        return decompression.getDecoStages();
    }
    /**
     *  This method returns the name of the exposure
     *  @return String indicating the name of the exposure
     */
    public String getExposureName()
    {
        return new String("Dive");
    }
    
    public Length getDiveHeight()
    {
        return this.diveHeight;
    }
    
    
    
    
    
    /*------------------------------------------------------------------------------------------------*\
     * Calculation
    \*------------------------------------------------------------------------------------------------*/    
    /**
     *  This method creates a list of all the different GasMixtures
     *  used during this Dive and Decompression
     */  
    public void createGasMixtureList()
    {
        Enumeration     segmentElements;
        DepthSegment    segment;
        Enumeration     decoElements;
        DecoStage       decoStage;
        GasMixture      mixture;
        GasMixture      segmentMixture;
        Enumeration     mixtureElements;
        int             iHeliumPercentage;
        int             iOxygenPercentage;
        boolean         bFound;
        int             iIndex;
        int             iNewIndexCounter;
        GasMixture      newGasMixture;
        
        // Clear current list with GasMixtures
        gasMixtures.clear();

        // Index of the GasMixture is the position in the GasMixture list
        // It is just a number used for reporting
        iNewIndexCounter=1;        
        
        // Now first parse all the dive segments
        segmentElements=diveSegments.elements();
        while (segmentElements.hasMoreElements())                                                   // process all segments
        {
            // For each segment, get the GasMixture and gas fractions of O2 and He2
            segment=(DepthSegment)segmentElements.nextElement();  
            segmentMixture=segment.getGasMixture();
            iHeliumPercentage=segmentMixture.getHe2Percentage();
            iOxygenPercentage=segmentMixture.getO2Percentage();
            
            // Now check if this mixture already occurs in the list
            bFound=false;

            mixtureElements=gasMixtures.elements();
            while (mixtureElements.hasMoreElements() && !bFound)
            {
                mixture=(GasMixture)mixtureElements.nextElement();
                
                if ((mixture.getHe2Percentage()==iHeliumPercentage) &&
                    (mixture.getO2Percentage()==iOxygenPercentage))
                {
                    // Allright, the Segments' Mixture is in the list
                    // Now tag the Segments' Mixture with the index from the list
                    iIndex=mixture.getIndex();
                    segmentMixture.setIndex(iIndex);
                    bFound=true;
                }

            }
            // If the GasMixture does not occur, add the GasMixture to the list
            if (!bFound)
            {
                newGasMixture=(GasMixture)segmentMixture.clone();
                newGasMixture.setIndex(iNewIndexCounter);
                segmentMixture.setIndex(iNewIndexCounter);
                gasMixtures.add(newGasMixture);
                iNewIndexCounter++;
                
            }
            
        }
        
        // Now get the DecoStages and parse them
        decoElements=decompression.getDecoStages().elements();
        while (decoElements.hasMoreElements())                                                   // process all segments
        {
            // For each DecoStage get the GasMixture and O2 and He2 fractions
            decoStage=(DecoStage)decoElements.nextElement();  
            segmentMixture=decoStage.getGasMixture();
            iHeliumPercentage=segmentMixture.getHe2Percentage();
            iOxygenPercentage=segmentMixture.getO2Percentage();
            
            // Check if the mixture already occurs in the list
            bFound=false;
            mixtureElements=gasMixtures.elements();
            while (mixtureElements.hasMoreElements() && !bFound)
            {
                mixture=(GasMixture)mixtureElements.nextElement();
                
                if ((mixture.getHe2Percentage()==iHeliumPercentage) &&
                    (mixture.getO2Percentage()==iOxygenPercentage))
                {
                    // Allright, the Segments' Mixture is in the list
                    // Now tag the Segments' Mixture with the index from the list
                    iIndex=mixture.getIndex();
                    segmentMixture.setIndex(iIndex);                    
                    bFound=true;
                }
            }
            
            // If the GasMixture does not occur, add it to the list
            if (!bFound)
            {
                newGasMixture=(GasMixture)segmentMixture.clone();
                newGasMixture.setIndex(iNewIndexCounter);
                segmentMixture.setIndex(iNewIndexCounter);                
                gasMixtures.add((GasMixture)segmentMixture.clone());
                iNewIndexCounter++;                
            }
            
        }
        
    }

    /** Updates the diver for this exposure. The decompression profile is
     *  calculated as part of this method
     *  @param          diver The diver
     *  @param          fRunTime The runtime at the start of the dive
     *  @exception      CalculationException
     */
    public void exposeDiver(Diver diver, double fRunTime) throws CalculationException
    {
        createGasMixtureList();                                 // Create an overview of the GasMixtures used, for reporting
        
        fDiveTime       =0.0;                                   // reset current dive time
        this.fRunTime   =fRunTime;
        iSegmentNumber  =1;                                     // reset segment numbering
        diver.resetBeforeDive();
        exposeDiverToDiveSegments(diver);                       // update diver for dive segments
        regenerateNuclei(diver);                                // regenerate VPM critical nuclei

        if (decompression.getDecoStages().size() > 0) {
            decompression.decompressDiver(diver, diveSegments,      // calculate the deco profile
                    this.fRunTime);
            fRunTime = decompression.getRunTime();
            iSegmentNumber = decompression.getSegmentNumber();
        }
    }

    /** Updates the diver for exposure to the dive segments (up to the start
     *  of decompression, decompression not included).
     *  @param          diver The diver
     *  @param          fRunTime The runtime at the start of the exposure
     *  @return         -
     *  @exception      CalculationException
     */
    private void exposeDiverToDiveSegments(Diver diver)
                throws CalculationException
    {
        TissueCompartment   compartment;
        Vector              compartments;
        Enumeration         elements;
        
        DepthSegment        segment;
        Enumeration         segmentElements;
        double              fSegmentRunTime;


        segmentElements=diveSegments.elements();

        while (segmentElements.hasMoreElements())                                                   // process all segments
        {
            segment=(DepthSegment)segmentElements.nextElement();
            
            segment.setSegmentNumber(iSegmentNumber);
            segment.setRunTime(fRunTime);
            
            compartments=diver.getCompartments();
            elements=compartments.elements();
            while (elements.hasMoreElements())
            {
                compartment=(TissueCompartment)elements.nextElement();
                
                segment.setSegmentNumber(iSegmentNumber);
                segment.setRunTime(fRunTime);
                segment.exposeTissueCompartment(compartment);
                segment.calculateCrushingPressure(compartment);
            }
//            decoDepth=segment.getDepthAtEnd();
            fRunTime        +=segment.getExposurePeriod();
            fDiveTime       +=segment.getExposurePeriod();
            iSegmentNumber  ++;
        }
    }

    /**
     *
     */
    private void regenerateNuclei(Diver diver)
    {
        TissueCompartment   compartment;
        Vector              compartments;
        Enumeration         elements;        

        compartments=diver.getCompartments();
        elements=compartments.elements();
        while (elements.hasMoreElements())
        {
            compartment=(TissueCompartment)elements.nextElement();           
            compartment.regenerateNuclei(fDiveTime);
        }
    }

    /*------------------------------------------------------------------------------------------------*\
     * Printing the dive table
    \*------------------------------------------------------------------------------------------------*/    
    /**
     * This method prints an overview of the dive. It prints
     * an overview of the gas mixtures, the dive and the decompression
     * profile
     * @param writer Output stream writer to use for the printing.
     */
    public void printExposure(Writer writer) throws IOException
    {

        Date                date;
        SimpleDateFormat    dateFormat;
        SimpleDateFormat    timeFormat;


        dateFormat=new SimpleDateFormat("dd-MM-yyyy");
        timeFormat=new SimpleDateFormat("hh:mm aa");
        date=new Date(System.currentTimeMillis());

        writer.write(Text.sReport00);
        writer.write(Text.sReport01a);
        writer.write(Text.sReport01b);
        writer.write(Text.sReport01c);
        writer.write(Text.sReport02);

        Object[]            args03={date, date, decompression.getAlgorithmDescription()};
        writer.write(MessageFormat.format(Text.sReport03, args03));
        Object[]            args04={sDescription};
        writer.write(MessageFormat.format(Text.sReport04, args04));

        printGasMixtures(writer);

        printDiveTable(writer);
        decompression.printDecoTable(writer);
    }

    public void printDiveTable(Writer writer) throws IOException
    {
        DepthSegment segment;
        Enumeration  elements;

        writer.write(Text.sReport05);
        writer.write(Text.sReport06);
        writer.write(Text.sReport07);
        if (Parameters.iPresentationPressureUnits==Pressure.UNITS_FSW)
        {
            writer.write(Text.sReport08a);
        }
        else
        {
            writer.write(Text.sReport08b);
        }
        writer.write(Text.sReport09);



        elements=diveSegments.elements();
        while (elements.hasMoreElements())
        {
            segment=(DepthSegment)elements.nextElement();
            segment.printDiveTableEntry(writer);
        }
        writer.write("\n");

    }

    public void printGasMixtures(Writer writer) throws IOException
    {
        GasMixture          gasMixture;
        Enumeration         elements;
        Object[]            args;
        int                 i;

        args=new Object[4];

        writer.write(Text.sReport23);
        elements=gasMixtures.elements();
        i=1;
        while (elements.hasMoreElements())
        {
            gasMixture=(GasMixture)elements.nextElement();
            args[0]=new Integer(gasMixture.getIndex());
            args[1]=new Double(gasMixture.getO2Fraction());
            args[2]=new Double(gasMixture.getHe2Fraction());
            args[3]=new Double(gasMixture.getN2Fraction());
            writer.write(MessageFormat.format(Text.sReport24, args));

            i++;
        }
        writer.write("\n");
    }

    public Vector<DepthSegment> getDiveSegments()
    {
        return diveSegments;
    }
    


    /*------------------------------------------------------------------------------------------------*\
     * XML parsing and writing
    \*------------------------------------------------------------------------------------------------*/
    /**
     *  This method creates an XML representation of the Dive
     *  @return The MyXML instance representing the Dive
     */
    public MyXML getXmlRepresentation() throws MyXMLException
    {
        MyXML               xmlDive;
        MyXML               xmlHeight;
        MyXML               xmlSegments;
        MyXML               xmlDecoStages;
        Enumeration         segments;
        DepthSegment        segment;
        Vector<DecoStage>   decoStages;
        Enumeration         stages;
        DecoStage           stage;
        
        xmlDive=new MyXML("Dive");

        // Add the description
        xmlDive.addElement("Description", sDescription);
        
        // Add the diveHeight
        xmlHeight=xmlDive.addElement("DiveHeight");
        xmlHeight.addElement(diveHeight.getXmlRepresentation());
        
        
        // Process the DiveSegments
        xmlSegments=xmlDive.addElement("DiveSegments");
        
        segments=diveSegments.elements();
        while (segments.hasMoreElements())
        {
            segment=(DepthSegment)segments.nextElement();
            
            xmlSegments.addElement(segment.getXmlRepresentation());
            
        }
        
        // Process the DecoStages
        xmlDecoStages=xmlDive.addElement("DecompressionStages");        
        
        decoStages=decompression.getDecoStages();
        stages=decoStages.elements();
        while (stages.hasMoreElements())
        {
            stage=(DecoStage)stages.nextElement();
            
            xmlDecoStages.addElement(stage.getXmlRepresentation());
        }
        
        return xmlDive;
    }    
    
    /**
     *  This method initialises the parameters characterising the Dive.
     *  The values are retrieved from the XML representation.
     *  @param xmlRepresentation Representation of the Dive
     */
    public void createFromXmlRepresentation(MyXML xmlRepresentation) throws MyXMLException, IllegalActionException
    {
        MyXML           xmlHeight;
        MyXML           xmlLength;
        MyXML           xmlSegments;
        MyXML           xmlSegment;
        MyXML           xmlDecoStages;
        MyXML           xmlDecoStage;
        DepthSegment    segment;
        DecoStage       decoStage;
        int             i;
        
        // Get the description
        sDescription=xmlRepresentation.findElement("Description").getValue();
        
        // Get the DiveHeight
        xmlHeight=xmlRepresentation.findElement("DiveHeight");
        xmlLength=xmlHeight.findElement("Length");
        diveHeight=new Length(xmlLength);

        // Get the Dive DepthSegments
        xmlSegments=xmlRepresentation.findElement("DiveSegments");
        i=0;
        while (i<xmlSegments.size())
        {
            xmlSegment=xmlSegments.getElement(i);
            if (xmlSegment.getTag().equals("StayAtDepth"))
            {
                segment=new ConstantDepthSegment(diveHeight, xmlSegment);
                diveSegments.add(segment);
            }
            else if (xmlSegment.getTag().equals("DepthChange"))
            {
                segment=new VaryingDepthSegment(diveHeight, xmlSegment);
                diveSegments.add(segment);                
            }
            i++;
        }
            
        // Get the DecoStages
        xmlDecoStages=xmlRepresentation.findElement("DecompressionStages");
        i=0;
        while (i<xmlDecoStages.size())
        {
            xmlDecoStage=xmlDecoStages.getElement(i);
            decoStage=new DecoStage(xmlDecoStage);
            decompression.addDecompressionStage(decoStage);
            i++;
        }
            
        
        
    }    

}