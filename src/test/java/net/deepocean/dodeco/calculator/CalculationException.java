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
 * Exception occuring when an error occured during calculation
 */

public class CalculationException extends Exception
{
    private static final long serialVersionUID = 1L;

    public CalculationException()
    {
    }

    public CalculationException(String sErrorMessage)
    {
        super(sErrorMessage);
    }

}
