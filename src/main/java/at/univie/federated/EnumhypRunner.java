package at.univie.federated;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EnumhypRunner {

    private final Path enumhyp;


    /**
     * @param enumhyp absolute path to the enumhyp project
     */
    public EnumhypRunner( Path enumhyp ){
        this.enumhyp = enumhyp;
    }

    private int runEnumhyp(List<String> args, Path workingDir) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(enumhyp.toString());
        command.addAll(args);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingDir.toFile());

        // Merge Stderr into Stdout  - easier Logger
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))){
            String line;
            while ((line = reader.readLine())  != null ) {
                System.out.println("[enumhyp] : "+ line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Enumhyp Failed to exit code: " + exitCode);
        }
        return exitCode;
    }

    /**
     * Calls: enumhyp generate table.csv
     * @param csvPath path to input CSV (absolute or relative to workingDir)
     * @param workingDir directory where enumhyp will run and create .graph file
     */
    public void generate(Path csvPath, Path workingDir) throws IOException, InterruptedException {
        List<String> args = List.of("generate", csvPath.toString());
        runEnumhyp(args, workingDir);
    }

    /**
     * Calls: enumhyp enumerate table.graph
     * @param graphPath path to .graph file
     * @param workingDir directory where enumeration happens
     */
    public void enumerate(Path graphPath, Path workingDir) throws IOException, InterruptedException {
        List<String> args = List.of("enumerate", graphPath.toString());
        runEnumhyp(args, workingDir);
    }

    /**
     * Example: show enumhyp --help
     */
    public void showHelp(Path workingDir) throws IOException, InterruptedException {
        List<String> args = List.of("--help");
        runEnumhyp(args, workingDir);
    }

    // Convenience factory for OS-specific binary path
    public static EnumhypRunner fromDefaultPath() {

        // TODO: change this to 'YOUR' actual enumhyp path:

        Path bin = Paths.get("C:\\Users\\jutt\\IdeaProjects\\ucc-bridge\\enumhyp\\bin\\enumhyp.exe"); // <- change this
        return new EnumhypRunner(bin);
    }
}

