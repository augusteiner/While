package plp.enquanto;

import java.io.IOException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import plp.enquanto.linguagem.Linguagem.Programa;
import plp.enquanto.parser.EnquantoLexer;
import plp.enquanto.parser.EnquantoParser;
import plp.enquanto.parser.MeuListener;

public class Principal {

    private static ParseTree parse(String arq) {
        CharStream input;
        System.err.println(String.format("Abrindo arquivo: '%s'.", arq));

        try {
            input = CharStreams.fromFileName(arq);
        } catch (IOException e) {
            String err = String.format("Arquivo: '%s' não encontrado.", arq);

            System.err.println(err);
            System.exit(1);

            throw new RuntimeException(err);
        }

        final EnquantoLexer lexer = new EnquantoLexer(input);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final EnquantoParser parser = new EnquantoParser(tokens);

        return parser.programa();
    }

    public static void main(String... args) throws IOException {
        String arq = args[0];
        final ParseTree tree = parse(arq);
        final ParseTreeWalker walker = new ParseTreeWalker();
        final MeuListener listener = new MeuListener();
        walker.walk(listener, tree);
        Programa p1 = listener.getPrograma();
        p1.execute();
    }
}
