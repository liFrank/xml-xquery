import java.io.StringWriter;
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
	
	public String getNodeString(Node node) {
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
		//System.out.println("size of nodes: "+nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
//			printNode(nodes.get(i));
			System.out.println(getNodeString(nodes.get(i)));
		}
	}
	
//	private void printNode(Node rootNode) {
//	    System.out.print(rootNode.getNodeName());
//	    if (rootNode.getNodeType() == Node.ELEMENT_NODE) {
//	    	System.out.print(", Node Type: " + "ELEMENT_NODE");
//	    	String textContent = "";
//	    	NodeList children = rootNode.getChildNodes();
//	    	for (int i = 0; i < children.getLength(); i++) {
//				Node child = children.item(i);
//				if (child.getNodeType() == Node.TEXT_NODE) {
//					textContent = child.getTextContent();
//				}
//	    	}
//	    	if (!textContent.isEmpty())
//	    		System.out.println(" -> " + textContent);
//	    }
//	    else if (rootNode.getNodeType() == Node.TEXT_NODE) {
//	    	System.out.print(", Node Type: " + "TEXT_NODE");
//	    	String textContent = rootNode.getTextContent();
//	    	System.out.println(" -> " + textContent);
//	    }
//	    else if (rootNode.getNodeType() == Node.ATTRIBUTE_NODE) {
//	    	System.out.println(", Node Type: " + "ATTRIBUTE_NODE");
//	    }
//	    else {
//	    	System.out.println(", Node Type: " + rootNode.getNodeType());
//	    }
//	}
}
