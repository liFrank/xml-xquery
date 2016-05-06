
public class XqueryBoolean implements IXqueryValue {
	private boolean value;
	
	public XqueryBoolean(boolean val) {
		value = val;
	}
	
	public XqueryBoolean not() {
		return new XqueryBoolean(!value);
	}
	
	public XqueryBoolean or(XqueryBoolean op) {
		return new XqueryBoolean(this.value || op.getValue());
	}
	
	public XqueryBoolean and(XqueryBoolean op) {
		return new XqueryBoolean(this.value && op.getValue());
	}
	
	public boolean getValue() {
		return value;
	}
	
	//by jialong
	public boolean equals(XqueryBoolean v)
	{
		return value==v.getValue();
	}
	//by jialong ---is this a legal overloading?
	public boolean equals(boolean v)
	{
		return value==v;
	}
}
