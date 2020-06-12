import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class ClassInfoTreeModel implements TreeModel {
	ClassInfo classInfo;
	
	public ClassInfoTreeModel(ClassInfo classInfo) {
		this.classInfo = classInfo;
	}
	
	@Override
	public Object getRoot() {
		return classInfo;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent instanceof ClassInfo) {
			ClassInfo c = (ClassInfo)parent;
			return c.getMemberInfo(index);
		}
		
		return null;
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent instanceof ClassInfo) {
			ClassInfo c = (ClassInfo)parent;
			return c.getMemberInfoSize();
		}
		
		return 0;
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent instanceof ClassInfo) {
			ClassInfo c = (ClassInfo)parent;
			c.getMemberIndex((MemberInfo)child);
		}
		
		return 0;
	}
	
	@Override
	public boolean isLeaf(Object node) {
		if (node instanceof MethodInfo) {
			return true;
		} else if (node instanceof FieldInfo) {
			return true;
		}
		
		return false;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {  }
	@Override
	public void addTreeModelListener(TreeModelListener l) {  }
	@Override
	public void removeTreeModelListener(TreeModelListener l) {	}

}
