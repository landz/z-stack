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

package org.xnio.nativeimpl;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class Native {

    static {
        try {
            System.loadLibrary("xnio");
        } catch (Error error) {
          System.out.println("XNIO native not available on this platform (%s)");
            throw error;
        }
        final int[] constants = init();
    }

    private Native() {}

    private static native int[] init();

    // POSIX-ish

    /**
     * Call the UNIX dup2 system call.
     *
     * @param oldFd the old FD
     * @param newFd the new FD
     * @return the result
     */
    public static native int dup2(int oldFd, int newFd);

    /**
     * Call the UNIX dup system call.
     *
     * @param oldFd the old FD
     * @return the new FD or error
     */
    public static native int dup(int oldFd);

    /**
     * Close the FD.
     *
     * @param fd the FD to close
     * @return 0 for okay, a negative error code for error
     */
    public static native int close(int fd);

    /**
     * Get a string for an error code.
     *
     * @param err a negative or positive error code
     * @return the string
     */
    public static native String strError(int err);

    /**
     * Shut down a socket.
     *
     * @param fd the socket FD
     * @param read {@code true} to shut down reads
     * @param write {@code true} to shut down writes
     * @return 0 for okay, a negative error code for error
     */
    public static native int shutdown(int fd, boolean read, boolean write);

    public static native int await2(final int fd, final boolean writes);

    public static native int await3(final int fd, final boolean writes, final long millis);

    public static native byte[] getSockName(final int fd);

    public static native byte[] getPeerName(final int fd);

    // read

    public static native long readLong(final int fd);

    public static native int readD(final int fd, ByteBuffer b1, int p1, int l1);

    public static native long readDD(final int fd, ByteBuffer b1, int p1, int l1, ByteBuffer b2, int p2, int l2);

    public static native long readDDD(final int fd, ByteBuffer b1, int p1, int l1, ByteBuffer b2, int p2, int l2, ByteBuffer b3, int p3, int l3);

    public static native int readH(final int fd, byte[] b1, int p1, int l1);

    public static native long readHH(final int fd, byte[] b1, int p1, int l1, byte[] b2, int p2, int l2);

    public static native long readHHH(final int fd, byte[] b1, int p1, int l1, byte[] b2, int p2, int l2, byte[] b3, int p3, int l3);

    // slower
    public static native long readMisc(final int fd, ByteBuffer[] buffers, int offs, int len);



    // write

    public static native int writeLong(final int fd, final long value);

    public static native int writeD(final int fd, ByteBuffer b1, int p1, int l1);

    public static native long writeDD(final int fd, ByteBuffer b1, int p1, int l1, ByteBuffer b2, int p2, int l2);

    public static native long writeDDD(final int fd, ByteBuffer b1, int p1, int l1, ByteBuffer b2, int p2, int l2, ByteBuffer b3, int p3, int l3);

    public static native int writeH(final int fd, byte[] b1, int p1, int l1);

    public static native long writeHH(final int fd, byte[] b1, int p1, int l1, byte[] b2, int p2, int l2);

    public static native long writeHHH(final int fd, byte[] b1, int p1, int l1, byte[] b2, int p2, int l2, byte[] b3, int p3, int l3);

    // slower
    public static native long writeMisc(final int fd, ByteBuffer[] buffers, int offs, int len);

    public static native int flushTcpCork(final int fd);


    // receive

    public static native int recvDirect(final int fd, ByteBuffer buffer, byte[] srcAddr, byte[] destAddr);

    public static native int recvHeap(final int fd, byte[] bytes, int offs, int len, int pos, int lim, byte[] srcAddr, byte[] destAddr);

    public static native int recvMisc(final int fd, ByteBuffer[] buffers, int offs, int len, byte[] srcAddr, byte[] destAddr);

    // send

    public static native int sendDirect(final int fd, ByteBuffer buffer, int[] posAndLimit, byte[] destAddr);

    public static native int sendHeap(final int fd, byte[] bytes, int offs, int len, byte[] destAddr);

    public static native int sendMisc(final int fd, ByteBuffer[] buffers, int offs, int len, byte[] destAddr);

    // transfer

    public static native long xferHeap(final int srcFd, byte[] bytes, int[] posAndLimit, int destFd);

    public static native long xferDirect(final int srcFd, ByteBuffer buffer, int[] posAndLimit, int destFd);

    public static native long sendfile(final int dest, FileChannel src, long offset, long length);

    // util

    public static native int socketPair(int[] fds);

    public static native int socketTcp();

    public static native int socketTcp6();

    public static native int socketUdp();

    public static native int socketUdp6();

    public static native int socketLocalStream();

    public static native int socketLocalDatagram();

    public static native int pipe(int[] fds);

    public static native int bind(int fd, byte[] address);

    public static native int accept(int fd);

    public static native int connect(int fd, byte[] peerAddress);

    public static native int listen(int fd, int backlog);

    public static native int finishConnect(int fd);

    // options

    public static native int getOptBroadcast(int fd);

    public static native int setOptBroadcast(int fd, boolean enabled);

    public static native int getOptDontRoute(int fd);

    public static native int setOptDontRoute(int fd, boolean enabled);

    public static native int getOptKeepAlive(int fd);

    public static native int setOptKeepAlive(int fd, boolean enabled);

    public static native int getOptCloseAbort(int fd);

    public static native int setOptCloseAbort(int fd, boolean enabled);

    public static native int getOptOobInline(int fd);

    public static native int setOptOobInline(int fd, boolean enabled);

    public static native int getOptReceiveBuffer(int fd);

    public static native int setOptReceiveBuffer(int fd, int size);

    public static native int getOptReuseAddr(int fd);

    public static native int setOptReuseAddr(int fd, boolean enabled);

    public static native int getOptSendBuffer(int fd);

    public static native int setOptSendBuffer(int fd, int size);

    public static native int getOptDeferAccept(int fd);

    public static native int setOptDeferAccept(int fd, boolean enabled);

    public static native int getOptMaxSegSize(int fd);

    public static native int setOptMaxSegSize(int fd, int size);

    public static native int getOptTcpNoDelay(int fd);

    public static native int setOptTcpNoDelay(int fd, boolean enabled);

    public static native int getOptMulticastTtl(int fd);

    public static native int setOptMulticastTtl(int fd, boolean enabled);

    // linux

    public static final int EPOLL_FLAG_READ    = 0b0000_0001;
    public static final int EPOLL_FLAG_WRITE   = 0b0000_0010;
    public static final int EPOLL_FLAG_EDGE    = 0b0000_0100;

    public static native int eventFD();

    public static native int epollCreate();

    public static native int epollWait(final int efd, long[] events, int timeout);

    public static native int epollCtlAdd(final int efd, final int fd, final int flags, final int id);

    public static native int epollCtlMod(final int efd, final int fd, final int flags, final int id);

    public static native int epollCtlDel(final int efd, final int fd);

    public static native int createTimer(final int seconds, final int nanos);

    public static native long spliceToFile(int src, FileChannel dest, long destOffs, long length);

    public static native long transfer(final int srcFd, final long count, final ByteBuffer throughBuffer, final int destFd);

    public static native long tee(int src, int dest, long length, boolean more);

    public static native int readTimer(final int fd);

    // utilities

    public static IOException exceptionFor(int err) {
        String msg = strError(err);
        return new IOException(msg);
    }


    @SuppressWarnings({ "deprecation" })
    public static SocketAddress getSocketAddress(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        switch (bytes[0]) {
            case 0: {
                // inet 4
                try {
                    return new InetSocketAddress(InetAddress.getByAddress(Arrays.copyOfRange(bytes, 1, 5)), (((bytes[5]) & 0xff) << 8) | ((bytes[6]) & 0xff));
                } catch (UnknownHostException e) {
                    // ???
                    return null;
                }
            }
            case 1: {
                // inet 6
                try {
                    return new InetSocketAddress(
                            Inet6Address.getByAddress(
                                    null,
                                    Arrays.copyOfRange(bytes, 1, 17),
                                    (bytes[19] & 0xff) << 24 | (bytes[20] & 0xff) << 16 | (bytes[21] & 0xff) << 8 | (bytes[22] & 0xff)
                            ),
                            (((bytes[17]) & 0xff) << 8) | ((bytes[18]) & 0xff));
                } catch (UnknownHostException e) {
                    // ???
                    return null;
                }
            }
            case 2: {
                // local
                int len = bytes[1] & 0xff;
                return new LocalSocketAddress(new String(bytes, 0, 2, len - 2));
            }
            default: {
                return null;
            }
        }
    }

    private static final byte[] INVALID_ADDR = { (byte) 0xff };

    public static byte[] encodeSocketAddress(SocketAddress src) {
        if (src == null) {
            return INVALID_ADDR;
        }
        if (src instanceof InetSocketAddress) {
            final InetSocketAddress inet = (InetSocketAddress) src;
            final int port = inet.getPort();
            final InetAddress address = inet.getAddress();
            if (address instanceof Inet4Address) {
                final Inet4Address inet4 = (Inet4Address) address;
                final byte[] bytes = inet4.getAddress();
                final byte[] result = new byte[7];
                result[0] = 0;
                System.arraycopy(bytes, 0, result, 1, 4);
                result[5] = (byte) (port >> 8);
                result[6] = (byte) port;
                return result;
            } else if (address instanceof Inet6Address) {
                final Inet6Address inet6 = (Inet6Address) address;
                final byte[] bytes = inet6.getAddress();
                final byte[] result = new byte[23];
                result[0] = 1;
                System.arraycopy(bytes, 0, result, 1, 16);
                result[17] = (byte) (port >> 8);
                result[18] = (byte) port;
                final int scopeId = inet6.getScopeId();
                result[19] = (byte) (scopeId >> 24);
                result[20] = (byte) (scopeId >> 16);
                result[21] = (byte) (scopeId >> 8);
                result[22] = (byte) scopeId;
                return result;
            } else {
                return INVALID_ADDR;
            }
        } else if (src instanceof LocalSocketAddress) {
            return encodeSocketAddress((LocalSocketAddress) src);
        } else {
            return INVALID_ADDR;
        }
    }

//    public static byte[] encodeSocketAddress(LocalSocketAddress local) {
//        final String name = local.getName();
//        final byte[] result = new byte[2 + UNIX_PATH_LEN];
//        result[0] = 2;
//        if (! encodeTo(name, result, 1)) {
//            return INVALID_ADDR;
//        }
//        return result;
//    }

    public static boolean encodeTo(String src, byte[] dest, int offs) {
        final int srcLen = src.length();
        try {
            for (int i = 0; i < srcLen; i = src.offsetByCodePoints(i, 1)) {
                int cp = src.codePointAt(i);
                if (cp > 0 && cp <= 0x7f) {
                    // don't accidentally null-terminate the string
                    dest[offs ++] = (byte) cp;
                } else if (cp <= 0x07ff) {
                    dest[offs ++] = (byte)(0xc0 | 0x1f & cp >> 6);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp);
                } else if (cp <= 0xffff) {
                    dest[offs ++] = (byte)(0xe0 | 0x0f & cp >> 12);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp >> 6);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp);
                } else if (cp <= 0x1fffff) {
                    dest[offs ++] = (byte)(0xf0 | 0x07 & cp >> 18);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp >> 12);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp >> 6);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp);
                } else if (cp <= 0x3ffffff) {
                    dest[offs ++] = (byte)(0xf8 | 0x03 & cp >> 24);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp >> 18);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp >> 12);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp >> 6);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp);
                } else if (cp >= 0) {
                    dest[offs ++] = (byte)(0xfc | 0x01 & cp >> 30);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp >> 24);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp >> 18);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp >> 12);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp >> 6);
                    dest[offs ++] = (byte)(0x80 | 0x3f & cp);
                } else {
                    return false;
                }
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

}

