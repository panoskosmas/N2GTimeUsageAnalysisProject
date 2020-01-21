package kosmasn2g;
import org.jfree.chart.ui.UIUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * This class is used for saving all names of the CSV files , in the folder , to an ArrayList of Strings named "paths".
 *
 * Project 2
 * @author Panos Kosmas
 * @since 3/12/2019
 */

class TXTReader {
    private static UIUtils RefineryUtilities;
    private ArrayList<String> paths = new ArrayList<>();
    private int counter             = 0;

    TXTReader(String sourcefile) throws IOException{
        String line       = "";
        String csvSplitBy = ",";
        BufferedReader br = new BufferedReader(new FileReader(Objects.requireNonNull(sourcefile)));
        try {
            while ((line = br.readLine()) != null) {
                counter++;
                String[] tool = line.split(csvSplitBy);
                paths.add(tool[1]);
            }
        } catch (IOException e){
                e.printStackTrace();
        }

    }
    ArrayList<String> returnStringsArrayList(){
        return paths;
    }
    int getLength(){
        return counter;
    }
}
