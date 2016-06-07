import java.util.List;

public class TreeNode<T> {
    public T data;
    public TreeNode<T> parent;
    public List<TreeNode<T>> children;
    public TreeNode()
    {
    	data=null;
    	parent=null;
    	children=null;
    }
}