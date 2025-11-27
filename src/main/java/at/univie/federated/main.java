package at.univie.federated;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class main
{
    public static void main(String[] args) {
        // 1. Configure the path to the enumhyp binary
        EnumhypRunner runner = EnumhypRunner.fromDefaultPath();

        // 2. Working directory: your project root (where "data" folder lives)
        //    Adjust if your CSV is elsewhere.
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path dataDir = projectRoot.resolve("data");

        // 3. Input CSV and expected graph path (relative to workingDir)
        Path csvPath = dataDir.resolve("call_a_bike.csv");     // data/table.csv
        Path graphPath = dataDir.resolve("call_a_bike_uccs.graph"); // will be created by generate

        try {
            System.out.println("== enumhyp --help ==");
            runner.showHelp(projectRoot);

//            System.out.println("\n== Generating hypergraph from CSV ==");
//            runner.generate(csvPath, dataDir);

            System.out.println("\n== Enumerating hitting sets ==");
            runner.enumerate(graphPath, dataDir);

            System.out.println("\nDone.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
