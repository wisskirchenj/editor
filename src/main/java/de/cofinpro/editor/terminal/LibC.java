package de.cofinpro.editor.terminal;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static de.cofinpro.editor.terminal.AnsiEscape.red;
import static de.cofinpro.editor.terminal.LibC.TermIos.initialState;

public interface LibC extends Library {

    // load the C standard library for POSIX systems
    LibC INSTANCE = Native.load("c", LibC.class);
    int STDIN_FD = 0;
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
    long RAW_TOGGLE_LFLAGS = ECHO | ICANON | IEXTEN | ISIG;
    long RAW_TOGGLE_IFLAGS = IXON | ICRNL;
    long RAW_TOGGLE_OFLAGS = OPOST;

    int tcgetattr(int fildes, TermIos termIosP);

    int tcsetattr(int fildes, int optionalActions, TermIos termIosP);

    /* declaration from ioctl.h: "int ioctl(int fd, int cmd, ...);", where ... is void* of max-length 0x1fff */
    int	ioctl(int fildes, long command, WinSize args);
    String strerror(int errno);

    default void setRawMode() {
        if (Objects.isNull(initialState)) {
            TermIos.storeInitialState();
        }
        var rawModeTermIos = Objects.requireNonNull(initialState).withRawModeFlags();
        var returnCode = INSTANCE.tcsetattr(STDIN_FD, TCSAFLUSH, rawModeTermIos);
        if (returnCode != 0) {
            TermIos.errorExit(returnCode, "tcsetattr");
        }
    }

    default void setNormalMode() {
        var returnCode = INSTANCE.tcsetattr(STDIN_FD, TCSAFLUSH, initialState);
        if (returnCode != 0) {
            TermIos.errorExit(returnCode, "tcsetattr");
        }
    }

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
    @Slf4j
    class TermIos extends Structure implements Structure.ByReference { // from termios.h

        public long cIflag;                //input modes
        public long cOflag;                //output modes
        public long cCflag;                //control modes
        public long cLflag;                //local modes
        public byte[] cCc = new byte[20];  //special characters
        public long cIspeed;               //control modes
        public long cOspeed;               //local modes

        static TermIos initialState;

        TermIos withRawModeFlags() {
            var result = of(this);
            result.cLflag &= ~RAW_TOGGLE_LFLAGS;
            result.cIflag &= ~RAW_TOGGLE_IFLAGS;
            result.cOflag &= ~RAW_TOGGLE_OFLAGS;
            return result;
        }

        static void storeInitialState() {
            initialState = new TermIos();
            var returnCode = INSTANCE.tcgetattr(STDIN_FD, initialState);
            if (returnCode != 0) {
                errorExit(returnCode, "tcgetattr");
            }
        }

        private static TermIos of(TermIos t) {
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

        static void errorExit(int returnCode, String problemCall) {
            log.error(red("Problem with {}! Return code {}\n"), problemCall, returnCode);
            log.error(red("errno='{}'\n"), INSTANCE.strerror(Native.getLastError()));
            System.exit(returnCode);
        }
    }
}
