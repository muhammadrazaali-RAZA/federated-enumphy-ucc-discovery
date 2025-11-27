package at.univie.federated;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class main
{
    public static void main(String[] args) throws Exception {

        EnumhypRunner runner = EnumhypRunner.fromDefaultPath();

        // 1. generate
//        runner.generate(Paths.get("abalone.csv"));

        // 2. enumerate without output file
//        runner.enumerate(Paths.get("civil_service_uccs.graph"));

        // 3. enumerate with output file
        runner.enumerate(
                Paths.get("abalone_ucc.graph"),
                Paths.get("abalone.txt")
        );
    }
}
