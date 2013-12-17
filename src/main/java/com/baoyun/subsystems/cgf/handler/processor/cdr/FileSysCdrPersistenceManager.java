package com.baoyun.subsystems.cgf.handler.processor.cdr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.panter.li.bi.asn.AsnValue;

import com.baoyun.subsystems.cgf.utils.BerCodingUtils;
import com.baoyun.subsystems.cgf.utils.MiscUtils;

public class FileSysCdrPersistenceManager extends CdrPersistenceManager {

	/**
	 * &lt;持久化的目标CDR, 相关参数&gt;
	 *
	 *
	 */
	private class QueueItem {

		private AsnValue cdr;

		private Map<String, String> config;

		public QueueItem() {

		}

		public AsnValue getCdr() {

			return cdr;
		}

		public void setCdr(AsnValue cdr) {

			this.cdr = cdr;
		}

		public Map<String, String> getConfig() {

			return config;
		}

		public void setConfig(Map<String, String> config) {

			this.config = config;
		}
	}

	private Logger log = LoggerFactory.getLogger(FileSysCdrPersistenceManager.class);

	/**
	 * cdr I/O缓存队列.
	 */
	private BlockingQueue<QueueItem> bufferQueue = new LinkedBlockingDeque<QueueItem>();

	/**
	 * file name pattern, saving folder, ...
	 */
	private CdrFileSysPersistenceInfo config;

	/**
	 * fs I/O线程.
	 */
	private Thread persister = new Thread(new Runnable() {

		@Override
		public void run() {

			while (true) {
				QueueItem item;
				try {
					item = bufferQueue.take();
					log.trace(
							"an item: {{}, {}} has just been taken out of the persistence queue and is about to be written to a file",
							item.getCdr(), item.getConfig());
					saveToFile(item.getCdr(), item.getConfig());
				} catch (InterruptedException e) {

					log.error(MiscUtils.exceptionStackTrace2String(e));
				}
			}
		}

		private void saveToFile(AsnValue cdr, Map<String, String> config) {

			long sn = FileSysCdrPersistenceManager.this.config.genSn();

			String prefix = FileSysCdrPersistenceManager.this.config.getFolder()
					+ System.getProperty("file.separator");

			String suffix = ".cdr";

			File folder = new File(prefix);
			if (!folder.exists()) {
				folder.mkdir();
			}

			String fileName = prefix
					+ String.format(
							FileSysCdrPersistenceManager.this.config.getFileNamePattern(),
							config.get("type"), config.get("merge"), sn);

			if (config.get("merge").equals("ORIGINAL")) {
				fileName += "_" + config.get("seq");
			}

			OutputStream out = null;
			try {
				out = new FileOutputStream(fileName + suffix, false);
				out.write(BerCodingUtils.encode(cdr));
			} catch (Exception e) {

				StringWriter buf = new StringWriter();
				PrintWriter writer = new PrintWriter(buf);
				e.printStackTrace(writer);
				log.error(buf.toString());
			} finally {

				if (out != null) {
					try {
						out.close();
					} catch (Exception e2) {

						StringWriter buf = new StringWriter();
						PrintWriter writer = new PrintWriter(buf);
						e2.printStackTrace(writer);
						log.error(buf.toString());
					}
				}
			}
		}
	});

	public FileSysCdrPersistenceManager() {

		CdrFileSysPersistenceInfo config = new CdrFileSysPersistenceInfo();
		// example: "P-GW_MERGE_1103.cdr", "P-GW_ORIGINAL_1205_1.cdr"
		config.setFileNamePattern("%s_%s_%s");
		config.setFolder("./saved_cdrs");

		this.config = config;
		persister.start();
	}

	public FileSysCdrPersistenceManager(CdrFileSysPersistenceInfo config) {

		this.config = config;
		persister.start();
	}

	/**
	 * <p>
	 * 提交持久化请求: 插入fs I/O缓存队列.
	 * </p>
	 *
	 * <p>
	 * config:
	 * <ul>
	 * <li>"type": {"P-GW", "S-GW"};</li>
	 * <li>"merge": {"MERGE", "ORIGINAL"};</li>
	 * <li>"partial": {"COMPLETE", "PARTIAL"};</li>
	 * <li>"sn": 取CdrFileSysPersistenceInfo#genSn()的值;</li>
	 * <li>"seq": 按照部分话单合并列表中的顺序;</li>
	 * </ul>
	 * </p>
	 */
	@Override
	public void persist(AsnValue cdr, Map<String, String> config) {

		QueueItem item = new QueueItem();
		item.setCdr(cdr);
		item.setConfig(config);

		bufferQueue.add(item);
		log.trace("a new item: {{}, {}} has just been added into the persistence queue", cdr,
				config);
	}

}
