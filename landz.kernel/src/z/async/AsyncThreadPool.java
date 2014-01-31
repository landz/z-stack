///**
// * Copyright 2013, Landz and its contributors. All rights reserved.
// *
// *    Licensed under the Apache BuiltinLicense, Version 2.0 (the "BuiltinLicense");
// *    you may not use this file except in compliance with the BuiltinLicense.
// *    You may obtain a copy of the BuiltinLicense at
// *
// *        http://www.apache.org/licenses/LICENSE-2.0
// *
// *    Unless required by applicable law or agreed to in writing, software
// *    distributed under the BuiltinLicense is distributed on an "AS IS" BASIS,
// *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *    See the BuiltinLicense for the specific language governing permissions and
// *    limitations under the BuiltinLicense.
// */
//
//package z.async;
//
//import z.channel.GenericHyperLoop;
//import z.channel.GenericMPMCQueue;
//import z.channel.LongHyperLoop;
//import z.channel.ReceivePort;
//
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.stream.IntStream;
//
//import static z.util.Contracts.contract;
//import static z.util.Unsafes.*;
//
///**
// * AsyncThreadPool, which holds a pool of {@link Thread}s, is used to schedule
// * {@link Asyncor}s' execution.
// *
// * Concepts:
// * Boss - ?
// * Worker - ?
// *
// */
//public class AsyncThreadPool {
//
//  public static final int DEFAULT_POOL_SIZE =
//      Runtime.getRuntime().availableProcessors()-1;//FIXME
//
//  public final int poolSize;
//
//  private final AsyncWorkerThread[] workers;
//
////  private final GenericHyperLoop<Asyncor>[] asyncorLoops;
////
////  private final GenericHyperLoop<Message>[] messageLoops;
////  //primitive specialization of messageLoops
////  private final LongHyperLoop[] primitiveMessageLoops;
//
////  private int robinIndex;
////  public final int robinIndexMask;
//
//  AsyncThreadPool() {
//    this(DEFAULT_POOL_SIZE);
//  }
//
//  AsyncThreadPool(int poolSize) {
//    contract(()->poolSize>0);//TODO
//    this.poolSize              = poolSize;
//    this.workers = new AsyncWorkerThread[poolSize];
////    this.asyncorLoops          = new GenericHyperLoop[poolSize];
////    this.messageLoops          = new GenericHyperLoop[poolSize];
////    this.primitiveMessageLoops = new LongHyperLoop[poolSize];
//
//    IntStream.range(0,poolSize)
//        .forEach(i -> workers[i] = new AsyncWorkerThread(i));
//
//    IntStream.range(0,poolSize)
//        .forEach(i -> workers[i].createInitialOutPorts());
//  }
//
//
////  public AsyncThreadPool(int poolSize, ThreadFactory threadFactory) {
////
////  }
//
//  //TODO or assign roundrobin?
//  private final ThreadLocalRandom rnd = ThreadLocalRandom.current();
//
//  /**
//   * TODO
//   * note: the queue is bounded, so this async may be blocked
//   * when the work queue can not accept the asyncors
//   *
//   */
//  public final void invoke(Asyncor asyncor) {
//    while (!workers[rnd.nextInt(0,poolSize)].asyncorQueue.offer(asyncor)){
//      Thread.yield();
//    }
//  }
//
//  public final boolean tryInvoke(Asyncor asyncor) {
//    //throw new RejectedExecutionException("Queue capacity exceeded");
//    return workers[rnd.nextInt(0,poolSize)].asyncorQueue.offer(asyncor);
//  }
//
//  public final void start() {
//    for (Thread t : workers) {
//      t.start();
//    }
//  }
//
//  /**
//   * AsyncWorkerThread, provide the worker thread implementation for AsyncPool.
//   *
//   */
//  class AsyncWorkerThread extends Thread {
//    //TODO: add a config option
//    private static final int HYPERLOOP_SIZE_DEFAULT = 1024;//TODO
//    private final int id;
//
//    private final GenericHyperLoop<Message> messageLoop;
//    private final ReceivePort<Message>[] messagePorts;
//
//    final GenericMPMCQueue<Asyncor> asyncorQueue;
////    private int pMsgReadIndex = 0;
////    private int pMsgWriteIndex = 0;
//
//
//    AsyncWorkerThread(int id) {
//      super("AsyncWorkerThread#"+id);
//
//      this.id = id;
//
//      this.messageLoop = new GenericHyperLoop(HYPERLOOP_SIZE_DEFAULT);
////      messageLoops[id] = this.messageLoop;
//
//      this.primitiveMessageLoop = new LongHyperLoop(HYPERLOOP_SIZE_DEFAULT);
////      primitiveMessageLoops[id] = this.primitiveMessageLoop;
//
//      this.messagePorts          = new GenericHyperLoop.OutPort[poolSize];
//      this.primitiveMessagePorts = new LongHyperLoop.OutPort[poolSize];
//
//      this.asyncorQueue = new GenericMPMCQueue(1024*128);
//    }
//
//    void createInitialOutPorts() {
//      contract(() -> workers[0] != null);
//
//      IntStream.range(0,poolSize)
//          .filter(i-> i!=id)
//          .forEach(i->
//              this.messagePorts[i] = workers[i].messageLoop.createReceivePort());
//      IntStream.range(0,poolSize)
//          .filter(i-> i!=id)
//          .forEach(i->
//              this.primitiveMessagePorts[i] =
//                  workers[i].primitiveMessageLoop.createOutPort());
//    }
//
//    /**
//     * main run loop of AsyncWorker
//     */
//    public void run() {
//      try {
//        for(;;) {
//          updateMessages();
//          runAsyncor();
//        }
//      } catch (Throwable ex) {
//        ex.printStackTrace();//FIXME
//      }
//    }
//
//    protected void runAsyncor() {
//      //start to work
//      Asyncor asyncor = asyncorQueue.poll();
//      if (asyncor!=null) {
//        asyncor.body.run();
//      } else {
//        //TODO scan and rob
//        UNSAFE.park(false,10L);//FIXME
//      }
//    }
//
//    protected void updateMessages() {
//      for (int i = 0; i < poolSize; i++) {
//        if (i==id) continue;
//        ReceivePort<Message> messagePort
//            = messagePorts[i];
//        if (messagePort.isReceivable()) {
//          Message msg = messagePort.tryReceive();
//          //FIXME
//        }
//      }
//      for (int i = 0; i < poolSize; i++) {
//        if (i==id) continue;
//        LongHyperLoop.OutPort primitiveMessagePort
//            = primitiveMessagePorts[i];
//        if (primitiveMessagePort.isReceivable()) {
//          long v = primitiveMessagePort.receive();
//          //FIXME
////              primitiveMessages[pMsgWriteIndex++] = v;
//        }
//      }
//    }
//  }//end AsyncWorkerThread
//
//
////  class PrimaryAsyncWorkerThread extends AsyncWorkerThread {
////
////    private final GenericHyperLoop<Asyncor>.OutPort<Asyncor> asyncorPoolPort;
////
////    PrimaryAsyncWorkerThread(
////        int id,
////        GenericHyperLoop<Asyncor>.OutPort<Asyncor> asyncorPoolPort) {
////      super(id);
////
////      this.asyncorPoolPort = asyncorPoolPort;
////
////    }
////
////    @Override
////    protected void runAsyncor() {
////      //start to work
////      Asyncor async = asyncorQueue.poll();
////      if (async==null) {
////        if (asyncorPoolPort.isReceivable()) {
////          async = asyncorPoolPort.receive();
////          asyncorQueue.offer(async);
////        }
////
////      }
////
////      //TODO scan...
////      UNSAFE.park(false,10L);//FIXME
////
////      async.body.run();
////    }
////  }//end PrimaryAsyncWorkerThread
//
//}
