package com.baoyun.subsystems.cgf.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.panter.li.bi.asn.AsnException;
import ch.panter.li.bi.asn.AsnTag;
import ch.panter.li.bi.asn.AsnTagClass;
import ch.panter.li.bi.asn.AsnTagImpl;
import ch.panter.li.bi.asn.AsnTagNature;
import ch.panter.li.bi.asn.AsnValue;
import ch.panter.li.bi.asn.AsnValueBase;
import ch.panter.li.bi.asn.AsnValueFactory;
import ch.panter.li.bi.asn.AsnValueFactoryBase;
import ch.panter.li.bi.asn.ber.AsnBerDecoder;
import ch.panter.li.bi.asn.ber.AsnBerDecoderImpl;
import ch.panter.li.bi.asn.ber.AsnBerDecodingContext;
import ch.panter.li.bi.asn.ber.AsnBerEncoder;
import ch.panter.li.bi.asn.ber.AsnBerEncoderImpl;
import ch.panter.li.bi.asn.ber.AsnBerInputStream;
import ch.panter.li.bi.asn.ber.AsnBerStructureBuilder;
import ch.panter.li.bi.asn.model.AsnType;
import ch.panter.li.bi.asn.model.AsnTypes;
import ch.panter.li.bi.asn.value.AsnChoice;
import ch.panter.li.bi.asn.value.AsnContainerValueBase;
import ch.panter.li.bi.asn.value.AsnSimpleValueBase;
import ch.panter.li.bi.asn.value.AsnUnknownExtension;
import ch.panter.li.bi.util.ArgumentTool;
import ch.panter.li.bi.util.ReadOnlyCollection;

/**
 * <p>
 * ASN.1 BER encoding/decoding utilities.
 * </p>
 *
 * <p>
 * 基于 asn1forj: http://asn1forj.sourceforge.net/
 * </p>
 *
 * TODO: 对按tag number获取容器内元素的所有方法, 添加可适应任何TagClass(APPLICATION, PRIVATE, ...)的特性.
 *
 * @author george
 *
 */
public class BerCodingUtils {

	/**
	 * <p>
	 * 以BER方式, 编码一个AsnValue对象.
	 * </p>
	 *
	 * @param asn1Obj
	 * @return
	 * @throws AsnException
	 */
	public static byte[] encode(AsnValue asn1Obj) throws AsnException {

		ArgumentTool.nonNullArgument(asn1Obj);

		AsnBerEncoder encoder = new AsnBerEncoderImpl();

		return encoder.encode(asn1Obj);
	}

	/**
	 * <p>
	 * 以BER方式, 从指定byte[]中, 解码出其中的第一个AsnValue对象.
	 * </p>
	 *
	 * @param asn1Bin
	 * @return
	 * @throws IOException
	 * @throws AsnException
	 */
	public static AsnValue decode(byte[] asn1Bin) throws IOException, AsnException {

		AsnValue[] allObjs = decodeAll(asn1Bin);

		ArgumentTool.nonNullArgument(allObjs);
		ArgumentTool.nonZeroArgument(allObjs.length);

		return allObjs[0];
	}

	/**
	 * <p>
	 * 以BER方式, 将指定byte[]中所包含的所有AsnValue对象都解码出来.
	 * </p>
	 *
	 * @param asn1Bin
	 * @return
	 * @throws IOException
	 * @throws AsnException
	 */
	public static AsnValue[] decodeAll(byte[] asn1Bin) throws IOException, AsnException {

		ArgumentTool.nonNullArgument(asn1Bin);

		InputStream in = new ByteArrayInputStream(asn1Bin);

		// 1. 创建decoder:
		AsnBerDecoder decoder = new AsnBerDecoderImpl();

		// 2. 从一个InputStream创建decoding context:
		AsnBerDecodingContext context = decoder.createContext(in);
		// 3. 为decoding context注册1个structure builder:
		context.addDecodingListener(new AsnBerStructureBuilder());

		// 4. 采用"一步到位"的方式, 完成decode(也可以分步进行decode):
		while (decoder.hasMoreDataAvailable(context)) {
			decoder.readNextPDU(context);
		}

		// 5. 取出第3步注册的那个structure builder, 从中取出装有已decode的所有AsnValue
		// (依据InputStream的具体内容, 可能包含多个AsnValue):
		ReadOnlyCollection<AsnValue> decoded = ((AsnBerStructureBuilder) context
				.getDecodingListeners().get(0)).getDecodedPDUs();

		return decoded.itemsAsArray(new AsnValue[decoded.size()]);
	}

