package com.baoyun.subsystems.cgf.utils;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>
 * For debugging, ...
 * </p>
 *
 * @author George
 *
 */
public class MiscUtils {

	/**
	 * <code>
	 * <pre> StringWriter buf = new StringWriter();
	 * PrintWriter out = new PrintWriter(buf);
	 *
	 * e.<b>printStackTrace(out)</b>;
	 *
	 * return buf.toString();
	 * </pre>
	 * </code>
	 *
	 * @param e
	 * @return
	 */
	public static String exceptionStackTrace2String(Throwable e) {

		StringWriter buf = new StringWriter();
		PrintWriter out = new PrintWriter(buf);

		e.printStackTrace(out);

		return buf.toString();
	}

	/**
	 * <p>
	 * 将指定的stack trace elements, 用指定的分隔符连接, 最后返回其字符串形式.
	 * </p>
	 *
	 * @param trace
	 *            StackTraceElement[].
	 * @param delim
	 *            默认为{@link System#getProperty(String) System.getProperty("line.separator")}.
	 * @return
	 */
	public static String stackTraceElements2String(StackTraceElement[] trace, String... delim) {

		String defaultDelim = System.getProperty("line.separator");
		String theDelim = delim.length == 0 ? defaultDelim : delim[0];
		if (theDelim.length() == 0) {
			theDelim = defaultDelim;
		}

		StringBuilder sb = new StringBuilder();
		for (StackTraceElement each : trace) {
			sb.append(each).append(theDelim);
		}
		sb.delete(sb.length() - theDelim.length(), sb.length());

		return sb.toString();
	}

	/**
	 * <p>
	 * 周期地(秒), 将当前Thread的dump, 按如下格式, 写入指定的目标流中去. 当写入次数达到指定次数后便不再写
	 * (首次写之前会延迟指定的周期时间):
	 * <pre> thread dump: <i>current-thread-name</i>:
	 * 	<i>StackTraceElement[0]</i>
	 * 	<i>StackTraceElement[1]</i>
	 * 	...
	 * </pre>
	 * </p>
	 *
	 * @param intervalInSecond
	 *            以秒为单位的周期.
	 * @param times
	 *            写多少次?
	 * @param whereToDump
	 *            要写入的目标.
	 */
	public static void periodicallyDumpCurrentThread(int intervalInSecond, final int times,
			OutputStream... whereToDump) {

		final Thread targetThread = Thread.currentThread();
		/*
		 * TODO: dump输出的目标流的close(), 至多应该是调用者的责任; 但当装饰在其上的writer和exceptionWriter被GC后,
		 * 是否会造成被装饰的目标流被close()?
		 */
		@SuppressWarnings("resource")
		final OutputStream where = whereToDump.length == 0 ? System.err : whereToDump[0];

		final Timer timer = new Timer();

		TimerTask dumper = new TimerTask() {

			private int executedTimes = 0;

			private Writer writer = new OutputStreamWriter(where);

//			private PrintWriter exceptionWriter = new PrintWriter(writer);

			@Override
			public void run() {

				if (executedTimes++ < times) {

					String dump = "thread dump: " + targetThread.getName()
							+ ":" + System.getProperty("line.separator") + "\t"
							+ stackTraceElements2String(targetThread.getStackTrace(),
									System.getProperty("line.separator") + "\t")
							+ System.getProperty("line.separator");
					try {
						writer.write(dump);
						writer.flush();
					} catch (Exception e) {
//						e.printStackTrace(exceptionWriter);
						e.printStackTrace();
					}
				} else {

					try {
						writer.flush();
//						exceptionWriter.flush();
//						writer.close();
					} catch (Exception e) {
//						e.printStackTrace(exceptionWriter);
						e.printStackTrace();
					}

					timer.cancel();
				}
			}
		};

		timer.scheduleAtFixedRate(dumper, intervalInSecond * 1000, intervalInSecond * 1000);
	}
}
