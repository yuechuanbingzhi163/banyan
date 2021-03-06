package com.messagebus.client.carry.impl;

import com.messagebus.business.model.Node;
import com.messagebus.client.AbstractMessageCarryer;
import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.carry.ISubscriber;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.MessageCarryHandlerChain;
import com.messagebus.client.handler.common.AsyncEventLoop;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GenericSubscriber extends AbstractMessageCarryer implements Runnable, ISubscriber {

    private static final Log logger = LogFactory.getLog(GenericSubscriber.class);

    private Thread                  currentThread;
    private TimeUnit                timeUnit;
    private IMessageReceiveListener onMessage;
    private String                  secret;

    private       long      timeout      = 0;
    private final Lock      eventLocker  = new ReentrantLock();
    private final Condition eventBlocker = eventLocker.newCondition();
    private final Lock      mainLock     = new ReentrantLock();
    private final Condition mainBlocker  = mainLock.newCondition();

    public GenericSubscriber() {
    }

    @Override
    public void run() {
        eventLocker.lock();
        final MessageContext ctx = initMessageContext();
        try {
            ctx.setCarryType(MessageCarryType.SUBSCRIBE);
            ctx.setSecret(this.secret);
            Node sourceNode = this.getContext().getConfigManager().getSecretNodeMap().get(this.secret);
            ctx.setSourceNode(sourceNode);
            ctx.setReceiveListener(this.onMessage);
            ctx.setSync(false);
            ctx.setNoticeListener(this.getContext().getNoticeListener());

            checkState();

            this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.SUBSCRIBE, this.getContext());
            this.genericSubscribe(ctx, handlerChain);

            if (this.timeout != 0) {
                eventBlocker.await(this.timeout,
                                   this.timeUnit == null ? TimeUnit.SECONDS : this.timeUnit);
            } else {
                eventBlocker.await();
            }
        } catch (InterruptedException e) {

        } finally {
            if (ctx.getAsyncEventLoop().isAlive()) {
                ctx.getAsyncEventLoop().shutdown();
            }

            eventLocker.unlock();
        }
    }

    @Override
    public void subscribe(String secret, IMessageReceiveListener onMessage, long timeout, TimeUnit unit) {
        this.onMessage = onMessage;
        this.timeout = timeout;
        this.timeUnit = unit;
        this.secret = secret;

        this.startup();
    }

    private void startup() {
        if (this.timeout != 0) {
            mainLock.lock();
            try {
                this.currentThread = new Thread(this);
                this.currentThread.setDaemon(true);
                this.currentThread.setName("subscriber-thread");
                this.currentThread.start();
                mainBlocker.await(this.timeout,
                                  this.timeUnit == null ? TimeUnit.SECONDS : this.timeUnit);
            } catch (InterruptedException e) {

            } finally {
                this.shutdown();
                mainLock.unlock();
            }
        } else {
            this.currentThread = new Thread(this);
            this.currentThread.setDaemon(true);
            this.currentThread.start();
        }
    }

    public void shutdown() {
        this.currentThread.interrupt();
    }

    private void genericSubscribe(MessageContext context,
                                  IHandlerChain chain) {
        AsyncEventLoop eventLoop = new AsyncEventLoop();
        eventLoop.setChain(chain);
        eventLoop.setContext(context);
        context.setAsyncEventLoop(eventLoop);

        if (chain instanceof MessageCarryHandlerChain) {
            eventLoop.startEventLoop();
        } else {
            throw new RuntimeException("the type of chain's instance is not MessageCarryHandlerChain");
        }
    }

}
