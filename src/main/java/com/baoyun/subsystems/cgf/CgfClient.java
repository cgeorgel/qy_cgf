package com.baoyun.subsystems.cgf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.panter.li.bi.asn.AsnException;

import com.baoyun.subsystems.cgf.constants.Constants;
import com.baoyun.subsystems.cgf.gtpp.helpers.CDRProvider;
import com.baoyun.subsystems.cgf.gtpp.helpers.SequenceProvider;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeDataRecordTransferCDRRequest;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeDataRecordTransferSequenceRelease;
import com.baoyun.subsystems.cgf.gtpp.messages.GtpPrimeMessageFactory;
import com.baoyun.subsystems.cgf.handler.ClientReceiveGtppMessageHandler;
import com.baoyun.subsystems.cgf.handler.GtpPrimeObjectDecoder;
import com.baoyun.subsystems.cgf.handler.GtpPrimeObjectEncoder;

/**
 *
 */
public class CgfClient {

	private static final Logger _log = LoggerFactory.getLogger(CgfClient.class);

	// private static final String TEST_GTP_IP = "192.168.122.1";
//	private static final String TEST_GTP_IP = "127.0.0.1";
	
	private static final String TEST_GTP_IP = "192.168.202.33";
	
	private static final int TEST_GTP_PORT = 3386;

	private ChannelFactory channelFactory;

	private Channel channel;

	@SuppressWarnings("unused")
	private ChannelFuture writeFuture;

	private final Thread createrThread;

	public CgfClient() {

		channelFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());

		ConnectionlessBootstrap bootStrap = new ConnectionlessBootstrap(channelFactory);
		bootStrap.setPipelineFactory(new ChannelPipelineFactory() {

			public ChannelPipeline getPipeline() throws Exception {

				return Channels.pipeline(new GtpPrimeObjectEncoder(),
						new GtpPrimeObjectDecoder(),
						new ClientReceiveGtppMessageHandler() {

							@Override
							public void messageReceived(ChannelHandlerContext ctx,
									MessageEvent event) throws Exception {

								super.messageReceived(ctx, event);

								Channel owningChannel = ctx.getChannel();

								ChannelFuture channelFuture = owningChannel.getCloseFuture();
								channelFuture.addListener(new ChannelFutureListener() {

									@Override
									public void operationComplete(ChannelFuture arg0)
											throws Exception {

										_log.debug("channel: {} closed...", arg0.getChannel());

										CgfClient.this.createrThread.resume();
									}
								});

								// TODO: do NOT close the channel in stress test.
//								owningChannel.close();

								// @formatter:off
								// to avoid deadlock, prefer addListener() to awaitUninterruptibly()
								/*channelFuture.awaitUninterruptibly();
								if (!channelFuture.isSuccess()) {
									channelFuture.getCause().printStackTrace();
								}

								_log.debug("channel: {} closed...", owningChannel);

								CgfClient.this.createrThread.resume();*/
								// @formatter:on
							}
						});
			}
		});

		bootStrap.setOption("broadcast", "false");
		bootStrap.setOption("receiveBufferSizePredictorFactory",
				new FixedReceiveBufferSizePredictorFactory(1024));
		bootStrap.setOption("sendBufferSize", 65535);
		bootStrap.setOption("receiveBufferSize", 65535);

		channel = bootStrap.bind(new InetSocketAddress(0));