	/**
	 * <p>
	 * 根据ASN.1标准Tag信息, 创建相应的Tag.
	 * </p>
	 *
	 * <p>
	 * ASN.1标准Tag规范参见: {@link AsnTagClass}, {@link AsnTagNature}.
	 * </p>
	 *
	 * @param tagClass
	 * @param tagNature
	 * @param tagNum
	 * @return
	 */
	public static AsnTag createAsn1Tag(AsnTagClass tagClass, AsnTagNature tagNature, int tagNum) {

		return new AsnTagImpl(tagClass, tagNature, tagNum);
	}

	/**
	 * <p>
	 * 根据ASN.1标准数据类型, 创建一个对应的(空的, 即未初始化的)AsnValue对象.
	 * </p>
	 *
	 * @param stdType
	 *            ASN.1 标准数据类型, 参见: {@link AsnTypes}.
	 * @return
	 * @throws AsnException
	 */
	public static <T extends AsnValueBase> T createAsn1Value(AsnType stdType) throws AsnException {

		ArgumentTool.nonNullArgument(stdType);

		AsnValueBase newValue;
		if (!stdType.equals(AsnTypes.CHOICE)) {

			newValue = valueFactory.newAsnValue(stdType.getTag());
		} else {
			newValue = new AsnChoice();
		}

		@SuppressWarnings("unchecked")
		T obj = (T) newValue;
		return obj;
	}

	/**
	 * <code>
	 * return {@link #decodeAsAsn1Atom(byte[], int, int, AsnType) decodeAsAsn1Atom}(dataContent, 0, dataContent.length, stdType);
	 * </code>
	 *
	 * @param dataContent
	 * @param stdType
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	public static <T extends AsnSimpleValueBase> T decodeAsAsn1Atom(byte[] dataContent,
			AsnType stdType) throws AsnException, IOException {

		ArgumentTool.nonNullArgument(dataContent);

		@SuppressWarnings("unchecked")
		T decoded = (T) decodeAsAsn1Atom(dataContent, 0, dataContent.length, stdType);

		return decoded;
	}

	/**
	 * <p>
	 * 从使用BER编码的, 包含ASN.1标准基本类型数据的byte[]中, 解码出指定类型
	 * (由asn1forj(http://asn1forj.sourceforge.net/)定义)的ASN.1对象.
	 * </p>
	 *
	 * <p>
	 * TODO: 对参数: dataBuf, offset以及length的要求: 从offset开始, 长度为length(byte)的字节串,
	 * 刚刚好为一个ASN.1标准基本类型的数据内容(不包含tag和length)??
	 * </p>
	 *
	 * @param dataBuf
	 *            BER编码的ASN.1数据.
	 * @param offset
	 *            the offset we take when dealing with dataBuf.
	 * @param length
	 *            数据内容实际所占的长度, 单位: byte.
	 * @param stdType
	 *            ASN.1标准数据类型; 参见 {@link AsnTypes}.
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	public static <T extends AsnSimpleValueBase> AsnSimpleValueBase decodeAsAsn1Atom(
			byte[] dataBuf, int offset, int length, AsnType stdType)
			throws AsnException, IOException {

		ArgumentTool.nonNullArgument(dataBuf);
		ArgumentTool.nonNullArgument(stdType);

		AsnBerInputStream in = new AsnBerInputStream(new ByteArrayInputStream(dataBuf, offset,
				length));

		if (!stdType.isSimple()) {
			throw new IllegalArgumentException("type: " + stdType
					+ " is NOT ASN.1 standard primitive type, please check the argument!");
		}

		AsnSimpleValueBase value = createAsn1Value(stdType);

		in.readValue(value, length);

		return value;
	}

	/**
	 * <p>
	 * 利用{@link #decodeAsAsn1Atom(byte[], AsnType)},
	 * 将一个具有合法ASN.1标准基本类型编码的AsnUnknownExtension对象的类型进行具体化.
	 * </p>
	 *
	 * @param rawElement
	 * @param requiredType
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	public static <T extends AsnSimpleValueBase> T interpretAsAsn1Atom(AsnValue rawElement,
			AsnType requiredType) throws AsnException, IOException {

		ArgumentTool.nonNullArgument(rawElement);

		/*
		 * 当没有使用IMPLICIT tag覆盖原有的ASN.1标准类型的tag, 或存在正确的ASN.1抽象类型层次信息时,
		 * 可能已经完成了值的解析, 即已经被解码为非AsnByteArrayValueBase对象.
		 */
		AsnType rawElementType = rawElement.getType();
		if (rawElementType != null
				&& (rawElementType.equals(requiredType) || rawElementType
						.isDerivedFrom(requiredType))) {

			@SuppressWarnings("unchecked")
			T obj = (T) rawElement;
			return obj;
		}

