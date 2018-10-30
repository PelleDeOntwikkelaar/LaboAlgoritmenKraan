package be.kul.gantry.Extra;

import java.io.FileWriter;
import java.io.IOException;

public class CSVFileWriter {

    //Delimiter used in CSV file
    private static final String COMMA_DELIMITER = ";";
    private static final String NEW_LINE_SEPARATOR = "\n";

    //CSV file header
    private static final String FILE_HEADER = " \"gID\";\"T\";\"x\";\"y\";\"itemInCraneID\" ";
    private static final String FILE_NAME = "output.csv";

    private StringBuilder stringBuilder;

    public CSVFileWriter() {
        stringBuilder = new StringBuilder();
    }

    public void addLine(String string) {
        //TODO: aanpassen om lijn op zo makkelijk mogelijke manier af te printen.
        stringBuilder.append(string);
        stringBuilder.append(NEW_LINE_SEPARATOR);
    }

    public void add(StringBuilder stb) {
        stringBuilder.append(stb);
    }

    public void flush() {
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(FILE_NAME);
            fileWriter.append(FILE_HEADER);
            fileWriter.append(NEW_LINE_SEPARATOR);
            fileWriter.append(stringBuilder.toString());

            fileWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
