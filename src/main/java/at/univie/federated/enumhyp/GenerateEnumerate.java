package at.univie.federated.enumhyp;

import at.univie.federated.enumhyp.EnumhypRunner;

import java.io.IOException;
import java.nio.file.Paths;

public final class GenerateEnumerate {

    public void generateEnumerate() throws IOException, InterruptedException {
        EnumhypRunner runner = EnumhypRunner.fromDefaultPath();

        // 1. Generate UCC graphs
        runner.generate(Paths.get("abalone_p1.csv"));
        runner.generate(Paths.get("abalone_p2.csv"));

        // 2. Enumerate with output file
        runner.enumerate(
                Paths.get("abalone_p1_ucc.graph"),
                Paths.get("abalone_p1.txt")
        );
    }
}