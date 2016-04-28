import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;



public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		ANTLRInputStream input = new ANTLRInputStream( "" );
		try {
			ANTLRFileStream input = new ANTLRFileStream("testcase/xqueries/simple.txt");
	        XqueryLexer lexer = new XqueryLexer(input);
	
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	
	        XqueryParser parser = new XqueryParser(tokens);
	        ParseTree tree = parser.xp(); // begin parsing at rule 'xq'
	        System.out.println("Visitor:");
	        EvalVisitor evalByVisitor = new EvalVisitor();
	        evalByVisitor.visit(tree);
	        System.out.println();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}
