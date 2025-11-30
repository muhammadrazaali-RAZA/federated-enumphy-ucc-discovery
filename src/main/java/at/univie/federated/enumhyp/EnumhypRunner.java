package at.univie.federated.enumhyp;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

final class EnumhypRunner {

    private final String dockerImage;
    private final Path dataDir;      // mounted with -v host:data

    /**
     * @param dockerImage  name of docker image, e.g. "enumhyp-ubuntu"
     * @param dataDir      host directory mounted to /data in container
     */
    public EnumhypRunner(String dockerImage, Path dataDir) {
        this.dockerImage = dockerImage;
        this.dataDir = dataDir;
    }

    /**
     * Build and run: enumhyp project in docker container
     * docker run --rm -v hostDataDir:/data image args...
     */
    private int runEnumhypDocker(List<String> enumhypArgs) throws IOException, InterruptedException {

        List<String> command = new ArrayList<>();

        command.add("docker");
        command.add("run");
        command.add("--rm");

        // mount host directory to container:/data
        command.add("-v");
        command.add(dataDir.toAbsolutePath() + ":/data");

        // docker image name
        command.add(dockerImage);

        // add enumhyp command and args
        command.addAll(enumhypArgs);

        System.out.println(command.toString());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[enumhyp] " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Enumhyp failed with exit code " + exitCode);
        }

        return exitCode;
    }

    // ------------------------------------------------------------
    //  PUBLIC API
    // ------------------------------------------------------------

    /**
     * Calls:
     * docker run ... enumhyp-ubuntu generate /data/input.csv
     * output:  Path to generated .graph file
     */
    public void generate(Path csvFile) throws IOException, InterruptedException {

        String graphName = csvFile.getFileName().toString().replace(".csv", "_ucc.graph");
        System.out.println("Generating " + graphName);

        List<String> args = List.of(
                "generate",
                "/data/" + csvFile.getFileName().toString(),
                "-o",
                "/data/" + graphName
        );

        System.out.println(runEnumhypDocker(args));
    }

    /**
     * Calls:
     * docker run ... enumhyp-ubuntu enumerate /data/graphFile -o /data/outputFile
     */
    public void enumerate(Path graphFile, Path outputFile) throws IOException, InterruptedException {
        List<String> args = List.of(
                "enumerate",
                "/data/" + graphFile.getFileName().toString(),
                "-o",
                "/data/" + outputFile.getFileName().toString()
        );

        System.out.println(runEnumhypDocker(args));
    }

    /**
     * Calls:
     * docker run ... enumhyp-ubuntu --help
     */
    public void showHelp() throws IOException, InterruptedException {
        List<String> args = List.of("--help");
        runEnumhypDocker(args);
    }

    // Convenience factory
    public static EnumhypRunner fromDefaultPath() {
        return new EnumhypRunner(
                "enumhyp-ubuntu",
                Paths.get("C:/Users/jutt/IdeaProjects/ucc-bridge/data")
        );
    }
}