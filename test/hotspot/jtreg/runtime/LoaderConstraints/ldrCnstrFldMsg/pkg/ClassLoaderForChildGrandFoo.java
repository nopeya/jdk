/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

package pkg;

import java.util.*;
import java.io.*;

// This class loader loads Foo, Child, and Grand.
public class ClassLoaderForChildGrandFoo extends ClassLoader {

    ClassLoader l2 = null;

    private final Set<String> names = new HashSet<>();

    public ClassLoaderForChildGrandFoo(String... names) {
        for (String n : names) this.names.add(n);
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Create class loader l2 to load Parent and Foo.  And, pass this class
        // loader to l2 so l2 can use it to load Grand.
        if (name.contains("Parent") && l2 == null) {
            l2 = new ClassLoaderForParentFoo(this, "pkg.Foo", "pkg.Grand", "pkg.Parent");
            Class<?> b = l2.loadClass("pkg.Parent");
            return b;
        }
        if (!names.contains(name)) return super.loadClass(name, resolve);
        Class<?> result = findLoadedClass(name);

        if (result == null) {
            String filename = name.replace('.', '/') + ".class";
            try (InputStream data = getResourceAsStream(filename)) {
                if (data == null) throw new ClassNotFoundException();
                try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                    int b;
                    do {
                        b = data.read();
                        if (b >= 0) buffer.write(b);
                    } while (b >= 0);
                    byte[] bytes = buffer.toByteArray();
                    result = defineClass(name, bytes, 0, bytes.length);
                }
            } catch (IOException e) {
                throw new ClassNotFoundException("Error reading class file", e);
            }
        }
        if (resolve) resolveClass(result);
        return result;
    }

}
