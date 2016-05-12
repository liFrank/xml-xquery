import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;


public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		ANTLRInputStream input = new ANTLRInputStream( "" );
		try {

			String filePath;
			if (args.length > 0) // command-line argument
				filePath = args[0];
			else { // user input on run
				Scanner reader = new Scanner(System.in);  // Reading from System.in
				System.out.println("Enter file path to query: ");
				filePath = reader.next();
				reader.close();
			}
			ANTLRFileStream input = new ANTLRFileStream(filePath);
	        XqueryLexer lexer = new XqueryLexer(input);
	
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	
	        XqueryParser parser = new XqueryParser(tokens);
	        ParseTree tree = parser.xq(); // begin parsing at rule 'xq'
	        EvalVisitor evalByVisitor = new EvalVisitor();
	        XqueryNodes result = (XqueryNodes) evalByVisitor.visit(tree);
	        result.printNodes();
	        //show AST in GUI
	        JFrame frame = new JFrame("Antlr AST");
	        JPanel panel = new JPanel();
	        TreeViewer viewr = new TreeViewer(Arrays.asList(parser.getRuleNames()),tree);
	        viewr.setScale(1.5); //scale option
	        panel.add(viewr);
	        frame.add(panel);
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.pack();
	        frame.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}
