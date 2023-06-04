package de.cofinpro.editor;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

import java.util.Arrays;

public interface LibC extends Library {

    int SYSTEM_OUT_FD = 0;
    long ISIG = 0x80;
    long ICANON = 0x100;
    long ECHO = 0x8;
    int TCSAFLUSH = 2;
    long IXON = 0x200;
    long ICRNL = 0x100;
    long IEXTEN = 0x400;
    long OPOST = 0x1;
    long VMIN = 6;
    long VTIME = 5;
    long TIOCGWINSZ = 0x40087468;

    // we're loading the C standard library for POSIX systems
    LibC INSTANCE = Native.load("c", LibC.class);

    @Structure.FieldOrder({"c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_cc", "c_ispeed", "c_ospeed"})
    class Termios extends Structure {
        public long c_iflag;    //input modes
        public long c_oflag;    //output modes
        public long c_cflag;    //control modes
        public long c_lflag;    //local modes
        public byte[] c_cc = new byte[20];     //special characters
        public long c_ispeed;    //control modes
        public long c_ospeed;    //local modes

        public Termios() {
        }

        public static Termios of(Termios t) {
            var copy = new Termios();
            copy.c_iflag = t.c_iflag;
            copy.c_oflag = t.c_oflag;
            copy.c_cflag = t.c_cflag;
            copy.c_lflag = t.c_lflag;
            copy.c_cc = t.c_cc.clone();
            copy.c_ispeed = t.c_ispeed;
            copy.c_ospeed = t.c_ospeed;
            return copy;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            Termios termios = (Termios) o;

            if (c_iflag != termios.c_iflag) return false;
            if (c_oflag != termios.c_oflag) return false;
            if (c_cflag != termios.c_cflag) return false;
            if (c_lflag != termios.c_lflag) return false;
            if (c_ispeed != termios.c_ispeed) return false;
            if (c_ospeed != termios.c_ospeed) return false;
            return Arrays.equals(c_cc, termios.c_cc);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (int) (c_iflag ^ (c_iflag >>> 32));
            result = 31 * result + (int) (c_oflag ^ (c_oflag >>> 32));
            result = 31 * result + (int) (c_cflag ^ (c_cflag >>> 32));
            result = 31 * result + (int) (c_lflag ^ (c_lflag >>> 32));
            result = 31 * result + Arrays.hashCode(c_cc);
            result = 31 * result + (int) (c_ispeed ^ (c_ispeed >>> 32));
            result = 31 * result + (int) (c_ospeed ^ (c_ospeed >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "Termios{" +
                   "c_iflag=0x%x".formatted(c_iflag) +
                   ", c_oflag=0x%x".formatted(c_oflag) +
                   ", c_cflag=0x%x".formatted(c_cflag) +
                   ", c_lflag=0x%x".formatted(c_lflag) +
                   ", c_cc=" + Arrays.toString(c_cc) +
                   ", c_ispeed=" + c_ispeed +
                   ", c_ospeed=" + c_ospeed +
                   '}';
        }
    }

    int tcgetattr(int fildes, Termios termios_p);

    int tcsetattr(int fildes, int optional_actions, Termios termios_p);

}
