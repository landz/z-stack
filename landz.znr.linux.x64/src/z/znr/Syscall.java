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

import java.lang.invoke.MethodHandle;

import static jnr.x86asm.Asm.*;
import static z.offheap.zmalloc.Allocator.allocate;
import static z.offheap.zmalloc.Allocator.free;
import static z.util.Unsafes.UNSAFE;
import static z.znr.MethodHandles.asm;

/**
 * functions for wrapping Syscall under Linux/x86-64
 */
public class Syscall {

  //===========================================================================
  //0	sys_read	unsigned int fd	char *buf	size_t count
  private static final MethodHandle mh_sys_read = asm(
      long.class, int.class, long.class, long.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(0));
        a.mov(rdi, rdx);
        a.mov(rsi, rcx);
        a.mov(rdx, r8);
        a.syscall();
        a.ret();
      }
  );

  /**
   * reads data from the open file referred to by the descriptor fd.
   *
   * @param fd - the file descriptor to be read from
   * @param address - supplies the address of the memory buffer which contains
   *                input data. Note: the size of buffer must be >=
   *                the below @param count.
   * @param count - specifies the maximum number of bytes to read.
   */
  public static final long sys_read(int fd, long address, long count) {
    try {
      return (long)mh_sys_read.invokeExact(fd,address,count);
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }


  //===========================================================================
  //1	sys_write	unsigned int fd	const char *buf	size_t count
  private static final MethodHandle mh_sys_write = asm(
      long.class, int.class, long.class, long.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(1));
        a.mov(rdi, rdx);
        a.mov(rsi, rcx);
        a.mov(rdx, r8);
        a.syscall();
        a.ret();
      }
  );

  /**
   */
  public static final long sys_write(int fd, long address, long count) {
    try {
      return (long)mh_sys_write.invokeExact(fd,address,count);
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }


  //===========================================================================
  //3	sys_close	unsigned int fd
  private static final MethodHandle mh_sys_close = asm(
      int.class, int.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(3));
        a.mov(rdi, rdx);
        a.syscall();
        a.ret();
      }
  );

  /**
   * close a file descriptor.
   * <p>
   * man:
   * http://man7.org/linux/man-pages/man2/close.2.html
   *
   * @param fd - the last file descriptor referring to the underlying
   *           open file description
   * @return  on success, 0; or on error, -errno
   */
  public static final int sys_close(int fd){
    try {
      return (int)mh_sys_close.invokeExact(fd);
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }


  //===========================================================================
  //41	sys_socket	int family	int type	int protocol
  private static final MethodHandle mh_sys_socket = asm(
      int.class, int.class, int.class, int.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(41));
        a.mov(rdi, rdx);
        a.mov(rsi, rcx);
        a.mov(rdx, r8);
        a.syscall();
        a.ret();
      }
  );

  /**
   */
  public static final int sys_socket(int domain, int type, int protocol) {
    try {
      return (int)mh_sys_socket.invokeExact(domain,type,protocol);
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }


  //===========================================================================
  //42	sys_connect	int fd	struct sockaddr *uservaddr	int addrlen
  private static final MethodHandle mh_sys_connect = asm(
      int.class, int.class, long.class, int.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(42));
        a.mov(rdi, rdx);
        a.mov(rsi, rcx);
        a.mov(rdx, r8);
        a.syscall();
        a.ret();
      }
  );

  /**
   */
  public static final int sys_connect(int sockfd, long address, int addrlen) {
    try {
      return (int)mh_sys_connect.invokeExact(sockfd, address, addrlen);
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }


  //===========================================================================
  //48	sys_shutdown	int fd	int how
  private static final MethodHandle mh_sys_shutdown = asm(
      int.class, int.class, int.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(48));
        a.mov(rdi, rdx);
        a.mov(rsi, rcx);
        a.syscall();
        a.ret();
      }
  );

  /**
   * The shutdown() system call closes one or both channels of the socket
   * depending on the value of how, which is specified by
   * {@link z.znr.socket.Sockets.ShutDownType}.
   * <p>
   * Note: it is suggested that,
   *       "SHUT_RD can’t be used meaningfully for TCP sockets".
   */
  public static final int sys_shutdown(int sockfd, int how) {
    try {
      return (int)mh_sys_shutdown.invokeExact(sockfd, how);
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }


  //===========================================================================
  //49	sys_bind	int fd	struct sokaddr *umyaddr	int addrlen
  private static final MethodHandle mh_sys_bind = asm(
      int.class, int.class, long.class, int.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(49));
        a.mov(rdi, rdx);
        a.mov(rsi, rcx);
        a.mov(rdx, r8);
        a.syscall();
        a.ret();
      }
  );

  /**
   */
  public static final int sys_bind(int sockfd, long address, int addrlen) {
    try {
      return (int)mh_sys_bind.invokeExact(sockfd, address, addrlen);
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }


  //===========================================================================
  //50	sys_listen	int fd	int backlog
  private static final MethodHandle mh_sys_listen = asm(
      int.class, int.class, int.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(50));
        a.mov(rdi, rdx);
        a.mov(rsi, rcx);
        a.syscall();
        a.ret();
      }
  );

  /**
   */
  public static final int sys_listen(int sockfd, int backlog) {
    try {
      return (int)mh_sys_listen.invokeExact(sockfd,backlog);
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }


  //===========================================================================
  //54	sys_setsockopt
  //int fd	int level	int optname	 char *optval	int optlen
  private static final MethodHandle mh_sys_setsockopt = asm(
      int.class,
      int.class, int.class, int.class, long.class, int.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(54));
        a.mov(rdi, rdx);
        a.mov(rsi, rcx);
        a.mov(rdx, r8);
        a.mov(r10, r9);
        a.mov(r8, dword_ptr(rsp, 8));//XXX: movsxd
        a.syscall();
        a.ret();
      }
  );

  /**
   *
   * NOTE: this method allocate offheap memory, take care to invoke it
   *       frequently.
   */
  public static final int sys_setsockopt(
      int sockfd, int level, int optname, int optval) {
    long address = 0L;
    try {
      address = allocate(8);
      UNSAFE.putInt(address, optval);
      int rt = (int)mh_sys_setsockopt.invokeExact(
          sockfd, level, optname, address, 4);
      return rt;
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    } finally {
      if (address!=0L)
        free(address);
    }
  }


  //===========================================================================
  //55	sys_getsockopt
  //int fd  int level  int optname  char *optval  int *optlen
  private static final MethodHandle mh_sys_getsockopt = asm(
      int.class,
      int.class, int.class, int.class, long.class, long.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(55));
        a.mov(rdi, rdx);
        a.mov(rsi, rcx);
        a.mov(rdx, r8);
        a.mov(r10, r9);
        a.mov(r8, qword_ptr(rsp, 8));
        a.syscall();
        a.ret();
      }
  );

  /**
   *
   * NOTE: this method allocate offheap memory, take care to invoke it
   *       frequently.
   *
   * TODO: this syscall method, we try a new style other than older. That is,
   *       the returned value is no longer the suc or errno,
   *       it returns the business value direcently.
   *       It may be changed when needed in the future.
   */
  public static final int sys_getsockopt(
      int sockfd, int level, int optname) {
    long addrOptval=0L, addrOptlen=0L;
    try {
      addrOptval = allocate(8);
      addrOptlen  = allocate(8);//XXX: ignore now
      UNSAFE.putLong(addrOptval,0L);
//      UNSAFE.putLong(addrOptlen,0L);
      UNSAFE.putInt(addrOptlen,4);//TODO: optlen is not value-result?
      int res = (int)mh_sys_getsockopt.invokeExact(
          sockfd, level, optname, addrOptval, addrOptlen);
      int rt = UNSAFE.getInt(addrOptval);

      if (res<0) {
        throw new SyscallInvocationException(
            "Call to sys_getsockopt failed with errno "+(-res));
      }
      return rt;
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    } finally {
      if (addrOptval!=0L)
        free(addrOptval);
      if (addrOptlen!=0L)
        free(addrOptlen);
    }
  }


  //===========================================================================
  //97	sys_getrlimit	unsigned int resource	struct rlimit *rlim
  private static final MethodHandle mh_sys_getrlimit = asm(
      int.class, int.class, long.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(97));
        a.mov(rdi, rdx);
        a.mov(rsi, rcx);
        a.syscall();
        a.ret();
      }
  );

  /**
   *
   * NOTE: this method allocate offheap memory, take care to invoke it
   *       frequently.
   */
  public static final int sys_getrlimit(int resource, Rlimit rlimit) {
    long address = 0L;
    try {
      address = allocate(16);

      int rt = (int)mh_sys_getrlimit.invokeExact(resource,address);
      rlimit.rlim_cur = UNSAFE.getLong(address);
      rlimit.rlim_max = UNSAFE.getLong(address+8);
      return rt;
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    } finally {
      if (address!=0L)
        free(address);
    }
  }

  public static final class Rlimit {
    // bits/resource.h,bits/typesizes.h
    public static final long RLIM_INFINITY = -1;

    /* Number of open files.  */
    public static final int RLIMIT_NOFILE = 7;

    public long rlim_cur, rlim_max; //rlim_t is __syscall_ulong_t
  }


  //===========================================================================
  //160	sys_setrlimit	unsigned int resource	struct rlimit *rlim
  private static final MethodHandle mh_sys_setrlimit = asm(
      int.class, int.class, long.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(160));
        a.mov(rdi, rdx);
        a.mov(rsi, rcx);
        a.syscall();
        a.ret();
      }
  );

  /**
   * NOTE: need to be invoked in a privileged process to raise rlim_max
   */
  public static final int sys_setrlimit(
      int resource, long rlim_cur, long rlim_max) {
    long address = 0L;
    try {
      address = allocate(16);
      UNSAFE.putLong(address, rlim_cur);
      UNSAFE.putLong(address + 8, rlim_max);
      int rt = (int)mh_sys_setrlimit.invokeExact(resource,address);
      free(address);
      return rt;
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    } finally {
      if (address!=0L)
        free(address);
    }
  }


  //===========================================================================
  //213	sys_epoll_create	int size
  private static final MethodHandle mh_sys_epoll_create = asm(
      int.class, int.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(213));
        a.mov(rdi, rdx);
        a.syscall();
        a.ret();
      }
  );

  /**
   * creates an epoll instance.
   * <p>
   * man:
   * http://man7.org/linux/man-pages/man2/epoll_create1.2.html
   *
   * @param size - hint for size, (this hint is no longer required, but
   *             must still be greater than zero)
   * @return  on success, an epoll file descriptor; or on error, -errno
   */
  public static final int sys_epoll_create(int size){
    try {
      return (int)mh_sys_epoll_create.invokeExact(size);
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }


  //===========================================================================
  //232	sys_epoll_wait
  // int epfd	struct epoll_event *events	int maxevents	int timeout
  private static final MethodHandle mh_sys_epoll_wait = asm(
      int.class,
      int.class, long.class, int.class, int.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(232));
        a.mov(rdi, rdx);
        a.mov(rsi, rcx);
        a.mov(rdx, r8);
        a.mov(r10, r9);
        a.syscall();
        a.ret();
      }
  );

  public static final int sys_epoll_wait(
      int epfd , long events , int maxevents , int timeout) {
    try {
      return (int)mh_sys_epoll_wait.invokeExact(
          epfd, events, maxevents, timeout);
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }


  //===========================================================================
  //233	sys_epoll_ctl	int epfd	int op	int fd	struct epoll_event *event
  private static final MethodHandle mh_sys_epoll_ctl = asm(
      int.class,
      int.class, int.class, int.class, long.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(233));
        a.mov(rdi, rdx);
        a.mov(rsi, rcx);
        a.mov(rdx, r8);
        a.mov(r10, r9);
        a.syscall();
        a.ret();
      }
  );

