package com.example.peter.sugar;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

class ProfileParser {

    private static final String ns = null;
    /**
     * This function processes the given XML file and returns a profile which can be later used
     * to activate the profile which is associated to the file.
     * @param in represents the input stream which reads the contents of the file
     * @return a profile which will be later used by "TimeManager","EnableProfileReceiver"
     * @throws XmlPullParserException is thrown if the file has formatting issues
     * @throws IOException is thrown if the file does not exist
     */
    public Profile parse (InputStream in)
            throws IOException,XmlPullParserException
    {
        Log.d(MainActivity.LOG_TAG, "ProfileParser: parse()");
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readProfile(parser);
        } finally {
            in.close();
        }
    }

    /**
     * Implements the interface function "parse()" to read the data from the XML file and brings
     * the content of the tags into a logical order.
     * @param parser parses the XML file further
     * @return profile for later usage
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Profile readProfile(XmlPullParser parser)
            throws XmlPullParserException,IOException
    {
        String profileName = null;
        boolean[] profileDays = new boolean[7];
        TimeObject[] startTime = new TimeObject[7];
        TimeObject[] endTime = new TimeObject[7];
        boolean active = false;
        boolean allowed = true;
        int mode = 0;
        ArrayList<String> numbers = new ArrayList<String>();
        ArrayList<String> contactNames = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG,ns,"profile");
        while( parser.next() != XmlPullParser.END_TAG )
        {
            if( parser.getEventType() != XmlPullParser.START_TAG )
            {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "name":
                {
                    //Log.d(MainActivity.LOG_TAG, "name-tag");
                    profileName = readProfileName(parser);
                    break;
                }
                case "days":
                {
                    profileDays = readActivatedDays(parser);
                    break;
                }
                case "startTime":
                {
                    startTime = readStartTimes(parser);
                    break;
                }
                case "endTime":
                {
                    endTime = readEndTimes(parser);
                    break;
                }
                case "active":
                {
                    active = readIsActive(parser);
                    break;
                }
                case "allowed":
                {
                    allowed = readIsAllowed(parser);
                    break;
                }
                case "mode":
                {
                    //Log.d(MainActivity.LOG_TAG, "mode-tag");
                    mode = readMode(parser);
                    break;
                }
                case "numbers":
                {
                    numbers = readPhoneNumbers(parser);
                    break;
                }
                case "contactNames":
                {
                    contactNames = readContactNames(parser);
                    break;
                }
            }
        }
        return new Profile(profileName, profileDays, startTime, endTime, active, allowed, mode, numbers, contactNames);
    }

    private String readProfileName(XmlPullParser parser)
            throws XmlPullParserException,IOException
    {
        parser.require(XmlPullParser.START_TAG,ns,"name");
        String name = readText(parser);
        parser.require(XmlPullParser.END_TAG,ns,"name");
        //Log.d(MainActivity.LOG_TAG, "Name: " + name);
        return name;
    }


    private boolean[] readActivatedDays(XmlPullParser parser)
            throws XmlPullParserException,IOException
    {
        boolean[] daysActivated = new boolean[7];
        parser.require(XmlPullParser.START_TAG,ns,"days");
        char[] days = readText(parser).toCharArray();
        parser.require(XmlPullParser.END_TAG,ns,"days");

        for( int currentDay = 0; currentDay < days.length; currentDay++ )
        {
            if( days[currentDay] == '1' )
            {
                daysActivated[currentDay] = true;
            }
            else if ( days[currentDay] == '0')
            {
                daysActivated[currentDay] = false;
            }
        }
        return daysActivated;
    }

    private TimeObject[] readStartTimes(XmlPullParser parser)
            throws XmlPullParserException,IOException
    {
        TimeObject[] result = new TimeObject[7];
        parser.require(XmlPullParser.START_TAG, ns, "startTime");
        String[] resultText = readText(parser).split(",");
        parser.require(XmlPullParser.END_TAG, ns, "startTime");

        for(int currentDay = 0; currentDay < resultText.length; currentDay++ )
        {
            result[currentDay] = new TimeObject(resultText[currentDay]);
        }
        return result;
    }

    private TimeObject[] readEndTimes(XmlPullParser parser)
            throws XmlPullParserException,IOException
    {
        TimeObject[] result = new TimeObject[7];
        parser.require(XmlPullParser.START_TAG,ns,"endTime");
        String[] resultText = readText(parser).split(",");
        parser.require(XmlPullParser.END_TAG,ns,"endTime");

        for(int currentDay = 0; currentDay < resultText.length; currentDay++ )
        {
            result[currentDay] = new TimeObject(resultText[currentDay]);
        }
        return result;
    }

    private boolean readIsActive(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "active");
        String resultText = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "active");

        return resultText.equals("1");
    }

    private boolean readIsAllowed(XmlPullParser parser)
            throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG,ns,"allowed");
        String resultText = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "allowed");

        return resultText.equals("1");
    }

    private int readMode(XmlPullParser parser) throws XmlPullParserException, IOException {
        //Log.d(MainActivity.LOG_TAG, "ProfileParser: readMode()");
        parser.require(XmlPullParser.START_TAG, ns, "mode");
        String resultText = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "mode");
        //Log.d(MainActivity.LOG_TAG, "Mode: " + resultText);

        return Integer.parseInt(resultText);
    }

    private ArrayList<String> readPhoneNumbers(XmlPullParser parser)
            throws XmlPullParserException,IOException
    {
        ArrayList<String> resultList = new ArrayList<>();
        parser.require(XmlPullParser.START_TAG,ns,"numbers");
        String[] phoneNumbers = readText(parser).split(",");
        parser.require(XmlPullParser.END_TAG,ns,"numbers");

        resultList.addAll(Arrays.asList(phoneNumbers));
        return resultList;
    }

    private ArrayList<String> readContactNames(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<String> resultList = new ArrayList<String>();
        parser.require(XmlPullParser.START_TAG,ns,"contactNames");
        String[] contactNames = readText(parser).split(",");
        parser.require(XmlPullParser.END_TAG,ns,"contactNames");

        resultList.addAll(Arrays.asList(contactNames));
        return resultList;
    }

    private String readText(XmlPullParser parser) throws XmlPullParserException,IOException
    {
        String result = "";
        if( parser.next() == XmlPullParser.TEXT )
        {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

}
