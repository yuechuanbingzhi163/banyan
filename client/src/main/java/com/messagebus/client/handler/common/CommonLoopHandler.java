package com.messagebus.client.handler.common;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.common.ExceptionHelper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by yanghua on 2/22/15.
 */
public abstract class CommonLoopHandler extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(CommonLoopHandler.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        QueueingConsumer currentConsumer = (QueueingConsumer) context.getOtherParams().get("consumer");
        try {
            while (true) {
                QueueingConsumer.Delivery delivery = currentConsumer.nextDelivery();

                AMQP.BasicProperties properties = delivery.getProperties();
                byte[] msgBody = delivery.getBody();

//                context.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                String msgTypeStr = properties.getType();
                if (msgTypeStr == null || msgTypeStr.isEmpty()) {
                    logger.error("[run] message type is null or empty");
                    continue;
                }

                MessageType msgType = null;
                try {
                    msgType = MessageType.lookup(msgTypeStr);
                } catch (UnknownError unknownError) {
                    ExceptionHelper.logException(logger, unknownError, "common loop handler");
                    continue;
                }
                Message msg = MessageFactory.createMessage(msgType);
                initMessage(msg, msgType, properties, msgBody);

                context.setConsumedMsg(msg);

                if (msgType.equals(MessageType.BroadcastMessage) && context.getNoticeListener() != null) {
                    IMessageReceiveListener noticeListener = context.getNoticeListener();
                    noticeListener.onMessage(msg);
                } else {
                    process(context);
                }
            }
        } catch (InterruptedException e) {
            logger.info("[run] close the consumer's message handler!");
        } catch (Exception e) {
            ExceptionHelper.logException(logger, e, "common loop handler");
        } finally {
            chain.handle(context);
        }
    }

    public abstract void process(MessageContext msgContext);

    private void initMessage(Message msg, MessageType msgType, AMQP.BasicProperties properties, byte[] bodyData) {
        MessageHeaderTransfer.unbox(properties, msg);
        msg.setContent(bodyData);
    }
}
