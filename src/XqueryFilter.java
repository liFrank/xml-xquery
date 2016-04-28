
public class XqueryFilter implements IXqueryValue {
	private boolean value;
	
	public XqueryFilter(boolean val) {
		value = val;
	}
	
	public XqueryFilter not() {
		return new XqueryFilter(!value);
	}
	
	public XqueryFilter or(XqueryFilter op) {
		return new XqueryFilter(this.value || op.getValue());
	}
	
	public XqueryFilter and(XqueryFilter op) {
		return new XqueryFilter(this.value && op.getValue());
	}
	
	public boolean getValue() {
		return value;
	}
}
