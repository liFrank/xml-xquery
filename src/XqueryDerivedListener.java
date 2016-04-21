
public class XqueryDerivedListener extends XqueryBaseListener {

	@Override
	public void enterXq(XqueryParser.XqContext ctx) { 
		for (int i = 0; i < ctx.depth(); i++) {
			System.out.print("\t");
		}
		System.out.println("Entering xq : " + ctx.getText());
	}
	
	@Override
	public void exitXq(XqueryParser.XqContext ctx) { 
		for (int i = 0; i < ctx.depth(); i++) {
			System.out.print("\t");
		}
		System.out.println("Exiting xq" );
	}
}
