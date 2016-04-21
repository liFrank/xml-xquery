import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		ANTLRInputStream input = new ANTLRInputStream( "" );
		try {
			ANTLRFileStream input = new ANTLRFileStream("testcase/xqueries/test1.txt");
	        XqueryLexer lexer = new XqueryLexer(input);
	
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	
	        XqueryParser parser = new XqueryParser(tokens);
	        ParseTree tree = parser.xq(); // begin parsing at rule 'xq'
	        ParseTreeWalker walker = new ParseTreeWalker();
	        XqueryDerivedListener listener = new XqueryDerivedListener();
	        walker.walk(listener, tree);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}
