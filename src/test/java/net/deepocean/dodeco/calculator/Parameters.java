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
import net.deepocean.dodeco.tools.SettingsFile;

import java.io.*;

/**
 * Class        : Parameters
 * Package      : net.deepocean.vpm.calculator
 * Description  : This class contains relevant parameters.
 * Exceptions   :
 *
 * @author        B.J. van der Velde
 * @version       1.0
 *
 *
 */

public class Parameters
{
    // Name of the parameter file
    public static final String      SETTINGS_FILENAME="vpmdeco.set";

    private static String           label;
    private static String           value;
 
    
    //######################################################################
    // GAS LOADING
    //######################################################################
    
    // Number of compartments
    public static final int         nCompartments=16;


    // Nitrogen tissue compartment halftimes
    public static double[]          fHalfTimeN2=
        {
            5.,    8.,  12.5, 18.5,
            27.,  38.3, 54.3, 77.,
            109.,146., 187.,  239.,
            305.,390., 498.,  635.
        };

    // Helium tissue compartment halftimes
    public static double[]          fHalfTimeHe2=
        {
            1.88,   3.02,   4.72,   6.99,
            10.21,  14.48,  20.53,  29.11,
            41.2,   55.19,  70.69,  90.34,
            115.29, 147.42, 188.24, 240.03
        };

    /** H2O pressure at 37 degrees celsius */
    public static Pressure pressureH2O=new Pressure(0.0627, Pressure.UNITS_BAR);                // bar

    // CO2 pressure in the lungs
    public static Pressure pressureCO2=new Pressure(0.0534, Pressure.UNITS_BAR);                // bar

    // pressure other gases in the tissue
    public static Pressure pressureOtherGasses=new Pressure(102.0*0.001333223,
                                                                        Pressure.UNITS_BAR);            // bar (=102 mm Hg)
    
    // Respiratory Coefficient
    public static double            fRq=0.8;                            //

    
    //######################################################################
    // VPM Models
    //######################################################################
    
    // surface tension
    public static double            fGamma=0.0179;                      // N/m

    // crumbling compression
    public static double            fGammaC=0.257;                      // N/m

    /** VPM parameter lambda (fsw-min) */
    public static Pressure lambda=new Pressure(7500.0,
                                                         Pressure.UNITS_FSW);

    /** Initial critical radius N2 nuclei (m) */
    public static Length            initialCriticalRadiusN2=new Length(0.8e-6, Length.UNITS_METER);
//    public static double            fInitialCriticalRadiusN2=0.8e-6;    // m
//    public static final double      fInitialCriticalRadiusN2=0.55e-6;    // m, HLPlanner setting

    /** Initial critical radius He2 nuclei (m) */
    public static Length            initialCriticalRadiusHe2=new Length(0.7e-6, Length.UNITS_METER);
//    public static double            fInitialCriticalRadiusHe2=0.7e-6;   // m
//    public static final double      fInitialCriticalRadiusHe2=0.45e-6;   // m, HLPlanner setting

    /** Indicates whether critical volume algorithm should be applied */
    public static boolean           bCriticalVolumeAlgorithm=true;

    /** Indicates whether altitude dive algorithm should be applied */
//    public static boolean           bAltitudeDiveAlgorithm=false;

    /** Nuclei regeneration time constant */
    public static double            fRegenTimeConstant=20160.0;         // min

    /** Gradient onset of impermeability */
    public static Pressure gradientOnsetOfImpermeability=
                                    new Pressure(8.2*1.01325, Pressure.UNITS_BAR); //bar

    //######################################################################
    // BUHLMANN MODELS
    //######################################################################
    
    /** The Helium Buhlman A factors for the tissue compartments in Bar. Defined by Ptol = Pamb/B + A */
    public static double[] He2A=
    {
      1.6187, 1.3830, 1.1919, 1.0485, 0.9220,
      0.8205, 0.7305, 0.6502, 0.5950, 0.5545,
      0.5333, 0.5189, 0.5181, 0.5176, 0.5172,
      0.5119
    };

    /** The Helium Buhlmann B factors (dimensionless) */

    public static double[] He2B=
    {
      0.4770, 0.5747, 0.6527, 0.7223, 0.7582,
      0.7957, 0.8279, 0.8553, 0.8757, 0.8903,
      0.8997, 0.9073, 0.9122, 0.9171, 0.9217,
      0.9267
    };

    
    /** The Nitrogen Buhlmann a factors of ZH-L16A series in Bar*/
    public static double[] N2A_ASeries=
    {
      1.1696, 1.0000, 0.8618, 0.7562, 0.6667,
      0.5933, 0.5282, 0.4701, 0.4187, 0.3798,
      0.3497, 0.3223, 0.2971, 0.2737, 0.2523,
      0.2327
    };

