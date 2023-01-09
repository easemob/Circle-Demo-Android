package io.agora.home.utils;


import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.agora.home.R;
import io.agora.home.bean.Node;

public class TreeHelper {
    /**
     * 传入我们的普通bean，转化为我们排序后的Node
     *
     * @param datas
     * @param defaultExpandLevel
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static <T> List<Node> getSortedNodes(List<T> datas,
                                                int defaultExpandLevel) throws IllegalArgumentException,
            IllegalAccessException {
        List<Node> result = new ArrayList<Node>();
        //将用户数据转化为List<Node>以及设置Node间关系
        List<Node> nodes = convetData2Node(datas);
        //拿到根节点
        List<Node> rootNodes = getRootNodes(nodes);
        //排序
        for (Node node : rootNodes) {
            addNode(result, node, defaultExpandLevel, 1);
        }
        return result;
    }

    /**
     * 过滤出所有可见的Node
     *
     * @param nodes
     * @return
     */
    public static List<Node> filterVisibleNode(List<Node> nodes) {
        List<Node> result = new ArrayList<Node>();

        for (Node node : nodes) {
            // 如果为跟节点，或者上层目录为展开状态
            if (node.isRoot() || node.isParentExpand()) {
                setNodeIcon(node);
                result.add(node);
            }
        }
        return result;
    }

    /**
     * 将我们的数据转化为树的节点
     *
     * @param datas
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private static <T> List<Node> convetData2Node(List<T> datas) throws IllegalArgumentException, IllegalAccessException {
        List<Node> nodes = new ArrayList<Node>();
        Node node = null;
        if (datas == null) {
            return null;
        }
        for (T obj : datas) {
            String id = "-1";
            String pId = "-1";
            String name = null;
            String ext = null;
            boolean isDefault = false;
            int channelMode=0;
            int seatCount=8;
            int icon = 0;
            if (obj == null) {
                continue;
            }
            Class<? extends Object> clazz = obj.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.getAnnotation(TreeNodeId.class) != null) {
                    field.setAccessible(true);
                    id = (String) field.get(obj);
                }
                if (field.getAnnotation(TreeNodePid.class) != null) {
                    field.setAccessible(true);
                    pId = (String) field.get(obj);
                }
                if (field.getAnnotation(TreeNodeName.class) != null) {
                    field.setAccessible(true);
                    name = (String) field.get(obj);
                }
                if (field.getAnnotation(TreeNodeExt.class) != null) {
                    field.setAccessible(true);
                    ext = (String) field.get(obj);
                }
                if (field.getAnnotation(TreeNodeDefault.class) != null) {
                    field.setAccessible(true);
                    isDefault = (boolean) field.get(obj);
                }
                if (field.getAnnotation(TreeNodeChannelMode.class) != null) {
                    field.setAccessible(true);
                    channelMode = (int) field.get(obj);
                }
                if (field.getAnnotation(TreeNodeSeatCount.class) != null) {
                    field.setAccessible(true);
                    seatCount = (int) field.get(obj);
                }
                if (field.getAnnotation(TreeNodeIcon.class) != null) {
                    field.setAccessible(true);
                    icon = (int) field.get(obj);
                }
                if ((!"-1".equals(id)) && (!"-1".equals(pId)) && name == null) {
                    break;
                }
            }
            node = new Node(id, pId, name);
            node.setDefault(isDefault);
            node.setIcon(icon);
            node.setChannelMode(channelMode);
            node.setSeatCount(seatCount);
            node.setExt(ext);
            nodes.add(node);
        }

        /**
         * 设置Node间，父子关系;让每两个节点都比较一次，即可设置其中的关系
         */
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            for (int j = i + 1; j < nodes.size(); j++) {
                Node m = nodes.get(j);
                if (TextUtils.equals(m.getPId(), n.getId())) {
                    n.getChildren().add(m);
                    m.setParent(n);
                } else if (TextUtils.equals(m.getId(), n.getPId())) {
                    m.getChildren().add(n);
                    n.setParent(m);
                }
            }
        }

        // 设置图片
        for (Node n : nodes) {
            setNodeIcon(n);
        }
        return nodes;
    }

    private static List<Node> getRootNodes(List<Node> nodes) {
        List<Node> root = new ArrayList<Node>();
        for (Node node : nodes) {
            if (node.isRoot())
                root.add(node);
        }
        return root;
    }

    /**
     * 把一个节点上的所有的内容都挂上去
     */
    private static void addNode(List<Node> nodes, Node node,
                                int defaultExpandLeval, int currentLevel) {

        nodes.add(node);
        if (defaultExpandLeval >= currentLevel) {
            node.setExpand(true);
        }

        if (node.isLeaf())
            return;
        for (int i = 0; i < node.getChildren().size(); i++) {
            addNode(nodes, node.getChildren().get(i), defaultExpandLeval,
                    currentLevel + 1);
        }
    }

    /**
     * 设置节点的图标
     *
     * @param node
     */
    private static void setNodeIcon(Node node) {
        if(node.getLevel()!=1) {
            if (node.getChildren().size() > 0 && node.isExpand() && node.getPId() != null) {
                node.setIcon(R.drawable.tree_ex);
            } else if (node.getChildren().size() > 0 && !node.isExpand() && node.getPId() != null) {
                node.setIcon(R.drawable.tree_ec);
            }
        }
    }
}
