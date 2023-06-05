package de.cofinpro.editor.terminal;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

public interface LibC extends Library {

    // load the C standard library for POSIX systems
    LibC INSTANCE = Native.load("c", LibC.class);
    int STDIN_FD = 1;
    int TCSAFLUSH = 2;
    long ISIG = 0x80;
    long ICANON = 0x100;
    long ECHO = 0x8;
    long IXON = 0x200;
    long ICRNL = 0x100;
    long IEXTEN = 0x400;
    long OPOST = 0x1;
    long VMIN = 6;
    long VTIME = 5;
    /* ------ ttycom.h defines TIOCGWINSZ and winsize struct ( class below) ----------------------
       #define TIOCGWINSZ      _IOR('t', 104, struct winsize)  // get window size
       ------ ioccom.h defines _IOR / _IOC-macros and consts needed  -----------------------------
       #define	_IOR(g,n,t)	_IOC(IOC_OUT,	(g), (n), sizeof(t)) gives
               _IOC( 0x40000000, 0x74=(int)'t', 0x68=104, 8=sizeof(struct winsize) )
       #define	_IOC(inout,group,num,len) (inout | ((len & IOCPARM_MASK) << 16) | ((group) << 8) | (num))
              with (len=8 & 01fff = 8) => 0x40087468 (!)
       #define	IOC_OUT		(unsigned long)0x40000000
       #define	IOCPARM_MASK	0x1fff	 */
    long TIOCGWINSZ = 0x40087468L;
    long TIOCSWINSZ = 0x80087467L;
    long TIOCGETA = 0x40447413;
    long RAW_TOGGLE_LFLAGS = ECHO | ICANON | IEXTEN | ISIG;
    long RAW_TOGGLE_IFLAGS = IXON | ICRNL;
    long RAW_TOGGLE_OFLAGS = OPOST;

    int tcgetattr(int fildes, TermIos termIosP);

    int tcsetattr(int fildes, int optionalActions, TermIos termIosP);

    /* declaration from ioctl.h: "int ioctl(int fd, int cmd, ...);", where ... is void* of max-length 0x1fff */
    int	ioctl(int fildes, long command, WinSize args);
    String strerror(int errno);

    @NoArgsConstructor
    @ToString
    @EqualsAndHashCode(callSuper = false)
    @Structure.FieldOrder({"wsRow", "wsCol", "wsXpixel", "wsYpixel"})
    class WinSize extends Structure implements Structure.ByReference { // from ttycom.h

        public short wsRow;     // rows, in characters
        public short wsCol;     // columns, in characters
        public short wsXpixel;  // horizontal size, pixels
        public short wsYpixel;  // vertical size, pixels
    }

    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @Structure.FieldOrder({"cIflag", "cOflag", "cCflag", "cLflag", "cCc", "cIspeed", "cOspeed"})
    class TermIos extends Structure implements Structure.ByReference { // from termios.h

        public long cIflag;                //input modes
        public long cOflag;                //output modes
        public long cCflag;                //control modes
        public long cLflag;                //local modes
        public byte[] cCc = new byte[20];  //special characters
        public long cIspeed;               //control modes
        public long cOspeed;               //local modes

        boolean inRawMode() {
            return (cOflag & LibC.OPOST) == 0x0;
        }

        static TermIos of(TermIos t) {
            var copy = new TermIos();
            copy.cIflag = t.cIflag;
            copy.cOflag = t.cOflag;
            copy.cCflag = t.cCflag;
            copy.cLflag = t.cLflag;
            copy.cCc = t.cCc.clone();
            copy.cIspeed = t.cIspeed;
            copy.cOspeed = t.cOspeed;
            return copy;
        }

        @Override
        public String toString() {
            return "TermIos{" +
                   "cIflag=0x%x".formatted(cIflag) +
                   ", cOflag=0x%x".formatted(cOflag) +
                   ", cCflag=0x%x".formatted(cCflag) +
                   ", cLflag=0x%x".formatted(cLflag) +
                   ", cIspeed=" + cIspeed +
                   ", cOspeed=" + cOspeed +
                   '}';
        }
    }
}
