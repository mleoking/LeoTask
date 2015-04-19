package org.leores.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.leores.ecpt.TRuntimeException;
import org.leores.util.able.Processable1;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SerialUtil extends ObjUtil {

	public static String[] toStrArray(Class tClass, Integer mod) {
		String[] rtn = null;

		if (tClass != null) {
			Field[] fields = getFields(tClass, mod);
			rtn = new String[fields.length];
			for (int i = 0; i < fields.length; i++) {
				rtn[i] = fields[i].getName();
			}
		}

		return rtn;
	}

	public static String[] toStrArray(Class tClass) {
		return toStrArray(tClass, null);
	}

	public static String[][] toStrArray(Object tObj, String[] sFields, String sPatNumOut, Integer mod, Processable1<String, Object> pa1) {
		String[][] rtn = null;

		if (tObj != null) {
			if (tObj instanceof String) {
				rtn = new String[2][1];
				rtn[0][0] = "String";
				rtn[1][0] = tObj + "";
			} else {
				if (sFields == null) {
					Field[] fields = getFields(tObj.getClass(), mod);
					rtn = new String[2][fields.length];
					for (int i = 0; i < fields.length; i++) {
						rtn[0][i] = fields[i].getName();
						Object oValue = getFieldValue(tObj, fields[i]);
						if (pa1 != null) {
							rtn[1][i] = pa1.process(oValue);
						} else {
							rtn[1][i] = valToStr(oValue, sPatNumOut);
						}
					}
				} else {
					rtn = new String[2][sFields.length];
					for (int i = 0; i < sFields.length; i++) {
						rtn[0][i] = sFields[i];
						Object oValue = getFieldValue(tObj, sFields[i], mod);
						if (pa1 != null) {
							rtn[1][i] = pa1.process(oValue);
						} else {
							rtn[1][i] = valToStr(oValue, sPatNumOut);
						}
					}
				}
			}
		}

		return rtn;
	}

	public static String[][] toStrArray(Object tObj, String[] sFields, String sPatNumOut, Processable1<String, Object> pa1) {
		return toStrArray(tObj, sFields, sPatNumOut, null, pa1);
	}

	public static String[][] toStrArray(Object tObj, String sPatNumOut, Processable1<String, Object> pa1) {
		return toStrArray(tObj, null, sPatNumOut, null, pa1);
	}

	//	public static String[][] toStrArray(Object tObj, String sPatNumOut) {
	//		return toStrArray(tObj, null, sPatNumOut, null, null);
	//	}

	public static String[][] toStrArray(Object tObj) {
		return toStrArray(tObj, null, null, null, null);
	}

	public static String[] toStrArray(List list, String sPatNumOut) {
		String[] rtn = null;

		if (list != null) {
			int size = list.size();
			rtn = new String[size];
			for (int i = 0; i < size; i++) {
				Object eObj = list.get(i);
				rtn[i] = valToStr(eObj, sPatNumOut);
			}
		}

		return rtn;
	}

	public static String[] toStrArray(List list) {
		return toStrArray(list, null);
	}

	public static String toStr(Object tObj, String[] sFields, String sPatNumOut, Integer mod, Processable1<String, Object> pa1) {
		String rtn = null;

		rtn = tObj + " ";
		if (tObj != null && tObj.getClass() != String.class && !(tObj instanceof Number)) {
			String[][] strArray = toStrArray(tObj, sFields, sPatNumOut, mod, pa1);
			if (strArray != null) {
				for (int i = 0; i < strArray[0].length; i++) {
					rtn += strArray[0][i] + "=" + strArray[1][i] + sEnumerate;
				}
			}
		}

		return rtn;
	}

	public static String toStr(Object tObj, String sPatNumOut, Integer mod) {
		return toStr(tObj, null, sPatNumOut, mod, null);
	}

	public static String toStr(Object tObj, Integer mod) {
		return toStr(tObj, null, null, mod, null);
	}

	public static String toStr(Object tObj) {
		return toStr(tObj, null, null, null, null);
	}

	public static String toStr(Object... objs) {
		String rtn = null;

		if (objs != null) {
			rtn = "[";
			for (int i = 0; i < objs.length; i++) {
				rtn += valToStr(objs[i]);
				if (i < objs.length - 1) {
					rtn += de;
				}
			}
			rtn += "]";

		} else {
			rtn = objs + "";
		}

		return rtn;
	}

	protected static String getSValue(Element e) {
		String rtn = null;
		if (e.hasChildNodes()) {
			rtn = e.getTextContent();
		} else {
			rtn = e.getAttribute("val");
		}
		return rtn;
	}

	protected static Variables createVariables(Element e) {
		Variables rtn = null;

		if (e != null) {
			try {
				String sVClass = e.getAttribute("class");
				Variables vars = (Variables) newInstance(Variables.class, sVClass);
				if (vars != null) {
					boolean bAllAdded = true;
					NodeList nList = e.getChildNodes();
					int length = nList.getLength();
					for (int i = 0; i < length; i++) {
						Node node = nList.item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element e1 = (Element) node;
							String name = e1.getNodeName();
							String sValues = getSValue(e1);
							boolean bAdded = vars.addVariable(name, sValues);
							if (!bAdded) {
								tLog(U.ll("LOG_ERROR"), "loadFromXML: Failed to add variable! [class, variable, values]:" + U.toStr(sVClass, name, sValues));
								bAllAdded = false;
								break;
							}
						}
					}
					if (bAllAdded) {
						rtn = vars;
					}
				}
			} catch (Exception ex) {
				rtn = null;
				tLog(ex);
			}
		}

		return rtn;
	}

	protected static String getSValueIfReachEnd(Element e) {
		String rtn = null;
		if (e.hasChildNodes()) {
			int neChildNodes = e.getChildNodes().getLength();
			Node nodeFirstChild = e.getFirstChild();
			short nodeFCType = nodeFirstChild.getNodeType();
			if (neChildNodes == 1 && nodeFCType != Node.ELEMENT_NODE) {//Node.TEXT_NODE || Node.CDATA_SECTION_NODE
				rtn = nodeFirstChild.getNodeValue();
			}
		} else {
			rtn = e.getAttribute("val");
		}
		return rtn;
	}

	@SuppressWarnings("all")
	public static List createList(Class eClass, Element e) {
		List rtn = null;

		if (eClass != null && e != null) {
			try {
				String sValues = getSValueIfReachEnd(e);
				if (sValues != null) {
					rtn = parseList(eClass, U.eval(sValues, null, U.EVAL_NullIgnore | U.EVAL_InvalidIgnore));//The eval here only processes special values such as $null$.
				} else {
					rtn = new ArrayList();
					NodeList nList = e.getChildNodes();
					for (int i = 0, size = nList.getLength(); i < size; i++) {
						Node node = nList.item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element eRoot = (Element) node;
							Object iObj = newInstance(eClass);
							loadFromXML(iObj, eRoot);
							rtn.add(iObj);
						}
					}
				}
			} catch (Exception ex) {
				rtn = null;
				tLog(ex);
			}
		}

		return rtn;
	}

	public static boolean loadFromSerial(Object tObj, String sFile) {
		boolean rtn = false;

		if (tObj != null && sFile != null) {
			try {
				FileInputStream fis = new FileInputStream(sFile);
				ObjectInputStream ois = new ObjectInputStream(fis);
				Object oIn = ois.readObject();
				if (oIn != null) {
					invokeObjMethodByName(tObj, "beforeLoadObj", false, "ObjUtil.loadFromSerial");
					copy(tObj, oIn);
					invokeObjMethodByName(tObj, "afterLoadObj", false, "ObjUtil.loadFromSerial");
				}
				ois.close();
				rtn = true;
			} catch (Exception e) {
				rtn = false;
				tLog(e);
			}
		}

		return rtn;
	}

	/**
	 * Note: String sValue = element.getAttribute("val"); when <xxx val=""/>,
	 * sValue == "" rather than null.
	 * 
	 * @param tObj
	 * @param eRoot
	 * @return
	 */

	public static boolean loadFromXML(Object tObj, Element eRoot) {
		boolean rtn = false;

		if (tObj != null && eRoot != null) {
			Class objClass = tObj.getClass();
			try {
				rtn = true;
				invokeObjMethodByName(tObj, "beforeLoadObj", false, "ObjUtil.loadFromXML");
				if (eRoot.getNodeType() == Node.ELEMENT_NODE) {
					NodeList nList = eRoot.getChildNodes();
					int nChildNodes = nList.getLength();
					for (int i = 0; i < nChildNodes; i++) {
						Node node = nList.item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element element = (Element) node;
							String eName = element.getNodeName();
							Method mSetter = getSetterMethod(objClass, eName, String.class);
							if (mSetter != null) {
								String sValue = getSValue(element);
								invokeMethod(tObj, mSetter, sValue);
							} else {
								Field field = getField(objClass, eName, modAll);
								if (field == null) {
									rtn = false;
									tLog(LOG_ERROR, "loadFromXML: Field: " + eName + " is not found in " + objClass + "!");
									//break;
								} else {
									Class eClass = null;
									if (element.hasAttribute("class")) {
										String sClass = element.getAttribute("class");
										eClass = Class.forName(sClass);
									}
									Object oFieldValue = null;
									Class fieldType = field.getType();
									if (bAssignable(Variables.class, fieldType)) {
										oFieldValue = createVariables(element);
									} else if (bAssignable(List.class, fieldType)) {
										if (eClass == null) {
											eClass = getComponentType(field);
											if (eClass == null) {
												tLog(LOG_ERROR, "loadFromXML: Please specify the class of the " + eName + " element either in the xml file or in the java file!");
											}
										}
										oFieldValue = createList(eClass, element);
									} else if (bAssignable(Object[].class, fieldType)) {
										if (eClass == null) {
											eClass = fieldType.getComponentType();
											if (eClass.isPrimitive()) {
												tLog(LOG_ERROR, "loadFromXML: Primitive component type is not supported! [sField, fieldType, componentType]" + toStr(eName, fieldType, eClass));
											}
										}
										List list = createList(eClass, element);
										oFieldValue = U.toArray(list, eClass);
									} else {
										if (eClass == null) {
											eClass = fieldType;
										} else if (!bAssignable(fieldType, eClass)) {
											tLog(LOG_ERROR, "loadFromXML: " + eClass + " is not assignable to " + eName + ":" + fieldType);
										}
										String sValue = getSValueIfReachEnd(element);
										if (sValue == null) {//not reach end yet
											oFieldValue = newInstance(eClass);
											loadFromXML(oFieldValue, element);
										} else {//reach end of a tree branch
											if (!eClass.equals(fieldType)) {
												oFieldValue = newInstance(eClass, sValue);
											} else {
												oFieldValue = sValue;
											}
										}
									}
									if (oFieldValue == null) {
										tLog(LOG_WARNING, "loadFromXML: the value of " + eName + " is null!");
									}
									//Use string field name eName instead of field obj can allow the eval of oFieldvalue if it is a string
									if (!setFieldValue(tObj, eName, oFieldValue, modAll, true, true)) {
										rtn = false;
										tLog(LOG_ERROR, "loadFromXL: can not set " + eName + " with " + oFieldValue);
									}
								}
							}
						}
					}
				}
				invokeObjMethodByName(tObj, "afterLoadObj", false, "ObjUtil.loadFromXML");
			} catch (Exception e) {
				rtn = false;
				tLog(e);
			}
		}

		return rtn;
	}

	/**
	 * Load fields' value of tObj from a xml file fn. This function does not
	 * support Primitive Data Types. They has to be replaced by their
	 * corresponding Object classes. e.g. double->Double
	 * 
	 * @param tObj
	 * @param sFile
	 * @param bUnMatchException
	 *            true: throw a TRuntimeException when the root element does not
	 *            match the Object class.
	 * @return
	 */
	public static boolean loadFromXML(Object tObj, String sFile, boolean bUnMatchException) {
		boolean rtn = false;

		if (tObj != null && U.valid(sFile)) {
			if (U.bExistFile(sFile)) {
				try {
					File file = new File(sFile);
					DocumentBuilderFactory dBFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder;
					dBuilder = dBFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(file);
					doc.getDocumentElement().normalize();

					Class objClass = tObj.getClass();
					Element eRoot = doc.getDocumentElement();
					String objClassName = objClass.getName();
					String docRootName = eRoot.getNodeName();

					if (objClassName.indexOf(docRootName) < 0 && bUnMatchException) {
						throw new TRuntimeException("loadFromXML: root element does not match the Object class! [sFile,objClassName,docRootName]:", sFile, objClassName, docRootName);
					}

					rtn = loadFromXML(tObj, eRoot);
				} catch (ParserConfigurationException e) {
					tLog(e);
				} catch (SAXException e) {
					tLog(e);
				} catch (IOException e) {
					tLog(e);
				}
			} else {
				throw new TRuntimeException("loadFromXML: file does not exist! [sFile]:", sFile);
			}
		}

		return rtn;
	}

	/**
	 * Load fields' value of tObj from a xml file fn. This function does not
	 * support Primitive Data Types. They has to be replaced by their
	 * corresponding Object classes. e.g. double->Double
	 * 
	 * @param tObj
	 * @param sFile
	 * @return
	 */
	public static boolean loadFromXML(Object tObj, String sFile) {
		return loadFromXML(tObj, sFile, true);
	}

	public static boolean bLoadString(String str) {
		boolean rtn = false;
		if (str != null && (str.contains(sSet) || canEval(str))) {
			rtn = true;
		}
		return rtn;
	}

	/**
	 * Load fields' value of tObj from str. This function does not support
	 * Primitive Data Types. They has to be replaced by their corresponding
	 * Object classes. e.g. double->Double
	 * 
	 * @param tObj
	 * @param str
	 *            format: var1=value1;var2=value2;...
	 * @return
	 */

	public static boolean loadFromString(Object tObj, String str, boolean bExeception) {
		boolean rtn = false;

		if (tObj != null && U.bLoadString(str)) {
			invokeObjMethodByName(tObj, "beforeLoadObj", false, "ObjUtil.loadFromString");
			while (!str.contains(sSet) && canEval(str)) {
				str = eval(str, tObj, bExeception);
			}
			rtn = true;
			String[] tokens = split(str);

			for (int i = 0; i < tokens.length; i++) {
				String token = trim(tokens[i], true, false);
				String[] setting = token.split(sSet, 2);
				if (setting.length == 2) {
					if (tObj instanceof Map) {
						Map mObj = (Map) tObj;
						mObj.put(setting[0], setting[1]);
					} else {
						rtn = setFieldValue(tObj, setting[0], setting[1], modAll, true, bExeception);
					}
				} else if (setting.length == 1 && canEval(token)) {
					eval(token, tObj, bExeception);
				} else {
					rtn = false;
					if (bExeception) {
						throw new TRuntimeException("Wrong format: " + tokens[i] + " should be: var=value" + sEnumerate + " or $method(parameters)$" + sEnumerate);
					}
				}
			}
			invokeObjMethodByName(tObj, "afterLoadObj", false, "ObjUtil.loadFromString");
		}

		return rtn;
	}

	public static boolean loadFromString(Object tObj, String str) {
		return loadFromString(tObj, str, true);
	}

	public static String toOutString(Class tClass, String before, String after, String delimiter) {
		String rtn = null;

		if (tClass != null && delimiter != null && before != null && after != null) {
			String[] strArray = toStrArray(tClass);
			if (strArray != null) {
				StringBuffer sBuffer = new StringBuffer();
				String classInfo = U.wrap(before, after, delimiter, "Class", tClass.getName());
				sBuffer.append(classInfo + "\n");
				String str = StrUtil.wrap(strArray, before, after, delimiter);
				sBuffer.append(str + "\n");
				rtn = sBuffer.toString();
			}
		}

		return rtn;
	}

	public static boolean saveToFile(Class tClass, String fn, String before, String after, String delimiter) {
		boolean rtn = false;

		if (fn != null) {
			String outString = toOutString(tClass, before, after, delimiter);
			if (outString != null) {
				rtn = appendFile(fn, outString);
			}
		}

		return rtn;
	}

	public static String toOutString(Object tObj, String before, String after, String delimiter, String sPatNumOut, boolean bOutClass, Processable1<String, Object> pa1) {
		String rtn = null;
		if (tObj != null && delimiter != null && before != null && after != null) {
			String[][] strArray = toStrArray(tObj, sPatNumOut, pa1);
			if (strArray != null) {
				StringBuffer sBuffer = new StringBuffer();
				String str;

				if (bOutClass) {
					String className = tObj.getClass().getName();
					String classInfo = U.wrap(before, after, delimiter, "Class", className);
					sBuffer.append(classInfo + "\n");
					str = StrUtil.wrap(strArray[0], before, after, delimiter);
					sBuffer.append(str + "\n");
				}

				str = StrUtil.wrap(strArray[1], before, after, delimiter);
				sBuffer.append(str + "\n");
				rtn = sBuffer.toString();
			}
		}
		return rtn;
	}

	public static String toOutStringCSV(Object tObj, String sPatNumOut, boolean bOutClass, Processable1<String, Object> pa1) {
		return toOutString(tObj, "\"", "\"", ",", sPatNumOut, bOutClass, pa1);
	}

	public static boolean saveToFile(Object tObj, String sFile, String before, String after, String delimiter, String sPatNumOut, boolean bOutClass, Processable1<String, Object> pa1) {
		boolean rtn = false;

		if (sFile != null) {
			invokeObjMethodByName(tObj, "beforeSaveObj", false, "ObjUtil.saveToFile");
			String outString = toOutString(tObj, before, after, delimiter, sPatNumOut, bOutClass, pa1);
			if (outString != null) {
				rtn = appendFile(sFile, outString);
			}
			invokeObjMethodByName(tObj, "afterSaveObj", false, "ObjUtil.saveToFile");
		}

		return rtn;
	}

	public static boolean saveToCsvFile(Object tObj, String sFile, String sPatNumOut, boolean bOutClass, Processable1<String, Object> pa1) {
		return saveToFile(tObj, sFile, "\"", "\"", ",", sPatNumOut, bOutClass, pa1);
	}

	public static boolean saveToCsvFile(Object tObj, String sFile, String sPatNumOut) {
		return saveToFile(tObj, sFile, "\"", "\"", ",", sPatNumOut, false, null);
	}

	public static boolean saveToCsvFile(Object tObj, String sFile) {
		return saveToFile(tObj, sFile, "\"", "\"", ",", null, false, null);
	}

	public static boolean saveToTxtFile(Object tObj, String sFile, String sPatNumOut, boolean bOutClass, Processable1<String, Object> pa1) {
		return saveToFile(tObj, sFile, "", "", " ", sPatNumOut, bOutClass, pa1);
	}

	public static boolean saveToTxtFile(Object tObj, String sFile, String sPatNumOut) {
		return saveToFile(tObj, sFile, "", "", " ", sPatNumOut, false, null);
	}

	public static boolean saveToTxtFile(Object tObj, String sFile) {
		return saveToFile(tObj, sFile, "", "", " ", null, false, null);
	}

	public static boolean saveToCsvFile(Class tClass, String fn) {
		return saveToFile(tClass, fn, "\"", "\"", ",");
	}

	public static boolean saveToTxtFile(Class tClass, String fn) {
		return saveToFile(tClass, fn, "", "", " ");
	}

	public static boolean saveToSerial(Object tObj, String fn) {
		boolean rtn = false;

		if (tObj != null && fn != null) {
			try {
				FileOutputStream fos = new FileOutputStream(fn);
				ObjectOutputStream ous = new ObjectOutputStream(fos);
				invokeObjMethodByName(tObj, "beforeSaveObj", false, "ObjUtil.saveToSerial");
				ous.writeObject(tObj);
				invokeObjMethodByName(tObj, "afterSaveObj", false, "ObjUtil.saveToSerial");
				ous.close();
				rtn = true;
			} catch (Exception e) {
				rtn = false;
				tLog(e);
			}
		}

		return rtn;
	}
}
