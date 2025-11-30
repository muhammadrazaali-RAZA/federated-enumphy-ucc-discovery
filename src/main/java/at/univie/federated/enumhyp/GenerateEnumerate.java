package at.univie.federated;

import at.univie.federated.enumhyp.EnumhypRunner;

final class GenerateEnumerate {

    final void generateEnumerate() {
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