package com.baoyun.subsystems.cgf.handler.processor.cdr;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.exception.MemcachedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.panter.li.bi.asn.AsnTagClass;
import ch.panter.li.bi.asn.AsnTagNature;
import ch.panter.li.bi.asn.AsnValue;
import ch.panter.li.bi.asn.model.AsnTypes;
import ch.panter.li.bi.asn.value.AsnContainerValueBase;
import ch.panter.li.bi.asn.value.AsnEnumerated;
import ch.panter.li.bi.asn.value.AsnInteger;

import com.baoyun.subsystems.cgf.asn1.ConsolidationResult;
import com.baoyun.subsystems.cgf.asn1.TimeStamp;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.CdrProcessingEvent;
import com.baoyun.subsystems.cgf.handler.processor.cdr.event.ParticularCdrReceived;
import com.baoyun.subsystems.cgf.utils.BerCodingUtils;
import com.baoyun.subsystems.cgf.utils.MergingUtils;
import com.baoyun.subsystems.cgf.utils.MiscUtils;

public class PgwCdrMerger extends CdrMerger {

	@Deprecated
	class CategoryContainer {

		// TODO: 使用int是否够用?
		private int cdrReceived;

		private Date setupTime;

		private Map<MergingDistinctKey, PendingList> allMergingList = new HashMap<PgwCdrMerger.MergingDistinctKey, PendingList>();

		private CategoryKey key;

		private String keyStringRepr;

		public CategoryContainer() {
 
		}

		void onPendingListMergeComplete(CdrProcessingEvent event) {

		}

		void onAllPendingListMergeComplete(CdrProcessingEvent event) throws Exception {

			log.trace(
					"all PendingLists of Category: {} have finished merging, checking and setting field: consolidationResult of merged CDRs",
					getKey());

			AsnValue inputCdr = (AsnValue) event.getArg();

			// @formatter:off
			/*
			 *	ConsolidationResult::= ENUMERATED {
			 *		normal (0),
			 *		abnormal (1),
			 *		forInterSGSNConsolidation (2),
			 *		reachLimit (3),
			 *		onlyOneCDRGenerated (4)
			 *	}
			 */
			// @formatter:on

			if (MergingUtils.isPartialCdr(inputCdr)) {
				// @formatter:off
				/*
				 * 8.2.5 PGW-CDR的合并
				 * 7）合并流程
				 * 第五步：如果合并完成（即Cause for Record Closing字段=normalRelease或者记录量达到门限）
				 * 检查相同P-GW地址+C-ID 下所有话单的Record Sequence Number列表，检查累加记录数=最大序号是否满足，
				 * 如满足置所有话单的ConsolidationResult =Normal，如不满足置ConsolidationResult =Abnormal；
				 * 如果是因为达到记录量的原因无法再合并，则置ConsolidationResult =ReachLimit。
				 */
				// @formatter:on
				// FIXME: 对下属所有PendingList进行汇总处理, 真实的实现:
				for (PendingList each : getAllMergingList().values()) {

					AsnValue mergingResultStub = each.getMergingResultStub();

					AsnEnumerated consolidationResult = BerCodingUtils
							.createAsn1Value(AsnTypes.ENUMERATED);
					consolidationResult.setValue(0);
					consolidationResult.setTag(BerCodingUtils.createAsn1Tag(
							AsnTagClass.ContextSpecific, AsnTagNature.Primitive, 101));

					BerCodingUtils.replaceElementByTagNum(
							(AsnContainerValueBase) mergingResultStub,
							consolidationResult, 101);
				}
			} else {

				int consolidationResultInitialVal = 0;

				int numOfpendingList = getAllMergingList().size();
				if (numOfpendingList > 1) {
					consolidationResultInitialVal = 1; // abnormal

					log.warn(
							"received COMPLETE P-GW CDR, but there are more than 1 PendingLists: {} in the Category: {}",
							getAllMergingList().entrySet(), getKey().toString());
				}

				for (PendingList each : getAllMergingList().values()) {

					AsnValue mergingResultStub = each.getMergingResultStub();

					AsnEnumerated consolidationResult = BerCodingUtils
							.createAsn1Value(AsnTypes.ENUMERATED);
					consolidationResult.setValue(consolidationResultInitialVal);
					consolidationResult.setTag(BerCodingUtils.createAsn1Tag(
							AsnTagClass.ContextSpecific, AsnTagNature.Primitive, 101));

					BerCodingUtils.replaceElementByTagNum(
							(AsnContainerValueBase) mergingResultStub,
							consolidationResult, 101);
				}
			}

			// 汇总结束
			for (PendingList each : getAllMergingList().values()) {

				each.requestPersistence();
				each.setStatus(MergingStatus.PERSISTING);
				log.trace("PendingList: {} state change to: {}", each.getKeyStringRepr(),
						each.getStatus());
			}

			/*
			 * FIXME: refactor: 暂不从清除Category和PendingList结构, 在持久化完成的回调中进行处理: 置PendingList的状态;
			 * 清除容器结构.
			 */
			log.trace("removing Category: {}", getKeyStringRepr());
			storageBackend.delete(getKeyStringRepr());
		}

		void onTimeout(CdrProcessingEvent event) {

			// FIXME: 超时处理: 对下属PendingList发出定时器到时处理请求.
		}

		public int getCdrReceived() {
			return cdrReceived;
		}

		public void setCdrReceived(int cdrReceived) {
			this.cdrReceived = cdrReceived;
		}

		public Map<MergingDistinctKey, PendingList> getAllMergingList() {
			return allMergingList;
		}

		public void setAllMergingList(Map<MergingDistinctKey, PendingList> allMergingList) {
			this.allMergingList = allMergingList;
		}

