import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.omg.CORBA.CTX_RESTRICT_SCOPE;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class EvalVisitor extends XqueryBaseVisitor<IXqueryValue>{
	Stack<XqueryNodes> rpContext = new Stack<XqueryNodes>();
//	ArrayList<Node> xqcur=new ArrayList<Node>();
//	@Override 
//	public ArrayList<Node> visitFilterAnd(XqueryParser.FilterAndContext ctx)
//	{
//		ArrayList<Node> left=visit(ctx.f(0));
//		ArrayList<Node> right=visit(ctx.f(1));
//		if(!left.isEmpty() &&!right.isEmpty())
//		{
//			return left;//just return either of them, because it stands true.
//		}
//		return new ArrayList<Node>();//null, stands false.
//	}
//	@Override
//	public ArrayList<Node> visitFilterOr(XqueryParser.FilterOrContext ctx)
//	{
//		ArrayList<Node> left=visit(ctx.f(0));
//		if(!left.isEmpty())
//			return left;
//		ArrayList<Node> right=visit(ctx.f(1));
//		if(!right.isEmpty())
//			return right;
//		return new ArrayList<Node>();
//	}
//	@Override 
//	public ArrayList<Node> visitFilterIs(XqueryParser.FilterIsContext ctx)
//	{
//		ArrayList<Node> tmp1=cur;
//		ArrayList<Node> left=visit(ctx.rp(0));
//		cur=tmp1;
//		ArrayList<Node> tmp2=cur;
//		ArrayList<Node> right=visit(ctx.rp(1));
//		cur=tmp2;
//		for(int i=0;i<left.size();i++)
//			for(int j=0;j<right.size();j++)
//				if(left.get(i).isEqualNode(right.get(j)))
//					return tmp2;
//		return new ArrayList<Node>();
//	}
//	@Override 
//	public ArrayList<Node> visitFilterEqual(XqueryParser.FilterEqualContext ctx)
//	{
//		ArrayList<Node> tmp1=cur;
//		ArrayList<Node> left=visit(ctx.rp(0));
//		cur=tmp1;
//		ArrayList<Node> tmp2=cur;
//		ArrayList<Node> right=visit(ctx.rp(1));
//		cur=tmp2;
//		for(int i=0;i<left.size();i++)
//			for(int j=0;j<right.size();j++)
//				if(left.get(i)==right.get(j))
//					return tmp2;
//		return new ArrayList<Node>();
//	}
//	@Override 
//	public ArrayList<Node> visitFilterNot(XqueryParser.FilterNotContext ctx)
//	{
//		ArrayList<Node> not=visit(ctx.f());
//		if(not.isEmpty())
//			return cur;
//		return new ArrayList<Node>();
//	}
//	@Override 
//	public ArrayList<Node> visitFilterParan(XqueryParser.FilterParanContext ctx)
//	{
//		return visit(ctx.f());
//	}
//	@Override 
//	public ArrayList<Node> visitFilter(XqueryParser.FilterContext ctx)
//	{
//		ArrayList<Node> temp=cur;//protect context.
//		ArrayList<Node> r=visit(ctx.rp());
//		cur=temp;
//		return r;
//	}
//	@Override 
//	public ArrayList<Node> visitConditionEqual(XqueryParser.ConditionEqualContext ctx)
//	{
//		ArrayList<Node> tmp1=xqcur;
//		ArrayList<Node> left=visit(ctx.xq(0));
//		xqcur=tmp1;
//		ArrayList<Node> tmp2=xqcur;
//		ArrayList<Node> right=visit(ctx.xq(1));
//		xqcur=tmp2;
//		for(int i=0;i<left.size();i++)
//			for(int j=0;j<right.size();j++)
//				if(left.get(i).isEqualNode(right.get(j)))
//				{
//					return tmp2;
//				}
//		return new ArrayList<Node>(); 
//	}
//	@Override 
//	public ArrayList<Node> visitConditionIs(XqueryParser.ConditionIsContext ctx)
//	{
//		ArrayList<Node> tmp1=xqcur;
//		ArrayList<Node> left=visit(ctx.xq(0));
//		xqcur=tmp1;
//		ArrayList<Node> tmp2=xqcur;
//		ArrayList<Node> right=visit(ctx.xq(1));
//		xqcur=tmp2;
//		for(int i=0;i<left.size();i++)
//			for(int j=0;j<right.size();j++)
//				if(left.get(i)==right.get(j))
//				{
//					return tmp2;
//				}
//		return new ArrayList<Node>(); 
//	}
//	@Override
//	public ArrayList<Node> visitConditionParanth(XqueryParser.ConditionParanthContext ctx)
//	{
//		return visit(ctx.cond());
//	}
//	@Override public ArrayList<Node> visitConditionAnd(XqueryParser.ConditionAndContext ctx)
//	{
//		ArrayList<Node> left= visit(ctx.cond(0));
//		ArrayList<Node> right = visit(ctx.cond(1));
//		if (!left.isEmpty() && !right.isEmpty()){
//				return left;
//		}
//		return new ArrayList<Node>();
//	}
//	@Override public ArrayList<Node> visitConditionOr(XqueryParser.ConditionOrContext ctx)
//	{
//		ArrayList<Node> left=visit(ctx.cond(0));
//		if(!left.isEmpty())
//		{
//			return left;
//		}
//		ArrayList<Node> right = visit(ctx.cond(1));
//		if ( !right.isEmpty()){
//			return right;
//		}
//		return new ArrayList<Node>();
//	}
	
	public ArrayList<Node> Doc(String name)
	{
		//open a file and change the current.
		File inputFile = new File(name);
        DocumentBuilderFactory dbFactory= DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //dBuilder.setEntityResolver(resolver);
        Document doc = null;
		try {
			doc = dBuilder.parse(inputFile);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        doc.getDocumentElement().normalize();
		ArrayList<Node> result = new ArrayList<Node>();
		result.add((Node)doc);
		return result;
        
	}
	@Override public XqueryNodes visitAPChildren(XqueryParser.APChildrenContext ctx)
	{
		//visit doc
		String filename = ctx.String().getText();
		filename = filename.substring(1, filename.length()-1); // strip leading and trailing \"
		XqueryNodes root = new XqueryNodes(Doc(filename));
		rpContext.push(root.getChildren());
		XqueryNodes returnVal = (XqueryNodes) visit(ctx.rp());
		for (int i = 0; i < returnVal.size(); i++) {
			Node child = returnVal.get(i);
			System.out.println("Node name: " + child.getNodeName());
			System.out.println("Node value: " + child.getNodeValue());
			System.out.println("Node type: " + child.getNodeType());
		}
		rpContext.pop();
		return returnVal;
	}
	@Override public XqueryNodes visitAPBoth(XqueryParser.APBothContext ctx)
	{
		String filename = ctx.String().getText();
		filename = filename.substring(1, filename.length()-1); // strip leading and trailing \"
		XqueryNodes root = new XqueryNodes(Doc(filename));
		rpContext.push(root.getDescendants());
		XqueryNodes returnVal = (XqueryNodes) visit(ctx.rp());
		for (int i = 0; i < returnVal.size(); i++) {
			Node child = returnVal.get(i);
			System.out.println("Node name: " + child.getNodeName());
			System.out.println("Node value: " + child.getNodeValue());
			System.out.println("Node type: " + child.getNodeType());
		}
		rpContext.pop();
		return returnVal;
	}
	@Override public XqueryNodes visitRPName(XqueryParser.RPNameContext ctx) 
	{ 
		String tagName = ctx.getText();
		XqueryNodes cur = rpContext.peek();
		XqueryNodes returnVal = cur.getNodes(tagName);
		return returnVal;
	}
	
//	@Override public ArrayList<Node> visitRPCurrent(XqueryParser.RPCurrentContext ctx) 
//	{
//		return cur;
//	}
//	@Override public ArrayList<Node> visitRPParents(XqueryParser.RPParentsContext ctx) 
//	{
//		ArrayList<Node> parent=new ArrayList<Node>();
//		for(int i=0;i<cur.size();i++)
//		{
//			parent.add(cur.get(i).getParentNode());
//		}
//		cur=parent;
//		return cur;
//	}
//	@Override public ArrayList<Node> visitRPParanth(XqueryParser.RPParanthContext ctx) 
//	{
//		return (visit(ctx.rp()));
//	}
//	
//	@Override public ArrayList<Node> visitRPWithRP(XqueryParser.RPWithRPContext ctx)
//	{
//		ArrayList<Node> tmp=new ArrayList<Node>();
//		tmp.addAll(cur);
//		ArrayList<Node> r=new ArrayList<Node>();
//		r.addAll(visit(ctx.rp(0)));
//		cur=tmp;
//		r.addAll(visit(ctx.rp(1)));
//		return r;
//	}
//	
	@Override public XqueryNodes visitRPChildren(XqueryParser.RPChildrenContext ctx) 
	{
		XqueryNodes x = (XqueryNodes) visit(ctx.rp(0));
		rpContext.push(x.getChildren());
		XqueryNodes y = (XqueryNodes) visit(ctx.rp(1));
		rpContext.pop();
		return y.unique();
	}

	@Override public XqueryNodes visitRPBoth(XqueryParser.RPBothContext ctx)
	{
		XqueryNodes x = (XqueryNodes) visit(ctx.rp(0));
		rpContext.push(x.getDescendants());
		XqueryNodes y = (XqueryNodes) visit(ctx.rp(1));
		rpContext.pop();
		return y.unique();
	}
	
//	@Override public ArrayList<Node> visitRPAll(XqueryParser.RPAllContext ctx)
//	{
//		ArrayList<Node> r=new ArrayList<Node>();
//		for(int i=0;i<cur.size();i++)
//		{
//			r.addAll(getChildren(cur.get(i)));
//		}
//		cur=r;
//		return r;
//	}
//	@Override public ArrayList<Node> visitRPText(XqueryParser.RPTextContext ctx)
//	{
//		for(int i=0;i<cur.size();i++)
//		{
//			for(int j=0;j<cur.get(i).getChildNodes().getLength();j++)
//			{
//				if(cur.get(i).getChildNodes().item(j).getNodeType() == org.w3c.dom.Node.TEXT_NODE )//sooner may be rewrite.
//				{
//					System.out.print(cur.get(i).getChildNodes().item(j).getTextContent());
//				}
//			}
//		}
//		return cur;
//	}
}
