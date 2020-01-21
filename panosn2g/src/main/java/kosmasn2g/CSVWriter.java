package kosmasn2g;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

class CSVWriter {
    private static final char DEFAULT_SEPARATOR = ',';

    static void writeLine(Writer w, List<String> values) throws IOException {
        writeLine(w,values,DEFAULT_SEPARATOR,' ');
    }
    static void writeLine(Writer w, List<String> values, char separators) throws IOException{
        writeLine(w, values, separators, ' ');
    }

    private static String followCSVformat (String value){

        String result = value;
        if( result.contains("\"")){
            result = result.replace("\"", "\"\"");
        }
        return result;
    }
    private static void writeLine(Writer w, List<String> values, char separators, char customQuote) throws IOException {
        boolean first = true;

        if (separators == ' '){
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for(String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' '){
                sb.append(followCSVformat(value));
            }
            else {
                sb.append(customQuote).append(followCSVformat(value)).append(customQuote);
            }
            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());
    }
}
