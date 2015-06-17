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
 * Title:        VPM Deco
 * Description:  This software implements the Baker VPM Deco program in Java
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author BJV
 * @version 1.0
 */


/**
 * Class        : IllegalActionException
 * Package      : net.deepocean.vpm.calculator
 * Description  : Exception occuring when code inconsistency is detected
 * Exceptions   :
 *
 * @author        B.J. van der Velde
 * @version       1.0
 *
 *
 */
public class IllegalActionException extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public IllegalActionException()
    {
    }

    public IllegalActionException(String sErrorMessage)
    {
        super(sErrorMessage);
    }

}