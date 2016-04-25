import java.util.ArrayList;

import org.w3c.dom.*;

public class EvalVisitor extends XqueryBaseVisitor<ArrayList<Node>>{
	ArrayList<Node> cur=new ArrayList<Node>();
	ArrayList<Node> xqcur=new ArrayList<Node>();
	@Override 
	public ArrayList<Node> visitFilterAnd(XqueryParser.FilterAndContext ctx)
	{
		ArrayList<Node> left=visit(ctx.f(0));
		ArrayList<Node> right=visit(ctx.f(1));
		if(!left.isEmpty() &&!right.isEmpty())
		{
			return left;//just return either of them, because it stands true.
		}
		return new ArrayList<Node>();//null, stands false.
	}
	@Override
	public ArrayList<Node> visitFilterOr(XqueryParser.FilterOrContext ctx)
	{
		ArrayList<Node> left=visit(ctx.f(0));
		if(!left.isEmpty())
			return left;
		ArrayList<Node> right=visit(ctx.f(1));
		if(!right.isEmpty())
			return right;
		return new ArrayList<Node>();
	}
	@Override 
	public ArrayList<Node> visitFilterIs(XqueryParser.FilterIsContext ctx)
	{
		ArrayList<Node> tmp1=cur;
		ArrayList<Node> left=visit(ctx.rp(0));
		cur=tmp1;
		ArrayList<Node> tmp2=cur;
		ArrayList<Node> right=visit(ctx.rp(1));
		cur=tmp2;
		for(int i=0;i<left.size();i++)
			for(int j=0;j<right.size();j++)
				if(left.get(i).isEqualNode(right.get(j)))
					return tmp2;
		return new ArrayList<Node>();
	}
	@Override 
	public ArrayList<Node> visitFilterEqual(XqueryParser.FilterEqualContext ctx)
	{
		ArrayList<Node> tmp1=cur;
		ArrayList<Node> left=visit(ctx.rp(0));
		cur=tmp1;
		ArrayList<Node> tmp2=cur;
		ArrayList<Node> right=visit(ctx.rp(1));
		cur=tmp2;
		for(int i=0;i<left.size();i++)
			for(int j=0;j<right.size();j++)
				if(left.get(i)==right.get(j))
					return tmp2;
		return new ArrayList<Node>();
	}
	@Override 
	public ArrayList<Node> visitFilterNot(XqueryParser.FilterNotContext ctx)
	{
		ArrayList<Node> not=visit(ctx.f());
		if(not.isEmpty())
			return cur;
		return new ArrayList<Node>();
	}
	@Override 
	public ArrayList<Node> visitFilterParan(XqueryParser.FilterParanContext ctx)
	{
		return visit(ctx.f());
	}
	@Override 
	public ArrayList<Node> visitFilter(XqueryParser.FilterContext ctx)
	{
		ArrayList<Node> temp=cur;//protect context.
		ArrayList<Node> r=visit(ctx.rp());
		cur=temp;
		return r;
	}
	@Override 
	public ArrayList<Node> visitConditionEqual(XqueryParser.ConditionEqualContext ctx)
	{
		ArrayList<Node> tmp1=xqcur;
		ArrayList<Node> left=visit(ctx.xq(0));
		xqcur=tmp1;
		ArrayList<Node> tmp2=xqcur;
		ArrayList<Node> right=visit(ctx.xq(1));
		xqcur=tmp2;
		for(int i=0;i<left.size();i++)
			for(int j=0;j<right.size();j++)
				if(left.get(i).isEqualNode(right.get(j)))
				{
					return tmp2;
				}
		return new ArrayList<Node>(); 
	}
	@Override 
	public ArrayList<Node> visitConditionIs(XqueryParser.ConditionIsContext ctx)
	{
		ArrayList<Node> tmp1=xqcur;
		ArrayList<Node> left=visit(ctx.xq(0));
		xqcur=tmp1;
		ArrayList<Node> tmp2=xqcur;
		ArrayList<Node> right=visit(ctx.xq(1));
		xqcur=tmp2;
		for(int i=0;i<left.size();i++)
			for(int j=0;j<right.size();j++)
				if(left.get(i)==right.get(j))
				{
					return tmp2;
				}
		return new ArrayList<Node>(); 
	}
	@Override
	public ArrayList<Node> visitConditionParanth(XqueryParser.ConditionParanthContext ctx)
	{
		return visit(ctx.cond());
	}
	@Override public ArrayList<Node> visitConditionAnd(XqueryParser.ConditionAndContext ctx)
	{
		ArrayList<Node> left= visit(ctx.cond(0));
		ArrayList<Node> right = visit(ctx.cond(1));
		if (!left.isEmpty() && !right.isEmpty()){
				return left;
		}
		return new ArrayList<Node>();
	}
	@Override public ArrayList<Node> visitConditionOr(XqueryParser.ConditionOrContext ctx)
	{
		ArrayList<Node> left=visit(ctx.cond(0));
		if(!left.isEmpty())
		{
			return left;
		}
		ArrayList<Node> right = visit(ctx.cond(1));
		if ( !right.isEmpty()){
			return right;
		}
		return new ArrayList<Node>();
	}
	
}
