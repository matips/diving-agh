package net.deepocean.dodeco.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * SettingsFile represents the VPMDeco style settings file.
 * It is not finished.
 */

public class SettingsFile
{
    private static final int    MAX_SETTINGS=20;
    private String              fileName;
    private String[]            labels;
    private String[]            values;
    private String              label;
    private String              value;
    private int                 iIndex;
    private int                 nSettings;
    private String              settingFileID;

    public SettingsFile(String fileName)
    {
        this.fileName=fileName;
        labels=new String[MAX_SETTINGS];
        values=new String[MAX_SETTINGS];
        iIndex=0;
        nSettings=0;

        if (parameterFileExists())
        {
            loadParameters();
        }
    }

    public boolean parameterFileExists()
    {
        boolean bFileExists;
        File    settingsFile;

        settingsFile=new File(fileName);

        return settingsFile.exists();

    }

    private void loadParameters()
    {
        FileInputStream     file;
        BufferedReader      reader;
        String              line;
        boolean             bExit;

        bExit=false;
        try
        {
            file=new FileInputStream(fileName);
            reader=new BufferedReader(new InputStreamReader(file));

            while (reader.ready() && !bExit)
            {
                line=reader.readLine();
                line=shape(line);
                if (line!=null)
                {
                    if (line.startsWith("/"))
                    {
                        bExit=true;
                    }
                    else if (line.startsWith("&"))
                    {
                        settingFileID=line.substring(1).trim();
                    }
                    else
                    {
                        getLabelAndValue(line);

                        if ((label!=null) && (value!=null))
                        {
                            labels[nSettings]=label;
                            values[nSettings]=value;
System.out.println("Label "+label+" Value "+value);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }

    private String shape(String line)
    {
        String trimmed;
        int    iIndex;

        iIndex=line.indexOf('!');

        if (iIndex<0)
        {
            trimmed=line.trim();
        }
        else
        {
            if (iIndex>0)
            {
                trimmed=line.substring(0, iIndex).trim();
            }
            else
            {
                trimmed=null;
            }
        }
        return trimmed;
    }


    private void getLabelAndValue(String line)
    {
        int    iIndex;

        iIndex=line.indexOf('=');

        if (iIndex<0)
        {
            label=null;
            value=null;
        }
        else
        {
            if (iIndex>0)
            {
                label=line.substring(0, iIndex).trim();
                if (iIndex+1<line.length())
                {
                    value=line.substring(iIndex+1).trim();
                }
                else
                {
                    value=null;
                }
            }
            else
            {
                label=null;
                value=null;
            }
        }
    }


    public void reset()
    {
        iIndex=0;
    }

    public void nextElement()
    {
        if (iIndex<nSettings)
        {
            iIndex++;
        }
    }

    public String getLabel()
    {
        String returnString;
        if (iIndex<nSettings)
        {
            returnString=labels[iIndex];
        }
        else
        {
            returnString=null;
        }
        return returnString;
    }

    public String getValue()
    {
        String returnString;
        if (iIndex<nSettings)
        {
            returnString=values[iIndex];
        }
        else
        {
            returnString=null;
        }
        return returnString;
    }

    public double getValueAsDouble()
    {
        double fReturnValue;
        if (iIndex<nSettings)
        {
            fReturnValue=Double.parseDouble(values[iIndex]);
        }
        else
        {
            fReturnValue=0.0;
        }
        return fReturnValue;
    }
}