		public CategoryKey getKey() {
			return key;
		}

		public void setKey(CategoryKey key) {
			this.key = key;
		}

		public String getKeyStringRepr() {
			return keyStringRepr;
		}

		public void setKeyStringRepr(String keyStringRepr) {
			this.keyStringRepr = keyStringRepr;
		}
	}

	@Deprecated
	class PendingList {

		private AsnValue mergingResultStub;

		private List<AsnValue> mergingList = new ArrayList<AsnValue>();

		private MergingStatus status;

		private MergingDistinctKey key;

		private String keyStringRepr;

		private CategoryContainer category;

		// 常用统计数据:

		// TODO: recordSequenceNumber 使用int是否够用?
		private int minRecordSequenceNumber;

		private int maxRecordSequenceNumber;

		/**
		 * <p>
		 * 若接收到完整的P-GW CDR, 则无须实际的合并操作: needMerge=false.
		 * </p>
		 */
		private boolean needMerge;

		public PendingList() {

		}

		void onMergingTriggerCdrReceived(CdrProcessingEvent event) throws Exception {

			AsnValue inputCdr = (AsnValue) event.getArg();
			log.trace("trigger CDR merging, input P-GW CDR: {}", inputCdr);

			if (MergingUtils.isPartialCdr(inputCdr)) {
				setStatus(MergingStatus.MERGING);
				log.trace("PendingList: {} state change to: {}", getKeyStringRepr(), getStatus());
			}

			doMerge(this, event);
		}

		void onMergeComplete(CdrProcessingEvent event) throws Exception {

			AsnValue lastReceivedCdr = (AsnValue) event.getArg();

			int causeForRecClosing = MergingUtils.getCauseForRecClosingValueOfInputCdr(lastReceivedCdr).intValue();

			// @formatter:off
			/*
			 *	CauseForRecClosing ::= INTEGER {
			 *		-- -- In PGW-CDR and SGW-CDR the value servingNodeChange is used for partial record
			 *		-- generation due to Serving Node Address list Overflow
			 *		-- In SGSN servingNodeChange indicates the SGSN change
			 *		-- -- LCS related causes belong to the MAP error causes acc. TS 29.002 [60]
			 *		-- -- cause codes 0 to 15 are defined 'CauseForTerm' (cause for termination)
			 *		--
			 *		normalRelease (0),
			 *		abnormalRelease (4),
			 *		cAMELInitCallRelease (5),
			 *		volumeLimit (16),
			 *		timeLimit (17),
			 *		servingNodeChange (18),
			 *		maxChangeCond (19),
			 *		managementIntervention (20),
			 *		intraSGSNIntersystemChange (21),
			 *		rATChange (22),
			 *		mSTimeZoneChange (23),
			 *		sGSNPLMNIDChange (24),
			 *		unauthorizedRequestingNetwork (52),
			 *		unauthorizedLCSClient (53),
			 *		positionMethodFailure (54),
			 *		unknownOrUnreachableLCSClient (58),
			 *		listofDownstreamNodeChange (59)
			 *	}
			 */
			// @formatter:on
			switch (causeForRecClosing) {
				case 0:
				case 4:
					/*
					 * 会话结束: 认为已经接收到当前IP-CAN承载数据的所有话单
					 */
					category.onAllPendingListMergeComplete(event);
					break;

				case 18:
				case 19:
					/*
					 * S-GW切换, ratType切换: 当前IP-CAN承载数据的CDR尚未完全接受完毕, 暂不通知Category容器进行汇总,
					 * 继续后续的新S-GW/ratType类的合并动作.
					 */
					break;

				default:
					throw new IllegalArgumentException("unknown causeForRecClosing: "
							+ causeForRecClosing + ", input CDR: " + lastReceivedCdr);
			}
		}

		void onTimeout(CdrProcessingEvent event) {

			// FIXME: 对Category超时监视器发出超时处理请求的处理.
		}

		void requestPersistence() {

			// 提交持久化请求: 合并结果CDR
			Map<String, String> mergingResultConfig = new HashMap<String, String>();
			mergingResultConfig.put("type", "P-GW");
			mergingResultConfig.put("merge", "MERGE");

			log.trace("requesting persistence (MERGED P-GW CDR): {}", getMergingResultStub());
			persistMgr.persist(getMergingResultStub(), mergingResultConfig);

			// 提交持久化请求: 原始话单CDR
			int oriCdrSeq = 1;
			for (AsnValue each : getMergingList()) {
				Map<String, String> oriCdrConfig = new HashMap<String, String>();
				oriCdrConfig.put("type", "P-GW");
				oriCdrConfig.put("merge", "ORIGINAL");
				oriCdrConfig.put("seq", Integer.valueOf(oriCdrSeq++).toString());

				log.trace("requesting persistence (ORIGINAL P-GW CDR): {}", each);
				persistMgr.persist(each, oriCdrConfig);
			}
		}

		private void requestPersistence(AsnValue aCdr) {

		}

		public AsnValue getMergingResultStub() {
			return mergingResultStub;
		}

		public void setMergingResultStub(AsnValue mergingResultStub) {
			this.mergingResultStub = mergingResultStub;
		}

		public MergingStatus getStatus() {
			return status;
		}

		public void setStatus(MergingStatus status) {
			this.status = status;
		}

		public MergingDistinctKey getKey() {
			return key;
		}

		public void setKey(MergingDistinctKey key) {
			this.key = key;
		}

		public String getKeyStringRepr() {
			return keyStringRepr;
		}

		public void setKeyStringRepr(String keyStringRepr) {
			this.keyStringRepr = keyStringRepr;
		}

		public List<AsnValue> getMergingList() {
			return mergingList;
		}