		// 认为使用了IMPLICIT tag, 覆盖了原有的ASN.1标准类型的tag, 此时无法进一步解码, 只能为AsnUnknownExtension.
		return decodeAsAsn1Atom(((AsnUnknownExtension) rawElement).getUnknownContentEncoding(),
				requiredType);
	}

	/**
	 * <p>
	 * 判断是否有指定tag number的元素(不管具体的TagClass, TagNature是什么):
	 * </p>
	 *
	 * <p>
	 * <code>
	 * <pre> if (getElementByTagNum(container, tagNum) != null) {
	 *     return true;
	 * } else {
	 *     return false;
	 * }
	 * </pre>
	 * </code>
	 * </p>
	 *
	 * @param container
	 * @param tagNum
	 * @return
	 */
	public static boolean hasElementWithTagNum(AsnContainerValueBase container, int tagNum) {

		if (getElementByTagNum(container, tagNum) != null) {
			return true;
		} else {
			return false;
		}
	}

	// @formatter:off
	// 对AsnContainerValueBase对象进行访问的4种基本方式:
	/*
	 * 查: get*ElementByTag*(), getElementByPosition();
	 * 改: replaceElementByTag*(), replaceElementByPosition();
	 * 增: addElement(), appendElement();
	 * 删: deleteElementByTag*(), deleteElementByPosition();
	 */
	// @formatter:on

	/**
	 * <p>
	 * 提供完整的Tag信息, 返回匹配的(首个)元素, 当不关注具体元素类型时, 或无法使用get*ElementByTagNum() 时使用.
	 * </p>
	 *
	 * <p>
	 * 直接调用{@link AsnContainerValueBase#getItemByTag(AsnTag)}实现.
	 * </p>
	 *
	 * @param container
	 * @param tag
	 * @return
	 */
	public static AsnValueBase getElementByTag(AsnContainerValueBase container, AsnTag tag) {

		ArgumentTool.nonNullArgument(container);
		ArgumentTool.nonNullArgument(tag);

		return (AsnValueBase) container.getItemByTag(tag);
	}

	/**
	 * <p>
	 * 只提供tag number, 在指定的AsnContainerValueBase中, 对对可能的tag class/nature组合一一进行尝试, 若找到匹配的则返回;
	 * 否则返回null.
	 * </p>
	 *
	 * <p>
	 * 不对找到的元素是primitive, 还是constructed进行限制.
	 * </p>
	 *
	 * @param container
	 * @param tagNum
	 * @return
	 */
	public static AsnValueBase getElementByTagNum(AsnContainerValueBase container, int tagNum) {

		ArgumentTool.nonNullArgument(container);

		AsnTag tag = null;
		AsnValue value = null;

		tag = createAsn1Tag(AsnTagClass.ContextSpecific, AsnTagNature.Primitive, tagNum);
		value = container.getItemByTag(tag);
		if (value != null) {
			return (AsnValueBase) value;
		}

		tag = createAsn1Tag(AsnTagClass.ContextSpecific, AsnTagNature.Constructed, tagNum);
		value = container.getItemByTag(tag);
		if (value != null) {
			return (AsnValueBase) value;
		}

		tag = createAsn1Tag(AsnTagClass.Universal, AsnTagNature.Primitive, tagNum);
		value = container.getItemByTag(tag);
		if (value != null) {
			return (AsnValueBase) value;
		}

		tag = createAsn1Tag(AsnTagClass.Universal, AsnTagNature.Constructed, tagNum);
		value = container.getItemByTag(tag);
		if (value != null) {
			return (AsnValueBase) value;
		}

		tag = createAsn1Tag(AsnTagClass.Private, AsnTagNature.Primitive, tagNum);
		value = container.getItemByTag(tag);
		if (value != null) {
			return (AsnValueBase) value;
		}

		tag = createAsn1Tag(AsnTagClass.Private, AsnTagNature.Constructed, tagNum);
		value = container.getItemByTag(tag);
		if (value != null) {
			return (AsnValueBase) value;
		}

		tag = createAsn1Tag(AsnTagClass.Application, AsnTagNature.Primitive, tagNum);
		value = container.getItemByTag(tag);
		if (value != null) {
			return (AsnValueBase) value;
		}

		tag = createAsn1Tag(AsnTagClass.Application, AsnTagNature.Constructed, tagNum);
		value = container.getItemByTag(tag);
		if (value != null) {
			return (AsnValueBase) value;
		}

		return null;
	}

	/**
	 * <p>
	 * 从一个ASN.1复合类型(SET, 所有元素都带tag的SEQUENCE, 以及带有tag的CHOICE)对象(AsnContainerValueBase)中, 根据tag
	 * number, 解码出指定基本类型的ASN.1基本类型对象(AsnSimpleValueBase).
	 * </p>
	 *
	 * <p>
	 * 注意: 要求tag类型为context-specific(默认以及大多数情况下), 且指定的ASN.1标准基本类型: stdType必须准确.
	 * </p>
	 *
	 * @param container
	 * @param tagNum
	 * @param stdType
	 * @return
	 * @throws Exception
	 */
	public static <T extends AsnSimpleValueBase> T getAtomElementByTagNum(
			AsnContainerValueBase container, int tagNum, AsnType stdType) throws Exception {

		ArgumentTool.nonNullArgument(container);
		ArgumentTool.nonNullArgument(stdType);

		// @formatter:off
		AsnValue rawElement/* = container.getItemByTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Primitive, tagNum))*/;

		// 只要tag number匹配即可, 不管是否context-specific, 是否为primitive:
		rawElement = getElementByTagNum(container, tagNum);

		// TODO: 未找到匹配Tag的元素, 此处是否应该抛出RuntimeException?
		if (rawElement == null) {
			/*throw new IllegalArgumentException(
					"tag(context-specific): ["
							+ tagNum
							+ "], with ASN.1 standard type: "
							+ stdType
							+ " NOT found in AsnContainerValueBase: "
							+ container
							+ ", please check the tag number/class, "
							+ "or consider judging which choice item is selected first(for CHOICE elements).");*/
			return null;
		}
		// @formatter:on

		return interpretAsAsn1Atom(rawElement, stdType);
	}

	/**
	 * <p>
	 * 与{@link #getAtomElementByTagNum(AsnContainerValueBase, int, AsnType)}不同在于:<br />
	 * 需要获取的是一个ASN.1复合数据类型对象(AsnContainerValueBase), 对更具体的复合数据类型({SET, SET OF, SEQUENCE, SEQUENCE
	 * OF, ...})不做要求.
	 * </p>
	 *
	 * @param container
	 * @param tagNum
	 * @return
	 */
	public static <T extends AsnContainerValueBase> T getComplexElementByTagNum(
			AsnContainerValueBase container, int tagNum) {

		ArgumentTool.nonNullArgument(container);

		// @formatter:off
		AsnValue rawElement/* = container.getItemByTag(BerCodingUtils.createAsn1Tag(
				AsnTagClass.ContextSpecific, AsnTagNature.Constructed, tagNum))*/;

		// 只要tag number匹配即可, 不管是否context-specific, 是否为constructed:
		rawElement = getElementByTagNum(container, tagNum);

		// TODO: 未找到匹配Tag的元素, 此处是否应该抛出RuntimeException?
		if (rawElement == null) {
			/*throw new IllegalArgumentException(
					"tag(context-specific): ["
							+ tagNum
							+ "], with one of ASN.1 Construted(non-atom) types({SET, SET OF, SEQUENCE, SEQUENCE OF, ...})"
							+ " is NOT found in AsnContainerValueBase: " + container
							+ ", please check the tag number or the tag class.");*/
			return null;
		}
		// @formatter:on

		if (!(rawElement instanceof AsnContainerValueBase)) {
			throw new IllegalArgumentException("element with tag: ["
					+ rawElement.getTag().getTagClass() + " " + tagNum
					+ "] in AsnContainerValueBase: " + container
					+ " is NOT an ASN.1 Construct type!");
		}

		@SuppressWarnings("unchecked")
		T obj = (T) rawElement;
		return obj;
	}

	/**
	 * <p>
	 * 对于ASN.1中的有序集合: SEQUENCE OF和SEQUENCE(非所有元素都满足distinct tags)的SEQUENCE);<br />
	 * 以及SET OF, 等无法使用tag唯一地确定其中元素的集合, 可能需要使用元素的位置进行访问.
	 * </p>
	 *
	 * <p>
	 * 对于所有元素都有distinct tags的集合, 不建议使用此方法访问, 而应该始终使用tag访问.
	 * </p>
	 *
	 * @param container
	 * @param pos
	 * @return
	 */
	public static AsnValueBase getElementByPosition(AsnContainerValueBase container, int pos) {

		ArgumentTool.nonNullArgument(container);

		return (AsnValueBase) container.getItems().get(pos);
	}

	/**
	 * <p>
	 * 按tag number, 获取指定AsnContainerValueBase中, 外面带有wrapper TLV的CHOICE元素: 去掉外层的T,
	 * 获取里面的V(即仅包含CHOICE的实际选项)并返回.
	 * </p>
	 *
	 * @param container
	 * @param tagNum
	 * @return
	 * @throws AsnException
	 */
	public static AsnValueBase getTaggedChoiceElement(AsnContainerValueBase container, int tagNum)
			throws AsnException {

		ArgumentTool.nonNullArgument(container);

		AsnContainerValueBase choice = getComplexElementByTagNum(container, tagNum);

		// TODO: 当指定tag number的CHOICE元素不存在时, 是直接返回null, 还是抛出异常?
		if (choice == null) {
			// @formatter:off
			/*throw new AsnException("explicit tagged CHOICE (tag number: " + tagNum
					+ ") NOT found in ASN.1 constructed: " + container);*/
			// @formatter:on
			return null;
		}
		if (choice.size() != 1) {
			throw new AsnException("explicit tagged CHOICE (tag number: " + tagNum + ", in: "
					+ container + ") has NO selected items, or has MORE THAN 1 selected items!");
		}

		return (AsnValueBase) choice.getItems().get(0);
	}

	/**
	 * <p>
	 * 提供CHOICE类型对象: tag number -&gt; 实际类型 的映射, 根据实际的选项, 对类型进行进一步处理:
	 * <ul>
	 * <li>对于基本类型: 利用{@link #interpretAsAsn1Atom(AsnValue, AsnType)}方法, 进一步解析;</li>
	 * <li>对于复合类型(包括CHOICE): 不做处理, 直接返回.</li>
	 * </ul>
	 * </p>
	 *
	 * @param <T>
	 * @param aChoice
	 * @param typeMapping
	 * @return
	 * @throws AsnException
	 * @throws IOException
	 */
	public static <T extends AsnValue> T interpretChoice(AsnValue aChoice,
			Map<Integer, AsnType> typeMapping) throws AsnException, IOException {

		int selectedTagNum = -1;

		selectedTagNum = judgeActualUntaggedChoiceItemTagNum(aChoice);
		if (selectedTagNum == -1) {
			throw new AsnException("problem occurs judging selected CHOICE item for object: "
					+ aChoice + " : can NOT get selected tag number!");
		}

		AsnType targetType = typeMapping.get(selectedTagNum);
		if (targetType == null) {
			throw new AsnException("selected CHOICE with tag number: " + selectedTagNum
					+ " does NOT exist in the type mapping: " + typeMapping
					+ ", please check the mapping infos!");
		}

		if (targetType.isComplex()) {
			// 对ASN.1 constructed类型的对象不进行进一步解析
			// (原因: AsnUnknownStructure与其他constructed类型的APIs差别不大)
			@SuppressWarnings("unchecked")
			T obj = (T) aChoice;
			return obj;
		} else {
			// targetType.isSimple() == true
			AsnSimpleValueBase interpreted = interpretAsAsn1Atom(aChoice, targetType);

			@SuppressWarnings("unchecked")
			T obj = (T) interpreted;
			return obj;
		}
	}

	/**
	 * <p>
	 * 使用新的AsnValue, 替换指定AsnContainerValueBase中的旧AsnValue, 要求新/旧AsnValue的Tag完全相同.
	 * </p>
	 *
	 * @param container
	 * @param newElement
	 * @param matchingTag
	 *
	 * @return
	 */
	public static AsnValue replaceElementByTag(AsnContainerValueBase container,
			AsnValue newElement, AsnTag matchingTag) {

		AsnValue oldValue = container.getItemByTag(matchingTag);
		if (oldValue == null) {
			throw new IllegalArgumentException("element with tag: ["
					+ matchingTag.getTagNumber().getValueAsInt() + "]"
					+ "is NOT found in AsnContainerValueBase: " + container
					+ ", please check the tag number/class, or the container ASN.1 object.");
		}

		AsnTag tagOfNewElement = newElement.getTag();
		if (!tagOfNewElement.isSame(matchingTag)) {
			throw new IllegalArgumentException("new element's tag: " + tagOfNewElement
					+ ", which is NOT equal to old element's tag: " + matchingTag
					+ ", please check the tag number/class, the container ASN.1 object, or use");
		}

		int positionOfMatchingElement = 0;
		ReadOnlyCollection<AsnValue> elementsOfContainer = container.getItems();
		for (int i = 0; i < container.size(); i++) {
			if (elementsOfContainer.get(i).getTag().isSame(matchingTag)) {
				positionOfMatchingElement = i;
			}
		}

		container.getWritableItems().set(positionOfMatchingElement, newElement);

		return oldValue;
	}

	/**
	 * <p>
	 * 使用指定的新元素, 将集合中匹配tag number, 且类型为context-specific的tag的元素替换掉, 并返回旧元素.
	 * </p>
	 *
	 * @param container
	 * @param newElement
	 * @param tagNum
	 *
	 * @return
	 */
	public static AsnValue replaceElementByTagNum(AsnContainerValueBase container,
			AsnValue newElement, int tagNum) {

		AsnTag tag = BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
				AsnTagNature.Constructed, tagNum);

		if (container.getItemByTag(tag) == null) {
			tag = BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific, AsnTagNature.Primitive,
					tagNum);
		}

		return replaceElementByTag(container, newElement, tag);
	}

	/**
	 * <p>
	 * 按元素位置, 将指定的新元素替换集合中的旧元素, 并返回旧元素.
	 * </p>
	 *
	 * <p>
	 * 适用于不具有distinct tags, 只能用元素位置进行访问的集合类型, 如: SEQUENCE OF, 符合条件的SEQUENCE, SET OF.
	 * </p>
	 *
	 * @param container
	 * @param newElement
	 * @param pos
	 *
	 * @return
	 */
	public static AsnValue replaceElementByPosition(AsnContainerValueBase container,
			AsnValue newElement, int pos) {

		ArgumentTool.nonNullArgument(newElement);
		ArgumentTool.nonNullArgument(container);

		AsnValue oldElement = container.getItems().get(pos);
		container.getWritableItems().set(pos, newElement);

		return oldElement;
	}

	/**
	 * <p>
	 * 添加一个AsnValue到指定的AsnContainerValueBase中去, 要求不能与集合中已有的AsnValue的Tag有冲突.
	 * </p>
	 *
	 * <p>
	 * 仅适用于其所有元素都具有distinct tags的集合类型, 如SET, 或者符合条件的SEQUENCE;<br />
	 * <b>注意</b>: 一般不适用于SEQUENCE, 以及SET OF等使用元素位置进行访问的集合.
	 * </p>
	 *
	 * @param container
	 * @param element
	 *
	 * @return
	 */
	public static AsnValue addElement(AsnContainerValueBase container, AsnValue element) {

		if (container.getItemByTag(element.getTag()) != null) {
			throw new IllegalArgumentException(
					"new added element's tag: "
							+ element.getTag()
							+ " already exist in the container: "
							+ container
							+ ", please consider using BerCodingUtils.replaceElementByTag(AsnValue, AsnContainerValueBase, AsnTag)");
		}

		container.getWritableItems().add(element);

		return container;
	}

	/**
	 * <p>
	 * 将一个元素添加到指定的ASN.1复合类型对象中去: 追加到末尾. 返回这个新添加了元素的ASN.1复合类型对象.
	 * </p>
	 *
	 * <p>
	 * 适用于没有distinct tags的, 只能使用元素位置进行访问的ASN.1集合类型, 如: SEQUENCE OF, SET OF, 符合条件的SEQUENCE等;<br />
	 * <b>注意</b>: 此方法不会检查要添加的元素的结构, 因而可能违反SET OF或SEQUENCE OF的语义.
	 * </p>
	 *
	 * @param container
	 * @param element
	 *
	 * @return
	 */
	public static AsnContainerValueBase appendElement(AsnContainerValueBase container,
			AsnValue element) {

		ArgumentTool.nonNullArgument(element);
		ArgumentTool.nonNullArgument(container);

		container.getWritableItems().add(element);

		return container;
	}

	/**
	 * <p>
	 * 删除AsnContainerValueBase中匹配指定Tag的AsnValue. 若有实际的删除动作(即存在匹配指定Tag的AsnValue), 返回true; 否则返回false.
	 * </p>
	 *
	 * @param container
	 * @param tagOfDeletingElement
	 *
	 * @return
	 */
	public static boolean deleteElementByTag(AsnContainerValueBase container,
			AsnTag tagOfDeletingElement) {

		AsnValue toDelete = container.getItemByTag(tagOfDeletingElement);
		if (toDelete == null) {
			return false;
		}

		container.remove(toDelete);
		return true;
	}

	/**
	 * <p>
	 * 删除AsnContainerValueBase中匹配指定tag number, tag类型为context-specific的AsnValue.
	 * 若有实际的删除动作(即存在匹配指定Tag的AsnValue), 返回true; 否则返回false.
	 * </p>
	 *
	 * @param container
	 * @param tagNumOfDeletingElement
	 *
	 * @return
	 */
	public static boolean deleteElementByTagNum(AsnContainerValueBase container,
			int tagNumOfDeletingElement) {

		AsnTag tag = BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
				AsnTagNature.Constructed, tagNumOfDeletingElement);

		if (container.getItemByTag(tag) == null) {
			tag = BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific, AsnTagNature.Primitive,
					tagNumOfDeletingElement);
		}

		return deleteElementByTag(container, tag);
	}

	/**
	 * <p>
	 * 将指定集合中的, 指定位置的元素删除; 若有实际的删除动作, 返回true, 否则返回false.
	 * </p>
	 *
	 * <p>
	 * 适用于没有distinct tags的, 只能使用元素位置进行访问的ASN.1集合类型, 如: SEQUENCE OF, SET OF, 符合条件的SEQUENCE等;
	 * </p>
	 *
	 * @param container
	 * @param pos
	 *
	 * @return
	 */
	public static boolean deleteElementByPosition(AsnContainerValueBase container, int pos) {

		ArgumentTool.nonNullArgument(container);

		if (container.getItems().get(pos) == null) {
			return false;
		}
		if (pos < 0 || pos >= container.size()) {
			return false;
		}

		container.remove(pos);
		return true;
	}

	/**
	 * <p>
	 * 注意: 仅适用于无tag的CHOICE.
	 * </p>
	 *
	 * <p>
	 * 对于在ASN.1规约文件中被定义为CHOICE的数据, 检查实际选中的是哪一项, 并返回其tag number.
	 * </p>
	 *
	 * @param aChoice
	 * @return
	 * @throws AsnException
	 */
	public static int judgeActualUntaggedChoiceItemTagNum(AsnValue aChoice) throws AsnException {

		ArgumentTool.nonNullArgument(aChoice);

		return aChoice.getTag().getTagNumber().getValueAsInt();
	}

	/**
	 * <p>
	 * 注意: 仅适用于外层有tag的CHOICE.
	 * </p>
	 *
	 * <p>
	 * 对于在ASN.1规约文件中被定义为CHOICE的数据, 检查实际选中的是哪一项, 并返回其tag number.
	 * </p>
	 *
	 * @param taggedChoice
	 * @return
	 * @throws AsnException
	 */
	public static int judgeActualTaggedChoiceItemTagNum(AsnValue taggedChoice) throws AsnException {

		ArgumentTool.nonNullArgument(taggedChoice);

		if (!(taggedChoice instanceof AsnContainerValueBase)) {
			throw new AsnException("CHOICE: " + taggedChoice
					+ " is NOT explicit tagged. pleaase check the tagging style!");
		}

		AsnContainerValueBase explicitTaggedChoice = (AsnContainerValueBase) taggedChoice;
		if (explicitTaggedChoice.size() != 1) {
			throw new AsnException("explicit tagged CHOICE: " + taggedChoice
					+ " has NO selected items, or has MORE THAN 1 selected items!");
		}

		AsnTag selectedTag = explicitTaggedChoice.getItems().get(0).getTag();
		return selectedTag.getTagNumber().getValueAsInt();
	}

	/**
	 * <p>
	 * 获取一个ASN.1复合类型对象所包含的所有数据成员的tag.
	 * </p>
	 *
	 * @param container
	 * @return
	 */
	public static List<AsnTag> listTags(AsnContainerValueBase container) {

		ArgumentTool.nonNullArgument(container);

		List<AsnTag> allTags = new ArrayList<AsnTag>(container.size());

		for (AsnValue each : container.getItems()) {
			allTags.add(each.getTag());
		}

		return allTags;
	}

	/**
	 * <p>
	 * print一个byte(按unsigned byte), 可指定进制(十六进制字母为大写).
	 * </p>
	 *
	 * @param aByte
	 * @param radix
	 * @return
	 */
	public static String getReadableByte(byte aByte, int radix) {

		int extInt = 0x000000FF & aByte;

		return Integer.toString(extInt, radix).toUpperCase();
	}

	/**
	 * <p>
	 * 以十六进制, print一个byte[] (所有byte均按unsigned byte处理), 可指定分隔符.
	 * </p>
	 *
	 * @param bytes
	 * @param radix
	 * @param delim
	 * @return
	 */
	public static String getReadableByteArray(byte[] bytes, int radix, String delim) {

		if (delim == null) {
			delim = " ";
		}
		if (!(radix == 2 || radix == 8 || radix == 10 || radix == 16)) {
			radix = 16;
		}

		String radixName;
		switch (radix) {
			case 2:
				radixName = "BIN";
				break;

			case 8:
				radixName = "OCT";
				break;

			case 10:
				radixName = "DEC";
				break;

			default:
				radixName = "HEX";
				break;
		}

		StringBuilder sb = new StringBuilder();
		for (byte each : bytes) {
			sb.append(getReadableByte(each, radix)).append(delim);
		}
		sb.delete(sb.length() - delim.length(), sb.length());

		return "byte[]( length: " + bytes.length + ", content(" + radixName + "): "
				+ sb + " )";
	}

	private static AsnValueFactory valueFactory = new AsnValueFactoryBase();

	private BerCodingUtils() {

	}
}
