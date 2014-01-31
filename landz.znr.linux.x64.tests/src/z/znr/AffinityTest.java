/**
 * Copyright 2013, Landz and its contributors. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package z.znr;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import z.znr.Affinity;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static z.util.MethodHandles.*;
import static z.util.Throwables.*;
import static z.znr.Affinity.*;

/**
 * this test should be run in fork mode in method level
 *
 */
public class AffinityTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testTopology() {
    MethodHandle mh_nSockets_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nSockets", int.class)
    );
    MethodHandle mh_nPhysicalCores_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nPhysicalCores", int.class)
    );
    MethodHandle mh_nPhysicalCoresPerSocket_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nPhysicalCoresPerSocket", int.class)
    );
    MethodHandle mh_nVirtualCores_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nVirtualCores", int.class)
    );
    MethodHandle mh_nVirtualCoresPerPhysicalCore_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nVirtualCoresPerPhysicalCore", int.class)
    );

    MethodHandle mh_socketId_setter = uncheckTo(()->
        LOOKUP.findSetter(Topology.class, "socketId", int.class)
    );

    MethodHandle ctorTopology = uncheckTo(()->
        LOOKUP.findConstructor(Topology.class, MethodType.methodType(void.class))
    );

    Topology top = uncheckTo(() -> (Topology)ctorTopology.invokeExact());


    //assume i7-3820QM  1S*4PC/8VC
    uncheck(() -> mh_nSockets_setter.invoke(1));
    uncheck(() -> mh_nPhysicalCores_setter.invoke(4));
    uncheck(() -> mh_nPhysicalCoresPerSocket_setter.invoke(4));
    uncheck(() -> mh_nVirtualCores_setter.invoke(8));
    uncheck(() -> mh_nVirtualCoresPerPhysicalCore_setter.invoke(2));

    Affinity.printTopology();

    uncheck(() -> mh_socketId_setter.invoke(top, 0));
    long affinityMask =
        top.physicalCore(0).virtualCore(0).getAffinityMask();
    assertThat(affinityMask, is(0b00000000_00000000_00000000_00000001L));

    uncheck(() -> mh_socketId_setter.invoke(top, 0));
    affinityMask =
        top.physicalCore(1).virtualCore(0).getAffinityMask();
    assertThat(affinityMask, is(0b00000000_00000000_00000000_00000010L));

    uncheck(() -> mh_socketId_setter.invoke(top,0));
    affinityMask =
        top.physicalCore(3).virtualCore(0).getAffinityMask();
    assertThat(affinityMask, is(0b00000000_00000000_00000000_00001000L));

    uncheck(() -> mh_socketId_setter.invoke(top,0));
    affinityMask =
        top.physicalCore(0).virtualCore(1).getAffinityMask();
    assertThat(affinityMask, is(0b00000000_00000000_00000000_00010000L));

    uncheck(() -> mh_socketId_setter.invoke(top,0));
    affinityMask =
        top.physicalCore(1).virtualCore(1).getAffinityMask();
    assertThat(affinityMask, is(0b00000000_00000000_00000000_00100000L));

    uncheck(() -> mh_socketId_setter.invoke(top,0));
    affinityMask =
        top.physicalCore(2).virtualCore(1).getAffinityMask();
    assertThat(affinityMask, is(0b00000000_00000000_00000000_01000000L));

    uncheck(() -> mh_socketId_setter.invoke(top,0));
    affinityMask =
        top.physicalCore(3).virtualCore(1).getAffinityMask();
    assertThat(affinityMask, is(0b00000000_00000000_00000000_10000000L));

    System.out.println("===============================================");
    //assume
    //Dual Xeon Sandy Bridge-EP E5-2650 2S*8PC/16VC
    uncheck(() -> mh_nSockets_setter.invoke(2));
    uncheck(() -> mh_nPhysicalCores_setter.invoke(8*2));
    uncheck(() -> mh_nPhysicalCoresPerSocket_setter.invoke(8));
    uncheck(() -> mh_nVirtualCores_setter.invoke(16*2));
    uncheck(() -> mh_nVirtualCoresPerPhysicalCore_setter.invoke(2));

    Affinity.printTopology();

    uncheck(() -> mh_socketId_setter.invoke(top,1));
    affinityMask =
        top.physicalCore(7).virtualCore(1).getAffinityMask();
    assertThat(affinityMask, is(0b10000000_00000000_00000000_00000000L));

    System.out.println("===============================================");
    //ref:http://www.richweb.com/cpu_info
    //Dual Single core CPU, Hyperthreading ENABLED
    /*
    processor	: 0
    physical id	: 0
    core id		: 0

    processor	: 1
    physical id	: 3
    core id		: 0

    processor	: 2
    physical id	: 0
    core id		: 0

    processor	: 3
    physical id	: 3
    core id		: 0
    */
    uncheck(() -> mh_nSockets_setter.invoke(2));
    uncheck(() -> mh_nPhysicalCores_setter.invoke(1*2));
    uncheck(() -> mh_nPhysicalCoresPerSocket_setter.invoke(1));
    uncheck(() -> mh_nVirtualCores_setter.invoke(2*2));
    uncheck(() -> mh_nVirtualCoresPerPhysicalCore_setter.invoke(2));

    Affinity.printTopology();

    uncheck(() -> mh_socketId_setter.invoke(top,1));
    affinityMask =
        top.physicalCore(0).virtualCore(0).getAffinityMask();
    assertThat(affinityMask, is(0b00000000_00000000_00000000_00000010L));

    uncheck(() -> mh_socketId_setter.invoke(top,1));
    affinityMask =
        top.physicalCore(0).virtualCore(1).getAffinityMask();
    assertThat(affinityMask, is(0b00000000_00000000_00000000_00001000L));

    uncheck(() -> mh_socketId_setter.invoke(top,0));
    affinityMask =
        top.physicalCore(0).virtualCore(1).getAffinityMask();
    assertThat(affinityMask, is(0b00000000_00000000_00000000_00000100L));

  }

  @Test
  public void  testTopologyException0() {
    MethodHandle mh_nSockets_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nSockets", int.class)
    );
    MethodHandle mh_nPhysicalCores_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nPhysicalCores", int.class)
    );
    MethodHandle mh_nPhysicalCoresPerSocket_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nPhysicalCoresPerSocket", int.class)
    );
    MethodHandle mh_nVirtualCores_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nVirtualCores", int.class)
    );
    MethodHandle mh_nVirtualCoresPerPhysicalCore_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nVirtualCoresPerPhysicalCore", int.class)
    );

    MethodHandle mh_socketId_setter = uncheckTo(()->
        LOOKUP.findSetter(Topology.class, "socketId", int.class)
    );

    MethodHandle ctorTopology = uncheckTo(()->
        LOOKUP.findConstructor(Topology.class, MethodType.methodType(void.class))
    );

    Topology top = uncheckTo(() -> (Topology)ctorTopology.invokeExact());


    //assume i7-3820QM  1S*4PC/8VC
    uncheck(() -> mh_nSockets_setter.invoke(1));
    uncheck(() -> mh_nPhysicalCores_setter.invoke(4));
    uncheck(() -> mh_nPhysicalCoresPerSocket_setter.invoke(4));
    uncheck(() -> mh_nVirtualCores_setter.invoke(8));
    uncheck(() -> mh_nVirtualCoresPerPhysicalCore_setter.invoke(2));


    uncheck(() -> mh_socketId_setter.invoke(top,0));
    exception.expect(RuntimeException.class);
    long affinityMask =
        top.physicalCore(3).virtualCore(2).getAffinityMask();

  }

  @Test
  public void  testTopologyException1() {
    MethodHandle mh_nSockets_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nSockets", int.class)
    );
    MethodHandle mh_nPhysicalCores_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nPhysicalCores", int.class)
    );
    MethodHandle mh_nPhysicalCoresPerSocket_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nPhysicalCoresPerSocket", int.class)
    );
    MethodHandle mh_nVirtualCores_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nVirtualCores", int.class)
    );
    MethodHandle mh_nVirtualCoresPerPhysicalCore_setter = uncheckTo(()->
        LOOKUP.findStaticSetter(Affinity.class, "nVirtualCoresPerPhysicalCore", int.class)
    );

    MethodHandle mh_socketId_setter = uncheckTo(()->
        LOOKUP.findSetter(Topology.class, "socketId", int.class)
    );

    MethodHandle ctorTopology = uncheckTo(()->
        LOOKUP.findConstructor(Topology.class, MethodType.methodType(void.class))
    );

    Topology top = uncheckTo(() -> (Topology)ctorTopology.invokeExact());


    //assume i7-3820QM  1S*4PC/8VC
    uncheck(() -> mh_nSockets_setter.invoke(1));
    uncheck(() -> mh_nPhysicalCores_setter.invoke(4));
    uncheck(() -> mh_nPhysicalCoresPerSocket_setter.invoke(4));
    uncheck(() -> mh_nVirtualCores_setter.invoke(8));
    uncheck(() -> mh_nVirtualCoresPerPhysicalCore_setter.invoke(2));

    exception.expect(RuntimeException.class);
    uncheck(() -> mh_socketId_setter.invoke(top,0));
    long affinityMask =
        top.physicalCore(4).virtualCore(0).getAffinityMask();

  }


  private volatile boolean suc1 = false;
  private volatile boolean suc2 = false;
  private volatile double a     = 0;
  @Test
  public void testBindto() {
    long RUNS = 1000L;//Long.MAX_VALUE;
    long SLEEP_TIME = 1000L;//1000_000L;
    Thread t1 = new Thread(()->{
      suc1 = bindTo(Topology.socket(0).physicalCore(1).virtualCore(0));//#1,cpu2
      for (long i = 0; i < RUNS; i++) {
        a =
            Math.ceil(Math.asin(Math.abs(Math.sqrt(Math.exp(RUNS)+1))));
      }
    });
    Thread t2 = new Thread(()->{
      suc2 = bindTo(Topology.socket(0).physicalCore(3).virtualCore(1));//#7,cpu8
      for (long i = 0; i < RUNS; i++) {
        a =
            Math.ceil(Math.asin(Math.abs(Math.sqrt(Math.exp(RUNS)+2))));
      }
    });
    t1.start();
    t2.start();
    uncheck(()->Thread.sleep(1000L));
    assertThat(suc1,is(true));
    assertThat(suc2,is(true));

    uncheck(()->Thread.sleep(SLEEP_TIME));
  }
}
