package com.freedom.messagebus.benchmark.client.testCase;

import com.freedom.messagebus.benchmark.client.*;
import com.freedom.messagebus.common.AbstractInitializer;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.interactor.message.IMessageBodyProcessor;
import com.freedom.messagebus.interactor.message.MessageBodyProcessorFactory;
import com.freedom.messagebus.interactor.message.MessageHeaderProcessor;
import com.freedom.messagebus.interactor.proxy.ProxyProducer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class OriginalProduceTestCase extends Benchmark {

    private static final Log logger = LogFactory.getLog(OriginalProduceTestCase.class);

    private static class BasicProduce extends AbstractInitializer implements Runnable, ITerminater, IFetcher {

        private Message               msg;
        private double                msgBodySize;
        private String                routingkey;
        private IMessageBodyProcessor msgBodyProcessor;
        private boolean flag    = true;
        private long    counter = 0;

        private BasicProduce(String host) {
            super(host);
            msg = TestMessageFactory.create(MessageType.QueueMessage, msgBodySize);
        }

        @Override
        public void terminate() {
            this.flag = false;
        }

        @Override
        public void run() {
            try {
                this.init();
                while (flag) {
                    msgBodyProcessor = MessageBodyProcessorFactory.createMsgBodyProcessor(msg.getMessageType());
                    ProxyProducer.produce(CONSTS.PROXY_EXCHANGE_NAME,
                                          this.channel,
                                          this.getRoutingkey(),
                                          msgBodyProcessor.box(msg.getMessageBody()),
                                          MessageHeaderProcessor.box(msg.getMessageHeader()));
                    ++counter;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    this.close();
                } catch (IOException e) {

                }
            }
        }

        @Override
        public long fetch() {
            return counter;
        }

        public double getMsgBodySize() {
            return msgBodySize;
        }

        public void setMsgBodySize(double msgBodySize) {
            this.msgBodySize = msgBodySize;
        }

        public String getRoutingkey() {
            return routingkey;
        }

        public void setRoutingkey(String routingkey) {
            this.routingkey = routingkey;
        }
    }

    public static void main(String[] args) {
        OriginalProduceTestCase testCase = new OriginalProduceTestCase();

        String host = "172.16.206.30";

        BasicProduce task = new BasicProduce(host);
        task.setMsgBodySize(TestConfigConstant.MSG_BODY_SIZE_OF_KB);
        task.setRoutingkey("routingkey.proxy.message.business.crm");

        testCase.test(task, TestConfigConstant.HOLD_TIME_OF_MILLIS,
                      TestConfigConstant.FETCH_NUM, "single_thread_original_produce_one_by_one");
    }


}
