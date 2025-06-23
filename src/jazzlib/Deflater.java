/* Deflater.java - Compress a data stream
   Copyright (C) 1999, 2000, 2001, 2004 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package jazzlib;

/**
 * This is the Deflater class.  The deflater class compresses input
 * with the deflate algorithm described in RFC 1951.  It has several
 * compression levels and three different strategies described below.
 * 
 * This class is <i>not</i> thread safe.  This is inherent in the API, due
 * to the split of deflate and setInput.
 * 
 * @author Jochen Hoenicke
 * @author Tom Tromey
 */

/**
 * @author Shinovon
 */
public final class Deflater
{
	  /**
	   * The best and slowest compression level.  This tries to find very
	   * long and distant string repetitions.  
	   */
	  public static final int BEST_COMPRESSION = 9;
	  /**
	   * The worst but fastest compression level.  
	   */
	  public static final int BEST_SPEED = 1;
	  /**
	   * The default compression level.
	   */
	  public static final int DEFAULT_COMPRESSION = -1;
	  /**
	   * This level won't compress at all but output uncompressed blocks.
	   */
	  public static final int NO_COMPRESSION = 0;

	  /**
	   * The default strategy.
	   */
	  public static final int DEFAULT_STRATEGY = 0;
	  /**
	   * This strategy will only allow longer string repetitions.  It is
	   * useful for random data with a small character set.
	   */
	  public static final int FILTERED = 1;

	  /** 
	   * This strategy will not look for string repetitions at all.  It
	   * only encodes with Huffman trees (which means, that more common
	   * characters get a smaller encoding.  
	   */
	  public static final int HUFFMAN_ONLY = 2;

	  /**
	   * The compression method.  This is the only method supported so far.
	   * There is no need to use this constant at all.
	   */
	  public static final int DEFLATED = 8;
	  
	private int level;
    private boolean noHeader;
    private int state;
    private long totalOut;
    private DeflaterPending pending;
    private DeflaterEngine engine;
    
    public Deflater() {
        this(-1, false);
    }
    
    public Deflater(final int n) {
        this(n, false);
    }
    
    public Deflater(int lvl, final boolean nowrap) {
        super();
        if (lvl == -1) {
            lvl = 6;
        }
        else if (lvl < 0 || lvl > 9) {
            throw new IllegalArgumentException();
        }
        this.pending = new DeflaterPending();
        this.engine = new DeflaterEngine(this.pending);
        this.noHeader = nowrap;
        this.setStrategy(0);
        this.setLevel(lvl);
        this.reset();
    }
    
    public final void reset() {
        this.state = (this.noHeader ? 16 : 0);
        this.totalOut = 0L;
        this.pending.reset();
        this.engine.reset();
    }
    
    public final int getTotalIn() {
        return (int)this.engine.getTotalIn();
    }
    
    public final int getTotalOut() {
        return (int)this.totalOut;
    }
    
    final void flush() {
        this.state |= 0x4;
    }
    
    public final void finish() {
        this.state |= 0xC;
    }
    
    public final boolean finished() {
        return this.state == 30 && this.pending.isFlushed();
    }
    
    public final boolean needsInput() {
        return this.engine.needsInput();
    }
    
    public final void setInput(final byte[] array, final int n, final int n2) {
        if ((this.state & 0x8) != 0x0) {
            throw new IllegalStateException("finish()/end() already called");
        }
        this.engine.setInput(array, n, n2);
    }
    
    public final void setLevel(int lvl) {
        if (lvl == -1) {
            lvl = 6;
        }
        else if (lvl < 0 || lvl > 9) {
            throw new IllegalArgumentException();
        }
        if (this.level != lvl) {
            this.level = lvl;
            this.engine.setLevel(lvl);
        }
    }
    
    public final void setStrategy(final int n) {
        if (n != 0 && n != 1 && n != 2) {
            throw new IllegalArgumentException();
        }
        this.engine.setStrategy(n);
    }
    
    public final int deflate(final byte[] output, int offset, int length) {
        final int origLength = length;
        if (this.state == 127) {
            throw new IllegalStateException("Deflater closed");
        }
        if (this.state < 16) {
            int level_flags;
            if ((level_flags = this.level - 1 >> 1) < 0 || level_flags > 3) {
                level_flags = 3;
            }
            int header = 0x7800 | level_flags << 6;
            if ((this.state & 0x1) != 0x0) {
                header |= 0x20;
            }
            this.pending.writeShortMSB(header + (31 - header % 31));
            if ((this.state & 0x1) != 0x0) {
                final int chksum = this.engine.getAdler();
                this.engine.resetAdler();
                this.pending.writeShortMSB(chksum >> 16);
                this.pending.writeShortMSB(chksum & 0xFFFF);
            }
            this.state = (0x10 | (this.state & 0xC));
        }
        while (true) {
            final int count = this.pending.flush(output, offset, length);
            offset += count;
            this.totalOut += count;
            if ((length -= count) == 0 || this.state == 30) {
                return origLength - length;
            }
            if (this.engine.deflate((this.state & 0x4) != 0x0, (this.state & 0x8) != 0x0)) {
                continue;
            }
            if (this.state == 16) {
                return origLength - length;
            }
            if (this.state == 20) {
                if (this.level != 0) {
                    for (int i = 8 + (-this.pending.getBitCount() & 0x7); i > 0; i -= 10) {
                        this.pending.writeBits(2, 10);
                    }
                }
                this.state = 16;
            }
            else {
                if (this.state != 28) {
                    continue;
                }
                this.pending.alignToByte();
                if (!this.noHeader) {
                    final int adler = this.engine.getAdler();
                    this.pending.writeShortMSB(adler >> 16);
                    this.pending.writeShortMSB(adler & 0xFFFF);
                }
                this.state = 30;
            }
        }
    }
}