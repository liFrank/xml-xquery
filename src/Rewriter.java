import java.io.PrintWriter;
import java.util.Scanner;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Rewriter {
	public static String rewrite_main(ParseTree tree)
	{
		return "!";
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {

			String filePath;
			if (args.length > 0) // command-line argument
				filePath = args[0];
			else { // user input on run
				Scanner reader = new Scanner(System.in);  // Reading from System.in
				System.out.println("Enter file path to rewrite: ");
				filePath = reader.next();
				reader.close();
			}
			ANTLRFileStream input = new ANTLRFileStream(filePath);
	        XqueryLexer lexer = new XqueryLexer(input);
	
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	
	        XqueryParser parser = new XqueryParser(tokens);
	        ParseTree tree = parser.xq(); // begin parsing at rule 'xq'
	        String rewritingresult=rewrite_main(tree);
	        if(rewritingresult!="")
	        {
	        	//store result in a file
	        	PrintWriter tofile=new PrintWriter("rewrited.txt");
	        	tofile.print(rewritingresult);
	        	tofile.close();
	        	System.out.println("The rewrited file is stored as rewrited.txt.");
	        	
	        }
	        else
	        {
	        	System.out.println("This file cannot be rewrited with join anymore.");
	        }
	        
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