    /** The Nitrogen Buhlmann a factors of ZH-L16B series in Bar*/
    public static double[] N2A_BSeries=
    {
      1.1696, 1.0000, 0.8618, 0.7562, 0.6667,
      0.5600, 0.4947, 0.4500, 0.4187, 0.3798,
      0.3497, 0.3223, 0.2850, 0.2737, 0.2523,
      0.2327
    };

    /** The Nitrogen Buhlmann a factors of ZH-L16C series in Bar*/
    public static double[] N2A_CSeries=
    {
      1.1696, 1.0000, 0.8618, 0.7562, 0.6200,
      0.5043, 0.4410, 0.4000, 0.3750, 0.3500,
      0.3295, 0.3065, 0.2835, 0.2610, 0.2480,
      0.2327
    };

    
    /** The Nitrogen Buhlmann b factors (dimensionless) */

    public static double[] N2B=
    {
      0.5578, 0.6514, 0.7222, 0.7725, 0.8125,
      0.8434, 0.8693, 0.8910, 0.9092, 0.9222,
      0.9319, 0.9403, 0.9477, 0.9544, 0.9602,
      0.9653
    };
    
    
    
    //######################################################################
    // BUHLMANN WITH GRADIENT MODEL
    //######################################################################
    
    /** This is the gradient factor (conservatism factor) at 
     *  the deepest deco stop
     */
    public static double          fLowGradientFactor=0.30;
    
    /**
     *  This is the gradient factor at the surface
     */
    public static double          fHighGradientFactor=0.75;  
    
    //######################################################################
    // GENERAL
    //######################################################################
    
    
    /** Units for pressentation Pressure.UNITS_FSW or Pressure.UNITS_MSW */
    public static int               iPresentationPressureUnits= Pressure.UNITS_FSW;

    /** Units used for Length */
    public static int               iLengthUnits=Length.UNITS_FEET;

    /** Minimum deco stop time: deco stop time unit (min)*/
    public static double            fMinimumDecoStopTime=1.0;           // min

    /** Ascent rate in feet/min */
    public static Length            ascentRate=new Length(-10.0, Length.UNITS_FEET);
    
    /** Descent rate in feet/min */
    public static Length            descentRate=new Length(75.0, Length.UNITS_FEET);
    
    
    public static Length            decoStepSize=new Length(10.0, Length.UNITS_FEET);

    
    
    
    
    
    
 
    private Parameters()
    {
    }



    public static void initialize() throws IllegalActionException
    {
        boolean bExit;

        SettingsFile file=new SettingsFile(SETTINGS_FILENAME);
        bExit=false;
        while (!bExit)
        {
            file.reset();
            label=file.getLabel();
            value=file.getValue();
            if (label!=null)
            {
                parseLabelAndValue();
            }
            else
            {
                bExit=true;
            }
        }
    }



    private static void parseLabelAndValue() throws IllegalActionException
    {
/*        
        if (label.equals("Units"))
        {
            if (value.equals("'fsw'") || value.equals("'FSW'"))
            {
                iPresentationPressureUnits=Pressure.UNITS_FSW;
            }
            if (value.equals("'msw'") || value.equals("'MSW'"))
            {
                iPresentationPressureUnits=Pressure.UNITS_MSW;
            }
        }

        else if (label.equals("Altitude_Dive_Algorithm"))
        {
            if (value.equals("'on'") || value.equals("'ON'"))
            {
                bAltitudeDiveAlgorithm=true;
            }
            if (value.equals("'off'") || value.equals("'OFF'"))
            {
                bAltitudeDiveAlgorithm=false;
            }
        }
*/

    }

