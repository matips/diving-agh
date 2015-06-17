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
 *  This class contains the text used throughout the software. These
 *  text basically are the texts used in the tables and exceptions.
 */

public class Text
{
    // Titling
    public static final String sReport00=
        "                          DECOMPRESSION CALCULATION PROGRAM\n";
    public static final String sReport01a=
        "                        Developed in FORTRAN by Erik C. Baker\n";
    public static final String sReport01b=
        "                 Converted to JAVA and extended by Jorgen van der Velde\n";
    public static final String sReport01c=
        "                    DO NOT USE FOR DIVING! EDUCATIONAL PURPOSES ONLY\n";
    public static final String sReport02=
        "\n";

    // Date/time stamp
    public static final String sReport03=
        "Program Run:    {0, date,dd-MM-yyyy} at {1, date,hh:mm aa}"+
        "     Model: {2}\n\n";

    // Description
    public static final String sReport04=
        "Description:    {0}\n\n";

    // Dive table: title
    public static final String sReport05=
        "                                    DIVE PROFILE\n\n";
    public static final String sReport06=
        "Seg-  Segm.  Run   | Gasmix | Ascent    From     To      Rate    | Constant\n";
    public static final String sReport07=
        "ment  Time   Time  |  Used  |   or     Depth   Depth    +Dn/-Up  |  Depth\n";
    public static final String sReport08a=
        "  #   (min)  (min) |    #   | Descent  (fswg)  (fswg)  (fsw/min) |  (fsw)\n";
    public static final String sReport08b=
        "  #   (min)  (min) |    #   | Descent  (mswg)  (mswg)  (msw/min) |  (msw)\n";
    public static final String sReport09=
        "----- -----  ----- | ------ | -------  ------  ------  --------- | --------\n";
    // Dive table: entry varying depth segment
    public static final String sReport10=
        "{0, number,000}   {1, number,000.0} {2, number,0000.0} |   {3, number,00}   | "+
        "{4} {5, number,0000000} {6, number,0000000}   {7, number,00000.0}  |\n";
    public static final String sReport11="Ascent ";
    public static final String sReport12="Descent";
    // Dive table entry constant depth segment
    public static final String sReport13=
        "{0, number,000}   {1, number,000.0} {2, number,0000.0} |   {3, number,00}   | "+
        "                                   | {4, number,0000000}\n";

    // Deco table: title
    public static final String sReport14=
        "\n\n                               DECOMPRESSION PROFILE\n\n";
    public static final String sReport15=
        "          Leading compartment enters the decompression zone at "+
        "{0, number,#####.#} {1}\n";
    public static final String sReport16=
        "                 Deepest possible decompression stop is "+
        "{0, number,#####.#} {1}\n\n";
    public static final String sReport17=
        "Seg-  Segm.  Run   | Gasmix | Ascent   Ascent   Col   |  DECO   STOP   RUN\n";
    public static final String sReport18=
        "ment  Time   Time  |  Used  |   To      Rate    Not   |  STOP   TIME   TIME\n";
    public static final String sReport19=
       "  #   (min)  (min) |    #   | ({0}) ({1})  Used  | ({0})  (min)  (min)\n";

    public static final String sReport20=
        "----- -----  ----- | ------ | ------ --------- ------ | ------ -----  -----\n";
    // Deco table: varying depth segment
    public static final String sReport21=
        "{0, number,000}   {1, number,000.0} {2, number,0000.0} |   {3, number,00}"+
        "   |  {4, number,0000}   {5, number,0000.0}         |\n";
    // Deco table: constant depth segment
    public static final String sReport22=
        "{0, number,000}   {1, number,000.0} {2, number,0000.0} |   {3, number,00}"+
        "   |                         |  {4, number,0000}   {5, number,0000}  "+
        "{6, number,00000}\n";


    public static final String sReport23=
            "Gasmix Summary:                        FO2    FHe    FN2\n";
    public static final String sReport24=
            "                          Gasmix #{0, number,00}  {1, number, 0.000}"+
            "  {2, number,0.000}  {3, number,0.000}\n";

    // units
    public static final String sReport50a="fswg";
    public static final String sReport50b="mswg";
    public static final String sReport51a="fsw/min";
    public static final String sReport51b="msw/min";


    public static final String sIllegalState01="Illegal Gas Mixture Index ";
    public static final String sIllegalState02="Dive height higher than Mount Everest ";

    public Text()
    {
    }
}