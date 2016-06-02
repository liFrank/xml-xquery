import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		ArrayList<LinkedHashMap<String,String>> partitions=new ArrayList<LinkedHashMap<String,String>>();
		//from the variable to its partition.
		LinkedHashMap<String,LinkedHashMap<String,String>> fromvartopartition=new LinkedHashMap<String,LinkedHashMap<String,String>>();
		//inside each partition, there is a where-list.
		ArrayList<StringBuilder> wherelist=new ArrayList<StringBuilder>();
		
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
					LinkedHashMap<String,String> partition=new LinkedHashMap<String,String>();
					partition.put(current_element.getText(), begin.getText());
					partitions.add(partition);
					fromvartopartition.put(current_element.getText(), partition);
					System.out.println("begin a partition!:"+partitions.indexOf(partition));
					wherelist.add(new StringBuilder(" "));
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
		String whereContent=whereclause.getChild(1).getText();
		System.out.println("String of where:");
		String [] condition=whereContent.split("and");
		ArrayList<String> cond=new ArrayList<String>();
		for(int i=0;i<condition.length;i++)
		{
			System.out.println(condition[i]);
			cond.add(condition[i]);
		}
		boolean initial_join_flag=true;
		String join="";
		String first="";
		//cause professor says on piazza, the testcase can either be complete joinize or not.
		while(partitions.size()>=2)
		{
			//in the loop, we do one join-rewriting.
			ArrayList<String> firstjointarget=new ArrayList<String>();
			ArrayList<String> secondjointarget=new ArrayList<String>();
			for(int i=0;i<cond.size();i++)
			{
				//System.out.println("!!!"+cond.toString());
				String currentCond=cond.get(i);
				String [] condleftandright=currentCond.split("eq|=");
				LinkedHashMap<String,String> leftp=fromvartopartition.get(condleftandright[0]);
				LinkedHashMap<String,String> rightp=fromvartopartition.get(condleftandright[1]);
				if(leftp==rightp||!condleftandright[0].contains("$")||!condleftandright[1].contains("$"))
				{//cannot be rewrite to join.
					int partition_index;
					if(!condleftandright[0].contains("$"))
					{
						partition_index=partitions.indexOf(rightp);
					}
					else
					{
						partition_index=partitions.indexOf(leftp);
					}
					wherelist.get(partition_index).append(" and "+ condleftandright[0]+" eq "+condleftandright[1]+" " );
				}
				else if(leftp!=rightp && leftp!=null &&rightp!=null)
				{//the condition that can be join.
					if(firstjointarget.isEmpty() && secondjointarget.isEmpty())
					{
						firstjointarget.add(condleftandright[0].substring(1));
						secondjointarget.add(condleftandright[1].substring(1));
						
					}
					else if(fromvartopartition.get("$"+firstjointarget.get(0))==leftp && fromvartopartition.get("$"+secondjointarget.get(0))==rightp )
					{
						firstjointarget.add(condleftandright[0].substring(1));
						secondjointarget.add(condleftandright[1].substring(1));
						
					}
					else if(fromvartopartition.get("$"+secondjointarget.get(0))==leftp && fromvartopartition.get("$"+firstjointarget.get(0))==rightp )
					{
						secondjointarget.add(condleftandright[0].substring(1));
						firstjointarget.add(condleftandright[1].substring(1));
						
					}
					else
						continue;
				}
				else
					continue;
				cond.remove(currentCond);
				i--;
			}
			//System.out.println("!"+firstjointarget.toString());
			//System.out.println("!!"+secondjointarget.toString());
			LinkedHashMap<String,String> partitionleft=fromvartopartition.get("$"+firstjointarget.get(0));
			LinkedHashMap<String,String> partitionright=fromvartopartition.get("$"+secondjointarget.get(0));
			
			if(initial_join_flag)
			{
				initial_join_flag=false;
				first="for ";
				for(String var:partitionleft.keySet())
				{
					first+= var+ " in "+partitionleft.get(var)+",\n";
				}
				first=first.substring(0,first.length()-3);
				first+="\n";
				StringBuilder where=wherelist.get(partitions.indexOf(partitionleft));
				System.out.println("StringBuilder"+where.toString());
				if(!where.toString().equals(" "))
				{
					String wherestring="where "+where.substring(5)+"\n";
					first+=wherestring;
				}
				first+="return <tuple> {\n";
				for(String var:partitionleft.keySet())
				{
					first+="<"+var.substring(1)+">{\t"+var+"\t}"+"</"+var.substring(1)+">,";
				}
				first=first.substring(0, first.length()-1)+"\n}</tuple>,\n";
				
				//wherelist.remove(partitions.indexOf(partitionleft));
				//partitions.remove(partitionleft);
			}
			//first="join( "+first;
			String second="for ";
			for(String var:partitionright.keySet())
			{
				second+= var+ " in "+partitionright.get(var)+",\n";
			}
			second=second.substring(0,second.length()-3);
			second+="\n";
			StringBuilder where=wherelist.get(partitions.indexOf(partitionright));
			System.out.println("StringBuilder"+where.toString());
			if(!where.toString().equals(" "))
			{
				String wherestring="where "+where.substring(5)+"\n";
				second+=wherestring;
			}
			second+="return <tuple> {\n";
			for(String var:partitionright.keySet())
			{
				second+="<"+var.substring(1)+">{\t"+var+"\t}"+"</"+var.substring(1)+">,";
			}
			second=second.substring(0, second.length()-1)+"\n}</tuple>,\n";
			first+=second;
			first+=firstjointarget.toString();
			first+=",";
			first+=secondjointarget.toString();
			first+=")\n,";
			
			join="join( "+first;
			first=join;
			//merge the partition on the right to the partition on the left.
			for(Map.Entry<String,String> m: partitionright.entrySet())
			{
				partitionleft.put(m.getKey(), m.getValue());
				fromvartopartition.replace(m.getKey(), partitionleft);
			}
			wherelist.remove(partitions.indexOf(partitionright));
			partitions.remove(partitionright);
			System.out.println(first);
		}
		result="for $tuple in "+join.substring(0,join.length()-1);
		//dealing with return clause.
		String returnclauseString=returnclause.getText();
		System.out.println(returnclauseString);
		Pattern pattern1=Pattern.compile("\\$\\w+\\/text\\(\\)");//text()
		Pattern pattern2=Pattern.compile("\\$\\w+\\/[^\\/]");//not //
		Pattern pattern3=Pattern.compile("\\$\\w+[,| |\\n|}|\\t]");//nothing after
		
		Matcher matcher1=pattern1.matcher(returnclauseString);
		while(matcher1.find())
		{
			String m1=matcher1.group(0).replace("/", "//");
			returnclauseString=returnclauseString.replace(matcher1.group(0), m1);
		}
		Matcher matcher2=pattern2.matcher(returnclauseString);
		while(matcher2.find())
		{
			String m2=matcher2.group(0);
			returnclauseString=returnclauseString.replace(m2, m2.substring(0, m2.length()-2)+"/*"+m2.substring(m2.length()-2));
		}
		Matcher matcher3= pattern3.matcher(returnclauseString);
		while(matcher3.find())
		{
			String m3=matcher3.group(0);
			returnclauseString=returnclauseString.replace(m3, m3.substring(0, m3.length()-1)+"/*"+m3.substring(m3.length()-1));
		}
		
		result+=returnclauseString.replace("$","$tuple/");
		
		if(resultTag!=null)
		{
			result = "<" + resultTag + ">  \n{\n" + result + "\n}\n  </" + resultTag + ">";
		}
		return result;
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
