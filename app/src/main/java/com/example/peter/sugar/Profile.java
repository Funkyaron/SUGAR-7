package com.example.peter.sugar;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.util.*;
import java.io.*;

/**
 * @author Peter
 */
class Profile
{
    private String name;
    private boolean[] days;
    private TimeObject[] startTime;
    private TimeObject[] endTime;
    private boolean active;
    private boolean allowed;
    private int mode;
    private ArrayList<String> numbers;
    private ArrayList<String> contactNames;

    public static final int MODE_BLOCK_SELECTED = 1;
    public static final int MODE_BLOCK_NOT_SELECTED = 2;
    public static final int MODE_BLOCK_ALL = 3;

    private final String[] weekDays = { "Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};

    Profile() {
        name = "";
        days = new boolean[7];
        for(int i = 0; i < 7; i++) {
            days[i] = false;
        }
        startTime = new TimeObject[7];
        for(int i = 0; i < 7; i++) {
            startTime[i] = new TimeObject(0,0);
        }
        endTime = new TimeObject[7];
        for(int i = 0; i < 7; i++) {
            endTime[i] = new TimeObject(23, 59);
        }
        active = false;
        allowed = true;
        mode = MODE_BLOCK_NOT_SELECTED;
        numbers = new ArrayList<>();
        contactNames = new ArrayList<>();
    }

    Profile(String name,
            boolean[] days,
            TimeObject[] startTime,
            TimeObject[] endTime,
            boolean active,
            boolean allowed,
            int mode,
            ArrayList<String> numbers,
            ArrayList<String> contactNames)
    {
        this.name = name;
        this.days = days;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = active;
        this.allowed = allowed;
        this.mode = mode;
        this.numbers = numbers;
        this.contactNames = contactNames;
    }

    /**
     *
     * @param
     * @return null if no profile is found
     */
    public static Profile readProfileFromXmlFile(String name, Context context)
            throws IOException,XmlPullParserException
    {
        Log.d(MainActivity.LOG_TAG, "Profile: readProfileFromXmlFile()");
        FileInputStream fileInput = context.openFileInput(name + ".xml");

        try {
            ProfileParser parser = new ProfileParser();
            return parser.parse(fileInput);
        } finally {
            fileInput.close();
        }
    }

    public static Profile readProfileFromXmlFile(File file)
            throws IOException, XmlPullParserException
    {
        FileInputStream fileInput = new FileInputStream(file);

        try {
            ProfileParser parser = new ProfileParser();
            return parser.parse(fileInput);
        } finally {
            fileInput.close();
        }
    }

    public static Profile[] readAllProfiles(Context context) {
        Log.d(MainActivity.LOG_TAG, "Profile: readAllProfiles");
        File[] allFiles = context.getFilesDir().listFiles();
        ArrayList<Profile> profiles = new ArrayList<>();
        for(int i = 0; i < allFiles.length; i++) {
            try {
                profiles.add(readProfileFromXmlFile(allFiles[i]));
            } catch(Exception e) {
                Log.e(MainActivity.LOG_TAG, e.toString());
            }
        }
        Profile[] result = new Profile[profiles.size()];
        result = profiles.toArray(result);
        return result;
    }

    /**
     * Saves the given Profile to the default filepath of Android
     * @throws IOException if problems occured during the function execution
     */
    public void saveProfile(Context context) throws IOException {
        Log.d(MainActivity.LOG_TAG, "Profile: saveProfile()");

        File file = new File(context.getFilesDir(), name + ".xml");
        if(file.exists())
            file.delete();

        FileOutputStream fileOutput = context.openFileOutput(name + ".xml",Context.MODE_PRIVATE);
        XmlSerializer xmlWriter = Xml.newSerializer();
        try {
            xmlWriter.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output",true);
            xmlWriter.setOutput(fileOutput, "UTF-8");
            xmlWriter.startDocument(null, true);

            xmlWriter.startTag(null,"profile");

            xmlWriter.startTag(null, "name");
            xmlWriter.text(name);
            xmlWriter.endTag(null, "name");

            xmlWriter.startTag(null, "days");
            StringBuilder builder = new StringBuilder();
            for ( int currentDay = 0; currentDay < days.length; currentDay++ )
            {
                if( days[currentDay] == true )
                    builder.append("1");
                else
                    builder.append("0");
            }
            xmlWriter.text(builder.toString());
            xmlWriter.endTag(null,"days");

            xmlWriter.startTag(null,"startTime");
            for(int currentDay = 0; currentDay < startTime.length; currentDay++ )
            {
                if( currentDay == startTime.length - 1)
                    xmlWriter.text(startTime[currentDay].getHour() + ":" + startTime[currentDay].getMinute());
                else
                    xmlWriter.text(startTime[currentDay].getHour() + ":" + startTime[currentDay].getMinute() + "," );
            }
            xmlWriter.endTag(null,"startTime");

            xmlWriter.startTag(null,"endTime");
            for(int currentDay = 0; currentDay < endTime.length; currentDay++ )
            {
                if( currentDay == endTime.length - 1)
                    xmlWriter.text(endTime[currentDay].getHour() + ":" + endTime[currentDay].getMinute());
                else
                    xmlWriter.text(endTime[currentDay].getHour() + ":" + endTime[currentDay].getMinute() + ",");
            }
            xmlWriter.endTag(null,"endTime");

            xmlWriter.startTag(null, "active");
            if(active)
                xmlWriter.text("1");
            else
                xmlWriter.text("0");
            xmlWriter.endTag(null, "active");

            xmlWriter.startTag(null, "allowed");
            if(allowed)
                xmlWriter.text("1");
            else
                xmlWriter.text("0");
            xmlWriter.endTag(null, "allowed");

            xmlWriter.startTag(null, "mode");
            xmlWriter.text("" + mode);
            xmlWriter.endTag(null, "mode");

            xmlWriter.startTag(null,"numbers");
            ListIterator<String> iterator = numbers.listIterator();
            builder.delete(0, builder.length());
            while (iterator.hasNext())
            {
                if(iterator.hasPrevious())
                {
                    builder.append(",");
                }
                builder.append(iterator.next());
            }
            xmlWriter.text(builder.toString());
            xmlWriter.endTag(null,"numbers");

            xmlWriter.startTag(null, "contactNames");
            iterator = contactNames.listIterator();
            builder.delete(0, builder.length());
            while(iterator.hasNext()) {
                if(iterator.hasPrevious())
                    builder.append(",");
                builder.append(iterator.next());
            }
            xmlWriter.text(builder.toString());
            xmlWriter.endTag(null, "contactNames");

            xmlWriter.endTag(null,"profile");

            xmlWriter.endDocument();
            xmlWriter.flush();
        } finally {
            fileOutput.close();
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Name : ").append(getName()).append("\n");
        for(int i = 0; i < 7; i++) {
            builder.append(weekDays[i]).append(": ");
            if(days[i]) {
                builder.append(startTime[i].toString())
                        .append(" - ")
                        .append(endTime[i].toString())
                        .append("\n");
            } else {
                builder.append("None\n");
            }
        }
        if(active)
            builder.append("Active\n");
        else
            builder.append("Inactive\n");

        if(allowed)
            builder.append("Calls allowed\n");
        else
            builder.append("Calls not allowed\n");

        builder.append("Mode: ").append(mode).append("\n");
        builder.append("Numbers: ").append(numbers.toString()).append("\n");
        builder.append("Names: ").append(contactNames.toString());

        return builder.toString();
    }

    public String getName()
    {
        return name;
    }

    public boolean[] getDays()
    {
        return days;
    }

    public TimeObject[] getStart()
    {
        return startTime;
    }

    public TimeObject[] getEnd()
    {
        return endTime;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public int getMode() {
        return mode;
    }

    public ArrayList<String> getPhoneNumbers()
    {
        return numbers;
    }

    public ArrayList<String> getContactNames() {
        return contactNames;
    }

    public void setName(String updatedName)
    {
        name = updatedName;
    }

    public void setDays(boolean[] updatedDays)
    {
        days = updatedDays;
    }

    public void setStart( TimeObject[] updatedStart )
    {
        startTime = updatedStart;
    }

    public void setStartForDay(int selectedDay,TimeObject updatedStart)
    {
        startTime[selectedDay] = updatedStart;
    }

    public void setEndForDay(int selectedDay,TimeObject updatedEnd)
    {
        endTime[selectedDay] = updatedEnd;
    }

    public void setEnd( TimeObject[] updatedEnd )
    {
        endTime = updatedEnd;
    }

    public void setActive(boolean active) {
        this.active = active;
        if(!active)
            setAllowed(true);
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setPhoneNumbers(ArrayList<String> numbers) {
        this.numbers = numbers;
    }

    public void setContactNames(ArrayList<String> names) {
        contactNames = names;
    }

    public void setDayActiveForWeekDay(int selectedDay)
    {
        days[selectedDay] = true;
    }

    public void setDayInactiveForWeekDay(int selectedDay)
    {
        days[selectedDay] = false;
    }

}