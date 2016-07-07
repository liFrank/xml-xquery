import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

public class XqueryNodes implements IXqueryValue {
	private ArrayList<Node> nodes;
	
	public XqueryNodes() {
		nodes = new ArrayList<Node>();
	}

	public XqueryNodes(Node n) {
		nodes = new ArrayList<Node>();
		nodes.add(n);
	}
	
	public XqueryNodes(ArrayList<Node> val) {
		nodes = val;
	}
	
	public int size() {
		return nodes.size();
	}
	
	public Node get(int index) {
		if (nodes.size() > index) 
			return nodes.get(index);
		else
			return null;
	}
	public void clear(){
		nodes.clear();
	}
	public boolean add(Node n) {
		return nodes.add(n);
	}
	
	public XqueryNodes concat(XqueryNodes other) {
		ArrayList<Node> combined = new ArrayList<Node>();
		combined.addAll(this.nodes);
		for (int i = 0; i < other.size(); i++) {
			combined.add(other.get(i));
		}
		return new XqueryNodes(combined);
	}
	
	public XqueryNodes unique() { 
		return uniqueByValue();
	}
	
	public XqueryNodes uniqueById() { 
		ArrayList<Node> uniques = new ArrayList<Node>();
		Set<Node> uniqueSet = new HashSet<Node>(nodes);
		for (Node n : uniqueSet) 
			uniques.add(n);
		return new XqueryNodes(uniques);
	}
	
	public XqueryNodes uniqueByValue() { 
		ArrayList<Node> uniques = new ArrayList<Node>();
		for (int i = 0; i < nodes.size(); i++) {
			boolean add = true;
			Node candidate = nodes.get(i);
			for (int j = 0; j < nodes.size() && i != j; j++) {
				Node other = nodes.get(j);
				if (candidate.isEqualNode(other)) {
					add = false;
					break;
				}
			}
			if (add) 
				uniques.add(candidate);
		}
		return new XqueryNodes(uniques);
	}
	