		public void setMergingList(List<AsnValue> mergingList) {
			this.mergingList = mergingList;
		}

		public boolean isNeedMerge() {
			return needMerge;
		}

		public void setNeedMerge(boolean needMerge) {
			this.needMerge = needMerge;
		}

		public CategoryContainer getCategory() {
			return category;
		}

		public void setCategory(CategoryContainer category) {
			this.category = category;
		}

		/**
		 * @return the minRecordSequenceNumber
		 */
		public int getMinRecordSequenceNumber() {
			return minRecordSequenceNumber;
		}

		/**
		 * @param minRecordSequenceNumber
		 *            the minRecordSequenceNumber to set
		 */
		public void setMinRecordSequenceNumber(int minRecordSequenceNumber) {
			this.minRecordSequenceNumber = minRecordSequenceNumber;
		}

		/**
		 * @return the maxRecordSequenceNumber
		 */
		public int getMaxRecordSequenceNumber() {
			return maxRecordSequenceNumber;
		}

		/**
		 * @param maxRecordSequenceNumber
		 *            the maxRecordSequenceNumber to set
		 */
		public void setMaxRecordSequenceNumber(int maxRecordSequenceNumber) {
			this.maxRecordSequenceNumber = maxRecordSequenceNumber;
		}
	}

	@Deprecated
	enum MergingStatus {

		FIRST_P_GW_CDR_RECEIVED,
		ALL_PARTIAL_P_GW_CDRs_RECEIVED,

		MERGING,
		MERGE_DONE,

		ONE_COMPLETE_P_GW_CDR_RECEIVED_NO_NEED_MERGING,
		FILTERED_NO_NEED_MERGING,

		PERSISTING,
		PERSISTENCE_DONE;
	}

	@Deprecated
	static class CategoryKey {

		private static String pattern = "P-GW CDRs(category key): {cgargingID=%s, p-GWAddress=%s}";

		// TODO: chargingId值的获取, Number类型的精度问题, Number没有覆盖hashCode()以及equals()的问题...
		private Number chargingId;

		// 对接收到的实际相同的地址, 而表示形式(ipv4/ipv6)不同时, 如何处理?: 无须处理, 系统整体切换.
		private InetAddress pgwAddress;

		private String stringRepr;

