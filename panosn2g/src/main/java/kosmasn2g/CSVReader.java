package kosmasn2g;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * This class is used for recognising every event among data for a specific device (CSV file of measurements for a specific
 * device in a house). We find the number of total usages between the Start and the End of Measurement period. We find the max value
 * in Watts , the duration and the kWh consumed for every usage. Then, we distribute the usages among the hours of a day
 * the days of a week , the weeks and months of a year to make a statistical (time usage) analysis.
 *
 * Project 2
 * @author Panos Kosmas
 * @since 3/12/2019
 */
class CSVReader {

    private int totalDuration;
    private int numberOfusages                = 0;
    private long from                         = 0;
    private long to                           = 0;
    private int[] hdiv                        = new int[24];
    private int[] ddiv                        = new int[7];
    private int[] wdiv                        = new int[53];
    private int[] mdiv                        = new int[12];
    private int[] hourlyDistributionOfUsages  = new int[24];
    private int[] dailyDistributionOfUsages   = new int[7];
    private int[] weeklyDistributionOfUsages  = new int[53];
    private int[] monthlyDistributionOfUsages = new int[12];
    private ArrayList<Double> durations       = new ArrayList<>();
    private ArrayList<Double> powers          = new ArrayList<>();
    private ArrayList<Double> kWhconsumed     = new ArrayList<>();
    private ArrayList<Double> valuesstd       = new ArrayList<>();
    private DescriptiveStatistics ds1         = new DescriptiveStatistics();
    /**
     * These values are Constant but Different for every device. They are NOT magic numbers and have been emerged through research on the web.
     * They represent the highest operating value of the under-consideration device (in Watts), the upper standby wattage found (or considered as
     * a wattage that cannot be surpassed in standby mode), the lowest operating value of the device , the lowest and the highest acceptable
     * values of usage duration (in minutes) of the device! (must not exceed some reasonable limits i.e 1800' > 1day)
     * In the array of doubles, these are defined in order {upper_standby,lower_operating,higher_operating,lowMinutes,highMinutes};
     */
    private static Map<String , double []> hmap = new HashMap<>() {{
       put("dvd" , new double[]{10.0, 15.0, 100.0, 5.0, 1440.0});
       put("audiovisual_media", new double[]{15.0, 18.0, 200.0, 5.0, 1440.0});
       put("stereo", new double[]{12.0, 20.0, 200.0, 5.0, 1440.0});
       put("game_console", new double[]{25.0, 30.0, 300.0, 5.0, 1440.0});
       put("laptop", new double[]{20.0, 23.0, 200.0, 5.0, 1440.0});
       put("computer", new double[]{35.0, 40.0, 250.0, 5.0, 1440.0});
       put("computer_screen", new double[]{16.0, 19.0, 210.0, 5.0, 1440.0});
       put("apple_tv", new double[]{3.0, 3.0, 50.0, 5.0, 1440.0});
       put("tv", new double[]{15.0, 20.0, 400.0, 5.0, 1440.0});
       put("coffee_grinder", new double[]{15.0, 15.0, 1500.0, 0.01, 5.0});
       put("coffee_maker", new double[]{80.0, 100.0, 2000.0, 0.01, 5.0});
       put("espresso_machine", new double[]{60.0, 200.0, 3000.0, 0.01, 5.0});
       put("kettle", new double[]{60.0, 600.0, 3500.0, 0.01, 10.0});
       put("microwave", new double[]{20.0, 200.0, 2000.0, 0.10, 30.0});
       put("stand_mixer", new double[]{75.0, 100.0, 2500.0, 0.01, 5.0});
       put("steamer", new double[]{100.0, 200.0, 2000.0, 0.01, 10.0});
       put("toaster", new double[]{250.0, 400.0, 2500.0, 0.01, 10.0});
    }};

    /**
     *
     * @param filename      the CSV file which is under consideration
     * @param device        the device which is under consideration
     * @param analysis      the kind of analysis - All days , OFF days or BUSINESS days
     * @throws IOException  an exception
     */

