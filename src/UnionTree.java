import java.util.ArrayList;

public class UnionTree<T> {
	private ArrayList<TreeNode<T>> roots; 
	
	public TreeNode<T> getRoot(int i)
	{
		return roots.get(i);
	}
	public UnionTree()
	{
		roots=new ArrayList<TreeNode<T>>();
	}
    public TreeNode<T> makeElement(T rootData) {
    	//put a new element in the tree as a new type.
        TreeNode<T> root = new TreeNode<T>();
        root.data = rootData;
        root.children = new ArrayList<TreeNode<T>>();
        roots.add(root);
        return root;
    }
    public int size()
    {
    	return roots.size();
    }
    public void mergeAtoB(TreeNode<T> A, TreeNode<T> B)
    {
    	B.children.add(A);
    	if(roots.contains(A))
    		roots.remove(A);
    	A.parent=B;
    }
    public int find(TreeNode<T> n){
    	if(n==null)
    		return -1;
    	if(n.parent==null && roots.contains(n))
    		return roots.indexOf(n);
    	return find(n.parent);
    	
    }
    public boolean samePartition(TreeNode<T> A, TreeNode<T> B)
    {
    	return find(A)==find(B);
    }
    public TreeNode<T> search(ArrayList<TreeNode<T>> roots,T data)
    {
    	for(int i=0;i<roots.size();i++)
    	{
    		TreeNode<T> in=search(roots.get(i),data);
    		if(in!=null)
    			return in;
    	}
    	return null;
    }
    public TreeNode<T> search(TreeNode<T> r,T data)
    {
    	if(r==null)
    		return null;
    	if(r!=null && r.data!=null&& r.data.equals(data))
    		return r;
    	else if(r.children!=null)
    		for(int i=0;i<r.children.size();i++)
    		{
    			TreeNode<T> in=search(r.children.get(i),data);
    			if(in!=null)
    				return in;
    		}
    			
		return null;
    }
    public TreeNode<T> search(T data)
    {
    	return search(roots,data);
    }
    public ArrayList<TreeNode<T>> descendant_helper(TreeNode<T> n,ArrayList<TreeNode<T>> r)
    {
    	if(n!=null)
    	{
    		r.add(n);
    		for(int i=0;i<n.children.size();i++)
    		{
    			descendant_helper(n.children.get(i),r);
    		}
    	}
    	return r;
    }
    public ArrayList<TreeNode<T>> descendant(TreeNode<T> n)
    {
    	ArrayList<TreeNode<T>> result=new ArrayList<TreeNode<T>>();
    	return descendant_helper(n, result);
    }
    public static void main(String[] args)
    {
    	UnionTree<String> tree=new UnionTree<String>();
    	TreeNode<String> a1=(TreeNode<String>) tree.makeElement("a");
    	TreeNode<String> b1=(TreeNode<String>) tree.makeElement("b");
//    	System.out.println(tree.samePartition(a1, b1));
    	tree.mergeAtoB(a1, b1);
//    	System.out.println(tree.samePartition(a1, b1));
//    	System.out.println(tree.search("a").data);
//    	System.out.println(tree.search("b").data);
    	
    }
    

    
}