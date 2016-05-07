import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.omg.CORBA.CTX_RESTRICT_SCOPE;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class EvalVisitor extends XqueryBaseVisitor<IXqueryValue>{
	private Document document;
	private Stack<XqueryNodes> rpContext;
	private Stack<LinkedHashMap<String, XqueryNodes>> scopeContext;
	
	public EvalVisitor() {
		super(); // may be unnecessary
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null; 
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		document = builder.newDocument();
		rpContext = new Stack<XqueryNodes>();
		scopeContext = new Stack<LinkedHashMap<String, XqueryNodes>>();
		scopeContext.push(new LinkedHashMap<String, XqueryNodes>());	
	}

	public LinkedHashMap<String, XqueryNodes> deepCopy(LinkedHashMap<String, XqueryNodes> hashMap) {
		LinkedHashMap<String, XqueryNodes> copy = new LinkedHashMap<String, XqueryNodes>();
	    for(String key : hashMap.keySet()){
	        copy.put(key, hashMap.get(key));
	    }
	    return copy;
	}
	
	// Evaluate where clause using every combination of ind. elements in variables from the context. 
	// In base case, evaluate where and return clauses using recursively generated combinations
	public void whereReturn(XqueryParser.XQForContext ctx, int keyIndex, XqueryNodes returnVal) {
		// base case
		int numVariables = ctx.forClause().Var().size();
		if (ctx.letClause() != null)
			numVariables = numVariables + ctx.letClause().Var().size();
		if (keyIndex >= numVariables) {
			XqueryBoolean condition = (XqueryBoolean) visit(ctx.whereClause());
			if (condition.getValue() == true) {
				XqueryNodes ret = (XqueryNodes) visit(ctx.returnClause());
				for (int i = 0; i < ret.size(); i++) 
					returnVal.add(ret.get(i)); // add to final return
			}
		}
		else {
			LinkedHashMap<String, XqueryNodes> currentContext = scopeContext.peek();
			String currentKey = null;
			XqueryNodes currentNodes = null;
			if (keyIndex < ctx.forClause().Var().size()) { // handle vars in forClause
				currentKey = ctx.forClause().Var(keyIndex).getText();
				currentNodes = (XqueryNodes) visit(ctx.forClause().xq(keyIndex));
			}
			else { // handle vars in letClause, should not be able to enter if there is no letClause
				int offsetKeyIndex = keyIndex - ctx.forClause().Var().size();
				currentKey = ctx.letClause().Var(offsetKeyIndex).getText();
				currentNodes = (XqueryNodes) visit(ctx.letClause().xq(offsetKeyIndex));
			}
			for (int i = 0; i < currentNodes.size(); i++) {
				Node singleNode = currentNodes.get(i);
				XqueryNodes xn = new XqueryNodes(singleNode);
				currentContext.put(currentKey, xn);
				whereReturn(ctx, keyIndex + 1, returnVal); // recursive call
			}
		}
	}
	
	@Override public XqueryNodes visitXQVar(XqueryParser.XQVarContext ctx) 
	{ 
		XqueryNodes ret = (XqueryNodes) scopeContext.peek().get(ctx.Var().getText()); 
		if (ret == null)// can return null if not found
			ret = new XqueryNodes(); // return empty
		return ret;
	}

	@Override public XqueryNodes visitXQString(XqueryParser.XQStringContext ctx) 
	{ 
		String string = ctx.String().getText();
		string = string.substring(1, string.length()-1); // strip leading and trailing \"
		Node textNode = document.createTextNode(string);
		return new XqueryNodes(textNode);
	}

	@Override public XqueryNodes visitXQAp(XqueryParser.XQApContext ctx) 
	{ 
		return (XqueryNodes) visit(ctx.ap()); 
	}

	@Override public XqueryNodes visitXQChildren(XqueryParser.XQChildrenContext ctx) 
	{ 
		XqueryNodes x = (XqueryNodes) visit(ctx.xq());
		rpContext.push(x.getChildren());
		XqueryNodes y = (XqueryNodes) visit(ctx.rp());
		rpContext.pop();
		return y.uniqueById();
	}

	@Override public XqueryNodes visitXQBoth(XqueryParser.XQBothContext ctx) 
	{ 
		XqueryNodes x = (XqueryNodes) visit(ctx.xq());
		rpContext.push(x.getDescendants());
		XqueryNodes y = (XqueryNodes) visit(ctx.rp());
		rpContext.pop();
		return y.uniqueById(); 
	}

	@Override public XqueryNodes visitXQParanth(XqueryParser.XQParanthContext ctx) 
	{ 
		return (XqueryNodes) visit(ctx.xq()); 
	}

	@Override public XqueryNodes visitXQWithXQ(XqueryParser.XQWithXQContext ctx) 
	{ 
		XqueryNodes left = (XqueryNodes) visit(ctx.xq(0));
		XqueryNodes right = (XqueryNodes) visit(ctx.xq(1));
		return left.concat(right);
	}

	@Override public XqueryNodes visitXQLet(XqueryParser.XQLetContext ctx) { 
		visit(ctx.letClause());
		return null;
	}

	@Override public XqueryNodes visitXQFor(XqueryParser.XQForContext ctx) 
	{ 
		LinkedHashMap<String, XqueryNodes> copy = deepCopy(scopeContext.peek());
		scopeContext.push(copy);
		XqueryNodes result = new XqueryNodes();
		if (ctx.whereClause() == null) { // Handle simple case w/o a whereClause
			visit(ctx.forClause());
			if (ctx.letClause() != null) { 
				visit(ctx.letClause());
			}
			result = (XqueryNodes) visit(ctx.returnClause());
		}
		else { // Handle whereClause
			whereReturn(ctx, 0, result);
		}
		scopeContext.pop();
		return result;
	}

	@Override public XqueryNodes visitXQTag(XqueryParser.XQTagContext ctx) 
	{ 
		String tagName = ctx.Name(0).getText();
		Node outer = document.createElement(tagName);
		XqueryNodes inner = (XqueryNodes) visit(ctx.xq());
		for (int i = 0; i < inner.size(); i++) {
			Node innerNode = document.importNode(inner.get(i), true);
			outer.appendChild(innerNode);
		}
		return new XqueryNodes(outer);
	}

	@Override public XqueryBoolean visitForClause(XqueryParser.ForClauseContext ctx) 
	{ 
		for (int i = 0; i < ctx.Var().size(); i++) {
			String var = ctx.Var(i).getText();
			XqueryNodes val = (XqueryNodes) visit(ctx.xq(i));
			scopeContext.peek().put(var, val);
		}
		return new XqueryBoolean(true); // unused return value
	}

	@Override public XqueryBoolean visitLetClause(XqueryParser.LetClauseContext ctx) 
	{ 
		// modify current context scope
		for (int i = 0; i < ctx.Var().size(); i++) {
			String var = ctx.Var(i).getText();
			XqueryNodes val = (XqueryNodes) visit(ctx.xq(i));
			scopeContext.peek().put(var, val);
		}
		return new XqueryBoolean(true); // unused return value
	}

	@Override public XqueryBoolean visitWhereClause(XqueryParser.WhereClauseContext ctx) 
	{ 
		return (XqueryBoolean) visit(ctx.cond());
	}

	@Override public XqueryNodes visitReturnClause(XqueryParser.ReturnClauseContext ctx) { 
		return (XqueryNodes) visit(ctx.xq()); 
	}

	@Override 
	public XqueryBoolean visitFilterAnd(XqueryParser.FilterAndContext ctx)
	{
		XqueryBoolean left = (XqueryBoolean) visit(ctx.f(0));
		XqueryBoolean right = (XqueryBoolean) visit(ctx.f(1));
		return left.and(right);
	}
	@Override
	public XqueryBoolean visitFilterOr(XqueryParser.FilterOrContext ctx)
	{
		XqueryBoolean left = (XqueryBoolean) visit(ctx.f(0));
		XqueryBoolean right = (XqueryBoolean) visit(ctx.f(1));
		return left.or(right);
	}
	@Override 
	public XqueryBoolean visitFilterIs(XqueryParser.FilterIsContext ctx)
	{
		XqueryNodes left = (XqueryNodes) visit(ctx.rp(0));
		XqueryNodes right = (XqueryNodes) visit(ctx.rp(1));
		return new XqueryBoolean(left.isEqualId(right));
	}
	@Override 
	public XqueryBoolean visitFilterEqual(XqueryParser.FilterEqualContext ctx)
	{
		XqueryNodes left = (XqueryNodes) visit(ctx.rp(0));
		XqueryNodes right = (XqueryNodes) visit(ctx.rp(1));
		return new XqueryBoolean(left.isEqualValue(right));
	}
	@Override 
	public XqueryBoolean visitFilterNot(XqueryParser.FilterNotContext ctx)
	{
		XqueryBoolean op = (XqueryBoolean) visit(ctx.f());
		return op.not();
	}
	@Override 
	public XqueryBoolean visitFilterParan(XqueryParser.FilterParanContext ctx)
	{
		return (XqueryBoolean) visit(ctx.f());
	}
	@Override 
	public XqueryBoolean visitFilter(XqueryParser.FilterContext ctx)
	{
		XqueryNodes x = (XqueryNodes) visit(ctx.rp());
		if (x.size() > 0)
			return new XqueryBoolean(true);
		return new XqueryBoolean(false);
	}
	@Override 
	public XqueryBoolean visitConditionEqual(XqueryParser.ConditionEqualContext ctx)
	{
		XqueryNodes left = (XqueryNodes) visit(ctx.xq(0));
		XqueryNodes right = (XqueryNodes) visit(ctx.xq(1));
		return new XqueryBoolean(left.isEqualValue(right));
	}
	@Override 
	public XqueryBoolean visitConditionIs(XqueryParser.ConditionIsContext ctx)
	{
		XqueryNodes left = (XqueryNodes) visit(ctx.xq(0));
		XqueryNodes right = (XqueryNodes) visit(ctx.xq(1));
		return new XqueryBoolean(left.isEqualId(right));
	}
	@Override
	public XqueryBoolean visitConditionParanth(XqueryParser.ConditionParanthContext ctx)
	{
		return (XqueryBoolean) visit(ctx.cond());
	}
	@Override public XqueryBoolean visitConditionAnd(XqueryParser.ConditionAndContext ctx)
	{
		XqueryBoolean left = (XqueryBoolean) visit(ctx.cond(0));
		XqueryBoolean right = (XqueryBoolean) visit(ctx.cond(1));
		return left.and(right);
	}
	@Override public XqueryBoolean visitConditionOr(XqueryParser.ConditionOrContext ctx)
	{
		XqueryBoolean left = (XqueryBoolean) visit(ctx.cond(0));
		XqueryBoolean right = (XqueryBoolean) visit(ctx.cond(1));
		return left.or(right);
	}
	
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
		rpContext.pop();
//		returnVal.printNodes();
		return returnVal;
	}
	@Override public XqueryNodes visitAPBoth(XqueryParser.APBothContext ctx)
	{
		String filename = ctx.String().getText();
		filename = filename.substring(1, filename.length()-1); // strip leading and trailing \"
		XqueryNodes root = new XqueryNodes(Doc(filename));
		rpContext.push(root.getDescendants());
		XqueryNodes returnVal = (XqueryNodes) visit(ctx.rp());
		rpContext.pop();
//		returnVal.printNodes();
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
		return y.uniqueById();
	}

	@Override public XqueryNodes visitRPBoth(XqueryParser.RPBothContext ctx)
	{
		XqueryNodes x = (XqueryNodes) visit(ctx.rp(0));
		rpContext.push(x.getDescendants());
		XqueryNodes y = (XqueryNodes) visit(ctx.rp(1));
		rpContext.pop();
		return y.uniqueById();
	}
	
	@Override public XqueryNodes visitRPWithFilter(XqueryParser.RPWithFilterContext ctx) 
	{ 
		XqueryNodes returnVal = new XqueryNodes();
		XqueryNodes x = (XqueryNodes) visit(ctx.rp());
		// Process each node separately, evaluating each one with the filter 
		for (int i = 0; i < x.size(); i++) {
			Node singleNode = x.get(i);
			XqueryNodes xn = new XqueryNodes(singleNode);
			rpContext.push(xn.getChildren());
			XqueryBoolean filter = (XqueryBoolean) visit(ctx.f());
			if(filter.getValue() == true)
				returnVal.add(singleNode);
			rpContext.pop();
		}
		return returnVal;
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
	@Override public XqueryNodes visitRPText(XqueryParser.RPTextContext ctx)
	{
		XqueryNodes cur = rpContext.peek();
		XqueryNodes returnVal = cur.getTextNodes();
		return returnVal;
	}
}
