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

/**
 * Class        : HeightSegment
 * Package      : net.deepocean.vpm.calculator
 * Description  : This class represents an surface segment.
 * Exceptions   :
 *
 * @author        B.J. van der Velde
 * @version       1.0
 *
 *
 */

public class HeightSegment extends ExposureSegment
{
    Length heightAtEnd;
    Length heightAtStart;


    public HeightSegment()
    {
        
    }
    

    /** Returns the height above sea level at the end of the segment
     *  @return         Height above sealevel at the end of the segment
     */
    public Length getHeightAtEnd()
    {
        return heightAtEnd;
    }

    /** Returns the height above sea level at the start of the segment
     *  @return         Height at the start of the segment
     */
    public Length getHeightAtStart()
    {
        return heightAtStart;
    }
}