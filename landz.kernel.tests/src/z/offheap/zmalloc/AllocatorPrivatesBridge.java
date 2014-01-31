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

package z.offheap.zmalloc;

import z.offheap.zmalloc.Allocator;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static z.util.MethodHandles.LOOKUP;
import static z.util.Throwables.uncheck;
import static z.util.Throwables.uncheckTo;

/**
 * wrap the private members of Allocator for testing
 *
 */
public class AllocatorPrivatesBridge {

  public static final long SIZE_ZMPAGE           = SIZE_ZMPAGE();
//  public static final int ZMPAGE_RAWCHUNK_OFFSET = SIZE_CACHE_LINE;

  //============================================================================
  public static int sizeClassIndex(int sizeOfBytes) {
    MethodHandle mh = uncheckTo(() ->
        LOOKUP.findStatic(Allocator.class, "sizeClassIndex",
            MethodType.methodType(int.class,int.class)));
    return (int)uncheckTo(()-> mh.invoke(sizeOfBytes));
  }

  public static long gp_Page_poll() {
    MethodHandle mh = uncheckTo(() ->
        LOOKUP.findStatic(Allocator.class, "gp_Page_poll",
            MethodType.methodType(long.class)));
    return (long)uncheckTo(()-> mh.invoke());
  }

  public static void gp_Page_offer(long addressFreePage) {
    MethodHandle mh = uncheckTo(() ->
        LOOKUP.findStatic(Allocator.class, "gp_Page_offer",
            MethodType.methodType(void.class,long.class)));
    uncheck(() -> mh.invoke(addressFreePage));
  }

  public static long get_addressGP() {
    MethodHandle mh =
        uncheckTo(() ->
            LOOKUP.findStaticGetter(
                Allocator.class,
                "addressGP",
                long.class));
    return (long)uncheckTo(()-> mh.invoke());
  }

  public static void tlp_ini(long tid) {
    MethodHandle mh = uncheckTo(() ->
        LOOKUP.findStatic(Allocator.class, "tlp_ini",
            MethodType.methodType(void.class, long.class)));

    uncheck(()-> mh.invoke(tid));
  }

  public static void tlp_AvailablePages_remove(long addrAvailablePageHead,
                                        long removedPage) {
    MethodHandle mh = uncheckTo(() ->
        LOOKUP.findStatic(Allocator.class, "tlp_AvailablePages_remove",
            MethodType.methodType(void.class,
                long.class, long.class)));

    uncheck(() -> mh.invoke(addrAvailablePageHead, removedPage));
  }

  public static void tlp_AvailablePages_addToHead(long addrAvailablePageHead,
                                           long newAvailablePage) {
    MethodHandle mh = uncheckTo(() ->
        LOOKUP.findStatic(Allocator.class, "tlp_AvailablePages_addToHead",
            MethodType.methodType(void.class,
                long.class, long.class)));

    uncheck(()-> mh.invoke(addrAvailablePageHead,newAvailablePage));
  }

  public static void tlp_AvailablePages_addToTail(long tid,
                                           int sizeClassIndex,
                                           long newAvailablePage) {
    MethodHandle mh = uncheckTo(() ->
        LOOKUP.findStatic(Allocator.class, "tlp_AvailablePages_addToTail",
            MethodType.methodType(void.class,
                long.class, int.class, long.class)));

    uncheck(()-> mh.invoke(tid,sizeClassIndex,newAvailablePage));
  }

  public static void tlp_RemoteFreedChunksHead_add(long addrRemoteFreedChunksTail,
                                            long addrRemoteFreedChunk) {
    MethodHandle mh = uncheckTo(() ->
        LOOKUP.findStatic(Allocator.class, "tlp_RemoteFreedChunksHead_add",
            MethodType.methodType(void.class,
                long.class, long.class)));

    uncheck(()-> mh.invoke(addrRemoteFreedChunksTail,addrRemoteFreedChunk));
  }

  public static long tlp_RemoteFreedChunksHead_remove(long addrRemoteFreedChunksHead,
                                               long addrRemoteFreedChunksTail,
                                               long addrRemoteFreedChunksDummy) {
    MethodHandle mh = uncheckTo(() ->
        LOOKUP.findStatic(Allocator.class, "tlp_RemoteFreedChunksHead_remove",
            MethodType.methodType(long.class,
                long.class,long.class,long.class))
    );

    return (long)uncheckTo(()-> mh.invoke(addrRemoteFreedChunksHead,
        addrRemoteFreedChunksTail,
        addrRemoteFreedChunksDummy));
  }


//  public static void tlp_FreePages_push(long tid,
//                                  long newFreePage) {
//    MethodHandle mh = uncheckTo(() ->
//        LOOKUP.findStatic(Allocator.class, "tlp_FreePages_push",
//            MethodType.methodType(void.class,
//                long.class, long.class)));
//
//    uncheck(()-> mh.invoke(tid,newFreePage));
//  }
//
//  public static long tlp_FreePages_pop(long tid) {
//    MethodHandle mh = uncheckTo(() ->
//        LOOKUP.findStatic(Allocator.class, "tlp_FreePages_pop",
//            MethodType.methodType(long.class,long.class)));
//
//    return (long)uncheckTo(()-> mh.invoke(tid));
//  }

