package de.cofinpro.editor.terminal;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

public interface LibC extends Library {

    int SYSTEM_OUT_FD = 0;
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
    long TIOCGWINSZ = 0x40087468;
    long RAW_TOGGLE_LFLAGS = ECHO | ICANON | IEXTEN | ISIG;
    long RAW_TOGGLE_IFLAGS = IXON | ICRNL;
    long RAW_TOGGLE_OFLAGS = OPOST;

    // load the C standard library for POSIX systems
    LibC INSTANCE = Native.load("c", LibC.class);

    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @Structure.FieldOrder({"cIflag", "cOflag", "cCflag", "cLflag", "cCc", "cIspeed", "cOspeed"})
    class TermIos extends Structure {

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

    int tcgetattr(int fildes, TermIos termIosP);

    int tcsetattr(int fildes, int optionalActions, TermIos termIosP);

}
