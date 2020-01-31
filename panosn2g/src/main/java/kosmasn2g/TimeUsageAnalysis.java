package kosmasn2g;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Project 2 - In this class , we define the variables 'analysis', 'category' and 'device' which indicate us
 * the device , the category and the kind of analysis , for which the code will run. We read all the filenames (CSV files in folder)
 * We initialize and pass values (calling CSVReader methods) to the desired outputs-results and we print them into txt files.
 * Clues like the number of usages, the duration , the wattage and the consumed energy of each usage are calculated. In addition ,
 * hourly, daily, weekly and monthly time-usage arrays are created to show us the distribution. We , also , calculate the
 * contribution of our data to the whole of hours,days,weeks or months. (Do we have data for all months? Do we have data for
 * month February? How many devices(houses) of our dataset contribute to month February? etc)
 *
 * @author Panos Kosmas
 * @since 3/12/2019
 *
 */
public class TimeUsageAnalysis  {
    public static void main(String[] args ) throws IOException {

        String analysis         = "bus" ; //off , bus or all - days
        String category         = "entertainment" ;
        String device           = "tv" ;

        /**
         * The "files.txt" include all folder's filenames. We read them and pass the names to an ArrayList.
         */
        String sourcefile       = "C:\\Users\\user\\Desktop\\project2_filenames\\"+category+"\\"+device+"\\files.txt";
        TXTReader source1       = new TXTReader(sourcefile);
        ArrayList<String> names = source1.returnStringsArrayList(); //Saves All Files' names.
        /**
         * We initialize the arrays that will show us the final distribution of usages to different hours, days, weeks and
         * months. Plus the # of devices(houses) in folder that contribute to each hour, day, week or month.
         */
        int [] hdiv                  = new int [24];
        int [] ddiv                  = new int [7];
        int [] wdiv                  = new int [53];
        int [] mdiv                  = new int [12];
        int [] totalUsagesPerHour    = new int [24];
        int [] totalUsagesPerDay     = new int [7];
        int [] totalUsagesPerWeek    = new int [53];
        int [] totalUsagesPerMonth   = new int [12];
        int [] numberOfUsagesArray   = new int [source1.getLength()];
        int [] durationOfUsagesArray = new int [source1.getLength()];
        float [] hoursProb           = new float [24];
        float [] daysProb            = new float [7];
        float [] weeksProb           = new float [53];
        float [] monthsProb          = new float [12];
        int counterhours             = 0;
        int counterdays              = 0;
        int counterweeks             = 0;
        int countermonths            = 0;
        int numberOfUsages           = 0;

        ArrayList<Double> durations;
        ArrayList<Double> powers;
        ArrayList<Double> kWhconsumed;
        /**
         * We keep track of the durations, the powers and the energy for all device's usages and print them to txt files.
         */
        String durationsfile = "C:\\Users\\user\\Desktop\\project2_filenames\\"+category+"\\"+device+"\\durations\\durations_"+analysis+".txt";
        FileWriter writer2   = new FileWriter(durationsfile);
        String powersfile    = "C:\\Users\\user\\Desktop\\project2_filenames\\"+category+"\\"+device+"\\powers\\powers_"+analysis+".txt";
        FileWriter writer3   = new FileWriter(powersfile);
        String kWhsfile      = "C:\\Users\\user\\Desktop\\project2_filenames\\"+category+"\\"+device+"\\kWhconsumed\\kWhconsumed_"+analysis+".txt";
        FileWriter writer4   = new FileWriter(kWhsfile);
//--------------------------------------------FOR Number Of Devices in the Folder---------------------------------------
        for(int i=0;i<source1.getLength();i++) { //source1.getLength()

            String filename       = "E:/project2-NEW/" + category + "/" + device + "/" + names.get(i);
            CSVReader reader1  = new CSVReader(filename, device, analysis );
            System.out.println("File under consideration: "+filename+", No: "+(i+1));

            int [] usagesPerHour  = reader1.getHourlyDistributionOfUsages();
            int [] usagesPerDay   = reader1.getDailyDistributionOfUsages();
            int [] usagesPerWeek  = reader1.getWeeklyDistributionOfUsages();
            int [] usagesPerMonth = reader1.getMonthlyDistributionOfUsages();
            durations             = reader1.getUsageDuration();
            powers                = reader1.getMaxPower();
            kWhconsumed           = reader1.returnUsageKWh();
            /**
             * Find every device's above statistics and add them to the final sums.
             */

            for(int index=0;index<24;index++){ totalUsagesPerHour[index] += usagesPerHour[index]; }
            for(int index=0;index<7;index++){ totalUsagesPerDay[index] += usagesPerDay[index]; }
            for(int index=0;index<53;index++){ totalUsagesPerWeek[index] += usagesPerWeek[index]; }
            for(int index=0;index<12;index++){ totalUsagesPerMonth[index] += usagesPerMonth[index]; }

            int [] devicesContributedHourly  = reader1.getNumberOfDevicesContibutedHourly();
            int [] devicesContributedDaily   = reader1.getNumberOfDevicesContibutedDaily();
            int [] devicesContributedWeekly  = reader1.getNumberOfDevicesContibutedWeekly();
            int [] devicesContributedMonthly = reader1.getNumberOfDevicesContibutedMonthly();
            /**
            * Find every device's above statistics and add them to the final sums.
            */

            for(int index=0;index<24;index++){ hdiv[index] += devicesContributedHourly[index]; }
            for(int index=0;index<7;index++) { ddiv[index] += devicesContributedDaily[index];  }
            for(int index=0;index<53;index++){ wdiv[index] += devicesContributedWeekly[index]; }
            for(int index=0;index<12;index++){ mdiv[index] += devicesContributedMonthly[index];}

            numberOfUsagesArray[i]   = reader1.getNumberOfUsages();
            durationOfUsagesArray[i] = reader1.getDuration();
            numberOfUsages += reader1.getNumberOfUsages();
            /**
             * Print the results to duration.txt , powers.txt and kWhconsumed.txt respectively.
             */
                CSVWriter.writeLine(writer2, Collections.singletonList("dur" + (i + 1) + "=" + durations));
                CSVWriter.writeLine(writer3, Collections.singletonList("pow" + (i + 1) + "=" + powers));
                CSVWriter.writeLine(writer4, Collections.singletonList("nrg" + (i + 1) + "=" + kWhconsumed));
//-----------------------------Printing Plots of particular Days 1234567--> Mon to Sun.---------------------------------
/*
            final XYTimeMeasurements demo = new XYTimeMeasurements("All-time DVD"+i+" Measuremnts" , filename , "1234567");
            demo.pack();
            UIUtils.centerFrameOnScreen(demo);
            demo.setVisible(true);
*/
        }
        /**
         * find files, whose duration of measurement is the smallest & the highest respectively (just to know the size of our data).
         */

        int mindur = durationOfUsagesArray[0];
        int maxdur = 0;
        for (int index=0; index<source1.getLength(); index++){
            if(durationOfUsagesArray[index]<=mindur){mindur=durationOfUsagesArray[index];}
            if(durationOfUsagesArray[index]>maxdur) {maxdur=durationOfUsagesArray[index];}
        }


        for(int index=0;index<24;index++){ counterhours += totalUsagesPerHour[index]; }
        for(int index=0;index<7;index++) { counterdays += totalUsagesPerDay[index]; }
        for(int index=0;index<53;index++){ counterweeks += totalUsagesPerWeek[index];}
        for(int index=0;index<12;index++){ countermonths += totalUsagesPerMonth[index]; }
        for(int index=0;index<24;index++){ hoursProb[index] = 100* (float) totalUsagesPerHour[index]/counterhours; }
        for(int index=0;index<7;index++) { daysProb[index] = 100*(float) totalUsagesPerDay[index]/counterdays; }
        for(int index=0;index<53;index++){ weeksProb[index] = 100*(float) totalUsagesPerWeek[index]/counterweeks; }
        for(int index=0;index<12;index++){ monthsProb[index] = 100*(float) totalUsagesPerMonth[index]/countermonths; }

        System.out.println("NoU="+Arrays.toString(numberOfUsagesArray)+"\nH="+Arrays.toString(totalUsagesPerHour)+"\nD="+Arrays.toString(totalUsagesPerDay)+"\nW="+Arrays.toString(totalUsagesPerWeek)+"\nM="+Arrays.toString(totalUsagesPerMonth));
        String filename1   = "C:\\Users\\user\\Desktop\\project2_filenames\\"+category+"\\"+device+"\\HDWMstats_"+device+"_"+analysis+".txt";
        FileWriter writer1 = new FileWriter(filename1);
        CSVWriter.writeLine(writer1,Collections.singletonList("NumberOfDevices="+source1.getLength()+"\nUsages="+numberOfUsages+"\nMinDuration="+mindur+"\tMaxDuration="+maxdur+"\nDoU="+Arrays.toString(durationOfUsagesArray)+"\nNoU="+Arrays.toString(numberOfUsagesArray)+"\nH"+analysis+"="+Arrays.toString(totalUsagesPerHour)+"\nD"+analysis+"="+Arrays.toString(totalUsagesPerDay)+"\nW"+analysis+"="+Arrays.toString(totalUsagesPerWeek)+"\nM"+analysis+"="+Arrays.toString(totalUsagesPerMonth)+"\n%H"+analysis+"="+Arrays.toString(hoursProb)+"\n%D"+analysis+"="+Arrays.toString(daysProb)+"\n%W"+analysis+"="+Arrays.toString(weeksProb)+"\n%M"+analysis+"="+Arrays.toString(monthsProb)+"\ncontrh="+ Arrays.toString(hdiv) +"\ncontrd="+ Arrays.toString(ddiv) +"\ncontrw="+ Arrays.toString(wdiv) +"\ncontrm="+ Arrays.toString(mdiv)));

            writer1.flush();
            writer1.close();
            writer2.flush();
            writer2.close();
            writer3.flush();
            writer3.close();
            writer4.flush();
            writer4.close();
    }
}