  public static void pg_setupPage(long addressPage,
                            int sizeClassIndex,
                            long tid) {
    MethodHandle mh = uncheckTo(() ->
        LOOKUP.findStatic(Allocator.class, "pg_setupPage",
            MethodType.methodType(void.class,
                long.class, int.class, long.class)));

    uncheck(()-> mh.invoke(addressPage,sizeClassIndex,tid));
  }

  public static void pg_AvailableChunks_push(long addressPage,
                                       long addressChunk) {
    MethodHandle mh = uncheckTo(() ->
        LOOKUP.findStatic(Allocator.class, "pg_AvailableChunks_push",
            MethodType.methodType(void.class,
                long.class, long.class)));

    uncheck(()-> mh.invoke(addressPage,addressChunk));
  }

  public static long pg_AvailableChunks_pop(long addressPage) {
    MethodHandle mh = uncheckTo(() ->
        LOOKUP.findStatic(Allocator.class, "pg_AvailableChunks_pop",
            MethodType.methodType(long.class,
                long.class)));

    return (long)uncheckTo(()-> mh.invoke(addressPage));
  }



  public static long addressTLPs() {
    MethodHandle mh =
        uncheckTo(() ->
            LOOKUP.findStaticGetter(
                Allocator.class,
                "addressTLPs",
                long.class));
    return (long)uncheckTo(()-> mh.invoke());
  }

  public static long totalAvailablepages() {
    MethodHandle mh =
        uncheckTo(() ->
            LOOKUP.findStaticGetter(
                Allocator.class,
                "totalAvailablepages",
                long.class));
    return (long)uncheckTo(()-> mh.invoke());
  }

  public static int TLP_ITEM() {
    MethodHandle mh =
        uncheckTo(() ->
            LOOKUP.findStaticGetter(
                Allocator.class,
                "TLP_ITEM_SIZE",
                int.class));
    return (int)uncheckTo(()-> mh.invoke());
  }

  public static int TLP_AVAILABLEPAGES_OFFSET() {
    MethodHandle mh =
        uncheckTo(() ->
            LOOKUP.findStaticGetter(
                Allocator.class,
                "TLP_AVAILABLEPAGES_OFFSET",
                int.class));
    return (int)uncheckTo(()-> mh.invoke());
  }

  public static int TLP_REMOTEFREEDCHUNKS_HEAD_OFFSET() {
    MethodHandle mh =
        uncheckTo(() ->
            LOOKUP.findStaticGetter(
                Allocator.class,
                "TLP_REMOTEFREEDCHUNKS_HEAD_OFFSET",
                int.class));
    return (int)uncheckTo(()-> mh.invoke());
  }


  public static int TLP_REMOTEFREEDCHUNKS_TAIL_OFFSET() {
    MethodHandle mh =
        uncheckTo(() ->
            LOOKUP.findStaticGetter(
                Allocator.class,
                "TLP_REMOTEFREEDCHUNKS_TAIL_OFFSET",
                int.class));
    return (int)uncheckTo(()-> mh.invoke());
  }

  public static int TLP_REMOTEFREEDCHUNKS_DUMMY_OFFSET() {
    MethodHandle mh =
        uncheckTo(() ->
            LOOKUP.findStaticGetter(
                Allocator.class,
                "TLP_REMOTEFREEDCHUNKS_DUMMY_OFFSET",
                int.class));
    return (int)uncheckTo(()-> mh.invoke());
  }


  public static int SIZE_TLP_AVAILABLEPAGES_ITEM() {
    MethodHandle mh =
        uncheckTo(() ->
            LOOKUP.findStaticGetter(
                Allocator.class,
                "TLP_AVAILABLEPAGES_ITEM_SIZE",
                int.class));
    return (int)uncheckTo(()-> mh.invoke());
  }

  public static int ZMPAGE_MAX_CHUNK_SIZE() {
    MethodHandle mh =
        uncheckTo(() ->
            LOOKUP.findStaticGetter(
                Allocator.class,
                "ZMPAGE_MAX_CHUNK_SIZE",
                int.class));
    return (int)uncheckTo(()-> mh.invoke());
  }

  public static int ZMPAGE_TID_OFFSET() {
    MethodHandle mh =
        uncheckTo(() ->
            LOOKUP.findStaticGetter(
                Allocator.class,
                "ZMPAGE_TID_OFFSET",
                int.class));
    return (int)uncheckTo(()-> mh.invoke());
  }

  public static long SIZE_ZMPAGE() {
    MethodHandle mh =
        uncheckTo(() ->
            LOOKUP.findStaticGetter(
                Allocator.class,
                "SIZE_ZMPAGE",
                long.class));
    return (long)uncheckTo(()-> mh.invoke());
  }

}
