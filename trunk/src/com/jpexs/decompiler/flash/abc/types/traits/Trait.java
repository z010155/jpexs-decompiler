/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc.types.traits;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Trait implements Serializable {

    public int name_index;
    public int kindType;
    public int kindFlags;
    public int metadata[] = new int[0];
    public long fileOffset;
    public byte bytes[];
    public static final int ATTR_Final = 0x1;
    public static final int ATTR_Override = 0x2;
    public static final int ATTR_Metadata = 0x4;
    public static final int TRAIT_SLOT = 0;
    public static final int TRAIT_METHOD = 1;
    public static final int TRAIT_GETTER = 2;
    public static final int TRAIT_SETTER = 3;
    public static final int TRAIT_CLASS = 4;
    public static final int TRAIT_FUNCTION = 5;
    public static final int TRAIT_CONST = 6;

    public String getModifiers(List<ABCContainerTag> abcTags, ABC abc, boolean isStatic) {
        String ret = "";
        if ((kindFlags & ATTR_Override) > 0) {
            ret += "override";
        }
        Multiname m = getName(abc);
        if (m != null) {
            String nsname = "";
            //if (abc.constants.constant_namespace[m.namespace_index].kind == Namespace.KIND_NAMESPACE) {
            {
                for (ABCContainerTag abcTag : abcTags) {
                    nsname = abcTag.getABC().nsValueToName(abc.constants.constant_namespace[m.namespace_index].getName(abc.constants));
                    if (nsname.equals("-")) {
                        break;
                    }
                    if (nsname.contains(".")) {
                        nsname = nsname.substring(nsname.lastIndexOf(".") + 1);
                    }
                    if (!nsname.equals("")) {
                        break;
                    }
                }
            }
            Namespace ns = m.getNamespace(abc.constants);

            if (nsname.contains(":")) {
                nsname = "";
            }


            if ((!nsname.equals("")) && (!nsname.equals("-"))) {
            } else {
                if (ns != null) {
                    if (ns.kind == Namespace.KIND_NAMESPACE) {
                        nsname = ns.getName(abc.constants);
                    }
                }
            }

            if ((!nsname.contains(":")) && (!nsname.equals(""))) {
                ret += " " + nsname;
            }
            if (ns != null) {
                ret += " " + ns.getPrefix(abc);
            }
        }
        if (isStatic) {
            ret += " static";
        }
        if ((kindFlags & ATTR_Final) > 0) {
            if (!isStatic) {
                ret += " final";
            }
        }
        return ret.trim();
    }

    @Override
    public String toString() {
        return "name_index=" + name_index + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    public String toString(ABC abc, List<String> fullyQualifiedNames) {
        return abc.constants.constant_multiname[name_index].toString(abc.constants, fullyQualifiedNames) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    public String convert(String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, boolean pcode, int scriptIndex, int classIndex, boolean highlight, List<String> fullyQualifiedNames, boolean paralel) {
        return abc.constants.constant_multiname[name_index].toString(abc.constants, fullyQualifiedNames) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    public String convertPackaged(String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, boolean pcod, int scriptIndex, int classIndex, boolean highlight, List<String> fullyQualifiedNames, boolean paralel) {
        return makePackageFromIndex(abc, name_index, convert(path, abcTags, abc, isStatic, pcod, scriptIndex, classIndex, highlight, fullyQualifiedNames, paralel));
    }

    public String convertHeader(String path, List<ABCContainerTag> abcTags, ABC abc, boolean isStatic, boolean pcode, int scriptIndex, int classIndex, boolean highlight, List<String> fullyQualifiedNames, boolean paralel) {
        return convert(path, abcTags, abc, isStatic, pcode, scriptIndex, classIndex, highlight, fullyQualifiedNames, paralel).trim();
    }

    protected String makePackageFromIndex(ABC abc, int name_index, String value) {
        Namespace ns = abc.constants.constant_multiname[name_index].getNamespace(abc.constants);
        if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
            String nsname = ns.getName(abc.constants);
            return "package " + nsname + "\r\n{\r\n" + value + "\r\n}";
        }
        return value;
    }

    public Multiname getName(ABC abc) {
        if (name_index == 0) {
            return null;
        } else {
            return abc.constants.constant_multiname[name_index];
        }
    }

    public abstract int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc);

    public String getPath(ABC abc) {
        Multiname name = getName(abc);
        Namespace ns = name.getNamespace(abc.constants);
        String packageName = ns.getName(abc.constants);
        String objectName = name.getName(abc.constants, new ArrayList<String>());
        return packageName + "." + objectName;
    }

    public void export(String directory, ABC abc, List<ABCContainerTag> abcList, boolean pcode, int scriptIndex, int classIndex, boolean isStatic, boolean paralel) throws IOException {
        Multiname name = getName(abc);
        Namespace ns = name.getNamespace(abc.constants);
        String packageName = ns.getName(abc.constants);
        String objectName = name.getName(abc.constants, new ArrayList<String>());
        File outDir = new File(directory + File.separatorChar + packageName.replace('.', File.separatorChar));
        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
                if (!outDir.exists()) {
                    throw new IOException("Cannot create directory " + outDir);
                }
            }
        }
        String fileName = outDir.toString() + File.separator + objectName + ".as";
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(convertPackaged("", abcList, abc, isStatic, pcode, scriptIndex, classIndex, false, new ArrayList<String>(), paralel).getBytes());
        }
    }
}
