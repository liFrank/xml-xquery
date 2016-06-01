import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Rewriter {
	public static String rewrite_main(ParseTree tree)
	{
		String result="";
		//first find the entry point contains "for".
		ParseTree begin=tree;
		String resultTag=null;
		if(!tree.getChild(0).getText().contains("for"))
		{
			resultTag=begin.getChild(1).getText();
			System.out.println("the resultTag is "+resultTag);
			for(int i=0;i<tree.getChildCount();i++)
			{
				if(tree.getChild(i).getText().contains("for"))
				{
					begin=tree.getChild(i);
					break;//here we stop finding the nested "for"
				}
			}
			
		}
		//get the for, where, return clause content.
		ParseTree forclause=begin.getChild(0);
		ParseTree whereclause=begin.getChild(1);
		ParseTree returnclause=begin.getChild(2);
		
		//The partitions in the map.
		ArrayList<HashMap<String,String>> partitions=new ArrayList<HashMap<String,String>>();
		//from the variable to its partition.
		HashMap<String,HashMap<String,String>> fromvartopartition=new HashMap<String,HashMap<String,String>>();
		
		//first let us analysis for clause;
		for(int i=0;i<forclause.getChildCount();i++)
		{
			ParseTree current_element=forclause.getChild(i);
			if(current_element.getText().contains("$"))
			{
				begin=forclause.getChild(i+2);
				System.out.println("var: "+current_element.getText());
				System.out.println("in :" +begin.getText());
				if(begin.getText().contains("document") ||begin.getText().contains("doc"))
				{
					HashMap<String,String> partition=new HashMap<String,String>();
					partition.put(current_element.getText(), begin.getText());
					partitions.add(partition);
					fromvartopartition.put(current_element.getText(), partition);
					System.out.println("begin a partition!:"+partitions.indexOf(partition));
				}
				else
				{
					//find which partition it belongs.
					for(int j=0;j<partitions.size();j++)
					{
						String parents=begin.getChild(0).getChild(0).getText();
						if(partitions.get(j).containsKey(parents))
						{
							partitions.get(j).put(current_element.getText(), begin.getText());
							fromvartopartition.put(current_element.getText(), partitions.get(j));
							System.out.println("depends on :"+j);
							break;
						}
					}
				}
				i+=2;
			}
		}
		if(partitions.size()<2)
			return "";
		
		
		if(resultTag!=null)
		{
			result = "<" + resultTag + ">  \n{\n" + result + "\n}\n  </" + resultTag + ">";
		}
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
