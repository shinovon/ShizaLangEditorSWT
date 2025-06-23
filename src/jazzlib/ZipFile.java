/* net.sf.jazzlib.ZipFile
   Copyright (C) 2001, 2002, 2003 Free Software Foundation, Inc.

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

import java.io.*;
import java.util.*;

import javax.microedition.io.file.FileConnection;

/**
 * @author Shinovon
 */
public final class ZipFile
{
    private byte[] b;
    private int off;
    private int length;
    private Hashtable entries;
    
    public ZipFile(final InputStream inputStream) throws ZipException, IOException {
        super();
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final byte[] array = new byte[1024];
        int read;
        while ((read = inputStream.read(array)) > 0) {
            byteArrayOutputStream.write(array, 0, read);
        }
        this.b = byteArrayOutputStream.toByteArray();
        this.off = 0;
        this.length = this.b.length;
        this.check();
    }
    
    public ZipFile(final FileConnection fc) throws ZipException, IOException {
    	this(fc.openInputStream());
    }
    
    private void check() throws ZipException {
        if (this.length < 4) {
            throw new ZipException("Not a valid zip archive");
        }
        if (((this.b[this.off] & 0xFF) | (this.b[this.off + 1] & 0xFF) << 8 | (this.b[this.off + 2] & 0xFF) << 16 | (this.b[this.off + 3] & 0xFF) << 24) != 0x4034B50L) {
            throw new ZipException("Not a valid zip archive");
        }
    }
    
    private void readEntries() throws ZipException, IOException {
        final RandomAccessFile raf = new RandomAccessFile(this.b, this.off, this.length);
        int pos = this.length - 22;
        while (pos >= Math.max(0, pos - 65536)) {
            raf.seek(this.off + pos--);
            if (raf.readInt() == 101010256L) {
                if (raf.skip(6L) != 6L) {
                    throw new EOFException();
                }
                final int count = raf.readShort();
                if (raf.skip(4L) != 4L) {
                    throw new EOFException();
                }
                final int centralOffset = raf.readInt();
                this.entries = new Hashtable(count + count / 2);
                raf.seek(this.off + centralOffset);
                for (int j = 0; j < count; ++j) {
                    if (raf.readInt() != 33639248L) {
                        throw new ZipException("Wrong Central Directory signature");
                    }
                    raf.skip(6L);
                    final int method = raf.readShort();
                    final int dostime = raf.readInt();
                    final int crc = raf.readInt();
                    final int csize = raf.readInt();
                    final int size = raf.readInt();
                    final int nameLen = raf.readShort();
                    final int extraLen = raf.readShort();
                    final int commentLen = raf.readShort();
                    raf.skip(8L);
                    final int offset = raf.readInt();
                    final String name = raf.readUTF(nameLen);
                    final ZipEntry entry;
                    (entry = new ZipEntry(name)).setMethod(method);
                    entry.setCrc((long)crc & 0xFFFFFFFFL);
                    entry.setSize((long)size & 0xFFFFFFFFL);
                    entry.setCompressedSize((long)csize & 0xFFFFFFFFL);
                    entry.setDOSTime(dostime);
                    if (extraLen > 0) {
                        final byte[] array = new byte[extraLen];
                        raf.readFully(array);
                        entry.setExtra(array);
                    }
                    if (commentLen > 0) {
                        entry.setComment(raf.readUTF(commentLen));
                    }
                    entry.offset = offset;
                    this.entries.put(name, entry);
                }
                return;
            }
        }
        throw new ZipException("central directory not found, probably not a zip archive");
    }
    
    public final Enumeration entries() {
        try {
            return this.getEntries().elements();
        }
        catch (IOException ex) {
            return new Hashtable().elements();
        }
    }
    
    private Hashtable getEntries() throws IOException {
        if (this.entries == null) {
            this.readEntries();
        }
        return this.entries;
    }
}

