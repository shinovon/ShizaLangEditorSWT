package jazzlib;

import java.io.*;

final class RandomAccessFile extends ByteArrayInputStream {
    private int anInt1216;
    
    public RandomAccessFile(final byte[] array, final int n, final int n2) {
        super(array, n, n2);
    }
    
    public final synchronized int read() {
        if (this.anInt1216 > 0 && this.pos == this.count) {
            this.anInt1216 = 0;
            ++this.pos;
            return 0;
        }
        return super.read();
    }
    
    public final synchronized int read(final byte[] array, final int n, final int n2) {
        int read = super.read(array, n, n2);
        if (this.anInt1216 > 0 && read < n2) {
            this.anInt1216 = 0;
            if (this.pos < this.count) {
                array[n + read++] = this.buf[this.pos++];
            }
            else if (this.pos == this.count) {
                if (read == -1) {
                    read = 0;
                }
                array[n + read++] = 0;
                ++this.pos;
            }
        }
        return read;
    }
    
    final void seek(final int pos) {
        this.pos = pos;
    }
    
    final void readFully(final byte[] array) throws EOFException {
        if (this.read(array, 0, array.length) != array.length) {
            throw new EOFException();
        }
    }
    
    final synchronized int readShort() throws EOFException {
        final int read = this.read();
        final int read2;
        if ((read2 = this.read()) == -1) {
            throw new EOFException();
        }
        return (read & 0xFF) | (read2 & 0xFF) << 8;
    }
    
    final synchronized int readInt() throws EOFException {
        final int read = this.read();
        final int read2 = this.read();
        final int read3 = this.read();
        final int read4;
        if ((read4 = this.read()) == -1) {
            throw new EOFException();
        }
        return (read & 0xFF) | (read2 & 0xFF) << 8 | ((read3 & 0xFF) | (read4 & 0xFF) << 8) << 16;
    }
    
    final synchronized String readUTF(final int n) throws EOFException {
        if (n > this.count - this.pos) {
            throw new EOFException();
        }
        final byte[] array = new byte[n];
        this.readFully(array);
        try {
            return new String(array, 0, n, "UTF-8");
        }
        catch (Exception ex2) {
            try {
                return new String(array);
            }
            catch (Exception ex) {
                throw new Error(ex.toString());
            }
        }
    }
}