    CSVReader(String filename, String device, String analysis) throws IOException {
        String line       = "";
        String csvSplitBy = ",";
        BufferedReader br = new BufferedReader(new FileReader(Objects.requireNonNull(filename)));

        /**
         * Depending on which device is under consideration , go and pass the proper values to our variables.
         */
        double upperStandby    = hmap.get(device)[0];
        double lowerOperation  = hmap.get(device)[1];
        double higherOperation = hmap.get(device)[2];
        double lowOpMinutes     = hmap.get(device)[3];
        double highOpMinutes    = hmap.get(device)[4];
        /**
         * We initialize both ArrayLists values2 & times2 that help us make all the following calculations.
         */
        ArrayList<Double> values2  = new ArrayList<>();
        ArrayList<Long> times2     = new ArrayList<>();

        try {
            line             = br.readLine();
            String[] column1 = line.split(csvSplitBy);
            from             = Long.parseLong(column1[0]);
            while ((line = br.readLine()) != null) {
                String[] column = line.split(csvSplitBy);
                to              = Long.parseLong(column[0]);

                if ((Integer.parseInt(column[1]) > 0) && (Integer.parseInt(column[1]) < upperStandby)) {
                    valuesstd.add(Double.parseDouble(column[1]));
                }
                if ((Integer.parseInt(column[1]) > 0) && (Integer.parseInt(column[1]) < higherOperation)) {
                            times2.add(Long.parseLong(column[0]));
                            values2.add(Double.parseDouble(column[1]));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        /**
         * (Pre-caution) We add an extra element in the end of the arraylist to ensure that the measurement "finishes".
         * (goes under the defined level of standby wattage).
         */
        if (times2.size()!=0) {
            times2.add(times2.get(times2.size() - 1) + 1000);
            values2.add(0.0);
        } else { System.out.println("Measurement is Floating"); }
        /**
         * We find and print the total Duration of measurement in days.
         */
        totalDuration = (int) ((to-from) / (1000 * 60 * 60 * 24));
        System.out.println(totalDuration);
//------------------------------------------------TIME USAGE -----------------------------------------------------------
//---------------------------------------------NUMBER OF USAGES---------------------------------------------------------
        int j;
        int k;
        int i = 0;
        double threshold;
        double maxpower    = 0.0;
        double standbyMean = getMeanStd();
        /**
         * threshold must have a value between the standby_wattage and the lowest_operating value!
         * So that we use it to recognise every usage (usage starts when our measurement value surpasses the threshold
         * and it ends , when our value is again below the threshold).
         */
        if (standbyMean <= lowerOperation) { threshold = (standbyMean + lowerOperation) / 2; }
        else { threshold = standbyMean + 1; }
        System.out.println(threshold);
        /**
        * We define TimeZone to be GMT+1 due to the fact that we have measurements from SWEDEN. The default would return the timestamps
        * in GMT timeZone which would affect our results.
        */
        while (i < times2.size()) {
            Calendar startOfUse = new GregorianCalendar();
            startOfUse.setTime(new Date(times2.get(i)));
            if ((startOfUse.get(Calendar.MONTH)==Calendar.MARCH)||(startOfUse.get(Calendar.MONTH)==Calendar.APRIL)||(startOfUse.get(Calendar.MONTH)==Calendar.MAY)||(startOfUse.get(Calendar.MONTH)==Calendar.JUNE)||(startOfUse.get(Calendar.MONTH)==Calendar.JULY)||(startOfUse.get(Calendar.MONTH)==Calendar.AUGUST)||(startOfUse.get(Calendar.MONTH)==Calendar.SEPTEMBER)){
                startOfUse.setTimeZone(TimeZone.getTimeZone("GMT+02:00"));
            }
            else {startOfUse.setTimeZone(TimeZone.getTimeZone("GMT+01:00"));}
            startOfUse.setTime(new Date(times2.get(i)));
/*
            System.out.println("ts="+times2.get(i));
            System.out.println("gmt="+startOfUse.getTimeZone());
            System.out.println("date="+startOfUse);
*/
                /**
                 * In this section, there are 3 cases. Depending on which type of analysis we work on (all/off or business days),
                 * we accept values whose timestamps are All days, MonTueWenThurFridays or Sundays/Saturdays respectively.
                 */
            if (((analysis.equals("all"))&&(values2.get(i) > threshold))||((analysis.equals("off"))&&((startOfUse.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY)||(startOfUse.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY))&&(values2.get(i) > threshold))||((analysis.equals("bus"))&&((startOfUse.get(Calendar.DAY_OF_WEEK)!=Calendar.SUNDAY)&&(startOfUse.get(Calendar.DAY_OF_WEEK)!=Calendar.SATURDAY))&&(values2.get(i) > threshold))) {
                    for (j = i; j < times2.size(); j++) {
                        Calendar endOfUse = new GregorianCalendar();
                        endOfUse.setTime(new Date(times2.get(j)));
                        if ((endOfUse.get(Calendar.MONTH)==Calendar.MARCH)||(endOfUse.get(Calendar.MONTH)==Calendar.APRIL)||(endOfUse.get(Calendar.MONTH)==Calendar.MAY)||(endOfUse.get(Calendar.MONTH)==Calendar.JUNE)||(endOfUse.get(Calendar.MONTH)==Calendar.JULY)||(endOfUse.get(Calendar.MONTH)==Calendar.AUGUST)||(endOfUse.get(Calendar.MONTH)==Calendar.SEPTEMBER)){
                            endOfUse.setTimeZone(TimeZone.getTimeZone("GMT+02"));
                        }
                        else {endOfUse.setTimeZone(TimeZone.getTimeZone("GMT+01"));}
                        endOfUse.setTime(new Date(times2.get(j)));

                        if ((values2.get(j) <= threshold)){
                            /**
                             * If it gets in , it means that we have a use, because initially it surpassed the threshold (i)
                             * and then fell down again (j).
                             * @var usageDuration is the duration of found usage in minutes.
                             */
                            double usageDuration = (times2.get(j) - times2.get(i)) / (1000.0 * 60.0);
                            DescriptiveStatistics ds3 = new DescriptiveStatistics();
                            for (k = i; k < j; k++) {
                                if (values2.get(k) > maxpower) {
                                ds3.addValue(values2.get(k));
                                maxpower = values2.get(k);
                                }
                            }
                        double meanpower = ds3.getMean();
                        double wattage = (meanpower / 1000) * (usageDuration / 60.0);
                        i = j + 1;
                        /**
                        * IF duration of usage is not acceptable (depending our limits) --> dont take this usage into account.
                        * ELSE save usage's normal stats.
                        */
                        if ((usageDuration < lowOpMinutes)||(((usageDuration > highOpMinutes)))) { break; } //avoid stack meters
                        else {
                            numberOfusages++;
                            durations.add(usageDuration);
                            powers.add(maxpower);
                            kWhconsumed.add(wattage);
                        }
                        /**
                        * Monthly report - extreme case use 31 Dec to 1 Jan
                        */
                            if (startOfUse.get(Calendar.MONTH) > endOfUse.get(Calendar.MONTH)) {
                                for (int index = (startOfUse.get(Calendar.MONTH)); index < 12; index++) {
                                    monthlyDistributionOfUsages[index]++; }
                                for (int index = 0; index < endOfUse.get(Calendar.MONTH)+1; index++) {
                                    monthlyDistributionOfUsages[index]++; }
                            }
                            else {
                                for (int index = (startOfUse.get(Calendar.MONTH)); index < endOfUse.get(Calendar.MONTH) + 1; index++) {
                                    monthlyDistributionOfUsages[index]++; }
                            }
                         /**
                        * Weekly report - extreme case use 31 Dec to 1 Jan
                        */
                            if (startOfUse.get(Calendar.WEEK_OF_YEAR) > endOfUse.get(Calendar.WEEK_OF_YEAR)) {
                                for (int index = (startOfUse.get(Calendar.WEEK_OF_YEAR)-1); index < 53; index++) {
                                    weeklyDistributionOfUsages[index]++; }
                                for (int index = 0; index < endOfUse.get(Calendar.WEEK_OF_YEAR); index++) {
                                    weeklyDistributionOfUsages[index]++; }
                            }
                            else {
                                for (int index = (startOfUse.get(Calendar.WEEK_OF_YEAR)-1); index < endOfUse.get(Calendar.WEEK_OF_YEAR) ; index++) {
                                    weeklyDistributionOfUsages[index]++; }
                            }
                        /**
                        * Daily report - extreme case use Saturday to Sunday (7 --> 1).
                        */
                        if (startOfUse.get(Calendar.DAY_OF_WEEK) > endOfUse.get(Calendar.DAY_OF_WEEK)) {
                            for (int index = (startOfUse.get(Calendar.DAY_OF_WEEK) - 1); index < 7; index++) {
                                dailyDistributionOfUsages[index]++; }
                            for (int index = 0; index < endOfUse.get(Calendar.DAY_OF_WEEK); index++) {
                                dailyDistributionOfUsages[index]++; }
                        }
                        else {
                            for (int index = (startOfUse.get(Calendar.DAY_OF_WEEK) - 1); index < endOfUse.get(Calendar.DAY_OF_WEEK); index++) {
                                dailyDistributionOfUsages[index]++; }
                        }
                        /**
                        * Hourly report - extreme case use between 12.01am to 11.59pm next day. (change in day)
                        */
                        if (startOfUse.get(Calendar.HOUR_OF_DAY) <= endOfUse.get(Calendar.HOUR_OF_DAY)) {
                            for (int index = startOfUse.get(Calendar.HOUR_OF_DAY); index < (endOfUse.get(Calendar.HOUR_OF_DAY) + 1); index++) {
                                hourlyDistributionOfUsages[index]++; }
                        } else {
                            for (int index = startOfUse.get(Calendar.HOUR_OF_DAY); index < 24; index++) {
                                hourlyDistributionOfUsages[index]++; }
                            for (int index = 0; index < (endOfUse.get(Calendar.HOUR_OF_DAY) + 1); index++) {
                                hourlyDistributionOfUsages[index]++; }
                        }
                    }
                }
            } else {i++;}
        }
        /**
         *  In this section, we define to which(?) hours/days/weeks and months of the year, our device contributes -
         *  (has measurements for).
         */
        Calendar startOfMeasurement = new GregorianCalendar();
        startOfMeasurement.setTime(new Date(from));
        if ((startOfMeasurement.get(Calendar.MONTH)==Calendar.MARCH)||(startOfMeasurement.get(Calendar.MONTH)==Calendar.APRIL)||(startOfMeasurement.get(Calendar.MONTH)==Calendar.MAY)||(startOfMeasurement.get(Calendar.MONTH)==Calendar.JUNE)||(startOfMeasurement.get(Calendar.MONTH)==Calendar.JULY)||(startOfMeasurement.get(Calendar.MONTH)==Calendar.AUGUST)||(startOfMeasurement.get(Calendar.MONTH)==Calendar.SEPTEMBER)){
            startOfMeasurement.setTimeZone(TimeZone.getTimeZone("GMT+02"));
        }
        else {startOfMeasurement.setTimeZone(TimeZone.getTimeZone("GMT+01"));}
        startOfMeasurement.setTime(new Date(from));

        Calendar endOfMeasurement = new GregorianCalendar();
        endOfMeasurement.setTime(new Date(to));
        if ((endOfMeasurement.get(Calendar.MONTH)==Calendar.MARCH)||(endOfMeasurement.get(Calendar.MONTH)==Calendar.APRIL)||(endOfMeasurement.get(Calendar.MONTH)==Calendar.MAY)||(endOfMeasurement.get(Calendar.MONTH)==Calendar.JUNE)||(endOfMeasurement.get(Calendar.MONTH)==Calendar.JULY)||(endOfMeasurement.get(Calendar.MONTH)==Calendar.AUGUST)||(endOfMeasurement.get(Calendar.MONTH)==Calendar.SEPTEMBER)){
            endOfMeasurement.setTimeZone(TimeZone.getTimeZone("GMT+02"));
        }
        else{endOfMeasurement.setTimeZone(TimeZone.getTimeZone("GMT+01"));}
        endOfMeasurement.setTime(new Date(to));
        /**
        * If totalDuration is more than a year --> all months have measurements
        * Else go find for which months I have measurement and make ' mdiv[index] ' flag go 1.
        */
        if ((totalDuration)>365){ Arrays.fill(mdiv,1); }
        else if (startOfMeasurement.get(Calendar.MONTH) <= endOfMeasurement.get(Calendar.MONTH)) {
            for (int p = startOfMeasurement.get(Calendar.MONTH); p < endOfMeasurement.get(Calendar.MONTH) + 1; p++) { mdiv[p] = 1; }
        }
        else {
            for (int p = startOfMeasurement.get(Calendar.MONTH); p < 12; p++) { mdiv[p] = 1; }
            for (int p = 0; p < endOfMeasurement.get(Calendar.MONTH) + 1; p++) { mdiv[p] = 1; }
        }
        /**
        * If totalDuration is more than a year --> all weeks have measurements
        * Else go find for which weeks, there are measurements and make ' wdiv[index] ' flag go 1.
        */
        if ((totalDuration)>365){ Arrays.fill(wdiv,1); }
        else if (startOfMeasurement.get(Calendar.WEEK_OF_YEAR) <= endOfMeasurement.get(Calendar.WEEK_OF_YEAR)) {
            for (int p = startOfMeasurement.get(Calendar.WEEK_OF_YEAR) - 1; p < endOfMeasurement.get(Calendar.WEEK_OF_YEAR); p++) { wdiv[p] = 1; }
        }
        else {
            for (int p = startOfMeasurement.get(Calendar.WEEK_OF_YEAR) - 1; p < 53; p++) { wdiv[p] = 1; }
            for (int p = 0; p < endOfMeasurement.get(Calendar.WEEK_OF_YEAR); p++) { wdiv[p] = 1; }
        }
        /**
        * If totalDuration is more than a week --> all days have measurements
        * Else go find for which days of the week, there are measurements and make ' ddiv[index] ' flag go 1.
        */
        if ((totalDuration)>7){ Arrays.fill(ddiv,1); }
        else if (startOfMeasurement.get(Calendar.DAY_OF_WEEK) <= endOfMeasurement.get(Calendar.DAY_OF_WEEK)) {
            for (int p = startOfMeasurement.get(Calendar.DAY_OF_WEEK) - 1; p < endOfMeasurement.get(Calendar.DAY_OF_WEEK); p++) { ddiv[p] = 1; }
        }
        else {
            for (int p = startOfMeasurement.get(Calendar.DAY_OF_WEEK) - 1; p < 7; p++) { ddiv[p] = 1; }
            for (int p = 0; p < endOfMeasurement.get(Calendar.DAY_OF_WEEK)+1; p++) { ddiv[p] = 1; }
        }
        /**
         * If totalDuration is more than a day --> all hours have measurements
         * Else go find for which hours of the day, I have measurements and make ' hdiv[index] ' flag go 1.
         */
        if ((totalDuration)>1){ Arrays.fill(hdiv,1); }
        else if (startOfMeasurement.get(Calendar.HOUR_OF_DAY) <= endOfMeasurement.get(Calendar.HOUR_OF_DAY)) {
            for (int p = startOfMeasurement.get(Calendar.HOUR_OF_DAY); p < endOfMeasurement.get(Calendar.HOUR_OF_DAY) + 1; p++) { hdiv[p] = 1; }
        } else {
            for (int p = startOfMeasurement.get(Calendar.HOUR_OF_DAY); p < 12; p++) { hdiv[p] = 1; }
            for (int p = 0; p < endOfMeasurement.get(Calendar.HOUR_OF_DAY) + 1; p++) { hdiv[p] = 1; }
        }
    }
    /** This is the getter method for the Mean value of valuesstd-Arraylist. Practically, it returns a value representing
     *  the upper "standby" wattage of the under-consideration device.
     */
    private double getMeanStd(){
        ds1.clear();
        for (Double number : valuesstd){
            ds1.addValue(number);
        }
        return ds1.getMean();
    }
    /** This is the getter method for the StartDate variable from (start of measurements).
     * @return the StartDate value , as a Date .
     */
    Date getStartDate(){ return new java.sql.Date(from); }
    /** This is the getter method for the EndDate variable from (end of measurements).
     * @return the EndtDate value , as a Date .
     */
    Date getEndDate(){ return new java.sql.Date(to); }
    /** This is the getter method for the totalDuration variable.
     * @return the totalDuration value , as an integer. It is in DAYS.
     */
    int getDuration(){ return totalDuration; }
    /** This is the getter method for the numberOfusages variable. It is the # of usages , found in the sample.
     * @return the numberOfusages value , as an integer.
     */
    int  getNumberOfUsages(){ return numberOfusages; }
    /** This is the getter method for the durations variable. It shows us the duration (in minutes) of every found usage
     *  in the sample.
     * @return the durations value , as an ArrayList of Doubles.
     */
    ArrayList<Double> getUsageDuration() { return durations; }
    /** This is the getter method for the powers variable. It shows us the power (in W) of every found usage
     *  in the sample.
     * @return the powers value , as an ArrayList of Doubles.
     */
    ArrayList<Double> getMaxPower (){ return powers; }
    /** This is the getter method for the kWhconsumed variable. It shows us the amount of energy ,that every found usage
     *  in the sample, consumed (in kWhs).
     * @return the kWhconsumed value , as an ArrayList of Doubles.
     */
    ArrayList<Double> returnUsageKWh(){ return kWhconsumed; }
    /** This is the getter method for the hourlyDistributionOfUsages variable. It shows us how the usages are distributed
     * hourly.
     * @return the hourlyDistributionOfUsages value ,as an array of 24 integers. Every cell represents an hour of the day.
     */
    int [] getHourlyDistributionOfUsages(){ return hourlyDistributionOfUsages;}
    /** This is the getter method for the dailyDistributionOfUsages variable. It shows us how the usages are distributed
     * daily.
     * @return the dailyDistributionOfUsages value ,as an array of 7 integers. Every cell represents a day of the week.
     * From 1 to 7 (with 1 being SUNDAY and 7 being the SATURDAY)
     */
    int [] getDailyDistributionOfUsages(){ return dailyDistributionOfUsages;}
    /** This is the getter method for the weeklyDistributionOfUsages variable. It shows us how the usages are distributed
     * weekly.
     * @return the weeklyDistributionOfUsages value ,as an array of 53 integers.Every cell represents a week of the year.
     */
    int [] getWeeklyDistributionOfUsages(){ return weeklyDistributionOfUsages;}
    /** This is the getter method for the monthlyDistributionOfUsages variable. It shows us how the usages are distributed
     * monthly.
     * @return the monthlyDistributionOfUsages value, as an array of 12 integers. Every cell represents a month of the year.
     */
    int [] getMonthlyDistributionOfUsages(){ return monthlyDistributionOfUsages;}
    /** This is the getter method for the hdiv variable. It practically shows us how many of the folder's total amount
     * of devices, are contributing (basically - are having a measurement) for a every hour of a day.
     * @return the hdiv value , as an array of integers.
     */
    int [] getNumberOfDevicesContibutedHourly(){ return hdiv;}
    /** This is the getter method for the ddiv variable. It practically shows us how many of the folder's total amount
     * of devices, are contributing for a every day of a week.
     * @return the ddiv value , as an array of integers.
     */
    int [] getNumberOfDevicesContibutedDaily(){ return ddiv;}
    /** This is the getter method for the wdiv variable. It practically shows us how many of the folder's total amount
     * of devices, are contributing for a every week of the year.
     * @return the wdiv value , as an array of integers.
     */
    int [] getNumberOfDevicesContibutedWeekly(){ return wdiv;}
    /** This is the getter method for the mdiv variable. It practically shows us how many of the folder's total amount
     * of devices, are contributing for a every month of the year.
     * @return the mdiv value , as an array of integers.
     */
    int [] getNumberOfDevicesContibutedMonthly(){ return mdiv;}
}
// Find the total amount of energy consumed by device (in Joules) during the measurement period of time.
/*
        double joules    = 0.0;

        for (int i = 0; i < (times2.size()-1); i++) {
            double diff = (times2.get(i + 1)-times2.get(i))/1000.0;
            joules += values2.get(i)*diff;
        }
        // Find total duration (in days) of device's measurements.
        totalDuration = (int) ((to-from) / (1000 * 60 * 60 * 24));
        // Find total kWh consumed in measurement
        double kWhtotal = (joules * 2.77778 * Math.pow(10, -7));
*/
