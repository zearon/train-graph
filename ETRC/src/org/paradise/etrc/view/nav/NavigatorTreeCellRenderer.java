package org.paradise.etrc.view.nav;
import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.paradise.etrc.data.v1.TrainType;

public class NavigatorTreeCellRenderer extends DefaultTreeCellRenderer {

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		
		// TODO:重要!重要! 导航栏节点自定义绘制
		Component component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		
		if (value instanceof DefaultMutableTreeNode) {
			Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
			
			if (userObject instanceof TrainType)
				component.setForeground(((TrainType) userObject).fontColor);
		}
		

//        setText(value.toString());  
//          
//        if (sel)  
//        {  
//            setForeground(getTextSelectionColor());  
//        }  
//        else  
//        {  
//            setForeground(getTextNonSelectionColor());  
//        }  
//          
//        //得到每个节点的TreeNode  
//        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;  
//          
//        //得到每个节点的text  
//        String str = node.toString();         
//          
//        //判断是哪个文本的节点设置对应的值（这里如果节点传入的是一个实体,则可以根据实体里面的一个类型属性来显示对应的图标）  
//        if (str == "a")  
//        {  
//            this.setIcon(new ImageIcon("treeimg/a.GIF"));  
//        }  
//        if (str == "b")  
//        {  
//            this.setIcon(new ImageIcon("treeimg/b.GIF"));  
//        }  
//        if (str == "c")  
//        {  
//            this.setIcon(new ImageIcon("treeimg/c.GIF"));  
//        }  
  
		
		return component;
	}

}