		createrThread = Thread.currentThread();
	}

	public void sendSampleEchoRequest() {

		writeFuture = channel.write(GtpPrimeMessageFactory.createEchoRequestMessage(),
				new InetSocketAddress(TEST_GTP_IP, TEST_GTP_PORT));
	}

	public void sendSampleNodeAliveRequest() {

		writeFuture = channel.write(GtpPrimeMessageFactory.createNodeAliveRequestMessage(
				"192.168.122.1", "fe80::224:d7ff:febd:559c"),
				new InetSocketAddress(TEST_GTP_IP, TEST_GTP_PORT));
	}

	public void sendSampleRedirectionRequest() {

		if (channel != null && channel.isOpen()) {
			writeFuture = channel.write(GtpPrimeMessageFactory.createRedirectionRequestMessage(
					Constants.GTP_PRIME_IET_CAUSE_NODE_GOING_DOWN,
					"192.168.122.1", "fe80::224:d7ff:febd:559c"),
					new InetSocketAddress(TEST_GTP_IP, TEST_GTP_PORT));
		}
	}

	class TestCompleteCDRProvider implements CDRProvider {

		ArrayList<byte[]> array;

		public TestCompleteCDRProvider() {
			// benu's p-GW cdr content ()
			byte[] pgwcdr = { (byte) 0xbf, 0x4f, (byte) 0x81, (byte) 0x9f, (byte) 0x80, 0x01, 0x55,
					(byte) 0x83,
					0x08, 0x04, 0x24, 0x00, 0x00, 0x00, 0x11, 0x11, (byte) 0xf1, (byte) 0xa4, 0x06,
					(byte) 0x80, 0x04,
					0x28, 0x28, 0x28, 0x28, (byte) 0x85, 0x01, 0x02, (byte) 0xa6, 0x06,
					(byte) 0x80, 0x04, (byte) 0xc0,
					(byte) 0xa8, (byte) 0xca, 0x36, (byte) 0x8b, 0x01, (byte) 0xff, (byte) 0x8d,
					0x09, 0x0b, 0x01, 0x01,
					0x02, 0x0c, 0x04, 0x2b, 0x00, 0x00, (byte) 0x8e, 0x01, 0x14, (byte) 0x8f, 0x01,
					0x00, (byte) 0x97, 0x02,
					0x00, 0x01, (byte) 0xbf, 0x22, 0x46, 0x30, 0x44, (byte) 0x81, 0x01, 0x14,
					(byte) 0x84, 0x01, 0x01,
					(byte) 0x85, 0x09, 0x0b, 0x01, 0x01, 0x02, 0x0c, 0x04, 0x2b, 0x00, 0x00,
					(byte) 0x86, 0x09, 0x0b, 0x01,
					0x01, 0x02, 0x0c, 0x18, 0x2b, 0x00, 0x00, (byte) 0x87, 0x01, 0x14, (byte) 0x88,
					0x02, 0x04, 0x00,
					(byte) 0xa9, 0x0c, (byte) 0x81, 0x01, 0x06, (byte) 0x84, 0x01, 0x00,
					(byte) 0x85, 0x01, 0x00, (byte) 0x86,
					0x01, 0x01, (byte) 0xaa, 0x06, (byte) 0x80, 0x04, (byte) 0xc0, (byte) 0xa8,
					(byte) 0xca, 0x36,
					(byte) 0x8e, 0x09, 0x0b, 0x01, 0x01, 0x02, 0x0c, 0x18, 0x2b, 0x00, 0x00,
					(byte) 0xbf, 0x23, 0x03, 0x0a,
					0x01, 0x03, (byte) 0x9f, 0x26, 0x09, 0x0b, 0x01, 0x01, 0x02, 0x0c, 0x04, 0x2b,
					0x00, 0x00, (byte) 0x9f, 0x27,
					0x09, 0x0b, 0x01, 0x01, 0x02, 0x0c, 0x18, 0x2b, 0x00, 0x00 };

			array = new ArrayList<byte[]>();
			array.add(pgwcdr);
			array.add(pgwcdr);
		}

		public int getDataRecordFormat() {
			return 1;
		}

		public int getDataRecordFormatVersion() {
			return 5;
		}

		public List<byte[]> getCDRs() {
			return array;
		}
	}

	public void sendSampleCompleteDataRecordTransferCDRRequest() {

		CDRProvider provider = new TestCompleteCDRProvider();
		GtpPrimeDataRecordTransferCDRRequest request = (GtpPrimeDataRecordTransferCDRRequest) GtpPrimeMessageFactory
				.createDataTransferCDRRequestMessage();
		try {
			request.addDataRecords(provider);
		} catch (Exception e) {
			e.printStackTrace();
		}

		writeFuture = channel.write(request, new InetSocketAddress(TEST_GTP_IP, TEST_GTP_PORT));
	}

	class TestPartialCDRProvider implements CDRProvider {

		ArrayList<byte[]> array;

		public TestPartialCDRProvider(int charId) {

			array = new ArrayList<byte[]>();

			try {
				array.add(CdrSendSimulator.setPartialPgwRecord(charId, new byte[] { 0x0b, 0x01,
						0x01, 0x02, 0x0c, 0x04, 0x2b, 0x00, 0x00 }, 12, 17, 1, true, false));
				array.add(CdrSendSimulator.setPartialPgwRecord(charId, new byte[] { 0x0b, 0x01,
						0x01, 0x02, 0x0c, 0x10, 0x2b, 0x00, 0x00 }, 8, 0, 2, false, true));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AsnException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public int getDataRecordFormat() {
			return 1;
		}

		public int getDataRecordFormatVersion() {
			return 5;
		}

		public List<byte[]> getCDRs() {
			return array;
		}
	}

	public void sendSamplePartialDataRecordTransferCDRRequest(int chargingId) {

		CDRProvider provider = new TestPartialCDRProvider(chargingId);
		GtpPrimeDataRecordTransferCDRRequest request = (GtpPrimeDataRecordTransferCDRRequest) GtpPrimeMessageFactory
				.createDataTransferCDRRequestMessage();
		try {
			request.addDataRecords(provider);
		} catch (Exception e) {
			e.printStackTrace();
		}

		writeFuture = channel.write(request, new InetSocketAddress(TEST_GTP_IP, TEST_GTP_PORT));
	}

	public void sendStressSamplePartialDataRecordTransferCDRRequestStress(int startCid, int total) {

		for (int i = 0; i < total; ++i) {

			CDRProvider provider = new TestPartialCDRProvider(startCid + i);
			GtpPrimeDataRecordTransferCDRRequest request = (GtpPrimeDataRecordTransferCDRRequest) GtpPrimeMessageFactory
					.createDataTransferCDRRequestMessage();
			try {
				request.addDataRecords(provider);
			} catch (Exception e) {
				e.printStackTrace();
			}

			writeFuture = channel.write(request, new InetSocketAddress(TEST_GTP_IP, TEST_GTP_PORT));
		}
	}

	class TestProvider implements SequenceProvider {

		ArrayList<Integer> array;

		public TestProvider() {
			int[] arr = { 1, 2, 3, 5, 8, 13, 21, 34 };

			array = new ArrayList<Integer>();
			for (int i = 0; i < arr.length; i++) {
				array.add(arr[i]);
			}

		}

		public List<Integer> getSequenceList() {
			return array;
		}

	}

	public void sendSampleDataRecordReleaseTransferRequest() {

		TestProvider provider = new TestProvider();
		GtpPrimeDataRecordTransferSequenceRelease request = (GtpPrimeDataRecordTransferSequenceRelease) GtpPrimeMessageFactory
				.createDataTransferSequenceReleaseRequestMessage();
		request.addCancelledSequences(provider);
		writeFuture = channel.write(request, new InetSocketAddress(TEST_GTP_IP, TEST_GTP_PORT));
	}

	public void closeUDPTransport() {

		if (channel != null && channel.isOpen()) {
			channel.close();
		}
	}

	public static void main(String[] args) {

		CgfClient client = new CgfClient();
		_log.debug("client starting................");
		// client.sendSampleNodeAliveRequest();
		// client.sendSampleEchoRequest();
		// client.sendSampleRedirectionRequest();

		// client.sendSampleDataRecordTransferRequest();
//		client.sendSampleCompleteDataRecordTransferCDRRequest();
//		client.sendSampleDataRecordReleaseTransferRequest();

		int startCid = 1103;
		int totalCid = 2;

//		client.sendSamplePartialDataRecordTransferCDRRequest(startCid);

		client.sendStressSamplePartialDataRecordTransferCDRRequestStress(startCid, totalCid);

		Thread.currentThread().suspend();

		client.channelFactory.releaseExternalResources();

		_log.debug("Closing the GTP' client");
	}

}