		@Override
		public String toString() {

			if (stringRepr != null) {
				return stringRepr;
			} else {
				return genStringRepresentation();
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((chargingId == null) ? 0 : Long.valueOf(chargingId.longValue()).hashCode());
			result = prime * result + ((pgwAddress == null) ? 0 : pgwAddress.hashCode());
			result = prime * result + ((stringRepr == null) ? 0 : stringRepr.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CategoryKey other = (CategoryKey) obj;
			if (chargingId == null) {
				if (other.chargingId != null) {
					return false;
				}
			} else if (chargingId.longValue() != other.chargingId.longValue()) {
				return false;
			}
			if (pgwAddress == null) {
				if (other.pgwAddress != null) {
					return false;
				}
			} else if (!pgwAddress.equals(other.pgwAddress)) {
				return false;
			}
			if (stringRepr == null) {
				if (other.stringRepr != null) {
					return false;
				}
			} else if (!stringRepr.equals(other.stringRepr)) {
				return false;
			}
			return true;
		}

		private String genStringRepresentation() {

			return String.format(pattern, chargingId, pgwAddress.getHostAddress());
		}

		public Number getChargingId() {
			return chargingId;
		}

		public void setChargingId(Number chargingId) {
			this.chargingId = chargingId;
		}

		public InetAddress getPgwAddress() {
			return pgwAddress;
		}

		public void setPgwAddress(InetAddress pgwAddress) {
			this.pgwAddress = pgwAddress;
		}
	}

	@Deprecated
	static class MergingDistinctKey {

		private static String pattern = "P-GW CDRs(merging distinct key): {chargingID=%s, p-GWAddress=%s, servingNodeAddress=%s, rATType=%s}";

		// TODO: chargingId, ratType值的获取, Number类型的精度问题, Number没有覆盖hashCode()以及equals()的问题...
		private Number chargingId;

		private InetAddress pgwAddress;

		private List<String> servingNodeType;

		private List<InetAddress> servingNodeAddress;

		private Number ratType;

		private String stringRepr;

		public CategoryKey genCategoryKey() {

			CategoryKey categoryKey = new CategoryKey();
			categoryKey.setChargingId(chargingId);
			categoryKey.setPgwAddress(pgwAddress);

			return categoryKey;
		}

		@Override
		public String toString() {

			if (stringRepr != null) {
				return stringRepr;
			} else {
				return genStringRepresentation();
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((chargingId == null) ? 0 : Long.valueOf(chargingId.longValue()).hashCode());
			result = prime * result + ((pgwAddress == null) ? 0 : pgwAddress.hashCode());
			result = prime * result
					+ ((ratType == null) ? 0 : Integer.valueOf(ratType.intValue()).hashCode());
			result = prime * result
					+ ((servingNodeAddress == null) ? 0 : servingNodeAddress.hashCode());
			result = prime * result + ((servingNodeType == null) ? 0 : servingNodeType.hashCode());
			result = prime * result + ((stringRepr == null) ? 0 : stringRepr.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			MergingDistinctKey other = (MergingDistinctKey) obj;
			if (chargingId == null) {
				if (other.chargingId != null) {
					return false;
				}
			} else if (!Long.valueOf(chargingId.longValue()).equals(
					Long.valueOf(other.chargingId.longValue()))) {
				return false;
			}
			if (pgwAddress == null) {
				if (other.pgwAddress != null) {
					return false;
				}
			} else if (!pgwAddress.equals(other.pgwAddress)) {
				return false;
			}
			if (ratType == null) {
				if (other.ratType != null) {
					return false;
				}
			} else if (!Integer.valueOf(ratType.intValue()).equals(
					Integer.valueOf(other.ratType.intValue()))) {
				return false;
			}
			// TODO: 对: servingNodeType和servingNodeAddress实际等价, 但顺序不同时的处理.
			if (servingNodeAddress == null) {
				if (other.servingNodeAddress != null) {
					return false;
				}
			} else if (!servingNodeAddress.equals(other.servingNodeAddress)) {
				return false;
			}
			if (servingNodeType == null) {
				if (other.servingNodeType != null) {
					return false;
				}
			} else if (!servingNodeType.equals(other.servingNodeType)) {
				return false;
			}
			if (stringRepr == null) {
				if (other.stringRepr != null) {
					return false;
				}
			} else if (!stringRepr.equals(other.stringRepr)) {
				return false;
			}
			return true;
		}

		private String genStringRepresentation() {

			// pattern: "[" address-type "=" address ("," address-type "=" address)* "]"
			List<String> addresses = new ArrayList<String>();
			for (int i = 0; i < servingNodeType.size(); ++i) {
				addresses.add(servingNodeType.get(i) + "="
						+ servingNodeAddress.get(i).getHostAddress());
			}
			return String.format(pattern, chargingId, pgwAddress.getHostAddress(), addresses,
					ratType);
		}

		public Number getChargingId() {
			return chargingId;
		}

		public void setChargingId(Number chargingId) {
			this.chargingId = chargingId;
		}

		public InetAddress getPgwAddress() {
			return pgwAddress;
		}

		public void setPgwAddress(InetAddress pgwAddress) {
			this.pgwAddress = pgwAddress;
		}

		public List<String> getServingNodeType() {
			return servingNodeType;
		}

		public void setServingNodeType(List<String> servingNodeType) {
			this.servingNodeType = servingNodeType;
		}

		public List<InetAddress> getServingNodeAddress() {
			return servingNodeAddress;
		}

		public void setServingNodeAddress(List<InetAddress> servingNodeAddress) {
			this.servingNodeAddress = servingNodeAddress;
		}

		public Number getRatType() {
			return ratType;
		}

		public void setRatType(Number ratType) {
			this.ratType = ratType;
		}
	}

	@Deprecated
	static class TempComparator implements Comparable<TempComparator> {

		private AsnValue pgwCdr;

		public TempComparator(AsnValue pgwCdr) {
			this.pgwCdr = pgwCdr;
		}

		@Override
		public String toString() {

			return pgwCdr.toString();
		}

		@Override
		public int compareTo(TempComparator that) {

			AsnInteger thisRecordSequenceNumber;
			Number thisVal;
			try {
				thisRecordSequenceNumber = (AsnInteger) BerCodingUtils.getAtomElementByTagNum(
						(AsnContainerValueBase) pgwCdr, 17, AsnTypes.INTEGER);
				thisVal = thisRecordSequenceNumber.getValue();
			} catch (Exception e) {
				log.error(MiscUtils.exceptionStackTrace2String(e));
				thisVal = 0;
			}

			AsnInteger thatRecordSequenceNumber;
			Number thatVal;
			try {
				thatRecordSequenceNumber = (AsnInteger) BerCodingUtils.getAtomElementByTagNum(
						(AsnContainerValueBase) that.getPgwCdr(), 17, AsnTypes.INTEGER);
				thatVal = thatRecordSequenceNumber.getValue();
			} catch (Exception e) {
				log.error(MiscUtils.exceptionStackTrace2String(e));
				thatVal = 0;
			}

			// TODO: 当超出long可表示范围时的检测/处理
			if (thisVal.longValue() > thatVal.longValue()) {
				return 1;
			} else if (thisVal.longValue() > thatVal.longValue()) {
				return -1;
			} else {
				return 0;
			}
		}

		public AsnValue getPgwCdr() {
			return pgwCdr;
		}

		public void setPgwCdr(AsnValue pgwCdr) {
			this.pgwCdr = pgwCdr;
		}
	}

	private static Logger log = LoggerFactory.getLogger(PgwCdrMerger.class);

	@Deprecated
	protected StorageBackend<CategoryContainer> storageBackend;

	protected StorageBackend<CdrMergerContext> mergerContexts;

	protected CdrPersistenceManager persistMgr;

	@Deprecated
	protected Set<String> savedDistinctKeys = new HashSet<String>();

	protected Map<String, String> filterConfig = new HashMap<String, String>();

	protected boolean incrementalMerging = false;

	public PgwCdrMerger() {

		storageBackend = new JavaUtilMapStorageBackend<CategoryContainer>();

		mergerContexts = new JavaUtilMapStorageBackend<CdrMergerContext>();
		persistMgr = new FileSysCdrPersistenceManager();
	}

	@Override
	public boolean checkDuplicate(AsnValue inputCdr) throws Exception {

		// @formatter:off
		/*
		 * 8.2.5 PGW-CDR的合并
		 * 7）合并流程 第二步：话单合并，CDR合并过程中首先看记录的Record Sequence Number是否在Record Sequence
		 * Number列表中存在，如果存在则说明是重复话单，予以剔除
		 */
		// @formatter:on
		boolean result = false;

		CategoryKey categoryKey = genCategoryKeyFor(inputCdr);
		MergingDistinctKey mergingDistinctKey = genMergingDistinctKeyFor(inputCdr);

		log.trace("checking duplicate for input P-GW CDR: {}, in Category: {}, PendingList: {}",
				new Object[] { inputCdr, categoryKey, mergingDistinctKey });

		// TODO: 若checkDuplicate()只能在刚刚调用完ensurePendingListExistence()时调用,
		// 那么此处是否需要判断非null? 为null的具体场景为哪些?
		CategoryContainer category = storageBackend.get(categoryKey.toString());
		if (category == null) {
			return false;
		}

		PendingList mergingList = category.getAllMergingList().get(mergingDistinctKey);
		if (mergingList == null) {
			return false;
		}

		Number recordSequenceNumber = MergingUtils
				.getRecordSequenceNumberValueOfInputCdr(inputCdr);

		// 完整话单, 无实际的合并操作:
		if (recordSequenceNumber == null) {

			if (!mergingList.isNeedMerge()
					&& (mergingList.getStatus() == MergingStatus.ONE_COMPLETE_P_GW_CDR_RECEIVED_NO_NEED_MERGING
							|| mergingList.getStatus() == MergingStatus.PERSISTING
							|| mergingList.getStatus() == MergingStatus.PERSISTENCE_DONE)) {

				if (mergingList.getMergingList().size() == 1) {
					// 接收到重复的完整话单
					log.trace("a duplicate COMPLETE P-GW CDR received: {}", inputCdr);
					return true;
				} else {

					return false;
				}
			} else {
				// 接收到完整话单, 但是PendingList等对象的状态不正确.
				log.warn(
						"bad status for PendingList: {}, status: {}, isNeedMerge: {}",
						new Object[] { mergingList, mergingList.getStatus(),
								mergingList.isNeedMerge() });
			}

		} else {
			// 接收到的CDR的recordSequenceNumber != null, 为部分话单:
			for (AsnValue each : mergingList.getMergingList()) {
				Number eachRecordSequenceNumber = MergingUtils
						.getRecordSequenceNumberValueOfInputCdr(each);
				if (eachRecordSequenceNumber != null
						&& eachRecordSequenceNumber.equals(recordSequenceNumber)) {
					// 接收到重复的部分话单
					result = true;
					log.trace("a duplicate PARTIAL P-GW CDR received: {}", inputCdr);
				} else if (eachRecordSequenceNumber == null) {
					throw new IllegalArgumentException("partial P-GW CDR: " + each
							+ " does NOT have recordSequenceNumber!");
				}
			}
		}

		return result;
	}

	public boolean filter(AsnValue inputCdr) {

		return false;
	}

	@Override
	public boolean processReceivedCdr(CdrProcessingEvent event) throws TimeoutException,
			InterruptedException, MemcachedException, Exception {

		boolean newCdrAdded = false;

		AsnValue inputCdr = (AsnValue) event.getArg();

		/*
		 * ref: mail: {name: "cg部分配置项", sender: "xbao@baoyunnetworks.com"}
		 *
		 * 如果满足filter条件, 是否为完全忽略接受的话单? 或者只是不进行合并, 仍然记录原始话单?
		 */
		if (filter(inputCdr)) {
			log.debug("filter conditions matched, input P-GW CDR ignored.");
			return false;
		}

		// TODO: 检查重单: checkDuplicate() 是在已接收到至少一个满足同类合并条件CDR的基础上,
		// 此时已存在相应的PendingList.
		ensurePendingListExistence(inputCdr);

		// 接收到重单, 忽略
		if (checkDuplicate(inputCdr)) {
			log.trace("a duplicate P-GW CDR is ignored: {}", inputCdr);
			return false;
		} else {

			CategoryKey categoryKey = genCategoryKeyFor(inputCdr);
			MergingDistinctKey mergingDistintKey = genMergingDistinctKeyFor(inputCdr);

			PendingList mergingList = storageBackend.get(categoryKey.toString())
					.getAllMergingList().get(mergingDistintKey);

			if (!MergingUtils.isPartialCdr(inputCdr)) {
				// 完整话单: 此处才将原始的完整话单放入PendingList的mergingList: List<AsnValue>中.
				mergingList.getMergingList().add(inputCdr);
				newCdrAdded = true;
			} else {

				// 非完整话单, 且不是重单: 也放入PendingList#mergingList: List<AsnValue>中.
				mergingList.getMergingList().add(inputCdr);
				log.trace(
						"a PARTIAL P-GW CDR: {} is received and added to Category: {}, PendingList: {}",
						new Object[] { inputCdr, categoryKey, mergingDistintKey });
				newCdrAdded = true;

			}

			if (MergingUtils.isTriggerPgwCdrMerging(inputCdr)) {
				if (mergingList.needMerge) {
					// 部分话单, 接收到触发合并条件的P-GW CDR:
					mergingList.setStatus(MergingStatus.ALL_PARTIAL_P_GW_CDRs_RECEIVED);
					log.trace(
							"all PARTIAL P-GW CDR of Category: {}, PendingList: {} have just received, triggering merge action...",
							categoryKey, mergingDistintKey);
				} else {
					// 完整话单(无实际的合并动作):
					log.trace(
							"a COMPLETE P-GW CDR of Category: {}, PendingList: {} have just received, triggering merge action...",
							categoryKey, mergingDistintKey);
				}

				// implicit call: PendingList#onLastRecordReceived(CdrProcessing):
				CdrProcessingEvent newEvent = new ParticularCdrReceived(inputCdr);
				mergingList.onMergingTriggerCdrReceived(newEvent);
			}
		}

		return newCdrAdded;
	}

	@Override
	public boolean merge(AsnValue inputCdr) throws Exception {
		// TODO Auto-generated method stub

		boolean newCdrAdded = false;

		return newCdrAdded;
	}

	public void persist(AsnValue outputCdr, Map<String, String> config) {

		persistMgr.persist(outputCdr, config);
	}

	/**
	 * <p>
	 * 调用条件: 接收到的CDR的causeForRecClosing为: normalRelease(0)
	 * </p>
	 *
	 * @param key
	 * @throws Exception
	 */
	private void doMerge(PendingList mergingList, CdrProcessingEvent event)
			throws Exception {

		if (mergingList != null) {

			// 若只包含完整话单: 认为合并动作已完成
			if (mergingList.isNeedMerge()) {
				// 对部分话单进行合并:
				AsnValue mergingResultStub = mergingList.getMergingResultStub();

				/*
				 * 1. 按recordSequenceNumber, 对PendingList中所有的P-GW CDR进行排序:
				 */
				List<TempComparator> sortedList = new ArrayList<PgwCdrMerger.TempComparator>();
				for (AsnValue each : mergingList.getMergingList()) {
					sortedList.add(new TempComparator(each));
				}
				Collections.sort(sortedList);

				AsnValue firstPartialPgwCdr = sortedList.get(0).getPgwCdr();
				AsnValue lastPartialPgwCdr = sortedList.get(sortedList.size() - 1).getPgwCdr();
				mergingList.setMinRecordSequenceNumber(MergingUtils
						.getRecordSequenceNumberValueOfInputCdr(firstPartialPgwCdr).intValue());
				mergingList.setMaxRecordSequenceNumber(MergingUtils
						.getRecordSequenceNumberValueOfInputCdr(lastPartialPgwCdr).intValue());

				/*
				 * 2. 处理合并结果的recordOpeningTime 和 duration字段:
				 */

				// 话单是否连续: 根据recordOpeningTime + duration与下一个部分话单的recordOpeningTime是否相差2秒以内判断:
				boolean isConsecutive = true;
				// 部分话单的duration累计值: TODO: 使用int类型是否够大?
				int totalDuration = MergingUtils.getDurationValueOfInputCdr(firstPartialPgwCdr).intValue();

				AsnValue previousPartialPgwCdr = firstPartialPgwCdr;
				for (int i = 1; i < sortedList.size(); ++i) {

					AsnValue currentPartialPgwCdr = sortedList.get(i).getPgwCdr();
					totalDuration += MergingUtils
							.getDurationValueOfInputCdr(currentPartialPgwCdr).intValue();

					if (!isConsecutive
							|| MergingUtils.getRecordSequenceNumberValueOfInputCdr(
									currentPartialPgwCdr)
									.intValue()
									- MergingUtils.getRecordSequenceNumberValueOfInputCdr(
											previousPartialPgwCdr).intValue() != 1) {
						// recordSequenceNumber不连续, 则认为部分话单非连续:
						isConsecutive = false;
					}
				}

				if (isConsecutive) {
					// @formatter:off
					/*
					 * 8.2.5. PGW-CDR的合并
					 *
					 * 4）需要过滤的字段 若部分话单连续, 则: 对于一批连续部分话单，Duration字段 = （最后的部分记录的Record Opening Time -
					 * 最先的部分记录的Record Opening Time + 最后的部分记录的Duration字段）； 对于不连续话单Duration字段累加。
					 */
					// @formatter:on
					TimeStamp firstRecordOpeningTime = MergingUtils
							.getRecordOpeningTimeValueOfInputCdr(firstPartialPgwCdr);
					TimeStamp lastRecordOpeningTime = MergingUtils
							.getRecordOpeningTimeValueOfInputCdr(lastPartialPgwCdr);
					int lastDuration = MergingUtils
							.getDurationValueOfInputCdr(lastPartialPgwCdr).intValue();

					totalDuration = lastDuration
							+ (int) (lastRecordOpeningTime.getTimeAsSecond() - firstRecordOpeningTime
									.getTimeAsSecond());
				}

				// 合并后的duration字段:
				AsnInteger mergedDuration = BerCodingUtils.createAsn1Value(AsnTypes.INTEGER);
				mergedDuration.setValue(totalDuration);
				mergedDuration.setTag(BerCodingUtils.createAsn1Tag(AsnTagClass.ContextSpecific,
						AsnTagNature.Primitive, 14));

				BerCodingUtils.replaceElementByTagNum((AsnContainerValueBase) mergingResultStub,
						mergedDuration, 14);

				// TODO: 合并后的recordOpeningTime字段, 取recordSequenceNumber最小的部分话单的;
				// 能否确保recordOpeningTime也是最小的?
				AsnValue minRecordOpeningTime = BerCodingUtils.getElementByTagNum(
						(AsnContainerValueBase) firstPartialPgwCdr, 13);

				if (!BerCodingUtils.getElementByTagNum((AsnContainerValueBase) mergingResultStub,
						13).equals(minRecordOpeningTime)) {
					// 当接收到的第一个部分P-GW CDR不是recordSequenceNumber最小的那个, 即CG并非按部分话单的顺序对其进行接收时:
					BerCodingUtils.replaceElementByTagNum(
							(AsnContainerValueBase) mergingResultStub, minRecordOpeningTime, 13);
				}

				/*
				 * 3. 处理合并结果的recordSequenceNumber, localSequenceNumber字段:
				 *
				 * recordSequenceNumber [17] RECORDSequenceNumber OPTIONAL, localSequenceNumber [20]
				 * LOCALRECORDSequenceNumber OPTIONAL
				 */
				AsnContainerValueBase mergingResultRecordSequenceNumber = BerCodingUtils
						.getComplexElementByTagNum((AsnContainerValueBase) mergingResultStub, 17);
				AsnContainerValueBase mergingResultRecordNumberList = BerCodingUtils
						.getComplexElementByTagNum(mergingResultRecordSequenceNumber, 1);

				AsnContainerValueBase mergingResultLocalSequenceNumber = BerCodingUtils
						.getComplexElementByTagNum((AsnContainerValueBase) mergingResultStub, 20);
				AsnContainerValueBase mergingResultLocalRecordNumberList = BerCodingUtils
						.getComplexElementByTagNum(mergingResultLocalSequenceNumber, 1);

				for (int i = 0; i < sortedList.size(); ++i) {
					// 按recordSequenceNumber的顺序, 将每个部分话单的recordSequenceNumber,
					// 和localSequenceNumber, 分别append到合并结果的recordSequenceNumber列表,
					// 以及localSequenceNumber列表中去:

					AsnValue currentPartialPgwCdr = sortedList.get(i).getPgwCdr();

					AsnValue currentRecordSequenceNumber = BerCodingUtils.getElementByTagNum(
							(AsnContainerValueBase) currentPartialPgwCdr, 17);
					if (currentRecordSequenceNumber != null) {
						// 对于部分话单, recordSequenceNumber应该一定非空.
						BerCodingUtils.appendElement(mergingResultRecordNumberList,
								currentRecordSequenceNumber);
					}

					AsnValue currentLocalSequenceNumber = BerCodingUtils.getElementByTagNum(
							(AsnContainerValueBase) currentPartialPgwCdr, 20);
					if (currentLocalSequenceNumber != null) {
						BerCodingUtils.appendElement(mergingResultLocalRecordNumberList,
								currentLocalSequenceNumber);
					}
				}

				// TODO: 完成有完整待合并P-GW CDR列表的PendingList的合并.

				/*
				 * 4. 处理合并结果的listOfServiceData字段(FIXME: 仅仅将所有部分话单的listOfServiceData进行连接):
				 *
				 * listOfServiceData [34] SEQUENCE OF ChangeOfServiceCondition OPTIONAL
				 */

				// 合并结果的listOfServiceData: SEQUENCE OF 字段:
				AsnContainerValueBase mergingResultListOfServiceData = BerCodingUtils
						.getComplexElementByTagNum((AsnContainerValueBase) mergingResultStub, 34);
				if (mergingResultListOfServiceData == null) {

					mergingResultListOfServiceData = BerCodingUtils
							.createAsn1Value(AsnTypes.SEQUENCE_OF);
					mergingResultListOfServiceData.setTag(BerCodingUtils.createAsn1Tag(
							AsnTagClass.ContextSpecific, AsnTagNature.Constructed, 34));
				}

				for (int i = 0; i < sortedList.size(); ++i) {
					// 按recordSequenceNumber的顺序, 将每个部分话单的listOfServiceData中的每条记录,
					// 都给append到合并结果的listOfServiceData中去:

					AsnValue currentPartialPgwCdr = sortedList.get(i).getPgwCdr();

					AsnContainerValueBase currentListOfServiceData = BerCodingUtils
							.getComplexElementByTagNum(
									(AsnContainerValueBase) currentPartialPgwCdr, 34);
					if (currentListOfServiceData != null) {
						for (int j = 0; j < currentListOfServiceData.size(); ++j) {
							BerCodingUtils.appendElement(mergingResultListOfServiceData,
									BerCodingUtils
											.getElementByPosition(currentListOfServiceData, j));
						}
					}
				}

				if (BerCodingUtils.hasElementWithTagNum((AsnContainerValueBase) mergingResultStub,
						34)) {
					BerCodingUtils.replaceElementByTagNum(
							(AsnContainerValueBase) mergingResultStub,
							mergingResultListOfServiceData, 34);
				} else {
					BerCodingUtils.addElement((AsnContainerValueBase) mergingResultStub,
							mergingResultListOfServiceData);
				}
			}
		}

		if (mergingList.needMerge) {
			mergingList.setStatus(MergingStatus.MERGE_DONE);
			log.trace("PendingList: {} state change to: {}", mergingList.getKeyStringRepr(),
					mergingList.getStatus());
		}

		mergingList.onMergeComplete(event);
	}

	private boolean existMergingResultStub(AsnValue inputCdr) throws Exception {

		CategoryKey categoryKey = genCategoryKeyFor(inputCdr);

		MergingDistinctKey mergingDistinctKey = genMergingDistinctKeyFor(inputCdr);

		CategoryContainer category = storageBackend.get(categoryKey.toString());
		if (category == null) {
			return false;
		} else {

			PendingList mergingList = category.getAllMergingList().get(mergingDistinctKey);
			if (mergingList == null) {
				return false;
			} else {

				return true;
			}
		}
	}

	@Override
	public CdrMergingManager getCdrMergingManager() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>
	 * 首次接收特定的: {chargingId, pgwAddress} 的P-GW CDR时调用.
	 * </p>
	 *
	 * @param inputPgwCdr
	 * @return
	 * @throws Exception
	 */
	private CategoryContainer genCategoryContainerFor(AsnValue inputPgwCdr) throws Exception {

		// key:
		CategoryKey key = genCategoryKeyFor(inputPgwCdr);

		CategoryContainer category = new CategoryContainer();

		category.setKey(key);
		category.setKeyStringRepr(key.toString());

		// the MergingKey & PendingList object for inputPgwCdr:
		PendingList pendingList = genPendingListFor(inputPgwCdr);
		pendingList.setCategory(category);

		return category;
	}

	/**
	 * <p>
	 * 若为完整话单, 则必须进行如下状态的更新:
	 * <ul>
	 * <li>将合并列表PendingList的needMerge置为false;</li><br />
	 * <br />
	 * <li>将合并列表PendingList的status置为MergingStatus.ONE_COMPLETE_P_GW_CDR_RECEIVED_NO_NEED_MERGING;</li>
	 * </ul>
	 * </p>
	 *
	 * @param inputPgwCdr
	 * @return
	 * @throws Exception
	 */
	private PendingList genPendingListFor(AsnValue inputPgwCdr) throws Exception {

		PendingList pendingList = new PendingList();

		MergingDistinctKey key = genMergingDistinctKeyFor(inputPgwCdr);

		String keyStringRepr = key.toString();

		AsnValue mergingResultStub = MergingUtils.createOutputPgwCdrFromInputPgwCdr(
				(AsnContainerValueBase) inputPgwCdr,
				ConsolidationResult.INIT.getValue());

		MergingStatus status;
		boolean needMerge;
		if (!MergingUtils.isPartialCdr(inputPgwCdr)) {
			status = MergingStatus.ONE_COMPLETE_P_GW_CDR_RECEIVED_NO_NEED_MERGING;
			needMerge = false;
		} else {
			status = MergingStatus.FIRST_P_GW_CDR_RECEIVED;
			needMerge = true;
		}

		pendingList.setKey(key);
		pendingList.setKeyStringRepr(keyStringRepr);
		pendingList.setMergingResultStub(mergingResultStub);
		pendingList.setStatus(status);
		pendingList.setNeedMerge(needMerge);

		return pendingList;
	}

	private CategoryKey genCategoryKeyFor(AsnValue inputPgwCdr) throws Exception {

		CategoryKey key = new CategoryKey();

		// chargingID:
		AsnInteger chargingIdObj = BerCodingUtils.getAtomElementByTagNum(
				(AsnContainerValueBase) inputPgwCdr, 5, AsnTypes.INTEGER);
		Number chargingId = chargingIdObj.getValue();

		// p-GWAddress:
		AsnValue p_GWAddressObj = MergingUtils.getPgwAddressOfInputCdr(inputPgwCdr);
		InetAddress p_GWAddress = MergingUtils.ipAddressObjectToInetAddress(p_GWAddressObj);

		key.setChargingId(chargingId);
		key.setPgwAddress(p_GWAddress);

		return key;
	}

	private MergingDistinctKey genMergingDistinctKeyFor(AsnValue inputPgwCdr) throws Exception {

		MergingDistinctKey key = new MergingDistinctKey();

		// chargingID:
		AsnInteger chargingIdObj = BerCodingUtils.getAtomElementByTagNum(
				(AsnContainerValueBase) inputPgwCdr, 5, AsnTypes.INTEGER);
		Number chargingId = chargingIdObj.getValue();

		// p-GWAddress:
		AsnValue p_GWAddressObj = MergingUtils.getPgwAddressOfInputCdr(inputPgwCdr);
		InetAddress p_GWAddress = MergingUtils.ipAddressObjectToInetAddress(p_GWAddressObj);

		// servingNodeType:
		List<String> servingNodeTypes = MergingUtils.resolveServingNodeTypesToStrings(BerCodingUtils
				.getComplexElementByTagNum((AsnContainerValueBase) inputPgwCdr, 35));

		// servingNodeAddress:
		List<InetAddress> servingNodeAddresses = new ArrayList<InetAddress>();
		List<AsnValue> servingNodeAddressObjs = MergingUtils
				.resolveServingNodeAddresses(BerCodingUtils.getComplexElementByTagNum(
						(AsnContainerValueBase) inputPgwCdr, 6));
		for (AsnValue each : servingNodeAddressObjs) {
			servingNodeAddresses.add(MergingUtils.ipAddressObjectToInetAddress(each));
		}

		// rATType:
		AsnInteger ratTypeObj = BerCodingUtils.getAtomElementByTagNum(
				(AsnContainerValueBase) inputPgwCdr, 30, AsnTypes.INTEGER);
		Number ratType = (ratTypeObj == null ? null : ratTypeObj.getValue());

		key.setChargingId(chargingId);
		key.setPgwAddress(p_GWAddress);
		key.setServingNodeType(servingNodeTypes);
		key.setServingNodeAddress(servingNodeAddresses);
		key.setRatType(ratType);

		return key;
	}

	/**
	 * <p>
	 * 若已经包含相应的CategoryContainer, 直接返回true; 否则新建, 并返回false.
	 * </p>
	 *
	 * @param inputPgwCdr
	 * @return
	 * @throws Exception
	 */
	private boolean ensureCategoryContainerExistence(AsnValue inputPgwCdr) throws Exception {

		CategoryKey categoryKey = genCategoryKeyFor(inputPgwCdr);

		if (!storageBackend.contains(categoryKey.toString())) {
			CategoryContainer category = genCategoryContainerFor(inputPgwCdr);
			storageBackend.put(categoryKey.toString(), category);
			log.trace("a new Category is created: {}", categoryKey);

			return false;
		}

		return true;
	}

	/**
	 * <p>
	 * 若已经包含相应的PendingList, 直接返回true; 否则新建, 并返回false.
	 * </p>
	 *
	 * @param inputPgwCdr
	 * @return
	 * @throws Exception
	 */
	private boolean ensurePendingListExistence(AsnValue inputPgwCdr) throws Exception {

		ensureCategoryContainerExistence(inputPgwCdr);

		CategoryKey categoryKey = genCategoryKeyFor(inputPgwCdr);
		CategoryContainer category = storageBackend.get(categoryKey.toString());

		MergingDistinctKey distinctKey = genMergingDistinctKeyFor(inputPgwCdr);
		if (!category.getAllMergingList().containsKey(distinctKey)) {

			PendingList mergingList = genPendingListFor(inputPgwCdr);
			category.getAllMergingList().put(distinctKey, mergingList);
			log.trace("a new PendingList is created: {}", distinctKey);
			mergingList.setCategory(category);

			return false;
		}

		return true;
	}

	public void clearCdrMergerContext(PgwCdrCategoryKey key) {

		mergerContexts.delete(key.toString());
	}
}