    /**
     *  This method writes user configurable settings to file.
     *  Settings are written in XML format
     *  @param file File to write the settings to
     */
    public static void saveSettingsFile(File file)
    {
        FileWriter          fileWriter;
        BufferedWriter      bufferedWriter;
        PrintWriter         printWriter;
        MyXML               xmlHe2Halftimes;
        MyXML               xmlN2Halftimes;
        MyXML               xmlPressure;
        MyXML               xmlLength;
        MyXML               xmlCoefficients;
        
        MyXML               xmlGasLoading;
        MyXML               xmlVpm;
        MyXML               xmlBuhlmann;
        MyXML               xmlBuhlmannGradient;
        MyXML               xmlDecompression;
        int                 i;
        
        try
        {
            fileWriter=new FileWriter(file);
            bufferedWriter=new BufferedWriter(fileWriter);
            printWriter=new PrintWriter(bufferedWriter);

            MyXML root = new MyXML("Settings");
            
            
            // *** SECTION DECOMPRESSION GENERAL

            xmlDecompression=root.addElement("GeneralDecompressionSettings");

            if (iLengthUnits==Length.UNITS_METER)
            {
                xmlDecompression.addElement("LengthUnits", "Meter");
               
            }
            else if (iLengthUnits==Length.UNITS_FEET)
            {
                xmlDecompression.addElement("LengthUnits", "Feet");
            }
               
            
            xmlDecompression.addElement("MinimumDecoStopTimeInMinutes", Double.toString(fMinimumDecoStopTime));
            
            xmlLength=xmlDecompression.addElement("AscentRate");
            xmlLength.addElement(ascentRate.getXmlRepresentation(iLengthUnits));

            xmlLength=xmlDecompression.addElement("DescentRate");
            xmlLength.addElement(descentRate.getXmlRepresentation(iLengthUnits));

            xmlLength=xmlDecompression.addElement("DecompressionDepthIncrement");
            xmlLength.addElement(decoStepSize.getXmlRepresentation(iLengthUnits));
            
            // *** SECTION GAS LOADING
            xmlGasLoading=root.addElement("GasLoadingSettings");
            
            xmlN2Halftimes=xmlGasLoading.addElement("NitrogenHalftimesInMinutes");
            i=0;
            while (i<16)
            {
                xmlN2Halftimes.addElement("TissueCompartment"+i, Double.toString(fHalfTimeN2[i]));
                i++;
            }
            xmlHe2Halftimes=xmlGasLoading.addElement("HeliumHalftimesInMinutes");
            i=0;
            while (i<16)
            {
                xmlHe2Halftimes.addElement("TissueCompartment"+i, Double.toString(fHalfTimeHe2[i]));
                i++;
            }
            
            
            xmlPressure=xmlGasLoading.addElement("PressureH2O");
            xmlPressure.addElement(pressureH2O.getXmlRepresentation());
            
            xmlPressure=xmlGasLoading.addElement("PressureCO2");
            xmlPressure.addElement(pressureCO2.getXmlRepresentation());
            
            xmlPressure=xmlGasLoading.addElement("PressureOtherGasses");
            xmlPressure.addElement(pressureOtherGasses.getXmlRepresentation());

            xmlGasLoading.addElement("RespiratoryCoefficient", Double.toString(fRq));

            
            // *** SECTION VPM
            
            xmlVpm=root.addElement("VpmSettings");
            
            xmlVpm.addElement("SurfaceTensionGamma", Double.toString(fGamma));
            xmlVpm.addElement("SkinCompressionGammaC", Double.toString(fGammaC));

            xmlPressure=xmlVpm.addElement("CriticalVolumeParameterLambda");
            xmlPressure.addElement(lambda.getXmlRepresentation());
            
            
            xmlLength=xmlVpm.addElement("InitialCriticalRadiusNitrogen");
            xmlLength.addElement(initialCriticalRadiusN2.getXmlRepresentation(iLengthUnits));
            
            xmlLength=xmlVpm.addElement("InitialCriticalRadiusHelium");
            xmlLength.addElement(initialCriticalRadiusHe2.getXmlRepresentation(iLengthUnits));

            xmlVpm.addElement("CriticalVolumeAlgorithm", Boolean.toString(bCriticalVolumeAlgorithm));
            
            xmlVpm.addElement("RegenerationTimeConstantInMinutes", Double.toString(fRegenTimeConstant));
            
            xmlPressure=xmlVpm.addElement("OnsetOfImpermeability");
            xmlPressure.addElement(gradientOnsetOfImpermeability.getXmlRepresentation());
            
            // *** SECTION BUHLMANN
            
            xmlBuhlmann=root.addElement("BuhlmannSettings");

            xmlCoefficients=xmlBuhlmann.addElement("CoefficientsHeliumInBar");
            i=0;
            while (i<16)
            {
                xmlCoefficients.addElement("a"+i, Double.toString(He2A[i]));
                i++;
            }            
            
            i=0;
            while (i<16)
            {
                xmlCoefficients.addElement("b"+i, Double.toString(He2B[i]));
                i++;
            }            
            
            xmlCoefficients=xmlBuhlmann.addElement("CoefficientsNitrogenInBar");
            i=0;
            while (i<16)
            {
                xmlCoefficients.addElement("a_ZHL16A"+i, Double.toString(N2A_ASeries[i]));
                i++;
            }            
            
            i=0;
            while (i<16)
            {
                xmlCoefficients.addElement("a_ZHL16B"+i, Double.toString(N2A_BSeries[i]));
                i++;
            }            
            
            i=0;
            while (i<16)
            {
                xmlCoefficients.addElement("a_ZHL16C"+i, Double.toString(N2A_CSeries[i]));
                i++;
            }            
           
            i=0;
            while (i<16)
            {
                xmlCoefficients.addElement("b"+i, Double.toString(N2B[i]));
                i++;
            }            
            
            // *** SECTION BUHLMANN WITH BAKER GRADIENT FACTORS
            
            xmlBuhlmannGradient=root.addElement("BuhlmannWithGradientFactorSettings");

            xmlBuhlmannGradient.addElement("GradientFactorAtFirstStop", fLowGradientFactor);
            xmlBuhlmannGradient.addElement("GradientFactorAtSurface"  , fHighGradientFactor);
            
            
            
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

    /** 
     *  This method loads user configurable settings from file
     *  @param file File to read from
     */
    public static void loadSettingsFile(File file)
    {
        FileReader          reader;
        BufferedReader      bufferedReader;
        MyXML               root;
        MyXML               xmlGasLoading;
        MyXML               xmlVpm;
        MyXML               xmlDecompression;
        MyXML               xmlBuhlmann;
        MyXML               xmlBuhlmannWithGradient;
        
        MyXML               xmlCoefficients;
        
        
        MyXML               xmlHe2Halftimes;
        MyXML               xmlN2Halftimes;
        MyXML               xmlValue;
        MyXML               xmlLength;
        MyXML               xmlPressure;
        int                 i;
        String              tag;
        Exposure            exposure;
        
        try
        {
            reader          =new FileReader(file);
            bufferedReader  =new BufferedReader(reader);
            root            =new MyXML(bufferedReader);
            
            xmlGasLoading           =root.findElement("GasLoadingSettings");
            xmlVpm                  =root.findElement("VpmSettings");
            xmlDecompression        =root.findElement("GeneralDecompressionSettings");
            xmlBuhlmann             =root.findElement("BuhlmannSettings");
            xmlBuhlmannWithGradient =root.findElement("BuhlmannWithGradientFactorSettings");

            // *** GENERAL DECOMPRESSION SETTINGS
           
            xmlValue=xmlDecompression.findElement("LengthUnits");
            if (xmlValue.getValue().equals("Meter"))
            {
                iLengthUnits=Length.UNITS_METER;
                iPresentationPressureUnits= Pressure.UNITS_MSW;
            }
            else if (xmlValue.getValue().equals("Feet"))
            {
                iLengthUnits=Length.UNITS_FEET;
                iPresentationPressureUnits= Pressure.UNITS_FSW;
            }
            
            xmlValue=xmlDecompression.findElement("MinimumDecoStopTimeInMinutes");
            fMinimumDecoStopTime=xmlValue.getValueAsDouble();
            
            xmlValue=xmlDecompression.findElement("AscentRate");
            ascentRate.createFromXmlRepresentation(xmlValue.findElement("Length"));
            
            xmlValue=xmlDecompression.findElement("DescentRate");
            descentRate.createFromXmlRepresentation(xmlValue.findElement("Length"));
            
            xmlValue=xmlDecompression.findElement("DecompressionDepthIncrement");
            decoStepSize.createFromXmlRepresentation(xmlValue.findElement("Length"));

            // *** GAS LOADING SECTION
            xmlN2Halftimes  =xmlGasLoading.findElement("NitrogenHalftimesInMinutes");
            if (xmlN2Halftimes!=null)
            {
                i=0;
                while (i<16)
                {
                    xmlValue=xmlN2Halftimes.findElement("TissueCompartment"+i);
                    fHalfTimeN2[i]=xmlValue.getValueAsDouble();
                    i++;
                }
            }
            
            xmlHe2Halftimes  =xmlGasLoading.findElement("HeliumHalftimesInMinutes");
            if (xmlHe2Halftimes!=null)
            {
                i=0;
                while (i<16)
                {
                    xmlValue=xmlHe2Halftimes.findElement("TissueCompartment"+i);
                    fHalfTimeHe2[i]=xmlValue.getValueAsDouble();
                    i++;
                }
            }
            
            xmlValue=xmlGasLoading.findElement("PressureH2O");
            pressureH2O.createFromXmlRepresentation(xmlValue.findElement("Pressure"));
            
            xmlValue=xmlGasLoading.findElement("PressureCO2");
            pressureCO2.createFromXmlRepresentation(xmlValue.findElement("Pressure"));
            
            xmlValue=xmlGasLoading.findElement("PressureOtherGasses");
            pressureOtherGasses.createFromXmlRepresentation(xmlValue.findElement("Pressure"));
            
            xmlValue=xmlGasLoading.findElement("RespiratoryCoefficient");
            fRq=xmlValue.getValueAsDouble();
 
            // *** VPM SETTINGS SECTION
            
            xmlValue=xmlVpm.findElement("SurfaceTensionGamma");
            fGamma=xmlValue.getValueAsDouble();
            
            xmlValue=xmlVpm.findElement("SkinCompressionGammaC");
            fGammaC=xmlValue.getValueAsDouble();

            xmlValue=xmlVpm.findElement("CriticalVolumeParameterLambda");
            lambda.createFromXmlRepresentation(xmlValue.findElement("Pressure"));
            
            xmlValue=xmlVpm.findElement("InitialCriticalRadiusNitrogen");
            initialCriticalRadiusN2.createFromXmlRepresentation(xmlValue.findElement("Length"));
            
            xmlValue=xmlVpm.findElement("InitialCriticalRadiusHelium");
            initialCriticalRadiusHe2.createFromXmlRepresentation(xmlValue.findElement("Length"));
            
            xmlValue=xmlVpm.findElement("CriticalVolumeAlgorithm");
            bCriticalVolumeAlgorithm=Boolean.valueOf(xmlValue.getValue());
            
            xmlValue=xmlVpm.findElement("RegenerationTimeConstantInMinutes");
            fRegenTimeConstant=xmlValue.getValueAsDouble();
            
            xmlValue=xmlVpm.findElement("OnsetOfImpermeability");
            gradientOnsetOfImpermeability.createFromXmlRepresentation(xmlValue.findElement("Pressure"));
            
            // Buhlmann model section
            xmlCoefficients =xmlBuhlmann.findElement("CoefficientsHeliumInBar");
            if (xmlCoefficients!=null)
            {
                i=0;
                while (i<16)
                {
                    xmlValue=xmlCoefficients.findElement("a"+i);
                    He2A[i]=xmlValue.getValueAsDouble();
                    i++;
                }
                i=0;
                while (i<16)
                {
                    xmlValue=xmlCoefficients.findElement("b"+i);
                    He2B[i]=xmlValue.getValueAsDouble();
                    i++;
                }
            }
            xmlCoefficients =xmlBuhlmann.findElement("CoefficientsNitrogenInBar");
            if (xmlCoefficients!=null)
            {
                i=0;
                while (i<16)
                {
                    xmlValue=xmlCoefficients.findElement("a_ZHL16A"+i);
                    N2A_ASeries[i]=xmlValue.getValueAsDouble();
                    i++;
                }
                i=0;
                while (i<16)
                {
                    xmlValue=xmlCoefficients.findElement("a_ZHL16B"+i);
                    N2A_BSeries[i]=xmlValue.getValueAsDouble();
                    i++;
                }
                i=0;
                while (i<16)
                {
                    xmlValue=xmlCoefficients.findElement("a_ZHL16C"+i);
                    N2A_CSeries[i]=xmlValue.getValueAsDouble();
                    i++;
                }
                i=0;
                while (i<16)
                {
                    xmlValue=xmlCoefficients.findElement("b"+i);
                    N2B[i]=xmlValue.getValueAsDouble();
                    i++;
                }
            }
            
            // Buhlmann with gradient factor
            
            xmlValue=xmlBuhlmannWithGradient.findElement("GradientFactorAtFirstStop");
            fLowGradientFactor=xmlValue.getValueAsDouble();       
            
            xmlValue=xmlBuhlmannWithGradient.findElement("GradientFactorAtSurface");
            fHighGradientFactor=xmlValue.getValueAsDouble();       
        }
        catch (Exception e)
        {
            System.err.println("Error "+e.getMessage());
        }        
       
    }
}