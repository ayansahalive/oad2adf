package conv;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PageDefXml {
    public PageDefXml() {
        super();
    }

    /**
     *
     * @param pgName
     * @param voName
     * @param attrName
     * @param dest
     * @param app
     * @param amDef
     * @param bindingType
     * @param pickListAttr
     * @param pickListVO
     * @param voPath
     * @param packagePath
     * @throws Exception
     */
    public static void handlePageDef(String pgName, String voName, Object attrName, String dest, String app,
                                     String amDef, String bindingType, String pickListAttr, String pickListVO,
                                     String voPath, String packagePath) throws Exception {
        System.out.println("Start Conv: handlePageDef " + pgName + " " + voName + " " + attrName + " " + dest + " " +
                           app + " " + amDef + " " + bindingType + " " + pickListAttr + " " + pickListVO + " " +
                           voPath + " " + packagePath);
        ErrorAndLog.handleLog(app,
                              "Start Conv: handlePageDef " + pgName + " " + voName + " " + attrName + " " + dest + " " +
                              app + " " + amDef + " " + bindingType + " " + pickListAttr + " " + pickListVO + " " +
                              voPath + " " + packagePath);
        DocumentBuilderFactory newDbFactory = DocumentBuilderFactory.newInstance();
        newDbFactory.setValidating(false);
        DocumentBuilder newDBuilder = newDbFactory.newDocumentBuilder();
        String pageDefDocFolder = FileReaderWritter.getViewPageDefDestinationPath(packagePath, app, dest);
        File file = new File(pageDefDocFolder + FileReaderWritter.getSeparator() + pgName + "PageDef.xml");
        Document pageDefDoc = null;
        Element rootElement = null;
        if (!file.exists()) {
            file.createNewFile();
            pageDefDoc = newDBuilder.newDocument();
            pageDefDoc = createPageDef(pageDefDoc, app);
            DataBindingsXml.handleDataBindingsPage(dest, app, pgName, file.getAbsolutePath(),
                                                   amDef.substring(amDef.lastIndexOf(".") + 1), packagePath);
        } else {
            pageDefDoc = newDBuilder.parse(file);
            rootElement = pageDefDoc.getDocumentElement();
        }
        rootElement = pageDefDoc.getDocumentElement();
        NodeList nodes = rootElement.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node != null && node.getNodeName() != null) {
                if (node.getNodeName().equals("executables") && (voName != null || pickListVO != null)) {
                    NodeList nodeList = node.getChildNodes();
                    List<Node> tempNodeList = getNode(nodeList, "iterator");
                    boolean voExists = false;
                    String bindValue = null;
                    for (Node tempNode : tempNodeList) {
                        NamedNodeMap tempNameNodeMap = tempNode.getAttributes();
                        if (tempNameNodeMap != null) {
                            for (int j = 0; j < tempNameNodeMap.getLength(); j++) {
                                Node itemNode = tempNameNodeMap.item(j);
                                if (itemNode != null && itemNode.getNodeName().equals("Binds")) {
                                    bindValue = itemNode.getNodeValue();
                                    if (bindValue.equals(voName) || bindValue.equals(pickListVO)) {
                                        voExists = true;
                                    }
                                }
                            }
                        }
                    }
                    if (!voExists) {
                        voExists = false;
                        Element iteratorDtls = pageDefDoc.createElement("iterator");
                        node.appendChild(iteratorDtls);

                        Attr binds = pageDefDoc.createAttribute("Binds");
                        if (voName != null) {
                            binds.setValue(voName);
                        } else {
                            binds.setValue(pickListVO);
                        }
                        binds.normalize();
                        iteratorDtls.setAttributeNode(binds);

                        Attr rangeSize = pageDefDoc.createAttribute("RangeSize");
                        rangeSize.setValue("25");
                        rangeSize.normalize();
                        iteratorDtls.setAttributeNode(rangeSize);

                        Attr dataControl = pageDefDoc.createAttribute("DataControl");
                        dataControl.setValue(amDef.substring(amDef.lastIndexOf(".") + 1) + "DataControl");
                        dataControl.normalize();
                        iteratorDtls.setAttributeNode(dataControl);

                        Attr id = pageDefDoc.createAttribute("id");
                        if (voName != null) {
                            id.setValue(voName + "Iterator");
                        } else {
                            id.setValue(pickListVO + "Iterator");
                        }
                        id.normalize();
                        iteratorDtls.setAttributeNode(id);
                    }
                } else if (node.getNodeName().equals("bindings")) {
                    if (bindingType != null && bindingType.equals("attributeValues")) {
                        Element attributeValues = pageDefDoc.createElement("attributeValues");
                        node.appendChild(attributeValues);

                        Attr iterBinding = pageDefDoc.createAttribute("IterBinding");
                        iterBinding.setValue(voName + "Iterator");
                        iterBinding.normalize();
                        attributeValues.setAttributeNode(iterBinding);

                        Attr id = pageDefDoc.createAttribute("id");
                        id.setValue((String) attrName);
                        id.normalize();
                        attributeValues.setAttributeNode(id);

                        Element attrNames = pageDefDoc.createElement("AttrNames");
                        attributeValues.appendChild(attrNames);

                        Element item = pageDefDoc.createElement("Item");
                        attrNames.appendChild(item);

                        Attr value = pageDefDoc.createAttribute("Value");
                        value.setValue((String) attrName);
                        value.normalize();
                        item.setAttributeNode(value);
                    } else if (bindingType != null && bindingType.equals("invokeMethod")) {
                        Element methodAction = pageDefDoc.createElement("methodAction");
                        node.appendChild(methodAction);

                        Attr id = pageDefDoc.createAttribute("id");
                        id.setValue((String) attrName);
                        id.normalize();
                        methodAction.setAttributeNode(id);

                        Attr requiresUpdateModel = pageDefDoc.createAttribute("RequiresUpdateModel");
                        requiresUpdateModel.setValue("true");
                        requiresUpdateModel.normalize();
                        methodAction.setAttributeNode(requiresUpdateModel);

                        Attr action = pageDefDoc.createAttribute("Action");
                        action.setValue("invokeMethod");
                        action.normalize();
                        methodAction.setAttributeNode(action);

                        Attr methodName = pageDefDoc.createAttribute("MethodName");
                        methodName.setValue((String) attrName);
                        methodName.normalize();
                        methodAction.setAttributeNode(methodName);

                        Attr isViewObjectMethod = pageDefDoc.createAttribute("IsViewObjectMethod");
                        isViewObjectMethod.setValue("false");
                        isViewObjectMethod.normalize();
                        methodAction.setAttributeNode(isViewObjectMethod);

                        Attr dataControl = pageDefDoc.createAttribute("DataControl");
                        dataControl.setValue(amDef.substring(amDef.lastIndexOf(".") + 1) + "DataControl");
                        dataControl.normalize();
                        methodAction.setAttributeNode(dataControl);

                        Attr instanceName = pageDefDoc.createAttribute("InstanceName");
                        instanceName.setValue("data." + amDef.substring(amDef.lastIndexOf(".") + 1) +
                                              "DataControl.dataProvider");
                        instanceName.normalize();
                        methodAction.setAttributeNode(instanceName);

                        Attr returnName = pageDefDoc.createAttribute("ReturnName");
                        returnName.setValue("data." + amDef.substring(amDef.lastIndexOf(".") + 1) +
                                            "DataControl.methodResults." + attrName + "_" +
                                            amDef.substring(amDef.lastIndexOf(".") + 1) + "DataControl_dataProvider_" +
                                            attrName + "_result");
                        returnName.normalize();
                        methodAction.setAttributeNode(returnName);
                    } else if (bindingType != null && bindingType.equals("listOfValues")) {
                        Element list = pageDefDoc.createElement("listOfValues");
                        node.appendChild(list);

                        Attr id = pageDefDoc.createAttribute("id");
                        id.setValue((String) attrName);
                        id.normalize();
                        list.setAttributeNode(id);

                        Attr iterBinding = pageDefDoc.createAttribute("IterBinding");
                        iterBinding.setValue(voName + "Iterator");
                        iterBinding.normalize();
                        list.setAttributeNode(iterBinding);

                        Attr staticList = pageDefDoc.createAttribute("StaticList");
                        staticList.setValue("false");
                        staticList.normalize();
                        list.setAttributeNode(staticList);

                        Attr uses = pageDefDoc.createAttribute("Uses");
                        uses.setValue("LOV_" + attrName);
                        uses.normalize();
                        list.setAttributeNode(uses);
                    } else if (bindingType != null && bindingType.equals("list")) {
                        Element list = pageDefDoc.createElement("list");
                        node.appendChild(list);

                        Attr id = pageDefDoc.createAttribute("id");
                        if (attrName != null) {
                            id.setValue((String) attrName);
                        } else {
                            id.setValue(pickListVO);
                        }
                        id.normalize();
                        list.setAttributeNode(id);


                        if (attrName == null && voName == null) {
                            Attr listOperMode = pageDefDoc.createAttribute("ListOperMode");
                            listOperMode.setValue("navigation");
                            listOperMode.normalize();
                            list.setAttributeNode(listOperMode);

                            Attr listIter = pageDefDoc.createAttribute("ListIter");
                            listIter.setValue(pickListVO + "Iterator");
                            listIter.normalize();
                            list.setAttributeNode(listIter);
                        } else {
                            Attr staticList = pageDefDoc.createAttribute("StaticList");
                            staticList.setValue("false");
                            staticList.normalize();
                            list.setAttributeNode(staticList);

                            Attr uses = pageDefDoc.createAttribute("Uses");
                            uses.setValue("LOV_" + attrName);
                            uses.normalize();
                            list.setAttributeNode(uses);

                        }

                        Attr iterBinding = pageDefDoc.createAttribute("IterBinding");
                        if (voName != null) {
                            iterBinding.setValue(voName + "Iterator");
                        } else {
                            iterBinding.setValue(pickListVO + "Iterator");
                        }
                        iterBinding.normalize();
                        list.setAttributeNode(iterBinding);

                        Attr dtSupportsMRU = pageDefDoc.createAttribute("DTSupportsMRU");
                        dtSupportsMRU.setValue("true");
                        dtSupportsMRU.normalize();
                        list.setAttributeNode(dtSupportsMRU);

                        Attr selectItemValueMode = pageDefDoc.createAttribute("SelectItemValueMode");
                        selectItemValueMode.setValue("ListObject");
                        selectItemValueMode.normalize();
                        list.setAttributeNode(selectItemValueMode);

                        if (voName == null) {
                            Element attrNames = pageDefDoc.createElement("AttrNames");
                            list.appendChild(attrNames);

                            Element item = pageDefDoc.createElement("Item");
                            attrNames.appendChild(item);

                            Attr value = pageDefDoc.createAttribute("Value");
                            value.setValue(pickListAttr);
                            value.normalize();
                            item.setAttributeNode(value);
                        }
                    } else if (bindingType != null && bindingType.equals("tree")) {
                        if (voPath != null) {
                            String removalPath =
                                dest + FileReaderWritter.getSeparator() + app + FileReaderWritter.getSeparator() +
                                "Model" + FileReaderWritter.getSeparator() + "src" + FileReaderWritter.getSeparator();
                            voPath = voPath.substring(removalPath.length());
                            voPath = voPath.substring(0, voPath.indexOf("."));
                            voPath = voPath.replace(FileReaderWritter.getSeparator(), ".");
                        }
                        Element tree = pageDefDoc.createElement("tree");
                        node.appendChild(tree);

                        Attr iterBinding = pageDefDoc.createAttribute("IterBinding");
                        iterBinding.setValue(voName + "Iterator");
                        iterBinding.normalize();
                        tree.setAttributeNode(iterBinding);

                        Attr id = pageDefDoc.createAttribute("id");
                        id.setValue(voName);
                        id.normalize();
                        tree.setAttributeNode(id);

                        Element nodeDefinition = pageDefDoc.createElement("nodeDefinition");
                        tree.appendChild(nodeDefinition);

                        Attr defName = pageDefDoc.createAttribute("DefName");
                        defName.setValue(voPath);
                        defName.normalize();
                        nodeDefinition.setAttributeNode(defName);

                        Attr name = pageDefDoc.createAttribute("Name");
                        name.setValue(voName);
                        name.normalize();
                        nodeDefinition.setAttributeNode(name);

                        List<String> attrList = (List<String>) attrName;
                        if (attrList.size() > 0) {
                            Element attrNames = pageDefDoc.createElement("AttrNames");
                            nodeDefinition.appendChild(attrNames);

                            for (String attrStr : attrList) {
                                Element item = pageDefDoc.createElement("Item");
                                attrNames.appendChild(item);

                                Attr attrValue = pageDefDoc.createAttribute("Value");
                                attrValue.setValue(attrStr);
                                attrValue.normalize();
                                item.setAttributeNode(attrValue);
                            }
                        }
                    }
                }
            }
        }
        FileReaderWritter.writeXMLFile(pageDefDoc, file.getAbsolutePath(), app);
        System.out.println("End Conv: handlePageDef ");
    }

    /**
     *
     * @param pageDefDoc
     * @param app
     * @return
     */
    public static Document createPageDef(Document pageDefDoc, String app) {
        System.out.println("Start Conv: createPageDef " + app);
        ErrorAndLog.handleLog(app, "Start Conv: createPageDef " + app);
        Element pageDefinition = pageDefDoc.createElement("pageDefinition");
        pageDefDoc.appendChild(pageDefinition);

        Attr xmlns = pageDefDoc.createAttribute("xmlns");
        xmlns.setValue("http://xmlns.oracle.com/adfm/uimodel");
        xmlns.normalize();
        pageDefinition.setAttributeNode(xmlns);

        Attr version = pageDefDoc.createAttribute("version");
        version.setValue("12.1.3.10.8");
        version.normalize();
        pageDefinition.setAttributeNode(version);

        Attr id = pageDefDoc.createAttribute("id");
        id.setValue("helloPGPageDef");
        id.normalize();
        pageDefinition.setAttributeNode(id);

        Attr packageAttr = pageDefDoc.createAttribute("Package");
        packageAttr.setValue("view.pageDefs");
        packageAttr.normalize();
        pageDefinition.setAttributeNode(packageAttr);

        Element parameters = pageDefDoc.createElement("parameters");
        pageDefinition.appendChild(parameters);

        Element executables = pageDefDoc.createElement("executables");
        pageDefinition.appendChild(executables);

        Element variableIterator = pageDefDoc.createElement("variableIterator");
        executables.appendChild(variableIterator);

        Attr variableIteratorId = pageDefDoc.createAttribute("id");
        variableIteratorId.setValue("variables");
        variableIterator.setAttributeNode(variableIteratorId);

        Element bindings = pageDefDoc.createElement("bindings");
        pageDefinition.appendChild(bindings);

        System.out.println("End Conv: createPageDef ");
        return pageDefDoc;
    }

    /**
     *
     * @param nodeList
     * @param name
     * @return
     */
    private static List<Node> getNode(NodeList nodeList, String name) {
        List<Node> retNodeList = new ArrayList<Node>();
        Node node = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // do something with the current element
                if (node.getNodeName().equals(name)) {
                    retNodeList.add(node);
                }
            }
        }
        return retNodeList;
    }
}
