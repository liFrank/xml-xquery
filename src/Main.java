import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;


public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		ANTLRInputStream input = new ANTLRInputStream( "" );
		try {

			String filePath;
			boolean rewrite = false;
			boolean resultToFile = false;
			boolean showAst = false;
			
			if (args.length > 0) // command-line argument
				filePath = args[args.length-1];
			else { // user input on run
				Scanner reader = new Scanner(System.in);  // Reading from System.in
				System.out.println("Enter file path to query: ");
				filePath = reader.next();
				System.out.println("Rewrite and process query as rewrited.txt with join? (y/n)");
				if (reader.next().toLowerCase().trim().equals("y"))
					rewrite = true;
				System.out.println("Write result to file result.txt? (y/n)");
				if (reader.next().toLowerCase().trim().equals("y"))
					resultToFile = true;
				System.out.println("Show AST? (y/n)");
				if (reader.next().toLowerCase().trim().equals("y"))
					showAst = true;
				reader.close();
			}

			ANTLRFileStream input = new ANTLRFileStream(filePath);
	        XqueryLexer lexer = new XqueryLexer(input);
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	        XqueryParser parser = new XqueryParser(tokens);
	        ParseTree tree = parser.xq(); // begin parsing at rule 'xq'
	        	       
	        // Rewrite query option
	        if (rewrite || Arrays.asList(args).contains("-rewrite")) {
		        // Rewrite query with join
		        String newQuery = Rewriter.rewrite_main(tree);
	        	if (!newQuery.isEmpty()) { // rewriting occurred
	        		System.out.println("Rewriting query into rewrited.txt");
	        		PrintWriter tofile = new PrintWriter("rewrited.txt");
		        	tofile.print(newQuery);
		        	tofile.close();  
		        	input = new ANTLRFileStream("rewrited.txt");
			        lexer = new XqueryLexer(input);
			        tokens = new CommonTokenStream(lexer);
			        parser = new XqueryParser(tokens);
			        tree = parser.xq(); // begin parsing at rule 'xq'
			        System.out.println("Reading rewritten query from: " + "rewrited.txt");
	        	}
	        	else {
	        		System.out.println("No rewriting done, reading query from: " + filePath);
	        	}
	        }
	        else {
    			System.out.println("Reading query from: " + filePath);
        	}
	        
	        // Timing and results
	        EvalVisitor evalByVisitor = new EvalVisitor();
	        final long startTime = System.currentTimeMillis();
	        XqueryNodes result = (XqueryNodes) evalByVisitor.visit(tree);
	        
	        // Write to file option
	        if (resultToFile || Arrays.asList(args).contains("-writefile")) {
	        	System.out.println("Writing result to: " + "result.txt");
	        	result.printNodesToFile("result.txt");
	        }
	        else {
	        	System.out.println("Writing result to: console");
	        	result.printNodes();
	        }
	        final long endTime = System.currentTimeMillis();
	        System.err.println("Total execution time: " + (endTime - startTime) + "ms");
	        
	        // show AST in GUI option
	        if (showAst || Arrays.asList(args).contains("-ast")) {
		        JFrame frame = new JFrame("Antlr AST");
		        JPanel panel = new JPanel();
		        TreeViewer viewr = new TreeViewer(Arrays.asList(parser.getRuleNames()),tree);
		        viewr.setScale(1.5); //scale option
		        panel.add(viewr);
		        JScrollPane scrollPanel = new JScrollPane(panel, 
		        		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		        		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		        frame.add(scrollPanel);
		        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		        frame.pack();
		        frame.setVisible(true);
	        }
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}
