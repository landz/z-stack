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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static z.util.Throwables.uncheckTo;
import static z.znr.MethodHandles.asm;

/**
 * {@link Affinity} is used to support bind your java thread to some
 * hardware presentations, that is Socket/PhysicalCore/VirtualCore. <p>
 * <p>
 * Concept explained:
 * <p>  Socket - physical packages which mounted on your machine
 * <p>  PhysicalCore - one package/die may have several physical cores
 * <p>  VirtualCore - if your physical core support hyperthreading technology,
 *                    then you will have more than one virtual cores on a
 *                    physical core.
 * <p> There is a 1-1 mapping between the above concepts
 *     to Linux's /proc/cpuinfo:
 * <p> Socket       -> "physical id"
 * <p> PhysicalCore -> "core id"
 * <p> VirtualCore  -> "processor"
 * <p> But, we think the above concepts are more clear for the massive.<p>
 * Then, the concept "Id" is used to index into these hardware presentations.
 * <p>
 * A DSL in {@link Affinity.Topology} has been provided to work with
 * above concepts. <p>
 * <p>
 * Note:
 * <p>1. now it only supports that the thread which setting the affinity itself
 *   should be the current running thread;
 * <p>2. this class intentionally only supports the Linux/x86-64 platform.
 * <p>3. all Id indexings are 0-based and kept the below invariants:
 *   <p> 0<= socketId       < number of Sockets
 *   <p> 0<= physicalCoreId < number of PhysicalCores per Socket
 *   <p> 0<= virtualCoreId  < number of VirtualCores per PhysicalCore
 *
 * <p>
 *
 */
public class Affinity {

  private static int nSockets                     = 0;
  private static int nPhysicalCores               = 0;
  private static int nVirtualCores                = 0;
  private static int nPhysicalCoresPerSocket      = 0;
  private static int nVirtualCoresPerPhysicalCore = 0;


  private static void readTopology() {
    Path cpuinfoPath = Paths.get("/proc/cpuinfo");
    if (!Files.exists(cpuinfoPath))
      throw new RuntimeException(
          "/proc/cpuinfo does not exist, however this invocation is only for Linux.");

    //TODO: not closed?
    List<String> lines = uncheckTo(() -> Files.readAllLines(cpuinfoPath));
    //how bad performance fluent style:)
    nVirtualCores =
        (int)lines.stream().
            filter(s -> s.startsWith("processor")).count();
    nPhysicalCores =
        (int)lines.stream().
            filter(s -> s.startsWith("core id")).distinct().count();
    nSockets =
        (int)lines.stream().
            filter(s -> s.startsWith("physical id")).distinct().count();

    nPhysicalCoresPerSocket      = nPhysicalCores/nSockets;
    nVirtualCoresPerPhysicalCore = nVirtualCores/nPhysicalCores;
  }

  public static void printTopology() {
    ensureTopologyRead();

    System.out.printf("Your machine has %d sockets, " +
        "%d physical cores[%d physical cores per socket], " +
        "%d virtual cores[%d virtual cores per physical core].\n",
        nSockets,
        nPhysicalCores, nPhysicalCoresPerSocket,
        nVirtualCores, nVirtualCoresPerPhysicalCore);
  }

  private static void ensureTopologyRead() {
    if (nVirtualCores==0)
      readTopology();
  }

  public static int getNumberOfVirtualCores() {
    ensureTopologyRead();
    return nVirtualCores;
  }

  public static boolean bindTo(TopologyBasedAffinityMask mask) {
    return LibC.setAffinity(0, new LibC.Cpuset(mask.getAffinityMask()))
        ==0 ? true:false;
  }


  /**
   * TODO: we now only support 64 cpus(first ulong), it is easy to expand to
   *       more cpus. But, it is better to have one true machine to confirm:)<p>
   * TODO: provide exclusive and/or setAffinityMask APIs <p>
   * TODO: use landz contract API<p>
   *
   */
  public static class Topology implements
      TopologySocket,
      TopologySocketPhysicalCore,
      TopologyBasedAffinityMask {
    private int processorId    = 0;

    private int socketId       = 0;
    private int physicalCoreId = 0;
    private int virtualCoreId  = 0;

    private Topology() {};

    /**
     * Note: make sure you are indexing socket in the right range
     * @param id
     * @return
     */
    public static TopologySocket socket(int id) {
      ensureTopologyRead();

      if (id<0 || id>=nSockets)
        throw new RuntimeException(
            "the input socket id is out of measured scope");

      if (nVirtualCores>64)
        throw new RuntimeException(
            "Topology DSL does not support more than 64 CPUs machine now. " +
                "Issue this to Landz to help us to improve this for you!");

      Topology topology = new Topology();
      topology.socketId = id;
      return topology;
    }

    @Override
    public TopologySocketPhysicalCore physicalCore(int id) {
      if (id<0 || id>=nPhysicalCoresPerSocket)
        throw new RuntimeException(
            "the input physicalCore id is out of measured scope");
      this.physicalCoreId = id;
      return this;
    }

    @Override
    public TopologyBasedAffinityMask virtualCore(int id) {
      if (id<0 || id>=nVirtualCoresPerPhysicalCore)
        throw new RuntimeException(
            "the input virtualCore id is out of measured scope");
      this.virtualCoreId = id;
      return this;
    }

    /**
     * Other than using {@link #socketId} API, you can assign a processor Id
     * directly.
     * <p>
     * Note: make sure you are indexing processor in the right range
     * @param id
     * @return
     */
    public TopologyBasedAffinityMask processor(int id) {
      ensureTopologyRead();

      if (id<0 || id>=nVirtualCores)
      this.processorId = id;
      return this;
    }

    @Override
    public long getAffinityMask() {
      processorId =
        virtualCoreId*nPhysicalCoresPerSocket*nSockets+
        physicalCoreId*nSockets+
        socketId;
      return 1L<<processorId;
    }

  }

  public static interface TopologySocket {
    public TopologySocketPhysicalCore physicalCore(int id);
  }

  public static interface TopologySocketPhysicalCore {
    public TopologyBasedAffinityMask virtualCore(int id);
  }

  public static interface TopologyBasedAffinityMask {
    public long getAffinityMask();
  }
}
