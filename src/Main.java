import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ANTLRInputStream input = new ANTLRInputStream( "hello there" );
        XqueryLexer lexer = new XqueryLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        XqueryParser parser = new XqueryParser(tokens);
        ParseTree tree = parser.r(); // begin parsing at rule 'r'
        System.out.println(tree.toStringTree(parser)); // print LISP-style tree

	}

}