	public boolean isEqualValue(XqueryNodes other) {
		for (int i = 0; i < nodes.size(); i++) {
			for (int j = 0; j < other.size(); j++) {
				if (nodes.get(i).isEqualNode(other.get(j))) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isEqualId(XqueryNodes other) {
		for (int i = 0; i < nodes.size(); i++) {
			for (int j = 0; j < other.size(); j++) {
				if (nodes.get(i).isSameNode(other.get(j))) {
					return true;
				}
			}
		}
		return false;
	}
	
	public XqueryNodes getNodes(String tagName) {
		ArrayList<Node> returnNodes = new ArrayList<Node>();
		for (int i = 0; i < nodes.size(); i++) {
			Node current = nodes.get(i);
			if (current.getNodeName().equals(tagName))
				returnNodes.add(current);
		}
		return new XqueryNodes(returnNodes);
	}
	
	public XqueryNodes getChildren() {
		return getChildren("");
	}
	
	public XqueryNodes getChildren(String path) {
		ArrayList<Node> childrenSubset = new ArrayList<Node>();
		for (int i = 0; i < nodes.size(); i++) {
			Node current = nodes.get(i);
			NodeList children = current.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				if (path.isEmpty() || child.getNodeName().equals(path))
					childrenSubset.add(child);
			}
		}
		return new XqueryNodes(childrenSubset);
	}
	
	public XqueryNodes getDescendants() {
		return getDescendants("");
	}
	
	public XqueryNodes getDescendants(String path) { 
		ArrayList<Node> descendants = new ArrayList<Node>();
		getDescendants(path, nodes, descendants);
		return new XqueryNodes(descendants);
	}
	
	public void getDescendants(String path, ArrayList<Node> parents, ArrayList<Node> returnVal) {
		if (parents.size() == 0) 
			return;
		ArrayList<Node> nextParents = new ArrayList<Node>();
		for (int i = 0; i < parents.size(); i++) {
			Node current = parents.get(i);
			NodeList children = current.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				nextParents.add(child);
				if (path.isEmpty() || child.getNodeName().equals(path)) 
					returnVal.add(child);
			}
		}
		getDescendants(path, nextParents, returnVal);
	}
	
	public XqueryNodes getParents() {
		ArrayList<Node> parents = new ArrayList<Node>();
		for (int i = 0; i < nodes.size(); i++) {
			Node current = nodes.get(i);
			parents.add(current.getParentNode());
		}
		return new XqueryNodes(parents);
	}
	
	public XqueryNodes getTextNodes() {
		ArrayList<Node> text = new ArrayList<Node>();
		for (int i = 0; i < nodes.size(); i++) {
			Node current = nodes.get(i);
			if (current.getNodeType() == Node.TEXT_NODE)
				text.add(current);
		}
		return new XqueryNodes(text);
	}
	
	public XqueryNodes getAttributeNodes(String attName) {
		ArrayList<Node> attribute = new ArrayList<Node>();
		for (int i = 0; i < nodes.size(); i++) {
			Node current = nodes.get(i);
			if (current.getNodeType() == Node.ATTRIBUTE_NODE && current.getNodeName().equals(attName))
				attribute.add(current);
		}
		return new XqueryNodes(attribute);
	}
	
	/**
     * Removes text nodes that only contains whitespace. The conditions for
     * removing text nodes, besides only containing whitespace, are: If the
     * parent node has at least one child of any of the following types, all
     * whitespace-only text-node children will be removed: - ELEMENT child -
     * CDATA child - COMMENT child
     * 
     * The purpose of this is to make the format() method (that use a
     * Transformer for formatting) more consistent regarding indenting and line
     * breaks.
     */

    private static void cleanEmptyTextNodes(Node parentNode) {
        boolean removeEmptyTextNodes = false;
        Node childNode = parentNode.getFirstChild();
        while (childNode != null) {
            removeEmptyTextNodes |= checkNodeTypes(childNode);
            childNode = childNode.getNextSibling();
        }

        if (removeEmptyTextNodes) {
            removeEmptyTextNodes(parentNode);
        }
    }

    private static void removeEmptyTextNodes(Node parentNode) {
        Node childNode = parentNode.getFirstChild();
        while (childNode != null) {
            // grab the "nextSibling" before the child node is removed
            Node nextChild = childNode.getNextSibling();

            short nodeType = childNode.getNodeType();
            if (nodeType == Node.TEXT_NODE) {
                boolean containsOnlyWhitespace = childNode.getNodeValue()
                        .trim().isEmpty();
                if (containsOnlyWhitespace) {
                    parentNode.removeChild(childNode);
                }
            }
            childNode = nextChild;
        }
    }

    private static boolean checkNodeTypes(Node childNode) {
        short nodeType = childNode.getNodeType();

        if (nodeType == Node.ELEMENT_NODE) {
            cleanEmptyTextNodes(childNode); // recurse into subtree
        }

        if (nodeType == Node.ELEMENT_NODE
                || nodeType == Node.CDATA_SECTION_NODE
                || nodeType == Node.COMMENT_NODE) {
            return true;
        } else {
            return false;
        }
    }
	
    // Used to determine attribute equality for join
	public static String getNodeString(Node node) {
	    try {
	    	StringWriter writer = new StringWriter();
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        transformer.transform(new DOMSource(node), new StreamResult(writer));
	        String output = writer.toString();
	        return output;
	    } catch (TransformerException e) {
	        e.printStackTrace();
	    }
	    return node.getTextContent();
	}
	
	// Used to print Node information
	public String getPrettyNodeString(Node node) {
		cleanEmptyTextNodes(node);
	    try {
	    	StringWriter writer = new StringWriter();
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", 2);
	        Transformer transformer = transformerFactory.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.transform(new DOMSource(node), new StreamResult(writer));
	        String output = writer.toString();
	        return output;
	    } catch (TransformerException e) {
	        e.printStackTrace();
	    }
	    return node.getTextContent();
	}
	
	public void printNodes() {
		for (int i = 0; i < nodes.size(); i++) {
			System.out.println(getPrettyNodeString(nodes.get(i)));
		}
	}
	
	public void printNodesToFile(String filename) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filename, "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < nodes.size(); i++) {
			writer.println(getPrettyNodeString(nodes.get(i)));
		}
		writer.close();
	}
}
