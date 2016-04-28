import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
	
	public XqueryNodes unique() { 
		ArrayList<Node> uniques = new ArrayList<Node>();
		Set<Node> uniqueSet = new HashSet<Node>(nodes);
		for (Node n : uniqueSet) 
			uniques.add(n);
		return new XqueryNodes(uniques);
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

}