//  /**
//   */
//  public static final int sys_epoll_ctl(
//      int epfd, int op, int fd, EPollEvent event) {
//    try {
//      return (int)mh_sys_epoll_ctl.invokeExact(epfd,op,fd,event.address());
//    } catch (Throwable t) {
//      throw new RuntimeException(t.getMessage());
//    }
//  }

  /**
   */
  public static final int sys_epoll_ctl(
      int epfd, int op, int fd, long address) {
    try {
      return (int)mh_sys_epoll_ctl.invokeExact(epfd,op,fd,address);
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }


  //===========================================================================
  // 288	sys_accept4
  // int fd	struct sockaddr *upeer_sockaddr	int *upeer_addrlen	int flags
  private static final MethodHandle mh_sys_accept4 = asm(
      int.class,
      int.class, long.class, int.class, int.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(288));
        a.mov(rdi, rdx);
        a.mov(rsi, rcx);
        a.mov(rdx, r8);
        a.mov(r10, r9);
        a.syscall();
        a.ret();
      }
  );

  /**
   */
  public static final int sys_accept4(
      int sockfd, long address, int addrlen ,int flags) {
    try {
      return (int)mh_sys_accept4.invokeExact(sockfd, address, addrlen, flags);
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }

  //===========================================================================
  //291	sys_epoll_create1	int flags
  private static final MethodHandle mh_sys_epoll_create1 = asm(
      int.class, int.class,
      //syscall number in rax, params in rdi, rsi, rdx, r10, r8, and r9
      a -> {
        a.mov(rax,imm(291));
        a.mov(rdi, rdx);
        a.syscall();
        a.ret();
      }
  );

  /**
   * creates an epoll instance with flags.
   * <p>
   * man:
   * http://man7.org/linux/man-pages/man2/epoll_create.2.html
   *
   * @param flags - 0 or EPOLL_CLOEXEC(note: there is no EPOLL_NONBLOCK at all).
   * @return  on success, an epoll file descriptor; or on error, -errno
   */
  public static final int sys_epoll_create1(int flags){
    try {
      return (int)mh_sys_epoll_create1.invokeExact(flags);
    } catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }



}
