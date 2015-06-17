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

import java.io.IOException;
import java.io.Writer;

/**
 * This class represents an diving segment. An diving segment
 * is a part of the dive in which depth is constant or linear
 * varying with time.
 */

public class DepthSegment extends ExposureSegment
{
    /*------------------------------------------------------------------------------------------------*\
     * Variables
    \*------------------------------------------------------------------------------------------------*/    
    protected Length      diveHeight;
    protected Length      diveDepthAtStart;
    protected Length      diveDepthAtEnd;


    /*------------------------------------------------------------------------------------------------*\
     * Construction and reinitialising
    \*------------------------------------------------------------------------------------------------*/    
    public DepthSegment()
    {
    }

    /** Set the height at which the exposure to this segment takes place
     *  @param          diveHeight The dive height
     */
    public void setDiveHeight(Length diveHeight)
    {
        this.diveHeight=(Length)diveHeight.clone();
        initSegment();
    }

/*    
    public void increaseDepth(Length depthIncrement)
    {
        diveDepthAtStart.addLength(depthIncrement);
        diveDepthAtEnd.addLength(depthIncrement);
        initSegment();
    }
*/
    
    /*------------------------------------------------------------------------------------------------*\
     * Get information
    \*------------------------------------------------------------------------------------------------*/    
    /** Gets the height at which the dive takes place
     *  @return         The dive height
     */
    public Length getDiveHeight()
    {
        return diveHeight;
    }

    /** Returns the dive depth at the end of the segment
     *  @return         Dive depth at the end of the segment
     */
    public Length getDepthAtEnd()
    {
        return diveDepthAtEnd;
    }

    /** Returns the dive depth at the start of the segment
     *  @return         The dive depth at the start of the segment
     */
    public Length getDepthAtStart()
    {
        return diveDepthAtStart;
    }

    /*------------------------------------------------------------------------------------------------*\
     * Printing the dive table
    \*------------------------------------------------------------------------------------------------*/    
    /** Print the segment parameters as an entry in the dive table
     *  @param          writer Output stream writer used to write
     *  @exception      java.io.IOException
     */
    public void printDiveTableEntry(Writer writer) throws IOException
    {
    }

    /** Print the segment parameters as an entry in the deco table
     *  @param          writer Output stream writer used to write
     *  @exception      java.io.IOException
     */
    public void printDecoTableEntry(Writer writer) throws IOException
    {
    }


}