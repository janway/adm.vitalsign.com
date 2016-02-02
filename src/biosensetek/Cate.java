package biosensetek;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Cate implements Serializable {
	private static final long serialVersionUID = -8665275041085951497L;

	private String uid;
	private String display;
	private String name;
	private int items, shops;
	private Cate parent;
	private ArrayList<Cate> children;

	public Cate() {
		super();
	}

	public String uid() {
		return uid;
	}

	public Cate uid(String uid) {
		this.uid = uid;
		return this;
	}

	public String display() {
		return display;
	}

	public Cate display(String display) {
		this.display = display;
		return this;
	}

	public String name() {
		return name;
	}

	public Cate name(String name) {
		this.name = name;
		return this;
	}

	public int items() {
		return items;
	}

	public Cate items(int items) {
		this.items = items;
		return this;
	}

	public int shops() {
		return shops;
	}

	public Cate shops(int shops) {
		this.shops = shops;
		return this;
	}

	public Cate parent() {
		return parent;
	}

	private ArrayList<Cate> parents;

	public ArrayList<Cate> parents() {
		if (parents == null) {
			parents = new ArrayList<Cate>(2);
			Cate cate = parent;
			while (cate != null) {
				parents.add(cate);
				cate = cate.parent;
			}
			Collections.reverse(parents);
		}
		return parents;
	}

	public ArrayList<Cate> children(Cate... child) {
		if (child != null) {
			for (Cate cate : child) {
				if (cate == null) continue;
				if (children == null) children = new ArrayList<>();
				children.add(cate);
				cate.parent = this;
			}
		}
		return children;
	}

	protected void check() {
		if (children == null) return;
		for (Cate child : children) {
			child.check();
			if (child.items > 0) {
				items += child.items;
			}
		}
	}
}